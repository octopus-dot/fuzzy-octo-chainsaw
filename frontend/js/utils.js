// Utility functions for H5 frontend

const Utils = {
  // Get current user from localStorage
  getUser() {
    try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; }
  },

  getToken() { return localStorage.getItem('token'); },

  isLoggedIn() { return !!this.getToken(); },

  isAdmin() { const u = this.getUser(); return u && u.role === 'admin'; },

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/index.html';
  },

  // Format date
  formatDate(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' });
  },

  formatDateTime(dateStr) {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    return d.toLocaleString('zh-CN');
  },

  // Relative time
  timeAgo(dateStr) {
    if (!dateStr) return '';
    const now = new Date();
    const d = new Date(dateStr);
    const diff = now - d;
    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + ' 分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + ' 小时前';
    if (diff < 604800000) return Math.floor(diff / 86400000) + ' 天前';
    return this.formatDate(dateStr);
  },

  // Truncate text
  excerpt(text, maxLen = 150) {
    if (!text) return '';
    const plain = text.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/\s+/g, ' ');
    return plain.length > maxLen ? plain.slice(0, maxLen) + '...' : plain;
  },

  // Parse image URLs from JSON string
  parseImages(imageUrls) {
    if (!imageUrls) return [];
    try { return JSON.parse(imageUrls); } catch { return []; }
  },

  // Get first image
  firstImage(imageUrls) {
    const imgs = this.parseImages(imageUrls);
    return imgs.length > 0 ? imgs[0] : null;
  },

  // Avatar URL with fallback
  avatarUrl(url) {
    return url || '/default-avatar.png';
  },

  // Toast notification
  toast(message, type = 'info') {
    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 2500);
  },

  // Simple modal
  showModal(title, content, onConfirm) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal">
        <h3>${title}</h3>
        <div>${content}</div>
        <div class="modal-actions">
          <button class="btn btn-outline cancel-btn">取消</button>
          <button class="btn btn-primary confirm-btn">确认</button>
        </div>
      </div>
    `;
    document.body.appendChild(overlay);
    overlay.querySelector('.cancel-btn').onclick = () => overlay.remove();
    overlay.querySelector('.confirm-btn').onclick = () => {
      overlay.remove();
      if (onConfirm) onConfirm();
    };
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
  },

  // Show reject dialog
  showRejectDialog(onConfirm) {
    const overlay = document.createElement('div');
    overlay.className = 'modal-overlay';
    overlay.innerHTML = `
      <div class="modal">
        <h3>退回理由</h3>
        <div>
          <textarea class="form-textarea" id="reject-reason" placeholder="请输入退回理由..." rows="3"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn btn-outline cancel-btn">取消</button>
          <button class="btn btn-danger confirm-btn">确认退回</button>
        </div>
      </div>
    `;
    document.body.appendChild(overlay);
    overlay.querySelector('.cancel-btn').onclick = () => overlay.remove();
    overlay.querySelector('.confirm-btn').onclick = () => {
      const reason = document.getElementById('reject-reason').value.trim();
      if (!reason) {
        Utils.toast('请输入退回理由', 'error');
        return;
      }
      overlay.remove();
      if (onConfirm) onConfirm(reason);
    };
    overlay.onclick = (e) => { if (e.target === overlay) overlay.remove(); };
  },

  // Build navbar HTML
  renderNavbar(containerId) {
    const user = this.getUser();
    const loggedIn = this.isLoggedIn();
    const el = document.getElementById(containerId);
    if (!el) return;

    let userMenuHTML = '';
    if (loggedIn && user) {
      userMenuHTML = `
        <div class="dropdown" id="user-dropdown">
          <img src="${this.avatarUrl(user.avatarUrl)}" class="nav-avatar" alt="avatar"
               onerror="this.src='/default-avatar.png';this.onerror=null;"
               id="nav-avatar-img" />
          <span class="nav-user">${user.nickname || user.username}</span>
          <div class="dropdown-menu" id="user-dropdown-menu">
            <a class="dropdown-item" href="/profile.html">👤 个人主页</a>
            <a class="dropdown-item" href="/upload.html">📝 发布帖子</a>
            ${user.role === 'admin' ? '<a class="dropdown-item" href="/admin.html">⚙️ 管理审核</a>' : ''}
            <div class="dropdown-divider"></div>
            <a class="dropdown-item" href="javascript:Utils.logout()">🚪 退出登录</a>
          </div>
        </div>
      `;
    } else {
      userMenuHTML = `
        <a href="/login.html"><button class="nav-btn outline">登录</button></a>
        <a href="/register.html"><button class="nav-btn primary">注册</button></a>
      `;
    }

    el.innerHTML = `
      <div class="navbar-inner">
        <a href="/index.html" class="nav-logo">📱 社交运营</a>
        <div class="nav-links">
          <a href="/index.html">推荐</a>
          <span id="nav-categories-btn">分类 ▼</span>
        </div>
        <div class="nav-right">
          <div class="nav-search">
            <input type="text" id="nav-search-input" placeholder="搜索帖子..." />
            <button class="nav-btn primary btn-sm" id="nav-search-btn">搜索</button>
          </div>
          ${userMenuHTML}
        </div>
      </div>
      <div id="categories-dropdown" class="dropdown-menu" style="position:fixed;top:56px;left:50%;transform:translateX(-50%);"></div>
    `;

    // Search handler
    document.getElementById('nav-search-btn').onclick = () => {
      const kw = document.getElementById('nav-search-input').value.trim();
      if (kw) window.location.href = '/search.html?keyword=' + encodeURIComponent(kw);
    };
    document.getElementById('nav-search-input').onkeyup = (e) => {
      if (e.key === 'Enter') {
        const kw = e.target.value.trim();
        if (kw) window.location.href = '/search.html?keyword=' + encodeURIComponent(kw);
      }
    };

    // User dropdown toggle
    if (loggedIn) {
      document.getElementById('nav-avatar-img').onclick = (e) => {
        e.stopPropagation();
        document.getElementById('user-dropdown-menu').classList.toggle('show');
      };
      document.getElementById('user-dropdown-menu').onclick = (e) => e.stopPropagation();
      document.addEventListener('click', () => {
        const menu = document.getElementById('user-dropdown-menu');
        if (menu) menu.classList.remove('show');
      });
    }

    // Load categories for dropdown
    this.loadCategories();
  },

  async loadCategories() {
    try {
      const res = await CategoryAPI.getAll();
      const cats = res.data;
      const dropdown = document.getElementById('categories-dropdown');
      const btn = document.getElementById('nav-categories-btn');
      if (!dropdown || !btn) return;

      dropdown.innerHTML = cats.map(c =>
        `<a class="dropdown-item" href="/category.html?id=${c.id}">${c.name}</a>`
      ).join('');

      let visible = false;
      btn.onclick = (e) => {
        e.stopPropagation();
        visible = !visible;
        dropdown.classList.toggle('show', visible);
      };
      dropdown.onclick = (e) => e.stopPropagation();
      document.addEventListener('click', () => {
        visible = false;
        dropdown.classList.remove('show');
      });
    } catch {}
  },

  // Build pagination
  renderPagination(containerId, currentPage, totalPages, onPageChange) {
    const el = document.getElementById(containerId);
    if (!el || totalPages <= 1) { if (el) el.innerHTML = ''; return; }
    let html = '';
    html += `<button ${currentPage <= 1 ? 'disabled' : ''} data-page="${currentPage - 1}">上一页</button>`;
    for (let i = 1; i <= totalPages; i++) {
      html += `<button class="${i === currentPage ? 'active' : ''}" data-page="${i}">${i}</button>`;
    }
    html += `<button ${currentPage >= totalPages ? 'disabled' : ''} data-page="${currentPage + 1}">下一页</button>`;
    el.innerHTML = html;
    el.querySelectorAll('button').forEach(btn => {
      btn.onclick = () => {
        const p = parseInt(btn.dataset.page);
        if (p && p !== currentPage) onPageChange(p);
      };
    });
  },

  // Toggle favorite
  async toggleFavorite(postId, isFavorited) {
    if (!this.isLoggedIn()) {
      window.location.href = '/login.html';
      return null;
    }
    try {
      if (isFavorited) {
        await FavoriteAPI.remove(postId);
        return false;
      } else {
        await FavoriteAPI.toggle(postId);
        return true;
      }
    } catch {
      return null;
    }
  }
};
