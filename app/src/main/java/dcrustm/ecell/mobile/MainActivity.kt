package dcrustm.ecell.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import dcrustm.ecell.mobile.domain.usecase.CheckOnBoardingCompletedUseCase
import dcrustm.ecell.mobile.domain.usecase.SetOnBoardingCompleteUseCase
import dcrustm.ecell.mobile.ui.theme.AppTheme
import dcrustm.ecell.mobile.ui.welcome.WelcomeScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var checkOnBoardingCompletedUseCase: CheckOnBoardingCompletedUseCase

    @Inject
    lateinit var setOnBoardingCompleteUseCase: SetOnBoardingCompleteUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                WelcomeScreen()
            }
        }
    }
}