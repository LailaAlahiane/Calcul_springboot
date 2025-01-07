package com.example.demo.service;

import com.example.demo.plugin.Plugin;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class PluginLoader {

    private Map<String, Plugin> loadedPlugins = new HashMap<>();
    private static final String PLUGINS_DIR = "src/main/resources/plugins";

    public PluginLoader() {
        try {
            Files.createDirectories(Paths.get(PLUGINS_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Plugin loadPlugin(String sourceCode, String className) throws Exception {
        // Ajouter le package à la source si nécessaire
        if (!sourceCode.contains("package com.example.demo.plugin;")) {
            sourceCode = "package com.example.demo.plugin;\n" + sourceCode;
        }

        // Écrire le fichier source
        Path sourcePath = Paths.get(PLUGINS_DIR, className + ".java");
        try (Writer writer = new FileWriter(sourcePath.toFile())) {
            writer.write(sourceCode);
        }

        // Configurer le compilateur
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // Configurer les options de compilation
        List<String> options = new ArrayList<>();
        options.add("-d");  // Spécifier le répertoire de sortie
        options.add(PLUGINS_DIR);  // Compiler directement dans resources/plugins

        // Compiler
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourcePath.toFile()));
        JavaCompiler.CompilationTask task = compiler.getTask(
                null,
                fileManager,
                diagnostics,
                options,
                null,
                compilationUnits
        );

        boolean success = task.call();
        fileManager.close();

        if (!success) {
            StringBuilder errorMessage = new StringBuilder("Compilation failed:\n");
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                errorMessage.append(diagnostic.getMessage(null)).append("\n");
            }
            throw new Exception(errorMessage.toString());
        }

        // Créer un nouveau ClassLoader pointant vers resources/plugins
        //Crée un chargeur de classes qui pointe vers le répertoire PLUGINS_DIR
        URLClassLoader classLoader = new URLClassLoader(
                new URL[] { new File(PLUGINS_DIR).toURI().toURL() }
        );

        // Charger la classe compilée
        Class<?> pluginClass = Class.forName("com.example.demo.plugin." + className, true, classLoader);
        //Crée une instance de la classe.
        Plugin plugin = (Plugin) pluginClass.getDeclaredConstructor().newInstance();

        // Stocker le plugin en mémoire
        loadedPlugins.put(plugin.getName(), plugin);

        return plugin;
    }

    public Plugin getPlugin(String name) {
        return loadedPlugins.get(name);
    }

    public Map<String, Plugin> getAllPlugins() {
        return new HashMap<>(loadedPlugins);
    }

    // Méthode pour charger tous les plugins existants
    public void loadAllPluginsFromResources() {
        try {
            Files.walk(Paths.get(PLUGINS_DIR))
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            String sourceCode = new String(Files.readAllBytes(path));
                            String className = path.getFileName().toString().replace(".java", "");
                            loadPlugin(sourceCode, className);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}