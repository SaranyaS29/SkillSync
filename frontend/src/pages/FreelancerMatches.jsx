import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'

export default function FreelancerMatches() {
  const { id } = useParams()
  const [project, setProject] = useState(null)
  const [matches, setMatches] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    Promise.all([api.get(`/projects/${id}`), api.get(`/projects/${id}/matches`)]).
      then(([projectResult, matchResult]) => { setProject(projectResult.data); setMatches(matchResult.data) }).
      catch((cause) => setError(apiError(cause))).
      finally(() => setLoading(false))
  }, [id])

  if (loading) return <main className="shell state">Finding suitable freelancers…</main>
  if (error) return <main className="shell page"><p className="error">{error}</p><Link to="/my/projects">Back to your projects</Link></main>
  return <main className="shell page"><Link className="back-link" to={`/projects/${id}`}>← Back to project</Link><p className="eyebrow">Skill and budget fit</p><h1>Freelancer matches</h1><p className="muted">Top matches for <strong>{project?.title}</strong>. Score weights: skills 70%, experience 20%, budget fit 10%.</p>
    {!matches.length && <section className="empty"><h2>No matching profiles yet</h2><p>Try again after freelancers add their skills, experience, and hourly rates.</p></section>}
    <div className="match-list">{matches.map((match) => <article className="project-card match-card" key={match.userId}><div className="match-heading"><div><h2><Link to={`/freelancers/${match.userId}`}>{match.fullName}</Link></h2>{match.headline && <p>{match.headline}</p>}</div><strong className="match-score">{match.score}%<small>match</small></strong></div><div className="match-meter"><span style={{ width: `${match.score}%` }}/></div><div className="match-breakdown"><span>Skills<strong>{match.skillScore}%</strong></span><span>Experience<strong>{match.experienceScore}%</strong></span><span>Budget<strong>{match.budgetScore}%</strong></span></div><p className="muted">{match.explanation}</p><div className="skill-tags">{match.skills.map((skill) => <span key={skill}>{skill}</span>)}</div><div className="project-bottom"><strong>{match.hourlyRate == null ? 'Rate not set' : `${formatMoney(match.hourlyRate)}/hour`}</strong><span>{match.experienceYears ?? 0} years experience</span></div></article>)}</div>
  </main>
}
