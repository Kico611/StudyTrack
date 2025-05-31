package ba.sum.fpmoz.studytrack.Fragments
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import ba.sum.fpmoz.studytrack.R
import android.widget.TextView


class CalendarFragment : Fragment() {

    private lateinit var compactCalendarView: CompactCalendarView
    private val db = FirebaseFirestore.getInstance()
    private lateinit var monthYearTextView: TextView

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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

        // Prikaži početni mjesec i godinu
        updateMonthYearTextView(monthYearTextView, compactCalendarView.firstDayOfCurrentMonth)

        compactCalendarView.setListener(object : CompactCalendarView.CompactCalendarViewListener {
            override fun onDayClick(dateClicked: Date) {
                val events = compactCalendarView.getEvents(dateClicked)
                if (events.isNotEmpty()) {
                    val sb = StringBuilder()
                    for (event in events) {
                        sb.append(event.data.toString()).append("\n\n")
                    }
                    // Prikaz u AlertDialogu
                    AlertDialog.Builder(requireContext())
                        .setTitle("Događaji za ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(dateClicked)}")
                        .setMessage(sb.toString().trim())
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


        loadExamDates()
        loadTaskDueDates()
    }

    private fun updateMonthYearTextView(textView: TextView, date: Date) {
        val monthYearFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        val formatted = monthYearFormat.format(date).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        textView.text = formatted
    }

    private fun loadExamDates() {
        db.collection("exams")
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
                            val eventColor = Color.RED
                            val eventData = "Ispit - Predmet: $subject\nBilješke: $note"
                            val event = Event(eventColor, it.time, eventData)
                            compactCalendarView.addEvent(event)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Error loading exams", exception)
            }
    }

    private fun loadTaskDueDates() {
        db.collection("tasks")
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
                            val eventColor = Color.BLUE
                            val status = if (completed) "Dovršeno" else "Nije dovršeno"
                            val eventData = "Zadatak: $title\nOpis: $description\nStatus: $status"
                            val event = Event(eventColor, it.time, eventData)
                            compactCalendarView.addEvent(event)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("CalendarFragment", "Error loading tasks", exception)
            }
    }
}