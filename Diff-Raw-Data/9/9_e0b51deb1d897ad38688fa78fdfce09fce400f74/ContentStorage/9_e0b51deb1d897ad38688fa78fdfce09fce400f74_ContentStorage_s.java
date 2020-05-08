 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.storage;
 
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxContentSecurityInfo;
 import com.flexive.shared.content.FxContentVersionInfo;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxProperty;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.structure.UniqueMode;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.core.storage.binary.BinaryInputStream;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * SQL storage interface for contents
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public interface ContentStorage {
 
     /**
      * Get the Database table name for this data type
      *
      * @param prop FxPropery to request the table name for
      * @return table name
      */
     String getTableName(FxProperty prop);
 
     /**
      * Get the (optional) uppercase column used for this data type.
      * If no uppercase column is used, <code>null</code> is returned.
      *
      * @param prop requested FxPropery
      * @return uppercase column name or <code>null</code>
      */
     String getUppercaseColumn(FxProperty prop);
 
     /**
      * Get the (optional) uppercase column used for this data type for search queries.
      * This method handles the special case of HTML columns which use the UFCLOB column
      * to store the extracted data and returns an SQL-UPPER call for the column.
      * If no uppercase column is used, <code>null</code> is returned.
      *
      * @param property requested FxPropery
      * @return uppercase column name or <code>null</code>
      */
     String getQueryUppercaseColumn(FxProperty property);
 
     /**
      * Get the database columns used for this data type
      *
      * @param prop requested FxProperty
      * @return the database columns used
      */
     String[] getColumns(FxProperty prop);
 
 
     /**
      * Get all security relevant information about a content instance identified by its primary key
      *
      * @param con an open and valid connection
      * @param pk  primary key to query security information for
      * @return FxContentSecurityInfo
      * @throws FxLoadException     on errors
      * @throws FxNotFoundException on errors
      */
     FxContentSecurityInfo getContentSecurityInfo(Connection con, FxPK pk) throws FxLoadException, FxNotFoundException;
 
     /**
      * Get information about the versions used for a content id
      *
      * @param con an open and valid Connection
      * @param id  the id to query version information for
      * @return FxContentVersionInfo
      * @throws FxNotFoundException if the requested id does not exist
      */
     FxContentVersionInfo getContentVersionInfo(Connection con, long id) throws FxNotFoundException;
 
 
     /**
      * Create a new content instance
      *
      * @param con     an open and valid Connection
      * @param env     Environment
      * @param sql     optional StringBuilder to save performance
      * @param newId   the new Id to assign
      * @param content the content to persist
      * @return primary key of the created content
      * @throws FxCreateException           on create errors
      * @throws FxInvalidParameterException on invalid values/missing required values in content
      */
     FxPK contentCreate(Connection con, FxEnvironment env, StringBuilder sql, long newId, FxContent content) throws FxCreateException, FxInvalidParameterException;
 
     /**
      * Create a new version for an existing content instance
      *
      * @param con     an open and valid Connection
      * @param env     Environment
      * @param sql     optional StringBuilder to save performance
      * @param content the content to persist
      * @return primary key of the new version
      * @throws FxCreateException           on create errors
      * @throws FxInvalidParameterException on invalid values/missing required values in content
      */
     FxPK contentCreateVersion(Connection con, FxEnvironment env, StringBuilder sql, FxContent content) throws FxCreateException, FxInvalidParameterException;
 
     /**
      * Load a content with the given primary key
      *
      * @param con an open and valid connection
      * @param pk  primary key
      * @param env Environment
      * @param sql optional StringBuilder to save performance
      * @return loaded content
      * @throws FxLoadException             on errors
      * @throws FxInvalidParameterException on invalid parameters (pk)
      * @throws FxNotFoundException         if no instance for this primary key was found
      */
     FxContent contentLoad(Connection con, FxPK pk, FxEnvironment env, StringBuilder sql) throws FxLoadException, FxInvalidParameterException, FxNotFoundException;
 
     /**
      * Save a content instance, creating new versions as needed
      *
      * @param con           an open and valid connection
      * @param env           Environment
      * @param sql           optional StringBuilder to save performance
      * @param content       the content to persist
      * @param fqnPropertyId id of the FQN property, needed to sync changes back to the tree
      * @return primary key of the saved content
      * @throws FxUpdateException           on errors
      * @throws FxInvalidParameterException on errors
      * @throws FxNoAccessException         if property permissions are violated
      */
     FxPK contentSave(Connection con, FxEnvironment env, StringBuilder sql, FxContent content, long fqnPropertyId) throws FxUpdateException, FxInvalidParameterException, FxNoAccessException;
 
     /**
      * Remove a content instance and all its versions, will throw an
      * Exception if it is referenced from other contents.
      *
      * @param con  an open and valid connection
      * @param type FxType
      * @param pk   primary key
      * @throws com.flexive.shared.exceptions.FxRemoveException
      *          on errors
      */
     void contentRemove(Connection con, FxType type, FxPK pk) throws FxRemoveException;
 
     /**
      * Remove a content's version, will throw an
      * Exception if it is referenced from other contents.
      * If the content consists only of this specific version the whole content is removed
      *
      * @param con  an open and valid connection
      * @param type FxType
      * @param pk   primary key for a distinct version @throws com.flexive.shared.exceptions.FxRemoveException
      *             on errors
      * @throws FxNotFoundException on errors
      * @throws FxRemoveException   on errors
      */
     void contentRemoveVersion(Connection con, FxType type, FxPK pk) throws FxRemoveException, FxNotFoundException;
 
     /**
      * Remove all instances of the given type.
      * Beans using this method should apply very strict security restrictions!
      *
      * @param con  an open and valid connection
      * @param type affected FxType
      * @return number of instances removed
      * @throws com.flexive.shared.exceptions.FxRemoveException
      *          on errors
      */
     int contentRemoveForType(Connection con, FxType type) throws FxRemoveException;
 
     /**
      * Get a list of all primary keys for the given FxType (can be a very long list!!!)
      *
      * @param con              an open and valid connection
      * @param type             the type to request the primary keys for
      * @param onePkPerInstance return one primary key per instance (with max version) or one per actual version?
      * @return list containing the primary keys
      * @throws FxDbException on errors
      */
     List<FxPK> getPKsForType(Connection con, FxType type, boolean onePkPerInstance) throws FxDbException;
 
     /**
      * Perform maintenance and cleanup tasks.
      * To be called periodically.
      *
      * @param con an open and valid connection
      */
     void maintenance(Connection con);
 
     /**
      * Prepare a content for a save or create operation (resolves binaries for script processing, etc.)
      *
      * @param con     an open and valid Connection
      * @param content the content to prepare
      * @throws FxInvalidParameterException on errors
      * @throws FxDbException               on errors
      */
     void prepareSave(Connection con, FxContent content) throws FxInvalidParameterException, FxDbException;
 
     //specialized repository methods
 
     /**
      * Receive a binary transit
      *
      * @param divisionId   division
      * @param handle       binary handle
      * @param expectedSize the expected size of the binary
      * @param ttl          time to live in the transit space
      * @return an output stream that receives the binary
      * @throws SQLException on errors
      * @throws IOException  on errors
      */
     OutputStream receiveTransitBinary(int divisionId, String handle, long expectedSize, long ttl) throws SQLException, IOException;
 
     /**
      * Fetch a binary as an InputStream, if the requested binary is not found, return <code>null</code>
      *
      * @param divisionId    division
      * @param size          requested preview size (images only)
      * @param binaryId      id
      * @param binaryVersion version
      * @param binaryQuality quality
     * @return InputStream, if the requested binary is not found <code>null</code>
      */
     BinaryInputStream fetchBinary(int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality);
 
     /**
      * Create a new or update an existing binary
      *
      * @param con     an open and valid Connection
      * @param id      id of the binary
      * @param version version of the binary
      * @param quality quality of the binary
      * @param name    file name
      * @param length  length of the binary
      * @param binary  the binary
      * @throws FxApplicationException on errors
      */
     void storeBinary(Connection con, long id, int version, int quality, String name, long length, InputStream binary) throws FxApplicationException;
 
     /**
      * Create a new or update an existing binary preview
      *
      * @param con     an open and valid Connection
      * @param id      id of the binary
      * @param version version of the binary
      * @param quality quality of the binary
      * @param preview the number of the preview to update (1..3)
      * @param width   width of the preview
      * @param height  height of the preview
      * @param length  length of the binary
      * @param binary  the binary
      * @throws FxApplicationException on errors
      */
     void updateBinaryPreview(Connection con, long id, int version, int quality, int preview, int width, int height, long length, InputStream binary) throws FxApplicationException;
 
     /**
      * Get the number of references that exist for the requested content id
      *
      * @param con an open and valid Connection
      * @param id  id of the requested content
      * @return number of references that exist for the requested content
      * @throws FxDbException on errors
      */
     int getReferencedContentCount(Connection con, long id) throws FxDbException;
 
     /**
      * Update XPath entires of content data
      *
      * @param con           an open and valid connection
      * @param assignmentId  assignment id
      * @param originalXPath original xpath
      * @param newXPath      new xpath
      * @throws FxInvalidParameterException if an XPath is not valid
      * @throws FxUpdateException           on errors
      */
     void updateXPath(Connection con, long assignmentId, String originalXPath, String newXPath) throws FxUpdateException, FxInvalidParameterException;
 
     /**
      * Check if a unique condition is valid for a propery
      *
      * @param con    an open and valid Connection
      * @param mode   UniqueMode
      * @param prop   property
      * @param typeId type
      * @param pk     primary key (optional)
      * @return if mode would be valid
      */
     boolean uniqueConditionValid(Connection con, UniqueMode mode, FxProperty prop, long typeId, FxPK pk);
 
     /**
      * Change an assignments multilanguage setting.
      * Multi to Single: lang=system, values of the def. lang. are used, if other translations exist an exception will be raised
      * Single to Multi: lang=default language
      *
      * @param con             an open and valid Connection
      * @param assignmentId    affected assignment
      * @param orgMultiLang    original multilanguage setting
      * @param newMultiLang    new multilanguage setting
      * @param defaultLanguage default language
      * @throws FxUpdateException on errors
      * @throws SQLException      on errors
      */
     void updateMultilanguageSettings(Connection con, long assignmentId, boolean orgMultiLang, boolean newMultiLang, long defaultLanguage) throws FxUpdateException, SQLException;
 
     /**
      * Prepare, identify and transfer a binary to the binary space
      *
      * @param con    an open and valid Connection
      * @param binary the binary to process (BinaryDescriptor's may be altered or replaced after calling this method!)
      * @throws java.sql.SQLException  on errors
      * @throws FxApplicationException on errors
      */
     void prepareBinary(Connection con, FxBinary binary) throws SQLException, FxApplicationException;
 
     /**
      * Get the number of instances for a given type
      *
      * @param con    an open and valid db connection
      * @param typeId the type's id
      * @return the number of content instances found for the given type id
      * @throws SQLException on errors
      */
     long getTypeInstanceCount(Connection con, long typeId) throws SQLException;
 }
