 package com.shedhack.testing.neo4j.json;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 
 import com.shedhack.testing.neo4j.dto.SeedData;
 import com.shedhack.testing.neo4j.exception.SeedDataException;
 
 /**
  * Test cases for the {@link JsonToSeedDataMapper} class.
  */
 public class JsonToSeedDataMapperTest
 {
     private static final String JSON_FILE = "json/test.json";
 
     private JsonToSeedDataMapper mapper = new JsonToSeedDataMapper();
 
     /**
      * Should throw exception with missing file.
      */
     @Test(expected = SeedDataException.class)
     public void shouldThrowExceptionWithMissingFile()
     {
         mapper.mapToSeedData(null);
     }
 
     /**
      * Should create seed data object.
      */
     @Test
     public void shouldCreateSeedDataObject()
     {
         SeedData data = mapper.mapToSeedData(JSON_FILE);
 
         // validate
         assertNotNull(data);
         assertEquals(4, data.getEntities().size());
         assertEquals(3, data.getRelationships().size());
 
         // check that the correct number of indexes for the entities
         assertEquals(3, data.getEntities().get(0).getIndexes().size());
         assertEquals(0, data.getEntities().get(1).getIndexes().size());
        assertEquals(1, data.getEntities().get(2).getIndexes().size());
        assertEquals(1, data.getEntities().get(3).getIndexes().size());
 
         // check that the correct number of properties for the entities
         assertEquals(3, data.getEntities().get(0).getProperties().size());
         assertEquals(1, data.getEntities().get(1).getProperties().size());
         assertEquals(2, data.getEntities().get(2).getProperties().size());
         assertEquals(2, data.getEntities().get(3).getProperties().size());
 
         // check the number of properties for the relationships
         assertEquals(1, data.getRelationships().get(0).getProperties().size());
 
         // check the types
         assertEquals("com.shedhack.dummy.app.example.spring.entity.UserEntity", data.getEntities().get(0).getType());
         assertEquals("com.shedhack.dummy.app.example.spring.entity.AddressEntity", data.getEntities().get(1).getType());
         assertEquals("com.shedhack.dummy.app.example.spring.entity.HobbyEntity", data.getEntities().get(2).getType());
         assertEquals("com.shedhack.dummy.app.example.spring.entity.HobbyEntity", data.getEntities().get(3).getType());
     }
 }
