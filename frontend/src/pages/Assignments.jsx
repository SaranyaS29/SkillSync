import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function Assignments() {
  const { user } = useAuth()
  const clientView = user?.roles?.includes('CLIENT')
  const [items, setItems] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    api.get('/assignments/my').then(({ data }) => setItems(data))
      .catch((cause) => setError(apiError(cause))).finally(() => setLoading(false))
  }, [])

  return <main className="shell page"><p className="eyebrow">Your work</p><h1>Your assignments</h1>
    <p className="muted">Review project requirements and deadlines, follow milestone progress, and communicate with each client.</p>
    {loading && <p className="state">Loading assignments…</p>}
    {error && <p className="error" role="alert">{error}</p>}
    {!loading && !error && items.length === 0 && <section className="empty"><h2>No assignments yet</h2><p>Projects appear here after a client accepts your proposal.</p></section>}
    <div className="project-list">{items.map((item) => <article className="project-card assignment-card" key={item.id}>
      <div className="project-meta"><span>{item.status}</span><span>Started {new Date(item.startedAt).toLocaleDateString()}</span></div>
      <h2><Link to={`/projects/${item.projectId}`}>{item.projectTitle}</Link></h2>
      {clientView ? <section className="assignment-client"><strong>Assigned freelancer</strong><p><Link className="inline-link" to={`/freelancers/${item.freelancerId}`}>{item.freelancerName}</Link></p></section> : <section className="assignment-client"><strong>Client details</strong><p>{item.clientName}{item.clientCompanyName ? ` · ${item.clientCompanyName}` : ''}</p>
        <div className="assignment-client-meta">{item.clientLocation && <span>{item.clientLocation}</span>}{item.clientWebsite && <a href={item.clientWebsite} target="_blank" rel="noreferrer">Company website ↗</a>}</div>
        {item.clientCompanyDescription && <details><summary>About the client</summary><p>{item.clientCompanyDescription}</p></details>}
      </section>}
      <div className="project-bottom"><strong>Agreed budget {formatMoney(item.agreedBudget)}</strong>
        <div className="project-actions"><Link className="inline-link" to={`/projects/${item.projectId}`}>Requirements &amp; deadline</Link><Link className="inline-link" to={`/assignments/${item.id}/milestones`}>Milestone progress →</Link><Link className="inline-link" to={`/messages?assignmentId=${item.id}`}>Message {clientView ? 'freelancer' : 'client'} →</Link>{item.status === 'COMPLETED' && <Link className="inline-link" to="/reviews">{clientView ? 'Rate freelancer →' : 'Rate client →'}</Link>}</div>
      </div>
    </article>)}</div>
  </main>
}
