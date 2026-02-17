package com.drape.ui.login

import com.drape.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import sun.misc.Unsafe

class LoginViewModelTest {

    @Test
    fun loginUiState_defaults_areCorrect() {
        val state = LoginUiState()

        assertEquals(false, state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(false, state.isLoginSuccessful)
    }

    @Test
    fun clearError_removesExistingErrorMessage() {
        val authRepository = allocateWithoutConstructor(AuthRepository::class.java)
        val viewModel = LoginViewModel(authRepository)
        setUiState(viewModel, LoginUiState(errorMessage = "Credenziali non valide"))

        viewModel.clearError()

        assertNull(viewModel.uiState.value.errorMessage)
    }

    private fun setUiState(viewModel: LoginViewModel, state: LoginUiState) {
        val field = LoginViewModel::class.java.getDeclaredField("_uiState")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<LoginUiState>
        stateFlow.value = state
    }

    private fun <T> allocateWithoutConstructor(clazz: Class<T>): T {
        val field = Unsafe::class.java.getDeclaredField("theUnsafe")
        field.isAccessible = true
        val unsafe = field.get(null) as Unsafe
        @Suppress("UNCHECKED_CAST")
        return unsafe.allocateInstance(clazz) as T
    }
}
