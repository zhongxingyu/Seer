 package com.jcope.vnc.server.screen;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 
 import com.jcope.debug.LLog;
 import com.jcope.util.SegmentationInfo;
 import com.jcope.vnc.server.ClientHandler;
 import com.jcope.vnc.server.DirectRobot;
 import com.jcope.vnc.server.StateMachine;
 import com.jcope.vnc.shared.StateMachine.SERVER_EVENT;
 
 /**
  * 
  * @author Joseph Copenhaver
  * 
  * This class will contain registered listener interfaces that
  * get notified when a particular screen changes graphically.
  * 
  * For each ScreenMonitor there is exactly one screen device
  * being sampled for which there can be one or more listening
  * components.
  * 
  * Components will be notified when a segment of a screen changes.
  * It is then the responsibility of the listening component to fetch
  * from this class the data segment of interest.
  * 
  * Should all listeners be terminated, then the Screen Monitor shall
  * terminate and be ready for garbage collection.
  * 
  * Let each screen be broken up into segmentWidth by segmentHeight pixel segments.
  * 
  * Let each segment be assigned an ID from left to right, top down where the first tile is ID 0
  * Let segment ID -1 indicate the collection of segments as a whole (The entire screen)
  *
  */
 
 public class Monitor extends Thread
 {
 	public static final long refreshMS = 1000;
 	int screenX, screenY;
 	SegmentationInfo segInfo = new SegmentationInfo();
 	private Integer screenWidth = null, screenHeight;
 	private ArrayList<ClientHandler> clients;
 	private DirectRobot dirbot;
 	private int[][] segments;
 	private boolean[] changedSegments;
 	private volatile boolean stopped = Boolean.FALSE;
 	private volatile boolean joined = Boolean.FALSE;
 	
 	public Monitor(int segmentWidth, int segmentHeight, DirectRobot dirbot, ArrayList<ClientHandler> clients)
 	{
 		super(String.format("Monitor: %s", dirbot.toString()));
 		segInfo.segmentWidth = segmentWidth;
 		segInfo.segmentHeight = segmentHeight;
 		this.dirbot = dirbot;
 		this.clients = clients;
 		syncBounds();
 	}
 	
 	private void syncBounds()
 	{
 		Integer lastWidth = screenWidth;
 		Integer lastHeight = screenHeight;
 		Rectangle bounds = getScreenBounds();
 		screenX = bounds.x;
 		screenY = bounds.y;
 		screenWidth = bounds.width;
 		screenHeight = bounds.height;
 		if (lastWidth == null || lastWidth != screenWidth || lastHeight != screenHeight)
 		{
 		    segInfo.loadConfig(screenWidth, screenHeight, segInfo.segmentWidth, segInfo.segmentHeight);
 		    segments = new int[segInfo.numSegments][];
 			changedSegments = new boolean[segInfo.numSegments];
 			for (int i=0; i<segInfo.numSegments; i++)
 			{
 				segments[i] = new int[getSegmentPixelCount(i)];
 				changedSegments[i] = Boolean.FALSE;
 			}
 			if (lastWidth != null)
 			{
 				// TODO: provide ability to lock a set of clients
 				for (ClientHandler client : clients)
 				{
 					StateMachine.handleServerEvent(client, SERVER_EVENT.SCREEN_RESIZED, screenWidth, screenHeight);
 				}
 			}
 		}
 	}
 	
 	public void run()
 	{
 		// detect change in a segment of the configured screen
 		// notify all listeners of the changed segment
 		
 		boolean changed;
 		
 		int[] buffer = new int[segInfo.maxSegmentNumPixels];
 		int[] segmentDim = new int[2];
 		int x, y;
 		long startAt, timeConsumed;
 		ArrayList<ClientHandler> newClients = new ArrayList<ClientHandler>();
 		
 		try
 		{
 			while (!stopped)
 			{
 				startAt = System.currentTimeMillis();
 				
 				changed = Boolean.FALSE;
 				
 				dirbot.markRGBCacheDirty();
 				
 				for (int i=0; i<=segInfo.maxSegmentID; i++)
 				{
 					getSegmentPos(i, segmentDim);
 					x = segmentDim[0];
 					y = segmentDim[1];
 					getSegmentDim(i, segmentDim);
 					dirbot.getRGBPixels(x, y, segmentDim[0], segmentDim[1], buffer);
 					if (copyIntArray(segments[i], buffer, segments[i].length))
 					{
 						changed = Boolean.TRUE;
 						changedSegments[i] = Boolean.TRUE;
 					}
 				}
 				
 				for (ClientHandler client : clients)
 				{
				    if (client.getIsNewFlag())
                     {
                         newClients.add(client);
                     }
 				}
 				
 				if (changed)
 				{
 					for (ClientHandler client : clients)
 					{
					    if (client.getIsNewFlag())
 					    {
 					        continue;
 					    }
 						ScreenListener l = client.getScreenListener(dirbot);
 						for (int i=0; i<changedSegments.length; i++)
 						{
 							if (changedSegments[i])
 							{
 								l.onScreenChange(i);
 							}
 						}
 					}
 					for (int i=0; i<changedSegments.length; i++)
 					{
 						changedSegments[i] = Boolean.FALSE;
 					}
 				}
 				
 				if (newClients.size() > 0)
 				{
 				    for (ClientHandler client : newClients)
 				    {
 				        client.setIsNewFlag(Boolean.FALSE);
 				        ScreenListener l = client.getScreenListener(dirbot);
 				        for (int i=0; i<segInfo.numSegments; i++)
 				        {
 				            l.onScreenChange(i);
 				        }
 				    }
 				    newClients.clear();
 				}
 				
 				timeConsumed = System.currentTimeMillis() - startAt;
 				
 				if (timeConsumed < refreshMS)
 				{
 					try
 					{
 						sleep(refreshMS - timeConsumed);
 					}
 					catch (InterruptedException e)
 					{
 						LLog.e(e);
 					}
 				}
 			}
 		}
 		finally {
 			
 			stopped = Boolean.TRUE;
 			joined = Boolean.TRUE;
 		}
 	}
 	
 	public void sendDisplayInitEvents(ClientHandler client)
 	{
 	    Rectangle bounds = getScreenBounds();
 	    client.sendEvent(SERVER_EVENT.SCREEN_RESIZED, bounds.width, bounds.height);
 	    client.sendEvent(SERVER_EVENT.SCREEN_SEGMENT_SIZE_UPDATE, segInfo.segmentWidth, segInfo.segmentHeight);
 	}
 	
 	private boolean copyIntArray(int[] dst, int[] src, int length)
 	{
 		boolean rval = Boolean.FALSE;
 		
 		for (int i=0; i<length; i++)
 		{
 			if (dst[i] != src[i])
 			{
 				dst[i] = src[i];
 				rval = Boolean.TRUE;
 			}
 		}
 		
 		return rval;
 	}
 	
 	public int getSegmentID(int x, int y)
 	{
 		int rval = segInfo.getSegmentID(x, y);
 		
 		return rval;
 	}
 	
 	public void getSegmentDim(int segmentID, int[] dim)
 	{
 	    segInfo.getDim(segmentID, dim);
 	}
 	
 	public void getSegmentPos(int segmentID, int[] absPos)
 	{
 	    segInfo.getPos(segmentID, absPos);
 	    absPos[0] += screenX;
 	    absPos[1] += screenY;
 	}
 	
 	public void getSegmentIdxPos(int segmentID, int[] pos)
 	{
 	    segInfo.getIdxPos(segmentID, pos);
 	}
 	
 	public int getSegmentPixelCount(int segmentID)
 	{
 		int rval = segInfo.getSegmentPixelCount(segmentID);
 		
 		return rval;
 	}
 	
 	public int getMaxSegmentPixelCount()
 	{
 		return segInfo.maxSegmentNumPixels;
 	}
 	
 	public int getSegmentWidth()
 	{
 		return segInfo.segmentWidth;
 	}
 	
 	public int getSegmentHeight()
 	{
 		return segInfo.segmentHeight;
 	}
 	
 	public Rectangle getScreenBounds()
 	{
 		return dirbot.getScreenBounds();
 	}
 	
 	public int getSegmentCount()
 	{
 		return segInfo.numSegments;
 	}
 	
 	private void signalStop()
 	{
 		stopped = true;
 	}
 	
 	public boolean isRunning()
 	{
 		return !stopped;
 	}
 	
 	public boolean isJoined()
 	{
 		return joined;
 	}
 	
 	public void kill()
 	{
 		signalStop();
 	}
 
     public int[] getSegment(int segmentID)
     {
         int[] rval = (segmentID == -1) ? dirbot.getRGBPixels() : segments[segmentID];
         
         return rval;
     }
 
 }
