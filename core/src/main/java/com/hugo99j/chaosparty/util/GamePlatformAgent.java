package com.hugo99j.chaosparty.util;

import org.spongepowered.asm.launch.platform.IMixinPlatformAgent;
import org.spongepowered.asm.launch.platform.MixinPlatformManager;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.IMixinPlatformAgent.AcceptResult;

public class GamePlatformAgent implements IMixinPlatformAgent {

    @Override
    public AcceptResult accept(MixinPlatformManager manager, IContainerHandle handle) {
        return AcceptResult.ACCEPTED;
    }

    @Override
    public String getPhaseProvider() {
        return "org.spongepowered.asm.launch.platform.MainPhaseProvider";
    }

    @Override
    public void prepare() {
        // no-op
    }

    @Override
    public void initPrimaryContainer() {
    }

    @Override
    public void inject() {
    }
}
