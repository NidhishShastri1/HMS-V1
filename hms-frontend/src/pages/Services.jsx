import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import { Layers, Search, PlusCircle, Edit, History, AlertTriangle, ShieldAlert, CheckCircle, XCircle } from 'lucide-react';

const Services = () => {
    const { user } = useContext(AuthContext);
    const [services, setServices] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [searchQuery, setSearchQuery] = useState('');
    const [filterCategory, setFilterCategory] = useState('');

    // Modals
    const [isAddModalOpen, setIsAddModalOpen] = useState(false);
    const [isPriceModalOpen, setIsPriceModalOpen] = useState(false);
    const [isHistoryModalOpen, setIsHistoryModalOpen] = useState(false);

    const [selectedService, setSelectedService] = useState(null);
    const [priceHistory, setPriceHistory] = useState([]);

    const [formData, setFormData] = useState({
        serviceName: '', category: '', defaultPrice: '', unit: ''
    });

    const [priceData, setPriceData] = useState({
        newPrice: '', reason: ''
    });

    useEffect(() => {
        fetchServices();
    }, []);

    const fetchServices = async () => {
        setLoading(true);
        try {
            // Reception sees only active, Admin/Supervisor sees all.
            const res = await api.get(`/services?activeOnly=${user.role === 'RECEPTION'}`);
            setServices(res.data);
        } catch (err) {
            setError('Failed to fetch service catalog.');
        } finally {
            setLoading(false);
        }
    };

    const handleCreateService = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.post('/services', formData);
            setIsAddModalOpen(false);
            setFormData({ serviceName: '', category: '', defaultPrice: '', unit: '' });
            fetchServices();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to create service.');
        }
    };

    const handlePriceUpdate = async (e) => {
        e.preventDefault();
        setError('');
        try {
            await api.put(`/services/${selectedService.serviceId}/price`, priceData);
            setIsPriceModalOpen(false);
            setPriceData({ newPrice: '', reason: '' });
            fetchServices();
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to update price.');
        }
    };

    const handleToggleStatus = async (serviceId) => {
        if (!window.confirm("Are you sure you want to toggle the active status of this service?")) return;
        try {
            await api.put(`/services/${serviceId}/status`);
            fetchServices();
        } catch (err) {
            alert('Status toggle failed.');
        }
    };

    const viewHistory = async (service) => {
        setSelectedService(service);
        setIsHistoryModalOpen(true);
        try {
            const res = await api.get(`/services/${service.serviceId}/history`);
            setPriceHistory(res.data);
        } catch (err) {
            alert('Failed to load history.');
        }
    };

    const filteredServices = services
        .filter(s => (filterCategory ? s.category === filterCategory : true))
        .filter(s => s.serviceName.toLowerCase().includes(searchQuery.toLowerCase()) || s.serviceId.toLowerCase().includes(searchQuery.toLowerCase()));

    return (
        <div className="dashboard-content dashboard-container">
            <div className="flex-between" style={{ marginBottom: '2rem' }}>
                <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <Layers size={32} color="var(--brand-color)" />
                    Service & Pricing Catalog
                </h1>
                {user.role === 'ADMIN' && (
                    <button className="btn" onClick={() => setIsAddModalOpen(true)} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <PlusCircle size={18} /> New Service
                    </button>
                )}
            </div>

            <div className="panel" style={{ marginBottom: '2rem', display: 'flex', gap: '1rem' }}>
                <div style={{ position: 'relative', flex: 1 }}>
                    <Search size={20} style={{ position: 'absolute', left: '1rem', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-secondary)' }} />
                    <input
                        type="text"
                        placeholder="Search services by ID or Name..."
                        style={{ width: '100%', padding: '0.75rem 0.75rem 0.75rem 3rem', fontSize: '1rem', background: 'var(--bg-color)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)' }}
                        value={searchQuery}
                        onChange={e => setSearchQuery(e.target.value)}
                    />
                </div>
                <select value={filterCategory} onChange={e => setFilterCategory(e.target.value)} style={{ width: '250px', background: 'var(--bg-color)', border: '1px solid var(--border-color)', borderRadius: '6px', color: 'var(--text-primary)', padding: '0.75rem' }}>
                    <option value="">All Categories</option>
                    <option value="CONSULTATION">Consultation</option>
                    <option value="LABORATORY">Laboratory</option>
                    <option value="IMAGING">Imaging</option>
                    <option value="PROCEDURE">Procedure</option>
                    <option value="ROOM_BED">Room / Bed</option>
                    <option value="MEDICINE">Medicine</option>
                    <option value="MISCELLANEOUS">Miscellaneous</option>
                </select>
            </div>

            {error && !isAddModalOpen && !isPriceModalOpen && (
                <div className="error-msg" style={{ marginBottom: '1rem' }}>{error}</div>
            )}

            <div className="panel" style={{ overflowX: 'auto' }}>
                {loading ? <p style={{ color: 'var(--text-secondary)' }}>Loading catalog...</p> : (
                    <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                        <thead>
                            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                                <th style={{ padding: '1rem' }}>Status</th>
                                <th style={{ padding: '1rem' }}>Service ID</th>
                                <th style={{ padding: '1rem' }}>Service Name</th>
                                <th style={{ padding: '1rem' }}>Category</th>
                                <th style={{ padding: '1rem' }}>Current Price</th>
                                <th style={{ padding: '1rem' }}>Unit</th>
                                <th style={{ padding: '1rem' }}>Last Updated</th>
                                {user.role === 'ADMIN' && <th style={{ padding: '1rem' }}>Admin Actions</th>}
                            </tr>
                        </thead>
                        <tbody>
                            {filteredServices.length === 0 ? (
                                <tr>
                                    <td colSpan="8" style={{ padding: '2rem', textAlign: 'center', color: 'var(--text-secondary)' }}>
                                        No services found.
                                    </td>
                                </tr>
                            ) : (
                                filteredServices.map(s => (
                                    <tr key={s.serviceId} style={{ borderBottom: '1px solid var(--border-color)', opacity: s.active ? 1 : 0.6 }}>
                                        <td style={{ padding: '1rem' }}>
                                            {s.active ? <CheckCircle size={20} color="var(--success)" /> : <XCircle size={20} color="var(--danger)" />}
                                        </td>
                                        <td style={{ padding: '1rem', fontFamily: 'monospace', color: 'var(--brand-color)' }}>{s.serviceId}</td>
                                        <td style={{ padding: '1rem', fontWeight: '500' }}>{s.serviceName}</td>
                                        <td style={{ padding: '1rem' }}>{s.category}</td>
                                        <td style={{ padding: '1rem', fontWeight: 'bold' }}>₹{parseFloat(s.defaultPrice).toFixed(2)}</td>
                                        <td style={{ padding: '1rem' }}>{s.unit}</td>
                                        <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>{s.updatedAt}</td>
                                        {user.role === 'ADMIN' && (
                                            <td style={{ padding: '1rem', display: 'flex', gap: '0.5rem' }}>
                                                <button className="btn btn-secondary" style={{ padding: '0.4rem 0.6rem' }} title="Update Price" onClick={() => { setSelectedService(s); setIsPriceModalOpen(true); setPriceData({ newPrice: s.defaultPrice, reason: '' }); setError(''); }}>
                                                    <Edit size={16} />
                                                </button>
                                                <button className="btn btn-secondary" style={{ padding: '0.4rem 0.6rem' }} title="Price History" onClick={() => viewHistory(s)}>
                                                    <History size={16} />
                                                </button>
                                                <button className="btn btn-secondary" style={{ padding: '0.4rem 0.6rem', color: s.active ? 'var(--danger)' : 'var(--success)' }} title={s.active ? "Disable Service" : "Enable Service"} onClick={() => handleToggleStatus(s.serviceId)}>
                                                    {s.active ? 'Disable' : 'Enable'}
                                                </button>
                                            </td>
                                        )}
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                )}
            </div>

            {/* ADD MODAL */}
            {isAddModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100 }}>
                    <div className="panel" style={{ width: '500px', padding: '2rem', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                        <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                            <PlusCircle size={24} color="var(--brand-color)" /> Define New Service
                        </h2>
                        {error && <div className="error-msg" style={{ marginBottom: '1rem' }}>{error}</div>}
                        <form onSubmit={handleCreateService}>
                            <div className="form-group">
                                <label>Service Name</label>
                                <input type="text" required value={formData.serviceName} onChange={e => setFormData({ ...formData, serviceName: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Category</label>
                                <select required value={formData.category} onChange={e => setFormData({ ...formData, category: e.target.value })}>
                                    <option value="">Select Category...</option>
                                    <option value="CONSULTATION">Consultation</option>
                                    <option value="LABORATORY">Laboratory</option>
                                    <option value="IMAGING">Imaging</option>
                                    <option value="PROCEDURE">Procedure</option>
                                    <option value="ROOM_BED">Room / Bed</option>
                                    <option value="MEDICINE">Medicine</option>
                                    <option value="MISCELLANEOUS">Miscellaneous</option>
                                </select>
                            </div>
                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                <div className="form-group">
                                    <label>Default Price (₹)</label>
                                    <input type="number" step="0.01" min="0" required value={formData.defaultPrice} onChange={e => setFormData({ ...formData, defaultPrice: e.target.value })} />
                                </div>
                                <div className="form-group">
                                    <label>Unit</label>
                                    <input type="text" placeholder="e.g. per visit, per day, per strip" required value={formData.unit} onChange={e => setFormData({ ...formData, unit: e.target.value })} />
                                </div>
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                                <button type="submit" className="btn" style={{ flex: 1 }}>Save Service</button>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => { setIsAddModalOpen(false); setError(''); }}>Cancel</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* UPDATE PRICE MODAL */}
            {isPriceModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100 }}>
                    <div className="panel" style={{ width: '500px', padding: '2rem', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                        <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#d29922' }}>
                            <ShieldAlert size={24} /> Authorized Price Override
                        </h2>
                        <p style={{ color: 'var(--text-secondary)', marginBottom: '1.5rem' }}>
                            You are modifying the core price for <strong>{selectedService?.serviceName}</strong>. This applies to all new bills moving forward. All changes are permanently audited.
                        </p>
                        {error && <div className="error-msg" style={{ marginBottom: '1rem' }}>{error}</div>}
                        <form onSubmit={handlePriceUpdate}>
                            <div className="form-group">
                                <label>New Strict Price (₹)</label>
                                <input type="number" step="0.01" min="0" required value={priceData.newPrice} onChange={e => setPriceData({ ...priceData, newPrice: e.target.value })} />
                            </div>
                            <div className="form-group">
                                <label>Mandatory Reason for Change</label>
                                <textarea required rows="2" placeholder="e.g. Annual inflation adjustment, Management directive" style={{ width: '100%', padding: '0.75rem', borderRadius: '4px', border: '1px solid var(--border-color)', background: 'var(--bg-color)', color: 'var(--text-primary)' }} value={priceData.reason} onChange={e => setPriceData({ ...priceData, reason: e.target.value })}></textarea>
                            </div>
                            <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
                                <button type="submit" className="btn" style={{ flex: 1, background: '#d29922', borderColor: '#d29922' }}>Execute Change</button>
                                <button type="button" className="btn btn-secondary" style={{ flex: 1 }} onClick={() => { setIsPriceModalOpen(false); setError(''); }}>Abort</button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* AUDIT HISTORY MODAL */}
            {isHistoryModalOpen && (
                <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 100 }}>
                    <div className="panel" style={{ width: '800px', padding: '2rem', maxHeight: '90vh', overflowY: 'auto', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
                            <h2 style={{ margin: 0, display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <History size={24} color="var(--brand-color)" /> Audit Trail: {selectedService?.serviceName}
                            </h2>
                            <button className="btn btn-secondary" onClick={() => setIsHistoryModalOpen(false)}>Close</button>
                        </div>

                        {priceHistory.length === 0 ? (
                            <p style={{ color: 'var(--text-secondary)' }}>No history available.</p>
                        ) : (
                            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
                                        <th style={{ padding: '1rem' }}>Timestamp</th>
                                        <th style={{ padding: '1rem' }}>Old Price</th>
                                        <th style={{ padding: '1rem' }}>New Price</th>
                                        <th style={{ padding: '1rem' }}>Changed By</th>
                                        <th style={{ padding: '1rem' }}>Reason</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {priceHistory.map((h, i) => (
                                        <tr key={i} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                            <td style={{ padding: '1rem', fontSize: '0.9rem' }}>{h.changedAt}</td>
                                            <td style={{ padding: '1rem', color: 'var(--text-secondary)' }}>₹{h.oldPrice.toFixed(2)}</td>
                                            <td style={{ padding: '1rem', fontWeight: 'bold', color: 'var(--success)' }}>₹{h.newPrice.toFixed(2)}</td>
                                            <td style={{ padding: '1rem', fontFamily: 'monospace' }}>{h.changedBy}</td>
                                            <td style={{ padding: '1rem', fontSize: '0.9rem' }}>{h.reason}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default Services;
