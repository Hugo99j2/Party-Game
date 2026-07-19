package com.hugo99j.chaosparty.entity;

import com.daniel99j.dungeongame.entity.*;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class ObjectTypes {
    public static final Map<String, ObjectType<?>> types = new HashMap<>();

    public static final ObjectType<Player> PLAYER = registerObjectType("player", Player::read, Player::createDefault);
    public static final ObjectType<SpriteObject> TILESET = registerObjectType("tileset", SpriteObject::read, SpriteObject::createDefault);
    public static final ObjectType<Button> BUTTON = registerObjectType("button", Button::read, Button::createDefault);
    public static final ObjectType<Sheep> SHEEP = registerObjectType("sheep", Sheep::read, Sheep::createDefault);
    public static final ObjectType<Potato> POTATO = registerObjectType("potato", Potato::read, Potato::createDefault);
    public static final ObjectType<LiquidBarrelObject> LIQUID_BARREL = registerObjectType("liquid_barrel", LiquidBarrelObject::read, LiquidBarrelObject::createDefault);
    public static final ObjectType<CollisionObject> COLLISION = registerObjectType("collision", CollisionObject::read, CollisionObject::createDefault);
    public static final ObjectType<PlayerSpawnPoint> PLAYER_SPAWN_POINT = registerObjectType("player_spawn_point", PlayerSpawnPoint::read, PlayerSpawnPoint::createDefault);
    public static final ObjectType<FallingFloorObject> FALLING_FLOOR = registerObjectType("falling_floor", FallingFloorObject::read, FallingFloorObject::createDefault);
    public static final ObjectType<TemporaryDevObject> TEMP_DEV_OBJECT = registerObjectType("temp_dev_object", TemporaryDevObject::read, TemporaryDevObject::createDefault);


    private static <T extends AbstractObject> ObjectType<T> registerObjectType(String id, Function<JsonObject, T> reader, Supplier<T> defaultConstructor) {
        boolean showInEditor = false;
        try {
            if(defaultConstructor.get() != null) showInEditor = true;
        } catch (Exception ignored) {}
        ObjectType<T> type = new ObjectType<>(id, reader, defaultConstructor, showInEditor);
        types.put(type.id(), type);
        return type;
    }
}
