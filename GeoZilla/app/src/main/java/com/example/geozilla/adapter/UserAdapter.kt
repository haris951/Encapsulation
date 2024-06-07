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

class UserAdapter(private var userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item_layout, parent, false)
        return UserViewHolder(view)
    }

    fun setUserList(users: List<User>) {
        userList = users
        notifyDataSetChanged() // Notify the adapter that the dataset has changed
    }

//    fun getUserList(): List<User> {
//        return userList
//    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

//    fun addUser(user: User) {
//        if (!userList.contains(user)) { // Check if user already exists
//            userList = userList.toMutableList().apply {
//                add(user)
//            }
//            notifyItemInserted(userList.size - 1)
//        }
//    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        private val imageView: CircleImageView = itemView.findViewById(R.id.userImageView)
        private val addressTextView:TextView=itemView.findViewById(R.id.userAddress)

        fun bind(user: User) {
            nameTextView.text = user.name
            addressTextView.text= user.address
            Glide.with(itemView.context)
                .load(user.photoUrl)
                .into(imageView)
        }
    }
}
