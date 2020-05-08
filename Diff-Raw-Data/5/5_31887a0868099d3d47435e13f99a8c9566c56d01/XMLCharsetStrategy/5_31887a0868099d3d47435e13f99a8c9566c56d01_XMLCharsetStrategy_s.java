 /*
  * Copyright (c) 2010 Zauber S.A.  -- All rights reserved
  */
 package ar.com.zauber.leviathan.impl.httpclient.charset;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.charset.Charset;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import ar.com.zauber.leviathan.common.CharsetStrategy;
 import ar.com.zauber.leviathan.common.ResponseMetadata;
 
 /**
  * {@link CharsetStrategy} para XMLs. Obtiene el charset a partir del header del
  * XML.
  * 
  * 
  * @author Francisco J. Gonzlez Costanz
  * @since Apr 9, 2010
  */
 public class XMLCharsetStrategy implements CharsetStrategy {
 
     /** @see CharsetStrategy#getCharset(ResponseMetadata, InputStream) */
     public final Charset getCharset(final ResponseMetadata meta,
             final InputStream content) {
         Validate.notNull(meta);
        if (!meta.getContentType().startsWith("text/xml")) {
            return null; 
         }
         
         try {
             final DocumentBuilder documentBuilder = DocumentBuilderFactory
                     .newInstance().newDocumentBuilder();
             final Document dom = documentBuilder.parse(content);
             final String xmlEncoding = dom.getXmlEncoding();
             if (!StringUtils.isBlank(xmlEncoding)) {
                 return Charset.forName(xmlEncoding);
             }
         } catch (ParserConfigurationException e) {
             // nada que hacer
         } catch (SAXException e) {
             // nada que hacer
         } catch (IOException e) {
             // nada que hacer
         }
         
         return null;
     }
 
 }
