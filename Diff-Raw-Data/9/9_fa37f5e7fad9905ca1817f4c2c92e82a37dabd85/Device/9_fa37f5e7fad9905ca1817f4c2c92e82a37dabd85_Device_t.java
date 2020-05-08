 package device;
 
 import java.util.Iterator;
 import java.util.concurrent.*;
 import java.util.logging.Logger;
 
 import device.external.IDevice;
 
 /**
  * A Device represents an abstract entity, i.e. device node, that can have
  * other devices in its internal list. It will start and shutdown
  * these devices (if any) and itself in a dedicated thread per device.
  * 
  * @author sebastian
  */
 public class Device implements IDevice, Runnable
 {
 	/** Logging support */
     Logger logger = Logger.getLogger (Device.class.getName ());
 
     /**
      * The internal device list can contain other devices.
      * For example if there is a device hierarchy.
      */
 	ConcurrentLinkedQueue<Device> deviceList;
 	
 	/**
 	 * If it is an abstract device the following fields are not set.
	 * These are dependent on the used MRS level underneath.
 	 */
 	/** The numerical identifiers of this device. */
 	int id = -1;
 	/** The host string, i.e. network name. */
 	String host = null;
 	/** The port number, i.e. host port. */
 	int port = -1;
 	/** The numerical device number. */
 	int index = -1;
 	
 	/** Every class of this type has it's own thread */
 	Thread thread = new Thread ( this );
 
 	/** The periodical idle time in ms this device sleeps between action cycles. */
 	long sleeptime = 100;
 	
 	/** Indicating if this device is currently in an action cycle. */
 	boolean isRunning = false;
 	/** Indicating if this device is started in its own thread. */
 	boolean isThreaded = false;
 	
 	/**
 	 * Creates a Device and initializes the internal device list.
 	 */
 	public Device()
 	{
 		deviceList = new ConcurrentLinkedQueue<Device>();
 	}
 	/**
      * This constructor is used to create a data device object
      * and has no internal devicelist or thread
      *
 	 * @param id The Device id.
 	 * @param host The host to which this Device is connected.
 	 * @param port The port to which this Device is connected.
 	 * @param index The Device number (aka index) of the Device.
 	 */
 	public Device (int id, String host, int port, int index)
 	{
 		this();
 		this.id = id;
 		if (host != null)
 			this.host = host;
 		this.port = port;
 		this.index = index;
 	}
 	/**
 	 * Creates a device with the given properties.
 	 * @param device A device template to create a new device
 	 */
 	public Device (Device device)
 	{
 	    this (
             device.getId(),
             device.getHost(),
             device.getPort(),
             device.getIndex()
 	    );
 	}
 	/**
 	 * Might be to be implemented by subclass to do something
 	 */
 	protected void update() {}
 
 	/**
 	 * Starts this device (and its sub-devices) in a thread each.
 	 */
 	public synchronized void runThreaded()
 	{
 	    if (isThreaded() != true)
 		{
 			/** Start all devices */
 			if (deviceList != null && deviceList.size() > 0)
 			{
 				Iterator<Device> deviceIterator = deviceList.iterator();
 
 				while (deviceIterator.hasNext())
 				{
 					Device device = deviceIterator.next();
 
 					/** Start device */
 					device.runThreaded();
 
 					/** Wait for device to be started to avoid device start order race conditions */
 					while (device.isThreaded() == false)
 					{
 		                try { Thread.sleep (10); } catch (InterruptedException e) { e.printStackTrace(); }
 					}
 				}
 			}
 
 			isThreaded  = true;
 
 			thread.start();
 
 			logger.fine("Running "+this+" in "+thread.getName());
 		}
 	}
 
 	/**
 	 * Manages the periodical action cycles and idle times.
 	 * @throws IllegalStateException When updating the device fails.
 	 */
 	@Override public void run() throws IllegalStateException
 	{
 	    try
 	    { 
 	        isRunning = true;
 	      
 	        /**
 	         * The device' run loop.
 	         * This has to stay as optimized as possible.
 	         */
 	        while (isThreaded == true)
 	        {
 	            /** Do sub-class specific stuff */
 	            update();
 
 	            if (sleeptime > 0)
 	            {
 	                Thread.sleep ( sleeptime );
 	            }
 	            else
 	                if (sleeptime == 0)
 	                {
 	                    Thread.yield();
 	                }
 	            /** else if (SLEEPTIME < 0) do nothing */				
 	        }
 	    }
         catch(InterruptedException ie)
         {
             /** Sleep interrupted */
         }
 	    catch (Exception e)
 	    { 
 	        String log = "Error updating device "+this;
 	        logger.severe(log);
 	        e.printStackTrace();
 //          throw new IllegalStateException(log); /** Exception will not be caught as dedicated thread */
 	    }
 	    finally
 	    {
             isRunning = false; /** sync with setNotThreaded */
             isThreaded = false; /** Thread is interrupted */
 	     
             logger.fine("Shutdown "+this+" in "+thread.getName());
 	    }
 	}
 	
 	/**
 	 * Shuts down this (and any sub-) device.
 	 */
 	public synchronized void shutdown()
 	{
 	    if (isThreaded() == true)
 	    {
 	        long delayCount = 0;
 
 	        /** Sync with run() method */
 	        isThreaded = false;
 
 	        /** Interrupt in case it is sleeping too long */
 	        if (getSleepTime() > 10000)
 	            thread.interrupt();
 
 	        /** wait to exit run loop */
 	        while (isRunning() == true)
 	        {
 	            delayCount += 1;
 	            
 	            if (delayCount >= 100)
 	            {
 	                logger.finer("Shutdown delayed "+delayCount+" "+ this);
 	            }
                 
 	            try { Thread.sleep (10); } catch (Exception e) { e.printStackTrace(); }
 	        }
 	    }
 		
 	    /** Stop all devices */
 		if (deviceList.size() > 0)
 		{
 		    /**
 		     * Loop through Device List in reverse order.
 		     * That's important to to handle device dependencies correctly.
 		     * E.g. a @see RobotDevice depends on a @see DeviceNode.
 		     */
 			Object[] devList = deviceList.toArray();
 			
 			for (int i=devList.length-1; i>=0; i--)
 			{
 			    Device device = (Device)devList[i];
 				
 				/** Stop device */
 				device.shutdown();
 			}
 			
 			/** empty device list */
 			deviceList.clear();
 		}
 	}
 	/**
 	 * Returns a list of devices that this robot client provides.
 	 * @return The device list.
 	 */
 	public final ConcurrentLinkedQueue<Device> getDeviceList()
 	{
 		return deviceList;
 	}
 	
 	/**
      * @param deviceList the deviceList to set
      */
     protected final void setDeviceList(ConcurrentLinkedQueue<Device> deviceList)
     {
         this.deviceList = deviceList;
     }
     /**
      * @return A Device array list of the internal devices (if any)
      */
     public final Device[] getDeviceListArray()
     { 
         return deviceList.toArray(new Device[deviceList.size()]);
     }
     
     /**
      * @return An iterator of the internal device list.
      */
 	public final Iterator<Device> getDeviceIterator()
 	{
 		if (deviceList != null)
 		{
 			return (Iterator<Device>) deviceList.iterator();
 		}
 		else
 		{
 			return null;
 		}
 	}
 	/**
 	 * Searches the internal device list for the device with the properties given.
 	 * @param dev Device template (0 or null for args not to take care of).
 	 * @return A device matching the given template when found in list, null otherwise.
 	 */
 	public final Device getDevice (Device dev)
 	{
 		int name = dev.getId();
 		int number = dev.getIndex();
 		Device found = null;
 		
 		Iterator<Device> it = getDeviceIterator();
 		
 		while (it.hasNext())
 		{
 			Device curDev = it.next();
 			if (curDev.getId() == name)
 			{
 				if (number != -1)
 				{
 					if (curDev.getIndex() == number)
 					{
 						found = curDev;
 					}
 				}
 				else
 				{
 					found = curDev;
 				}
 			}
 			// TODO use and debug following
 //			if (curDev.matches(dev))
 //			{
 //			    found = curDev;
 //			}
 		}
 		return found;
 	}
 	/**
 	 * @return This device' host string, can be null.
 	 */
 	public String getHost()
 	{
 	    return host;
 	}
 	/**
 	 * Sets this device' host string.
 	 * @param host The host string.
 	 */
 	public void setHost(String host)
 	{
 		this.host = host;
 	}
 	/**
 	 * @return This device' host port.
 	 */
 	public int getPort()
 	{
 		return port;
 	}
 	/**
 	 * Sets this device' host port.
 	 * @param port
 	 */
 	public void setPort(int port)
 	{
 		this.port = port;
 	}
 	/**
 	 * @return This device' numerical identifier.
 	 */
 	public int getId()
 	{
 		return id;
 	}
 	/**
 	 * Sets this device' numerical identifier.
 	 * @param name The identifier.
 	 */
 	public void setName(int name) {
 		this.id = name;
 	}
 	/**
 	 * @return The device number.
 	 */
 	public int getIndex()
 	{
 		return index;
 	}
 	/**
 	 * @return This device' thread name.
 	 */
 	public String getThreadName()
 	{
 		if (thread != null)
 		{
 			return thread.getName();
 		}
 		else
 		{
 			return null;
 		}
 	}
 	/**
 	 * @param time The idle time of this device.
 	 */
 	public void setSleepTime(long time)
 	{
 		this.sleeptime = time;
 	}
 	/**
 	 * @return This device' idle time.
 	 */
 	public long getSleepTime()
 	{
 		return sleeptime;
 	}
 	/**
 	 * @return true if this device is currently doing something (action cycle).
 	 */
 	public boolean isRunning()
 	{
 		return isRunning;
 	}
 	/**
 	 * @return true if this device is threaded, false else.
 	 */
 	public boolean isThreaded()
 	{
 		return isThreaded;
 	}
     /**
      * @return The logger.
      */
     public Logger getLogger()
     {
         return logger;
     }
     @Override public String toString()
     {
         return ""+getId()+","+getHost()+":"+getPort()+":"+getIndex()+"("+getSleepTime()+"ms)";
     }
     /**
      * Compares a Device according to its properties to another one
      * @param aDevice The device to compare to.
      * @return true if they equal, false else.
      */
     public boolean equals(Device aDevice)
     {
         if (
             getId() == aDevice.getId() &&
             getHost().equals(aDevice.getHost()) == true &&
             getPort() == aDevice.getPort() &&
             getIndex() == aDevice.getIndex()
         )
         {
             return true;
         }
         else
         {
             return false;   
         }
     }
     /**
      * Matches this device with the given one.
      * It compares the explicitly set device properties only.
      * Disabled properties will be ignored.
      * @param aDevice The device to match against.
      * @return true if the device matches, false else.
      */
     public boolean matches(Device aDevice)
     {
         if (aDevice == null)
             return true;
         
         if ( getId() == aDevice.getId() || getId() == -1 || aDevice.getId() == -1 )
         {
             if ( getHost() == null || aDevice.getHost() == null || getHost().equals(aDevice.getHost()) == true )
             {
                 if ( getPort() == aDevice.getPort() || getPort() == -1 || aDevice.getPort() == -1 )
                 {
                     if ( getIndex() == aDevice.getIndex() || getIndex() == -1 || aDevice.getIndex() == -1 )
                     {
                         return true;
                     }
                 }
             }
         }
       
         return false;
     }
     /**
      * Checks if this device properties matches against any of the given device list.
      * @param aDevList The list to search in.
      * @return true if any device of the list matches, false else.
      */
     public boolean matchesList(Device[] aDevList)
     {
         if (aDevList == null)
             return true;
         
         for (int i=0; i<aDevList.length; i++)
         {
             if (matches(aDevList[i]) == true)
             {
                 return true;
             }
         }
         
         return false;
     }
     /**
      * Checks if this device properties is in the given device list.
      * @param aDevList The list to search in.
      * @return true if any device of the list equals, false else.
      */
     public boolean isInList(Device[] aDevList)
     {
         for (int i=0; i<aDevList.length; i++)
         {
             if (equals(aDevList[i]) == true)
             {
                 return true;
             }
         }
         
         return false;
     }
     public boolean isSupported()
     {
         return false;
     }
 }
