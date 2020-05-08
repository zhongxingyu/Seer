 package com.cedarsoft.couchdb;
 
import com.google.common.base.Joiner;

 import javax.annotation.Nonnull;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class Key {
   @Nonnull
   public static final Key EMPTY_ARRAY = new Key( "[]" );
   @Nonnull
   public static final Key EMPTY_OBJECT = new Key( "{}" );
 
   @Nonnull
   private final String json;
 
   public Key( @Nonnull String json ) {
     this.json = json;
   }
 
   @Nonnull
   public String getJson() {
     return json;
   }
 
   @Override
   public boolean equals( Object o ) {
     if ( this == o ) {
       return true;
     }
     if ( !( o instanceof Key ) ) {
       return false;
     }
 
     Key key = ( Key ) o;
 
     if ( !json.equals( key.json ) ) {
       return false;
     }
 
     return true;
   }
 
   @Override
   public int hashCode() {
     return json.hashCode();
   }
 
   @Override
   public String toString() {
     return "Key{" +
       json + '\'' +
       '}';
   }
 
   @Nonnull
   public static Key array( @Nonnull String... parts ) {
     StringBuilder builder = new StringBuilder();
     builder.append( "[" );
 
    builder.append( Joiner.on( "," ).join( parts ) );
 
     builder.append( "]" );
     return new Key( builder.toString() );
   }
 
   /**
    * Returns a key that can be used as "end" array (for "endkey")
    *
    * @param parts the parts
    * @return an array that contains an additional empty object as last element
    */
   @Nonnull
   public static Key endArray( @Nonnull String... parts ) {
     if ( parts.length == 0 ) {
       throw new IllegalArgumentException( "Need at least one element" );
     }
 
     StringBuilder builder = new StringBuilder();
     builder.append( "[" );
 
    builder.append( Joiner.on( "," ).join( parts ) );
 
     builder.append( ",{}" );
     builder.append( "]" );
     return new Key( builder.toString() );
 
   }
 }
