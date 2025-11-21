package fr.dreamin.dreamapi.api.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public abstract class MiniGame {

  private final Plugin plugin;
  private MiniGameState currentState;

  private final List<Listener> globalListeners = new ArrayList<>();

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  public void setState(final @Nullable MiniGameState newState) {
    final var previous = this.currentState;
    if (previous != null) previous.exit(newState);

    if (newState != null) {
      newState.setParentGame(this);
      newState.enter(this.currentState);
    }

    this.currentState = newState;
  }

  public void tick(final int currentTick) {
    if (this.currentState != null)
      this.currentState.tick(currentTick);
  }

  public final void start() {
    registerGlobalListeners();
    onStart();
  }

  public final void stop() {
    onStop();
    unregisterGlobalListeners();
    if (currentState != null)
      currentState.exit(null);
  }

  // ###############################################################
  // ---------------------- ABSTRACT METHODS -----------------------
  // ###############################################################

  public abstract void onStart();

  public abstract void onPause();

  public abstract void onResume();

  public abstract void onStop();

  // ###############################################################
  // ---------------------- PROTECTED METHODS ----------------------
  // ###############################################################

  protected void registerGlobalListeners() {
    this.globalListeners.forEach(listener ->
      this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin));
  }

  protected void unregisterGlobalListeners() {
    this.globalListeners.forEach(HandlerList::unregisterAll);
  }

}
