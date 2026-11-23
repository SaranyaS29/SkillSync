import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'
import ProjectFiles from '../components/ProjectFiles.jsx'
import { formatMoney } from '../utils/formatMoney.js'

export default function ProjectDetails() {
  const { id } = useParams()
  const { user, loading: authLoading } = useAuth()
  const [project, setProject] = useState(null)
  const [assignment, setAssignment] = useState(null)
  const [proposal, setProposal] = useState({ coverLetter: '', proposedBudget: '', completionDays: '', portfolioLinks: [] })
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    if (authLoading) return () => { active = false }
    async function load() {
      setLoading(true); setError('')
      try {
        const { data } = await api.get(`/projects/${id}`)
        let currentAssignment = null
        if (user?.roles?.some((role) => ['CLIENT', 'FREELANCER'].includes(role))) {
          const result = await api.get('/assignments/my')
          currentAssignment = result.data.find((item) => Number(item.projectId) === Number(id)) || null
        }
        if (active) { setProject(data); setAssignment(currentAssignment) }
      } catch (cause) { if (active) setError(apiError(cause)) }
      finally { if (active) setLoading(false) }
    }
    load()
    return () => { active = false }
  }, [id, user?.id, authLoading])

  async function submit(event) {
    event.preventDefault(); setError(''); setMessage('')
    try {
      await api.post(`/projects/${id}/proposals`, { ...proposal, proposedBudget: Number(proposal.proposedBudget), completionDays: Number(proposal.completionDays), portfolioLinks: proposal.portfolioLinks.map((link) => link.trim()).filter(Boolean) })
      setMessage('Your proposal has been sent. You can track its status from your proposals.')
    } catch (cause) { setError(apiError(cause)) }
  }
  async function cancelProject() {
    if (!window.confirm('Cancel this project? Pending proposals will be declined.')) return
    setError(''); setMessage('')
    try { const { data } = await api.put(`/projects/${id}/cancel`); setProject(data); setMessage('Project cancelled.') }
    catch (cause) { setError(apiError(cause)) }
  }

  if (loading) return <main className="shell state">Loading project…</main>
  if (!project) return <main className="shell state"><p className="error">{error || 'Project not found.'}</p><Link to="/projects">Back to projects</Link></main>

  const canPropose = user?.roles?.includes('FREELANCER') && project.status === 'OPEN'
  const isOwner = user?.roles?.includes('CLIENT') && project.clientId === user.id
  const isAssignedParticipant = Boolean(assignment && (assignment.clientId === user?.id || assignment.freelancerId === user?.id))
  const canViewFiles = project.status === 'OPEN' || isOwner || isAssignedParticipant
  const canUploadProjectFiles = isOwner && ['DRAFT', 'OPEN'].includes(project.status)
  const workspaceLinks = assignment && <div className="project-actions"><Link className="button" to={`/assignments/${assignment.id}/milestones`}>Open assignment workspace</Link><Link className="secondary-button" to={`/messages?assignmentId=${assignment.id}`}>Message project participant</Link></div>

  return <main className="shell page detail"><Link to="/projects" className="back-link">← All projects</Link><p className="eyebrow">{project.category || project.projectType.replace('_', ' ')}</p><h1>{project.title}</h1><div className="project-meta"><span>{project.status.replace('_', ' ')}</span><span>{project.experienceLevel || 'All experience levels'}</span></div>
    <div className="detail-grid"><article className="detail-copy"><h2>About this project</h2><p className="long-copy">{project.description}</p>
      {project.requiredSkills?.length > 0 && <><h2>Required skills</h2><div className="skill-tags">{project.requiredSkills.map((skill) => <span key={skill}>{skill}</span>)}</div></>}
      {project.deadline && <p><strong>Target deadline:</strong> {project.deadline}</p>}
      {canViewFiles && <ProjectFiles projectId={project.id} canManage={canUploadProjectFiles}/>}
      {user?.roles?.some((role) => ['CLIENT', 'FREELANCER'].includes(role)) && <Link className="report-link" to={`/report?projectId=${project.id}`}>Report this project</Link>}
    </article>
      <aside className="budget-box"><small>PROJECT BUDGET</small><strong>{formatMoney(project.budgetMin)} – {formatMoney(project.budgetMax)}</strong><span>{project.projectType === 'HOURLY' ? 'Per hour' : 'Fixed price'}</span>
        {canPropose ? <form onSubmit={submit} className="proposal-form"><h2>Send a proposal</h2><label>Your budget (₹)<input type="number" min="0.01" step="0.01" required value={proposal.proposedBudget} onChange={(event) => setProposal({ ...proposal, proposedBudget: event.target.value })}/></label><label>Estimated completion time (days)<input type="number" min="1" max="3650" required value={proposal.completionDays} onChange={(event) => setProposal({ ...proposal, completionDays: event.target.value })}/></label><label>Cover letter<textarea required maxLength="12000" rows="6" value={proposal.coverLetter} onChange={(event) => setProposal({ ...proposal, coverLetter: event.target.value })} placeholder="Explain why your skills and experience make you a good fit."/></label><div className="proposal-link-heading"><strong>Relevant portfolio links</strong><small>Optional · Add up to 10 links</small></div>{proposal.portfolioLinks.map((link, index) => <div className="proposal-link-input" key={index}><input aria-label={`Portfolio link ${index + 1}`} type="url" maxLength="2048" value={link} onChange={(event) => setProposal({ ...proposal, portfolioLinks: proposal.portfolioLinks.map((item, itemIndex) => itemIndex === index ? event.target.value : item) })} placeholder="https://example.com/your-work"/><button type="button" className="secondary-button" onClick={() => setProposal({ ...proposal, portfolioLinks: proposal.portfolioLinks.filter((_, itemIndex) => itemIndex !== index) })}>Remove</button></div>)}{proposal.portfolioLinks.length < 10 && <button type="button" className="secondary-button" onClick={() => setProposal({ ...proposal, portfolioLinks: [...proposal.portfolioLinks, ''] })}>Add portfolio link</button>}<button className="button" disabled={Boolean(message)}>{message ? 'Proposal submitted' : 'Submit proposal'}</button></form>
          : !user ? <Link className="button" to="/login">Log in to send a proposal</Link>
            : isOwner ? <><div className="project-actions">{['DRAFT', 'OPEN'].includes(project.status) && <><Link className="secondary-button" to={`/projects/${id}/matches`}>Find freelancer matches</Link><Link className="secondary-button" to={`/projects/${id}/edit`}>Edit project</Link></>}{project.status === 'OPEN' && <button className="secondary-button" onClick={cancelProject}>Cancel project</button>}</div>{project.status === 'OPEN' && <Link className="button" to={`/projects/${id}/proposals`}>Review proposals</Link>}{project.status !== 'OPEN' && workspaceLinks}</>
              : isAssignedParticipant ? <><p className="success">You are assigned to this project.</p>{workspaceLinks}</>
                : <p className="muted">Only open projects accept proposals.</p>}
        {error && <p className="error" role="alert">{error}</p>}{message && <p className="success" role="status">{message}</p>}
      </aside>
    </div>
  </main>
}
