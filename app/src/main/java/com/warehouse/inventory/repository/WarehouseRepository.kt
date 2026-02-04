package com.warehouse.inventory.repository

import androidx.lifecycle.LiveData
import com.warehouse.inventory.data.dao.FloorDao
import com.warehouse.inventory.data.dao.ProductDao
import com.warehouse.inventory.data.model.Floor
import com.warehouse.inventory.data.model.Product

class WarehouseRepository(
    private val productDao: ProductDao,
    private val floorDao: FloorDao
) {
    
    // Product operations
    fun getAllProducts(): LiveData<List<Product>> = productDao.getAllProducts()
    
    fun getProductsByModel(model: String): LiveData<List<Product>> = 
        productDao.getProductsByModel(model)
    
    fun getProductsByFloor(floorNumber: Int): LiveData<List<Product>> = 
        productDao.getProductsByFloor(floorNumber)
    
    fun getProductsByPosition(floorNumber: Int, position: String): LiveData<List<Product>> = 
        productDao.getProductsByPosition(floorNumber, position)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun updateQuantity(id: Long, quantity: Int) = 
        productDao.updateQuantity(id, quantity)
    
    suspend fun deleteAllProducts() = productDao.deleteAllProducts()
    
    // Floor operations
    fun getAllFloors(): LiveData<List<Floor>> = floorDao.getAllFloors()
    
    suspend fun getFloor(floorNumber: Int): Floor? = floorDao.getFloor(floorNumber)
    
    suspend fun insertFloor(floor: Floor) = floorDao.insertFloor(floor)
    
    suspend fun updateFloor(floor: Floor) = floorDao.updateFloor(floor)
    
    suspend fun deleteFloor(floor: Floor) = floorDao.deleteFloor(floor)
    
    suspend fun deleteAllFloors() = floorDao.deleteAllFloors()
    
    suspend fun getFloorCount(): Int = floorDao.getFloorCount()
    
    // Business logic methods
    suspend fun initializeWarehouse() {
        deleteAllProducts()
        deleteAllFloors()
    }
    
    suspend fun addOrUpdateProduct(
        model: String, 
        quantity: Int, 
        floorNumber: Int, 
        position: String
    ): Boolean {
        return try {
            val formattedModel = Product.formatModel(model)
            if (!Product.validateModel(formattedModel) || !Product.validateQuantity(quantity.toString())) {
                return false
            }
            
            val product = Product(
                model = formattedModel,
                quantity = quantity,
                floorNumber = floorNumber,
                position = position
            )
            insertProduct(product)
            true
        } catch (e: Exception) {
            false
        }
    }
}