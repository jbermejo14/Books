# Releasing Libri

Everything below is a one-time setup except step 4.

## 1. Create the upload keystore

**This is the single most important artefact in the project.** If you lose it, you can
never publish an update to the same Play listing again — you would have to create a new
listing and start from zero users and reviews. If it leaks, someone else can sign
builds that Play will accept as yours.

Run this yourself and choose your own passwords — they are never stored in this repo,
and nobody but you should ever see them:

```bash
keytool -genkeypair -v -keystore libri-upload.jks -alias libri -keyalg RSA -keysize 4096 -validity 10000
```

`keytool` ships with the JDK. On this machine it lives at
`C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe`.

Then:

- Store `libri-upload.jks` **outside** the repository (a password manager's file vault,
  or an encrypted backup — in at least two places).
- Record the store password, key alias and key password in your password manager.
- If you enrol in Play App Signing (recommended, and the default for new apps), Google
  holds the *app signing key* and this file is only your *upload key* — which Google can
  reset if you lose it. Enrol, and this becomes recoverable.

## 2. Point the build at it

Create `keystore.properties` at the repository root. It is gitignored — check that it
never appears in `git status` before committing.

```properties
storeFile=C:/path/outside/the/repo/libri-upload.jks
storePassword=…
keyAlias=libri
keyPassword=…
```

CI can supply the same four values as the environment variables `LIBRI_STORE_FILE`,
`LIBRI_STORE_PASSWORD`, `LIBRI_KEY_ALIAS` and `LIBRI_KEY_PASSWORD` instead.

Without either, the release build still assembles — just unsigned — so a fresh clone
compiles without holding the key.

## 3. Verify signing is active

```bash
./gradlew :app:bundleRelease
```

The output lands at `app/build/outputs/bundle/release/app-release.aab`. If the file is
named `app-release-unsigned.*`, the keystore was not picked up.

Confirm the signature:

```bash
jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab
```

## 4. Every release after that

1. Bump `versionCode` (must strictly increase — Play rejects a reused value) and
   `versionName` in `app/build.gradle.kts`.
2. If the Room schema changed, **add a `Migration`** in `BookDatabase` and a migration
   test. There is deliberately no `fallbackToDestructiveMigration()`: without a
   migration the app will crash on upgrade rather than silently wipe a user's library.
   The exported schemas under `app/schemas/` are what migrations are written against, so
   commit them.
3. `./gradlew clean test bundleRelease`
4. Upload the `.aab` to the Play Console.

## Play Console checklist

- **Data safety form** — the app collects no personal data and has no account. It does
  send the user's search text to Open Library over HTTPS to fetch results, and that
  text is not stored or shared by this app. Declare "no data collected", and note the
  search request under "data shared" only if you consider the query itself user data;
  Google's guidance treats a transient search that is not retained as not collected.
- **Privacy policy URL** — required because the app makes network requests. Host
  [`PRIVACY.md`](PRIVACY.md) (GitHub Pages works) and paste the URL.
- **Content rating questionnaire** — no user-generated content is shared between users;
  notes are local to the device.
- **Target audience** — not directed at children.
- **Ads** — none.
- **App access** — no login required; leave "all functionality available without
  restrictions".
