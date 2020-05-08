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
 package de.escidoc.core.oum.business.handler;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.naming.directory.NoSuchAttributeException;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.exceptions.application.invalid.InvalidXmlException;
 import de.escidoc.core.common.exceptions.application.missing.MissingAttributeValueException;
 import de.escidoc.core.common.exceptions.application.missing.MissingMdRecordException;
 import de.escidoc.core.common.util.logger.AppLogger;
 import de.escidoc.core.common.util.stax.StaxParser;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.common.util.xml.stax.events.Attribute;
 import de.escidoc.core.common.util.xml.stax.events.EndElement;
 import de.escidoc.core.common.util.xml.stax.events.StartElement;
 
 /**
  * The MetadataHandler.
  * 
  * @author MSC
  * 
  * @om
  */
 public class OrganizationalUnitMetadataHandler
     extends OrganizationalUnitHandlerBase {
 
     private static final String UNKNOWN = "unknown";
 
     public static final String SCHEMA = "schema";
 
     public static final String TYPE = "md-type";
 
     public static final String NAME = "name";
 
     private boolean insideMdRecord = false;
 
     private boolean rootMetadataElementFound = false;
 
     private String currentMdRecordName = null;
 
     private String rootPath = null;
 
     private String dcTitle = "";
 
     private String escidocMetadataRecordNameSpace = null;
 
     private static AppLogger log =
         new AppLogger(OrganizationalUnitMetadataHandler.class.getName());
 
     private final Map<String, Map<String, String>> metadataAttributes =
         new HashMap<String, Map<String, String>>();
 
     private static final String MANDATORY_MD_RECORD_NAME = "escidoc";
 
     private boolean mandatoryMdRecordFound = false;
 
     /**
      * Instantiate a MetaDataHandler.
      * 
      * @param parser
      *            The parser.
      * @param rootPath
      *            XML root element path
      * @om
      */
     public OrganizationalUnitMetadataHandler(final StaxParser parser,
         final String rootPath) {
         super(null, parser);
         if (rootPath == null || "/".equals(rootPath)) {
             this.rootPath = "";
         }
         else {
             this.rootPath = rootPath;
         }
     }
 
     /**
      * Handle the start of an element.
      * 
      * @param element
      *            The element.
      * @return The element.
      * @throws MissingAttributeValueException
      * @throws MissingAttributeValueException
      *             If a required attribute is missing.
      * @see de.escidoc.core.common.util.xml.stax.handler.DefaultHandler#startElement
      *      (de.escidoc.core.common.util.xml.stax.events.StartElement)
      * @om
      */
     @Override
     public StartElement startElement(final StartElement element)
         throws InvalidXmlException, MissingAttributeValueException {
 
         String elementName = element.getLocalName();
 
         if (getMdRecordPath().equals(getParser().getCurPath())) {
             insideMdRecord = true;
             Attribute name;
             try {
                 name = element.getAttribute(null, NAME);
                 this.currentMdRecordName = name.getValue();
 
                 if (currentMdRecordName.equals("")) {
                     String message =
                         "The value of attribute 'name' of the element "
                             + elementName + " was not set!";
                     log.error(message);
                     throw new MissingAttributeValueException(message);
 
                 }
                 else if (MANDATORY_MD_RECORD_NAME.equals(currentMdRecordName)) {
                     mandatoryMdRecordFound = true;
                 }
             }
             catch (NoSuchAttributeException e) {
                 String message =
                     "The mandatory attribute 'name' of the element "
                         + elementName + " was not found!";
                 log.error(message);
                 throw new MissingAttributeValueException(message);
             }
             HashMap<String, String> md = new HashMap<String, String>();
             int indexOfType = element.indexOfAttribute(null, TYPE);
             if (indexOfType != -1) {
                 md.put("type", element.getAttribute(indexOfType).getValue());
             }
            else {
                md.put("type", UNKNOWN);
            }
             int indexOfSchema = element.indexOfAttribute(null, SCHEMA);
             if (indexOfSchema != -1) {
                 md.put(SCHEMA, element.getAttribute(indexOfSchema).getValue());
             }
             else {
                 md.put(SCHEMA, UNKNOWN);
             }
             metadataAttributes.put(this.currentMdRecordName, md);
 
         }
         else if (insideMdRecord && !rootMetadataElementFound) {
             rootMetadataElementFound = true;
             if (this.currentMdRecordName.equals(MANDATORY_MD_RECORD_NAME)) {
                 this.escidocMetadataRecordNameSpace = element.getNamespace();
             }
         }
         return element;
     }
 
     /**
      * Handle the end of an element.
      * 
      * @param element
      *            The element.
      * @return The element.
      * @see de.escidoc.core.common.util.xml.stax.handler.DefaultHandler#endElement
      *      (de.escidoc.core.common.util.xml.stax.events.EndElement)
      * @om
      */
     @Override
     public EndElement endElement(final EndElement element)
         throws MissingMdRecordException {
 
         if (getMdRecordPath().equals(getParser().getCurPath())) {
             insideMdRecord = false;
             rootMetadataElementFound = false;
             currentMdRecordName = null;
         }
         else if (getMdRecordsPath().equals(getParser().getCurPath())) {
             if (!mandatoryMdRecordFound) {
                 String message =
                     "Mandatory md-record with a name "
                         + MANDATORY_MD_RECORD_NAME + " is missing.";
                 log.error(message);
                 throw new MissingMdRecordException(message);
             }
         }
         return element;
     }
 
     /**
      * Handle the character section of an element.
      * 
      * @param s
      *            The contents of the character section.
      * @param element
      *            The element.
      * @return The character section.
      * @see de.escidoc.core.common.util.xml.stax.handler.DefaultHandler#characters
      *      (java.lang.String,
      *      de.escidoc.core.common.util.xml.stax.events.StartElement)
      * @om
      */
     @Override
     public String characters(final String s, final StartElement element) {
 
         if (MANDATORY_MD_RECORD_NAME.equals(currentMdRecordName)) {
             if ("title".equals(element.getLocalName())
                 && Constants.DC_NS_URI.equals(element.getNamespace())) {
                 dcTitle = s;
             }
         }
         return s;
     }
 
     /**
      * @return Returns metadata attributes.
      */
     public Map<String, Map<String, String>> getMetadataAttributes() {
         return this.metadataAttributes;
     }
 
     /**
      * Retrieves a namespace uri of a child element of "md-record" element,
      * whose attribute "name" set to "escidoc".
      * 
      * @return Namespace of MetadataRecord
      */
     public String getEscidocMetadataRecordNameSpace() {
         return this.escidocMetadataRecordNameSpace;
     }
 
     /**
      * @return the rootPath
      */
     private String getRootPath() {
         return rootPath;
     }
 
     /**
      * @return the mdRecordsPath
      */
     public String getMdRecordsPath() {
         return getRootPath() + "/" + XmlUtility.NAME_MDRECORDS;
     }
 
     /**
      * @return the mdRecordpath
      */
     public String getMdRecordPath() {
         return getMdRecordsPath() + "/" + XmlUtility.NAME_MDRECORD;
     }
 
     /**
      * @return the dcTitle
      */
     public String getDcTitle() {
         return dcTitle;
     }
 
 }
