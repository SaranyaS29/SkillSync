import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'

const proposalStatusMeaning = {
  PENDING: 'The client has not decided yet.',
  ACCEPTED: 'The client selected you for this project.',
  REJECTED: 'The client rejected this proposal.',
  WITHDRAWN: 'You withdrew this proposal.',
}

export default function MyWork({ kind }) {
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const clientMode = kind === 'projects'
  const visibleItems = clientMode || statusFilter === 'ALL' ? items : items.filter((item) => item.status === statusFilter)

  async function reload() {
    const { data } = await api.get(clientMode ? '/projects/mine' : '/proposals/my')
    setItems(data.content || data)
  }
  useEffect(() => { reload().catch((cause) => setError(apiError(cause))).finally(() => setLoading(false)) }, [clientMode])

  async function withdraw(id) {
    setError('')
    try { await api.put(`/proposals/${id}/withdraw`); await reload() }
    catch (cause) { setError(apiError(cause)) }
  }
  async function cancelProject(id) {
    if (!window.confirm('Cancel this project? Pending proposals will be declined.')) return
    setError('')
    try { await api.put(`/projects/${id}/cancel`); await reload() }
    catch (cause) { setError(apiError(cause)) }
  }

  return <main className="shell page"><p className="eyebrow">Your workspace</p><div className="page-heading"><div><h1>{clientMode ? 'Projects you posted' : 'Your proposals'}</h1><p className="muted">{clientMode ? 'Review, update, and manage the projects you published.' : 'Track every proposal you have submitted and the client’s decision.'}</p></div>{clientMode && <Link className="button" to="/projects/new">Post a project</Link>}</div>
    {loading && <p className="state">Loading your {clientMode ? 'projects' : 'proposals'}…</p>}{error && <p className="error" role="alert">{error}</p>}
    {!clientMode && items.length > 0 && <><section className="proposal-status-legend">{Object.entries(proposalStatusMeaning).map(([status, meaning]) => <div key={status}><span className={`proposal-status ${status.toLowerCase()}`}>{status}</span><small>{meaning}</small></div>)}</section><div className="project-filter-actions proposal-status-filter"><label>Show status<select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}><option value="ALL">All proposals ({items.length})</option>{['PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN'].map((status) => <option value={status} key={status}>{status} ({items.filter((item) => item.status === status).length})</option>)}</select></label></div></>}
    {!loading && !error && items.length === 0 && <section className="empty"><h2>{clientMode ? 'No projects yet' : 'No proposals yet'}</h2><p>{clientMode ? 'Post your first project to hear from freelancers.' : 'Explore open projects and send your first proposal.'}</p><Link className="button" to={clientMode ? '/projects/new' : '/projects'}>{clientMode ? 'Post a project' : 'Explore projects'}</Link></section>}
    {!clientMode && !loading && items.length > 0 && visibleItems.length === 0 && <section className="empty"><h2>No proposals with this status</h2><p>Choose another status to see your submitted proposals.</p></section>}
    <div className="project-list">{visibleItems.map((item) => {
      const activeProject = clientMode && ['DRAFT', 'OPEN'].includes(item.status)
      const projectLink = clientMode || ['PENDING', 'ACCEPTED'].includes(item.status)
      return <article className="project-card" key={item.id}><div className="project-meta"><span>{clientMode ? item.projectType.replace('_', ' ') : <span className={`proposal-status ${item.status.toLowerCase()}`}>{item.status}</span>}</span><span>{clientMode ? item.status : `Submitted ${new Date(item.createdAt).toLocaleDateString()}`}</span></div><h2>{projectLink ? <Link to={`/projects/${clientMode ? item.id : item.projectId}`}>{clientMode ? item.title : item.projectTitle || `Project #${item.projectId}`}</Link> : (item.projectTitle || `Project #${item.projectId}`)}</h2><p>{clientMode ? item.description : item.coverLetter}</p>{!clientMode && <p className="proposal-meaning">{proposalStatusMeaning[item.status]}</p>}<div className="project-bottom"><strong>{clientMode ? `${formatMoney(item.budgetMin)}–${formatMoney(item.budgetMax)}` : `Your offer: ${formatMoney(item.proposedBudget)}`}</strong>{!clientMode && <span>{item.completionDays} days estimated</span>}</div>
        {!clientMode && item.portfolioLinks?.length > 0 && <div className="proposal-portfolio">{item.portfolioLinks.map((url, index) => <a key={`${item.id}-${url}`} href={url} target="_blank" rel="noreferrer">Portfolio link {index + 1} ↗</a>)}</div>}
        {clientMode && item.status === 'OPEN' && <Link className="inline-link" to={`/projects/${item.id}/proposals`}>Review proposals →</Link>}
        {activeProject && <div className="project-actions"><Link className="secondary-button" to={`/projects/${item.id}/matches`}>Find freelancer matches</Link><Link className="secondary-button" to={`/projects/${item.id}/edit`}>Edit</Link><button className="secondary-button" onClick={() => cancelProject(item.id)}>Cancel project</button></div>}
        {!clientMode && item.status === 'PENDING' && <button className="secondary-button" onClick={() => withdraw(item.id)}>Withdraw proposal</button>}
      </article>
    })}</div>
  </main>
}
