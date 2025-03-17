package dcrustm.ecell.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.credentials.CredentialManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import dcrustm.ecell.mobile.domain.usecase.CheckOnBoardingCompletedUseCase
import dcrustm.ecell.mobile.domain.usecase.SetOnBoardingCompleteUseCase
import dcrustm.ecell.mobile.navigation.RootApp
import dcrustm.ecell.mobile.ui.meeting.MeetingScreen
import dcrustm.ecell.mobile.ui.quiz.QuestionAdditionScreen
import dcrustm.ecell.mobile.ui.quiz.QuizSetupScreen
import dcrustm.ecell.mobile.ui.theme.AppTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val credentialManager = CredentialManager.create(this)

    @Inject
    lateinit var checkOnBoardingCompletedUseCase: CheckOnBoardingCompletedUseCase

    @Inject
    lateinit var setOnBoardingCompleteUseCase: SetOnBoardingCompleteUseCase


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                MeetingScreen()
            }
        }
    }
}


// TODO: Stable the code for quiz setup screen.

//val navController = rememberNavController()
//NavHost(navController = navController, startDestination = "quiz_setup") {
//    composable("quiz_setup") {
//        QuizSetupScreen(onNext = { navController.navigate("question_addition") })
//    }
//    composable("question_addition") {
//        QuestionAdditionScreen(onFinish = {
//            // Handle finish quiz or navigate to the next screen
//        })
//    }
//}
