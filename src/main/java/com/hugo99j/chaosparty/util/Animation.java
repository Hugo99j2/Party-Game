package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;

import java.util.List;

public record Animation(String name, float frameTime, List<TextureAtlas.AtlasRegion> sprites) {
}
