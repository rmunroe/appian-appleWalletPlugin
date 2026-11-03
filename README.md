# Apple Wallet Plugin for Appian

An Appian plug-in that creates signed Apple Wallet passes (`.pkpass` files) from a process model and lets users download them.

- **Current version in this branch:** 2.0.0, in testing and not yet released. See [Project status](#project-status).
- **Last published version:** 1.0.6.
- **Requires:** Appian 26.6 or later. Sites on older versions should stay on 1.0.6.

## What's in the plug-in

| Component | Name | What it does |
| --- | --- | --- |
| Smart service | Create Generic Pass (`CreateGenericPassSmartService_1`) | Builds and signs a generic pass, then saves it as a document in a folder you choose. Found in the process modeler palette under Custom Services > Apple Wallet. |
| Function | `awpGetUuid(pkpassDocument)` | Returns the UUID of a pass document, for building a download link. |
| Servlet | `/suite/plugins/servlet/downloadPass` | Serves a pass to a logged-in user with the correct content type, so Apple devices offer to add it to Wallet. |
| Data types | `GenericPass`, `PassImages`, `PassBarcode`, `PassLocation` | The inputs to the smart service, in the namespace `urn:robmunroe:plugin:appleWallet:types`. |

Only generic passes are supported. The plug-in doesn't handle pass updates, push notifications, or the other pass styles (boarding passes, coupons, etc.).

## Before you start

You need the following from your [Apple Developer account](https://developer.apple.com/documentation/walletpasses):

1. A **Pass Type ID**, and your **Team ID**.
2. The **Pass Type ID certificate and private key**, exported as a password-protected `.p12` file.
3. The **Apple WWDR intermediate certificate** that issued your certificate.

Then set these up in Appian:

1. Upload the `.p12` and WWDR certificate as documents, in a folder that only the people and processes that need them can read.
2. Upload your pass images as documents. An icon is required, and Apple's documentation lists the image sizes.
3. Go to [Third-Party Credentials](https://docs.appian.com/suite/help/26.6/admin-third-party-credentials.html) in the Admin Console and create an entry with a field named `Password`, holding the `.p12` password. The plug-in reads the `password` attribute of the site-wide credentials.
4. Add this plug-in to that entry's **Plug-ins** list.

## Creating a pass

Add the **Create Generic Pass** smart service to your process model and configure the inputs below.

### Smart service inputs

All inputs are required.

| Input | Type | Description |
| --- | --- | --- |
| `appleWWDRCA` | Document | The Apple WWDR intermediate certificate. |
| `privateKey` | Document | The `.p12` file with your Pass Type ID certificate and private key. |
| `passwordScsKey` | Text | The key of the Third-Party Credentials entry that holds the `.p12` password. |
| `outputFolder` | Folder | Where to save the pass. |
| `outputFilename` | Text | The document name, without the extension. `.pkpass` is added for you. |
| `passImages` | `PassImages` | The pass images. |
| `genericPass` | `GenericPass` | The pass content. |

### Smart service outputs

| Output | Type | Description |
| --- | --- | --- |
| `outputFile` | Document | The new pass. Null if the run failed. |
| `errorOccurred` | Boolean | True if the pass couldn't be created. |
| `errorTxt` | Text | What went wrong. Null if the run succeeded. |

The smart service reports failures through these outputs and doesn't throw an error on the node, so **add an XOR gateway after it that checks `errorOccurred`**. A failed run doesn't leave a document behind. Details are also written to the application log.

The two error messages to expect:

- `The provided arguments did not produce a valid Pass: ...` means the pass content failed validation.
- `Could not create the pass: ...` means something else failed, such as a wrong password, a missing image, or a storage problem.

### `GenericPass`

| Field | Type | Notes |
| --- | --- | --- |
| `passTypeIdentifier` | Text | Your Pass Type ID, such as `pass.com.example.member`. Required by Apple. |
| `teamIdentifier` | Text | Your Team ID. Required by Apple. |
| `organizationName` | Text | Required by Apple. |
| `serialNumber` | Text | Unique for each pass of this type. Required by Apple. |
| `description` | Text | Used for accessibility. Required by Apple. |
| `authenticationToken` | Text | Optional. At least 16 characters if set. |
| `logoText` | Text | Optional. Shown next to the logo. |
| `foregroundColor`, `backgroundColor`, `labelColor` | Text | Optional. A hex color such as `#1A2B3C`. |
| `maxDistance` | Number (Integer) | Optional. Distance in meters for location relevance. |
| `relevantDate`, `expirationDate` | Date and Time | Optional. |
| `voided`, `sharingProhibited` | Boolean | Optional. |
| `headerFieldsJson`, `primaryFieldsJson`, `secondaryFieldsJson`, `auxiliaryFieldsJson`, `backFieldsJson` | Text | Optional. The fields shown on the pass, as JSON. See below. |
| `locations` | List of `PassLocation` | Optional. |
| `barcodes` | List of `PassBarcode` | Optional. |

### Field JSON

Each `*FieldsJson` input takes a JSON list of objects with a `key`, `label`, and `value`. In SAIL:

```
a!toJson({
  a!map(key: "member", label: "Member", value: "Jane Smith"),
  a!map(key: "points", label: "Points", value: 1250)
})
```

Some behavior to know about:

- Numbers keep their form, so `5` displays as `5`, not `5.0`.
- A null value displays as blank.
- If `value` is an object, the plug-in uses its `id` property.

### `PassBarcode`

| Field | Type | Notes |
| --- | --- | --- |
| `format` | Text | `PKBarcodeFormatQR`, `PKBarcodeFormatPDF417`, `PKBarcodeFormatAztec`, or `PKBarcodeFormatCode128`. |
| `messageEncoding` | Text | A character set name, such as `ISO-8859-1` or `UTF-8`. |
| `message` | Text | The barcode content. |

### `PassLocation`

| Field | Type | Notes |
| --- | --- | --- |
| `latitude`, `longitude` | Decimal | Required. |
| `altitude` | Decimal | Optional. |
| `relevantText` | Text | Optional. Shown on the lock screen near this location. |

### `PassImages`

Each field is a document, and only `icon` is required. Each image also has a `Retina` (@2x) and `RetinaHd` (@3x) variant, such as `iconRetina` and `iconRetinaHd`.

`icon`, `logo`, `thumbnail`, `strip`, `background`, `footer`, `personalizationLogo`

## Downloading a pass

Use `awpGetUuid()` to build a link to the servlet, for example in an interface:

```
a!safeLink(
  label: "Add to Apple Wallet",
  uri: "https://your-site.appiancloud.com/suite/plugins/servlet/downloadPass?id=" & awpGetUuid(ri!passDocument)
)
```

The user has to be logged in to Appian in the browser that opens the link.

| Status | Meaning |
| --- | --- |
| 200 | The pass, as `application/vnd.apple.pkpass`. |
| 400 | The `id` parameter is missing or empty. |
| 403 | The user doesn't have rights to this document. |
| 404 | The document doesn't exist, or it isn't a `.pkpass` file. |
| 500 | The document couldn't be read. |

The servlet only serves `.pkpass` files, so it can't be used to download other documents such as your signing key, and access follows the document's Appian security.

## Upgrading from 1.0.6

Existing process models shouldn't need changes, because the plug-in key, smart service key, function, servlet URL, inputs, outputs, and data types are all unchanged.

These behaviors change in 2.0.0:

- **Minimum version:** Appian 26.6, up from 20.2.
- **Numbers on passes:** whole numbers display as `5`, not `5.0`.
- **Null field values:** display as blank, not the word `null`.
- **Unset optional fields:** null `relevantDate`, `expirationDate`, `voided`, or `sharingProhibited` no longer fail the pass.
- **Failed runs:** no longer leave an empty document behind.
- **Error text:** failures unrelated to the pass content now start with `Could not create the pass:`.
- **Replaced documents:** the current version of a certificate, key, or image is always used.
- **Servlet:** rejects non-`.pkpass` documents and returns real error codes.

## Building from source

You need JDK 17. Gradle downloads the [Appian plug-in SDK](https://central.sonatype.com/artifact/com.appian/appian-plug-in-sdk) from Maven Central, along with the other dependencies.

```bash
cd src
./gradlew build
```

This runs the tests and writes the plug-in to `src/build/libs/`.

The SDK version is set in `src/build.gradle`. It only affects compiling and testing, because Appian supplies these classes at run time and the SDK isn't bundled in the plug-in.

To check the bundled libraries for known vulnerabilities, run `./gradlew dependencyCheckAnalyze`. Pass `-PnvdApiKey=<key>` with an [NVD API key](https://nvd.nist.gov/developers/request-an-api-key), because without one the vulnerability data can take a very long time to download.

### Project layout

| Path | Contents |
| --- | --- |
| `src/build.gradle` | The build, including the exact list of bundled libraries. |
| `src/resources/appian-plugin.xml` | The plug-in descriptor, and the single source of the version number. |
| `src/jar_root/src/` | Plug-in source code. |
| `src/src/test/` | Tests, including a golden `pass.json` that pins the output. |
| `docs/` | An empty Sphinx skeleton, not currently used. |
| `AGENTS.md` | Conventions and development status, for contributors and coding agents. |

The tests create their own throwaway certificates at run time, so no real credentials are stored here.

## Project status

The 2.0.0 work is on the `modernize-2.0.0` branch. It passes its build and unit tests, but those tests mock Appian, so it hasn't yet been proven on a live site or on an iPhone. See [AGENTS.md](AGENTS.md) for the details and remaining checks.
