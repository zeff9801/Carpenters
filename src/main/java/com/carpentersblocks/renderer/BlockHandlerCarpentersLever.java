package com.carpentersblocks.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;

import com.carpentersblocks.block.BlockCarpentersLever;
import com.carpentersblocks.block.BlockCoverable;
import com.carpentersblocks.data.Lever;
import com.carpentersblocks.data.Lever.Axis;
import com.carpentersblocks.renderer.helper.VertexHelper;
import com.carpentersblocks.util.BlockProperties;
import com.carpentersblocks.util.registry.BlockRegistry;
import com.carpentersblocks.util.registry.IconRegistry;
import com.gtnewhorizons.angelica.interfaces.IThreadSafeISBRH;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockHandlerCarpentersLever extends BlockHandlerBase {

    private static final ThreadLocal<BlockHandlerCarpentersLever> threadRenderer = ThreadLocal
            .withInitial(BlockHandlerCarpentersLever::new);

    public IThreadSafeISBRH getThreadLocal() {
        return (IThreadSafeISBRH) threadRenderer.get();
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    @Override
    /**
     * Override to provide custom icons.
     */
    protected IIcon getUniqueIcon(ItemStack itemStack, int side, IIcon icon) {
        Block block = BlockProperties.toBlock(itemStack);

        if (block instanceof BlockCoverable) {
            return IconRegistry.icon_uncovered_solid;
        } else {
            return icon;
        }
    }

    @Override
    /**
     * Renders block
     */
    protected void renderCarpentersBlock(int x, int y, int z) {
        renderBlocks.renderAllFaces = true;
        renderLever(getCoverForRendering(), x, y, z);
        renderBlocks.renderAllFaces = false;
    }

    /**
     * Renders lever.
     */
    private void renderLever(ItemStack itemStack, int x, int y, int z) {
        /* Set block bounds and render lever base. */

        BlockCarpentersLever blockRef = (BlockCarpentersLever) BlockRegistry.blockCarpentersLever;
        blockRef.setBlockBoundsBasedOnState(renderBlocks.blockAccess, x, y, z);

        renderBlocks.setRenderBoundsFromBlock(blockRef);
        renderBlock(itemStack, x, y, z);

        /* Render lever handle. */

        renderLeverHandle(x, y, z);
    }

    /**
     * Renders the lever handle.
     */
    private void renderLeverHandle(int x, int y, int z) {
        Tessellator tessellator = Tessellator.instance;
        tessellator.setBrightness(Blocks.dirt.getMixedBrightnessForBlock(renderBlocks.blockAccess, x, y, z));
        tessellator.setColorOpaque_F(1.0F, 1.0F, 1.0F);

        Lever data = new Lever();
        ForgeDirection dir = data.getDirection(TE);
        boolean toggleState = data.getState(TE) == data.STATE_ON;
        boolean rotateLever = data.getAxis(TE) == Axis.X;

        IIcon icon = renderBlocks.hasOverrideBlockTexture() ? renderBlocks.overrideBlockTexture
                : IconRegistry.icon_lever;

        double uMin = icon.getMinU();
        double uMax = icon.getMaxU();
        double vMin = icon.getMinV();
        double vMax = icon.getMaxV();

        Vec3[] vector = { Vec3.createVectorHelper(-0.0625F, 0.0D, -0.0625F),
                Vec3.createVectorHelper(0.0625F, 0.0D, -0.0625F), Vec3.createVectorHelper(0.0625F, 0.0D, 0.0625F),
                Vec3.createVectorHelper(-0.0625F, 0.0D, 0.0625F), Vec3.createVectorHelper(-0.0625F, 0.625F, -0.0625F),
                Vec3.createVectorHelper(0.0625F, 0.625F, -0.0625F), Vec3.createVectorHelper(0.0625F, 0.625F, 0.0625F),
                Vec3.createVectorHelper(-0.0625F, 0.625F, 0.0625F) };

        /* Set up lever handle rotation. */

        for (int vecCount = 0; vecCount < 8; ++vecCount) {
            if (toggleState) {
                vector[vecCount].zCoord -= 0.0625D;
                vector[vecCount].rotateAroundX((float) Math.PI * 2F / 9F);
            } else {
                vector[vecCount].zCoord += 0.0625D;
                vector[vecCount].rotateAroundX(-((float) Math.PI * 2F / 9F));
            }

            if (dir.ordinal() < 2) {

                if (dir.equals(ForgeDirection.DOWN)) {
                    vector[vecCount].rotateAroundZ((float) Math.PI);
                }

                if (rotateLever) {
                    vector[vecCount].rotateAroundY((float) Math.PI / 2F);
                }

                if (dir.equals(ForgeDirection.UP)) {
                    vector[vecCount].xCoord += x + 0.5D;
                    vector[vecCount].yCoord += y + 0.125F;
                    vector[vecCount].zCoord += z + 0.5D;
                } else {
                    vector[vecCount].xCoord += x + 0.5D;
                    vector[vecCount].yCoord += y + 0.875F;
                    vector[vecCount].zCoord += z + 0.5D;
                }

            } else {

                vector[vecCount].yCoord -= 0.375D;
                vector[vecCount].rotateAroundX((float) Math.PI / 2F);

                switch (dir) {
                    case NORTH:
                        vector[vecCount].rotateAroundY(0.0F);
                        break;
                    case SOUTH:
                        vector[vecCount].rotateAroundY((float) Math.PI);
                        break;
                    case WEST:
                        vector[vecCount].rotateAroundY((float) Math.PI / 2F);
                        break;
                    case EAST:
                        vector[vecCount].rotateAroundY(-((float) Math.PI / 2F));
                        break;
                    default: {}
                }

                vector[vecCount].xCoord += x + 0.5D;
                vector[vecCount].yCoord += y + 0.5F;
                vector[vecCount].zCoord += z + 0.5D;
            }
        }

        Vec3 vertex1 = null;
        Vec3 vertex2 = null;
        Vec3 vertex3 = null;
        Vec3 vertex4 = null;

        for (int idx = 0; idx < 6; ++idx) {
            if (idx == 0) {
                uMin = icon.getInterpolatedU(7.0D);
                vMin = icon.getInterpolatedV(6.0D);
                uMax = icon.getInterpolatedU(9.0D);
                vMax = icon.getInterpolatedV(8.0D);
            } else if (idx == 2) {
                uMin = icon.getInterpolatedU(7.0D);
                vMin = icon.getInterpolatedV(6.0D);
                uMax = icon.getInterpolatedU(9.0D);
                vMax = icon.getMaxV();
            }

            switch (idx) {
                case 0:
                    vertex1 = vector[0];
                    vertex2 = vector[1];
                    vertex3 = vector[2];
                    vertex4 = vector[3];
                    break;
                case 1:
                    vertex1 = vector[7];
                    vertex2 = vector[6];
                    vertex3 = vector[5];
                    vertex4 = vector[4];
                    break;
                case 2:
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[1];
                    vertex2 = vector[0];
                    vertex3 = vector[4];
                    vertex4 = vector[5];
                    break;
                case 3:
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[2];
                    vertex2 = vector[1];
                    vertex3 = vector[5];
                    vertex4 = vector[6];
                    break;
                case 4:
                    tessellator.setColorOpaque_F(0.8F, 0.8F, 0.8F);
                    vertex1 = vector[3];
                    vertex2 = vector[2];
                    vertex3 = vector[6];
                    vertex4 = vector[7];
                    break;
                case 5:
                    tessellator.setColorOpaque_F(0.6F, 0.6F, 0.6F);
                    vertex1 = vector[0];
                    vertex2 = vector[3];
                    vertex3 = vector[7];
                    vertex4 = vector[4];
                    break;
            }

            final VertexHelper vertexHelper = VertexHelper.get();
            vertexHelper.drawVertex(renderBlocks, vertex1.xCoord, vertex1.yCoord, vertex1.zCoord, uMin, vMax);
            vertexHelper.drawVertex(renderBlocks, vertex2.xCoord, vertex2.yCoord, vertex2.zCoord, uMax, vMax);
            vertexHelper.drawVertex(renderBlocks, vertex3.xCoord, vertex3.yCoord, vertex3.zCoord, uMax, vMin);
            vertexHelper.drawVertex(renderBlocks, vertex4.xCoord, vertex4.yCoord, vertex4.zCoord, uMin, vMin);
        }
    }
}
