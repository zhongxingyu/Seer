 package gov.nih.nci.ncicb.cadsr.common.util;
 
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.ApplicationServiceLocator;
 import gov.nih.nci.ncicb.cadsr.common.servicelocator.spring.ApplicationServiceLocatorImpl;
 import gov.nih.nci.ncicb.cadsr.common.util.logging.Log;
 import gov.nih.nci.ncicb.cadsr.common.util.logging.LogFactory;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.util.ResourceBundle;
 
 public class CDEBrowserParams
 {
     private static Log log = LogFactory.getLog(CDEBrowserParams.class.getName());
       //This is used monitor application mode
     public static String mode ="";
     //TODO has to be moved to read from Spring Application Context
     private static ApplicationServiceLocator appServiceLocator= new ApplicationServiceLocatorImpl();
     //String sbrextDSN = "";
     //String sbrDSN = "";
     String xmlDownloadDir = "";
     String xmlPaginationFlag = "no";
     String xmlFileMaxRecord;
     String treeURL = "";
     String evsSources = "";
     String showFormsAlphebetical ="no";    
     String excludeTestContext = "no";
     String excludeTrainingContext="no";
     String excludeWorkFlowStatuses = "";
     String excludeRegistrationStatuses = "";
     
     String curationToolUrl = "";
     String nciMetathesaurusUrl="";
     String nciTerminologyServerUrl="";
     String sentinelToolUrl="";
     String adminToolUrl="";
     String umlBrowserUrl="";
     String regStatusCsTree="";
     String csTypeRegStatus="";
     String csTypeContainer="container";
     String sentinelAPIUrl="";
     String formBuilderUrl="";
     String cdeBrowserUrl="";
     String objectCartUrl="";
     String cadsrAPIUrl="";
     
     String contextTest = "";
     String contextTraining = "";
     
     Map evsUrlMap = new HashMap();
     
     static CDEBrowserParams instance;
 
    /**
    *  Read the resource bundle file
    *  propFilename - the specified resource file (fn.properties) without the extension
    *  (e.g., medsurv)
    */
    private CDEBrowserParams()
    {
    }
 
     public String getXMLDownloadDir(){
       return xmlDownloadDir;
     }
     public static CDEBrowserParams getInstance(){
       if (instance == null ) {
         try
         {
           getDebugInstance();
           log.debug("Using debug properties file");
           mode="DEBUG MODE";
           return instance;
         }
         catch (NoSuchElementException nse)
         {
           log.error("Cannot find property"+nse);
           throw nse;
         }
         catch (Exception e)
         {        
         }
         Properties properties = appServiceLocator.findCDEBrowserService().getApplicationProperties(Locale.US);
         instance = new CDEBrowserParams();
         instance.initAttributesFromProperties(properties);
         log.debug("Using database for properties");
       }
       return instance;
     }
     
    public static void reloadInstance(){
       instance = null;
       getInstance();
    }
 
    public static CDEBrowserParams getToolInstance(String toolName)
    {
        Properties properties = appServiceLocator.findCDEBrowserService().getApplicationProperties(Locale.US, toolName);
        String browseurl = properties.getProperty("CDEBrowser_URL");
        if (browseurl.contains("/CDEBrowser/"))
        {
     	   browseurl = browseurl.replace("/CDEBrowser/","");
     	   properties.put("CDEBrowser_URL", browseurl);
        }
 
        instance = new CDEBrowserParams();
        instance.initAttributesFromProperties(properties);
        
 	   return instance;
    }
    
     public static CDEBrowserParams getDebugInstance(){
       if (instance == null ) {
           ResourceBundle b = ResourceBundle.getBundle("cdebrowser", java.util.Locale.getDefault());
           Properties properties = new Properties();
 
             for (Enumeration e = b.getKeys() ; e.hasMoreElements() ;) {
                  String key = (String)e.nextElement();
                 if(key!=null)
                 {
                 log.debug(" Get CDEBrowser.property = "+ key );
                 properties.setProperty(key,b.getString(key));   
                 }
             }
 
           
         instance = new CDEBrowserParams();
         instance.initAttributesFromProperties(properties);         
       }
       return instance;
     }
     
     public static void reloadInstance(String userName){
         Properties properties = appServiceLocator.findCDEBrowserService().reloadApplicationProperties(Locale.US,userName);
         instance = new CDEBrowserParams();
         instance.initAttributesFromProperties(properties);
     }
     public String getTreeURL() {
       return treeURL;
     }
     public String getXMLPaginationFlag(){
       return xmlPaginationFlag;
     }
     public String getXMLFileMaxRecords() {
       return xmlFileMaxRecord;
     }
   
   public void setEvsUrlMap(Map evsUrlMap)
   {
    this.evsUrlMap = evsUrlMap;
   }
 
   public void setEvsUrlMap(Properties bundle,String evsSourcesArr)
   {
         try
         {
             String[] urls = StringUtils.tokenizeCSVList(evsSourcesArr);
             for(int i=0; i<urls.length;i++)
             {
               String key  = urls[i];
               String value = bundle.getProperty(key);
               if(evsUrlMap==null)
                 evsUrlMap = new HashMap();
               evsUrlMap.put(key,value);
             }
         }
         catch (java.util.MissingResourceException mre) 
         {
             log.error("Error getting init parameters, missing resource values");
             log.error("EVS Url not mapped correctly");
             log.error(mre.getMessage(), mre);
             System.exit(-1);
         }
         catch (Exception e)
         {
             log.error("Exception occurred", e);
             System.exit(-1);
         }
   }
   public Map getEvsUrlMap()
   {
     return evsUrlMap;
   }
   
   public void setShowFormsAlphebetical(String showFormsAlphebetical)
   {
     this.showFormsAlphebetical = showFormsAlphebetical;
   }
 
 
   public String getShowFormsAlphebetical()
   {
     return showFormsAlphebetical;
   }
   
   
   public void setExcludeTestContext(String excludeTestContext)
   {
     this.excludeTestContext = excludeTestContext;
   }
 
 
   public String getExcludeTestContext()
   {
     return excludeTestContext;
   }
   
   public void setExcludeTrainingContext(String excludeTrainingContext)
   {
     this.excludeTrainingContext = excludeTrainingContext;
   }
 
 
   public String getExcludeTrainingContext()
   {
     return excludeTrainingContext;
   }  
 
   public String getExcludeWorkFlowStatuses()
   {
     return excludeWorkFlowStatuses;
   }
 
   public void setExcludeWorkFlowStatuses(String excludeWorkFlowStatuses)
   {
     this.excludeWorkFlowStatuses = excludeWorkFlowStatuses;
   }
 
   public String getExcludeRegistrationStatuses()
   {
     return excludeRegistrationStatuses;
   }
 
   public void setExcludeRegistrationStatuses(String excludeRegistrationStatuses)
   {
     this.excludeRegistrationStatuses = excludeRegistrationStatuses;
   }
 
 
   public void setCurationToolUrl(String curationToolUrl)
   {
     this.curationToolUrl = curationToolUrl;
   }
 
 
   public String getCurationToolUrl()
   {
     return curationToolUrl;
   }
 
 
   public void setNciMetathesaurusUrl(String nciMetathesaurusUrl)
   {
     this.nciMetathesaurusUrl = nciMetathesaurusUrl;
   }
 
 
   public String getNciMetathesaurusUrl()
   {
     return nciMetathesaurusUrl;
   }
 
 
   public void setNciTerminologyServerUrl(String nciTerminologyServerUrl)
   {
     this.nciTerminologyServerUrl = nciTerminologyServerUrl;
   }
 
 
   public String getNciTerminologyServerUrl()
   {
     return nciTerminologyServerUrl;
   }
 
 
   public void setSentinelToolUrl(String sentinelToolUrl)
   {
     this.sentinelToolUrl = sentinelToolUrl;
   }
 
 
   public String getSentinelToolUrl()
   {
     return sentinelToolUrl;
   }
 
 
   public void setAdminToolUrl(String adminToolUrl)
   {
     this.adminToolUrl = adminToolUrl;
   }
 
 
   public String getAdminToolUrl()
   {
     return adminToolUrl;
   }
   
   private void initAttributesFromProperties(Properties properties)
   {
         // read the init parameters from the resource bundle
         int index = 0;
         try
         {
                      
             xmlDownloadDir = properties.getProperty("XML_DOWNLOAD_DIR");
             index++;
             xmlPaginationFlag = properties.getProperty("XML_PAGINATION_FLAG");
             index++;
             xmlFileMaxRecord = properties.getProperty("XML_FILE_MAX_RECORDS");
             index++;
             treeURL = properties.getProperty("TREE_URL");
             index++;
             evsSources = properties.getProperty("EVS_URL_SOURCES");
             index++;
             setEvsUrlMap(properties,evsSources);
             showFormsAlphebetical = properties.getProperty("SHOW_FORMS_ALPHEBETICAL");
             index++;            
             excludeTestContext = properties.getProperty("EXCLUDE_TEST_CONTEXT_BY_DEFAULT");
             index++;
             excludeTrainingContext = properties.getProperty("EXCLUDE_TRAINING_CONTEXT_BY_DEFAULT");
             index++;            
             excludeWorkFlowStatuses = properties.getProperty("EXCLUDE_WORKFLOW_BY_DEFAULT");
             index++;
             excludeRegistrationStatuses = properties.getProperty("EXCLUDE_REGISTRATION_BY_DEFAULT");
             index++;      
             //Temporary Page for Admin Tool
             adminToolUrl = "/CDEBrowser/common/adminRedirection.html";//properties.getProperty("AdminTool_URL");
             index++;      
             curationToolUrl = properties.getProperty("CURATION_URL");
             index++;
            nciMetathesaurusUrl = properties.getProperty("NCI_METATHESAURUS_URL");
             index++;            
            nciTerminologyServerUrl = properties.getProperty("NCI_TERMINOLOGY_SERVER_URL");
             index++;
             sentinelToolUrl = properties.getProperty("SENTINEL_URL");
             index++;             
             regStatusCsTree = properties.getProperty("CS_TYPE_REGISTRATION_STATUS");
             index++;             
             csTypeRegStatus = properties.getProperty("REG_STATUS_CS_TREE");
             index++;      
             csTypeContainer = properties.getProperty("CS_TYPE_CONTAINER");
             index++;      
             sentinelAPIUrl = properties.getProperty("SENTINEL_URL");
             index++;                
             umlBrowserUrl = properties.getProperty("UMLBrowser_URL");
             index++;      
             formBuilderUrl = properties.getProperty("FormBuilder_URL");
             index++;      
             cdeBrowserUrl = properties.getProperty("CDEBrowser_URL");
             index++;      
             objectCartUrl = properties.getProperty("ObjectCartAPI_URL");
             index++;      
             cadsrAPIUrl = properties.getProperty("CADSRAPI_URL");
             index++;      
             contextTest = properties.getProperty("BROADCAST.EXCLUDE.CONTEXT.00.NAME");
             index++;      
             contextTraining = properties.getProperty("BROADCAST.EXCLUDE.CONTEXT.01.NAME");
             index++;      
             log.info("Loaded Properties"+properties);
                         
         } 
         catch (java.util.MissingResourceException mre) 
         {
             log.error("Error getting init parameters, missing resource values");
             log.error("Property missing index: " + index);
             log.error(mre.getMessage(), mre);
         }
         catch (Exception e)
         {
             log.error("Exception occurred when loading properties", e);
         }    
   }
 
    public String getRegStatusCsTree() {
       return regStatusCsTree;
    }
 
    public String getCsTypeRegStatus() {
       return csTypeRegStatus;
    }
 
     public String getCsTypeContainer() {
        return csTypeContainer;
     }
 
     public void setSentinelAPIUrl(String sentinelAPIUrl)
     {
         this.sentinelAPIUrl = sentinelAPIUrl;
     }
 
     public String getSentinelAPIUrl()
     {
         return sentinelAPIUrl;
     }
 
    public void setUmlBrowserUrl(String umlBrowserUrl) {
       this.umlBrowserUrl = umlBrowserUrl;
    }
 
    public String getUmlBrowserUrl() {
       return umlBrowserUrl;
    }
 
 	public String getFormBuilderUrl() {
 		return formBuilderUrl;
 	}
 	
 	public void setFormBuilderUrl(String formBuilderUrl) {
 		this.formBuilderUrl = formBuilderUrl;
 	}
 
 	public String getCdeBrowserUrl() {
 		return cdeBrowserUrl;
 	}
 
 	public void setCdeBrowserUrl(String cdeBrowserUrl) {
 		this.cdeBrowserUrl = cdeBrowserUrl;
 	}
 	
 	public String getObjectCartUrl() {
 		return (objectCartUrl == null) ? "" : objectCartUrl;
 	}
 
 	public void setObjectCartUrl(String objectCartUrl) {
 		this.objectCartUrl = objectCartUrl;
 	}
 
 	public String getCadsrAPIUrl() {
 		return (cadsrAPIUrl == null) ? "" : cadsrAPIUrl;
 	}
 
 	public void setCadsrAPIUrl(String cadsrAPIUrl) {
 		this.cadsrAPIUrl = cadsrAPIUrl;
 	}
 
 	public String getContextTest()
 	{
 		return contextTest;
 	}
 	
 	public String getContextTraining()
 	{
 		return contextTraining;
 	}
 }
