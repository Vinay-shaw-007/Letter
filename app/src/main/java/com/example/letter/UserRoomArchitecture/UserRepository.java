package com.example.letter.UserRoomArchitecture;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class UserRepository {
    private final UserDao userDao;
    private final LiveData<List<UserEntity>> allUsers;

    UserRepository(Application application) {
        UserDatabase userDatabase = UserDatabase.getInstance(application);
        userDao = userDatabase.userDao();
        allUsers = userDao.getAllUsers();


    }
    void insert(UserEntity userEntity){
        UserDatabase.databaseWriteExecutor.execute(() -> {
            userDao.insert(userEntity);
            userDao.update(userEntity);
            });
    }
    void FirebaseUsers(){
        UserDatabase.databaseWriteExecutor.execute(userDao::deleteAll);
                FirebaseDatabase.getInstance().getReference().child("DashBoard")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            Log.d("RoomDatabaseTesting", "onDataChange: ");
                            for (DataSnapshot snapshot1 : snapshot.getChildren()){
                                if (snapshot1 != null) {
                                    UserEntity entity = snapshot1.getValue(UserEntity.class);
                                    assert entity != null;
                                    String myUid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                                    String hisUid = entity.getUid();
                                    Log.d("RoomDatabaseTesting", "ViewModel: myUid = " + myUid);
                                    Log.d("RoomDatabaseTesting", "ViewModel: hisUid = " + hisUid);
                                    if (!hisUid.equals(myUid))
                                        insert(entity);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });

    }

    void update(UserEntity userEntities){
        UserDatabase.databaseWriteExecutor.execute(() -> userDao.update(userEntities));
    }
    void delete(UserEntity userEntities){
        deleteFromFirebase(userEntities);
    }
    private void deleteFromFirebase(UserEntity userEntities){
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("DashBoard")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        reference.child(userEntities.getUid()).removeValue()
                .addOnCompleteListener(task -> UserDatabase.databaseWriteExecutor.execute(() -> userDao.delete(userEntities)));
        List<UserEntity> roomDatabaseUser = allUsers.getValue();
        if (roomDatabaseUser != null){
            Log.d("FetchContactUsers", "getContactFromUser: roomDatabaseUser = "+roomDatabaseUser.size());

        }
    }

    void deleteAll(){
        UserDatabase.databaseWriteExecutor.execute(userDao::deleteAll);
    }
    LiveData<List<UserEntity>> getAllUsers(){
        return allUsers;
    }

}
