 package org.dasein.cloud.azure;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.AbstractCloud;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.azure.compute.AzureComputeServices;
 import org.dasein.cloud.azure.network.AzureNetworkServices;
 import org.dasein.cloud.azure.storage.AzureStorageServices;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.io.UnsupportedEncodingException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Random;
 
 /**
  * Core cloud provider implementation for the Microsoft Azure cloud.
  * @author George Reese (george.reese@imaginary.com)
  * @since 2012.04.1
  * @version 2012.04.1
  */
 public class Azure extends AbstractCloud {
     static private final Logger logger = Azure.getLogger(Azure.class);
 	
     static private @Nonnull String getLastItem(@Nonnull String name) {
         int idx = name.lastIndexOf('.');
 
         if( idx < 0 ) {
             return name;
         }
         else if( idx == (name.length()-1) ) {
             return "";
         }
         return name.substring(idx + 1);
     }
 
     static public @Nonnull Logger getLogger(@Nonnull Class<?> cls) {
         String pkg = getLastItem(cls.getPackage().getName());
 
         if( pkg.equals("azure") ) {
             pkg = "";
         }
         else {
             pkg = pkg + ".";
         }
         return Logger.getLogger("dasein.cloud.azure.std." + pkg + getLastItem(cls.getName()));
     }
 
     static public @Nonnull Logger getWireLogger(@Nonnull Class<?> cls) {
         return Logger.getLogger("dasein.cloud.azure.wire." + getLastItem(cls.getPackage().getName()) + "." + getLastItem(cls.getName()));
     }
 
 
     public Azure() { }
 
     public String affinityGroup = null;
     public String affinityRegion = null;
 
     static private final Random random = new Random();
     
     public @Nonnull String generateToken(int minLength, int maxLength) {
         if( minLength < 0 ) {
             minLength = 0;
         }
         if( maxLength < minLength ) {
             if( minLength == 0 ) {
                 return "";
             }
             maxLength = minLength;
         }
         int length = ((maxLength == minLength) ? minLength : random.nextInt(maxLength-minLength) + minLength);
         StringBuilder token = new StringBuilder();
 
         token.append('A' + random.nextInt(20));
         while( token.length() < length ) {
             char c = (char)random.nextInt(256);
             
             if( (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >='A' && c <= 'Z') || c == '!' || c == '@' || c == '^' || c == '=' || c == '+' || c == ',' ) {
                 token.append(c);
             }
         }
         return (token.toString() + (System.currentTimeMillis()%1000));
     }
   
     @Override
     public @Nonnull String getCloudName() {
         return "Azure";
     }
 
     public String getDataCenterId(String regionId){
     	return regionId;
     }
 
 
     @Override
     public @Nonnull AzureComputeServices getComputeServices() {
         return new AzureComputeServices(this);
     }
 
     @Override
     public @Nonnull AzureLocation getDataCenterServices() {
         return new AzureLocation(this);
     }
     
     @Override
     public @Nonnull AzureNetworkServices getNetworkServices() {
     	//return null;
     	//Not ready yet
     	return new AzureNetworkServices(this);
     }
 
     private transient String storageEndpoint;
 
     public @Nonnull String getStorageEndpoint() throws CloudException, InternalException {
         if( storageEndpoint == null ) {
             ProviderContext ctx = getContext();
 
             if( ctx == null ) {
                 throw new AzureConfigException("No configuration was set for this request");
             }
             AzureMethod method = new AzureMethod(this);
 
             Document xml = method.getAsXML(ctx.getAccountNumber(), "/services/storageservices");
 
             if( xml == null ) {
                 throw new CloudException("Unable to identify the blob endpoint");
             }
             NodeList endpoints = xml.getElementsByTagName("Endpoint");
 
             for( int i=0; i<endpoints.getLength(); i++ ) {
                 Node node = endpoints.item(i);
 
                 if( node.hasChildNodes() ) {
                     String endpoint = node.getFirstChild().getNodeValue().trim();
 
                     if( endpoint.contains("blob") ) {
                         if( !endpoint.endsWith("/") ) {
                             endpoint = endpoint + "/";
                         }
                         storageEndpoint = endpoint;
                         break;
                     }
                 }
             }
             if( storageEndpoint == null ) {
                 throw new CloudException("There is no blob endpoint");
             }
         }
         return storageEndpoint;
     }
     private transient String storageService;
 
     public @Nullable String getStorageService() throws CloudException, InternalException {
         if( storageService == null ) {
             ProviderContext ctx = getContext();
 
             if( ctx == null ) {
                 throw new AzureConfigException("No configuration was set for this request");
             }
             AzureMethod method = new AzureMethod(this);
 
             Document xml = method.getAsXML(ctx.getAccountNumber(), "/services/storageservices");
 
             if( xml == null ) {
                 throw new CloudException("Unable to identify the blob endpoint");
             }
             NodeList names = xml.getElementsByTagName("ServiceName");
 
             for( int i=0; i<names.getLength(); i++ ) {
                 Node node = names.item(i);
 
                 if( node.hasChildNodes() ) {
                     storageService = node.getFirstChild().getNodeValue().trim();
                     break;
                 }
             }
         }
         return storageService;
     }
 
     @Override
     public @Nonnull AzureStorageServices getStorageServices() {
         return new AzureStorageServices(this);
     }
     
     
     @Override
     public @Nonnull String getProviderName() {
         return "Microsoft";
     }
 
     public String getAffinityGroup() throws InternalException,CloudException{
         if(affinityGroup == null){
             ProviderContext ctx = getContext();
 
             if( ctx == null ) {
                 throw new AzureConfigException("No context was specified for this request");
             }
            logger.info("Get affinity group for "+ctx.getRegionId()+" for account "+ctx.getAccountNumber());
             AzureMethod method = new AzureMethod(this);
 
             Document doc = method.getAsXML(ctx.getAccountNumber(), "/affinitygroups");
 
             NodeList entries = doc.getElementsByTagName("AffinityGroup");
 
             for (int i = 0; i<entries.getLength(); i++) {
                 Node entry = entries.item(i);
 
                 NodeList attributes = entry.getChildNodes();
 
                 for( int j=0; j<attributes.getLength(); j++ ) {
                     Node attribute = attributes.item(j);
                     if(attribute.getNodeType() == Node.TEXT_NODE) continue;
                     String nodeName = attribute.getNodeName();
 
                     if (nodeName.equalsIgnoreCase("name") && attribute.hasChildNodes() ) {
                         affinityGroup = attribute.getFirstChild().getNodeValue().trim();
                     }
                     else if (nodeName.equalsIgnoreCase("location") && attribute.hasChildNodes()) {
                         affinityRegion = attribute.getFirstChild().getNodeValue().trim();
                         if (!ctx.getRegionId().equalsIgnoreCase(affinityRegion)) {
                             affinityGroup = null;
                             affinityRegion = null;
                             break;
                         }
                     }
                 }
             }
             if (affinityGroup == null) {
                logger.info("Create affinity group for "+ctx.getRegionId());
                 //create new affinityGroup
                 String name = "EnstratiusAffinity";
                 String label;
                 try {
                     StringBuilder xml = new StringBuilder();
 
                     try {
                         label = new String(Base64.encodeBase64(name.getBytes("utf-8")));
                     }
                     catch( UnsupportedEncodingException e ) {
                         throw new InternalException(e);
                     }
 
                    xml.append("<CreateAffinityGroup xmlns=\"http://schemas.microsoft.com/windowsazure\">") ;
                     xml.append("<Name>").append(name).append("</Name>");
                     xml.append("<Label>").append(label).append("</Label>");
                     xml.append("<Location>").append(ctx.getRegionId()).append("</Location>");
                     xml.append("</CreateAffinityGroup>");
                    method.post(ctx.getAccountNumber(),"/affinitygroups", xml.toString());
                 }
                 catch (CloudException e) {
                     logger.error("Unable to create affinity group",e);
                     throw new CloudException(e);
                 }
                 affinityGroup = name;
             }
         }
 
         return affinityGroup;
     }
 
     public long parseTimestamp(@Nullable String time) throws CloudException {
         if( time == null ) {
             return 0L;
         }
         SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
 
         if( time.length() > 0 ) {
             try {
                 return fmt.parse(time).getTime();
             }
             catch( ParseException e ) {
                 fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                 try {
                     return fmt.parse(time).getTime();
                 }
                 catch( ParseException encore ) {
                     throw new CloudException("Could not parse date: " + time);
                 }
             }
         }
         return 0L;
     }
 
     @Override
     public @Nullable String testContext() {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER: " + Azure.class.getName() + ".testContext()");
         }
         try {
             try {
                 AzureMethod method = new AzureMethod(this);
                 ProviderContext ctx = getContext();
                 
                 if( ctx == null ) {
                     logger.error("No context was specified for a context test");
                     return null;
                 }
 
                /* logger.debug("--------------Context-------------");
                 logger.debug("Account number: "+ctx.getAccountNumber());
                 logger.debug("X509 cert: "+new String(ctx.getX509Cert(), "utf-8"));
                 logger.debug("X509 key: "+new String(ctx.getX509Key(), "utf-8"));
                 logger.debug("--------------Context-------------");
                 */
                 if( method.getAsStream(ctx.getAccountNumber(), "/locations") == null ) {
                     logger.warn("Account number was invalid for context test: " + ctx.getAccountNumber());
                     return null;
                 }
                 if( logger.isDebugEnabled() ) {
                     logger.debug("Valid account: " + ctx.getAccountNumber());
                 }
                 return ctx.getAccountNumber();
             }
             catch( Exception e ) {
                 logger.error("Failed to test context: " + e.getMessage());
                 e.printStackTrace();
                 return null;
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT: " + Azure.class.getName() + ".testContext()");
             }
         }
     }
 }
