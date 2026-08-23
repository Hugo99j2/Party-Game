package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.math.Interpolation;

import java.util.function.Function;

public class VibrationAmount {
    private final Function<Float, Float> intense;
    private final Function<Float, Float> fast;

    private VibrationAmount(Function<Float, Float> intense, Function<Float, Float> fast) {
        this.intense = intense;
        this.fast = fast;
    }

    public static VibrationAmount of(Function<Float, Float> intense, Function<Float, Float> fast) {
        return new VibrationAmount(intense, fast);
    }

    public static VibrationAmount of(Function<Float, Float> amount) {
        return new VibrationAmount(amount, amount);
    }

    public static VibrationAmount of(float amount) {
        return of((f) -> amount);
    }

    //new float[]{0.0f,0.18584071f,0.28169015f,0.18584071f,0.36217305f,0.40265486f,0.5633803f,0.40265486f,0.5633803f,0.7787611f,0.86519116f,0.78318584f,1.0060362f,0.0f,1.167002f,0.3539823f,1.4889336f,0.3761062f,1.4889336f,0.088495575f,1.5694165f,0.26548672f,1.8913481f,0.27433628f,2.0f,0.0f,2.1931589f,0.45575222f,2.5150905f,0.44690266f,2.5754528f,0.03539823f,2.8772635f,0.4778761f,3.2394366f,0.46902654f,3.2997987f,0.048672568f,3.5412474f,0.24336283f,3.8229377f,0.23451327f,3.8832998f,0.0f,4.0241446f,0.044247787f,4.2253523f,0.16371681f,4.5674043f,0.17256637f,4.72837f,0.0f,4.9295774f,0.26106194f,5.2917504f,0.2699115f,5.4124746f,0.030973451f,5.593561f,0.42920354f,6.1770625f,0.43362832f,6.2173038f,0.057522126f,6.4788733f,0.44247788f,6.8008046f,0.44690266f,7.0422535f,0.03539823f,7.2032194f,0.3318584f,8.128773f,0.3318584f,8.128773f,0.0f,8.32998f,0.03539823f,8.490946f,0.2079646f,9.074447f,0.24778761f,9.094567f,0.06637168f,9.295774f,0.15044248f,10.301811f,0.1460177f,10.56338f,0.0f,10.824949f,0.26106194f,11.830986f,0.22123894f,12.132797f,0.25221238f,12.334004f,0.6814159f,12.6961775f,0.7079646f,12.877264f,0.28761062f,13.259558f,0.44690266f,13.722334f,0.45575222f,13.742455f,0.31858408f,14.064386f,0.20353982f,14.587525f,0.23451327f,14.768612f,0.123893805f,15.231388f,0.13716814f,15.352113f,0.2920354f,16.116701f,0.29646018f,16.519115f,0.14159292f,17.022133f,0.15044248f,17.203218f,0.0f}, new float[]{0.0f,1.0f,0.1f,0.4f,2.0f,0.0f}
    public static VibrationAmount of(float[] fast, float[] intense) {
        float maxFast = fast.length == 0 ? 0 : fast[fast.length - 2];
        float maxIntense = intense.length == 0 ? 0 : intense[intense.length - 2];
        float max = Math.max(maxFast, maxIntense);
        return new VibrationAmount((t) -> {
            return parse(intense, maxIntense, max, t);
        }, (t) -> {
            return parse(fast, maxFast, max, t);
        });
    }

    private static float parse(float[] floats, float maxThis, float maxAll, float time) {
        if(time >= maxAll) return -1f;
        if(time >= maxThis) return 0f;
        int index = 0;
        for (int i = 0; i < floats.length; i+=2) {
            if(floats[i] >= time) {
                index = i;
                break;
            }
        }
        int previous = index-2;

        float startTime = 0;
        float endTime = floats[index];
        float startValue = 0;
        float endValue = floats[index + 1];
        if (previous >= 0) {
            startTime = floats[previous];
            startValue = floats[previous + 1];
        }

        float alpha = (time - startTime) / (endTime - startTime);
        return startValue + (endValue - startValue) * alpha;
    }

    public float getIntense(float currentTime) {
        return intense.apply(currentTime);
    }

    public float getFast(float currentTime) {
        return fast.apply(currentTime);
    }
}
