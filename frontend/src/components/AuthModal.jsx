import React, { useState } from 'react';
import { supabase } from '../supabaseClient';

export default function AuthModal() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setErrorMsg('');

    const { error } = await supabase.auth.signInWithPassword({ email, password });

    if (error) {
      setErrorMsg(error.message);
    }
    setLoading(false);
  };

  return (
    <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: '#020617ba', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 9999, fontFamily: 'sans-serif' }}>
      <form onSubmit={handleLogin} style={{ width: '360px', padding: '2.5rem', backgroundColor: '#1e293b', borderRadius: '12px', boxShadow: '0 20px 25px -5px rgb(0 0 0 / 0.5)', border: '1px solid #334155' }}>
        <h2 style={{ color: '#f8fafc', fontSize: '1.35rem', fontWeight: '600', marginBottom: '0.25rem', textAlign: 'center' }}>Access Restricted</h2>
        <p style={{ color: '#64748b', fontSize: '0.85rem', textAlign: 'center', marginBottom: '1.5rem' }}>Please authenticate to view application metrics.</p>

        {errorMsg && (
          <div style={{ color: '#ef4444', backgroundColor: '#ef444415', padding: '0.75rem', borderRadius: '6px', fontSize: '0.875rem', marginBottom: '1rem', border: '1px solid #ef444430' }}>
            {errorMsg}
          </div>
        )}

        <div style={{ marginBottom: '1.25rem' }}>
          <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.815rem', marginBottom: '0.5rem' }}>Email</label>
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #334155', backgroundColor: '#0f172a', color: '#f8fafc', outline: 'none', boxSizing: 'border-box' }} />
        </div>

        <div style={{ marginBottom: '1.75rem' }}>
          <label style={{ display: 'block', color: '#94a3b8', fontSize: '0.815rem', marginBottom: '0.5rem' }}>Password</label>
          <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', border: '1px solid #334155', backgroundColor: '#0f172a', color: '#f8fafc', outline: 'none', boxSizing: 'border-box' }} />
        </div>

        <button type="submit" disabled={loading} style={{ width: '100%', padding: '0.75rem', borderRadius: '6px', backgroundColor: '#3b82f6', color: '#fff', fontWeight: '600', border: 'none', cursor: 'pointer', opacity: loading ? 0.7 : 1 }}>
          {loading ? 'Verifying...' : 'Unlock Dashboard'}
        </button>
      </form>
    </div>
  );
}