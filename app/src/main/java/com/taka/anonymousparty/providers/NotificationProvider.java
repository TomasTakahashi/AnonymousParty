package com.taka.anonymousparty.providers;

import com.taka.anonymousparty.models.FCMBody;
import com.taka.anonymousparty.models.FCMResponse;
import com.taka.anonymousparty.retrofit.IFCMApi;
import com.taka.anonymousparty.retrofit.RetrofitClient;

import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider(){    }

    public Call<FCMResponse> sendNotification(FCMBody body){
        return RetrofitClient.getClient(url).create(IFCMApi.class).send(body);
    }

}
