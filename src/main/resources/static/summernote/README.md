# Summernote bundle for Thymeleaf + Spring Boot

Self-hosted Summernote **0.9.1** with the dependencies it needs, arranged so you can
copy it straight into a Spring Boot project. No CDN calls, so it works offline and
behind a strict Content-Security-Policy.

## What's in the box

| Path | Contents |
|---|---|
| `static/vendor/summernote/` | `summernote-lite`, `summernote-bs4`, `summernote-bs5` (min + unminified lite for debugging) |
| `static/vendor/summernote/font/` | Icon font (eot/ttf/woff/woff2) — **required**, referenced as `font/…` from the CSS |
| `static/vendor/summernote/lang/` | 47 minified locale files |
| `static/vendor/summernote/plugin/` | Bundled example plugins: `hello`, `specialchars`, `databasic` |
| `static/vendor/jquery/` | jQuery 3.7.1 (Summernote 0.9.x needs jQuery 3.x — it does **not** support jQuery 4) |
| `static/vendor/bootstrap-5/` | Bootstrap 5.3.8 CSS + bundle JS |
| `static/vendor/bootstrap-4/` | Bootstrap 4.6.2 CSS + bundle JS |
| `static/js/summernote-init.js` | Shared initialiser: CSRF header, image upload, data-attribute config |
| `templates/fragments/summernote.html` | Thymeleaf fragments for CSS, JS, the editor, and read-only rendering |
| `examples/` | Sample form page, upload controller, resource config, sanitiser notes |

## Install

Copy the two folders into your project:

```
src/main/resources/static/vendor/      <- from static/vendor/
src/main/resources/static/js/          <- from static/js/
src/main/resources/templates/fragments/summernote.html
```

Final layout:

```
src/main/resources/
├── static/
│   ├── js/summernote-init.js
│   └── vendor/
│       ├── bootstrap-4/
│       ├── bootstrap-5/
│       ├── jquery/jquery.min.js
│       └── summernote/
│           ├── font/
│           ├── lang/
│           ├── plugin/
│           └── summernote-*.min.{css,js}
└── templates/
    └── fragments/summernote.html
```

Spring Boot serves `src/main/resources/static` at the context root automatically, so
`/vendor/summernote/summernote-lite.min.css` just works. Nothing to register.

## Which build do I use?

Pick **one** — mixing skins causes double-styled toolbars.

- **`summernote-lite`** — no Bootstrap dependency. Use this if your app uses Tailwind,
  plain CSS, or no framework at all.
- **`summernote-bs5`** — pairs with Bootstrap 5.x, which is the normal choice for a new
  Spring Boot app. Needs `bootstrap.bundle.min.js` (it includes Popper) for the dropdowns
  and modals.
- **`summernote-bs4`** — pairs with Bootstrap 4.6.x. Only for older projects.

There's also a Bootstrap 3 build upstream (`summernote.min.css/js`); it's omitted here
since BS3 is long unmaintained.

## Usage

**1. Page template**

```html
<head>
  <meta name="_csrf" th:content="${_csrf?.token}"/>
  <meta name="_csrf_header" th:content="${_csrf?.headerName}"/>
  <th:block th:replace="~{fragments/summernote :: css-bs5}"></th:block>
</head>
<body>
  <form th:object="${post}" th:action="@{/posts}" method="post">
    <th:block th:replace="~{fragments/summernote :: editor('body', 420, '/api/uploads/images')}"></th:block>
    <button type="submit">Save</button>
  </form>

  <th:block th:replace="~{fragments/summernote :: js-bs5}"></th:block>
</body>
```

Fragment arguments are `(field, height, uploadUrl)`. Pass `''` as the upload URL to keep
images inline as base64 instead of uploading them.

Swap `css-bs5`/`js-bs5` for `css`/`js` (lite) or `css-bs4`/`js-bs4` as needed.

**2. Form object** — the field is plain HTML in a `String`:

```java
public class PostForm {
    private Long id;
    @NotBlank private String title;
    @NotBlank private String body;   // Summernote HTML lands here
    // getters / setters
}
```

Use `columnDefinition = "TEXT"` (Postgres/H2) or `@Lob` on the JPA entity —
a default `varchar(255)` will truncate the content.

**3. Rendering it back**

```html
<th:block th:replace="~{fragments/summernote :: render(${post.body})}"></th:block>
```

This uses `th:utext`, which does **not** escape. Sanitise server-side with Jsoup first —
see `examples/StaticResourceConfig.java` for the safelist. Skipping this is a stored-XSS
vulnerability.

## Per-editor options

`summernote-init.js` picks up any `<textarea class="summernote">` on the page and reads:

```html
<textarea class="summernote"
          name="body"
          data-height="500"
          data-lang="en-US"
          data-placeholder="Start writing…"
          data-upload-url="/api/uploads/images"></textarea>
```

Global defaults, set before the init script runs:

```html
<script>window.SummernoteDefaults = { height: 500, toolbar: [ /* … */ ] };</script>
```

For editors added to the DOM later (inside a modal, say), call `initSummernote('#myArea')`.

## Languages

Load a locale file after the main Summernote script, then set `data-lang`:

```html
<script th:src="@{/vendor/summernote/lang/summernote-fr-FR.min.js}"></script>
<textarea class="summernote" data-lang="fr-FR"></textarea>
```

Available: `ar-AR az-AZ bg-BG bn-BD ca-ES cs-CZ da-DK de-CH de-DE el-GR en-US es-ES es-EU
fa-IR fi-FI fr-FR gl-ES he-IL hr-HR hu-HU id-ID it-IT ja-JP ko-KR lt-LT lt-LV mn-MN nb-NO
nl-NL pl-PL pt-BR pt-PT ro-RO ru-RU sk-SK sl-SI sr-RS sr-RS-Latin sv-SE ta-IN th-TH tr-TR
uk-UA uz-UZ vi-VN zh-CN zh-TW` (47 files). English is the built-in default, so `en-US`
only needs loading if you switch back to it explicitly.

## Image uploads

Default behaviour without `data-upload-url` is base64 inlining, which bloats your rows
fast. `examples/ImageUploadController.java` is a working multipart endpoint that writes
to an external folder and returns `{"url": "/uploads/<uuid>.png"}`.

Note that `src/main/resources/static` is read-only inside a packaged jar — write uploads
to a configured directory on disk and serve them with a resource handler
(`examples/StaticResourceConfig.java`).

## Spring Security

- CSRF: the meta tags plus the `$(document).ajaxSend` hook in `summernote-init.js` handle
  the header for uploads. Without them the POST returns 403.
- Permit static paths:
  `.requestMatchers("/vendor/**", "/js/**", "/uploads/**").permitAll()`

## Common gotchas

- **Toolbar icons show as boxes** — the `font/` folder didn't get copied, or a resource
  handler is rewriting `/vendor/**`. The CSS references `font/summernote.woff2` relative
  to itself, so the folder must sit next to the CSS file.
- **`$ is not defined`** — jQuery must load before `summernote-*.js`, and both before
  `summernote-init.js`.
- **Editor renders but toolbar dropdowns don't open (bs5)** — you loaded
  `bootstrap.min.js` instead of `bootstrap.bundle.min.js`, so Popper is missing.
- **Content is blank on edit** — `th:field` on a `<textarea>` writes the value as the
  element's body; make sure the tag isn't self-closed and has no whitespace you care about.
- **HTML arrives escaped** — you used `th:text` instead of `th:utext` when rendering.
- **413 / silent upload failure** — raise `spring.servlet.multipart.max-file-size`.
- **Editor inside a Bootstrap modal misbehaves** — `dialogsInBody: true` is already set in
  the init script, which fixes the z-index clash.

## Versions

| Package | Version | License |
|---|---|---|
| Summernote | 0.9.1 | MIT |
| jQuery | 3.7.1 | MIT |
| Bootstrap | 5.3.8 / 4.6.2 | MIT |

All fetched from the official npm registry and unmodified apart from removing `.map` files
and unused build variants.
