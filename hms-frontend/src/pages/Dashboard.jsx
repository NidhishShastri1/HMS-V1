import React, { useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import AdminPanel from '../components/AdminPanel';
import { Activity, Users, ShieldCheck, Cpu, TrendingUp } from 'lucide-react';

const Dashboard = () => {
    const { user } = useContext(AuthContext);
    const navigate = useNavigate();

    return (
        <div>
            <h2 style={{ marginBottom: '2rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
                System Dashboard
            </h2>

            <div className="stats-grid">
                <div className="stat-card">
                    <div className="flex-between">
                        <h3>System Status</h3>
                        <Activity color="var(--success)" size={20} />
                    </div>
                    <p style={{ color: 'var(--success)', fontSize: '1.2rem' }}>Online & Secure</p>
                </div>
                <div className="stat-card">
                    <div className="flex-between">
                        <h3>Active Network</h3>
                        <Cpu color="var(--brand-color)" size={20} />
                    </div>
                    <p style={{ fontSize: '1.2rem' }}>Offline LAN</p>
                </div>
                <div className="stat-card">
                    <div className="flex-between">
                        <h3>Clearance Level</h3>
                        <ShieldCheck color="#d29922" size={20} />
                    </div>
                    <p style={{ fontSize: '1.2rem', textTransform: 'uppercase' }}>{user.role}</p>
                </div>
            </div>

            {user.role === 'ADMIN' ? (
                <>
                    <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
                        <button className="btn" onClick={() => navigate('/billing')} style={{ width: 'auto', padding: '0.75rem 2rem', background: 'var(--brand-color)' }}>OPD Billing & Visits</button>
                        <button className="btn" onClick={() => navigate('/services')} style={{ width: 'auto', padding: '0.75rem 2rem' }}>Services Catalog</button>
                        <button className="btn" onClick={() => navigate('/patients')} style={{ width: 'auto', padding: '0.75rem 2rem' }}>Patient Registry</button>
                    </div>
                    <AdminPanel />
                </>
            ) : (
                <div style={{ background: 'var(--panel-bg)', padding: '2rem', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginBottom: '1rem' }}>
                        <Users size={32} color="var(--brand-color)" />
                        <h3 style={{ fontSize: '1.5rem' }}>{user.role === 'RECEPTION' ? 'Patient Registration Module' : 'Supervisory Overview'}</h3>
                    </div>
                    <p style={{ color: 'var(--text-secondary)' }}>
                        Welcome to the HMS Core. Your module is currently functioning within secure parameters.
                        All actions are being logged. No unauthorized transmissions detected.
                    </p>
                    <div style={{ marginTop: '2rem', display: 'flex', gap: '1rem' }}>
                        {user.role === 'DOCTOR' ? (
                            <button className="btn" onClick={() => navigate('/financial-dashboard')} style={{ width: 'auto', padding: '0.75rem 2rem', background: 'var(--brand-color)' }}>
                                <TrendingUp size={18} style={{ marginRight: '8px' }} /> Financial Oversight
                            </button>
                        ) : (
                            <button className="btn" onClick={() => navigate('/billing')} style={{ width: 'auto', padding: '0.75rem 2rem', background: 'var(--brand-color)' }}>Launch OPD Billing</button>
                        )}
                        <button className="btn btn-secondary" onClick={() => navigate('/patients')} style={{ width: 'auto', padding: '0.75rem 2rem' }}>Patient Registry</button>
                        {user.role === 'DOCTOR' && (
                            <button className="btn btn-secondary" onClick={() => navigate('/services')} style={{ width: 'auto', padding: '0.75rem 2rem' }}>Services Catalog</button>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default Dashboard;
