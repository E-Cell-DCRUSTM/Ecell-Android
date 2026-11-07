package dcrustm.ecell.mobile.ui.onboarding

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.auth.AuthError
import dcrustm.ecell.mobile.domain.auth.AuthSuccess
import dcrustm.ecell.mobile.domain.usecase.auth.GoogleLoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val googleLoginUseCase: GoogleLoginUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    // Firebase's signInWithCredential creates the account on first use, so a single
    // Google flow covers both sign-up and sign-in.
    fun continueWithGoogle(credentialManager: CredentialManager, context: Activity) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        val result = runCatching { googleLoginUseCase(credentialManager, context) }
            .getOrElse { AuthError(it.message ?: "Google sign-in failed. Please try again.") }
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
