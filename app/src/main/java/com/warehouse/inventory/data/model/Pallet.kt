package com.warehouse.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pallets")
data class Pallet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val floorNumber: Int,
    val position: String, // "L1-1", "R3-2" 等
    val column: Int,
    val row: Int,
    val side: String // "L" 或 "R"
) {
    companion object {
        fun generatePosition(side: String, column: Int, row: Int): String {
            return "$side$column-$row"
        }
        
        fun parsePosition(position: String): Triple<String, Int, Int>? {
            val regex = Regex("^([LR])(\\d+)-(\\d+)$")
            val matchResult = regex.find(position)
            return if (matchResult != null) {
                val (side, column, row) = matchResult.destructured
                Triple(side, column.toInt(), row.toInt())
            } else {
                null
            }
        }
    }
}