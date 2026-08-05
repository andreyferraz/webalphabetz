(() => {
  'use strict';

  const $ = (selector, context = document) => context.querySelector(selector);
  const $$ = (selector, context = document) => Array.from(context.querySelectorAll(selector));

  const page = document.body;
  let activeModal = null;
  let lastFocusedElement = null;
  let toastTimer = null;

  const sidebar = $('[data-sidebar]');
  const openSidebar = () => {
    if (!sidebar) return;
    page.classList.add('sidebar-open');
    $('[data-sidebar-close]', sidebar)?.focus();
  };
  const closeSidebar = () => page.classList.remove('sidebar-open');

  $('[data-sidebar-open]')?.addEventListener('click', openSidebar);
  $$('[data-sidebar-close]').forEach(button => button.addEventListener('click', closeSidebar));

  const toast = $('[data-toast]');
  const hideToast = () => {
    if (!toast) return;
    toast.classList.remove('is-visible');
    window.clearTimeout(toastTimer);
  };
  const showToast = (title, message) => {
    if (!toast) return;
    $('[data-toast-title]', toast).textContent = title;
    $('[data-toast-message]', toast).textContent = message;
    toast.classList.add('is-visible');
    window.clearTimeout(toastTimer);
    toastTimer = window.setTimeout(hideToast, 4800);
  };
  $('[data-toast-close]')?.addEventListener('click', hideToast);

  const setModalFormMode = (modal, trigger) => {
    const form = $('form', modal);
    const title = $('[data-modal-form-title]', modal);
    const itemNameInput = $('[data-item-name-input]', modal);
    const imageInput = $('[data-image-input]', modal);
    const isEdit = trigger.dataset.formMode === 'edit';
    const itemName = trigger.dataset.itemName || '';
    const isBlog = modal.dataset.modal === 'blog-form';

    if (!isEdit) {
      form?.reset();
      $$('[data-image-preview]', modal).forEach(preview => { preview.innerHTML = ''; });
    }

    if (title) {
      title.textContent = isEdit
        ? `Editar ${isBlog ? 'postagem' : 'slide'}`
        : `Nov${isBlog ? 'a postagem' : 'o slide'}`;
    }
    if (itemNameInput) itemNameInput.value = isEdit ? itemName : '';
    if (imageInput) imageInput.required = !isEdit;
  };

  const openModal = (modal, trigger) => {
    if (!modal) return;
    lastFocusedElement = trigger || document.activeElement;
    activeModal = modal;
    modal.hidden = false;
    modal.setAttribute('aria-hidden', 'false');
    page.classList.add('modal-open');

    if (trigger?.dataset.formMode) setModalFormMode(modal, trigger);

    window.requestAnimationFrame(() => {
      const focusTarget = $('input:not([type="hidden"]), textarea, select, button', modal);
      focusTarget?.focus();
    });
  };

  const closeModal = modal => {
    if (!modal) return;
    modal.hidden = true;
    modal.setAttribute('aria-hidden', 'true');
    page.classList.remove('modal-open');
    activeModal = null;
    lastFocusedElement?.focus();
  };

  $$('[data-modal-open]').forEach(trigger => {
    trigger.addEventListener('click', () => openModal($(`[data-modal="${trigger.dataset.modalOpen}"]`), trigger));
  });

  $$('[data-modal-close]').forEach(button => {
    button.addEventListener('click', () => closeModal(button.closest('[data-modal]')));
  });

  document.addEventListener('keydown', event => {
    if (event.key === 'Escape' && activeModal) closeModal(activeModal);
  });

  $$('[data-delete-trigger]').forEach(trigger => {
    trigger.addEventListener('click', () => {
      const modal = $('[data-modal="delete"]');
      if (!modal) return;
      $('[data-delete-type]', modal).textContent = trigger.dataset.deleteTypeValue || 'item';
      $('[data-delete-name]', modal).textContent = trigger.dataset.deleteNameValue || 'este conteúdo';
      openModal(modal, trigger);
    });
  });

  $('[data-confirm-delete]')?.addEventListener('click', event => {
    closeModal(event.currentTarget.closest('[data-modal]'));
    showToast('Exclusão simulada', 'O item continua disponível porque esta versão não persiste alterações.');
  });

  $$('[data-password-toggle]').forEach(toggle => {
    toggle.addEventListener('click', () => {
      const shell = toggle.closest('.input-shell');
      const input = $('[data-password-input]', shell || toggle.parentElement);
      if (!input) return;
      const isVisible = input.type === 'text';
      input.type = isVisible ? 'password' : 'text';
      toggle.setAttribute('aria-pressed', String(!isVisible));
      toggle.setAttribute('aria-label', isVisible ? 'Exibir senha' : 'Ocultar senha');
      const icon = $('i', toggle);
      icon?.classList.toggle('fa-eye', isVisible);
      icon?.classList.toggle('fa-eye-slash', !isVisible);
    });
  });

  const normalizeText = value => (value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();

  $$('[data-filter-list]').forEach(list => {
    const key = list.dataset.filterList;
    const search = $(`[data-filter-input="${key}"]`);
    const category = $(`[data-category-filter="${key}"]`);
    const emptyState = $(`[data-empty-state="${key}"]`);
    const count = $('[data-visible-count]');
    const items = $$('[data-filter-item]', list);

    const filterItems = () => {
      const term = normalizeText(search?.value);
      const selectedCategory = normalizeText(category?.value);
      let visible = 0;

      items.forEach(item => {
        const matchesTerm = normalizeText(item.dataset.filterText).includes(term);
        const matchesCategory = !selectedCategory || normalizeText(item.dataset.category) === selectedCategory;
        const shouldShow = matchesTerm && matchesCategory;
        item.hidden = !shouldShow;
        if (shouldShow) visible += 1;
      });

      if (emptyState) emptyState.hidden = visible !== 0;
      if (count) count.textContent = String(visible);
    };

    search?.addEventListener('input', filterItems);
    category?.addEventListener('change', filterItems);
  });

  $$('[data-image-input]').forEach(input => {
    const field = input.closest('.form-field');
    const preview = $('[data-image-preview]', field || input.parentElement);
    const dropZone = input.closest('[data-drop-zone]');
    let objectUrls = [];

    const clearObjectUrls = () => {
      objectUrls.forEach(url => URL.revokeObjectURL(url));
      objectUrls = [];
    };

    const renderFiles = fileList => {
      if (!preview) return;
      clearObjectUrls();
      preview.innerHTML = '';

      Array.from(fileList).forEach((file, index) => {
        if (!file.type.startsWith('image/')) return;
        const url = URL.createObjectURL(file);
        objectUrls.push(url);

        const item = document.createElement('div');
        item.className = 'image-preview-item';
        const image = document.createElement('img');
        image.src = url;
        image.alt = `Prévia da imagem ${index + 1}`;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.setAttribute('aria-label', `Remover imagem ${index + 1}`);
        remove.innerHTML = '<i class="fa-solid fa-xmark" aria-hidden="true"></i>';
        remove.addEventListener('click', () => item.remove());
        item.append(image, remove);
        preview.append(item);
      });
    };

    input.addEventListener('change', () => renderFiles(input.files));

    if (dropZone) {
      ['dragenter', 'dragover'].forEach(eventName => {
        dropZone.addEventListener(eventName, event => {
          event.preventDefault();
          dropZone.classList.add('is-dragging');
        });
      });
      ['dragleave', 'drop'].forEach(eventName => {
        dropZone.addEventListener(eventName, event => {
          event.preventDefault();
          dropZone.classList.remove('is-dragging');
        });
      });
      dropZone.addEventListener('drop', event => renderFiles(event.dataTransfer.files));
    }
  });

  const newPassword = $('[data-new-password]');
  const confirmPassword = $('[data-confirm-password]');
  const passwordError = $('[data-password-error]');
  const strengthBar = $('[data-strength-bar]');
  const strengthLabel = $('[data-strength-label]');
  const requirementList = $('[data-password-requirements]');

  const passwordRules = password => ({
    length: password.length >= 8,
    number: /\d/.test(password),
    letter: /[a-z]/.test(password) && /[A-Z]/.test(password),
    symbol: /[^A-Za-z0-9]/.test(password)
  });

  const updatePasswordState = () => {
    if (!newPassword) return;
    const rules = passwordRules(newPassword.value);
    const validRules = Object.values(rules).filter(Boolean).length;
    const strength = validRules * 25;

    if (strengthBar) {
      strengthBar.style.width = `${strength}%`;
      strengthBar.style.background = validRules < 2 ? '#eb593e' : validRules < 4 ? '#ffb000' : '#46a891';
    }
    if (strengthLabel) {
      strengthLabel.textContent = validRules === 4 ? 'Senha forte.' : validRules >= 2 ? 'Senha média. Complete os requisitos.' : 'Use pelo menos 8 caracteres.';
    }
    Object.entries(rules).forEach(([rule, isValid]) => {
      $(`[data-requirement="${rule}"]`, requirementList || document)?.classList.toggle('is-valid', isValid);
    });

    if (confirmPassword?.value) {
      const matches = confirmPassword.value === newPassword.value;
      confirmPassword.setCustomValidity(matches ? '' : 'As senhas não coincidem.');
      if (passwordError) passwordError.hidden = matches;
    }
  };

  newPassword?.addEventListener('input', updatePasswordState);
  confirmPassword?.addEventListener('input', updatePasswordState);

  $$('[data-static-form]').forEach(form => {
    form.addEventListener('submit', event => {
      event.preventDefault();

      if (form.matches('[data-password-form]')) {
        const rules = passwordRules(newPassword?.value || '');
        const meetsRequirements = Object.values(rules).every(Boolean);
        newPassword?.setCustomValidity(meetsRequirements ? '' : 'A senha precisa atender a todos os requisitos.');
        const matches = newPassword?.value === confirmPassword?.value;
        confirmPassword?.setCustomValidity(matches ? '' : 'As senhas não coincidem.');
        if (passwordError) passwordError.hidden = matches;
      }

      if (!form.reportValidity()) return;

      const title = form.dataset.successTitle || 'Ação simulada';
      const message = form.dataset.successMessage || 'Nenhuma alteração foi persistida.';
      const modal = form.closest('[data-modal]');
      if (modal) closeModal(modal);
      showToast(title, message);

      form.reset();
      $$('[data-image-preview]', form).forEach(preview => { preview.innerHTML = ''; });
      if (form.matches('[data-password-form]')) updatePasswordState();
    });
  });
})();
