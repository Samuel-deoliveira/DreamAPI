package fr.dreamin.dreamapi.core.service;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class ClassScanner {

  // ###############################################################
  // ----------------------- PUBLIC METHODS ------------------------
  // ###############################################################

  /** Scans for all classes in a package, optionally including subpackages. */
  public static Set<Class<?>> getClasses(final @NotNull Plugin plugin, final @NotNull String packageName, final boolean recursive) throws IOException, ClassNotFoundException {
    Set<Class<?>> classes = new HashSet<>();
    final var path = packageName.replace('.', '/');
    final var classLoader = plugin.getClass().getClassLoader();
    final var resources = classLoader.getResources(path);
    plugin.getLogger().info(String.format("[ClassScanner] Starting scan in path: %s, has resources? %b", path, resources.hasMoreElements()));

    while (resources.hasMoreElements()) {
      final var resource = resources.nextElement();
      final var protocol = resource.getProtocol();

      if ("file".equals(protocol)) {
        final var directory = new File(resource.getFile());
        if (directory.exists())
          findClassesInDirectory(packageName, directory, recursive, classes);

      } else if ("jar".equals(protocol)) {
        final var jarConnection = (JarURLConnection) resource.openConnection();
        final var jarFile = jarConnection.getJarFile();

        for (final var entry : jarFile.stream().toList()) {
          final var name = entry.getName();
          if (name.endsWith(".class")) {
            final var className = name.replace('/', '.').substring(0, name.length() - 6);
            if (className.startsWith(packageName) && (recursive || !className.substring(packageName.length() + 1).contains(".")))
              classes.add(Class.forName(className));
          }
        }
      }
    }
    plugin.getLogger().info(String.format("[ClassScanner] Scan completed: %d classes found", classes.size()));
    return classes;
  }

  // ###############################################################
  // ----------------------- PRIVATE METHODS -----------------------
  // ###############################################################

  private static void findClassesInDirectory(final @NotNull String packageName, final @NotNull File directory, final boolean recursive, final @NotNull Set<Class<?>> classes) throws ClassNotFoundException {
    for (final var file : Objects.requireNonNull(directory.listFiles())) {
      if (file.isDirectory() && recursive)
        findClassesInDirectory(String.format("%s.%s",packageName, file.getName()), file, true, classes);
      else if (file.getName().endsWith(".class")) {
        final var className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
        classes.add(Class.forName(className));
      }
    }
  }
}