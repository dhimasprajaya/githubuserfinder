package com.dhimasprajaya.githubuserfinder.ui

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.dhimasprajaya.githubuserfinder.R
import com.dhimasprajaya.githubuserfinder.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val image: CircleImageView = view.findViewById(R.id.image)
    private val username: TextView = view.findViewById(R.id.username)
    private val repository: TextView = view.findViewById(R.id.repository)

    private var user: User? = null

    init {
        view.setOnClickListener {
            user?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(user: User?) {
        if (user == null) {
//            val resources = itemView.resources
//            name.text = resources.getString(R.string.loading)
//            description.visibility = View.GONE
//            language.visibility = View.GONE
//            stars.text = resources.getString(R.string.unknown)
//            forks.text = resources.getString(R.string.unknown)
        } else {
            showRepoData(user)
        }
    }

    private fun showRepoData(user: User) {
        this.user = user

        Picasso.get()
            .load(user.avatar_url)
            .fit()
//            .placeholder(R.drawable.ic_placeholder)
            .centerCrop()
            .into(image)

        username.text = user.login
        repository.text = user.url
    }

    companion object {
        fun create(parent: ViewGroup): UserViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.linear_item, parent, false)
            return UserViewHolder(view)
        }
    }
}
