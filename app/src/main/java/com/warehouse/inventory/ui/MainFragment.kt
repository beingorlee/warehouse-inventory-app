package com.warehouse.inventory.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.warehouse.inventory.R
import com.warehouse.inventory.databinding.FragmentMainBinding
import com.warehouse.inventory.data.model.Floor
import com.warehouse.inventory.data.model.Product
import com.warehouse.inventory.viewmodel.MainViewModel

class MainFragment : Fragment() {
    
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MainViewModel
    private var currentFloor = 1
    private val floors = mutableListOf<Floor>()
    private val products = mutableListOf<Product>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnSearch.setOnClickListener {
            searchProduct()
        }
        
        binding.btnAddProduct.setOnClickListener {
            showAddProductDialog()
        }
        
        binding.btnReset.setOnClickListener {
            showResetConfirmDialog()
        }
    }
    
    private fun observeViewModel() {
        viewModel.allFloors.observe(viewLifecycleOwner) { floorList ->
            floors.clear()
            floors.addAll(floorList)
            setupFloorTabs()
            if (floors.isNotEmpty()) {
                displayFloorPlan(currentFloor)
            }
        }
        
        viewModel.allProducts.observe(viewLifecycleOwner) { productList ->
            products.clear()
            products.addAll(productList)
            displayFloorPlan(currentFloor)
        }
        
        viewModel.searchResults.observe(viewLifecycleOwner) { results ->
            highlightSearchResults(results)
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }
    
    private fun setupFloorTabs() {
        binding.layoutFloorTabs.removeAllViews()
        
        floors.forEach { floor ->
            val button = MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            )
            button.text = "第${floor.floorNumber}层"
            button.setOnClickListener {
                currentFloor = floor.floorNumber
                displayFloorPlan(currentFloor)
                updateFloorTabSelection()
            }
            
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.marginEnd = 16
            button.layoutParams = layoutParams
            
            binding.layoutFloorTabs.addView(button)
        }
        
        updateFloorTabSelection()
    }
    
    private fun updateFloorTabSelection() {
        for (i in 0 until binding.layoutFloorTabs.childCount) {
            val button = binding.layoutFloorTabs.getChildAt(i) as MaterialButton
            val floorNumber = i + 1
            if (floorNumber == currentFloor) {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.purple_200))
            } else {
                button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            }
        }
    }
    
    private fun displayFloorPlan(floorNumber: Int) {
        binding.tvFloorTitle.text = "第${floorNumber}层平面图"
        binding.layoutFloorPlan.removeAllViews()
        
        val floor = floors.find { it.floorNumber == floorNumber } ?: return
        val floorProducts = products.filter { it.floorNumber == floorNumber }
        
        // 创建左区
        val leftArea = createStorageArea("L", floor.leftColumns, floorProducts)
        binding.layoutFloorPlan.addView(leftArea)
        
        // 创建过道
        val aisle = createAisle()
        binding.layoutFloorPlan.addView(aisle)
        
        // 创建右区
        val rightArea = createStorageArea("R", floor.rightColumns, floorProducts)
        binding.layoutFloorPlan.addView(rightArea)
    }
    
    private fun createStorageArea(side: String, columns: Int, floorProducts: List<Product>): LinearLayout {
        val area = LinearLayout(requireContext())
        area.orientation = LinearLayout.HORIZONTAL
        
        for (column in 1..columns) {
            val columnLayout = createColumn(side, column, floorProducts)
            area.addView(columnLayout)
        }
        
        return area
    }
    
    private fun createColumn(side: String, column: Int, floorProducts: List<Product>): LinearLayout {
        val columnLayout = LinearLayout(requireContext())
        columnLayout.orientation = LinearLayout.VERTICAL
        
        val layoutParams = LinearLayout.LayoutParams(120, LinearLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.marginEnd = 8
        columnLayout.layoutParams = layoutParams
        
        // 创建栈板位置 (默认每列3个栈板)
        for (row in 1..3) {
            val position = "${side}${column}-${row}"
            val palletView = createPalletView(position, floorProducts)
            columnLayout.addView(palletView)
        }
        
        return columnLayout
    }
    
    private fun createPalletView(position: String, floorProducts: List<Product>): View {
        val palletLayout = LinearLayout(requireContext())
        palletLayout.orientation = LinearLayout.VERTICAL
        palletLayout.setPadding(8, 8, 8, 8)
        palletLayout.setBackgroundResource(R.drawable.pallet_background)
        
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            100
        )
        layoutParams.bottomMargin = 8
        palletLayout.layoutParams = layoutParams
        
        // 位置标签
        val positionLabel = TextView(requireContext())
        positionLabel.text = position
        positionLabel.textSize = 10f
        positionLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
        palletLayout.addView(positionLabel)
        
        // 显示该位置的产品
        val positionProducts = floorProducts.filter { it.position == position }
        positionProducts.forEach { product ->
            val productLabel = TextView(requireContext())
            productLabel.text = "${product.model}(${product.quantity})"
            productLabel.textSize = 8f
            productLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.blue))
            palletLayout.addView(productLabel)
        }
        
        // 点击事件
        palletLayout.setOnClickListener {
            showPositionDialog(position, positionProducts)
        }
        
        return palletLayout
    }
    
    private fun createAisle(): View {
        val aisle = View(requireContext())
        val layoutParams = LinearLayout.LayoutParams(40, LinearLayout.LayoutParams.MATCH_PARENT)
        aisle.layoutParams = layoutParams
        aisle.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.teal_200))
        return aisle
    }
    
    private fun searchProduct() {
        val model = binding.etSearchModel.text.toString().trim()
        if (model.isBlank()) {
            Toast.makeText(context, "请输入产品型号", Toast.LENGTH_SHORT).show()
            return
        }
        
        viewModel.searchProductByModel(model)
    }
    
    private fun highlightSearchResults(results: List<Product>) {
        if (results.isEmpty()) {
            Toast.makeText(context, "未找到该产品", Toast.LENGTH_SHORT).show()
            return
        }
        
        // 显示搜索结果
        val message = results.joinToString("\n") { 
            "第${it.floorNumber}层 ${it.position}: ${it.model}(${it.quantity}个)"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("搜索结果")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showAddProductDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_product, null)
        
        val etModel = dialogView.findViewById<TextInputEditText>(R.id.etProductModel)
        val etQuantity = dialogView.findViewById<TextInputEditText>(R.id.etProductQuantity)
        val etFloor = dialogView.findViewById<TextInputEditText>(R.id.etProductFloor)
        val etPosition = dialogView.findViewById<TextInputEditText>(R.id.etProductPosition)
        
        etFloor.setText(currentFloor.toString())
        
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val model = etModel.text.toString()
            val quantity = etQuantity.text.toString()
            val floor = etFloor.text.toString()
            val position = etPosition.text.toString()
            
            if (model.isBlank() || quantity.isBlank() || floor.isBlank() || position.isBlank()) {
                Toast.makeText(context, "请填写所有字段", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            try {
                viewModel.addProduct(model, quantity, floor.toInt(), position.uppercase())
                dialog.dismiss()
                Toast.makeText(context, "产品添加成功", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "楼层必须是数字", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    private fun showPositionDialog(position: String, products: List<Product>) {
        if (products.isEmpty()) {
            Toast.makeText(context, "位置 $position 暂无产品", Toast.LENGTH_SHORT).show()
            return
        }
        
        val message = products.joinToString("\n") { 
            "${it.model}: ${it.quantity}个"
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("位置 $position")
            .setMessage(message)
            .setPositiveButton("确定", null)
            .show()
    }
    
    private fun showResetConfirmDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("重置仓库")
            .setMessage("此操作将清空所有数据，请输入"确认"来继续")
            .create()
        
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_reset, null)
        dialog.setView(dialogView)
        
        val etConfirm = dialogView.findViewById<TextInputEditText>(R.id.etConfirm)
        
        dialogView.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }
        
        dialogView.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val confirmText = etConfirm.text.toString()
            if (confirmText == "确认") {
                viewModel.resetWarehouse()
                dialog.dismiss()
                Toast.makeText(context, "仓库已重置", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "请输入"确认"", Toast.LENGTH_SHORT).show()
            }
        }
        
        dialog.show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}