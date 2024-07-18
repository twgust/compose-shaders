package com.example.myapplication

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Define the BaseViewModel class
abstract class BaseViewModel<T : Any>(initialState: T) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<T> = _state

    fun updateState(update: T.() -> T) {
        _state.value = _state.value.update()
    }
}