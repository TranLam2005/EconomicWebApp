(function () {
  const STORAGE_KEYS = {
    accessToken: 'parfumerie_access_token',
    refreshToken: 'parfumerie_refresh_token',
    email: 'parfumerie_user_email',
    name: 'parfumerie_user_name',
    role: 'parfumerie_user_role'
  };

  function $(id) {
    return document.getElementById(id);
  }

  function getParam(name) {
    const params = new URLSearchParams(window.location.search);
    return params.get(name);
  }

  function decodeJwt(token) {
    try {
      const payload = token.split('.')[1];
      if (!payload) return null;
      const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
      const json = decodeURIComponent(atob(normalized).split('').map(function (c) {
        return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
      }).join(''));
      return JSON.parse(json);
    } catch (e) {
      return null;
    }
  }

  function setMessage(targetId, message, type) {
    const target = $(targetId);
    if (!target) return;
    target.textContent = message || '';
    target.className = 'auth-message ' + (type === 'success' ? 'success' : type === 'warning' ? 'warning' : 'error');
    target.style.display = message ? 'block' : 'none';
  }

  async function postJson(url, body) {
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });

    const contentType = response.headers.get('content-type') || '';
    let data = null;
    if (contentType.includes('application/json')) {
      data = await response.json().catch(() => null);
    } else {
      data = await response.text().catch(() => null);
    }

    if (!response.ok) {
      const message = data && typeof data === 'object'
        ? (data.message || data.error || data.detail)
        : data;
      throw new Error(message || 'Yêu cầu không thành công. Vui lòng kiểm tra lại thông tin.');
    }
    return data;
  }

  function saveAuth(tokenResponse, fallbackEmail, fallbackName) {
    localStorage.setItem(STORAGE_KEYS.accessToken, tokenResponse.accessToken || '');
    localStorage.setItem(STORAGE_KEYS.refreshToken, tokenResponse.refreshToken || '');

    const payload = decodeJwt(tokenResponse.accessToken || '');
    const email = payload && payload.sub ? payload.sub : fallbackEmail;
    const roles = payload && Array.isArray(payload.roles) ? payload.roles : [];
    const role = roles.includes('ROLE_ADMIN') || roles.includes('ADMIN') ? 'ADMIN' : 'USER';

    localStorage.setItem(STORAGE_KEYS.email, email || '');
    localStorage.setItem(STORAGE_KEYS.name, fallbackName || email || 'Khách hàng');
    localStorage.setItem(STORAGE_KEYS.role, role);
  }

  function isLoggedIn() {
    return !!localStorage.getItem(STORAGE_KEYS.accessToken);
  }

  function logout() {
    Object.values(STORAGE_KEYS).forEach(function (key) {
      localStorage.removeItem(key);
    });
    window.location.href = '/dang-nhap';
  }

  function renderHeaderAuth() {
    const box = document.querySelector('[data-auth-box]');
    if (!box) return;

    if (!isLoggedIn()) {
      box.innerHTML = `
        <p class="text-[18px] font-semibold">Xin chào, Khách</p>
        <p class="mt-2 text-[16px]">
          <a href="/dang-nhap" class="font-bold hover:underline">Đăng nhập</a>
          <span class="font-normal text-white/85"> hoặc </span>
          <a href="/dang-ky" class="font-bold hover:underline">Đăng ký</a>
        </p>`;
      return;
    }

    const email = localStorage.getItem(STORAGE_KEYS.email) || '';
    const name = localStorage.getItem(STORAGE_KEYS.name) || email || 'Khách hàng';
    const role = localStorage.getItem(STORAGE_KEYS.role) || 'USER';
    const adminLink = role === 'ADMIN'
      ? '<a href="/local-admin/dashboard" class="font-bold hover:underline">Admin</a><span class="mx-1 text-white/70">|</span>'
      : '';

    box.innerHTML = `
      <p class="text-[18px] font-semibold">Xin chào, ${escapeHtml(name)}</p>
      <p class="mt-2 text-[16px]">
        ${adminLink}
        <span class="text-white/85">${escapeHtml(email)}</span>
        <span class="mx-1 text-white/70">|</span>
        <button type="button" data-auth-logout class="font-bold hover:underline">Đăng xuất</button>
      </p>`;
  }

  function escapeHtml(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  async function handleLogin(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector('button[type="submit"]');
    const email = form.email.value.trim();
    const password = form.password.value;

    if (!email || !password) {
      setMessage('loginMessage', 'Vui lòng nhập đầy đủ email và mật khẩu.', 'error');
      return;
    }

    try {
      button.disabled = true;
      button.textContent = 'Đang đăng nhập...';
      setMessage('loginMessage', '', 'success');

      const tokenResponse = await postJson('/auth/login', { email, password });
      saveAuth(tokenResponse, email, email);
      setMessage('loginMessage', 'Đăng nhập thành công. Đang chuyển trang...', 'success');

      const redirect = getParam('redirect') || '/';
      setTimeout(function () {
        window.location.href = redirect;
      }, 500);
    } catch (error) {
      setMessage('loginMessage', 'Đăng nhập thất bại. Email hoặc mật khẩu không đúng.', 'error');
    } finally {
      button.disabled = false;
      button.textContent = 'Đăng nhập';
    }
  }

  async function handleRegister(event) {
    event.preventDefault();
    const form = event.currentTarget;
    const button = form.querySelector('button[type="submit"]');
    const firstName = form.firstName.value.trim();
    const lastName = form.lastName.value.trim();
    const email = form.email.value.trim();
    const password = form.password.value;
    const confirmPassword = form.confirmPassword.value;

    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      setMessage('registerMessage', 'Vui lòng nhập đầy đủ thông tin.', 'error');
      return;
    }

    if (password.length < 6) {
      setMessage('registerMessage', 'Mật khẩu nên có ít nhất 6 ký tự.', 'warning');
      return;
    }

    if (password !== confirmPassword) {
      setMessage('registerMessage', 'Mật khẩu nhập lại chưa khớp.', 'error');
      return;
    }

    try {
      button.disabled = true;
      button.textContent = 'Đang tạo tài khoản...';
      setMessage('registerMessage', '', 'success');

      // Chỉ cần /auth/register thành công là coi như đăng ký thành công.
      // Không gộp lỗi đăng nhập tự động vào lỗi đăng ký, vì tài khoản có thể đã được lưu DB.
      await postJson('/auth/register', { firstName, lastName, email, password });
      setMessage('registerMessage', 'Đăng ký thành công. Đang đăng nhập...', 'success');

      try {
        const tokenResponse = await postJson('/auth/login', { email, password });
        saveAuth(tokenResponse, email, (firstName + ' ' + lastName).trim());
        setMessage('registerMessage', 'Đăng ký và đăng nhập thành công. Đang chuyển trang...', 'success');
        setTimeout(function () {
          window.location.href = '/';
        }, 700);
      } catch (loginError) {
        setMessage('registerMessage', 'Đăng ký thành công nhưng chưa tự đăng nhập được. Vui lòng sang trang đăng nhập.', 'warning');
        setTimeout(function () {
          window.location.href = '/dang-nhap?email=' + encodeURIComponent(email);
        }, 1200);
      }
    } catch (error) {
      const text = (error && error.message ? error.message : '').toLowerCase();
      if (text.includes('tồn tại') || text.includes('exist') || text.includes('duplicate') || text.includes('email')) {
        setMessage('registerMessage', 'Email này đã được sử dụng. Vui lòng đăng nhập hoặc dùng email khác.', 'error');
      } else {
        setMessage('registerMessage', error && error.message ? error.message : 'Đăng ký thất bại. Vui lòng kiểm tra lại thông tin.', 'error');
      }
    } finally {
      button.disabled = false;
      button.textContent = 'Đăng ký';
    }
  }

  document.addEventListener('DOMContentLoaded', function () {
    renderHeaderAuth();

    const loginForm = $('loginForm');
    if (loginForm) {
      const emailFromUrl = getParam('email');
      if (emailFromUrl && loginForm.email) loginForm.email.value = emailFromUrl;
      loginForm.addEventListener('submit', handleLogin);
    }

    const registerForm = $('registerForm');
    if (registerForm) registerForm.addEventListener('submit', handleRegister);

    document.addEventListener('click', function (event) {
      if (event.target && event.target.matches('[data-auth-logout]')) {
        logout();
      }
    });
  });

  window.ParfumerieAuth = {
    isLoggedIn,
    logout,
    getAccessToken: function () { return localStorage.getItem(STORAGE_KEYS.accessToken); },
    getUserEmail: function () { return localStorage.getItem(STORAGE_KEYS.email); }
  };
})();
