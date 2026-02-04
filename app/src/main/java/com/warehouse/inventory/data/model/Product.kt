package com.warehouse.inventory.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val model: String,
    val quantity: Int,
    val floorNumber: Int,
    val position: String // 格式: "L5-2" 或 "R3-1"
) {
    companion object {
        // 产品型号验证正则表达式: 字母开头，5-7位，包含字母、数字或-
        private val MODEL_PATTERN = Regex("^[A-Z][A-Z0-9-]{4,6}$")
        
        fun validateModel(model: String): Boolean {
            return MODEL_PATTERN.matches(model.trim().uppercase())
        }
        
        fun formatModel(input: String): String {
            return input.trim().uppercase()
        }
        
        fun validateQuantity(quantity: String): Boolean {
            return try {
                val num = quantity.toInt()
                num > 0
            } catch (e: NumberFormatException) {
                false
            }
        }
    }
}