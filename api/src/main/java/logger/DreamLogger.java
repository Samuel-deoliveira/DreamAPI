package logger;

public interface DreamLogger {

  void info(String msg, Object... args);
  void warn(String msg, Object... args);
  void error(String msg, Object... args);
  void debug(String msg, Object... args);

  boolean isDebugEnabled();

}
