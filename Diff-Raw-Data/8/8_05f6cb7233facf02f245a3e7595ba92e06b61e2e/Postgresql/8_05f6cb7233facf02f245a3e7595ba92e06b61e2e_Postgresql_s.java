 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 David Warnock
  * 
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     David Warnock (david@sundayta.co.uk)
  *     Sundayta Ltd
  *     International House, 174 Three Bridges Road, Crawley, West Sussex RH10 1LE, UK
  *
  */
 
 package org.melati.poem.dbms;
 
 import java.sql.*;
 import org.melati.poem.*;
 
 public class Postgresql extends AnsiStandard {
 
     public Postgresql() {
         setDriverClassName("org.melati.poem.postgresql.jdbc2.Driver");
     }
 
     public String getStringSqlDefinition(int size) throws SQLException {
         if (size < 0) { 
             return "TEXT";
         }
         return super.getStringSqlDefinition(size);
     }
 
     public String getBinarySqlDefinition(int size) throws SQLException {
         // BLOBs in Postgres are represented as OIDs pointing to the data
         return "OID";
     }
 
     public static class OidPoemType extends IntegerPoemType {
         public OidPoemType(boolean nullable) {
             super(Types.INTEGER, "OID", nullable);
         }
 
         protected boolean _canRepresent(SQLPoemType other) {
             return other instanceof BinaryPoemType;
         }
 
         public PoemType canRepresent(PoemType other) {
             return other instanceof BinaryPoemType &&
                    !(!getNullable() && ((BinaryPoemType)other).getNullable()) ?
                        other : null;
         }
     }
 
     public SQLPoemType defaultPoemTypeOfColumnMetaData(ResultSet md)
         throws SQLException {
       return
           md.getString("TYPE_NAME").equals("oid") ?
               new OidPoemType(md.getInt("NULLABLE") ==
                                   DatabaseMetaData.columnNullable) :
               super.defaultPoemTypeOfColumnMetaData(md);
     }
 
    public SQLPoemException exceptionForUpdate(Table table, String sql,
                                               boolean insert, SQLException e) {
 
       String m = e.getMessage();
 
       // Postgres's duplicate key message is:
       // "Cannot insert a duplicate key into unique index user_login_index"
 
      int s, u;
       if (m != null &&
           m.indexOf("duplicate key") >= 0) {
 
         // We call POEM's own indexes <table>_<column>_index:
         // see Table.dbCreateIndex
 
         if (m.endsWith("_index\n") &&
             (s = m.lastIndexOf(' ')) >= 0 && (u = m.indexOf('_', s+1)) >= 0) {
           String colname = m.substring(u+1, m.length() - 7);
           try {
             return new DuplicateKeySQLPoemException(table.getColumn(colname),
                                                     sql, insert, e);
           }
           catch (NoSuchColumnPoemException f) {
           }
         }
 
         return new DuplicateKeySQLPoemException(table, sql, insert, e);
       }
       else
         return super.exceptionForUpdate(table, sql, insert, e);
     }
 }
