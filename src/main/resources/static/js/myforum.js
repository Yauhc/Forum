const feedEl = document.getElementById('feed');
const hotForumsEl = document.getElementById('hotForums');
const pagerEl = document.getElementById('pager');
const qEl = document.getElementById('q');

let page = 1;
const pageSize = 10;

/**
 * 检查用户是否登录
 * @returns {Promise<boolean>} 已登录返回 true，否则跳转登录页并返回 false
 */
async function requireLogin() {
    try {
        const res = await fetch('/users/me'); // 调用后端接口获取当前登录用户信息
        const data = await res.json();
        if (res.ok && data?.user) {
            // 已登录
            return true;
        } else {
            // 未登录，跳转登录页
            window.location.href = '/login.html';
            return false;
        }
    } catch (err) {
        console.error('检查登录状态失败', err);
        window.location.href = '/login.html';
        return false;
    }
}


document.addEventListener("DOMContentLoaded", async () => {
    const btnLogin = document.getElementById('btnLogin');

    try {
        // 向后端查询当前登录状态（你需要写一个 /users/me 或类似接口）
        const res = await fetch("/users/me");
        const data = await res.json();

        if (res.ok && data?.user) {
            // 已登录
            btnLogin.textContent = data.user.username; // 显示用户名
            btnLogin.addEventListener('click', () => {
                // 以后可以加下拉菜单：比如退出登录 / 个人中心
                window.location.href = '/profile.html';
            });
        } else {
            // 未登录
            btnLogin.textContent = "登录 / 注册";
            btnLogin.addEventListener('click', () => {
                window.location.href = '/login.html';
            });
        }
    } catch (err) {
        console.error("检查登录状态失败:", err);
        btnLogin.textContent = "登录 / 注册";
        btnLogin.addEventListener('click', () => {
            window.location.href = '/login.html';
        });
    }
});

const btnWrite = document.getElementById('btnWrite');
const postModal = document.getElementById('postModal');
const closeModal = document.getElementById('closeModal');
const postForm = document.getElementById('postForm');

btnWrite.addEventListener('click', async () => {
    const loggedIn = await requireLogin();
    if (!loggedIn) return; // 未登录已跳转，不继续执行

    // 已登录则显示弹窗
    postModal.style.display = 'flex';
});

closeModal.addEventListener('click', () => {
    postModal.style.display = 'none';
});

window.addEventListener('click', (e) => {
    if(e.target === postModal){
        postModal.style.display = 'none';
    }
});

postForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const formData = new FormData(postForm);
    const data = {
        title: formData.get('title'),
        content: formData.get('content'),
    };

    try {
        const res = await fetch('/myforum/createPost', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });
        const result = await res.json();
        if(res.ok && result.code === 200){
            alert('发布成功！');
            postModal.style.display = 'none';
            loadPosts(); // 刷新帖子列表
        } else {
            alert('发布失败: ' + (result.message || '未知错误'));
        }
    } catch(err){
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
    const q = qEl.value.trim();
    try {
        // 传分页参数
        const url = `/myforum/listPosts?page=${page}&size=${pageSize}`;
        const res = await fetch(url, { method: 'GET' });
        if (!res.ok) throw new Error('网络响应非 200');
        const result = await res.json();

        // 后端返回的数据结构
        const posts = (result && result.code === 200) ? result.posts : [];
        const total = result.total || 0;
        const pages = result.pages || 1;

        renderPosts(posts || [], total, pages);
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
        return;
    }

    feedEl.innerHTML = posts.map(p => postCard(p)).join('');

    // 热门版块（简单统计）
    const forums = {};
    posts.forEach(p => { if (p.forum) forums[p.forum] = (forums[p.forum] || 0) + 1; });
    hotForumsEl.innerHTML = Object.keys(forums).slice(0,6).map(f => `<span class="pill">${f} · ${forums[f]}</span>`).join('') || '<div class="small">暂无热门板块</div>';

    //用 total/pages 来渲染分页
    pagerEl.innerHTML = `
      <button class="btn secondary" ${page<=1 ? 'disabled' : ''} onclick="pagePrev()">上一页</button>
      <div style="padding:10px 14px;border-radius:10px;background:#fff">${page} / ${pages} 页（共 ${total} 条）</div>
      <button class="btn secondary" ${page>=pages ? 'disabled' : ''} onclick="pageNext(${pages})">下一页</button>
    `;
}

function postCard(p) {
    const created = p.createdAt ? new Date(p.createdAt).toLocaleString() : '';
    const excerpt = p.excerpt || (p.content ? (p.content.length>180? p.content.slice(0,180)+'...': p.content) : '');
    const forum = p.forum || '默认';
    const avatarText = (p.author || 'U').slice(0,2).toUpperCase();
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
    // 打开帖子详情页（你需要实现 /post/{id}）
    window.location.href = `/post/${id}`;
};

window.pagePrev = function(){
    if(page>1){ page--; loadPosts(); }
};
window.pageNext = function(){
    if(page<pages){ page++; loadPosts(); }
};

// 示例数据（当后端不可用时展示）
function samplePosts(){
    return [
        { id:1, title:'欢迎来到新论坛', content:'这是首篇示例文章。用它来确认显示效果。', author:'admin', createdAt: new Date().toISOString(), forum:'公告' },
        { id:2, title:'如何搭建本地开发环境', content:'本贴介绍如何在本地跑起后端和前端。', author:'dev', createdAt: new Date().toISOString(), forum:'开发' },
        { id:3, title:'讨论：设计一个好的 API', content:'API 设计应兼顾简洁与扩展...', author:'alice', createdAt: new Date().toISOString(), forum:'后端' }
    ];
}

// 首次加载
loadPosts();
