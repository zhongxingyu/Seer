 package org.esgf.srm.utils;
 
 import java.io.File;
 
 import org.esgf.srm.SRMResponse;
 
 public class SRMUtils {
 
     public static boolean srm_disabled = true;
     
     //properties file for postgres
     public static String SRM_PROPERTIES_FILE_LOCATION = "/esg/config/srm.properties";
 
     //default locations
    public static String searchAPI = "http://localhost/esg-search/search?";
     public static String srmAPIURL = "http://esg.ccs.ornl.gov:8080/esgf-srm/service/srmrequest?";
     
     
     //the complexity of the returned script (attached in email)
     public static String SCRIPT_COMPLEXITY = "basic";
     
     //the type of datastore used for teh srm cache table
     public static String DB_TYPE = "postgres";
 
     //testing params
     public static int TEST_NUMFILE_LIMIT = 3;
     public static boolean numFileTest = true;
     
     //simulation or production
     public static String ENVIRONMENT = "Production";
     
     
 
     //printing flags for srmproxy controller
     public static boolean srmproxydebugflag = true;
     
     //printing flags for srm cache store operations
     public static boolean updateAllSRMEntriesForDatasetIdFlag = false;
     
     //printing flag for input params
     public static boolean inputParamsFlag = false;
 
     //printing flag for simulation output
     public static boolean simulationOutputParamsFlag = false;
     
     //parameters after initialization of dataset id, file ids, and file urls
     public static boolean afterInitializationFlag = false;
     
     //debug flag for the existance of whether or not a table exists
     public static boolean tableExistsFlag = false;
     
     //email debug flags
     public static boolean initialEmailTextflag = false;
     public static boolean confirmationEmailTextflag = false;
     
     //update of the bestman cache table
     public static boolean updateStatusFlag = true;
     
     //print the IDS in getSolrParams helper function
     public static boolean printIDsFlag = false; 
     
     //email send flags
     public static boolean initialEmailSendflag = false;
     public static boolean confirmationEmailSendflag = false;
     
     public static boolean timeParamsFlag = false;
     
     //script generation flags
     public static boolean scriptGenFlag = false;
     
     //in filetransformercontroller for http translations
     public static boolean httpFileFlag = false;
     
     //in postgres bestman path controller
     public static boolean postgresBestmanPathFlag = false;
 
     //debug flag to check when a properties file is being opened (in PostgresSRMCacheStore constructor)
     public static boolean postgresCacheStoreFlag = false;
 
     
     public static boolean useDefaultEmail = true;
     
     
     //attachment file names
     public static String GUC_ATTACHMENT_NAME = "globus-url-copy.sh";
     public static String WGET_ATTACHMENT_NAME = "wget.sh";
     
 
     public static String THREDDS_DATAROOT = "/thredds/fileServer/esg_srm_dataroot";
     
     public static String SRM_CACHE_REPLACE = "/SRMTemp/";
     
     public static String expiration = "86400000";
     public static String success_message = "success";
     public static String failure_message = "failure";
     
     public static String db_name = "esgcet";
     //public static String db_name = "first_db";
     public static String table_name = "esgf_security.srm_entries";
     //public static String table_name = "srm_entries";
     //public static String valid_user = "first_user";
     public static String valid_user = "dbsuper";
     public static String valid_password = "xxxxx";
     
     
     
     
     
     
     public static String [] gridftp2httpArr(String [] grid_ftp_arr) {
         String [] http_arr = new String[grid_ftp_arr.length];
 
         for(int i=0;i<grid_ftp_arr.length;i++) {
             http_arr[i] = gridftp2http(grid_ftp_arr[i]);
         }
         
         return http_arr;
 
     }
     
     
     public static String gridftp2http(String gsiftp) {
         String http = "";
         
         http = transformServerName(gsiftp);
         
         http = http.replace("gsiftp", "http");
         
         System.out.println("\n\n\n\n\tPRe http: " + http);
         
         http = http.replace("//lustre/esgfs/SRMTemp", THREDDS_DATAROOT);
         
         System.out.println("\tPoST http: " + http);
         
         return http;
     }
     
     public static String extractServerName(String url) {
         
         String serverName = null;
         
         serverName = url.substring(0, url.indexOf("?"));
         
         return serverName;
     }
     
     public static String transformServerName(String url) {
         return url.replace("esg2-sdnl1.ccs.ornl.gov", "esg.ccs.ornl.gov");
     }
     
     
     
     
     public static String [] replaceCacheNames(String [] url) {
         String [] replaced_urls = new String [url.length];
         
         for(int i=0;i<replaced_urls.length;i++){
             replaced_urls[i] = replaceCacheName(url[i]);
         }
         
         return replaced_urls;
     }
     
     public static String replaceCacheName(String url) {
         return url.replace("/SRM/", SRM_CACHE_REPLACE);
     }
     
     public static String stripIndex(String url) {
         
         int endIndex = url.indexOf("|");
         if(endIndex == -1) {
             return url;
         } 
         else {
             return url.substring(0,endIndex);
         }
     }
     
     public static String extractFileName(String fileName) {
         String newFileName = "";
         String tempStr = "";
         int counter = fileName.length()-1;
         char ch = fileName.charAt(counter);
         while(ch != '/') {
             counter--;
             tempStr += ch;
             ch = fileName.charAt(counter);
         }   
         
         for(int i=tempStr.length()-1;i>=0;i--) {
             newFileName += tempStr.charAt(i);
         }
         
         return newFileName;
     }
     
     public static String [] extractFileNames(String [] fileNames) {
         String [] newFileNames = new String[fileNames.length];
         
         for(int i=0;i<fileNames.length;i++) {
             newFileNames[i] = extractFileName(fileNames[i]);
         }
         
         return newFileNames;
     }
     
     //input
     //srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-09.nc
     //output
     //gsiftp://esg2-sdnl1.ccs.ornl.gov//lustre/esgfs/SRM/shared/V.0.0-505553807/t341f02.FAMIPr.cam2.h0.1978-09.nc
     public static SRMResponse simulateSRM(String [] inputFiles) {
         SRMResponse srm_response = new SRMResponse();
         
         String [] outputFiles = new String [inputFiles.length];
         
         
         for(int i=0;i<inputFiles.length;i++) {
             String tempFile = inputFiles[i].replace("srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://", "file:///");
             //tempFile = transformServerName(tempFile);
             
             File f = new File(tempFile);
             String fileName = f.getName();
             
             //String outputFile = "gsiftp://esg.ccs.ornl.gov:2811//lustre/esgfs/SRM/" + fileName;
 
             String outputFile = "gsiftp://esg.ccs.ornl.gov:2811//lustre/esgfs/" + fileName;
             outputFiles[i] = outputFile;
         }
         
         srm_response.setResponse_urls(outputFiles);
         srm_response.setMessage("Doin fine");
         
         return srm_response;
     }
     
     
     //input
     //srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-09.nc
     //output
     //gsiftp://esg2-sdnl1.ccs.ornl.gov//lustre/esgfs/SRM/shared/V.0.0-505553807/t341f02.FAMIPr.cam2.h0.1978-09.nc
     
     public static String stripSRMServer(String srm_url) {
         String sourceUrl = srm_url;
         
         String tempFile = srm_url.replace("srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://", "file:///");
         
         File f = new File(tempFile);
         String fileName = f.getName();
         
         sourceUrl = "gsiftp://esg.ccs.ornl.gov:2811//lustre/esgfs/SRM/" + fileName;
         
         sourceUrl = replaceCacheName(sourceUrl);
         
         return sourceUrl;
     }
     
     public static void main(String [] args) {
         //String url = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?" +
         //        "SFN=mss://esg2-sdnl1.ccs.ornl.gov/proj/cli049/UHRGCS/ORNL/CESM1" +
         //        "/t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1979-01.nc";
         
         
         String url = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?" +
                      "SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/" +
                      "t341f02.FAMIPr/atm/hist/t341f02.FAMIPr.cam2.h0.1978-09.nc";
         
         String [] srm_urls = new String [1];
         
         String str = stripSRMServer(url);
         System.out.println(str);
         
         str = gridftp2http(str);
         System.out.println(str);
         
         
         /*
         srm_urls[0] = url;
         
         SRMResponse srm_response = new SRMResponse();
         
         srm_response = simulateSRM(srm_urls);
         
         String [] response_urls = replaceCacheNames(srm_response.getResponse_urls());
         
         for(int i=0;i<response_urls.length;i++) {
             System.out.println("resp: " + i + " " + response_urls[i]) ;
         }
         */
         
     }
     
     
     
     
     
     
     
     //srmproxy controller constants
     public static String INPUT_FILTERED = "false";
     public static String INPUT_PEERSTR = "localhost";
     public static String INPUT_TECHNOTESTR = "undefined;undefined";
     public static String INPUT_FQPARAMSTR = ";offset=0;query=*;latest=true;replica=false;";
     public static String INPUT_INITIALQUERY = "true";
     public static String INPUT_FILE_COUNTER = "10";
 
     public static String INPUT_OPENID = "https://esg.ccs.ornl.gov/esgf-idp/openid/jfharney";
     public static String RETURN_TYPE = "http";
     
     public static String DEFAULT_INPUT_FILE_ID = "N/A";
     public static String DEFAULT_INPUT_FILE_URL = "N/A";
     public static String DEFAULT_INPUT_DATASET_ID = "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov";
     public static String DEFAULT_INPUT_FILTERED = "false";
     public static String DEFAULT_INPUT_TYPE = "Dataset";
     public static String DEFAULT_INPUT_PEERSTR = "localhost";
     public static String DEFAULT_INPUT_TECHNOTESTR = "undefined;undefined";
     public static String DEFAULT_INPUT_FQPARAMSTR = ";offset=0;query=*;latest=true;replica=false;";
     public static String DEFAULT_INPUT_INITIALQUERY = "true";
     public static String DEFAULT_INPUT_FILE_COUNTER = "10";
 
     public static String INPUT_DATASET_ID = "ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov";
     public static String INPUT_CONSTRAINTS = ";offset=0;query=snow;latest=true;replica=false;";
     public static String INPUT_DATASET_TYPE = "Dataset";
     public static String INPUT_DATASET_FILE_ID = "";//"ornl.ultrahighres.CESM1.t85f09.F1850p.v1.m2.h1.0002-06-20-00000.nc|esg2-sdnl1.ccs.ornl.gov";
     public static String INPUT_DATASET_FILE_URL = "srm://esg2-sdnl1.ccs.ornl.gov:46790/srm/v2/server?SFN=mss://esg2-sdnl1.ccs.ornl.gov//proj/cli049/UHRGCS/ORNL/CESM1/t85f09.F1850p/atm/hist/t85f09.F1850p.cam2.h1.0002-06-20-00000.nc";
     public static final String INPUT_OPEN_ID = "https://esg.ccs.ornl.gov/esgf-idp/openid/jfharney";
     public static String INPUT_TYPE_DATASET = "Dataset";
     public static String INPUT_TYPE_FILE = "File";
     public static String INPUT_FILE_FILE_ID = "N/A";
     public static String INPUT_FILE_FILE_URL = "N/A";
     
     public static String INPUT_SCRIPT_TYPE = "WGET";
 
     
     
     
     public static final String DEFAULT_EMAIL_ADDR = "jfharney@gmail.com";
     
     
     
     
 }
