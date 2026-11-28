
import { useCallback, useEffect, useMemo, useState } from 'react'
import UserSearch from '../components/conversations/UserSearch'
import InvitationsList from '../components/conversations/InvitationsList'
import {
  getMyConversations,
  getIncomingRequests,
} from '../services/conversationService'
import './ConversationPage.css'

function getUsernameFromToken() {
  try {
    const token = localStorage.getItem('token')
    if (!token) return ''

    const payload = token.split('.')[1]
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/')
    const decoded = JSON.parse(atob(normalized))

    return decoded.sub || decoded.username || ''
  } catch {
    return ''
  }
}

function getOtherUser(conversation, currentUsername) {
  if (conversation.senderUsername === currentUsername) {
    return {
      id: conversation.receiverId,
      username: conversation.receiverUsername,
      email: conversation.receiverEmail,
    }
  }

  return {
    id: conversation.senderId,
    username: conversation.senderUsername,
    email: conversation.senderEmail,
  }
}

function formatConversationDate(dateValue) {
  if (!dateValue) return ''

  const date = new Date(dateValue)

  if (Number.isNaN(date.getTime())) return ''

  return date.toLocaleDateString([], {
    day: 'numeric',
    month: 'short',
  })
}

function ConversationPage({ onLogout }) {
  const [conversations, setConversations] = useState([])
  const [requests, setRequests] = useState([])
  const [activeTab, setActiveTab] = useState('chats')
  const [selectedConversation, setSelectedConversation] = useState(null)
  const [friendSearch, setFriendSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [mobileChatOpen, setMobileChatOpen] = useState(false)

  const currentUsername = getUsernameFromToken()

  const loadData = useCallback(async () => {
    try {
      setError('')

      const [conversationData, requestData] = await Promise.all([
        getMyConversations(),
        getIncomingRequests(),
      ])

      const safeConversations = Array.isArray(conversationData)
        ? conversationData
        : []

      const safeRequests = Array.isArray(requestData)
        ? requestData
        : []

      setConversations(safeConversations)
      setRequests(safeRequests)

      // Keep the selected conversation updated after accepting/reloading.
      setSelectedConversation((previous) => {
        if (!previous) return null

        return (
          safeConversations.find(
            (conversation) => conversation.id === previous.id
          ) || null
        )
      })
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    loadData()
  }, [loadData])

  const acceptedConversations = useMemo(
    () =>
      conversations.filter(
        (conversation) => conversation.status === 'ACCEPTED'
      ),
    [conversations]
  )

  const filteredConversations = useMemo(() => {
    const search = friendSearch.trim().toLowerCase()

    if (!search) return acceptedConversations

    return acceptedConversations.filter((conversation) => {
      const user = getOtherUser(conversation, currentUsername)

      return (
        user.username?.toLowerCase().includes(search) ||
        user.email?.toLowerCase().includes(search)
      )
    })
  }, [acceptedConversations, friendSearch, currentUsername])

  const handleSelectConversation = (conversation) => {
    setSelectedConversation(conversation)
    setMobileChatOpen(true)
  }

  const handleTabChange = (tab) => {
    setActiveTab(tab)
    setMobileChatOpen(false)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    onLogout()
  }

  const selectedUser = selectedConversation
    ? getOtherUser(selectedConversation, currentUsername)
    : null

  if (loading) {
    return (
      <main className="convo-loading-screen">
        <div className="convo-loading-logo">C</div>
        <div className="convo-loading-spinner" />
        <p>Preparing your conversations...</p>
      </main>
    )
  }

  return (
    <main className="convo-app">
      {/* LEFT SIDEBAR */}
      <aside
        className={`convo-sidebar ${
          mobileChatOpen ? 'sidebar-mobile-hidden' : ''
        }`}
      >
        <header className="sidebar-header">
          <div className="convo-logo">
            <span className="convo-logo-icon">C</span>
            <span>convo</span>
          </div>

          <button
            className="sidebar-logout"
            type="button"
            onClick={handleLogout}
            title="Logout"
          >
            <span>↗</span>
          </button>
        </header>

        <div className="sidebar-profile">
          <div className="sidebar-profile-avatar">
            {currentUsername?.charAt(0).toUpperCase() || 'U'}
          </div>

          <div className="sidebar-profile-details">
            <strong>{currentUsername || 'Your account'}</strong>
            <span>
              <i />
              Signed in
            </span>
          </div>
        </div>

        {/* SIDEBAR NAVIGATION */}
        <nav className="sidebar-tabs">
          <button
            type="button"
            className={activeTab === 'chats' ? 'active' : ''}
            onClick={() => handleTabChange('chats')}
          >
            <span className="tab-icon">▤</span>
            <span>Chats</span>
          </button>

          <button
            type="button"
            className={activeTab === 'search' ? 'active' : ''}
            onClick={() => handleTabChange('search')}
          >
            <span className="tab-icon">⌕</span>
            <span>Find people</span>
          </button>

          <button
            type="button"
            className={activeTab === 'invites' ? 'active' : ''}
            onClick={() => handleTabChange('invites')}
          >
            <span className="tab-icon">✉</span>
            <span>Invites</span>

            {requests.length > 0 && (
              <span className="tab-count">{requests.length}</span>
            )}
          </button>
        </nav>

        <div className="sidebar-content">
          {error && (
            <div className="sidebar-error">
              <p>{error}</p>
              <button type="button" onClick={loadData}>
                Retry
              </button>
            </div>
          )}

          {/* CHATS TAB */}
          {activeTab === 'chats' && (
            <section className="sidebar-chat-section">
              <div className="sidebar-section-heading">
                <div>
                  <span className="sidebar-eyebrow">YOUR PEOPLE</span>
                  <h2>Messages</h2>
                </div>

                <span className="sidebar-total">
                  {acceptedConversations.length}
                </span>
              </div>

              <div className="sidebar-search">
                <span>⌕</span>
                <input
                  type="text"
                  placeholder="Search conversations..."
                  value={friendSearch}
                  onChange={(event) => setFriendSearch(event.target.value)}
                />
              </div>

              {filteredConversations.length === 0 ? (
                <div className="sidebar-empty">
                  <div className="sidebar-empty-icon">♧</div>

                  <strong>
                    {friendSearch ? 'No matching people' : 'No conversations yet'}
                  </strong>

                  <p>
                    {friendSearch
                      ? 'Try another username or email.'
                      : 'Find someone and start connecting.'}
                  </p>

                  {!friendSearch && (
                    <button
                      type="button"
                      onClick={() => handleTabChange('search')}
                    >
                      Find people
                    </button>
                  )}
                </div>
              ) : (
                <div className="sidebar-friends-list">
                  {filteredConversations.map((conversation) => {
                    const user = getOtherUser(
                      conversation,
                      currentUsername
                    )

                    const isSelected =
                      selectedConversation?.id === conversation.id

                    return (
                      <button
                        type="button"
                        key={conversation.id}
                        className={`sidebar-friend ${
                          isSelected ? 'selected' : ''
                        }`}
                        onClick={() =>
                          handleSelectConversation(conversation)
                        }
                      >
                        <div className="friend-avatar">
                          {user.username?.charAt(0).toUpperCase() || '?'}
                          <span className="friend-avatar-status" />
                        </div>

                        <div className="friend-preview">
                          <div className="friend-preview-top">
                            <strong>{user.username}</strong>
                            <span>
                              {formatConversationDate(
                                conversation.updatedAt
                              )}
                            </span>
                          </div>

                          <p>{user.email}</p>
                        </div>
                      </button>
                    )
                  })}
                </div>
              )}
            </section>
          )}

          {/* FIND PEOPLE TAB */}
          {activeTab === 'search' && (
            <section className="sidebar-tool-section">
              <div className="sidebar-section-heading">
                <div>
                  <span className="sidebar-eyebrow">DISCOVER</span>
                  <h2>Find people</h2>
                </div>
              </div>

              <UserSearch onRequestSent={loadData} />
            </section>
          )}

          {/* INVITATIONS TAB */}
          {activeTab === 'invites' && (
            <section className="sidebar-tool-section">
              <div className="sidebar-section-heading">
                <div>
                  <span className="sidebar-eyebrow">CONNECTION REQUESTS</span>
                  <h2>Invitations</h2>
                </div>

                <span className="sidebar-total">{requests.length}</span>
              </div>

              <InvitationsList
                requests={requests}
                onRequestUpdated={loadData}
              />
            </section>
          )}
        </div>

        <footer className="sidebar-footer">
          <span>© 2026 Convo</span>
          <span>Made for conversations.</span>
        </footer>
      </aside>

      {/* RIGHT CHAT AREA */}
      <section
        className={`convo-chat-panel ${
          mobileChatOpen ? 'chat-mobile-visible' : ''
        }`}
      >
        {selectedConversation && selectedUser ? (
          <>
            <header className="chat-header">
              <button
                className="mobile-back-button"
                type="button"
                onClick={() => setMobileChatOpen(false)}
                aria-label="Back to conversations"
              >
                ←
              </button>

              <div className="chat-header-avatar">
                {selectedUser.username?.charAt(0).toUpperCase() || '?'}
              </div>

              <div className="chat-header-details">
                <strong>{selectedUser.username}</strong>
                <span>Connected on Convo</span>
              </div>

              <button
                className="chat-header-menu"
                type="button"
                title="Conversation details"
              >
                ⋮
              </button>
            </header>

            <div className="chat-message-area">
              <div className="chat-date-divider">
                <span>YOUR CONVERSATION</span>
              </div>

              <div className="chat-start-card">
                <div className="chat-start-avatar">
                  {selectedUser.username?.charAt(0).toUpperCase() || '?'}
                </div>

                <h2>{selectedUser.username}</h2>
                <p>{selectedUser.email}</p>

                <div className="chat-connected-label">
                  <span />
                  You are connected
                </div>

                <p className="chat-start-description">
                  This is the beginning of your conversation.
                  Say hello and make a connection!
                </p>
              </div>
            </div>

            <div className="chat-composer">
              <button
                type="button"
                className="composer-attachment"
                disabled
                title="Attachments will be available soon"
              >
                +
              </button>

              <input
                type="text"
                placeholder="Real-time messaging will be available soon..."
                disabled
              />

              <button
                type="button"
                className="composer-send"
                disabled
                title="Messaging is not connected yet"
              >
                ➤
              </button>
            </div>
          </>
        ) : (
          <div className="chat-welcome-screen">
            <div className="chat-welcome-decoration">
              <div className="welcome-orbit orbit-one" />
              <div className="welcome-orbit orbit-two" />

              <div className="welcome-chat-icon">
                <span>✉</span>
              </div>
            </div>

            <h1>Your conversations,</h1>
            <h1 className="welcome-purple-text">all in one place.</h1>

            <p>
              Select a friend from the left to open your conversation.
              Your next great conversation starts here.
            </p>

            <div className="welcome-bottom-label">
              <span />
              CONNECTED TO CONVO
            </div>
          </div>
        )}
      </section>
    </main>
  )
}

export default ConversationPage
