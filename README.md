
<div align="center">

# CrafterAuth

Modern, hızlı ve güvenli kimlik doğrulama eklentisi — Velocity için Crafter tarafından geliştirildi.

![Velocity](https://img.shields.io/badge/Velocity-Proxy-0d6efd)
![Java](https://img.shields.io/badge/Java-17-orange)
![License](https://img.shields.io/badge/License-AGPL--3.0-success)
![Made by](https://img.shields.io/badge/Made%20by-Crafter-6f42c1)

</div>

## Nedir?

CrafterAuth; kayıt/giriş, güçlü parola politikaları, oyuncularınıza pürüzsüz bir doğrulama deneyimi sunar. Crafter CMS ile lisans ve kullanıcı doğrulaması yaparak tek merkezden yönetim sağlar.

## Öne Çıkanlar

- Crafter CMS ile lisans ve kullanıcı doğrulama
- Türkçe varsayılan mesajlar, kolayca özelleştirilebilir metinler
- Kayıt sırasında e‑posta toplama akışı (Crafter modu)
- BCrypt ile güvenli parola saklama, TOTP 2FA desteği
- Limbo tabanlı güvenli bekleme/oturum alanı

## Hızlı Başlangıç

1) Jar dosyasını indirin veya projeyi derleyin.
2) Jar dosyasını Velocity `plugins` klasörüne kopyalayın.
3) Sunucuyu başlatın; yapılandırma dosyaları otomatik oluşur.

## ⚠️ Güvenlik Uyarısı

**ASLA** bu bilgileri Git'e `config.yml` dosyasında pushlayın:
- API anahtarları (`license-key`, `api-secret`)
- Veritabanı şifresi
- Hassas kimlik bilgileri

Konfigürasyon dosyası `.gitignore` tarafından otomatik olarak git'ten hariç tutulur. 

Örnek konfigürasyon için `config.yml.example` dosyasını kullanın ve kendi bilgilerinizi doldurun.

## Yapılandırma (Crafter CMS)

`plugins/crafterauth/config.yml` içinde aşağıyı düzenleyin:

```yaml
database:
	storage-type: CRAFTER
	api-url: "https://api.crafter.net.tr"
	license-key: "<lisans_anahtarınız>"
	api-secret: "<api_anahtarınız>"
```

Prefix, mesajlar ve komutlar dahil tüm metinler yine bu dosyada özelleştirilebilir.

## Komutlar

- `/register`, `/kayit` — Kayıt ol
- `/login`, `/giris` — Giriş yap
- `/2fa`, `/totp` — 2FA yönetimi (opsiyonel)

## Neden CrafterAuth?

- Kurumsal: Lisans ve kullanıcı doğrulaması Crafter CMS ile entegre
- Güvenli: BCrypt, TOTP ve limbo tabanlı akış
- Yerel: Türkçe varsayılanlar, kolay uyarlama
- Hafif: Velocity için optimize edilmiş

## Destek

Sorularınız için: https://crafter.net.tr/discord

## Lisans ve Atıf

Bu proje AGPL‑3.0 lisansı altındadır (bkz. LICENSE).

Bu yazılım, açık kaynak bir kimlik doğrulama teknolojisinden türetilmiştir ve lisans yükümlülükleri doğrultusunda atıf korunmuştur.

