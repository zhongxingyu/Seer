 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.core.uiprocess;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.eclipse.core.internal.jobs.JobManager;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import org.eclipse.riena.core.exception.IExceptionHandlerManager;
 import org.eclipse.riena.core.service.Service;
 import org.eclipse.riena.internal.ui.core.Activator;
 
 public class UIProcess extends PlatformObject implements IUIMonitor {
 
 	public final static QualifiedName PROPERTY_CONTEXT = new QualifiedName("uiProcess", "context"); //$NON-NLS-1$//$NON-NLS-2$
 
 	private final UICallbackDispatcher callbackDispatcher;
 	private final Job job;
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param name
 	 *            the name of the job
 	 * 
 	 * @throws some_kind_of_runtime_exception
 	 *             if no configuration point is found or if an object of the
 	 *             class given in the extension point can not be instantiated.
 	 * 
 	 * @see UISynchronizer
 	 */
 	public UIProcess(final String name) {
 		this(name, false);
 	}
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param name
 	 *            the name of the job
 	 * 
 	 * @param user
 	 *            sets whether or not the process should be displayed as a
 	 *            window
 	 * 
 	 * @throws some_kind_of_runtime_exception
 	 *             if no configuration point is found or if an object of the
 	 *             class given in the extension point can not be instantiated.
 	 * 
 	 * @see UISynchronizer
 	 */
 	public UIProcess(final String name, final boolean user) {
 		this(name, user, new Object());
 	}
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param name
 	 *            the name of the job
 	 * 
 	 * @param user
 	 *            sets whether or not the process should be displayed as a
 	 *            window
 	 * 
 	 * @param context
 	 *            an object representing the context of this {@link UIProcess}
 	 * 
 	 * @throws some_kind_of_runtime_exception
 	 *             if no configuration point is found or if an object of the
 	 *             class given in the extension point can not be instantiated.
 	 * 
 	 */
 	public UIProcess(final String name, final boolean user, final Object context) {
 		this(name, UISynchronizer.createSynchronizer(), user, context);
 	}
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param name
 	 *            the name of the job
 	 * 
 	 * @param syncher
 	 *            the synchronizer which serializes callbacks to the UI-
 	 *            {@link Thread} of the Widget Toolkit
 	 * 
 	 * @param user
 	 *            sets whether or not the process should be displayed as a
 	 *            window
 	 * 
 	 * @param context
 	 *            an object representing the context of this {@link UIProcess}
 	 * 
 	 * @throws some_kind_of_runtime_exception
 	 *             if no configuration point is found or if an object of the
 	 *             class given in the extension point can not be instantiated.
 	 * 
 	 */
 	public UIProcess(final String name, final IUISynchronizer syncher, final boolean user, final Object context) {
 		this(name, new UICallbackDispatcher(syncher), user, context);
 	}
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param name
 	 *            the name of the job
 	 * 
 	 * @param dispatcher
 	 *            the {@link UICallbackDispatcher} which is responsible for
 	 *            Thread-switches and {@link IUIMonitor}-delegation (Note: this
 	 *            is very low level)
 	 * 
 	 * @param user
 	 *            sets whether or not the process should be displayed as a
 	 *            window
 	 * 
 	 * @param context
 	 *            an object representing the context of this {@link UIProcess}
 	 * 
 	 * @throws some_kind_of_runtime_exception
 	 *             if no configuration point is found or if an object of the
 	 *             class given in the extension point can not be instantiated.
 	 * 
 	 */
 	private UIProcess(final String name, final UICallbackDispatcher dispatcher, final boolean user, final Object context) {
 		this.callbackDispatcher = dispatcher;
 		this.job = createJob(name, user, context);
 		updateProcessConfiguration();
 	}
 
 /**
 	 * Creates a new UIProcess.
 	 * <p>
 	 * For executing processes the {@code UIProcess) uses the
 	 * 
 	 * @code UISynchronizer} to create {@code IUISynchronizer} instances which
 	 *       perform the actual execution of the process.
 	 * 
 	 * @param job
 	 *            the job which is wrapped by the {@link UIProcess}
 	 */
 	public UIProcess(final Job job) {
 		this.callbackDispatcher = new UICallbackDispatcher(UISynchronizer.createSynchronizer());
 		this.job = job;
 		updateProcessConfiguration();
 	}
 
	/**
	 * @return
	 * @deprecated This should use {@code UISynchronizer.createSynchronizer()}
	 */
	@Deprecated
	public static IUISynchronizer getSynchronizerFromExtensionPoint() {
		return UISynchronizer.createSynchronizer();
	}

 	private void updateProcessConfiguration() {
 		configureProcessInfo();
 	}
 
 	private void configureProcessInfo() {
 		if (callbackDispatcher != null) {
 			final ProcessInfo processInfo = callbackDispatcher.getProcessInfo();
 			processInfo.setContext(job.getProperty(PROPERTY_CONTEXT));
 			processInfo.addPropertyChangeListener(new CancelListener());
 			processInfo.setDialogVisible(job.isUser());
 			processInfo.setNote(job.getName());
 			processInfo.setTitle(job.getName());
 		}
 	}
 
 	private Job createJob(final String name, final boolean user, final Object context) {
 		final Job newJob = new InternalJob(name);
 		newJob.setUser(user);
 		newJob.setProperty(PROPERTY_CONTEXT, context);
 		return newJob;
 	}
 
 	private final class CancelListener implements PropertyChangeListener {
 		public void propertyChange(final PropertyChangeEvent event) {
 			if (ProcessInfo.PROPERTY_CANCELED.equals(event.getPropertyName())) {
 				job.cancel();
 			}
 		}
 	}
 
 	protected boolean forceMonitorBegin() {
 		return true;
 	}
 
 	private final class InternalJob extends Job {
 
 		public InternalJob(final String name) {
 			super(name);
 		}
 
 		@Override
 		protected IStatus run(final IProgressMonitor monitor) {
 			beforeRun(monitor);
 			if (forceMonitorBegin()) {
 				monitor.beginTask(getName(), getTotalWork());
 			}
 			boolean state = false;
 			try {
 				state = runJob(monitor);
 			} catch (final OperationCanceledException e) {
 				if (!monitor.isCanceled()) {
 					monitor.setCanceled(true);
 				}
 			} catch (final Throwable t) {
 				// Forward to exception handler
 				Service.get(Activator.getDefault().getContext(), IExceptionHandlerManager.class).handleException(t);
 			}
 			monitor.done();
 			afterRun(monitor);
 			ProgressProviderBridge.instance().unregisterMapping(this);
 			return state ? Status.OK_STATUS : Status.CANCEL_STATUS;
 		}
 
 	}
 
 	/**
 	 * 
 	 * @return the job wrapped by the {@link UIProcess}. This job is run by the
 	 *         {@link JobManager} on a worker {@link Thread}
 	 * @since 3.0
 	 */
 	public Job getJob() {
 		return job;
 	}
 
 	/**
 	 * called before {@link #runJob(IProgressMonitor)} is invoked (async)
 	 */
 	protected void beforeRun(final IProgressMonitor monitor) {
 	}
 
 	/**
 	 * called after {@link #runJob(IProgressMonitor)} is invoked (async)
 	 */
 	protected void afterRun(final IProgressMonitor monitor) {
 	}
 
 	protected int getTotalWork() {
 		return IProgressMonitor.UNKNOWN;
 	}
 
 	/**
 	 * registers the job at the {@link ProgressProviderBridge}
 	 */
 	private void register() {
 		// registers itself as a monitor
 		callbackDispatcher.addUIMonitor(this);
 		ProgressProviderBridge.instance().registerMapping(job, this);
 	}
 
 	public UICallbackDispatcher getCallbackDispatcher() {
 		return callbackDispatcher;
 	}
 
 	/**
 	 * called whenever a unit of work is completed
 	 */
 	public void updateProgress(final int progress) {
 	}
 
 	/**
 	 * called on the user interface thread before aynch work is done
 	 */
 	public void initialUpdateUI(final int totalWork) {
 	}
 
 	/**
 	 * called on the user interface thread after aynch work is done
 	 */
 	public void finalUpdateUI() {
 	}
 
 	/**
 	 * override this method for implementation of logic on a worker thread
 	 * 
 	 * @param monitor
 	 *            the jobs API monitor used to control the {@link UIProcess}
 	 * @return true if the method has been run without errors
 	 */
 	public boolean runJob(final IProgressMonitor monitor) {
 		return true;
 	}
 
 	/**
 	 * starts the {@link UIProcess} using jobs API
 	 * 
 	 * @return true if the UIProces could be scheduled false if the UIProcess is
 	 *         already scheduled.
 	 * @since 3.0
 	 */
 	public boolean start() {
 		final int state = job.getState();
 		if (state == Job.RUNNING || state == Job.WAITING || state == Job.SLEEPING) {
 			return false;
 		}
 		register();
 		job.schedule();
 		return true;
 	}
 
 	@Override
 	@SuppressWarnings("rawtypes")
 	public Object getAdapter(final Class adapter) {
 		Object adapted = super.getAdapter(adapter);
 		if (adapted == null) {
 			if (adapter.isInstance(this)) {
 				adapted = this;
 			}
 			if (adapter.equals(UICallbackDispatcher.class)) {
 				adapted = getCallbackDispatcher();
 			}
 		}
 		return adapted;
 	}
 
 	/**
 	 * @param note
 	 *            the note to set
 	 */
 	public void setNote(final String note) {
 		getProcessInfo().setNote(note);
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(final String title) {
 		getProcessInfo().setTitle(title);
 	}
 
 	/**
 	 * Sets the enabled state of the cancel button of the uiprocess window
 	 * 
 	 * @param enabled
 	 * @since 3.0
 	 */
 	public void setCancelEnabled(final boolean enabled) {
 		getProcessInfo().setCancelEnabled(enabled);
 	}
 
 	/**
 	 * Sets the visible state of the cancel button of the uiprocess window
 	 * 
 	 * @param visible
 	 * @since 3.0
 	 */
 	public void setCancelVisible(final boolean visible) {
 		getProcessInfo().setCancelVisible(visible);
 	}
 
 	/**
 	 * @param icon
 	 *            the icon to set
 	 */
 	public void setIcon(final String icon) {
 		getProcessInfo().setIcon(icon);
 	}
 
 	/**
 	 * Sets the strategy of how progress is interpreted
 	 * 
 	 * @param strategy
 	 *            - the progress strategy
 	 */
 	public void setProgresStrategy(final ProcessInfo.ProgresStrategy strategy) {
 		getProcessInfo().setProgresStartegy(strategy);
 	}
 
 	/**
 	 * @return the {@link ProcessInfo} object holding meta information of this
 	 *         {@link UIProcess}
 	 */
 	private ProcessInfo getProcessInfo() {
 		return getCallbackDispatcher().getProcessInfo();
 	}
 
 	/**
 	 * call this method to get a "ui thread serialized run" of
 	 * {@link #updateUi()}
 	 */
 	protected void notifyUpdateUI() {
 		// serialize on ui thread
 		getCallbackDispatcher().getSyncher().syncExec(new Runnable() {
 
 			public void run() {
 				try {
 					updateUI();
 				} catch (final Exception e) {
 					Service.get(Activator.getDefault().getContext(), IExceptionHandlerManager.class).handleException(e);
 				}
 			}
 
 		});
 	}
 
 	/**
 	 * called on the user interface thread as the result of a call to
 	 * {@link #notifyUpdateUI()}
 	 * 
 	 * @deprecated use {@link updateUI} instead.
 	 */
 	@Deprecated
 	public void updateUi() {
 	}
 
 	/**
 	 * called on the user interface thread as the result of a call to
 	 * {@link #notifyUpdateUI()}
 	 * 
 	 * @since 4.0
 	 */
 	protected void updateUI() {
 		updateUi();
 	}
 
 }
