(() => {
  'use strict';

  if (!window.Quill) return;

  document.querySelectorAll('[data-quill-editor]').forEach(editorElement => {
    const form = editorElement.closest('[data-quill-form]');
    const hiddenInput = form?.querySelector('[data-quill-input]');
    const field = editorElement.closest('.quill-form-field');
    const error = field?.querySelector('[data-quill-error]');

    const quill = new window.Quill(editorElement, {
      theme: 'snow',
      formats: ['header', 'bold', 'italic', 'underline', 'blockquote', 'list', 'link'],
      modules: {
        toolbar: [
          [{ header: [2, 3, false] }],
          ['bold', 'italic', 'underline'],
          [{ list: 'ordered' }, { list: 'bullet' }],
          ['blockquote', 'link'],
          ['clean']
        ]
      }
    });

    const updateValidation = () => {
      const isEmpty = quill.getText().trim().length === 0;
      field?.classList.toggle('is-invalid', isEmpty);
      if (error) error.hidden = !isEmpty;
      return !isEmpty;
    };

    quill.on('text-change', () => {
      if (quill.getText().trim()) {
        field?.classList.remove('is-invalid');
        if (error) error.hidden = true;
      }
    });

    form?.addEventListener('submit', event => {
      if (!updateValidation()) {
        event.preventDefault();
        quill.focus();
        return;
      }
      if (hiddenInput) hiddenInput.value = quill.root.innerHTML;
    });
  });
})();
