package com.warehouse.inventory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.warehouse.inventory.data.dao.FloorDao
import com.warehouse.inventory.data.dao.ProductDao
import com.warehouse.inventory.data.model.Floor
import com.warehouse.inventory.data.model.Pallet
import com.warehouse.inventory.data.model.Product

@Database(
    entities = [Product::class, Floor::class, Pallet::class],
    version = 1,
    exportSchema = false
)
abstract class WarehouseDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun floorDao(): FloorDao
    
    companion object {
        @Volatile
        private var INSTANCE: WarehouseDatabase? = null
        
        fun getDatabase(context: Context): WarehouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarehouseDatabase::class.java,
                    "warehouse_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}