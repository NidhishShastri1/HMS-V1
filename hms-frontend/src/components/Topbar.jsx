import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { ShieldAlert, LogOut, User } from 'lucide-react';

const Topbar = () => {
    const { user, logout } = useContext(AuthContext);
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="topbar">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <ShieldAlert color="var(--brand-color)" />
                <h1 style={{ fontSize: '1.2rem', margin: 0 }}>HMS Core</h1>
            </div>
            {user && (
                <div className="user-info">
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', background: 'rgba(0,0,0,0.2)', padding: '0.5rem 1rem', borderRadius: '20px' }}>
                        <User size={16} />
                        <span style={{ fontWeight: 'bold' }}>{user.username}</span>
                        <span style={{ fontSize: '0.8rem', color: 'var(--brand-color)', borderLeft: '1px solid var(--border-color)', paddingLeft: '0.5rem' }}>
                            {user.role}
                        </span>
                    </div>
                    <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '0.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', width: 'auto' }} title="Secure Logout">
                        <LogOut size={16} /> Logout
                    </button>
                </div>
            )}
        </div>
    );
};

export default Topbar;
