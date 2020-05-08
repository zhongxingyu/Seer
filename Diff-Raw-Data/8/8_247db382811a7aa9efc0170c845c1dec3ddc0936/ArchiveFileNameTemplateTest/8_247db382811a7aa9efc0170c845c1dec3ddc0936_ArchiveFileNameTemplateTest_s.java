 package info.mikaelsvensson.ftpbackup.command.job;
 
 import org.junit.Test;
 
 import java.text.SimpleDateFormat;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class ArchiveFileNameTemplateTest {
     @Test
     public void testName() throws Exception {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         assertThat(sdf.format(ArchiveFileNameTemplate.DOT_DATE.getArchivingDate("hello.txt.20120824-235959")), is("2012-08-24"));
         assertThat(ArchiveFileNameTemplate.DOT_DATE.getOriginalFileName("hello.txt.20120824-235959"), is("hello.txt"));
     }
 }
