package ba.sum.fpmoz.studytrack.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ba.sum.fpmoz.studytrack.LoginActivity
import ba.sum.fpmoz.studytrack.R
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var registrationDateTextView: TextView
    private lateinit var changePasswordButton: Button
    private lateinit var logoutButton: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        profileImageView = view.findViewById(R.id.profileImageView)
        nameTextView = view.findViewById(R.id.nameTextView)
        usernameTextView = view.findViewById(R.id.usernameTextView)
        emailTextView = view.findViewById(R.id.emailTextView)
        registrationDateTextView = view.findViewById(R.id.registrationDateTextView)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)
        logoutButton = view.findViewById(R.id.logoutButton)

        loadUserData()

        changePasswordButton.setOnClickListener {
            sendPasswordResetEmail()
        }

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Odjavljeni ste", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserData() {
        val userId = auth.currentUser?.uid
        val userEmail = auth.currentUser?.email

        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val firstName = document.getString("firstName") ?: ""
                        val lastName = document.getString("lastName") ?: ""
                        val fullName = "$firstName $lastName"

                        nameTextView.text = fullName
                        usernameTextView.text = "@${firstName.lowercase()}.${lastName.lowercase()}"
                        emailTextView.text = document.getString("email") ?: userEmail ?: "Email"

                        // Ako imaš datum registracije (npr. kao timestamp), konvertiraj ga u string
                        val registrationTimestamp = document.getTimestamp("registrationDate")
                        registrationDateTextView.text = if (registrationTimestamp != null) {
                            val date = registrationTimestamp.toDate()
                            android.text.format.DateFormat.format("dd.MM.yyyy", date).toString()
                        } else {
                            "Datum registracije nije dostupan"
                        }

                        // Profilna slika (ako postoji)
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .into(profileImageView)
                        } else {
                            profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        }
                    } else {
                        Toast.makeText(requireContext(), "Korisnički dokument ne postoji", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri dohvaćanju podataka", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun sendPasswordResetEmail() {
        val email = auth.currentUser?.email
        if (!email.isNullOrEmpty()) {
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Email za promjenu lozinke poslan.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Greška pri slanju emaila.", Toast.LENGTH_SHORT).show()
                }
        }
    }
}