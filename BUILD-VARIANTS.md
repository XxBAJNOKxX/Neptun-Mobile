# BUILD-VARIANTS Terv: NeptunMobile Debug és Release Variánsok Szétválasztása

> **Státusz:** Tervezet jóváhagyásra  
> **Ág:** `feat/build-variants` (elszigetelt worktree)  
> **Párhuzamos ág:** `fix/elte-session` (érintetlen auth réteg)  
> **Dátum:** 2026-10-02  

---

## 1. Architektúrális Áttekintés és Alapelvek

A cél a NeptunMobile Android alkalmazás **debug** és **release** build-változatainak teljes szétválasztása úgy, hogy:
1. **Párhuzamosan telepíthetők legyenek ugyanarra az eszközre** ütközések nélkül (`applicationIdSuffix = ".debug"`, független FileProvider, widgetek, értesítések).
2. **A release tiszta legyen:** nincs demó adat, nincs fejlesztői menü, R8 teljes optimalizálás (`Log.d`/`Log.v` strip), tiltott cleartext forgalom, és dedikált ellenőrző teszt/task a tisztaság igazolására.
3. **A debug funkciógazdag legyen:** demó mód (`DEMO01` mintaadatokkal), maszkolt fejlesztői hálózati/session naplózó, StrictMode, LeakCanary, vizuális megkülönböztetés (ikon-badge, app név, verziójelzés a UI-n).
4. **Az in-app frissítő önállóan kezelje a két változatot:** a release app kizárólag release APK-t, a debug app kizárólag debug APK-t frissíthet; a debug frissítés determinisztikus aláírás-egyezést kap egy repóba rögzített `debug.keystore` révén; a GitHub API hívások ETag / időalapú cache-sel és rate-limit védelemmel egészülnek ki.
5. **SZIGORÚ KORLÁT – Párhuzamos munka védelme:** Az auth rétegekhez (`fix/elte-session` ág területe: login, 2FA, cookie/token tárolás, HTTP kliens session-kezelése, `core/security`) **TILOS** hozzányúlni. A variánsok szétválasztása kizárólag a Gradle konfiguráció és a forráskészletek (`src/debug` vs `src/release`) szintjén történik!

---

## 2. FÁZIS 0 – Felderítési Leltár

### 2.1. Csomagnév és Azonosítók
* **Jelenlegi `applicationId`:** `app.neptun.yrklqi` ([app/build.gradle.kts#L30](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/build.gradle.kts#L30)).
* **Cél debug `applicationId`:** `app.neptun.yrklqi.debug`.
* **Alkalmazás név:**
  * Release: `@string/app_name` = `"Neptun Mobile"`
  * Debug: `"NeptunMobile Debug"` (a `src/debug/res/values/strings.xml` felülbírálásával vagy `resValue`-val).
* **Alkalmazás ikon:**
  * Release: meglévő adaptív ikon (`ic_launcher.xml`).
  * Debug: `src/debug/res/` alatt megkülönböztethető foreground/layer-list DEBUG jelvénnyel (szalaggal).

### 2.2. ApplicationId-hoz kötött Komponensek és Erőforrások
* **FileProvider:**
  * [AndroidManifest.xml#L66](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/AndroidManifest.xml#L66): `android:authorities="${applicationId}.fileprovider"` – Helyes, a dinamikus manifest helyettesítőt használja.
  * Kotlin hivatkozások:
    * [AppUpdateManager.kt#L397](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/core/update/AppUpdateManager.kt#L397): `val authority = "${context.packageName}.fileprovider"` – Dinamikus, futásidőben felveszi a `.debug`-ot.
    * [MainAppContent.kt#L750](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/presentation/ui/MainAppContent.kt#L750): `"${app.packageName}.fileprovider"` – Szintén dinamikus.
* **Widget:**
  * [TodayWidgetReceiver](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/AndroidManifest.xml#L54): `BroadcastReceiver`, nem ContentProvider, így a rendszer csomagnév szerint különíti el, nem okoz ütközést.
* **Értesítési Csatornák:**
  * [NotificationHelper.kt#L17-L28](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/core/notification/NotificationHelper.kt#L17-L28): A csatorna ID-k konstansok (`neptun_classes_channel` stb.). Mivel az Android OS a csatornákat csomagnév (`packageName`) szerint tárolja és különíti el, a `.debug` csomagnév miatt a két app csatornái nem keverednek össze a rendszerbeállításokban.
* **Deep Linkek:**
  * Jelenleg **nincs** regisztrált deep link intent-filter az [AndroidManifest.xml](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/AndroidManifest.xml)-ben.
  * Tervezett konvenció jövőbeli deep linkekhez: debugban `neptun-debug://` séma vagy `${applicationId}` alapú host.

### 2.3. Demó Mód Leltár
* **Adatforrás:** [MockNeptunDataSource.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/data/network/MockNeptunDataSource.kt) – 593 sornyi statikus mock adat (órák, jegyek, üzenetek, pénzügyek, vizsgák, előrehaladás).
* **Használat a repóban:**
  * [NeptunRepositoryImpl.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/data/repository/NeptunRepositoryImpl.kt): `refreshCalendar()`, `refreshGrades()`, `refreshMessages()`, `refreshFinances()`, `refreshExams()`, `refreshDegreeProgress()`, `refreshAcademicPeriods()` metódusokban `if (isDemo)` ágak hívják.
  * [AuthRepositoryImpl.kt#L72-L86](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt#L72-L86): `DEMO01` belépési ág (Auth réteg – nem piszkálható!).
  * [AuthViewModel.kt#L238](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/presentation/viewmodel/AuthViewModel.kt#L238): `quickDemoFill()` (Auth réteg – nem piszkálható!).
  * [MainAppContent.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/presentation/ui/MainAppContent.kt): `DemoModeBanner` komponens megjelenítése.

### 2.4. In-App Frissítő Leltár
* [AppUpdateManager.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/core/update/AppUpdateManager.kt):
  * **Hiba az asset választásban:** `selectBestAsset` fallback ága (`?: apkAssets.firstOrNull()`) felkínálhatja a másik build típus APK-ját, ha a megfelelő nem található.
  * **Hiányzó cache:** Minden indításkor közvetlen GitHub API lekérdezést indít, nincs ETag (`If-None-Match`) és nincs időablak (pl. 30 perc).
  * **Verzió parsing:** A jelenlegi `ParsedVersion` nem kezeli a `-debug+<build_number>` vagy `-dev.<build>` utótagok precíz numerikus összehasonlítását.
  * **Aláírás-ellenőrzés:** A `verifyApkSignature` funkció már létezik és ellenőrzi a tanúsítvány-egyezést.

### 2.5. Keystore és Aláírás Leltár
* [app/build.gradle.kts#L41-L69](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/build.gradle.kts#L41-L69):
  * A `signingConfigs.debug` keresi a `${rootDir}/debug.keystore`-t, de a fájl hiányzik.
  * A [.gitignore#L18](file:///C:/Users/danit/Downloads/NeptunMobile-variants/.gitignore#L18) ignorálja a `debug.keystore`-t.
  * Emiatt a lokális buildek gépenként eltérő `~/.android/debug.keystore`-ral készülnek, ami eltöri a debug in-app frissítést (`verifyApkSignature` elbukik).
  * A release `signingConfig` veszélyes fallbacket tartalmaz a debug kulcsra, ha nincs megadva titkos kulcs.

---

## 3. FÁZIS 1 – Változtatási Terv Fájlonként

### 3.1. Érintett Fájlok és Változtatások

#### A) Építési Rendszer és Konfiguráció
1. **[.gitignore](file:///C:/Users/danit/Downloads/NeptunMobile-variants/.gitignore)**
   - Kivesszük a `debug.keystore` ignorálását, vagy fehérlistázzuk a rögzített debug kulcsot (`!debug.keystore` vagy `!app/debug.keystore`).
2. **`debug.keystore` (ÚJ FÁJL a projekt gyökerében vagy `app/debug.keystore`)**
   - Standard, rögzített Android debug keystore elhelyezése (`storePassword: android`, `keyAlias: androiddebugkey`, `keyPassword: android`).
   - Ezt használja minden fejlesztő lokálisan és a CI környezet is.
3. **[gradle/libs.versions.toml](file:///C:/Users/danit/Downloads/NeptunMobile-variants/gradle/libs.versions.toml)**
   - LeakCanary verzió és könyvtár hozzáadása:
     `leakcanary = "2.14"`  
     `leakcanary-android = { group = "com.squareup.leakcanary", name = "leakcanary-android", version.ref = "leakcanary" }`
4. **[app/build.gradle.kts](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/build.gradle.kts)**
   - **Debug buildType:**
     - `applicationIdSuffix = ".debug"`
     - `signingConfig = signingConfigs.getByName("debug")`
   - **Release buildType:**
     - `isMinifyEnabled = true`
     - `isShrinkResources = true`
     - `isDebuggable = false`
     - `proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")`
     - Aláírás: kizárólag a `signingConfigs.release` használata, a veszélyes fallback (`initWith(debug)`) elkerülése / biztonságos CI ellenőrzés.
   - **Függőségek:**
     - `debugImplementation(libs.leakcanary.android)`
5. **[app/proguard-rules.pro](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/proguard-rules.pro)**
   - Log strip szabályok hozzáadása:
     ```proguard
     -assumenosideeffects class android.util.Log {
         public static boolean isLoggable(java.lang.String, int);
         public static int v(...);
         public static int d(...);
     }
     ```

#### B) Forráskészletek (Source Sets) Szétválasztása
1. **[MockNeptunDataSource.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/data/network/MockNeptunDataSource.kt) áthelyezése:**
   - **Törlés a `src/main`-ből.**
   - **Debug implementáció:** `app/src/debug/java/com/example/data/network/MockNeptunDataSource.kt` – a teljes ~600 soros mock adatkészlet.
   - **Release csonk (No-Op):** `app/src/release/java/com/example/data/network/MockNeptunDataSource.kt` – azonos metódus szignatúrák, de minden metódusa `emptyList()`-et ad vissza vagy kivételt dob, egyetlen betűnyi mock adat vagy tesztnév nélkül!
2. **Debug Eszközök és Fejlesztői Menü (`DebugFeatures`):**
   - **Debug implementáció:** `app/src/debug/java/com/example/core/debug/DebugFeatures.kt`:
     - `val isDebug: Boolean = true`
     - `val isDemoAllowed: Boolean = true`
     - `fun init(context: Context)` -> StrictMode beállítása (VmPolicy és ThreadPolicy).
     - Fejlesztői menü komponensek: maszkolt hálózati/session napló néző, feature flag-ek, verzió és build-dátum kijelzés.
   - **Release implementáció (No-Op):** `app/src/release/java/com/example/core/debug/DebugFeatures.kt`:
     - `val isDebug: Boolean = false`
     - `val isDemoAllowed: Boolean = false`
     - `fun init(context: Context) {}` (üres)
     - Fejlesztői menü Composable: `@Composable fun DevMenu(...) {}` (üres no-op).
3. **Alkalmazás belépési pont:**
   - [app/src/main/java/com/example/NeptunApp.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/NeptunApp.kt):
     - `onCreate()`-ben: `DebugFeatures.init(this)`. Debugban bekapcsolja a StrictMode-ot, release-ben no-op.
4. **Erőforrások (Debug megkülönböztetés):**
   - `app/src/debug/res/values/strings.xml`:
     - `<string name="app_name">NeptunMobile Debug</string>`
   - `app/src/debug/res/drawable/` vagy `app/src/debug/res/mipmap-anydpi-v26/`:
     - Debug ikon overlay vagy jelvénnyel ellátott háttér/foreground, hogy a launcherben azonnal látható legyen a különbség.

#### C) In-App Frissítő Logika és Típusbiztonság
1. **[app/src/main/java/com/example/core/update/AppUpdateManager.kt](file:///C:/Users/danit/Downloads/NeptunMobile-variants/app/src/main/java/com/example/core/update/AppUpdateManager.kt)**:
   - **Szigorú Asset Szűrés (`selectBestAsset`):**
     - Debug alkalmazás: **CSAK** olyan APK-t fogad el, amely nevében szerepel a `debug` kifejezés (pl. `NeptunMobile-1.2.0-debug.apk` vagy `app-debug.apk`). Ha nem talál, `null`-t ad vissza (nem fallbackel a release-re!).
     - Release alkalmazás: **CSAK** olyan APK-t fogad el, amely nevében szerepel a `release` kifejezés (pl. `NeptunMobile-1.2.0-release.apk` vagy `app-release.apk`) ÉS NEM szerepel benne a `debug`. Ha nem talál, `null`-t ad vissza (nem fallbackel a debugra!).
   - **Verzió Összehasonlítás (`ParsedVersion`):**
     - Kibővítés a debug és pre-release utótagok helyes kezelésére:
       - Formátum támogatás: `v1.2.0`, `1.2.0-debug+105`, `1.2.0-dev.45`.
       - Ha a verziószám (Major.Minor.Patch) azonos, de van build szám (pl. `+105` vs `+104`), a magasabb build szám tekintendő újabbnak.
       - A release stabilabb / újabb, mint az azonos számú debug/dev (kivéve ha a debug build száma magasabb az összehasonlított debug buildnél).
   - **GitHub API Cache és Rate-Limit Védelem:**
     - Cache bevezetése: a legutóbbi sikeres API válasz és időbélyeg elmentése (memóriában vagy SharedPreferences-ben).
     - Minimális lekérdezési ablak: automatikus ellenőrzésnél legalább 30 percen belül nem hívja újra a hálózatot, a cache-elt eredményt adja vissza.
     - HTTP fejlécek: ETag (`If-None-Match`) küldése. Ha `304 Not Modified`, a korábbi release adatot használja (a 304 nem fogyasztja a GitHub 60 kérés/óra névtelen limitjét!).
     - Rate-limit figyelés: HTTP 403 / 429 esetén nem dob összeomlást okozó kivételt, hanem logolja az `x-ratelimit-remaining` / `x-ratelimit-reset` értékeket és csendesen `null`-t ad vissza.

#### D) Maszkolt Hálózati és Session Napló a Fejlesztői Menüben
1. **`DebugSessionLogger` (kizárólag `src/debug`-ban):**
   - Nem nyúlunk a `NeptunApiClient.kt`-hez! Ehelyett a debug source setben létrehozott logger a debug menü számára gyűjti az eseményeket és a hibákat egy körkörös pufferbe (pl. utolsó 100 bejegyzés).
   - **Kötelező szigorú maszkolási szabályok:**
     - Jelszavak (`password=...`, `"password":"..."`) -> `***MASKED***`
     - Munkamenet tokenek (`sessionToken`, `Bearer ...`) -> `***MASKED***`
     - Cookie-k (`ASP.NET_SessionId=...`, `.ASPXAUTH=...`, `NeptunSession=...`) -> `CookieName=***MASKED***`
     - 2FA kódok (`TOTPCode`, `EmailCode`, 6-jegyű numerikus kódok) -> `***MASKED***`
   - Release APK-ban: a release `DebugFeatures` osztály nem tartalmaz puffert, nem tárol naplókat.

#### E) Release Tisztaság Ellenőrzése (Gradle Task / Unit Teszt)
1. **`ReleaseCleannessTest.kt` (Új teszt az `app/src/test/` alatt):**
   - Automatizált ellenőrzés, amely közvetlenül vizsgálja a `com.example.data.network.MockNeptunDataSource` release implementációját vagy a lefordított osztályokat.
   - Ellenőrzi, hogy:
     - A release `MockNeptunDataSource` nem tartalmaz-e demó szövegeket (`BMEVIIIM01`, `Demó Hallgató`, `IB025`, `cal_1`).
     - A release `DebugFeatures.isDebug` értéke szigorúan `false`.
     - A release `DebugFeatures.isDemoAllowed` értéke szigorúan `false`.

#### F) CI/CD Folyamat (.github/workflows/ci.yml)
1. **[.github/workflows/ci.yml](file:///C:/Users/danit/Downloads/NeptunMobile-variants/.github/workflows/ci.yml):**
   - A `Restore Keystore for Consistent Signing` lépés leegyszerűsítése: a repóban rögzített `debug.keystore`-t használja a debug építéshez.
   - Az `assembleDebug` és `assembleRelease` feladatok mindkét variánst megépítik.
   - **Asset Elnevezési Séma a GitHub Release-hez:**
     - Release APK: `NeptunMobile-${VERSION_NAME}-release.apk`
     - Debug APK: `NeptunMobile-${VERSION_NAME}-debug.apk`
   - A release jegyzetekben és a release artifactek között mindkét APK elérhetővé válik, így az in-app frissítő a megfelelő variánst tudja letölteni.

---

## 4. Kifejezetten KIHAGYOTT (Auth / Security) Fájlok és Indoklás

A párhuzamosan futó `fix/elte-session` munka megzavarásának és merge-konfliktusoknak a megelőzése érdekében az alábbi fájlokat **NEM MÓDOSÍTJUK**:

| Fájl | Felelősség | Miért nem módosítjuk? |
|---|---|---|
| `app/src/main/java/com/example/data/network/NeptunApiClient.kt` | HTTP kliens session, ELTE bejelentkezési folyamat, cookie-k | A párhuzamos ág elsődleges javítási célpontja. A debug naplózást source set szinten, külsőleg illesztjük. |
| `app/src/main/java/com/example/data/repository/AuthRepositoryImpl.kt` | Bejelentkezés, hitelesítés, session perzisztálás | Közvetlen auth réteg. A benne lévő `DEMO01` ág nem okoz problémát, mivel release-ben a `MockNeptunDataSource` üres csonk, így valódi demó adatok nem jelenhetnek meg. |
| `app/src/main/java/com/example/core/security/EncryptedPreferencesManager.kt` | Titkosított hitelesítő adatok, session tárolás | Szigorú security réteg. A debug és release automatikusan külön Keystore aliasokat és privát app sandboxot kap az Android OS szintjén a `.debug` suffix miatt. |
| `app/src/main/java/com/example/presentation/ui/screens/LoginScreen.kt` | Bejelentkező felület és 2FA UI | Auth UI. Nem nyúlunk a belső input logikájához; a demó kitöltés gomb és jelvény megjelenítését kívülről (`MainAppContent` / `DebugFeatures`) vezéreljük. |

---

## 5. Asset- és Verzióséma Részletesen

### 5.1. Asset Nevek
* **Release APK:** `NeptunMobile-<verzió>-release.apk` (pl. `NeptunMobile-0.2.0-release.apk`)
* **Debug APK:** `NeptunMobile-<verzió>-debug.apk` (pl. `NeptunMobile-0.2.0-debug.apk`)

### 5.2. Verzió- és Csatornaképzés
* **Stabil kiadások (`stable` ág):**
  - Verzió: SemVer szerinti `X.Y.0` (pl. `0.2.0`).
  - Csatorna: `STABLE`.
* **Fejlesztői kiadások (`main` vagy egyéb ág):**
  - Verzió: `X.Y.Z-dev+<build>` vagy `X.Y.Z-debug+<build>` (pl. `0.1.15-debug+42`).
  - Csatorna: `DEV`.
* **Verzió-összehasonlítási szabályzat (`AppUpdateManager.ParsedVersion`):**
  1. `major > other.major` -> újabb.
  2. `minor > other.minor` -> újabb.
  3. `patch > other.patch` -> újabb.
  4. Ha `major`, `minor`, `patch` megegyezik:
     - Ha mindkettő tartalmaz `buildNumber`-t (`+42` vs `+41`), a nagyobb szám a nyerő.
     - A release (utótag nélküli) újabbnak számít, mint a pre-release/debug azonos számmal.

---

## 6. Kockázatok és Enyhítési Stratégiák

| Kockázat | Súlyosság | Enyhítési Stratégia |
|---|---|---|
| **Aláírás-eltérés frissítéskor (debug)** | Magas | A repóba helyezett, verziókövetett, rögzített `debug.keystore` használata lokálisan és a CI-ben egyaránt. |
| **Release build véletlenül debug kulccsal íródik alá** | Magas | A release `signingConfig` veszélyes debug fallbackjének eltávolítása a Gradle scriptből; CI-ben kötelező secret meglét. |
| **Auth merge konfliktus a másik sessionnel** | Magas | Szigorú elhatárolás: `NeptunApiClient.kt`, `AuthRepositoryImpl.kt` és `EncryptedPreferencesManager.kt` érintetlenül hagyása. |
| **GitHub API Rate Limit túllépés** | Közepes | 30 perces minimális lekérdezési ablak, ETag (`If-None-Match`) küldése, 403 / 429 kódok elegáns lekezelése. |
| **LeakCanary bekerül a release APK-ba** | Magas | Kizárólag `debugImplementation` konfiguráció használata a `build.gradle.kts`-ben; a release build classpath-járól fizikailag hiányzik. |
| **Keresztreferenciás telepítési hiba (`CONFLICTING_PROVIDER`)** | Magas | Minden provider és authority `${applicationId}` dinamikus változót használ; a debug `.debug.fileprovider` authority-t kap. |

---

## 7. Kézi Tesztelési Forgatókönyvek (Átadási Lista)

Mivel a fizikai eszközre vagy emulátorra való adb-s telepítés a párhuzamos session miatt tilos, a tesztelést a jóváhagyás és elkészülés után az alábbi forgatókönyvek alapján kell végrehajtani:

1. **Párhuzamos Telepítés és Név/Ikon Ellenőrzés:**
   - Telepítsd fel a `NeptunMobile-<verzió>-release.apk`-t és a `NeptunMobile-<verzió>-debug.apk`-t ugyanarra a készülékre.
   - *Elvárt eredmény:* Mindkét app hibátlanul feltelepül (`INSTALL_FAILED_CONFLICTING_PROVIDER` nélkül). A kezdőképernyőn két külön app látható: "Neptun Mobile" és "NeptunMobile Debug", a debug ikonon világos jelvénnyel.
2. **Adatszeparáció és Sandbox Védelem:**
   - Jelentkezz be a Release appba valódi fiókkal.
   - Nyisd meg a Debug appot, és válassz demó bejelentkezést (`DEMO01`).
   - *Elvárt eredmény:* A két alkalmazás adatai (fiók, beállítások, órarend, offline cache) teljes mértékben elkülönülnek, egymás adatait nem látják és nem írják felül.
3. **In-App Frissítő Típushelyessége:**
   - Indítsd el a Release appot és keress frissítést.
   - *Elvárt eredmény:* Csak a release APK-t tölti le, ha csak debug érhető el, "Nincs elérhető új verzió"-t jelez.
   - Indítsd el a Debug appot és keress frissítést.
   - *Elvárt eredmény:* Csak a debug APK-t tölti le és telepíti.
4. **Debug Auto-Update Aláírás Ellenőrzés:**
   - Telepíts egy korábbi debug APK-t, majd frissíts a legújabb debug APK-ra az in-app frissítővel.
   - *Elvárt eredmény:* Az APK letöltődik, a csomagnév és az aláírás ellenőrzése sikeresen lefut (nem dob "Aláírás-eltérés" hibát), és a frissítő párbeszédablak megnyílik.
5. **Naptár (.ics) Exportálás Mindkét Variánsban:**
   - Exportálj egy órát naptárba a Release appból, majd a Debug appból.
   - *Elvárt eredmény:* A FileProvider mindkét appban a saját csomagnevét használja (`app.neptun.yrklqi.fileprovider` és `app.neptun.yrklqi.debug.fileprovider`), a rendszer naptáralkalmazása sikeresen megnyitja a megosztott `.ics` fájlt.
6. **Főképernyős Widget (TodayWidget):**
   - Helyezz ki egy órarendi widgetet a Release apphoz, és egy másikat a Debug apphoz.
   - *Elvárt eredmény:* Mindkét widget a saját appjának adatait jeleníti meg, a widgetekre kattintva a megfelelő app nyílik meg.
7. **Fejlesztői Menü és Maszkolás Ellenőrzése (Debug):**
   - Nyisd meg a Debug app Beállítások képernyőjét és a Fejlesztői Menüt.
   - Vizsgáld meg a hálózati/session naplót.
   - *Elvárt eredmény:* A jelszavak, tokenek, cookie értékek és 2FA kódok sehol sem látszanak nyers szövegként (`***MASKED***` maszkolással jelennek meg). Release appban a Fejlesztői Menü nem létezik.

---

## 8. Merge-sorrend Javaslat a Másik Sessionnel

1. **Először:** A `fix/elte-session` ág befejezése és tesztelése az auth / ELTE munkamenet javítására.
2. **Másodszor:** A `feat/build-variants` ágon elkészült build-változatok és frissítő merge-elése.
3. **Konfliktusmentesség garanciája:** Mivel a `feat/build-variants` nem érinti a `NeptunApiClient.kt`, `AuthRepositoryImpl.kt` és `EncryptedPreferencesManager.kt` fájlokat, a két ág tiszta, triviális merge-t eredményez.
