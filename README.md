# S&S BLOCK PUZZLE — The Next Generation Block Puzzle Game

**Created by SAYOD & SUBHON**  
**Studio: S&S**  
**Slogan:** *"Think Smart. Play Better. Enjoy Every Block."*

---

## 🎮 Overview

**S&S Block Puzzle** is a mobile puzzle game for Android smartphones and tablets. It combines classic block-fitting gameplay with 10 distinct game modes, 8 visual themes, real-time procedural audio synthesis, particle explosion effects, and localization in **English**, **Russian (Русский)**, and authentic **Tajik Cyrillic (Тоҷикӣ)**.

---

## 🌟 Key Features

### 1. 10 Fully Playable Game Modes
1. **Classic Mode:** The timeless 10×10 strategic puzzle. Unlimited play until no available move can be placed.
2. **Time Attack:** Fast-paced 90-second countdown where clearing lines awards valuable bonus seconds.
3. **Zen Mode:** Relaxing, peaceful atmosphere with soothing ambient melodies and an unlimited Undo feature.
4. **Bomb Challenge:** Defuse ticking countdown bombs by clearing the rows or columns they occupy before they reach zero.
5. **Adventure Mode:** 20 handcrafted progressive stages with star ratings (1–3 stars), obstacles, target scores, and coin rewards.
6. **Daily Challenge:** A unique date-seeded obstacle puzzle every calendar day with streak tracking and coin bonuses.
7. **Hexa Mode:** Innovative hexagonal honeycomb board layout (axial coordinates) with 3-axis line clearing and special hex shapes.
8. **Puzzle Challenge:** Limited move budget (25 moves) with an 800-point victory target requiring calculated foresight.
9. **Combo Rush:** Fast-paced consecutive clearing system with stacking combo multipliers (x2, x3, x4, x5...).
10. **Endless Challenge:** Progressive difficulty scaling for veteran puzzle masters.

### 2. 8 Visual Themes
- **Modern Dark:** Sleek deep navy and obsidian with glowing neon jewel accents.
- **Pure Light:** Crisp icy white with high-contrast, polished jewel blocks.
- **Ocean Blue:** Abyssal cyan and aquatic gradients with glowing coral blocks.
- **Neon Glow:** Cyberpunk dark purple with electric magenta, lime, and laser cyan.
- **Nature:** Forest emerald and warm botanical earth tones.
- **Midnight:** Deep cosmic violet and starlight purple with constellation gold.
- **Pastel:** Dreamy lavender and soft candy hues.
- **Premium Gold:** Luxurious black and metallic imperial gold.

### 3. Procedural Audio Engine (`SoundManager.kt`)
- Zero external audio file dependencies. Real-time procedural PCM audio generation using Android `AudioTrack`:
  - Block pickup chirp
  - Satisfying wooden snap / placement thud
  - Sparkle chimes for line clears
  - Ascending harmonic major arpeggios for combos
  - Fanfares for victory and achievements
  - Loopable ambient background music for Zen and Classic modes
  - Independent volume controls for Master, Music, SFX, and mute toggles.

### 4. Trilingual Localization
Instant switching in Settings between:
- **English**
- **Русский (Russian)**
- **Тоҷикӣ (Tajik)** with correct Tajik Cyrillic characters (Ғ, Ӣ, Қ, Ӯ, Ҳ, Ҷ).

### 5. Modern Shiny 3D Blocks & Tactile Controls
- Realistic glossy 3D crystal/bevel block rendering (`BlockRenderer.kt`) with 3-stop gradients, specular highlights, rim lighting, soft drop shadows, and glowing bomb countdown badges.
- Smooth touch & mouse dragging with floating 1:1 scale preview and adaptive finger lift offset.
- Placement ghost preview plus predictive line-clear highlights showing rows, columns, or hex axes about to clear.
- Smart Tap-to-Place alternative mode for accessible one-handed or stylus play.
- Placement spring pop animation, line-clear flash sweep, haptic vibration, and Compose Canvas particle bursts.

### 6. Persistence & Offline Operation
- 100% offline-compatible.
- SharedPreferences storage for high scores across all 10 modes, statistics, achievements, theme unlocks, coins, adventure stars, and daily challenge streaks.

---

## 🛠️ Technology & Architecture

- **Platform:** Native Android (Kotlin + Jetpack Compose)
- **Minimum SDK:** API 24 (Android 7.0+)
- **Target SDK:** API 36 (Android 16)
- **UI:** Jetpack Compose with custom hardware-accelerated Canvas rendering (60 FPS)
- **Audio:** Custom procedural PCM synthesis via Android `AudioTrack`
- **Haptics:** Android `Vibrator` / `VibrationEffect`
- **Build Output:** Signed debug APK at `app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Testing & Quality Assurance Report

- **Robolectric Local JVM Tests:** Passed (`ExampleRobolectricTest`)
- **Unit Tests:** Passed (`ExampleUnitTest`)
  - Standard 10×10 line clearing (horizontal and vertical)
  - Hexagonal axial line clearing across 3 axes
  - Bomb countdown ticking and detonation logic
  - Trilingual string dictionary validation
  - 8-theme color palette coverage
- **Build Status:** Verified with `compile_applet` and `gradle assembleDebug` (Exit 0).
