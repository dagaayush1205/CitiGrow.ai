# CitiGrow.ai Android

Native Kotlin / Jetpack Compose implementation of the supplied [handoff](HANDOFF.md) and [interactive reference](reference/citigrow-prototype.dc.html). Android 7.0+ (API 24).

## Run

Open this directory in Android Studio, allow Gradle to sync, and run `app` on a device or emulator. Use JDK 17 or newer and Android SDK 36. Set your SDK location in `local.properties` if needed.

```sh
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` after a successful Gradle build.

Demo access: Ram **1111**, Lakshman **2222**, parent **3333**, teacher **4444**. Start with Ram for a populated journey or Lakshman for discovery. New profiles start with sharing disabled. Role PINs demonstrate the flow; they are not production authentication.

## Implemented

- English, Hindi and Tamil interface dictionaries from the prototype, with English fallback. Content remains mostly English and needs translation review.
- Language selection, role selection, shared-phone profiles, PIN validation and throttling, three-step registration, and demo guardian consent.
- Student Home / Discover / Learn / Profile; discovery conversation with multilingual keyword rules; all 12 supplied careers and explanations; career comparison and two career scenarios.
- Nine-question Skills Compass, skill radar and practice suggestions; adaptive seven-step Life Map; eight sample courses, offline saving, lesson checks and four quizzes.
- Thirteen sample opportunities, explicit eligibility reasons, income categories, confirmations, saved application checklists, and external official portal links.
- Teacher-mediated partner interests and group-seat requests; achievements, fictional success stories, read-aloud preferences and privacy controls.
- Parent summary with sharing controls, usage bars, costs, financial help and a suggested family-phone schedule. Student conversations are never included.
- Teacher Overview / Students / Courses / Profile, assisted profile creation, student-PIN access to assisted sessions, local recommendations, module publishing, demo enrolments and simulated earnings.
- Partner contribution form, safety agreement, planned verification steps and persisted pending submissions.
- Persistent local JSON state, per-profile salted PIN hashes, a durable local event queue, and explicit offline simulation. No server upload is performed or claimed. Backups and shared-preference device transfer are disabled.
- Voice engine interface: bundled `assets/audio/<language>/<string_key>.ogg` clips, an injectable on-device engine slot, and honest caption fallback. New profiles request read-aloud on screen entry; Stop and manual listening controls are available. No audio assets or TTS models are bundled yet.

## Deliberate limitations

This is a functional local hackathon demo, not a production pilot. There is no backend, identity verification, real partner verification, payments, message delivery, live listing refresh, speech recognition, downloaded media, or AI tutor. “Online” / “Offline demo” is a manual simulation, not a network detector. Saving a course marks already bundled text as available offline. The event queue remains local even when the toggle returns online.

Lesson screens use the supplied course summaries and checks; full per-lesson teaching content still needs authoring. Newly created teacher modules are visible as local descriptions and demo-seat interests, without lesson uploads. Pricing is illustrative; paid versus subscription billing remains undecided. Educational advertisements are static, fictional examples with no targeting or ad network. Sample earnings are not money received.

Career requirements, eligibility rules, sample dates and exam mappings are imported from the prototype and labelled unverified. No real submission or guaranteed eligibility is claimed. Unverified deep application links are not opened. The source catalogue has 12 careers; expansion to 30–40 officially grounded careers remains future work.

Noto Sans fonts for English, Hindi and Tamil are bundled for offline use. The prototype's Baloo display fonts are still unavailable; headings use Noto at the specified sizes and weights until those assets are supplied. Hindi and Tamil text needs native-speaker review. Room, WorkManager and a real sync protocol remain integration work once a server contract exists. The local repository is intentionally independent of the UI to support that migration.

## UI reference alignment

The visual reference is `reference/citigrow-prototype.dc.html`, not the original purple landing-screen stub. The Compose design tokens now follow its CSS: navy #102A43, blue #1677C8, teal #19A38C, background #F7F9FC, muted text #52606D and border #E4E9F0. Cards use 16dp padding and 20dp corners; primary buttons are 52dp, chips 44dp, page gutters 16dp, and page gaps 14dp. The original SVG icon paths are imported into `assets/prototype-icons.json` by the data import script and drawn as native vectors.

The welcome illustration follows the reference SVG coordinates. Onboarding uses three language tiles, colored role cards, profile avatars and a PIN keypad. The student shell uses a white header, connection pill, profile avatar and compact white bottom navigation. Home restores the blue next-step panel and shortcut tiles; Discover restores its navy conversation banner, horizontal stories and career badges. Learn uses a gold Skills Compass card and progress-based course cards. Life Map uses the reference's winding path coordinates, alternate branch, selectable steps and weekly bars. Parent usage is a seven-day chart with a goal line; secondary screens share navy hero panels and the same component styling.

This alignment has been checked against the prototype markup and CSS. It has **not** been verified by comparing device screenshots: ADB and Gradle cannot start their socket services in the managed workspace. Exact visual parity is still pending a rendered device comparison and the missing Baloo font assets.

## Source layout

`data/GrowStore.kt` stores local records and reads the catalogue. `data/GrowRules.kt` implements matching, eligibility, discovery and assessment logic. `ui/` contains Compose screens; `voice/` contains the swappable audio pipeline. All paths are under `app/src/main/java/com/example/citigrow/`.

The reference is retained verbatim. Regenerate bundled reference data with:

```sh
node scripts/import-prototype.cjs
```

## Verification in the managed workspace

Gradle startup is blocked here because the sandbox denies socket creation, including network-interface discovery used by Gradle's file-lock service. A cached-dependency checker compiles all Kotlin/Compose source and device-test source without Gradle, then runs ten deterministic rule tests using the SDK's real JSON implementation:

```sh
node scripts/check-kotlin.cjs --test
```

Set `CITIGROW_DEPENDENCY_CACHE` and `ANDROID_HOME` to override its local cache and SDK paths. This fallback requires a populated dependency cache and SDK sources; it does not package an APK or run Compose/device tests. Device tests cover shared-phone PIN entry and local persistence, and should run on a fresh demo installation in Android Studio. An emulator/device run, accessibility pass and release build remain necessary before distribution.

Portal roots checked while implementing: [NSP](https://scholarships.gov.in/), [DIKSHA](https://diksha.gov.in/), [SWAYAM](https://swayam.gov.in/), [AISHE](https://aishe.gov.in/). The [apprenticeship](https://www.apprenticeshipindia.gov.in/) and [Skill India Digital](https://www.skillindiadigital.gov.in/) sites did not return readable content to the verification tool; their links remain portal references, not verified listing evidence.
