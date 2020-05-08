 package com.cedarsoft.couchdb;
 
 import junit.framework.TestCase;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class KeyTest extends TestCase {
   public void testDefault() throws Exception {
     assertThat( new Key( "asdf" ) ).isEqualTo( new Key( "asdf" ) );
   }
 
   public void testArray() throws Exception {
     assertThat( Key.array().getJson() ).isEqualTo( "[]" );
    assertThat( Key.array( "a", "b", "c" ).getJson() ).isEqualTo( "[\"a\",\"b\",\"c\"]" );
   }
 
   public void testEndKey() throws Exception {
     try {
       Key.endArray();
       fail( "Where is the Exception" );
     } catch ( IllegalArgumentException e ) {
       assertThat( e ).hasMessage( "Need at least one element" );
     }
 
    assertThat( Key.endArray( "a" ).getJson() ).isEqualTo( "[\"a\",{}]" );
   }
 }
