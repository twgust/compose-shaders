package com.example.myapplication

sealed interface MainActivityEvent {
    data object AddUserPicture: MainActivityEvent
}

open class MainActivityEventChannel<out T>(private val content: T) {

    private var hasBeenHandled = false

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}
