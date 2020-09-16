package br.com.unicred.associacao.googlemaps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.clustering.ClusterManager
import java.util.*


private val REQUIRED_PERMISSIONS_LOCATION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION
)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapLongClickListener {

    var mMap: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var mClusterManager: ClusterManager<MarkerClusterItem>

    private lateinit var builder: LatLngBounds.Builder
    private lateinit var cu: CameraUpdate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment.getMapAsync(this)

    }

    private fun configBoundLatLng() {
        mMap?.apply {
            clear()

            val teste = getListMarkers2()

            builder = LatLngBounds.Builder()

            teste.forEach { marker: Marker ->
                builder.include(marker.position)
            }

            val padding = 150

            val bounds = builder.build()

            cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)

            setOnMapLoadedCallback {
                animateCamera(cu)
            }
        }
    }


    private fun getPermissionsDeniedList(
        permissions: MutableMap<String, Boolean>
    ): MutableList<String> {
        val permissionDeniedList = mutableListOf<String>()
        permissions.entries.forEach {
            if (!it.value) {
                permissionDeniedList.add(it.key)
            }
        }
        return permissionDeniedList
    }

    private val requestPermissionLocation =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionDeniedList = getPermissionsDeniedList(permissions)

            if (permissionDeniedList.size == 0) {
                mMap?.apply {
//                    enableMyLocationButton()
//                    configureUiSettings()

                    configBoundLatLng()
                }
            } else {
                redirectToPreferencesPermissions()
//                requestPermissionDenied(permissionDeniedList)
            }
        }

    fun redirectToPreferencesPermissions() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun getListMarkers(): List<MarkerClusterItem> {
        var list = mutableListOf<MarkerClusterItem>()
        list.add(MarkerClusterItem(LatLng(51.5145160, -0.1270060), "teste 1"))
        list.add(MarkerClusterItem(LatLng(51.5064490, -0.1244260), "teste 1"))
        list.add(MarkerClusterItem(LatLng(51.5097080, -0.1200450), "teste 1"))
        list.add(MarkerClusterItem(LatLng(51.5090680, -0.1421420), "teste 1"))
        return list
    }

    private fun getListMarkers2(): List<Marker> {
        val list = mutableListOf<Marker>()

        val item1 = mMap?.addMarker(getMarkerOptions(LatLng(51.5145160, -0.1270060), "title 1"))
        val item2 = mMap?.addMarker(getMarkerOptions(LatLng(51.5064490, -0.1244260), "title 2"))
        val item3 = mMap?.addMarker(getMarkerOptions(LatLng(51.6374890, -0.2354860), "title 3"))

        item1?.let { list.add(it) }
        item2?.let { list.add(it) }
        item3?.let { list.add(it) }

        return list
    }

    private fun addClusterItems() {
        for (markerOptions in getListMarkers()) {
            val clusterItem =
                MarkerClusterItem(markerOptions.position, markerOptions.title ?: "")

            mClusterManager.addItem(clusterItem)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocationButton() {
        mMap?.apply {
            isMyLocationEnabled = false
            getDeviceLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.apply {
            mMap = this
//            googleMap.setOnMyLocationButtonClickListener(this@MapsActivity)
//            googleMap.setOnMyLocationClickListener(this@MapsActivity)

            requestPermissionLocation.launch(REQUIRED_PERMISSIONS_LOCATION)
        }
    }

    fun configureUiSettings() {
        mMap?.apply {
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isTiltGesturesEnabled = false
            uiSettings.isMapToolbarEnabled = false
        }

    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {


//            if (locationPermissionGranted) {
            val locationResult = fusedLocationProviderClient.lastLocation
            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result

                    if (lastKnownLocation != null) {

                        val latLng = convertLocationToLatLng(task.result)
                        val unicred = LatLng(51.503186, -0.126446)

//                        mMap?.addPolyline { clickable(true)
//                            .add(
//                                latLng, unicred
//                            )
//                        }

                        mMap?.apply {
                            moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    unicred, DEFAULT_ZOOM.toFloat()
                                )
                            )

//                            private void setUpClusterManager(){
//                                // cluster
//                                clusterManager = new ClusterManager<MarkerClusterItem>(getActivity(), this.googleMap);
//                                clusterManager.setAnimation(false);
//                                MarkerClusterRenderer clusterRenderer = new MarkerClusterRenderer(getActivity(), googleMap, clusterManager);
//                                clusterManager.setRenderer(clusterRenderer);
//                                // marker clic
//                                clusterManager.setOnClusterClickListener(this);
//                            }

//                            configClusterManager()

//                            addClusterItems()

//                            setOnCameraIdleListener(mClusterManager)
//                            setOnMarkerClickListener(mClusterManager)
//
//
//                            addMarker {
//                                position(latLng)
//                                title("Seu endereço")
//                            }
////
//                            addMarker {
//                                position(unicred)
//                                title("Cooperativa Novo Hamburgo")
//                            }

//                            Log.d("israel", distanciaEmMetros(latLng, unicred))
                        }

//                        geocoder()
                    }


                }
//                    else {
//                        mMap?.moveCamera(CameraUpdateFactory
//                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
//                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
//                    }
//                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun GoogleMap.configClusterManager() {
        mClusterManager = ClusterManager(this@MapsActivity, this)
        setOnCameraIdleListener(mClusterManager)
        mClusterManager.markerCollection.setOnInfoWindowClickListener {
            Toast.makeText(this@MapsActivity, "teste", Toast.LENGTH_LONG).show()
        }

        mClusterManager.markerCollection.setOnMarkerClickListener {
            Toast.makeText(
                this@MapsActivity,
                "${it.id} - ${it.position.latitude} - ${it.position.longitude}",
                Toast.LENGTH_LONG
            ).show()
            true
        }
    }

    private fun geocoder() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val teste = geocoder.getFromLocationName("93336150", 10)
        teste.forEach { address ->

            Log.d("israel-latitude", address.latitude.toString())
            Log.d("israel-longitude", address.longitude.toString())
            Log.d("israel-postalCode", address.postalCode.orEmpty())
            Log.d("israel-adminArea", address.adminArea.orEmpty())
            Log.d("israel-featureName", address.featureName.orEmpty())
            Log.d("israel-subAdminArea", address.subAdminArea.orEmpty())

            Log.d("israel-subLocality", address.subLocality.orEmpty())
            Log.d("israel-adressLines", address.getAddressLine(0).orEmpty())
            Log.d("israel-subThoroughfare", address.subThoroughfare.orEmpty())
            Log.d("israel-locality", address.locality.orEmpty())
        }

    }

    private fun distanciaEmMetros(from: LatLng, to: LatLng): String {
        return SphericalUtil.computeDistanceBetween(from, to).toString()
    }

    private fun targetScreenWhenLoad(latLng: LatLng): CameraUpdate? {
        return CameraUpdateFactory.newLatLng(latLng)
    }

    private fun getMarkerOptions(latLng: LatLng, title: String): MarkerOptions? {
        return MarkerOptions().position(latLng).title(title)
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }

    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG).show()
    }

    override fun onMapLongClick(p0: LatLng?) {
        Toast.makeText(this, "teste", Toast.LENGTH_LONG).show()
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 13
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    /*
    val googleMap = // ...
    val sydney = LatLng(-33.852, 151.211)
    val marker = googleMap.addMarker {
        position(sydney)
        title("Marker in Sydney")
    }
    */

    private fun convertLocationToLatLng(location: Location?): LatLng {
        return LatLng(location?.latitude!!, location.longitude)
    }
//
//    override fun onClusterClick(cluster: Cluster<MarkerClusterItem>?): Boolean {
//
//        return true
//    }
//
//    override fun onClusterInfoWindowClick(cluster: Cluster<MarkerClusterItem>?) {
//        Log.d("israel", "onClusterInfoWindowClick")
//    }
//
//    override fun onClusterItemClick(item: MarkerClusterItem?): Boolean {
//        return false
//    }
//
//    override fun onClusterItemInfoWindowClick(item: MarkerClusterItem?) {
//        Log.d("israel", "onClusterItemInfoWindowClick")
//    }

}
