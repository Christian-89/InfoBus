package com.herrera.infobus

import com.google.android.gms.maps.model.LatLng


data class MarkerData (
    val coordinates: LatLng,
    val title: String,
    val description: String
)