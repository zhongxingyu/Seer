 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 /**
  *
  */
 package de.escidoc.core.common.business.fedora.resources;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.interfaces.FedoraResource;
 import de.escidoc.core.common.business.fedora.resources.interfaces.ItemInterface;
 import de.escidoc.core.common.business.fedora.resources.item.Component;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.AddNewSubTreesToDatastream;
 import de.escidoc.core.common.util.stax.handler.DcReadHandler;
 import de.escidoc.core.common.util.stax.handler.RelsExtRefListExtractor;
 import de.escidoc.core.common.util.stax.handler.item.RemoveObjectRelationHandlerNew;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElementWithChildElements;
 import org.fcrepo.server.types.gen.DatastreamControlGroup;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Configurable;
 
 import javax.annotation.PostConstruct;
 import javax.xml.stream.XMLStreamException;
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * Implementation of a Fedora Item Object which consist of datastreams managed in Fedora Digital Repository System.
  * 
  * @author Frank Schwichtenberg
  */
 @Configurable(preConstruction = true)
 public class Item extends GenericVersionableResourcePid implements ItemInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Item.class);
 
     private Datastream cts;
 
     private final Map<String, Datastream> mdRecords = new HashMap<String, Datastream>();
 
     private final Map<String, Datastream> contentStreams = new HashMap<String, Datastream>();
 
    private Map<String, Component> components = new HashMap<String, Component>();
 
     private Map<String, Component> componentsByLocalName = new HashMap<String, Component>();
 
     private Collection<String> alteredComponent = new ArrayList<String>();
 
     // properties initiation
     private boolean resourceInit;
 
     /**
      * Constructs the Item with the specified id. The datastreams are instantiated and retrieved if the related getter
      * is called.
      * 
      * @param id
      *            The id of an item managed in Fedora.
      * @throws StreamNotFoundException
      *             Thrown if data streams of Item object was not found.
      * @throws IntegritySystemException
      *             Thrown if there is an integrity error with the addressed object.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      */
     public Item(final String id) throws StreamNotFoundException, TripleStoreSystemException, WebserverSystemException,
         XmlParserSystemException, IntegritySystemException, ResourceNotFoundException, FedoraSystemException,
         ComponentNotFoundException, ItemNotFoundException {

         super(id);
         setPropertiesNames(expandPropertiesNames(getPropertiesNames()),
             expandPropertiesNamesMapping(getPropertiesNamesMapping()));

         setHref(Constants.ITEM_URL_BASE + getId());
     }
 
     private void init() throws WebserverSystemException, FedoraSystemException, IntegritySystemException,
         StreamNotFoundException, TripleStoreSystemException, ItemNotFoundException, ComponentNotFoundException {
         initDatastreams(getDatastreamInfos());
         if (!checkResourceType(ResourceType.ITEM)) {
             throw new ItemNotFoundException("Item with the provided objid '" + this.getId() + "' does not exit.");
         }
         setTitle(getProperty("title"));
         try {
             initComponents();
         }
         catch (final SystemException e) {
             throw new WebserverSystemException(e);
         }
     }
 
     /**
      * Add a Component to the Item.
      * 
      * @param c
      *            The new Component. (The Component has not to be (but could) persist, this is done with with the Item.)
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     public void addComponent(final Component c) throws FedoraSystemException, WebserverSystemException,
         IntegritySystemException, EncodingSystemException {
 
         addComponentToRelsExt(c.getId());
         this.components.put(c.getId(), c);
         this.alteredComponent.add(c.getId());
     }
 
     /**
      * Add a Component by id to the Item.
      * 
      * @param componentId
      *            This has to be a persistent Component. This Component has already to exists within the repository!
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     public void addComponent(final String componentId) throws FedoraSystemException, WebserverSystemException,
         IntegritySystemException, EncodingSystemException {
 
         addComponentToRelsExt(componentId);
         this.alteredComponent.add(componentId);
     }
 
     /**
      * Delete a Component from the Item.
      * 
      * @param componentId
      *            The id of the Component which is to delete from the Item.
      * @throws de.escidoc.core.common.exceptions.application.violated.LockingException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException
      * @throws de.escidoc.core.common.exceptions.system.SystemException
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     public void deleteComponent(final String componentId) throws LockingException, ComponentNotFoundException,
         InvalidStatusException, SystemException, EncodingSystemException, IntegritySystemException,
         FedoraSystemException, TripleStoreSystemException, XmlParserSystemException, WebserverSystemException {
 
         deleteComponent(getComponent(componentId));
     }
 
     /**
      * Delete a Component from the Item.
      * 
      * @param c
      *            The Component which is to delete from the Item.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.violated.LockingException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      * @throws de.escidoc.core.common.exceptions.system.EncodingSystemException
      */
     public void deleteComponent(final FedoraResource c) throws LockingException, ComponentNotFoundException,
         InvalidStatusException, WebserverSystemException, TripleStoreSystemException, FedoraSystemException,
         XmlParserSystemException, IntegritySystemException, EncodingSystemException {
 
         final String componentId = c.getId();
         removeComponent(componentId);
         this.components.remove(componentId);
         this.alteredComponent.add(componentId);
     }
 
     /**
      * Get a Component.
      * 
      * @param componentId
      *            The id of the component.
      * @return Component.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     public Component getComponent(final String componentId) throws ComponentNotFoundException,
         WebserverSystemException, FedoraSystemException, TripleStoreSystemException, IntegritySystemException,
         XmlParserSystemException {
 
         // check if the Component is part of this Item.
         if (!this.components.containsKey(componentId)) {
             throw new ComponentNotFoundException("Component with objid='" + componentId
                 + "' is not part of Item with objid='" + getId() + '\'');
         }
 
         Component c = this.components.get(componentId);
 
         // load Component
         if (c == null) {
             try {
                 c = new Component(componentId, getId(), getVersionDate());
             }
             catch (final ResourceNotFoundException e) {
                 throw new ComponentNotFoundException(e);
             }
             this.components.put(componentId, c);
         }
 
         return c;
     }
 
     /**
      * @return A map which contains unique 'content-category' entries associated with a component object. May be used
      *         for other identifications which are possibly unique in item scope.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public Map<String, Component> getComponentsByLocalName() throws ComponentNotFoundException,
         WebserverSystemException, FedoraSystemException, TripleStoreSystemException, XmlParserSystemException {
 
         initComponents();
         return this.componentsByLocalName;
     }
 
     /**
      * Get a Component by name.
      * 
      * @param componentName
      *            The name of the Component.
      * @return Component
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public Component getComponentByLocalName(final String componentName) throws ComponentNotFoundException,
         WebserverSystemException, FedoraSystemException, TripleStoreSystemException, XmlParserSystemException {
 
         initComponents();
         final Component c = this.componentsByLocalName.get(componentName);
         if (c == null) {
             throw new ComponentNotFoundException("Component with name '" + componentName + "' could not be found.");
         }
         return c;
     }
 
     // /**
     // * Re initialize the set of components for this item.
     // *
     // * @param ids
     // * The ids of the components.
     // * @throws ComponentNotFoundException
     // * If a component is not available.
     // * @throws SystemException
     // * Thrown in case of an internal system error.
     // */
     // public void setComponents(final Collection<String> ids)
     // throws ComponentNotFoundException, SystemException {
     //
     // this.componentIds = ids;
     // }
 
     /**
      * Get the IDs of Components.
      * 
      * @return Component IDs
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     public Collection<String> getComponentIds() throws XmlParserSystemException, TripleStoreSystemException,
         WebserverSystemException {
 
         final Collection<String> componentIds;
 
         if (this.components == null) {
             if (isLatestVersion()) {
                 componentIds = getTripleStoreUtility().getComponents(getId());
             }
             else {
                 final List<String> predicates = new ArrayList<String>();
                 predicates.add(Constants.STRUCTURAL_RELATIONS_NS_URI + "component");
                 final StaxParser sp = new StaxParser();
                 final RelsExtRefListExtractor rerle = new RelsExtRefListExtractor(predicates);
                 sp.addHandler(rerle);
                 try {
                     sp.parse(getRelsExt().getStream());
                 }
                 catch (final Exception e) {
                     throw new XmlParserSystemException("Unexpected exception.", e);
                 }
                 componentIds = rerle.getEntries().get(Constants.STRUCTURAL_RELATIONS_NS_URI + "component");
             }
         }
         else {
             componentIds = this.components.keySet();
         }
 
         return componentIds;
     }
 
     /**
      * Initialize the Components.
      * 
      * @throws ComponentNotFoundException
      *             Thrown if Component of Item could no t be found.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      */
     private void initComponents() throws ComponentNotFoundException, TripleStoreSystemException,
         XmlParserSystemException, WebserverSystemException {
 
         if (this.components == null) {
             final Iterator<String> idsIter = getComponentIds().iterator();
             this.componentsByLocalName = new HashMap<String, Component>();
             this.components = new HashMap<String, Component>();
 
             while (idsIter.hasNext()) {
                 final String id = idsIter.next();
                 this.components.put(id, null);
                 final String type = this.getProperty(TripleStoreUtility.PROP_CONTENT_CATEGORY);
 
                 if (type != null) {
                     this.componentsByLocalName.put(type, null);
                 }
             }
         }
     }
 
     private Map<String, String> getDublinCorePropertiesMap() throws XmlParserSystemException {
 
         // parse version-history
         final StaxParser sp = new StaxParser();
         final DcReadHandler dch = new DcReadHandler(sp);
         sp.addHandler(dch);
         try {
             sp.parse(getDc().getStream());
         }
         catch (final Exception e) {
             throw new XmlParserSystemException("Unexpected exception.", e);
         }
         return dch.getPropertiesMap();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #getRelsExt()
      */
     public Datastream getDc() throws FedoraSystemException, WebserverSystemException {
 
         if (this.dc == null) {
             final Datastream ds;
             try {
                 ds = new Datastream("DC", getId(), getVersionDate());
             }
             catch (final StreamNotFoundException e) {
                 throw new WebserverSystemException(e);
             }
             // ,
             // location, controlGroupValue);
             // ds.setAlternateIDs(new Vector<String>(altIDs));
             // ds.setLabel(label);
             this.dc = ds;
         }
 
         return this.dc;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource
      * #setRelsExt(de.escidoc.core.common.business.fedora.datastream.Datastream)
      */
     public void setDc(final Datastream ds) throws FedoraSystemException, WebserverSystemException {
         // TODO should lock only be checked in handler?
         // if (this.isLocked) {
         // throw new LockingException("Item " + getId() + " is locked.");
         // }
         // check if relsExt is set, is equal to ds and save to fedora
 
         final Datastream curDs = getDc();
 
         if (!ds.equals(curDs)) {
 
             this.dc = ds;
             ds.merge();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #getGenericProperties()
      */
     public Datastream getCts() {
         return this.cts;
     }
 
     public void setCts(final Datastream ds) throws FedoraSystemException, WebserverSystemException {
         // TODO should lock only be checked in handler?
         // if (this.isLocked) {
         // throw new LockingException("Item " + getId() + " is locked.");
         // }
         // check if cts is set, is equal to ds and save to
         // fedora
 
         final Datastream curDs = getCts();
         if (!ds.equals(curDs)) {
             try {
                 ds.merge();
                 this.cts = ds;
             }
             catch (final FedoraSystemException e) {
                 if (LOGGER.isWarnEnabled()) {
                     LOGGER.warn("Error on merging datastream.");
                 }
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Error on merging datastream.", e);
                 }
                 // this is not an update; its a create
                 ds.persist(false);
                 this.cts = ds;
             }
         }
     }
 
     /**
      * See Interface for functional description.
      */
     @Override
     public Map<String, Datastream> getMdRecords() {
 
         return this.mdRecords;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #setMdRecords(java.util.HashMap)
      */
     @Override
     public void setMdRecords(final Map<String, Datastream> mdRecords) throws WebserverSystemException,
         EncodingSystemException, IntegritySystemException, FedoraSystemException, TripleStoreSystemException {
         // check if mdRecords is set, contains all metadata
         // datastreams, is equal to given mdRecords and save every
         // changed datastream to fedora
 
         // get list of names of data streams with alternateId = "metadata"
         final Set<String> namesInFedora = getMdRecords().keySet();
         // delete data streams which are in fedora but not in mdRecords
         for (final String nameInFedora : namesInFedora) {
             if (!mdRecords.containsKey(nameInFedora)) {
                 final Datastream fedoraDs;
                 try {
                     fedoraDs = getMdRecord(nameInFedora);
                 }
                 catch (final MdRecordNotFoundException e) {
                     throw new IntegritySystemException("Can not find md-record previously found in item " + getId()
                         + '.', e);
                 }
                 fedoraDs.delete();
                 // TODO ? remove it from this.mdrecords?
                 // TODO create remove method?
             }
         }
         Set<Entry<String, Datastream>> mdRecordsEntrySet = mdRecords.entrySet();
         // create/activate data streams which are in mdRecords but not in fedora
         for (final Entry<String, Datastream> entry : mdRecordsEntrySet) {
             final String name = entry.getKey();
             if (!namesInFedora.contains(name)) {
                 final Datastream currentMdRecord = entry.getValue();
                 setMdRecord(name, currentMdRecord);
             }
         }
         mdRecordsEntrySet = mdRecords.entrySet();
         for (final Entry<String, Datastream> entry : mdRecordsEntrySet) {
             setMdRecord(entry.getKey(), entry.getValue());
         }
 
         // this.lastModifiedDate =
         // getTripleStoreUtility().getPropertiesElements(getId(),
         // "latest-version.date",
         // Constants.CONTAINER_PROPERTIES_NAMESPACE_URI);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #getMdRecord(java.lang.String)
      */
     @Override
     public Datastream getMdRecord(final String name) throws MdRecordNotFoundException {
         if (!this.mdRecords.containsKey(name)) {
             throw new MdRecordNotFoundException("Metadata record with name " + name + " not found in item " + getId()
                 + '.');
         }
         return this.mdRecords.get(name);
     }
 
     /**
      * 
      * @param name
      * @return
      */
     private boolean hasMdRecord(final String name) {
         return this.mdRecords.containsKey(name);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.escidoc.core.om.business.fedora.resources.interfaces.FedoraResource #setMdRecord(java.lang.String,
      * de.escidoc.core.common.business.fedora.datastream.Datastream)
      */
 
     @Override
     public void setMdRecord(final String name, final Datastream ds) throws WebserverSystemException,
         EncodingSystemException, IntegritySystemException, FedoraSystemException, TripleStoreSystemException {
         // check if the metadata datastream is set, is equal to ds and save to
         // fedora
 
         // don't trust the handler
         // ds.addAlternateId("metadata");
         final String type = ds.getAlternateIDs().get(1);
         final String schema = ds.getAlternateIDs().get(2);
         final String mimeType = ds.getMimeType();
         boolean isNew = true;
         Datastream curDs = null;
 
         if (hasMdRecord(name)) {
             try {
                 curDs = getMdRecord(name);
             }
             catch (MdRecordNotFoundException e) {
                 throw new IntegritySystemException(e);
             }
             isNew = false;
         }
 
         boolean contentChanged = false;
         if (!isNew) { // curDs is not null
             final String curMimeType = curDs.getMimeType();
             String curType = "";
             String curSchema = "";
             final List<String> altIds = curDs.getAlternateIDs();
             if (altIds.size() > 1) {
                 curType = altIds.get(1);
                 if (altIds.size() > 2) {
                     curSchema = altIds.get(2);
                 }
             }
             if (!ds.equals(curDs) || !type.equals(curType) || !schema.equals(curSchema)
                 || !mimeType.equals(curMimeType)) {
                 contentChanged = true;
             }
         }
 
         try {
             if (contentChanged || isNew) {
                 if (contentChanged && "escidoc".equals(name)) {
 
                     final Map<String, String> mdProperties = ds.getProperties();
                     if (mdProperties != null) {
                         if (mdProperties.containsKey("nsUri")) {
                             final String nsUri = mdProperties.get("nsUri");
                             final String dcNewContent =
                                 XmlUtility.createDC(nsUri, ds.toStringUTF8(), getId(),
                                     getResourcePropertiesValue(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID));
 
                             if (dcNewContent != null && dcNewContent.trim().length() > 0) {
                                 final Datastream dcNew;
                                 try {
                                     dcNew =
                                         new Datastream("DC", getId(), dcNewContent
                                             .getBytes(XmlUtility.CHARACTER_ENCODING), Datastream.MIME_TYPE_TEXT_XML);
                                 }
                                 catch (final UnsupportedEncodingException e) {
                                     throw new EncodingSystemException(e);
                                 }
                                 setDc(dcNew);
                             }
                         }
                         else {
                             throw new IntegritySystemException("namespace uri of 'escidoc' metadata"
                                 + " is not set in datastream.");
                         }
                     }
                     else {
                         throw new IntegritySystemException("Properties of 'md-record' datastream"
                             + " with the name 'escidoc' does not exist");
                     }
                 }
 
                 // isNew does not indicate that the datastream does not exist
                 // in fedora, it may be deleted
                 this.mdRecords.put(name, ds);
                 ds.merge();
 
             }
         }
         catch (final FedoraSystemException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on getting MD-records.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on getting MD-records.", e);
             }
             // this is not an update; its a create
             ds.addAlternateId(type);
             ds.addAlternateId(schema);
             this.mdRecords.put(name, ds);
             ds.persist(false);
         }
     }
 
     @Deprecated
     public Map<String, Datastream> getContentStreams() {
         return this.contentStreams;
     }
 
     @Deprecated
     public Datastream getContentStream(final String name) {
         return this.contentStreams.get(name);
     }
 
     @Deprecated
     public void setContentStream(final String name, final Datastream ds) throws FedoraSystemException,
         WebserverSystemException {
         // don't trust the handler
         final List<String> alternateIDs = new ArrayList<String>();
         alternateIDs.add("content-stream");
         ds.setAlternateIDs(alternateIDs);
 
         try {
             final Datastream curDs = getContentStream(name);
 
             boolean contentChanged = false;
             boolean isNew = false;
 
             if (curDs == null) {
                 isNew = true;
             }
             else {
                 // FIXME change storage and mime-type ? (FRS)
                 // TODO ds.getLocation() may be null -> inline content
                 if (ds.getLocation() != null && !ds.getLocation().equals(curDs.getLocation())
                     && !ds.getLocation().startsWith("/ir/item/" + getId())
                     || !curDs.getControlGroup().equals(ds.getControlGroup())
                     || !curDs.getMimeType().equals(ds.getMimeType()) || !curDs.getLabel().equals(ds.getLabel())
                     || !ds.equals(curDs)) {
                     contentChanged = true;
                 }
             }
 
             // isNew does not indicate that the datastream does not extist
             // in fedora, it may be deleted
             if (contentChanged || isNew) {
                 this.contentStreams.put(name, ds);
                 ds.merge();
             }
         }
         catch (final FedoraSystemException e) {
             if (LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on setting content stream.");
             }
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on setting content stream.", e);
             }
             // this is not an update; its a create
             this.contentStreams.put(name, ds);
             ds.persist(false);
         }
     }
 
     @Deprecated
     public void setContentStreams(final Map<String, Datastream> contentStreamDatastreams) throws FedoraSystemException,
         WebserverSystemException, IntegritySystemException {
 
         final Set<String> namesInFedora = getContentStreams().keySet();
 
         Set<Entry<String, Datastream>> contentStreamDatastreamsEntrySet = contentStreamDatastreams.entrySet();
         for (final Entry<String, Datastream> entry : contentStreamDatastreamsEntrySet) {
             final String name = entry.getKey();
             if (!namesInFedora.contains(name)) {
                 setContentStream(name, entry.getValue());
             }
         }
         // update DSs which still remain in given list
         contentStreamDatastreamsEntrySet = contentStreamDatastreams.entrySet();
         for (final Entry<String, Datastream> entry : contentStreamDatastreamsEntrySet) {
             setContentStream(entry.getKey(), entry.getValue());
         }
 
         // delete data streams which are in fedora but not in given list
         for (final String nameInFedora : namesInFedora) {
             if (!contentStreamDatastreams.containsKey(nameInFedora)) {
                 final Datastream fedoraDs = getContentStream(nameInFedora);
                 fedoraDs.delete();
                 this.contentStreams.remove(nameInFedora);
             }
         }
     }
 
     /**
      * Expand a list with names of properties values with the propertiesNames for a versionated resource. These list
      * could be used to request the TripleStore.
      * 
      * @param propertiesNames
      *            Collection of propertiesNames. The collection contains only the version resource specific
      *            propertiesNames.
      * @return Parameter name collection
      */
     private static Collection<String> expandPropertiesNames(final Collection<String> propertiesNames) {
 
         final Collection<String> newPropertiesNames =
             propertiesNames != null ? propertiesNames : new ArrayList<String>();
 
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_MODEL_TITLE);
         newPropertiesNames.add(TripleStoreUtility.PROP_CONTENT_CATEGORY);
 
         return newPropertiesNames;
     }
 
     /**
      * Expanding the properties naming map.
      * 
      * @param propertiesMapping
      *            The properties name mapping from external as key and the internal name as value. E.g. with the key
      *            "version-status" and "LATEST_VERSION_STATUS" as value is the value of "versin-status" after the
      *            mapping accessible with the internal key "LATEST_VERSION_STATUS".
      * @return The key mapping.
      */
     private static Map<String, String> expandPropertiesNamesMapping(final Map<String, String> propertiesMapping) {
 
         final Map<String, String> newPropertiesNames =
             propertiesMapping != null ? propertiesMapping : new HashMap<String, String>();
 
         newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_VERSION_PID, PropertyMapKeys.LATEST_VERSION_PID);
         newPropertiesNames.put(TripleStoreUtility.PROP_CONTENT_CATEGORY,
             PropertyMapKeys.LATEST_VERSION_CONTENT_CATEGORY);
         // FIXME release is a methd of Item/Container so this is to move higher
         // within the hirarchie
         newPropertiesNames.put(TripleStoreUtility.PROP_LATEST_RELEASE_PID, PropertyMapKeys.LATEST_RELEASE_PID);
 
         return newPropertiesNames;
     }
 
     /**
      * See Interface for functional description.
      * 
      * @return resource properties.
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      * @see GenericResource#getResourceProperties()
      */
     @Override
     public Map<String, String> getResourceProperties() throws TripleStoreSystemException, WebserverSystemException {
 
         if (!this.resourceInit) {
             super.getResourceProperties();
 
             // override dc properties
             try {
                 addResourceProperties(getDublinCorePropertiesMap());
             }
             catch (final XmlParserSystemException e) {
                 throw new WebserverSystemException(e);
             }
             this.resourceInit = true;
         }
         // FIXME add caching of mapping
         return super.getResourceProperties();
     }
 
     /**
      * Persist all Component of this Item.
      * 
      * @return true if a Component was updated (and with this persisted), false otherwise.
      * @throws ComponentNotFoundException
      *             Thrown if Component was not found.
      * @throws de.escidoc.core.common.exceptions.system.WebserverSystemException
      * @throws de.escidoc.core.common.exceptions.system.XmlParserSystemException
      * @throws de.escidoc.core.common.exceptions.system.TripleStoreSystemException
      * @throws de.escidoc.core.common.exceptions.system.FedoraSystemException
      * @throws de.escidoc.core.common.exceptions.system.IntegritySystemException
      */
     public boolean persistComponents() throws ComponentNotFoundException, TripleStoreSystemException,
         FedoraSystemException, XmlParserSystemException, WebserverSystemException, IntegritySystemException {
 
         boolean resourceUpdated = false;
 
         for (final String s : getComponentIds()) {
             final Component component = getComponent(s);
             if (component != null) {
                 final String lmd = component.getLastFedoraModificationDate();
                 final String newLmd = component.persist(false);
                 if (!lmd.equals(newLmd)) {
                     resourceUpdated = true;
                 }
             }
         }
         if (resourceUpdated) {
             getFedoraUtility().sync();
         }
 
         if (!this.alteredComponent.isEmpty()) {
             resourceUpdated = true;
             // persist is called during the alter method
             // should move at this position
             // this.alteredComponent = new Vector<String>();
         }
 
         return resourceUpdated;
     }
 
     /**
      * Persist Item (with all Components).
      * 
      * @return last modification date of Item. Null if Item was not written.
      * @throws FedoraSystemException
      *             Thrown if request of Fedora failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     @Override
     public String persist() throws FedoraSystemException, WebserverSystemException {
 
         try {
             if (persistComponents()) {
                 getFedoraUtility().touchObject(getId(), true);
                 this.setNeedSync(true);
             }
         }
         catch (final ComponentNotFoundException e) {
             throw new WebserverSystemException(e);
         }
         catch (final SystemException e) {
             throw new WebserverSystemException(e);
         }
         if (!this.alteredComponent.isEmpty()) {
             this.alteredComponent = new ArrayList<String>();
         }
         return super.persist();
     }
 
     /**
      * Init all item datastreams. Some are initilized by super classes. (This is faster than init of each single data
      * stream).
      * 
      * @param datastreamInfos
      *            The Fedora datastream information.
      */
     @Override
     protected final void initDatastreams(final org.fcrepo.server.types.gen.Datastream[] datastreamInfos)
         throws WebserverSystemException, FedoraSystemException, TripleStoreSystemException, IntegritySystemException,
         StreamNotFoundException {
 
         super.initDatastreams(datastreamInfos);
 
         for (final org.fcrepo.server.types.gen.Datastream datastreamInfo : datastreamInfos) {
             final List<String> altIDs = Arrays.asList(datastreamInfo.getAltIDs());
             final String name = datastreamInfo.getID();
             final String label = datastreamInfo.getLabel();
             final DatastreamControlGroup controlGroup = datastreamInfo.getControlGroup();
             final String controlGroupValue = controlGroup.getValue();
             final String mimeType = datastreamInfo.getMIMEType();
             final String location = datastreamInfo.getLocation();
 
             final Datastream ds;
             if (altIDs.contains(Datastream.METADATA_ALTERNATE_ID)) {
                 // found md-record
                 ds = new Datastream(name, getId(), getVersionDate(), mimeType, location, controlGroupValue);
                 ds.setAlternateIDs(new ArrayList<String>(altIDs));
                 ds.setLabel(label);
                 this.mdRecords.put(name, ds);
             }
             else if (altIDs.contains("content-stream")) {
                 // found content-stream
                 ds = new Datastream(name, getId(), getVersionDate(), mimeType, location, controlGroupValue);
                 ds.setAlternateIDs(new ArrayList<String>(altIDs));
                 ds.setLabel(label);
                 this.contentStreams.put(name, ds);
             }
             // content-model-specific
             else if ("content-model-specific".equals(name)) {
                 ds = new Datastream(name, getId(), getVersionDate(), mimeType, location, controlGroupValue);
                 ds.setAlternateIDs(new ArrayList<String>(altIDs));
                 ds.setLabel(label);
                 this.cts = ds;
             }
             else {
                 if (LOGGER.isDebugEnabled()) {
                     LOGGER.debug("Datastream " + getId() + '/' + name + " not instanziated in Item.<init>.");
                 }
             }
         }
     }
 
     /**
      * Removes a component from an item.
      * 
      * @param componentId
      *            The component ID.
      * @throws ComponentNotFoundException
      *             Thrown if the Component with the id was not found.
      * @throws WebserverSystemException
      *             In case of an internal error.
      * @throws LockingException
      *             If the iem is locked and the current user is not the one who locked it.
      * @throws TripleStoreSystemException
      *             If triple store reports an error.
      * @throws InvalidStatusException
      *             If component is in invalid status for removing.
      * @throws FedoraSystemException
      *             If Fedora reports an error.
      * @throws XmlParserSystemException
      *             If parsing of xml data fails.
      * @throws IntegritySystemException
      *             If the integrity of the repository is violated.
      * @throws EncodingSystemException
      *             If encoding fails.
      */
     private void removeComponent(final String componentId) throws LockingException, ComponentNotFoundException,
         WebserverSystemException, InvalidStatusException, TripleStoreSystemException, FedoraSystemException,
         XmlParserSystemException, IntegritySystemException, EncodingSystemException {
 
         // TODO move precondition checks to service method (?FRS)
         // checkLocked();
         // checkReleased();
         // final String status =
         // getItem().getProperty(TripleStoreUtility.PROP_PUBLIC_STATUS);
         // if (!(status.equals(Constants.STATUS_PENDING))) {
         // throw new InvalidStatusException("Item " + getItem().getId()
         // + " is in status " + status + ". Can not delete.");
         // }
         // Don't purge the component object, only delete the entry in item
         // RELS-EXT
         // getFedoraUtility().deleteObject(componentId, false);
 
         final StaxParser sp = new StaxParser();
         final RemoveObjectRelationHandlerNew rh = new RemoveObjectRelationHandlerNew(componentId);
         sp.addHandler(rh);
 
         try {
             sp.parse(getRelsExt().getStream());
 
             final String newRelsExt = rh.getOutputStream().toString();
             setRelsExt(newRelsExt);
             // FIXME: sync() is not needed, ItemHandlerCretae().setComponents
             // does not queri a Triple Store
             // FedoraUtility.getInstance().sync();
             // TODO delete component from components or isn't it necessary
             // because this session is stateless (? FRS)
         }
         catch (final StreamNotFoundException e) {
             throw new IntegritySystemException(e);
         }
         catch (final XMLStreamException e) {
             throw new XmlParserSystemException(e);
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
 
         if (rh.getNoOfRemovedObjects() != 1) {
             throw new ComponentNotFoundException("Component with the id '" + componentId + "' not found.");
         }
     }
 
     private void addComponentToRelsExt(final String componentId) throws FedoraSystemException,
         WebserverSystemException, IntegritySystemException, EncodingSystemException {
 
         final StaxParser sp = new StaxParser();
 
         final AddNewSubTreesToDatastream addNewEntriesHandler = new AddNewSubTreesToDatastream("/RDF", sp);
 
         final StartElement pointer = new StartElement();
         pointer.setLocalName("Description");
         pointer.setPrefix(Constants.RDF_NAMESPACE_PREFIX);
         pointer.setNamespace(Constants.RDF_NAMESPACE_URI);
 
         addNewEntriesHandler.setPointerElement(pointer);
         final StartElementWithChildElements newComponentIdElement = new StartElementWithChildElements();
         newComponentIdElement.setLocalName("component");
         newComponentIdElement.setPrefix(Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         newComponentIdElement.setNamespace(Constants.STRUCTURAL_RELATIONS_NS_URI);
         final Attribute resource =
             new Attribute("resource", Constants.RDF_NAMESPACE_URI, Constants.RDF_NAMESPACE_PREFIX,
                 Constants.IDENTIFIER_PREFIX + componentId);
         newComponentIdElement.addAttribute(resource);
         // newComponentIdElement.setElementText(componentId);
         newComponentIdElement.setChildrenElements(null);
         final List<StartElementWithChildElements> elements = new ArrayList<StartElementWithChildElements>();
 
         elements.add(newComponentIdElement);
         addNewEntriesHandler.setSubtreeToInsert(elements);
         sp.addHandler(addNewEntriesHandler);
 
         try {
             sp.parse(getRelsExt().getStream());
         }
         catch (final Exception e) {
             throw new WebserverSystemException(e);
         }
         sp.clearHandlerChain();
         final ByteArrayOutputStream relsExtNew = addNewEntriesHandler.getOutputStreams();
 
         try {
             setRelsExt(relsExtNew);
         }
         catch (final StreamNotFoundException e1) {
             throw new IntegritySystemException(e1);
         }
     }
 }
