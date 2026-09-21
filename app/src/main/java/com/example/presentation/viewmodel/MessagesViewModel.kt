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
    val unreadCount: Int = 0,
    val searchQuery: String = "",
    val errorMessage: String? = null,
    val contentErrorMessage: String? = null
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
                    val filtered = applyFilters(msgs, state.showUnreadOnly, state.searchQuery)
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

    private fun applyFilters(
        messages: List<NeptunMessage>,
        unreadOnly: Boolean,
        query: String
    ): List<NeptunMessage> {
        var result = messages
        if (unreadOnly) {
            result = result.filter { !it.isRead }
        }
        if (query.isNotBlank()) {
            result = result.filter { msg ->
                msg.subject.contains(query, ignoreCase = true) ||
                    msg.sender.contains(query, ignoreCase = true)
            }
        }
        return result
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredMessages = applyFilters(state.messages, state.showUnreadOnly, query)
            )
        }
    }

    fun toggleUnreadFilter() {
        _uiState.update { state ->
            val newFilter = !state.showUnreadOnly
            state.copy(
                showUnreadOnly = newFilter,
                filteredMessages = applyFilters(state.messages, newFilter, state.searchQuery)
            )
        }
    }

    fun openMessage(message: NeptunMessage) {
        val needsFetch = message.bodyHtml.isBlank()
        _uiState.update {
            it.copy(
                selectedMessage = message,
                isLoadingContent = needsFetch,
                contentErrorMessage = null
            )
        }
        viewModelScope.launch {
            if (needsFetch) {
                val content = neptunRepository.getMessageContent(message.id)
                if (content.isNotBlank()) {
                    val updated = message.copy(bodyHtml = content, isRead = true)
                    _uiState.update { state ->
                        if (state.selectedMessage?.id == message.id) {
                            state.copy(selectedMessage = updated, isLoadingContent = false, contentErrorMessage = null)
                        } else {
                            state.copy(isLoadingContent = false)
                        }
                    }
                } else {
                    _uiState.update { state ->
                        if (state.selectedMessage?.id == message.id) {
                            state.copy(isLoadingContent = false, contentErrorMessage = "Az üzenet betöltése nem sikerült.")
                        } else {
                            state.copy(isLoadingContent = false)
                        }
                    }
                }
            } else {
                if (!message.isRead) {
                    neptunRepository.markMessageAsRead(message.id)
                }
            }
        }
    }

    fun closeMessage() {
        _uiState.update { it.copy(selectedMessage = null, isLoadingContent = false, contentErrorMessage = null) }
    }

    fun reloadSelectedMessageContent() {
        val message = _uiState.value.selectedMessage ?: return
        _uiState.update { it.copy(isLoadingContent = true, contentErrorMessage = null) }
        viewModelScope.launch {
            val content = neptunRepository.getMessageContent(message.id)
            if (content.isNotBlank()) {
                val updated = message.copy(
                    bodyHtml = content,
                    isRead = true
                )
                _uiState.update { state ->
                    if (state.selectedMessage?.id == message.id) {
                        state.copy(selectedMessage = updated, isLoadingContent = false, contentErrorMessage = null)
                    } else {
                        state.copy(isLoadingContent = false)
                    }
                }
            } else {
                _uiState.update { state ->
                    if (state.selectedMessage?.id == message.id) {
                        state.copy(isLoadingContent = false, contentErrorMessage = "Az üzenet betöltése nem sikerült.")
                    } else {
                        state.copy(isLoadingContent = false)
                    }
                }
            }
        }
    }

    fun refreshMessages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            val res = neptunRepository.refreshMessages()
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    errorMessage = if (res.isFailure) "Az üzenetek betöltése nem sikerült." else null
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
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
