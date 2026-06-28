package com.daniel99j.dungeongame.ui.screenss;

import com.daniel99j.djutil.maths.MathsContext;
import com.daniel99j.djutil.maths.MathsInterpreter;
import com.daniel99j.djutil.maths.MathsParsingError;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.Debuggers;

import java.util.HashMap;
import java.util.Map;

public class ScreenSS {
    private final Map<String, String> getters;
    private final Map<String, CacheKey> cache = new HashMap<>();
    private final boolean center;
    private final String elementId;

    protected ScreenSS(Map<String, String> getters, String elementId) {
        this.center = getters.getOrDefault("center", "false").equals("true");
        this.elementId = elementId;
        getters.remove("center");
        this.getters = getters;
    }

    public String getElementId() {
        return elementId;
    }

    public int getAsInt(String name) {
        return (int) get(name);
    }

    public double get(String name) {
        double oldResult = 0;
        if(cache.containsKey(name)) {
            CacheKey key = cache.get(name);
            if(key.time != GameData.time) {
                oldResult = key.result;
                cache.remove(name);
            } else {
                return key.result;
            }
        }
        if(getters.containsKey(name)) {
            if(GameData.DEBUGGING && Debuggers.isEnabled("screenSSDebugger")) {
                //fine as it doesnt run immediately
                if(!Debuggers.activeScreenSS.contains(this)) Debuggers.activeScreenSS.add(this);
            }

            try {
                double result = MathsInterpreter.eval(getters.get(name), this.createContext(name));
                cache.put(name, new CacheKey(result, GameData.time));
                return result;
            } catch (MathsParsingError e) {
                if(GameData.DEBUGGING && Debuggers.isEnabled("ignoreInvalidSS")) {
                    cache.put(name, new CacheKey(oldResult, GameData.time));
                    return oldResult;
                } else throw e;
            }

        } else throw new IllegalArgumentException("Unknown getter: " + name);
    }

    public Map<String, String> getGetters() {
        return getters;
    }

    protected MathsContext createContext(String name) {
        return MathsContext.create().withGlobalVariable("vw", String.valueOf(GameData.width)).withGlobalVariable("vh", String.valueOf(GameData.height)).withGlobalVariable("time", String.valueOf(GameData.time));
    }

    public int getX() {
        return getAsInt("x") - (center ? getXSize()/2 : 0);
    }
    public int getY() {
        return getAsInt("y") - (center ? getYSize()/2 : 0);
    }
    public int getXSize() {
        return getAsInt("xSize");
    }
    public int getYSize() {
        return getAsInt("ySize");
    }

    protected boolean has(String v) {
        return getters.containsKey(v);
    }

    private record CacheKey(double result, float time) {}
}
