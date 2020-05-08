 package xelitez.updateutility;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.List;
 
 public class Version implements IXEZUpdate
 {
     public static int majorVersion = 1;
     public static int minorVersion = 2;
    public static int majorBuild = 8;
     public static String MC = "MC:1.7.2";
     
     public static String newVersion;
     public static boolean available = false;
     
     public static String getVersion()
     {
         return produceVersion(majorVersion, minorVersion, majorBuild);
     }
     
     private static String produceVersion(int var1, int var2, int var3)
     {
         StringBuilder Str1 = new StringBuilder();
         Str1.append(var1);
 
         Str1.append(".");
         Str1.append(var2);
 
         Str1.append(".");
         Str1.append(var3);
 
         return Str1.toString();
     }
 
 	@Override
 	public String getCurrentVersion() 
 	{
 		return Version.getVersion() + " for " + Version.MC;
 	}
 
 	@Override
 	public String getNewVersion() 
 	{
 		return Version.newVersion;
 	}
 
 	@Override
 	public void checkForUpdates() 
 	{
 		List<String> strings = new ArrayList<String>();
 		int MV = 0;
 		int mV = 0;
 		int MB = 0;
 		String NMC = "";
 		
 		try
 		{
			URL url = new URL("https://raw2.github.com/XEZKalvin/UpdateUtility/master/src/main/java/xelitez/updateutility/Version.java");
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
 			XEZLog.info("Unable to check for updates");
 		    		return;
 		}
 		catch (ConnectException e)
 		{
 			XEZLog.info("Unable to connect to update page");
 			return;
 		    	}
 		catch (IOException e)
 		{
 			XEZLog.info("Unable to check for updates");
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
 				
 				if (line.contains("public static String MC") && !line.contains("\"public static String MC\"") && line.contains("MC:") && !line.contains("\"MC:\""))
 				{
 					line = line.substring(line.indexOf("MC:") + 3, line.indexOf("\";"));
 					NMC = line;
 				}
 			}
 		}
 		available = false;
 		if ((!getVersion().matches(produceVersion(MV, mV, MB)) || !MC.matches("MC:" + NMC)) && !produceVersion(MV, mV, MB).matches("0"))
 		{
 			if (!MC.matches("MC:" + NMC) || !getVersion().matches(produceVersion(MV, mV, MB)))
 			{
 				available = true;
 			}
 		}
 		newVersion = produceVersion(MV, mV, MB);
 		
 		if (!NMC.matches(""))
 		{
 			newVersion = newVersion + " for MC:" + NMC;
 		}
 		
 	}			
 
 	@Override
 	public boolean doesModCheckForUpdates() 
 	{
 		return true;
 	}
 
 	@Override
 	public boolean isUpdateAvailable() 
 	{
 		return Version.available;
 	}
 
 	@Override
 	public String getModIcon() 
 	{
 		return "uu:xelitez/updateutility/xezmods.png";
 	}
 
 	@Override
 	public String getUpdateUrl() 
 	{
 		return "http://www.minecraftforum.net/topic/842232-/#XUU";
 	}
 
 	@Override
 	public String getDownloadUrl() 
 	{
 		List<String> strings = new ArrayList<String>();
 		
 		try
 		{
 			URL url = new URL("https://raw2.github.com/XEZKalvin/UpdateUtility/master/updateURLstorage.txt");
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
 		catch(Exception E)
 		{
 			XEZLog.severe("Unable to obtain download URL");
 		}
 		for (int i = 0; i < strings.size(); i++)
 		{
 			String line = "";
 			
 			if (strings.get(i) != null)
 			{
 				line = (String)strings.get(i);
 			}
 			if(line.contains("<updateutility>"))
 			{
 				return line.substring(line.indexOf("<updateutility>") + 15, line.indexOf("</updateutility>"));
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public String stringToDelete() 
 	{
 		return "UpdateUtility";
 	}
 
 }
