package com.example.letter.UserRoomArchitecture;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.letter.UserRoomArchitecture.UserEntity;
import com.example.letter.UserRoomArchitecture.UserRepository;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final LiveData<List<UserEntity>> allUsers;
    public UserViewModel(@NonNull @NotNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
        allUsers = userRepository.getAllUsers();
    }
    // used methods
    public LiveData<List<UserEntity>> getAllUsers(){
        return allUsers;
    }
    public void FirebaseUsers(){
        userRepository.FirebaseUsers();
    }
    public void delete(UserEntity userEntities) {
        userRepository.delete(userEntities);
    }
    // not yet used methods
    public void insert(UserEntity userEntity){
        userRepository.insert(userEntity);
    }
    public void update(UserEntity userEntities) {
        userRepository.update(userEntities);
    }
    public void deleteAll() {
        userRepository.deleteAll();
    }

}
