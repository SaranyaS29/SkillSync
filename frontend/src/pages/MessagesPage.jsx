import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function MessagesPage() {
  const { user } = useAuth()
  const [searchParams] = useSearchParams()
  const requestedAssignmentId = searchParams.get('assignmentId')
  const [assignments, setAssignments] = useState([])
  const [selected, setSelected] = useState(null)
  const [conversation, setConversation] = useState(null)
  const [items, setItems] = useState([])
  const [body, setBody] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const endOfMessages = useRef(null)
  const shouldScroll = useRef(true)

  useEffect(() => {
    api.get('/assignments/my').then(({ data }) => {
      setAssignments(data)
      const requested = data.find((assignment) => String(assignment.id) === requestedAssignmentId)
      if (requested) open(requested)
    })
      .catch((cause) => setError(apiError(cause))).finally(() => setLoading(false))
  }, [requestedAssignmentId])

  const refreshConversation = useCallback(async (conversationId) => {
    const { data } = await api.get(`/conversations/${conversationId}/messages`, { params: { size: 100 } })
    setItems(data.content.slice().reverse())
    await api.put(`/conversations/${conversationId}/read`)
  }, [])

  useEffect(() => {
    if (!conversation) return undefined
    let active = true
    const refresh = async () => {
      try {
        const { data } = await api.get(`/conversations/${conversation}/messages`, { params: { size: 100 } })
        if (active) setItems(data.content.slice().reverse())
        if (active) await api.put(`/conversations/${conversation}/read`)
      } catch (cause) { if (active) setError(apiError(cause)) }
    }
    refresh()
    const timer = window.setInterval(refresh, 5000)
    return () => { active = false; window.clearInterval(timer) }
  }, [conversation])

  useEffect(() => {
    if (shouldScroll.current) { endOfMessages.current?.scrollIntoView({ behavior: 'smooth' }); shouldScroll.current = false }
  }, [items])

  async function open(assignment) {
    shouldScroll.current = true
    setSelected(assignment); setConversation(null); setItems([]); setError('')
    try {
      const { data } = await api.post(`/projects/${assignment.projectId}/conversation`)
      setConversation(data.conversationId)
      await refreshConversation(data.conversationId)
    } catch (cause) { setError(apiError(cause)) }
  }

  async function send(event) {
    event.preventDefault()
    if (!body.trim() || !conversation || sending) return
    setSending(true); setError('')
    try {
      await api.post(`/conversations/${conversation}/messages`, { body: body.trim() })
      shouldScroll.current = true
      setBody('')
      await refreshConversation(conversation)
    } catch (cause) { setError(apiError(cause)) }
    finally { setSending(false) }
  }

  return <main className="shell page">
    <p className="eyebrow">Project communication</p><h1>Messages</h1>
    <p className="muted">Chat with the other participant on an assigned project. New messages refresh automatically.</p>
    {error && <p className="error" role="alert">{error}</p>}
    <div className="message-layout">
      <aside className="message-projects">
        {loading && <p className="muted">Loading assignments…</p>}
        {!loading && assignments.length === 0 && <p className="muted">Assigned project conversations will appear here.</p>}
        {assignments.map((assignment) => <button className={selected?.id === assignment.id ? 'selected' : ''} key={assignment.id} onClick={() => open(assignment)}>
          {assignment.projectTitle}<small>{assignment.clientName} · {assignment.freelancerName}</small>
        </button>)}
      </aside>
      <section className="message-panel">
        {!selected ? <p className="muted">Choose an assigned project to open its conversation.</p> : <>
          <div className="message-project-heading"><div><h2>{selected.projectTitle}</h2><small>{selected.clientName} · {selected.freelancerName}</small></div>
            <div className="message-project-links"><Link to={`/projects/${selected.projectId}`}>Project</Link><Link to={`/assignments/${selected.id}/milestones`}>Milestones</Link></div>
          </div>
          <div className="message-list">{items.map((message) => <article className={`message ${message.senderId === user?.id ? 'message-own' : ''}`} key={message.id}>
            <b>{message.senderName}</b><p>{message.body}</p><time>{new Date(message.sentAt).toLocaleString()}</time>
          </article>)}<div ref={endOfMessages}/></div>
          <form className="message-compose" onSubmit={send}><textarea required maxLength="5000" rows="3" value={body} onChange={(event) => setBody(event.target.value)} placeholder="Write a project-related message"/><button className="button" disabled={sending || !conversation}>{sending ? 'Sending…' : 'Send'}</button></form>
        </>}
      </section>
    </div>
  </main>
}
