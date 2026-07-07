(() => {
  const $ = (selector, root = document) => root.querySelector(selector);
  const $$ = (selector, root = document) => Array.from(root.querySelectorAll(selector));

  const header = $('[data-header]');
  const navToggle = $('[data-nav-toggle]');
  const navMenu = $('[data-nav-menu]');

  const normalizePath = value => {
    if (!value) return '/';
    let path = value;

    if (path.startsWith('http://') || path.startsWith('https://')) {
      path = new URL(path, window.location.origin).pathname;
    }

    if (!path.startsWith('/')) {
      path = `/${path}`;
    }

    path = path.replace(/\/index\.html$/i, '/');
    path = path.replace(/\/+$/, '');
    return path || '/';
  };

  const updateHeader = () => {
    if (!header) return;
    header.classList.toggle('is-scrolled', window.scrollY > 14);
  };

  window.addEventListener('scroll', updateHeader, { passive: true });
  updateHeader();

  const currentPath = normalizePath(window.location.pathname);
  const navLinks = $$('.nav-menu .nav-link');

  navLinks.forEach(link => {
    const href = link.getAttribute('href');
    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return;

    const linkPath = normalizePath(new URL(href, window.location.origin).pathname);
    if (linkPath === currentPath) {
      link.classList.add('is-active');
      link.setAttribute('aria-current', 'page');

      const parentDropdown = link.closest('.nav-item-dropdown');
      const parentLink = parentDropdown?.querySelector('.nav-link-parent');
      if (parentLink && parentLink !== link) {
        parentLink.classList.add('is-active');
      }
    }
  });

  navToggle?.addEventListener('click', () => {
    const isOpen = navToggle.getAttribute('aria-expanded') === 'true';
    navToggle.setAttribute('aria-expanded', String(!isOpen));
    navMenu?.classList.toggle('is-open', !isOpen);
  });

  navMenu?.addEventListener('click', event => {
    if (event.target.closest('a')) {
      navMenu.classList.remove('is-open');
      navToggle?.setAttribute('aria-expanded', 'false');
    }
  });

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape') {
      navMenu?.classList.remove('is-open');
      navToggle?.setAttribute('aria-expanded', 'false');
    }
  });

  const revealItems = $$('.reveal');
  if ('IntersectionObserver' in window) {
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.14 });
    revealItems.forEach(item => observer.observe(item));
  } else {
    revealItems.forEach(item => item.classList.add('is-visible'));
  }

  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const heroBadges = $$('[data-hero-badge]');

  if (!prefersReducedMotion && heroBadges.length) {
    heroBadges.forEach(badge => {
      badge.addEventListener('mousemove', event => {
        const rect = badge.getBoundingClientRect();
        const dx = (event.clientX - rect.left) / rect.width - 0.5;
        const dy = (event.clientY - rect.top) / rect.height - 0.5;
        badge.style.transform = `translateY(-4px) rotateX(${(-dy * 3).toFixed(2)}deg) rotateY(${(dx * 4).toFixed(2)}deg)`;
      });

      badge.addEventListener('mouseleave', () => {
        badge.style.transform = '';
      });
    });
  }

  const defaultTestimonialSlides = [
    'assets/slide/3013153_1_054751154538.jpg',
    'assets/slide/3013153_1_054751291642.png',
    'assets/slide/3013153_1_05475444899.jpg',
    'assets/slide/3013153_1_054755517317.jpg',
    'assets/slide/3013153_1_054755571275.jpg',
    'assets/slide/3013153_1_054756177509.jpg',
    'assets/slide/3013153_1_054757699205.jpg',
    'assets/slide/3013153_1_054758088056.jpg',
    'assets/slide/3013153_1_054758958225.jpg',
    'assets/slide/3013153_1_054759271173.jpg',
    'assets/slide/3013153_1_1753361936458585662306801.jpg',
    'assets/slide/3013153_1_175336193645858566263139.jpg',
    'assets/slide/3013153_1_1753361936458585663653104.jpg',
    'assets/slide/3013153_1_1753361936458585664001742.jpg',
    'assets/slide/3013153_1_1753361936458585664574078.jpg',
    'assets/slide/3013153_1_1753361936458585664809850.jpg',
    'assets/slide/3013153_1_1753361936458585666149389.jpg',
    'assets/slide/3013153_1_1753361936458585666242130.jpg',
    'assets/slide/3013153_1_1753361936458585667601668.jpg',
    'assets/slide/3013153_1_1753361936458585668634866.jpg',
    'assets/slide/3013153_1_1753361936458585669844856.jpg',
    'assets/slide/3013153_1_443171444290.jpg',
    'assets/slide/3013153_1_44317384050.jpg',
    'assets/slide/3013153_1_443175095715.jpg',
    'assets/slide/3013153_1_443175923853.jpg',
    'assets/slide/3013153_1_443177289722.jpg',
    'assets/slide/3013153_1_443177838032.jpg',
    'assets/slide/3013153_1_443179263051.jpg'
  ];

  const cuidaSlides = [
    'assets/slide/cuida1.jpg',
    'assets/slide/cuida2.jpg',
    'assets/slide/cuida3.jpg',
    'assets/slide/cuida4.jpg',
    'assets/slide/cuida5.jpg',
    'assets/slide/cuida6.jpg',
    'assets/slide/cuida7.jpg',
    'assets/slide/cuida8.png'
  ];

  const brincaSlides = [
    'assets/slide/brinca1.jpg',
    'assets/slide/brinca2.jpg',
    'assets/slide/brinca3.jpg',
    'assets/slide/brinca4.jpg',
    'assets/slide/brinca5.jpg',
    'assets/slide/brinca6.jpg',
    'assets/slide/brinca7.jpg',
    'assets/slide/brinca8.jpg',
    'assets/slide/brinca9.jpg',
    'assets/slide/brinca10.jpg',
    'assets/slide/brinca11.jpg'
  ];

  const interageSlides = [
    'assets/slide/interage1.jpg',
    'assets/slide/interage2.jpg',
    'assets/slide/interage3.jpg',
    'assets/slide/interage4.jpg',
    'assets/slide/interage5.jpg',
    'assets/slide/interage6.jpg',
    'assets/slide/interage7.jpg'
  ];

  const desenvolveSlides = [
    'assets/slide/desenvolve1.jpg',
    'assets/slide/desenvolve2.jpg',
    'assets/slide/desenvolve3.jpg',
    'assets/slide/desenvolve4.jpg',
    'assets/slide/desenvolve5.jpg',
    'assets/slide/desenvolve6.jpg',
    'assets/slide/desenvolve7.jpg',
    'assets/slide/desenvolve8.jpg',
    'assets/slide/desenvolve9.jpg',
    'assets/slide/desenvolve10.jpg'
  ];
  const lightbox = $('[data-lightbox]');
  const lightboxImage = $('[data-lightbox-image]');
  const lightboxCloseEls = $$('[data-lightbox-close]');
  const slideSets = {
    default: defaultTestimonialSlides,
    cuida: cuidaSlides,
    brinca: brincaSlides,
    interage: interageSlides,
    desenvolve: desenvolveSlides
  };
  const testimonialCards = $$('[data-testimonial-set]');

  const openLightbox = imageSrc => {
    if (!lightbox || !lightboxImage || !imageSrc) return;
    lightboxImage.src = imageSrc;
    lightbox.hidden = false;
    lightbox.setAttribute('aria-hidden', 'false');
    document.body.style.overflow = 'hidden';
  };

  const closeLightbox = () => {
    if (!lightbox || !lightboxImage) return;
    lightbox.hidden = true;
    lightbox.setAttribute('aria-hidden', 'true');
    lightboxImage.src = '';
    document.body.style.overflow = '';
  };

  testimonialCards.forEach(card => {
    const imageEl = $('[data-testimonial-image]', card);
    const openBtn = $('[data-testimonial-open]', card);
    const prevBtn = $('[data-testimonial-prev]', card);
    const nextBtn = $('[data-testimonial-next]', card);
    const testimonialSet = card.dataset.testimonialSet || 'default';
    const testimonialSlides = slideSets[testimonialSet] || defaultTestimonialSlides;
    let testimonialIndex = 0;

    if (!imageEl || !testimonialSlides.length) return;

    const renderTestimonial = () => {
      const item = testimonialSlides[testimonialIndex];
      imageEl.style.opacity = '0';
      window.setTimeout(() => {
        imageEl.src = item;
        imageEl.style.opacity = '1';
      }, 160);
    };

    const nextTestimonial = () => {
      testimonialIndex = (testimonialIndex + 1) % testimonialSlides.length;
      renderTestimonial();
    };

    const prevTestimonial = () => {
      testimonialIndex = (testimonialIndex - 1 + testimonialSlides.length) % testimonialSlides.length;
      renderTestimonial();
    };

    nextBtn?.addEventListener('click', nextTestimonial);
    prevBtn?.addEventListener('click', prevTestimonial);
    imageEl.style.transition = 'opacity .18s ease';
    window.setInterval(nextTestimonial, 6500);
    openBtn?.addEventListener('click', () => openLightbox(imageEl.src));
  });

  lightboxCloseEls.forEach(element => {
    element.addEventListener('click', closeLightbox);
  });

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && lightbox && !lightbox.hidden) {
      closeLightbox();
    }
  });

  const tabButtons = $$('[data-tab-target]');
  const tabPanels = $$('.tab-panel');
  tabButtons.forEach(button => {
    button.addEventListener('click', () => {
      const targetId = button.dataset.tabTarget;
      tabButtons.forEach(btn => {
        btn.classList.toggle('is-active', btn === button);
        btn.setAttribute('aria-selected', String(btn === button));
      });
      tabPanels.forEach(panel => {
        panel.classList.toggle('is-active', panel.id === targetId);
      });
    });
  });

  const filterButtons = $$('[data-filter]');
  const classCards = $$('.class-card');
  filterButtons.forEach(button => {
    button.addEventListener('click', () => {
      const filter = button.dataset.filter;
      filterButtons.forEach(btn => btn.classList.toggle('is-active', btn === button));
      classCards.forEach(card => {
        const show = filter === 'all' || card.dataset.category === filter;
        card.hidden = !show;
        if (show) card.classList.add('is-visible');
      });
    });
  });

  const formatPhone = input => {
    const digits = input.value.replace(/\D/g, '').slice(0, 11);
    if (digits.length <= 2) {
      input.value = digits;
      return;
    }
    if (digits.length <= 6) {
      input.value = `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
      return;
    }
    if (digits.length <= 10) {
      input.value = `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
      return;
    }
    input.value = `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7)}`;
  };

  $$('#telefone, .phone-mask').forEach(phoneInput => {
    phoneInput.addEventListener('input', () => formatPhone(phoneInput));
  });

  const form = $('[data-contact-form]');
  const formStatus = $('[data-form-status]');
  form?.addEventListener('submit', event => {
    event.preventDefault();
    const data = new FormData(form);
    const nome = String(data.get('nome') || '').trim();
    const telefone = String(data.get('telefone') || '').trim();
    const idade = String(data.get('idade') || '').trim();
    const mensagem = String(data.get('mensagem') || '').trim();

    if (!nome || !telefone || !idade) {
      if (formStatus) {
        formStatus.textContent = 'Preencha nome, telefone e idade da criança.';
        formStatus.style.color = '#E95032';
      }
      return;
    }

    const whatsappMessage = [
      'Olá, Alphabetz! Gostaria de agendar uma visita.',
      `Responsável: ${nome}`,
      `Telefone: ${telefone}`,
      `Idade da criança: ${idade}`,
      mensagem ? `Mensagem: ${mensagem}` : ''
    ].filter(Boolean).join('\n');

    if (formStatus) {
      formStatus.textContent = 'Abrindo WhatsApp com a mensagem pronta...';
      formStatus.style.color = '#004E4A';
    }

    const url = `https://wa.me/552730291110?text=${encodeURIComponent(whatsappMessage)}`;
    window.open(url, '_blank', 'noopener');
  });

  const careerForm = $('[data-career-form]');
  const careerStatus = $('[data-career-status]');
  careerForm?.addEventListener('submit', event => {
    event.preventDefault();
    const data = new FormData(careerForm);
    const requiredFields = ['nome', 'email', 'telefone', 'area'];
    const missing = requiredFields.some(field => !String(data.get(field) || '').trim());
    const fileInput = $('#curriculo');
    const file = fileInput?.files?.[0];
    const consent = careerForm.querySelector('input[name="consentimento"]')?.checked;

    const setCareerStatus = (message, color = '#004E4A') => {
      if (!careerStatus) return;
      careerStatus.textContent = message;
      careerStatus.style.color = color;
    };

    if (missing || !file || !consent) {
      setCareerStatus('Preencha os campos obrigatórios, anexe o currículo e marque o consentimento.', '#E95032');
      return;
    }

    const allowedExtensions = /\.(pdf|doc|docx)$/i;
    const maxSize = 5 * 1024 * 1024;

    if (!allowedExtensions.test(file.name)) {
      setCareerStatus('Envie o currículo em PDF, DOC ou DOCX.', '#E95032');
      return;
    }

    if (file.size > maxSize) {
      setCareerStatus('O currículo deve ter até 5 MB.', '#E95032');
      return;
    }

    setCareerStatus('Candidatura validada. Integre este formulário a um backend para receber o anexo em produção.');
  });

})();
