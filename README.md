# BotForge

BotForge is a first working Android skeleton for a local Telegram bot runner.

Package: `com.rofls.botforge`

Language: Java

UI: XML layouts, no Jetpack Compose

## How to Open

1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle.
3. Run the `app` configuration on a device or emulator.
4. Add a Telegram bot token from BotFather and press `Проверить токен`.

The repository includes a Gradle wrapper, so a local debug build can be started with `gradlew.bat assembleDebug` on Windows.

## What Works

- Dark Telegram-like UI.
- Add bot by name and token.
- Validate token through real Telegram Bot API `getMe`.
- Save bot metadata locally with `SharedPreferences`.
- Store Telegram bot tokens in `EncryptedSharedPreferences`.
- Exclude encrypted token storage from Android Auto Backup and device transfer.
- Save scripts locally with `SharedPreferences`.
- Show bots list.
- Open bot card.
- Start/stop long polling in a background thread.
- Fetch updates through real `getUpdates`.
- Send replies through real `sendMessage`.
- Store `lastUpdateId` and use it as polling offset.
- Prevent two simultaneous BotForge runners in this first version.
- Write events and errors to local logs.
- Template engines for Echo, Command, Menu, FAQ and Buy/Sell.
- Developer Mode script editor with basic validation.
- Script API documentation in `docs/script-api.md` and inside the app.

## Important Limitation

BotForge is not a VPS and not guaranteed 24/7 hosting. Android can stop background work, restrict networking and save battery. Treat this app as a local mobile runner, test bench and learning platform.

## Stub

`PythonBotEngine` is currently a replaceable stub. It reads the saved script and returns:

`Python engine пока в режиме заглушки. Скрипт сохранён.`

The Java side intentionally owns Telegram tokens, polling, offsets and message sending. Python must not receive the bot token.

## How to Connect Chaquopy Later

1. Add Chaquopy plugin repositories and plugin version to Gradle.
2. Apply the Chaquopy plugin in `app/build.gradle`.
3. Add Python source files under `app/src/main/python`.
4. Implement a bridge where Java passes a safe context object to Python:
   - text
   - chat id
   - user id
   - current state/session handles
5. Return a Java-friendly response object:
   - text
   - optional buttons JSON
6. Replace only `engine/PythonBotEngine.java`; keep `BotRunner` and `TelegramApiClient` unchanged.

## Debug First

1. Token validation in `AddBotActivity`.
2. Secure token storage in `SecureTokenStorage`.
3. Telegram HTTP errors in `TelegramApiClient`.
4. Polling loop and offset updates in `BotRunner`.
5. Local persistence in `BotRepository`, `ScriptRepository`, `LogRepository`.
6. Developer Mode bridge in `PythonBotEngine`.
