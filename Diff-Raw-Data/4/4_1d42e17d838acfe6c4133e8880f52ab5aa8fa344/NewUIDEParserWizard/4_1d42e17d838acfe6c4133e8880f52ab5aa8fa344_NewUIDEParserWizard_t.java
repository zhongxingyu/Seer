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
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 import org.eclipse.uide.runtime.RuntimePlugin;
 import org.eclipse.uide.wizards.ExtensionPointWizard;
 import org.eclipse.uide.wizards.ExtensionPointWizardPage;
 import org.jikespg.uide.JikesPGPlugin;
 import org.jikespg.uide.builder.JikesPGBuilder;
 
 /**
  * This wizard creates a JikesPG grammar, a "parser" language service, and a
  * stub IParseController implementation for use with the UIDE.
  */
 public class NewUIDEParserWizard extends ExtensionPointWizard implements INewWizard {
     //	private NewUIDEParserWizardPage page;
     //	private ISelection selection;
 
     private IProject fProject;
     private GrammarOptions fGrammarOptions;
 
     protected String fLanguageName;
     protected String fPackageName;
     protected String fPackageFolder;
     protected String fParserPackage;
     protected String fClassName;
 
     public NewUIDEParserWizard() {
 	super();
 	setNeedsProgressMonitor(true);
     }
 
     public void addPages() {
 	addPages(new ExtensionPointWizardPage[] { new NewUIDEParserWizardPage(this), });
     }
 
     private final static List/*<String pluginID>*/ dependencies= new ArrayList();
 
     static {
 	dependencies.add(RuntimePlugin.UIDE_RUNTIME);
 	dependencies.add(JikesPGPlugin.kPluginID);
 	dependencies.add("org.eclipse.core.runtime");
 	dependencies.add("org.eclipse.core.resources");
 	dependencies.add("org.eclipse.uide.runtime");
 	dependencies.add("lpg");
     }
 
     protected List getPluginDependencies() {
 	return dependencies;
     }
 
     protected void collectCodeParms() {
         NewUIDEParserWizardPage page= (NewUIDEParserWizardPage) pages[0];
         
         fProject= page.getProject();
         fGrammarOptions= page.fGrammarOptions;
         fGrammarOptions.setLanguageName(page.getValue("language"));
         fGrammarOptions.setProjectName(fProject.getName());
         String className= page.getValue("class");
         fGrammarOptions.setPackageName(className.substring(0, className.lastIndexOf('.')));
 
 	fPackageName= fGrammarOptions.getPackageName();
 	fLanguageName= fGrammarOptions.getLanguageName();
         fPackageFolder= fPackageName.replace('.', File.separatorChar);
     }
 
     protected void generateCodeStubs(IProgressMonitor monitor) throws CoreException {
 	boolean hasKeywords= fGrammarOptions.getHasKeywords();
 	boolean requiresBacktracking= fGrammarOptions.getRequiresBacktracking();
 	boolean autoGenerateASTs= fGrammarOptions.getAutoGenerateASTs();
 	String templateKind= fGrammarOptions.getTemplateKind();
 
         String parserTemplateName= templateKind + (requiresBacktracking ? "/bt" : "/dt") + "ParserTemplate.gi";
 	String lexerTemplateName= templateKind + "/LexerTemplate.gi";
 	String kwLexerTemplateName= templateKind + "/KeywordTemplate.gi";
         String parseCtlrTemplateName= "ParseController.tmpl";
 
         String langClassName= Character.toUpperCase(fLanguageName.charAt(0)) + fLanguageName.substring(1);
 
         fClassName= langClassName;
 
         String grammarFileName= langClassName + "Parser.g";
 	String lexerFileName= langClassName + "Lexer.gi";
 	String kwlexerFileName= langClassName + "KWLexer.gi";
 	String controllerFileName= langClassName + "ParseController.java";
 
         // RMF 3/2/2006 - The following would probably be simpler if we just passed in the 
 	IFile grammarFile= createGrammar(grammarFileName, parserTemplateName, autoGenerateASTs, fProject,
 		monitor);
 
 	createLexer(lexerFileName, lexerTemplateName, hasKeywords, fProject, monitor);
         if (hasKeywords)
             createKWLexer(kwlexerFileName, kwLexerTemplateName, hasKeywords, fProject, monitor);
 	createParseController(controllerFileName, parseCtlrTemplateName, hasKeywords, fProject,
 		monitor);
 
 	editFile(monitor, grammarFile);
 	enableBuilders(monitor, fProject, new String[] { JikesPGBuilder.BUILDER_ID });
     }
 
     static final String astDirectory= "Ast";
 
     static final String astNode= "ASTNode";
 
     static final String sAutoGenTemplate= "%options automatic_ast=toplevel,visitor,ast_directory=./" + astDirectory
 	    + ",ast_type=" + astNode;
 
     static final String sKeywordTemplate= "%options filter=kwTemplate.gi";
 
     private IFile createGrammar(String fileName, String templateName,
 	    boolean autoGenerateASTs, IProject project, IProgressMonitor monitor) throws CoreException {
 
 	Map subs= getStandardSubstitutions();
 
 	subs.put("$AUTO_GENERATE$", autoGenerateASTs ? sAutoGenTemplate : "");
 	subs.put("$TEMPLATE$", templateName);
 
 	return createFileFromTemplate(fileName, "grammar.tmpl", fPackageFolder, subs, project, monitor);
     }
 
     private IFile createLexer(String fileName, String templateName,
 	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException {
 	Map subs= getStandardSubstitutions();
 
 	subs.put("$TEMPLATE$", templateName);
 	subs.put("$KEYWORD_FILTER$",
		hasKeywords ? ("%options filter=" + fClassName + "KWLexer.gi") : "");
	subs.put("$KEYWORD_LEXER$", hasKeywords ? ("$" + fClassName + "KWLexer") : "Object");
 	subs.put("$LEXER_MAP$", (hasKeywords ? "LexerBasicMap" : "LexerVeryBasicMap"));
 
 	return createFileFromTemplate(fileName, "lexer.tmpl", fPackageFolder, subs, project, monitor);
     }
 
     private IFile createKWLexer(String fileName, String templateName,
 	    boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException {
 	Map subs= getStandardSubstitutions();
 	subs.put("$TEMPLATE$", templateName);
 
 	return createFileFromTemplate(fileName, "kwlexer.tmpl", fPackageFolder, subs, project, monitor);
     }
 
     private IFile createParseController(String fileName, 
 	    String templateName, boolean hasKeywords, IProject project, IProgressMonitor monitor) throws CoreException {
 	Map subs= getStandardSubstitutions();
 
 	subs.put("$AST_PKG_NODE$", fPackageName + "." + astDirectory + "." + astNode);
 	subs.put("$AST_NODE$", astNode);
 	subs.put("$PARSER_TYPE$", fLanguageName + "Parser");
 	subs.put("$LEXER_TYPE$", fLanguageName + "Lexer");
 
 	return createFileFromTemplate(fileName, "ParseController.tmpl", fPackageFolder, subs, project, monitor);
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
         result.put("$CLASS_NAME_PREFIX$", fClassName);
         result.put("$PACKAGE_NAME$", fPackageName);
         return result;
     }
 }
