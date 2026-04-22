# Politics Trade Market Tracker

A polished native Android app for following live politics prediction
markets — browse markets, watch candidate and event pricing, follow
markets you care about, and see price history at a glance.

Built as a portfolio project to demonstrate modern Android architecture:
Kotlin + Jetpack Compose, MVVM with ViewModel/LiveData, a Room-backed
offline-first repository, background refresh with WorkManager, and a
clean layered package structure.

## Features

- **Home / Markets list** — live market cards with title, category, YES
  probability, 24h change badge, volume, and close date.
- **Search, filters, and sort** — search by keyword, filter by category
  (Presidential / Congress / International / Economy / Trending), and
  sort by most active, biggest movers, or newest.
- **Market detail** — full description, current price and change, a
  Compose `Canvas` price-history chart, close date, volume, category,
  and resolution rules.
- **Favorites / Watchlist** — persist favorited markets locally; they
  remain saved even if a market falls out of the active API set.
- **Offline-first** — Room is the single source of truth; the last
  successful snapshot is always shown. A banner surfaces stale data
  when the latest refresh failed.
- **Background refresh** — a WorkManager periodic worker refreshes
  market data every 30 minutes on a metered network-aware schedule.
- **Loading / Empty / Error states** — every screen handles the four
  canonical UI states explicitly.

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, unidirectional state |
| State exposure | `LiveData` from `ViewModel` |
| Reactive plumbing | Kotlin Coroutines + Flow (Room → repo) |
| Navigation | `androidx.navigation:navigation-compose` |
| Networking | Retrofit 2 + OkHttp + Moshi |
| Persistence | Room (KSP codegen) |
| Background | WorkManager (`CoroutineWorker`) |
| Tests | JUnit4, Truth, `kotlinx-coroutines-test`, AndroidX `core-testing` |

## Architecture

The app follows a clean, layered MVVM structure:

```
com.shayanhaque.politicstracker
├── PoliticsTrackerApp.kt          // Application — owns AppContainer, schedules work
├── MainActivity.kt                // Single-activity host; sets Compose content
├── di/AppContainer.kt             // Manual DI graph (easy to swap for Hilt later)
├── model/                         // Pure domain types (Market, PricePoint, …)
├── data/
│   ├── remote/                    // Retrofit API + DTOs + data source abstraction
│   │   ├── PolymarketApi.kt
│   │   ├── PolymarketRemoteDataSource.kt
│   │   ├── FakeMarketRemoteDataSource.kt
│   │   ├── NetworkModule.kt
│   │   └── dto/…                  // DTO + mapper to domain
│   ├── local/                     // Room database, DAO, entities
│   │   └── entity/…
│   └── repository/                // Offline-first repository (single source of truth)
├── viewmodel/                     // HomeViewModel, DetailViewModel, WatchlistViewModel, UiState
├── ui/
│   ├── components/                // Reusable Compose components (cards, chips, chart, states)
│   ├── home/   detail/   watchlist/
│   ├── navigation/                // NavHost + Destinations
│   └── theme/                     // Material 3 ColorScheme + Typography
├── util/                          // Formatters, NetworkResult
└── work/MarketRefreshWorker.kt    // WorkManager periodic refresh
```

### Data flow (unidirectional)

```
Polymarket REST  ─▶  RemoteDataSource  ─▶  MarketRepositoryImpl  ─▶  Room DAO (source of truth)
                                                                      │
                                                                      ▼
                                                          Flow<List<Market>>
                                                                      │
                                                                      ▼
                                                ViewModel (combines with query/filter/sort)
                                                                      │
                                                                      ▼
                                                           LiveData<UiState<…>>
                                                                      │
                                                                      ▼
                                                            Compose UI observes
```

- Room is the **single source of truth**. The network just fills the
  cache; every observer re-emits automatically when a refresh lands.
- ViewModels never touch Retrofit or Room directly — they only talk
  to the `MarketRepository` interface, which makes them trivially
  testable with an in-memory fake DAO.
- Every screen has a `UiState<T>` (Loading / Success / Empty / Error)
  that Compose renders exhaustively.

### UI state model

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T, val stale: Boolean = false) : UiState<T>
    data object Empty : UiState<Nothing>
    data class Error(val message: String) : UiState<Nothing>
}
```

`Success` carries a `stale` flag so the UI can keep showing cached data
with a banner when the latest refresh fails, instead of flashing an
error screen over perfectly usable content.

## Setup / run

1. **Open in Android Studio.** Hedgehog or newer is recommended (AGP
   `8.5.x`, Kotlin `1.9.24`).
2. **Sync Gradle.** The project uses the Gradle wrapper; Android Studio
   will fetch the right Gradle version automatically on first sync.
3. **Run `app` on an emulator or device** (minSdk 26, targetSdk 34).

No API key is required — the app uses Polymarket's public Gamma API.

### Demo / offline mode

If you want to demo without network access (or if the API is flaky),
flip `useFakeData = true` in
[`PoliticsTrackerApp.kt`](app/src/main/java/com/shayanhaque/politicstracker/PoliticsTrackerApp.kt).
That swaps `PolymarketRemoteDataSource` for
`FakeMarketRemoteDataSource`, which serves a curated list of realistic
political markets with synthetic price history.

## API notes

- **Source:** Polymarket Gamma API (`https://gamma-api.polymarket.com`).
- **Endpoints used:**
  - `GET /markets?active=true&closed=false&limit=100&order=volume` — list
    of currently-open markets.
  - `GET /markets/{id}` — single market detail.
  - `GET /prices-history?market={id}&interval=1m` — historical trade
    prices for the chart.
- **No auth** is required for these endpoints at the time of writing.
  If that ever changes, add the header in an OkHttp interceptor in
  [`NetworkModule`](app/src/main/java/com/shayanhaque/politicstracker/data/remote/NetworkModule.kt).
- **DTO ≠ domain.** Upstream quirks (outcome prices arriving as a
  JSON-encoded string, timestamps in seconds) are normalized in
  [`MarketMapper`](app/src/main/java/com/shayanhaque/politicstracker/data/remote/dto/MarketMapper.kt)
  so the rest of the codebase never sees them.
- **Category inference.** Polymarket tags are free-form strings; the
  app maps them into a fixed `MarketCategory` enum so filter chips are
  exhaustive and compile-time safe.

## Testing

Unit tests live in `app/src/test`:

- `MarketRepositoryImplTest` — covers refresh success/failure and
  favorite toggling using an in-memory fake DAO + fake remote.
- `HomeViewModelTest` — verifies initial load, category filtering,
  and the biggest-movers sort order.
- `MarketMapperTest` — verifies DTO → domain edge cases
  (stringified `outcomePrices`, malformed payloads).

Run them with:

```bash
./gradlew :app:testDebugUnitTest
```

## Key design choices

- **Offline-first, Room-as-source-of-truth.** Prediction-market users
  will open this app while on flaky connections. Serving the UI from
  Room (and letting refreshes just update the cache) means the list
  never blanks out, and every observer updates automatically.
- **Interface-backed remote data source.** `MarketRemoteDataSource`
  has both a real Retrofit implementation and a `Fake…` one. Tests
  and offline demos plug in the fake in one line.
- **LiveData at the ViewModel boundary, Flow everywhere underneath.**
  The spec called for LiveData, but Room + search/filter state compose
  most naturally as Flows. ViewModels combine Flows internally and
  surface the final state as LiveData via `asLiveData()`.
- **`Success.stale = true` instead of a separate Error screen.** When
  a refresh fails but we already have cached data, surfacing an error
  screen would be worse UX than showing the cache with a banner.
- **Compose `Canvas` chart.** Pulling in MPAndroidChart or Vico for
  one chart is overkill; a 40-line Canvas implementation is easier to
  read and has zero transitive-dependency risk.
- **Manual DI.** Hilt would be justifiable but is a lot of boilerplate
  for four injection points. `AppContainer` keeps the graph explicit
  and in one file, and migrating to Hilt later is mechanical.

## Future improvements

- Pull-to-refresh on Home (`Material3 PullToRefresh` is still in
  experimental; worker covers the gap).
- Deep links on `politicstracker://market/{id}`.
- Paging 3 once the upstream supports cursor pagination.
- More sophisticated category inference via an ML classifier rather
  than keyword matching.
- Instrumentation tests for navigation and the favorite toggle.
- Dark/light theme toggle independent of system setting.

## License

MIT.
