 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package novelang.common;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import novelang.ScratchDirectoryFixture;
 
 /**
  * Tests for {@link FileTools}.
  *
  * @author Laurent Caillette
  */
 public class FileToolsTest {
   private static final int TIMEOUT_MILLISECONDS = 1000;
 
   @Test( timeout = TIMEOUT_MILLISECONDS )
   public void testRelativizeOkWithoutTrailingSeparatorOnParent() {
     final String relativized = FileTools.relativizePath( parentNoTrailingSeparator, childFile ) ;
     Assert.assertEquals( "childFile", relativized ) ;
   }
 
   @Test( timeout = TIMEOUT_MILLISECONDS )
   public void testRelativizeOkWithDirectoryAsChild() {
     final String relativized = FileTools.relativizePath( parentNoTrailingSeparator, childFile ) ;
     Assert.assertEquals( "childFile", relativized ) ;
   }
 
   @Test( timeout = TIMEOUT_MILLISECONDS )
   public void testRelativizeOkWithTrailingSeparatorOnParent() {
     final String relativized = FileTools.relativizePath( parentWithTrailingSeparator, childFile ) ;
     Assert.assertEquals( "childFile", relativized ) ;
   }
 
   @Test( timeout = TIMEOUT_MILLISECONDS )
   public void testRelativizeOkWithSubdirectory() {
     final String relativized = FileTools.relativizePath(
         parentWithTrailingSeparator, childDirectory ) ;
     Assert.assertEquals( "childDirectory/", relativized ) ;
   }
 
   @Test( expected = IllegalArgumentException.class )
   public void testRelativizeFails() {
     FileTools.relativizePath( childDirectory, childFile ) ;
   }
 
   /**
    * This doesn't test {@link FileTools} really but it's about verifying JDK behavior.
    */
   @Test
   public void fileInstantiationWithLeadingDot() throws IOException {
     final File childFileDotted = new File( parentNoTrailingSeparator, "./childFile" ) ;
     Assert.assertEquals( 
         childFile.getAbsolutePath(), 
         childFileDotted.getCanonicalFile().getAbsolutePath() 
     ) ;
     
   }
 
   @Test
   public void testListDirectories() {
     final List< File > directories = FileTools.scanDirectories( parentNoTrailingSeparator ) ;
     Assert.assertEquals( 3, directories.size() ) ;
     Assert.assertEquals( "parent", directories.get( 0 ).getName() ) ;
     Assert.assertEquals( "childDirectory", directories.get( 1 ).getName() ) ;
     Assert.assertEquals( "grandChildDirectory", directories.get( 2 ).getName() ) ;
   }

  @Test
  public void urlifyPath() {
    Assert.assertEquals( "foo/bar/baz/", FileTools.urlifyPath( "foo/bar\\baz\\" ) ) ;
  }
   
 // =======
 // Fixture
 // =======
 
   private File parentNoTrailingSeparator;
   private File parentWithTrailingSeparator;
   private File childDirectory;
   private File grandChildDirectory;
   private File childFile;
 
   @Before
   public void setUp() throws IOException {
     
     final ScratchDirectoryFixture fixture = new ScratchDirectoryFixture( getClass().getName() ) ;
     final File scratchDirectory = fixture.getTestScratchDirectory() ;
 
     parentNoTrailingSeparator = createDirectory( scratchDirectory, "parent" ) ;
     parentWithTrailingSeparator = createDirectory( scratchDirectory, "parent/" ) ;
     childDirectory = createDirectory( parentNoTrailingSeparator, "childDirectory" ) ;
     grandChildDirectory = createDirectory( parentNoTrailingSeparator, "grandChildDirectory" ) ;
     childFile = new File( parentNoTrailingSeparator, "childFile" ) ;
   }
 
   private static File createDirectory( File parent, String name ) {
     final File directory = new File( parent, name ) ;
     directory.mkdirs() ;
     FileUtils.waitFor( directory, TIMEOUT_MILLISECONDS ) ;
     return directory ;
   }
 
 }
