 package no.uis.service.fsimport;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import no.uis.service.fsimport.StudInfoImport.StudinfoType;
 import no.uis.service.fsimport.util.ContextPath;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class ValidationErrorHandler extends DefaultHandler implements ErrorHandler {
   
   private List<String> messages = new LinkedList<String>();
   
   private ContextPath contextPath = new ContextPath();
 
   private String currentContent;
   
   private final int year;
 
   private final String semester;
 
   private final String language;
 
   private final StudinfoType infoType;
 
   private String contextId;
   
   public ValidationErrorHandler(StudinfoType infoType, int year, String semester, String language) {
     this.infoType = infoType;
     this.year = year;
     this.semester = semester;
     this.language = language;
   }
 
   @Override
   public void warning(SAXParseException ex) throws SAXException {
     messages.add(formatMessage("WARN", ex));
   }
 
   @Override
   public void error(SAXParseException ex) throws SAXException {
     messages.add(formatMessage("ERROR", ex));
   }
 
   @Override
   public void fatalError(SAXParseException ex) throws SAXException {
     messages.add(formatMessage("FATAL", ex));
   }
   
   protected String formatMessage(String cat, SAXParseException ex) {
     StringBuilder sb = new StringBuilder();
     sb.append(infoType);
     sb.append('_');
     sb.append(year);
     sb.append('_');
     sb.append(semester);
     sb.append('_');
     sb.append(language);
     sb.append(' ');
     sb.append('[');
     sb.append(cat);
     sb.append("] ");
     sb.append(contextString());
     sb.append(" \"");
     sb.append(currentContent);
     sb.append("\" ");
     sb.append(ex.getLineNumber());
     sb.append(':');
     sb.append(ex.getColumnNumber());
     sb.append(' ');
     sb.append(ex.getLocalizedMessage());
     return sb.toString();
   }
   
   private String contextString() {
     
     StringBuilder sb = new StringBuilder();
     int depth = 0;
     for (String elem : contextPath) {
       sb.append('/');
       sb.append(elem);
       if(depth == 1) {
         if (contextId != null) {
           sb.append('[');
           sb.append(contextId);
           sb.append(']');
         }
       }
       depth++;
     }
     return sb.toString();
   }
 
   public List<String> getMessages() {
     return messages;
   }
 
   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
     contextPath.push(localName);
   }
 
   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
     contextPath.pop();
   }
 
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
     String content = new String(ch, start, length);
     currentContent = content;
     switch (infoType) {
       case STUDIEPROGRAM:
         if (contextPath.peek().equals("studieprogramkode")) {
          if (contextPath.getPath().equals("///fs-studieinfo/studieprogram/studieprogramkode/")) {
             contextId = content;
           }
         } 
         break;
       case EMNE:
         if (contextPath.peek().equals("emnekode")) {
          if (contextPath.getPath().equals("///fs-studieinfo/emne/emneid/emnekode/")) {
             contextId = content;
           }
         }
         break;
       case KURS:
         break;
     }
   }
 }
