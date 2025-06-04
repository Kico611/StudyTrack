package ba.sum.fpmoz.studytrack.Fragments

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import ba.sum.fpmoz.studytrack.R
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {

    private lateinit var compactCalendarView: CompactCalendarView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var monthYearTextView: TextView
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        monthYearTextView = view.findViewById(R.id.monthYearTextView)
        compactCalendarView = view.findViewById(R.id.compactCalendarView)

        updateMonthYearTextView(monthYearTextView, compactCalendarView.firstDayOfCurrentMonth)

        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                val events = compactCalendarView.getEvents(dateClicked)
                if (events.isNotEmpty()) {
                    val sb = StringBuilder()
                    for ((index, event) in events.withIndex()) {
                        sb.append("• ").append(event.data.toString().trim())
                        if (index < events.size - 1) sb.append("\n\n")
                    }
                    AlertDialog.Builder(requireContext())
                        .setTitle("Događaji za ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateClicked)}")
                        .setMessage(sb.toString())
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Nema događaja za ovaj datum", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onMonthScroll(firstDayOfNewMonth: Date) {
                updateMonthYearTextView(monthYearTextView, firstDayOfNewMonth)
            }
        })

        if (currentUserId != null) {
            loadExamDates(currentUserId!!)
            loadTaskDueDates(currentUserId!!)
        } else {
            Toast.makeText(requireContext(), "Korisnik nije prijavljen", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMonthYearTextView(textView: TextView, date: Date) {
        val monthYearFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        val formatted = monthYearFormat.format(date).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        textView.text = formatted
    }

    private fun loadExamDates(userId: String) {
        db.collection("exams")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val dateStr = doc.getString("date")
                    val subject = doc.getString("subject") ?: "Nepoznati predmet"
                    val note = doc.getString("note") ?: ""
                    if (dateStr != null) {
                        val date = try {
                            dateFormat.parse(dateStr)
                        } catch (e: Exception) {
                            null
                        }
                        date?.let {
                            val event = Event(Color.RED, it.time, "Ispit - Predmet: $subject\nBilješke: $note")
                            compactCalendarView.addEvent(event)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Greška pri učitavanju ispita", exception)
            }
    }

    private fun loadTaskDueDates(userId: String) {
        db.collection("tasks")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (doc in documents) {
                    val dueDateStr = doc.getString("dueDate")
                    val title = doc.getString("title") ?: "Nema naslova"
                    val description = doc.getString("description") ?: ""
                    val completed = doc.getBoolean("completed") ?: false

                    if (dueDateStr != null) {
                        val date = try {
                            dateFormat.parse(dueDateStr)
                        } catch (e: Exception) {
                            null
                        }
                        date?.let {
                            val status = if (completed) "Dovršeno" else "Nije dovršeno"
                            val event = Event(Color.BLUE, it.time, "Zadatak: $title\nOpis: $description\nStatus: $status")
                            compactCalendarView.addEvent(event)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Greška pri učitavanju zadataka", exception)
            }
    }
}
