 package net.madz.db.core.impl.mysql;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Collection;
 import java.util.List;
 
 import net.madz.db.core.AbsDatabaseGenerator;
 import net.madz.db.core.meta.immutable.ForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.IndexMetaData.Entry;
 import net.madz.db.core.meta.immutable.mysql.MySQLColumnMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLIndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLSchemaMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLTableMetaData;
 import net.madz.db.core.meta.immutable.types.KeyTypeEnum;
 import net.madz.db.utils.MessageConsts;
 
 public class MySQLDatabaseGeneratorImpl extends
         AbsDatabaseGenerator<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> {
 
     public MySQLDatabaseGeneratorImpl() {
     }
 
     @Override
     public String generateDatabase(final MySQLSchemaMetaData metaData, final Connection conn, final String targetDatabaseName) throws SQLException {
         if ( null == targetDatabaseName || 0 >= targetDatabaseName.length() ) {
             throw new IllegalArgumentException("Target " + MessageConsts.DATABASE_NAME_SHOULD_NOT_BE_NULL);
         }
         GenerateDatabase(metaData, conn, targetDatabaseName);
         // Generate tables
         GenerateTables(metaData, conn, targetDatabaseName);
         // Generate foreign keys
         GenerateForeignKeys(metaData, conn, targetDatabaseName);
         return targetDatabaseName;
     }
 
     private void GenerateDatabase(final MySQLSchemaMetaData metaData, final Connection conn, final String targetDatabaseName) throws SQLException {
         final Statement stmt = conn.createStatement();
         final StringBuilder result = new StringBuilder();
         result.append("CREATE DATABASE IF NOT EXISTS ");
         result.append(targetDatabaseName);
         if ( null != metaData.getCharSet() && 0 < metaData.getCharSet().length() ) {
             result.append(" DEFAULT CHARACTER SET = '");
             result.append(metaData.getCharSet());
             result.append("'");
         }
         if ( null != metaData.getCollation() && 0 < metaData.getCollation().length() ) {
             result.append(" DEFAULT COLLATE = '");
             result.append(metaData.getCollation());
             result.append("'");
         }
         result.append(";");
         stmt.execute(result.toString());
     }
 
     /**
      * @param metaData
      * @param conn
      * @param targetDatabaseName
      * @throws SQLException
      */
     private void GenerateTables(final MySQLSchemaMetaData metaData, final Connection conn, final String targetDatabaseName) throws SQLException {
         final Statement stmt = conn.createStatement();
         conn.setAutoCommit(false);
         stmt.executeUpdate("USE `" + targetDatabaseName + "`");
         for ( final MySQLTableMetaData table : metaData.getTables() ) {
             final StringBuilder result = new StringBuilder();
             result.append("CREATE TABLE IF NOT EXISTS `");
             result.append(table.getTableName());
             result.append("` (");
             if ( 0 >= table.getColumns().size() ) {
                 throw new IllegalStateException("Table: " + table.getTableName() + " has no columns.");
             }
             for ( final MySQLColumnMetaData column : table.getColumns() ) {
                 appendBackQuotation(result);
                 result.append(column.getColumnName());
                 appendBackQuotation(result);
                 appendSpace(result);
                 if ( null != column.getColumnType() ) {
                     result.append(column.getColumnType());
                     appendCharSet(result, column);
                     appendCollation(result, column);
                 } else {
                     result.append(assembleColumnType(column));
                 }
                 if ( column.isNullable() ) {
                     result.append(" NULL ");
                 } else {
                     result.append(" NOT NULL ");
                 }
                 if ( column.hasDefaultValue() ) {
                     result.append(" DEFAULT ");
                     if ( !column.getSqlTypeName().equalsIgnoreCase("BIT") ) {
                         result.append("'");
                     }
                     result.append(column.getDefaultValue());
                     if ( !column.getSqlTypeName().equalsIgnoreCase("BIT") ) {
                         result.append("'");
                     }
                 }
                 if ( column.isAutoIncremented() ) {
                     result.append(" AUTO_INCREMENT ");
                 }
                 if ( null != column.getRemarks() && 0 < column.getRemarks().length() ) {
                    result.append(" COMMENT '");
                     result.append(column.getRemarks());
                    result.append("'");
                     appendSpace(result);
                 }
                 result.append(",");
             }
             result.deleteCharAt(result.length() - 1);
             // Append primary keys
             final MySQLIndexMetaData pk = table.getPrimaryKey();
             if ( null != pk ) {
                 Collection<Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>> entrySet = pk
                         .getEntrySet();
                 if ( entrySet.size() > 0 ) {
                     result.append(", PRIMARY KEY(");
                     for ( Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> entry : entrySet ) {
                         final MySQLColumnMetaData column = entry.getColumn();
                         appendBackQuotation(result);
                         result.append(column.getColumnName());
                         appendBackQuotation(result);
                         result.append(",");
                     }
                     result.deleteCharAt(result.length() - 1);
                     result.append(")");
                 }
             }
             final Collection<MySQLIndexMetaData> indexSet = table.getIndexSet();
             for ( MySQLIndexMetaData index : indexSet ) {
                 final KeyTypeEnum keyType = index.getKeyType();
                 final Collection<Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>> entrySet = index
                         .getEntrySet();
                 // Append Unique keys
                 if ( keyType.equals(KeyTypeEnum.uniqueKey) ) {
                     result.append(",");
                     result.append("UNIQUE KEY");
                     appendIndexName(result, index);
                     result.append("(");
                     appendIndexEntries(result, entrySet);
                 }
                 // Append Indexes
                 if ( keyType.equals(KeyTypeEnum.index) ) {
                     result.append(",");
                     result.append("KEY");
                     appendIndexName(result, index);
                     result.append("(");
                     appendIndexEntries(result, entrySet);
                     if ( null != index.getIndexType() ) {
                         result.append("USING {");
                         result.append(index.getIndexType());
                         result.append("}");
                     }
                 }
             }
             result.append(") ");
             if ( null != table.getEngine() && 0 <= table.getEngine().name().length() ) {
                 result.append("ENGINE ");
                 result.append(table.getEngine());
                 appendSpace(result);
             }
             if ( null != table.getCharacterSet() ) {
                 result.append("CHARACTER SET ");
                 result.append(table.getCharacterSet());
                 appendSpace(result);
             }
             if ( null != table.getCollation() ) {
                 result.append("COLLATE ");
                 result.append(table.getCollation());
                 appendSpace(result);
             }
             if ( null != table.getRemarks() && 0 < table.getRemarks().length() ) {
                 result.append("COMMENT '");
                 result.append(table.getRemarks());
                 result.append("'");
                 appendSpace(result);
             }
             result.append(";");
             System.out.println(result.toString());
             stmt.addBatch(result.toString());
         }
         stmt.executeBatch();
         conn.commit();
     }
 
     private void appendCollation(final StringBuilder result, final MySQLColumnMetaData column) {
         if ( null != column.getCollationName() ) {
             result.append(" COLLATE ");
             result.append(column.getCollationName());
             appendSpace(result);
         }
     }
 
     private void appendCharSet(final StringBuilder result, final MySQLColumnMetaData column) {
         if ( null != column.getCharacterSet() ) {
             result.append(" CHARACTER SET ");
             result.append(column.getCharacterSet());
             appendSpace(result);
         }
     }
 
     private String assembleColumnType(final MySQLColumnMetaData column) {
         final StringBuilder result = new StringBuilder();
         String sqlTypeName = column.getSqlTypeName();
         if ( null == sqlTypeName ) {
             throw new IllegalArgumentException(MessageConsts.SQL_TYPE_NAME_IS_NULL);
         }
         if ( sqlTypeName.equalsIgnoreCase("BIT") || sqlTypeName.equalsIgnoreCase("BINARY") || sqlTypeName.equalsIgnoreCase("VARBINARY") ) {
             result.append(sqlTypeName);
             result.append("(");
             result.append(column.getNumericPrecision());
             result.append(") ");
         } else if ( sqlTypeName.equalsIgnoreCase("TINYINT") || sqlTypeName.equalsIgnoreCase("SMALLINT") || sqlTypeName.equalsIgnoreCase("MEDIUMINT")
                 || sqlTypeName.equalsIgnoreCase("INT") || sqlTypeName.equalsIgnoreCase("INTEGER") || sqlTypeName.equalsIgnoreCase("BIGINT") ) {
             result.append(sqlTypeName);
             result.append("(");
             result.append(column.getNumericPrecision());
             result.append(")");
             appendSpace(result);
             if ( column.isUnsigned() ) {
                 result.append("UNSIGNED");
                 appendSpace(result);
             }
             if ( column.isZeroFill() ) {
                 result.append("ZEROFILL");
                 appendSpace(result);
             }
         } else if ( sqlTypeName.equalsIgnoreCase("REAL") || sqlTypeName.equalsIgnoreCase("DOUBLE") || sqlTypeName.equalsIgnoreCase("FLOAT")
                 || sqlTypeName.equalsIgnoreCase("DECIMAL") || sqlTypeName.equalsIgnoreCase("NUMERIC") ) {
             result.append(sqlTypeName);
             result.append("(");
             result.append(column.getNumericPrecision());
             result.append(",");
             result.append(column.getNumericScale());
             result.append(")");
             if ( column.isUnsigned() ) {
                 result.append("UNSIGNED");
                 appendSpace(result);
             }
             if ( column.isZeroFill() ) {
                 result.append("ZEROFILL");
                 appendSpace(result);
             }
         } else if ( sqlTypeName.equalsIgnoreCase("CHAR") || sqlTypeName.equalsIgnoreCase("VARCHAR") ) {
             result.append(sqlTypeName);
             result.append("(");
             result.append(column.getCharacterMaximumLength());
             result.append(")");
             appendSpace(result);
             appendCharSet(result, column);
             if ( column.isCollationWithBin() ) {
                 result.append(" BINARY ");
             }
             appendCollation(result, column);
         } else if ( sqlTypeName.equalsIgnoreCase("TINYTEXT") || sqlTypeName.equalsIgnoreCase("TEXT") || sqlTypeName.equalsIgnoreCase("MEDIUMTEXT")
                 || sqlTypeName.equalsIgnoreCase("LONGTEXT") ) {
             result.append(sqlTypeName);
             if ( column.isCollationWithBin() ) {
                 appendSpace(result);
                 result.append("BINARY");
                 appendSpace(result);
             }
             appendCharSet(result, column);
             appendCollation(result, column);
         } else if ( sqlTypeName.toUpperCase().contains("ENUM") || sqlTypeName.toUpperCase().contains("SET") ) {
             result.append(sqlTypeName);
             result.append("(");
             for ( String value : column.getTypeValues() ) {
                 result.append(value);
                 result.append(",");
             }
             result.deleteCharAt(result.length() - 1);
             result.append(")");
             appendSpace(result);
             appendCharSet(result, column);
             appendCollation(result, column);
         } else {
             result.append(sqlTypeName);
         }
         return result.toString();
     }
 
     private void GenerateForeignKeys(MySQLSchemaMetaData metaData, Connection conn, String targetDatabaseName) throws SQLException {
         final Statement stmt = conn.createStatement();
         stmt.execute("USE " + targetDatabaseName + ";");
         final Collection<MySQLTableMetaData> tables = metaData.getTables();
         for ( MySQLTableMetaData table : tables ) {
             final Collection<MySQLForeignKeyMetaData> foreignKeySet = table.getForeignKeySet();
             if ( null != foreignKeySet && 0 < foreignKeySet.size() ) {
                 for ( MySQLForeignKeyMetaData fk : foreignKeySet ) {
                     final StringBuilder result = new StringBuilder();
                     result.append("ALTER TABLE ");
                     appendBackQuotation(result);
                     result.append(table.getTableName());
                     appendBackQuotation(result);
                     appendSpace(result);
                     result.append("ADD ");
                     if ( null != fk.getForeignKeyName() && 0 < fk.getForeignKeyName().length() ) {
                         result.append("CONSTRAINT ");
                         appendBackQuotation(result);
                         result.append(fk.getForeignKeyName());
                         appendBackQuotation(result);
                     }
                     result.append("FOREIGN KEY ");
                     if ( null != fk.getForeignKeyIndex() && 0 < fk.getForeignKeyIndex().getIndexName().length() ) {
                         result.append(fk.getForeignKeyIndex().getIndexName());
                     }
                     result.append("(");
                     final List<ForeignKeyMetaData.Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>> entrySet = fk
                             .getEntrySet();
                     for ( ForeignKeyMetaData.Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> entry : entrySet ) {
                         appendBackQuotation(result);
                         result.append(entry.getForeignKeyColumn().getColumnName());
                         appendBackQuotation(result);
                         result.append(",");
                     }
                     result.deleteCharAt(result.length() - 1);
                     result.append(")");
                     appendSpace(result);
                     result.append("REFERENCES ");
                     appendBackQuotation(result);
                     result.append(fk.getPrimaryKeyTable().getTableName());
                     appendBackQuotation(result);
                     result.append("(");
                     for ( ForeignKeyMetaData.Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> entry : entrySet ) {
                         appendBackQuotation(result);
                         result.append(entry.getPrimaryKeyColumn().getColumnName());
                         appendBackQuotation(result);
                         result.append(",");
                     }
                     result.deleteCharAt(result.length() - 1);
                     result.append(");");
                     System.out.println(result.toString());
                     stmt.addBatch(result.toString());
                 }
             }
         }
         stmt.executeBatch();
         conn.commit();
     }
 
     private void appendIndexEntries(final StringBuilder result,
             final Collection<Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>> entrySet) {
         for ( Entry<MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData> entry : entrySet ) {
             appendBackQuotation(result);
             result.append(entry.getColumn().getColumnName());
             appendBackQuotation(result);
             appendSpace(result);
             result.append(",");
         }
         result.deleteCharAt(result.length() - 1);
         result.append(")");
     }
 
     private void appendIndexName(final StringBuilder result, MySQLIndexMetaData index) {
         if ( null != index.getIndexName() && index.getIndexName().length() > 0 ) {
             appendSpace(result);
             appendBackQuotation(result);
             result.append(index.getIndexName());
             appendBackQuotation(result);
         }
     }
 
     private void appendSpace(final StringBuilder result) {
         result.append(" ");
     }
 
     private void appendBackQuotation(final StringBuilder result) {
         result.append("`");
     }
 }
