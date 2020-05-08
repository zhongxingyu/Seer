 /*
  * $Id$
  *
  * Copyright 2003-2006 Wanadoo Nederland B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.management.openmbean.CompositeDataSupport;
 import javax.management.openmbean.CompositeType;
 import javax.management.openmbean.OpenType;
 import javax.management.openmbean.SimpleType;
 import javax.management.openmbean.TabularDataSupport;
 import javax.management.openmbean.TabularType;
 import org.xins.common.Utils;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.collections.PropertyReaderConverter;
 import org.xins.common.collections.PropertyReaderUtils;
 import org.xins.common.text.DateConverter;
 import org.xins.common.xml.Element;
 
 /**
  * Management bean for the API.
  *
  * @version $Revision$ $Date$
  * @author Anthony Goubard (<a href="mailto:anthony.goubard@nl.wanadoo.com">anthony.goubard@nl.wanadoo.com</a>)
  *
  * @since XINS 1.5.0
  */
 public final class APIManager implements APIManagerMBean {
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    APIManager(API api) {
       _api = api;
       try {
          _ip = InetAddress.getLocalHost().getHostAddress();
       } catch (UnknownHostException uhex) {
          Log.log_3250(uhex);
          _ip = "127.0.0.1";
       }
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The API, never <code>null</code>
     */
    private final API _api;
 
    /**
     * The IP address runing this class, never <code>null</code>.
     */
    private String _ip;
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the version of the API.
     *
     * @return
     *    the version of the API running.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public String getAPIVersion() throws IOException {
       return _api.getBootstrapProperties().get(API.API_VERSION_PROPERTY);
    }
 
    /**
     * Gets the version of XINS which is running this API.
     *
     * @return
     *    the version of XINS running the API.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public String getXINSVersion() throws IOException {
       return Library.getVersion();
    }
 
    /**
     * Gets the name of the API.
     *
     * @return
     *    the name the API.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public String getAPIName() throws IOException {
       return _api.getName();
    }
 
    /**
     * Gets the bootstrap properties.
     *
     * @return
     *    the bootstrap properties for this API.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public CompositeDataSupport getBootstrapProperties() throws IOException {
        Properties bootstrapProps = PropertyReaderConverter.toProperties(_api.getBootstrapProperties());
        return propertiesToCompositeData(bootstrapProps);
    }
 
    /**
     * Gets the runtime properties.
     *
     * @return
     *    the runtime properties for this API.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public CompositeDataSupport getRuntimeProperties() throws IOException {
       Properties runtimeProps = PropertyReaderConverter.toProperties(_api.getRuntimeProperties());
       return propertiesToCompositeData(runtimeProps);
    }
 
    /**
     * Gets the time at which the API was started.
     *
     * @return
     *    the time at which the API was started in the form YYYYMMDDThhmmssSSS+TZ.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public String getStartupTime() throws IOException {
       return DateConverter.toDateString(_api.getStartupTimestamp());
    }
     
 
    /**
     * Gets the list of the API functions.
     *
     * @return
     *    the list of the API function names.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public String[] getFunctionNames() throws IOException {
       List functions =  _api.getFunctionList();
       String[] functionNames = new String[functions.size()];
       for (int i = 0; i < functions.size(); i++) {
          Function nextFunction = (Function) functions.get(i);
          functionNames[i] = nextFunction.getName();
       }
       return functionNames;
    }
     
    /**
     * Gets the statistics of the functions.
     *
     * @return
     *    the statistics of the functions.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public TabularDataSupport getStatistics() throws IOException {
      String[] statsNames = {"count", "error code", "average"};
      OpenType[] statsTypes = {SimpleType.STRING, SimpleType.STRING, SimpleType.STRING};
       try {
          CompositeType statType = new CompositeType("Statistic", 
                "A statistic of a function", statsNames, statsNames, statsTypes);
          TabularType tabType = new TabularType("Function statistics", 
               "Statistics of the functions", statType, getFunctionNames());
          TabularDataSupport tabularData = new TabularDataSupport(tabType);
          Iterator itFunctions =  _api.getFunctionList().iterator();
          while (itFunctions.hasNext()) {
             Function nextFunction = (Function) itFunctions.next();
             Element success = nextFunction.getStatistics().getSuccessfulElement();
             HashMap statMap = new HashMap();
             statMap.put("count", success.getAttribute("count"));
             statMap.put("error code", "");
             statMap.put("average", success.getAttribute("average"));
             CompositeDataSupport statData = new CompositeDataSupport(statType, statMap);
             tabularData.put(statData);
          }
          
          return tabularData;
       } catch (Exception ex) {
          ex.printStackTrace();
          return null;
       }
    }
 
    /**
     * Executes the _NoOp meta function.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public void noOp() throws IOException, NoSuchFunctionException, AccessDeniedException {
       FunctionRequest noOpRequest = new FunctionRequest("_NoOp",
             PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
       _api.handleCall(System.currentTimeMillis(), noOpRequest, _ip);
    }
 
    /**
     * Reloads the runtime properties if the file has changed.
     *
     * @throws IOException
     *    if the connection to the MBean fails.
     */
    public void reloadProperties() throws IOException, NoSuchFunctionException, AccessDeniedException {
       FunctionRequest reloadPropertiesRequest = new FunctionRequest("_ReloadProperties",
             PropertyReaderUtils.EMPTY_PROPERTY_READER, null);
       _api.handleCall(System.currentTimeMillis(), reloadPropertiesRequest, _ip);
    }
 
    /**
     * Utility method to convert a {@link Properties} to a {@link CompositeDataSupport}
     *
     * @param properties
     *    the properties to represent to the JMX agent, cannot be <code>null</code>.
     *
     * @return
     *    the {@link CompositeDataSupport} containng the properties, or <code>null</code>
     *    if an error occured.
     */
    private CompositeDataSupport propertiesToCompositeData(Properties properties) {
        try {
           //String[] itemNames = {"key", "value"};
           String[] keys = (String[]) properties.keySet().toArray(new String[0]);
           OpenType[] itemTypes = new OpenType[keys.length];
           for (int i = 0; i < itemTypes.length; i++) {
              itemTypes[i] = SimpleType.STRING;
           }
           CompositeType propsType = new CompositeType("Properties type", "properties", keys, keys, itemTypes);
           //TabularType tabType = new TabularType("bootstrapProperties", "bootstrap properties", propsType, keys);
           //TabularDataSupport tabSupport = new TabularDataSupport(tabType);
           CompositeDataSupport propsData = new CompositeDataSupport(propsType, properties);
           //tabSupport.putAll(bootstrapProps);
           //tabSupport.put(propsData);
           //return tabSupport;
           return propsData;
        } catch (Exception ex) {
           ex.printStackTrace();
           return null;
        }
    }
 }
