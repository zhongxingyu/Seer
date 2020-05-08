 package com.wolfesoftware.mipsos.debugger;
 
 import java.io.IOException;
 import java.util.*;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.wolfesoftware.mipsos.assembler.*;
 import com.wolfesoftware.mipsos.common.*;
 import com.wolfesoftware.mipsos.simulator.*;
 
 public class Debugger
 {
     public static void main(String[] args) throws AssemblingException, IOException
     {
         // parse the args
         LinkedList<String> argList = Util.arrayToLinkedList(args);
         DebuggerOptions debuggerOptions = new DebuggerOptions();
         debuggerOptions.parse(argList);
         debuggerOptions.normalize();
 
         if (argList.size() != 1)
             throw new RuntimeException();
         String inputPath = argList.getFirst();
 
         // assemble source
         ExecutableBinary binary = Assembler.assembleToBinary(inputPath, debuggerOptions.simulatorOptions.assemblerOptions);
         // grab the debug info
         DebugInfo debugInfo = null;
         for (Segment segment : binary.segments) {
             if (Arrays.equals(segment.attributes.get(Segment.ATTRIBUTE_TYPE), Segment.TYPE_DEBUGINFO)) {
                 debugInfo = DebugInfo.fromSegment(segment);
                 break;
             }
         }
 
         // init the simualtor core (provide the listener later)
         SimulatorCore simulatorCore = new SimulatorCore(debuggerOptions.simulatorOptions, null);
         simulatorCore.loadBinary(binary);
 
         // init the debugger (including setting the listener)
         Debugger debugger = new Debugger(simulatorCore, debugInfo);
         if (debuggerOptions.breakAt != -1)
             debugger.setBreakpointAtLine(debuggerOptions.breakAt);
 
         // maybe start
         if (debuggerOptions.run)
             debugger.go();
 
         // pass control to the debugger's terminal interface
         debugger.cliMain();
     }
 
     private final SimulatorCore simulatorCore;
     private final DebugInfo debugInfo;
     private final String[] sourceLines;
     private final BlockingEvent needUserActionEvent = new BlockingEvent(true);
     private final Thread simulatorThread;
     private final LinkedBlockingQueue<Runnable> simulatorActions = new LinkedBlockingQueue<Runnable>();
     private static final Runnable STOP = new Runnable() {
         @Override
         public void run()
         {
         }
     };
     private final LinkedBlockingQueue<Character> stdinQueue = new LinkedBlockingQueue<Character>();
     private final HashSet<Integer> breakpoints = new HashSet<Integer>();
     private boolean pausing;
 
     public Debugger(SimulatorCore simulatorCore, DebugInfo debugInfo)
     {
         this.simulatorCore = simulatorCore;
         this.debugInfo = debugInfo;
         this.sourceLines = Util.readLines(debugInfo.inputPath);
         simulatorCore.listener = new ISimulatorListener() {
             @Override
             public char readCharacter()
             {
                 return internalReadCharacter();
             }
             @Override
             public void printCharacter(char c)
             {
                 internalPrintCharacter(c);
             }
         };
         simulatorThread = new Thread(new Runnable() {
             @Override
             public void run()
             {
                 simulatorThreadMain();
             }
         });
         simulatorThread.start();
     }
 
     private void cliMain()
     {
         new Cli().main();
     }
     private void simulatorThreadMain()
     {
         while (true) {
             Runnable action = Util.take(simulatorActions);
             if (action == STOP)
                 break;
             action.run();
         }
     }
 
     public Listing list(int listRadius)
     {
         int address = simulatorCore.getPc();
         int lineNumber;
         try {
             lineNumber = debugInfo.addressToLine(address);
         } catch (IllegalArgumentException e) {
             return new Listing(new String[] { "[no source. " + addressToString(address) + "]" }, -1, -1);
         }
         ArrayList<String> lines = new ArrayList<String>(listRadius);
         int start = Math.max(lineNumber - listRadius, 0);
         int end = Math.min(lineNumber + listRadius + 1, sourceLines.length);
         for (int i = start; i < end; i++) {
             lines.add(sourceLines[i]);
         }
         return new Listing(lines.toArray(new String[lines.size()]), start, lineNumber);
     }
 
     private static String addressToString(int address)
     {
         return "0x" + Util.zfill(Integer.toHexString(address), 8);
     }
 
 
     public void step()
     {
         step(1);
     }
     public void step(final int count)
     {
         needUserActionEvent.clear();
 
         Util.put(simulatorActions, new Runnable() {
             @Override
             public void run()
             {
                 for (int i = 0; i < count && !pausing; i++)
                     simulatorCore.step();
 
                 needUserActionEvent.set();
             }
         });
     }
 
     public void go()
     {
         needUserActionEvent.clear();
 
         Util.put(simulatorActions, new Runnable() {
             @Override
             public void run()
             {
                 while (!pausing) {
                     SimulatorStatus simulatorStatus = simulatorCore.step();
                     if (simulatorStatus != SimulatorStatus.Ready)
                         break;
                     if (breakpoints.contains(simulatorCore.getPc()))
                         break;
                 }
                 pausing = false;
 
                 needUserActionEvent.set();
             }
         });
     }
     public void pause()
     {
         pausing = true;
     }
 
     public void input(String string)
     {
         for (char c : string.toCharArray()) {
             Util.put(stdinQueue, c);
             needUserActionEvent.clear();
         }
     }
 
     public void setBreakpointAtLine(int lineNumber)
     {
         setBreakpointAtAddress(debugInfo.lineToAddress(lineNumber));
     }
     public void setBreakpointAtAddress(int address)
     {
         breakpoints.add(address);
     }
     private boolean isBreakpointAtLine(int lineNumber)
     {
         for (int address : breakpoints)
             if (debugInfo.addressToLine(address) == lineNumber)
                 return true;
         return false;
     }
 
 
     private char internalReadCharacter()
     {
         if (stdinQueue.isEmpty())
             needUserActionEvent.set();
         return Util.take(stdinQueue);
     }
     private void internalPrintCharacter(char c)
     {
         System.out.print(c);
     }
     private class Cli
     {
         private CliSettings settings = new CliSettings();
         public void main()
         {
             Scanner scanner = new Scanner(System.in);
             String lastLine = null;
             while (true) {
                 if (!needUserActionEvent.poll()) {
                     needUserActionEvent.waitForIt();
                     if (simulatorCore.getStatus() == SimulatorStatus.Done)
                         break;
 
                     parseAndRun(settings.autoCommand);
 
                     if (simulatorCore.getStatus() == SimulatorStatus.Stdin)
                         System.out.println("* Blocking on stdin");
                 }
 
                 System.out.print(">>> ");
                 if (!scanner.hasNextLine())
                     break;
                 String line = scanner.nextLine();
                 if (lastLine != null && line.equals("."))
                     line = lastLine;
                 else
                     lastLine = line;
 
                 parseAndRun(line);
             }
 
             pause();
             Util.put(simulatorActions, STOP);
             System.out.println();
             Util.put(stdinQueue, '\0');
         }
         private void parseAndRun(final String line)
         {
             class Parser
             {
                 private final ArrayList<String> commandBuffer = new ArrayList<String>();
                 private final StringBuilder tokenBuffer = new StringBuilder();
                 public void parseAndRun()
                 {
                     int index = 0;
                     while (index < line.length()) {
                         char c = line.charAt(index++);
                         switch (c) {
                             case ' ':
                                 flushToken();
                                 break;
                             case ';':
                                 flushCommand();
                                 break;
                             case '"':
                                 // string literal
                                 while (index < line.length()) {
                                     c = line.charAt(index++);
                                     if (c == '\\' && index < line.length()) {
                                         // escape
                                         c = line.charAt(index++);
                                         switch (c) {
                                             case '\\':
                                                 tokenBuffer.append('\\');
                                                 break;
                                             case 'n':
                                                 tokenBuffer.append('\n');
                                                 break;
                                             default:
                                                 System.err.println("Invalid escape sequence: \\" + c);
                                                 return;
                                         }
                                     } else if (c == '"') {
                                         if (tokenBuffer.length() == 0) {
                                             // special empty token
                                             commandBuffer.add("");
                                         }
                                         break;
                                     } else {
                                         tokenBuffer.append(c);
                                     }
                                 }
                                 break;
                             default:
                                 tokenBuffer.append(c);
                                 break;
                         }
                     }
                     flushCommand();
                 }
 
                 private void flushCommand()
                 {
                     flushToken();
                     if (commandBuffer.isEmpty())
                         return;
                     try {
                         String commandName = commandBuffer.get(0);
                         Command command = commands.get(commandName);
                         if (command == null) {
                             System.err.println("* Bad Command: " + commandName);
                             return;
                         }
                         commandBuffer.remove(0);
                         int argLength = commandBuffer.size();
                         if (!(command.minArgs <= argLength)) {
                             System.err.println("* command " + commandName + " takes at least " + command.minArgs + " argument" + (command.minArgs != 1 ? "s" : ""));
                             return;
                         }
                         if (command.maxArgs != -1 && !(argLength <= command.maxArgs)) {
                             System.err.println("* command " + commandName + " takes at most " + command.maxArgs + " argument" + (command.maxArgs != 1 ? "s" : ""));
                             return;
                         }
                         command.run(commandBuffer.toArray(new String[commandBuffer.size()]));
                     } finally {
                         commandBuffer.clear();
                     }
                 }
 
                 private void flushToken()
                 {
                     if (tokenBuffer.length() == 0)
                         return;
                     commandBuffer.add(tokenBuffer.toString());
                     tokenBuffer.delete(0, tokenBuffer.length());
                 }
             }
             new Parser().parseAndRun();
         }
         private abstract class Command
         {
             public final int minArgs, maxArgs;
             protected Command()
             {
                 this(0, 0);
             }
             protected Command(int minArgs, int maxArgs)
             {
                 this.minArgs = minArgs;
                 this.maxArgs = maxArgs;
             }
             abstract void run(String[] args);
         }
         private HashMap<String, Command> commands = new HashMap<String, Command>();
         private void registerCommand(Command command, String... names)
         {
             for (String name : names)
                 commands.put(name, command);
         }
         {
             registerCommand(new Command(0, 1) {
                 @Override
                 void run(String[] args)
                 {
                     if (args.length == 0) {
                         System.out.println(settings.autoCommand);
                     } else {
                         settings.autoCommand = (String)args[0];
                     }
                 }
             }, "auto");
             registerCommand(new Command(0, -1) {
                 @Override
                 void run(String[] args)
                 {
                     if (args.length == 0) {
                         // list
                         for (int address : breakpoints) {
                             String printThis;
                             try {
                                 int lineNumber = debugInfo.addressToLine(address);
                                 printThis = lineNumber + " [" + addressToString(address) + "]";
                             } catch (IllegalArgumentException e) {
                                 printThis = "[" + addressToString(address) + "]";
                             }
                             System.out.println(printThis);
                         }
                     } else {
                         // toggle
                         for (String arg : args) {
                             if (arg.startsWith("0x")) {
                                 // address
                                 int address;
                                 try {
                                     address = Integer.parseInt(arg.substring("0x".length()), 16);
                                 } catch (NumberFormatException e) {
                                     System.err.println(e.getMessage());
                                     continue;
                                 }
                                 if ((address & 3) != 0) {
                                     System.err.println("* WARNING: word aligning address: " + addressToString(address));
                                     address &= ~3;
                                 }
                                 if (breakpoints.remove(address)) {
                                     // removed
                                     try {
                                         int lineNumber = debugInfo.addressToLine(address);
                                         System.out.println("* breakpoint deleted: " + lineNumber + " [" + addressToString(address) + "]");
                                     } catch (IllegalArgumentException e) {
                                         System.out.println("* breakpoint deleted: [" + addressToString(address) + "]");
                                     }
                                 } else {
                                     // add
                                     breakpoints.add(address);
                                     try {
                                         int lineNumber = debugInfo.addressToLine(address);
                                         System.out.println("* breakpoint created: " + lineNumber + " [" + addressToString(address) + "]");
                                     } catch (IllegalArgumentException e) {
                                         System.err.println("* WARNING: no line number for address " + addressToString(address));
                                         System.out.println("* breakpoint created: [" + addressToString(address) + "]");
                                     }
                                 }
                             } else {
                                 // line number
                                 int lineNumber;
                                 try {
                                     lineNumber = Integer.parseInt(arg);
                                 } catch (NumberFormatException e) {
                                     System.err.println(e.getMessage());
                                     continue;
                                 }
                                lineNumber++;
                                 try {
                                     int address = debugInfo.lineToAddress(lineNumber);
                                     if (breakpoints.remove(address)) {
                                         // removed
                                         System.out.println("* breakpoint deleted: " + lineNumber + " [" + addressToString(address) + "]");
                                     } else {
                                         // add
                                         breakpoints.add(address);
                                         System.out.println("* breakpoint created: " + lineNumber + " [" + addressToString(address) + "]");
                                     }
                                 } catch (IllegalArgumentException e) {
                                     System.out.println("* ERROR: no address for line number " + lineNumber);
                                 }
                             }
                         }
                     }
                 }
             }, "b", "break", "breaks", "breakpoint", "breakpoints");
             registerCommand(new Command() {
                 @Override
                 void run(String[] args)
                 {
                     go();
                 }
             }, "g", "go", "continue", "resume");
             registerCommand(new Command(1, 1) {
                 @Override
                 void run(String[] args)
                 {
                     input((String)args[0] + "\n");
                 }
             }, "i", "in", "stdin", "input");
             registerCommand(new Command(0, 1) {
                 @Override
                 void run(String[] args)
                 {
                     if (args.length != 0) {
                         try {
                             settings.listRadius = Integer.parseInt((String)args[0]);
                         } catch (NumberFormatException e) {
                             System.err.println(e.getMessage());
                         }
                     }
                     Listing listing = list(settings.listRadius);
                     for (int i = 0; i < listing.lines.length; i++) {
                         int lineNumber = i + listing.startLine;
                         boolean current = lineNumber == listing.currentLine;
                         boolean breakpoint = isBreakpointAtLine(lineNumber);
                         String prefix = current ? (breakpoint ? "@>" : "->") : (breakpoint ? "@ " : "  ");
                         System.out.println(prefix + listing.lines[i]);
                     }
                 }
             }, "l", "ls", "list");
             registerCommand(new Command() {
                 @Override
                 void run(String[] args)
                 {
                     pause();
                 }
             }, "pause");
             registerCommand(new Command(0, 1) {
                 @Override
                 void run(String[] args)
                 {
                     int count = 1;
                     if (args.length != 0) {
                         try {
                             count = Integer.parseInt((String)args[0]);
                         } catch (NumberFormatException e) {
                             System.err.println(e.getMessage());
                         }
                     }
                     step(count);
                 }
             }, "s", "step");
         }
     }
 }
