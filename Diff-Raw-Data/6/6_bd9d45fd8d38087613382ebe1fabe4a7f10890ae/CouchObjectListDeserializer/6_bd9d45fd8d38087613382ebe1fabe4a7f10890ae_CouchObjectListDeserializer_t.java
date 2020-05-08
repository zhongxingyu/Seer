 /*******************************************************************************
  * Copyright 2011 John Casey
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.commonjava.couch.io.json;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.commonjava.couch.db.model.CouchObjectList;
 import org.commonjava.couch.model.CouchDocument;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonDeserializationContext;
 import com.google.gson.JsonDeserializer;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParseException;
 import com.google.gson.reflect.TypeToken;
 
 public class CouchObjectListDeserializer<T extends CouchDocument>
     implements JsonDeserializer<CouchObjectList<T>>, SerializationAdapter
 {
 
     private static final String ROWS = "rows";
 
     private static final String DOC_ELEMENT = "doc";
 
     private final Class<T> type;
 
     private final boolean allowMissing;
 
     public CouchObjectListDeserializer( final Class<T> type )
     {
         this.type = type;
         this.allowMissing = false;
     }
 
     public CouchObjectListDeserializer( final Class<T> type, final boolean allowMissing )
     {
         this.type = type;
         this.allowMissing = allowMissing;
     }
 
     @Override
     public Type typeLiteral()
     {
         return new TypeToken<CouchObjectList<T>>()
         {
         }.getType();
     }
 
     @Override
     public CouchObjectList<T> deserialize( final JsonElement json, final Type typeOfT,
                                            final JsonDeserializationContext context )
         throws JsonParseException
     {
         final List<T> items = new ArrayList<T>();
 
         final JsonElement rowsRaw = json.getAsJsonObject()
                                         .get( ROWS );
         if ( rowsRaw == null )
         {
             throw new JsonParseException( "Cannot find " + ROWS + " field within root object." );
         }
 
         final JsonArray rows = rowsRaw.getAsJsonArray();
         for ( final JsonElement row : rows )
         {
             final JsonObject rowObj = row.getAsJsonObject();
             final JsonElement doc = rowObj.get( DOC_ELEMENT );
             if ( doc == null )
             {
                 if ( allowMissing )
                 {
                     continue;
                 }
 
                 throw new JsonParseException( "Cannot find " + DOC_ELEMENT + " field within row: " + row
                     + "\nDid you access the view with the '?include_docs=true' query parameter?" );
             }
 
            final Object val = context.deserialize( doc, type );
            if ( val != null )
            {
                items.add( type.cast( val ) );
            }
         }
 
         return new CouchObjectList<T>( items );
     }
 
 }
