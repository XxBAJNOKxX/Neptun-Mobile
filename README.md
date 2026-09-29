# 🎓 Neptun Mobile

**Modern, nyílt forráskódú alternatív Neptun kliens magyar egyetemistáknak** – Jetpack Compose-ra épülő natív Android alkalmazás.

> ⚠️ **Disclaimer:** Ez egy nem hivatalos, közösségi kliens. Nem áll kapcsolatban az SDA Informatika Zrt.-vel / a Neptun üzemeltetőivel. A bejelentkezési adatok kizárólag titkosítva, helyben (eszközön) tárolódnak.

## ✨ Funkciók

| Modul | Leírás |
|---|---|
| 🏠 **Kezdőlap** | Napi áttekintés: következő/zajló óra, olvasatlan üzenetek, fizetendő tételek, aktuális tanulmányi időszak, kredithaladás, mai órarend |
| 📅 **Órarend** | Napi és heti nézet, A/B hét (páros/páratlan) szűrés, hétvége megjelenítése opció, tanév időszakai (szorgalmi, vizsgaidőszak), órarendi emlékeztetők |
| 📊 **Jegyek** | Súlyozott átlag, kreditindex, **szellemjegy kalkulátor**, féléves statisztika, kredithaladás és mintatanterv, hivatalos vizsgalista visszaszámlálóval |
| ✉️ **Üzenetek** | Oktatói/tanulmányi üzenetek kereséssel, olvasatlan szűrővel, szerveroldali olvasottá jelöléssel és gazdag HTML/táblázat megjelenítéssel |
| 💰 **Pénzügyek** | Befizetendő és teljesített tételek, határidők, státusz szerinti szűrés, pénzügyi összesítés |
| 🔔 **Értesítések** | Óra-emlékeztetők (pontos alarm), ÚJ jegyekről / üzenetekről / pénzügyi tételekről szóló push (diff-alapú, spam-mentes háttérszinkronnal) |
| 🔐 **Biztonság** | Titkosított hitelesítő-tárolás (Keystore + EncryptedSharedPreferences), kétlépcsős azonosítás (ELTE 2FA e-mail & TOTP), biometrikus zár opció, TLS tanúsítványkezelés |
| 🎨 **Megjelenés** | Világos, sötét és AMOLED fekete téma, 6 akcentusszín + Material You dinamikus színek, testreszabható menüsáv és kezdőképernyő |
| 🌐 **Többnyelvűség** | Teljes felületi lokalizáció (magyar 🇭🇺, angol 🇬🇧, német 🇩🇪) |
| 📱 **Widget** | Kezdőképernyő-widget a mai órákkal (Glance) |
| 📤 **Export** | Órarend exportálása `.ics` naptárfájlba |
| 🔄 **Frissítés** | Beépített frissítéskereső és in-app telepítő GitHub Releases alapján (Stabil és Dev csatornák) |

## 🏗️ Architektúra

```
app/
├── core/           # DI, biztonság, értesítések, szinkron (WorkManager), widget, export, frissítés, i18n
├── data/           # Room adatbázis, Neptun API kliens (modern REST + legacy WCF), repository-k
├── domain/         # Modellek, use case-ek (átlagszámítás, "ma" órák)
└── presentation/   # Compose UI (Material 3) + ViewModels (MVVM)
```

- **Modern API-first megközelítés**: ahol az egyetem Neptun portálja kínál modern REST API-t, azt használja; máshol a legacy `MobileService.svc` végpontokra esik vissza.
- **Offline-first**: minden adat Room adatbázisban cache-elt, így jellemzően offline is működik.
- 54 magyar egyetem/főiskola előre konfigurálva (`universities.json`).

## 🔨 Buildelés

Követelmények: JDK 17+, Android SDK (compileSdk 36, minSdk 26).

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

A GitHub [Releases](../../releases) oldalról tölthető le az APK (debug és release változat). Az app maga is figyeli a frissítéseket és felajánlja az in-app update-et (Stabil és Dev csatornán).

## 🗺️ Tervezett funkciók

- [x] Teljes i18n többnyelvűség (magyar, angol, német)
- [ ] **Jegyzetfüzet és Határidős Feladatkezelő (To-Do & Notes)**:
  - Tanulmányi jegyzetek és beadandó feladatok vezetése tárgyakhoz rendelve
  - Határidők (dátum, óra/perc) és konfigurálható előzetes értesítések (pl. 1 órával, 1 nappal előtte)
  - Kipipálható státusz (checkbox): befejezéskor az ütemezett értesítés automatikusan törlődik
  - Kezdőlapi kiemelés a közelgő határidőkről
- [ ] Tárgyak részletes adatai (követelmények, kurzusok, oktatók elérhetősége)
- [ ] Vizsgára jelentkezés / tárgyfelvétel (a szerveroldali API függvényében)
- [ ] Kérvények státuszának követése (leadva, bírálat alatt, elfogadva) és push értesítés változáskor
- [ ] Ösztöndíjak és kifizetések jóváírásainak listája a pénzügyeknél
- [ ] Üres terem / szabad tanulóhely kereső a campus épületeiben

## 📜 Licenc

Lásd: [LICENSE](LICENSE)
