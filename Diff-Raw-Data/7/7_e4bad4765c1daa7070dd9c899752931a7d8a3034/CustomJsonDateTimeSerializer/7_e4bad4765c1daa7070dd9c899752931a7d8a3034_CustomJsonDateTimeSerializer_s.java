 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.nttdata.masterthesis.javabackend.helper;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonSerializer;
 import com.fasterxml.jackson.databind.SerializerProvider;
 
 /**
  * JsonDateTIME serializer serializes a Java-Date object as german datetime format.
  * @author MATHAF
  */
 public class CustomJsonDateTimeSerializer extends JsonSerializer<Date>
 {
     /**
      * datetimeformat of json response, for example. 01.01.2012 12:12:59 for January, 01 2012 12:12:59 pm
      */
     public static final String DATETIME_FORMAT = "dd.MM.yyyy HH:mm:ss";
 
     @Override
     public void serialize( Date aDate, JsonGenerator aJsonGenerator,
                            SerializerProvider aSerializerProvider )
     throws IOException
     {
 
         SimpleDateFormat dateFormat = new SimpleDateFormat( DATETIME_FORMAT );
         String dateString = dateFormat.format( aDate );
         aJsonGenerator.writeString( dateString );
     }
 }
