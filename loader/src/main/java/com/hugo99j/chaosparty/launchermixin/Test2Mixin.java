package com.hugo99j.chaosparty.launchermixin;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * An example mixin that calls into Fabric Loader so it can finish initializing itself.
 */
@Mixin(ShapeRenderer.class)
public abstract class Test2Mixin {
}
