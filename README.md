# 🔐 CrafterAuth

**CrafterAuth**, Velocity proxy sunucuları için geliştirilmiş, [Crafter CMS](https://crafter.net.tr) platformuyla native entegre çalışan modern bir Minecraft kimlik doğrulama eklentisidir.

[LimboAuth by Elytrium](https://github.com/Elytrium/LimboAuth) temel alınarak geliştirilmiştir.

[![Version](https://img.shields.io/badge/versiyon-1.1-blue.svg)](https://github.com/Crafter-CMS/CrafterAuth)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://www.oracle.com/java/)
[![Velocity](https://img.shields.io/badge/Velocity-3.x-green.svg)](https://velocitypowered.com/)
[![License](https://img.shields.io/badge/lisans-AGPL--3.0-red.svg)](LICENSE)

---

## ✨ Özellikler

### 🛡️ Güvenlik
- **BCrypt Şifre Hashleme** — Endüstri standardı şifre şifreleme
- **Brute Force Koruması** — Başarısız girişimlerden sonra otomatik IP yasaklama
- **Oturum Yönetimi** — Yapılandırılabilir oturum önbelleği
- **IP Sınırlama** — Aynı IP'den birden fazla hesap oluşturmayı engelleme

### 🌐 Crafter CMS Entegrasyonu
- **Native API Desteği** — Crafter platformuyla doğrudan entegrasyon
- **Merkezi Kullanıcı Yönetimi** — Tüm hesaplar Crafter CMS panelinde görünür
- **E-posta Kaydı** — Kullanıcılar kayıt sırasında e-posta girer
- **Otomatik Senkronizasyon** — Kayıt ve giriş işlemleri otomatik API'ye iletilir

### 🎮 Kullanıcı Deneyimi
- **Boss Bar Entegrasyonu** — Görsel geri sayım
- **Özelleştirilebilir Mesajlar** — Tüm mesajlar config üzerinden düzenlenebilir
- **TOTP / 2FA Desteği** — Google Authenticator ile uyumlu
- **Premium Hesap Desteği** — Mojang hesaplarıyla sorunsuz entegrasyon
- **Yetkili 2FA** — Yetkili oyuncular için ekstra güvenlik katmanı

### ⚙️ Teknik
- **Çoklu Veritabanı Desteği** — MySQL, PostgreSQL, SQLite, H2, MongoDB
- **Çoklu Hash Desteği** — AuthMe, DBA, nLogin vb. eklentilerden geçiş
- **Dünya Özelleştirme** — WorldEdit schematic ile özel auth limbo dünyası
- **Geyser / Floodgate Desteği** — Bedrock oyuncuları için otomatik atlama

---

## 📦 Kurulum

### Gereksinimler
- Java 17 veya üzeri
- Velocity 3.x proxy sunucusu
- [Crafter CMS](https://crafter.net.tr) hesabı ve API anahtarı

### Adımlar

1. **[Releases](https://github.com/Crafter-CMS/CrafterAuth/releases) sayfasından son JAR'ı indirin**

2. **Velocity `plugins/` klasörüne yerleştirin**

3. **Sunucuyu başlatarak config dosyalarını oluşturun**
   ```
   plugins/crafterauth/config.yml
   ```

4. **Config'e Crafter CMS bilgilerini girin** (`plugins/crafterauth/config.yml`):
   ```yaml
   database:
     storage-type: CRAFTER
     api-url: "https://api.crafter.net.tr"
     license-key: "lisans-anahtariniz"
     api-secret: "api-secretiniz"
   ```

5. **Sunucuyu yeniden başlatın**

## ⚙️ Yapılandırma

### Ana Yapılandırma (`config.yml`)

#### Crafter CMS
```yaml
CRAFTER-CMS:
  ENABLED: true
  API_URL: "https://api.crafter.net.tr"
  LICENSE_KEY: "lisans-anahtariniz"
  API_SECRET: "api-secretiniz"
```

#### Kimlik Doğrulama
```yaml
AUTH-TIME: 60000              # Kimlik doğrulama süresi limiti (ms)
LOGIN-ATTEMPTS: 3             # Atılmadan önceki max giriş denemesi
MIN-PASSWORD-LENGTH: 8        # Minimum şifre uzunluğu
MAX-PASSWORD-LENGTH: 71       # Maximum şifre uzunluğu (BCrypt limiti)
CHECK-PASSWORD-STRENGTH: true # Şifre gücü kontrolü
```

#### Oturum Yönetimi
```yaml
PURGE-CACHE-MILLIS: 0         # 0 = her girişte şifre sor
                               # 3600000 = 1 saat oturum önbelleği
```

#### Güvenlik
```yaml
BRUTEFORCE-MAX-ATTEMPTS: 10           # IP yasaklamadan önceki max deneme
PURGE-BRUTEFORCE-CACHE-MILLIS: 3600000 # Brute force yasak süresi (1 saat)

IP-LIMIT-REGISTRATIONS: 3     # IP başına max hesap sayısı
IP-LIMIT-VALID-TIME: 0        # IP limit süresi (0 = sınırsız)
```

#### Boss Bar
```yaml
ENABLE-BOSSBAR: true
BOSSBAR-COLOR: BLUE          # PINK, BLUE, RED, GREEN, YELLOW, PURPLE, WHITE
BOSSBAR-OVERLAY: NOTCHED_20  # PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20

---

## 🔧 Özel Auth Dünyası

Zaten Dünya otomatik olarak geliyor ama siz kendiniz isterseniz 
1. WorldEdit schematic oluşturun
2. `plugins/crafterauth/auth.schem` konumuna yerleştirin
3. Config'de yapılandırın:
```yaml
WORLD-FILE-TYPE: WORLDEDIT_SCHEM  # SCHEMATIC, STRUCTURE, WORLDEDIT_SCHEM
WORLD-FILE-PATH: "auth.schem"
DIMENSION: THE_END                # OVERWORLD, NETHER, THE_END
GAME-MODE: ADVENTURE              # ADVENTURE, CREATIVE, SURVIVAL, SPECTATOR

```

## 📄 Lisans

Bu proje **AGPL-3.0** lisansı altındadır. Detaylar için [LICENSE](LICENSE) dosyasına bakın.

---

## 🙏 Teşekkürler

- **Temel Alınan:** [LimboAuth by Elytrium](https://github.com/Elytrium/LimboAuth)
- **Velocity API:** [PaperMC](https://velocitypowered.com/)

---

<div align="center">

**[Reliva Network](https://reliva.network) için ❤️ ile yapıldı**
**[Crafter](https://crafter.net.tr) için ❤️ ile yapıldı**

[Website](https://crafter.net.tr) • [Discord](https://discord.gg/crafter) • [GitHub Issues](https://github.com/Crafter-CMS/CrafterAuth/issues)

</div>
