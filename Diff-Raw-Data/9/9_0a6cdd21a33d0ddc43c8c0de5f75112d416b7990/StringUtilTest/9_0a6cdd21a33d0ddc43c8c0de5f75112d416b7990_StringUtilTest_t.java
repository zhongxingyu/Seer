 package org.cujau.utils;
 
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class StringUtilTest {
 
     @Before
     public void before() {
         System.setProperty( "asdf.asdf.asdf", "ASDF.ASDF.ASDF" );
         System.setProperty( "my.you", "love.me" );
         System.setProperty( "home.free.or.die", "live.free.or.die" );
         System.setProperty( "abc.def", "alphabet" );
         System.setProperty( "123.456", "numbers" );
         System.setProperty( "ab_cd.ef_gh", "letter" );
     }
     
     @Test
     public void stringArrayTest() {
         String[] ary = new String[] { "abc", "def", "ghi" };
         String res = StringUtil.toString( ary );
         assertTrue( "abc,def,ghi".equals( res ) );
         res = StringUtil.toString( ary, "\n" );
         assertTrue( "abc\ndef\nghi".equals( res ) );
         
         ary = new String[] { "abc" };
         res = StringUtil.toString( ary );
         assertTrue( "abc".equals( res ) );
         
         ary = new String[] {};
         res = StringUtil.toString( ary );
         assertTrue( "".equals( res ) );
         
         ary = new String[] { "abc", null, "ghi" };
         res = StringUtil.toString( ary );
         assertTrue( "abc,null,ghi".equals( res ) );
         
         ary = new String[] { null };
         res = StringUtil.toString( ary );
         assertTrue( "null".equals( res ) );
     }
     
     @Test
     public void intArrayTest() {
         Integer[] ary = new Integer[] { 123, 456, 789 };
         String res = StringUtil.toString( ary );
         assertTrue( "123,456,789".equals( res ) );
         
         ary = new Integer[] { 123 };
         res = StringUtil.toString( ary );
         assertTrue( "123".equals( res ) );
         
         ary = new Integer[] {};
         res = StringUtil.toString( ary );
         assertTrue( "".equals( res ) );
         
         ary = new Integer[] { 123, null, 789 };
         res = StringUtil.toString( ary );
         assertTrue( "123,null,789".equals( res ) );
         
         ary = new Integer[] { null };
         res = StringUtil.toString( ary );
         assertTrue( "null".equals( res ) );
     }
     
     @Test
     public void collectionStringTest() {
         ArrayList<Integer> col = new ArrayList<Integer>();
         col.add( 1 );
         col.add( 2 );
         col.add( 5 );
         col.add( 3 );
         String res = StringUtil.toString( col );
         assertTrue( "1,2,5,3".equals( res ) );
         res = StringUtil.toString( col, ";" );
         assertEquals( "1;2;5;3", res );
        res = StringUtil.toString( col, " -|- " );
        assertEquals( "1 -|- 2 -|- 5 -|- 3", res );
         
         res = StringUtil.toString( new ArrayList<String>() );
         assertTrue( "".equals( res ) );
         
         res = StringUtil.toString( (List<?>) null );
         assertTrue( "".equals( res ) );
     }
     
     @Test
     public void propertyReplacementTest() {
         String str = "asdfasdf ${asdf.asdf.asdf} asdf ${my.you}. ${home.free.or.die}";
         Pattern propPattern = Pattern.compile("(\\$\\{([\\w\\.]+)\\})");
         Matcher m = propPattern.matcher(str);
 
         while (m.find()) {
             String propName = m.group(2);
             String propReplace = m.group(1);
             // LOG.debug( "{}, {}, {}, {}, {}", new Object[] {m.start(),
             // m.end(), m.group(),
             // m.group(1), m.group(2)} );
             str = str.replace(propReplace, System.getProperty(propName));
         }
 
         // LOG.debug( "str = {}", str );
     }
 
     @Test
     public void propTest() {
         String str = "A${abc.def}B${xyz.abc}C${123.456}";
         str = StringUtil.replaceProperties(str);
         assertTrue(str.equals("AalphabetB${xyz.abc}Cnumbers"));
 
         String str2 = StringUtil.replaceProperties(str, null);
         assertTrue(str.equals(str2));
 
         str = "A${abc.def}B${xyz.abc}C${123.456}";
         str = StringUtil.replaceProperties(str, System.getProperties(), true);
         assertTrue(str.equals("AalphabetBCnumbers"));
 
         str = "A${abc.def}B${xyz.abc}C${123.456}A${abc.def}";
         str = StringUtil.replaceProperties(str, System.getProperties(), true);
         assertTrue(str.equals("AalphabetBCnumbersAalphabet"));
     }
     
 
 }
