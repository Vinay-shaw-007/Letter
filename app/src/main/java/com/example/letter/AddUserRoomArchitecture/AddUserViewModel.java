package com.example.letter.AddUserRoomArchitecture;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AddUserViewModel extends AndroidViewModel{
    private final AddUserRepository repository;
    private final LiveData<List<AddUserEntity>> allContactUser;
    public AddUserViewModel(@NonNull @NotNull Application application) {
        super(application);
        repository = new AddUserRepository(application);
        allContactUser = repository.getAllContactUser();
    }
    public LiveData<List<AddUserEntity>> getAllContactUser(){
        return allContactUser;
    }
    public void deleteAll(){
        repository.deleteAll();
    }
    public void fetch(){
        repository.getContactFromUser();
    }
}
