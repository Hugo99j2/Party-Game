package com.daniel99j.dungeongame.ui.screenss;

import com.daniel99j.djutil.MiscUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenSSBuilder {
    protected String name = "root";
    protected final ScreenSSBuilder parent;
    protected final List<ScreenSSBuilder> children = new ArrayList<>();
    protected final Map<String, String> attributes = new HashMap<>();
    private boolean finished = false;
    protected List<String> parentVars = new ArrayList<>();

    private ScreenSSBuilder(ScreenSSBuilder parent) {
        this.parent = parent;
    }

    public static ScreenSSBuilder create() {
        return new ScreenSSBuilder(null);
    }

    public ScreenSSBuilder newChild(String name) {
        if(finished) throw new IllegalStateException("Already built");
        ScreenSSBuilder child = new ScreenSSBuilder(this);
        child.name = name;
        children.add(child);
        return child;
    }

    public ScreenSSBuilder set(String name, String value) {
        if(finished) throw new IllegalStateException("Already built");
        while (value.contains("parent('")) {
            String varName = MiscUtils.getTextBetween(value, "parent('", "')");
            parentVars.add(varName);
            value = value.replace("parent('"+varName+ "')", "parent("+parentVars.indexOf(varName)+")");
        }
        attributes.put(name, value);
        return this;
    }

    public ScreenSSBuilder set(String name, Object value) {
        return set(name, value.toString());
    }

    public ScreenSSBuilder finishChild() {
        if(finished) throw new IllegalStateException("Already built");
        if(this.parent == null) throw new IllegalArgumentException("No parent");
        return this.parent;
    }

    public CombinedScreenSS build() {
        if(this.parent != null) throw new IllegalArgumentException("Children cannot be built");
        finished = true;
        return new CombinedScreenSS(this);
    }
}
