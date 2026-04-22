package com.shayanhaque.politicstracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.shayanhaque.politicstracker.data.local.entity.FavoriteEntity
import com.shayanhaque.politicstracker.data.local.entity.MarketEntity
import com.shayanhaque.politicstracker.data.local.entity.PricePointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketDao {

    // --- Markets -----------------------------------------------------------

    @Query("SELECT * FROM markets ORDER BY volumeUsd DESC")
    fun observeMarkets(): Flow<List<MarketEntity>>

    @Query("SELECT * FROM markets WHERE id = :id LIMIT 1")
    suspend fun getMarket(id: String): MarketEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMarkets(markets: List<MarketEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMarket(market: MarketEntity)

    @Query("SELECT MAX(cachedAtMillis) FROM markets")
    suspend fun latestCacheTimestamp(): Long?

    @Query("DELETE FROM markets")
    suspend fun clearMarkets()

    /** Atomically swap the market cache so observers never see a half-empty list. */
    @Transaction
    suspend fun replaceMarkets(markets: List<MarketEntity>) {
        clearMarkets()
        upsertMarkets(markets)
    }

    // --- Favorites ---------------------------------------------------------

    @Query(
        """
        SELECT m.* FROM markets AS m
        INNER JOIN favorites AS f ON f.marketId = m.id
        ORDER BY f.favoritedAtMillis DESC
        """
    )
    fun observeFavoriteMarkets(): Flow<List<MarketEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE marketId = :id)")
    fun observeIsFavorite(id: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE marketId = :id")
    suspend fun removeFavorite(id: String)

    // --- Price history -----------------------------------------------------

    @Query("SELECT * FROM price_points WHERE marketId = :id ORDER BY timestampMillis ASC")
    fun observePriceHistory(id: String): Flow<List<PricePointEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPriceHistory(points: List<PricePointEntity>)

    @Query("DELETE FROM price_points WHERE marketId = :id")
    suspend fun clearPriceHistory(id: String)

    @Transaction
    suspend fun replacePriceHistory(id: String, points: List<PricePointEntity>) {
        clearPriceHistory(id)
        upsertPriceHistory(points)
    }
}
