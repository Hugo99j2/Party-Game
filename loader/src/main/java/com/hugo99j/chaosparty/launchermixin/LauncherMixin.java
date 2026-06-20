package com.hugo99j.chaosparty.launchermixin;

import com.hugo99j.chaosparty.Launcher;
import com.hugo99j.chaosparty.loader.Lwjgl3Launcher;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

/**
 * An example mixin that calls into Fabric Loader so it can finish initializing itself.
 */
@Mixin(Launcher.class)
public abstract class LauncherMixin {
    // This allows Fabric loader to do the rest of the initialization work that it needs to do.
    @Inject(
        method = "main",
        at = @At(
            value = "INVOKE",
            target = "Lcom/hugo99j/chaosparty/loader/Lwjgl3Launcher;main([Ljava/lang/String;)V"
        ),
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    private static void main(String[] args, CallbackInfo ci, Lwjgl3Launcher base) {
        //FabricLoaderImpl.INSTANCE.prepareModInit(FabricLoader.getInstance().getGameDir(), base);
    }

//    @Inject(
//        method = "main",
//        at = @At(
//            value = "HEAD"
//        ),
//        locals = LocalCapture.CAPTURE_FAILHARD
//    )
//    private static void test(String[] args, CallbackInfo ci) {
//        test2(args);
//    }
//
//    @Unique
//    private static void test2(String[] args) {
//        FabricLoaderImpl.INSTANCE.prepareModInit(FabricLoader.getInstance().getGameDir(), base);
//
//    }
//    static {
//        System.out.println("Loaded LauncherMixin!");
//    }
}
