 /*-
  * Copyright 2013 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.diffraction;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.TreeSet;
 
 import javax.measure.unit.SI;
 
 import org.apache.commons.math.ConvergenceException;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
 import org.apache.commons.math.analysis.MultivariateMatrixFunction;
 import org.apache.commons.math.analysis.MultivariateVectorialFunction;
 import org.apache.commons.math.optimization.VectorialPointValuePair;
 import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
 import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.uncommons.maths.combinatorics.CombinationGenerator;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.BooleanIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.Comparisons;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 import uk.ac.diamond.scisoft.analysis.fitting.Generic1DFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitFunction;
 import uk.ac.diamond.scisoft.analysis.fitting.IConicSectionFitter;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 import uk.ac.diamond.scisoft.analysis.roi.CircularROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalFitROI;
 import uk.ac.diamond.scisoft.analysis.roi.EllipticalROI;
 import uk.ac.diamond.scisoft.analysis.roi.PointROI;
 import uk.ac.diamond.scisoft.analysis.roi.PolylineROI;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Utilities to fit powder rings
  */
 public class PowderRingsUtils {
 	private static Logger logger = LoggerFactory.getLogger(PowderRingsUtils.class);
 
 //	/**
 //	 * Given two (fitted) rings, which have parallel major axes, calculate detector distance
 //	 * and beam angle relative to detector normal if angles are known
 //	 * @param innerAxis     length on major axis of inner ring
 //	 * @param firstGap      gap between outer and inner ring
 //	 * @param secondGap     gap between inner and outer ring
 //	 * @param innerTwoTheta scattering angle for inner ring
 //	 * @param outerTwoTheta scattering angle for outer ring
 //	 */
 //	public static DetectorProperties calculateDetectorProperties(double innerAxis, double firstGap, double secondGap, double innerTwoTheta, double outerTwoTheta) {
 //
 //		DetectorProperties prop = new DetectorProperties();
 //		return prop;
 //	}
 
 	/*
 	 * Given circle in image
 	 *  - generate radii/spokes so they are spaced a few pixels apart on circle
 	 *  - find peak centres on spokes near circle if within image
 	 *  - fit ellipse to those centres
 	 *  - repeat for larger ellipses by scanning along longest spoke
 	 */
 
 	private static final double ARC_LENGTH = 8;
 	private static final double RADIAL_DELTA = 10;
 	private static final int MAX_POINTS = 200;
 
 	private static final int PEAK_SMOOTHING = 3;
 	private static final double MAX_FWHM_FACTOR = 2;
 	private static final double RING_SEPARATION = 4;
 
 	public static PolylineROI findPOIsNearCircle(IMonitor mon, AbstractDataset image, BooleanDataset mask, CircularROI circle) {
 		return findPOIsNearCircle(mon, image, mask, circle, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
 	public static PolylineROI findPOIsNearCircle(IMonitor mon, AbstractDataset image, BooleanDataset mask, CircularROI circle,
 			double arcLength, double radialDelta, int maxPoints) {
 		return findPOIsNearEllipse(mon, image, mask, new EllipticalROI(circle), arcLength, radialDelta, maxPoints);
 	}
 
 	public static PolylineROI findPOIsNearEllipse(IMonitor mon, AbstractDataset image, BooleanDataset mask, EllipticalROI ellipse) {
 		return findPOIsNearEllipse(mon, image, mask, ellipse, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
	
	// global lower threshold for pixel values is determined by linear interpolation of given fraction between image mean and max
	private static final double MEAN_TO_MAX_FRACTION = 0.999;

 	/**
 	 * Find a set of points of interests near given ellipse from an image.
 	 * <p>
 	 * The ellipse is divided into sub-areas and these POIs are considered to
 	 * be the locations of maximum pixel intensities found within those sub-areas.
 	 * @param mon
 	 * @param image
 	 * @param mask (can be null)
 	 * @param ellipse
 	 * @param arcLength step size along arc in pixels
 	 * @param radialDelta +/- value to define area to search
 	 * @param maxPoints maximum number of points to return ( 
 	 * @return polyline ROI
 	 */
 	public static PolylineROI findPOIsNearEllipse(IMonitor mon, AbstractDataset image, BooleanDataset mask, EllipticalROI ellipse,
 			double arcLength, double radialDelta, int maxPoints) {
 		if (image.getRank() != 2) {
 			logger.error("Dataset must have two dimensions");
 			throw new IllegalArgumentException("Dataset must have two dimensions");
 		}
 		if (mask != null && !image.isCompatibleWith(mask)) {
 			logger.error("Mask must match image shape");
 			throw new IllegalArgumentException("Mask must match image shape");
 		}
 
 		final double aj = ellipse.getSemiAxis(0);
 		final double an = ellipse.getSemiAxis(1);
 		if (an < arcLength) {
 			logger.error("Ellipse/circle is too small");
 			throw new IllegalArgumentException("Ellipse/circle is too small");
 		}
 
 		final double xc = ellipse.getPointX();
 		final double yc = ellipse.getPointY();
 		final double ang = ellipse.getAngle();
 		final double ca = Math.cos(ang);
 		final double sa = Math.sin(ang);
 		final int[] shape = image.getShape();
 		final int h = shape[0];
 		final int w = shape[1];
 
 		final double pdelta = arcLength / aj; // change in angle
 		double rdelta = radialDelta; // semi-width of annulus of interest
 		if (rdelta < 1) {
 			logger.warn("Radial delta was set too low: setting to 1");
 			rdelta = 1;
 		}
 		final double rsj = aj - rdelta;
 		final double rej = aj + rdelta;
 		final double rsn = an - rdelta;
 		final double ren = an + rdelta;
 
 		final int imax = (int) Math.ceil(Math.PI * 2. / pdelta);
 
 		logger.debug("Major semi-axis = [{}, {}]; {}", new Object[] { rsj, rej, imax });
 		final int[] start = new int[2];
 		final int[] stop = new int[2];
 		final int[] step = new int[] { 1, 1 };
 		HashSet<PointROI> pointSet = new HashSet<PointROI>();
 		for (int i = 0; i < imax; i++) {
 			double p = i * pdelta;
 			double cp = Math.cos(p);
 			double sp = Math.sin(p);
 			AbstractDataset sub;
 			final int[] beg = new int[] { (int) (yc + rsj * sa * cp + rsn * ca * sp),
 					(int) (xc + rsj * ca * cp - rsn * sa * sp) };
 			final int[] end = new int[] { (int) (yc + rej * sa * cp + ren * ca * sp),
 					(int) (xc + rej * ca * cp - ren * sa * sp) };
 			start[0] = Math.max(0, Math.min(beg[0], end[0]));
 			stop[0] = Math.min(h, Math.max(beg[0], end[0]));
 			if (start[0] == stop[0]) {
 				if (stop[0] == h) {
 					start[0]--;
 				} else {
 					stop[0]++;
 				}
 			} else if (start[0] > stop[0] || start[0] >= h) {
 				continue;
 			} else {
 				stop[0] = Math.min(Math.max(stop[0], start[0] + (int) radialDelta), h);
 			}
 			start[1] = Math.max(0, Math.min(beg[1], end[1]));
 			stop[1] = Math.min(w, Math.max(beg[1], end[1]));
 			if (start[1] == stop[1]) {
 				if (stop[1] == w) {
 					start[1]--;
 				} else {
 					stop[1]++;
 				}
 			} else if (start[1] > stop[1] || start[1] >= w) {
 				continue;
 			} else {
 				stop[1] = Math.min(Math.max(stop[1], start[1] + (int) radialDelta), w);
 			}
 			sub = image.getSlice(start, stop, step);
 
 			int[] pos = sub.maxPos();
			if (mask != null) {
				pos[0] += start[0];
				pos[1] += start[1];
 
 				if (mask.get(pos)) {
 					AbstractDataset sorted = DatasetUtils.sort(sub.flatten(), null);
 					int l = sorted.getSize() - 1;
 					do {
 						double x = sorted.getElementDoubleAbs(l);
 						pos = sub.getNDPosition(DatasetUtils.findIndexEqualTo(sub, x));
 						pos[0] += start[0];
 						pos[1] += start[1];
 					} while (!mask.get(pos) && --l >= 0);
 					if (l < 0) {
 						logger.warn("Could not find unmasked value for slice!");
 					} else {
 						pointSet.add(new PointROI(pos[1], pos[0]));
 					}
 				}
 			} else {
 //			System.err.printf("Slice: %s, %s has max at %s\n", Arrays.toString(start), Arrays.toString(stop), Arrays.toString(pos));
				pointSet.add(new PointROI(pos[1]+start[1], pos[0]+start[0]));
 			}
 
 			if (mon != null)
 				mon.worked(1);
 		}
 
 		// analyse pixel values
 		int n = pointSet.size();
 		double[] values = new double[n];
 		int i = 0;
 		for (PointROI p : pointSet) {
 			int[] pos = p.getIntPoint();
 			values[i++] = image.getDouble(pos[1], pos[0]);
 		}
 
 		DoubleDataset pixels = new DoubleDataset(values);
 		System.err.println(pixels);
 
 		// threshold with population stats from maxima
 		logger.debug("Stats: {} {} {} {}", new Object[] {pixels.min(), pixels.mean(), pixels.max(),
 				Arrays.toString(Stats.quantile(pixels, 0.25, 0.5, 0.75))});
 
 		double threshold;
 		if (n > maxPoints) {
 			threshold = Stats.quantile(pixels, 1 - maxPoints/(double) n);
 			logger.debug("Threshold: {} setting for highest {}", threshold, maxPoints);
 		} else {
 			double iqr = (Double) Stats.iqr(pixels);
 			threshold = (Double) pixels.mean() - 2*iqr;
 			double min = pixels.min().doubleValue();
 			while (threshold < min) {
 				threshold += iqr;
 			}
 			logger.debug("Threshold: {} setting by mean - 2IQR", threshold);
 		}
 
		double gThreshold = MEAN_TO_MAX_FRACTION*((Double) image.mean()) + (1-MEAN_TO_MAX_FRACTION)*image.max().doubleValue();
		logger.debug("Threshold: global (mean-based) is {}", gThreshold);
		if (threshold < gThreshold) {
			threshold = gThreshold;
			logger.debug("Threshold: too low, now {} setting by image mean", threshold);
		}

 		PolylineROI polyline = new PolylineROI();
 		if (threshold > (Double) pixels.min()) {
 			for (PointROI p : pointSet) {
 				int[] pos = p.getIntPoint();
 				double v = image.getDouble(pos[1], pos[0]);
 				if (v >= threshold) {
 //					System.err.printf("Adding %f %s\n", v, Arrays.toString(pos));
 					polyline.insertPoint(p);
 //				} else {
 //					System.err.println("Rejecting " + p + " = " + v);
 				}
 			}
 		} else {
 			for (PointROI p : pointSet) {
 				polyline.insertPoint(p);
 			}
 		}
 		if (mon != null)
 			mon.worked(polyline.getNumberOfPoints());
 
 		logger.debug("Used {} of {} pixels", polyline.getNumberOfPoints(), pointSet.size());
 
 		return polyline;
 	}
 
 	public static EllipticalFitROI fitAndTrimOutliers(IMonitor mon, PolylineROI points, boolean circleOnly) {
 		return fitAndTrimOutliers(mon, points, RADIAL_DELTA, circleOnly);
 	}
 
 	/**
 	 * Fit an ellipse to given points, trim points if they fall outside given distance
 	 * and re-fit
 	 * @param points
 	 * @param trimDelta trim distance
 	 * @param circleOnly if true, then fit a circle
 	 * @return fitted ellipse
 	 */
 	public static EllipticalFitROI fitAndTrimOutliers(IMonitor mon, PolylineROI points, double trimDelta, boolean circleOnly) {
 		try {
 			EllipticalFitROI efroi = new EllipticalFitROI(points, circleOnly);
 
 			PolylineROI cpts = points;
 			int n = cpts.getNumberOfPoints();
 			IConicSectionFitter f = efroi.getFitter();
 			IConicSectionFitFunction fn = f.getFitFunction(null, null);
 
 			AbstractDataset d = fn.calcDistanceSquared(f.getParameters());
 
 			// find outliers
 			double h = trimDelta * trimDelta;
 			double ds = d.max().doubleValue();
 			logger.debug("Range: [0, {}] cf [{}, {}, {}]", new Object[] { h, d.min(), d.mean(), d.max() });
 			if (ds < h)
 				return efroi;
 
 			BooleanDataset b = Comparisons.lessThanOrEqualTo(d, h);
 			BooleanIterator it = d.getBooleanIterator(b, true);
 			PolylineROI npts = new PolylineROI();
 			while (it.hasNext()) {
 				npts.insertPoint(cpts.getPoint(it.index));
 			}
 			int m = npts.getNumberOfPoints();
 			if (m < n) {
 				logger.debug("Found some outliers: {}/{}", n - m, n);
 				efroi.setPoints(npts);
 				if (mon != null)
 					mon.worked(m);
 			}
 
 			return efroi;
 		} catch (Exception e) {
 			logger.error("Problem with trimming: {}", e);
 			throw new IllegalArgumentException("Problem!: ", e);
 		}
 	}
 
 	/**
 	 * Find other ellipses from given ellipse and image.
 	 * <p>
 	 * This is done by looking at the box profile along spokes from the
 	 * given centre (from a minimum distance of the minor semi-axis) and finding peaks.
 	 * Then the distance out to those peaks is used to search for more POIs and so more ellipses
 	 * @param image
 	 * @param mask (can be null)
 	 * @param roi initial ellipse
 	 * @return list of ellipses
 	 */
 	public static List<EllipticalROI> findOtherEllipses(IMonitor mon, AbstractDataset image, BooleanDataset mask, EllipticalROI roi) {
 		return findOtherEllipses(mon, image, mask, roi, roi.getSemiAxis(1), RADIAL_DELTA, ARC_LENGTH, RADIAL_DELTA, MAX_POINTS);
 	}
 
 	/**
 	 * Find other ellipses from given ellipse and image.
 	 * <p>
 	 * This is done by looking at the box profile along spokes from the
 	 * given centre and finding peaks. Then the distance out to those peaks is used
 	 * to search for more POIs and so more ellipses
 	 * @param image
 	 * @param mask (can be null)
 	 * @param roi initial ellipse
 	 * @param radialMin
 	 * @param radialDelta
 	 * @param arcLength
 	 * @param trimDelta
 	 * @param maxPoints
 	 * @return list of ellipses
 	 */
 	public static List<EllipticalROI> findOtherEllipses(IMonitor mon, AbstractDataset image, BooleanDataset mask, EllipticalROI roi,
 			double radialMin, double radialDelta, double arcLength, double trimDelta, int maxPoints) {
 		if (image.getRank() != 2) {
 			logger.error("Dataset must have two dimensions");
 			throw new IllegalArgumentException("Dataset must have two dimensions");
 		}
 		if (mask != null && !image.isCompatibleWith(mask)) {
 			logger.error("Mask must match image shape");
 			throw new IllegalArgumentException("Mask must match image shape");
 		}
 
 		// explore all corners
 		final int[] shape = image.getShape();
 		final int h = shape[0];
 		final int w = shape[1];
 		double[] ec = roi.getPoint();
 		TreeSet<Double> majors = new TreeSet<Double>();
 
 		// TODO farm this out across several threads
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, 0 - ec[0], 0 - ec[1]); // TL
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, w - ec[0], 0 - ec[1]); // TR
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, w - ec[0], h - ec[1]); // BR
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, 0 - ec[0], h - ec[1]); // BL
 
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, 0, h - ec[1]); // T
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, w - ec[0], 0); // R
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, 0, 0 - ec[1]); // B
 		findMajorAxes(mon, majors, image, mask, roi, radialMin, radialDelta, ec, 0 - ec[0], 0); // L
 
 		// and finally find POIs
 		List<EllipticalROI> ells = new ArrayList<EllipticalROI>();
 		double major = roi.getSemiAxis(0);
 		double aspect = roi.getSemiAxis(0)/roi.getSemiAxis(1);
 		double last = Double.NEGATIVE_INFINITY;
 		for (double a : majors) {
 			System.err.println("Current " + a + ", last " + last);
 			if (a < last) {
 				System.err.println("Dropped as less than last");
 				continue;
 			}
 			if (Math.abs(a - last) < RING_SEPARATION) { // omit close rings
 				last = a;
 				System.err.println("Dropped as too close");
 				continue;
 			}
 			if (Math.abs(a - major) < RING_SEPARATION) {
 				last = major;
 				System.err.println("Add original");
 				ells.add(roi);
 			} else {
 				EllipticalROI er = new EllipticalROI(a, a/aspect, roi.getAngle(), ec[0], ec[1]);
 				try {
 					PolylineROI polyline = findPOIsNearEllipse(mon, image, mask, er, arcLength, radialDelta, maxPoints);
 					if (polyline.getNumberOfPoints() > 2) {
 						er = fitAndTrimOutliers(mon, polyline, trimDelta, roi.isCircular());
 						if (Math.abs(er.getSemiAxis(0) - last) < RING_SEPARATION) { // omit close rings
 							last = a;
 							System.err.println("Dropped as fit is too close");
 							continue;
 						}
 						double[] c = er.getPointRef();
 						if (Math.hypot(c[0] - ec[0], c[1] - ec[1]) > 8*radialDelta) {
 							last = a; // omit fits with far-off centres
 							System.err.println("Dropped as centre is far-off");
 							continue;
 						}
 						last = Math.max(a, er.getSemiAxis(0));
 						ells.add(er);
 					} else {
 						logger.warn("Could not find enough points at {}", er);
 					}
 				} catch (IllegalArgumentException e) {
 					logger.debug("Problem with {}", er, e);
 					last = a;
 				}
 				if (mon != null)
 					mon.worked(1);
 			}
 		}
 
 		return ells;
 	}
 
 	/**
 	 * Find major axes by looking along thick line given by relative coordinates to centre for
 	 * maximum intensity values
 	 * @param mon
 	 * @param axes
 	 * @param image
 	 * @param mask
 	 * @param roi
 	 * @param offset minimum position of peaks
 	 * @param width of line
 	 * @param centre
 	 * @param dx
 	 * @param dy
 	 */
 	private static void findMajorAxes(IMonitor mon, TreeSet<Double> axes, AbstractDataset image, AbstractDataset mask, EllipticalROI roi, double offset, double width, double[] centre, double dx, double dy) {
 		RectangularROI rroi = new RectangularROI();
 		rroi.setPoint(centre);
 		rroi.setAngle(Math.atan2(dy, dx));
 		rroi.setLengths(Math.hypot(dx, dy), width);
 		rroi.translate(0, -0.5);
 		rroi.setClippingCompensation(true);
 		AbstractDataset profile = ROIProfile.maxInBox(image, mask, rroi)[0];
 		List<IdentifiedPeak> peaks = Generic1DFitter.findPeaks(AbstractDataset.arange(profile.getSize(), AbstractDataset.INT), profile, PEAK_SMOOTHING);
 		if (mon != null)
 			mon.worked(profile.getSize());
 
 		DescriptiveStatistics stats = new DescriptiveStatistics();
 		for (IdentifiedPeak p : peaks) {
 			if (p.getPos() < offset) {
 				continue;
 			}
 			stats.addValue(p.getArea());
 //			System.err.printf("P %f A %f W %f H %f\n", p.getPos(), p.getArea(), p.getFWHM(), p.getHeight());
 		}
 		
 		double area = stats.getMean() + 1.5*(stats.getPercentile(75) - stats.getPercentile(25));
 		logger.debug("Area: {}", stats);
 		logger.debug("Minimum threshold: {}", area);
 
 		double majorFactor = roi.getSemiAxis(0)/roi.getDistance(rroi.getAngle());
 		double maxFWHM = MAX_FWHM_FACTOR*width;
 		for (IdentifiedPeak p : peaks) {
 			double l = p.getPos();
 			if (l < offset) {
 				continue;
 			}
 //			System.err.println(p);
 			// filter on area and FWHM
 			if (p.getFWHM() > maxFWHM) {
 				continue;
 			}
 			if (p.getArea() < area) {
 				break;
 			}
 			axes.add(l*majorFactor);
 		}
 		if (mon != null)
 			mon.worked(peaks.size());
 
 	}
 
 	/**
 	 * @param mon
 	 * @param detector
 	 * @param env
 	 * @param ellipses
 	 * @param spacings
 	 *            a list of possible spacings
 	 * @return q-space
 	 */
 	public static QSpace fitEllipsesToQSpace(IMonitor mon, DetectorProperties detector, DiffractionCrystalEnvironment env, List<EllipticalROI> ellipses, List<HKL> spacings) {
 
 		int n = ellipses.size();
 
 		double dmin = spacings.get(0).getD().doubleValue(SI.MILLIMETRE);
 		{
 			double rmin = detector.getDetectorDistance() * Math.tan(2.0 * Math.asin(0.5 * env.getWavelength() * 1e-7 / dmin)) / detector.getVPxSize();
 			int l = 0;
 			for (EllipticalROI e : ellipses) {
 				if (e.getSemiAxis(0) > rmin)
 					break;
 				l++;
 			}
 
 			if (l >= n) {
 				throw new IllegalArgumentException("Maybe all rings are too small!");
 			}
 			if (l > 0) {
 				logger.debug("Discarding first {} rings", l);
 				ellipses = ellipses.subList(l, n);
 				n = ellipses.size();
 			}
 		}
 
 		if (n >= spacings.size()) { // always allow a choice to be made
 			int l = n - spacings.size();
 			logger.warn("The number of d-spacings ({}) should be greater than or equal to {}: using outer rings",
 					l, n-1);
 			ellipses = ellipses.subList(l+1, n);
 			n = ellipses.size();
 		}
 		logger.debug("Using {} rings:", n);
 		boolean allCircles = true;
 		for (int i = 0; i < n; i++) {
 			EllipticalROI e = ellipses.get(i);
 			logger.debug("    {}", e);
 			if (allCircles && !e.isCircular()) {
 				allCircles = false;
 			}
 		}
 
 		FitDiffFunction f;
 		if (allCircles) {
 			logger.debug("All rings are circular");
 			f = createQFitFunction3(ellipses, detector.getVPxSize(), env.getWavelength()*1e-7, detector.getDetectorDistance(), n, true);
 		} else {
 			f = createQFitFunction2(ellipses, detector.getVPxSize(), env.getWavelength()*1e-7, detector.getDetectorDistance(), detector.getTiltAngle(), n, true);
 		}
 
 		logger.debug("Init: {}", f.getInit());
 
 		// set up a combination generator for all (but first spacing)
 		List<Double> s = new ArrayList<Double>();
 		for (int i = 0, imax = spacings.size(); i < imax; i++) {
 			HKL d = spacings.get(i);
 			s.add(d.getD().doubleValue(SI.MILLIMETRE));
 		}
 		CombinationGenerator<Double> gen = new CombinationGenerator<Double>(s, n);
 		if (mon != null) {
 			mon.worked(1);
 		}
 		logger.debug("There are {} combinations", gen.getTotalCombinations());
 
 		double min = Double.POSITIVE_INFINITY;
 		LevenbergMarquardtOptimizer opt = new LevenbergMarquardtOptimizer();
 		// opt.setMaxEvaluations(2000);
 		List<Double> fSpacings = null;
 
 		int i = 0;
 		for (List<Double> list : gen) { // find combination that minimizes residuals
 			// list.add(0, dmin);
 			f.setSpacings(list);
 
 			try {
 				VectorialPointValuePair result = opt.optimize(f, f.getTarget(), f.getWeight(), f.getInit());
 
 				// logger.info("Q-space fit: rms = {}, x^2 = {}", opt.getRMS(), opt.getChiSquare());
 				double res = opt.getRMS();
 				if (res < min) {
 					min = res;
 					f.setParameters(result.getPoint());
 					fSpacings = list;
 				}
 				System.err.printf(".");
 			} catch (FunctionEvaluationException e) {
 				logger.error("Function evaluation problem", e);
 				// cannot happen
 			} catch (IllegalArgumentException e) {
 				logger.error("Start point has wrong dimension", e);
 				// should not happen!
 			} catch (ConvergenceException e) {
 				System.err.printf("c");
 //				logger.error("Convergence problem: max iterations ({}) exceeded", opt.getMaxIterations());
 				// ignore
 				// throw new IllegalArgumentException("Problem with optimizer converging");
 			}
 			if (mon != null) {
 				mon.worked(10);
 				if (mon.isCancelled())
 					return null;
 			}
 			if (i++ == 100) {
 				System.err.printf("\n");
 				i = 0;
 			}
 		}
 		System.err.printf("\n");
 
 		if (fSpacings == null || f.getParameters() == null) {
 			logger.warn("Problem with fitting - as could not find a single fit!");
 			return null;
 		}
 
 		double tilt = f.getTiltAngle();
 		logger.debug("Parameters: w {}, D {}, e {} (min {})", new Object[] { f.getWavelength(), f.getDistance(), tilt, min });
 		logger.debug("Spacings used: {}", fSpacings);
 		f.setSpacings(fSpacings);
 		logger.debug("Residual values for {}: {}", f.getRMS(f.getParameters()), f.getValues());
 
 		DetectorProperties nDetector = new DetectorProperties(detector);
 		nDetector.setDetectorDistance(f.getDistance());
 		nDetector.setNormalAnglesInDegrees(tilt, 0, f.getRollAngle());
 		DiffractionCrystalEnvironment nEnv = new DiffractionCrystalEnvironment(f.getWavelength());
 		return new QSpace(nDetector, nEnv);
 	}
 
 	static FitDiffFunction createQFitFunction(List<EllipticalROI> ellipses, double pix, double wavelength, double distance, double tilt, int n, boolean fixedWavelength) {
 		double[] known1 = new double[n];
 		double[] known2 = new double[n];
 
 		double rollAngle = 0;
 		double min = Double.POSITIVE_INFINITY;
 		for (int i = 0; i < n; i++) {
 			EllipticalROI e = ellipses.get(i);
 			if (e instanceof EllipticalFitROI) {
 				double rms = ((EllipticalFitROI) e).getRMS();
 				if (rms < min) {
 					min = rms;
 					rollAngle = e.getAngle();
 				}
 			}
 			double a = e.getSemiAxis(0);
 			double rs = a/e.getSemiAxis(1);
 			a *= pix;
 			rs = 1./(rs*rs);
 			known1[i] = a*rs; // in mm
 			known2[i] = rs;
 		}
 
 		double st = Math.sin(tilt);
 		FitDiffFunction f;
 		if (fixedWavelength) {
 			f = new QSpaceFitDiffFixedWFunction(known1, known2, wavelength, rollAngle);
 			f.setInit(new double[] {distance, st*st});
 		} else {
 			f = new QSpaceFitDiffFunction(known1, known2, rollAngle);
 			f.setInit(new double[] {wavelength, distance, st*st});
 		}
 		return f;
 	}
 
 	static FitDiffFunction createQFitFunction2(List<EllipticalROI> ellipses, double pix, double wavelength, double distance, double tilt, int n, boolean fixedWavelength) {
 		double[] known1 = new double[n];
 		double[] known2 = new double[n];
 
 		double rollAngle = 0;
 		double min = Double.POSITIVE_INFINITY;
 		for (int i = 0; i < n; i++) {
 			EllipticalROI e = ellipses.get(i);
 			if (e instanceof EllipticalFitROI) {
 				double rms = ((EllipticalFitROI) e).getRMS();
 				if (rms < min) {
 					min = rms;
 					rollAngle = e.getAngle();
 				}
 			}
 			known1[i] = pix * e.getSemiAxis(0); // in mm
 			known2[i] = pix * e.getSemiAxis(1);
 		}
 
 		double st = Math.sin(tilt);
 		FitDiffFunction f;
 		if (fixedWavelength) {
 			f = new QSpaceFitDiffFixedWFunction2(known1, known2, wavelength, rollAngle);
 			f.setInit(new double[] {distance, st*st});
 		} else {
 			f = new QSpaceFitDiffFunction2(known1, known2, rollAngle);
 			f.setInit(new double[] {wavelength, distance, st*st});
 		}
 		return f;
 	}
 
 	static FitDiffFunction createQFitFunction3(List<EllipticalROI> ellipses, double pix, double wavelength, double distance, int n, boolean fixedWavelength) {
 		double[] known1 = new double[n];
 
 		for (int i = 0; i < n; i++) {
 			EllipticalROI e = ellipses.get(i);
 			known1[i] = pix * e.getSemiAxis(0); // in mm
 		}
 
 		FitDiffFunction f;
 		if (fixedWavelength) {
 			f = new QSpaceFitDiffFixedWFunction3(known1, wavelength);
 			f.setInit(new double[] {distance});
 		} else {
 			f = new QSpaceFitDiffFunction3(known1);
 			f.setInit(new double[] {wavelength, distance});
 		}
 		return f;
 	}
 
 	interface FitFunction extends MultivariateVectorialFunction {
 		public double getRollAngle();
 		public double getTiltAngle();
 
 		public void setParameters(double[] arg);
 
 		public double[] getParameters();
 
 		public double[] getInit();
 		public void setInit(double[] init);
 		/**
 		 * @return wavelength in Angstrom
 		 */
 		public double getWavelength();
 		/**
 		 * @return distance in mm
 		 */
 		public double getDistance();
 
 		public double[] getWeight();
 
 
 		public double[] getTarget();
 
 		public double getRMS(double[] arg);
 
 		public double[] getValues();
 
 		/**
 		 * @param spacings (in mm)
 		 */
 		public void setSpacings(List<Double> spacings);
 	}
 
 	interface FitDiffFunction extends FitFunction, DifferentiableMultivariateVectorialFunction {
 	}
 
 	static abstract class FitFunctionBase implements FitFunction {
 		private double[] initial;
 		protected double[] target;
 		protected double[] spacing; // in mm
 		protected double[] value;
 
 		protected int nT;
 		protected int nV;
 		protected double[] weight;
 		protected double angle;
 		protected double[] parameters;
 
 		@Override
 		public double getRollAngle() {
 			return angle;
 		}
 
 		@Override
 		public double[] getWeight() {
 			return weight;
 		}
 
 		@Override
 		public double[] getTarget() {
 			return target;
 		}
 
 		@Override
 		public void setParameters(double[] arg) {
 			parameters = arg;
 		}
 
 		@Override
 		public double[] getParameters() {
 			return parameters;
 		}
 
 		@Override
 		public void setInit(double[] init) {
 			initial = init;
 		}
 
 		@Override
 		public double[] getInit() {
 			return initial;
 		}
 
 		/**
 		 * @param spacings (in mm)
 		 */
 		@Override
 		public void setSpacings(List<Double> spacings) {
 			for (int i = 0; i < nT; i++) {
 				spacing[i] = spacings.get(i);
 			}
 		}
 
 		/**
 		 * @param arg wavelength (mm), perpendicular distance (mm), sine-squared of tilt angle
 		 * @return values
 		 */
 		@Override
 		public double[] value(double[] arg) throws FunctionEvaluationException, IllegalArgumentException {
 			calcValues(arg);
 			return value;
 		}
 
 		/**
 		 * @param arg wavelength (mm), perpendicular distance (mm), sine-squared of tilt angle
 		 * @return root mean square of residuals
 		 */
 		@Override
 		public double getRMS(double[] arg) {
 			calcResidualValues(arg);
 			double s = 0;
 			for (int i = 0; i < nV; i++) {
 				s += value[i];
 			}
 			return s/nV;
 		}
 
 		@Override
 		public double[] getValues() {
 			return value;
 		}
 
 		abstract void calcValues(double[] arg);
 
 		abstract void calcResidualValues(double[] arg);
 	}
 
 	/**
 	 * LS function uses parameters: wavelength (mm), perpendicular distance (mm), sine-squared of tilt angle
 	 */
 	static class QSpaceFitFunction extends FitFunctionBase {
 
 		private static final double EXTRA_WEIGHT = 10; // for distance
 
 		public QSpaceFitFunction(double[] known1, double[] known2, double rollAngle) {
 			nT = known1.length;
 			nV = 2 * nT;
 			angle = rollAngle;
 			target = new double[nV];
 			weight = new double[nV];
 			for (int i = 0, j = 0; i < nT; i++) {
 				double k = known1[i];
 				target[j] = k;
 				weight[j++] = EXTRA_WEIGHT/(k*k); // make residual similar magnitude to known2's
 				target[j] = known2[i];
 				weight[j++] = 1;
 			}
 			spacing = new double[nT];
 			value = new double[nV];
 		}
 
 		@Override
 		public double getWavelength() {
 			return parameters[0]*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[1];
 		}
 
 		@Override
 		public double getTiltAngle() {
 			double ses = Math.abs(parameters[2]);
 			return Math.toDegrees(Math.asin(Math.sqrt(ses)));
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double ti = Math.sqrt(0.5 + x)*si/x;
 				value[j++] = ti*dist;
 				value[j++] = 1 - setas*(1+ti*ti);
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double ti = Math.sqrt(0.5 + x)*si/x;
 				double t = ti*dist - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 				t = 1 - setas*(1+ti*ti) - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFunction extends QSpaceFitFunction implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFunction(double[] known1, double[] known2, double rollAngle) {
 			super(known1, known2, rollAngle);
 			jac = new double[nV][3];
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					calcJacobian(point);
 					return jac;
 				}
 			};
 		}
 
 		private void calcJacobian(double[] arg) {
 			double wlen = arg[0];
 			double hdist = 0.5 * arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double otdi = 0.5 / spacing[i];
 				double si = wlen * otdi;
 				double x = 0.5 - si * si;
 				double ti = Math.sqrt(0.5 + x) * si / x;
 
 				double fx = otdi / x;
 				jac[j][0] = fx * hdist * ti / (si * (0.5 + x));
 				jac[j][1] = ti;
 				jac[j][2] = 0;
 				j++;
 
 				double gx = 1 / (x * x);
 				jac[j][0] = -fx * setas * si * gx;
 				jac[j][1] = 0;
 				jac[j][2] = -0.25 * gx;
 				j++;
 			}
 		}
 	}
 
 	/**
 	 * LS function uses parameters: perpendicular distance (mm), sine-squared of tilt angle
 	 */
 	static class QSpaceFitFixedWFunction extends QSpaceFitFunction {
 		protected double lambda;
 
 		public QSpaceFitFixedWFunction(double[] known1, double[] known2, double wavelength, double rollAngle) {
 			super(known1, known2, rollAngle);
 			lambda = wavelength;
 		}
 
 		@Override
 		public double getWavelength() {
 			return lambda*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[0];
 		}
 
 		@Override
 		public double getTiltAngle() {
 			double ses = Math.abs(parameters[1]);
 			return Math.toDegrees(Math.asin(Math.sqrt(ses)));
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double dist = arg[0];
 			double setas = arg[1];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double ti = Math.sqrt(0.5 + x)*si/x;
 				value[j++] = ti*dist;
 				value[j++] = 1 - setas*(1+ti*ti);
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double dist = arg[0];
 			double setas = arg[1];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double ti = Math.sqrt(0.5 + x)*si/x;
 				double t = ti*dist - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 				t = 1 - setas*(1+ti*ti) - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFixedWFunction extends QSpaceFitFixedWFunction implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFixedWFunction(double[] known1, double[] known2, double wavelength, double rollAngle) {
 			super(known1, known2, wavelength, rollAngle);
 			jac = new double[nV][3];
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					return jac;
 				}
 			};
 		}
 
 		@Override
 		public void setSpacings(List<Double> spacings) {
 			super.setSpacings(spacings);
 			calcJacobian();
 		}
 
 		private void calcJacobian() {
 			for (int i = 0, j = 0; i < nT; i++) {
 				double otdi = 0.5 / spacing[i];
 				double si = lambda * otdi;
 				double x = 0.5 - si * si;
 				double ti = Math.sqrt(0.5 + x) * si / x;
 
 				jac[j][0] = ti;
 				jac[j][1] = 0;
 				j++;
 
 				double gx = 1 / (x * x);
 				jac[j][0] = 0;
 				jac[j][1] = -0.25 * gx;
 				j++;
 			}
 		}
 	}
 
 	/**
 	 * LS function uses parameters: wavelength (mm), perpendicular distance (mm), sine-squared of tilt angle
 	 */
 	static class QSpaceFitFunction2 extends FitFunctionBase {
 
 		public QSpaceFitFunction2(double[] known1, double[] known2, double rollAngle) {
 			nT = known1.length;
 			nV = 2 * nT;
 			angle = rollAngle;
 			target = new double[nV];
 			weight = new double[nV];
 			for (int i = 0, j = 0; i < nT; i++) {
 				target[j] = known1[i];
 				weight[j++] = 1;
 				target[j] = known2[i];
 				weight[j++] = 1;
 			}
 			spacing = new double[nT];
 			value = new double[nV];
 		}
 
 		@Override
 		public double getWavelength() {
 			return parameters[0]*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[1];
 		}
 
 		@Override
 		public double getTiltAngle() {
 			double ses = Math.abs(parameters[2]);
 			return Math.toDegrees(Math.asin(Math.sqrt(ses)));
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double den = 1.0/(ca*ca - setas);
 				value[j++] = saD*ca*den;
 				value[j++] = saD*Math.sqrt(den);
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double den = 1.0/(ca*ca - setas);
 				double t = saD*ca*den - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 				t = saD*Math.sqrt(den) - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFunction2 extends QSpaceFitFunction2 implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFunction2(double[] known1, double[] known2, double rollAngle) {
 			super(known1, known2, rollAngle);
 			jac = new double[nV][3];
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					calcJacobian(point);
 					return jac;
 				}
 			};
 		}
 
 		private void calcJacobian(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 			double setas = arg[2];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double otdi = 0.5 / spacing[i];
 				double si = wlen * otdi;
 				double x = 0.5 - si*si;
 				double ca = 2.0*x;
 				double cas = ca*ca;
 				double sa = 2.0*si*Math.sqrt(0.5 + x);
 				double den = 1.0/(cas - setas);
 
 				double fx = otdi*si*4.0*dist/sa;
 				jac[j][0] = fx*(cas*(1 - 2*setas) + setas)*den*den;
 				double t = sa*ca*den;
 				jac[j][1] = t;
 				jac[j][2] = t*dist*den;
 				j++;
 
 				t = Math.sqrt(den);
 				jac[j][0] = fx*ca*(1 - setas)*den*t;
 				t *= sa;
 				jac[j][1] = t;
 				jac[j][2] = 0.5*t*dist*den;
 				j++;
 			}
 		}
 	}
 
 	/**
 	 * LS function uses parameters: perpendicular distance (mm), sine-squared of tilt angle
 	 */
 	static class QSpaceFitFixedWFunction2 extends QSpaceFitDiffFunction2 {
 		protected double lambda;
 
 		public QSpaceFitFixedWFunction2(double[] known1, double[] known2, double wavelength, double rollAngle) {
 			super(known1, known2, rollAngle);
 			lambda = wavelength;
 		}
 
 		@Override
 		public double getWavelength() {
 			return lambda*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[0];
 		}
 
 		@Override
 		public double getTiltAngle() {
 			double ses = Math.abs(parameters[1]);
 			return Math.toDegrees(Math.asin(Math.sqrt(ses)));
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double dist = arg[0];
 			double setas = arg[1];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double den = 1.0/(ca*ca - setas);
 				value[j++] = saD*ca*den;
 				value[j++] = saD*Math.sqrt(den);
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double dist = arg[0];
 			double setas = arg[1];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double den = 1.0/(ca*ca - setas);
 				double t = saD*ca*den - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 				t = saD*Math.sqrt(den) - target[j];
 				value[j] = t*t*weight[j];
 				j++;
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFixedWFunction2 extends QSpaceFitFixedWFunction2 implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFixedWFunction2(double[] known1, double[] known2, double wavelength, double rollAngle) {
 			super(known1, known2, wavelength, rollAngle);
 			jac = new double[nV][2];
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					calcJacobian(point);
 					return jac;
 				}
 			};
 		}
 
 		private void calcJacobian(double[] arg) {
 			double dist = arg[0];
 			double setas = arg[1];
 
 			for (int i = 0, j = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double sa = 2.0*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double den = 1.0/(ca*ca - setas);
 
 				double t = sa*ca*den;
 				jac[j][0] = t;
 				jac[j][1] = t*dist*den;
 				j++;
 
 				t = sa*Math.sqrt(den);
 				jac[j][0] = t;
 				jac[j][1] = 0.5*t*dist*den;
 				j++;
 			}
 		}
 	}
 
 	/**
 	 * Zero-angle
 	 * LS function uses parameters: wavelength (mm), perpendicular distance (mm)
 	 */
 	static class QSpaceFitFunction3 extends FitFunctionBase {
 
 		public QSpaceFitFunction3(double[] known1) {
 			nT = known1.length;
 			nV = nT;
 			angle = 0;
 			target = new double[nT];
 			weight = new double[nT];
 			for (int i = 0; i < nT; i++) {
 				target[i] = known1[i];
 				weight[i] = 1;
 			}
 			spacing = new double[nT];
 			value = new double[nV];
 		}
 
 		@Override
 		public double getWavelength() {
 			return parameters[0]*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[1];
 		}
 
 		@Override
 		public double getTiltAngle() {
 			return 0;
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 
 			for (int i = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				value[i] = saD/ca;
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 
 			for (int i = 0; i < nT; i++) {
 				double si = 0.5*wlen/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double t = saD/ca - target[i];
 				value[i] = t*t*weight[i];
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFunction3 extends QSpaceFitFunction3 implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFunction3(double[] known1) {
 			super(known1);
 			jac = new double[nT][2];
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					calcJacobian(point);
 					return jac;
 				}
 			};
 		}
 
 		private void calcJacobian(double[] arg) {
 			double wlen = arg[0];
 			double dist = arg[1];
 
 			for (int i = 0; i < nT; i++) {
 				double otdi = 0.5 / spacing[i];
 				double si = wlen * otdi;
 				double x = 0.5 - si*si;
 				double ca = 2.0*x;
 				double sa = 2.0*si*Math.sqrt(0.5 + x);
 
 				double fx = otdi*si*4.0*dist/sa;
 				jac[i][0] = fx/(ca*ca);
 				double t = sa/ca;
 				jac[i][1] = t;
 			}
 		}
 	}
 
 	/**
 	 * LS function uses parameters: perpendicular distance (mm)
 	 */
 	static class QSpaceFitFixedWFunction3 extends QSpaceFitFunction3 {
 		protected double lambda;
 
 		public QSpaceFitFixedWFunction3(double[] known1, double wavelength) {
 			super(known1);
 			lambda = wavelength;
 		}
 
 		@Override
 		public double getWavelength() {
 			return lambda*1e7;
 		}
 
 		@Override
 		public double getDistance() {
 			return parameters[0];
 		}
 
 		@Override
 		void calcValues(double[] arg) {
 			double dist = arg[0];
 
 			for (int i = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				value[i] = saD/ca;
 			}
 		}
 
 		@Override
 		void calcResidualValues(double[] arg) {
 			double dist = arg[0];
 
 			for (int i = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 				double saD = 2.0*dist*si*Math.sqrt(0.5 + x);
 				double ca = 2.0*x;
 				double t = saD/ca - target[i];
 				value[i] = t*t*weight[i];
 			}
 		}
 	}
 
 	static class QSpaceFitDiffFixedWFunction3 extends QSpaceFitFixedWFunction3 implements FitDiffFunction {
 		private double[][] jac;
 
 		public QSpaceFitDiffFixedWFunction3(double[] known1, double wavelength) {
 			super(known1, wavelength);
 			jac = new double[nT][1];
 		}
 
 		@Override
 		public void setSpacings(List<Double> spacings) {
 			super.setSpacings(spacings);
 			calcJacobian();
 		}
 
 		@Override
 		public MultivariateMatrixFunction jacobian() {
 			return new MultivariateMatrixFunction() {
 				@Override
 				public double[][] value(double[] point) throws FunctionEvaluationException, IllegalArgumentException {
 					return jac;
 				}
 			};
 		}
 
 		private void calcJacobian() {
 			for (int i = 0; i < nT; i++) {
 				double si = 0.5*lambda/spacing[i];
 				double x = 0.5 - si*si;
 
 				jac[i][0] = si*Math.sqrt(0.5 + x)/x;
 			}
 		}
 	}
 }
