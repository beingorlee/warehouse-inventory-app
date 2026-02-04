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
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.warehouse.inventory.R
import com.warehouse.inventory.databinding.FragmentSetupBinding
import com.warehouse.inventory.viewmodel.MainViewModel

class SetupFragment : Fragment() {
    
    private var _binding: FragmentSetupBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: MainViewModel
    private var floorCount = 0
    private val floorConfigurations = mutableListOf<Pair<Int, Int>>() // (leftColumns, rightColumns)
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        
        setupClickListeners()
        observeViewModel()
    }
    
    private fun setupClickListeners() {
        binding.btnCreateFloors.setOnClickListener {
            createFloorSetup()
        }
        
        binding.btnCompleteSetup.setOnClickListener {
            completeSetup()
        }
    }
    
    private fun observeViewModel() {
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
        
        viewModel.isInitialized.observe(viewLifecycleOwner) { isInitialized ->
            if (isInitialized) {
                findNavController().navigate(R.id.action_setupFragment_to_mainFragment)
            }
        }
    }
    
    private fun createFloorSetup() {
        val floorCountText = binding.etFloorCount.text.toString()
        if (floorCountText.isBlank()) {
            Toast.makeText(context, "请输入楼层数", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            floorCount = floorCountText.toInt()
            if (floorCount < 1 || floorCount > 10) {
                Toast.makeText(context, "楼层数必须在1-10之间", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: NumberFormatException) {
            Toast.makeText(context, "请输入有效的楼层数", Toast.LENGTH_SHORT).show()
            return
        }
        
        createFloorConfigurationViews()
        binding.layoutFloorSetup.visibility = View.VISIBLE
        binding.btnCompleteSetup.visibility = View.VISIBLE
    }
    
    private fun createFloorConfigurationViews() {
        binding.layoutFloorSetup.removeAllViews()
        floorConfigurations.clear()
        
        for (i in 1..floorCount) {
            val floorView = createFloorConfigView(i)
            binding.layoutFloorSetup.addView(floorView)
        }
    }
    
    private fun createFloorConfigView(floorNumber: Int): View {
        val inflater = LayoutInflater.from(context)
        val cardView = inflater.inflate(R.layout.item_floor_config, binding.layoutFloorSetup, false)
        
        val tvFloorTitle = cardView.findViewById<TextView>(R.id.tvFloorTitle)
        val etLeftColumns = cardView.findViewById<TextInputEditText>(R.id.etLeftColumns)
        val etRightColumns = cardView.findViewById<TextInputEditText>(R.id.etRightColumns)
        
        tvFloorTitle.text = "第${floorNumber}层配置"
        etLeftColumns.setText("5") // 默认值
        etRightColumns.setText("5") // 默认值
        
        return cardView
    }
    
    private fun completeSetup() {
        // 收集所有楼层配置
        floorConfigurations.clear()
        
        for (i in 0 until binding.layoutFloorSetup.childCount) {
            val floorView = binding.layoutFloorSetup.getChildAt(i)
            val etLeftColumns = floorView.findViewById<TextInputEditText>(R.id.etLeftColumns)
            val etRightColumns = floorView.findViewById<TextInputEditText>(R.id.etRightColumns)
            
            val leftText = etLeftColumns.text.toString()
            val rightText = etRightColumns.text.toString()
            
            if (leftText.isBlank() || rightText.isBlank()) {
                Toast.makeText(context, "请填写第${i + 1}层的列数配置", Toast.LENGTH_SHORT).show()
                return
            }
            
            try {
                val leftColumns = leftText.toInt()
                val rightColumns = rightText.toInt()
                
                if (leftColumns < 1 || leftColumns > 20 || rightColumns < 1 || rightColumns > 20) {
                    Toast.makeText(context, "列数必须在1-20之间", Toast.LENGTH_SHORT).show()
                    return
                }
                
                floorConfigurations.add(Pair(leftColumns, rightColumns))
            } catch (e: NumberFormatException) {
                Toast.makeText(context, "请输入有效的列数", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // 创建楼层
        for (i in floorConfigurations.indices) {
            val (leftColumns, rightColumns) = floorConfigurations[i]
            viewModel.addFloor(i + 1, leftColumns, rightColumns)
        }
        
        Toast.makeText(context, "仓库设置完成", Toast.LENGTH_SHORT).show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}