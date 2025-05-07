package ba.sum.fpmoz.studytrack

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<EditText>(R.id.loginEmailEditText)
        val passwordEditText = findViewById<EditText>(R.id.loginPasswordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)

        // Postavi onClickListener za Create Account gumb
        createAccountButton.setOnClickListener {
            // Kada korisnik klikne "Create Account", otvori RegisterActivity
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
