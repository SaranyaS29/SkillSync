import { useEffect, useState } from 'react'
import api, { apiError } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function ReviewsPage() {
  const { user } = useAuth()
  const isClient = user?.roles?.includes('CLIENT')
  const isFreelancer = user?.roles?.includes('FREELANCER')
  const [assignments, setAssignments] = useState([]); const [received, setReceived] = useState([]); const [written, setWritten] = useState([])
  const [forms, setForms] = useState({}); const [error, setError] = useState(''); const [loading, setLoading] = useState(true)
  async function reload() {
    const [a, r, w] = await Promise.all([api.get('/assignments/my'), api.get('/reviews/my'), api.get('/reviews/written')])
    setAssignments(a.data.filter((x) => x.status === 'COMPLETED')); setReceived(r.data); setWritten(w.data)
  }
  useEffect(() => { reload().catch((e) => setError(apiError(e))).finally(() => setLoading(false)) }, [])
  async function submit(event, assignment) {
    event.preventDefault(); setError('')
    try { await api.post(`/assignments/${assignment.id}/reviews`, forms[assignment.id]); await reload() }
    catch (ex) { setError(apiError(ex)) }
  }
  const reviewed = new Set(written.map((r) => r.assignmentId))
  return <main className="shell page"><p className="eyebrow">Reputation</p><h1>Ratings and reviews</h1><p className="muted">After all project milestones are approved, clients can rate freelancer performance and freelancers can rate client communication and professionalism.</p>{error && <p className="error" role="alert">{error}</p>}{loading && <p className="state">Loading reviews…</p>}
    {(isClient || isFreelancer) && <><h2>Completed projects to review</h2>{assignments.filter((a) => !reviewed.has(a.id)).map((a) => <form className="review-form" key={a.id} onSubmit={(e) => submit(e, a)}><b>{a.projectTitle}</b><p className="muted">{isClient ? `Rate ${a.freelancerName}’s performance on this project.` : `Rate ${a.clientName} on communication and professionalism.`}</p><label>Rating<select required value={forms[a.id]?.rating || ''} onChange={(e) => setForms({ ...forms, [a.id]: { ...forms[a.id], rating: Number(e.target.value) } })}><option value="">Choose a rating</option>{[5, 4, 3, 2, 1].map((n) => <option value={n} key={n}>{n} {n === 1 ? 'star' : 'stars'}</option>)}</select></label><label>Feedback<textarea required maxLength="4000" rows="4" value={forms[a.id]?.feedback || ''} onChange={(e) => setForms({ ...forms, [a.id]: { ...forms[a.id], feedback: e.target.value } })} placeholder={isClient ? 'Share specific feedback about the freelancer’s work and collaboration.' : 'Share feedback about the client’s communication and professionalism.'}/></label><button className="button">Submit review</button></form>)}{!loading && assignments.length > 0 && assignments.every((a) => reviewed.has(a.id)) && <p className="success">You have reviewed every completed project.</p>}{!loading && assignments.length === 0 && <p className="muted">Completed projects will appear here when their milestones are approved.</p>}</>}
    <h2>Reviews you received</h2>{received.map((r) => <article className="submission" key={r.id}><b>{'★'.repeat(r.rating)} · {r.reviewerName}</b><p>{r.feedback}</p></article>)}{!loading && received.length === 0 && <p className="muted">No reviews received yet.</p>}
    <h2>Reviews you wrote</h2>{written.map((r) => <article className="submission" key={r.id}><b>{'★'.repeat(r.rating)} · {r.revieweeName}</b><p>{r.feedback}</p></article>)}
  </main>
}
