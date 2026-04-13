import { Toaster } from 'react-hot-toast'
import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import AdminPage from './pages/AdminPage'
import AnalyticsPage from './pages/AnalyticsPage'
import FeedPage from './pages/FeedPage'
import LoginPage from './pages/LoginPage'
import MessagesPage from './pages/MessagesPage'
import NotFoundPage from './pages/NotFoundPage'
import ProfilePage from './pages/ProfilePage'
import MyProfilePage from './pages/MyProfilePage'

function App() {
  return (
    <>
      <Toaster position="top-right" />

      <Routes>
        <Route path="/login" element={<LoginPage />} />


        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<Navigate to="/feed" replace />} />
          <Route path="/feed" element={<FeedPage />} />
          <Route path="/messages" element={<MessagesPage />} />
          <Route path="/profile/:handle" element={<ProfilePage />} />
          <Route path="/my-profile" element={<MyProfilePage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
          <Route path="/admin" element={<AdminPage />} />
        </Route>

        <Route element={<ProtectedRoute allowedRoles={['CREATOR']} />}>
          <Route path="/analytics" element={<AnalyticsPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </>
  )
}

export default App
