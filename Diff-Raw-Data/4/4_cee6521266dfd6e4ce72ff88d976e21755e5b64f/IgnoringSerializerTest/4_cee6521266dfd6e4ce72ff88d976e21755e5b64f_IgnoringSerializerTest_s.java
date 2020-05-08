 package com.cedarsoft.serialization.jackson;
 
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonParser;
 import org.junit.*;
 import org.junit.experimental.theories.*;
 import org.junit.runner.*;
 
 import javax.annotation.Nonnull;
 import java.io.ByteArrayInputStream;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 @RunWith( Theories.class )
 public class IgnoringSerializerTest {
 
   private IgnoringSerializer serializer;
 
   @Before
   public void setUp() throws Exception {
     serializer = new IgnoringSerializer();
   }
 
   @Theory
   public void testIt( @Nonnull String json ) throws Exception {
     JsonFactory jsonFactory = JacksonSupport.getJsonFactory();
     JsonParser parser = jsonFactory.createJsonParser( new ByteArrayInputStream( json.getBytes() ) );
 
    Void result = serializer.deserialize( parser );
     assertThat( result ).isNull();
   }
 
   @DataPoints
   public static String[] testIt() throws Exception {
     return new String[]{
       "{}",
       "[]",
       "[1,2,3,4]",
       "{\"id\":123}",
       "{\"id\":[123]}",
       "{\"id\":{\"value\":123}}",
       "\"\""
     };
   }
 }
