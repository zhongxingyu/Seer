 package com.five35.minecraft.fractalcrates.client;
 
 import com.five35.minecraft.fractalcrates.CrateTileEntity;
 import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
 import net.minecraft.block.Block;
 import net.minecraft.client.renderer.RenderBlocks;
 import net.minecraft.client.renderer.entity.RenderItem;
 import net.minecraft.client.renderer.entity.RenderManager;
 import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraftforge.client.IItemRenderer;
 import net.minecraftforge.common.ForgeDirection;
 import org.lwjgl.opengl.GL11;
 
 public class CrateRenderer extends TileEntitySpecialRenderer implements IItemRenderer, ISimpleBlockRenderingHandler {
 	private final EntityItem itemEntity = new EntityItem(null);
 
 	private final RenderItem itemRenderer = new RenderItem() {
 		@Override
 		public boolean shouldBob() {
 			return false;
 		}
 	};
 
 	protected final int renderId;
 
 	private static boolean renderBlock(final BlockRenderHelper helper) {
 		boolean rendered = false;
 
 		// full faces
 		rendered |= helper.renderFace(ForgeDirection.DOWN);
 		rendered |= helper.renderFace(ForgeDirection.NORTH);
 		rendered |= helper.renderFace(ForgeDirection.SOUTH);
 		rendered |= helper.renderFace(ForgeDirection.WEST);
 		rendered |= helper.renderFace(ForgeDirection.EAST);
 
 		// rim
 		rendered |= helper.renderQuad(ForgeDirection.UP, 0, 0, 0, 15, 1);
 		rendered |= helper.renderQuad(ForgeDirection.UP, 0, 0, 1, 1, 16);
 		rendered |= helper.renderQuad(ForgeDirection.UP, 0, 1, 15, 16, 16);
 		rendered |= helper.renderQuad(ForgeDirection.UP, 0, 15, 0, 16, 15);
 
 		// "bowl"
 		rendered |= helper.renderQuad(ForgeDirection.UP, 15, 1, 1, 15, 15);
 		rendered |= helper.renderQuad(ForgeDirection.NORTH, 15, 1, 0, 15, 15);
 		rendered |= helper.renderQuad(ForgeDirection.SOUTH, 15, 1, 0, 15, 15);
 		rendered |= helper.renderQuad(ForgeDirection.WEST, 15, 1, 0, 15, 15);
 		rendered |= helper.renderQuad(ForgeDirection.EAST, 15, 1, 0, 15, 15);
 
 		return rendered;
 	}
 
 	public CrateRenderer(final int renderId) {
 		this.renderId = renderId;
 
 		this.itemEntity.hoverStart = 0;
 		this.itemRenderer.setRenderManager(RenderManager.instance);
 	}
 
 	@Override
 	public int getRenderId() {
 		return this.renderId;
 	}
 
 	@Override
 	public boolean handleRenderType(final ItemStack item, final ItemRenderType type) {
 		return true;
 	}
 
 	@Override
 	public void renderInventoryBlock(final Block block, final int metadata, final int modelId, final RenderBlocks renderer) {
 		CrateRenderer.renderBlock(new InventoryBlockRenderHelper(block, metadata));
 	}
 
 	@Override
 	public void renderItem(final ItemRenderType type, final ItemStack stack, final Object... data) {
 		if (type == ItemRenderType.ENTITY) {
 			GL11.glPushMatrix();
 			GL11.glTranslated(-0.5, -0.5, -0.5);
 		}
 
 		CrateRenderer.renderBlock(new InventoryBlockRenderHelper(Block.blocksList[stack.itemID], stack.getItemDamage()));
 
 		if (stack.stackTagCompound != null && stack.stackTagCompound.hasKey("Contents")) {
 			final ItemStack contents = ItemStack.loadItemStackFromNBT(stack.stackTagCompound.getCompoundTag("Contents"));
 
 			contents.stackSize = 1;
 
 			GL11.glPushMatrix();
 			GL11.glTranslated(0.5, 0.5, 0.5);
 			GL11.glScaled(3.3, 3.3, 3.3);
 
 			this.itemEntity.setEntityItemStack(contents);
 			this.itemRenderer.doRenderItem(this.itemEntity, 0, 0, 0, 0, 0);
 
 			GL11.glPopMatrix();
 		}
 
 		if (type == ItemRenderType.ENTITY) {
 			GL11.glPopMatrix();
 		}
 	}
 
 	@Override
 	public void renderTileEntityAt(final TileEntity entity, final double x, final double y, final double z, final float time) {
 		if (!(entity instanceof CrateTileEntity)) {
 			return;
 		}
 
 		final CrateTileEntity crateEntity = (CrateTileEntity) entity;
 		final ItemStack stack = crateEntity.getStackInSlot(0);
 
 		if (stack == null || stack.stackSize == 0) {
 			return;
 		}
 
 		final ItemStack copy = stack.copy();
 		copy.stackSize = 1;
 
		this.itemEntity.setEntityItemStack(copy);

 		GL11.glPushMatrix();
 		GL11.glTranslated(x, y, z);
 
 		for (int i = 0; i < stack.stackSize; i++) {
 			GL11.glPushMatrix();
 
 			final int itemX = i & 3;
 			final int itemY = i >> 4;
 			final int itemZ = i >> 2 & 3;
 
 			// blocks render three "pixels" wide, with 2/5 of a pixel spacing between them
 			GL11.glTranslated(2.9 / 16 + itemX * 3.4 / 16, 2.9 / 16 + itemY * 3.4 / 16, 2.9 / 16 + itemZ * 3.4 / 16);
 			GL11.glScaled(0.75, 0.75, 0.75);
 
 			this.itemRenderer.doRenderItem(this.itemEntity, 0, 0, 0, 0, 0);
 
 			GL11.glPopMatrix();
 		}
 
 		GL11.glPopMatrix();
 	}
 
 	@Override
 	public boolean renderWorldBlock(final IBlockAccess world, final int x, final int y, final int z, final Block block, final int modelId, final RenderBlocks renderer) {
 		return CrateRenderer.renderBlock(new WorldBlockRenderHelper(world, x, y, z));
 	}
 
 	@Override
 	public boolean shouldRender3DInInventory() {
 		return true;
 	}
 
 	@Override
 	public boolean shouldUseRenderHelper(final ItemRenderType type, final ItemStack item, final ItemRendererHelper helper) {
 		return true;
 	}
 }
