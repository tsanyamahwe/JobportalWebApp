/*
 * summernote-init.js
 * Reusable Summernote initializer for a Thymeleaf + Spring Boot app.
 *
 * Usage in a Thymeleaf template:
 *   <textarea class="summernote" name="body" th:field="*{body}"></textarea>
 *
 * Optional per-editor overrides via data attributes:
 *   data-height="400"
 *   data-lang="en-US"                 (a matching file must exist in vendor/summernote/lang)
 *   data-placeholder="Write here..."
 *   data-upload-url="/api/uploads/images"   (omit to keep images as inline base64)
 *
 * Global defaults can be overridden BEFORE this file loads:
 *   <script>window.SummernoteDefaults = { height: 500 };</script>
 */
(function ($) {
  'use strict';

  if (typeof $ === 'undefined') {
    console.error('[summernote-init] jQuery is required and must be loaded first.');
    return;
  }

  /* ------------------------------------------------------------------ *
   * Spring Security CSRF support.
   * Put these in your layout <head> so AJAX uploads are not rejected:
   *   <meta name="_csrf"        th:content="${_csrf.token}"/>
   *   <meta name="_csrf_header" th:content="${_csrf.headerName}"/>
   * ------------------------------------------------------------------ */
  var csrfToken = $('meta[name="_csrf"]').attr('content');
  var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

  if (csrfToken && csrfHeader) {
    $(document).ajaxSend(function (e, xhr) {
      xhr.setRequestHeader(csrfHeader, csrfToken);
    });
  }

  var BASE_OPTIONS = {
    height: 300,
    minHeight: 200,
    focus: false,
    disableDragAndDrop: false,
    dialogsInBody: true,   // avoids modal/z-index clashes inside Bootstrap modals
    dialogsFade: true,
    toolbar: [
      ['style', ['style']],
      ['font', ['bold', 'italic', 'underline', 'strikethrough', 'clear']],
      ['fontname', ['fontname']],
      ['fontsize', ['fontsize']],
      ['color', ['color']],
      ['para', ['ul', 'ol', 'paragraph']],
      ['table', ['table']],
      ['insert', ['link', 'picture', 'video', 'hr']],
      ['view', ['fullscreen', 'codeview', 'help']]
    ]
  };

  /**
   * Uploads a single file to the given endpoint and inserts the returned URL.
   * The endpoint is expected to return either a bare URL string or JSON
   * such as { "url": "/uploads/abc.png" }.
   */
  function uploadImage($editor, file, uploadUrl) {
    var data = new FormData();
    data.append('file', file);

    $.ajax({
      url: uploadUrl,
      method: 'POST',
      data: data,
      contentType: false,
      processData: false,
      cache: false
    }).done(function (response) {
      var url = (response && response.url) ? response.url : response;
      $editor.summernote('insertImage', url, function ($image) {
        $image.css('max-width', '100%');
      });
    }).fail(function (xhr) {
      console.error('[summernote-init] Image upload failed:', xhr.status, xhr.responseText);
      window.alert('Image upload failed. Please try again.');
    });
  }

  function initEditor(element) {
    var $el = $(element);

    if ($el.data('summernoteInitialised')) {
      return;
    }

    var uploadUrl = $el.data('upload-url');

    var options = $.extend(true, {}, BASE_OPTIONS, window.SummernoteDefaults || {}, {
      height: $el.data('height') || undefined,
      lang: $el.data('lang') || undefined,
      placeholder: $el.data('placeholder') || undefined
    });

    // Strip undefined keys so they do not clobber the defaults.
    Object.keys(options).forEach(function (key) {
      if (options[key] === undefined) {
        delete options[key];
      }
    });

    if (uploadUrl) {
      options.callbacks = $.extend({}, options.callbacks, {
        onImageUpload: function (files) {
          var $editor = $(this);
          for (var i = 0; i < files.length; i++) {
            uploadImage($editor, files[i], uploadUrl);
          }
        }
      });
    }

    $el.summernote(options);
    $el.data('summernoteInitialised', true);
  }

  // Expose for manual/dynamic initialisation (e.g. content loaded into a modal).
  window.initSummernote = function (selector) {
    $(selector || '.summernote').each(function () {
      initEditor(this);
    });
  };

  $(function () {
    window.initSummernote('.summernote');
  });

})(window.jQuery);
