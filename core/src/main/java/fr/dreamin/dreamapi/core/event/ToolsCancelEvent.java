package fr.dreamin.dreamapi.core.event;

import lombok.Getter;
import org.bukkit.event.Cancellable;

/**
 * Represents an abstract event in the Tools system that can be canceled.
 * This event extends {@link ToolsEvent} and implements the {@link Cancellable} interface,
 * allowing the event's execution to be interrupted or canceled based on its state.
 * <p>
 * This class can be used as a base for specific Tools events that support cancellation.
 */
@Getter
public abstract class ToolsCancelEvent extends ToolsEvent implements Cancellable {

  private boolean cancelled = false;

  public ToolsCancelEvent() {
    super();
  }

  public ToolsCancelEvent(boolean isAsync) {
    super(isAsync);
  }

  @Override
  public boolean isCancelled() {
    return this.cancelled;
  }

  @Override
  public void setCancelled(boolean cancel) {
    this.cancelled = cancel;
  }

}
