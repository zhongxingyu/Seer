 package cc.warlock.core.stormfront.script.wsl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Stack;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import cc.warlock.core.script.AbstractScript;
 import cc.warlock.core.script.IScriptEngine;
 import cc.warlock.core.script.IScriptInfo;
 import cc.warlock.core.script.IScriptListener;
 import cc.warlock.core.script.Match;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.TextMatch;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;
 import cc.warlock.core.stormfront.script.internal.StormFrontScriptCommands;
 
 public class WSLScript extends AbstractScript {
 	
 	protected boolean running, stopped;
 	protected HashMap<String, WSLScriptLine> labels = new HashMap<String, WSLScriptLine>();
 	protected WSLScriptLine nextLine;
 	protected WSLScriptLine curLine;
 	protected WSLScriptLine endLine;
 	protected HashMap<String, IWSLValue> variables = new HashMap<String, IWSLValue>();
 	protected Stack<WSLScriptLine> callstack = new Stack<WSLScriptLine>();
 	protected HashMap<String, WSLCommand> wslCommands = new HashMap<String, WSLCommand>();
 	protected int pauseLine;
 	protected Thread scriptThread;
 	private Pattern commandPattern = Pattern.compile("^([\\w_]+)(\\s+(.*))?");
 	private ScriptTimer timer = new ScriptTimer();
 	
 	protected WSLEngine engine;
 	protected IStormFrontScriptCommands commands;
 	protected IStormFrontClient client;
 	
 	private final Lock lock = new ReentrantLock();
 	private final Condition gotResume = lock.newCondition();
 	
 	private static final String argSeparator = "\\s+";
 	
 	public WSLScript (WSLEngine engine, IScriptInfo info, IStormFrontClient client)
 	{
 		super(info);
 		this.engine = engine;
 		this.client = client;
 		
 		commands = new StormFrontScriptCommands(client, this);
 		
 		// add command handlers
 		addCommand("put", new WSLPut());
 		addCommand("echo", new WSLEcho());
 		addCommand("pause", new WSLPause());
 		addCommand("shift", new WSLShift());
 		addCommand("save", new WSLSave());
 		addCommand("action", new WSLAction());
 		addCommand("counter", new WSLCounter());
 		addCommand("deletevariable", new WSLDeleteVariable());
 		addCommand("setvariable", new WSLSetVariable());
 		addCommand("goto", new WSLGoto());
 		addCommand("gosub", new WSLGosub());
 		addCommand("random", new WSLRandom());
 		addCommand("return", new WSLReturn());
 		addCommand("matchwait", new WSLMatchWait());
 		addCommand("matchre", new WSLMatchRe());
 		addCommand("match", new WSLMatch());
 		addCommand("waitforre", new WSLWaitForRe());
 		addCommand("waitfor", new WSLWaitFor());
 		addCommand("wait", new WSLWait());
 		addCommand("move", new WSLMove());
 		addCommand("nextroom", new WSLNextRoom());
 		addCommand("exit", new WSLExit());
 		addCommand("timer", new WSLTimer());
 		
 		for(int i = 1; i <= 9; i++) {
 			addCommand("if_" + i, new WSLIf_(String.valueOf(i)));
 		}
 		
 		setVariable("t", new WSLTime());
 		setVariable("mana", new WSLMana());
 		setVariable("health", new WSLHealth());
 		setVariable("fatigue", new WSLFatigue());
 		setVariable("spirit", new WSLSpirit());
 		setVariable("rt", new WSLRoundTime());
 		setVariable("lefthand", new WSLLeftHand());
 		setVariable("righthand", new WSLRightHand());
 		setVariable("spell", new WSLSpell());
 		setVariable("roomdesc", new WSLRoomDesc());
 		setVariable("roomtitle", new WSLRoomTitle());
 		
 		// TODO add roomexits
 	}
 
 	public IWSLValue getVariable(String name) {
 		return variables.get(name);
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 	
 	private class WSLTime extends WSLAbstractNumber {
 		public double toDouble() {
 			return timer.get();
 		}
 	}
 	
 	private class WSLMana extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getMana().get();
 		}
 	}
 	
 	private class WSLHealth extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getHealth().get();
 		}
 	}
 	
 	private class WSLFatigue extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getFatigue().get();
 		}
 	}
 	
 	private class WSLSpirit extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getSpirit().get();
 		}
 	}
 	
 	private class WSLRoundTime extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getRoundtime().get();
 		}
 	}
 	
 	private class WSLLeftHand extends WSLAbstractString {
 		public String toString() {
 			return client.getLeftHand().get();
 		}
 	}
 	
 	private class WSLRightHand extends WSLAbstractString {
 		public String toString() {
 			return client.getRightHand().get();
 		}
 	}
 	
 	private class WSLSpell extends WSLAbstractString {
 		public String toString() {
 			return client.getCurrentSpell().get();
 		}
 	}
 	
 	private class WSLRoomDesc extends WSLAbstractString {
 		public String toString() {
 			return client.getRoomDescription().get();
 		}
 	}
 	
 	private class WSLRoomTitle extends WSLAbstractString {
 		public String toString() {
 			return client.getStream(IStormFrontClient.ROOM_STREAM_NAME).getTitle().get();
 		}
 	}
 	
 	private class ScriptRunner  implements Runnable {
 		public void run() {
 			doStart();
 			
 			while(curLine != null && !stopped) {
 				waitForResume();
 				nextLine = curLine.getNext();
 				
 				String line = curLine.get();
 				if(line != null) {
 					execute(line);
 				}
 				
 				curLine = nextLine;
 				commands.clearInterrupt();
 			}
 			
 			if(!stopped)
 				stop();
 		}
 	}
 	
 	public void start (List<String> arguments)
 	{
 		for (int i = 0; i < arguments.size(); i++) {
 			setVariable(Integer.toString(i + 1), arguments.get(i));
 		}
 		
 		for (String varName : commands.getStormFrontClient().getServerSettings().getVariableNames())
 		{
 			setVariable(varName, commands.getStormFrontClient().getServerSettings().getVariable(varName));
 		}
 		
 		scriptThread = new Thread(new ScriptRunner());
 		scriptThread.setName("Wizard Script: " + getName());
 		scriptThread.start();
 		
 		for (IScriptListener listener : listeners) listener.scriptStarted(this);
 	}
 	
 	protected void doStart ()
 	{
 		try {
 			Reader scriptReader = info.openReader();
 			
 			CharStream input = new ANTLRReaderStream(scriptReader);
 			WSLLexer lex = new WSLLexer(input);
 			CommonTokenStream tokens = new CommonTokenStream(lex);
 			WSLParser parser = new WSLParser(tokens);
 
 			parser.setScript(this);
 
 			parser.script();
 		} catch(IOException e) {
 			e.printStackTrace();
 			return;
 		} catch (RecognitionException e) {
 			e.printStackTrace();
 			// TODO handle the exception
 		}
 
 		client.getDefaultStream().echo("[script started: " + getName() + "]\n");
 		running = true;
 		stopped = false;
 		commands.waitForRoundtime();
 	}
 	
 	private void waitForResume() {
 		while(!running && !stopped) {
 			lock.lock();
 			try {
 				gotResume.await();
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				lock.unlock();
 			}
 		}
 	}
 	
 	public void addLabel(String label, WSLScriptLine line) {
 		labels.put(label.toLowerCase(), line);
 	}
 	
 	public void addLine(WSLScriptLine line) {
 		if(curLine == null) {
 			curLine = line;
 		}
 		if(endLine != null) {
 			endLine.setNext(line);
 		}
 		endLine = line;
 	}
 	
 	public void execute(String line) {
 		Matcher m = commandPattern.matcher(line);
 		
 		if (!m.find()) {
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		String arguments = m.group(3);
 		if(arguments == null) arguments = "";
 		
 		WSLCommand command = wslCommands.get(commandName);
 		if(command != null)
 			command.execute(arguments);
 		else
 			//TODO output the line number here
 			commands.echo("Invalid command \"" + line + "\"");
 	}
 	
 	public void stop() {
 		running = false;
 		stopped = true;
 		commands.stop();
 		
 		client.getDefaultStream().echo("[script stopped: " + getName() + "]\n");
 		super.stop();
 	}
 
 	public void suspend() {
 		running = false;
 		
 		client.getDefaultStream().echo("[script paused: " + getName() + "]\n");
 		super.suspend();
 	}
 	
 	public void resume() {
 		running = true;
 		
 		client.getDefaultStream().echo("[script resumed: " + getName() + "]\n");
 
 		super.resume();
 		
 		lock.lock();
 		try {
 			gotResume.signalAll();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			lock.unlock();
 		}
 	}
 	
 	protected void addCommand (String name, WSLCommand command) {
 		wslCommands.put(name, command);
 	}
 	
 	protected void scriptError(String message) {
 		client.getDefaultStream().echo("Script error on line " + curLine.getLineNumber() + ": " + message + "\n");
 		stop();
 	}
 	
 	protected class ScriptTimer {
 		private long timerStart = -1L;
 		private long timePast = 0L;
 		
 		public long get() {
 			if(timerStart < 0) return timePast / 1000;
 			return (System.currentTimeMillis() - timerStart) / 1000;
 		}
 		
 		public void start() {
 			if(timerStart < 0)
 				timerStart = System.currentTimeMillis() - timePast;
 		}
 		
 		public void stop() {
 			if(timerStart >= 0) {
 				timePast = timerStart - System.currentTimeMillis();
 				timerStart = -1L;
 			}
 		}
 		
 		public void clear() {
 			timerStart = -1L;
 			timePast = 0L;
 		}
 	}
 	
 	abstract protected class WSLCommand {
 		
 		abstract public void execute(String arguments);
 		
 	}
 	
 	protected class WSLSave extends WSLCommand {
 		
 		public void execute(String arguments) {
 			setVariable("s", arguments);
 		}
 	}
 
 	protected class WSLShift extends WSLCommand {
 		
 		public void execute (String arguments) {
 			for (int i = 1; ; i++) {
 				if (!variables.containsKey(Integer.toString(i+1)))
 				{
 					deleteVariable(Integer.toString(i));
 					break;
 				}
 				else
 				{
 					String arg = variables.get(Integer.toString(i+1)).toString();
 					if (arg == null) {
 						deleteVariable(Integer.toString(i));
 						break;
 					}
 					setVariable(Integer.toString(i), arg);
 				}
 			}
 		}
 	}
 
 	protected class WSLCounter extends WSLCommand {
 		
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator);
 			if (args.length < 1) {
 				scriptError("Invalid arguments to counter");
 				return;
 			}
 			
 			int operand;
 			if (args.length > 1)
 				operand = Integer.parseInt(args[1]);
 			else
 				operand = 1;
 
 			String counterFunction = args[0];
 			int value = variables.containsKey("c") ?
 					Integer.parseInt(variables.get("c").toString()) : 0;
 
 			if ("set".equalsIgnoreCase(counterFunction))
 			{
 				setVariable("c", Integer.toString(operand));
 			}
 			else if ("add".equalsIgnoreCase(counterFunction))
 			{	
 				int newValue = value + operand;
 				setVariable("c", Integer.toString(newValue));
 			}
 			else if ("subtract".equalsIgnoreCase(counterFunction))
 			{
 				int newValue = value - operand;
 				setVariable("c", Integer.toString(newValue));
 			}
 			else if ("multiply".equalsIgnoreCase(counterFunction))
 			{
 				int newValue = value * operand;
 				setVariable("c", Integer.toString(newValue));
 			}
 			else if ("divide".equalsIgnoreCase(counterFunction))
 			{
				if (operand == 0) {
					scriptError("Cannot divide by zero");
					return;
				}
 				int newValue = value / operand;
 				setVariable("c", Integer.toString(newValue));
 			}
 			else if ("modulus".equalsIgnoreCase(counterFunction))
 			{
 				int newValue = value % operand;
 				setVariable("c", Integer.toString(newValue));
 			}
 			else
 			{
 				scriptError("Unrecognized counter command");
 			}
 		}
 	}
 
 	protected class WSLDeleteVariable extends WSLCommand {
 		
 		public void execute (String arguments) {
 			String var = arguments.split(argSeparator)[0];
 			deleteVariable(var);
 		}
 	}
 
 	private void setVariable(String name, String value) {
 		setVariable(name, new WSLString(value));
 	}
 	
 	private void setVariable(String name, IWSLValue value) {
 		variables.put(name, value);
 	}
 	
 	private void deleteVariable(String name) {
 		variables.remove(name);
 	}
 	
 	protected class WSLSetVariable extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)(\\s+(.+)?)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			if (m.find())
 			{
 				String name = m.group(1);
 				String value = m.group(3);
 				if(value == null)
 					value = " ";
 				commands.echo("setVariable: " + name + "=" + value);
 				setVariable(name, value);
 			} else {
 				scriptError("Invalid arguments to setvariable");
 			}
 		}
 	}
 	
 	protected class WSLAction extends WSLCommand {
 		
 		private Pattern clearFormat = Pattern.compile("^clear");
 		private Pattern removeFormat = Pattern.compile("^remove\\s+(.*)$");
 		private Pattern addFormat = Pattern.compile("^(.*)\\s+when\\s+(.*)$");
 		
 		public void execute(String arguments) {
 			Matcher clearMatcher = clearFormat.matcher(arguments);
 			Matcher removeMatcher = removeFormat.matcher(arguments);
 			Matcher addMatcher = addFormat.matcher(arguments);
 			
 			if(clearMatcher.find()) {
 				commands.clearActions();
 			} else if(removeMatcher.find()) {
 				commands.removeAction(removeMatcher.group(1));
 			} else if(addMatcher.find()) {
 				commands.addAction(addMatcher.group(1), addMatcher.group(2));
 			} else {
 				scriptError("Invalid arguments to action");
 			}
 		}
 	}
 	
 	protected void gotoLine(WSLScriptLine command) {
 		curLine = nextLine = command;
 		
 		// if we're in an action, interrupt execution on the main thread
 		if(Thread.currentThread() != scriptThread) {
 			commands.interrupt();
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		WSLScriptLine command = labels.get(label.toLowerCase());
 		
 		if (command != null)
 		{
 			gotoLine(command);
 		}
 		else {
 			command = labels.get("labelerror");
 			if (command != null)
 			{
 				gotoLine(command);
 			}
 			else
 			{
 				scriptError("Invalid goto statement, label \"" + label + "\" does not exist");
 			}
 		}
 	}
 	
 	protected class WSLGoto extends WSLCommand {
 		
 		public void execute (String arguments) {
 			if(arguments.trim().length() > 0) {
 				String[] args = arguments.split(argSeparator);
 				String label = args[0];
 				gotoLabel(label);
 			} else {
 				scriptError("Invalid arguments to goto");
 			}
 		}
 	}
 	
 	protected void gosub (String label, String arguments)
 	{
 		String[] args = arguments.split(argSeparator);
 		
 		// TODO save previous state of variables
 		setVariable("$0", arguments);
 		for(int i = 0; i < args.length; i++) {
 			setVariable("$" + (i + 1), args[i]);
 		}
 		
 		callstack.push(nextLine);
 		WSLScriptLine command = labels.get(label.toLowerCase());
 		
 		if (command != null)
 		{
 			gotoLine(command);
 		} else {
 			scriptError("Invalid gosub statement, label \"" + label + "\" does not exist");
 		}
 	}
 	
 	protected class WSLGosub extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s*(.*)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find()) {
 				gosub(m.group(1), m.group(2));
 			} else {
 				scriptError("Invalid arguments to gosub");
 			}
 		}
 	}
 	
 	protected void gosubReturn () {
 		if (callstack.empty()) {
 			scriptError("Invalid use of return, not in a subroutine");
 		} else {
 			curLine = nextLine = callstack.pop();
 		}
 	}
 	
 	protected class WSLReturn extends WSLCommand {
 		
 		public void execute (String arguments) {
 			gosubReturn();
 		}
 	}
 
 	protected class WSLMatchWait extends WSLCommand {
 		
 		public void execute (String arguments) {
 			Match match = commands.matchWait();
 			
 			if (match != null)
 			{
 				gotoLabel((String)match.getAttribute("label"));
 				commands.waitForPrompt();
 				commands.waitForRoundtime();
 			} else {
 				if(!stopped)
 					scriptError("Internal error, no match was found. Please inform Warlock developers.");
 			}
 		}
 	}
 
 	protected void addMatch(String label, Match match) {
 		match.setAttribute("label", label);
 		commands.addMatch(match);
 	}
 	
 	protected class WSLMatchRe extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s+/(.*)/(\\w*)");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				String regex = m.group(2);
 				boolean caseInsensitive = m.group(3).contains("i");
 				Match match = new RegexMatch(regex, caseInsensitive);
 				
 				addMatch(m.group(1), match);
 			} else {
 				scriptError("Invalid arguments to matchre");
 			}
 		}
 
 	}
 
 	protected class WSLMatch extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([^\\s]+)\\s+(.*)$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				Match match = new TextMatch(m.group(2));
 				addMatch(m.group(1), match);
 			} else {
 				scriptError("Invalid arguments to match");
 			}
 		}
 	}
 
 	protected class WSLWaitForRe extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^/(.*)/(\\w*)");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				String flags = m.group(2);
 				boolean ignoreCase = false;
 				
 				if (flags != null && flags.contains("i"))
 				{
 					ignoreCase = true;
 				}
 				
 				Match match = new RegexMatch(m.group(1), ignoreCase);
 				
 				commands.waitFor(match);
 			} else {
 				scriptError("Invalid arguments to waitforre");
 			}
 		}
 	}
 	
 	protected class WSLWaitFor extends WSLCommand {
 		
 		public void execute (String arguments) {
 			if (arguments.length() >= 1)
 			{
 				Match match = new TextMatch(arguments);
 				commands.waitFor(match);
 				
 			} else {
 				scriptError("Invalid arguments to waitfor");
 			}
 		}
 	}
 
 	protected class WSLWait extends WSLCommand {
 		
 		public void execute (String arguments) {
 			commands.waitForPrompt();
 		}
 	}
 	
 	protected class WSLPut extends WSLCommand {
 		
 		public void execute(String arguments) {
 			commands.put(arguments);
 		}
 	}
 	
 	protected class WSLEcho extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.echo(arguments);
 		}
 	}
 	
 	protected class WSLPause extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			int time;
 			
 			if(arguments.trim().length() > 0) {
 				String[] args = arguments.split(argSeparator);
 			
 				try {
 					time = Integer.parseInt(args[0]);
 				} catch(NumberFormatException e) {
 					scriptError("Non-numeral \"" + args[0] + "\" passed to pause");
 					return;
 				}
 			} else {
 				time = 1;
 			}
 			commands.pause(time);
 		}
 	}
 	
 	protected class WSLMove extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.move(arguments);
 		}
 	}
 	
 	protected class WSLNextRoom extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.nextRoom();
 		}
 	}
 	
 	protected class WSLExit extends WSLCommand {
 		
 		public void execute (String arguments) {
 			// TODO figure out if we should make this call here or elsewhere
 			stop();
 		}
 	}
 	
 	protected class WSLIf_ extends WSLCommand {
 		private String variableName;
 		public WSLIf_ (String variableName)
 		{
 			this.variableName = variableName;
 		}
 		
 		public void execute (String arguments) {
 			if (variables.containsKey(variableName))
 			{
 				WSLScript.this.execute(arguments);
 			}
 		}
 	}
 	
 	private class WSLRandom extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^(\\d+)\\s+(\\d+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				int min = Integer.parseInt(m.group(1));
 				int max = Integer.parseInt(m.group(2));
 				int r = min + new Random().nextInt(max - min + 1);
 				
 				setVariable("r", Integer.toString(r));
 			} else {
 				scriptError("Invalid arguments to random");
 			}
 		}
 	}
 	
 	private class WSLTimer extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^(\\w+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				String command = m.group(1);
 				if(command.equals("start"))			timer.start();
 				else if(command.equals("stop"))		timer.stop();
 				else if(command.equals("clear"))	timer.clear();
 				else {
 					scriptError("Invalid command \"" + command + "\" given to timer");
 				}
 			} else {
 				scriptError("Invalid arguments to timer");
 			}
 		}
 	}
 	
 	public IScriptEngine getScriptEngine() {
 		return engine;
 	}
 	
 	public void movedToRoom() {
 		commands.movedToRoom();
 	}
 	
 }
