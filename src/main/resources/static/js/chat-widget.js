/* ══════════════════════════════════════════════════
   AUTONOMOUS AI AGENT — MOVEMENT + HUD ENGINE
══════════════════════════════════════════════════ */
(function () {

    /* ── State ── */
    const ORB = {
        x: window.innerWidth  - 110,
        y: window.innerHeight - 110,
        vx: -0.6, vy: -0.4,
        targetX: 0, targetY: 0,
        size: 66, isOpen: false,
        behaviorTimer: null, lastParticleTime: 0,
    };
    const MARGIN = 30, CHAT_W = 360, CHAT_H = 500;
    let btnEl, containerEl, particleLayer;

    function init() {
        btnEl       = document.getElementById('ai-chat-button');
        containerEl = document.getElementById('ai-chat-container');
        if (!document.getElementById('chat-backdrop')) {
            const bd = document.createElement('div');
            bd.id = 'chat-backdrop';
            bd.addEventListener('click', () => { if (ORB.isOpen) onOrbClick(); });
            document.body.appendChild(bd);
        }
        if (!btnEl || !containerEl) return;
        if (containerEl.parentElement !== document.body) document.body.appendChild(containerEl);
        if (btnEl.parentElement !== document.body) document.body.appendChild(btnEl);

        particleLayer = document.createElement('div');
        particleLayer.id = 'orb-particles';
        document.body.appendChild(particleLayer);

        ORB.x = window.innerWidth  - 110;
        ORB.y = window.innerHeight - 110;
        pickNewTarget();
        applyPosition(true);
        positionChat();
        startClock();
        startHudCanvas();

        requestAnimationFrame(moveLoop);
        scheduleBehavior();

        btnEl.addEventListener('click',      onOrbClick);
        btnEl.addEventListener('mouseenter', onOrbHover);
        btnEl.addEventListener('mouseleave', onOrbLeave);
        window.addEventListener('resize',    clampPosition);
        window.addEventListener('mousemove', onMouseMove);
    }

    function moveLoop() {
        if (!ORB.isOpen) {
            const dx   = ORB.targetX - ORB.x;
            const dy   = ORB.targetY - ORB.y;
            const dist = Math.hypot(dx, dy);
            const steer = .018, dampen = .96, speed = 1.4;
            if (dist > 5) { ORB.vx += (dx / dist) * steer * speed; ORB.vy += (dy / dist) * steer * speed; }
            ORB.vx *= dampen; ORB.vy *= dampen;
            const spd = Math.hypot(ORB.vx, ORB.vy), maxSpd = 2.4;
            if (spd > maxSpd) { ORB.vx = (ORB.vx / spd) * maxSpd; ORB.vy = (ORB.vy / spd) * maxSpd; }
            ORB.x += ORB.vx; ORB.y += ORB.vy;
            const maxX = window.innerWidth  - ORB.size - MARGIN;
            const maxY = window.innerHeight - ORB.size - MARGIN;
            if (ORB.x < MARGIN) { ORB.x = MARGIN;  ORB.vx *= -.6; }
            if (ORB.x > maxX)   { ORB.x = maxX;    ORB.vx *= -.6; }
            if (ORB.y < MARGIN) { ORB.y = MARGIN;  ORB.vy *= -.6; }
            if (ORB.y > maxY)   { ORB.y = maxY;    ORB.vy *= -.6; }
            if (dist < 8) pickNewTarget();
            applyPosition();
            spawnParticles();
        }
        requestAnimationFrame(moveLoop);
    }

    function pickNewTarget() {
        const vw = window.innerWidth, vh = window.innerHeight;
        const zones = [
            { x: MARGIN+20,          y: MARGIN+20 },
            { x: vw-ORB.size-50,     y: MARGIN+20 },
            { x: MARGIN+20,          y: vh-ORB.size-50 },
            { x: vw-ORB.size-50,     y: vh-ORB.size-50 },
            { x: vw/2-ORB.size/2,    y: MARGIN+20 },
            { x: vw/2-ORB.size/2,    y: vh-ORB.size-50 },
            { x: MARGIN+20,          y: vh/2-ORB.size/2 },
            { x: vw-ORB.size-50,     y: vh/2-ORB.size/2 },
        ];
        const t = zones[Math.floor(Math.random() * zones.length)];
        ORB.targetX = Math.max(MARGIN, Math.min(vw-ORB.size-MARGIN, t.x + (Math.random()-.5)*60));
        ORB.targetY = Math.max(MARGIN, Math.min(vh-ORB.size-MARGIN, t.y + (Math.random()-.5)*60));
    }

    function applyPosition(instant) {
        btnEl.style.left = ORB.x + 'px';
        btnEl.style.top  = ORB.y + 'px';
        btnEl.style.transition = instant ? 'none' : 'left .05s linear, top .05s linear, filter .3s ease';
    }

    function positionChat() {
        const vw = window.innerWidth, vh = window.innerHeight, pad = 12;
        let left = ORB.x, top = ORB.y - CHAT_H - pad;
        if (left + CHAT_W > vw - pad)  left = vw - CHAT_W - pad;
        if (top < pad)                 top  = ORB.y + ORB.size + pad;
        left = Math.max(pad, left);
        top  = Math.max(pad, Math.min(vh - CHAT_H - pad, top));
        containerEl.style.left   = left + 'px';
        containerEl.style.top    = top  + 'px';
        containerEl.style.right  = 'auto';
        containerEl.style.bottom = 'auto';
    }

    function clampPosition() {
        ORB.x = Math.max(MARGIN, Math.min(window.innerWidth  - ORB.size - MARGIN, ORB.x));
        ORB.y = Math.max(MARGIN, Math.min(window.innerHeight - ORB.size - MARGIN, ORB.y));
        applyPosition(true);
    }

    function spawnParticles() {
        const now = performance.now();
        const spd = Math.hypot(ORB.vx, ORB.vy);
        if (spd < .3 || now - ORB.lastParticleTime < 80) return;
        ORB.lastParticleTime = now;
        for (let i = 0; i < Math.floor(spd * 1.5); i++) {
            const p  = document.createElement('div');
            const sz = Math.random() * 5 + 2;
            p.className = 'particle';
            p.style.cssText = `
                width:${sz}px;height:${sz}px;
                left:${ORB.x + ORB.size/2 + (Math.random()-.5)*20}px;
                top:${ORB.y  + ORB.size/2 + (Math.random()-.5)*20}px;
                background:hsl(${Math.random()>.5?270:250},80%,${60+Math.random()*20}%);
                opacity:${.6+Math.random()*.4};
                --dx:${(Math.random()-.5)*30}px;
                --dy:${(Math.random()-.5)*30}px;
                animation-duration:${.5+Math.random()*.5}s;
            `;
            particleLayer.appendChild(p);
            p.addEventListener('animationend', () => p.remove(), { once: true });
        }
    }

    function scheduleBehavior() {
        ORB.behaviorTimer = setTimeout(() => {
            if (!ORB.isOpen) {
                const b = ['look-left','look-right','look-up','excited'][Math.floor(Math.random()*4)];
                btnEl.classList.add(b);
                setTimeout(() => btnEl.classList.remove(b), 1200);
            }
            scheduleBehavior();
        }, 4000 + Math.random() * 8000);
    }

    function onMouseMove(e) {
        if (ORB.isOpen) return;
        const cx = ORB.x + ORB.size/2, cy = ORB.y + ORB.size/2;
        if (Math.hypot(e.clientX - cx, e.clientY - cy) < 120 && !btnEl.classList.contains('excited')) {
            btnEl.classList.add('excited');
            setTimeout(() => btnEl.classList.remove('excited'), 600);
        }
    }

    function onOrbClick() {
        ORB.isOpen = !ORB.isOpen;
        btnEl.classList.toggle('chat-open', ORB.isOpen);
        containerEl.classList.toggle('collapsed', !ORB.isOpen);
        const bd = document.getElementById('chat-backdrop');
        if (bd) bd.classList.toggle('visible', ORB.isOpen);
        if (ORB.isOpen) {
            positionChat();
            setTimeout(() => { const i = document.getElementById('chat-input'); if (i) i.focus(); }, 400);
        }
    }
    function onOrbHover() { if (!ORB.isOpen) { ORB.targetX = ORB.x; ORB.targetY = ORB.y; ORB.vx = 0; ORB.vy = 0; } }
    function onOrbLeave() { if (!ORB.isOpen) setTimeout(pickNewTarget, 1500); }

    function startClock() {
        const el = document.getElementById('hud-clock');
        if (!el) return;
        function tick() {
            const d = new Date();
            el.textContent = [d.getHours(), d.getMinutes(), d.getSeconds()]
                .map(n => String(n).padStart(2,'0')).join(':');
        }
        tick(); setInterval(tick, 1000);
    }

    function startHudCanvas() {
        const canvas = document.getElementById('hud-canvas');
        if (!canvas) return;
        const ctx = canvas.getContext('2d');
        const W = 360, H = 500;
        canvas.width = W; canvas.height = H;
        const nodes = Array.from({length: 22}, () => ({
            x: Math.random() * W, y: Math.random() * H,
            vx: (Math.random() - .5) * .4, vy: (Math.random() - .5) * .4,
        }));
        function drawFrame() {
            ctx.clearRect(0, 0, W, H);
            nodes.forEach(n => {
                n.x += n.vx; n.y += n.vy;
                if (n.x < 0 || n.x > W) n.vx *= -1;
                if (n.y < 0 || n.y > H) n.vy *= -1;
            });
            for (let i = 0; i < nodes.length; i++) {
                for (let j = i+1; j < nodes.length; j++) {
                    const d = Math.hypot(nodes[i].x - nodes[j].x, nodes[i].y - nodes[j].y);
                    if (d < 100) {
                        ctx.beginPath();
                        ctx.moveTo(nodes[i].x, nodes[i].y);
                        ctx.lineTo(nodes[j].x, nodes[j].y);
                        ctx.strokeStyle = `rgba(139,92,246,${.5 * (1 - d/100)})`;
                        ctx.lineWidth = .6;
                        ctx.stroke();
                    }
                }
            }
            nodes.forEach(n => {
                ctx.beginPath();
                ctx.arc(n.x, n.y, 2, 0, Math.PI * 2);
                ctx.fillStyle = 'rgba(167,139,250,.8)';
                ctx.fill();
            });
            requestAnimationFrame(drawFrame);
        }
        drawFrame();
    }

    /* ════════════════════════════════
       CHAT API
    ════════════════════════════════ */
    window.toggleChat = onOrbClick;
    window.autoResize = function(el) { el.style.height = 'auto'; el.style.height = Math.min(el.scrollHeight, 120) + 'px'; };
    window.handleKeyPress = function(e) { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); sendMessage(); } };

    window.sendMessage = async function() {
        const input = document.getElementById('chat-input');
        const text  = input ? input.value.trim() : '';
        if (!text) return;
        input.value = ''; input.style.height = 'auto';
        appendMessage('user', text);
        showTypingIndicator();
        btnEl.classList.add('excited');
        try {
            const res = await fetch('http://localhost:8000/chat', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ query: text, session_id: 'user-123' })
            });
            btnEl.classList.remove('excited');
            removeTypingIndicator();
            if (res.ok) {
                const data = await res.json();
                appendMessage('ai', data.reply);
                if (data.action?.type === 'navigate' && data.action.url)
                    setTimeout(() => { window.location.href = data.action.url; }, 1500);
            } else {
                appendMessage('ai', 'عفواً، هناك مشكلة في الاتصال بالخادم.');
            }
        } catch (err) {
            btnEl.classList.remove('excited');
            removeTypingIndicator();
            appendMessage('ai', 'عفواً، الخادم لا يستجيب. تأكد من تشغيل AI Microservice.');
        }
    };

    window.appendMessage = function(sender, text) {
        const body = document.getElementById('chat-messages');
        if (!body) return;
        const wrap = document.createElement('div');
        wrap.className = `message ${sender}-message`;
        const meta = document.createElement('div');
        meta.className = 'hud-msg-meta';
        meta.textContent = sender === 'ai' ? '◈ AGENT · TRANSMISSION' : '◉ USER · UPLINK';
        const content = document.createElement('div');
        content.className = 'msg-content';
        content.dir = 'auto';
        content.textContent = text;
        wrap.appendChild(meta);
        wrap.appendChild(content);
        body.appendChild(wrap);
        body.scrollTop = body.scrollHeight;
    };

    window.showTypingIndicator = function() {
        const body = document.getElementById('chat-messages');
        if (!body) return;
        const div = document.createElement('div');
        div.id = 'ai-typing'; div.className = 'typing-indicator message ai-message';
        div.innerHTML = '<span></span><span></span><span></span>';
        body.appendChild(div); body.scrollTop = body.scrollHeight;
    };

    window.removeTypingIndicator = function() {
        const el = document.getElementById('ai-typing');
        if (el) el.remove();
    };

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();

})();

/* ════════════════════════════════════════════════════════
   VOICE ASSISTANT  (Groq Whisper STT + ElevenLabs TTS)
════════════════════════════════════════════════════════ */
(function () {

    // ── Config ──────────────────────────────────────────
    const GROQ_API_KEY     = 'YOUR_GROQ_API_KEY';
    const ELEVENLABS_KEY   = 'YOUR_ELEVENLABS_KEY';
    const ELEVENLABS_VOICE = 'pCKbQ4EPGE06zpEPGNvS';
    const STT_MODEL        = 'whisper-large-v3-turbo';
    const STT_LANG         = 'ar';

    // ── State ────────────────────────────────────────────
    let mediaRecorder = null;
    let audioChunks   = [];
    let isRecording   = false;
    let currentAudio  = null;

    // ── DOM helpers ──────────────────────────────────────
    const overlay    = () => document.getElementById('voice-overlay');
    const orbEl      = () => document.getElementById('voice-orb');
    const transcript = () => document.getElementById('voice-transcript');
    const statusEl   = () => document.getElementById('voice-status');
    const micBtn     = () => document.getElementById('mic-btn');

    function setStatus(text)     { if (statusEl())  statusEl().textContent  = text; }
    function setTranscript(text) { if (transcript()) transcript().textContent = text; }
    function orbState(state) {
        const o = orbEl(); if (!o) return;
        o.classList.remove('listening', 'speaking');
        if (state) o.classList.add(state);
    }

    // ── Open / Close ─────────────────────────────────────
    window.toggleVoice = function () {
        if (isRecording) { stopRecording(); return; }
        openVoice();
    };

    window.closeVoice = function () {
        stopRecording();
        stopSpeaking();
        overlay().classList.add('hidden');
        micBtn() && micBtn().classList.remove('active');
    };

    function openVoice() {
        overlay().classList.remove('hidden');
        micBtn() && micBtn().classList.add('active');
        setTranscript('جاري الاستماع...');
        setStatus('تكلم دلوقتي 🎙');
        orbState('listening');
        startRecording();
    }

    // ── Recording ────────────────────────────────────────
    async function startRecording() {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            audioChunks  = [];
            mediaRecorder = new MediaRecorder(stream, { mimeType: 'audio/webm' });
            mediaRecorder.ondataavailable = e => { if (e.data.size) audioChunks.push(e.data); };
            mediaRecorder.onstop = async () => {
                stream.getTracks().forEach(t => t.stop());
                const blob = new Blob(audioChunks, { type: 'audio/webm' });
                await transcribeAndSend(blob);
            };
            mediaRecorder.start();
            isRecording = true;
            setTimeout(() => { if (isRecording) stopRecording(); }, 8000);
        } catch (err) {
            setTranscript('مش قادر يوصل للميكروفون');
            setStatus('تأكد من إذن الميكروفون ⚠️');
            orbState('');
        }
    }

    function stopRecording() {
        if (mediaRecorder && isRecording) {
            mediaRecorder.stop();
            isRecording = false;
            micBtn() && micBtn().classList.remove('active');
            orbState('');
            setStatus('بيعالج الكلام...');
        }
    }

    // ── Whisper STT ──────────────────────────────────────
    async function transcribeAndSend(blob) {
        setStatus('بيحول الكلام لنص... ✨');
        try {
            const formData = new FormData();
            formData.append('file', blob, 'voice.webm');
            formData.append('model', STT_MODEL);
            formData.append('language', STT_LANG);

            const res  = await fetch('https://api.groq.com/openai/v1/audio/transcriptions', {
                method:  'POST',
                headers: { 'Authorization': `Bearer ${GROQ_API_KEY}` },
                body:    formData,
            });
            const data = await res.json();
            const text = data.text?.trim();

            if (!text) {
                setTranscript('معرفتش أسمع حاجة واضحة، حاول تاني 🎙');
                setStatus('جرب تتكلم أوضح');
                orbState('');
                return;
            }

            setTranscript(text);
            setStatus('بيفكر... 🤔');
            orbState('');
            await sendVoiceQuery(text);

        } catch (err) {
            setTranscript('حصل خطأ في تحويل الكلام');
            setStatus('حاول تاني ⚠️');
        }
    }

    // ── Send to chatbot ───────────────────────────────────
    async function sendVoiceQuery(text) {
        if (window.appendMessage) window.appendMessage('user', text);
        try {
            const res  = await fetch('http://localhost:8000/chat', {
                method:  'POST',
                headers: { 'Content-Type': 'application/json' },
                body:    JSON.stringify({ query: text, session_id: 'voice-session' }),
            });
            const data  = await res.json();
            const reply = data.reply || '';

            setTranscript(reply);
            setStatus('بيتكلم... 🔊');
            orbState('speaking');

            if (window.appendMessage) window.appendMessage('ai', reply);
            await speakText(reply);

            if (data.action?.type === 'navigate' && data.action.url)
                setTimeout(() => { window.location.href = data.action.url; }, 2000);

        } catch (err) {
            setTranscript('الخادم مش شغال دلوقتي');
            setStatus('⚠️ تأكد من تشغيل AI Service');
            orbState('');
        }
    }

    // ── ElevenLabs TTS ────────────────────────────────────
    async function speakText(text) {
        try {
            stopSpeaking();
            const res = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${ELEVENLABS_VOICE}`, {
                method: 'POST',
                headers: {
                    'xi-api-key': ELEVENLABS_KEY,
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    text: text,
                    model_id: 'eleven_multilingual_v2',
                    voice_settings: { stability: 0.5, similarity_boost: 0.75 }
                }),
            });
            const audioBlob = await res.blob();
            const audioUrl  = URL.createObjectURL(audioBlob);
            currentAudio    = new Audio(audioUrl);
            currentAudio.onended = () => {
                orbState('');
                setStatus('دوس على الميكروفون للسؤال تاني 🎙');
                URL.revokeObjectURL(audioUrl);
            };
            currentAudio.play();
        } catch (err) {
            orbState('');
            setStatus('دوس على الميكروفون للسؤال تاني 🎙');
        }
    }

    function stopSpeaking() {
        if (currentAudio) { currentAudio.pause(); currentAudio = null; }
    }

})();