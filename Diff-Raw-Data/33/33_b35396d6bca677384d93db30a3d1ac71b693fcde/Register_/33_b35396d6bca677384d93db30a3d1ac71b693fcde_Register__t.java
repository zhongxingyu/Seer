 package huisken.projection;
 
 import fiji.util.gui.GenericDialogPlus;
 import ij.IJ;
 import ij.ImagePlus;
 import ij.Prefs;
 import ij.plugin.PlugIn;
 import ij.plugin.filter.GaussianBlur;
 import ij.process.ImageProcessor;
 
 import java.awt.Polygon;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import meshtools.IndexedTriangleMesh;
 
 
 public class Register_ implements PlugIn {
 
 	@Override
 	public void run(String arg) {
 		String datadir = Prefs.get("register_sphere_proj.datadir", "");
 		String outputdir = Prefs.get("register_sphere_proj.outputdir", "");
 		GenericDialogPlus gd = new GenericDialogPlus("Register sphere projections");
 		gd.addDirectoryField("Data directory", datadir);
 		gd.addDirectoryField("Output directory", outputdir);
 		gd.showDialog();
 		if(gd.wasCanceled())
 			return;
 		datadir = gd.getNextString();
 		outputdir = gd.getNextString();
 
 		Prefs.set("register_sphere_proj.datadir", datadir);
 		Prefs.set("register_sphere_proj.outputdir", outputdir);
 		Prefs.savePreferences();
 
 		try {
 			register(datadir, outputdir);
 		} catch(Exception e) {
 		IJ.error(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public void createFullerProjection(SphericalMaxProjection smp, String datadir) throws IOException {
 		File outputdir = new File(datadir, "fuller");
 		if(outputdir.exists()) {
 			boolean cancelled = !IJ.showMessageWithCancel("Recalculate Fuller projection?", "Recalculate Fuller projection?");
 			if(cancelled)
 				return;
 		} else {
 			outputdir.mkdirs();
 		}
 
 		int w = 1000;
 
 		FullerProjection proj = new FullerProjection();
 		proj.prepareForProjection(smp, w);
 
 		// collect files
 		List<String> tmp = new ArrayList<String>();
 		tmp.addAll(Arrays.asList(new File(datadir).list()));
 		for(int i = tmp.size() - 1; i >= 0; i--)
 			if(!tmp.get(i).endsWith(".vertices"))
 				tmp.remove(i);
 		String[] files = new String[tmp.size()];
 		tmp.toArray(files);
 		for(int i = 0; i < files.length; i++)
 			files[i] = datadir + File.separator + files[i];
 		Arrays.sort(files);
 
 
 		// the following is copied from FullerProjection.
 		double s = w / 5.5;
 		double BLA = 0.5 * Math.sqrt(3);
 		int h = (int)Math.round(3 * BLA * s);
 
 		Icosahedron icosa = new Icosahedron(smp.radius);
 		for(Point3f p : icosa.getVertices())
 			p.add(smp.center);
 
 		IndexedTriangleMesh flatIcosa = icosa.createFlatVersion((float)s);
 
 		int[][] vIndices = new int[w * h][3];
 		Point3f[] flatVertices = flatIcosa.getVertices();
 		int[] flatFaces = flatIcosa.getFaces();
 		Point3f[] icosaVertices = icosa.getVertices();
 		int[] icosaFaces = icosa.getFaces();
 
 		GaussianBlur gauss = new GaussianBlur();
 
 		for(String file : files) {
 			smp.loadMaxima(file);
 			String filename = new File(file).getName();
 			filename = filename.substring(0, filename.length() - 9) + ".tif";
 			ImageProcessor fuller = proj.project();
 			IJ.save(new ImagePlus("", fuller), new File(outputdir, filename).getAbsolutePath());
 
 			gauss.blur(fuller, 1.5);
 			Polygon pt = new MaximumFinder().getMaxima(fuller, 10, true);
 
 			ArrayList<Point3f> pts = new ArrayList<Point3f>();
 			for(int i = 0; i < pt.npoints; i++) {
 				int x = pt.xpoints[i];
 				int y = pt.ypoints[i];
 
 				int index = y * w + x;
 				int t = FullerProjection.getTriangle(x, y, s);
 				if(t < 0) {
 					vIndices[index][0] = vIndices[index][1] = vIndices[index][2] = -1;
 					continue;
 				}
 
 				Point3f pf1 = flatVertices[flatFaces[3 * t]];
 				Point3f pf2 = flatVertices[flatFaces[3 * t + 1]];
 				Point3f pf3 = flatVertices[flatFaces[3 * t + 2]];
 
 				Vector3f v1 = new Vector3f(); v1.sub(pf2, pf1); v1.normalize();
 				Vector3f v2 = new Vector3f(); v2.sub(pf3, pf1); v2.normalize();
 				Vector3f p  = new Vector3f((x - pf1.x), (y - pf1.y), 0);
 
 				// solve d1.x * v1.x + d2.x * v2.x = p.x
 				//       d1.y * v1.y + d2.y * v2.y = p.y
 				float d2 = (v1.x * p.y - v1.y * p.x) / (v1.x * v2.y - v1.y * v2.x);
 				float d1 = (p.x - d2 * v2.x) / v1.x;
 				d1 /= pf1.distance(pf2);
 				d2 /= pf1.distance(pf3);
 
 				Point3f p1 = icosaVertices[icosaFaces[3 * t]];
 				Point3f p2 = icosaVertices[icosaFaces[3 * t + 1]];
 				Point3f p3 = icosaVertices[icosaFaces[3 * t + 2]];
 
 				Vector3f nearest = new Vector3f(p1);
 				v1.sub(p2, p1); v1.normalize();
 				v2.sub(p3, p1); v2.normalize();
 				nearest.scaleAdd(d1 * p1.distance(p2), v1, nearest);
 				nearest.scaleAdd(d2 * p1.distance(p3), v2, nearest);
 
 				// project onto sphere
 				nearest.sub(nearest, smp.center);
 				nearest.normalize();
 				Point3f ptmp = new Point3f();
 				ptmp.scaleAdd(smp.radius, nearest, smp.center);
 				pts.add(ptmp);
 			}
 			filename = new File(file).getName();
 			filename = filename.substring(0, filename.length() - 9) + ".pts";
 			savePoints(pts, new File(outputdir, filename));
 		}
 	}
 
 	private void savePoints(ArrayList<Point3f> pts, File outfile) throws IOException {
 		PrintStream out = new PrintStream(new FileOutputStream(outfile));
 		for(Point3f p : pts)
 			out.println(p.x + " " + p.y + " " + p.z);
 		out.close();
 	}
 
 	private ArrayList<Point3f> loadPoints(File file) throws IOException {
 		BufferedReader in = new BufferedReader(new FileReader(file));
 		String line = null;
 		ArrayList<Point3f> list = new ArrayList<Point3f>();
 		while((line = in.readLine()) != null) {
 			String[] toks = line.split(" ");
 			list.add(new Point3f(Float.parseFloat(toks[0]), Float.parseFloat(toks[1]), Float.parseFloat(toks[2])));
 		}
 		return list;
 	}
 
 	public void register(String dataDirectory, String outputDirectory) throws IOException {
 		// check and create files and folders
 		if(!new File(dataDirectory).isDirectory())
 			throw new IllegalArgumentException(dataDirectory + " is not a directory");
 
 		File outputdir = new File(outputDirectory);
 		if(outputdir.isDirectory() && outputdir.list().length > 0) {
 			boolean cancelled = !IJ.showMessageWithCancel("Overwrite",
 					outputdir + " already exists. Overwrite?");
 			if(cancelled)
 				return;
 		} else {
 			outputdir.mkdir();
 		}
 
 		File objfile = new File(dataDirectory, "Sphere.obj");
 		if(!objfile.exists())
 			throw new IllegalArgumentException("Cannot find " + objfile.getAbsolutePath());
 
 
 		if(!dataDirectory.endsWith(File.separator))
 			dataDirectory += File.separator;
 		if(!outputDirectory.endsWith(File.separator))
 			outputDirectory += File.separator;
 
 		SphericalMaxProjection src = new SphericalMaxProjection(objfile.getAbsolutePath());
 		createFullerProjection(src, dataDirectory);
 
 		// obtain list of local maxima files
 		List<String> tmp = new ArrayList<String>();
 		File fullerDir = new File(dataDirectory, "fuller");
 		tmp.addAll(Arrays.asList(fullerDir.list()));
 		for(int i = tmp.size() - 1; i >= 0; i--)
 			if(!tmp.get(i).endsWith(".pts"))
 				tmp.remove(i);
 		String[] files = new String[tmp.size()];
 		tmp.toArray(files);
 		Arrays.sort(files);
 
		// load spherical maximum projection for source
		src.saveSphere(outputDirectory + "Sphere.obj");
		String vName = files[0].substring(0, files[0].lastIndexOf('.')) + ".vertices";
		src.loadMaxima(dataDirectory + vName);
		src.saveMaxima(outputDirectory + vName);
 
 		Matrix4f overall = new Matrix4f();
 		overall.setIdentity();
 
		ArrayList<Point3f> tgtPts = loadPoints(new File(fullerDir, files[0]));

 		// register
 		for(int i = 1; i < files.length; i++) {
			System.out.println(files[i]);
 			ArrayList<Point3f> srcPts = loadPoints(new File(fullerDir, files[i]));
 
			// make a deep copy of src points, to be used as target points for the next iteration
			ArrayList<Point3f> nextTgtPts = new ArrayList<Point3f>(srcPts.size());
			for(Point3f p : srcPts)
				nextTgtPts.add(new Point3f(p));


			vName = files[i].substring(0, files[i].lastIndexOf('.')) + ".vertices";
 			src.loadMaxima(dataDirectory + vName);
 
 			Matrix4f mat = new Matrix4f();
 			mat.setIdentity();
 			ICPRegistration.register(tgtPts, srcPts, mat, src.center);
			// overall.mul(mat, overall);
			overall.mul(mat);
 			src.applyTransform(overall);
 
 			src.saveMaxima(outputDirectory + vName);

			tgtPts = nextTgtPts;
 			IJ.showProgress(i, files.length);
 		}
 		IJ.showProgress(1);
 	}
 }
