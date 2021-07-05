package com.example.letter.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letter.Adapter.FindUserAdapter;
import com.example.letter.Models.User;
import com.example.letter.R;
import com.example.letter.Utils.IsoToPrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class FindUserActivity extends AppCompatActivity implements FindUserAdapter.newUserClicked {

    private RecyclerView mRecyclerView;
    private FindUserAdapter mAdapter;
    private ArrayList<User> userList, contactList, groupMembers;
    private FirebaseDatabase database;
    private boolean from_group;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        Toolbar toolbar = findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Add Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        from_group = getIntent().getBooleanExtra("addMembers", false);
        if (getIntent() != null && getIntent().getExtras() != null && getIntent().hasExtra("groupMembers")){
            Bundle bundle = getIntent().getExtras();
            groupMembers = (ArrayList<User>) bundle.getSerializable("groupMembers");
            FindUserAdapter.setSelectedMembers(groupMembers);
        }
        database = FirebaseDatabase.getInstance();

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setCancelable(true);
        dialog.setMessage("Loading Users");

        userList = new ArrayList<>();
        contactList = new ArrayList<>();

//        mRecyclerView = findViewById(R.id.newUserRV);
//        mRecyclerView.setHasFixedSize(true);
//        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fetchUsers();
        checkPermission();
//        fetchUsers();

    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(FindUserActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(FindUserActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, 100);
        }else {
            //fetch contact
            getContactList();
        }
    }

    private void getContactList() {
        String ISOPrefix = getCountryIS();
        //Clear arrayList
        contactList.clear();
        //Initialize uri
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Sort by ascending
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;//+"ASC"
        //Initialize cursor
        Cursor cursor = getContentResolver().query(uri, null, null, null, sort);
        //Check condition
        if (cursor.getCount()>0){
            //When cursor is greater than 0
            //Use while loop
            while (cursor.moveToNext()){
                //Cursor move to next
                //Get contact id
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                //Get contact name
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                //Initialize phoneUri
                Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                //Initialize selection
                String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =?";
                //Initialize phoneCursor
                Cursor phoneCursor = getContentResolver().query(phoneUri, null, selection, new String[]{id}, null);
                //Check condition
                if (phoneCursor.moveToNext()){
                    //When phone cursor move to next
                    String number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    //Initialize contact model and add data
                    //Replacing the some character with space
                    number = number.replace(" ","");
                    number = number.replace("_","");
                    number = number.replace("-","");
                    number = number.replace("(","");
                    number = number.replace(")","");
                    if (String.valueOf(number.charAt(0)).equals("0")){
                        number = number.replace(String.valueOf(number.charAt(0)),"");
                    }
                    if (!String.valueOf(number.charAt(0)).equals("+"))
                        number = ISOPrefix + number;
                    User user = new User("",name,number,"");
                    //Add model in the arrayList
                    contactList.add(user);
                    //fetch only user that are logged into this app
                    getUserDetails(user);
                    //Close phone cursor
                    phoneCursor.close();
                }

            }
            cursor.close();
        }

    }

    private void getUserDetails(User user) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("phoneNumber").equalTo(user.getPhoneNumber());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        User user1 = snapshot1.getValue(User.class);
                        Objects.requireNonNull(user1).setName(user.getName());
                        userList.add(user1);
//                        return;
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getCountryIS(){
        String ISO = null;
        getApplicationContext();
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(TELEPHONY_SERVICE);
        if (manager.getNetworkCountryIso() != null){
            if (!manager.getNetworkCountryIso().equals("")){
                ISO = manager.getNetworkCountryIso();
            }
        }
        assert ISO != null;
        return IsoToPrefix.getPhone(ISO);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search,menu);
        if (from_group) getMenuInflater().inflate(R.menu.form_group,menu);
        MenuItem item = menu.findItem(R.id.searchBar);
        android.widget.SearchView searchView = (android.widget.SearchView) item.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUsers(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (searchView.getQuery().length() != 0) {
                    searchUsers(newText);
                }
                else{
                    searchUsers("");
                }
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Check condition
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //When permission is granted
            //Call method
            getContactList();
        }else {
            //When permission is denied
            Toast.makeText(this, "Permission Required", Toast.LENGTH_SHORT).show();
            //Call checkPermission method
            checkPermission();
        }
    }
    private void searchUsers(String query) {
        ArrayList<User> searchList = new ArrayList<>();
        for (int j=0; j<userList.size(); j++){
            if(userList.get(j).getName().toLowerCase().startsWith(query.toLowerCase().trim())){
                searchList.add(userList.get(j));
            }
            mAdapter = new FindUserAdapter(searchList, FindUserActivity.this, FindUserActivity.this, from_group);
            mRecyclerView.setAdapter(mAdapter);
            mAdapter.notifyDataSetChanged();
            if (query.equals("")){
                mAdapter = new FindUserAdapter(userList, FindUserActivity.this, FindUserActivity.this, from_group);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

        }
    }
    private void fetchUsers(){
        mRecyclerView = findViewById(R.id.newUserRV);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new FindUserAdapter(userList, FindUserActivity.this, FindUserActivity.this, from_group);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
        return super.onSupportNavigateUp();
    }

    @Override
    public void onUserClicked(User newUser) {
        database.getReference().child("DashBoard")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child(newUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    database.getReference()
                            .child("DashBoard")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child(newUser.getUid())
                            .setValue(newUser);
                    Toast.makeText(FindUserActivity.this, "Added in chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.done){

            ArrayList<User> selectedMembers1 = FindUserAdapter.getSelectedMembers();

            Intent i = new Intent(this, NewGroup.class);
            Bundle bundle = new Bundle();


            bundle.putSerializable("groupMembers", selectedMembers1);

            i.putExtras(bundle);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
            finish();
        }
        return true;
    }
}