package com.hugo99j.chaosparty.ui.element;

import com.hugo99j.chaosparty.ui.renderable.RenderState;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.ui.debugger.Debuggers;
import com.hugo99j.chaosparty.util.RenderUtil;
import com.hugo99j.chaosparty.GameData;

public abstract class Timer extends UiElement {
    //Does the timer count up or down?
    private boolean up;
    //Time remaining/time at
    private float time;
    //3 -> '1:23:05' 2 -> '23:05' 1 -> '5'
    private final int display;

    public Timer(String elementId, int time, int display, boolean countUp) {
        super(elementId);
        this.time = time;
        this.display = display;
        this.up = countUp;
    }

    public void setTime(int time, boolean countUp) {
        this.time = time;
        this.up = countUp;
    }

    @Override
    public void render(RenderState state) {
        super.render(state);
        if(!GameData.DEBUGGING || !Debuggers.isEnabled("pauseTimers")) {
            if (this.up) time += state.time();
            else time -= state.time();
        }

        int seconds = (int) Math.floor(time);
        String s = "";
        if(display == 3) {
            s += Math.floorDiv(seconds, 60*60)+":";
        }
        if(display >= 2) {
            String n = (Math.floorDiv(seconds, 60) % 60)+":";
            s += n.length() == 3 || display == 2 ? n : "0"+n;
        }
        if(display >= 1) {
            String n = String.valueOf(seconds % 60);
            s += n.length() == 2 || display == 1 ? n : "0"+n;
        }
        //RenderUtil.renderText(s, this.getStyle());
    }

    public int getSeconds() {
        return (int) Math.floor(time);
    }
}
