 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.kernel.impl.nioneo.store;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.neo4j.kernel.CommonFactories;
 import org.neo4j.kernel.IdGeneratorFactory;
 
 public class GraphDatabaseStore
 {
     private static final Field STRING_PROPERTY_STORE, ARRAY_PROPERTY_STORE;
     static
     {
         // FIXME: this should not use reflection,
         // refactor GraphDatabaseStore to allow access to these fields
         STRING_PROPERTY_STORE = field( PropertyStore.class, "stringPropertyStore" );
         ARRAY_PROPERTY_STORE = field( PropertyStore.class, "arrayPropertyStore" );
     }
     private final NodeStore nodeStore;
     private final RelationshipStore relStore;
     private final PropertyStore propStore;
     private final DynamicStringStore stringStore;
     private final DynamicArrayStore arrayStore;
     private final String path;
 
     public GraphDatabaseStore( String path )
     {
         this( path, defaultParams() );
     }
 
     public static Object get( Class<?> type, String name, Object obj )
     {
         return get( obj, field( type, name ) );
     }
 
     private static Field field( Class<?> type, String name )
     {
         Field field;
         try
         {
             field = type.getDeclaredField( name );
         }
         catch ( Exception cause )
         {
             throw new RuntimeException( cause );
         }
         field.setAccessible( true );
         return field;
     }
 
     private static Object get( Object obj, Field field )
     {
         try
         {
             return field.get( obj );
         }
         catch ( Exception cause )
         {
             throw new RuntimeException( cause );
         }
     }
 
     public GraphDatabaseStore( String path, Map<Object, Object> params )
     {
         this.path = path;
         this.nodeStore = new NodeStore( path + "/neostore.nodestore.db", params );
         this.relStore = new RelationshipStore( path + "/neostore.relationshipstore.db", params );
         this.propStore = new PropertyStore( path + "/neostore.propertystore.db", params );
         this.stringStore = (DynamicStringStore) get( propStore, STRING_PROPERTY_STORE );
         this.arrayStore = (DynamicArrayStore) get( propStore, ARRAY_PROPERTY_STORE );
     }
 
     @Override
     public String toString()
     {
         return getClass().getSimpleName() + "[" + path + "]";
     }
 
     public LogicalLogAccess logicalLog( String name )
     {
         return new LogicalLogAccess( path, name );
     }
 
     public void makeStoreOk()
     {
         nodeStore.makeStoreOk();
         relStore.makeStoreOk();
         propStore.makeStoreOk();
         stringStore.makeStoreOk();
         arrayStore.makeStoreOk();
     }
 
     public void shutdown()
     {
         nodeStore.close();
         relStore.close();
         propStore.close();
     }
 
     private static Map<Object, Object> defaultParams()
     {
         Map<Object, Object> params = new HashMap<Object, Object>();
         params.put( "neostore.nodestore.db.mapped_memory", "20M" );
         params.put( "neostore.propertystore.db.mapped_memory", "90M" );
         params.put( "neostore.propertystore.db.index.mapped_memory", "1M" );
         params.put( "neostore.propertystore.db.index.keys.mapped_memory", "1M" );
         params.put( "neostore.propertystore.db.strings.mapped_memory", "130M" );
         params.put( "neostore.propertystore.db.arrays.mapped_memory", "130M" );
         params.put( "neostore.relationshipstore.db.mapped_memory", "100M" );
         // if on windows, default no memory mapping
         String nameOs = System.getProperty( "os.name" );
         if ( nameOs.startsWith( "Windows" ) )
         {
             params.put( "use_memory_mapped_buffers", "false" );
         }
         //
         params.put( IdGeneratorFactory.class, new CommonFactories.DefaultIdGeneratorFactory() );
         return params;
     }
 
     public NodeStoreAccess getNodeStore()
     {
         return new NodeStoreAccess( nodeStore );
     }
 
     public RelationshipStoreAccess getRelStore()
     {
         return new RelationshipStoreAccess( relStore );
     }
 
     public PropertyStoreAccess getPropStore()
     {
         return new PropertyStoreAccess( propStore );
     }
 
     public StringPropertyStoreAccess getStringPropertyStore()
     {
         return new StringPropertyStoreAccess( stringStore );
     }
 
     public ArrayPropertyStoreAccess getArrayPropertyStore()
     {
         return new ArrayPropertyStoreAccess( arrayStore );
     }
 
     static PropertyType getEnumTypeSafe( int type )
     {
        switch ( type )
         {
        case 1:
            return PropertyType.INT;
        case 2:
            return PropertyType.STRING;
        case 3:
            return PropertyType.BOOL;
        case 4:
            return PropertyType.DOUBLE;
        case 5:
            return PropertyType.FLOAT;
        case 6:
            return PropertyType.LONG;
        case 7:
            return PropertyType.BYTE;
        case 8:
            return PropertyType.CHAR;
        case 9:
            return PropertyType.ARRAY;
        case 10:
            return PropertyType.SHORT;
        default:
             return null;
         }
     }
 }
