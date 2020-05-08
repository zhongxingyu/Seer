 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.hbase.event;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.hadoop.hbase.client.Put;
 import org.codehaus.jackson.map.ObjectMapper;
 
 import com.google.common.base.Charsets;
 import com.google.inject.Inject;
 import com.nesscomputing.event.NessEvent;
 import com.nesscomputing.logging.Log;
 
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

 /**
  * The "default" implementation of the event strategy. This encompasses the original functionality of the event writer.
  * @see HBaseEventStrategy
  */
 public class DefaultHBaseEventStrategy implements HBaseEventStrategy
 {
     private static final Log LOG = Log.findLog();
 
     // "Immutable".  Change this at runtime and you win a Darwin award.
     private static final byte[] EVENT_COLUMN_FAMILY = "ev".getBytes(Charsets.UTF_8);
 
     private ObjectMapper objectMapper;
 
 
     @Inject
     public DefaultHBaseEventStrategy(ObjectMapper objectMapper)
     {
         this.objectMapper = objectMapper;
     }
 
     @Override
     public String getTableName()
     {
         return "events";
     }
 
     @Override
     public boolean acceptEvent(NessEvent event)
     {
         return event != null;
     }
 
     @Override
     public Put encodeEvent(NessEvent event)
     {
         LOG.trace("Encoding event: %s", event);
         final Put put = new Put(HBaseEncoder.rowKeyForEvent(event));
         // Try to keep the v1 format as much alive as possible
         addKey(put, "entryTimestamp", HBaseEncoder.bytesForObject(event.getTimestamp()));
         addKey(put, "eventType", HBaseEncoder.bytesForString(event.getType().getName()));
 
         final UUID userId = event.getUser();
 
         if (userId != null) {
             addKey(put, "user", HBaseEncoder.bytesForObject(userId));
         }
 
         addKey(put, "uuid", HBaseEncoder.bytesForObject(event.getId()));
         addKey(put, "v", HBaseEncoder.bytesForObject(event.getVersion()));
 
         for (Map.Entry<String, ? extends Object> e : event.getPayload().entrySet()) {
             try {
                 final String value = stringify(e.getValue());
                 LOG.trace("%s --> %s", e.getKey(), value);
                 addKey(put, e.getKey(), HBaseEncoder.bytesForString(value));
             }
             catch (IOException ioe) {
                 LOG.warn(ioe, "Could not serialize '%s'", e.getValue());
             }
         }
 
         return put;
     }
 
     @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
     public byte[] getEventColumnFamily()
     {
         return EVENT_COLUMN_FAMILY;
 
     }
 
     private void addKey(final Put put, final String key,  final byte[] value)
     {
         put.add(this.getEventColumnFamily(), key.getBytes(Charsets.UTF_8), value);
     }
 
     private String stringify(final Object value)
         throws IOException
     {
         if (value == null) {
             return null;
         }
         else if (value instanceof Map || value instanceof List || value.getClass().isArray()) {
             return objectMapper.writeValueAsString(value);
         }
         else {
             return value.toString();
         }
     }
 
 }
