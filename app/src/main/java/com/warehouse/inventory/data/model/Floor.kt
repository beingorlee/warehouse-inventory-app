package com.warehouse.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "floors")
data class Floor(
    @PrimaryKey
    val floorNumber: Int,
    val leftColumns: Int,
    val rightColumns: Int
) {
    companion object {
        const val MIN_COLUMNS = 1
        const val MAX_COLUMNS = 20
        
        fun validateColumns(columns: Int): Boolean {
            return columns in MIN_COLUMNS..MAX_COLUMNS
        }
    }
}