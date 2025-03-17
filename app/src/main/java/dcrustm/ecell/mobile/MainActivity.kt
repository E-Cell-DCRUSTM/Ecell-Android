package dcrustm.ecell.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.credentials.CredentialManager
import dagger.hilt.android.AndroidEntryPoint
import dcrustm.ecell.mobile.domain.usecase.CheckOnBoardingCompletedUseCase
import dcrustm.ecell.mobile.domain.usecase.SetOnBoardingCompleteUseCase
import dcrustm.ecell.mobile.navigation.MainScreen
import dcrustm.ecell.mobile.navigation.OnBoardingNavigation
import dcrustm.ecell.mobile.ui.about.AboutUsScreen
import dcrustm.ecell.mobile.ui.admin.AdminScreen
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
//                OnBoardingNavigation(
//                    credentialManager = credentialManager
//                )
            //                ()
                MainScreen()
            }
        }
    }
}