package ba.sum.fpmoz.studytrack.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.R
import ba.sum.fpmoz.studytrack.model.Exam

class ExamAdapter(
    private val exams: List<Exam>,
    private val onEdit: (Exam) -> Unit,
    private val onDelete: (Exam) -> Unit
) : RecyclerView.Adapter<ExamAdapter.ExamViewHolder>() {

    class ExamViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val subjectText: TextView = itemView.findViewById(R.id.examSubjectTextView)
        val dateText: TextView = itemView.findViewById(R.id.examDateTextView)
        val notesText: TextView = itemView.findViewById(R.id.examNotesTextView)
        val editButton: ImageButton = itemView.findViewById(R.id.editExamButton)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteExamButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExamViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.exam_item, parent, false)
        return ExamViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExamViewHolder, position: Int) {
        val exam = exams[position]
        holder.subjectText.text = exam.subject
        holder.dateText.text = exam.date
        holder.notesText.text = exam.notes

        holder.editButton.setOnClickListener { onEdit(exam) }
        holder.deleteButton.setOnClickListener { onDelete(exam) }
    }

    override fun getItemCount(): Int = exams.size
}