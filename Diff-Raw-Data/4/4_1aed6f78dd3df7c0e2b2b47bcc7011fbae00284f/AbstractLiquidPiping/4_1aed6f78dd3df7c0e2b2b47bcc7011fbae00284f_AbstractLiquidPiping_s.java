 package com.madpcgaming.citytech.piping.liquid;
 
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraftforge.common.util.ForgeDirection;
 import net.minecraftforge.fluids.IFluidHandler;
 import buildcraft.api.transport.IPipeTile;
 import buildcraft.api.transport.IPipeTile.PipeType;
 
 import com.madpcgaming.citytech.machine.RedstoneControlMode;
 import com.madpcgaming.citytech.piping.AbstractPiping;
 import com.madpcgaming.citytech.piping.ConnectionMode;
 import com.madpcgaming.citytech.piping.IPiping;
 import com.madpcgaming.citytech.piping.IPipingBundle;
 import com.madpcgaming.citytech.piping.PipingUtil;
 import com.madpcgaming.citytech.util.BlockCoord;
 import com.madpcgaming.citytech.util.DyeColor;
 
 public abstract class AbstractLiquidPiping extends AbstractPiping implements ILiquidPiping
 {
 	protected final EnumMap<ForgeDirection, RedstoneControlMode> extractionModes = new EnumMap<ForgeDirection, RedstoneControlMode>(ForgeDirection.class);
 	protected final EnumMap<ForgeDirection, DyeColor> extractionColors = new EnumMap<ForgeDirection, DyeColor>(ForgeDirection.class);
 	
 	protected final Map<ForgeDirection, Integer> externalRedstoneSignals = new HashMap<ForgeDirection, Integer>();
 	protected boolean redstoneStateDirty = true;
 	
 	public IFluidHandler getExternalHandler(ForgeDirection direction)
 	{
 		IFluidHandler pipe = getTankContainer(getLocation().getLocation(direction));
 		return (pipe != null && !(pipe instanceof IPipingBundle)) ? pipe : null;
 	}
 	
 	public IFluidHandler getTankContainer(BlockCoord bc)
 	{
 		return getTankContainer(bc.x, bc.y, bc.z);
 	}
 	
 	public IFluidHandler getTankContainer(int x, int y, int z)
 	{
 		TileEntity te = getBundle().getEntity().getWorldObj().getTileEntity(x, y, z);
 		if(te instanceof IFluidHandler)
 		{
 			if(te instanceof IPipeTile)
 			{
 				if(((IPipeTile) te).getPipeType() != PipeType.FLUID)
 				{
 					return null;
 				}
 			}
 			return (IFluidHandler) te;
 		}
 		return null;
 	}
 	
 	@Override
 	public boolean canConnectToExternal(ForgeDirection direction, boolean ignoreDisabled)
 	{
 		return getExternalHandler(direction) != null;
 	}
 	
 	@Override
 	public Class<? extends IPiping> getBasePipingType()
 	{
 		return ILiquidPiping.class;
 	}
 	
 	@Override
	public boolean onNeighborBlockChange(int blockId)
 	{
 		redstoneStateDirty = true;
 		return super.onNeighborBlockChange(blockId);
 	}
 	
 	@Override
 	public void setExtractionRedstoneMode(RedstoneControlMode mode, ForgeDirection dir)
 	{
 		extractionModes.put(dir, mode);
 		redstoneStateDirty = true;
 	}
 	
 	@Override
 	public RedstoneControlMode getExtractionRedstoneMode(ForgeDirection dir)
 	{
 		RedstoneControlMode res = extractionModes.get(dir);
 		if(res == null)
 		{
 			res = RedstoneControlMode.ON;
 		}
 		return res;
 	}
 	
 	@Override
 	public void setExtractionSignalColor(ForgeDirection dir, DyeColor col)
 	{
 		extractionColors.put(dir, col);
 	}
 	
 	@Override
 	public DyeColor getExtractionSignalColor(ForgeDirection dir)
 	{
 		DyeColor result = extractionColors.get(dir);
 		if(result == null)
 		{
 			return DyeColor.RED;
 		}
 		return result;
 	}
 	
 	 @Override
 	  public boolean canOutputToDir(ForgeDirection dir) {
 	    if(isExtractingFromDir(dir) || getConnectionMode(dir) == ConnectionMode.DISABLED) {
 	      return false;
 	    }
 	    if(pipingConnections.contains(dir)) {
 	      return true;
 	    }
 	    if(!externalConnections.contains(dir)) {
 	      return false;
 	    }
 	    return true;
 	  }
 	 
 	 @SuppressWarnings("static-access")
 	protected boolean autoExtractForDir(ForgeDirection dir) {
 		    if(!isExtractingFromDir(dir)) {
 		      return false;
 		    }
 		    RedstoneControlMode mode = getExtractionRedstoneMode(dir);
 		    if(mode == RedstoneControlMode.IGNORE) {
 		      return true;
 		    }
 		    if(mode == RedstoneControlMode.NEVER) {
 		      return false;
 		    }
 		    if(redstoneStateDirty) {
 		      externalRedstoneSignals.clear();
 		      redstoneStateDirty = false;
 		    }
 
 		    DyeColor col = getExtractionSignalColor(dir);
 		    int signal = PipingUtil.getInternalSignalForColor(getBundle(), col);
 		    if(mode.isConditionMet(mode, signal)) {
 		      return true;
 		    }
 
 		    int externalSignal = 0;
 		    if(col == DyeColor.RED) {
 		      Integer val = externalRedstoneSignals.get(dir);
 		      if(val == null) {
 		        TileEntity te = getBundle().getEntity();
 		        externalSignal = te.getWorldObj().getStrongestIndirectPower(te.xCoord, te.yCoord, te.zCoord);
 		        externalRedstoneSignals.put(dir, externalSignal);
 		      } else {
 		        externalSignal = val;
 		      }
 		    }
 
 		    return mode.isConditionMet(mode, externalSignal);
 		  }
 	 @Override
 	  public boolean isExtractingFromDir(ForgeDirection dir) {
 	    return getConnectionMode(dir) == ConnectionMode.INPUT;
 	  }
 
 	  @Override
 	  public void writeToNBT(NBTTagCompound nbtRoot) {
 	    super.writeToNBT(nbtRoot);
 
 	    for (Entry<ForgeDirection, RedstoneControlMode> entry : extractionModes.entrySet()) {
 	      if(entry.getValue() != null) {
 	        short ord = (short) entry.getValue().ordinal();
 	        nbtRoot.setShort("extRM." + entry.getKey().name(), ord);
 	      }
 	    }
 
 	    for (Entry<ForgeDirection, DyeColor> entry : extractionColors.entrySet()) {
 	      if(entry.getValue() != null) {
 	        short ord = (short) entry.getValue().ordinal();
 	        nbtRoot.setShort("extSC." + entry.getKey().name(), ord);
 	      }
 	    }
 
 	  }
 
 	  @Override
 	  public void readFromNBT(NBTTagCompound nbtRoot) {
 	    super.readFromNBT(nbtRoot);
 
 	    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
 	      String key = "extRM." + dir.name();
 	      if(nbtRoot.hasKey(key)) {
 	        short ord = nbtRoot.getShort(key);
 	        if(ord >= 0 && ord < RedstoneControlMode.values().length) {
 	          extractionModes.put(dir, RedstoneControlMode.values()[ord]);
 	        }
 	      }
 	      key = "extSC." + dir.name();
 	      if(nbtRoot.hasKey(key)) {
 	        short ord = nbtRoot.getShort(key);
 	        if(ord >= 0 && ord < DyeColor.values().length) {
 	          extractionColors.put(dir, DyeColor.values()[ord]);
 	        }
 	      }
 	    }
 	  }
 
 }
