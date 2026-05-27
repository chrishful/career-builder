import React, { useState } from 'react';
import { styles } from '../style/styles';

export default function ApplicationRow({ app, onDelete, onStatusChange }) {
  const [isHovered, setIsHovered] = useState(false);

  // Keep this lowercase string exclusively to fetch your color style blocks
  const styleKey = app.status ? app.status.toLowerCase() : 'applied';

  const getStatusBadgeStyle = (statusStr) => {
    switch (statusStr) {
      case 'interview': return { ...styles.statusBadge, ...styles.statusInterview };
      case 'applied': return { ...styles.statusBadge, ...styles.statusApplied };
      case 'rejected': return { ...styles.statusBadge, ...styles.statusRejected };
      default: return styles.statusBadge;
    }
  };

  const handleSelectChange = (e) => {
    const nextStatus = e.target.value; // Pass capitalized "Applied", "Interview", etc.
    onStatusChange(app.id, nextStatus);
  };

  return (
    <tr
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      style={isHovered ? { backgroundColor: '#1e293b50', transition: 'background-color 0.15s' } : {}}
    >
      <td style={{ ...styles.td, ...styles.companyName }}>{app.company || 'Unknown'}</td>
      <td style={{ ...styles.td, ...styles.roleName }}>{app.role || 'Not Specified'}</td>

      {/* Interactive Status Badge Column */}
      <td style={styles.td}>
        <div style={{ position: 'relative', display: 'inline-block' }}>
          <select
            value={app.status || 'Applied'} // ⚡ FIX: Binds to raw backend casing so it matches the options below
            onChange={handleSelectChange}
            style={{
              ...getStatusBadgeStyle(styleKey), // ⚡ FIX: Uses lowercase key to correctly match your style blocks
              appearance: 'none',
              WebkitAppearance: 'none',
              border: 'none',
              cursor: 'pointer',
              paddingRight: '20px'
            }}
          >
            <option value="Applied" style={styles.dropdownOption}>Applied</option>
            <option value="Interview" style={styles.dropdownOption}>Interview</option>
            <option value="Rejected" style={styles.dropdownOption}>Rejected</option>
          </select>
        </div>
      </td>

      <td style={{ ...styles.td, ...styles.dateText }}>{app.dateApplied || 'N/A'}</td>

      <td style={{ ...styles.td, ...styles.notesCell, position: 'relative', paddingRight: '48px' }}>
        <span>{app.notes || '—'}</span>

        <button
          onClick={() => onDelete(app.id)}
          style={{
            ...styles.deleteButton,
            opacity: isHovered ? 1 : 0,
            pointerEvents: isHovered ? 'auto' : 'none'
          }}
          title="Delete Application"
        >
          <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="3 6 5 6 21 6"></polyline>
            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v3"></path>
            <line x1="10" y1="11" x2="10" y2="17"></line>
            <line x1="14" y1="11" x2="14" y2="17"></line>
          </svg>
        </button>
      </td>
    </tr>
  );
}