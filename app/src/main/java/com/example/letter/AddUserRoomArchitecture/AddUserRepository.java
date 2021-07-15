package com.example.letter.AddUserRoomArchitecture;

import android.app.Application;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.letter.Utils.IsoToPrefix;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static android.content.Context.TELEPHONY_SERVICE;

public class AddUserRepository {
    private final AddUserDao addUserDao;
    private final LiveData<List<AddUserEntity>> allContactUser;
    private final Application application;
    private final List<AddUserEntity> contactUser = new ArrayList<>();
    private List<AddUserEntity> roomDatabaseUser = new ArrayList<>();
    public AddUserRepository(Application application) {
        this.application = application;
        AddUserDatabase database = AddUserDatabase.getInstance(application);
        addUserDao = database.addUserDao();
        allContactUser = addUserDao.getAllContactUsers();
    }

    void insert(AddUserEntity addUserEntity){
        AddUserDatabase.databaseWriteExecutor.execute(() -> {
            addUserDao.insert(addUserEntity);
            addUserDao.update(addUserEntity);
        });
    }
    void update(AddUserEntity addUserEntity){
        AddUserDatabase.databaseWriteExecutor.execute(() -> {
            addUserDao.update(addUserEntity);
        });
    }

    void getContactFromUser(){
        AddUserDatabase.databaseWriteExecutor.execute(this::fetchContactFromUserPhone);
    }

    private void fetchContactFromUserPhone() {

        contactUser.clear();
        String ISOPrefix = getCountryISO();
        //Initialize uri
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        //Sort by ascending
        String sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME;//+"ASC"
        //Initialize cursor
        Cursor cursor = application.getContentResolver().query(uri, null, null, null, sort);
        if (cursor.getCount()>0) {
            //When cursor is greater than 0
            //Use while loop
            while (cursor.moveToNext()) {
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
                Cursor phoneCursor = application.getContentResolver().query(phoneUri, null, selection, new String[]{id}, null);
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
                        number = number.replace(String.valueOf(number.charAt(0)), "");
                    }
                    if (!String.valueOf(number.charAt(0)).equals("+"))
                        number = ISOPrefix + number;
                    AddUserEntity user = new AddUserEntity("",name,"",number);
                    //fetch only user that are logged into this app
                    AddUserDatabase.databaseWriteExecutor.execute(() -> getUserDetails(user));
                    //Close phone cursor
                    phoneCursor.close();
                }
            }
            cursor.close();
        }
        AddUserDatabase.databaseWriteExecutor.execute(new Runnable() {
            @Override
            public void run() {
                roomDatabaseUser = allContactUser.getValue();
                Log.d("FetchContactUsers", "getContactFromUser: contactUser = "+contactUser.size());
                if (roomDatabaseUser != null){
                    Log.d("FetchContactUsers", "getContactFromUser: roomDatabaseUser = "+roomDatabaseUser.size());

                }

                if (roomDatabaseUser != null && contactUser.size() != roomDatabaseUser.size()){
                    Log.d("FetchContactUsers", "getContactFromUser: New Data");
                    addUserDao.deleteAll();
                    for (AddUserEntity entity : contactUser){
                        insert(entity);
                    }
                }
                else if (roomDatabaseUser != null) {
                        Log.d("FetchContactUsers", "getContactFromUser: update Data");
                        for (AddUserEntity entity : contactUser) {
                            update(entity);
                        }
                    }
            }
        });
    }

    private void getUserDetails(AddUserEntity user) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("users");
        Query query = reference.orderByChild("phoneNumber").equalTo(user.getPhoneNumber());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot snapshot1 : snapshot.getChildren()){
                        AddUserEntity addUserEntity = snapshot1.getValue(AddUserEntity.class);
                        Objects.requireNonNull(addUserEntity).setName(user.getName());
                        String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
                        String hisUid = addUserEntity.getPhoneNumber();
                        Log.d("FetchContactUsers", "onDataChange: myNumber = "+myUid+" hisNumber = "+hisUid);
                        if (!hisUid.equals(myUid)){
//                            insert(addUserEntity);
                            Log.d("FetchContactUsers", "Name: "+addUserEntity.getName());
                            Log.d("FetchContactUsers", "phoneNumber: "+addUserEntity.getPhoneNumber());
                            contactUser.add(addUserEntity);
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }

    LiveData<List<AddUserEntity>> getAllContactUser(){
        return allContactUser;
    }
    void deleteAll(){
        AddUserDatabase.databaseWriteExecutor.execute(addUserDao::deleteAll);
    }
    private String getCountryISO(){
        String ISO = null;
        TelephonyManager manager = (TelephonyManager) application.getSystemService(TELEPHONY_SERVICE);
        if (manager.getNetworkCountryIso() != null){
            if (!manager.getNetworkCountryIso().equals("")){
                ISO = manager.getNetworkCountryIso();
            }
        }
        assert ISO != null;
        return IsoToPrefix.getPhone(ISO);
    }
}
