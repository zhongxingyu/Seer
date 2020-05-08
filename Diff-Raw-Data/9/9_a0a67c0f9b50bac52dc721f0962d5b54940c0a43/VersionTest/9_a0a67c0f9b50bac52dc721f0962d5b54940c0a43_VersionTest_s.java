 package org.novelang;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  * Tests for the {@link Version} class.
  * 
  * @author Laurent Caillette
  */
 public class VersionTest {
   
   @Test
   public void defaultsForSnapshot() {
     final Version snapshot = new Version();
     assertTrue( snapshot.isSnapshot() ) ;
     assertEquals( "SNAPSHOT", snapshot.getName() ) ;
   }
   
   @Test
   public void parseSnapshot() throws VersionFormatException {
    assertTrue( Version.parse( "@" + "project.version@" ).isSnapshot() ) ;
     assertTrue( Version.parse( "SNAPSHOT" ).isSnapshot() ) ;
     assertTrue( Version.parse( new Version().getName() ).isSnapshot() ) ;
   }
   
   @Test
   public void parse0_1_23() throws VersionFormatException {
     final Version version = Version.parse( "0.1.23" ) ;
     assertFalse( version.isSnapshot() ) ;
     assertEquals(  0, version.getMajor() ) ;
     assertEquals(  1, version.getMinor() ) ;
     assertEquals( 23, version.getFix() ) ;
     assertEquals( "0.1.23", version.getName() ) ;
   }
   
   @Test
   public void normalNumberedVersion() {
     final Version version = new Version( 0, 12, 3 ) ;
     assertFalse( version.isSnapshot() ) ;
     assertEquals( 0, version.getMajor() ) ;
     assertEquals( 12, version.getMinor() ) ;
     assertEquals( 3, version.getFix() ) ;
   }
   
   @Test( expected = VersionFormatException.class )
   public void rejectUnparseableString() throws VersionFormatException {
     Version.parse( "0" ) ;
   }
   
   @Test( expected = IllegalArgumentException.class )
   public void rejectWrongMajorNumber() throws VersionFormatException {
     new Version( -1, 2, 3 ) ;
   }
   
   @Test( expected = IllegalArgumentException.class )
   public void rejectWrongMinorNumber() throws VersionFormatException {
     new Version( 1, -2, 3 ) ;
   }
   
   @Test( expected = IllegalArgumentException.class )
   public void rejectWrongFixNumber() throws VersionFormatException {
     new Version( 1, 2, -3 ) ;
   }
   
   @Test( expected = IllegalStateException.class )
   public void noMajorNumberForSnapshot() {
     new Version().getMajor() ;
   }
   
   @Test( expected = IllegalStateException.class )
   public void noMinorNumberForSnapshot() {
     new Version().getMinor() ;
   }
   
   @Test( expected = IllegalStateException.class )
   public void noFixNumberForSnapshot() {
     new Version().getFix() ;
   }
   
   @Test
   public void compare() {
     final Version vSnapshotA = new Version() ;
     final Version vSnapshotB = new Version() ;
     final Version v3_1_1 = new Version( 3, 1, 1 ) ;
     final Version v3_1_1b = new Version( 3, 1, 1 ) ;
     final Version v2_3_1 = new Version( 2, 3, 1 ) ;
     final Version v2_1_3 = new Version( 2, 1, 3 ) ;
     final Version v1_3_3 = new Version( 1, 3, 3 ) ;
     final Version v1_3_4 = new Version( 1, 3, 4 ) ;
     
     compare(  0, null,       null ) ;
     
     compare( -1, null,       vSnapshotA ) ;
     compare(  1, vSnapshotA, null ) ;
     
     compare(  0, vSnapshotA, vSnapshotA ) ;
     compare(  0, vSnapshotA, vSnapshotB ) ;
     compare(  0, v3_1_1,     v3_1_1b ) ;
     compare(  0, v3_1_1,     v3_1_1b ) ;
     compare(  0, v3_1_1b,    v3_1_1 ) ;
 
     compare(  1, vSnapshotA, v1_3_3 ) ;
     compare( -1, v1_3_3,     vSnapshotA ) ;
    
     compare( -1, v2_3_1, v3_1_1 ) ;
     compare(  1, v3_1_1, v2_3_1 ) ;
     
     compare( -1, v2_1_3, v2_3_1 ) ;
     compare(  1, v2_3_1, v2_1_3 ) ;
     
     compare( -1, v1_3_3, v1_3_4 ) ;
     compare(  1, v1_3_4, v1_3_3 ) ;
   }
   
 // =======  
 // Fixture  
 // =======
   
   private static void compare( 
       final int expected, 
       final Version version1, 
       final Version version2 
   ) {
     if( expected == 0 ) {
       assertEquals( expected, Version.COMPARATOR.compare( version1, version2 ) ) ;
     } else {
       final int result =  Version.COMPARATOR.compare( version1, version2 ) ;
       if( expected < 0 ) {
         assertTrue( "Got " + result, result < 0 ) ;
       } else {
         assertTrue( "Got " + result, result > 0 ) ;
       }
     }
   }
 
 }
