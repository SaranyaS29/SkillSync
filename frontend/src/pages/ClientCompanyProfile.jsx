import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api, { apiError } from '../api/client.js'

export default function ClientCompanyProfile() {
  const [profile, setProfile] = useState(null)
  const [error, setError] = useState('')
  useEffect(() => { api.get('/clients/profile').then(({ data }) => setProfile(data)).catch((e) => setError(apiError(e))) }, [])
  if (error) return <main className="shell page"><p className="error">{error}</p></main>
  if (!profile) return <main className="shell page state">Loading company profile…</main>
  return <main className="shell page"><section className="form-panel wide"><p className="eyebrow">Client account</p><h1>Company profile</h1><h2>{profile.companyName || 'Your company name'}</h2><p className="muted">{profile.companyDescription || 'Add a description so freelancers can learn about your company.'}</p><dl className="company-details"><div><dt>Account holder</dt><dd>{profile.fullName}</dd></div><div><dt>Email</dt><dd>{profile.email}</dd></div>{profile.website && <div><dt>Website</dt><dd><a className="inline-link" href={profile.website} target="_blank" rel="noreferrer">{profile.website}</a></dd></div>}{profile.location && <div><dt>Location</dt><dd>{profile.location}</dd></div>}</dl><Link className="button" to="/profile/edit">Edit company profile</Link></section></main>
}
