 package Jordan.bau5.FRC2115.subsystems;
 
 import edu.wpi.first.wpilibj.camera.AxisCamera;
 import edu.wpi.first.wpilibj.camera.AxisCameraException;
 import edu.wpi.first.wpilibj.command.Subsystem;
 import edu.wpi.first.wpilibj.image.BinaryImage;
 import edu.wpi.first.wpilibj.image.ColorImage;
 import edu.wpi.first.wpilibj.image.CriteriaCollection;
 import edu.wpi.first.wpilibj.image.NIVision.MeasurementType;
 import edu.wpi.first.wpilibj.image.NIVisionException;
 import edu.wpi.first.wpilibj.image.ParticleAnalysisReport;
 
 public class Camera extends Subsystem
 {
     private AxisCamera camera;
     private ColorImage image;
     private CriteriaCollection cc;
 
     public Camera()
     {
          camera = AxisCamera.getInstance();
          cc = new CriteriaCollection();
          cc.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 30, 400, false);
          cc.addCriteria(MeasurementType.IMAQ_MT_BOUNDING_RECT_WIDTH, 40, 400, false);
     }
 
     public int processImage()
     {
         try
         {
             if(!camera.freshImage())
                 image = camera.getImage();
             BinaryImage thresholdImage = image.thresholdHSL(0, 255, 0, 255, 165, 255);
             BinaryImage bigObjectsImage = thresholdImage.removeSmallObjects(false, 2);
             BinaryImage convexHullImage = bigObjectsImage.convexHull(false);
             BinaryImage filteredImage = convexHullImage.particleFilter(cc);
 
             ParticleAnalysisReport[] reports = filteredImage.getOrderedParticleAnalysisReports(4);
 
             int maxIndex = 0;
             double maxVal = -99;
             for(int i = 0; i < reports.length; i++)
             {
                 if(reports[i].center_mass_y_normalized > maxVal)
                 {
                     maxIndex = i;
                     maxVal = reports[i].center_mass_y_normalized;
                 }
             }
 
             filteredImage.free();
             convexHullImage.free();
             bigObjectsImage.free();
             thresholdImage.free();
             image.free();
 
            if(reports[maxIndex].center_mass_x_normalized > .05)
                 return -1;
            else if(reports[maxIndex].center_mass_x_normalized < .05)
                 return 1;
             else
                 return 0;
         }
         catch(Exception e)
         {
             System.out.println("Exception caught and ignored:");
             System.out.println(e);
             
             return 0;
         }
     }
 
     protected void initDefaultCommand()
     {
     }
 }
