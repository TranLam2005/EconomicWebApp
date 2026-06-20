document.addEventListener('DOMContentLoaded', () => {
  const root = document.querySelector('[data-page="home"]');
  if (!root) return;

  initHeroCarousel(root);
  initScrollTop(root);
  initBlogSlider(root);
});

// Hero Banner Carousel
function initHeroCarousel(root) {
  const hero = root.querySelector('[data-home-hero]');
  if (!hero) return;

  const slides = hero.querySelectorAll('[data-hero-slide]');
  const dots = hero.querySelectorAll('[data-hero-dot]');
  if (!slides.length) return;

  let current = 0;
  let timer = null;

  function goTo(index) {
    slides.forEach((slide, i) => {
      slide.classList.toggle('opacity-0', i !== index);
      slide.classList.toggle('opacity-90', i === index);
    });
    dots.forEach((dot, i) => {
      dot.classList.toggle('w-8', i === index);
      dot.classList.toggle('w-2', i !== index);
      dot.classList.toggle('bg-white', i === index);
      dot.classList.toggle('bg-white/50', i !== index);
    });
    current = index;
  }

  function next() {
    goTo((current + 1) % slides.length);
  }

  function startAutoplay() {
    stopAutoplay();
    timer = setInterval(next, 5000);
  }

  function stopAutoplay() {
    if (timer) clearInterval(timer);
  }

  dots.forEach((dot) => {
    dot.addEventListener('click', () => {
      const index = Number(dot.getAttribute('data-index'));
      goTo(index);
      startAutoplay();
    });
  });

  goTo(0);
  startAutoplay();
}

// Scroll to top button
function initScrollTop(root) {
  const btn = root.querySelector('[data-scroll-top]');
  if (!btn) return;

  btn.addEventListener('click', () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  });
}

// Blog / Thông tin slider (prev/next theo từng nhóm thẻ)
function initBlogSlider(root) {
  const prevBtn = root.querySelector('[data-blog-prev]');
  const nextBtn = root.querySelector('[data-blog-next]');
  const track = root.querySelector('[data-blog-prev]')?.closest('.relative')?.querySelector('.grid');
  if (!prevBtn || !nextBtn || !track) return;

  prevBtn.addEventListener('click', () => {
    track.scrollBy({ left: -track.clientWidth, behavior: 'smooth' });
  });
  nextBtn.addEventListener('click', () => {
    track.scrollBy({ left: track.clientWidth, behavior: 'smooth' });
  });
}