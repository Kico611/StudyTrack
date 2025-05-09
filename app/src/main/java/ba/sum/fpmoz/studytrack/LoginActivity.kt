package ba.sum.fpmoz.studytrack

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicijalizacija Firebase autentifikacije
        auth = FirebaseAuth.getInstance()

        // Povezivanje s XML elementima
        val emailEditText = findViewById<EditText>(R.id.loginEmailEditText)
        val passwordEditText = findViewById<EditText>(R.id.loginPasswordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val resetPasswordButton = findViewById<Button>(R.id.resetPasswordButton)

        // Login dugme
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Unesite email i lozinku", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Prijava uspješna", Toast.LENGTH_SHORT).show()
                        // Ovdje preusmjeriš korisnika na početni ekran (ako postoji)
                        // startActivity(Intent(this, PocetnaActivity::class.java))
                        // finish()
                    } else {
                        Toast.makeText(this, "Greška: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Dugme za registraciju
        createAccountButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Reset lozinke
        resetPasswordButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Unesite email za reset lozinke", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Email za reset lozinke je poslan", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            this,
                            "Greška prilikom slanja emaila: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}
