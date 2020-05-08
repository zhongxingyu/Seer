 package de.ptb.epics.eve.data.measuringstation.filter;
 
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 
 /**
 * <code>ExcludeDevicesOfScanModuleFilterManualUpdate</code> is a modified 
 * version of 
 * {@link de.ptb.epics.eve.data.measuringstation.filter.ExcludeDevicesOfScanModuleFilter}.
 * It differs in the way it updates itself (when receiving update events). The 
 * Update mechanism is suspended by default and can be activated (and 
 * deactivated) via {@link #setUpdate(boolean)}. * 
 * 
 * @author Marcus Michalsky
 * @since 0.4.2
  */
 public class ExcludeDevicesOfScanModuleFilterManualUpdate extends
 		ExcludeDevicesOfScanModuleFilter {
 
 	/* indicates whether the filter should update itself, if it is triggered */
 	private boolean update;
 	
 	/**
	 * Constructor.
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
	 * {@inheritDoc}
	 * <br>
	 * Only has the described effect if {@link #setUpdate(boolean)} is called 
	 * with <code>true</code> as argument.
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		if(update) {
 			super.updateEvent(modelUpdateEvent);
 		}
 	}
 	
 	/**
	 * Forces the filter to update itself.
 	 */
 	public void update() {
 		super.updateEvent(null);
 	}
	
	/**
	 * Sets whether the filter should update itself when an update event is 
	 * received.
	 * 
	 * @param update <code>true</code> if the filter should update itself when 
	 * 		an update event is received, <code>false</code> otherwise
	 */
	public void setUpdate(boolean update) {
		this.update = update;
	}
 }
