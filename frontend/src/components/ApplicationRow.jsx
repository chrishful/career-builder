import React from 'react';
import styles from '../style/styles.module.css';

export default function ApplicationRow({ app, onDelete, onStatusChange }) {
  const statusKey = app.status ? app.status.toLowerCase() : 'applied';

  const statusBadgeClass = {
    interview: `${styles.statusBadge} ${styles.statusInterview}`,
    applied:   `${styles.statusBadge} ${styles.statusApplied}`,
    rejected:  `${styles.statusBadge} ${styles.statusRejected}`,
  }[statusKey] ?? styles.statusBadge;

  const handleSelectChange = (e) => {
    onStatusChange(app.id, e.target.value);
  };

  return (
    <tr className={styles.tableRow}>
      <td className={`${styles.td} ${styles.companyName}`}>{app.company || 'Unknown'}</td>
      <td className={`${styles.td} ${styles.roleName}`}>{app.role || 'Not Specified'}</td>

      <td className={styles.td}>
        <select
          value={app.status || 'Applied'}
          onChange={handleSelectChange}
          className={statusBadgeClass}
        >
          <option value="Applied"    className={styles.dropdownOption}>Applied</option>
          <option value="Interview"  className={styles.dropdownOption}>Interview</option>
          <option value="Rejected"   className={styles.dropdownOption}>Rejected</option>
        </select>
      </td>

      <td className={`${styles.td} ${styles.dateText}`}>{app.dateApplied || 'N/A'}</td>

      <td className={`${styles.td} ${styles.notesCell}`}>
        <span>{app.notes || '—'}</span>
        <button
          onClick={() => onDelete(app.id)}
          className={styles.deleteButton}
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