# Examples

## Hello World

```python
from botforge import bot

@bot.command("/start")
def start(ctx):
    ctx.reply("Hello")
```

## Storage Example

```python
ctx.storage.add(
    "test",
    {
        "message": "hello"
    }
)

data = ctx.storage.all("test")
```

## Session Example

```python
ctx.session["counter"] = 1

counter = ctx.session.get("counter")
```
