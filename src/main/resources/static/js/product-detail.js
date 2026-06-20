document.addEventListener('DOMContentLoaded', () => {
  const root = document.querySelector('[data-page="detail"]');
  if (!root) return;

  initGallery(root);
  initVariantSelect(root);
  initQtyStepper(root);
  initTabs(root);
  initWishlist(root);
  initCartActions(root);
  initStickyHeader();
});

function formatVnd(amount) {
  return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
}

// Gallery Thumbnails
const THUMB_ACTIVE = ['border-[#084c3c]'];
const THUMB_INACTIVE = ['border-stone-200'];

function initGallery(root) {
  const mainImage = root.querySelector('[data-pd-main-image]');
  const thumbs = root.querySelectorAll('[data-pd-thumb]');
  if (!mainImage || !thumbs.length) return;

  thumbs.forEach((thumb) => {
    thumb.addEventListener('click', () => {
      const url = thumb.getAttribute('data-image-url');
      if (!url) return;
      mainImage.src = url;

      thumbs.forEach((t) => {
        t.classList.remove(...THUMB_ACTIVE);
        t.classList.add(...THUMB_INACTIVE);
      });
      thumb.classList.remove(...THUMB_INACTIVE);
      thumb.classList.add(...THUMB_ACTIVE);
    });
  });
}

// Variant Selection
const VARIANT_ACTIVE = ['border-[#084c3c]', 'text-[#084c3c]'];
const VARIANT_INACTIVE = ['border-stone-300', 'text-stone-600'];

function initVariantSelect(root) {
  const pills = root.querySelectorAll('[data-pd-variant]');
  const priceEl = root.querySelector('[data-pd-price]');
  if (!pills.length) return;

  pills.forEach((pill) => {
    pill.addEventListener('click', () => {
      pills.forEach((p) => {
        p.classList.remove(...VARIANT_ACTIVE);
        p.classList.add(...VARIANT_INACTIVE);
      });
      pill.classList.remove(...VARIANT_INACTIVE);
      pill.classList.add(...VARIANT_ACTIVE);

      const price = Number(pill.getAttribute('data-price'));
      if (priceEl && !Number.isNaN(price)) {
        priceEl.textContent = formatVnd(price);
      }
    });
  });
}

// Quantity Stepper
function initQtyStepper(root) {
  const input = root.querySelector('[data-pd-qty-input]');
  const minus = root.querySelector('[data-pd-qty-minus]');
  const plus = root.querySelector('[data-pd-qty-plus]');
  if (!input || !minus || !plus) return;

  minus.addEventListener('click', () => {
    input.value = Math.max(1, Number(input.value || 1) - 1);
  });
  plus.addEventListener('click', () => {
    input.value = Math.max(1, Number(input.value || 1) + 1);
  });
  input.addEventListener('change', () => {
    input.value = Math.max(1, Number(input.value || 1));
  });
}

// Tabs Controller
const TAB_ACTIVE = ['font-bold', 'border-b-2', 'border-white'];
const TAB_INACTIVE = ['font-medium', 'text-white/80'];

function initTabs(root) {
  const tabs = root.querySelectorAll('[data-pd-tab]');
  const panels = root.querySelectorAll('[data-pd-panel]');
  if (!tabs.length) return;

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      const target = tab.getAttribute('data-pd-tab');

      tabs.forEach((t) => {
        t.classList.remove(...TAB_ACTIVE);
        t.classList.add(...TAB_INACTIVE);
      });
      tab.classList.remove(...TAB_INACTIVE);
      tab.classList.add(...TAB_ACTIVE);

      panels.forEach((panel) => {
        panel.classList.toggle('hidden', panel.getAttribute('data-pd-panel') !== target);
      });
    });
  });
}

// Sticky Header Logic (Hiển thị thanh menu nhỏ trên cùng khi cuộn màn hình)
function initStickyHeader() {
  const stickyHeader = document.getElementById('sticky-header');
  if (!stickyHeader) return;

  window.addEventListener('scroll', () => {
    if (window.scrollY > 400) {
      stickyHeader.classList.remove('hidden', '-translate-y-full');
    } else {
      stickyHeader.classList.add('-translate-y-full');
    }
  });
}

// Fake Wishlist Toggle
function initWishlist(root) {
  const btn = root.querySelector('[data-detail-add-fav]');
  if (!btn) return;

  btn.addEventListener('click', () => {
    const isActive = btn.classList.toggle('is-favorited');
    const icon = btn.querySelector('svg');
    if (isActive) {
      icon.classList.remove('text-stone-400');
      icon.classList.add('fill-red-500', 'text-red-500');
    } else {
      icon.classList.add('text-stone-400');
      icon.classList.remove('fill-red-500', 'text-red-500');
    }
  });
}

// Cart & Buy now
function initCartActions(root) {
  const addToCartBtns = root.querySelectorAll('[data-detail-add-cart]');
  const buyNowBtn = root.querySelector('[data-detail-buy-now]');
  const qtyInput = root.querySelector('[data-pd-qty-input]');
  const activeVariant = () => root.querySelector('[data-pd-variant].border-\\[\\#084c3c\\]');

  addToCartBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      const qty = Number(qtyInput ? qtyInput.value : 1);
      const variant = activeVariant();
      console.log('Thêm vào giỏ hàng', { qty, variant: variant ? variant.textContent.trim() : null });
    });
  });

  if (buyNowBtn) {
    buyNowBtn.addEventListener('click', () => {
      const qty = Number(qtyInput ? qtyInput.value : 1);
      const variant = activeVariant();
      console.log('Mua ngay', { qty, variant: variant ? variant.textContent.trim() : null });
    });
  }
}