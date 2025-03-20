package dcrustm.ecell.mobile.ui.onboarding

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.usecase.auth.EmailLoginUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.EmailSignUpUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.GetGoogleIdUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.GoogleLoginUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.GoogleSignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val getGoogleIdUseCase: GetGoogleIdUseCase,
    private val emailSignUpUseCase: EmailSignUpUseCase,
    private val emailLoginUseCase: EmailLoginUseCase,
    private val googleSignUpUseCase: GoogleSignUpUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    // Function to signup with google all in one and return the access token.
    fun signUpWithGoogle(credentialManager: CredentialManager, context: Activity) = viewModelScope.launch {
        googleSignUpUseCase(credentialManager, context)
    }

    // Function to login with google all in one and return the access token.
    fun loginWithGoogle(credentialManager: CredentialManager, context: Activity) = viewModelScope.launch {
        googleLoginUseCase(credentialManager, context)
    }

    // Function to signup with email all in one and return the access token.
    fun signUpWithEmail(user: User) = viewModelScope.launch {
        emailSignUpUseCase(user)
    }

    // Function to login with Email Id all in one and return the access token.
    fun loginWithEmail(email: String, password: String) = viewModelScope.launch {
        emailLoginUseCase(email, password)
    }

    // Function to fetch google details, left for debugging.
    fun getGoogleIdDetails(credentialManager: CredentialManager, context: Activity) = viewModelScope.launch{
        getGoogleIdUseCase(credentialManager = credentialManager,context = context)
    }

}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
