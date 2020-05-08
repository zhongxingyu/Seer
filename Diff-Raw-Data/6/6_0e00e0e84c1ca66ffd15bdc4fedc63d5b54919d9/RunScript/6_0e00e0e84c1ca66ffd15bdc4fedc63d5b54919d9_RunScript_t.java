 package org.freemedsoftware.util.webharness;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 
 import javax.script.Invocable;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.RefreshHandler;
 import com.gargoylesoftware.htmlunit.WebClient;
 
 public class RunScript {
 
 	public static Logger log = null;
 
 	public static void main(String[] args) throws Exception {
 		URL log4j = RunScript.class.getClassLoader().getResource(
 				"log4j.properties");
 		PropertyConfigurator.configure(log4j);
 
 		log = Logger.getLogger(RunScript.class);
 
 		// Set logging for htmlunit
 		System.getProperties().put(
 				"org.apache.commons.logging.simplelog.defaultlog", "warn");
 
 		if (args.length < 1) {
 			throw new Exception("Need to specify script name");
 		}
 		String scriptName = args[0];
 
 		ScriptEngineManager engineMgr = new ScriptEngineManager();
 		ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
 
 		// Instantiate web client (htmlunit)
 		WebClient webClient = new WebClient(BrowserVersion.CHROME);
 		webClient.getOptions().setJavaScriptEnabled(true);

		// Fix AJAX responses
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());

 		webClient.setRefreshHandler(new RefreshHandler() {
 			@Override
 			public void handleRefresh(Page arg0, URL arg1, int arg2)
 					throws IOException {
 				log.debug("Attempted refresh to " + arg1.getPath());
 			}
 		});
 
 		// Inject objects
 		engine.put("webClient", webClient);
 		engine.put("log", log);
 
 		InputStream is = new FileInputStream(scriptName);
 		String out = null;
 		try {
 			// Evaluate plugin
 			Reader reader = new InputStreamReader(is);
 			engine.eval(reader);
 
 			Invocable invocableEngine = (Invocable) engine;
 			Object output = invocableEngine.invokeFunction("run");
 			if (output != null) {
 				if (output instanceof String) {
 					out = ((String) output);
 				} else {
 					out = output.toString();
 				}
 			} else {
 				out = new String("");
 			}
 		} catch (ScriptException ex) {
 			log.error(ex.getMessage());
 			out = new String("");
 		}
 
 		System.out.println(out);
 	}
 
 }
