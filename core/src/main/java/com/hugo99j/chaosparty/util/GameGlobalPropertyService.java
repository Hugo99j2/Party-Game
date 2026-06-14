package com.hugo99j.chaosparty.util;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;
import java.util.Map;

public class GameGlobalPropertyService implements IGlobalPropertyService {

    private final Map<String, IPropertyKey> keyCache = new HashMap<>();
    private final Map<String, Object> properties = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return keyCache.computeIfAbsent(name, n -> new IPropertyKey() {
            @Override public String toString() { return n; }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key) {
        return (T) properties.get(key.toString());
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        properties.put(key.toString(), value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T) properties.getOrDefault(key.toString(), defaultValue);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object value = properties.get(key.toString());
        return value != null ? value.toString() : defaultValue;
    }
}
