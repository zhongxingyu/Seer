 package it.italiangrid.wnodes.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import it.italiangrid.wnodes.exception.WnodesPortletException;
 import it.italiangrid.wnodes.utils.WnodesConfig;
 import org.apache.log4j.Logger;
 
 
 public class SSHKeyCheck implements Runnable {
 
 	private static final Logger log = Logger.getLogger(SSHKeyCheck.class);
 
 	public void run() {
 		
 		log.error("Check SSH Key");
 		
 		try {
 			String[] users = WnodesConfig.getProperties("active.users").split(";");
 			
 			log.error("Check ssh keys for users: " + WnodesConfig.getProperties("active.users"));
 			
 			for(String user: users){
 				
 				log.error("user: " + user);
 				
 				String keyFile = System.getProperty("java.io.tmpdir") + "/users/" + user
 						+ "/id_rsa";
 				
 				String keyPubFile = System.getProperty("java.io.tmpdir") + "/users/" + user
 						+ "/id_rsa.pub";
 				File privateKey =  new File(keyFile);
 				
 				log.error(keyFile);
 				log.error(keyPubFile);
 				
 				if(!privateKey.exists()){
 					log.error("Private key for user "+user + " already deleted.");
 					
				}else{
				
					if(keyIsExpired(keyFile)&&keyIsExpired(keyPubFile)){
						
						privateKey.delete();
						
						log.error("Private key for user "+user + " successfully deleted.");
						
					}else{
						log.error("Keys for user "+user + " valid.");
					}
 				}
 				
				
 			}
 			
 		} catch (WnodesPortletException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		} catch (ParseException e) {
 			log.error(e.getMessage());
 			e.printStackTrace();
 		}
 		
 		return;
 			
 	}
 	
 	private boolean keyIsExpired(String filename) throws IOException, ParseException, WnodesPortletException{
 		
 		File file = new File(filename);
 		
 		if(file.exists()){
 		
 			String[] cmd = {"stat", "-c", "%x", filename};
 			
 			Process p = Runtime.getRuntime().exec(cmd);
 			
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 	
 			BufferedReader output = new BufferedReader(
 					new InputStreamReader(stdout));
 			String line = null;
 			String time = "";
 	
 			while ((line = output.readLine()) != null) {
 				log.error("[Stdout] " + line);
 				time = line;
 				
 			}
 			output.close();
 			time = time.substring(0, 23)+time.substring(29);
 			log.error("Sono qui!! -->" + time + "<--");
 													   //yyyy-MM-dd HH:mm:ss.SSS
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS ZZZZ");
 			
 			Date accessDate=sdf.parse(time);
 			log.error("now: " +getDate(0));
 			log.error("parsed data: " + accessDate);
 			
 			BufferedReader brCleanUp = new BufferedReader(new InputStreamReader(
 					stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 					log.error("[Stderr] " + line);
 			}
 			
 			String expireTime = WnodesConfig.getProperties("ssh.expire.time");
 			
 			int before = Integer.parseInt(expireTime)*-1;
 			
 			log.error("compare with: " +getDate(before));
 			
 			log.error("result: "+ accessDate.compareTo(getDate(before)));
 			
 			if(accessDate.compareTo(getDate(before))<=0)
 				return true;
 		
 		}else{
 			
 			log.error("File: " + filename + " don't exist.");
 		}
 		return false;
 	}
 	
 	private Date getDate(int hours){
 		Calendar cal = new GregorianCalendar();
 		cal.add(Calendar.HOUR_OF_DAY, hours);
 		return cal.getTime();
 	}
 
 }
