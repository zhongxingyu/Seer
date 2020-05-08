 package pixlepix.complexmachines.common.laser;
 
 import java.util.Random;
 
 import pixlepix.complexmachines.common.ComplexMachines;
 import pixlepix.complexmachines.common.laser.tileentity.LaserBeamTileEntity;
 import pixlepix.complexmachines.common.laser.tileentity.SuctionLaserBeamTileEntity;
 
 import net.minecraft.entity.item.EntityItem;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.nbt.NBTTagList;
 import net.minecraft.network.INetworkManager;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.World;
 import net.minecraftforge.common.ForgeDirection;
 import universalelectricity.core.UniversalElectricity;
 import universalelectricity.core.block.IElectricityStorage;
 import universalelectricity.core.electricity.ElectricityNetworkHelper;
 import universalelectricity.core.electricity.ElectricityPack;
 import universalelectricity.core.electricity.IElectricityNetwork;
 import universalelectricity.core.vector.Vector3;
 import universalelectricity.core.vector.VectorHelper;
 import universalelectricity.prefab.network.IPacketReceiver;
 import universalelectricity.prefab.network.PacketManager;
 import universalelectricity.prefab.tile.TileEntityElectricityRunnable;
 
 import com.google.common.io.ByteArrayDataInput;
 
 public class LaserEmitterTileEntity extends TileEntityElectricityRunnable
 		implements IPacketReceiver, IElectricityStorage {
 	public final double WATTS_PER_TICK = 5000;
 	public final double TRANSFER_LIMIT = 12500;
 	private int drawingTicks = 0;
 	private double joulesStored = 0;
 	private boolean decay=false;
 	public static double maxJoules = 100000;
 	public int ticks = 0;
 	/**
 	 * The ItemStacks that hold the items currently being used in the wire mill;
 	 * 0 = battery; 1 = input; 2 = output;
 	 */
 	private ItemStack[] inventory = new ItemStack[1];
 
 	private int playersUsing = 0;
 	public int orientation;
 	private int targetID = 0;
 	private int targetMeta = 0;
 
 	private boolean initialized;
 	private int internalId;
 	private int laserBeamId;
 
 	@Override
 	public void initiate() {
 		this.initialized = true;
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		// System.out.println(getJoules());
 		if (!this.worldObj.isRemote) {
 
 			ForgeDirection inputDirection = ForgeDirection.getOrientation(this
 					.getBlockMetadata() + 2);
 			TileEntity inputTile = VectorHelper.getTileEntityFromSide(
 					this.worldObj, new Vector3(this), inputDirection);
 
 			IElectricityNetwork inputNetwork = ElectricityNetworkHelper
 					.getNetworkFromTileEntity(inputTile,
 							inputDirection.getOpposite());
 
 			if (inputNetwork != null) {
 				if (this.joulesStored < LaserEmitterTileEntity.maxJoules) {
 					inputNetwork.startRequesting(this,Math.min(this.getMaxJoules() - this.getJoules(),this.TRANSFER_LIMIT) / this.getVoltage(),this.getVoltage());
 					ElectricityPack electricityPack = inputNetwork.consumeElectricity(this);
 					this.setJoules(this.joulesStored+ electricityPack.getWatts());
 
 					if (UniversalElectricity.isVoltageSensitive) {
 						if (electricityPack.voltage > this.getVoltage()) {
 							this.worldObj.createExplosion(null, this.xCoord,
 									this.yCoord, this.zCoord, 2f, true);
 						}
 					}
 				} else {
 					inputNetwork.stopRequesting(this);
 				}
 				ticks++;
 				if (getJoules() > 10000) {
 					ForgeDirection laserDirection = inputDirection.getOpposite();
 					int id=worldObj.getBlockId(xCoord+ laserDirection.offsetX, yCoord, zCoord + laserDirection.offsetZ);
 					if (ticks % 40 == 0||id==0||decay) {
 						setJoules(getJoules() - 8000);
 
 						switch(internalId){
 
 						case 276:
 							laserBeamId=ComplexMachines.blockStartingID+11;
 							break;
 						case 20:
 							laserBeamId=ComplexMachines.blockStartingID+12;
 							//System.out.println("Laser of glass");
 							break;
 						case 331:
 							laserBeamId=ComplexMachines.blockStartingID+14;
 							//System.out.println("Laser of glass");
 							break;
 						case 399:
 							laserBeamId=ComplexMachines.blockStartingID+13;
 							//System.out.println("Laser of glass");
 							break;
 						case 326:
 							laserBeamId=ComplexMachines.blockStartingID+16;
 							//System.out.println("Laser of glass");
 							break;
 						case 295:
 							laserBeamId=ComplexMachines.blockStartingID+18;
 							//System.out.println("Laser of glass");
 							break;
 						case 328:
 							laserBeamId=ComplexMachines.blockStartingID+17;
 							//System.out.println("Laser of glass");
 							break;
 						case 4:
 							laserBeamId=ComplexMachines.blockStartingID+19;
 							//System.out.println("Laser of glass");
 							break;
 						case 278:
 							laserBeamId=ComplexMachines.blockStartingID+15;
 							//System.out.println("Laser of glass");
 							break;
 						default:
 							laserBeamId=ComplexMachines.blockStartingID+10;
 							break;
 
 
 						}
 						int max=30;
 						if(laserBeamId==ComplexMachines.blockStartingID+18){
 							max=3;
 						}
 						for (int i = 1; i < max; i++) {
 							if(worldObj.getBlockId(xCoord + laserDirection.offsetX * i, yCoord, zCoord + laserDirection.offsetZ * i) == 1&&internalId==278){
 								if(getJoules()>50000){
 									setJoules(getJoules()-50000);
 									worldObj.setBlock(xCoord + laserDirection.offsetX * i, yCoord, zCoord + laserDirection.offsetZ * i, 0);
 									
 									
 								}
 							}
 							if (worldObj.getBlockId(xCoord + laserDirection.offsetX * i, yCoord, zCoord + laserDirection.offsetZ * i) == 0||worldObj.getBlockTileEntity(xCoord + laserDirection.offsetX * i, yCoord, zCoord + laserDirection.offsetZ * i) instanceof LaserBeamTileEntity) {
 								worldObj.setBlock(xCoord+ laserDirection.offsetX * i, yCoord,zCoord + laserDirection.offsetZ * i,laserBeamId);
 								TileEntity entity=worldObj.getBlockTileEntity(xCoord + laserDirection.offsetX*i, yCoord, zCoord + laserDirection.offsetZ * i);
								if(entity instanceof LaserBeamTileEntity){
									LaserBeamTileEntity laserEntity=(LaserBeamTileEntity)entity;
									laserEntity.setEntity(this);
								{
 								if (worldObj.getBlockTileEntity(xCoord+ laserDirection.offsetX * i, yCoord,zCoord + laserDirection.offsetZ * i) instanceof SuctionLaserBeamTileEntity) {
 									SuctionLaserBeamTileEntity suctionEntity = (SuctionLaserBeamTileEntity) worldObj.getBlockTileEntity(xCoord+ laserDirection.offsetX* i, yCoord, zCoord+ laserDirection.offsetZ* i);
 									suctionEntity.xDirection=-1*laserDirection.offsetX;
 									suctionEntity.zDirection=-1*laserDirection.offsetZ;
 								}
 							} else {
 								return;
 							}
 						}
 					}
 
 				}
 
 			}
 		}
 
 		if (!this.worldObj.isRemote) {
 			if (this.ticks % 3 == 0 && this.playersUsing > 0) {
 				PacketManager.sendPacketToClients(this.getDescriptionPacket(),
 						this.worldObj, new Vector3(this), 12);
 			}
 		}
 
 		this.joulesStored = Math.min(this.joulesStored, this.getMaxJoules());
 		this.joulesStored = Math.max(this.joulesStored, 0d);
 	}
 
 	@Override
 	public void handlePacketData(INetworkManager inputNetwork, int type,
 			Packet250CustomPayload packet, EntityPlayer player,
 			ByteArrayDataInput dataStream) {
 		try {
 			this.drawingTicks = dataStream.readInt();
 			this.disabledTicks = dataStream.readInt();
 			this.joulesStored = dataStream.readDouble();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound par1NBTTagCompound) {
 		super.readFromNBT(par1NBTTagCompound);
 		try {
 			this.joulesStored = par1NBTTagCompound.getDouble("joulesStored");
 			this.internalId = par1NBTTagCompound.getInteger("InternalId");
 		} catch (Exception e) {
 		}
 
 		NBTTagList var2 = par1NBTTagCompound.getTagList("Items");
 
 	}
 
 	/**
 	 * Writes a tile entity to NBT.
 	 */
 	@Override
 	public void writeToNBT(NBTTagCompound par1NBTTagCompound) {
 		super.writeToNBT(par1NBTTagCompound);
 		par1NBTTagCompound.setDouble("joulesStored", this.getJoules());
 		par1NBTTagCompound.setInteger("InternalId", this.internalId);
 
 		NBTTagList var2 = new NBTTagList();
 
 		for (int var3 = 0; var3 < this.inventory.length; ++var3) {
 			if (this.inventory[var3] != null) {
 				NBTTagCompound var4 = new NBTTagCompound();
 				var4.setByte("Slot", (byte) var3);
 				this.inventory[var3].writeToNBT(var4);
 				var2.appendTag(var4);
 			}
 		}
 		par1NBTTagCompound.setTag("Items", var2);
 	}
 
 	@Override
 	public double getVoltage() {
 		return 480;
 	}
 
 	/**
 	 * @return The amount of ticks required to draw this item
 	 */
 
 	public int getDrawingTimeLeft() {
 		return this.drawingTicks;
 	}
 
 	@Override
 	public double getJoules() {
 		return this.joulesStored;
 	}
 
 	@Override
 	public void setJoules(double joules) {
 		this.joulesStored = joules;
 	}
 
 	@Override
 	public double getMaxJoules() {
 		return LaserEmitterTileEntity.maxJoules;
 	}
 
 	@Override
 	public boolean canConnect(ForgeDirection direction) {
 		return direction.ordinal() == this.getBlockMetadata() + 2;
 	}
 
 
 
 
 
 
 	public boolean onMachineActivated(World world, int x, int y, int z,
 			EntityPlayer entityPlayer, int side, float hitX, float hitY,
 			float hitZ) {
 
 		ItemStack target=entityPlayer.getHeldItem();
 		if(target!=null){
 			internalId=target.itemID;
 		}else{
 			internalId=0;
 		}
 		return true;
 	}
 
 	public boolean isValidBeam(int x, int y, int z) {
 		// TODO Auto-generated method stub
 		
 			if(worldObj.getBlockId(x, y, z)==laserBeamId){
 				if(this.getJoules()>50000)
 				return false;
 			}
 		
 		return true;
 	}
 
 	public void notifyDecay() {
 		decay=true;
 		
 	}
 
 
 
 }
