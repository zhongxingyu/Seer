 package com.txtr.hibernatedelta.generator;
 
 import java.math.BigInteger;
 import java.sql.Connection;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.reflect.FieldUtils;
 
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 import com.txtr.hibernatedelta.SqlType;
 import com.txtr.hibernatedelta.SqlUtil;
 import com.txtr.hibernatedelta.model.HibernateColumn;
 import com.txtr.hibernatedelta.model.HibernateDatabase;
 import com.txtr.hibernatedelta.model.HibernateIndex;
 import com.txtr.hibernatedelta.model.HibernateIndexType;
 import com.txtr.hibernatedelta.model.HibernateIndexUtil;
 import com.txtr.hibernatedelta.model.HibernateTable;
 import liquibase.database.Database;
 import liquibase.database.DatabaseFactory;
 import liquibase.database.core.OracleDatabase;
 import liquibase.database.jvm.JdbcConnection;
 import liquibase.datatype.DataTypeFactory;
 import liquibase.integration.commandline.CommandLineUtils;
 import liquibase.snapshot.DatabaseSnapshot;
 import liquibase.snapshot.SnapshotControl;
 import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.structure.DatabaseObject;
 import liquibase.structure.core.Column;
 import liquibase.structure.core.DataType;
 import liquibase.structure.core.DataType.ColumnSizeUnit;
 import liquibase.structure.core.ForeignKey;
 import liquibase.structure.core.Index;
 import liquibase.structure.core.PrimaryKey;
 import liquibase.structure.core.Schema;
 import liquibase.structure.core.Sequence;
 import liquibase.structure.core.Table;
 import liquibase.structure.core.UniqueConstraint;
 
 public class LiquibaseModelFactory {
 
     public static final OracleDatabase DATABASE = new OracleDatabase();
     public static final int MAX_CHAR_SIZE = 4000;
 
     private static final Schema SCHEMA = new Schema();
 
     private LiquibaseModelFactory() {
     }
 
     static {
         SnapshotGeneratorFactory.getInstance().register(new LiquibaseSnapshotGenerator());
         try {
             FieldUtils.writeDeclaredStaticField(DataTypeFactory.class, "instance", new LiquibaseDataTypeFactory(), true);
         } catch (IllegalAccessException e) {
             throw Throwables.propagate(e);
         }
     }
 
     public static DatabaseSnapshot readSnapshotFromDatabase(String jdbcUrl, String userName, String password) throws Exception {
         Database database = CommandLineUtils.createDatabaseObject(
             Thread.currentThread().getContextClassLoader(),
                 jdbcUrl,
                 userName,
                 password,
                 "oracle.jdbc.driver.OracleDriver",
                 null,
                 null,
                 null,
                 null);
 
 
         return prepareDatabase(database);
     }
 
     private static DatabaseSnapshot prepareDatabase(Database database) throws Exception {
        return removeIgnoredTables(SnapshotGeneratorFactory.getInstance().createSnapshot(new DatabaseObject[]{}, database, new SnapshotControl()));
     }
 
     public static DatabaseSnapshot readSnapshotFromDatabase(Connection connection) throws Exception {
         DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
         Database database = databaseFactory.findCorrectDatabaseImplementation(new JdbcConnection(connection));
 
         return prepareDatabase(database);
     }
 
     public static DatabaseSnapshot removeIgnoredTables(DatabaseSnapshot snapshot) {
         for (Table table : Lists.newArrayList(snapshot.get(Table.class))) {
             if (SqlUtil.isIgnoredTable(table.getName())) {
                 snapshot.remove(table);
             }
         }
 
         for (UniqueConstraint constraint : Lists.newArrayList(snapshot.get(UniqueConstraint.class))) {
             if (SqlUtil.isIgnoredTable(constraint.getTable().getName())) {
                 snapshot.remove(constraint);
             }
         }
 
         for (Index index : Lists.newArrayList(snapshot.get(Index.class))) {
             if (SqlUtil.isIgnoredTable(index.getTable().getName()) || SqlUtil.isIgnoredIndex(index.getName())) {
                 snapshot.remove(index);
             }
         }
 
         for (PrimaryKey primaryKey : Lists.newArrayList(snapshot.get(PrimaryKey.class))) {
             if (SqlUtil.isIgnoredTable(primaryKey.getTable().getName())) {
                 snapshot.remove(primaryKey);
             }
         }
 
         for (ForeignKey foreignKey : Lists.newArrayList(snapshot.get(ForeignKey.class))) {
             if (SqlUtil.isIgnoredTable(foreignKey.getForeignKeyTable().getName())) {
                 snapshot.remove(foreignKey);
             }
         }
 
         return snapshot;
     }
 
     public static DatabaseSnapshot create(HibernateDatabase database) {
         try {
             DatabaseSnapshot result = createEmptySnapshot();
             final List<HibernateTable> tables = removeVirtualTables(database.getTables());
 
             for (HibernateTable hibernateTable : tables) {
                 result.add(createTable(hibernateTable));
             }
 
             for (HibernateTable hibernateTable : tables) {
                 Table table = result.getTable(hibernateTable.getName());
 
                 String sequenceName = hibernateTable.getSequenceName();
                 if (sequenceName != null) {
                     Sequence sequence = new Sequence();
                     sequence.setSchema(SCHEMA);
                     sequence.setName(sequenceName);
                     sequence.setIncrementBy(BigInteger.valueOf(SqlUtil.SEQUENCE_INCREMENT));
                     sequence.setStartValue(BigInteger.valueOf(SqlUtil.SEQUENCE_START));
                     result.add(sequence);
                 }
 
                 for (HibernateIndex hibernateIndex : HibernateIndexUtil.getIndexes(hibernateTable)) {
                     HibernateIndexType type = hibernateIndex.getType();
                     if (type.isPrimaryKey()) {
                         result.add(createPrimaryKey(hibernateIndex, table));
                     } else {
                         if (type.isUnique() && type.isConstraint()) {
                             result.add(createUniqueConstraint(hibernateIndex, table));
                         }
 
                         result.add(createIndex(hibernateIndex, table));
                     }
                 }
 
                 for (HibernateColumn column : hibernateTable.getColumns()) {
                     if (column.getTargetTable() != null) {
                         result.add(createForeignKey(table, result.getTable(column.getTargetTable()), database.getTable(column.getTargetTable()), column.getName(), column.getForeignKeyIndexName() + "F"));
                     }
                     result.add(createColumn(column, table));
                 }
             }
 
             return result;
         } catch (Exception e) {
             throw Throwables.propagate(e);
         }
     }
 
     private static List<HibernateTable> removeVirtualTables(List<HibernateTable> tables) {
         final ArrayList<HibernateTable> result = new ArrayList<HibernateTable>();
         for (HibernateTable table : tables) {
             if (! table.isVirtualRootTable()){
                 result.add(table);
             }
         }
         return result;
     }
 
     public static DatabaseSnapshot createEmptySnapshot() {
         return new DatabaseSnapshot(DATABASE) { };
     }
 
     private static Index createIndex(HibernateIndex hibernateIndex, Table table) {
         Index result = new Index();
 
         result.setName(hibernateIndex.getName());
         result.getColumns().addAll(hibernateIndex.getColumns());
         result.setUnique(hibernateIndex.getType().isUnique());
         result.setTable(table);
 
         return result;
     }
 
     private static UniqueConstraint createUniqueConstraint(HibernateIndex hibernateIndex, Table table) {
         UniqueConstraint result = new UniqueConstraint();
         result.setName(hibernateIndex.getName() + "U");
         result.getColumns().addAll(hibernateIndex.getColumns());
         result.setTable(table);
         return result;
     }
 
     private static PrimaryKey createPrimaryKey(HibernateIndex hibernateIndex, Table table) {
         PrimaryKey result = new PrimaryKey();
         result.setTable(table);
         result.setName(hibernateIndex.getName() + "P");
         result.getColumnNamesAsList().addAll(hibernateIndex.getColumns());
         return result;
     }
 
     private static ForeignKey createForeignKey(Table table, Table targetTable, HibernateTable targetHibernateTable, String columnName, String name) {
         ForeignKey result = new ForeignKey();
         result.setPrimaryKeyTable(targetTable);
 
         List<String> names = new ArrayList<String>();
         for (HibernateColumn column : targetHibernateTable.getPrimaryKeyColumns()) {
             names.add(column.getName());
         }
 
         result.setPrimaryKeyColumns(StringUtils.join(names, ", "));
         result.setForeignKeyTable(table);
         result.setForeignKeyColumns(columnName);
         result.setDeferrable(true);
         result.setInitiallyDeferred(true);
         result.setName(name);
         return result;
     }
 
     private static Table createTable(HibernateTable hibernateTable) {
         Table result = new Table();
         result.setSchema(SCHEMA);
         result.setName(hibernateTable.getName());
         for (HibernateColumn column : hibernateTable.getColumns()) {
             result.getColumns().add(createColumn(column, result));
         }
 
         return result;
     }
 
     private static Column createColumn(HibernateColumn column, Table table) {
         Column result = new Column();
 
         DataType type = new DataType(column.getSqlType());
         result.setType(type);
         result.setName(column.getName());
         result.setRelation(table);
 
         if (column.getLength() != null) {
             type.setColumnSize(column.getLength());
         }
         if (column.getDecimalDigits() != null) {
             type.setDecimalDigits(column.getDecimalDigits());
         }
 
         SqlType sqlType = new SqlType(column.getSqlType());
         if (sqlType.getValue() == Types.VARCHAR) {
             type.setColumnSizeUnit(ColumnSizeUnit.CHAR);
         } else if (sqlType.getValue() == Types.CLOB) {
             type.setColumnSize(MAX_CHAR_SIZE);
         }
 
         result.setNullable(column.isNullable());
 
         return result;
     }
 
 
 }
