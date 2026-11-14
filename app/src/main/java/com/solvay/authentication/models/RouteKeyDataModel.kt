package com.solvay.authentication.models

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.solvay.BuildConfig

data class RouteKeyRequest (
    val route: String? = null,
    val qr: Boolean? = null,
    val checkRoute: Boolean? = null,
    var location: List<Double?>? = null,
)

data class RouteKeyResponse(
    val success: Boolean? = null,
    var message: String? = null,
    val data: RouteData? = null,
    val error: Int? = null
)

data class RouteData(
    val route: Route? = null,
    val expired: Boolean? = null
)

data class Route(
    val route: String? = null,
    val pickUp: Location? = null,
    val drop: Location? = null,
    val stops: List<Location>? = null,
    val jsonArray: Any? = null,
    val qr: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val duration: Int? = null,
    val lastLocation: String? = null,
    val progress: Int? = null,
    val status: Any? = null,
    val parentId: String? = null,
    val _id: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val __v: Int? = null
)

//data class Location(
//    val name: String? = null,
//    val type: String? = null,
//    val cords: List<Double>? = null,
//    val file: String? = null,
//    val _id: String? = null,
//    var isCurrentLocation: Boolean? = null,
//    var isLastLocation: Boolean? = null
//)

data class Location(
    val name: String? = null,
    val type: String? = null,
    val cords: List<Double>? = null,
    val file: String? = null,
    val _id: String? = null,
    var isAutomated: Boolean? = null,
    var isRescue: Boolean? = null,
    var isCurrentLocation: Boolean? = null,
    var isLastLocation: Boolean? = null,
    var image: String? = null,
    var imageBitmap: Bitmap? = null,
    var imageDrawable: Drawable? = null,
    var description: String? = null,
    var title: String? = null,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        cords = parcel.createDoubleArray()?.toList(),
        type = parcel.readString(),
        isCurrentLocation = parcel.readByte() != 0.toByte(),
        isLastLocation = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDoubleArray(cords?.toDoubleArray() ?: doubleArrayOf())
        parcel.writeString(type)
        parcel.writeByte(if (isCurrentLocation == true) 1 else 0)
        parcel.writeByte(if (isLastLocation == true) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Location> {
        override fun createFromParcel(parcel: Parcel): Location = Location(parcel)
        override fun newArray(size: Int): Array<Location?> = arrayOfNulls(size)
    }
}






