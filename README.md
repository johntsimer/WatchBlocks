# WatchBlocks

A small, native block-puzzle game for Wear OS, built from scratch to
mimic Block Blast: 8x8 grid, 3-piece tray, drag pieces onto the board,
clear full rows/columns for points. No ads, no network permission,
no third-party SDKs — nothing to hang or crash on startup.

## Important: this project has not been compiled or run yet

I wrote this on a sandboxed machine with no Android SDK and no
network access, so I could not run a Gradle build or test it on a
device myself. The code is written carefully and should build cleanly,
but if Android Studio / Gradle throws an error on first build, paste
me the exact error and I'll fix it — that's a much faster loop than
guessing blind like we were doing with the APK mod.

## What's in here

```
app/src/main/java/com/watchblocks/puzzle/
  MainActivity.kt          entry point, loads/saves high score
  game/Piece.kt             piece shapes (polyominoes)
  game/GameState.kt         grid, placement rules, line clearing, game over
  ui/GameScreen.kt          Compose UI: grid, tray, drag-and-drop, game over screen
```

Everything is one Gradle module (`app`), minSdk 30 (Wear OS 3+, matches
your OnePlus Watch 3), no phone companion app required — it's a
standalone watch app (`com.google.android.wearable.standalone` = true).

## How to build

### Option A — Android Studio (recommended, on your Linux Mint PC)

1. Install Android Studio if you don't already have it.
2. File → Open, select the `BlockDashWear` folder (the one with
   `settings.gradle.kts` at the top).
3. Let Gradle sync — first sync will download the Gradle 8.7
   distribution and dependencies, needs internet.
4. Connect to your watch: on the OnePlus Watch 3, enable
   Developer Options → ADB debugging (and Debug over Wi-Fi if you
   want to skip the cable), then:
   ```
   adb connect <watch-ip>:5555
   ```
5. In Android Studio, select the watch as your run target and hit Run.
   That builds, installs, and launches it in one step.

### Option B — command line (Termux or Linux Mint terminal)

You need the Android SDK command-line tools + a JDK. From the project
root:
```bash
./gradlew assembleDebug
```
(If `gradlew` isn't executable: `chmod +x gradlew` first. If it's
missing the wrapper jar, run `gradle wrapper` once with a system
Gradle install, or just open the project in Android Studio once —
it'll generate the wrapper for you.)

The output APK lands at:
```
app/build/outputs/apk/debug/app-debug.apk
```

Install it directly:
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## How it plays

- Drag any of the 3 pieces from the tray up onto the grid.
- The piece lifts above your finger while dragging so you can see
  where it's landing — green tint means it fits, red means it doesn't.
- Lift your finger to drop it. Full rows and columns clear and add
  to your score.
- When none of your 3 pieces can be placed anywhere, it's game over —
  tap anywhere to restart. Your best score is saved on-device.

## Straightforward things to extend later

- Sound effects / haptics on placement and line-clear (Wear OS haptics
  API is simple to wire in — happy to add if you want it)
- A subtler combo/streak scoring curve
- An ambient-mode-friendly paused state (currently pauses naturally
  since Compose stops recomposing, but no dedicated low-power view)
- App icon (currently uses the system default placeholder)
