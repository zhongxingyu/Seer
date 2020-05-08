 package dk.statsbiblioteket.doms.common;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 
 public class SimpleFFProbeParser {
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     public static String getFormatURIFromFile(File ffProbeFile)
             throws XPathExpressionException, ParserConfigurationException,
             IOException, SAXException {
 
         String ffProbeOutput = "";
 
         BufferedReader reader = new BufferedReader(new FileReader(ffProbeFile));
 
         String line;
         while ((line = reader.readLine()) != null) {
             ffProbeOutput += line + System.lineSeparator();
         }
 
         return getFormatURIFromFFProbeOutput(ffProbeOutput);
     }
 
     public static String getFormatURIFromFFProbeOutput(String ffProbeOutput)
             throws XPathExpressionException, ParserConfigurationException,
             IOException, SAXException {
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(false);
         DocumentBuilder builder = factory.newDocumentBuilder();
         Document doc = builder.parse(new ByteArrayInputStream(
                 ffProbeOutput.getBytes()));
         XPathFactory xPathfactory = XPathFactory.newInstance();
 
         XPath xpath;
         XPathExpression expr;
 
         // Get format name
         String formatName;
         xpath = xPathfactory.newXPath();
         expr = xpath.compile("ffprobe/format/@format_name");
         formatName = expr.evaluate(doc);
         if (formatName.trim().isEmpty()){
             throw new RuntimeException("Invalid ffprobe file, no format_name");
         }
 
         // Get codec name
         xpath = xPathfactory.newXPath();
         expr = xpath.compile("ffprobe/streams/stream/@codec_name");
         NodeList codecsNodeList
                 = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
         Set<String> codecSet = new HashSet<String>();
         // Convert nodelist to a set of strings, to remove duplicates
 
         for(int i = 0; i < codecsNodeList.getLength(); i++) {
             codecSet.add(codecsNodeList.item(i).getNodeValue());
         }
 
         // Convert set to a list of strings
         List<String> codecs = new ArrayList<String>(codecSet);
         // Sort it
         Collections.sort(codecs);
 
         formatName = formatName.toLowerCase();
 
        return formatUriFromFFPRobe(formatName, codecs);
    }

    private static String formatUriFromFFPRobe(String formatName, List<String> codecs) {
         String formatUri = null;
 
        if (formatName.equals("wav")) { // wave
             formatUri = "info:pronom/fmt/6";
        } else if (formatName.equals("mpeg")) { // mpeg
             for (String codec : codecs) {
                 if (codec.equals("mpeg1video")) {
                     formatUri = "info:pronom/x-fmt/385";
                     break;
                } else if (codec.equals("mpeg2video")) {
                    formatUri = "info:pronom/x-fmt/386";
                     break;
                 }
             }
        } else if (formatName.equals("mpegts")) { // transport streams
             formatUri = "info:mime/video/MP2T";
             if (codecs.size() > 0){
                 formatUri += ";codecs=\"";
                 for (int i = 0; i < codecs.size(); i++){
                     String codec = codecs.get(i);
                     formatUri += codec;
                     if (i+1 != codecs.size()){
                         formatUri += ",";
                     }
                 }
                 formatUri += "\"";
             }
        } else if (formatName.equals("asf")) { // wmv
             formatUri = "info:pronom/fmt/133";
        } else if (formatName.equals("mov,mp4,m4a,3gp,3g2,mj2")) { // mp4
             formatUri = "info:pronom/fmt/199";
         }
         return formatUri;
     }
 }
