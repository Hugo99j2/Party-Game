package com.hugo99j.chaosparty.mixin;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.daniel99j.dungeongame.sounds.SoundFile;
import com.hugo99j.chaosparty.GameData;
import com.hugo99j.chaosparty.util.ImageUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(Sound.class)
public abstract class MoreSoundsMixin {
    @Shadow
    public abstract BitmapFont.BitmapFontData getData();

    @SuppressWarnings("AddedMixinMembersNamePattern")
    @Unique
    private static final Map<Character, CharRegion> icons = new HashMap<>();

    @Inject(method = "<init>(Lcom/badlogic/gdx/graphics/g2d/BitmapFont$BitmapFontData;Lcom/badlogic/gdx/utils/Array;Z)V", at = @At(value = "INVOKE", target = "Lcom/badlogic/gdx/graphics/g2d/BitmapFont;newFontCache()Lcom/badlogic/gdx/graphics/g2d/BitmapFontCache;", shift = At.Shift.BEFORE))
    private void addPages(BitmapFont.BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer, CallbackInfo ci) {
        List<String> alreadyAdded = new ArrayList<>();
        for (Map.Entry<String, Character> entry : GameData.getIcons().entrySet()) {
            TextureAtlas.AtlasRegion region = ImageUtil.get("ui/icon/" + entry.getKey());
            if(!alreadyAdded.contains(entry.getKey())) {
                alreadyAdded.add(entry.getKey());
                pageRegions.add(region);
            }
            icons.put(entry.getValue(), new CharRegion(pageRegions.indexOf(region, true), region.getU(), region.getV(), region.getU2(), region.getV2()));
        }
    }

    @Inject(method = "<init>(Lcom/badlogic/gdx/graphics/g2d/BitmapFont$BitmapFontData;Lcom/badlogic/gdx/utils/Array;Z)V", at = @At(value = "TAIL"))
    private void addGlyphs(BitmapFont.BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer, CallbackInfo ci) {
        icons.forEach((key, value) -> {
            BitmapFont.Glyph glyph = new BitmapFont.Glyph();
            glyph.page = value.page;
            glyph.width = 32;
            glyph.height = -32;
            glyph.yoffset = -16;
            glyph.fixedWidth = true;
            glyph.xadvance = glyph.width;
            glyph.u = value.u;
            glyph.v = value.v;
            glyph.u2 = value.u2;
            glyph.v2 = value.v2;
            this.getData().setGlyph(key, glyph);
        });
        icons.clear();
    }

    private record CharRegion(int page, float u, float v, float u2, float v2) {

    }
}
