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
 
 import javax.vecmath.Point3f;
 
 public class Spherical_Max_Projection implements PlugIn {
 
 	public static final float FIT_SPHERE_THRESHOLD = 1600f;
 
	public static final int OLD_FOLDER_STRUCTURE = 1;
	public static final int New_FOLDER_STRUCTURE = 0;
 
 	@Override
 	public void run(String arg) {
 		GenericDialogPlus gd = new GenericDialogPlus("Spherical_Max_Projection");
 		gd.addDirectoryField("Data directory", "");
 		gd.addNumericField("Timepoint used for sphere fitting", 1, 0);
 		String[] types = new String[] { "New folder structure", "Old folder structure" };
 		gd.addChoice("Data source", types, types[0]);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 		File datadir = new File(gd.getNextString());
 		int fittingTimepoint = (int)gd.getNextNumber();
 		int source = gd.getNextChoiceIndex();
 
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
 			process(source, datadir.getAbsolutePath(), outputdir.getAbsolutePath(), fittingTimepoint);
 		} catch(Exception e) {
 			IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public void process(int source, String datadir, String outputdir, int fittingTimepoint) {
 		Opener opener = null;
 		try {
 			switch(source) {
 			case New_FOLDER_STRUCTURE: opener = new NewTimelapseOpener(datadir, true); break;
 			case OLD_FOLDER_STRUCTURE: opener = new OldTimelapseOpener(datadir, true); break;
 			}
 		} catch(Exception e) {
 			throw new RuntimeException("Cannot open timelapse", e);
 		}
 		GenericDialog gd = new GenericDialog("Limit processing");
 		gd.addNumericField("Start_timepoint",      opener.getTimepointStart(), 0);
 		gd.addNumericField("Timepoint_Increment",  opener.getTimepointInc(), 0);
 		gd.addNumericField("Number_of_timepoints", opener.getNTimepoints(), 0);
 		gd.addNumericField("Start_angle",          opener.getAngleStart(), 0);
 		gd.addNumericField("Angle_Increment",      opener.getAngleInc(), 0);
 		gd.addNumericField("Number_of_angles",     opener.getNAngles(), 0);
 		gd.addCheckbox("Also save single views", false);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 
 		int timepointStart = (int)gd.getNextNumber();
 		int timepointInc   = (int)gd.getNextNumber();
 		int nTimepoints    = (int)gd.getNextNumber();
 		int angleStart     = (int)gd.getNextNumber();
 		int angleInc       = (int)gd.getNextNumber();
 		int nAngles        = (int)gd.getNextNumber();
 		boolean saveSingleViews = gd.getNextBoolean();
 
 		// fit the spheres to the specified timepoint
 		Point3f[] centers = new Point3f[nAngles];
 		for(int i = 0; i < centers.length; i++)
 			centers[i] = new Point3f();
 		float radius = fitSpheres(opener, fittingTimepoint, centers, angleStart, angleInc, nAngles);
 
 		MultiViewSphericalMaxProjection mmsmp = new MultiViewSphericalMaxProjection(
 				outputdir, timepointStart, timepointInc, nTimepoints,
 				angleStart, angleInc, nAngles,
 				opener.getWidth(), opener.getHeight(), opener.getDepth(),
 				opener.getPixelWidth(), opener.getPixelHeight(), opener.getPixelDepth(),
 				centers, radius,
 				saveSingleViews);
 
 		int nPlanes = opener.getDepth();
 
 		// start the projections
 		for(int tp = timepointStart; tp < timepointStart + nTimepoints; tp += timepointInc) {
 			IJ.showStatus("Timepoint " + (tp - timepointStart + 1) + "/" + nTimepoints);
 
 			for(int a = 0; a < nAngles; a++) {
 				int angle = angleStart + a * angleInc;
 				for(int z = 0; z < nPlanes; z++) {
 					ImageProcessor left  = opener.openPlane(tp, angle, z, Opener.LEFT);
 					ImageProcessor right = opener.openPlane(tp, angle, z, Opener.RIGHT);
 					mmsmp.process(left, right);
 				}
 			}
 		}
 	}
 
 	private float fitSpheres(Opener opener, int timepoint, Point3f[] centers, int angleStart, int angleInc, int nAngles) {
 		float radius = 0;
 		for(int a = 0; a < nAngles; a++) {
 			int angle = angleStart + angleInc * a;
 
 			// left illumination
 			ImagePlus imp = opener.openStack(timepoint, angle, Opener.LEFT);
 			limitAreaForFitSphere(imp, FIT_SPHERE_THRESHOLD);
 			imp.show();
 			IJ.runMacro("setThreshold(" + FIT_SPHERE_THRESHOLD + ", 16000);");
 			IJ.run("Threshold...");
 			new WaitForUserDialog("Fit sphere", "Adjust ROI and minimum threshold").show();
 			float threshold = (float)imp.getProcessor().getMinThreshold();
 			Fit_Sphere fs = new Fit_Sphere(imp);
 			fs.fit(threshold);
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
 }
