 package capstone.wrapper;
 
 import static capstone.wrapper.DebuggerCommand.*;
 import capstone.util.*;
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
         active = true;
     }
 
     public abstract List<ProgramError> prepare(String programText) throws IOException, InterruptedException;
     public abstract void killDebugger();
     //public abstract void cleanup(); // TODO require a cleanup method
     public abstract void runProgram() throws IOException;
 
     public abstract String getStdOut() throws IOException;
     public abstract void provideInput(String input) throws IOException;
     public abstract StackFrame getLocalValues() throws IOException;
     public abstract List<StackFrame> getStack() throws IOException;
     public abstract String evaluateExpression(String expression) throws IOException;
 
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
             if (request == null)
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
             request.monitor.notifyAll();
             System.out.println("[gdb] Notified the daemon!");
             request = null;
         }
     }
 
     public void shutdown()
     {
         active = false;
     }
 
     @Override
     public void run()
     {
        System.out.println("[gdb] Starting the thread!");
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
                     request.result = "";
                     switch (request.command)
                     {
                         case PREPARE:
                             System.out.println("[gdb] Handling a prepare command!");
                             List<ProgramError> errors = prepare(request.data);
                             request.result = errors.toString(); // FIXME use JSON
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
                             // TODO
                             request.result = "";
                             break;
 
                         case GETSTDOUT:
                             request.result = getStdOut();
                             break;
 
                         case GETSTDERR:
                             // TODO
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
                             request.result = String.valueOf(lineNumber);
                             break;
 
                         case KILLDEBUGGER:
                             active = false;
                             killDebugger();
                             break;
 
                         case UNKNOWN:
                             break;
                     }
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
 
