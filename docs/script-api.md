# API скриптов BotForge

Статус: первая исполняемая версия API для режима разработчика.

Python-скрипты выполняются локально через Chaquopy. API намеренно небольшой: его можно расширять после стабилизации раннера.

Главное правило: Python-код не получает Telegram-токен. Android отвечает за хранение токена, опрос, offset и вызовы Telegram Bot API.

## Минимальный бот

```python
from botforge import bot

@bot.message()
def echo(ctx):
    ctx.reply(ctx.text)
```

## Декораторы

### `@bot.message()`

Обрабатывает любое текстовое сообщение, если не найден более точный обработчик.

```python
@bot.message()
def any_message(ctx):
    ctx.reply("Я получил: " + ctx.text)
```

### `@bot.command("/start")`

Обрабатывает Telegram-команду. Сравнение команды точное и зависит от регистра.

```python
@bot.command("/start")
def start(ctx):
    ctx.reply("Привет из BotForge")
```

### `@bot.button("Каталог")`

Обрабатывает нажатие кнопки. В первой версии связки Java/Python это работает как сравнение текста кнопки с `ctx.text`.

```python
@bot.button("Каталог")
def catalog(ctx):
    ctx.reply("Каталог пуст.", buttons=[
        ["Назад"]
    ])
```

### `@bot.state("state_name")`

Обрабатывает сообщение, когда текущий чат находится в указанном состоянии.

```python
@bot.state("add_title")
def add_title(ctx):
    ctx.session["title"] = ctx.text
    ctx.set_state("add_price")
    ctx.reply("Теперь напишите цену")
```

## Контекст

Каждый обработчик получает `ctx`.

Доступные поля:

- `ctx.text`: текст входящего сообщения.
- `ctx.chat_id`: Telegram chat id.
- `ctx.user_id`: Telegram user id.
- `ctx.username`: Telegram username, если он есть.
- `ctx.first_name`: имя пользователя Telegram, если оно есть.
- `ctx.message_id`: Telegram message id.
- `ctx.session`: изменяемый словарь для пары бот-чат.
- `ctx.storage`: простое локальное хранилище для бота.

Доступные методы:

- `ctx.reply(text)`
- `ctx.reply(text, buttons=[["Да"], ["Нет"]])`
- `ctx.set_state("state_name")`
- `ctx.clear_state()`
- `ctx.get_state()`

## Кнопки

Кнопки описываются двумерным списком. Каждый вложенный список — отдельная строка клавиатуры.

```python
ctx.reply("Главное меню", buttons=[
    ["Каталог"],
    ["Помощь", "Назад"]
])
```

Android преобразует это в Telegram `reply_markup.keyboard`.

## Хранилище

Хранилище локальное для Android-приложения. Это не облачная база данных.

Доступный API:

```python
ctx.storage.add("items", {
    "title": "Телефон",
    "price": "10000",
    "seller_id": ctx.user_id
})

items = ctx.storage.all("items")
ctx.storage.clear("items")
```

## Сессия и состояние

`ctx.session` нужен для временных данных конкретного чата. `state` управляет многошаговыми диалогами.

```python
@bot.button("Продать")
def sell(ctx):
    ctx.set_state("add_title")
    ctx.reply("Напишите название товара")

@bot.state("add_title")
def add_title(ctx):
    ctx.session["title"] = ctx.text
    ctx.set_state("add_price")
    ctx.reply("Напишите цену")

@bot.state("add_price")
def add_price(ctx):
    ctx.storage.add("items", {
        "title": ctx.session["title"],
        "price": ctx.text,
        "seller_id": ctx.user_id
    })
    ctx.clear_state()
    ctx.reply("Товар добавлен")
```

## Приоритет обработчиков

Рекомендуемый порядок выбора обработчика:

1. Текущий обработчик `@bot.state(...)`.
2. Точное совпадение `@bot.command(...)`.
3. Точное совпадение `@bot.button(...)`.
4. Запасной обработчик `@bot.message()`.

## Ограничения выполнения

BotForge — локальный мобильный раннер, а не VPS.

- Android может остановить фоновую работу.
- Сеть может быть ограничена.
- Экономия батареи может прервать опрос Telegram.
- Долгие Python-обработчики лучше не использовать.
- Скрипты не должны блокировать поток опроса.

## Текущая реализация

Android-код сейчас:

- сохраняет скрипты в `ScriptRepository`;
- проверяет скрипты простой текстовой проверкой;
- выполняет скрипты через `PythonBotEngine` и Chaquopy;
- предоставляет `ctx`, декораторы, кнопки ответа, состояние, сессию и простое хранилище.

Опрос Telegram, offset, хранение токена и `sendMessage` остаются в Java.
