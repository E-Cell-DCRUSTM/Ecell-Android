package dcrustm.ecell.mobile.ui.quiz

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.data.local.quiz.QuestionEntity
import dcrustm.ecell.mobile.data.local.quiz.QuizEntity
import dcrustm.ecell.mobile.domain.usecase.CreateQuizUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val createQuizUseCase: CreateQuizUseCase
) : ViewModel() {

    // Quiz Details (setup screen)
    var topic by mutableStateOf("")
    var scheduleTime by mutableStateOf("")
    var durationSeconds by mutableStateOf("")  // as string input
    var durationError by mutableStateOf("")

    // List to store questions created sequentially
    var questions = mutableStateListOf<QuestionInput>()

    // Current question data (for sequential addition)
    var currentQuestionText by mutableStateOf("")
    var currentQuestionType by mutableStateOf("MCQ")  // Either "MCQ" or "SHORT"

    // For MCQ questions:
    var option1 by mutableStateOf("")
    var option2 by mutableStateOf("")
    var option3 by mutableStateOf("")
    var option4 by mutableStateOf("")
    var correctOptionIndex by mutableStateOf(-1)  // 0 to 3, -1 means not selected

    // For short answer questions:
    var shortAnswer by mutableStateOf("")

    // Error message for current question input
    var questionError by mutableStateOf("")

    // Temporary data holder for a question before converting to QuestionEntity.
    data class QuestionInput(
        val questionType: String,
        val questionText: String,
        // For MCQ:
        val option1: String? = null,
        val option2: String? = null,
        val option3: String? = null,
        val option4: String? = null,
        val correctOption: Int? = null,
        // For short answer:
        val answer: String? = null
    )

    // Called when the admin clicks “Add Question.”
    fun addCurrentQuestion(): Boolean {
        if (currentQuestionText.isBlank()) {
            questionError = "Question text cannot be empty"
            return false
        }
        if (currentQuestionType == "MCQ") {
            if (option1.isBlank() || option2.isBlank() ||
                option3.isBlank() || option4.isBlank()
            ) {
                questionError = "Please provide all 4 options"
                return false
            }
            if (correctOptionIndex !in 0..3) {
                questionError = "Please select the correct option"
                return false
            }
            val question = QuestionInput(
                questionType = "MCQ",
                questionText = currentQuestionText,
                option1 = option1,
                option2 = option2,
                option3 = option3,
                option4 = option4,
                correctOption = correctOptionIndex + 1 // convert to 1-based index
            )
            questions.add(question)
        } else {
            // For short answer, validate that the answer is a single word.
            if (shortAnswer.isBlank()) {
                questionError = "Answer cannot be empty"
                return false
            }
            if (shortAnswer.trim().split(" ").size > 1) {
                questionError = "Short answer must be a single word"
                return false
            }
            val question = QuestionInput(
                questionType = "SHORT",
                questionText = currentQuestionText,
                answer = shortAnswer.trim()
            )
            questions.add(question)
        }
        // Clear current question fields
        currentQuestionText = ""
        option1 = ""
        option2 = ""
        option3 = ""
        option4 = ""
        correctOptionIndex = -1
        shortAnswer = ""
        questionError = ""
        return true
    }

    // Called when the admin clicks “Finish Quiz.”
    fun saveQuiz(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (topic.isBlank()) {
            onError("Quiz topic is required")
            return
        }
        if (scheduleTime.isBlank()) {
            onError("Schedule time is required")
            return
        }
        val durationInt = durationSeconds.toIntOrNull() ?: 0
        if (durationInt < 30 || durationInt > 120) {
            onError("Duration must be between 30 and 120 seconds")
            return
        }
        if (questions.isEmpty()) {
            onError("Please add at least one question")
            return
        }

        val quizEntity = QuizEntity(
            topic = topic,
            scheduleTime = scheduleTime,
            durationSeconds = durationInt
        )

        // Convert temporary question inputs to QuestionEntity list.
        val questionEntities: List<QuestionEntity> = questions.map { input ->
            QuestionEntity(
                quizId = 0, // temporary; the DAO will replace this after inserting the quiz.
                questionType = input.questionType,
                questionText = input.questionText,
                option1 = input.option1,
                option2 = input.option2,
                option3 = input.option3,
                option4 = input.option4,
                correctOption = input.correctOption,
                answer = input.answer
            )
        }

        viewModelScope.launch {
            try {
                createQuizUseCase(quizEntity, questionEntities)
                onSuccess()
            } catch (e: Exception) {
                onError("Error saving quiz: ${e.localizedMessage}")
            }
        }
    }

}

