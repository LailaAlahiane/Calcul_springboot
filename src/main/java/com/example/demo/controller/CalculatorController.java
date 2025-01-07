package com.example.demo.controller;

import com.example.demo.plugin.Plugin;
import com.example.demo.service.PluginLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class CalculatorController {

    @Autowired
    private PluginLoader pluginLoader;

    @PostConstruct
    public void init() {
        pluginLoader.loadAllPluginsFromResources();
    }

    @PostMapping("/upload-plugin")
    public ResponseEntity<String> uploadPlugin(
            @RequestBody String sourceCode,
            @RequestParam String className) {
        try {
            Plugin plugin = pluginLoader.loadPlugin(sourceCode, className);
            return ResponseEntity.ok("Plugin " + plugin.getName() + " loaded successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error loading plugin: " + e.getMessage());
        }
    }

    @GetMapping("/plugins")
    public ResponseEntity<Map<String, Plugin>> getAllPlugins() {
        return ResponseEntity.ok(pluginLoader.getAllPlugins());
    }

    @PostMapping("/calculate/{pluginName}")
    public ResponseEntity<Double> calculate(
            @PathVariable String pluginName,
            @RequestParam double value) {
        Plugin plugin = pluginLoader.getPlugin(pluginName);
        if (plugin == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plugin.calculate(value));
    }
}