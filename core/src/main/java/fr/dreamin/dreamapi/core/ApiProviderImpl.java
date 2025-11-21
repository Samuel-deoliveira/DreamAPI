package fr.dreamin.dreamapi.core;

import fr.dreamin.dreamapi.api.DreamAPI;
import fr.dreamin.dreamapi.api.config.Configurations;
import fr.dreamin.dreamapi.core.cuboid.module.CuboidModule;
import fr.dreamin.dreamapi.core.cuboid.module.MemoryCuboidModule;
import fr.dreamin.dreamapi.core.cuboid.module.MovableCuboidModule;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitItemStackModule;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitLocationModule;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitPotionEffectModule;
import fr.dreamin.dreamapi.core.bukkit.module.BukkitVectorModule;
import fr.dreamin.dreamapi.core.item.listener.PlayerItemListener;
import fr.dreamin.dreamapi.core.particle.module.*;
import fr.dreamin.dreamapi.core.time.module.TickTaskModule;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

/**
 * <p>
 * Default implementation of the {@link DreamAPI.IApiProvider} interface.
 * </p>
 *
 * <p>
 * This class acts as the central runtime container of DreamAPI.
 * It provides access to all core managers and initializes the API provider
 * at startup (via {@link DreamAPI#setProvider(DreamAPI.IApiProvider)}).
 * </p>
 *
 * <p>
 * if not already initialized.
 * </p>
 *
 * @author Dreamin
 * @since 1.0.0
 *
 */
public final class ApiProviderImpl implements DreamAPI.IApiProvider {

  private final Plugin plugin;

  /**
   * Constructs and registers a new API provider instance.
   * <p>
   * This constructor is responsible for creating all internal manager
   * implementations and registering this instance into the global
   * {@link DreamAPI} factory.
   * </p>
   */
  public ApiProviderImpl(final @NotNull Plugin plugin) {
    this.plugin = plugin;

    DreamAPI.setProvider(this);

    loadJacksonModules();
    loadListeners();
  }

  // ###############################################################
  // -------------------------- METHODS ----------------------------
  // ###############################################################

  @Override
  public <T> T getService(@NotNull Class<T> serviceClass) {
    final var sm = Bukkit.getServicesManager();
    T service = sm.load(serviceClass);
    if (service == null)
      throw new IllegalStateException("Service " + serviceClass.getName() + " is not loaded");
    return service;
  }

  @Override
  public <T extends Event> T callEvent(@NotNull T event) {
    Bukkit.getPluginManager().callEvent(event);
    return event;
  }

  @Override
  public @NotNull Plugin plugin() {
    return this.plugin;
  }

  @Override
  public @NotNull Logger getLogger() {
    return this.plugin.getLogger();
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private void loadJacksonModules() {
    Configurations.addModule(new BukkitItemStackModule());
    Configurations.addModule(new BukkitLocationModule());
    Configurations.addModule(new BukkitVectorModule());
    Configurations.addModule(new BukkitPotionEffectModule());

    Configurations.addModule(new CuboidModule());
    Configurations.addModule(new MemoryCuboidModule());
    Configurations.addModule(new MovableCuboidModule());

    Configurations.addModule(new ParticleAnimationModule());
    Configurations.addModule(new ParticleChainMorphModule());
    Configurations.addModule(new ParticleOptionsModule());
    Configurations.addModule(new ParticlePathModule());
    Configurations.addModule(new ParticleSequenceItemModule());
    Configurations.addModule(new ParticleSequenceModule());
    Configurations.addModule(new ParticleShapeModule());
    Configurations.addModule(new ParticleShapeMorphModule());

    Configurations.addModule(new TickTaskModule());

  }

  private void loadListeners() {
    Bukkit.getPluginManager().registerEvents(new PlayerItemListener(), this.plugin);
  }

}
