package com.raywenderlich.android.petsave.common.data.api.utils

import androidx.test.platform.app.InstrumentationRegistry
import com.raywenderlich.android.logging.Logger
import java.io.IOException
import java.io.InputStream

object JsonReader {
  fun getJson(path: String): String {
    return try {
      val context = InstrumentationRegistry.getInstrumentation().context
      val jsonStream: InputStream = context.assets.open("networkresponses/$path")
      String(jsonStream.readBytes())
    } catch (exception: IOException) {
      Logger.e(exception, "Error reading network response json asset")
      throw exception
    }
  }
}