 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 /**
  * 
  */
 package org.eclipse.imp.preferences.wizards;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.imp.core.ErrorHandler;
 import org.eclipse.imp.preferences.PreferencesPlugin;
 import org.eclipse.imp.wizards.CodeServiceWizard;
 import org.eclipse.imp.wizards.ExtensionPointEnabler;
 import org.eclipse.imp.wizards.ExtensionPointWizardPage;
 import org.eclipse.imp.wizards.WizardPageField;
 import org.eclipse.imp.wizards.WizardUtilities;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 
 	
 
 public class NewPreferencesSpecificationWizard extends CodeServiceWizard {
 	
 	protected String fPreferencesPackage;
 	protected String fTemplate;	
 	protected String fFileName;
 	protected String fPagePackage;
 	protected String fPageClassNameBase;
 	protected String fPageName;
 	protected String fPageId;
 	protected String fMenuItem;
 	protected String fInitializerFileName;
 	protected String fAlternativeMessage;
 
     // This is actually the problem id for the prefs spec compiler
 	public static final String PREFERENCES_ID = "org.eclipse.imp.preferences.problem";
 	
 	
 	public void addPages() {
 	    addPages(new ExtensionPointWizardPage[] { new NewPreferencesSpecificationWizardPage(this) } );
 	}
 
 	protected List getPluginDependencies() {
 	    return Arrays.asList(new String[] { "org.eclipse.core.runtime", "org.eclipse.core.resources",
 		    "org.eclipse.imp.runtime" });
 	}
 
 	
     protected void collectCodeParms()
     {
     	//super.collectCodeParms();
     	
 		ExtensionPointWizardPage page= (ExtensionPointWizardPage) pages[0];
     	
 		WizardPageField field = null;
 		
     	fProject = page.getProjectOfRecord();
 		
 		field = page.getField("template");
 		fTemplate = field.fValue;
 		
 		field = page.getField("fileName");
 		fFileName = field.fValue;
 		
 		field = page.getField("pagePackage");
 		fPagePackage = field.fValue;
 		
 		field = page.getField("pageClassNameBase");
 		fPageClassNameBase = field.fValue;
 		
 		field = page.getField("pageName");
 		fPageName = field.fValue;
 		
 		field = page.getField("pageId");
 		fPageId = field.fValue;
 		
 		field = page.getField("category");
         fMenuItem = field.fValue;
 
         field = page.getField("alternative");
         fAlternativeMessage = field.fValue;
     }
 	
 	
 	public void generateCodeStubs(IProgressMonitor mon) throws CoreException {
 
         Map<String,String> subs= getStandardSubstitutions(fProject);
 
         // The user-specified user-friendly preferences page name
         // might include blanks, which should be excluded when the
         // name is used as an identifier within the page itself
         subs.remove("$PREFS_PAGE_NAME$");
         String identifierSegments[] = fPageName.split(" ");
         String pageIdentifier = identifierSegments[0];
         for (int i = 1; i < identifierSegments.length; i++) {
         	pageIdentifier = pageIdentifier + identifierSegments[i];
         }
         subs.put("$PREFS_PAGE_NAME$", pageIdentifier);
 
         String templateName = fTemplate.replace('\\', '/');
         subs.put("$PREFS_TEMPLATE_NAME$", templateName);
         int lastFileSep = templateName.length();
         if (templateName.lastIndexOf("/") > -1)
         	lastFileSep = templateName.lastIndexOf("/");
         subs.put("$PREFS_TEMPLATE_DIR$", templateName.substring(0, lastFileSep));
         
         if (fAlternativeMessage.length() != 0){
             subs.remove("$PREFS_ALTERNATIVE_MESSAGE$");
             subs.put("$PREFS_ALTERNATIVE_MESSAGE$", fAlternativeMessage);
             IFile pageSrc = WizardUtilities.createFileFromTemplate(
             	fFullClassName + ".java", "preferencesPageAlternative.java", fPackageFolder, getProjectSourceLocation(fProject), subs, fProject, mon);
             editFile(mon, pageSrc);
             return;
         }
 
 
 
 //        PrefspecsCompiler prefspecsCompiler = new PrefspecsCompiler(PREFERENCES_ID);
         // fFieldSpecs has full absolute path; need to give project relative path
         String projectLocation = fProject.getLocation().toString();
         String fieldSpecsLocation = fPagePackage;
         fieldSpecsLocation = fieldSpecsLocation.replace("\\", "/");
         if (fieldSpecsLocation.startsWith(projectLocation)) {
         	fieldSpecsLocation = fieldSpecsLocation.substring(projectLocation.length());
         }
         // createFileFromTemplate takes a short (unqualified) name for the
         // template file, but we may have a full absolute path name for the file,
         // so extract the short name from that
         String templateNameForCreatingFile = fTemplate.replace('\\', '/');
         int lastIndex = templateNameForCreatingFile.lastIndexOf('/');
         if (lastIndex >= 0 && lastIndex < templateNameForCreatingFile.length())
         	templateNameForCreatingFile = templateNameForCreatingFile.substring(lastIndex+1);
 
 //        IFile fieldSpecsFile = fProject.getFile(fieldSpecsLocation);
 //        PreferencesPageInfo pageInfo = prefspecsCompiler.compile(fieldSpecsFile, new NullProgressMonitor());
 //        String constantsClassName = fFullClassName + "Constants";
 //        String initializerClassName = fFullClassName + "Initializer";
 //
 //		ISourceProject sourceProject = null;
 //    	try {
 //    		sourceProject = ModelFactory.open(fProject);
 //    	} catch (ModelException me){
 //            System.err.println("NewPreferencesSpecificationWizard.generateCodeStubs(..):  Model exception:\n" + me.getMessage() + "\nReturning without parsing");
 //            return;
 //    	}
         
         
         // Enable the extension for the initializer
         ExtensionPointEnabler.enable(
             	fProject, "org.eclipse.core.runtime", "preferences",
             	new String[][] {
             			{ "initializer:class", fPagePackage + "." + fPageClassNameBase + "Initializer" },
                 	    },
         		false,
         		getPluginDependencies(),
         		mon);
         
         
         IFile prefSpecsSpec = WizardUtilities.createFileFromTemplate(
         	fFileName, PreferencesPlugin.PREFERENCES_PLUGIN_ID, templateNameForCreatingFile, fPagePackage, getProjectSourceLocation(fProject), subs, fProject, mon);
         
         editFile(mon, prefSpecsSpec);
 
 	}
 	
 	
     /**
      * Creates a file of the given name from the named template in the given location in the
      * given project. Subjects the template's contents to meta-variable substitution.
      * 
      * SMS 4 Aug 2007:  Copied and adapted from ExtensionPointWizard, where the original
      * has a couple of features specific to Java files (use of project source location and
      * formatting of Java source).
      * 
      * @param fileName
      * @param templateName
      * @param folder
      * @param replacements a Map of meta-variable substitutions to apply to the template
      * @param project
      * @param monitor
      * @return a handle to the file created
      * @throws CoreException
      */
 //    protected IFile createFileFromTemplate(
 //    	String fileName, String templateName, String folder, Map replacements,
 //	    IProject project, IProgressMonitor monitor)
 //    throws CoreException
 //	{
 //		monitor.setTaskName("NewPreferencesSpecificationWizard.createFileFromTemplate:  Creating " + fileName);
 //	
 //		String packagePath = getProjectSourceLocation() + fPagePackage.replace('.', '/');
 //		IPath specFilePath = new Path(packagePath + "/" + fFileName);
 //		final IFile file= project.getFile(specFilePath);
 //
 //		String templateContents= new String(getTemplateFile(templateName));
 //		String contents= performSubstitutions(templateContents, replacements);
 //	
 //		if (file.exists()) {
 //		    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, monitor);
 //		} else {
 //		    createSubFolders(packagePath, project, monitor);
 //		    file.create(new ByteArrayInputStream(contents.getBytes()), true, monitor);
 //		}
 //	//	monitor.worked(1);
 //		return file;
 //    }
 
 	/*
 	 * TODO:
 	 * - Figure out what to do about empty or null values, especially
 	 *   the menu item, where null might be allowed in the extension
 	 * - Define some constants to represent the constant strings that
 	 *   may be used here (e.g., for the file name extension).
 	 */
    	protected void writeOutGenerationParameters()
    		throws CoreException
    	{
    		String contents =
    			"PagePackage=" + fPagePackage + "\n" +
    			"PageClassNameBase=" + fPageClassNameBase + "\n" +
    			"PageName=" + fPageName + "\n" +
    			"PageId=" + fPageId + "\n" +
    			// Not sure about how to treat this last one ...
    			"PageMenuItem=" + (fMenuItem == null || fMenuItem.length() == 0 ? "TOP" : fMenuItem) + "\n" +
    			"AlternativeMessage=" + (fAlternativeMessage == null || fAlternativeMessage.length() == 0 ? "Message omitted." : fAlternativeMessage) + "\n";
    		
 		String packagePath = getProjectSourceLocation(fProject) + fPagePackage.replace('.', '/');
 		IPath paramFilePath = new Path(packagePath + "/" + fPageClassNameBase + ".genparams");	
 		final IFile file= fProject.getFile(paramFilePath);
 		
 		IProgressMonitor mon = new NullProgressMonitor();
 		if (file.exists()) {
 		    file.setContents(new ByteArrayInputStream(contents.getBytes()), true, true, mon);
 		} else {
 		    WizardUtilities.createSubFolders(packagePath, fProject	, mon);
 		    file.create(new ByteArrayInputStream(contents.getBytes()), true, mon);
 		}
    		
    	}
 	
 	
     /**
      * This method is called when 'Finish' button is pressed in the wizard.
      * We will create an operation and run it using wizard as execution context.
      * 
      * SMS 18 Dec 2006:
      * Overrode this method so as to call ExtensionPointEnabler with the
      * point id for preference menu items
      */
 	public boolean performFinish() {
 		// In UI thread while wizard-page fields remain accessible
 		collectCodeParms(); 
 		if (!checkFieldConsistency())
 			return false;
 		if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
     		return false;
 	   	
 		IRunnableWithProgress op= new IRunnableWithProgress() {
 		    public void run(IProgressMonitor monitor) throws InvocationTargetException {
 			IWorkspaceRunnable wsop= new IWorkspaceRunnable() {
 			    public void run(IProgressMonitor monitor) throws CoreException {
 				try {
					// SMS 18 Jun 2007:  Duplicative if generateCodeStubs() calls compile?
					//PreferencesPageInfo pageInfo = compile(fProject.getFile(new Path(fieldSpecsRelativeLocation)), monitor);
 					generateCodeStubs(new NullProgressMonitor());
 				   	writeOutGenerationParameters();
 				} catch (Exception e) {
 					ErrorHandler.reportError("NewPreferencesSpecificationWizard.performFinish():  Error adding extension or generating code", e);
 				} finally {
 					monitor.done();
 				}
 			    }
 			};
 			try {
 			    ResourcesPlugin.getWorkspace().run(wsop, monitor);
 			} catch (Exception e) {
 			    ErrorHandler.reportError("Could not add extension points", e);
 			}
 		    }
 		};
 		try {
 		    getContainer().run(true, false, op);
 		} catch (InvocationTargetException e) {
 		    Throwable realException= e.getTargetException();
 		    ErrorHandler.reportError("Error", realException);
 		    return false;
 		} catch (InterruptedException e) {
 		    return false;
 		}
 		
 		//postReminderAboutPreferencesInitializer();
 		
 		return true;	
 	}
     
 	
 //	protected void	postReminderAboutPreferencesInitializer()
 //	{
 //	   	String message = "REMINDER:  Update the plugin file for language to call the new preferences initializer\n";
 //	   	message = message + "to initialize default preferences:  " + fInitializerFileName;
 //		Shell parent = this.getShell();
 //		MessageBox messageBox = new MessageBox(parent, (SWT.OK));
 //		messageBox.setMessage(message);
 //		int result = messageBox.open();
 //	}
 
 	
 	/**
 	 * 
 	 */
 	protected boolean checkFieldConsistency() {
 		// does template file exist as a file?
 		File templateFile = new File(fTemplate);
 		if (!templateFile.exists())
 			return postErrorMessage("Specified template file does not exist");
 		if (!templateFile.isFile())
 			return postErrorMessage("Specified template file is not a file");
 		
 		return true;
 	}
 	
 	
 	protected boolean postErrorMessage(String msg) {
 		Shell parent = this.getShell();
 		MessageBox messageBox = new MessageBox(parent, (SWT.CANCEL));
 		messageBox.setMessage(msg);
 		int result = messageBox.open();
 		//if (result == SWT.CANCEL)
 		return false;
 	}
 	
 	
    /**
      * Return the names of any existing files that would be clobbered by the
      * new files to be generated.
      * 
      * @return	An array of names of existing files that would be clobbered by
      * 			the new files to be generated
      */
 	   protected String[] getFilesThatCouldBeClobbered() {
 			String packagePath = getProjectSourceLocation(fProject) + fPagePackage.replace('.', '/');
 			IPath specFilePath = new Path(packagePath + "/" + fFileName);
 			final IFile file= fProject.getFile(specFilePath);
 			if (file.exists()) {
 				return new String[] { file.getLocation().toString() } ;
 			} else {
 				return new String[] { };
 			}
 	    }
  
 	
 	   
 	   /**
 	     * Refreshes all resources in the entire project tree containing the given resource.
 	     * Crude but effective.
 	     * Copied from BuilderBase, where it's not static, and probably wouldn't belong
 	     * if it were.  Should probably put into a utility somewhere.
 	     */
 	    protected void doRefresh(final IResource resource) {
 	        new Thread() {
 	            public void run() {
 	        	try {
 	        	    resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 	        	} catch (CoreException e) {
 	        	    e.printStackTrace();
 	        	}
 	            }
 	        }.start();
 	    }
 
 }
