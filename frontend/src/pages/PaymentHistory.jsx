import { useEffect, useMemo, useState } from 'react'
import api, { apiError } from '../api/client.js'
import { formatMoney } from '../utils/formatMoney.js'
import { useAuth } from '../context/AuthContext.jsx'

const filters = ['ALL', 'PENDING', 'PAID', 'FAILED']

export default function PaymentHistory() {
  const { user } = useAuth()
  const isClient = user?.roles?.includes('CLIENT')
  const [projects, setProjects] = useState([])
  const [payments, setPayments] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  async function reload() {
    const [assignmentsResult, paymentsResult] = await Promise.all([
      api.get('/assignments/my'),
      api.get('/payments/my'),
    ])
    const assignments = assignmentsResult.data
    const withMilestones = await Promise.all(assignments.map(async (assignment) => {
      const { data } = await api.get(`/assignments/${assignment.id}/milestones`)
      return { ...assignment, milestones: data }
    }))
    setProjects(withMilestones)
    setPayments(paymentsResult.data)
  }

  useEffect(() => {
    reload().catch((cause) => setError(apiError(cause))).finally(() => setLoading(false))
  }, [])

  const paymentByMilestone = useMemo(() => new Map(payments.filter((payment) => payment.milestoneId != null).map((payment) => [payment.milestoneId, payment])), [payments])
  const visiblePayments = payments.filter((payment) => {
    if (filter === 'PENDING') return ['PENDING', 'PROCESSING'].includes(payment.status)
    if (filter === 'PAID') return payment.status === 'PAID'
    if (filter === 'FAILED') return payment.status === 'FAILED'
    return true
  })
  const assignedProjectValue = projects.reduce((sum, project) => sum + Number(project.agreedBudget || 0), 0)
  const pendingTotal = payments.filter((payment) => ['PENDING', 'PROCESSING'].includes(payment.status)).reduce((sum, payment) => sum + Number(payment.amount), 0)
  const paidTotal = payments.filter((payment) => payment.status === 'PAID').reduce((sum, payment) => sum + Number(payment.amount), 0)
  const failedTotal = payments.filter((payment) => payment.status === 'FAILED').reduce((sum, payment) => sum + Number(payment.amount), 0)

  async function checkout(id) {
    setError('')
    try { await api.post(`/payments/${id}/checkout`); await reload() }
    catch (cause) { setError(apiError(cause)) }
  }

  async function simulate(id) {
    setError('')
    try { await api.post(`/dev/payments/${id}/simulate-success`); await reload() }
    catch (cause) { setError(apiError(cause)) }
  }

  async function receipt(id) {
    setError('')
    try {
      const { data } = await api.get(`/payments/${id}/receipt`, { responseType: 'blob' })
      const url = URL.createObjectURL(data)
      const anchor = document.createElement('a')
      anchor.href = url
      anchor.download = `receipt-${id}.txt`
      anchor.click()
      URL.revokeObjectURL(url)
    } catch (cause) { setError(apiError(cause)) }
  }

  return <main className="shell page">
    <p className="eyebrow">Payment tracking</p>
    <h1>Payment management</h1>
    <p className="muted">Sandbox payment tracking only. These simulated transactions do not move real money.</p>
    {error && <p className="error" role="alert">{error}</p>}
    {loading && <p className="state">Loading payment details…</p>}
    {!loading && <>
      <section className="payment-summary" aria-label="Payment totals">
        <article><small>Total assigned project value</small><strong>{formatMoney(assignedProjectValue)}</strong></article>
        <article><small>Awaiting payment</small><strong>{formatMoney(pendingTotal)}</strong></article>
        <article><small>Successfully paid</small><strong>{formatMoney(paidTotal)}</strong></article>
        <article><small>Failed transactions</small><strong>{formatMoney(failedTotal)}</strong></article>
      </section>

      <h2>Project budgets and milestones</h2>
      {!projects.length && <section className="empty"><h2>No assigned projects yet</h2><p>Project budgets and approved milestone payments will appear here.</p></section>}
      <div className="payment-project-list">{projects.map((project) => {
        const planned = project.milestones.reduce((sum, milestone) => sum + Number(milestone.amount || 0), 0)
        return <article className="project-card payment-project" key={project.id}>
          <div className="project-meta"><span>{project.status}</span><span>{project.clientName} · {project.freelancerName}</span></div>
          <h2>{project.projectTitle}</h2>
          <div className="payment-budget-row"><span>Agreed project budget</span><strong>{formatMoney(project.agreedBudget)}</strong></div>
          <div className="payment-budget-row"><span>Milestones planned</span><strong>{formatMoney(planned)}</strong></div>
          <div className="payment-milestones">{project.milestones.map((milestone) => {
            const payment = paymentByMilestone.get(milestone.id)
            return <div className="payment-milestone" key={milestone.id}>
              <div><strong>{milestone.sequenceNumber}. {milestone.title}</strong><small>{milestone.status.replaceAll('_', ' ')} · Due {milestone.dueDate || 'not set'}</small></div>
              <strong>{formatMoney(milestone.amount)}</strong>
              {payment && <span className={`payment-status ${payment.status.toLowerCase()}`}>{payment.status === 'PAID' ? 'Paid' : payment.status === 'PROCESSING' ? 'Processing' : payment.status}</span>}
              {payment?.status === 'PENDING' && isClient && <button className="small-button button" onClick={() => checkout(payment.id)}>Initiate simulated payment</button>}
              {payment?.status === 'FAILED' && isClient && <button className="small-button button" onClick={() => checkout(payment.id)}>Retry simulated payment</button>}
              {payment?.status === 'PROCESSING' && <div className="payment-actions"><span className="muted">Sandbox checkout initiated</span>{isClient && import.meta.env.DEV && <button className="secondary-button" onClick={() => simulate(payment.id)}>Simulate successful payment</button>}</div>}
              {payment?.status === 'PAID' && <button className="secondary-button" onClick={() => receipt(payment.id)}>Download receipt</button>}
            </div>
          })}</div>
        </article>
      })}</div>

      <div className="page-heading payment-heading"><div><p className="eyebrow">Transactions</p><h2>Payment history</h2></div>
        <div className="payment-filters" aria-label="Filter payments">{filters.map((value) => <button key={value} className={filter === value ? 'active' : ''} onClick={() => setFilter(value)}>{value === 'ALL' ? 'All' : value === 'PAID' ? 'Successful' : value === 'FAILED' ? 'Failed' : 'Pending'}</button>)}</div>
      </div>
      {!visiblePayments.length && <section className="empty"><h2>No payments in this view</h2><p>Approving a milestone creates its pending payment record.</p></section>}
      <div className="payment-history-list">{visiblePayments.map((payment) => {
        const project = projects.find((item) => item.projectId === payment.projectId)
        const milestone = project?.milestones.find((item) => item.id === payment.milestoneId)
        return <article className="payment-history-row" key={payment.id}>
          <div><span className={`payment-status ${payment.status.toLowerCase()}`}>{payment.status === 'PAID' ? 'Successful' : payment.status}</span><h3>{project?.projectTitle || `Project #${payment.projectId}`}</h3><p>{milestone?.title || 'Project payment'} · Payment #{payment.id}</p></div>
          <strong>{formatMoney(payment.amount)}</strong>
          {payment.transactionReference && <small>{payment.transactionReference}</small>}
          {payment.status === 'PENDING' && isClient && <button className="button small-button" onClick={() => checkout(payment.id)}>Initiate simulated payment</button>}
          {payment.status === 'FAILED' && isClient && <button className="button small-button" onClick={() => checkout(payment.id)}>Retry simulated payment</button>}
          {payment.status === 'PROCESSING' && isClient && import.meta.env.DEV && <button className="secondary-button" onClick={() => simulate(payment.id)}>Simulate successful payment</button>}
          {payment.status === 'PAID' && <button className="secondary-button" onClick={() => receipt(payment.id)}>Download receipt</button>}
        </article>
      })}</div>
    </>}
  </main>
}
