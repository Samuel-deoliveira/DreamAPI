package fr.dreamin.dreamapi.api.services;

import java.lang.annotation.*;

/**
 * Marks a constructor to be used by the DreamServiceManager for automatic dependency injection.
 *
 * Supported constructor parameters:
 * - Plugin (the owning plugin instance)
 * - Any DreamService (interface type)
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject { }
