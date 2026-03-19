# Epson iOS SDK Setup

The `libepos2.xcframework` binary is **not committed to git** due to its size (~71 MB).

## Download

Run the following from the project root:

```bash
brew install git-lfs
mkdir -p /tmp/epson_sdk && cd /tmp/epson_sdk
git lfs clone --depth 1 --branch 2.23.1 https://github.com/popina/EpsonSDK.git .
mkdir -p <project-root>/print-plugins/epson/nativeInterop/libs/ios
cp -r libepos2.xcframework <project-root>/print-plugins/epson/nativeInterop/libs/ios/
```

## SDK version

| SDK Version | Tested |
|-------------|--------|
| 2.23.1 | ✅ |

The SDK is sourced from the CocoaPod mirror at https://github.com/popina/EpsonSDK.
Original SDK: https://download.epson-biz.com/modules/pos/index.php?page=soft&pcat=3&scat=50
