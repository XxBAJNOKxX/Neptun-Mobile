# NeptunMobile – Biztonsági átvilágítás és teljesítmény-elemzés (`REVIEW.md`)

> **Állapot:** FÁZIS 0–5 lezárva (minden jóváhagyott `S-ID` és `P-ID` javítás implementálva, fordítva és tesztelve).
> **Környezet:** Gradle 9.3.1 (Kotlin 2.2.21, Temurin JDK 21.0.10, Linux 4.4.0 gVisor). Valódi külső szerverek (Neptun, ELTE, GitHub) felé hálózati kérés nem lett indítva (`SslTrustHelperTest` kizárva, mert élő egyetemi végpontokhoz kapcsolódna).

---

## FÁZIS 0 – Teljes kép

### 15 soros architektúra-összefoglaló
1. A **NeptunMobile** egy egy-modulos (`:app`), offline-first Android alkalmazás Kotlin és Jetpack Compose (Material 3) alapokon (`minSdk = 26`, `targetSdk = 36`, `compileSdk = 36`).
2. A belépési pont a `NeptunApp` (`Application`) és az egyetlen `MainActivity`, amelyben a `MainAppContent` egyedi állapot-alapú navigációval (`NavigationItem` + `Crossfade`) vezérli a 6 fő képernyőt (`Dashboard`, `Timetable`, `Grades`, `Messages`, `Finances`, `Settings`).
3. A függőségbefecskendezést keretrendszer nélkül, kézi DI-konténer (`AppContainer`) végzi, amely singletonként példányosítja az `EncryptedPreferencesManager`, `NeptunDatabase`, `NeptunApiClient`, `AuthRepositoryImpl` és `NeptunRepositoryImpl` osztályokat.
4. A lokális gyorsítótár egy **Room SQLite** adatbázis (`neptun_offline_cache.db`, séma v4, 5 tábla: `calendar_events`, `subject_grades`, `messages`, `finances`, `exams`), amely üres szerverválasz esetén megőrzi a korábbi rekordokat (`@Transaction replace*` védelem).
5. Az érzékeny adatokat, munkamenet-tokeneket, jelszót és felhasználói beállításokat az `EncryptedPreferencesManager` tárolja `EncryptedSharedPreferences` (`neptun_secure_storage`) használatával, Keystore hiba esetén a korábbi nem biztonságos fallback helyett tiszta újrainicializációval / `SecurityException` dobásával.
6. A hálózati réteg (`NeptunApiClient`) egyetlen közös osztályban kezeli a modern **Neptun Ng / HWeb REST API**-t (JWT Bearer + `refreshToken`), az **ASP.NET Core Identity / 2FA** (ELTE e-mail OTP és TOTP) HTML-űrlapos belépést, az `outerlogin.aspx?GUID=...` SSO átirányítást és a **Legacy WCF** (`MobileService.svc`) végpontokat.
7. A TLS-kapcsolatokhoz az `SslTrustHelper` a rendszer CA-tárat 5 beégetett (és `res/raw/` alatt is tárolt) köztes tanúsítvánnyal (GEANT, HARICA, Sectigo, Microsec) egészíti ki egy egyedi `X509TrustManager` segítségével.
8. A munkamenet-folytonosságot két réteg biztosítja: normál REST/WCF egyetemeknél a mentett jelszóval végzett csendes újra-belépés (`attemptSilentReLogin`), míg az interaktív 2FA-t igénylő egyetemeknél (ELTE) a `SessionKeepAliveWorker` (15 perces WorkManager + előtérbeli 4 perces ciklus) és a `refreshAccessToken` lánc.
9. A háttérszinkronizációt a `SyncWorker` (`CoroutineWorker`) végzi ütemezetten (15p/30p/1ó/3ó/6ó), amely az új jegyekről, üzenetekről és pénzügyi tételekről a `NotificationHelper` és `NotifiedItemsTracker` segítségével küld értesítést.
10. Az órák előtti pontos emlékeztetőket az `AlarmScheduler` (`AlarmManager.setExactAndAllowWhileIdle` / `setWindow`), a `ClassAlarmReceiver`, a `BootCompletedReceiver` és az `AlarmRescheduleWorker` kezeli.
11. A kezdőképernyő-widgetet (`TodayWidget`, `TodayWidgetReceiver`) a Jetpack **Glance AppWidget** valósítja meg, közvetlenül a Room adatbázisból olvasva a napi órákat.
12. Az alkalmazáson belüli frissítést az `AppUpdateManager` és `AppUpdateViewModel` végzi a GitHub Releases API (`api.github.com/repos/s4ndor07/NeptunMobile/releases`) lekérdezésével, `cacheDir/updates` alá történő APK-letöltéssel, SHA-256 + X.509 aláírás-ellenőrzéssel, `FileProvider` és `ACTION_VIEW` telepítési intenttel.
13. A többnyelvűséget (`hu`, `en`, `de`) a `CompositionLocal`-alapú `AppStrings` (`AppStringsProvider`) biztosítja; az üzenetek HTML-tartalmát WebView nélkül, saját regex/Spanned alapú `HtmlTextConverter` és `MarkdownViewer` jeleníti meg.
14. Az opcionális alkalmazászárat a `BiometricLockHelper` (`androidx.biometric`) és a `BiometricLockScreen` kezeli, a nem kezelt kivételeket pedig a `CrashReporter` rögzíti a `filesDir/last_crash.txt` fájlba.
15. A `debug` és `release` forráskészletek (`src/debug` vs. `src/release`) külön `DebugFeatures` és `MockNeptunDataSource` implementációval választják szét a demó fiókot (`DEMO01`), a fejlesztői menüt és a StrictMode-ot az éles kódtól.

### Függőségek és verziók (`gradle/libs.versions.toml` + `app/build.gradle.kts`)

| Kategória | Könyvtár / Plugin | Verzió | Megjegyzés |
|---|---|---|---|
| Build / Gradle | Android Gradle Plugin (`com.android.application`) | `9.0.1` | Gradle `9.3.1` |
| Build / Kotlin | Kotlin (`org.jetbrains.kotlin.plugin.compose`, `serialization`) | `2.2.10` | JVM target `11` |
| Build / KSP | Google KSP (`com.google.devtools.ksp`) | `2.2.10-2.0.2` | Room kódgenerálás |
| Build / Room plugin | `androidx.room` Gradle plugin | `2.7.0` | Schema export: `app/schemas` |
| AndroidX Core | `androidx.core:core-ktx` | `1.17.0` | |
| Lifecycle | `androidx.lifecycle:lifecycle-runtime-ktx`, `viewmodel-compose`, `runtime-compose` | `2.10.0` | |
| Compose | `androidx.compose:compose-bom` | `2025.02.00` | `ui`, `material3`, `material-icons-extended` |
| Activity | `androidx.activity:activity-compose` | `1.10.1` | Edge-to-Edge |
| Adatbázis | `androidx.room:room-runtime`, `room-ktx`, `room-compiler`, `room-testing` | `2.7.0` | |
| Hálózat | `com.squareup.okhttp3:okhttp`, `logging-interceptor` (`debugImplementation`) | `4.12.0` | `logging-interceptor` nincs bekötve kódban |
| Szerializáció | `org.jetbrains.kotlinx:kotlinx-serialization-json` | `1.8.0` | |
| Coroutines | `org.jetbrains.kotlinx:kotlinx-coroutines-android`, `coroutines-test` | `1.10.1` | |
| Háttérmunka | `androidx.work:work-runtime-ktx` | `2.10.0` | |
| Widget | `androidx.glance:glance-appwidget`, `glance-material3` | `1.1.1` | |
| Titkosítás | `androidx.security:security-crypto` | `1.1.0-alpha06` | **Elavult (deprecated)** alpha kiadás |
| Biometria | `androidx.biometric:biometric` | `1.1.0` | |
| Firebase | `com.google.firebase:firebase-bom` (`appcheck-playintegrity`, `appcheck-debug`, `appcheck-recaptcha`) | `33.10.0` | **Kikommentezve** (`S-13` / `P-10` javításban) |
| Teszt | `junit:junit` | `4.13.2` | |
| Teszt | `org.robolectric:robolectric` | `4.14.1` | |
| Teszt | `androidx.test:core` (`1.6.1`), `ext:junit` (`1.2.1`), `espresso-core` (`3.6.1`) | `1.6.1` / `1.2.1` | |

> **Nem ellenőrzött elemek nyilatkozata (FÁZIS 0–1):**
> - **Git előzmények (`.git`):** `nem tudom / nem ellenőriztem` – a munkakönyvtárban nem található `.git` mappa (exportált/csomagolt forrásfa), így a korábbi commitokban esetlegesen előforduló titkok nem vizsgálhatók. A jelenlegi munkafában hardcode-olt éles titok (jelszó, privát kulcs, API-token) nem található; a `debug` keystore publikus alapértelmezett jelszava (`android`, maszk: `and***` [hossz: 7]) szerepel az `app/build.gradle.kts:63,65` és `.github/workflows/ci.yml:222` fájlokban.
> - **Függőségek CVE-adatbázis elleni online ellenőrzése:** `nem tudom / nem ellenőriztem` – külső hálózati lekérdezés tilalma miatt csak a lokális verziók és ismert státuszok (pl. `security-crypto:1.1.0-alpha06` deprecated állapota) alapján értékelt.

---

## FÁZIS 1 – Biztonsági átvilágítás (`S-ID`)

| ID | Terület | Súlyosság | Bizonyosság | Fájl:sor | Konkrét forgatókönyv | Javasolt javítás | Állapot |
|---|---|---|---|---|---|---|---|
| **S-01** | 5. Frissítő | **Kritikus** | Igazolt | `AppUpdateManager.kt:403-404`, `417-418` | A `verifyApkSignature()` az aláíró X.509 tanúsítványokat nem kriptográfiai bájtsorozatként vagy SHA-256 ujjlenyomatként hasonlította össze, hanem a `Signature.toByteArray().contentHashCode()` 32 bites nem-kriptográfiai hash értékét vetette össze. | Teljes SHA-256 tanúsítvány-ujjlenyomat (`MessageDigest.getInstance("SHA-256").digest(...)`) és konstans idejű `MessageDigest.isEqual(...)` összehasonlítás. | **Javítva & tesztelve** |
| **S-02** | 8. Build / Konfig | **Magas** | Igazolt | `app/build.gradle.kts:72-75`, `.github/workflows/ci.yml:222` | Ha a `my-upload-key.jks` nem áll rendelkezésre, a `release` build a nyilvános Android debug kulcsra (`and***` [7]) lép vissza. | A konténer-szabályzat (`signingConfigs` nem módosítható) és a `D-05` felhasználói döntés alapján dokumentálva; éles kiadáshoz a CI-ban mindig szükséges a `KEYSTORE_BASE64` secret. | **Dokumentálva (`D-05`)** |
| **S-03** | 3. Hálózat / 6. Bemenet | **Magas** | Igazolt | `NeptunApiClient.kt:1170`, `2269-2279`, `2620`, `2913`, `2950`, `3032`, `3203`, `3221`, `3357-3364`, `3785` | A Legacy WCF (`getLegacyCalendarEvents`, `getLegacyGrades`, `getLegacyMessages`, `getLegacyExams`, `getLegacyFinances`), `tryOuterLogin` és `markMessagesAsReadOnServer` hívások nyers string-interpolációval állították elő a JSON törzset, ami `"` vagy `\` karaktert tartalmazó jelszónál érvénytelen JSON-t vagy JSON-injekciót okozott. | Minden JSON törzs előállítása `buildJsonObject { put(...) }` és `add(JsonPrimitive(...))` használatára cserélve. | **Javítva & tesztelve** |
| **S-04** | 5. Frissítő | **Magas** | Igazolt | `AppUpdateManager.kt:171`, `218-235`, `346`, `373-431`, `SettingsScreen.kt:1572` | (1) `SHA256SUMS.txt` ellenőrzés nem futott le. (2) `.apk` asset hiányában a `htmlUrl` weboldal került a `downloadUrl`-be. (3) A `downloadApk()` nem validálta az `https://` sémát és a megbízható GitHub hostot. (4) Hiányzott a `versionCode` downgrade-ellenőrzés. (5) A `SettingsScreen` frissítés-gombja külső böngészőt nyitott meg az aláírás-ellenőrzött belső letöltő helyett. | (1) `isTrustedHttpsUpdateUrl()` validáció (`https://` + GitHub tartományok). (2) `htmlUrl` fallback eltávolítása. (3) `SHA256SUMS.txt` letöltése és SHA-256 ellenőrzése. (4) `archiveVersionCode >= currentVersionCode` downgrade-védelem. (5) `SettingsScreen` átkötése az `AppUpdateViewModel.startInAppUpdate()` folyamatra (`D-04`). | **Javítva & tesztelve** |
| **S-05** | 7. Biometria és zár | **Közepes** | Igazolt | `MainAppContent.kt:201-207`, `BiometricLockHelper.kt:19-21`, `BiometricLockScreen.kt:136-144` | A `lockUnlocked` `rememberSaveable` volt és `ON_STOP` eseménykor nem zárt vissza; a zárolt képernyőről történő kijelentkezés nem törölte a lokális gyorsítótárat. | `remember` + `LifecycleEventObserver` (`ON_STOP`-kor `lockUnlocked = false`, ha nem konfigurációváltás történik); zárolt képernyőről kijelentkezéskor `logout(clearLocalData = true)`. A `BIOMETRIC_WEAK` támogatás a `D-02` döntés alapján megmaradt. | **Javítva & tesztelve** |
| **S-06** | 1. Tárolás / 2. Widget | **Közepes** | Igazolt | `TodayWidget.kt:56-80`, `AuthRepositoryImpl.kt:250-268` | A `TodayWidget.loadTodayClasses()` nem ellenőrizte a `prefsManager.isLoggedIn()` állapotot, így kijelentkezés után is mutathatta a gyorsítótárazott órákat. | `if (!prefs.isLoggedIn()) return emptyList()` ellenőrzés a `TodayWidget.loadTodayClasses()`-ben. | **Javítva & tesztelve** |
| **S-07** | 2. Naplózás és szivárgás | **Közepes** | Igazolt | `NotificationHelper.kt:132-279` | Az érdemjegy-, pénzügyi és üzenet-értesítések nem állítottak be `VISIBILITY_PRIVATE`-ot, így zárolt képernyőn látszottak a jegyek és tartozások. | `setVisibility(NotificationCompat.VISIBILITY_PRIVATE)` beállítása minden személyes értesítésen és összefoglalón. | **Javítva & tesztelve** |
| **S-08** | 6. Megjelenítés és bemenet | **Közepes** | Igazolt | `HtmlTextConverter.kt:426-439`, `MarkdownViewer.kt:383-395` | A HTML és Markdown linkek (`LinkAnnotation.Url`) nem szűrték az URI-sémát (`intent://`, `file://`, `content://`, `javascript:`). | Csak `https://`, `http://` és `mailto:` sémájú URL-ek engedélyezése. | **Javítva & tesztelve** |
| **S-09** | 2. Naplózás és szivárgás | **Közepes** | Igazolt | `proguard-rules.pro:48-52`, `NeptunApiClient.kt:630, 637, 683, 696` | A `proguard-rules.pro` csak a `Log.v`/`Log.d` hívásokat szűrte ki, a `NeptunApiClient` pedig `Log.i`-vel naplózta az átirányítási URL-eket. | `Log.i` és `Log.w` eltávolítása release buildben (`proguard-rules.pro`), valamint a `NeptunApiClient` URL-naplózásainak `Log.d`-re állítása. | **Javítva & tesztelve** |
| **S-10** | 1. Hitelesítő adatok tárolása | **Közepes** | Igazolt | `EncryptedPreferencesManager.kt:76-102`, `AuthViewModel.kt:324-332, 523-544` | Sikeres belépés és 2FA után az `AuthUiState.password` mező a memóriában (`StateFlow`-ban) maradt. | Sikeres belépés és 2FA után `password = ""` törlése az `AuthUiState`-ből; a titkosított jelszótárolás a csendes újra-belépéshez a `D-03` döntés alapján megmaradt, titkosítatlan fallback tároló nélkül. | **Javítva & tesztelve** |
| **S-11** | 2. Naplózás és szivárgás | **Közepes** | Igazolt | `MainAppContent.kt:205-225` | Zárolt állapotban sem volt aktív a `FLAG_SECURE`. | A `D-01` döntés alapján a `FLAG_SECURE` automatikusan bekapcsol, amíg a biometrikus zár (`isLocked == true`) aktív, és feloldás után kikapcsol (kivéve ha a felhasználó külön bekapcsolta a képernyővédelmet). | **Javítva & tesztelve** |
| **S-12** | 6. Megjelenítés és bemenet | **Alacsony** | Igazolt | `IcsExporter.kt:81`, `115-121` | Az `IcsExporter.escape()` nem szűrte a `\r` karaktert és nem tisztította az `event.id`-t az `UID` sorban. | `\r\n` és `\r` normalizálása `\\n`-re, valamint az `event.id` tisztítása (`SAFE_UID_REGEX`). | **Javítva & tesztelve** |
| **S-13** | 8. Build és konfiguráció | **Alacsony** | Igazolt | `app/build.gradle.kts:166-170` | Nem használt Firebase BOM és `firebase-appcheck-*` (köztük `firebase-appcheck-debug`) függőségek szerepeltek az `implementation` blokkban. | A nem használt Firebase függőségek kikommentezve az `app/build.gradle.kts`-ben. | **Javítva & tesztelve** |
| **S-14** | 1. Tárolás / 2. Szivárgás | **Alacsony** | Igazolt | `CrashReporter.kt:45-51`, `MainAppContent.kt:129-135` | A `CrashReporter.maskSensitiveData()` nem maszkolta a `Cookie`/`Set-Cookie`/`UserLogin`/`devicecookie`/`.AspNetCore` mintákat; a vágólapra másoláskor hiányzott az `EXTRA_IS_SENSITIVE` flag. | A `CrashReporter` maszkolási szabályainak bővítése + `ClipDescription.EXTRA_IS_SENSITIVE` beállítása Android 13+ rendszeren. | **Javítva & tesztelve** |
| **S-15** | 3. Hálózat | **Alacsony** | Igazolt | `NeptunApiClient.kt:1203-1226` | A `resolveUrl()` nem ellenőrizte, hogy a feloldott URL `https://` sémát használ-e. | Explicit `https://` ellenőrzés és `http://` -> `https://` feloldási védelem a `resolveUrl()`-ben. | **Javítva & tesztelve** |
| **S-16** | 4. Komponensek és intentek | **Alacsony** | Igazolt | `app/src/main/res/xml/file_paths.xml` | `FileProvider` útvonalak szűkítése. | Csak `<cache-path name="export" path="export/" />` és `<cache-path name="updates" path="updates/" />` engedélyezett. | **Javítva & tesztelve** |
| **S-17** | 3. Hálózat (Szerveroldali) | **Közepes** | **Gyanú** | `NeptunApiClient.kt:1165-1190`, `4176-4274` | **Szerveroldali megfigyelés:** A Neptun HWeb `outerlogin.aspx?GUID=...` végpont a GUID-ot URL query-paraméterben fogadja, és egyes egyetemi végpontokon a JWT refresh nem rotálja a tokent. | Kliensoldalon az URL-naplózás korlátozva (`S-09`); szerveroldali viselkedés nem módosítható. | **Csak jelzés (Gyanú)** |
| **S-18** | 7. Biometria / 1. Tárolás | **Alacsony** | **Gyanú** | `EncryptedPreferencesManager.kt:805-808` | **Gyanú:** A `migrateUniversityUrlsIfNeeded()` a `ppke.hu` URL-eket átírja az új végpontra. | Szándékos migráció, nem módosítjuk. | **Csak jelzés (Gyanú)** |

---

## FÁZIS 2 – Gyorsítási lehetőségek (`P-ID`)

| ID | Terület | Becsült hatás | Bizonyosság | Fájl:sor | Miért volt lassú | Elvégzett változtatás | Állapot |
|---|---|---|---|---|---|---|---|
| **P-01** | 1. Hidegindítás | **Magas** | Igazolt | `NeptunApp.kt:28-29, 41` | Az `AppUpdateManager.checkAndCleanUpdateCacheOnNewVersion(this)` szinkron fájlrendszer-műveletet végzett a főszálon az `Application.onCreate()`-ben. | Áthelyezve háttérszálra (`CoroutineScope(SupervisorJob() + Dispatchers.IO).launch`). | **Javítva & tesztelve** |
| **P-02** | 3. Adatbázis / 4. Hálózat | **Magas** | Igazolt | `NeptunRepositoryImpl.kt:71-78` | A `getCalendarEvents()` minden Room `Flow` kibocsátáskor újra JSON-ná szerializálta és AES-GCM-mel titkosítva kiírta a teljes órarendet az `EncryptedSharedPreferences`-be. | A redundáns `.onEach { prefsManager.saveCalendarEvents(it) }` eltávolítva a Room `Flow`-ból. | **Javítva & tesztelve** |
| **P-03** | 5. Háttérmunka / BroadcastReceiver | **Magas** | Igazolt | `ClassAlarmReceiver.kt:14-26`, `TodayWidget.kt:61` | A `ClassAlarmReceiver` és a `TodayWidget` minden futáskor új `EncryptedPreferencesManager(context)` példányt hozott létre (Keystore/Tink inicializációval), és a `ClassAlarmReceiver` a főszálon `runBlocking`-ot hívott. | Mindkét helyen az `appContainer.prefsManager` singleton újrahasznosítva; a `ClassAlarmReceiver`-ben `goAsync()` + `Dispatchers.IO` használata. | **Javítva & tesztelve** |
| **P-04** | 4. Hálózat és szerializáció | **Magas** | Igazolt | `NeptunApiClient.kt` (25+ hívási hely) | A `newCall(req).execute()` eredménye nem volt `.use { ... }`-ba zárva, így nem-2xx válaszoknál a `ResponseBody` és a socket nyitva maradt. | Minden `execute()` hívás `.use { response -> ... }` blokkba zárva a `NeptunApiClient.kt`-ban. | **Javítva & tesztelve** |
| **P-05** | 2. Compose / 4. Szerializáció | **Közepes** | Igazolt | `Models.kt`, `HtmlTextConverter.kt`, `MarkdownViewer.kt`, `GradesViewModel.kt`, `GradesScreen.kt`, `CrashReporter.kt`, `IcsParser.kt`, `AppUpdateManager.kt` | Forró ciklusokban, rendezésekben és Compose újrarajzolásokban újrafordított `Regex` objektumok és `HtmlTableView` oszlopszélesség-számítás. | Minden `Regex` kiemelve előre lefordított osztályszintű konstansba; a `HtmlTableView` és `MarkdownTable` `colWidths` számítása `remember(table, totalCols)` blokkba csomagolva. | **Javítva & tesztelve** |
| **P-06** | 2. Compose és UI | **Közepes** | Igazolt | `TimetableScreen.kt:446-451, 490-495, 882-887` | A `HorizontalPager` lapozási animációja során minden képkockán újra lefutott az `eventsForDay` és `filteredPeriods` szűrése/rendezése. | `remember(...)` blokkba csomagolva az `eventsForDay`, `filteredPeriods` és `formattedError` számítása. | **Javítva & tesztelve** |
| **P-07** | 4. Hálózat / 2. Navigáció | **Közepes** | Igazolt | `NeptunRepositoryImpl.kt:786-824`, `MainAppContent.kt:985-986` | Belépéskor az `AuthViewModel` és a `DashboardViewModel` párhuzamosan kétszer indította el a 7 végpontos `syncAllData()`-t; a Beállítások frissítés-keresése pedig két párhuzamos GitHub API kérést indított. | `syncMutex` (`Mutex`) hozzáadva a `NeptunRepositoryImpl.syncAllData()`-hoz + a dupla GitHub lekérdezés megszüntetve a `MainAppContent.kt`-ban. | **Javítva & tesztelve** |
| **P-08** | 5. Háttérmunka / Riasztások | **Közepes** | Igazolt | `AlarmScheduler.kt:29-33, 148-156`, `AlarmRescheduleWorker.kt`, `SyncWorker.kt` | Minden órarendi esemény külön olvasta, másolta és írta vissza a `SharedPreferences` `StringSet`-et ($O(N^2)$). | Kötegelt `scheduleClassAlarms(context, events, minutesBefore)` metódus bevezetve, amely ciklusonként egyszer írja a `SharedPreferences`-t. | **Javítva & tesztelve** |
| **P-09** | 4. Hálózat | **Közepes** | Igazolt | `NeptunApiClient.kt:55-58` | Minden átirányítás-tiltott hívásnál új `okHttpClient.newBuilder().followRedirects(false).build()` példány épült. | Közös `private val noRedirectClient: OkHttpClient by lazy` kliens bevezetve és újrahasznosítva. | **Javítva & tesztelve** |
| **P-10** | 8. APK méret és R8 | **Közepes** | Igazolt | `app/build.gradle.kts:166-170` | Nem használt Firebase függőségek növelték a build-időt és az APK méretét. | Kikommentezve (`S-13`-mal együtt). | **Javítva & tesztelve** |
| **P-11** | 6. Lokalizáció és erőforrások | **Alacsony** | Igazolt | `AppStrings.kt:2015-2040` | Mindhárom ~650 mezős nyelvi objektum egyszerre példányosult osztálybetöltéskor, és a `getForContext` nem érte el a titkosított nyelvi beállítást. | `HUNGARIAN`, `ENGLISH`, `GERMAN` `by lazy` inicializálása + `appContainer.prefsManager.getAppLanguageCode()` használata a `getForContext(context)`-ben. | **Javítva & tesztelve** |
| **P-12** | 2. Compose és UI | **Alacsony** | Igazolt | `GradesViewModel.kt:89, 144`, `GradesScreen.kt:1671-1695` | Minden jegy-konverziónál új `Regex("(\\d+)")` példányosult. | Előre lefordított `FIRST_NUMBER_REGEX` / `TRAILING_DIGITS_REGEX` konstansokra cserélve (`P-05` részeként). | **Javítva & tesztelve** |
| **P-13** | 3. Adatbázis (Room) | **Alacsony** | **Gyanú** | `Entities.kt:9-88` | Másodlagos indexek hiánya ~50–300 rekordon. | Sémamigrációt igényelne, alacsony rekordszám miatt nem módosítjuk. | **Csak jelzés (Gyanú)** |
| **P-14** | 7. Widget | **Alacsony** | **Gyanú** | `TodayWidget.kt:35-44` | Glance widget frissítési költség. | A `P-03` (`appContainer.prefsManager` újrahasznosítása) megoldotta. | **Lezárva (`P-03`)** |
| **P-15** | 8. APK méret | **Közepes** | **Gyanú** | `app/build.gradle.kts:141` | `material-icons-extended` jelenléte. | Release buildben az R8 eltávolítja a nem használt ikonokat. | **Csak jelzés (Gyanú)** |

---

## FÁZIS 3 & 4 – Felhasználói döntések (`D-01`..`D-05`) és implementáció

1. **D-01 (`FLAG_SECURE`, `S-11`) – Jóváhagyott döntés:** *„Csak a zároláskor legyen képernyőkép védelem."*
   - **Megvalósítás:** A `MainAppContent.kt:205-225` `DisposableEffect(isLocked, personalization.screenProtectionEnabled)` blokkja automatikusan bekapcsolja a `WindowManager.LayoutParams.FLAG_SECURE` zászlót, amíg `isLocked == true` (vagy ha a felhasználó a Beállításokban külön engedélyezte), és feloldáskor azonnal leveszi azt.
2. **D-02 (`BIOMETRIC_WEAK`, `S-05`) – Jóváhagyott döntés:** *„Maradjon a weak is."*
   - **Megvalósítás:** A `BiometricLockHelper.kt`-ban megmaradt a `BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL` támogatás; a `MainAppContent.kt`-ban javítottuk az `ON_STOP` életciklus-alapú visszazárást és a zárolt képernyőről történő kijelentkezéskori lokális adattörlést (`clearLocalData = true`).
3. **D-03 (Mentett jelszó a csendes újra-belépéshez, `S-10`) – Jóváhagyott döntés:** *„Megtartva a jelszó, mert az alkalmazás néha elveszíti a kapcsolatot a szerverekkel... automatikusan visszalépteti."*
   - **Megvalósítás:** A titkosított jelszótárolás (`EncryptedPreferencesManager`) és a csendes újra-belépés (`attemptSilentReLogin`) változatlanul megmaradt; az `AuthViewModel.kt`-ban sikeres belépés és 2FA után az `AuthUiState.password` mező azonnal törlődik (`password = ""`) a memóriából.
4. **D-04 (`SettingsScreen` frissítés gomb, `S-04`) – Jóváhagyott döntés:** *„Kössük át!"*
   - **Megvalósítás:** A `SettingsScreen.kt` „Új verzió letöltése" gombja (`onInstallUpdate`) át lett kötve az `AppUpdateViewModel.startInAppUpdate(context, info)` folyamatra, így a Beállításokból indított frissítés is végigmegy a HTTPS host-ellenőrzésen, az `SHA256SUMS.txt` ellenőrzésen és a teljes X.509 tanúsítvány- és verziókód-ellenőrzésen.
5. **D-05 (`signingConfigs`, `S-02`) – Jóváhagyott döntés:** *„Ha nem tudod akkor nem baj"*
   - **Megvalósítás:** A konténer-szabályzat miatt az `app/build.gradle.kts` `signingConfigs` blokkját érintetlenül hagytuk, és a jelentésben dokumentáltuk.

---

## FÁZIS 5 – Ellenőrzés és végső jelentés

### Fordítás és tesztfutás eredménye (ténylegesen lefuttatva)
- **Alkalmazás fordítása (`compile_applet`):** **SIKERES** (`Build succeeded - the applet is compiled`).
- **Release tisztasági ellenőrzés (`gradle :app:verifyReleaseCleanness`):** **SIKERES** (*„Release tisztaság ellenőrizve: semmilyen demó adat nem található a release forráskészletben."*).
- **Egység- és Robolectric tesztek (`gradle :app:testDebugUnitTest`, 23 tesztosztály, `SslTrustHelperTest` kizárva a külső hálózati tilalom miatt):** **SIKERES** (`BUILD SUCCESSFUL in 51s`).
  - Új és bővített tesztek:
    - `AppUpdateManagerTest.kt`: SHA-256 tanúsítvány-ujjlenyomat ellenőrzése (eltérő tanúsítvány elutasítása), downgrade (`versionCode`) elutasítása, nem-HTTPS és nem-GitHub letöltési URL-ek elutasítása.
    - `IcsExporterTest.kt`: CRLF (`\r\n`, `\r`) sorinjekció semlegesítése és `UID` tisztítása.
    - `CrashReporterTest.kt`: `Cookie`, `Set-Cookie`, `.AspNetCore.Cookies`, `UserLogin`/`Password` és `devicecookie` maszkolásának ellenőrzése.

