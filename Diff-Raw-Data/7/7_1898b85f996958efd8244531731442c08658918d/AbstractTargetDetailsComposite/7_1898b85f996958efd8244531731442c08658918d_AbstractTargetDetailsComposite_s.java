 package org.zend.php.zendserver.deployment.ui.targets;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.operation.IRunnableContext;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.zend.php.zendserver.deployment.core.targets.PhpcloudContainerListener;
 import org.zend.php.zendserver.deployment.ui.Activator;
 import org.zend.php.zendserver.deployment.ui.wizards.OpenShiftInitializationWizard;

 import org.zend.webapi.core.WebApiClient;
 import org.zend.webapi.core.WebApiException;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.response.ResponseCode;
 import org.zend.webapi.core.service.IRequestListener;
 import org.zend.webapi.internal.core.connection.exception.UnexpectedResponseCode;
 
 /**
  * Abstract subclass for editing target details.
  * 
  */
 public abstract class AbstractTargetDetailsComposite {
 	
 	protected class CancelCreationException extends Exception {
 
 		private static final long serialVersionUID = 1L;
 		
 		public CancelCreationException(String message) {
 			super(message);
 		}
 		
 	}
 
 	private class OpenShiftInitializer {
 
 		private IZendTarget target;
 		private IProgressMonitor monitor;
 		private IStatus status;
 
 		public OpenShiftInitializer(IZendTarget target, IProgressMonitor monitor) {
 			super();
 			this.target = target;
 		}
 
 		public IZendTarget getTarget() {
 			return target;
 		}
 
 		public IStatus getStatus() {
 			return status;
 		}
 
 		public void init() {
 			Display.getDefault().syncExec(new Runnable() {
 
 				public void run() {
 					Shell shell = PlatformUI.getWorkbench()
 							.getActiveWorkbenchWindow().getShell();
 					WizardDialog dialog = new WizardDialog(shell,
 							new OpenShiftInitializationWizard(target));
 					dialog.open();
 				}
 			});
 			try {
 				target = testConnectAndDetectPort(target, monitor);
 			} catch (WebApiException ex) {
 				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 						ex.getMessage(), ex);
 			} catch (RuntimeException e) {
 				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 						e.getMessage(), e);
 			}
 		}
 
 	}
 
 	private static final int[] possiblePorts = new int[] { 10081, 10082, 10088 };
 	private static final int[] possiblePhpcloudPorts = new int[] { 10082 };
 	
 	/**
 	 * Fired when validation event occurs.
 	 */
 	public static final String PROP_ERROR_MESSAGE = "errorMessage"; //$NON-NLS-1$
 	public static final String PROP_WARNING_MESSAGE = "warningMessage"; //$NON-NLS-1$
 	public static final String PROP_MESSAGE = "message"; //$NON-NLS-1$
 	public static final String PROP_MODIFY = "modify"; //$NON-NLS-1$
 
 	protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(
 			this);
 
 	private String[] data;
 
 	private String errorMessage;
 	
 	private String warningMessage;
 
 	protected IZendTarget[] result;
 	
 	protected IRunnableContext runnableContext;
 
 	/**
 	 * Creates composite contents and returns control.
 	 * 
 	 * @param parent
 	 * @return created control.
 	 */
 	abstract public Composite create(Composite parent);
 
 	/**
 	 * Sets default values on the composite based on provided default target.
 	 * 
 	 * @param defaultTarget
 	 *            target with default values.
 	 */
 	abstract public void setDefaultTargetSettings(IZendTarget defaultTarget);
 
 	/**
 	 * Returns essential data provided by user, that is necessary to create
 	 * target. Called from UI thread, so should not block.
 	 * 
 	 * @return data to be passed to #createTarget(String[]).
 	 */
 	abstract protected String[] getData();
 	
 	/**
 	 * @return help resource
 	 */
 	abstract protected String getHelpResource();
 
 	/**
 	 * Creates target based on data gathered in UI. Can be time consuming
 	 * operation. May be called from non-UI thread, so should avoid accessing
 	 * GUI elements.
 	 * 
 	 * @param data
 	 *            data from getData()
 	 * @param monitor 
 	 * @return created target
 	 * 
 	 * @throws SdkException
 	 * @throws IOException
 	 * @throws CancelCreationException 
 	 */
 	abstract protected IZendTarget[] createTarget(String[] data, IProgressMonitor monitor)
 			throws SdkException, IOException, CoreException, CancelCreationException;
 
 	public void addPropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		changeSupport.addPropertyChangeListener(propertyName, listener);
 	}
 
 	public void removePropertyChangeListener(String propertyName,
 			PropertyChangeListener listener) {
 		changeSupport.removePropertyChangeListener(propertyName, listener);
 	}
 
 	public void setErrorMessage(String errorMessage) {
 		String oldMessage = this.errorMessage;
 		this.errorMessage = errorMessage;
 		changeSupport.firePropertyChange(PROP_ERROR_MESSAGE, oldMessage,
 				errorMessage);
 	}
 
 	public String getErrorMessage() {
 		return errorMessage;
 	}
 	
 	public void setWarningMessage(String warningMessage) {
 		String oldMessage = this.warningMessage;
 		this.warningMessage = warningMessage;
 		changeSupport.firePropertyChange(PROP_WARNING_MESSAGE, oldMessage,
 				warningMessage);
 	}
 	
 	public void setMessage(String message) {
 		changeSupport.firePropertyChange(PROP_MESSAGE, null,
 				message);
 	}
 
 	public String getWarningMessage() {
 		return warningMessage;
 	}
 
 	public IStatus validate(final IProgressMonitor monitor) {
 		result = null;
 		monitor.beginTask("Validating target", 4);
 		monitor.worked(1);
 		Display.getDefault().syncExec(new Runnable() {
 
 			public void run() {
 				data = getData();
 			}
 		});
 		monitor.worked(2);
 		final Thread toCancel = Thread.currentThread();
 		Thread cancelThread = new Thread(new Runnable() {
 
 			public void run() {
 				while (toCancel.isAlive() && !monitor.isCanceled()) {
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						// ignore
 					}
 				}
 
 				while (toCancel.isAlive() && monitor.isCanceled()) {
 					toCancel.interrupt();
 				}
 			}
 
 		});
 		cancelThread.start();
 		IStatus result = doValidate(data, monitor);
 		monitor.worked(1);
 
 		return result;
 	}
 
 	public void setRunnableContext(IRunnableContext runnableContext) {
 		this.runnableContext= runnableContext;
 	}
 
 	private IStatus doValidate(String[] data, IProgressMonitor monitor) {
 		IZendTarget[] targets;
 		monitor.subTask("Creating targets");
 		try {
 			targets = createTarget(data, monitor);
 		} catch (SdkException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (UnknownHostException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					"Unknown host " + e.getMessage(), e);
 		} catch (IOException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (CoreException e) {
 			return e.getStatus();
 		} catch (RuntimeException e) {
 			return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 					e.getMessage(), e);
 		} catch (CancelCreationException e) {
 			return new Status(IStatus.CANCEL, Activator.PLUGIN_ID,
 					e.getMessage());
 		}
 		if (targets == null || targets.length == 0) {
 			return new Status(
 					IStatus.ERROR,
 					Activator.PLUGIN_ID,
 					"Could not find any containers associated with Phpcloud account. "
 							+ "Create at least one container to be able to add Phpcloud target.");
 		}
 		monitor.subTask("Found "+targets.length+" target"+(targets.length == 1 ? "" : "s"));
 		List<IZendTarget> finalTargets = new ArrayList<IZendTarget>(targets.length);
 		IStatus status = null;
 		for (IZendTarget target : targets) {
 			if (target == null) {
 				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 						"Target was not created.");
 				continue;
 			}
 
 			String message = ((ZendTarget)target).validateTarget();
 			if (message != null) {
 				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
 				continue;
 			}
 
 			if (!target.isTemporary()) {
 				try {
 					target = testConnectAndDetectPort(target, monitor);
 				} catch (WebApiException ex) {
 					if (TargetsManager.isOpenShift(target)
 							&& ex instanceof UnexpectedResponseCode) {
 						UnexpectedResponseCode codeException = (UnexpectedResponseCode) ex;
 						if (codeException.getResponseCode() == ResponseCode.SERVER_NOT_CONFIGURED) {
 							OpenShiftInitializer initializer = new OpenShiftInitializer(
 									target, monitor);
 							initializer.init();
 							if (status == null) {
 								target = initializer.getTarget();
 							} else {
 								status = initializer.getStatus();
 							}
 						}
 					} else {
 						status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 								ex.getMessage(), ex);
 						continue;
 					}
 				} catch (RuntimeException e) {
 					status = new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 							e.getMessage(), e);
 					continue;
 				}
 			}
 			
 			if (target != null) {
 				finalTargets.add(target);
 			}
 		}
 		result = finalTargets.toArray(new IZendTarget[finalTargets.size()]);
 		if (status != null) {
 			if (finalTargets.size() == 0) {
 				return status;
 			} else {
 				return new Status(
 						IStatus.WARNING,
 						Activator.PLUGIN_ID,
 						"Cannot connect to at least one of containers on specified account. Only valid containers will be added.");
 			}
 		}
 		return Status.OK_STATUS;
 	}
 
 	private IZendTarget testConnectAndDetectPort(IZendTarget target,
 			IProgressMonitor monitor) throws WebApiException {
 		WebApiException catchedException = null;
 		IRequestListener preListener = null;
 		int[] portToTest = possiblePorts;
 		try {
 			if (TargetsManager.isPhpcloud(target)) {
 				preListener = new PhpcloudContainerListener(target);
 				WebApiClient.registerPreRequestListener(preListener);
 				portToTest = possiblePhpcloudPorts;
 			}
 			if (target.getHost().getPort() == -1) {
 				for (int port : portToTest) {
 					URL old = target.getHost();
 					URL host;
 					try {
 						host = new URL(old.getProtocol(), old.getHost(), port,
 								old.getFile());
 						((ZendTarget) target).setHost(host);
 					} catch (MalformedURLException e) {
 						// should never happen
 					}
 					monitor.subTask("Testing port " + port
 							+ " of detected target "
 							+ target.getHost().getHost());
 					try {
 						// test if this is zend server 6
 						try {
 							if (target.connect(WebApiVersion.V1_3,
 									ServerType.ZEND_SERVER)) {
 								return target;
 							}
 						} catch (WebApiException e) {
 							catchedException = e;
 							if (e instanceof UnexpectedResponseCode) {
 								UnexpectedResponseCode codeException = (UnexpectedResponseCode) e;
 								ResponseCode code = codeException
 										.getResponseCode();
 								switch (code) {
 								case UNSUPPORTED_API_VERSION:
 									try {
 										if (target.connect(WebApiVersion.UNKNOWN,
 												ServerType.ZEND_SERVER)) {
 											return target;
 										}
 									} catch (WebApiException ex) {
 										if (target.connect()) {
 											return target;
 										}
 									}
 									break;
 								case AUTH_ERROR:
 								case INSUFFICIENT_ACCESS_LEVEL:
 									throw e;
 								default:
 									break;
 								}
 							}
 						}
 					} catch (WebApiException ex) {
 						// before throwing exception, try out all possible ports
 						catchedException = ex;
 						if (ex instanceof UnexpectedResponseCode) {
 							UnexpectedResponseCode codeException = (UnexpectedResponseCode) ex;
 							ResponseCode code = codeException.getResponseCode();
 							switch (code) {
 							case UNSUPPORTED_API_VERSION:
 							case AUTH_ERROR:
 							case INSUFFICIENT_ACCESS_LEVEL:
 								throw catchedException;
 							default:
 								break;
 							}
 						}
 					}
 				}
 			} else {
 				// test if this is zend server 6
 				try {
 					if (target.connect(WebApiVersion.V1_3,
 							ServerType.ZEND_SERVER)) {
 						return target;
 					}
 				} catch (WebApiException e) {
 					try {
 						if (target.connect(WebApiVersion.UNKNOWN,
 								ServerType.ZEND_SERVER)) {
 							return target;
 						}
 					} catch (WebApiException ex) {
 						if (target.connect()) {
 							return target;
 						}
 					}
 				}
 			}
 		} finally {
 			if (preListener != null) {
 				WebApiClient.unregisterPreRequestListener(preListener);
 			}
 		}
 		if (catchedException != null) {
 			throw catchedException;
 		}
 		return null;
 	}
 
 	public IZendTarget[] getTarget() {
 		return result;
 	}
 
 	/**
 	 * Early checking if create() returns any GUI, or not. Some target types
 	 * don't need GUI - e.g. local target detection.
 	 * 
 	 * @return
 	 */
 	abstract public boolean hasPage();
 	
 	abstract protected boolean validatePage();
 	
 }
