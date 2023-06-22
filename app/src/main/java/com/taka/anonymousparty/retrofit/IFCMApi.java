package com.taka.anonymousparty.retrofit;

import com.taka.anonymousparty.models.FCMBody;
import com.taka.anonymousparty.models.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {

    @Headers({
            "Content-Type:application/jason",
            "Authorization:key:AAAA4AcxRkM:APA91bFYsc-2mg5cL1-dIYAwRevWiPK2VARvSf0TdoRRA4ugG-JCKwm47nTuY6o4ICgpdBQPIRjLaxnOh6__ykBzaTg1Mba6lLBb9ufZE0ykPOzmLEahnR_g-d3sGObgXY6_-xL3aej6"
    })
    @POST("fcm/send")
    Call <FCMResponse> send(@Body FCMBody body);
}
