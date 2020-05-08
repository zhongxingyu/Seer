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
 package de.escidoc.core.om.business.fedora;
 
 import de.escidoc.core.common.business.TripleStoreConnector;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreOutputFormatException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.system.EncodingSystemException;
 import de.escidoc.core.common.exceptions.system.TripleStoreSystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.exceptions.system.XmlParserSystemException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.SemanticQueryHandler;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.business.interfaces.SemanticStoreHandlerInterface;
 import de.escidoc.core.om.business.stax.handler.filter.RDFRegisteredOntologyFilter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 /**
  * @author Rozita Friedman
  */
 @Service("business.FedoraSemanticStoreHandler")
 public class FedoraSemanticStoreHandler implements SemanticStoreHandlerInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FedoraSemanticStoreHandler.class);
 
     @Autowired
     @Qualifier("business.TripleStoreConnector")
     private TripleStoreConnector tripleStoreConnector;
 
     /**
      * Protected constructor to prevent instantiation outside of the Spring-context.
      */
     protected FedoraSemanticStoreHandler() {
     }
 
     /**
      * Retrieves a result of provided triple store query in a provided output format.
      *
      * @param taskParam SPO query parameter and return representation type.
      *                  <p/>
      *                  <pre>
      *
      *                  &lt;param&gt;
      *
      *                                   &lt;query&gt;&lt;info:fedora/escidoc:111&gt;
      *
      *                                   &lt;http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf&gt;
      *                                                                                                       /query&gt;
      *
      *                                   &lt;format&gt;N-Triples&lt;/format&gt;
      *
      *                  &lt;/param&gt;
      *                                                                                                       </pre>
      * @return Returns XML representation of the query result.
      * @throws InvalidTripleStoreQueryException
      *          Thrown if triple store query is invalid.
      * @throws InvalidTripleStoreOutputFormatException
      *          Thrown if triple store output format is wrong defined.
      */
     @Override
     public String spo(final String taskParam) throws InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException, InvalidXmlException, MissingElementValueException,
         EncodingSystemException, TripleStoreSystemException, XmlParserSystemException, WebserverSystemException {
 
         final StaxParser sp = new StaxParser();
         final SemanticQueryHandler qh = new SemanticQueryHandler();
         sp.addHandler(qh);
         try {
             sp.parse(taskParam);
             sp.clearHandlerChain();
         }
         catch (final MissingElementValueException e) {
             throw e;
         }
         catch (final Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         sp.clearHandlerChain();
         final String query = qh.getQuery();
         // check predicate
         final String predicate = qh.getPredicate();
         boolean accept;
         try {
             accept = OntologyUtility.checkPredicate(predicate);
         }
         catch (InvalidContentException e) {
             throw new WebserverSystemException("Predicate '" + predicate + "' is invalid.", e);
         }
 
         if (!"*".equals(predicate) && !accept) {
             throw new InvalidTripleStoreQueryException("Predicate '"
                 + XmlUtility.escapeForbiddenXmlCharacters(predicate) + "' not allowed.");
         }
         final String format = qh.getFormat();
         String result = tripleStoreConnector.requestMPT(query, format);
         if (!"".equals(result) && "*".equals(predicate)) {
             // TODO check result for unallowed predicates
             if ("N-Triples".equals(format)) {
                 final String[] triples = result.split("\\s\\.");
                 final StringBuilder stringBuffer = new StringBuilder();
                 for (final String triple : triples) {
                     final String[] tripleParts = triple.trim().split("\\ +", 3);
 
                    if (tripleParts.length == 3) {
                         try {
                             accept = OntologyUtility.checkPredicate(tripleParts[1]);
                         }
                         catch (InvalidContentException e) {
                             throw new WebserverSystemException("Predicate '" + tripleParts[1] + "' is invalid.", e);
                         }
 
                        if (tripleParts.length >= 2 && accept) {
                             stringBuffer.append(triple);
                             stringBuffer.append(".\n");
                         }
                     }
                 }
                 result = stringBuffer.toString();
             }
             else if ("RDF/XML".equals(format)) {
                 // TODO revise, move
                 try {
                     final XMLInputFactory inf = XMLInputFactory.newInstance();
                     final XMLEventReader reader =
                         inf.createFilteredReader(inf.createXMLEventReader(new StringReader(result)),
                             new RDFRegisteredOntologyFilter());
 
                     final StringWriter sw = new StringWriter();
                     final XMLEventWriter writer = XmlUtility.createXmlEventWriter(sw);
 
                     // writer.add(reader);
                     while (reader.hasNext()) {
                         final XMLEvent event = reader.nextEvent();
                         writer.add(event);
                     }
 
                     result = sw.toString();
                 }
                 catch (final XMLStreamException e) {
                     throw new WebserverSystemException(e);
                 }
             }
             else {
                 LOGGER.warn("No filter defined for result format '" + format + "'.");
             }
         }
         return result;
     }
 
 }
