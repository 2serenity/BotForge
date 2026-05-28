import json
import sys
import traceback
import types
from collections.abc import MutableMapping


class BotRegistry:
    def __init__(self):
        self.message_handlers = []
        self.command_handlers = {}
        self.button_handlers = {}
        self.state_handlers = {}

    def message(self):
        def decorator(func):
            self.message_handlers.append(func)
            return func
        return decorator

    def command(self, command_text):
        def decorator(func):
            self.command_handlers[command_text] = func
            return func
        return decorator

    def button(self, label):
        def decorator(func):
            self.button_handlers[label] = func
            return func
        return decorator

    def state(self, state_name):
        def decorator(func):
            self.state_handlers[state_name] = func
            return func
        return decorator


class SessionProxy(MutableMapping):
    def __init__(self, bridge):
        self._bridge = bridge
        raw = str(bridge.getSessionJson())
        try:
            data = json.loads(raw) if raw else {}
        except Exception:
            data = {}
        self._data = data if isinstance(data, dict) else {}

    def __getitem__(self, key):
        return self._data[key]

    def __setitem__(self, key, value):
        self._data[key] = value
        self._sync()

    def __delitem__(self, key):
        del self._data[key]
        self._sync()

    def __iter__(self):
        return iter(self._data)

    def __len__(self):
        return len(self._data)

    def clear(self):
        self._data.clear()
        self._sync()

    def _sync(self):
        self._bridge.setSessionJson(json.dumps(self._data, ensure_ascii=False))


class StorageProxy:
    def __init__(self, bridge):
        self._bridge = bridge

    def all(self, collection):
        raw = str(self._bridge.storageAll(collection))
        try:
            data = json.loads(raw) if raw else []
        except Exception:
            data = []
        return data if isinstance(data, list) else []

    def add(self, collection, item):
        if not isinstance(item, dict):
            raise TypeError("ctx.storage.add expects a dict item")
        self._bridge.storageAdd(collection, json.dumps(item, ensure_ascii=False))

    def clear(self, collection):
        self._bridge.storageClear(collection)


class BotContext:
    def __init__(self, data, bridge):
        self._bridge = bridge
        self._response = None

        self.bot_id = data.get("bot_id", "")
        self.bot_name = data.get("bot_name", "")
        self.text = data.get("text", "")
        self.chat_id = int(data.get("chat_id", 0) or 0)
        self.user_id = int(data.get("user_id", 0) or 0)
        self.username = data.get("username", "")
        self.first_name = data.get("first_name", "")
        self.message_id = int(data.get("message_id", 0) or 0)

        self.session = SessionProxy(bridge)
        self.storage = StorageProxy(bridge)

    def reply(self, text, buttons=None):
        self._response = {
            "text": "" if text is None else str(text),
            "buttons": _normalize_buttons(buttons),
        }

    def set_state(self, state_name):
        self._bridge.setState("" if state_name is None else str(state_name))

    def clear_state(self):
        self._bridge.clearState()

    def get_state(self):
        return str(self._bridge.getState())


def handle_message(script, data_json, bridge):
    try:
        data = json.loads(script_safe(data_json))
        registry = BotRegistry()
        _execute_user_script(script, registry)

        ctx = BotContext(data, bridge)
        handler = _select_handler(registry, ctx)
        if handler is None:
            return json.dumps({"text": ""}, ensure_ascii=False)

        returned = handler(ctx)
        response = _response_from_returned(ctx, returned)
        return json.dumps(response, ensure_ascii=False)
    except Exception:
        return json.dumps({
            "error": traceback.format_exc(),
        }, ensure_ascii=False)


def _execute_user_script(script, registry):
    module = types.ModuleType("botforge")
    module.bot = registry

    old_module = sys.modules.get("botforge")
    sys.modules["botforge"] = module
    try:
        globals_dict = {
            "__name__": "__botforge_user_script__",
            "__builtins__": __builtins__,
        }
        exec(script, globals_dict, globals_dict)
    finally:
        if old_module is None:
            sys.modules.pop("botforge", None)
        else:
            sys.modules["botforge"] = old_module


def _select_handler(registry, ctx):
    state = ctx.get_state()
    if state and state in registry.state_handlers:
        return registry.state_handlers[state]

    text = ctx.text.strip()
    if text in registry.command_handlers:
        return registry.command_handlers[text]

    if text in registry.button_handlers:
        return registry.button_handlers[text]

    if registry.message_handlers:
        return registry.message_handlers[0]

    return None


def _response_from_returned(ctx, returned):
    if isinstance(returned, dict):
        return {
            "text": "" if returned.get("text") is None else str(returned.get("text")),
            "buttons": _normalize_buttons(returned.get("buttons")),
        }
    if returned is not None:
        return {
            "text": str(returned),
            "buttons": None,
        }
    if ctx._response is not None:
        return ctx._response
    return {"text": "", "buttons": None}


def _normalize_buttons(buttons):
    if buttons is None:
        return None
    if not isinstance(buttons, list):
        raise TypeError("buttons must be a list of rows")

    normalized = []
    for row in buttons:
        if isinstance(row, list):
            normalized.append(["" if value is None else str(value) for value in row])
        else:
            normalized.append(["" if row is None else str(row)])
    return normalized


def script_safe(value):
    return "{}" if value is None else str(value)
