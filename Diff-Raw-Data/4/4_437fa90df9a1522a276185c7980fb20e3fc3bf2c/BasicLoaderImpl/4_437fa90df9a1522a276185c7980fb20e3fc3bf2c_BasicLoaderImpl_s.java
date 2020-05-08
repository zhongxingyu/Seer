 package com.sapienter.jbilling.server.mediation.cache;
 
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.mediation.task.IMediationReader;
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.TransactionCallback;
 import org.springframework.transaction.support.TransactionTemplate;
 import org.springframework.util.StopWatch;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BasicLoaderImpl implements ILoader {
     private static final Logger LOG = Logger.getLogger(BasicLoaderImpl.class);
 
     private static final String SPACE = " ";
     private static final String COMMA = ", ";
 
     private JdbcTemplate jdbcTemplate = null;
     private TransactionTemplate transactionTemplate = null;
     private IMediationReader reader = null;
     private String tableName = "rules_table";
     private String indexName = "rules_table_idx";
     private String indexColumnNames = null;
 
     private boolean tableCreated = false;
 
     public BasicLoaderImpl() {
     }
 
     public JdbcTemplate getJdbcTemplate() {
         return jdbcTemplate;
     }
 
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     public TransactionTemplate getTransactionTemplate() {
         return transactionTemplate;
     }
 
     public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
         this.transactionTemplate = transactionTemplate;
     }
 
     public IMediationReader getReader() {
         return reader;
     }
 
     public void setReader(IMediationReader reader) {
         this.reader = reader;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public void setTableName(String tableName) {
         this.tableName = tableName;
     }
 
     public String getIndexName() {
         return indexName;
     }
 
     public void setIndexName(String indexName) {
         this.indexName = indexName;
     }
 
     public String getIndexColumnNames() {
         return indexColumnNames;
     }
 
     public void setIndexColumnNames(String indexColumnNames) {
         this.indexColumnNames = indexColumnNames;
     }
 
     /**
      * Initializes the loader, creating the in-memory database and inserting
      * all relevant data for future retrieval.
      */
     public void init() {
         initDB();
         if (tableCreated) this.createIndexes();
         LOG.debug("Loader initialized successfully.");
     }
 
     /**
      * Method removes the in-memory table and prepares the external data
      * source to be read again. The loader is destroyed when the Spring container is
      * shut down.
      */
     public void destroy() {
         jdbcTemplate.execute("DROP TABLE IF EXISTS " + getTableName() + ";");
     }
 
     protected void initDB() {
         LOG.debug("Initializing cached memory table '" + getTableName() + "'");
         LOG.debug("Index columns: " + indexColumnNames);
 
         StopWatch watch = new StopWatch();
         watch.start();
 
         if (reader.validate(new ArrayList<String>())) {
             try {
                 // /check if transactions can be begun here
                 for (List<Record> group : reader) {
                     LOG.debug("Loading " + group.size()+ " records.");
 
                     if (group.size() < 1)
                         return;
 
                     if (!tableCreated)
                         tableCreated = createTable(group.get(0));
 
                     // insert record using jdbcTemplate
                     final List<Record> batch = new ArrayList<Record>(group);
                     transactionTemplate.execute(new TransactionCallback() {
                         public Object doInTransaction(TransactionStatus ts) {
                             return jdbcTemplate.batchUpdate(computeInsertSql(batch));
                         }
                     });
                 }
             } catch (Throwable t) {
                 LOG.error("Unhandled exception occurred during loading.", t);
             }
         }
 
         watch.stop();
         LOG.debug("Finished loading cached memory table in (secs): " + watch.getTotalTimeSeconds());
         reader = null; // discard reader, not used after initalization.
     }
 
     private boolean createIndexes() {
         if (null == indexColumnNames || indexColumnNames.length() == 0) {
             indexColumnNames = CACHE_PRIMARY_KEY;
         }
 
         String[] cols = indexColumnNames.split(",");
         StringBuffer indexSql = new StringBuffer()
                 .append("CREATE INDEX ")
                 .append(getIndexName())
                 .append(" ON ");
 
         indexSql.append(getTableName()).append("(");
         for (String col : cols) {
             indexSql.append(col).append(COMMA);
         }
 
         int lastIdxOfComma = indexSql.lastIndexOf(COMMA);
         indexSql.replace(lastIdxOfComma, (indexSql.lastIndexOf(COMMA) + 2), ")");
 
         LOG.debug("Schema Index: '" + indexSql + "'");
         this.jdbcTemplate.execute(indexSql.toString());
         return true;
     }
 
     private String[] computeInsertSql(List<Record> records) {
         int totalRecords = records.size();
         String[] batch = new String[totalRecords--];
         StringBuffer retVal = new StringBuffer("INSERT INTO ").append(getTableName()).append(SPACE).append("(");
 
         int outer = 0;
         for (Record record : records) {
 
             StringBuffer values = new StringBuffer("");
             List<PricingField> fields = record.getFields();
 
             int listSize= fields.size();
             values.append("(");
 
             for (int iter = 1; iter <= listSize; iter++) {
 
                 PricingField field = (PricingField) fields.get(iter-1);
                 if ( 0 == outer) {
                     retVal.append(field.getName());
                 }
 
                 // setting value with appropriate markers into the sql string
                 switch (field.getType()) {
                     case STRING:
                         values.append("'")
                                 .append(escapeSpecialChars((String) field.getValue()))
                                 .append("'");
                         break;
 
                     default:
                         values.append(field.getValue());
                         break;
                 }
                 if ( 0 == outer ) {
                     if ( iter < listSize ) {
                         retVal.append(COMMA);
                     } else {
                         retVal.append(") VALUES ");
                     }
                 }
 
                 if (iter < listSize) {
                     values.append(COMMA);
                 }
             }
 
             values.append(");");
             batch[outer]= (retVal.toString().concat(values.toString()));
             outer++;
         }
 
         return batch;
     }
 
     private static String escapeSpecialChars(String value) {
        if (value.indexOf("'") > -1) {
            value= value.replaceAll("'", "''");
         }
         return value;
     }
 
     /**
      *
      * @param record
      * @return
      */
     private boolean createTable(Record record) {
         StringBuffer createTable = new StringBuffer()
                 .append("CREATE CACHED TABLE ")
                 .append(getTableName())
                 .append(SPACE);
 
         // indexes
         createTable
                 .append("(")
                 .append(CACHE_PRIMARY_KEY)
                 .append(" identity NOT NULL PRIMARY KEY,");
 
         List<PricingField> fields = record.getFields();
         for (PricingField field : fields) {
             createTable.append(field.getName()).append(SPACE).append(mapDBType(field));
             createTable.append(COMMA);
         }
 
         int lastIdxOfComma = createTable.lastIndexOf(COMMA);
         createTable.replace(lastIdxOfComma, (createTable.lastIndexOf(COMMA) + 2), ")");
 
         // execute create table SQL
         LOG.debug("Schema DDL: '" + createTable + "'");
         this.jdbcTemplate.execute(createTable.toString());
         return true;
     }
 
     /**
      *
      * @param field
      * @return
      */
     private static final String mapDBType(PricingField field) {
         String retVal = null;
         switch (field.getType()) {
         case STRING:
             retVal = " VARCHAR(100) ";
             break;
         case INTEGER:
             retVal = " INTEGER ";
             break;
         case DECIMAL:
             retVal = " NUMERIC(22,10) ";
             break;
         case DATE:
             retVal = " TIMESTAMP ";
             break;
         case BOOLEAN:
             retVal = " BOOLEAN ";
             break;
         }
         return retVal;
     }
 }
