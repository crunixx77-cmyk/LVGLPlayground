package com.example.lvglplayground

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var codeInput: EditText
    private lateinit var errorLogText: TextView
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttachFile: ImageButton
    private lateinit var interactiveCanvas: InteractiveCanvasView

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { readTextFromUri(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        codeInput = findViewById(R.id.codeInput)
        errorLogText = findViewById(R.id.errorLogText)
        btnSend = findViewById(R.id.btnSend)
        btnAttachFile = findViewById(R.id.btnAttachFile)
        interactiveCanvas = findViewById(R.id.interactiveCanvas)

        initNativeEngine()

        btnSend.setOnClickListener {
            val code = codeInput.text.toString()
            if (code.isNotBlank()) {
                val result = runScriptNative(code)
                if (result == "OK") {
                    errorLogText.visibility = View.GONE
                } else {
                    errorLogText.text = result
                    errorLogText.visibility = View.VISIBLE
                }
            }
        }

        btnAttachFile.setOnClickListener {
            openFileLauncher.launch("*/*")
        }
    }

    private fun readTextFromUri(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val stringBuilder = StringBuilder()
                    var line: String? = reader.readLine()
                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = reader.readLine()
                    }
                    codeInput.setText(stringBuilder.toString())
                }
            }
        } catch (e: Exception) {
            errorLogText.text = "Gagal membaca file: ${e.message}"
            errorLogText.visibility = View.VISIBLE
        }
    }

    private external fun initNativeEngine()
    private external fun runScriptNative(code: String): String

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }
}
