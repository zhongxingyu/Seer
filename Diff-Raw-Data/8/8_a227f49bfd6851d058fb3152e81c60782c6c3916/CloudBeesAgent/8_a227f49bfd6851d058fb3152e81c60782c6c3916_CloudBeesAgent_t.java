 package com.cloudbees.run;
 
 import java.io.*;
 import java.lang.instrument.Instrumentation;
 import java.util.*;
 
 /**
  * @author Fabian Donze
  */
 public class CloudBeesAgent {
     protected Map<String, String> options = new HashMap<String, String>();
     protected Instrumentation instrumentation;
 
     public static void main(String[] args) {
         if (args.length == 1) {
             premain(args[0], null);
         }
         System.out.println(System.getProperties());
         Properties p = System.getProperties();
         SortedMap sortedSystemProperties = new TreeMap(p);
         Set keySet = sortedSystemProperties.keySet();
         for (Object aKeySet : keySet) {
             String key = (String) aKeySet;
             String value = (String) p.get(key);
             System.out.println("[" +key + "]=[" + value + "]");
         }
     }
     public static void premain(String agentArgs, Instrumentation inst) {
         CloudBeesAgent agent = new CloudBeesAgent(agentArgs, inst);
         try {
             agent.loadSystemProperties();
         } catch (IOException e) {
             System.err.println("Cannot load properties: " + e.getMessage());
         }
     }
 
     public CloudBeesAgent(String agentArgs, Instrumentation inst) {
         if (agentArgs != null) {
             String[] opts = agentArgs.split(",");
             for (String opt : opts) {
                 String[] parts = opt.split(":");
                 if (parts.length == 2)
                     options.put(parts[0], parts[1]);
             }
         }
         instrumentation = inst;
 
 //        instrumentation.addTransformer(this);
     }
 
     public void loadSystemProperties() throws IOException {
         String propFileName = options.get("sys_prop");
         if (propFileName != null) {
             File propertyFile = new File(propFileName);
             if (propertyFile.exists()) {
                 BufferedReader br = new BufferedReader(new FileReader(propertyFile));
                 try {
                     String line = br.readLine();
                     while (line != null) {
                        int idx = line.indexOf("=");
                        if (idx > -1) {
                            String name = line.substring(0, idx);
                            String value = line.substring(idx+1);
                             if (value.startsWith("\"") && value.endsWith("\""))
                                 value = value.substring(1, value.length()-1);
 //                            System.out.println("Set: [" + name + "=" + value + "]");
 
                             // un-escape \" to "
                             value = value.replaceAll("\\\\\"", "\"");
 //                            System.out.println("Set: [" + name + "=" + value + "]");
                             // un-escape \\ to \
                             value = value.replaceAll("\\\\\\\\", "\\\\");
 //                            System.out.println("Set: [" + name + "=" + value + "]");
 
                             System.setProperty(name, value);
                         }
 
                         line = br.readLine();
                     }
                 } finally {
                     br.close();
                 }
             } else {
                 System.err.println("Property file not found: " + propFileName);
             }
         } else {
             System.err.println("Property file not defined");
         }
     }
 
 /*
     public byte[] transform(ClassLoader loader,
                             String className,
                             Class<?> classBeingRedefined,
                             ProtectionDomain protectionDomain,
                             byte[] classfileBuffer)
             throws IllegalClassFormatException {
         System.out.println("transform(): class: " + className + " (" + classfileBuffer.length + " bytes)");
         return null;
     }
 */
 }
