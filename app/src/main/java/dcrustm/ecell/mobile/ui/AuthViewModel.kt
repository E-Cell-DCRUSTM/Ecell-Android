package dcrustm.ecell.mobile.ui

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
                _authState.value = AuthState.Authenticated(it)
            } ?: run {
                _authState.value = AuthState.Unauthenticated
            }
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    fun signInWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken)
                .onSuccess { user ->
                    _authState.value = AuthState.Authenticated(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Authentication failed")
                }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase()
                .onSuccess {
                    _authState.value = AuthState.Unauthenticated
                }
                .onFailure { error ->
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
