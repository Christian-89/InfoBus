package com.herrera.infobus


import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ApiService2 {
    @GET("route/v1/driving/{startLon},{startLat};{endLon},{endLat}")
    suspend fun getRoute(
        @Path("startLon") startLon: String,
        @Path("startLat") startLat: String,
        @Path("endLon") endLon: String,
        @Path("endLat") endLat: String,
        @Query("overview") overview: String = "full",
        @Query("steps") steps: String = "true"
    ): Response<DirectionsResponse>
}