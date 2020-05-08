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
 package com.flexive.core.storage.binary;
 
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.stream.ServerLocation;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Access to binary files
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public interface BinaryStorage {
 
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
     * @return BinaryInputStream
      */
     BinaryInputStream fetchBinary(int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality);
 
     /**
      * Internal binary update to set binary id and acl for the main data
      *
      * @param con       an open and valid connection
      * @param pk        primary key
      * @param binaryId  id of the binary to set
      * @param binaryACL acl for the binary
      * @throws com.flexive.shared.exceptions.FxUpdateException
      *          on errors
      */
     void updateContentBinaryEntry(Connection con, FxPK pk, long binaryId, long binaryACL) throws FxUpdateException;
 
     /**
      * Transfer a binary from the transit to the 'real' binary table
      *
      * @param con    open and valid connection
      * @param binary the binary descriptor
      * @return descriptor of final binary
      * @throws FxApplicationException on errors looking up the sequencer
      */
     BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary) throws FxApplicationException;
 
     /**
      * Load a binary descriptor
      *
      * @param server server side StreamServers that can provide the binary
      * @param con    open connection
      * @param id     id of the binary (in FX_BINARY table)
      * @return BinaryDescriptor
      * @throws com.flexive.shared.exceptions.FxDbException
      *          on errors
      */
     BinaryDescriptor loadBinaryDescriptor(List<ServerLocation> server, Connection con, long id) throws FxDbException;
 
     /**
      * Prepare and identify a binary
      *
      * @param con         an open and valid Connection
      * @param mimeMetaMap optional mimeMetaMap to avoid saving duplicates, if <code>null</code>, binary will be transfered to the real binary space
      * @param binary      the binary to process (BinaryDescriptor's may be altered or replaced after calling this method!)
      * @throws FxApplicationException on errors
      */
     void prepareBinary(Connection con, Map<String, String[]> mimeMetaMap, FxBinary binary) throws FxApplicationException;
 
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
      * Remove all binary entries for the given primary key (all versions!).
      * Binary references will be set to <code>0</code> to maintain integrity
      *
      * @param con an open and valid connection
      * @param pk  primary key
      * @throws FxApplicationException on errors
      */
     void removeBinariesForPK(Connection con, FxPK pk) throws FxApplicationException;
 
     /**
      * Remove all binary entries for the given primary key (only specified version).
      * Binary references will be set to <code>0</code> to maintain integrity
      *
      * @param con an open and valid connection
      * @param pk  primary key
      * @throws FxApplicationException on errors
      */
     void removeBinariesForVersion(Connection con, FxPK pk) throws FxApplicationException;
 
     /**
      * Remove all binary entries for the given type.
      * Binary references will be set to <code>0</code> to maintain integrity
      *
      * @param con    an open and valid connection
      * @param typeId id of the type
      * @throws FxApplicationException on errors
      */
     void removeBinariesForType(Connection con, long typeId) throws FxApplicationException;
 
     /**
      * Remove all binaries in the transit space that have expired
      *
      * @param con an open and valid connection
      */
     void removeExpiredTransitEntries(Connection con);
 }
