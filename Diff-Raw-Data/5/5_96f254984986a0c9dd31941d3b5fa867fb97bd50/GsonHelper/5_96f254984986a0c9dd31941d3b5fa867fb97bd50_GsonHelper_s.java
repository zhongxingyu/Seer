 package nl.pvanassen.highchart.api.base;
 
 import nl.pvanassen.highchart.api.format.DateTimeLabelFormats;
 import nl.pvanassen.highchart.api.serializer.DateTimeLabelFormatsSerializer;
 import nl.pvanassen.highchart.api.serializer.StyleSerializer;
 
 import com.google.gson.ExclusionStrategy;
 import com.google.gson.FieldAttributes;
 import com.google.gson.GsonBuilder;
 
 public final class GsonHelper {
 
    private static final String     yyyy_MM_dd  = "yyyyMMdd";
 
     private static final String     USER_OBJECT = "userObject";
 
     private static final GsonHelper INSTANCE    = new GsonHelper();
 
     static String toJson( Object object ) {
         return INSTANCE.gsonBuilder.create().toJson( object );
     }
 
     private final GsonBuilder gsonBuilder;
 
     private GsonHelper() {
         gsonBuilder = new GsonBuilder().registerTypeAdapter( DateTimeLabelFormats.class, new DateTimeLabelFormatsSerializer() ) //
         .registerTypeAdapter( Style.class, new StyleSerializer() )//
        .setDateFormat( yyyy_MM_dd )//
         .setExclusionStrategies( new ExclusionStrategy() {
 
             @Override
             public boolean shouldSkipClass( Class<?> arg0 ) {
                 return false;
             }
 
             @Override
             public boolean shouldSkipField( FieldAttributes attributes ) {
                 return attributes.getName().equals( USER_OBJECT );
             }
 
         } );
     }
 }
