package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.FavoriteStationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteStationDao {
    @Query("SELECT * FROM favorite_stations ORDER BY addedTimestamp DESC")
    fun getAllFavorites(): Flow<List<FavoriteStationEntity>>

    @Query("SELECT * FROM favorite_stations")
    suspend fun getAllFavoritesList(): List<FavoriteStationEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationId = :stationId)")
    fun isFavoriteFlow(stationId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_stations WHERE stationId = :stationId)")
    suspend fun isFavorite(stationId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteStationEntity)

    @Query("DELETE FROM favorite_stations WHERE stationId = :stationId")
    suspend fun deleteFavoriteById(stationId: String)

    @Update
    suspend fun updateFavorite(favorite: FavoriteStationEntity)
}
