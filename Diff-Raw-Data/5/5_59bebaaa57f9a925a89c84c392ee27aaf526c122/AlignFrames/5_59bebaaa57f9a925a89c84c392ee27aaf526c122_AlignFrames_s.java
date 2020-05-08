 package rgbdslam;
 
 import april.jmat.LinAlg;
 import java.util.ArrayList;
 import java.util.List;
 import kinect.*;
 import kinect.Kinect.Frame;
 
 /**
  *
  * @author pdaquino
  */
 public class AlignFrames {
 
     public static final int DECIMATION_FACTOR = 10;
     
     private List<ImageFeature> currFeatures, lastFeatures;
     private ColorPointCloud currFullPtCloud, currDecimatedPtCloud;
     private ColorPointCloud lastFullPtCloud, lastDecimatedPtCloud;
 
     public AlignFrames(Kinect.Frame currFrame, Kinect.Frame lastFrame) {
         this.currFullPtCloud = makeFullPtCloud(currFrame);
         this.currFeatures = extractAndProjectFeatures(currFrame, currFullPtCloud);
         this.currDecimatedPtCloud = makeDecimatedPtCloud(currFrame);
 
        this.lastFullPtCloud = makeFullPtCloud(currFrame);
         this.lastFeatures = extractAndProjectFeatures(lastFrame, lastFullPtCloud);
        this.lastDecimatedPtCloud = makeDecimatedPtCloud(currFrame);
     }
 
     public AlignFrames(Kinect.Frame currFrame, List<ImageFeature> lastProjectedFeatures,
             ColorPointCloud lastFullPtCloud, ColorPointCloud lastDecimatedPtCloud) {
         this.currFullPtCloud = makeFullPtCloud(currFrame);
         this.currFeatures = extractAndProjectFeatures(currFrame, currFullPtCloud);
         this.currDecimatedPtCloud = makeDecimatedPtCloud(currFrame);
 
 
         this.lastFeatures = lastProjectedFeatures;
         this.lastFullPtCloud = lastFullPtCloud;
         this.lastDecimatedPtCloud = lastDecimatedPtCloud;
     }
 
     public double[][] align() {
         DescriptorMatcher dm = new DescriptorMatcher(lastFeatures, currFeatures);
         ArrayList<DescriptorMatcher.Match> matches = dm.match();
 
         ArrayList<DescriptorMatcher.Match> inliers = new ArrayList<DescriptorMatcher.Match>();
         double[][] transform = RANSAC.RANSAC(matches, inliers);
 
         ICP icp = new ICP(lastDecimatedPtCloud);
         transform = icp.match(currDecimatedPtCloud, transform);
         
         return transform;
     }
 
     public ColorPointCloud getLastDecimatedPtCloud() {
         return lastDecimatedPtCloud;
     }
 
     public List<ImageFeature> getLastFeatures() {
         return lastFeatures;
     }
 
     public ColorPointCloud getLastFullPtCloud() {
         return lastFullPtCloud;
     }
 
     public ColorPointCloud getCurrDecimatedPtCloud() {
         return currDecimatedPtCloud;
     }
 
     public List<ImageFeature> getCurrFeatures() {
         return currFeatures;
     }
 
     public ColorPointCloud getCurrFullPtCloud() {
         return currFullPtCloud;
     }
     
     
     private ColorPointCloud makeDecimatedPtCloud(Frame frame) {
         return new ColorPointCloud(frame, DECIMATION_FACTOR);
     }
 
     private ColorPointCloud makeFullPtCloud(Frame frame) {
         return new ColorPointCloud(frame);
     }
 
     private List<ImageFeature> extractAndProjectFeatures(Frame frame, ColorPointCloud fullPtCloud) {
         List<ImageFeature> allFeatures = OpenCV.extractFeatures(frame.argb, Constants.WIDTH);
         List<ImageFeature> features = new ArrayList<ImageFeature>();
         for (ImageFeature fc : allFeatures) {
             fc.setXyz(fullPtCloud.Project(LinAlg.copyDoubles(fc.xy())));
             if (fc.xyz()[2] != -1) {
                 features.add(fc);
             }
         }
         return features;
     }
 }
