# 🛒 Akıllı Market Fiyat Karşılaştırma Sistemi

Production-grade Android uygulaması - Kotlin + Jetpack Compose + MVVM + Clean Architecture

## 📋 Proje Özeti

Kullanıcıların marketlerden aldığı ürünleri kaydetmesini, gram/kg/litre birim fiyatlarını otomatik hesaplamalarını, geçmiş fiyatlarla karşılaştırmalarını ve en uygun marketi bulmalarını sağlayan gelişmiş Android uygulaması.

---

## 🏗️ Mimari

```
MarketFiyat/
├── app/
│   └── src/main/kotlin/com/marketfiyat/
│       ├── core/
│       │   ├── data/
│       │   │   ├── local/
│       │   │   │   ├── dao/          → Room DAOs
│       │   │   │   ├── entity/       → Room Entities + Mappers
│       │   │   │   ├── relations/    → Room Relations
│       │   │   │   └── datastore/    → DataStore Preferences
│       │   │   └── repository/       → Repository Implementations
│       │   ├── domain/
│       │   │   ├── model/            → Domain Models
│       │   │   └── repository/       → Repository Interfaces
│       │   ├── di/                   → Hilt DI Modules
│       │   ├── navigation/           → NavHost + Routes
│       │   ├── receiver/             → BroadcastReceiver + WorkManager
│       │   ├── service/              → Foreground Services
│       │   ├── ui/components/        → Reusable Compose Components
│       │   └── util/                 → Utilities, Extensions
│       └── feature/
│           ├── home/                 → Ana Sayfa
│           ├── addprice/             → Fiyat Ekleme
│           ├── archive/              → Ürün Arşivi
│           ├── compare/              → Market Karşılaştırması
│           ├── shoppinglist/         → Alışveriş Listesi
│           ├── statistics/           → İstatistikler
│           ├── settings/             → Ayarlar
│           ├── barcode/              → Barkod Tarayıcı
│           └── ocr/                  → Fiş Okuyucu (OCR)
```

---

## 🛠️ Tech Stack

| Katman | Teknoloji |
|--------|-----------|
| UI | Jetpack Compose, Material 3, Animations |
| Architecture | MVVM + Clean Architecture + Repository Pattern |
| DI | Hilt 2.55 |
| Database | Room 2.7.0 |
| Preferences | DataStore |
| Navigation | Navigation Compose 2.8.9 |
| Async | Kotlin Coroutines + Flow |
| Network | Retrofit 2.11 + OkHttp 4.12 |
| Serialization | Kotlin Serialization 1.8.0 |
| Image Loading | Coil 2.7.0 |
| Camera | CameraX 1.4.1 |
| ML Kit | Barcode Scanning + Text Recognition |
| Background | WorkManager 2.10.0 |
| Build | Gradle 9.4.1 + AGP 8.8.0 + KSP |

---

## 📦 Kurulum

### Gereksinimler
- Android Studio Meerkat (2024.3.1+)
- JDK 17+
- Android SDK 36
- Gradle 9.4.1

### Adımlar

```bash
# 1. Projeyi klonla veya ZIP'i aç
cd MarketFiyat

# 2. local.properties dosyasını düzenle
echo "sdk.dir=/path/to/Android/sdk" > local.properties

# 3. Android Studio'da aç ve sync et
# ya da terminal'den:
./gradlew assembleDebug
```

---

## 🗄️ Veritabanı Yapısı

```sql
products          → Ürün bilgileri (isim, marka, barkod, favori)
product_prices    → Fiyat geçmişi + birim fiyatlar (perKg, per100g, perLitre)
markets           → Market listesi (A101, BİM, ŞOK, Migros, ...)
shopping_lists    → Alışveriş listeleri
shopping_list_items → Liste öğeleri + tahmin fiyat
barcode_cache     → Barkod önbelleği
```

**İndeksler:** `product_id`, `market_id`, `purchase_date`, `unit_price_per_kg` — 50.000+ ürün için optimize edildi.

---

## ✨ Özellikler

### 1. Ürün Ekleme
- Ürün adı, marka, market, miktar, birim, fiyat, indirimli fiyat, tarih, not
- Barkod tarama ile otomatik doldurma
- OCR ile fiş okuma

### 2. Otomatik Birim Fiyat Hesabı
```
700g → 189 TL ise:
  1 KG  = 270,00 TL
  100g  =  27,00 TL
```

### 3. Gerçek Zamanlı Analiz
- Fiyat girilirken anında karşılaştırma
- "Son 6 ayın en ucuz fiyatı!" bildirimi
- Geçmiş ortalamaya göre yüzdelik fark

### 4. Market Karşılaştırması
- Aynı ürün için tüm marketlerin fiyatları
- En ucuz market vurgulaması
- Birim fiyat karşılaştırması

### 5. Akıllı Alışveriş Listesi
- Market bazlı gruplama
- Tahmini toplam hesaplama
- Tamamlama takibi

### 6. İstatistikler
- Aylık harcama grafiği
- Market bazlı harcama dağılımı
- Dönem seçimi (1-3-6-12 ay)

### 7. Yedekleme
- JSON export/import
- CSV export
- Otomatik günlük yedekleme (WorkManager)

### 8. Tema
- Material You (Dynamic Color)
- Dark / Light / System modu
- Tam Türkçe dil desteği

---

## 🔒 İzinler

```
CAMERA                    → Barkod + OCR tarama
POST_NOTIFICATIONS        → Fiyat alarmları
RECEIVE_BOOT_COMPLETED    → Yedekleme zamanlayıcısı
FOREGROUND_SERVICE        → Arka plan işlemleri
FOREGROUND_SERVICE_DATA_SYNC → Android 14+ uyumu
```

---

## 🧪 Test

```bash
# Unit testleri çalıştır
./gradlew test

# Lint kontrolü
./gradlew lint

# Release APK build
./gradlew assembleRelease
```

**Test kapsamı:**
- `UnitPriceCalculatorTest` — Birim fiyat hesaplamaları
- `PriceFormatTest` — Para birimi formatı

---

## 📁 Önemli Dosyalar

| Dosya | Açıklama |
|-------|---------|
| `MarketFiyatApp.kt` | Application + Hilt + Timber + Bildirim kanalları |
| `MainActivity.kt` | SplashScreen API + EdgeToEdge + Tema |
| `MarketFiyatDatabase.kt` | Room DB + 6 tablo + varsayılan marketler |
| `Navigation.kt` | NavHost + animasyonlu geçişler |
| `UserPreferencesDataStore.kt` | Kullanıcı tercihleri |
| `UnitPriceCalculator.kt` | Birim fiyat hesaplama motoru |
| `CommonComponents.kt` | Shimmer, EmptyState, ErrorState, TopBar |

---

## 📝 Notlar

- **minSdk 26** → Android 8.0+ (geniş kapsam)
- **targetSdk 36** → Android 16 hedefi
- **KSP** kullanıldı (KAPT yerine — daha hızlı build)
- **Configuration Cache** aktif → tekrar buildlerde ~40% hız artışı
- **R8 full mode** → release build'de küçük APK boyutu
- **Scoped Storage** uyumlu — FileProvider ile backup
- **Android 14+** `FOREGROUND_SERVICE_TYPE_DATA_SYNC` kullanıldı

---

## 🔮 Gelecek Özellikler (Roadmap)

- [ ] Google Drive yedekleme entegrasyonu
- [ ] Fiyat alarmı WorkManager implementasyonu
- [ ] Widget desteği (App Widget)
- [ ] YZ destekli fiyat tahmini
- [ ] Enflasyon grafiği
- [ ] Favori ürün bildirimleri
- [ ] Çoklu para birimi desteği

---

**Geliştirici:** Market Fiyat Team  
**Lisans:** Private  
**Versiyon:** 1.0.0
