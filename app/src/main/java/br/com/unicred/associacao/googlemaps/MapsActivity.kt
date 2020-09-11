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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.ktx.addMarker
import java.util.*

private val REQUIRED_PERMISSIONS_LOCATION = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION
)

class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    OnMyLocationButtonClickListener, OnMyLocationClickListener, OnMapLongClickListener {

    var mMap: GoogleMap? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        mapFragment.getMapAsync(this)


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
                    enableMuyLocationButton()
                    teste()
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

    @SuppressLint("MissingPermission")
    private fun enableMuyLocationButton() {
        mMap?.apply {
            isMyLocationEnabled = true
            getDeviceLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {

        googleMap?.apply {
            mMap = this
            googleMap.setOnMyLocationButtonClickListener(this@MapsActivity)
            googleMap.setOnMyLocationClickListener(this@MapsActivity)

            requestPermissionLocation.launch(REQUIRED_PERMISSIONS_LOCATION)
        }
    }

    fun teste() {
        mMap?.apply {
//            val cooperativa = LatLng(-29.690641, -51.135547)
//            val usuario = LatLng(-29.684117, -51.126942)
//
//            addMarker(
//                getMarkerOptions(cooperativa, "Cooperativa")
//            )

//            addMarker(
//                getMarkerOptions(usuario, "Seu local agora")
//            )

//            moveCamera(targetScreenWhenLoad(cooperativa))

            uiSettings.isMyLocationButtonEnabled = true
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
                        val unicred = LatLng(-29.684117, -51.126942)

                        mMap?.apply {
                            moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    latLng, DEFAULT_ZOOM.toFloat()
                                )
                            )

                            addMarker {
                                position(latLng)
                                title("Meu Local")
                            }

                            addMarker {
                                position(unicred)
                                title("Cooperativa Novo Hamburgo")
                            }

                            Log.d("israel", distanciaEmMetros(latLng, unicred))
                        }

                        geocoder()
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

    private fun geocoder() {
        val geocoder = Geocoder(this, Locale.getDefault())
        val teste = geocoder.getFromLocationName("93336150", 10)
        teste.forEach {address ->

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
        TODO("Not yet implemented")
    }

    companion object {
        private val TAG = MapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
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

}
