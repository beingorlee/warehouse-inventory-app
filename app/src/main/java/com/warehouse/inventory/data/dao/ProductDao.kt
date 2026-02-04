package com.warehouse.inventory.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.warehouse.inventory.data.model.Product

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products ORDER BY floorNumber, position")
    fun getAllProducts(): LiveData<List<Product>>
    
    @Query("SELECT * FROM products WHERE model = :model")
    fun getProductsByModel(model: String): LiveData<List<Product>>
    
    @Query("SELECT * FROM products WHERE floorNumber = :floorNumber")
    fun getProductsByFloor(floorNumber: Int): LiveData<List<Product>>
    
    @Query("SELECT * FROM products WHERE position = :position AND floorNumber = :floorNumber")
    fun getProductsByPosition(floorNumber: Int, position: String): LiveData<List<Product>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("DELETE FROM products WHERE position = :position AND floorNumber = :floorNumber AND model = :model")
    suspend fun deleteProductByPosition(floorNumber: Int, position: String, model: String)
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
    
    @Query("UPDATE products SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int)
}