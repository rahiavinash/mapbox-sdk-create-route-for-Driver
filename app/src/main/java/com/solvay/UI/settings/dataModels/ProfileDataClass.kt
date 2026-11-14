package com.solvay.UI.settings.dataModels

import android.net.Uri
import com.google.gson.annotations.SerializedName
import com.mapbox.navigation.tripdata.maneuver.model.PrimaryManeuver

data class ProfileResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: ProfileData? = null
)

data class ProfileData(
    @SerializedName("_id") val id: String? = null,
    val language: String? = null,
    val name: String? = null,
    val plateNumber: String? = null,
    val contractNumber: String? = null,
    val image: String? = null,
    val imageUri: Uri? = null
)

data class ProfileImageUpdation(
    val imageUri: Uri? = null
)
data class textUpdate(
    val primaryText: String? = null,
    val distance: String? = null,
    val time: String? = null,
    val primaryManeuver: PrimaryManeuver?= null
)

//data class PrimaryManeuver(
//    val text: String,
//    val type: String,
//    val degrees: Int?, // nullable, since it's `null` in your example
//    val modifier: String?,
//    val drivingSide: String?,
//    val componentList: List<Component>
//)
//
//data class Component(
//    val type: String,
//    val node: TextComponentNode
//)
//
//data class TextComponentNode(
//    val text: String,
//    val abbr: String?,
//    val abbrPriority: Int?
//)
