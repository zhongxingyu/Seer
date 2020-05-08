 package org.dawb.workbench.plotting.tools;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.workbench.plotting.Activator;
 import org.dawb.workbench.plotting.preference.FittingConstants;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.IAnalysisMonitor;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
import uk.ac.diamond.scisoft.analysis.fitting.functions.IPeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Lorentzian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.PearsonVII;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.PseudoVoigt;
 import uk.ac.diamond.scisoft.analysis.optimize.GeneticAlg;
 import uk.ac.diamond.scisoft.analysis.optimize.IOptimizer;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 public class FittingUtils {
 
 	private static Logger logger = LoggerFactory.getLogger(FittingUtils.class);
 	
 	/**
 	 * This method runs a fit on data passed in. It reads preferences to 
 	 * determine which algorithm etc. the user prefers. 
 	 * 
 	 * It can take a while to run. Callers of these utils should protect the
 	 * call by running it from a Job. This method is UI thread safe and may be called
 	 * in any thread.
 	 * 
 	 * The x and y data may have been sliced if the user has completed a selection choice
 	 * which limits the peak fit range.
 	 * 
 	 * The RegionBounds returns are not computed in y. Instead the peak fitter provides the
 	 * y values as zero. So XAXIS regions should be created, then Y data is not required.
 	 * 
 	 * The method also computes x,y abstract data set pairs which are plot fragments for the
 	 * function plotted over the peak region. These can be plotted to show the user the
 	 * fitted function, which they may wish to adjust.
 	 * 
 	 * @param x
 	 * @param y
 	 * @param monitor
 	 * @return
 	 */
 	public static FittedPeaks getFittedPeaks(final AbstractDataset  x, 
 			                                     final AbstractDataset  y,
 			                                     final IProgressMonitor monitor) {
 				
 		final IOptimizer optimizer = getOptimizer();
		final List<CompositeFunction> composites =  Generic1DFitter.fitPeaks(x, y, getPeakType(), optimizer, getSmoothing(), getPeaksRequired(), 0.0, false, false, new IAnalysisMonitor() {
 			@Override
 			public boolean hasBeenCancelled() {
 				return monitor.isCanceled(); // We always use the monitor.isCancelled() the fitting can take a while
 				                             // and should always allow stopping.
 			}
 		});
 		
		if (composites==null || composites.isEmpty()) return null;
				
 		final FittedPeaks bean = new FittedPeaks();
		for (CompositeFunction function : composites) {
 			
			final IPeak peak = function.getPeak(0);
 			if (monitor.isCanceled()) return null;
 			double w = peak.getFWHM();
 			RectangularROI bounds = new RectangularROI(peak.getPosition() - w/2, 0, w, 0, 0);
 			
			final AbstractDataset[] pf = getPeakFunction(x, y, function);
 			
 			bean.addFittedPeak(new FittedPeak(peak, bounds, pf));
 
 		}
 		
 		bean.setOptimizer(optimizer);
 		return bean;
 	}
 	
 	private static int getPeaksRequired() {
 		return Activator.getDefault().getPreferenceStore().getInt(FittingConstants.PEAK_NUMBER);
 	}
 
 	/**
 	 * 
 	 * @param x
 	 * @param peak
 	 * @return
 	 */
	private static AbstractDataset[] getPeakFunction(AbstractDataset x, final AbstractDataset y, CompositeFunction peak) {
 
 //		double min = peak.getPosition() - (peak.getFWHM()); // Quite wide
 //		double max = peak.getPosition() + (peak.getFWHM());
 //
 //		final AbstractDataset[] a = xintersection(x,y,min,max);
 //		x=a[0];
 //		y=a[1];
 		
//		CompositeFunction function = new CompositeFunction();
 //		Offset os = new Offset(y.min().doubleValue(), y.max().doubleValue());
//		function.addFunction(peak);
 //		function.addFunction(os);
 		
 		// We make an x dataset with n times the points of the real data to get a smooth
 		// fitting function
 		final int    factor = Activator.getDefault().getPreferenceStore().getInt(FittingConstants.FIT_SMOOTH_FACTOR);
 		final double xmin = x.min().doubleValue();
 		final double xmax = x.max().doubleValue();
 		final double step = (xmax-xmin)/(x.getSize()*factor);
 		x = AbstractDataset.arange(xmin, xmax, step, AbstractDataset.FLOAT64);
 
		return new AbstractDataset[]{x,peak.makeDataset(x)};
 		
 	}
 
     /**
      * TODO
      * @return
      */
 	private static int getSmoothing() {
 		return Activator.getDefault().getPreferenceStore().getInt(FittingConstants.SMOOTHING);
 	}
 
 	private static IOptimizer getOptimizer() {
 		return new GeneticAlg(getQuality());
 	}
 
 	/**
 	 * TODO
 	 * @return
 	 */
 	private static double getQuality() {
 		return Activator.getDefault().getPreferenceStore().getDouble(FittingConstants.QUALITY);
 	}
 
 	private static APeak getPeakType() {
 		try {
 			
 			final String peakClass = Activator.getDefault().getPreferenceStore().getString(FittingConstants.PEAK_TYPE);
 			
 			/**
 			 * Could use reflection to save on objects, but there's only 4 of them.
 			 */
 			return getPeakOptions().get(peakClass);
 			
 		} catch (Exception ne) {
 			logger.error("Cannot determine peak type required!", ne);
 			Activator.getDefault().getPreferenceStore().setValue(FittingConstants.PEAK_TYPE, Gaussian.class.getName());
 		    return new Gaussian(1, 1, 1, 1);
 		}
 	}
 
 	/**
 	 * Slices x and y using the x as the reference.
 	 * @param x
 	 * @param y - may be null
 	 * @param startValue
 	 * @param endValue
 	 * @return x and y sliced to the startValue and endValue
 	 */
 	public static AbstractDataset[] xintersection(AbstractDataset x,
 			                                      AbstractDataset y, 
 			                                      final double startValue, 
 			                                      final double endValue) {
 		
 		List<Double> cross = DatasetUtils.crossings(x, startValue);		
 		final int    start = cross==null || cross.isEmpty() 
 				           ? 0
 				           : (int)Math.floor(cross.get(0)); // Lower value
 		
 		cross = DatasetUtils.crossings(x, endValue);		
 		final int    stop  =  cross==null || cross.isEmpty() 
 				           ? x.getSize()-1
 				           : (int)Math.ceil(cross.get(cross.size()-1)); // Upper value
 		
 		x = x.getSlice(new int[] { start }, new int[] { stop }, null);
 		if (y!=null) y = y.getSlice(new int[] { start }, new int[] { stop }, null);		
 		
 		return (y!=null) ? new AbstractDataset[]{x,y} : new AbstractDataset[]{x};
 	}
 	
 	
 	public static Map<String, APeak> getPeakOptions() {
 		final Map<String, APeak> opts = new LinkedHashMap<String, APeak>(4);
 		opts.put(Gaussian.class.getName(),    new Gaussian(1, 1, 1, 1));
 		opts.put(Lorentzian.class.getName(),  new Lorentzian(1, 1, 1, 1));
 		opts.put(PearsonVII.class.getName(),  new PearsonVII(1, 1, 1, 1));
 		opts.put(PseudoVoigt.class.getName(), new PseudoVoigt(1, 1, 1, 1));
 		return opts;
 	}
 }
