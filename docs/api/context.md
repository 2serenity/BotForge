# Context API

Every bot handler receives a `ctx` object.

## Fields

### ctx.text
Message text.

### ctx.user_id
Telegram user identifier.

### ctx.chat_id
Telegram chat identifier.

### ctx.message_id
Telegram message identifier.

### ctx.username
Telegram username.

### ctx.first_name
Telegram first name.

## Methods

### ctx.reply(text)

Send a text message.

Example:

```python
ctx.reply("Hello")
```

## State API

### ctx.set_state(state)

Set user state.

### ctx.get_state()

Get current state.

### ctx.clear_state()

Clear current state.
