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
        db.collection("subjects")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                subjectList.clear()
                if (snapshot.isEmpty) {
                    return@addOnSuccessListener
                }

                val subjectsTemp = mutableListOf<Subject>()
                val tasksCollection = db.collection("tasks")

                var processedCount = 0
                for (doc in snapshot) {
                    val subjectId = doc.id
                    val name = doc["name"].toString()
                    val userId = doc["userId"].toString()

                    tasksCollection.whereEqualTo("subjectId", subjectId)
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
                            subjectsTemp.add(subject)

                            processedCount++
                            if (processedCount == snapshot.size()) {
                                subjectList.clear()
                                subjectList.addAll(subjectsTemp.sortedBy { it.name })
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }

    private fun showAddSubjectDialog() {
        val input = EditText(requireContext())
        input.hint = "Naziv predmeta"

        AlertDialog.Builder(requireContext())
            .setTitle("Dodaj predmet")
            .setView(input)
            .setPositiveButton("Spremi") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val newSubject = hashMapOf(
                        "name" to name,
                        "userId" to auth.currentUser?.uid
                    )
                    db.collection("subjects")
                        .add(newSubject)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Predmet dodan", Toast.LENGTH_SHORT).show()
                            loadSubjects()
                        }
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun showEditSubjectDialog(subject: Subject) {
        val input = EditText(requireContext())
        input.setText(subject.name)

        AlertDialog.Builder(requireContext())
            .setTitle("Uredi predmet")
            .setView(input)
            .setPositiveButton("Spremi") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotEmpty()) {
                    db.collection("subjects").document(subject.id)
                        .update("name", newName)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Predmet ažuriran", Toast.LENGTH_SHORT).show()
                            loadSubjects()
                        }
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun deleteSubject(subject: Subject) {
        AlertDialog.Builder(requireContext())
            .setTitle("Brisanje predmeta")
            .setMessage("Jeste li sigurni da želite obrisati '${subject.name}'?")
            .setPositiveButton("Da") { _, _ ->
                db.collection("subjects").document(subject.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Predmet obrisan", Toast.LENGTH_SHORT).show()
                        loadSubjects()
                    }
            }
            .setNegativeButton("Ne", null)
            .show()
    }
}