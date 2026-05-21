/* ═══════════════════════════════════════════════════════════════
   SafetyHub — Live Notification System  v2
   - WARNING  → full-screen modal, blur backdrop, alarm GIF animation,
                panic alarm sound, red/white flashing bold text
   - TRAINING → small toast (bottom-right, non-intrusive)
   - Manager pages → skipped entirely
═══════════════════════════════════════════════════════════════ */
(function () {
  'use strict';

  /* ── Role check — skip everything for Manager ─────────────── */
  function getUserRole() {
    const meta = document.getElementById('sh-role');
    return meta ? (meta.getAttribute('content') || '').trim() : '';
  }
  function isManagerPage() {
    return getUserRole().toLowerCase() === 'manager';
  }

  /* ══════════════════════════════════════════════════════════════
     PANIC ALARM SOUND  (Web Audio API — no external file needed)
     Simulates a real industrial panic alarm:
     rapid alternating high/low tones with harsh buzz
  ══════════════════════════════════════════════════════════════ */
  let alarmInterval = null;
  let alarmCtx      = null;

  function startPanicAlarm() {
    stopPanicAlarm();
    try {
      alarmCtx = new (window.AudioContext || window.webkitAudioContext)();
      let tick = 0;

      function beep() {
        if (!alarmCtx) return;
        const osc  = alarmCtx.createOscillator();
        const gain = alarmCtx.createGain();
        const dist = alarmCtx.createWaveShaper();

        // Distortion curve for harsh buzz
        const curve = new Float32Array(256);
        for (let i = 0; i < 256; i++) {
          const x = (i * 2) / 256 - 1;
          curve[i] = (Math.PI + 300) * x / (Math.PI + 300 * Math.abs(x));
        }
        dist.curve = curve;
        dist.oversample = '4x';

        osc.connect(dist);
        dist.connect(gain);
        gain.connect(alarmCtx.destination);

        // Alternate between two frequencies for panic effect
        osc.type = 'sawtooth';
        osc.frequency.value = tick % 2 === 0 ? 960 : 750;

        gain.gain.setValueAtTime(0, alarmCtx.currentTime);
        gain.gain.linearRampToValueAtTime(0.4, alarmCtx.currentTime + 0.02);
        gain.gain.setValueAtTime(0.4, alarmCtx.currentTime + 0.15);
        gain.gain.linearRampToValueAtTime(0, alarmCtx.currentTime + 0.22);

        osc.start(alarmCtx.currentTime);
        osc.stop(alarmCtx.currentTime + 0.25);
        tick++;
      }

      beep();
      alarmInterval = setInterval(beep, 280);

      // Auto-stop after 8 seconds
      setTimeout(stopPanicAlarm, 8000);
    } catch (e) { /* silent fallback */ }
  }

  function stopPanicAlarm() {
    clearInterval(alarmInterval);
    alarmInterval = null;
    if (alarmCtx) {
      try { alarmCtx.close(); } catch(e){}
      alarmCtx = null;
    }
  }

  /* ══════════════════════════════════════════════════════════════
     WARNING MODAL  — full screen, blur, alarm animation
  ══════════════════════════════════════════════════════════════ */
  function injectWarningStyles() {
    if (document.getElementById('sh-warning-styles')) return;
    const s = document.createElement('style');
    s.id = 'sh-warning-styles';
    s.textContent = `
      /* ── Backdrop ── */
      #sh-warning-backdrop {
        position: fixed; inset: 0; z-index: 999998;
        background: rgba(0, 0, 0, 0.75);
        backdrop-filter: blur(8px);
        -webkit-backdrop-filter: blur(8px);
        display: flex; align-items: center; justify-content: center;
        animation: shBackIn 0.3s ease both;
      }
      @keyframes shBackIn {
        from { opacity: 0; backdrop-filter: blur(0px); }
        to   { opacity: 1; backdrop-filter: blur(8px); }
      }

      /* ── Screen flash overlay ── */
      #sh-warning-backdrop::before {
        content: '';
        position: fixed; inset: 0; z-index: -1;
        background: rgba(220, 0, 0, 0.18);
        animation: shScreenFlash 0.6s ease-in-out infinite alternate;
        pointer-events: none;
      }
      @keyframes shScreenFlash {
        from { opacity: 0; }
        to   { opacity: 1; }
      }

      /* ── Modal box ── */
      #sh-warning-modal {
        position: relative;
        background: #0d0000;
        border: 2px solid #dc2626;
        border-radius: 24px;
        padding: 48px 52px;
        max-width: 560px; width: 90%;
        text-align: center;
        box-shadow:
          0 0 0 1px rgba(220,38,38,0.3),
          0 0 80px rgba(220,38,38,0.5),
          0 32px 80px rgba(0,0,0,0.8);
        animation: shModalIn 0.4s cubic-bezier(.34,1.56,.64,1) both;
        overflow: hidden;
      }
      @keyframes shModalIn {
        from { opacity:0; transform: scale(0.7) translateY(40px); }
        to   { opacity:1; transform: scale(1) translateY(0); }
      }

      /* Pulsing red border glow */
      #sh-warning-modal::before {
        content: '';
        position: absolute; inset: -2px;
        border-radius: 24px;
        background: transparent;
        box-shadow: 0 0 40px rgba(220,38,38,0.6);
        animation: shBorderPulse 0.8s ease-in-out infinite alternate;
        pointer-events: none;
      }
      @keyframes shBorderPulse {
        from { box-shadow: 0 0 20px rgba(220,38,38,0.4); }
        to   { box-shadow: 0 0 70px rgba(220,38,38,0.9); }
      }

      /* ── GIF-style alarm bell animation (pure CSS) ── */
      .sh-alarm-icon {
        font-size: 80px;
        display: block;
        margin: 0 auto 20px;
        animation: shAlarmShake 0.25s ease-in-out infinite;
        filter: drop-shadow(0 0 20px rgba(220,38,38,0.9));
        transform-origin: top center;
        position: relative; z-index: 1;
      }
      @keyframes shAlarmShake {
        0%   { transform: rotate(-22deg) scale(1.05); }
        25%  { transform: rotate(0deg)   scale(1.15); }
        50%  { transform: rotate(22deg)  scale(1.05); }
        75%  { transform: rotate(0deg)   scale(1.15); }
        100% { transform: rotate(-22deg) scale(1.05); }
      }

      /* ── Rings radiating from bell ── */
      .sh-alarm-rings {
        position: absolute; top: 28px; left: 50%;
        transform: translateX(-50%);
        width: 120px; height: 120px;
        pointer-events: none; z-index: 0;
      }
      .sh-ring {
        position: absolute; inset: 0; border-radius: 50%;
        border: 3px solid rgba(220,38,38,0.6);
        animation: shRingExpand 1.2s ease-out infinite;
      }
      .sh-ring:nth-child(2) { animation-delay: 0.4s; }
      .sh-ring:nth-child(3) { animation-delay: 0.8s; }
      @keyframes shRingExpand {
        0%   { transform: scale(0.8); opacity: 0.9; }
        100% { transform: scale(2.8); opacity: 0; }
      }

      /* ── WARNING label ── */
      .sh-warning-label {
        font-size: 11px; font-weight: 800; letter-spacing: 4px;
        text-transform: uppercase;
        color: #ef4444;
        margin-bottom: 10px;
        position: relative; z-index: 1;
      }

      /* ── Event title ── */
      .sh-warning-title {
        font-size: 22px; font-weight: 800;
        color: #fff;
        margin-bottom: 20px; line-height: 1.3;
        position: relative; z-index: 1;
      }

      /* ── Description — the Manager's message ── */
      .sh-warning-desc {
        font-size: 20px; font-weight: 700;
        line-height: 1.5;
        position: relative; z-index: 1;
        padding: 18px 20px;
        background: rgba(220,38,38,0.08);
        border: 1px solid rgba(220,38,38,0.25);
        border-radius: 14px;
        margin-bottom: 24px;

        /* Red / white flashing animation */
        animation: shTextFlash 0.7s ease-in-out infinite;
      }
      @keyframes shTextFlash {
        0%,100% { color: #ff4444; text-shadow: 0 0 20px rgba(255,68,68,0.8); }
        50%      { color: #ffffff; text-shadow: 0 0 20px rgba(255,255,255,0.6); }
      }

      /* ── Dept / date info ── */
      .sh-warning-meta {
        font-size: 12px; color: rgba(255,255,255,0.4);
        margin-bottom: 28px; position: relative; z-index: 1;
      }

      /* ── Severity dots ── */
      .sh-sev-row {
        display: flex; align-items: center; justify-content: center;
        gap: 6px; margin-bottom: 28px; position: relative; z-index: 1;
      }
      .sh-sev-label { font-size: 11px; color: rgba(255,255,255,0.4); margin-right: 6px; }
      .sh-sev-dot {
        width: 10px; height: 10px; border-radius: 50%;
        background: rgba(220,38,38,0.25);
        transition: background 0.2s;
      }
      .sh-sev-dot.on {
        background: #ef4444;
        animation: shDotPulse 0.8s ease-in-out infinite;
      }
      @keyframes shDotPulse {
        0%,100% { box-shadow: 0 0 0 0 rgba(239,68,68,0.6); }
        50%      { box-shadow: 0 0 0 6px rgba(239,68,68,0); }
      }

      /* ── Dismiss button ── */
      .sh-dismiss-btn {
        display: inline-flex; align-items: center; gap: 8px;
        background: rgba(220,38,38,0.15);
        border: 1.5px solid rgba(220,38,38,0.4);
        color: #fca5a5;
        font-size: 13px; font-weight: 600;
        padding: 12px 28px; border-radius: 12px;
        cursor: pointer; position: relative; z-index: 1;
        transition: background 0.2s, transform 0.15s;
      }
      .sh-dismiss-btn:hover {
        background: rgba(220,38,38,0.3);
        transform: scale(1.04);
      }

      /* ══════════════════════════════════════════════════
         TRAINING TOAST  (small, bottom-right)
      ══════════════════════════════════════════════════ */
      #sh-toast-container {
        position: fixed; bottom: 24px; right: 24px;
        z-index: 99997;
        display: flex; flex-direction: column; gap: 10px;
        pointer-events: none; max-width: 360px; width: 100%;
      }
      .sh-toast {
        pointer-events: all;
        background: linear-gradient(135deg, #00091a, #001226);
        border: 1.5px solid rgba(59,130,246,0.35);
        border-radius: 14px; overflow: hidden;
        box-shadow: 0 16px 48px rgba(0,0,0,0.4);
        animation: shToastIn 0.4s cubic-bezier(.34,1.4,.64,1) both;
        cursor: pointer;
      }
      .sh-toast.removing {
        animation: shToastOut 0.3s ease both;
      }
      @keyframes shToastIn {
        from { opacity:0; transform: translateX(100%) scale(0.9); }
        to   { opacity:1; transform: translateX(0) scale(1); }
      }
      @keyframes shToastOut {
        from { opacity:1; transform: translateX(0) scale(1); }
        to   { opacity:0; transform: translateX(110%); }
      }
      .sh-toast-header {
        display: flex; align-items: center; gap: 10px;
        padding: 14px 14px 10px;
        background: rgba(59,130,246,0.12);
        border-bottom: 1px solid rgba(59,130,246,0.1);
      }
      .sh-toast-icon {
        width: 34px; height: 34px; border-radius: 8px;
        background: rgba(59,130,246,0.15); border: 1px solid rgba(59,130,246,0.3);
        display: flex; align-items: center; justify-content: center; font-size: 16px;
      }
      .sh-toast-type { font-size: 9px; font-weight: 800; letter-spacing: 2px; text-transform: uppercase; color: #60a5fa; }
      .sh-toast-title { font-size: 13px; font-weight: 700; color: #bfdbfe; line-height: 1.3; }
      .sh-toast-body { padding: 10px 14px 14px; font-size: 12px; color: rgba(191,219,254,0.7); line-height: 1.6; }
      .sh-toast-prog { height: 2px; background: rgba(255,255,255,0.06); }
      .sh-toast-prog-fill {
        height: 100%; background: linear-gradient(90deg, #3b82f6, #10b981);
        animation: shProgDrain var(--d,7s) linear forwards;
      }
      @keyframes shProgDrain { to { transform: scaleX(0); transform-origin: left; } }
      .sh-toast:hover .sh-toast-prog-fill { animation-play-state: paused; }
    `;
    document.head.appendChild(s);
  }

  /* ── Show WARNING modal ──────────────────────────────────── */
  function showWarningModal(event) {
    // Remove any existing
    const old = document.getElementById('sh-warning-backdrop');
    if (old) old.remove();

    startPanicAlarm();

    const desc  = event.warningMessage || event.description || '';
    const title = event.title || 'Safety Warning';
    const dept  = event.targetDepartment ? `Department: ${event.targetDepartment}` : '';
    const sev   = event.severity || 0;

    // Severity dots
    const dots = Array.from({length: 4}, (_, i) =>
      `<div class="sh-sev-dot ${i < sev ? 'on' : ''}"></div>`
    ).join('');
    const sevLabel = ['', 'LOW', 'MEDIUM', 'HIGH', 'CRITICAL'][sev] || '';

    const backdrop = document.createElement('div');
    backdrop.id = 'sh-warning-backdrop';
    backdrop.innerHTML = `
      <div id="sh-warning-modal">
        <div class="sh-alarm-rings">
          <div class="sh-ring"></div>
          <div class="sh-ring"></div>
          <div class="sh-ring"></div>
        </div>
        <span class="sh-alarm-icon">🔔</span>

        <div class="sh-warning-label">⚠ SAFETY WARNING ⚠</div>
        <div class="sh-warning-title">${esc(title)}</div>
        <div class="sh-warning-desc">${esc(desc)}</div>
        <div class="sh-warning-meta">${esc(dept)}</div>

        ${sev > 0 ? `
        <div class="sh-sev-row">
          <span class="sh-sev-label">SEVERITY</span>
          ${dots}
          <span class="sh-sev-label" style="margin-left:6px;color:#ef4444;font-weight:700">${sevLabel}</span>
        </div>` : ''}

        <button class="sh-dismiss-btn" onclick="document.getElementById('sh-warning-backdrop').remove(); window.SafetyNotifications._stopAlarm();">
          ✓ I Understand — Dismiss
        </button>
      </div>
    `;

    document.body.appendChild(backdrop);

    // Click outside to dismiss
    backdrop.addEventListener('click', (e) => {
      if (e.target === backdrop) {
        backdrop.remove();
        stopPanicAlarm();
      }
    });
  }

  /* ── Show TRAINING toast ─────────────────────────────────── */
  function showTrainingToast(event) {
    injectWarningStyles();

    let container = document.getElementById('sh-toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'sh-toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = 'sh-toast';
    toast.innerHTML = `
      <div class="sh-toast-header">
        <div class="sh-toast-icon">📚</div>
        <div>
          <div class="sh-toast-type">Training Event</div>
          <div class="sh-toast-title">${esc(event.title || 'New Training')}</div>
        </div>
      </div>
      <div class="sh-toast-body">${esc(event.description || '')}</div>
      <div class="sh-toast-prog"><div class="sh-toast-prog-fill" style="--d:7s"></div></div>
    `;

    toast.addEventListener('click', () => dismissToast(toast));
    container.insertBefore(toast, container.firstChild);

    const timer = setTimeout(() => dismissToast(toast), 7000);
    toast._timer = timer;

    // Max 3 toasts
    const all = container.querySelectorAll('.sh-toast');
    if (all.length > 3) dismissToast(all[all.length - 1]);
  }

  function dismissToast(toast) {
    if (!toast || toast.classList.contains('removing')) return;
    clearTimeout(toast._timer);
    toast.classList.add('removing');
    toast.addEventListener('animationend', () => toast.remove(), { once: true });
  }

  /* ── Main show function ──────────────────────────────────── */
  function showNotification(event) {
    if (isManagerPage()) return;  // ← Manager sees nothing
    injectWarningStyles();

    const type = (event.eventType || '').toUpperCase();
    if (type === 'WARNING') {
      showWarningModal(event);
    } else {
      showTrainingToast(event);
    }
  }

  /* ── Polling ─────────────────────────────────────────────── */
  let lastSeenId = null;

  function pollEvents() {
    if (isManagerPage()) return;
    fetch('/api/events/latest', { credentials: 'same-origin' })
      .then(r => r.ok ? r.json() : null)
      .then(events => {
        if (!Array.isArray(events) || !events.length) return;
        const newEvents = lastSeenId
          ? events.filter(e => e.id > lastSeenId)
          : [events[0]];
        lastSeenId = events[0].id;
        [...newEvents].reverse().forEach(e => showNotification(e));
      })
      .catch(() => {});
  }

  /* ── Helpers ─────────────────────────────────────────────── */
  function esc(str) {
    return String(str)
      .replace(/&/g,'&amp;').replace(/</g,'&lt;')
      .replace(/>/g,'&gt;').replace(/"/g,'&quot;');
  }

  /* ── Public API ──────────────────────────────────────────── */
  window.SafetyNotifications = {
    show:       showNotification,
    poll:       pollEvents,
    _stopAlarm: stopPanicAlarm,
    demo: {
      warning: () => showNotification({
        id: Date.now(), eventType: 'WARNING',
        title: 'Chemical Spill Detected — Zone B',
        warningMessage: 'Evacuate Zone B immediately. Do not use elevator. Follow emergency exits marked in red.',
        targetDepartment: 'Manufacturing', severity: 4
      }),
      training: () => showNotification({
        id: Date.now(), eventType: 'TRAINING',
        title: 'New Fire Safety Module Available',
        description: 'A new training module on fire extinguisher operation has been assigned to your team.',
      })
    }
  };

  /* ── Auto-start ──────────────────────────────────────────── */
  document.addEventListener('DOMContentLoaded', () => {
    if (document.querySelector('.shell') || document.querySelector('.main')) {
      if (!isManagerPage()) {
        pollEvents();
        setInterval(pollEvents, 30000);
      }
    }
  });

})();
