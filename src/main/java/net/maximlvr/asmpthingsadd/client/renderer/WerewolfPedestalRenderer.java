package net.maximlvr.asmpthingsadd.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.maximlvr.asmpthingsadd.block.entity.WerewolfPedestalBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WerewolfPedestalRenderer implements BlockEntityRenderer<WerewolfPedestalBlockEntity> {

    public WerewolfPedestalRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(WerewolfPedestalBlockEntity blockEntity,
                       float partialTick,
                       PoseStack poseStack,
                       MultiBufferSource bufferSource,
                       int packedLight,
                       int packedOverlay) {
        BlockState camouflageState = blockEntity.getCamouflageState();

        if (camouflageState == null || camouflageState.isAir()) {
            camouflageState = Blocks.OAK_PLANKS.defaultBlockState();
        }

        poseStack.pushPose();

        Minecraft.getInstance()
                .getBlockRenderer()
                .renderSingleBlock(
                        camouflageState,
                        poseStack,
                        bufferSource,
                        packedLight,
                        OverlayTexture.NO_OVERLAY
                );

        poseStack.popPose();

        renderDisplayedItem(blockEntity, partialTick, poseStack, bufferSource, packedLight);
    }

    private void renderDisplayedItem(WerewolfPedestalBlockEntity blockEntity,
                                     float partialTick,
                                     PoseStack poseStack,
                                     MultiBufferSource bufferSource,
                                     int packedLight) {
        ItemStack displayedItem = blockEntity.getDisplayedItem();

        if (displayedItem.isEmpty()) {
            return;
        }

        poseStack.pushPose();

        // Centre du bloc, posé juste au-dessus de la surface.
        poseStack.translate(0.5D, 1.025D, 0.5D);

        // Pose l'item à plat sur le dessus du bloc.
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));

        // Taille de l'item posé.
        poseStack.scale(0.75F, 0.75F, 0.75F);

        Minecraft.getInstance()
                .getItemRenderer()
                .renderStatic(
                        displayedItem,
                        ItemDisplayContext.GROUND,
                        packedLight,
                        OverlayTexture.NO_OVERLAY,
                        poseStack,
                        bufferSource,
                        blockEntity.getLevel(),
                        0
                );

        poseStack.popPose();
    }
}