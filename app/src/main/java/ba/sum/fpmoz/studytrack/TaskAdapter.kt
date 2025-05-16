package ba.sum.fpmoz.studytrack

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.model.Task

class TaskAdapter(
    private val tasks: List<Task>,
    private val onEdit: (Task) -> Unit,
    private val onDelete: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.taskTitleTextView)
        val description = view.findViewById<TextView>(R.id.taskDescriptionTextView)
        val date = view.findViewById<TextView>(R.id.taskDueDateTextView)
        val edit = view.findViewById<Button>(R.id.editTaskButton)
        val delete = view.findViewById<Button>(R.id.deleteTaskButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.date.text = task.dueDate
        holder.description.text = task.description
        holder.edit.setOnClickListener { onEdit(task) }
        holder.delete.setOnClickListener { onDelete(task) }
    }

    override fun getItemCount() = tasks.size
}
