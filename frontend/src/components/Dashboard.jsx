import React, { useState, useEffect } from 'react';
import ApplicationRow from './ApplicationRow';
import MetricCard from './MetricCard';
import ChatSidebar from './ChatSidebar';
import { getSessionToken } from '../supabaseClient';
import styles from '../style/styles.module.css';
import { createClient } from '@supabase/supabase-js';

const supabase = createClient(
  process.env.REACT_APP_SUPABASE_URL,
  process.env.REACT_APP_SUPABASE_ANON_KEY
);

const backendUrl = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';

export default function Dashboard() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('all');
  const [chatOpen, setChatOpen] = useState(false);

  const filteredApplications = activeFilter === 'all'
    ? applications
    : applications.filter(app => app.status?.toLowerCase() === activeFilter.toLowerCase());


  useEffect(() => {
    const channel = supabase
      .channel('applications-realtime')
      .on(
        'postgres_changes',
        { event: '*', schema: 'public', table: 'applications' },
        (payload) => {
          if (payload.eventType === 'INSERT') {
            setApplications(prev => [...prev, payload.new]);
          } else if (payload.eventType === 'UPDATE') {
            setApplications(prev =>
              prev.map(app => app.id === payload.new.id ? payload.new : app)
            );
          } else if (payload.eventType === 'DELETE') {
            setApplications(prev => prev.filter(app => app.id !== payload.old.id));
          }
        }
      )
      .subscribe();

    return () => supabase.removeChannel(channel);
  }, []);

  const handleStatusChange = async (id, nextStatus) => {
    const token = await getSessionToken();
    try {
      const response = await fetch(`${backendUrl}/v1/applications/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Basic ${token}`
        },
        body: JSON.stringify({ status: nextStatus }),
      });

      if (!response.ok) {
        throw new Error(`Server returned status code ${response.status}`);
      }

      setApplications(prevApps =>
        prevApps.map(app => app.id === id ? { ...app, status: nextStatus } : app)
      );
    } catch (error) {
      console.error('Network failure executing patch:', error);
    }
  };

  const handleDelete = async (id) => {
    const token = await getSessionToken();
    try {
      const response = await fetch(`${backendUrl}/v1/applications/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Basic ${token}` }
      });

      if (!response.ok) {
        throw new Error(`Server returned status code ${response.status}`);
      }

      setApplications(prevApps => prevApps.filter(app => app.id !== id));
    } catch (error) {
      console.error('Failed processing delete operational flow:', error);
    }
  };

  useEffect(() => {
    const loadInitialData = async () => {
      const token = await getSessionToken();
      if (!token) return;

      try {
        const res = await fetch(`${backendUrl}/v1/applications`, {
          headers: { 'Authorization': `Basic ${token}` }
        });

        if (!res.ok) throw new Error(`Fetch failed with status ${res.status}`);

        const data = await res.json();
        setApplications(data);
      } catch (err) {
        console.error('Data pull metrics crash:', err);
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, []);

  const totalApplied = applications.length;
  const activeCount = applications.filter(app => app.status && ['applied', 'interview'].includes(app.status.toLowerCase())).length;
  const inProgressCount = applications.filter(app => app.status && app.status.toLowerCase() === 'interview').length;
  const rejectedCount = applications.filter(app => app.status && app.status.toLowerCase() === 'rejected').length;

  return (
    <div className={styles.appContainer}>
      <header className={styles.header}>
        <div className={styles.logo}>Role<span className={styles.logoAccent}>Trackr</span></div>
        <div className={styles.tagline}>your one-stop-shop for application tracking</div>
      </header>

      <main className={styles.mainContent}>
        <div className={styles.leftColumn}>
          <div className={styles.metricsGrid}>
            <MetricCard count={totalApplied} label="Applied" />
            <MetricCard count={activeCount} label="Active" />
            <MetricCard count={inProgressCount} label="In Progress" isActiveTrack={true} />
            <MetricCard count={rejectedCount} label="Rejected" />
          </div>

          <div className={styles.tableContainer}>
            <div className={styles.tableHeaderActions}>
              <div className={styles.tableTitle}>Applications</div>
              <div className={styles.filterGroup}>
                {['all', 'applied', 'interview', 'rejected'].map(filter => (
                  <button
                    key={filter}
                    onClick={() => setActiveFilter(filter)}
                    className={`${styles.filterBtn} ${activeFilter === filter ? styles.filterBtnActive : ''}`}
                  >
                    {filter}
                  </button>
                ))}
              </div>
            </div>

            {loading ? (
              <div className={styles.stateMessage}>Loading application pipeline data...</div>
            ) : filteredApplications.length === 0 ? (
              <div className={styles.stateMessage}>No job applications found matching this status.</div>
            ) : (
              <div className={styles.tableWrapper}>
                <table className={styles.table}>
                  <thead>
                    <tr>
                      <th className={styles.th} style={{ width: '25%' }}>Company</th>
                      <th className={styles.th} style={{ width: '35%' }}>Role</th>
                      <th className={styles.th} style={{ width: '15%' }}>Status</th>
                      <th className={styles.th} style={{ width: '15%' }}>Applied</th>
                      <th className={styles.th} style={{ width: '10%' }}>Notes</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredApplications.map((app, idx) => (
                      <ApplicationRow
                        key={app.id || idx}
                        app={app}
                        onDelete={handleDelete}
                        onStatusChange={handleStatusChange}
                      />
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {/* Desktop sidebar — hidden on mobile */}
        <div className={styles.desktopSidebar}>
          <ChatSidebar />
        </div>
      </main>

      {/* Mobile: floating chat button */}
      <button
        className={styles.chatFab}
        onClick={() => setChatOpen(true)}
        aria-label="Open chat"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path>
        </svg>
        <span>Chat</span>
      </button>

      {/* Mobile: chat drawer overlay */}
      {chatOpen && (
        <div className={styles.chatDrawerOverlay} onClick={() => setChatOpen(false)}>
          <div className={styles.chatDrawer} onClick={e => e.stopPropagation()}>
            <div className={styles.chatDrawerHandle}>
              <button
                className={styles.chatDrawerClose}
                onClick={() => setChatOpen(false)}
                aria-label="Close chat"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"></line>
                  <line x1="6" y1="6" x2="18" y2="18"></line>
                </svg>
              </button>
            </div>
            <ChatSidebar />
          </div>
        </div>
      )}
    </div>
  );
}