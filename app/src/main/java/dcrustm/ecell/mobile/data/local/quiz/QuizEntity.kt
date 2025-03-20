package dcrustm.ecell.mobile.data.local.quiz

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

//@Entity(tableName = "quizzes")
//data class QuizEntity(
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0,
//    val topic: String,
//    val scheduleTime: String, // e.g. "2025-03-20 10:00"
//    val durationSeconds: Int
//)
//
//@Entity(
//    tableName = "questions",
//    foreignKeys = [
//        ForeignKey(
//            entity = QuizEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["quizId"],
//            onDelete = ForeignKey.CASCADE
//        )
//    ]
//)
//
//data class QuestionEntity(
//    @PrimaryKey(autoGenerate = true)
//    val id: Long = 0,
//    var quizId: Long,
//    val questionType: String, // "MCQ" or "SHORT"
//    val questionText: String,
//    // For MCQ:
//    val option1: String? = null,
//    val option2: String? = null,
//    val option3: String? = null,
//    val option4: String? = null,
//    val correctOption: Int? = null, // 1-based index (1 to 4)
//    // For short answer:
//    val answer: String? = null
//)