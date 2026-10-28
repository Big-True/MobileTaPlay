package com.mobiletaplay.app

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration

/**
 * Shared orientation so Home / Detail / Game stay in sync.
 *
 * Handheld quirk: some devices report a portrait physical panel (e.g. 1080x1920)
 * while the logical display is landscape (e.g. 1920x1080). Free SENSOR can flip
 * the window to portrait and make games render a tall column on a wide screen.
 */
object OrientationPrefs {
    var orientation: Int = ActivityInfo.SCREEN_ORIENTATION_SENSOR

    fun apply(activity: Activity) {
        activity.requestedOrientation = resolve(activity)
    }

    /** Pin the window to the aspect the display is actually using right now. */
    fun resolve(activity: Activity): Int {
        val landscape =
            activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        return when (orientation) {
            ActivityInfo.SCREEN_ORIENTATION_SENSOR ->
                if (landscape) ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> orientation
        }
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
