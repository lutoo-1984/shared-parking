package com.sharedparking.android.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharedparking.android.model.Message
import com.sharedparking.android.network.ApiClient
import com.sharedparking.android.network.ApiService
import com.sharedparking.android.utils.Resource
import kotlinx.coroutines.launch

/**
 * 消息ViewModel
 * 管理收件箱、发件箱和会话消息
 */
class MessageViewModel : ViewModel() {

    private val api = com.sharedparking.android.network.ApiClientBuilder.getApiService()

    private val _messagesState = MutableLiveData<Resource<List<Message>>>()
    val messagesState: LiveData<Resource<List<Message>>> = _messagesState

    private val _sendState = MutableLiveData<Resource<Message>>()
    val sendState: LiveData<Resource<Message>> = _sendState

    /**
     * 加载收件箱
     */
    fun loadInbox(page: Int = 1, limit: Int = 50) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            try {
                val response = api.getInbox(page, limit)
                if (response.isSuccessful) {
                    val body = response.body()
                    val messages = body?.data?.messages ?: emptyList()
                    _messagesState.value = Resource.Success(messages)
                } else {
                    _messagesState.value = Resource.Error("加载消息失败")
                }
            } catch (e: Exception) {
                _messagesState.value = Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 加载与某用户的对话
     */
    fun loadConversation(otherUserId: Int, page: Int = 1, limit: Int = 50) {
        viewModelScope.launch {
            _messagesState.value = Resource.Loading()
            try {
                val response = api.getConversation(otherUserId, page, limit)
                if (response.isSuccessful) {
                    val body = response.body()
                    val messages = body?.data?.messages ?: emptyList()
                    _messagesState.value = Resource.Success(messages)
                } else {
                    _messagesState.value = Resource.Error("加载对话失败")
                }
            } catch (e: Exception) {
                _messagesState.value = Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 发送消息
     */
    fun sendMessage(receiverId: Int, content: String, subject: String = "", bookingId: Int? = null) {
        viewModelScope.launch {
            _sendState.value = Resource.Loading()
            try {
                val request = com.sharedparking.android.model.SendMessageRequest(
                    receiverId = receiverId,
                    content = content,
                    subject = subject.ifEmpty { null },
                    bookingId = bookingId
                )
                val response = api.sendMessage(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    val message = body?.data
                    _sendState.value = Resource.Success(message)
                } else {
                    val errorMsg = when {
                        response.code() == 401 -> "请先登录"
                        response.code() == 403 -> "无权操作"
                        else -> "发送失败"
                    }
                    _sendState.value = Resource.Error(errorMsg)
                }
            } catch (e: Exception) {
                _sendState.value = Resource.Error(e.message ?: "网络错误")
            }
        }
    }

    /**
     * 标记消息为已读
     */
    fun markAsRead(messageId: Int) {
        viewModelScope.launch {
            try {
                api.markMessageRead(messageId)
            } catch (e: Exception) {
                // 忽略标记已读失败
            }
        }
    }

    /**
     * 获取未读消息数
     */
    fun getUnreadCount(callback: (Int) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.getUnreadCount()
                if (response.isSuccessful) {
                    val count = response.body()?.data?.unreadCount ?: 0
                    callback(count)
                }
            } catch (e: Exception) {
                callback(0)
            }
        }
    }
}
