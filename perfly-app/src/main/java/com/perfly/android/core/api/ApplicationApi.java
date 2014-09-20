package com.perfly.android.core.api;

import com.perfly.android.core.sys.Application;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;

public interface ApplicationApi {

    @POST("/api/app")
    void postApplication(@Body Application app, Callback<Response> cb);

}
