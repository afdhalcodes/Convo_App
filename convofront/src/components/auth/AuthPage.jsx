
import { useEffect, useState } from 'react'
import './AuthPage.css'

const API_URL = import.meta.env.VITE_API_URL || ''

function AuthPage() {
  const [page, setPage] = useState('login')

  const [loginData, setLoginData] = useState({
    identifier: '',
    password: '',
  })

  const [registerData, setRegisterData] = useState({
    username: '',
    email: '',
    password: '',
  })

  const [otp, setOtp] = useState('')
  const [verificationEmail, setVerificationEmail] = useState('')

  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState('')
  const [messageType, setMessageType] = useState('')
  const [resendTimer, setResendTimer] = useState(60)

  const [loggedIn, setLoggedIn] = useState(
    Boolean(localStorage.getItem('token'))
  )

  useEffect(() => {
    if (page !== 'verify' || resendTimer <= 0) {
      return
    }

    const timer = setTimeout(() => {
      setResendTimer((previous) => previous - 1)
    }, 1000)

    return () => clearTimeout(timer)
  }, [page, resendTimer])

  const showMessage = (text, type = 'error') => {
    setMessage(text)
    setMessageType(type)
  }

  const clearMessage = () => {
    setMessage('')
    setMessageType('')
  }

  const getErrorMessage = async (response) => {
    const responseText = await response.text()

    if (!responseText) {
      return 'Something went wrong. Please try again.'
    }

    try {
      const data = JSON.parse(responseText)
      return data.message || data.error || responseText
    } catch {
      return responseText
    }
  }

  const apiRequest = async (endpoint, data) => {
    const response = await fetch(`${API_URL}${endpoint}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(data),
    })

    if (!response.ok) {
      throw new Error(await getErrorMessage(response))
    }

    return response.text()
  }

  const handleLoginChange = (event) => {
    const { name, value } = event.target

    setLoginData((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  const handleRegisterChange = (event) => {
    const { name, value } = event.target

    setRegisterData((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  const handleLogin = async (event) => {
    event.preventDefault()
    clearMessage()
    setLoading(true)

    try {
      const token = await apiRequest('/api/auth/login', loginData)

      localStorage.setItem('token', token.trim())
      setLoggedIn(true)

      showMessage('Login successful! You are now signed in.', 'success')
    } catch (error) {
      showMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleRegister = async (event) => {
    event.preventDefault()
    clearMessage()
    setLoading(true)

    try {
      await apiRequest('/api/auth/register/request-otp', registerData)

      setVerificationEmail(registerData.email.trim())
      setOtp('')
      setResendTimer(60)
      setPage('verify')

      showMessage('Verification code sent to your email.', 'success')
    } catch (error) {
      showMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (event) => {
    event.preventDefault()
    clearMessage()

    if (!/^[0-9]{6}$/.test(otp)) {
      showMessage('Please enter a valid 6-digit OTP.')
      return
    }

    setLoading(true)

    try {
      await apiRequest('/api/auth/register/verify-otp', {
        email: verificationEmail,
        otp,
      })

      setPage('login')

      setLoginData({
        identifier: verificationEmail,
        password: '',
      })

      setRegisterData({
        username: '',
        email: '',
        password: '',
      })

      setOtp('')

      showMessage(
        'Email verified successfully! Your account has been created. Please log in.',
        'success'
      )
    } catch (error) {
      showMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const handleResendOtp = async () => {
    clearMessage()
    setLoading(true)

    try {
      await apiRequest('/api/auth/register/resend-otp', {
        email: verificationEmail,
      })

      setOtp('')
      setResendTimer(60)

      showMessage('A new verification code has been sent.', 'success')
    } catch (error) {
      showMessage(error.message)
    } finally {
      setLoading(false)
    }
  }

  const changePage = (nextPage) => {
    setPage(nextPage)
    clearMessage()
  }

  const handleLogout = () => {
    localStorage.removeItem('token')
    setLoggedIn(false)

    setLoginData({
      identifier: '',
      password: '',
    })

    showMessage('You have been logged out.', 'success')
  }

  return (
    <main className="auth-page">
      <div className="auth-background">
        <div className="glow glow-one" />
        <div className="glow glow-two" />
      </div>

      <header className="topbar">
        <button
          className="brand"
          onClick={() => changePage('login')}
          type="button"
        >
          <span className="brand-icon">C</span>
          <span>convo</span>
        </button>

        <span className="topbar-caption">
          Connect. Communicate. Collaborate.
        </span>
      </header>

      <section className="auth-content">
        <div className="auth-intro">
          <div className="eyebrow">
            <span className="status-dot" />
            YOUR SPACE TO CONNECT
          </div>

          <h1>
            Conversations
            <br />
            <span>start here.</span>
          </h1>

          <p>
            A place to connect with people, share ideas,
            and make every conversation count.
          </p>

          <div className="intro-decoration">
            <div className="decoration-card decoration-card-one">
              <span className="decoration-avatar">A</span>

              <div>
                <strong>Meaningful connections</strong>
                <small>Every conversation matters</small>
              </div>

              <span className="online-indicator" />
            </div>

            <div className="decoration-card decoration-card-two">
              <span className="decoration-symbol">✦</span>

              <div>
                <strong>One space. Many conversations.</strong>
                <small>Make your voice heard.</small>
              </div>
            </div>
          </div>
        </div>

        <div className="auth-card">
          {loggedIn ? (
            <div className="success-screen">
              <div className="success-icon">✓</div>

              <span className="form-eyebrow">WELCOME TO CONVO</span>

              <h2>You&apos;re signed in!</h2>

              <p>
                Your authentication was successful.
                You can now continue to the application.
              </p>

              <button
                className="primary-button"
                type="button"
                onClick={handleLogout}
              >
                Logout
                <span>→</span>
              </button>

              {message && (
                <div className={`message ${messageType}`}>
                  {message}
                </div>
              )}
            </div>
          ) : (
            <>
              {page === 'login' && (
                <div className="form-content">
                  <span className="form-eyebrow">WELCOME BACK</span>

                  <h2>Sign in</h2>

                  <p className="form-description">
                    Enter your details to access your account.
                  </p>

                  {message && (
                    <div className={`message ${messageType}`}>
                      {message}
                    </div>
                  )}

                  <form onSubmit={handleLogin}>
                    <div className="form-group">
                      <label htmlFor="identifier">
                        Email or username
                      </label>

                      <input
                        id="identifier"
                        type="text"
                        name="identifier"
                        placeholder="Enter your email or username"
                        value={loginData.identifier}
                        onChange={handleLoginChange}
                        autoComplete="username"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="login-password">
                        Password
                      </label>

                      <input
                        id="login-password"
                        type="password"
                        name="password"
                        placeholder="Enter your password"
                        value={loginData.password}
                        onChange={handleLoginChange}
                        autoComplete="current-password"
                        required
                      />
                    </div>

                    <button
                      className="primary-button"
                      type="submit"
                      disabled={loading}
                    >
                      {loading ? 'Signing in...' : 'Sign in'}
                      {!loading && <span>→</span>}
                    </button>
                  </form>

                  <div className="form-footer">
                    Don&apos;t have an account?

                    <button
                      type="button"
                      onClick={() => changePage('register')}
                    >
                      Create account
                    </button>
                  </div>
                </div>
              )}

              {page === 'register' && (
                <div className="form-content">
                  <span className="form-eyebrow">GET STARTED</span>

                  <h2>Create account</h2>

                  <p className="form-description">
                    Join Convo and start connecting with people.
                  </p>

                  {message && (
                    <div className={`message ${messageType}`}>
                      {message}
                    </div>
                  )}

                  <form onSubmit={handleRegister}>
                    <div className="form-group">
                      <label htmlFor="username">Username</label>

                      <input
                        id="username"
                        type="text"
                        name="username"
                        placeholder="Choose a username"
                        value={registerData.username}
                        onChange={handleRegisterChange}
                        minLength={3}
                        maxLength={30}
                        autoComplete="username"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="register-email">
                        Email address
                      </label>

                      <input
                        id="register-email"
                        type="email"
                        name="email"
                        placeholder="Enter your email address"
                        value={registerData.email}
                        onChange={handleRegisterChange}
                        autoComplete="email"
                        required
                      />
                    </div>

                    <div className="form-group">
                      <label htmlFor="register-password">
                        Password
                      </label>

                      <input
                        id="register-password"
                        type="password"
                        name="password"
                        placeholder="Create a password"
                        value={registerData.password}
                        onChange={handleRegisterChange}
                        minLength={8}
                        autoComplete="new-password"
                        required
                      />

                      <small className="input-hint">
                        Password must contain at least 8 characters.
                      </small>
                    </div>

                    <button
                      className="primary-button"
                      type="submit"
                      disabled={loading}
                    >
                      {loading ? 'Sending verification code...' : 'Create account'}
                      {!loading && <span>→</span>}
                    </button>
                  </form>

                  <div className="form-footer">
                    Already have an account?

                    <button
                      type="button"
                      onClick={() => changePage('login')}
                    >
                      Sign in
                    </button>
                  </div>
                </div>
              )}

              {page === 'verify' && (
                <div className="form-content verification-content">
                  <div className="mail-icon">✉</div>

                  <span className="form-eyebrow">EMAIL VERIFICATION</span>

                  <h2>Check your inbox</h2>

                  <p className="form-description">
                    We&apos;ve sent a 6-digit verification code to

                    <strong className="verification-email">
                      {verificationEmail}
                    </strong>
                  </p>

                  {message && (
                    <div className={`message ${messageType}`}>
                      {message}
                    </div>
                  )}

                  <form onSubmit={handleVerifyOtp}>
                    <div className="form-group">
                      <label htmlFor="otp">Verification code</label>

                      <input
                        id="otp"
                        className="otp-input"
                        type="text"
                        inputMode="numeric"
                        autoComplete="one-time-code"
                        placeholder="000000"
                        maxLength={6}
                        value={otp}
                        onChange={(event) => {
                          const value = event.target.value.replace(/\D/g, '')
                          setOtp(value)
                        }}
                        required
                      />
                    </div>

                    <button
                      className="primary-button"
                      type="submit"
                      disabled={loading || otp.length !== 6}
                    >
                      {loading ? 'Verifying...' : 'Verify email'}
                      {!loading && <span>→</span>}
                    </button>
                  </form>

                  <div className="resend-section">
                    <span>Didn&apos;t receive the code?</span>

                    {resendTimer > 0 ? (
                      <span className="resend-timer">
                        Resend available in {resendTimer}s
                      </span>
                    ) : (
                      <button
                        type="button"
                        onClick={handleResendOtp}
                        disabled={loading}
                      >
                        Resend code
                      </button>
                    )}
                  </div>

                  <button
                    className="back-button"
                    type="button"
                    onClick={() => changePage('register')}
                  >
                    ← Back to registration
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      </section>

      <footer className="page-footer">
        <span>© 2026 Convo. All rights reserved.</span>
        <span>Made for better conversations.</span>
      </footer>
    </main>
  )
}

export default AuthPage
