 /*
  * Jaffas and more!
  * author: monnef
  */
 
 package monnef.core.block;
 
 import cofh.api.energy.IEnergyProvider;
 import cofh.api.energy.IEnergyReceiver;
 import monnef.core.MonnefCorePlugin;
 import monnef.core.api.IIntegerCoordinates;
 import monnef.core.power.MonnefCoreEnergyStorage;
 import monnef.core.power.PowerValues;
 import monnef.core.utils.DirectionHelper;
 import monnef.core.utils.IntegerCoordinates;
 import monnef.jaffas.power.common.RedstoneFluxHelper;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.NetworkManager;
 import net.minecraft.network.Packet;
 import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.AxisAlignedBB;
 import net.minecraft.util.MathHelper;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import java.util.Random;
 
public abstract class TileMachine extends TileMonnefCore implements IEnergyProvider, IEnergyReceiver {
     public static final String ROTATION_TAG_NAME = "rotation";
     public static final Random rand = new Random();
     private static final int DUMMY_CREATION_PHASE_INSTANCE_COUNTER_LIMIT = 5;
     private static final int REFRESH_CUSTOMER_EVERY_NTH_TICK = 40;
     protected int slowingCoefficient = 1;
     protected int doWorkCounter;
 
     private ForgeDirection rotation;
     protected MonnefCoreEnergyStorage energyStorage;
 
     protected int powerNeeded;
     protected int powerStorage;
     protected int maxEnergyReceived;
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
         energyStorage = new MonnefCoreEnergyStorage(powerStorage, maxEnergyReceived);
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
 
         if (gotPowerToActivate()) {
             doWorkCounter++;
             if (doWorkCounter >= slowingCoefficient) {
                 doWorkCounter = 0;
                 doMachineWork();
             }
         }
 
         if (!worldObj.isRemote) {
             onServerTick();
         }
     }
 
     private void onServerTick() {
         handlePossiblePowerSource();
     }
 
     private void handlePossiblePowerSource() {
         onBeforePowerSourceHandling();
 
         if (isPowerSource) {
             handlePowerSource();
         }
 
         onAfterPowerSourceHandling();
     }
 
     private void handlePowerSource() {
         if (tickCounter % REFRESH_CUSTOMER_EVERY_NTH_TICK == 0) refreshCustomer();
         int generatedEnergy = Math.round(getEnergyGeneratedThisTick());
         if (generatedEnergy > 0) {
             generatedEnergy = handleEnergyDistribution(generatedEnergy);
 
             if (generatedEnergy > 0) {
                 // TODO: react on energy wasting, maybe some particles like smoke?
             }
         }
     }
 
     private int handleEnergyDistribution(int generatedEnergy) {
         generatedEnergy -= energyStorage.receiveEnergy(generatedEnergy, false);
 
         if (energyStorage.getEnergyStored() > 0 && gotCustomer()) {
             TileEntity consumerTile = getConsumerTile();
             if (RedstoneFluxHelper.isTilePowerReceiver(consumerTile)) {
                 IEnergyReceiver customerEnergyConnection = (IEnergyReceiver) consumerTile;
                 ForgeDirection myDirectionFromCustomersView = customerDirection.getOpposite();
                 int availableEnergy = extractEnergy(customerDirection, energyStorage.getMaxExtract(), true);
                 int sentPower = customerEnergyConnection.receiveEnergy(myDirectionFromCustomersView, availableEnergy, false);
                 extractEnergy(customerDirection, sentPower, false);
             }
         }
         return generatedEnergy;
     }
 
     /**
      * Used for generator to handle states sync.
      */
     protected void onAfterPowerSourceHandling() {
     }
 
     protected void onBeforePowerSourceHandling() {
     }
 
     public int getEnergyGeneratedThisTick() {
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
         if (!RedstoneFluxHelper.isTilePowerReceiver(customer)) return false;
         return RedstoneFluxHelper.gotFreeSpaceInEnergyStorageAndWantsEnergy((IEnergyReceiver) customer, customerDirection.getOpposite());
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
                 IEnergyReceiver consumer = (IEnergyReceiver) getConsumerTileInDirection(currentDirection);
                 if (RedstoneFluxHelper.gotFreeSpaceInEnergyStorage(consumer, currentDirection.getOpposite())) {
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
         powerNeeded = 0;
     }
 
     protected void configurePowerParameters() {
         powerNeeded = MathHelper.floor_float(200 * PowerValues.totalPowerConsumptionCoef());
         maxEnergyReceived = powerNeeded;
         powerStorage = 10 * powerNeeded;
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
         energyStorage.readFromNBT(tag);
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tag) {
         super.writeToNBT(tag);
         tag.setByte(ROTATION_TAG_NAME, (byte) this.rotation.ordinal());
         energyStorage.writeToNBT(tag);
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
 
     public boolean gotPowerToActivate() {
         return gotPower(powerNeeded);
     }
 
     public boolean gotPower(int amount) {
         return energyStorage.getEnergyStored() >= amount;
     }
 
     protected int consumeNeededPower() {
         return consumePower(powerNeeded);
     }
 
     protected int consumePower(int amount) {
         return energyStorage.extractEnergy(amount, false);
     }
 
     //<editor-fold desc="RedstoneFlux API">
 
     // Receiver
 
     @Override
     public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
         return isPowerSource ? 0 : energyStorage.receiveEnergy(maxReceive, simulate);
     }
 
     @Override
     public int getEnergyStored(ForgeDirection from) {
         return energyStorage.getEnergyStored();
     }
 
     @Override
     public int getMaxEnergyStored(ForgeDirection from) {
         return energyStorage.getMaxEnergyStored();
     }
 
     // Provider
 
     @Override
     public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
         return isPowerSource ? energyStorage.extractEnergy(maxExtract, simulate) : 0;
     }
 
     @Override
     public boolean canConnectEnergy(ForgeDirection from) {
         return true;
     }
     //</editor-fold>
 
 
     public MonnefCoreEnergyStorage getEnergyStorage() {
         return energyStorage;
     }
 
     public int getPowerNeeded() {
         return powerNeeded;
     }
 
     public boolean isInternalEnergyStorageFull() {
         return energyStorage.getEnergyStored() >= energyStorage.getMaxEnergyStored();
     }
 
     public boolean isPowerSource() {
         return isPowerSource;
     }
 }
