 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.applications.rainbow;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.net.URLDecoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.eclipse.swt.widgets.Shell;
 
 import net.sf.okapi.applications.rainbow.lib.FormatManager;
 import net.sf.okapi.applications.rainbow.lib.LanguageManager;
 import net.sf.okapi.applications.rainbow.lib.Utils;
 import net.sf.okapi.applications.rainbow.pipeline.IPredefinedPipeline;
 import net.sf.okapi.applications.rainbow.pipeline.PipelineEditor;
 import net.sf.okapi.applications.rainbow.pipeline.PipelineWrapper;
 import net.sf.okapi.applications.rainbow.pipeline.PreDefinedPipelines;
 import net.sf.okapi.applications.rainbow.utilities.IUtility;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.filters.DefaultFilters;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.plugins.PluginsManager;
 import net.sf.okapi.common.ui.BaseHelp;
 
 public class CommandLine {
 
 	private String appRootFolder;
 	private String sharedFolder;
 	private LanguageManager lm;
 	private Project prj;
 	private Shell shell;
 	private UtilityDriver ud;
 	private FilterConfigurationMapper fcMapper;
 	private UtilitiesAccess utilitiesAccess;
 	private BatchLog log;
 	private String utilityId;
 	private String pipelineFile;
 	private String optionsFile;
 	private boolean promptForOptions = true;
 	private BaseHelp help;
 	private PluginsManager pm;
 	private PrintStream ps = null;
 	
 	public int execute (Shell shell,
 		String[] args)
 	{
 		try {
 			File file  = new File(System.getProperty("user.home")+"/rainbowBatchLog.txt");  
 			ps = new PrintStream(new FileOutputStream(file));
 			System.setOut(ps);
 			System.setErr(ps);
 			
 			this.shell = shell;
 			printBanner();
 			initialize();
 			if ( !parseArguments(args) ) {
 				return 1;
 			}
 			
 			IPredefinedPipeline predefinedPipeline = null;
 			// Detect if the argument for -x is a true utility or a predefined pipeline
 			if ( utilityId != null ) {
 				PreDefinedPipelines ppMapper = new PreDefinedPipelines();
 				predefinedPipeline = ppMapper.create(utilityId);
 				if ( predefinedPipeline != null ) {
 					utilityId = null;
 				}
 			}
 			
 			// Launch either a utility
 			if ( utilityId != null ) {
 				launchUtility();
 			}
 			// Or a pipeline
 			else {
 				if ( pipelineFile != null ) {
 					launchPipeline(null);
 				}
 				else if ( predefinedPipeline != null ) {
 					launchPipeline(predefinedPipeline);
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			e.printStackTrace();
 			return 1;
 		}
 		finally {
 			if ( ps != null ) ps.close();
 		}
 		if (( log != null ) && ( log.getErrorCount() > 0 )) {
 			return 1;
 		}
 		else {
 			return 0;
 		}
 	}
 	
 	/**
 	 * Parse the command line.
 	 * @return True to execute something, false if error or exit immediately.
 	 * @throws Exception 
 	 */
 	private boolean parseArguments (String[] args) throws Exception {
 		String arg;
 		boolean continueAfter = false;
 		
 		// Creates default project
 		FormatManager fm = new FormatManager();
 		fm.load(null); // TODO: implement real external file, for now it's hard-coded
 		prj = new Project(lm);
 		prj.setInputRoot(0, appRootFolder, true);
 		prj.setInputRoot(1, appRootFolder, true);
 		prj.setInputRoot(2, appRootFolder, true);
 		boolean setOutSearch = false;
 		int inpList = -1;
 		optionsFile = null;
 		pipelineFile = null;
 		
 		for ( int i=0; i<args.length; i++ ) {
 			arg = args[i];
 			if ( "-p".equals(arg) ) { // Load a project //$NON-NLS-1$
 				prj.load(nextArg(args, ++i));
 			}
 			else if ( "-x".equals(arg) ) { // Execute utility //$NON-NLS-1$
 				utilityId = nextArg(args, ++i);
 				continueAfter = true;
 			}
 			else if ( "-np".equals(arg) ) { // No prompt for options //$NON-NLS-1$
 				promptForOptions = false;
 			}
 			else if (( "-h".equals(arg) ) || ( "-?".equals(arg) )) { // Help //$NON-NLS-1$ //$NON-NLS-2$
 				help.showWiki("Rainbow Help#Rainbow - Command Line"); //$NON-NLS-1$
 			}
 			else if ( "-se".equals(arg) ) { // Source encoding //$NON-NLS-1$
 				prj.setSourceEncoding(nextArg(args, ++i));
 			}
 			else if ( "-te".equals(arg) ) { // Target encoding //$NON-NLS-1$
 				prj.setTargetEncoding(nextArg(args, ++i));
 			}
 			else if ( "-sl".equals(arg) ) { // Source language //$NON-NLS-1$
 				prj.setSourceLanguage(LocaleId.fromString(nextArg(args, ++i)));
 			}
 			else if ( "-tl".equals(arg) ) { // Target language //$NON-NLS-1$
 				prj.setTargetLanguage(LocaleId.fromString(nextArg(args, ++i)));
 			}
 			else if (( "-ir".equals(arg) ) || ( "-ir0".equals(arg) )) { // Input root list 0 //$NON-NLS-1$ //$NON-NLS-2$
 				prj.setInputRoot(0, nextArg(args, ++i), true);
 			}
 			else if ( "-pd".equals(arg) ) { //$NON-NLS-1$
 				prj.setCustomParametersFolder(nextArg(args, ++i));
 				prj.setUseCustomParametersFolder(true);
 			}
 			else if ( "-rd".equals(arg) ) { //$NON-NLS-1$
 				// Use the project file name to set the root directory
 				prj.setPath(Util.ensureSeparator(nextArg(args, ++i), false)+"project.rnb");
 			}
 			else if ( "-opt".equals(arg) ) { //$NON-NLS-1$
 				optionsFile = nextArg(args, ++i);
 			}
 			else if ( "-pln".equals(arg) ) {
 				pipelineFile = nextArg(args, ++i);
 				continueAfter = true;
 			}
 			else if ( "-fc".equals(arg) ) { //$NON-NLS-1$
 				Input inp = prj.getLastItem(inpList);
 				if ( inp == null ) { 
 					throw new RuntimeException(Res.getString("CommandLine.fsBeforeInputError")); //$NON-NLS-1$
 				}
 				else {
 					inp.filterConfigId = nextArg(args, ++i);
 				}
 			}
 			else if ( "-o".equals(arg) ) { // Output file //$NON-NLS-1$
 				File f = new File(nextArg(args, ++i));
 				prj.setOutputRoot(Util.getDirectoryName(f.getAbsolutePath()));
 				prj.setUseOutputRoot(true);
 				prj.pathBuilder.setUseExtension(false);
 				prj.pathBuilder.setUseReplace(true);
 				prj.pathBuilder.setReplace(Util.getFilename(f.getAbsolutePath(), true));
 				setOutSearch = true;
 			}
 			else if ( !arg.startsWith("-") ) { // Input file //$NON-NLS-1$
 				if ( ++inpList > 2 ) {
 					throw new RuntimeException(Res.getString("CommandLine.tooManyInput")); //$NON-NLS-1$
 				}
 				File f = new File(arg);
 				String[] res = fm.guessFormat(f.getAbsolutePath());
 				prj.inputLists.get(inpList).clear();
 				prj.setInputRoot(inpList, Util.getDirectoryName(f.getAbsolutePath()), true);
 				prj.addDocument(inpList, f.getAbsolutePath(), res[0], null, res[1], false);
 			}
 			else {
 				log.error(Res.getString("CommandLine.invalidCommand")+args[i]); //$NON-NLS-1$
 				continueAfter = false;
 			}
 			
 			// Sets the search part of the output builder if an output path was specified.
 			if ( setOutSearch ) {
 				Input inp = prj.getLastItem(0);
 				if ( inp == null ) { 
 					throw new RuntimeException(Res.getString("CommandLine.noInput")); //$NON-NLS-1$
 				}
 				else {
 					prj.pathBuilder.setSearch(inp.relativePath);
 				}
 			}
 		}
 		return continueAfter;
 	}
 	
 	private String nextArg (String[] args, int index) {
 		if ( index >= args.length ) {
 			throw new RuntimeException(Res.getString("CommandLine.missingParameter")); //$NON-NLS-1$
 		}
 		return args[index];
 	}
 	
 	private void printBanner () {
 		System.out.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
 		System.out.println(Res.getString("CommandLine.bannerApplication")); //$NON-NLS-1$
 		System.out.println(Res.getString("CommandLine.bannerVersion")+getClass().getPackage().getImplementationVersion()); //$NON-NLS-1$
 		System.out.println("-------------------------------------------------------------------------------"); //$NON-NLS-1$
 	}
 	
 	private void initialize () throws Exception {
     	// Get the location of the main class source
     	File file = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getFile());
     	appRootFolder = URLDecoder.decode(file.getAbsolutePath(),"utf-8"); //$NON-NLS-1$
     	boolean fromJar = appRootFolder.endsWith(".jar");
     	// Remove the JAR file if running an installed version
     	if ( fromJar ) appRootFolder = Util.getDirectoryName(appRootFolder); //$NON-NLS-1$
     	// Remove the application folder in all cases
     	appRootFolder = Util.getDirectoryName(appRootFolder);
 		sharedFolder = Utils.getOkapiSharedFolder(appRootFolder, fromJar);
 		help = new BaseHelp(appRootFolder+File.separator+"help"); //$NON-NLS-1$
 
 		log = new BatchLog();
 		lm = new LanguageManager();
 		lm.loadList(sharedFolder + File.separator + "languages.xml"); //$NON-NLS-1$
 		
 		// Set up the filter configuration mapper
 		fcMapper = new FilterConfigurationMapper();
 		// Get pre-defined configurations
 		DefaultFilters.setMappings(fcMapper, false, true);
 		// Discover and add plug-ins
 		pm = new PluginsManager();
 		pm.discover(new File(appRootFolder+File.separator+"dropins"), true);
 		fcMapper.addFromPlugins(pm);
 
 		utilitiesAccess = new UtilitiesAccess();
 		utilitiesAccess.loadMenu(sharedFolder+File.separator+"rainbowUtilities.xml");
 	}
 	
 	private void launchUtility () {
 		// Create the utility driver if needed
 		if ( ud == null ) {
 			fcMapper.setCustomConfigurationsDirectory(prj.getParametersFolder());
 			fcMapper.updateCustomConfigurations();
 			ud = new UtilityDriver(log, fcMapper, utilitiesAccess, help, false);
 		}
 		
 		// Get default/project data for the utility and instantiate the utility object
 		ud.setData(prj, utilityId);
 		IUtility util = ud.getUtility();
 		
 		// Override the options if a file is provided from the command-line
 		if ( optionsFile != null ) {
 			if ( util.hasParameters() ) {
 				// Ignore errors to allow to create an options file
 				File f = new File(optionsFile);
 				util.getParameters().load(Util.toURI(f.getAbsolutePath()), true);
 			}
 		}
 		
 		// Prompt to edit the parameters if requested 
 		if ( promptForOptions ) {
 			if ( !ud.checkParameters(shell) ) return;
 			// Save the file if needed
 			if (( optionsFile != null ) && ( util.hasParameters() )) {
 				util.getParameters().save(optionsFile);
 			}
 		}
 		
 		// Run the utility
 		ud.execute(shell);
 	}
 
 	private void launchPipeline (IPredefinedPipeline predefinedPipeline) {
 		// Save any pending data
 		fcMapper.setCustomConfigurationsDirectory(prj.getParametersFolder());
 		fcMapper.updateCustomConfigurations();
 		
 		PipelineWrapper wrapper = new PipelineWrapper(fcMapper, appRootFolder, pm,
 			prj.getProjectFolder(), prj.getInputRoot(0), null);
 
 		// If we have a predefined pipeline: set it
 		if ( predefinedPipeline != null ) {
 			// Get the parameters data from the project
 			predefinedPipeline.setParameters(wrapper.getAvailableSteps(),
 				prj.getUtilityParameters(predefinedPipeline.getId()));
 			// Load the pipeline
 			wrapper.loadPipeline(predefinedPipeline, null);
 		}
 		else { // It's a pipeline file
 			wrapper.load(pipelineFile);
 		}
 
 		if ( promptForOptions ) {
 			PipelineEditor dlg = new PipelineEditor();
 			int res = dlg.edit(shell, wrapper.getAvailableSteps(), wrapper,
 				(predefinedPipeline==null) ? null : predefinedPipeline.getTitle(),
 				help, null,
 				(predefinedPipeline==null) ? -1 : predefinedPipeline.getInitialStepIndex());
 		
 			if ( res == PipelineEditor.RESULT_CANCEL ) {
 				return; // No execution, no save
 			}
 
 			if ( res == PipelineEditor.RESULT_CLOSE ) {
 				return; // No execution
 			}
 		}
 		
 		// Else: execute
 //		startWaiting(Res.getString("MainForm.startWaiting"), true); //$NON-NLS-1$
 		wrapper.execute(prj);
 	}
 }
