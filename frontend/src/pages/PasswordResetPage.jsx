import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'

export default function PasswordResetPage({ reset = false }) {
  const [params] = useSearchParams(); const [email, setEmail] = useState(''); const [password, setPassword] = useState('')
  const [message, setMessage] = useState(''); const [error, setError] = useState(''); const [busy, setBusy] = useState(false)
  async function submit(event) {
    event.preventDefault(); setError(''); setMessage(''); setBusy(true)
    try {
      if (reset) { await api.post('/auth/reset-password', { token: params.get('token') || '', newPassword: password }); setMessage('Your password has been updated. You can log in now.') }
      else { const { data } = await api.post('/auth/forgot-password', { email }); setMessage(data.message) }
    } catch (cause) { setError(apiError(cause)) } finally { setBusy(false) }
  }
  return <main className="shell"><section className="form-panel"><p className="eyebrow">Account help</p><h1>{reset ? 'Choose a new password' : 'Reset your password'}</h1><p className="muted">{reset ? 'Reset links expire after 30 minutes and can only be used once.' : 'We’ll send a one-time reset link if this email has an account.'}</p><form onSubmit={submit}>{reset ? <label>New password<input required minLength="10" maxLength="72" type="password" autoComplete="new-password" value={password} onChange={(e) => setPassword(e.target.value)}/></label> : <label>Email<input required type="email" value={email} onChange={(e) => setEmail(e.target.value)}/></label>}{error && <p className="error" role="alert">{error}</p>}{message && <p className="success" role="status">{message}</p>}<button className="button" disabled={busy}>{busy ? 'Please wait…' : reset ? 'Update password' : 'Send reset link'}</button></form><Link className="inline-link" to="/login">Back to log in</Link></section></main>
}
