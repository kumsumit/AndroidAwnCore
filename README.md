# AndroidAwnCore

Awesome Notifications Android Core is the Android notification core used by the
Awesome Notifications stack.

## Current Status

This project has been modernized for a current Android toolchain:

- Gradle `9.5.1`
- Android Gradle Plugin `9.2.1`
- Kotlin DSL build scripts (`*.gradle.kts`)
- `compileSdk` `37`
- Java `21`
- Minimum SDK `24`

## Core Integrations

### Media3

Media notification support now uses AndroidX Media3:

```kotlin
implementation("androidx.media3:media3-session:1.10.1")
```

The old `MediaSessionCompat`, `MediaMetadataCompat`, `PlaybackStateCompat`, and
`androidx.media.app.NotificationCompat.MediaStyle` integration has been removed.
The core exposes notification media metadata/state to System UI through a small
internal Media3 `SimpleBasePlayer` adapter, without adding ExoPlayer or taking
ownership of real playback.

### ShortcutBadger

Launcher badge support now uses the maintained fork:

```kotlin
implementation("com.github.kumsumit:ShortcutBadger:8bd8c795c7")
```

The dependency is pinned to a commit for reproducible JitPack builds.

Badge handling uses the fork's important APIs:

- `applyCount(...)` for setting launcher badge counts.
- `removeCount(...)` for clearing badge counts.
- `isBadgeCounterSupported(...)` for support checks.
- `applyNotification(...)` for OEM notification badge hooks, including Xiaomi-style behavior.

The fork also contributes its manifest `<queries>` and OEM badge permissions, so
Android 11+ package visibility works for supported launchers.

## Badge Behavior

Badge count is stored locally in shared preferences and mirrored to supported
launchers through ShortcutBadger. When a notification provides an explicit
`badge` value, that value becomes the global badge count. Otherwise, eligible
non-summary notifications increment the stored count when the channel allows
badges.

For modern Android launchers that derive badges from active notifications, the
notification itself is also updated with the badge count where the OEM hook is
available.

## Build

```bash
./gradlew build
```
