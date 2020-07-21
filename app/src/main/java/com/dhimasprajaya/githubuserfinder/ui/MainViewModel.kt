package com.dhimasprajaya.githubuserfinder.ui

import androidx.lifecycle.*
import com.dhimasprajaya.githubuserfinder.data.GithubRepository
import com.dhimasprajaya.githubuserfinder.model.UserSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MainViewModel(private val repository: GithubRepository) : ViewModel() {

    companion object {
        private const val VISIBLE_THRESHOLD = 5
    }

    private val queryLiveData = MutableLiveData<String>()
    val userResult: LiveData<UserSearchResult> = queryLiveData.switchMap { queryString ->
        liveData {
            val repos = repository.getSearchResultStream(queryString).asLiveData(Dispatchers.Main)
            emitSource(repos)
        }
    }

    fun searchRepo(queryString: String) {
        queryLiveData.postValue(queryString)
    }

    fun listScrolled(visibleItemCount: Int, lastVisibleItemPosition: Int, totalItemCount: Int) {
        if (visibleItemCount + lastVisibleItemPosition + VISIBLE_THRESHOLD >= totalItemCount) {
            val immutableQuery = queryLiveData.value
            if (immutableQuery != null) {
                viewModelScope.launch {
                    repository.requestMore(immutableQuery)
                }
            }
        }
    }
}