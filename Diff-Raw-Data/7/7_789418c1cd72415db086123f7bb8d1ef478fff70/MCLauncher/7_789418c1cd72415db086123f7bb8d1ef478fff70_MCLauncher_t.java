 package com.kokakiwi.mclauncher;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 
 import javax.swing.UIManager;
 
 import com.kokakiwi.mclauncher.api.LauncherAPI;
 import com.kokakiwi.mclauncher.api.ui.Theme;
 import com.kokakiwi.mclauncher.core.Launcher;
 import com.kokakiwi.mclauncher.core.Loginer;
 import com.kokakiwi.mclauncher.core.TimeLine;
 import com.kokakiwi.mclauncher.core.Updater;
 import com.kokakiwi.mclauncher.core.launcher.LauncherApplet;
 import com.kokakiwi.mclauncher.ui.LauncherFrame;
 import com.kokakiwi.mclauncher.ui.simple.SimpleTheme;
 import com.kokakiwi.mclauncher.utils.ConfigList;
 import com.kokakiwi.mclauncher.utils.Configuration;
 import com.kokakiwi.mclauncher.utils.MCLogger;
 import com.kokakiwi.mclauncher.utils.lang.Translater;
 
 public class MCLauncher
 {
     // Components
     private Configuration config;
     public ConfigList cl;
     private LauncherAPI   api;
     private LauncherFrame frame;
     
     private Theme         theme;
     
     // Core
     private TimeLine      timeLine;
     private Loginer       loginer;
     private Updater       updater;
     private Launcher      launcher;
     
     public MCLauncher(String[] args)
     {
         instance = this;
         MCLogger.register();
         // Minecraft Forge Mod Loader property.
         System.setProperty("minecraft.applet.WrapperClass",
                 LauncherApplet.class.getCanonicalName());
         
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (final Exception e)
         {
             e.printStackTrace();
         }
         
         init(args);
     }
     
     private void init(String[] args)
     {
         this.setConfig(Configuration.getLauncherConfiguration());
         Translater.setLang(config.getString("window.lang"));
         
         api = new LauncherAPI(this); 
         cl = new ConfigList(api,false);
         timeLine = new TimeLine(this);
 
         //Try loading mod-list
         
         File path = null;
 		try 
 		{
 			String[] pathes = System.getProperty("java.class.path").split(":");
 			for(String pth : pathes)
 			{
 				path = new File(pth);
 				if(!path.isDirectory())
 					path = path.getParentFile();
 				
 				if(path != null)
 		        {
 		        	File modlist = new File(path,"list.txt");
 		        	if(modlist.exists())
 		        	{
 		        		String list = api.readFile(modlist);
 		        		String[] gms = list.split(",");
 		        		for(String gm : gms)
 		        		{
 		        			if(cl.getConfig(gm) == null)
 		        				cl.registerConfig(gm);
 		        		}
 		        	}
 		        }
 			}
 			
 		} catch (Exception e) { System.out.println(e); }
 		
         
         //Load last selected mod
         String last = "default";
         File f = new File(api.getLauncherDirectory().toString()+"/last");
     	try 
     	{
     		if(!f.exists())
     		{
 	    		this.setLastMod("Default");
 				api.writeFile(f, "Default");
     		}
 			last = api.readFile(f);
 		} catch (Exception e) {
 			MCLogger.debug("Unable to load last mod file");
 		}
    	
         Configuration c = cl.getConfig(last);
 		if(c != null)
 			this.setConfig(c);
 		
         this.reload();
         timeLine.init();
         parseArguments(args);
     }
     
     public void setLastMod(String s)
     {
     	File f = new File(api.getLauncherDirectory().toString()+"/last");
     	try 
     	{
 	    	f.createNewFile();
 			api.writeFile(f, s);
     	} catch (Exception e) {
 			MCLogger.debug("Unable to create last mod file");
 		}
     }
 
     public void setConfig(Configuration c)
     {
     	this.config = c;
     }
     
     public void reload()
     {
     	
     	Translater.setLang(config.getString("window.lang"));
     	System.out.println("Select Language: "+config.getString("window.lang"));
     	
         loginer = new Loginer(this);
         updater = new Updater(this);
         launcher = new Launcher(this);
         
         api = new LauncherAPI(this);
         
         if(cl != null)
         	cl.reloadConfigs(true);
         
        loginer.init();
        
         if(frame == null)
         {
         	frame = new LauncherFrame(this);
         	loadTheme();
             frame.init();
         } else
         {
         	timeLine.reload();
         }
          
        
     }
     
     @SuppressWarnings("unchecked")
     private void loadTheme()
     {
         try
         {
             final Class<? extends Theme> clazz = (Class<? extends Theme>) MCLauncher.class
                     .getClassLoader().loadClass(
                             config.getString("window.theme"));
             final Constructor<? extends Theme> constructor = clazz
                     .getConstructor();
             theme = constructor.newInstance();
             
             System.out.println("Using custom theme '" + clazz.getSimpleName()
                     + "'...");
         }
         catch (final Exception e)
         {
             theme = new SimpleTheme();
         }
         
         theme.onLoad(api);
     }
     
     private void parseArguments(String[] args)
     {
         if (args.length > 0)
         {
             config.set("username", args[0]);
             if (args.length > 1)
             {
                 config.set("password", args[1]);
                 if (args.length > 2)
                 {
                     String ip = args[2];
                     String port = "25565";
                     
                     if (ip.contains(":"))
                     {
                         final String[] split = ip.split(":");
                         ip = split[0];
                         port = split[1];
                     }
                     
                     config.set("server", ip);
                     config.set("port", port);
                 }
             }
         }
     }
     
     public void start()
     {
         timeLine.start();
         frame.setVisible(true);
     }
     
     public void stop()
     {
         frame.setVisible(false);
         new Thread(new Runnable() {
             
             public void run()
             {
                 try
                 {
                     Thread.sleep(500L);
                 }
                 catch (final InterruptedException e)
                 {
                     e.printStackTrace();
                 }
                 
                 System.exit(0);
             }
         }).start();
         System.exit(0);
     }
     
     // Getter / Setter
     
     public Configuration getConfig()
     {
         return config;
     }
     
     public Loginer getLoginer()
     {
         return loginer;
     }
     
     public Updater getUpdater()
     {
         return updater;
     }
     
     public Launcher getLauncher()
     {
         return launcher;
     }
     
     public LauncherAPI getApi()
     {
         return api;
     }
     
     public TimeLine getTimeLine()
     {
         return timeLine;
     }
     
     public LauncherFrame getFrame()
     {
         return frame;
     }
     
     public Theme getTheme()
     {
         return theme;
     }
     
     // INSTANCE
     
     private static MCLauncher instance = null;
     
     // Main Class methods
     
     public static MCLauncher getInstance()
     {
         return instance;
     }
     
     public final static long MIN_HEAP         = 511;
     
     public final static long RECOMMENDED_HEAP = 1024;
     
     public static void main(String[] args)
     {
         final float heapSizeMegs = Runtime.getRuntime().maxMemory() / 1024L / 1024L;
         boolean start = false;
         if (heapSizeMegs > MIN_HEAP || !start)
         {
             start = true;
         }
         else
         {
             try
             {
                 final String pathToJar = MCLauncher.class.getProtectionDomain()
                         .getCodeSource().getLocation().toURI().getPath();
                 
                 final ArrayList<String> params = new ArrayList<String>();
                 
                 params.add("javaw");
                 params.add("-Xmx" + Long.toString(RECOMMENDED_HEAP) + "m");
                 params.add("-Dsun.java2d.noddraw=true");
                 params.add("-Dsun.java2d.d3d=false");
                 params.add("-Dsun.java2d.opengl=false");
                 params.add("-Dsun.java2d.pmoffscreen=false");
                 
                 params.add("-classpath");
                 params.add(pathToJar);
                 params.add("com.kokakiwi.mclauncher.MCLauncher");
                 final ProcessBuilder pb = new ProcessBuilder(params);
                 final Process process = pb.start();
                 if (process == null)
                 {
                     throw new Exception("!");
                 }
                 System.exit(0);
             }
             catch (final Exception e)
             {
                 e.printStackTrace();
                 start = true;
             }
         }
         
         if (start)
         {
             final MCLauncher main = new MCLauncher(args);
             main.start();
         }
     }
     
 }
