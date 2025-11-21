package fr.dreamin.dreamapi.api.game;

import lombok.Getter;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class GameState {

  private final List<Listener> listeners = new ArrayList<>();

  public final void enter(final GameState previousState) {
    this.onEnter(previousState);
  }

  public final void exit(final GameState nextState) {
    this.onExit(nextState);
  }

  public abstract void tick(int currentTick);

  protected abstract void onEnter(final GameState previousState);

  protected abstract void onExit(final GameState nextState);

}
