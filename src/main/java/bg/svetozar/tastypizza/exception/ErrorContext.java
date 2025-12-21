package bg.svetozar.tastypizza.exception;

import java.util.HashMap;
import java.util.Map;

public final class ErrorContext {

    private ErrorContext() {}

    public static Map<String, Object> of(String key, Object value) {
        if (value == null) {
            return Map.of();
        }
        return Map.of(key, value);
    }

    public static Map<String, Object> of(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> map = new HashMap<>();
        if (v1 != null) map.put(k1, v1);
        if (v2 != null) map.put(k2, v2);
        return map;
    }
}
