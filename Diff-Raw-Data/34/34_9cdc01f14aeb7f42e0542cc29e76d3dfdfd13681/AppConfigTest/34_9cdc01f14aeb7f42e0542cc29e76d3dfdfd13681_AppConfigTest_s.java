 package net.ccaper.LandscapePortraitImageSort.spring;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.util.StringUtils;
 
 public class AppConfigTest {
 
   @Before
   public void setUp() throws Exception {
   }
 
   @After
   public void tearDown() throws Exception {
   }
 
   @Test
   public void testConvertSlashToOsFileDelimiter_UnixSeparator()
       throws Exception {
     assertEquals("C:/dir1/dir2/file.txt",
         AppConfig.convertSlashToOsFileDelimiter("C:\\dir1\\dir2\\file.txt",
             AppConfig.UNIX_FILE_DELIMETER));
     assertEquals("blah", AppConfig.convertSlashToOsFileDelimiter("blah",
         AppConfig.UNIX_FILE_DELIMETER));
     assertEquals("", AppConfig.convertSlashToOsFileDelimiter("",
         AppConfig.UNIX_FILE_DELIMETER));
     assertEquals(null, AppConfig.convertSlashToOsFileDelimiter(null,
         AppConfig.UNIX_FILE_DELIMETER));
   }
 
   @Test
   public void testConvertSlashToOsFileDelimiter_MsSeparator() throws Exception {
     assertEquals("\\dir1\\dir2\\file.txt",
         AppConfig.convertSlashToOsFileDelimiter("/dir1/dir2/file.txt",
             AppConfig.MS_FILE_DELIMETER));
     assertEquals("blah", AppConfig.convertSlashToOsFileDelimiter("blah",
         AppConfig.MS_FILE_DELIMETER));
     assertEquals(null, AppConfig.convertSlashToOsFileDelimiter(null,
         AppConfig.MS_FILE_DELIMETER));
   }
 
   @Test(expected = IllegalArgumentException.class)
   public void testConvertSlashToOsFileDelimiter_BadDelimiters()
       throws Exception {
     AppConfig.convertSlashToOsFileDelimiter("someString", "-");
     AppConfig.convertSlashToOsFileDelimiter("someString", "");
     AppConfig.convertSlashToOsFileDelimiter("someString", null);
   }
 
   @Test
   public void testGenerateFileFromString() throws Exception {
     String string = " dir1/dir2/file1.txt ";
     assertEquals(new File(StringUtils.trimAllWhitespace(string)),
         AppConfig.generateFileFromString(string));
   }
 }
