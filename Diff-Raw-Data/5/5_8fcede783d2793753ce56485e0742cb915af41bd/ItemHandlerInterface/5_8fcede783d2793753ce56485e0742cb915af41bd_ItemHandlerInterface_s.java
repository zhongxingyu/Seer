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
 package de.escidoc.core.om.service.interfaces;
 
 import java.util.Map;
 
 import de.escidoc.core.common.annotation.Validate;
 import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContextException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidSearchQueryException;
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
 import de.escidoc.core.common.exceptions.application.notfound.ContainerNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentStreamNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.FileNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ItemNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.MdRecordNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OperationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
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
 
 /**
  * Interface of an Item handler.
  * 
  * @author TTE
  */
 public interface ItemHandlerInterface {
 
     /**
      * Retrieves a list of complete Items applying filters.<br/>
      * <b>Prerequisites:</b><br/>
      * At least one filter containing a value must be specified.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>Check weather all filter names are valid.</li>
      * <li>The Items are accessed using the provided filters.</li>
      * <li>The XML representation to be returned for all Item will not contain
      * any binary content but references to them.</li>
      * <li>The XML representation of the list of all Items the current user is
      * allowed to see is returned as output.</li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param filter
      *            Simple XML containing the filter definition. See functional
      *            specification.
      * @return Returns the XML representation of found Items with a surrounding
      *         list element.
      * 
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a SQL query
      * @throws InvalidXmlException
      *             If the given XML is not valid.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @deprecated replaced by {@link #retrieveItems(java.util.Map)}
      */
     @Validate(param = 0, resolver = "getFilterSchemaLocation")
     @Deprecated
     String retrieveItems(final String filter)
         throws MissingMethodParameterException, InvalidSearchQueryException,
         InvalidXmlException, SystemException;
 
     /**
      * Retrieves a list of complete Items applying filters.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>Check weather all filter names are valid.</li>
      * <li>The Items are accessed using the provided filters.</li>
      * <li>The XML representation to be returned for all Item will not contain
      * any binary content but references to them.</li>
      * <li>The XML representation of the list of all Items the current user is
      * allowed to see is returned as output corresponding to the SRU/SRW schema.
      * </li>
      * </ul>
      * <br/>
      * See chapter "Filters" for detailed information about filter definitions.
      * 
      * @param filter
      *            map of key/value pairs containing the filter definition. See
      *            functional specification.
      * 
      * @return Returns the XML representation of found Items corresponding to
      *         the SRW schema.
      * 
      * @throws MissingMethodParameterException
      *             If the parameter filter is not given.
      * @throws InvalidSearchQueryException
      *             thrown if the given search query could not be translated into
      *             a CQL query
      * @throws SystemException
      *             If an error occurs.
      * 
      */
     String retrieveItems(final Map<String, String[]> filter)
         throws MissingMethodParameterException, InvalidSearchQueryException,
         SystemException;
 
     /**
      * Create an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of an Item.</li>
      * <li>It's checked weather the context id is provided. In a REST case it's
      * checked weather REST-URL of a context is correct.</li>
      * <li>It's checked weather the content-model id is provided. In a REST case
      * it's checked weather REST-URL of a content-model is correct</li>
      * <li>It's checked weather the context to the provided id exists.</li>
      * <li>It's checked weather the content-model to the provided id exists.</li>
      * <li>If a provided Item representation is a surrogate Item representation
      * (contains a property'origin') it's checked weather the referenced origin
      * Item version exists and is in a state 'released'. Then it is checked if
      * the creator has privileges to access the referenced origin Item version
      * and if the origin Item itself is not a surrogate Item.</li>
      * <li>If a "relations" section is set, it's checked weather resources to
      * all provided relations targets exist and whether used relations are part
      * of the related ontology.</li>
      * <li>Linked files are downloaded <b> or </b>extracted if inline delivered
      * and the Components are created.</li>
      * <li>The public-status of the Item is set to "pending".</li>
      * <li>The version 1 of the Item is created.</li>
      * <li>The XML input data is updated and some new data is added (see Chapter
      * 4)</li>
      * <li>The XML representation of the Item corresponding to the XML schema is
      * returned as output.</li>
      * </ul>
      * 
      * The Persistent Identifier (PID) of a resource can be given with create.
      * Later, one of the assignPid methods must be used.<br/>
      * 
      * @param xmlData
      *            The XML representation of the Item to be created corresponding
      *            to XML schema "item.xsd".
      * @return The XML representation of the created Item corresponding to XML
      *         schema "item.xsd".
      * 
      * @throws ContextNotFoundException
      *             Thrown if the Context specified in the provided data cannot
      *             be found.
      * @throws ContentModelNotFoundException
      *             Thrown if the content type specified in the provided data
      *             cannot be found.
      * @throws ReferencedResourceNotFoundException
      *             If a resource referred from the provided data could not be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws XmlCorruptedException
      *             Thrown if provided data is corrupted.
      * @throws XmlSchemaValidationException
      *             Thrown if the schema validation of the provided data fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws FileNotFoundException
      *             Thrown if a file cannot be found.
      * @throws InvalidContentException
      *             Thrown if the provided XML data is not valid for the creation
      *             of the resource.
      * @throws InvalidStatusException
      *             Thrown if the status of the specified Context is not valid
      *             for executing the action.
      * @throws MissingMdRecordException
      *             Thrown if the required metadata record (with name 'escidoc')
      *             is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws MissingContentException
      *             If some mandatory content is missing.
      * @throws ReadonlyElementViolationException
      *             If a read-only element is set.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws ReadonlyAttributeViolationException
      *             If a read-only attribute is set.
      * @throws RelationPredicateNotFoundException
      *             If the predicate of a given relation is unknown.
      * @throws ReferencedResourceNotFoundException
      *             If a resource referred from the provided data could not be
      *             found.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      */
     @Validate(param = 0, resolver = "getItemSchemaLocation")
     String create(String xmlData) throws MissingContentException,
         ContextNotFoundException, ContentModelNotFoundException,
         ReadonlyElementViolationException, MissingElementValueException,
         ReadonlyAttributeViolationException, AuthenticationException,
         AuthorizationException, XmlCorruptedException,
         XmlSchemaValidationException, MissingMethodParameterException,
         FileNotFoundException, SystemException, InvalidContentException,
         ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, MissingAttributeValueException,
         MissingMdRecordException, InvalidStatusException;
 
     /**
      * Delete an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Item has to be in public-status "pending" or "in-revision" and must
      * be unlocked, otherwise the removing of the Item will fail.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>All content relations pointing from and to that Item will be deleted.
      * </li>
      * <li>The Item will be deleted from IR.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item to be deleted.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AlreadyPublishedException
      *             Thrown if the Item with the specified id has been published.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws InvalidStatusException
      *             Thrown if the Items status is not valid for executing the
      *             action.
      * @throws MissingMethodParameterException
      *             If a mandatory element value is missing.
      * @throws SystemException
      *             If an error occurs.
      */
     void delete(String id) throws ItemNotFoundException,
         AlreadyPublishedException, LockingException, AuthenticationException,
         AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieve an Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided reference.</li>
      * <li>The XML representation to be returned for that Item will not contain
      * any binary content but references to them.</li>
      * <li>The XML representation of the Item corresponding to XML schema is
      * returned as output.</li>
      * </ul>
      * 
      * The binary content of an Item is not included but referenced from the
      * Item representation.
      * 
      * @param id
      *            The id of the Item to be retrieved. In order to retrieve a
      *            specific version of an Item the id must be suffixed with a
      *            colon (':') and the version number.
      * @return The XML representation of the retrieved Item corresponding to
      *         XML-schema "item.xsd".
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component of the Item cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If a mandatory element value is missing.
      * @throws SystemException
      *             If an error occurs.
      */
     String retrieve(String id) throws ItemNotFoundException,
         ComponentNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException;
 
     /**
      * Update an Item<br/>
      * <b>Prerequisites:</b> <br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * The Item must exist.<br/>
      * The the public-status is not "withdrawn".<br/>
      * The Item is not locked by another user.<br/>
      * Optimistic Locking criteria of the Item is checked.<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * Only the latest version can be used here.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of an Item.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>It is checked whether used relations are part of the related
      * ontologies.</li>
      * <li>A relations section contains a list of "relation" elements with
      * existing relations data of the provided Item, which should remain after
      * update and a new relations data. The framework will remove all existing
      * relations of the provided Item, which are not on the list. It is checked,
      * if provided relation targets and provided relation predicates exist. The
      * attribute "xlink:href" of the "relation" element is set to a REST-url and
      * respectively the attribute "objid" is set to id of the target. A target
      * id may not contain a version number.</li>
      * <li>A Components section contains the Components of the provided Item,
      * which should remain after update and new Components. The framework will
      * remove all existing Components of the specified Item, which are not in
      * this section inside the provided XML data.</li>
      * <li>If new Components are specified the linked files are downloaded <b>
      * or </b>extracted if inline delivered this data is used and the new
      * Components are created.</li>
      * <li>For existing Components if new references are specified the linked
      * files are downloaded <b> or </b>extracted from the XML representation
      * (inline delivered) and the Components are created.</li>
      * 
      * <li>Differences between modifiable elements in the delivered XML data and
      * the XML representation of the currently stored Item are taken to modify
      * the Item in the system.</li>
      * 
      * <li>If the Item is modified a new version of the Item is created.</li>
      * <li>If the status of the latest version is "released" a new version is
      * created and gets the status "pending" otherwise a new version is created
      * with the same version status as before. This also applies to the
      * public-status till it is once set to "released".</li>
      * <li>The XML input data is updated and some new data is added (see Chapter
      * 4)</li>
      * <li>The XML representation of the Item corresponding to XML schema is
      * returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item to be updated.
      * @param xmlData
      *            The XML representation of the Item to be updated corresponding
      *            to XML-schema "item.xsd".
      * @return The XML representation of the updated Item corresponding to
      *         XML-schema "item.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws FileNotFoundException
      *             Thrown if a file cannot be found.
      * @throws InvalidContextException
      *             Thrown if the content is invalid.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws NotPublishedException
      *             Thrown if the status shall be changed to withdrawn but the
      *             Item has not been published.
      * @throws MissingLicenceException
      *             Thrown if the status shall be changed to published but a
      *             license is missing.
      * @throws ComponentNotFoundException
      *             Thrown if a Component specified in the XML data can not be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws MissingMethodParameterException
      *             If one of the parameters Item ID or data is not provided.
      * @throws MissingContentException
      *             If some mandatory content is missing.
      * @throws MissingAttributeValueException
      *             If a mandatory attribute value is missing.
      * @throws MissingMdRecordException
      *             Thrown if the required metadata record (with name 'escidoc')
      *             is not provided.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the specified id is not the one of the latest version of
      *             the Item.
      * @throws AlreadyExistsException
      *             If a subresource to create already exists.
      * @throws ReferencedResourceNotFoundException
      *             If a resource referred from the provided data could not be
      *             found.
      * @throws RelationPredicateNotFoundException
      *             If the predicate of a given relation is unknown.
      */
     @Validate(param = 1, resolver = "getItemSchemaLocation")
     String update(String id, String xmlData) throws ItemNotFoundException,
         FileNotFoundException, InvalidContextException, InvalidStatusException,
         LockingException, NotPublishedException, MissingLicenceException,
         ComponentNotFoundException, MissingContentException,
         MissingAttributeValueException, AuthenticationException,
         AuthorizationException, InvalidXmlException,
         MissingMethodParameterException, InvalidContentException,
         SystemException, OptimisticLockingException, AlreadyExistsException,
         ReadonlyViolationException, ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, ReadonlyVersionException,
         MissingMdRecordException;
 
     //
     // Subresources
     //
 
     //
     // Subresource - Component
     //
 
     /**
      * Add a new Component to an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * The Item must exist<br/>
      * The the public-status is not "withdrawn".<br/>
      * The Item is not locked by another user.<br/>
      * The Item must not be a surrogate Item.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML-Schema of an Item.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>If there is a link to a file it is downloaded.</li>
      * <li>The Component is created and all Component specific properties are
      * added.</li>
      * <li>A new Version of the Item is created.</li>
      * <li>If the version-status of the Item is "released" the version-status of
      * the new version is set to "pending".</li>
      * <li>The XML representation of the Component corresponding to XML schema
      * is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item. The Component will be added to this Item.
      * @param xmlData
      *            The XML representation of the Component to be added
      *            corresponding to XML-schema "components.xsd".
      * @return The XML representation of the created Component corresponding to
      *         XML-schema "components.xsd".
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws ComponentNotFoundException
      *             Thrown if Component cannot be found.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *             If one of the parameters Item ID or data is not provided.
      * @throws MissingContentException
      *             If some mandatory content is missing.
      * @throws FileNotFoundException
      *             thrown if a file cannot be found.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws SystemException
      *             If an error occurs.
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws MissingElementValueException
      *             If a mandatory element value is missing.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * 
      * @escidoc_core.visible false
      */
     @Validate(param = 0, resolver = "getItemSchemaLocation")
     String createComponent(final String id, final String xmlData)
         throws MissingContentException, ItemNotFoundException,
         ComponentNotFoundException, LockingException,
         MissingElementValueException, AuthenticationException,
         AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, FileNotFoundException,
         InvalidXmlException, InvalidContentException,
         ReadonlyViolationException, SystemException,
         OptimisticLockingException, MissingAttributeValueException;
 
     /**
      * Delete a Component of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * Only Components of Items in public-status "pending" or "submitted" can be
      * deleted.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided reference.</li>
      * <li>The Component will be deleted from IR.</li>
      * </ul>
      * 
      * @param itemId
      *            The Item id.
      * @param componentId
      *            The id of the Component to be deleted.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If one of the parameters Item ID or Component ID is not
      *             provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.visible false
      */
     void deleteComponent(final String itemId, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         InvalidStatusException;
 
     /**
      * Retrieve a Component of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>The XML data for that Component is delivered (no binary content is
      * included, instead a link to the subresource "content" is added.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param componentId
      *            The id of the Component to be retrieved.
      * @return The XML representation of the related Component corresponding to
      *         XML schema "components.xsd"..
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveComponent(final String id, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Update a Component of an Item<br/>
      * <b>Prerequisites:</b><br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * The the public-status is not "withdrawn".<br/>
      * The Item is not locked by another user.<br/>
      * Optimistic Locking criteria of the Item is checked.<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * Only the latest version can be used here.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The XML data is validated against the XML schema of an Item.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The technical Metadata Record is completed with additional
      * attributes.</li>
      * <li>Linked file is downloaded if a new link is delivered.</li>
      * <li>The Component is updated</li>
      * <li>If changed, the related attributes are updated.</li>
      * 
      * <li>Differences between modifiable elements in the delivered XML data and
      * the XML representation of the currently stored Item are taken to modify
      * the Item in the system.</li>
      * 
      * <li>A new Version of the Item is created.</li>
      * <li>In case of status "released" of the Item, the status of the new
      * version is set to "in revision"</li>
      * <li>Timestamp of the latest modification of the Item in the system is
      * updated to the current time.</li>
      * <li>The XML data is created with version-date, version and all other
      * attributes which have been created or modified by the framework for that
      * Component. Link to binary data stream is changed according the new
      * location.</li>
      * <li>The XML data for the Component is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param componentId
      *            The id of the Component to be updated.
      * @param xmlData
      *            The XML representation of the Component to be updated
      *            corresponding to XML schema "components.xsd".
      * @return The XML representation of the updated Component corresponding to
      *         XML schema "components.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws FileNotFoundException
      *             thrown if a file cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws MissingContentException
      *             If some mandatory content is missing.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * 
      * @escidoc_core.visible false
      */
     @Validate(param = 2, resolver = "getItemSchemaLocation")
     String updateComponent(
         final String id, final String componentId, final String xmlData)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, FileNotFoundException,
         MissingAttributeValueException, AuthenticationException,
         AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidXmlException,
         ReadonlyViolationException, MissingContentException,
         InvalidContentException, ReadonlyVersionException;
 
     //
     // Subresource - Components
     //
 
     /**
      * Retrieve all Components of an Item. <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The Components for the specified Item are retrieved.</li>
      * <li>The XML data for that list of Components is created (no binary data
      * stream is added, instead a link to the subresource "content" is added.</li>
      * <li>The XML representation of the subresource Components of the Item
      * corresponding to XML-schema is returned as output.</li>
      * </ul>
      * 
      * 
      * @param id
      *            The id of the resource.
      * @return Returns the XML representation of the subresource Components of
      *         the Item.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveComponents(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         ComponentNotFoundException, MissingMethodParameterException,
         SystemException;
 
     /**
      * 
      * Retrieve the properties of a Component of an Item<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * If the Item is a surrogate Item, the user has to have privileges to
      * access the origin Item, referenced by the surrogate Item.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>The properties are retrieved from the Component.</li>
      * <li>The properties are returned.</li>
      * </ul>
      * 
      * @param itemId
      *            The id of the Item.
      * @param componentId
      *            The id of the Component.
      * @return The properties of the Component.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveComponentProperties(
         final String itemId, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieve all metadata records of a Component.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item and the Component are accessed using the provided Ids.</li>
      * <li>The XML data for the set of Metadata Records is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param itemId
      *            The id of the Item.
      * @param componentId
      *            The id of the Component.
      * @return The XML representation of the metadata records of the Component
      *         corresponding to XML-schema "md-records.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveComponentMdRecords(
         final String itemId, final String componentId)
         throws ItemNotFoundException, ComponentNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieves a metadata record of a Component.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * The Metadata Record must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item and the Component are accessed using the provided Ids.</li>
      * <li>Retrieve the requested matadata record of the Component.</li>
      * <li>The XML data for metadata record is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param componentId
      *            The id of the Component.
      * @param mdRecordId
      *            The id of the metdata record.
      * @return The XML representation of the Metadata Record of the Component
      *         corresponding to XML-schema "md-records.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component with the specified id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the Item does not have the specified metadata
      *             record.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveComponentMdRecord(
         final String id, final String componentId, final String mdRecordId)
         throws ItemNotFoundException, ComponentNotFoundException,
         MdRecordNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException;
 
     //
     // Content
     //
 
     /**
      * Retrieve the binary content of a Component of an Item.<br>
      * This subresource provides access to the binary content of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * The Content must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>The binary content is retrieved from the Component.</li>
      * <li>The binary content is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param contentId
      *            The id of the Component the binary content should be retrieved
      *            from.
      * @return The binary content.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if the Component containing the content cannot be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.available REST
      */
     EscidocBinaryContent retrieveContent(final String id, final String contentId)
         throws ItemNotFoundException, ComponentNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         InvalidStatusException, ResourceNotFoundException;
 
     /**
      * Retrieve the binary content of a ContentStream.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Content must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The binary content is retrieved.</li>
      * <li>The binary content is returned.</li>
      * </ul>
      * 
      * @param itemId
      *            The id of the Item.
      * @param name
      *            The name of the ContentStream.
      * @return The binary content.
      * 
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ContentStreamNotFoundException
      *             Thrown if the Component containing the content cannot be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @escidoc_core.available REST
      */
     EscidocBinaryContent retrieveContentStreamContent(
         final String itemId, final String name) throws AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         ItemNotFoundException, SystemException, ContentStreamNotFoundException;
 
     /**
      * Retrieve the transformed binary content of a Component of an Item.<br>
      * The transformer of the binary content is an additional service. This
      * subresource provides access to the binary content of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * The content must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>The binary content is retrieved from the Component.</li>
      * <li>The binary content is transformed through the transformation service
      * (digilib).<br/>
      * Parameters for transformation are taken from the HTTP GET parameter.</li>
      * <li>The binary content is returned.</li>
      * </ul>
      * <br/>
      * Current Supported Transformation Services:<br/>
      * <ul>
      * <li>Digilib Scaler<br/>
      * /digilib?param1=value1&param2=...</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param contentId
      *            The id of the Component the binary content should be retrieved
      *            from.
      * @param transformer
      *            The transformation service
      * @param param
      *            The parameter for the associated transformer (GET parameter)
      * @return The transformed binary content.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if the Component containing the content cannot be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID or parameter is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.available REST
      */
     EscidocBinaryContent retrieveContent(
         final String id, final String contentId, final String transformer,
         final String param) throws ItemNotFoundException,
         ComponentNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException, InvalidStatusException;
 
     /**
      * Get a redirect to a content service. For digilib is this a redirect to
      * the digilib user front-end.
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The Component must exist<br/>
      * The content must exist<br/>
      * The contentService is configured<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided ID.</li>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>The request is forwarded to the content service. Additional parameter
      * are added to the forward, if the service requires.</li>
      * </ul>
      * <br/>
      * Current Supported Content Services:<br/>
      * <ul>
      * <li>Digimage (Digilib Client)<br/>
      * ../digilib/digimage</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param componentId
      *            The id of the Component the binary content should be retrieved
      *            from.
      * @param transformer
      *            The transformation service
      * @param clientService
      *            The name of the requested content service.
      * @return The redirect to the external content service.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if the Component containing the content cannot be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID or parameter is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.available REST
      * 
      */
     EscidocServiceRedirectInterface redirectContentService(
         final String id, final String componentId, final String transformer,
         final String clientService) throws ItemNotFoundException,
         ComponentNotFoundException, MissingMethodParameterException,
         SystemException, InvalidStatusException, AuthorizationException;
 
     //
     // Subresource - metadata record
     //
 
     /**
      * Add a Metadata Record to an Item.<br/>
      * 
      * Deprecated because of inconsistent naming. Use createMdRecord instead of.
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Add new metadata record to the Item.</li>
      * <li>A new Version of the Item is created.</li>
      * <li>If the status of the latest version is "released" a new version is
      * created and gets the status "pending" otherwise a new version is created
      * with the same version status as before. This also applies to the
      * public-status till it is once set to "released".</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param xmlData
      *            The XML representation of the metadata record of the Item
      *            corresponding to XML schema "md-records.xsd".
      * 
      * @return Returns the value of the subresource.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component can not be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws MissingMethodParameterException
      *             If the ID or data is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.visible false
      */
     @Deprecated
     @Validate(param = 1, resolver = "getItemSchemaLocation")
     String createMetadataRecord(final String id, final String xmlData)
         throws ItemNotFoundException, ComponentNotFoundException,
         XmlSchemaNotFoundException, LockingException,
         MissingAttributeValueException, AuthenticationException,
         AuthorizationException, InvalidStatusException,
         MissingMethodParameterException, SystemException, InvalidXmlException;
 
     /**
      * Add a Metadata Record to an Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Add new metadata record to the Item.</li>
      * <li>A new Version of the Item is created.</li>
      * <li>If the status of the latest version is "released" a new version is
      * created and gets the status "pending" otherwise a new version is created
      * with the same version status as before. This also applies to the
      * public-status till it is once set to "released".</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param xmlData
      *            The XML representation of the metadata record of the Item
      *            corresponding to XML schema "md-records.xsd".
      * 
      * @return Returns the value of the subresource.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a Component can not be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingAttributeValueException
      *             It a mandatory attribute value is missing.
      * @throws MissingMethodParameterException
      *             If the ID or data is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * 
      * @escidoc_core.visible false
      */
     @Validate(param = 1, resolver = "getItemSchemaLocation")
     String createMdRecord(final String id, final String xmlData)
         throws ItemNotFoundException, SystemException, InvalidXmlException,
         LockingException, MissingAttributeValueException,
         InvalidStatusException, ComponentNotFoundException,
         AuthorizationException, AuthenticationException,
         MissingMethodParameterException;
 
     /**
      * Retrieves a metadata record of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The metadata record must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Retrieve the requested matadata record of the Item.</li>
      * <li>The XML data for metadata record is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param mdRecordId
      *            The id of the metdata record.
      * @return The XML representation of the metadata record of the Item
      *         corresponding to XML-schema "md-records.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the Item does not have the specified metadata
      *             record.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveMdRecord(final String id, final String mdRecordId)
         throws ItemNotFoundException, MdRecordNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieves the specified metadata of an Item. In oposite to
      * <code>retrieveMdRecord</code> there is no surrounding 'md-record' element.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The metadata record must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Retrieve the requested matadata record of the Item.</li>
      * <li>The content is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param mdRecordId
      *            The id of the Item.
      * @return The metadata for the Item.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the Item does not have the specified metadata
      *             record.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @escidoc_core.visible false
      */
     String retrieveMdRecordContent(final String id, final String mdRecordId)
         throws ItemNotFoundException, MdRecordNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieves the Dublin Core metadata of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The DC metadata record must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Retrieve the requested matadata record of the Item.</li>
      * <li>The content is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @return The DC metadata for the Item.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws MdRecordNotFoundException
      *             Thrown if the Item does not have the specified metadata
      *             record.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @escidoc_core.visible false
      */
     String retrieveDcRecordContent(final String id)
         throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         MdRecordNotFoundException, SystemException;
 
     /**
      * Update a metadata record of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The provided XML data in the body is only accepted if the size is less
      * than ESCIDOC_MAX_XML_SIZE.<br/>
      * The Item must exist.<br/>
      * The metadata mecord must exist.<br/>
      * The the public-status is not "withdrawn".<br/>
      * The Item is not locked by another user.<br/>
      * Optimistic Locking criteria of the Item is checked.<br/>
      * Only the latest version can be used here.<br/>
      * See chapter 4 for detailed information about input and output data
      * elements<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Differences between modifiable elements in the delivered XML data and
      * the XML representation of the currently stored metadata record are taken
      * to modify the metadata record in the system.</li>
      ** 
      * <li>If the metadata record is modified a new version of the Item is
      * created.</li>
      * <li>If the status of the latest version is "released" a new version is
      * created and gets the status "pending" otherwise a new version is created
      * with the same version status as before. This also applies to the
      * public-status till it is once set to "released".</li>
      * <li>The XML input data is updated and some new data is added (see Chapter
      * 4)</li>
      * <li>The XML representation of the metadata record corresponding to XML
      * schema is returned as output.</li>
      * </ul>
      * 
      * 
      * @param id
      *            The id of the Item.
      * @param mdRecordId
      *            The id of the Metadata Record.
      * @param xmlData
      *            The XML representation of the metadata record to be updated
      *            corresponding to XML-schema "md-records.xsd".
      * @return Returns the value of the metadata record.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws XmlSchemaNotFoundException
      *             Thrown if the specified schema cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws MdRecordNotFoundException
      *             If the specified metadata record could not be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *             If at least one ID or parameter is not provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidXmlException
      *             If the provided data is not valid XML.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * 
      * @escidoc_core.visible false
      */
     @Validate(param = 2, resolver = "getItemSchemaLocation")
     String updateMdRecord(
         final String id, final String mdRecordId, final String xmlData)
         throws ItemNotFoundException, XmlSchemaNotFoundException,
         LockingException, InvalidContentException, MdRecordNotFoundException,
         AuthenticationException, AuthorizationException,
         InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, InvalidXmlException,
         ReadonlyViolationException, ReadonlyVersionException;
 
     //
     // Subresource - metadata records
     //
 
     /**
      * Retrieve all Metadata Records of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>The XML data for the set of Metadata Records is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @return The XML representation of the Metadata Records of the Item
      *         corresponding to XML-schema "md-records.xsd".
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If the ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveMdRecords(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     //
     // Subresource - content-streams
     //
 
     /**
      * Retrieve all content streams of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>The XML data for the set of content streams is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * 
      * @return Returns the XML representation of the set of content streams.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If the ID is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveContentStreams(final String id)
         throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException;
 
     /**
      * Retrieves a content stream of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * The content stream must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Retrieve the requested content stream of the Item.</li>
      * <li>The XML data for the content stream is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the resource.
      * @param name
      *            The name of the content stream subresource.
      * 
      * @return Returns the XML representation of the content stream.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ContentStreamNotFoundException
      *             Thrown if a content stream with the specified name cannot be
      *             found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If at least one of the IDs is not provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveContentStream(final String id, final String name)
         throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException, ContentStreamNotFoundException;
 
     //
     // Subresource - properties
     //
     /**
      * Retrieve the Properties of an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided reference.</li>
      * <li>Retrieve the properties of the Item. This includes the Content Type
      * specific properties, too.</li>
      * <li>The XML representation of the Item properties corresponding to
      * XML-schema is returned as output.</li>
      * </ul>
      * 
      * @param id
      *            The id of the resource.
      * @return Returns the XML representation of the Item properties
      *         corresponding to XML-schema.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @escidoc_core.visible false
      */
     String retrieveProperties(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     //
     // Subresource - resources
     //
     /**
      * Retrieve the list of virtual Resources of an Item.<br/>
      * This methods returns a list of additional resources which aren't stored
      * in IR but created on request<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Determine which resources are available.</li>
      * <li>Create the list of resources.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @return The XML representation of the resources of that Item
      *         corresponding to XML-schema "Item.xsd".
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @axis.exclude
      * 
      * @escidoc_core.available REST
      */
     String retrieveResources(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Retrieve the content of the specified virtual Resources of an Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Determine if the resource is available.</li>
      * <li>Create the content of the resource.</li>
      * <li>The data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The id of the Item.
      * @param resourceName
      *            The name of the resource.
      * 
      * @return The content of the resource.
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OperationNotFoundException
      *             If there is no operation for the given name.
      * @axis.exclude
      * 
      * @escidoc_core.available REST
      */
     EscidocBinaryContent retrieveResource(
         final String id, final String resourceName,
         final Map<String, String[]> parameters) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         OperationNotFoundException;
 
     /**
      * Retrieve the history of an Item.<br/>
      * 
      * This method is only allowed if the user is a depositor of that object or
      * other users with special privileges, e.g. someone like an Admin.<br/>
      * 
      * The version history contains a version element for every version of the
      * object inside a version-history element. A version element contains
      * elements with values for version-number, timestamp, version-status,
      * valid-status and a comment which belongs to the latest update of that
      * version.<br/>
      * 
      * A modification of the object that does not create a new version is
      * recorded as premis event inside a events element in the version element.<br/>
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Retrieve all version information.</li>
      * <li>The XML datastream 'version-history' is returned.</li>
      * </ul>
      * <b>Example:</b><br/>
      * <br/>
      * 
      * <pre>
      * &lt;escidocVersions:version-history&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;!-- namespaces omitted for readability --&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;escidocVersions:version objid=&quot;escidoc:13164:5&quot;
      * </pre>
      * 
      * <pre>
      * timestamp = &quot;2007-08-24T15:11:45.218Z&quot;
      * </pre>
      * 
      * <pre>
      *      xlink:href=&quot;/ir/Item/escidoc:13164:5&quot;
      * </pre>
      * 
      * <pre>
      *      xlink:title=&quot;Version 5&quot;
      * </pre>
      * 
      * <pre>
      *      xml:base=&quot;http://localhost:8080&quot; xlink:type=&quot;simple&quot;
      * </pre>
      * 
      * <pre>
      *      last-modification-date=&quot;2007-08-24T15:11:45.218Z&quot; &gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:version-number&gt;5&lt;/escidocVersions:version-number&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:timestamp&gt;2007-08-24T15:11:45.218Z
      * </pre>
      * 
      * <pre>
      *     &lt;/escidocVersions:timestamp&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:version-status&gt;released&lt;/escidocVersions:version-status&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:valid-status&gt;valid&lt;/escidocVersions:valid-status&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:comment&gt;Update comment&lt;/escidocVersions:comment&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;escidocVersions:events&gt;
      * </pre>
      * 
      * <pre>
      *       &lt;premis:event xmlId=&quot;v5e1&quot;&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:eventIdentifier&gt;
      * </pre>
      * 
      * <pre>
      *           &lt;premis:eventIdentifierType&gt;URL&lt;/premis:eventIdentifierType&gt;
      * </pre>
      * 
      * <pre>
      *           &lt;premis:eventIdentifierValue&gt;/ir/Item/version-history#v5e1
      * </pre>
      * 
      * <pre>
      *           &lt;/premis:eventIdentifierValue&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;/premis:eventIdentifier&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:eventType&gt;update&lt;/premis:eventType&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:eventDateTime&gt;2007-08-24T15:11:45.218Z&lt;/premis:eventDateTime&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:eventDetail&gt;Update comment&lt;/premis:eventDetail&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:linkingAgentIdentifier
      * </pre>
      * 
      * <pre>
      *                 xlink:href=&quot;/aa/user-account/escidoc:user42&quot;
      * </pre>
      * 
      * <pre>
      *                 xlink:title=&quot;roland&quot; xlink:type=&quot;simple&quot;&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:linkingAgentIdentifierType&gt;escidoc-internal
      * </pre>
      * 
      * <pre>
      *           &lt;/premis:linkingAgentIdentifierType&gt;
      * </pre>
      * 
      * <pre>
      *           &lt;premis:linkingAgentIdentifierValue&gt;escidoc:user42
      * </pre>
      * 
      * <pre>
      *           &lt;/premis:linkingAgentIdentifierValue&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;/premis:linkingAgentIdentifier&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;premis:linkingObjectIdentifier&gt;
      * </pre>
      * 
      * <pre>
      *           &lt;premis:linkingObjectIdentifierType&gt;escidoc-internal
      * </pre>
      * 
      * <pre>
      *           &lt;/premis:linkingObjectIdentifierType&gt;
      * </pre>
      * 
      * <pre>
      *           &lt;premis:linkingObjectIdentifierValue&gt;escidoc:13164
      * </pre>
      * 
      * <pre>
      *           &lt;/premis:linkingObjectIdentifierValue&gt;
      * </pre>
      * 
      * <pre>
      *         &lt;/premis:linkingObjectIdentifier&gt;
      * </pre>
      * 
      * <pre>
      *       &lt;/premis:event&gt;
      * </pre>
      * 
      * <pre>
      *     &lt;/escidocVersions:events&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;/escidocVersions:version&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;!-- some more versions --&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/escidocVersions:version-history&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item.
      * @return The XML representation of the version history of the Item. (see
      *         example above)
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      */
     String retrieveVersionHistory(final String id)
         throws ItemNotFoundException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException;
 
     /**
      * Retrieve a list with references to all Containers to that this
      * Item is directly subordinated.<br />
      * <br />
      * <b>Prerequisites:</b><br />
      * <br />
      * The Item must exist.<br />
      * <br />
      * <b>Tasks:</b>
      * <ul>
      * <li>The XML representation of a list of references to the parent
      * Containers is created.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * @param id
      *            The identifier of the Item.
      * 
      * @return The XML representation of the parents of that Item
      *         corresponding to XML schema "parents.xsd".
      * @throws MissingMethodParameterException
      *             Thrown if the XML data is not provided.
      * @throws ItemNotFoundException
      *             Thrown if an Item with the provided id does
      *             not exist.
      * @throws SystemException
      *             Thrown in case of an internal error.
      * @throws AuthenticationException
      *             Thrown if the authentication failed due to an invalid
      *             provided eSciDoc user handle.
      * @throws AuthorizationException
      *             Thrown if the authorization failed.
      */
     String retrieveParents(final String id) throws AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         ItemNotFoundException, SystemException;
 
     /**
      * Retrieve the list of Relations of the Item, where the Item is the
      * relations source.<br/>
      * 
      * The list of Relations can be manipulated by the methods
      * addContentRelations, removeContentRelations.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * The Item must exist<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Determine which relations are available.</li>
      * <li>Create the list of relations.</li>
      * <li>The XML data is returned.</li>
      * </ul>
      * 
      * 
      * @param id
      *            The id of the Item.
      * @return The XML representation of the relations of that Item
      *         corresponding to XML-schema "relations.xsd".
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * 
      */
     String retrieveRelations(final String id) throws ItemNotFoundException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException;
 
     /**
      * Release an Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The the latest-version-status is "submitted".<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The latest version status of the Item is changed to "released". This
      * also applies to public-status till it is once set to "released".</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;comment&gt;Release comment.&lt;/comment&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be released.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      * @om
      */
     String release(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, InvalidStatusException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException,
         InvalidXmlException;
 
     /**
      * Submit an Item.<br/>
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The the latest-version-status is "pending" or "in-revision".<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "submitted". This also
      * applies to public-status till it is once set to "released".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;comment&gt;Submit comment.&lt;/comment&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be submitted.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      * @om
      */
     String submit(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, InvalidStatusException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException,
         InvalidXmlException;
 
     /**
      * Set an Item in revision.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The the latest-version-status is "submitted".<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The status of the latest-version is changed to "in-revision".</li>
      * <li>Latest version date is updated.</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;comment&gt;Revise comment.&lt;/comment&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be revised.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * @throws XmlCorruptedException
      *             Thrown if the taskParam has invalid structure.
      * 
      * @om
      */
     String revise(final String id, final String taskParam)
         throws AuthenticationException, AuthorizationException,
         ItemNotFoundException, ComponentNotFoundException, LockingException,
         InvalidStatusException, MissingMethodParameterException,
         SystemException, InvalidContentException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException,
         XmlCorruptedException;
 
     /**
      * Withdraw an Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The public-status is "released".<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The public-status of the Item is changed to "withdrawn".</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;comment&gt;Withdraw comment.&lt;/comment&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be withdrawn.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws NotPublishedException
      *             Thrown if the status shall be changed to withdrawn but the
      *             Item has not been published.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AlreadyWithdrawnException
      *             If the Item is already withdrawn.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws ReadonlyViolationException
      *             If a read-only rule is violated.
      * @throws ReadonlyVersionException
      *             If the Item is not in its latest version specified.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      * @om
      */
     String withdraw(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         NotPublishedException, LockingException, AlreadyWithdrawnException,
         AuthenticationException, AuthorizationException,
         InvalidStatusException, MissingMethodParameterException,
         SystemException, OptimisticLockingException,
         ReadonlyViolationException, ReadonlyVersionException,
         InvalidXmlException;
 
     /**
      * Lock an Item.<br/>
      * 
      * The Item will be locked by a user and no other user will be able to
      * change this Item until the Lock-User (or the Admin) will unlock this
      * Item.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The lock-status of the Item is changed to "locked".</li>
      * <li>The lock-date and lock-owner are added to the Item.</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;comment&gt;Look comment.&lt;/comment&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be locked.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      * @throws InvalidStatusException
      *             Thrown if Item is in status withdrawn.
      * @om
      */
     String lock(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, InvalidContentException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         SystemException, OptimisticLockingException, InvalidXmlException,
         InvalidStatusException;
 
     /**
      * Unlock an Item.<br/>
      * The Item will be unlocked.<br>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is in lock-status "locked".<br/>
      * 
      * Only the user who has locked the Item (and the Admin) are allowed to
      * unlock the Item.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The lock-status of the Item is changed to "unlocked".</li>
      * <li>The lock-date and lock-owner are removed from the Item.</li>
      * <li>No new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item to be unlocked.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose. (see example above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidXmlException
      *             Thrown if the taskParam has invalid structure.
      * @om
      */
     String unlock(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidXmlException;
 
     /**
      * Move an Item to an other Context<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Context-ID is validated.</li>
      * <li>It is checked whether the Item fits to the prerequisites in the Admin
      * Descriptor of the new Context.</li>
      * <li>Item is added to the provided Context.</li>
      * <li>An XML representation of the Item is created.</li>
      * <li>This XML data is returned.</li>
      * </ul>
      * 
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The Item ID.
      * @param taskParam
      *            The timestamp of the last modification of the Item. Necessary
      *            for optimistic locking purpose.
      * 
      * @return The XML representation of the Item corresponding to XML-schema
      *         "Item.xsd". (see example above)
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ContextNotFoundException
      *             Thrown if an context with the specified id cannot be found.
      * @throws InvalidContentException
      *             Thrown if the content is invalid.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * 
      * @escidoc_core.visible false
      */
     String moveToContext(final String id, final String taskParam)
         throws ItemNotFoundException, ContextNotFoundException,
         InvalidContentException, LockingException, InvalidStatusException,
         MissingMethodParameterException, AuthenticationException,
         AuthorizationException, SystemException;
 
     /**
      * Assign a Persistent Identifier (PID) to a version of an Item.<br/>
      * 
      * This PID represents the reference to a specific version of the Item. <br/>
      * 
      * The used PID Service and the assignment behavior is configured in
      * <b>escidoc-core.properties</b>.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * No version PID is assigned to this version of the Item.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id including the version
      * Identifier.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Identifier is created and with the provided URL registered in the
      * configured PID System.</li>
      * <li>Persistent Identifier is added to the version if the Item.</li>
      * <li>The assigned Persistent Identifier is returned within an XML
      * structure.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * REST: /ir/Item/escidoc:123:3/assign-version-pid
      * </pre>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;url&gt;http://application.url/some/resource&lt;/url&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * <b>Response:</b> (example)<br/>
      * 
      * <pre>
      * &lt;result last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;hdl:12345/98765&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/result&gt;
      * </pre>
      * 
      * An assignment does not lead to a new version.
      * 
      * PID Assignment with externally managed PIDs:
      * 
      * Another alternative is to register a PID which is managed outside of the
      * framework. Extend an Item with an already existing PID is also possible
      * through the assignObjectPid() method. Instead of providing the URL to the
      * resource is an XML element called 'pid' to include. E.g.
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;somePid&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * The value of the pid element is used within the framework as PID. Be
      * aware that the value of the pid element is not checked!
      * 
      * @param id
      *            The Item ID including the version identifier.
      * @param taskParam
      *            The timestamp of the last modification of the Item and at
      *            minimum the URL which is to register in the PID resolver. This
      *            parameter list is forwarded to the PID resolver and could be
      *            extended. (see example above)
      * @return The XML snippet corresponding to (result.xsd) with
      *         last-modification-date and the Persistent Identifier (see example
      *         above).
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws XmlCorruptedException
      *             Thrown if the taskParam has invalid structure.
      * @throws ReadonlyVersionException
      *             Thrown if a provided Item version id is not a latest version.
      */
     String assignVersionPid(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidStatusException,
         XmlCorruptedException, ReadonlyVersionException;
 
     /**
      * Assign a Persistent Identifier (PID) to an Item.<br/>
      * 
      * This PID represents the object reference and should used to identify the
      * Item as object reference. <br/>
      * 
      * The PID Service is configured in <b>escidoc-core.properties</b>.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * No object PID is assigned to the Item.<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>Persistent Identifier is added to the Item.</li>
      * <li>The assigned Persistent Identifier is returned within an XML
      * structure.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * <br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;url&gt;http://application.url/some/resource&lt;/url&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * <b>Response:</b> (example)<br/>
      * 
      * <pre>
      * &lt;result last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;hdl:12345/98765&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/result&gt;
      * </pre>
      * 
      * PID Assignment with externally managed PIDs:
      * 
      * Another alternative is to register a PID which is managed outside of the
      * framework. Extend an Item with an already existing PID is also possible
      * through the assignObjectPid() method. Instead of providing the URL to the
      * resource is an XML element called 'pid' to include. E.g.
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;somePid&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * The value of the pid element is used within the framework as PID. Be
      * aware that the value of the pid element is not checked!
      * 
      * @param id
      *            The Item ID.
      * @param taskParam
      *            The timestamp of the last modification of the Item and at
      *            minimum the URL which is to register in the PID resolver. This
      *            parameter list is forwarded to the PID resolver and could be
      *            extended. (see example above)
      * @return The XML snippet corresponding to (result.xsd) with
      *         last-modification-date and the Persistent Identifier (see example
      *         above).
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws XmlCorruptedException
      *             Thrown if the taskParam has invalid structure.
      */
     String assignObjectPid(final String id, final String taskParam)
         throws ItemNotFoundException, ComponentNotFoundException,
         LockingException, AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidStatusException,
         XmlCorruptedException;
 
     /**
      * Assign a Persistent Identifier to a Component of an Item.
      * 
      * This PID represents the object reference and should used to identify a
      * component of an Item. <br/>
      * 
      * The PID Service is configured in <b>escidoc-core.properties</b>.<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item public-status must not be "withdrawn".<br/>
      * 
      * The Component must exist.<br/>
      * 
      * No object pid is assigned to the Item.<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The Component is accessed using the provided ID.</li>
      * <li>Persistent Identifier is added to the component.</li>
      * <li>The assigned Persistent Identifier is returned within an XML
      * structure.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;url&gt;http://application.url/some/resource&lt;/url&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * <b>Response:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;hdl:12345/98765&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * PID Assignment with externally managed PIDs:
      * 
      * Another alternative is to register a PID which is managed outside of the
      * framework. Extend an Item with an already existing PID is also possible
      * through the assignObjectPid() method. Instead of providing the URL to the
      * resource is an XML element called 'pid' to include. E.g.
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;pid&gt;somePid&lt;/pid&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * The value of the pid element is used within the framework as PID. Be
      * aware that the value of the pid element is not checked!
      * 
      * @param id
      *            The Item ID.
      * @param componentId
      *            The Id of the Component.
      * @param taskParam
      *            The timestamp of the last modification of the Item and at
      *            minimum the URL which is to register in the PID resolver. This
      *            parameter list is forwarded to the PID resolver and could be
      *            extended. (see example above)
      * @return The XML snippet corresponding to (result.xsd) with
      *         last-modification-date and the Persistent Identifier (see example
      *         above).
      * 
      * @throws ItemNotFoundException
      *             Thrown if an Item with the specified id cannot be found.
      * @throws ComponentNotFoundException
      *             Thrown if an component with the specified id cannot be found.
      * @throws LockingException
      *             Thrown if the Item is locked and the current user is not the
      *             one who locked it.
      * @throws InvalidStatusException
      *             Thrown in case of an invalid status.
      * @throws XmlCorruptedException
      *             Thorwn if taskParam is invalid XML.
      * @throws AuthenticationException
      *             Thrown if the authentication fails due to an invalid provided
      *             eSciDocUserHandle.
      * @throws AuthorizationException
      *             Thrown if the authorization fails.
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws OptimisticLockingException
      *             If the provided latest-modification-date does not match.
      */
     String assignContentPid(
         final String id, final String componentId, final String taskParam)
         throws ItemNotFoundException, LockingException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, SystemException,
         OptimisticLockingException, InvalidStatusException,
         XmlCorruptedException, ComponentNotFoundException;
 
     /**
      * Add new content relations to the Item<br/>
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The new relations, which have the Item as a source are created
      * according to a provided data</li>
      * <li>Latest version date is updated</li>
      * <li>A new version is created.</li>
      * <li>Method checks if content relations with provided data already exist
      * in the system. If there are matched content relations the
      * AlreadyExistException will be thrown.</li>
      * <li>For content relations data that does not match existing relations the
     * framework checks if provided target ids contain a version nummer. In this
      * case an InvalidContentException will be thrown. Then framework checks if
     * resources with a provided ids and provided predicates from provided
      * ontology exist in the system.</li>
      * <li>New relations will be created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:500&lt;/targetId&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * 
      * <pre>
      *     content-relations#isAnnotationOf&lt;/predicate&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:340&lt;/targetId&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * 
      * <pre>
      *     content-relations #isRevisionOf&lt;/predicate&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the Item, which will be a source of new relations.
      * @param taskParameter
      *            xml structure with a last modification date of the source Item
      *            and a list of sections containing respectively a target id and
      *            a content relation predicate. A target id may not contain a
      *            version number: (see description above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws OptimisticLockingException
      * @throws ReferencedResourceNotFoundException
      * @throws RelationPredicateNotFoundException
      * @throws AlreadyExistsException
      * @throws InvalidStatusException
      * @throws InvalidXmlException
      * @throws MissingElementValueException
      * @throws LockingException
      * @throws ReadonlyViolationException
      * @throws InvalidContentException
      * @throws AuthenticationException
      * @throws AuthorizationException
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      * @throws ReadonlyVersionException
      * 
      */
     @Validate(param = 1, resolver = "getUpdateRelationsSchemaLocation")
     String addContentRelations(final String id, final String taskParameter)
         throws SystemException, ItemNotFoundException,
         ComponentNotFoundException, OptimisticLockingException,
         ReferencedResourceNotFoundException,
         RelationPredicateNotFoundException, AlreadyExistsException,
         InvalidStatusException, InvalidXmlException,
         MissingElementValueException, LockingException,
         ReadonlyViolationException, InvalidContentException,
         AuthenticationException, AuthorizationException,
         MissingMethodParameterException, ReadonlyVersionException;
 
     /**
      * Remove content relations from the Item<br/>
      * 
      * <b>Prerequisites:</b><br/>
      * 
      * The Item must exist<br/>
      * 
      * The Item is not locked.<br/>
      * 
      * The public-status is not "withdrawn".<br/>
      * 
      * Only the lastest version can be used here.<br/>
      * 
      * <b>Tasks:</b><br/>
      * <ul>
      * <li>The Item is accessed using the provided Id.</li>
      * <li>Optimistic Locking criteria is checked.</li>
      * <li>The method checks if a source Item contains all relations with
      * provided data. Otherwise a ContentRelationNotFoundException will be
      * thrown.</li>
      * <li>The relations will be deleted from the source Item.</li>
      * <li>Latest version date is updated.</li>
      * <li>A new version is created.</li>
      * <li>No data is returned.</li>
      * </ul>
      * 
      * <b>Parameter for request:</b> (example)<br/>
      * 
      * <pre>
      * &lt;param last-modification-date=&quot;1967-08-13T12:00:00.000+01:00&quot;&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:500&lt;/targetId&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * 
      * <pre>
      *     content-relations#isAnnotationOf&lt;/predicate&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;relation&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;targetId&gt;escidoc:340&lt;/targetId&gt;
      * </pre>
      * 
      * <pre>
      *   &lt;predicate&gt;http://www.escidoc.de/ontologies/mpdl-ontologies/
      * </pre>
      * 
      * <pre>
      *     content-relations #isRevisionOf&lt;/predicate&gt;
      * </pre>
      * 
      * <pre>
      *  &lt;/relation&gt;
      * </pre>
      * 
      * <pre>
      * &lt;/param&gt;
      * </pre>
      * 
      * @param id
      *            The id of the source resource.
      * @param param
      *            xml structure with a last modification date of the source
      *            resource and a list of sections containing respectively a
      *            target id and a content relation predicate: (see description
      *            above)
      * @return last-modification-date within XML (result.xsd)
      * 
      * @throws ItemNotFoundException
      * @throws ComponentNotFoundException
      *             Thrown if a component with the specified id cannot be found.
      * @throws OptimisticLockingException
      * @throws InvalidStatusException
      * @throws MissingElementValueException
      * @throws InvalidContentException
      * @throws InvalidXmlException
      * @throws ContentRelationNotFoundException
      * @throws AlreadyDeletedException
      * @throws LockingException
      * @throws ReadonlyViolationException
      * @throws AuthenticationException
      * @throws AuthorizationException
      * @throws ReadonlyVersionException
      * @throws MissingMethodParameterException
      *             If no data is provided.
      * @throws SystemException
      *             If an error occurs.
      */
     @Validate(param = 1, resolver = "getUpdateRelationsSchemaLocation")
     String removeContentRelations(final String id, final String param)
         throws SystemException, ItemNotFoundException,
         ComponentNotFoundException, OptimisticLockingException,
         InvalidStatusException, MissingElementValueException,
         InvalidContentException, InvalidXmlException,
         ContentRelationNotFoundException, AlreadyDeletedException,
         LockingException, ReadonlyViolationException, AuthenticationException,
         AuthorizationException, MissingMethodParameterException,
         ReadonlyVersionException;
 }
