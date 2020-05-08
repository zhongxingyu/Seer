 package info.mikaelsvensson.ftpbackup.util;
 
 import org.junit.Test;
 
 import java.text.DateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class FTPBackupExtendedMessageFormatFactoryTest {
 
     @Test
     public void testSimpleArgument() throws Exception {
         performNumberTest("123,456.789k", "{0,prefix,k}", 123456789L);
     }
     @Test
     public void testNoArgument() throws Exception {
         performNumberTest("123.457M", "{0,prefix}", 123456789L);
     }
     @Test
     public void testComplexArgument() throws Exception {
         performNumberTest("008.50 Ki", "{0,prefix,3.2_Ki}", 8 * 1024L + 512);
     }
 
     @Test
     public void testSingleNamedString() throws Exception {
         performNamedArgumentTest("hello", "{arg}", Collections.<String, Object>singletonMap("arg", "hello"));
     }
     @Test
     public void testSingleNamedDate() throws Exception {
        performNamedArgumentTest(DateFormat.getDateInstance(DateFormat.SHORT).format(new Date()), "{today,date,short}", Collections.<String, Object>singletonMap("today", new Date()));
     }
     @Test
     public void testNamedAndNamelessArguments() throws Exception {
         performNamedArgumentTest("before hello after", "{0} {arg} {1}", Collections.<String, Object>singletonMap("arg", "hello"), "before", "after");
     }
     @Test
     public void testOnlyNamelessArguments() throws Exception {
         performNamedArgumentTest("hello, world!", "{0}, {1}!", Collections.<String, Object>emptyMap(), "hello", "world");
     }
 
     private void performNumberTest(String expected, String format, long value) throws UnknownNamedArgumentException {
         assertThat(FTPBackupExtendedMessageFormatFactory.format(format, Locale.ENGLISH, new Object[]{value}, null), is(expected));
     }
 
     private void performNamedArgumentTest(String expected, String format, Map<String, Object> namedArguments, Object... arguments) throws UnknownNamedArgumentException {
        assertThat(FTPBackupExtendedMessageFormatFactory.format(format, Locale.ENGLISH, arguments, namedArguments), is(expected));
     }
 }
