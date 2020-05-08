 package bootstrap;
 
 import org.apache.commons.cli.*;
 import java.io.IOException;
 import java.io.File;
  
 import org.apache.commons.io.FileUtils; 
 import org.apache.commons.io.IOUtils;
 import java.util.zip.*;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.lang.reflect.*;
 
 public class MynaInstaller
 {
 	public static String 					webctx 			= "/";
 	public static String 					serverName		= "myna";
 	public static String					webroot			= "./myna";
 	public static String					logFile			= null;
 	public static int						port			= 8180;
 	public static java.util.List 			javaOpts	 	= new java.util.ArrayList();
 	public static java.util.Properties 		props			= new java.util.Properties();
 	public static boolean					isJar			= false;
 	public static String 					mode 			= "";
 	public static String 					user 			= "nobody";
 	public static Vector					modeOptions	= new Vector();
 	public static String 					classUrl;
 	
 	public static int						sslPort			= 0;
 	public static String					keystore		= null;
 	public static String					ksPass			= "changeit";
 	public static String					ksAlias			= "myna";
 	
 	public static void main(String[] args) throws Exception
 	{
 		classUrl = MynaInstaller.class.getResource("MynaInstaller.class").toString();
 		isJar = (classUrl.indexOf("jar") == 0);
 		if (!isJar) {
 			System.err.println("Installer can only be run from inside a Myna distribution war file");
 			System.exit(1);
 		}
 	
 		Thread.sleep(1000);
 		
 		
 		CommandLineParser parser = new PosixParser();
 	
 		// create the Options
 		Options options = new Options();
 		options.addOption( "c", "context", true, "Webapp context. Must Start with \"/\" Default: " + webctx);
 		options.addOption( "h", "help", false, "Displays help." );
 		options.addOption( "w", "webroot", true, "Webroot to use. Will be created if webroot/WEB-INF does not exist. Default: " + webroot );
 		options.addOption( "l", "logfile", true, "Log file to use. Will be created if it does not exist. Default: ./<context>.log" );
 		options.addOption( "s", "servername", true, "Name of this instance. Defaults to either \"myna\" or the value of <context> if defined" );
 		
 		options.addOption( "p", "port", true, "HTTP port. Set to 0 to disable HTTP. Default: " + port );
 		options.addOption( "sp", "ssl-port", true, "SSL (HTTPS) port. Set to 0 to disable SSL, Default: 0");
 		
 		options.addOption( "ks", "keystore", true, "keystore path. Default: <webroot>/WEB-INF/myna/myna_keystore");
 		options.addOption( "ksp", "ks-pass", true, "keystore password. Default: " + ksPass );
 		options.addOption( "ksa", "ks-alias", true, "certificate alias. Default: " + ksAlias );
 		
 		modeOptions.add("upgrade");
 		modeOptions.add("install");
 		options.addOption( "m", "mode", true, "Mode: one of "+modeOptions.toString()+". \n"+
 			"\"upgrade\": Upgrades myna installation in webroot and exits. "+
 			"\"install\": Unpacks to webroot, and installs startup files"
 		);
 		options.addOption( "u", "user", true, "User to own and run the Myna installation. Only applies to unix installs. Default: nobody" );
 			
 				
 		HelpFormatter formatter = new HelpFormatter();
 		
 		String cmdSyntax = "java -jar myna-X.war -m <mode> [options]";
 		try {
 			if (args.length == 0){
 				formatter.printHelp(cmdSyntax, options );
 				System.exit(1);	
 			}
 			CommandLine line = parser.parse( options, args );
 			
 			if( line.hasOption( "help" ) ) {
 				formatter.printHelp(cmdSyntax, options);
 				System.exit(0);
 			}
 			
 			if( line.hasOption( "mode" ) ) {
 				mode = line.getOptionValue( "mode" );
 				if (!modeOptions.contains(mode)){
 					System.err.println( "Invalid Arguments.  Reason: Mode must be in " + modeOptions.toString());
 					formatter.printHelp( cmdSyntax, options );
 					System.exit(0);
 				}
 				
 			}
 			
 			if( line.hasOption( "port" ) ) {
 				port = Integer.parseInt(line.getOptionValue( "port" ));
 			}
 			if( line.hasOption( "context" ) ) {
 				webctx=line.getOptionValue( "context" );
 				if (!webctx.startsWith("/")){
 					webctx = "/" + webctx;	
 				}
 			}
 			if( line.hasOption( "servername" ) ) {
 				serverName=line.getOptionValue( "servername" );
 			} else if (!webctx.equals("/")){
 				serverName= webctx.substring(1);	
 			}
 				
 			if( line.hasOption( "user" ) ) {
 				user=line.getOptionValue( "user" );
 			}
 			if( line.hasOption( "logfile" ) ) {
 				logFile= line.getOptionValue( "logfile" );
 			} else if (!webctx.equals("/")){
 				logFile=webctx.substring(1)+".log";	
 			} else{
 				logFile="myna.log";
 			}
 			if( line.hasOption( "webroot" ) ) {
 				webroot=line.getOptionValue( "webroot" );
 			}
 			
 			//ssl stuff
 			if( line.hasOption( "ssl-port" ) ) {
 				sslPort = Integer.parseInt(line.getOptionValue( "ssl-port" ));
 			}
 			
 			if( line.hasOption( "ks-pass" ) ) {
 				ksPass=line.getOptionValue( "ks-pass" );
 			}
 			if( line.hasOption( "ks-alias" ) ) {
 				ksAlias=line.getOptionValue( "ks-alias" );
 			}
 			if( line.hasOption( "keystore" ) ) {
 				keystore=line.getOptionValue( "keystore" );
 			} else {
 				String appBase = new File(webroot).getCanonicalPath();
 				if (keystore == null){
 					keystore = appBase+"/WEB-INF/myna/myna_keystore";
 				}	
 			}
 			
 			javaOpts = line.getArgList();
 		} 
 		catch (ParseException exp ) {
 			System.err.println( "Invalid Arguments.	Reason: " + exp.getMessage() );
 			
 			formatter.printHelp(cmdSyntax, options );
 			System.exit(1);
 		}
 		File wrFile = new File(webroot);
 		webroot= wrFile.toString();
 		//unpack myna if necessary
 		if (!wrFile.exists() || mode.equals("upgrade") || mode.equals("install")){
 			upgrade(wrFile);
 		}
 		
 		if (mode.equals("install")){
 			String javaHome = System.getProperty("java.home");
 			webroot=new File(webroot).getCanonicalPath();
 			if (serverName.length() == 0) serverName = "myna";
 			if (java.lang.System.getProperty("os.name").toLowerCase().indexOf("win") >= 0){
 				if (!new File(logFile).isAbsolute()){
 					logFile = new File(wrFile.toURI().resolve("WEB-INF/" + logFile)).toString();
 				}
 				File templateFile = new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/windows/start_myna.bat")
 				);   
 				
 				String initScript=FileUtils.readFileToString(templateFile)
 				.replaceAll("\\{webctx\\}",webctx)      
 				.replaceAll("\\{server\\}",Matcher.quoteReplacement(serverName))
 				.replaceAll("\\{webroot\\}",Matcher.quoteReplacement(webroot))
 				.replaceAll("\\{logfile\\}",Matcher.quoteReplacement(logFile))
 				.replaceAll("\\{javahome\\}",Matcher.quoteReplacement(javaHome))
 				.replaceAll("\\{port\\}",new Integer(port).toString())
 				.replaceAll("\\{sslPort\\}",new Integer(sslPort).toString())
 				.replaceAll("\\{keystore\\}",Matcher.quoteReplacement(keystore))
 				.replaceAll("\\{ksPass\\}",Matcher.quoteReplacement(ksPass))
 				.replaceAll("\\{ksAlias\\}",Matcher.quoteReplacement(ksAlias))
 				;
 				
 				File scriptFile =new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/start_" + serverName+".bat")
 				);          
 				
 				FileUtils.writeStringToFile(scriptFile,initScript);
 				
 								
 				System.out.println("\nInit script '" + scriptFile +"' created with the following settings:\n");
 				System.out.println("memory=256MB");
 				System.out.println("serverName="+ serverName);
 				System.out.println("javaHome="+ javaHome);
 				System.out.println("context=" +webctx);
 				System.out.println("port=" + port);
 				System.out.println("myna_home=" + webroot);
 				System.out.println("logfile=" + logFile);
 				
 				System.out.println("sslPort=" +sslPort);
 				System.out.println("keyStore=" + keystore);
 				System.out.println("ksPass=" + ksPass);
 				System.out.println("ksAlias=" + ksAlias);
 				
 				System.out.println("\nEdit this file to customize startup behavior");
 				
 				
 			} else {
 				String curUser=java.lang.System.getProperty("user.name") ;
 				if (!curUser.equals("root")){
 					System.out.println("Install mode must be run as root.");
 					System.exit(1);
 				}
 				
 				if (!new File(logFile).isAbsolute()){
 					logFile = new File(wrFile.toURI().resolve("WEB-INF/" + logFile)).toString();
 				}
 				File templateFile = new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/linux/init_script")
 				);          
 				String initScript=FileUtils.readFileToString(templateFile)
 				.replaceAll("\\{webctx\\}",webctx)      
 				.replaceAll("\\{server\\}",serverName)
 				.replaceAll("\\{user\\}",user)
 				.replaceAll("\\{webroot\\}",webroot)
 				.replaceAll("\\{javahome\\}",javaHome)
 				.replaceAll("\\{logfile\\}",logFile)
 				.replaceAll("\\{port\\}",new Integer(port).toString())
 				.replaceAll("\\{sslPort\\}",new Integer(sslPort).toString())
 				.replaceAll("\\{keystore\\}",keystore)
 				.replaceAll("\\{ksPass\\}",ksPass)
 				.replaceAll("\\{ksAlias\\}",ksAlias);
 				
 				File scriptFile =new File(
 					wrFile.toURI().resolve("WEB-INF/myna/install/" + serverName)
 				);          
 				
 				FileUtils.writeStringToFile(scriptFile,initScript);
 				
 				if (new File("/etc/init.d").exists()){
 					
 					exec("chown  -R "+user+" "+webroot);
 					exec("chown root " +scriptFile.toString());
 					exec("chmod 700 " +scriptFile.toString());
 					exec("cp " +scriptFile.toString() +" /etc/init.d/");
 					
 					System.out.println("\nInit script '/etc/init.d/" + serverName +"' created with the following settings:\n");
 				} else{
 					System.out.println("\nInit script '" + scriptFile +"' created with the following settings:\n");
 				}
 				
 				System.out.println("user=" + user);
 				System.out.println("memory=256MB");
 				System.out.println("server="+ serverName);
 				System.out.println("context=" +webctx);
 				System.out.println("port=" + port);
 				System.out.println("myna_home=" + webroot);
 				System.out.println("logfile=" + logFile);
 				
 				System.out.println("sslPort=" +sslPort);
 				System.out.println("keyStore=" + keystore);
 				System.out.println("ksPass=" + ksPass);
 				System.out.println("ksAlias=" + ksAlias);
 				
 				
 				System.out.println("\nEdit this file to customize startup behavior");
 			} 
 		}
 	}
 	public static boolean exec(String cmd) throws Exception{
 		int exitVal = -1;
 		try
 		{            
 			Runtime rt = Runtime.getRuntime();
 			Process proc = rt.exec(new String[] {"/bin/bash", "-c", cmd});
 			
 			OutputHandler err = new 
 			OutputHandler(proc.getErrorStream(), cmd);            
 			err.start();
 			
 			OutputHandler out = new 
 			OutputHandler(proc.getInputStream(), cmd);
 			out.start();
 			
 						
 			
 			exitVal = proc.waitFor();
 			
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 			
 		}
 		return (exitVal == 0);
 	}
 	public static void upgrade(File wrFile) throws Exception
 	{
 		System.out.println("Installing/upgrading Myna in '"+wrFile.toString()+"'...");
 		wrFile.mkdirs();
 		File web_inf =  new File(wrFile.toURI().resolve("WEB-INF"));
 		boolean isUpgrade = false;
 		File backupDir = null;
 		if (web_inf.exists()){
 			
 			String dateString = new java.text.SimpleDateFormat("MM-dd-yyyy_HH.mm.ss.S").format(new Date());
 			String backupBase = 	"WEB-INF/upgrade_backups/backup_" + dateString;
 			backupDir = new File(wrFile.toURI().resolve(backupBase));
 			backupDir.mkdirs();
 			isUpgrade=true;
 			System.out.println("Backups stored in " + backupDir);
			//backup entire /myna folder because we're wiping it out
			FileUtils.copyDirectory(
				new File(wrFile.toURI().resolve("myna")), 
				new File(backupDir.toURI().resolve("myna"))
			);
			FileUtils.deleteDirectory(new File(wrFile.toURI().resolve("myna")));
 		}
 		
 		if (isJar){
 			String jarFilePath = classUrl.substring(
 				classUrl.indexOf(":")+1,
 				classUrl.indexOf("!")
 			);
 			File jarFile = new File(new java.net.URL(jarFilePath).toURI());
 			ZipFile zipFile= new ZipFile(jarFile);
 			
 			for (ZipEntry entry :  java.util.Collections.list(zipFile.entries())){
 				;
 				File outputFile = new File(
 					wrFile.toURI().resolve(java.net.URLEncoder.encode(entry.getName(),"UTF-8"))
 				);
 				File backupFile = null;
 				if (isUpgrade){
 					backupFile= new File(
 						backupDir.toURI().resolve(java.net.URLEncoder.encode(entry.getName(),"UTF-8"))
 					);
 				}
 				if(entry.isDirectory()) {
 					outputFile.mkdirs();
 					if (isUpgrade) backupFile.mkdirs();
 				} else {
 					if (isUpgrade && outputFile.exists()){
 						
 						java.io.InputStream sourceIS = zipFile.getInputStream(entry);
 						java.io.InputStream targetIS = FileUtils.openInputStream(outputFile);
 						boolean isSame =IOUtils.contentEquals(sourceIS,targetIS);
 						sourceIS.close();
 						targetIS.close();
 						
 						if (isSame
 							|| entry.toString().equals("index.html")
 							|| entry.toString().equals("application.sjs")
 							|| entry.toString().equals("WEB-INF/classes/general.properties")
 							|| entry.toString().startsWith("WEB-INF/myna/ds")
 						){
 							continue;
 						} else { 
 							System.out.println("...backing up " + entry);
 							FileUtils.copyFile(outputFile, backupFile, true) ;
 							//outputFile.copyTo(backupFile);
 							//fusebox.upgradeLog("Backup: " + backupFile);
 						}
 						
 						
 					} 
 					java.io.InputStream is = zipFile.getInputStream(entry);
 					java.io.OutputStream os = FileUtils.openOutputStream(outputFile);
 					IOUtils.copyLarge(is,os);
 					is.close();
 					os.close();
 					
 				}
 			}
 			zipFile.close();
 			//FileUtils.deleteDirectory()
 			
 			System.out.println("Done unpacking.");
 		}
 		 
 	}
 }
