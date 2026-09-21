# 🎓 Neptun Mobile

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?logo=android&logoColor=white" alt="Platform: Android 8.0+">
  <img src="https://img.shields.io/badge/Language-Kotlin_2.0+-7F52FF?logo=kotlin&logoColor=white" alt="Language: Kotlin">
  <img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white" alt="UI: Jetpack Compose">
  <img src="https://img.shields.io/badge/Design-Material_3-7C4DFF?logo=materialdesign&logoColor=white" alt="Material 3">
  <img src="https://img.shields.io/badge/License-MIT-blue.svg" alt="License: MIT">
  <img src="https://img.shields.io/github/v/release/XxBAJNOKxX/Neptun-Mobile?include_prereleases&label=Latest%20Version&color=orange" alt="GitHub Release">
</p>

**Modern, villámgyors és nyílt forráskódú alternatív Neptun kliens magyar egyetemistáknak és főiskolásoknak.**
Az alkalmazás natív **Android Jetpack Compose** technológiára, tiszta architektúrára (Clean Architecture + MVVM) és offline-first szemléletre épül. Célja, hogy egy letisztult, ergonomikus és átlátható felületet biztosítson a mindennapi egyetemi teendők kezelésére.

> [!WARNING]
> **Jogi nyilatkozat (Disclaimer):**
> Ez egy független, nyílt forráskódú közösségi projekt. Nem áll kapcsolatban az SDA Informatika Zrt.-vel, a Hivatalos Neptun üzemeltetőivel vagy bármely oktatási intézménnyel. A hitelesítési adatok kizárólag a telefon biztonsági hardverelemével védett titkosított tárolójában (`Android Keystore` + `EncryptedSharedPreferences`) tárolódnak, harmadik fél szervereire soha nem kerülnek továbbításra.

---

## 📑 Tartalomjegyzék

- [✨ Főbb funkciók](#-főbb-funkciók)
  - [🏠 Kezdőlap (Dashboard)](#-kezdőlap-dashboard)
  - [📅 Órarend és Időszakok](#-órarend-és-időszakok)
  - [📊 Jegyek, Szellemjegy kalkulátor, Vizsgák és Tanulmányi haladás](#-jegyek-szellemjegy-kalkulátor-vizsgák-és-tanulmányi-haladás)
  - [✉️ Üzenetek](#️-üzenetek)
  - [💰 Pénzügyek](#-pénzügyek)
  - [📱 Kezdőképernyő Widget (Glance)](#-kezdőképernyő-widget-glance)
  - [🔔 Intelligens Értesítések és Szinkronizáció](#-intelligens-értesítések-és-szinkronizáció)
  - [🔐 Biztonság és Kétlépcsős azonosítás (2FA)](#-biztonság-és-kétlépcsős-azonosítás-2fa)
  - [🎨 Testreszabhatóság, Témák és Többnyelvűség](#-testreszabhatóság-témák-és-többnyelvűség)
  - [🔄 Beépített Frissítéskezelő (In-App Updates)](#-beépített-frissítéskezelő-in-app-updates)
- [🏫 Támogatott Egyetemek](#-támogatott-egyetemek)
- [🏗️ Architektúra és Technológiák](#️-architektúra-és-technológiák)
- [🔨 Buildelés és Fejlesztés](#-buildelés-és-fejlesztés)
  - [Előfeltételek](#előfeltételek)
  - [Gradle parancsok](#gradle-parancsok)
  - [Demo üzemmód](#demo-üzemmód)
- [🚀 CI/CD és Kiadások](#-cicd-és-kiadások)
- [🤝 Hozzájárulás](#-hozzájárulás)
- [📜 Licenc](#-licenc)

---

## ✨ Főbb funkciók

### 🏠 Kezdőlap (Dashboard)
- **Személyre szabott üdvözlés:** Napszakhoz igazodó köszöntés a hallgató nevével, egyetemével és szakjával.
- **Gyorsstátusz összefoglaló:** Egy pillantással látható az olvasatlan üzenetek száma, a befizetésre váró összegek, a féléves súlyozott tanulmányi átlag és a soron következő órák.
- **Éppen zajló óra kijelző:** Élő előrehaladási sávval és terem-információval mutatja a jelenleg tartó előadást/gyakorlatot.
- **Következő óra kártya:** Pontos időponttal, visszaszámlálóval és helyszínnel.
- **Aktuális tanulmányi időszak banner:** Dinamikusan jelzi a kurzusfelvételi, vizsga- vagy szorgalmi időszak státuszát és a hátralévő napokat.
- **Kredithaladás összefoglaló:** Vizuális folyamatjelző a teljes diploma eléréséhez szükséges kreditek állásáról közvetlen mélyhivatkozással.
- **Mai órarend áttekintés:** A napi órák kronologikus listája.

### 📅 Órarend és Időszakok
- **Napi és heti nézet:** Rugalmas váltási lehetőség a kompakt napi és a teljes heti órarendi bontás között.
- **A/B hét támogatás:** Automatikus páros és páratlan heti kurzusszűrés a helyes órarendi struktúrához.
- **Kurzustípusok színezett címkékkel:** Külön vizuális jelölés az előadásokhoz, gyakorlatokhoz, laborokhoz, szemináriumokhoz és vizsgákhoz.
- **Részletes tanóra információk:** Épület, terem, oktató neve, kezdési és befejezési időpont.
- **Hétvégi napok opció:** A beállításokban tetszés szerint engedélyezhető vagy elrejthető a szombat/vasárnap (pl. levelezős hallgatóknak).
- **Időszakok (Academic Periods) nézet:**
  - A félév hivatalos szakaszai: regisztrációs időszak, tárgyfelvétel, szorgalmi időszak, vizsgaidőszak, oktatási szünetek.
  - Szűrési lehetőségek: *Aktív*, *Közelgő*, *Lezárult* és *Mind*.
  - Hátralévő vagy kezdésig hátralévő napok számlálója.
- **Órarendi emlékeztetők:** Egy érintéssel beállítható helyi értesítés vagy pontos riasztás bármelyik órához.
- **Naptárexport (.ics):** Teljes féléves órarend exportálása szabványos iCalendar (`.ics`) fájlba, amely közvetlenül importálható Google Calendarba, Apple Calendarba vagy Outlookba.

### 📊 Jegyek, Szellemjegy kalkulátor, Vizsgák és Tanulmányi haladás
A tanulmányi felület 3 fő szekcióra tagolódik:

#### 1. Jegyek és Átlagok
- **Félévválasztó:** Tetszőleges félévek jegyeinek és felvett kurzusainak megtekintése.
- **Valós idejű átlagszámítás:**
  - **Súlyozott Tanulmányi Átlag (SZTÁ):** $\frac{\sum (\text{Jegy} \times \text{Kredit})}{\sum \text{Kredit}}$
  - **Hagyományos Kreditindex:** $\frac{\sum (\text{Jegy} \times \text{Kredit})}{30}$
- **Szellemjegy (Ghost Grade) kalkulátor:**
  - Szimulációs eszköz: beállíthatsz virtuális, várható jegyeket (1–5) a még le nem zárt tantárgyaidhoz.
  - Az alkalmazás azonnal újraszámolja a várható súlyozott átlagot és kreditindexet.
  - Egyetlen kattintással visszaállíthatóak az eredeti szerveroldali állapotok.
- **Státuszjelvények:** Pontos vizuális visszajelzés (Jeles, Jó, Közepes, Elégséges, Elégtelen, Aláírva, Nem teljesített, Még nincs jegy).

#### 2. Vizsgák (Exams)
- Hivatalos Neptun vizsgalisták lekérdezése (vizsganév, kód, terem, oktató, pontos időpont, vizsgatípus).
- Visszaszámláló a vizsga kezdetéig hátralévő napokkal.
- Szűrés: *Minden vizsga*, *Csak felvett vizsgák*, *Közelgő vizsgák*.
- Jelentkezési határidők megjelenítése.

#### 3. Tanulmányi haladás (Degree Progress)
- Kredithaladási sáv a képzés teljes kreditszámához képest (pl. 145 / 210 kredit).
- Mintatantervi modulok részletes teljesítési arányai:
  - Kötelező tárgyak
  - Kötelezően választható tárgyak
  - Szabadon választható tárgyak
  - Diplomamunka / Szakdolgozat kreditek
  - Kritériumkövetelmények (pl. testnevelés, szakmai nyelv)
- Kumulatív tanulmányi átlag és kumulatív kreditindex.

### ✉️ Üzenetek
- Oktatói, tanszéki és tanulmányi osztályos üzenetek gyors betöltése.
- **Keresőmotor:** Keresés a feladó nevében, tárgyban és a levél előnézeti szövegében.
- **Olvasatlan szűrő:** Egy érintéssel csak a még el nem olvasott levelek mutatása.
- **Kétirányú olvasott állapot:** Az üzenet megnyitásakor helyben és a Neptun szerveren is olvasottá válik.
- **Gazdag HTML formázás Modal Bottom Sheet-ben:** Neptun táblázatok (`HtmlTableView`), hiperhivatkozások és kiemelések igényes, görgethető és kimásolható megjelenítése.

### 💰 Pénzügyek
- Pénzügyi összesítő kártya: kiírt tartozások összege és összes teljesített befizetés forintban.
- Státusz szűrőchipek: *Mind*, *Kiírva (Pending)*, *Teljesítve (Completed)*, *Késedelmes (Overdue)*.
- Tétel részletei: fizetési határidő, félév, tranzakcióazonosító, befizetés napja.

### 📱 Kezdőképernyő Widget (Glance)
- Modern, **Jetpack Glance** alapú reszponzív Android kezdőképernyő-widget.
- Automatikusan listázza a mai nap tanóráit, tantermeit és időpontjait közvetlenül a főképernyőn az app megnyitása nélkül.
- Egy érintéssel elindítja az applikációt a releváns nézetben.

### 🔔 Intelligens Értesítések és Szinkronizáció
- **Pontos óra-emlékeztetők:** Android `AlarmManager` (`setExactAndAllowWhileIdle`) használatával az órák előtt 5–120 perccel (beállítható időközzel), amely a telefon újraindításakor automatikusan újraszerveződik (`BootCompletedReceiver`).
- **Diff-alapú intelligens értesítési motor:** A `WorkManager` háttérszinkronizáció összehasonlítja az adatbázis korábbi és friss állapotát – **csak akkor küld értesítést, ha valóban új jegy, új üzenet vagy új befizetendő tétel érkezett**, megszüntetve a felesleges ismétlődő push-értesítéseket.
- Különálló Android értesítési csatornák (Órák, Jegyek, Üzenetek, Pénzügyek), egyenként konfigurálható engedélyekkel.

### 🔐 Biztonság és Kétlépcsős azonosítás (2FA)
- **Hardveresen titkosított adattárolás:** A bejelentkezési adatok és munkamenetek az `Android Keystore` által generált AES256-GCM kulcsokkal titkosított `EncryptedSharedPreferences` tárolóban maradnak az eszközön.
- **Kétlépcsős hitelesítés (2FA):** Teljes ELTE és egyéb intézményi kétfaktoros azonosítás támogatása (E-mail kód és TOTP hitelesítő applikációk).
- **Biometrikus alkalmazászár:** Ujjlenyomatos vagy arc alapú biometrikus hitelesítés (`BiometricPrompt`) kérhető minden alkalmazásindításkor.
- **Intézményi TLS tanúsítványkezelés:** Automatikus egyetemi gyökértanúsítvány-kezelés (`SslTrustHelper`), amely régebbi Android változatokon is stabil, megbízható HTTPS kapcsolatot biztosít.
- **Offline-first működés:** Sikeres bejelentkezés után a teljes órarend, jegyek és adatok internetkapcsolat nélkül, a helyi Room adatbázisból azonnal elérhetőek.

### 🎨 Testreszabhatóság, Témák és Többnyelvűség
- **Témamódok:** Rendszerszintű automatikus váltás, Állandó Világos, Állandó Sötét, valamint valódi **AMOLED tiszta fekete** sötét mód.
- **Színpaletták:**
  - Neptun Kék
  - Zafír Indigó
  - Óceán Cián
  - Erdőzöld
  - Naplemente Narancs
  - Bíbor Rózsa
  - **Dinamikus Material You színvilág** (Android 12+ háttérkép-alapú paletta).
- **Kezdőképernyő választó:** Beállítható, hogy indításkor a Kezdőlap, az Órarend vagy a Jegyek jelenjen meg.
- **Testreszabható navigáció:** Az alsó menüsáv bármely felesleges eleme elrejthető.
- **Többnyelvű felület (i18n):** Teljes, anyanyelvi szintű lokalizáció:
  - 🇭🇺 **Magyar**
  - 🇬🇧 **English**
  - 🇩🇪 **Deutsch**

### 🔄 Beépített Frissítéskezelő (In-App Updates)
- Az alkalmazás képes a háttérben ellenőrizni a GitHub Releases új verzióit.
- **Csatornaválasztó:**
  - **Stabil (Stable):** Csak a hivatalosan tesztelt, mérföldkő kiadások.
  - **Fejlesztői (Dev):** A legfrissebb fejlesztői buildek azonnali elérése.
- Letöltési folyamatjelző és közvetlen, egykattintásos APK telepítés FileProvideren keresztül.

---

## 🏫 Támogatott Egyetemek

Az applikációban több mint **50 magyar felsőoktatási intézmény** és kar Neptun-rendszere előre be van konfigurálva az [`app/src/main/assets/universities.json`](app/src/main/assets/universities.json) alapján:

- Budapesti Műszaki és Gazdaságtudományi Egyetem (BME)
- Eötvös Loránd Tudományegyetem (ELTE)
- Budapesti Corvinus Egyetem (BCE)
- Szegedi Tudományegyetem (SZTE)
- Debreceni Egyetem (DE)
- Pécsi Tudományegyetem (PTE)
- Óbudai Egyetem (ÓE)
- Pázmány Péter Katolikus Egyetem (PPKE)
- Széchenyi István Egyetem (SZE)
- Budapesti Gazdasági Egyetem (BGE)
- Magyar Agrár- és Élettudományi Egyetem (MATE)
- Károli Gáspár Református Egyetem (KRE)
- Semmelweis Egyetem (SE)
- Miskolci Egyetem (ME)
- Soproni Egyetem (SOE)
- Pannon Egyetem (PE)
- Nemzeti Közszolgálati Egyetem (NKE)
- És további több mint 35 magyar egyetem és főiskola.

A bejelentkezési képernyőn intelligens keresővel választható ki a kívánt intézmény.

---

## 🏗️ Architektúra és Technológiák

A projekt a Google által javasolt modern Android architektúra-ajánlásokat követi:

```
app/src/main/java/com/example/
├── core/
│   ├── crash/              # Hibakezelés és globális logolás
│   ├── di/                 # Dependency Injection modulok
│   ├── export/             # .ics iCalendar naptárexportáló
│   ├── i18n/               # Nemzetköziesítés (AppStrings: HU, EN, DE)
│   ├── network/            # TLS/SSL tanúsítvány megbízhatósági kezelő
│   ├── notification/       # AlarmScheduler, NotificationHelper, NotifiedItemsTracker
│   ├── security/           # EncryptedPreferencesManager, BiometricLockHelper
│   ├── update/             # GitHub In-App Update letöltő és telepítő
│   ├── widget/             # Android Glance kezdőképernyő widget
│   └── work/               # WorkManager háttérszinkronizáció (SyncWorker)
├── data/
│   ├── local/              # Room Adatbázis (NeptunDatabase), DAO-k és Entity-k
│   ├── network/            # Kétutas NeptunApiClient (Modern REST API + WCF fallback)
│   └── repository/         # AuthRepository és NeptunRepository implementációk
├── domain/
│   ├── model/              # Domain adatmodellek (CalendarEvent, SubjectGrade, stb.)
│   └── usecase/            # Üzleti logika és számítások (CalculateAverages, GetTodayClasses)
├── presentation/
│   ├── navigation/         # Compose Navigation útvonalak és alsó navigációs sáv
│   ├── ui/
│   │   ├── components/     # Újrafelhasználható UI komponensek (TopBar, Badge-ek)
│   │   ├── screens/        # Képernyők (Dashboard, Timetable, Grades, Messages, stb.)
│   │   └── util/           # HTML formázó segédeszközök (HtmlTableView, link kezelés)
│   └── viewmodel/          # StateFlow alapú MVVM ViewModel réteg
└── ui/theme/               # Material 3 design rendszer, dinamikus témák, AMOLED paletták
```

### Kulcstechnológiák:
- **UI:** Jetpack Compose, Material 3, Compose Navigation, Material Icons Extended
- **Aszinkronitás & Reaktív adatfolyam:** Kotlin Coroutines, StateFlow, SharedFlow
- **Adatbázis & Cache:** Room (SQLite) orm, KSP fordító
- **Hálózati réteg:** OkHttp 4, HttpLoggingInterceptor, robusztus HTML/JSON parser
- **Widget:** AndroidX Glance AppWidget
- **Biztonság:** AndroidX Security Crypto (MasterKey, AES256-GCM), BiometricPrompt
- **Ütemezés:** Android WorkManager, AlarmManager

---

## 🔨 Buildelés és Fejlesztés

### Előfeltételek
- **JDK:** OpenJDK 17 vagy frissebb
- **Android SDK:** `compileSdk 36` (Android 16), `minSdk 26` (Android 8.0 Oreo)
- **Gradle:** Gradle Wrapper (a repository tartalmazza)

### Gradle parancsok

A projekt közvetlenül buildelhető parancssorból vagy Android Studio-ból:

```bash
# Debug APK fordítása
./gradlew assembleDebug

# Release APK fordítása (R8 optimalizáció és Proguard kódzsugorítás)
./gradlew assembleRelease

# Unit és Robolectric tesztek futtatása
./gradlew testDebugUnitTest

# Kódminőség és Lint ellenőrzés
./gradlew lintDebug
```

> **Windows rendszeren:** Használd a `.\gradlew.bat` parancsot a `./gradlew` helyett.

### Demo üzemmód
A debug változat tartalmaz egy beépített mintaadat-generátort, amellyel valódi Neptun bejelentkezés nélkül is kipróbálható a felület:
- **Neptun kód:** `DEMO01`
- **Jelszó:** `demo`

*(Release buildben a demo fiók biztonsági okokból teljesen le van tiltva).*

---

## 🚀 CI/CD és Kiadások

A repository GitHub Actions alapú automatizált munkafolyamatokat használ:
- **Folyamatos integráció ([`ci.yml`](.github/workflows/ci.yml)):** Minden Pull Request és push esetén lefut a JUnit tesztcsomag, a Robolectric tesztek, a lint ellenőrzés és a debug/release összeállítás.
- **Automatikus kiadások ([`android-release.yml`](.github/workflows/android-release.yml)):** 
  - A `main` ágra való push esetén automatikus **Dev (Pre-release)** build jön létre.
  - A `stable` ágon hivatalos, verziószámozott **Stabil kiadás** készül, aláírt APK melléklettel.

A legfrissebb kiadások elérhetők a [GitHub Releases](../../releases) felületén.

---

## 🤝 Hozzájárulás

Szívesen fogadjuk a hibajelentéseket, funkciójavaslatokat és a Pull Requesteket!
1. Nyiss egy új Issue-t a javasolt módosítás részleteivel.
2. Forkold a repót, hozz létre egy új feature-ágat (`feature/uj-funkcio`).
3. Győződj meg róla, hogy a tesztek sikeresen lefutnak (`./gradlew testDebugUnitTest`).
4. Nyiss egy Pull Requestet a `main` ágra.

---

## 📜 Licenc

A projekt az **MIT Licenc** feltételei szerint használható és módosítható. Részletek a [LICENSE](LICENSE) fájlban.
