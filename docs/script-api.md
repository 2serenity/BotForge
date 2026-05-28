# BotForge Script API

Status: draft API for Developer Mode.

In the current Android build, Python scripts are saved but not executed yet. `PythonBotEngine` is still a stub. This document describes the intended API contract that the Chaquopy bridge should implement later.

Important rule: Python code must never receive the Telegram bot token. Android owns token storage, polling, offset handling and calls to Telegram Bot API.

## Minimal Bot

```python
from botforge import bot

@bot.message()
def echo(ctx):
    ctx.reply(ctx.text)
```

## Decorators

### `@bot.message()`

Handles any text message that was not matched by a more specific handler.

```python
@bot.message()
def any_message(ctx):
    ctx.reply("I received: " + ctx.text)
```

### `@bot.command("/start")`

Handles a Telegram command. Command matching should be exact and case-sensitive.

```python
@bot.command("/start")
def start(ctx):
    ctx.reply("Hello from BotForge")
```

### `@bot.button("Catalog")`

Handles a button press. In the first bridge version this can be implemented as text matching against `ctx.text`.

```python
@bot.button("Catalog")
def catalog(ctx):
    ctx.reply("Catalog is empty.", buttons=[
        ["Back"]
    ])
```

### `@bot.state("state_name")`

Handles a message when the current chat is in the given state.

```python
@bot.state("add_title")
def add_title(ctx):
    ctx.session["title"] = ctx.text
    ctx.set_state("add_price")
    ctx.reply("Now send the price")
```

## Context

Every handler receives `ctx`.

Planned fields:

- `ctx.text`: incoming message text.
- `ctx.chat_id`: Telegram chat id.
- `ctx.user_id`: Telegram user id.
- `ctx.username`: Telegram username, if present.
- `ctx.first_name`: Telegram first name, if present.
- `ctx.message_id`: Telegram message id.
- `ctx.session`: per-bot, per-chat mutable dictionary.
- `ctx.storage`: simple per-bot storage object.

Planned methods:

- `ctx.reply(text)`
- `ctx.reply(text, buttons=[["A"], ["B"]])`
- `ctx.set_state("state_name")`
- `ctx.clear_state()`
- `ctx.get_state()`

## Buttons

Buttons are represented as a two-dimensional list. Each nested list is one keyboard row.

```python
ctx.reply("Main menu", buttons=[
    ["Catalog"],
    ["Help", "Back"]
])
```

Android should convert this to Telegram `reply_markup.keyboard`.

## Storage

Storage is local to the Android app. It is not a cloud database.

Planned API:

```python
ctx.storage.add("items", {
    "title": "Phone",
    "price": "10000",
    "seller_id": ctx.user_id
})

items = ctx.storage.all("items")
ctx.storage.clear("items")
```

## Session and State

`ctx.session` is intended for temporary per-chat data. `state` controls multi-step dialogs.

```python
@bot.button("Sell")
def sell(ctx):
    ctx.set_state("add_title")
    ctx.reply("Send item title")

@bot.state("add_title")
def add_title(ctx):
    ctx.session["title"] = ctx.text
    ctx.set_state("add_price")
    ctx.reply("Send price")

@bot.state("add_price")
def add_price(ctx):
    ctx.storage.add("items", {
        "title": ctx.session["title"],
        "price": ctx.text,
        "seller_id": ctx.user_id
    })
    ctx.clear_state()
    ctx.reply("Item added")
```

## Handler Priority

Recommended order for the real bridge:

1. Current `@bot.state(...)` handler.
2. Exact `@bot.command(...)`.
3. Exact `@bot.button(...)`.
4. Fallback `@bot.message()`.

## Runtime Limits

BotForge is a local mobile runner, not a VPS.

- Android can stop background work.
- Network can be restricted.
- Battery optimization can interrupt polling.
- Long-running Python handlers should be avoided.
- Scripts should not block the polling thread.

## Current Implementation Gap

Current Android code:

- saves scripts in `ScriptRepository`;
- validates scripts with a basic text check;
- uses `PythonBotEngine` as a stub;
- returns a fixed response instead of executing Python.

Next implementation step: replace only `PythonBotEngine` with a Chaquopy bridge while keeping `BotRunner` and `TelegramApiClient` in Java.
