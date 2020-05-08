 package gda.device.detector.nxdetector.plugin.areadetector;
 
 import gda.device.DeviceException;
 import gda.device.detector.areadetector.v18.NDStatsPVs.TSControlCommands;
 import gda.device.scannable.PositionInputStream;
 import gda.epics.PV;
 import gda.epics.ReadOnlyPV;
 import gda.epics.predicate.GreaterThanOrEqualTo;
 
 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /**
  * Represents a single one-off collection from an Epics time series array, or arrays that hang off the same
  * control PVs. 
  */
 class TimeSeriesInputStreamCollection implements PositionInputStream<List<Double>> {
 
 	private final PV<TSControlCommands> tsControlPV;
 
 	private final PV<Integer> tsNumPointsPV;
 
 	private final ReadOnlyPV<Integer> tsCurrentPointPV;
 
 	private final List<ReadOnlyPV<Double[]>> tsArrayPVList;
 
 	private final int numPointsToCollect;
 
 	private int numPointsReturned = 0;
 
 	private Object completetionMonitor = new Object();
 
 	private boolean complete = false;
 
 	/**
 	 * Create and start a time series collection.
 	 * 
 	 * @param tsControlPV
 	 * @param tsNumPointsPV
 	 * @param tsCurrentPointPV
 	 * @param tsArrayPVList
 	 * @param numPointsToCollect
 	 * @throws IOException
 	 */
 	public TimeSeriesInputStreamCollection(PV<TSControlCommands> tsControlPV, PV<Integer> tsNumPointsPV,
 			ReadOnlyPV<Integer> tsCurrentPointPV, List<ReadOnlyPV<Double[]>> tsArrayPVList, int numPointsToCollect)
 			throws IOException {
 		if (tsArrayPVList.isEmpty()) {
 			throw new IllegalArgumentException("No stats to collect");
 		}
 		this.tsControlPV = tsControlPV;
 		this.tsNumPointsPV = tsNumPointsPV;
 		this.tsCurrentPointPV = tsCurrentPointPV;
 		this.tsArrayPVList = tsArrayPVList;
 		this.numPointsToCollect = numPointsToCollect;
 		start();
 	}
 
 	private void start() throws IOException {
 		tsNumPointsPV.putWait(numPointsToCollect);
 		tsNumPointsPV.setValueMonitoring(true);
 		Integer configuredNumPoints = tsNumPointsPV.get();
 		if (numPointsToCollect != configuredNumPoints) {
 			throw new IllegalArgumentException(
 					MessageFormat
 							.format("The number of points requested ({0}) exceeds the maximum configured for this EPICS installation ({1}).",
 									numPointsToCollect, configuredNumPoints));
 		}
 		tsControlPV.putWait(TSControlCommands.ERASE_AND_START);
 	}
 
 	public void waitForCompletion() throws InterruptedException {
 		synchronized (completetionMonitor) {
 			if (!complete) {
 				completetionMonitor.wait();
 			}
 		}
 	}
 
 	private void collectionComplete() throws IOException {
 		synchronized (completetionMonitor) {
 			complete = true;
 			completetionMonitor.notifyAll();
 		}
 		tidyup();
 	}
 
 	public boolean isComplete() {
 		return complete;
 	}
 
 	public void stop() throws IOException {
 		tsControlPV.putWait(TSControlCommands.STOP);
 		tidyup();
 	}
 
 	private void tidyup() throws IOException {
 		tsNumPointsPV.setValueMonitoring(false);
 	}
 
 	@Override
 	public List<List<Double>> read(int maxToRead) throws NoSuchElementException, InterruptedException, DeviceException {
 		if (numPointsReturned >= numPointsToCollect) {
 			throw new IllegalStateException("All " + numPointsToCollect + " points to collect have been read.");
 		}
 		int desiredPoint = numPointsReturned + 1;
 
 		int numPointsAvailable;
 		try {
 			numPointsAvailable = tsCurrentPointPV.waitForValue(new GreaterThanOrEqualTo(desiredPoint), -1);
 		} catch (InterruptedIOException e) {
 			throw new InterruptedException("Interupted while waiting for point: " + desiredPoint);
 		} catch (Exception e) {
 			throw new DeviceException("Problem while waiting for point: " + desiredPoint, e);
 		}
		
		try {
			// Jon Thompson and Giles Knap have discovered that this works around pushing 1 to the TSRead not updating
			// the arrays.
			tsControlPV.putWait(TSControlCommands.READ);
		} catch (IOException e1) {
			throw new DeviceException("Problem asking EPICS to read data up into PVs: " + desiredPoint, e1);
		}
 
 		int numNewPoints = numPointsAvailable - numPointsReturned;
 		// Below a 'point' is a list of doubles, one for each pv to read.
 		List<List<Double>> pointList = new ArrayList<List<Double>>(numNewPoints);
 		for (int i = 0; i < numNewPoints; i++) {
 			pointList.add(new ArrayList<Double>());
 		}
 
 		// Readout series of new points from each array
 		for (ReadOnlyPV<Double[]> arrayPV : tsArrayPVList) {
 			Double[] completeArray;
 			try {
 				completeArray = arrayPV.get();
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 			for (int i = 0; i < numNewPoints; i++) {
 				Double element = completeArray[numPointsReturned + i];
 				pointList.get(i).add(element);
 			}
 		}
 
 		numPointsReturned = numPointsAvailable;
 		if (numPointsReturned == numPointsToCollect) {
 			try {
 				collectionComplete();
 			} catch (IOException e) {
 				throw new DeviceException(e);
 			}
 		}
 		return pointList;
 	}
 }
