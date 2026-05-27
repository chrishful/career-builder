import React, { useState, useEffect } from 'react';
import ApplicationRow from './ApplicationRow';
import MetricCard from './MetricCard';
import ChatSidebar from './ChatSidebar';
import { getSessionToken } from '../supabaseClient';
import { styles } from '../style/styles';

export default function Dashboard() {
  const [applications, setApplications] = useState([]);
  const [filteredApplications, setFilteredApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('all');

  const handleStatusChange = async (id, nextStatus) => {
    const token = await getSessionToken();
    try {
      const response = await fetch(`/v1/applications/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ status: nextStatus }),
      });

      if (response.ok) {
        setApplications(prevApps =>
          prevApps.map(app => app.id === id ? { ...app, status: nextStatus } : app)
        );
      }
    } catch (error) {
      console.error('Network failure executing patch:', error);
    }
  };

  const handleDelete = async (id) => {
    const token = await getSessionToken();
    try {
      const response = await fetch(`/v1/applications/${id}`, {
        method: 'DELETE',
        headers: { 'Authorization': `Bearer ${token}` }
      });

      if (response.ok) {
        setApplications(prevApps => prevApps.filter(app => app.id !== id));
      }
    } catch (error) {
      console.error('Failed processing delete operational flow:', error);
    }
  };

  useEffect(() => {
    const loadInitialData = async () => {
      const token = await getSessionToken();
      if (!token) return; // Keep loading visible or wait till overlay unlocks

      try {
        const res = await fetch('/v1/applications', {
          headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error('Network execution error');
        const data = await res.json();
        setApplications(data);
        setFilteredApplications(data);
      } catch (err) {
        console.error('Data pull metrics crash:', err);
      } finally {
        setLoading(false);
      }
    };

    loadInitialData();
  }, []);

  useEffect(() => {
    if (activeFilter === 'all') {
      setFilteredApplications(applications);
    } else {
      setFilteredApplications(
        applications.filter(app => app.status && app.status.toLowerCase() === activeFilter)
      );
    }
  }, [activeFilter, applications]);

  const totalApplied = applications.length;
  const activeCount = applications.filter(app => app.status && ['applied', 'interview'].includes(app.status.toLowerCase())).length;
  const inProgressCount = applications.filter(app => app.status && app.status.toLowerCase() === 'interview').length;
  const rejectedCount = applications.filter(app => app.status && app.status.toLowerCase() === 'rejected').length;

  return (
    <div style={styles.appContainer}>
      <header style={styles.header}>
        <div style={styles.logo}>Role<span style={styles.logoAccent}>Trackr</span></div>
        <div style={styles.tagline}>your one-stop-shop for application tracking</div>
      </header>

      <main style={styles.mainContent}>
        <div style={styles.leftColumn}>
          <div style={styles.metricsGrid}>
            <MetricCard count={totalApplied} label="Applied" />
            <MetricCard count={activeCount} label="Active" />
            <MetricCard count={inProgressCount} label="In Progress" isActiveTrack={true} />
            <MetricCard count={rejectedCount} label="Rejected" />
          </div>

          <div style={styles.tableContainer}>
            <div style={styles.tableHeaderActions}>
              <div style={styles.tableTitle}>Applications</div>
              <div style={styles.filterGroup}>
                {['all', 'applied', 'interview', 'rejected'].map(filter => (
                  <button key={filter} onClick={() => setActiveFilter(filter)} style={{ ...styles.filterBtn, ...(activeFilter === filter ? styles.filterBtnActive : {}) }}>
                    {filter}
                  </button>
                ))}
              </div>
            </div>

            {loading ? (
              <div style={styles.stateMessage}>Loading application pipeline data...</div>
            ) : filteredApplications.length === 0 ? (
              <div style={styles.stateMessage}>No job applications found matching this status.</div>
            ) : (
              <table style={styles.table}>
                <thead>
                  <tr>
                    <th style={{ ...styles.th, width: '30%' }}>Company</th>
                    <th style={{ ...styles.th, width: '40%' }}>Role</th>
                    <th style={{ ...styles.th, width: '15%' }}>Status</th>
                    <th style={{ ...styles.th, width: '15%' }}>Applied</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredApplications.map((app, idx) => (
                    <ApplicationRow key={app.id || idx} app={app} onDelete={handleDelete} onStatusChange={handleStatusChange}/>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
        <ChatSidebar />
      </main>
    </div>
  );
}