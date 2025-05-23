package ba.sum.fpmoz.studytrack.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.R
import ba.sum.fpmoz.studytrack.model.Subject

class SubjectAdapter(
    private val subjects: List<Subject>,
    private val onEditClick: (Subject) -> Unit,
    private val onDeleteClick: (Subject) -> Unit,
    private val onItemClick: (Subject) -> Unit
) : RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>() {

    inner class SubjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.subjectNameTextView)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val progressPercentTextView: TextView = itemView.findViewById(R.id.progressPercentTextView)
        val editIcon: ImageView = itemView.findViewById(R.id.editIcon)
        val deleteIcon: ImageView = itemView.findViewById(R.id.deleteIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]

        holder.nameTextView.text = subject.name
        holder.progressBar.progress = subject.progress
        holder.progressPercentTextView.text = "${subject.progress}%"

        holder.editIcon.setOnClickListener { onEditClick(subject) }
        holder.deleteIcon.setOnClickListener { onDeleteClick(subject) }

        holder.itemView.setOnClickListener {
            onItemClick(subject)
        }
    }

    override fun getItemCount() = subjects.size
}