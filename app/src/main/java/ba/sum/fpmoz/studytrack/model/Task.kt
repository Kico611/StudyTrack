package ba.sum.fpmoz.studytrack.model

data class Task(
    var id: String = "",
    var subjectId: String = "",
    var title: String = "",
    var completed: Boolean = false,
    var dueDate: String = "",
    var userId: String = ""
)
