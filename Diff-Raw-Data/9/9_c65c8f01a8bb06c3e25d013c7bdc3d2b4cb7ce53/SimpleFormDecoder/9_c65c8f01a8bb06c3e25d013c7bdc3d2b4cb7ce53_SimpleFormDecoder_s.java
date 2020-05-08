 /*
  * Created on Jun 15, 2005
  */
 package uk.org.ponder.servletutil;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 
 import uk.org.ponder.errorutil.TargettedMessage;
import uk.org.ponder.saxalizer.SAXLeafParser;
 import uk.org.ponder.util.Logger;
 
 /** An obsolete class used to deserialise an HttpServletRequest onto a 
  * flat Object, predating the DataAlterationRequest architecture.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *  
  */
 public class SimpleFormDecoder {
   public static TargettedMessage[] decodeForm(Object target,
       HttpServletRequest hsr) {
     Class targclass = target.getClass();
     Field[] fields = targclass.getFields();
    SAXLeafParser parser = SAXLeafParser.instance();
     ArrayList messages = new ArrayList();
 //    for (Iterator pit = hsr.getParameterMap().keySet().iterator(); pit.hasNext();) {
 //      String param = (String) pit.next();
 //      String value = hsr.getParameter(param);
 //      Logger.log.info("Param name " + param + " value " + value);
 //    }
     for (int i = 0; i < fields.length; ++i) {
       Class fieldtype = fields[i].getType();
       String fieldname = fields[i].getName();
       String param = hsr.getParameter(fieldname);
      Class parsedtype = SAXLeafParser.wrapClass(fieldtype);
 //      Logger.log.info("Field name " + fieldname + " param " + param + " type " + parsedtype);
       if (param != null) {
         try {
           Object parsed = parser.parse(parsedtype, param);
           fields[i].set(target, parsed);
         }
         catch (Exception e) {
           messages.add(new TargettedMessage(e.getMessage(), e.getClass(),
               fieldname));
           Logger.log.warn("Error parsing field " + fieldname + " for "
               + fieldtype, e);
         }
       }
     }
     return (TargettedMessage[]) messages.toArray(new TargettedMessage[messages
         .size()]);
   }
 }
