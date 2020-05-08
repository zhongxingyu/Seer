 package capstone.wrapper;
 
 import static capstone.wrapper.DebuggerCommand.*;
 import capstone.util.*;
 import org.json.simple.*;
 import java.util.*;
 import java.util.concurrent.*;
 import java.io.*;
 
 public abstract class Wrapper extends Thread
 {
     private Object requestLock;
     private DebuggerRequest request;
     private boolean active;
 
     public Wrapper()
     {
         requestLock = new Object();
         active = false;
     }
 
     public abstract List<ProgramError> prepare(String programText) throws IOException, InterruptedException;
     public abstract void killDebugger();
     //public abstract void cleanup(); // TODO require a cleanup method
     public abstract void runProgram() throws IOException;
 
     public abstract String getStdOut() throws IOException;
    //public abstract String getStdErr() throws IOException; // TODO add this
     public abstract void provideInput(String input) throws IOException;
     public abstract StackFrame getLocalValues() throws IOException;
     public abstract List<StackFrame> getStack() throws IOException;
    public abstract String evaluateExpression(String expression) throws IOException; // TODO comment this -- will return null if there is an error
 
     public abstract void stepIn() throws IOException;
     public abstract void stepOut() throws IOException;
     public abstract void stepOver() throws IOException;
 
     public abstract void addBreakpoint(int lineNumber) throws IOException;
     public abstract int getLineNumber() throws IOException;
 
     public boolean submitRequest(DebuggerRequest request)
     throws InterruptedException
     {
         synchronized (requestLock)
         {
            if (this.request == null && request != null)
             {
                 this.request = request;
                 System.out.println("[gdb] Notifying to wake up the wrapper!");
                 requestLock.notify();
                 System.out.println("[gdb] Notified to wake up the wrapper!");
                 return true;
             }
             else
             {
                 return false;
             }
         }
     }
 
     public boolean waiting()
     {
         synchronized (requestLock)
         {
             return (request != null);
         }
     }
 
     private void clearRequest()
     {
         synchronized (requestLock)
         {
             System.out.println("[gdb] Notifying the daemon!");
             synchronized (request.monitor)
             {
                 request.monitor.notifyAll();
             }
             System.out.println("[gdb] Notified the daemon!");
             request = null;
         }
     }
 
     public void shutdown()
     {
         active = false;
     }
 
     public boolean isActive()
     {
         return active;
     }
 
     @Override
     public void run()
     {
         System.out.println("[gdb] Starting the thread!");
         active = true;
         activeLoop: while (active)
         {
             System.out.println("[gdb] entering request lock block");
             synchronized (requestLock)
             {
                 while (request == null)
                 {
                     try
                     {
                         System.out.println("[gdb] waiting for request lock");
                         requestLock.wait();
                         System.out.println("[gdb] woken up");
                     }
                     catch (InterruptedException exception)
                     {
                         System.out.println("[gdb] exception!");
                         if (active)
                         {
                             continue;
                         }
                         else
                         {
                             // To whoever reads this: I'm sorry.
                             // I decided to use a label and I
                             // should be punished.  --ntietz
                             break activeLoop;
                         }
                     }
                 }
 
                 try
                 {
                     JSONObject jsonResult = new JSONObject();
                     request.result = "";
                     switch (request.command)
                     {
                         case PREPARE:
                             System.out.println("[gdb] Handling a prepare command!");
                             List<ProgramError> errors = prepare(request.data);
                             JSONArray jsonErrorList = new JSONArray();
                             for (ProgramError each : errors)
                             {
                                 JSONObject jsonEach = new JSONObject();
                                 jsonEach.put("linenumber", "" + each.lineNumber);
                                 jsonEach.put("errortext", each.errorText);
                                 jsonErrorList.add(jsonEach);
                             }
                             jsonResult.put("errors", jsonErrorList);
                             System.out.println("[gdb] Results: " + request.result);
                             break;
 
                         case RUN:
                             runProgram();
                             break;
 
                         case STEPIN:
                             stepIn();
                             break;
 
                         case STEPOUT:
                             stepOut();
                             break;
 
                         case STEPOVER:
                             stepOver();
                             break;
 
                         case GETVALUES:
                             // TODO this hasn't been implemented yet...
                             request.result = "";
                             break;
 
                         case GETSTDOUT:
                             String output = getStdOut();
                             System.out.println("[gdb] Output was: " + output);
                             jsonResult.put("stdout", output);
                             break;
 
                         case GETSTDERR:
                             // TODO this hasn't been implemented yet...
                             break;
 
                         //note: GIVEINPUT case is not handled because
                         // it should be handled by the daemon itself
 
                         case ADDBREAKPOINT:
                             try
                             {
                                 addBreakpoint(Integer.parseInt(request.data));
                             }
                             catch (NumberFormatException badFormatException)
                             {
                                 request.result = "Error: invalid line number";
                             }
                             break;
 
                         case GETLINENUMBER:
                             int lineNumber = getLineNumber();
                             jsonResult.put("linenumber", lineNumber);
                             request.result = String.valueOf(lineNumber);
                             break;
 
                         case KILLDEBUGGER:
                             active = false;
                             killDebugger();
                             break;
 
                         case UNKNOWN:
                             break;
                     }
                     request.result = jsonResult.toString();
                 }
                 catch (IOException exception)
                 {
                     active = false;
                     killDebugger();
                 }
                 catch (InterruptedException exception)
                 {
                     active = false;
                     killDebugger();
                 }
             }
 
             clearRequest();
         } 
     }
 }
 
