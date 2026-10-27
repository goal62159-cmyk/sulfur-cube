# Sulfur Cube - Fabric port (Minecraft 1.21.11)

NeoForge mod `updatetest` ka Fabric version.

## Build
1. JDK 21 install karo, folder ko IntelliJ IDEA mein open karo (ya terminal: `gradle build`, Gradle 9.x chahiye).
2. Jar yahan milegi: `build/libs/sulfur-cube-fabric-1.0.0.jar`
3. Isse `.minecraft/mods` mein daalo + Fabric API (1.21.11) bhi chahiye.

Test: `/summon updatetest:sulfer_cube` ya creative tab "Sulfer Cube" se spawn egg lo.

## Kya badla (NeoForge -> Fabric)
- DeferredRegister -> `Registry.register`
- NeoForge biome_modifier JSON -> `BiomeModifications.addSpawn`
- EntityAttributeCreationEvent -> `FabricDefaultAttributeRegistry`
- Renderer/model layer events -> `EntityRendererRegistry` / `EntityModelLayerRegistry`
- Spawn placement ab common side par hai (original mein sirf client class mein tha, wo bug tha)
- Hataya gaya: ModConfig (sirf log messages ke liye tha), `example_block` (iska koi model/texture hi nahi tha), empty mixins, NeoForge hooks (onMobSplit, onLivingJump)
