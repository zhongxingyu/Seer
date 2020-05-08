 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  *  * This class is used to compute the decision time in a report
  * 
  * Contributors:
  *   Jacques Bouthillier -Initial implementation of the R4E report generation
  *   
  *******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.report.internal.dialog;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Level;
 import org.eclipse.birt.report.engine.api.EngineConfig;
 import org.eclipse.birt.report.engine.api.EngineException;
 import org.eclipse.birt.report.engine.api.HTMLRenderOption;
 import org.eclipse.birt.report.engine.api.IPDFRenderOption;
 import org.eclipse.birt.report.engine.api.IRenderOption;
 import org.eclipse.birt.report.engine.api.IReportEngine;
 import org.eclipse.birt.report.engine.api.IReportRunnable;
 import org.eclipse.birt.report.engine.api.IRunAndRenderTask;
 import org.eclipse.birt.report.engine.api.PDFRenderOption;
 import org.eclipse.birt.report.engine.api.RenderOption;
 import org.eclipse.birt.report.engine.api.ReportEngine;
 import org.eclipse.birt.report.model.api.DataSourceHandle;
 import org.eclipse.birt.report.model.api.ModuleHandle;
 import org.eclipse.birt.report.model.api.activity.SemanticException;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.StringConverter;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.SerializeFactory;
 import org.eclipse.mylyn.reviews.r4e.internal.transform.ModelTransform;
 import org.eclipse.mylyn.reviews.r4e.internal.transform.resources.ReviewGroupRes;
 import org.eclipse.mylyn.reviews.r4e.report.impl.IR4EReport;
 import org.eclipse.mylyn.reviews.r4e.report.internal.Activator;
 import org.eclipse.mylyn.reviews.r4e.report.internal.util.OSPLATFORM;
 import org.eclipse.mylyn.reviews.r4e.report.internal.util.Popup;
 import org.eclipse.mylyn.reviews.r4e.report.internal.util.R4EReportString;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.osgi.framework.Bundle;
 
 /**
  * @author Jacques Bouthillier
  * 
  */
 public class ReportGeneration implements IR4EReport {
 
 	// BIRT Debug level
 	// private Level fBIRT_LEVEL = Level.ALL; //Use the debug level desired
 	// Level.OFF by default
 	private Level fBIRT_LEVEL = Level.OFF; // Use the debug level desired
 	// Level.OFF by default
 	private String fLogConfigdir = "C:/R4EDebug"; // Put a directory to log
 	// the debug from BIRT
 	// Test variable
 	String fDesignName = "C:/git/r4eSecond/r4e/org.eclipse.mylyn.reviews.r4e.report/design/globalReport.rptdesign";
 
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	public final String fFILE_SEPARATOR = "/"; // Portable
 
 	// Use the definition of the report Data Source
 	private final String fGROUP_DATA_SOURCE = "Merged_group";
 	private final String fREVIEW_DATA_SOURCE = "Merged_review";
 
 	//Report design template
 	private final String fInspectionRecordFileName = "inspectionRecord.rptdesign";
 	private final String fGlobalReportFileName = "globalReport.rptdesign";
 	
 	// REPORT NAME
 	private final String fR4E_GLOBAL_REPORT = "R4E_REPORT";
 	private final String fINSPECT_RECORD = "InspectionRecord";
 	private final String fNO_NAME = "NoNameYet";
 
 	private final String fREVIEW_END = "_review";
 	private final String fGROUP_END = "_group_root";
 	private final String fEXTENSION = ".xrer";
 
 	// Variable used for the report
 	private final String fSOURCE_PREFIX = "Merged";
 	private final String fPROJECT_REPORT_DIR = "Reports";
 	private final String fPROJECT_WORKING_DIR = "r4e_work";
 	private final String fGROUP_FILE = fSOURCE_PREFIX + fGROUP_END + fEXTENSION;
 	private final String fFILE_SEP = "_";
 	
 
 
 	// Report extension file
 	private final String fEXTENSION_SEPARATOR = ".";
 	public final String fHTML_EXTENSION = HTML_EXTENSION;
 	public final String fPDF_EXTENSION = PDF_EXTENSION;
 
 	// Report type to be generated
 	public final String fINSPECTION_RECORD_TYPE = INSPECTION_RECORD_TYPE;
 	public final String fGLOBAL_REPORT_TYPE = GLOBAL_REPORT_TYPE;
 	public final String fSINGLE_REPORT_TYPE = SINGLE_REPORT_TYPE;
 	private final int fGLOBAL_REPORT_NUM = 0;
 	private final int fINSPECTION_RECORD_NUM = 1;
 	private final int fSINGLE_REPORT_NUM = 2;
 
 
 	// Format the date
 	private final SimpleDateFormat fDATE_FORMAT = new SimpleDateFormat(
 			"dd-MMM-yyyy_hhmmss", new Locale("eng", "US"));
 	
 	//Folder to find the report design
 	private final String fFOLDER = "src/org/eclipse/mylyn/reviews/r4e/report/internal/design";
 
 	// ------------------------------------------------------------------------
 	// Variables
 	// ------------------------------------------------------------------------
 	private String fR4E_REPORT = "R4E_REPORT";
 	private InputStream fInputDesignName = null;
 	private String fDesignFileName = "globalReport.rptdesign"; //Default value
 
 	// Variable to test if the file to generate the report exist
 	private Boolean fPROPERTY_FILE_CREATED = false;
 	private Composite fCompositeParent = null;
 	
 
 	private File fReportDir = null;
 	private String fReportName = "";
 
 	private int fCurrent_report_num = -1;
 
 	private File[] fReviewNameList = null;
 	private String fOutputFormat = fHTML_EXTENSION; //Set HTML as default value
 	
 	private R4EReviewGroup floadedGroup = null;
 	private final RModelFactoryExt	fFactory		= SerializeFactory.getModelExtension();
 	private String fGroupFile = "";
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 	/**
 	 * C'tor
 	 */
 	public ReportGeneration() {
 
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/****************************************/
 	/*                                      */
 	/*       PUBLIC METHOD                  */
 	/*                                      */
 	/****************************************/
 	/**
 	 * Set the type of report
 	 * 
 	 * @param aReportType
 	 */
 	public void setReportType(String aReportType) {
 		Activator.FTracer.traceInfo("ReportGeneration.setReportType() : " + aReportType);
 		if (aReportType.equals(fGLOBAL_REPORT_TYPE)) {
 			fDesignFileName = fGlobalReportFileName;
 			setReportName(fR4E_GLOBAL_REPORT);
 			fCurrent_report_num = fGLOBAL_REPORT_NUM;
 		} else if (aReportType.equals(fINSPECTION_RECORD_TYPE)) {
 			fDesignFileName = fInspectionRecordFileName;
 			String inspectionReportName = buildInspectionReportName();
 			setReportName(inspectionReportName);
 			fCurrent_report_num = fINSPECTION_RECORD_NUM;
 		} else {
 			// Will create a list of report with the GLOBAL_REPORT template
 			fDesignFileName = fGlobalReportFileName;
 			setReportName(fR4E_GLOBAL_REPORT);
 			fCurrent_report_num = fSINGLE_REPORT_NUM;
 		}
 		Activator.FTracer.traceInfo("ReportGeneration.setReportType() design file name: "
 				+ fDesignFileName + "\t\t cur number report: "
 				+ fCurrent_report_num);
 	}
 
 	/**
 	 * Set the output format to generate the report
 	 * 
 	 * @param String
 	 *            aFormatOutput
 	 */
 	public void setOuputFormat(String aFormatOutput) {
 		Activator.FTracer.traceInfo("ReportGeneration.setOuputFormat() : " + aFormatOutput);
 		fOutputFormat = aFormatOutput;
 	}
 
 	/**
 	 * Generate the selected report
 	 * @param String agroupFile File of the Group
 	 */
 	public void handleReportGeneration(final String  agroupFile) {
 		
 		//Keep a copy of the original group file
 		setGroupFile (agroupFile);
 		// Get the file directory of the selected review
 		
 		final File groupFile = new File(agroupFile);
 		
 		File rootDir = null;
 		
 		if (groupFile != null ) {
 			rootDir = groupFile.getParentFile();
 		}
 
 		if (rootDir != null) {
 
 			//Should think to use a progress bar here
 			
 //			// Generate a pop-up dialog to notify the report generation
 //			final Shell s = new Shell(SWT.MODELESS);
 //
 //			String message = R4EReportString.getString("Popup.messageOneMoment");
 //			final MessageDialog dialog = new MessageDialog(
 //					s,
 //					R4EReportString.getString("Popup.messageTitle"),
 //					null, // accept
 //					// the
 //					// default
 //					// window
 //					// icon
 //					message, MessageDialog.INFORMATION,
 //					new String[] { IDialogConstants.OK_LABEL }, 0);
 //			// ok is the default
 
 			// Thread to generate the report while the window is still
 			// editable
 			final File rootDirFinal = rootDir;
 			new Thread() {
 				public void run() {
 					setDesignReportTemplate();
 					if (fCurrent_report_num == fSINGLE_REPORT_NUM) {
 						// Generate a list of reports
 						prepareMultiReport(rootDirFinal);
 
 					} else {
 						prepareReport(rootDirFinal);
 					}
 				}
 			}.start();
 
 			// Open the wait window
 			//dialog.open();
 //			Activator.FTracer.traceInfo
 //					("After the dialog close ret: "
 //							+ dialog.getReturnCode());
 
 		}
 
 	}
 		
 
 	/**
 	 * Register the list of selected reviews
 	 * @param File[] aListSelectedReview
 	 */
 	public void setReviewListSelection(File[] aListSelectedReview) {
 		fReviewNameList = aListSelectedReview;
 	}
 
 	/**
 	 * Test if the report type selected is an inspection record
 	 * 
 	 * @return Boolean
 	 */
 	public Boolean isInspectionRecord() {
 		return fCurrent_report_num == fINSPECTION_RECORD_NUM;
 	}
 
 	/**
 	 * Count the number of selected review
 	 * 
 	 * @return int
 	 */
 	public int selectedReviewNumber() {
 		// //Get the selected review count
 		File[] selectedReview = getReviewListSelection();
 		int nbReview = selectedReview.length;
 
 		return nbReview;
 	}
 	
 	/****************************************/
 	/*                                      */
 	/*       PRIVATE METHOD                 */
 	/*                                      */
 	/****************************************/
 	/**
 	 * Prepare the report name to be generated
 	 * 
 	 * @param aStr
 	 *            Initial string for the report name
 	 */
 	private void setReportName(String aStr) {
 		fR4E_REPORT = aStr;
 	}
 
 	/**
 	 * Set the configuration to allow BIRT to log information
 	 * @param aEngineConf
 	 * @return
 	 */
 	private EngineConfig createBirtDebug(EngineConfig aEngineConf) {
 //		String operatingSystem = Platform.getOS();
 //		Activator.FTracer.traceInfo("ReportGeneration.createBirtDebug() operating SYSTEM: "
 //				+ operatingSystem);
 		// Add BIRT log only for window for now
 		if (OSPLATFORM.FTYPE.isWindowsOS()) {
 			if (fLogConfigdir != null) {
 				File f = new File(fLogConfigdir);
 				File newFile = createReportDir(f, "");
 				Activator.FTracer.traceInfo("ReportGeneration.createBirtDebug() created:"
 						+ newFile.getAbsolutePath());
 				if (!fBIRT_LEVEL.equals(Level.OFF)) {
 					aEngineConf.setLogConfig(newFile.getAbsolutePath(), fBIRT_LEVEL);
 				}
 			}
 
 		}
 		return aEngineConf;
 	}
 
 	/**
 	 * Build the inspection record based on the review name
 	 * 
 	 * @return String
 	 */
 	private String buildInspectionReportName() {
 		String inspectionFileName = "";
 		StringBuilder sb = new StringBuilder();
 		File[] reviewFile = getReviewListSelection();
 		if (reviewFile != null ) {
 			if (reviewFile.length == 1) {
 				sb.append(fINSPECT_RECORD);
 				sb.append(fFILE_SEP);
 				sb.append(reviewFile[0].getName());
 				inspectionFileName = sb.toString();
 			}
 			
 		} else {
 			sb.append(fINSPECT_RECORD);
 			sb.append(fFILE_SEP);
 			sb.append(fNO_NAME);
 			inspectionFileName = sb.toString();		
 		}
 		Activator.FTracer.traceInfo("buildInspectionReportName() :" + inspectionFileName);
 		return inspectionFileName;
 	}
 
 	private File[] getReviewListSelection() {
 		if (fReviewNameList != null) {
 			for (int i = 0; i < fReviewNameList.length; i++) {
 				Activator.FTracer.traceInfo("getReviewListSelection() [" + i + " ] : "
 						+ fReviewNameList[i].getAbsolutePath());
 			}			
 		} else {
 			//Display a pop-up when no review
 			String message = R4EReportString.getString("Popup.noReview" );
 			Popup.warningRunnable(null, message);
 		}
 		return fReviewNameList;
 	}
 
 	/**
 	 * Keep a copy of the original group file
 	 * @param aGroupFile
 	 */
 	private void setGroupFile (String aGroupFile ) {
 		fGroupFile = aGroupFile;
 	}
 	
 	private String getGroupFile () {
 		return fGroupFile;
 	}
 	
 
 	/** *************************** */
 	/*                              */
 	/* METHOD TO PREPARE THE REPORT */
 	/*                              */
 	/** *************************** */
 
 	/**
 	 * Set the design template to generate a report
 	 * i.e. Global report or Inspection report
 	 */
 	private void setDesignReportTemplate() {
 		//Test 
 		Bundle bdl = Platform.getBundle(Activator.FPLUGIN_ID);
 
 		// Build the report design file name
 		StringBuilder rptDesign = new StringBuilder();
 		rptDesign.append(fFOLDER);
 		rptDesign.append(fFILE_SEPARATOR);
 		rptDesign.append(fDesignFileName);
 		
 		
 		IPath path = new Path (rptDesign.toString());
 		try {
 			fInputDesignName = FileLocator.openStream(bdl, path, false);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Activator.FTracer.traceInfo
 			("ReportGeneration.setDesignReportTemplate() Input stream ioException: "
 					+ e);
 		}
 		
 		if (fInputDesignName == null) {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.setDesignReportTemplate() Did not find the design input stream");
 			return;
 		}
 	}
 
 	/**
 	 * Return the selected template to generate a report
 	 * 
 	 * @return InputStream
 	 */
 	private InputStream getDesignReportTemplate() {
 		return fInputDesignName;
 	}
 
 	/**
 	 * Clean the temporary report directory and notify the end-user hat he
 	 * cannot save the report
 	 * 
 	 * @param aWorkDir
 	 *            Temporary directory to generate a report
 	 * @param aRootDir
 	 *            parent directory for the report
 	 */
 	private void cleanAndNotify(File aWorkDir, File aRootDir) {
 		Activator.FTracer.traceInfo
 				("ReportGeneration.cleanAndNotify() has no permission, no report created");
 		cleanReportDirectory(aWorkDir);
 		String reportParent = "";
 
 		if (fReportDir != null) {
 			reportParent = fReportDir.getAbsolutePath();
 		} else {
 			// Build the report directory having no privileges
 			if (aRootDir != null) {
 				File fileReport = new File(aRootDir.getParentFile()
 						.getAbsoluteFile().toString()
 						+ fFILE_SEPARATOR + fPROJECT_REPORT_DIR);
 				reportParent = fileReport.getAbsolutePath();
 			}
 		}
 		String message = R4EReportString.getFormattedString(
 				"Popup.accessDenied", reportParent);
 		Popup.warningRunnable(null, message);
 
 	}
 
 	/**
 	 * Prepare the R4E report
 	 * 
 	 * @param aRootDir
 	 *            File current directory for the review files
 	 */
 	private void prepareReport(File aRootDir) {
 		File workingDir = aRootDir;
 		Boolean displayReport = true;
 		Activator.FTracer.traceInfo
 				("ReportGeneration.prepareReport() Parent report Directory: "
 						+ workingDir);
 		// Build a unique string for the temporary working directory
 		Date d = new Date();
 		long ti = d.getTime();
 		String stDate = StringConverter.asString(ti);
 		StringBuilder workingStr = new StringBuilder();
 		workingStr.append(fPROJECT_WORKING_DIR);
 		workingStr.append(fFILE_SEP);
 		workingStr.append(stDate);
 
 		// Create a temporary directory to maintain the report data
 		workingDir = createReportDir(aRootDir, workingStr.toString());
 
 		// Build the report directory
 		Boolean ok = BuildReportDir(aRootDir);
 
 		// Verify if we can proceed to generate a report
 		if (!ok) {
 			cleanAndNotify(workingDir, aRootDir);
 			return;
 		}
 
 		// Test if we can have the working directory
 		if (workingDir == null) {
 			// Try to build a new location
 			// Create a temporary directory to maintain the report data
 			workingDir = createReportDir(fReportDir, workingStr.toString());
 		}
 
 		if (workingDir == null) {
 			
 			String message = R4EReportString.getString("Popup.noReportDir" );
 			Popup.error(null, message);
 			return;
 		}
 
 		IReportRunnable runnable = null;
 		// Create an Engine Config object
 		EngineConfig config = new EngineConfig();
 
 		// Set up the location and level of logging output.
 		config = createBirtDebug(config);
 
 		ReportEngine engine = new ReportEngine(config);
 		try {
 
 			// //Get the selected review Name
 			File[] selectedReview = getReviewListSelection();
 			int nbReview = selectedReview.length;
 			
 			
 			//Set the default  report type based on the number of review selected
 			if (nbReview == 1) {
 				//Select the Inspection Record
 				setReportType(fINSPECTION_RECORD_TYPE);
 			} else {
 				//Select the Global report
 				setReportType(fGLOBAL_REPORT_TYPE);				
 			}
 			//Need to set the template in case it is different for the default
 			setDesignReportTemplate();
 			
 			// runnable = engine.openReportDesign(fDesignName);
 			// runnable = engine.openReportDesign(inputDesignName);
 			runnable = engine.openReportDesign(getDesignReportTemplate());
 			ModuleHandle reportDesignHandle = runnable.getDesignHandle()
 			.getModuleHandle();
 
 			//Build the file needed for the report
 			ReviewGroupRes destGroup = prepareReportSourceFiles (workingDir, selectedReview);
 
 			File[] destFile = workingDir.listFiles();
 			
 			//Should get the Group here
 			for (int count = 0; count < destFile.length; count++) {
 				Activator.FTracer.traceInfo("List reportFile: " +
 						destFile[count].getName());	
 				if (destFile[count].isFile()) {
 					//Register the Group file for the report
 					prepareDataSource(destFile[count], reportDesignHandle, workingDir);
 				}
 				File [] revFile = destFile[count].listFiles();
 				
 				for (int i = 0; revFile != null && i < revFile.length; i++) {
 					//Now we should have one file for each Data Source
 					// Set the data source for the report
 					prepareDataSource(revFile[i], reportDesignHandle, workingDir);
 				}
 			}
 			
 			// Prepare the report output
 			// Decide here if we generate HTML or PDF format report
 			IRunAndRenderTask renderTask = prepareOutputFile(runnable,
 					engine, fOutputFormat);
 			// Execute the preparation of the report
 			renderTask.run();
 
 		} catch (EngineException aEngineEx) {
 			aEngineEx.printStackTrace();
 			Activator.FTracer.traceInfo
 					("ReportGeneration.prepareReport() Generate Report.run() EngineException : "
 							+ aEngineEx);
 
 		} catch (ResourceHandlingException e) {
 			e.printStackTrace();
 			Activator.FTracer.traceInfo
 			("ReportGeneration.prepareReport() ResourceHandlingException : "
 					+ e);
 		}
 		// Need to destroy the engine
 		engine.destroy();
 		// Clean the directory maintaining the data used for the report
 		// after the report is generated
 		cleanReportDirectory(workingDir);
 
 		// Pop-up the report file
 		if (displayReport) {
 			displayReport();
 		}
 	}
 
 	/**
 	 * Create the output transformation file needed to query for the report
 	 * @param destinationDir
 	 * @return
 	 * @throws ResourceHandlingException
 	 */
 	private ReviewGroupRes prepareReportSourceFiles (File aDestinationDir, File[] aSelectedReview) throws ResourceHandlingException {
 
 		URI origURI = URI.createFileURI(getGroupFile());
 		floadedGroup = fFactory.openR4EReviewGroup(origURI);
 
 		
 		// //Get the selected review Name
 		int nbReview = aSelectedReview.length;
 
 		// Open Original Serialised model
 		//Create the destination folder
 		URI destFolderURI = URI.createFileURI(aDestinationDir.getAbsolutePath());
 		ReviewGroupRes destGroup = null;
 		try {
 			// Use temporary group name 
 			destGroup = ModelTransform.instance.createReviewGroupRes(destFolderURI, floadedGroup.getName(), fSOURCE_PREFIX);
 		} catch (ResourceHandlingException e) {
 			e.printStackTrace();
 			//fail("Exception");
 		}
 
 
 		R4EReview dReview = null;
 		URI destURI = destGroup.eResource().getURI();
 		for (int countReview = 0; countReview < nbReview; countReview++) {
 			String reviewName = aSelectedReview[countReview].getName();
 			// Open the review
 			try {
 				fFactory.openR4EReview(floadedGroup, reviewName);
 				dReview = ModelTransform.instance.transformReview(origURI, destURI, reviewName);
 			} catch (ResourceHandlingException e) {
 				e.printStackTrace();
 				//fail("Exception");
 			}
 
 		}
 		return destGroup;	
 	}
 	
 	/** ********************************** */
 	/* Handling the creation for the */
 	/* necessary report file to generate */
 	/* a report */
 	/** ********************************** */
 
 	private void setPropertyFileCreated(Boolean aBol) {
 		fPROPERTY_FILE_CREATED = aBol;
 	}
 
 	private void resetReportingFileCreated() {
 		// Only test those files, all others will always be available
 		setPropertyFileCreated(false);
 	}
 
 	/**
 	 * Prepare the report output to be generated
 	 * 
 	 * @param aRunnable
 	 * @param aReportEngine
 	 * @return
 	 */
 	private IRunAndRenderTask prepareOutputFile(IReportRunnable aRunnable,
 			IReportEngine aReportEngine, String aFormat) {
 
 		IRenderOption options = new RenderOption();
 		options.setOutputFormat(aFormat);
 
 		// Prepare the final report file extension and format
 		if (options.getOutputFormat().equalsIgnoreCase(fHTML_EXTENSION)) {
 			HTMLRenderOption htmlOption = new HTMLRenderOption(options);
 			String output = createReportName(fHTML_EXTENSION);
 			htmlOption.setOutputFileName(output);
 			htmlOption.setOutputFormat(fHTML_EXTENSION);
 
 		} else if (options.getOutputFormat().equalsIgnoreCase(fPDF_EXTENSION)) {
 
 			IPDFRenderOption pdfOptions = new PDFRenderOption(options);
 //			pdfOptions.setOption(IPDFRenderOption.FIT_TO_PAGE, true);
 			pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW, IPDFRenderOption.FIT_TO_PAGE_SIZE);
 			// pdfOptions.setOption(IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY,
 			// true);
 
 			// XXX The following is not available in Eclipse 3.3, may need
 			// Eclipse 3.4 or Eclipse 3.5
 
 			// pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW,
 			// ( Integer.valueOf( PDFRenderOption.FIT_TO_PAGE_SIZE ) |
 			// Integer.valueOf( PDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES ) ) );
 
 			// pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW,
 			// ( Integer.valueOf( PDFRenderOption.FIT_TO_PAGE_SIZE ) |
 			// Integer.valueOf( PDFRenderOption.ENLARGE_PAGE_SIZE )) );
 
 			// pdfOptions.setOption(IPDFRenderOption.PAGE_OVERFLOW,
 			// Integer.valueOf( PDFRenderOption.OUTPUT_TO_MULTIPLE_PAGES ));
 
 			// XXX The following option is not available under Eclipse 3.3
 			// Solaris flavour
 			// IPDFRenderOption.PDF_TEXT_WRAPPING
 			// pdfOptions.setOption(IPDFRenderOption.PDF_TEXT_WRAPPING,
 			// new Boolean(true));
 			String output = createReportName(fPDF_EXTENSION);
 			pdfOptions.setOutputFileName(output);
 		}
 
 		//
 		// HTMLRenderOption htmlOption = new HTMLRenderOption();
 		// String output = createReportName();
 		// htmlOption.setOutputFileName(output);
 		// htmlOption.setOutputFormat("html");
 
 		// Set the output file
 		IRunAndRenderTask task = aReportEngine.createRunAndRenderTask(aRunnable);
 		// task.setRenderOption(htmlOption);
 		task.setRenderOption(options);
 
 		// HTMLRenderContext renderContext = new HTMLRenderContext();
 		// // Apply the rendering context to the task
 		// HashMap appContext = new HashMap();
 		// appContext.put(EngineConstants.APPCONTEXT_HTML_RENDER_CONTEXT,
 		// renderContext);
 		// task.setAppContext(appContext);
 
 		return task;
 	}
 
 	/**
 	 * Main method which will prepare the Data source for the report
 	 * 
 	 */
 	private void prepareDataSource(File aFile, ModuleHandle aReportDesignHandle,
 			File aReportDir) {
 
 		// Remove the file extension
 		String[] sta = aFile.getName().split(fEXTENSION);
 		String name = sta[0];
 
 		// Verify the end of the file
 		if (name.endsWith(fGROUP_END)) {
 			// Lets save as well the group file since it is not under
 			// any review
 			groupSetup(aFile, aReportDesignHandle);
 		} else if (name.endsWith(fREVIEW_END)) {
 			// We have the REVIEW property file
 			propertyReviewSetup(aFile, aReportDesignHandle, aReportDir);
 		}
 	}
 
 	/**
 	 * Create the report filename which is: - path of the group review directory
 	 * - r4e_ - user name creating the report - Date the report is created -
 	 * file extension - ex: c:/report/R4E_Report_lmcbout_19-Jun-2008_0955.html
 	 * 
 	 * @return report name being created
 	 */
 	private String createReportName(String aExtension ) {
 		Date d = new Date();
 		String user = getLocalUser();
 		StringBuilder filename = new StringBuilder();
 		filename.append(getReportDir().getAbsolutePath());
 		filename.append(fFILE_SEPARATOR);
 		filename.append(fR4E_REPORT);
 		filename.append(fFILE_SEP);
 		filename.append(user);
 		filename.append(fFILE_SEP);
 		filename.append(fDATE_FORMAT.format(d));
 		filename.append(fEXTENSION_SEPARATOR);
 		filename.append(aExtension);
 		Activator.FTracer.traceInfo("Report Name: " + filename.toString());
 		fReportName = filename.toString();
 		return fReportName;
 	}
 
 
 	/**
 	 * Prepare the setup for the review property file
 	 * 
 	 * @param aFile
 	 * @param aReportDesignHandle
 	 * @param aReportDir
 	 */
 	private void propertyReviewSetup(File aFile, ModuleHandle aReportDesignHandle,
 			File aReportDir) {
 
 		setDataSourceFile(aFile, aReportDesignHandle, fREVIEW_DATA_SOURCE);
 		setPropertyFileCreated(true);
 	}
 
 
 	/**
 	 * Prepare the setup for the group (Department) file
 	 * 
 	 * @param aFile
 	 * @param aReportDesignHandle
 	 * @param aReportDir
 	 */
 	private void groupSetup(File aFile, ModuleHandle aReportDesignHandle) {
 
 		// Set the GROUP file to the Data Source
 		setDataSourceFile(aFile, aReportDesignHandle, fGROUP_DATA_SOURCE);
 
 	}
 
 	/**
 	 * Set the report Item file as a new Data source file
 	 * 
 	 * @param aFile
 	 * @param aReportDesignHandle
 	 */
 	private void setDataSourceFile(File aFile, ModuleHandle aReportDesignHandle,
 			String data_source) {
 
 		DataSourceHandle dso = aReportDesignHandle.findDataSource(data_source);
 
 		// Test if we found or not the appropriate data source
 		if (dso == null) {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.setDataSourceFile() DataSourceHandle is NULL");
 		} else {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.setDataSourceFile() DataSourceHandle : "
 							+ dso.getFullName()
 							+ "\n\t XPath: "
 							+ dso.getXPath()
 							+ "\n\t module: "
 							+ dso.getModule().getFileName()
 							+ "\n\t BeforeOpen: "
 							+ dso.getElement().toString()
 							+ "\n\t ElementFactory: "
 							+ dso.getElementFactory().toString());
 
 			try {
 
 				Activator.FTracer.traceInfo
 						("ReportGeneration.setDataSourceFile() DSO before open OK ");
 
 				// Set the new file name for the data source
 				dso.setProperty("FILELIST", aFile.getAbsolutePath());
 			} catch (SemanticException e) {
 				e.printStackTrace();
 				Activator.FTracer.traceInfo
 						("ReportGeneration.setDataSourceFile() DSO before open EXCEPTION: "
 								+ e);
 			}
 		}
 	}
 
 
 	/** ********************************* */
 	/*                                    */
 	/* METHOD TO SET THE REPORT DIRECTORY */
 	/*                                    */
 	/** ********************************* */
 
 	/**
 	 * Method used to create the temporary directory holding the files to
 	 * generate the report
 	 * 
 	 * @param aReportDir
 	 *            Directory from which we refer to define the destination report
 	 *            directory
 	 * @param aDirSt 
 	 * 				
 	 * @return directory File
 	 */
 	private File createReportDir(File aReportDir, String aDirSt) {
 		// File parent = new File(reportDir.getAbsoluteFile().toString()
 		// + FFILE_SEPARATOR + PROJECT_WORKING_DIR);
 		File parent = new File(aReportDir.getAbsoluteFile().toString()
 				+ fFILE_SEPARATOR + aDirSt);
 		if (parent.exists()) {
 //			Activator.FTracer.traceInfo
 //					("ReportGeneration.createReportDir() Directory exists already.");
 		} else {
 			// 1) Create directory
 			if (parent.mkdir()) {
 //				Activator.FTracer.traceInfo("ReportGeneration.createReportDir() Directory "
 //						+ parent.getAbsolutePath() + " created.");
 				// Set the report directory permission
 				//ReviewHeader.setFilePermission(parent.getAbsolutePath());
 				//setFilePermission (parent.getAbsolutePath());
 				} else {
 				Activator.FTracer.traceInfo("ReportGeneration.createReportDir() ERROR");
 				return null;
 			}
 		}
 
 		return parent;
 	}
 
 	/**
 	 * Clean the report directory from old data used to generate the last report
 	 * 
 	 * @param aReportDir
 	 *            report temporary directory
 	 */
 	private void cleanReportDirectory(File aReportDir) {
 		// Verify if the directory exist
 		if (aReportDir != null) {
 			Activator.FTracer.traceInfo("ReportGeneration.cleanReportDirectory(): "
 					+ aReportDir.getAbsolutePath());
 			if (aReportDir.exists()) {
 				Activator.FTracer.traceInfo
 						("ReportGeneration.cleanReportDirectory() Directory exists already.");
 				Boolean b;
 				for (File f : aReportDir.listFiles()) {
 					b = f.delete();
 					Activator.FTracer.traceInfo
 							("ReportGeneration.cleanReportDirectory() Files to delete: "
 									+ f + "\t deleted: " + b);
 				}
 				b = aReportDir.delete();
 				Activator.FTracer.traceInfo
 						("ReportGeneration.cleanReportDirectory() removing Directory: "
 								+ aReportDir + "\t : " + b);
 			} else {
 				Activator.FTracer.traceInfo
 						("ReportGeneration.cleanReportDirectory() Directory "
 								+ aReportDir.getAbsolutePath()
 								+ " DO not exist.");
 			}
 		}
 
 		// Reset the file verifying the temporary reporting file creation
 		resetReportingFileCreated();
 	}
 
 	/**
 	 * Build a directory to maintain the report If the directory already exist,
 	 * no creation occurs
 	 * 
 	 * @param aRootDir
 	 *            directory for the current review
 	 */
 	private Boolean BuildReportDir(File aRootDir) {
 		Boolean ok = true;
 		File parentDir = aRootDir.getParentFile();
 //		Activator.FTracer.traceInfo
 //				("ReportGeneration.BuildReportDir() Parent report Directory: "
 //						+ parentDir);
 		fReportDir = createReportDir(parentDir, fPROJECT_REPORT_DIR);
 
 		// Verify if we can write the report in this directory
 		Boolean b = verifyWritePermission(fReportDir);
 		// If not allowed, offer the user to select a new directory
 		if (!b) {
 			// Need to create a new path to save the report
 			fReportDir = createSaveDirectory(fReportDir);
 			Boolean bo = verifyWritePermission(fReportDir);
 			// If the user select another directory not allowed, just put a
 			// pop-up now
 			if (!bo) {
 				ok = false; // Permission denied
 			}
 		}
 		return ok;
 	}
 
 	/**
 	 * Verify if the selected directory allows writing in it
 	 * 
 	 * @param aDir
 	 * @return Boolean
 	 */
 	private Boolean verifyWritePermission(File aDir) {
 		Boolean b = true;
 		if (aDir != null) {
 			Activator.FTracer.traceInfo("ReportGeneration.verifyWritePermission() BEGIN:  "
 					+ aDir.getAbsolutePath());
 			String test = "test.txt";
 			StringBuilder sb = new StringBuilder();
 			sb.append(aDir.toString());
 			sb.append(fFILE_SEPARATOR);
 			sb.append(test);
 			File f = new File(sb.toString());
 			try {
 				f.createNewFile();
 				//Activator.FTracer.traceInfo("Create a Report file is allow here");
 			} catch (IOException e) {
 				Activator.FTracer.traceInfo("Create a Report file is NOT allow: "
 						+ e.getMessage());
 				b = false;
 			}
 			if (f.exists()) {
 				Boolean del;
 				del = f.delete();
 				Activator.FTracer.traceInfo("Temp file " + f.getAbsolutePath() + " deleted: "
 						+ del);
 			}
 
 		} else {
 			// False, it is a null directory
 			b = false;
 		}
 
 		return b;
 	}
 
 	/**
 	 * Create a new directory to save the report
 	 * 
 	 * @param aReportDir
 	 * @return Boolean
 	 */
 	private File createSaveDirectory(final File aReportDir) {
 		final ReportDirectorySelection dirSelect = new ReportDirectorySelection(
 				fCompositeParent.getShell());
 		Runnable runnable = new Runnable() {
 
 			public void run() {
 				dirSelect.create();
 				if (aReportDir != null) {
 					// Set the default directory if not null
 					dirSelect.setFieldDirectory(aReportDir.getAbsolutePath());
 				}
 				dirSelect.open();
 				// TODO We need to create a dialog to allow the end-user to
 				// enter his
 				// own directory here
 				// StringBuilder sb = new StringBuilder();
 				// sb.append(reportDir.getParentFile().getParent());
 				// Activator.FTracer.traceInfo("New report Directory should be: " +
 				// sb.toString());
 				// File f = new File(sb.toString());
 
 			}
 		};
 		Display.getDefault().syncExec(runnable);
 		File fi = dirSelect.getReportDirectory();
 		if (fi != null) {
 			Activator.FTracer.traceInfo("File return from directory selection: "
 					+ fi.getAbsolutePath());
 		} else {
 			Activator.FTracer.traceInfo("File return from directory selection: NULL ");
 		}
 
 		return fi;
 	}
 
 	private File getReportDir() {
 		return fReportDir;
 	}
 
 	private String getReportName() {
 		return fReportName;
 	}
 
 	/**
 	 * Method notifying the user the generation of the report is completed, and
 	 * open the report in the workbench editor
 	 */
 	private void displayReport() {
 
 		Runnable runnable = new Runnable() {
 
 			public void run() {
 				String msg = " The following report has been generated: \n"
 						+ getReportName();
 				Popup.info(null, msg);
 				// To open in the workspace file editor
 				openFile();
 			}
 
 		};
 		Display.getDefault().asyncExec(runnable);
 	}
 
 	//
 	// Adjusted to the report file handling
 	//
 	private void openFile() {
 
 		String filename = getReportName();
 		Activator.FTracer.traceInfo("ReportGeneration.openFile() " + filename);
 
 		IWorkbench workbench = Activator.getDefault().getWorkbench();
 		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
 
 		IFileStore fileStore = EFS.getLocalFileSystem().getStore(
 				new Path(filename));
 		IWorkbenchPage page = workbench.getActiveWorkbenchWindow()
 				.getActivePage();
 
 		// fileStore = fileStore.getChild("a file name);
 		Activator.FTracer.traceInfo("ReportGeneration.openFile() " + fileStore.getName()
 				+ " Directory=" + fileStore.fetchInfo().isDirectory()
 				+ " Exist=" + fileStore.fetchInfo().exists());
 		if (fileStore.fetchInfo().isDirectory()) {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.openFile() File is a directory or does not exist. "
 							+ fileStore.getName());
 
 		} else {
 			if (fileStore.fetchInfo().exists()) {
 				Activator.FTracer.traceInfo("ReportGeneration.openFile() File ( "
 						+ fileStore.getName() + " ) exist, just handle it now");
 			}
 		}
 
 		if (!fileStore.fetchInfo().isDirectory()
 				&& fileStore.fetchInfo().exists()) {
 
 			String editorId = getEditorId(fileStore);
 			try {
 				// Test the current active editor
 				IEditorPart fep = page.getActiveEditor();
 				// Create the new file to display in the eclipse editor
 				IEditorInput input = new FileStoreEditorInput(fileStore);
 				if (fep != null) {
 					String strConvert = fep.getTitleToolTip()
 							.replace("\\", "/");
 					Activator.FTracer.traceInfo("ReportGeneration.openFile() find "
 							+ "editorpart return: " + fep.toString()
 							+ "\n\t tooltip value: " + fep.getTitleToolTip()
 							+ "\n\t FileName: " + filename
 							+ "\n\t tooltip string converted: " + strConvert);
 					// If the editor already display the file
 					if (filename.equals(strConvert)) {
 						Activator.FTracer.traceInfo
 								("ReportGeneration.OpenFile() current editor already display the file: "
 										+ fep.getTitleToolTip());
 					} else {
 						Activator.FTracer.traceInfo
 								("ReportGeneration.openFile() need refresh to the right file: "
 										+ filename);
 						// Need to change the editor part
 						fep = page.openEditor(input, editorId);
 					}
 
 				} else {
 					Activator.FTracer.traceInfo
 							("ReportGeneration.openFile() find editorpart return NULL");
 					// Need to change the editor part
 					// input cannot be null here
 					fep = page.openEditor(input, editorId);
 				}
 
 			} catch (PartInitException e) {	
 				String str = R4EReportString.getString("messageError1");
 				Activator.getDefault().logError(str,e);
 				String msg = str +" : "+ fileStore.getName();
 				MessageDialog.openError(window.getShell(), str, msg);
 			}
 
 		} else {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.openFile() File is a directory or does not exist. "
 							+ fileStore.getName());
 		}
 
 	}
 
 	//
 	// Method borrowed from DisplayFileAction.java
 	// Adjusted to the report file handling
 	//
 	private String getEditorId(IFileStore aFileStore) {
 		IWorkbench workbench = Activator.getDefault().getWorkbench();
 
 		IEditorRegistry editorRegistry = workbench.getEditorRegistry();
 
 		IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(aFileStore
 				.getName(), null);
 		// check the OS for in-place editor (OLE on Win32)
 		if (descriptor == null
 				&& editorRegistry
 						.isSystemInPlaceEditorAvailable(aFileStore.getName())) {
 			Activator.FTracer.traceInfo
 					("ReportGeneration.getEditorId() SYSTEM_INPLACE_EDITOR_ID");
 			descriptor = editorRegistry
 					.findEditor(IEditorRegistry.SYSTEM_INPLACE_EDITOR_ID);
 		}
 
 		// check the OS for external editor
 		if (descriptor == null
 				&& editorRegistry.isSystemExternalEditorAvailable(aFileStore
 						.getName())) {
 			descriptor = editorRegistry
 					.findEditor(IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID);
 		}
 
 		if (descriptor != null) {
 			Activator.FTracer.traceInfo("ReportGeneration.getEditorId() editor id "
 					+ descriptor.getId() + " " + descriptor.getLabel());
 			return descriptor.getId();
 		}
 
 		Activator.FTracer.traceInfo("ReportGeneration.getEditorId() DEFAULT_TEXT_EDITOR_ID");
 		return EditorsUI.DEFAULT_TEXT_EDITOR_ID;
 	}
 
 	/**
 	 * Prepare the R4E report
 	 * 
 	 * @param aRootDir
 	 *            File current directory for the review files
 	 */
 	private void prepareMultiReport(File aRootDir) {
 		File workingDir = null;
 		File origWorkingDir = aRootDir.getParentFile();
 		IReportEngine engine = null;
 		Activator.FTracer.traceInfo
 				("ReportGeneration.prepareMultiReport() Parent report Directory: "
 						+ workingDir);
 		// Build the report directory
 		Boolean ok = BuildReportDir(aRootDir);
 
 		// Verify if we can proceed to generate a report
 		if (!ok) {
 			cleanAndNotify(workingDir, aRootDir);
 			return;
 		}
 
 		if (origWorkingDir == null) {
 			String message = " Could not create the report directory";
 			Popup.error(null, message);
 			return;
 		}
 
 		IReportRunnable runnable = null;
 		// Create an Engine Config object
 		EngineConfig config = new EngineConfig();
 
 		// Set up the location and level of logging output.
 		config = createBirtDebug(config);
 
 		// //Get the selected review Name
 		File[] selectedReview = getReviewListSelection();
 		int nbReview = selectedReview.length;
 
 		// Set the progress bar limits
 		InitProgressBar(nbReview);
 
 		engine = new ReportEngine(config);
 		for (int countReview = 0; countReview < nbReview; countReview++) {
 			// Adjust the progress bar
 			notifyProgress(countReview);
 
 			// Build a unique string for the temporary working directory
 			Date d = new Date();
 			long ti = d.getTime();
 			String stDate = StringConverter.asString(ti);
 			StringBuilder workingStr = new StringBuilder();
 			workingStr.append(fPROJECT_WORKING_DIR);
 			workingStr.append(fFILE_SEP);
 			workingStr.append(stDate);
 
 			// Create a temporary directory to maintain the report data
 			workingDir = createReportDir(origWorkingDir, workingStr.toString());
 			setDesignReportTemplate();
 			try {
 				runnable = engine.openReportDesign(getDesignReportTemplate());
 				Activator.FTracer.traceInfo
 						("ReportGeneration.prepareReport()Engine getconfig: "
 								+ engine.getConfig()
 								+ "\n\t author:"
 								+ runnable.getProperty(IReportRunnable.AUTHOR));
 				ModuleHandle reportDesignHandle = runnable.getDesignHandle()
 						.getModuleHandle();
 
 				// *************************************
 				//Build the file needed for the report
 				File[] selRev = new File[1];
 				selRev[0] = selectedReview[countReview];
 				ReviewGroupRes destGroup = prepareReportSourceFiles (workingDir, selRev);
 
 				File[] destFile = workingDir.listFiles();
 				//Should get the Group here
 				for (int count = 0; count < destFile.length; count++) {
 					Activator.FTracer.traceInfo("List reportFile: " +
 							destFile[count].getName());	
 					if (destFile[count].isFile()) {
 						//Register the Group file for the report
 						prepareDataSource(destFile[count], reportDesignHandle, workingDir);
 					}
 					File [] revFile = destFile[count].listFiles();
 					
 					for (int i = 0; revFile != null && i < revFile.length; i++) {
 						//Now we should have one file for each Data Source
 						// Set the data source for the report
 						prepareDataSource(revFile[i], reportDesignHandle, workingDir);
 					}
 				}
 
 
 					// Prepare the report output
 					// Decide here if we generate HTML or PDF format report
 					IRunAndRenderTask renderTask = prepareOutputFile(runnable,
 							engine, fOutputFormat);
 					// Execute the preparation of the report
 					renderTask.run();
 
 				// Clean the directory maintaining the data used for the report
 				// after the report is generated
 				cleanReportDirectory(workingDir);
 
 			} catch (EngineException e) {
 				String str = R4EReportString.getString("messageError2");
 				Activator.getDefault().logError(str,e);
 				Activator.FTracer.traceInfo
 						("ReportGeneration.prepareReport() Generate Report.run() EngineException : "
 								+ e);
 			} catch (ResourceHandlingException e) {
 				e.printStackTrace();
 				Activator.FTracer.traceInfo
 				("ReportGeneration.prepareReport() ResourceHandlingException : "
 						+ e);
 			
 			}
 
 		}
 		// Remove the progress bar
 		notifyProgressComplete();
 
 		// Pop-up the last report file
 		displayReport();
 
 		// Need to destroy the engine
 		engine.destroy();
 	}
 
 	/**
 	 * Init the progress bar for the multiple review generation
 	 * 
 	 * @param aNum
 	 */
 	private void InitProgressBar(final int aNum) {
 //		Display.getDefault().syncExec(new Runnable() {
 //
 //			public void run() {
 //				if (fReviewNameTableModel != null
 //						&& !(fReviewNameTableModel.isDisposed())) {
 //					fReviewNameTableModel.setprogressLimit(aNum);
 //				}
 //			}
 //		});
 		// if (reviewNameTableModel != null) {
 		// reviewNameTableModel.setprogressLimit(nb);
 		// }
 	}
 
 	/**
 	 * Close the progress bar after generating the list of reports
 	 */
 	private void notifyProgressComplete() {
 //		Display.getDefault().asyncExec(new Runnable() {
 //
 //			public void run() {
 //				if (fReviewNameTableModel != null
 //						&& !(fReviewNameTableModel.isDisposed())) {
 //					// Adjust the column width
 //					fReviewNameTableModel.adjustColumnWidth();
 //
 //					fReviewNameTableModel.setProgressComplete();
 //				}
 //			}
 //		});
 
 	}
 
 	/**
 	 * Adjust the progress bar when we generates a list of report
 	 * 
 	 * @param aCountReview
 	 *            int
 	 */
 	private void notifyProgress(final int aCountReview) {
 //		Display.getDefault().asyncExec(new Runnable() {
 //
 //			public void run() {
 //				if (fReviewNameTableModel != null
 //						&& !(fReviewNameTableModel.isDisposed())) {
 //					fReviewNameTableModel.progressDisplay(aCountReview);
 //				}
 //			}
 //		});
 	}
 
 	/**
 	 * Read the current user
 	 * @return String
 	 */
 	private String getLocalUser() {
 		String localUser = new String(System.getProperty("user.name"));
 		return localUser.toLowerCase();
 	}
 	
 
 //=================================================================
 //
 //      TO DELETE AFTER THIS LINE
 //
 //=================================================================	
 	
 	
 	//For Progress BAR if needed later
 //	public void setReviewNameTableModel(final ReviewNameTableModel aRevNameTable) {
 //		fReviewNameTableModel = aRevNameTable;
 //	}
 
 
 	
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 //		String[] strReviews = {"C:\\temp\\openTest\\Formal-3"};
 		String[] strReviews = {"C:/temp/openTest/Formal-3"};
 		
 		File[] listSelectReviews = new File[strReviews.length];
 		for (int i = 0; i < strReviews.length;i++) {
 			//Create a file out of the reviews name string
 			listSelectReviews[i] = new File(strReviews[i]);
 		}
 //		String groupFile = "C:\\temp\\openTest\\Various_group_root.xrer";
 		String groupFile = "C:/temp/openTest/Various_group_root.xrer";
 //		String groupFile = "C:/temp/openStorage/TestWindowOpen_group_root.xrer";
 		ReportGeneration reportGen = new ReportGeneration();
 		reportGen.setReviewListSelection(listSelectReviews);
 		reportGen.setReportType(reportGen.fGLOBAL_REPORT_TYPE);
 		reportGen.setOuputFormat(reportGen.fHTML_EXTENSION);
 		reportGen.handleReportGeneration(groupFile);
 	}
 
 }
