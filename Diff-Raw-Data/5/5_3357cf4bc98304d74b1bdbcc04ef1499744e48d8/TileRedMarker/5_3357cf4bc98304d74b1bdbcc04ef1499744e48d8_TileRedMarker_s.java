 package mods.firstspring.advfiller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.minecraft.tileentity.TileEntity;
 import buildcraft.api.core.IAreaProvider;
 import buildcraft.builders.TileMarker;
 
 public class TileRedMarker extends TileMarker implements IAreaProvider {
 	int xmin,ymin,zmin,xmax,ymax,zmax;
 	boolean hasPosition = false;
 
 	@Override
 	public int xMin() {
 		if(hasPosition)
 			return xmin;
 		return xCoord;
 	}
 
 	@Override
 	public int yMin() {
 		if(hasPosition)
 			return ymin;
 		return yCoord;
 	}
 
 	@Override
 	public int zMin() {
 		if(hasPosition)
 			return zmin;
 		return zCoord;
 	}
 
 	@Override
 	public int xMax() {
 		if(hasPosition)
 			return xmax;
 		return xCoord;
 	}
 
 	@Override
 	public int yMax() {
 		if(hasPosition)
 			return ymax;
 		return yCoord;
 	}
 
 	@Override
 	public int zMax() {
 		if(hasPosition)
 			return zmax;
 		return zCoord;
 	}
 
 	@Override
 	public void removeFromWorld() {
 		worldObj.setBlock(xCoord, yCoord, zCoord, 0);
 		AdvFiller.redMarker.dropBlockAsItem(worldObj, xCoord, yCoord, zCoord, 0, 0);
 	}
 	
 	@Override
 	public void updateEntity(){
 		if(worldObj.isRemote)
 			return;
 		List<TileEntity> tile = getNeighborTileEntityList();
 		TileAdvFiller filler = null;
 		for(TileEntity te : tile)
 			if(te instanceof TileAdvFiller)
 				filler = (TileAdvFiller)te;
 		if(filler == null)
 			return;
 		xmin = filler.fromX;
 		ymin = filler.fromY;
 		zmin = filler.fromZ;
 		xmax = filler.toX;
 		ymax = filler.toY;
 		zmax = filler.toZ;
 	}
 	
 	public List<TileEntity> getNeighborTileEntityList(){
 		ArrayList<TileEntity> list = new ArrayList();
 		if(worldObj == null)
 			return list;
 		TileEntity[] tile = new TileEntity[6];
 		tile[0] = worldObj.getBlockTileEntity(xCoord-1, yCoord, zCoord);
 		tile[1] = worldObj.getBlockTileEntity(xCoord+1, yCoord, zCoord);
 		tile[2] = worldObj.getBlockTileEntity(xCoord, yCoord-1, zCoord);
 		tile[3] = worldObj.getBlockTileEntity(xCoord, yCoord+1, zCoord);
 		tile[4] = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord+1);
 		tile[5] = worldObj.getBlockTileEntity(xCoord, yCoord, zCoord-1);
 		for(TileEntity tileBuf:tile)
			if(tileBuf != null)
 				list.add(tileBuf);
 		return list;
 	}
 
 }
