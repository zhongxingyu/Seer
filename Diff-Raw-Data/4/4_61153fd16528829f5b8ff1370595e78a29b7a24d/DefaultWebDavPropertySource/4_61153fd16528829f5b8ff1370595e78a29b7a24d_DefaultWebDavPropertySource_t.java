 package com.bradmcevoy.http.webdav;
 
 import com.bradmcevoy.property.PropertySource;
 import com.bradmcevoy.http.CollectionResource;
 import com.bradmcevoy.http.DateUtils;
 import com.bradmcevoy.http.GetableResource;
 import com.bradmcevoy.http.LockToken;
 import com.bradmcevoy.http.LockableResource;
 import com.bradmcevoy.http.PropFindableResource;
 import com.bradmcevoy.http.PutableResource;
 import com.bradmcevoy.http.Resource;
 import com.bradmcevoy.http.Utils;
 import com.bradmcevoy.http.XmlWriter;
 import com.bradmcevoy.http.http11.DefaultHttp11ResponseHandler;
 import com.bradmcevoy.http.quota.DefaultQuotaDataAccessor;
 import com.bradmcevoy.http.quota.QuotaDataAccessor;
 import com.bradmcevoy.http.webdav.WebDavProtocol.SupportedLocks;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.namespace.QName;
 
 /**
  *
  * @author brad
  */
 public class DefaultWebDavPropertySource implements PropertySource {
 
     private final Map<String, StandardProperty> writersMap = new HashMap<String, StandardProperty>();
     private final ResourceTypeHelper resourceTypeHelper;
     private final QuotaDataAccessor quotaDataAccessor;
 
     public DefaultWebDavPropertySource( ResourceTypeHelper resourceTypeHelper ) {
         this(resourceTypeHelper, new DefaultQuotaDataAccessor());
     }
 
     public DefaultWebDavPropertySource(ResourceTypeHelper resourceTypeHelper, QuotaDataAccessor quotaDataAccessor) {
         this.resourceTypeHelper = resourceTypeHelper;
         this.quotaDataAccessor = new DefaultQuotaDataAccessor();
         add( new ContentLengthPropertyWriter() );
         add( new ContentTypePropertyWriter() );
         add( new CreationDatePropertyWriter() );
         add( new DisplayNamePropertyWriter() );
         add( new LastModifiedDatePropertyWriter() );
         add( new ResourceTypePropertyWriter() );
         add( new EtagPropertyWriter() );
 
         add( new SupportedLockPropertyWriter() );
         add( new LockDiscoveryPropertyWriter() );
 
         add( new MSIsCollectionPropertyWriter() );
         add( new MSIsReadOnlyPropertyWriter() );
         add( new MSNamePropertyWriter() );
 
         add( new QuotaAvailableBytesPropertyWriter() );
         add( new QuotaUsedBytesPropertyWriter() );
 
     }
 
     public Object getProperty( QName name, Resource r ) {
         if( !name.getNamespaceURI().equals( WebDavProtocol.NS_DAV ) )
             return null;
         StandardProperty pa = writersMap.get( name.getLocalPart() );
         if( pa == null ) return null;
         if( r instanceof PropFindableResource ) {
             return pa.getValue( (PropFindableResource) r );
         } else {
             return null;
         }
     }
 
     public void setProperty( QName name, Object value, Resource r ) {
         throw new UnsupportedOperationException( "Cannot set readonly property: " + name );
     }
 
     public void clearProperty( QName name, Resource r ) {
         throw new UnsupportedOperationException( "Cannot set readonly property: " + name );
     }
 
     public List<QName> getAllPropertyNames( Resource r ) {
         List<QName> list = new ArrayList<QName>();
         for( String nm : this.writersMap.keySet() ) {
             QName qname = new QName( WebDavProtocol.NS_DAV, nm );
             list.add( qname );
         }
         return list;
     }
 
     public PropertyMetaData getPropertyMetaData( QName name, Resource r ) {
         if( !name.getNamespaceURI().equals( WebDavProtocol.NS_DAV ) )
             return PropertyMetaData.UNKNOWN;
         StandardProperty pa = writersMap.get( name.getLocalPart() );
         if( pa == null ) {
             return PropertyMetaData.UNKNOWN;
         } else {
             if( r instanceof PropFindableResource ) {
                 return new PropertyMetaData( PropertyAccessibility.READ_ONLY, pa.getValueClass() );
             } else {
                 return PropertyMetaData.UNKNOWN;
             }
         }
     }
 
     public interface StandardProperty<T> {
 
         String fieldName();
 
         T getValue( PropFindableResource res );
 
         Class getValueClass();
     }
 
     class DisplayNamePropertyWriter implements StandardProperty<String> {
 
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
             String s = nameEncode( getValue( res ) );
             sendStringProp( writer, "D:" + fieldName(), s );
         }
 
         public String getValue( PropFindableResource res ) {
             return res.getName();
         }
 
         public String fieldName() {
             return "displayname";
         }
 
         public Class<String> getValueClass() {
             return String.class;
         }        
     }
 
     class LastModifiedDatePropertyWriter implements StandardProperty<Date> {
 
         public String fieldName() {
             return "getlastmodified";
         }
 
         public Date getValue( PropFindableResource res ) {
             return res.getModifiedDate();
         }
 
         public Class<Date> getValueClass() {
             return Date.class;
         }
     }
 
     class CreationDatePropertyWriter implements StandardProperty<Date> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             sendDateProp( xmlWriter, "D:" + fieldName(), getValue( res ) );
         }
 
         public Date getValue( PropFindableResource res ) {
             return res.getCreateDate();
         }
 
         public String fieldName() {
             return "creationdate";
         }
 
         public Class<Date> getValueClass() {
             return Date.class;
         }
     }
 
     class ResourceTypePropertyWriter implements StandardProperty<List<QName>> {
 
         public List<QName> getValue( PropFindableResource res ) {
             return resourceTypeHelper.getResourceTypes( res );
         }
 
         public String fieldName() {
             return "resourcetype";
         }
 
         public Class getValueClass() {
             return List.class;
         }
 
 
     }
 
     class ContentTypePropertyWriter implements StandardProperty<String> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             String ct = getValue( res );
             sendStringProp( xmlWriter, "D:" + fieldName(), ct );
         }
 
         public String getValue( PropFindableResource res ) {
             if( res instanceof GetableResource ) {
                 GetableResource getable = (GetableResource) res;
                 return getable.getContentType( null );
             } else {
                 return "";
             }
         }
 
         public String fieldName() {
             return "getcontenttype";
         }
 
         public Class getValueClass() {
             return String.class;
         }
     }
 
     class ContentLengthPropertyWriter implements StandardProperty<Long> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             Long ll = getValue( res );
             sendStringProp( xmlWriter, "D:" + fieldName(), ll == null ? "" : ll.toString() );
         }
 
         public Long getValue( PropFindableResource res ) {
             if( res instanceof GetableResource ) {
                 GetableResource getable = (GetableResource) res;
                 Long l = getable.getContentLength();
                 return l;
             } else {
                 return null;
             }
         }
 
         public String fieldName() {
             return "getcontentlength";
         }
 
         public Class getValueClass() {
             return Long.class;
         }
     }
 
 
     class QuotaUsedBytesPropertyWriter implements StandardProperty<Long> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             Long ll = getValue( res );
             sendStringProp( xmlWriter, "D:" + fieldName(), ll == null ? "" : ll.toString() );
         }
 
         public Long getValue( PropFindableResource res ) {
             return quotaDataAccessor.getQuotaUsed( res );
         }
 
         public String fieldName() {
             return "quota-used-bytes";
         }
 
         public Class getValueClass() {
             return Long.class;
         }
     }
 
 
     class QuotaAvailableBytesPropertyWriter implements StandardProperty<Long> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             Long ll = getValue( res );
             sendStringProp( xmlWriter, "D:" + fieldName(), ll == null ? "" : ll.toString() );
         }
 
         public Long getValue( PropFindableResource res ) {
             return quotaDataAccessor.getQuotaAvailable( res );
         }
 
         public String fieldName() {
             return "quota-available-bytes";
         }
 
         public Class getValueClass() {
             return Long.class;
         }
     }
 
 
 
     class EtagPropertyWriter implements StandardProperty<String> {
 
         public void append( XmlWriter writer, PropFindableResource resource, String href ) {
             String etag = getValue( resource );
             if( etag != null ) {
                 sendStringProp( writer, "D:getetag", etag );
             }
         }
 
         public String getValue( PropFindableResource res ) {
             String etag = DefaultHttp11ResponseHandler.generateEtag( res );
             return etag;
         }
 
         public String fieldName() {
             return "getetag";
         }
 
         public Class getValueClass() {
             return String.class;
         }
     }
 
 //    <D:supportedlock/><D:lockdiscovery/>
     class LockDiscoveryPropertyWriter implements StandardProperty<LockToken> {
 
         public LockToken getValue( PropFindableResource res ) {
             if( !( res instanceof LockableResource ) ) return null;
             LockableResource lr = (LockableResource) res;
             LockToken token = lr.getCurrentLock();
             return token;
         }
 
         public String fieldName() {
            return "lockdiscovery";
         }
 
         public Class getValueClass() {
             return LockToken.class;
         }
     }
 
     class SupportedLockPropertyWriter implements StandardProperty<SupportedLocks> {
 
         public SupportedLocks getValue( PropFindableResource res ) {
             if( res instanceof LockableResource ) {
                 return new SupportedLocks();
             } else {
                 return null;
             }
         }
 
         public String fieldName() {
             return "supportedlock";
         }
 
         public Class getValueClass() {
             return SupportedLocks.class;
         }
     }
 
     // MS specific fields
     class MSNamePropertyWriter extends DisplayNamePropertyWriter {
 
         @Override
         public String fieldName() {
             return "name";
         }
     }
 
     class MSIsCollectionPropertyWriter implements StandardProperty<Boolean> {
 
         @Override
         public String fieldName() {
             return "iscollection";
         }
 
         public Boolean getValue( PropFindableResource res ) {
             return ( res instanceof CollectionResource );
         }
 
         public Class getValueClass() {
             return Boolean.class;
         }
 
 
     }
 
     class MSIsReadOnlyPropertyWriter implements StandardProperty<Boolean> {
 
         @Override
         public String fieldName() {
             return "isreadonly";
         }
 
         public Boolean getValue( PropFindableResource res ) {
             return !( res instanceof PutableResource );
         }
 
         public Class getValueClass() {
             return Boolean.class;
         }
 
     }
 
     private String nameEncode( String s ) {
         //return Utils.encode(href, false); // see MIL-31
         return Utils.escapeXml( s );
         //return href.replaceAll("&", "&amp;");  // http://www.ettrema.com:8080/browse/MIL-24
     }
 
     protected void sendStringProp( XmlWriter writer, String name, String value ) {
         String s = value;
         if( s == null ) {
             writer.writeProperty( null, name );
         } else {
             writer.writeProperty( null, name, s );
         }
     }
 
     void sendDateProp( XmlWriter writer, String name, Date date ) {
         sendStringProp( writer, name, ( date == null ? null : DateUtils.formatDate( date ) ) );
     }
 
     private void add( StandardProperty pw ) {
         writersMap.put( pw.fieldName(), pw );
     }
 }
