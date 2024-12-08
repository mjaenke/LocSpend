package com.cs407.locspend

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
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
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.cs407.locspend.data.BudgetDatabase
import kotlinx.coroutines.launch
import java.util.Calendar


class HomeFragment (
    private val injectedUserViewModel: UserViewModel? = null
): Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels() // access view model

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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var placesClient: PlacesClient
    private lateinit var budgetDB: BudgetDatabase
    private lateinit var userViewModel: UserViewModel

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
        budgetDB = BudgetDatabase.getDatabase(requireContext())
        userViewModel = injectedUserViewModel ?: ViewModelProvider(requireActivity()).get(UserViewModel::class.java)


        // Set HomeFragment text views to correct values from the view model
        addressText.text = homeViewModel.address
        categoryText.text = getString(R.string.category, homeViewModel.category)
        budgetText.text = getString(R.string.diningBudget, homeViewModel.category, homeViewModel.budget)
        spentText.text = getString(R.string.spent, homeViewModel.spent)
        remainingText.text = getString(R.string.remaining, homeViewModel.budget - homeViewModel.spent)
        summaryText.text = getString(R.string.summary, homeViewModel.percentBudget, homeViewModel.percentMonth)

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
            if (isAdded) {
                updateLocationInfo(location)
            }
            Log.e("HomeFragment", "fragment not attached, skipping location update")
        }

        // Check for location permission
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // If no permission, request permission
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // With permission, start listening to location
            startListening(locationListener)
        }


        // Check for notification permission
        requestPermission()


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
        if (isAdded) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.map_fragment) as? SupportMapFragment
            mapFragment?.getMapAsync { googleMap: GoogleMap ->
                setLocationMarker(googleMap, LatLng(location.latitude, location.longitude))
                getPlacesInfo()
            }

            // create and show notification for the loc update
            NotificationHelper.getInstance().createNotificationChannel(requireContext())
            NotificationHelper.getInstance().appendNotificationItem(
                categoryText.text.toString()
            )
            NotificationHelper.getInstance().showNotification(requireContext(), -1)

        } else {
            Log.e("HomeFragment", "Fragment not added yet, skipping location update")
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

    // if the notifications are enabled, then set shared prefs
    private val requestNotifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        setNotificationsEnabled(this.context, true)
    }

    //requests notification permission from the user
    @VisibleForTesting
    public fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        if (ContextCompat.checkSelfPermission(
                requireContext(), android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestNotifPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    //sets shared preferences to true if the notifs are enabled
    public fun setNotificationsEnabled(context: Context?, enabled: Boolean) {
        if (context != null) {
            sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putBoolean("notifications_enabled", true).apply()
        } else {
            Toast.makeText(requireContext(), "Context was null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getPlacesInfo() {
        val placeFields: List<Place.Field> = listOf(Place.Field.NAME, Place.Field.TYPES, Place.Field.ADDRESS)
        val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)
        if (ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) {
            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    if (response.placeLikelihoods.isEmpty()) {
                        homeViewModel.address = getString(R.string.addrError)
                        homeViewModel.category = "Miscellaneous"
                        updateBudgetInfo()
                    } else {
                        val place = response.placeLikelihoods.first().place

                        if (place.address != null) {
                            homeViewModel.address = place.address
                            if (place.name != null) {
                                homeViewModel.address = buildString {
                                    append("${place.name}\n${homeViewModel.address}")
                                }
                            }
                        } else {
                            homeViewModel.address = getString(R.string.addrError)
                        }

                        if (place.placeTypes != null && place.placeTypes.size > 0) {
                            homeViewModel.category = getCategory(place.placeTypes)
                        } else {
                            homeViewModel.category = "Miscellaneous"
                        }
                        updateBudgetInfo()
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

    private fun updateBudgetInfo() {
        // Set viewmodel values to correct values
        lifecycleScope.launch {
            homeViewModel.budget = getTotalBudget()
            homeViewModel.spent = getTotalSpent()
            homeViewModel.percentMonth = getPercentageMonth()
            homeViewModel.remaining = homeViewModel.budget - homeViewModel.spent
            homeViewModel.percentBudget = ((homeViewModel.spent / homeViewModel.budget) * 100f).toInt()


            // Set HomeFragment text views to correct budget values
            addressText.text = homeViewModel.address
            categoryText.text = getString(R.string.category, homeViewModel.category)
            budgetText.text =
                getString(R.string.diningBudget, homeViewModel.category, homeViewModel.budget)
            spentText.text = getString(R.string.spent, homeViewModel.spent)
            remainingText.text =
                getString(R.string.remaining, homeViewModel.budget - homeViewModel.spent)
            summaryText.text =
                getString(R.string.summary, homeViewModel.percentBudget, homeViewModel.percentMonth)
        }
    }

    private suspend fun getTotalBudget(): Double {
        val userState = userViewModel.userState.value
        var totalBudget = 0.0
        val budgets = budgetDB.deleteDao().getAllBudgetIdsByUser(userState.id)
        for (budget in budgets) {
            val current_budget = budgetDB.budgetDao().getById(budget)
            totalBudget += current_budget.budgetAmount
        }
        return totalBudget
    }

    private suspend fun getTotalSpent() : Double {
        val userState = userViewModel.userState.value
        var totalSpent = 0.0
        val budgets = budgetDB.deleteDao().getAllBudgetIdsByUser(userState.id)
        for (budget in budgets) {
            val current_budget = budgetDB.budgetDao().getById(budget)
            totalSpent += current_budget.budgetSpent
        }
        return totalSpent
    }

    private fun getPercentageMonth() : Int {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val totalDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val monthPercentage = ((day / totalDays.toDouble()) * 100).toInt()
        return monthPercentage
    }


}
