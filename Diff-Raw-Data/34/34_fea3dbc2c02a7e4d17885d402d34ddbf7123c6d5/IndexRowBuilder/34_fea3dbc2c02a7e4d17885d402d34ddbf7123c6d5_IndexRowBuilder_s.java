 package com.nearinfinity.honeycomb.hbase.rowkey;
 
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.hadoop.hbase.util.Bytes;

 import com.google.common.collect.ImmutableMap;
 import com.nearinfinity.honeycomb.mysql.gen.ColumnType;
 import com.nearinfinity.honeycomb.util.Verify;
 
 /**
  * A builder for creating {@link IndexRow} instances.  Builder instances can be reused as it is safe
  * to call {@link #build} multiple times.
  */
 public class IndexRowBuilder {
     private static final long INVERT_SIGN_MASK = 0x8000000000000000L;
     private long tableId;
     private long indexId;
     private SortOrder sortOrder;
     private Map<String, ByteBuffer> records;
     private Map<String, ColumnType> columnTypes;
     private Collection<String> columnOrder;
     private UUID uuid;
 
     private IndexRowBuilder() {
     }
 
     /**
      * Creates a builder with the specified table and index identifiers using {@link SortOrder#Ascending}
      * as the default sort order
      *
      * @param tableId The valid table id that the {@link IndexRow} will correspond to
      * @param indexId The valid index id used to identify the {@link IndexRow}
      * @return The current builder instance
      */
     public static IndexRowBuilder newBuilder(final long tableId, final long indexId) {
         Verify.isValidTableId(tableId);
         checkArgument(indexId >= 0, "The index id is invalid");
         final IndexRowBuilder builder = new IndexRowBuilder();
         builder.tableId = tableId;
         builder.indexId = indexId;
         builder.sortOrder = SortOrder.Ascending;
         return builder;
     }
 
     /**
      * Adds the specified {@link SortOrder} to the builder instance being constructed
      *
      * @param sortOrder The sort order to use during the build phase, not null
      * @return The current builder instance
      */
     public IndexRowBuilder withSortOrder(final SortOrder sortOrder) {
         checkNotNull(sortOrder);
         this.sortOrder = sortOrder;
         return this;
     }
 
     /**
     *  Adds the specified {@link UUID} to the builder instance being constructed
      *
      * @param uuid The identifier to use during the build phase, not null
      * @return The current builder instance
      */
     public IndexRowBuilder withUUID(final UUID uuid) {
         checkNotNull(uuid);
         this.uuid = uuid;
         return this;
     }
 
     /**
      * Adds the specified records and column metadata details to the builder
      * instance being constructed
      *
     * @param records The records to associate with the index, not null
      * @param columnTypes The column name to {@link ColumnType} mapping for the index, not null
      * @param columnOrder The order of column names used for sorting the records. not null
      * @return The current builder instance
      */
     public IndexRowBuilder withRecords(final Map<String, ByteBuffer> records,
            final Map<String, ColumnType> columnTypes, final Collection<String> columnOrder) {
         checkNotNull(records);
         checkNotNull(columnTypes);
         checkNotNull(columnOrder);
         this.records = records;
         this.columnTypes = columnTypes;
         this.columnOrder = columnOrder;
         return this;
     }
 
     /**
      * Creates an {@link IndexRow} instance with the parameters supplied to the builder
      *
      * @return A new row instance constructed by the builder
      */
     public IndexRow build() {
         List<byte[]> sortedRecords = null;
         if (records != null) {
             Map<String, byte[]> encodedRow = encodeRow(records, columnTypes);
             if (sortOrder == SortOrder.Descending) {
                 encodedRow = reverseRowValues(encodedRow);
             }
 
             sortedRecords = getValuesInColumnOrder(encodedRow, columnOrder);
         }
 
         if (sortOrder == SortOrder.Ascending) {
             return new AscIndexRow(tableId, indexId, sortedRecords, uuid);
         }
 
         return new DescIndexRow(tableId, indexId, sortedRecords, uuid);
     }
 
     private static List<byte[]> getValuesInColumnOrder(final Map<String, byte[]> records, final Collection<String> columns) {
         final List<byte[]> sortedRecords = new LinkedList<byte[]>();
         for (final String column : columns) {
            sortedRecords.add(records.get(column));
         }
 
         return sortedRecords;
     }
 
     private static Map<String, byte[]> reverseRowValues(final Map<String, byte[]> row) {
         final ImmutableMap.Builder<String, byte[]> result = ImmutableMap.builder();
         for (final Map.Entry<String, byte[]> entry : row.entrySet()) {
             result.put(entry.getKey(), reverseValue(entry.getValue()));
         }
 
         return result.build();
     }
 
     private static byte[] reverseValue(final byte[] value) {
         final ByteBuffer buffer = ByteBuffer.allocate(value.length);
 
         for (final byte aValue : value) {
             buffer.put((byte) (~aValue));
         }
 
         return buffer.array();
     }
 
     private static Map<String, byte[]> encodeRow(final Map<String, ByteBuffer> rows, final Map<String, ColumnType> columnTypes) {
         final ImmutableMap.Builder<String, byte[]> result = ImmutableMap.builder();
         for (final Map.Entry<String, ByteBuffer> entry : rows.entrySet()) {
             byte[] encodedValue = encodeValue(entry.getValue(), columnTypes.get(entry.getKey()));
             result.put(entry.getKey(), encodedValue);
         }
 
         return result.build();
     }
 
     private static byte[] encodeValue(final ByteBuffer value, final ColumnType columnType) {
         try {
             switch (columnType) {
                 case LONG:
                 case ULONG:
                 case TIME: {
                     final long longValue = value.getLong();
                     return Bytes.toBytes(longValue ^ INVERT_SIGN_MASK);
                 }
                 case DOUBLE: {
                     final double doubleValue = value.getDouble();
                     final long longValue = Double.doubleToLongBits(doubleValue);
                     if (doubleValue < 0.0) {
                         return Bytes.toBytes(~longValue);
                     }
 
                     return Bytes.toBytes(longValue ^ INVERT_SIGN_MASK);
                 }
                 default:
                     return value.array();
             }
         } finally {
             value.rewind(); // rewind the ByteBuffer's index pointer
         }
     }
 
     /**
      * Representation of the rowkey associated with an index in descending order for data row content
      */
     private static class DescIndexRow extends IndexRow {
         private static final byte PREFIX = 0x08;
         private static final byte[] NOT_NULL_BYTES = {0x00};
         private static final byte[] NULL_BYTES = {0x01};
 
         public DescIndexRow(final long tableId, final long indexId,
                             final List<byte[]> records, final UUID uuid) {
             super(tableId, indexId, records, uuid, PREFIX, NOT_NULL_BYTES, NULL_BYTES);
         }
 
         @Override
         public SortOrder getSortOrder() {
             return SortOrder.Descending;
         }
     }
 
     /**
      * Representation of the rowkey associated with an index in ascending order for data row content
      */
     private static class AscIndexRow extends IndexRow {
         private static final byte PREFIX = 0x07;
         private static final byte[] NOT_NULL_BYTES = {0x01};
         private static final byte[] NULL_BYTES = {0x00};
 
         public AscIndexRow(final long tableId, final long indexId,
                            final List<byte[]> records, final UUID uuid) {
             super(tableId, indexId, records, uuid, PREFIX, NOT_NULL_BYTES, NULL_BYTES);
         }
 
         @Override
         public SortOrder getSortOrder() {
             return SortOrder.Ascending;
         }
     }
 }
