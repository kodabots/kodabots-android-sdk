# Changelog

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
