import React, { useState, useEffect, useContext, useRef } from 'react';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import { UserPlus, Search, Building2, User, Edit, FileText, AlertTriangle, Printer, Trash2, GitMerge } from 'lucide-react';
import ClinicalTab from '../components/ClinicalTab';
import IpdTab from '../components/IpdTab';

const Patients = () => {
    const { user } = useContext(AuthContext);
    const [patients, setPatients] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [viewingPatient, setViewingPatient] = useState(null);
    const [activeTab, setActiveTab] = useState('info');
    const [patientVisits, setPatientVisits] = useState([]);
    const [selectedVisitId, setSelectedVisitId] = useState(null);
    const [loadingVisits, setLoadingVisits] = useState(false);

    // Registration/Edit Modal State
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);
    const [formData, setFormData] = useState({
        firstName: '', lastName: '', phone: '', gender: '', dateOfBirth: '', age: '', address: '', idProof: ''
    });

    // Merge Modal State (Admin)
    const [isMergeModalOpen, setIsMergeModalOpen] = useState(false);
    const [mergeTargetId, setMergeTargetId] = useState('');

    const autoFocusRef = useRef(null);

    const fetchPatientVisits = async (patientId) => {
        setLoadingVisits(true);
        try {
            const res = await api.get(`/opd/patient/${patientId}`);
            setPatientVisits(res.data);
            setSelectedVisitId(null);
        } catch (err) {
            console.error('Failed to load visits', err);
        } finally {
            setLoadingVisits(false);
        }
    };

    useEffect(() => {
        fetchPatients();
    }, []);

    useEffect(() => {
        if (isModalOpen && autoFocusRef.current) {
            autoFocusRef.current.focus();
        }
    }, [isModalOpen]);

    const fetchPatients = async () => {
        setLoading(true);
        try {
            const res = await api.get('/patients');
            setPatients(res.data);
            setError('');
        } catch (err) {
            setError('Failed to load patients.');
        } finally {
            setLoading(false);
        }
    };

    const handleSearch = async (e) => {
        e.preventDefault();
        if (!searchQuery.trim()) {
            return fetchPatients();
        }
        setLoading(true);
        try {
            const res = await api.get(`/patients/search?query=${searchQuery}`);
            setPatients(res.data);
            setError('');
        } catch (err) {
            setError('Search failed.');
        } finally {
            setLoading(false);
        }
    };

    const handleOpenRegister = () => {
        setIsEditMode(false);
        setFormData({ firstName: '', lastName: '', phone: '', gender: '', dateOfBirth: '', age: '', address: '', idProof: '' });
        setIsModalOpen(true);
    };

    const handleOpenEdit = (p) => {
        setIsEditMode(true);
        setFormData({
            firstName: p.firstName, lastName: p.lastName, phone: p.phone,
            gender: p.gender || '', dateOfBirth: p.dateOfBirth || '', age: p.age || '',
            address: p.address || '', idProof: p.idProof || ''
        });
        setIsModalOpen(true);
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        try {
            if (isEditMode && viewingPatient) {
                const res = await api.put(`/patients/${viewingPatient.patientId}`, formData);
                setViewingPatient(res.data);
            } else {
                await api.post('/patients', formData);
            }
            setIsModalOpen(false);
            fetchPatients();
        } catch (err) {
            if (err.response?.status === 409) {
                setError('DUPLICATE DETECTED: A patient with this exact Name, Phone, and DOB already exists in the system.');
            } else {
                setError(err.response?.data?.message || 'Failed to process request');
            }
        }
    };

    const handleDelete = async (patientId) => {
        if (!window.confirm('Are you sure you want to delete this patient record?')) return;
        try {
            await api.delete(`/patients/${patientId}`);
            setViewingPatient(null);
            fetchPatients();
        } catch (err) {
            alert('Failed to delete patient. Ensure you have Admin rights.');
        }
    };

    const handleMerge = async (e) => {
        e.preventDefault();
        try {
            await api.post('/patients/merge', {
                sourcePatientId: viewingPatient.patientId,
                targetPatientId: mergeTargetId
            });
            setIsMergeModalOpen(false);
            setViewingPatient(null);
            fetchPatients();
            alert('Merge successful. Source record removed.');
        } catch (err) {
            alert(err.response?.data || 'Failed to merge patients.');
        }
    };

    const printProfile = () => {
        window.print();
    };

    const renderProfile = () => {
        if (!viewingPatient) return null;
        return (
            <div className="panel" style={{ marginTop: '2rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem', marginBottom: '1rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                        <div style={{ background: 'var(--brand-color)', padding: '1rem', borderRadius: '50%', color: '#fff', fontSize: '1.5rem', fontWeight: 'bold' }}>
                            {viewingPatient.firstName.charAt(0)}{viewingPatient.lastName.charAt(0)}
                        </div>
                        <div>
                            <h2 style={{ margin: 0 }}>{viewingPatient.firstName} {viewingPatient.lastName}</h2>
                            <p style={{ margin: 0, color: 'var(--text-secondary)' }}>ID: <strong>{viewingPatient.patientId}</strong> | Registered: {viewingPatient.registrationDate}</p>
                        </div>
                    </div>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                        <button className="btn btn-secondary" onClick={() => handleOpenEdit(viewingPatient)} title="Edit Profile">
                            <Edit size={16} />
                        </button>
                        <button className="btn btn-secondary" onClick={printProfile} title="Print">
                            <Printer size={16} />
                        </button>
                        {user.role === 'ADMIN' && (
                            <>
                                <button className="btn btn-secondary" onClick={() => setIsMergeModalOpen(true)} title="Merge Patient">
                                    <GitMerge size={16} />
                                </button>
                                <button className="btn" style={{ background: 'var(--danger)', borderColor: 'var(--danger)' }} onClick={() => handleDelete(viewingPatient.patientId)} title="Soft Delete">
                                    <Trash2 size={16} />
                                </button>
                            </>
                        )}
                        <button className="btn btn-secondary" onClick={() => setViewingPatient(null)}>Close</button>
                    </div>
                </div>

                <div style={{ display: 'flex', gap: '2rem', borderBottom: '1px solid var(--border-color)', marginBottom: '1rem' }}>
                    <div className={`tab ${activeTab === 'info' ? 'active' : ''}`} onClick={() => setActiveTab('info')} style={{ paddingBottom: '0.5rem', cursor: 'pointer', borderBottom: activeTab === 'info' ? '2px solid var(--brand-color)' : 'none' }}>Demographics</div>
                    <div className={`tab ${activeTab === 'visits' ? 'active' : ''}`} onClick={() => { setActiveTab('visits'); fetchPatientVisits(viewingPatient.patientId); }} style={{ paddingBottom: '0.5rem', cursor: 'pointer', borderBottom: activeTab === 'visits' ? '2px solid var(--brand-color)' : 'none' }}>Visit History</div>
                    <div className={`tab ${activeTab === 'bills' ? 'active' : ''}`} onClick={() => setActiveTab('bills')} style={{ paddingBottom: '0.5rem', cursor: 'pointer', borderBottom: activeTab === 'bills' ? '2px solid var(--brand-color)' : 'none' }}>Billing Summary</div>
                </div>

                {activeTab === 'info' && (
                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
                        <div>
                            <p><strong>Phone:</strong> {viewingPatient.phone}</p>
                            <p><strong>Gender:</strong> {viewingPatient.gender || 'Not specified'}</p>
                            <p><strong>ID Proof:</strong> {viewingPatient.idProof || 'Not provided'}</p>
                        </div>
                        <div>
                            <p><strong>Date of Birth:</strong> {viewingPatient.dateOfBirth || 'Not specified'}</p>
                            <p><strong>Age:</strong> {viewingPatient.age || 'Not specified'}</p>
                            <p><strong>Address:</strong> {viewingPatient.address || 'Not specified'}</p>
                        </div>
                    </div>
                )}

                {activeTab === 'visits' && (
                    <div style={{ padding: '1rem 0' }}>
                        {loadingVisits ? <p>Loading visits...</p> : (
                            <>
                                {patientVisits.length === 0 ? <p style={{ color: 'var(--text-secondary)' }}>No visit history found.</p> : (
                                    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
                                        {patientVisits.map(visit => (
                                            <div key={visit.visitId} style={{ border: '1px solid var(--border-color)', borderRadius: '8px', overflow: 'hidden' }}>
                                                <div
                                                    style={{ padding: '1rem', background: 'var(--panel-bg)', cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}
                                                    onClick={() => setSelectedVisitId(selectedVisitId === visit.visitId ? null : visit.visitId)}
                                                >
                                                    <div>
                                                        <strong>{visit.visitId}</strong> - {visit.department} ({visit.visitCategory})
                                                        <span style={{ marginLeft: '1rem', color: 'var(--text-secondary)' }}>{new Date(visit.visitDateTime).toLocaleString()}</span>
                                                    </div>
                                                    <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                                                        <span style={{ padding: '0.2rem 0.6rem', borderRadius: '4px', fontSize: '0.8rem', background: visit.visitStatus === 'CLOSED' ? '#444' : 'var(--brand-color)' }}>{visit.visitStatus || 'OPEN'}</span>
                                                        <span style={{ padding: '0.2rem 0.6rem', borderRadius: '4px', fontSize: '0.8rem', background: '#333' }}>{visit.status}</span>
                                                    </div>
                                                </div>
                                                {selectedVisitId === visit.visitId && (
                                                    <div style={{ padding: '1rem', borderTop: '1px solid var(--border-color)', background: '#181e2e' }}>
                                                        {visit.visitCategory === 'IPD' && (
                                                            <div style={{ marginBottom: '2rem' }}>
                                                                <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
                                                                    <div className="tab active" style={{ borderBottom: '2px solid var(--brand-color)', paddingBottom: '0.5rem' }}>IPD Tracking</div>
                                                                </div>
                                                                <IpdTab visitId={visit.visitId} initialIpdStatus={visit.ipdStatus} finalSettlementLocked={visit.finalSettlementLocked} />
                                                            </div>
                                                        )}
                                                        <div style={{ display: 'flex', gap: '1rem', marginBottom: '1rem', borderBottom: '1px solid var(--border-color)' }}>
                                                            <div className="tab active" style={{ borderBottom: '2px solid var(--brand-color)', paddingBottom: '0.5rem' }}>Clinical Data ({visit.visitCategory})</div>
                                                        </div>
                                                        <ClinicalTab visitId={visit.visitId} visitStatus={visit.visitStatus} />
                                                    </div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </>
                        )}
                    </div>
                )}

                {activeTab === 'bills' && (
                    <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                        Billing Management Module (In Development - Phase 4)
                    </div>
                )}
            </div>
        );
    };

    return (
        <div className="dashboard-content dashboard-container">
            <div className="flex-between" style={{ marginBottom: '2rem' }}>
                <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <Building2 size={32} color="var(--brand-color)" />
                    Patient Registry Workspace
                </h1>
                <button className="btn" onClick={handleOpenRegister} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', fontWeight: 'bold' }}>
                    <UserPlus size={18} /> NEW PATIENT
                </button>
            </div>

            {error && !isModalOpen && <div className="error-msg" style={{ marginBottom: '1rem' }}>{error}</div>}

            {!viewingPatient ? (
                <>
                    <div className="panel" style={{ marginBottom: '2rem' }}>
                        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '1rem' }}>
                            <div style={{ position: 'relative', flex: 1 }}>
                                <Search size={20} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
                                <input
                                    type="text"
                                    placeholder="Fast Search by Patient ID, Phone, or Name..."
                                    style={{ width: '100%', padding: '0.75rem 0.75rem 0.75rem 3rem', fontSize: '1.1rem', background: 'var(--bg-color)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)' }}
                                    value={searchQuery}
                                    onChange={e => setSearchQuery(e.target.value)}
                                />
                            </div>
                            <button type="submit" className="btn" style={{ width: '150px' }}>Search</button>
                        </form>
                    </div>

                    <div className="panel" style={{ overflowX: 'auto' }}>
                        {loading ? <p style={{ color: 'var(--text-secondary)', padding: '1rem' }}>Executing search...</p> : (
                            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                                        <th style={{ padding: '1rem' }}>Patient ID</th>
                                        <th style={{ padding: '1rem' }}>Full Name</th>
                                        <th style={{ padding: '1rem' }}>Phone Number</th>
                                        <th style={{ padding: '1rem' }}>Age / DOB</th>
                                        <th style={{ padding: '1rem' }}>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {patients.length === 0 ? (
                                        <tr>
                                            <td colSpan="5" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                                                No patients found matching your criteria.
                                            </td>
                                        </tr>
                                    ) : (
                                        patients.map(p => (
                                            <tr key={p.id} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                                <td style={{ padding: '1rem', fontWeight: 'bold', color: 'var(--brand-color)' }}>{p.patientId}</td>
                                                <td style={{ padding: '1rem', fontWeight: '500' }}>{p.firstName} {p.lastName}</td>
                                                <td style={{ padding: '1rem' }}>{p.phone}</td>
                                                <td style={{ padding: '1rem' }}>{p.age ? `${p.age} yrs` : p.dateOfBirth}</td>
                                                <td style={{ padding: '1rem' }}>
                                                    <button className="btn" style={{ padding: '0.4rem 1rem', fontSize: '0.9rem' }} onClick={() => setViewingPatient(p)}>Open Profile</button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                </tbody>
                            </table>
                        )}
                    </div>
                </>
            ) : (
                renderProfile()
            )}

            {isModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100 }}>
                    <div className="panel" style={{ width: '600px', padding: '2rem', maxHeight: '90vh', overflowY: 'auto', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                        <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
                            <FileText size={24} color="var(--brand-color)" /> {isEditMode ? 'Update Demographics' : 'New Patient Registration'}
                        </h2>

                        {error && (
                            <div className="error-msg" style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem' }}>
                                <AlertTriangle size={20} /> {error}
                            </div>
                        )}

                        <form onSubmit={handleSubmit}>
                            {isEditMode && (
                                <div style={{ marginBottom: '1rem', color: 'var(--text-secondary)' }}>
                                    <em>Note: Patient ID ({viewingPatient?.patientId}) is immutable and cannot be changed.</em>
                                </div>
                            )}

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <div className="form-group">
                                    <label>First Name *</label>
                                    <input type="text" ref={autoFocusRef} required value={formData.firstName} onChange={e => setFormData({ ...formData, firstName: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Last Name *</label>
                                    <input type="text" required value={formData.lastName} onChange={e => setFormData({ ...formData, lastName: e.target.value })} />
                                </div>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <div className="form-group">
                                    <label>Phone Number *</label>
                                    <input type="text" required pattern="\d{10}" title="Must be exactly 10 digits" value={formData.phone} onChange={e => setFormData({ ...formData, phone: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Gender</label>
                                    <select value={formData.gender} onChange={e => setFormData({ ...formData, gender: e.target.value })}>
                                        <option value="">Select...</option>
                                        <option value="MALE">Male</option>
                                        <option value="FEMALE">Female</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>
                            </div>

                            <div style={{ padding: '1rem', background: '#1c2235', borderRadius: '8px', marginBottom: '1rem', border: '1px solid var(--border-color)' }}>
                                <p style={{ margin: '0 0 0.5rem 0', fontSize: '0.9rem', color: 'var(--brand-color)' }}>Age Verification (Provide One)*</p>
                                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                    <div className="form-group" style={{ margin: 0 }}>
                                        <label>Date of Birth</label>
                                        <input type="date" value={formData.dateOfBirth} onChange={e => setFormData({ ...formData, dateOfBirth: e.target.value, age: '' })} disabled={!!formData.age} />
                                    </div>
                                    <div className="form-group" style={{ margin: 0 }}>
                                        <label>Or Age (Years)</label>
                                        <input type="number" min="0" value={formData.age} onChange={e => setFormData({ ...formData, age: e.target.value, dateOfBirth: '' })} disabled={!!formData.dateOfBirth} />
                                    </div>
                                </div>
                            </div>

                            <div className="form-group">
                                <label>Govt ID / Proof Document Reference (Optional)</label>
                                <input type="text" value={formData.idProof} onChange={e => setFormData({ ...formData, idProof: e.target.value })} placeholder="e.g. Passport or License No." />
                            </div>

                            <div className="form-group">
                                <label>Residential Address</label>
                                <textarea rows="2" style={{ width: '100%', padding: '0.75rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-color)', color: 'var(--text-primary)' }} value={formData.address} onChange={e => setFormData({ ...formData, address: e.target.value })}></textarea>
                            </div>

                            <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                                <button type="submit" className="btn" style={{ flex: 1, height: '50px', fontSize: '1.1rem' }}>
                                    {isEditMode ? 'Update' : 'Confirm Registration'}
                                </button>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => { setIsModalOpen(false); setError(''); }}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {isMergeModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100 }}>
                    <div className="panel" style={{ width: '500px', padding: '2rem', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                        <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--danger)' }}>
                            <AlertTriangle size={24} /> Admin Merge Protocol
                        </h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                            You are merging <strong>{viewingPatient.patientId}</strong> into another target record. This current record will be soft-deleted. No visits or bills are migrated in this phase.
                        </p>
                        <form onSubmit={handleMerge}>
                            <div className="form-group">
                                <label>Target Patient ID (Master Record)</label>
                                <input type="text" required placeholder="e.g., PT-ABC12345" value={mergeTargetId} onChange={e => setMergeTargetId(e.target.value)} />
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', marginTop: '2rem' }}>
                                <button type="submit" className="btn" style={{ flex: 1, background: 'var(--danger)', borderColor: 'var(--danger)' }}>Merge Now</button>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => setIsMergeModalOpen(false)}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Patients;
