package ba.sum.fpmoz.studytrack.Fragments

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.Adapters.TasksAdapter
import ba.sum.fpmoz.studytrack.model.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.navArgs
import ba.sum.fpmoz.studytrack.R
import java.util.Calendar

class TasksFragment : Fragment() {

    private val args: TasksFragmentArgs by navArgs()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TasksAdapter
    private val taskList = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_task, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.tasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = TasksAdapter(
            taskList,
            onCheckChanged = { task, isChecked -> toggleTaskCompletion(task, isChecked) },
            onEditClick = { task -> showEditTaskDialog(task) },
            onDeleteClick = { task -> deleteTask(task) }
        )
        recyclerView.adapter = adapter

        view.findViewById<View>(R.id.addTaskButton).setOnClickListener {
            showAddTaskDialog()
        }

        loadTasks()
    }

    private fun loadTasks() {
        db.collection("tasks")
            .whereEqualTo("subjectId", args.subjectId)
            .get()
            .addOnSuccessListener { snapshot ->
                taskList.clear()
                for (doc in snapshot) {
                    val task = Task(
                        id = doc.id,
                        subjectId = doc.getString("subjectId") ?: "",
                        title = doc.getString("title") ?: "",
                        completed = doc.getBoolean("completed") ?: false,
                        dueDate = doc.getString("dueDate") ?: ""
                    )
                    taskList.add(task)
                }
                adapter.notifyDataSetChanged()
                updateSubjectProgress()
            }
    }

    private fun updateSubjectProgress() {
        val total = taskList.size
        val completed = taskList.count { it.completed }
        val progress = if (total > 0) (completed * 100) / total else 0

        db.collection("subjects")
            .document(args.subjectId)
            .update("progress", progress)
    }

    private fun showAddTaskDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_task, null)
        val titleEditText = dialogView.findViewById<EditText>(R.id.taskTitleEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.taskDescriptionEditText)
        val dueDateEditText = dialogView.findViewById<EditText>(R.id.taskDueDate)

        // Otvori DatePicker kada korisnik klikne na dueDateEditText
        dueDateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = String.format("%02d.%02d.%d", selectedDay, selectedMonth + 1, selectedYear)
                dueDateEditText.setText(selectedDate)
            }, year, month, day)

            datePickerDialog.show()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Dodaj zadatak")
            .setView(dialogView)
            .setPositiveButton("Spremi") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val dueDate = dueDateEditText.text.toString().trim()

                if (title.isNotEmpty()) {
                    val task = hashMapOf(
                        "title" to title,
                        "description" to description,
                        "dueDate" to dueDate,
                        "completed" to false,
                        "subjectId" to args.subjectId
                    )
                    db.collection("tasks").add(task).addOnSuccessListener {
                        loadTasks()
                    }
                } else {
                    Toast.makeText(requireContext(), "Naslov ne smije biti prazan", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }


    private fun showEditTaskDialog(task: Task) {
        val input = EditText(requireContext())
        input.setText(task.title)

        AlertDialog.Builder(requireContext())
            .setTitle("Uredi zadatak")
            .setView(input)
            .setPositiveButton("Spremi") { _, _ ->
                val newTitle = input.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    db.collection("tasks")
                        .document(task.id)
                        .update("title", newTitle)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Zadatak ažuriran", Toast.LENGTH_SHORT).show()
                            loadTasks()
                        }
                }
            }
            .setNegativeButton("Odustani", null)
            .show()
    }

    private fun deleteTask(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle("Brisanje zadatka")
            .setMessage("Jeste li sigurni da želite obrisati '${task.title}'?")
            .setPositiveButton("Da") { _, _ ->
                db.collection("tasks")
                    .document(task.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Zadatak obrisan", Toast.LENGTH_SHORT).show()
                        loadTasks()
                    }
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun toggleTaskCompletion(task: Task, isChecked: Boolean) {
        db.collection("tasks")
            .document(task.id)
            .update("completed", isChecked)
            .addOnSuccessListener {
                task.completed = isChecked
                updateSubjectProgress()
            }
    }
}
