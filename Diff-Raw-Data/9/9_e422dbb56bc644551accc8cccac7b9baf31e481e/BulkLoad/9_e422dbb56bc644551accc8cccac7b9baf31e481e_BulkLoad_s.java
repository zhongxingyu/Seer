 package com.marklogic.training;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStream;
 import java.util.Arrays;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.marklogic.client.document.XMLDocumentManager;
 import com.marklogic.client.io.InputStreamHandle;
 
 public class BulkLoad {
 
 	private static final Logger logger = LoggerFactory.getLogger(BulkLoad.class);
 	
	public static void load(String path) {
 		try {
 			MarkLogicConnection conn = new MarkLogicConnection();
 			
 			try {	
 				// create a manager for XML documents
 				XMLDocumentManager docMgr = conn.getClient().newXMLDocumentManager();				
 												
 				// create a dir listing for input path
 				File dirList = new File(path);
 				String [] list = dirList.list();
 				Arrays.sort(list);
 				logger.info("Found "+list.length+" songs");
 				for (int i=0; i<list.length; i++) {
 					// read document from file 
 					InputStream docStream = new BufferedInputStream(new FileInputStream(path + File.separator + list[i]) );
 			
 					// create a handle on the content
 					InputStreamHandle contentHandle = new InputStreamHandle(docStream);
					String uid = path+list[i];
 					
 					//logger.info("Writing document "+uid +" to db"  );
 					
 					// write to the db
 					docMgr.write(uid,contentHandle);
 				}
 
 				logger.info("wrote "+list.length+" songs to the db");
 				
 							    
 			} catch (Exception e) {
 				logger.error("Exception : " + e.toString() );
 			} finally { 
 				// don't leave a hanging connection in the case an exception is thrown in this try block
 				if (conn.getClient() != null)
 					conn.getClient().release();
 			}
 		
 		} catch(Exception e) {
 			logger.error("Exception :" + e.toString() );
 		}
 		
 	}
 		
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		//set up document id to be read from the database
 		String pathToExercises = "/Users/jcrean/Training/DevelopingMarkLogicApplications/Exercises";
 		String pathToFile = "mls-developer/unit12/top-songs-source/songs";
		load(pathToExercises+File.separator+pathToFile);
 	}
 
 }
