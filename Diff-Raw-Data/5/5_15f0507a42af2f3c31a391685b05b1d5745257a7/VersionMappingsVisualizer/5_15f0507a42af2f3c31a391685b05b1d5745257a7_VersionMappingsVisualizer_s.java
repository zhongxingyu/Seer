 /**
  * Copyright (C) cedarsoft GmbH.
  *
  * Licensed under the GNU General Public License version 3 (the "License")
  * with Classpath Exception; you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *         http://www.cedarsoft.org/gpl3ce
  *         (GPL 3 with Classpath Exception)
  *
  * This code is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 3 only, as
  * published by the Free Software Foundation. cedarsoft GmbH designates this
  * particular file as subject to the "Classpath" exception as provided
  * by cedarsoft GmbH in the LICENSE file that accompanied this code.
  *
  * This code is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  * version 3 for more details (a copy is included in the LICENSE file that
  * accompanied this code).
  *
  * You should have received a copy of the GNU General Public License version
  * 3 along with this work; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * Please contact cedarsoft GmbH, 72810 Gomaringen, Germany,
  * or visit www.cedarsoft.com if you need additional information or
  * have any questions.
  */
 
 package com.cedarsoft.serialization.ui;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.serialization.ToString;
 import com.cedarsoft.serialization.VersionMapping;
 import com.cedarsoft.serialization.VersionMappings;
import com.sun.xml.internal.ws.util.StringUtils;

import javax.annotation.Nullable;
 
 import javax.annotation.Nonnull;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.SortedSet;
 
 /**
  * @param <T> the type
  */
 public class VersionMappingsVisualizer<T> {
   @Nonnull
 
   private static final String COL_SEPARATOR = "  ";
   @Nonnull
 
   private static final String FIRST_COLUMN_SEPARATOR = " -->";
   private static final int COL_WIDTH = 8;
   @Nonnull
 
   private static final String COL_VERSION_REPEAT = "|  ";
   @Nonnull
   private final VersionMappings<T> mappings;
   @Nonnull
   private final Comparator<T> comparator;
 
   @Nullable
   private final ToString<T> toString;
 
 
   public VersionMappingsVisualizer( @Nonnull VersionMappings<T> mappings ) {
     this( mappings, new ToStringComparator<T>() );
   }
 
   public VersionMappingsVisualizer( @Nonnull VersionMappings<T> mappings, @Nonnull Comparator<T> comparator ) {
     this( mappings, comparator, new DefaultToString<T>() );
   }
 
   public VersionMappingsVisualizer( @Nonnull VersionMappings<T> mappings, @Nonnull ToString<T> toString ) {
     this( mappings, new ToStringComparator<T>(), toString );
   }
 
   public VersionMappingsVisualizer( @Nonnull VersionMappings<T> mappings, @Nonnull Comparator<T> comparator, @Nonnull ToString<T> toString ) {
     this.mappings = mappings;
     this.comparator = comparator;
     this.toString = toString;
   }
 
   @Nonnull
 
   public String visualize() throws IOException {
     StringWriter writer = new StringWriter();
     visualize( writer );
     return writer.toString();
   }
 
   public void visualize( @Nonnull Writer out ) throws IOException {
     Collection<Column> columns = new ArrayList<Column>();
 
     //The versions
     SortedSet<Version> keyVersions = mappings.getMappedVersions();
 
     //The keys
     List<T> keys = new ArrayList<T>( mappings.getMappings().keySet() );
     Collections.sort( keys, comparator );
 
     for ( T key : keys ) {
       VersionMapping mapping = mappings.getMapping( key );
 
       List<Version> versions = new ArrayList<Version>();
       for ( Version keyVersion : keyVersions ) {
         versions.add( mapping.resolveVersion( keyVersion ) );
       }
 
       columns.add( new Column( toString.convert( key ), versions ) );
     }
 
     writeHeadline( columns, out );
     writeSeparator( columns.size(), out );
     writeContent( new ArrayList<Version>( keyVersions ), columns, out );
     writeSeparator( columns.size(), out );
   }
 
   private static void writeContent( @Nonnull List<? extends Version> keyVersions, @Nonnull Iterable<? extends Column> columns, @Nonnull Writer out ) throws IOException {
     for ( int i = 0, keyVersionsSize = keyVersions.size(); i < keyVersionsSize; i++ ) {
       Version keyVersion = keyVersions.get( i );
       out.write( extend( keyVersion.format() ) );
       out.write( FIRST_COLUMN_SEPARATOR );
 
 
       //Now write the columns
       for ( Column column : columns ) {
         out.write( COL_SEPARATOR );
         out.write( extend( column.lines.get( i ) ) );
       }
       out.write( "\n" );
     }
   }
 
   private static void writeSeparator( int columnsSize, @Nonnull Writer out ) throws IOException {
     int count = COL_WIDTH;
     count += FIRST_COLUMN_SEPARATOR.length();
     count += COL_SEPARATOR.length() * columnsSize;
     count += COL_WIDTH * columnsSize;
 
     StringBuilder builder = new StringBuilder();
     for (int i = 0; i < count; i++) {
       builder.append("-");
     }
 
     out.write(builder.append("\n").toString());
   }
 
   protected void writeHeadline( @Nonnull Iterable<? extends Column> columns, @Nonnull Writer out ) throws IOException {
     out.write( extend( "" ) );//first column
     out.write( FIRST_COLUMN_SEPARATOR );
 
     for ( Column column : columns ) {
       out.write( COL_SEPARATOR ); //delimiter
       out.write( extend( column.header ) );
     }
 
     out.write( "\n" );
   }
 
   @Nonnull
   public static <T> VersionMappingsVisualizer<T> create( @Nonnull VersionMappings<T> mappings, @Nonnull Comparator<T> comparator, @Nonnull ToString<T> toString ) {
     return new VersionMappingsVisualizer<T>( mappings, comparator, toString );
   }
 
   @Nonnull
 
   public static <T> String toString( @Nonnull VersionMappings<T> mappings ) throws IOException {
     return new VersionMappingsVisualizer<T>( mappings ).visualize();
   }
 
   @Nonnull
 
   public static <T> String toString( @Nonnull VersionMappings<T> mappings, @Nonnull ToString<T> toString ) throws IOException {
     return new VersionMappingsVisualizer<T>( mappings, toString ).visualize();
   }
 
   @Nonnull
   private static String extend( @Nonnull String string ) {
     if ( string.length() > COL_WIDTH ) {
       return string.substring( 0, COL_WIDTH );
     }
 
     StringBuilder builder = new StringBuilder();
     for (int i = 0; i < COL_WIDTH - string.length(); i++) {
       builder.append(" ");
     }
 
     return builder.append(string).toString();
   }
 
   public static class Column {
     @Nonnull
 
     private final String header;
     @Nonnull
     private final List<String> lines = new ArrayList<String>();
 
     public Column( @Nonnull String header, @Nonnull Iterable<? extends Version> versions ) {
       this.header = header;
 
       Version lastVersion = null;
       for ( Version version : versions ) {
         if ( version.equals( lastVersion ) ) {
           lines.add( COL_VERSION_REPEAT );
         } else {
           lines.add( version.format() );
         }
         lastVersion = version;
       }
     }
   }
 
   public static class ToStringComparator<T> implements Comparator<T> {
     @Override
     public int compare( T o1, T o2 ) {
       return o1.toString().compareTo( o2.toString() );
     }
   }
 
   public static class DefaultToString<T> implements ToString<T> {
     @Nonnull
     @Override
     public String convert( @Nonnull T object ) {
       return String.valueOf( object );
     }
   }
 }
