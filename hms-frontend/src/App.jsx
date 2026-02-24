import React, { useContext, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { AuthProvider, AuthContext } from './context/AuthContext';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ForcePasswordChange from './components/ForcePasswordChange';
import Topbar from './components/Topbar';
import Patients from './pages/Patients';

const AutoLogout = ({ children }) => {
    const { logout } = useContext(AuthContext);
    const navigate = useNavigate();

    useEffect(() => {
        let timeout;
        const resetTimer = () => {
            clearTimeout(timeout);
            // Auto logout after 20 minutes
            timeout = setTimeout(() => {
                logout();
                navigate('/login');
            }, 20 * 60 * 1000);
        };

        window.addEventListener('mousemove', resetTimer);
        window.addEventListener('keypress', resetTimer);
        window.addEventListener('click', resetTimer);
        window.addEventListener('scroll', resetTimer);

        resetTimer();

        return () => {
            clearTimeout(timeout);
            window.removeEventListener('mousemove', resetTimer);
            window.removeEventListener('keypress', resetTimer);
            window.removeEventListener('click', resetTimer);
            window.removeEventListener('scroll', resetTimer);
        };
    }, [logout, navigate]);

    return children;
};

const ProtectedRoute = ({ children, allowedRoles }) => {
    const { user, loading } = useContext(AuthContext);

    if (loading) return <div>Loading...</div>;
    if (!user) return <Navigate to="/login" replace />;

    if (user.forcePasswordChange) {
        return (
            <div className="app-container">
                <Topbar />
                <AutoLogout>
                    <ForcePasswordChange />
                </AutoLogout>
            </div>
        );
    }

    if (allowedRoles && !allowedRoles.includes(user.role)) {
        return <Navigate to="/dashboard" replace />;
    }

    return (
        <div className="app-container">
            <Topbar />
            <AutoLogout>
                <div className="main-content">
                    {children}
                </div>
            </AutoLogout>
        </div>
    );
};

function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route
                path="/dashboard"
                element={
                    <ProtectedRoute>
                        <Dashboard />
                    </ProtectedRoute>
                }
            />
            <Route
                path="/patients"
                element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'RECEPTION']}>
                        <Patients />
                    </ProtectedRoute>
                }
            />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
        </Routes>
    );
}

function App() {
    return (
        <Router>
            <AuthProvider>
                <AppRoutes />
            </AuthProvider>
        </Router>
    );
}

export default App;
