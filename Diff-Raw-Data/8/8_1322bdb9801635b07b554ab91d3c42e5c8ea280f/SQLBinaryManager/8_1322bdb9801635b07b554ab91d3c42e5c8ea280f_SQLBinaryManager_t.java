 /*
  * (C) Copyright 2010-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     Florent Guillaume
  */
 package org.nuxeo.ecm.core.storage.sql;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.nuxeo.common.file.FileCache;
 import org.nuxeo.common.file.LRUFileCache;
 import org.nuxeo.common.utils.SizeUtils;
 import org.nuxeo.ecm.core.storage.StorageException;
 import org.nuxeo.ecm.core.storage.sql.jdbc.db.Column;
 import org.nuxeo.ecm.core.storage.sql.jdbc.db.Database;
 import org.nuxeo.ecm.core.storage.sql.jdbc.db.Table;
 import org.nuxeo.ecm.core.storage.sql.jdbc.dialect.Dialect;
 import org.nuxeo.runtime.api.DataSourceHelper;
 
 /**
  * A Binary Manager that stores binaries as SQL BLOBs.
  * <p>
  * The BLOBs are cached locally on first access for efficiency.
  * <p>
  * Because the BLOB length can be accessed independently of the binary stream,
  * it is also cached in a simple text file if accessed before the stream.
  */
 public class SQLBinaryManager extends DefaultBinaryManager {
 
     private static final Log log = LogFactory.getLog(SQLBinaryManager.class);
 
     public static final String DS_PREFIX = "datasource=";
 
     public static final String TABLE_PREFIX = "table=";
 
     public static final String CACHE_SIZE_PREFIX = "cachesize=";
 
     public static final String DEFAULT_CACHE_SIZE = "10M";
 
     public static final String COL_ID = "id";
 
     public static final String COL_BIN = "bin";
 
     public static final String COL_MARK = "mark"; // for mark & sweep GC
 
     protected static final String LEN_DIGEST_SUFFIX = "-len";
 
     protected String dataSourceName;
 
     protected DataSource dataSource;
 
     protected FileCache fileCache;
 
     protected String checkSql;
 
     protected String putSql;
 
     protected String getSql;
 
     protected String getLengthSql;
 
     protected String gcStartSql;
 
     protected String gcMarkSql;
 
     protected String gcStatsSql;
 
     protected String gcSweepSql;
 
     protected static boolean disableCheckExisting; // for unit tests
 
     protected static boolean resetCache; // for unit tests
 
     @Override
     public void initialize(RepositoryDescriptor repositoryDescriptor)
             throws IOException {
         repositoryName = repositoryDescriptor.name;
         descriptor = new BinaryManagerDescriptor();
         descriptor.digest = getDigest();
         log.info("Repository '" + repositoryDescriptor.name + "' using "
                 + getClass().getSimpleName());
 
         dataSourceName = null;
         String tableName = null;
         String cacheSizeStr = DEFAULT_CACHE_SIZE;
         for (String part : repositoryDescriptor.binaryManagerKey.split(",")) {
             if (part.startsWith(DS_PREFIX)) {
                 dataSourceName = part.substring(DS_PREFIX.length()).trim();
             }
             if (part.startsWith(TABLE_PREFIX)) {
                 tableName = part.substring(TABLE_PREFIX.length()).trim();
             }
             if (part.startsWith(CACHE_SIZE_PREFIX)) {
                 cacheSizeStr = part.substring(CACHE_SIZE_PREFIX.length()).trim();
             }
         }
         if (dataSourceName == null) {
             throw new RuntimeException("Missing " + DS_PREFIX
                     + " in binaryManager key");
         }
         if (tableName == null) {
             throw new RuntimeException("Missing " + TABLE_PREFIX
                     + " in binaryManager key");
         }
 
         try {
             dataSource = DataSourceHelper.getDataSource(dataSourceName);
         } catch (NamingException e) {
             throw new IOException("Cannot find datasource: " + dataSourceName,
                     e);
         }
 
         // create the SQL statements used
         createSql(tableName);
 
         // create file cache
         File dir = File.createTempFile("nxbincache.", "", null);
         dir.delete();
         dir.mkdir();
         dir.deleteOnExit();
         long cacheSize = SizeUtils.parseSizeInBytes(cacheSizeStr);
         fileCache = new LRUFileCache(dir, cacheSize);
         log.info("Using binary cache directory: " + dir.getPath() + " size: "
                 + cacheSizeStr);
 
         createGarbageCollector();
     }
 
     @Override
     protected void createGarbageCollector() {
         garbageCollector = new SQLBinaryGarbageCollector(this);
     }
 
     protected void createSql(String tableName) throws IOException {
         Dialect dialect = getDialect();
         Database database = new Database(dialect);
         Table table = database.addTable(tableName);
         ColumnType dummytype = ColumnType.STRING;
         Column idCol = table.addColumn(COL_ID, dummytype, COL_ID, null);
         Column binCol = table.addColumn(COL_BIN, dummytype, COL_BIN, null);
         Column markCol = table.addColumn(COL_MARK, dummytype, COL_MARK, null);
 
         checkSql = String.format("SELECT 1 FROM %s WHERE %s = ?",
                 table.getQuotedName(), idCol.getQuotedName());
         putSql = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
                 table.getQuotedName(), idCol.getQuotedName(),
                 binCol.getQuotedName(), markCol.getQuotedName());
         getSql = String.format("SELECT %s FROM %s WHERE %s = ?",
                 binCol.getQuotedName(), table.getQuotedName(),
                 idCol.getQuotedName());
         getLengthSql = String.format("SELECT %s(%s) FROM %s WHERE %s = ?",
                 dialect.getBlobLengthFunction(), binCol.getQuotedName(),
                 table.getQuotedName(), idCol.getQuotedName());
 
         gcStartSql = String.format("UPDATE %s SET %s = ?",
                 table.getQuotedName(), markCol.getQuotedName());
         gcMarkSql = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
                 table.getQuotedName(), markCol.getQuotedName(),
                 idCol.getQuotedName());
         gcStatsSql = String.format(
                 "SELECT COUNT(*), SUM(%s(%s)) FROM %s WHERE %s = ?",
                 dialect.getBlobLengthFunction(), binCol.getQuotedName(),
                 table.getQuotedName(), markCol.getQuotedName());
         gcSweepSql = String.format("DELETE FROM %s WHERE %s = ?",
                 table.getQuotedName(), markCol.getQuotedName());
     }
 
     protected Dialect getDialect() throws IOException {
         Connection connection = null;
         try {
             connection = dataSource.getConnection();
             return Dialect.createDialect(connection, null, null);
         } catch (StorageException e) {
             throw new IOException(e);
         } catch (SQLException e) {
             throw new IOException(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     log.error(e, e);
                 }
             }
         }
     }
 
     /**
      * Gets the message digest to use to hash binaries.
      */
     protected String getDigest() {
         return DEFAULT_DIGEST;
     }
 
     protected static void logSQL(String sql, Serializable... values) {
         if (!log.isTraceEnabled()) {
             return;
         }
         StringBuilder buf = new StringBuilder();
         int start = 0;
         for (Serializable v : values) {
             int index = sql.indexOf('?', start);
             if (index == -1) {
                 // mismatch between number of ? and number of values
                 break;
             }
             buf.append(sql, start, index);
             buf.append(loggedValue(v));
             start = index + 1;
         }
         buf.append(sql, start, sql.length());
         log.trace("(bin) SQL: " + buf.toString());
     }
 
     protected static String loggedValue(Serializable value) {
         if (value == null) {
             return "NULL";
         }
         if (value instanceof String) {
             String v = (String) value;
             return "'" + v.replace("'", "''") + "'";
         }
         return value.toString();
     }
 
     @Override
     public Binary getBinary(InputStream in) throws IOException {
         // write the input stream to a temporary file, while computing a digest
         File tmp = fileCache.getTempFile();
         OutputStream out = new FileOutputStream(tmp);
         String digest;
         try {
             digest = storeAndDigest(in, out);
         } finally {
             in.close();
             out.close();
         }
 
         // store the blob in the SQL database
         Connection connection = null;
         try {
             connection = dataSource.getConnection();
             boolean existing;
             if (disableCheckExisting) {
                 // for unit tests
                 existing = false;
             } else {
                 logSQL(checkSql, digest);
                 PreparedStatement ps = connection.prepareStatement(checkSql);
                 ps.setString(1, digest);
                 ResultSet rs = ps.executeQuery();
                 existing = rs.next();
                 ps.close();
             }
             if (!existing) {
                 // insert new blob
                 logSQL(putSql, digest, "somebinary", Boolean.TRUE);
                 PreparedStatement ps = connection.prepareStatement(putSql);
                 ps.setString(1, digest);
                 // needs dbcp 1.4:
                 // ps.setBlob(2, new FileInputStream(file), file.length());
                FileInputStream tmpis = new FileInputStream(tmp);
                try {
                    ps.setBinaryStream(2, tmpis, (int) tmp.length());
                } finally {
                    tmpis.close();
                }
                 ps.setBoolean(3, true); // mark new additions for GC
                 try {
                     ps.execute();
                 } catch (SQLException e) {
                     if (!isDuplicateKeyException(e)) {
                         throw e;
                     }
                 }
                 ps.close();
             }
         } catch (SQLException e) {
             throw new IOException(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     log.error(e, e);
                 }
             }
         }
 
         // register the file in the file cache if all went well
         File file = fileCache.putFile(digest, tmp);
 
         return new Binary(file, digest, repositoryName);
     }
 
     protected boolean isDuplicateKeyException(SQLException e) {
         String sqlState = e.getSQLState();
         if ("23000".equals(sqlState)) {
             // MySQL: Duplicate entry ... for key ...
             // Oracle: unique constraint ... violated
             // SQL Server: Violation of PRIMARY KEY constraint
             return true;
         }
         if ("23001".equals(sqlState)) {
             // H2: Unique index or primary key violation
             return true;
         }
         if ("23505".equals(sqlState)) {
             // PostgreSQL: duplicate key value violates unique constraint
             return true;
         }
         return false;
     }
 
     @Override
     public Binary getBinary(String digest) {
         if (resetCache) {
             // for unit tests
             resetCache = false;
             fileCache.clear();
         }
         // check in the cache
         File file = fileCache.getFile(digest);
         if (file == null) {
             return new SQLLazyBinary(digest, fileCache, dataSource, getSql,
                     getLengthSql);
         } else {
             return new Binary(file, digest, repositoryName);
         }
     }
 
     public static class SQLLazyBinary extends LazyBinary {
 
         private static final long serialVersionUID = 1L;
 
         protected final DataSource dataSource;
 
         protected final String getSql;
 
         protected final String getLengthSql;
 
         public SQLLazyBinary(String digest, FileCache fileCache,
                 DataSource dataSource, String getSql, String getLengthSql) {
             super(digest, fileCache);
             this.dataSource = dataSource;
             this.getSql = getSql;
             this.getLengthSql = getLengthSql;
         }
 
         @Override
         protected boolean fetchFile(File tmp) {
             Connection connection = null;
             try {
                 connection = dataSource.getConnection();
                 logSQL(getSql, digest);
                 PreparedStatement ps = connection.prepareStatement(getSql);
                 ps.setString(1, digest);
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next()) {
                     log.error("Unknown binary: " + digest);
                     return false;
                 }
                 InputStream in = rs.getBinaryStream(1);
                 if (in == null) {
                     log.error("Missing binary: " + digest);
                     return false;
                 }
                 // store in file
                 OutputStream out = null;
                 try {
                     out = new FileOutputStream(tmp);
                     IOUtils.copy(in, out);
                 } finally {
                     in.close();
                     if (out != null) {
                         out.close();
                     }
                 }
                 return true;
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (connection != null) {
                     try {
                         connection.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
             }
         }
 
         @Override
         protected Long fetchLength() {
             Connection connection = null;
             try {
                 connection = dataSource.getConnection();
                 logSQL(getLengthSql, digest);
                 PreparedStatement ps = connection.prepareStatement(getLengthSql);
                 ps.setString(1, digest);
                 ResultSet rs = ps.executeQuery();
                 if (!rs.next()) {
                     log.error("Unknown binary: " + digest);
                     return null;
                 }
                 return Long.valueOf(rs.getLong(1));
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (connection != null) {
                     try {
                         connection.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
             }
         }
     }
 
     public static class SQLBinaryGarbageCollector implements
             BinaryGarbageCollector {
 
         protected final SQLBinaryManager binaryManager;
 
         protected volatile long startTime;
 
         protected BinaryManagerStatus status;
 
         public SQLBinaryGarbageCollector(SQLBinaryManager binaryManager) {
             this.binaryManager = binaryManager;
         }
 
         @Override
         public String getId() {
             return "datasource:" + binaryManager.dataSourceName;
         }
 
         @Override
         public BinaryManagerStatus getStatus() {
             return status;
         }
 
         @Override
         public boolean isInProgress() {
             // volatile as this is designed to be called from another thread
             return startTime != 0;
         }
 
         @Override
         public void start() {
             if (startTime != 0) {
                 throw new RuntimeException("Alread started");
             }
             startTime = System.currentTimeMillis();
             status = new BinaryManagerStatus();
 
             Connection connection = null;
             PreparedStatement ps = null;
             try {
                 connection = binaryManager.dataSource.getConnection();
                 logSQL(binaryManager.gcStartSql, Boolean.FALSE);
                 ps = connection.prepareStatement(binaryManager.gcStartSql);
                 ps.setBoolean(1, false); // clear marks
                 int n = ps.executeUpdate();
                 logSQL("  -> ? rows", Long.valueOf(n));
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (ps != null) {
                     try {
                         ps.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
                 if (connection != null) {
                     try {
                         connection.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
             }
         }
 
         @Override
         public void mark(String digest) {
             Connection connection = null;
             PreparedStatement ps = null;
             try {
                 connection = binaryManager.dataSource.getConnection();
                 logSQL(binaryManager.gcMarkSql, Boolean.TRUE, digest);
                 ps = connection.prepareStatement(binaryManager.gcMarkSql);
                 ps.setBoolean(1, true); // mark
                 ps.setString(2, digest);
                 ps.execute();
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (ps != null) {
                     try {
                         ps.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
                 if (connection != null) {
                     try {
                         connection.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
             }
         }
 
         @Override
         public void stop(boolean delete) {
             if (startTime == 0) {
                 throw new RuntimeException("Not started");
             }
 
             Connection connection = null;
             PreparedStatement ps = null;
             try {
                 connection = binaryManager.dataSource.getConnection();
                 // stats
                 logSQL(binaryManager.gcStatsSql, Boolean.TRUE);
                 ps = connection.prepareStatement(binaryManager.gcStatsSql);
                 ps.setBoolean(1, true); // marked
                 ResultSet rs = ps.executeQuery();
                 rs.next();
                 status.numBinaries = rs.getLong(1);
                 status.sizeBinaries = rs.getLong(2);
                 logSQL("  -> ?, ?", Long.valueOf(status.numBinaries),
                         Long.valueOf(status.sizeBinaries));
                 logSQL(binaryManager.gcStatsSql, Boolean.FALSE);
                 ps.setBoolean(1, false); // unmarked
                 rs = ps.executeQuery();
                 rs.next();
                 status.numBinariesGC = rs.getLong(1);
                 status.sizeBinariesGC = rs.getLong(2);
                 logSQL("  -> ?, ?", Long.valueOf(status.numBinariesGC),
                         Long.valueOf(status.sizeBinariesGC));
                 if (delete) {
                     // sweep
                     ps.close();
                     logSQL(binaryManager.gcSweepSql, Boolean.FALSE);
                     ps = connection.prepareStatement(binaryManager.gcSweepSql);
                     ps.setBoolean(1, false); // sweep unmarked
                     int n = ps.executeUpdate();
                     logSQL("  -> ? rows", Long.valueOf(n));
                 }
             } catch (SQLException e) {
                 throw new RuntimeException(e);
             } finally {
                 if (ps != null) {
                     try {
                         ps.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
                 if (connection != null) {
                     try {
                         connection.close();
                     } catch (SQLException e) {
                         log.error(e, e);
                     }
                 }
             }
 
             status.gcDuration = System.currentTimeMillis() - startTime;
             startTime = 0;
         }
     }
 }
