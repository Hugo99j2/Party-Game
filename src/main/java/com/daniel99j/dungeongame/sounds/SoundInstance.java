package com.daniel99j.dungeongame.sounds;

import com.badlogic.gdx.backends.lwjgl3.audio.OpenALSound;

import java.util.function.Consumer;

import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.alGetSourcei;

public class SoundInstance {
    private final SoundFile file;
    private final long id;
    private final Consumer<SoundInstance> onFinish;
    private float volume;
    private float pan;
    private float pitch;
    private boolean paused = false;
    private float currentTime = 0;
    private final float duration;

    protected SoundInstance(SoundFile file, float volume, float pitch, float pan, Consumer<SoundInstance> onFinish) {
        this.file = file;
        this.id = file.getActualAudio().play();
        this.onFinish = onFinish;
        this.setVolume(volume);
        this.setPitch(pitch);
        this.setPan(pan);
        this.duration = ((OpenALSound) this.file.getActualAudio()).duration();
    }

    public void pause() {
        this.file.getActualAudio().pause(this.id);
        paused = true;
    }

    public void play() {
        this.file.getActualAudio().resume(this.id);
        paused = false;
    }

    public void cancel() {
        this.file.getActualAudio().stop(this.id);
        this.onFinish.accept(this);
        this.file.getInstances().remove(this);
    }

    public void setVolume(float v) {
        this.file.getActualAudio().setVolume(this.id, v);
        this.volume = v;
    }

    public void setPitch(float p) {
        this.file.getActualAudio().setPitch(this.id, p);
        pitch = p;
    }

    public void setPan(float p) {
        this.file.getActualAudio().setPan(this.id, p, this.volume);
        pan = p;
    }

    public float getDuration() {
        return duration;
    }

    public float getCurrentTime() {
        return currentTime;
    }

    public boolean isComplete() {
        return this.currentTime > this.duration;
    }

    protected void tick(float deltaTime) {
        if(paused) return;
        this.currentTime += deltaTime*pitch;
        if(this.isComplete()) {
            this.cancel();
        }
    }

    public float getPitch() {
        return pitch;
    }

    public float getVolume() {
        return volume;
    }

    public float getPan() {
        return pan;
    }

    public String getName() {
        return file.getName();
    }

    public boolean isPaused() {
        return paused;
    }
}
