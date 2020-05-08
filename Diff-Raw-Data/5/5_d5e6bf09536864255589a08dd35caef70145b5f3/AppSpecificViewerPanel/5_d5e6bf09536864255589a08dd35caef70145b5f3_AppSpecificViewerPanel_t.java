 package org.vpac.grisu.frontend.view.swing.jobmonitoring.single.appSpecific;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.reflect.Constructor;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.control.JobConstants;
 import org.vpac.grisu.control.ServiceInterface;
 import org.vpac.grisu.frontend.model.job.JobObject;
 import org.vpac.grisu.frontend.view.swing.jobmonitoring.single.JobDetailPanel;
 import org.vpac.grisu.model.FileManager;
 import org.vpac.grisu.model.GrisuRegistryManager;
 
 import au.org.arcs.jcommons.constants.Constants;
 
 public abstract class AppSpecificViewerPanel extends JPanel implements
 		JobDetailPanel, PropertyChangeListener {
 
 	class UpdateProgressTask extends TimerTask {
 
 		@Override
 		public void run() {
 
 			if (checkJobStatus() == JobConstants.ACTIVE) {
 				progressUpdate();
 			}
 
 		}
 
 	}
 
 	static final Logger myLogger = Logger
 			.getLogger(AppSpecificViewerPanel.class.getName());
 
 	public static AppSpecificViewerPanel create(ServiceInterface si,
 			JobObject job) {
 
 		try {
 			final String appName = job
 					.getJobProperty(Constants.APPLICATIONNAME_KEY);
 
 			final String className = "org.vpac.grisu.frontend.view.swing.jobmonitoring.single.appSpecific."
 					+ appName;
 			final Class classO = Class.forName(className);
 
 			final Constructor<AppSpecificViewerPanel> constO = classO
 					.getConstructor(ServiceInterface.class);
 
 			final AppSpecificViewerPanel asvp = constO.newInstance(si);
 
 			return asvp;
 
 		} catch (final Exception e) {
 			myLogger.info(e);
 			return null;
 		}
 
 	}
 
 	private final int DEFAULT_PROGRESS_CHECK_INTERVALL = 120;
 
 	private final Thread updateThread = null;
 
 	private final boolean jobIsFinished = false;
 
 	private Timer timer;
 
 	private JobObject job = null;
 	protected final ServiceInterface si;
 	protected final FileManager fm;
 
 	public AppSpecificViewerPanel(ServiceInterface si) {
 		super();
 		this.si = si;
 		if (si != null) {
 			this.fm = GrisuRegistryManager.getDefault(si).getFileManager();
 		} else {
 			this.fm = null;
 		}
 	}
 
 	protected synchronized int checkJobStatus() {
 
 		int status = getJob().getStatus(false);
 		System.out.println("PANEL: " + JobConstants.translateStatus(status));
 		if (status >= JobConstants.FINISHED_EITHER_WAY) {
 
 			if (timer != null) {
 				timer.cancel();
 			}
 		}
 		return status;
 
 	}
 
 	public JobObject getJob() {
 		return this.job;
 	}
 
 	public JPanel getPanel() {
 		return this;
 	}
 
 	abstract public void initialize();
 
 	/**
 	 * This gets called once the first time Grisu figures out the job is
 	 * finished.
 	 * 
 	 * Maybe just after initialization, maybe sometime later.
 	 */
 	abstract void jobFinished();
 
 	/**
 	 * Called once when the job is started on the cluster. Or when job is
	 * already running when monitoring begins (but is not finished yet).
 	 */
 	abstract public void jobStarted();
 
 	/**
 	 * This method gets called if some property of the JobObject changes.
 	 * 
 	 * This method does get all the job events, except for the events that
 	 * concern the jobs status.
 	 * 
 	 * 
 	 * @param evt
 	 *            the property event that is fired from the JobObject
 	 */
 	abstract public void jobUpdated(PropertyChangeEvent evt);
 
 	/**
 	 * This gets called while the job is running in a configurable intervall. It
 	 * also is called one last time after the status changed from running to
 	 * finished (successfull or not).
 	 * 
 	 * Use this to update progress. For example, download an output file and
 	 * display it's content grahically (a progress bar, for example).
 	 */
 	abstract void progressUpdate();
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		System.out.println("PropertyChanged: " + evt.getPropertyName() + ": "
 				+ evt.getNewValue());
 
 		if ("status".equals(evt.getPropertyName())) {
 
 			int status = checkJobStatus();
 			System.out.println("After check");
 
 			if (status == JobConstants.NO_SUCH_JOB) {
 				return;
 			} else if (status == JobConstants.ACTIVE) {
 				jobStarted();
 			} else if ((status >= JobConstants.FINISHED_EITHER_WAY)) {
 				System.out.println("JOB FINISHED");
 				jobFinished();
 			}
 		} else if ("statusString".equals(evt.getPropertyName())) {
 			return;
 		} else if ("finished".equals(evt.getPropertyName())) {
 			return;
 		} else {
 			jobUpdated(evt);
 		}
 
 	}
 
 	public void setJob(JobObject job) {
 		if (this.job != null) {
 			this.job.removePropertyChangeListener(this);
 		}
 		this.job = job;
 		this.job.addPropertyChangeListener(this);
 
 		try {
 			initialize();
 		} catch (final Exception e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 
 		if (checkJobStatus() < JobConstants.FINISHED_EITHER_WAY) {
 			// first time around we do it manually
 			if (checkJobStatus() == JobConstants.ACTIVE) {
 				jobStarted();
 			}
 			timer = new Timer();
 			timer.scheduleAtFixedRate(new UpdateProgressTask(),
 					DEFAULT_PROGRESS_CHECK_INTERVALL * 1000,
 					DEFAULT_PROGRESS_CHECK_INTERVALL * 1000);
 
		} else {
			jobFinished();
 		}
 
 	}
 
 }
