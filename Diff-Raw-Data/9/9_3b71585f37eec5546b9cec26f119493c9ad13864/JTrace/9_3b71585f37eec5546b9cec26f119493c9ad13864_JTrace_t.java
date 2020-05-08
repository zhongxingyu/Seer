 package src;
 
 import java.util.Map;
 import java.util.List;
 import java.util.Iterator;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import com.sun.jdi.Bootstrap;
 import com.sun.jdi.VirtualMachineManager;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.connect.*;
 
 public class JTrace {
     private final VirtualMachine vm;
 
     private String[] excludes = {"java.*", "javax.*", "sun.*", "com.sun.*"};
     
     public static void main(String[] args) {
         if (args.length != 4) {
            System.err.println("Usage:");
            System.err.println("\tjava JTrace attach <pid> <class> <method>");
            System.err.println("\tjava JTrace launch <command>:<address> <class> <method>");
             return;
         }
 
         JTrace jt = new JTrace(args[0], args[1], args[2], args[3]);
         jt.vm.setDebugTraceMode(VirtualMachine.TRACE_NONE);
     }
 
     public JTrace(String connect, String jdiInfo, String className, String methodName) {
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
         
         System.out.println(arguments);
 
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
         
         System.out.println(arguments);
         
         try {
             return connector.attach(arguments);
         } catch (Exception e) {
             throw new Error(e);
         } 
     }
 }
