package com.example.letter.UserRoomArchitecture;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.letter.UserRoomArchitecture.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(UserEntity users);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(UserEntity users);

    @Delete
    void delete(UserEntity users);

    @Query("Delete From USER_INFORMATION")
    void deleteAll();

    @Transaction
    @Query("SELECT * FROM USER_INFORMATION")
    LiveData<List<UserEntity>> getAllUsers();
}
