package com.belgie.updatetest;

import com.google.common.annotations.VisibleForTesting;
import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.ConversionType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public class SulferCubeEntity extends Mob implements Enemy {
    private static final int DEFAULT_AGE = 0;
    @VisibleForTesting
    public static int ticksToBeFrog = 24000;
    private int age = DEFAULT_AGE;

    private static final EntityDataAccessor<Integer> ID_SIZE =
            SynchedEntityData.defineId(SulferCubeEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<BlockState>> DATA_CARRY_STATE =
            SynchedEntityData.defineId(SulferCubeEntity.class, EntityDataSerializers.OPTIONAL_BLOCK_STATE);

    public static final int MIN_SIZE = 1;
    public static final int MAX_SIZE = 127;
    public static final int MAX_NATURAL_SIZE = 4;

    public float targetSquish;
    public float squish;
    public float oSquish;
    private boolean wasOnGround = false;

    public SulferCubeEntity(EntityType<? extends SulferCubeEntity> type, Level level) {
        super(type, level);
        this.fixupDimensions();
        this.moveControl = new SlimeMoveControl(this);
    }

    public boolean canMove() {
        return this.getCarriedBlock() == null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide() && this.isTiny()) {
            this.setAge(this.age + 1);
        }
    }

    /** Sirf deep underground (sea level - 33 se neeche) spawn hota hai. */
    public static boolean checkSulferCubeSpawnRules(EntityType<SulferCubeEntity> type, LevelAccessor level,
                                                    EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return pos.getY() <= level.getSeaLevel() - 33 && checkMobSpawnRules(type, level, reason, pos, random);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (!this.canMove() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            Entity attacker = source.getEntity();
            if (!this.isInvulnerableTo(level, source) && !this.isDeadOrDying() && attacker instanceof Player player) {
                this.addDeltaMovement(player.getLookAngle().scale(
                        (double) (0.1f * damage * this.hasIce())
                                * this.getAttributeValue(Attributes.MOVEMENT_SPEED)
                                * (double) this.getBlockSpeedFactor()));
                this.hurtMarked = true;
                return true;
            }
            return false;
        }
        return super.hurtServer(level, source, damage);
    }

    public float hasIce() {
        BlockState carried = this.getCarriedBlock();
        if (carried != null) {
            if (carried.is(Blocks.PACKED_ICE)) {
                return 4.0f;
            }
            if (carried.is(BlockTags.WOOL)) {
                return 2.2f;
            }
        }
        return 0.6f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SlimeFloatGoal(this));
        this.goalSelector.addGoal(3, new SlimeRandomDirectionGoal(this));
        this.goalSelector.addGoal(5, new SlimeKeepOnJumpingGoal(this));
    }

    public static AttributeSupplier.Builder createMobAttributes() {
        return LivingEntity.createLivingAttributes().add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ID_SIZE, 1);
        builder.define(DATA_CARRY_STATE, Optional.empty());
    }

    @VisibleForTesting
    public void setSize(int size, boolean updateHealth) {
        int actual = Mth.clamp(size, MIN_SIZE, MAX_SIZE);
        this.entityData.set(ID_SIZE, actual);
        this.reapplyPosition();
        this.refreshDimensions();
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue((double) (actual * actual));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue((double) (0.2f + 0.1f * (float) actual));
        if (updateHealth) {
            this.setHealth(this.getMaxHealth());
        }
        this.xpReward = actual;
    }

    public int getSize() {
        return this.entityData.get(ID_SIZE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("Size", this.getSize() - 1);
        output.putInt("Age", this.age);
        output.putBoolean("wasOnGround", this.wasOnGround);
        BlockState carried = this.getCarriedBlock();
        if (carried != null) {
            output.store("carriedBlockState", BlockState.CODEC, carried);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        this.setCarriedBlock(input.read("carriedBlockState", BlockState.CODEC)
                .filter(state -> !state.isAir()).orElse(null));
        this.setAge(input.getIntOr("Age", 0));
        this.setSize(input.getIntOr("Size", 0) + 1, false);
        super.readAdditionalSaveData(input);
        this.wasOnGround = input.getBooleanOr("wasOnGround", false);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        BlockState carried = this.getCarriedBlock();
        if (carried == null && held.getItem() instanceof BlockItem blockItem) {
            this.setCarriedBlock(blockItem.getBlock().defaultBlockState());
            this.level().playSound(player, this, SoundEvents.ALLAY_ITEM_GIVEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (carried != null && held.is(Items.SHEARS)) {
            this.level().playSound(player, this, SoundEvents.ALLAY_ITEM_TAKEN, SoundSource.NEUTRAL, 2.0f, 1.0f);
            this.swing(InteractionHand.MAIN_HAND);
            if (this.level() instanceof ServerLevel serverLevel) {
                this.spawnAtLocation(serverLevel, BlockItem.byBlock(carried.getBlock()).asItem());
            }
            this.setCarriedBlock(null);
            held.hurtAndBreak(1, player, hand);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    private void setAge(int newAge) {
        this.age = newAge;
        if (this.age >= ticksToBeFrog) {
            this.setSize(3, true);
        }
    }

    public void setCarriedBlock(@Nullable BlockState state) {
        this.entityData.set(DATA_CARRY_STATE, Optional.ofNullable(state));
    }

    public @Nullable BlockState getCarriedBlock() {
        return this.entityData.get(DATA_CARRY_STATE).orElse(null);
    }

    public boolean isTiny() {
        return this.getSize() <= 1;
    }

    protected ParticleOptions getParticleType() {
        return ParticleTypes.COMPOSTER;
    }

    @Override
    public void tick() {
        this.oSquish = this.squish;
        this.squish += (this.targetSquish - this.squish) * 0.5f;
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        BlockState carried = this.getCarriedBlock();
        if (carried != null && !this.onGround() && movement.y < 0.0) {
            if (carried.is(BlockTags.WOOL)) {
                this.setDeltaMovement(movement.multiply(1.0, 0.6, 1.0));
            }
            if (carried.is(Blocks.IRON_BLOCK)) {
                this.setDeltaMovement(movement.multiply(1.0, 1.2, 1.0));
            }
        }
        if (this.onGround() && !this.wasOnGround) {
            this.playSound(this.getSquishSound(), this.getSoundVolume(),
                    ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) / 0.8f);
            this.targetSquish = -0.5f;
        } else if (!this.onGround() && this.wasOnGround) {
            this.targetSquish = 1.0f;
        }
        this.wasOnGround = this.onGround();
        this.decreaseSquish();
    }

    protected void decreaseSquish() {
        this.targetSquish *= 0.6f;
    }

    protected int getJumpDelay() {
        return this.random.nextInt(20) + 10;
    }

    @Override
    public void refreshDimensions() {
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();
        super.refreshDimensions();
        this.setPos(x, y, z);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (ID_SIZE.equals(accessor)) {
            this.refreshDimensions();
            this.setYRot(this.yHeadRot);
            this.yBodyRot = this.yHeadRot;
            if (this.isInWater() && this.random.nextInt(20) == 0) {
                this.doWaterSplashEffect();
            }
        }
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        int size = this.getSize();
        if (!this.level().isClientSide() && size > 1 && this.isDeadOrDying()) {
            float width = this.getDimensions(this.getPose()).width();
            float offset = width / 2.0f;
            int count = 2 + this.random.nextInt(3);
            PlayerTeam team = this.getTeam();
            for (int i = 0; i < count; ++i) {
                float xd = ((float) (i % 2) - 0.5f) * offset;
                float zd = ((float) (i / 2) - 0.5f) * offset;
                // convertTo() vanilla mein khud entity ko world mein add kar deta hai
                this.convertTo(this.getType(),
                        new ConversionParams(ConversionType.SPLIT_ON_DEATH, false, false, team),
                        EntitySpawnReason.TRIGGERED,
                        child -> {
                            child.setSize(1, true);
                            child.setPos(this.getX() + (double) xd, this.getY() + 0.5, this.getZ() + (double) zd);
                            child.setYRot(this.random.nextFloat() * 360.0f);
                            child.setXRot(0.0f);
                        });
            }
        }
        super.remove(reason);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0.0, (double) dimensions.height() - 0.015625 * (double) this.getSize() * (double) scale, 0.0);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isTiny() ? SoundEvents.SLIME_HURT_SMALL : SoundEvents.SLIME_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isTiny() ? SoundEvents.SLIME_DEATH_SMALL : SoundEvents.SLIME_DEATH;
    }

    protected SoundEvent getSquishSound() {
        return this.isTiny() ? SoundEvents.SLIME_SQUISH_SMALL : SoundEvents.SLIME_SQUISH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f * (float) this.getSize();
    }

    @Override
    public int getMaxHeadXRot() {
        return 0;
    }

    protected boolean doPlayJumpSound() {
        return this.getSize() > 0;
    }

    @Override
    public void jumpFromGround() {
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, this.getJumpPower(), movement.z);
        this.hurtMarked = true;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                                  EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.setSize(3, true);
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    private float getSoundPitch() {
        float adjuster = this.isTiny() ? 1.4f : 0.8f;
        return ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * adjuster;
    }

    protected SoundEvent getJumpSound() {
        return this.isTiny() ? SoundEvents.SLIME_JUMP_SMALL : SoundEvents.SLIME_JUMP;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale((float) this.getSize());
    }

    // ---------------------------------------------------------------- AI

    private static class SlimeMoveControl extends MoveControl {
        private float yRot;
        private int jumpDelay;
        private final SulferCubeEntity slime;
        private boolean isAggressive;

        public SlimeMoveControl(SulferCubeEntity slime) {
            super(slime);
            this.slime = slime;
            this.yRot = 180.0f * slime.getYRot() / (float) Math.PI;
        }

        public void setDirection(float yRot, boolean aggressive) {
            this.yRot = yRot;
            this.isAggressive = aggressive;
        }

        public void setWantedMovement(double speed) {
            this.speedModifier = speed;
            this.operation = MoveControl.Operation.MOVE_TO;
        }

        @Override
        public void tick() {
            if (!this.slime.canMove()) {
                return;
            }
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), this.yRot, 90.0f));
            this.mob.yHeadRot = this.mob.getYRot();
            this.mob.yBodyRot = this.mob.getYRot();
            if (this.operation != MoveControl.Operation.MOVE_TO) {
                this.mob.setZza(0.0f);
                return;
            }
            this.operation = MoveControl.Operation.WAIT;
            if (this.mob.onGround()) {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
                if (this.jumpDelay-- <= 0) {
                    this.jumpDelay = this.slime.getJumpDelay();
                    if (this.isAggressive) {
                        this.jumpDelay /= 3;
                    }
                    this.slime.getJumpControl().jump();
                    if (this.slime.doPlayJumpSound()) {
                        this.slime.playSound(this.slime.getJumpSound(), this.slime.getSoundVolume(),
                                this.slime.getSoundPitch());
                    }
                } else {
                    this.slime.xxa = 0.0f;
                    this.slime.zza = 0.0f;
                    this.mob.setSpeed(0.0f);
                }
            } else {
                this.mob.setSpeed((float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            }
        }
    }

    private static class SlimeFloatGoal extends Goal {
        private final SulferCubeEntity slime;

        public SlimeFloatGoal(SulferCubeEntity mob) {
            this.slime = mob;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
            mob.getNavigation().setCanFloat(true);
        }

        @Override
        public boolean canUse() {
            return (this.slime.isInWater() || this.slime.isInLava())
                    && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (this.slime.getRandom().nextFloat() < 0.8f) {
                this.slime.getJumpControl().jump();
            }
            if (this.slime.getMoveControl() instanceof SlimeMoveControl control) {
                control.setWantedMovement(1.2);
            }
        }
    }

    private static class SlimeRandomDirectionGoal extends Goal {
        private final SulferCubeEntity slime;
        private float chosenDegrees;
        private int nextRandomizeTime;

        public SlimeRandomDirectionGoal(SulferCubeEntity slime) {
            this.slime = slime;
            this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.slime.getTarget() == null
                    && (this.slime.onGround() || this.slime.isInWater() || this.slime.isInLava()
                    || this.slime.hasEffect(MobEffects.LEVITATION))
                    && this.slime.getMoveControl() instanceof SlimeMoveControl;
        }

        @Override
        public void tick() {
            if (--this.nextRandomizeTime <= 0) {
                this.nextRandomizeTime = this.adjustedTickDelay(40 + this.slime.getRandom().nextInt(60));
                this.chosenDegrees = this.slime.getRandom().nextInt(360);
            }
            if (this.slime.getMoveControl() instanceof SlimeMoveControl control) {
                control.setDirection(this.chosenDegrees, false);
            }
        }
    }

    private static class SlimeKeepOnJumpingGoal extends Goal {
        private final SulferCubeEntity slime;

        public SlimeKeepOnJumpingGoal(SulferCubeEntity mob) {
            this.slime = mob;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return !this.slime.isPassenger();
        }

        @Override
        public void tick() {
            if (this.slime.getMoveControl() instanceof SlimeMoveControl control) {
                control.setWantedMovement(1.0);
            }
        }
    }
}
