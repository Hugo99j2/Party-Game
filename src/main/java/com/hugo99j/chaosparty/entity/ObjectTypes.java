package com.hugo99j.chaosparty.entity;

import com.daniel99j.dungeongame.entity.*;

import java.util.HashMap;
import java.util.Map;

public class ObjectTypes {
    public static final Map<String, ObjectType<?>> types = new HashMap<>();

    public static final ObjectType<Player> PLAYER = registerObjectType(new ObjectType<>("player", Player::read, Player::createDefault));
    public static final ObjectType<SpriteObject> TILESET = registerObjectType(new ObjectType<>("tileset", SpriteObject::read, SpriteObject::createDefault));
    public static final ObjectType<Button> BUTTON = registerObjectType(new ObjectType<>("button", Button::read, Button::createDefault));
    public static final ObjectType<Sheep> SHEEP = registerObjectType(new ObjectType<>("sheep", Sheep::read, Sheep::createDefault));
    public static final ObjectType<Potato> POTATO = registerObjectType(new ObjectType<>("potato", Potato::read, Potato::createDefault));
    public static final ObjectType<LiquidBarrelObject> LIQUID_BARREL = registerObjectType(new ObjectType<>("liquid_barrel", LiquidBarrelObject::read, LiquidBarrelObject::createDefault));
    public static final ObjectType<CollisionObject> COLLISION = registerObjectType(new ObjectType<>("collision", CollisionObject::read, CollisionObject::createDefault));
    public static final ObjectType<PlayerSpawnPoint> PLAYER_SPAWN_POINT = registerObjectType(new ObjectType<>("player_spawn_point", PlayerSpawnPoint::read, PlayerSpawnPoint::createDefault));
    public static final ObjectType<FallingFloorObject> FALLING_FLOOR = registerObjectType(new ObjectType<>("falling_floor", FallingFloorObject::read, FallingFloorObject::createDefault));


    private static <T extends AbstractObject> ObjectType<T> registerObjectType(ObjectType<T> type) {
        types.put(type.id(), type);
        return type;
    }
}
