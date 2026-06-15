# Session API

Session is temporary per-user storage.

Acts like a normal Python dictionary.

Example:

```python
ctx.session["step"] = "waiting_name"

step = ctx.session.get("step")
```

Supported operations:

```python
ctx.session[key] = value

ctx.session.get(key)

ctx.session.clear()

ctx.session.keys()

ctx.session.values()

ctx.session.items()
```
