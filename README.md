# appian-appleWalletPlugin
Provides Appian with the ability to create Apple Wallet Passes.

Requires Appian 24.4 or later.

## Building

You need JDK 17 and a copy of the Appian plug-in SDK jar, `appian-plug-in-sdk-<version>.jar`, from your own Appian installation. The SDK is proprietary, so it isn't in this repo.

Tell the build where the jar is, either on the command line or by setting `appianSdkJar` in `~/.gradle/gradle.properties`:

```bash
cd src
./gradlew build -PappianSdkJar=/path/to/appian-plug-in-sdk.jar
```

This runs the tests and writes the plugin to `src/build/libs/`.

To check the bundled libraries for known vulnerabilities, run `./gradlew dependencyCheckAnalyze`. Pass `-PnvdApiKey=<key>` with an [NVD API key](https://nvd.nist.gov/developers/request-an-api-key), because without one the vulnerability data can take a very long time to download.
