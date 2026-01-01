# CrafterAuth Discord Özelliği - Task Listesi

## Amaç
CrafterAuth eklentisinin Discord bot entegrasyonu ile ilgili yapılan işleri ve eksikleri takip etmek için bu dosya oluşturulmuştur.

## Discord Özelliği Neler Yapıyor?
- Discord botunu başlatır ve token ile kimlik doğrulaması yapar.
- Belirli kullanıcıya (ID ile) DM (özel mesaj) gönderebilir.
- 2FA (iki faktörlü doğrulama) kodunu embed ve buton ile gönderir.
- Kullanıcı butona tıkladığında, doğrulama eventini dinler ve sunucuya iletir.
- Botun başlatılması ve kapatılması için fonksiyonlar içerir.

---

## Task Listesi

### Temel Discord Bot Fonksiyonları
- [x] Discord botunu başlatma ve durdurma
- [x] DM ile mesaj gönderme
- [x] 2FA kodunu embed ve buton ile gönderme
- [x] Buton tıklama eventini dinleme ve doğrulama

---

### Staff Yönetimi
- [x] Konsol üzerinden `staff add <discord_id> <oyun_ismi>` komutu
  - Discord ID ve oyun içi ismi birlikte kaydetme
  - UUID otomatik resolve edilmesi (ilk loginde otomatik)
  - Dosya veya veritabanında saklama (discord.yml)
  - Başarılı işlemde onay mesajı
  - Hatalı girişlerde açıklayıcı uyarı
- [x] `staff remove <oyun_ismi | discord_id>` komutu
- [x] `staff list` komutu (online/offline bilgisi ile)
- [x] Staff verisini reload edebilme (restart gerektirmeden)

---

### Giriş & Doğrulama Akışı
- [x] Staff login algılama (UUID bazlı)
- [x] Staff girişlerinde Discord doğrulamasını zorunlu kılma
- [x] Login isteği için tek kullanımlık request ID üretimi
- [x] Login isteği timeout (örn. 60 saniye)
- [x] Timeout sonrası otomatik kick
- [x] Aynı login isteğinin tekrar kullanılmasını engelleme (replay protection)

---

### Güvenlik
- [x] IP + login context eşleşme kontrolü
- [x] Aynı Discord hesabından paralel login engelleme
- [ ] Fail-safe modu (Discord offline iken davranış tanımı)
- [ ] Token güvenliği
  - Config’te plaintext yerine env destekleme
  - Token rotate / reset mekanizması

---

### Rate Limit & Stabilite
- [x] Discord rate limit yönetimi
- [x] Otomatik reconnect (Gateway disconnect)
- [x] Graceful shutdown (bot kapatılırken açık session temizleme)
- [x] Thread-safe event handling
- [x] Async işlem hatalarında botun çökmesini engelleme

---

### Loglama & İzleme
- [ ] Detaylı audit log
  - Kim doğruladı
  - Ne zaman
  - IP / ülke bilgisi
- [ ] Şüpheli login denemeleri için ayrı log kanalı
- [ ] Discord log channel entegrasyonu
- [ ] Debug mode (staff login neden reddedildi?)

---

### Konfigürasyon
- [x] Discord doğrulamasını opsiyonel yapabilme
- [x] Timeout sürelerini config'ten ayarlayabilme (staff_login_timeout)
- [ ] Mesaj / embed metinlerini özelleştirme

---

### Test & Dokümantasyon
- [ ] Unit testler (token, request, timeout)
- [ ] Discord interaction testleri
- [ ] Load test (çoklu staff login)
- [ ] Kurulum dokümantasyonu
- [ ] Güvenlik ve best-practice rehberi

---

## Notlar
- Discord doğrulama sistemi **sadece staff hesapları** için tasarlanmıştır.
- Amaç, staff account hijack riskini minimuma indirmektir.
- Sistem, klasik Auth eklentilerinden farklı olarak **real-time onay** mantığı ile çalışır.
---

## Tamamlanan Özellikler (23 Aralık 2025)

### Yeni Eklenen Sınıflar
1. **StaffLoginRequest.java**
   - Tek kullanımlık login request yönetimi
   - Timeout scheduler ile otomatik zaman aşımı
   - Replay protection mekanizması
   - Request ID ile unique tanımlama

2. **StaffAuthenticationHandler.java**
   - Discord doğrulama akışını koordine eder
   - Staff login algılama ve Discord mesajı gönderimi
   - Button interaction listener kaydı
   - Embed mesaj oluşturma ve gönderme
   - Onay/reddetme callback'leri

### Güncellemeler
1. **StaffDatabase.java**
   - UUID field eklendi (otomatik resolve)
   - `setUUID(String username, UUID uuid)` metodu
   - `getDiscordId(UUID uuid)` metodu
   - `isStaff(UUID)` ve `isStaff(String)` overload
   - `reload()` metodu (hot-reload)

2. **StaffCommand.java**
   - `/crafterauth staff reload` komutu eklendi
   - `/crafterauth staff list` UUID ve online/offline gösterimi
   - Renkli konsol çıktısı (yeşil: online, kırmızı: offline)

3. **DiscordBot.java**
   - Otomatik reconnect logic
   - Rate limit yönetimi
   - Connection status monitoring (`isConnected()`, `isReconnecting()`)
   - Session event handlers (Ready, Disconnect, Recreate)
   - Logger injection desteği
   - Graceful shutdown (10 saniye await)

4. **LimboAuth.java**
   - StaffAuthenticationHandler entegrasyonu
   - Discord config'te staff_login_timeout eklendi
   - Duplicate bot initialization temizlendi
   - Shutdown lifecycle güncellendi

### Build Durumu
- ✅ Gradle build başarılı
- ✅ License headers eklendi (AGPL-3.0)
- ⚠️ 687 checkstyle uyarısı (indentation, whitespace)
- ✅ Deployable JAR oluşturuldu