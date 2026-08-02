# Yapay zekâ asistanları için notlar

Bu projenin kuralları tek bir yerde tutuluyor: **[CLAUDE.md](CLAUDE.md)**.

Hangi aracı kullanıyor olursan ol, kod yazmadan önce o dosyayı oku. Özellikle
sessizce kullanıcı verisini silen veya güncelleme zincirini koparan üç konu
orada anlatılıyor:

1. Room şeması değişiyorsa migration yazmak zorunludur — yoksa tüm kullanıcı
   verisi (IPTV playlist'i, kullanıcı adı, şifre dahil) sessizce silinir
2. Her yayında `versionCode` artmalı, `versionName` git etiketiyle eşleşmeli
3. İmzalama keystore'u asla değişmemeli
