
import { apiRequest } from './api'

export const searchUsers = (query) => {
  return apiRequest(
    `/api/users/search?q=${encodeURIComponent(query)}`
  )
}

export const sendConversationRequest = (receiverUsername) => {
  return apiRequest('/api/conversations', {
    method: 'POST',
    body: JSON.stringify({ receiverUsername }),
  })
}

export const getMyConversations = () => {
  return apiRequest('/api/conversations')
}

export const getIncomingRequests = () => {
  return apiRequest('/api/conversations/requests')
}

export const acceptConversation = (conversationId) => {
  return apiRequest(`/api/conversations/${conversationId}/accept`, {
    method: 'PATCH',
  })
}

export const rejectConversation = (conversationId) => {
  return apiRequest(`/api/conversations/${conversationId}/reject`, {
    method: 'PATCH',
  })
}
