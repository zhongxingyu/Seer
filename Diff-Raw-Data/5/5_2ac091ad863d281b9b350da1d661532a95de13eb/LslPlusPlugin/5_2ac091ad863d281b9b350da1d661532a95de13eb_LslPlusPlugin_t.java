 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package lslplus;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import lslplus.decorators.ErrorDecorator;
 import lslplus.editor.LslPartitionScanner;
 import lslplus.editor.lsl.LslCodeScanner;
 import lslplus.language_metadata.LslConstant;
 import lslplus.language_metadata.LslFunction;
 import lslplus.language_metadata.LslHandler;
 import lslplus.language_metadata.LslMetaData;
 import lslplus.language_metadata.LslParam;
 import lslplus.lsltest.TestManager;
 import lslplus.util.LslColorProvider;
 import lslplus.util.Util;
 import lslplus.util.Util.ArrayMapFunc;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.io.xml.DomDriver;
 
 /**
  * The class representing the LSL Plus plugin.
  * @author rgreayer
  *
  */
 public class LslPlusPlugin extends AbstractUIPlugin {
 	private static final Pattern LSLPLUS_CORE_VERSION_PAT = Pattern.compile("^0\\.1(\\..*)?$");
 	private static final String LSLPLUS_CORE_VERSION = "0.1.*";
 	private static final String LSL_EXECUTABLE = "LslPlus" + ((File.separatorChar == '\\') ? ".EXE" : "");  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
 	private static final String LSL_COMMAND = "LslPlus";
 	
     private static class ValidationResult {
         public boolean ok;
         public String msg;            
     }
     
     public static final boolean DEBUG = true;
 
     private static LslPlusPlugin instance;
 
     public final static String LSL_PARTITIONING = "__lsl_partitioning"; //$NON-NLS-1$
     
     public static final String PLUGIN_ID = "lslplus"; //$NON-NLS-1$
     public static Image createImage(String path) {
         ImageDescriptor descriptor = imageDescriptorFromPlugin(path);
         if (descriptor != null) return descriptor.createImage();
         return null;
     }
     /**
      * Returns the default plug-in instance.
      * 
      * @return the default plug-in instance
      */
     public static LslPlusPlugin getDefault() {
         return instance;
     }
     public static ImageDescriptor imageDescriptorFromPlugin(String path) {
         return imageDescriptorFromPlugin(PLUGIN_ID, path);
     }
     public static void openResource(Shell shell, final IFile resource) {
         final IWorkbenchPage activePage= 
             (PlatformUI.getWorkbench() != null &&
             PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) ?
             PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() : null;
             
         if (activePage != null) {
             final Display display= shell.getDisplay();
             if (display != null) {
                 display.asyncExec(new Runnable() {
                     public void run() {
                         try {
                             IDE.openEditor(activePage, resource, true);
                         } catch (PartInitException e) {
                             Util.log(e, e.getLocalizedMessage());
                         }
                     }
                 });
             }
         }
     }
     
     /**
      * Run executable with an input string.  Return a process so that the output can be monitored
      * as it is produced.
      * @param command the path to the executable
      * @param input the input string
      * @param redir an indicator as to whether stderr should be redirected to stdout
      * @return the process to monitor
      */
     public static Process runCommand(String command, String input, boolean redir) {
         try {
             Process process = launchCoreCommand(command, redir);
             if (process == null) return null;
 
             OutputStream out = process.getOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out);
 
             writer.write(input);
             writer.close();
             return process;
         } catch (IOException e) {
             Util.log(e, e.getMessage());
             return null;
         }
     }
 
     /**
      * Find and launch a command in the Lsl Plus Core.
      * @param command the command for the core to execute.
      * @param redir an indicator as to whether stderr should be redirected to stdout
      * @return the process to monitor
      */
     public static Process launchCoreCommand(String command, boolean redir) {
         try {
         	String exeName = null;
             File f = findExecutable(LSL_EXECUTABLE);
             if (f == null) {
                 Util.error("Can't find executable (" + LSL_EXECUTABLE + ")");
             	exeName = LSL_COMMAND;
            } else {
                exeName = f.getPath();
             }
             
             ProcessBuilder builder = new ProcessBuilder(new String[] { exeName, command });
             builder.redirectErrorStream(redir);
             Process process = builder.start();
 
             return process;
         } catch (IOException e) {
             Util.log(e, e.getLocalizedMessage());
             return null;
         }
     }
     
     private static File findExecutable(String executablePath) throws IOException {
         URL url = FileLocator.find(getDefault().getBundle(), new Path("$os$/" + executablePath), null); //$NON-NLS-1$
    
         if (url == null) {
             Util.error("no such executable: " + executablePath); //$NON-NLS-1$
             return null;
         }
 
         File f = new File(FileLocator.toFileURL(url).getFile());
 
         if (f == null) {
             Util.error("couldn't find executable: " + executablePath); //$NON-NLS-1$
             return null;
         }
         return f;
     }
     
     public static String runTask(String command, String input) {
         Process p = runCommand(command, input, true);
         
         if (p == null) return null;
         
         StringBuilder buf = new StringBuilder();
         InputStreamReader reader = new InputStreamReader(p.getInputStream());
         
         char[] chars = new char[512];
         int count = 0;
         
         try {
             while ((count = reader.read(chars)) >= 0) {
                 buf.append(chars, 0, count);
             }
             
             return buf.toString();
         } catch (IOException e) {
             Util.log(e,e.getLocalizedMessage());
             return null;
         } finally {
             try { 
                 reader.close();
             } catch (IOException e) {
                 Util.log(e, e.getLocalizedMessage());
             }
         }
     }
     
     static String validateExpression(String expression) {
         if (DEBUG) Util.log("expression: " + expression);
         String result = runTask("ExpressionHandler", expression); //$NON-NLS-1$
         if (DEBUG) Util.log("result: " + result);
         if (result == null) {
             return "Can't evaluate expression (internal error)";
         }
         XStream xstream = new XStream(new DomDriver());
         xstream.alias("result", ValidationResult.class); //$NON-NLS-1$
         ValidationResult e = (ValidationResult) xstream.fromXML(result);
         
         if (e.ok) return null;
         return e.msg;
     }
     
     private LslCodeScanner fCodeScanner;
 
     private LslColorProvider fColorProvider;
 
     private ErrorDecorator fErrorDecorator;
 
     private LslPartitionScanner fPartitionScanner;
 
     private LslMetaData lslMetaData = null;
 
     private TestManager testManager = null;
 
     private SimManager simManager = null;
 
     private static String[] statefulFunctions = null;
 
     /**
      * Creates a new plug-in instance.
      */
     public LslPlusPlugin() {
         instance = this;
         testManager = new TestManager();
         simManager  = new SimManager();
     }
 
     private LslMetaData buildMetaData() {
         String result = runTask("MetaData", ""); //$NON-NLS-1$//$NON-NLS-2$
         if (result == null) {
             Util.error(Messages.LslPlusPlugin_NO_META_DATA);
             return new LslMetaData();
         }
         if (DEBUG) Util.log("Meta-Data: " + result); //$NON-NLS-1$
         XStream xstream = new XStream(new DomDriver());
 
         xstream.alias("lslmeta", LslMetaData.class); //$NON-NLS-1$
         xstream.alias("handler", LslHandler.class); //$NON-NLS-1$
         xstream.alias("param", LslParam.class); //$NON-NLS-1$
         xstream.alias("function", LslFunction.class); //$NON-NLS-1$
         xstream.alias("constant", LslConstant.class); //$NON-NLS-1$
         LslMetaData md = null;
         try {
             md = (LslMetaData) xstream.fromXML(result);
         } catch (Exception e) {
             Util.log(e, Messages.LslPlusPlugin_COULD_NOT_DESERIALIZE_META_DATA);
             md = new LslMetaData();
         }
         return md;
     }
 
     public void errorStatusChanged() {
         getWorkbench().getDisplay().asyncExec(new Runnable() {
             public void run() {
                 if (fErrorDecorator != null) {
                     fErrorDecorator.errorStatusChanged();
                 }
             }
         });
     }
 
     /**
      * Returns the singleton LSL code scanner.
      * 
      * @return the singleton LSL code scanner
      */
     public RuleBasedScanner getLslCodeScanner() {
         if (fCodeScanner == null) {
             String[] handlerNames = (String[]) Util.arrayMap(new Util.ArrayMapFunc() {
                 public Class elementType() {
                     return String.class;
                 }
 
                 public Object map(Object o) {
                     return ((LslHandler) o).getName();
                 }
             }, getLslMetaData().getHandlers());
             String[] predefFuncs = (String[]) Util.arrayMap(new Util.ArrayMapFunc() {
                 public Class elementType() {
                     return String.class;
                 }
 
                 public Object map(Object o) {
                     return ((LslFunction) o).getName();
                 }
             }, getLslMetaData().getFunctions());
             String[] predefConsts = (String[]) Util.arrayMap(new Util.ArrayMapFunc() {
                 public Class elementType() {
                     return String.class;
                 }
 
                 public Object map(Object o) {
                     return ((LslConstant) o).getName();
                 }
             }, getLslMetaData().getConstants());
             fCodeScanner = new LslCodeScanner(getLslColorProvider(), handlerNames, predefFuncs,
                     predefConsts);
         }
         return fCodeScanner;
     }
 
     /**
      * Returns the singleton Java color provider.
      * 
      * @return the singleton Java color provider
      */
     public LslColorProvider getLslColorProvider() {
         if (fColorProvider == null)
             fColorProvider = new LslColorProvider();
         return fColorProvider;
     }
     
     public synchronized LslMetaData getLslMetaData() {
         if (lslMetaData == null) {
             lslMetaData = buildMetaData();
         }
         return lslMetaData;
     }
     
     /**
      * Return a scanner for creating LSL Plus partitions.
      * 
      * @return a scanner for creating Java partitions
      */
     public LslPartitionScanner getLslPartitionScanner() {
         if (fPartitionScanner == null)
             fPartitionScanner = new LslPartitionScanner();
         return fPartitionScanner;
     }
 
     public TestManager getTestManager() {
         return testManager;
     }
     
     public void setErrorDecorator(ErrorDecorator errorDecorator) {
         this.fErrorDecorator = errorDecorator;
     }
     public SimManager getSimManager() {
         return simManager;
     }
     public static synchronized String[] getStatefulFunctions() {
         if (LslPlusPlugin.statefulFunctions == null) {
             List funcs = Util.filtMap(new ArrayMapFunc() {
                 public Class elementType() { return String.class; }
                 public Object map(Object o) {
                     LslFunction f = (LslFunction) o;
                     return f.isStateless() ? null : f.getName();
                 }
             }, getLLFunctions());
             
             LslPlusPlugin.statefulFunctions = (String[]) funcs.toArray(new String[funcs.size()]);
         }
         
         return LslPlusPlugin.statefulFunctions;
     }
     public static LslFunction[] getLLFunctions() {
         return getDefault().getLslMetaData().getFunctions();
     }
 
     public void start(BundleContext context) throws Exception {
     	super.start(context);
     	final String version = runTask("Version", "");
     	
     	if (version == null) { // executable not found
 	    	Util.log("LslPlus core not found");
 	    	getWorkbench().getDisplay().asyncExec(new Runnable() {
 	    		public void run() {
 	    			MessageDialog dlg = new MessageDialog(
 	    					getWorkbench().getActiveWorkbenchWindow().getShell(),
 	    					"LSL Plus Core Not Found",
 	    					null,
 	    					"The LSLPlus native executable (version " + LSLPLUS_CORE_VERSION + ")\n" +
 	    					"was not found.\n\n" +
 	    					"The LSLPlus native executable is available from Hackage:\n" +
 	    					"http://hackage.haskell.org/cgi-bin/hackage-scripts/pacakge/LslPlus\n\n" +
 	    					"Please also see the Help documentation for LSL Plus, under 'Installation'",
 	    					MessageDialog.ERROR,
 	    					new String[] { "Ok" },
 	    					0);
 	    			dlg.open();
 	    		}
 	    	});
     	} else {
 	    	Util.log("LslPlus core version: " + version);
 	    	Matcher m = LSLPLUS_CORE_VERSION_PAT.matcher(version.trim());
 	    	if (!m.matches()) {
 		    	getWorkbench().getDisplay().asyncExec(new Runnable() {
 		    		public void run() {
 		    			MessageDialog dlg = new MessageDialog(
 		    					getWorkbench().getActiveWorkbenchWindow().getShell(),
 		    					"LSL Plus Core Version",
 		    					null,
 		    					"The version of the LSLPlus native executable (" + version.trim() + ")\n" +
 		    					"is incompatible with this version of the LSL Plus Eclipse plugin,\n" +
 		    					"which requires version " + LSLPLUS_CORE_VERSION + ".\n"  +
 		    					"The LSLPlus native executable is available from Hackage:\n" +
 		    					"http://hackage.haskell.org/cgi-bin/hackage-scripts/pacakge/LslPlus\n\n" +
 		    					"Please also see the Help documentation for LSL Plus, under 'Installation'",
 		    					MessageDialog.ERROR,
 		    					new String[] { "Ok" },
 		    					0);
 		    			dlg.open();
 		    		}
 		    	});
 	    	}
     	}
     }
 }
