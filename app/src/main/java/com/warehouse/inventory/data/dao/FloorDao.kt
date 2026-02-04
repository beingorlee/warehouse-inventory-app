package com.warehouse.inventory.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.warehouse.inventory.data.model.Floor

@Dao
interface FloorDao {
    
    @Query("SELECT * FROM floors ORDER BY floorNumber")
    fun getAllFloors(): LiveData<List<Floor>>
    
    @Query("SELECT * FROM floors WHERE floorNumber = :floorNumber")
    suspend fun getFloor(floorNumber: Int): Floor?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFloor(floor: Floor)
    
    @Update
    suspend fun updateFloor(floor: Floor)
    
    @Delete
    suspend fun deleteFloor(floor: Floor)
    
    @Query("DELETE FROM floors")
    suspend fun deleteAllFloors()
    
    @Query("SELECT COUNT(*) FROM floors")
    suspend fun getFloorCount(): Int
}