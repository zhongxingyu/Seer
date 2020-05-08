 package storm2013.smartdashboard;
 
 import com.googlecode.javacpp.Pointer;
 import com.googlecode.javacv.CanvasFrame;
 import static com.googlecode.javacv.cpp.opencv_core.*;
 import static com.googlecode.javacv.cpp.opencv_imgproc.*;
 import edu.wpi.first.smartdashboard.camera.WPICameraExtension;
 import edu.wpi.first.smartdashboard.camera.WPILaptopCameraExtension;
 import edu.wpi.first.smartdashboard.properties.*;
 import edu.wpi.first.smartdashboard.robot.Robot;
 import edu.wpi.first.wpijavacv.StormCVUtil;
 import edu.wpi.first.wpijavacv.WPIColorImage;
 import edu.wpi.first.wpijavacv.WPIImage;
 import edu.wpi.first.wpilibj.tables.ITable;
 import java.awt.Color;
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  * This project depends on the following files from the SmartDashboard 
  * installation:
  *     SmartDashboard.jar
  *     extensions/WPICameraExtension.jar
  *     lib/NetworkTable_Client.jar
  *     extensions/lib/javacpp.jar
  *     extensions/lib/javacv-<environment>.jar (eg. javacv-windows-x86.jar)
  *     extensions/lib/javacv.jar
  *     extensions/lib/WPIJavaCV.jar
  * @author Joe
  */
 public class StormCV extends WPILaptopCameraExtension {
 //public class StormCV extends WPICameraExtension {
     public static final String NAME = "StormCV Target Tracker";
     
     // Dummy objects representing steps of the process (for processProperty)
     private static final Object _process_nothing    = new Object(),
                                 _process_threshold  = new Object(),
                                 _process_closeHoles = new Object(),
                                 _process_contours   = new Object(),
                                 _process_convexHull = new Object(),
                                 _process_select     = new Object(),
                                 _process_calculate  = new Object(),
                                 _process_all        = new Object();
     
     private static final Object _select_biggest     = new Object(),
                                 _select_closest     = new Object();
         
     private static final ITable outputTable = Robot.getTable();
     
     // Having aspects of the process editable as properties allows for quick
     // and easy tuning and testing.
     public final DoubleProperty
         fovxProperty = new DoubleProperty(this,"horizontal FOV", 47),  // as per datasheet
         fovyProperty = new DoubleProperty(this,"Vertical FOV", 36.13); // see http://photo.stackexchange.com/questions/21536/how-can-i-calculate-vertical-field-of-view-from-horizontal-field-of-view#21543;
  
     public final DoubleListProperty
         desiredXAnglesProperty = new DoubleListProperty(this,"Desired X angle",new double[]{0,0,0,0}),
         desiredYAnglesProperty = new DoubleListProperty(this,"Desired Y angles",new double[]{-2.5,-4.1,-5.5,-5.5});
     
     public final StringListProperty
         distanceKeysProperty = new StringListProperty(this,"Distance keys",new String[]{"Near",
                                                                                         "Center line",
                                                                                         "Opponent Auto",
                                                                                         "Feeder"});
     
     public final IntegerProperty 
         h0Property = new IntegerProperty(this, "Low Hue threshold",        50),
         h1Property = new IntegerProperty(this, "High Hue threshold",       90),
         s0Property = new IntegerProperty(this, "Low Saturation threshold", 220),
         s1Property = new IntegerProperty(this, "High Saturation threshold",255),
         v0Property = new IntegerProperty(this, "Low Value threshold",      50),
         v1Property = new IntegerProperty(this, "High Value threshold",     255);
         
     public final IntegerProperty
         holeClosingIterationsProperty = new IntegerProperty(this, "Hole Closing Iterations",2);
     
     public final DoubleProperty
         polygonApproxProperty = new DoubleProperty(this, "Polygon approximation parameter",10);
     
     public final DoubleProperty
         nearVertAngleProperty  = new DoubleProperty(this,"Nearly vertical angle",  70);
     
     public final DoubleProperty
         minAreaRatioProperty      = new DoubleProperty(this,"Minimum contour/image area ratio",0.5/100),
         min5ptHeightRatioProperty = new DoubleProperty(this,"Minimum contour/image height ratio for 5pt",7.0/100);
     public final DoubleProperty
         min3ptAspectRatioProperty = new DoubleProperty(this,"Minimum 3-pt aspect ratio",2.5),
         max3ptAspectRatioProperty = new DoubleProperty(this,"Maximum 3-pt aspect ratio",4),
         min2ptAspectRatioProperty = new DoubleProperty(this,"Minimum 2-pt aspect ratio",1.7),
         max2ptAspectRatioProperty = new DoubleProperty(this,"Maximum 2-pt aspect ratio",2.5);
         
     public final ColorProperty
         contourColor3ptProperty = new ColorProperty(this, "3pt Contour color", Color.red),
         contourColor2ptProperty = new ColorProperty(this, "2pt Contour color", Color.orange),
         contourColor5ptProperty = new ColorProperty(this, "5pt Contour color", Color.magenta),
         lineColorProperty       = new ColorProperty(this, "Line color",        Color.pink),
         crosshairColorProperty  = new ColorProperty(this, "Crosshair color",   Color.cyan);
     
     public final IntegerProperty
         crosshairSizeProperty = new IntegerProperty(this, "Crosshair size",10);
         
     public final MultiProperty
         processProperty = new MultiProperty(this, "Process until?"),
         selectProperty  = new MultiProperty(this, "Select for?");
     
     public final BooleanProperty
         useTestImageProperty = new BooleanProperty(this, "Use Test Image",false);
     
     public final DoubleProperty
         savePeriodProperty = new DoubleProperty(this,"Save period (s)",10);
     
     public final StringProperty
         saveLocationProperty = new StringProperty(this, "Save location",".");
     
     
     // Store these and update them in propertyChanged() because getValue()
     // has too much overhead to be called every time.
     private double _fovx,_fovy;
     private double[] _desiredXAngles,
                      _desiredYAngles;
     private final Map<String,Integer> _distanceIndices = new HashMap<>();
     private int _h0,_h1,
                 _s0,_s1,
                 _v0,_v1;
     private int _holeClosingIterations;
     private double _polygonApprox;
     private double _minAreaRatio,
                    _min5ptHeightRatio;
     private double _min3ptAspectRatio,
                    _max3ptAspectRatio,
                    _min2ptAspectRatio,
                    _max2ptAspectRatio;
     private Color _contourColor3pt,
                   _contourColor2pt,
                   _contourColor5pt,
                   _lineColor,
                   _crosshairColor;
     private int _crosshairSize;
     private double _nearVertSlope;
     private Object _process,
                    _select;
     private boolean _useTestImage;
     private String _saveLocation;
     private double _savePeriod;
     private long _prevSaveTime;
     
     CvPoint2D32f _desiredLocNormed;
     
     // This holds the image returned from processImage() (if the selected
     // processing mode replaces the rawImage instead of drawing on top of
     // it). It prevents SmartDashboard from crashing without any indication 
     // whatsoever.
     private WPIImage _ret;
     
     // Keep temporaries around so they aren't constantly being reallocated
     private CvSize   _size;
     private IplImage _hsv;
     private IplImage _bin;
     private IplImage _hueLow, _hueHigh;
     private IplImage _satLow, _satHigh;
     private IplImage _valLow, _valHigh;
     private IplConvKernel _morphology = IplConvKernel.create(3, 3, 1, 1, CV_SHAPE_RECT, null);;
     private CvMemStorage _storage;
     
     private WPIColorImage _loadedImage,
                           _processImage;
             
     private boolean  _sendResults         = true,
                      _displayIntermediate = false;
     
     private void _updateDistanceIndices() {
         _distanceIndices.clear();
         String[] values = distanceKeysProperty.getValue();
         if(values != null) {
             for(int i=0;i<values.length;++i) {
                 _distanceIndices.put(values[i], i);
             }
         }
     }
     
     private int _getDistanceIndex() {
         String key = Robot.getTable().getString("Distance",null);
         if(key == null) {
             return 0;
         }
         Integer ret = _distanceIndices.get(key);
         if(ret == null) {
             return 0;
         }
         return ret;
     }
 
     private void _initVars() {
         processProperty.add("Nothing",           _process_nothing);
         processProperty.add("Apply Threshold",   _process_threshold);
         processProperty.add("Close Holes",       _process_closeHoles);
         processProperty.add("Find Contours",     _process_contours);
         processProperty.add("Apply Convex Hull", _process_convexHull);
         processProperty.add("Select Contour",    _process_select);
         processProperty.add("Calculate data",    _process_calculate);
         processProperty.add("Everything",        _process_all);
         
         processProperty.setDefault("Everything");
         
         selectProperty.add("Largest",    _select_biggest);
         selectProperty.add("Closest",    _select_closest);
         
         selectProperty.setDefault("Closest");
         
         _fovx = fovxProperty.getValue();
         _fovy = fovyProperty.getValue();
         
         _desiredXAngles = desiredXAnglesProperty.getValue();
         _desiredYAngles = desiredYAnglesProperty.getValue();
         
         _h0 = h0Property.getValue();
         _h1 = h1Property.getValue();
         _s0 = s0Property.getValue();
         _s1 = s1Property.getValue();
         _v0 = v0Property.getValue();
         _v1 = v1Property.getValue();
         
         _holeClosingIterations = holeClosingIterationsProperty.getValue();
         
         _polygonApprox = polygonApproxProperty.getValue();
         
         _minAreaRatio = minAreaRatioProperty.getValue();
         
         _min3ptAspectRatio = min3ptAspectRatioProperty.getValue();
         _max3ptAspectRatio = max3ptAspectRatioProperty.getValue();
         _min2ptAspectRatio = min2ptAspectRatioProperty.getValue();
         _max2ptAspectRatio = max2ptAspectRatioProperty.getValue();
         
         _nearVertSlope = Math.tan(Math.toRadians(nearVertAngleProperty.getValue()));
 
         _contourColor3pt = contourColor3ptProperty.getValue();
         _contourColor2pt = contourColor2ptProperty.getValue();
         _contourColor5pt = contourColor5ptProperty.getValue();
         _lineColor = lineColorProperty.getValue();
         _crosshairColor = crosshairColorProperty.getValue();
         
         _crosshairSize = crosshairSizeProperty.getValue();
         
         _process = processProperty.getValue();
         _select  = selectProperty.getValue();
         
         _saveLocation = saveLocationProperty.getValue();
         _savePeriod = savePeriodProperty.getValue();
         
         _prevSaveTime = -1;
         
         _updateDistanceIndices();
     }
     
     @Override
     public void init() {
         super.init();
         
         _initVars();
         
         for(int i=0;i<prefixes.length;++i) {
             _sendData(i, false, 0, 0);
         }
     }
 
     @Override
     public void propertyChanged(Property property) {        
         if(property == fovxProperty) {
             _fovx = fovxProperty.getValue();
         } else if(property == fovyProperty) {
             _fovy = fovyProperty.getValue();
         } else if(property == desiredXAnglesProperty) {
             _desiredXAngles = desiredXAnglesProperty.getValue();
         } else if(property == desiredYAnglesProperty) {
             _desiredYAngles = desiredYAnglesProperty.getValue();
         } else if(property == distanceKeysProperty) {
             _updateDistanceIndices();
         } else if(property == h0Property) {
             _h0 = h0Property.getValue();
         } else if(property == h1Property) {
             _h1 = h1Property.getValue();
         } else if(property == s0Property) {
             _s0 = s0Property.getValue();
         } else if(property == s1Property) {
             _s1 = s1Property.getValue();
         } else if(property == v0Property) {
             _v0 = v0Property.getValue();
         } else if(property == v1Property) {
             _v1 = v1Property.getValue();
         } else if(property == holeClosingIterationsProperty) {
             _holeClosingIterations = holeClosingIterationsProperty.getValue();
         } else if(property == polygonApproxProperty) {
             _polygonApprox = polygonApproxProperty.getValue();
         } else if(property == nearVertAngleProperty) {
             _nearVertSlope = Math.tan(Math.toRadians(nearVertAngleProperty.getValue()));
         } else if(property == minAreaRatioProperty) {
             _minAreaRatio = minAreaRatioProperty.getValue();
         } else if(property == min5ptHeightRatioProperty) {
             _min5ptHeightRatio = min5ptHeightRatioProperty.getValue();
         } else if(property == min3ptAspectRatioProperty) {
             _min3ptAspectRatio = min3ptAspectRatioProperty.getValue();
         } else if(property == max3ptAspectRatioProperty) {
             _max3ptAspectRatio = max3ptAspectRatioProperty.getValue();
         } else if(property == min2ptAspectRatioProperty) {
             _min2ptAspectRatio = min2ptAspectRatioProperty.getValue();
         } else if(property == max2ptAspectRatioProperty) {
             _max2ptAspectRatio = max2ptAspectRatioProperty.getValue();
         } else if(property == useTestImageProperty) {
             _useTestImage = useTestImageProperty.getValue();
         } else if(property == contourColor3ptProperty) {
             _contourColor3pt = contourColor3ptProperty.getValue();
         } else if(property == contourColor2ptProperty) {
             _contourColor2pt = contourColor2ptProperty.getValue();
         } else if(property == lineColorProperty) {
             _lineColor = lineColorProperty.getValue();
         } else if(property == crosshairColorProperty) {
             _crosshairColor = crosshairColorProperty.getValue();
         } else if(property == crosshairSizeProperty) {
             _crosshairSize = crosshairSizeProperty.getValue();
         } else if(property == processProperty) {
             _process = processProperty.getValue();
         } else if(property == selectProperty) {
             _select = selectProperty.getValue();
         } else if(property == savePeriodProperty) {
             _savePeriod = savePeriodProperty.getValue();
         }
     }
     
     private String[] prefixes = { "3pt","2pt","5pt" };
     
     private void _sendData(int index,boolean found,double x,double y) {
         x *= _fovx/2;
         y *= _fovy/2;
         String prefix = prefixes[index];
         if(_sendResults) {
             outputTable.putBoolean(prefix + " Target Found?", found);
             outputTable.putNumber (prefix + " Target X Angle",     x);
             outputTable.putNumber (prefix + " Target Y Angle",     y);
         } else {
             if(found) {
                 System.out.println(prefix + " Target X Angle: " + x);
                 System.out.println(prefix + " Target Y Angle: " + y);
             } else {
                 System.out.println(prefix + " Target not found");
             }
         }
     }
     
     private void _sendTime(long nanoTime) {
         double msTime = nanoTime/1.0e6;
         if(_sendResults) {
             outputTable.putNumber("ms per Frame",msTime);
         } else {
             System.out.format("Processed in %f ms\n",msTime);
         }
     }
     
     private double _aspectRatio(CvPoint points) {
         ArrayList<Double> horizDx = new ArrayList<>(),
                           horizDy = new ArrayList<>(),
                           vertDx  = new ArrayList<>(),
                           vertDy  = new ArrayList<>();
         for(int i=0;i<4;++i) {
             int startIndex = i,
                 endIndex   = (i+1)%4;
             int startX = points.position(startIndex).x(),
                 startY = points.position(startIndex).y();
             int endX   = points.position(endIndex).x(),
                 endY   = points.position(endIndex).y();
             double dx = endX-startX,
                    dy = endY-startY;
             double absDx = Math.abs(dx),
                    absDy = Math.abs(dy);
             if(absDy < absDx) {
                 horizDx.add(dx);
                 horizDy.add(dy);
             } else {
                 vertDx.add(dx);
                 vertDy.add(dy);
             }
         }
         if(horizDx.size() != 2 || vertDx.size() != 2) {
             return 0;
         }
         
         double horiz,vert;
         
         double[] absHorizDx = { Math.abs(horizDx.get(0)),
                                 Math.abs(horizDx.get(1)) },
                  absHorizDy = { Math.abs(horizDy.get(0)),
                                 Math.abs(horizDy.get(1)) },
                  absVertDx  = { Math.abs(vertDx.get(0)),
                                 Math.abs(vertDx.get(1)) },
                  absVertDy  = { Math.abs(vertDy.get(0)),
                                 Math.abs(vertDy.get(1)) };
         if((horizDy.get(0)/horizDx.get(0) < 0) != (horizDy.get(1)/horizDx.get(1) < 0)) {
             System.out.println("dx override");
             horiz = (absHorizDx[0]+absHorizDx[1])/2;
         } else {
             horiz = (Math.hypot(absHorizDx[0], absHorizDy[0])
                      +Math.hypot(absHorizDx[1], absHorizDy[1]))
                     /2;
         }
         if((vertDx.get(0)/vertDy.get(0) < 0) != (vertDx.get(1)/vertDy.get(1) < 0)) {
             System.out.println("dy override");
             vert = (absVertDy[0]+absVertDy[1])/2;
         } else {
             vert = (Math.hypot(absVertDx[0], absVertDy[0])
                      +Math.hypot(absVertDx[1], absVertDy[1]))
                     /2;
         }
         
 //        System.out.format("%f/%f = %f\n",horiz,vert,horiz/vert);
         System.out.println("Aspect ratio: " + horiz/vert);
         return horiz/vert;
     }
     
     private void _doThreshold(WPIImage rawImage) {
         // Extract the IplImage so we can do OpenCV magic.
         IplImage image = StormCVUtil.getIplImage(rawImage);
         
         // Convert to HSV
         cvCvtColor(image, _hsv, CV_BGR2HSV);
                 
         // Split into individual color channels (from HSV)
         cvSplit(_hsv, _hueLow, _satLow, _valLow, null);
         
         // Apply thresholds
         // OpenCV can only do one-way threshold (less than or greater than
         // a specific value), so to have a ranged threshold, both thresholds
         // are performed then the results are ANDed together
         // The -1 is to make it an inclusive range (a >= n is equivalent to
         // a > n-1 for integers)
         cvThreshold(_hueLow,   _hueHigh,   _h0-1, 255, CV_THRESH_BINARY);
         cvThreshold(_hueLow,   _hueLow,    _h1,   255, CV_THRESH_BINARY_INV);
         
         cvThreshold(_satLow, _satHigh, _s0-1, 255, CV_THRESH_BINARY);
         cvThreshold(_satLow, _satLow,  _s1,   255, CV_THRESH_BINARY_INV);
         
         cvThreshold(_valLow,  _valHigh,  _v0-1, 255, CV_THRESH_BINARY);
         cvThreshold(_valLow,  _valLow,   _v1,   255, CV_THRESH_BINARY_INV);
         
         // ANDing the images leaves only the pixels within all of the ranges
         cvAnd(_hueLow, _hueHigh,   _bin, null);
         cvAnd(_bin,    _valLow,   _bin, null);
         cvAnd(_bin,    _valHigh,  _bin, null);
         cvAnd(_bin,    _satLow,  _bin, null);
         cvAnd(_bin,    _satHigh, _bin, null);
         
         if(_displayIntermediate) {
             _displayImage("Threshold",_bin);
         }
     }
     
     private void _closeHoles() {
         // Apply repeated dilations followed by repeated erosions in order
         // to close holes
         cvMorphologyEx(_bin, _bin, null, _morphology, CV_MOP_CLOSE, _holeClosingIterations);
 
         if(_displayIntermediate) {
             _displayImage("Hole Closing",_bin);
         }
     }
     
     private void _processContour(int index,WPIImage rawImage,CvSeq contour,Color color) {
         CvScalar cvColor = CV_RGB(color.getRed(),color.getGreen(),color.getBlue());
         IplImage target = StormCVUtil.getIplImage(rawImage);
         cvDrawContours(target,contour,cvColor,cvColor,0,2,8);
         
         if(_process != _process_convexHull) {
             double desiredXNormed = _desiredLocNormed.x(),
                    desiredYNormed = _desiredLocNormed.y();
             // desired location in screen pixels, for drawing purposes
             CvPoint desiredLoc = new CvPoint((int)((desiredXNormed +1)/2*rawImage.getWidth()),
                                              (int)((-desiredYNormed+1)/2*rawImage.getHeight()));
             
             int totalPoints = contour.total();
             if(totalPoints <= 0) {
                 // Something is REALLY wrong
                 System.err.println("No points in selected contour");
                 return;
             }
             
             CvPoint points = new CvPoint(contour.total());
             cvCvtSeqToArray(contour, points, CV_WHOLE_SEQ);
             
             double centerX = 0,
                    centerY = 0;
             
             for(int i=0;i<totalPoints;++i) {
                 CvPoint point = points.position(i);
                 centerX += point.x();
                 centerY += point.y();
             }
             
             centerX /= totalPoints;
             centerY /= totalPoints;
             
             cvLine(StormCVUtil.getIplImage(rawImage),
                    desiredLoc,
                    new CvPoint((int)centerX,(int)centerY),
                    CV_RGB(_lineColor.getRed(),_lineColor.getGreen(),_lineColor.getBlue()),
                    2,8,0);
             
             double centerXNormed = centerX/rawImage.getWidth() *2-1,
                    centerYNormed = -(centerY/rawImage.getHeight()*2-1);
             
             double offsetX = centerXNormed-_desiredLocNormed.x(),
                    offsetY = centerYNormed-_desiredLocNormed.y();
             
             _sendData(index,true,offsetX,offsetY);
         }
     }
 
     @Override
     public WPIImage processImage(WPIColorImage rawImage) {
        if(Robot.getTable().getBoolean("Running", false)) {
             long currTime = System.currentTimeMillis();
             if(_savePeriod >= 0 && (_prevSaveTime < 0 || _prevSaveTime + _savePeriod*1000 <= currTime)) { 
                 _prevSaveTime = currTime;
                 final IplImage raw = StormCVUtil.getIplImage(rawImage);
                 new Thread() {
                     private final String name = _saveLocation+"/Capture " + new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(new Date()) + ".jpg";
                     private final IplImage copy = IplImage.create(raw.cvSize(),raw.depth(),raw.nChannels());
                     {
                         cvCopy(raw, copy);
                     }
                     @Override
                     public void run() {
                         try {
                             File out = new File(name);
                             ImageIO.write(copy.getBufferedImage(), "jpg", out);
                         } catch (IOException ex) {
                            Logger.getLogger(StormCV.class.getName()).log(Level.SEVERE, "Save failed", ex);
                         } finally {
                             copy.deallocate();
                         }
                     }
                 }.start();
             }
         } else {
             _prevSaveTime = -1;
         }
         if(_useTestImage) {
             _processImage = new WPIColorImage(_loadedImage.getBufferedImage());
             rawImage = _processImage;
         }
         long startTime = System.nanoTime();
         if(_displayIntermediate) {
             _displayImage("Raw",StormCVUtil.getIplImage(rawImage));
         }
         
         // If we aren't doing any processing, leave the image as-is
         if(_process == _process_nothing) {
             return rawImage;
         }
         
         int distanceIndex = _getDistanceIndex();
         double desiredXAngle = _desiredXAngles[distanceIndex],
                desiredYAngle = _desiredYAngles[distanceIndex];
         _desiredLocNormed = new CvPoint2D32f(desiredXAngle/(_fovx/2),desiredYAngle/(_fovy/2));
         
         // Reallocate temporaries if the size has changed
         if(_size == null || _size.width() != rawImage.getWidth() || _size.height() != rawImage.getHeight()) {
             _size    = cvSize(rawImage.getWidth(),rawImage.getHeight());
             _hsv     = IplImage.create(_size, 8, 3);
             _bin     = IplImage.create(_size, 8, 1);
             _hueLow  = IplImage.create(_size, 8, 1);
             _hueHigh = IplImage.create(_size, 8, 1);
             _satLow  = IplImage.create(_size, 8, 1);
             _satHigh = IplImage.create(_size, 8, 1);
             _valLow  = IplImage.create(_size, 8, 1);
             _valHigh = IplImage.create(_size, 8, 1);
         }
         
         _doThreshold(rawImage);
         
         if(_process != _process_threshold) {
             _closeHoles();
         }
         
         if(_process == _process_threshold || _process == _process_closeHoles) {
             // Allocate _ret if it's the first time, otherwise reuse it.
             if(_ret == null) {
                 _ret = StormCVUtil.makeWPIGrayscaleImage(_bin);
             } else {
                 StormCVUtil.copyImage(_ret, _bin);
             }
             
             return _ret;
         }
         
         if(_storage == null) {
             _storage = CvMemStorage.create();
         } else {
             cvClearMemStorage(_storage);
         }
         
         CvSeq contours = new CvSeq();
         
         // Detects any contours in _bin. CV_RETR_EXTERNAL makes it only find the
         // outer contours of a shape, CV_CHAIN_APPROX_TC89_KCOS uses "Teh-Chin
         // Chain Approximation" -- I have no idea what that means yet.
         cvFindContours(_bin, _storage, contours, 256, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_TC89_KCOS);
         
         CvScalar crosshairColor = CV_RGB(_crosshairColor.getRed(),
                                          _crosshairColor.getGreen(),
                                          _crosshairColor.getBlue());
 
         double desiredXNormed = _desiredLocNormed.x(),
                desiredYNormed = _desiredLocNormed.y();
         CvPoint desiredLoc = new CvPoint((int)((desiredXNormed +1)/2*rawImage.getWidth()),
                                          (int)((-desiredYNormed+1)/2*rawImage.getHeight()));
         CvPoint left   = new CvPoint(desiredLoc.x()-_crosshairSize,desiredLoc.y()),
                 right  = new CvPoint(desiredLoc.x()+_crosshairSize,desiredLoc.y()),
                 top    = new CvPoint(desiredLoc.x(),desiredLoc.y()-_crosshairSize),
                 bottom = new CvPoint(desiredLoc.x(),desiredLoc.y()+_crosshairSize);
 
         cvLine(StormCVUtil.getIplImage(rawImage),
                left,right,
                crosshairColor,
                2,8,0);
         cvLine(StormCVUtil.getIplImage(rawImage),
                top,bottom,
                crosshairColor,
                2,8,0);
         
         if(contours == null || contours.isNull() || contours.total() == 0) {
             for(int i=0;i<prefixes.length;++i) {
                 _sendData(i,false,0,0);
             }
             return rawImage;
         }
         
         CvScalar color3pt = CV_RGB(_contourColor3pt.getRed(),_contourColor3pt.getGreen(),_contourColor3pt.getBlue());
         
         if(_displayIntermediate) {
             IplImage raw = StormCVUtil.getIplImage(rawImage);
             IplImage copy = IplImage.create(raw.cvSize(),raw.depth(),raw.nChannels());
             cvCopy(raw, copy);
             cvDrawContours(copy, contours, color3pt, color3pt, 1, 2, 8);
             
             _displayImage("Find Contours",copy);
             
             copy.deallocate();
         }
         
         if(_process == _process_contours) {
             cvDrawContours(StormCVUtil.getIplImage(rawImage), contours, color3pt, color3pt, 1, 2, 8);
             return rawImage;
         }
         
         ArrayList<CvSeq> convexContours = new ArrayList<>();
         while(contours != null && !contours.isNull()) {
             CvSeq convexHull = cvConvexHull2(contours, _storage, CV_CLOCKWISE, 1);
             CvSeq polygon    = cvApproxPoly(convexHull,convexHull.header_size(),_storage,CV_POLY_APPROX_DP,_polygonApprox,0);
             convexContours.add(polygon);
             contours = contours.h_next();
         }
         
         if(_process == _process_convexHull) {
             IplImage target = StormCVUtil.getIplImage(rawImage);
             for(CvSeq contour: convexContours) {
                 cvDrawContours(target,contour,color3pt,color3pt,0,2,8);
             }
             return rawImage;
         } else if(_displayIntermediate) {
             IplImage raw = StormCVUtil.getIplImage(rawImage);
             IplImage copy = IplImage.create(raw.cvSize(),raw.depth(),raw.nChannels());
             cvCopy(raw, copy);
             
             for(CvSeq contour: convexContours) {
                 cvDrawContours(copy,contour,color3pt,color3pt,0,2,8);
             }
             _displayImage("Convex Hull", copy);
             
             copy.deallocate();
         }
         
         int[] selectedIndices = { -1, -1, -1 };
         
         double minArea = rawImage.getWidth()*rawImage.getHeight()*_minAreaRatio;
         double[] largestAreas = { 0, 0 };
         double tallestHeight = 0;
         double[] smallestDistances = { 0, 0 };
         for(int i=0;i<convexContours.size();++i) {
             CvSeq contour = convexContours.get(i);
             CvRect boundingRect = cvBoundingRect(contour, 1);
             double area = boundingRect.width()*boundingRect.height();
             if(contour.total() != 2 && area < minArea) {
                 continue;
             }
             if(contour.total() != 4 && contour.total() != 2) {
                 continue;
             }
             CvPoint points = new CvPoint(contour.total());
             cvCvtSeqToArray(contour, points, CV_WHOLE_SEQ);
 
             int index;
             
             if(contour.total() == 2) {
                 System.out.println("Line");
                 index = 2;
                 double dx = Math.abs(points.position(1).x()-points.position(0).x()),
                        dy = Math.abs(points.position(1).y()-points.position(0).y());
                 if(dy < _nearVertSlope*dx || dy < _min5ptHeightRatio*rawImage.getHeight()) {
                     continue;
                 }
                 System.out.println("Vert line");
                 if(selectedIndices[index] == -1 || dy > tallestHeight) {
                     selectedIndices[index] = i;
                     tallestHeight          = dy;
                 }
             } else {
 
                 double aspectRatio = _aspectRatio(points);
 
                 if(aspectRatio > _min3ptAspectRatio && aspectRatio < _max3ptAspectRatio) {
                     index = 0;
                 } else if(aspectRatio > _min2ptAspectRatio && aspectRatio < _max2ptAspectRatio) {
                     index = 1;
                 } else {
                     continue;
                 }
 
                 if(_select == _select_biggest) {
                     if(selectedIndices[index] == -1 || area > largestAreas[index]) {
                         selectedIndices[index] = i;
                         largestAreas[index]    = area;
                     }
                 } else {
                     double centroidX = 0,centroidY = 0;
                     for(int j=0;j<contour.total();++j) {
                         centroidX += points.position(j).x();
                         centroidY += points.position(j).y();
                     }
                     centroidX /= contour.total();
                     centroidY /= contour.total();
 
                     // normalize
                     centroidX = centroidX*2/rawImage.getWidth()-1;
                     centroidY = centroidY*2/rawImage.getWidth()-1;
 
                     double dx = centroidX-_desiredLocNormed.x(),
                            dy = centroidY-_desiredLocNormed.y();
 
                     double dist = Math.sqrt(dx*dx+dy*dy);
                     if(selectedIndices[index] == -1 || dist < smallestDistances[index]) {
                         selectedIndices[index]   = i;
                         smallestDistances[index] = dist;
                     }
                 }
             }
         }
         
         Color[] colors = { _contourColor3pt,_contourColor2pt,_contourColor5pt };
         
         for(int i=0;i<selectedIndices.length;++i) {
             int selectedIndex = selectedIndices[i];
             if(selectedIndex == -1) {
                 _sendData(i,false, 0, 0);
                 continue;
             }
             
             CvSeq selectedContour = convexContours.get(selectedIndex);
             _processContour(i, rawImage, selectedContour, colors[i]);
         }
         
         long totalTime = System.nanoTime()-startTime;
         
         _sendTime(totalTime);
         
         return rawImage;
     }
     
     private ArrayList<IplImage> _displayedImages = new ArrayList<>();
     private ArrayList<CanvasFrame> _frames = new ArrayList<>();
     private void _displayImage(String title,IplImage image) {
         IplImage newImage = IplImage.create(image.cvSize(), image.depth(), image.nChannels());
         cvCopy(image, newImage);
         _displayedImages.add(newImage);
         CanvasFrame result = new CanvasFrame(title);
         result.showImage(newImage.getBufferedImage());
         _frames.add(result);
     }
     
     private static void _deallocateIfNonNull(Pointer p) {
         if(p != null && !p.isNull()) {
             p.deallocate();
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         _deallocateIfNonNull(_size);
         _deallocateIfNonNull(_bin);
         _deallocateIfNonNull(_hueLow);
         _deallocateIfNonNull(_hueHigh);
         _deallocateIfNonNull(_satLow);
         _deallocateIfNonNull(_satHigh);
         _deallocateIfNonNull(_valLow);
         _deallocateIfNonNull(_valHigh);
         for(IplImage image:_displayedImages) {
             _deallocateIfNonNull(image);
         }
         for(CanvasFrame frame:_frames) {
             if(frame.isVisible()) {
                 frame.setVisible(false);
                 frame.dispose();
             }
         }
     }
     
     public static void main(String[] args) {
         boolean showUsage = (args.length == 0);
         boolean flagShow  = false;
         if(!showUsage && args[0].equals("--show")) {
             flagShow  = true;
             if(args.length == 1) {
                 showUsage = true;
             }
         }
         
         if(showUsage) {
             System.out.println("Usage: [--show] [FILE1] ... [FILEN]");
             System.exit(0);
         }
         
         StormCV cv = new StormCV();
         
         cv._sendResults = false;
         
         cv._displayIntermediate = flagShow;
         cv._initVars();
         
         Scanner scanner = new Scanner(System.in);
         
         int start = flagShow ? 1 : 0;
         
         for(int i=start;i<args.length;++i) {
             String filename = args[i];
             
             System.out.println(filename + ":");
             
             WPIColorImage rawImage;
             try {
                 rawImage = new WPIColorImage(ImageIO.read(new File(filename)));
             } catch (IOException e) {
                 System.err.println("Could not find file!");
                 return;
             }
             
             WPIImage result;
             
             result = cv.processImage(rawImage);
             
             cv._displayImage("Result: " + filename, StormCVUtil.getIplImage(result));
             
             System.out.println("Press Enter to Continue...");
             scanner.nextLine();
         }
         System.out.println("Done!");
         System.out.println("Press Enter to Continue...");
         scanner.nextLine();
         System.exit(0);
     }
 }
