 package com.asascience.edc.erddap;
 
 import com.asascience.edc.sos.SensorContainer;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.json.JSONConfiguration;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.log4j.Logger;
 import org.codehaus.jettison.json.JSONArray;
 import org.codehaus.jettison.json.JSONObject;
 
 /**
  * ErddapDataset.java
  * 
  * @author Kyle Wilcox <kwilcox@asascience.com>
  */
 public class ErddapDataset {
 
   private ErddapServer erddapServer;
   private final String id;
   private String title;
   private String summary;
   private String background_info;
   private String institution;
   private String cdm_data_type;
   private boolean griddap;
   private boolean tabledap;
   private boolean subset;
   private boolean wms;
   private ErddapVariable timeVariable;
   private ErddapVariable yVariable;
   private ErddapVariable xVariable;
   private ErddapVariable zVariable;
   private ErddapVariable cdmVariable;
   private ArrayList<ErddapVariable> variables;
   private ArrayList<SensorContainer> locations = null;
   private static Logger guiLogger = Logger.getLogger("com.asascience.log." + ErddapDataset.class.getName());
 
   public ErddapDataset(String id) {
     this.id = id;
   }
 
   public ErddapDataset(String id, String title, String summary, String background_info) {
     this.id = id;
     this.title = title;
     this.summary = summary;
     this.background_info = background_info;
   }
 
   public void setTitle(String title) {
     this.title = title;
   }
 
   public void setBackgroundInfo(String background_info) {
     this.background_info = background_info;
   }
 
   public void setSummary(String summary) {
     this.summary = summary;
   }
 
   public void setInstitution(String institution) {
     this.institution = institution;
   }
 
   public void setErddapServer(ErddapServer erddapServer) {
     this.erddapServer = erddapServer;
   }
 
   public void setGriddap(boolean griddap) {
     this.griddap = griddap;
   }
 
   public void setSubset(boolean subset) {
     this.subset = subset;
   }
 
   public void setTabledap(boolean tabledap) {
     this.tabledap = tabledap;
   }
 
   public void setWms(boolean wms) {
     this.wms = wms;
   }
 
   public boolean isGriddap() {
     return griddap;
   }
 
   public boolean isSubset() {
     return subset;
   }
 
   public boolean isTabledap() {
     return tabledap;
   }
 
   public boolean isWms() {
     return wms;
   }
   
   public boolean isGraph() {
     return (isTabledap() || isGriddap());
   }
 
   public String getId() {
     return id;
   }
 
   public String getInstitution() {
     return institution;
   }
 
   public String getTitle() {
     return title;
   }
 
   public ErddapServer getErddapServer() {
     return erddapServer;
   }
 
   public String getBackgroundInfo() {
     return background_info;
   }
 
   public String getSummary() {
     return summary;
   }
 
   public String getInfo() {
     return erddapServer.getURL() + "info/" + this.id + "/index.json";
   }
 
   public String getRSS() {
     return erddapServer.getURL() + "rss/" + this.id + ".rss";
   }
 
   public String getEmail() {
     return erddapServer.getURL() + "subscriptions/add.html?datasetID=" + this.id + "&showErrors=false&email=";
   }
 
   public String getGriddap() {
     if (isGriddap()) {
       return erddapServer.getURL() + "griddap/" + this.id;
     }
     return null;
   }
 
   public boolean hasX() {
     return getX() != null;
   }
   
   public ErddapVariable getX() {
     return xVariable;
   }
   
   public boolean hasY() {
     return getY() != null;
   }
   
   public ErddapVariable getY() {
     return yVariable;
   }
   
   public boolean hasZ() {
     return getZ() != null;
   }
   
   public ErddapVariable getZ() {
     return zVariable;
   }
   
   public boolean hasCdmAxis() {
     return getCdmAxis() != null;
   }
   
   public ErddapVariable getCdmAxis() {
     return cdmVariable;
   }
   
   public boolean hasLocations() {
     return getLocations() != null;
   }
   
   public List<SensorContainer> getLocations() {
     return locations;
   }
       
   public boolean hasTime() {
     return getTime() != null;
   }
   
   public ErddapVariable getTime() {
     return timeVariable;
   }
   
   public boolean hasCdmDataType() {
     return getCdmDataType() != null;
   }
 
   public String getCdmDataType() {
     return cdm_data_type;
   }
 
   public String getSubset() {
     if (isSubset()) {
       return getTabledap() + ".subset";
     }
     return null;
   }
 
   public String getTabledap() {
     if (isTabledap()) {
       return erddapServer.getURL() + "tabledap/" + this.id;
     }
     return null;
   }
   
   public String getTabledapHtml() {
     if (isTabledap()) {
       return getTabledap() + ".html";
     }
     return null;
   }
 
   public String getWms() {
     if (isWms()) {
       return erddapServer.getURL() + "wms/" + this.id + "/request";
     }
     return null;
   }
 
   public String getGraph() {
     if (isTabledap()) {
       return getTabledap() + ".graph";
     } else {
       if (isGriddap()) {
         return getGriddap() + ".graph";
       }
     }
     return null;
   }
   
   public List<ErddapVariable> getVariables() {
     return variables;
   }
   
   public void buildVariables() {
     guiLogger.info("Building Variables from ERDDAP Dataset: " + this.getTitle());
     try {
       ClientConfig clientConfig = new DefaultClientConfig();
       clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
       Client c = Client.create(clientConfig);
       guiLogger.info("Request: " + this.getInfo());
       WebResource wr = c.resource(this.getInfo());
       JSONObject result = wr.get(JSONObject.class);
       JSONArray ar = result.getJSONObject("table").getJSONArray("rows");
       variables = new ArrayList<ErddapVariable>();
       ErddapVariable edv = null;
       String[] subsetVars = null;
       Double frst,scnd;
       for (int i = 0 ; i < ar.length() ; i++) {
         if (ar.getJSONArray(i).getString(2).equals("subsetVariables")) {
           subsetVars = ar.getJSONArray(i).getString(4).split(",");
           for (int j = 0 ; j < subsetVars.length ; j++) {
             subsetVars[j] = subsetVars[j].trim();
           }
           continue;
         }
         
         if (ar.getJSONArray(i).getString(2).equals("cdm_data_type")) {
           cdm_data_type = ar.getJSONArray(i).getString(4);
           continue;
         }
         
         if (ar.getJSONArray(i).getString(0).equals("variable")) {
           if (edv != null) {
             setAxis(edv);
             variables.add(edv);
           }
           edv = new ErddapVariable(this,ar.getJSONArray(i).getString(1),ar.getJSONArray(i).getString(3),Arrays.asList(subsetVars).contains(ar.getJSONArray(i).getString(1).trim()));
           continue;
         }
           
         if (ar.getJSONArray(i).getString(0).equals("attribute")) {
           if (ar.getJSONArray(i).getString(2).equals("actual_range")) {
            // The range is not always going to be in the correct order
            frst = Double.parseDouble(ar.getJSONArray(i).getString(4).split(",")[0].trim());
            scnd = Double.parseDouble(ar.getJSONArray(i).getString(4).split(",")[1].trim());
            edv.setMin(String.valueOf(Math.min(frst,scnd)));
            edv.setMax(String.valueOf(Math.max(frst,scnd)));
           } else if (ar.getJSONArray(i).getString(2).equals("long_name")) {
             edv.setLongname(ar.getJSONArray(i).getString(4).trim());
           } else if (ar.getJSONArray(i).getString(2).equals("units")) {
             edv.setUnits(ar.getJSONArray(i).getString(4).trim());
           } else if (ar.getJSONArray(i).getString(2).equals("cf_role")) {
             edv.setAxis(ar.getJSONArray(i).getString(4).trim());
           } else if (ar.getJSONArray(i).getString(2).equalsIgnoreCase("description")) {
             edv.setDescription(ar.getJSONArray(i).getString(4).trim());
           }
         }
       }
       // Get the last iteration's edv variable
       if (edv != null) {
         variables.add(edv);
       }
       
       // Does this dataset have points we can strip out?
       // latitude and longitude also need to be subsetVariables or
       // this might be a long and hard query (from Bob Simmons).
       if (hasX() && hasY() && !getX().getValues().isEmpty() && !getX().getValues().isEmpty()) {
         buildLocations();
       }
       
     } catch (Exception e) {
       guiLogger.error("Exception", e);
     }
   }
   
   private void buildLocations() {
     guiLogger.info("Building Locations from ERDDAP Dataset: " + this.getTitle());
     try {
       ClientConfig clientConfig = new DefaultClientConfig();
       clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
       Client c = Client.create(clientConfig);
       guiLogger.info("Request: " + this.getTabledap() + ".json?" + this.getX().getName() + "," + this.getY().getName() + "&distinct()");
       WebResource wr = c.resource(this.getTabledap() + ".json?" + this.getX().getName() + "," + this.getY().getName() + "&distinct()");
       JSONObject result = wr.get(JSONObject.class);
       JSONArray ar = result.getJSONObject("table").getJSONArray("rows");
       locations = new ArrayList<SensorContainer>(ar.length());
       SensorContainer senc;
       double[] loc = new double[4];
       for (int i = 0 ; i < ar.length() ; i++) {
         senc = new SensorContainer();
         loc[0] = ar.getJSONArray(i).getDouble(1);
         loc[1] = ar.getJSONArray(i).getDouble(0);
         loc[2] = ar.getJSONArray(i).getDouble(1);
         loc[3] = ar.getJSONArray(i).getDouble(0);
         senc.setNESW(loc.clone());
         senc.setName("");
         locations.add(senc);
       }
     } catch (Exception e) {
       guiLogger.error("Exception", e);
     }
   }
   
   private void setAxis(ErddapVariable erv) {
     if (erv.isTime()) {
       timeVariable = erv;
     } else if (erv.isX()) {
       xVariable = erv;
     } else if (erv.isY()) {
       yVariable = erv;
     } else if (erv.isZ()) {
       zVariable = erv;
     } else if (erv.isCdm()) {
       cdmVariable = erv;
     }
   }
   
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
     sb.append("Title: ").append(getTitle()).append("\n\n");
     sb.append("Summary: ").append(getSummary()).append("\n\n");
     return sb.toString();
   }
 
   private String linkify(String label, String link) {
     return "<a href='" + link +"'>" + label + "</a>";
   }
   
   private String buildWmsRequest(String baseURL) {
     StringBuilder sb = new StringBuilder(baseURL);
     sb.append("?SERVICE=WMS");
     sb.append("&VERSION=1.3.0");
     sb.append("&REQUEST=GetCapabilities");
     return sb.toString();
   }
 
   public String toHTMLString() {
     StringBuilder sb = new StringBuilder();
     sb.append("<b>Title:</b> ").append(getTitle()).append("<br /><br />");
     sb.append("<b>Dataset ID:</b> ").append(getId()).append("<br /><br />");
     sb.append("<b>Institution:</b> ").append(getInstitution()).append("<br /><br />");
     sb.append("<b>Background Info:</b> ").append(linkify(getBackgroundInfo(),getBackgroundInfo())).append("<br /><br />");
     sb.append("<b>Summary:</b> ").append(getSummary()).append("<br /><br />");
     
     sb.append("<b>ERDDAP Provided Web Services:</b><br />");
     if (isWms()) {
       sb.append("<i>WMS GetCapabilities:</i> ").append(linkify(getWms(),buildWmsRequest(getWms()))).append("<br />");
     }
     if (isTabledap()) {
       sb.append("<i>DataAccess Web Form:</i> ").append(linkify(getTabledap() + ".html",getTabledap() + ".html")).append("<br />");
     }
     if (isGriddap()) {
       sb.append("<i>DataAccess Web Form:</i> ").append(linkify(getGriddap() + ".html",getGriddap() + ".html")).append("<br />");
     }
     if (isSubset()) {
       sb.append("<i>Subset Web Form:</i> ").append(linkify(getSubset(),getSubset())).append("<br />");
     }
     if (isGraph()) {
       sb.append("<i>Graphing Web Form:</i> ").append(linkify(getGraph(),getGraph())).append("<br />");
     }
     
     return sb.toString();
   }
   
 }
