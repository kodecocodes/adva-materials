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

package com.raywenderlich.android.petsave.core

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.animalsnearyou.presentation.AnimalsNearYouFragmentViewModel
import com.raywenderlich.android.petsave.core.data.preferences.PetSavePreferences
import com.raywenderlich.android.petsave.core.data.preferences.Preferences
import com.raywenderlich.android.petsave.core.domain.model.user.User
import com.raywenderlich.android.petsave.core.domain.repositories.UserRepository
import com.raywenderlich.android.petsave.core.utils.FileConstants
import com.raywenderlich.android.petsave.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.ObjectInputStream
import kotlinx.android.synthetic.main.activity_main.bottom_navigation
import kotlinx.android.synthetic.main.activity_main.login_button
import kotlinx.android.synthetic.main.activity_main.login_email
import kotlinx.android.synthetic.main.activity_main.nav_host_fragment

/**
 * Main Screen
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
  private val binding get() = _binding!!
  private var _binding: ActivityMainBinding? = null

  private val navController by lazy { findNavController(R.id.nav_host_fragment) }
  private val appBarConfiguration by lazy { AppBarConfiguration(topLevelDestinationIds = setOf(
      R.id.animalsNearYou, R.id.search, R.id.report)) }

  private val viewModel: AnimalsNearYouFragmentViewModel by viewModels()
  private var isSignedUp = false
  private var workingFile: File? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)
    window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

    _binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupFragment()
    setupActionBar()
    setupBottomNav()
    setupWorkingFiles()
    updateLoggedInState()
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }

  private fun setupFragment() {
    val fragmentManager = supportFragmentManager
    fragmentManager.beginTransaction()
        .hide(nav_host_fragment)
        .commit()
  }

  private fun setupActionBar() {
    setSupportActionBar(binding.toolbar)
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  private fun setupBottomNav() {
    bottom_navigation.visibility = View.GONE
    binding.bottomNavigation.setupWithNavController(navController)
  }

  private fun setupWorkingFiles() {
    workingFile = File(filesDir.absolutePath + File.separator +
        FileConstants.DATA_SOURCE_FILE_NAME)
  }

  fun loginPressed(view: View) {
    displayLogin(view, false)
  }

  private fun updateLoggedInState() {
    val fileExists = workingFile?.exists() ?: false
    if (fileExists) {
      isSignedUp = true
      login_button.text = getString(R.string.login)
      login_email.visibility = View.INVISIBLE
    } else {
      login_button.text = getString(R.string.signup)
    }
  }

  private fun displayLogin(view: View, fallback: Boolean) {
    //TODO: Replace below
    performLoginOperation(view)
  }

  private fun performLoginOperation(view: View) {
    var success = false
    val preferences: Preferences = PetSavePreferences(this)

    workingFile?.let {
      //Check if already signed up
      if (isSignedUp) {
        val fileInputStream = FileInputStream(it)
        val objectInputStream = ObjectInputStream(fileInputStream)
        val list = objectInputStream.readObject() as ArrayList<User>
        val firstUser = list.first() as? User
        if (firstUser is User) { //2
          //TODO: Replace below with implementation that decrypts password
          success = true
        }

        if (success) {
          toast("Last login: ${preferences.getLastLoggedIn()}")
        } else {
          toast("Please check your credentials and try again.")
        }

        objectInputStream.close()
        fileInputStream.close()
      } else {
        //TODO: Replace with encrypted data source below
        UserRepository.createDataSource(applicationContext, it, ByteArray(0))
        success = true
      }
    }

    if (success) {
      preferences.putLastLoggedInTime()
      viewModel.setIsLoggedIn(true)

      //Show fragment
      login_email.visibility = View.GONE
      login_button.visibility = View.GONE
      val fragmentManager = supportFragmentManager
      fragmentManager.beginTransaction()
          .show(nav_host_fragment)
          .commit()
      fragmentManager.executePendingTransactions()
      bottom_navigation.visibility = View.VISIBLE
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    _binding = null
  }

  private fun toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }
}
