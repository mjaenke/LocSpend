package com.cs407.locspend

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.cs407.locspend.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.activityViewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var placesClient: PlacesClient
    private lateinit var address: String
    private lateinit var category: String

    override fun onCreate(savedInstanceState: Bundle?) {

        // Get the shared preferences
        sharedPreferences = getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)

        // Apply the theme
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkModeEnabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
        val navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> binding.bottomNavigationView.visibility = View.GONE
                R.id.createAccountFragment -> binding.bottomNavigationView.visibility = View.GONE
                else -> binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.homeMenu -> navController.navigate(R.id.homeFragment)
                R.id.budgetMenu -> navController.navigate(R.id.budgetFragment)
                R.id.settingsMenu -> navController.navigate(R.id.settingsFragment)

                else -> {}
            }
            true
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        val apiKey = BuildConfig.PLACES_API_KEY
        if (apiKey.isEmpty()) {
            Log.e("Places test", "No api key")
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        // Initialize places client
        placesClient = Places.createClient(applicationContext)
        address = ""
        category = ""

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = LocationListener { location: Location ->
            getPlacesInfo()
        }

        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),1)
        } else {
            startListening(locationListener)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0L,0f,locationListener)
            val location : Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                updateLocationInfo(location)
            }
        }


    }

    private fun updateLocationInfo(location: Location) {
        val pastCategory = category
        getPlacesInfo()
        //only show a notification if the category has changed

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array <out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode==1){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startListening(locationListener)
            }
        }
    }

    private fun startListening(locationListener: LocationListener){
        //get location from the system
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            ==PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L, 0f, locationListener
            )
        }
    }


    private fun getPlacesInfo() {
        val pastCategory = category
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.TYPES, Place.Field.ADDRESS)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    if (response.placeLikelihoods.isEmpty()) {
                        address = getString(R.string.addrError)
                        category = "Miscellaneous"
                    } else {
                        val place = response.placeLikelihoods.first().place

                        if (place.address != null) {
                            address = place.address
                            if (place.name != null) {
                                address = buildString {
                                    append("${place.name}\n${address}")
                                }
                            }
                        } else {
                           address = getString(R.string.addrError)
                        }

                        if (place.placeTypes != null && place.placeTypes.size > 0) {
                            category = getCategory(place.placeTypes)
                        } else {
                            category = "Miscellaneous"
                        }

                        if (pastCategory!=category){
                            // create and show notification for the loc update
                            Log.e("Notification", category)
                            NotificationHelper.getInstance().createNotificationChannel(applicationContext)
                            NotificationHelper.getInstance().appendNotificationItem(
                                category
                            )
                            NotificationHelper.getInstance().showNotification(applicationContext, -1)
                        }

                    }
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("TAG", "Place not found: ${exception.statusCode}")
                    }
                }
            }
        } else {
            // do nothing
        }
    }
    private fun getCategory(placeTypes: List<String>) : String {
        // For each of our categories, set list of corresponding placeTypes
        val dining = listOf("restaurant", "food", "cafe", "coffee_shop", "pub", "diner", "ice_cream_shop", "diner", "deli")
        val grocery = listOf("grocery_or_supermarket", "supermarket", "liquor_store", "grocery_store", "convenience_store")
        val clothing = listOf("clothing_store", "shoe_store")
        val transportation = listOf("transit_station", "airport", "bus_station", "subway_station", "car_dealer", "car_rental", "car_repair", "car_wash", "gas_station", "parking")
        val entertainment = listOf("movie_theater", "night_club", "art_gallery", "museum", "amusement_park", "bowling_alley", "concert_hall", "zoo")

        // Check each placeType to see if it matches any of our categories
        for (place in placeTypes) {
            if (dining.contains(place)) {
                return "Dining"
            } else if (grocery.contains(place)) {
                return "Grocery"
            } else if (clothing.contains(place)) {
                return "Clothing"
            } else if (transportation.contains(place)) {
                return "Transportation"
            } else if (entertainment.contains(place)) {
                return "Entertainment"
            }
        }

        // If none of the place types match our categories, return miscellaneous
        return "Miscellaneous"
    }
}