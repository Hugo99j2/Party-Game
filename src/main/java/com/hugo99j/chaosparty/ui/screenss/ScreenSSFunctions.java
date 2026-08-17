package com.hugo99j.chaosparty.ui.screenss;

import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.ui.element.PaddingSettings;
import com.hugo99j.chaosparty.ui.renderable.UiElement;
import com.hugo99j.chaosparty.util.Logger;

public class ScreenSSFunctions {
    public static float parentX(UiElement this1, float percent) {
        check("x");
        return this1.getParent().getX() + this1.getParent().getWidth() * (percent/100);
    }

    public static float parentY(UiElement this1, float percent) {
        check("y");
        return this1.getParent().getY() + this1.getParent().getHeight() * (percent/100);
    }

    public static float parentWidth(UiElement this1, float percent) {
        check("width");
        return this1.getParent().getWidth() * (percent/100);
    }

    public static float parentHeight(UiElement this1, float percent) {
        check("height");
        return this1.getParent().getHeight() * (percent/100);
    }

    public static float autoRowsYPos(UiElement this1) {
        check("y");
        int myIndex = this1.getParent().getChildren().indexOf(this1)+1;
        //Dont ask me why 0.41 works, it just does.
        float maxIndex = this1.getParent().getChildren().size();

        float padding = 0;
        if(this1.getParent() instanceof PaddingSettings settings) {
            padding = settings.getPadding()*this1.getParent().getChildren().indexOf(this1);
        }

        return this1.getParent().getHeight() - (this1.getParent().getHeight() * ((float) myIndex / maxIndex) + padding);
    }

    public static float autoRowsHeight(UiElement this1) {
        check("height");
        float height = this1.getParent().getHeight() / (this1.getParent().getChildren().size() + 1);
        if(this1.getParent() instanceof PaddingSettings settings) {
            height -= settings.getPadding();
        }
        return height;
    }


    public static float autoColumnsXPos(UiElement this1) {
        check("x");
        int myIndex = this1.getParent().getChildren().indexOf(this1);
        //Dont ask me why 0.41 works, it just does.
        float maxIndex = this1.getParent().getChildren().size() + 0.41f;

        float padding = 0;
        if(this1.getParent() instanceof PaddingSettings settings) {
            padding = settings.getPadding()*this1.getParent().getChildren().indexOf(this1);
        }

        return this1.getParent().getWidth() * ((float) myIndex / maxIndex) + padding;
    }

    public static float autoColumnsWidth(UiElement this1) {
        check("width");
        float height = this1.getParent().getWidth() / (this1.getParent().getChildren().size() + 1);
        if(this1.getParent() instanceof PaddingSettings settings) {
            height -= settings.getPadding();
        }
        return height;
    }

    public static float centerX(UiElement this1, float originalX) {
        check("x");
        return originalX - this1.getWidth()/2f;
    }

    public static float centerY(UiElement this1, float originalX) {
        check("y");
        return originalX - this1.getHeight()/2f;
    }

    private static boolean noSmartFit = false;
    public static float smartFitWidth(UiElement this1, float currentWidth, float minX, float maxX) {
        check("width");
        if(noSmartFit) return currentWidth;
        noSmartFit = true;
        float currentY = this1.getX();
        noSmartFit = false;
        //does not fit
        if(currentY < minX || currentY+currentWidth > maxX) {
            //float difference = Math.max(minY - currentY, 0) + Math.max((currentY+currentHeight)-maxY, 0);
            ////should always be, but it's better to check!
            //if(difference > 0) {
                return maxX-minX;
            //}
        }
        return currentWidth;
    }

    public static float smartFitHeight(UiElement this1, float currentHeight, float minY, float maxY) {
        check("height");
        if(noSmartFit) return currentHeight;
        noSmartFit = true;
        float currentY = this1.getY();
        noSmartFit = false;
        //does not fit
        if(currentY < minY || currentY+currentHeight > maxY) {
            //float difference = Math.max(minY - currentY, 0) + Math.max((currentY+currentHeight)-maxY, 0);
            ////should always be, but it's better to check!
            //if(difference > 0) {
            return maxY-minY;
            //}
        }
        return currentHeight;
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
