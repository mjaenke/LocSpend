package com.cs407.locspend

import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.util.Locale


class HomeFragment : Fragment() {

    private lateinit var addressText: TextView
    private lateinit var categoryText: TextView
    private lateinit var budgetText: TextView
    private lateinit var spentText: TextView
    private lateinit var remainingText: TextView
    private lateinit var summaryText: TextView

    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var homeAddButton : Button
    private lateinit var placesClient: PlacesClient

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Initialize view
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Assign variable
        addressText = view.findViewById(R.id.addr)
        categoryText = view.findViewById(R.id.home_category)
        budgetText = view.findViewById(R.id.home_budget)
        spentText = view.findViewById(R.id.home_spent)
        remainingText = view.findViewById(R.id.home_remaining)
        summaryText = view.findViewById(R.id.home_summary)

        // Initialize location client
        locationClient = LocationServices
            .getFusedLocationProviderClient(
                requireActivity()
            )

        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(requireContext(), apiKey)

        // Initialize places client
        placesClient = Places.createClient(requireContext())

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        homeAddButton = view.findViewById(R.id.button)

        homeAddButton.setOnClickListener {
            Navigation.findNavController(view)
                .navigate(R.id.action_homeFragment_to_addSpendingFragment)

        }
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
            getPlacesInfo()
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

    private fun getPlacesInfo() {
        var category: String? = null
        var name: String? = null
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result

                    val place = response.placeLikelihoods.first().place

                    if (place.name != null) {
                        name = place.name
                    }

                    if (place.primaryType != null) {
                        Log.i("TAG", "primary type ${place.primaryType}")
                        category = place.primaryType
                    } else {
                        category = "DEFAULT"
                    }

                    if (name != null) {
                        addressText.text = buildString {
                            append("$name\n${addressText.text}")
                        }
                    }
                    if (category != null) {
                        updateBudgetInfo(getCategory(category!!))
                    }


                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("TAG", "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            requestPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getCategory(category: String) : String {
        // TODO: Connect category to our categories
        return "DEFAULT"
    }

    private fun updateBudgetInfo(category: String) {
        // TODO: Replace numbers with actual values from database
        categoryText.text = getString(R.string.category, category)
        budgetText.text = getString(R.string.diningBudget, category, 0)
        spentText.text = getString(R.string.spent, 0)
        remainingText.text = getString(R.string.remaining, 0)
        summaryText.text = getString(R.string.summary, 0, 0)

    }
}
