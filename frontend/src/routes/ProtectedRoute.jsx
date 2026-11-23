import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ roles }) {
  const { user, loading } = useAuth()
  if (loading) return <main className="shell state">Checking your session…</main>
  if (!user) return <Navigate to="/login" replace />
  if (roles && !roles.some((role) => user.roles?.includes(role))) return <Navigate to="/projects" replace />
  return <Outlet />
}
