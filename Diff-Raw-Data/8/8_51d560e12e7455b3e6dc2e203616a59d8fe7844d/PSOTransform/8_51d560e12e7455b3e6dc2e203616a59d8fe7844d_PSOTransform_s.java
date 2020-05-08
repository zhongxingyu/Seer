 /*
  * com.percussion.pso.transform PSOTransform.java
  *  
  * @author DavidBenua
  *
  */
 package com.percussion.pso.jexl;
 
 import java.io.File;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import com.percussion.data.PSCachedStylesheet;
 import com.percussion.extension.IPSJexlExpression;
 import com.percussion.extension.IPSJexlMethod;
 import com.percussion.extension.IPSJexlParam;
 import com.percussion.extension.PSJexlUtilBase;
 import com.percussion.services.general.IPSRhythmyxInfo;
 import com.percussion.services.general.PSRhythmyxInfoLocator;
 import com.percussion.xmldom.PSStylesheetCacheManager;
 
 /**
  * 
  *
  * @author DavidBenua
  *
  */
 public class PSOTransform extends PSJexlUtilBase implements IPSJexlExpression
 {
    /**
     * Logger for this class
     */
    private static final Log log = LogFactory.getLog(PSOTransform.class);
    
    /**
     * 
     */
    public PSOTransform()
    {
       super();
    }
    
    @IPSJexlMethod(description="Transform a value with an XSLT transform", 
          params={
         @IPSJexlParam(name="source", type="String", description="the source to transform"),
         @IPSJexlParam(name="stylesheetName", type="String", description="the URI of the stylesheet to apply")})
    public String transform(String source, String stylesheetName)
    {
       URL styleFile;
       
       try
       {
          IPSRhythmyxInfo info = PSRhythmyxInfoLocator.getRhythmyxInfo(); 
          String rxRoot = info.getProperty(IPSRhythmyxInfo.Key.ROOT_DIRECTORY).toString();
          log.debug("Rx Root is " + rxRoot);
          File f; 
          if (stylesheetName.startsWith("file:") && stylesheetName.length() > 6 &&
                stylesheetName.charAt(5) != '/')
          {
             f = new File(rxRoot, stylesheetName.substring(5));
             
          }
          else
          {  
             f = new File(rxRoot, stylesheetName); 
             
          }
          log.debug("Stylesheet file is " + f.toString()); 
          styleFile = f.toURL(); 
          PSCachedStylesheet styleCached = 
             PSStylesheetCacheManager.getStyleSheetFromCache(styleFile);
          
          Transformer nt = styleCached.getStylesheetTemplate().newTransformer();
          
          TransformerFactory xfactory = TransformerFactory.newInstance();
          
          Source src = new StreamSource(new StringReader(wrapField(source)));
          StringWriter outString = new StringWriter();
          StreamResult res = new StreamResult(outString);
 
          nt.transform(src, res);
          
          return outString.toString();
 
      } catch (Exception ex)
       {         
         log.error("XSLT Exception " + ex.getMessage(), ex);
       } 
      return null;
    }
    
    private static String wrapField(String field)
    {
       StringBuilder sb = new StringBuilder(); 
       sb.append("<div class=\"rxbodyfield\">");
       sb.append(field);
       sb.append("</div>");
       return sb.toString(); 
    }
 }
