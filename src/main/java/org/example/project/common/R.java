package org.example.project.common;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

public class R extends HashMap<String, Object> {
    @Serial
    private static final long serialVersionUID = 1L;

    public R() {
        put("code", 200);
        put("message", "success");
    }

    public static R error() {
        return error(500, "服务器内部错误");
    }

    public static R error(String message) {
        return error(500, message);
    }

    public static R error(int code, String message) {
        R r = new R();
        r.put("code", code);
        r.put("message", message);
        return r;
    }

    public static R ok(String message) {
        R r = new R();
        r.put("message", message);
        return r;
    }

    public static R ok(Map<String, Object> map) {
        R r = new R();
        r.putAll(map);
        return r;
    }

    public static R ok() {
        return new R();
    }

    @Override
    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
