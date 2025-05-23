package ba.sum.fpmoz.studytrack.Adapters

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.model.Exam
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ba.sum.fpmoz.studytrack.R

class ExamsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExamAdapter
    private val exams = mutableListOf<Exam>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_exams, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.examsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ExamAdapter(exams,
            onEdit = { exam -> showEditDialog(exam) },
            onDelete = { exam -> deleteExam(exam) }
        )
        recyclerView.adapter = adapter

        val addExamButton: FloatingActionButton = view.findViewById(R.id.addExamButton)
        addExamButton.setOnClickListener { showAddDialog() }

        loadExams()
    }

    private fun loadExams() {
        db.collection("exams")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                exams.clear()
                for (doc in snapshot) {
                    exams.add(
                        Exam(
                            id = doc.id,
                            subject = doc["subject"].toString(),
                            date = doc["date"].toString(),
                            notes = doc["notes"].toString()
                        )
                    )
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun deleteExam(exam: Exam) {
        db.collection("exams").document(exam.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Ispit obrisan", Toast.LENGTH_SHORT).show()
                loadExams()
            }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exam, null)
        val subjectInput = dialogView.findViewById<EditText>(R.id.examSubjectEditText)
        val dateInput = dialogView.findViewById<EditText>(R.id.examDateEditText)
        val notesInput = dialogView.findViewById<EditText>(R.id.examNotesEditText)

        dateInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                DatePickerDialog(requireContext(), { _, y, m, d ->
                    dateInput.setText(String.format("%02d.%02d.%04d", d, m + 1, y))
                }, year, month, day).show()
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Novi ispit")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val subject = subjectInput.text.toString().trim()
                val date = dateInput.text.toString().trim()
                val notes = notesInput.text.toString().trim()

                if (subject.isNotEmpty() && date.isNotEmpty()) {
                    val newExam = hashMapOf(
                        "subject" to subject,
                        "date" to date,
                        "notes" to notes,
                        "userId" to auth.currentUser?.uid
                    )
                    db.collection("exams").add(newExam)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Ispit dodan", Toast.LENGTH_SHORT).show()
                            loadExams()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Greška pri dodavanju", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Unesite predmet i datum", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun showEditDialog(exam: Exam) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_exam, null)
        val subjectInput = dialogView.findViewById<EditText>(R.id.examSubjectEditText)
        val dateInput = dialogView.findViewById<EditText>(R.id.examDateEditText)
        val notesInput = dialogView.findViewById<EditText>(R.id.examNotesEditText)

        subjectInput.setText(exam.subject)
        dateInput.setText(exam.date)
        notesInput.setText(exam.notes)

        dateInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val parts = exam.date.split(".")
                val day = parts.getOrNull(0)?.toIntOrNull() ?: 1
                val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                val year = parts.getOrNull(2)?.toIntOrNull() ?: 2025

                DatePickerDialog(requireContext(), { _, y, m, d ->
                    dateInput.setText(String.format("%02d.%02d.%04d", d, m + 1, y))
                }, year, month, day).show()
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Uredi ispit")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val updatedSubject = subjectInput.text.toString().trim()
                val updatedDate = dateInput.text.toString().trim()
                val updatedNotes = notesInput.text.toString().trim()

                if (updatedSubject.isNotEmpty() && updatedDate.isNotEmpty()) {
                    val updatedExam = hashMapOf(
                        "subject" to updatedSubject,
                        "date" to updatedDate,
                        "notes" to updatedNotes
                    )
                    db.collection("exams").document(exam.id)
                        .update(updatedExam as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Ispit ažuriran", Toast.LENGTH_SHORT).show()
                            loadExams()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Greška pri ažuriranju", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Unesite predmet i datum", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }
}
