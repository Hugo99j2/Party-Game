package com.daniel99j.dungeongame.ui.screenss;

import com.daniel99j.djutil.maths.MathsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedScreenSS {
    private final Map<String, ScreenSS> getter = new HashMap<>();

    protected CombinedScreenSS(ScreenSSBuilder builder) {
        add(builder, null);
    }

    private void add(ScreenSSBuilder builder, ScreenSS parent) {
        ScreenParentSS current = new ScreenParentSS(builder, parent);
        getter.put(builder.name, current);
        builder.children.forEach(builder1 -> add(builder1, current));
    }

    public ScreenSS get(String name) {
        if(!getter.containsKey(name)) throw new IllegalArgumentException("Unknown child '"+name+"'");
        return getter.get(name);
    }

    private static class ScreenParentSS extends ScreenSS {
        private final ScreenSS parent;
        protected List<String> parentVars;

        protected ScreenParentSS(ScreenSSBuilder builder, ScreenSS parent) {
            super(builder.attributes);
            this.parent = parent;
            this.parentVars = builder.parentVars;
        }

        @Override
        public int getX() {
            return super.getX()+(parent == null ? 0 : parent.getX());
        }

        @Override
        public int getY() {
            return super.getY()+(parent == null ? 0 : parent.getY());
        }

        @Override
        protected MathsContext createContext(String name) {
            MathsContext context = super.createContext(name).withFunction("parent", (variable) -> parent.get(parentVars.get(variable.intValue())));
            if(parent == null) return context;
            if(name.equals("x") || name.equals("xSize")) context.withGlobalVariable("%", String.valueOf(parent.get("xSize")/100));
            if(name.equals("Y") || name.equals("ySize")) context.withGlobalVariable("%", String.valueOf(parent.get("ySize")/100));
            return context;
        }
    }
}
