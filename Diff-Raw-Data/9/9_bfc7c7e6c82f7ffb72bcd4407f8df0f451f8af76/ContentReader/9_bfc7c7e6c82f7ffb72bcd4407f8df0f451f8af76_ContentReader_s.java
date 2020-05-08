 /*
  * Copyright (c) 2008-2012 David Kellum
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package iudex.da;
 
 import iudex.core.ContentKeys;
 import iudex.core.VisitURL;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 
 import com.gravitext.htmap.UniMap;
 
 public class ContentReader
 {
     public ContentReader( DataSource source, ContentMapper mapper )
     {
         _mapper = mapper;
         _dsource = source;
     }
 
     public ContentMapper mapper()
     {
         return _mapper;
     }
 
     public DataSource dataSource()
     {
         return _dsource;
     }
 
     /**
     * Read map's URL and update map.
      */
     public void read( UniMap map ) throws SQLException
     {
         UniMap update = read( map.get( ContentKeys.URL ) );
        map.putAll( update );
     }
 
     public UniMap read( VisitURL url ) throws SQLException
     {
         final StringBuilder qb = new StringBuilder( 256 );
         qb.append( "SELECT " );
         mapper().appendFieldNames( qb );
         qb.append( " FROM urls WHERE uhash = ?;" );
 
         List<UniMap> list = select( qb.toString(), url.uhash() );
 
         if( list.size() == 1 ) return list.get( 0 );
         if( list.size() > 1 ) {
             throw new IllegalStateException(
                "Multiple url rows returned for uhash=" + url.uhash() );
         }
         return null;
     }
 
     public List<UniMap> select( String query, Object... params )
         throws SQLException
     {
         QueryRunner runner = new QueryRunner( _dsource );
 
         return runner.query( query, new MapHandler(), params );
     }
 
     protected final class MapHandler
         implements ResultSetHandler<List<UniMap>>
     {
         @Override
         public List<UniMap> handle( ResultSet rset ) throws SQLException
         {
             ArrayList<UniMap> contents = new ArrayList<UniMap>( 128 );
             while( rset.next() ) {
                 contents.add( _mapper.fromResultSet( rset ) );
             }
             return contents;
         }

     }
 
     private final DataSource _dsource;
     private final ContentMapper _mapper;
 }
