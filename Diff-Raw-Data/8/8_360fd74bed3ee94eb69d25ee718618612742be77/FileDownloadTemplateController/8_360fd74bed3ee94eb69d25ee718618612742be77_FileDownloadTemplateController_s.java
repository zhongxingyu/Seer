 package org.esgf.datacart;
 
 import java.util.Enumeration;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 @RequestMapping("/solrfileproxy2")
 public class FileDownloadTemplateController {
 
     private final static boolean writeToXMLFile = false;
     private final static String testInitializationFile = "C:\\Users\\8xo\\esgf-web-fe\\docselement.xml";
 
     @SuppressWarnings("unused")
     public static void main(String [] args) {
         final MockHttpServletRequest mockRequest = new MockHttpServletRequest();
         
         String peerStr = "dev.esg.anl.gov;esg-datanode.jpl.nasa.gov";
         String showAll = "false";
         String fqStr = ";offset=0;replica=false";
         String initialQuery = "true";
 
         //String [] id = {"anl.cssef.homme.v1|esg.anl.gov","obs4MIPs.CNES.AVISO.mon.v1|esg-datanode.jpl.nasa.gov"};
         
         String idStr = "anl.cssef.homme.v1|esg.anl.gov;obs4MIPs.CNES.AVISO.mon.v1|esg-datanode.jpl.nasa.gov";
         
         mockRequest.addParameter("peer", peerStr);        
         mockRequest.addParameter("showAll", showAll);
         mockRequest.addParameter("fq", fqStr);
         mockRequest.addParameter("initialQuery", initialQuery);
         mockRequest.addParameter("id", idStr);
         mockRequest.addParameter("fileCounter", "10");
         
         
         FileDownloadTemplateController fc = new FileDownloadTemplateController();
         
         //fc.getDocElement(mockRequest);
         
     }
     
     
     
     
     /**
      * 
      * @param request
      * @return
      */
     @RequestMapping(method=RequestMethod.GET, value="/datacart")
     public @ResponseBody String getDocs(HttpServletRequest request) {
         
         //this.printParameters(request);
         
         
         String peerStr = request.getParameter("peerStr");
         String [] peers = peerStr.split(";");
         
         //get the peer string and convert to an array of peers
         String idStr = request.getParameter("idStr");
         String [] id = idStr.split(";");
 
         
         //WHY THIS? - blanks will always split by one
         if(id.length == 1 && (id[0].equals("") || id[0].equals(" "))) {
             id = null;
         }
 
         //NOTE: ID and PEERS SHOULD BE THE SAME LENGTH!!!!
         
         //get the fq string and convert to an array of peers
         String fqStr = request.getParameter("fqStr");
         String [] fq = fqStr.split(";");
         
         
         String technotes = request.getParameter("technotesStr");
         System.out.println("Technotes: " + technotes);
         
         
         //get the search constraints togggle parameter
         String showAllStr = request.getParameter("showAllStr");
         
         //get the flag denoting whether or not this is an initial Query
         String initialQuery = request.getParameter("initialQuery");
 
         
         //get the fileCounter
         String fileCounter = request.getParameter("fileCounter");
         
         
         //DataCartSolrHandler handler = new DataCartSolrHandler(showAll,fq,initialQuery);
         DataCartDocs docs = new DataCartDocs();
 
         
         //get all the docs (as long as id is not null)
         if(id != null) {
             for(int i=0;i<id.length;i++) {
                 
                 DocElement d = getDocElement(id[i],peers[i],initialQuery,fq,showAllStr,fileCounter);
                 docs.addDocElement(d);
                 
             }
         }
         
         if(writeToXMLFile){
             docs.toFile(testInitializationFile);
         }
         return docs.toJSON();
         
     }
     
     /**
      * 
      * @param request
      * @return
      */
     @SuppressWarnings("unused")
     @RequestMapping(method=RequestMethod.POST, value="/datacart/{datasetId}")
     public @ResponseBody String getDoc(@PathVariable String datasetId,HttpServletRequest request) {
     
         
         String idStr = request.getParameter("idStr");
         
         //System.out.println("\n\n\n\nDATASET ID: " + idStr + "\n\n\n\n");
         
         
         String peerStr = request.getParameter("peerStr");
         //String [] peers = peerStr.split(";");
 
         //NOTE: ID and PEERS SHOULD BE THE SAME LENGTH!!!!
         
         //get the fq string and convert to an array of peers
         String fqStr = request.getParameter("fqStr");
         String [] fq = fqStr.split(";");
         
         
         String technotes = request.getParameter("technotesStr");
         //System.out.println("Technotes: " + technotes);
         
         
         //get the search constraints togggle parameter
         String showAllStr = request.getParameter("showAllStr");
         
         //get the flag denoting whether or not this is an initial Query
         String initialQuery = request.getParameter("initialQuery");
         
 
         //get the fileCounter
         String fileCounter = request.getParameter("fileCounter");
         
         /*
         System.out.println("--------");
         System.out.println("\tpeerStr\t" + peerStr);
         System.out.println("\tfqStr\t" + fqStr);
         System.out.println("\ttechnotesStr\t" + technotes);
         System.out.println("\tshowAllStr\t" + showAllStr);
         System.out.println("\tinitialQuery\t" + initialQuery);
         System.out.println("\tfileCount\t " + fileCounter + "\n");
         */
         
         datasetId = idStr;
         
         DocElement doc = getDocElement(datasetId,peerStr,initialQuery,fq,showAllStr,fileCounter);
         
         //System.out.println(doc.toXML());
         
        
         
         //return "<a>b</a>";
         return doc.toJSON();
     }
     
     
     private DocElement getDocElement(String id,String peer,String initialQuery,String [] fq,String showAllStr,String fileCounter) {
         
         DocElement doc = null;
         
         DataCartSolrHandler handler = new DataCartSolrHandler();
         handler.preassembleQueryString();
 
         handler.addLimit(initialQuery,fileCounter);
         handler.addSearchConstraints(fq, showAllStr);
 
         
         //REPLACE ME!
         //should be handler.addShard(peer+":8983/solr");
         //handler.addShard("localhost"+":8983/solr");
         handler.addShard(peer+":8983/solr");
 
         doc = handler.getDocElement(id);
         
         
         return doc;
     }
     
     
     
     @SuppressWarnings("unchecked")
     public static void queryStringInfo(HttpServletRequest request) {
         System.out.println("--------Utils Query String Info--------");
         Enumeration<String> paramEnum = request.getParameterNames();
         
         while(paramEnum.hasMoreElements()) { 
             String postContent = (String) paramEnum.nextElement();
             System.out.println(postContent+"-->"); 
             System.out.println(request.getParameter(postContent));
         }
         System.out.println("--------End Utils Query String Info--------");
     }
     
     
     
     
     
     
 
     /**
      * 
      * @param request
      */
     public void printParameters(HttpServletRequest request) {
         
         String peerStr = request.getParameter("peerStr");
         if(peerStr != null) {
             String [] peers = peerStr.split(";");
             //System.out.println("PEERS");
             for(int i=0;i<peers.length;i++) {
                 System.out.println("\tPeer: " + peers[i]);
             }
         } else {
             System.out.println("\tPEERS IS NULL");
         }
         
         
 
         System.out.println("IDS");
         String idStr = request.getParameter("idStr");
         if(idStr != null) {
             String [] id = idStr.split(";");
             for(int i=0;i<id.length;i++) {
                 System.out.println("\tID: " + id[i]);
             }
         } else {
             System.out.println("\tID IS NULL");
         }
         
 
         System.out.println("Search Constraints");
         String fqStr = request.getParameter("fqStr");
         if(fqStr != null) {
             String [] fq = fqStr.split(";");
             for(int i=0;i<fq.length;i++) {
                 System.out.println("\tFQ: " + fq[i]);
             }
         } else {
             System.out.println("\tFQ IS NULL");
         }
         
 
         System.out.println("Technotes");
         String technotes = request.getParameter("technotesStr");
         if(technotes != null) {
             System.out.println("\tTechnotes: " + technotes);
         } else {
             System.out.println("\tTechnotes is null");
         }
 
         System.out.println("ShowAll");
         String showAll = request.getParameter("showAllStr");
         if(showAll != null) {
             System.out.println("\tShowAll: " + showAll);
         } else {
             System.out.println("\tShowAll is null");
         }
         
         System.out.println("Initial Query");
         String initialQuery = request.getParameter("initialQuery");
         if(initialQuery != null) {
             System.out.println("\tinitialQuery: " + initialQuery);
         } else {
             System.out.println("\tinitialQuery is null");
         }
         
         System.out.println("File Counter");
         String fileCounter = request.getParameter("fileCounter");
         if(fileCounter != null) {
             System.out.println("\tfileCounter: " + fileCounter);
         } else {
             System.out.println("\tfileCounter is null");
         }
         
     }
 }
 
 
