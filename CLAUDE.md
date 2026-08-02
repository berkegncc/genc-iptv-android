# Genç IPTV Player — yapay zekâ asistanları için notlar

Bu dosya, bu projede kod yazan yapay zekâ araçları içindir. Projenin sahibi
yazılım geliştirmiyor; değişiklikleri asistanlara yaptırıyor ve kodu satır satır
denetleyemiyor. Aşağıdaki üç konu **sessizce** kullanıcı verisini siler veya
güncelleme zincirini koparır. Hiçbiri derleme hatası vermez, testte de
görünmez — bu yüzden her değişiklikte bilerek kontrol edilmeleri gerekir.

---

## 1. Veritabanı şeması değişiyorsa migration ZORUNLU

Room veritabanı şu an **sürüm 4** (`data/source/local/AppDatabase.kt`).

Bir `@Entity`'ye sütun eklersen, çıkarırsan, adını veya tipini değiştirirsen ya
da yeni tablo eklersen, şu üçünü **birlikte** yap:

1. `AppDatabase.kt` içindeki `version` değerini bir artır
2. `data/di/DatabaseModule.kt` içine `Migration(eski, yeni)` yaz ve
   `addMigrations(...)` çağrısına ekle
3. Derlemeden sonra oluşan `app/schemas/…/<yeni sürüm>.json` dosyasını
   commit'e dahil et

**Yapmazsan ne olur:** `DatabaseModule.kt` içinde bilinçli olarak
`fallbackToDestructiveMigration(dropAllTables = true)` duruyor. Room bir geçiş
yolu bulamazsa uygulama çökmez — bunun yerine **bütün tabloları siler**.
Kullanıcı IPTV playlist'ini, sunucu adresini, kullanıcı adını ve şifresini
kaybeder ve hepsini elle yeniden girmek zorunda kalır; bu bilgiler
`PlaylistEntity` içinde tutuluyor.

Bu davranış bilinçli bir tercihtir. Yan yüklenen bir uygulamada açılışta çöken
bir sürüm kullanıcıyı tamamen kilitler ve geri dönüş yolu yoktur; veri kaybı en
azından telafi edilebilir. **Bu satırı "daha güvenli" yapmak için değiştirme** —
doğru çözüm migration'ı yazmaktır.

## 2. Her yayında `versionCode` artmalı

`app/build.gradle.kts`:

- **`versionCode`** her yayında artmalı. Android güncelleme kararını
  `versionName`'e göre değil buna göre verir; eşit veya küçükse kurulumu
  `INSTALL_FAILED_VERSION_DOWNGRADE` ile reddeder ve kullanıcı güncellemeyi
  alamaz.
- **`versionName`** git etiketiyle birebir aynı olmalı ve üç haneli semver
  kullanmalı: `1.3.0` → etiket `v1.3.0`. Uygulama içi güncelleme sistemi bu
  ikisini karşılaştırıyor (`core/util/VersionComparator.kt`).

## 3. Keystore asla değişmemeli

Yayınlanan APK'lar `genciptv-release.jks` ile imzalanıyor (yolu ve şifreleri
`keystore.properties` içinde, git'e dahil değil). Android farklı bir sertifikayla
imzalanmış APK'yı güncelleme olarak kabul etmez. Anahtar kaybolur veya
değişirse **tüm kullanıcılar** uygulamayı kaldırıp yeniden kurmak ve verilerini
kaybetmek zorunda kalır.

Debug derlemesi ayrı bir anahtarla (`CN=Android Debug`) imzalanır. Bu yüzden
telefonda debug sürüm kuruluyken GitHub'daki release APK'sı kurulamaz; bu bir
hata değil, beklenen davranıştır. Geliştirme sırasında güncelleme akışını test
edecekseniz release derlemesini kurun.

---

## Güncelleme sistemi nerede

| Dosya | Görevi |
|---|---|
| `data/source/github/` | GitHub Releases API'si (`berkegncc/genc-iptv-android`) |
| `data/repository/UpdateRepository.kt` | Kontrol (24 saat aralıklı) + APK indirme |
| `core/util/VersionComparator.kt` | Semver karşılaştırma; farklı uzunlukları tolere eder |
| `core/util/ApkInstaller.kt` | İzin, doğrulama, sistem installer'ına devir |
| `feature/update/` | Durum, ViewModel, dialog |

Güncelleme kontrolü **sessiz bir işlemdir**: rate limit, çevrimdışı, DNS —
hepsi sessizce "güncelleme yok"a düşer, kullanıcıya asla hata gösterilmez.
Bu davranışı bozma.

## 4. Gizli bilgiler asla depoya girmez

`local.properties` (TMDB anahtarı), `keystore.properties` (imza şifreleri) ve
`genciptv-release.jks` gitignore'da ve öyle kalmalı. Bunları commit'e ekleme,
içeriklerini başka bir dosyaya kopyalama, örnek dosyalara gerçek değer yazma.

TMDB anahtarı `BuildConfig` üzerinden APK'ya gömülebiliyor
(`app/build.gradle.kts` içindeki `buildConfigField`). Bu yalnızca yerel
geliştirme kolaylığı içindir — **yayınlanan APK'lar anahtarsız derlenmelidir**,
çünkü APK'ya gömülen anahtar indiren herkes tarafından çıkarılabilir ve tüm
kurulumlar tek kotayı paylaşır. Kullanıcılar kendi anahtarlarını
Profil → Hakkında → TMDB API Anahtarı üzerinden giriyor; bu değer DataStore'da
saklanıyor ve `TmdbRepository.resolveApiKey()` içinde BuildConfig'in önüne
geçiyor.

## Genel çalışma notları

- Mimari: Jetpack Compose M3, MVVM, Hilt, Retrofit + kotlinx.serialization,
  Room, DataStore, Coroutines/Flow
- Kullanıcıya görünen tüm metinler Türkçe
- Tasarım dili: başlıklarda Instrument Serif, gövdede Geist, teknik/sayısal
  metinlerde Geist Mono; renkler `LocalAccentPalette` ve `designsystem` üzerinden,
  hardcoded renk yazma
- **Room sorgularında `id` sütununa göre sıralama yapma.** `id` TEXT'tir, SQLite
  onu sözlüksel karşılaştırır ve `"999"` > `"3500"` çıkar. Sayısal sıralama için
  `providerId` / `addedAt` kullan.
- Birim testleri bu makinede proje yolunda çalışmıyor (yol `Masaüstü` içeriyor,
  Gradle test worker'ı sınıfları yükleyemiyor). Test çalıştırmak gerekirse
  projeyi ASCII bir yola kopyalayıp orada koştur.
