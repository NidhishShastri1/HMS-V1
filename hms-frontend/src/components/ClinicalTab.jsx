import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import { FileText, Activity, Clock, Plus, ShieldAlert, CheckCircle } from 'lucide-react';

const ClinicalTab = ({ visitId, visitStatus }) => {
    const { user } = useContext(AuthContext);
    const [notes, setNotes] = useState([]);
    const [diagnoses, setDiagnoses] = useState([]);
    const [loading, setLoading] = useState(true);

    const [noteType, setNoteType] = useState('CONSULTATION');
    const [noteContent, setNoteContent] = useState('');

    const [diagCode, setDiagCode] = useState('');
    const [diagDesc, setDiagDesc] = useState('');
    const [isPrimary, setIsPrimary] = useState(false);

    useEffect(() => {
        if (visitId) {
            fetchClinicalData();
        }
    }, [visitId]);

    const fetchClinicalData = async () => {
        setLoading(true);
        try {
            const notesRes = await api.get(`/opd/visits/${visitId}/clinical-notes`);
            setNotes(notesRes.data);
            const diagRes = await api.get(`/opd/visits/${visitId}/diagnosis`);
            setDiagnoses(diagRes.data);
        } catch (err) {
            console.error("Failed to fetch clinical data", err);
        } finally {
            setLoading(false);
        }
    };

    const handleAddNote = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/opd/visits/${visitId}/clinical-notes`, { noteType, noteContent });
            setNoteContent('');
            fetchClinicalData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to add note');
        }
    };

    const handleAddDiagnosis = async (e) => {
        e.preventDefault();
        try {
            await api.post(`/opd/visits/${visitId}/diagnosis`, { diagnosisCode: diagCode, diagnosisDescription: diagDesc, isPrimary });
            setDiagCode('');
            setDiagDesc('');
            setIsPrimary(false);
            fetchClinicalData();
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to add diagnosis');
        }
    };

    const handleStatusUpdate = async (status) => {
        try {
            await api.post(`/opd/visits/${visitId}/status`, { status });
            // Since we updated status, notify parent or simply alert
            alert(`Visit marked as ${status}`);
            window.location.reload(); // Quick refresh to update the parent visit status
        } catch (err) {
            alert(err.response?.data?.message || 'Failed to update status');
        }
    };

    if (loading) return <div>Loading clinical records...</div>;

    const isClosed = visitStatus === 'CLOSED';
    const canAddClinical = user.role === 'DOCTOR' && !isClosed;
    const canMarkClosed = (user.role === 'ADMIN' || user.role === 'SUPERVISOR') && !isClosed;

    return (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
            <div className="panel" style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)' }}>
                <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--brand-color)' }}>
                    <FileText size={20} /> Clinical Notes
                </h3>
                {canAddClinical && (
                    <form onSubmit={handleAddNote} style={{ marginBottom: '1.5rem', padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px' }}>
                        <div className="form-group">
                            <label>Note Type</label>
                            <select value={noteType} onChange={(e) => setNoteType(e.target.value)}>
                                <option value="CONSULTATION">Consultation</option>
                                <option value="PROGRESS">Progress Note</option>
                                <option value="DISCHARGE_SUMMARY">Discharge Summary</option>
                            </select>
                        </div>
                        <div className="form-group">
                            <label>Content</label>
                            <textarea rows="3" required value={noteContent} onChange={(e) => setNoteContent(e.target.value)}></textarea>
                        </div>
                        <button type="submit" className="btn" style={{ width: '100%' }}>Add Note</button>
                    </form>
                )}

                <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                    {notes.map(note => (
                        <div key={note.id} style={{ padding: '1rem', background: 'var(--panel-bg)', borderRadius: '8px', borderLeft: note.current ? '4px solid var(--brand-color)' : '4px solid #555' }}>
                            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                                <strong>{note.noteType}</strong>
                                <span>v{note.versionNumber} | {new Date(note.createdAt).toLocaleString()}</span>
                            </div>
                            <p style={{ margin: 0, whiteSpace: 'pre-wrap' }}>{note.noteContent}</p>
                            {!note.current && <span style={{ fontSize: '0.8rem', color: '#888' }}>(Superseded)</span>}
                        </div>
                    ))}
                    {notes.length === 0 && <p style={{ color: 'var(--text-secondary)' }}>No clinical notes available.</p>}
                </div>
            </div>

            <div className="panel" style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
                    <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--danger)', margin: 0 }}>
                        <Activity size={20} /> Diagnosis
                    </h3>
                    <div style={{ display: 'flex', gap: '0.5rem' }}>
                        {canAddClinical && (
                            <button className="btn btn-secondary" onClick={() => handleStatusUpdate('COMPLETED')} title="Mark COMPLETED">
                                <CheckCircle size={16} /> Mark COMPLETED
                            </button>
                        )}
                        {canMarkClosed && (
                            <button className="btn btn-danger" style={{ background: 'darkred', color: '#fff', border: 'none' }} onClick={() => handleStatusUpdate('CLOSED')} title="Mark CLOSED">
                                <ShieldAlert size={16} /> Mark CLOSED
                            </button>
                        )}
                    </div>
                </div>

                {canAddClinical && (
                    <form onSubmit={handleAddDiagnosis} style={{ marginBottom: '1.5rem', padding: '1rem', background: 'rgba(0,0,0,0.2)', borderRadius: '8px' }}>
                        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '1rem' }}>
                            <div className="form-group">
                                <label>Code (ICD)</label>
                                <input type="text" value={diagCode} onChange={(e) => setDiagCode(e.target.value)} />
                            </div>
                            <div className="form-group">
                                <label>Description *</label>
                                <input type="text" required value={diagDesc} onChange={(e) => setDiagDesc(e.target.value)} />
                            </div>
                        </div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                            <input type="checkbox" id="primaryDiag" checked={isPrimary} onChange={(e) => setIsPrimary(e.target.checked)} />
                            <label htmlFor="primaryDiag" style={{ margin: 0, cursor: 'pointer' }}>Set as Primary Diagnosis</label>
                        </div>
                        <button type="submit" className="btn" style={{ width: '100%' }}>Add Diagnosis</button>
                    </form>
                )}

                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                    <thead>
                        <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                            <th style={{ padding: '0.5rem' }}>Code</th>
                            <th style={{ padding: '0.5rem' }}>Description</th>
                            <th style={{ padding: '0.5rem' }}>Type</th>
                        </tr>
                    </thead>
                    <tbody>
                        {diagnoses.map(diag => (
                            <tr key={diag.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                <td style={{ padding: '0.5rem' }}>{diag.diagnosisCode || '-'}</td>
                                <td style={{ padding: '0.5rem' }}>{diag.diagnosisDescription}</td>
                                <td style={{ padding: '0.5rem' }}>
                                    {diag.primary ? <span style={{ color: 'var(--danger)', fontWeight: 'bold' }}>Primary</span> : 'Secondary'}
                                </td>
                            </tr>
                        ))}
                        {diagnoses.length === 0 && (
                            <tr><td colSpan="3" style={{ padding: '1rem', textAlign: 'center', color: 'var(--text-secondary)' }}>No diagnosis recorded.</td></tr>
                        )}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default ClinicalTab;
