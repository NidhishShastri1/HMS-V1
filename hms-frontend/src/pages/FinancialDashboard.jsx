import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import {
    TrendingUp,
    CreditCard,
    AlertTriangle,
    Settings,
    UserCheck,
    RefreshCw,
    Wallet
} from 'lucide-react';

const FinancialDashboard = () => {
    const [stats, setStats] = useState({
        todaysTotal: 0,
        paymentModeBreakdown: {},
        totalAdjustments: 0,
        totalOverrides: 0,
        integrityViolations: 0
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const fetchStats = async () => {
        try {
            setLoading(true);
            const response = await api.get('/financial-dashboard/stats');
            setStats(response.data);
            setError(null);
        } catch (err) {
            setError('Failed to fetch financial data. Unauthorized or network error.');
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchStats();
    }, []);

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '60vh' }}>
                <div className="flex-center" style={{ gap: '1rem' }}>
                    <RefreshCw className="animate-spin" />
                    <span>Processing Financial Aggregations...</span>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div style={{ padding: '2rem', background: 'rgba(239, 68, 68, 0.1)', border: '1px solid #ef4444', borderRadius: '8px', color: '#ef4444' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                    <AlertTriangle />
                    <span>{error}</span>
                </div>
            </div>
        );
    }

    return (
        <div className="financial-dashboard" style={{ padding: '1rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
                <h2 style={{ fontSize: '1.8rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <TrendingUp color="var(--brand-color)" /> Financial Integrity Dashboard
                </h2>
                <button onClick={fetchStats} className="btn-secondary" style={{ width: 'auto', padding: '0.5rem 1rem' }}>
                    <RefreshCw size={16} /> Refresh Stats
                </button>
            </div>

            <div className="stats-grid" style={{ marginBottom: '2rem' }}>
                <div className="stat-card" style={{ background: 'linear-gradient(135deg, var(--panel-bg), rgba(2, 60, 255, 0.05))' }}>
                    <div className="flex-between">
                        <span style={{ color: 'var(--text-secondary)' }}>Today's Realized Revenue</span>
                        <Wallet color="var(--brand-color)" />
                    </div>
                    <p style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>
                        ₹ {(stats?.todaysTotal || 0).toLocaleString('en-IN')}
                    </p>
                    <div style={{ fontSize: '0.8rem', color: 'var(--success)' }}>
                        Updated: {new Date().toLocaleTimeString()}
                    </div>
                </div>

                <div className="stat-card">
                    <div className="flex-between">
                        <span style={{ color: 'var(--text-secondary)' }}>Total Adjustments</span>
                        <Settings color="#d29922" />
                    </div>
                    <p style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>
                        {stats.totalAdjustments}
                    </p>
                    <span style={{ fontSize: '0.8rem' }}>Non-destructive corrections</span>
                </div>

                <div className="stat-card">
                    <div className="flex-between">
                        <span style={{ color: 'var(--text-secondary)' }}>Manual Overrides</span>
                        <UserCheck color="#a855f7" />
                    </div>
                    <p style={{ fontSize: '2rem', fontWeight: 'bold', margin: '0.5rem 0' }}>
                        {stats.totalOverrides}
                    </p>
                    <span style={{ fontSize: '0.8rem' }}>Audit entries recorded</span>
                </div>

                <div className="stat-card" style={{ border: stats.integrityViolations > 0 ? '1px solid #ef4444' : '1px solid var(--border-color)' }}>
                    <div className="flex-between">
                        <span style={{ color: 'var(--text-secondary)' }}>Integrity Exceptions</span>
                        <AlertTriangle color={stats.integrityViolations > 0 ? "#ef4444" : "var(--success)"} />
                    </div>
                    <p style={{
                        fontSize: '2rem',
                        fontWeight: 'bold',
                        margin: '0.5rem 0',
                        color: stats.integrityViolations > 0 ? "#ef4444" : "inherit"
                    }}>
                        {stats.integrityViolations}
                    </p>
                    <span style={{ fontSize: '0.8rem' }}>
                        {stats.integrityViolations > 0 ? 'TAMPERING DETECTED' : 'System Secure'}
                    </span>
                </div>
            </div>

            <div className="panels-container" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '2rem' }}>
                <div style={{ background: 'var(--panel-bg)', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                    <h3 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <CreditCard size={20} color="var(--brand-color)" /> Payment Mode Distribution
                    </h3>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        {Object.entries(stats.paymentModeBreakdown).map(([mode, amount]) => (
                            <div key={mode} style={{ padding: '1rem', background: 'rgba(255,255,255,0.03)', borderRadius: '8px' }}>
                                <div className="flex-between">
                                    <span style={{ fontWeight: '500' }}>{mode}</span>
                                    <span style={{ fontWeight: 'bold' }}>₹ {(amount || 0).toLocaleString('en-IN')}</span>
                                </div>
                                <div style={{
                                    width: '100%',
                                    height: '6px',
                                    background: 'var(--border-color)',
                                    borderRadius: '3px',
                                    marginTop: '0.5rem',
                                    overflow: 'hidden'
                                }}>
                                    <div style={{
                                        width: `${(amount / (stats.todaysTotal || 1)) * 100}%`,
                                        height: '100%',
                                        background: 'var(--brand-color)'
                                    }} />
                                </div>
                            </div>
                        ))}
                        {Object.keys(stats.paymentModeBreakdown).length === 0 && (
                            <p style={{ textAlign: 'center', color: 'var(--text-secondary)', padding: '2rem' }}>No payments recorded today.</p>
                        )}
                    </div>
                </div>

                <div style={{ background: 'var(--panel-bg)', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--border-color)' }}>
                    <h3 style={{ marginBottom: '1.5rem', color: 'var(--text-secondary)' }}>Compliance & Oversight</h3>
                    <ul style={{ listStyle: 'none', padding: 0, display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                        <li style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--brand-color)', marginTop: '6px' }} />
                            <div>
                                <h4 style={{ margin: 0 }}>Passive Auditing Active</h4>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '4px 0 0' }}>All financial corrections are logged via immutable audit chain.</p>
                            </div>
                        </li>
                        <li style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: 'var(--success)', marginTop: '6px' }} />
                            <div>
                                <h4 style={{ margin: 0 }}>Integrity Monitoring</h4>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '4px 0 0' }}>Real-time SHA-256 validation protecting database records.</p>
                            </div>
                        </li>
                        <li style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
                            <div style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#d29922', marginTop: '6px' }} />
                            <div>
                                <h4 style={{ margin: 0 }}>Read-Only Assurance</h4>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '4px 0 0' }}>This interface does not permit modification of transactional data.</p>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
        </div>
    );
};

export default FinancialDashboard;
