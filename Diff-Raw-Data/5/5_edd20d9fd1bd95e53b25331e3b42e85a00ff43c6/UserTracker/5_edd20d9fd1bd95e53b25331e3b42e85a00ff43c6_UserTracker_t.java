 import org.OpenNI.*;
 
 import java.util.HashMap;
 import java.awt.*;
 import java.awt.color.ColorSpace;
 import java.awt.image.*;
 
 public class UserTracker extends Component
 {
 	Sensor kinect;
 	SensorSnapshot snapshot;
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
     private byte[] imgbytes;
     private float histogram[];
     HashMap<Integer, HashMap<Integer, float[]>> joints;
 
     private boolean drawBackground = true;
     private boolean drawPixels = true;
     private boolean drawSkeleton = true;
     
     
     private BufferedImage bimg;
     int width, height;
     public UserTracker()
     {
         histogram = new float[10000];
         width = 320;
         height = 240;
         imgbytes = new byte[width*height*3];
         UniDevice device = new UniOpenNIDevice();
         kinect = new Sensor(device);
         joints = new HashMap<Integer, HashMap<Integer, float[]>>();
     }
     
     private void calcHist(Channel depth)
     {
         // reset
         for (int i = 0; i < histogram.length; ++i)
             histogram[i] = 0;
 
         int points = 0;
         int depthIterator = 0;
         while(depthIterator < depth.getNumberOfTuples())
         {
             short depthVal = depth.getTuple(depthIterator++).getElementShort(0);
             if (depthVal != 0)
             {
                 histogram[depthVal]++;
                 points++;
             }
         }
         
         for (int i = 1; i < histogram.length; i++)
         {
             histogram[i] += histogram[i-1];
         }
 
         if (points > 0)
         {
             for (int i = 1; i < histogram.length; i++)
             {
                 histogram[i] = 1.0f - (histogram[i] / (float)points);
             }
         }
     }
 
 
     void updateDepth()
     {
         snapshot = kinect.getSensorSnapshot();
 		
 		Channel depthChannel = snapshot.getChannel("Depth");
 
 		calcHist(depthChannel);
 		
 		for (int i = 0; i < depthChannel.getNumberOfTuples(); ++i)
 		{
 		    short pixel = depthChannel.getTuple(i).getElementShort(0);
 		    
 			imgbytes[3*i] = 0;
 			imgbytes[3*i+1] = 0;
 			imgbytes[3*i] = 0;                	
 
 		    if (drawBackground || pixel != 0)
 		    {
 		    	int colorID = colors.length-1;
 		    	if (pixel != 0)
 		    	{
 		    		float histValue = histogram[pixel];
 		    		imgbytes[3*i] = (byte)(histValue*colors[colorID].getRed());
 		    		imgbytes[3*i+1] = (byte)(histValue*colors[colorID].getGreen());
 		    		imgbytes[3*i+2] = (byte)(histValue*colors[colorID].getBlue());
 		    	}
 		    }
 		}
 		
 		// Update Joints
 		updateJoints();
     }
     
     void updateJoints()
     {
     	Channel user1Channel = snapshot.getChannel("User1");
     	if (user1Channel != null)
     	{
    		HashMap<Integer, float[]> user1Skeleton = new HashMap<Integer, float[]>();	
 	    	for (int jointIndex = 0; jointIndex < 15; ++jointIndex)
 	    	{
	    		float[] coordsAndConf = new float[4]; // coordinates (x, y, z) and confidence
 	    		coordsAndConf[0] = user1Channel.getTuple(jointIndex).getElementFloat(0);
 	    		coordsAndConf[1] = user1Channel.getTuple(jointIndex).getElementFloat(1);
 	    		coordsAndConf[2] = user1Channel.getTuple(jointIndex).getElementFloat(2);
 	    		coordsAndConf[3] = user1Channel.getTuple(jointIndex).getElementFloat(3);
 	    		
 	    		user1Skeleton.put(jointIndex, coordsAndConf);
 	    	}
 	    	joints.put(1, user1Skeleton);
     	}
     }
 
     Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW, Color.WHITE};
 
     void drawLine(Graphics g, HashMap<Integer, float[]> dict, int i, int j)
     {
 		float[] pos1 = dict.get(i);
 		float[] pos2 = dict.get(j);
 
 		if (pos1 != null && pos2 != null) 
 		{
 			if (pos1[3] == 0 || pos2[3] == 0)
 				return;
 	
 			g.drawLine((int)pos1[0], (int)pos1[1], (int)pos2[0], (int)pos2[1]);
 		}
     }
     
     public void drawSkeleton(Graphics g, int user) throws StatusException
     {
     	HashMap<Integer, float[]> dict = joints.get(new Integer(user));
 
     	if (dict != null)
     	{
 	    	drawLine(g, dict, 0, 1);
 	
 	    	drawLine(g, dict, 2, 8);
 	    	drawLine(g, dict, 5, 8);
 	
 	    	drawLine(g, dict, 1, 2);
 	    	drawLine(g, dict, 2, 3);
 	    	drawLine(g, dict, 3, 4);
 	
 	    	drawLine(g, dict, 2, 5);
 	    	drawLine(g, dict, 5, 6);
 	    	drawLine(g, dict, 6, 7);
 	
 	    	drawLine(g, dict, 9, 8);
 	    	drawLine(g, dict, 12, 8);
 	    	drawLine(g, dict, 9, 12);
 	
 	    	drawLine(g, dict, 9, 10);
 	    	drawLine(g, dict, 10, 11);
 	
 	    	drawLine(g, dict, 12, 13);
 	    	drawLine(g, dict, 13, 14);
     	}
 
     }
     
     public void paint(Graphics g)
     {
     	if (drawPixels)
     	{
             DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height*3);
 
             WritableRaster raster = Raster.createInterleavedRaster(dataBuffer, width, height, width * 3, 3, new int[]{0, 1, 2}, null); 
 
             ColorModel colorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{8, 8, 8}, false, false, ComponentColorModel.OPAQUE, DataBuffer.TYPE_BYTE);
 
             bimg = new BufferedImage(colorModel, raster, false, null);
 
     		g.drawImage(bimg, 0, 0, null);
     	}
         try
 		{
 			int[] users = {1};
 			for (int i = 0; i < users.length; ++i)
 			{
 		    	Color c = colors[users[i]%colors.length];
 		    	c = new Color(255-c.getRed(), 255-c.getGreen(), 255-c.getBlue());
 
 		    	g.setColor(c);
 		    	if (drawSkeleton)
 				{
 					drawSkeleton(g, users[i]);
 				}
 			}
 		} catch (StatusException e)
 		{
 			e.printStackTrace();
 		}
     }
     
     public Dimension getPreferredSize() {
         return new Dimension(width, height);
     }
     
 }
 
