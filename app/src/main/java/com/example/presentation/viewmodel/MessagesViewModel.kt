package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.model.NeptunMessage
import com.example.domain.repository.NeptunRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MessagesUiState(
    val messages: List<NeptunMessage> = emptyList(),
    val filteredMessages: List<NeptunMessage> = emptyList(),
    val selectedMessage: NeptunMessage? = null,
    val isLoadingContent: Boolean = false,
    val showUnreadOnly: Boolean = false,
    val isRefreshing: Boolean = false,
    val unreadCount: Int = 0
)

class MessagesViewModel(
    private val neptunRepository: NeptunRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessagesUiState())
    val uiState: StateFlow<MessagesUiState> = _uiState.asStateFlow()

    init {
        observeMessages()
        refreshMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            neptunRepository.getMessages().collect { msgs ->
                val unread = msgs.count { !it.isRead }
                _uiState.update { state ->
                    val filtered = if (state.showUnreadOnly) {
                        msgs.filter { !it.isRead }
                    } else {
                        msgs
                    }
                    val updatedSelected = state.selectedMessage?.let { curSel ->
                        msgs.firstOrNull { it.id == curSel.id } ?: curSel
                    }
                    state.copy(
                        messages = msgs,
                        filteredMessages = filtered,
                        selectedMessage = updatedSelected,
                        unreadCount = unread
                    )
                }
            }
        }
    }

    fun toggleUnreadFilter() {
        _uiState.update { state ->
            val newFilter = !state.showUnreadOnly
            val filtered = if (newFilter) {
                state.messages.filter { !it.isRead }
            } else {
                state.messages
            }
            state.copy(showUnreadOnly = newFilter, filteredMessages = filtered)
        }
    }

    fun openMessage(message: NeptunMessage) {
        val needsFetch = message.bodyHtml.isBlank()
        _uiState.update { it.copy(selectedMessage = message, isLoadingContent = needsFetch) }
        viewModelScope.launch {
            if (!message.isRead) {
                neptunRepository.markMessageAsRead(message.id)
            }
            if (needsFetch) {
                val content = neptunRepository.getMessageContent(message.id)
                if (content.isNotBlank()) {
                    val updated = message.copy(bodyHtml = content, isRead = true)
                    _uiState.update { state ->
                        if (state.selectedMessage?.id == message.id) {
                            state.copy(selectedMessage = updated, isLoadingContent = false)
                        } else {
                            state.copy(isLoadingContent = false)
                        }
                    }
                } else {
                    _uiState.update { it.copy(isLoadingContent = false) }
                }
            }
        }
    }

    fun closeMessage() {
        _uiState.update { it.copy(selectedMessage = null, isLoadingContent = false) }
    }

    fun reloadSelectedMessageContent() {
        val message = _uiState.value.selectedMessage ?: return
        _uiState.update { it.copy(isLoadingContent = true) }
        viewModelScope.launch {
            val content = neptunRepository.getMessageContent(message.id)
            val updated = message.copy(
                bodyHtml = content.ifBlank { "Az üzenet tartalma nem érhető el vagy üres." },
                isRead = true
            )
            _uiState.update { state ->
                if (state.selectedMessage?.id == message.id) {
                    state.copy(selectedMessage = updated, isLoadingContent = false)
                } else {
                    state.copy(isLoadingContent = false)
                }
            }
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            neptunRepository.refreshMessages()
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    companion object {
        fun provideFactory(
            neptunRepository: NeptunRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MessagesViewModel(neptunRepository) as T
            }
        }
    }
}
