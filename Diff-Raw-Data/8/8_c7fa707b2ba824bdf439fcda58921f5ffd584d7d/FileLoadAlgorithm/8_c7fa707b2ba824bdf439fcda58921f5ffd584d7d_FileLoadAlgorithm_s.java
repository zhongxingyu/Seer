 package org.cishell.reference.gui.persistence.load;
 
 import java.io.File;
 import java.util.Dictionary;
 
 import org.cishell.framework.CIShellContext;
 import org.cishell.framework.algorithm.Algorithm;
 import org.cishell.framework.algorithm.AlgorithmExecutionException;
 import org.cishell.framework.algorithm.AlgorithmFactory;
 import org.cishell.framework.algorithm.ProgressMonitor;
 import org.cishell.framework.algorithm.ProgressTrackable;
 import org.cishell.framework.data.Data;
 import org.cishell.utilities.StringUtilities;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.log.LogService;
 
 public class FileLoadAlgorithm implements Algorithm, ProgressTrackable {
 	public static final String LOAD_DIRECTORY_PREFERENCE_KEY = "loadDir";
 
 	public static String defaultLoadDirectory;
 
 	private final LogService logger;
 	private BundleContext bundleContext;
 	private CIShellContext ciShellContext;
 	private ProgressMonitor progressMonitor = ProgressMonitor.NULL_MONITOR;
 
 	public FileLoadAlgorithm(
 			CIShellContext ciShellContext,
 			BundleContext bundleContext,
 			Dictionary<String, Object> preferences) {
 		this.logger = (LogService)ciShellContext.getService(LogService.class.getName());
 		this.ciShellContext = ciShellContext;
 		this.bundleContext = bundleContext;
 
 		// This is not done upon declaration because the preference service may not have started.
 		if (FileLoadAlgorithm.defaultLoadDirectory == null) {
 			FileLoadAlgorithm.defaultLoadDirectory = determineDefaultLoadDirectory(preferences);
 		}
 	}
 
 	public Data[] execute() throws AlgorithmExecutionException {
 		IWorkbenchWindow window = getFirstWorkbenchWindow();
 		Display display = PlatformUI.getWorkbench().getDisplay();
 		File file = getFileToLoadFromUser(window, display);

		return validateFile(window, display, file);
 	}
 
 	public ProgressMonitor getProgressMonitor() {
 		return this.progressMonitor;
 	}
 
 	public void setProgressMonitor(ProgressMonitor progressMonitor) {
 		this.progressMonitor = progressMonitor;
 	}
 
 	private static String determineDefaultLoadDirectory(Dictionary<String, Object> preferences) {
 		return StringUtilities.emptyStringIfNull(preferences.get(LOAD_DIRECTORY_PREFERENCE_KEY));
 	}
 
 	private IWorkbenchWindow getFirstWorkbenchWindow() throws AlgorithmExecutionException {
 		final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
 
 		if (windows.length == 0) {
 			throw new AlgorithmExecutionException(
 				"Cannot obtain workbench window needed to open dialog.");
 		} else {
 			return windows[0];
 		}
 	}
 
 	private File getFileToLoadFromUser(IWorkbenchWindow window, Display display) {
 		FileSelectorRunnable fileSelector = new FileSelectorRunnable(window);
 
 		if (Thread.currentThread() != display.getThread()) {
 			display.syncExec(fileSelector);
 		} else {
 			fileSelector.run();
 		}
 
 		return fileSelector.getFile();
 	}
 
 	private Data[] validateFile(IWorkbenchWindow window, Display display, File file)
 			throws AlgorithmExecutionException {
 		AlgorithmFactory validator = null;
 		boolean shouldTryValidator = true;
 
 		while (shouldTryValidator) {
 			try {
 				validator = getValidatorFromUser(window, display, file);
 
 				if ((file == null) || (validator == null)) {
 					String logMessage = "File loading canceled";
 					this.logger.log(LogService.LOG_WARNING, logMessage);
 
 					shouldTryValidator = false;
 				} else {
 					return FileValidator.validateFile(
 						file, validator, this.progressMonitor, this.ciShellContext, this.logger);
 				}
 			} catch (Throwable e) {
 				String logMessage =
 					"The chosen file is not compatible with the chosen file.  " +
 					"Please try a different format or cancel.";
 				this.logger.log(LogService.LOG_ERROR, logMessage);
 			}
 		}
 
 		return null;
 	}
 
 	private AlgorithmFactory getValidatorFromUser(
 			IWorkbenchWindow window, Display display, File file) {
 		ValidatorSelectorRunnable validatorSelector =
 			new ValidatorSelectorRunnable(window, this.bundleContext, file);
 
 		if (Thread.currentThread() != display.getThread()) {
 			display.syncExec(validatorSelector);
 		} else {
 			validatorSelector.run();
 		}
 
 		return validatorSelector.getValidator();
 	}
 }
