
import { useState } from 'react'
import {
  searchUsers,
  sendConversationRequest,
} from '../../services/conversationService'

function UserSearch({ onRequestSent }) {
  const [query, setQuery] = useState('')
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(false)
  const [requestingUser, setRequestingUser] = useState(null)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState('')

  const handleSearch = async (event) => {
    event.preventDefault()

    if (query.trim().length < 2) {
      setMessage('Enter at least 2 characters to search.')
      setMessageType('error')
      return
    }

    setLoading(true)
    setMessage('')
    setUsers([])

    try {
      const results = await searchUsers(query.trim())
      setUsers(Array.isArray(results) ? results : [])

      if (!results || results.length === 0) {
        setMessage('No users found.')
        setMessageType('info')
      }
    } catch (error) {
      setMessage(error.message)
      setMessageType('error')
    } finally {
      setLoading(false)
    }
  }

  const handleSendRequest = async (user) => {
    setRequestingUser(user.id)
    setMessage('')

    try {
      const result = await sendConversationRequest(user.username)

      setMessage(
        result?.status === 'ACCEPTED'
          ? `You and ${user.username} are now connected!`
          : `Invitation sent to ${user.username}.`
      )
      setMessageType('success')

      if (onRequestSent) {
        onRequestSent(result)
      }
    } catch (error) {
      setMessage(error.message)
      setMessageType('error')
    } finally {
      setRequestingUser(null)
    }
  }

  return (
    <section className="dashboard-card search-card">
      <div className="section-heading">
        <div>
          <span className="section-eyebrow">DISCOVER PEOPLE</span>
          <h2>Find people</h2>
          <p>Search for someone using their username or email.</p>
        </div>
        <div className="section-icon">⌕</div>
      </div>

      <form className="search-form" onSubmit={handleSearch}>
        <div className="search-input-wrapper">
          <span>⌕</span>
          <input
            type="text"
            placeholder="Search username or email..."
            value={query}
            onChange={(event) => setQuery(event.target.value)}
          />
        </div>

        <button
          className="primary-button search-button"
          type="submit"
          disabled={loading}
        >
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {message && (
        <div className={`dashboard-message ${messageType}`}>
          {message}
        </div>
      )}

      {users.length > 0 && (
        <div className="search-results">
          {users.map((user) => (
            <div className="user-result" key={user.id}>
              <div className="user-avatar">
                {user.username?.charAt(0).toUpperCase() || '?'}
              </div>

              <div className="user-details">
                <strong>{user.username}</strong>
                <span>{user.email}</span>
              </div>

              <button
                className="outline-button"
                type="button"
                disabled={requestingUser === user.id}
                onClick={() => handleSendRequest(user)}
              >
                {requestingUser === user.id ? 'Sending...' : 'Connect'}
              </button>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

export default UserSearch
