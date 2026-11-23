import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'
import { useAuth } from '../context/AuthContext.jsx'

function formatDueDate(value) {
  if (!value) return 'No deadline'
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })
}

export default function MilestoneBoard() {
  const { id } = useParams()
  const { user } = useAuth()
  const client = user?.roles?.includes('CLIENT')
  const freelancer = user?.roles?.includes('FREELANCER')
  const [items, setItems] = useState([])
  const [form, setForm] = useState({ title: '', description: '', dueDate: '', amount: '', sequenceNumber: 1 })
  const [work, setWork] = useState({})
  const [workComments, setWorkComments] = useState({})
  const [progressNotes, setProgressNotes] = useState({})
  const [workFiles, setWorkFiles] = useState({})
  const [retryFiles, setRetryFiles] = useState({})
  const [comments, setComments] = useState({})
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function reload() { const { data } = await api.get(`/assignments/${id}/milestones`); setItems(data) }
  useEffect(() => { reload().catch((cause) => setError(apiError(cause))).finally(() => setLoading(false)) }, [id])

  async function create(event) {
    event.preventDefault(); setError('')
    try {
      await api.post(`/assignments/${id}/milestones`, { ...form, amount: Number(form.amount), sequenceNumber: Number(form.sequenceNumber), dueDate: form.dueDate || null })
      setForm({ title: '', description: '', dueDate: '', amount: '', sequenceNumber: items.length + 2 }); await reload()
    } catch (cause) { setError(apiError(cause)) }
  }

  async function submitWork(event, milestoneId) {
    event.preventDefault(); setError('')
    let createdSubmissionId = null
    try {
      const { data } = await api.post(`/milestones/${milestoneId}/submit`, { description: work[milestoneId], comments: workComments[milestoneId] || '' })
      const submission = data.submissions?.[0]
      createdSubmissionId = submission?.id || null
      if (createdSubmissionId) await uploadFiles(createdSubmissionId, workFiles[milestoneId] || [])
      setWork({ ...work, [milestoneId]: '' }); setWorkComments({ ...workComments, [milestoneId]: '' }); setWorkFiles({ ...workFiles, [milestoneId]: [] }); await reload()
    } catch (cause) { if (createdSubmissionId) await reload().catch(() => {}); setError(apiError(cause)) }
  }

  async function uploadFiles(submissionId, files) {
    for (const file of files) {
      const formData = new FormData(); formData.append('file', file)
      await api.post(`/submissions/${submissionId}/files`, formData)
    }
  }

  async function retrySubmissionFiles(event, submissionId) {
    event.preventDefault(); setError('')
    try {
      await uploadFiles(submissionId, retryFiles[submissionId] || [])
      setRetryFiles({ ...retryFiles, [submissionId]: [] })
      await reload()
    } catch (cause) { await reload().catch(() => {}); setError(apiError(cause)) }
  }

  async function startWorking(milestoneId) {
    setError('')
    try { await api.put(`/milestones/${milestoneId}/start`); await reload() }
    catch (cause) { setError(apiError(cause)) }
  }

  async function updateProgress(event, milestoneId) {
    event.preventDefault(); setError('')
    try {
      await api.post(`/milestones/${milestoneId}/progress`, { comment: progressNotes[milestoneId] })
      setProgressNotes({ ...progressNotes, [milestoneId]: '' })
      await reload()
    } catch (cause) { setError(apiError(cause)) }
  }

  async function action(milestoneId, kind) {
    setError('')
    try {
      if (kind === 'request-revision') await api.put(`/milestones/${milestoneId}/request-revision`, { comment: comments[milestoneId] })
      else await api.put(`/milestones/${milestoneId}/${kind}`)
      await reload()
    } catch (cause) { setError(apiError(cause)) }
  }

  const approvedCount = items.filter((item) => item.status === 'APPROVED').length
  const submittedCount = items.filter((item) => item.status === 'SUBMITTED').length
  const progress = items.length ? Math.round((approvedCount / items.length) * 100) : 0
  const plannedAmount = items.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  const approvedAmount = items.filter((item) => item.status === 'APPROVED').reduce((sum, item) => sum + Number(item.amount || 0), 0)

  if (loading) return <main className="shell state">Loading milestones…</main>
  return <main className="shell page">
    <Link to="/my/assignments" className="back-link">← Assignments</Link>
    <p className="eyebrow">Project delivery</p><h1>Milestones</h1>
    <Link className="secondary-button" to={`/messages?assignmentId=${id}`}>Discuss this project</Link>
    {error && <p className="error" role="alert">{error}</p>}
    <section className="milestone-progress" aria-label="Milestone progress">
      <div className="milestone-progress-heading"><div><h2>Project progress</h2><p>{approvedCount} of {items.length} milestones approved · {submittedCount} awaiting review</p></div><strong>{progress}%</strong></div>
      <div className="milestone-progress-track" role="progressbar" aria-valuenow={progress} aria-valuemin="0" aria-valuemax="100"><span style={{ width: `${progress}%` }}/></div>
      <p className="milestone-budget-progress">Approved {formatMoney(approvedAmount)} of {formatMoney(plannedAmount)} in planned milestones</p>
    </section>
    {items.length === 0 && !client && <section className="empty"><h2>Milestones are being planned</h2><p>The client has not added milestones yet.</p></section>}
    <div className="milestone-list">{items.map((item) => <article className="project-card" key={item.id}>
      <div className="project-meta"><span>Milestone {item.sequenceNumber}</span><span>{item.status.replaceAll('_', ' ')}</span></div>
      <h2>{item.title}</h2><p>{item.description}</p>
      <div className="project-bottom"><strong>{formatMoney(item.amount)}</strong><span>Due {formatDueDate(item.dueDate)}</span></div>
      {item.status === 'REVISION_REQUESTED' && <RevisionFeedback updates={item.progressUpdates || []}/>}
      {item.progressUpdates?.length > 0 && <details className="milestone-history"><summary>Progress and revision history ({item.progressUpdates.length})</summary>{item.progressUpdates.map((update, index) => <article key={`${item.id}-update-${index}`}><div><strong>{update.toStatus?.replaceAll('_', ' ')}</strong><span>{update.changedBy} · {new Date(update.changedAt).toLocaleString()}</span></div>{update.comment && <p>{update.comment}</p>}</article>)}</details>}
      {item.submissions?.map((submission) => <section className="submission" key={submission.id}>
        <b>Submitted by {submission.submittedBy}</b><p>{submission.description}</p>{submission.comments && <p className="submission-comments"><strong>Freelancer comments:</strong> {submission.comments}</p>}
        {submission.files?.map((file) => <FileDownload key={file.id} file={file}/>) }
        {freelancer && item.status === 'SUBMITTED' && item.submissions?.[0]?.id === submission.id && <form className="upload-row" onSubmit={(event) => retrySubmissionFiles(event, submission.id)}><label>Add or retry deliverable files<input type="file" multiple onChange={(event) => setRetryFiles({ ...retryFiles, [submission.id]: Array.from(event.target.files || [])})}/><small>PDF, TXT, PNG, JPG, DOC, DOCX or ZIP; max 10 MB each.</small></label><button className="secondary-button" disabled={!retryFiles[submission.id]?.length}>Upload files</button></form>}
      </section>)}
      {freelancer && item.status === 'PENDING' && <button className="button" onClick={() => startWorking(item.id)}>Start working</button>}
      {client && item.status === 'PENDING' && <p className="muted">Waiting for the assigned freelancer to start this milestone.</p>}
      {freelancer && ['IN_PROGRESS', 'REVISION_REQUESTED'].includes(item.status) && <>
        <form className="proposal-form" onSubmit={(e) => updateProgress(e, item.id)}>
          <label>Work progress update<textarea required maxLength="4000" rows="3" value={progressNotes[item.id] || ''} onChange={(e) => setProgressNotes({ ...progressNotes, [item.id]: e.target.value })} placeholder="Share what you have completed or what you are working on next."/></label>
          <button className="secondary-button">Post progress update</button>
        </form>
        <form className="proposal-form" onSubmit={(e) => submitWork(e, item.id)}>
        <label>Submit work<textarea required rows="4" value={work[item.id] || ''} onChange={(e) => setWork({ ...work, [item.id]: e.target.value })}/></label>
        <label>Comments for the client<textarea rows="3" maxLength="4000" value={workComments[item.id] || ''} onChange={(e) => setWorkComments({ ...workComments, [item.id]: e.target.value })} placeholder="Summarize the deliverable or note anything the client should review."/></label>
        <label>Deliverable files (optional)<input type="file" multiple onChange={(e) => setWorkFiles({ ...workFiles, [item.id]: Array.from(e.target.files || []) })}/><small>PDF, TXT, PNG, JPG, DOC, DOCX or ZIP; max 10 MB each.</small></label>
        <button className="button">Submit for review</button>
        </form>
      </>}
      {client && item.status === 'SUBMITTED' && <div className="review-actions">
        <button className="button" onClick={() => action(item.id, 'approve')}>Approve work</button>
        <label>Revision feedback<input value={comments[item.id] || ''} onChange={(e) => setComments({ ...comments, [item.id]: e.target.value })}/></label>
        <button className="secondary-button" onClick={() => action(item.id, 'request-revision')}>Request revision</button>
      </div>}
    </article>)}</div>
    {client && <form className="form-panel wide" onSubmit={create}>
      <h2>Add a milestone</h2><label>Title<input required maxLength="160" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })}/></label>
      <label>Description<textarea rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })}/></label>
      <div className="form-row"><label>Amount<input required type="number" min="0.01" step="0.01" value={form.amount} onChange={(e) => setForm({ ...form, amount: e.target.value })}/></label><label>Sequence<input required type="number" min="1" value={form.sequenceNumber} onChange={(e) => setForm({ ...form, sequenceNumber: e.target.value })}/></label></div>
      <label>Deadline<input required type="date" min={new Date().toISOString().slice(0, 10)} value={form.dueDate} onChange={(e) => setForm({ ...form, dueDate: e.target.value })}/></label><button className="button">Add milestone</button>
    </form>}
  </main>
}

function FileDownload({ file }) {
  const [error, setError] = useState('')
  async function download() {
    try { const { data } = await api.get(file.downloadUrl, { responseType: 'blob' }); const url = URL.createObjectURL(data); const a = document.createElement('a'); a.href = url; a.download = file.originalName; a.click(); URL.revokeObjectURL(url) }
    catch (cause) { setError(apiError(cause)) }
  }
  return <div><button className="file-row" onClick={download}>{file.originalName}<span>Download</span></button>{error && <small className="error">{error}</small>}</div>
}

function RevisionFeedback({ updates }) {
  const latest = [...updates].reverse().find((update) => update.toStatus === 'REVISION_REQUESTED' && update.comment)
  if (!latest) return null
  return <section className="revision-feedback"><strong>Client feedback for this revision</strong><p>{latest.comment}</p><small>{latest.changedBy} · {new Date(latest.changedAt).toLocaleString()}</small></section>
}
