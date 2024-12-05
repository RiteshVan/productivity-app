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


//Activity wheres users can login with their details and proceed to main application
class SignInActivity : AppCompatActivity() {

    private lateinit var db:LoginDetailsDatabase

    private lateinit var usernameText: String

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        //Sets layout for page
        setContentView(R.layout.activity_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = LoginDetailsDatabase(this)

        val signIn = findViewById<ImageButton>(R.id.sign_in)



        //Listener for when user clicks button. User is then directed to HomeActivity, which hold the app fragments
        signIn.setOnClickListener{
            val username = findViewById<EditText>(R.id.username_input)
            val password = findViewById<EditText>(R.id.password_input)

            usernameText = username.text.toString()

            login(usernameText,password.text.toString())
        }

    }


    private fun login(username:String,password:String) {
        val userIsPresent = db.verifyUser(username,password)

        if (userIsPresent) {
            Toast.makeText(this,"Login successful!",Toast.LENGTH_SHORT).show()
            val intent = Intent(this,HomeActivity::class.java)
            intent.putExtra("Username",username)
            startActivity(intent)
            finish()
        }
        else{
            Toast.makeText(this,"Login unsuccessful!",Toast.LENGTH_SHORT).show()
        }
    }



}