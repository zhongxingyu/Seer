 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.block;
 
 import buildcraft.api.power.IPowerEmitter;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerHandler;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.api.IIntegerCoordinates;
 import monnef.core.power.PowerValues;
 import monnef.core.utils.DirectionHelper;
 import monnef.core.utils.IntegerCoordinates;
 import monnef.jaffas.power.common.BuildCraftHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.NetworkManager;
 import net.minecraft.network.Packet;
 import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.MathHelper;
 import net.minecraft.world.World;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import java.util.Random;
 
 public abstract class TileMachine extends TileMonnefCore implements IPowerReceptor, IPowerEmitter {
     public static final String POWER_TAG_NAME = "PowerData";
     public static final String ROTATION_TAG_NAME = "rotation";
     public static final Random rand = new Random();
     private static final int DUMMY_CREATION_PHASE_INSTANCE_COUNTER_LIMIT = 5;
     public static final float POWER_LOSS = 0.01f;
     protected int slowingCoefficient = 1;
     protected int doWorkCounter;
 
     private ForgeDirection rotation;
     protected PowerHandler powerHandler;
 
     protected int powerNeeded;
     protected int powerStorage;
     protected int maxEnergyReceived;
     protected PowerHandler.Type bcPowerType;
     private boolean isRedstoneSensitive = false;
     private boolean cachedRedstoneStatus;
     private boolean isRedstoneStatusDirty;
     private boolean forceFullCubeRenderBoundingBox;
     private boolean isPowerSource;
     private int tickCounter = 0;
 
     public static final ForgeDirection[] CUSTOMER_DIRECTIONS_ALL = new ForgeDirection[]{ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.DOWN};
     public static final ForgeDirection[] CUSTOMER_DIRECTIONS_NOT_BOTTOM = new ForgeDirection[]{ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.WEST, ForgeDirection.EAST};
     public static final ForgeDirection[] CUSTOMER_DIRECTIONS_BACK_AND_BOTTOM = new ForgeDirection[]{ForgeDirection.SOUTH, ForgeDirection.DOWN};
 
     private int startDirNumber;
     private ForgeDirection customerDirection = ForgeDirection.UNKNOWN;
 
     protected TileMachine() {
         onNewInstance(this);
         setRotation(ForgeDirection.UNKNOWN);
 
         configurePowerParameters();
         powerHandler = new PowerHandler(this, bcPowerType);
         powerHandler.configure(1, maxEnergyReceived, powerNeeded, powerStorage);
         powerHandler.setPerdition(new PowerHandler.PerditionCalculator(POWER_LOSS));
     }
 
     public void setForceFullCubeRenderBoundingBox(boolean value) {
         forceFullCubeRenderBoundingBox = value;
     }
 
     public boolean isRedstoneSensitive() {
         return isRedstoneSensitive;
     }
 
     public void setIsRedstoneSensitive() {
         isRedstoneSensitive = true;
     }
 
     public abstract String getMachineTitle();
 
     @Override
     public void updateEntity() {
         super.updateEntity();
         if (tickCounter == 0) {
             onFirstTick();
         }
         tickCounter++;
         if (isRedstoneStatusDirty) {
             isRedstoneStatusDirty = false;
             refreshCachedRedstoneStatus();
         }
         powerHandler.update();
         if (gotPowerToActivate() && gotPower(POWER_LOSS * 10)) {
             applyCounterPerdition();
         }
 
         if (!worldObj.isRemote) {
             onServerTick();
         }
     }
 
     private void onServerTick() {
         onBeforePowerSourceHandling();
 
         if (isPowerSource) {
             refreshCustomer();
 
             if (gotCustomer()) {
                 float energy = getEnergyGeneratedThisTick();
                 TileEntity consumerTile = getConsumerTile();
                 if (BuildCraftHelper.isPowerTile(consumerTile) && energy > 0) {
                     PowerHandler.PowerReceiver customersPowerReceiver = ((IPowerReceptor) consumerTile).getPowerReceiver(customerDirection.getOpposite());
                     customersPowerReceiver.receiveEnergy(PowerHandler.Type.ENGINE, energy, customerDirection.getOpposite());
                 }
             }
         }
 
         onAfterPowerSourceHandling();
     }
 
     /**
      * Used for generator to handle states sync.
      */
     protected void onAfterPowerSourceHandling() {
     }
 
     protected void onBeforePowerSourceHandling() {
     }
 
     public float getEnergyGeneratedThisTick() {
         return 0;
     }
 
     private TileEntity getConsumerTileInDirection(ForgeDirection dir) {
         return (new IntegerCoordinates(this)).shiftInDirectionBy(dir, 1).getBlockTileEntity();
     }
 
     protected boolean gotCustomer() {
         return customerDirection != ForgeDirection.UNKNOWN;
     }
 
     private boolean isCustomerInDirection(ForgeDirection dir) {
         TileEntity customer = getConsumerTileInDirection(dir);
         if (!BuildCraftHelper.isPowerTile(customer)) return false;
         return BuildCraftHelper.gotFreeSpaceInEnergyStorageAndWantsEnergy((IPowerReceptor) customer, customerDirection.getOpposite());
     }
 
    private int getDownRotationsNeededForCurrentRotation() {
         switch (rotation) {
             case NORTH:
                 return 0;
             case WEST:
                 return 1;
             case SOUTH:
                 return 2;
             case EAST:
                 return 3;
             default:
                 return 0;
         }
     }
 
    private int getUpRotationsNeededForCurrentRotation() {
        return 4 - getDownRotationsNeededForCurrentRotation();
    }

     private void refreshCustomer() {
         customerDirection = ForgeDirection.UNKNOWN;
         setNextCustomerDirection();
         int tested = 0;
 
         while (tested < getValidCustomerDirections().length) {
             ForgeDirection currentDirection = getValidCustomerDirections()[startDirNumber];
             currentDirection = DirectionHelper.applyRotationRepeatedly(currentDirection, ForgeDirection.UP, getUpRotationsNeededForCurrentRotation());
             if (isCustomerInDirection(currentDirection)) {
                 IPowerReceptor consumer = (IPowerReceptor) getConsumerTileInDirection(currentDirection);
                 if (BuildCraftHelper.gotFreeSpaceInEnergyStorage(consumer, currentDirection.getOpposite())) {
                     customerDirection = currentDirection;
                     return;
                 }
             }
             tested++;
             setNextCustomerDirection();
         }
 
         customerDirection = ForgeDirection.UNKNOWN;
     }
 
     private void setNextCustomerDirection() {
         startDirNumber++;
         if (startDirNumber >= getValidCustomerDirections().length) startDirNumber = 0;
     }
 
     public ForgeDirection[] getValidCustomerDirections() {
         return CUSTOMER_DIRECTIONS_ALL;
     }
 
     private TileEntity getConsumerTile() {
         return getConsumerTileInDirection(customerDirection);
     }
 
     protected void onFirstTick() {
     }
 
     /**
      * Configures this instance to serve as an engine.
      * Use only from {@link #configurePowerParameters}.
      */
     protected void configureAsPowerSource() {
         isPowerSource = true;
         bcPowerType = PowerHandler.Type.ENGINE;
         powerNeeded = 0;
     }
 
     protected void configurePowerParameters() {
         powerNeeded = MathHelper.floor_float(200 * PowerValues.totalPowerConsumptionCoef());
         maxEnergyReceived = powerNeeded;
         powerStorage = 10 * powerNeeded;
         bcPowerType = PowerHandler.Type.MACHINE;
     }
 
     public BlockMachine getMachineBlock() {
         return (BlockMachine) this.getBlockType();
     }
 
     public ForgeDirection getRotation() {
         return rotation;
     }
 
     public void setRotation(ForgeDirection rotation) {
         this.rotation = rotation;
     }
 
     @Override
     public void readFromNBT(NBTTagCompound tag) {
         super.readFromNBT(tag);
         this.rotation = ForgeDirection.getOrientation(tag.getByte(ROTATION_TAG_NAME));
         getPowerHandler().readFromNBT(tag, POWER_TAG_NAME);
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tag) {
         super.writeToNBT(tag);
         tag.setByte(ROTATION_TAG_NAME, (byte) this.rotation.ordinal());
         getPowerHandler().writeToNBT(tag, POWER_TAG_NAME);
     }
 
     @Override
     public void validate() {
         super.validate();
         markRedstoneStatusDirty();
     }
 
     public void markRedstoneStatusDirty() {
         isRedstoneStatusDirty = true;
     }
 
     @Override
     public Packet getDescriptionPacket() {
         S35PacketUpdateTileEntity packet = (S35PacketUpdateTileEntity) super.getDescriptionPacket();
         NBTTagCompound tag = packet != null ? packet.func_148857_g() : new NBTTagCompound();
 
         writeToNBT(tag);
 
         return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 8, tag);
     }
 
     @Override
     public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
         super.onDataPacket(net, pkt);
         NBTTagCompound tag = pkt.func_148857_g();
         readFromNBT(tag);
     }
 
     public void sendUpdate() {
         worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
     }
 
     public void setRotation(int direction) {
         this.setRotation(ForgeDirection.getOrientation(direction));
     }
 
     private static boolean dummyCreationPhase = false;
     private static int dummyCreationPhaseCounter;
 
     public static void enableDummyCreationPhase() {
         if (dummyCreationPhase) {
             throw new RuntimeException("Already in dummy creation phase.");
         }
 
         dummyCreationPhase = true;
         dummyCreationPhaseCounter = 0;
     }
 
     public static void disableDummyCreationPhase() {
         if (!dummyCreationPhase) {
             throw new RuntimeException("Not in dummy creation phase.");
         }
 
         dummyCreationPhase = false;
     }
 
     private static void onNewInstance(TileMachine instance) {
         if (dummyCreationPhase) {
             dummyCreationPhaseCounter++;
         }
 
         if (dummyCreationPhaseCounter >= DUMMY_CREATION_PHASE_INSTANCE_COUNTER_LIMIT) {
             MonnefCorePlugin.Log.printSevere(instance.getClass().getSimpleName() + ": limit of dummy creation has been exceeded!");
         }
     }
 
     protected void refreshCachedRedstoneStatus() {
         if (!isRedstoneSensitive()) return;
         cachedRedstoneStatus = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord);
     }
 
     /**
      * Returns if machine is getting any redstone power.
      * Cached!
      *
      * @return True if so.
      */
     public boolean isBeingPoweredByRedstone() {
         return cachedRedstoneStatus;
     }
 
     public boolean toggleRotation() {
         rotation = ForgeDirection.VALID_DIRECTIONS[(rotation.ordinal() + 1) % 4];
         return true;
     }
 
     @Override
     public AxisAlignedBB getRenderBoundingBox() {
         if (forceFullCubeRenderBoundingBox) {
             return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
         }
         return super.getRenderBoundingBox();
     }
 
     protected abstract void doMachineWork();
 
     public IIntegerCoordinates getPosition() {
         return new IntegerCoordinates(this);
     }
 
     public void onItemDebug(EntityPlayer player) {
     }
 
     public PowerHandler getPowerHandler() {
         return powerHandler;
     }
 
     //<editor-fold desc="BuildCraft API">
     @Override
     public final void doWork(PowerHandler workProvider) {
         doWorkCounter++;
         if (doWorkCounter >= slowingCoefficient) {
             doWorkCounter = 0;
             doMachineWork();
         }
     }
 
     // just fixing what was broken in BuildCraft,
     // I really need to look at Thermal Expansion power and move away from BC
     private void applyCounterPerdition() {
         getPowerHandler().addEnergy(POWER_LOSS);
     }
 
     @Override
     public boolean canEmitPowerFrom(ForgeDirection side) {
         return isPowerSource;
     }
 
     @Override
     public PowerHandler.PowerReceiver getPowerReceiver(ForgeDirection side) {
         return powerHandler.getPowerReceiver();
     }
 
     @Override
     public World getWorld() {
         return worldObj;
     }
     //</editor-fold>
 
     public boolean gotPowerToActivate() {
         return gotPower(getPowerHandler().getActivationEnergy());
     }
 
     public boolean gotPower(double amount) {
         return getPowerHandler().getEnergyStored() >= amount;
     }
 
     protected double consumeNeededPower() {
         return consumePower(powerNeeded);
     }
 
     protected double consumePower(double amount) {
         return getPowerHandler().useEnergy(amount, amount, true);
     }
 }
 
