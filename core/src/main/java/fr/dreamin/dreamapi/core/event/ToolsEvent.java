package fr.dreamin.dreamapi.core.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an abstract event specifically designed for the Tools system.
 * This class extends the base {@link Event} and serves as a foundation for all events
 * related to the HUD functionality.
 * <p>
 * ToolsEvent provides mechanisms for both synchronous and asynchronous event handling.
 * It includes static and instance-level handler management for event processing.
 */
public abstract class ToolsEvent extends Event {

  private static final HandlerList HANDLERS = new HandlerList();

  /**
   * Default constructor (sync event)
   */
  public ToolsEvent() {
    super();
  }

  /**
   * Constructor with specification of async mode
   *
   * @param isAsync true if event is async, else false
   */
  public ToolsEvent(boolean isAsync) {
    super(isAsync);
  }

  /**
   * Get the list of handlers for this event
   *
   * @return the list of handlers
   */
  @NotNull
  @Override
  public HandlerList getHandlers() {
    return HANDLERS;
  }

  /**
   * Get the static list of handlers
   *
   * @return the static list of handlers
   */
  @NotNull
  public static HandlerList getHandlerList() {
    return HANDLERS;
  }

}
