
import { useEffect, useState } from 'react'
import AuthPage from './components/auth/AuthPage'
import ConversationPage from './pages/ConversationPage'

function App() {
  const [token, setToken] = useState(
    () => localStorage.getItem('token')
  )

  useEffect(() => {
    const handleUnauthorized = () => {
      localStorage.removeItem('token')
      setToken(null)
    }

    window.addEventListener('convo:unauthorized', handleUnauthorized)

    return () => {
      window.removeEventListener(
        'convo:unauthorized',
        handleUnauthorized
      )
    }
  }, [])

  const handleLogin = (newToken) => {
    setToken(newToken)
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    setToken(null)
  }

  return token ? (
    <ConversationPage onLogout={handleLogout} />
  ) : (
    <AuthPage onLogin={handleLogin} />
  )
}

export default App
