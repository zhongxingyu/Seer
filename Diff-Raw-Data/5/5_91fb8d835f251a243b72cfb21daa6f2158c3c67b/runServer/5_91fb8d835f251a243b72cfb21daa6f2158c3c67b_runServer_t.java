 /**
 * @Author Chris Card
 * This file runs the server program
 */
 
 import java.lang.Runtime;
 import java.io.*;
 
 public class runServer
 {
 	/**
 	* gets the name of the current machine
 	* @return the name of the current machine
 	*/
 	public static String getHost()
 	{
 		String line = "",line2="";
 
 		try{
 			Process p = Runtime.getRuntime().exec("hostname");
 
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 			while ((line = b.readLine()) != null)
 			{
 				line2 = line.replace("\n","").replace("\r","");
 			}
 
 		} catch (Exception e) {
 			System.err.println("Failed to find host name!");
 			e.printStackTrace();
 		}
 		return line2;
 	}
 
 	/**
 	* Gets the path to the current directory
 	* @return the path of the current directory
 	*/
 	public static String getPath()
 	{
 		String line = "",line2="";
 
 		try{
 			Process p = Runtime.getRuntime().exec("pwd");
 
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
 			while ((line = b.readLine()) != null)
 			{
 				line2 = line.replace("\n","").replace("\r","");
 			}
 		} catch (Exception e) {
 			System.err.println("Failed to find path!");
 			e.printStackTrace();
 		}
 		return line2;
 	}
 
 	/**
 	*	The arguments are as follows
 	*     -s <socket> "Provides the socket number"
 	*	  -master (only when instantiating the master server)
 	* 	  -slave (only when instantiating the slave server)
 	*	  -mhost <Master hostname>:<socket> (only used if not the master node)
 	*/
 	public static void main(String[] args)
 	{
 		if (args.length == 0) {
 			String usage = "java runServer -s <socket> [-master | -slave] -mhost <Master hostname>:<socket>\n";
			usage += "\n-s <socket>, [-master | -slave] are required argumnts\n";
			usage += "-mhost <Master hostname>:<socket> is only used with the -slave option";
			System.out.println(usage);
 		}
 		try{
 			String path = getPath();
 			String host = getHost();
 
 			//builds, redirects output and runs the commandline call to the server program
 			ProcessBuilder run = new ProcessBuilder("java","-Djava.rmi.server.codebase=http://"+host+path+"/Compute/compute.jar","-Djava.rmi.server.hostname="+host+".mines.edu","Server.Server",args[0],args[1],args[2],(args.length >= 4 ? args[3] : ""),(args.length == 5 ? args[4] : ""));
 			run.redirectErrorStream(true);
 			Process p = run.start();
 
 			//reads the output of the server program
 			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
 			String line = "";
 
 			while ((line = b.readLine()) != null)
 			{
 				if(line.compareTo("EOF") == 0)
 				{
 					break;
 				}
 				System.out.println(line);
 			}
 						
 		}catch (Exception e){
 			e.printStackTrace();
 		}
 	}
 }
