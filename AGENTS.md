# AGENTS.md

Guidance for anyone working in this repo, human or coding agent. See [README.md](README.md) for what the plug-in does and how to use it.

## Current status (November 4, 2026)

**2.0.0 has one live check left.** It's on the `modernize-2.0.0` branch, not merged to `main`, and not submitted to AppMarket. `main` is still 1.0.6.

**The goal of 2.0.0:** current dependencies and real bugs fixed, with no new features and no regressions for existing process models.

### Verified

- A clean build passes with all 42 unit tests, on JDK 17 against the 26.6 SDK from Maven Central.
- The jar built against the 26.6 SDK has the same contents as the one built against the 25.3 SDK, apart from the minimum version in the descriptor and the smart service icons.
- `pass.json` content matches the golden file captured from the 1.0.6 code, and the signature verifies.
- The jar bundles the expected 14 libraries, with no netty, pushy, Spring, or Appian classes.
- The plug-in's classes contain no `printStackTrace` or deprecated document API references. `ServiceLocator` is used only in the servlet: see [Decisions already made](#decisions-already-made).
- OSV reports no known vulnerabilities in the bundled versions, as of the 2.0.0 dependency update.

### Verified on a live site

On the `edge.appiancorp.com` dev site, Appian 26.9.50.0. Nothing has been run on 26.6 itself:

- The plug-in deploys and its classes load, including `org.apache.log4j`. Its errors reach `tomcat-stdOut.log`.
- A valid run makes a pass, and a bad input gives `errorOccurred=true`, a useful `errorTxt`, and no document.
- **A pass signed with a real Pass Type ID certificate installs in Apple Wallet on an iPhone.** Passes with all fields set and with optional values left null both show `5`, `10000000000`, and a blank null value in `pass.json`, their manifest hashes match, and their signatures verify up to Apple's root certificate.
- The servlet serves a pass with a 200 to a signed-in user with rights to it. It returns 400 for a missing or empty id, and 404 for an unknown id, a malformed id, and a non-pass document. The `.p12` gets a 404 for an administrator with rights to it, so it doesn't download.
- Replacing a document with a new version uses the new version. This was checked with the icon, and the certificate, key, and images all use the same code path.
- **The servlet's rights check works.** With 2 non-administrator users, one with Viewer rights to the pass got a 200, and one with no rights got a 403. Both got a 403 for the `.p12`, which neither has rights to.
- `awpGetUuid` returns the right UUID, and null for null.

### Not verified yet

- **An existing process model:** one built on 1.0.6 has to open and run without reconfiguration.
- **Appian 26.6:** see [Decisions already made](#decisions-already-made).
- **`./gradlew dependencyCheckAnalyze`:** it has never been run for 2.0.0, because it needs an NVD API key and none is available. The OSV result above is the only vulnerability check.

### Open questions

- **Log4j and JAXB bundling:** Appian's [Plug-in Packages page](https://docs.appian.com/suite/help/26.6/Custom_Plug-in_Packages.html) lists `org.apache.log4j` and `javax.xml.bind` as "provided but deprecated" and says to bundle such libraries. Both are compile-only here, and both work that way on 26.9. A bundled copy might not work: Appian may not see bundled JAXB annotations, and Log4j output may not reach Appian's logs.
- **Global security provider:** jpasskit's `CertUtils` calls `Security.addProvider` to register BouncyCastle. AppMarket policy discourages changing global JVM settings, so watch for this in review and on redeploys.

### Decisions already made

Ask before reversing any of these:

- **Vendor:** stays `Appian Corporation`, because that's what the plug-in is published as. Revisit only if AppMarket rejects it.
- **Minimum version:** Appian 26.6 on Java 17, matching the SDK the plug-in is built against. It's only been run on 26.9. That's accepted because it's built against the 26.6 SDK. It will be tested on 26.6 after AppMarket submission, and any failure gets raised as a bug.
- **Logging:** Log4j, not SLF4J. AppMarket policy says "Logging must be done through Log4j." `slf4j-api` is only bundled because jpasskit uses it.
- **Errors:** `run()` reports failures through `errorOccurred` and `errorTxt`. Don't throw `SmartServiceException` or move checks into `validate()`. Either would change how existing process models behave on failure, because errors in `validate()` stop `run()`.
- **`relevantDates`:** not supported, because that would be a new feature. Apple has deprecated `relevantDate` in its favor, so this is the likely first feature for a later release. jpasskit's empty `relevantDates` array is deliberately left out of `pass.json` by a mix-in in `PassHelper`.
- **Pass ownership:** the servlet relies on Appian document security. There's no separate check that this plug-in created the pass.
- **Services in the servlet:** it uses `ServiceLocator` with a context from `WebServiceContextFactory`. An `@Inject ContentService` field fails on 26.9 with `No qualifying bean of type 'com.appiancorp.suiteapi.content.ContentService'`, which stops the servlet from starting, and every request then gets a 404. `ServiceLocator`'s Javadoc says it isn't supported for plug-ins, but Appian's servlet plug-in docs still use it in their example, so watch for this in AppMarket review.

### Next steps

1. Run the checks above and fix what they find.
2. Merge `modernize-2.0.0` to `main`.
3. Submit 2.0.0 to AppMarket, with the behavior changes from the README in the release notes.

Don't merge to `main` or submit to AppMarket without being asked.

## Commands

```bash
cd src
./gradlew build   # tests and jar
./gradlew test    # tests only
```

Use JDK 17. Gradle gets the Appian SDK from Maven Central, and its version is in `containerProvided` in `build.gradle`. The jar is written to `src/build/libs/`.

## Layout

The Gradle project root is `src/`, not the repo root.

- `src/jar_root/src/com/robmunroe/plugin/appleWallet/`: plug-in source.
  - `smartServices/`: `CreatePassSmartService` holds the logic, and `CreateGenericPassSmartService` is the registered subclass.
  - `servlet/`: `DownloadPassServlet` is thin, and `DownloadPassHandler` holds the logic so it can be tested without the container.
  - `helpers/ContentHelper`: the single place for document access.
  - `dataTypes/`: the CDTs.
- `src/src/test/`: tests.
- `src/resources/appian-plugin.xml`: the descriptor. `build.gradle` reads the version from here.

## What must not change

Existing process models depend on these:

- Plug-in key `com.robmunroe.plugin.appleWallet`, smart service key `CreateGenericPassSmartService_1`, function `awpGetUuid`, servlet URL `/downloadPass`.
- Smart service input and output names.
- CDT classes: namespace, names, fields, `propOrder`, and `javax.xml.bind`. Appian doesn't support `jakarta` here.

## Conventions

- **Dependencies are non-transitive.** `bundledLibraries` in `build.gradle` is exactly what ships, and the tests run against the same set, so a missing library fails the build. pushy and netty stay out on purpose: only jpasskit's push notification class needs them, and the plug-in never loads it.
- **Container libraries are compile-only.** Add them to `containerProvided`. Test-only needs go under `test*` configurations, so they stay out of the jar.
- **Get services by injection, except in servlets.** Use constructor parameters in smart services and method parameters in functions. Servlets have to use `ServiceLocator`: see [Decisions already made](#decisions-already-made). `javax.inject` is deliberately not a dependency, so an `@Inject` field won't compile.
- **Read documents as streams.** Use `ContentHelper.openCurrentVersion`, which resolves the current version first. Direct file system access is forbidden by AppMarket policy.
- **Close what you open,** and log through Log4j. No `System.out` or `printStackTrace`.
- **Never commit SDK jars or real signing material.** The SDK comes from Maven Central, `src/dependencies/lib-compile/` is ignored because old local copies may still be there, and tests generate throwaway certificates at run time.

## Testing

- Write a failing test first for any bug fix.
- `GenericPassSigningTest` compares `pass.json` to `src/src/test/resources/golden/pass.json`, captured from the 1.0.6 code. If a dependency upgrade makes it fail, treat it as a behavior change: this test has already caught 2 regressions. Don't update the golden file without explaining the difference.
- `com/appiancorp/content/DocumentHelper` and `com/appiancorp/plugins/PluginUsageLogger` under `src/src/test/java` are test-only stubs, needed because the SDK jar omits classes that `new Document()` uses. They aren't shipped.
- `com/appiancorp/suiteapi/resources/Data` under `src/src/test/java` is also a test-only stub. The 26.6 SDK's copy of this enum is missing its private constructor, so it can't initialize, and Mockito can't mock `ContentService` without it. Its constants must match the SDK's, so check them when changing the SDK version.

## Known leftovers

- `src/.idea` is tracked and out of date (`compiler.xml` still says Java 1.8). Reimporting the Gradle project fixes it locally.
- `docs/` is an empty Sphinx skeleton.
