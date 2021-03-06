 package org.codehaus.jackson.map.deser;
 
 import org.codehaus.jackson.map.BaseMapTest;
 
 import org.codehaus.jackson.annotate.*;
 import org.codehaus.jackson.map.*;
 import org.codehaus.jackson.map.annotate.*;
 
 /**
  * Unit tests for verifying that field-backed properties can also be
  * deserialized (since version 1.1) as well as
  * setter-accessible properties.
  */
 public class TestFieldDeserialization
     extends BaseMapTest
 {
     /*
     //////////////////////////////////////////////
     // Annotated helper classes
     //////////////////////////////////////////////
      */
 
     static class SimpleFieldBean
     {
         public int x, y;
 
         // not auto-detectable, not public
         int z;
 
         // ignored, not detectable either
         @JsonIgnore public int a;
     }
 
     static class SimpleFieldBean2
     {
         @JsonDeserialize String[] values;
     }
 
     @JsonAutoDetect(JsonMethod.SETTER)
     static class NoAutoDetectBean
     {
         // not auto-detectable any more
         public int z;
 
         @JsonProperty("z")
         public int _z;
     }
 
     // Let's test invalid bean too
     static class DupFieldBean
     {
         public int z;
 
         @JsonProperty("z")
         public int _z;
     }
 
    public class DupFieldBean2
    {
        @JsonProperty("foo")
        public int _z;

        @SuppressWarnings("unused")
        @JsonDeserialize
        private int foo;
    }

     /*
     //////////////////////////////////////////////
     // Main tests
     //////////////////////////////////////////////
      */
 
     public void testSimpleAutoDetect() throws Exception
     {
         ObjectMapper m = new ObjectMapper();
         SimpleFieldBean result = m.readValue("{ \"x\" : -13 }",
                                            SimpleFieldBean.class);
         assertEquals(-13, result.x);
         assertEquals(0, result.y);
     }
 
     public void testSimpleAnnotation() throws Exception
     {
         ObjectMapper m = new ObjectMapper();
         SimpleFieldBean2 bean = m.readValue("{ \"values\" : [ \"x\", \"y\" ] }",
                                            SimpleFieldBean2.class);
         String[] values = bean.values;
         assertNotNull(values);
         assertEquals(2, values.length);
        assertEquals("x", values[0]);
        assertEquals("y", values[1]);
     }
 
     public void testNoAutoDetect() throws Exception
     {
         ObjectMapper m = new ObjectMapper();
         NoAutoDetectBean bean = m.readValue("{ \"z\" : 7 }",
                                             NoAutoDetectBean.class);
        assertEquals(7, bean._z);
     }
 
     public void testFailureDueToDups() throws Exception
     {
         try {
             writeAndMap(new ObjectMapper(), new DupFieldBean());
         } catch (JsonMappingException e) {
             verifyException(e, "Multiple fields representing property");
         }
     }

    public void testFailureDueToDups2() throws Exception
    {
        try {
            writeAndMap(new ObjectMapper(), new DupFieldBean2());
        } catch (JsonMappingException e) {
            verifyException(e, "Multiple fields representing property");
        }
    }
 }
