 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the
  * Common Development and Distribution License, Version 1.0 only
  * (the "License").  You may not use this file except in compliance
  * with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE
  * or http://www.escidoc.de/license.
  * See the License for the specific language governing permissions
  * and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each
  * file and include the License file at license/ESCIDOC.LICENSE.
  * If applicable, add the following below this CDDL HEADER, with the
  * fields enclosed by brackets "[]" replaced with your own identifying
  * information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  */
 
 /*
  * Copyright 2006-2008 Fachinformationszentrum Karlsruhe Gesellschaft
  * fuer wissenschaftlich-technische Information mbH and Max-Planck-
  * Gesellschaft zur Foerderung der Wissenschaft e.V.  
  * All rights reserved.  Use is subject to license terms.
  */
 package de.escidoc.core.om.business.renderer;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Container;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.ContainerXmlProvider;
 import de.escidoc.core.common.util.xml.factory.MetadataRecordsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.RelationsXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProviderConstants;
 import de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface;
 import de.escidoc.core.om.business.security.UserFilter;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.ISODateTimeFormat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Render XML representations of a Container.
  */
 @Service
 public class VelocityXmlContainerRenderer implements ContainerRendererInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(VelocityXmlContainerRenderer.class);
 
     private static final int THREE = 3;
 
     @Autowired
     private VelocityXmlCommonRenderer commonRenderer;
 
     @Autowired
     @Qualifier("business.TripleStoreUtility")
     private TripleStoreUtility tripleStoreUtility;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected VelocityXmlContainerRenderer() {
     }
 
     /**
      * See Interface for functional description.
      *
      * @param container Container
      * @return XML Container representation.
      * @throws SystemException If an error occurs.
      */
     @Override
     public String render(final Container container) throws SystemException {
 
         // Container container = containerHandler.getContainer();
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("containerTitle", container.getTitle());
         values.put("containerHref", container.getHref());
         values.put("containerId", container.getId());
 
         addPropertiesValus(values, container);
         // addOrganizationDetailsValues(organizationalUnit, values);
         addResourcesValues(container, values);
 
         addStructMapValus(container, values);
         addMdRecordsValues(container, values);
         final List<Map<String, String>> relations = container.getRelations();
         commonRenderer.addRelationsValues(relations, container.getHref(), values);
         VelocityXmlCommonRenderer.addRelationsNamespaceValues(values);
         values.put("contentRelationsTitle", "Relations of Container");
 
         return ContainerXmlProvider.getInstance().getContainerXml(values);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface
      * #
      * renderProperties(de.escidoc.core.common.business.fedora.resources.Container
      * )
      */
     @Override
     public String renderProperties(final Container container) throws WebserverSystemException,
         TripleStoreSystemException, EncodingSystemException, IntegritySystemException, FedoraSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootProperties", XmlTemplateProviderConstants.TRUE);
         addPropertiesValus(values, container);
         return ContainerXmlProvider.getInstance().getPropertiesXml(values);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ContainerRendererInterface
      * #
      * renderResources(de.escidoc.core.common.business.fedora.resources.Container
      * )
      */
     @Override
     public String renderResources(final Container container) throws WebserverSystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootResources", XmlTemplateProviderConstants.TRUE);
         addResourcesValues(container, values);
         return ContainerXmlProvider.getInstance().getResourcesXml(values);
     }
 
     /**
      * Gets the representation of the sub resource {@code relations} of an item/container.
      *
      * @param container The Container.
      * @return Returns the XML representation of the sub resource {@code ou-parents} of an organizational unit.
      */
     @Override
     public String renderRelations(final Container container) throws WebserverSystemException,
         TripleStoreSystemException, IntegritySystemException, FedoraSystemException, XmlParserSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         values.put("isRootRelations", XmlTemplateProviderConstants.TRUE);
 
         commonRenderer.addRelationsValues(container.getRelations(), container.getHref(), values);
         values.put("contentRelationsTitle", "Relations of Container");
         VelocityXmlCommonRenderer.addRelationsNamespaceValues(values);
         return RelationsXmlProvider.getInstance().getRelationsXml(values);
     }
 
     /**
      * Gets the representation of the virtual resource {@code parents} of an item/container.
      *
      * @param containerId The Container.
      * @return Returns the XML representation of the virtual resource {@code parents} of an container.
      */
     @Override
     public String renderParents(final String containerId) throws WebserverSystemException, TripleStoreSystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         VelocityXmlCommonRenderer.addXlinkValues(values);
         VelocityXmlCommonRenderer.addStructuralRelationsValues(values);
         values.put(XmlTemplateProviderConstants.VAR_LAST_MODIFICATION_DATE, ISODateTimeFormat.dateTime().withZone(
             DateTimeZone.UTC).print(System.currentTimeMillis()));
         values.put("isRootParents", XmlTemplateProviderConstants.TRUE);
         addParentsValues(containerId, values);
         VelocityXmlCommonRenderer.addParentsNamespaceValues(values);
         return ContainerXmlProvider.getInstance().getParentsXml(values);
     }
 
     /**
      * Adds the parents values to the provided map.
      *
      * @param containerId The container for that data shall be created.
      * @param values      The map to add values to.
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void addParentsValues(final String containerId, final Map<String, Object> values)
         throws TripleStoreSystemException {
         values.put("parentsHref", XmlUtility.getContainerParentsHref(XmlUtility.getContainerHref(containerId)));
         values.put("parentsTitle", "parents of container " + containerId);
         final StringBuffer query =
             this.tripleStoreUtility.getRetrieveSelectClause(true, TripleStoreUtility.PROP_MEMBER);
 
         if (query.length() > 0) {
             query.append(this.tripleStoreUtility.getRetrieveWhereClause(true, TripleStoreUtility.PROP_MEMBER,
                 XmlUtility.getObjidWithoutVersion(containerId), null, null, null));
             List<String> ids = new ArrayList<String>();
             try {
                 ids = this.tripleStoreUtility.retrieve(query.toString());
             }
             catch (final TripleStoreSystemException e) {
                 if (LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Error on quering triple store.");
                 }
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Error on quering triple store.", e);
                 }
             }
 
             final Iterator<String> idIter = ids.iterator();
             final Collection<Map<String, String>> entries = new ArrayList<Map<String, String>>(ids.size());
             while (idIter.hasNext()) {
                 final Map<String, String> entry = new HashMap<String, String>(THREE);
                 final String id = idIter.next();
                 entry.put("id", id);
                 entry.put("href", XmlUtility.getContainerHref(id));
                 entry.put("title", this.tripleStoreUtility.getTitle(id));
 
                 entries.add(entry);
             }
             if (!entries.isEmpty()) {
                 values.put(XmlTemplateProviderConstants.VAR_PARENTS, entries);
             }
         }
     }
 
     /**
      * Adds values for namespace declaration.
      *
      * @param values Already added values.
      * @throws WebserverSystemException If an error occurs.
      */
     private static void addNamespaceValues(final Map<String, Object> values) {
 
         values.put("containerNamespacePrefix", Constants.CONTAINER_NAMESPACE_PREFIX);
         values.put("containerNamespace", Constants.CONTAINER_NAMESPACE_URI);
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS_PREFIX, Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_PROPERTIES_NS, Constants.PROPERTIES_NS_URI);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS_PREFIX, Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values.put(XmlTemplateProviderConstants.ESCIDOC_SREL_NS, Constants.STRUCTURAL_RELATIONS_NS_URI);
         values.put("versionNamespacePrefix", Constants.VERSION_NS_PREFIX);
         values.put("versionNamespace", Constants.VERSION_NS_URI);
         values.put("releaseNamespacePrefix", Constants.RELEASE_NS_PREFIX);
         values.put("releaseNamespace", Constants.RELEASE_NS_URI);
         values.put("structmapNamespacePrefix", Constants.STRUCT_MAP_PREFIX);
         values.put("structmapNamespace", Constants.STRUCT_MAP_NAMESPACE_URI);
 
     }
 
     /**
      * Adds the properties values to the provided map.
      *
      * @param values    The map to add values to.
      * @param container The Container.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     private static void addPropertiesValus(final Map<String, Object> values, final Container container)
         throws TripleStoreSystemException, EncodingSystemException, IntegritySystemException, FedoraSystemException,
         WebserverSystemException {
 
         final String id = container.getId();
         values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_TITLE, "Properties");
         values.put(XmlTemplateProviderConstants.VAR_PROPERTIES_HREF, XmlUtility.getContainerPropertiesHref(container
             .getHref()));
         // status
         values.put("containerStatus", container.getStatus());
         values.put("containerCreationDate", container.getCreationDate());
         values.put(XmlTemplateProviderConstants.VAR_CONTAINER_STATUS_COMMENT, XmlUtility
             .escapeForbiddenXmlCharacters(container.getProperty(PropertyMapKeys.PUBLIC_STATUS_COMMENT)));
         // name
         values.put("containerName", container.getTitle());
         // description
         final String description = container.getDescription();
         if (description != null) {
             values.put("containerDescription", PropertyMapKeys.CURRENT_VERSION_DESCRIPTION);
         }
 
         // context
         values.put("containerContextId", container.getProperty(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
         values.put("containerContextHref", Constants.CONTEXT_URL_BASE
             + container.getProperty(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
         values.put("containerContextTitle", container.getProperty(PropertyMapKeys.CURRENT_VERSION_CONTEXT_TITLE));
         // content model
         final String contentModelId = container.getProperty(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID);
         values.put("containerContentModelId", contentModelId);
         values.put("containerContentModelHref", XmlUtility.getContentModelHref(contentModelId));
         values.put("containerContentModelTitle", container
             .getProperty(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_TITLE));
 
         // created-by -----------
         final String createdById = container.getProperty(PropertyMapKeys.CREATED_BY_ID);
         values.put("containerCreatedById", createdById);
         values.put("containerCreatedByHref", XmlUtility.getUserAccountHref(createdById));
         values.put("containerCreatedByTitle", container.getProperty(PropertyMapKeys.CREATED_BY_TITLE));
 
         // lock -status, -owner, -date
         if (container.isLocked()) {
             values.put("containerLocked", XmlTemplateProviderConstants.TRUE);
             final String lockOwnerId = container.getLockOwner();
             values.put("containerLockStatus", "locked");
             values.put("containerLockDate", container.getLockDate());
             values.put("containerLockOwnerHref", XmlUtility.getUserAccountHref(lockOwnerId));
             values.put("containerLockOwnerId", lockOwnerId);
             values.put("containerLockOwnerTitle", container.getLockOwnerTitle());
         }
         else {
             values.put("containerLocked", XmlTemplateProviderConstants.FALSE);
             values.put("containerLockStatus", "unlocked");
         }
 
         final String currentVersionId = container.getFullId();
         final String latestVersionNumber = container.getProperty(PropertyMapKeys.LATEST_VERSION_NUMBER);
         String curVersionNumber = container.getVersionId();
         if (curVersionNumber == null) {
             curVersionNumber = latestVersionNumber;
         }
 
         // pid ---------------
         final String pid = container.getObjectPid();
         if (pid != null && pid.length() > 0) {
             values.put("containerPid", pid);
         }
         // current version
         values.put("containerCurrentVersionHref", container.getVersionHref());
         values.put("containerCurrentVersionTitle", "current version");
         values.put("containerCurrentVersionId", currentVersionId);
         values.put("containerCurrentVersionNumber", curVersionNumber);
         values.put("containerCurrentVersionComment", XmlUtility.escapeForbiddenXmlCharacters(container
             .getProperty(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT)));
 
         // modified by
 
         final String modifiedById = container.getProperty(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID);
         values.put("containerCurrentVersionModifiedByTitle", container
             .getProperty(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
         values.put("containerCurrentVersionModifiedByHref", XmlUtility.getUserAccountHref(modifiedById));
         values.put("containerCurrentVersionModifiedById", modifiedById);
 
         final String versionPid = container.getVersionPid();
         // container
         if (versionPid != null && versionPid.length() != 0) {
             values.put("containerCurrentVersionPID", versionPid);
         }
         values.put("containerCurrentVersionDate", container.getVersionDate());
 
         if (curVersionNumber.equals(latestVersionNumber)) {
             final String latestVersionStatus = container.getProperty(PropertyMapKeys.LATEST_VERSION_VERSION_STATUS);
             values.put("containerCurrentVersionStatus", latestVersionStatus);
 
             if (latestVersionStatus.equals(Constants.STATUS_RELEASED)) {
                 final String latestReleasePid = container.getProperty(PropertyMapKeys.LATEST_RELEASE_PID);
                 if (latestReleasePid != null && latestReleasePid.length() != 0) {
                     values.put("containerCurrentVersionPID", latestReleasePid);
                 }
             }
 
         }
         else {
             values.put("containerCurrentVersionStatus", container.getProperty(PropertyMapKeys.CURRENT_VERSION_STATUS));
 
             values.put("containerCurrentVersionComment", XmlUtility.escapeForbiddenXmlCharacters(container
                 .getProperty(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT)));
             values.put("containerCurrentVersionModifiedById", container
                 .getProperty(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
 
             values.put("containerCurrentVersionModifiedByHref", container
                 .getProperty(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_HREF));
 
             values.put("containerCurrentVersionModifiedByTitle", container
                 .getProperty(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
         }
 
         // latest version
         values.put("containerLatestVersionHref", container.getLatestVersionHref());
         values.put("containerLatestVersionId", container.getLatestVersionId());
         values.put("containerLatestVersionTitle", "latest version");
         values.put("containerLatestVersionDate", container.getProperty(PropertyMapKeys.LATEST_VERSION_DATE));
         values.put("containerLatestVersionNumber", latestVersionNumber);
         // latest release
         final String containerStatus = container.getStatus();
         if (containerStatus.equals(Constants.STATUS_RELEASED) || containerStatus.equals(Constants.STATUS_WITHDRAWN)) {
             values.put("containerLatestReleaseHref", container.getHrefWithoutVersionNumber() + ':'
                 + container.getProperty(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
 
             values.put("containerLatestReleaseId", id + ':'
                 + container.getProperty(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
             values.put("containerLatestReleaseTitle", "latest release");
             values.put("containerLatestReleaseNumber", container
                 .getProperty(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
             values
                 .put("containerLatestReleaseDate", container.getProperty(PropertyMapKeys.LATEST_RELEASE_VERSION_DATE));
             final String latestReleasePid = container.getProperty(PropertyMapKeys.LATEST_RELEASE_PID);
             if (latestReleasePid != null && latestReleasePid.length() != 0) {
                 values.put("containerLatestReleasePid", latestReleasePid);
             }
         }
         // content model specific
         try {
             final Datastream cmsDs = container.getCts();
             final String xml = cmsDs.toStringUTF8();
             values.put(XmlTemplateProviderConstants.CONTAINER_CONTENT_MODEL_SPECIFIC, xml);
         }
         catch (final StreamNotFoundException e) {
             // This element is now optional.
             if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Error on getting container content modell specific.", e);
             }
         }
     }
 
     /**
      * Adds the struct-map values to the provided map.
      *
      * @param container The Container.
      * @param values    The map to add values to.
      * @throws SystemException Thrown in case of an internal error.
      */
     private void addStructMapValus(final Container container, final Map<String, Object> values) throws SystemException {
 
         values.put("structMapTitle", "StructMap of Container");
         values.put("structMapHref", container.getHref() + "/struct-map");
 
         try {
             addMemberRefs(container, values);
         }
         catch (final MissingMethodParameterException e) {
             throw new WebserverSystemException(e);
         }
     }
 
     /**
      *
      * @param container
      * @param values
      * @throws SystemException
      * @throws MissingMethodParameterException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void addMemberRefs(final Container container, final Map<String, Object> values) throws SystemException,
         MissingMethodParameterException {
 
         final UserFilter ufilter = new UserFilter();
 
         final List<String> ids = ufilter.getMemberRefList(container);
         final Iterator<String> idIter = ids.iterator();
         final Collection<Map<String, String>> items = new ArrayList<Map<String, String>>();
         final Collection<Map<String, String>> containers = new ArrayList<Map<String, String>>();
 
         while (idIter.hasNext()) {
             final Map<String, String> entry = new HashMap<String, String>(3);
             final String id = idIter.next();
             final String objectType = this.tripleStoreUtility.getObjectType(id);
             if (Constants.ITEM_OBJECT_TYPE.equals(objectType) || Constants.CONTAINER_OBJECT_TYPE.equals(objectType)) {
                 entry.put("memberId", id);
                 entry.put("memberTitle", this.tripleStoreUtility.getTitle(id));
                 if (objectType.equals(Constants.ITEM_OBJECT_TYPE)) {
 
                     items.add(entry);
                     entry.put("memberHref", XmlUtility.BASE_OM + "item/" + id);
                     entry.put("elementName", "item-ref");
                 }
                 else {
 
                     containers.add(entry);
                     entry.put("memberHref", XmlUtility.BASE_OM + "container/" + id);
                     entry.put("elementName", "container-ref");
                 }
 
             }
             else {
                 final String msg =
                     "FedoraContainerHandler.getMemberRefs: can not " + "write member entry to struct-map for "
                         + "object with unknown type: " + id + '.';
                 LOGGER.error(msg);
             }
 
         }
         if (!items.isEmpty()) {
             values.put("items", items);
         }
         if (!containers.isEmpty()) {
             values.put("containers", containers);
         }
     }
 
     /**
      * Adds the resource values to the provided map.
      *
      * @param container The Container for that data shall be created.
      * @param values    The map to add values to.
      * @throws WebserverSystemException If an error occurs.
      */
     private void addResourcesValues(final FedoraResource container, final Map<String, Object> values)
         throws WebserverSystemException {
 
         values.put(XmlTemplateProviderConstants.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", XmlUtility.getContainerResourcesHref(container.getHref()));
         values.put("membersHref", container.getHref() + "/resources/members");
         values.put("membersTitle", "Members ");
         values.put("versionHistoryTitle", "Version History");
         values.put("versionHistoryHref", XmlUtility.getContainerResourcesHref(container.getHref()) + '/'
             + Elements.ELEMENT_RESOURCES_VERSION_HISTORY);
 
         // add operations from Fedora service definitions
         // FIXME use container properties instead of triplestore util
         try {
             values.put("resourceOperationNames", this.tripleStoreUtility.getMethodNames(container.getId()));
         }
         catch (final TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
     }
 
     /**
      * Adds values for metadata XML of the container.
      *
      * @param container The container object.
      * @param values    Already added values.
      * @throws EncodingSystemException  If an encoding error occurs.
      * @throws FedoraSystemException    If Fedora throws an exception.
      * @throws WebserverSystemException If an error occurs.
      * @throws IntegritySystemException If the repository integrity is violated.
      */
     private void addMdRecordsValues(final Container container, final Map<String, Object> values)
         throws EncodingSystemException, FedoraSystemException, WebserverSystemException, IntegritySystemException {
 
         values.put(XmlTemplateProviderConstants.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.MD_RECORDS_NAMESPACE, Constants.METADATARECORDS_NAMESPACE_URI);
         values.put("mdRecordsHref", XmlUtility.getContainerMdRecordsHref(container.getHref()));
         values.put("mdRecordsTitle", "Metadata Records of Container " + container.getId());
 
         final HashMap<String, Datastream> mdRecords = (HashMap<String, Datastream>) container.getMdRecords();
         final Collection<Datastream> mdRecordsDatastreams = mdRecords.values();
         final Iterator<Datastream> it = mdRecordsDatastreams.iterator();
         final StringBuilder content = new StringBuilder();
         while (it.hasNext()) {
             final Datastream mdRecord = it.next();
             final String md = renderMetadataRecord(container, mdRecord, false);
             content.append(md);
         }
         values.put(XmlTemplateProviderConstants.VAR_MD_RECORDS_CONTENT, content.toString());
 
     }
 
     /**
      * Renders a single meta data record to XML representation.
      *
      * @param container      The Container.
      * @param mdRecord       The to render md record.
      * @param isRootMdRecord Set true if md-record is to render with root elements.
      * @return Returns the XML representation of the metadata records.
      * @throws EncodingSystemException  If an encoding error occurs.
      * @throws FedoraSystemException    If Fedora throws an exception.
      * @throws WebserverSystemException If an error occurs.
      */
     @Override
     public String renderMetadataRecord(
         final Container container, final Datastream mdRecord, final boolean isRootMdRecord)
         throws EncodingSystemException, FedoraSystemException, WebserverSystemException {
 
         if (mdRecord.isDeleted()) {
             return "";
         }
 
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         values.put("mdRecordHref", XmlUtility.getContainerMdRecordsHref(container.getHref()) + "/md-record/"
             + mdRecord.getName());
         values.put(XmlTemplateProviderConstants.MD_RECORD_NAME, mdRecord.getName());
         values.put("mdRecordTitle", mdRecord.getName());
         values.put(XmlTemplateProviderConstants.IS_ROOT_MD_RECORD, isRootMdRecord);
         values.put(XmlTemplateProviderConstants.MD_RECRORDS_NAMESPACE_PREFIX,
             Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values.put(XmlTemplateProviderConstants.MD_RECORDS_NAMESPACE, Constants.METADATARECORDS_NAMESPACE_URI);
         final String mdRecordContent = mdRecord.toStringUTF8();
         values.put(XmlTemplateProviderConstants.MD_RECORD_CONTENT, mdRecordContent);
         final List<String> altIds = mdRecord.getAlternateIDs();
         if (!Constants.UNKNOWN.equals(altIds.get(1))) {
             values.put(XmlTemplateProviderConstants.MD_RECORD_TYPE, altIds.get(1));
         }
         if (!Constants.UNKNOWN.equals(altIds.get(2))) {
             values.put(XmlTemplateProviderConstants.MD_RECORD_SCHEMA, altIds.get(2));
         }
 
         return MetadataRecordsXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /**
      * @param container The Container.
      * @return Returns the XML representation of the metadata records.
      * @throws EncodingSystemException  If an encoding error occurs.
      * @throws FedoraSystemException    If Fedora throws an exception.
      * @throws WebserverSystemException If an error occurs.
      * @throws IntegritySystemException If the repository integrity is violated.
      */
     @Override
     public String renderMetadataRecords(final Container container) throws EncodingSystemException,
         FedoraSystemException, WebserverSystemException, IntegritySystemException {
 
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         values.put(XmlTemplateProviderConstants.IS_ROOT_SUB_RESOURCE, XmlTemplateProviderConstants.TRUE);
         addMdRecordsValues(container, values);
 
         return MetadataRecordsXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     /**
      * Gets the representation of the virtual sub resource {@code struct-map} of an organizational unit.
      *
      * @param container The Container.
      * @return Returns the XML representation of the virtual sub resource {@code children} of an organizational
      *         unit.
      * @throws SystemException Thrown in case of an internal error.
      */
     @Override
     public String renderStructMap(final Container container) throws SystemException {
         final Map<String, Object> values = new HashMap<String, Object>();
         commonRenderer.addCommonValues(container, values);
         addNamespaceValues(values);
         values.put("isRootStructMap", XmlTemplateProviderConstants.TRUE);
         values.put("isSrelNeeded", XmlTemplateProviderConstants.TRUE);
         addStructMapValus(container, values);
 
         return ContainerXmlProvider.getInstance().getStructMapXml(values);
 
     }
 }
