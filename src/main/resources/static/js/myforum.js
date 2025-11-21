const feedEl = document.getElementById('feed');
const hotForumsEl = document.getElementById('hotForums');
const pagerEl = document.getElementById('pager');
const qEl = document.getElementById('q');

let page = 1;
let totalPages = 1;
const pageSize = 10;

const ACCESS_TOKEN_KEY = 'myforum.accessToken';
const REFRESH_TOKEN_KEY = 'myforum.refreshToken';
let csrfToken = '';

async function ensureCsrfToken() {
    if (csrfToken) {
        return csrfToken;
    }
    const res = await fetch('/csrf-token', { credentials: 'same-origin' });
    if (!res.ok) {
        throw new Error('无法获取 CSRF Token');
    }
    const data = await res.json();
    csrfToken = data.token;
    return csrfToken;
}

function saveTokens(tokens) {
    if (!tokens) return;
    localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

function getAccessToken() {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
}

function getRefreshToken() {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
}

function clearTokens() {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
}

async function refreshTokens() {
    const refreshToken = getRefreshToken();
    if (!refreshToken) return false;
    const res = await fetch('/users/refresh-token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ refreshToken })
    });
    if (!res.ok) {
        clearTokens();
        return false;
    }
    const data = await res.json();
    if (data?.tokens) {
        saveTokens(data.tokens);
        return true;
    }
    return false;
}

async function fetchWithAuth(url, options = {}, retry = true) {
    const headers = new Headers(options.headers || {});
    const token = getAccessToken();
    if (token) {
        headers.set('Authorization', `Bearer ${token}`);
    }
    if (options.method && options.method.toUpperCase() !== 'GET') {
        const csrf = await ensureCsrfToken();
        headers.set('X-XSRF-TOKEN', csrf);
    }
    const res = await fetch(url, { ...options, headers });
    if (res.status === 401 && retry && await refreshTokens()) {
        return fetchWithAuth(url, options, false);
    }
    return res;
}

async function requireLogin() {
    if (!getAccessToken()) {
        window.location.href = '/login.html';
        return false;
    }
    try {
        const res = await fetchWithAuth('/users/me');
        if (res.ok) {
            return true;
        }
    } catch (err) {
        console.error('requireLogin failed', err);
    }
    window.location.href = '/login.html';
    return false;
}

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await ensureCsrfToken();
    } catch (err) {
        console.warn('无法初始化 CSRF token', err);
    }
    const btnLogin = document.getElementById('btnLogin');
    try {
        const res = await fetchWithAuth('/users/me');
        const data = await res.json();
        if (res.ok && data?.user) {
            btnLogin.textContent = data.user.username;
            btnLogin.addEventListener('click', () => window.location.href = '/profile.html');
        } else {
            btnLogin.textContent = '登录 / 注册';
            btnLogin.addEventListener('click', () => window.location.href = '/login.html');
        }
    } catch (err) {
        console.error('failed to load current user', err);
        btnLogin.textContent = '登录 / 注册';
        btnLogin.addEventListener('click', () => window.location.href = '/login.html');
    }
});

const btnWrite = document.getElementById('btnWrite');
const postModal = document.getElementById('postModal');
const closeModal = document.getElementById('closeModal');
const postForm = document.getElementById('postForm');

btnWrite.addEventListener('click', async () => {
    const loggedIn = await requireLogin();
    if (!loggedIn) return;
    postModal.style.display = 'flex';
});

closeModal.addEventListener('click', () => {
    postModal.style.display = 'none';
});

postForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(postForm);
    const payload = {
        title: formData.get('title'),
        content: formData.get('content')
    };
    try {
        const res = await fetchWithAuth('/myforum/createPost', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const result = await res.json();
        if (res.ok && result.code === 200) {
            alert('发布成功！');
            postModal.style.display = 'none';
            postForm.reset();
            loadPosts();
        } else {
            alert(`发布失败: ${result.message || res.status}`);
        }
    } catch (err) {
        console.error(err);
        alert('发布失败，请稍后重试');
    }
});

document.getElementById('btnSearch').addEventListener('click', () => {
    page = 1;
    loadPosts();
});

async function loadPosts() {
    feedEl.innerHTML = renderLoading();
    const keyword = encodeURIComponent(qEl.value.trim());
    try {
        const url = `/myforum/listPosts?page=${page}&size=${pageSize}&q=${keyword}`;
        const res = await fetch(url);
        if (!res.ok) throw new Error('加载失败');
        const result = await res.json();
        const posts = (result && result.code === 200) ? result.posts : [];
        const total = result.total || 0;
        const pages = result.pages || 1;
        renderPosts(posts, total, pages);
    } catch (err) {
        console.warn('加载帖子失败，使用示例数据：', err);
        renderPosts(samplePosts(), 3, 1);
    }
}

function renderLoading() {
    return `<div class="card-box empty">加载中…</div>`;
}

function renderPosts(posts, total, pages) {
    if (!posts || posts.length === 0) {
        feedEl.innerHTML = `<div class="card-box empty">暂无帖子 — 成为第一个开口的人吧。</div>`;
        pagerEl.innerHTML = '';
        totalPages = 1;
        return;
    }

    totalPages = pages;
    feedEl.innerHTML = posts.map(p => postCard(p)).join('');

    const forums = {};
    posts.forEach(p => { if (p.forum) forums[p.forum] = (forums[p.forum] || 0) + 1; });
    hotForumsEl.innerHTML = Object.keys(forums).slice(0, 6)
        .map(f => `<span class="pill">${f} · ${forums[f]}</span>`).join('') || '<div class="small">暂无热门板块</div>';

    pagerEl.innerHTML = `
      <button class="btn secondary" ${page<=1 ? 'disabled' : ''} onclick="pagePrev()">上一页</button>
      <div style="padding:10px 14px;border-radius:10px;background:#fff">${page} / ${pages} 页（共 ${total} 条）</div>
      <button class="btn secondary" ${page>=pages ? 'disabled' : ''} onclick="pageNext()">下一页</button>
    `;
}

function postCard(p) {
    const created = p.createdAt ? new Date(p.createdAt).toLocaleString() : '';
    const excerpt = p.excerpt || (p.content ? (p.content.length>180? p.content.slice(0,180)+'...': p.content) : '');
    const forum = p.forum || '默认';
    return `
    <article class="post" onclick="goToPost(${p.id})" style="cursor:pointer">
      <div class="meta">
        <div class="forum">${forum}</div>
        <div style="margin-top:10px" class="small">${created.split(' ')[0] || ''}</div>
      </div>
      <div class="avatar" style="display:none"></div>
      <div class="body">
        <h3 class="title">${escapeHtml(p.title || '（无标题）')}</h3>
        <div class="excerpt">${escapeHtml(excerpt)}</div>
        <div class="footer">
          <div class="small">作者：${escapeHtml(p.author || '匿名')}</div>
          <div class="small">• ${escapeHtml(created)}</div>
        </div>
      </div>
    </article>
  `;
}

function escapeHtml(s){
    if(!s) return '';
    return s.replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
}

window.goToPost = function(id){
    window.location.href = `/post/${id}`;
};

window.pagePrev = function(){
    if(page>1){ page--; loadPosts(); }
};
window.pageNext = function(){
    if(page<totalPages){ page++; loadPosts(); }
};

function samplePosts(){
    return [
        { id:1, title:'欢迎来到新论坛', content:'这是首篇示例文章。用它来确认显示效果。', author:'admin', createdAt: new Date().toISOString(), forum:'公告' },
        { id:2, title:'如何搭建本地开发环境', content:'本贴介绍如何在本地跑起后端和前端。', author:'dev', createdAt: new Date().toISOString(), forum:'开发' },
        { id:3, title:'讨论：设计一个好的 API', content:'API 设计应兼顾简洁与扩展...', author:'alice', createdAt: new Date().toISOString(), forum:'后端' }
    ];
}

loadPosts();

