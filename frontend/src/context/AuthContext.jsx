import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import api from '../api/client.js'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem('currentUser') || 'null') } catch { return null }
  })
  const [loading, setLoading] = useState(Boolean(sessionStorage.getItem('accessToken')))

  const clearSession = useCallback(() => {
    sessionStorage.removeItem('accessToken')
    sessionStorage.removeItem('currentUser')
    setUser(null)
  }, [])

  useEffect(() => {
    if (!sessionStorage.getItem('accessToken')) return
    api.get('/auth/me').then(({ data }) => setUser(data)).catch(clearSession).finally(() => setLoading(false))
  }, [clearSession])

  const login = useCallback(async (credentials) => {
    const { data } = await api.post('/auth/login', credentials)
    sessionStorage.setItem('accessToken', data.accessToken)
    sessionStorage.setItem('currentUser', JSON.stringify(data.user))
    setUser(data.user)
    return data.user
  }, [])

  const register = useCallback(async (details) => {
    const { data } = await api.post('/auth/register', details)
    sessionStorage.setItem('accessToken', data.accessToken)
    sessionStorage.setItem('currentUser', JSON.stringify(data.user))
    setUser(data.user)
    return data.user
  }, [])

  const value = useMemo(() => ({ user, loading, login, register, logout: clearSession }), [user, loading, login, register, clearSession])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const value = useContext(AuthContext)
  if (!value) throw new Error('useAuth must be used inside AuthProvider')
  return value
}
