package com.example.clinexusapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinexusapp.api.AuthRepository
import com.example.clinexusapp.model.*
import com.example.clinexusapp.util.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(private val repository: AuthRepository) : ViewModel() {

    // ---- States ----
    private val _chatState = MutableStateFlow<Resource<List<ChatMessageDTO>>>(Resource.Loading())
    val chatState = _chatState.asStateFlow()

    private val _contactsState = MutableStateFlow<Resource<List<ContactDTO>>>(Resource.Loading())
    val contactsState = _contactsState.asStateFlow()

    private val _conversationsState = MutableStateFlow<Resource<List<ConversationDTO>>>(Resource.Loading())
    val conversationsState = _conversationsState.asStateFlow()

    private val _conversationMessagesState =
        MutableStateFlow<Resource<ConversationMessagesResponse>>(Resource.Loading())
    val conversationMessagesState = _conversationMessagesState.asStateFlow()

    private val _sendMessageState = MutableStateFlow<Resource<SendMessageResponse>?>(null)
    val sendMessageState = _sendMessageState.asStateFlow()

    private val _selectedConversation = MutableStateFlow<ConversationDTO?>(null)
    val selectedConversation = _selectedConversation.asStateFlow()

    private val _selectedContact = MutableStateFlow<ContactDTO?>(null)
    val selectedContact = _selectedContact.asStateFlow()

    // Internal tracking
    private var lastMarkedReadId: Int? = null
    private var pollingJob: Job? = null

    init {
        fetchConversations()
        fetchContacts()
    }

    // ---- Public actions ----

    fun selectConversation(conversation: ConversationDTO?) {
        _selectedConversation.value = conversation
        _selectedContact.value = null
        if (conversation != null) {
            fetchConversationMessages(conversation.conversation_id)
            startPollingMessages(conversation.conversation_id)
        } else {
            stopPollingMessages()
        }
    }

    fun selectContact(contact: ContactDTO?) {
        _selectedContact.value = contact
        _selectedConversation.value = null
        stopPollingMessages()
    }

    fun fetchConversations() {
        viewModelScope.launch {
            _conversationsState.value = Resource.Loading()
            val result = repository.getConversations()
            _conversationsState.value = result
        }
    }

    fun fetchContacts() {
        viewModelScope.launch {
            _contactsState.value = Resource.Loading()
            _contactsState.value = repository.getAvailableContacts()
        }
    }

    fun fetchMessages() {
        viewModelScope.launch {
            _chatState.value = Resource.Loading()
            _chatState.value = repository.getChatMessages()
        }
    }

    fun fetchConversationMessages(conversationID: Int, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                lastMarkedReadId = null
                _conversationMessagesState.value = Resource.Loading()
            }
            val result = repository.getConversationMessages(conversationID)
            // Only update state if it's a success or we are not in silent mode
            if (result is Resource.Success || !silent) {
                _conversationMessagesState.value = result
            }
        }
    }

    fun startPollingMessages(conversationID: Int) {
        stopPollingMessages()
        pollingJob = viewModelScope.launch {
            while (true) {
                delay(5000) // Poll every 5 seconds
                fetchConversationMessages(conversationID, silent = true)
            }
        }
    }

    fun stopPollingMessages() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun sendMessage(receiverAccountType: String, receiverAccountID: Int, messageContent: String, conversationID: Int?) {
        viewModelScope.launch {
            _sendMessageState.value = Resource.Loading()
            val result = repository.sendMessage(receiverAccountType, receiverAccountID, messageContent)
            _sendMessageState.value = result

            if (result is Resource.Success) {
                if (conversationID != null) {
                    // Refresh messages for existing conversation
                    fetchConversationMessages(conversationID)
                } else {
                    // New conversation: refresh list to find new conversation ID
                    val convResult = repository.getConversations()
                    if (convResult is Resource.Success) {
                        _conversationsState.value = convResult
                        // Find the conversation with the receiver
                        val newConv = convResult.data?.find {
                            it.account_type == receiverAccountType && it.account_id == receiverAccountID
                        }
                        if (newConv != null) {
                            selectConversation(newConv)
                        } else {
                            // If not found, just refresh conversations
                            fetchConversations()
                        }
                    } else {
                        // If getting conversations fails, still refresh the list
                        fetchConversations()
                    }
                }
            }
        }
    }

    fun markConversationAsRead(conversationID: Int, lastMessageId: Int) {
        if (lastMarkedReadId == lastMessageId) return // Already marked

        viewModelScope.launch {
            val result = repository.markConversationAsRead(conversationID, lastMessageId)
            if (result is Resource.Success) {
                lastMarkedReadId = lastMessageId
            }
        }
    }

    fun resetSendMessageState() {
        _sendMessageState.value = null
    }
}