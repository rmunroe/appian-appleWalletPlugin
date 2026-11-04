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

## Before you begin

### 1. Prerequisites from your Apple Developer account

To sign Apple Wallet passes you need three things from your [Apple Developer account](https://developer.apple.com/documentation/walletpasses):

1. A **Pass Type ID** and your **Team ID**
2. The **Pass Type ID certificate and private key**, exported as a password-protected `.p12` file
3. The **Apple WWDR intermediate certificate** that issued your certificate

You need a paid Apple Developer Program membership, and your account role must be **Account Holder** or **Admin** to create identifiers and certificates.

#### Step 1: Find your Team ID

1. Sign in at [developer.apple.com/account](https://developer.apple.com/account).
2. Scroll to **Membership details**.
3. Copy the **Team ID**, a 10-character alphanumeric string such as `A1B2C3D4E5`. This will be used as the `teamIdentifier` input in the Smart Service.

#### Step 2: Create a Pass Type ID

1. In your developer account, go to **Certificates, Identifiers & Profiles** → **Identifiers**.
2. Click the **+** button.
3. Select **Pass Type IDs** and click **Continue**.
4. Enter:
    - **Description**: a human-readable name, for example `Acme Loyalty Card`.
    - **Identifier**: a reverse-DNS string starting with `pass.`, for example `pass.com.acme.loyalty`.
5. Click **Continue**, then **Register**.
6. Record the identifier. This is your **Pass Type ID**, and it must match the `passTypeIdentifier` field in your `pass.json` exactly.

#### Step 3: Create a Certificate Signing Request (CSR)

Choose **Option A** on a Mac or **Option B** on any platform with OpenSSL.

##### Option A: macOS (Keychain Access)

1. Open **Keychain Access**. On recent macOS versions, the quickest way to find it is Spotlight (⌘ + Space, then type "Keychain Access").
2. From the menu bar, choose **Keychain Access** → **Certificate Assistant** → **Request a Certificate From a Certificate Authority…**
3. Fill in:
    - **User Email Address**: your email.
    - **Common Name**: a descriptive name, for example `Acme Pass Signing Key`.
    - **CA Email Address**: leave blank.
4. Select **Saved to disk** and click **Continue**.
5. Save the `.certSigningRequest` file.

Keychain Access generates the private key and stores it in your **login** keychain. Do the rest of the macOS steps on this same Mac, because the key exists only here.

##### Option B: OpenSSL (Windows, Linux, or macOS)

```bash
# Generate a private key
openssl genrsa -out pass.key 2048

# Create the CSR (replace the subject values with your own)
openssl req -new -key pass.key -out pass.csr \
  -subj "/emailAddress=you@example.com/CN=Acme Pass Signing Key/C=US"
```

Keep `pass.key` safe. You'll need it in Step 5.

#### Step 4: Create and download the Pass Type ID certificate

1. Go to **Certificates, Identifiers & Profiles** → **Identifiers**.
2. Use the filter dropdown at the top right to show **Pass Type IDs**, then select the one you created in Step 2.
3. Under **Production Certificates**, click **Create Certificate**.
4. Upload the CSR file from Step 3 and click **Continue**.
5. Click **Download** to save the certificate, usually named `pass.cer`.

Note the certificate's expiration date. When it expires, passes can no longer be signed or updated, so you'll need to repeat this process before then.

#### Step 5: Export the certificate and private key as a `.p12` file

##### Option A: macOS (Keychain Access)

1. Double-click `pass.cer` to install it into your **login** keychain.
2. In Keychain Access, select the **login** keychain and the **My Certificates** tab.
3. Find the certificate named **Pass Type ID: pass.com.acme.loyalty** (your identifier).
4. Click the disclosure arrow next to it and confirm a private key appears underneath. If there's no key, the certificate was installed on a different Mac from the one that created the CSR.
5. Right-click the **certificate** (not the key) and choose **Export "Pass Type ID: …"**.
6. Set the file format to **Personal Information Exchange (.p12)** and save it, for example as `pass.p12`.
7. When prompted, enter a strong **export password**. Your pass-signing code will need this password.
8. Enter your Mac login password if macOS asks for it to allow the export.

##### Option B: OpenSSL

```bash
# Convert the downloaded certificate from DER to PEM
openssl x509 -inform der -in pass.cer -out pass.pem

# Bundle the certificate and private key into a password-protected .p12
openssl pkcs12 -export -inkey pass.key -in pass.pem -out pass.p12
```

You'll be prompted to set an export password.

If you use OpenSSL 3.x and your signing library fails to read the `.p12`, add the `-legacy` flag to the `pkcs12` command. Older libraries don't support the newer default encryption.

#### Step 6: Download the Apple WWDR intermediate certificate

Apple issues signing certificates from several generations of its Worldwide Developer Relations (WWDR) intermediate certificate, so first check which one issued yours.

1. Open your Pass Type ID certificate:
    - **macOS**: double-click it in Keychain Access and expand **Details**.
    - **OpenSSL**: run `openssl x509 -in pass.pem -noout -issuer`.
2. Look at the **Issuer**. Its **Organizational Unit (OU)** field shows the generation, for example `OU=G4`.
3. Go to the [Apple PKI page](https://www.apple.com/certificateauthority/) and, under **Apple Intermediate Certificates**, download the matching **Worldwide Developer Relations** certificate (for example, `AppleWWDRCAG4.cer` for G4).

#### Checklist

When you're done, you should have:

| Item | Example |
|---|---|
| Team ID | `A1B2C3D4E5` |
| Pass Type ID | `pass.com.acme.loyalty` |
| Certificate and private key | `pass.p12` |
| `.p12` export password | (store it in a secrets manager) |
| WWDR intermediate certificate | `AppleWWDRCAG4.cer` |

**Security note:** The `.p12` file and its password can sign passes as your organization. Don't commit them to source control. Store them in a secrets manager or another secure location.

### 2. Upload the files to Appian

1. Create a document folder for the signing files. Turn off its security inheritance, and give access only to the people who manage the certificate and to the user the smart service runs as.
2. Upload `pass.p12` and the WWDR `.cer` to that folder.
3. Open the security of each uploaded document and confirm it inherits from the folder. A document with its own role map doesn't follow the folder's security.
4. Upload your pass images as PNG documents to a separate folder. An icon is required, and Apple's documentation lists the image sizes.
5. Create a separate folder for generated passes. Users need access to a pass to download it, so don't use the signing folder.
6. On the smart service node's **Assignment** tab, choose **Run as whoever designed the process model**. With **Run as whoever started this process**, every user who starts the process would need access to your private key.
7. Give the user the node runs as these permissions, unless that user is a system administrator:

| Folder | Permission |
| --- | --- |
| Signing files | Viewer |
| Pass images | Viewer |
| Generated passes | Editor |

System administrators can read every document, so test access with a user who isn't one. Users who download passes need Viewer on the generated passes folder, or on the individual pass.

### 3. Store the password

The plug-in reads the `.p12` password from [Third-Party Credentials](https://docs.appian.com/suite/help/26.6/admin-third-party-credentials.html), so it's never stored in your process model.

1. In the Admin Console, go to **Integration > Third-Party Credentials** and click **CREATE**.
2. In the **Identity** section, enter a **Name**. Appian generates the entry's **Key** from the name. You can't edit the key, and renaming the entry doesn't change it.
3. In the **Plug-ins List** section, type `Apple Wallet Plugin` in the **Plug-ins** field and select it.
4. In the **Credentials** section, click **Add field**, then:
   1. Enter `Password` as the **Field name**. Its **Key** must be `password`, because that's the key the plug-in reads. Renaming a field doesn't change its key, so if the key is wrong, delete the field and add it again.
   2. Enter the `.p12` password in the **Value** column.
   3. Leave the option for individuals to set their own credentials off. The plug-in only reads the site-wide value.
5. Click **SAVE**, then reopen the entry and copy its **Key**. This is the value for the smart service's `passwordScsKey` input.

### If a run fails during setup

| `errorTxt` contains | What to check |
| --- | --- |
| `is not registered to access secured values` | Either `passwordScsKey` doesn't match the entry's **Key**, or the plug-in isn't on the entry's **Plug-ins** list. The message is the same for both. |
| `has no field named "password"` | The field's **Key** isn't `password`. |
| `Icon image is required` | `passImages` has no `icon`. |

### When the certificate expires

1. Repeat part 1 to make a new `.p12`.
2. Upload it as a new version of the existing `.p12` document, so your process models don't need to change.
3. If the password changed, update the **Value** in the Third-Party Credentials entry.
4. If the certificate's issuer changed, do the same for the WWDR document.

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

The 2.0.0 work is on the `modernize-2.0.0` branch. It has been tested on a live Appian 26.9 site, and a pass it signed installs in Apple Wallet on an iPhone. It hasn't yet been run on 26.6, or with a process model built on 1.0.6. See [AGENTS.md](AGENTS.md) for the details and remaining checks.
