import React from 'react';
import { styles } from '../style/styles';

export default function MetricCard({ count, label, isActiveTrack }) {
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