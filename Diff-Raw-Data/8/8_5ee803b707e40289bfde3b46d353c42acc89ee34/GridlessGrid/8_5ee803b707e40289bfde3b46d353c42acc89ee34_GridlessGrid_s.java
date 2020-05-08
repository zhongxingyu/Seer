 package net.rptools.maptool.model;
 
 import java.awt.geom.Area;
 
 public class GridlessGrid extends Grid {
 
 	private static final GridCapabilities GRID_CAPABILITIES= new GridCapabilities() {
 		public boolean isPathingSupported() {return false;}
 		public boolean isSnapToGridSupported() {return false;}
 		public boolean isPathLineSupported() {return false;}
 		public boolean isSecondDimensionAdjustmentSupported() {return false;}
 	};
 
 	
 	@Override
 	public ZonePoint convert(CellPoint cp) {
 		return new ZonePoint(cp.x, cp.y);
 	}
 	@Override
 	public CellPoint convert(ZonePoint zp) {
 		return new CellPoint(zp.x, zp.y);
 	}
 	@Override
 	protected Area createCellShape(int size) {
 		// Doesn't do this
 		return null;
 	}
 	@Override
 	public GridCapabilities getCapabilities() {
 		return GRID_CAPABILITIES;
 	}
 
 }
