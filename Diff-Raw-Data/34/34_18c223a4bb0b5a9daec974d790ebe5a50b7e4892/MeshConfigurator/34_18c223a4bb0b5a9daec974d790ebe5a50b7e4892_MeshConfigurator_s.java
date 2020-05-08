 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.bnl.racf.ps.dashboard.meshconfig;
 
 import gov.bnl.racf.ps.dashboard.db.data_objects.*;
 import gov.bnl.racf.ps.dashboard.db.data_store.PsDataStore;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsHostManipulator;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsMatrixManipulator;
 import gov.bnl.racf.ps.dashboard.db.object_manipulators.PsObjectCreator;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.hibernate.Session;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 
 /**
  *
  * @author tomw
  */
 public class MeshConfigurator {
 
     private String mode;
     private PrintWriter out;
     private Session session;
     private JSONObject json;
     // cloud to be configured by current json
     private PsCloud cloud;
     private List<PsMatrix> listOfMatrices = new ArrayList<PsMatrix>();
 
     public void setSession(Session session) {
         this.session = session;
     }
 
     public void setOut(PrintWriter out) {
         this.out = out;
     }
 
     public String getMode() {
         return mode;
     }
 
     public void setMode(String mode) {
         this.mode = mode;
     }
 
     public void setJson(JSONObject jsonObject) {
         this.json = jsonObject;
     }
 
     public JSONObject getJson() {
         return json;
     }
 
     public void configure() throws Exception {
         //throw new UnsupportedOperationException("Not yet implemented");
         configureCloud();
         this.out.println("keys json=" + json2keys(json));
         configureMatrices();
 
         this.out.println("assign matrices to cloud<BR>");
         this.assignMatricesToCloud();
 
     }
 
     private PsCloud createCloudIfNotExists(String cloudName) {
         PsCloud cloud = PsDataStore.getCloudByName(this.session, cloudName);
         if (cloud == null) {
             cloud = PsObjectCreator.createNewCloudWithGivenName(session, cloudName);
             this.out.println("Created new cloud " + cloudName + "<BR>");
         } else {
             this.out.println("Known cloud " + cloudName + "<BR>");
         }
         return cloud;
     }
 
     private PsSite createSiteIfNotExists(String siteName) {
         PsSite site = PsDataStore.getSiteByName(this.session, siteName);
         if (site == null) {
             site = PsObjectCreator.createNewSiteWithGivenName(session, siteName);
             this.out.println("Created new site " + siteName + "<BR>");
         } else {
             this.out.println("Known site " + siteName + "<BR>");
         }
         return site;
     }
 
     private PsHost createHostIfNotExists(String hostName) {
         PsHost host = PsDataStore.getHostByName(this.session, hostName);
         if (host == null) {
             host = PsObjectCreator.createNewHostWithGivenName(session, hostName);
             this.out.println("Created new host " + hostName + "<BR>");
         } else {
             this.out.println("Known host " + hostName + "<BR>");
         }
         return host;
     }
 
     private PsMatrix createMatrixIfNotExists(String matrixName, PsServiceType matrixType) {
         PsMatrix matrix = PsDataStore.getMatrixByNameAndType(this.session, matrixName, matrixType);
         if (matrix == null) {
             matrix = PsObjectCreator.createNewMatrix(session, matrixType, matrixName);
         }
         return matrix;
     }
 
     private String json2keys(JSONObject jsonObject) {
         String result = "";
         Iterator it = jsonObject.keySet().iterator();
         while (it.hasNext()) {
             String key = (String) it.next();
             result = result + " " + key;
         }
         return result;
     }
 
     private boolean isBandwidthHost(JSONObject host) {
         if (host.containsKey("measurement_archives")) {
             JSONArray measurementArchives =
                     (JSONArray) host.get("measurement_archives");
             Iterator archiveIterator = measurementArchives.iterator();
 
             while (archiveIterator.hasNext()) {
                 JSONObject archiveJson = (JSONObject) archiveIterator.next();
                if (archiveJson.containsKey("type")) {
                     String type = (String) archiveJson.get("type");
                     if (type.contains("bwctl")) {
                         return true;
                     }
                 }
             }
 
         }
         return false;
     }
 
     private boolean isLatencyHost(JSONObject host) {
         if (host.containsKey("measurement_archives")) {
             JSONArray measurementArchives =
                     (JSONArray) host.get("measurement_archives");
             Iterator archiveIterator = measurementArchives.iterator();
 
             while (archiveIterator.hasNext()) {
                 JSONObject archiveJson = (JSONObject) archiveIterator.next();
                 if (archiveJson.containsKey("type")) {
                     String type = (String) archiveJson.get("type");
                     if (type.contains("owamp")) {
                         return true;
                     }
                 }
             }
 
         }
         return false;
     }
 
     private void configureCloud() {
         String jsonCloudName = (String) json.get("description");
         this.cloud = this.createCloudIfNotExists(jsonCloudName);
 
         JSONArray jsonOrganisations = (JSONArray) this.json.get("organizations");
         Iterator iter = jsonOrganisations.iterator();
         while (iter.hasNext()) {
             JSONObject organisation = (JSONObject) iter.next();
             //out.println("========================");
             //out.println("organisation description="
             //        + (String) organisation.get("description"));
             JSONArray sites = (JSONArray) organisation.get("sites");
             Iterator sitesIterator = sites.iterator();
             while (sitesIterator.hasNext()) {
                 JSONObject site = (JSONObject) sitesIterator.next();
                 String siteDescription = (String) site.get("description");
                 //out.println("site description = " + siteDescription);
 
                 PsSite currentSite = createSiteIfNotExists(siteDescription);
                 if (this.cloud.containsSite(currentSite)) {
                     //out.println("cloud " + cloud.getName()
                     //        + " contains site " + currentSite.getName());
                 } else {
                     //out.println("cloud " + cloud.getName()
                     //        + " does not contain site " + currentSite.getName());
                     this.cloud.addSite(currentSite);
                     this.out.println("Site " + currentSite.getName() + " added to cloud " + this.cloud.getName() + "<BR>");
                 }
                 //out.println("site keys=" + json2keys(site));
                 if (site.containsKey("hosts")) {
                     JSONArray listOfHostsAtSite = (JSONArray) site.get("hosts");
 
                     Iterator hostsIterator = listOfHostsAtSite.iterator();
                     while (hostsIterator.hasNext()) {
                         JSONObject hostJson = (JSONObject) hostsIterator.next();
 
                        //out.println("host keys=" + json2keys(hostJson));
                         String hostName = (String) ((JSONArray) hostJson.get("addresses")).get(0);
                        //out.println("host info: description=" + hostJson.get("description")
                        //        + " address=" + hostJson.get("addresses"));
                        //if (PsDataStore.getHostByName(this.session, hostName) != null) {
                        //    out.println("This is known host");
                        //} else {
                        //    out.println("This is unknown host");
                        //}
                         PsHost host = createHostIfNotExists(hostName);
 
                         if (isBandwidthHost(hostJson)) {
                            //out.println("This is bandwidth host");
                             PsHostManipulator.addThroughputServices(session, host);
                         }
                         if (isLatencyHost(hostJson)) {
                            //out.println("This is latency host");
                             PsHostManipulator.addLatencyServices(session, host);
                         }
 
 
                         if (currentSite.containsHost(host)) {
                             //out.println("Site " + currentSite.getName() + " contains host " + host.getHostname());
                         } else {
                             currentSite.addHost(host);
                             this.out.println("Host " + host.getHostname() + " added to " + currentSite.getName() + "<BR>");
                         }
                     }
                 } else {
                     this.out.println("site " + siteDescription + " does not contain hosts information<BR>");
                 }
             }
 
         }
         this.out.println("<BR>");
     }
 
     private boolean testIsTraceroute(JSONObject test) {
         if (test.containsKey("parameters")) {
             JSONObject parameters = (JSONObject) test.get("parameters");
             if (parameters.containsKey("type")) {
                 String type = (String) parameters.get("type");
                 if (type != null) {
                     if (type.contains("traceroute")) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean testIsBandwidth(JSONObject test) {
         if (test.containsKey("parameters")) {
             JSONObject parameters = (JSONObject) test.get("parameters");
             if (parameters.containsKey("type")) {
                 String type = (String) parameters.get("type");
                 if (type != null) {
                     if (type.contains("bwctl")) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean testIsLatency(JSONObject test) {
         if (test.containsKey("parameters")) {
             JSONObject parameters = (JSONObject) test.get("parameters");
             if (parameters.containsKey("type")) {
                 String type = (String) parameters.get("type");
                 if (type != null) {
                     if (type.contains("owamp")) {
                         return true;
                     }
                 }
             }
         }
         return false;
     }
 
     private boolean membersTypeIsMesh(JSONObject members) {
         if (members.containsKey("type")) {
             String type = (String) members.get("type");
             if ("mesh".equals(type)) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
     private PsServiceType testType(JSONObject test) throws Exception {
         String typeId = "";
         if (testIsLatency(test)) {
             typeId = PsServiceType.LATENCY;
         }
         if (testIsBandwidth(test)) {
             typeId = PsServiceType.THROUGHPUT;
         }
         if (testIsTraceroute(test)) {
             typeId = PsServiceType.TRACEROUTE;
         }
         PsServiceType serviceType =
                 PsDataStore.getServiceType(this.session, typeId);
         if ("".equals(typeId)) {
             throw new Exception("unknown test type for test=" + test.toString());
         }
         return serviceType;
     }
 
     private void configureMatrices() throws Exception {
         JSONArray tests = (JSONArray) this.json.get("tests");
         Iterator testsIter = tests.iterator();
         while (testsIter.hasNext()) {
             JSONObject test = (JSONObject) testsIter.next();
 
             this.out.println("test: description=" + (String) test.get("description"));
             this.out.println("test: parameters=" + (JSONObject) test.get("parameters"));
             this.out.println("test: members=" + (JSONObject) test.get("members"));
             if (testIsLatency(test)) {
                 this.out.println("test is latency");
             }
             if (testIsBandwidth(test)) {
                 this.out.println("test is bandwidth");
             }
             if (testIsTraceroute(test)) {
                 this.out.println("test is traceroute");
             }
             if (!testIsTraceroute(test)) {
                 PsServiceType serviceType = testType(test);
                 String matrixName = (String) test.get("description");
 
                 if (PsDataStore.containsMatrixOfNameAndType(this.session,
                         matrixName,
                         serviceType)) {
                     this.out.println("This is known matrix");
                 } else {
                     this.out.println("This is unknown matrix");
                 }
                 PsMatrix matrix = createMatrixIfNotExists(matrixName, serviceType);
                 this.listOfMatrices.add(matrix);
 
                 JSONObject members = (JSONObject) test.get("members");
                 if (membersTypeIsMesh(members)) {
                     if (members.containsKey("members")) {
                         JSONArray listOfHostNames = (JSONArray) members.get("members");
                         Iterator hostNameIter = listOfHostNames.iterator();
                         while (hostNameIter.hasNext()) {
                             String hostName = (String) hostNameIter.next();
                             PsHost host = PsDataStore.getHostByName(session, hostName);
                             if (host == null) {
                                 throw new Exception("Unknown host " + hostName);
                             } else {
                                 if (matrix.containsHost(host)) {
                                     this.out.println("Matrix contains host " + hostName);
                                 } else {
                                     this.out.println("Matrix does not contain host " + hostName);
                                     PsMatrixManipulator.addHostToMatrix(session, matrix, host);
                                    //matrix.addHost(host);
                                 }
                             }
                         }
                     } else {
                         this.out.println("Test has no hosts associated with it <BR>");
                     }
 
                 } else {
                     throw new Exception("Unsupported test type " + test);
                 }
             }else{
                 this.out.println("Traceroute matrix type is not supported yet <BR>");
             }
 
         }
 
     }
 
     private void assignMatricesToCloud() {
         this.out.println("Assign matrices to clouds<BR>");
         Iterator iter = this.listOfMatrices.iterator();
         while (iter.hasNext()) {
             PsMatrix currentMatrix = (PsMatrix) iter.next();
             if (!this.cloud.containsMatrix(currentMatrix)) {
                 this.out.println("Assign marix " + currentMatrix.getName()
                         + " to cloud " + this.cloud.getName() + "<BR>");
                 this.cloud.addMatrix(currentMatrix);
             } else {
                 this.out.println("Cloud " + this.cloud.getName()
                         + " contains matrix " + currentMatrix.getName() + "<BR>");
             }
         }
     }
 }
