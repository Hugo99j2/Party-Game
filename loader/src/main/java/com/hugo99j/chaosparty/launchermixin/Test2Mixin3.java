package com.hugo99j.chaosparty.launchermixin;

import com.daniel99j.djutil.NumberUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * An example mixin that calls into Fabric Loader so it can finish initializing itself.
 */
@Mixin(value = NumberUtils.class, remap = false)
public abstract class Test2Mixin3 {
}
