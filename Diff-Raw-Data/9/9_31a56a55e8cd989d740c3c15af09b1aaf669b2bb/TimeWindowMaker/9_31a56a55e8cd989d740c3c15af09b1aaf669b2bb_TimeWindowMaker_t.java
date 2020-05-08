 package com.guseggert.sensorloggerfeatureextractor.data;
 
 import java.util.Observable;
 
 import com.guseggert.sensorloggerfeatureextractor.DataInstance;
 import com.guseggert.sensorloggerfeatureextractor.DataReader;
 import com.guseggert.sensorloggerfeatureextractor.DataWriter;
 import com.guseggert.sensorloggerfeatureextractor.feature.FeatureSet;
 
 
 
 //Each time window observes TimeWindowMaker for new data points.
 //When a time window is full, it calls onTimeWindowFinished(), which
 //removes the time window as an observer and then processes the data.
 public class TimeWindowMaker extends Observable {
 	public final static int MSG_FEATURE_EXTRACTION_COMPLETE = 0; // message code for thread communication
 	public final static int MSG_FEATURE_EXTRACTION_FAILURE = 1;
 
 	private final long TIMEWINDOWLENGTH = 5000000000l; // nanoseconds
 	private final float TIMEWINDOWOVERLAP = 0.5f; // overlap of time windows
 	private TimeWindow mLastTimeWindow = null;
	private final String INPUTFILENAME = "2013-04-18--13-35-56";
	private final String EXTENSION = ".csv";
	private final String FULLINPUTFILENAME = INPUTFILENAME + EXTENSION;
	private final String OUTPUTFILENAME = INPUTFILENAME + "_features" + EXTENSION;
 	private DataWriter mDataWriter = new DataWriter(OUTPUTFILENAME);
 	
 	public TimeWindowMaker() {
		DataReader dr = new DataReader(FULLINPUTFILENAME);
 		while (dr.hasNext())
 			addPoint(dr.nextLine());
 	}
 	
 	private void addPoint(DataInstance instance) {
 		if (mLastTimeWindow == null || boundaryReached(instance.Time))
 			newTimeWindow(instance);
 		setChanged();
 		notifyObservers(instance);		
 	}
 	
 	private boolean boundaryReached(long time) {
 		long interval = time - mLastTimeWindow.getStartTime();
 		return interval >= TIMEWINDOWLENGTH * TIMEWINDOWOVERLAP;
 	}
 	
 	private void newTimeWindow(DataInstance instance) {
 		System.out.println("New time window: " + instance.Time);
 		TimeWindow tw = new TimeWindow(instance, TIMEWINDOWLENGTH, this);
 		addObserver(tw);
 		mLastTimeWindow = tw;
 	}
 	
 	public void onTimeWindowFull(TimeWindow timeWindow) {
 		System.out.println("Time window full: " + timeWindow.getStartTime());
 		deleteObserver(timeWindow);
 		FeatureSet featureSet = new FeatureSet(timeWindow);
 		mDataWriter.writeLine(featureSet);
 		// write feature set to file
 	}
 
 }
