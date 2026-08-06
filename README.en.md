# Genç IPTV Player

[Türkçe](README.md) · **English**

A modern, restrained, typography-led IPTV player for Android.

Built to watch live channels, movies and series in one app, from **M3U** or
**Xtream Codes** playlists.

> ⚠️ **Genç IPTV Player is a media player** — it provides no content and no
> playlists. You need your own M3U URL or Xtream credentials to use it.

---

## Features

### Live TV
- M3U and Xtream Codes playlists (keep several, one active)
- Browse channels by category
- Channel logos and what is on right now
- Programme guide (XMLTV) — by day, by channel
- Pull to refresh

### Movies and series
- Catalogue browsing by category
- Season and episode navigation
- **Continue watching** — several episodes of one series collapse into a single
  row, and resume where you left off
- "Next episode" in the player, crossing season boundaries
- Search covers the whole tab; the category filter steps aside while you type

### Player
- ExoPlayer (HLS, DASH, MPEG-TS, MP4)
- Picture-in-picture
- Subtitle styling — 13 settings: typeface, colour, edge, position, size
- Aspect ratio (Original / 16:9 / 21:9 / Stretch / Fit)
- Playback speed for movies and series
- Three-stage stream fallback (HLS → progressive → .ts)
- Custom User-Agent and preferred audio language under advanced settings

### Interface
- Bilingual: English and Turkish. Follows your system language, or pick one
  under Profil → Dil
- A splash sequence where light travels along the mark
- 8 accent palettes, light/dark/system theme
- Tablet layout — list and detail side by side on wide screens, with a nav rail
- Favourites (channels / movies / series)
- Search across everything
- Recently watched channels on the home screen

### Updates and data
- In-app updates — checks GitHub Releases, downloads and installs
- **Content refresh** setting: Wi-Fi only, or mobile data as well. When a
  refresh is held back waiting for Wi-Fi, the home screen says so
- Feedback from inside the app

### Architecture
- Jetpack Compose (Material 3)
- MVVM + Hilt
- Room (SQLite) + DataStore
- Media3 / ExoPlayer
- Retrofit + kotlinx.serialization
- Coil 3
- WorkManager
- Coroutines + Flow

---

## Installing

### From a release (recommended)

Download the newest APK from [Releases](../../releases) and open it. You may
need to allow installing from unknown sources.

If the app is already installed it updates in place and your data is kept.
After that, updates arrive from inside the app.

### Building from source

Requirements: **Android Studio** (Hedgehog 2023.1.1+), **Android SDK 36**,
**JDK 17**. Minimum SDK 24 (Android 7.0).

```bash
git clone https://github.com/berkegncc/genc-iptv-android.git
```

Open in Android Studio → sync Gradle → Run. Or from a terminal:

```bash
./gradlew :app:assembleDebug
```

### TMDB API key (optional)

The app works without TMDB. Nearly all artwork already comes from your
provider; TMDB is used for exactly two things:

- **Cast photos** on movie and series detail pages
- A fallback poster when the provider ships none

**Published APKs contain no key.** Baking one in would let anyone who downloads
the APK extract it, and every install would then share a single quota. To use
your own:

1. Get a free key at
   [themoviedb.org/settings/api](https://www.themoviedb.org/settings/api)
2. Go to **Profil → Hakkında → TMDB API Anahtarı**
3. Paste and save

The key is stored on your device only and is never sent anywhere. Remove it and
those two features switch off; everything else carries on.

> Building from source, you can put `TMDB_API_KEY=...` in `local.properties` —
> that file is gitignored. **Never commit your key.** The in-app setting always
> takes precedence over a compiled-in one.

---

## Using it

1. **First run** — enter your M3U URL or Xtream credentials.
2. **Syncing** — the first sync starts when a playlist is added. After that,
   once a day in the background, plus on app open when the catalogue is more
   than six hours old. Both honour your content-refresh setting; a manual sync
   always runs, on any connection.
3. **Channels** — bottom navigation → Kanallar → category → channel.
4. **Movies and series** — the Filmler or Diziler tab.
5. **Favourites** — tap the star next to an item.
6. **Settings** — Profil: theme, language, player, subtitles, playlists.

---

## Cutting a release

Releases are made by hand, and the version numbers matter — the in-app updater
reads them.

**1. Set the version** (`app/build.gradle.kts` → `defaultConfig`)

- `versionCode` — must increase on every release. Android decides "is this an
  update?" from this, not from `versionName`; leave it alone and the install is
  refused with `INSTALL_FAILED_VERSION_DOWNGRADE`. A published `versionCode` can
  never be reused.
- `versionName` — three-part semver (`1.3.0`). The git tag is that string with a
  `v` in front.

**2. Build a signed, key-free APK**

```bash
TMDB_API_KEY= ./gradlew :app:assembleRelease
```

On PowerShell: `$env:TMDB_API_KEY=""; ./gradlew :app:assembleRelease`

The environment variable takes precedence over `local.properties` — but do not
rely on it alone, since a Gradle daemon already running can carry the old
environment. The check that actually tells you is the build output: if you see
**"WARNING: this release APK has a TMDB API key compiled in"**, the key is in
there and the APK must not be published. No warning means it is clean. The
certain fix is to blank the line in `local.properties` for the build.

Check the signature:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

You want `CN=Genc IPTV`. If you see `CN=Android Debug`, **do not publish it** —
it will not install for any existing user.

**3. Create the release**

Tag it `v1.3.0`, attach the APK. The updater reads the `latest` release and
downloads the **first asset ending in `.apk`**; anything marked pre-release is
skipped.

### Two things that bite

- **The keystore must never change.** If `genciptv-release.jks` is lost or
  replaced, every user has to uninstall and reinstall, losing their data.
- **Write a migration if the database schema changed.** When Room finds no path
  between versions the app does not crash — it drops every table, and the user
  loses their playlist, server address, username and password.

---

## Contributing

Pull requests welcome. For anything large, open an issue first so we can talk
it through.

- Official Kotlin style (already set in `gradle.properties`)
- Composables PascalCase, functions camelCase
- **No user-facing text in code.** The app is bilingual: `res/values/` is
  English (the default every unlisted language falls back to) and
  `res/values-tr/` is Turkish. A new key must go in **both** — add it only to
  `values/` and Turkish users see English there, silently.
  `tools/check_locales.py` verifies both locales define the same keys.
- Never build a sentence by concatenation; use `%1$s` placeholders. Turkish and
  English word order differ.

---

## Disclaimer

- **Genç IPTV Player is only a media player.** It does **not** provide, host,
  bundle, stream, or distribute any channels, content, or playlists.
- The app ships with **no** preloaded sources. You must supply your own **M3U
  URL** or **Xtream Codes** credentials to use it.
- **You are solely responsible** for the content you access and for the
  legality of any IPTV service, playlist, or credentials you use with this app,
  in your own jurisdiction.
- The developer accepts **no liability** for how the app is used or for the
  legality or content of user-supplied sources.
- This product uses the TMDB API but is not endorsed or certified by TMDB.

To report a security issue, see [SECURITY.md](SECURITY.md).

---

## Licence

MIT — see [LICENSE](LICENSE).

---

## Thanks

- [ExoPlayer / Media3](https://github.com/androidx/media)
- [Coil 3](https://github.com/coil-kt/coil)
- [TMDB](https://www.themoviedb.org/)
- [Instrument Serif](https://fonts.google.com/specimen/Instrument+Serif) ·
  [Geist and Geist Mono](https://vercel.com/font)
