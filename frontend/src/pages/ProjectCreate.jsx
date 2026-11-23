import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import api, { apiError } from '../api/client.js'

const initialForm = { title: '', description: '', projectType: 'FIXED_PRICE', budgetMin: '', budgetMax: '', experienceLevel: 'INTERMEDIATE', category: '', deadline: '', skills: '', publish: true }

export default function ProjectCreate() {
  const { id } = useParams()
  const editing = Boolean(id)
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [loading, setLoading] = useState(editing)

  useEffect(() => {
    if (!editing) return
    api.get(`/projects/${id}`).then(({ data }) => setForm({
      title: data.title, description: data.description, projectType: data.projectType,
      budgetMin: data.budgetMin, budgetMax: data.budgetMax, experienceLevel: data.experienceLevel || 'INTERMEDIATE',
      category: data.category || '', deadline: data.deadline || '', skills: (data.requiredSkills || []).join(', '),
      publish: data.status === 'OPEN'
    })).catch((cause) => setError(apiError(cause))).finally(() => setLoading(false))
  }, [editing, id])

  function change(key, value) { setForm((previous) => ({ ...previous, [key]: value })) }
  async function submit(event) {
    event.preventDefault(); setError(''); setBusy(true)
    const body = {
      title: form.title.trim(), description: form.description.trim(), projectType: form.projectType,
      budgetMin: Number(form.budgetMin), budgetMax: Number(form.budgetMax), experienceLevel: form.experienceLevel,
      category: form.category.trim(), deadline: form.deadline || null,
      requiredSkills: form.skills.split(',').map((skill) => skill.trim()).filter(Boolean), publish: form.publish
    }
    try {
      const { data } = editing ? await api.put(`/projects/${id}`, body) : await api.post('/projects', body)
      navigate(`/projects/${data.id}`)
    } catch (cause) { setError(apiError(cause)) } finally { setBusy(false) }
  }

  if (loading) return <main className="shell state">Loading project…</main>

  return <main className="shell page"><section className="form-panel wide"><p className="eyebrow">For clients</p><h1>{editing ? 'Edit project' : 'Post a project'}</h1><p className="muted">Set a clear scope so the right freelancers can respond.</p><form onSubmit={submit}>
    <label>Project title<input required maxLength="180" value={form.title} onChange={(e) => change('title', e.target.value)}/></label>
    <label>Description<textarea required rows="8" maxLength="12000" value={form.description} onChange={(e) => change('description', e.target.value)}/></label>
    <div className="form-row"><label>Payment type<select value={form.projectType} onChange={(e) => change('projectType', e.target.value)}><option value="FIXED_PRICE">Fixed price</option><option value="HOURLY">Hourly</option></select></label><label>Experience level<select value={form.experienceLevel} onChange={(e) => change('experienceLevel', e.target.value)}><option value="ENTRY">Entry level</option><option value="INTERMEDIATE">Intermediate</option><option value="EXPERT">Expert</option></select></label></div>
    <div className="form-row"><label>Minimum budget (₹)<input required type="number" min="0" step="0.01" value={form.budgetMin} onChange={(e) => change('budgetMin', e.target.value)}/></label><label>Maximum budget (₹)<input required type="number" min="0" step="0.01" value={form.budgetMax} onChange={(e) => change('budgetMax', e.target.value)}/></label></div>
    <div className="form-row"><label>Category<input maxLength="80" value={form.category} onChange={(e) => change('category', e.target.value)}/></label><label>Deadline<input required type="date" min={new Date().toISOString().slice(0, 10)} value={form.deadline} onChange={(e) => change('deadline', e.target.value)}/></label></div>
    <label>Required skills<input placeholder="Java, React, MySQL" value={form.skills} onChange={(e) => change('skills', e.target.value)}/><small>Separate skills with commas.</small></label>
    <label className="publish-toggle"><input type="checkbox" checked={form.publish} onChange={(e) => change('publish', e.target.checked)}/><span>Publish this project so freelancers can find it</span></label>
    {error && <p className="error" role="alert">{error}</p>}<button className="button" disabled={busy}>{busy ? 'Saving…' : editing ? 'Save changes' : form.publish ? 'Publish project' : 'Save draft'}</button>
  </form></section></main>
}
