/**
 * SafeGuard — app.js
 * Theme / Lang / Canvas / Modal / Toast
 */

// ─── Safety Background Canvas ─────────────────────────────────────────────────
(function initCanvas() {
  const canvas = document.getElementById('safety-canvas');
  if (!canvas) return;
  const ctx = canvas.getContext('2d');
  function resize() { canvas.width = window.innerWidth; canvas.height = window.innerHeight; }
  resize();
  window.addEventListener('resize', resize);
  const SYMBOLS = ['⚠','🦺','🔥','⛑','🧯','☣','⚡','🚧','🛡','🔒'];
  const isDark  = () => document.documentElement.getAttribute('data-theme') === 'dark';
  const particles = Array.from({ length: 22 }, (_, i) => ({
    x: Math.random() * window.innerWidth, y: Math.random() * window.innerHeight,
    size: 16 + Math.random() * 18, symbol: SYMBOLS[i % SYMBOLS.length],
    vy: -0.2 - Math.random() * 0.25, vx: (Math.random() - 0.5) * 0.13,
    opacity: 0.04 + Math.random() * 0.06, rotation: Math.random() * Math.PI * 2,
    rotSpeed: (Math.random() - 0.5) * 0.004,
  }));
  function draw() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    const dark = isDark();
    particles.forEach(p => {
      ctx.save(); ctx.globalAlpha = p.opacity;
      ctx.translate(p.x, p.y); ctx.rotate(p.rotation);
      ctx.font = `${p.size}px serif`;
      ctx.fillStyle = dark ? '#ffffff' : '#1e293b';
      ctx.fillText(p.symbol, 0, 0); ctx.restore();
      p.y += p.vy; p.x += p.vx; p.rotation += p.rotSpeed;
      if (p.y < -50) { p.y = canvas.height + 50; p.x = Math.random() * canvas.width; }
    });
    requestAnimationFrame(draw);
  }
  draw();
})();

// ─── Toast ────────────────────────────────────────────────────────────────────
function showToast(message) {
  const container = document.getElementById('toast-wrap');
  if (!container) return;
  const el = document.createElement('div');
  el.className = 'toast';
  el.innerHTML = `<span>✓</span> ${message}`;
  container.appendChild(el);
  setTimeout(() => {
    el.style.opacity = '0'; el.style.transform = 'translateY(8px)';
    el.style.transition = '0.3s ease';
    setTimeout(() => el.remove(), 300);
  }, 3000);
}
window.addEventListener('DOMContentLoaded', () => {
  const srv = document.getElementById('srv-toast');
  if (srv && srv.dataset.msg) showToast(srv.dataset.msg);
});

// ─── Modal System ─────────────────────────────────────────────────────────────
const Modal = {
  open(id)  { const el = document.getElementById(id); if (el) { el.classList.remove('hidden'); document.body.style.overflow = 'hidden'; } },
  close(id) { const el = document.getElementById(id); if (el) { el.classList.add('hidden');    document.body.style.overflow = ''; } },
  closeAll(){ document.querySelectorAll('.modal-backdrop').forEach(m => m.classList.add('hidden')); document.body.style.overflow = ''; }
};
document.addEventListener('click', e => { if (e.target.classList.contains('modal-backdrop')) Modal.closeAll(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') Modal.closeAll(); });

// ─── Employee Detail Modal ────────────────────────────────────────────────────
function openDetailModal(employeeId) {
  const params = new URLSearchParams(window.location.search);
  const lang   = params.get('lang')  || 'en';
  const theme  = params.get('theme') || 'dark';
  const frame  = document.getElementById('detail-frame');
  if (frame) frame.src = `/employees/${employeeId}/detail?lang=${lang}&theme=${theme}`;
  Modal.open('detail-modal');
}

// ─── Other Modals ─────────────────────────────────────────────────────────────
function openDropModal(empId, courseId, courseName) {
  document.getElementById('drop-course-name').textContent = courseName;
  document.getElementById('drop-form').action = `/employees/${empId}/drop-course/${courseId}${buildQueryString()}`;
  Modal.open('drop-modal');
}
function openResetModal(empId, empName) {
  document.getElementById('reset-emp-name').textContent = empName;
  document.getElementById('reset-form').action = `/employees/${empId}/reset-progress${buildQueryString()}`;
  Modal.open('reset-modal');
}
function openDeadlineModal(empId, currentDeadline) {
  const input = document.getElementById('deadline-input');
  if (currentDeadline) input.value = currentDeadline;
  input.min = new Date().toISOString().split('T')[0];
  document.getElementById('deadline-form').action = `/employees/${empId}/update-deadline${buildQueryString()}`;
  Modal.open('deadline-modal');
}

// ─── Theme Toggle ─────────────────────────────────────────────────────────────
function toggleTheme() {
  const html    = document.documentElement;
  const current = html.getAttribute('data-theme') || 'dark';
  const next    = current === 'dark' ? 'light' : 'dark';
  html.setAttribute('data-theme', next);
  localStorage.setItem('sg-theme', next);
  updateThemeBtn(next);
  // reload page preserving all params
  const url = new URL(window.location.href);
  url.searchParams.set('theme', next);
  window.location.href = url.toString();
}
function updateThemeBtn(theme) {
  const btn = document.getElementById('theme-btn');
  if (btn) btn.textContent = theme === 'dark' ? '☀ Light' : '🌙 Dark';
}

// ─── Language Toggle ──────────────────────────────────────────────────────────
function toggleLang() {
  const url     = new URL(window.location.href);
  const current = url.searchParams.get('lang') || 'en';
  const next    = current === 'en' ? 'ar' : 'en';
  url.searchParams.set('lang', next);
  window.location.href = url.toString();
}

// ─── URL helpers ──────────────────────────────────────────────────────────────
function buildQueryString() {
  const p = new URLSearchParams(window.location.search);
  return `?lang=${p.get('lang')||'en'}&theme=${p.get('theme')||'dark'}`;
}

// ─── Restore theme on load ────────────────────────────────────────────────────
(function restoreTheme() {
  const url   = new URL(window.location.href);
  const param = url.searchParams.get('theme');
  const saved = localStorage.getItem('sg-theme');
  const theme = param || saved || 'dark';
  document.documentElement.setAttribute('data-theme', theme);
  updateThemeBtn(theme);
  // If URL doesn't have theme param, add it silently
  if (!param && saved) {
    url.searchParams.set('theme', saved);
    window.history.replaceState({}, '', url.toString());
  }
})();

// ─── Live search ──────────────────────────────────────────────────────────────
(function initLiveSearch() {
  const input = document.getElementById('search-input');
  const form  = document.getElementById('filter-form');
  if (!input || !form) return;
  let t;
  input.addEventListener('input', () => {
    clearTimeout(t);
    t = setTimeout(() => form.submit(), 350);
  });
})();
