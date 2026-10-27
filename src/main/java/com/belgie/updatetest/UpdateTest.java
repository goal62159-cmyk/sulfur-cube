package com.belgie.updatetest;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import org.slf4j.Logger;

public class UpdateTest implements ModInitializer {
    public static final String MODID = "updatetest";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    public static final ResourceKey<EntityType<?>> SULFER_CUBE_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, id("sulfer_cube"));

    public static final EntityType<SulferCubeEntity> SULFER_CUBE = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            SULFER_CUBE_KEY,
            EntityType.Builder.of(SulferCubeEntity::new, MobCategory.CREATURE)
                    .sized(0.52f, 0.52f)
                    .eyeHeight(0.325f)
                    .spawnDimensionsScale(4.0f)
                    .clientTrackingRange(10)
                    .build(SULFER_CUBE_KEY));

    public static final ResourceKey<Item> SPAWN_EGG_KEY =
            ResourceKey.create(Registries.ITEM, id("sulfer_cube_spawn_egg"));

    public static final Item SPAWN_EGG = Registry.register(
            BuiltInRegistries.ITEM,
            SPAWN_EGG_KEY,
            new SpawnEggItem(new Item.Properties().spawnEgg(SULFER_CUBE).setId(SPAWN_EGG_KEY)));

    public static final ResourceKey<CreativeModeTab> TAB_KEY =
            ResourceKey.create(Registries.CREATIVE_MODE_TAB, id("example_tab"));

    public static final CreativeModeTab TAB = Registry.register(
            BuiltInRegistries.CREATIVE_MODE_TAB,
            TAB_KEY,
            FabricItemGroup.builder()
                    .title(Component.translatable("itemGroup.updatetest"))
                    .withTabsBefore(CreativeModeTabs.COMBAT)
                    .icon(() -> SPAWN_EGG.getDefaultInstance())
                    .displayItems((parameters, output) -> output.accept(SPAWN_EGG))
                    .build());

    @Override
    public void onInitialize() {
        FabricDefaultAttributeRegistry.register(SULFER_CUBE, SulferCubeEntity.createMobAttributes());

        // NeoForge biome_modifier (add_spawns) ka Fabric version: overworld, weight 5, group 2-2
        BiomeModifications.addSpawn(BiomeSelectors.foundInOverworld(),
                MobCategory.CREATURE, SULFER_CUBE, 5, 2, 2);

        // Original mod ye sirf client class mein register karta tha (bug) - yahan common side par hai
        SpawnPlacements.register(SULFER_CUBE, SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SulferCubeEntity::checkSulferCubeSpawnRules);

        LOGGER.info("Sulfer Cube (Fabric) loaded");
    }
}
