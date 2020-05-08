 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.tomography.reconstruction.views;
 
 import gda.util.OSCommandRunner;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.provider.ReflectiveItemProviderAdapterFactory;
 import org.eclipse.emf.edit.provider.resource.ResourceItemProviderAdapterFactory;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.ILazyDataset;
 import uk.ac.diamond.scisoft.analysis.io.DataHolder;
 import uk.ac.diamond.scisoft.analysis.io.HDF5Loader;
 import uk.ac.diamond.tomography.reconstruction.Activator;
 import uk.ac.diamond.tomography.reconstruction.ReconUtil;
 import uk.ac.diamond.tomography.reconstruction.jobs.ReconSchedulingRule;
 import uk.ac.diamond.tomography.reconstruction.parameters.hm.DocumentRoot;
 import uk.ac.diamond.tomography.reconstruction.parameters.hm.HMxmlType;
 import uk.ac.diamond.tomography.reconstruction.parameters.hm.provider.HmItemProviderAdapterFactory;
 import uk.ac.gda.util.io.FileUtils;
 
 public abstract class BaseParameterView extends BaseTomoReconPart implements ISelectionProvider {
 	public static final String BLANK = "";
 	public static final String JOB_NAME_CREATING_COMPRESSED_NEXUS = "Creating compressed Nexus(%s)";
 
 	private static final String DATA_PATH_IN_NEXUS = "/entry1/tomo_entry/data/data";
 
 	private static final String COMPRESS_NXS_URL_FORMAT = "platform:/plugin/%s/scripts/compress_nxs.sh";
 
 	public static final String HM_FILE_EXTN = "hm";
 
 	private static final Logger logger = LoggerFactory.getLogger(BaseParameterView.class);
 
 	private IFile defaultSettingFile;
 
 	private File hmSettingsInProcessingDir;
 
 	private String pathname = "tomoSettings.hm";
 
 	private File fileOnFileSystem;
 
 	protected Text txtCentreOfRotation;
 
 	protected File reducedNexusFile;
 
 	protected int[] reducedDataShape;
 
 	protected Text txtFileName;
 	private EditingDomain editingDomain;
 
 	@Override
 	public void createPartControl(Composite parent) {
 		super.createPartControl(parent);
 		createSettingsFile();
 	}
 
 	protected void initializeEditingDomain() {
 		// Create an adapter factory that yields item providers.
 
 		if (editingDomain == null) {
 			ComposedAdapterFactory adapterFactory = new ComposedAdapterFactory(
 					ComposedAdapterFactory.Descriptor.Registry.INSTANCE);
 
 			adapterFactory.addAdapterFactory(new ResourceItemProviderAdapterFactory());
 			adapterFactory.addAdapterFactory(new HmItemProviderAdapterFactory());
 			adapterFactory.addAdapterFactory(new ReflectiveItemProviderAdapterFactory());
 
 			// Create the command stack that will notify this editor as commands are executed.
 			//
 			BasicCommandStack commandStack = new BasicCommandStack();
 
 			editingDomain = new AdapterFactoryEditingDomain(adapterFactory, commandStack,
 					new HashMap<Resource, Boolean>());
 		}
 	}
 
 	protected HMxmlType getHmXmlModel() {
 		if (hmSettingsInProcessingDir != null && hmSettingsInProcessingDir.exists()) {
 
 			Resource hmRes = getEditingDomain().getResourceSet().getResource(
 					org.eclipse.emf.common.util.URI.createFileURI(hmSettingsInProcessingDir.getAbsolutePath()), true);
 
 			EObject eObject = hmRes.getContents().get(0);
 			if (eObject != null) {
 
 				if (eObject instanceof DocumentRoot) {
 					DocumentRoot dr = (DocumentRoot) eObject;
 					return dr.getHMxml();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public EditingDomain getEditingDomain() {
 		if (editingDomain == null) {
 			initializeEditingDomain();
 		}
 		return editingDomain;
 	}
 
 	protected ILazyDataset getDataSetFromFileLocation(String path) {
 		HDF5Loader hdf5Loader = new HDF5Loader(path);
 		DataHolder loadFile;
 		ILazyDataset dataset = null;
 		try {
 			loadFile = hdf5Loader.loadFile();
 			dataset = loadFile.getLazyDataset(DATA_PATH_IN_NEXUS);
 
 		} catch (Exception ex) {
 			logger.error("Error", ex);
 		}
 		return dataset;
 	}
 
 	@Override
 	protected void processNewNexusFile() {
 		hmSettingsInProcessingDir = null;
 		hmSettingsInProcessingDir = getHmSettingsInProcessingDir();
 		try {
 			getReducedDataShape();
 		} catch (Exception e) {
 			logger.error("Unable to get reduced data shape", e);
 		}
 	}
 
 	protected int[] getReducedDataShape() throws Exception {
 		reducedNexusFile = getReducedNexusFile(nexusFile);
 		if (reducedNexusFile.exists()) {
 			ILazyDataset reducedDataset = getDataSetFromFileLocation(reducedNexusFile.getPath());
 			if (reducedDataset != null) {
 				reducedDataShape = reducedDataset.getShape();
 			} else {
 				logger.warn("Reduced data set not ready yet");
 			}
 		}
 		return reducedDataShape;
 	}
 
 	private File getReducedNexusFile(IFile nexusFile) throws Exception {
 		File reducedNexusFile = ReconUtil.getReducedNexusFile(nexusFile.getLocation().toString());
 		if (!reducedNexusFile.exists()) {
 			createReducedNexusFile(nexusFile.getLocation().toOSString(), reducedNexusFile.getPath());
 		}
 		return reducedNexusFile;
 	}
 
 	private void createReducedNexusFile(String actualNexusFileLocation, String outputNexusFileLocation)
 			throws Exception {
 		URL compressNxsURL = null;
 		try {
 			String compressNxsUrlString = String.format(COMPRESS_NXS_URL_FORMAT, Activator.PLUGIN_ID);
 			compressNxsURL = new URL(compressNxsUrlString);
 		} catch (MalformedURLException e) {
 			logger.error("Cant find compress_nxs script", e);
 		}
 		logger.debug("shFileURL:{}", compressNxsURL);
 		File compressNexusScript = null;
 		try {
 			compressNexusScript = new File(FileLocator.toFileURL(compressNxsURL).toURI());
 		} catch (URISyntaxException e) {
 			logger.error("Wrong location", e);
 		} catch (IOException e) {
 			logger.error("Unable to find file compressScript", e);
 		}
 
 		File reducedNxsFileHandle = new File(outputNexusFileLocation);
 
 		String reducedNxsFileParentLocation = reducedNxsFileHandle.getParent();
 		File reducedNxsFileParentFileHandle = new File(reducedNxsFileParentLocation);
 
 		reducedNxsFileParentFileHandle.mkdirs();
 
 		if (compressNexusScript != null) {
 			String compressNxsScriptFullLocation = compressNexusScript.getAbsolutePath();
 			String command = String.format("%s %s %s", compressNxsScriptFullLocation, actualNexusFileLocation,
 					outputNexusFileLocation);
 			runCommand(String.format(JOB_NAME_CREATING_COMPRESSED_NEXUS, nexusFile.getName()), command);
 		}
 	}
 
 	protected File getHmSettingsInProcessingDir() {
 		if (hmSettingsInProcessingDir == null) {
 			IPath hmSettingsPath = new Path(ReconUtil.getSettingsFileLocation(nexusFile).getAbsolutePath()).append(
 					new Path(nexusFile.getName()).removeFileExtension().toString()).addFileExtension(HM_FILE_EXTN);
 
 			File hmSettingsFile = new File(hmSettingsPath.toOSString());
 			if (!hmSettingsFile.exists()) {
 				logger.debug("hm settings path:{}", hmSettingsPath);
 				try {
 					hmSettingsFile = new File(hmSettingsPath.toString());
 					File file = defaultSettingFile.getLocation().toFile();
 					FileUtils.copy(file, hmSettingsFile);
 				} catch (IOException e) {
 					logger.error("Unable to create hm setting file.", e);
 				}
 			}
 			hmSettingsInProcessingDir = hmSettingsFile;
 		}
 
 		return hmSettingsInProcessingDir;
 	}
 
 	private void createSettingsFile() {
 		String blueprintFileLoc = null;
 
 		try {
 			String urlSpec = String.format("platform:/plugin/%s/resources/settings.xml", Activator.PLUGIN_ID);
 			blueprintFileLoc = new URL(urlSpec).toString();
 			logger.debug("shFileURL:{}", blueprintFileLoc);
 		} catch (MalformedURLException e) {
 			logger.error("URL is malformed.", e);
 		}
 
 		fileOnFileSystem = null;
 		try {
 			URL fileURL = new URL(blueprintFileLoc);
 			fileOnFileSystem = new File(FileLocator.resolve(fileURL).toURI());
 		} catch (URISyntaxException e) {
 			logger.error("URI problem", e);
 		} catch (IOException e) {
 			logger.error("File not found problem", e);
 		}
 		final IProject project = ResourcesPlugin.getWorkspace().getRoot()
 				.getProject(Activator.PROJECT_TOMOGRAPHY_SETTINGS);
 		if (!project.exists()) {
 			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {
 
 				@Override
 				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
 						InterruptedException {
 					project.create(monitor);
 					project.open(monitor);
 				}
 			};
 			try {
 				workspaceModifyOperation.run(null);
 			} catch (InvocationTargetException e) {
 				logger.error("Cannot create project for tomo.", e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted creating tomo project.", e);
 			}
 		} else if (!project.isAccessible()) {
 
 			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {
 
 				@Override
 				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
 						InterruptedException {
 					project.open(monitor);
 				}
 			};
 			try {
 				workspaceModifyOperation.run(null);
 			} catch (InvocationTargetException e) {
 				logger.error("Cannot open tomo project", e);
 			} catch (InterruptedException e) {
 				logger.error("Interrupted opening tomo project", e);
 			}
 		}
 
 		defaultSettingFile = project.getFile(pathname);
 		if (!defaultSettingFile.exists()) {
 			WorkspaceModifyOperation workspaceModifyOperation = new WorkspaceModifyOperation() {
 
 				@Override
 				protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
 						InterruptedException {
 					try {
 						defaultSettingFile.create(new FileInputStream(fileOnFileSystem), true, null);
 					} catch (FileNotFoundException e) {
 						logger.error("Unable to create default Setting File.", e);
 					}
 				}
 			};
 			try {
 				workspaceModifyOperation.run(new NullProgressMonitor());
 
 			} catch (InvocationTargetException e) {
				logger.error("TODO put description of error here", e);
 			} catch (InterruptedException e) {
				logger.error("TODO put description of error here", e);
 			}
 		}
 	}
 
 	@Override
 	public ISelection getSelection() {
 		if (getHmXmlModel() != null && txtCentreOfRotation != null
 				&& (txtCentreOfRotation.getText() != null && txtCentreOfRotation.getText().length() > 0)) {
 			double centreOfRotation = Double.parseDouble(txtCentreOfRotation.getText());
 			return new ParametersSelection(nexusFile.getLocation().toOSString(), centreOfRotation);
 		}
 		return null;
 	}
 
 	@Override
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		if (selection instanceof ParametersSelection) {
 			ParametersSelection parametersSelection = (ParametersSelection) selection;
 			if (nexusFile != null
 					&& parametersSelection.getNexusFileFullPath().equals(nexusFile.getLocation().toOSString())) {
 				txtCentreOfRotation.setText(Double.toString(parametersSelection.getCentreOfRotation()));
 			}
 		}
 	}
 
 	@Override
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 	}
 
 	public IFile getDefaultSettingFile() {
 		return defaultSettingFile;
 	}
 
 	protected void runCommand(final String jobName, final String command) throws Exception {
 		Job job = new Job(jobName) {
 			@Override
 			public IStatus run(IProgressMonitor monitor) {
 				monitor.beginTask(jobName, IProgressMonitor.UNKNOWN);
 				OSCommandRunner osCommandRunner = new OSCommandRunner(command, true, null, null);
 				if (osCommandRunner.exception != null) {
 					String msg = "Exception seen trying to run command " + osCommandRunner.getCommandAsString();
 					logger.error(msg);
 					logger.error(osCommandRunner.exception.toString());
 				} else if (osCommandRunner.exitValue != 0) {
 					String msg = "Exit code = " + Integer.toString(osCommandRunner.exitValue)
 							+ " returned from command " + osCommandRunner.getCommandAsString();
 					logger.warn(msg);
 					osCommandRunner.logOutput();
 				} else {
 					osCommandRunner.logOutput();
 				}
 				monitor.done();
 				return Status.OK_STATUS;
 			}
 		};
 		job.setRule(new ReconSchedulingRule(nexusFile.getLocation().toOSString()));
 		job.schedule();
 	}
 
 	protected Text createTextFileName(FormToolkit toolkit, Composite parent) {
 		txtFileName = toolkit.createText(parent, BLANK);
 		txtFileName.setEditable(false);
 		return txtFileName;
 	}
 }
