package com.example.geozilla.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.geozilla.R
import com.example.geozilla.model.User
import de.hdodenhof.circleimageview.CircleImageView


class UserNotification(private var userList: List<User>,private val clickListener: (User) -> Unit) : RecyclerView.Adapter<UserNotification.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_notification, parent, false)
        return UserViewHolder(view)
    }

    fun setUserList(users: List<User>) {
        userList = users
        notifyDataSetChanged() // Notify the adapter that the dataset has changed
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user,clickListener)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.namenotification)
        private val imageView: CircleImageView = itemView.findViewById(R.id.imageviewnotification)
        fun bind(user: User, clickListener: (User) -> Unit) {
            nameTextView.text = user.name
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .into(imageView)
            itemView.setOnClickListener {
                clickListener(user)
            }

        }
    }
}
