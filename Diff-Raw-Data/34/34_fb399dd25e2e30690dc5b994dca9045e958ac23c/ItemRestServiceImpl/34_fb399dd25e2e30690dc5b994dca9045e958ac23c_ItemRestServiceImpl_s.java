 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License
  * for the specific language governing permissions and limitations under the License.
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
 package de.escidoc.core.om.internal;
 
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlSchemaValidationException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingLicenceException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.ComponentNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentStreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.XmlSchemaNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyDeletedException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyWithdrawnException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.NotPublishedException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyVersionException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.ItemRestService;
 import de.escidoc.core.om.service.interfaces.ItemHandlerInterface;
 import org.escidoc.core.domain.result.ResultTO;
 import org.escidoc.core.domain.service.ServiceUtility;
 import org.escidoc.core.domain.taskparam.assignpid.AssignPidTaskParamTO;
 import org.escidoc.core.domain.taskparam.optimisticlocking.OptimisticLockingTaskParamTO;
 import org.escidoc.core.domain.taskparam.relation.RelationTaskParamTO;
 import org.escidoc.core.domain.taskparam.status.StatusTaskParamTO;
 import org.escidoc.core.domain.version.history.VersionHistoryTO;
 import org.escidoc.core.utils.io.IOUtils;
 import org.escidoc.core.utils.io.Stream;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import org.escidoc.core.domain.item.ItemTO;
 import org.escidoc.core.domain.item.ItemPropertiesTO;
 import org.escidoc.core.domain.item.ItemResourcesTO;
 import org.escidoc.core.domain.components.ComponentTO;
 import org.escidoc.core.domain.components.ComponentsTO;
 import org.escidoc.core.domain.components.ComponentPropertiesTO;
 import org.escidoc.core.domain.content.stream.ContentStreamTO;
 import org.escidoc.core.domain.content.stream.ContentStreamsTO;
 import org.escidoc.core.domain.metadatarecords.MdRecordTO;
 import org.escidoc.core.domain.metadatarecords.MdRecordsTO;
 import org.escidoc.core.domain.ou.ParentsTO;
 import org.escidoc.core.domain.relations.RelationsTO;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.rmi.RemoteException;
 
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.core.Response;
 
 /**
  * REST Service Implementation for Item.
  * 
  * @author SWA
  * 
  */
 @Service
 public class ItemRestServiceImpl implements ItemRestService {
 
     private final static Logger LOG = LoggerFactory.getLogger(ItemRestServiceImpl.class);
 
     @Autowired
     @Qualifier("service.ItemHandler")
     private ItemHandlerInterface itemHandler;
 
     @Autowired
     private ServiceUtility serviceUtility;
 
     protected ItemRestServiceImpl() {}
 
     @Override
     public ItemTO create(final ItemTO itemTO) throws MissingContentException, ContextNotFoundException,
         ContentModelNotFoundException, ReadonlyElementViolationException, MissingAttributeValueException,
         MissingElementValueException, ReadonlyAttributeViolationException, AuthenticationException,
         AuthorizationException, XmlCorruptedException, XmlSchemaValidationException, MissingMethodParameterException,
         FileNotFoundException, SystemException, InvalidContentException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, MissingMdRecordException, InvalidStatusException, RemoteException {
 
         return serviceUtility.fromXML(ItemTO.class, this.itemHandler.create(serviceUtility.toXML(itemTO)));
     }
 
     @Override
     public void delete(final String id) throws ItemNotFoundException, AlreadyPublishedException, LockingException,
         AuthenticationException, AuthorizationException, InvalidStatusException, MissingMethodParameterException,
         SystemException, RemoteException {
 
         this.itemHandler.delete(id);
     }
 
     @Override
     public ItemTO retrieve(final String id) throws ItemNotFoundException, ComponentNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         RemoteException {
 
         return serviceUtility.fromXML(ItemTO.class, this.itemHandler.retrieve(id));
     }
 
     @Override
     public ItemTO update(final String id, final ItemTO itemTO) throws ItemNotFoundException, FileNotFoundException,
         InvalidContextException, InvalidStatusException, LockingException, NotPublishedException,
         MissingLicenceException, ComponentNotFoundException, MissingContentException, AuthenticationException,
         AuthorizationException, InvalidXmlException, MissingMethodParameterException, InvalidContentException,
         SystemException, OptimisticLockingException, AlreadyExistsException, ReadonlyViolationException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException, ReadonlyVersionException,
         MissingAttributeValueException, MissingMdRecordException, RemoteException {
 
         return serviceUtility.fromXML(ItemTO.class, this.itemHandler.update(id, serviceUtility.toXML(itemTO)));
     }
 
     @Override
     public ComponentTO createComponent(final String id, final ComponentTO componentTO) throws MissingContentException,
         ItemNotFoundException, ComponentNotFoundException, LockingException, MissingElementValueException,
         AuthenticationException, AuthorizationException, InvalidStatusException, MissingMethodParameterException,
         FileNotFoundException, InvalidXmlException, InvalidContentException, SystemException,
         ReadonlyViolationException, OptimisticLockingException, MissingAttributeValueException, RemoteException {
 
         return serviceUtility.fromXML(ComponentTO.class,
             this.itemHandler.createComponent(id, serviceUtility.toXML(componentTO)));
     }
 
     @Override
     public ComponentTO retrieveComponent(final String id, final String componentId) throws ItemNotFoundException,
         ComponentNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
         SystemException, RemoteException {
 
         return serviceUtility.fromXML(ComponentTO.class, this.itemHandler.retrieveComponent(id, componentId));
     }
 
     @Override
     public MdRecordsTO retrieveComponentMdRecords(final String id, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(MdRecordsTO.class, this.itemHandler.retrieveComponentMdRecords(id, componentId));
     }
 
     @Override
     public MdRecordTO retrieveComponentMdRecord(final String id, final String componentId, String mdRecordId)
         throws ItemNotFoundException, AuthenticationException, AuthorizationException, ComponentNotFoundException,
         MdRecordNotFoundException, MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(MdRecordTO.class,
             this.itemHandler.retrieveComponentMdRecord(id, componentId, mdRecordId));
     }
 
     @Override
     public ComponentTO updateComponent(final String id, final String componentId, ComponentTO componentTO)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, FileNotFoundException,
         MissingAttributeValueException, AuthenticationException, AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
         ReadonlyViolationException, MissingContentException, InvalidContentException, ReadonlyVersionException,
         RemoteException {
 
         return serviceUtility.fromXML(ComponentTO.class,
             this.itemHandler.updateComponent(id, componentId, serviceUtility.toXML(componentTO)));
     }
 
     @Override
     public ComponentsTO retrieveComponents(final String id) throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, ComponentNotFoundException, MissingMethodParameterException, SystemException,
         RemoteException {
 
         return serviceUtility.fromXML(ComponentsTO.class, this.itemHandler.retrieveComponents(id));
     }
 
     @Override
     public ComponentPropertiesTO retrieveComponentProperties(final String id, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(ComponentPropertiesTO.class,
             this.itemHandler.retrieveComponentProperties(id, componentId));
     }
 
     // TODO not supported till version 1.4
     // @Override
     // public MdRecordTO createMdRecord(final String id, final MdRecordTO mdRecordTO) throws ItemNotFoundException,
     // SystemException, InvalidXmlException, LockingException, MissingAttributeValueException, InvalidStatusException,
     // ComponentNotFoundException, MissingMethodParameterException, AuthorizationException, AuthenticationException,
     // RemoteException;
 
     @Override
     public MdRecordTO retrieveMdRecord(final String id, final String mdRecordId) throws ItemNotFoundException,
         MdRecordNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
         SystemException, RemoteException {
 
         return serviceUtility.fromXML(MdRecordTO.class, this.itemHandler.retrieveMdRecord(id, mdRecordId));
     }
 
     @Override
     public Stream retrieveMdRecordContent(final String id, final String mdRecordId) throws ItemNotFoundException,
         MdRecordNotFoundException, AuthenticationException, AuthorizationException, MissingMethodParameterException,
         SystemException {
 
         Stream stream = new Stream();
         try {
             InputStream inputStream =
                 new ByteArrayInputStream(this.itemHandler.retrieveMdRecordContent(id, mdRecordId).getBytes(
                     XmlUtility.CHARACTER_ENCODING));
             IOUtils.copy(inputStream, stream);
         }
         catch (IOException e) {
             LOG.error("Failed to copy stream", e);
             throw new SystemException(e);
         }
         return stream;
     }
 
     @Override
     public Stream retrieveDcRecordContent(final String id) throws ItemNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, MdRecordNotFoundException,
         SystemException {
 
         Stream stream = new Stream();
         try {
             InputStream inputStream =
                 new ByteArrayInputStream(this.itemHandler.retrieveDcRecordContent(id).getBytes(
                     XmlUtility.CHARACTER_ENCODING));
             IOUtils.copy(inputStream, stream);
         }
         catch (IOException e) {
             LOG.error("Failed to copy stream", e);
             throw new SystemException(e);
         }
         return stream;
     }
 
     @Override
     public MdRecordTO updateMdRecord(final String id, final String mdRecordId, MdRecordTO mdRecordTO)
         throws ItemNotFoundException, XmlSchemaNotFoundException, LockingException, InvalidContentException,
         MdRecordNotFoundException, AuthenticationException, AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidXmlException,
         ReadonlyViolationException, ReadonlyVersionException, RemoteException {
 
         return serviceUtility.fromXML(MdRecordTO.class,
             this.itemHandler.updateMdRecord(id, mdRecordId, serviceUtility.toXML(mdRecordTO)));
     }
 
     @Override
     public MdRecordsTO retrieveMdRecords(final String id) throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(MdRecordsTO.class, this.itemHandler.retrieveMdRecords(id));
     }
 
     @Override
     public ContentStreamsTO retrieveContentStreams(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         RemoteException {
 
         return serviceUtility.fromXML(ContentStreamsTO.class, this.itemHandler.retrieveContentStreams(id));
     }
 
     @Override
     public ContentStreamTO retrieveContentStream(final String id, final String name) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         ContentStreamNotFoundException, RemoteException {
 
         return serviceUtility.fromXML(ContentStreamTO.class, this.itemHandler.retrieveContentStream(id, name));
     }
 
     @Override
     public ItemPropertiesTO retrieveProperties(final String id) throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(ItemPropertiesTO.class, this.itemHandler.retrieveProperties(id));
     }
 
     @Override
     public VersionHistoryTO retrieveVersionHistory(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         RemoteException {
 
         return serviceUtility.fromXML(VersionHistoryTO.class, this.itemHandler.retrieveVersionHistory(id));
     }
 
     @Override
     public ParentsTO retrieveParents(final String id) throws ItemNotFoundException, MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(ParentsTO.class, this.itemHandler.retrieveParents(id));
     }
 
     @Override
     public RelationsTO retrieveRelations(final String id) throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, RemoteException {
 
         return serviceUtility.fromXML(RelationsTO.class, this.itemHandler.retrieveRelations(id));
     }
 
     @Override
     public ItemResourcesTO retrieveResources(final String id) throws ItemNotFoundException,
         MissingMethodParameterException, AuthenticationException, AuthorizationException, SystemException {
 
         return serviceUtility.fromXML(ItemResourcesTO.class, this.itemHandler.retrieveResources(id));
     }
 
     @Override
     public Stream retrieveResource(final String id, final String resourceName) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         OperationNotFoundException {
 
         Stream stream = new Stream();
         try {
             InputStream inputStream = this.itemHandler.retrieveResource(id, resourceName).getContent();
             IOUtils.copy(inputStream, stream);
         }
         catch (IOException e) {
             LOG.error("Failed to copy stream", e);
             throw new SystemException(e);
         }
         return stream;
     }
 
     @Override
     public ResultTO release(final String id, final StatusTaskParamTO statusTaskParamTO) throws ItemNotFoundException,
         ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.release(id, serviceUtility.toXML(statusTaskParamTO)));
     }
 
     @Override
     public ResultTO submit(final String id, final StatusTaskParamTO statusTaskParamTO) throws ItemNotFoundException,
         ComponentNotFoundException, LockingException, InvalidStatusException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, InvalidXmlException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.submit(id, serviceUtility.toXML(statusTaskParamTO)));
     }
 
     @Override
     public ResultTO revise(final String id, final StatusTaskParamTO statusTaskParamTO) throws AuthenticationException,
         AuthorizationException, ItemNotFoundException, ComponentNotFoundException, LockingException,
         InvalidStatusException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException, InvalidContentException, XmlCorruptedException,
         RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.revise(id, serviceUtility.toXML(statusTaskParamTO)));
     }
 
     @Override
     public ResultTO withdraw(final String id, final StatusTaskParamTO statusTaskParamTO) throws ItemNotFoundException,
         ComponentNotFoundException, NotPublishedException, LockingException, AlreadyWithdrawnException,
         AuthenticationException, AuthorizationException, InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, ReadonlyViolationException, ReadonlyVersionException,
         InvalidXmlException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.withdraw(id, serviceUtility.toXML(statusTaskParamTO)));
     }
 
     @Override
     public ResultTO lock(final String id, final OptimisticLockingTaskParamTO optimisticLockingTaskParamTO)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, InvalidContentException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidXmlException, InvalidStatusException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.lock(id, serviceUtility.toXML(optimisticLockingTaskParamTO)));
     }
 
     @Override
     public ResultTO unlock(final String id, final OptimisticLockingTaskParamTO optimisticLockingTaskParamTO)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidXmlException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.unlock(id, serviceUtility.toXML(optimisticLockingTaskParamTO)));
     }
 
     @Override
     public void deleteComponent(final String id, final String componentId) throws ItemNotFoundException,
         ComponentNotFoundException, LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException, InvalidStatusException, RemoteException {
 
         this.itemHandler.deleteComponent(id, componentId);
     }
 
     @Override
     public ResultTO assignVersionPid(final String id, final AssignPidTaskParamTO assignPidTaskParamTO)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidStatusException, XmlCorruptedException, ReadonlyVersionException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.assignVersionPid(id, serviceUtility.toXML(assignPidTaskParamTO)));
     }
 
     @Override
     public ResultTO assignObjectPid(final String id, final AssignPidTaskParamTO assignPidTaskParamTO)
         throws ItemNotFoundException, ComponentNotFoundException, LockingException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, OptimisticLockingException,
         InvalidStatusException, XmlCorruptedException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.assignObjectPid(id, serviceUtility.toXML(assignPidTaskParamTO)));
     }
 
     @Override
     public ResultTO assignContentPid(
         final String id, final String componentId, AssignPidTaskParamTO assignPidTaskParamTO)
         throws ItemNotFoundException, LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException, OptimisticLockingException, InvalidStatusException,
         ComponentNotFoundException, XmlCorruptedException, ReadonlyVersionException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class,
             this.itemHandler.assignContentPid(id, componentId, serviceUtility.toXML(assignPidTaskParamTO)));
     }
 
     @Override
     public ResultTO addContentRelations(final String id, final RelationTaskParamTO relationTaskParamTO)
         throws SystemException, ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException,
         ReferencedResourceNotFoundException, RelationPredicateNotFoundException, AlreadyExistsException,
         InvalidStatusException, InvalidXmlException, MissingElementValueException, LockingException,
         ReadonlyViolationException, InvalidContentException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, ReadonlyVersionException, RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class, this.itemHandler.retrieve(id));
     }
 
     @Override
     public ResultTO removeContentRelations(final String id, final RelationTaskParamTO relationTaskParamTO)
         throws SystemException, ItemNotFoundException, ComponentNotFoundException, OptimisticLockingException,
         InvalidStatusException, MissingElementValueException, InvalidContentException, InvalidXmlException,
         ContentRelationNotFoundException, AlreadyDeletedException, LockingException, ReadonlyViolationException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, ReadonlyVersionException,
         RemoteException {
 
         return serviceUtility.fromXML(ResultTO.class, this.itemHandler.retrieve(id));
     }
 
     @Override
    public Response retrieveContent(final String id, final String contentId) throws AuthenticationException,
         AuthorizationException, MissingMethodParameterException, SystemException, InvalidStatusException,
         ResourceNotFoundException {
        return serviceUtility.toResponse(this.itemHandler.retrieveContent(id, contentId));
     }
 }
