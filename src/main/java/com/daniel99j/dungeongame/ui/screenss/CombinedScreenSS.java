package com.daniel99j.dungeongame.ui.screenss;

import com.daniel99j.djutil.maths.MathsContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CombinedScreenSS {
    private final Map<String, ScreenSS> getter = new HashMap<>();

    protected CombinedScreenSS(ScreenSSBuilder builder) {
        add(builder, null, 0);
    }

    private void add(ScreenSSBuilder builder, ScreenParentSS parent, int index) {
        ScreenParentSS current = new ScreenParentSS(builder, parent, builder.name, index);
        getter.put(builder.name, current);
        for(int i = 0; i < builder.children.size(); i++) {
            add(builder.children.get(i), current, i);
        }
    }

    public ScreenSS get(String name) {
        if(!getter.containsKey(name)) throw new IllegalArgumentException("Unknown child '"+name+"'");
        return getter.get(name);
    }

    public static class ScreenParentSS extends ScreenSS {
        private final ScreenParentSS parent;
        private final List<ScreenParentSS> children = new java.util.ArrayList<>();
        private final int index;
        protected List<String> parentVars;

        protected ScreenParentSS(ScreenSSBuilder builder, ScreenParentSS parent, String elementId, int index) {
            super(builder.attributes, elementId);
            this.parent = parent;
            this.index = index;
            this.parentVars = builder.parentVars;
            if(parent != null) parent.children.add(this);
        }

        public ScreenSS getParent() {
            return parent;
        }

        @Override
        public double get(String name) {
            if(isAutoLayoutValue(name)) return getLayoutValue(name);
            return super.get(name);
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
            if(name.equals("y") || name.equals("ySize")) context.withGlobalVariable("%", String.valueOf(parent.get("ySize")/100));
            return context;
        }

        private boolean isAutoLayoutValue(String name) {
            return switch(name) {
                case "x", "y", "xSize", "ySize" -> isAuto(name);
                default -> false;
            };
        }

        private boolean isAuto(String name) {
            String value = getAsString(name);
            return value == null || value.equalsIgnoreCase("auto");
        }

        private double getLayoutValue(String name) {
            if(parent == null) return 0;
            if(name.equals("xSize")) return getAutoSize(true);
            if(name.equals("ySize")) return getAutoSize(false);
            if(parent.isFlex()) return parent.getFlexPosition(this, name);
            return getFallbackPosition(name);
        }

        private double getFallbackPosition(String name) {
            return switch(name) {
                case "x" -> parent.getPaddingLeft();
                case "y" -> parent.getPaddingBottom();
                default -> 0;
            };
        }

        private double getAutoSize(boolean xAxis) {
            if(parent == null) return 0;
            if(parent.isFlex() && isStretchedCrossAxis(xAxis)) {
                return Math.max(0, parent.getInnerSize(xAxis));
            }
            return 0;
        }

        private boolean isStretchedCrossAxis(boolean xAxis) {
            String direction = parent.getFlexDirection();
            boolean mainAxisX = direction.equals("row") || direction.equals("row-reverse");
            return mainAxisX != xAxis && parent.getAlignItems().equals("stretch");
        }

        private boolean isFlex() {
            return (getAsString("display") != null && getAsString("display").equalsIgnoreCase("flex")) || has("flexDirection");
        }

        private String getFlexDirection() {
            return getAsString("flexDirection") == null ? "row" : getAsString("flexDirection");
        }

        private String getJustifyContent() {
            return getAsString("justifyContent") == null ? "flex-start" : getAsString("justifyContent");
        }

        private String getAlignItems() {
            return getAsString("alignItems") == null ? "stretch" : getAsString("alignItems");
        }

        private int getGap() {
            return has("gap") ? getAsInt("gap") : 0;
        }

        private int getInnerSize(boolean xAxis) {
            int size = xAxis ? getXSize() : getYSize();
            int before = xAxis ? getPaddingLeft() : getPaddingTop();
            int after = xAxis ? getPaddingRight() : getPaddingBottom();
            return Math.max(0, size - before - after);
        }

        private double getFlexPosition(ScreenParentSS child, String name) {
            boolean row = getFlexDirection().equals("row") || getFlexDirection().equals("row-reverse");
            if(name.equals("x")) return row ? getMainAxisPosition(child, true) : getCrossAxisPosition(child, true);
            if(name.equals("y")) return row ? getCrossAxisPosition(child, false) : getMainAxisPosition(child, false);
            return 0;
        }

        private double getMainAxisPosition(ScreenParentSS child, boolean xAxis) {
            boolean reverse = getFlexDirection().endsWith("-reverse");
            int innerSize = getInnerSize(xAxis);
            int childCount = children.size();
            int gap = getGap();
            int totalChildSize = 0;
            for(ScreenParentSS sibling : children) totalChildSize += sibling.getMainSize(xAxis);

            int gapsSize = Math.max(0, childCount - 1) * gap;
            double free = Math.max(0, innerSize - totalChildSize - gapsSize);
            double start = getMainAxisStartOffset(free, childCount);
            double between = getMainAxisBetweenOffset(free, childCount, gap);
            double position = start;
            int visualIndex = reverse ? childCount - 1 - child.index : child.index;

            for(int i = 0; i < visualIndex; i++) {
                ScreenParentSS sibling = children.get(reverse ? childCount - 1 - i : i);
                position += sibling.getMainSize(xAxis) + between;
            }

            if(reverse) position = innerSize - position - child.getMainSize(xAxis);

            if(xAxis) return getPaddingLeft() + position;
            return getYSize() - getPaddingTop() - position - child.getYSize();
        }

        private int getMainSize(boolean xAxis) {
            return xAxis ? getXSize() : getYSize();
        }

        private double getMainAxisStartOffset(double free, int childCount) {
            return switch(getJustifyContent()) {
                case "center" -> free / 2;
                case "flex-end", "end" -> free;
                case "space-around" -> childCount == 0 ? 0 : free / childCount / 2;
                case "space-evenly" -> childCount == 0 ? 0 : free / (childCount + 1);
                default -> 0;
            };
        }

        private double getMainAxisBetweenOffset(double free, int childCount, int gap) {
            return switch(getJustifyContent()) {
                case "space-between" -> childCount <= 1 ? 0 : gap + free / (childCount - 1);
                case "space-around" -> childCount == 0 ? gap : gap + free / childCount;
                case "space-evenly" -> childCount == 0 ? gap : gap + free / (childCount + 1);
                default -> gap;
            };
        }

        private double getCrossAxisPosition(ScreenParentSS child, boolean xAxis) {
            String align = getAlignItems();
            int innerSize = getInnerSize(xAxis);
            int childSize = xAxis ? child.getXSize() : child.getYSize();
            double free = Math.max(0, innerSize - childSize);

            if(xAxis) {
                return switch(align) {
                    case "center" -> getPaddingLeft() + free / 2;
                    case "flex-end", "end" -> getPaddingLeft() + free;
                    default -> getPaddingLeft();
                };
            }

            return switch(align) {
                case "center" -> getPaddingBottom() + free / 2;
                case "flex-end", "end" -> getPaddingBottom();
                default -> getYSize() - getPaddingTop() - childSize;
            };
        }
    }
}
