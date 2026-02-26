import React, { useState, useEffect } from 'react';
import api from '../api/axios';
import { UserPlus, Settings, Lock, Unlock, RefreshCw, X } from 'lucide-react';

const AdminPanel = () => {
    const [users, setUsers] = useState([]);
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [activeTab, setActiveTab] = useState('users');

    const [schema] = useState(['ADMIN', 'RECEPTION', 'SUPERVISOR', 'DOCTOR']);
    const [newUser, setNewUser] = useState({ username: '', password: '', role: 'RECEPTION' });
    const [modalOpen, setModalOpen] = useState(false);
    const [selectedUser, setSelectedUser] = useState(null);
    const [resetPassword, setResetPassword] = useState('');
    const [msg, setMsg] = useState({ text: '', type: '' });

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = async () => {
        try {
            setLoading(true);
            const [resUsers, resLogs] = await Promise.all([
                api.get('/admin/users'),
                api.get('/admin/logs')
            ]);
            setUsers(resUsers.data);
            setLogs(resLogs.data.slice(0, 50)); // Last 50 runs for UI
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    const showMsg = (text, type = 'success') => {
        setMsg({ text, type });
        setTimeout(() => setMsg({ text: '', type: '' }), 3000);
    };

    const handleCreateUser = async (e) => {
        e.preventDefault();
        try {
            await api.post('/admin/users', newUser);
            showMsg('User created successfully');
            setNewUser({ username: '', password: '', role: 'RECEPTION' });
            fetchData();
        } catch (err) {
            showMsg(err.response?.data?.message || 'Error creating user', 'error');
        }
    };

    const toggleStatus = async (id, currentStatus) => {
        try {
            await api.put(`/admin/users/${id}/toggle-status?enable=${!currentStatus}`);
            fetchData();
            showMsg(`User ${!currentStatus ? 'enabled' : 'disabled'}`);
        } catch (err) {
            showMsg('Error updating status', 'error');
        }
    };

    const toggleLock = async (id, currentLock) => {
        try {
            await api.put(`/admin/users/${id}/toggle-lock?unlock=${currentLock}`);
            fetchData();
            showMsg(`User ${currentLock ? 'unlocked' : 'locked'}`);
        } catch (err) {
            showMsg('Error updating lock status', 'error');
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/admin/users/${selectedUser.id}/reset-password`, { newPassword: resetPassword });
            showMsg(`Password reset for ${selectedUser.username}`);
            setModalOpen(false);
            setResetPassword('');
            setSelectedUser(null);
        } catch (err) {
            showMsg(err.response?.data?.message || 'Error resetting password', 'error');
        }
    };

    return (
        <div style={{ marginTop: '2rem' }}>
            <div style={{ display: 'flex', gap: '1rem', marginBottom: '2rem' }}>
                <button className={`btn ${activeTab === 'users' ? '' : 'btn-secondary'}`} style={{ width: 'auto' }} onClick={() => setActiveTab('users')}>
                    User Management
                </button>
                <button className={`btn ${activeTab === 'logs' ? '' : 'btn-secondary'}`} style={{ width: 'auto' }} onClick={() => setActiveTab('logs')}>
                    Audit Logs
                </button>
                <button className="btn btn-secondary" style={{ width: 'auto', marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '5px' }} onClick={fetchData}>
                    <RefreshCw size={16} /> Refresh
                </button>
            </div>

            {msg.text && (
                <div className={msg.type === 'error' ? 'error-msg' : 'success-msg'} style={{ maxWidth: '400px', marginBottom: '1rem' }}>
                    {msg.text}
                </div>
            )}

            {activeTab === 'users' && (
                <div style={{ display: 'grid', gridTemplateColumns: 'minmax(300px, 400px) 1fr', gap: '2rem' }}>
                    {/* Create User Form */}
                    <div className="auth-card" style={{ transform: 'none' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '0.5rem' }}>
                            <UserPlus size={24} color="var(--brand-color)" />
                            <h3>Provision New User</h3>
                        </div>
                        <form onSubmit={handleCreateUser}>
                            <div className="form-group">
                                <label>Username</label>
                                <input type="text" value={newUser.username} onChange={e => setNewUser({ ...newUser, username: e.target.value })} required />
                            </div>
                            <div className="form-group">
                                <label>Temporary Password</label>
                                <input type="password" value={newUser.password} onChange={e => setNewUser({ ...newUser, password: e.target.value })} required />
                            </div>
                            <div className="form-group">
                                <label>Role</label>
                                <select value={newUser.role} onChange={e => setNewUser({ ...newUser, role: e.target.value })} required>
                                    {schema.map(r => <option key={r} value={r}>{r}</option>)}
                                </select>
                            </div>
                            <button type="submit" className="btn" style={{ marginTop: '1rem' }}>Create Personnel Record</button>
                        </form>
                    </div>

                    {/* User List */}
                    <div>
                        <h3>Active Personnel Registry</h3>
                        <table className="data-table">
                            <thead>
                                <tr>
                                    <th>Username</th>
                                    <th>Role</th>
                                    <th>Status</th>
                                    <th>Lock</th>
                                    <th>Last Access</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? <tr><td colSpan="6">Loading...</td></tr> : users.map(u => (
                                    <tr key={u.id}>
                                        <td style={{ fontWeight: 'bold' }}>{u.username}</td>
                                        <td>{u.role}</td>
                                        <td>
                                            <span className={`badge ${u.enabled ? 'active' : 'disabled'}`}>
                                                {u.enabled ? 'Active' : 'Disabled'}
                                            </span>
                                        </td>
                                        <td>
                                            <span className={`badge ${u.locked ? 'locked' : 'active'}`}>
                                                {u.locked ? 'Locked' : 'Unlocked'}
                                            </span>
                                        </td>
                                        <td style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                                            {u.lastLogin ? new Date(u.lastLogin).toLocaleString() : 'Never'}
                                        </td>
                                        <td style={{ display: 'flex', gap: '0.5rem' }}>
                                            <button className="btn btn-secondary" style={{ padding: '0.4rem', width: 'auto' }} title="Toggle Status" onClick={() => toggleStatus(u.id, u.enabled)}>
                                                <Settings size={14} />
                                            </button>
                                            <button className="btn btn-secondary" style={{ padding: '0.4rem', width: 'auto' }} title={u.locked ? 'Unlock Account' : 'Lock Account'} onClick={() => toggleLock(u.id, u.locked)}>
                                                {u.locked ? <Unlock size={14} /> : <Lock size={14} />}
                                            </button>
                                            <button className="btn btn-danger" style={{ padding: '0.4rem', width: 'auto' }} title="Reset Password" onClick={() => { setSelectedUser(u); setModalOpen(true); }}>
                                                <RefreshCw size={14} />
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </div>
            )}

            {activeTab === 'logs' && (
                <div>
                    <h3>System Audit Log</h3>
                    <p style={{ color: 'var(--text-secondary)', marginBottom: '1rem' }}>Showing last 50 login attempts. These records are immutable.</p>
                    <table className="data-table">
                        <thead>
                            <tr>
                                <th>Timestamp</th>
                                <th>User</th>
                                <th>IP Address</th>
                                <th>Result</th>
                                <th>Details</th>
                                <th>Logout Time</th>
                            </tr>
                        </thead>
                        <tbody>
                            {loading ? <tr><td colSpan="6">Loading...</td></tr> : logs.map(l => (
                                <tr key={l.id}>
                                    <td style={{ whiteSpace: 'nowrap' }}>{new Date(l.loginTime).toLocaleString()}</td>
                                    <td style={{ fontWeight: 'bold' }}>{l.username}</td>
                                    <td style={{ fontFamily: 'monospace' }}>{l.ipAddress}</td>
                                    <td>
                                        <span className={`badge ${l.success ? 'active' : 'disabled'}`}>
                                            {l.success ? 'Success' : 'Failed'}
                                        </span>
                                    </td>
                                    <td style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{l.failureReason || 'Authorized Access'}</td>
                                    <td>{l.logoutTime ? new Date(l.logoutTime).toLocaleString() : (l.success ? 'Active Session' : '-')}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}

            {modalOpen && selectedUser && (
                <div className="modal-overlay">
                    <div className="modal-content">
                        <div className="flex-between" style={{ marginBottom: '1.5rem' }}>
                            <h3>Reset Password for {selectedUser.username}</h3>
                            <button onClick={() => setModalOpen(false)} style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer' }}><X /></button>
                        </div>
                        <form onSubmit={handleResetPassword}>
                            <div className="form-group">
                                <label>New Administrative Password</label>
                                <input type="password" value={resetPassword} onChange={e => setResetPassword(e.target.value)} required />
                                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.5rem' }}>User will be forced to change this upon next login.</p>
                            </div>
                            <button className="btn" type="submit">Execute Override</button>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default AdminPanel;
