package fr.dreamin.dreamapi.api.services;

import org.bukkit.plugin.ServicePriority;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DreamAutoService {
  Class<?> value(); // The interface class of the service
  ServicePriority priority() default ServicePriority.Normal;
  Class<? extends DreamService>[] dependencies() default {}; // Dépendances
}
