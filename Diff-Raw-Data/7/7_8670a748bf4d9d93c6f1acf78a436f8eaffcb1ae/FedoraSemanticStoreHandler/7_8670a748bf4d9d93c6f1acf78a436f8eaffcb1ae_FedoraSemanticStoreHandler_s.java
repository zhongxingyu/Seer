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
 
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.xml.stream.FactoryConfigurationError;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.events.XMLEvent;
 
 import de.escidoc.core.common.business.TripleStoreConnector;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreOutputFormatException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidTripleStoreQueryException;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingElementValueException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.stax.handler.SemanticQueryHandler;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.om.business.interfaces.SemanticStoreHandlerInterface;
 import de.escidoc.core.om.business.stax.handler.filter.RDFRegisteredOntologyFilter;
 
 /**
  * @spring.bean id="business.FedoraSemanticStoreHandler"
  * @author ROF
  * 
  */
 
 public class FedoraSemanticStoreHandler
     implements SemanticStoreHandlerInterface {
 
     private static AppLogger log = new AppLogger(
         FedoraSemanticStoreHandler.class.getName());
 
     private TripleStoreConnector tripleStoreConnector = null;
 
    public void setTripleStoreConnector(TripleStoreConnector tripleStoreConnector) {
        this.tripleStoreConnector = tripleStoreConnector;
    }

     /**
      * Retrieves a result of provided triple store query in a provided output
      * format.
      * 
      * @param taskParam
      *            SPO query parameter and return representation type.
      * 
      *            <pre>
      *  &lt;param&gt;
      *      &lt;query&gt;&lt;info:fedora/escidoc:111&gt;
      *      &lt;http://www.escidoc.de/ontologies/mpdl-ontologies/content-relations#isRevisionOf&gt;
      * /query&gt;
      *      &lt;format&gt;N-Triples&lt;/format&gt;                                           
      *      &lt;/param&gt;
      * </pre>
      * 
      * @return Returns XML representation of the query result.
      * @thows InvalidXmlException Thrown if the parameter content is invalid
      *        XML.
      * @throws InvalidTripleStoreQueryException
      *             Thrown if triple store query is invalid.
      * @throws SystemException
      *             Thrown in case of internal failure.
      * @throws InvalidTripleStoreOutputFormatException
      *             Thrown if triple store output format is wrong defined.
      * @om
      */
     public String spo(final String taskParam) throws SystemException,
         InvalidTripleStoreQueryException,
         InvalidTripleStoreOutputFormatException, InvalidXmlException,
         MissingElementValueException {
 
         StaxParser sp = new StaxParser();
         SemanticQueryHandler qh = new SemanticQueryHandler(sp);
         sp.addHandler(qh);
         try {
             sp.parse(taskParam);
             sp.clearHandlerChain();
         }
         catch (MissingElementValueException e) {
             throw e;
         }
         catch (Exception e) {
             XmlUtility.handleUnexpectedStaxParserException("", e);
         }
         sp.clearHandlerChain();
         String query = qh.getQuery();
         // check predicate
         String predicate = qh.getPredicate();
         if (!predicate.equals("*")
             && !OntologyUtility.checkPredicate(predicate)) {
             throw new InvalidTripleStoreQueryException("Predicate '"
                 + XmlUtility.escapeForbiddenXmlCharacters(predicate)
                 + "' not allowed.");
         }
         String format = qh.getFormat();
         String result = tripleStoreConnector.requestMPT(query, format);
         if (!"".equals(result) && predicate.equals("*")) {
             // TODO check result for unallowed predicates
             if (format.equals("N-Triples")) {
                 String[] triples = result.split("\\s\\.");
                 result = "";
                 for (int i = 0; i < triples.length; i++) {
                     String[] tripleParts = triples[i].trim().split("\\ +", 3);
                     if (tripleParts.length >= 2) {
                         if (OntologyUtility.checkPredicate(tripleParts[1])) {
                             result += triples[i] + ".\n";
                         }
                     }
                 }
             }
             else if (format.equals("RDF/XML")) {
                 // TODO revise, move
                 try {
                     XMLInputFactory inf = XMLInputFactory.newInstance();
                     XMLEventReader reader =
                         inf.createFilteredReader(
                             inf.createXMLEventReader(new StringReader(result)),
                             new RDFRegisteredOntologyFilter());
 
                     StringWriter sw = new StringWriter();
                     XMLEventWriter writer = XmlUtility.createXmlEventWriter(sw);
 
                     // writer.add(reader);
                     while (reader.hasNext()) {
                         XMLEvent event = reader.nextEvent();
                         writer.add(event);
                     }
 
                     result = sw.toString();
                 }
                 catch (FactoryConfigurationError e) {
                     throw new WebserverSystemException(e);
                 }
                 catch (XMLStreamException e) {
                     throw new WebserverSystemException(e);
                 }
             }
             else {
                 log.warn("No filter defined for result format '" + format
                     + "'.");
             }
         }
         return result;
     }
 
     /**
      * Injects the triple store connector bean.
      * 
      * @param tripleStoreConnector
      *            The {@link TripleStoreConnector}.
      * @spring.property ref="business.TripleStoreConnector"
      * 
      */
    public void setTripleStoreUtility(
         final TripleStoreConnector tripleStoreConnector) {
         this.tripleStoreConnector = tripleStoreConnector;
     }
 }
