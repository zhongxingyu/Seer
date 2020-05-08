 //$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
 /*----------------------------------------------------------------------------
  This file is part of deegree, http://deegree.org/
  Copyright (C) 2001-2012 by:
  - Department of Geography, University of Bonn -
  and
  - lat/lon GmbH -
 
  This library is free software; you can redistribute it and/or modify it under
  the terms of the GNU Lesser General Public License as published by the Free
  Software Foundation; either version 2.1 of the License, or (at your option)
  any later version.
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  details.
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation, Inc.,
  59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
  Contact information:
 
  lat/lon GmbH
  Aennchenstr. 19, 53177 Bonn
  Germany
  http://lat-lon.de/
 
  Department of Geography, University of Bonn
  Prof. Dr. Klaus Greve
  Postfach 1147, 53001 Bonn
  Germany
  http://www.geographie.uni-bonn.de/deegree/
 
  e-mail: info@deegree.org
  ----------------------------------------------------------------------------*/
 package org.deegree.igeo.dataadapter.database.sqlserver;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.deegree.datatypes.Types;
 import org.deegree.framework.log.ILogger;
 import org.deegree.framework.log.LoggerFactory;
 import org.deegree.igeo.dataadapter.database.AbstractDatabaseWriter;
 import org.deegree.igeo.mapmodel.DatabaseDatasource;
 import org.deegree.model.feature.Feature;
 import org.deegree.model.feature.schema.PropertyType;
 import org.deegree.model.spatialschema.Geometry;
 import org.deegree.model.spatialschema.JTSAdapter;
 
 import com.vividsolutions.jts.io.WKBWriter;
 
 /**
  * TODO add class documentation here
  * 
  * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz</a>
  * @author last edited by: $Author: lyn $
  * 
  * @version $Revision: $, $Date: $
  */
 public class SqlServerDataWriter extends AbstractDatabaseWriter {
 
     private static final ILogger LOG = LoggerFactory.getLogger( SqlServerDataWriter.class );
 
     @Override
     protected int setFieldValues( PreparedStatement stmt, DatabaseDatasource datasource, Feature feature,
                                  PropertyType[] pt, String tableName, Connection connection )
                             throws Exception {
         int index = 1;
         for ( int i = 0; i < pt.length; i++ ) {
             Object value = feature.getDefaultProperty( pt[i].getName() ).getValue();
             if ( pt[i].getName().getLocalName().equalsIgnoreCase( datasource.getPrimaryKeyFieldName() ) ) {
                 feature.getDefaultProperty( pt[i].getName() ).setValue( value );
             }
             if ( value != null ) {
                 if ( pt[i].getType() == Types.GEOMETRY ) {
                     Geometry geom = (Geometry) value;
                     WKBWriter writer = new WKBWriter();
                     byte[] write = writer.write( JTSAdapter.export( geom ) );
                     stmt.setObject( index++, write );
                 } else {
                     if ( !ignoreValue( pt[i].getName().getLocalName(), tableName, connection ) ) {
                         stmt.setObject( index++, value, pt[i].getType() );
                     }
                 }
             } else {
                 if ( pt[i].getType() == Types.GEOMETRY ) {
                     stmt.setNull( index++, Types.OTHER );
                 } else {
                     stmt.setNull( index++, pt[i].getType() );
                 }
             }
         }
         return index;
     }
 
     @Override
     protected void setWhereCondition( PreparedStatement stmt, DatabaseDatasource datasource, PropertyType[] pt,
                                       Feature feature, int index )
                             throws SQLException {
         for ( int i = 0; i < pt.length; i++ ) {
             if ( pt[i].getName().getLocalName().equalsIgnoreCase( datasource.getPrimaryKeyFieldName() ) ) {
                 Object value = feature.getDefaultProperty( pt[i].getName() ).getValue();
                 stmt.setObject( index, value, pt[i].getType() );
                 break;
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.deegree.igeo.dataadapter.database.AbstractDatabaseWriter#getSqlSnippet(java.lang.String,
      * java.lang.String, java.sql.Connection, org.deegree.igeo.mapmodel.DatabaseDatasource)
      */
     @Override
     protected String getSqlSnippet( String columnName, String tableName, Connection connection,
                                     DatabaseDatasource datasource ) {
         if ( columnName.equalsIgnoreCase( datasource.getGeometryFieldName() ) ) {
            return "geometry::STGeomFromWKB(?, " + datasource.getNativeCoordinateSystem().getLocalName() + ")";
         }
         if ( ignoreValue( columnName, tableName, connection ) ) {
             return null;
         }
         return super.getSqlSnippet( columnName, tableName, connection, datasource );
     }
 
     private boolean ignoreValue( String columnName, String tableName, Connection connection ) {
         ResultSet columns;
         try {
             columns = connection.getMetaData().getColumns( null, null, tableName, columnName );
             while ( columns.next() ) {
                 if ( columnName.equalsIgnoreCase( columns.getString( "COLUMN_NAME" ) ) ) {
                     String isAutoincrement = columns.getString( "IS_AUTOINCREMENT" );
                     return "YES".equalsIgnoreCase( isAutoincrement );
                 }
             }
         } catch ( SQLException e ) {
             LOG.logDebug( "Could not determine id column is autoincrement: " + e.getMessage() );
         }
         return false;
     }
 
 }
