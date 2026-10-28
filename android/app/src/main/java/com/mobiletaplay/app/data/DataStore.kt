package com.mobiletaplay.app.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.mtpDataStore by preferencesDataStore(name = "mtp_settings")
