package com.shayanhaque.politicstracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Favorites live in their own table so that a market falling out of the
 * API's active set (e.g. it resolved) doesn't silently drop from the user's
 * watchlist — we still own the pointer even if the data is gone.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val marketId: String,
    val favoritedAtMillis: Long,
)
