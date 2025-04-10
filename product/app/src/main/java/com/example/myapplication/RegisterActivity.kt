package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.*
import java.io.IOException

// Register page where users can sign up or move to sign in screen
class RegisterActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // Sets the layout for the register page
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Assigns sign in image button to variable
        val signInButton = findViewById<ImageButton>(R.id.registered_sign_in)

        // Listener checks for click and redirects user to sign in page
        signInButton.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val registerButton = findViewById<ImageButton>(R.id.register_button)

        registerButton.setOnClickListener {
            val usernameInput = findViewById<EditText>(R.id.username_input)
            val passwordInput = findViewById<EditText>(R.id.password_input)

            dbSignUp(usernameInput.text.toString(), passwordInput.text.toString())
        }
    }

    // Function used to sign up and details sent to database for verification
    private fun dbSignUp(
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
                .url("http://192.168.1.112:5000/register")
                .post(formBody)
                .build()

        client.newCall(request).enqueue(
            object : Callback {
                override fun onFailure(
                    call: Call,
                    e: IOException,
                ) {
                    runOnUiThread {
                        Toast.makeText(this@RegisterActivity, "Error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(
                    call: Call,
                    response: Response,
                ) {
                    try {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            if (responseBody == "User Added Successfully!") {
                                runOnUiThread {
                                    Toast
                                        .makeText(
                                            this@RegisterActivity,
                                            responseBody,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    val intent =
                                        Intent(this@RegisterActivity, SignInActivity::class.java)
                                    startActivity(intent)
                                }
                            } else {
                                runOnUiThread {
                                    Toast
                                        .makeText(
                                            this@RegisterActivity,
                                            responseBody,
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("NetworkError", "Error reading response: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@RegisterActivity, "Response error", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        response.close()
                    }
                }
            },
        )
    }
}
