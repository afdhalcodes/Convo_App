
function FriendsList({ conversations, currentUsername, onOpenConversation }) {
  const acceptedConversations = conversations.filter(
    (conversation) => conversation.status === 'ACCEPTED'
  )

  const outgoingRequests = conversations.filter(
    (conversation) =>
      conversation.status === 'PENDING' &&
      conversation.senderUsername === currentUsername
  )

  const getOtherUser = (conversation) => {
    if (conversation.senderUsername === currentUsername) {
      return {
        username: conversation.receiverUsername,
        email: conversation.receiverEmail,
      }
    }

    return {
      username: conversation.senderUsername,
      email: conversation.senderEmail,
    }
  }

  return (
    <section className="dashboard-card friends-card">
      <div className="section-heading">
        <div>
          <span className="section-eyebrow">YOUR CONNECTIONS</span>
          <h2>Friends</h2>
          <p>People you have connected with on Convo.</p>
        </div>

        <div className="count-badge">{acceptedConversations.length}</div>
      </div>

      {acceptedConversations.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">♧</div>
          <strong>No connections yet</strong>
          <p>Search for people and start connecting with them.</p>
        </div>
      ) : (
        <div className="people-list">
          {acceptedConversations.map((conversation) => {
            const user = getOtherUser(conversation)

            return (
              <div className="person-row" key={conversation.id}>
                <div className="user-avatar">
                  {user.username?.charAt(0).toUpperCase() || '?'}
                </div>

                <div className="user-details">
                  <strong>{user.username}</strong>
                  <span>{user.email}</span>
                </div>

                <button
                  className="chat-button"
                  type="button"
                  onClick={() => onOpenConversation(conversation)}
                >
                  Open chat <span>→</span>
                </button>
              </div>
            )
          })}
        </div>
      )}

      <div className="subsection-divider" />

      <div className="subsection-heading">
        <div>
          <h3>Sent invitations</h3>
          <p>Requests waiting for the other person.</p>
        </div>
        <span className="small-count">{outgoingRequests.length}</span>
      </div>

      {outgoingRequests.length === 0 ? (
        <p className="muted-empty">You have no pending sent invitations.</p>
      ) : (
        <div className="people-list">
          {outgoingRequests.map((conversation) => {
            const user = getOtherUser(conversation)

            return (
              <div className="person-row" key={conversation.id}>
                <div className="user-avatar pending-avatar">
                  {user.username?.charAt(0).toUpperCase() || '?'}
                </div>

                <div className="user-details">
                  <strong>{user.username}</strong>
                  <span>{user.email}</span>
                </div>

                <span className="pending-label">Pending</span>
              </div>
            )
          })}
        </div>
      )}
    </section>
  )
}

export default FriendsList
