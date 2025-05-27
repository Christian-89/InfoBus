package com.herrera.infobus

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/v2/directions/driving-car")
    //Creamos el metodo para lo que va a pedir la API, importante poner el mismo nombre que aparece
    //en la API
    suspend fun getRoute(
        @Query("api_key") key: String,
        @Query("start", encoded = true) start: String,
        @Query("end", encoded = true) end: String
    ):Response<RouterResponse>

}