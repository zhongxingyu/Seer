 package net.beadsproject.beads.data;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.lang.Thread.State;
 import java.util.Arrays;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioFormat;
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import net.beadsproject.beads.core.AudioUtils;
 
 /**
  * A Buffered Sample provides random access to an audio file. 
  * 
  * Only supports 16-bit samples at the moment.
  * TODO: Fix MP3 region boundary issues..
  * 
  * Notes:
  * - BufferedSample is not particularly thread-safe. 
  *   This shouldn't matter because SamplePlayers are not executed in parallel 
  *   when used in a UGen chain.    
  *    
  * @author ben
  */
 public class Sample implements Runnable {
 
 	private Regime bufferingRegime;
 	
 	static public class Regime
 	{
 		// defaults 
 		static final public TotalRegime TOTAL = new TotalRegime(); 
 		static final public TimedRegime TIMED = new TimedRegime(); 
 	};
 	
 	/**
 	 * Only some parts of the audio file are stored in memory. 
 	 * Useful for very large audio files. 
 	 * Various parameters affect the buffering behaviour.
 	 */  
 	static public class TimedRegime extends Regime
 	{
 		public int lookAhead; // time lookahead, ms
 		public int lookBack; // time lookback, ms 
 		public long memory;   // age (ms) at which regions get removed		
 		public float regionSize; // size of each region (by default 10s)
 		
 		static public enum Order {
 			NEAREST,
 			ORDERED
 		};
 		
 		public Order loadingOrder;
 	
 		public TimedRegime()
 		{
 			lookAhead = 100;
 			lookBack = 0;
 			memory = 1000;
 			regionSize = 100;
 			loadingOrder = Order.ORDERED;
 		}
 		
 		public TimedRegime(int regionSize, 
 						   int lookAhead, 
 						   int lookBack, 
 						   int memory, 
 						   Order loadingOrder)
 		{
 			this.regionSize = regionSize;
 			this.lookAhead = lookAhead;
 			this.lookBack = lookBack;
 			this.memory = memory;
 			this.loadingOrder = loadingOrder;
 		}
 		
 		/**
 		 * Set how many milliseconds from last loaded point to look ahead.
 		 * 
 		 * @param lookahead time to look ahead in ms.
 		 */
 		public void setLookAhead(float lookahead) 
 		{
 			this.lookAhead = (int)lookahead;
 		}
 
 		/**
 		 * Set how many milliseconds from last loaded point to look backwards.
 		 * 
 		 * @param lookback time to look backwards in ms.
 		 */
 		public void setLookBack(float lookback) 
 		{
 			this.lookBack = (int)lookback;
 		}
 
 		/**
 		 * If a part of an audio file has not been accessed for some amount of time it is discarded.
 		 * The time that the part remains in memory is specified by setMemory().
 		 * Passing a value of -1 to this function will set the memory to the maximum value possible.
 		 * 
 		 * @param ms Duration in milliseconds that unaccessed regions remain loaded.  
 		 */
 		public void setMemory(float ms)
 		{
 			this.memory = (int)ms;
 		}
 
 		/**
 		 * Specify the size of each buffered region.
 		 * 
 		 * @param ms Size of the region (ms)
 		 */
 		public void setRegionSize(float ms)
 		{
 			this.regionSize = ms;
 		}
 		
 		/**
 		 * When a region is loaded, nearby regions are put on a queue to be 
 		 * loaded also. The loading regime affects the order in which the nearby 
 		 * regions (defined by lookback and lookahead) are loaded.
 		 * 
 		 * NEAREST (the default) will load regions nearest to the region first.
 		 * ORDERED will load the regions from lowest to highest.
 		 * 
 		 * NEAREST makes sense if you are accessing the near regions first, e.g., playing a sample backwards or forwards.
 		 * ORDERED makes sense if you are accessing random nearby regions. Loading regions in order is generally quicker.
 		 * 
 		 * @param lr The order to load regions.
 		 */
 		public void setLoadingRegime(Order lr)
 		{
 			this.loadingOrder = lr;
 		}
 	};
 	
 	/**
 	 * The entire file is read into memory, providing fast access at the cost of more memory used.
 	 */
 	static public class TotalRegime extends Regime
 	{
 		
 	};
 	 	
 	/** 
 	 * Store the sample data with native bit-depth. 
 	 * Storing in native bit-depth reduces memory load, but increases cpu-load. 
 	 */ 
 	public boolean storeInNativeBitDepth = false;	
 	
 	private boolean verbose;
 	
 	private AudioFile audioFile;
 
 	/** The audio format. */
 	private AudioFormat audioFormat;
 
 	/** The number of channels. */
 	private int nChannels;
 
 	/** The number of sample frames. */
 	private long nFrames;
 
 	/** The length in milliseconds. */
 	private float length;	
 	
 	private boolean isBigEndian;
 
 	// regionSize is the number of frames per region
 	// these parameters are only used in the TIMED regime
 	private int r_regionSize;
 	private int r_lookahead; // num region lookahead
 	private int r_lookback; // num regions lookback
 	private long r_memory;
 	
 	private int regionSizeInBytes; // the number of bytes per region (regionSize * nChannels * bitconversionfactor)
 	private int numberOfRegions; // total number of regions
 	private int numberOfRegionsLoaded; // number of loaded regions
 	
 	private byte[][] regions; // the actual data
 	private float[][][] f_regions; // uninterleaved data
 	
 	private long[] regionAge; // the age of each region (in ms)
 	private long timeAtLastAgeUpdate; // the time at the last age updated operation
 	
 	private boolean[] regionQueued; // true if a region is currently queued
 	private ConcurrentLinkedQueue<Integer> regionQueue; // a queue of regions to be loaded
 	private Thread regionThread; // the thread that loads regions in the background
 	private Lock[] regionLocks; // to support safe deletion/writing of regions
 		
 	// this parameter is only used if regime==TOTAL
 	private byte[] sampleData;
 	private float[][] f_sampleData;
 
 	/**
      * Instantiates a new empty buffer with the specified audio format and
      * number of frames.
      * 
      * @param audioFormat the audio format.
      * @param totalFrames the number of sample frames.
      */
     public Sample(AudioFormat audioFormat, long totalFrames) {
         this();
     	this.audioFormat = audioFormat;
         nChannels = audioFormat.getChannels();
         nFrames = totalFrames;
         storeInNativeBitDepth = true;
         sampleData = new byte[2*nChannels*(int)totalFrames]; //16-bit
         Arrays.fill(sampleData,(byte)0);
         length = totalFrames / audioFormat.getSampleRate() * 1000f;
     }
 	
     /**
 	 * Create a sample.
 	 * Call setFile to initialise the sample.
  	 *
 	 */
 	public Sample()
 	{
         bufferingRegime = Regime.TOTAL;
         isBigEndian = true;
         verbose = false;
 	}
 	
 	/**
 	 * Create a sample from a file. This constructor immediately loads the entire audio file into memory.
 	 * 
 	 * @throws UnsupportedAudioFileException 
 	 * @throws IOException 
 	 */
 	public Sample(String filename) throws IOException, UnsupportedAudioFileException
 	{    	
 		this();
 		setFile(filename);
 	}
 	
 	/**
 	 * Create a sample from an Audio File, using the default buffering scheme. 
 	 *  
 	 * @throws UnsupportedAudioFileException 
 	 * @throws IOException 
 	 */
 	public Sample(AudioFile af) throws IOException, UnsupportedAudioFileException
 	{    	
 		this();		
 		setFile(af);
 	}
 	
 	/**
 	 * Create a sample from an Audio File, using the buffering scheme suggested. 
 	 *  
 	 * @throws UnsupportedAudioFileException 
 	 * @throws IOException 
 	 */
 	public Sample(AudioFile af, Regime r) throws IOException, UnsupportedAudioFileException
 	{    	
 		this();
 		setBufferingRegime(r);		
 		setFile(af);
 	}
 	
 	/**
 	 * Create a sample from a file, using the buffering scheme suggested.
 	 * 
 	 * @throws UnsupportedAudioFileException 
 	 * @throws IOException 
 	 */
 	public Sample(String filename, Regime r) throws IOException, UnsupportedAudioFileException
 	{    	
 		this();
 		setBufferingRegime(r);
 		setFile(filename);
 	}
 
 	/**
 	 * The buffering regime affects how the sample accesses the audio data.
 	 * 
 	 * 
 	 * @param r The buffering regime to use.
 	 */
 	public void setBufferingRegime(Regime r)
 	{
 		bufferingRegime = r;
 	}
 
 	/**
 	 * Specify an audio file that the Sample reads from.
 	 * 
 	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
 	 * 
 	 */
 	private void setFile(String file) throws IOException, UnsupportedAudioFileException
 	{
 		audioFile = new AudioFile(file);
 		setFile(audioFile);
 	}
 	
 	/**
 	 * Specify an explicit AudioFile that the Sample reads from.
 	 * NOTE: Only one sample should reference a particular AudioFile.
 	 * 
 	 * If BufferedRegime is TOTAL, this will block until the sample is loaded.
 	 * 
 	 */
 	private void setFile(AudioFile af) throws IOException, UnsupportedAudioFileException
 	{
 		audioFile = af;
 		audioFile.open();
 
 		audioFormat = audioFile.getDecodedFormat();
 		nFrames = audioFile.nFrames;
 		nChannels = audioFile.nChannels;
 		length = audioFile.length;
 		isBigEndian = audioFile.getDecodedFormat().isBigEndian();
 		
 		init();
 	}
 
 	/// set everything up, ready to use
 	private void init() throws IOException
 	{
 		if (isTotal())
 		{
 			// load all the sample data into a byte buffer
 			loadEntireSample();			
 			// lot of crap left over, so call the gc
 			// TODO: should probably call gc() only once after ALL samples are loaded
 			// Can do this in sample manager, for instance
 			// Ollie: or two alternatives: advise user to do this, or AudioContext calls gc() before starting
 			//which assumes that most samples are loaded before system is turned on.
 			//On the other hand, this is probably fine since it is only a hint to the system.
 			System.gc();
 		}
 		else // bufferingRegime instanceof TimedRegime
 		{
 			if (nFrames==AudioSystem.NOT_SPECIFIED)
 			{
 				// TODO: Do a quick run through and guess the length?
 				System.out.println("BufferedSample needs to know the length of the audio file it uses, but cannot determine the length.");
 				System.exit(1);
 			}
 			
 			TimedRegime tr = (TimedRegime) bufferingRegime;
 			// initialise params
 			r_regionSize = (int)Math.ceil(((tr.regionSize/1000.) * audioFile.getDecodedFormat().getSampleRate()));
 			r_lookahead = (int)Math.ceil(((tr.lookAhead/1000.) * audioFile.getDecodedFormat().getSampleRate())/r_regionSize);
 			r_lookback = (int)Math.ceil(((tr.lookBack/1000.) * audioFile.getDecodedFormat().getSampleRate())/r_regionSize);
 			if (tr.memory==-1) r_memory = Long.MAX_VALUE;			
 			r_memory = tr.memory;	
 			regionSizeInBytes = r_regionSize * 2 * nChannels;
 			numberOfRegions = 1 + (int)(nFrames / r_regionSize);
 			
 			// the last region may contain 0 to (regionSize-1) samples
 			
 			numberOfRegionsLoaded = 0;
 			
 			if (storeInNativeBitDepth)
 			{
 				regions = new byte[numberOfRegions][];
 				Arrays.fill(regions,null);
 			}
 			else
 			{
 				f_regions = new float[numberOfRegions][][];
 				Arrays.fill(f_regions,null);
 			}
 			
 			regionAge = new long[numberOfRegions];
 			Arrays.fill(regionAge,0);
 						
 			if(verbose) System.out.printf("Timed Sample has %d regions of %d bytes each.\n",numberOfRegions,regionSizeInBytes);
 						
 			// initialise region thread stuff
 			regionQueue = new ConcurrentLinkedQueue<Integer>();
 			regionQueued = new boolean[numberOfRegions];
 			regionLocks = new Lock[numberOfRegions];
 			for (int j=0;j<regionLocks.length;j++)
 			{
 				regionLocks[j] = new ReentrantLock();
 			}
 			//TODO might want to actually switch thread on and off
 			regionThread = new Thread(this);
 			regionThread.setDaemon(true);
 			
 			timeAtLastAgeUpdate = 0; //System.nanoTime()/1000;			
 			regionThread.start();
 						
 			// regionThread.setPriority()
 		}
 	}
 	
 	
 	/// are we using the total regime?
 	private boolean isTotal()
 	{
 		return (bufferingRegime instanceof TotalRegime);
 	}
 
 	/**
 	 * Return a single frame. 
 	 * If the data is not readily available this function blocks until it is.
 	 *  
 	 * @param frame Must be in range, else framedata is unchanged. 
 	 * @param frameData
 	 * 
 	 */
 	public void getFrame(int frame, float[] frameData)
 	{
 		if (frame >= nFrames) return;
 		
 		if (isTotal())
 		{
 			if (storeInNativeBitDepth)
 			{
 				int startIndex = frame * 2 * nChannels;			
 				AudioUtils.byteToFloat(frameData,sampleData,isBigEndian,startIndex,frameData.length);
 			}
 			else
 			{
 				for(int i=0;i<nChannels;i++)
 					frameData[i] = f_sampleData[i][frame];
 			}
 		}
 		else // bufferingRegime==BufferingRegime.TIMED
 		{
 			int whichRegion = frame / r_regionSize;
 			
 			// When someone requests a region, it may not be loaded yet.
 			// Alternatively it may currently be being deleted, in which case we have to wait.
 			
 			// lock access to region r, load it, and return it...
 			// wait until it is free...		
 			try {
 				while (!regionLocks[whichRegion].tryLock(10, TimeUnit.MILLISECONDS)){}
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			try {
 				
 				if (storeInNativeBitDepth)
 				{									
 					byte[] regionData = getRegion(whichRegion);
 					if (regionData!=null)
 					{
 						// convert it to the correct format,
 						int startIndex = (frame % r_regionSize) * 2 * nChannels;
 						AudioUtils.byteToFloat(frameData,regionData,isBigEndian,startIndex,frameData.length);
 					}
 					else
 					{
 						System.err.println("Sample.java:409 no region data!");
 						//System.exit(1);
 					}
 				}
 				else
 				{
 					float[][] regionData = getRegionF(whichRegion);
 					if (regionData!=null)
 					{
 						// convert it to the correct format,
 						int startIndex = frame % r_regionSize;
 						//System.arraycopy(regionData[startIndex], srcPos, dest, destPos, length)
 						for(int i=0;i<nChannels;i++)
 							frameData[i] = regionData[i][startIndex];
 						//AudioUtils.byteToFloat(frameData,regionData,isBigEndian,startIndex,frameData.length);
 					}
 				}
 					
 					
 			}
 			finally {
 				regionLocks[whichRegion].unlock();
 			}
 		}
 	}
 
 	/**
 	 * Get a series of frames. FrameData will only be filled with the available frames. 
 	 * It is the caller's responsibility to count how many frames are valid.
 	 * e.g., min(nFrames - frame, frameData[0].length) frames in frameData are valid.  
 	 * 
 	 * If the data is not readily available this function blocks until it is.
 	 * 
 	 * @param frame
 	 * @param frameData
 	 */
 	public void getFrames(int frame, float[][] frameData)
 	{
 		if (frame >= nFrames) return;
 		
 		if (isTotal())
 		{
			int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;			
 			
 			if (storeInNativeBitDepth)
 			{
 				int startIndex = frame * 2 * nChannels;			
				float[] floatdata = new float[numFloats];
				AudioUtils.byteToFloat(floatdata, sampleData, isBigEndian, startIndex, numFloats);
 				AudioUtils.deinterleave(floatdata,nChannels,frameData[0].length,frameData);
 			}
 			else
 			{
 				for(int i=0;i<nChannels;i++)
 					System.arraycopy(f_sampleData[i],frame,frameData[i],0,numFloats);
 			}
 		}
 		else // bufferingRegime==BufferingRegime.TIMED
 		{
 			int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;
 			
 			float[] floatdata = null;
 			if (storeInNativeBitDepth)
 				floatdata = new float[numFloats];
 			
 			// fill floatdata with successive regions of byte data
 			int floatdataindex = 0;			
 			int regionindex = frame % r_regionSize;
 			int whichregion = frame / r_regionSize;
 			int numfloatstocopy = Math.min(r_regionSize - regionindex,numFloats - floatdataindex);
 			
 			while (numfloatstocopy>0)
 			{
 				// see getFrame() for explanation
 				try {
 					while (!regionLocks[whichregion].tryLock(10, TimeUnit.MILLISECONDS)){}
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				try {
 					
 					if (storeInNativeBitDepth)
 					{					
 						byte[] regionData = getRegion(whichregion);
 						if (regionData!=null)
 							AudioUtils.byteToFloat(floatdata, regionData, isBigEndian, regionindex*2*nChannels, floatdataindex*nChannels, numfloatstocopy*nChannels);
 					}
 					else
 					{
 						float[][] regionData = getRegionF(whichregion);
 						if (regionData!=null)
 						{
 							// copy all channels...
 							for(int i=0;i<nChannels;i++)
 							{
 								System.arraycopy(regionData[i], 0, frameData[i], floatdataindex, numfloatstocopy);
 							}
 						}
 					}
 				}
 				finally {
 					regionLocks[whichregion].unlock();
 				}
 				floatdataindex += numfloatstocopy;
 				regionindex = 0;				
 				numfloatstocopy = Math.min(r_regionSize,numFloats - floatdataindex);				
 				whichregion++;
 			}
 			
 			// deinterleave the whole thing			
 			AudioUtils.deinterleave(floatdata,nChannels,frameData[0].length,frameData);
 		}
 	}	
 	
 	// Region loading, handling, queuing, removing, etc...	
 	
 	/// Region handling, loading, etc...
 	private byte[] getRegion(int r)
 	{	
 		// first determine whether the region is valid
 		if (!isRegionAvailable(r))
 		{
 			//System.out.println("REGION NOT THERE!");
 			loadRegion(r);
 		}
 		
 		touchRegion(r);
 		
 		// then return it
 		return regions[r];
 	}
 	
 	/// Region handling, loading, etc...
 	// assume region r exists
 	private float[][] getRegionF(int r)
 	{	
 		// first determine whether the region is valid
 		if (!isRegionAvailable(r))
 		{
 			//System.out.println("REGION NOT THERE!");
 			loadRegion(r);
 		}
 		
 		touchRegion(r);
 		
 		// then return it
 		return f_regions[r];
 	}
 	
 	private void touchRegion(int r)
 	{
 		if (((TimedRegime)bufferingRegime).loadingOrder==TimedRegime.Order.ORDERED)
 		{
 			// queue the regions from back to front
 			for(int i=Math.max(0,r-r_lookback);i<Math.min(r+r_lookahead,numberOfRegions);i++)
 			{
 				if (i!=r) queueRegionForLoading(i);
 			}
 		}
 		else // loadingOrder==LoadingRegime.NEAREST
 		{
 			// queue the regions from nearest to furthest, back to front...
 			int br = Math.min(r,r_lookback); // number of back regions
 			int fr = Math.min(r_lookahead,numberOfRegions-1-r); // number of ahead regions			
 			
 			// have two pointers, one going backwards the other going forwards			
 			int bp = 1;
 			int fp = 1;
 			boolean backwards = (bp<=br); // start backwards (if there are backward regions)			
 			while(bp<=br || fp<=fr)
 			{
 				if (backwards)
 				{
 					queueRegionForLoading(r-bp);					
 					bp++;
 					if (fp<=fr) backwards = false;
 				}
 				else // if forwards
 				{
 					queueRegionForLoading(r+fp);
 					fp++;
 					if (bp<=br) backwards = true;
 				}				
 			}
 		}
 		
 		// touch the region, make it new
 		synchronized (regionAge)
 		{	regionAge[r] = 0; }
 	}
 	
 	private boolean isRegionAvailable(int r)
 	{
 		if (storeInNativeBitDepth)
 			return regions[r]!=null;
 		else
 			return f_regions[r]!=null;
 	}
 	
 	private boolean isRegionQueued(int r)
 	{
 		return regionQueued[r];
 	}
 	
 	/// loads the region IMMEDIATELY, blocks until it is loaded
 	// this is called by the regionloader as it loads,
 	// but also by the main thread when it needs a region RIGHT AWAY
 	synchronized private void loadRegion(int r)
 	{
 		// for now, just seek to the correct position 
 		try {			
 			if (storeInNativeBitDepth)
 			{
 				regions[r] = new byte[regionSizeInBytes];
 				numberOfRegionsLoaded++;
 				audioFile.seek(r_regionSize*r);
 				int bytesRead = audioFile.read(regions[r]);
 				if (bytesRead<=0)
 					regions[r] = null;
 			}
 			else // store in float[][] format
 			{
 				// load the bytes and convert them on the spot
 				byte[] region = new byte[regionSizeInBytes];
 				numberOfRegionsLoaded++;
 				audioFile.seek(r_regionSize*r);
 				int bytesRead = audioFile.read(region);
 				if (bytesRead<=0)
 					regions[r] = null;
 				
 				// now convert
 				f_regions[r] = new float[nChannels][r_regionSize];
 				float[] interleaved = new float[nChannels*r_regionSize];
 				AudioUtils.byteToFloat(interleaved, region, isBigEndian);	
 				AudioUtils.deinterleave(interleaved, nChannels, r_regionSize, f_regions[r]);
 			}
 			synchronized(regionAge)
 			{
 				regionAge[r] = 0;
 			}
 		} catch (Exception  e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/// load the region r when you can, non-blocking
 	private void queueRegionForLoading(int r)
 	{
 		if (!isRegionAvailable(r) && !isRegionQueued(r))
 		{
 			regionQueued[r] = true;
 			regionQueue.add(r);
 			
 			// wake up the region thread master
 			if (regionThread.getState()==State.TIMED_WAITING)
 				regionThread.interrupt();
 			
 		}
 	}
 		
 	private void unloadRegion(int r)
 	{
 		if (storeInNativeBitDepth)
 			regions[r]=null;
 		else
 			f_regions[r]=null;		
 	}
 
 	public void run() {
 		while (true)
 		{		
 			// if there's a region on the queue then load it
 			boolean queuedregion = !regionQueue.isEmpty();
 			
 			if (queuedregion)
 			{
 				int r = regionQueue.poll();
 				if (regionLocks[r].tryLock())
 				{		
 					try {
 						if (!isRegionAvailable(r))
 							loadRegion(r);							
 						synchronized(regionQueued)
 						{
 							regionQueued[r] = false;
 						}
 					}
 					finally {
 						regionLocks[r].unlock();
 					}
 				}
 			}
 			
 			// age all the loaded regions
 			// remove the oldest ones if we exceed the memory limit		
 			// TODO: don't need to age things all the time, this should be based on some tunable param
 			if (timeAtLastAgeUpdate==0) timeAtLastAgeUpdate = System.currentTimeMillis();
 			long dt = System.currentTimeMillis() - timeAtLastAgeUpdate;	
 			//System.out.println(dt);
 			//System.out.println();
 			
 			//int numRegionsToRemove = numberOfRegionsLoaded - maxRegionsLoadedAtOnce;
 			//SortedSet sortedByAge = new TreeSet<Integer>(new Comparator(){});
 			//if (numRegionsToRemove>0)
 			for (int i=0;i<numberOfRegions;i++)
 			{	
 				//System.out.printf("%d ",regionAge[i]);
 				if (!isRegionAvailable(i))
 				{
 					synchronized(regionAge)
 					{
 						regionAge[i] += dt;
 					}
 					if (regionAge[i]>r_memory)
 					{
 						// if it is unlocked, then remove it...
 						if (regionLocks[i].tryLock())
 						{
 							try {
 								unloadRegion(i);
 								numberOfRegionsLoaded--;
 							}
 							finally {
 								regionLocks[i].unlock();
 							}
 						}
 						// else, ignore and try again next time...
 					}
 				}
 			}
 			timeAtLastAgeUpdate += dt;
 			
 			if (!queuedregion)
 			{
 				try {
 					Thread.sleep(10000);
 				} catch (InterruptedException ignore) 
 				{
 					//System.out.println("Wake up!");
 				}
 			}
 		}
 	}
 	
 	// a helper function, loads the entire sample into sampleData
 	private void loadEntireSample() throws IOException
 	{
 		final int BUFFERSIZE = 4096;
 		byte[] audioBytes = new byte[BUFFERSIZE];
 
 		int sampleBufferSize = 4096;
 		byte[] data = new byte[sampleBufferSize];
 				
 		int bytesRead;
 		int totalBytesRead = 0;
 		
 		int numberOfFrames = 0;
 		
 		while ((bytesRead = audioFile.read(audioBytes))!=-1) {
 			
 			int numFramesJustRead = bytesRead / (2 * nChannels);
 			
 			// resize buf if necessary
 			if (bytesRead > (sampleBufferSize-totalBytesRead))
 			{
 				sampleBufferSize = Math.max(sampleBufferSize*2, sampleBufferSize + bytesRead);
 				//System.out.printf("Adjusted samplebuffersize to %d\n",sampleBufferSize);
 				
 				// resize buffer
 				byte[] newBuf = new byte[sampleBufferSize];
 				System.arraycopy(data, 0, newBuf, 0, data.length);					
 				data = newBuf;
 			}
 			
 			System.arraycopy(audioBytes, 0, data, totalBytesRead, bytesRead);			
 			
 			numberOfFrames += numFramesJustRead;
 			totalBytesRead += bytesRead;
 		}
 		
 		// resize buf to proper length
 		// resize buf if necessary
 		if (sampleBufferSize > totalBytesRead)
 		{
 			sampleBufferSize = totalBytesRead;
 			
 			// resize buffer				
 			byte[] newBuf = new byte[sampleBufferSize];
 			System.arraycopy(data, 0, newBuf, 0, sampleBufferSize);					
 			data = newBuf;
 		}
 		
 		nFrames = sampleBufferSize / (2*nChannels);
 		
 		if (!storeInNativeBitDepth)
 		{
 			// copy and deinterleave entire data
 
 			f_sampleData = new float[nChannels][(int) nFrames];
 			float[] interleaved = new float[(int) (nChannels*nFrames)];
 			AudioUtils.byteToFloat(interleaved, data, isBigEndian);	
 			AudioUtils.deinterleave(interleaved, nChannels, (int) nFrames, f_sampleData);
 		}		
 		else
 		{
 			// store the data in this sample in native format
 			sampleData = data;
 		}	
 		
 		audioFile.close();		
 	}
 	
 	/**
      * Prints audio format info to System.out.
      */
     public void printAudioFormatInfo() {
         System.out.println("Sample Rate: " + audioFormat.getSampleRate());
         System.out.println("Channels: " + nChannels);
         System.out.println("Frame size in Bytes: " + audioFormat.getFrameSize());
         System.out.println("Encoding: " + audioFormat.getEncoding());
         System.out.println("Big Endian: " + audioFormat.isBigEndian());
     }
     
     /* (non-Javadoc)
      * @see java.lang.Object#toString()
      */
     public String toString() {
     	return getFileName();
     }
     
     
     /**
      * Gets the full file path.
      * 
      * @return the file path.
      */
     public String getFileName() {
     	return audioFile.file.getAbsolutePath();
     }
     
     
     /**
      * Gets the simple file name.
      * 
      * @return the file name.
      */
     public String getSimpleFileName() {
     	return audioFile.file.getName();
     }
     
     /**
      * Converts from milliseconds to samples based on the sample rate specified by {@link #audioFormat}.
      * 
      * @param msTime the time in milliseconds.
      * 
      * @return the time in samples.
      */
     public double msToSamples(double msTime) {
         return msTime * audioFormat.getSampleRate() / 1000.0f;
     }
 
     /**
      * Converts from samples to milliseconds based on the sample rate specified by {@link #audioFormat}.
      * 
      * @param sampleTime the time in samples.
      * 
      * @return the time in milliseconds.
      */
     public double samplesToMs(double sampleTime) {
         return sampleTime / audioFormat.getSampleRate() * 1000.0f;
     }
     
     /**
      * Retrieves a frame of audio using linear interpolation.
      * 
      * @param currentSample the current sample.
      * @param fractionOffset the offset from the current sample as a fraction of the time
      * to the next sample.
      * 
      * @return the interpolated frame.
      */
     public float[] getFrameLinear(int currentSample, float fractionOffset) {
         float[] result = new float[nChannels];
     	if(currentSample >= 0 && currentSample < nFrames) {
     		if (currentSample == nFrames-1)
     		{
     			getFrame(currentSample,result);    			
     		}
     		else
     		{
 	    		float[] current = new float[nChannels];    		
 	    		getFrame(currentSample,current);
 	    		float[] next = new float[nChannels];
 	    		getFrame(currentSample+1,next);
 	    		
 	    		for (int i = 0; i < nChannels; i++) {
 	    			result[i] = (1 - fractionOffset) * current[i] +
                         fractionOffset * next[i];
 	    		}   
             }
         } else {
              for(int i = 0; i < nChannels; i++) {
                  result[i] = 0.0f;
              }
         }
         return result;
     }
     
     /**
      * Retrieves a frame of audio using cubic interpolation.
      * 
      * @param currentSample the current sample.
      * @param fractionOffset the offset from the current sample as a fraction of the time
      * to the next sample.
      * 
      * @return the interpolated frame.
      */
     public float[] getFrameCubic(int currentSample, float fractionOffset) {    	
         float[] result = new float[nChannels];
         float[] buf = new float[nChannels];
         float a0,a1,a2,a3,mu2;
         float ym1,y0,y1,y2;
         for (int i = 0; i < nChannels; i++) {
             int realCurrentSample = currentSample;
             if(realCurrentSample >= 0 && realCurrentSample < (nFrames - 1)) {
                 realCurrentSample--;
                 if (realCurrentSample < 0) {
                 	getFrame(0, buf);
                     ym1 = buf[i];
                     realCurrentSample = 0;
                 } else {
                 	getFrame(realCurrentSample++, buf);
                     ym1 = buf[i];
                 }
             	getFrame(realCurrentSample++, buf);
                 y0 = buf[i];
                 if (realCurrentSample >= nFrames) {
                 	getFrame((int)nFrames - 1, buf);
                     y1 = buf[i]; //??
                 } else {
                 	getFrame(realCurrentSample++, buf);
                     y1 = buf[i];
                 }
                 if (realCurrentSample >= nFrames) {
                 	getFrame((int)nFrames - 1, buf);
                     y2 = buf[i]; //??
                 } else {
                 	getFrame(realCurrentSample++, buf);
                     y2 = buf[i];
                 }
                 mu2 = fractionOffset * fractionOffset;
                 a0 = y2 - y1 - ym1 + y0;
                 a1 = ym1 - y0 - a0;
                 a2 = y1 - ym1;
                 a3 = y0;
                 result[i] = a0 * fractionOffset * mu2 + a1 * mu2 + a2 * fractionOffset + a3;
             } else {
                 result[i] = 0.0f;
             }
         }
         return result;
     }
     
     /** 
 	 * A Sample needs to be writeable in order to be recorded into.
 	 * Currently buffered samples are not writeable, but TOTAL (file or empty) samples are.
 	 */
 	public boolean isWriteable()
 	{
 		return isTotal();
 	}
 	
 	/**
 	 * Write a single frame into this sample. Takes care of format conversion.
 	 * 
 	 * This only makes sense if this.isWriteable() returns true.
 	 * If isWriteable() is false, the behaviour is undefined/unstable.
 	 * 
 	 * @param frame The frame to write into.
 	 * @param frameData The frame data to write.
 	 */
 	public void putFrame(int frame, float[] frameData)
 	{
 		int startIndex = frame * 2 * nChannels;
 		AudioUtils.floatToByte(sampleData, startIndex, frameData, 0, frameData.length, isBigEndian);					
 	}
 	
 	/**
 	 * Write multiple frames data into the sample.
 	 * 
 	 * This only makes sense if this.isWriteable() returns true.
 	 * If isWriteable() is false, the behaviour is undefined/unstable.
 	 * 
 	 * @param frame The frame to write into.
 	 * @param frameData The frames to write.
 	 */
 	public void putFrames(int frame, float[][] frameData)
 	{
 		int startIndex = frame * 2 * nChannels;			
 		int numFloats = Math.min(frameData[0].length,(int)(nFrames-frame))*nChannels;			
 		
 		float[] floatdata = new float[numFloats];
 		AudioUtils.interleave(frameData,nChannels,frameData[0].length,floatdata);
 		AudioUtils.floatToByte(sampleData, startIndex, floatdata, 0, floatdata.length, isBigEndian);
 	}
 	
 	/**
 	 * Write multiple frames data into the sample.
 	 * 
 	 * This only makes sense if this.isWriteable() returns true.
 	 * If isWriteable() is false, the behaviour is undefined/unstable.
 	 * 
 	 * @param frame The frame to write into.
 	 * @param frameData The frames to write.
 	 * @param offset The offset into frameData
 	 * @param numFrames The number of frames from frameData to write
 	 */
 	public void putFrames(int frame, float[][] frameData, int offset, int numFrames)
 	{
 		if (numFrames<=0) return;
 		
 		int startIndex = frame * 2 * nChannels;			
 		int numFloats = Math.min(numFrames,(int)(nFrames-frame))*nChannels;			
 		
 		float[] floatdata = new float[numFloats];
 		AudioUtils.interleave(frameData,nChannels,offset,frameData[0].length,floatdata);
 		AudioUtils.floatToByte(sampleData, startIndex, floatdata, 0, floatdata.length, isBigEndian);
 	}
     
     /**
      * Write Sample to a file.
      * This will record the entire sample to a file.
      * BLOCKING.
      * 
      * @param fn the file name.
      * 
      * @throws IOException Signals that an I/O exception has occurred.
      */
     public void write(String fn) throws IOException {
     	if (isTotal())
     	{
     		ByteArrayInputStream bais = new ByteArrayInputStream(sampleData);
             AudioInputStream aos = new AudioInputStream(bais, audioFormat, nFrames);
             AudioSystem.write(aos, AudioFileFormat.Type.AIFF, new File(fn));
     	}
     	else // bufferingRegime==BufferingRegime.TIMED
     	{
     		System.out.println("Writing buffered samples to disk is not yet supported.");
     		System.exit(1);
     		
     		/*
     		// for each region, load it if necessary and write out the data
     		File file = new File(fn);
     		for(int i=0;i<numberOfRegions;i++)
     		{
     			try {
 					while (!regionLocks[i].tryLock(10, TimeUnit.MILLISECONDS)){}
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 				try {
 					byte[] regionData = getRegion(i);
 					if (regionData!=null)
 					{
 						ByteArrayInputStream bais = new ByteArrayInputStream(regionData);
 						AudioInputStream aos = new AudioInputStream(bais, audioFormat, regionSize);
 			            AudioSystem.write(aos, AudioFileFormat.Type.AIFF, file);
 					}
 				}
 				finally {
 					regionLocks[i].unlock();
 				}
     		}
     		*/
     	}
     	
         
     }
 
 	
 	public AudioFile getAudioFile() {
 		return audioFile;
 	}
 
 	
 	public AudioFormat getAudioFormat() {
 		return audioFormat;
 	}
 
 	
 	public int getNumChannels() {
 		return nChannels;
 	}
 
 	
 	public long getNumFrames() {
 		return nFrames;
 	}
 
 	
 	public float getLength() {
 		return length;
 	}
 
 	public float getSampleRate() {
 		return audioFormat.getSampleRate();
 	}
     
     public int getNumberOfRegionsLoaded() {
     	return numberOfRegionsLoaded;
     }
     
 }
