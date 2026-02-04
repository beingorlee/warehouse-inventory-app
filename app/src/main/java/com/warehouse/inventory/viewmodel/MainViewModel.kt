package com.warehouse.inventory.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.warehouse.inventory.data.database.WarehouseDatabase
import com.warehouse.inventory.data.model.Floor
import com.warehouse.inventory.data.model.Product
import com.warehouse.inventory.repository.WarehouseRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: WarehouseRepository
    
    val allProducts: LiveData<List<Product>>
    val allFloors: LiveData<List<Floor>>
    
    private val _searchResults = MutableLiveData<List<Product>>()
    val searchResults: LiveData<List<Product>> = _searchResults
    
    private val _isInitialized = MutableLiveData<Boolean>()
    val isInitialized: LiveData<Boolean> = _isInitialized
    
    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage
    
    init {
        val database = WarehouseDatabase.getDatabase(application)
        repository = WarehouseRepository(database.productDao(), database.floorDao())
        allProducts = repository.getAllProducts()
        allFloors = repository.getAllFloors()
        
        checkInitializationStatus()
    }
    
    private fun checkInitializationStatus() {
        viewModelScope.launch {
            val floorCount = repository.getFloorCount()
            _isInitialized.value = floorCount > 0
        }
    }
    
    fun searchProductByModel(model: String) {
        if (model.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        
        val formattedModel = Product.formatModel(model)
        if (!Product.validateModel(formattedModel)) {
            _errorMessage.value = "产品型号格式不正确"
            return
        }
        
        viewModelScope.launch {
            repository.getProductsByModel(formattedModel).observeForever { products ->
                _searchResults.value = products
            }
        }
    }
    
    fun addFloor(floorNumber: Int, leftColumns: Int, rightColumns: Int) {
        if (!Floor.validateColumns(leftColumns) || !Floor.validateColumns(rightColumns)) {
            _errorMessage.value = "列数必须在${Floor.MIN_COLUMNS}-${Floor.MAX_COLUMNS}之间"
            return
        }
        
        viewModelScope.launch {
            try {
                val floor = Floor(floorNumber, leftColumns, rightColumns)
                repository.insertFloor(floor)
                checkInitializationStatus()
            } catch (e: Exception) {
                _errorMessage.value = "添加楼层失败: ${e.message}"
            }
        }
    }
    
    fun addProduct(model: String, quantity: String, floorNumber: Int, position: String) {
        val formattedModel = Product.formatModel(model)
        
        if (!Product.validateModel(formattedModel)) {
            _errorMessage.value = "产品型号格式不正确"
            return
        }
        
        if (!Product.validateQuantity(quantity)) {
            _errorMessage.value = "数量必须是正整数"
            return
        }
        
        viewModelScope.launch {
            try {
                val success = repository.addOrUpdateProduct(
                    formattedModel, 
                    quantity.toInt(), 
                    floorNumber, 
                    position
                )
                if (!success) {
                    _errorMessage.value = "添加产品失败"
                }
            } catch (e: Exception) {
                _errorMessage.value = "添加产品失败: ${e.message}"
            }
        }
    }
    
    fun updateProductQuantity(productId: Long, newQuantity: String) {
        if (!Product.validateQuantity(newQuantity)) {
            _errorMessage.value = "数量必须是正整数"
            return
        }
        
        viewModelScope.launch {
            try {
                repository.updateQuantity(productId, newQuantity.toInt())
            } catch (e: Exception) {
                _errorMessage.value = "更新数量失败: ${e.message}"
            }
        }
    }
    
    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                repository.deleteProduct(product)
            } catch (e: Exception) {
                _errorMessage.value = "删除产品失败: ${e.message}"
            }
        }
    }
    
    fun resetWarehouse() {
        viewModelScope.launch {
            try {
                repository.initializeWarehouse()
                _isInitialized.value = false
            } catch (e: Exception) {
                _errorMessage.value = "重置仓库失败: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = ""
    }
}