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

package com.raywenderlich.android.petsave.main.presentation

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.dynamicfeatures.fragment.DynamicNavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

/**
 * Main Screen
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding

  private val viewModel by viewModels<MainActivityViewModel>()

  private val navController by lazy {
    (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as DynamicNavHostFragment)
      .navController
  }
  
  private val appBarConfiguration by lazy {
    AppBarConfiguration(topLevelDestinationIds = setOf(
        R.id.onboardingFragment,
        R.id.animalsNearYouFragment,
        R.id.searchFragment
    ))
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    // Switch to AppTheme for displaying the activity
    setTheme(R.style.AppTheme)

    super.onCreate(savedInstanceState)

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setupActionBar()
    setupBottomNav()
    triggerStartDestinationEvent()
    observeViewEffects()
  }

  override fun onSupportNavigateUp(): Boolean {
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }

  private fun setupActionBar() {
    setSupportActionBar(binding.toolbar)
    setupActionBarWithNavController(navController, appBarConfiguration)
  }

  private fun setupBottomNav() {
    binding.bottomNavigation.setupWithNavController(navController)
    hideBottomNavWhenNeeded()
  }

  private fun hideBottomNavWhenNeeded() {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.onboardingFragment -> binding.bottomNavigation.visibility = View.GONE
        else -> binding.bottomNavigation.visibility = View.VISIBLE
      }
    }
  }

  private fun triggerStartDestinationEvent() {
    viewModel.onEvent(MainActivityEvent.DefineStartDestination)
  }

  private fun observeViewEffects() {
    lifecycleScope.launchWhenStarted {
      viewModel.viewEffect.collect { reactTo(it) }
    }
  }

  private fun reactTo(effect: MainActivityViewEffect) {
    when (effect) {
      is MainActivityViewEffect.SetStartDestination -> setNavGraphStartDestination(effect.destination)
    }
  }

  private fun setNavGraphStartDestination(startDestination: Int) {
    val navGraph = navController.navInflater.inflate(R.navigation.nav_graph)

    navGraph.startDestination = startDestination
    navController.graph = navGraph
  }
}
