 package com.sic.controller.util;
 
 import com.sic.entity.Event;
 import com.sic.entity.Log;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
 import java.util.List;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.model.SelectItem;
 import javax.servlet.ServletContext;
 import javax.xml.bind.Binder;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.SAXException;
 
 public class JsfUtil {
 
     private static final String XML_LOG = "log-sic.xml";
 
     public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
         int size = selectOne ? entities.size() + 1 : entities.size();
         SelectItem[] items = new SelectItem[size];
         int i = 0;
         if (selectOne) {
             items[0] = new SelectItem("", "---");
             i++;
         }
         for (Object x : entities) {
             items[i++] = new SelectItem(x, x.toString());
         }
         return items;
     }
 
     public static SelectItem[] getSelectItems(List<?> entities) {
         int size = entities.size();
         SelectItem[] items;
         int i = 0;
         if (size == 0) {
             items = new SelectItem[size + 1];
             items[0] = new SelectItem("", "---");
         } else {
             items = new SelectItem[size];
         }
         for (Object x : entities) {
             items[i++] = new SelectItem(x, x.toString());
         }
         return items;
     }
 
     public static void addErrorMessage(Exception ex, String defaultMsg) {
         String msg = ex.getLocalizedMessage();
         if (msg != null && msg.length() > 0) {
             addErrorMessage(msg);
         } else {
             addErrorMessage(defaultMsg);
         }
     }
 
     public static void addErrorMessages(List<String> messages) {
         for (String message : messages) {
             addErrorMessage(message);
         }
     }
 
     public static void addErrorMessage(String msg) {
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg);
         FacesContext.getCurrentInstance().addMessage("ERROR", facesMsg);
     }
 
     public static void addSuccessMessage(String msg) {
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, msg);
         FacesContext.getCurrentInstance().addMessage("Información", facesMsg);
     }
 
     public static void addWarningMessage(String msg) {
         FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
         FacesContext.getCurrentInstance().addMessage("Advertencia", facesMsg);
     }
 
     public static String getRequestParameter(String key) {
         return FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get(key);
     }
 
     public static Object getObjectFromRequestParameter(String requestParameterName, Converter converter, UIComponent component) {
         String theId = JsfUtil.getRequestParameter(requestParameterName);
         return converter.getAsObject(FacesContext.getCurrentInstance(), component, theId);
     }
 
     public static void redirect(String url) throws IOException {
         FacesContext context = FacesContext.getCurrentInstance();
         context.getExternalContext().redirect(url);
     }
 
     public static void writeLog(Event event) throws JAXBException, FileNotFoundException, SAXException, ParserConfigurationException, IOException, TransformerConfigurationException, TransformerException {
         ServletContext servletContext = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
         String path = servletContext.getRealPath("/log/").concat("\\");
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db = dbf.newDocumentBuilder();
         File xml = new File(path + XML_LOG);
         Document document = db.parse(xml);
         JAXBContext jc = JAXBContext.newInstance(Log.class);
         Binder<Node> binder = jc.createBinder();
         Log log = (Log) binder.unmarshal(document);
        if(log.getEvents() == null){
            log.setEvents(new ArrayList<Event>());
        }
         log.getEvents().add(event);
         binder.updateXML(log);
         TransformerFactory tf = TransformerFactory.newInstance();
         Transformer t = tf.newTransformer();
         t.transform(new DOMSource(document), new StreamResult(xml));
     }
 
     public static void putSession(Object obj, String name) {
         FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(name, obj);
     }
 
     public static Object getSessionAtribute(String name) {
         return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(name);
     }
 }
