package com.raywenderlich.android.petsave.common

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.hilt.android.testing.CustomTestApplication

open class TestApplication: Application() {
  override fun onCreate() {
    super.onCreate()
    AndroidThreeTen.init(this)
  }
}

@CustomTestApplication(TestApplication::class)
interface InstrumentedTestApplication