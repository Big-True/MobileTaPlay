package com.mobiletaplay.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration

/**
 * Shared orientation so Home / Detail / Game stay in sync.
 */
object OrientationPrefs {
    var orientation: Int = ActivityInfo.SCREEN_ORIENTATION_SENSOR

    fun apply(activity: Activity) {
        activity.requestedOrientation = orientation
    }

    fun toggle(activity: Activity) {
        val landscape =
            activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        orientation = if (landscape) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        } else {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        }
        activity.requestedOrientation = orientation
    }
}
