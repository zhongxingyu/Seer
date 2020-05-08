 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.webdetails.cdb.query;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.pentaho.platform.api.engine.IParameterProvider;
 import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
 import pt.webdetails.cdb.connector.ConnectorEngine;
 import pt.webdetails.cpf.persistence.PersistenceEngine;
 
 /**
  *
  * @author pdpi
  */
 public class QueryEngine {
 
   private static final Log logger = LogFactory.getLog(ConnectorEngine.class);
   private static QueryEngine _instance;
 
   public static QueryEngine getInstance() {
     if (_instance == null) {
       _instance = new QueryEngine();
     }
     return _instance;
   }
 
   public JSONObject listGroups() {
     JSONObject response;
     PersistenceEngine pe = PersistenceEngine.getInstance();
     try {
      response = pe.query("select distinct(group) as name from Query where userid = '" +  PentahoSessionHolder.getSession().getName()  + "'");
     } catch (JSONException e) {
       return null;
     }
     return response;
   }
 
   public void process(IParameterProvider requestParams, IParameterProvider pathParams, OutputStream out) {
 
     String method = requestParams.getStringParameter("method", "");
     if ("listGroups".equals(method)) {
       try {
         out.write(listGroups().toString(2).getBytes("utf-8"));
       } catch (Exception e) {
         logger.error("Error listing queries: " + e);
       } 
     } else {
       logger.error("Unsupported method");
     }
   }
 
 }
