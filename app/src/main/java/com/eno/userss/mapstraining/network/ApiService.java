package com.eno.userss.mapstraining.network;


import com.eno.userss.mapstraining.model.ResponseWayPoint;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by FUADMASKA on 6/16/2017.
 */

public interface ApiService {

        @GET("api/directions/json")
        Call<ResponseWayPoint> request_route(
                @Query("origin") String origin,
                @Query("destination") String tujuan,
                @Query("key") String key);




}
