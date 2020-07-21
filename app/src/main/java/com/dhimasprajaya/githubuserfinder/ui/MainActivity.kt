package com.dhimasprajaya.githubuserfinder.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dhimasprajaya.githubuserfinder.Injection
import com.dhimasprajaya.githubuserfinder.R
import com.dhimasprajaya.githubuserfinder.databinding.ActivityMainBinding
import com.dhimasprajaya.githubuserfinder.model.UserSearchResult
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val adapter = UsersAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // get the view model
        viewModel = ViewModelProvider(this, Injection.provideViewModelFactory())
            .get(MainViewModel::class.java)
        Log.d("TEST_LOG", "JALAN")
        Log.d("TEST_LOG", viewModel.toString())

        // add dividers between RecyclerView's row items
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        binding.list.addItemDecoration(decoration)
        setupScrollListener()

        initAdapter()
        val query = savedInstanceState?.getString(LAST_SEARCH_QUERY) ?: DEFAULT_QUERY
        if (viewModel.userResult.value == null) {
            viewModel.searchRepo(query)
        }
        initSearch(query)

        binding.search.setOnClickListener {
            initSearch(query)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(LAST_SEARCH_QUERY, binding.username.text.trim().toString())
    }

    private fun initAdapter() {
        binding.list.adapter = adapter
        viewModel.userResult.observe(this) { result ->
            when (result) {
                is UserSearchResult.Success -> {
                    showEmptyList(result.data.isEmpty())
                    adapter.submitList(result.data)
                }
                is UserSearchResult.Error -> {
                    Toast.makeText(
                        this,
                        "\uD83D\uDE28 Wooops $result.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun initSearch(query: String) {
        binding.username.setText(query)

        binding.username.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
        binding.username.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
    }

    private fun updateRepoListFromInput() {
        binding.username.text.trim().let {
            if (it.isNotEmpty()) {
                binding.list.scrollToPosition(0)
                viewModel.searchRepo(it.toString())
            }
        }
    }

    private fun showEmptyList(show: Boolean) {
        if (show) {
            binding.emptylist.visibility = View.VISIBLE
            binding.list.visibility = View.GONE
        } else {
            binding.emptylist.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
        }
    }

    private fun setupScrollListener() {
        val layoutManager = binding.list.layoutManager as LinearLayoutManager
        binding.list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                viewModel.listScrolled(visibleItemCount, lastVisibleItem, totalItemCount)
            }
        })
    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "octo"
    }
}
