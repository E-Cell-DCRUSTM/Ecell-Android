package dcrustm.ecell.mobile.domain.usecase

import dcrustm.ecell.mobile.data.local.quiz.QuestionEntity
import dcrustm.ecell.mobile.data.local.quiz.QuizEntity
import dcrustm.ecell.mobile.data.local.quiz.QuizRepository
import javax.inject.Inject

class CreateQuizUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(quiz: QuizEntity, questions: List<QuestionEntity>) {
        repository.createQuiz(quiz, questions)
    }
}