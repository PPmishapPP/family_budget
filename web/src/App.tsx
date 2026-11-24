import { Navigate, Route, Routes } from 'react-router-dom'

import { RequireAuth } from './auth/RequireAuth.tsx'
import { AppLayout } from './components/AppLayout.tsx'
import { HistoryPage } from './pages/HistoryPage.tsx'
import { LoginPage } from './pages/LoginPage.tsx'
import { MediaPage } from './pages/MediaPage.tsx'
import { PaymentsPage } from './pages/PaymentsPage.tsx'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/payments" replace />} />
          <Route path="payments" element={<PaymentsPage />} />
          <Route path="history" element={<HistoryPage />} />
          <Route path="media" element={<MediaPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/payments" replace />} />
    </Routes>
  )
}

export default App
