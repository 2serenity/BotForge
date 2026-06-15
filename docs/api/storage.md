# Storage API

Persistent append-only storage.

## Methods

### add(key, item)

Adds an item to storage.

Parameters:

- key: string
- item: dict

Example:

```python
ctx.storage.add(
    "keys_123",
    {
        "public": "...",
        "private": "..."
    }
)
```

### all(key)

Returns all items for key.

Example:

```python
items = ctx.storage.all("keys_123")
```

Result:

```python
[
    {
        "public": "...",
        "private": "..."
    }
]
```

### clear()

Clears storage.

## Notes

Storage items must be dictionaries.

This call is invalid:

```python
ctx.storage.add("test", "hello")
```

This call is valid:

```python
ctx.storage.add("test", {"message": "hello"})
```
