 package net.dontdrinkandroot.wicket.model.progress;
 
import net.dontdrinkandroot.utils.oldprogressmonitor.ProgressMonitor;
 import net.dontdrinkandroot.wicket.model.AbstractChainedModel;
 
 import org.apache.wicket.model.IModel;
 
 
 public class ProgressMonitorMessageModel extends AbstractChainedModel<ProgressMonitor, String> {
 
 	public ProgressMonitorMessageModel(IModel<? extends ProgressMonitor> parent) {
 
 		super(parent);
 	}
 
 
 	@Override
 	public String getObject() {
 
 		return this.getParent().getObject().getMessage();
 	}
 
 }
