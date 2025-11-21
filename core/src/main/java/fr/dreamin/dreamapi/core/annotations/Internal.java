package fr.dreamin.dreamapi.core.annotations;

import java.lang.annotation.*;

/**
 * Marks a class or method as internal to DreamAPI Core.
 * <p>
 * This element is not part of the public API and may change or be removed
 * at any time without notice.
 * </p>
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.SOURCE)
public @interface Internal {
}
