import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'

export default function ReceivedProposals() {
  const { id } = useParams()
  const [project, setProject] = useState(null)
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [savingId, setSavingId] = useState(null)

  async function reload() {
    const [projectResult, proposalResult] = await Promise.all([api.get(`/projects/${id}`), api.get(`/projects/${id}/proposals`)])
    setProject(projectResult.data); setItems(proposalResult.data)
  }
  useEffect(() => { reload().catch((cause) => setError(apiError(cause))).finally(() => setLoading(false)) }, [id])

  async function decide(proposalId, action) {
    setError(''); setSavingId(proposalId)
    try { await api.put(`/proposals/${proposalId}/${action}`); await reload() }
    catch (cause) { setError(apiError(cause)) }
    finally { setSavingId(null) }
  }

  if (loading) return <main className="shell state">Loading proposals…</main>
  const ordered = [...items].sort((a, b) => {
    const statusOrder = (a.status === 'PENDING' ? 0 : 1) - (b.status === 'PENDING' ? 0 : 1)
    return statusOrder || Number(a.proposedBudget) - Number(b.proposedBudget)
  })
  const pendingCount = items.filter((proposal) => proposal.status === 'PENDING').length

  return <main className="shell page"><Link to="/my/projects" className="back-link">← Your projects</Link><p className="eyebrow">Client workspace</p><h1>Proposals {project ? `for ${project.title}` : ''}</h1>
    <p className="muted">{pendingCount} proposal{pendingCount === 1 ? '' : 's'} awaiting your decision. Compare each freelancer’s profile, offer, and delivery time.</p>
    {error && <p className="error" role="alert">{error}</p>}
    {items.length === 0 ? <section className="empty"><h2>No proposals yet</h2><p>New proposals will appear here, and you’ll receive an in-app notification when a freelancer applies.</p></section> : <div className="table-wrap proposal-comparison"><table><thead><tr><th>Freelancer</th><th>Proposed budget</th><th>Delivery time</th><th>Cover letter</th><th>Status</th><th>Decision</th></tr></thead><tbody>{ordered.map((proposal) => <tr key={proposal.id}>
      <td><Link className="inline-link" to={`/freelancers/${proposal.freelancerId}`}>{proposal.freelancerName}</Link>{proposal.portfolioLinks?.length > 0 && <small className="proposal-portfolio">{proposal.portfolioLinks.map((url, index) => <a key={url} href={url} target="_blank" rel="noreferrer">Portfolio {index + 1}</a>)}</small>}</td>
      <td><strong>{formatMoney(proposal.proposedBudget)}</strong></td><td>{proposal.completionDays} days</td><td className="proposal-cover-letter">{proposal.coverLetter}</td><td><span className={`proposal-status ${proposal.status.toLowerCase()}`}>{proposal.status.replace('_', ' ')}</span></td>
      <td>{proposal.status === 'PENDING' && project?.status === 'OPEN' ? <div className="proposal-actions"><button className="button" disabled={savingId !== null} onClick={() => decide(proposal.id, 'accept')}>{savingId === proposal.id ? 'Saving…' : 'Accept'}</button><button className="secondary-button" disabled={savingId !== null} onClick={() => decide(proposal.id, 'reject')}>Reject</button></div> : '—'}</td>
    </tr>)}</tbody></table></div>}
  </main>
}
