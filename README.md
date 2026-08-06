# Genç IPTV Player

**Türkçe** · [English](README.en.md)

Modern, sade ve tipografi odaklı bir Android IPTV oynatıcı.

**M3U** ve **Xtream Codes** playlist desteğiyle canlı kanalları, filmleri ve
dizileri tek bir uygulamada izlemek için tasarlandı.

> ⚠️ **Genç IPTV Player bir oynatıcıdır** — içerik veya playlist sağlamaz.
> Kullanmak için kendi M3U URL'iniz ya da Xtream bilgileriniz olması gerekir.

---

## Özellikler

### Canlı yayın
- M3U ve Xtream Codes playlist desteği (birden fazla kayıtlı, biri aktif)
- Kategori bazlı kanal gezintisi
- Kanal logoları + anlık program bilgisi
- Program rehberi (XMLTV) — gün gün, kanal kanal
- Aşağı çekerek elle yenileme

### Film & Dizi
- Kategori bazlı film ve dizi kataloğu
- Sezon/bölüm gezintisi
- **Devam Et** — aynı diziden farklı bölümler tek satıra iniyor, kaldığınız
  yerden devam
- Oynatıcıda "Sonraki Bölüm" — sezon sonunda bir sonrakine geçer
- Arama tüm sekmeyi tarar; yazarken kategori filtresi devre dışı kalır

### Oynatıcı
- ExoPlayer (HLS, DASH, MPEG-TS, MP4)
- Pencere içinde pencere (PiP)
- Altyazı özelleştirme — 13 ayar: yazı tipi, renk, kenar, konum, boyut
- En-boy oranı (Orijinal / 16:9 / 21:9 / Doldur / Sığdır)
- Hız kontrolü (film ve dizilerde)
- Üç aşamalı yayın yedeklemesi (HLS → progressive → .ts)
- Özel User-Agent ve tercih edilen ses dili (gelişmiş ayarlar)

### Arayüz
- İki dilli: Türkçe ve İngilizce. Sistem dilinizi izler, Profil → Dil'den
  elle de seçebilirsiniz
- Işığın işaret üzerinde yol aldığı açılış animasyonu
- 8 vurgu rengi + açık/koyu/sistem teması
- Tablet düzeni — geniş ekranda liste ve detay yan yana, kenar navigasyonu
- Favoriler (kanal / film / dizi)
- Tüm içerikte arama
- Son izlenen kanallar ana ekranda

### Güncelleme ve veri
- Uygulama içi güncelleme — GitHub Releases üzerinden kontrol, indirme ve kurulum
- **İçerik Yenileme** ayarı: yalnızca Wi-Fi, ya da mobil veri dahil. Yenileme
  Wi-Fi beklediği için atlandığında ana sayfa bunu söyler
- Uygulama içinden geri bildirim

### Mimari
- Jetpack Compose (Material 3)
- MVVM + Hilt
- Room (SQLite) + DataStore
- Media3 / ExoPlayer
- Retrofit + kotlinx.serialization
- Coil 3
- WorkManager
- Coroutines + Flow

---

## Kurulum

### Hazır APK ile (önerilen)

[Releases](../../releases) sayfasından en güncel APK'yı indirip kurun.
**Bilinmeyen kaynaklardan yüklemeye izin ver** seçeneğini açmanız gerekebilir.

Uygulama kuruluysa üzerine güncellenir, verileriniz korunur. Sonraki
güncellemeler uygulama içinden gelir.

### Kaynaktan derleme

Gereksinimler: **Android Studio** (Hedgehog 2023.1.1+), **Android SDK 36**,
**JDK 17**. Minimum SDK 24 (Android 7.0).

```bash
git clone https://github.com/berkegncc/genc-iptv-android.git
```

Android Studio'da açın → Gradle senkronizasyonu → Run. Terminalden:

```bash
./gradlew :app:assembleDebug
```

### TMDB API anahtarı (isteğe bağlı)

Uygulama TMDB olmadan çalışır. Afişlerin ve arka planların neredeyse tamamı
zaten sağlayıcınızdan geliyor; TMDB yalnızca iki şey için kullanılıyor:

- Film ve dizi detayındaki **oyuncu fotoğrafları**
- Sağlayıcının **afiş vermediği** içerikler için yedek afiş

**Yayınlanan APK'lar anahtar içermez.** Anahtarı APK'ya gömmek, indiren herkesin
onu çıkarabilmesi ve tüm kurulumların tek bir kotayı paylaşması demek olurdu.
Kendi anahtarınızı girmek için:

1. [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api)
   adresinden ücretsiz anahtar alın
2. **Profil → Hakkında → TMDB API Anahtarı**
3. Yapıştırıp kaydedin

Anahtar yalnızca cihazınızda saklanır, hiçbir yere gönderilmez. Kaldırırsanız
bu iki özellik kapanır, gerisi normal çalışır.

> Kaynaktan derleyenler `local.properties` içine `TMDB_API_KEY=...` yazabilir;
> bu dosya gitignore'da. **Anahtarınızı asla commit etmeyin.** Uygulama içi
> ayar her zaman derlemedeki anahtarın önüne geçer.

---

## Kullanım

1. **İlk açılış** — M3U URL'inizi veya Xtream bilgilerinizi girin.
2. **Senkronizasyon** — Playlist eklendiğinde ilk eşitleme başlar. Sonrasında
   günde bir arka planda, ayrıca katalog 6 saatten eskiyse uygulamayı açtığınızda.
   İkisi de İçerik Yenileme ayarınıza uyar; elle yenileme her bağlantıda çalışır.
3. **Kanallar** — Alt navigasyondan Kanallar → kategori → kanal.
4. **Film ve diziler** — Filmler ya da Diziler sekmesi.
5. **Favoriler** — Öğenin yanındaki yıldıza dokunun.
6. **Ayarlar** — Profil: tema, dil, oynatıcı, altyazı, playlist yönetimi.

---

## Yayın çıkarma

Yayın elle yapılıyor ve sürüm numaraları kritik: uygulama içi güncelleme
sistemi bunlara bakıyor.

**1. Sürümü ayarlayın** (`app/build.gradle.kts` → `defaultConfig`)

- `versionCode` — her yayında artmalı. Android güncelleme kararını buna göre
  verir; artırmazsanız kurulum `INSTALL_FAILED_VERSION_DOWNGRADE` ile reddedilir.
  Yayımlanmış bir `versionCode` bir daha kullanılamaz.
- `versionName` — üç haneli semver (`1.3.0`). Git etiketi bunun başına `v`
  eklenmiş hâli olmalı.

**2. Anahtarsız, imzalı APK üretin**

```bash
TMDB_API_KEY= ./gradlew :app:assembleRelease
```

PowerShell'de: `$env:TMDB_API_KEY=""; ./gradlew :app:assembleRelease`

Ortam değişkeni `local.properties`'in önüne geçer. Ama yalnızca buna güvenmeyin
— çalışmakta olan bir Gradle daemon'ı eski ortamı taşıyabilir. Asıl kontrol
derleme çıktısıdır: **"WARNING: this release APK has a TMDB API key compiled
in"** uyarısı görüyorsanız APK'da anahtar vardır, yayınlamayın. Uyarı
çıkmıyorsa temizdir. Kesin çözüm `local.properties` içindeki satırı geçici
olarak boşaltmaktır.

İmzayı doğrulayın:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

`CN=Genc IPTV` görmelisiniz. `CN=Android Debug` görüyorsanız **yayınlamayın** —
mevcut kullanıcıların hiçbirine kurulmaz.

**3. Release oluşturun**

Etiket `v1.3.0` biçiminde, APK'yı asset olarak yükleyin. Güncelleme sistemi
`latest` release'i okur ve **ilk `.apk` uzantılı asset'i** indirir; ön sürüm
işaretli olanları atlar.

### Dikkat

- **Keystore asla değişmemeli.** `genciptv-release.jks` kaybolur veya değişirse
  tüm kullanıcılar uygulamayı kaldırıp yeniden kurmak ve verilerini kaybetmek
  zorunda kalır.
- **Veritabanı şeması değiştiyse migration yazın.** Room bir geçiş yolu
  bulamazsa uygulama çökmez — bütün tabloları siler ve kullanıcı playlist'ini,
  sunucu adresini, kullanıcı adını ve şifresini kaybeder.

---

## Katkı

PR'lara açığım. Büyük değişiklikler için önce issue açıp tartışalım.

- Kotlin resmî stili (`gradle.properties` içinde ayarlı)
- Composable'lar PascalCase, fonksiyonlar camelCase
- **Kullanıcıya görünen metinleri koda yazmayın.** Uygulama iki dilli:
  `res/values/` İngilizce (varsayılan), `res/values-tr/` Türkçe. Yeni bir
  anahtar **iki dosyaya birden** eklenmeli — yalnızca `values/`'a eklerseniz
  Türkçe kullanan kişi orada İngilizce görür ve bu sessizce olur.
  `tools/check_locales.py` iki dilin aynı anahtarları tanımladığını doğrular.
- Değişken içeren metinleri parça birleştirerek kurmayın, `%1$s` kullanın —
  Türkçe ve İngilizce sözdizimi farklı.

---

## Yasal uyarı

- **Genç IPTV Player yalnızca bir medya oynatıcısıdır.** Hiçbir kanal, içerik
  veya playlist **sağlamaz, barındırmaz, yayınlamaz veya dağıtmaz.**
- Uygulama **hiçbir** hazır kaynakla gelmez; kullanmak için kendi **M3U
  URL'inizi** ya da **Xtream Codes** bilgilerinizi girmeniz gerekir.
- Eriştiğiniz içerikten ve uygulamayla kullandığınız IPTV servisinin,
  playlist'in veya hesabın **kendi ülkenizdeki yasallığından yalnızca siz
  sorumlusunuz.**
- Geliştirici, uygulamanın nasıl kullanıldığından ya da kullanıcının girdiği
  kaynakların yasallığı veya içeriğinden **hiçbir sorumluluk kabul etmez.**
- Bu ürün TMDB API'sini kullanır; TMDB tarafından onaylanmış veya
  sertifikalanmış değildir.

Güvenlik açığı bildirimi için [SECURITY.md](SECURITY.md).

---

## Lisans

MIT — ayrıntılar [LICENSE](LICENSE) dosyasında.

---

## Teşekkürler

- [ExoPlayer / Media3](https://github.com/androidx/media)
- [Coil 3](https://github.com/coil-kt/coil)
- [TMDB](https://www.themoviedb.org/)
- [Instrument Serif](https://fonts.google.com/specimen/Instrument+Serif) ·
  [Geist ve Geist Mono](https://vercel.com/font)
