package com.daniel99j.dungeongame.sounds;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.daniel99j.djutil.Either;
import com.daniel99j.dungeongame.util.PathUtil;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SoundFile implements Disposable {
    private final Sound actualAudio;
    private final String name;
    private final ArrayList<SoundInstance> instances = new ArrayList<>();

    protected SoundFile(String name) {
        this.actualAudio = Gdx.audio.newSound(Gdx.files.internal(PathUtil.asset("sounds/"+name+".mp3")));
        this.name = name;
    }

    public SoundInstance playSingle(float volume) {
        return playSingle(volume, 1);
    }

    public SoundInstance playSingle(float volume, float pitch) {
        return playSingle(volume, pitch, 0);
    }

    public SoundInstance playSingle(float volume, float pitch, float pan) {
        return playSingle(volume, pitch, pan, (s) -> {});
    }

    public SoundInstance playSingle(float volume, float pitch, float pan, Consumer<SoundInstance> onFinish) {
        return play(volume, pitch, pan, (s) -> {
            boolean any = false;
            for (SoundInstance activeSound : this.getInstances()) {
                if(activeSound != s) {
                    any = true;
                    break;
                }
            }
            if(!any) dispose();
            onFinish.accept(s);
        });
    }

    public SoundInstance play(float volume) {
        return play(volume, 1);
    }

    public SoundInstance play(float volume, float pitch) {
        return play(volume, pitch, 0);
    }

    public SoundInstance play(float volume, float pitch, float pan) {
        return play(volume, pitch, pan, (s) -> {});
    }


    public SoundInstance play(float volume, float pitch, float pan, Consumer<SoundInstance> onFinish) {
        SoundInstance i = new SoundInstance(this, volume, pitch, pan, onFinish);
        instances.add(i);
        return i;
    }

    protected Sound getActualAudio() {
        return actualAudio;
    }

    @Override
    public void dispose() {
        this.actualAudio.dispose();
        SoundManager.loadedFiles.remove(name);
    }

    public void tick(float deltaTime) {
        ArrayList<SoundInstance> a = new ArrayList<>(this.instances);
        for (SoundInstance instance : a) {
            instance.tick(deltaTime);
        }
    }

    protected ArrayList<SoundInstance> getInstances() {
        return instances;
    }

    public String getName() {
        return name;
    }
}
