package com.example.letter.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.example.letter.R;
import com.google.firebase.auth.FirebaseAuth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;


public class ShowImageActivity extends AppCompatActivity {

    private ImageView viewImage;
    private FrameLayout frameLayout;
    private boolean hideToolbar = true;
    private FileOutputStream outputStream = null;
    private TextView name1, time1;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        Toolbar toolbar = findViewById(R.id.customToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        frameLayout = findViewById(R.id.frameLayout);
        name1 = findViewById(R.id.profileName);
        time1 = findViewById(R.id.time);
        viewImage = findViewById(R.id.viewImage);

        ImageView backBtn = findViewById(R.id.backBtn);
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String name = getIntent().getStringExtra("userName");
        String time = getIntent().getStringExtra("time");
        String senderId = getIntent().getStringExtra("senderId");
        if (senderId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
            name1.setText("You");
        }else{
            name1.setText(name);
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd LLL, HH:mm", Locale.getDefault());
        Date date = new Date(Long.parseLong(time));
        time1.setText(sdf.format(date));
        Uri uri = Uri.parse(imageUrl);
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            viewImage.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("showImage", "onImageClicked: ImageUrl "+imageUrl);
        Log.d("showImage", "onImageClicked: Uri "+uri);

//        Glide.with(this).load(imageUrl).into(viewImage);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        frameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(ShowImageActivity.this, "Toolbar Hide8", Toast.LENGTH_SHORT).show();
                if (hideToolbar){
                    hideToolbar = false;
                    getSupportActionBar().hide();
                }else {
                    hideToolbar = true;
                    getSupportActionBar().show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_image_menu,menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.download) {
            saveToGallery();
        } else {
            throw new IllegalStateException("Unexpected value: " + item.getItemId());
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void saveToGallery() {
        BitmapDrawable bitmapDrawable = (BitmapDrawable)viewImage.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();


        File file = Environment.getExternalStorageDirectory();

        File dir = new File(file.getAbsolutePath() + "/Letter/Media");
        dir.mkdir();

//        String fileName = System.currentTimeMillis()+".png";
        File outFile = new File(dir,System.currentTimeMillis()+".jpg");
        try {
            outputStream = new FileOutputStream(outFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,outputStream);
//            bitmap.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        }catch (Exception e){
            Log.d("bitmap", "saveToGallery: "+e.getMessage());
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        try {
            if (outputStream == null) throw new AssertionError();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
    }
}