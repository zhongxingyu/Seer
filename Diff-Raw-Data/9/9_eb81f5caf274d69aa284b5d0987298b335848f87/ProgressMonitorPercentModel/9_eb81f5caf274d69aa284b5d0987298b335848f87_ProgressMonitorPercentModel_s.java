 package net.dontdrinkandroot.wicket.model.progress;
 
import net.dontdrinkandroot.utils.oldprogressmonitor.ProgressMonitor;
 import net.dontdrinkandroot.wicket.model.AbstractChainedModel;
 
 import org.apache.wicket.model.IModel;
 
 
 public class ProgressMonitorPercentModel extends AbstractChainedModel<ProgressMonitor, Integer> {
 
 	public ProgressMonitorPercentModel(IModel<? extends ProgressMonitor> parent) {
 
 		super(parent);
 	}
 
 
 	@Override
 	public Integer getObject() {
 
 		return this.getParent().getObject().getProgress();
 	}
 
 }
