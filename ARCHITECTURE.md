# Royal Backgammon — Architecture Document

> Auto-generated during Phase 1 (Foundation Audit). Last updated: August 2026.

---

## 1. Project Overview

| Property | Value |
|----------|-------|
| Package (applicationId) | `com.royalbackgammon.offline` |
| Namespace (code) | `games.mrlaki5.backgammon` |
| Language | Java (no Kotlin yet) |
| Min SDK | 21 |
| Target/Compile SDK | 35 |
| Version | 1.0.3 (versionCode 4) |
| Build system | Gradle 8.13, AGP 8.13.0 |
| Dependencies | `appcompat:1.7.0`, `constraintlayout:2.2.1` (no 3rd-party game/network libs) |
| Network permissions | **None** — fully offline |

---

## 2. Module Structure (current)

```
:app  (single module — Android Application)
```

All code lives under `app/src/main/java/games/mrlaki5/backgammon/`.

---

## 3. Package Layout

```
games.mrlaki5.backgammon
├── Beans/                  # Data classes (value objects)
│   ├── BoardFieldState     # One board point: chipCount + ownerPlayer
│   ├── DiceThrow           # One die: value + usedFlag
│   ├── NextJump            # One legal move: dieValue + src + dst
│   └── ScoreElem           # Score display entry
├── Database/               # Local SQLite via DbHelper + ScoresTableEntry
├── GameControllers/        # Game loop, rules, activity
│   ├── GameActivity        # Main game screen (landscape)
│   ├── GameLogic           # *** CORE RULES ENGINE ***
│   ├── GameMoveExecutor    # Applies moves to model (no Android deps)
│   └── GameTask            # AsyncTask-based game loop / FSM
├── GameModel/              # State representation + persistence
│   ├── Model               # Board state, dice, players, turn, FSM state
│   └── ModelLoader         # Save/load to text file
├── GameView/               # Custom drawing (OnBoardImage, RoyalOnBoardImage)
├── Menus/                  # Non-game activities
│   ├── SplashActivity
│   ├── MenuActivity
│   ├── SettingsActivity
│   ├── ResultsActivity
│   ├── ScoresActivity
│   └── ScoresAdapter
├── Players/                # Player abstraction + implementations
│   ├── Player (abstract)   # actionMove(), actionRoll(), syncs with GameTask
│   ├── Human               # Waits for touch/shake input via GameActivity
│   ├── Bot                 # AI player using BotMoveStrategy
│   └── BotMoveStrategy     # Minimax/expectimax with difficulty profiles
├── GameAudio               # Music + SFX via MediaPlayer/SoundPool
├── GamePreferences         # SharedPrefs (difficulty, theme, language, volume)
└── LocaleHelper            # FA/EN runtime locale switch
```

---

## 4. Activity Flow

```
SplashActivity (launcher, noHistory)
       │
       ▼
MenuActivity ──────────────────────────────────────────┐
  │ "New Game"          │ "Continue"    │ "Settings"   │ "Scores"
  ▼                     ▼               ▼              ▼
GameActivity         GameActivity    SettingsActivity  ScoresActivity
  │  (extras: p1Name, p2Name, p1Kind, p2Kind)
  │  Result → ResultsActivity (winner display)
  └────────────────────────────────────────────────────┘
```

All activities are landscape-locked.

---

## 5. Game Engine (Rules & Logic) — Coupling Analysis

### 5.1 Classes with ZERO Android dependency (pure Java)

| Class | Responsibility |
|-------|---------------|
| `GameLogic` | Legal move calculation, position mapping, dice rolling, game-phase detection, win detection |
| `GameMoveExecutor` | Validates + applies a move to Model, handles hits/bar, consumes dice |
| `BotMoveStrategy` | AI: minimax over move sequences, expectimax over future dice rolls, board evaluation heuristic |
| `BoardFieldState` | Data: chip count + owner |
| `DiceThrow` | Data: die value + used flag |
| `NextJump` | Data: move descriptor (die value, src, dst) |

### 5.2 Classes with PARTIAL Android dependency

| Class | Android coupling reason |
|-------|------------------------|
| `Model` | Constructor accepts `Bundle` + `GameActivity` to read Intent extras and create Player objects |
| `ModelLoader` | Uses `File` (fine for pure Java) but also `GameActivity.getFilesDir()` for path |

### 5.3 Classes TIGHTLY coupled to Android

| Class | Reason |
|-------|--------|
| `Player` (abstract) | Holds `GameActivity` reference |
| `Human` | Calls `activateTouchListener()`, `activateShakeListener()` on GameActivity |
| `Bot` | Calls `getBoardImage().postInvalidate()`, `onCheckerMoved()`, audio, preferences via Context |
| `GameTask` | Extends `AsyncTask`; calls `GameActivity.finish()`, writes messages to view |

### 5.4 Summary

> **The core rules engine (`GameLogic`, `GameMoveExecutor`, `BotMoveStrategy`, Beans) has NO Android imports and can be extracted to a pure Kotlin/Java module with minimal effort.** The main refactoring needed is removing `Model`'s constructor dependency on `Bundle`/`GameActivity` (split into a factory or builder) and copying `GamePreferences.BOT_*` constants to a shared location.

---

## 6. Board State Representation

`Model.BoardFields[28]`:
- Indices 0–23: the 24 triangles (points)
- Index 24: White's bar
- Index 25: Red's bar
- Index 26: Red's bear-off
- Index 27: White's bear-off

Position mapping uses `GameLogic.calculateRealPosition(matrixPos, playerNum)`:
- White: matrix 0→real 12, matrix 11→real 1, matrix 12→real 13, matrix 23→real 24
- Red: inverted (real = 25 − White's real)
- Special: bar → real 0, bear-off → real 100

Initial setup (standard Backgammon):
```
White: 5@point12, 2@point1(matrix11), 3@point17(matrix16), 5@point19(matrix18)
Red:   3@point17(matrix4), 5@point19(matrix6), 5@point12(matrix12), 2@point1(matrix23)
```

---

## 7. Dice System

- `DiceThrow[4]` stored in Model.
- Normal roll: slots 0,1 get values 1–6; slots 2,3 zeroed+used.
- Doubles: all 4 slots get the same value (4 moves available).
- Initial phase (states 0/1): each player rolls one die to determine who goes first; higher number starts.
- `GameLogic.rollDices()` uses `Math.random()` — NOT injectable, needs refactor for testability and future online sync.

---

## 8. Game Loop / FSM (GameTask)

State machine running on a background thread (`AsyncTask.doInBackground`):

| State | Description | Transition |
|-------|-------------|------------|
| 0 | Player 1 rolls one die | → State 1 |
| 1 | Player 2 rolls one die; higher starts | → State 2 |
| 2 | Current player moves (or wins) | → State 3 (or finish) |
| 3 | Current player rolls dice | → State 2 |

---

## 9. Bot AI (BotMoveStrategy)

- **Algorithm**: Minimax with expectimax over all 21 distinct dice outcomes.
- **Depth control**: `lookaheadRolls` (0 for Easy, 1 for others) + `nodeBudget` (80–3600).
- **Difficulty profiles** (Easy/Medium/Hard/Royal) tune: noise, evaluation weights, node budget.
- **Evaluation heuristic**: borne-off weight, bar penalty, progress, blot exposure (distance-based), made-point bonus, prime detection, home-board weight.
- **Tactical bonuses**: extra score for bearing off, hitting blots, escaping isolated checkers.

---

## 10. Persistence

`ModelLoader` saves/loads game state to a plain text file (6 lines):
1. Player 1 name
2. Player 2 name
3. Player kinds (1=Human, 2=Bot)
4. Board state (28 × "chips,player")
5. Dice state (4 × "value,used")
6. CurrentPlayer + State

---

## 11. Refactoring Plan for `:game-core` Module

To extract a pure Kotlin module, the following changes are needed:

1. **Move** `GameLogic`, `GameMoveExecutor`, `BotMoveStrategy` + all Beans to `:game-core`
2. **Create** a new `GameState` data class (Kotlin) replacing `Model` for the engine layer — no Android deps
3. **Inject** randomness via a `DiceRoller` interface (allows deterministic testing + future server-authority)
4. **Add** JSON serialization of `GameState` (kotlinx.serialization or Gson)
5. **Keep** `Model`, `ModelLoader`, `Player` hierarchy in `:app` — they adapt the pure engine to Android
6. **Port** difficulty constants from `GamePreferences` to a shared `BotDifficulty` enum in `:game-core`

---

## 12. Key Design Decisions (for future phases)

| Decision | Current state | Future need |
|----------|---------------|-------------|
| Backend (online multiplayer) | None | Firebase or custom Node.js/Socket.IO — **ask user** (Iran access concerns) |
| Ad SDK | None | Tapsell / Bazaar Ads — **ask user** |
| Analytics | None | Self-hosted (Matomo) or Iran-accessible SDK — **ask user** |
| IAP | None | Cafe Bazaar IAB + Myket IAP (separate build flavors) |
| Auth | None | Anonymous + optional phone binding |

---

## 13. Online Multiplayer Infrastructure (Phase 3)

### Architecture Overview

The online system is built with **complete backend abstraction** — all network operations go through interfaces so the actual server implementation can be swapped without touching game code.

```
┌─────────────────────────────────────────────────────────────┐
│                    OnlineGameCoordinator                      │
│  (orchestrates auth → matchmaking → sync → anti-cheat)       │
├──────────┬─────────────────┬────────────────┬───────────────┤
│AuthProvider│MatchmakingService│ GameSyncService │NetworkMonitor │
│(interface)│   (interface)    │  (interface)   │  (concrete)   │
├──────────┼─────────────────┼────────────────┼───────────────┤
│StubAuth  │    (stub/impl)   │  (stub/impl)   │ConnectivityMgr│
│Firebase  │                   │   WebSocket    │               │
│Custom JWT│                   │   Firebase RT  │               │
└──────────┴─────────────────┴────────────────┴───────────────┘
```

### Package Structure

```
games.mrlaki5.backgammon.Online/
├── NetworkMonitor.java           # Connectivity state observation
├── OnlineGameCoordinator.java    # Main orchestrator (lifecycle manager)
├── auth/
│   ├── AuthProvider.java         # Interface: signInAnonymously, linkPhone
│   ├── AuthCallback.java         # Callback for auth results
│   ├── OnlineUser.java           # User model (uid, name, isAnonymous)
│   └── StubAuthProvider.java     # Dev/test implementation
├── matchmaking/
│   ├── MatchmakingService.java   # Interface: findMatch, cancel
│   ├── MatchRequest.java         # Request: RANDOM, CREATE_PRIVATE, JOIN_PRIVATE
│   ├── MatchResult.java          # Result: MATCHED, WAITING, ROOM_CREATED, ERROR
│   └── MatchmakingCallback.java  # Async callback
├── sync/
│   ├── GameSyncService.java      # Interface: connect, sendEvent, disconnect
│   ├── GameEvent.java            # Event model: MOVE, DICE_ROLL, NO_MOVES, RESIGN, etc.
│   ├── GameSyncCallback.java     # Receives opponent events
│   └── ReconnectManager.java     # 60s reconnect timeout handling
└── anticheat/
    └── MoveValidator.java        # Client-side move validation via game-core
```

### Design Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Backend binding | **None (interfaces only)** | User will add server details later |
| Auth default | Anonymous (guest) | Zero friction for Iranian market; phone optional |
| Event format | Small events (not full state) | Minimizes data usage for limited mobile plans |
| Anti-cheat | Client + Server validation | Server MUST validate (game-core runs on both sides) |
| Reconnect | 60s window, state persisted on server | Handles flaky connections common in Iran |
| Matchmaking | Random queue + invite code | Invite-by-code is critical for viral growth |
| Dice authority | Server-authoritative | Prevents dice manipulation cheats |

### Event-Driven Sync Protocol

Move events are lightweight JSON:
```json
{"matchId":"abc","turn":12,"type":"MOVE","player":1,"from":6,"to":3,"dieValue":3,"timestamp":1692000000000}
```

The receiving client:
1. Validates the event against local game-core rules (anti-cheat)
2. Applies the event to local state
3. Updates the UI

### Reconnection Flow

```
Player disconnects
       │
       ▼
Server starts 60s countdown
       │
       ├─→ Player reconnects within 60s → requestFullState() → resume
       │
       └─→ Timeout expires → GAME_OVER event (disconnected player loses)
```

### Integration Points with GameActivity

`OnlineGameCoordinator.OnlineGameListener` provides callbacks that `GameActivity` implements to:
- Show matchmaking UI (searching, waiting, match found)
- Apply opponent moves to the board
- Show connection banners
- Handle resign/forfeit

