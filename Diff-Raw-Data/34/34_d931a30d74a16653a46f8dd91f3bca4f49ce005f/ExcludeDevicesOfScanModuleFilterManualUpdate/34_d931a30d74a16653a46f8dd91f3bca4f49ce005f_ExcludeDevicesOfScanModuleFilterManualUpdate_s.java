 package de.ptb.epics.eve.data.measuringstation.filter;
 
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
 * @author mmichals
 *
  */
 public class ExcludeDevicesOfScanModuleFilterManualUpdate extends
 		ExcludeDevicesOfScanModuleFilter {
 
 	/* indicates whether the filter should update itself, if it is triggered */
 	private boolean update;
 	
 	/**
 	 * 
 	 * @param excludeAxes
 	 * @param excludeChannels
 	 * @param excludePrescans
 	 * @param excludePostscans
 	 * @param excludePositionsings
 	 */
 	public ExcludeDevicesOfScanModuleFilterManualUpdate(boolean excludeAxes,
 			boolean excludeChannels, boolean excludePrescans,
 			boolean excludePostscans, boolean excludePositionsings) {
 		super(excludeAxes, excludeChannels, excludePrescans, excludePostscans,
 				excludePositionsings);
 		update = false;
 	}
 
 	/**
	 * 
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		if(update) {
 			super.updateEvent(modelUpdateEvent);
 		}
 	}
 	
 	/**
	 * 
 	 */
 	public void update() {
 		super.updateEvent(null);
 	}
 }
