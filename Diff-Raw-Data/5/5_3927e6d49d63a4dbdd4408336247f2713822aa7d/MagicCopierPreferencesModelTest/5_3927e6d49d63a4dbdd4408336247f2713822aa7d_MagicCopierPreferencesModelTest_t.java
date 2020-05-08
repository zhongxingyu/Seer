 /*
  * To change this license header, choose License Headers in Project Properties.
  * To change this template file, choose Tools | Templates
  * and open the template in the editor.
  */
 package magiccopier;
 
 import java.io.File;
 import javafx.beans.property.DoubleProperty;
 import javafx.beans.property.LongProperty;
 import javafx.beans.property.ObjectProperty;
 import magiccopier.StorageSize.StorageSizeUnit;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author abhishekmunie
  */
 public class MagicCopierPreferencesModelTest {
 
     public MagicCopierPreferencesModelTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of getDrives method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testGetDrives() {
         System.out.println("getDrives");
         Drive[] expResult = null;
         Drive[] result = MagicCopierPreferencesModel.getDrives();
         assertArrayEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of scaleSize method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testScaleSize() {
         System.out.println("scaleSize");
         long size = 0L;
         StorageSizeUnit unit = null;
         double expResult = 0.0;
        double result = MagicCopierPreferencesModel.scaleSize(size, unit, 100);
         assertEquals(expResult, result, 0.0);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of destinationDirectoryProperty method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testDestinationDirectoryProperty() {
         System.out.println("destinationDirectoryProperty");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         ObjectProperty<File> expResult = null;
         ObjectProperty<File> result = instance.destinationDirectoryProperty();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getDestinationDirectory method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testGetDestinationDirectory() {
         System.out.println("getDestinationDirectory");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         File expResult = null;
         File result = instance.getDestinationDirectory();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of maxSizeProperty method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testMaxSizeProperty() {
         System.out.println("maxSizeProperty");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         LongProperty expResult = null;
         LongProperty result = instance.maxSizeProperty();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of maxSizeUnitProperty method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testMaxSizeUnitProperty() {
         System.out.println("maxSizeUnitProperty");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         ObjectProperty<StorageSizeUnit> expResult = null;
         ObjectProperty<StorageSizeUnit> result = instance.maxSizeUnitProperty();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of scaledMaxSizeProperty method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testScaledMaxSizeProperty() {
         System.out.println("scaledMaxSizeProperty");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         DoubleProperty expResult = null;
         DoubleProperty result = instance.scaledMaxSizeProperty();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getSacledMaxSize method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testGetSacledMaxSize() {
         System.out.println("getSacledMaxSize");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         double expResult = 0.0;
         double result = instance.getSacledMaxSize();
         assertEquals(expResult, result, 0.0);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getMaxSize method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testGetMaxSize() {
         System.out.println("getMaxSize");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         long expResult = 0L;
         long result = instance.getMaxSize();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of getMaxSizeUnit method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testGetMaxSizeUnit() {
         System.out.println("getMaxSizeUnit");
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         StorageSizeUnit expResult = null;
         StorageSizeUnit result = instance.getMaxSizeUnit();
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of setMaxSize method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testSetMaxSize() {
         System.out.println("setMaxSize");
         long maxSize = 0L;
         StorageSizeUnit maxSizeUnit = null;
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
         instance.setMaxSize(maxSize, maxSizeUnit);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
     /**
      * Test of setMaxSizeFromScaledSize method, of class MagicCopierPreferencesModel.
      */
     @Test
     public void testSetMaxSizeFromScaledSize() {
         System.out.println("setMaxSizeFromScaledSize");
         double scaledSize = 0.0;
         MagicCopierPreferencesModel instance = new MagicCopierPreferencesModel();
        instance.setMaxSizeFromScaledSize(scaledSize, 100);
         // TODO review the generated test code and remove the default call to fail.
         fail("The test case is a prototype.");
     }
 
 }
