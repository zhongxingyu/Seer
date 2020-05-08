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
        Assert.assertEquals("CREATE TABLE Customer (name VARCHAR2(255));", getChanges("emptySchema.xml", "emptyIndexNames.xml", "schema1.xml", "emptyIndexNames.xml", com.txtr.hibernatedelta.generator.customer1.Customer.class));
     }
 
     @Test
     public void testAddColumn() throws Exception {
        Assert.assertEquals("ALTER TABLE Customer ADD street VARCHAR2(255);", getChanges("schema1.xml", "emptyIndexNames.xml", "schema2.xml", "emptyIndexNames.xml", Customer.class));
     }
 
     @Test
     public void testRemoveColumn() throws Exception {
         Assert.assertEquals("ALTER TABLE Customer DROP COLUMN street;", getChanges("schema2.xml", "emptyIndexNames.xml", "schema1.xml", "emptyIndexNames.xml", com.txtr.hibernatedelta.generator.customer1.Customer.class));
     }
 
     public String getChanges(String schema, String indexNames, String newSchema, String newIndexNames, Class<?> entity) throws Exception {
         URL names = getResource(indexNames);
         DatabaseWithIndexes newDatabase = new EntityValidator().verify(ImmutableList.<Class<?>>of(entity), names);
 
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
