# Libri

A private digital library and reading tracker for Android, built on the **Libri**
design system — a "Digital Sanctuary" for bibliophiles. Books are discovered through
the [Open Library](https://openlibrary.org) API, then tracked, rated and reviewed
entirely on device.

## Features

- **Discover** — search Open Library by title, author or subject; add any result to a
  shelf with a rating and a review, cover art included.
- **Dashboard** — greeting, active reading count, an inline "Update Progress" card with
  a live preview bar, subject shortcuts and a recently-added grid.
- **Pendientes** — filter chips (All Unread / Currently Reading / Next Up) and sorting
  over an "In Progress" section with progress bars and a "To Read" backlog.
- **Wishlist & History** — the same shelf grid over a different reading status; History
  shows the star rating you gave each book.
- **Four shelves** — Wishlist, To Read, Reading, Finished. Reaching the last page
  promotes a book to Finished automatically.

Nothing you write leaves the device. See [PRIVACY.md](PRIVACY.md).

## Tech stack

| Concern | Choice |
| --- | --- |
| Language | Kotlin 2.0 |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM with `StateFlow` |
| Persistence | Room (KSP) |
| Navigation | Navigation Compose |
| Networking | Retrofit + kotlinx.serialization + OkHttp |
| Images | Coil |
| DI | Manual container (`AppContainer`) |

`minSdk` 26, `compileSdk`/`targetSdk` 35, JVM target 17.

## Architecture

```
data/          Room entity, DAO, database, repository + reading-status invariants
data/remote/   Open Library API, DTOs, HTTP client factory and search repository
di/            AppContainer — wires Room and Retrofit
ui/theme/      Libri palette, typography and shapes
ui/components/ Shared design-system pieces (covers, grids, sheet, inputs, nav bars)
ui/<feature>/  One screen + one ViewModel per feature
```

Every write goes through `Book.normalized()` in
[`BookRepository.kt`](app/src/main/java/com/bolskapps/libri/data/BookRepository.kt) —
the single place the library's invariants live: `currentPage` is clamped to
`0..totalPages`, and reaching the last page promotes a book to Finished.

The HTTP stack is built in
[`OpenLibraryClient`](app/src/main/java/com/bolskapps/libri/data/remote/OpenLibraryClient.kt)
rather than inline in the DI container, so the unit tests exercise the exact
serializer configuration that ships.

## Building

```bash
./gradlew assembleDebug
```

`local.properties` is not committed — Android Studio writes it on first open, or create
it with `sdk.dir=/path/to/Android/Sdk`.

## Tests

```bash
./gradlew test
```

Covers the reading-status invariants and the Open Library layer end to end against a
`MockWebServer`: real payload parsing, unknown upstream fields, malformed documents,
duplicate keys, and the distinction between rate limiting, server errors and a dead
network.

## Releasing

Signing, versioning, migrations and the Play Console checklist are documented in
[RELEASING.md](RELEASING.md). Release signing material is never committed:
`keystore.properties`, `*.jks` and `*.keystore` are gitignored.

There is deliberately no `fallbackToDestructiveMigration()`. A schema change without a
matching `Migration` will crash on upgrade rather than silently delete a user's library.

## Design

The design system lives in [`Diseños/`](Diseños): `libri/DESIGN.md` holds the tokens
(colours, typography, spacing, elevation), alongside reference renders of the three
screens. The palette is transcribed verbatim into
[`ui/theme/Color.kt`](app/src/main/java/com/bolskapps/libri/ui/theme/Color.kt).

Libri is a light-only identity — the spec ships a single "Paper White" palette, so no
dark scheme is declared and dynamic colour is deliberately off.

## Credits

- Book metadata and cover art: [Open Library](https://openlibrary.org), by the Internet Archive.
- Typefaces: [Libre Caslon Text](https://fonts.google.com/specimen/Libre+Caslon+Text) and
  [Manrope](https://fonts.google.com/specimen/Manrope), both under the SIL Open Font
  License. The licence texts ship with the app in
  [`app/src/main/assets/licenses/`](app/src/main/assets/licenses).
