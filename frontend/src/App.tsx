import { Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './layouts/MainLayout'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import UserList from './pages/UserList'
import CompanyList from './pages/CompanyList'
import InvoiceList from './pages/InvoiceList'
import Reconciliation from './pages/Reconciliation'
import PrivateRoute from './components/PrivateRoute'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/" element={<PrivateRoute><MainLayout /></PrivateRoute>}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<Dashboard />} />
        <Route path="users" element={<UserList />} />
        <Route path="companies" element={<CompanyList />} />
        <Route path="invoices" element={<InvoiceList />} />
        <Route path="reconciliation" element={<Reconciliation />} />
      </Route>
    </Routes>
  )
}

export default App