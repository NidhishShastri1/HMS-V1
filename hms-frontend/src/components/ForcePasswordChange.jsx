import React, { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import { KeyRound } from 'lucide-react';

const ForcePasswordChange = () => {
    const [oldPassword, setOldPassword] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const { checkAuth } = useContext(AuthContext);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        if (newPassword !== confirmPassword) {
            return setError('Passwords do not match');
        }

        const regex = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*#?&]{8,}$/;
        if (!regex.test(newPassword)) {
            return setError('Password must be at least 8 characters, include at least 1 letter and 1 number');
        }

        setLoading(true);
        try {
            await api.post('/auth/change-password', {
                oldPassword,
                newPassword
            });
            await checkAuth(); // Refresh user state
        } catch (err) {
            if (err.response && err.response.data && err.response.data.message) {
                setError(err.response.data.message);
            } else {
                setError('Failed to change password');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: 'calc(100vh - 80px)' }}>
            <div className="auth-card">
                <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem' }}>
                    <KeyRound size={48} color="var(--brand-color)" />
                </div>
                <h2>Mandatory Password Update</h2>
                <p style={{ textAlign: 'center', color: 'var(--text-secondary)', marginBottom: '1rem', fontSize: '0.9rem' }}>
                    For security reasons, you must change your password before accessing the system.
                </p>

                {error && <div className="error-msg">{error}</div>}

                <form onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Current Password</label>
                        <input type="password" value={oldPassword} onChange={e => setOldPassword(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>New Password (min 8 chars, 1 letter, 1 number)</label>
                        <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} required />
                    </div>
                    <div className="form-group">
                        <label>Confirm New Password</label>
                        <input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} required />
                    </div>
                    <button type="submit" className="btn" disabled={loading}>
                        {loading ? 'Updating...' : 'Update Password & Continue'}
                    </button>
                </form>
            </div>
        </div>
    );
};

export default ForcePasswordChange;
