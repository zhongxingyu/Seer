 package com.madpcgaming.citytech.piping;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.init.Blocks;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.IBlockAccess;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 import net.minecraftforge.fluids.FluidRegistry;
 import net.minecraftforge.fluids.FluidStack;
 
 import com.madpcgaming.buildcraft.api.tools.IToolWrench;
 import com.madpcgaming.citytech.CityTech;
 import com.madpcgaming.citytech.items.ModItems;
 import com.madpcgaming.citytech.machine.RedstoneControlMode;
 import com.madpcgaming.citytech.piping.IPipingBundle.FacadeRenderState;
 import com.madpcgaming.citytech.piping.item.IItemPiping;
 import com.madpcgaming.citytech.piping.liquid.ILiquidPiping;
 import com.madpcgaming.citytech.piping.liquid.LiquidPipingNetwork;
 import com.madpcgaming.citytech.piping.redstone.IRedstonePiping;
 import com.madpcgaming.citytech.piping.redstone.Signal;
 import com.madpcgaming.citytech.util.BlockCoord;
 import com.madpcgaming.citytech.util.DyeColor;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class PipingUtil
 {
 	public static final Random RANDOM = new Random();
 	
 	public static AbstractPipingNetwork<?,?> createNetworkForType(Class<? extends IPiping> type)
 	{
 		if(IRedstonePiping.class.isAssignableFrom(type))
 		{
 			//TODO: Implement RedsonePipingNetwork
 			return null;//new RedstonePipingNetwork();
 		}
 		else if(ILiquidPiping.class.isAssignableFrom(type))
 		{
 			return new LiquidPipingNetwork();
 		}
 		else if(IItemPiping.class.isAssignableFrom(type))
 		{
 			//TODO: Implement ItemPipingNetwork
 			return null;// new ItemPipingNetwork();
 		}
 		FMLCommonHandler.instance().raiseException(new Exception("Could not determine network type for class " + type), "PipingUtil.createNetworkForType", false);
 		return null;
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static void ensureValidNetwork(IPiping piping)
 	{
 		TileEntity te = piping.getBundle().getEntity();
 		World world = te.getWorldObj();
 		Collection<? extends IPiping> connections = PipingUtil.getConnectedPiping(world, te.xCoord, te.yCoord, te.zCoord, piping.getBasePipingType());
 		
 		if(reuseNetwork(piping, connections, world))
 		{
 			return;
 		}
 		
 		AbstractPipingNetwork res = createNetworkForType(piping.getClass());
 		res.init(piping.getBundle(), connections, world);
 		return;
 	}
 	
 	@SuppressWarnings({ "rawtypes", "unchecked"})
 	private static boolean reuseNetwork(IPiping pipe, Collection<? extends IPiping> connections, World world)
 	{
 		AbstractPipingNetwork network = null;
 		for(IPiping pipes : connections)
 		{
 			if(network == null)
 			{
 				network = pipes.getNetwork();
 			}
 			else if(network != pipes.getNetwork())
 			{
 				return false;
 			}
 		}
 		if(network == null)
 		{
 			return false;
 		}
 		if(pipe.setNetwork(network))
 		{
 			network.addPiping(pipe);
 			network.notifyNetworkOfUpdate();
 			return true;
 		}
 		return false;
 	}
 	
 	public static <T extends IPiping> void disconnectPiping(T pipe, ForgeDirection connDir)
 	{
 		pipe.pipingConnectionRemoved(connDir);
 		BlockCoord loc = pipe.getLocation().getLocation(connDir);
 		IPiping neighbor = PipingUtil.getPiping(pipe.getBundle().getEntity().getWorldObj(), loc.x, loc.y, loc.z, pipe.getBasePipingType());
 		if(neighbor != null)
 		{
 			neighbor.pipingConnectionRemoved(connDir.getOpposite());
 			if(neighbor.getNetwork() != null)
 			{
 				neighbor.getNetwork().destroyNetwork();
 			}
 		}
 		if(pipe.getNetwork() != null)
 		{
 			pipe.getNetwork().destroyNetwork();
 		}
 	}
 	
 	public static <T extends IPiping> boolean joinPiping(T pipe, ForgeDirection faceHit)
 	{
 		BlockCoord loc = pipe.getLocation().getLocation(faceHit);
 		IPiping neighbor = PipingUtil.getPiping(pipe.getBundle().getEntity().getWorldObj(), loc.x, loc.y, loc.z, pipe.getBasePipingType());
 		if(neighbor != null && pipe.canConnectToPiping(faceHit, neighbor) && neighbor.canConnectToPiping(faceHit.getOpposite(), pipe)) {
 		      pipe.pipingConnectionAdded(faceHit);
 		      neighbor.pipingConnectionAdded(faceHit.getOpposite());
 		      if(pipe.getNetwork() != null) {
 		        pipe.getNetwork().destroyNetwork();
 		      }
 		      if(neighbor.getNetwork() != null) {
 		        neighbor.getNetwork().destroyNetwork();
 		      }
 		      return true;
 		    }
 		    return false;
 		  }
 	
 	public static boolean forceSkylightRecalulation(World worldObj, int xCoord, int yCoord, int zCoord)
 	{
 		int height = worldObj.getHeightValue(xCoord, zCoord);
 		if(height <= yCoord)
 		{
 			for(int i = 1; i < 12; i++)
 			{
 				if(worldObj.isAirBlock(xCoord, yCoord + 1, zCoord))
 				{
 					worldObj.setBlock(xCoord, yCoord + i, zCoord, Blocks.air);
 					worldObj.setBlockToAir(xCoord, yCoord + i, zCoord);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public static FacadeRenderState getRequiredFacadeRenderState(IPipingBundle bundle, EntityPlayer player)
 	{
 		if(!bundle.hasFacade())
 		{
 			return FacadeRenderState.NONE;
 		}
 		if(isFacadeHidden(bundle, player))
 		{
 			return FacadeRenderState.WIRE_FRAME;
 		}
 		return FacadeRenderState.FULL;
 	}
 	
 	public static boolean isSolidFacadeRendered(IPipingBundle bundle, EntityPlayer player)
 	{
 		return bundle.getFacadeID() > 0 && !isFacadeHidden(bundle, player);
 	}
 	
 	public static boolean isFacadeHidden(IPipingBundle bundle, EntityPlayer player)
 	{
 		return bundle.getFacadeID() > 0 && (isToolEquipped(player) || isPipingEquipped(player));
 	}
 	
 	public static PipingDisplayMode getDisplayMode(EntityPlayer player)
 	{
 		player = player == null ? CityTech.proxy.getClientPlayer() : player;
 		if(player == null)
 		{
 			return PipingDisplayMode.ALL;
 		}
 		ItemStack equipped = player.getHeldItem();
 		if(equipped == null)
 		{
 			return PipingDisplayMode.ALL;
 		}
 		if(equipped.getItem() != ModItems.Wrench)
 		{
 			return PipingDisplayMode.ALL;
 		}
 		PipingDisplayMode result = PipingDisplayMode.getDisplayMode(equipped);
 		if(result == null)
 		{
 			return PipingDisplayMode.ALL;
 		}
 		return result;
 	}
 	
 	public static boolean renderPiping(EntityPlayer player, IPiping pipes)
 	{
 		if(player == null || pipes == null)
 		{
 			return true;
 		}
 		return renderPiping(player, pipes.getBasePipingType());
 	}
 	
 	public static boolean renderPiping(EntityPlayer player, Class<? extends IPiping> pipingType)
 	{
 		if(player == null || pipingType == null)
 		{
 			return true;
 		}
 		PipingDisplayMode mode = getDisplayMode(player);
 		switch(mode)
 		{
 			case ALL:
 				return true;
 			case FLUID:
 				return pipingType == ILiquidPiping.class;
			//case ITEM:
				//return pipingType == IItemPiping.class;
 			default:
 				break;
 		}
 		return true;
 	}
 	
 	public static boolean isPipingEquipped(EntityPlayer player)
 	{
 		player = player == null ? CityTech.proxy.getClientPlayer() : player;
 		if(player == null)
 		{
 			return false;
 		}
 		ItemStack equipped = player.getCurrentEquippedItem();
 		if(equipped == null)
 		{
 			return false;
 		}
 		return equipped.getItem() instanceof IPipingItem;
 	}
 	
 	public static boolean isToolEquipped(EntityPlayer player)
 	{
 		player = player == null ? CityTech.proxy.getClientPlayer() : player;
 		if(player == null)
 		{
 			return false;
 		}
 		ItemStack equipped = player.getCurrentEquippedItem();
 		if(equipped == null)
 		{
 			return false;
 		}
 		return equipped.getItem() instanceof IToolWrench;
 	}
 	
 	public static <T extends IPiping> T getPiping(IBlockAccess world, int x, int y, int z, Class<T> type)
 	{
 		if(world == null)
 		{
 			return null;
 		}
 		TileEntity te = world.getTileEntity(x, y, z);
 		if(te instanceof IPipingBundle)
 		{
 			IPipingBundle pipe = (IPipingBundle) te;
 			return pipe.getPiping(type);
 		}
 		return null;
 	}
 	
 	public static <T extends IPiping> T getPiping(IBlockAccess world, TileEntity te, ForgeDirection dir, Class<T> type)
 	{
 		return PipingUtil.getPiping(world, te.xCoord + dir.offsetX, te.yCoord + dir.offsetY, te.zCoord + dir.offsetZ, type);
 	}
 	
 	public static <T extends IPiping> Collection<T> getConnectedPiping(IBlockAccess world, int x, int y, int z, Class<T> type)
 	{
 		TileEntity te = world.getTileEntity(x, y, z);
 		if(!(te instanceof IPipingBundle))
 		{
 			return Collections.emptyList();
 		}
 		List<T> result = new ArrayList<T>();
 		IPipingBundle root = (IPipingBundle) te;
 		T pipes = root.getPiping(type);
 		if(pipes != null)
 		{
 			for(ForgeDirection dir : pipes.getPipingConnections())
 			{
 				T connected = getPiping(world, root.getEntity(), dir, type);
 				if(connected != null)
 				{
 					result.add(connected);
 				}
 			}
 		}
 		return result;
 	}
 	
 	public static void writeToNBT(IPiping piping, NBTTagCompound pipingRoot)
 	{
 		if(piping == null)
 		{
 			return;
 		}
 		
 		NBTTagCompound pipingBody = new NBTTagCompound();
 		piping.writeToNBT(pipingBody);
 		
 		pipingRoot.setString("pipingType", piping.getClass().getCanonicalName());
 		pipingRoot.setTag("piping", pipingBody);
 	}
 	
 	public static IPiping readPipingFromNBT(NBTTagCompound pipingRoot, short nbtVersion)
 	{
 		String typeName = pipingRoot.getString("pipingType");
 		NBTTagCompound pipingBody = pipingRoot.getCompoundTag("piping");
 		if(typeName == null || pipingBody == null)
 		{
 			return null;
 		}
 		IPiping result;
 		try
 		{
 			result = (IPiping) Class.forName(typeName).newInstance();
 		}
 		catch(Exception e)
 		{
 			throw new RuntimeException("Could not create an instance of the piping with name: " + typeName, e);
 		}
 		result.readFromNBT(pipingBody);
 		return result;
 	}
 	
 	@SuppressWarnings("static-access")
 	public static boolean isRedstoneControlModeMet(IPipingBundle bundle, RedstoneControlMode mode, DyeColor col)
 	{
 		if(mode == RedstoneControlMode.IGNORE)
 		{
 			return true;
 		}
 		else if(mode == RedstoneControlMode.NEVER)
 		{
 			return false;
 		}
 		
 		int signalStrength = getInternalSignalForColor(bundle, col);
 		if(signalStrength < 15 && DyeColor.RED == col && bundle != null && bundle.getEntity() != null)
 		{
 			TileEntity te = bundle.getEntity();
 			signalStrength = Math.max(signalStrength, te.getWorldObj().getStrongestIndirectPower(te.xCoord, te.yCoord, te.zCoord));
 		}
 		return mode.isConditionMet(mode, signalStrength);
 	}
 	
 	public static int getInternalSignalForColor(IPipingBundle bundle, DyeColor col)
 	{
 		int signalStrength = 0;
 		IRedstonePiping rsPipe = bundle.getPiping(IRedstonePiping.class);
 		if(rsPipe != null)
 		{
 			Set<Signal> signals = rsPipe.getNetworkOutputs(ForgeDirection.UNKNOWN);
 			for(Signal sig : signals)
 			{
 				if(sig.color == col)
 				{
 					if(sig.strength > signalStrength)
 					{
 						signalStrength = sig.strength;
 					}
 				}
 			}
 		}
 		return signalStrength;
 	}
 	
 	public static boolean isFluidValid(FluidStack fluidStack)
 	{
 		if(fluidStack != null)
 		{
 			String name = FluidRegistry.getFluidName(fluidStack);
 			if(name != null && !name.trim().isEmpty())
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
