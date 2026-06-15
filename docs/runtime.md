# Runtime

BotForge uses:

- Android
- Chaquopy
- Python 3.11

## Environment

Bots run inside Android sandbox.

## Notes

System binaries are not guaranteed to exist.

Example:

```python
subprocess.run(["openssl"])
```

may fail because OpenSSL is not available on the device.

External Python libraries are only available if bundled with the application.
