package com.example.core.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.domain.model.NeptunLanguage

interface AppStrings {
    val languageName: String
    val languageCode: String

    // Navigation
    val navDashboard: String
    val navTimetable: String
    val navGrades: String
    val navMessages: String
    val navFinances: String
    val navSettings: String

    // General / Common
    val appName: String
    val save: String
    val cancel: String
    val close: String
    val copy: String
    val later: String
    val sessionExpiredTitle: String
    val sessionExpiredDesc: String
    val shareTimetableChooser: String
    val delete: String
    val edit: String
    val refresh: String
    val search: String
    val ok: String
    val yes: String
    val no: String
    val retry: String
    val error: String
    val success: String
    val loading: String
    val empty: String
    val all: String
    val details: String
    val select: String
    val back: String

    // Auth / Login
    val loginTitle: String
    val loginSubtitle: String
    val selectUniversity: String
    val searchUniversity: String
    val neptunCode: String
    val password: String
    val loginButton: String
    val loggingIn: String
    val demoLogin: String
    val offlineMode: String
    val offlineModeDesc: String
    val invalidNeptunCode: String
    val emptyPassword: String
    val languageSelectTitle: String
    val universityLanguageInfo: String

    // 2FA
    val twoFactorTitle: String
    val twoFactorSubtitle: String
    val twoFactorEmailCode: String
    val twoFactorTotpCode: String
    val twoFactorEnterCode: String
    val twoFactorVerify: String
    val twoFactorResendCode: String
    val twoFactorSuccess: String

    // Dashboard
    val dashboardGreeting: String
    val nextClass: String
    val todayClasses: String
    val noClassesToday: String
    val noMoreClassesToday: String
    val viewFullTimetable: String
    val recentMessages: String
    val unreadMessagesCount: (Int) -> String
    val quickStats: String
    val currentAverage: String
    val totalCredits: String
    val pendingFinancesCount: (Int) -> String

    // Timetable
    val timetableTitle: String
    val dayView: String
    val weekView: String
    val exportIcs: String
    val icsExportSuccess: String
    val weekA: String
    val weekB: String
    val monday: String
    val tuesday: String
    val wednesday: String
    val thursday: String
    val friday: String
    val saturday: String
    val sunday: String
    val room: String
    val instructor: String
    val courseCode: String

    // Grades
    val gradesTitle: String
    val weightedAverage: String
    val creditIndex: String
    val enrolledCredits: String
    val completedCredits: String
    val ghostGrades: String
    val ghostGradeSimulator: String
    val addGhostGrade: String
    val resetGhostGrades: String
    val gradeText5: String
    val gradeText4: String
    val gradeText3: String
    val gradeText2: String
    val gradeText1: String
    val newEntry: String
    val noGradesYet: String

    // Messages
    val messagesTitle: String
    val officialMessage: String
    val systemMessage: String
    val markAsRead: String
    val markAllAsRead: String
    val unreadOnly: String
    val searchMessages: String
    val noMessages: String
    val sender: String
    val date: String

    // Finances
    val financesTitle: String
    val statusPending: String
    val statusCompleted: String
    val statusOverdue: String
    val dueDate: String
    val paymentDate: String
    val amount: String
    val noFinances: String

    // Course Types
    val courseTypeLecture: String
    val courseTypePractice: String
    val courseTypeLab: String
    val courseTypeSeminar: String
    val courseTypeExam: String

    // Extra UI strings
    val inProgressClass: String
    val allMessagesRead: String

    // Settings
    val settingsTitle: String
    val sectionAccount: String
    val sectionLanguage: String
    val sectionTheme: String
    val sectionNotifications: String
    val sectionPersonalization: String
    val sectionDataCache: String
    val sectionAbout: String
    val logout: String
    val logoutConfirmTitle: String
    val logoutConfirmMessage: String
    val loggedInAs: String
    val university: String
    val neptunServer: String
    val languageDescription: String
    val languageSyncNeptun: String
    val languageChangedNotice: (String) -> String
    val themeMode: String
    val themeSystem: String
    val themeLight: String
    val themeDark: String
    val dynamicColors: String
    val accentColor: String
    val notifyMessages: String
    val notifyGrades: String
    val notifyFinances: String
    val notifyClasses: String
    val classReminderTime: String
    val quietHours: String
    val clearCache: String
    val cacheCleared: String
    val checkUpdates: String
    val appVersion: String
    val serverConnectionMode: String

    // Extended Settings Strings
    val profileSubtitle: String
    val studentDefaultName: String
    val studentDefaultUni: String
    val studentDefaultProgram: String
    val neptunCodeLabel: String
    val languageSubtitle: String
    val supportedLanguagesHeader: String
    val appearanceTitle: String
    val appearanceSubtitle: String
    val themeModeLabel: String
    val dynamicColorLabel: String
    val dynamicColorDesc: String
    val dynamicColorOnlyAndroid12: String
    val dynamicColorActiveBanner: String
    val accentColorTitle: String
    val accentColorSelected: String
    val customThemeBadge: String
    val livePreviewTitle: String
    val activeThemeLabel: String
    val btnPrimary: String
    val btnTonal: String
    val btnOutlined: String
    val notificationsTitle: String
    val notificationsSubtitle: String
    val notifPermissionGranted: String
    val notifPermissionRequired: String
    val notifPermissionGrantedDesc: String
    val notifPermissionRequiredDesc: String
    val grantPermissionBtn: String
    val notifClassesTitle: String
    val notifClassesDesc: String
    val notifGradesTitle: String
    val notifGradesDesc: String
    val notifMessagesTitle: String
    val notifMessagesDesc: String
    val notifFinancesTitle: String
    val notifFinancesDesc: String
    val testClassBtn: String
    val testGradeBtn: String
    val testMsgBtn: String
    val testFinanceBtn: String
    val personalizationTitle: String
    val personalizationSubtitle: String
    val startScreenLabel: String
    val startScreenDesc: String
    val selectStartScreenPlaceholder: String
    val currentlyHiddenPage: String
    val timetableSectionLabel: String
    val showWeekendLabel: String
    val showWeekendDesc: String
    val visiblePagesLabel: String
    val visiblePagesDesc: String
    val targetCreditsLabel: String
    val targetCreditsDesc: String
    val creditsUnit: String
    val quietHoursLabel: String
    val quietHoursDesc: String
    val activeWindowLabel: String
    val securityTitle: String
    val securitySubtitle: String
    val securityDescription: String
    val biometricLockTitle: String
    val biometricLockDesc: String
    val lastSyncLabel: String
    val notSyncedYet: String
    val appUpdatesTitle: String
    val appUpdatesSubtitle: String
    val updateChannelLabel: String
    val installedVersionLabel: String
    val packageNameLabel: String
    val downloadNewVersionBtn: String
    val checkingUpdates: String
    val checkUpdatesBtn: String
    val githubReleasesBtn: String
    val syncInProgress: String
    val manualSyncBtn: String
    val exportIcsBtn: String
    val clearCacheBtn: String
    val clearingCacheInProgress: String
    val logoutBtn: String
    val logoutDialogTitle: String
    val logoutDialogMessage: String

    // Extended Login Strings
    val loginBrandingSubtitle: String
    val institutionLabel: String
    val selectUniversityPlaceholder: String
    val neptunCodeInputLabel: String
    val passwordInputLabel: String
    val quickDemoCredentials: String
    val keystoreSecurityNote: String
    val selectInstitutionTitle: String
    val searchUniversityPlaceholder: String
    val selectLanguageSubtitle: String
    val twoFactorHeader: String
    val twoFactorAccountPrompt: (String) -> String
    val twoFactorEmailTab: String
    val twoFactorTotpTab: String
    val twoFactorEmailPrompt: String
    val twoFactorRequestingCode: String
    val twoFactorRequestEmailBtn: String
    val twoFactorCodeSent: String
    val twoFactorCodePrefix: String
    val twoFactorEmailCodeInputLabel: String
    val twoFactorResendBtn: String
    val twoFactorTestCodeBtn: String
    val twoFactorTotpPrompt: String
    val twoFactorNoTotpNote: String
    val twoFactorTotpInputLabel: String
    val twoFactorVerifyBtn: String

    // Theme Mode
    val themeModeSystem: String
    val themeModeLight: String
    val themeModeDark: String
    val themeModeSystemDesc: String
    val themeModeLightDesc: String
    val themeModeDarkDesc: String

    // Accent Colors
    val colorBlue: String
    val colorIndigo: String
    val colorCyan: String
    val colorEmerald: String
    val colorGold: String
    val colorPurple: String
    val colorCrimson: String
    val colorRose: String

    // Update Channel
    val updateChannelStable: String
    val updateChannelStableDesc: String
    val updateChannelDev: String
    val updateChannelDevDesc: String

    // Feedback messages
    val languageChanged: (String) -> String
    val languageChangeError: (String) -> String
    val syncSuccess: String
    val syncCompleted: String
    val cacheClearedSuccess: String
    val cacheClearFailed: String
    val updateAvailableOnChannel: (String, String) -> String
    val appUpToDate: (String) -> String
    val githubReleasesPrompt: String

    // In-App Update Dialog
    val updateDialogNewVersionTitle: String
    val updateDialogReleaseNotes: String
    val updateDialogDefaultDesc: String
    val updateDialogUpdateNow: String
    val updateDialogLater: String
    val updateDialogDownloadingTitle: (Int) -> String
    val updateDialogVersion: (String) -> String
    val updateDialogBackground: String
    val updateDialogReadyTitle: String
    val updateDialogReadyMessage: (String) -> String
    val updateDialogOpenInstall: String
    val updateDialogErrorTitle: String
    val updateDialogGithubDownload: String

    // Biometric Lock Screen
    val biometricLockedTitle: String
    val biometricLockedDesc: String
    val biometricUnlockBtn: String
    val biometricNotAvailable: String

    // Crash Dialog
    val crashDialogTitle: String
    val crashDialogDesc: String

    // Additional Timetable strings
    val nextClassToday: String
    val noClassesScheduledForDay: String
    val noClassesThisDay: String
    val noRoomSpecified: String
    val timeSlot: String

    // Additional Grades & Ghost Mark strings
    val semesterStats: String
    val notEnoughDataForStats: String
    val creditProgress: String
    val creditProgressFormat: (Int, Int) -> String
    val creditProgressHint: String
    val creditsCount: (Int) -> String
    val signedStatus: String
    val notRegisteredStatus: String
    val noDetailsInfo: String
    val noGradeYet: String
    val editGhostGrade: (Int) -> String
    val setGhostGradeTitle: String
    val ghostGradeDescription: String
    val removeGhostGrade: String
    val ghostGradeSimulatorActive: (Int) -> String
    val ghostCreditIndexSimulation: (Double) -> String
    val expectedAverageWithGhost: String
    val noExamsFound: String
    val examsNotAvailableNotice: String

    // Additional Messages strings
    val tapToViewFullMessage: String
    val noUnreadMessages: String
    val noMessagesInInbox: String
    val officialNotice: String
    val downloadingMessageContent: String

    // Demo and fallback banner
    val demoModeBanner: String
    val fallbackSampleDataBanner: String

    // Additional Widget strings
    val widgetTodayClasses: String
    val widgetNoMoreClasses: String
    val widgetMoreClassesCount: (Int) -> String

    // Notifications
    val notifChannelClasses: String
    val notifChannelClassesDesc: String
    val notifChannelMessages: String
    val notifChannelMessagesDesc: String
    val notifChannelGrades: String
    val notifChannelGradesDesc: String
    val notifChannelFinances: String
    val notifChannelFinancesDesc: String
    val notifClassReminderTitle: (String) -> String
    val notifClassReminderText: (String, String, String) -> String
    val notifClassReminderBigText: (String, String, Int, String, String) -> String
    val notifNewMessageTitle: (String) -> String
    val notifNewGradeTitle: (String) -> String
    val notifNewGradeText: (String, Int) -> String
    val notifNewGradeBigText: (String, String, Int) -> String
    val notifFinanceTitle: (String) -> String
    val notifFinanceText: (String, String) -> String
    val notifFinanceBigText: (String, String, String) -> String
    val notifMessagesSummaryTitle: (Int) -> String
    val notifGradesSummaryTitle: (Int) -> String
    val notifFinancesSummaryTitle: (Int) -> String
}

class HungarianStrings : AppStrings {
    override val languageName: String = "Magyar"
    override val languageCode: String = "hu"

    override val navDashboard = "Kezdőlap"
    override val navTimetable = "Órarend"
    override val navGrades = "Jegyek"
    override val navMessages = "Üzenetek"
    override val navFinances = "Pénzügyek"
    override val navSettings = "Beállítások"

    override val appName = "Neptun Mobile"
    override val save = "Mentés"
    override val cancel = "Mégse"
    override val close = "Bezárás"
    override val copy = "Másolás"
    override val later = "Később"
    override val sessionExpiredTitle = "Lejárt a munkamenet"
    override val sessionExpiredDesc = "A Neptun szerver visszautasította a munkamenetet, és nem sikerült automatikusan megújítani. Kérlek, jelentkezz be újra a friss adatokért."
    override val shareTimetableChooser = "Órarend megosztása"
    override val delete = "Törlés"
    override val edit = "Szerkesztés"
    override val refresh = "Frissítés"
    override val search = "Keresés"
    override val ok = "Rendben"
    override val yes = "Igen"
    override val no = "Nem"
    override val retry = "Újrapróbálás"
    override val error = "Hiba történt"
    override val success = "Sikeres művelet"
    override val loading = "Betöltés..."
    override val empty = "Nincs adat"
    override val all = "Összes"
    override val details = "Részletek"
    override val select = "Kiválasztás"
    override val back = "Vissza"

    override val loginTitle = "Neptun Bejelentkezés"
    override val loginSubtitle = "Válassz intézményt és lépj be a Neptun kódoddal"
    override val selectUniversity = "Intézmény kiválasztása"
    override val searchUniversity = "Keresés az egyetemek között..."
    override val neptunCode = "Neptun kód"
    override val password = "Jelszó"
    override val loginButton = "Bejelentkezés"
    override val loggingIn = "Bejelentkezés folyamatban..."
    override val demoLogin = "Kipróbálás Demo módban"
    override val offlineMode = "Offline mód folytatása"
    override val offlineModeDesc = "Mentett adatok megtekintése internet nélkül"
    override val invalidNeptunCode = "A Neptun kódnak pontosan 6 karakterből kell állnia!"
    override val emptyPassword = "A jelszó mező nem lehet üres!"
    override val languageSelectTitle = "Nyelv kiválasztása"
    override val universityLanguageInfo = "Az intézmény Neptun szervere által támogatott nyelvek"

    override val twoFactorTitle = "Kétlépcsős azonosítás (2FA)"
    override val twoFactorSubtitle = "Add meg a kapott megerősítő kódot"
    override val twoFactorEmailCode = "E-mail kód kérése"
    override val twoFactorTotpCode = "Hitelesítő App kód"
    override val twoFactorEnterCode = "Megerősítő kód"
    override val twoFactorVerify = "Azonosítás megerősítése"
    override val twoFactorResendCode = "Új kód kérése"
    override val twoFactorSuccess = "Sikeres kétlépcsős azonosítás!"

    override val dashboardGreeting = "Szia"
    override val nextClass = "Következő óra"
    override val todayClasses = "Mai órák"
    override val noClassesToday = "Ma nincsenek óráid 🎉"
    override val noMoreClassesToday = "Mára végeztél az órákkal! 🎉"
    override val viewFullTimetable = "Teljes órarend megnyitása"
    override val recentMessages = "Legfrissebb üzenetek"
    override val unreadMessagesCount = { count: Int -> "$count olvasatlan üzenet" }
    override val quickStats = "Tanulmányi áttekintés"
    override val currentAverage = "Súlyozott átlag"
    override val totalCredits = "Kreditek"
    override val pendingFinancesCount = { count: Int -> "$count befizetendő tétel" }

    override val timetableTitle = "Órarend"
    override val dayView = "Napi"
    override val weekView = "Heti"
    override val exportIcs = "Exportálás (.ics)"
    override val icsExportSuccess = "Órarend sikeresen exportálva!"
    override val weekA = "A hét (Páratlan)"
    override val weekB = "B hét (Páros)"
    override val monday = "Hétfő"
    override val tuesday = "Kedd"
    override val wednesday = "Szerda"
    override val thursday = "Csütörtök"
    override val friday = "Péntek"
    override val saturday = "Szombat"
    override val sunday = "Vasárnap"
    override val room = "Terem"
    override val instructor = "Oktató"
    override val courseCode = "Kurzus kód"

    override val gradesTitle = "Jegyek és Átlagok"
    override val weightedAverage = "Súlyozott átlag (SÁ)"
    override val creditIndex = "Kreditindex (KI)"
    override val enrolledCredits = "Felvett kredit"
    override val completedCredits = "Teljesített kredit"
    override val ghostGrades = "Szellemjegyek"
    override val ghostGradeSimulator = "Szellemjegy szimulátor"
    override val addGhostGrade = "Szellemjegy hozzáadása"
    override val resetGhostGrades = "Szellemjegyek törlése"
    override val gradeText5 = "Jeles (5)"
    override val gradeText4 = "Jó (4)"
    override val gradeText3 = "Közepes (3)"
    override val gradeText2 = "Elégséges (2)"
    override val gradeText1 = "Elégtelen (1)"
    override val newEntry = "Új bejegyzés"
    override val noGradesYet = "Ebben a félévben még nincs beírt jegy"

    override val messagesTitle = "Üzenetek"
    override val officialMessage = "Hivatalos"
    override val systemMessage = "Rendszerüzenet"
    override val markAsRead = "Olvasottnak jelölés"
    override val markAllAsRead = "Mind olvasottnak jelölése"
    override val unreadOnly = "Csak olvasatlan"
    override val searchMessages = "Keresés üzenetek között..."
    override val noMessages = "Nincsenek üzenetek"
    override val sender = "Feladó"
    override val date = "Dátum"

    override val financesTitle = "Pénzügyek"
    override val statusPending = "Kiírva"
    override val statusCompleted = "Teljesítve"
    override val statusOverdue = "Késedelmes"
    override val dueDate = "Határidő"
    override val paymentDate = "Befizetés dátuma"
    override val amount = "Összeg"
    override val noFinances = "Nincsenek pénzügyi tételek"

    // Course Types
    override val courseTypeLecture = "Előadás"
    override val courseTypePractice = "Gyakorlat"
    override val courseTypeLab = "Labor"
    override val courseTypeSeminar = "Szeminárium"
    override val courseTypeExam = "Vizsga"

    // Extra UI strings
    override val inProgressClass = "Éppen zajló óra"
    override val allMessagesRead = "Minden üzenet elolvasva"

    override val settingsTitle = "Beállítások"
    override val sectionAccount = "Fiók és Intézmény"
    override val sectionLanguage = "Nyelv / Language"
    override val sectionTheme = "Megjelenés és Téma"
    override val sectionNotifications = "Értesítések"
    override val sectionPersonalization = "Személyreszabás"
    override val sectionDataCache = "Adatok és Gyorsítótár"
    override val sectionAbout = "Névjegy és Frissítések"
    override val logout = "Kijelentkezés"
    override val logoutConfirmTitle = "Biztosan kijelentkezel?"
    override val logoutConfirmMessage = "A bejelentkezési adatok törlődnek, de a letöltött értesítési állapot megmarad."
    override val loggedInAs = "Bejelentkezve mint"
    override val university = "Intézmény"
    override val neptunServer = "Neptun szerver"
    override val languageDescription = "Válassz nyelvet az apphoz és a Neptun felülethez"
    override val languageSyncNeptun = "Szinkronizálás a Neptun szerverrel"
    override val languageChangedNotice = { lang: String -> "Nyelv beállítva: $lang" }
    override val themeMode = "Téma mód"
    override val themeSystem = "Rendszer"
    override val themeLight = "Világos"
    override val themeDark = "Sötét"
    override val dynamicColors = "Dinamikus Material You színek"
    override val accentColor = "Kiemelő szín"
    override val notifyMessages = "Új üzenetek értesítése"
    override val notifyGrades = "Új érdemjegyek értesítése"
    override val notifyFinances = "Befizetendő tételek értesítése"
    override val notifyClasses = "Órarendi emlékeztetők"
    override val classReminderTime = "Emlékeztető ideje óra előtt"
    override val quietHours = "Csendes időszak (ne zavarjanak)"
    override val clearCache = "Gyorsítótár törlése"
    override val cacheCleared = "Gyorsítótár sikeresen kiürítve"
    override val checkUpdates = "Frissítések keresése"
    override val appVersion = "Alkalmazás verzió"
    override val serverConnectionMode = "Kapcsolati mód"

    // Extended Settings Strings
    override val profileSubtitle = "Profil & Testreszabás"
    override val studentDefaultName = "Egyetemi Hallgató"
    override val studentDefaultUni = "Felsőoktatási Intézmény"
    override val studentDefaultProgram = "BSc Hallgató"
    override val neptunCodeLabel = "Neptun kód:"
    override val languageSubtitle = "Alkalmazás és Neptun kiszolgáló nyelve"
    override val supportedLanguagesHeader = "Intézmény által támogatott nyelvek"
    override val appearanceTitle = "Megjelenés és Téma"
    override val appearanceSubtitle = "Szabd személyre az alkalmazás arculatát"
    override val themeModeLabel = "Téma mód"
    override val dynamicColorLabel = "Dinamikus színek"
    override val dynamicColorDesc = "A telefon háttérképéhez illeszkedő Material You paletta használata"
    override val dynamicColorOnlyAndroid12 = "Csak Android 12 vagy újabb rendszeren érhető el"
    override val dynamicColorActiveBanner = "A dinamikus szín aktív. Ha egyéni hangsúlyszínt választasz, a dinamikus szín automatikusan kikapcsol."
    override val accentColorTitle = "Hangsúlyszín kiválasztása"
    override val accentColorSelected = "Kiválasztva:"
    override val customThemeBadge = "Egyéni téma"
    override val livePreviewTitle = "Élő arculati előnézet"
    override val activeThemeLabel = "Aktív téma:"
    override val btnPrimary = "Fő gomb"
    override val btnTonal = "Tonális"
    override val btnOutlined = "Keretes"
    override val notificationsTitle = "Értesítések és Emlékeztetők"
    override val notificationsSubtitle = "Kategóriák és háttérbeli értesítések"
    override val notifPermissionGranted = "Értesítések engedélyezve"
    override val notifPermissionRequired = "Értesítési engedély szükséges"
    override val notifPermissionGrantedDesc = "Az alkalmazás küldhet órarendi és tanulmányi értesítéseket."
    override val notifPermissionRequiredDesc = "Kattints az engedély megadásához."
    override val grantPermissionBtn = "Engedély kérése"
    override val notifClassesTitle = "Órarendi értesítések"
    override val notifClassesDesc = "15 perccel az órák előtt emlékeztető a pontos teremszámmal"
    override val notifGradesTitle = "Jegyek és értékelések"
    override val notifGradesDesc = "Azonnali figyelmeztetés új érdemjegy vagy bejegyzés rögzítésekor"
    override val notifMessagesTitle = "Neptun üzenetek"
    override val notifMessagesDesc = "Értesítés oktatói és tanulmányi rendszerüzenetek érkezésekor"
    override val notifFinancesTitle = "Pénzügyi tételek"
    override val notifFinancesDesc = "Emlékeztetők kiírásokról, díjakról és fizetési határidőkről"
    override val testClassBtn = "Óra teszt"
    override val testGradeBtn = "Jegy teszt"
    override val testMsgBtn = "Üzenet teszt"
    override val testFinanceBtn = "Pénzügy teszt"
    override val personalizationTitle = "Személyreszabás"
    override val personalizationSubtitle = "Kezdőképernyő, órarend és tanulmányi beállítások"
    override val startScreenLabel = "Kezdőképernyő"
    override val startScreenDesc = "Az alkalmazás megnyitásakor megjelenő alapértelmezett oldal."
    override val selectStartScreenPlaceholder = "Kezdőlap kiválasztása"
    override val currentlyHiddenPage = "Jelenleg rejtett oldal"
    override val timetableSectionLabel = "Órarend"
    override val showWeekendLabel = "Hétvége megjelenítése"
    override val showWeekendDesc = "Szombat és vasárnap oszlopai"
    override val visiblePagesLabel = "Látható oldalak"
    override val visiblePagesDesc = "A számodra nem hasznos oldalakat elrejtheted – eltűnnek az alsó sávból. A Profil mindig látható marad."
    override val targetCreditsLabel = "Cél kreditek (diploma)"
    override val targetCreditsDesc = "A kredithaladás sávja ezt a célt mutatja a Jegyek fülön."
    override val creditsUnit = "kredit"
    override val quietHoursLabel = "Halk órák"
    override val quietHoursDesc = "Ebben az időszakban nem küldünk üzenet-, jegy- és pénzügyi értesítést (az óra-emlékeztetők maradnak)."
    override val activeWindowLabel = "Aktív időablak"
    override val securityTitle = "Biztonság és Titkosítás"
    override val securitySubtitle = "Helyi és hardveres adatvédelem"
    override val securityDescription = "A Neptun bejelentkezési adatok hardveresen védett Android Keystore (AES-256) titkosítással vannak tárolva. Az órarend, jegyek és üzenetek helyi Room adatbázisban tárolódnak, így internetkapcsolat nélkül is azonnal elérhetők."
    override val biometricLockTitle = "Biometrikus zár"
    override val biometricLockDesc = "Az app felnyitásához ujjlenyomat vagy arcfelismerés szükséges"
    override val lastSyncLabel = "Utolsó sikeres szinkronizálás:"
    override val notSyncedYet = "Még nincs szinkronizálva"
    override val appUpdatesTitle = "Alkalmazás és Frissítések"
    override val appUpdatesSubtitle = "Verziókezelés és GitHub Releases"
    override val updateChannelLabel = "Frissítési csatorna"
    override val installedVersionLabel = "Telepített verzió"
    override val packageNameLabel = "Csomagnév"
    override val downloadNewVersionBtn = "Új verzió letöltése"
    override val checkingUpdates = "Keresés..."
    override val checkUpdatesBtn = "Frissítés keresése"
    override val githubReleasesBtn = "GitHub Releases"
    override val syncInProgress = "Szinkronizálás folyamatban..."
    override val manualSyncBtn = "Azonnali szinkronizálás"
    override val exportIcsBtn = "Órarend exportálása (.ics)"
    override val clearCacheBtn = "Helyi gyorsítótár törlése"
    override val clearingCacheInProgress = "Törlés folyamatban..."
    override val logoutBtn = "Kijelentkezés"
    override val logoutDialogTitle = "Kijelentkezés"
    override val logoutDialogMessage = "Biztosan ki szeretnél jelentkezni? A helyileg tárolt hitelesítő adatok és az offline cache törlődnek."

    // Extended Login Strings
    override val loginBrandingSubtitle = "Modern alternatív Neptun kliens 2FA támogatással"
    override val institutionLabel = "Intézmény / Egyetem"
    override val selectUniversityPlaceholder = "Válassz egyetemet..."
    override val neptunCodeInputLabel = "Neptun kód (6 karakter)"
    override val passwordInputLabel = "Jelszó"
    override val quickDemoCredentials = "Offline Demo adatok betöltése"
    override val keystoreSecurityNote = "Hitelesítő adataidat az Android Keystore (EncryptedSharedPreferences) biztonságosan, titkosítva tárolja az eszközödön."
    override val selectInstitutionTitle = "Válassz intézményt"
    override val searchUniversityPlaceholder = "Keresés név, kód vagy város alapján..."
    override val selectLanguageSubtitle = "Válassz nyelvet a felülethez és a Neptunhoz"
    override val twoFactorHeader = "Kétlépcsős Azonosítás (2FA)"
    override val twoFactorAccountPrompt = { code: String -> "A Neptun kétlépcsős hitelesítést igényel a(z) $code fiókhoz." }
    override val twoFactorEmailTab = "E-mail kód"
    override val twoFactorTotpTab = "Hitelesítő App"
    override val twoFactorEmailPrompt = "Kérj belépési kódot az egyetemi e-mail címedre:"
    override val twoFactorRequestingCode = "Kód kérése folyamatban..."
    override val twoFactorRequestEmailBtn = "E-mail kód kérése"
    override val twoFactorCodeSent = "Kód elküldve az egyetemi fiókodra!"
    override val twoFactorCodePrefix = "A Neptun által generált előtag:"
    override val twoFactorEmailCodeInputLabel = "6 jegyű kód az e-mailből"
    override val twoFactorResendBtn = "Új kód kérése"
    override val twoFactorTestCodeBtn = "Teszt: 999999"
    override val twoFactorTotpPrompt = "Add meg a Google / Microsoft Authenticator appban megjelenő 6 számjegyű kódot:"
    override val twoFactorNoTotpNote = "Megjegyzés: A Neptun szerint még nincs TOTP kulcs párosítva ehhez a fiókhoz. Használd az E-mail kód opciót!"
    override val twoFactorTotpInputLabel = "TOTP Kód (pl. 482910)"
    override val twoFactorVerifyBtn = "Belépés"

    // Theme Mode
    override val themeModeSystem = "Rendszer"
    override val themeModeLight = "Világos"
    override val themeModeDark = "Sötét"
    override val themeModeSystemDesc = "Követi a telefon beállításait"
    override val themeModeLightDesc = "Állandó világos megjelenés"
    override val themeModeDarkDesc = "Kíméli a szemet sötétben"

    // Accent Colors
    override val colorBlue = "Neptun Kék"
    override val colorIndigo = "Zafír Indigó"
    override val colorCyan = "Türkiz Cselló"
    override val colorEmerald = "Smaragd Zöld"
    override val colorGold = "Neptun Arany"
    override val colorPurple = "Ametiszt Bíbor"
    override val colorCrimson = "Rubin Piros"
    override val colorRose = "Rózsakvarc"

    // Update Channel
    override val updateChannelStable = "Stabil kiadások"
    override val updateChannelStableDesc = "Kizárólag hivatalosan tesztelt, megbízható verziók"
    override val updateChannelDev = "Fejlesztői (Dev)"
    override val updateChannelDevDesc = "A legújabb fejlesztői buildek és előzetes funkciók"

    // Feedback messages
    override val languageChanged: (String) -> String = { lang -> "Nyelv módosítva: $lang" }
    override val languageChangeError: (String) -> String = { lang -> "Nem sikerült a nyelvet átállítani erre: $lang" }
    override val syncSuccess = "Sikeres szinkronizálás! Minden adat naprakész."
    override val syncCompleted = "Szinkronizálás befejeződött."
    override val cacheClearedSuccess = "A helyi gyorsítótár törölve. A következő szinkronizáláskor friss adatok töltődnek le."
    override val cacheClearFailed = "A törlés nem sikerült."
    override val updateAvailableOnChannel: (String, String) -> String = { version, channel -> "Új verzió ($version) érhető el a $channel csatornán!" }
    override val appUpToDate: (String) -> String = { version -> "A legfrissebb verziót használod ($version)." }
    override val githubReleasesPrompt = "Nyisd meg a GitHub Releases oldalt a letöltéshez."

    // In-App Update Dialog
    override val updateDialogNewVersionTitle = "Frissítés érhető el!"
    override val updateDialogReleaseNotes = "Újdonságok és változtatások:"
    override val updateDialogDefaultDesc = "A Neptun Mobile új verziója készen áll a frissítésre hibajavításokkal és teljesítmény-fejlesztésekkel."
    override val updateDialogUpdateNow = "Frissítés most"
    override val updateDialogLater = "Később"
    override val updateDialogDownloadingTitle: (Int) -> String = { progress -> "Frissítés letöltése... ($progress%)" }
    override val updateDialogVersion: (String) -> String = { version -> "Verzió: $version" }
    override val updateDialogBackground = "Háttérbe"
    override val updateDialogReadyTitle = "Letöltés kész!"
    override val updateDialogReadyMessage: (String) -> String = { version -> "A frissítés ($version) sikeresen letöltődött. Érintsd meg a gombot a telepítéshez." }
    override val updateDialogOpenInstall = "Telepítés megnyitása"
    override val updateDialogErrorTitle = "Frissítési hiba"
    override val updateDialogGithubDownload = "GitHub letöltés"

    // Biometric Lock Screen
    override val biometricLockedTitle = "Az alkalmazás zárolva van"
    override val biometricLockedDesc = "Erősítsd meg az azonosságod az ujjlenyomatoddal vagy arcoddal."
    override val biometricUnlockBtn = "Feloldás"
    override val biometricNotAvailable = "A biometrikus hitelesítés nem elérhető ezen az eszközön."

    // Crash Dialog
    override val crashDialogTitle = "Az alkalmazás váratlanul leállt"
    override val crashDialogDesc = "Az előző futás hibanaplója (a hibajelentéshez másolható):"

    // Additional Timetable strings
    override val nextClassToday = "Következő óra ma"
    override val noClassesScheduledForDay = "Erre a napra nincs felvett órád ezen a héten."
    override val noClassesThisDay = "Ezen a napon nincs tanóra ezen a héten."
    override val noRoomSpecified = "Nincs terem megadva"
    override val timeSlot = "Idősáv"

    // Additional Grades & Ghost Mark strings
    override val semesterStats = "Féléves statisztika"
    override val notEnoughDataForStats = "Még nincs elég adat a statisztikához."
    override val creditProgress = "Kredithaladás"
    override val creditProgressFormat: (Int, Int) -> String = { completed, target -> "$completed / $target kredit" }
    override val creditProgressHint = "A célt a Beállítások → Személyreszabás menüben módosíthatod."
    override val creditsCount: (Int) -> String = { "$it kredit" }
    override val signedStatus = "Aláírva"
    override val notRegisteredStatus = "Nincs jelentkezve"
    override val noDetailsInfo = "Nincs részletinformáció"
    override val noGradeYet = "Még nincs jegy"
    override val editGhostGrade: (Int) -> String = { "Szellemjegy módosítása ($it)" }
    override val setGhostGradeTitle = "Szellemjegy beállítása"
    override val ghostGradeDescription = "Adj meg egy virtuális jegyet a várható féléves átlag és kreditindex azonnali szimulálásához."
    override val removeGhostGrade = "Szellemjegy eltávolítása"
    override val ghostGradeSimulatorActive: (Int) -> String = { "$it szellemjegy aktív" }
    override val ghostCreditIndexSimulation: (Double) -> String = { "Szimulált kreditindex: $it" }
    override val expectedAverageWithGhost = "Várható átlag szellemjegyekkel"
    override val noExamsFound = "Nem található vizsgaadat."
    override val examsNotAvailableNotice = "A vizsgalista ezen az egyetemi szerveren nem elérhető, vagy nincs felvett vizsgád. (Kísérleti funkció)"

    // Additional Messages strings
    override val tapToViewFullMessage = "Koppints a teljes üzenet megtekintéséhez..."
    override val noUnreadMessages = "Nincs olvasatlan üzeneted!"
    override val noMessagesInInbox = "Nem érkezett üzenet a fiókodba."
    override val officialNotice = "Hivatalos Értesítés"
    override val downloadingMessageContent = "Üzenet tartalmának letöltése a Neptunból..."

    // Demo and fallback banner
    override val demoModeBanner = "Demo mód – a megjelenített adatok nem valódiak"
    override val fallbackSampleDataBanner = "Mintaadatok láthatók (szinkronizálás nem sikerült)"

    // Additional Widget strings
    override val widgetTodayClasses = "· Mai órák"
    override val widgetNoMoreClasses = "Ma nincs több órád!"
    override val widgetMoreClassesCount: (Int) -> String = { "…és még $it óra" }

    // Notifications
    override val notifChannelClasses = "Órarendi Értesítések"
    override val notifChannelClassesDesc = "Értesítés az órák megkezdése előtt a terem megjelölésével"
    override val notifChannelMessages = "Neptun Üzenetek"
    override val notifChannelMessagesDesc = "Értesítés az új oktatói és tanulmányi üzenetekről"
    override val notifChannelGrades = "Jegyek és Értékelések"
    override val notifChannelGradesDesc = "Értesítés az új érdemjegyekről és félévközi eredményekről"
    override val notifChannelFinances = "Pénzügyi Értesítések"
    override val notifChannelFinancesDesc = "Értesítés a pénzügyi kiírásokról és határidőkről"
    override val notifClassReminderTitle: (String) -> String = { "Hamarosan kezdődik: $it" }
    override val notifClassReminderText: (String, String, String) -> String = { type, time, room -> "$type $time-kor | Terem: $room" }
    override val notifClassReminderBigText: (String, String, Int, String, String) -> String = { subject, type, minutes, time, room -> "Az órád ($subject - $type) $minutes perc múlva ($time) kezdődik a(z) $room teremben." }
    override val notifNewMessageTitle: (String) -> String = { "Új üzenet: $it" }
    override val notifNewGradeTitle: (String) -> String = { "Új érdemjegy: $it" }
    override val notifNewGradeText: (String, Int) -> String = { grade, credit -> "Eredmény: $grade ($credit kredit)" }
    override val notifNewGradeBigText: (String, String, Int) -> String = { subject, grade, credit -> "Új értékelés érkezett a(z) $subject tantárgyból!\nÉrdemjegy: $grade | Kreditérték: $credit" }
    override val notifFinanceTitle: (String) -> String = { "Pénzügyi tétel: $it" }
    override val notifFinanceText: (String, String) -> String = { amount, due -> "Összeg: $amount Ft | Határidő: $due" }
    override val notifFinanceBigText: (String, String, String) -> String = { title, amount, due -> "Figyelem! Befizetendő pénzügyi tétel: $title\nÖsszeg: $amount HUF\nFizetési határidő: $due" }
    override val notifMessagesSummaryTitle: (Int) -> String = { "$it új üzenet" }
    override val notifGradesSummaryTitle: (Int) -> String = { "$it új érdemjegy" }
    override val notifFinancesSummaryTitle: (Int) -> String = { "$it befizetendő tétel" }
}

class EnglishStrings : AppStrings {
    override val languageName: String = "English"
    override val languageCode: String = "en"

    override val navDashboard = "Dashboard"
    override val navTimetable = "Timetable"
    override val navGrades = "Grades"
    override val navMessages = "Messages"
    override val navFinances = "Finances"
    override val navSettings = "Settings"

    override val appName = "Neptun Mobile"
    override val save = "Save"
    override val cancel = "Cancel"
    override val close = "Close"
    override val copy = "Copy"
    override val later = "Later"
    override val sessionExpiredTitle = "Session Expired"
    override val sessionExpiredDesc = "The Neptun server rejected the session and it could not be refreshed automatically. Please log in again to fetch fresh data."
    override val shareTimetableChooser = "Share Timetable"
    override val delete = "Delete"
    override val edit = "Edit"
    override val refresh = "Refresh"
    override val search = "Search"
    override val ok = "OK"
    override val yes = "Yes"
    override val no = "No"
    override val retry = "Retry"
    override val error = "An error occurred"
    override val success = "Success"
    override val loading = "Loading..."
    override val empty = "No data"
    override val all = "All"
    override val details = "Details"
    override val select = "Select"
    override val back = "Back"

    override val loginTitle = "Neptun Login"
    override val loginSubtitle = "Select your institution and log in with your Neptun code"
    override val selectUniversity = "Select Institution"
    override val searchUniversity = "Search institutions..."
    override val neptunCode = "Neptun Code"
    override val password = "Password"
    override val loginButton = "Log In"
    override val loggingIn = "Logging in..."
    override val demoLogin = "Try Demo Mode"
    override val offlineMode = "Continue Offline"
    override val offlineModeDesc = "View saved data without network"
    override val invalidNeptunCode = "Neptun code must be exactly 6 characters!"
    override val emptyPassword = "Password cannot be empty!"
    override val languageSelectTitle = "Select Language"
    override val universityLanguageInfo = "Languages supported by this university's Neptun server"

    override val twoFactorTitle = "Two-Factor Authentication (2FA)"
    override val twoFactorSubtitle = "Enter the verification code you received"
    override val twoFactorEmailCode = "Request Email Code"
    override val twoFactorTotpCode = "Authenticator App Code"
    override val twoFactorEnterCode = "Verification Code"
    override val twoFactorVerify = "Verify Authentication"
    override val twoFactorResendCode = "Resend Code"
    override val twoFactorSuccess = "Two-factor authentication successful!"

    override val dashboardGreeting = "Hello"
    override val nextClass = "Next Class"
    override val todayClasses = "Today's Schedule"
    override val noClassesToday = "No classes today 🎉"
    override val noMoreClassesToday = "You're done for today! 🎉"
    override val viewFullTimetable = "Open full timetable"
    override val recentMessages = "Recent Messages"
    override val unreadMessagesCount = { count: Int -> "$count unread messages" }
    override val quickStats = "Academic Overview"
    override val currentAverage = "Weighted Average"
    override val totalCredits = "Credits"
    override val pendingFinancesCount = { count: Int -> "$count pending payment(s)" }

    override val timetableTitle = "Timetable"
    override val dayView = "Day"
    override val weekView = "Week"
    override val exportIcs = "Export (.ics)"
    override val icsExportSuccess = "Timetable exported successfully!"
    override val weekA = "Week A (Odd)"
    override val weekB = "Week B (Even)"
    override val monday = "Monday"
    override val tuesday = "Tuesday"
    override val wednesday = "Wednesday"
    override val thursday = "Thursday"
    override val friday = "Friday"
    override val saturday = "Saturday"
    override val sunday = "Sunday"
    override val room = "Room"
    override val instructor = "Instructor"
    override val courseCode = "Course Code"

    override val gradesTitle = "Grades & Averages"
    override val weightedAverage = "Weighted Average (GPA)"
    override val creditIndex = "Credit Index"
    override val enrolledCredits = "Enrolled Credits"
    override val completedCredits = "Completed Credits"
    override val ghostGrades = "Simulated Grades"
    override val ghostGradeSimulator = "Grade Simulator"
    override val addGhostGrade = "Add Simulated Grade"
    override val resetGhostGrades = "Clear Simulated Grades"
    override val gradeText5 = "Excellent (5)"
    override val gradeText4 = "Good (4)"
    override val gradeText3 = "Satisfactory (3)"
    override val gradeText2 = "Pass (2)"
    override val gradeText1 = "Fail (1)"
    override val newEntry = "New entry"
    override val noGradesYet = "No grades recorded for this semester yet"

    override val messagesTitle = "Messages"
    override val officialMessage = "Official"
    override val systemMessage = "System Message"
    override val markAsRead = "Mark as Read"
    override val markAllAsRead = "Mark All as Read"
    override val unreadOnly = "Unread Only"
    override val searchMessages = "Search messages..."
    override val noMessages = "No messages"
    override val sender = "Sender"
    override val date = "Date"

    override val financesTitle = "Finances"
    override val statusPending = "Pending"
    override val statusCompleted = "Paid"
    override val statusOverdue = "Overdue"
    override val dueDate = "Due Date"
    override val paymentDate = "Payment Date"
    override val amount = "Amount"
    override val noFinances = "No financial items"

    // Course Types
    override val courseTypeLecture = "Lecture"
    override val courseTypePractice = "Practice"
    override val courseTypeLab = "Lab"
    override val courseTypeSeminar = "Seminar"
    override val courseTypeExam = "Exam"

    // Extra UI strings
    override val inProgressClass = "Class in progress"
    override val allMessagesRead = "All messages read"

    override val settingsTitle = "Settings"
    override val sectionAccount = "Account & University"
    override val sectionLanguage = "Language / Nyelv"
    override val sectionTheme = "Appearance & Theme"
    override val sectionNotifications = "Notifications"
    override val sectionPersonalization = "Personalization"
    override val sectionDataCache = "Data & Cache"
    override val sectionAbout = "About & Updates"
    override val logout = "Log Out"
    override val logoutConfirmTitle = "Are you sure you want to log out?"
    override val logoutConfirmMessage = "Credentials will be cleared, notification tracking will be preserved."
    override val loggedInAs = "Logged in as"
    override val university = "University"
    override val neptunServer = "Neptun Server"
    override val languageDescription = "Choose language for the app and Neptun interface"
    override val languageSyncNeptun = "Sync with Neptun Server"
    override val languageChangedNotice = { lang: String -> "Language set to: $lang" }
    override val themeMode = "Theme Mode"
    override val themeSystem = "System"
    override val themeLight = "Light"
    override val themeDark = "Dark"
    override val dynamicColors = "Material You Dynamic Colors"
    override val accentColor = "Accent Color"
    override val notifyMessages = "New Message Alerts"
    override val notifyGrades = "New Grade Alerts"
    override val notifyFinances = "Pending Payment Alerts"
    override val notifyClasses = "Class Reminders"
    override val classReminderTime = "Reminder time before class"
    override val quietHours = "Quiet Hours (Do Not Disturb)"
    override val clearCache = "Clear Cache"
    override val cacheCleared = "Cache cleared successfully"
    override val checkUpdates = "Check for Updates"
    override val appVersion = "App Version"
    override val serverConnectionMode = "Connection Mode"

    // Extended Settings Strings
    override val profileSubtitle = "Profile & Customization"
    override val studentDefaultName = "University Student"
    override val studentDefaultUni = "Higher Education Institution"
    override val studentDefaultProgram = "BSc Student"
    override val neptunCodeLabel = "Neptun code:"
    override val languageSubtitle = "Application and Neptun server language"
    override val supportedLanguagesHeader = "Languages supported by institution"
    override val appearanceTitle = "Appearance & Theme"
    override val appearanceSubtitle = "Customize the look and feel of the app"
    override val themeModeLabel = "Theme mode"
    override val dynamicColorLabel = "Dynamic colors"
    override val dynamicColorDesc = "Use Material You palette matching phone wallpaper"
    override val dynamicColorOnlyAndroid12 = "Available on Android 12 or newer only"
    override val dynamicColorActiveBanner = "Dynamic color is active. Selecting a custom accent color will disable dynamic color."
    override val accentColorTitle = "Select Accent Color"
    override val accentColorSelected = "Selected:"
    override val customThemeBadge = "Custom theme"
    override val livePreviewTitle = "Live Theme Preview"
    override val activeThemeLabel = "Active theme:"
    override val btnPrimary = "Primary"
    override val btnTonal = "Tonal"
    override val btnOutlined = "Outlined"
    override val notificationsTitle = "Notifications & Reminders"
    override val notificationsSubtitle = "Categories and background notifications"
    override val notifPermissionGranted = "Notifications enabled"
    override val notifPermissionRequired = "Notification permission required"
    override val notifPermissionGrantedDesc = "The app can send timetable and academic alerts."
    override val notifPermissionRequiredDesc = "Tap to grant permission."
    override val grantPermissionBtn = "Grant permission"
    override val notifClassesTitle = "Timetable alerts"
    override val notifClassesDesc = "Reminder 15 minutes before classes with exact room number"
    override val notifGradesTitle = "Grades and assessments"
    override val notifGradesDesc = "Instant alert when a new grade or entry is recorded"
    override val notifMessagesTitle = "Neptun messages"
    override val notifMessagesDesc = "Alert on instructor and academic system messages"
    override val notifFinancesTitle = "Financial items"
    override val notifFinancesDesc = "Reminders about fees, charges, and payment deadlines"
    override val testClassBtn = "Class test"
    override val testGradeBtn = "Grade test"
    override val testMsgBtn = "Message test"
    override val testFinanceBtn = "Finance test"
    override val personalizationTitle = "Personalization"
    override val personalizationSubtitle = "Home screen, timetable, and study settings"
    override val startScreenLabel = "Default Start Screen"
    override val startScreenDesc = "Default page displayed when launching the app."
    override val selectStartScreenPlaceholder = "Select start screen"
    override val currentlyHiddenPage = "Currently hidden page"
    override val timetableSectionLabel = "Timetable"
    override val showWeekendLabel = "Show weekend"
    override val showWeekendDesc = "Saturday and Sunday columns"
    override val visiblePagesLabel = "Visible Pages"
    override val visiblePagesDesc = "Hide pages you don't need – they will disappear from the bottom bar. Settings remains visible."
    override val targetCreditsLabel = "Target Credits (Degree)"
    override val targetCreditsDesc = "Credit progress bar shows this target on the Grades tab."
    override val creditsUnit = "credits"
    override val quietHoursLabel = "Quiet Hours"
    override val quietHoursDesc = "No message, grade, or finance alerts will be sent during this period (class reminders remain active)."
    override val activeWindowLabel = "Active time window"
    override val securityTitle = "Security & Encryption"
    override val securitySubtitle = "Local and hardware data protection"
    override val securityDescription = "Neptun credentials are stored with hardware-backed Android Keystore (AES-256) encryption. Timetable, grades, and messages are stored in a local Room database for instant offline access."
    override val biometricLockTitle = "Biometric Lock"
    override val biometricLockDesc = "Fingerprint or facial recognition required to open the app"
    override val lastSyncLabel = "Last successful sync:"
    override val notSyncedYet = "Not synced yet"
    override val appUpdatesTitle = "App & Updates"
    override val appUpdatesSubtitle = "Version management and GitHub Releases"
    override val updateChannelLabel = "Update channel"
    override val installedVersionLabel = "Installed version"
    override val packageNameLabel = "Package name"
    override val downloadNewVersionBtn = "Download New Version"
    override val checkingUpdates = "Checking..."
    override val checkUpdatesBtn = "Check for updates"
    override val githubReleasesBtn = "GitHub Releases"
    override val syncInProgress = "Syncing in progress..."
    override val manualSyncBtn = "Sync Now"
    override val exportIcsBtn = "Export timetable (.ics)"
    override val clearCacheBtn = "Clear local cache"
    override val clearingCacheInProgress = "Clearing cache..."
    override val logoutBtn = "Log Out"
    override val logoutDialogTitle = "Log Out"
    override val logoutDialogMessage = "Are you sure you want to log out? Stored credentials and offline cache will be deleted."

    // Extended Login Strings
    override val loginBrandingSubtitle = "Modern alternative Neptun client with 2FA support"
    override val institutionLabel = "Institution / University"
    override val selectUniversityPlaceholder = "Select university..."
    override val neptunCodeInputLabel = "Neptun code (6 characters)"
    override val passwordInputLabel = "Password"
    override val quickDemoCredentials = "Load Offline Demo Credentials"
    override val keystoreSecurityNote = "Your credentials are encrypted and securely stored on your device using Android Keystore."
    override val selectInstitutionTitle = "Select Institution"
    override val searchUniversityPlaceholder = "Search by name, code or city..."
    override val selectLanguageSubtitle = "Choose language for the interface and Neptun"
    override val twoFactorHeader = "Two-Factor Authentication (2FA)"
    override val twoFactorAccountPrompt = { code: String -> "Neptun requires two-factor authentication for account $code." }
    override val twoFactorEmailTab = "Email Code"
    override val twoFactorTotpTab = "Authenticator App"
    override val twoFactorEmailPrompt = "Request a login code to your university email address:"
    override val twoFactorRequestingCode = "Requesting code..."
    override val twoFactorRequestEmailBtn = "Request Email Code"
    override val twoFactorCodeSent = "Code sent to your university account!"
    override val twoFactorCodePrefix = "Neptun generated prefix:"
    override val twoFactorEmailCodeInputLabel = "6-digit code from email"
    override val twoFactorResendBtn = "Request new code"
    override val twoFactorTestCodeBtn = "Test: 999999"
    override val twoFactorTotpPrompt = "Enter the 6-digit code from your Google / Microsoft Authenticator app:"
    override val twoFactorNoTotpNote = "Note: No TOTP key is paired with this account in Neptun. Please use the Email code option!"
    override val twoFactorTotpInputLabel = "TOTP Code (e.g. 482910)"
    override val twoFactorVerifyBtn = "Log In"

    // Theme Mode
    override val themeModeSystem = "System"
    override val themeModeLight = "Light"
    override val themeModeDark = "Dark"
    override val themeModeSystemDesc = "Follows system settings"
    override val themeModeLightDesc = "Always light appearance"
    override val themeModeDarkDesc = "Easy on the eyes in dark"

    // Accent Colors
    override val colorBlue = "Neptune Blue"
    override val colorIndigo = "Sapphire Indigo"
    override val colorCyan = "Turquoise Cyan"
    override val colorEmerald = "Emerald Green"
    override val colorGold = "Neptune Gold"
    override val colorPurple = "Amethyst Purple"
    override val colorCrimson = "Crimson Red"
    override val colorRose = "Rose Quartz"

    // Update Channel
    override val updateChannelStable = "Stable releases"
    override val updateChannelStableDesc = "Officially tested and reliable versions only"
    override val updateChannelDev = "Developer (Dev)"
    override val updateChannelDevDesc = "Latest developer builds and preview features"

    // Feedback messages
    override val languageChanged: (String) -> String = { lang -> "Language changed to: $lang" }
    override val languageChangeError: (String) -> String = { lang -> "Failed to change language to: $lang" }
    override val syncSuccess = "Sync successful! All data up to date."
    override val syncCompleted = "Synchronization completed."
    override val cacheClearedSuccess = "Local cache cleared. Fresh data will be downloaded on next sync."
    override val cacheClearFailed = "Failed to clear local cache."
    override val updateAvailableOnChannel: (String, String) -> String = { version, channel -> "New version ($version) available on $channel channel!" }
    override val appUpToDate: (String) -> String = { version -> "You are using the latest version ($version)." }
    override val githubReleasesPrompt = "Open GitHub Releases page to download."

    // In-App Update Dialog
    override val updateDialogNewVersionTitle = "Update available!"
    override val updateDialogReleaseNotes = "What's new & Changes:"
    override val updateDialogDefaultDesc = "A new version of Neptun Mobile is ready to update with bug fixes and performance improvements."
    override val updateDialogUpdateNow = "Update now"
    override val updateDialogLater = "Later"
    override val updateDialogDownloadingTitle: (Int) -> String = { progress -> "Downloading update... ($progress%)" }
    override val updateDialogVersion: (String) -> String = { version -> "Version: $version" }
    override val updateDialogBackground = "Background"
    override val updateDialogReadyTitle = "Download complete!"
    override val updateDialogReadyMessage: (String) -> String = { version -> "Update ($version) downloaded. Tap the button to install." }
    override val updateDialogOpenInstall = "Open Installer"
    override val updateDialogErrorTitle = "Update error"
    override val updateDialogGithubDownload = "GitHub download"

    // Biometric Lock Screen
    override val biometricLockedTitle = "App is locked"
    override val biometricLockedDesc = "Confirm your identity with fingerprint or face."
    override val biometricUnlockBtn = "Unlock"
    override val biometricNotAvailable = "Biometric authentication is not available on this device."

    // Crash Dialog
    override val crashDialogTitle = "App stopped unexpectedly"
    override val crashDialogDesc = "Crash log from previous run (copyable for bug report):"

    // Additional Timetable strings
    override val nextClassToday = "Next class today"
    override val noClassesScheduledForDay = "You have no classes scheduled for this day."
    override val noClassesThisDay = "No classes on this day this week."
    override val noRoomSpecified = "No room specified"
    override val timeSlot = "Time slot"

    // Additional Grades & Ghost Mark strings
    override val semesterStats = "Semester statistics"
    override val notEnoughDataForStats = "Not enough data for statistics yet."
    override val creditProgress = "Credit progress"
    override val creditProgressFormat: (Int, Int) -> String = { completed, target -> "$completed / $target credits" }
    override val creditProgressHint = "You can adjust the target under Settings → Personalization."
    override val creditsCount: (Int) -> String = { "$it credits" }
    override val signedStatus = "Signed"
    override val notRegisteredStatus = "Not registered"
    override val noDetailsInfo = "No details available"
    override val noGradeYet = "No grade yet"
    override val editGhostGrade: (Int) -> String = { "Edit simulated grade ($it)" }
    override val setGhostGradeTitle = "Set simulated grade"
    override val ghostGradeDescription = "Enter a virtual grade to immediately simulate your expected semester average and credit index."
    override val removeGhostGrade = "Remove simulated grade"
    override val ghostGradeSimulatorActive: (Int) -> String = { "$it simulated grade(s) active" }
    override val ghostCreditIndexSimulation: (Double) -> String = { "Simulated credit index: $it" }
    override val expectedAverageWithGhost = "Expected average with simulated grades"
    override val noExamsFound = "No exam data found."
    override val examsNotAvailableNotice = "Exam list is either unavailable on this university server or you have no registered exams. (Experimental feature)"

    // Additional Messages strings
    override val tapToViewFullMessage = "Tap to view full message..."
    override val noUnreadMessages = "No unread messages!"
    override val noMessagesInInbox = "No messages in your inbox."
    override val officialNotice = "Official Notice"
    override val downloadingMessageContent = "Downloading message content from Neptun..."

    // Demo and fallback banner
    override val demoModeBanner = "Demo mode – displayed data is simulated"
    override val fallbackSampleDataBanner = "Sample data displayed (synchronization failed)"

    // Additional Widget strings
    override val widgetTodayClasses = "· Today's classes"
    override val widgetNoMoreClasses = "No more classes today!"
    override val widgetMoreClassesCount: (Int) -> String = { "…and $it more classes" }

    // Notifications
    override val notifChannelClasses = "Timetable Notifications"
    override val notifChannelClassesDesc = "Reminders before classes begin with room information"
    override val notifChannelMessages = "Neptun Messages"
    override val notifChannelMessagesDesc = "Notifications about new official and instructor messages"
    override val notifChannelGrades = "Grades and Evaluations"
    override val notifChannelGradesDesc = "Notifications for newly posted grades and midterm results"
    override val notifChannelFinances = "Financial Notifications"
    override val notifChannelFinancesDesc = "Notifications for payment liabilities and deadlines"
    override val notifClassReminderTitle: (String) -> String = { "Starting soon: $it" }
    override val notifClassReminderText: (String, String, String) -> String = { type, time, room -> "$type at $time | Room: $room" }
    override val notifClassReminderBigText: (String, String, Int, String, String) -> String = { subject, type, minutes, time, room -> "Your class ($subject - $type) starts in $minutes minutes ($time) in room $room." }
    override val notifNewMessageTitle: (String) -> String = { "New message: $it" }
    override val notifNewGradeTitle: (String) -> String = { "New grade: $it" }
    override val notifNewGradeText: (String, Int) -> String = { grade, credit -> "Result: $grade ($credit credits)" }
    override val notifNewGradeBigText: (String, String, Int) -> String = { subject, grade, credit -> "New grade posted for $subject!\nGrade: $grade | Credits: $credit" }
    override val notifFinanceTitle: (String) -> String = { "Financial item: $it" }
    override val notifFinanceText: (String, String) -> String = { amount, due -> "Amount: $amount HUF | Due: $due" }
    override val notifFinanceBigText: (String, String, String) -> String = { title, amount, due -> "Attention! Payment due: $title\nAmount: $amount HUF\nDue date: $due" }
    override val notifMessagesSummaryTitle: (Int) -> String = { "$it new messages" }
    override val notifGradesSummaryTitle: (Int) -> String = { "$it new grades" }
    override val notifFinancesSummaryTitle: (Int) -> String = { "$it pending payment items" }
}

class GermanStrings : AppStrings {
    override val languageName: String = "Deutsch"
    override val languageCode: String = "de"

    override val navDashboard = "Übersicht"
    override val navTimetable = "Stundenplan"
    override val navGrades = "Noten"
    override val navMessages = "Nachrichten"
    override val navFinances = "Finanzen"
    override val navSettings = "Einstellungen"

    override val appName = "Neptun Mobile"
    override val save = "Speichern"
    override val cancel = "Abbrechen"
    override val close = "Schließen"
    override val copy = "Kopieren"
    override val later = "Später"
    override val sessionExpiredTitle = "Sitzung abgelaufen"
    override val sessionExpiredDesc = "Der Neptun-Server hat die Sitzung abgelehnt und sie konnte nicht automatisch erneuert werden. Bitte melden Sie sich erneut an, um die Daten zu aktualisieren."
    override val shareTimetableChooser = "Stundenplan teilen"
    override val delete = "Löschen"
    override val edit = "Bearbeiten"
    override val refresh = "Aktualisieren"
    override val search = "Suchen"
    override val ok = "OK"
    override val yes = "Ja"
    override val no = "Nein"
    override val retry = "Wiederholen"
    override val error = "Ein Fehler ist aufgetreten"
    override val success = "Erfolg"
    override val loading = "Laden..."
    override val empty = "Keine Daten"
    override val all = "Alle"
    override val details = "Details"
    override val select = "Auswählen"
    override val back = "Zurück"

    override val loginTitle = "Neptun Anmeldung"
    override val loginSubtitle = "Wählen Sie Ihre Universität und melden Sie sich an"
    override val selectUniversity = "Universität auswählen"
    override val searchUniversity = "Universität suchen..."
    override val neptunCode = "Neptun-Code"
    override val password = "Passwort"
    override val loginButton = "Anmelden"
    override val loggingIn = "Anmeldung läuft..."
    override val demoLogin = "Demo-Modus ausprobieren"
    override val offlineMode = "Offline fortfahren"
    override val offlineModeDesc = "Gespeicherte Daten ohne Internet ansehen"
    override val invalidNeptunCode = "Der Neptun-Code muss genau 6 Zeichen lang sein!"
    override val emptyPassword = "Das Passwortfeld darf nicht leer sein!"
    override val languageSelectTitle = "Sprache auswählen"
    override val universityLanguageInfo = "Vom Neptun-Server der Universität unterstützte Sprachen"

    override val twoFactorTitle = "Zwei-Faktor-Authentifizierung (2FA)"
    override val twoFactorSubtitle = "Geben Sie den Bestätigungscode ein"
    override val twoFactorEmailCode = "E-Mail-Code anfordern"
    override val twoFactorTotpCode = "Authenticator-App Code"
    override val twoFactorEnterCode = "Bestätigungscode"
    override val twoFactorVerify = "Bestätigen"
    override val twoFactorResendCode = "Neuen Code anfordern"
    override val twoFactorSuccess = "Erfolgreich authentifiziert!"

    override val dashboardGreeting = "Hallo"
    override val nextClass = "Nächste Vorlesung"
    override val todayClasses = "Heutiger Stundenplan"
    override val noClassesToday = "Heute keine Vorlesungen 🎉"
    override val noMoreClassesToday = "Für heute sind alle Vorlesungen vorbei! 🎉"
    override val viewFullTimetable = "Vollständigen Stundenplan öffnen"
    override val recentMessages = "Neueste Nachrichten"
    override val unreadMessagesCount = { count: Int -> "$count ungelesene Nachrichten" }
    override val quickStats = "Studienübersicht"
    override val currentAverage = "Gewichteter Notendurchschnitt"
    override val totalCredits = "Credits"
    override val pendingFinancesCount = { count: Int -> "$count offene Zahlung(en)" }

    override val timetableTitle = "Stundenplan"
    override val dayView = "Tag"
    override val weekView = "Woche"
    override val exportIcs = "Exportieren (.ics)"
    override val icsExportSuccess = "Stundenplan erfolgreich exportiert!"
    override val weekA = "Woche A (Ungerade)"
    override val weekB = "Woche B (Gerade)"
    override val monday = "Montag"
    override val tuesday = "Dienstag"
    override val wednesday = "Mittwoch"
    override val thursday = "Donnerstag"
    override val friday = "Freitag"
    override val saturday = "Samstag"
    override val sunday = "Sonntag"
    override val room = "Raum"
    override val instructor = "Dozent"
    override val courseCode = "Kurs-Code"

    override val gradesTitle = "Noten & Durchschnitte"
    override val weightedAverage = "Gewichteter Durchschnitt"
    override val creditIndex = "Kreditindex"
    override val enrolledCredits = "Belegte Credits"
    override val completedCredits = "Erreichte Credits"
    override val ghostGrades = "Simulierte Noten"
    override val ghostGradeSimulator = "Noten-Simulator"
    override val addGhostGrade = "Note simulieren"
    override val resetGhostGrades = "Simulierte Noten löschen"
    override val gradeText5 = "Sehr gut (5)"
    override val gradeText4 = "Gut (4)"
    override val gradeText3 = "Befriedigend (3)"
    override val gradeText2 = "Genügend (2)"
    override val gradeText1 = "Nicht genügend (1)"
    override val newEntry = "Neuer Eintrag"
    override val noGradesYet = "In diesem Semester liegen noch keine Noten vor"

    override val messagesTitle = "Nachrichten"
    override val officialMessage = "Offiziell"
    override val systemMessage = "Systemnachricht"
    override val markAsRead = "Als gelesen markieren"
    override val markAllAsRead = "Alle als gelesen markieren"
    override val unreadOnly = "Nur ungelesene"
    override val searchMessages = "Nachrichten suchen..."
    override val noMessages = "Keine Nachrichten"
    override val sender = "Absender"
    override val date = "Datum"

    override val financesTitle = "Finanzen"
    override val statusPending = "Offen"
    override val statusCompleted = "Bezahlt"
    override val statusOverdue = "Überfällig"
    override val dueDate = "Fälligkeitsdatum"
    override val paymentDate = "Zahlungsdatum"
    override val amount = "Betrag"
    override val noFinances = "Keine Finanzpositionen"

    // Course Types
    override val courseTypeLecture = "Vorlesung"
    override val courseTypePractice = "Übung"
    override val courseTypeLab = "Labor"
    override val courseTypeSeminar = "Seminar"
    override val courseTypeExam = "Prüfung"

    // Extra UI strings
    override val inProgressClass = "Laufende Vorlesung"
    override val allMessagesRead = "Alle Nachrichten gelesen"

    override val settingsTitle = "Einstellungen"
    override val sectionAccount = "Konto & Universität"
    override val sectionLanguage = "Sprache / Language"
    override val sectionTheme = "Erscheinungsbild & Design"
    override val sectionNotifications = "Benachrichtigungen"
    override val sectionPersonalization = "Personalisierung"
    override val sectionDataCache = "Daten & Cache"
    override val sectionAbout = "Über die App & Updates"
    override val logout = "Abmelden"
    override val logoutConfirmTitle = "Möchten Sie sich wirklich abmelden?"
    override val logoutConfirmMessage = "Anmeldedaten werden gelöscht, Benachrichtigungsstatus bleibt erhalten."
    override val loggedInAs = "Angemeldet als"
    override val university = "Universität"
    override val neptunServer = "Neptun-Server"
    override val languageDescription = "Wählen Sie die Sprache für die App und Neptun"
    override val languageSyncNeptun = "Mit Neptun-Server synchronisieren"
    override val languageChangedNotice = { lang: String -> "Sprache eingestellt auf: $lang" }
    override val themeMode = "Designmodus"
    override val themeSystem = "System"
    override val themeLight = "Hell"
    override val themeDark = "Dunkel"
    override val dynamicColors = "Material You Dynamische Farben"
    override val accentColor = "Akzentfarbe"
    override val notifyMessages = "Neue Nachrichten"
    override val notifyGrades = "Neue Noten"
    override val notifyFinances = "Offene Zahlungen"
    override val notifyClasses = "Stundenplan-Erinnerungen"
    override val classReminderTime = "Erinnerungszeit vor der Vorlesung"
    override val quietHours = "Ruhezeiten (Nicht stören)"
    override val clearCache = "Cache leeren"
    override val cacheCleared = "Cache erfolgreich geleert"
    override val checkUpdates = "Nach Updates suchen"
    override val appVersion = "App-Version"
    override val serverConnectionMode = "Verbindungsmodus"

    // Extended Settings Strings
    override val profileSubtitle = "Profil & Anpassung"
    override val studentDefaultName = "Student"
    override val studentDefaultUni = "Hochschule"
    override val studentDefaultProgram = "BSc Student"
    override val neptunCodeLabel = "Neptun-Code:"
    override val languageSubtitle = "Sprache der App und des Neptun-Servers"
    override val supportedLanguagesHeader = "Von der Institution unterstützte Sprachen"
    override val appearanceTitle = "Erscheinungsbild & Design"
    override val appearanceSubtitle = "Passen Sie das Erscheinungsbild der App an"
    override val themeModeLabel = "Designmodus"
    override val dynamicColorLabel = "Dynamische Farben"
    override val dynamicColorDesc = "Material You-Palette passend zum Hintergrundbild verwenden"
    override val dynamicColorOnlyAndroid12 = "Nur ab Android 12 verfügbar"
    override val dynamicColorActiveBanner = "Dynamische Farben sind aktiv. Die Auswahl einer Akzentfarbe deaktiviert dynamische Farben."
    override val accentColorTitle = "Akzentfarbe auswählen"
    override val accentColorSelected = "Ausgewählt:"
    override val customThemeBadge = "Eigenes Design"
    override val livePreviewTitle = "Live-Vorschau des Designs"
    override val activeThemeLabel = "Aktives Design:"
    override val btnPrimary = "Primär"
    override val btnTonal = "Tonal"
    override val btnOutlined = "Umrandet"
    override val notificationsTitle = "Benachrichtigungen & Erinnerungen"
    override val notificationsSubtitle = "Kategorien und Hintergrundbenachrichtigungen"
    override val notifPermissionGranted = "Benachrichtigungen aktiviert"
    override val notifPermissionRequired = "Benachrichtigungsberechtigung erforderlich"
    override val notifPermissionGrantedDesc = "Die App kann Stundenplan- und Studienbenachrichtigungen senden."
    override val notifPermissionRequiredDesc = "Tippen Sie hier, um die Berechtigung zu erteilen."
    override val grantPermissionBtn = "Berechtigung erteilen"
    override val notifClassesTitle = "Stundenplan-Erinnerungen"
    override val notifClassesDesc = "Erinnerung 15 Minuten vor Vorlesungen mit genauer Raumnummer"
    override val notifGradesTitle = "Noten und Bewertungen"
    override val notifGradesDesc = "Sofortige Benachrichtigung bei Eintragung einer neuen Note"
    override val notifMessagesTitle = "Neptun-Nachrichten"
    override val notifMessagesDesc = "Benachrichtigung bei Dozenten- und Systemnachrichten"
    override val notifFinancesTitle = "Finanzpositionen"
    override val notifFinancesDesc = "Erinnerungen an Gebühren, Zahlungen und Fristen"
    override val testClassBtn = "Vorlesung Test"
    override val testGradeBtn = "Noten Test"
    override val testMsgBtn = "Nachricht Test"
    override val testFinanceBtn = "Finanz Test"
    override val personalizationTitle = "Personalisierung"
    override val personalizationSubtitle = "Startbildschirm, Stundenplan- und Studieneinstellungen"
    override val startScreenLabel = "Startbildschirm"
    override val startScreenDesc = "Standardseite beim Öffnen der App."
    override val selectStartScreenPlaceholder = "Startbildschirm wählen"
    override val currentlyHiddenPage = "Derzeit ausgeblendete Seite"
    override val timetableSectionLabel = "Stundenplan"
    override val showWeekendLabel = "Wochenende anzeigen"
    override val showWeekendDesc = "Samstags- und Sonntagsspalten"
    override val visiblePagesLabel = "Sichtbare Seiten"
    override val visiblePagesDesc = "Blenden Sie nicht benötigte Seiten aus. Die Einstellungen bleiben immer sichtbar."
    override val targetCreditsLabel = "Ziel-Credits (Abschluss)"
    override val targetCreditsDesc = "Der Kreditfortschrittsbalken zeigt dieses Ziel im Noten-Tab."
    override val creditsUnit = "Credits"
    override val quietHoursLabel = "Ruhezeiten"
    override val quietHoursDesc = "In diesem Zeitraum werden keine Nachrichten-, Noten- oder Finanzbenachrichtigungen gesendet."
    override val activeWindowLabel = "Aktives Zeitfenster"
    override val securityTitle = "Sicherheit & Verschlüsselung"
    override val securitySubtitle = "Lokaler und hardwarebasierter Datenschutz"
    override val securityDescription = "Neptun-Anmeldedaten werden mit hardwaregeschützter Android Keystore-Verschlüsselung (AES-256) gespeichert. Stundenplan, Noten und Nachrichten werden für den Offline-Zugriff in einer lokalen Room-Datenbank gespeichert."
    override val biometricLockTitle = "Biometrische Sperre"
    override val biometricLockDesc = "Fingerabdruck oder Gesichtserkennung zum Öffnen erforderlich"
    override val lastSyncLabel = "Letzte erfolgreiche Synchronisierung:"
    override val notSyncedYet = "Noch nicht synchronisiert"
    override val appUpdatesTitle = "App & Updates"
    override val appUpdatesSubtitle = "Versionsverwaltung und GitHub Releases"
    override val updateChannelLabel = "Update-Kanal"
    override val installedVersionLabel = "Installierte Version"
    override val packageNameLabel = "Paketname"
    override val downloadNewVersionBtn = "Neue Version herunterladen"
    override val checkingUpdates = "Suche..."
    override val checkUpdatesBtn = "Nach Updates suchen"
    override val githubReleasesBtn = "GitHub Releases"
    override val syncInProgress = "Synchronisierung läuft..."
    override val manualSyncBtn = "Jetzt synchronisieren"
    override val exportIcsBtn = "Stundenplan exportieren (.ics)"
    override val clearCacheBtn = "Lokalen Cache leeren"
    override val clearingCacheInProgress = "Cache wird geleert..."
    override val logoutBtn = "Abmelden"
    override val logoutDialogTitle = "Abmelden"
    override val logoutDialogMessage = "Möchten Sie sich wirklich abmelden? Gespeicherte Anmeldedaten und Cache werden gelöscht."

    // Extended Login Strings
    override val loginBrandingSubtitle = "Moderner alternativer Neptun-Client mit 2FA-Unterstützung"
    override val institutionLabel = "Institution / Universität"
    override val selectUniversityPlaceholder = "Universität auswählen..."
    override val neptunCodeInputLabel = "Neptun-Code (6 Zeichen)"
    override val passwordInputLabel = "Passwort"
    override val quickDemoCredentials = "Offline-Demo-Daten laden"
    override val keystoreSecurityNote = "Ihre Anmeldedaten werden verschlüsselt und sicher auf Ihrem Gerät mit dem Android Keystore gespeichert."
    override val selectInstitutionTitle = "Institution auswählen"
    override val searchUniversityPlaceholder = "Suche nach Name, Code oder Stadt..."
    override val selectLanguageSubtitle = "Wählen Sie die Sprache für die Benutzeroberfläche und Neptun"
    override val twoFactorHeader = "Zwei-Faktor-Authentifizierung (2FA)"
    override val twoFactorAccountPrompt = { code: String -> "Neptun erfordert eine Zwei-Faktor-Authentifizierung für Konto $code." }
    override val twoFactorEmailTab = "E-Mail-Code"
    override val twoFactorTotpTab = "Authenticator-App"
    override val twoFactorEmailPrompt = "Fordern Sie einen Anmeldecode an Ihre Hochschul-E-Mail-Adresse an:"
    override val twoFactorRequestingCode = "Code wird angefordert..."
    override val twoFactorRequestEmailBtn = "E-Mail-Code anfordern"
    override val twoFactorCodeSent = "Code an Ihr Hochschulkonto gesendet!"
    override val twoFactorCodePrefix = "Vom Neptun generiertes Präfix:"
    override val twoFactorEmailCodeInputLabel = "6-stelliger Code aus der E-Mail"
    override val twoFactorResendBtn = "Neuen Code anfordern"
    override val twoFactorTestCodeBtn = "Test: 999999"
    override val twoFactorTotpPrompt = "Geben Sie den 6-stelligen Code aus der Authenticator-App ein:"
    override val twoFactorNoTotpNote = "Hinweis: Für dieses Konto ist noch kein TOTP-Schlüssel im Neptun gekoppelt. Bitte verwenden Sie die E-Mail-Option!"
    override val twoFactorTotpInputLabel = "TOTP-Code (z.B. 482910)"
    override val twoFactorVerifyBtn = "Anmelden"

    // Theme Mode
    override val themeModeSystem = "System"
    override val themeModeLight = "Hell"
    override val themeModeDark = "Dunkel"
    override val themeModeSystemDesc = "Folgt den Systemeinstellungen"
    override val themeModeLightDesc = "Dauerhaft helles Erscheinungsbild"
    override val themeModeDarkDesc = "Schont die Augen im Dunkeln"

    // Accent Colors
    override val colorBlue = "Neptun Blau"
    override val colorIndigo = "Saphir Indigo"
    override val colorCyan = "Türkis Cello"
    override val colorEmerald = "Smaragd Grün"
    override val colorGold = "Neptun Gold"
    override val colorPurple = "Amethyst Violett"
    override val colorCrimson = "Rubinrot"
    override val colorRose = "Rosenquarz"

    // Update Channel
    override val updateChannelStable = "Stabile Veröffentlichungen"
    override val updateChannelStableDesc = "Nur offiziell getestete, zuverlässige Versionen"
    override val updateChannelDev = "Entwickler (Dev)"
    override val updateChannelDevDesc = "Die neuesten Developer-Builds und Vorschau-Funktionen"

    // Feedback messages
    override val languageChanged: (String) -> String = { lang -> "Sprache geändert auf: $lang" }
    override val languageChangeError: (String) -> String = { lang -> "Fehler beim Ändern der Sprache auf: $lang" }
    override val syncSuccess = "Synchronisierung erfolgreich! Alle Daten sind aktuell."
    override val syncCompleted = "Synchronisierung abgeschlossen."
    override val cacheClearedSuccess = "Lokaler Cache geleert. Bei der nächsten Synchronisierung werden frische Daten heruntergeladen."
    override val cacheClearFailed = "Cache konnte nicht geleert werden."
    override val updateAvailableOnChannel: (String, String) -> String = { version, channel -> "Neue Version ($version) auf Kanal $channel verfügbar!" }
    override val appUpToDate: (String) -> String = { version -> "Sie verwenden die neueste Version ($version)." }
    override val githubReleasesPrompt = "Öffnen Sie die GitHub Releases Seite zum Herunterladen."

    // In-App Update Dialog
    override val updateDialogNewVersionTitle = "Update verfügbar!"
    override val updateDialogReleaseNotes = "Neuerungen und Änderungen:"
    override val updateDialogDefaultDesc = "Eine neue Version von Neptun Mobile steht mit Fehlerbehebungen und Leistungsverbesserungen bereit."
    override val updateDialogUpdateNow = "Jetzt aktualisieren"
    override val updateDialogLater = "Später"
    override val updateDialogDownloadingTitle: (Int) -> String = { progress -> "Update wird heruntergeladen... ($progress%)" }
    override val updateDialogVersion: (String) -> String = { version -> "Version: $version" }
    override val updateDialogBackground = "Hintergrund"
    override val updateDialogReadyTitle = "Download abgeschlossen!"
    override val updateDialogReadyMessage: (String) -> String = { version -> "Das Update ($version) wurde heruntergeladen. Tippen Sie auf die Schaltfläche zur Installation." }
    override val updateDialogOpenInstall = "Installer öffnen"
    override val updateDialogErrorTitle = "Update-Fehler"
    override val updateDialogGithubDownload = "GitHub Download"

    // Biometric Lock Screen
    override val biometricLockedTitle = "App ist gesperrt"
    override val biometricLockedDesc = "Bestätigen Sie Ihre Identität mit Fingerabdruck oder Gesicht."
    override val biometricUnlockBtn = "Entsperren"
    override val biometricNotAvailable = "Biometrische Authentifizierung ist auf diesem Gerät nicht verfügbar."

    // Crash Dialog
    override val crashDialogTitle = "Die Anwendung wurde unerwartet beendet"
    override val crashDialogDesc = "Fehlerprotokoll des vorherigen Laufs (für Fehlerbericht kopierbar):"

    // Additional Timetable strings
    override val nextClassToday = "Nächste Vorlesung heute"
    override val noClassesScheduledForDay = "Für diesen Tag sind keine Lehrveranstaltungen eingetragen."
    override val noClassesThisDay = "An diesem Tag finden diese Woche keine Lehrveranstaltungen statt."
    override val noRoomSpecified = "Kein Raum angegeben"
    override val timeSlot = "Zeitfenster"

    // Additional Grades & Ghost Mark strings
    override val semesterStats = "Semesterstatistiken"
    override val notEnoughDataForStats = "Noch nicht genügend Daten für Statistiken."
    override val creditProgress = "Kreditfortschritt"
    override val creditProgressFormat: (Int, Int) -> String = { completed, target -> "$completed / $target Credits" }
    override val creditProgressHint = "Sie können das Ziel unter Einstellungen → Personalisierung anpassen."
    override val creditsCount: (Int) -> String = { "$it Credits" }
    override val signedStatus = "Unterschrieben"
    override val notRegisteredStatus = "Nicht angemeldet"
    override val noDetailsInfo = "Keine Details verfügbar"
    override val noGradeYet = "Noch keine Note"
    override val editGhostGrade: (Int) -> String = { "Simulierte Note bearbeiten ($it)" }
    override val setGhostGradeTitle = "Simulierte Note festlegen"
    override val ghostGradeDescription = "Geben Sie eine virtuelle Note ein, um Ihren voraussichtlichen Notendurchschnitt und Kreditindex zu simulieren."
    override val removeGhostGrade = "Simulierte Note entfernen"
    override val ghostGradeSimulatorActive: (Int) -> String = { "$it simulierte Note(n) aktiv" }
    override val ghostCreditIndexSimulation: (Double) -> String = { "Simulierter Kreditindex: $it" }
    override val expectedAverageWithGhost = "Voraussichtlicher Durchschnitt mit simulierten Noten"
    override val noExamsFound = "Keine Prüfungsdaten gefunden."
    override val examsNotAvailableNotice = "Die Prüfungsliste ist auf diesem Universitätsserver nicht verfügbar oder Sie haben keine Prüfungen angemeldet. (Experimentelle Funktion)"

    // Additional Messages strings
    override val tapToViewFullMessage = "Tippen Sie hier, um die vollständige Nachricht zu lesen..."
    override val noUnreadMessages = "Keine ungelesenen Nachrichten!"
    override val noMessagesInInbox = "Keine Nachrichten in deinem Postfach."
    override val officialNotice = "Offizielle Mitteilung"
    override val downloadingMessageContent = "Lade Nachrichteninhalt von Neptun..."

    // Demo and fallback banner
    override val demoModeBanner = "Demo-Modus – angezeigte Daten sind nicht echt"
    override val fallbackSampleDataBanner = "Beispieldaten werden angezeigt (Synchronisierung fehlgeschlagen)"

    // Additional Widget strings
    override val widgetTodayClasses = "· Heutige Stunden"
    override val widgetNoMoreClasses = "Heute keine weiteren Stunden!"
    override val widgetMoreClassesCount: (Int) -> String = { "…und noch $it Stunden" }

    // Notifications
    override val notifChannelClasses = "Stundenplan-Benachrichtigungen"
    override val notifChannelClassesDesc = "Erinnerungen vor Beginn der Vorlesungen mit Raumangabe"
    override val notifChannelMessages = "Neptun-Nachrichten"
    override val notifChannelMessagesDesc = "Benachrichtigungen über neue Mitteilungen und Nachrichten"
    override val notifChannelGrades = "Noten und Bewertungen"
    override val notifChannelGradesDesc = "Benachrichtigungen über neue Noten und Prüfungsergebnisse"
    override val notifChannelFinances = "Finanzielle Benachrichtigungen"
    override val notifChannelFinancesDesc = "Benachrichtigungen über Zahlungsverpflichtungen und Fristen"
    override val notifClassReminderTitle: (String) -> String = { "Beginnt in Kürze: $it" }
    override val notifClassReminderText: (String, String, String) -> String = { type, time, room -> "$type um $time | Raum: $room" }
    override val notifClassReminderBigText: (String, String, Int, String, String) -> String = { subject, type, minutes, time, room -> "Deine Lehrveranstaltung ($subject - $type) beginnt in $minutes Minuten ($time) im Raum $room." }
    override val notifNewMessageTitle: (String) -> String = { "Neue Nachricht: $it" }
    override val notifNewGradeTitle: (String) -> String = { "Neue Note: $it" }
    override val notifNewGradeText: (String, Int) -> String = { grade, credit -> "Ergebnis: $grade ($credit Credits)" }
    override val notifNewGradeBigText: (String, String, Int) -> String = { subject, grade, credit -> "Neue Bewertung für $subject erhalten!\nNote: $grade | Credits: $credit" }
    override val notifFinanceTitle: (String) -> String = { "Finanzposten: $it" }
    override val notifFinanceText: (String, String) -> String = { amount, due -> "Betrag: $amount HUF | Fälligkeit: $due" }
    override val notifFinanceBigText: (String, String, String) -> String = { title, amount, due -> "Achtung! Ausstehende Zahlung: $title\nBetrag: $amount HUF\nFälligkeitsdatum: $due" }
    override val notifMessagesSummaryTitle: (Int) -> String = { "$it neue Nachrichten" }
    override val notifGradesSummaryTitle: (Int) -> String = { "$it neue Noten" }
    override val notifFinancesSummaryTitle: (Int) -> String = { "$it ausstehende Zahlungen" }
}

object AppStringsProvider {
    val HUNGARIAN: AppStrings = HungarianStrings()
    val ENGLISH: AppStrings = EnglishStrings()
    val GERMAN: AppStrings = GermanStrings()

    fun getForCode(code: String): AppStrings {
        return when (code.lowercase()) {
            "en" -> ENGLISH
            "de" -> GERMAN
            else -> HUNGARIAN
        }
    }

    fun getForContext(context: Context): AppStrings {
        return try {
            val prefs = context.getSharedPreferences("neptun_secure_storage", Context.MODE_PRIVATE)
            val fallback = context.getSharedPreferences("neptun_secure_storage_fallback", Context.MODE_PRIVATE)
            val code = prefs.getString("key_app_language_code", null)
                ?: fallback.getString("key_app_language_code", null)
                ?: "hu"
            getForCode(code)
        } catch (e: Exception) {
            HUNGARIAN
        }
    }
}

val LocalAppStrings = compositionLocalOf<AppStrings> { AppStringsProvider.HUNGARIAN }

@Composable
fun currentStrings(): AppStrings = LocalAppStrings.current
