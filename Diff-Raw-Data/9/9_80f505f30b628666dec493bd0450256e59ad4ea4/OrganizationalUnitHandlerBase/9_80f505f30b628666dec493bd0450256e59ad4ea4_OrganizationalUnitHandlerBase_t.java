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
 package de.escidoc.core.oum.business.fedora.organizationalunit;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.HandlerBase;
 import de.escidoc.core.common.business.fedora.Utility;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidStatusException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.invalid.TmeException;
 import de.escidoc.core.common.exceptions.application.invalid.XmlCorruptedException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingContentException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentModelNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContentRelationNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ContextNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.OrganizationalUnitNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ReferencedResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.RelationPredicateNotFoundException;
 import de.escidoc.core.common.exceptions.application.notfound.ResourceNotFoundException;
 import de.escidoc.core.common.exceptions.application.violated.AlreadyExistsException;
 import de.escidoc.core.common.exceptions.application.violated.LockingException;
 import de.escidoc.core.common.exceptions.application.violated.OptimisticLockingException;
 import de.escidoc.core.common.exceptions.application.violated.PidAlreadyAssignedException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyAttributeViolationException;
 import de.escidoc.core.common.exceptions.application.violated.ReadonlyElementViolationException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.MultipleExtractor2;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.oum.business.fedora.resources.OrganizationalUnit;
 import de.escidoc.core.oum.business.renderer.VelocityXmlOrganizationalUnitFoXmlRenderer;
 import de.escidoc.core.oum.business.renderer.VelocityXmlOrganizationalUnitRenderer;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitFoXmlRendererInterface;
 import de.escidoc.core.oum.business.renderer.interfaces.OrganizationalUnitRendererInterface;
 
 import javax.xml.stream.XMLStreamException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.Vector;
 
 /**
  * This class contains common methods for all handler classes of an
  * organizational unit.
  * 
  * @author MSC
  * 
  */
 public class OrganizationalUnitHandlerBase extends HandlerBase {
 
     private Stack<List<String>> pathes;
 
     private Utility utility = null;
 
     private OrganizationalUnit organizationalUnit = null;
 
     private OrganizationalUnitRendererInterface renderer = null;
 
     private OrganizationalUnitFoXmlRendererInterface foxmlRenderer = null;
 
     public static final String DATA_ENCLOSING_TAG_START = "<data-contents>";
 
     public static final String DATA_ENCLOSING_TAG_END = "</data-contents>";
 
     /**
      * Binds an organizational unit object to this handler.
      * 
      * @param id
      *            The id of the organizational unit which should be bound to
      *            this Handler.
      * @throws OrganizationalUnitNotFoundException
      *             If no organizational unit with the given id exists.
      * @throws SystemException
      *             If anything unexpected goes wrong.
      */
     protected void setOrganizationalUnit(final String id)
         throws OrganizationalUnitNotFoundException, SystemException {
 
         if (id != null) {
             try {
                 organizationalUnit = new OrganizationalUnit(id);
             }
             catch (ResourceNotFoundException e) {
                 throw new OrganizationalUnitNotFoundException(e);
             }
         }
         else {
             organizationalUnit = null;
         }
     }
 
     /**
      * 
      * @return Get the current organizational unit resource.
      */
     protected OrganizationalUnit getOrganizationalUnit() {
         return organizationalUnit;
     }
 
     /**
      * @return Returns the utility.
      */
     protected Utility getUtility() {
         if (utility == null) {
             utility = Utility.getInstance();
         }
         return utility;
     }
 
     /**
      * Check the name. It may neither be empty nor null. Additionally it must be
      * unique within its scope.
      * 
      * @param id
      *            If a create is executed, id is null. In case of an update the
      *            organizational unit already exists and has an id.
      * @param name
      *            The name.
      * @param parents
      *            The list of parents.
      * @throws MissingElementValueException
      *             It the name is empty or null.
      * @throws SystemException
      *             Thrown in case of an internal error.
      */
     protected void checkName(
         final String id, final String name, final List<String> parents)
         throws MissingElementValueException, SystemException {
 
         if (("".equals(name)) || (name == null)) {
             throw new MissingElementValueException(
                 "Name of organizational unit must be set!");
         }
     }
 
     /**
      * Initialize the pathes queue.
      * 
      */
     protected void initPathes() {
         this.pathes = new Stack<List<String>>();
     }
 
     /**
      * @return The pathes queue.
      */
     protected Stack<List<String>> getPathes() {
         return this.pathes;
     }
 
     /**
      * Expands the given path with its parents. If there are no parents the
      * given path is the only result.
      * 
      * @param path
      *            The path to expand.
      * @throws SystemException
      *             If the access to the triplestore fails.
      */
     protected void expandPaths(final List<String> path) throws SystemException {
 
         List<String> organizationalUnitIds =
             getTripleStoreUtility().getParents(path.get(path.size() - 1));
         if (organizationalUnitIds != null) {
             if (!organizationalUnitIds.isEmpty()) {
                 for (String organizationalUnitId : organizationalUnitIds) {
                     List<String> newPath = new ArrayList<String>(path);
                     String parent = organizationalUnitId;
                     newPath.add(parent);
                     getPathes().push(newPath);
 
                 }
             }
             else {
                getPathes().push(new ArrayList<String>(path));
             }
         }
     }
 
     /**
      * Parse the organizational unit xml for create purposes with the stax
      * parser.
      * 
      * @param xml
      *            The xml to parse.
      * @param parser
      *            The stax parser.
      * @throws MissingAttributeValueException
      *             If a mandatory attribute was not set.
      * @throws MissingElementValueException
      *             If a mandatory element was not set.
      * @throws OrganizationalUnitNotFoundException
      *             If the organizational unit does not exist.
      * @throws XmlCorruptedException
      *             Thrown if the schema validation of the provided data failed.
      * @throws SystemException
      *             If anything fails.
      * @throws MissingMdRecordException
      *             If the required md-record is missing
      */
     protected void parseIncomingXmlForCreate(
         final String xml, final StaxParser parser)
         throws MissingAttributeValueException, MissingElementValueException,
         OrganizationalUnitNotFoundException, XmlCorruptedException,
         SystemException, MissingMdRecordException {
 
         try {
             parser.parse(XmlUtility.convertToByteArrayInputStream(xml));
             parser.clearHandlerChain();
         }
         catch (LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (XMLStreamException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (OptimisticLockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContentModelNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContextNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReferencedResourceNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (RelationPredicateNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (InvalidContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (InvalidStatusException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
     }
 
     /**
      * Parse the organizational unit xml for update purposes with the stax
      * parser.
      * 
      * @param xml
      *            The xml to parse.
      * @param parser
      *            The stax parser.
      * @throws InvalidXmlException
      *             If xml is invalid.
      * @throws OptimisticLockingException
      *             If the organizational unit was changed in the meantime.
      * @throws OrganizationalUnitNotFoundException
      *             If the organizational unit does not exist.
      * @throws SystemException
      *             If anything fails.
      */
     protected void parseIncomingXmlForUpdate(
         final String xml, final StaxParser parser) throws InvalidXmlException,
         OptimisticLockingException, OrganizationalUnitNotFoundException,
         SystemException {
 
         try {
             parser.parse(XmlUtility.convertToByteArrayInputStream(xml));
             parser.clearHandlerChain();
         }
         catch (XMLStreamException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (MissingAttributeValueException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (MissingElementValueException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (MissingContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (MissingMdRecordException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContextNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContentModelNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (LockingException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (AlreadyExistsException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReferencedResourceNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (RelationPredicateNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ContentRelationNotFoundException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReadonlyAttributeViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (InvalidContentException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (InvalidStatusException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (ReadonlyElementViolationException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (PidAlreadyAssignedException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         catch (TmeException e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
     }
 
     /**
      * Creates and initlizes a
      * {@link de.escidoc.core.common.util.stax.handler.MultipleExtractor2} that
      * uses the provided {@link de.escidoc.core.common.util.stax.StaxParser}.<br/>
      * The created <code>MultipleExtractor2</code> extracts the pathes
      * {@link OrganizationalUnitHandlerBase.OU_ORGANIZATION_DETAILS_PATH}.<br/>
      * It is initilized with the prefixes for
      * {@link Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI} and
      * {@link Constants.XLINK_NS_URI}.
      * 
      * @param sp
      *            The {@link de.escidoc.core.common.util.stax.StaxParser} to
      *            use.
      * @param mdRecordPath
      *            The xpath to organization-details element.
      * @return Returns the created
      *         {@link de.escidoc.core.common.util.stax.handler.MultipleExtractor2}
      *         .
      */
     protected MultipleExtractor2 createMultipleExtractor(
         final StaxParser sp, final String mdRecordPath) {
 
         HashMap<String, String> extractPathes = new HashMap<String, String>();
         extractPathes.put(mdRecordPath, "name");
         Map<String, String> namespaceMap = new HashMap<String, String>(2);
         namespaceMap.put(Constants.ORGANIZATIONAL_UNIT_NAMESPACE_URI,
             Constants.ORGANIZATIONAL_UNIT_PREFIX);
         namespaceMap.put(Constants.XLINK_NS_URI, Constants.XLINK_NS_PREFIX);
         return new MultipleExtractor2(namespaceMap, extractPathes, sp);
     }
 
     /**
      * 
      * @return The foxml renderer.
      */
     public OrganizationalUnitFoXmlRendererInterface getFoxmlRenderer() {
 
         if (foxmlRenderer == null) {
             foxmlRenderer = new VelocityXmlOrganizationalUnitFoXmlRenderer();
         }
         return foxmlRenderer;
     }
 
     /**
      * @return the renderer
      */
     public OrganizationalUnitRendererInterface getRenderer() {
         if (renderer == null) {
             renderer = new VelocityXmlOrganizationalUnitRenderer();
         }
         return renderer;
     }
 }
