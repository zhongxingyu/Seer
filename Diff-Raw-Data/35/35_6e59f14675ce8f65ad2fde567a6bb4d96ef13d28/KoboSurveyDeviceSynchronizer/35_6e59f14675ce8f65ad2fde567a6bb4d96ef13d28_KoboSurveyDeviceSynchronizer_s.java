 /* 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.oyrm.kobo.postproc;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.SwingWorker;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 import org.oyrm.kobo.postproc.constants.Constants;
 import org.w3c.dom.Document;
 
 
 
 /**
  * The KoboSurveyDeviceSynchronizer reads data from a specified directory and
  * copies it onto a specified storage directory in order to maintain copies of
  * the XML locally.
  * 
  * The application of this class is intended to permit data to be harvested from
  * devices mounted to the local file system and stored in a local file storage
  * directory.
  * 
  * @author Gary Hendrick
  * 
  */
 public class KoboSurveyDeviceSynchronizer extends SwingWorker<Void, Void> {
 	private static Logger logger = Logger.getLogger("Formhub Uploader");
 	private static FileHandler lh;
 	private static Formatter lf;
 	private String DeviceID;
 	static {
 		try {
 			/*lh = new FileHandler(System.getProperty("user.home")
 					+ File.separator + Constants.CONFIG_STORAGEDIR
 					+ File.separator + "kobo.log", true);
 			lf = new SimpleFormatter();*/
 		} catch (SecurityException e) {
 			e.printStackTrace();
 		}
 		
 		//logger.setLevel(Level.parse(System.getProperty(Constants.PROPKEY_LOGGING_LEVEL)));
 		logger.setLevel(Level.ALL);
 		// TODO:
 		// NullPointerException
 		// when
 		// launched
 		// by itself
 	}
 
 	static final String[] typeName = { "none", "Element", "Attr", "Text",
 			"CDATA", "EntityRef", "Entity", "ProcInstr", "Comment", "Document",
 			"DocType", "DocFragment", "Notation", };
 
 	private Document documentSource;
 	private Document documentExisting;
 	private File readdir, storedir,zipdir,storeodk;
 	private String zipPathName;
 	private Integer nSynced;
 	 
 	
 	public KoboSurveyDeviceSynchronizer(){
 		
 	}
 
 	public KoboSurveyDeviceSynchronizer(File source, File storage, String DeviceID) {
 		super();
 		this.DeviceID = DeviceID;
 		String DevicePathName = storage.toString().concat("/"+DeviceID);
 		zipPathName = storage.toString().concat("/"+DeviceID+".zip");
 		
 		boolean success = (new File(DevicePathName)).mkdir();
 		//success = (new File(storage.getAbsolutePath().concat("odk"))).mkdir();
 		
 		File DeviceStorage = new File(DevicePathName);
         
 		if (!source.exists() || !source.isDirectory()) {
 			throw new IllegalArgumentException(
 					"Usage: SimpleSAXSurveyReader requires a valid java.io.File() argument representing a directory");
 		}
 		readdir = source;
 		storedir = DeviceStorage;
 		storeodk = new File(DeviceStorage.getAbsolutePath().concat("/odk"));
 		
 	}
 
 	@Override
 	public Void doInBackground() throws Exception {
 		logger.entering(getClass().getName(), "doInBackground()");
 		setProgress(1);
 		try {
 			processDirectory();
 		} catch (Exception ex) {
 			logger.warning(ex.toString());
 			throw ex;
 		} finally {
 			setProgress(100);
 		}
 		logger.exiting(getClass().getName(), null);
 		return null;
 	}
 
 	@Override
 	public void done() {
 		logger.entering(getClass().getName(), "done()");
 		logger.exiting(getClass().getName(), "done()");
 	}
 
 	/**
 	 * Read the storage directory and source directory and sync new files
 	 * 
 	 * @throws Exception
 	 */
 	private void processDirectory() throws Exception {
 		logger.entering(getClass().getName(), "processDirectory");
 		try {
 			if (!storedir.exists())
 			{
 				throw new IOException("Storage Directory, "
 						+ storedir.getAbsolutePath() + ", Does Not Exist");
 			}
 			if (!readdir.exists())
 			{
 				throw new IOException("Source Directory, "
 						+ readdir.getAbsolutePath() + ", Does Not Exist");
 			}
 			copyDirectory(readdir, storeodk);
 			CreateZipFile zips = new CreateZipFile();
 			zips.zipFolder(storedir.toString(), zipPathName);
 			deleteDir(storedir);
 			
 			
 		} catch (IOException ioex) {
 			logger.warning(ioex.toString());
 			throw ioex;
 		} catch (Exception e) {
 			logger.warning(e.toString());
 			throw e;
 		}
 		logger.exiting(getClass().getName(), "processDirectory");
 		return;
 	}
 	
 	public static boolean deleteDir(File dir) {
 	    if (dir.isDirectory()) {
 	        String[] children = dir.list();
 	        for (int i=0; i<children.length; i++) {
 	            boolean success = deleteDir(new File(dir, children[i]));
 	            if (!success) {
 	                return false;
 	            }
 	        }
 	    }
 
 	    // The directory is now empty so delete it
 	    return dir.delete();
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean SendFiles(String username, String Location) throws MalformedURLException,IOException
 	{
 		    try{
 		    	File f = new File(Location);
 	            HttpClient client = new DefaultHttpClient();  
 	            String postURL = "http://formhub.org/"+username +"/bulk-submission";
 	            HttpPost post = new HttpPost(postURL); 
 	            FileBody bin = new FileBody(f);
 	            MultipartEntity submission = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		           submission.addPart("zip_submission_file", bin);
 		            post.setEntity(submission); 
 		            HttpResponse response = client.execute(post);  
 		            HttpEntity resEntity = response.getEntity();  
 		            if (resEntity != null) {    
 		             }
 	            }catch (Exception e){ e.printStackTrace(); return false;}
 			return true;
 	}
 	
 	@SuppressWarnings("deprecation")
 	public boolean BulkUpload(String username, String Location) throws MalformedURLException,IOException
 	{
 		try{
 			//Getting the logfile location
 			boolean append = true;
 			FileHandler fh = new FileHandler(Location.concat("/UpdateLogFile.log"),append);  
             logger.addHandler(fh);
             
           //logger.setLevel(Level.ALL);  
             SimpleFormatter formatter = new SimpleFormatter();  
             fh.setFormatter(formatter);  
            
            
			File folder = new File(Location.concat("/Downloaded Files"));
 			InputStream in = null;
 			OutputStream out = null;
 			int survey = 0;
 			for (String fileName : folder.list()) {
 					if(fileName.endsWith(".zip"))
 					{
 						    File f = new File(Location.concat("/Downloaded Files/"+fileName));
 						    HttpClient client = new DefaultHttpClient();
 						    String postURL = "http://formhub.org/"+username +"/bulk-submission";
 						    HttpPost post = new HttpPost(postURL); 
 							FileBody bin = new FileBody(f);
 							MultipartEntity submission = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 							submission.addPart("zip_submission_file", bin);
 							post.setEntity(submission); 
 							HttpResponse response = client.execute(post); 
 							HttpEntity resEntity = response.getEntity();
 							String s = fileName;
 							java.util.StringTokenizer st = new java.util.StringTokenizer(s, "_");
							
 							logger.info("USER:"+username+ "  " +"DeviceID:"+ st.nextToken() + "   " +  EntityUtils.toString(response.getEntity()));
 							if (resEntity != null) {
 								//return resEntity.toString();
			               }
 							File dir = new File(Location.concat("/Synchonized Files"));
 							boolean shiftFiles = f.renameTo(new File(dir, f.getName()));
 							survey ++;
 					}
 				
 			}
 			
		}catch (Exception e){ e.printStackTrace();  return false;}
 		return true;
 			
 	}
 	
 	
 	/*public boolean BulkUpload(String username, String Location) throws MalformedURLException,IOException
 	{
 		try{
 			File folder = new File(Location.concat("/Downloaded Files"));
 			InputStream in = null;
 			OutputStream out = null;
 			int survey = 0;
 			for (String folderDate : folder.list()) {
 				File zipfiles = new File(Location.concat("/Downloaded Files/2012-03-22/"));
 				for (String fileName : zipfiles.list())
 				{
 					
 					if(fileName.endsWith(".zip"))
 					{
 						    File f = new File(Location.concat("/Downloaded Files/2012-03-22/" + fileName));
 						    HttpClient client = new DefaultHttpClient();  
 							String postURL = "http://192.168.1.78:8000/"+username +"/bulk-submission";
 							HttpPost post = new HttpPost(postURL); 
 							FileBody bin = new FileBody(f);
 							MultipartEntity submission = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 							submission.addPart("zip_submission_file", bin);
 							post.setEntity(submission); 
 							HttpResponse response = client.execute(post);  
 							HttpEntity resEntity = response.getEntity();  
 							if (resEntity != null) {    
 			               }
 							//copy the file into synchronized folder and delete the rest 
 							boolean makeFolder = (new File(Location.concat("/Synchonized Files/"+folderDate))).mkdir();
 							File dir = new File(Location.concat("/Synchonized Files/"+ folderDate));
 							boolean shiftFiles = f.renameTo(new File(dir, f.getName()));
 							survey ++;
 					}
 				}
 			}
 			
 		}catch (Exception e){ e.printStackTrace();  return false;}
 		return true;
 			
 	}*/
 	
 	public boolean Unmount(String Directory)
 	{
 		/*String osName = System.getProperty("os.name");
 		
 		try
 		{
 			Runtime.getRuntime().exec("umount -f \"" + Directory+ "\"");
 		}
 		catch(Exception e){return false;}*/
 		return true;
 	}	
 	public int getLengthOfTask() {
 		return 100;
 	}
 	
 	public void copyDirectory(File sourceLocation , File targetLocation)
 		    throws IOException {
 		        
 		        if (sourceLocation.isDirectory()) {
 		            if (!targetLocation.exists()) {
 		                targetLocation.mkdir();
 		            }
 		            
 		            String[] children = sourceLocation.list();
 		            for (int i=0; i<children.length; i++) {
 		                copyDirectory(new File(sourceLocation, children[i]),
 		                        new File(targetLocation, children[i]));
 		            }
 		        } else {
 		            
 		            InputStream in = new FileInputStream(sourceLocation);
 		            OutputStream out = new FileOutputStream(targetLocation);
 		            
 		            // Copy the bits from instream to outstream
 		            byte[] buf = new byte[1024];
 		            int len;
 		            while ((len = in.read(buf)) > 0) {
 		                out.write(buf, 0, len);
 		            }
 		            in.close();
 		            out.close();
 		        }
 		    }
 
 
 	
 	
 	/**
 	 * Have gone to GUI execution, this may need updating
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			//System.out.println("hello");
 			if (args.length != 2) {
 				throw new IllegalArgumentException(
 						"Usage: "
 								+ KoboSurveyDeviceSynchronizer.class.getName()
 								+ " <Source Directory> <Destination Directory>"
 								+ "\n\tSimpleDOMSurveyReader requires a source directory"
 								+ " name argument along with a destination File directory");
 			}
 			KoboSurveyDeviceSynchronizer handler = new KoboSurveyDeviceSynchronizer(
 					new File(args[0]), new File(args[1]), args[2] );
 			//KoboSurveyDeviceSynchronizer handler = new KoboSurveyDeviceSynchronizer();
 			handler.execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
