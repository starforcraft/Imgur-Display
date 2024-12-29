package com.ultramega.imgurdisplay.items;

import com.ultramega.imgurdisplay.entities.DisplayEntity;
import com.ultramega.imgurdisplay.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DisplayItem extends Item {
    public DisplayItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getClickedFace();
        BlockPos offset = pos.relative(facing);
        Player player = context.getPlayer();
        if (player != null && !canPlace(player, facing, context.getItemInHand(), offset)) {
            return InteractionResult.FAIL;
        }

        Level level = context.getLevel();

        DisplayEntity display = new DisplayEntity(ModEntities.DISPLAY.get(), level);
        display.setFacing(facing);
        display.setImagePosition(offset);
        if (player != null) display.setOwner(player.getUUID());

        display.playPlacementSound();
        if(!level.isClientSide && !display.isRemoved()) {
            level.addFreshEntity(display);
        }

        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }

    protected boolean canPlace(Player player, Direction facing, ItemStack stack, BlockPos pos) {
        return !facing.getAxis().isVertical() && player.mayUseItemAt(pos, facing, stack);
    }
}
