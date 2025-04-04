package com.example.calculator

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.calculator.databinding.ActivityMainBinding
import net.objecthunter.exp4j.ExpressionBuilder

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<Pair<String, String>>()

    private var isEquals = false
    private val symbols = "÷×-+%,"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        historyList.addAll(HistoryStorageManager.loadHistory(this))

        historyAdapter = HistoryAdapter(historyList)

        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                stackFromEnd = true
                reverseLayout = true
            }
            adapter = historyAdapter
        }


        val tvPercent = binding.percent
        val tvPlus = binding.tvPlus
        val tvMultiple = binding.tvMultiple
        val tvMinus = binding.tvMinus
        val tvComma = binding.tvComma
        val ivDivide = binding.divide

        val tvButtons = listOf(
            binding.tv0, binding.tv1, binding.tv2, binding.tv3, binding.tv4,
            binding.tv5, binding.tv6, binding.tv7, binding.tv8, binding.tv9
        )

        for (button in tvButtons)
            button.setOnClickListener { addNumber(button.text.toString()) }

        ivDivide.setOnClickListener { addSymbol("÷") }
        tvMinus.setOnClickListener { addSymbol("-") }
        tvPlus.setOnClickListener { addSymbol("+") }
        tvMultiple.setOnClickListener { addSymbol("×") }
        tvComma.setOnClickListener { addSymbol(",") }


        binding.tvClearHistory.setOnClickListener { clearHistory() }
        binding.ivEraselast.setOnClickListener { eraseLast() }

        tvPercent.setOnClickListener {
            setDefaultStyleTextViews()
            applyPercent()
        }


        binding.tvClear.setOnClickListener {
            setDefaultStyleTextViews()
            binding.linearNumbers.visibility = View.GONE
            clearText()
        }

        binding.ivEquals.setOnClickListener {
            isEquals = true

            if(binding.inputText.text != "0")
                setReverseStyleTextViews()
        }
    }

    override fun onPause() {
        super.onPause()
        HistoryStorageManager.saveHistory(this, historyList)
    }

    override fun onDestroy() {
        super.onDestroy()
        HistoryStorageManager.saveHistory(this, historyList)
    }



    //✅ Ready
    private fun eraseLast() {
        val inputText = binding.inputText.text.toString()

        if (inputText.length == 1)
            binding.linearNumbers.visibility = View.GONE

        if (inputText.length != 1)
            binding.inputText.text = inputText.dropLast(1)

        else if (inputText.last() != '0') {
            binding.inputText.text = "0"
            setDefaultStyleTextViews()
        }

        calculateExpression()
    }

    //✅ Ready
    private fun addSymbol(symbol: String) {

        if(isEquals) {
            addItemToHistory()
            setDefaultStyleTextViews()

            binding.inputText.text = binding.resultText.text.toString()
            isEquals = false
        }

        // Если состояние isEquals - активное, то при вводе числа присваиваем результат к вводу
        var inputText = binding.inputText.text.toString()

        if (inputText.last() in symbols)
            inputText = inputText.dropLast(1)

        inputText += symbol
        binding.inputText.text = inputText

    }
    private fun addNumber(currentInput: String) {
//        setDefaultStyleTextViews()

        if(isEquals) {
            addItemToHistory()
            clearText()
            setDefaultStyleTextViews()
            isEquals = false
        }

        // Функция добавляет числа к строкам inputText
        val tvInput = binding.inputText

        if (tvInput.text.toString() == "0") {
            tvInput.text = ""
        }

        val resultText = tvInput.text.toString() + currentInput

        binding.inputText.text = resultText
        binding.resultText.text = resultText

        // NEW !!! linearNumbers view
        calculateExpression()

    }

    private fun clearText() {
        setDefaultStyleTextViews()
        binding.inputText.text = "0"
        binding.resultText.text = "0"
    }


    // Functions

    private fun calculateExpression() {
        if(binding.inputText.text.toString() != "0")
            binding.linearNumbers.visibility = View.VISIBLE

        try {
            val expression = binding.inputText.text
                .toString()
                .replace(",", ".")
                .replace("÷", "/")
                .replace("×", "*")

            val result = ExpressionBuilder(expression)
                .build()
                .evaluate()

            binding.resultText.text = formatNumber(result)
                .replace(".", ",")
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractLastNumber(expression: String): String? {
        val regex = Regex("[-]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?$")
        return regex.find(expression)?.value
    }

    private fun formatNumber(value: Double): String {
        val formatted = value
            .toBigDecimal()
            .stripTrailingZeros()
            .toPlainString()
        return formatted.replace("E", "e")
    }

    private fun applyPercent() {

        var inputText = binding.inputText.text.toString()
            .replace(",", ".")

        if (inputText == "0") return

        if (inputText.last() in symbols)
            inputText = inputText.dropLast(1)


        val lastNumber = extractLastNumber(inputText)
        if (!lastNumber.isNullOrEmpty()) {
            val result = ExpressionBuilder("$lastNumber / 100").build().evaluate()

            // Удаляем последнее число и заменяем его на результат
            inputText = inputText.dropLast(lastNumber.length) + formatNumber(result)
        }
        else {
            val result = ExpressionBuilder("$inputText / 100").build().evaluate()
            inputText = formatNumber(result)
        }
        binding.inputText.text = inputText.replace(".", ",")
        calculateExpression()
    }

    private fun addItemToHistory() {
        val inputText = binding.inputText.text.toString()
        val result = binding.resultText.text.toString()

        if (binding.inputText.text != "0") {
            historyAdapter.addItem(inputText, "= $result")
            binding.historyRecyclerView.smoothScrollToPosition(historyAdapter.itemCount - 1)
        }
    }

    private fun clearHistory() {
        historyAdapter.clearItems()
    }

    private fun setDefaultStyleTextViews() {
        binding.inputText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_Equals)
        binding.resultText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_AutoSolved)
        binding.equalsResultText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_AutoSolved)
    }
    private fun setReverseStyleTextViews() {
        binding.inputText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_AutoSolved)
        binding.resultText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_Equals)
        binding.equalsResultText
            .setTextAppearance(R.style.TextAppearance_AppCompat_Numbers_Equals)
    }
}