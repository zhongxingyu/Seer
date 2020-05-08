 package zensiert1997.realphysics.core.item;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.ForgeDirection;
 import zensiert1997.realphysics.core.block.BoundingBox;
 import zensiert1997.realphysics.core.models.RenderGear;
 import zensiert1997.realphysics.core.tile.BaseTileEntity.INBTCompatible;
 
 public class GearData implements INBTCompatible {
 	
 	public float radiusInnerCircle, radiusOuterCircle;
 	public int spikes;
 	public float xCoord, yCoord, zCoord;
 	public EnumAxis axis;
 	public float rotation = 0.0f;
 	
 	public GearData() {
 		this(EnumAxis.Z);
 	}
 	
 	public GearData(EnumAxis axis) {
 		this(axis, ItemGear.defaultSpikes, 0, 0, 0);
 	}
 	
 	public GearData(EnumAxis axis, int spikes) {
 		this(axis, spikes, 0, 0, 0);
 	}
 	
 	public GearData(EnumAxis axis, int spikes, float x, float y, float z) {
 		this.xCoord = x;
 		this.yCoord = y;
 		this.zCoord = z;
 		this.axis = axis;
 		this.spikes = spikes;
 		calculateRadius();
 	}
 	
 	private void calculateRadius() {
 		this.radiusInnerCircle = spikes*RenderGear.spikeRatio;
 		this.radiusOuterCircle = radiusInnerCircle+RenderGear.spikeSize;
 	}
 	
 	@Override
 	public void writeToNBT(NBTTagCompound tag) {
 		tag.setInteger("spikes", spikes);
 		tag.setFloat("rotation", rotation);
 		tag.setByte("axis", (byte) axis.ordinal());
 		tag.setFloat("xCoord", xCoord);
 		tag.setFloat("yCoord", yCoord);
 		tag.setFloat("zCoord", zCoord);
 	}
 
 	@Override
 	public void readFromNBT(NBTTagCompound tag) {
 		if(tag.hasKey("spikes")) {
 			spikes = tag.getInteger("spikes");
 		}
 		if(tag.hasKey("rotation")) {
 			rotation = tag.getFloat("rotation");
 		}
 		if(tag.hasKey("axis")) {
 			axis = EnumAxis.values()[tag.getByte("axis")];
 		}
 		if(tag.hasKey("xCoord") && tag.hasKey("yCoord") && tag.hasKey("zCoord")) {
 			xCoord = tag.getFloat("xCoord");
 			yCoord = tag.getFloat("yCoord");
 			zCoord = tag.getFloat("zCoord");
 		}
 		calculateRadius();
 	}
 	
 	public void writeToStream(DataOutputStream stream) throws IOException {
 		stream.writeInt(spikes);
 		stream.writeFloat(rotation);
 		stream.writeByte((byte) axis.ordinal());
 		stream.writeFloat(xCoord);
 		stream.writeFloat(yCoord);
 		stream.writeFloat(zCoord);
 	}
 	
 	public void readFromStream(DataInputStream stream) throws IOException {
 		spikes = stream.readInt();
 		rotation = stream.readFloat();
 		axis = EnumAxis.values()[stream.readByte()];
 		xCoord = stream.readFloat();
 		yCoord = stream.readFloat();
 		zCoord = stream.readFloat();
 		calculateRadius();
 	}
 	
 	public BoundingBox getBoundingBox() {
 		float minX, maxX, minY, maxY, minZ, maxZ;
 		if(axis == EnumAxis.Y) {
 			minX = Math.max((0.5f+xCoord)-radiusOuterCircle, 0);
 			maxX = Math.min((0.5f+xCoord)+radiusOuterCircle, 1);
 			minY = Math.max((0.5f+yCoord)-RenderGear.halfGearThickness, 0);
 			maxY = Math.min((0.5f+yCoord)+RenderGear.halfGearThickness, 1);
 			minZ = Math.max((0.5f+zCoord)-radiusOuterCircle, 0);
 			maxZ = Math.min((0.5f+zCoord)+radiusOuterCircle, 1);
 		} else if(axis == EnumAxis.X) {
 			minX = Math.max((0.5f+xCoord)-RenderGear.halfGearThickness, 0);
 			maxX = Math.min((0.5f+xCoord)+RenderGear.halfGearThickness, 1);
 			minY = Math.max((0.5f+yCoord)-radiusOuterCircle, 0);
 			maxY = Math.min((0.5f+yCoord)+radiusOuterCircle, 1);
 			minZ = Math.max((0.5f+zCoord)-radiusOuterCircle, 0);
 			maxZ = Math.min((0.5f+zCoord)+radiusOuterCircle, 1);
 		} else {
			minX = Math.max((0.5f+xCoord)-radiusOuterCircle, 0);
			maxX = Math.min((0.5f+xCoord)+radiusOuterCircle, 1);
 			minY = Math.max((0.5f+yCoord)-radiusOuterCircle, 0);
 			maxY = Math.min((0.5f+yCoord)+radiusOuterCircle, 1);
 			minZ = Math.max((0.5f+zCoord)-RenderGear.halfGearThickness, 0);
 			maxZ = Math.min((0.5f+zCoord)+RenderGear.halfGearThickness, 1);
 		}
 		return new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
 	}
 	
 	public enum EnumAxis {
 		X(0, 1, 0, 90), Y(1, 0, 0, 90), Z(0, 0, 0, 0);
 		
 		public final int vecX, vecY, vecZ, angle;
 		EnumAxis(int vecX, int vecY, int vecZ, int angle) {
 			this.vecX = vecX;
 			this.vecY = vecY;
 			this.vecZ = vecZ;
 			this.angle = angle;
 		}
 		
 		public static EnumAxis getFromForgeDirection(ForgeDirection direction) {
 			switch(direction) {
 			case DOWN: return Y;
 			case UP: return Y;
 			case EAST: return X;
 			case WEST: return X;
 			default:
 				return Z;
 			}
 		}
 	}
 }
