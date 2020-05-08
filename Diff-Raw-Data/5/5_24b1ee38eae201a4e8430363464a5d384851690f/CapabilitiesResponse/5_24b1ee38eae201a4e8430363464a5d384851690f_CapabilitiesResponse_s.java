 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses;
 
 import org.vfny.geoserver.*;
 import org.vfny.geoserver.config.*;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.responses.*;
 import org.vfny.geoserver.responses.wfs.*;
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 import java.io.*;
 import java.nio.charset.Charset;
 import javax.xml.transform.*;
 import javax.xml.transform.sax.*;
 import javax.xml.transform.stream.*;
 
 
 /**
  * DOCUMENT ME!
  *
  * @author Gabriel Roldn
 * @version $Id: CapabilitiesResponse.java,v 1.22 2004/01/02 21:06:32 cholmesny Exp $
  */
 public abstract class CapabilitiesResponse extends XMLFilterImpl
     implements Response, XMLReader {
     private static OutputStream nullOutputStream = new OutputStream() {
             public void write(int b) throws IOException {
             }
 
             public void write(byte[] b) throws IOException {
             }
 
             public void write(byte[] b, int off, int len)
                 throws IOException {
             }
 
             public void flush() throws IOException {
             }
 
             public void close() throws IOException {
             }
         };
 
     /** handler to do the processing */
     private ContentHandler contentHandler;
     protected String service;
 
     /**
      * writes to a void output stream to throw any exception that can occur in
      * writeTo too.
      *
      * @param request DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      */
     public void execute(Request request) throws ServiceException {
         this.service = request.getService();
         writeTo(nullOutputStream);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentType() {
         return ServerConfig.getInstance().getGlobalConfig().getMimeType();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param out DOCUMENT ME!
      *
      * @throws ServiceException DOCUMENT ME!
      * @throws WfsException DOCUMENT ME!
      *
      * @task REVISIT: should we do our own xml declaration?  Will UTF-8 always
      *       be sufficient, or do we need to allow other encodings? can we do
      *       that programatically?
      */
     public void writeTo(OutputStream out) throws ServiceException {
         try {
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
 
             // don't know what this should be, or if its even important
             InputSource inputSource = new InputSource("XXX");
             SAXSource source = new SAXSource(this, inputSource);
             Charset charset = ServerConfig.getInstance().getGlobalConfig()
                                           .getCharSet();
             Writer writer = new OutputStreamWriter(out, charset);
 
             //HACK: This should be done programatically, but for the life of
             //me I can't find it.  There seems to be no output key that 
             //allows you to set _where_ to find the dtd.  And I found
             //transformer stuff to read in dtd elements like this, but none
             //to declare them and have them transformed.  If someone knows
             //how to get rid of this horrible hack please let me know.
             if ((this.service != null) && this.service.equals("WMS")) {
                 transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                     "yes");
 
                 //TODO: Should reference a local capabilities_1_1_1.dtd
                 String declaration = "<?xml version='1.0' encoding=\"UTF-8\" "
                     + "standalone=\"no\" ?>  "
                     + "<!DOCTYPE WMT_MS_Capabilities SYSTEM "
                    + +"\"http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd\">";
 
                 writer.write(declaration, 0, declaration.length());
             }
 
             StreamResult result = new StreamResult(writer);
 
             transformer.transform(source, result);
         } catch (TransformerException ex) {
             throw new WfsException(ex);
         } catch (TransformerFactoryConfigurationError ex) {
             throw new WfsException(ex);
         } catch (java.io.IOException ex) {
             throw new WfsException(ex);
         }
     }
 
     /**
      * sets the content handler.
      *
      * @param handler DOCUMENT ME!
      */
     public void setContentHandler(ContentHandler handler) {
         contentHandler = handler;
     }
 
     /**
      * walks the given collection.
      *
      * @param systemId DOCUMENT ME!
      *
      * @throws java.io.IOException DOCUMENT ME!
      * @throws SAXException DOCUMENT ME!
      */
     public void parse(String systemId) throws java.io.IOException, SAXException {
         walk();
     }
 
     /**
      * walks the given collection.
      *
      * @param input DOCUMENT ME!
      *
      * @throws java.io.IOException DOCUMENT ME!
      * @throws SAXException DOCUMENT ME!
      */
     public void parse(InputSource input)
         throws java.io.IOException, SAXException {
         walk();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws SAXException DOCUMENT ME!
      */
     protected void walk() throws SAXException {
         contentHandler.startDocument();
 
         ServiceConfig service = getServiceConfig();
         ResponseHandler handler = getResponseHandler(contentHandler);
         handler.handleDocument(service);
         handler.endDocument(service);
         contentHandler.endDocument();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected abstract ServiceConfig getServiceConfig();
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     protected abstract ResponseHandler getResponseHandler(
         ContentHandler contentHandler);
 }
