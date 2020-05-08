 package net.worldoftomorrow.nala.ni;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 public class Updater {
 	
 	private int currentMajor = 0;
 	private int currentMinor = 0;
 	private int currentBuild = 0;
 	private int latestMajor = 0;
 	private int latestMinor = 0;
 	private int latestBuild = 0;
 	private String recurl = "http://www.worldoftomorrow.net/noitem/latest.html";
 	private String devurl = "http://www.worldoftomorrow.net/noitem/latest_dev.html";
 	
 	public Updater(String currentVersion){
 		this.currentMajor = Integer.valueOf(currentVersion.split("\\.")[0]);
 		this.currentMinor = Integer.valueOf(currentVersion.split("\\.")[1]);
 		this.currentBuild = Integer.valueOf(currentVersion.split("\\.")[2]);
 		
 		try{
 			URL site;
 			if(Configuration.pluginChannel().equalsIgnoreCase("main")){
 				site = new URL(recurl);
 			}
 			if(Configuration.pluginChannel().equalsIgnoreCase("dev")){
 				site = new URL(devurl);
 			} else {
 				site = new URL(recurl);
 			}
 			URLConnection conn = site.openConnection();
 			//Fake the request to make it look like it is coming from a browser since my server doesn't like Java connecting :P
 			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
 			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String latest = in.readLine();
 			
 			this.latestMajor = Integer.valueOf(latest.split("\\.")[0]);
 			this.latestMinor = Integer.valueOf(latest.split("\\.")[1]);
 			this.latestBuild = Integer.valueOf(latest.split("\\.")[2]);
 			
 			in.close();
 		} catch(MalformedURLException e){
 			e.printStackTrace();
 		} catch (IOException e){
 			e.printStackTrace();
 		}
 	}
 	
 	public String getLatest(){
 		return this.latestMajor + "." + this.latestMinor + "." + this.latestBuild;
 	}
 	
 	public boolean isLatest(){
 		if(currentMajor >= latestMajor && currentMinor >= latestMinor && currentBuild >= latestBuild){
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
