package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.controllers.*;

import java.util.UUID;

public class DummyController implements Controller, ControllerUtil {
    public static ControllerMapping DUMMY_MAPPING;

    @Override
    public boolean getButton(int buttonCode) {
        return false;
    }

    @Override
    public float getAxis(int axisCode) {
        return 0;
    }

    @Override
    public String getName() {
        return "Dummy";
    }

    @Override
    public String getUniqueId() {
        return UUID.randomUUID().toString();
    }

    @Override
    public int getMinButtonIndex() {
        return 0;
    }

    @Override
    public int getMaxButtonIndex() {
        return 0;
    }

    @Override
    public int getAxisCount() {
        return 0;
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean canVibrate() {
        return false;
    }

    @Override
    public boolean isVibrating() {
        return false;
    }

    @Override
    public void startVibration(int duration, float strength) {

    }

    @Override
    public void cancelVibration() {

    }

    @Override
    public boolean supportsPlayerIndex() {
        return false;
    }

    @Override
    public int getPlayerIndex() {
        return 0;
    }

    @Override
    public void setPlayerIndex(int index) {

    }

    @Override
    public ControllerMapping getMapping() {
        return DUMMY_MAPPING;
    }

    @Override
    public ControllerPowerLevel getPowerLevel() {
        return ControllerPowerLevel.POWER_FULL;
    }

    @Override
    public void addListener(ControllerListener listener) {

    }

    @Override
    public void removeListener(ControllerListener listener) {

    }


    // ControllerUtil


    @Override
    public boolean wasJustPressed(ControllerInput input) {
        return false;
    }

    @Override
    public float getValue(ControllerInput input) {
        return 0;
    }

    @Override
    public boolean wasJustPressedThisTick(ControllerInput input) {
        return false;
    }

    @Override
    public void onTick() {

    }

    @Override
    public void updateUtil() {

    }

    @Override
    public void vibrate(VibrationAmount calculator) {

    }

    @Override
    public void vibrate(VibrationAmount calculator, float maxTime) {

    }

    @Override
    public void vibrate(float intensity, float maxTime) {

    }

    @Override
    public float[] getCurrentVibration() {
        return new float[]{0f, 0f};
    }

    @Override
    public boolean isPressed(ControllerInput input) {
        return false;
    }
}
