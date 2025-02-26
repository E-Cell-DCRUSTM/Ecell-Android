package dcrustm.ecell.mobile.ui

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dcrustm.ecell.mobile.domain.model.User
import dcrustm.ecell.mobile.domain.usecase.auth.GetCurrentUserUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.IsUserAuthenticatedUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.SignInWithGoogleUseCase
import dcrustm.ecell.mobile.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        val isAuthenticated = isUserAuthenticatedUseCase()
        if (isAuthenticated) {
            val user = getCurrentUserUseCase()
            user?.let {
                println("The user is already authenticated!")
                _authState.value = AuthState.Authenticated(it)
            } ?: run {
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signInWithGoogle(credentialManager: CredentialManager, activity: Activity) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            signInWithGoogleUseCase(credentialManager, activity)
                .onSuccess { user ->
                    println("Successful login: $user")
//                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    println("Authentication failed")
//                    _authState.value = AuthState.Error(error.message ?: "Authentication failed")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    println("Sign out successfully!")
//                    _authState.value = AuthState.Unauthenticated
                }
                .onFailure { error ->
                    println("Sign out failed")
                    _authState.value = AuthState.Error(error.message ?: "Sign out failed")
                }
        }
    }

}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val user: User) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
