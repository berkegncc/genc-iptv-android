# Security Policy / Güvenlik Politikası

## English

### Reporting a vulnerability

Please report security vulnerabilities **privately** — do **not** open a public
issue, since that discloses the problem before a fix is available.

- **Preferred:** use GitHub's **"Report a vulnerability"** button under the
  repository's **Security** tab (Private Vulnerability Reporting).
- **Alternatively:** contact the maintainer privately through their GitHub
  profile ([@berkegncc](https://github.com/berkegncc)).

When reporting, please include:

- a description of the issue and its potential impact,
- steps to reproduce (or a proof of concept),
- the affected version (e.g. the APK version from **Releases**).

We aim to acknowledge valid reports within a few days and to ship a fix in the
next release.

### Scope

Genç IPTV Player is a **client-side media player** with no backend of its own:

- IPTV / Xtream servers are **user-supplied** and therefore **out of scope** —
  report issues with a provider to that provider.
- IPTV credentials are stored **locally on-device** and excluded from backups.
- In-scope examples: credential leakage by the app, insecure local storage,
  signing/build integrity, or code-level vulnerabilities in this repository.

### Supported versions

Only the **latest release** receives security fixes.

---

## Türkçe

### Güvenlik açığı bildirimi

Lütfen güvenlik açıklarını **özel olarak** bildir — **public issue açma**, çünkü
bu, düzeltme hazır olmadan sorunu herkese açık hale getirir.

- **Tercih edilen:** deponun **Security** sekmesindeki **"Report a vulnerability"**
  (Özel Güvenlik Bildirimi) butonunu kullan.
- **Alternatif:** geliştiriciyle GitHub profili üzerinden özel iletişime geç
  ([@berkegncc](https://github.com/berkegncc)).

Bildiriminde lütfen şunları ekle:

- sorunun açıklaması ve olası etkisi,
- yeniden üretme adımları (veya kavram kanıtı / PoC),
- etkilenen sürüm (örn. **Releases**'teki APK sürümü).

Geçerli bildirimleri birkaç gün içinde yanıtlamayı ve bir sonraki sürümde
düzeltmeyi hedefliyoruz.

### Kapsam

Genç IPTV Player, kendi backend'i olmayan bir **istemci tarafı oynatıcıdır**:

- IPTV / Xtream sunucuları **kullanıcı tarafından sağlanır**, bu yüzden **kapsam
  dışıdır** — sağlayıcıyla ilgili sorunları o sağlayıcıya bildir.
- IPTV kimlik bilgileri **cihazda yerel olarak** saklanır ve yedeklere dahil
  edilmez.
- Kapsam içi örnekler: uygulamanın kimlik bilgisi sızdırması, güvensiz yerel
  depolama, imzalama/derleme bütünlüğü veya bu depodaki kod seviyesinde açıklar.

### Desteklenen sürümler

Yalnızca **en güncel sürüm** güvenlik düzeltmeleri alır.
