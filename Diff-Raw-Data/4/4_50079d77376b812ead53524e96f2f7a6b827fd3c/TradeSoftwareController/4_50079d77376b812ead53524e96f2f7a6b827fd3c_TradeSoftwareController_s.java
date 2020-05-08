 package com.ghlh.tradeway.software;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.eaio.nativecall.IntCall;
 import com.eaio.nativecall.NativeCall;
 import com.ghlh.conf.ConfigurationAccessor;
 import com.ghlh.util.TimeUtil;
 
 public class TradeSoftwareController {
 	public final static int CMD_PAUSE_INTERVAL = 500;
 	public final static int ACTION_PAUSE_INTERVAL = 500;
 
 	private IntCall exec;
	private static TradeSoftwareController instance = new TradeSoftwareController();
 	private static Logger logger = Logger
 			.getLogger(TradeSoftwareController.class);
 
 	public static TradeSoftwareController getInstance() {
 		return instance;
 	}
 
 	public TradeSoftwareController() {
 		try {
 			NativeCall.init();
 			init();
 		} catch (Throwable t) {
 			logger.error("Load hotkey throw:", t);
 			t.printStackTrace();
 		}
 	}
 
 	private void init() {
 		IntCall textdll = new IntCall("AutoHotkey.dll", "ahktextdll");
 		textdll.executeCall(new Object[] { "", "", "" });
 		exec = new IntCall("AutoHotkey.dll", "ahkExec");
 	}
 
 	public void activateTradeSoft() {
 		String cmdName = "ActivateTradeSoftwareWindow";
 		executeTradeSoftwareCMD(cmdName, new HashMap<String, Object>());
 	}
 
 	public void executeTradeCMD(String cmdName,
 			Map<String, Object> cmdParameters) {
 		executeTradeSoftwareCMD(cmdName, cmdParameters);
 	}
 
 	private synchronized void executeTradeSoftwareCMD(String cmdName,
 			Map<String, Object> cmdParameters) {
 		if (exec == null) {
 			return;
 		}
 		List<String> scripts = (List<String>) ControllScriptReader
 				.getInstance().getCMDScripts(cmdName);
 		for (int i = 0; i < scripts.size(); i++) {
 			String cmd = (String) scripts.get(i);
 			if (cmd.indexOf("price") > 0 && cmdParameters.get("price") == null) {
 				continue;
 			}
 			if (cmd.indexOf("%") > 0) {
 				String parameterName = cmd.substring(cmd.indexOf("%") + 1);
 				parameterName = parameterName.substring(0,
 						parameterName.indexOf("%"));
 				String parameter = cmdParameters.get(parameterName).toString();
 				cmd = cmd.replace("%" + parameterName + "%", parameter);
 			}
 			exec.executeCall(cmd);
 			TimeUtil.pause(CMD_PAUSE_INTERVAL);
 		}
 		TimeUtil.pause(ACTION_PAUSE_INTERVAL);
 	}
 
 	public static void main(String[] args) {
 		new TradeSoftwareController().activateTradeSoft();
 	}
 
 }
