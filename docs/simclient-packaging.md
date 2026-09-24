# Smaller SimClient APKs

Build the optimized, installable local APKs from the repository root:

```powershell
.\gradlew.bat :app:assembleCompact -PsimClientSplitAbis
```

Output directory: `app/build/outputs/apk/compact/`.

Measured on 2026-09-16 (decimal MB): old debug APK 58.96 MB; compact ARM64 13.48 MB (77.1% smaller); compact ARM32 9.19 MB; compact universal 43.45 MB. Both old and compact APK signatures match. All bundled OCR model asset paths and uncompressed sizes were checked against the original APK.

- `SimClient-arm64-v8a.apk`: current ARM64 Android phones.
- `SimClient-armeabi-v7a.apk`: older 32-bit ARM devices.
- `SimClient-x86.apk` / `SimClient-x86_64.apk`: matching Intel devices/emulators.
- `SimClient-universal.apk`: all four architectures; largest, for unknown device types.

Check a connected device using `adb shell getprop ro.product.cpu.abilist`. Do not install an architecture-specific APK on an incompatible device. The three features and bundled offline English/French OCR are present in every APK.

The `compact` build uses R8 code optimization and resource shrinking. It is signed with the existing local debug certificate so it can update local test installations signed with that certificate without changing the application ID or clearing their data. It is not debuggable. The `release` build is also optimized, but production signing must be configured separately. Normal `assembleDebug` remains available for debugging.

## What is excluded

Only `com.just.print.simclient` and the shared inventory calculation package are compiled. Full POS, printing, PDF, crypto, HTTP-client, and other unrelated dependencies are no longer declared in this branch's app module. Old POS activities/services and unused SMS/storage permissions are removed from the manifest. Their source files remain in the repository.

`prepareSimClientResources` copies the explicit resource allowlist in `app/build.gradle` into `app/build/generated/simclient-res`. Edit the original files under `src/main/res`, not the generated copies. Add newly required resources to that allowlist when adding UI features. This avoids compiling old POS layouts or carrying their custom widget references into the client.

Offline OCR contains a native library per CPU architecture. A universal APK therefore remains substantially larger than a single-architecture APK even after unused Java code and resources have been removed.

Reference: [Android APK size reduction](https://developer.android.com/topic/performance/reduce-apk-size).

## Fixed filename for distribution

Every successful `assembleDebug`, `assembleCompact`, or `assembleRelease` stages a copy at `app/build/outputs/publish/<variant>/SimClient.apk`. Non-split builds also name their original APK `SimClient.apk`. With ABI splits enabled, the original files retain their ABI names to avoid collisions; the staging task selects ARM64 by default. Use `-PsimClientPublishAbi=universal` (or another ABI) to select a different upload artifact. Selection uses the current build's output metadata, never an arbitrary APK left by an older build. A failed package task cannot trigger staging.

Every successful assemble now runs `tools/publish-apk.ps1` after staging and the variant's checks. It reuses Security's Bitvise profile `C:\DropBox\bk\a2.tlp` and Windows-encrypted credential `C:\DropBox\bk\a2-deploy-credential.xml`. No password is stored in this repository.

Destination: `/root/apache-tomcat-8.5/webapps/ROOT/images/SimClient.apk`.
Public URL: `http://216.137.179.176/resources/images/SimClient.apk`.

The script verifies the APK signature, uploads a unique temporary file, checks its SHA256 on the server, retains the previous different APK as `SimClient.apk.previous`, and atomically replaces `SimClient.apk`. It then downloads the public URL and verifies SHA256 again. Success writes `upload-receipt.json` beside the staged APK. Any failure fails the Gradle build; a staged file alone does not mean it has been uploaded. Unsigned release APKs are rejected before upload.

For a local-only build use `-PsimClientSkipUpload=true`. Normal Android Studio/Gradle assemble commands upload automatically on this configured Windows workstation. Prefer `assembleCompact -PsimClientSplitAbis` for the small ARM64 artifact; a normal non-split debug build uploads its universal debug APK. Do not run multiple publishing builds simultaneously, since all variants publish to the same server filename.