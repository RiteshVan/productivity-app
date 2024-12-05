package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


//Register page where users can sign up or move to sign in screen
class RegisterActivity : AppCompatActivity() {

    private lateinit var db: LoginDetailsDatabase

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

        if (username.isNotEmpty() && password.isNotEmpty()) {
            val insertQuery = db.addUser(username,password)
            if (insertQuery){
                Toast.makeText(this,"Sign up success!",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, SignInActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this,"Sign up error",Toast.LENGTH_SHORT).show()
            }

        }
        else{
            Toast.makeText(this,"Field(s) are empty",Toast.LENGTH_SHORT).show()
        }
    }

}