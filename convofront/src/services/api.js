
const API_URL = import.meta.env.VITE_API_URL || ''

export const apiRequest = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token')

  const headers = {
    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
    ...options.headers,
  }

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }

  const response = await fetch(`${API_URL}${endpoint}`, {
    ...options,
    headers,
  })

  if (response.status === 401) {
    localStorage.removeItem('token')
    window.dispatchEvent(new Event('convo:unauthorized'))
    throw new Error('Your session has expired. Please log in again.')
  }

  if (!response.ok) {
    const responseText = await response.text()

    let errorMessage = responseText || 'Something went wrong. Please try again.'

    try {
    const data = JSON.parse(responseText)
    errorMessage = data.message || data.error || errorMessage
    } catch {
    // The server returned plain-text error details.
    }

    throw new Error(errorMessage)
    }

  if (response.status === 204) {
    return null
  }

  const responseText = await response.text()

  if (!responseText) {
    return null
  }

  try {
    return JSON.parse(responseText)
  } catch {
    return responseText
  }
}
