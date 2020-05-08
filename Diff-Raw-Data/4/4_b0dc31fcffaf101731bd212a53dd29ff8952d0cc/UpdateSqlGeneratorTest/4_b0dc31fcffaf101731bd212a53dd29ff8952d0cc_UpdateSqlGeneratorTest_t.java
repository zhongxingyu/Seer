 package com.txtr.hibernatedelta.generator;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.txtr.hibernatedelta.DatabaseWithIndexes;
 import com.txtr.hibernatedelta.generator.customer2.Customer;
 import com.txtr.hibernatedelta.model.HibernateDatabase;
 import com.txtr.hibernatedelta.model.HibernateIndexNames;
 import com.txtr.hibernatedelta.validator.EntityValidator;
 
 public class UpdateSqlGeneratorTest {
 
     @Test
     public void testAddTable() throws Exception {
         Assert.assertEquals("CREATE TABLE Customer (catalog_id NUMBER(19, 0) NOT NULL, id NUMBER(19, 0) NOT NULL, name VARCHAR2(255 CHAR), street VARCHAR2(255 CHAR));\n" +
             "ALTER TABLE Customer ADD CONSTRAINT CUSTOMER0P PRIMARY KEY (id);\n" +
             "CREATE INDEX CUSTOMER1 ON Customer(catalog_id) parallel 3 nologging;\n" +
             "ALTER TABLE Customer ADD CONSTRAINT Customer1F FOREIGN KEY (catalog_id) REFERENCES Customer (id) DEFERRABLE INITIALLY DEFERRED;",
             getChanges("emptySchema.xml", "emptyIndexNames.xml", "schema2.xml", "indexNames2.xml", ImmutableList.<Class<?>>of(Customer.class)));
     }
 
     @Test
     public void testAddColumn() throws Exception {
         Assert.assertEquals("ALTER TABLE Customer ADD street VARCHAR2(255 CHAR);\n" +
             "ALTER TABLE Customer ADD id NUMBER(19, 0) NOT NULL;\n" +
             "ALTER TABLE Customer ADD catalog_id NUMBER(19, 0) NOT NULL;\n" +
             "ALTER TABLE Customer ADD CONSTRAINT CUSTOMER0P PRIMARY KEY (id);\n" +
             "CREATE INDEX CUSTOMER1 ON Customer(catalog_id) parallel 3 nologging;\n" +
             "ALTER TABLE Customer ADD CONSTRAINT Customer1F FOREIGN KEY (catalog_id) REFERENCES Customer (id) DEFERRABLE INITIALLY DEFERRED;",
             getChanges("schema1.xml", "emptyIndexNames.xml", "schema2.xml", "indexNames2.xml", ImmutableList.<Class<?>>of(Customer.class)));
     }
 
     @Test
     public void testRemoveColumn() throws Exception {
        Assert.assertEquals(
             "ALTER TABLE Customer DROP PRIMARY KEY;\n" +
            "ALTER TABLE Customer DROP CONSTRAINT Customer1F;\n" +
             "DROP INDEX CUSTOMER1;\n" +
             "ALTER TABLE Customer DROP COLUMN street;\n" +
             "ALTER TABLE Customer DROP COLUMN id;\n" +
             "ALTER TABLE Customer DROP COLUMN catalog_id;",
             getChanges("schema2.xml", "emptyIndexNames.xml", "schema1.xml", "emptyIndexNames.xml", ImmutableList.<Class<?>>of(com.txtr.hibernatedelta.generator.customer1.Customer.class)));
     }
 
     @Test
     public void testRemoveTable() throws Exception {
         Assert.assertEquals("ALTER TABLE Customer DROP PRIMARY KEY;\n" +
             "ALTER TABLE Customer DROP CONSTRAINT Customer1F;\n" +
             "DROP INDEX CUSTOMER1;\n" +
             "ALTER TABLE Customer DROP COLUMN street;\n" +
             "ALTER TABLE Customer DROP COLUMN id;\n" +
             "ALTER TABLE Customer DROP COLUMN catalog_id;\n" +
             "ALTER TABLE Customer DROP COLUMN name;\n" +
             "DROP TABLE Customer;",
             getChanges("schema2.xml", "emptyIndexNames.xml", "emptySchema2.xml", "emptyIndexNames.xml", ImmutableList.<Class<?>>of()));
     }
 
     public String getChanges(String schema, String indexNames, String newSchema, String newIndexNames, ImmutableList<Class<?>> entities) throws Exception {
         URL names = getResource(indexNames);
         DatabaseWithIndexes newDatabase = new EntityValidator().verify(entities, names);
 
         String sql = new BackendSqlGenerator().createUpdateAgainstCommittedSchema(getResource(schema), newDatabase, names).trim();
 
         assertSchema(newSchema, newDatabase.getDatabase());
 
         assertIndexNames(newIndexNames, newDatabase.getIndexNames());
 
         return sql;
     }
 
     private void assertIndexNames(String newIndexNames, HibernateIndexNames indexNames) throws JAXBException, IOException {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         BackendSqlGenerator.getIndexMarshaller().marshal(indexNames, stream);
         Assert.assertEquals(IOUtils.toString(getResource(newIndexNames)), stream.toString().trim());
     }
 
     private void assertSchema(String newSchema, HibernateDatabase database) throws JAXBException, IOException {
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         BackendSqlGenerator.getSchemaMarshaller().marshal(database, stream);
         Assert.assertEquals(IOUtils.toString(getResource(newSchema)), stream.toString().trim());
     }
 
     private URL getResource(String indexNames) {
         return getClass().getClassLoader().getResource(indexNames);
     }
 }
