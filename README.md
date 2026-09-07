# Neptun Mobile

Modern alternatív Neptun kliens magyar egyetemistáknak: órarend, jegyek, szellemjegy-kalkulátor,
üzenetek, pénzügyek és értesítések – egy Jetpack Compose appban.

## Arculat: „Filc” skin

A teljes felületet a [reFilc](https://github.com/QwIT-Development/app-legacy) (e-KRÉTA napló)
vizuális világa szerint építettük át: pasztilles lime háttér, kártyalapú elrendezés, pilula
indikátoros alsó navigáció, Montserrat tipográfia. A design nyers értékei a
`app/src/main/java/com/example/ui/theme/` alatt élnek, a komponensek a
`presentation/ui/components/Filc*.kt` fájlokban.

| | fény | sötét |
|---|---|---|
| háttér | `#FAFFF0` | `#0D1202` |
| lap / kártya | `#F3FBDE` | `#141905` |
| gombfelület | `#FEFFFD` | `#20290B` |
| accent | `#A7DC22` | `#A7DC22` |
| ikon (kék) | `#0A2456` | `#BAD1FF` |

Az accent nem marad egy színre ragasztva: a `FilcPalette.of(accent, dark)` a reFilc
`ColorsUtils().lighten/darken` képletével bármelyik, a felhasználó által választott accentből (12 választható szín + Material You)
legenerálja a teljes palettát – a lime esetén az eredmény pontosan a fenti referenciaérték.

Jegyszínek (5 → 1): `#22CCAD`, `#92EA3B`, `#F9CF00`, `#FFA046`, `#FF54A1`, ugyanaz fényben és sötétben.

## Képernyők

- **Kezdőlap** (`HomeScreen` + `HomeViewModel`) – köszöntős fejléc, „élő óra” kártya (most mi következik,
  quantized progress-szel és visszaszámlálóval), gyors statisztikák (átlag, előző félévhez viszonyított
  változás, kredit, olvasatlan üzenet), mai órák, legutóbbi jegyek, üzenetek, fizetendő.
- **Órarend** – heti szűrősáv (H–P), hétlapok, aktualitás szerint jelzett órák, egyedi emlékeztető-periódus (5–60 perc), „óra részletei” alsó lap.
- **Jegyek** – félév választó, átlag és kredit index, tantárgyankénti lista,
  szellemjegy-kalkulátor párbeszéddel (átlag „mi lenne ha” számolás).
- **Üzenetek** – szűrő (olvasatlan), levél lista, tartalom betöltése, szövegre tisztított nézet.
- **Pénzügyek** – díjfizetési tételek, fizetendő / határidő alatt státuszjelzőkkel, szűrősávval.
- **Bejelentkezés** – a reFilc v5 login képe: márkaszöveg felül, kártya alul,
  12 dp-es, accent szélű mezők, szem ikon a jelszóhoz, egyetemválasztó alsó lap, kétlépcsős
  azonosítás blokk.
- **Profil / beállítások** – nem tab: a fejléc avatárjából nyílik (úgy, mint a reFilcben).
  Témamód, dynamic color, accentválasztó, értesítések, szinkron, frissítés-ellenőrzés, kijelentkezés.

## Design komponensek

`FilcPanel`, `FilcCard`, `FilcPanelButton`, `FilcChip`, `FilcFilterBar`, `FilcRoundIcon`,
`FilcProgressBar`, `FilcBottomSheet`, `FilcAvatar`, `FilcSwitch`, `FilcStatTile`, `FilcEmptyState`,
`GradeCircle` / `GradeBadge`, `AveragePill` (szellem („ghost”) pontozással), `TrendDisplay`,
`FinanceStatusBadge`, `UnreadDot`, `FilcBottomBar`, `FilcTopBar` / `FilcBackBar`, `FilcLessonTile`.

A magyar számformátum (tizedesvessző) a `formatHungarian()` segédből jön, így az átlag
`4,07`, a kredit index `2,53` alakban jelenik meg – akárcsak a Neptunban.

## Fejlesztés

```bash
./gradlew assembleDebug        # APK
./gradlew test                 # unit + Robolectric/Roborazzi tesztek
./gradlew recordRoborazziDebug # golden screenshot felvétele
```

A tesztek: `SettingsThemeTest` (téma-beállítások tárolása és visszaolvasása,
Filc-paletta értékek, régi accent-idk feloldása), `StudyProfileTest` (a Kezdőlap
profil-számításai), `GreetingScreenshotTest` (login golden kép).

## CI

Minden push és PR lefordul a GitHub Actions `CI` munkafolyamatban:
`:app:assembleDebug`, `:app:assembleDebugAndroidTest` (az androidTest forrásokat
is ellenőrzi) és `:app:testDebugUnitTest`. A fordítási hibákat és a tesztbukásokat
a workflow annotációként **és** a commit kommentjeként is visszaírja, így a napló
letöltése nélkül is azonnal látszik, mi romlott el:

```bash
gh api repos/<owner>/<repo>/commits/<sha>/comments --jq '.[0].body'
```

Néhány dolog, amire figyelni kell:

- a Robolectric 4.16 az SDK 36-hoz Java 21-et kér, ezért a CI JDK-ja 21
  (az `android-release.yml` APK-építéséhez a 17 elég),
- a CI felülírja a Kotlin fordító `in-process` stratégiáját (`daemon`), mert a
  `gradle.properties` beállítása a KSP2-vel összeütközik – helyben nem változott semmi,
- UI-váltás után a `app/src/test/screenshots/greeting.png` golden képet újra kell
  rögzíteni: helyben `./gradlew :app:recordRoborazziDebug`, vagy az Actions
  *Run workflow* űrlapján az `update_golden` bepipálása, ami a CI által rögzített
  képet visszacommitolja a branchre.
