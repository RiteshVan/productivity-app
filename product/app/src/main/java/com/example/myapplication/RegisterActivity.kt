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
import okhttp3.FormBody
import okhttp3.*
import okhttp3.RequestBody
import java.io.IOException


//Register page where users can sign up or move to sign in screen
class RegisterActivity : AppCompatActivity() {

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        //Sets the layout for the register page
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        db = LoginDetailsDatabase(this)

        //Assigns sign in image button to variable
        val signInButton = findViewById<ImageButton>(R.id.registered_sign_in)


        //Listener checks for click and redirects user to sign in page
        signInButton.setOnClickListener{
            val intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)
        }

        val registerButton = findViewById<ImageButton>(R.id.register_button)

        registerButton.setOnClickListener {
            val usernameInput =  findViewById<EditText>(R.id.username_input)
            val passwordInput =  findViewById<EditText>(R.id.password_input)

            dbSignUp(usernameInput.text.toString(), passwordInput.text.toString())
        }



    }

    private fun dbSignUp(username: String , password: String) {

        val formBody:RequestBody = FormBody.Builder()
            .add("username",username)
            .add("password",password)
            .build()

        val request = Request.Builder().url("http://127.0.0.1:5000").post(formBody).build()

        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                    Toast.makeText(this@RegisterActivity,"Failed",Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call, response: Response) {
                TODO("Not yet implemented")
            }

        })

    }

}