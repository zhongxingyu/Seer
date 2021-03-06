 /**
  * Copyright (c) 2002-2013 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
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
 package org.neo4j.cypher.pycompat;
 
import java.util.Collection;
 import java.util.Map;
 
 import org.neo4j.graphdb.Path;
import scala.collection.JavaConversions;
 
 /**
  * Used to wrap gnarly scala classes into something that JPype understands.
  */
 public class ScalaToPythonWrapper
 {
 
     public static Object wrap( Object obj )
     {
         if(obj instanceof Path )
         {
             return new WrappedPath((Path)obj);
         }
         else if(obj instanceof Map)
         {
             return new WrappedMap( (Map<String, Object>) obj );
         }
        else if(obj instanceof JavaConversions.SeqWrapper)
        {
            return new WrappedCollection<Object>( (Collection)obj );
        }

         return obj;
     }
 
     public static Map<String, Object> wrapMap( Map<String, Object> map )
     {
         return new WrappedMap(map);
     }
 }
