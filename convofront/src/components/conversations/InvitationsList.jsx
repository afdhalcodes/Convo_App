
import { useState } from 'react'
import {
  acceptConversation,
  rejectConversation,
} from '../../services/conversationService'

function InvitationsList({ requests, onRequestUpdated }) {
  const [processingId, setProcessingId] = useState(null)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState('')

  const handleAction = async (conversationId, action) => {
    setProcessingId(conversationId)
    setMessage('')

    try {
      if (action === 'accept') {
        await acceptConversation(conversationId)
        setMessage('Invitation accepted successfully.')
      } else {
        await rejectConversation(conversationId)
        setMessage('Invitation rejected.')
      }

      setMessageType('success')
      await onRequestUpdated()
    } catch (error) {
      setMessage(error.message)
      setMessageType('error')
    } finally {
      setProcessingId(null)
    }
  }

  return (
    <section className="dashboard-card invitations-card">
      <div className="section-heading">
        <div>
          <span className="section-eyebrow">STAY CONNECTED</span>
          <h2>Invitations</h2>
          <p>People who want to connect with you.</p>
        </div>

        <div className="count-badge">
          {requests.length}
        </div>
      </div>

      {message && (
        <div className={`dashboard-message ${messageType}`}>
          {message}
        </div>
      )}

      {requests.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">✉</div>
          <strong>You're all caught up</strong>
          <p>New connection invitations will appear here.</p>
        </div>
      ) : (
        <div className="people-list">
          {requests.map((request) => (
            <div className="invitation-row" key={request.id}>
              <div className="person-row invitation-person">
                <div className="user-avatar">
                  {request.senderUsername?.charAt(0).toUpperCase() || '?'}
                </div>

                <div className="user-details">
                  <strong>{request.senderUsername}</strong>
                  <span>{request.senderEmail}</span>
                </div>
              </div>

              <div className="invitation-actions">
                <button
                  className="accept-button"
                  type="button"
                  disabled={processingId === request.id}
                  onClick={() => handleAction(request.id, 'accept')}
                >
                  {processingId === request.id ? 'Please wait...' : 'Accept'}
                </button>

                <button
                  className="reject-button"
                  type="button"
                  disabled={processingId === request.id}
                  onClick={() => handleAction(request.id, 'reject')}
                >
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </section>
  )
}

export default InvitationsList
