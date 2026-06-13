package com.sharedparking.android.ui.messages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sharedparking.android.databinding.ActivityMessagesBinding
import com.sharedparking.android.model.Message
import com.sharedparking.android.network.ApiClient
import com.sharedparking.android.network.ApiService
import com.sharedparking.android.utils.Resource
import com.sharedparking.android.viewmodel.MessageViewModel
import java.text.SimpleDateFormat
import java.util.*

class MessagesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMessagesBinding
    private lateinit var viewModel: MessageViewModel
    private lateinit var adapter: MessageAdapter

    private var otherUserId: Int = 0
    private var otherUserName: String? = null
    private var spotId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        otherUserId = intent.getIntExtra("extra_other_user_id", 0)
        otherUserName = intent.getStringExtra("extra_other_user_name")
        spotId = intent.getIntExtra("extra_spot_id", 0)

        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = otherUserName ?: "消息"

        setupRecyclerView()
        setupRefresh()
        setupSendMessage()
        observeData()

        if (otherUserId > 0) {
            viewModel.loadConversation(otherUserId)
        } else {
            viewModel.loadInbox()
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter { message ->
            Toast.makeText(this, message.content, Toast.LENGTH_SHORT).show()
        }
        binding.rvConversations.layoutManager = LinearLayoutManager(this)
        binding.rvConversations.adapter = adapter
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            if (otherUserId > 0) {
                viewModel.loadConversation(otherUserId)
            } else {
                viewModel.loadInbox()
            }
        }
    }

    private fun setupSendMessage() {
        binding.btnSend.setOnClickListener {
            val content = binding.etMessage.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "请输入消息内容", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (otherUserId <= 0) {
                Toast.makeText(this, "请先从消息列表选择会话", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.sendMessage(otherUserId, content, "", if (spotId > 0) spotId else null)
            binding.etMessage.text.clear()
        }
    }

    private fun observeData() {
        viewModel.messagesState.observe(this) { resource ->
            binding.swipeRefresh.isRefreshing = false
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvConversations.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val messages = resource.data ?: emptyList()
                    adapter.submitList(messages)
                    if (messages.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.rvConversations.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.rvConversations.visibility = View.VISIBLE
                        binding.rvConversations.smoothScrollToPosition(messages.size - 1)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                    binding.rvConversations.visibility = View.GONE
                    Toast.makeText(this, resource.message ?: "加载失败", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewModel.sendState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.btnSend.isEnabled = false
                    binding.btnSend.text = "发送中..."
                }
                is Resource.Success -> {
                    binding.btnSend.isEnabled = true
                    binding.btnSend.text = "发送"
                    otherUserId?.let { viewModel.loadConversation(it) }
                }
                is Resource.Error -> {
                    binding.btnSend.isEnabled = true
                    binding.btnSend.text = "发送"
                    Toast.makeText(this, resource.message ?: "发送失败", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

/**
 * 消息列表适配器
 */
class MessageAdapter(
    private val onItemClick: (Message) -> Unit
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    private var messages: List<Message> = emptyList()

    fun submitList(list: List<Message>) {
        messages = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class ViewHolder(
        itemView: View,
        private val onItemClick: (Message) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(android.R.id.text1)
        private val tvSubtitle: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(message: Message) {
            tvTitle.text = message.senderUsername ?: "用户#${message.senderId}"
            tvSubtitle.text = message.content.take(100)
            itemView.setOnClickListener { onItemClick(message) }
        }
    }
}
