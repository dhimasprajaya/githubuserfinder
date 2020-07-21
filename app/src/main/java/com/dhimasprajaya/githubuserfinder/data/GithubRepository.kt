package com.dhimasprajaya.githubuserfinder.data

import android.util.Log
import com.dhimasprajaya.githubuserfinder.api.GithubService
import com.dhimasprajaya.githubuserfinder.api.IN_QUALIFIER
import com.dhimasprajaya.githubuserfinder.model.User
import com.dhimasprajaya.githubuserfinder.model.UserSearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import retrofit2.HttpException
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 1

@ExperimentalCoroutinesApi
class GithubRepository(private val service: GithubService) {

    // keep the list of all results received
    private val inMemoryCache = mutableListOf<User>()

    // keep channel of results. The channel allows us to broadcast updates so
    // the subscriber will have the latest data
    private val searchResults = ConflatedBroadcastChannel<UserSearchResult>()

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = GITHUB_STARTING_PAGE_INDEX

    // avoid triggering multiple requests in the same time
    private var isRequestInProgress = false

    /**
     * Search repositories whose names match the query, exposed as a stream of data that will emit
     * every time we get more data from the network.
     */
    suspend fun getSearchResultStream(query: String): Flow<UserSearchResult> {
        Log.d("GithubRepository", "New query: $query")
        lastRequestedPage = 1
        inMemoryCache.clear()
        requestAndSaveData(query)

        return searchResults.asFlow()
    }

    suspend fun requestMore(query: String) {
        if (isRequestInProgress) return
        val successful = requestAndSaveData(query)
        if (successful) {
            lastRequestedPage++
        }
    }

    suspend fun retry(query: String) {
        if (isRequestInProgress) return
        requestAndSaveData(query)
    }

    private suspend fun requestAndSaveData(query: String): Boolean {
        isRequestInProgress = true
        var successful = false

        val apiQuery = query + IN_QUALIFIER
        try {
            val response = service.searchRepos(apiQuery, lastRequestedPage, NETWORK_PAGE_SIZE)
            Log.d("GithubRepository", "response $response")
            val repos = response.items ?: emptyList()
            inMemoryCache.addAll(repos)
            val reposByName = reposByName(query)
            searchResults.offer(UserSearchResult.Success(reposByName))
            successful = true
        } catch (exception: IOException) {
            searchResults.offer(UserSearchResult.Error(exception))
        } catch (exception: HttpException) {
            searchResults.offer(UserSearchResult.Error(exception))
        }
        isRequestInProgress = false
        return successful
    }

    private fun reposByName(query: String): List<User> {
        // from the in memory cache select only the repos whose name or description matches
        // the query. Then order the results.
        return inMemoryCache.filter {
            it.login.contains(query, true)
        }.sortedWith(compareByDescending<User> { it.login }.thenBy { it.id })
    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 20
    }
}