 package net.madz.db.core.meta.mutable.mysql.impl;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import net.madz.db.core.meta.immutable.mysql.MySQLColumnMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLIndexMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLSchemaMetaData;
 import net.madz.db.core.meta.immutable.mysql.MySQLTableMetaData;
 import net.madz.db.core.meta.immutable.mysql.enums.MySQLIndexMethod;
 import net.madz.db.core.meta.immutable.mysql.impl.MySQLIndexMetaDataImpl;
 import net.madz.db.core.meta.immutable.types.KeyTypeEnum;
 import net.madz.db.core.meta.immutable.types.SortDirectionEnum;
 import net.madz.db.core.meta.mutable.impl.BaseIndexMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLColumnMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLForeignKeyMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLIndexMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLSchemaMetaDataBuilder;
 import net.madz.db.core.meta.mutable.mysql.MySQLTableMetaDataBuilder;
 import net.madz.db.utils.ResourceManagementUtils;
import net.madz.db.utils.Utilities;
 
 public class MySQLIndexMetaDataBuilderImpl
         extends
         BaseIndexMetaDataBuilder<MySQLSchemaMetaDataBuilder, MySQLTableMetaDataBuilder, MySQLColumnMetaDataBuilder, MySQLForeignKeyMetaDataBuilder, MySQLIndexMetaDataBuilder, MySQLSchemaMetaData, MySQLTableMetaData, MySQLColumnMetaData, MySQLForeignKeyMetaData, MySQLIndexMetaData>
         implements MySQLIndexMetaDataBuilder {
 
     private MySQLIndexMethod indexMethod;
     private String indexComment;
 
     public MySQLIndexMetaDataBuilderImpl(MySQLTableMetaDataBuilder table, String indexName) {
         super(table, indexName);
     }
 
     @Override
     public MySQLIndexMetaDataBuilder build(Connection conn) throws SQLException {
         Statement stmt = conn.createStatement();
         // TABLE_CATALOG | TABLE_SCHEMA | TABLE_NAME | NON_UNIQUE | INDEX_SCHEMA
         // | INDEX_NAME | SEQ_IN_INDEX | COLUMN_NAME | COLLATION | CARDINALITY |
         // SUB_PART | PACKED | NULLABLE | INDEX_TYPE | COMMENT | INDEX_COMMENT |
         stmt.executeQuery("use information_schema;");
         ResultSet rs = null;
         try {
             rs = stmt.executeQuery("SELECT * FROM statistics WHERE table_schema='" + this.indexPath.getParent().getParent().getName() + "' AND table_name='"
                    + this.indexPath.getParent().getName() + "' AND index_name='" + Utilities.handleSpecialCharacters(this.indexPath.getName()) + "';");
             int count = 0;
             while ( rs.next() ) {
                 if ( count == 0 ) {
                     this.isUnique = !rs.getBoolean("non_unique");
                     if ( this.isUnique )
                         this.keyType = KeyTypeEnum.uniqueKey;
                     else
                         this.keyType = KeyTypeEnum.index;
                     // TODO index type??? moved to jdbc level
                     this.sortDirection = SortDirectionEnum.getSortDirection(rs.getString("collation"));
                     this.cardinatlity = rs.getInt("cardinality");
                     // TODO pages ?? moved to jdbc level
                     this.indexMethod = MySQLIndexMethod.getIndexMethod(rs.getString("index_type"));
                     this.indexComment = rs.getString("INDEX_COMMENT");
                 }
                 count++;
                 short subPart = rs.getShort("sub_part");
                 short position = rs.getShort("seq_in_index");
                 MySQLColumnMetaDataBuilder column = this.table.getColumnBuilder(rs.getString("column_name"));
                 BaseIndexMetaDataBuilder.Entry entry = new BaseIndexMetaDataBuilder.Entry(this, subPart, column, position);
                 if ( this.isUnique ) {
                     column.appendUniqueIndexEntry(entry);
                 } else {
                     column.appendNonUniqueIndexEntry(entry);
                 }
                 this.addEntry(entry);
             }
         } finally {
             ResourceManagementUtils.closeResultSet(rs);
         }
         // TODO is packed, nullable belonged to entry
         // What is the difference of comment and index_comment?
         return this;
     }
 
     @Override
     public MySQLIndexMethod getIndexMethod() {
         return this.indexMethod;
     }
 
     @Override
     public void setIndexMethod(MySQLIndexMethod indexMethod) {
         this.indexMethod = indexMethod;
     }
 
     @Override
     public void setIndexComment(String indexComment) {
         this.indexComment = indexComment;
     }
 
     @Override
     public String getIndexComment() {
         return indexComment;
     }
 
     @Override
     public MySQLIndexMetaData createMetaData() {
         this.constructedMetaData = new MySQLIndexMetaDataImpl(this.table.getMetaData(), this);
         return this.constructedMetaData;
     }
 
     @Override
     public void setKeyType(KeyTypeEnum keyType) {
         this.keyType = keyType;
     }
 }
