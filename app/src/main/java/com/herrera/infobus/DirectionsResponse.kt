package com.herrera.infobus

data class DirectionsResponse(
    val routes: List<Route>
)

data class Route(
    val geometry: Geometry2
)

data class Geometry2(
    val coordinates: List<List<Double>>,  // Lista de coordenadas (longitud, latitud)
    val type: String                       // Tipo de geometría
)