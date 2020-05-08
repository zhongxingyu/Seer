 package com.qzx.au.extras;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 import net.minecraft.block.Block;
 import net.minecraft.block.material.Material;
 import net.minecraft.client.renderer.texture.IconRegister;
 import net.minecraft.entity.Entity;
 import net.minecraft.entity.EnumCreatureType;
 import net.minecraft.item.ItemBlock;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.Icon;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 
 import net.minecraftforge.common.ForgeDirection;
 
 import java.util.List;
 
 import com.qzx.au.core.BlockCoord;
 import com.qzx.au.core.IConnectedTexture;
 
 public class BlockColoredPane extends BlockColored {
 	@SideOnly(Side.CLIENT)
 	private Icon sidesIcon;
 	@SideOnly(Side.CLIENT)
	private Icon[][] blockIcons = null;
 
 	private Block parentBlock;
 	private boolean hasConnectedTextures;
 
 	public BlockColoredPane(int id, String name, String readableName, Class<? extends ItemBlock> itemblockclass, Material material, Block parentBlock, boolean hasConnectedTextures){
 		super(id, name, readableName, itemblockclass, material);
 		this.parentBlock = parentBlock;
 		this.hasConnectedTextures = hasConnectedTextures;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public void registerIcons(IconRegister iconRegister){
 		// block textures registered in BlockGlass
 
 		this.sidesIcon = iconRegister.registerIcon("au_extras:colorPaneSides");
 
 		// both tinted panes use special item icons for now
 		if(this.parentBlock == null){
 			this.blockIcons = new Icon[16][1];
 			for(int c = 0; c < 16; c++)
 				this.blockIcons[c][0] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "")+c);
 		} else if(this.parentBlock == AUExtras.blockGlassTinted || this.parentBlock == AUExtras.blockGlassTintedNoFrame){
 			this.blockIcons = new Icon[16][1];
 			for(int c = 0; c < 16; c++)
 				this.blockIcons[c][0] = iconRegister.registerIcon("au_extras:"+this.getUnlocalizedName().replace("tile.au.", "").replace("Pane", "")+c+"-item");
		}
 	}
 	private Icon[] getBlockIcons(int color){
 		// glass panes use their parnet icons
 		if(this.parentBlock instanceof BlockGlass) return ((BlockGlass)this.parentBlock).getBlockIcons(color);
 
 		return this.blockIcons[color];
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public Icon getIcon(int side, int color){
 		// both tinted panes use special item icons for now
 		if(this.blockIcons != null) return this.blockIcons[color][0];
 
 		return this.getBlockIcons(color)[this.hasConnectedTextures ? IConnectedTexture.ctm_default : 0];
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon getSidesIcon(){
 		return this.sidesIcon;
 	}
 
 /*
 	@SideOnly(Side.CLIENT)
 	public boolean isTinted(){
 		return (this.style != 0);
 	}
 	@SideOnly(Side.CLIENT)
 	public boolean isTintedWithFrame(){
 		return (this.style == 1);
 	}
 */
 
 	@Override
 	public boolean isOpaqueCube(){
 		return false;
 	}
 
 	@Override
 	public boolean renderAsNormalBlock(){
 		return false;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getRenderType(){
 		return ClientProxy.paneRenderType;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public int getRenderBlockPass(){
 		return 1;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean canRenderInPass(int pass){
 		ClientProxy.renderPass = pass;
 		return (pass == 1);
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public boolean shouldSideBeRendered(IBlockAccess access, int x, int y, int z, int side){
 		// coordinates are the block at each side, not the block being rendered
 
 		if(!(Block.blocksList[access.getBlockId(x, y, z)] instanceof BlockColoredPane))
 			return super.shouldSideBeRendered(access, x, y, z, side);
 
 		boolean thisVertical = (this.minY == 0.0F);
 		boolean neighborVertical = (this.canPaneConnectTo(access, x, y, z, ForgeDirection.UP) || this.canPaneConnectTo(access, x, y, z, ForgeDirection.DOWN));
 
 		return thisVertical != neighborVertical;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectSideTexturesVertical(int id, int metadata, int side, BlockCoord neighbor){
 		if(!this.hasConnectedTextures) return false; // no connected textures
 
 		int neighbor_id = neighbor.getBlockID();
 		Block neighbor_block = neighbor.getBlock();
 
 		// connect to same color of glass pane on sides
 		if(neighbor_id != id || neighbor.getBlockMetadata() != metadata) return false;
 
 		// don't connect to horizontal panes
 		if(!this.canPaneConnectTo(neighbor.access, neighbor.x, neighbor.y, neighbor.z, ForgeDirection.UP)
 		&& !this.canPaneConnectTo(neighbor.access, neighbor.x, neighbor.y, neighbor.z, ForgeDirection.DOWN))
 			return false;
 
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectCornerTexturesVertical(int id, int metadata, int side, BlockCoord diagonal){
 		if(!this.hasConnectedTextures) return false; // no connected textures
 
 		int diagonal_id = diagonal.getBlockID();
 		Block diagonal_block = diagonal.getBlock();
 
 		// connect to same color glass pane (corners)
 		if(diagonal_id != id || diagonal.getBlockMetadata() != metadata) return false;
 
 		// don't connect to horizontal panes
 		if(!this.canPaneConnectTo(diagonal.access, diagonal.x, diagonal.y, diagonal.z, ForgeDirection.UP)
 		&& !this.canPaneConnectTo(diagonal.access, diagonal.x, diagonal.y, diagonal.z, ForgeDirection.DOWN))
 			return false;
 
 		return true;
 	}
 
 	private int getPaneWidthOnSide(IBlockAccess access, int x, int y, int z, int side){
 		final float SIDE_N = 0.5F - BlockCoord.ADD_1_16;
 		final float SIDE_P = 0.5F + BlockCoord.ADD_1_16;
 		boolean connect_d = this.canPaneConnectTo(access, x, y, z, ForgeDirection.DOWN);
 		boolean connect_u = this.canPaneConnectTo(access, x, y, z, ForgeDirection.UP);
 
 		if(!connect_d && !connect_u){
 			return 0;
 		} else {
 			float minX = SIDE_N, maxX = SIDE_P;
 			float minZ = SIDE_N, maxZ = SIDE_P;
 			boolean connect_n = this.canPaneConnectTo(access, x, y, z, ForgeDirection.NORTH);
 			boolean connect_s = this.canPaneConnectTo(access, x, y, z, ForgeDirection.SOUTH);
 			boolean connect_w = this.canPaneConnectTo(access, x, y, z, ForgeDirection.WEST);
 			boolean connect_e = this.canPaneConnectTo(access, x, y, z, ForgeDirection.EAST);
 
 			if((!connect_w || !connect_e) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_w && !connect_e)
 					minX = 0.0F;
 				else if(!connect_w && connect_e)
 					maxX = 1.0F;
 			} else {
 				minX = 0.0F;
 				maxX = 1.0F;
 			}
 
 			if((!connect_n || !connect_s) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_n && !connect_s)
 					minZ = 0.0F;
 				else if(!connect_n && connect_s)
 					maxZ = 1.0F;
 			} else {
 				minZ = 0.0F;
 				maxZ = 1.0F;
 			}
 
 			return (side == 2 ? (maxX == 1.0F ? 2 : 0) | (minX == 0.0F ? 1 : 0) : (minZ == 0.0F ? 2 : 0) | (maxZ == 1.0F ? 1 : 0));
 		}
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon[] getBlockTextureVertical(IBlockAccess access, int x, int y, int z, int side){
 		BlockCoord coord = new BlockCoord(access, x, y, z);
 		int blockID = coord.getBlockID();
 		int blockColor = coord.getBlockMetadata();
 
 		Icon[] halves = new Icon[2];
 
 		if(!this.hasConnectedTextures){
 			halves[0] = this.getBlockIcons(blockColor)[0];
 			halves[1] = this.getBlockIcons(blockColor)[0];
 			return halves; // no connected textures
 		}
 
 		BlockCoord u = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.UP);
 		BlockCoord d = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.DOWN);
 		BlockCoord l = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.LEFT);
 		BlockCoord r = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.RIGHT);
 		BlockCoord ul = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.LEFT);
 		BlockCoord ur = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.RIGHT);
 		BlockCoord dl = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.LEFT);
 		BlockCoord dr = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.RIGHT);
 		boolean connect_t = this.canConnectSideTexturesVertical(blockID, blockColor, side, u);
 		boolean connect_r = this.canConnectSideTexturesVertical(blockID, blockColor, side, r);
 		boolean connect_b = this.canConnectSideTexturesVertical(blockID, blockColor, side, d);
 		boolean connect_l = this.canConnectSideTexturesVertical(blockID, blockColor, side, l);
 		boolean connect_tl = this.canConnectCornerTexturesVertical(blockID, blockColor, side, ul);
 		boolean connect_tr = this.canConnectCornerTexturesVertical(blockID, blockColor, side, ur);
 		boolean connect_bl = this.canConnectCornerTexturesVertical(blockID, blockColor, side, dl);
 		boolean connect_br = this.canConnectCornerTexturesVertical(blockID, blockColor, side, dr);
 		int panes_t = connect_t ? this.getPaneWidthOnSide(access, u.x, u.y, u.z, side) : 0;
 		connect_r = connect_r ? this.getPaneWidthOnSide(access, r.x, r.y, r.z, side) != 0 : false;
 		int panes_b = connect_b ? this.getPaneWidthOnSide(access, d.x, d.y, d.z, side) : 0;
 		connect_l = connect_l ? this.getPaneWidthOnSide(access, l.x, l.y, l.z, side) != 0 : false;
 		int panes_tl = connect_tl ? this.getPaneWidthOnSide(access, ul.x, ul.y, ul.z, side) : 0;
 		int panes_tr = connect_tr ? this.getPaneWidthOnSide(access, ur.x, ur.y, ur.z, side) : 0;
 		int panes_bl = connect_bl ? this.getPaneWidthOnSide(access, dl.x, dl.y, dl.z, side) : 0;
 		int panes_br = connect_br ? this.getPaneWidthOnSide(access, dr.x, dr.y, dr.z, side) : 0;
 		boolean pane_l = (side == 2 ? this.maxX == 1.0F : this.minZ == 0.0F);
 		boolean pane_r = (side == 2 ? this.minX == 0.0F : this.maxZ == 1.0F);
 
 		// disconnect T-panes
 		boolean connect_panes = true;
 		if(pane_l && pane_r)
 			if((side == 4 ? this.maxX == 1.0F : this.minZ == 0.0F) || (side == 4 ? this.minX == 0.0F : this.maxZ == 1.0F))
 				connect_panes = false;
 
 		// left pane
 		int textureL = 0;
 		if((panes_t&2) != 2) textureL |= 1<<0;								// T
 			else if(connect_l && (panes_tl&1) != 1) textureL |= 2<<0;		// tl
 		if(!pane_r || !connect_panes) textureL |= 1<<2;						// R
 			else if((panes_t&2) == 2 && (panes_t&1) != 1) textureL |= 2<<2;	// tr
 		if((panes_b&2) != 2) textureL |= 1<<4;								// B
 			else if(pane_r && (panes_b&1) != 1) textureL |= 2<<4;			// br
 		if(!connect_l) textureL |= 1<<6;									// L
 			else if((panes_b&2) == 2 && (panes_bl&1) != 1) textureL |= 2<<6;// bl
 		halves[0] = this.getBlockIcons(blockColor)[textureL];
 
 		// right pane
 		int textureR = 0;
 		if((panes_t&1) != 1) textureR |= 1<<0;								// T
 			else if(pane_l && (panes_t&2) != 2) textureR |= 2<<0;			// tl
 		if(!connect_r) textureR |= 1<<2;									// R
 			else if((panes_t&1) == 1 && (panes_tr&2) != 2) textureR |= 2<<2;// tr
 		if((panes_b&1) != 1) textureR |= 1<<4;								// B
 			else if(connect_r && (panes_br&2) != 2) textureR |= 2<<4;		// br
 		if(!pane_l || !connect_panes) textureR |= 1<<6;						// L
 			else if((panes_b&1) == 1 && (panes_b&2) != 2) textureR |= 2<<6;	// bl
 		halves[1] = this.getBlockIcons(blockColor)[textureR];
 
 		return halves;
 	}
 
 	//////////
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectSideTexturesHorizontal(int id, int metadata, int side, BlockCoord neighbor){
 		if(!this.hasConnectedTextures) return false; // no connected textures
 
 		int neighbor_id = neighbor.getBlockID();
 		Block neighbor_block = neighbor.getBlock();
 
 		// connect to same color of glass pane on sides
 		if(neighbor_id != id || neighbor.getBlockMetadata() != metadata) return false;
 
 		// don't connect to vertical panes
 		if(this.canPaneConnectTo(neighbor.access, neighbor.x, neighbor.y, neighbor.z, ForgeDirection.UP)
 		|| this.canPaneConnectTo(neighbor.access, neighbor.x, neighbor.y, neighbor.z, ForgeDirection.DOWN))
 			return false;
 
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public boolean canConnectCornerTexturesHorizontal(int id, int metadata, int side, BlockCoord diagonal){
 		if(!this.hasConnectedTextures) return false; // no connected textures
 
 		int diagonal_id = diagonal.getBlockID();
 		Block diagonal_block = diagonal.getBlock();
 
 		// connect to same color glass pane (corners)
 		if(diagonal_id != id || diagonal.getBlockMetadata() != metadata) return false;
 
 		// don't connect to vertical panes
 		if(this.canPaneConnectTo(diagonal.access, diagonal.x, diagonal.y, diagonal.z, ForgeDirection.UP)
 		|| this.canPaneConnectTo(diagonal.access, diagonal.x, diagonal.y, diagonal.z, ForgeDirection.DOWN))
 			return false;
 
 		return true;
 	}
 
 	@SideOnly(Side.CLIENT)
 	public Icon getBlockTextureHorizontal(IBlockAccess access, int x, int y, int z, int side){
 		BlockCoord coord = new BlockCoord(access, x, y, z);
 		int blockID = coord.getBlockID();
 		int blockColor = coord.getBlockMetadata();
 
 		if(!this.hasConnectedTextures) return this.getBlockIcons(blockColor)[0]; // no connected textures
 
 		BlockCoord u = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.UP);
 		BlockCoord d = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.DOWN);
 		BlockCoord l = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.LEFT);
 		BlockCoord r = (new BlockCoord(coord)).translateToSideAtDirection(side, BlockCoord.RIGHT);
 		BlockCoord ul = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.LEFT);
 		BlockCoord ur = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.UP, BlockCoord.RIGHT);
 		BlockCoord dl = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.LEFT);
 		BlockCoord dr = (new BlockCoord(coord)).translateToDiagonalAtDirection(side, BlockCoord.DOWN, BlockCoord.RIGHT);
 		boolean connect_t = this.canConnectSideTexturesHorizontal(blockID, blockColor, side, u);
 		boolean connect_r = this.canConnectSideTexturesHorizontal(blockID, blockColor, side, r);
 		boolean connect_b = this.canConnectSideTexturesHorizontal(blockID, blockColor, side, d);
 		boolean connect_l = this.canConnectSideTexturesHorizontal(blockID, blockColor, side, l);
 		boolean connect_tl = this.canConnectCornerTexturesHorizontal(blockID, blockColor, side, ul);
 		boolean connect_tr = this.canConnectCornerTexturesHorizontal(blockID, blockColor, side, ur);
 		boolean connect_bl = this.canConnectCornerTexturesHorizontal(blockID, blockColor, side, dl);
 		boolean connect_br = this.canConnectCornerTexturesHorizontal(blockID, blockColor, side, dr);
 
 		int texture = 0;
 		if(!connect_t) texture |= 1<<0;							// T
 			else if(connect_l && !connect_tl) texture |= 2<<0;	// tl
 		if(!connect_r) texture |= 1<<2;							// R
 			else if(connect_t && !connect_tr) texture |= 2<<2;	// tr
 		if(!connect_b) texture |= 1<<4;							// B
 			else if(connect_r && !connect_br) texture |= 2<<4;	// br
 		if(!connect_l) texture |= 1<<6;							// L
 			else if(connect_b && !connect_bl) texture |= 2<<6;	// bl
 
 		return this.getBlockIcons(blockColor)[texture];
 	}
 
 	//////////
 
 	// modified from net.minecraft.block.BlockPane
 	@Override
 	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axisAlignedBB, List list, Entity entity){
 		final float SIDE_N = 0.5F - BlockCoord.ADD_1_16;
 		final float SIDE_P = 0.5F + BlockCoord.ADD_1_16;
 		boolean connect_d = this.canPaneConnectTo(world, x, y, z, ForgeDirection.DOWN);
 		boolean connect_u = this.canPaneConnectTo(world, x, y, z, ForgeDirection.UP);
 
 		if(!connect_d && !connect_u){
 			this.setBlockBounds(0.0F, SIDE_N, 0.0F, 1.0F, SIDE_P, 1.0F);
 			super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 		} else {
 			boolean connect_n = this.canPaneConnectTo(world, x, y, z, ForgeDirection.NORTH);
 			boolean connect_s = this.canPaneConnectTo(world, x, y, z, ForgeDirection.SOUTH);
 			boolean connect_w = this.canPaneConnectTo(world, x, y, z, ForgeDirection.WEST);
 			boolean connect_e = this.canPaneConnectTo(world, x, y, z, ForgeDirection.EAST);
 
 			if((!connect_w || !connect_e) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_w && !connect_e){
 					this.setBlockBounds(0.0F, 0.0F, SIDE_N, 0.5F, 1.0F, SIDE_P);
 					super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 				} else if(!connect_w && connect_e){
 					this.setBlockBounds(0.5F, 0.0F, SIDE_N, 1.0F, 1.0F, SIDE_P);
 					super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 				}
 			} else {
 				this.setBlockBounds(0.0F, 0.0F, SIDE_N, 1.0F, 1.0F, SIDE_P);
 				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 			}
 
 			if((!connect_n || !connect_s) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_n && !connect_s){
 					this.setBlockBounds(SIDE_N, 0.0F, 0.0F, SIDE_P, 1.0F, 0.5F);
 					super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 				} else if(!connect_n && connect_s){
 					this.setBlockBounds(SIDE_N, 0.0F, 0.5F, SIDE_P, 1.0F, 1.0F);
 					super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 				}
 			} else {
 				this.setBlockBounds(SIDE_N, 0.0F, 0.0F, SIDE_P, 1.0F, 1.0F);
 				super.addCollisionBoxesToList(world, x, y, z, axisAlignedBB, list, entity);
 			}
 		}
 	}
 
 	// modified from net.minecraft.block.BlockPane
 	@Override
 	public void setBlockBoundsBasedOnState(IBlockAccess access, int x, int y, int z){
 		final float SIDE_N = 0.5F - BlockCoord.ADD_1_16;
 		final float SIDE_P = 0.5F + BlockCoord.ADD_1_16;
 		boolean connect_d = this.canPaneConnectTo(access, x, y, z, ForgeDirection.DOWN);
 		boolean connect_u = this.canPaneConnectTo(access, x, y, z, ForgeDirection.UP);
 
 		if(!connect_d && !connect_u){
 			this.setBlockBounds(0.0F, SIDE_N, 0.0F, 1.0F, SIDE_P, 1.0F);
 		} else {
 			float minX = SIDE_N, maxX = SIDE_P;
 			float minZ = SIDE_N, maxZ = SIDE_P;
 			boolean connect_n = this.canPaneConnectTo(access, x, y, z, ForgeDirection.NORTH);
 			boolean connect_s = this.canPaneConnectTo(access, x, y, z, ForgeDirection.SOUTH);
 			boolean connect_w = this.canPaneConnectTo(access, x, y, z, ForgeDirection.WEST);
 			boolean connect_e = this.canPaneConnectTo(access, x, y, z, ForgeDirection.EAST);
 
 			if((!connect_w || !connect_e) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_w && !connect_e)
 					minX = 0.0F;
 				else if(!connect_w && connect_e)
 					maxX = 1.0F;
 			} else {
 				minX = 0.0F;
 				maxX = 1.0F;
 			}
 
 			if((!connect_n || !connect_s) && (connect_w || connect_e || connect_n || connect_s)){
 				if(connect_n && !connect_s)
 					minZ = 0.0F;
 				else if(!connect_n && connect_s)
 					maxZ = 1.0F;
 			} else {
 				minZ = 0.0F;
 				maxZ = 1.0F;
 			}
 
 			this.setBlockBounds(minX, 0.0F, minZ, maxX, 1.0F, maxZ);
 		}
 	}
 
 	private boolean canPaneConnectTo(IBlockAccess access, int x, int y, int z, ForgeDirection direction){
 		int neighbor_id = access.getBlockId(x+direction.offsetX, y+direction.offsetY, z+direction.offsetZ);
 		if(access.isBlockSolidOnSide(x+direction.offsetX, y+direction.offsetY, z+direction.offsetZ, direction.getOpposite(), false) || Block.opaqueCubeLookup[neighbor_id])
 			return true;
 		if(!(Block.blocksList[neighbor_id] instanceof BlockColoredPane)) return false;
 
 		// vertical panes can't connect to horizontal panes
 		if(direction != ForgeDirection.DOWN && direction != ForgeDirection.UP)
 			if(this.canPaneConnectTo(access, x, y, z, ForgeDirection.UP) || this.canPaneConnectTo(access, x, y, z, ForgeDirection.DOWN))
 				if(!this.canPaneConnectTo(access, x+direction.offsetX, y, z+direction.offsetZ, ForgeDirection.UP)
 				&& !this.canPaneConnectTo(access, x+direction.offsetX, y, z+direction.offsetZ, ForgeDirection.DOWN))
 					return false;
 		return true;
 	}
 
 	//////////
 
 	@Override
 	public boolean canCreatureSpawn(EnumCreatureType type, World world, int x, int y, int z){
 		return false;
 	}
 }
