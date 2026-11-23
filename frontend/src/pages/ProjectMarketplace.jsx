import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'

const defaultFilters = { title: '', skill: '', minBudget: '', maxBudget: '', experienceLevel: '', projectType: '', sort: 'newest' }
const experienceLabels = { ENTRY: 'Entry level', INTERMEDIATE: 'Intermediate', EXPERT: 'Expert' }

function formatDate(value) {
  if (!value) return null
  const [year, month, day] = value.split('-').map(Number)
  return new Date(year, month - 1, day).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })
}

export default function ProjectMarketplace() {
  const [filters, setFilters] = useState(defaultFilters)
  const [applied, setApplied] = useState(defaultFilters)
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let active = true
    setLoading(true); setError('')
    const params = {
      q: applied.title.trim() || undefined,
      skill: applied.skill.trim() || undefined,
      minBudget: applied.minBudget || undefined,
      maxBudget: applied.maxBudget || undefined,
      experienceLevel: applied.experienceLevel || undefined,
      projectType: applied.projectType || undefined,
      page, size: 12,
      sort: `createdAt,${applied.sort === 'oldest' ? 'asc' : 'desc'}`,
    }
    api.get('/projects', { params })
      .then((response) => { if (active) setData(response.data) })
      .catch((cause) => { if (active) setError(apiError(cause)) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [applied, page])

  function change(key, value) { setFilters((current) => ({ ...current, [key]: value })) }
  function search(event) { event.preventDefault(); setPage(0); setApplied({ ...filters }) }
  function clearFilters() { setFilters(defaultFilters); setPage(0); setApplied({ ...defaultFilters }) }

  return <main className="shell page">
    <div className="page-heading"><div><p className="eyebrow">Open opportunities</p><h1>Find work that fits.</h1><p className="muted">Browse and filter projects posted by clients on SkillSync.</p></div></div>
    <form className="project-filters" onSubmit={search}>
      <label>Project title<input value={filters.title} onChange={(event) => change('title', event.target.value)} placeholder="Search project titles"/></label>
      <label>Technical skill<input value={filters.skill} onChange={(event) => change('skill', event.target.value)} placeholder="Java, React, Python…"/></label>
      <label>Minimum budget (₹)<input type="number" min="0" step="0.01" value={filters.minBudget} onChange={(event) => change('minBudget', event.target.value)}/></label>
      <label>Maximum budget (₹)<input type="number" min="0" step="0.01" value={filters.maxBudget} onChange={(event) => change('maxBudget', event.target.value)}/></label>
      <label>Experience level<select value={filters.experienceLevel} onChange={(event) => change('experienceLevel', event.target.value)}><option value="">Any experience</option><option value="ENTRY">Entry level</option><option value="INTERMEDIATE">Intermediate</option><option value="EXPERT">Expert</option></select></label>
      <label>Payment type<select value={filters.projectType} onChange={(event) => change('projectType', event.target.value)}><option value="">Fixed price or hourly</option><option value="FIXED_PRICE">Fixed price</option><option value="HOURLY">Hourly</option></select></label>
      <label>Sort by<select value={filters.sort} onChange={(event) => change('sort', event.target.value)}><option value="newest">Newest projects</option><option value="oldest">Oldest projects</option></select></label>
      <div className="project-filter-actions"><button className="button">Apply filters</button><button type="button" className="secondary-button" onClick={clearFilters}>Clear filters</button></div>
    </form>
    {loading && <p className="state">Loading projects…</p>}
    {error && <p className="error" role="alert">{error}</p>}
    {!loading && !error && data?.content?.length === 0 && <section className="empty"><h2>No open projects found</h2><p>Try changing or clearing your filters. New opportunities will appear here.</p></section>}
    {!loading && !error && data?.content?.length > 0 && <>
      <div className="project-list">{data.content.map((project) => <article className="project-card" key={project.id}>
        <div className="project-meta"><span>OPEN PROJECT</span><span>Posted {formatDate(project.createdAt.slice(0, 10))}</span></div>
        <h2><Link to={`/projects/${project.id}`}>{project.title}</Link></h2>
        <p>{project.description.length > 230 ? `${project.description.slice(0, 230)}…` : project.description}</p>
        {project.deadline && <p className="muted">Deadline: {formatDate(project.deadline)}</p>}
        {project.requiredSkills?.length > 0 && <div className="skill-tags">{project.requiredSkills.map((skill) => <span key={skill}>{skill}</span>)}</div>}
        <div className="project-bottom"><strong>{formatMoney(project.budgetMin)}–{formatMoney(project.budgetMax)}{project.projectType === 'HOURLY' ? ' / hour' : ''}</strong><span>{experienceLabels[project.experienceLevel] || 'All experience levels'} · {project.projectType === 'HOURLY' ? 'Hourly' : 'Fixed price'}</span></div>
      </article>)}</div>
      <div className="pagination"><button disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button><span>Page {page + 1} of {Math.max(data.totalPages, 1)}</span><button disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button></div>
    </>}
  </main>
}
