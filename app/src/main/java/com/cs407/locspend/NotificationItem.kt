package com.cs407.locspend

import android.provider.Settings.Global.getString
import androidx.core.content.ContentProviderCompat.requireContext

class NotificationItem(
    private val locationCategory: String?,
    private val id : Int = -1
) {
    public fun getLocationCategory() : String? {
        return locationCategory
    }

    public fun getContent(): String? {
        return "Check your Budget?"
    }

    public fun getId() : Int {
        return id
    }
}