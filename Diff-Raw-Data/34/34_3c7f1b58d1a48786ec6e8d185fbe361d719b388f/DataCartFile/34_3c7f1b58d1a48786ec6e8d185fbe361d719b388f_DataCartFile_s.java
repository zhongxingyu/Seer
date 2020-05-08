 package org.esgf.solr.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.esgf.datacart.XmlFormatter;
 import org.esgf.srm.SRMEntryList;
 import org.esgf.srm.SRMUtils;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 public class DataCartFile {
 
     private String fileId;
     private String title;
     private String size;
     private String tracking_id;
     private String checksum;
     private String checksum_type;
     
     private String technoteStr;
     
     private List<String> services;
     private List<String> urls;
     private List<String> mimes;
     
     private String isCached;
 
     private List<String> technotes;
     
     
     public DataCartFile() {
         this.setFileId(null);
         this.setTitle(null);
         this.setSize(null);
     }
     
     public DataCartFile(SolrRecord solrRecord) {
         
         if(Utils.debugMode)
            System.out.println("\n\n\nIn datacart File\n");
         
         String fileId = null;
         if(solrRecord.getStrField("id") == null) {
             fileId = "N/A";
         } else {
             fileId = solrRecord.getStrField("id");
         }
         String title = null;
         if(solrRecord.getStrField("title") == null) {
             title = "N/A";
         } else {
             title = solrRecord.getStrField("title");
         }
         String size = null;
         if(solrRecord.getStrField("size") == null) {
             size = "N/A";
         } else {
             size = solrRecord.getStrField("size");
         }
         String tracking_id = null;
         if(solrRecord.getStrField("tracking_id") == null) {
             tracking_id = "N/A";
         } else {
             tracking_id = solrRecord.getStrField("tracking_id");
         }
         String checksum = null;
         if(solrRecord.getStrField("checksum") == null) {
             checksum = "N/A";
         } else {
             checksum = solrRecord.getStrField("checksum");
         }
         String checksum_type = null;
         if(solrRecord.getStrField("checksum_type") == null) {
             checksum_type = "N/A";
         } else {
             checksum_type = solrRecord.getStrField("checksum_type");
         }
         
         this.fileId = fileId;
         this.title = title;
         this.size = size;
         
         this.tracking_id = tracking_id;
         this.checksum = checksum;
         this.checksum_type = checksum_type;
         
         
         this.getCacheInfo(solrRecord);
         
         this.parseUrl(solrRecord);
         
         this.getTechnotesFromRecord(solrRecord);
     }
     
     public void getTechnotesFromRecord(SolrRecord solrRecord) {
         
        
         List<String> technotes = solrRecord.getArrField("xlink");
         List<String> technoteStrs = new ArrayList<String>();
         
         
         if(technotes != null) {
            System.out.println(technotes.size());
             
             for(int i=0;i<technotes.size();i++) {
                 String technoteStrElement = technotes.get(i);
                 String [] technoteStr = technoteStrElement.split("\\|");
                 technoteStrs.add(technoteStr[0]);
             }
             
             this.technotes = technoteStrs;
         }
         
         
     }
     
     public String toXML() {
         String xml = "";
         
         Element servicesEl = this.toElement();
 
         XMLOutputter outputter = new XMLOutputter();
         xml = outputter.outputString(servicesEl);
         
         return xml;
     }
     
     public Element toElement() {
          
         Element fileEl = new Element("file");
         
         if(this.fileId != null) {
             Element fileIdEl = new Element("fileId");
             fileIdEl.addContent(this.fileId);
             fileEl.addContent(fileIdEl);
         } else {
             Element fileIdEl = new Element("fileId");
             fileIdEl.addContent("N/A");
             fileEl.addContent(fileIdEl);
         }
         
         if(this.size != null) {
             Element sizeEl = new Element("size");
             sizeEl.addContent(this.size);
             fileEl.addContent(sizeEl);
         } else {
             Element sizeEl = new Element("size");
             sizeEl.addContent("N/A");
             fileEl.addContent(sizeEl);
         }
         
 
         if(this.title != null) {
             Element titleEl = new Element("title");
             titleEl.addContent(this.title);
             fileEl.addContent(titleEl);
         } else {
             Element titleEl = new Element("title");
             titleEl.addContent("N/A");
             fileEl.addContent(titleEl);
         }
         
         
         if(this.tracking_id != null) {
             Element tracking_idEl = new Element("tracking_id");
             tracking_idEl.addContent(this.tracking_id);
             fileEl.addContent(tracking_idEl);
         } else {
             Element tracking_idEl = new Element("tracking_id");
             tracking_idEl.addContent("N/A");
             fileEl.addContent(tracking_idEl);
         }
 
         if(this.checksum != null) {
             Element checksumEl = new Element("checksum");
             checksumEl.addContent(this.checksum);
             fileEl.addContent(checksumEl);
         } else {
             Element checksumEl = new Element("checksum");
             checksumEl.addContent("N/A");
             fileEl.addContent(checksumEl);
         }
 
         if(this.checksum_type != null) {
             Element checksum_typeEl = new Element("checksum_type");
             checksum_typeEl.addContent(this.checksum_type);
             fileEl.addContent(checksum_typeEl);
         } else {
             Element checksum_typeEl = new Element("checksum_type");
             checksum_typeEl.addContent("N/A");
             fileEl.addContent(checksum_typeEl);
         }
 
         if(this.isCached != null) {
             Element isCachedEl = new Element("isCached");
             isCachedEl.addContent(this.isCached);
             fileEl.addContent(isCachedEl);
         } else {
             Element isCachedEl = new Element("isCached");
             isCachedEl.addContent("true");
             fileEl.addContent(isCachedEl);
         }
         
         if(this.services != null) {
             Element servicesEl = new Element("services");
             
             if(this.services != null) {
                 for(int i=0;i<services.size();i++) {
                     Element serviceEl = new Element("service");
                     serviceEl.addContent(services.get(i));
                     servicesEl.addContent(serviceEl);
                 }
             }
             fileEl.addContent(servicesEl);
         } else {
             Element servicesEl = new Element("services");
 
             fileEl.addContent(servicesEl);
         }
         
         if(this.urls != null) {
             Element urlsEl = new Element("urls");
             
             if(this.urls != null) {
                 for(int i=0;i<urls.size();i++) {
                     Element urlEl = new Element("url");
                     urlEl.addContent(urls.get(i));
                     urlsEl.addContent(urlEl);
                 }
             }
             fileEl.addContent(urlsEl);
         } else {
             Element urlsEl = new Element("urls");
 
             fileEl.addContent(urlsEl);
         }
         
         if(this.technotes != null) {
             Element technotesEl = new Element("technotes");
             
             if(this.technotes != null) {
                 for(int i=0;i<technotes.size();i++) {
                     Element technoteEl = new Element("technote");
                     technoteEl.addContent(technotes.get(i));
                     technotesEl.addContent(technoteEl);
                 }
             }
             fileEl.addContent(technotesEl);
         } else {
             Element technotesEl = new Element("technotes");
 
             fileEl.addContent(technotesEl);
         }
         
         return fileEl;
     }
     
     public static void main(String [] args) {
 
         Solr solr = new Solr();
         
         solr.addConstraint("query", "*");
         solr.addConstraint("distrib", "false");
         solr.addConstraint("limit", "8");
         solr.addConstraint("type", "File");
         solr.addConstraint("dataset_id","ornl.ultrahighres.CESM1.t341f02.FAMIPr.v1|esg2-sdnl1.ccs.ornl.gov");
         //solr.addConstraint("project", "CMIP5");
         
         solr.executeQuery();
         
         
         SolrResponse solrResponse = solr.getSolrResponse();
         
         List<SolrRecord> solrRecords = solrResponse.getSolrRecords();
         
         System.out.println("Count: " + solrResponse.getCount() + " " + solrRecords.size());
         
         
         DataCartFile datacartFile = new DataCartFile(solrRecords.get(0));
         
         System.out.println( new XmlFormatter().format(datacartFile.toXML()));
         
         
         
     }
     
     
     
    
     
     public void getCacheInfo(SolrRecord solrRecord) {
         SRMEntryList srm_entry_list = new SRMEntryList();
         
         srm_entry_list.fromFile(SRMUtils.SRM_CACHE_FILE_LIST_FILE_LOC);
         
         this.isCached = srm_entry_list.isCached(solrRecord.getStrField("id"));
         
         
     }
     
     public void parseUrl(SolrRecord solrRecord) {
         
         List<String> values = solrRecord.getArrField("url");
         this.services = new ArrayList<String>();
         this.urls = new ArrayList<String>();
         this.mimes = new ArrayList<String>();
         
         
         if(this.isCached.equals("true")) {
             
             for(int i=0;i<values.size();i++) {
                 String value = values.get(i);
                 String [] components = value.split("\\|");
                 
                 String srm_url = components[0];
                 
                 //add http and gridftp urls if cached
                System.out.println(srm_url);
                 String gsiftp = SRMUtils.stripSRMServer(srm_url);
                 String http = SRMUtils.gridftp2http(gsiftp);
 
                 this.urls.add(http);
                 this.services.add("HTTPServer");
                 
                 this.urls.add(gsiftp);
                 this.services.add("GridFTP");
                 
                 
             }
             
         } else {
             
             for(int i=0;i<values.size();i++) {
                 String value = values.get(i);
                 String [] components = value.split("\\|");
                 
                 String srm_url = components[0];
                 this.urls.add(srm_url);
                 
                 String service = components[components.length-1];
                 this.services.add(service);
             }
             
         }
         
         
         
     }
     
     
 
     /**
      * @return the fileId
      */
     public String getFileId() {
         return fileId;
     }
 
     /**
      * @param fileId the fileId to set
      */
     public void setFileId(String fileId) {
         this.fileId = fileId;
     }
 
     /**
      * @return the title
      */
     public String getTitle() {
         return title;
     }
 
     /**
      * @param title the title to set
      */
     public void setTitle(String title) {
         this.title = title;
     }
 
     /**
      * @return the size
      */
     public String getSize() {
         return size;
     }
 
     /**
      * @param size the size to set
      */
     public void setSize(String size) {
         this.size = size;
     }
 
     /**
      * @return the tracking_id
      */
     public String getTracking_id() {
         return tracking_id;
     }
 
     /**
      * @param tracking_id the tracking_id to set
      */
     public void setTracking_id(String tracking_id) {
         this.tracking_id = tracking_id;
     }
 
     /**
      * @return the checksum
      */
     public String getChecksum() {
         return checksum;
     }
 
     /**
      * @param checksum the checksum to set
      */
     public void setChecksum(String checksum) {
         this.checksum = checksum;
     }
 
     /**
      * @return the checksum_type
      */
     public String getChecksum_type() {
         return checksum_type;
     }
 
     /**
      * @param checksum_type the checksum_type to set
      */
     public void setChecksum_type(String checksum_type) {
         this.checksum_type = checksum_type;
     }
 
     /**
      * @return the technoteStr
      */
     public String getTechnoteStr() {
         return technoteStr;
     }
 
     /**
      * @param technoteStr the technoteStr to set
      */
     public void setTechnoteStr(String technoteStr) {
         this.technoteStr = technoteStr;
     }
 
     /**
      * @return the services
      */
     public List<String> getServices() {
         return services;
     }
 
     /**
      * @param services the services to set
      */
     public void setServices(List<String> services) {
         this.services = services;
     }
 
     /**
      * @return the urls
      */
     public List<String> getUrls() {
         return urls;
     }
 
     /**
      * @param urls the urls to set
      */
     public void setUrls(List<String> urls) {
         this.urls = urls;
     }
 
     /**
      * @return the mimes
      */
     public List<String> getMimes() {
         return mimes;
     }
 
     /**
      * @param mimes the mimes to set
      */
     public void setMimes(List<String> mimes) {
         this.mimes = mimes;
     }
 
     /**
      * @return the isCached
      */
     public String getIsCached() {
         return isCached;
     }
 
     /**
      * @param isCached the isCached to set
      */
     public void setIsCached(String isCached) {
         this.isCached = isCached;
     }
 
     /**
      * @return the technotes
      */
     public List<String> getTechnotes() {
         return technotes;
     }
 
     /**
      * @param technotes the technotes to set
      */
     public void setTechnotes(List<String> technotes) {
         this.technotes = technotes;
     }
     
     
     
 }
