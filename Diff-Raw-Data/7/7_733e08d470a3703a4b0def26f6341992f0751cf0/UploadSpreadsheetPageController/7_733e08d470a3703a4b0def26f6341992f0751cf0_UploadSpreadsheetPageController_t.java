 package org.openmrs.module.conceptmanagementapps.page.controller;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.multipart.MultipartFile;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openmrs.module.appui.UiSessionContext;
 import org.openmrs.ui.framework.page.PageModel;
 
 public class UploadSpreadsheetPageController {
 	
 	protected final Log log = LogFactory.getLog(this.getClass());
 	
 	public void post(@RequestParam("spreadsheet") MultipartFile spreadsheetFile) {
	System.out.println("-----------------------------test the log info");

 		BufferedReader br = null;
 		String line = "";
 		try {
 			br = new BufferedReader(new InputStreamReader(spreadsheetFile.getInputStream()));
 			int linenumber = 1;
 			boolean header = true;
 			String mappings = "";
 			boolean foundMapping = false;
 			while ((line = br.readLine()) != null) {
 				if (!header) {
 					if (!foundMapping && !line.endsWith("\"")) {
 						line = line + "\"";
 						foundMapping = true;
 					} else if (foundMapping && !line.endsWith("\"")) {
 						line = "\"" + line + "\"";
 						foundMapping = true;
 					} else if (foundMapping && line.endsWith("\"")) {
 						line = "\"" + line;
 						foundMapping = false;
 					}
 					
 				}
 				String[] tokens = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
 				for (String t : tokens) {
 					switch (linenumber) {
						case 1: System.out.println("-----------------------------test the log info");
 							log.info("jenn remove 1 " + t);
							
 							linenumber++;
 							break;
 						case 2:
 							log.info("jenn remove 2 " + t);
 							
 							linenumber++;
 							break;
 						case 3:
 							log.info("jenn remove 3 " + t);
 							
 							linenumber++;
 							break;
 						case 4:
 							log.info("jenn remove 4 " + t);
 							
 							linenumber++;
 							break;
 						case 5:
 							log.info("jenn remove 5 " + t);
 							
 							linenumber++;
 							break;
 						case 6:
 							log.info("jenn remove 6 " + t);
 							
 							linenumber++;
 							break;
 						case 7:
 							log.info("jenn remove 7 " + t);
 							
 							linenumber++;
 							break;
 						case 8:
 							log.info("jenn remove 8 " + t);
 							
 							linenumber++;
 							break;
 						case 9:
 							log.info("jenn remove 9 " + t);
 							
 							linenumber++;
 							break;
 						case 10:
 							log.info("jenn remove 10 " + t);
 							if (!header) {
 								if (t.startsWith("\"") && t.length() > 2) {
 									mappings += t;
 								} else if (t.endsWith("\"") && t.length() > 2) {
 									mappings += t;
 									linenumber = 1;
 									mappings = "";
 								} else {
 									linenumber = 1;
 									mappings = "";
 								}
 							} else {
 								header = false;
 								linenumber = 1;
 							}
 							break;
 						default:
 							linenumber++;
 							log.error("jenn remove if not needed: invalid  : " + linenumber);
 							break;
 					
 					}
 				}
 				
 			}
 			
 		}
 		catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 		}
 		finally {
 			if (br != null) {
 				try {
 					br.close();
 				}
 				catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	public void get(UiSessionContext sessionContext, PageModel model) throws Exception {
 		
 	}
 	
 }
