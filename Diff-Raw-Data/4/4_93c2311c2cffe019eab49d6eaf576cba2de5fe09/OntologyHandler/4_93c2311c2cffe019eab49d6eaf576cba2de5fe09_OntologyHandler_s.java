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
 package de.escidoc.core.om.business.stax.handler;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.Predicate;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidContentException;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 
 import javax.xml.XMLConstants;
 
 /**
  * Check if predicate is defined in XML (Ontology).
  * 
  * @author ?
  * 
  */
 public class OntologyHandler extends DefaultHandler {
 
     private final StaxParser parser;
 
     private Predicate predicate;
 
     private String base;
 
     private boolean exist;
 
     /**
      * 
      * @param parser
      * @param predicate
      * @throws InvalidContentException
      */
     public OntologyHandler(final StaxParser parser, final String predicate) throws InvalidContentException {
 
         this.parser = parser;
         String p = predicate;
         if (p.startsWith("<")) {
             p = p.substring(1);
         }
         if (p.endsWith(">")) {
             p = p.substring(0, p.length() - 1);
         }
 
         this.predicate = new Predicate(p);
     }
 
     @Override
     public StartElement startElement(final StartElement element) {
 
         final String basePath = "/RDF";
         final String currentPath = parser.getCurPath();
         if (basePath.equals(currentPath)) {
             final int indexOfBase = element.indexOfAttribute(XMLConstants.XML_NS_URI, "base");
             if (indexOfBase != -1) {
                 this.base = element.getAttribute(indexOfBase).getValue();
             }
         }
         final String elementPath = "/RDF/Property";
         if (elementPath.equals(currentPath)) {
 
             final int indexOfId = element.indexOfAttribute(Constants.RDF_NAMESPACE_URI, "ID");
 
             if (indexOfId != -1) {
                 final String id = element.getAttribute(indexOfId).getValue();
                if (id.equals(this.predicate.getLocalname()) && (base + '#').equals(this.predicate.getNamespace())
                    || id.equals(this.predicate)) {
                     this.exist = true;
                 }
             }
         }
         return element;
     }
 
     /**
      * Existence of predicate in ontology.
      * 
      * @return True if predicate exist in parsed XML (ontology), false otherwise.
      */
     public boolean isExist() {
         return this.exist;
     }
 
 }
