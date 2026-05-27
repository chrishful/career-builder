import React, { useState, useEffect } from 'react';
import ApplicationRow from './components/ApplicationRow';
import { styles } from './style/styles';

// ==========================================
// MAIN DASHBOARD COMPONENT WITH LIVE HOOKS
// ==========================================
export default function App() {
  const [applications, setApplications] = useState([]);
  const [filteredApplications, setFilteredApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFilter, setActiveFilter] = useState('all');

  const handleStatusChange = async (id, nextStatus) => {
    try {
      const response = await fetch(`http://localhost:8080/v1/applications/${id}`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ status: nextStatus }),
      });

      if (response.ok) {
        // Instantly modify the state matrix array locally to update the badge color map
        setApplications(prevApps =>
          prevApps.map(app => app.id === id ? { ...app, status: nextStatus } : app)
        );
      } else {
        console.error('Server side patch operation failed. Status code:', response.status);
      }
    } catch (error) {
      console.error('Network failure executing patch configuration update:', error);
    }
  };

    const handleDelete = async (id) => {
      try {
        const response = await fetch(`http://localhost:8080/v1/applications/${id}`, {
          method: 'DELETE',
        });

        if (response.ok) {
          // Use the string ID to instantly pop the item out of the UI array
          setApplications(prevApps => prevApps.filter(app => app.id !== id));
        }
      } catch (error) {
        console.error('Failed processing delete execution:', error);
      }
    };

  // Fetch applications from Spring Boot backend on mount
  useEffect(() => {
    fetch('/v1/applications')
      .then((res) => {
        if (!res.ok) throw new Error('Network response failed');
        return res.json();
      })
      .then((data) => {
        setApplications(data);
        setFilteredApplications(data);
        setLoading(false);
      })
      .catch((err) => {
        console.error('Error fetching data from backend:', err);
        setLoading(false);
      });
  }, []);

  // Handle client-side filtering changes
  useEffect(() => {
    if (activeFilter === 'all') {
      setFilteredApplications(applications);
    } else {
      setFilteredApplications(
        applications.filter(
          (app) => app.status && app.status.toLowerCase() === activeFilter
        )
      );
    }
  }, [activeFilter, applications]);

  // Compute live card counts based on actual backend data status
  const totalApplied = applications.length;
  const activeCount = applications.filter(
    (app) => app.status && ['applied', 'interview'].includes(app.status.toLowerCase())
  ).length;
  const inProgressCount = applications.filter(
    (app) => app.status && app.status.toLowerCase() === 'interview'
  ).length;
  const rejectedCount = applications.filter(
    (app) => app.status && app.status.toLowerCase() === 'rejected'
  ).length;

  return (
    <div style={styles.appContainer}>
      <header style={styles.header}>
        <div style={styles.logo}>
          Role<span style={styles.logoAccent}>Trackr</span>
        </div>
        <div style={styles.tagline}>
          your one-stop-shop for application tracking
        </div>
      </header>

      <main style={styles.mainContent}>
        <div style={styles.leftColumn}>

          {/* Dynamically Computed Metric Grid Cards */}
          <div style={styles.metricsGrid}>
            <MetricCard count={totalApplied} label="Applied" />
            <MetricCard count={activeCount} label="Active" />
            <MetricCard count={inProgressCount} label="In Progress" isActiveTrack={true} />
            <MetricCard count={rejectedCount} label="Rejected" />
          </div>

          {/* Applications Data Table */}
          <div style={styles.tableContainer}>
            <div style={styles.tableHeaderActions}>
              <div style={styles.tableTitle}>Applications</div>
              <div style={styles.filterGroup}>
                {['all', 'applied', 'interview', 'rejected'].map((filter) => (
                  <button
                    key={filter}
                    onClick={() => setActiveFilter(filter)}
                    style={{
                      ...styles.filterBtn,
                      ...(activeFilter === filter ? styles.filterBtnActive : {}),
                    }}
                  >
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
                    <ApplicationRow key={app.number || idx} app={app} onDelete={handleDelete} onStatusChange={handleStatusChange}/>
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

// ==========================================
// ABSTRACTED ATOMIC SUB-COMPONENTS
// ==========================================

function MetricCard({ count, label, isActiveTrack }) {
  const cardStyle = isActiveTrack
    ? { ...styles.metricCard, ...styles.metricCardActive }
    : styles.metricCard;

  return (
    <div style={cardStyle}>
      <div style={styles.metricNumber}>{count}</div>
      <div style={styles.metricLabel}>{label}</div>
    </div>
  );
}

function ChatSidebar() {
  return (
    <aside style={styles.sidebar}>
      <div style={styles.sidebarHeader}>
        <span>Chat</span>
        <div style={styles.sidebarDot}></div>
      </div>

      <div style={styles.chatBubble}>
        hey — ask me anything about your search.
      </div>

      <div style={styles.chatInputContainer}>
        <input
          type="text"
          placeholder="ask something..."
          style={styles.chatInput}
          disabled
        />
        <button style={styles.chatSubmitBtn}>send</button>
      </div>
    </aside>
  );
}