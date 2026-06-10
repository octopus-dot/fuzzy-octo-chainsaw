// API Client for Social Media Operation System
const API_BASE = '/api';

const api = {
  async request(method, url, data = null, isFormData = false) {
    const headers = {};
    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
    }
    const token = localStorage.getItem('token');
    if (token) {
      headers['Authorization'] = 'Bearer ' + token;
    }

    const options = { method, headers };
    if (data) {
      options.body = isFormData ? data : JSON.stringify(data);
    }

    const res = await fetch(API_BASE + url, options);
    const json = await res.json();
    if (json.code !== 200) {
      if (res.status === 401 || json.code === 401) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        if (window.location.pathname.indexOf('login.html') === -1) {
          window.location.href = '/login.html';
        }
      }
      throw new Error(json.message || 'Request failed');
    }
    return json;
  },

  get(url) { return this.request('GET', url); },
  post(url, data, isFormData) { return this.request('POST', url, data, isFormData); },
  put(url, data) { return this.request('PUT', url, data); },
  delete(url) { return this.request('DELETE', url); }
};

// ---- Auth API ----
const AuthAPI = {
  login(data) { return api.post('/login', data); },
  register(data) { return api.post('/register', data); }
};

// ---- User API ----
const UserAPI = {
  getProfile() { return api.get('/user/profile'); },
  updateNickname(nickname) { return api.put('/user/profile/nickname', { nickname }); },
  updatePassword(oldPassword, newPassword) { return api.put('/user/profile/password', { oldPassword, newPassword }); },
  uploadAvatar(file) { const fd = new FormData(); fd.append('file', file); return api.post('/user/profile/avatar', fd, true); },
  getUserPosts(page = 1, size = 10) { return api.get(`/user/posts?page=${page}&size=${size}`); },
  getUserComments(page = 1, size = 10) { return api.get(`/user/comments?page=${page}&size=${size}`); }
};

// ---- Post API ----
const PostAPI = {
  getRecommended(page = 1, size = 10) { return api.get(`/recommended?page=${page}&size=${size}`); },
  getByCategory(catId, page = 1, size = 10) { return api.get(`/posts/by-category/${catId}?page=${page}&size=${size}`); },
  search(keyword, page = 1, size = 10) { return api.get(`/posts/search?keyword=${encodeURIComponent(keyword)}&page=${page}&size=${size}`); },
  getDetail(id) { return api.get(`/posts/${id}`); },
  upload(formData) { return api.post('/posts', formData, true); }
};

// ---- Category API ----
const CategoryAPI = {
  getAll() { return api.get('/categories'); }
};

// ---- Admin API ----
const AdminAPI = {
  getPending(page = 1, size = 10) { return api.get(`/admin/posts/pending?page=${page}&size=${size}`); },
  review(postId, action, rejectReason) { return api.post('/admin/posts/review', { postId, action, rejectReason }); }
};

// ---- Favorite API ----
const FavoriteAPI = {
  toggle(postId) { return api.post(`/favorites/${postId}`); },
  remove(postId) { return api.delete(`/favorites/${postId}`); },
  list(page = 1, size = 10) { return api.get(`/favorites?page=${page}&size=${size}`); }
};

// ---- Comment API ----
const CommentAPI = {
  getByPost(postId, page = 1, size = 10) { return api.get(`/comments/post/${postId}?page=${page}&size=${size}`); },
  create(postId, content) { return api.post('/comments', { postId, content }); },
  delete(commentId) { return api.delete(`/comments/${commentId}`); },
  toggleLike(commentId) { return api.post(`/comments/like/${commentId}`); }
};
