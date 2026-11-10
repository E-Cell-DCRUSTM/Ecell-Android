package dcrustm.ecell.mobile.ui.onboarding

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.auth.FirebaseAuthRepository
import dcrustm.ecell.mobile.domain.usecase.auth.GoogleLoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val firebaseAuthRepository: FirebaseAuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun continueWithGoogle(credentialManager: CredentialManager, context: Activity) = viewModelScope.launch {
        if (_authState.value == AuthState.Loading) return@launch
        _authState.value = AuthState.Loading
        val result = runCatching { googleLoginUseCase(credentialManager, context) }
            .getOrElse { AuthError(it.message ?: "Google sign-in failed. Please try again.") }
        when (result) {
            is AuthSuccess -> _authState.value = AuthState.Authenticated(result)
            is AuthError -> _authState.value = AuthState.Error(result.errorMessage)
        }
    }

    fun signInWithEmail(email: String, pass: String) = viewModelScope.launch {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthState.Error("Please enter both email and password")
            return@launch
        }
        if (_authState.value == AuthState.Loading) return@launch
        _authState.value = AuthState.Loading
        val result = runCatching { firebaseAuthRepository.signInWithEmail(email, pass) }
            .getOrElse { AuthError(it.message ?: "Sign-in failed") }
        when (result) {
            is AuthSuccess -> _authState.value = AuthState.Authenticated(result)
            is AuthError -> _authState.value = AuthState.Error(result.errorMessage)
        }
    }

}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val authData: AuthSuccess) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
