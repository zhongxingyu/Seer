 package org.eclipse.dltk.ruby.internal.ui;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.dltk.ruby.core.RubyPlugin;
 import org.eclipse.ui.progress.UIJob;
 
 public class InitializeAfterLoadJob extends UIJob {
 	
 	private final class RealJob extends Job {
 		public RealJob(String name) {
 			super(name);
 		}
 		protected IStatus run(IProgressMonitor monitor) {
 			monitor.beginTask("", 10); //$NON-NLS-1$
 			try {
 				RubyPlugin.initializeAfterLoad(new SubProgressMonitor(monitor, 6));
 				RubyUI.initializeAfterLoad(new SubProgressMonitor(monitor, 4));
 			} catch (CoreException e) {
 				RubyPlugin.log(e);
 				return e.getStatus();
 			}
 			return new Status(IStatus.OK, RubyPlugin.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
 		}
 		public boolean belongsTo(Object family) {
 			return RubyUI.PLUGIN_ID.equals(family);
 		}
 	}
 	
 	public InitializeAfterLoadJob() {
 		super("Starting DLTK Ruby initialization");
 		setSystem(true);
 	}
 	public IStatus runInUIThread(IProgressMonitor monitor) {
 		Job job = new RealJob("Initializing DLTK Ruby");
 		job.setPriority(Job.SHORT);
 		job.schedule();
 		return new Status(IStatus.OK, RubyUI.PLUGIN_ID, IStatus.OK, "", null); //$NON-NLS-1$
 	}
 
 }
