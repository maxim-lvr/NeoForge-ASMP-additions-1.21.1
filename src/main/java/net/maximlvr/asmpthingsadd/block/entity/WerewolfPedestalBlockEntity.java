package net.maximlvr.asmpthingsadd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WerewolfPedestalBlockEntity extends BlockEntity {
    private static final String TAG_CAMOUFLAGE_STATE = "CamouflageState";
    private static final String TAG_DISPLAYED_ITEM = "DisplayedItem";

    private BlockState camouflageState = Blocks.OAK_PLANKS.defaultBlockState();
    private ItemStack displayedItem = ItemStack.EMPTY;

    public WerewolfPedestalBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.WEREWOLF_PEDESTAL.get(), pos, blockState);
    }

    public BlockState getCamouflageState() {
        return camouflageState;
    }

    public void setCamouflageState(BlockState camouflageState) {
        this.camouflageState = camouflageState;
        sync();
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public boolean hasDisplayedItem() {
        return !displayedItem.isEmpty();
    }

    public void setDisplayedItem(ItemStack stack) {
        if (stack.isEmpty()) {
            this.displayedItem = ItemStack.EMPTY;
        } else {
            this.displayedItem = stack.copyWithCount(1);
        }

        sync();
    }

    public ItemStack removeDisplayedItem() {
        ItemStack result = displayedItem.copy();
        this.displayedItem = ItemStack.EMPTY;
        sync();
        return result;
    }

    private void sync() {
        setChanged();

        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put(TAG_CAMOUFLAGE_STATE, NbtUtils.writeBlockState(camouflageState));

        if (!displayedItem.isEmpty()) {
            tag.put(TAG_DISPLAYED_ITEM, displayedItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains(TAG_CAMOUFLAGE_STATE)) {
            this.camouflageState = NbtUtils.readBlockState(
                    registries.lookupOrThrow(Registries.BLOCK),
                    tag.getCompound(TAG_CAMOUFLAGE_STATE)
            );
        }

        if (tag.contains(TAG_DISPLAYED_ITEM)) {
            this.displayedItem = ItemStack.parseOptional(
                    registries,
                    tag.getCompound(TAG_DISPLAYED_ITEM)
            );
        } else {
            this.displayedItem = ItemStack.EMPTY;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}