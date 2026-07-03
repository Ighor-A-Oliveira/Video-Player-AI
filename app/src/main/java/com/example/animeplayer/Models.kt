package com.example.animeplayer

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Episode(
    val uriString: String,
    val name: String,
    val seriesName: String,
    val index: Int
) : Parcelable

data class Season(
    val name: String,
    val episodes: List<Episode>
)

data class Series(
    val name: String,
    val seasons: List<Season>
)
