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
 package de.escidoc.core.om.business.fedora.item;
 
 import de.escidoc.core.common.business.PropertyMapKeys;
 import de.escidoc.core.common.business.fedora.Constants;
 import de.escidoc.core.common.business.fedora.TripleStoreUtility;
 import de.escidoc.core.common.business.fedora.datastream.Datastream;
 import de.escidoc.core.common.business.fedora.resources.Item;
 import de.escidoc.core.common.business.fedora.resources.Relation;
 import de.escidoc.core.common.business.fedora.resources.item.Component;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.StreamNotFoundException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.FedoraSystemException;
 import de.escidoc.core.common.exceptions.system.IntegritySystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.date.Iso8601Util;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.WovContentRelationsRetrieveHandler;
 import de.escidoc.core.common.util.xml.Elements;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.factory.FoXmlProvider;
 import de.escidoc.core.common.util.xml.factory.ItemXmlProvider;
 import de.escidoc.core.common.util.xml.factory.XmlTemplateProvider;
 import de.escidoc.core.om.business.renderer.VelocityXmlCommonRenderer;
 import de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface;
 import org.joda.time.DateTimeZone;
 import org.joda.time.format.ISODateTimeFormat;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * This is a class, indeed.
  * 
  * @author MSC
  * 
  */
 public class ItemHandlerRetrieve extends ItemHandlerBase
     implements ItemRendererInterface {
 
     private static final AppLogger log = new AppLogger(
         ItemHandlerRetrieve.class.getName());
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #render()
      */
     public String render() throws SystemException, ComponentNotFoundException,
         ItemNotFoundException {
 
         String result;
         Map<String, Object> values = new HashMap<String, Object>();
 
         Map<String, String> commonValues = getCommonValues(getItem());
 
         values.putAll(getPropertiesValues(getItem()));
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_CONTENT,
             renderMdRecords(commonValues, false));
         values.put(XmlTemplateProvider.CONTENT_STREAMS,
             renderContentStreams(commonValues, false));
         values.put(XmlTemplateProvider.VAR_COMPONENTS_CONTENT,
             renderComponents(commonValues, false));
         values.putAll(getRelationValues(getItem()));
         values.putAll(getResourcesValues(getItem()));
         values.putAll(commonValues);
 
         result = ItemXmlProvider.getInstance().getItemXml(values);
         return result;
 
     }
 
     /**
      * Get XML representation of Components.
      * 
      * @param isRoot
      *            Set true if Components is XML root element, false if it is XML
      *            sub-element.
      * @return XML representation of Components
      * @throws ComponentNotFoundException
      *             Thrown if Component was not found
      * @throws SystemException
      *             Thrown if an unexpected error occurs
      */
     public String renderComponents(final boolean isRoot)
         throws ComponentNotFoundException, SystemException {
 
         return renderComponents(getCommonValues(getItem()), isRoot);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderComponents(boolean)
      */
     public String renderComponents(
         final Map<String, String> commonValues, final boolean isRoot)
         throws ComponentNotFoundException, SystemException {
 
         String result;
 
         Map<String, String> values = new HashMap<String, String>();
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                 XmlTemplateProvider.TRUE);
         }
 
         Collection<String> componentIds;
         String originObjectId =
             getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         if (originObjectId != null) {
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values.putAll(getCommonValues(getOriginItem()));
             values
                 .put("componentsTitle", "Components of Item " + getOriginId());
             values.put("componentsHref",
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + getOriginId() + "/components");
 
             componentIds = getOriginItem().getComponentIds();
         }
         else {
 
             values.putAll(commonValues);
             values.put("componentsTitle", "Components of Item "
                 + getItem().getId());
             values.put("componentsHref", getItem().getHref() + "/components");
 
             componentIds = getItem().getComponentIds();
         }
         if (componentIds.size() > 0) {
             StringBuffer renderedComponents = new StringBuffer();
             for (String componentId1 : componentIds) {
                 String componentId = componentId1;
                 try {
                     renderedComponents.append(renderComponent(componentId, commonValues, false));
                 } catch (ComponentNotFoundException e) {
                     throw new IntegritySystemException(e);
                 }
             }
             values.put("components", renderedComponents.toString());
         }
 
         result = ItemXmlProvider.getInstance().getComponentsXml(values);
         return result;
     }
 
     /**
      * Get XML representation of Component.
      * 
      * @param id
      *            objid of Component
      * @param isRoot
      *            Set true if Component is XML root element, false if it is XML
      *            sub-element.
      * @return XML representation of Component with provided id
      * @throws ComponentNotFoundException
      *             Thrown if Component was not found
      * @throws SystemException
      *             Thrown if an unexpected error occurs
      */
     public String renderComponent(final String id, final boolean isRoot)
         throws ComponentNotFoundException, SystemException {
 
         return renderComponent(id, getCommonValues(getItem()), isRoot);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderComponent(java.lang.String, boolean)
      */
     public String renderComponent(
         final String id, final Map<String, String> commonValues,
         final boolean isRoot) throws ComponentNotFoundException,
         SystemException {
 
         Component component = getComponent(id);
         String result;
 
         Map<String, String> values = new HashMap<String, String>();
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT, XmlTemplateProvider.TRUE);
         }
         String originObjectId =
             getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         if (originObjectId != null) {
             component = getComponent(id);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values.put("componentHref",
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + getOriginId() + component.getHrefPart());
             values.putAll(getCommonValues(getOriginItem()));
         }
         else {
             component = getComponent(id);
             values.put("componentHref",
                 getItem().getHref() + component.getHrefPart());
             values.putAll(getCommonValues(getItem()));
         }
         values
             .put(
                 XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
                 de.escidoc.core.common.business.Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values
             .put(
                 XmlTemplateProvider.MD_RECORDS_NAMESPACE,
                 de.escidoc.core.common.business.Constants.METADATARECORDS_NAMESPACE_URI);
         values.put("componentTitle", component.getTitle());
         values.put("componentHref",
             getItem().getHref() + component.getHrefPart());
         values.put("componentId", component.getId());
         values.putAll(commonValues);
         values.putAll(getComponentPropertiesValues(component));
         values.put("componentContentTitle", component.getTitle());
 
         Datastream content = component.getContent();
         String storage = content.getControlGroup();
         if (storage.equals(FoXmlProvider.CONTROL_GROUP_M)) {
             values.put("storage", Constants.STORAGE_INTERNAL_MANAGED);
             if (getOriginItem() != null) {
                 values.put("componentContentHref",
                     de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                         + getOriginId() + component.getHrefPart() + "/content");
             }
             else {
                 values.put("componentContentHref", getItem().getHref()
                     + component.getHrefPart() + "/content");
             }
         }
         else if (storage.equals(FoXmlProvider.CONTROL_GROUP_E)) {
             values.put("storage", Constants.STORAGE_EXTERNAL_MANAGED);
             if (getOriginItem() != null) {
                 values.put("componentContentHref",
                     de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                         + getOriginId() + component.getHrefPart() + "/content");
             }
             else {
                 values.put("componentContentHref", getItem().getHref()
                     + component.getHrefPart() + "/content");
             }
         }
         else if (storage.equals(FoXmlProvider.CONTROL_GROUP_R)) {
             values.put("storage", Constants.STORAGE_EXTERNAL_URL);
             values.put("componentContentHref", content.getLocation());
         }
 
         String mdRecordsContent =
             renderComponentMdRecords(component.getId(), commonValues, false);
         if (mdRecordsContent.length() > 0) {
             values.put("componentMdRecordsContent", mdRecordsContent);
         }
 
         result = ItemXmlProvider.getInstance().getComponentXml(values);
         return result;
     }
 
     public String renderComponentProperties(final String id)
         throws ComponentNotFoundException, FedoraSystemException,
         SystemException {
 
         Component component;
         Map<String, String> values = new HashMap<String, String>();
 
         if (getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN) != null) {
             component = getComponent(id);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values.put("componentHref",
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + getOriginId() + component.getHrefPart());
             values.putAll(getCommonValues(getOriginItem()));
         }
         else {
 
             component = getComponent(id);
             values.putAll(getCommonValues(getItem()));
         }
         String result;
 
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.TRUE);
         values.put("componentHref",
             getItem().getHref() + component.getHrefPart());
         values.put("componentId", component.getId());
         values.putAll(getComponentPropertiesValues(component));
 
         result =
             ItemXmlProvider.getInstance().getComponentPropertiesXml(values);
 
         return result;
     }
 
     public String renderMdRecords(final boolean isRoot)
         throws WebserverSystemException, EncodingSystemException,
         FedoraSystemException, IntegritySystemException,
         TripleStoreSystemException {
 
         return renderMdRecords(getCommonValues(getItem()), isRoot);
     }
 
     public String renderMdRecords(
         final Map<String, String> commonValues, final boolean isRoot)
         throws WebserverSystemException, EncodingSystemException,
         FedoraSystemException, IntegritySystemException,
         TripleStoreSystemException {
 
         HashMap<String, Datastream> mdRecords =
             (HashMap<String, Datastream>) getItem().getMdRecords();
 
         StringBuffer content = new StringBuffer();
         Map<String, String> values = new HashMap<String, String>();
 
         Iterator<String> namesIter = mdRecords.keySet().iterator();
         while (namesIter.hasNext()) {
             String mdRecordName = namesIter.next();
             try {
                 String mdRecordContent =
                     renderMdRecord(mdRecordName, commonValues, false, false);
                 if (mdRecordContent.equals("")) {
                     namesIter.remove();
                 }
                 else {
                     content.append(mdRecordContent);
                 }
             }
             catch (MdRecordNotFoundException e) {
                 throw new WebserverSystemException(
                     "Metadata record previously found in list not found.", e);
             }
         }
 
         if (getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN) != null) {
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             HashMap<String, Datastream> originMdRecords =
                 (HashMap<String, Datastream>) getOriginItem().getMdRecords();
             for (String s : originMdRecords.keySet()) {
                 String mdRecordName = s;
                 if (!mdRecords.keySet().contains(mdRecordName)) {
                     try {
                         content.append(renderMdRecord(mdRecordName,
                                 commonValues, true, false));
                     } catch (MdRecordNotFoundException e) {
                         throw new WebserverSystemException(
                                 "Metadata record previously found in list not found.",
                                 e);
                     }
                 }
             }
         }
         if (content.length() == 0) {
             return "";
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_HREF, getItem().getHref()
             + de.escidoc.core.common.business.Constants.MD_RECORDS_URL_PART);
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_TITLE,
             "Metadata Records of Item " + getItem().getId());
         values.put(XmlTemplateProvider.VAR_MD_RECORDS_CONTENT,
             content.toString());
 
         return ItemXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     /**
      * Render MD Record whereas the common values are not to recompile.
      * 
      * @param name
      *            Name of md-record.
      * @param commonValues
      *            Common render values.
      * @param isOrigin
      *            set true if Item is origin Item, false otherwise
      * @param isRoot
      *            Set true is md-record is to render with XML root element
      * @return XMl representation of md-record.
      * 
      * @throws WebserverSystemException
      * @throws IntegritySystemException
      * @throws EncodingSystemException
      * @throws MdRecordNotFoundException
      * @throws TripleStoreSystemException
      */
     public String renderMdRecord(
         final String name, final Map<String, String> commonValues,
         final boolean isOrigin, final boolean isRoot)
         throws WebserverSystemException, IntegritySystemException,
         EncodingSystemException, MdRecordNotFoundException,
         TripleStoreSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
         Datastream ds;
         if (isOrigin) {
             ds = getOriginItem().getMdRecord(name);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values
                 .put(
                     XmlTemplateProvider.VAR_MD_RECORD_HREF,
                     de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                         + getOriginId()
                         + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
                         + "/" + name);
             if (name
                 .equals(XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING)) {
                 commonValues.put(XmlTemplateProvider.TITLE, getOriginItem()
                     .getTitle());
             }
         }
         else {
             ds = getItem().getMdRecord(name);
             values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF, getItem()
                 .getHref()
                 + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
                 + "/" + name);
         }
 
         if (ds.isDeleted()) {
             return "";
         }
 
         List<String> altIds = ds.getAlternateIDs();
         if (altIds.size() > 1 && !altIds.get(1).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_TYPE, altIds.get(1));
         }
         if (altIds.size() > 2 && !altIds.get(2).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_SCHEMA, altIds.get(2));
         }
         try {
             values.put(XmlTemplateProvider.MD_RECORD_CONTENT,
                 ds.toString(XmlUtility.CHARACTER_ENCODING));
         }
         catch (EncodingSystemException e) {
             throw new EncodingSystemException(e.getMessage(), e);
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.MD_RECORD_NAME, name);
         values.put(XmlTemplateProvider.VAR_MD_RECORD_TITLE, name);
         values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF, getItem().getHref()
             + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
             + "/" + name);
 
         return ItemXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /**
      * Render MD Record whereas the common values are not to recompile.
      * 
      * @param name
      *            Name of md-record.
      * @param isOrigin
      *            set true if Item is origin Item, false otherwise
      * @param isRoot
      *            Set true is md-record is to render with XML root element
      * @return XMl representation of md-record.
      * 
      * @throws WebserverSystemException
      * @throws IntegritySystemException
      * @throws EncodingSystemException
      * @throws MdRecordNotFoundException
      * @throws TripleStoreSystemException
      */
     public String renderMdRecord(
         final String name, final boolean isOrigin, final boolean isRoot)
         throws WebserverSystemException, IntegritySystemException,
         EncodingSystemException, MdRecordNotFoundException,
         TripleStoreSystemException {
 
         return renderMdRecord(name, getCommonValues(getItem()), isOrigin,
             isRoot);
     }
 
     @Deprecated
     public String retrieveMdRecord(final String name, final boolean isOrigin)
         throws MdRecordNotFoundException {
         Datastream mdRecord;
         if (isOrigin) {
             mdRecord = getOriginItem().getMdRecord(name);
         }
         else {
             mdRecord = getItem().getMdRecord(name);
         }
         if (mdRecord.isDeleted()) {
             String message =
                 "Metadata record with name " + name + " not found in item "
                     + getItem().getId() + ".";
             log.error(message);
             throw new MdRecordNotFoundException();
         }
 
         return mdRecord.toString();
     }
 
     public String renderContentStreams(final boolean isRoot)
         throws WebserverSystemException, EncodingSystemException,
         FedoraSystemException, IntegritySystemException,
         TripleStoreSystemException {
 
         return renderContentStreams(getCommonValues(getItem()), isRoot);
     }
 
     public String renderContentStreams(
         final Map<String, String> commonValues, final boolean isRoot)
         throws WebserverSystemException, EncodingSystemException,
         FedoraSystemException, IntegritySystemException,
         TripleStoreSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
         StringBuffer content = new StringBuffer();
         if (getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN) != null) {
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             for (String s : ((HashMap<String, Datastream>) getOriginItem()
                     .getContentStreams()).keySet()) {
                 String contentStreamName = s;
                 content.append(renderContentStream(contentStreamName, false));
             }
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_HREF,
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + getOriginId() + Constants.CONTENT_STREAMS_URL_PART);
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_TITLE,
                 "Content streams of Item " + getOriginId());
         }
         else {
             for (String s : ((HashMap<String, Datastream>) getItem().getContentStreams())
                     .keySet()) {
                 String contentStreamName = s;
                 content.append(renderContentStream(contentStreamName,
                         commonValues, false));
             }
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_HREF, getItem()
                 .getHref() + Constants.CONTENT_STREAMS_URL_PART);
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_TITLE,
                 "Content streams of Item " + getItem().getId());
 
         }
         if (content.length() == 0) {
             return "";
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.VAR_CONTENT_STREAMS_CONTENT,
             content.toString());
         return ItemXmlProvider.getInstance().getContentStreamsXml(values);
     }
 
     public String renderContentStream(final String name, final boolean isRoot)
         throws WebserverSystemException, IntegritySystemException,
         TripleStoreSystemException {
 
         return renderContentStream(name, getCommonValues(getItem()), isRoot);
     }
 
     public String renderContentStream(
         final String name, final Map<String, String> commonValues,
         final boolean isRoot) throws WebserverSystemException,
         IntegritySystemException, TripleStoreSystemException {
         Map<String, String> values = new HashMap<String, String>();
 
         Datastream ds;
 
         if (isRoot) {
             values.put("isRootContentStream", XmlTemplateProvider.TRUE);
         }
         String originObjectId =
             getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         if (originObjectId != null) {
             ds = getOriginItem().getContentStream(name);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
         }
         else {
             ds = getItem().getContentStream(name);
         }
         values.putAll(commonValues);
         values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_NAME, ds.getName());
         values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_TITLE, ds.getLabel());
         String location = ds.getLocation();
         if (ds.getControlGroup().equals("M")
             || ds.getControlGroup().equals("X")) {
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                 Constants.STORAGE_INTERNAL_MANAGED);
             location =
                 getItem().getHref() + Constants.CONTENT_STREAM_URL_PART + "/"
                     + ds.getName()
                     + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;
             if (ds.getControlGroup().equals("X")) {
                 try {
                     values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_CONTENT,
                         ds.toStringUTF8());
                 }
                 catch (EncodingSystemException e) {
                     throw new WebserverSystemException(e);
                 }
             }
         }
         else if (ds.getControlGroup().equals("E")) {
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                 Constants.STORAGE_EXTERNAL_MANAGED);
             location =
                 getItem().getHref() + Constants.CONTENT_STREAM_URL_PART + "/"
                     + ds.getName()
                     + Constants.CONTENT_STREAM_CONTENT_URL_EXTENSION;
 
         }
         else if (ds.getControlGroup().equals("R")) {
             values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_STORAGE,
                 Constants.STORAGE_EXTERNAL_URL);
         }
         values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_MIME_TYPE,
             ds.getMimeType());
         values.put(XmlTemplateProvider.VAR_CONTENT_STREAM_HREF, location);
 
         return ItemXmlProvider.getInstance().getContentStreamXml(values);
     }
 
     public String renderComponentMdRecords(
         final String componentId, final boolean isRoot)
         throws ComponentNotFoundException, SystemException {
 
         return renderComponentMdRecords(componentId,
             getCommonValues(getItem()), isRoot);
     }
 
     public String renderComponentMdRecords(
         final String componentId, final Map<String, String> commonValues,
         final boolean isRoot) throws ComponentNotFoundException,
         SystemException {
 
         Component component;
         Map<String, String> values = new HashMap<String, String>();
         String originObjectId =
             getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         if (originObjectId != null) {
             component = getComponent(componentId);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values
                 .put(
                     XmlTemplateProvider.VAR_MD_RECORDS_HREF,
                     de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                         + getOriginId()
                         + component.getHrefPart()
                         + de.escidoc.core.common.business.Constants.MD_RECORDS_URL_PART);
             values.putAll(getCommonValues(getOriginItem()));
         }
         else {
             component = getComponent(componentId);
             values
                 .put(
                     XmlTemplateProvider.VAR_MD_RECORD_HREF,
                     getItem().getHref()
                         + component.getHrefPart()
                         + de.escidoc.core.common.business.Constants.MD_RECORDS_URL_PART);
             values.putAll(getCommonValues(getItem()));
         }
         HashMap<String, Datastream> mdRecords =
             (HashMap<String, Datastream>) component.getMdRecords();
         StringBuffer content = new StringBuffer();
         for (String s : mdRecords.keySet()) {
             String mdRecordName = s;
             try {
                 content.append(renderComponentMdRecord(componentId,
                         mdRecordName, commonValues, false));
             } catch (MdRecordNotFoundException e) {
                 throw new IntegritySystemException(e.getMessage(), e);
             }
         }
         if (!isRoot && content.length() == 0) {
             return "";
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_SUB_RESOURCE,
                 XmlTemplateProvider.TRUE);
         }
         values.putAll(commonValues);
         values
             .put("mdRecordsHref", getItem().getHref() + component.getHrefPart()
                 + de.escidoc.core.common.business.Constants.MD_RECORDS_URL_PART);
         values.put("mdRecordsTitle", "Metadata Records of Component "
             + component.getId());
         values.put("mdRecordsContent", content.toString());
         return ItemXmlProvider.getInstance().getMdRecordsXml(values);
     }
 
     public String renderComponentMdRecord(
         final String componentId, final String name, final boolean isRoot)
         throws MdRecordNotFoundException, ComponentNotFoundException,
         FedoraSystemException, SystemException {
 
         return renderComponentMdRecord(componentId, name,
             getCommonValues(getItem()), isRoot);
     }
 
     public String renderComponentMdRecord(
         final String componentId, final String name,
         final Map<String, String> commonValues, final boolean isRoot)
         throws MdRecordNotFoundException, ComponentNotFoundException,
         FedoraSystemException, SystemException {
 
         Component component;
         Map<String, String> values = new HashMap<String, String>();
         String originObjectId =
             getItem().getResourceProperties().get(PropertyMapKeys.ORIGIN);
         if (originObjectId != null) {
             component = getComponent(componentId);
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             values
                 .put(
                     XmlTemplateProvider.VAR_MD_RECORD_HREF,
                     de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                         + getOriginId()
                         + component.getHrefPart()
                         + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
                         + "/" + name);
             values.putAll(getCommonValues(getOriginItem()));
         }
         else {
             component = getComponent(componentId);
             values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF, getItem()
                 .getHref()
                 + component.getHrefPart()
                 + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
                 + "/" + name);
             values.putAll(commonValues);
         }
 
         Datastream ds = component.getMdRecord(name);
         if (ds.isDeleted()) {
             return "";
         }
         List<String> altIds = ds.getAlternateIDs();
         if (altIds.size() > 1 && !altIds.get(1).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_TYPE, altIds.get(1));
         }
         if (altIds.size() > 2 && !altIds.get(2).equals("unknown")) {
             values.put(XmlTemplateProvider.MD_RECORD_SCHEMA, altIds.get(2));
         }
         try {
             values.put(XmlTemplateProvider.MD_RECORD_CONTENT,
                 ds.toString(XmlUtility.CHARACTER_ENCODING));
         }
         catch (EncodingSystemException e) {
             throw new EncodingSystemException(e.getMessage(), e);
         }
 
         if (isRoot) {
             values.put(XmlTemplateProvider.IS_ROOT_MD_RECORD,
                 XmlTemplateProvider.TRUE);
         }
         values.put(XmlTemplateProvider.MD_RECORD_NAME, name);
         values.put(XmlTemplateProvider.VAR_MD_RECORD_TITLE, name);
         values.put(XmlTemplateProvider.VAR_MD_RECORD_HREF, getItem().getHref()
             + component.getHrefPart()
             + de.escidoc.core.common.business.Constants.MD_RECORD_URL_PART
             + "/" + name);
         return ItemXmlProvider.getInstance().getMdRecordXml(values);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderProperties(de.escidoc.core.common.business.fedora.resources.Item)
      */
     public String renderProperties() throws WebserverSystemException,
         TripleStoreSystemException, IntegritySystemException,
         XmlParserSystemException, EncodingSystemException,
         FedoraSystemException, ItemNotFoundException {
         String result;
 
         Map<String, String> values = new HashMap<String, String>();
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.TRUE);
         if (getOriginItem() != null) {
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
         }
         values.putAll(getCommonValues(getItem()));
         values.putAll(getPropertiesValues(getItem()));
 
         result = ItemXmlProvider.getInstance().getItemPropertiesXml(values);
 
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderRelations(de.escidoc.core.common.business.fedora.resources.Item)
      */
     public String renderRelations() throws WebserverSystemException,
         FedoraSystemException, IntegritySystemException,
         XmlParserSystemException, TripleStoreSystemException {
         String result;
 
         Map<String, Object> values = new HashMap<String, Object>();
         values.put("isRootRelations", XmlTemplateProvider.TRUE);
         values.putAll(getCommonValues(getItem()));
         values.putAll(getRelationValues(getItem()));
 
         result = ItemXmlProvider.getInstance().getItemRelationsXml(values);
 
         return result;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderResources(de.escidoc.core.common.business.fedora.resources.Item)
      */
     public String renderResources() throws WebserverSystemException {
         String result;
 
         Map<String, Object> values = new HashMap<String, Object>();
         values.put(XmlTemplateProvider.IS_ROOT_PROPERTIES,
             XmlTemplateProvider.TRUE);
         values.putAll(getCommonValues(getItem()));
         values.putAll(getResourcesValues(getItem()));
 
         // set this true if the reources should point to the
         // digilib client (digicat). The item XML Schema is currently not
         // compaptible with the this extension.
         values.put("showContentTransformer", XmlTemplateProvider.FALSE);
 
         result = ItemXmlProvider.getInstance().getItemResourcesXml(values);
 
         return result;
     }
 
     /**
      * Gets the representation of the virtual resource <code>parents</code> of
      * an item/container.
      * 
      * @return Returns the XML representation of the virtual resource
      *         <code>parents</code> of an container.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     public String renderParents(String itemId) throws SystemException {
 
         String result;
         Map<String, Object> values = new HashMap<String, Object>();
         addXlinkValues(values);
         addStructuralRelationsValues(values);
         values.put("isRootParents", XmlTemplateProvider.TRUE);
         values.put(
             XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE,
             ISODateTimeFormat
                 .dateTime().withZone(DateTimeZone.UTC)
                 .print(System.currentTimeMillis()));
         addParentsValues(values, itemId);
         addParentsNamespaceValues(values);
         result = ItemXmlProvider.getInstance().getParentsXml(values);
         return result;
     }
 
     /**
      * Adds the parents values to the provided map.
      * 
      * @param values
      *            The map to add values to.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     private void addParentsValues(
         final Map<String, Object> values, final String itemId)
         throws SystemException {
         values.put("parentsHref",
             XmlUtility.getItemParentsHref(XmlUtility.getItemHref(itemId)));
         values.put("parentsTitle", "parents of item " + itemId);
 
         final StringBuffer query =
             getTripleStoreUtility().getRetrieveSelectClause(true,
                 TripleStoreUtility.PROP_MEMBER);
 
         if (query.length() > 0) {
             query.append(getTripleStoreUtility().getRetrieveWhereClause(true,
                 TripleStoreUtility.PROP_MEMBER, itemId, null, null, null));
             List<String> ids = new ArrayList<String>();
             try {
                 ids = getTripleStoreUtility().retrieve(query.toString());
             }
             catch (TripleStoreSystemException e) {
             }
             Iterator<String> idIter = ids.iterator();
             List<Map<String, String>> entries =
                 new ArrayList<Map<String, String>>(ids.size());
             while (idIter.hasNext()) {
                 Map<String, String> entry = new HashMap<String, String>(3);
                 String id = idIter.next();
                 entry.put("id", id);
                 entry.put("href", XmlUtility.getContainerHref(id));
                 entry.put("title", getTripleStoreUtility().getTitle(id));
 
                 entries.add(entry);
             }
             if (!entries.isEmpty()) {
                 values.put(XmlTemplateProvider.VAR_PARENTS, entries);
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * de.escidoc.core.om.business.renderer.interfaces.ItemRendererInterface
      * #renderItems(java.util.List)
      */
     public String renderItems(final List<String> items) throws SystemException {
 
         List<String> renderedEntries = new ArrayList<String>();
         Map<String, Object> values = new HashMap<String, Object>();
 
         for (String item : items) {
             String itemId = item;
             try {
                 setItem(itemId);
                 renderedEntries.add(render());
 
             } catch (ResourceNotFoundException e) {
                 String msg =
                         "FedoraItemHandler.retrieveItems: can not retrieve object "
                                 + itemId + ". ResourceNotFoundException: "
                                 + e.getCause() + ".";
                 log.error(msg);
                 throw new WebserverSystemException(msg, e);
             }
         }
         values.put(XmlTemplateProvider.VAR_ITEM_LIST_MEMBERS, renderedEntries);
 
         values.put(XmlTemplateProvider.VAR_ITEM_LIST_TITLE, "list of items");
         values.put(XmlTemplateProvider.VAR_ITEM_LIST_NAMESPACE,
             de.escidoc.core.common.business.Constants.ITEM_LIST_NAMESPACE_URI);
         values
             .put(
                 XmlTemplateProvider.VAR_ITEM_LIST_NAMESPACE_PREFIX,
                 de.escidoc.core.common.business.Constants.ITEM_LIST_NAMESPACE_PREFIX);
         values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
             XmlUtility.getEscidocBaseUrl());
 
         return getItemXmlProvider().getItemListXml(values);
     }
 
     /**
      * Prepare properties values from item resource as velocity values.
      * 
      * @param item
      *            The Item.
      * @return Map with properties values (for velocity template)
      * 
      * @throws TripleStoreSystemException
      * @throws WebserverSystemException
      * @throws IntegritySystemException
      * @throws XmlParserSystemException
      * @throws EncodingSystemException
      * @throws FedoraSystemException
      * @throws ItemNotFoundException
      */
     public Map<String, String> getPropertiesValues(final Item item)
         throws TripleStoreSystemException, WebserverSystemException,
         IntegritySystemException, XmlParserSystemException,
         EncodingSystemException, FedoraSystemException, ItemNotFoundException {
 
         // retrieve properties from resource (the resource decided where are the
         // data to load, TripleStore or Wov)
 
         Map<String, String> properties = item.getResourceProperties();
 
         Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProvider.VAR_PROPERTIES_TITLE, "Properties");
         values.put(XmlTemplateProvider.VAR_PROPERTIES_HREF, item.getHref()
             + Constants.PROPERTIES_URL_PART);
 
         // surrogate item
         String origin = properties.get(PropertyMapKeys.ORIGIN);
         if (origin != null) {
             values.put(XmlTemplateProvider.ORIGIN, XmlTemplateProvider.TRUE);
             String originVersion =
                 properties.get(PropertyMapKeys.ORIGIN_VERSION);
             if (originVersion != null) {
                 origin = origin + ":" + originVersion;
             }
             values.put(XmlTemplateProvider.VAR_ORIGIN_ID, origin);
             values.put(XmlTemplateProvider.VAR_ITEM_ORIGIN_HREF,
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + getOriginId());
             values.put(XmlTemplateProvider.VAR_ITEM_ORIGIN_TITLE,
                 getOriginItem().getTitle());
         }
 
         try {
             String creationDate = item.getCreationDate();
             values
                 .put(XmlTemplateProvider.VAR_ITEM_CREATION_DATE, creationDate);
         }
         catch (TripleStoreSystemException e) {
             throw new ItemNotFoundException(e);
         }
 
         values.put(XmlTemplateProvider.VAR_ITEM_CREATED_BY_TITLE,
             properties.get(PropertyMapKeys.CREATED_BY_TITLE));
         values.put(XmlTemplateProvider.VAR_ITEM_CREATED_BY_HREF,
             de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                 + properties.get(PropertyMapKeys.CREATED_BY_ID));
         values.put(XmlTemplateProvider.VAR_ITEM_CREATED_BY_ID,
             properties.get(PropertyMapKeys.CREATED_BY_ID));
 
         values.put(XmlTemplateProvider.VAR_ITEM_CONTEXT_TITLE,
             properties.get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_TITLE));
         values.put(XmlTemplateProvider.VAR_ITEM_CONTEXT_HREF,
             de.escidoc.core.common.business.Constants.CONTEXT_URL_BASE
                 + properties.get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
         values.put(XmlTemplateProvider.VAR_ITEM_CONTEXT_ID,
             properties.get(PropertyMapKeys.CURRENT_VERSION_CONTEXT_ID));
 
         values
             .put(XmlTemplateProvider.VAR_ITEM_CONTENT_MODEL_TITLE, properties
                 .get(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_TITLE));
         values.put(
             XmlTemplateProvider.VAR_ITEM_CONTENT_MODEL_HREF,
             de.escidoc.core.common.business.Constants.CONTENT_MODEL_URL_BASE
                 + properties
                     .get(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID));
         values.put(XmlTemplateProvider.VAR_ITEM_CONTENT_MODEL_ID,
             properties.get(PropertyMapKeys.CURRENT_VERSION_CONTENT_MODEL_ID));
 
         values.put(XmlTemplateProvider.VAR_ITEM_STATUS, item.getStatus());
         values.put(XmlTemplateProvider.VAR_ITEM_STATUS_COMMENT, XmlUtility
             .escapeForbiddenXmlCharacters(properties
                 .get(PropertyMapKeys.PUBLIC_STATUS_COMMENT)));
 
         if (item.hasObjectPid()) {
             values.put(XmlTemplateProvider.VAR_ITEM_OBJECT_PID,
                 item.getObjectPid());
         }
 
         if (item.isLocked()) {
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_STATUS,
                 de.escidoc.core.common.business.Constants.STATUS_LOCKED);
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_DATE,
                 item.getLockDate());
             String lockOwnerId = item.getLockOwner();
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_OWNER_ID, lockOwnerId);
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_OWNER_HREF,
                 de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                     + lockOwnerId);
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_OWNER_TITLE,
                 item.getLockOwnerTitle());
             // TODO lock-date
         }
         else {
             values.put(XmlTemplateProvider.VAR_ITEM_LOCK_STATUS,
                 de.escidoc.core.common.business.Constants.STATUS_UNLOCKED);
         }
 
         // version
         final StringBuffer versionIdBase =
             new StringBuffer(item.getId()).append(":");
 
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_HREF,
             item.getVersionHref());
         // de.escidoc.core.common.business.Constants.ITEM_URL_BASE
         // + currentVersionId);
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_ID,
             item.getFullId());
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_TITLE,
             "This Version");
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_NUMBER,
             item.getVersionId());
         // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_NUMBER));
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_DATE,
             item.getVersionDate());
         // properties.get(TripleStoreUtility.PROP_VERSION_DATE));
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_STATUS,
             item.getVersionStatus());
         // properties.get(TripleStoreUtility.PROP_CURRENT_VERSION_STATUS));
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_VALID_STATUS,
             properties.get(PropertyMapKeys.CURRENT_VERSION_VALID_STATUS));
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_COMMENT,
             XmlUtility.escapeForbiddenXmlCharacters(properties
                 .get(PropertyMapKeys.CURRENT_VERSION_VERSION_COMMENT)));
 
         values.put(XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_MODIFIED_BY_ID,
             properties.get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
         values.put(
             XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_MODIFIED_BY_TITLE,
             properties.get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_TITLE));
 
         // href is rest only value
         values.put(
             XmlTemplateProvider.VAR_ITEM_CURRENT_VERSION_MODIFIED_BY_HREF,
             de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                 + properties
                     .get(PropertyMapKeys.CURRENT_VERSION_MODIFIED_BY_ID));
 
         // PID ---------------------------------------------------
         if (item.hasVersionPid()) {
             values.put(XmlTemplateProvider.VAR_ITEM_VERSION_PID,
                 item.getVersionPid());
         }
 
         String latestVersionId = item.getLatestVersionId();
         values.put(XmlTemplateProvider.VAR_ITEM_LATEST_VERSION_HREF,
             de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                 + latestVersionId);
         values.put(XmlTemplateProvider.VAR_ITEM_LATEST_VERSION_TITLE,
             "Latest Version");
         values.put(XmlTemplateProvider.VAR_ITEM_LATEST_VERSION_ID,
             latestVersionId);
         values.put(XmlTemplateProvider.VAR_ITEM_LATEST_VERSION_NUMBER,
             properties.get(PropertyMapKeys.LATEST_VERSION_NUMBER));
         values.put(XmlTemplateProvider.VAR_ITEM_LATEST_VERSION_DATE,
             properties.get(PropertyMapKeys.LATEST_VERSION_DATE));
 
         // if item is released -------------------------------------------------
         if (properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER) != null) {
 
             values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_NUMBER,
                 properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER));
 
             // ! changes versionIdBase
             final String latestRevisonId =
                 versionIdBase
                     .append(
                         properties
                             .get(PropertyMapKeys.LATEST_RELEASE_VERSION_NUMBER))
                     .toString();
             values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_HREF,
                 de.escidoc.core.common.business.Constants.ITEM_URL_BASE
                     + latestRevisonId);
             values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_TITLE,
                 "Latest public version");
             values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_ID,
                 latestRevisonId);
             values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_DATE,
                 properties.get(PropertyMapKeys.LATEST_RELEASE_VERSION_DATE));
 
             String latestReleasePid = item.getLatestReleasePid();
             if (latestReleasePid != null) {
                 values.put(XmlTemplateProvider.VAR_ITEM_LATEST_RELEASE_PID,
                     latestReleasePid);
             }
         }
 
         Datastream contentModelSpecific = item.getCts();
         if (contentModelSpecific != null) {
             values.put(XmlTemplateProvider.VAR_ITEM_CONTENT_MODEL_SPECIFIC,
                 contentModelSpecific.toStringUTF8());
         }
         values
             .put(
                 XmlTemplateProvider.VAR_NAMESPACE_PREFIX,
                 de.escidoc.core.common.business.Constants.ITEM_PROPERTIES_NAMESPACE_PREFIX);
         values
             .put(
                 XmlTemplateProvider.VAR_NAMESPACE,
                 de.escidoc.core.common.business.Constants.ITEM_PROPERTIES_NAMESPACE_URI);
 
         return values;
     }
 
     // /**
     // * Get the xml representation of the properties of an item.
     // *
     // * @param isRoot
     // * Indicates if the returned xml representation is part of an
     // * Item representation or returned as root element (the xml
     // * representation of the properties sub resource)
     // * @return
     // * @throws SystemException
     // * Thrown in case of an internal error.
     // */
     // protected StringBuffer getPropertiesXml(final boolean isRoot)
     // throws SystemException {
     //
     // StringBuffer result = null;
     // final String objid = getItem().getId();
     // return result;
     // }
 
     /**
      * Get the content type specific properties.
      * 
      * @return The content type specific properties.
      * @throws EncodingSystemException
      * @throws WebserverSystemException
      */
     protected String getContentTypeSpecificPropertiesXml()
         throws EncodingSystemException, WebserverSystemException {
 
         return getItem().getCts().toStringUTF8();
     }
 
    public Set checkRelations(final String versionDate, final HashMap relations)
         throws TripleStoreSystemException, WebserverSystemException,
         FedoraSystemException, IntegritySystemException {
         Set relationsData;
         relationsData = relations.entrySet();
         Iterator it = relationsData.iterator();
         while (it.hasNext()) {
             Map.Entry relData = (Map.Entry) it.next();
             String id = (String) relData.getKey();
             Relation relation;
             try {
                 relation = new Relation(id);
             }
             catch (ResourceNotFoundException e) {
                 throw new WebserverSystemException("unreachable", e);
             }
             byte[] wov;
             try {
                 wov = relation.getWov().getStream();
             }
             catch (StreamNotFoundException e) {
                 throw new IntegritySystemException("unreachable", e);
             }
             StaxParser sp = new StaxParser();
 
             WovContentRelationsRetrieveHandler wovHandler =
                 new WovContentRelationsRetrieveHandler(sp, versionDate);
             sp.addHandler(wovHandler);
             try {
                 sp.parse(wov);
                 sp.clearHandlerChain();
             }
             catch (Exception e) {
                 throw new WebserverSystemException("unreachable", e);
             }
             String status = wovHandler.getStatus();
             if (status.equals("inactive")) {
                 it.remove();
             }
 
         }
         return relationsData;
     }
 
     /**
      * Get Common values from Item.
      * 
      * @param item
      *            The item.
      * @return Map with common Item values.
      * 
      * @throws WebserverSystemException
      *             Thrown if values extracting failed.
      */
     private Map<String, String> getCommonValues(final Item item)
         throws WebserverSystemException {
 
         Map<String, String> values = new HashMap<String, String>();
 
         values.put(XmlTemplateProvider.OBJID, getItem().getId());
 
         // If later while rendering of md-records the mandatory md-record
         // will be imported from the original item, the
         // title will be overridden in a this map by a title of a original item
         values.put(XmlTemplateProvider.TITLE, getItem().getTitle());
 
         values.put(XmlTemplateProvider.HREF, getItem().getHref());
 
         try {
             values.put(XmlTemplateProvider.VAR_LAST_MODIFICATION_DATE,
                 Iso8601Util.getIso8601(Iso8601Util.parseIso8601(item
                     .getLastModificationDate())));
         }
         catch (ParseException e) {
             try {
                 throw new WebserverSystemException(
                     "Unable to parse last-modification-date '"
                         + item.getLastModificationDate() + "' of item '"
                         + item.getId() + "'!", e);
             }
             catch (FedoraSystemException e1) {
                 throw new WebserverSystemException(e1);
             }
         }
         catch (FedoraSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         values.put("itemNamespacePrefix",
             de.escidoc.core.common.business.Constants.ITEM_NAMESPACE_PREFIX);
         values.put("itemNamespace",
             de.escidoc.core.common.business.Constants.ITEM_NAMESPACE_URI);
         values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
             XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE_PREFIX,
             de.escidoc.core.common.business.Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE,
             de.escidoc.core.common.business.Constants.XLINK_NS_URI);
 
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS_PREFIX,
             de.escidoc.core.common.business.Constants.PROPERTIES_NS_PREFIX);
         values.put(XmlTemplateProvider.ESCIDOC_PROPERTIES_NS,
             de.escidoc.core.common.business.Constants.PROPERTIES_NS_URI);
         values
             .put(
                 XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
                 de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values
             .put(
                 XmlTemplateProvider.ESCIDOC_SREL_NS,
                 de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_URI);
         values.put("versionNamespacePrefix",
             de.escidoc.core.common.business.Constants.VERSION_NS_PREFIX);
         values.put("versionNamespace",
             de.escidoc.core.common.business.Constants.VERSION_NS_URI);
         values.put("releaseNamespacePrefix",
             de.escidoc.core.common.business.Constants.RELEASE_NS_PREFIX);
         values.put("releaseNamespace",
             de.escidoc.core.common.business.Constants.RELEASE_NS_URI);
         values
             .put(
                 "componentsNamespacePrefix",
                 de.escidoc.core.common.business.Constants.COMPONENTS_NAMESPACE_PREFIX);
         values.put("componentsNamespace",
             de.escidoc.core.common.business.Constants.COMPONENTS_NAMESPACE_URI);
 
         values
             .put(
                 XmlTemplateProvider.MD_RECRORDS_NAMESPACE_PREFIX,
                 de.escidoc.core.common.business.Constants.METADATARECORDS_NAMESPACE_PREFIX);
         values
             .put(
                 XmlTemplateProvider.MD_RECORDS_NAMESPACE,
                 de.escidoc.core.common.business.Constants.METADATARECORDS_NAMESPACE_URI);
 
         values
             .put(
                 "contentStreamsNamespacePrefix",
                 de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_PREFIX);
         values
             .put(
                 "contentStreamsNamespace",
                 de.escidoc.core.common.business.Constants.CONTENT_STREAMS_NAMESPACE_URI);
 
         return values;
     }
 
     private Map<String, Object> getRelationValues(final Item item)
         throws FedoraSystemException, IntegritySystemException,
         XmlParserSystemException, WebserverSystemException,
         TripleStoreSystemException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         values
             .put(
                 "contentRelationsNamespacePrefix",
                 de.escidoc.core.common.business.Constants.CONTENT_RELATIONS_NAMESPACE_PREFIX);
         values
             .put(
                 "contentRelationsNamespace",
                 de.escidoc.core.common.business.Constants.CONTENT_RELATIONS_NAMESPACE_URI);
         VelocityXmlCommonRenderer renderer = new VelocityXmlCommonRenderer();
         renderer
             .addRelationsValues(item.getRelations(), item.getHref(), values);
         values.put("contentRelationsTitle", "Relations of Item");
 
         return values;
     }
 
     protected void addParentsNamespaceValues(final Map values)
         throws WebserverSystemException {
         values.put("parentsNamespacePrefix",
             de.escidoc.core.common.business.Constants.PARENTS_NAMESPACE_PREFIX);
         values.put("parentsNamespace",
             de.escidoc.core.common.business.Constants.PARENTS_NAMESPACE_URI);
 
     }
 
     protected void addXlinkValues(final Map values)
         throws WebserverSystemException {
 
         values.put(XmlTemplateProvider.VAR_ESCIDOC_BASE_URL,
             XmlUtility.getEscidocBaseUrl());
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE_PREFIX,
             de.escidoc.core.common.business.Constants.XLINK_NS_PREFIX);
         values.put(XmlTemplateProvider.VAR_XLINK_NAMESPACE,
             de.escidoc.core.common.business.Constants.XLINK_NS_URI);
     }
 
     protected void addStructuralRelationsValues(final Map values)
         throws WebserverSystemException {
         values
             .put(
                 XmlTemplateProvider.ESCIDOC_SREL_NS_PREFIX,
                 de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_PREFIX);
         values
             .put(
                 XmlTemplateProvider.ESCIDOC_SREL_NS,
                 de.escidoc.core.common.business.Constants.STRUCTURAL_RELATIONS_NS_URI);
     }
 
     private Map<String, Object> getResourcesValues(final Item item)
         throws WebserverSystemException {
 
         Map<String, Object> values = new HashMap<String, Object>();
         values.put(XmlTemplateProvider.RESOURCES_TITLE, "Resources");
         values.put("resourcesHref", item.getHref() + "/resources");
         // set in template
         // values.put("versionHistoryHref", item.getHref()
         // + "/resources/version-history");
 
         // add operations from Fedora service definitions
         // FIXME use item properties instead of triplestore util
         try {
             values.put("resourceOperationNames", getTripleStoreUtility()
                 .getMethodNames(item.getId()));
         }
         catch (TripleStoreSystemException e) {
             throw new WebserverSystemException(e);
         }
 
         return values;
     }
 
     /**
      * Get properties values of the Component.
      * 
      * @param component
      *            The Component.
      * @return Map with component properties values.
      * 
      * @throws TripleStoreSystemException
      *             Thrown if TripleStore request failed.
      * @throws WebserverSystemException
      *             Thrown in case of internal failure.
      */
     private Map<String, String> getComponentPropertiesValues(
         final Component component) throws TripleStoreSystemException,
         WebserverSystemException {
 
         Map<String, String> properties = component.getResourceProperties();
         String baseHRef = getItem().getHref() + component.getHrefPart();
         // TODO version
         Map<String, String> values = new HashMap<String, String>();
         values.put(XmlTemplateProvider.VAR_COMPONENT_PROPERTIES_TITLE,
             "Properties");
         values.put(XmlTemplateProvider.VAR_COMPONENT_PROPERTIES_HREF, baseHRef
             + Constants.PROPERTIES_URL_PART);
 
         if ((properties.get(de.escidoc.core.common.business.Constants.DC_NS_URI
             + Elements.ELEMENT_DESCRIPTION) != null
             && component.getMdRecords().containsKey(
                 XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING))
             && (!component
                 .getMdRecord(
                     XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING)
                 .isDeleted())) {
             values
                 .put(
                     XmlTemplateProvider.VAR_COMPONENT_DESCRIPTION,
                     properties
                         .get(de.escidoc.core.common.business.Constants.DC_NS_URI
                             + Elements.ELEMENT_DESCRIPTION));
         }
         values.put(XmlTemplateProvider.VAR_COMPONENT_CREATION_DATE,
             component.getCreationDate());
         values.put(XmlTemplateProvider.VAR_COMPONENT_CREATED_BY_TITLE,
             properties.get(TripleStoreUtility.PROP_CREATED_BY_TITLE));
         values.put(XmlTemplateProvider.VAR_COMPONENT_CREATED_BY_HREF,
             de.escidoc.core.common.business.Constants.USER_ACCOUNT_URL_BASE
                 + properties.get(TripleStoreUtility.PROP_CREATED_BY_ID));
         values.put(XmlTemplateProvider.VAR_COMPONENT_CREATED_BY_ID,
             properties.get(TripleStoreUtility.PROP_CREATED_BY_ID));
 
         if (properties.get(TripleStoreUtility.PROP_VALID_STATUS) != null) {
             values.put(XmlTemplateProvider.VAR_COMPONENT_VALID_STATUS,
                 properties.get(TripleStoreUtility.PROP_VALID_STATUS));
         }
 
         values.put(XmlTemplateProvider.VAR_COMPONENT_VISIBILITY,
             properties.get(TripleStoreUtility.PROP_VISIBILITY));
 
         values.put(XmlTemplateProvider.VAR_COMPONENT_CONTENT_CATEGORY,
             properties.get(TripleStoreUtility.PROP_CONTENT_CATEGORY));
 
         if (properties.get(TripleStoreUtility.PROP_MIME_TYPE) != null) {
             values.put(XmlTemplateProvider.VAR_COMPONENT_MIME_TYPE,
                 properties.get(TripleStoreUtility.PROP_MIME_TYPE));
         }
 
         if ((properties.get(de.escidoc.core.common.business.Constants.DC_NS_URI
             + Elements.ELEMENT_DC_TITLE) != null
             && component.getMdRecords().containsKey(
                 XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING))
             && (!component
                 .getMdRecord(
                     XmlTemplateProvider.DEFAULT_METADATA_FOR_DC_MAPPING)
                 .isDeleted())) {
             values
                 .put(
                     XmlTemplateProvider.VAR_COMPONENT_FILE_NAME,
                     properties
                         .get(de.escidoc.core.common.business.Constants.DC_NS_URI
                             + Elements.ELEMENT_DC_TITLE));
         }
         if (properties.get(TripleStoreUtility.PROP_COMPONENT_PID) != null) {
             values.put(XmlTemplateProvider.VAR_COMPONENT_PID,
                 properties.get(TripleStoreUtility.PROP_COMPONENT_PID));
         }
 
         values.put(XmlTemplateProvider.CONTENT_CHECKSUM_ALGORITHM, properties
             .get(Elements.ELEMENT_COMPONENT_CONTENT_CHECKSUM_ALGORITHM));
         values.put(XmlTemplateProvider.CONTENT_CHECKSUM,
             properties.get(Elements.ELEMENT_COMPONENT_CONTENT_CHECKSUM));
 
         return values;
     }
 }
