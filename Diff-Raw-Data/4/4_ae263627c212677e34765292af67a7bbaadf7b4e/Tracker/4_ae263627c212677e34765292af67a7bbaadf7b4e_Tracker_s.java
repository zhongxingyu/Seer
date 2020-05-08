 package tracker;
 
 import com.sun.squawk.util.MathUtils;
 import edu.wpi.first.wpilibj.IterativeRobot;
 import edu.wpi.first.wpilibj.Timer;
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
 
 /**
  * The VM is configured to automatically run this class, and to call the
  * functions corresponding to each mode, as described in the IterativeRobot
  * documentation. If you change the name of this class or the package after
  * creating this project, you must also update the manifest file in the resource
  * directory.
  */
 public class Tracker extends IterativeRobot {
 
     private static final double FOCAL_LENGTH = 240.0 / Math.tan(47.0 / 180.0 * Math.PI);
     private AxisCamera camera = AxisCamera.getInstance();
     private CriteriaCollection cc;
 
     /**
      * This function is run when the robot is first started up and should be
      * used for any initialization code.
      */
     public void robotInit() {
         cc = new CriteriaCollection();
         cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
         cc.addCriteria(NIVision.MeasurementType.IMAQ_MT_BOUNDING_RECT_HEIGHT, 40, 400, false);
     }
 
     /**
      * This function is called periodically during autonomous
      */
     public void autonomousPeriodic() {
     }
 
     /**
      * This function is called periodically during operator control
      */
     public void teleopPeriodic() {
         try {
             ColorImage img = camera.getImage();
             BinaryImage thresholdImg = img.thresholdRGB(0, 100, 230, 255, 230, 255);
             BinaryImage bigsImg = thresholdImg.removeSmallObjects(false, 2);
             BinaryImage convexHullImg = bigsImg.convexHull(false);
             BinaryImage filteredImg = convexHullImg.particleFilter(cc);
             ParticleAnalysisReport[] reports = filteredImg.getOrderedParticleAnalysisReports();
             if (filteredImg.getNumberParticles() >= 1) {
                 ParticleAnalysisReport report = reports[0];
                 //double offset = reports[0].center_mass_x - (img.getWidth() / 2);
                 int rectUpper = 240 - report.boundingRectTop;
                 int rectLower = 240 - (report.boundingRectTop - report.boundingRectHeight);
                 double phi = MathUtils.atan(rectUpper / FOCAL_LENGTH);
                 double theta = MathUtils.atan(rectLower / FOCAL_LENGTH);
                 SmartDashboard.putNumber("Phi: ", phi / Math.PI * 180.0);
                 SmartDashboard.putNumber("Theta: ", theta / Math.PI * 180.0);
                 //chassis.drive((-offset / 200), (offset / 200));
             }
             filteredImg.free();
             convexHullImg.free();
             bigsImg.free();
             thresholdImg.free();
             img.free();
             Timer.delay(0.05);
         } catch (AxisCameraException ex) {
             ex.printStackTrace();
         } catch (NIVisionException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * This function is called periodically during test mode
      */
     public void testPeriodic() {
     }
 }
