package com.hugo99j.chaosparty.ui.debugger;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import imgui.ImGui;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

public class MenuBar {
    private static final List<Consumer<Builder>> builders = new ArrayList<>();

    public static void render() {
        Builder b = new Builder();
        b.addCategory("File");
        b.addCategory("Edit");
        builders.forEach(builder -> builder.accept(b));
        ImGui.beginMainMenuBar();
        b.getItems().forEach((category, e) -> {
            if(!e.isEmpty() && ImGui.beginMenu(category)) {
                for (Item item : e) {
                    if(!item.enabled) ImGui.beginDisabled();
                    int remaining = 10-item.label.length();
                    if(ImGui.menuItem(item.label()+(" ".repeat(remaining))+"CTRL+"+ Input.Keys.toString(item.key)) && item.enabled) {
                        item.action.run();
                    }
                    if(!item.enabled) ImGui.endDisabled();
                }
                ImGui.endMenu();
            }
        });
        ImGui.endMainMenuBar();
        if(Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT)) b.getItems().forEach((category, e) -> {
            for (Item item : e) {
                if(Gdx.input.isKeyJustPressed(item.key) && item.enabled) {
                    item.action.run();
                    break;
                }
            }
        });
    }

    protected static void registerBuilder(Consumer<Builder> builder) {
        builders.add(builder);
    }

    protected static class Builder {
        private final LinkedHashMap<String, List<Item>> items = new LinkedHashMap<>();

        private Builder() {}

        public void addItem(String category, int key, String label, Runnable action) {
            addItem(category, key, label, action, true);
        }

        public void addItem(String category, int key, String label, Runnable action, boolean enabled) {
            if(!items.containsKey(category)) items.put(category, new ArrayList<>());
            items.get(category).add(new Item(key, label, action, enabled));
        }

        private Map<String, List<Item>> getItems() {
            return items;
        }

        public void addCategory(String name) {
            items.put(name, new ArrayList<>());
        }
    }

    private record Item(int key, String label, Runnable action, boolean enabled) {}
}
