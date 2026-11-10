package dcrustm.ecell.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.credentials.CredentialManager
import dagger.hilt.android.AndroidEntryPoint
import dcrustm.ecell.mobile.domain.usecase.CheckOnBoardingCompletedUseCase
import dcrustm.ecell.mobile.domain.usecase.SetOnBoardingCompleteUseCase
import dcrustm.ecell.mobile.navigation.OnBoardingNavigation
import dcrustm.ecell.mobile.navigation.OnBoardingRoutes
import dcrustm.ecell.mobile.ui.theme.AppTheme
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val credentialManager by lazy { CredentialManager.create(this) }

    @Inject
    lateinit var checkOnBoardingCompletedUseCase: CheckOnBoardingCompletedUseCase

    @Inject
    lateinit var setOnBoardingCompleteUseCase: SetOnBoardingCompleteUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            AppTheme {
                val isOnBoardingCompleted by produceState<Boolean?>(initialValue = null) {
                    value = checkOnBoardingCompletedUseCase().first()
                }

                if (isOnBoardingCompleted == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    OnBoardingNavigation(
                        credentialManager = credentialManager,
                        setOnBoardingCompleteUseCase = setOnBoardingCompleteUseCase,
                        startDestination = if (isOnBoardingCompleted == true) OnBoardingRoutes.RootAppRoute
                        else OnBoardingRoutes.WelcomeAppRoute
                    )
                }
            }
        }
    }
}

