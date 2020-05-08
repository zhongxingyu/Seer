 package cc.warlock.script.wsl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.warlock.script.wsl.WarlockWSLScript.WarlockWSLCommand;
 
 public class WarlockWSLScriptLine {
 	private int lineNumber;
 	private WarlockWSLScript script;
 	private WarlockWSLScriptLine next;
 	private ArrayList<WarlockWSLScriptArg> args = new ArrayList<WarlockWSLScriptArg>();
 	
 	private Pattern commandPattern;
 	
 	public WarlockWSLScriptLine(WarlockWSLScript script, int lineNumber) {
 		this.script = script;
 		this.lineNumber = lineNumber;
 		commandPattern = Pattern.compile("^([\\w_]+)\\s+(.*)");
 	}
 	
 	public void execute(HashMap<String, String> variables, HashMap<String, WarlockWSLCommand> commands) {
 		StringBuffer buffer = new StringBuffer();
 		for(WarlockWSLScriptArg arg : args) {
 			buffer.append(arg.getString(variables));
 		}
 		
 		Matcher m = commandPattern.matcher(buffer.toString());
 		
 		String commandName = m.group(1).toLowerCase();
 		String args = m.group(2);
 		
 		WarlockWSLCommand command = commands.get(commandName);
 		if(command != null) command.execute(args);
 	}
 	
 	public int getLineNumber() {
 		return lineNumber;
 	}
 	
 	public void setNext(WarlockWSLScriptLine next) {
 		this.next = next;
 	}
 	
 	public WarlockWSLScriptLine getNext() {
 		return next;
 	}
 	
 	public void addArg(WarlockWSLScriptArg arg) {
 		args.add(arg);
 	}
 	
 	public void addArgs(ArrayList<WarlockWSLScriptArg> args) {
 		for(WarlockWSLScriptArg arg : args) {
 			addArg(arg);
 		}
 	}
 }
