# Changelog

## Unreleased

### Build compatibility

- Pinned BuildKonfig to 0.21.2 and constrained the Kotlin Gradle plugin to prevent a transitive upgrade to Kotlin 2.4.
- Standardized Kotlin plugins, the standard library, and test libraries on Kotlin 2.3.21, with language/API level 2.3 and JVM target 17.
- Replaced the serialization Gradle plugin dependency in `commonMain` with the multiplatform JSON runtime.
- Aligned coroutine dependencies on 1.11.0 and removed the legacy `native-mt` dependency.
- Migrated the Android sample from `kotlinOptions` to `compilerOptions`.
- Aligned Lottie's deployment target in the synthetic CocoaPods project with the SDK's iOS 16 minimum for current Xcode builds.

## 2.3.0

### New features

**Custom base URLs in `KodaBotsConfig`**

You can now override the chatbot web URL and REST API URL per driver instance. Useful for staging environments and white-label deployments.

Android:
```kotlin
val config = KodaBotsConfig().apply {
    customBaseUrl = "https://staging.example.com/mobile/v1/"
    customBaseRestUrl = "https://staging-api.example.com/sdk/v1"
}
```

iOS:
```swift
let config = KodaBotsConfig(
    customBaseUrl: "https://staging.example.com/mobile/v1/",
    customBaseRestUrl: "https://staging-api.example.com/sdk/v1"
)
```

Both fields are optional — when `nil`/`null`, the SDK uses its built-in default URLs.

---

### Breaking changes

**`customClientId` removed from `KodaBotsConfig`**

Use `customClientToken` instead (available since 2.2.0).

Before:
```kotlin
config.customClientId = "your_token"
```
After:
```kotlin
config.customClientToken = "your_token"
```

---

### Bug fixes

- Fixed an issue where the WebView would open the configured base URL in an external browser instead of navigating within the WebView.
