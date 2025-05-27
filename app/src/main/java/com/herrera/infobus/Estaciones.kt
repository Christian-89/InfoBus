package com.herrera.infobus

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass.Device
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.ar.core.Point
import com.google.firebase.firestore.GeoPoint
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//asdfasdf
import com.herrera.infobus.DirectionsResponse
import com.herrera.infobus.Route
import okhttp3.OkHttpClient

class Estaciones : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var full_map: GoogleMap

    // Coordenadas fijas para Crear la ruta del autobus hasta Chihualtepec
    val inicio = LatLng(19.21222, -96.96219)
    val fin = LatLng(19.439678820279262, -96.89508834810688)
    //Crear la ruta de Chihualtepec hasta La pradera
    val A = LatLng(19.439678820279262, -96.89508834810688)
    val B = LatLng(19.505030297581, -96.85769981868444)
    //Crear la ruta de La pradera a la Central
    val G = LatLng(19.505030297581, -96.85769981868444)
    val H = LatLng(19.5364, -96.90168)


    private lateinit var btnCalculate: Button

    //Variables para crar ruta por parte del usuario
    private var start: String = ""
    private var end: String = ""
    var poly: Polyline? = null

    //Variables para Crear la ruta del autobus hasta Chihualtepec
    private var C: String = ""
    private var D: String = ""

    //Variables para Crear la ruta del autobus DE Chihualtepec hasta La pradera
    private var E: String = ""
    private var F: String = ""

    //Crear la ruta de La pradera a la Central
    private var I: String = ""
    private var J: String = ""


    var poly2: Polyline? = null



    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estaciones)


        //Cofdigo para el boton para poder mostar la ruta del usuario
        btnCalculate = findViewById(R.id.btnCalculaRute)

        btnCalculate.setOnClickListener {
            start = ""
            end = ""
            poly?.remove()
            poly = null
            Toast.makeText(this, "Selecciona el punto de origen y final", Toast.LENGTH_SHORT).show()
            if (::full_map.isInitialized) {
                full_map.setOnMapClickListener {
                    if (start.isEmpty()) {
                        start = "${it.longitude},${it.latitude}"
                    } else if (end.isEmpty()) {
                        end = "${it.longitude},${it.latitude}"
                        createRoute()
                    }
                }
            }
        }

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.full_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    //Metodo principal
    override fun onMapReady(map: GoogleMap) {
        this.full_map = map

        full_map.uiSettings.isZoomControlsEnabled = true // Habilitar los controles de zoom
        // Limitar el zoom
        full_map.setMinZoomPreference(3.0f) // Nivel mínimo de zoom permitido
        full_map.setMaxZoomPreference(15.0f) // Nivel máximo de zoom permitido

        // Habilitar la ubicación del usuario si es necesario
        enableMyLocation()
        createRoute()

        // Crear la ruta por defecto con las coordenadas fijas
        C = "${inicio.longitude},${inicio.latitude}"
        D = "${fin.longitude},${fin.latitude}"
        //Mnadamos a traer los metodos
        createRoute2()
        //Crear la ruta de Chihualtepec hasta La pradera
        E = "${A.longitude},${A.latitude}"
        F = "${B.longitude},${B.latitude}"
        createRoute3()

        //Crear la ruta de La pradera hasta La central
        I = "${G.longitude},${G.latitude}"
        J = "${H.longitude},${H.latitude}"
        createRoute4()

        //Estaciones
        // Configurar listeners del mapa
        // Configurar listeners del mapa
        full_map.setOnInfoWindowClickListener { marker ->
            Toast.makeText(this, "Clic en el marcador: ${marker.title}", Toast.LENGTH_SHORT).show()
        }

        full_map.setOnMarkerClickListener { marker ->
            val dialog = AlertDialog.Builder(this)
                .setTitle(marker.title)
                .setMessage(marker.snippet)  // Mostrar la descripción (snippet) del marcador
                .setPositiveButton("Aceptar") { dialog, _ -> dialog.dismiss() }
                .create()
            dialog.show()

            true // Indica que el evento fue manejado
        }

        // Agregar marcadores al mapa
        createMarker()
    }


    //Crear la ruta de usuario
    private fun createRoute() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute(
                "5b3ce3597851110001cf62481d945b0923c24b34b245df6e9cb7c0ac",
                start,
                end
            )
            if (call.isSuccessful) {
                drawRute(call.body())
            } else {
                Log.i("Error", "Respuesta no exitosa: ${call.code()}")
            }
        }
    }

    //Crear la ruta hasta hihualtepec
    private fun createRoute2() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute(
                "5b3ce3597851110001cf62481d945b0923c24b34b245df6e9cb7c0ac",
                C,
                D
            )
            if (call.isSuccessful) {
                drawRute2(call.body())
            } else {
                Log.i("Chris", "ko")
            }
        }
    }


    //Crear la ruta de Chihualtepec hasta La pradera
    private fun createRoute3() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute(
                "5b3ce3597851110001cf62481d945b0923c24b34b245df6e9cb7c0ac",
                E,
                F
            )
            if (call.isSuccessful) {
                drawRute2(call.body())
            } else {
                Log.i("Chris", "ko")
            }
        }
    }

    //Crear la ruta de La pradera hasta La central
    private fun createRoute4() {
        CoroutineScope(Dispatchers.IO).launch {
            val call = getRetrofit().create(ApiService::class.java).getRoute(
                "5b3ce3597851110001cf62481d945b0923c24b34b245df6e9cb7c0ac",
                I,
                J
            )
            if (call.isSuccessful) {
                drawRute2(call.body())
            } else {
                Log.i("Chris", "ko")
            }
        }
    }


    //Metodo para mostrar la ruta del autobus
    private fun drawRute(routeResponse: RouterResponse?) {
        val polyLineOptions = PolylineOptions().apply {
            color(Color.CYAN) // Cambiar el color de la línea (puedes usar colores de la clase Color o un código hexadecimal).
            width(10f)       // Establecer el ancho de la línea en píxeles.
            // pattern(listOf(Dot(), Gap(10f))) // Patrón de puntos con un espacio de 10 píxeles.
            geodesic(true)   // Hacer que las líneas sigan una forma geodésica.
        }
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            poly = full_map.addPolyline(polyLineOptions)
        }
    }

    //Metodo para mostrar la ruta del autobus  asdfasdfadsf
    private fun drawRute2(routeResponse: RouterResponse?) {
        val polyLineOptions = PolylineOptions().apply {
            color(Color.BLUE) // Cambiar el color de la línea (puedes usar colores de la clase Color o un código hexadecimal).
            width(10f)       // Establecer el ancho de la línea en píxeles.
            // pattern(listOf(Dot(), Gap(10f))) // Patrón de puntos con un espacio de 10 píxeles.
            geodesic(true)   // Hacer que las líneas sigan una forma geodésica.
        }
        routeResponse?.features?.first()?.geometry?.coordinates?.forEach {
            polyLineOptions.add(LatLng(it[1], it[0]))
        }
        runOnUiThread {
            full_map.addPolyline(polyLineOptions) // Agrega una nueva línea al mapa
        }
    }


    //Creación de los marcadores para las estaciones de los autobuses

    private fun createMarker() {
        // Lista de marcadores con coordenadas, títulos y descripciones
        val markerData = listOf(
            MarkerData(LatLng(19.21222, -96.96219), "Estación Totutla 2", "El nombre de Totutla proviene del náhuatl y su significado es “lugar de aves” o “lugar entre aves”, el cual es muy bien representado por la amplia variedad de fauna en la zona y por algunos bellos murales localizados cerca del palacio municipal y el parque principal."),
            MarkerData(LatLng(19.21048, -96.99378), "Estación de Poxtla", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.25177, -96.98613), "Estación de Ohuapan", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.29761, -96.92388), "Estación de Monte chico", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.31339, -96.90896), "Estación de Tlaltetela", "En el periodo de 1880-1909 personas procedentes de San Miguel Huascaleca, Puebla llegan a lo que hoy es Tlaltetela, cabe mencionar que la localidad de Tlihuayan, por su significado “Suelo Humedo” existía mucho antes.\n" +
                    "Tlaltetela comienza a poblarse en el año de 1919 con las rancherías de Ixtaca y personas que llegan de la congregación de Limones municipio de Cosautlán.\n" +
                    "En el año de 1970 llega el primer servicio de transporte de la línea “Azteca” con los números económicos 21 y 22, cubriendo la ruta Xalapa-Totutla.\n"),
            MarkerData(LatLng(19.39409, -96.8708), "Estación de Tuzamapan", "Se encuentra en la ruta Jalcomulco-Totutla-Coatepec, cerca de la capital veracruzana. \n" +
                    "Limita al norte con Coatepec, al sur con Ayahualco y al oeste con Perote. \n" +
                    "Su nombre proviene de las palabras náhuatl tuzan, que significa topo, y pan, que significa río, haciendo referencia a la corriente del Zempoala que pasa cerca del lugar.\n" +
                    "Tuzamapan, nombre nahuatl proveniente de las radicales \"tuzan\", topo llamado tuza, y \"pan\", río refiriéndose a la corriente del Zempoala que pasa cerca de ese lugar; “Río de las Tuzas”.\n"),
            MarkerData(LatLng(19.448, -96.86594), "Estación de Alborada", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.46063, -96.85748), "Estación de La Estanzuela", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.50512, -96.85792), "Estación de La Pradera", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.50732, -96.86392), "Estación de Las trancas", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.51448, -96.87661), "Estación de Plaza las americas", "Información no disponible por el momento. Estaremos actualizando esta sección próximamente. ¡Gracias por tu comprensión!"),
            MarkerData(LatLng(19.5364, -96.90168), "Central Los Aztecas", "La ciudad es conocida por ser promotora de la cultura y esto se debe en gran parte a la presencia de la Universidad Veracruzana, por lo que es común ver las plazas y calles inundadas de vida estudiantil que dan un aire fresco y llenan de energía el centro.\n" +
                    "Xalapa posee una importante herencia gastronómica, tanto de la cultura prehispánica, como de la española. Uno de los platillos más representativos de la cocina xalapeña son los “chiles xalapeños” rellenos con picadillo de carnero o marisco, servidos con guarnición de zanahoria y cebollas en escabeche.\n")

        )

        // Iterar y agregar marcadores al mapa
        // Iterar y agregar todos los marcadores al mapa
        for (data in markerData) {
            full_map.addMarker(
                MarkerOptions()
                    .position(data.coordinates)
                    .title(data.title)
                    .snippet(data.description)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.autobusicon)) // Usa tu ícono personalizado
            )
        }

    }


    //Link del API que se ocupo para mostrar el mapa
    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openrouteservice.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun isLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (!::full_map.isInitialized) return
        if (isLocationPermissionsGranted()) {
            full_map.isMyLocationEnabled = true
        } else {
            requestLocationPermission()
        }
    }

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    //Metodo para activar la ubicación del usuario
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    //Si no tiene activada la ubicación saldra el siguiente mensaje
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                full_map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Para activar la localización ve a ajustes y acepta los permisos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::full_map.isInitialized) return
        if (!isLocationPermissionsGranted()) {
            full_map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localización ve a ajustes y acepta los permisos",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Botón pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.latitude}, ${p0.longitude}", Toast.LENGTH_SHORT).show()
    }
}
