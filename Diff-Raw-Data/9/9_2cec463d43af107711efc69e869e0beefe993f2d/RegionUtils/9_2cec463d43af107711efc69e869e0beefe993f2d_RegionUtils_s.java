 package org.dawnsci.plotting.api.region;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.region.IRegion.RegionType;
 import org.eclipse.swt.graphics.Color;
 
 /**
  * Class containing utility methods for regions to avoid duplication 
  * @author fcp94556
  *
  */
 public class RegionUtils {
 
 	/**
 	 * Call to get a unique region name 
 	 * @param nameStub
 	 * @param system
 	 * @return
 	 */
 	public static String getUniqueName(final String nameStub, final IPlottingSystem system, final String... usedNames) {
 		int i = 1;
 		@SuppressWarnings("unchecked")
 		final List<String> used = (List<String>) (usedNames!=null ? Arrays.asList(usedNames) : Collections.emptyList());
 		while(system.getRegion(nameStub+" "+i)!=null || used.contains(nameStub+" "+i)) {
 			++i;
 			if (i>10000) break; // something went wrong!
 		}
 		return nameStub+" "+i;
 	}
 
 	/**
 	 * 
 	 * @param plotter
 	 * @return
 	 */
	public static Color getUnqueColor(IRegion.RegionType type, IPlottingSystem plotter, Collection<Color> colours) {
 
 		final Collection<Color> used = new HashSet<Color>(7);
 		for (IRegion reg : plotter.getRegions()) {
 			if (reg.getRegionType()!=type) continue;
 			used.add(reg.getRegionColor());
 		}
         
 		for (Color color : colours) {
 			if (!used.contains(color)) return color;
 		}
 		return colours.iterator().next();
 	}
 
 	/**
 	 * Creates a region (first deleting it if one with that name exists)
 	 * @param string
 	 * @param xaxis
 	 * @return
 	 * @throws Exception 
 	 */
 	public static final IRegion replaceCreateRegion(final IPlottingSystem system, 
 			                                        final String          name, 
 			                                        final RegionType      type) throws Exception {
 		
 		if (system.getRegion(name)!=null) {
 			system.removeRegion(system.getRegion(name));
 		}
 		return system.createRegion(name, type);
 	}
 	
 	/**
 	 * Method attempts to make the best IRegion it
 	 * can for the ROI.
 	 * 
 	 * @param plottingSystem
 	 * @param roi
 	 * @param roiName
 	 * @return
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
 	 */
 		
 
 }
