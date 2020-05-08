 package org.jikespg.uide.wizards;
 	
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.safari.jikespg.builder.JikesPGBuilder;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.eclipse.uide.core.ErrorHandler;
 import org.eclipse.uide.runtime.RuntimePlugin;
 import org.eclipse.uide.wizards.ExtensionPointEnabler;
 import org.eclipse.uide.wizards.ExtensionPointWizard;
 import org.eclipse.uide.wizards.ExtensionPointWizardPage;
 import org.jikespg.uide.JikesPGPlugin;
 
 /**
  * This wizard creates a JikesPG grammar, a "parser" language service, and a
  * stub IParseController implementation for use with the UIDE.
  */
 public class NewLPGGrammarWithParserWrapperWizard extends ExtensionPointWizard implements INewWizard
 {
     private IProject fProject;
     private GrammarOptions fGrammarOptions;
 
     protected String fLanguageName;
     protected String fPackageName;
     protected String fPackageFolder;
     protected String fParserPackage;
     protected String fClassNamePrefix;
 
     
     protected String fGrammarFileName;
     protected String fLexerFileName;
     protected String fKwlexerFileName;
     protected String fControllerFileName;
     protected String fLocatorFileName;
     
     public NewLPGGrammarWithParserWrapperWizard() {
 		super();
 		setNeedsProgressMonitor(true);
     }
 
     public void addPages() {
     	addPages(new ExtensionPointWizardPage[] { new NewLPGGrammarWithParserWrapperWizardPage(this) });
     }
 
     private final static List/*<String pluginID>*/ dependencies= new ArrayList();
 
     static {
 		dependencies.add(RuntimePlugin.UIDE_RUNTIME);
 		dependencies.add("org.eclipse.core.runtime");
 		dependencies.add("org.eclipse.core.resources");
 		dependencies.add("org.eclipse.uide.runtime");
 		dependencies.add("lpg.runtime");
     }
 
     protected List getPluginDependencies() {
 	return dependencies;
     }
 
     protected void collectCodeParms() {
     	NewLPGGrammarWithParserWrapperWizardPage page= (NewLPGGrammarWithParserWrapperWizardPage) pages[0];
         
         fProject= page.getProject();
         fGrammarOptions= page.fGrammarOptions;
         fGrammarOptions.setLanguageName(page.getValue("language"));
         fGrammarOptions.setProjectName(fProject.getName());
         String qualifiedClassName = page.getValue("class");
         fGrammarOptions.setPackageName(qualifiedClassName.substring(0, qualifiedClassName.lastIndexOf('.')));
 
 	    fPackageName= fGrammarOptions.getPackageName();
 	    fLanguageName= fGrammarOptions.getLanguageName();
         fPackageFolder= fPackageName.replace('.', File.separatorChar);
         
         // SMS 13 Apr 2007 moving this here from generateCodeStubs
         // so that the information is available for advising the user
         // before stubs are generated
         fGrammarOptions.setClassNamePrefix(Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1));
         fClassNamePrefix = fGrammarOptions.getClassNamePrefix();
 
         fGrammarFileName= fClassNamePrefix + "Parser.g";
 	    fLexerFileName= fClassNamePrefix + "Lexer.gi";
 	    fKwlexerFileName= fClassNamePrefix + "KWLexer.gi";
         fControllerFileName = fGrammarOptions.getDefaultSimpleNameForParseController(fLanguageName) + ".java";	//fClassNamePrefix + "ParseController.java";
 		fLocatorFileName = fGrammarOptions.getDefaultSimpleNameForNodeLocator(fLanguageName) + ".java";			// fClassNamePrefix + "ASTNodeLocator.java";
     }
 
     	
     protected void generateCodeStubs(IProgressMonitor monitor) throws CoreException {
 		boolean hasKeywords= fGrammarOptions.getHasKeywords();
 		boolean requiresBacktracking= fGrammarOptions.getRequiresBacktracking();
 		boolean autoGenerateASTs= fGrammarOptions.getAutoGenerateASTs();
 		String templateKind= fGrammarOptions.getTemplateKind();
 
         String parserTemplateName= templateKind + (requiresBacktracking ? "/bt" : "/dt") + "ParserTemplate.gi";
 	    String lexerTemplateName= templateKind + "/LexerTemplate.gi";
 	    String kwLexerTemplateName= templateKind + "/KeywordTemplate.gi";
 	    String parseCtlrTemplateName= "ParseController.java";
 		String locatorTemplateName = "ASTNodeLocator.java";
 
 		IFile parseControllerFile = createParseController(fControllerFileName, parseCtlrTemplateName, hasKeywords, fProject, monitor);
 		IFile nodeLocatorFile = createNodeLocator(fLocatorFileName, locatorTemplateName, fProject, monitor);
 		
         ExtensionPointEnabler.enable(fProject, "org.eclipse.uide.runtime", "parser", new String[][] {
                 { "extension:id", fProject.getName() + ".parserWrapper" },
                 { "extension:name", fLanguageName + " Parser Wrapper" },
                { "parserWrapper:class", fPackageName + "." + fClassNamePrefix },
                 { "parserWrapper:language", fLanguageName }
         		}, 	
         		false, new NullProgressMonitor());
 		
 		editFile(monitor, parseControllerFile);
 		editFile(monitor, nodeLocatorFile);
         
 		IFile lexerFile = createLexer(fLexerFileName, lexerTemplateName, hasKeywords, fProject, monitor);
 		editFile(monitor, lexerFile);
         if (hasKeywords) {
             IFile kwLexerFile = createKWLexer(fKwlexerFileName, kwLexerTemplateName, hasKeywords, fProject, monitor);
             editFile(monitor, kwLexerFile);
         }
 		IFile grammarFile= createGrammar(fGrammarFileName, parserTemplateName, autoGenerateASTs, fProject, monitor);
 		editFile(monitor, grammarFile);
 		
 		enableBuilders(monitor, fProject, new String[] { JikesPGBuilder.BUILDER_ID });
     }
 
     static final String astDirectory= "Ast";
 
     static final String astNode= "ASTNode";
 
     static final String sAutoGenTemplate= "%options parent_saved,automatic_ast=toplevel,visitor=preorder,ast_directory=./" + astDirectory
 	    + ",ast_type=" + astNode;
 
     static final String sKeywordTemplate= "%options filter=kwTemplate.gi";
 
     private IFile createGrammar(String fileName, String templateName,
 	    boolean autoGenerateASTs, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map subs= getStandardSubstitutions();
 	
 		subs.put("$AUTO_GENERATE$", autoGenerateASTs ? sAutoGenTemplate : "");
 		subs.put("$TEMPLATE$", templateName);
 	
 		String grammarTemplateFileName = "grammar.g";
 		return createFileFromTemplate(fileName, grammarTemplateFileName, fPackageFolder, subs, project, monitor);
     }
 
     private IFile createLexer(String fileName, String templateName,
 	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map subs= getStandardSubstitutions();
 	
 		subs.put("$TEMPLATE$", templateName);
 		subs.put("$KEYWORD_FILTER$",
 			hasKeywords ? ("%options filter=" + fClassNamePrefix + "KWLexer.gi") : "");
 		subs.put("$KEYWORD_LEXER$", hasKeywords ? ("$" + fClassNamePrefix + "KWLexer") : "Object");
 		subs.put("$LEXER_MAP$", (hasKeywords ? "LexerBasicMap" : "LexerVeryBasicMap"));
 	
 		String lexerTemplateName = "lexer.gi";
 		return createFileFromTemplate(fileName, lexerTemplateName, fPackageFolder, subs, project, monitor);
     }
 
     private IFile createKWLexer(String fileName, String templateName,
 	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException
     {
 		Map subs= getStandardSubstitutions();
 		subs.put("$TEMPLATE$", templateName);
 	
 		String kwLexerTemplateName = "kwlexer.gi";
 		return createFileFromTemplate(fileName, kwLexerTemplateName, fPackageFolder, subs, project, monitor);
     }
 
     private IFile createParseController(
     	String fileName, String templateName, boolean hasKeywords, IProject project, IProgressMonitor monitor)
     	throws CoreException
     {
 		Map subs= getStandardSubstitutions();
 	
 		subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
 		subs.put("$AST_NODE$", astNode);
 		subs.put("$PARSER_TYPE$", fClassNamePrefix + "Parser");
 		subs.put("$LEXER_TYPE$", fClassNamePrefix + "Lexer");
 		
 		return createFileFromTemplate(fileName, templateName, fPackageFolder, subs, project, monitor);
     }
 
     	
 	// SMS 29 Sep 2006
     // All this node locator stuff, to provide one that has the same AST node type
     // as is generated for the parser by the other parts of this wizard (and unlike
     // the node type assumed in org.eclipse.uide.parser)
     
 	
     private IFile createNodeLocator(
 		String fileName, String templateName, IProject project, IProgressMonitor monitor) throws CoreException
     {
     	Map subs= getStandardSubstitutions();
 
     	subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
     	subs.put("$AST_NODE$", astNode);
     	subs.put("$PARSER_TYPE$", fClassNamePrefix + "Parser");
     	subs.put("$LEXER_TYPE$", fClassNamePrefix + "Lexer");
 
     	return createFileFromTemplate(fileName, templateName, fPackageFolder, subs, project, monitor);
 	}
 
     
     protected String getTemplateBundleID() {
         return JikesPGPlugin.kPluginID;
     }
 
     /**
      * We will accept the selection in the workbench to see if
      * we can initialize from it.
      * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
      */
     public void init(IWorkbench workbench, IStructuredSelection selection) {
         // this.selection = selection;
     }
 
     protected Map getStandardSubstitutions() {
         Map result= new HashMap();
         result.put("$LANG_NAME$", fLanguageName);
         result.put("$CLASS_NAME_PREFIX$", fClassNamePrefix);
         result.put("$PACKAGE_NAME$", fPackageName);
         return result;
     }
     
     
     
     
     private String fFileNamePrefix = null;
     
     private void setFileNamePrefix() {
     	String projectLocation = fProject.getLocation().toString();
     	fFileNamePrefix = projectLocation + '/' +   getProjectSourceLocation() + fPackageName.replace('.', '/') + '/';
     }
     
     private String getFileNamePrefix() {
     	if (fFileNamePrefix == null) {
     		setFileNamePrefix();
     	}
     	return fFileNamePrefix;
     }
     
     
     /**
      * Return the names of any existing files that would be clobbered by the
      * new files to be generated.
      * 
      * @return	An array of names of existing files that would be clobbered by
      * 			the new files to be generated
      */
     protected String[] getFilesThatCouldBeClobbered() {
     	String prefix = getFileNamePrefix();
     	List<String> fileNames = new ArrayList();
     	
     	if (fGrammarOptions.getGenGrammarFiles()) {
 			fileNames.add(prefix + fGrammarFileName);
 		    fileNames.add(prefix + fLexerFileName);
 		    fileNames.add(prefix + fKwlexerFileName);
     	}
     	
 	    fileNames.add(prefix + fControllerFileName);
 	    fileNames.add(prefix + fLocatorFileName);
   	
     	String[] result = fileNames.toArray(new String[0]);
     	return result;
     }
     
     
     
     /**
      * Post a dialog advising the user that finishing the wizard
      * could lead to the clobbering of previously auto-generated AST
      * classes.  If the user is OK  with that, continue by calling
      * oktoClobberFiles in the parent with the list of specific files
      * that might be clobbered and return the results of that.
      * 
      * Current implementation expects that the file names provided will
      * be the full absolute path names in the file system.
      * 
      * @param files		The names of files that would be clobbered by
      * 					files to be generated
      * @return			True if the user presses OK in response to
      * 					all dialogs involved in the check; false
      *                  if the user presses CANCEL for any of them
      *                  (presumably)
      */
     protected boolean okToClobberFiles(String[] files)
     {	
         // Safety check to warn the user about the possibility of clobbering existing
         // auto-generated AST classes (that may have been modified)
     	String astDir = getFileNamePrefix() + astDirectory;
     	final File file= new File(astDir);
     	
     	if (fGrammarOptions.getAutoGenerateASTs() && file.exists()) {
     		String message = "Any previously auto-generated AST classes will be overwritten; proceed?";
         	Shell parent = this.getShell();
         	MessageBox messageBox = new MessageBox(parent, (SWT.CANCEL | SWT.OK));
         	messageBox.setMessage(message);
         	int result = messageBox.open();
         	if (result == SWT.CANCEL)
         		return false;
     	}
 
     	return super.okToClobberFiles(files);
     }
     	
     
     // Copied from GeneratedComponentWizard
     /**
      * This method is called when 'Finish' button is pressed in the wizard.
      * We will create an operation and run it using wizard as execution context.
      * 
      * This method is quite a bit simpler than the corresponding method for
      * ExtensionPointWizard since no extensions have to be created here.
      */
     public boolean performFinish()
     {
     	// Do this in the UI thread while the wizard fields are still accessible
     	collectCodeParms();
 
 		// Invoke after collectCodeParms() so that collectCodeParms()
 		// can collect the names of files from the wizard
     	if (!okToClobberFiles(getFilesThatCouldBeClobbered()))
     		return false;
     	// Do we need to do just this in a runnable?  Evidently not.
     	try {
     		generateCodeStubs(new NullProgressMonitor());
     	} catch (Exception e){
 		    ErrorHandler.reportError("NewLPGGrammarWith	ParserWrapperWizard.performFinish:  Could not generate code stubs", e);
 		    return false;
     	}
     			
 		return true;
     }
     
 }
