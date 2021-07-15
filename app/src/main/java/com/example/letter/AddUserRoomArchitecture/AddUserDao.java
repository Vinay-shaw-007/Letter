package com.example.letter.AddUserRoomArchitecture;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AddUserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(AddUserEntity user);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(AddUserEntity user);

    @Transaction
    @Query("SELECT * FROM USER_CONTACT_INFORMATION")
    LiveData<List<AddUserEntity>> getAllContactUsers();

    @Query("DELETE FROM USER_CONTACT_INFORMATION")
    void deleteAll();
}
