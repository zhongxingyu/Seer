 package gov.nih.nci.rembrandt.web.struts2.action;
 
 import gov.nih.nci.caintegrator.analysis.messaging.IdGroup;
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.application.analysis.gp.GenePatternPublicUserPool;
 import gov.nih.nci.caintegrator.application.lists.ListItem;
 import gov.nih.nci.caintegrator.application.lists.UserListBeanHelper;
 import gov.nih.nci.caintegrator.dto.de.GeneIdentifierDE;
 import gov.nih.nci.caintegrator.dto.de.InstitutionDE;
 import gov.nih.nci.caintegrator.enumeration.AnalysisType;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.security.EncryptionUtil;
 import gov.nih.nci.caintegrator.security.PublicUserPool;
 import gov.nih.nci.caintegrator.security.UserCredentials;
 import gov.nih.nci.caintegrator.service.task.GPTask;
 import gov.nih.nci.caintegrator.util.idmapping.IdMapper;
 import gov.nih.nci.rembrandt.cache.RembrandtPresentationTierCache;
 import gov.nih.nci.rembrandt.dto.lookup.LookupManager;
 import gov.nih.nci.rembrandt.queryservice.validation.DataValidator;
 import gov.nih.nci.rembrandt.util.RembrandtConstants;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 import gov.nih.nci.rembrandt.web.helper.GroupRetriever;
 import gov.nih.nci.rembrandt.web.helper.InsitutionAccessHelper;
 import gov.nih.nci.rembrandt.web.struts2.form.GpIntegrationForm;
 import gov.nih.nci.rembrandt.web.struts2.form.UIFormValidator;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.ServletRequestAware;
 import org.genepattern.client.GPClient;
 import org.genepattern.webservice.Parameter;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 //public class GPIntegrationAction extends DispatchAction {
 public class GPIntegrationAction extends ActionSupport implements ServletRequestAware {
 	HttpServletRequest servletRequest;
 	GpIntegrationForm gpIntegrationForm;
 	
     private IdMapper idMappingManager;
 	private static Logger logger = Logger.getLogger(GPIntegrationAction.class);
     private Collection<GeneIdentifierDE> geneIdentifierDECollection;
     private String gpPoolString = ":GP30:RBT";
    
     public String setup()
     throws Exception {
     	String sID = getServletRequest().getHeader("Referer");
     	
     	// prevents Referer Header injection
     	if ( sID != null && sID != "" && !sID.contains("rembrandt")) {
     		return "failure";
     	}
 
         /*setup the defined Disease query names and the list of samples selected from a Resultset*/
         GroupRetriever groupRetriever = new GroupRetriever();
         getGpIntegrationForm().setExistingGroupsList(groupRetriever.getClinicalGroupsCollection(getServletRequest().getSession()));         
 //        saveToken(request);
         
         return "success";
     }
     
     /**
      * Method submittal
      * 
      * @param ActionMapping
      *            mapping
      * @param ActionForm
      *            form
      * @param HttpServletRequest
      *            request
      * @param HttpServletResponse
      *            response
      * @return ActionForward
      * @throws Exception
      */
     @SuppressWarnings("unchecked")
 	public String submit()
             throws Exception {
     	
 /*    	if (!isTokenValid(request)) {
 			return mapping.findForward("failure");
 		}
 */		
 		List<String> errors = validateFormData();
 		if (errors.size() > 0) {
 			for (String error : errors)
 				addActionError(error);
 			
			return "success";
 		}
     	
     	   String sessionId = getServletRequest().getSession().getId();
            HttpSession session = getServletRequest().getSession();
            
        	   String[] patientGroups = getGpIntegrationForm().getSelectedGroups();
        	   
      	   List<String> filePathList = extractPatientGroups(getServletRequest(), session, patientGroups, null,"ge");
 		
 		String platformName = getGpIntegrationForm().getArrayPlatform();
 	
 		//Now get the R-binary file name:
 		
 		String r_fileName = null;
 		String a_fileName = null;
 		
 	
        if(platformName != null && platformName.equalsIgnoreCase(ArrayPlatformType.AFFY_OLIGO_PLATFORM.toString())) {
        
 			r_fileName = System.getProperty("gov.nih.nci.rembrandt.affy_data_matrix");
 			a_fileName = System.getProperty("gov.nih.nci.rembrandt.affy_data_annotation");		
 		 
 	   }
 	
 		
        runGpTask(getServletRequest(), getGpIntegrationForm(), session, filePathList, r_fileName, a_fileName);
        getServletRequest().setAttribute("gpTaskType", "Regular");
        
 //       resetToken(request);
        
 	   return "viewJob";
 
 					
    		
 
     }
 
 	protected void runGpTask(HttpServletRequest request,
 			GpIntegrationForm gpForm, HttpSession session,
 			List<String> filePathList, String r_fileName, String a_fileName)
 			throws Exception {
 		//		*** RUN TASK ON THE GP SERVER
 				String tid = "209";
 				String gpModule =  System.getProperty("gov.nih.nci.caintegrator.gp.modulename");						
 				String rembrandtUser = null;
 				GPClient gpClient = null;
 				String ticketString = "";
 				String analysisResultName = gpForm.getAnalysisResultName();
 				
 				String gpserverURL = System.getProperty("gov.nih.nci.caintegrator.gp.server")!=null ? 
 						(String)System.getProperty("gov.nih.nci.caintegrator.gp.server") : "localhost:8080"; //default to localhost
 						try {
 							//*	
 							
 						 	   rembrandtUser = retreiveRembrandtUser(request, session, rembrandtUser);
 								
 								ticketString = retrieveTicketString(rembrandtUser, gpserverURL);
 					            
 								String password = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.password");
 								gpClient = new GPClient(gpserverURL, rembrandtUser, password);
 								int size = filePathList.size();
 								Parameter[] par = new Parameter[filePathList.size() + 3 + 3];
 								int currpos= 1;
 								for (int i = 0; i < filePathList.size(); i++){
 									par[i] = new Parameter("input.filename" + currpos++, filePathList.get(i));
 								}
 								par[--currpos] = new Parameter("project.name", "rembrandt");
 		
 								//r_fileName = "'/usr/local/genepattern/resources/DataMatrix_ISPY_306cDNA_17May07.Rda'";
 								par[++currpos] = new Parameter("array.filename", r_fileName);
 								par[++currpos] = new Parameter("annotation.filename", a_fileName);
 									
 								par[++currpos] = new Parameter("analysis.name", analysisResultName);
 		
 								//always just 2
 								par[++currpos] = new Parameter("output.cls.file",analysisResultName+".cls");
 								par[++currpos] = new Parameter("output.gct.file",analysisResultName+".gct");
 								
 								//JobResult preprocess = gpServer.runAnalysis(gpModule, par);
 								int nowait = gpClient.runAnalysisNoWait(gpModule, par);
 		
 								tid = String.valueOf(nowait);
 								//LSID = urn:lsid:8080.root.localhost:genepatternmodules:20:2.1.7
 								request.setAttribute("jobId", tid);
 								request.setAttribute("gpStatus", "running");
 								session.setAttribute("genePatternServer", gpClient);
 								request.setAttribute("genePatternURL", ticketString);
 								request.getSession().setAttribute("gptid", tid);
 								request.getSession().setAttribute("gpUserId", rembrandtUser);
 								request.getSession().setAttribute("ticketString", ticketString);
 								GPTask gpTask = createGpTask(tid, analysisResultName);
 								RembrandtPresentationTierCache _cacheManager = ApplicationFactory.getPresentationTierCache();
 								_cacheManager.addNonPersistableToSessionCache(request.getSession().getId(), "latestGpTask",(Serializable) gpTask); 
 								
 							} catch (Exception e) {
 								StringWriter sw = new StringWriter();
 								PrintWriter pw = new PrintWriter(sw);
 								e.printStackTrace(pw);
 								logger.error(sw.toString());
 								logger.error(gpModule + " failed...." + e.getMessage());
 								throw new Exception(e.getMessage());
 							}
 	}
 
 	protected String retrieveTicketString(String rembrandtUser, String gpserverURL)
 			throws UnsupportedEncodingException {
 		String ticketString;
 		String encryptKey = System.getProperty("gov.nih.nci.caintegrator.gp.desencrypter.key");
 		String urlString = EncryptionUtil.encrypt(rembrandtUser+ gpPoolString, encryptKey);
 		urlString = URLEncoder.encode(urlString, "UTF-8");
 		ticketString = gpserverURL+"gp?ticket="+ urlString;
 		
 		logger.info(ticketString);
 		URL url;
 		try {
 			url = new java.net.URL(ticketString);
 			URLConnection conn = url.openConnection();
 			final int size = conn.getContentLength();
 			logger.info(Integer.toString(size));
 
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 		}
 		return ticketString;
 	}
 
 	protected String retreiveRembrandtUser(HttpServletRequest request,
 			HttpSession session, String rembrandtUser) {
 		UserCredentials credentials = (UserCredentials)request.getSession().getAttribute(RembrandtConstants.USER_CREDENTIALS);
 				       
 			if(credentials!= null) {
 				rembrandtUser= credentials.getUserName();
 			}
 				     
 			String publicUser = System.getProperty("gov.nih.nci.caintegrator.gp.publicuser.name");
 				
 			if(rembrandtUser==null)	{
 				rembrandtUser=publicUser;
 			}
 			
 				//Check to see the user is already created otherwise create one.
 			if (rembrandtUser.equals(publicUser)){
 				String gpUser = (String)session.getAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME);
 				if (gpUser == null){
 					PublicUserPool pool = GenePatternPublicUserPool.getInstance();
 					gpUser = pool.borrowPublicUser();
 					session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_NAME, gpUser);
 					session.setAttribute(GenePatternPublicUserPool.PUBLIC_USER_POOL, pool);
 				}
 				rembrandtUser = gpUser;
 			}
 		return rembrandtUser;
 	}
 
 	protected GPTask createGpTask(String tid, String analysisResultName) {
 		GPTask gpTask = new GPTask(tid, analysisResultName, FindingStatus.Running);
 		return gpTask;
 	}
 
 	protected List<String> extractPatientGroups(HttpServletRequest request,
 			HttpSession session, String[] patientGroups, AnalysisType analysisType, String taskType) throws Exception,
 			IOException {
 		List<String> idStringList = new ArrayList<String>();
    	       List<List<String>> allStringList = new ArrayList<List<String>>();
            List<String> fileNameList = new ArrayList<String>();
            List<String> reportIdStringList = new ArrayList<String>();
 
 
         
 
        	   //create a sampleGroup array with the length of selected  patient groups, it needs to be >2
        	   // SampleGroup objects will be used to store patient group names and specimen ids
        	   SampleGroup[] sampleGroup = new SampleGroup [patientGroups.length];            
        	   
        	   
            Collection<InstitutionDE> accessInstitutions = InsitutionAccessHelper.getInsititutionCollection(session);
     	
            UserListBeanHelper helper = new UserListBeanHelper(request.getSession().getId());
            Set<String> patientIdset = new HashSet<String>();
            
          	   for(int j=0; j<patientGroups.length;j++) {
         	   Set<String> idSet = null;        	   
         	   
         	   String[] uiDropdownString =patientGroups[j].split("#");
         	   
                String myClassName = uiDropdownString[0];           
                String myValueName = uiDropdownString[1];              
                sampleGroup[j] = new SampleGroup();  
                
                // add the selected pt group names such "GBM", "MIXED" to the sampleGroup object
                sampleGroup[j].setGroupName(myValueName);
                patientIdset.clear();
                
                List<ListItem> listItemts = helper.getUserList(myValueName).getListItems();            	
                
                for (Iterator i = listItemts.iterator(); i.hasNext(); ) {
    				ListItem item = (ListItem)i.next();
    				String id = item.getName();
    				patientIdset.add(id);
    				
    			  }               
             
             	
            
            if(patientIdset != null && patientIdset.size()>0) {
         	   
         	   // need to convert pt dids to the specimen ids
         		List<String> specimenNames = LookupManager.getSpecimenNames(patientIdset, accessInstitutions);  
         		List<String> validspecimenNames = null;
         		if(taskType!= null && taskType == "ge"){
                 //Validate that samples has GE data
         			validspecimenNames = DataValidator.validateSampleIdsForGEData(specimenNames);
         		} else if(taskType!= null && analysisType != null && taskType == "cp"){
         			 //Validate that samples has CP data
         			validspecimenNames = DataValidator.validateSampleIdsForCnSegData(specimenNames, analysisType);
         		} else if(taskType!= null && analysisType != null && taskType == "ge-cp"){
         			 //Validate that samples has GE & CP data
         			validspecimenNames = DataValidator.validateSampleIdsForGEData(specimenNames);
         			List<String> validspecimenNames2 = DataValidator.validateSampleIdsForCnSegData(specimenNames, analysisType);
         			if(validspecimenNames != null && validspecimenNames2 != null){
         				validspecimenNames.addAll(validspecimenNames2);
         			}else if (validspecimenNames == null && validspecimenNames2 != null){
         				validspecimenNames = validspecimenNames2;
         			}
         		} 
         		
         		if(validspecimenNames != null){
         			   for (String specimenName: validspecimenNames ) {
         				   // add specimenName to the samplegroup with the corresponding selected pt group
         				   sampleGroup[j].add(specimenName);	        	   			   
         		   			
         		          }// end of for
                     }// end of if
         		
         		idStringList.add(getIdsAsDelimitedString(sampleGroup[j], "\t"));
         		
               }	
         		
            
            }
           
           
         
            allStringList.add(idStringList);
    		   fileNameList.add("labIdsFile");
    	
 			reportIdStringList.add("reporter=NONE");
 			allStringList.add(reportIdStringList);
 			fileNameList.add("reproterIdsFile");
 			logger.info("Have no gene or report list...");
 	
    		  
    		  
    		
    		  
    	
 	
 	
 		//Now let's write them to files
 		List<String> filePathList = new ArrayList<String>();
 		writeGPFile(filePathList, allStringList, fileNameList);
 		return filePathList;
 	} 
     
    
 		
 	private String getIdsAsDelimitedString(String listName, Set<String> idSet,	String token){
 				StringBuffer sb = null;
 				
 				sb = new StringBuffer(replaceSpace(listName) + "=");
 				int size = idSet.size();
 				int count = 0;
 				for (String id : idSet) {
 					sb.append(id);
 					if (++count < size) {
 						sb.append(token);
 					}
 				}
 				if (sb.length() == 0)
 					return "";
 				return sb.toString();
 		    }
 		    
     
 	 private String getIdsAsDelimitedString(IdGroup idGroup, String token){
 			StringBuffer sb = new StringBuffer(replaceSpace(idGroup.getGroupName()) + "=");
 			for (Iterator i = idGroup.iterator(); i.hasNext(); ) {
 			  //sb.append(DOUBLE_QUOTE+(String)i.next()+DOUBLE_QUOTE);
 			  sb.append((String)i.next());
 			  if (i.hasNext()) {
 			    sb.append(token);
 			  }
 			}
 			if (sb.length() == 0)
 				return "";
 			return sb.toString();
 	    }
 	 
     private String replaceSpace(String text){
     	return text.replaceAll(" ", "_");
     }
 
     private void writeGPFile(List<String> filePathList, 
     		List<List<String>> allIdStringList,
     		List<String> fileNameList)throws IOException{
     	int count = 0; 
     	String fileName = null;
     	String fileExtension = ".txt";
         File tempdir = new File(getTempDir(), "ConvertToGctAndClsFile");
         tempdir.mkdirs();
         File[] currentFiles = tempdir.listFiles();
         
     	for (int i = 0; i < currentFiles.length; i++){
     		
     		if (currentFiles[i].isFile()){
     			currentFiles[i].delete();
     		}
     	}
 		for (List<String> list : allIdStringList){
 			if (!list.isEmpty()){
 				fileName = fileNameList.get(count);	
 
 				File idFile = new File(tempdir, fileName + fileExtension);
 				FileWriter idFw = new FileWriter(idFile);
 				for (String ids : list){
 					idFw.write(ids);
 					idFw.write("\n");
 				}
 				idFw.close();
 				filePathList.add(idFile.getAbsolutePath());
 			}
 			else
 				filePathList.add("");
 			count++;
 		}
     }
 
     protected File getTempDir() throws IOException {
         File tempdir = File.createTempFile("foo", ".temp");
     	//File tempdir =File.createTempFile("foo", ".libdir", new File("C:\\temp\\applet"));
         tempdir.delete();
         return tempdir.getParentFile();
     }
         
 	public HttpServletRequest getServletRequest() {
 		return servletRequest;
 	}
 
 	public void setServletRequest(HttpServletRequest servletRequest) {
 		this.servletRequest = servletRequest;
 	}
 
 	public GpIntegrationForm getGpIntegrationForm() {
 		if( gpIntegrationForm == null ) {
 			gpIntegrationForm = new GpIntegrationForm();
 		}
 		return gpIntegrationForm;
 	}
 
 	public void setGpIntegrationForm(GpIntegrationForm gpIntegrationForm) {
 		this.gpIntegrationForm = gpIntegrationForm;
 	}
 	
 	public GpIntegrationForm getForm() {
 		return  getGpIntegrationForm();
 	}
 
 	
     public List<String> validateFormData() {
 		List<String> errors = new ArrayList<String>();
 
        // Analysis name cannot be blank
         errors.addAll(UIFormValidator.validateQueryName(getGpIntegrationForm().getAnalysisResultName(), errors));
        
        // Validate group field
         errors.addAll(UIFormValidator.validateSelectedGroups(getGpIntegrationForm().getSelectedGroups(),2, errors));
        
         return errors;
     }	
 }
 
