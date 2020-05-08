 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.parsers.sql.dsql;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.Index;
 import com.google.appengine.api.datastore.Index.IndexState;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeMap;
 import org.vesalainen.parsers.sql.ColumnMetadata;
 import org.vesalainen.parsers.sql.TableMetadata;
 
 /**
  * @author Timo Vesalainen
  */
 public class Statistics
 {
     private static final long DAY = 24*60*60*1000;
     
     private long nextUpdate;
     private DatastoreService datastore;
     private final Map<String, TableMetadata> map = new TreeMap<>();
     private Map<Index, IndexState> indexes;
 
     public Statistics(DatastoreService datastore)
     {
         this.datastore = datastore;
         update();
     }
 
     public void update()
     {
         synchronized(map)
         {
             if (System.currentTimeMillis() > nextUpdate)
             {
                 indexes = datastore.getIndexes();
                 map.clear();
                Query q0 = new Query("__Stat_Ns_Total__");
                 Entity total = datastore.prepare(q0).asSingleEntity();
                 if (total != null)
                 {
                     Date timestamp = (Date) total.getProperty("timestamp");
                     nextUpdate = timestamp.getTime() + DAY;
                    Query q1 = new Query("__Stat_Ns_PropertyName_Kind__");
                     q1.addFilter("timestamp", Query.FilterOperator.EQUAL, timestamp);
                     PreparedQuery p1 = datastore.prepare(q1);
                     for (Entity prop : p1.asIterable())
                     {
                         String kind = (String) prop.getProperty("kind_name");
                         if (!kind.startsWith("_"))
                         {
                             String property = (String) prop.getProperty("property_name");
                             long count = (long) prop.getProperty("count");
                             long indexCount = (long) prop.getProperty("builtin_index_count");
                             KindEntry kindEntry = (KindEntry) map.get(kind.toUpperCase());
                             if  (kindEntry == null)
                             {
                                 kindEntry = new KindEntry(kind);
                                 map.put(kind.toUpperCase(), kindEntry);
                             }
                             kindEntry.addProperty(property, count, indexCount > 0);
                         }
                     }
                 }
             }
         }
     }
 
     public Map<Index, IndexState> getIndexes()
     {
         return indexes;
     }
     
     public TableMetadata getKind(String name)
     {
         if (name != null)
         {
             return map.get(name.toUpperCase());
         }
         return null;
     }
     
     public ColumnMetadata getProperty(String kind, String property)
     {
         KindEntry kindEntry = (KindEntry) getKind(kind);
         if (kindEntry != null)
         {
             return kindEntry.getColumnMetadata(property);
         }
         return null;
     }
 
     public Iterable<TableMetadata> getTables()
     {
         return map.values();
     }
 
     public class KindEntry implements TableMetadata, Iterable<ColumnMetadata>
     {
         private String name;
         private long entityCount;
         private Map<String,ColumnMetadata> properties = new HashMap<>();
         private PropertyEntry key;
 
         public KindEntry(String name)
         {
             this.name = name;
         }
 
         public void addProperty(String name, long count, boolean indexed)
         {
             if (!properties.containsKey(name))
             {
                 properties.put(name.toUpperCase(), new PropertyEntry(name, count, indexed));
             }
             if (entityCount < count)
             {
                 entityCount = count;
             }
         }
         @Override
         public ColumnMetadata getColumnMetadata(String name)
         {
             if (Entity.KEY_RESERVED_PROPERTY.equals(name))
             {
                 if (key == null)
                 {
                     key = new PropertyEntry(name, entityCount, true, true);
                 }
                 return key;
             }
             return properties.get(name.toUpperCase());
         }
 
         public long getEntityCount()
         {
             return entityCount;
         }
 
         @Override
         public String getName()
         {
             return name;
         }
 
         @Override
         public long getCount()
         {
             return entityCount;
         }
 
         @Override
         public Iterable<ColumnMetadata> getColumns()
         {
             return this;
         }
 
         @Override
         public Iterator<ColumnMetadata> iterator()
         {
             return properties.values().iterator();
         }
 
         @Override
         public String toString()
         {
             return name;
         }
         
     }
     public class PropertyEntry implements ColumnMetadata
     {
         private String name;
         private long count;
         private boolean indexed;
         private boolean unique;
 
         public PropertyEntry(String name, long count, boolean indexed)
         {
             this.name = name;
             this.count = count;
             this.indexed = indexed;
         }
 
         public PropertyEntry(String name, long count, boolean indexed, boolean unique)
         {
             this.name = name;
             this.count = count;
             this.indexed = indexed;
             this.unique = unique;
         }
 
         @Override
         public long getCount()
         {
             return count;
         }
 
         @Override
         public boolean isUnique()
         {
             return unique;
         }
 
         @Override
         public boolean isIndexed()
         {
             return indexed;
         }
      
         public long getEstimatedCount()
         {
             if (indexed)
             {
                 return count / 10;
             }
             else
             {
                 return count;
             }
         }
 
         @Override
         public String getName()
         {
             return name;
         }
 
         @Override
         public float getSelectivity()
         {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public String toString()
         {
             return name;
         }
         
     }
 }
