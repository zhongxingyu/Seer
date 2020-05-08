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
 package com.cedarsoft.couchdb.test.utils;
 
 import com.cedarsoft.couchdb.DesignDocument;
 import com.cedarsoft.couchdb.DesignDocuments;
 import com.cedarsoft.test.utils.JsonUtils;
 import org.junit.*;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Nonnull;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 /**
  * @author Johannes Schneider (<a href="mailto:js@cedarsoft.com">js@cedarsoft.com</a>)
  */
 public class DesignDocumentsTest {
 
   private URL resource;
 
   @Before
   public void setUp( ) throws Exception {
     resource = getClass( ).getResource( "views/doc1/file1.map.js" );
   }
 
   @Test
   public void testBaseDir( ) throws Exception {
     File dir = DesignDocuments.guessBaseDir( resource );
     assertThat( dir ).isDirectory( );
     assertThat( dir.getName( ) ).isEqualTo( "doc1" );
 
     Collection<? extends File> files = DesignDocuments.listJsFiles( dir );
     assertThat( files ).hasSize( 3 );
   }
 
   @Test
   public void testGetIt( ) throws Exception {
     File dir = DesignDocuments.guessBaseDir( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( dir.getName( ) ).isEqualTo( "doc1" );
 
     DesignDocument designDocument = DesignDocuments.createDesignDocument( "doc1", DesignDocuments.listJsFiles( dir ) );
     assertThat( designDocument.getViews( ) ).hasSize( 2 );
 
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc.json" ), designDocument.createJson( ) );
   }
 
   @Test
   public void testApi( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     verifyDoc1( designDocument );
   }
 
   @Test
   public void testEmpty( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/empty/none.txt" ) );
     assertThat( designDocument.hasViews( ) ).isFalse( );
   }
 
   @Test
   public void testPathNames( ) throws Exception {
     DesignDocument designDocument = DesignDocuments.createDesignDocument( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( designDocument.getDesignDocumentPath( ) ).isEqualTo( "_design/doc1" );
 
     assertThat( new DesignDocument( "asdf" ).getDesignDocumentPath( ) ).isEqualTo( "_design/asdf" );
     assertThat( new DesignDocument( "1233" ).getDesignDocumentPath( ) ).isEqualTo( "_design/1233" );
   }
 
   @Test
   public void testAllViews( ) throws Exception {
     List<? extends DesignDocument> designDocuments = DesignDocuments.createDesignDocuments( getClass( ).getResource( "views/doc1/file1.map.js" ) );
     assertThat( designDocuments ).hasSize( 3 );
 
    for ( DesignDocument designDocument : designDocuments ) {
      if ( !designDocument.hasViews( ) ) {
        assertThat( designDocument.getId( ) ).isEqualTo( "empty" );
        continue;
      }
      verifyDoc( designDocument );
    }
   }
 
   private void verifyDoc( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     if ( designDocument.getId( ).equals( "doc1" ) ) {
       verifyDoc1( designDocument );
     } else if ( designDocument.getId( ).equals( "doc2" ) ) {
       verifyDoc2( designDocument );
     } else {
       throw new AssertionError( "invalid id <" + designDocument.getId( ) + ">" );
     }
   }
 
   private void verifyDoc1( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     assertThat( designDocument.getId( ) ).isEqualTo( "doc1" );
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc.json" ), designDocument.createJson( ) );
   }
 
   private void verifyDoc2( @Nonnull DesignDocument designDocument ) throws SAXException, IOException {
     assertThat( designDocument.getId( ) ).isEqualTo( "doc2" );
     JsonUtils.assertJsonEquals( getClass( ).getResource( "designDoc2.json" ), designDocument.createJson( ) );
   }
 
 }
