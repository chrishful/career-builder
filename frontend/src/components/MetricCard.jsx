import React from 'react';
import styles from '../style/styles.module.css';

export default function MetricCard({ count, label, isActiveTrack }) {
  return (
    <div className={`${styles.metricCard} ${isActiveTrack ? styles.metricCardActive : ''}`}>
      <div className={styles.metricNumber}>{count}</div>
      <div className={styles.metricLabel}>{label}</div>
    </div>
  );
}
