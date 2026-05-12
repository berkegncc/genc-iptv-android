# Genç IPTV

Modern, sade ve tipografi odaklı bir Android IPTV oynatıcı.

**M3U** ve **Xtream Codes** playlist desteğiyle canlı kanalları, filmleri ve
dizileri tek bir uygulamada izlemek için tasarlandı.

> ⚠️ Genç IPTV bir **oynatıcı**dır — içerik veya playlist sağlamaz. Kullanmak
> için kendi M3U URL'iniz ya da Xtream credentials'larınız olması gerekir.

---

## Özellikler

### Canlı yayın
- M3U ve Xtream Codes playlist desteği (aynı anda birden fazla kayıtlı, biri aktif)
- Kategori bazlı kanal gezintisi (Glyph row UI)
- Kanal logoları + canlı program bilgisi (EPG entegre)
- Program rehberi (XMLTV) — gün gün, kanal kanal grid
- Pull-to-refresh ile manuel senkron
- 6 saatten eski sync varsa otomatik güncelleme

### Film & Dizi (VOD)
- Kategori bazlı film/dizi kataloğu
- Dizi sezon/bölüm gezintisi
- **"Devam Et"** — aynı diziden farklı bölümler tek satıra collapse, kaldığın
  bölümden direkt resume
- Oynatıcıda "Sonraki Bölüm" butonu — sezon arası otomatik geçiş
- TMDB entegrasyonu (opsiyonel) — boş poster'lar otomatik doldurulur, oyuncu
  kadrosu çekilir

### Oynatıcı
- ExoPlayer (HLS, DASH, MPEG-TS, MP4)
- Picture-in-Picture
- Subtitle özelleştirme (13 ayar: font, renk, edge type, konum vb.)
- Aspect ratio (Original / 16:9 / 21:9 / Stretch / Fit)
- Hız kontrolü (VOD)
- 3 aşamalı stream fallback (HLS → progressive → .ts variant)
- Custom User-Agent & SSL bypass (gelişmiş ayarlardan)
- Tercih edilen audio dili (boş bırakılırsa stream'in kendi dili)

### UX
- Animasyonlu splash sekansı (logo fade-in + halo + pulse + wordmark)
- 8 accent renk paleti + light/dark/system tema
- Editorial tipografi (Instrument Serif + Geist + Geist Mono)
- Favoriler (kanallar / filmler / diziler — swipe-able tab'lar)
- Tüm içerikte arama (Ctrl+F tarzı palette)
- Recent channels — son izlenen kanallar ana ekranda

### Mimari
- Jetpack Compose (Material 3)
- MVVM + Hilt DI
- Room (SQLite) + DataStore
- Coil 3 (image loading)
- Retrofit + Kotlinx Serialization (Xtream API)
- Coroutines + Flow

---

## Ekran görüntüleri

> *Ekran görüntüleri eklenecek.*

---

## Kurulum

### Hazır APK ile (önerilen)

[GitHub Releases](../../releases) sayfasından en güncel APK'yı indir, telefonuna
kur. **Bilinmeyen kaynaklardan yüklemeye izin ver**'i açman gerekebilir.

### Kaynaktan derleme

#### Gereksinimler
- **Android Studio** (Hedgehog 2023.1.1+ veya daha yenisi)
- **Android SDK 36** (compile + target)
- **Minimum SDK**: 24 (Android 7.0)
- **JDK 17**

#### Adımlar

```bash
git clone https://github.com/<kullanici>/genc_iptv_mobil.git
cd genc_iptv_mobil
```

Android Studio'da projeyi aç → "Sync Project with Gradle Files" → "Run".
İlk açılışta tüm bağımlılıkları indirir, ardından bağlı cihaza/emülatöre
uygulamayı kurar.

Terminal üzerinden:
```bash
./gradlew :app:assembleDebug
# Çıktı: app/build/outputs/apk/debug/app-debug.apk
```

#### TMDB API anahtarı (opsiyonel)

Genç IPTV temel kullanım için TMDB'siz çalışır — Xtream sağlayıcı kendi
poster'ları ve metadata'sını döner. Ancak:

- Xtream'in **dönmediği** posterler için TMDB fallback aktif
- Film ve dizi detay sayfasındaki **oyuncu kadrosu** TMDB'den çekiliyor

Bu özellikleri istiyorsan:
1. https://www.themoviedb.org/settings/api adresinden ücretsiz API key al
2. `local.properties.example` dosyasını `local.properties` olarak kopyala
3. `TMDB_API_KEY=...` satırına anahtarını ekle
4. Projeyi yeniden derle

Anahtar yoksa uygulama bu özellikleri sessizce atlar — temel oynatma
fonksiyonları etkilenmez.

---

## Kullanım

1. **İlk açılış**: Onboarding ekranında M3U URL veya Xtream credentials gir.
2. **Senkron**: Playlist eklendikten sonra ilk sync otomatik başlar; sonra her
   24 saatte bir arka planda + her 6 saatte bir açılış sırasında.
3. **Kanal izleme**: Sol alt navigasyondan "Kanallar" → kategori → kanal seç.
4. **Film/dizi izleme**: "Filmler" veya "Diziler" sekmesinden katalogda gez.
5. **Favoriler**: Bir öğenin yanındaki ⭐ ikonuna tıkla → "Favoriler"
   sekmesinden ulaş.
6. **Ayarlar**: Profil → Tema, Oynatıcı, Altyazı, Playlist Yönetimi.

---

## Disclaimer

- Genç IPTV **yalnızca bir oynatıcıdır**; içerik, kanal veya playlist sağlamaz.
- Kullandığın IPTV servisinin **yasal**lığından **sen** sorumlusun.
- Uygulama TMDB'nin API'sini kullanır ve bu hizmet için TMDB'ye atıf vermek
  şarttır: _"This product uses the TMDB API but is not endorsed or certified
  by TMDB."_
- Geliştirici, kullanılan playlist'lerin/Xtream hesaplarının yasallığı veya
  içeriği hakkında hiçbir sorumluluk kabul etmez.

---

## Katkı

PR'lara açığım. Büyük değişiklikler için önce issue açıp tartışalım.

Kod stili:
- Kotlin official style (`kotlin.code.style=official` zaten `gradle.properties`'te)
- Compose composable'lar PascalCase, fonksiyonlar camelCase
- Tüm UI metinleri Türkçe (i18n şu an yok)

---

## Lisans

MIT — detaylar [LICENSE](LICENSE) dosyasında.

---

## Teşekkürler

- [ExoPlayer / Media3](https://github.com/androidx/media)
- [Coil 3](https://github.com/coil-kt/coil)
- [TMDB](https://www.themoviedb.org/)
- [Instrument Serif](https://fonts.google.com/specimen/Instrument+Serif), [Geist](https://vercel.com/font), [Geist Mono](https://vercel.com/font) — Google Fonts
