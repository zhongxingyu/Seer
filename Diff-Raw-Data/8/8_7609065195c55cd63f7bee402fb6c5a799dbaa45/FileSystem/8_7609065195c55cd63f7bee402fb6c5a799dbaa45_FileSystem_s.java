 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.api.plugins;
 
 import com.google.gson.Gson;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import net.wgr.xenmaster.api.Host;
 import net.wgr.xenmaster.controller.BadAPICallException;
 
 /**
  * 
  * @created Dec 12, 2011
  * @author double-u
  */
 public class FileSystem {
     protected final static String FILESYTEM_PLUGIN = "xm-filesystem";
     protected static Gson gson = new Gson();
    protected final static Pattern PARTITIION_PATTERN = Pattern.compile("([A-z]{3})(\\d+)");
     
     public static String[] getDiskDevices(Host host) throws BadAPICallException {
         String result = host.callPlugin(FILESYTEM_PLUGIN, "disks", new HashMap<String, String>());
         String[] str = gson.fromJson(result, String[].class);
         ArrayList<String> disks = new ArrayList<>();
         
         for (String device : str) {
            if (!PARTITIION_PATTERN.matcher(device).matches()) disks.add(device);
         }
         return disks.toArray(new String[0]);
     }
     
     public static Map<String, ArrayList<String>> getDiskStructure(Host host) throws BadAPICallException {
         HashMap<String, ArrayList<String>> struct = new HashMap<>();
         String result = host.callPlugin(FILESYTEM_PLUGIN, "disks", new HashMap<String, String>());
         String[] str = gson.fromJson(result, String[].class);
         
         for (String device : str) {
            Matcher m = PARTITIION_PATTERN.matcher(device);
             if (m.matches()) {
                 if (!struct.containsKey(m.group(1))) struct.put(m.group(1), new ArrayList<String>());
                 struct.get(m.group(1)).add(device);
             } else if (!device.startsWith("sd")) {
                 struct.put(device, null);
             }
         }
         
         return struct;
     }
 }
