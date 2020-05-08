 package org.dawnsci.python.rpc.action;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IParameterValues;
 import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
 import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
 import org.eclipse.dawnsci.macro.api.IMacroService;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleConstants;
 import org.eclipse.ui.console.IConsoleView;
 import org.python.pydev.core.IPythonNature;
 import org.python.pydev.debug.newconsole.PydevConsole;
 import org.python.pydev.debug.newconsole.PydevConsoleConstants;
 import org.python.pydev.debug.newconsole.PydevConsoleFactory;
 import org.python.pydev.debug.newconsole.PydevConsoleInterpreter;
 import org.python.pydev.debug.newconsole.env.PydevIProcessFactory;
 import org.python.pydev.debug.newconsole.env.PydevIProcessFactory.PydevConsoleLaunchInfo;
 import org.python.pydev.debug.newconsole.env.UserCanceledException;
 import org.python.pydev.debug.newconsole.prefs.InteractiveConsolePrefs;
 import org.python.pydev.shared_interactive_console.console.ui.ScriptConsole;
 import org.python.pydev.shared_interactive_console.console.ui.internal.ScriptConsoleViewer;
 import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class InjectPyDevConsole {
 	
 	// OSGi Injection
 	private static IMacroService mservice;
 	public static void setMacroService(IMacroService s) {
 		mservice = s;
 	}
 	public InjectPyDevConsole() {
 		
 	}
 	// End OSGi Injection
 
 	private static Logger logger = LoggerFactory.getLogger(InjectPyDevConsoleHandler.class);
 
 	/**
 	 * Command ID (as defined in plugin.xml)
 	 */
 	public static String COMMAND_ID = "uk.ac.diamond.scisoft.analysis.rcp.plotting.actions.injectPyDevConsole";
 	/**
 	 * The parameter key for the ExecutionEvent that specifies whether to create a new console or reuse an existing one
 	 * if possible, value should be "true" or "false". Optional, default false.
 	 */
 	public static String CREATE_NEW_CONSOLE_PARAM = COMMAND_ID + ".createNewConsoleAlways";
 	/**
 	 * The parameter key for the ExecutionEvent that specifies the name of the view to link. Optional. If unspecified
 	 * default plot is left.
 	 */
 	public static String VIEW_NAME_PARAM = COMMAND_ID + ".viewName";
 	/**
 	 * The parameter key for the ExecutionEvent that specifies "always", "never" or "newonly" as to whether to setup
 	 * scisoftpy. Optional, default is "newonly".
 	 * <p>
 	 * e.g.
 	 * 
 	 * <pre>
 	 * import scisoftpy as dnp
 	 * </pre>
 	 */
 	public static String SETUP_SCISOFTPY_PARAM = COMMAND_ID + ".addScisoftPySetup";
 	/**
 	 * Commands to be injected.
 	 */
 	public static String INJECT_COMMANDS_PARAM = COMMAND_ID + ".commandToInject";
 
 	
 	private Map<String, String> params;
 	
 	public InjectPyDevConsole(Map<String,String> params) {	
 		this.params = params;
 	}
 	
 	public void open(boolean useExisting) throws ExecutionException {
 
 		ScriptConsole console = null;
 		if (useExisting) {
 			console = getActiveScriptConsole(PydevConsoleConstants.CONSOLE_TYPE);
 		}
 		if (console==null && !Boolean.parseBoolean(params.get(CREATE_NEW_CONSOLE_PARAM))) {
 			console = getActiveScriptConsole(PydevConsoleConstants.CONSOLE_TYPE);
 		}
 
 		try {
 			String cmd  = createPythonCommands(console == null, params);
 						
 			InterpreterInfo        info = null;
 			PydevConsoleFactory     pcf = null;
 			PydevConsoleInterpreter pci = null;
 			
 			if (console == null) {
 				pcf = new PydevConsoleFactory();
 				pci = getConsole();
 				if (pci == null) return;
 				info = (InterpreterInfo)pci.getInterpreterInfo();
 			} else {
 				info = (InterpreterInfo)((PydevConsole)console).getInterpreterInfo();
 			}
 
 			if (data!=null) {
 				if (info.executableOrJar!=null && info.executableOrJar.toLowerCase().contains("python")) {
 				    cmd = cmd.concat("import numpy\n");
 				}
 			}
 
	        String flat = data != null ? createFlattenCommands(data, info.getInterpreterType()) : null;
             if (flat!=null) cmd = cmd.concat(flat);
             
             if (console == null) {
 				pcf.createConsole(pci, cmd);
 				
 			} else {                
 				sendCommand(cmd, console);
 			}
 			
 		} catch (UserCanceledException e) {
 			logger.error("Operation canceled", e);
 			throw new ExecutionException("Operation canceled", e);
 		} catch (Exception e) {
 			logger.error("Cannot open console", e);
 			throw new ExecutionException("Cannot open console", e);
 		}
 
 		return;
 	}
 
 	private String createFlattenCommands(Map<String, IDataset> d, int interpreterType) {
 		
		AbstractMacroGenerator<Map<String, IDataset>> gen = mservice.getGenerator(d.getClass());
 		return interpreterType==0 ? gen.getPythonCommand(d) : gen.getJythonCommand(d);
 	}
 
 	private Map<String, IDataset> data;
 	/**
 	 * If there is a running console, then the variable are flattened and sent to it,
 	 * if there is no console the variables are assigned and then if a console is started
 	 * their commands are appended as needed.
 	 * 
 	 * @param varName
 	 * @param data
 	 * @return true if inject happened.
 	 */
 	public boolean inject(Map<String,IDataset> curData) throws Exception {
 		
 		PydevConsole console = (PydevConsole)getActiveScriptConsole(PydevConsoleConstants.CONSOLE_TYPE);
 		
 		if (console==null || console.getDocument()==null) {
 			data = curData;
 			
 		} else {
 			this.data    = null;
             String cmds  = createFlattenCommands(curData, ((InterpreterInfo)console.getInterpreterInfo()).getInterpreterType());
 			if (cmds!=null) sendCommand(cmds, console);
 		}
 		
 		
 		return false;
 	}
 
 	public boolean isConsoleAvailable() {
 		ScriptConsole console = getActiveScriptConsole(PydevConsoleConstants.CONSOLE_TYPE);
 		return console!=null;
 	}
 
 	private void sendCommand(String cmd, ScriptConsole console) throws BadLocationException {
 		
 		PydevConsole pydevConsole = (PydevConsole) console;
 		IDocument document = pydevConsole.getDocument();
 
 		// Done because they can open the scripting elsewhere and then run the action
 		// to send data. Therefore there is a chance "import numpy" was not done.
 		if (!document.get().contains("import numpy") && cmd.contains("numpy")) {
 			cmd = "import numpy\n"+cmd;
 		}
 		if (cmd != null) {
 			if (!cmd.endsWith("\n")) cmd = cmd+"\n";
 			document.replace(document.getLength(), 0, cmd);
 		}
 
 		if (InteractiveConsolePrefs.getFocusConsoleOnSendCommand()) {
 			ScriptConsoleViewer viewer = pydevConsole.getViewer();
 			if (viewer != null) {
 
 				StyledText textWidget = viewer.getTextWidget();
 				if (textWidget != null) textWidget.setFocus();
 
 			}
 		}
 	}
 
 	private  String createPythonCommands(boolean newConsole, Map<String,String> params) {
 		StringBuffer cmds = new StringBuffer();
 		SetupScisoftpy setup = SetupScisoftpy.valueOfIgnoreCase(params.get(SETUP_SCISOFTPY_PARAM));
 		if (setup.setupScisoftpy(newConsole)) {
 			cmds.append("# Importing scisoftpy.\n");
 			cmds.append("import scisoftpy as dnp\n");
 		}
 		if (params.get(VIEW_NAME_PARAM) != null && !"".equals(params.get(VIEW_NAME_PARAM))) {
 			String viewName = params.get(VIEW_NAME_PARAM);
 			cmds.append("# Connecting to plot '" + viewName + "'.\n");
 			cmds.append("dnp.plot.setdefname('" + viewName + "')\n");
 		}
 		if (params.get(INJECT_COMMANDS_PARAM) != null && !"".equals(params.get(INJECT_COMMANDS_PARAM))) {
 			cmds.append(params.get(INJECT_COMMANDS_PARAM));
 		}
 		
 		return cmds.toString();
 	}
 
 	private  PydevConsoleInterpreter getConsole() throws Exception {
 
 		PydevIProcessFactory iprocessFactory = new PydevIProcessFactory();
 
 		// Shows GUI - NOTE Change here to always link into Jython without showing dialog.
 		PydevConsoleLaunchInfo createInteractiveLaunch = null;
 		try {
 			createInteractiveLaunch = iprocessFactory.createInteractiveLaunch();
 		} catch (UserCanceledException e) {
 			throw new Exception("Interpreter creation canceled", e);
 		}
 
 		if (createInteractiveLaunch == null) {
 			return null;
 		}
 
 		List<IPythonNature> naturesUsed = iprocessFactory.getNaturesUsed();
 		return PydevConsoleFactory.createPydevInterpreter(createInteractiveLaunch, naturesUsed);
 	}
 
 	/**
 	 * Code borrowed from EvaluateActionSetter in com.python.pydev
 	 * 
 	 * @param consoleType
 	 *            the console type we're searching for
 	 * @return the currently active console.
 	 */
 	private  ScriptConsole getActiveScriptConsole(String consoleType) {
 		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (window != null) {
 			IWorkbenchPage page = window.getActivePage();
 			if (page != null) {
 
 				List<IViewPart> consoleParts = getConsoleParts(page, false);
 				if (consoleParts.size() == 0) {
 					consoleParts = getConsoleParts(page, true);
 				}
 
 				if (consoleParts.size() > 0) {
 					IConsoleView view = null;
 					long lastChangeMillis = Long.MIN_VALUE;
 
 					if (consoleParts.size() == 1) {
 						view = (IConsoleView) consoleParts.get(0);
 					} else {
 						// more than 1 view available
 						for (int i = 0; i < consoleParts.size(); i++) {
 							IConsoleView temp = (IConsoleView) consoleParts.get(i);
 							IConsole console = temp.getConsole();
 							if (console instanceof PydevConsole) {
 								PydevConsole tempConsole = (PydevConsole) console;
 								ScriptConsoleViewer viewer = tempConsole.getViewer();
 
 								long tempLastChangeMillis = viewer.getLastChangeMillis();
 								if (tempLastChangeMillis > lastChangeMillis) {
 									lastChangeMillis = tempLastChangeMillis;
 									view = temp;
 								}
 							}
 						}
 					}
 
 					if (view != null) {
 						IConsole console = view.getConsole();
 
 						if (console instanceof ScriptConsole && console.getType().equals(consoleType)) {
 							return (ScriptConsole) console;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Code borrowed from EvaluateActionSetter in com.python.pydev
 	 * 
 	 * @param page
 	 *            the page where the console view is
 	 * @param restore
 	 *            whether we should try to restore it
 	 * @return a list with the parts containing the console
 	 */
 	private  List<IViewPart> getConsoleParts(IWorkbenchPage page, boolean restore) {
 		List<IViewPart> consoleParts = new ArrayList<IViewPart>();
 
 		IViewReference[] viewReferences = page.getViewReferences();
 		for (IViewReference ref : viewReferences) {
 			if (ref.getId().equals(IConsoleConstants.ID_CONSOLE_VIEW)) {
 				IViewPart part = ref.getView(restore);
 				if (part != null) {
 					consoleParts.add(part);
 					if (restore) {
 						return consoleParts;
 					}
 				}
 			}
 		}
 		return consoleParts;
 	}
 
 	public static enum SetupScisoftpy {
 		ALWAYS("Yes") {
 			@Override
 			public boolean setupScisoftpy(boolean isNewConsole) {
 				return true;
 			}
 		},
 		NEVER("No") {
 			@Override
 			public boolean setupScisoftpy(boolean isNewConsole) {
 				return false;
 			}
 		},
 		NEWONLY("Only if a new console is created") {
 			@Override
 			public boolean setupScisoftpy(boolean isNewConsole) {
 				return isNewConsole;
 			}
 		};
 
 		private final String display;
 
 		SetupScisoftpy(String display) {
 			this.display = display;
 		}
 
 		public String getDisplay() {
 			return display;
 		}
 
 		public static SetupScisoftpy valueOfIgnoreCase(String value) {
 			for (SetupScisoftpy s : SetupScisoftpy.values()) {
 				if (s.toString().equalsIgnoreCase(value)) {
 					return s;
 				}
 			}
 			// return default
 			return NEWONLY;
 		}
 
 		public abstract boolean setupScisoftpy(boolean isNewConsole);
 
 	}
 
 	public static class SetupSciSoftPyParameterValues implements IParameterValues {
 
 		@Override
 		public Map<String, String> getParameterValues() {
 			Map<String, String> values = new HashMap<String, String>();
 			for (SetupScisoftpy s : SetupScisoftpy.values()) {
 				values.put(s.getDisplay(), s.toString());
 			}
 			return values;
 		}
 
 	}
 
 
 	
 	public static String getLegalVarName(String setName) {
 		return getLegalVarName(setName, null);
 	}
 	
 	/**
 	 * Attempts to generate legal variable name. Does not take into account key words.
 	 * @param setName
 	 * @return
 	 * @throws Exception 
 	 */
 	public static String getLegalVarName(String setName, final Collection<String> names) {
 		
 		if (setName.endsWith("/"))   setName = setName.substring(0,setName.length()-1);
 		if (setName.indexOf('/')>-1) setName = setName.substring(setName.lastIndexOf('/'));
 		
 		setName = setName.replaceAll(" ", "_");
 		setName = setName.replaceAll("[^a-zA-Z0-9_]", "");
 		final Matcher matcher = Pattern.compile("(\\d+)(.+)").matcher(setName);
 		if (matcher.matches()) {
 			setName = matcher.group(2);
 		}
 		
 		if (Pattern.compile("(\\d+)").matcher(setName).matches()) {
 			return "data"+setName;
 		}
 		
 		if (names!=null) if (names.contains(setName)) {
 			int i = 1;
 			while(names.contains(setName+i)) i++;
 			setName = setName+i;
 		}
 		
 		return setName;
 	}
 
 
 }
