/*
 * Copyright (c) 2020 Razeware LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 * distribute, sublicense, create a derivative work, and/or sell copies of the
 * Software in any work that is designed, intended, or marketed for pedagogical or
 * instructional purposes related to programming, coding, application development,
 * or information technology.  Permission for such use, copying, modification,
 * merger, publication, distribution, sublicensing, creation of derivative works,
 * or sale is expressly withheld.
 *
 * This project and source code may use libraries or frameworks that are
 * released under various Open-Source licenses. Use of those libraries and
 * frameworks are governed by their own individual licenses.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.realworld.android.petsave.report.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.realworld.android.petsave.databinding.FragmentReportDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.RandomAccessFile
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@AndroidEntryPoint
class ReportDetailFragment : Fragment() {

  companion object {
    private const val API_URL = "https://example.com/?send_report"
    private const val PIC_FROM_GALLERY = 2
    private const val REPORT_APP_ID = 46341L
    private const val REPORT_PROVIDER_ID = 46341L
    private const val REPORT_SESSION_KEY = "session_key_in_next_chapter"
  }

  object ReportTracker {
    var reportNumber = AtomicInteger()
  }

  @Volatile
  private var isSendingReport = false

  private val binding get() = _binding!!
  private var _binding: FragmentReportDetailBinding? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    _binding = FragmentReportDetailBinding.inflate(inflater, container, false)

    binding.sendButton.setOnClickListener {
      sendReportPressed()
    }

    binding.uploadPhotoButton.setOnClickListener {
      uploadPhotoPressed()
    }

    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    setupUI()
  }

  private fun setupUI() {
    binding.detailsEdtxtview.imeOptions = EditorInfo.IME_ACTION_DONE
    binding.detailsEdtxtview.setRawInputType(InputType.TYPE_CLASS_TEXT)
  }

  private fun sendReportPressed() {
    if (!isSendingReport) {
      isSendingReport = true

      //1. Save report
      var reportString = binding.categoryEdtxtview.text.toString()
      reportString += " : "
      reportString += binding.detailsEdtxtview.text.toString()
      val reportID = UUID.randomUUID().toString()

      context?.let { theContext ->
        //TODO: Replace below for encrypting the file
        val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
        file.bufferedWriter().use {
          it.write(reportString)
        }
      }

      //TODO: Test your custom encryption here
      //testCustomEncryption(reportString)

      ReportTracker.reportNumber.incrementAndGet()

      //2. Send report
      val postParameters = mapOf("application_id" to REPORT_APP_ID * REPORT_PROVIDER_ID,
          "report_id" to reportID,
          "report" to reportString)
      if (postParameters.isNotEmpty()) {
        //send report
        val connection = URL(API_URL).openConnection() as HttpURLConnection
        //...
      }

      isSendingReport = false
      context?.let {
        val report = "Report: ${ReportTracker.reportNumber.get()}"
        val toast = Toast.makeText(it, "Thank you for your report.$report", Toast
            .LENGTH_LONG)
        toast.show()
      }

      val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as
      InputMethodManager
      inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
    }
  }

  private fun testCustomEncryption(reportString: String) {
    
  }

  private fun uploadPhotoPressed() {
    context?.let {
      if (ContextCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE)
          != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE), PIC_FROM_GALLERY)
      } else {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PIC_FROM_GALLERY)
      }
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int,
                                          permissions: Array<String>, grantResults: IntArray) {
    when (requestCode) {
      PIC_FROM_GALLERY -> {
        // If request is cancelled, the result arrays are empty.
        if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
          // Permission was granted
          val galleryIntent = Intent(Intent.ACTION_PICK,
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
          startActivityForResult(galleryIntent, PIC_FROM_GALLERY)
        }
        return
      }
      else -> {
        // Ignore all other requests.
      }
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    when (requestCode) {

      PIC_FROM_GALLERY ->

        if (resultCode == Activity.RESULT_OK) {

          //image from gallery
          val selectedImage = data?.data
          selectedImage?.let {
            getFilename(selectedImage)
          }
        }
      else -> println("Didn't select picture option")
    }
  }

  private fun getFilename(selectedImage: Uri) {
    // Validate image
    val isValid = isValidJPEGAtPath(selectedImage)
    if (isValid) {
      //get filename
      val fileNameColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
      val nameCursor = activity?.contentResolver?.query(selectedImage, fileNameColumn,
          null, null, null)
      nameCursor?.moveToFirst()
      val nameIndex = nameCursor?.getColumnIndex(fileNameColumn[0])
      var filename = ""
      nameIndex?.let {
        filename = nameCursor.getString(it)
      }
      nameCursor?.close()

      //update UI with filename
      binding.uploadStatusTextview?.text = filename
    } else {
      val toast = Toast.makeText(context, "Please choose a JPEG image", Toast.LENGTH_LONG)
      toast.show()
    }
  }

  private fun isValidJPEGAtPath(selectedImage: Uri): Boolean {
    var success = false
    val file = File(context?.cacheDir, "temp.jpg")
    val inputStream = activity?.contentResolver?.openInputStream(selectedImage)
    val outputStream = activity?.contentResolver?.openOutputStream(Uri.fromFile(file))
    outputStream?.let {
      inputStream?.copyTo(it)

      val randomAccessFile = RandomAccessFile(file, "r")
      val length = randomAccessFile.length()
      val lengthError = (length < 10L)
      val start = ByteArray(2)
      randomAccessFile.readFully(start)
      randomAccessFile.seek(length - 2)
      val end = ByteArray(2)
      randomAccessFile.readFully(end)
      success = !lengthError && start[0].toInt() == -1 && start[1].toInt() == -40 &&
          end[0].toInt() == -1 && end[1].toInt() == -39

      randomAccessFile.close()
      outputStream.close()
    }
    inputStream?.close()
    file.delete()

    return success
  }
}
