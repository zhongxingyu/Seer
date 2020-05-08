 package src;
 
 import java.util.*;
 import java.io.*;
 import com.sun.jdi.*;
 import com.sun.jdi.connect.*;
 
 public class JTrace {
     private final VirtualMachine vm;
 
     private String[] excludedPackages = {"java.*", "javax.*", "com.sun.*"};
     
     public static void main(String[] args) {
         if (args.length != 4) {
             System.err.println("Usage:");
             System.err.println("\tjava JTrace attach <pid> <class> <method>");
             System.err.println("\tjava JTrace launch <command>:<address> <class> <method>");
             return;
         }
 
         JTrace jt = new JTrace(args[0], args[1]);
         jt.trace_method(args[2], args[3]);
     }
 
     public JTrace(String connect, String jdiInfo) {
         if (connect.equals("attach")) {
             vm = attach(jdiInfo);
         } else if (connect.equals("launch")) {
             vm = launch(jdiInfo);            
         } else if (connect.equals("list")) {
             System.out.println("Connections:");
             for (Connector connector : Bootstrap.virtualMachineManager().allConnectors()) {
                 System.out.println(connector.name());
                 System.out.println("\t" + connector.transport());
                 System.out.println("\t" + connector.description());
             }
             System.exit(1);
             vm = null;
         } else {
             System.err.println("'" + connect + "' is an invalid connection method.");
             System.exit(1);
             vm = null;
         }
     }
 
     public void trace_method(String className, String methodName) {
         vm.setDebugTraceMode(VirtualMachine.TRACE_NONE);
         PrintWriter output = new PrintWriter(System.out); 
         TraceThread tt = new TraceThread(vm, excludedPackages, output);
         tt.setDefaultEventRequests();
         tt.start();
 
         vm.resume();
         try {
             tt.join();
         } catch (InterruptedException e) {
             System.err.println("Thread was interrupted.");
         }
 	output.close();
     }
 
     VirtualMachine launch(String jdiInfo) {
         LaunchingConnector connector = null;
         
         for (LaunchingConnector c : Bootstrap.virtualMachineManager().launchingConnectors()) {
             if (c.name().equals("com.sun.jdi.RawCommandLineLaunch")) {
                 connector = c;
                 break;
             }   
         }
 
         if (connector == null) {
             throw new Error("No CLI JDI launcher found.");
         }
 
         String[] jdiInfos = jdiInfo.split(":");
         if (jdiInfos.length != 2) {
             throw new Error("Invalid CLI with address form is command:address");
         }
         
         Map<String, Connector.Argument> arguments = connector.defaultArguments();
         Connector.Argument cmdArg = arguments.get("command");
         if (cmdArg == null) {
             throw new Error("No command argument in CLI JDI launcher found.");
         }
         cmdArg.setValue(jdiInfos[0]);
         
         cmdArg = arguments.get("address");
         if (cmdArg == null) {
             throw new Error("No address argument in CLI JDI launcher found.");
         }
         cmdArg.setValue(jdiInfos[1]);
         
         try {
             return connector.launch(arguments);
         } catch (Exception e) {
             throw new Error(e);
         } 
     }
     
     VirtualMachine attach(String jdiInfo) {
         AttachingConnector connector = null;
         for (AttachingConnector c : Bootstrap.virtualMachineManager().attachingConnectors()) {
             if (c.name().equals("com.sun.jdi.ProcessAttach")) {
                 connector = c;
                 break;
             }
         }
         
         if (connector == null) {
             throw new Error("No pid attaching JDI connector found.");
         }
 
         Map<String, Connector.Argument> arguments = connector.defaultArguments();
         Connector.Argument pidArg = arguments.get("pid");
         if (pidArg == null) {
             throw new Error("No pid argument in pid attaching JDI connector found.");
         }
         pidArg.setValue(jdiInfo);
         pidArg = arguments.get("timeout");
         if (pidArg != null) {
             pidArg.setValue("100");
         }
         
         try {
             return connector.attach(arguments);
         } catch (Exception e) {
             throw new Error(e);
         } 
     }
 }
