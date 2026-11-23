import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'

export default function FreelancerProfilePage() {
  const { id } = useParams(); const [profile, setProfile] = useState(null); const [error, setError] = useState('')
  useEffect(() => { api.get(`/freelancers/${id}`).then(({ data }) => setProfile(data)).catch((e) => setError(apiError(e))) }, [id])
  const apiRoot = api.defaults.baseURL.replace(/\/api\/?$/, '')
  if (error) return <main className="shell page"><p className="error">{error}</p></main>
  if (!profile) return <main className="shell state">Loading profile…</main>
  return <main className="shell page"><Link className="back-link" to="/freelancers">← Freelancer directory</Link><p className="eyebrow">{profile.availability}</p><div className="talent-heading large">{profile.photoUrl && <img className="talent-avatar" src={`${apiRoot}${profile.photoUrl}`} alt=""/>}<div><h1>{profile.fullName}</h1>{profile.headline && <h2>{profile.headline}</h2>}</div></div><p className="muted">{profile.averageRating ? `★ ${profile.averageRating.toFixed(1)} from ${profile.reviewCount} reviews` : 'No reviews yet'} · {profile.experienceYears ?? 0} years experience</p>
    <div className="detail-grid"><section className="detail-copy"><h2>About</h2><p className="long-copy">{profile.description || 'No description provided yet.'}</p><h2>Skills</h2><div className="skill-tags">{profile.skills.map((s) => <span key={s}>{s}</span>)}</div><h2>Portfolio</h2>{profile.portfolioItems?.length ? profile.portfolioItems.map((item, i) => <article className="portfolio-card" key={`${item.title}-${i}`}><h3>{item.title}</h3><p>{item.description}</p>{item.url && <a className="inline-link" href={item.url} target="_blank" rel="noreferrer">View project →</a>}</article>) : <p className="muted">No portfolio items added yet.</p>}<h2>Reviews</h2><Reviews userId={profile.userId}/><Link className="report-link" to={`/report?userId=${profile.userId}`}>Report this freelancer</Link></section><aside className="budget-box"><small>HOURLY RATE</small><strong>{profile.hourlyRate == null ? 'Not set' : formatMoney(profile.hourlyRate)}</strong><span>{profile.location || 'Location not provided'}</span></aside></div>
  </main>
}

function Reviews({ userId }) {
  const [data, setData] = useState(null)
  useEffect(() => { api.get(`/users/${userId}/reviews`).then((r) => setData(r.data)).catch(() => setData({ reviews: [] })) }, [userId])
  if (!data) return <p>Loading reviews…</p>
  return data.reviews.length ? data.reviews.map((r) => <article className="submission" key={r.id}><b>{'★'.repeat(r.rating)} · {r.reviewerName}</b><p>{r.feedback}</p></article>) : <p className="muted">No reviews yet.</p>
}
