import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'
import { useAuth } from '../context/AuthContext.jsx'

const emptyFilters = { skill: '', minExperience: '', maxExperience: '', minRate: '', maxRate: '', availability: 'ANY', minRating: '' }

export default function FreelancerDirectory() {
  const { user } = useAuth()
  const client = user?.roles?.includes('CLIENT')
  const [filters, setFilters] = useState(emptyFilters)
  const [profiles, setProfiles] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function search(event) {
    event?.preventDefault()
    setLoading(true); setError('')
    const params = { size: 30, sort: 'experienceYears,desc' }
    Object.entries(filters).forEach(([key, value]) => { if (value !== '' && value !== 'ANY') params[key] = value })
    try { const { data } = await api.get('/freelancers', { params }); setProfiles(data.content || []) }
    catch (cause) { setError(apiError(cause)) }
    finally { setLoading(false) }
  }
  useEffect(() => { search() }, [])
  function change(key, value) { setFilters((current) => ({ ...current, [key]: value })) }
  async function reset() {
    setFilters(emptyFilters); setLoading(true); setError('')
    try { const { data } = await api.get('/freelancers', { params: { size: 30, sort: 'experienceYears,desc' } }); setProfiles(data.content || []) }
    catch (cause) { setError(apiError(cause)) }
    finally { setLoading(false) }
  }

  const apiRoot = api.defaults.baseURL.replace(/\/api\/?$/, '')
  return <main className="shell page"><p className="eyebrow">Independent talent</p><h1>Meet skilled freelancers.</h1>
    {client ? <form className="talent-filters" onSubmit={search}>
      <label>Skill<input value={filters.skill} onChange={(e) => change('skill', e.target.value)} placeholder="Java, React, Python…"/></label>
      <label>Experience from (years)<input type="number" min="0" max="80" value={filters.minExperience} onChange={(e) => change('minExperience', e.target.value)}/></label>
      <label>Experience to (years)<input type="number" min="0" max="80" value={filters.maxExperience} onChange={(e) => change('maxExperience', e.target.value)}/></label>
      <label>Hourly rate from (₹)<input type="number" min="0" step="0.01" value={filters.minRate} onChange={(e) => change('minRate', e.target.value)}/></label>
      <label>Hourly rate to (₹)<input type="number" min="0" step="0.01" value={filters.maxRate} onChange={(e) => change('maxRate', e.target.value)}/></label>
      <label>Availability<select value={filters.availability} onChange={(e) => change('availability', e.target.value)}><option value="ANY">Any availability</option><option value="AVAILABLE">Available</option><option value="LIMITED">Limited</option></select></label>
      <label>Minimum rating<select value={filters.minRating} onChange={(e) => change('minRating', e.target.value)}><option value="">Any rating</option><option value="3">3 stars and up</option><option value="4">4 stars and up</option><option value="4.5">4.5 stars and up</option></select></label>
      <div className="filter-actions"><button className="button">Search freelancers</button><button type="button" className="secondary-button" onClick={reset}>Clear filters</button></div>
    </form> : <form className="search" onSubmit={search}><input aria-label="Filter by skill" value={filters.skill} onChange={(e) => change('skill', e.target.value)} placeholder="Filter by skill, such as React"/><button className="button">Search</button></form>}
    {loading && <p className="state">Loading profiles…</p>}{error && <p className="error">{error}</p>}
    {!loading && !error && profiles.length === 0 && <section className="empty"><h2>No freelancer profiles found</h2><p>Try widening your search filters.</p></section>}
    <div className="project-list">{profiles.map((profile) => <article className="project-card" key={profile.userId}>
      <div className="talent-heading">{profile.photoUrl && <img className="talent-avatar" src={`${apiRoot}${profile.photoUrl}`} alt=""/>}<div><div className="project-meta"><span>{profile.availability}</span><span>{profile.averageRating ? `★ ${profile.averageRating.toFixed(1)} (${profile.reviewCount})` : 'New profile'}</span></div><h2><Link to={`/freelancers/${profile.userId}`}>{profile.fullName}</Link></h2></div></div>
      {profile.headline && <b>{profile.headline}</b>}<p>{profile.description || 'This freelancer is setting up their profile.'}</p><div className="skill-tags">{profile.skills.map((skill) => <span key={skill}>{skill}</span>)}</div>
      <div className="project-bottom"><strong>{profile.hourlyRate == null ? 'Rate not set' : `${formatMoney(profile.hourlyRate)}/hour`}</strong><span>{profile.experienceYears ?? 0} years experience</span></div>
    </article>)}</div>
  </main>
}
