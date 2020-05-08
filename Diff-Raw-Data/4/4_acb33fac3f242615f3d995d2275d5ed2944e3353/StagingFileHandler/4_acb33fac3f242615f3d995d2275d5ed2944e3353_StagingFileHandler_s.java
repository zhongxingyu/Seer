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
 package de.escidoc.core.st.business;
 
 import de.escidoc.core.common.business.Constants;
 import de.escidoc.core.common.business.fedora.EscidocBinaryContent;
 import de.escidoc.core.common.exceptions.application.missing.MissingMethodParameterException;
 import de.escidoc.core.common.exceptions.application.notfound.StagingFileNotFoundException;
 import de.escidoc.core.common.exceptions.application.security.AuthenticationException;
 import de.escidoc.core.common.exceptions.application.security.AuthorizationException;
 import de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException;
 import de.escidoc.core.common.exceptions.system.SystemException;
 import de.escidoc.core.common.exceptions.system.WebserverSystemException;
 import de.escidoc.core.common.util.string.StringUtility;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import de.escidoc.core.st.business.interfaces.StagingFileHandlerInterface;
 import de.escidoc.core.st.business.persistence.StagingFileDao;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 
 /**
  * Staging File Handler implementation.
  * 
  * @author Torsten Tetteroo
  */
 @Service("business.StagingFileHandler")
 public class StagingFileHandler implements StagingFileHandlerInterface {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(StagingFileHandler.class);
 
     @Autowired
     @Qualifier("persistence.StagingFileDao")
     private StagingFileDao dao;
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.st.service.interfaces.StagingFileHandlerInterface
      *      #create(de.escidoc.core.om.service.result.EscidocBinaryContent)
      */
     @Override
     public String create(final EscidocBinaryContent binaryContent) throws MissingMethodParameterException,
         AuthenticationException, AuthorizationException, SqlDatabaseSystemException, WebserverSystemException {
 
         StagingFile stagingFile = StagingUtil.generateStagingFile(true, this.dao);
         if (stagingFile == null) {
             throw new MissingMethodParameterException("Missing staging file.");
         }
 
         if (binaryContent == null || binaryContent.getContent() == null) {
             throw new MissingMethodParameterException("Binary content must be provided.");
         }
         String token = stagingFile.getToken();
         stagingFile.setReference(StagingUtil.concatenatePath(StagingUtil.getUploadStagingArea(), token));
 
         try {
             stagingFile.read(binaryContent.getContent());
         }
         catch (final IOException e) {
             throw new MissingMethodParameterException("Binary content must be provided.", e);
         }
         stagingFile.setMimeType(binaryContent.getMimeType());
         dao.update(stagingFile);
 
         final ByteArrayOutputStream out = new ByteArrayOutputStream();
         try {
             final XMLStreamWriter writer = XmlUtility.createXmlStreamWriter(out);
 
             writer.setPrefix("xlink", Constants.XLINK_NS_URI);
             writer.setPrefix("xml", Constants.XML_NS_URI);
             writer.setDefaultNamespace(Constants.STAGING_FILE_NS_URI);
             writer.setPrefix("staging-file", Constants.STAGING_FILE_NS_URI);
 
             writer.writeStartElement("staging-file", "staging-file", Constants.STAGING_FILE_NS_URI);
             XmlUtility.addXmlBaseAttribute(writer);
             writer.writeDefaultNamespace(Constants.STAGING_FILE_NS_URI);
             writer.writeNamespace("staging-file", Constants.STAGING_FILE_NS_URI);
             writer.writeNamespace("xlink", Constants.XLINK_NS_URI);
             XmlUtility.addXlinkAttributes(writer, null, "/st/staging-file/" + token);
             XmlUtility.addLastModificationDateAttribute(writer, new DateTime());
 
             writer.writeEndElement();
             writer.writeEndDocument();
             writer.flush();
 
             return out.toString(XmlUtility.CHARACTER_ENCODING);
         }
         catch (final XMLStreamException e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
         catch (final IOException e) {
             throw new WebserverSystemException(e.getMessage(), e);
         }
         finally {
             try {
                 if (binaryContent.getContent() != null) {
                     binaryContent.getContent().close();
                 }
             }
             catch (final IOException e) {
                 LOGGER.error("error on closing stream", e);
             }
         }
     }
 
     /**
      * See Interface for functional description.
      * 
      * @see de.escidoc.core.st.service.interfaces.StagingFileHandlerInterface #retrieve(java.lang.String)
      */
     @Override
     public EscidocBinaryContent retrieve(final String stagingFileId) throws StagingFileNotFoundException,
         AuthenticationException, AuthorizationException, MissingMethodParameterException, SystemException,
         SqlDatabaseSystemException {
 
         final StagingFile stagingFile = getStagingFile(stagingFileId);
         final EscidocBinaryContent binaryContent = new EscidocBinaryContent();
         binaryContent.setMimeType(stagingFile.getMimeType());
         binaryContent.setFileName(stagingFile.getReference());
         try {
             binaryContent.setContent(stagingFile.getFileInputStream());
         }
         catch (final IOException e) {
             throw new StagingFileNotFoundException("Binary content of addressed staging file cannot be found.", e);
         }
 
         // finally, the staging file is set to expired to prevent further
         // accesses to the binary content.
         stagingFile.setExpiryTs(System.currentTimeMillis());
         dao.update(stagingFile);
 
         return binaryContent;
     }
 
     /**
      * Setter for the dao.
      * 
      * @param dao
      *            The data access object.
      */
     public void setDao(final StagingFileDao dao) {
         this.dao = dao;
     }
 
     /**
      * Retrieve the staging file with the provided id.
      * 
      * @param stagingFileId
      *            The StagingFile id.
      * @return The staging file.
      * @throws MissingMethodParameterException
      *             Thrown in case of missing id.
      * @throws StagingFileNotFoundException
      *             Thrown if no staging file with provided id exists.
      * @throws de.escidoc.core.common.exceptions.system.SqlDatabaseSystemException
      */
     private StagingFile getStagingFile(final String stagingFileId) throws MissingMethodParameterException,
         StagingFileNotFoundException, SqlDatabaseSystemException {
 
         if (stagingFileId == null) {
             throw new MissingMethodParameterException("staging file id must be provided.");
         }
 
         final StagingFile result = dao.findStagingFile(stagingFileId);
         if (result == null || result.isExpired()) {
             throw new StagingFileNotFoundException(StringUtility.format(
                 "Provided id does not match valid staging file.", stagingFileId));
         }
         return result;
     }
 
 }
