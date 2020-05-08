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
 package de.escidoc.core.common.util.stax.handler;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.stax.events.EndElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 import de.escidoc.core.common.util.xml.stax.handler.DefaultHandler;
 
 public class RelsExtContentRelationsReadHandler extends DefaultHandler {
 
     protected StaxParser parser;
 
     protected Vector<HashMap<String, String>> relations =
         new Vector<HashMap<String, String>>();
 
     protected boolean inRdf = false;
 
     protected boolean inRelation = false;
 
     protected String path = "/RDF/Description";
 
     protected String targetId = null;
 
     protected String predicate = null;
 
     protected static AppLogger log =
         new AppLogger(MultipleExtractor.class.getName());
 
     public RelsExtContentRelationsReadHandler(StaxParser parser) {
         this.parser = parser;
     }
 
     public Vector<HashMap<String, String>> getRelations() {
         return this.relations;
     }
 
     public StartElement startElement(StartElement element)
         throws WebserverSystemException {
         String curPath = parser.getCurPath();
 
         if (curPath.equals(path)) {
             inRdf = true;
         }
         if (inRdf) {
 
             if (element.getPrefix().equals(
                 Constants.CONTENT_RELATIONS_NS_PREFIX_IN_RELSEXT)) {
                 inRelation = true;
 
                 int indexOfResource =
                     element.indexOfAttribute(Constants.RDF_NAMESPACE_URI,
                         "resource");
                 if (indexOfResource != -1) {
                     String resourceValue =
                         element.getAttribute(indexOfResource).getValue();
                     String[] target = resourceValue.split("/");
                     targetId = target[1];
                 }
                 else {
                     String message =
                         "The attribute 'rdf:resource' of the element '"
                             + element.getLocalName() + "' is missing.";
                     log.error(message);
                     throw new WebserverSystemException(message);
                 }
                 String predicateNs = element.getNamespace();
                predicateNs =
                    predicateNs.substring(0, predicateNs.length() - 1);
                 String predicateValue = element.getLocalName();
                predicate = predicateNs + "#" + predicateValue;
             }
 
         }
         return element;
     }
 
     public EndElement endElement(EndElement element) {
 
         if (inRelation) {
             HashMap<String, String> relationData =
                 new HashMap<String, String>();
             relations.add(relationData);
             relationData.put("predicate", predicate);
             relationData.put("target", targetId);
 
             targetId = null;
             predicate = null;
             inRelation = false;
         }
 
         return element;
     }
 
 }
