 package org.dawb.common.ui.plot.axis;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.ui.plot.IPlottingSystem;
 
 public class AxisUtils {
 
 	public static String getUniqueAxisTitle(String title, IPlottingSystem system) {
 		String sugTitle = title;
 		int iTitle      = 0;
 		while (!isAxisUnique(sugTitle, system)) {
 			iTitle++;
 			sugTitle = title+iTitle;
 		}
 		return sugTitle;
 	}
 
 	public static boolean isAxisUnique(String title, IPlottingSystem system) {
 		final List<IAxis> axes = system.getAxes();
 		for (IAxis ia : axes) {
 			if (ia.getTitle()!=null && ia.getTitle().equals(title)) {
 			    return false;
 			}
  		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @return Non-primary axes
 	 */
 	public static List<IAxis> getUserAxes(IPlottingSystem system) {
 		final List<IAxis>  axes = system.getAxes();
 		final List<IAxis> avail = new ArrayList<IAxis>(axes.size());
 		for (IAxis axis : axes) {
			if (axis.isPrimaryAxis()) continue;
 			avail.add(axis);
 		}
 		return avail;
 	}
 
 }
