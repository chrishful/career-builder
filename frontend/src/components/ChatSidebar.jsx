import React from 'react';
import { styles } from '../style/styles';

export default function ChatSidebar() {
  return (
    <aside style={styles.sidebar}>
      <div style={styles.sidebarHeader}>
        <span>Chat</span>
        <div style={styles.sidebarDot}></div>
      </div>
      <div style={styles.chatBubble}>hey — ask me anything about your search.</div>
      <div style={styles.chatInputContainer}>
        <input type="text" placeholder="ask something..." style={styles.chatInput} disabled />
        <button style={styles.chatSubmitBtn}>send</button>
      </div>
    </aside>
  );
}