import { useEffect, useState } from 'react'
import api, { apiError } from '../api/client.js'

export default function NotificationsPage() {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    const reload = () => api.get('/notifications', { params: { size: 50, sort: 'createdAt,desc' } })
      .then(({ data }) => { if (active) setItems(data.content) })
      .catch((cause) => { if (active) setError(apiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    reload()
    const timer = window.setInterval(reload, 30000)
    return () => { active = false; window.clearInterval(timer) }
  }, [])

  async function read(id) {
    setError('')
    try { await api.put(`/notifications/${id}/read`); const { data } = await api.get('/notifications', { params: { size: 50, sort: 'createdAt,desc' } }); setItems(data.content); window.dispatchEvent(new Event('skillsync-notifications-updated')) }
    catch (cause) { setError(apiError(cause)) }
  }

  return <main className="shell page"><p className="eyebrow">Updates</p><h1>Notifications</h1>
    <p className="muted">Project messages, revision updates, and deadline reminders appear here.</p>
    {error && <p className="error" role="alert">{error}</p>}
    {loading && <p className="state">Loading…</p>}
    {!loading && items.length === 0 && <section className="empty"><h2>You’re all caught up</h2><p>Project activity and messages will appear here.</p></section>}
    <div className="notice-list">{items.map((item) => <article className={`notice ${item.readAt ? 'read' : ''}`} key={item.id}>
      <div><small>{item.type.replaceAll('_', ' ')}</small><p>{item.message}</p><time>{new Date(item.createdAt).toLocaleString()}</time></div>
      {!item.readAt && <button className="secondary-button" onClick={() => read(item.id)}>Mark read</button>}
    </article>)}</div>
  </main>
}
