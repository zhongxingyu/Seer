 package com.dtolabs.rundeck.plugin.resources.url;
 
 import java.io.*;
 import java.util.Properties;
 import com.dtolabs.rundeck.core.resources.ResourceModelSource;
 import com.dtolabs.rundeck.core.resources.ResourceModelSourceException;
 import com.dtolabs.rundeck.core.resources.format.ResourceFormatParserException;
 import com.dtolabs.rundeck.core.resources.format.ResourceXMLFormatParser;
 import com.dtolabs.rundeck.core.plugins.configuration.ConfigurationException;
 import com.dtolabs.rundeck.core.common.INodeSet;
 
 public class XMLResourceModelSource implements ResourceModelSource {
 
    private AsynchronousWorker asynchronousWorker;
    public XMLResourceModelSource(final Properties configuration) {
 
 
      String resourcesUrl = configuration.getProperty(AsynchronousResourceModelSourceFactory.RESOURCES_URL_KEY);
      int refreshInterval = Integer.parseInt(configuration.getProperty(AsynchronousResourceModelSourceFactory.REFRESH_INTERVAL_KEY));
       this.asynchronousWorker = new AsynchronousWorker(resourcesUrl, refreshInterval);
       this.asynchronousWorker.initialize();
    }
 
    public synchronized INodeSet getNodes() throws ResourceModelSourceException {
 
       ResourceXMLFormatParser resourceFormatParser = new ResourceXMLFormatParser();
 
       InputStream is = null;
       INodeSet iNodeSet = null;
       try {
          is = new ByteArrayInputStream(this.asynchronousWorker.getUrlWorker().getVolatileResourcesXml().getBytes("UTF-8"));
          iNodeSet = resourceFormatParser.parseDocument(is);
       } catch (IOException e) {
          e.printStackTrace();
          throw new ResourceModelSourceException("caught IOException");
       } catch (ResourceFormatParserException e) {
          e.printStackTrace();
          throw new ResourceModelSourceException("caught ResourceFormatParserException");
       }
 
       return iNodeSet;
    }
 
     public void validate() throws ConfigurationException {
        if (false) throw new ConfigurationException("never thrown");
     }
 
 }
