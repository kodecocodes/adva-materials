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

package com.raywenderlich.android.petsave.report.presentation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.raywenderlich.android.petsave.core.MainActivity
import com.raywenderlich.android.petsave.core.data.api.ReportManager
import com.raywenderlich.android.petsave.core.utils.Encryption
import com.raywenderlich.android.petsave.core.utils.Encryption.Companion.encryptFile
import com.raywenderlich.android.petsave.databinding.FragmentReportDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_report_detail.*
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@AndroidEntryPoint
class ReportDetailFragment : Fragment() {

  companion object {
    private const val PIC_FROM_GALLERY = 2
    private const val REPORT_APP_ID = 46341
    private const val REPORT_SESSION_KEY = "session_key_test"
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
    details_edtxtview.imeOptions = EditorInfo.IME_ACTION_DONE
    details_edtxtview.setRawInputType(InputType.TYPE_CLASS_TEXT)
  }

  private fun sendReportPressed() {
    if (!isSendingReport) {
      isSendingReport = true
      var success = false

      //1. Save report
      var reportString = category_edtxtview.text.toString()
      reportString += " : "
      reportString += details_edtxtview.text.toString()
      val reportID = UUID.randomUUID().toString()

      context?.let { theContext ->
        val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
        val encryptedFile = encryptFile(theContext, file)
        encryptedFile.openFileOutput().bufferedWriter().use {
          it.write(reportString)
        }
      }

      ReportTracker.reportNumber.incrementAndGet()

      //2. Send report
      val mainActivity = activity as MainActivity
      var requestSignature = ""
      //TODO: Add Signature here
      val stringToSign = "$REPORT_APP_ID+$reportID+$reportString"
      val bytesToSign = stringToSign.toByteArray(Charsets.UTF_8)
      val signedData = mainActivity.clientAuthenticator.sign(bytesToSign)
      requestSignature = Base64.encodeToString(signedData, Base64.NO_WRAP)
      val postParameters = mapOf("application_id" to REPORT_APP_ID,
          "report_id" to reportID,
          "report" to reportString,
          "signature" to requestSignature)
      if (postParameters.isNotEmpty()) {
        //send report
        mainActivity.reportManager.sendReport(postParameters) {
          val reportSent: Boolean = it["success"] as Boolean
          if (reportSent) {
            //TODO: Verify signature here
            success = true
          } //end if (reportSent) {
          onReportReceived(success)
        } //mainActivity.reportManager.sendReport(postParameters) {
      } //end if (postParameters.isNotEmpty()) {
    }
  }

  private fun onReportReceived(success: Boolean) {
    isSendingReport = false
    if (success) {
      context?.let {
        val report = "Report: ${ReportTracker.reportNumber.get()}"
        val toast = Toast.makeText(context, "Thank you for your report.$report", Toast
            .LENGTH_LONG)
        toast.show()
      }
    }
    else {
      val toast = Toast.makeText(context, "There was a problem sending the report.", Toast
          .LENGTH_LONG)
      toast.setGravity(Gravity.TOP, 0, 0)
      toast.show()
    }
    val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as
        InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view?.windowToken, 0)
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
            showFilename(selectedImage)
          }
        }
      else -> println("Didn't select picture option")
    }
  }

  private fun showFilename(selectedImage: Uri) {
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
    upload_status_textview?.text = filename
  }

}
