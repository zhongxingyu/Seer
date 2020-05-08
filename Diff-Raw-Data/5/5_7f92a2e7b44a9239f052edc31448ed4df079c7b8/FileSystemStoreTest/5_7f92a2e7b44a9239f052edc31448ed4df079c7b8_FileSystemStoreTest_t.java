 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.store;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests {@link FileSystemStore}.
  *
  * @author Willi Schoenborn
  */
 public final class FileSystemStoreTest extends AbstractStoreTest {
 
     private final File directory = new File(System.getProperty("java.io.tmpdir"), "store");
     
     @Override
     public Store unit() {
         try {
             return new FileSystemStore(directory);
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
     }
     
     /**
      * Runs after every test and deletes the store directory.
      * 
      * @throws IOException should not happen
      */
     @After
     public void cleanup() throws IOException {
         FileUtils.deleteDirectory(directory);
     }
     
     /**
      * Tests whether {@link FileSystemStore#delete(String)} automatically deletes
      * empty parent directories.
      * 
      * @throws IOException should not happen 
      */
     @Test
     public void deleteEmptyDirectories() throws IOException {
         final Store unit = unit();
         final InputStream stream = getClass().getClassLoader().getResourceAsStream("willi.png");
         final String identifier = unit.create(stream);
         Assert.assertTrue(IOUtils.contentEquals(
             getClass().getClassLoader().getResourceAsStream("willi.png"), 
             unit.read(identifier))
         );
     }
     
     /**
      * Tests whether {@link FileSystemStore#delete(String)} keeps non empty directories
      * while deleting parent directories.
      * 
      * @throws IOException should not happen
      */
     @Test
     public void keepNonEmptyDirectories() throws IOException {
         final Store unit = unit();
         final InputStream stream = getClass().getClassLoader().getResourceAsStream("willi.png");
         final String identifier = unit.create(stream);
         stream.close();
         final File target = new File(directory, identifier.replace("-", File.separator));
         Assert.assertTrue(target.exists());
         final File top = new File(directory, identifier.split("-")[0]);
         Assert.assertTrue(top.exists());
        IOUtils.copy(
            getClass().getClassLoader().getResourceAsStream("willi.png"), 
            new FileOutputStream(new File(top, "willi.png"))
        );
         unit.delete(identifier);
         Assert.assertFalse(target.exists());
         Assert.assertTrue(top.exists());
     }
     
 }
