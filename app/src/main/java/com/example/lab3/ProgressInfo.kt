package com.example.lab3

import android.os.Parcel
import android.os.Parcelable

class ProgressInfo(
        var mDownloadedBytes : Int =0,
        var mSize : Int = 0,
        var mStatus : String = "")
    : Parcelable {

        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString() ?: ""
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeInt(mDownloadedBytes)
            dest.writeInt(mSize)
            dest.writeString(mStatus)
        }

        companion object CREATOR : Parcelable.Creator<ProgressInfo> {
            override fun createFromParcel(source: Parcel): ProgressInfo? {
                return ProgressInfo(source)
            }

            override fun newArray(size: Int): Array<ProgressInfo?> {
                return arrayOfNulls(size)
            }
        }


}