package ba.sum.fpmoz.studytrack

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ba.sum.fpmoz.studytrack.model.Task



class TasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.tasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TaskAdapter(tasks,
            onEdit = { task -> showEditDialog(task) },
            onDelete = { task -> deleteTask(task) }
        )
        recyclerView.adapter = adapter

        val addTaskButton: View = view.findViewById(R.id.addTaskButton)
        addTaskButton.setOnClickListener {
            showAddDialog()
        }


        loadTasks()
    }

    private fun loadTasks() {
        db.collection("tasks")
            .whereEqualTo("userId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { snapshot ->
                tasks.clear()
                for (doc in snapshot) {
                    val task = Task(
                        id = doc.id,
                        title = doc["title"].toString(),
                        description = doc["description"].toString(),
                        dueDate = doc["dueDate"].toString()
                    )
                    tasks.add(task)
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun deleteTask(task: Task) {
        db.collection("tasks").document(task.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Obrisano", Toast.LENGTH_SHORT).show()
                loadTasks()
            }
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.taskTitleEditText)
        val descriptionInput = dialogView.findViewById<android.widget.EditText>(R.id.taskDescriptionEditText)
        val dueDateInput = dialogView.findViewById<android.widget.EditText>(R.id.taskDueDateEditText)

        // Onemogućavanje ručnog unosa i postavljanje DatePicker-a
        dueDateInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val calendar = java.util.Calendar.getInstance()
                val year = calendar.get(java.util.Calendar.YEAR)
                val month = calendar.get(java.util.Calendar.MONTH)
                val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)

                val datePicker = android.app.DatePickerDialog(
                    requireContext(),
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
                        dueDateInput.setText(formattedDate)
                    },
                    year, month, day
                )
                datePicker.show()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Novi zadatak")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val dueDate = dueDateInput.text.toString().trim()

                if (title.isNotEmpty() && dueDate.isNotEmpty()) {
                    val newTask = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "dueDate" to dueDate,
                        "userId" to auth.currentUser?.uid
                    )
                    db.collection("tasks")
                        .add(newTask)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Zadatak dodan", Toast.LENGTH_SHORT).show()
                            loadTasks()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Greška pri dodavanju", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Unesite barem naslov i rok", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .create()

        dialog.show()
    }

    private fun showEditDialog(task: Task) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)

        val titleInput = dialogView.findViewById<android.widget.EditText>(R.id.taskTitleEditText)
        val descriptionInput = dialogView.findViewById<android.widget.EditText>(R.id.taskDescriptionEditText)
        val dueDateInput = dialogView.findViewById<android.widget.EditText>(R.id.taskDueDateEditText)

        // Postavi postojeće vrijednosti
        titleInput.setText(task.title)
        descriptionInput.setText(task.description)
        dueDateInput.setText(task.dueDate)

        // Onemogućavanje ručnog unosa i postavljanje DatePicker-a
        dueDateInput.apply {
            isFocusable = false
            isClickable = true
            setOnClickListener {
                val parts = task.dueDate.split(".")
                val day = parts.getOrNull(0)?.toIntOrNull() ?: 1
                val month = (parts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
                val year = parts.getOrNull(2)?.toIntOrNull() ?: 2025

                val datePicker = android.app.DatePickerDialog(
                    requireContext(),
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val formattedDate = String.format("%02d.%02d.%04d", selectedDay, selectedMonth + 1, selectedYear)
                        dueDateInput.setText(formattedDate)
                    },
                    year, month, day
                )
                datePicker.show()
            }
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Uredi zadatak")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val updatedTitle = titleInput.text.toString().trim()
                val updatedDescription = descriptionInput.text.toString().trim()
                val updatedDueDate = dueDateInput.text.toString().trim()

                if (updatedTitle.isNotEmpty() && updatedDueDate.isNotEmpty()) {
                    val updatedTask = hashMapOf(
                        "title" to updatedTitle,
                        "description" to updatedDescription,
                        "dueDate" to updatedDueDate
                    )
                    db.collection("tasks").document(task.id)
                        .update(updatedTask as Map<String, Any>)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Zadatak ažuriran", Toast.LENGTH_SHORT).show()
                            loadTasks()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Greška pri ažuriranju", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Naslov i rok su obavezni", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .create()

        dialog.show()
    }

}