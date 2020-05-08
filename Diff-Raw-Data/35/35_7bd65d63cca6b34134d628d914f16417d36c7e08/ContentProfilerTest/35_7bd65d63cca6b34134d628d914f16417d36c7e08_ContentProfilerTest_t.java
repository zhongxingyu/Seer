 /**
  * 
  */
 package net.lovelycode.dp.contentprofiler;
 
 import java.io.File;
 
 import net.lovelycode.dp.contentprofiler.metadata.FileSummarizeEvent;
 import net.lovelycode.dp.contentprofiler.metadata.FileSummary;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author AnJackson
  *
  */
 public class ContentProfilerTest {
     
     static String[] files = { 
        "src/test/data/files/WO1_GWHD_1869_01_20-0001.jp2",
        "src/test/data/files/WO1_GWHD_1869_01_20-0004.tif",
        "src/test/data/files/WO1_MCLN_1856_12_30-0001.tif",
         "src/test/data/files/Emulation Experiment Integration.odt",
         "src/test/data/files/Emulation Experiment Integration.doc",
     };
     
     static String testFolder = "src/test/data";
 
     /**
      * @throws java.lang.Exception
      */
     @Before
     public void setUp() throws Exception {
     }
 
     /**
      * Test method for {@link uk.bl.dpt.contentprofiler.ContentProfiler#fileSystem()}.
      * @throws SigarException 
      */
     @Test
     public void testFileSystem() {
         FileSummarizeEvent event = ContentProfiler.profile(new File(testFolder), null);
         for( FileSummary fs: event.getSummaries() ) {
             System.out.println(fs.toString());
         }
     }
     
     /*
      <fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://hul.harvard.edu/ois/xml/ns/fits/fits_output http://hul.harvard.edu/ois/xml/xsd/fits/fits_output.xsd" version="0.2.5" timestamp="12/10/09 09:59">
   <identification status="SINGLE_RESULT">
     <identity format="Portable Network Graphics" mimetype="image/png">
       <tool toolname="file utility" toolversion="4.26" />
       <tool toolname="Exiftool" toolversion="7.74" />
       <tool toolname="Droid" toolversion="3.0" />
       <tool toolname="ffident" toolversion="0.2" />
       <version toolname="Droid" toolversion="3.0">1.0</version>
       <externalIdentifier toolname="Droid" toolversion="3.0" type="puid">fmt/11</externalIdentifier>
     </identity>
   </identification>
   <fileinfo>
     <lastmodified toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">2009:10:09 14:52:35+01:00</lastmodified>
     <filename toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">C:\BritishLibrary\Eclipse\workspace-bl\ContentProfiler\BLProfiler\src\test\data\files\chartserver.png</filename>
     <size toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">16321</size>
     <md5checksum toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">9ecf56511f18870b9a66b623a4bdac66</md5checksum>
     <fslastmodified toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">1255096355984</fslastmodified>
   </fileinfo>
   <filestatus />
   <metadata>
     <image>
       <compressionScheme toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">Deflate/Inflate</compressionScheme>
       <imageWidth toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">400</imageWidth>
       <imageHeight toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">300</imageHeight>
     </image>
   </metadata>
 </fits>
      */
 
     /*
      * 
 <fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://hul.harvard.edu/ois/xml/ns/fits/fits_output http://hul.harvard.edu/ois/xml/xsd/fits/fits_output.xsd" version="0.2.5" timestamp="09/10/09 16:09">
   <identification status="SINGLE_RESULT">
     <identity format="JPEG 2000" mimetype="image/jp2">
       <tool toolname="Jhove" toolversion="1.3" />
       <tool toolname="file utility" toolversion="4.26" />
       <tool toolname="Exiftool" toolversion="7.74" />
     </identity>
   </identification>
   <fileinfo>
     <size toolname="Jhove" toolversion="1.3">63958</size>
     <lastmodified toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">2009:10:09 14:52:36+01:00</lastmodified>
     <filename toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">C:\BritishLibrary\Eclipse\workspace-bl\ContentProfiler\BLProfiler\src\test\data\files\world.jp2</filename>
     <md5checksum toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">ae8a44361dbd5b472cc2d6efde063bc6</md5checksum>
     <fslastmodified toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">1255096356000</fslastmodified>
   </fileinfo>
   <filestatus>
     <well-formed toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">true</well-formed>
     <valid toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">true</valid>
   </filestatus>
   <metadata>
     <image>
       <byteOrder toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">big-endian</byteOrder>
       <compressionScheme toolname="Jhove" toolversion="1.3" status="CONFLICT">JPEG 2000 Lossy</compressionScheme>
       <compressionScheme toolname="Exiftool" toolversion="7.74" status="CONFLICT">JPEG 2000</compressionScheme>
       <imageWidth toolname="Jhove" toolversion="1.3">800</imageWidth>
       <imageHeight toolname="Jhove" toolversion="1.3">400</imageHeight>
       <colorSpace toolname="Jhove" toolversion="1.3">sRGB</colorSpace>
       <tileWidth toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">800</tileWidth>
       <tileHeight toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">400</tileHeight>
       <qualityLayers toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">3</qualityLayers>
       <resolutionLevels toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">1</resolutionLevels>
       <bitsPerSample toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">8 8 8</bitsPerSample>
       <samplesPerPixel toolname="Jhove" toolversion="1.3" status="SINGLE_RESULT">3</samplesPerPixel>
     </image>
   </metadata>
 </fits>
      */
 
     /*
      <fits xmlns="http://hul.harvard.edu/ois/xml/ns/fits/fits_output" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://hul.harvard.edu/ois/xml/ns/fits/fits_output http://hul.harvard.edu/ois/xml/xsd/fits/fits_output.xsd" version="0.2.5" timestamp="12/10/09 10:32">
   <identification status="CONFLICT">
     <identity format="Microsoft Word" mimetype="application/msword">
       <tool toolname="Exiftool" toolversion="7.74" />
       <tool toolname="Droid" toolversion="3.0" />
       <tool toolname="NLNZ Metadata Extractor" toolversion="3.4GA" />
       <version toolname="Droid" toolversion="3.0">97-2003</version>
       <externalIdentifier toolname="Droid" toolversion="3.0" type="puid">fmt/40</externalIdentifier>
     </identity>
     <identity format="Microsoft Excel Format" mimetype="application/vnd.ms-excel">
       <tool toolname="ffident" toolversion="0.2" />
     </identity>
   </identification>
   <fileinfo>
     <lastmodified toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">2009:10:12 10:31:39+01:00</lastmodified>
     <created toolname="Exiftool" toolversion="7.74" status="CONFLICT">2009:10:06 12:07:06</created>
     <created toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="CONFLICT">2009-10-06 13:07:06</created>
     <creatingApplicationName toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">null</creatingApplicationName>
     <creatingApplicationVersion toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">0x204d</creatingApplicationVersion>
     <filename toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">C:\BritishLibrary\Eclipse\workspace-bl\ContentProfiler\BLProfiler\src\test\data\files\Emulation Experiment Integration.doc</filename>
     <size toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">35840</size>
     <md5checksum toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">a93ca7fb268407ebd28efc9f4c24cb15</md5checksum>
     <fslastmodified toolname="OIS File Information" toolversion="0.1" status="SINGLE_RESULT">1255339899144</fslastmodified>
   </fileinfo>
   <filestatus />
   <metadata>
     <document>
       <title toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">null</title>
       <author toolname="Exiftool" toolversion="7.74">Andy Jackson</author>
       <isRightsManaged toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">no</isRightsManaged>
       <isProtected toolname="Exiftool" toolversion="7.74" status="SINGLE_RESULT">no</isProtected>
       <pages toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">0</pages>
       <wordCount toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">0</wordCount>
       <characterCount toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">0</characterCount>
       <language toolname="NLNZ Metadata Extractor" toolversion="3.4GA" status="SINGLE_RESULT">U.S. English</language>
     </document>
   </metadata>
 </fits>
      */
 
 }
