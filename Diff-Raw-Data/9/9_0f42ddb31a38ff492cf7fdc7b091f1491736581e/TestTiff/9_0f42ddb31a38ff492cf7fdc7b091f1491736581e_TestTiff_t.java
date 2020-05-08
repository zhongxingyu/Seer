 package org.thiesen.exiftool;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.apache.commons.lang.StringUtils;
 import org.thiesen.exiftool.tiff.TiffReader;
 
 public class TestTiff {
 
 
     public static void main(String[] args) throws FileNotFoundException, IOException {
        final TiffReader reader = TiffReader.read(new FileInputStream(new File("test.tif")));
         
         System.out.println( StringUtils.join(reader.parse(), "\n" ) );
         
         
     }
 
 }
