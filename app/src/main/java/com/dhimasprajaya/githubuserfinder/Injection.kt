package com.dhimasprajaya.githubuserfinder

import androidx.lifecycle.ViewModelProvider
import com.dhimasprajaya.githubuserfinder.api.GithubService
import com.dhimasprajaya.githubuserfinder.data.GithubRepository
import com.dhimasprajaya.githubuserfinder.ui.ViewModelFactory

object Injection {

    private fun provideGithubRepository(): GithubRepository {
        return GithubRepository(GithubService.create())
    }

    fun provideViewModelFactory(): ViewModelProvider.Factory {
        return ViewModelFactory(provideGithubRepository())
    }
}