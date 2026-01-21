# 🔐 CrafterAuth

Velocity proxy sunucuları için Discord 2FA entegrasyonlu modern Minecraft kimlik doğrulama eklentisi.

[![Version](https://img.shields.io/badge/versiyon-1.1-blue.svg)](https://github.com/yourusername/CrafterAuth)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Velocity](https://img.shields.io/badge/Velocity-3.x-green.svg)](https://velocitypowered.com/)
[![License](https://img.shields.io/badge/lisans-AGPL--3.0-red.svg)](LICENSE)

## ✨ Özellikler

### 🛡️ Güvenlik
- **BCrypt Şifre Hashleme** - Endüstri standardı şifre şifreleme
- **Discord 2FA Entegrasyonu** - Discord DM üzerinden iki faktörlü kimlik doğrulama
- **Brute Force Koruması** - Başarısız girişimlerden sonra otomatik IP yasaklama
- **Oturum Yönetimi** - Yapılandırılabilir oturum zaman aşımı
- **IP Sınırlama** - Aynı IP'den birden fazla hesap oluşturmayı engelleme

### 👥 Yetkili Yönetimi
- **Discord Tabanlı 2FA** - Yetkili üyeler Discord doğrulaması gerektirir
- **Her Girişte Doğrulama** - Her yetkili girişi şifre + 2FA onayı gerektirir
- **IP Takibi** - Giriş denemelerinin Discord onayıyla aynı IP'den yapıldığını doğrulama
- **Denetim Günlüğü** - Tüm yetkili kimlik doğrulamalarının CSV logları
- **Otomatik Oturum Temizleme** - Yetkililer her zaman şifre + 2FA gerektirir

### 🎮 Kullanıcı Deneyimi
- **Boss Bar Entegrasyonu** - Görsel geri sayım sayaçları
- **Özel Mesajlar** - Tamamen özelleştirilebilir Türkçe mesajlar
- **E-posta Kaydı** - İsteğe bağlı e-posta tabanlı kayıt
- **TOTP Desteği** - Geleneksel 2FA token desteği
- **Premium Hesap Desteği** - Mojang hesaplarıyla sorunsuz entegrasyon

### ⚙️ Teknik
- **Veritabanı Desteği** - MySQL, PostgreSQL, SQLite, H2, MongoDB
- **Crafter CMS Entegrasyonu** - Crafter platformu ile native entegrasyon
- **Çoklu Hash Desteği** - AuthMe, DBA, nLogin vb. eklentilerden geçiş
- **Dünya Özelleştirme** - WorldEdit schematic ile özel auth limbo dünyası
- **API Backend** - Harici entegrasyonlar için RESTful API

---

## 📦 Kurulum

### Gereksinimler
- Java 17 veya üzeri
- Velocity 3.x proxy sunucusu
- Discord Bot (2FA özelliği için)

### Adımlar

1. **En son sürümü indirin**
   ```bash
   # Releases'dan CrafterAuth-1.1.jar dosyasını indirin
   ```

2. **Plugins klasörüne yerleştirin**
   ```bash
   # Windows: plugins\CrafterAuth-1.1.jar
   # Linux: plugins/CrafterAuth-1.1.jar
   ```

3. **Sunucuyu başlatarak config dosyalarını oluşturun**
   ```bash
   # Velocity sunucusunu başlatın
   # Config dosyaları plugins/crafterauth/ klasöründe oluşturulacak
   ```

4. **Discord Bot'u yapılandırın** (İsteğe bağlı, 2FA için)
   - [Discord Developer Portal](https://discord.com/developers/applications)'da bir Discord botu oluşturun
   - Bot token'ını `config/discord.yml` dosyasına kopyalayın
   - Gerekli bot intent'lerini aktif edin (Server Members, Message Content)

5. **Sunucuyu yeniden başlatın**
   ```bash
   # Yapılandırmayı uygulamak için Velocity'i yeniden başlatın
   ```

---

## 🎯 Hızlı Başlangıç

### Temel Kurulum (Discord Olmadan)
```yaml
# config/main.yml
purge-cache-millis: 0          # Her zaman giriş gereksin (0 = devre dışı)
ip-limit-registrations: 3      # IP başına max 3 hesap
ip-limit-valid-time: 0         # Zaman sınırı yok (0 = devre dışı)
```

### Discord 2FA Kurulumu
```yaml
# config/discord.yml
token: "DISCORD_BOT_TOKENINIZ"
staff-auth-timeout: 60         # Yetkili onayı için 60 saniye
```

### Yetkili Ekleme
```bash
/crafterauth staff add <kullaniciadi> <discord_id>
```

---

## 📝 Komutlar

### Oyuncu Komutları
| Komut | Açıklama | İzin |
|-------|----------|------|
| `/login <şifre>` | Hesaba giriş yap | - |
| `/register <şifre> <şifre>` | Yeni hesap oluştur | - |
| `/changepassword <eski> <yeni>` | Şifreyi değiştir | `limboauth.commands.changepassword` |
| `/2fa enable <şifre>` | TOTP 2FA'yı aktif et | `limboauth.commands.totp` |
| `/2fa disable <kod>` | TOTP 2FA'yı kapat | `limboauth.commands.totp` |
| `/unregister <şifre> confirm` | Hesabı sil | `limboauth.commands.unregister` |

### Yetkili Komutları
| Komut | Açıklama | İzin |
|-------|----------|------|
| `/crafterauth staff add <kullanıcı> <discord_id>` | Yetkili ekle | `limboauth.admin.staff` |
| `/crafterauth staff remove <kullanıcı>` | Yetkili çıkar | `limboauth.admin.staff` |
| `/crafterauth staff list` | Tüm yetkilileri listele | `limboauth.admin.staff` |
| `/crafterauth staff reload` | Yetkili veritabanını yeniden yükle | `limboauth.admin.staff` |

### Admin Komutları
| Komut | Açıklama | İzin |
|-------|----------|------|
| `/limboauth forcelogin <kullanıcı>` | Oyuncuyu zorla giriş yaptır | `limboauth.admin.forcelogin` |
| `/limboauth forceregister <kullanıcı> <şifre>` | Zorla kayıt et | `limboauth.admin.forceregister` |
| `/limboauth forceunregister <kullanıcı>` | Zorla kaydı sil | `limboauth.admin.forceunregister` |
| `/limboauth forcechangepassword <kullanıcı> <şifre>` | Zorla şifre değiştir | `limboauth.admin.forcechangepassword` |
| `/limboauth reload` | Yapılandırmayı yeniden yükle | `limboauth.admin.reload` |

---

## ⚙️ Yapılandırma

### Ana Yapılandırma (`config/main.yml`)

#### Kimlik Doğrulama
```yaml
auth-time: 60000              # Kimlik doğrulama süresi limiti (ms)
login-attempts: 3             # Atılmadan önceki max giriş denemesi
min-password-length: 8        # Minimum şifre uzunluğu
max-password-length: 71       # Maximum şifre uzunluğu (BCrypt limiti)
check-password-strength: true # Şifre gücü kontrolünü aktif et
```

#### Oturum Yönetimi
```yaml
purge-cache-millis: 0         # Oturum zaman aşımı (0 = her zaman giriş gereksin)
                              # 3600000 = 1 saat
                              # 7200000 = 2 saat
```

#### Güvenlik
```yaml
bruteforce-max-attempts: 10           # IP yasaklamadan önceki max başarısız deneme
purge-bruteforce-cache-millis: 3600000 # Brute force yasak süresi (1 saat)

ip-limit-registrations: 3     # IP başına max hesap sayısı
ip-limit-valid-time: 0        # IP limit süresi (0 = sınırsız)
```

#### Boss Bar
```yaml
enable-bossbar: true
bossbar-color: BLUE          # PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
bossbar-overlay: NOTCHED_20  # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20
```

### Discord Yapılandırması (`config/discord.yml`)

```yaml
bot:
  token: "DISCORD_BOT_TOKENINIZ"
  enabled: true

staff-auth:
  timeout-seconds: 60
  failsafe-mode: true          # Discord çevrimdışıysa girişe izin ver
  
messages:
  title: "🔐 CrafterAuth Doğrulama"
  description: "Bir yetkili giriş denemesi algılandı."
  approve-button: "✅ Onayla"
  deny-button: "❌ Reddet"
  timeout-message: "⏱️ Doğrulama zaman aşımına uğradı"
  approved-message: "✅ Giriş onaylandı"
  denied-message: "❌ Giriş reddedildi"
```

### Yetkili Veritabanı (`config/staff.yml`)

```yaml
staff:
  - username: "OyuncuAdi"
    uuid: "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx"
    discord_id: "123456789012345678"
    added_at: "2025-12-24T12:00:00Z"
```

---

## 🔧 Gelişmiş Özellikler

### Veritabanı Yapılandırması

#### MySQL/MariaDB
```yaml
database:
  storage-type: mysql
  hostname: "localhost:3306"
  user: "crafterauth"
  password: "guvenli_sifre"
  database: "limboauth"
  connection-parameters: "?autoReconnect=true&useSSL=false"
```

#### PostgreSQL
```yaml
database:
  storage-type: postgresql
  hostname: "localhost:5432"
  user: "crafterauth"
  password: "guvenli_sifre"
  database: "limboauth"
```

#### Crafter CMS Entegrasyonu
```yaml
database:
  storage-type: crafter
  api-url: "https://api.crafter.net.tr"
  license-key: "api-anahtariniz"
  api-secret: "api-secretiniz"
```

### Geçiş Desteği

Diğer auth eklentilerinden geçiş yapın:
```yaml
migration-hash: AUTHME  # AUTHME, SHA256, SHA512, MD5, ARGON2, vb.
```

Desteklenen formatlar:
- AuthMe (SHA256)
- DBA (SHA512)
- nLogin (SHA512)
- NexAuth (SHA256/SHA512)
- JPremium (SHA256)
- Plain MD5
- Argon2

---

## 📊 Denetim Günlüğü

Yetkili kimlik doğrulama logları CSV formatında kaydedilir:

**Konum:** `plugins/crafterauth/logs/staff_audit.csv`

**Format:**
```csv
timestamp,event_type,username,discord_id,ip_address,status,details
2025-12-24 12:00:00,LOGIN_ATTEMPT,OyuncuAdi,123456789,192.168.1.1,SUCCESS,Discord üzerinden onaylandı
2025-12-24 12:05:00,LOGIN_TIMEOUT,OyuncuAdi,123456789,192.168.1.1,FAILED,Doğrulama zaman aşımı (60s)
```

---

## 🔐 Discord 2FA Akışı

### Yetkili Üyeler İçin

1. **Giriş Yap** - Normal şekilde şifre gir
   ```
   /login GuvenliSifrem123
   ```

2. **Boss Bar Değişir** - Görsel geri bildirim
   - ❌ "Giriş yapmak için X saniye kaldı" kaybolur
   - ✅ "Discord doğrulama için kalan süre: 60 saniye" görünür

3. **Discord DM'i Kontrol Et** - Bot doğrulama mesajı gönderir
   ```
   🔐 CrafterAuth Doğrulama
   
   Kullanıcı Adı: OyuncuAdi
   IP Adresi: 192.168.1.1
   Zaman: 2025-12-24 12:00:00
   
   [✅ Onayla]  [❌ Reddet]
   ```

4. **Onayla'ya Tıkla** - Giriş tamamlanır
   - ✅ Oyuncu sunucuya girer
   - 📝 Olay denetim loguna kaydedilir

5. **Zaman Aşımı (60s)** - Otomatik atılma
   - ❌ Oyuncu sunucudan atılır
   - 📝 Zaman aşımı denetim loguna kaydedilir

### Oturum Yönetimi

- **Yetkililer:** Her giriş şifre + Discord 2FA gerektirir
- **Normal Oyuncular:** Oturum tabanlı (yapılandırılabilir zaman aşımı)

---

## 🎨 Özelleştirme

### Özel Mesajlar

`config/main.yml` dosyasında mesajları düzenleyin:

```yaml
strings:
  login: "{PRFX} &b/login <şifre>&7 ile giriş yapın"
  register: "{PRFX} &b/register <şifre> <şifre>&7 ile kayıt olun"
  login-successful: "{PRFX} &aBaşarıyla giriş yaptınız!"
  
  # Discord 2FA özel mesajları
  discord-check-dm: "{PRFX} &e&lDiscord DM'inizi kontrol edin!"
  discord-verify-prompt: "{PRFX} &7Discord doğrulamasına yanıt verin"
  discord-timeout: "{PRFX} &7Doğrulama için 60 saniyeniz var"
```

### Özel Auth Dünyası

1. Bir WorldEdit schematic oluşturun
2. `plugins/crafterauth/auth.schem` konumuna yerleştirin
3. `config/main.yml` dosyasında yapılandırın:
```yaml
world-file-type: WORLDEDIT_SCHEM  # SCHEMATIC, STRUCTURE, WORLDEDIT_SCHEM
world-file-path: "auth.schem"
dimension: THE_END                # OVERWORLD, NETHER, THE_END
game-mode: ADVENTURE              # ADVENTURE, CREATIVE, SURVIVAL, SPECTATOR
```

---

## 🐛 Sorun Giderme

### Discord Bot Yanıt Vermiyor

**Bot token'ını kontrol edin:**
```yaml
# config/discord.yml
token: "GERÇEK_TOKENINIZI_GIRIN"
```

**Bot izinlerini kontrol edin:**
- ✅ Mesajları Oku
- ✅ Mesaj Gönder
- ✅ Slash Komutlarını Kullan
- ✅ Link Yerleştir

**Konsol hatalarını kontrol edin:**
```bash
# Logs klasöründeki latest.log dosyasını kontrol edin
```

### Yetkili DM Almıyor

**Discord ID'yi doğrulayın:**
```bash
/crafterauth staff list
# Discord ID'nin doğru olduğunu kontrol edin (18 haneli sayı)
```

**DM'leri aktif edin:**
- Kullanıcı sunucu üyelerinden DM almasına izin vermelidir
- Gizlilik Ayarları → Sunucu üyelerinden direkt mesaj almasına izin ver

### Boss Bar Değişmiyor

**Config'i kontrol edin:**
```yaml
enable-bossbar: true  # Aktif olmalı
```

**Eklentiyi yeniden yükleyin:**
```bash
/limboauth reload
```

### Oturum Temizlenmiyor

**Yetkililer için (her zaman giriş yapmalı):**
```bash
# Yetkili veritabanını kontrol edin
/crafterauth staff list

# Kullanıcı adının listede olduğunu doğrulayın
# Yetkililer oturum cache'ini atlar
```

**Normal oyuncular için:**
```yaml
purge-cache-millis: 0  # Her zaman giriş gerektirmek için 0 yapın
```

---

## 🤝 Katkıda Bulunma

Katkılar memnuniyetle karşılanır! Lütfen:

1. Repository'yi fork edin
2. Özellik dalı oluşturun (`git checkout -b feature/harika-ozellik`)
3. Değişikliklerinizi commit edin (`git commit -m 'Harika özellik eklendi'`)
4. Dalınıza push edin (`git push origin feature/harika-ozellik`)
5. Pull Request açın

---

## 📄 Lisans

Bu proje AGPL-3.0 Lisansı altında lisanslanmıştır - detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 🙏 Teşekkürler

- **Temel Alınan:** [LimboAuth by Elytrium](https://github.com/Elytrium/LimboAuth)
- **Discord Entegrasyonu:** JDA (Java Discord API)
- **Velocity API:** [PaperMC Velocity](https://velocitypowered.com/)

---

## 📞 Destek

- **Sorunlar:** [GitHub Issues](https://github.com/yourusername/CrafterAuth/issues)
- **Discord:** [Discord Sunucumuza Katılın](https://discord.gg/crafter)
- **Wiki:** [Dokümantasyon](https://wiki.crafter.net.tr)

---

<div align="center">

**Crafter Network için ❤️ ile yapıldı**

[Website](https://crafter.net.tr) • [Discord](https://discord.gg/crafter) • [Wiki](https://wiki.crafter.net.tr)

</div>

