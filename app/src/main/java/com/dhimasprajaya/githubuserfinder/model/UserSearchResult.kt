package com.dhimasprajaya.githubuserfinder.model

import java.lang.Exception

sealed class UserSearchResult {
    data class Success(val data: List<User>) : UserSearchResult()
    data class Error(val error: Exception) : UserSearchResult()
}