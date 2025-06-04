package ba.sum.fpmoz.studytrack.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.R
import ba.sum.fpmoz.studytrack.model.Task

class TasksAdapter(
    private val tasks: List<Task>,
    private val onCheckChanged: (Task, Boolean) -> Unit,
    private val onEditClick: (Task) -> Unit,
    private val onDeleteClick: (Task) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.taskTitle)
        val dueDate: TextView = view.findViewById(R.id.taskDueDate) // dodano
        val checkBox: CheckBox = view.findViewById(R.id.taskCheckbox)
        val editButton: ImageButton = view.findViewById(R.id.editTaskButton)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteTaskButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.dueDate.text = "Rok: ${task.dueDate}" // dodano
        holder.checkBox.isChecked = task.completed
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckChanged(task, isChecked)
        }

        holder.editButton.setOnClickListener { onEditClick(task) }
        holder.deleteButton.setOnClickListener { onDeleteClick(task) }
    }

    override fun getItemCount() = tasks.size
}
