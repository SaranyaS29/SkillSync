import { Link, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import { useEffect, useState } from 'react'
import { AuthProvider, useAuth } from './context/AuthContext.jsx'
import api from './api/client.js'
import ProtectedRoute from './routes/ProtectedRoute.jsx'
import AuthPage from './pages/AuthPage.jsx'
import ProjectMarketplace from './pages/ProjectMarketplace.jsx'
import ProjectDetails from './pages/ProjectDetails.jsx'
import ProjectCreate from './pages/ProjectCreate.jsx'
import MyWork from './pages/MyWork.jsx'
import ReceivedProposals from './pages/ReceivedProposals.jsx'
import Assignments from './pages/Assignments.jsx'
import MilestoneBoard from './pages/MilestoneBoard.jsx'
import PasswordResetPage from './pages/PasswordResetPage.jsx'
import FreelancerDirectory from './pages/FreelancerDirectory.jsx'
import FreelancerProfilePage from './pages/FreelancerProfilePage.jsx'
import EditProfile from './pages/EditProfile.jsx'
import NotificationsPage from './pages/NotificationsPage.jsx'
import PaymentHistory from './pages/PaymentHistory.jsx'
import ReviewsPage from './pages/ReviewsPage.jsx'
import MessagesPage from './pages/MessagesPage.jsx'
import ReportPage from './pages/ReportPage.jsx'
import RoleDashboard from './pages/RoleDashboard.jsx'
import ClientCompanyProfile from './pages/ClientCompanyProfile.jsx'
import ChangePasswordPage from './pages/ChangePasswordPage.jsx'
import FreelancerMatches from './pages/FreelancerMatches.jsx'

function Home() {
  return <main className="shell"><section className="hero"><p className="eyebrow">A better way to work together</p><h1>Great work starts<br/>with the right people.</h1><p className="intro">Find meaningful projects, build trusted partnerships, and keep every milestone moving.</p><div className="actions"><Link className="button" to="/login?role=FREELANCER">Find a project</Link><Link className="text-link" to="/login?role=CLIENT">Hire independent talent →</Link></div><div className="proof"><span>Project discovery</span><span>Clear milestones</span><span>Work you can track</span></div></section><section className="cards"><article><b>01</b><h2>Find your fit</h2><p>Discover projects and people by skills, budget, and working style.</p></article><article><b>02</b><h2>Agree on the work</h2><p>Set expectations with proposals, assignments, and clear milestones.</p></article><article><b>03</b><h2>Move forward together</h2><p>Share updates, submit work, and keep payment status in view.</p></article></section><footer>Built for independent work, from first proposal to final handoff.</footer></main>
}

function Navigation() {
  const { user, logout } = useAuth(); const location = useLocation()
  const [unreadNotifications, setUnreadNotifications] = useState(0)
  const roles = user?.roles || []
  const path = location.pathname
  const isHome = path === '/'
  const freelancer = roles.includes('FREELANCER')
  const client = roles.includes('CLIENT')
  useEffect(() => {
    if (!user || !roles.some((role) => ['CLIENT', 'FREELANCER'].includes(role))) { setUnreadNotifications(0); return undefined }
    let active = true
    const refresh = () => api.get('/notifications/unread-count').then(({ data }) => { if (active) setUnreadNotifications(data) }).catch(() => {})
    refresh()
    const timer = window.setInterval(refresh, 30000)
    window.addEventListener('skillsync-notifications-updated', refresh)
    return () => { active = false; window.clearInterval(timer); window.removeEventListener('skillsync-notifications-updated', refresh) }
  }, [user?.id])
  let items = []
  if (!user) {
    if (!isHome) {
      items = [['Home', '/']]
      if (path.startsWith('/projects')) items.push(['Projects', '/projects'])
      if (path.startsWith('/freelancers')) items.push(['Freelancers', '/freelancers'])
    }
  } else if (isHome) {
    items = []
  } else if (path === '/dashboard') {
    items = client
      ? [['Home', '/'], ['My projects', '/my/projects'], ['Find freelancers', '/freelancers'], ['Post project', '/projects/new'], ['Payment management', '/payments']]
      : [['Home', '/'], ['Find projects', '/projects'], ['My proposals', '/my/proposals'], ['Payment management', '/payments']]
  } else if (path === '/projects' || /^\/projects\/\d+(\/edit)?$/.test(path)) {
    items = [['Home', '/'], ['Projects', '/projects']]
    if (freelancer) items.push(['My proposals', '/my/proposals'])
    if (client) items.push(['My projects', '/my/projects'], ['Post project', '/projects/new'])
  } else if (path.startsWith('/projects/') && path.endsWith('/matches')) {
    items = [['Home', '/'], ['My projects', '/my/projects'], ['Freelancers', '/freelancers']]
  } else if (path.startsWith('/projects/') && path.endsWith('/proposals')) {
    items = [['Home', '/'], ['My projects', '/my/projects']]
  } else if (path === '/projects/new' || path === '/my/projects') {
    items = [['Home', '/'], ['My projects', '/my/projects'], ['Find freelancers', '/freelancers']]
  } else if (path === '/freelancers' || path.startsWith('/freelancers/')) {
    items = [['Home', '/'], ['Freelancers', '/freelancers']]
    if (client) items.push(['My projects', '/my/projects'], ['Post project', '/projects/new'])
    if (freelancer) items.push(['My proposals', '/my/proposals'])
  } else if (path === '/my/proposals') {
    items = [['Home', '/'], ['Find projects', '/projects'], ['My proposals', '/my/proposals']]
  } else if (path.startsWith('/assignments/')) {
    items = [['Home', '/'], ['Assignments', '/my/assignments'], ['Messages', '/messages'], ['Payment management', '/payments']]
  } else if (path === '/my/assignments') {
    items = [['Home', '/'], ['Assignments', '/my/assignments'], ['Messages', '/messages'], ['Payment management', '/payments']]
  } else if (path === '/messages') {
    items = [['Home', '/'], ['Assignments', '/my/assignments'], ['Messages', '/messages'], ['Payment management', '/payments'], ['Alerts', '/notifications']]
  } else if (path === '/notifications') {
    items = [['Home', '/'], ['Messages', '/messages'], ['Payment management', '/payments'], ['Alerts', '/notifications']]
  } else if (path === '/payments') {
    items = [['Home', '/'], ['Assignments', '/my/assignments'], ['Payments', '/payments']]
  } else if (path === '/reviews') {
    items = [['Home', '/'], ['Assignments', '/my/assignments'], ['Reviews', '/reviews'], ['Payment management', '/payments']]
  } else if (path === '/report') {
    items = [['Home', '/'], ['Dashboard', '/dashboard']]
  } else if (path === '/profile/edit' || path === '/company-profile' || path === '/change-password') {
    items = [['Home', '/'], ['Dashboard', '/dashboard']]
  } else {
    items = [['Home', '/'], ['Dashboard', '/dashboard']]
  }

  if (freelancer && !isHome && !items.some(([, href]) => href === '/messages')) {
    items.push(['Messages', '/messages'])
  }
  if ((client || freelancer) && !isHome && !items.some(([, href]) => href === '/reviews')) {
    items.push(['Ratings & reviews', '/reviews'])
  }

  return <header className="topbar"><nav className="shell nav-inner"><Link className="brand" to="/">SkillSync</Link><div className="nav-links">
    {items.map(([label, href], index) => <Link key={`${href}-${index}`} to={href} aria-label={href === '/notifications' && unreadNotifications ? `${label}, ${unreadNotifications} unread` : undefined}>{label}{href === '/notifications' && unreadNotifications > 0 && <span className="notification-badge">{unreadNotifications > 99 ? '99+' : unreadNotifications}</span>}</Link>)}
    {user && !isHome && (client || freelancer) && <details className="nav-account"><summary>Account</summary><div className="nav-account-menu">{client && <Link to="/company-profile">Company profile</Link>}{freelancer && <Link to={`/freelancers/${user.id}`}>View profile</Link>}<Link to="/profile/edit">Edit profile</Link><Link to="/change-password">Change password</Link></div></details>}
    {user ? <button className="nav-button" onClick={logout}>Log out</button> : path === '/login' ? <Link className="button small-button" to="/register">Join now</Link> : path === '/register' ? <Link to="/login" state={{ from: location.pathname }}>Log in</Link> : path === '/forgot-password' || path === '/reset-password' ? <Link to="/login">Log in</Link> : <><Link to="/login" state={{ from: location.pathname }}>Log in</Link><Link className="button small-button" to="/register">Join now</Link></>}
  </div></nav></header>
}

function AppRoutes() {
  return <><Navigation/><Routes>
    <Route path="/" element={<Home/>}/><Route path="/login" element={<AuthPage mode="login"/>}/><Route path="/register" element={<AuthPage mode="register"/>}/><Route path="/forgot-password" element={<PasswordResetPage/>}/><Route path="/reset-password" element={<PasswordResetPage reset/>}/>
    <Route path="/projects" element={<ProjectMarketplace/>}/><Route path="/projects/:id" element={<ProjectDetails/>}/><Route path="/freelancers" element={<FreelancerDirectory/>}/><Route path="/freelancers/:id" element={<FreelancerProfilePage/>}/>
    <Route element={<ProtectedRoute roles={['CLIENT','FREELANCER']}/> }><Route path="/profile/edit" element={<EditProfile/>}/><Route path="/change-password" element={<ChangePasswordPage/>}/><Route path="/notifications" element={<NotificationsPage/>}/><Route path="/payments" element={<PaymentHistory/>}/><Route path="/reviews" element={<ReviewsPage/>}/><Route path="/messages" element={<MessagesPage/>}/><Route path="/report" element={<ReportPage/>}/></Route>
    <Route element={<ProtectedRoute roles={['CLIENT']}/> }><Route path="/company-profile" element={<ClientCompanyProfile/>}/></Route>
    <Route element={<ProtectedRoute roles={['CLIENT','FREELANCER']}/> }><Route path="/dashboard" element={<RoleDashboard/>}/></Route>
    <Route element={<ProtectedRoute roles={['CLIENT']}/> }><Route path="/projects/new" element={<ProjectCreate/>}/><Route path="/projects/:id/edit" element={<ProjectCreate/>}/><Route path="/projects/:id/matches" element={<FreelancerMatches/>}/><Route path="/projects/:id/proposals" element={<ReceivedProposals/>}/><Route path="/my/projects" element={<MyWork kind="projects"/>}/></Route>
    <Route element={<ProtectedRoute roles={['FREELANCER']}/> }><Route path="/my/proposals" element={<MyWork kind="proposals"/>}/></Route>
    <Route element={<ProtectedRoute roles={['CLIENT','FREELANCER']}/> }><Route path="/my/assignments" element={<Assignments/>}/><Route path="/assignments/:id/milestones" element={<MilestoneBoard/>}/></Route>
    <Route path="*" element={<Navigate to="/projects" replace/>}/>
  </Routes></>
}

export default function App() { return <AuthProvider><AppRoutes/></AuthProvider> }
