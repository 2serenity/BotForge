# BotForge Manual Test Checklist

Use this checklist on a real device or emulator with a test Telegram bot token from BotFather.

## Install and Launch

- Build and install debug APK.
- Open BotForge.
- Confirm Splash screen opens.
- Open bots list.
- Confirm empty state is shown when no bots exist.

## Token Validation

- Try saving a bot with an empty token.
- Try `Проверить токен` with an invalid token.
- Try `Проверить токен` with a valid token.
- Confirm `getMe` shows the bot username.
- Confirm an invalid token cannot be saved.

## Bot CRUD

- Save a TEMPLATE bot.
- Confirm it appears in the bots list.
- Open bot detail screen.
- Confirm username, mode, template and `lastUpdateId` are shown.
- Delete a bot and confirm it disappears.

## Template Bots

- Echo Bot: send any text and expect the same text back.
- Command Bot: send `/start`, `/help`, and an unknown command.
- Menu Bot: send `/start`, tap `Каталог`, tap `Назад`.
- FAQ Bot: send messages containing `цена`, `время`, `контакты`, and unknown text.
- Buy/Sell Bot: add an item through `Продать`, then view it through `Купить`.

## Custom Templates

- Create a DEVELOPER bot.
- Write and save a working script.
- Press `Сохранить как шаблон`.
- Fill template name, description and difficulty.
- Save the custom template.
- Open template catalog.
- Confirm the custom template is marked as `мой`.
- Edit the custom template and save changes.
- Use the custom template to create a new bot.
- Confirm the new bot receives a copied script and runs through Python engine.
- Delete the custom template.
- Confirm already-created bots from this template still keep their script copy.

## Developer Mode

- Create a DEVELOPER bot.
- Open script editor.
- Try checking an empty script.
- Save a valid script.
- Leave the editor with unsaved changes and confirm the warning appears.
- Run this script:

```python
from botforge import bot

@bot.command("/start")
def start(ctx):
    ctx.reply("Developer mode works", buttons=[
        ["Echo"]
    ])

@bot.button("Echo")
def echo_button(ctx):
    ctx.reply("Send any text")

@bot.message()
def echo(ctx):
    ctx.reply("You said: " + ctx.text)
```

- Send `/start` in Telegram.
- Tap `Echo`.
- Send any text and confirm the reply.

## State, Session and Storage

Run this script:

```python
from botforge import bot

@bot.command("/start")
def start(ctx):
    ctx.reply("Menu", buttons=[
        ["Add"],
        ["List"]
    ])

@bot.button("Add")
def add(ctx):
    ctx.set_state("add_title")
    ctx.reply("Send title")

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
    ctx.reply("Saved", buttons=[
        ["List"]
    ])

@bot.button("List")
def list_items(ctx):
    items = ctx.storage.all("items")
    if not items:
        ctx.reply("Empty")
        return

    text = "Items:\n"
    for item in items:
        text += item["title"] + " - " + item["price"] + "\n"
    ctx.reply(text)
```

- Add an item.
- List items.
- Restart polling.
- Confirm stored item still exists.

## Polling and Offset

- Start polling.
- Confirm status becomes `RUNNING`.
- Send a Telegram message.
- Confirm `lastUpdateId` changes.
- Stop polling.
- Confirm status becomes `STOPPED`.
- Reset offset while stopped.
- Start polling again and confirm old pending updates may be processed.
- Confirm reset offset is blocked while polling is running.

## Logs

- Open global logs.
- Open bot-specific logs.
- Confirm logs auto-refresh while polling is active.
- Press `Обновить`.
- Clear bot logs.
- Clear global logs.

## Resilience

- Start polling, then disable network temporarily.
- Confirm logs show retry/backoff warnings instead of immediate fatal stop.
- Restore network.
- Confirm polling recovers.
- Send a script response longer than 4096 characters.
- Confirm BotForge sends it as multiple Telegram messages.

## Security

- Confirm bot token is not present in the bot metadata JSON in regular preferences.
- Confirm encrypted token preferences are excluded from backup rules.
- Confirm Python scripts cannot access the Telegram token through `ctx`.
