# Epson iOS SDK Setup

The `libepos2.xcframework` binary is **not committed to git** due to its size (~71 MB).

## Automatic download

The SDK is downloaded automatically on first iOS build. No manual steps needed:

```bash
./gradlew :print-plugins:epson:cinteropEpos2IosArm64
```

Or simply build the project — any task that depends on cinterop will trigger the download.

To re-download (e.g. after version bump), delete the cached copy:

```bash
rm -rf print-plugins/epson/nativeInterop/libs/ios/libepos2.xcframework
```

## Manual download

If you prefer to download manually:

```bash
git clone --depth 1 --branch 2.23.1 https://github.com/popina/EpsonSDK.git /tmp/epson_sdk
mkdir -p print-plugins/epson/nativeInterop/libs/ios
cp -r /tmp/epson_sdk/libepos2.xcframework print-plugins/epson/nativeInterop/libs/ios/
rm -rf /tmp/epson_sdk
```

## SDK version

| SDK Version | Tested |
|-------------|--------|
| 2.23.1      | yes    |

The SDK is sourced from the CocoaPod mirror at https://github.com/popina/EpsonSDK.
Original SDK: https://download.epson-biz.com/modules/pos/index.php?page=soft&pcat=3&scat=50