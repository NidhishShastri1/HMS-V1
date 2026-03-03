import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import { Bed, DollarSign, Activity, AlertTriangle, CheckCircle, Lock } from 'lucide-react';

const IpdTab = ({ visitId, initialIpdStatus, finalSettlementLocked }) => {
    const { user } = useContext(AuthContext);
    const [allocations, setAllocations] = useState([]);
    const [charges, setCharges] = useState([]);
    const [ipdStatus, setIpdStatus] = useState(initialIpdStatus);
    const [locked, setLocked] = useState(finalSettlementLocked);
    const [loading, setLoading] = useState(true);

    const [bedNumber, setBedNumber] = useState('');
    const [wardName, setWardName] = useState('');

    const [chargeDate, setChargeDate] = useState(new Date().toISOString().split('T')[0]);
    const [chargeType, setChargeType] = useState('ROOM');
    const [amount, setAmount] = useState('');

    useEffect(() => {
        if (visitId) fetchIpdData();
    }, [visitId]);

    const fetchIpdData = async () => {
        setLoading(true);
        try {
            const [bedsRes, chargesRes] = await Promise.all([
                api.get(`/ipd/${visitId}/allocate-bed`),
                api.get(`/ipd/${visitId}/add-daily-charge`)
            ]);
            setAllocations(bedsRes.data);
            setCharges(chargesRes.data);
        } catch (err) {
            console.error("Failed to fetch IPD data", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAllocateBed = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/ipd/${visitId}/allocate-bed`, { bedNumber, wardName });
            setBedNumber('');
            setWardName('');
            fetchIpdData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to allocate bed');
        }
    };

    const handleAddCharge = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/ipd/${visitId}/add-daily-charge`, { chargeDate, chargeType, amount: parseFloat(amount) });
            setAmount('');
            fetchIpdData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to add charge');
        }
    };

    const handleStatusUpdate = async (status) => {
        try {
            await api.post(`/ipd/${visitId}/change-status`, { status });
            alert(`IPD Visit marked as ${status}`);
            window.location.reload();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to update IPD status');
        }
    };

    if (loading) return <div>Loading IPD records...</div>;

    const isSettled = ipdStatus === 'SETTLED' || locked;
    const isDischarged = ipdStatus === 'DISCHARGED';

    const canAllocate = ['ADMIN', 'RECEPTION', 'SUPERVISOR'].includes(user.role) && !isSettled && !isDischarged;
    const canAddCharge = ['ADMIN', 'RECEPTION', 'SUPERVISOR'].includes(user.role) && !isSettled;

    return (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <div className="panel" style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-color)' }}>
                        <Bed size={20} /> Bed Allocation Tracking
                    </h3>
                    {locked && <span style={{ color: 'var(--danger)', display: 'flex', alignItems: 'center', gap: '0.5rem' }}><Lock size={16} /> SETTLEMENT LOCKED</span>}
                </div>

                {canAllocate && (
                    <form onSubmit={handleAllocateBed} style={{ marginBottom: '1.5rem', padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label>Ward Name</label>
                                <input type="text" required value={wardName} onChange={(e) => setWardName(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label>Bed Number</label>
                                <input type="text" required value={bedNumber} onChange={(e) => setBedNumber(e.target.value)} />
                            </div>
                        </div>
                        <button type="submit" className="btn" style={{ width: '100%', marginTop: '1rem' }}>Allocate New Bed</button>
                    </form>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {allocations.map(alloc => (
                        <div key={alloc.id} style={{ padding: '1rem', background: 'var(--panel-bg)', borderRadius: '8px', borderLeft: alloc.releasedAt ? '4px solid #555' : '4px solid var(--brand-color)' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                <strong>Bed: {alloc.bedNumber} (Ward: {alloc.wardName})</strong>
                                {alloc.releasedAt ? <span style={{ color: '#888' }}>Released</span> : <span style={{ color: 'var(--brand-color)', fontWeight: 'bold' }}>Active</span>}
                            </div>
                            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                                Allocated: {new Date(alloc.allocatedAt).toLocaleString()} <br />
                                {alloc.releasedAt && <span>Released: {new Date(alloc.releasedAt).toLocaleString()}</span>}
                            </p>
                        </div>
                    ))}
                    {allocations.length === 0 && <p style={{ color: 'var(--text-secondary)' }}>No bed allocated yet.</p>}
                </div>
            </div>

            <div className="panel" style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', margin: 0 }}>
                        <DollarSign size={20} color="var(--success)" /> Daily Charge Ledger
                    </h3>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {user.role === 'DOCTOR' && ipdStatus === 'UNDER_TREATMENT' && (
                            <button className="btn btn-secondary" onClick={() => handleStatusUpdate('READY_FOR_DISCHARGE')} title="Ready for Discharge">
                                <Activity size={16} /> RTD
                            </button>
                        )}
                        {['ADMIN', 'SUPERVISOR'].includes(user.role) && ipdStatus === 'READY_FOR_DISCHARGE' && (
                            <button className="btn" style={{ background: 'orange', color: '#fff', border: 'none' }} onClick={() => handleStatusUpdate('DISCHARGED')} title="Discharge">
                                <AlertTriangle size={16} /> DISCHARGE
                            </button>
                        )}
                        {['ADMIN', 'RECEPTION', 'SUPERVISOR'].includes(user.role) && ipdStatus === 'DISCHARGED' && (
                            <button className="btn btn-danger" style={{ background: 'green', color: '#fff', border: 'none' }} onClick={() => handleStatusUpdate('SETTLED')} title="Settle">
                                <CheckCircle size={16} /> SETTLE
                            </button>
                        )}
                    </div>
                </div>

                {canAddCharge && (
                    <form onSubmit={handleAddCharge} style={{ marginBottom: '1.5rem', padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label>Date</label>
                                <input type="date" required value={chargeDate} onChange={(e) => setChargeDate(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label>Type</label>
                                <select value={chargeType} onChange={(e) => setChargeType(e.target.value)}>
                                    <option value="ROOM">Room / Bed</option>
                                    <option value="NURSING">Nursing</option>
                                    <option value="PROCEDURE">Procedure</option>
                                    <option value="OTHER">Other</option>
                                </select>
                            </div>
                        </div>
                        <div className="form-group" style={{ marginTop: '1rem' }}>
                            <label>Amount (₹)</label>
                            <input type="number" step="0.01" required value={amount} onChange={(e) => setAmount(e.target.value)} />
                        </div>
                        <button type="submit" className="btn" style={{ width: '100%', marginTop: '1rem' }}>Add Charge to Bill</button>
                    </form>
                )}

                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                            <th style={{ padding: '0.5rem' }}>Date</th>
                            <th style={{ padding: '0.5rem' }}>Type</th>
                            <th style={{ padding: '0.5rem' }}>Amount</th>
                        </tr>
                    </thead>
                    <tbody>
                        {charges.map(charge => (
                            <tr key={charge.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                <td style={{ padding: '0.5rem' }}>{charge.chargeDate}</td>
                                <td style={{ padding: '0.5rem' }}>{charge.chargeType}</td>
                                <td style={{ padding: '0.5rem', color: 'var(--success)' }}>₹{charge.amount}</td>
                            </tr>
                        ))}
                        {charges.length === 0 && (
                            <tr><td colSpan="3" style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-secondary)' }}>No daily charges recorded.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default IpdTab;
