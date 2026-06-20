const SAMPLE_PRODUCTS = [
  {
    id: 1,
    productName: 'Dior Sauvage EDP',
    brand: 'Dior',
    gender: 'Men',
    concentration: 'EDP',
    releaseYear: 2018,
    price: 3200000,
    description: 'Mùi hương nam tính, sạch, cay nhẹ và dễ dùng cho hằng ngày.',
    variants: [{ id: 101, variantName: '100ml', volumeMl: 100, price: 3200000, stockQuantity: 15 }],
    images: []
  },
  {
    id: 2,
    productName: 'Chanel Coco Mademoiselle',
    brand: 'Chanel',
    gender: 'Women',
    concentration: 'EDP',
    releaseYear: 2001,
    price: 4100000,
    description: 'Mùi hương nữ thanh lịch, sang trọng, hợp đi làm và đi tiệc.',
    variants: [{ id: 102, variantName: '50ml', volumeMl: 50, price: 4100000, stockQuantity: 8 }],
    images: []
  },
  {
    id: 3,
    productName: 'Gucci Bloom',
    brand: 'Gucci',
    gender: 'Women',
    concentration: 'EDP',
    releaseYear: 2017,
    price: 2850000,
    description: 'Hương hoa trắng mềm mại, nữ tính, dễ tạo cảm giác sạch sẽ.',
    variants: [{ id: 103, variantName: '100ml', volumeMl: 100, price: 2850000, stockQuantity: 10 }],
    images: []
  },
  {
    id: 4,
    productName: 'YSL Libre',
    brand: 'YSL',
    gender: 'Unisex',
    concentration: 'EDP',
    releaseYear: 2019,
    price: 3650000,
    description: 'Mùi hương hiện đại, ngọt ấm vừa đủ, hợp phong cách tự tin.',
    variants: [{ id: 104, variantName: '90ml', volumeMl: 90, price: 3650000, stockQuantity: 12 }],
    images: []
  }
];

const money = value => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(Number(value || 0));
const getStore = key => JSON.parse(localStorage.getItem(key) || '[]');
const setStore = (key, value) => localStorage.setItem(key, JSON.stringify(value));
const getToken = () => localStorage.getItem('parfumerieToken') || '';
const normalizeText = value => String(value || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');

function productId(product) { return Number(product.id || product.productId || product.product_id || 0); }
function productName(product) { return product.productName || product.name || product.title || 'Sản phẩm nước hoa'; }
function productPrice(product) {
  const variantPrice = Array.isArray(product.variants) && product.variants[0] ? product.variants[0].price : null;
  return Number(product.price || variantPrice || 0);
}
function productBrand(product) { return product.brand || 'Parfumerie'; }
function productDesc(product) { return product.description || product.desc || 'Nước hoa chính hãng được tuyển chọn tại Parfumerie.'; }
function productGender(product) { return product.gender || 'Unisex'; }
function productConcentration(product) { return product.concentration || 'EDP'; }
function productYear(product) { return product.releaseYear || product.year || '2026'; }

function updateCounters() {
  const cart = getStore('parfumerieCart');
  const favorites = getStore('parfumerieFavorites');
  const cartCount = cart.reduce((sum, item) => sum + Number(item.quantity || 1), 0);
  document.querySelectorAll('[data-cart-count]').forEach(el => el.textContent = cartCount);
  document.querySelectorAll('[data-fav-count]').forEach(el => el.textContent = favorites.length);
}

function addToCart(product, quantity = 1) {
  const cart = getStore('parfumerieCart');
  const id = productId(product);
  const existing = cart.find(item => Number(item.id) === id);
  if (existing) existing.quantity = Number(existing.quantity || 1) + quantity;
  else cart.push({ id, productName: productName(product), brand: productBrand(product), price: productPrice(product), quantity });
  setStore('parfumerieCart', cart);
  updateCounters();
}

function addToFavorites(product) {
  const favorites = getStore('parfumerieFavorites');
  const id = productId(product);
  if (!favorites.some(item => Number(item.id) === id)) {
    favorites.push({ id, productName: productName(product), brand: productBrand(product), price: productPrice(product), gender: productGender(product), concentration: productConcentration(product) });
    setStore('parfumerieFavorites', favorites);
  }
  updateCounters();
}

function cardHtml(product) {
  const id = productId(product) || 1;
  return `
    <article class="product-card">
      <a href="/shop/products/${id}" class="product-art" aria-label="Xem ${productName(product)}"></a>
      <h3>${productName(product)}</h3>
      <p>${productBrand(product)} • ${productGender(product)} • ${productConcentration(product)}</p>
      <div class="price">${money(productPrice(product))}</div>
      <div class="card-actions">
        <button class="primary-btn" data-add-cart="${id}">Thêm giỏ</button>
        <button class="icon-btn" data-add-favorite="${id}" aria-label="Thêm yêu thích">♥</button>
      </div>
    </article>
  `;
}

function bindProductButtons(scope = document, products = SAMPLE_PRODUCTS) {
  scope.querySelectorAll('[data-add-cart]').forEach(btn => {
    btn.addEventListener('click', () => {
      const product = products.find(p => productId(p) === Number(btn.dataset.addCart)) || SAMPLE_PRODUCTS[0];
      addToCart(product);
      btn.textContent = 'Đã thêm';
      setTimeout(() => btn.textContent = 'Thêm giỏ', 900);
    });
  });
  scope.querySelectorAll('[data-add-favorite]').forEach(btn => {
    btn.addEventListener('click', () => {
      const product = products.find(p => productId(p) === Number(btn.dataset.addFavorite)) || SAMPLE_PRODUCTS[0];
      addToFavorites(product);
      btn.textContent = '✓';
    });
  });
}

async function loadProducts(params = {}) {
  const query = new URLSearchParams(params);
  const endpoints = [
    `/products/search?${query}`,
    `/public/products/search?${query}`,
    `/api/products/search?${query}`
  ];
  for (const endpoint of endpoints) {
    try {
      const res = await fetch(endpoint);
      if (!res.ok) continue;
      const data = await res.json();
      if (Array.isArray(data)) return data;
      if (Array.isArray(data.content)) return data.content;
      if (Array.isArray(data.data)) return data.data;
    } catch (err) {}
  }
  return filterSampleProducts(params);
}

function filterSampleProducts(params = {}) {
  const keyword = normalizeText(params.keyword || '');
  const brand = normalizeText(params.brand || '');
  const gender = normalizeText(params.gender || '');
  const concentration = normalizeText(params.concentration || '');
  return SAMPLE_PRODUCTS.filter(product => {
    const haystack = normalizeText(`${productName(product)} ${productBrand(product)} ${productGender(product)} ${productConcentration(product)} ${productDesc(product)}`);
    return (!keyword || haystack.includes(keyword))
      && (!brand || normalizeText(productBrand(product)).includes(brand))
      && (!gender || normalizeText(productGender(product)).includes(gender))
      && (!concentration || normalizeText(productConcentration(product)).includes(concentration));
  });
}

function initHeaderSearch() {
  const form = document.querySelector('[data-header-search]');
  if (!form) return;
  form.addEventListener('submit', event => {
    event.preventDefault();
    const keyword = form.keyword.value.trim();
    window.location.href = `/shop/search${keyword ? `?keyword=${encodeURIComponent(keyword)}` : ''}`;
  });
}

async function initHome() {
  const grid = document.querySelector('[data-home-products]');
  if (!grid) return;
  const products = await loadProducts({ keyword: '' });
  grid.innerHTML = products.slice(0, 4).map(cardHtml).join('');
  bindProductButtons(grid, products);
}

async function initSearch() {
  const grid = document.querySelector('[data-search-results]');
  if (!grid) return;
  const url = new URL(window.location.href);
  const keywordInput = document.querySelector('[data-search-keyword]');
  const brandSelect = document.querySelector('[data-search-brand]');
  const genderSelect = document.querySelector('[data-search-gender]');
  const resultCount = document.querySelector('[data-result-count]');
  const resultTitle = document.querySelector('[data-result-title]');

  keywordInput.value = url.searchParams.get('keyword') || '';
  brandSelect.value = url.searchParams.get('brand') || '';
  genderSelect.value = url.searchParams.get('gender') || '';

  async function render() {
    const params = {
      keyword: keywordInput.value.trim(),
      brand: brandSelect.value,
      gender: genderSelect.value,
      concentration: url.searchParams.get('concentration') || ''
    };
    const products = await loadProducts(params);
    resultTitle.textContent = params.keyword ? `Kết quả cho “${params.keyword}”` : 'Danh sách sản phẩm';
    resultCount.textContent = `${products.length} sản phẩm`;
    grid.innerHTML = products.length ? products.map(cardHtml).join('') : '<div class="empty-state">Không tìm thấy sản phẩm phù hợp.</div>';
    bindProductButtons(grid, products);
  }

  document.querySelector('[data-search-button]').addEventListener('click', () => {
    const nextUrl = new URL('/shop/search', window.location.origin);
    if (keywordInput.value.trim()) nextUrl.searchParams.set('keyword', keywordInput.value.trim());
    if (brandSelect.value) nextUrl.searchParams.set('brand', brandSelect.value);
    if (genderSelect.value) nextUrl.searchParams.set('gender', genderSelect.value);
    window.history.replaceState({}, '', nextUrl);
    render();
  });
  render();
}

function initCart() {
  const list = document.querySelector('[data-cart-list]');
  if (!list) return;
  const subtotalEl = document.querySelector('[data-cart-subtotal]');
  const totalEl = document.querySelector('[data-cart-total]');

  function render() {
    const cart = getStore('parfumerieCart');
    if (!cart.length) {
      list.innerHTML = '<div class="empty-state">Giỏ hàng đang trống. Bấm “Thêm sản phẩm mẫu” hoặc quay lại trang tìm kiếm.</div>';
      subtotalEl.textContent = totalEl.textContent = money(0);
      updateCounters();
      return;
    }
    let subtotal = 0;
    list.innerHTML = cart.map(item => {
      subtotal += Number(item.price || 0) * Number(item.quantity || 1);
      return `
        <div class="cart-item">
          <div class="cart-thumb"></div>
          <div>
            <strong>${item.productName}</strong>
            <p>${item.brand} • ${money(item.price)}</p>
          </div>
          <div class="qty-box">
            <button data-cart-minus="${item.id}">−</button>
            <strong>${item.quantity || 1}</strong>
            <button data-cart-plus="${item.id}">+</button>
            <button data-cart-remove="${item.id}">Xóa</button>
          </div>
        </div>
      `;
    }).join('');
    subtotalEl.textContent = totalEl.textContent = money(subtotal);
    list.querySelectorAll('[data-cart-plus]').forEach(btn => btn.addEventListener('click', () => updateQty(btn.dataset.cartPlus, 1)));
    list.querySelectorAll('[data-cart-minus]').forEach(btn => btn.addEventListener('click', () => updateQty(btn.dataset.cartMinus, -1)));
    list.querySelectorAll('[data-cart-remove]').forEach(btn => btn.addEventListener('click', () => removeItem(btn.dataset.cartRemove)));
    updateCounters();
  }

  function updateQty(id, change) {
    const cart = getStore('parfumerieCart').map(item => Number(item.id) === Number(id) ? { ...item, quantity: Math.max(1, Number(item.quantity || 1) + change) } : item);
    setStore('parfumerieCart', cart);
    render();
  }
  function removeItem(id) {
    setStore('parfumerieCart', getStore('parfumerieCart').filter(item => Number(item.id) !== Number(id)));
    render();
  }
  document.querySelector('[data-add-demo-cart]').addEventListener('click', () => { addToCart(SAMPLE_PRODUCTS[0]); render(); });
  render();
}

function initFavorites() {
  const grid = document.querySelector('[data-favorite-list]');
  if (!grid) return;

  function favoriteCardHtml(product) {
    const id = productId(product) || 1;
    return `
      <article class="product-card">
        <a href="/shop/products/${id}" class="product-art" aria-label="Xem chi tiết ${productName(product)}"></a>
        <h3>${productName(product)}</h3>
        <p>${productBrand(product)} • ${productGender(product)} • ${productConcentration(product)}</p>
        <div class="price">${money(productPrice(product))}</div>
        <div class="card-actions favorite-actions">
          <button class="primary-btn" data-fav-to-cart="${id}">Thêm giỏ</button>
          <button class="ghost-btn" data-view-detail="${id}">Xem</button>
          <button class="icon-btn" data-remove-favorite="${id}" aria-label="Xóa yêu thích">Xóa</button>
        </div>
      </article>
    `;
  }

  function render() {
    const favorites = getStore('parfumerieFavorites');
    grid.innerHTML = favorites.length ? favorites.map(favoriteCardHtml).join('') : '<div class="empty-state">Chưa có sản phẩm yêu thích. Hãy thêm từ trang tìm kiếm hoặc chi tiết sản phẩm.</div>';

    grid.querySelectorAll('[data-fav-to-cart]').forEach(btn => {
      btn.addEventListener('click', () => {
        const product = favorites.find(item => Number(item.id) === Number(btn.dataset.favToCart));
        if (product) addToCart(product);
        btn.textContent = 'Đã thêm';
        setTimeout(() => btn.textContent = 'Thêm giỏ', 900);
      });
    });

    grid.querySelectorAll('[data-view-detail]').forEach(btn => {
      btn.addEventListener('click', () => {
        window.location.href = `/shop/products/${btn.dataset.viewDetail}`;
      });
    });

    grid.querySelectorAll('[data-remove-favorite]').forEach(btn => {
      btn.addEventListener('click', () => {
        setStore('parfumerieFavorites', getStore('parfumerieFavorites').filter(item => Number(item.id) !== Number(btn.dataset.removeFavorite)));
        render();
      });
    });
    updateCounters();
  }
  render();
}

async function initDetail() {
  const root = document.querySelector('[data-detail-root]');
  if (!root) return;
  const id = Number(document.body.dataset.productId || new URL(location.href).pathname.split('/').pop() || 1);
  let products = await loadProducts({ keyword: '' });
  let product = products.find(p => productId(p) === id) || SAMPLE_PRODUCTS.find(p => productId(p) === id) || SAMPLE_PRODUCTS[0];

  document.querySelector('[data-detail-brand]').textContent = productBrand(product);
  document.querySelector('[data-detail-name]').textContent = productName(product);
  document.querySelector('[data-detail-price]').textContent = money(productPrice(product));
  document.querySelector('[data-detail-gender]').textContent = productGender(product);
  document.querySelector('[data-detail-concentration]').textContent = productConcentration(product);
  document.querySelector('[data-detail-year]').textContent = productYear(product);
  document.querySelector('[data-detail-desc]').textContent = productDesc(product);

  const variants = Array.isArray(product.variants) && product.variants.length ? product.variants : [{ variantName: '100ml', volumeMl: 100 }];
  document.querySelector('[data-detail-variants]').innerHTML = variants.map((variant, index) => `<button class="${index === 0 ? 'active' : ''}">${variant.variantName || `${variant.volumeMl || 100}ml`}</button>`).join('');
  document.querySelector('[data-detail-add-cart]').addEventListener('click', () => addToCart(product));
  document.querySelector('[data-detail-add-fav]').addEventListener('click', () => addToFavorites(product));

  const related = document.querySelector('[data-related-products]');
  related.innerHTML = SAMPLE_PRODUCTS.filter(item => productId(item) !== productId(product)).slice(0, 4).map(cardHtml).join('');
  bindProductButtons(related, SAMPLE_PRODUCTS);
}

function parseJwt(token) {
  try { return JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))); } catch (err) { return null; }
}

function initAccount() {
  const btn = document.querySelector('[data-login-button]');
  if (!btn) return;
  const message = document.querySelector('[data-login-message]');
  const renderProfile = () => {
    const token = getToken();
    const jwt = parseJwt(token);
    const email = jwt?.sub || localStorage.getItem('parfumerieEmail') || 'Chưa đăng nhập';
    const role = Array.isArray(jwt?.roles) ? jwt.roles.join(', ') : 'Guest';
    document.querySelector('[data-account-name]').textContent = email === 'Chưa đăng nhập' ? 'Khách hàng' : email.split('@')[0];
    document.querySelector('[data-account-email]').textContent = email;
    document.querySelector('[data-account-role]').textContent = role;
    document.querySelector('[data-account-fav]').textContent = getStore('parfumerieFavorites').length;
    document.querySelector('[data-account-cart]').textContent = getStore('parfumerieCart').reduce((sum, item) => sum + Number(item.quantity || 1), 0);
  };
  btn.addEventListener('click', async () => {
    message.textContent = 'Đang đăng nhập...';
    try {
      const email = document.querySelector('[data-login-email]').value.trim();
      const password = document.querySelector('[data-login-password]').value;
      const res = await fetch('/auth/login', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email, password }) });
      if (!res.ok) throw new Error('Sai tài khoản hoặc API chưa sẵn sàng');
      const data = await res.json();
      localStorage.setItem('parfumerieToken', data.accessToken || data.token || '');
      localStorage.setItem('parfumerieEmail', email);
      message.textContent = 'Đăng nhập thành công. Token đã lưu để test quản trị khách hàng.';
      renderProfile();
    } catch (err) {
      message.textContent = `Không đăng nhập được: ${err.message}`;
    }
  });
  document.querySelector('[data-logout-button]').addEventListener('click', () => {
    localStorage.removeItem('parfumerieToken');
    localStorage.removeItem('parfumerieEmail');
    renderProfile();
  });
  renderProfile();
}

function initCustomers() {
  const table = document.querySelector('[data-customer-table]');
  if (!table) return;
  const message = document.querySelector('[data-customer-message]');
  let currentUsers = [];
  const sampleUsers = [
    { id: 1, firstName: 'Dang', lastName: 'Hoang', email: 'dang@test.com', role: 'USER', createdAt: '2026-06-09T23:08:55' },
    { id: 2, firstName: 'Admin', lastName: 'Dang', email: 'admin@test.com', role: 'ADMIN', createdAt: '2026-06-13T00:41:30' }
  ];

  function lockedIds() { return getStore('parfumerieLockedUsers').map(Number); }
  function isLocked(id) { return lockedIds().includes(Number(id)); }
  function toggleLocked(id) {
    const ids = lockedIds();
    const next = ids.includes(Number(id)) ? ids.filter(item => item !== Number(id)) : [...ids, Number(id)];
    setStore('parfumerieLockedUsers', next);
  }

  const rowHtml = user => {
    const locked = isLocked(user.id);
    return `
      <tr class="${locked ? 'locked-row' : ''}">
        <td>${user.id || ''}</td>
        <td>${[user.firstName, user.lastName].filter(Boolean).join(' ') || user.fullName || 'Khách hàng'}</td>
        <td>${user.email || ''}</td>
        <td><span class="badge ${locked ? 'warning' : (user.role === 'ADMIN' ? 'dark' : 'success')}">${locked ? 'LOCKED' : (user.role || 'USER')}</span></td>
        <td>${String(user.createdAt || '').replace('T', ' ').slice(0, 19)}</td>
        <td><div class="row-actions"><button data-view-user="${user.id}">Xem</button><button data-lock-user="${user.id}">${locked ? 'Mở khóa' : 'Khóa'}</button></div></td>
      </tr>
    `;
  };

  function bindCustomerActions() {
    table.querySelectorAll('[data-view-user]').forEach(btn => {
      btn.addEventListener('click', () => {
        const user = currentUsers.find(item => Number(item.id) === Number(btn.dataset.viewUser));
        if (!user) return;
        alert(`Thông tin khách hàng\nID: ${user.id}\nTên: ${[user.firstName, user.lastName].filter(Boolean).join(' ') || 'Khách hàng'}\nEmail: ${user.email || ''}\nVai trò: ${user.role || 'USER'}\nTrạng thái: ${isLocked(user.id) ? 'Đã khóa' : 'Đang hoạt động'}`);
      });
    });

    table.querySelectorAll('[data-lock-user]').forEach(btn => {
      btn.addEventListener('click', () => {
        toggleLocked(btn.dataset.lockUser);
        message.textContent = 'Đã cập nhật trạng thái khóa/mở khóa trên giao diện demo.';
        renderRows(currentUsers);
      });
    });
  }

  function renderRows(users) {
    const keyword = normalizeText(document.querySelector('[data-customer-keyword]').value);
    const role = document.querySelector('[data-customer-role]').value;
    currentUsers = users.filter(user => {
      const haystack = normalizeText(`${user.firstName || ''} ${user.lastName || ''} ${user.fullName || ''} ${user.email || ''}`);
      return (!keyword || haystack.includes(keyword)) && (!role || user.role === role);
    });
    table.innerHTML = currentUsers.length ? currentUsers.map(rowHtml).join('') : '<tr><td colspan="6">Không có khách hàng phù hợp.</td></tr>';
    bindCustomerActions();
  }

  async function load() {
    let users = sampleUsers;
    try {
      const res = await fetch('/admin/users', { headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : {} });
      if (res.ok) {
        users = await res.json();
        message.textContent = 'Đã tải danh sách khách hàng từ API /admin/users.';
      } else {
        message.textContent = 'Chưa có token admin hoặc token hết hạn, đang hiển thị dữ liệu mẫu.';
      }
    } catch (err) {
      message.textContent = 'Không gọi được API, đang hiển thị dữ liệu mẫu.';
    }
    renderRows(users);
  }
  document.querySelector('[data-load-customers]').addEventListener('click', load);
  document.querySelector('[data-customer-keyword]').addEventListener('input', load);
  document.querySelector('[data-customer-role]').addEventListener('change', load);
  load();
}

document.addEventListener('click', event => {
  if (event.target.matches('.variant-options button')) {
    event.target.parentElement.querySelectorAll('button').forEach(btn => btn.classList.remove('active'));
    event.target.classList.add('active');
  }
});

document.addEventListener('DOMContentLoaded', () => {
  updateCounters();
  initHeaderSearch();
  initHome();
  initSearch();
  initCart();
  initFavorites();
  initDetail();
  initAccount();
  initCustomers();
});
