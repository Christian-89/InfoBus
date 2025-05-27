package com.herrera.infobus


import com.google.gson.annotations.SerializedName


data class RouterResponse (@SerializedName ("features")val features:List<Feature>)
data class Feature(@SerializedName("geometry") val geometry:Geometry)
data class Geometry(@SerializedName("coordinates") val coordinates:List<List<Double>>)