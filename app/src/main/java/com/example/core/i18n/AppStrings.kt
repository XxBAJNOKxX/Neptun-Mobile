package com.example.core.i18n

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
}

val LocalAppStrings = compositionLocalOf<AppStrings> { AppStringsProvider.HUNGARIAN }

@Composable
fun currentStrings(): AppStrings = LocalAppStrings.current
