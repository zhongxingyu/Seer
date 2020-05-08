 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 
 import huisken.fusion.HistogramFeatures;
 
 import ij.IJ;
 import ij.ImagePlus;
 
 import ij.gui.GenericDialog;
 import ij.gui.Roi;
 import ij.gui.WaitForUserDialog;
 
 import ij.plugin.PlugIn;
 
 import ij.process.ImageProcessor;
 
 import java.io.File;
 
 import meshtools.IndexedTriangleMesh;
 
 import vib.FastMatrix;
 
 import javax.vecmath.Point3f;
 
 
 
 public class Spherical_Max_Projection implements PlugIn {
 
 
 	public static final float FIT_SPHERE_THRESHOLD = 1600f;
 
 	public void run(String arg) {
 		GenericDialogPlus gd = new GenericDialogPlus("Spherical_Max_Projection");
 		gd.addDirectoryField("Data directory", "");
 		gd.addNumericField("Timepoint used for sphere fitting", 1, 0);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 		File datadir = new File(gd.getNextString());
 		int fittingTimepoint = (int)gd.getNextNumber();
 		if(!datadir.isDirectory()) {
 			IJ.error(datadir + " is not a directory");
 			return;
 		}
 
 		File outputdir = new File(datadir, "SphereProjection");
 		if(outputdir.isDirectory()) {
 			boolean cancelled = !IJ.showMessageWithCancel("Overwrite",
 					outputdir + " already exists. Overwrite?");
 			if(cancelled)
 				return;
 		} else {
 			outputdir.mkdir();
 		}
 
 		try {
 			process(datadir.getAbsolutePath(), outputdir.getAbsolutePath(), fittingTimepoint);
 		} catch(Exception e) {
 			IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	private TimelapseOpener opener = null;
 
 	private static FastMatrix rotateY(double rad, Point3f center) {
 		FastMatrix rot = FastMatrix.rotate(rad, 1);
 		rot.apply(center.x, center.y, center.z);
 		return FastMatrix.translate(center.x - rot.x, center.y - rot.y, center.z - rot.z).times(rot);
 	}
 
 	public void process(String datadir, String outputdir, int fittingTimepoint) {
 		TimelapseOpener opener = null;
 		try {
 			opener = new TimelapseOpener(datadir, true);
 		} catch(Exception e) {
 			throw new RuntimeException("Cannot open timelapse", e);
 		}
 		GenericDialog gd = new GenericDialog("Limit processing");
 		gd.addNumericField("Start_timepoint",      opener.timepointStart, 0);
 		gd.addNumericField("Timepoint_Increment",  opener.timepointInc, 0);
 		gd.addNumericField("Number_of_timepoints", opener.nTimepoints, 0);
 		gd.addNumericField("Start_angle",          opener.angleStart, 0);
 		gd.addNumericField("Angle_Increment",      opener.angleInc, 0);
 		gd.addNumericField("Number_of_angles",     opener.nAngles, 0);
 		gd.addCheckbox("Also save single views", false);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 		process(opener, outputdir, fittingTimepoint,
 			(int)gd.getNextNumber(),
 			(int)gd.getNextNumber(),
 			(int)gd.getNextNumber(),
 			(int)gd.getNextNumber(),
 			(int)gd.getNextNumber(),
 			(int)gd.getNextNumber(),
 			gd.getNextBoolean());
 	}
 
 	public void process(TimelapseOpener opener, String outputdir, int fittingTimepoint, int timepointStart, int timepointInc, int nTimepoints, int angleStart, int angleInc, int nAngles, boolean saveSingleViews) {
 		this.opener = opener;
 
 		if(!outputdir.endsWith(File.separator))
 			outputdir += File.separator;
 
 		// fit the spheres to the specified timepoint
 		int startTimepoint = timepointStart;
 
 		Point3f[] centers = new Point3f[nAngles];
 		for(int i = 0; i < centers.length; i++)
 			centers[i] = new Point3f();
 		float radius = fitSpheres(fittingTimepoint, centers, angleStart, angleInc, nAngles);
 
 		// calculate sphere transformations for each angle
 		FastMatrix[] transforms = new FastMatrix[nAngles];
 		for(int a = 1; a < nAngles; a++) {
 			double angle = angleStart + angleInc * a;
 			angle = angle * Math.PI / 180.0;
 			transforms[a] = FastMatrix.translate(
						centers[a].x - centers[0].x,
						centers[a].y - centers[0].y,
						centers[a].z - centers[0].z)
					.times(rotateY(-angle, centers[0]));
 		}
 
 
 		// initialize the maximum projections
 		SphericalMaxProjection[][] smp = initSphericalMaximumProjection(transforms, centers[0], radius, angleStart, angleInc, nAngles);
 
 		// save the sphere geometry
 		String spherepath = new File(outputdir, "Sphere.obj").getAbsolutePath();
 		try {
 			smp[0][0].saveSphere(spherepath);
 		} catch(Exception e) {
 			throw new RuntimeException("Cannot save sphere: " + spherepath, e);
 		}
 
 		AngleWeighter aw = new AngleWeighter(nAngles);
 
 		// start the projections
 		for(int tp = startTimepoint; tp < startTimepoint + nTimepoints; tp += timepointInc) {
 			IJ.showStatus("Timepoint " + (tp - startTimepoint + 1) + "/" + nTimepoints);
 
 			for(int a = 0; a < nAngles; a++) {
 				int angle = angleStart + a * angleInc;
 
 				// left ill
 				ImagePlus image = opener.openStack(tp, angle, 0, 2, -1);
 				smp[a][0].project(image);
 
 				// right ill
 				image = opener.openStack(tp, angle, 1, 2, -1);
 				smp[a][1].project(image);
 
 				// sum up left and right illumination
 				smp[a][0].addMaxima(smp[a][1].getMaxima());
 
 				// if specified, save the single views in separate folders
 				if(saveSingleViews) {
 					String filename = String.format("tp%04d.tif", tp, angle);
 					String subfolder = String.format("angle%3d/", angle);
 					String path = new File(outputdir + subfolder, filename + ".vertices").getAbsolutePath();
 					File subf = new File(outputdir, subfolder);
 					if(!subf.exists()) {
 						subf.mkdir();
 						try {
 							smp[0][0].saveSphere(outputdir + subfolder + "Sphere.obj");
 						} catch(Exception e) {
 							throw new RuntimeException("Cannot save sphere: " + outputdir + subfolder + "Sphere.obj", e);
 						}
 					}
 
 					try {
 						smp[a][0].saveMaxima(path);
 					} catch(Exception e) {
 						throw new RuntimeException("Cannot save " + path);
 					}
 				}
 
 				// scale the resulting maxima according to angle
 				smp[a][0].scaleMaxima(aw);
 
 				// sum them all up
 				if(a > 0)
 					smp[0][0].addMaxima(smp[a][0].getMaxima());
 			}
 
 			String filename = String.format("tp%04d.tif", tp);
 			String path = new File(outputdir, filename + ".vertices").getAbsolutePath();
 			try {
 				smp[0][0].saveMaxima(path);
 			} catch(Exception e) {
 				throw new RuntimeException("Cannot save " + path);
 			}
 		}
 	}
 
 	private float fitSpheres(int timepoint, Point3f[] centers, int angleStart, int angleInc, int nAngles) {
 		float radius = 0;
 		for(int a = 0; a < nAngles; a++) {
 			int angle = angleStart + angleInc * a;
 
 			// left illumination
 			ImagePlus imp = opener.openStack(timepoint, angle, 0, 2, -1);
 			limitAreaForFitSphere(imp, FIT_SPHERE_THRESHOLD);
 			imp.show();
 			IJ.runMacro("setThreshold(" + FIT_SPHERE_THRESHOLD + ", 16000);");
 			IJ.run("Threshold...");
 			new WaitForUserDialog("Fit sphere", "Adjust ROI and minimum threshold").show();
 			float threshold = (float)imp.getProcessor().getMinThreshold();
 			Fit_Sphere fs = new Fit_Sphere(imp);
 			fs.fit(FIT_SPHERE_THRESHOLD);
 			fs.getControlImage().show();
 			fs.getCenter(centers[a]);
 			radius += fs.getRadius();
 			imp.close();
 		}
 		return radius / nAngles;
 	}
 
 	private static void limitAreaForFitSphere(ImagePlus imp, float threshold) {
 		int w = imp.getWidth();
 		int h = imp.getHeight();
 		int d = imp.getStackSize();
 
 		int[] xs = new int[w];
 		int[] ys = new int[h];
 		int sum = 0;
 		for(int z = 0; z < d; z++) {
 			ImageProcessor ip = imp.getStack().getProcessor(z + 1);
 			for(int y = 0; y < h; y++) {
 				for(int x = 0; x < w; x++) {
 					float v = ip.getf(x, y);
 					if(v >= threshold) {
 						xs[x]++;
 						ys[y]++;
 						sum++;
 					}
 				}
 			}
 		}
 		int xl = Math.round(HistogramFeatures.getQuantile(xs, sum, 0.1f));
 		int xu = Math.round(HistogramFeatures.getQuantile(xs, sum, 0.9f));
 		int yl = Math.round(HistogramFeatures.getQuantile(ys, sum, 0.1f));
 		int yu = Math.round(HistogramFeatures.getQuantile(ys, sum, 0.9f));
 		imp.setRoi(new Roi(xl, yl, xu - xl, yu - yl));
 	}
 
 	/**
 	 * @param transform Array with one transformation for each angle; the first entry
 	 *                  in this array is ignored (it is assumed to be the identity matrix.
 	 */
 	private SphericalMaxProjection[][] initSphericalMaximumProjection(FastMatrix[] transform, Point3f center, float radius, int angleStart, int angleInc, int nAngles) {
 
 		if(transform.length != nAngles)
 			throw new IllegalArgumentException("Need one transformation for each angle");
 
 		int w = opener.w, h = opener.h, d = opener.d / 2;
 		double pw = opener.pw, ph = opener.ph, pd = opener.pd;
 
 		int subd = (int)Math.round(radius / (Math.min(pw, Math.min(ph, pd))));
 		subd /= 4;
 
 		SphericalMaxProjection[][] smp = new SphericalMaxProjection[nAngles][2];
 
 		// 0 degree, left illumination
 		smp[0][0] = new SphericalMaxProjection(center, radius, subd);
 		smp[0][0].prepareForProjection(w, h, d, pw, ph, pd, new SimpleLeftWeighter(center.x));
 
 		IndexedTriangleMesh sphere = smp[0][0].getSphere();
 
 		// 0 degree, right handed illumination
 		smp[0][1] = new SphericalMaxProjection(sphere, center, radius);
 		smp[0][1].prepareForProjection(w, h, d, pw, ph, pd, new SimpleRightWeighter(center.x));
 
 		// all other angles
 		for(int a = 1; a < nAngles; a++) {
 			// left illumination
 			smp[a][0] = new SphericalMaxProjection(sphere, center, radius, transform[a]);
 			smp[a][0].prepareForProjection(w, h, d, pw, ph, pd, new SimpleLeftWeighter(center.x));
 
 			// right illumination
 			smp[a][1] = new SphericalMaxProjection(sphere, center, radius, transform[a]);
 			smp[a][1].prepareForProjection(w, h, d, pw, ph, pd, new SimpleRightWeighter(center.x));
 		}
 
 		return smp;
 	}
 }
