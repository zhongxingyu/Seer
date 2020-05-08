 package ch.cern.atlas.apvs.client.widget;
 
 import ch.cern.atlas.apvs.client.ui.Module;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 
 public class UpdateScheduler {
 
 	private boolean updateScheduled = false;
 	private Module module;
 
 	public UpdateScheduler(Module module) {
 		this.module = module;
 	}
 
 	public synchronized boolean update() {
 		if (updateScheduled) {
 			return false;
 		}
 
 		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
 
 			@Override
 			public void execute() {
 				updateScheduled = false;
 				module.update();
 			}
 		});
 		updateScheduled = true;
 		return true;
 	}
 }
