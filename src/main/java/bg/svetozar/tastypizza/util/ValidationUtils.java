package bg.svetozar.tastypizza.util;

import org.springframework.util.CollectionUtils;

import java.util.Collection;

public final class ValidationUtils {

    private ValidationUtils() {
    }

    /** null-safe check */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return CollectionUtils.isEmpty(collection);
    }

    /** Valid database ID: null is allowed, but if present must be > 0 */
    public static boolean isInvalidId(Long id) {
        return id != null && id < 1;
    }

    /** Strict ID validation: null NOT allowed */
    public static boolean isInvalidRequiredId(Long id) {
        return id == null || id < 1;
    }
}
