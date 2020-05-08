 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pt.webdetails.cdb.exporters;
 
 import java.io.IOException;
 import java.io.OutputStream;
import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.Node;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.pentaho.platform.api.engine.IParameterProvider;
 import org.pentaho.platform.api.repository.ISolutionRepository;
 import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
 import org.pentaho.platform.engine.core.system.PentahoSystem;
 import pt.webdetails.cdb.ExporterNotFoundException;
 import pt.webdetails.cpf.InterPluginCall;
 
 /**
  *
  * @author pdpi
  */
 public class ExporterEngine {
 
   protected Log logger = LogFactory.getLog(ExporterEngine.class);
   private static ExporterEngine _instance;
 
   private ExporterEngine() {
   }
 
   public static ExporterEngine getInstance() {
     if (_instance == null) {
       _instance = new ExporterEngine();
     }
     return _instance;
   }
 
   public void process(IParameterProvider requestParams, IParameterProvider pathParams, OutputStream out) {
     try {
       String method = requestParams.getStringParameter("method", "");
 
       if ("listExporters".equals(method)) {
         String exporters = listExporters();
         out.write(exporters.getBytes("utf-8"));
       } else if ("export".equals(method)) {
         ServletRequestWrapper wrapper = (ServletRequestWrapper) pathParams.getParameter("httprequest");
         String exporterName = requestParams.getStringParameter("exporter", ""),
                 group = requestParams.getStringParameter("group", ""),
                 id = requestParams.getStringParameter("id", ""),
                 filename = requestParams.getStringParameter("filename", "default"),
                 url = wrapper.getScheme() + "://" + wrapper.getServerName() + ":" + wrapper.getServerPort();
 
         boolean toFile = requestParams.getStringParameter("toFile", "false").equals("true");
         Exporter exporter = getExporter(exporterName);
         if (toFile) {
           HttpServletResponse response = (HttpServletResponse) pathParams.getParameter("httpresponse");
          response.setHeader("content-disposition", "attachment; filename=" + URLEncoder.encode(exporter.getFilename(group, id, url), "utf-8"));
           exporter.binaryExport(group, id, url, out);
         } else {
           out.write(exporter.export(group, id, url).getBytes("utf-8"));
         }
         /*} catch (ExporterRuntimeException e) {
         logger.error(e);
         } catch (ExporterNotFoundException e) {
         logger.error(e);*/      }
     } catch (Exception e) {
       logger.error(e);
     }
   }
 
   public void export(String exporterName, String group, String id, String url, OutputStream out) throws ExporterRuntimeException, ExporterNotFoundException {
     Exporter exporter = getExporter(exporterName);
     try {
       out.write(exporter.export(group, id, url).getBytes("utf-8"));
     } catch (Exception e) {
       logger.error(e);
     }
   }
 
   public void exportToFile(String exporterName, String group, String id, String url, OutputStream out) throws ExporterRuntimeException, ExporterNotFoundException {
     Exporter exporter = getExporter(exporterName);
     try {
       exporter.binaryExport(group, id, url, out);
     } catch (Exception e) {
       logger.error(e);
     }
   }
 
   protected Exporter getExporter(String exporterName) throws ExporterRuntimeException, ExporterNotFoundException {
     try {
       Document doc = getConfigFile();
       String exporterClassName = doc.selectSingleNode("//exporter[@id='"+exporterName+"']/@class").getText();
       Class exporterClass = Class.forName(exporterClassName);
       return (Exporter) exporterClass.getConstructor().newInstance();
     } catch (ClassNotFoundException e) {
       throw new ExporterNotFoundException(e);
     } catch (Exception e) {
       throw new ExporterRuntimeException(e);
     }
   }
 
   public String listExporters() {
     JSONArray arr = new JSONArray();
 
     Document doc = getConfigFile();
     List<Node> exporters = doc.selectNodes("//exporter");
     for (Node exporter : exporters) {
       String id = exporter.selectSingleNode("@id").getText();
       try {
         JSONObject jsonExporter = new JSONObject();
         JSONObject jsonModes = new JSONObject();
         jsonExporter.put("id", id);
         jsonExporter.put("label", exporter.selectSingleNode("@label").getText());
         List<Node> modes = exporter.selectNodes(".//mode");
         for (Node mode : modes) {
           jsonModes.put(mode.selectSingleNode("@type").getText(), true);
         }
         jsonExporter.put("modes", jsonModes);
         arr.put(jsonExporter);
       } catch (JSONException e) {
         logger.error("Failed to list exporter " + id + ". Reason: " + e);
       }
     }
     try {
       return arr.toString(2);
     } catch (JSONException e) {
       return null;
     }
 
   }
 
   public static String exportCda(String group, String id, String outputType) {
     Map<String, Object> params = new HashMap<String, Object>();
     params.put("path", "cdb/queries/" + group + ".cda");
     params.put("dataAccessId", id);
     params.put("outputType", outputType);
 
     InterPluginCall pluginCall = new InterPluginCall(InterPluginCall.CDA, "doQuery", params);
     return pluginCall.call();           
   }
 
   private Document getConfigFile() {
     ISolutionRepository solutionRepository = PentahoSystem.get(ISolutionRepository.class, PentahoSessionHolder.getSession());
     Document doc;
 
     try {
       doc = solutionRepository.getResourceAsDocument("/system/cdb/exporters.xml", 0);
     } catch (IOException e) {
       doc = null;
     }
     return doc;
   }
 
 }
