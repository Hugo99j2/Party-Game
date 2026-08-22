package com.hugo99j.chaosparty.ui;

import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.element.PaddingSettings;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ScreenSSFunctions {
    private static final Map<String, Float> cachedValues = new HashMap<>();
    private static final Function<UiElement, Float> GET_X = UiElement::getX;
    private static final Function<UiElement, Float> GET_Y = UiElement::getY;
    private static final Function<UiElement, Float> GET_WIDTH = UiElement::getWidth;
    private static final Function<UiElement, Float> GET_HEIGHT = UiElement::getHeight;

    public static float parentX(UiElement this1, float percent) {
        check("x");
        return get(this1.getParent(), GET_X) + get(this1.getParent(), GET_WIDTH) * (percent/100);
    }

    public static float parentXAdd(UiElement this1, float amount) {
        check("x");
        return get(this1.getParent(), GET_X) + amount;
    }

    public static float parentY(UiElement this1, float percent) {
        check("y");
        return get(this1.getParent(), GET_Y) + get(this1.getParent(), GET_HEIGHT) * (percent/100);
    }

    public static float parentYAdd(UiElement this1, float amount) {
        check("y");
        return get(this1.getParent(), GET_Y) + amount;
    }

    public static float parentWidth(UiElement this1, float percent) {
        check("width");
        return get(this1.getParent(), GET_WIDTH) * (percent/100);
    }

    public static float parentHeight(UiElement this1, float percent) {
        check("height");
        return get(this1.getParent(), GET_HEIGHT) * (percent/100);
    }

    public static float autoRowsYPos(UiElement this1) {
        return autoRowsYPos(this1, false);
    }

    public static float autoRowsYPos(UiElement this1, boolean invert) {
        check("y");
        int myIndex = this1.getParent().getChildren().indexOf(this1);
        float maxIndex = this1.getParent().getChildren().size();
        float singleHeight = get(this1, GET_HEIGHT);
        if(invert) myIndex = (int) (maxIndex - myIndex)-1;

        if(myIndex == maxIndex-1) {
            return 0;
        }

        if(this1.getParent() instanceof PaddingSettings settings && maxIndex > 0 && myIndex > 0) {
            float yOffset = 0;
            if(myIndex != maxIndex-1) {
                yOffset += settings.getPadding()*(myIndex);
            }
            yOffset += singleHeight * (myIndex+1);

            return get(this1.getParent(), GET_HEIGHT) - yOffset;
        }

        return get(this1.getParent(), GET_HEIGHT) - singleHeight * (myIndex+1);
    }

    public static float autoRowsHeight(UiElement this1) {
        check("height");
        if(this1.getParent() instanceof PaddingSettings settings) {
            float totalHeight = get(this1.getParent(), GET_HEIGHT);
            int amount = this1.getParent().getChildren().size();
            totalHeight -= settings.getPadding()*(amount-1);
            return totalHeight / (this1.getParent().getChildren().size());
        }
        return get(this1.getParent(), GET_HEIGHT) / (this1.getParent().getChildren().size());
    }

    public static float autoColumnsXPos(UiElement this1) {
        return autoColumnsXPos(this1, false);
    }

    public static float autoColumnsXPos(UiElement this1, boolean invert) {
        check("x");
        int myIndex = this1.getParent().getChildren().indexOf(this1);
        float maxIndex = this1.getParent().getChildren().size();
        float singleWidth = get(this1, GET_WIDTH);
        //invert so left to right
        if(!invert) myIndex = (int) (maxIndex - myIndex)-1;

        if(myIndex == maxIndex-1) {
            return 0;
        }

        if(this1.getParent() instanceof PaddingSettings settings && maxIndex > 0 && myIndex > 0) {
            float yOffset = 0;
            if(myIndex != maxIndex-1) {
                yOffset += settings.getPadding()*(myIndex);
            }
            yOffset += singleWidth * (myIndex+1);

            return get(this1.getParent(), GET_WIDTH) - yOffset;
        }

        return get(this1.getParent(), GET_WIDTH) - singleWidth * (myIndex+1);
    }

    public static float autoColumnsWidth(UiElement this1) {
        check("width");
        if(this1.getParent() instanceof PaddingSettings settings) {
            float totalWidth = get(this1.getParent(), GET_WIDTH);
            int amount = this1.getParent().getChildren().size();
            totalWidth -= settings.getPadding()*(amount-1);
            return totalWidth / (this1.getParent().getChildren().size());
        }
        return get(this1.getParent(), GET_WIDTH) / (this1.getParent().getChildren().size());
    }

    public static float centerX(UiElement this1, float originalX) {
        check("x");
        return originalX - get(this1, GET_WIDTH)/2f;
    }

    public static float centerY(UiElement this1, float originalX) {
        check("y");
        return originalX - get(this1, GET_HEIGHT)/2f;
    }

    private static boolean noSmartFit = false;
    public static float smartFitWidth(UiElement this1, float currentWidth, float minX, float maxX) {
        check("width");
        if(noSmartFit) return currentWidth;
        noSmartFit = true;
        float currentX = get(this1, GET_X, "noSmartFit");
        noSmartFit = false;
        //does not fit
        if(currentX < minX || currentX+currentWidth > maxX) {
            return maxX-minX;
        }
        return currentWidth;
    }

    public static float smartFitHeight(UiElement this1, float currentHeight, float minY, float maxY) {
        check("height");
        if(noSmartFit) return currentHeight;
        noSmartFit = true;
        float currentY = get(this1, GET_Y, "noSmartFit");
        noSmartFit = false;
        //does not fit
        if(currentY < minY || currentY+currentHeight > maxY) {
            return maxY-minY;
        }
        return currentHeight;
    }

    public static void beginFrame() {
        cachedValues.clear();
    }

    private static <T> float get(T from, Function<T, Float> function) {
        return get(from, function, "");
    }

    private static <T> float get(T from, Function<T, Float> function, String extra) {
        String hash = from.hashCode()+","+function.toString()+","+extra;
        if(!cachedValues.containsKey(hash)) cachedValues.put(hash, function.apply(from));
        return cachedValues.get(hash);
    }

    private static void check(String valid) {
        if(GameData.DEBUGGING) {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            String caller = stackTrace[2].getMethodName();
            if(!caller.toLowerCase().contains(valid.toLowerCase())) {
                Logger.error("Possibly using wrong method ("+caller+" in "+stackTrace[2].getClassName()+" called "+stackTrace[1].getMethodName()+")");
            }
        }
    }
}
