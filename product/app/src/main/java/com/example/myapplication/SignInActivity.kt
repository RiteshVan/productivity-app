package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import java.io.IOException

// Activity wheres users can login with their details and proceed to main application
class SignInActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    private lateinit var usernameText: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Sets layout for page
        setContentView(R.layout.activity_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val signIn = findViewById<ImageButton>(R.id.sign_in)

        // Listener for when user clicks button. User is then directed to HomeActivity, which hold the app fragments
        signIn.setOnClickListener {
            val username = findViewById<EditText>(R.id.username_input)
            val password = findViewById<EditText>(R.id.password_input)

            usernameText = username.text.toString()

            login(usernameText, password.text.toString())
        }
    }

    // User details sent to backend in order to verify user
    private fun login(
        username: String,
        password: String,
    ) {
        val formBody: RequestBody =
            FormBody
                .Builder()
                .add("username", username)
                .add("password", password)
                .build()

        val request =
            Request
                .Builder()
                .url("http://192.168.1.112:5000/login")
                .post(formBody)
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    runOnUiThread {
                        Toast.makeText(this@SignInActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    runOnUiThread {
                        try {
                            if (response.isSuccessful) {
                                val responseBody = response.body?.string()

                                if (responseBody == "Logged in successfully") {
                                    Toast
                                        .makeText(
                                            this@SignInActivity,
                                            responseBody,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                                    startActivity(intent)
                                } else {
                                    Toast
                                        .makeText(
                                            this@SignInActivity,
                                            responseBody,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast
                                .makeText(
                                    this@SignInActivity,
                                    "Response error",
                                    Toast.LENGTH_SHORT,
                                ).show()
                        } finally {
                            response.close()
                        }
                    }
                }
            },
        )
    }
}
