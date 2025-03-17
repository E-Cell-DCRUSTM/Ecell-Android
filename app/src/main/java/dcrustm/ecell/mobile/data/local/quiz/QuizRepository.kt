package dcrustm.ecell.mobile.data.local.quiz

import javax.inject.Inject
import javax.inject.Singleton

interface QuizRepository {
    suspend fun createQuiz(quiz: QuizEntity, questions: List<QuestionEntity>)
}

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizDao: QuizDao
) : QuizRepository {
    override suspend fun createQuiz(quiz: QuizEntity, questions: List<QuestionEntity>) {
        quizDao.insertQuizWithQuestions(quiz, questions)
    }
}