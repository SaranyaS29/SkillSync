import { useEffect, useState } from 'react'
import { Link, Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { apiError } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

function destination(user) {
  if (user?.roles?.some((role) => ['CLIENT', 'FREELANCER'].includes(role))) return '/dashboard'
  return '/projects'
}

export default function AuthPage({ mode }) {
  const creating = mode === 'register'
  const { user, login, register, logout } = useAuth()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const queryRole = searchParams.get('role')
  const preferredRole = ['CLIENT', 'FREELANCER'].includes(queryRole) ? queryRole : null
  const [form, setForm] = useState({ fullName: '', email: '', password: '', role: preferredRole || 'FREELANCER' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  useEffect(() => { if (preferredRole) setForm((current) => ({ ...current, role: preferredRole })) }, [preferredRole])
  if (user) return <Navigate to={destination(user)} replace />

  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true)
    try {
      const account = creating ? await register(form) : await login({ email: form.email, password: form.password })
      if (!creating && !account.roles?.includes(form.role)) {
        logout()
        throw new Error(`This account is not registered as a ${form.role === 'CLIENT' ? 'client' : 'freelancer'}. Choose the correct account type and try again.`)
      }
      navigate(destination(account))
    } catch (cause) { setError(apiError(cause)) } finally { setBusy(false) }
  }

  return <main className="shell"><section className="form-panel"><p className="eyebrow">{creating ? 'Start collaborating' : `Sign in as ${form.role === 'CLIENT' ? 'a client' : 'a freelancer'}`}</p><h1>{creating ? 'Create your account' : 'Welcome back'}</h1><p className="muted">{creating ? 'Join as a freelancer or a client.' : 'Sign in to continue to your workspace.'}</p>
    <form onSubmit={submit}>
      {creating && <label>Full name<input required maxLength="120" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })}/></label>}
      <label>Email<input required type="email" autoComplete="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })}/></label>
      <label>Password<input required type="password" minLength="10" autoComplete={creating ? 'new-password' : 'current-password'} value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })}/>{creating && <small>Use at least 10 characters.</small>}</label>
      <label>{creating ? 'I’m here to' : 'Account type'}<select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}><option value="FREELANCER">Find freelance work</option><option value="CLIENT">Hire independent talent</option></select></label>
      {error && <p className="error" role="alert">{error}</p>}
      <button className="button" disabled={busy}>{busy ? 'Please wait…' : creating ? 'Create account' : 'Log in'}</button>
    </form>{!creating && <p><Link className="inline-link" to="/forgot-password">Forgot password?</Link></p>}<p className="muted">{creating ? 'Already have an account?' : 'New to SkillSync?'} <Link className="inline-link" to={creating ? `/login?role=${form.role}` : `/register?role=${form.role}`}>{creating ? 'Log in' : 'Create an account'}</Link></p>
  </section></main>
}
