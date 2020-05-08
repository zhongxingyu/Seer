 /*******************************************************************************
  * @author Reika Kalseki
  * 
  * Copyright 2014
  * 
  * All rights reserved.
  * Distribution of the software in any form is only allowed with
  * explicit, prior permission from the owner.
  ******************************************************************************/
 package Reika.DyeTrees.Blocks;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Random;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.creativetab.CreativeTabs;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.item.Item;
 import net.minecraft.item.ItemStack;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraft.world.biome.BiomeGenBase;
 import net.minecraftforge.common.ForgeDirection;
 import Reika.DragonAPI.ModList;
 import Reika.DragonAPI.Base.BlockCustomLeaf;
 import Reika.DragonAPI.Libraries.Java.ReikaRandomHelper;
 import Reika.DragonAPI.Libraries.MathSci.ReikaMathLibrary;
 import Reika.DragonAPI.Libraries.World.ReikaChunkHelper;
 import Reika.DragonAPI.Libraries.World.ReikaWorldHelper;
 import Reika.DragonAPI.ModInteract.ReikaMystcraftHelper;
 import Reika.DragonAPI.ModInteract.ThaumBlockHandler;
 import Reika.DyeTrees.DyeTrees;
 import Reika.DyeTrees.TileEntityRainbowBeacon;
 import Reika.DyeTrees.Registry.DyeBlocks;
 import Reika.DyeTrees.Registry.DyeItems;
 import Reika.DyeTrees.Registry.DyeOptions;
 
 import com.xcompwiz.mystcraft.api.MystObjects;
 
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class BlockRainbowLeaf extends BlockCustomLeaf {
 
 	private static final boolean TILE = false;
 
 	public BlockRainbowLeaf(int par1) {
 		super(par1);
 	}
 
 	@Override
 	public boolean decays() {
 		return true;
 	}
 
 	@Override
 	public boolean showInCreative() {
 		return true;
 	}
 
 	@Override
 	public CreativeTabs getCreativeTab() {
 		return DyeTrees.dyeTreeTab;
 	}
 
 	@Override
 	@SideOnly(Side.CLIENT)
 	public final int getRenderColor(int dmg)
 	{
 		//return Color.HSBtoRGB(((System.currentTimeMillis()/60)%360)/360F, 0.8F, 1);
 		World world = Minecraft.getMinecraft().theWorld;
 		EntityPlayer ep = Minecraft.getMinecraft().thePlayer;
 		int x = MathHelper.floor_double(ep.posX);
 		int y = MathHelper.floor_double(ep.posY+ep.getEyeHeight());
 		int z = MathHelper.floor_double(ep.posZ);
 		return this.colorMultiplier(world, x, y, z);
 	}
 
 	@Override
 	public final int colorMultiplier(IBlockAccess iba, int x, int y, int z)
 	{
 		int sc = 32;
 		float hue = (float)(ReikaMathLibrary.py3d(x, y*3, z+x)%sc)/sc;
 		return Color.HSBtoRGB(hue, 0.7F, 1F);
 	}
 
 	@Override
 	public final int idDropped(int id, Random r, int fortune)
 	{
 		return DyeBlocks.RAINBOWSAPLING.getBlockID();
 	}
 
 	@Override
 	public int damageDropped(int dmg)
 	{
 		return 0;
 	}
 
 	@Override
 	public final void dropBlockAsItemWithChance(World world, int x, int y, int z, int metadata, float chance, int fortune)
 	{
 		if (!world.isRemote) {
 			float saplingChance = 0.0125F;
 			float appleChance = 0.1F;
 			float goldAppleChance = 0.025F;
 			float rareGoldAppleChance = 0.0025F;
 
 			saplingChance *= (1+fortune);
 			appleChance *= (1+fortune*5);
 			goldAppleChance *= (1+fortune*3);
 			rareGoldAppleChance *= (1+fortune*3);
 
 			if (ReikaRandomHelper.doWithChance(saplingChance))
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(DyeBlocks.RAINBOWSAPLING.getBlockID(), 1, metadata));
 			if (ReikaRandomHelper.doWithChance(appleChance))
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Item.appleRed, 1, 0));
 			if (ReikaRandomHelper.doWithChance(goldAppleChance))
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Item.appleGold, 1, 0));
 			if (ReikaRandomHelper.doWithChance(rareGoldAppleChance))
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Item.appleGold, 1, 1));
 			this.dropDye(world, x, y, z, fortune);
 		}
 	}
 
 	private final void dropDye(World world, int x, int y, int z, int fortune) {
 		int drop = this.getDyeDropCount(fortune);
 		for (int i = 0; i < drop; i++) {
 			if (ReikaRandomHelper.doWithChance(DyeOptions.DYEFRAC.getValue())) {
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(Item.dyePowder.itemID, 1, rand.nextInt(16)));
 			}
 			else {
 				this.dropBlockAsItem_do(world, x, y, z, new ItemStack(DyeItems.DYE.getShiftedItemID(), 1, rand.nextInt(16)));
 			}
 		}
 	}
 
 	private int getDyeDropCount(int fortune) {
 		return 1+rand.nextInt(3*(1+fortune))+fortune+rand.nextInt(1+fortune*fortune);
 	}
 
 	@Override
 	public final ArrayList<ItemStack> onSheared(ItemStack item, World world, int x, int y, int z, int fortune)
 	{
 		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
 		ret.add(new ItemStack(DyeBlocks.RAINBOW.getBlockID(), 1, 1));
 		return ret;
 	}
 
 	@Override
 	protected final ItemStack createStackedBlock(int par1)
 	{
 		return new ItemStack(DyeBlocks.RAINBOW.getBlockID(), 1, 1);
 	}
 
 	@Override
 	public final void randomDisplayTick(World world, int x, int y, int z, Random rand) {
 		int color = this.colorMultiplier(world, x, y, z);
 		Color c = new Color(color);
 		float r = c.getRed()/255F;
 		float g = c.getGreen()/255F;
 		float b = c.getBlue()/255F;
 		world.spawnParticle("reddust", x+rand.nextDouble(), y, z+rand.nextDouble(), r, g, b);
 	}
 
 	@Override
 	public final int idPicked(World par1World, int par2, int par3, int par4)
 	{
 		return DyeBlocks.RAINBOW.getBlockID();
 	}
 
 	@Override
 	public String getFastGraphicsIcon(int meta) {
 		return "DyeTrees:leaves_opaque";
 	}
 
 	@Override
 	public String getFancyGraphicsIcon(int meta) {
 		return "DyeTrees:leaves";
 	}
 
 	@Override
 	public boolean shouldTryDecay(World world, int x, int y, int z, int meta) {
 		return meta%2 == 0;
 	}
 
 	@Override
 	public int getFlammability(IBlockAccess world, int x, int y, int z, int metadata, ForgeDirection face)
 	{
 		return 90;
 	}
 
 	@Override
 	public int getFireSpreadSpeed(World world, int x, int y, int z, int metadata, ForgeDirection face)
 	{
 		return 180;
 	}
 
 	@Override
 	public boolean hasTileEntity(int meta) {
 		return TILE ? meta == 2 || meta == 3 : false;
 	}
 
 	@Override
 	public TileEntity createTileEntity(World world, int meta) {
 		if (TILE) {
 			if (meta == 2 || meta == 3) {
 				return new TileEntityRainbowBeacon();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected void onRandomUpdate(World world, int x, int y, int z, Random r) {/*
 		if (r.nextInt(20) == 0)
 			this.dropDye(world, x, y, z, 0);
 
 		if (!world.isRemote && r.nextInt(400) == 0 && DyeOptions.RAINBOWSPREAD.getState()) {
 			int rx = ReikaRandomHelper.getRandomPlusMinus(x, 32);
 			int rz = ReikaRandomHelper.getRandomPlusMinus(z, 32);
 			ReikaWorldHelper.setBiomeForXZ(world, rx, rz, DyeTrees.forest);
 			ReikaJavaLibrary.pConsole(rx+", "+rz);
 			for (int i = 0; i < 256; i++) {
 				ReikaWorldHelper.temperatureEnvironment(world, rx, i, rz, ReikaWorldHelper.getBiomeTemp(DyeTrees.forest));
 				world.markBlockForRenderUpdate(rx, i, rz);
 				world.markBlockForUpdate(rx, i, rz);
 			}
 		}*/
 
 		if (!world.isRemote) {
 			if (ModList.THAUMCRAFT.isLoaded()) {
 				if (rand.nextInt(25) == 0)
 					this.fightTaint(world, x, y, z);
 				if (rand.nextInt(20) == 0)
 					this.fightEerie(world, x, y, z);
 			}
 			if (ModList.MYSTCRAFT.isLoaded() && ReikaMystcraftHelper.isMystAge(world)) {
 				if (rand.nextInt(20) == 0)
 					this.fightInstability(world, x, y, z);
 				if (rand.nextInt(10) == 0)
 					this.fightDecay(world, x, y, z);
 			}
 		}
 	}
 
 	private void fightDecay(World world, int x, int y, int z) {
 		if (MystObjects.decay != null) {
 			int rx = ReikaRandomHelper.getRandomPlusMinus(x, 64);
 			int rz = ReikaRandomHelper.getRandomPlusMinus(z, 64);
 			ReikaChunkHelper.removeBlocksFromChunk(world, rx, rz, MystObjects.decay.blockID, -1);
 		}
 	}
 
 	private void fightInstability(World world, int x, int y, int z) {
 		if (ReikaMystcraftHelper.loadedCorrectly) {
 			if (ReikaMystcraftHelper.getBonusInstabilityForAge(world) > 0) {
 				ReikaMystcraftHelper.addBonusInstabilityForAge(world, -1);
 				//ReikaJavaLibrary.pConsole("bon: "+ReikaMystcraftHelper.getBonusInstabilityForAge(world));
 			}
 			else if (ReikaMystcraftHelper.getBaseInstabilityForAge(world) > 0) {
 				ReikaMystcraftHelper.addBaseInstabilityForAge(world, (short)-1);
 				//ReikaJavaLibrary.pConsole("base: "+ReikaMystcraftHelper.getBaseInstabilityForAge(world));
 			}
 			else {
 				ReikaMystcraftHelper.addStabilityForAge(world, 1);
 				//ReikaJavaLibrary.pConsole("sta: "+ReikaMystcraftHelper.getStabilityForAge(world));
 			}
 		}
 	}
 
 	private void fightEerie(World world, int x, int y, int z) {
 		int rx = ReikaRandomHelper.getRandomPlusMinus(x, 32);
 		int rz = ReikaRandomHelper.getRandomPlusMinus(z, 32);
 
 		int r = 3;
 		for (int i = -r; i <= r; i++) {
 			for (int k = -r; k <= r; k++) {
 				int dx = rx+i;
 				int dz = rz+k;
 				BiomeGenBase biome = world.getBiomeGenForCoords(dx, dz);
 				int id = biome.biomeID;
 				if (id == ThaumBlockHandler.getInstance().eerieBiomeID) {
 					BiomeGenBase[] biomes = new BiomeGenBase[1];
 					biomes = world.getWorldChunkManager().loadBlockGeneratorData(biomes, dx, dz, 1, 1);
 					BiomeGenBase natural = biomes != null && biomes.length > 0 ? biomes[0] : null;
 					if (natural != null) {
 						ReikaWorldHelper.setBiomeForXZ(world, dx, dz, natural);
 					}
 				}
 			}
 		}
 	}
 
 	private void fightTaint(World world, int x, int y, int z) {
 		int rx = ReikaRandomHelper.getRandomPlusMinus(x, 32);
 		int rz = ReikaRandomHelper.getRandomPlusMinus(z, 32);
 
 		int r = 3;
 		for (int i = -r; i <= r; i++) {
 			for (int k = -r; k <= r; k++) {
 				int dx = rx+i;
 				int dz = rz+k;
 				BiomeGenBase biome = world.getBiomeGenForCoords(dx, dz);
 				int id = biome.biomeID;
 				if (id == ThaumBlockHandler.getInstance().taintBiomeID) {
					//ReikaJavaLibrary.pConsole(dx+", "+dz, Side.CLIENT);
 					BiomeGenBase[] biomes = new BiomeGenBase[1];
 					biomes = world.getWorldChunkManager().loadBlockGeneratorData(biomes, dx, dz, 1, 1);
 					BiomeGenBase natural = biomes != null && biomes.length > 0 ? biomes[0] : null;
 					if (natural != null) {
 						ReikaWorldHelper.setBiomeForXZ(world, dx, dz, natural);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean shouldRandomTick() {
 		return true;
 	}
 
 
 }
