import React, { useState, useEffect, useContext } from 'react';
import api from '../api/axios';
import { AuthContext } from '../context/AuthContext';
import {
    UserPlus,
    Plus,
    Trash2,
    CreditCard,
    CheckCircle,
    FileText,
    Printer,
    Clock,
    Stethoscope,
    ShoppingCart,
    DollarSign
} from 'lucide-react';

const OpdBilling = () => {
    const { user } = useContext(AuthContext);

    // States
    const [services, setServices] = useState([]);
    const [filterCategory, setFilterCategory] = useState('ALL');
    const [searchServiceQuery, setSearchServiceQuery] = useState('');

    const [currentVisit, setCurrentVisit] = useState(null);
    const [currentBill, setCurrentBill] = useState(null);

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Quick Patient Entry State
    const [firstName, setFirstName] = useState('');
    const [lastName, setLastName] = useState('');
    const [phone, setPhone] = useState('');
    const [age, setAge] = useState('');
    const [gender, setGender] = useState('Male');
    const [address, setAddress] = useState('');

    // Visit Creation State
    const [doctor, setDoctor] = useState('');
    const [department, setDepartment] = useState('');
    const [visitCategory, setVisitCategory] = useState('CASH');
    const [visitNotes, setVisitNotes] = useState('');

    // Payment State
    const [isPaymentModalOpen, setIsPaymentModalOpen] = useState(false);
    const [paymentMode, setPaymentMode] = useState('CASH');
    const [paymentAmount, setPaymentAmount] = useState('');

    // History State
    const [recentVisits, setRecentVisits] = useState([]);

    useEffect(() => {
        fetchServices();
        fetchRecentVisits();
    }, []);

    const fetchServices = async () => {
        try {
            const res = await api.get('/services');
            setServices(res.data);
        } catch (err) {
            setError('Failed to fetch services catalog');
        }
    };

    const fetchRecentVisits = async () => {
        try {
            const res = await api.get('/opd/visits');
            setRecentVisits(res.data.slice(0, 5));
        } catch (err) {
            console.error('Failed to fetch recent visits');
        }
    };

    const resetForm = () => {
        setCurrentVisit(null);
        setCurrentBill(null);
        setFirstName('');
        setLastName('');
        setPhone('');
        setAge('');
        setGender('Male');
        setAddress('');
        setDoctor('');
        setDepartment('');
        setVisitCategory('CASH');
        setVisitNotes('');
    };

    const startQuickBilling = async () => {
        if (!firstName || !lastName || !phone || !age || !doctor || !department) {
            setError('Missing mandatory fields. Name, Phone, Age, Doctor, Dept are required.');
            return;
        }
        setLoading(true);
        setError('');
        try {
            const data = {
                firstName,
                lastName,
                phone,
                age: parseInt(age),
                gender,
                address,
                assignedDoctor: doctor,
                department,
                visitCategory,
                notes: visitNotes
            };
            const res = await api.post('/opd/quick-billing', data);
            setCurrentBill(res.data);
            setCurrentVisit({
                visitId: res.data.visitId,
                assignedDoctor: doctor,
                department,
                visitCategory,
                visitDateTime: res.data.createdAt || new Date().toISOString()
            });
            setSuccess('OPD Billing Initialized');
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to initialize billing');
        } finally {
            setLoading(false);
        }
    };

    const fetchBill = async (billId) => {
        try {
            const res = await api.get(`/opd/bills/${billId}`);
            setCurrentBill(res.data);
        } catch (err) {
            setError('Failed to fetch bill details');
        }
    };

    const addServiceToBill = async (service) => {
        if (!currentBill) return;
        try {
            const res = await api.post(`/opd/bills/${currentBill.billId}/services`, {
                serviceId: service.serviceId,
                quantity: 1
            });
            setCurrentBill(res.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to add service');
        }
    };

    const removeServiceFromBill = async (itemId) => {
        try {
            const res = await api.delete(`/opd/bills/${currentBill.billId}/items/${itemId}`);
            setCurrentBill(res.data);
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to remove service');
        }
    };

    const processPayment = async () => {
        if (!paymentAmount || parseFloat(paymentAmount) <= 0) {
            setError('Invalid payment amount');
            return;
        }
        setLoading(true);
        try {
            const res = await api.post(`/opd/bills/${currentBill.billId}/payments`, {
                paymentMode,
                amount: parseFloat(paymentAmount)
            });
            setCurrentBill(res.data);
            setIsPaymentModalOpen(false);
            setPaymentAmount('');
            setSuccess('Payment recorded');
            setTimeout(() => setSuccess(''), 3000);
        } catch (err) {
            setError(err.response?.data?.message || 'Payment failed');
        } finally {
            setLoading(false);
        }
    };

    const finalizeBill = async () => {
        setLoading(true);
        try {
            const res = await api.post(`/opd/bills/${currentBill.billId}/finalize`, {});
            setCurrentBill(res.data);
            setSuccess('Bill Finalized & Closed');
            setTimeout(() => setSuccess(''), 5000);
        } catch (err) {
            setError(err.response?.data?.message || 'Finalization failed');
        } finally {
            setLoading(false);
        }
    };

    const calculateBalance = () => {
        if (!currentBill) return 0;
        const totalPaid = currentBill.payments.reduce((sum, p) => sum + p.amount, 0);
        return currentBill.grandTotal - totalPaid;
    };

    const filteredServices = services.filter(s =>
        (filterCategory === 'ALL' || s.category === filterCategory) &&
        (s.serviceName.toLowerCase().includes(searchServiceQuery.toLowerCase()) ||
            s.serviceId.toLowerCase().includes(searchServiceQuery.toLowerCase()))
    );

    return (
        <div className="billing-container">
            {/* Header */}
            <div className="flex-between" style={{ marginBottom: '1.5rem', borderBottom: '1px solid var(--border-color)', paddingBottom: '1rem' }}>
                <h2 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <CreditCard color="var(--brand-color)" /> OPD Management & Billing
                </h2>
                <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                    <button className="btn btn-secondary" onClick={resetForm} style={{ fontSize: '0.8rem', padding: '0.5rem 1rem' }}>
                        <Plus size={14} /> New Bill
                    </button>
                    {error && <div className="error-msg" style={{ padding: '0.5rem 1rem' }}>{error}</div>}
                    {success && <div style={{ color: 'var(--success)', background: 'rgba(63, 185, 80, 0.1)', padding: '0.5rem 1rem', borderRadius: '6px', border: '1px solid var(--success)' }}>{success}</div>}
                </div>
            </div>

            <div className="billing-grid">
                {/* Left Column: Quick Entry Form */}
                <div className="billing-col">
                    <div className="panel" style={{ flex: 1, overflowY: 'auto' }}>
                        {!currentVisit ? (
                            <>
                                <h3><UserPlus size={18} /> Quick Patient Entry</h3>
                                <div style={{ marginTop: '1rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                    <div className="form-group">
                                        <label>First Name *</label>
                                        <input type="text" className="form-input" value={firstName} onChange={(e) => setFirstName(e.target.value)} placeholder="First Name" />
                                    </div>
                                    <div className="form-group">
                                        <label>Last Name *</label>
                                        <input type="text" className="form-input" value={lastName} onChange={(e) => setLastName(e.target.value)} placeholder="Last Name" />
                                    </div>
                                </div>
                                <div className="form-group" style={{ marginTop: '1rem' }}>
                                    <label>Phone Number *</label>
                                    <input type="text" className="form-input" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="Mobile Number" />
                                </div>
                                <div style={{ marginTop: '1rem', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
                                    <div className="form-group">
                                        <label>Age *</label>
                                        <input type="number" className="form-input" value={age} onChange={(e) => setAge(e.target.value)} placeholder="Age" />
                                    </div>
                                    <div className="form-group">
                                        <label>Gender *</label>
                                        <select className="form-input" value={gender} onChange={(e) => setGender(e.target.value)}>
                                            <option value="Male">Male</option>
                                            <option value="Female">Female</option>
                                            <option value="Other">Other</option>
                                        </select>
                                    </div>
                                </div>
                                <div className="form-group" style={{ marginTop: '1rem' }}>
                                    <label>Address (Optional)</label>
                                    <input type="text" className="form-input" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Address" />
                                </div>

                                <h3 style={{ marginTop: '2rem' }}><Stethoscope size={18} /> Visit Information</h3>
                                <div className="form-group" style={{ marginTop: '1rem' }}>
                                    <label>Consulting Doctor *</label>
                                    <input
                                        type="text"
                                        className="form-input"
                                        value={doctor}
                                        onChange={(e) => setDoctor(e.target.value)}
                                        placeholder="e.g. Dr. Sharma"
                                    />
                                </div>
                                <div className="form-group" style={{ marginTop: '1rem' }}>
                                    <label>Department *</label>
                                    <select
                                        className="form-input"
                                        value={department}
                                        onChange={(e) => setDepartment(e.target.value)}
                                    >
                                        <option value="">Select Department...</option>
                                        <option value="General Medicine">General Medicine</option>
                                        <option value="Cardiology">Cardiology</option>
                                        <option value="Pediatrics">Pediatrics</option>
                                        <option value="Orthopedics">Orthopedics</option>
                                        <option value="Gynaecology">Gynaecology</option>
                                        <option value="ENT">ENT</option>
                                        <option value="Dermatology">Dermatology</option>
                                    </select>
                                </div>
                                <div className="form-group" style={{ marginTop: '1rem' }}>
                                    <label>Visit Category *</label>
                                    <select
                                        className="form-input"
                                        value={visitCategory}
                                        onChange={(e) => setVisitCategory(e.target.value)}
                                    >
                                        <option value="CASH">Cash</option>
                                        <option value="TPA">TPA</option>
                                        <option value="INSURANCE">Insurance</option>
                                        <option value="SCHEME">Scheme</option>
                                        <option value="COMPLIMENTARY">Complimentary</option>
                                    </select>
                                </div>

                                <button className="btn" style={{ width: '100%', marginTop: '2rem' }} onClick={startQuickBilling} disabled={loading}>
                                    <Plus size={18} /> Start Billing
                                </button>
                            </>
                        ) : (
                            <div style={{ padding: '0.5rem' }}>
                                <h3><CheckCircle size={18} color="var(--success)" /> Patient & Visit Locked</h3>
                                <div className="selected-patient-box" style={{ marginTop: '1rem', padding: '1.5rem', background: 'rgba(88, 166, 255, 0.05)', borderRadius: '6px', border: '1px solid var(--brand-color)' }}>
                                    <div style={{ fontWeight: 'bold', fontSize: '1.2rem', marginBottom: '0.5rem' }}>{firstName} {lastName}</div>
                                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)' }}>UHID: {currentBill.patientId}</div>
                                    <div style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: '1rem' }}>Phone: {phone} | Age: {age} | {gender}</div>

                                    <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '1rem' }}>
                                        <div style={{ marginBottom: '0.4rem' }}><strong>Visit ID:</strong> {currentVisit.visitId}</div>
                                        <div style={{ marginBottom: '0.4rem' }}><strong>Doctor:</strong> {currentVisit.assignedDoctor}</div>
                                        <div style={{ marginBottom: '0.4rem' }}><strong>Dept:</strong> {currentVisit.department}</div>
                                        <div style={{ fontSize: '0.8rem', opacity: 0.7 }}>{new Date(currentVisit.visitDateTime).toLocaleString()}</div>
                                    </div>
                                </div>
                                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '1rem', textAlign: 'center' }}>
                                    Details are locked once billing starts. Use "New Bill" to reset.
                                </p>
                            </div>
                        )}

                        {/* Integrated Recent Activity - Inside Sidebar */}
                        <div style={{ marginTop: '2.5rem', borderTop: '1px solid var(--border-color)', paddingTop: '1.5rem' }}>
                            <h4 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '1rem', color: 'var(--text-secondary)', fontSize: '0.9rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                                <Clock size={16} /> Recent OPD Activity
                            </h4>
                            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
                                {recentVisits.map(v => (
                                    <div key={v.visitId} style={{ padding: '0.75rem', background: 'rgba(255,255,255,0.02)', border: '1px solid var(--border-color)', borderRadius: '6px' }}>
                                        <div className="flex-between" style={{ marginBottom: '0.25rem' }}>
                                            <span style={{ fontWeight: 'bold', fontSize: '0.85rem' }}>{v.patientName}</span>
                                            <span style={{ fontSize: '0.7rem', opacity: 0.6 }}>{v.visitId}</span>
                                        </div>
                                        <div className="flex-between">
                                            <span style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{v.assignedDoctor}</span>
                                            <button
                                                style={{ background: 'none', border: 'none', color: 'var(--brand-color)', fontSize: '0.75rem', cursor: 'pointer', padding: 0 }}
                                                onClick={() => {
                                                    setCurrentVisit(v);
                                                    fetchBill(v.billId);
                                                }}
                                            >
                                                View
                                            </button>
                                        </div>
                                    </div>
                                ))}
                                {recentVisits.length === 0 && <p style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', textAlign: 'center' }}>No recent activity</p>}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Middle Column: Service Catalog */}
                <div className="billing-col" style={{ flex: 1.5 }}>
                    <div className="panel" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <h3><ShoppingCart size={18} /> Service Catalog</h3>
                        <div style={{ display: 'flex', gap: '0.5rem', margin: '1rem 0' }}>
                            <input
                                type="text"
                                placeholder="Filter services..."
                                value={searchServiceQuery}
                                onChange={(e) => setSearchServiceQuery(e.target.value)}
                                style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)', color: 'var(--text-primary)', padding: '0.75rem', borderRadius: '6px', flex: 1 }}
                            />
                            <select
                                value={filterCategory}
                                onChange={(e) => setFilterCategory(e.target.value)}
                                style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)', color: 'var(--text-primary)', padding: '0.75rem', borderRadius: '6px' }}
                            >
                                <option value="ALL">All Categories</option>
                                <option value="CONSULTATION">Consultation</option>
                                <option value="LABORATORY">Laboratory</option>
                                <option value="IMAGING">Imaging / Radiology</option>
                                <option value="PROCEDURE">Procedures</option>
                                <option value="MEDICINE">Medicines</option>
                                <option value="MISCELLANEOUS">Miscellaneous</option>
                            </select>
                        </div>

                        <div className="service-list" style={{ flex: 1, overflowY: 'auto' }}>
                            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                                <thead style={{ position: 'sticky', top: 0, background: 'var(--panel-bg)', zIndex: 1, borderBottom: '2px solid var(--border-color)' }}>
                                    <tr>
                                        <th style={{ textAlign: 'left', padding: '0.75rem' }}>Service Name</th>
                                        <th style={{ textAlign: 'right', padding: '0.75rem' }}>Price</th>
                                        <th style={{ textAlign: 'center', padding: '0.75rem' }}>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {filteredServices.map(s => (
                                        <tr key={s.serviceId} style={{ borderBottom: '1px solid var(--border-color)' }}>
                                            <td style={{ padding: '0.75rem' }}>
                                                <div>{s.serviceName}</div>
                                                <div style={{ fontSize: '0.7rem', color: 'var(--text-secondary)' }}>{s.serviceId}</div>
                                            </td>
                                            <td style={{ padding: '0.75rem', textAlign: 'right' }}>₹{s.defaultPrice.toFixed(2)}</td>
                                            <td style={{ padding: '0.75rem', textAlign: 'center' }}>
                                                <button
                                                    className="btn btn-secondary"
                                                    style={{ padding: '0.4rem', borderRadius: '4px', width: 'auto' }}
                                                    onClick={() => addServiceToBill(s)}
                                                    disabled={!currentBill || currentBill.status !== 'DRAFT'}
                                                >
                                                    <Plus size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                {/* Right Column: Bill Summary & Payment */}
                <div className="billing-col">
                    <div className="panel" style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <div className="flex-between">
                            <h3><FileText size={18} /> Bill Summary</h3>
                            {currentBill && (
                                <div style={{ fontSize: '0.8rem', opacity: 0.7 }}>{currentBill.billId}</div>
                            )}
                        </div>

                        {!currentBill ? (
                            <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', color: 'var(--text-secondary)', textAlign: 'center' }}>
                                <p>Initialize a visit to <br /> generate a bill</p>
                            </div>
                        ) : (
                            <>
                                <div className="bill-items" style={{ flex: 1, overflowY: 'auto', margin: '1rem 0' }}>
                                    {currentBill.items.length === 0 && <p style={{ textAlign: 'center', color: 'var(--text-secondary)', marginTop: '2rem' }}>No services added yet</p>}
                                    {currentBill.items.map(item => (
                                        <div key={item.id} className="bill-item-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '0.5rem 0', borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                                            <div style={{ flex: 1 }}>
                                                <div style={{ fontSize: '0.9rem' }}>{item.serviceName}</div>
                                                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>{item.quantity} x ₹{item.unitPrice.toFixed(2)}</div>
                                            </div>
                                            <div style={{ textAlign: 'right', marginLeft: '1rem' }}>
                                                <div style={{ fontWeight: 'bold' }}>₹{item.totalAmount.toFixed(2)}</div>
                                                {currentBill.status === 'DRAFT' && (
                                                    <button
                                                        onClick={() => removeServiceFromBill(item.id)}
                                                        style={{ background: 'none', border: 'none', color: 'var(--error)', cursor: 'pointer', padding: '0.2rem' }}
                                                    >
                                                        <Trash2 size={14} />
                                                    </button>
                                                )}
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                <div className="bill-totals" style={{ borderTop: '2px solid var(--border-color)', paddingTop: '1rem' }}>
                                    <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                        <span>Subtotal</span>
                                        <span>₹{currentBill.subTotal.toFixed(2)}</span>
                                    </div>
                                    <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                        <span>Tax</span>
                                        <span>₹{currentBill.tax.toFixed(2)}</span>
                                    </div>
                                    <div className="flex-between" style={{ marginBottom: '1rem', fontSize: '1.2rem', fontWeight: 'bold', color: 'var(--brand-color)' }}>
                                        <span>Grand Total</span>
                                        <span>₹{currentBill.grandTotal.toFixed(2)}</span>
                                    </div>

                                    {/* Payment Section */}
                                    <div style={{ background: 'rgba(0,0,0,0.2)', padding: '1rem', borderRadius: '6px', marginBottom: '1rem' }}>
                                        <div className="flex-between" style={{ marginBottom: '0.5rem' }}>
                                            <span style={{ fontSize: '0.9rem' }}>Total Paid</span>
                                            <span style={{ color: 'var(--success)' }}>₹{currentBill.payments.reduce((s, p) => s + p.amount, 0).toFixed(2)}</span>
                                        </div>
                                        <div className="flex-between" style={{ fontWeight: 'bold' }}>
                                            <span>Balance Due</span>
                                            <span style={{ color: calculateBalance() > 0 ? 'var(--error)' : 'var(--success)' }}>₹{calculateBalance().toFixed(2)}</span>
                                        </div>
                                    </div>

                                    <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.5rem' }}>
                                        <button
                                            className="btn btn-secondary"
                                            disabled={currentBill.status === 'LOCKED' || currentBill.status === 'CANCELLED' || calculateBalance() <= 0}
                                            onClick={() => setIsPaymentModalOpen(true)}
                                        >
                                            <DollarSign size={18} /> Payment
                                        </button>
                                        <button
                                            className="btn"
                                            disabled={currentBill.status !== 'PAID'}
                                            onClick={finalizeBill}
                                            style={{ background: currentBill.status === 'PAID' ? 'var(--brand-color)' : '' }}
                                        >
                                            <CheckCircle size={18} /> Finalize
                                        </button>
                                    </div>

                                    {currentBill.status === 'LOCKED' && (
                                        <button className="btn" style={{ width: '100%', marginTop: '0.5rem', background: 'var(--success)' }} onClick={() => window.print()}>
                                            <Printer size={18} /> Print Receipt
                                        </button>
                                    )}
                                </div>
                            </>
                        )}
                    </div>
                </div>
            </div>

            {/* Printable Receipt Section (Hidden on screen) */}
            {
                currentBill && (
                    <div id="printable-receipt" className="print-only">
                        <div style={{ padding: '2rem', color: '#000', background: '#fff' }}>
                            <div style={{ textAlign: 'center', marginBottom: '1.5rem', borderBottom: '2px solid #000', paddingBottom: '1rem' }}>
                                <h1 style={{ margin: 0 }}>HMS Core Hospital</h1>
                                <p style={{ margin: '0.2rem 0' }}>123 Health Street, Medical City</p>
                                <p style={{ margin: '0.2rem 0' }}>Phone: +91 98765 43210</p>
                                <h2 style={{ marginTop: '1rem', textTransform: 'uppercase' }}>OPD PAYMENT RECEIPT</h2>
                            </div>

                            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
                                <div>
                                    <p><strong>Receipt ID:</strong> {currentBill.payments[currentBill.payments.length - 1]?.receiptId || 'N/A'}</p>
                                    <p><strong>Date:</strong> {currentBill.createdAt ? new Date(currentBill.createdAt).toLocaleString() : new Date().toLocaleString()}</p>
                                    <p><strong>Visit ID:</strong> {currentBill.visitId}</p>
                                    <p><strong>Category:</strong> {currentVisit?.visitCategory}</p>
                                </div>
                                <div>
                                    <p><strong>Patient Name:</strong> {currentBill.patientName}</p>
                                    <p><strong>Patient ID:</strong> {currentBill.patientId}</p>
                                    <p><strong>Age/Sex:</strong> {currentBill.age} / {currentBill.gender}</p>
                                    <p><strong>Phone:</strong> {currentBill.phone}</p>
                                    <p><strong>Doctor:</strong> {currentVisit?.assignedDoctor || 'N/A'}</p>
                                    <p><strong>Dept:</strong> {currentVisit?.department}</p>
                                </div>
                            </div>

                            <table style={{ width: '100%', borderCollapse: 'collapse', marginBottom: '2rem' }}>
                                <thead>
                                    <tr style={{ borderBottom: '1px solid #000' }}>
                                        <th style={{ textAlign: 'left', padding: '0.5rem' }}>Service Description</th>
                                        <th style={{ textAlign: 'center', padding: '0.5rem' }}>Qty</th>
                                        <th style={{ textAlign: 'right', padding: '0.5rem' }}>Price</th>
                                        <th style={{ textAlign: 'right', padding: '0.5rem' }}>Total</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {currentBill.items.map(item => (
                                        <tr key={item.id}>
                                            <td style={{ padding: '0.5rem' }}>{item.serviceName}</td>
                                            <td style={{ padding: '0.5rem', textAlign: 'center' }}>{item.quantity}</td>
                                            <td style={{ padding: '0.5rem', textAlign: 'right' }}>₹{item.unitPrice.toFixed(2)}</td>
                                            <td style={{ padding: '0.5rem', textAlign: 'right' }}>₹{item.totalAmount.toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>

                            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', borderTop: '2px solid #000', paddingTop: '1rem' }}>
                                <div style={{ flex: 1 }}>
                                    <p style={{ margin: 0 }}><strong>Amount in Words:</strong></p>
                                    <p style={{ margin: 0, fontStyle: 'italic' }}>{currentBill.amountInWords}</p>
                                </div>
                                <div style={{ width: '250px' }}>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                        <span>Subtotal:</span>
                                        <span>₹{currentBill.subTotal.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                                        <span>Tax (5%):</span>
                                        <span>₹{currentBill.tax.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem', borderTop: '1px solid #000', paddingTop: '0.5rem', fontWeight: 'bold', fontSize: '1.1rem' }}>
                                        <span>Grand Total:</span>
                                        <span>₹{currentBill.grandTotal.toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '1rem', color: '#000' }}>
                                        <span>Amount Paid:</span>
                                        <span style={{ textDecoration: 'underline' }}>₹{currentBill.payments.reduce((s, p) => s + p.amount, 0).toFixed(2)}</span>
                                    </div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: '0.5rem', fontWeight: 'bold' }}>
                                        <span>Balance:</span>
                                        <span>₹{calculateBalance().toFixed(2)}</span>
                                    </div>
                                </div>
                            </div>

                            <div style={{ marginTop: '3rem', display: 'flex', justifyContent: 'space-between' }}>
                                <div style={{ textAlign: 'center' }}>
                                    <div style={{ height: '50px' }}></div>
                                    <p style={{ borderTop: '1px solid #000', width: '150px' }}>Patient/Relative</p>
                                </div>
                                <div style={{ textAlign: 'center' }}>
                                    <div style={{ height: '50px', display: 'flex', alignItems: 'flex-end', justifyContent: 'center', fontSize: '0.8rem' }}>{currentBill.createdBy}</div>
                                    <p style={{ borderTop: '1px solid #000', width: '150px' }}>Auth Signature</p>
                                </div>
                            </div>

                            <div style={{ marginTop: '2rem', textAlign: 'center', fontSize: '0.8rem', borderTop: '1px dotted #ccc', paddingTop: '1rem' }}>
                                <p>This is a system generated receipt. Visit ID: {currentBill.visitId} | Receipt No: {currentBill.payments[currentBill.payments.length - 1]?.receiptId}</p>
                                <p>Thank you for choosing HMS Core. Wish you a speedy recovery.</p>
                            </div>
                        </div>
                    </div>
                )
            }


            {/* Payment Modal */}
            {
                isPaymentModalOpen && (
                    <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(8px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 }}>
                        <div className="panel" style={{ width: '400px', padding: '2rem', background: 'var(--panel-bg)', border: '1px solid var(--border-color)', borderRadius: '12px', boxShadow: '0 8px 32px rgba(0,0,0,0.5)' }}>
                            <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                <CreditCard size={24} color="var(--brand-color)" /> Record Payment
                            </h2>
                            <div className="form-group" style={{ marginBottom: '1.5rem' }}>
                                <label>Amount (Max ₹{calculateBalance().toFixed(2)})</label>
                                <input
                                    type="number"
                                    className="form-input"
                                    value={paymentAmount}
                                    onChange={(e) => setPaymentAmount(e.target.value)}
                                    placeholder="0.00"
                                    autoFocus
                                    style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)', color: 'var(--text-primary)', padding: '1rem', borderRadius: '6px', width: '100%', fontSize: '1.5rem' }}
                                />
                            </div>
                            <div className="form-group" style={{ marginBottom: '2rem' }}>
                                <label>Payment Mode</label>
                                <select
                                    className="form-input"
                                    value={paymentMode}
                                    onChange={(e) => setPaymentMode(e.target.value)}
                                    style={{ background: 'var(--bg-color)', border: '1px solid var(--border-color)', color: 'var(--text-primary)', padding: '0.75rem', borderRadius: '6px', width: '100%' }}
                                >
                                    <option value="CASH">Cash</option>
                                    <option value="CARD">Debit/Credit Card</option>
                                    <option value="UPI">UPI / Digital</option>
                                    <option value="BANK_TRANSFER">Bank Transfer</option>
                                </select>
                            </div>
                            <div style={{ display: 'flex', gap: '1rem' }}>
                                <button className="btn btn-secondary" onClick={() => setIsPaymentModalOpen(false)} style={{ flex: 1 }}>Cancel</button>
                                <button className="btn" onClick={processPayment} disabled={loading} style={{ flex: 2 }}>Confirm Payment</button>
                            </div>
                        </div>
                    </div>
                )
            }


            <style>{`
                .billing-grid {
                    display: grid;
                    grid-template-columns: 350px 1fr 400px;
                    gap: 1.5rem;
                    min-height: calc(100vh - 200px);
                }
                .billing-col {
                    display: flex;
                    flex-direction: column;
                    height: 100%;
                }
                .panel {
                    background: var(--panel-bg);
                    border: 1px solid var(--border-color);
                    border-radius: 8px;
                    padding: 1.5rem;
                }
                .panel h3 {
                    margin: 0;
                    font-size: 1.1rem;
                    display: flex;
                    align-items: center;
                    gap: 0.5rem;
                }
                .flex-between {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                }
                .search-item:hover {
                    background: rgba(88, 166, 255, 0.1);
                }
                .service-list th {
                    font-size: 0.85rem;
                    color: var(--text-secondary);
                    text-transform: uppercase;
                    letter-spacing: 0.05em;
                }
                .bill-items::-webkit-scrollbar,
                .service-list::-webkit-scrollbar {
                    width: 6px;
                }
                .bill-items::-webkit-scrollbar-thumb,
                .service-list::-webkit-scrollbar-thumb {
                    background: var(--border-color);
                    border-radius: 10px;
                }
                .print-only {
                    display: none;
                }
                @media print {
                    body * {
                        visibility: hidden;
                    }
                    .print-only, .print-only * {
                        visibility: visible;
                    }
                    .print-only {
                        display: block;
                        position: absolute;
                        left: 0;
                        top: 0;
                        width: 100%;
                    }
                }
            `}</style>
        </div >
    );
};

export default OpdBilling;
