 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.util.db;
 
 import de.escidoc.core.common.util.IOUtils;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * This class represents the table structure and the index list of a database.
  * It can be used to compare the current structure with a structure stored in a
  * file to see if the database is in the expected state.
  * 
  * @author André Schenk
  */
 public class Fingerprint implements Comparable<Object> {
 
     // Java version string may differ but is not important for equality.
     // taken from method compareTo in order to make it static final
     private static final String JAVA_VERSION_PATTERN = "^<java version=\\S+";
 
     private static final Map<String, String> IGNORED_SCHEMAS =
         new HashMap<String, String>();
 
     static {
         IGNORED_SCHEMAS.put("information_schema", "");
         IGNORED_SCHEMAS.put("pg_catalog", "");
         IGNORED_SCHEMAS.put("pg_toast_temp_1", "");
         IGNORED_SCHEMAS.put("public", "");
     }
 
     private static final Set<String> VALID_SM_TABLES = new HashSet<String>();
 
     static {
         VALID_SM_TABLES.add("agg_stat_data_selectors");
         VALID_SM_TABLES.add("aggregation_definitions");
         VALID_SM_TABLES.add("aggregation_table_fields");
         VALID_SM_TABLES.add("aggregation_table_index_fields");
         VALID_SM_TABLES.add("aggregation_table_indexes");
         VALID_SM_TABLES.add("aggregation_tables");
         VALID_SM_TABLES.add("preprocessing_logs");
         VALID_SM_TABLES.add("report_definition_roles");
         VALID_SM_TABLES.add("report_definitions");
         VALID_SM_TABLES.add("scopes");
         VALID_SM_TABLES.add("statistic_data");
     }
 
     private Schema[] schemas;
 
     /**
      * Constructor for bean deserialization.
      */
     public Fingerprint() {
     }
 
     /**
      * Create a new finger print from the given database connection.
      * 
      * @param conn
      *            database connection
      * 
      * @throws IOException
      *             Thrown if the XML file could not be written.
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     public Fingerprint(final Connection conn) throws IOException, SQLException {
         final ArrayList<Schema> schemas = new ArrayList<Schema>();
 
         for (final String schemaName : getSchemaNames(conn)) {
             final ArrayList<Table> tables = new ArrayList<Table>();
 
             for (final String tableName : getTableNames(conn, schemaName)) {
                 tables.add(new Table(tableName, getColumns(conn, schemaName,
                     tableName), getIndexInfo(conn, schemaName, tableName),
                     getPrimaryKeys(conn, schemaName, tableName),
                     getImportedKeys(conn, schemaName, tableName)));
             }
             schemas.add(new Schema(schemaName, tables.toArray(new Table[tables
                 .size()])));
         }
         setSchemas(schemas.toArray(new Schema[schemas.size()]));
 
         // store current finger print for debugging
         writeObject(new FileOutputStream(System.getProperty("java.io.tmpdir")
             + "/fingerprint.xml"));
     }
 
     /**
      * Compares to finger prints.
      * 
      * @param o
      *            the Object to be compared.
      * 
      * @return a negative integer, zero, or a positive integer as this object is
      *         less than, equal to, or greater than the specified object.
      */
     @Override
     public int compareTo(final Object o) {
         try {
             final ByteArrayOutputStream b1 = new ByteArrayOutputStream();
             final ByteArrayOutputStream b2 = new ByteArrayOutputStream();
 
             writeObject(b1);
             ((Fingerprint) o).writeObject(b2);
             return b1
                 .toString().replaceAll(JAVA_VERSION_PATTERN, "")
                 .compareTo(b2.toString().replaceAll(JAVA_VERSION_PATTERN, ""));
         }
         catch (final IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Compares this finger print to the specified object. The result is true if
      * and only if the argument is not null and is a Fingerprint object that
      * represents the same database structure as this object.
      * 
      * @param anObject
      *            The object to compare this finger print against
      * 
      * @return true if the given object represents a finger print equivalent to
      *         this finger print, false otherwise
      */
     @Override
     public boolean equals(Object anObject) {
         return (anObject != null) && (compareTo(anObject) == 0);
     }
 
     /**
      * Get a list of all table columns for the given combination of schema name
      * and table name.
      * 
      * @param conn
      *            database connection
      * @param schema
      *            schema name
      * @param table
      *            table name
      * 
      * @return list of all table columns
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getColumns(
         final Connection conn, final String schema, final String table)
         throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs =
             metaData.getColumns(conn.getCatalog(), schema, table, null);
         try {
             while (rs.next()) {
                 final StringBuilder column = new StringBuilder();
                 for (int index = 4; index <= 22; index++) {
                     // ignore column position
                     if (index != 17) {
                         if (column.length() > 0) {
                             column.append('/');
                         }
                         column.append(rs.getString(index));
                     }
                 }
                 result.add(column.toString());
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Get a list of all foreign keys that are defined for the table.
      * 
      * @param conn
      *            database connection
      * @param schema
      *            schema name
      * @param table
      *            table name
      * 
      * @return list of all foreign keys
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getImportedKeys(
         final Connection conn, final String schema, final String table)
         throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs =
             metaData.getImportedKeys(conn.getCatalog(), schema, table);
         try {
             while (rs.next()) {
                 final StringBuilder indexInfo = new StringBuilder();
                 for (int index = 4; index <= 14; index++) {
                     if (indexInfo.length() > 0) {
                         indexInfo.append('/');
                     }
                     indexInfo.append(rs.getString(index));
                 }
                 result.add(indexInfo.toString());
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Get a list of all table indexes for the given combination of schema name
      * and table name.
      * 
      * @param conn
      *            database connection
      * @param schema
      *            schema name
      * @param table
      *            table name
      * 
      * @return list of all table indexes
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getIndexInfo(
         final Connection conn, final String schema, final String table)
         throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs =
             metaData
                 .getIndexInfo(conn.getCatalog(), schema, table, false, true);
         try {
             while (rs.next()) {
                 final StringBuilder indexInfo = new StringBuilder();
                 for (int index = 4; index <= 10; index++) {
                     if (indexInfo.length() > 0) {
                         indexInfo.append('/');
                     }
                     indexInfo.append(rs.getString(index));
                 }
                 result.add(indexInfo.toString());
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Get a list of all primary keys that are defined for the table.
      * 
      * @param conn
      *            database connection
      * @param schema
      *            schema name
      * @param table
      *            table name
      * 
      * @return list of all primary keys
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getPrimaryKeys(
         final Connection conn, final String schema, final String table)
         throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs =
             metaData.getPrimaryKeys(conn.getCatalog(), schema, table);
         try {
             while (rs.next()) {
                 final StringBuilder indexInfo = new StringBuilder();
                 for (int index = 4; index <= 6; index++) {
                     if (indexInfo.length() > 0) {
                         indexInfo.append('/');
                     }
                     indexInfo.append(rs.getString(index));
                 }
                 result.add(indexInfo.toString());
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Get a list of all schemas for the given connection.
      * 
      * @param conn
      *            database connection
      * 
      * @return list of all schemas
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getSchemaNames(final Connection conn)
         throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs = metaData.getSchemas();
         try {
             while (rs.next()) {
                 final String schema = rs.getString(1);
                 if (!IGNORED_SCHEMAS.containsKey(schema)) {
                     result.add(schema);
                 }
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Get all schemas.
      * 
      * @return schema list
      */
     public Schema[] getSchemas() {
         return this.schemas;
     }
 
     /**
      * This class will never be inserted into a HashMap/HashTable.
      * 
      * @return a hash code value for this object.
      */
     @Override
     public int hashCode() {
         assert false : "hashCode not designed";
         return 1;
     }
 
     /**
      * Get a list of all tables for the given schema.
      * 
      * @param conn
      *            database connection
      * @param schema
      *            schema name
      * 
      * @return list of all tables
      * @throws SQLException
      *             Thrown if an SQL statement failed to be executed.
      */
     private static String[] getTableNames(
         final Connection conn, final String schema) throws SQLException {
         final ArrayList<String> result = new ArrayList<String>();
         final DatabaseMetaData metaData = conn.getMetaData();
         final ResultSet rs =
             metaData.getTables(conn.getCatalog(), schema, null,
                 new String[] { "TABLE" });
         try {
             while (rs.next()) {
                 final String name = rs.getString(3);
                 // ignore dynamically created tables for statistics manager
                 if (!"sm".equals(schema) || VALID_SM_TABLES.contains(name)) {
                     result.add(name);
                 }
             }
         }
         finally {
             IOUtils.closeResultSet(rs);
         }
         return result.toArray(new String[result.size()]);
     }
 
     /**
      * Reads the next object from the underlying input stream.
      * 
      * @param filename
      *            source from which to read the object.
      * 
      * @return the next object read
      * @throws FileNotFoundException
      *             Thrown if the given file could not be found.
      */
     public static Fingerprint readObject(final String filename)
         throws FileNotFoundException {
         return readObject(new FileInputStream(filename));
     }
 
     /**
      * Reads the next object from the underlying input stream.
      * 
      * @param input
      *            source from which to read the object.
      * 
      * @return the next object read
      */
     public static Fingerprint readObject(final InputStream input) {
         final XMLDecoder d = new XMLDecoder(new BufferedInputStream(input));
 
         final Fingerprint result = (Fingerprint) d.readObject();
         d.close();
         return result;
     }
 
     /**
      * Set the schemas.
      * 
      * @param schemas
      *            schema list
      */
     public final void setSchemas(final Schema[] schemas) {
        this.schemas = schemas;
     }
 
     /**
      * Write an XML representation of the specified object to the output.
      * 
      * @param o
      *            The object to be written to the stream.
      * 
      * @throws IOException
      *             Thrown if the object could not be written.
      */
     public final void writeObject(final OutputStream o) throws IOException {
         final XMLEncoder e = new XMLEncoder(new BufferedOutputStream(o));
 
         e.writeObject(this);
         e.close();
     }
 }
