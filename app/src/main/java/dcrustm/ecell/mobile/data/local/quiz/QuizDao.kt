package dcrustm.ecell.mobile.data.local.quiz

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction

@Dao
interface QuizDao {

    @Insert
    suspend fun insertQuiz(quiz: QuizEntity): Long

    @Insert
    suspend fun insertQuestion(question: QuestionEntity): Long

    @Transaction
    suspend fun insertQuizWithQuestions(quiz: QuizEntity, questions: List<QuestionEntity>) {
        val quizId = insertQuiz(quiz)
        questions.forEach { question ->
            // update quizId for each question before inserting
            insertQuestion(question.copy(quizId = quizId))
        }
    }

}