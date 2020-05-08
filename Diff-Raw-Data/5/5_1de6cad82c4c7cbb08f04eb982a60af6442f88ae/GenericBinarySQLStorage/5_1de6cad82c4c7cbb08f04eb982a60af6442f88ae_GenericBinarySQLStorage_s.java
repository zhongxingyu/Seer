 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
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
 package com.flexive.core.storage.genericSQL;
 
 import com.flexive.core.Database;
 import com.flexive.core.DatabaseConst;
 import com.flexive.core.storage.StorageManager;
 import com.flexive.core.storage.binary.BinaryInputStream;
 import com.flexive.core.storage.binary.BinaryStorage;
 import com.flexive.core.storage.binary.BinaryTransitFileInfo;
 import com.flexive.core.storage.binary.FxBinaryUtils;
 import com.flexive.shared.*;
 import com.flexive.shared.configuration.SystemParameters;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxDbException;
 import com.flexive.shared.exceptions.FxUpdateException;
 import com.flexive.shared.interfaces.DivisionConfigurationEngine;
 import com.flexive.shared.interfaces.ScriptingEngine;
 import com.flexive.shared.media.FxMediaEngine;
 import com.flexive.shared.media.impl.FxMimeType;
 import com.flexive.shared.scripting.FxScriptBinding;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptResult;
 import com.flexive.shared.stream.BinaryUploadPayload;
 import com.flexive.shared.stream.FxStreamUtils;
 import com.flexive.shared.structure.FxDataType;
 import com.flexive.shared.structure.FxPropertyAssignment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxBinary;
 import com.flexive.stream.ServerLocation;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.*;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static com.flexive.core.DatabaseConst.*;
 import static com.flexive.shared.value.BinaryDescriptor.PreviewSizes;
 import static com.flexive.shared.value.BinaryDescriptor.SYS_UNKNOWN;
 import static org.apache.commons.lang.StringUtils.defaultString;
 
 /**
  * Generic SQL based implementation to access binaries
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class GenericBinarySQLStorage implements BinaryStorage {
 
     private static final Log LOG = LogFactory.getLog(GenericBinarySQLStorage.class);
 
     protected static final String CONTENT_MAIN_BINARY_UPDATE = "UPDATE " + TBL_CONTENT + " SET " +
             //       1          2              3          4          5         6
             "DBIN_ID=?,DBIN_VER=?,DBIN_QUALITY=?,DBIN_ACL=? WHERE ID=? AND VER=?";
 
     //                                                       1    2        3          4        5       6          7     8      9                                                            1         2             3
     protected static final String BINARY_DESC_LOAD = "SELECT NAME,BLOBSIZE,CREATED_AT,MIMETYPE,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,MD5SUM FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
     protected static final String BINARY_META_LOAD = "SELECT XMLMETA FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?";
 
     //    select into xx () select  FBLOB1,FBLOB2,?,?,?
     protected static final String BINARY_TRANSIT_HEADER = "SELECT FBLOB,MIMETYPE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     protected static final String BINARY_TRANSIT_MIMETYPE = "SELECT MIMETYPE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                   1           2         3         4         5
     protected static final String BINARY_TRANSIT_PREVIEW_SIZES = "SELECT PREVIEW_REF,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
 
     protected static final String BINARY_TRANSIT = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,MD5SUM,PREV1,PREV2,PREV3,PREV4) " +
             //      1 2 3       4 5 6     7                                         8             9 0 1 2
             "SELECT ?,?,?,FBLOB,?,?,?," + StorageManager.getTimestampFunction() + ",?,PREVIEW_REF,?,?,?,?,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,?";
     protected static final String BINARY_TRANSIT_REPLACE = "UPDATE " + TBL_CONTENT_BINARY + " SET FBLOB=";
     protected static final String BINARY_TRANSIT_REPLACE_FBLOB_COPY = "(SELECT DISTINCT FBLOB FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?)";
     protected static final String BINARY_TRANSIT_REPLACE_FBLOB_PARAM = "?";
     //                                                                   2          3         4          5             6         7            8       9        10       11         12        13            14            15
     protected static final String BINARY_TRANSIT_REPLACE_PARAMS = ",NAME=?,BLOBSIZE=?,XMLMETA=?,MIMETYPE=?,PREVIEW_REF=?,ISIMAGE=?,RESOLUTION=?,WIDTH=?,HEIGHT=?,MD5SUM=? WHERE ID=? AND VER=? AND QUALITY=?";
     protected static final String BINARY_TRANSIT_FILESYSTEM = "INSERT INTO " + TBL_CONTENT_BINARY + "(ID,VER,QUALITY,FBLOB,NAME,BLOBSIZE,XMLMETA,CREATED_AT,MIMETYPE,PREVIEW_REF,ISIMAGE,RESOLUTION,WIDTH,HEIGHT,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,MD5SUM,PREV1,PREV2,PREV3,PREV4) " +
             //      1 2 3 4 5 6 7     8                                         9             0 1 2 3                                                                                                                                                  
             "SELECT ?,?,?,?,?,?,?," + StorageManager.getTimestampFunction() + ",?,PREVIEW_REF,?,?,?,?,PREV1_WIDTH,PREV1_HEIGHT,PREV2_WIDTH,PREV2_HEIGHT,PREV3_WIDTH,PREV3_HEIGHT,PREV4_WIDTH,PREV4_HEIGHT,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,?";
 
     protected static final String BINARY_TRANSIT_PREVIEW_WHERE = " FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?";
     //                                                                                                   1              2               3        4              5               6        7              8               9        10             11              12           13           14           15           16           17
     protected static final String BINARY_TRANSIT_PREVIEWS = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREV1=?, PREV1_WIDTH=?, PREV1_HEIGHT=?, PREV2=?, PREV2_WIDTH=?, PREV2_HEIGHT=?, PREV3=?, PREV3_WIDTH=?, PREV3_HEIGHT=?, PREV4=?, PREV4_WIDTH=?, PREV4_HEIGHT=?, PREV1SIZE=?, PREV2SIZE=?, PREV3SIZE=?, PREV4SIZE=? WHERE BKEY=?";
     protected static final String BINARY_TRANSIT_PREVIEWS_REF = "UPDATE " + TBL_BINARY_TRANSIT + " SET PREVIEW_REF=? WHERE BKEY=?";
 
     protected static final String CONTENT_BINARY_TRANSIT_CLEANUP = "DELETE FROM " + TBL_BINARY_TRANSIT + " WHERE EXPIRE<?";
     protected static final String CONTENT_BINARY_REMOVE_ID = "DELETE FROM " + TBL_CONTENT_BINARY + " WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " WHERE FBLOB IS NOT NULL AND ID=?";
     protected static final String CONTENT_BINARY_REMOVE_TYPE_GET = "SELECT DISTINCT FBLOB FROM " + TBL_CONTENT_DATA + " d, " + TBL_CONTENT + " c WHERE FBLOB IS NOT NULL AND d.ID=c.ID and c.TDEF=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_ID = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_RESET_ID = "UPDATE " + TBL_CONTENT + " SET DBIN_ID=-1 WHERE ID=?";
     protected static final String CONTENT_BINARY_REMOVE_RESETDATA_TYPE = "UPDATE " + TBL_CONTENT_DATA + " SET FBLOB=NULL WHERE ID IN (SELECT DISTINCT ID FROM " + TBL_CONTENT + " WHERE TDEF=?)";
     protected static final String CONTENT_BINARY_REMOVE_RESET_TYPE = "UPDATE " + TBL_CONTENT + " SET DBIN_ID=-1 WHERE TDEF=?";
 
     /**
      * Are statements like "insert into ... select ?,... from ..." allowed where ? directly sets the binary stream?
      *
      * @return if such a statement is allowed
      */
     protected boolean blobInsertSelectAllowed() {
         return true;
     }
 
     /**
      * Set a big(long) string value, implementations may differ by used database
      *
      * @param ps   the prepared statement to operate on
      * @param pos  argument position
      * @param data the big string to set
      * @throws SQLException on errors
      */
     protected void setBigString(PreparedStatement ps, int pos, String data) throws SQLException {
         //default implementation using PreparedStatement#setString
         ps.setString(pos, data);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public OutputStream receiveTransitBinary(int divisionId, String handle, String mimeType, long expectedSize, long ttl) throws SQLException, IOException {
         try {
             if (EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB))
                 return new GenericBinarySQLOutputStream(divisionId, handle, mimeType, expectedSize, ttl);
             else {
                 Connection con;
                 PreparedStatement ps = null;
                 con = Database.getNonTXDataSource(divisionId).getConnection();
                 try {
                     //create a dummy entry
                     ps = con.prepareStatement("INSERT INTO " + DatabaseConst.TBL_BINARY_TRANSIT + " (BKEY,MIMETYPE,FBLOB,TFER_DONE,EXPIRE) VALUES(?,?,NULL,?,?)");
                     ps.setString(1, handle);
                     ps.setString(2, mimeType);
                     ps.setBoolean(3, true);
                     ps.setLong(4, System.currentTimeMillis() + ttl);
                     ps.executeUpdate();
                 } finally {
                     Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
                 }
                 return new FileOutputStream(FxBinaryUtils.createTransitFile(divisionId, handle, ttl));
             }
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     public BinaryInputStream fetchBinary(Connection con, int divisionId, BinaryDescriptor.PreviewSizes size, long binaryId, int binaryVersion, int binaryQuality) {
         Connection _con = con;
         PreparedStatement ps = null;
         String mimeType;
         int datasize;
         try {
             if (_con == null)
                 _con = Database.getDbConnection(divisionId);
             String column = "FBLOB";
             String sizeColumn = "BLOBSIZE";
             long previewId = 0;
             ResultSet rs;
             if (size != PreviewSizes.ORIGINAL) {
                 //unless the real content is requested, try to find the correct preview image
                 //                                 1           2         3         4         5         6     7
                 ps = _con.prepareStatement("SELECT PREVIEW_REF,PREV1SIZE,PREV2SIZE,PREV3SIZE,PREV4SIZE,WIDTH,HEIGHT FROM " + TBL_CONTENT_BINARY +
                         " WHERE ID=? AND VER=? AND QUALITY=?");
                 ps.setLong(1, binaryId);
                 ps.setInt(2, binaryVersion);
                 ps.setInt(3, binaryQuality);
                 rs = ps.executeQuery();
                 if (rs != null && rs.next()) {
                     previewId = rs.getLong(1);
                     if (rs.wasNull())
                         previewId = 0;
                     boolean found = previewId == 0;
                     if (!found) { //fall back to referenced preview
                         rs.close();
                         ps.setLong(1, previewId);
                         ps.setInt(2, binaryVersion);
                         ps.setInt(3, binaryQuality);
                         rs = ps.executeQuery();
                         found = rs != null && rs.next();
                     }
                     if (found)
                         size = getAvailablePreviewSize(size, rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5), rs.getInt(6), rs.getInt(7));
                 }
                 ps.close();
             }
             switch (size) {
                 case PREVIEW1:
                     column = "PREV1";
                     sizeColumn = "PREV1SIZE";
                     break;
                 case PREVIEW2:
                     column = "PREV2";
                     sizeColumn = "PREV2SIZE";
                     break;
                 case PREVIEW3:
                     column = "PREV3";
                     sizeColumn = "PREV3SIZE";
                     break;
                 case SCREENVIEW:
                     column = "PREV4";
                     sizeColumn = "PREV4SIZE";
                     break;
             }
             ps = _con.prepareStatement("SELECT " + column + ",MIMETYPE," + sizeColumn + " FROM " + TBL_CONTENT_BINARY +
                     " WHERE ID=? AND VER=? AND QUALITY=?");
             if (previewId != 0)
                 ps.setLong(1, previewId);
             else
                 ps.setLong(1, binaryId);
             ps.setInt(2, binaryVersion);
             ps.setInt(3, binaryQuality);
             rs = ps.executeQuery();
             if (rs == null || !rs.next()) {
                 Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                 return new GenericBinarySQLInputStream(false);
             }
             InputStream bin = rs.getBinaryStream(1);
             if (rs.wasNull()) {
                 //try from filesystem
                 File fsBinary = FxBinaryUtils.getBinaryFile(divisionId, binaryId, binaryVersion, binaryQuality, size.getBlobIndex());
 
                 try {
                     if (fsBinary != null)
                         bin = new FileInputStream(fsBinary);
                     else {
                         if (size == PreviewSizes.SCREENVIEW) {
                             //since screenview is a new preview size, it might not exist in old versions. Fall back to Preview3
                             Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                             LOG.warn("Screenview for binary #" + binaryId + " not found! Falling back to Preview3 size.");
                             return this.fetchBinary(null, divisionId, PreviewSizes.PREVIEW3, binaryId, binaryVersion, binaryQuality);
                         }
                         LOG.error("Binary file #" + binaryId + "[" + size.name() + "] was not found!");
                         Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
                         return new GenericBinarySQLInputStream(false);
                     }
                 } catch (FileNotFoundException e) {
                     LOG.error("Binary not found on filesystem! Id=" + binaryId + ", version=" + binaryVersion + ", quality=" + binaryQuality + ", size=" + size.name());
                     Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
                     return new GenericBinarySQLInputStream(false);
                 }
             }
             mimeType = rs.getString(2);
             datasize = rs.getInt(3);
             if (rs.wasNull() && size == PreviewSizes.SCREENVIEW) {
                 Database.closeObjects(GenericBinarySQLInputStream.class, _con, ps);
                 LOG.warn("Screenview for binary #" + binaryId + " not found! Falling back to Preview3 size.");
                 return this.fetchBinary(null, divisionId, PreviewSizes.PREVIEW3, binaryId, binaryVersion, binaryQuality);
             }
             return new GenericBinarySQLInputStream(_con, ps, true, bin, mimeType, datasize);
         } catch (SQLException e) {
             Database.closeObjects(GenericBinarySQLInputStream.class, con == null ? _con : null, ps);
             return new GenericBinarySQLInputStream(false);
         }
     }
 
     /**
      * "Downgrade" the requested size to the first available size
      *
      * @param requestedSize requested size
      * @param prev1size     length of preview 1
      * @param prev2size     length of preview 2
      * @param prev3size     length of preview 3
      * @param prev4size     length of preview 4 (screenview)
      * @param width         original width
      * @param height        original height
      * @return downgraded previewsize
      */
     private PreviewSizes getAvailablePreviewSize(PreviewSizes requestedSize, int prev1size, int prev2size, int prev3size,
                                                  int prev4size, int width, int height) {
         PreviewSizes result = requestedSize;
         if (result == PreviewSizes.SCREENVIEW && prev4size == 0) {
             result = PreviewSizes.PREVIEW3;
             if (width < BinaryDescriptor.SCREENVIEW_WIDTH && height < BinaryDescriptor.SCREENVIEW_HEIGHT)
                 result = PreviewSizes.ORIGINAL;
         }
         if (result == PreviewSizes.PREVIEW3 && prev3size == 0) {
             result = PreviewSizes.PREVIEW2;
             if (width < result.getSize() || height < result.getSize())
                 result = PreviewSizes.ORIGINAL;
         }
         if (result == PreviewSizes.PREVIEW2 && prev2size == 0) {
             result = PreviewSizes.PREVIEW1;
             if (width < result.getSize() || height < result.getSize())
                 result = PreviewSizes.ORIGINAL;
         }
         if (result == PreviewSizes.PREVIEW1 && prev1size == 0)
             result = PreviewSizes.ORIGINAL;
         if (LOG.isDebugEnabled() && result != requestedSize)
             LOG.debug("Delivering PreviewSize " + result + " for " + requestedSize);
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateContentBinaryEntry(Connection con, FxPK pk, long binaryId, long binaryACL) throws FxUpdateException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_MAIN_BINARY_UPDATE);
             ps.setLong(1, binaryId);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //quality
             ps.setLong(4, binaryACL);
             ps.setLong(5, pk.getId());
             ps.setInt(6, pk.getVersion());
             ps.executeUpdate();
         } catch (SQLException e) {
             throw new FxUpdateException(LOG, e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public BinaryDescriptor loadBinaryDescriptor(List<ServerLocation> server, Connection con, long id) throws FxDbException {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(BINARY_DESC_LOAD);
             ps.setLong(1, id);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //ver
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return new BinaryDescriptor(server, id, 1, 1, rs.getLong(3), rs.getString(1), rs.getLong(2), null,
                         rs.getString(4), rs.getBoolean(5), rs.getDouble(6), rs.getInt(7), rs.getInt(8), rs.getString(9));
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, ps);
         }
         throw new FxDbException("ex.content.binary.loadDescriptor.failed", id);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getBinaryMetaData(Connection con, long binaryId) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(BINARY_META_LOAD);
             ps.setLong(1, binaryId);
             ps.setInt(2, 1); //ver
             ps.setInt(3, 1); //ver
             ResultSet rs = ps.executeQuery();
             if (rs != null && rs.next()) {
                 return rs.getString(1);
             }
         } catch (SQLException e) {
             LOG.error(e);
         } finally {
             try {
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 LOG.error(e);
             }
         }
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     public BinaryDescriptor binaryTransit(Connection con, BinaryDescriptor binary) throws FxApplicationException {
         long id = EJBLookup.getSequencerEngine().getId(FxSystemSequencer.BINARY);
         return binaryTransit(con, binary, id, 1, 1);
     }
 
     /**
      * Transfer a binary from the transit to the 'real' binary table
      *
     * @param con     open and valid connection
      * @param binary  the binary descriptor
      * @param id      desired id
      * @param version desired version
      * @param quality desired quality
      * @return descriptor of final binary
      * @throws FxDbException on errors looking up the sequencer
      */
     private BinaryDescriptor binaryTransit(Connection _con, BinaryDescriptor binary, long id, int version, int quality) throws FxDbException {
         PreparedStatement ps = null;
         BinaryDescriptor created;
         FileInputStream fis = null;
         boolean dbTransit;
         boolean dbStorage;
         final long dbThreshold;
         final long dbPreviewThreshold;
         final int divisionId = FxContext.get().getDivisionId();
         try {
             final DivisionConfigurationEngine divisionConfig = EJBLookup.getDivisionConfigurationEngine();
             dbTransit = divisionConfig.get(SystemParameters.BINARY_TRANSIT_DB);
             if (id >= 0) {
                 dbThreshold = divisionConfig.get(SystemParameters.BINARY_DB_THRESHOLD);
                 dbPreviewThreshold = divisionConfig.get(SystemParameters.BINARY_DB_PREVIEW_THRESHOLD);
             } else {
                 //force storage of system binaries in the database
                 dbThreshold = -1;
                 dbPreviewThreshold = -1;
             }
             dbStorage = dbThreshold < 0 || binary.getSize() < dbThreshold;
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         Connection con = null;
         try {
             con = Database.getNonTXDataSource(divisionId).getConnection();
             con.setAutoCommit(false);
             double resolution = 0.0;
             int width = 0;
             int height = 0;
             boolean isImage = binary.getMimeType().startsWith("image/");
             if (isImage) {
                 try {
                     width = Integer.parseInt(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "width"), "0"));
                     height = Integer.parseInt(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "height"), "0"));
                     resolution = Double.parseDouble(defaultString(FxXMLUtils.getElementData(binary.getMetadata(), "xResolution"), "0"));
                 } catch (NumberFormatException e) {
                     //ignore
                     LOG.warn(e, e);
                 }
             }
             created = new BinaryDescriptor(CacheAdmin.getStreamServers(), id, version, quality, System.currentTimeMillis(),
                     binary.getName(), binary.getSize(), binary.getMetadata(), binary.getMimeType(), isImage, resolution, width, height, binary.getMd5sum());
             //we can copy the blob directly into the binary table if the database is used for transit and the final binary is
             //stored in the filesystem
             final boolean copyBlob = dbTransit && dbStorage;
             boolean storePrev1FS = false, storePrev2FS = false, storePrev3FS = false, storePrev4FS = false;
             long prev1Length = -1, prev2Length = -1, prev3Length = -1, prev4Length = -1;
             if (dbPreviewThreshold >= 0) {
                 //we have to check if preview should be stored on the filesystem
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEW_SIZES);
                 ps.setString(1, binary.getHandle());
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next())
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
                 rs.getLong(1); //check if previewref is null
                 if (rs.wasNull()) {
                     //if previews are not referenced, check thresholds
                     storePrev1FS = (prev1Length = rs.getLong(2)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev2FS = (prev2Length = rs.getLong(3)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev3FS = (prev3Length = rs.getLong(4)) >= dbPreviewThreshold && !rs.wasNull();
                     storePrev4FS = (prev4Length = rs.getLong(5)) >= dbPreviewThreshold && !rs.wasNull();
                 }
             }
             if (ps != null)
                 ps.close();
             String previewSelect = (storePrev1FS ? ",NULL" : ",PREV1") + (storePrev2FS ? ",NULL" : ",PREV2") + (storePrev3FS ? ",NULL" : ",PREV3") + (storePrev4FS ? ",NULL" : ",PREV4");
             //check if the binary is to be replaced
             ps = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_BINARY + " WHERE ID=? AND VER=? AND QUALITY=?");
             ps.setLong(1, created.getId());
             ps.setInt(2, created.getVersion()); //version
             ps.setInt(3, created.getQuality()); //quality
             ResultSet rsExist = ps.executeQuery();
             final boolean replaceBinary = rsExist != null && rsExist.next() && rsExist.getLong(1) > 0;
             ps.close();
             int paramIndex = 1;
             if( replaceBinary ) {
                 ps = con.prepareStatement(BINARY_TRANSIT_REPLACE + (copyBlob ? BINARY_TRANSIT_REPLACE_FBLOB_COPY : BINARY_TRANSIT_REPLACE_FBLOB_PARAM) + BINARY_TRANSIT_REPLACE_PARAMS);
                 FxBinaryUtils.removeBinary(divisionId, created.getId());
             } else {
                 ps = con.prepareStatement((copyBlob ? BINARY_TRANSIT : BINARY_TRANSIT_FILESYSTEM) + previewSelect + BINARY_TRANSIT_PREVIEW_WHERE);
                 ps.setLong(paramIndex++, created.getId());
                 ps.setInt(paramIndex++, created.getVersion()); //version
                 ps.setInt(paramIndex++, created.getQuality()); //quality
             }
             File binaryTransit = null;
             boolean removeTransitFile = false;
             if (dbTransit) {
                 //transit is handled in the database
                 try {
                     if (!dbStorage) {
                         //binaries are stored on the filesystem
                         binaryTransit = getBinaryTransitFileInfo(binary).getBinaryTransitFile();
                         removeTransitFile = true; //have to clean up afterwards since its a temporary file we get
                     }
                 } catch (FxApplicationException e) {
                     if (e instanceof FxDbException)
                         throw (FxDbException) e;
                     throw new FxDbException(e);
                 }
             } else {
                 //transit file resides on the local file system
                 binaryTransit = FxBinaryUtils.getTransitFile(divisionId, binary.getHandle());
                 if (binaryTransit == null)
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
             }
 
             boolean needExplicitBlobInsert = false;
             if( copyBlob && replaceBinary )
                 ps.setString(paramIndex++, binary.getHandle());
             if (!copyBlob) {
                 //we do not perform a simple blob copy operation in the database
                 if (dbStorage) {
                     //binary is stored in the database -> copy it from the transit file (might be a temp. file)
                     if (blobInsertSelectAllowed()) {
                         fis = new FileInputStream(binaryTransit);
                         ps.setBinaryStream(paramIndex++, fis, (int) binaryTransit.length());
                     } else {
                         ps.setNull(paramIndex++, Types.BINARY);
                         needExplicitBlobInsert = true;
                     }
                 } else {
                     //binary is stored on the filesystem -> copy transit file to binary storage file
                     try {
                         if (!FxFileUtils.copyFile(binaryTransit,
                                 FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                         PreviewSizes.ORIGINAL.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId());
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId(), e.getMessage());
                     }
                     ps.setNull(paramIndex++, Types.BINARY);
                 }
             }
 
 //            int cnt = paramIndex; //copyBlob ? 4 : 5;
             ps.setString(paramIndex++, created.getName());
             ps.setLong(paramIndex++, created.getSize());
             setBigString(ps, paramIndex++, created.getMetadata());
             ps.setString(paramIndex++, created.getMimeType());
             if (replaceBinary)
                 ps.setNull(paramIndex++, java.sql.Types.NUMERIC); //set preview ref to null
             ps.setBoolean(paramIndex++, created.isImage());
             ps.setDouble(paramIndex++, created.getResolution());
             ps.setInt(paramIndex++, created.getWidth());
             ps.setInt(paramIndex++, created.getHeight());
             ps.setString(paramIndex++, created.getMd5sum());
             if( replaceBinary ) {
                 ps.setLong(paramIndex++, created.getId());
                 ps.setInt(paramIndex++, created.getVersion()); //version
                 ps.setInt(paramIndex, created.getQuality()); //quality
             } else
                 ps.setString(paramIndex, binary.getHandle());
             ps.executeUpdate();
             if (needExplicitBlobInsert) {
                 ps.close();
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_BINARY + " SET FBLOB=? WHERE ID=? AND VER=? AND QUALITY=?");
                 fis = new FileInputStream(binaryTransit);
                 ps.setBinaryStream(1, fis, (int) binaryTransit.length());
                 ps.setLong(2, created.getId());
                 ps.setInt(3, created.getVersion()); //version
                 ps.setInt(4, created.getQuality()); //quality
                 ps.executeUpdate();
             }
             if (removeTransitFile && binaryTransit != null) {
                 //transit file was a temp. file -> got to clean up
                 FxFileUtils.removeFile(binaryTransit);
             }
 
             if (replaceBinary) {
                 ps.close();
                 //set all preview entries to the values provided by the transit table
                 ps = con.prepareStatement("UPDATE " + TBL_CONTENT_BINARY + " SET PREV1=NULL,PREV2=NULL,PREV3=NULL,PREV4=NULL WHERE ID=? AND VER=? AND QUALITY=?");
                 ps.setLong(1, created.getId());
                 ps.setInt(2, created.getVersion()); //version
                 ps.setInt(3, created.getQuality()); //quality
                 ps.executeUpdate();
                 ps.close();
                 ps = con.prepareStatement("SELECT PREV1_WIDTH,PREV1_HEIGHT,PREV1SIZE,PREV2_WIDTH,PREV2_HEIGHT,PREV2SIZE,PREV3_WIDTH,PREV3_HEIGHT,PREV3SIZE,PREV4_WIDTH,PREV4_HEIGHT,PREV4SIZE FROM " + TBL_BINARY_TRANSIT + " WHERE BKEY=?");
                 ps.setString(1, binary.getHandle());
                 ResultSet rsPrev = ps.executeQuery();
                 if (rsPrev != null && rsPrev.next()) {
                     long[] data = new long[12];
                     for (int d = 0; d < 12; d++)
                         data[d] = rsPrev.getLong(d + 1);
                     ps.close();
                     ps = con.prepareStatement("UPDATE " + TBL_CONTENT_BINARY + " SET PREV1_WIDTH=?,PREV1_HEIGHT=?,PREV1SIZE=?,PREV2_WIDTH=?,PREV2_HEIGHT=?,PREV2SIZE=?,PREV3_WIDTH=?,PREV3_HEIGHT=?,PREV3SIZE=?,PREV4_WIDTH=?,PREV4_HEIGHT=?,PREV4SIZE=? WHERE ID=? AND VER=? AND QUALITY=?");
                     for (int d = 0; d < 12; d++)
                         ps.setLong(d + 1, data[d]);
                     ps.setLong(13, created.getId());
                     ps.setInt(14, created.getVersion()); //version
                     ps.setInt(15, created.getQuality()); //quality
                     ps.executeUpdate();
                 }
             }
 
             //finally fetch the preview blobs from transit and store them on the filesystem if required
             if (storePrev1FS || storePrev2FS || storePrev3FS || storePrev4FS) {
                 ps.close();
                 previewSelect = (!storePrev1FS ? ",NULL" : ",PREV1") + (!storePrev2FS ? ",NULL" : ",PREV2") + (!storePrev3FS ? ",NULL" : ",PREV3") + (!storePrev4FS ? ",NULL" : ",PREV4");
                 ps = con.prepareStatement("SELECT " + previewSelect.substring(1) + BINARY_TRANSIT_PREVIEW_WHERE);
                 ps.setString(1, binary.getHandle());
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next())
                     throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
                 if (storePrev1FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev1Length, rs.getBinaryStream(1), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW1.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW1.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW1.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev2FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev2Length, rs.getBinaryStream(2), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW2.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW2.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW2.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev3FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev3Length, rs.getBinaryStream(3), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.PREVIEW3.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.PREVIEW3.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.PREVIEW3.getBlobIndex() + "]", e.getMessage());
                     }
                 if (storePrev4FS)
                     try {
                         if (!FxFileUtils.copyStream2File(prev4Length, rs.getBinaryStream(4), FxBinaryUtils.createBinaryFile(divisionId, created.getId(), created.getVersion(), created.getQuality(),
                                 PreviewSizes.SCREENVIEW.getBlobIndex())))
                             throw new FxDbException(LOG, "ex.content.binary.fsCopyFailed", created.getId() + "[" + PreviewSizes.SCREENVIEW.getBlobIndex() + "]");
                     } catch (IOException e) {
                         throw new FxDbException(LOG, "ex.content.binary.fsCopyFailedError", created.getId() + "[" + PreviewSizes.SCREENVIEW.getBlobIndex() + "]", e.getMessage());
                     }
             }
             con.commit();
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } catch (FileNotFoundException e) {
             throw new FxDbException(e, "ex.content.binary.IOError", binary.getHandle());
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
             FxSharedUtils.close(fis);
         }
         return created;
     }
 
     /**
      * {@inheritDoc}
      */
     public void prepareBinary(Connection con, Map<String, String[]> mimeMetaMap, FxBinary binary) throws FxApplicationException {
         for (long lang : binary.getTranslatedLanguages()) {
             BinaryDescriptor bd = binary.getTranslation(lang);
             if (bd.isEmpty()) {
                 //remove empty languages to prevent further processing (FX-327)
                 binary.removeLanguage(lang);
                 continue;
             }
             if (!bd.isNewBinary())
                 continue;
             if (mimeMetaMap != null && mimeMetaMap.containsKey(bd.getHandle())) {
                 String[] mm = mimeMetaMap.get(bd.getHandle());
                 BinaryDescriptor bdNew = new BinaryDescriptor(bd.getHandle(), bd.getName(), bd.getSize(), mm[0], mm[1]);
                 binary.setTranslation(lang, bdNew);
             } else {
                 BinaryDescriptor bdNew = identifyAndTransferTransitBinary(con, bd);
                 if (mimeMetaMap == null)
                     bdNew = binaryTransit(con, bdNew);
                 binary.setTranslation(lang, bdNew);
                 if (mimeMetaMap != null)
                     mimeMetaMap.put(bdNew.getHandle(), new String[]{bdNew.getMimeType(), bdNew.getMetadata()});
             }
         }
     }
 
     /**
      * Identifies a binary in the transit table and generates previews etc.
      *
     * @param con    an open and valid Connection
      * @param binary the binary to identify
      * @return BinaryDescriptor
      * @throws FxApplicationException on errors
      */
     private BinaryDescriptor identifyAndTransferTransitBinary(Connection _con, BinaryDescriptor binary) throws FxApplicationException {
         //check if already identified
         if (!StringUtils.isEmpty(binary.getMetadata()))
             return binary;
         PreparedStatement ps = null;
         BinaryTransitFileInfo binaryTransitFileInfo = null;
         File previewFile1 = null, previewFile2 = null, previewFile3 = null, previewFile4 = null;
         FileInputStream pin1 = null, pin2 = null, pin3 = null, pin4 = null;
         int[] dimensionsPreview1 = {0, 0};
         int[] dimensionsPreview2 = {0, 0};
         int[] dimensionsPreview3 = {0, 0};
         int[] dimensionsPreview4 = {0, 0};
         String metaData = "<empty/>";
         ResultSet rs = null;
         String md5sum = "";
         Connection con = null;
         try {
             con = Database.getNonTXDataSource().getConnection();
             con.setAutoCommit(false);
             binaryTransitFileInfo = getBinaryTransitFileInfo(binary);
             boolean processed = false;
             boolean useDefaultPreview = true;
             int defaultId = SYS_UNKNOWN;
 
             FxScriptBinding binding;
             ScriptingEngine scripting = EJBLookup.getScriptingEngine();
             for (long script : scripting.getByScriptEvent(FxScriptEvent.BinaryPreviewProcess)) {
                 binding = new FxScriptBinding();
                 binding.setVariable("processed", processed);
                 binding.setVariable("useDefaultPreview", useDefaultPreview);
                 binding.setVariable("defaultId", defaultId);
                 binding.setVariable("mimeType", binaryTransitFileInfo.getMimeType());
                 binding.setVariable("metaData", metaData);
                 binding.setVariable("binaryFile", binaryTransitFileInfo.getBinaryTransitFile().getAbsolutePath());
                 binding.setVariable("previewFile1", null);
                 binding.setVariable("previewFile2", null);
                 binding.setVariable("previewFile3", null);
                 binding.setVariable("previewFile4", null);
                 binding.setVariable("dimensionsPreview1", dimensionsPreview1);
                 binding.setVariable("dimensionsPreview2", dimensionsPreview2);
                 binding.setVariable("dimensionsPreview3", dimensionsPreview3);
                 binding.setVariable("dimensionsPreview4", dimensionsPreview4);
                 FxScriptResult result;
                 try {
                     result = scripting.runScript(script, binding);
                     binding = result.getBinding();
                     processed = (Boolean) binding.getVariable("processed");
                     if (processed) {
                         useDefaultPreview = (Boolean) binding.getVariable("useDefaultPreview");
                         defaultId = (Integer) binding.getVariable("defaultId");
                         previewFile1 = getFileHandleFromBinding("previewFile1", binding);
                         previewFile2 = getFileHandleFromBinding("previewFile2", binding);
                         previewFile3 = getFileHandleFromBinding("previewFile3", binding);
                         previewFile4 = getFileHandleFromBinding("previewFile4", binding);
                         dimensionsPreview1 = (int[]) binding.getVariableOrNull("dimensionsPreview1");
                         dimensionsPreview2 = (int[]) binding.getVariableOrNull("dimensionsPreview2");
                         dimensionsPreview3 = (int[]) binding.getVariableOrNull("dimensionsPreview3");
                         dimensionsPreview4 = (int[]) binding.getVariableOrNull("dimensionsPreview4");
                         metaData = (String) binding.getVariable("metaData");
                         break;
                     }
                 } catch (Throwable e) {
                     LOG.error("Error running binary processing script: " + e.getMessage());
                     processed = false;
                 }
             }
             //only negative values are allowed for default previews
             if (useDefaultPreview && defaultId >= 0) {
                 defaultId = SYS_UNKNOWN;
                 LOG.warn("Only default preview id's that are negative and defined in BinaryDescriptor as constants are allowed!");
             }
 
             if (!useDefaultPreview) {
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS);
                 pin1 = setPreviewTransferParameters(ps, previewFile1, dimensionsPreview1, 1, 13);
                 pin2 = setPreviewTransferParameters(ps, previewFile2, dimensionsPreview2, 4, 14);
                 pin3 = setPreviewTransferParameters(ps, previewFile3, dimensionsPreview3, 7, 15);
                 pin4 = setPreviewTransferParameters(ps, previewFile4, dimensionsPreview4, 10, 16);
                 ps.setString(17, binary.getHandle());
                 ps.executeUpdate();
             } else {
                 ps = con.prepareStatement(BINARY_TRANSIT_PREVIEWS_REF);
                 ps.setLong(1, defaultId);
                 ps.setString(2, binary.getHandle());
                 ps.executeUpdate();
             }
             md5sum = FxSharedUtils.getMD5Sum(binaryTransitFileInfo.getBinaryTransitFile());
             con.commit();
         } catch (IOException e) {
             LOG.error("Stream reading failed:" + e.getMessage(), e);
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, con, null);
             FxSharedUtils.close(pin1, pin2, pin3, pin4);
             if (binaryTransitFileInfo != null && binaryTransitFileInfo.isDBStorage())
                 FxFileUtils.removeFile(binaryTransitFileInfo.getBinaryTransitFile());
             FxFileUtils.removeFile(previewFile1);
             FxFileUtils.removeFile(previewFile2);
             FxFileUtils.removeFile(previewFile3);
             FxFileUtils.removeFile(previewFile4);
             try {
                 if (rs != null) rs.close();
                 if (ps != null) ps.close();
             } catch (SQLException e) {
                 //noinspection ThrowFromFinallyBlock
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             }
         }
         return new BinaryDescriptor(binary.getHandle(), binary.getName(), binary.getSize(),
                 binaryTransitFileInfo.getMimeType(), metaData, md5sum);
     }
 
     /**
      * Get a variable from a binding as File
      *
      * @param variable name of the variable
      * @param binding  binding
      * @return File handle
      */
     private File getFileHandleFromBinding(String variable, FxScriptBinding binding) {
         Object o = binding.getVariableOrNull(variable);
         if (o == null)
             return null;
         else if (o instanceof File)
             return (File) o;
         else if (o instanceof String)
             return new File((String) o);
         return null;
     }
 
     /**
      * Set insert parameters for a preview image
      *
      * @param ps                the prepared statement to use
      * @param previewFile       the preview file
      * @param dimensionsPreview dimensions (width, height)
      * @param positionBinary    position in the prepared statement
      * @param positionSize      position of the file size parameter in the prepared statement
      * @return FileInputStream
      * @throws FileNotFoundException if the file does not exist
      * @throws SQLException          on errors
      */
     private FileInputStream setPreviewTransferParameters(PreparedStatement ps, File previewFile,
                                                          int[] dimensionsPreview, int positionBinary, int positionSize) throws FileNotFoundException, SQLException {
         FileInputStream pin = null;
         if (previewFile != null && previewFile.exists()) {
             pin = new FileInputStream(previewFile);
             ps.setBinaryStream(positionBinary, pin, (int) previewFile.length());
             ps.setInt(positionBinary + 1, dimensionsPreview[0]);
             ps.setInt(positionBinary + 2, dimensionsPreview[1]);
             ps.setInt(positionSize, (int) previewFile.length());
         } else {
             ps.setNull(positionBinary, Types.BINARY);
             ps.setInt(positionBinary + 1, 0);
             ps.setInt(positionBinary + 2, 0);
             ps.setInt(positionSize, 0);
         }
         return pin;
     }
 
     /**
      * Retrieve a File handle and mime type for a binary transit entry
      *
      * @param binary binary descriptor
      * @return BinaryTransitFileInfo containing a File handle and the detected mime type
      * @throws FxApplicationException on errors
      */
     @SuppressWarnings({"ThrowFromFinallyBlock"})
     protected BinaryTransitFileInfo getBinaryTransitFileInfo(BinaryDescriptor binary) throws FxApplicationException {
         PreparedStatement ps = null;
         ResultSet rs = null;
         File binaryTransitFile = null;
         InputStream in = null;
         FileOutputStream fos = null;
         String mimeType = "unknown";
         byte[] header = null;
         boolean inDB;
         try {
             inDB = EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB);
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
         if (inDB) {
             Connection con = null;
             try {
                 con = Database.getNonTXDataSource().getConnection();
                 ps = con.prepareStatement(BINARY_TRANSIT_HEADER);
                 ps.setString(1, binary.getHandle());
 
                 rs = ps.executeQuery();
                 if (!rs.next()) {
                     throw new FxApplicationException(LOG, "ex.content.binary.transitNotFound", binary.getHandle());
                 }
 
                 binaryTransitFile = File.createTempFile("FXBIN_", "_TEMP");
                 in = rs.getBinaryStream(1);
                 mimeType = rs.getString(2);
                 if (rs.wasNull())
                     mimeType = FxMimeType.UNKNOWN;
                 fos = new FileOutputStream(binaryTransitFile);
                 byte[] buffer = new byte[4096];
                 int read;
                 while ((read = in.read(buffer)) != -1) {
                     if (header == null && read > 0) {
                         header = new byte[read > 48 ? 48 : read];
                         System.arraycopy(buffer, 0, header, 0, read > 48 ? 48 : read);
                     }
                     fos.write(buffer, 0, read);
                 }
                 fos.close();
                 fos = null;
                 in.close();
                 in = null;
             } catch (FileNotFoundException e) {
                 throw new FxApplicationException(e, "ex.content.binary.transitNotFound", binary.getHandle());
             } catch (IOException e) {
                 throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
             } catch (SQLException e) {
                 throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
             } finally {
                 FxSharedUtils.close(fos, in);
                 try {
                     if (rs != null) rs.close();
                 } catch (SQLException e) {
                     //noinspection ThrowFromFinallyBlock
                     throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
                 }
                 Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
             }
         } else {
             binaryTransitFile = FxBinaryUtils.getTransitFile(FxContext.get().getDivisionId(), binary.getHandle());
             if (binaryTransitFile != null) {
                 Connection con = null;
                 try {
                     con = Database.getNonTXDataSource().getConnection();
                     ps = con.prepareStatement(BINARY_TRANSIT_MIMETYPE);
                     ps.setString(1, binary.getHandle());
                     ResultSet rsMime = ps.executeQuery();
                     if (rsMime != null && rsMime.next()) {
                         mimeType = rsMime.getString(1);
                         if (rsMime.wasNull())
                             mimeType = FxMimeType.UNKNOWN;
                     }
                     in = new FileInputStream(binaryTransitFile);
                     header = new byte[48];
                     if (in.read(header, 0, 48) < 48)
                         header = null;
                 } catch (SQLException e) {
                     throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
                 } catch (IOException e) {
                     throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
                 } finally {
                     Database.closeObjects(GenericBinarySQLStorage.class, con, ps);
                     try {
                         in.close();
                     } catch (IOException e) {
                         throw new FxApplicationException(e, "ex.content.binary.IOError", binary.getHandle());
                     }
                 }
             } else
                 throw new FxDbException("ex.content.binary.transitNotFound", binary.getHandle());
         }
         if (header != null) {
             String detectedMimeType = FxMediaEngine.detectMimeType(header, binary.getName());
             if (!FxMimeType.UNKNOWN.equalsIgnoreCase(detectedMimeType))
                 mimeType = detectedMimeType;
         }
 
         if (binaryTransitFile == null || !binaryTransitFile.exists())
             throw new FxApplicationException("ex.content.binary.transitNotFound", binary.getHandle());
         return new BinaryTransitFileInfo(binaryTransitFile, mimeType, inDB);
     }
 
     /**
      * {@inheritDoc}
      */
     public void storeBinary(Connection con, long id, int version, int quality, String name, long length, InputStream binary) throws FxApplicationException {
         BinaryUploadPayload payload = FxStreamUtils.uploadBinary(length, binary);
         BinaryDescriptor desc = new BinaryDescriptor(payload.getHandle(), name, length, null, null);
         desc = identifyAndTransferTransitBinary(con, desc);
         binaryTransit(con, desc, id, version, quality);
     }
 
     /**
      * {@inheritDoc}
      */
     public void updateBinaryPreview(Connection con, long id, int version, int quality, int preview, int width, int height, long length, InputStream binary) {
         //TODO: code me!
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeBinaries(Connection con, SelectOperation remOp, FxPK pk, FxType type) throws FxApplicationException {
         if (type != null) {
             // bail out early if type has no binary properties
             boolean hasBinaryProperties = false;
             for (FxPropertyAssignment pa : type.getAllProperties()) {
                 if (pa.getProperty().getDataType() == FxDataType.Binary) {
                     hasBinaryProperties = true;
                     break;
                 }
             }
             if (!hasBinaryProperties) {
                 return;
             }
         }
         PreparedStatement ps = null;
         List<Long> binaries = null;
         try {
             //query affected binary id's
             switch (remOp) {
                 case SelectId:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET);
                     ps.setLong(1, pk.getId());
                     break;
                 case SelectVersion:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_GET + " AND VER=?");
                     ps.setLong(1, pk.getId());
                     ps.setInt(2, pk.getVersion());
                     break;
                 case SelectType:
                     ps = con.prepareStatement(CONTENT_BINARY_REMOVE_TYPE_GET);
                     ps.setLong(1, type.getId());
                     break;
                 default:
                     return;
             }
             ResultSet rs = ps.executeQuery();
             while (rs != null && rs.next()) {
                 if (binaries == null)
                     binaries = new ArrayList<Long>(20);
                 binaries.add(rs.getLong(1));
             }
             ps.close();
 
             //reset data
             if (binaries != null) {
                 switch (remOp) {
                     case SelectId:
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID);
                         ps.setLong(1, pk.getId());
                         ps.executeUpdate();
                         ps.close();
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_ID);
                         ps.setLong(1, pk.getId());
                         ps.executeUpdate();
                         break;
                     case SelectVersion:
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_ID + " AND VER=?");
                         ps.setLong(1, pk.getId());
                         ps.setInt(2, pk.getVersion());
                         ps.executeUpdate();
                         ps.close();
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_ID + " AND VER=?");
                         ps.setLong(1, pk.getId());
                         ps.setInt(2, pk.getVersion());
                         ps.executeUpdate();
                         break;
                     case SelectType:
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESETDATA_TYPE);
                         ps.setLong(1, type.getId());
                         ps.executeUpdate();
                         ps.close();
                         ps = con.prepareStatement(CONTENT_BINARY_REMOVE_RESET_TYPE);
                         ps.setLong(1, type.getId());
                         ps.executeUpdate();
                         break;
                     default:
                         return;
                 }
                 removeBinaries(con, binaries);
             }
         } catch (SQLException e) {
             throw new FxDbException(e, "ex.db.sqlError", e.getMessage());
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, ps);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void removeExpiredTransitEntries(Connection con) {
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement(CONTENT_BINARY_TRANSIT_CLEANUP);
             ps.setLong(1, System.currentTimeMillis());
             int count = ps.executeUpdate();
             int fscount = 0;
             if (!EJBLookup.getConfigurationEngine().get(SystemParameters.BINARY_TRANSIT_DB))
                 fscount = FxBinaryUtils.removeExpiredTransitFiles(FxContext.get().getDivisionId());
             if (count > 0 || fscount > 0)
                 LOG.info(count + " expired binary transit entries removed" +
                         (fscount > 0 ? " (" + fscount + " on the filesystem)" : ""));
         } catch (SQLException e) {
             LOG.error(e, e);
         } catch (FxApplicationException e) {
             LOG.error(e, e);
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, ps);
         }
     }
 
     /**
      * Get the usage count of a binary
      *
      * @param ps valid prepared statement for the usage query
      * @param id id of the binary
      * @return usage count
      * @throws SQLException on errors
      */
     private long getUsageCount(PreparedStatement ps, long id) throws SQLException {
         ps.setLong(1, id);
         ResultSet rs = ps.executeQuery();
         if (rs != null && rs.next())
             return rs.getLong(1);
         return 0;
     }
 
     /**
      * Remove the given list of binaries if they are not in use
      *
      * @param con      an open and valid connection
      * @param binaries list of binaries to remove
      * @throws SQLException on errors
      */
     protected void removeBinaries(Connection con, List<Long> binaries) throws SQLException {
         PreparedStatement psRemove = null;
         PreparedStatement psUsage1 = null;
         PreparedStatement psUsage2 = null;
         PreparedStatement psUsage3 = null;
         PreparedStatement psUsage4 = null;
         try {
             psRemove = con.prepareStatement(CONTENT_BINARY_REMOVE_ID);
             psUsage1 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT + " WHERE DBIN_ID=?");
             psUsage2 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_DATA + " WHERE FBLOB=?");
             psUsage3 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_STRUCT_SELECTLIST_ITEM + " WHERE DBIN_ID=?");
             psUsage4 = con.prepareStatement("SELECT COUNT(*) FROM " + TBL_CONTENT_BINARY + " WHERE PREVIEW_REF=?");
             int divisionId = FxContext.get().getDivisionId();
             long cnt1, cnt2, cnt3, cnt4;
             for (Long id : binaries) {
                 cnt2 = cnt3 = cnt4 = 0;
                 cnt1 = getUsageCount(psUsage1, id);
                 if (cnt1 <= 0) cnt2 = getUsageCount(psUsage2, id);
                 if (cnt2 == 0) cnt3 = getUsageCount(psUsage3, id);
                 if (cnt3 == 0) cnt4 = getUsageCount(psUsage4, id);
                 if (cnt1 + cnt2 + cnt3 + cnt4 > 0) {
                     if (LOG.isDebugEnabled())
                         LOG.debug("Binary #" + id + " is in use! (content:" + cnt1 + ",content_data:" + cnt2 + ",selectlist:" + cnt3 + ",binary:" + cnt4 + ") - only the first usage is calculated, rest is set to 0!");
                     continue;
                 } else {
                     if (LOG.isDebugEnabled()) LOG.debug("Removing binary #" + id);
                 }
 
                 psRemove.setLong(1, id);
                 psRemove.executeUpdate();
                 FxBinaryUtils.removeBinary(divisionId, id);
             }
         } finally {
             Database.closeObjects(GenericBinarySQLStorage.class, psRemove, psUsage1, psUsage2, psUsage3, psUsage4);
         }
     }
 }
