package fr.dreamin.dreamapi.api.game;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public abstract class MiniGameState extends GameState {
  private @Nullable MiniGame parentGame;
}
