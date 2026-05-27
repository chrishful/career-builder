import React, { useState, useEffect } from 'react';
import { supabase } from './supabaseClient';
import Dashboard from './components/Dashboard';
import AuthModal from './components/AuthModal';

export default function App() {
  const [session, setSession] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    supabase.auth.getSession().then(({ data: { session } }) => {
      setSession(session);
      setLoading(false);
    });

    const { data: { subscription } } = supabase.auth.onAuthStateChange((_event, session) => {
      setSession(session);
    });

    return () => subscription.unsubscribe();
  }, []);

  if (loading) {
    return (
      <div style={{ backgroundColor: '#0f172a', height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', color: '#94a3b8', fontFamily: 'sans-serif' }}>
        Initialising platform pipeline...
      </div>
    );
  }

  return (
    <div style={{ position: 'relative', minHeight: '100vh', backgroundColor: '#0f172a' }}>
      {/* Background Dashboard Layer */}
      <div style={{ filter: !session ? 'blur(8px)' : 'none', transition: 'filter 0.3s ease' }}>
        <Dashboard />
      </div>

      {/* Persistent Auth Popup Overlay */}
      {!session && <AuthModal />}
    </div>
  );
}