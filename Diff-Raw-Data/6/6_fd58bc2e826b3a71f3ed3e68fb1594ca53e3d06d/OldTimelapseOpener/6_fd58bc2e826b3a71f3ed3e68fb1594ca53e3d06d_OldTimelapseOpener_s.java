 package huisken.projection;
 
 import huisken.util.XMLReader;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.ImageStack;
 import ij.process.ImageProcessor;
 
 import java.io.File;
 import java.util.TreeSet;
 
 public class OldTimelapseOpener extends Opener {
 
 	private final String parentdir;
 
 	private final int w, h, d, angleStart, angleInc, nAngles, timepointStart, timepointInc, nTimepoints;
 	private final double pw, ph, pd;
 
 	private final boolean doublesided;
 
 	public OldTimelapseOpener(String parentdir, boolean doublesided) {
 		this.doublesided = doublesided;
 		if(!doublesided)
 			throw new UnsupportedOperationException("Only double-sided illumination is supported at the moment");
 
 		if(!parentdir.endsWith(File.separator))
 			parentdir += File.separator;
 		this.parentdir = parentdir;
 		String xmlpath = new File(new File(parentdir).getParentFile(), "recording_002_e0000.xml").getAbsolutePath();
 		XMLReader xml = null;
 		try {
 			xml = new XMLReader(xmlpath);
 		} catch(Exception e) {
 			throw new RuntimeException("Cannot parse xml file: " + xmlpath, e);
 		}
 		this.w = xml.width;
 		this.h = xml.height;
 		this.d = xml.depth;
 		this.pw = xml.pw;
 		this.ph = xml.ph;
 		this.pd = xml.pd;
 
 		TreeSet<Integer> timepoints = new TreeSet<Integer>();
 		TreeSet<Integer> angles = new TreeSet<Integer>();
 		String[] files = new File(parentdir).list();
 		for(String file : files) {
 			if(!file.startsWith("tp"))
 				continue;
 			int tp = Integer.parseInt(file.substring(2, 6));
 			int angle = Integer.parseInt(file.substring(18, 21));
 			timepoints.add(tp);
 			angles.add(angle);
 		}
 
 		java.util.Iterator<Integer> angleIt = angles.iterator();
 		java.util.Iterator<Integer> timepointsIt = timepoints.iterator();
 		this.angleStart = angleIt.next();
 		this.angleInc   = angleIt.next() - angleStart;
 		this.nAngles    = angles.size();
 		this.timepointStart = timepointsIt.next();
 		this.timepointInc   = timepointsIt.next() - timepointStart;
 		this.nTimepoints    = timepoints.size();
 	}
 
 	public void print() {
 		System.out.println("parentdir = " + parentdir);
 		System.out.println("w = " + getWidth());
 		System.out.println("h = " + getHeight());
 		System.out.println("d = " + getDepth());
 		System.out.println("pw = " + getPixelWidth());
 		System.out.println("ph = " + getPixelHeight());
 		System.out.println("pd = " + getPixelDepth());
 		System.out.println("angleStart = " + getAngleStart());
 		System.out.println("angleInc = " + getAngleInc());
 		System.out.println("nAngles = " + getNAngles());
 		System.out.println("timepointStart = " + getTimepointStart());
 		System.out.println("timepointInc = " + getTimepointInc());
 		System.out.println("nTimepoints = " + getNTimepoints());
 	}
 
 	/*
 	 * illumination is one of LEFT, RIGHT
 	 */
 	@Override
 	public ImageProcessor openPlane(int timepoint, int angle, int plane, int illumination) {
 		String folderPattern = "tp%04d_view%d_angle%03d/";
 		String slicePattern = "0001_%04d.tif";
 
		int p = doublesided ? plane : 2 * plane + illumination;
 
 		int view = 1 + (angle - getAngleStart()) / getAngleInc();
 		String file = parentdir
 				+ String.format(folderPattern, timepoint, view, angle)
 				+ String.format(slicePattern, p);
 
 		ImagePlus imp = IJ.openImage(file);
 		if(imp == null)
 			throw new RuntimeException("Cannot open " + (file));
 
 		return imp.getProcessor();
 	}
 
 	@Override
 	public ImagePlus openStack(int timepoint, int angle, int illumination) {
 		ImageStack stack = new ImageStack(getWidth(), getHeight());
 
 		for(int z = 0; z < d; z ++) {
 			stack.addSlice("", openPlane(timepoint, angle, z, illumination));
 			IJ.showProgress(z + 1, d);
 		}
 		ImagePlus imp = new ImagePlus(parentdir, stack);
 		imp.getCalibration().pixelWidth  = getPixelWidth();
 		imp.getCalibration().pixelHeight = getPixelHeight();
 		imp.getCalibration().pixelDepth  = getPixelDepth();
 		return imp;
 	}
 
 	@Override
 	public int getWidth() {
 		return w;
 	}
 
 	@Override
 	public int getHeight() {
 		return h;
 	}
 
 	@Override
 	public int getDepth() {
 		return d;
 	}
 
 	@Override
 	public int getAngleStart() {
 		return angleStart;
 	}
 
 	@Override
 	public int getAngleInc() {
 		return angleInc;
 	}
 
 	@Override
 	public int getNAngles() {
 		return nAngles;
 	}
 
 	@Override
 	public int getTimepointStart() {
 		return timepointStart;
 	}
 
 	@Override
 	public int getTimepointInc() {
 		return timepointInc;
 	}
 
 	@Override
 	public int getNTimepoints() {
 		return nTimepoints;
 	}
 
 	@Override
 	public double getPixelWidth() {
 		return pw;
 	}
 
 	@Override
 	public double getPixelHeight() {
 		return ph;
 	}
 
 	@Override
 	public double getPixelDepth() {
 		return pd;
 	}
}
