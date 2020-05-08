 package de.escidoc.core.cmm.business.fedora.contentModel;
 
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.Constants;
 import de.escidoc.core.common.business.fedora.HandlerBase;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.cmm.ContentModel;
 import de.escidoc.core.common.business.fedora.resources.cmm.DsTypeModel;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ContentModelXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ContentModelHandlerRetrieve extends HandlerBase {
 
     private ContentModel contentModel;
 
     // TODO ContentModelHandlerBase ?
     public ContentModel getContentModel() {
         return this.contentModel;
     }
 
     // TODO ContentModelHandlerBase ?
     protected void setContentModel(final String id) throws ContentModelNotFoundException, TripleStoreSystemException,
         IntegritySystemException, FedoraSystemException, WebserverSystemException {
 
         try {
             this.contentModel = new ContentModel(id);
         }
         catch (final StreamNotFoundException e) {
             throw new ContentModelNotFoundException(e.getMessage(), e);
         }
         catch (final ResourceNotFoundException e) {
             throw new ContentModelNotFoundException(e.getMessage(), e);
         }
     }
 
     // TODO ContentHandlerRetrieve ?
     protected String render() throws WebserverSystemException, ContentModelNotFoundException,
         TripleStoreSystemException, IntegritySystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         values.putAll(getCommonValues(getContentModel()));
         values.putAll(getPropertiesValues(getContentModel()));
         values.putAll(getMdRecordDefinitionsValues());
         values.putAll(getResourceDefinitionsValues());
         values.put(XmlTemplateProviderConstants.CONTENT_STREAMS, renderContentStreams(false));
         values.putAll(getResourcesValues(getContentModel()));
 
         return ContentModelXmlProvider.getInstance().getContentModelXml(values);
     }
 
    protected String renderProperties() throws WebserverSystemException {

         final Map<String, String> values = getCommonValues(getContentModel());
        values.putAll(getResourcesValues(getContentModel()));
        values.put(XmlTemplateProviderConstants.IS_ROOT_RESOURCES, XmlTemplateProviderConstants.TRUE);
         return ContentModelXmlProvider.getInstance().getContentModelPropertiesXml(values);
     }
 
     // TODO ContentHandlerRetrieve ?
     protected String renderResources() throws WebserverSystemException {
 
         final Map<String, String> values = getCommonValues(getContentModel());
         values.putAll(getResourcesValues(getContentModel()));
         values.put(XmlTemplateProviderConstants.IS_ROOT_RESOURCES, XmlTemplateProviderConstants.TRUE);
         return ContentModelXmlProvider.getInstance().getContentModelResourcesXml(values);
     }
 
     // TODO ContentHandlerRetrieve ?
     protected String renderResourceDefinitions() {
         throw new UnsupportedOperationException("Not yet implemented.");
     }
 
     // TODO ContentHandlerRetrieve ?
     protected String renderResourceDefinition(final String name) {
         throw new UnsupportedOperationException("Not yet implemented.");
     }
 
     // TODO ContentModelHandlerRetrieve ?
     protected String renderContentStreams(final boolean isRoot) throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         final Map<String, String> commonValues = getCommonValues(getContentModel());
         values.putAll(commonValues);
 
         final StringBuilder content = new StringBuilder();
         for (final String contentStreamName : getContentModel().getContentStreams().keySet()) {
             content.append(renderContentStream(contentStreamName, false));
         }
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAMS_HREF, getContentModel().getHref()
             + Constants.CONTENT_STREAMS_URL_PART);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAMS_TITLE, "Content streams of Item "
             + getContentModel().getId());
 
         if (content.length() == 0) {
             return "";
         }
 
         if (isRoot) {
             values.put(XmlTemplateProviderConstants.IS_ROOT_SUB_RESOURCE, XmlTemplateProviderConstants.TRUE);
         }
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAMS_CONTENT, content.toString());
         return ContentModelXmlProvider.getInstance().getContentStreamsXml(values);
 
     }
 
     // TODO ContentModelHandlerRetrieve ?
     protected String renderContentStream(final String name, final boolean isRoot) throws WebserverSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
 
         if (isRoot) {
             values.put("isRootContentStream", XmlTemplateProviderConstants.TRUE);
         }
         final Map<String, String> commonValues = getCommonValues(getContentModel());
         values.putAll(commonValues);
 
         final Datastream ds = getContentModel().getContentStream(name);
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_NAME, ds.getName());
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_TITLE, ds.getLabel());
         String location = ds.getLocation();
         if ("M".equals(ds.getControlGroup()) || "X".equals(ds.getControlGroup())) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_STORAGE, Constants.STORAGE_INTERNAL_MANAGED);
             location =
                 getContentModel().getHref() + Constants.CONTENT_STREAM_URL_PART + '/' + ds.getName()
                     + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;
             if ("X".equals(ds.getControlGroup())) {
                 try {
                     values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_CONTENT, ds.toStringUTF8());
                 }
                 catch (final EncodingSystemException e) {
                     throw new WebserverSystemException(e);
                 }
             }
         }
         else if ("E".equals(ds.getControlGroup())) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_STORAGE, Constants.STORAGE_EXTERNAL_MANAGED);
             location =
                 getContentModel().getHref() + Constants.CONTENT_STREAM_URL_PART + '/' + ds.getName()
                     + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;
 
         }
         else if ("R".equals(ds.getControlGroup())) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_STORAGE, Constants.STORAGE_EXTERNAL_URL);
         }
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_MIME_TYPE, ds.getMimeType());
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_HREF, location);
 
         return ContentModelXmlProvider.getInstance().getContentStreamXml(values);
     }
 
     /*
      * Values for rendering
      */
 
     // TODO ContentHandlerRetrieve ?
 
     /**
      * Get Common values from ContentModel.
      *
      * @param contentModel The ContentModel.
      * @return Map with common ContentModel values.
      * @throws WebserverSystemException Thrown if values extracting failed.
      */
     private Map<String, String> getCommonValues(final ContentModel contentModel) throws WebserverSystemException {
 
         final Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProviderConstants.OBJID, getContentModel().getId());
         values.put(XmlTemplateProviderConstants.TITLE, getContentModel().getTitle());
 
         values.put(XmlTemplateProviderConstants.HREF, getContentModel().getHref());
 
         try {
             values.put(XmlTemplateProviderConstants.VAR_LAST_MODIFICATION_DATE, contentModel
                 .getLastModificationDate().toString());
         }
         catch (final FedoraSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_NAMESPACE_PREFIX,
             de.escidoc.core.common.business.Constants.CONTENT_MODEL_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_NAMESPACE,
             de.escidoc.core.common.business.Constants.CONTENT_MODEL_NAMESPACE_URI);
         values.put(XmlTemplateProviderConstants.VAR_ESCIDOC_BASE_URL, XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProviderConstants.VAR_XLINK_NAMESPACE_PREFIX,
             de.escidoc.core.common.business.Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.VAR_XLINK_NAMESPACE,
             de.escidoc.core.common.business.Constants.XLINK_NS_URI);
 
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS,
             de.escidoc.core.common.business.Constants.PROPERTIES_NS_URI);
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS_PREFIX,
             de.escidoc.core.common.business.Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS_PREFIX,
             de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS,
             de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_URI);
         values.put("versionNamespacePrefix", de.escidoc.core.common.business.Constants.VERSION_NS_PREFIX);
         values.put("versionNamespace", de.escidoc.core.common.business.Constants.VERSION_NS_URI);
         values.put("releaseNamespacePrefix", de.escidoc.core.common.business.Constants.RELEASE_NS_PREFIX);
         values.put("releaseNamespace", de.escidoc.core.common.business.Constants.RELEASE_NS_URI);
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_NS_PREFIX,
             de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_STREAM_NS,
             de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_URI);
 
         return values;
     }
 
     // TODO ContentModelHandlerRetrieve ?
 
     /**
      * Prepare properties values from content model resource as velocity values.
      *
      * @param contentModel The Content Model.
      * @return Map with properties values (for velocity template)
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException
      */
     private static Map<String, String> getPropertiesValues(final ContentModel contentModel)
         throws TripleStoreSystemException, WebserverSystemException, IntegritySystemException,
         ContentModelNotFoundException {
 
         // retrieve properties from resource (the resource decided where are the
         // data to load, TripleStore or Wov)
 
         final Map<String, String> properties = contentModel.getResourceProperties();
 
         final Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_TITLE, "Properties");
         values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_HREF, contentModel.getHref()
             + Constants.PROPERTIES_URL_PART);
 
         // FIXME description not in map? (FRS)
         final String debug = contentModel.getDescription();
         // properties.get(PropertyMapKeys.LATEST_VERSION_DESCRIPTION);
         values.put(XmlTemplateProviderConstants.VAR_DESCRIPTION, debug);
 
         try {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CREATION_DATE, contentModel.getCreationDate());
         }
         catch (final TripleStoreSystemException e) {
             throw new ContentModelNotFoundException(e);
         }
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CREATED_BY_TITLE, properties
             .get(PropertyMapKeys.CREATED_BY_TITLE));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CREATED_BY_HREF,
             de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                 + properties.get(PropertyMapKeys.CREATED_BY_ID));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CREATED_BY_ID, properties
             .get(PropertyMapKeys.CREATED_BY_ID));
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_STATUS, contentModel.getStatus());
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_STATUS_COMMENT, properties
             .get(PropertyMapKeys.PUBLIC_STATUS_COMMENT));
 
         if (contentModel.hasObjectPid()) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_OBJECT_PID, contentModel.getObjectPid());
         }
 
         if (contentModel.isLocked()) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_STATUS,
                 de.escidoc.core.common.business.Constants.STATUS_LOCKED);
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_DATE, contentModel.getLockDate());
             final String lockOwnerId = contentModel.getLockOwner();
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_OWNER_ID, lockOwnerId);
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_OWNER_HREF,
                 de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE + lockOwnerId);
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_OWNER_TITLE, contentModel
                 .getLockOwnerTitle());
         }
         else {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LOCK_STATUS,
                 de.escidoc.core.common.business.Constants.STATUS_UNLOCKED);
         }
 
         // version
         final StringBuilder versionIdBase = new StringBuilder(contentModel.getId()).append(':');
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_HREF, contentModel.getVersionHref());
         // de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
         // + currentVersionId);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_ID, contentModel.getFullId());
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_TITLE, "This Version");
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_NUMBER, contentModel.getVersionId());
         // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_NUMBER));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_DATE, contentModel
             .getVersionDate().toString());
         // properties.get(TripleStoreUtility.PROP_VERSION_DATE));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_STATUS, contentModel
             .getVersionStatus());
         // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_STATUS));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_VALID_STATUS, properties
             .get(PropertyMapKeys.CURRENT_VERSION_VALID_STATUS));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_COMMENT, properties
             .get(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT));
 
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_ID, properties
             .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_TITLE, properties
             .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
 
         // href is rest only value
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_CURRENT_VERSION_MODIFIED_BY_HREF,
             de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                 + properties.get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
 
         // PID ---------------------------------------------------
         if (contentModel.hasVersionPid()) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_VERSION_PID, contentModel.getVersionPid());
         }
 
         final String latestVersionId = contentModel.getLatestVersionId();
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_VERSION_HREF,
             de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE + latestVersionId);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_VERSION_TITLE, "Latest Version");
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_VERSION_ID, latestVersionId);
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_VERSION_NUMBER, properties
             .get(PropertyMapKeys.LATEST_VERSION_NUMBER));
         values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_VERSION_DATE, properties
             .get(PropertyMapKeys.LATEST_VERSION_DATE));
 
         // if contentModel is released
         // -------------------------------------------------
         if (properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER) != null) {
 
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_NUMBER, properties
                 .get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
 
             // ! changes versionIdBase
             final String latestRevisonId =
                 versionIdBase.append(properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER)).toString();
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_HREF,
                 de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE + latestRevisonId);
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_TITLE, "Latest public version");
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_ID, latestRevisonId);
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_DATE, properties
                 .get(PropertyMapKeys.LATEST_RELEASE_VERSION_DATE));
 
             final String latestReleasePid = contentModel.getLatestReleasePid();
             if (latestReleasePid != null) {
                 values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_LATEST_RELEASE_PID, latestReleasePid);
             }
         }
 
         return values;
     }
 
     // TODO ContentModelHandlerRetrieve ?
     private Map<String, Object> getMdRecordDefinitionsValues() throws IntegritySystemException,
         WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         final Collection<Map<String, String>> mdRecordDefinitions = new ArrayList<Map<String, String>>();
 
         // get dsTypeModel/@ID entries from datastream DS-COMPOSITE-MODEL
         final List<DsTypeModel> datastreamEntries = getContentModel().getMdRecordDefinitionIDs();
 
         if (datastreamEntries != null) {
             for (final DsTypeModel datastreamEntry : datastreamEntries) {
                 final Map<String, String> mdRecordDefinition = new HashMap<String, String>();
                 mdRecordDefinition.put("name", datastreamEntry.getName());
                 if (datastreamEntry.hasSchema()) {
                     mdRecordDefinition.put("schemaHref",
                         de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE + getContentModel().getId()
                             + "/md-record-definitions/md-record-definition/" + datastreamEntry.getName()
                             + "/schema/content");
                 }
                 mdRecordDefinitions.add(mdRecordDefinition);
             }
         }
 
         if (!mdRecordDefinitions.isEmpty()) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_MDRECORD_DEFINITIONS, mdRecordDefinitions);
         }
 
         return values;
     }
 
     // TODO ContentModelHandlerRetrieve ?
     private Map<String, Object> getResourceDefinitionsValues() throws TripleStoreSystemException {
 
         // for every xslt service definition bound to this content model
         // get the operation name and create a URL for the datastream "xslt"
         // (while create or update the URL of the XSLT may point to extern, then
         // the XSLT is retrieved and stored in the service definition object)
 
         final Map<String, Object> values = new HashMap<String, Object>();
         final Collection<Map<String, String>> resourceDefinitions = new ArrayList<Map<String, String>>();
 
         // FIXME do not use triplestore
 
         final Collection<String> methodNames = new ArrayList<String>();
         // <info:fedora/fedora-system:def/model#hasService>
         final List<String> sdefs =
             getTripleStoreUtility().getPropertiesElementsVector(getContentModel().getId(),
                 "info:fedora/fedora-system:def/model#hasService");
         // <info:fedora/fedora-system:def/model#definesMethod>
         // and
         // TODO <http://escidoc.de/core/01/tmp/transforms>
         for (final String sdef : sdefs) {
             methodNames.add(getTripleStoreUtility().getProperty(sdef,
                 "info:fedora/fedora-system:def/model#definesMethod"));
         }
 
         if (!methodNames.isEmpty()) {
             for (final String methodName : methodNames) {
 
                 for (final String sdef : sdefs) {
                     final Map<String, String> resourceDefinition = new HashMap<String, String>();
                     resourceDefinition.put("name", methodName);
                     resourceDefinition.put("xsltHref", getContentModel().getHref()
                         + "/resource-definitions/resource-definition/" + methodName + "/xslt/content");
 
                     // md-record-name
                     final String mdRecordName =
                         getTripleStoreUtility().getPropertiesElements(sdef, "http://escidoc.de/core/01/tmp/transforms");
 
                     // FIXME get from service deployment
                     // <http://escidoc.de/core/01/tmp/transforms>
                     // it's just a name or
                     resourceDefinition.put("mdRecordName", mdRecordName);
                     // it's a URL
                     // resourceDefinition.put("xmlHref", "http://xml.to/transform");
 
                     resourceDefinitions.add(resourceDefinition);
                 }
             }
         }
 
         if (!resourceDefinitions.isEmpty()) {
             values.put(XmlTemplateProviderConstants.VAR_CONTENT_MODEL_RESOURCE_DEFINITIONS, resourceDefinitions);
         }
 
         return values;
     }
 
     // TODO ContentModelHandlerRetrieve ?
     private static Map<String, String> getResourcesValues(final FedoraResource contentModel) {
         final Map<String, String> values = new HashMap<String, String>();
         values.put(XmlTemplateProviderConstants.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", contentModel.getHref() + "/resources");
         return values;
     }
 }
