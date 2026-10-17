# CitiGrow.ai Android

Native Kotlin / Jetpack Compose implementation of the supplied [handoff](HANDOFF.md) and [interactive reference](reference/citigrow-prototype.dc.html). Android 7.0+ (API 24).

## Product purpose and readiness

CitiGrow helps students turn interests, everyday experience and available study time into a practical learning and career pathway. It connects discovery, skills checks, learning, opportunity preparation and family/teacher support in one local-first Android experience. Its intended users include learners who share phones, have intermittent connectivity, prefer regional-language guidance, or need help navigating education and livelihood options.

The product hypothesis is that understandable guidance and a clear next action can reduce the effort between discovering a pathway and taking a useful step toward it. This is a genuine problem to investigate with learners and facilitators, not a claim of validated demand: the repository contains no customer research, paid adoption, placement results or measured income uplift.

Current readiness: a functional local prototype, not a production service. The sections below distinguish implemented capabilities, engineering assessment, commercial hypotheses and outcomes still to be measured. No revenue, usability, scale or livelihood-impact figures are presented as observed results.

## Technical soundness, architecture and maintainability

### Is the solution technically sound and realistically implementable?

Yes for demonstrating the scoped local workflow. Kotlin/Compose provides a native Android interface; deterministic rules produce explainable suggestions; local persistence supports continuity without a server; and on-device speech avoids a speech-generation backend. Host source checks, 17 deterministic tests and Python inference with the supplied model have passed. APK packaging, Android ONNX JNI, device playback and rendered UI still need device verification. These checks do not establish production reliability.

### Is the architecture well-designed and maintainable?

It has useful boundaries: `ui/` renders screens, `GrowRules` handles matching/eligibility, `GrowStore` owns local records, and `voice/` separates language routing, synthesis and playback. Static content and prompts are assets rather than embedded model outputs. The narration interface also supports testing highlight behavior without a real speech engine.

The architecture is appropriate for a prototype, but not yet a mature production architecture. Screens mutate shared JSON records, persistence rewrites a shared-preferences JSON document, content lacks a managed publication lifecycle, and the reflection-based ONNX bridge cannot be API-checked by the fallback compiler. A small catalogue is maintainable today; growing teams and datasets would benefit from typed models, explicit screen state/ViewModels, repository interfaces, schema migrations, transactional Room storage, feature-level ownership and CI that builds real APKs and exercises JNI/device tests. Keep rule, prompt and tokenizer regression tests when making those changes.

### Does it address security, reliability, integrations and performance?

| Area | Implemented today | Required before a real deployment |
| --- | --- | --- |
| Security and privacy | App-private data; salted PIN hashes and attempt throttling; backup/device transfer disabled; sharing off for new profiles; no microphone permission; registration narration excludes entered values | Replace demonstration role PINs and fast PIN hashing with a reviewed authentication design; enforce role/tenant authorization on a backend; assess device encryption, recovery, retention/deletion and child safeguarding; verify guardian consent rather than treating a checkbox as proof |
| Reliability | Local persistence, cancellable playback, audio-focus handling, bounded speech caches, model checksum verification and explicit voice errors | Transactional persistence and migrations, crash/recovery tests, storage-pressure handling, monitored releases, rollback procedures and tested sync/conflict handling. The event queue does not currently upload anything |
| Integrations | External portal links and local demonstration records; offline Android TTS and bundled ONNX runtime integration | Verified provider contracts/APIs, authenticated adapters, idempotent requests, retry limits, source freshness checks and reconciliation. No live application submission, payment, identity or LMS integration is implemented |
| Performance | Lazy model loading, serialized background CPU inference, reused sessions and generated-audio caching | Benchmark cold/warm latency, memory, battery, app size and thermal behavior on low-cost phones; test rapid cancellation and concurrent app use. The approximately 118 MB model plus its installed copy is a material storage/distribution cost |

Device-side generation can avoid per-request cloud speech costs, but shifts compute, battery and storage costs to the phone. No phone latency or capacity guarantee is established. Model licensing/attribution and dependency updates must remain part of release management. Security and safeguarding review are release gates, not completed certifications.

### Does it offer AI-powered personalization and intelligence?

Partially, with an important distinction:

- AI inference is implemented for Tamil/Malayalam speech synthesis. It improves access to instructions; it is not a career-reasoning model.
- Personalization is implemented through rules: discovery answers and interests, assessment skill gaps, prior experiences, budget and relocation constraints influence career suggestions; available study time influences pathway pacing. Recommendations include reasons and gaps.
- The displayed match percentage is a heuristic score, not a calibrated probability of employment, course completion or success. Skills checks are demonstration assessments, not validated psychometric instruments.
- There is no trained recommendation model, LLM tutor, predictive placement engine or learning-from-user-feedback pipeline today. An intelligent-advising roadmap could add verified-content retrieval and evaluated ranking with consent, fairness checks, explanations, human escalation and versioned evaluations. It must not invent eligibility, promise jobs or automatically exclude learners from opportunities.

## Usability, workflow and adoption

### Is it easy to understand, intuitive and quick to learn?

The design aims to be: language selection comes first; onboarding is divided into three steps; dropdowns reduce ambiguous entry; and spoken field instructions follow a visible blue highlight. Replay and Stop give users control. Home, Discover, Learn and Profile separate the main tasks; the Life Map supplies an ordered next action. Shared-phone profiles and teacher-assisted creation address assisted use.

These are usability design choices, not proof that users find the interface intuitive. Regional-language coverage is incomplete, some options remain English, voice availability differs by device, PINs may create friction, and the large model may deter installation. Validate with first-time learners across literacy levels, assistive technologies, languages and low-cost/shared devices. Measure onboarding completion, time to first useful recommendation, unassisted task completion, replay/error rates and whether users can explain their next step after one session. Device accessibility and native-speaker reviews remain pending.

### Does it simplify workflows and provide a seamless journey?

The intended journey is:

1. Choose a language and create or unlock a private student profile.
2. Describe interests and constraints, then compare explained career suggestions.
3. Check skills and follow a paced Life Map with learning and practice.
4. Review relevant opportunities, identify eligibility gaps and prepare a document checklist.
5. Involve a teacher or parent through explicit sharing controls and review progress.

This consolidates planning and preparation that would otherwise require separate notes, repeated explanations and unstructured searching. Learners retain their local progress; teachers can guide multiple students; parents see a simpler summary rather than private conversations. Actual time savings have not been measured.

The local demonstration journey is connected, but the complete service journey is not yet seamless. Official applications leave the app, listings are unverified samples, full lessons are not authored, partner verification is simulated, and there is no confirmed enrolment/placement feedback loop. Production needs verified opportunity handoffs, clear status/ownership, accessible support and consented outcome tracking before claiming end-to-end delivery.

### Can it grow from 10 users to 10,000 and across regions or organizations?

Local inference and storage mean each installation handles its own speech and records; there is no central speech endpoint to saturate. That makes wider installation plausible, but 10,000 installations are not the same as a tested 10,000-user service. Current profiles are device-local, and there is no cross-device identity, organization management, central reporting or support operation. User count alone is also insufficient for capacity planning: measure active users, requests, content downloads and peak concurrency.

A staged adoption plan is:

- **10 users:** verify device builds, guided tasks, language comprehension and safeguarding in a supervised demonstration; collect usability feedback with consent.
- **100–1,000 users:** only after readiness gates, pilot typed transactional storage, authenticated tenant-scoped APIs, real connectivity detection, durable background sync and conflict resolution; establish verified content, facilitator training and support ownership.
- **10,000 users:** load-test the backend against measured traffic and bursts; use indexed/paginated data access, queued long-running work and managed content distribution; define service targets, monitoring, incident response, restore tests and cost limits. Separate organizations' permissions/data and test those boundaries.

Broader customer/business-unit adoption also needs configurable programmes and curricula rather than customer-specific code forks, accessible onboarding, organizational reporting and a repeatable rollout/support playbook. Regional expansion needs reviewed translations, supported voices, local curricula and verified region-specific opportunities. Listing every state/union territory in a form does not mean the service has verified coverage there. None of these production-scale capabilities should be inferred from the local demo.

## Revenue, efficiency and business case

### Does it create revenue opportunities?

Potentially. The strongest hypotheses are institution-funded access for schools, training organizations or livelihood programmes; implementation/training services; and paid organizational reporting or cohort-management features once those services exist. Verified partner programmes could fund access while keeping basic learner guidance affordable. These are proposed models, not existing customers, contracts or recurring revenue. Purchaser willingness to pay, procurement cycles and delivery costs need validation.

Revenue should not depend on selling student data, pay-to-rank career advice or charging for unverified job promises. Any future employer/provider fees need transparent disclosure and separation from recommendation quality. Current prices, course purchases, partner submissions and teacher earnings are demonstrations; payment collection is not implemented.

### Does it reduce costs and improve operational efficiency?

The proposed efficiency gains are less repetitive form explanation through read-aloud guidance, reusable eligibility/document checklists, shared cohort visibility and clearer family/teacher coordination. Local speech avoids a metered cloud-speech call for each replay. These gains could free facilitator time, reduce repeated visits and improve preparedness, but the prototype has no operational study demonstrating them. Model storage, device support, translation, verified-content operations and staff training can offset savings.

The business case is compelling only if institutions value the outcomes enough to fund delivery and measured benefits exceed full lifecycle costs. Validate with a named buyer, a baseline workflow, a paid or explicitly budgeted pilot and a plan for maintaining verified opportunities. Track cost per active learner, facilitator time per learner, retention, partner renewal, contribution margin and cost per verified progression outcome—not downloads alone.

### What is the potential ROI?

ROI is not established. Use a purchaser-specific model rather than treating all social benefits as vendor revenue:

`Annual operational ROI = (annual realizable benefits − annual total cost) / annual total cost × 100`

Illustrative assumptions only: a programme serves 1,000 learners; each completes four assisted tasks per year; each task saves 10 facilitator minutes; and staff time is valued at ₹300/hour. This gives approximately 667 hours or ₹200,000 of annual capacity value. If the all-in annual cost were ₹150,000, net capacity value would be ₹50,000 and ROI approximately 33%. At five minutes saved per task, value falls to ₹100,000 and ROI becomes approximately −33%; break-even is 7.5 minutes saved per task under these same assumptions.

These figures are an arithmetic example, not observed savings, a market price or a budget quote. Time released becomes a cash saving only if staffing/overtime costs are actually avoided; otherwise it is capacity for additional service. Include software delivery/licensing, devices/data, onboarding, translation, content verification, support, hosting/sync, monitoring and maintenance in the real cost estimate. Avoid counting the same staff saving twice. Measure learner income and broader social value separately from the institution's financial ROI.

## Livelihood, economic outcomes and customer value

### Does it solve a genuine pain point and materially improve lives or work?

The target pain points are difficulty understanding career choices, fragmented learning/application steps, uncertainty about requirements and costs, language barriers and limited access to individual guidance. CitiGrow addresses these through explained pathways, skills-gap practice, paced next steps, read-aloud instructions, preparation checklists and family/teacher support. If validated, this could improve confidence and access while reducing avoidable effort for learners and facilitators.

The livelihood mechanism is a hypothesis: better understanding → a feasible pathway → relevant skill practice → a completed, eligible application → training/work access → sustained economic benefit. The app directly demonstrates the early guidance/preparation steps. Later outcomes depend on verified supply, training quality, affordability, employer demand, learner circumstances and human support. It cannot guarantee admission, funding, placement or income, and it must not steer minors into inappropriate work.

### How significant is the improvement, and how will it be proved?

No quantitative livelihood or income improvement has been demonstrated. A pilot should establish baseline access and outcomes, use a comparable non-user group or staged rollout where feasible, and obtain informed consent for follow-up. Set targets before collecting results and report sample size, missing follow-ups and attribution limits.

| Outcome layer | Suggested measures |
| --- | --- |
| Understanding and experience | First-session task success; comprehension of requirements/next steps; satisfaction; language/accessibility issues; complaints |
| Learning and preparation | Skills improvement using a validated assessment; course completion; application completeness; avoided repeat visits; time/cost to prepare |
| Access and progression | Verified eligible applications; scholarship/training admissions; completed training; age-appropriate placement or progression; time to outcome |
| Sustained economic value | Baseline versus 3/6/12-month earnings, hours worked and job retention for relevant consenting participants; training/travel costs; net rather than gross benefit |
| Inclusion and safety | Outcome differences by region, language, gender and access needs where appropriate and consented; harmful advice, privacy incidents, grievance resolution and dropout reasons |

Positive user experience is an intended design outcome, not an established result. A strong pilot would show that users understand and complete tasks more easily, receive accurate opportunities, and progress to independently verified outcomes without sacrificing privacy or safety. Report failures and exclusion as well as success stories. Until that evidence exists, position CitiGrow as a promising guidance prototype rather than a proven income-improvement platform.

## Run

Open this directory in Android Studio, allow Gradle to sync, and run `app` on a device or emulator. Use JDK 17 or newer and Android SDK 36. Set your SDK location in `local.properties` if needed.

```sh
./gradlew :app:assembleDebug
./gradlew :app:connectedDebugAndroidTest
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk` after a successful Gradle build.

Demo access: Ram **1111**, Lakshman **2222**, parent **3333**, teacher **4444**. Start with Ram for a populated journey or Lakshman for discovery. New profiles start with sharing disabled. Role PINs demonstrate the flow; they are not production authentication.

## Implemented

- English, Hindi and Tamil interface dictionaries from the prototype, plus Malayalam onboarding labels/instructions and basic navigation, with English fallback. Content remains mostly English and needs translation review.
- Language selection, role selection, shared-phone profiles, PIN validation and throttling, three-step registration, and demo guardian consent.
- Student Home / Discover / Learn / Profile; discovery conversation with multilingual keyword rules; all 12 supplied careers and explanations; career comparison and two career scenarios.
- Nine-question Skills Compass, skill radar and practice suggestions; adaptive seven-step Life Map; eight sample courses, offline saving, lesson checks and four quizzes.
- Thirteen sample opportunities, explicit eligibility reasons, income categories, confirmations, saved application checklists, and external official portal links.
- Teacher-mediated partner interests and group-seat requests; achievements, fictional success stories, read-aloud preferences and privacy controls.
- Parent summary with sharing controls, usage bars, costs, financial help and a suggested family-phone schedule. Student conversations are never included.
- Teacher Overview / Students / Courses / Profile, assisted profile creation, student-PIN access to assisted sessions, local recommendations, module publishing, demo enrolments and simulated earnings.
- Partner contribution form, safety agreement, planned verification steps and persisted pending submissions.
- Persistent local JSON state, per-profile salted PIN hashes, a durable local event queue, and explicit offline simulation. No server upload is performed or claimed. Backups and shared-preference device transfer are disabled.
- On-device speech: bundled VITS ONNX inference for Tamil/Malayalam and installed offline Android voices for English/Hindi. Child-profile onboarding reads instructions in order, scrolls to each field and gently highlights it during playback. Replay, per-field listening and Stop controls are available; typing or opening a dropdown stops narration.

## On-device narration

See the [on-device speech README](docs/on-device-speech/README.md) for setup, language routing, Malayalam onboarding, implementation details, tests and troubleshooting.

Keep the validated model at `tests/model.onnx`. Gradle copies it into generated app assets during the build; do not also copy it into `src/main/assets`. The model adds approximately 118 MB before ONNX Runtime libraries. The first Tamil/Malayalam request installs a checksum-verified app-private copy, so allow additional device storage. Both languages share this copy. No model download or speech server is used.

The supplied [AI4Bharat VITS Rasa model](https://huggingface.co/ai4bharat/vits_rasa_13) supports thirteen languages, including Tamil and Malayalam, but not English or Hindi. The app enables Tamil speaker 18 and Malayalam speaker 11, both with emotion 0, the matching character vocabulary and 24,000 Hz mono WAV output. English/Hindi require an installed, non-network Android TTS voice; if one is absent, the app explains that it must be installed through Android text-to-speech settings. It does not silently use cloud speech. See [model attribution](app/src/main/assets/tts/rasa/NOTICE.md).

Registration narration uses only translated labels and instructions, never entered names, PINs or other form values. Highlighting follows actual playback rather than a timer. Navigation, backgrounding and Stop cancel playback; generated audio is cached privately with size limits. First-run ONNX generation may take longer while the model loads. Speech input is not implemented and no microphone permission is requested. Hindi/Tamil/Malayalam instructions still need native-speaker review. Malayalam uses Android's offline system font fallback; dropdown option values and untranslated screens still use English.

The model/tokenizer fixtures can be regenerated and checked using the Python environment containing ONNX Runtime and Transformers:

```sh
HF_HUB_OFFLINE=1 .venv/bin/python scripts/prepare-voice-assets.py
HF_HUB_OFFLINE=1 .venv/bin/python scripts/verify-voice-model.py
```

These scripts require the existing cached Hugging Face tokenizer for the pinned revision in `tts/rasa/config.json`. They do not replace Android testing. After Gradle sync, run `OnnxSpeechDeviceTest` to validate the packaged JNI runtime and `RegistrationNarrationTest` to check playback-driven highlighting on a device.

## Deliberate limitations

This is a functional local hackathon demo, not a production pilot. There is no backend, identity verification, real partner verification, payments, message delivery, live listing refresh, speech recognition, downloaded media, or AI tutor. “Online” / “Offline demo” is a manual simulation, not a network detector. Saving a course marks already bundled text as available offline. The event queue remains local even when the toggle returns online.

Lesson screens use the supplied course summaries and checks; full per-lesson teaching content still needs authoring. Newly created teacher modules are visible as local descriptions and demo-seat interests, without lesson uploads. Pricing is illustrative; paid versus subscription billing remains undecided. Educational advertisements are static, fictional examples with no targeting or ad network. Sample earnings are not money received.

Career requirements, eligibility rules, sample dates and exam mappings are imported from the prototype and labelled unverified. No real submission or guaranteed eligibility is claimed. Unverified deep application links are not opened. The source catalogue has 12 careers; expansion to 30–40 officially grounded careers remains future work.

Noto Sans fonts for English, Hindi and Tamil are bundled for offline use. The prototype's Baloo display fonts are still unavailable; headings use Noto at the specified sizes and weights until those assets are supplied. Hindi and Tamil text needs native-speaker review. Room, WorkManager and a real sync protocol remain integration work once a server contract exists. The local repository is intentionally independent of the UI to support that migration.

## UI reference alignment

The visual reference is `reference/citigrow-prototype.dc.html`, not the original purple landing-screen stub. The Compose design tokens now follow its CSS: navy #102A43, blue #1677C8, teal #19A38C, background #F7F9FC, muted text #52606D and border #E4E9F0. Cards use 16dp padding and 20dp corners; primary buttons are 52dp, chips 44dp, page gutters 16dp, and page gaps 14dp. The original SVG icon paths are imported into `assets/prototype-icons.json` by the data import script and drawn as native vectors.

The welcome illustration follows the reference SVG coordinates. Onboarding uses four language tiles in a two-column grid (including Malayalam), colored role cards, profile avatars and a PIN keypad. The student shell uses a white header, connection pill, profile avatar and compact white bottom navigation. Home restores the blue next-step panel and shortcut tiles; Discover restores its navy conversation banner, horizontal stories and career badges. Learn uses a gold Skills Compass card and progress-based course cards. Life Map uses the reference's winding path coordinates, alternate branch, selectable steps and weekly bars. Parent usage is a seven-day chart with a goal line; secondary screens share navy hero panels and the same component styling.

This alignment has been checked against the prototype markup and CSS. It has **not** been verified by comparing device screenshots: ADB and Gradle cannot start their socket services in the managed workspace. Exact visual parity is still pending a rendered device comparison and the missing Baloo font assets.

## Source layout

`data/GrowStore.kt` stores local records and reads the catalogue. `data/GrowRules.kt` implements matching, eligibility, discovery and assessment logic. `ui/` contains Compose screens; `voice/` contains the swappable audio pipeline. All paths are under `app/src/main/java/com/example/citigrow/`.

The reference is retained verbatim. Regenerate bundled reference data with:

```sh
node scripts/import-prototype.cjs
```

## Verification in the managed workspace

Gradle startup is blocked here because the sandbox denies socket creation, including network-interface discovery used by Gradle's file-lock service. A cached-dependency checker compiles all Kotlin/Compose source and device-test source without Gradle, then runs seventeen deterministic rule, language-routing, prompt-completeness, tokenizer-parity and WAV tests using the SDK's real JSON implementation:

```sh
node scripts/check-kotlin.cjs --test
```

Set `CITIGROW_DEPENDENCY_CACHE` and `ANDROID_HOME` to override its local cache and SDK paths. This fallback requires a populated dependency cache and SDK sources; it does not resolve the new Gradle dependencies, package an APK, load Android ONNX JNI or run Compose/device tests. The ONNX adapter uses reflection and is therefore not API-checked by this fallback compiler. Python CPU inference with the supplied model passes locally for Tamil and Malayalam; Android runtime integration and rendered UI remain unverified. Device tests also cover shared-phone PIN entry and local persistence, and should run on a fresh demo installation in Android Studio. An emulator/device run, accessibility pass and release build remain necessary before distribution.

Portal roots checked while implementing: [NSP](https://scholarships.gov.in/), [DIKSHA](https://diksha.gov.in/), [SWAYAM](https://swayam.gov.in/), [AISHE](https://aishe.gov.in/). The [apprenticeship](https://www.apprenticeshipindia.gov.in/) and [Skill India Digital](https://www.skillindiadigital.gov.in/) sites did not return readable content to the verification tool; their links remain portal references, not verified listing evidence.

