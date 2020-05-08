 package snake.mcmods.theinvoker.energy;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 
 public abstract class EnergyUnit
 {
 	public EnergyUnit(TileEntity te, int energyID)
 	{
 		if (te == null)
 			throw new IllegalArgumentException("you dare to create a EnergyContainer without a valid TileEntity?");
 		this.te = te;
 		if (EnergyCenter.INSTANCE.getEnergyForce(energyID) != null)
 			this.energyID = energyID;
 		else
 			throw new IllegalArgumentException("you have created a EnergyContainer without a valid Energy ID");
 		isAvailable = true;
 	}
 
 	private int energyID;
 	private TileEntity te;
 	protected int effectiveRange;
 	protected int maxEnergyRequest;
 	protected boolean isAvailable;
 	protected boolean isRegistered;
 
 	public int getEnergyID()
 	{
 		return energyID;
 	}
 
 	public boolean getIsAvailable()
 	{
 		return isAvailable;
 	}
 
 	public void setIsAvailable(boolean val)
 	{
 		isAvailable = val;
 	}
 
 	public boolean getIsRegistered()
 	{
 		return isRegistered;
 	}
 
 	public int getEffectiveRange()
 	{
 		return effectiveRange;
 	}
 
 	public void setEffectiveRange(int range)
 	{
 		effectiveRange = range;
 	}
 
 	public int getEffectiveRangeSq()
 	{
 		return getEffectiveRange() * getEffectiveRange();
 	}
 
 	public int getMaxEnergyRequest()
 	{
		return maxEnergyRequest;
 	}
 
 	public void setMaxEnergyRequest(int val)
 	{
		maxEnergyRequest = val;
 	}
 
 	public TileEntity getTileEntity()
 	{
 		return te;
 	}
 
 	public abstract void register();
 
 	public abstract void destroy();
 
 	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
 	{
 		nbt.setInteger(TAG_RANGE, getEffectiveRange());
 		nbt.setInteger(TAG_ENERGY_ID, getEnergyID());
 		nbt.setInteger(TAG_MAX_REQUEST, getMaxEnergyRequest());
 		nbt.setBoolean(TAG_IS_AVAILABLE, getIsAvailable());
 		return nbt;
 	}
 
 	protected static final String TAG_ENERGY_ID = "energyId";
 	protected static final String TAG_RANGE = "range";
 	protected static final String TAG_IS_AVAILABLE = "isAvailable";
 	protected static final String TAG_MAX_REQUEST = "maxRequest";
 }
