# 🎓 Neptun Mobile

**Modern, nyílt forráskódú alternatív Neptun kliens magyar egyetemistáknak** – Jetpack Compose-ra épülő natív Android alkalmazás.

> ⚠️ **Disclaimer:** Ez egy nem hivatalos, közösségi kliens. Nem áll kapcsolatban a Mr. Nyílt / a Neptun üzemeltetőivel. A bejelentkezési adatok kizárólag titkosítva, helyben (eszközön) tárolódnak.

## ✨ Funkciók

| Modul | Leírás |
|---|---|
| 🏠 **Kezdőlap** | Napi áttekintés: következő/zajló óra, olvasatlan üzenetek, fizetendő tételek, mai órarend |
| 📅 **Órarend** | Napi és heti nézet, A/B hét (páros/páratlan) szűrés, hétvége megjelenítése opció, órarendi emlékeztetők |
| 📊 **Jegyek** | Súlyozott átlag, kreditindex, **szellemjegy kalkulátor**, féléves statisztika-diagram, kredithaladás-sáv, vizsgalista (kísérleti) |
| ✉️ **Üzenetek** | Oktatói/tanulmányi üzenetek olvasatlan szűrővel és kereséssel |
| 💰 **Pénzügyek** | Befizetendő és teljesített tételek, határidők, összesítés |
| 🔔 **Értesítések** | Óra-emlékeztetők (pontos alarm), ÚJ jegyekről / üzenetekről / pénzügyi tételekről szóló push (diff-alapú, spam-mentes), halk órák |
| 🔐 **Biztonság** | Titkosított hitelesítő-tárolás (Keystore + EncryptedSharedPreferences), TLS tanúsítvány-ellenőrzés, biometrikus zár opció |
| 📱 **Widget** | Kezdőképernyő-widget a mai órákkal (Glance) |
| 📤 **Export** | Órarend exportálása `.ics` naptárfájlba |

## 🏗️ Architektúra

```
app/
├── core/           # DI, biztonság, értesítések, szinkron (WorkManager), widget, export
├── data/           # Room adatbázis, Neptun API kliens (modern + legacy), repository-k
├── domain/         # Modellek, use case-ek (átlagszámítás, "ma" órák)
└── presentation/   # Compose UI + ViewModels (MVVM)
```

- **Modern API-first megközelítés**: ahol az egyetem Neptun portálja kínál modern REST API-t, azt használja; máshol a legacy `MobileService.svc` végpontokra esik vissza.
- **Offline-first**: minden adat Room adatbázisban cache-elt, így jellemzően offline is működik.
- 54 magyar egyetem/főiskoda előre konfigurálva (`universities.json`).

## 🔨 Buildelés

Követelmények: JDK 17+, Android SDK (compileSdk 36).

```bash
# Debug build
./gradlew assembleDebug

# Release build (R8 minify + shrink)
./gradlew assembleRelease

# Unit tesztek (JUnit + Robolectric)
./gradlew testDebugUnitTest

# Lint
./gradlew lintDebug
```

A CI (GitHub Actions) minden pushnál lefuttatja a teszteket, lintet és mindkét buildet: lásd [`.github/workflows/ci.yml`](.github/workflows/ci.yml).

### Demo mód (csak debug buildben)

Debug buildban a `DEMO01` neptun kóddal vagy `demo` jelszóval mintaadatokkal lehet kipróbálni az appot. Release buildben ez a lehetőség **letiltott**.

## 📥 Letöltés

A GitHub [Releases](../../releases) oldalról tölthető le az APK (debug és release változat). Az app maga is figyeli a frissítéseket és felajánlja az in-app update-et.

## 🗺️ Tervezett funkciók

- [ ] Vizsgára jelentkezés / tárgyfelvétel (a szerveroldali API függvényében)
- [ ] Teljes i18n (jelenleg a felület magyar nyelvű)
- [ ] Tárgyak részletes adatai (óra- és vizsgaidőpontok, követelmények)

## 📜 Licenc

Lásd: [LICENSE](LICENSE)
