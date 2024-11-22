package com.cs407.locspend

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var addressText: TextView
    private lateinit var client: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize view
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Assign variable
        addressText = view.findViewById(R.id.addr)

        // Initialize location client
        client = LocationServices
            .getFusedLocationProviderClient(
                requireActivity()
            )

        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location: Location ->
            updateLocationInfo(location)
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If no permission, request permission
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // With permission, start listening to location
            startListening(locationListener)
        }

        // Return view
        return view
    }

    // Request location permission
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                startListening(locationListener)
            }
        }

    private fun startListening(locationListener: LocationListener) {
        // get location from the system
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L, 10f, locationListener
            )
        }
    }

    /*
     * Updates the address text at the top of the screen using current location.
     * Adds a marker on the map at the current location.
     */
    private fun updateLocationInfo(location: Location) {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap: GoogleMap ->
            setLocationMarker(googleMap, LatLng(location.latitude, location.longitude))
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            geocoder.getFromLocation(
                location.latitude,
                location.longitude, 1
            ) { addresses ->
                if (addresses.isNotEmpty()) {
                    val address = addresses[0]
                    var addressString = ""
                    address.subThoroughfare?.let { addressString += "$it " }
                    address.thoroughfare?.let { addressString += "$it\n" }
                    address.locality?.let { addressString += it }
                    address.adminArea?.let { addressString += ", $it " }
                    address.postalCode?.let { addressString += it }
                    addressText.text = addressString
                }
            }
        }
    }

    /**
     * Adds marker representations of the places list on the provided GoogleMap object
     */
    private fun setLocationMarker(googleMap:GoogleMap, destination:LatLng) {
        // clear previous marker
        googleMap.clear()
        // add marker for current location
        googleMap.addMarker(
            MarkerOptions()
                .position(destination)
        )
        // zoom camera to the current location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(destination, 15f))
        // allow zooming on map
        googleMap.uiSettings.isZoomGesturesEnabled = true
    }
}
