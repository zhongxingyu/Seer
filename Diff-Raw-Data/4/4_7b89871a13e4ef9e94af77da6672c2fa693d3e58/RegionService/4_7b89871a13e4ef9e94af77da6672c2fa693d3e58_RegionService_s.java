 package org.dawb.common.ui.plot.region;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 
 import uk.ac.diamond.scisoft.analysis.roi.CircularFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.FreeDrawROI;
 import uk.ac.diamond.scisoft.analysis.roi.GridROI;
 import uk.ac.diamond.scisoft.analysis.roi.IROI;
 import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
 import uk.ac.diamond.scisoft.analysis.roi.PerimeterBoxROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolygonalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 import uk.ac.diamond.scisoft.analysis.roi.RingROI;
 import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
 import uk.ac.diamond.scisoft.analysis.roi.XAxisBoxROI;
 import uk.ac.diamond.scisoft.analysis.roi.XAxisLineBoxROI;
 import uk.ac.diamond.scisoft.analysis.roi.YAxisBoxROI;
 import uk.ac.diamond.scisoft.analysis.roi.YAxisLineBoxROI;
 
 public class RegionService {
 
 	private static Map<Object,Object> roiMap;
 	
 	private static Map<Object,Object> getRoiMap() {
 		if (roiMap!=null) return roiMap;
 		roiMap = new HashMap<Object,Object>(7);
 		roiMap.put(RegionType.LINE,          LinearROI.class);
 		roiMap.put(RegionType.POLYLINE,      PolylineROI.class);
 		roiMap.put(RegionType.POLYGON,       PolygonalROI.class);
 		roiMap.put(RegionType.BOX,           RectangularROI.class);
 		roiMap.put(RegionType.PERIMETERBOX,  PerimeterBoxROI.class);
 		roiMap.put(RegionType.GRID,          GridROI.class);
 		roiMap.put(RegionType.CIRCLE,        CircularROI.class);
 		roiMap.put(RegionType.CIRCLEFIT,     CircularFitROI.class);
 		roiMap.put(RegionType.SECTOR,        SectorROI.class);
 		roiMap.put(RegionType.POINT,         PointROI.class);
 		roiMap.put(RegionType.ELLIPSE,       EllipticalROI.class);
 		roiMap.put(RegionType.ELLIPSEFIT,    EllipticalFitROI.class);
 		roiMap.put(RegionType.RING,          RingROI.class);
 		roiMap.put(RegionType.XAXIS,         XAxisBoxROI.class);
 		roiMap.put(RegionType.YAXIS,         YAxisBoxROI.class);
 		roiMap.put(RegionType.XAXIS_LINE,    XAxisLineBoxROI.class);
 		roiMap.put(RegionType.YAXIS_LINE,    YAxisLineBoxROI.class);
 		roiMap.put(RegionType.FREE_DRAW,     FreeDrawROI.class);
 
 		// Goes both ways.
		for (Object key : roiMap.keySet()) {
 			roiMap.put(roiMap.get(key), key);
 		}
 		return roiMap;
 	}
 	
 	public static final RegionType forROI(IROI iroi) {
 		return (RegionType)getRoiMap().get(iroi.getClass());
 	}
 	public static RegionType getRegion(Class<? extends IROI> clazz) {
 		return (RegionType)getRoiMap().get(clazz);
 	}
 	
 	/**
 	 * Method attempts to make the best IRegion it
 	 * can for the ROI.
 	 * 
 	 * @param plottingSystem
 	 * @param roi
 	 * @param roiName
 	 * @return
 	 */
 	public static IRegion createRegion( final IPlottingSystem plottingSystem,
 										final IROI            roi, 
 										final String          roiName) throws Exception {
 
 		IRegion region = plottingSystem.getRegion(roiName);
 		if (region != null && region.isVisible()) {
 			region.setROI(roi);
 			return region;
 		} 
 		
 		RegionType type = null;
 		if (roi instanceof LinearROI) {
 			type = RegionType.LINE;
 			
 		} else if (roi instanceof RectangularROI) {
 			if (roi instanceof PerimeterBoxROI) {
 				type = RegionType.PERIMETERBOX;
 			} else if (roi instanceof XAxisBoxROI){
 				type = RegionType.XAXIS;
 			} else if (roi instanceof YAxisBoxROI){
 				type = RegionType.YAXIS;
 			} else {
 				type = RegionType.BOX;
 			}
 		
 		} else if (roi instanceof SectorROI) {
 			if(roi instanceof RingROI){
 				type = RegionType.RING;
 			} else {
 				type = RegionType.SECTOR;
 			}
 		} else if (roi instanceof CircularROI) {
 			type = RegionType.CIRCLE;
 			
 		} else if (roi instanceof CircularFitROI) {
 			type = RegionType.CIRCLEFIT;
 			
 		} else if (roi instanceof EllipticalROI) {
 			type = RegionType.ELLIPSE;
 			
 		} else if (roi instanceof EllipticalFitROI) {
 			type = RegionType.ELLIPSEFIT;
 			
 		} else if (roi instanceof PointROI) {
 			type = RegionType.POINT;
 
 		}
 		
 		if (type==null) return null;
 		
 		IRegion newRegion = plottingSystem.createRegion(roiName, type);
 		newRegion.setROI(roi);
 		plottingSystem.addRegion(newRegion);
 
 		return newRegion;
 
 	}
 
 		
 
 }
