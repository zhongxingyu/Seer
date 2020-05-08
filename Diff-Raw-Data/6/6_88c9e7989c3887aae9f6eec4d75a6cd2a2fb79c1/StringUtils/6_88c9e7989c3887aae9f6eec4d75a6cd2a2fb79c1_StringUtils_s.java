 package org.n52.util;
 
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 /**
  * @author <a href="mailto:broering@52north.org">Arne Broering</a>
  */
 public class StringUtils {
 
     /**
      * 
      * @param baseString
      * @param wordToFind
      * @param wordToUse
      * @return
      */
     public static StringBuilder replaceWordInString(StringBuilder baseString, String wordToFind, String wordToUse)
     {
         int i = baseString.indexOf(wordToFind);
         while(i != -1) {
             baseString = baseString.replace(i, i + wordToFind.length(), wordToUse);
             
             int fromIndex = i + wordToFind.length() + 1;
             
             i = baseString.indexOf(wordToFind, fromIndex);
         }
         
         return baseString;
     }
     
     /**
      * Takes an InputStream containing a String and converts it to pretty XML using the specified indent.
      * @throws TransformerException 
      */
     public static String prettyFormatAsXML(InputStream inputStreamString, int indent) throws TransformerException {
         return prettyFormatAsXML(new StreamSource(inputStreamString), indent);
     }
     
     /**
      * Takes a String and converts it to pretty XML using the specified indent.
      * @throws TransformerException 
      */
     public static String prettyFormatAsXML(String inputString, int indent) throws TransformerException {
         return prettyFormatAsXML(new StreamSource(new StringReader(inputString)), indent);
     }
     
     /**
      * Takes a javax.xml.transform.Source containing a String and converts it to pretty XML using the specified indent.
      * @throws TransformerException 
      */
     public static String prettyFormatAsXML(Source xmlInput, int indent) throws TransformerException {
         StringWriter stringWriter = new StringWriter();
         StreamResult xmlOutput = new StreamResult(stringWriter);
         
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setAttribute("indent-number", indent);
        Transformer transformer = transformerFactory.newTransformer(); 
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         
         transformer.transform(xmlInput, xmlOutput);
         
         return xmlOutput.getWriter().toString();
     }
     
 }
