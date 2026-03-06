# StockWatch

An Android app for tracking stock prices at a glance. Search for any ticker, save it to your watchlist, and see live price and daily change data powered by Yahoo Finance.

## Features

- Search stocks by symbol and select from matching results
- Watchlist persisted locally with SQLite — survives app restarts
- Live price, daily change, and percentage change via Yahoo Finance API
- Color-coded rows: green for gains, red for losses
- Swipe left to delete a stock from your list
- Tap any row to open the stock's MarketWatch page in your browser
- Pull-to-refresh to reload all prices
- No-network detection with user-friendly error dialogs

## Tech Stack

- Java, Android SDK (min SDK 21, target SDK 33)
- RecyclerView with custom adapter
- AsyncTask for background network calls
- SQLite via SQLiteOpenHelper
- Yahoo Finance unofficial APIs (search + chart)

## Setup

1. Clone the repo
2. Open in Android Studio
3. Run on a device or emulator (API 21+)

> Requires an internet connection to load or refresh stock data.

## Original README

See [README_original.md](README_original.md) for the original 2018 README with screenshots.

## Credits

- Launcher icon generated with [Android Asset Studio](https://github.com/romannurik/AndroidAssetStudio)
