package com.cs407.locspend

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var logoutButton: Button
    private lateinit var notificationSwitch : Switch
    private lateinit var userViewModel: UserViewModel

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        sharedPreferences = requireActivity().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)
        logoutButton = view.findViewById(R.id.test_logout)
        notificationSwitch = view.findViewById(R.id.notificationSwitch)

        // Initialize dark mode switch
        val darkModeSwitch: Switch = view.findViewById(R.id.darkModeSwitch)

        // Set the switch state based on saved preferences
        val isDarkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkModeEnabled

        // Listener for the switch
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save the preference
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()

            // Apply the theme
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        val notificationSwitch : Switch = view.findViewById(R.id.notificationSwitch)
        val areNotificationsOn = sharedPreferences.getBoolean("notifications_enabled", false)
        notificationSwitch.isChecked = areNotificationsOn

        val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                //permission is granted, save to shared preferences
                sharedPreferences.edit().putBoolean("notifications_enabled", true).apply()
                Toast.makeText(requireContext(), "Notifications are enabled", Toast.LENGTH_SHORT).show()
            } else {
                notificationSwitch.isChecked = false
                Toast.makeText(
                    requireContext(),
                    "Permission denied. Notifications are disabled.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            //turn notifications on if they arent already
            if (isChecked) {
                if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                    // If permission is not granted, request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    sharedPreferences.edit().putBoolean("notifications_enabled", true).apply()
                    Toast.makeText(requireContext(), "Notifications are enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                sharedPreferences.edit().putBoolean("notifications_enabled", false).apply()
                Toast.makeText(requireContext(), "Notifications are disabled", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logoutButton.setOnClickListener {
            // navigate to another fragment after successful logout
//            userViewModel.setUser(UserState())
            lifecycleScope.launch {
                Navigation.findNavController(view)
                    .navigate(R.id.action_settingsFragment_to_loginFragment)
            }
        }


    }



    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}