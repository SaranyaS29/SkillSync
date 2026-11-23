import { useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'

export default function ChangePasswordPage() {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)
  function update(key, value) { setForm((current) => ({ ...current, [key]: value })) }
  async function submit(event) {
    event.preventDefault(); setError(''); setSaved(false)
    if (form.newPassword !== form.confirmPassword) { setError('The new passwords do not match.'); return }
    try {
      await api.put('/auth/change-password', { currentPassword: form.currentPassword, newPassword: form.newPassword })
      setForm({ currentPassword: '', newPassword: '', confirmPassword: '' }); setSaved(true)
    } catch (e) { setError(apiError(e)) }
  }
  return <main className="shell page"><section className="form-panel"><p className="eyebrow">Account security</p><h1>Change password</h1><form onSubmit={submit}><label>Current password<input type="password" autoComplete="current-password" required value={form.currentPassword} onChange={(e) => update('currentPassword', e.target.value)}/></label><label>New password<input type="password" autoComplete="new-password" minLength={10} maxLength={72} required value={form.newPassword} onChange={(e) => update('newPassword', e.target.value)}/><small>Use 10–72 characters.</small></label><label>Confirm new password<input type="password" autoComplete="new-password" minLength={10} maxLength={72} required value={form.confirmPassword} onChange={(e) => update('confirmPassword', e.target.value)}/></label>{error && <p className="error">{error}</p>}{saved && <p className="success">Password changed successfully.</p>}<button className="button">Update password</button></form><Link className="inline-link" to="/dashboard">Back to dashboard</Link></section></main>
}
