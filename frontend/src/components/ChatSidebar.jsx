import React, { useState, useRef, useEffect } from 'react';
import styles from '../style/styles.module.css';
import { getSessionToken } from '../supabaseClient';



// const backendUrl = process.env.REACT_APP_BACKEND_URL || 'http://localhost:8080';
const backendUrl = 'http://localhost:8080';


export default function ChatSidebar() {
  const [messages, setMessages] = useState([
    { role: 'assistant', text: 'hey — ask me anything about your search.' }
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef(null);
  const messagesRef = useRef(null);

  useEffect(() => {
    const el = messagesRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages]);

  const sendMessage = async () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages(prev => [...prev, { role: 'user', text }]);
    setInput('');
    setLoading(true);
    const token = await getSessionToken();

    try {
      const res = await fetch(`${backendUrl}/v1/talk`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Basic ${token}`
        },
        body: JSON.stringify({ userId: "USER_ID", sessionId: "SESSION_ID", message: text }),
      });

      if (!res.ok) throw new Error(`Server error: ${res.status}`);
      const reply = await res.text();
      setMessages(prev => [...prev, { role: 'assistant', text: reply }]);
    } catch (err) {
      setMessages(prev => [...prev, { role: 'assistant', text: `Error: ${err.message}` }]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();
    }
  };

  return (
    <aside className={styles.sidebar}>
      <div className={styles.sidebarHeader}>
        <span>Chat</span>
        <div className={styles.sidebarDot}></div>
      </div>

      <div className={styles.chatMessages} ref={messagesRef}>
        {messages.map((msg, i) => (
          <div
            key={i}
            className={`${styles.chatBubble} ${msg.role === 'user' ? styles.chatBubbleUser : styles.chatBubbleAssistant}`}
          >
            {msg.text}
          </div>
        ))}
        {loading && (
          <div className={`${styles.chatBubble} ${styles.chatBubbleAssistant} ${styles.chatBubbleLoading}`}>
            <span className={styles.typingDot} />
            <span className={styles.typingDot} />
            <span className={styles.typingDot} />
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      <div className={styles.chatInputContainer}>
        <input
          type="text"
          placeholder="ask something..."
          className={styles.chatInput}
          value={input}
          onChange={(e) => setInput(e.target.value)}
          onKeyDown={handleKeyDown}
          disabled={loading}
        />
        <button
          className={styles.chatSubmitBtn}
          onClick={sendMessage}
          disabled={loading}
        >
          send
        </button>
      </div>
    </aside>
  );
}