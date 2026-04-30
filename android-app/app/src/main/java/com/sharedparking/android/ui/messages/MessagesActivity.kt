package com.sharedparking.android.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sharedparking.android.databinding.ActivityMessagesBinding

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var adapter: ConversationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupRecyclerView()
        setupRefresh()

        // 初始加载 - 空状态（等待后端API实现）
        showEmptyState()
    }

    private fun setupRecyclerView() {
        adapter = ConversationAdapter { conversation ->
            Toast.makeText(this, "消息详情功能开发中", Toast.LENGTH_SHORT).show()
        }
        binding.rvConversations.layoutManager = LinearLayoutManager(this)
        binding.rvConversations.adapter = adapter
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
            showEmptyState()
        }
    }

    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.rvConversations.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

/**
 * 会话列表适配器
 */
private class ConversationAdapter(
    private val onItemClick: (Conversation) -> Unit
) : RecyclerView.Adapter<ConversationAdapter.ViewHolder>() {

    private var conversations: List<Conversation> = emptyList()

    fun submitList(list: List<Conversation>) {
        conversations = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(conversations[position])
    }

    override fun getItemCount(): Int = conversations.size

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Conversation) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(android.R.id.text1)
        private val tvSubtitle: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(conversation: Conversation) {
            tvTitle.text = conversation.otherUserName
            tvSubtitle.text = conversation.lastMessage
            itemView.setOnClickListener { onItemClick(conversation) }
        }
    }
}

/**
 * 会话数据类
 */
private data class Conversation(
    val id: Int,
    val otherUserId: Int,
    val otherUserName: String,
    val lastMessage: String,
    val unreadCount: Int,
    val lastMessageTime: String
)
