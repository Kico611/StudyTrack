package ba.sum.fpmoz.studytrack.Fragments

import ba.sum.fpmoz.studytrack.Adapters.SubjectAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.R
import ba.sum.fpmoz.studytrack.model.Subject
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SubjectsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SubjectAdapter
    private val subjectList = mutableListOf<Subject>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.subjectsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SubjectAdapter(
            subjects = subjectList,
            onEditClick = { subject -> showEditSubjectDialog(subject) },
            onDeleteClick = { subject -> deleteSubject(subject) },
            onItemClick = { subject ->
                val action = SubjectsFragmentDirections
                    .actionSubjectsFragmentToTaskFragment(subject.id)
                findNavController().navigate(action)
            }
        )

        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.addSubjectButton).setOnClickListener {
            showAddSubjectDialog()
        }

        loadSubjects()
    }

    private fun loadSubjects() {
        val currentUser = auth.currentUser ?: return
        subjectList.clear()

        val tasksCollection = db.collection("tasks")
        db.collection("subjects")
            .whereEqualTo("userId", currentUser.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val tempSubjects = mutableListOf<Subject>()
                val totalSubjects = snapshot.size()
                var processed = 0

                for (doc in snapshot) {
                    val subjectId = doc.id
                    val name = doc.getString("name") ?: ""
                    val userId = doc.getString("userId") ?: ""

                    tasksCollection.whereEqualTo("subjectId", subjectId)
                        .whereEqualTo("userId", currentUser.uid)
                        .get()
                        .addOnSuccessListener { tasksSnapshot ->
                            val totalTasks = tasksSnapshot.size()
                            val completedTasks = tasksSnapshot.count { it.getBoolean("completed") == true }
                            val progress = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

                            val subject = Subject(
                                id = subjectId,
                                name = name,
                                userId = userId,
                                progress = progress
                            )
                            tempSubjects.add(subject)
                            processed++

                            if (processed == totalSubjects) {
                                subjectList.clear()
                                subjectList.addAll(tempSubjects.sortedBy { it.name })
                                adapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener {
                            processed++
                            if (processed == totalSubjects) {
                                subjectList.clear()
                                subjectList.addAll(tempSubjects.sortedBy { it.name })
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }

    private fun showAddSubjectDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_subject, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.subjectNameEditText)
        val notesInput = dialogView.findViewById<EditText>(R.id.subjectNotesEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Dodaj kolegij")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val name = nameInput.text.toString().trim()
                val notes = notesInput.text.toString().trim()

                if (name.isNotEmpty()) {
                    val newSubject = hashMapOf(
                        "name" to name,
                        "notes" to notes,
                        "userId" to auth.currentUser?.uid
                    )
                    db.collection("subjects")
                        .add(newSubject)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Kolegij dodan", Toast.LENGTH_SHORT).show()
                            loadSubjects()
                        }
                } else {
                    Toast.makeText(context, "Naziv kolegija je obavezan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun showEditSubjectDialog(subject: Subject) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_subject, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.subjectNameEditText)
        val notesInput = dialogView.findViewById<EditText>(R.id.subjectNotesEditText)

        nameInput.setText(subject.name)

        // Ako već koristiš "notes" u Subject modelu, ovdje ga popuni
        // notesInput.setText(subject.notes)

        AlertDialog.Builder(requireContext())
            .setTitle("Uredi kolegij")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val newName = nameInput.text.toString().trim()
                val newNotes = notesInput.text.toString().trim()

                if (newName.isNotEmpty()) {
                    db.collection("subjects").document(subject.id)
                        .update(mapOf(
                            "name" to newName,
                            "notes" to newNotes
                        ))
                        .addOnSuccessListener {
                            Toast.makeText(context, "Kolegij ažuriran", Toast.LENGTH_SHORT).show()
                            loadSubjects()
                        }
                } else {
                    Toast.makeText(context, "Naziv kolegija je obavezan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun deleteSubject(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Brisanje kolegija")
            .setMessage("Jeste li sigurni da želite obrisati '${subject.name}'?")
            .setPositiveButton("Da") { _, _ ->
                db.collection("subjects").document(subject.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Kolegij obrisan", Toast.LENGTH_SHORT).show()
                        loadSubjects()
                    }
            }
            .setNegativeButton("Ne", null)
            .show()
    }
}
