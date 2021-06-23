package com.example.letter.Notification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAMH3jMOQ:APA91bGzdnoUyc26rZWu6Myyvq0Gn_2W15zIA4tiKcoj8chHafsfZNhS0hVxa28dqS5MLr3n59fCrh3ASD48u1ajXx0xqYdAqhZUscP78xXj8Fk5GTFY2PDP2bJHGMVFsD6LunhUA0s9"
    })
    @POST("fcm/send")
    Call<Response> sendNotification(@Body Sender body);
}
