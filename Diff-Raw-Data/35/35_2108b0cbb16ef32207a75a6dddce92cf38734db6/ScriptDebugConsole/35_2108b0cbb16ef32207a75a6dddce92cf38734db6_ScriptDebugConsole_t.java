 package org.eclipse.dltk.debug.ui;
 
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.console.IOConsole;
 
 public class ScriptDebugConsole extends IOConsole {
 	private ILaunch launch;
 	public ILaunch getLaunch() {
 		return launch;
 	}
 	public void setLaunch(ILaunch launch) {
 		this.launch = launch;
 	}
 	public ScriptDebugConsole(String name, ImageDescriptor imageDescriptor) {
 		super(name, imageDescriptor);
 	}
	public void matcherFinished() {
		super.matcherFinished();
	}
	public void partitionerFinished() {
		super.partitionerFinished();
	}
	
 }
