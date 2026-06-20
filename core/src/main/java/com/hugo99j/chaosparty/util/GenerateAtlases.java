package com.hugo99j.chaosparty.util;

import com.badlogic.gdx.tools.texturepacker.TexturePacker;

import java.util.Objects;

public class GenerateAtlases {
    public static void main(String[] args) {
        if(Objects.equals(System.getenv("CODING_GAME"), "1")) {
            //Create atlases
            TexturePacker.Settings settings = new TexturePacker.Settings();
            settings.combineSubdirectories = true;
            TexturePacker.process(settings, PathUtil.codingDir(PathUtil.asset("textures")), PathUtil.codingDir(PathUtil.generated("atlases")), "main");
        }
    }
}
