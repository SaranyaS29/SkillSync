import { useEffect, useState } from 'react'
import api, { apiError } from '../api/client.js'
import { useAuth } from '../context/AuthContext.jsx'

export default function EditProfile() {
  const { user } = useAuth()
  const freelancer = user?.roles?.includes('FREELANCER')
  const [form, setForm] = useState({})
  const [error, setError] = useState('')
  const [saved, setSaved] = useState(false)
  const [loading, setLoading] = useState(true)
  useEffect(() => {
    const path = freelancer ? `/freelancers/${user.id}` : '/clients/profile'
    api.get(path).then(({ data }) => setForm(freelancer ? { ...data, skills: data.skills.join(', ') } : data)).catch((e) => setError(apiError(e))).finally(() => setLoading(false))
  }, [freelancer, user.id])
  function set(key, value) { setForm((old) => ({ ...old, [key]: value })) }
  async function submit(event) {
    event.preventDefault(); setError(''); setSaved(false)
    try {
      const body = freelancer ? {
        headline: form.headline || '', description: form.description || '',
        hourlyRate: form.hourlyRate === '' || form.hourlyRate == null ? null : Number(form.hourlyRate),
        experienceYears: form.experienceYears === '' || form.experienceYears == null ? null : Number(form.experienceYears),
        availability: form.availability || 'AVAILABLE', location: form.location || '',
        skills: (form.skills || '').split(',').map((skill) => skill.trim()).filter(Boolean),
        portfolioItems: form.portfolioItems || [],
      } : form
      const { data } = await api.put(freelancer ? '/freelancers/profile' : '/clients/profile', body)
      setForm(freelancer ? { ...data, skills: data.skills.join(', ') } : data); setSaved(true)
    } catch (ex) { setError(apiError(ex)) }
  }
  function setPortfolio(index, key, value) { setForm((old) => ({ ...old, portfolioItems: old.portfolioItems.map((item, i) => i === index ? { ...item, [key]: value } : item) })) }
  async function downloadResume() {
    try {
      const { data, headers } = await api.get('/freelancers/profile/resume', { responseType: 'blob' })
      const disposition = headers['content-disposition'] || ''
      const filename = disposition.match(/filename\*=UTF-8''([^;]+)/i)?.[1]
        ? decodeURIComponent(disposition.match(/filename\*=UTF-8''([^;]+)/i)[1])
        : disposition.match(/filename="?([^";]+)"?/i)?.[1] || 'resume'
      const url = URL.createObjectURL(data); const a = document.createElement('a'); a.href = url; a.download = filename; a.click(); URL.revokeObjectURL(url)
    }
    catch (e) { setError(apiError(e)) }
  }
  if (loading) return <main className="shell state">Loading profile…</main>
  return <main className="shell page"><section className="form-panel wide"><p className="eyebrow">Your profile</p><h1>{freelancer ? 'Freelancer' : 'Client'} profile</h1>
    <form onSubmit={submit}>
      {freelancer ? <>
        <label>Professional headline<input maxLength="160" value={form.headline || ''} onChange={(e) => set('headline', e.target.value)}/></label>
        <label>About<textarea rows="6" maxLength="12000" value={form.description || ''} onChange={(e) => set('description', e.target.value)}/></label>
        <label>Skills<input value={form.skills || ''} onChange={(e) => set('skills', e.target.value)}/><small>Separate skills with commas.</small></label>
        <div className="form-row"><label>Hourly rate<input type="number" min="0" step="0.01" value={form.hourlyRate ?? ''} onChange={(e) => set('hourlyRate', e.target.value)}/></label><label>Experience in years<input type="number" min="0" max="80" value={form.experienceYears ?? ''} onChange={(e) => set('experienceYears', e.target.value)}/></label></div>
        <div className="form-row"><label>Availability<select value={form.availability || 'AVAILABLE'} onChange={(e) => set('availability', e.target.value)}><option>AVAILABLE</option><option>LIMITED</option><option>UNAVAILABLE</option></select></label><label>Location<input maxLength="120" value={form.location || ''} onChange={(e) => set('location', e.target.value)}/></label></div>
        <h2>Portfolio</h2>{(form.portfolioItems || []).map((item, index) => <div className="portfolio-edit" key={index}><label>Title<input maxLength="160" value={item.title} onChange={(e) => setPortfolio(index, 'title', e.target.value)}/></label><label>Description<textarea maxLength="4000" value={item.description || ''} onChange={(e) => setPortfolio(index, 'description', e.target.value)}/></label><label>Link<input type="url" maxLength="2048" value={item.url || ''} onChange={(e) => setPortfolio(index, 'url', e.target.value)}/></label><button type="button" className="secondary-button" onClick={() => set('portfolioItems', form.portfolioItems.filter((_, i) => i !== index))}>Remove portfolio item</button></div>)}
        {(form.portfolioItems || []).length < 20 && <button type="button" className="secondary-button" onClick={() => set('portfolioItems', [...(form.portfolioItems || []), { title: '', description: '', url: '' }])}>Add portfolio item</button>}
      </> : <>
        <label>Company name<input maxLength="160" value={form.companyName || ''} onChange={(e) => set('companyName', e.target.value)}/></label>
        <label>Company description<textarea rows="6" maxLength="12000" value={form.companyDescription || ''} onChange={(e) => set('companyDescription', e.target.value)}/></label>
        <div className="form-row"><label>Website<input type="url" maxLength="255" value={form.website || ''} onChange={(e) => set('website', e.target.value)}/></label><label>Location<input maxLength="120" value={form.location || ''} onChange={(e) => set('location', e.target.value)}/></label></div>
      </>}
      {error && <p className="error">{error}</p>}{saved && <p className="success">Profile saved.</p>}<button className="button">Save profile</button>
    </form>
    {freelancer && <ProfileAssets profile={form} onError={setError} onUpdate={setForm} onDownloadResume={downloadResume}/>}
  </section></main>
}

function ProfileAssets({ profile, onError, onUpdate, onDownloadResume }) {
  const [photo, setPhoto] = useState(null); const [resume, setResume] = useState(null); const [message, setMessage] = useState('')
  async function upload(path, file, kind) {
    if (!file) return
    const body = new FormData(); body.append('file', file); onError(''); setMessage('')
    try { await api.post(path, body); setMessage(`${kind} uploaded.`); const { data } = await api.get(`/freelancers/${profile.userId}`); onUpdate((old) => ({ ...old, photoUrl: data.photoUrl, hasResume: data.hasResume })); if (kind === 'photo') setPhoto(null); else setResume(null) }
    catch (e) { onError(apiError(e)) }
  }
  const root = api.defaults.baseURL.replace(/\/api\/?$/, '')
  return <section className="assets-panel"><h2>Photo and resume</h2>{profile.photoUrl && <img className="profile-photo-preview" src={`${root}${profile.photoUrl}`} alt="Profile"/>}<label>Profile picture<input type="file" accept="image/png,image/jpeg" onChange={(e) => setPhoto(e.target.files?.[0] || null)}/><small>PNG or JPG, up to 10 MB.</small></label><button type="button" className="secondary-button" onClick={() => upload('/freelancers/profile/photo', photo, 'Profile photo')}>Upload photo</button><label>Resume<input type="file" accept=".pdf,.doc,.docx" onChange={(e) => setResume(e.target.files?.[0] || null)}/><small>PDF, DOC, or DOCX, up to 10 MB.</small></label><div className="button-row"><button type="button" className="secondary-button" onClick={() => upload('/freelancers/profile/resume', resume, 'Resume')}>Upload resume</button>{profile.hasResume && <button type="button" className="secondary-button" onClick={onDownloadResume}>Download current resume</button>}</div>{message && <p className="success">{message}</p>}</section>
}
