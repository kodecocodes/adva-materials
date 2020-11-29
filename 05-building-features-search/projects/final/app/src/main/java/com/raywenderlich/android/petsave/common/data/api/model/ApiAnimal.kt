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

package com.raywenderlich.android.petsave.common.data.api.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiAnimal(
    @field:Json(name = "id") val id: Long?,
    @field:Json(name = "organization_id") val organizationId: String?,
    @field:Json(name = "url") val url: String?,
    @field:Json(name = "type") val type: String?,
    @field:Json(name = "species") val species:String?,
    @field:Json(name = "breeds") val breeds: ApiBreeds?,
    @field:Json(name = "colors") val colors: ApiColors?,
    @field:Json(name = "age") val age: String?,
    @field:Json(name = "gender") val gender: String?,
    @field:Json(name = "size") val size: String?,
    @field:Json(name = "coat") val coat: String?,
    @field:Json(name = "name") val name: String?,
    @field:Json(name = "description") val description: String?,
    @field:Json(name = "photos") val photos: List<ApiPhotoSizes>?,
    @field:Json(name = "videos") val videos: List<ApiVideoLink>?,
    @field:Json(name = "status") val status: String?,
    @field:Json(name = "attributes") val attributes: ApiAttributes?,
    @field:Json(name = "environment") val environment: ApiEnvironment?,
    @field:Json(name = "tags") val tags: List<String?>?,
    @field:Json(name = "contact") val contact: ApiContact?,
    @field:Json(name = "published_at") val publishedAt: String?,
    @field:Json(name = "distance") val distance: Float?
)

@JsonClass(generateAdapter = true)
data class ApiBreeds(
    @field:Json(name = "primary") val primary: String?,
    @field:Json(name = "secondary") val secondary: String?,
    @field:Json(name = "mixed") val mixed: Boolean?,
    @field:Json(name = "unknown") val unknown: Boolean?
)

@JsonClass(generateAdapter = true)
data class ApiColors(
    @field:Json(name = "primary") val primary: String?,
    @field:Json(name = "secondary") val secondary: String?,
    @field:Json(name = "tertiary") val tertiary: String?
)

@JsonClass(generateAdapter = true)
data class ApiPhotoSizes(
    @field:Json(name = "small") val small: String?,
    @field:Json(name = "medium") val medium: String?,
    @field:Json(name = "large") val large: String?,
    @field:Json(name = "full") val full: String?
)

@JsonClass(generateAdapter = true)
data class ApiVideoLink(
    @field:Json(name = "embed") val embed: String?
)

@JsonClass(generateAdapter = true)
data class ApiAttributes(
    @field:Json(name = "spayed_neutered") val spayedNeutered: Boolean?,
    @field:Json(name = "house_trained") val houseTrained: Boolean?,
    @field:Json(name = "declawed") val declawed: Boolean?,
    @field:Json(name = "special_needs") val specialNeeds: Boolean?,
    @field:Json(name = "shots_current") val shotsCurrent: Boolean?
)

@JsonClass(generateAdapter = true)
data class ApiEnvironment(
    @field:Json(name = "children") val children: Boolean?,
    @field:Json(name = "dogs") val dogs: Boolean?,
    @field:Json(name = "cats") val cats: Boolean?
)

@JsonClass(generateAdapter = true)
data class ApiContact(
    @field:Json(name = "email") val email: String?,
    @field:Json(name = "phone") val phone: String?,
    @field:Json(name = "address") val address: ApiAddress?
)

@JsonClass(generateAdapter = true)
data class ApiAddress(
    @field:Json(name = "address1") val address1: String?,
    @field:Json(name = "address2") val address2: String?,
    @field:Json(name = "city") val city: String?,
    @field:Json(name = "state") val state: String?,
    @field:Json(name = "postcode") val postcode: String?,
    @field:Json(name = "country") val country: String?
)