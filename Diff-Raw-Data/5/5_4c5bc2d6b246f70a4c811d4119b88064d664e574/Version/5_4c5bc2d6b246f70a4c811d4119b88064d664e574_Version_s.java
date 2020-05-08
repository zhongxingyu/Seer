 package xelitez.frostcraft;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 import xelitez.frostcraft.registry.Settings;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 
 public class Version
 {
     public static int majorVersion = 1;
     public static int minorVersion = 0;
     public static int majorBuild = 3;
    public static int minorBuild = 10;
     public static String MC = "MC:1.7.2";
 
     public static String newVersion;
     public static boolean available = false;
     public static String color = "";
     public static boolean notify = true;
     
     public static boolean registered = false;
 
     public static String getVersion()
     {
         return produceVersion(majorVersion, minorVersion, majorBuild, minorBuild);
     }
 
     private static String produceVersion(int var1, int var2, int var3, int var4)
     {
         StringBuilder Str1 = new StringBuilder();
         Str1.append(var1);
 
         Str1.append(".");
         Str1.append(var2);
 
         Str1.append(".");
         Str1.append(var3);
             
         Str1.append(".");
         Str1.append(var4);
 
         return Str1.toString();
     }
 
     public void checkForUpdatesNoUtility()
     {
     	new Thread()
     	{
     		public void run()
     		{
     			checkForUpdates();
 		    }	
     	}.start();
     }
     
     public void checkForUpdates()
     {
     	List<String> strings = new ArrayList<String>();
     	int MV = 0;
     	int mV = 0;
     	int MB = 0;
     	int mB = 0;
     	String NMC = "";
 			
     	try
     	{
    		URL url = new URL("https://raw.github.com/XEZKalvin/Frostcraft/master/common/xelitez/frostcraft/Version.java");
     		URLConnection connect = url.openConnection();
     		connect.setConnectTimeout(5000);
     		connect.setReadTimeout(5000);
     		BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
     		String str;
     		
     		while ((str = in.readLine()) != null)
     		{
     			strings.add(str);
     		}
     		
     		in.close();
     	}
     	catch (MalformedURLException e)
     	{
     		FCLog.info("Unable to check for updates");
     		return;
     	}
     	catch (ConnectException e)
     	{
     		FCLog.info("Unable to connect to update page");
     		return;
     	}
     	catch (IOException e)
     	{
     		FCLog.info("Unable to check for updates");
     		return;
     	}
 			
     	for (int i = 0; i < strings.size(); i++)
     	{
     		String line = "";
     		
     		if (strings.get(i) != null)
     		{
     			line = (String)strings.get(i);
     		}
     		
     		if (line != null && !line.matches(""))
     		{
     			if (line.contains("public static int majorVersion") && !line.contains("\"public static int majorVersion\""))
     			{
     				line = line.substring(line.indexOf("= ") + 2, line.indexOf(';'));
     				MV = Integer.parseInt(line);
     			}
     			
     			if (line.contains("public static int minorVersion") && !line.contains("\"public static int minorVersion\""))
     			{
     				line = line.substring(line.indexOf("= ") + 2, line.indexOf(';'));
     				mV = Integer.parseInt(line);
     			}
     			
     			if (line.contains("public static int majorBuild") && !line.contains("\"public static int majorBuild\""))
     			{
     				line = line.substring(line.indexOf("= ") + 2, line.indexOf(';'));
     				MB = Integer.parseInt(line);
     			}
     			
     			if (line.contains("public static int minorBuild") && !line.contains("\"public static int minorBuild\""))
     			{
     				line = line.substring(line.indexOf("= ") + 2, line.indexOf(';'));
     				mB = Integer.parseInt(line);
     			}
     			
     			if (line.contains("public static String MC") && !line.contains("\"public static String MC\"") && line.contains("MC:") && !line.contains("\"MC:\""))
     			{
     				line = line.substring(line.indexOf("MC:") + 3, line.indexOf("\";"));
     				NMC = line;
     			}
     		}
     	}
 		available = false;
     	if ((!getVersion().matches(produceVersion(MV, mV, MB, mB)) || !MC.matches("MC:" + NMC)) && !produceVersion(MV, mV, MB, mB).matches("0"))
     	{
     		if ((Settings.ignoreMC && MC.matches("MC:" + NMC) || (!Settings.ignoreMC && !MC.matches("MC:" + NMC))) || ((Settings.ignoremB && !produceVersion(MV, mV, MB, 0).matches(produceVersion(majorVersion, minorVersion, majorBuild, 0))) || (!Settings.ignoremB && !getVersion().matches(produceVersion(MV, mV, MB, mB)))))
     		{
     			available = true;
     		}
     	}
     	newVersion = produceVersion(MV, mV, MB, mB);
     			
     	if (!NMC.matches(""))
     	{
     		newVersion = newVersion + " for MC:" + NMC;
     	}
     	
     	if (FMLCommonHandler.instance().getSide() == Side.SERVER && !registered && available)
     	{
     		FCLog.info("A new version of Frostcraft is available(" + newVersion + ")");
     	}
     }
 }	
