 package com.nearinfinity.mysqlengine.jni;
 
 import com.nearinfinity.hbaseclient.ColumnMetadata;
 import com.nearinfinity.hbaseclient.Constants;
 import com.nearinfinity.hbaseclient.HBaseClient;
 import com.nearinfinity.hbaseclient.ResultParser;
 import com.nearinfinity.mysqlengine.*;
 import com.nearinfinity.hbaseclient.strategy.*;
 import com.nearinfinity.mysqlengine.scanner.SingleResultScanner;
 import com.nearinfinity.mysqlengine.scanner.DoubleResultScanner;
 import com.nearinfinity.mysqlengine.scanner.HBaseResultScanner;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jedstrom
  * Date: 8/1/12
  * Time: 9:46 AM
  * To change this template use File | Settings | File Templates.
  */
 public class HBaseAdapter {
     private static AtomicLong connectionCounter;
     private static Map<Long, Connection> clientPool;
     private static HBaseClient client;
     private static final Logger logger = Logger.getLogger(HBaseAdapter.class);
 
     private static final int DEFAULT_NUM_CACHED_ROWS = 2500;
     private static final long DEFAULT_WRITE_BUFFER_SIZE = 5 * 1024 * 1024;
 
     static {
         try {
             logger.info("Static Initializer-> Begin");
 
             //Read config options from adapter.conf
             Scanner confFile = new Scanner(new File("/etc/mysql/adapter.conf"));
             Map<String, String> params = new HashMap<String, String>();
             while (confFile.hasNextLine()) {
                 Scanner line = new Scanner(confFile.nextLine());
                 params.put(line.next(), line.next());
             }
 
             //Initialize class variables
             client = new HBaseClient(params.get("hbase_table_name"), params.get("zk_quorum"));
             connectionCounter = new AtomicLong(0L);
             clientPool = new ConcurrentHashMap<Long, Connection>();
 
             try {
                 int cacheSize = Integer.parseInt(params.get("table_scan_cache_rows"));
                 client.setCacheSize(cacheSize);
             } catch (NumberFormatException e) {
                 logger.info("Static Initializer-> Number of rows to cache was not provided or invalid" +
                         " - using default of " + DEFAULT_NUM_CACHED_ROWS);
                 client.setCacheSize(DEFAULT_NUM_CACHED_ROWS);
             }
 
             try {
                 long writeBufferSize = Long.parseLong(params.get("write_buffer_size"));
                 client.setWriteBufferSize(writeBufferSize);
             } catch (NumberFormatException e) {
                 logger.info("Static Initializer-> Write buffer size was not provided or invalid - using default of " + DEFAULT_WRITE_BUFFER_SIZE);
                 client.setWriteBufferSize(DEFAULT_WRITE_BUFFER_SIZE);
             }
 
             boolean flushChangesImmediately = Boolean.parseBoolean(params.get("flush_changes_immediately"));
             client.setAutoFlushTables(flushChangesImmediately);
         } catch (FileNotFoundException e) {
             logger.warn("Static Initializer-> FileNotFoundException:", e);
         }
     }
 
     public static boolean createTable(String tableName, Map<String, List<ColumnMetadata>> columns) throws HBaseAdapterException {
         logger.info("creatingTable-> tableName:" + tableName);
 
         try {
             client.createTableFull(tableName, columns);
         } catch (Exception e) {
             logger.error("createTable-> Exception:", e);
             throw new HBaseAdapterException("createTable", e);
         }
 
         return true;
     }
 
     public static long startScan(String tableName, boolean isFullTableScan) throws HBaseAdapterException {
         long scanId = connectionCounter.incrementAndGet();
        logger.info("startScan-> tableName: " + tableName + ", scanId: " + scanId + ", isFullTableScan: " + isFullTableScan);
         try {
             ScanStrategy strategy = new FullTableScanStrategy(tableName, "", new byte[0]);
             SingleResultScanner dataScanner = new SingleResultScanner(client.getScanner(strategy));
             clientPool.put(scanId, new Connection(tableName, dataScanner));
         } catch (Exception e) {
             logger.error("startScan-> Exception:", e);
             throw new HBaseAdapterException("startScan", e);
         }
 
         return scanId;
     }
 
     public static Row nextRow(long scanId) throws HBaseAdapterException {
         Connection conn = getConnectionForId(scanId);
 
         Row row = new Row();
         try {
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.next(null);
 
             if (result == null) {
                 return null;
             }
 
             //Set values and UUID
             Map<String, byte[]> values = client.parseRow(result, conn.getTableName());
             UUID uuid = ResultParser.parseUUID(result);
             row.parse(values, uuid);
         } catch (Exception e) {
             logger.error("nextRow-> Exception:", e);
             throw new HBaseAdapterException("nextRow", e);
         }
 
         return row;
     }
 
     public static void endScan(long scanId) throws HBaseAdapterException {
         logger.info("endScan-> scanId: " + scanId);
         Connection conn = getConnectionForId(scanId);
         conn.close();
     }
 
     public static boolean writeRow(String tableName, Map<String, byte[]> values, byte[] unireg) throws HBaseAdapterException {
         try {
             client.writeRow(tableName, values, unireg);
         } catch (Exception e) {
             logger.error("writeRow-> Exception:", e);
             throw new HBaseAdapterException("writeRow", e);
         }
 
         return true;
     }
 
     public static void flushWrites() {
         client.flushWrites();
     }
 
     public static boolean deleteRow(long scanId) throws HBaseAdapterException {
         logger.info("deleteRow-> scanId: " + scanId);
 
         boolean deleted;
         try {
             Connection conn = getConnectionForId(scanId);
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.getLastResult();
             String tableName = conn.getTableName();
             UUID uuid = ResultParser.parseUUID(result);
 
             deleted = client.deleteRow(tableName, uuid);
         } catch (IOException e) {
             logger.error("deleteRow-> Exception:", e);
             throw new HBaseAdapterException("deleteRow", e);
         }
 
         return deleted;
     }
 
     public static int deleteAllRows(String tableName) throws HBaseAdapterException {
         logger.info("deleteAllRows-> tableName: " + tableName);
 
         int deleted;
         try {
             deleted = client.deleteAllRows(tableName);
         } catch (IOException e) {
             logger.error("deleteAllRows-> Exception:", e);
             throw new HBaseAdapterException("deleteAllRows", e);
         }
 
         return deleted;
     }
 
     public static boolean dropTable(String tableName) throws HBaseAdapterException {
         logger.info("dropTable-> tableName: " + tableName);
 
         boolean deleted;
         try {
             deleted = client.dropTable(tableName);
         } catch (IOException e) {
             logger.error("dropTable-> Exception:", e);
             throw new HBaseAdapterException("dropTable", e);
         }
 
         return deleted;
     }
 
     public static Row getRow(long scanId, byte[] uuid) throws HBaseAdapterException {
         logger.info("getRow-> scanId: " + scanId + "," + Bytes.toStringBinary(uuid));
         Connection conn = getConnectionForId(scanId);
 
         Row row = new Row();
         try {
             String tableName = conn.getTableName();
             ByteBuffer buffer = ByteBuffer.wrap(uuid);
             UUID rowUuid = new UUID(buffer.getLong(), buffer.getLong());
 
             Result result = client.getDataRow(rowUuid, tableName);

             if (result == null) {
                 logger.error("getRow-> Exception: Row not found");
                 throw new HBaseAdapterException("getRow", new Exception());
             }
 
            conn.getScanner().setLastResult(result);

             Map<String, byte[]> values = client.parseRow(result, conn.getTableName());
             row.setUUID(rowUuid);
             row.setRowMap(values);
         } catch (Exception e) {
             logger.error("getRow-> Exception:", e);
             throw new HBaseAdapterException("getRow", e);
         }
 
         return row;
     }
 
     public static long startIndexScan(String tableName, String columnName) throws HBaseAdapterException {
         logger.info("startIndexScan-> tableName " + tableName + ", columnName: " + columnName);
 
         long scanId = connectionCounter.incrementAndGet();
         try {
             clientPool.put(scanId, new Connection(tableName, columnName));
         } catch (Exception e) {
             logger.error("startIndexScan-> Exception:", e);
             throw new HBaseAdapterException("startIndexScan", e);
         }
 
         return scanId;
     }
 
     public static IndexRow indexRead(long scanId, byte[] value, IndexReadType readType) throws HBaseAdapterException {
         logger.info("Reading index with scanId " + scanId + " read type " + readType.name());
         Connection conn = getConnectionForId(scanId);
 
         IndexRow indexRow = new IndexRow();
         try {
             String tableName = conn.getTableName();
             String columnName = conn.getColumnName();
 
             byte[] valueToSkip = null;
             HBaseResultScanner scanner = null;
 
             switch (readType) {
                 case HA_READ_KEY_EXACT: {
                     ScanStrategy strategy = new ExactScanStrategy(tableName, columnName, value);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                 }
                 break;
                 case HA_READ_AFTER_KEY: {
                     ScanStrategy strategy = new OrderedScanStrategy(tableName, columnName, value);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                     valueToSkip = value;
                 }
                 break;
                 case HA_READ_KEY_OR_NEXT: {
                     ScanStrategy strategy = new OrderedScanStrategy(tableName, columnName, value);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                 }
                 break;
                 case HA_READ_BEFORE_KEY: {
                     ScanStrategy strategy = new ReverseScanStrategy(tableName, columnName, value);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                     valueToSkip = value;
                 }
                 break;
                 case HA_READ_KEY_OR_PREV: {
                     ScanStrategy strategy = new ReverseScanStrategy(tableName, columnName, value);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                 }
                 break;
                 case INDEX_FIRST: {
                     ScanStrategy strategy = new OrderedScanStrategy(tableName, columnName, new byte[0]);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                 }
                 break;
                 case INDEX_LAST: {
                     ScanStrategy strategy = new ReverseScanStrategy(tableName, columnName, new byte[0]);
                     scanner = new DoubleResultScanner(client.getScanner(strategy), tableName, columnName, client);
                 }
                 break;
                 case INDEX_NULL: {
                     ScanStrategy strategy = new NullScanStrategy(tableName, columnName, null);
                     scanner = new SingleResultScanner(client.getScanner(strategy));
                 }
                 break;
             }
 
             conn.setScanner(scanner);
             Result result = scanner.next(valueToSkip);
 
             if (result == null) {
                 return indexRow;
             }
 
             indexRow.parseResult(result);
         } catch (Exception e) {
             logger.error("indexRead-> Exception:", e);
             throw new HBaseAdapterException("indexRead", e);
         }
 
         return indexRow;
     }
 
     public static IndexRow nextIndexRow(long scanId) throws HBaseAdapterException {
         logger.info("nextIndexRow-> scanId: " + scanId);
 
         Connection conn = getConnectionForId(scanId);
 
         IndexRow indexRow = new IndexRow();
         try {
             HBaseResultScanner scanner = conn.getScanner();
             Result result = scanner.next(null);
             if (result == null) {
                 return indexRow;
             }
 
             indexRow.parseResult(result);
         } catch (Exception e) {
             logger.error("nextIndexRow-> Exception:", e);
             throw new HBaseAdapterException("nextIndexRow", e);
         }
 
         return indexRow;
     }
 
     private static Connection getConnectionForId(long scanId) throws HBaseAdapterException {
         Connection conn = clientPool.get(scanId);
         if (conn == null) {
             throw new HBaseAdapterException("getConnectionForId->No connection for scanId: " + scanId, null);
         }
         return conn;
     }
 }
