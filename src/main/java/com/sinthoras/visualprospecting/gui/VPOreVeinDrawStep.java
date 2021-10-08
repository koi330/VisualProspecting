package com.sinthoras.visualprospecting.gui;

import com.sinthoras.visualprospecting.VP;
import com.sinthoras.visualprospecting.VPConfig;
import com.sinthoras.visualprospecting.VPUtils;
import com.sinthoras.visualprospecting.database.veintypes.VPVeinType;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import journeymap.client.cartography.RGB;
import journeymap.client.forge.helper.ForgeHelper;
import journeymap.client.forge.helper.IRenderHelper;
import journeymap.client.render.draw.DrawStep;
import journeymap.client.render.draw.DrawUtil;
import journeymap.client.render.map.GridRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

import java.awt.geom.Point2D;

public class VPOreVeinDrawStep implements DrawStep {

    private final int blockX;
    private final int blockZ;
    private final VPVeinType veinType;


    public VPOreVeinDrawStep(final VPVeinType veinType, int chunkX, int chunkZ) {
        blockX = VPUtils.coordChunkToBlock(chunkX) + 8;
        blockZ = VPUtils.coordChunkToBlock(chunkZ) + 8;
        this.veinType = veinType;
    }

    @Override
    public void draw(double xOffset, double yOffset, GridRenderer gridRenderer, float drawScale, double fontScale, double rotation) {
        if(gridRenderer.getZoom() >= VPConfig.minZoomLevel) {
            Materials aMaterial = GregTech_API.sGeneratedMaterials[veinType.primaryOreMeta];

            final double textureSize = 32 * fontScale;
            final double textureSizeHalf = textureSize / 2;

            final Point2D.Double pixel = gridRenderer.getBlockPixelInGrid(blockX, blockZ);
            DrawUtil.drawLabel(veinType.getNameReadable() + " Vein", pixel.getX() + xOffset, pixel.getY() + yOffset - textureSize, DrawUtil.HAlign.Center, DrawUtil.VAlign.Middle, 0, 180, 0x00FFFFFF, 255, fontScale, false, rotation);

            final IIcon blockIcon = Blocks.stone.getIcon(0, 0);
            drawQuad(blockIcon, pixel.getX() - textureSizeHalf + xOffset, pixel.getY() - textureSizeHalf + yOffset, textureSize, textureSize, 0.0, 0xFFFFFF, 255, false, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);

            final int color = (aMaterial.mRGBa[0] << 16) | (aMaterial.mRGBa[1]) << 8 | aMaterial.mRGBa[2];
            final IIcon oreIcon = aMaterial.mIconSet.mTextures[OrePrefixes.ore.mTextureIndex].getIcon();
            drawQuad(oreIcon, pixel.getX() - textureSizeHalf + xOffset, pixel.getY() - textureSizeHalf + yOffset, textureSize, textureSize, 0.0, color, 255, true, GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, false);
        }
    }

    public static void drawQuad(IIcon icon, double x, double y, double width, double height, double rotation, Integer color, float alpha, boolean blend, int glBlendSfactor, int glBlendDFactor, boolean clampTexture) {
        IRenderHelper renderHelper = ForgeHelper.INSTANCE.getRenderHelper();

        GL11.glPushMatrix();

        try {
            if (blend) {
                renderHelper.glEnableBlend();
                renderHelper.glBlendFunc(glBlendSfactor, glBlendDFactor, 1, 0);
            }

            renderHelper.glEnableTexture2D();
            Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
            if (blend && color != null) {
                float[] c = RGB.floats(color);
                renderHelper.glColor4f(c[0], c[1], c[2], alpha);
            } else {
                renderHelper.glColor4f(1.0F, 1.0F, 1.0F, alpha);
            }

            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            int texEdgeBehavior = clampTexture ? GL11.GL_NEAREST : GL11.GL_REPEAT;
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, texEdgeBehavior);
            renderHelper.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, texEdgeBehavior);
            if (rotation != 0.0D) {
                double transX = x + width / 2.0D;
                double transY = y + height / 2.0D;
                GL11.glTranslated(transX, transY, 0.0D);
                GL11.glRotated(rotation, 0.0D, 0.0D, 1.0D);
                GL11.glTranslated(-transX, -transY, 0.0D);
            }
            renderHelper.startDrawingQuads(false);
            renderHelper.addVertexWithUV(x, y + height, DrawUtil.zLevel, icon.getMinU(), icon.getMaxV());
            renderHelper.addVertexWithUV(x + width, y + height, DrawUtil.zLevel, icon.getMaxU(), icon.getMaxV());
            renderHelper.addVertexWithUV(x + width, y, DrawUtil.zLevel, icon.getMaxU(), icon.getMinV());
            renderHelper.addVertexWithUV(x, y, DrawUtil.zLevel, icon.getMinU(), icon.getMinV());
            renderHelper.draw();
            if (blend) {
                renderHelper.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                if (glBlendSfactor != GL11.GL_SRC_ALPHA || glBlendDFactor != GL11.GL_ONE_MINUS_SRC_ALPHA) {
                    renderHelper.glEnableBlend();
                    renderHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
                }
            }
        } finally {
            GL11.glPopMatrix();
        }

    }
}
