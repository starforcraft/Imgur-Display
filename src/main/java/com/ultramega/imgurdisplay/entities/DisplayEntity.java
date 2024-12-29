package com.ultramega.imgurdisplay.entities;

import com.ultramega.imgurdisplay.gui.DisplayScreen;
import com.ultramega.imgurdisplay.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DisplayEntity extends Entity {
    private static final EntityDataAccessor<String> ID = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Direction> FACING = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Integer> WIDTH = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> STRETCHED = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> EDIT_RESTRICTED = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SHOW_HITBOX = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<UUID>> OWNER = SynchedEntityData.defineId(DisplayEntity.class, EntityDataSerializers.OPTIONAL_UUID);

    private static final AABB NULL_AABB = new AABB(0D, 0D, 0D, 0D, 0D, 0D);
    private static final double THICKNESS = 1D / 16D;
    private static final int MAX_WIDTH = 8;
    private static final int MAX_HEIGHT = 8;

    public DisplayEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        setBoundingBox(NULL_AABB);
    }

    @Override
    public void tick() {
        updateBoundingBox();
        super.tick();
    }

    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (!canModify(player)) {
            return InteractionResult.PASS;
        }

        if (level().isClientSide()) {
            openGui();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @OnlyIn(Dist.CLIENT)
    private void openGui() {
        Minecraft.getInstance().setScreen(new DisplayScreen(getUUID(), getImageID(), isStretched(), isEditRestricted(), isShowHitbox()));
    }

    private boolean canModify(Player player) {
        if (!player.getAbilities().mayBuild) {
            return false;
        }

        if (isEditRestricted() && !player.hasPermissions(1)) {
            Optional<UUID> ownerUUID = getOwner();
            if (ownerUUID.isPresent()) {
                return player.getUUID().equals(ownerUUID.get());
            }
        }

        return true;
    }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (level().isClientSide()) {
            return true;
        }
        if (!(source.getDirectEntity() instanceof Player)) {
            return false;
        }
        if (isInvulnerableTo(source)) {
            return false;
        }

        this.gameEvent(GameEvent.BLOCK_CHANGE, source.getEntity());
        this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);

        removeFrame(source.getEntity());
        return true;
    }

    @Override
    protected void defineSynchedData(final SynchedEntityData.Builder builder) {
        builder.define(ID, "");
        builder.define(FACING, Direction.NORTH);
        builder.define(WIDTH, 1);
        builder.define(HEIGHT, 1);
        builder.define(STRETCHED, false);
        builder.define(EDIT_RESTRICTED, false);
        builder.define(SHOW_HITBOX, true);
        builder.define(OWNER, Optional.empty());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compoundTag) {
        setImageID(compoundTag.getString("imageId"));
        setFacing(Direction.from3DDataValue(compoundTag.getInt("facing")));
        setDisplayWidth(compoundTag.getInt("width"));
        setDisplayHeight(compoundTag.getInt("height"));
        setStretched(compoundTag.getBoolean("stretched"));
        setEditRestricted(compoundTag.getBoolean("editRestricted"));
        setShowHitbox(compoundTag.getBoolean("showHitbox"));
        if (compoundTag.contains("owner")) {
            setOwner(compoundTag.getUUID("owner"));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag compoundTag) {
        compoundTag.putString("imageId", getImageID());
        compoundTag.putInt("facing", getFacing().get3DDataValue());
        compoundTag.putInt("width", getDisplayWidth());
        compoundTag.putInt("height", getDisplayHeight());
        compoundTag.putBoolean("stretched", isStretched());
        compoundTag.putBoolean("editRestricted", isEditRestricted());
        compoundTag.putBoolean("showHitbox", isShowHitbox());
        if (getOwner().isPresent()) {
            compoundTag.putUUID("owner", getOwner().get());
        }
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return calculateBoundingBox(blockPosition(), getFacing(), getDisplayWidth(), getDisplayHeight());
    }

    private AABB calculateBoundingBox(BlockPos pos, Direction facing, double width, double height) {
        return switch (facing) {
            case SOUTH ->
                    new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + width, pos.getY() + height, pos.getZ() + THICKNESS);
            case WEST ->
                    new AABB(pos.getX() + 1D - THICKNESS, pos.getY(), pos.getZ(), pos.getX() + 1D, pos.getY() + height, pos.getZ() + width);
            case EAST ->
                    new AABB(pos.getX(), pos.getY(), pos.getZ() + 1D, pos.getX() + THICKNESS, pos.getY() + height, pos.getZ() - width + 1D);
            default ->
                    new AABB(pos.getX() + 1D, pos.getY(), pos.getZ() + 1D - THICKNESS, pos.getX() - width + 1D, pos.getY() + height, pos.getZ() + 1D);
        };
    }

    public void resize(Direction direction, int amount) {
        switch (direction) {
            case UP:
                setDisplayHeight(getDisplayHeight() + amount);
                break;
            case DOWN:
                if (setDisplayHeight(getDisplayHeight() + amount)) {
                    setImagePosition(blockPosition().relative(Direction.DOWN, amount));
                }
                break;
            case EAST:
                setDisplayWidth(getDisplayWidth() + amount);
                break;
            case WEST:
                if (setDisplayWidth(getDisplayWidth() + amount)) {
                    setImagePosition(blockPosition().relative(getResizeOffset(), amount));
                }
                break;
        }

        combineDisplay();
    }

    public void combineDisplay() {
        AABB aabb = this.getBoundingBox().move(-this.getX(), -this.getY(), -this.getZ()).move(this.position());
        aabb = aabb.inflate(1.1);
        List<DisplayEntity> nearbyEntities = this.level().getEntitiesOfClass(DisplayEntity.class, aabb);

        if(!level().isClientSide()) {
            for(DisplayEntity display : nearbyEntities) {
                if(isRemoved() || display.equals(this) || this.getFacing() != display.getFacing()) continue;

                String thisImageID = this.getImageID();
                String displayImageID = display.getImageID();

                if (!thisImageID.equals(displayImageID)) {
                    continue;
                }

                double x1 = this.getX(), x2 = display.getX();
                double y1 = this.getY(), y2 = display.getY();
                double z1 = this.getZ(), z2 = display.getZ();

                boolean sameHeight = this.getDisplayHeight() == display.getDisplayHeight();
                boolean sameWidth = this.getDisplayWidth() == display.getDisplayWidth();

                boolean isLeft = false, isRight = false, isAbove = false, isBelow = false;

                if(this.getFacing() == Direction.SOUTH || this.getFacing() == Direction.NORTH) {
                    isLeft = x2 < x1 && y2 == y1 && (Math.abs(x2 - x1) == 0 || Math.abs(x2 - x1) == display.getDisplayWidth() || Math.abs(x2 - x1) == this.getDisplayWidth()) && sameHeight;
                    isRight = x2 > x1 && y2 == y1 && (Math.abs(x2 - x1) == 0 || Math.abs(x2 - x1) == display.getDisplayWidth() || Math.abs(x2 - x1) == this.getDisplayWidth()) && sameHeight;
                    isAbove = y2 < y1 && x2 == x1 && (Math.abs(y2 - y1) == 0 || Math.abs(y2 - y1) == display.getDisplayHeight() || Math.abs(y2 - y1) == this.getDisplayHeight()) && sameWidth;
                    isBelow = y2 > y1 && x2 == x1 && (Math.abs(y2 - y1) == 0 || Math.abs(y2 - y1) == display.getDisplayHeight() || Math.abs(y2 - y1) == this.getDisplayHeight()) && sameWidth;
                } else if(this.getFacing() == Direction.EAST || this.getFacing() == Direction.WEST) {
                    isLeft = z2 < z1 && y2 == y1 && (Math.abs(z2 - z1) == 0 || Math.abs(z2 - z1) == display.getDisplayWidth() || Math.abs(z2 - z1) == this.getDisplayWidth()) && sameHeight;
                    isRight = z2 > z1 && y2 == y1 && (Math.abs(z2 - z1) == 0 || Math.abs(z2 - z1) == display.getDisplayWidth() || Math.abs(z2 - z1) == this.getDisplayWidth()) && sameHeight;
                    isAbove = y2 < y1 && z2 == z1 && (Math.abs(y2 - y1) == 0 || Math.abs(y2 - y1) == display.getDisplayHeight() || Math.abs(y2 - y1) == this.getDisplayHeight()) && sameWidth;
                    isBelow = y2 > y1 && z2 == z1 && (Math.abs(y2 - y1) == 0 || Math.abs(y2 - y1) == display.getDisplayHeight() || Math.abs(y2 - y1) == this.getDisplayHeight()) && sameWidth;
                }

                if (this.getFacing() == Direction.SOUTH || this.getFacing() == Direction.WEST) {
                    if(isRight) {
                        isRight = false;
                        isLeft = true;
                    } else if(isLeft) {
                        isRight = true;
                        isLeft = false;
                    }
                }

                if (isLeft || isRight || isAbove || isBelow) {
                    combineWithDisplay(display, nearbyEntities, isLeft, isRight, isAbove, isBelow);
                    break;
                }
            }
        }
    }

    private void combineWithDisplay(DisplayEntity display, List<DisplayEntity> nearbyEntities, boolean isLeft, boolean isRight, boolean isAbove, boolean isBelow) {
        kill();

        if (isLeft) {
            display.resize(Direction.WEST, this.getDisplayWidth());
        } else if (isRight) {
            display.resize(Direction.EAST, this.getDisplayWidth());
        } else if (isAbove) {
            display.resize(Direction.UP, this.getDisplayHeight());
        } else if (isBelow) {
            display.resize(Direction.DOWN, this.getDisplayHeight());
        }

        nearbyEntities.remove(display);
        for (DisplayEntity nearbyDisplay : nearbyEntities) {
            if (nearbyDisplay.isRemoved() || nearbyDisplay.equals(this)) {
                continue;
            }
            nearbyDisplay.combineDisplay();
        }
    }

    private Direction getResizeOffset() {
        return switch (getFacing()) {
            case WEST -> Direction.NORTH;
            case NORTH -> Direction.EAST;
            case SOUTH -> Direction.WEST;
            default -> Direction.SOUTH;
        };
    }

    public void removeFrame(Entity source) {
        if (!isRemoved() && !level().isClientSide) {
            onBroken(source);
            kill();
        }
    }

    public void onBroken(Entity entity) {
        if (!level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
            return;
        }
        if (entity instanceof Player player) {
            if (player.getAbilities().instabuild) {
                return;
            }
        }

        this.playSound(SoundEvents.ITEM_FRAME_BREAK, 1.0F, 1.0F);
        this.spawnAtLocation(new ItemStack(ModItems.DISPLAY.get(), this.getDisplayWidth() * this.getDisplayHeight()));
    }

    private void updateBoundingBox() {
        Direction facing = getFacing();

        if (facing.getAxis().isHorizontal()) {
            setXRot(0F);
            setYRot(facing.get2DDataValue() * 90F);
        } else {
            setXRot(-90F * facing.getAxisDirection().getStep());
            setYRot(0F);
        }

        xRotO = getXRot();
        yRotO = getYRot();

        setBoundingBox(makeBoundingBox());
    }

    @Override
    public ItemStack getPickedResult(HitResult target) {
        return new ItemStack(ModItems.DISPLAY.get());
    }

    @Override
    protected boolean repositionEntityAfterLoad() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    public void playPlacementSound() {
        this.playSound(SoundEvents.ITEM_FRAME_PLACE, 1.0F, 1.0F);
    }

    public void setImagePosition(BlockPos position) {
        moveTo(position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D, getYRot(), getXRot());
        updateBoundingBox();

        combineDisplay();
    }

    public String getImageID() {
        return entityData.get(ID);
    }

    public void setImageID(String id) {
        entityData.set(ID, id);
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
        updateBoundingBox();
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public boolean setDisplayWidth(int width) {
        width = Math.max(1, Math.min(width, MAX_WIDTH));
        int oldWidth = getDisplayWidth();
        entityData.set(WIDTH, width);
        return oldWidth != width;
    }

    public int getDisplayWidth() {
        return entityData.get(WIDTH);
    }

    public boolean setDisplayHeight(int height) {
        height = Math.max(1, Math.min(height, MAX_HEIGHT));
        int oldHeight = getDisplayHeight();
        entityData.set(HEIGHT, height);
        return oldHeight != height;
    }

    public int getDisplayHeight() {
        return entityData.get(HEIGHT);
    }

    public void setStretched(boolean stretched) {
        entityData.set(STRETCHED, stretched);
    }

    public boolean isStretched() {
        return entityData.get(STRETCHED);
    }

    public void setEditRestricted(boolean editRestricted) {
        entityData.set(EDIT_RESTRICTED, editRestricted);
    }

    public boolean isEditRestricted() {
        return entityData.get(EDIT_RESTRICTED);
    }

    public void setShowHitbox(boolean showHitbox) {
        entityData.set(SHOW_HITBOX, showHitbox);
    }

    public boolean isShowHitbox() {
        return entityData.get(SHOW_HITBOX);
    }

    public Optional<UUID> getOwner() {
        return entityData.get(OWNER);
    }

    public void setOwner(UUID owner) {
        entityData.set(OWNER, Optional.ofNullable(owner));
    }
}
