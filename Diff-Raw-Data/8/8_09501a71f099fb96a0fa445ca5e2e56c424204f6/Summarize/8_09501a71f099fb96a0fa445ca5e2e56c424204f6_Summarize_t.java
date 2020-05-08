 package at.uniklu.itec.videosummary;
 
 import net.semanticmetadata.lire.imageanalysis.*;
 
 import javax.imageio.ImageIO;
 
 import com.infomata.data.*;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 import java.awt.image.BufferedImage;
 import java.awt.*;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.ConvolveOp;
 import java.awt.image.Kernel;
 import java.io.BufferedInputStream;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
import java.lang.management.GarbageCollectorMXBean;
 import java.math.MathContext;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 
 import org.opencv.core.*;
 import org.opencv.highgui.*;
 import org.opencv.imgproc.Imgproc;
 /**
  * A video summary generator based on FFMPEG.
  * Date: 11.07.2008
  * Time: 10:38:58
  * (c) 2008 Mathias Lux, Klaus Schoeffmann & Markus Waltl, ITEC, Klagenfurt University
  *
  * This source code is licensed under GPL. That means it is is free software;
  * you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * The code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this programm; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * @author Mathias Lux, mathias@juggle.at
  * @author Markus Waltl
  */
 public class Summarize {
 
     private String ffmpegCmdPrefix = "data\\ffmpeg.exe -i \"";
     private String ffmpegCmdSuffix = "\" -deinterlace -f rawvideo -pix_fmt yuv420p -";
     private String file;
     private int videoHeight;
     private int videoWidth;
     private LinkedList<LireFeature> ceddFeatures;
     private Class lireFeatureClass = CEDD.class;
     private double fps;
     private String outfile;
     private boolean fastProcessing = false;
     private int modul = 1;
     private int NUM_CLUSTERS = 3;
     private int totalFrames;
     private SummaryProgress prog = null;
     private boolean withNavdata = false;
     private boolean withContext = false;
     private float threshold = 1000; 
     private HashMap<Integer, Point3D> navInfo = null;
 	private LinkedList<Point3D> ptFeatures;
     // handle the actual output ...
     public static boolean outputSummaryFrames = true;
     public static boolean outputStripe = true;
     public static boolean paintClusterDistribution = true;
 
     
     public Summarize(String directory, String dataFile, String contextfile, Class feature, String outfile, int numClusters) throws IOException{
     	ceddFeatures = new LinkedList<LireFeature>();
     	ptFeatures = new LinkedList<Point3D>();
         NUM_CLUSTERS = numClusters;        
         lireFeatureClass = feature;
         this.outfile = outfile;
         this.file = directory;
         File dir = new File(directory);
         String headerString = "sync_frame"; //Change this if image filename changes 
         File data, context = null;
         
         File[] matchingFiles = dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith("jpg");
             }});
         
         int numFiles = matchingFiles.length;
         LinkedList<File> usedFiles = new LinkedList<File>();
         if(numFiles < numClusters)
         {
         	throw new IOException("Insufficient Data");
         }
         
         if(dataFile != null)
         {
         	data= new File(dataFile);
         	if(!data.exists())
         		throw new IOException("Navigational Data not found");
         	navInfo = parseNavigationInfo(data);
         	withNavdata = true;
         }
         
         if(contextfile != null)
         {
         	context = new File(contextfile);
         	if(!context.exists())
         		throw new IOException("Context Info not found");        	
         	withContext = true;
         }
         
         try{    
         	LireFeature contextFeature = null;
         	if(withContext)
         	{
         		BufferedImage img_context = ImageIO.read(context);        		
         		contextFeature = (LireFeature) lireFeatureClass.newInstance();
         		contextFeature.extract(img_context);
         	}
         	for(File frame : matchingFiles){
         		BufferedImage img = ImageIO.read(frame);
         		LireFeature c = (LireFeature) lireFeatureClass.newInstance();
         		c.extract(img);
         		if(withContext)
         		{
         			float dist = c.getDistance(contextFeature);
         			if(dist > threshold)
         				continue;
         		}
         		ceddFeatures.add(c);
         		usedFiles.add(frame);
         		if (withNavdata)
         		{
         			String fileName = frame.getName();
         			int endIndex = fileName.indexOf(".jpg");
         			int startIndex = headerString.length();
         			String seq = fileName.substring(startIndex, endIndex);
         			int seqNo = Integer.parseInt(seq);
         			Point3D point = navInfo.get(seqNo);
         			ptFeatures.add(point);
         		}        		        	
         	}
         	
         	if(withNavdata && ceddFeatures.size() != ptFeatures.size())
         	{
         		throw new IOException("Number of images does not match with number of points");        		
         	}
         	
         	Cluster[] clusters = findFrames(NUM_CLUSTERS, ceddFeatures, ptFeatures);
         	File outdir = new File(outfile);
         	outdir.mkdir();
         	System.loadLibrary("opencv_java246");
         	for(Cluster cluster: clusters){
         		ArrayList<Integer> temp = new ArrayList<Integer>(cluster.members);
         		double maxSharpness = 0;
         		int maxId = -1;
                 for (int id : temp) {
                 	File frame = usedFiles.get(id); 
                 	double sharpness = getImageSharpness(frame);
                 	if(sharpness > maxSharpness){
                 		maxSharpness = sharpness;
                 		maxId = id;
                 	}
                 }
                 File frame = usedFiles.get(maxId);
         		String output_file = outfile + File.separator + frame.getName();
         		System.out.println("Copying file "+output_file);
         		Path src = Paths.get(frame.getCanonicalPath());
         		Path dst = Paths.get(output_file);
         		Files.copy(src, dst);
         	}
         }catch(Exception e)
         {
         	e.printStackTrace();
         }
 	}
         
     private double getImageSharpness(File frame) {		
 		Mat img = Highgui.imread(frame.getAbsolutePath(), 0);
 		Mat dx , dy;
 		dx = new Mat();
 		dy = new Mat();
 		Imgproc.Sobel(img, dx, CvType.CV_32F, 1,0);
 		Imgproc.Sobel(img, dy, CvType.CV_32F, 0,1);
 		Core.magnitude(dx, dy, dx);
 		Scalar sum = Core.sumElems(dx);
		img.release();
		dx.release();
		dy.release();
		System.gc();
		System.gc();
		System.gc();
 		//System.out.println("Sum of gradients= "+sum);
 		return (sum.val[0]);
 	}
 
 	private HashMap<Integer, Point3D> parseNavigationInfo(File data) throws IOException{
 		DataFile read = DataFile.createReader("8859_1");
 		read.setDataFormat(new CSVFormat());
 		read.containsHeader(true);
 		HashMap<Integer, Point3D> pointCloud = new HashMap<Integer, Point3D>();
 		try {
 			read.open(data);
 			for (DataRow row = read.next(); row != null; row = read.next()) {
 				int seq = row.getInt(0);
 				double x = row.getDouble(1);
 				double y = row.getDouble(2);
 				double z = row.getDouble(3);
 				Point3D point = new Point3D(x,y,z);
 				pointCloud.put(seq, point);
 			}
 		}catch(Exception e)
 		{
 			throw new IOException("Error in parsing Navigational Info");
 		}
 		finally {
 			read.close();
 		}
 		return pointCloud;
 	}
 
 	public Summarize(String file, Class feature, String outfile, boolean faster, int numClusters, SummaryProgress progress) throws IOException {
         ceddFeatures = new LinkedList<LireFeature>();        
         NUM_CLUSTERS = numClusters;
         fastProcessing = faster;
         lireFeatureClass = feature;
         this.outfile = outfile;
         prog = progress;
         // call ffmpeg for info:
         this.file = file;
         System.out.println("Getting info from file.");
 
         String OS = System.getProperty("os.name").toLowerCase();
         if (OS.indexOf("windows") > -1) {
             ffmpegCmdPrefix = "data\\ffmpeg.exe";
         } else {
             ffmpegCmdPrefix = "ffmpeg";
         }
 
         Process process = null;
         process = Runtime.getRuntime().exec(new String[]{ffmpegCmdPrefix, "-i", file});
         InputStream error = process.getErrorStream();
         StringBuilder sb = new StringBuilder(512);
         int tmp = -1;
 
         while ((tmp = error.read()) > -1) {
             sb.append((char) tmp);
         }
 
         String duration = "";
         String video = "";
         if (OS.indexOf("windows") > -1) {
             duration = sb.substring(sb.indexOf("Duration: ") + "Duration: ".length(), sb.indexOf("\n", sb.indexOf("Duration: ")) - 1);
             video = sb.substring(sb.indexOf("Video: ") + "Video: ".length(), sb.indexOf("\n", sb.indexOf("Video: ")) - 1);
         } else {
             duration = sb.substring(sb.indexOf("Duration: ") + "Duration: ".length(), sb.indexOf("\n", sb.indexOf("Duration: ")));
             video = sb.substring(sb.indexOf("Video: ") + "Video: ".length(), sb.indexOf("\n", sb.indexOf("Video: ")));
         }
 
         System.out.println("video = " + video);
         System.out.println("duration = " + duration);
 
 
         StringTokenizer st = new StringTokenizer(duration, ",");
         String durationString_ = st.nextToken().trim();
         String[] dur = durationString_.split(":");
         double duration_ = ((Double.valueOf(dur[0]) * 60) + Double.valueOf(dur[1])) * 60 + Double.valueOf(dur[2]);
         @SuppressWarnings("unused")
         String starttime = st.nextToken().trim();
 
         String bitrate = st.nextToken().trim();
         int pos = bitrate.indexOf("bitrate:");
         if (pos != -1) {
             bitrate = bitrate.substring(9).trim();
         }
 
 
         st = new StringTokenizer(video, ",");
         String decoder = st.nextToken().trim();
         String format = st.nextToken().trim();
         String size = st.nextToken().trim();
 
         while (st.hasMoreTokens()) {
             String strFPS = st.nextToken().trim();
             pos = strFPS.indexOf("tb");
             if (pos != -1) {
             	String substr = strFPS.substring(0, pos);
             	int index = substr.indexOf("k");
             	int mult = 1;
             	if(index !=-1)
             	{            		
             		substr = substr.substring(0, index);
             		mult = 1000;
             	}
                 fps = Double.valueOf(substr) * mult;
             } else {
                 pos = strFPS.indexOf("fps");
                 if (pos != -1) {
                     fps = Double.valueOf(strFPS.substring(0, pos));
                 }
             }
         }
 
         // dimension_ = size;
 
         pos = size.indexOf("[");
         if (pos != -1) {
             size = size.substring(0, pos);
         }
         size = size.trim();
         String[] sizeArray = size.split("x");
 
         st = new StringTokenizer(duration, ",");
         String time = st.nextToken();
         String[] timeBits = time.split("\\:");
 
         totalFrames = (int) ((Integer.parseInt(timeBits[0]) * 60 * 60 + Integer.parseInt(timeBits[1]) * 60) * fps);
         totalFrames += Integer.parseInt(timeBits[2].split("\\.")[0]) * fps;
         totalFrames += Integer.parseInt(timeBits[2].split("\\.")[1]);
 
         videoWidth = Integer.parseInt(sizeArray[0]);
         videoHeight = Integer.parseInt(sizeArray[1]);
 
         process = Runtime.getRuntime().exec(new String[]{ffmpegCmdPrefix, "-i", file, "-deinterlace", "-f", "rawvideo",
                     "-pix_fmt", "yuv420p", "-"});
 
         InputStream in = process.getInputStream();
         error = process.getErrorStream();
         new Thread(new ErrorReaderThread(error)).start();
         summarize(in);
     }
 
     private void summarize(InputStream in) {
         int pixels = videoWidth * videoHeight;
         BufferedImage frame = new BufferedImage(videoWidth, videoHeight, BufferedImage.TYPE_INT_RGB);
         // change 1-dim array ... :( a lot faster ...
         // int[][][] raster = new int[videoWidth][videoHeight][3];
 
         byte uu, yy, vv;
         int[] rgb = new int[3];
         byte[] buffer = new byte[pixels + pixels / 2];
         long time = 0;
         int frameCount = 0;
         // use a 10 MB buffer:
         BufferedInputStream bin = new BufferedInputStream(in, 10 * 104 * 1024);
         try {
             boolean isReadable = true;
             while (isReadable) {
                 isReadable = readNextFrame(buffer, bin) > -1;
                 // the next frame has been read.
                 if (frameCount % modul == 0) { // take only every x-th frame
                     time = System.nanoTime();
                     for (int x = 0; x < videoWidth; x++) {
                         for (int y = 0; y < videoHeight; y++) {
                             yy = (buffer[y * videoWidth + x]);
                             uu = (buffer[pixels + (y >> 1) * (videoWidth >> 1) + (x >> 1)]);
                             vv = (buffer[pixels + (pixels >> 2) + (y >> 1) * (videoWidth >> 1) + (x >> 1)]);
                             yuv2rgb(toInt(yy), toInt(uu), toInt(vv), rgb);
                             // create a picture ...
                             frame.getRaster().setPixel(x, y, rgb);
                         }
 
                     }
                     LireFeature c = (LireFeature) lireFeatureClass.newInstance();
                     c.extract(frame);
                     ceddFeatures.add(c);
                     time = System.nanoTime() - time;
                 }
                 frameCount++;
                 if (frameCount % (int) fps == 0) {
                     System.out.println("Processed  " + (frameCount * 100) / totalFrames + "% (" + (time) / (1000 * 1000) + " ms per processed frame)");
                     if (prog != null) {
                         prog.reportProgress(((frameCount * 100) / totalFrames), "");
                     }
                 }
 
             }
             System.out.println("Processed 100%");
             if (prog != null) {
                 prog.reportProgress(100, "Analysis finished. Please wait until summary is generated.");
             }
 
             Cluster[] clusters = findFrames(NUM_CLUSTERS, ceddFeatures);
 //            extractWithFFMPEG(clusters);
             extractNative(clusters);
             if (prog != null) {
                 prog.reportProgress(0, "Finished");
                 prog.finished();
             }
             // -------------- debug 
             BufferedWriter bw = new BufferedWriter(new FileWriter("stats.out"));
             int count = 0;
 //            for (LireFeature l : ceddFeatures) {
 //                    bw.write(count + " " + l.getStringRepresentation() + "\n");
 //                    count++;
 //            }
             for (Cluster c : clusters) {
                 count++;
                 for (Integer frameNum : c.members) {
                     bw.write(count + " " + frameNum + "\n");
                 }
             }
             bw.close();
         // -------------- debug 
         } catch (IOException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         }
 
     }
 
     private void extractNative(Cluster[] clusters) throws IOException {
         System.out.println("Generating summary ...");
         Process process = Runtime.getRuntime().exec(new String[]{ffmpegCmdPrefix, "-i", file, "-deinterlace", "-f", "rawvideo",
                     "-pix_fmt", "yuv420p", "-"});
         InputStream in = process.getInputStream();
         InputStream error = process.getErrorStream();
         new Thread(new ErrorReaderThread(error)).start();
 
         int pixels = videoWidth * videoHeight;
         BufferedImage frame = new BufferedImage(videoWidth, videoHeight, BufferedImage.TYPE_INT_RGB);
 //        int[][][] raster = new int[videoWidth][videoHeight][3];
         HashMap<Integer, BufferedImage> images = new HashMap<Integer, BufferedImage>(clusters.length);
         byte uu, yy, vv;
         int[] rgb = new int[3];
         byte[] buffer = new byte[pixels + pixels / 2];
         long time;
         int frameCount = 0;
         // use a 10 MB buffer:
         BufferedInputStream bin = new BufferedInputStream(in, 10 * 104 * 1024);
         try {
             boolean isReadable = true;
             while (isReadable) {
                 isReadable = readNextFrame(buffer, bin) > -1;
                 // the next frame has been read.
                 if (isInClusters(frameCount, clusters)) {
                     for (int x = 0; x < videoWidth; x++) {
                         for (int y = 0; y < videoHeight; y++) {
                             yy = (buffer[y * videoWidth + x]);
                             uu = (buffer[pixels + (y >> 1) * (videoWidth >> 1) + (x >> 1)]);
                             vv = (buffer[pixels + (pixels >> 2) + (y >> 1) * (videoWidth >> 1) + (x >> 1)]);
                             yuv2rgb(toInt(yy), toInt(uu), toInt(vv), rgb);
                             // This creates an actual image from the rgb data:
                             frame.getRaster().setPixel(x, y, rgb);
                         }
                     }
                     images.put(frameCount, frame);
                     frame = new BufferedImage(videoWidth, videoHeight, BufferedImage.TYPE_INT_RGB);
                 }
                 frameCount++;
                 if (frameCount % (int) fps == 0) {
                     if (prog != null) {
                         prog.reportProgress(((frameCount * 100) / totalFrames), "Creating summary ...");
                     }
                 }
 
             }
             Arrays.sort(clusters);
             DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
             df.setMaximumFractionDigits(0);
             df.setMinimumIntegerDigits(5);
             df.setGroupingSize(0);
             String path = new File(file).getName();
             if (path.contains(".")) {
                 path = path.substring(0, path.lastIndexOf("."));
             }
             try {
                 File parentFile = new File(file).getParentFile();
                 if (parentFile != null) {
                     path = parentFile.getCanonicalPath() + File.separator + path;
                 }
             } catch (IOException e) {
                 System.err.println("Error creating path for outfiles. " + e.toString());
             }
             if (paintClusterDistribution) {
                 int indexHeight = 4, indexOffset = 4;
                 float[] matrix = {
                     0.111f, 0.111f, 0.111f,
                     0.111f, 0.111f, 0.111f,
                     0.111f, 0.111f, 0.111f,};
                 BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));
 
 
                 for (Cluster c : clusters) {
                     // -< creating the index image >---------------------------
                     BufferedImage index = new BufferedImage(frameCount + indexOffset, indexHeight + indexOffset, BufferedImage.TYPE_INT_RGB);
                     Graphics2D g2s = (Graphics2D) index.getGraphics();
                     g2s.setColor(Color.black);
                     g2s.fillRect(0, 0, index.getWidth(), index.getHeight());
                     g2s.setColor(Color.green.darker());
                     for (Integer frameId : c.members) {
                         g2s.fillRect(frameId * modul + indexOffset / 2, indexOffset / 2, 1 * modul, indexHeight);
                     }
                     // paint start and end:
                     g2s.setColor(Color.lightGray);
                     g2s.fillRect(indexOffset / 2, indexOffset / 2, 2, indexHeight);
                     g2s.fillRect(frameCount - 2 - indexOffset / 2, indexOffset / 2, 2, indexHeight);
                     // blur:
                     index = op.filter(index, null);
 
                     // -< outputting the frame >---------------------------
                     BufferedImage img = images.get(c.median * modul);
                     g2s = (Graphics2D) img.getGraphics();
                     g2s.drawImage(index, 0, img.getHeight() - indexHeight - 2, img.getWidth(), indexHeight, Color.black, null);
                 }
             }
             if (outputSummaryFrames) {
                 for (Cluster c : clusters) {
                     File outFile = new File(path + "_" + df.format(c.members.size()) + "_frame" + df.format(c.median * modul) + ".png");
                     ImageIO.write(images.get(c.median * modul), "png", outFile);
                 }
             }
             if (outputStripe) {
                 BufferedImage stripe = new BufferedImage((videoWidth * clusters.length) / 2, videoHeight / 2, BufferedImage.TYPE_INT_RGB);
                 Graphics2D g2s = (Graphics2D) stripe.getGraphics();
                 for (int i = 0; i < clusters.length; i++) {
                     g2s.drawImage(images.get(clusters[i].median * modul), videoWidth / 2 * i, 0, videoWidth / 2 * (i + 1), videoHeight / 2, 0, 0, videoWidth, videoHeight, null);
                 }
                 File outFile = new File(path + "_summary_stripe.png");
                 ImageIO.write(stripe, "png", outFile);
             }
             if (clusters.length > 2) {
                 BufferedImage summary;
                 if (clusters.length > 4) {
                     summary = new BufferedImage(videoWidth * 2, videoHeight,
                             BufferedImage.TYPE_INT_RGB);
                 } else {
                     summary = new BufferedImage(videoWidth + videoWidth / 2, videoHeight,
                             BufferedImage.TYPE_INT_RGB);
                 }
                 Graphics2D g2 = (Graphics2D) summary.getGraphics();
                 g2.drawImage(images.get(clusters[0].median * modul), 0, 0, videoWidth, videoHeight, null);
                 g2.drawImage(images.get(clusters[1].median * modul), videoWidth, 0, videoWidth / 2, videoHeight / 2, null);
                 g2.drawImage(images.get(clusters[2].median * modul), videoWidth, videoHeight / 2, videoWidth / 2, videoHeight / 2, null);
                 if (clusters.length > 4) {
                     g2.drawImage(images.get(clusters[3].median * modul), videoWidth + videoWidth / 2, 0, videoWidth / 2, videoHeight / 2, null);
                     g2.drawImage(images.get(clusters[4].median * modul), videoWidth + videoWidth / 2, videoHeight / 2, videoWidth / 2, videoHeight / 2, null);
                 }
                 ImageIO.write(summary, "png", new File(outfile));
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
 
 
     }
 
     private boolean isInClusters(int frameCount, Cluster[] clusters) {
         boolean retVal = false;
         for (Cluster c : clusters) {
             if (c.median * modul == frameCount) {
                 retVal = true;
             }
         }
         return retVal;
     }
 
     private Cluster[] findFrames(int numClusters, LinkedList<? extends LireFeature> list1, LinkedList<Point3D> list2) {
     	if(withNavdata == false)
     		return findFrames(numClusters, list1);
     
     	Cluster[] clusters = new Cluster[numClusters];
         for (int i = 0; i < clusters.length; i++) {
             clusters[i] = new Cluster();
             clusters[i].median = list1.size() / numClusters * i;
         }
         for (int i = 0; i < list1.size(); i++) {
             LireFeature t = list1.get(i);
             Point3D p = list2.get(i);
             double minDist = -1;
             Cluster toAdd = clusters[0];
             for (Cluster c : clusters) {
             	int median = c.median;
                 double v = t.getDistance(list1.get(median)) + p.getDistance(list2.get(median));
             	
                 if (minDist < 0) {
                     minDist = v;
                 } else {
                     if (minDist > v) {
                         minDist = v;
                         toAdd = c;
                     }
                 }
 
             }
             toAdd.members.add(i);
         }
         recomputeMedian(clusters, list1, list2);
         int changes = reArrangeClusters(clusters, list1, list2);
         int steps = 1;
         while (changes > 0) {
             System.out.println("Clustering step ...");
             if (prog != null) {
                 prog.reportProgress(0, "Clustering steps: " + steps++);
             }
             recomputeMedian(clusters, list1, list2);
             changes = reArrangeClusters(clusters, list1, list2);
         }
         recomputeMedian(clusters, list1, list2);
         return clusters;
     }
     	
     private int reArrangeClusters(Cluster[] clusters, LinkedList<? extends LireFeature> list1, LinkedList<Point3D> list2) {
     	int reArrangements = 0;
         for (Cluster c : clusters) {
             ArrayList<Integer> temp = new ArrayList<Integer>(c.members);
             for (int id : temp) {
             	int median = c.median;
                 double min = list1.get(id).getDistance(list1.get(median)) + list2.get(id).getDistance(list2.get(median));
                 Cluster toCopyTo = null;
                 for (Cluster candidate : clusters) {
                 	int median2 = candidate.median;
                     double v = list1.get(id).getDistance(list1.get(median2)) + list2.get(id).getDistance(list2.get(median2));
                     if (v < min) {
                         min = v;
                         toCopyTo = candidate;
                     }
                 }
                 if (toCopyTo != null) { // move to new Cluster
                     c.members.remove(id);
                     toCopyTo.members.add(id);
                     reArrangements++;
                 }
             }
         }
         return reArrangements;
 	}
 
 	private void recomputeMedian(Cluster[] clusters, LinkedList<? extends LireFeature> list1, LinkedList<Point3D> list2) {
 		for (Cluster c : clusters) {
             int median = -1;
             double minDist = 0;
             for (Iterator<Integer> iterator = c.members.iterator(); iterator.hasNext();) {
                 double totalDist = 0;
                 Integer id = iterator.next();
                 for (Iterator<Integer> secondIt = c.members.iterator(); secondIt.hasNext();) {
                     Integer id2 = secondIt.next();
                     totalDist += list1.get(id).getDistance(list1.get(id2)) + list2.get(id).getDistance(list2.get(id2));
                 }
                 if (median < 0) {
                     minDist = totalDist;
                     median = id;
                 } else {
                     if (totalDist < minDist) {
                         minDist = totalDist;
                         median = id;
                     }
                 }
             }
             if (median > 0) {
                 c.median = median;
             }
         }
 	}
 
 	private Cluster[] findFrames(int numClusters, LinkedList<? extends LireFeature> list) {
         Cluster[] clusters = new Cluster[numClusters];
         for (int i = 0; i < clusters.length; i++) {
             clusters[i] = new Cluster();
             clusters[i].median = list.size() / numClusters * i;
         }
         for (int i = 0; i < list.size(); i++) {
             LireFeature t = list.get(i);
             double minDist = -1;
             Cluster toAdd = clusters[0];
             for (Cluster c : clusters) {
                 float v = t.getDistance(list.get(c.median));
                 if (minDist < 0) {
                     minDist = v;
                 } else {
                     if (minDist > v) {
                         minDist = v;
                         toAdd = c;
                     }
                 }
 
             }
             toAdd.members.add(i);
         }
         recomputeMedian(clusters, list);
         int changes = reArrangeClusters(clusters, list);
         int steps = 1;
         while (changes > 0) {
             System.out.println("Clustering step ...");
             if (prog != null) {
                 prog.reportProgress(0, "Clustering steps: " + steps++);
             }
             recomputeMedian(clusters, list);
             changes = reArrangeClusters(clusters, list);
         }
         recomputeMedian(clusters, list);
         return clusters;
     }
 
     private void extractWithFFMPEG(Cluster[] clusters) {
         for (Cluster cluster : clusters) {
             System.out.println(cluster.toString());
             int frameNumber = cluster.median;
             int second = (int) (frameNumber / fps);
             frameNumber = frameNumber - ((int) (second * fps));
             int minute = 0;
             if (second > 59) {
                 minute = second / 60;
                 second = second % 60;
             }
             NumberFormat df = DecimalFormat.getInstance();
             df.setMaximumFractionDigits(0);
             df.setMinimumIntegerDigits(2);
             String ss = "00:" + df.format(minute) + ":" + df.format(second) + "." + frameNumber;
             try {
                 String command = ffmpegCmdPrefix + file + "\" -an -ss " + ss + " -t 00:00:00.1 -deinterlace tn_" + cluster.median + "_" + cluster.members.size() + "_%d.jpg";
                 Process process = Runtime.getRuntime().exec(command);
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     private int reArrangeClusters(Cluster[] clusters, LinkedList<? extends LireFeature> list) {
         int reArrangements = 0;
         for (Cluster c : clusters) {
             ArrayList<Integer> temp = new ArrayList<Integer>(c.members);
             for (int id : temp) {
                 float min = list.get(id).getDistance(list.get(c.median));
                 Cluster toCopyTo = null;
                 for (Cluster candidate : clusters) {
                     float v = list.get(id).getDistance(list.get(candidate.median));
                     if (v < min) {
                         min = v;
                         toCopyTo = candidate;
                     }
                 }
                 if (toCopyTo != null) { // move to new Cluster
                     c.members.remove(id);
                     toCopyTo.members.add(id);
                     reArrangements++;
                 }
             }
         }
         return reArrangements;
     }
 
     private void recomputeMedian(Cluster[] clusters, LinkedList<? extends LireFeature> list) {
         for (Cluster c : clusters) {
             int median = -1;
             double minDist = 0;
             for (Iterator<Integer> iterator = c.members.iterator(); iterator.hasNext();) {
                 double totalDist = 0;
                 Integer id = iterator.next();
                 for (Iterator<Integer> secondIt = c.members.iterator(); secondIt.hasNext();) {
                     Integer id2 = secondIt.next();
                     totalDist += list.get(id).getDistance(list.get(id2));
                 }
                 if (median < 0) {
                     minDist = totalDist;
                     median = id;
                 } else {
                     if (totalDist < minDist) {
                         minDist = totalDist;
                         median = id;
                     }
                 }
             }
             if (median > 0) {
                 c.median = median;
             }
         }
     }
 
     private int readNextFrame(byte[] buffer, InputStream in) throws IOException {
         int bytesRead = in.read(buffer);
         if (bytesRead < 0) {
             return -1;
         }
         while ((bytesRead < buffer.length)) {
             int i = in.read(buffer, bytesRead, buffer.length - bytesRead);
             if (i < 0) {
                 return -1;
             }
             bytesRead += i;
         }
         return bytesRead;
     }
 
     private int toInt(byte b) {
         return b & 0xff;
     }
 
     private int[] yuv2rgb(int y, int u, int v, int[] rgb) {
         int c = y - 16;
         int d = u - 128;
         int e = v - 128;
         rgb[0] = (298 * c + 409 * e + 128) >> 8;
         rgb[1] = (298 * c - 100 * d - 208 * e + 128) >> 8;
         rgb[2] = (298 * c + 516 * d + 128) >> 8;
 
         // clamp:
         for (int i = 0; i < rgb.length; i++) {
             rgb[i] = Math.min(rgb[i], 255);
             rgb[i] = Math.max(rgb[i], 0);
         }
         return rgb;
     }
 
     public static void main(String[] args) {
         String file = null;
         String feature = "cedd";
         String outfile = null;
         int numClusters = 3;
         boolean faster = false;
         int w = 0, h = 0, f = 0;
         for (int i = 0; i < args.length; i++) {
             String s = args[i];
             if (s.equals("-file") || s.startsWith("-i")) {
                 file = args[i + 1];
             } else if (s.equals("-f")) {
                 feature = args[i + 1].toLowerCase();
             } else if (s.startsWith("-s")) {
                 faster = true;
             } else if (s.equals("-n")) {
                 numClusters = Integer.parseInt(args[i + 1]);
             } else if (s.equals("-noframes")) {
                 outputSummaryFrames = false;
             } else if (s.equals("-nocluvis")) {
                 paintClusterDistribution = false;
             } else if (s.equals("-nostripe")) {
                 outputStripe = false;
             } else if (s.equals("-o")) {
                 outfile = args[i + 1];
             }
         }
         if (file == null) {
             printHelp();
             System.exit(1);
         } else if (!new File(file).exists()) {
             System.out.println("File does not exist.");
             printHelp();
             System.exit(1);
         }
         if (outfile == null) {
             outfile = new File(file).getName();
             if (outfile.contains(".")) {
                 outfile = outfile.substring(0, outfile.lastIndexOf("."));
             }
             try {
                 outfile = new File(file).getParentFile().getCanonicalPath() + File.separator + outfile;
             } catch (IOException e) {
                 e.printStackTrace();
             }
             outfile = outfile + "_summary.png";
             System.out.println("outfile = " + outfile);
         }
         try {
             Class featureClass = CEDD.class;
             if (feature.startsWith("tamura")) {
                 featureClass = Tamura.class;
             } else if (feature.startsWith("fcth")) {
                 featureClass = FCTH.class;
             } else if (feature.startsWith("acc")) {
                 featureClass = AutoColorCorrelogram.class;
             } else if (feature.startsWith("gabor")) {
                 featureClass = Gabor.class;
             } else if (feature.startsWith("colorhist")) {
                 featureClass = SimpleColorHistogram.class;
             }
             System.out.println("Starting a new summarization using feature " + featureClass.getName());
             new Summarize(file, featureClass, outfile, faster, numClusters, null);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private static void printHelp() {
         String help = "Running video summary generator:\n" +
                 "================================\n" +
                 "\n" +
                 "java Summarize.jar -i <video> [-f <feature>] [-o <summary-file>] [-n <number>] [-s] [-noframes] [-nostripe]\n" +
                 "-i ... input file, video needs to be supported by ffmpeg\n" +
                 "-f ... feature, the lire feature to use for summarization: \n" +
                 "       cedd (default), tamura, fcth, gabor, acc, colorhist\n" +
                 "-o ... outfile, the summary file to write (PNG, eg. summary.png)\n" +
                 "-n ... number of clusters, summary generated with > 2 clusters\n" +
                 "-s ... speed, process only one frame per second, recommended for videos > 1 min\n" +
                 "-noframes ... do not output the frames of the summary as single pictures\n" +
                 "-nostripe ... do not create a summary stripe\n" +
                 "-nocluvis ... do not paint a cluster visualization into summaries\n";
         System.out.println(help);
         System.exit(0);
     }
 }
 
 class Cluster implements Comparable {
 
     int median;
     HashSet<Integer> members = new HashSet<Integer>();
 
     public String toString() {
         StringBuilder sb = new StringBuilder(512);
         sb.append(median).append(": \t");
         for (Integer integer : members) {
             sb.append(integer);
             sb.append(", ");
         }
         return sb.toString();
     }
 
     public int compareTo(Object o) {
         return ((Cluster) o).members.size() - members.size();
     }
 }
 
 class Point3D{
 	double X,Y,Z;
 	
 	public Point3D(){X=0; Y=0; Z=0;}
 	
 	public double getDistance(Point3D point) {
 		double d = (X - point.X) * (X - point.X) + (Y - point.Y) * (Y - point.Y) + (Z - point.Z) * (Z - point.Z);
 		return Math.sqrt(d);
 	}
 
 	public Point3D(double xx, double yy, double zz){
 		X = xx; Y = yy; Z = zz;  
 	}
 	
 	public String toString(){
 		String str = "X = "+X+" Y = "+Y+" Z ="+Z;
 		return str;
 	}
 		
 	public double getX(){return X;}
 	
 	public double getY(){return Y;}
 	
 	public double getZ(){return Z;}	
 }
