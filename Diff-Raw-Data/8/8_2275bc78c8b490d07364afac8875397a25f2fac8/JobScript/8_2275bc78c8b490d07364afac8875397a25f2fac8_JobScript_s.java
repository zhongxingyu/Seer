 package jobmatch.presentation.script;
 
 import com.lutris.appserver.server.httpPresentation.*;
 import org.python.util.PythonInterpreter;
 import java.util.*;
 import org.w3c.dom.html.*;
 
 public class JobScript implements HttpPresentation {
 
     public void run(HttpPresentationComms comms) 
         throws HttpPresentationException {
 
 	final String action = comms.request.getParameter("action");
 
 	if (action != null && action.equals("execute")) {
 	    final String command = comms.request.getParameter("command");
 	    final String where = comms.request.getParameter("where");
 
 	    if (where != null && where.equals("new")) {
 		this.createNewInterpreter(comms);
 	    }
 	    if (command != null) {
 		this.execute(comms, command);
 	    }
 	}
 	
 	ScriptInterpreterHTML page = (ScriptInterpreterHTML)comms.xmlcFactory.create(ScriptInterpreterHTML.class);
 	
 	this.writeHistory(this.getHistory(comms), page);
 	this.writeDictionary(this.getInterpreter(comms), page);
         comms.response.writeHTML(page);
     }
 
     private void createNewInterpreter(HttpPresentationComms comms) {
 	try {
 	    PythonInterpreter interpreter = new PythonInterpreter();
 	    comms.sessionData.set("interpreter", interpreter);
 	    comms.sessionData.set("history", new ArrayList());
 	    this.initInterpreter(interpreter);
 	}  catch (Exception e) { throw new RuntimeException(e.toString()); }
     }
 
     protected void initInterpreter(PythonInterpreter interpreter) {
 	try {
	    interpreter.exec("import java, javax, org, com, jobmatch");
 	} catch (Exception e) { System.err.println(e.toString()); }
     }
 
     private PythonInterpreter getInterpreter(HttpPresentationComms comms) {
 	try {
 	    PythonInterpreter result = (PythonInterpreter) comms.sessionData.get("interpreter");
 	    if (result == null) {
 		this.createNewInterpreter(comms);
 		result = (PythonInterpreter) comms.sessionData.get("interpreter");
 	    }
 	    return result;
 	} catch (Exception e) { throw new RuntimeException(e.toString()); }
     }
 
     private List getHistory(HttpPresentationComms comms) {
 	try {
 	    List result = (List) comms.sessionData.get("history");
 	    if (result == null) {
 		this.createNewInterpreter(comms);
 		result = (List) comms.sessionData.get("history");
 	    }
 	    return result;
 	} catch (Exception e) { throw new RuntimeException(e.toString()); }
     }
 
     private void execute(HttpPresentationComms comms, String command) {
 	PythonInterpreter interpreter = this.getInterpreter(comms);
 	Object result = null;
 	try {
 	    result = interpreter.eval(command);
 	} catch (Exception e) {
 	    try {
 		interpreter.exec(command);
 		result = "-";
 	    } catch (Exception e2) {
 		result = e2.toString();
 	    }
 	} finally {
 	    if (result == null) {
 		result = "unknown";
 	    }
 	}
 	List history = this.getHistory(comms);
 	history.add(new HistoryEntry(command, result.toString()));
     }
 
     private void writeHistory(List history, ScriptInterpreterHTML page) {
 	HTMLElement container = page.getElementHistory();
 	HTMLElement item = page.getElementHistoryRow();
 	for (int i=history.size(); i > 0; i--) {
 	    HistoryEntry entry = (HistoryEntry) history.get(i-1);
 	    page.getElementCmndURL().setHref(this.commandHref(entry.command));
 	    page.setTextCommand(entry.command);
 	    page.setTextResult(entry.result);
 	    container.appendChild(item.cloneNode(true));
 	}
 	container.removeChild(item);
 	if (!history.isEmpty()) {
 	    page.setTextLastResult(((HistoryEntry) history.get(history.size()-1)).result.toString());
 	} else {
 	    HTMLElement lastDiv = page.getElementLastDiv();
 	    lastDiv.getParentNode().removeChild(lastDiv);
 	}
     }
 
     private String encodeCommand(String command) {
 	return java.net.URLEncoder.encode(command);
     }
 
     private String commandHref(String command) {
	return "./script/JobScript.po?action=execute&command=" + this.encodeCommand(command);
     }
 
     private void writeDictionary(PythonInterpreter interpreter, ScriptInterpreterHTML page) {
 	page.setTextDictionary(interpreter.eval("dir()").toString());
     }
 
     private class HistoryEntry {
 	public String command;
 	public String result;
 	public HistoryEntry(String command, String result) {
 	    this.command = command;
 	    this.result = result;
 	}
     }
     
}
