 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import au.edu.jcu.v4l4j.CaptureCallback;
 import au.edu.jcu.v4l4j.DeviceInfo;
 import au.edu.jcu.v4l4j.FrameGrabber;
 import au.edu.jcu.v4l4j.ImageFormat;
 import au.edu.jcu.v4l4j.VideoDevice;
 import au.edu.jcu.v4l4j.VideoFrame;
 import au.edu.jcu.v4l4j.exceptions.V4L4JException;
 
 /**
  * The main class for showing the video feed and processing the video
  * data. Identifies ball and robot locations, and robot orientations.
  * 
  * @author s0840449
  */
 public class Vision extends WindowAdapter {
     
     private VideoDevice videoDev;
     private JLabel label;
     private JFrame windowFrame;
     private FrameGrabber frameGrabber;
     private Thread captureThread;
     private boolean stop;
     private int width, height;
     private WorldState worldState;
     private ThresholdsState thresholdsState;
     private PitchConstants pitchConstants;
     //private int[] xDistortion;
     //private int[] yDistortion;
     
     /**
      * Default constructor.
      * 
      * @param videoDevice           The video device file to capture from.
      * @param width                 The desired capture width.
      * @param height                The desired capture height.
      * @param videoStandard         The capture standard.
      * @param channel               The capture channel.
      * @param compressionQuality    The JPEG compression quality.
      * @param worldState
      * @param thresholdsState
      * @param pitchConstants
      * 
      * @throws V4L4JException   If any parameter if invalid.
      */
     public Vision(String videoDevice, int width, int height, int channel, int videoStandard,
             int compressionQuality, WorldState worldState, ThresholdsState thresholdsState,
             PitchConstants pitchConstants) throws V4L4JException {
         
         /* Set the state fields. */
         this.worldState = worldState;
         this.thresholdsState = thresholdsState;
         this.pitchConstants = pitchConstants;
         
         /* Initialise the GUI that displays the video feed. */
         initFrameGrabber(videoDevice, width, height, channel, videoStandard, compressionQuality);
         initGUI();
     }
     
      /**
      * Initialises a FrameGrabber object with the given parameters.
      * 
      * @param videoDevice           The video device file to capture from.
      * @param inWidth               The desired capture width.
      * @param inHeight              The desired capture height.
      * @param channel               The capture channel.
      * @param videoStandard         The capture standard.
      * @param compressionQuality    The JPEG compression quality.
      * 
      * @throws V4L4JException   If any parameter is invalid.
      */
     private void initFrameGrabber(String videoDevice, int inWidth, int inHeight, int channel,
             int videoStandard, int compressionQuality) throws V4L4JException {
         videoDev = new VideoDevice(videoDevice);
 
         DeviceInfo deviceInfo = videoDev.getDeviceInfo();
         // TODO: Check that deviceInfo has formats first.
         ImageFormat imageFormat = deviceInfo.getFormatList().getNativeFormat(0);
 
         frameGrabber = videoDev.getJPEGFrameGrabber(inWidth, inHeight, channel, videoStandard,
                 compressionQuality, imageFormat);
 
         frameGrabber.setCaptureCallback(new CaptureCallback() {
             public void exceptionReceived(V4L4JException e) {
                 System.err.println("WARNING: " + e.getMessage());
             }
 
             public void nextFrame(VideoFrame frame) {
                 // TODO: Recycle the frame first.
                 long before = System.currentTimeMillis();
                 processAndUpdateImage(frame.getBufferedImage(), before);
                 frame.recycle();
             }
         });
         
         frameGrabber.startCapture();
         
         /* The actual width and height are slightly wider/taller than
          * the FrameGrabber claims. */
        width = frameGrabber.getWidth();
        height = frameGrabber.getHeight();
     }
     
     /** 
      * Creates the graphical interface components and initialises them
      */
     private void initGUI() {
         
         windowFrame = new JFrame("Vision Window");
         label = new JLabel();
         windowFrame.getContentPane().add(label);
         windowFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         windowFrame.addWindowListener(this);
         windowFrame.setVisible(true);
         windowFrame.setSize(width, height);
         
     }
     
     /**
      * Catches the window closing event, so that we can free up resources
      * before exiting.
      * 
      * @param e         The window closing event.
      */
     public void windowClosing(WindowEvent e) {
         /* Dispose of the various swing and v4l4j components. */
         frameGrabber.stopCapture();
         videoDev.releaseFrameGrabber();
 
         windowFrame.dispose();   
         
         System.exit(0);
     }
 
     /**
      * Processes an input image, extracting the ball and robot positions and robot
      * orientations from it, and then displays the image (with some additional graphics
      * layered on top for debugging) in the vision frame.
      * 
      * @param image     The image to process and then show.
      */
     public void processAndUpdateImage(BufferedImage image, long before) {
         
         /*
         //Lens distortion - not working fully
         BufferedImage image = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);
         
         
         int centerX = 320;
         int centerY = 240;
         float k = (float) 0.006;
         
         for (int i = 0; i < 480; i++) {
             for (int j = 0; j < 640; j++) {
                 int x = (int) Math.floor(getRadialX(j, i, centerX, centerY, (float) Math.pow(k, 2)));
                 int y = (int) Math.floor(getRadialY(j, i, centerX, centerY, (float) Math.pow(k, 2)));
                 
                 if (y >= 480) { y = 1; }
                 if (x >= 640) { x = 1; }
                 if (y < 0) { y = 1; }
                 if (x < 0) { x = 1; }
                 
                 image.setRGB(j, i, input.getRGB(x, y));
             }
         }
         */
         
         
         /*
         for (int i = 0; i < image.getHeight(); i++) {
             for (int j = 0; j < image.getWidth(); j++) {
                 image.setRGB(j, i, input.getRGB(xDistortion[j], yDistortion[i]));
                 //image.setRGB(j, i, input.getRGB(j, i));
             }
         }
         */
         
         int ballX = 0;
         int ballY = 0;
         int numBallPos = 0;
         
         int blueX = 0;
         int blueY = 0;
         int numBluePos = 0;
         
         int yellowX = 0;
         int yellowY = 0;
         int numYellowPos = 0;
         
         ArrayList<Integer> ballXPoints = new ArrayList<Integer>();
         ArrayList<Integer> ballYPoints = new ArrayList<Integer>();
         ArrayList<Integer> blueXPoints = new ArrayList<Integer>();
         ArrayList<Integer> blueYPoints = new ArrayList<Integer>();
         ArrayList<Integer> yellowXPoints = new ArrayList<Integer>();
         ArrayList<Integer> yellowYPoints = new ArrayList<Integer>();
         
         int topBuffer = pitchConstants.topBuffer;
         int bottomBuffer = pitchConstants.bottomBuffer;
         int leftBuffer = pitchConstants.leftBuffer;
         int rightBuffer = pitchConstants.rightBuffer;
         
         /* For every pixel within the pitch, test to see if it belongs to the ball,
          * the yellow T, the blue T, either green plate or a grey circle. */
         for (int row = topBuffer; row < image.getHeight() - bottomBuffer; row++) {
             
             for (int column = leftBuffer; column < image.getWidth() - rightBuffer; column++) {
                 
                 /* The RGB colours and hsv values for the current pixel. */
                 Color c = new Color(image.getRGB(column, row));
                 float hsbvals[] = new float[3];
                 Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                 
                 /* Debug graphics for the grey circles and green plates.
                  * TODO: Move these into the actual detection. */
                 if (thresholdsState.isGrey_debug() && isGrey(c, hsbvals)) {
                     image.setRGB(column, row, 0xFFFF0099);
                 }
                 
                 if (thresholdsState.isGreen_debug() && isGreen(c, hsbvals)) {
                     image.setRGB(column, row, 0xFFFF0099);
                 }
                 
                 /* Is this pixel part of the Blue T? */
                 if (isBlue(c, hsbvals) ){
 
                     blueX += column;
                     blueY += row;
                     numBluePos++;
 
                     blueXPoints.add(column);
                     blueYPoints.add(row);
 
                     /* If we're in the "Blue" tab, we show what pixels we're looking at,
                      * for debugging and to help with threshold setting. */
                     if (thresholdsState.isBlue_debug()) {
                         image.setRGB(column, row, 0xFFFF0099);
                     }
 
                 }
                 
                 /* Is this pixel part of the Yellow T? */
                 if (isYellow(c, hsbvals)) {
 
                     yellowX += column;
                     yellowY += row;
                     numYellowPos++;
 
                     yellowXPoints.add(column);
                     yellowYPoints.add(row);
 
                     /* If we're in the "Yellow" tab, we show what pixels we're looking at,
                      * for debugging and to help with threshold setting. */
                     if (thresholdsState.isYellow_debug()) {
                         image.setRGB(column, row, 0xFFFF0099);
                     }
                 }
                 
                 /* Is this pixel part of the Ball? */
                 if (isBall(c, hsbvals)) {
 
                     ballX += column;
                     ballY += row;
                     numBallPos++;
 
                     ballXPoints.add(column);
                     ballYPoints.add(row);
                     
                     /* If we're in the "Ball" tab, we show what pixels we're looking at,
                      * for debugging and to help with threshold setting. */
                     if (thresholdsState.isBall_debug()) {
                         image.setRGB(column, row, 0xFF000000);
                     }
                 }
             }
         }
         
         /* Position objects to hold the centre point of the ball and both robots. */
         Position ball;
         Position blue;
         Position yellow;
         
         /* If we have only found a few 'Ball' pixels, chances are that the ball has not
          * actually been detected. */
         if (numBallPos > 5) {
             ballX /= numBallPos;
             ballY /= numBallPos;
             
             ball = new Position(ballX, ballY);
             ball.fixValues(worldState.getBallX(), worldState.getBallY());
             ball.filterPoints(ballXPoints, ballYPoints);
         } else {
             ball = new Position(worldState.getBallX(), worldState.getBallY());
         }
         
         /* If we have only found a few 'Blue' pixels, chances are that the ball has not
          * actually been detected. */
         if (numBluePos > 0) {
             blueX /= numBluePos;
             blueY /= numBluePos;
             
             blue = new Position(blueX, blueY);
             blue.fixValues(worldState.getBlueX(), worldState.getBlueY());
             blue.filterPoints(blueXPoints, blueYPoints);
         } else {
             blue = new Position(worldState.getBlueX(), worldState.getBlueY());
         }
 
         /* If we have only found a few 'Yellow' pixels, chances are that the ball has not
          * actually been detected. */
         if (numYellowPos > 0) {
             yellowX /= numYellowPos;
             yellowY /= numYellowPos;
             
             yellow = new Position(yellowX, yellowY);
             yellow.fixValues(worldState.getYellowX(), worldState.getYellowY());
             yellow.filterPoints(yellowXPoints, yellowYPoints);
         } else {
             yellow = new Position(worldState.getYellowX(), worldState.getYellowY());
         }
 
         
         
         /* Attempt to find the blue robot's orientation. */
 		try {
 			float blueOrientation = findOrientation(blueXPoints, blueYPoints, blue.getX(), blue.getY(), image, true);
 	        float diff = Math.abs(blueOrientation - worldState.getBlueOrientation());
 	        if (diff > 0.1) {
 	            float angle = (float) Math.round(((blueOrientation / Math.PI) * 180) / 5) * 5;
 	            worldState.setBlueOrientation((float) (angle / 180 * Math.PI));
 	        }
 		} catch (NoAngleException e) {
 			worldState.setBlueOrientation(worldState.getBlueOrientation());
 			System.out.println("Blue robot: " + e.getMessage());
 		}
 
         
         /* Attempt to find the yellow robot's orientation. */
 		try {
 			float yellowOrientation = findOrientation(yellowXPoints, yellowYPoints, yellow.getX(), yellow.getY(), image, true);
 	        float diff = Math.abs(yellowOrientation - worldState.getYellowOrientation());
 	        if (yellowOrientation != 0 && diff > 0.1) {
 	            float angle = (float) Math.round(((yellowOrientation / Math.PI) * 180) / 5) * 5;
 	            worldState.setYellowOrientation((float) (angle / 180 * Math.PI));
 	        }
 		} catch (NoAngleException e) {
 			worldState.setYellowOrientation(worldState.getYellowOrientation());
 			System.out.println("Yellow robot: " + e.getMessage());
 		}
 
         
         worldState.setBallX(ball.getX());
         worldState.setBallY(ball.getY());
         
         worldState.setBlueX(blue.getX());
         worldState.setBlueY(blue.getY());
         worldState.setYellowX(yellow.getX());
         worldState.setYellowY(yellow.getY());
         worldState.updateCounter();
         
         /* Draw the image onto the vision frame. */
         Graphics frameGraphics = label.getGraphics();
             
         Graphics imageGraphics = image.getGraphics();
 
         /* Only display these markers in non-debug mode. */
         if (!(thresholdsState.isBall_debug() || thresholdsState.isBlue_debug() 
                 || thresholdsState.isYellow_debug() || thresholdsState.isGreen_debug()
                 || thresholdsState.isGrey_debug())) {
             imageGraphics.setColor(Color.red);
             imageGraphics.drawLine(0, ball.getY(), 640, ball.getY());
             imageGraphics.drawLine(ball.getX(), 0, ball.getX(), 480);
             imageGraphics.setColor(Color.blue);
             imageGraphics.drawOval(blue.getX()-15, blue.getY()-15, 30,30);
             imageGraphics.setColor(Color.yellow);
             imageGraphics.drawOval(yellow.getX()-15, yellow.getY()-15, 30,30);
             imageGraphics.setColor(Color.white);
             
             /*
             float ax = (float) Math.cos(worldState.getBlueOrientation());
             float ay = (float) Math.sin(worldState.getBlueOrientation());
             imageGraphics.drawLine(blue.getX(), blue.getY(), (int) (ax*70), (int) (ay*70));
             
             ax = (float) Math.sin(worldState.getYellowOrientation());
             ay = (float) Math.cos(worldState.getYellowOrientation());
             imageGraphics.drawLine(yellow.getX(), yellow.getY(), (int) (ax*70), (int) (ay*70));
             */
         }
 
         /* Used to calculate the FPS. */
         long after = System.currentTimeMillis();
         
         /* Display the FPS that the vision system is running at. */
         float fps = (1.0f)/((after - before) / 1000.0f);
         imageGraphics.setColor(Color.white);
         imageGraphics.drawString("FPS: " + fps, 15, 15);
         
         frameGraphics.drawImage(image, 0, 0, width, height, null);
         
 
     }
 
     /**
      * Determines if a pixel is part of the blue T, based on input RGB colours
      * and hsv values. 
      * 
      * @param color         The RGB colours for the pixel.
      * @param hsbvals       The HSV values for the pixel.
      * 
      * @return              True if the RGB and HSV values are within the defined
      *                      thresholds (and thus the pixel is part of the blue T),
      *                      false otherwise.
      */
     private boolean isBlue(Color color, float[] hsbvals) {
         return hsbvals[0] <= thresholdsState.getBlue_h_high() && hsbvals[0] >= thresholdsState.getBlue_h_low() &&
         hsbvals[1] <= thresholdsState.getBlue_s_high() && hsbvals[1] >= thresholdsState.getBlue_s_low() &&
         hsbvals[2] <= thresholdsState.getBlue_v_high() && hsbvals[2] >= thresholdsState.getBlue_v_low() &&
         color.getRed() <= thresholdsState.getBlue_r_high() && color.getRed() >= thresholdsState.getBlue_r_low() &&
         color.getGreen() <= thresholdsState.getBlue_g_high() && color.getGreen() >= thresholdsState.getBlue_g_low() &&
         color.getBlue() <= thresholdsState.getBlue_b_high() && color.getBlue() >= thresholdsState.getBlue_b_low();
     }
 
     /**
      * Determines if a pixel is part of the yellow T, based on input RGB colours
      * and hsv values. 
      * 
      * @param color         The RGB colours for the pixel.
      * @param hsbvals       The HSV values for the pixel.
      * 
      * @return              True if the RGB and HSV values are within the defined
      *                      thresholds (and thus the pixel is part of the yellow T),
      *                      false otherwise.
      */
     private boolean isYellow(Color colour, float[] hsbvals) {
 
         return hsbvals[0] <= thresholdsState.getYellow_h_high() && hsbvals[0] >= thresholdsState.getYellow_h_low() &&
         hsbvals[1] <= thresholdsState.getYellow_s_high() &&  hsbvals[1] >= thresholdsState.getYellow_s_low() &&
         hsbvals[2] <= thresholdsState.getYellow_v_high() &&  hsbvals[2] >= thresholdsState.getYellow_v_low() &&
         colour.getRed() <= thresholdsState.getYellow_r_high() &&  colour.getRed() >= thresholdsState.getYellow_r_low() &&
         colour.getGreen() <= thresholdsState.getYellow_g_high() && colour.getGreen() >= thresholdsState.getYellow_g_low() &&
         colour.getBlue() <= thresholdsState.getYellow_b_high() && colour.getBlue() >= thresholdsState.getYellow_b_low();
 
     }
 
     /**
      * Determines if a pixel is part of the ball, based on input RGB colours
      * and hsv values. 
      * 
      * @param color         The RGB colours for the pixel.
      * @param hsbvals       The HSV values for the pixel.
      * 
      * @return              True if the RGB and HSV values are within the defined
      *                      thresholds (and thus the pixel is part of the ball),
      *                      false otherwise.
      */
     private boolean isBall(Color colour, float[] hsbvals) {
 
         return hsbvals[0] <= thresholdsState.getBall_h_high() && hsbvals[0] >= thresholdsState.getBall_h_low() &&
         hsbvals[1] <= thresholdsState.getBall_s_high() &&  hsbvals[1] >= thresholdsState.getBall_s_low() &&
         hsbvals[2] <= thresholdsState.getBall_v_high() &&  hsbvals[2] >= thresholdsState.getBall_v_low() &&
         colour.getRed() <= thresholdsState.getBall_r_high() &&  colour.getRed() >= thresholdsState.getBall_r_low() &&
         colour.getGreen() <= thresholdsState.getBall_g_high() && colour.getGreen() >= thresholdsState.getBall_g_low() &&
         colour.getBlue() <= thresholdsState.getBall_b_high() && colour.getBlue() >= thresholdsState.getBall_b_low();
 
     }
 
     /**
      * Determines if a pixel is part of either grey circle, based on input RGB colours
      * and hsv values. 
      * 
      * @param color         The RGB colours for the pixel.
      * @param hsbvals       The HSV values for the pixel.
      * 
      * @return              True if the RGB and HSV values are within the defined
      *                      thresholds (and thus the pixel is part of a grey circle),
      *                      false otherwise.
      */
     private boolean isGrey(Color colour, float[] hsbvals) {
 
         return hsbvals[0] <= thresholdsState.getGrey_h_high() && hsbvals[0] >= thresholdsState.getGrey_h_low() &&
         hsbvals[1] <= thresholdsState.getGrey_s_high() &&  hsbvals[1] >= thresholdsState.getGrey_s_low() &&
         hsbvals[2] <= thresholdsState.getGrey_v_high() &&  hsbvals[2] >= thresholdsState.getGrey_v_low() &&
         colour.getRed() <= thresholdsState.getGrey_r_high() &&  colour.getRed() >= thresholdsState.getGrey_r_low() &&
         colour.getGreen() <= thresholdsState.getGrey_g_high() && colour.getGreen() >= thresholdsState.getGrey_g_low() &&
         colour.getBlue() <= thresholdsState.getGrey_b_high() && colour.getBlue() >= thresholdsState.getGrey_b_low();
 
     }
 
     /**
      * Determines if a pixel is part of either green plate, based on input RGB colours
      * and hsv values. 
      * 
      * @param color         The RGB colours for the pixel.
      * @param hsbvals       The HSV values for the pixel.
      * 
      * @return              True if the RGB and HSV values are within the defined
      *                      thresholds (and thus the pixel is part of a green plate),
      *                      false otherwise.
      */
     private boolean isGreen(Color colour, float[] hsbvals) {
         
         return hsbvals[0] <= thresholdsState.getGreen_h_high() && hsbvals[0] >= thresholdsState.getGreen_h_low() &&
         hsbvals[1] <= thresholdsState.getGreen_s_high() &&  hsbvals[1] >= thresholdsState.getGreen_s_low() &&
         hsbvals[2] <= thresholdsState.getGreen_v_high() &&  hsbvals[2] >= thresholdsState.getGreen_v_low() &&
         colour.getRed() <= thresholdsState.getGreen_r_high() &&  colour.getRed() >= thresholdsState.getGreen_r_low() &&
         colour.getGreen() <= thresholdsState.getGreen_g_high() && colour.getGreen() >= thresholdsState.getGreen_g_low() &&
         colour.getBlue() <= thresholdsState.getGreen_b_high() && colour.getBlue() >= thresholdsState.getGreen_b_low();
         
     }
     
     /**
      * Finds the orientation of a robot, given a list of the points contained within it's
      * T-shape (in terms of a list of x coordinates and y coordinates), the mean x and 
      * y coordinates, and the image from which it was taken.
      * 
      * @param xpoints           The x-coordinates of the points contained within the T-shape.
      * @param ypoints           The y-coordinates of the points contained within the T-shape.
      * @param meanX             The mean x-point of the T.
      * @param meanY             The mean y-point of the T.
      * @param image             The image from which the points were taken.
      * @param showImage         A boolean flag - if true a line will be drawn showing
      *                          the direction of orientation found.
      * 
      * @return                  An orientation from -Pi to Pi degrees.
      * @throws NoAngleException 
      */
     public float findOrientation(ArrayList<Integer> xpoints, ArrayList<Integer> ypoints,
             int meanX, int meanY, BufferedImage image, boolean showImage) throws NoAngleException {
 
         /* TODO: Get Andy to go through this and explain it. */
         
         assert (xpoints.size() == ypoints.size()) :
             "Error: Must be equal number of x and y points!";
         
         if (xpoints.size() == 0) {
             throw new NoAngleException("No T pixels");
         }
         
         int stdev = 0;
         /* Standard deviation */
         for (int i = 0; i < xpoints.size(); i++) {
             int x = xpoints.get(i);
             int y = ypoints.get(i);
             
             stdev += Math.pow(Math.sqrt(Position.sqrdEuclidDist(x, y, meanX, meanY)), 2);
         }
         stdev  = (int) Math.sqrt(stdev / xpoints.size());
 
         /* Find the position of the front of the T. */
         int frontX = 0;
         int frontY = 0;
         int frontCount = 0;
         for (int i = 0; i < xpoints.size(); i++) {
             if (stdev > 15) {
                 if (Math.abs(xpoints.get(i) - meanX) < stdev && Math.abs(ypoints.get(i) - meanY) < stdev &&
                         Position.sqrdEuclidDist(xpoints.get(i), ypoints.get(i), meanX, meanY) > Math.pow(15, 2)) {
                     frontCount++;
                     frontX += xpoints.get(i);
                     frontY += ypoints.get(i);
                 }
             } else {
                 if (Position.sqrdEuclidDist(xpoints.get(i), ypoints.get(i), meanX, meanY) > Math.pow(15, 2)) {
                     frontCount++;
                     frontX += xpoints.get(i);
                     frontY += ypoints.get(i);
                 }
             }
         }
 
         /* If no points were found, we'd better bail. */
         if (frontCount == 0) {
         	throw new NoAngleException("Front of T was not found");
         }
             
         /* Otherwise, get the frontX and Y. */
         frontX /= frontCount;
         frontY /= frontCount;
 
         /* In here, calculate the vector between meanX/frontX and
          * meanY/frontY, and then get the angle of that vector. */
         
         // Calculate the angle from center of the T to the front of the T
         float length = (float) Math.sqrt(Math.pow(frontX - meanX, 2)
                 + Math.pow(frontY - meanY, 2));
         float ax = (frontX - meanX) / length;
         float ay = (frontY - meanY) / length;
         float angle = (float) Math.acos(ax);
 
         if (frontY < meanY) {
             angle = -angle;
         }
         
         //Look in a cone in the opposite direction to try to find the grey circle
         ArrayList<Integer> greyXPoints = new ArrayList<Integer>();
         ArrayList<Integer> greyYPoints = new ArrayList<Integer>();
         
         for (int a=-20; a < 21; a++) {
             ax = (float) Math.cos(angle+((a*Math.PI)/180));
             ay = (float) Math.sin(angle+((a*Math.PI)/180));
             for (int i = 15; i < 25; i++) {
                 int greyX = meanX - (int) (ax * i);
                 int greyY = meanY - (int) (ay * i);
                 try {
                     Color c = new Color(image.getRGB(greyX, greyY));
                     float hsbvals[] = new float[3];
                     Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                     if (isGrey(c, hsbvals)) {
                         greyXPoints.add(greyX);
                         greyYPoints.add(greyY);
                     }
                 } catch (Exception e) {
                     //This happens if part of the search area goes outside the image
                 	//This is okay, just ignore and continue
                 }
             }
         }
         /* No grey circle found
          * The angle found is probably wrong, skip this value and return 0 */
         
         if (greyXPoints.size() < 30) {
             throw new NoAngleException("No grey circle found");
         }
         
         /* Calculate center of grey circle points */
         int totalX = 0;
         int totalY = 0;
         for (int i = 0; i < greyXPoints.size(); i++) {
             totalX += greyXPoints.get(i);
             totalY += greyYPoints.get(i);
         }
         
         /* Center of grey circle */
         float backX = totalX / greyXPoints.size();
         float backY = totalY / greyXPoints.size();
         
         /* Check that the circle is surrounded by the green plate
          * Currently checks above and below the circle */
         
         int foundGreen = 0;
         int greenSides = 0;
         /* Check if green points are above the grey circle */
         for (int x=(int) (backX-2); x < (int) (backX+3); x++) {
             for (int y = (int) (backY-9); y < backY; y++) {
                 try {
                     Color c = new Color(image.getRGB(x, y));
                     float hsbvals[] = new float[3];
                     Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                     if (isGreen(c, hsbvals)) {
                         foundGreen++;
                         break;
                     }
                 } catch (Exception e) {
                     
                 }
             }
         }
         
         if (foundGreen >= 3) {
         	greenSides++;
         }
         
         
         /* Check if green points are below the grey circle */
         foundGreen = 0;
         for (int x=(int) (backX-2); x < (int) (backX+3); x++) {
             for (int y = (int) (backY); y < backY+10; y++) {
                 try {
                     Color c = new Color(image.getRGB(x, y));
                     float hsbvals[] = new float[3];
                     Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                     if (isGreen(c, hsbvals)) {
                         foundGreen++;
                         break;
                     }
                 } catch (Exception e) {
                     
                 }
             }
         }
         
         if (foundGreen >= 3) {
         	greenSides++;
         }
         
         
         /* Check if green points are left of the grey circle */
         foundGreen = 0;
         for (int x=(int) (backX-9); x < backX; x++) {
             for (int y = (int) (backY-2); y < backY+3; y++) {
                 try {
                     Color c = new Color(image.getRGB(x, y));
                     float hsbvals[] = new float[3];
                     Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                     if (isGreen(c, hsbvals)) {
                         foundGreen++;
                         break;
                     }
                 } catch (Exception e) {
                     
                 }
             }
         }
         
         if (foundGreen >= 3) {
         	greenSides++;
         }
         
         /* Check if green points are right of the grey circle */
         foundGreen = 0;
         for (int x=(int) (backX); x < (int) (backX+10); x++) {
             for (int y = (int) (backY-2); y < backY+3; y++) {
                 try {
                     Color c = new Color(image.getRGB(x, y));
                     float hsbvals[] = new float[3];
                     Color.RGBtoHSB(c.getRed(), c.getBlue(), c.getGreen(), hsbvals);
                     if (isGreen(c, hsbvals)) {
                         foundGreen++;
                         break;
                     }
                 } catch (Exception e) {
                     
                 }
             }
         }
         
         if (foundGreen >= 3) {
         	greenSides++;
         }
         
         
         if (greenSides < 3) {
         	throw new NoAngleException("Not enough green areas around the grey circle");
         }
         
         
         /*
          * At this point, the following is true:
          * Center of the T has been found
          * Front of the T has been found
          * Grey circle has been found
          * Grey circle is surrounded by green plate pixels on at least 3 sides
          * The grey circle, center of the T and front of the T line up roughly with the same angle
          */
         
         /* Calculate new angle using just the center of the T and the grey circle */
         length = (float) Math.sqrt(Math.pow(meanX - backX, 2)
                 + Math.pow(meanY - backY, 2));
         ax = (meanX - backX) / length;
         ay = (meanY - backY) / length;
         angle = (float) Math.acos(ax);
         
         if (frontY < meanY) {
             angle = -angle;
         }
         
         if (showImage) {
             image.getGraphics().drawLine((int)backX, (int)backY, (int)(backX+ax*70), (int)(backY+ay*70));
             image.getGraphics().drawOval((int) backX-4, (int) backY-4, 8, 8);
         }
         
         if (angle == 0) {
             return (float) 0.001;
         }
         
         return angle;
     }
     
     /* Doesn't work */
     /*
     private void calculateDistortion() {
         this.xDistortion = new int[640];
         this.yDistortion = new int[480];
         
         int centerX = 320;
         int centerY = 240;
         float k = (float) 0.01;
         
         for (int i = 0; i < 480; i++) {
             for (int j = 0; j < 640; j++) {
                 int x = (int) Math.floor(getRadialX(j, i, centerX, centerY, (float) Math.pow(k, 2)));
                 int y = (int) Math.floor(getRadialY(j, i, centerX, centerY, (float) Math.pow(k, 2)));
                 
                 if (y >= 480) { y = 240; }
                 if (x >= 640) { x = 320; }
                 
                 xDistortion[j] = x;
                 yDistortion[i] = y;
             }
         }
     }
     */
 
 }
