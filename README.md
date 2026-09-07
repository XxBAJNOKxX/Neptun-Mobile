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

A tesztaknák: `SettingsThemeTest` (téma/előzmény-beállítások, paletta-értékek),
`StudyProfileTest` (a Kezdőlap profil-számításai), `GreetingScreenshotTest` (login screenshot).
