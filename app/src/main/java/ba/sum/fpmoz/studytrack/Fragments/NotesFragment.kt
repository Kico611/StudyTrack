package ba.sum.fpmoz.studytrack.Fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ba.sum.fpmoz.studytrack.Adapters.NotesAdapter
import ba.sum.fpmoz.studytrack.R
import ba.sum.fpmoz.studytrack.model.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class NotesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editTextNewNote: EditText
    private lateinit var buttonAddNote: Button
    private val notesList = mutableListOf<Note>()
    private lateinit var adapter: NotesAdapter
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        editTextNewNote = view.findViewById(R.id.editTextNewNote)
        buttonAddNote = view.findViewById(R.id.buttonAddNote)
        recyclerView = view.findViewById(R.id.recyclerViewNotes)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotesAdapter(notesList,
            onEditClick = { note -> showEditDialog(note) },
            onDeleteClick = { note -> deleteNote(note) }
        )
        recyclerView.adapter = adapter

        val currentUser = auth.currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Greška: korisnik nije prijavljen", Toast.LENGTH_SHORT).show()
            return view
        }

        loadNotes(userId)

        buttonAddNote.setOnClickListener {
            val noteText = editTextNewNote.text.toString().trim()
            if (noteText.isNotEmpty()) {
                val noteId = UUID.randomUUID().toString()
                val note = Note(id = noteId, text = noteText)

                db.collection("users").document(userId).collection("notes").document(noteId).set(note)
                    .addOnSuccessListener {
                        notesList.add(0, note)
                        adapter.notifyItemInserted(0)
                        editTextNewNote.text.clear()
                        recyclerView.scrollToPosition(0)
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Greška pri spremanju", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

    private fun deleteNote(note: Note) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("notes").document(note.id).delete()
            .addOnSuccessListener {
                val index = notesList.indexOfFirst { it.id == note.id }
                if (index != -1) {
                    notesList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "Bilješka obrisana", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri brisanju", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditDialog(note: Note) {
        val editText = EditText(requireContext()).apply {
            setText(note.text)
            setSelection(note.text.length)
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Uredi bilješku")
            .setView(editText)
            .setPositiveButton("Spremi") { dialog, _ ->
                val newText = editText.text.toString().trim()
                if (newText.isNotEmpty()) {
                    updateNoteText(note, newText)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Odustani") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateNoteText(note: Note, newText: String) {
        val userId = auth.currentUser?.uid ?: return
        val updatedNote = note.copy(text = newText)

        db.collection("users").document(userId).collection("notes").document(note.id).set(updatedNote)
            .addOnSuccessListener {
                val index = notesList.indexOfFirst { it.id == note.id }
                if (index != -1) {
                    notesList[index] = updatedNote
                    adapter.notifyItemChanged(index)
                    Toast.makeText(requireContext(), "Bilješka ažurirana", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri ažuriranju", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadNotes(userId: String) {
        db.collection("users").document(userId).collection("notes").get()
            .addOnSuccessListener { result ->
                notesList.clear()
                for (document in result) {
                    val note = document.toObject(Note::class.java)
                    notesList.add(note)
                }
                notesList.reverse()
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Greška pri učitavanju", Toast.LENGTH_SHORT).show()
            }
    }
}
