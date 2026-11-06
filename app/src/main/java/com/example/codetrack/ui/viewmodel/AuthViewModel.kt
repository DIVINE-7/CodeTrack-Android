package com.example.codetrack.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.codetrack.data.local.security.UserSession
import com.example.codetrack.data.remote.dto.AuthResponseDto
import com.example.codetrack.data.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Generic reactive state model for asynchronous authentication operations.
 */
sealed interface AuthUiState<out T> {
    data object Idle : AuthUiState<Nothing>
    data object Loading : AuthUiState<Nothing>
    data class Success<out T>(val data: T) : AuthUiState<T>
    data class Error(val message: String) : AuthUiState<Nothing>
}

/**
 * ViewModel managing state and operations for user authentication, registration,
 * password recovery, and session persistence.
 *
 * @param authRepository Repository providing authentication and token operations.
 * @param externalScope Optional coroutine scope for unit testing; defaults to [viewModelScope].
 * @param dispatcher Coroutine dispatcher; defaults to [Dispatchers.Main].
 */
class AuthViewModel(
    private val authRepository: AuthRepository,
    private val externalScope: CoroutineScope? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val scope: CoroutineScope
        get() = externalScope ?: viewModelScope

    // -------------------------------------------------------------
    // REACTIVE STATE FLOWS
    // -------------------------------------------------------------

    private val _loginState = MutableStateFlow<AuthUiState<AuthResponseDto>>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState<AuthResponseDto>> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow<AuthUiState<AuthResponseDto>>(AuthUiState.Idle)
    val registerState: StateFlow<AuthUiState<AuthResponseDto>> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthUiState<String>>(AuthUiState.Idle)
    val forgotPasswordState: StateFlow<AuthUiState<String>> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<AuthUiState<String>>(AuthUiState.Idle)
    val resetPasswordState: StateFlow<AuthUiState<String>> = _resetPasswordState.asStateFlow()

    private val _sessionState = MutableStateFlow<AuthUiState<UserSession>>(AuthUiState.Idle)
    val sessionState: StateFlow<AuthUiState<UserSession>> = _sessionState.asStateFlow()

    // -------------------------------------------------------------
    // CONVENIENCE PROPERTIES
    // -------------------------------------------------------------

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    val currentSession: UserSession?
        get() = authRepository.getStoredSession()

    // -------------------------------------------------------------
    // AUTHENTICATION OPERATIONS
    // -------------------------------------------------------------

    /**
     * Executes user login.
     * Prevents duplicate submissions if a request is already in progress.
     */
    fun login(email: String, password: String) {
        if (_loginState.value is AuthUiState.Loading) {
            return
        }
        _loginState.value = AuthUiState.Loading

        scope.launch(dispatcher) {
            val result = authRepository.login(email = email, password = password)
            result.fold(
                onSuccess = { authResponse ->
                    _loginState.value = AuthUiState.Success(authResponse)
                    authRepository.getStoredSession()?.let { session ->
                        _sessionState.value = AuthUiState.Success(session)
                    }
                },
                onFailure = { error ->
                    _loginState.value = AuthUiState.Error(
                        error.message ?: "Login failed. Please check your credentials."
                    )
                }
            )
        }
    }

    /**
     * Executes new user registration.
     * Prevents duplicate submissions while loading.
     */
    fun register(name: String, email: String, password: String) {
        if (_registerState.value is AuthUiState.Loading) {
            return
        }
        _registerState.value = AuthUiState.Loading

        scope.launch(dispatcher) {
            val result = authRepository.register(name = name, email = email, password = password)
            result.fold(
                onSuccess = { authResponse ->
                    _registerState.value = AuthUiState.Success(authResponse)
                    authRepository.getStoredSession()?.let { session ->
                        _sessionState.value = AuthUiState.Success(session)
                    }
                },
                onFailure = { error ->
                    _registerState.value = AuthUiState.Error(
                        error.message ?: "Registration failed. Please try again."
                    )
                }
            )
        }
    }

    /**
     * Sends password reset request for specified email.
     * Prevents duplicate submissions while loading.
     */
    fun forgotPassword(email: String) {
        if (_forgotPasswordState.value is AuthUiState.Loading) {
            return
        }
        _forgotPasswordState.value = AuthUiState.Loading

        scope.launch(dispatcher) {
            val result = authRepository.forgotPassword(email = email)
            result.fold(
                onSuccess = { message ->
                    _forgotPasswordState.value = AuthUiState.Success(message)
                },
                onFailure = { error ->
                    _forgotPasswordState.value = AuthUiState.Error(
                        error.message ?: "Unable to request password reset."
                    )
                }
            )
        }
    }

    /**
     * Submits token and new password to finalize reset.
     * Prevents duplicate submissions while loading.
     */
    fun resetPassword(email: String, token: String, newPassword: String) {
        if (_resetPasswordState.value is AuthUiState.Loading) {
            return
        }
        _resetPasswordState.value = AuthUiState.Loading

        scope.launch(dispatcher) {
            val result = authRepository.resetPassword(
                email = email,
                token = token,
                newPassword = newPassword
            )
            result.fold(
                onSuccess = { message ->
                    _resetPasswordState.value = AuthUiState.Success(message)
                },
                onFailure = { error ->
                    _resetPasswordState.value = AuthUiState.Error(
                        error.message ?: "Password reset failed. Please check your reset token."
                    )
                }
            )
        }
    }

    /**
     * Validates and restores existing session on app launch.
     * Prevents duplicate concurrent checks.
     */
    fun restoreSession() {
        if (_sessionState.value is AuthUiState.Loading) {
            return
        }
        _sessionState.value = AuthUiState.Loading

        scope.launch(dispatcher) {
            val result = authRepository.restoreSession()
            result.fold(
                onSuccess = { session ->
                    _sessionState.value = AuthUiState.Success(session)
                },
                onFailure = { error ->
                    _sessionState.value = AuthUiState.Error(
                        error.message ?: "Session restoration failed."
                    )
                }
            )
        }
    }

    /**
     * Wipes all user session data and resets all UI operation states to Idle.
     */
    fun logout() {
        authRepository.logout()
        _sessionState.value = AuthUiState.Idle
        _loginState.value = AuthUiState.Idle
        _registerState.value = AuthUiState.Idle
        _forgotPasswordState.value = AuthUiState.Idle
        _resetPasswordState.value = AuthUiState.Idle
    }

    // -------------------------------------------------------------
    // STATE RESET METHODS (For UI Consumption / Dismissal)
    // -------------------------------------------------------------

    fun resetLoginState() {
        _loginState.value = AuthUiState.Idle
    }

    fun resetRegisterState() {
        _registerState.value = AuthUiState.Idle
    }

    fun resetForgotPasswordState() {
        _forgotPasswordState.value = AuthUiState.Idle
    }

    fun resetResetPasswordState() {
        _resetPasswordState.value = AuthUiState.Idle
    }

    fun resetSessionState() {
        _sessionState.value = AuthUiState.Idle
    }

    // -------------------------------------------------------------
    // FACTORY & PROVIDERS
    // -------------------------------------------------------------

    class Factory(
        private val authRepository: AuthRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                return AuthViewModel(authRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val repository = AuthRepository.getInstance(context)
            return Factory(repository)
        }

        fun provideFactory(repository: AuthRepository): ViewModelProvider.Factory {
            return Factory(repository)
        }
    }
}
