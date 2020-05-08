 /**
  * Copyright (C) 2011-
  *
  * All rights reserved.
  */
 package gov.va.vinci.v3nlp.services.hitex;
 
 import gov.va.vinci.v3nlp.Utilities;
 import gov.va.vinci.v3nlp.services.gate.HitexSectionizerImpl;
 import org.junit.Test;
 
 import java.util.List;
 
 import static org.junit.Assert.fail;
 
 public class HitexSectionizerImplTest {
 
     private static String SECTIONIZER_CONFIG_CREATE_TEST1_INPUT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
             "<!--  This file contains header definitions for Sectionizer NLP component -->\n" +
             "<headers>\t\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}CAROTID\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}FEMORAL\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABD\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"COMMENTS\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)COMMENTS:]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER, HIST\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABUSE\\s{1,3}HISTORY\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<includes>\n" +
             "\t\t<include>OTHER</include>\n" +
             "\t\t<include>COMMENTS</include> " +
             "\t</includes>\n" +
             "\t<excludes>\n" +
             "\t\t<exclude>HIST</exclude>\n" +
             "\t</excludes>\n" +
             "</headers>";
 
     private static String SECTIONIZER_CONFIG_CREATE_TEST2_INPUT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
             "<!--  This file contains header definitions for Sectionizer NLP component -->\n" +
             "<headers>\t\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}CAROTID\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}FEMORAL\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABD\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"COMMENTS\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)COMMENTS:]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER, HIST\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABUSE\\s{1,3}HISTORY\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<includes>\n" +
             "\t\t<include>COMMENTS</include> " +
             "\t</includes>\n" +
             "\t<excludes>\n" +
             "\t\t<exclude>HIST</exclude>\n" +
             "\t\t<exclude>OTHER</exclude>\n" +
             "\t</excludes>\n" +
             "</headers>";
 
     private static String SECTIONIZER_CONFIG_CREATE_TEST1_OUTPUT =
             "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                     "<headers>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}CAROTID\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}FEMORAL\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)ABD\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"COMMENTS\">\n" +
                     "\t\t<![CDATA[(?i)COMMENTS:]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER, HIST\">\n" +
                     "\t\t<![CDATA[(?i)ABUSE\\s{1,3}HISTORY\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "</headers>\n";
 
     private static String SECTIONIZER_CONFIG_CREATE_TEST2_OUTPUT =
             "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n" +
                     "<headers>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}CAROTID\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}FEMORAL\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER\">\n" +
                     "\t\t<![CDATA[(?i)ABD\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"COMMENTS\">\n" +
                     "\t\t<![CDATA[(?i)COMMENTS:]]>\n" +
                     "\t</header>\n" +
                     "<header captGroupNum=\"0\" categories=\"OTHER, HIST\">\n" +
                     "\t\t<![CDATA[(?i)ABUSE\\s{1,3}HISTORY\\s{0,3}(:|;)]]>\n" +
                     "\t</header>\n" +
                     "</headers>\n";
 
     private static String SECTIONIZER_CONFIG_CREATE_TEST3_INPUT = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
             "<!--  This file contains header definitions for Sectionizer NLP component -->\n" +
             "<headers>\t\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}CAROTID\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)\\(BILATERAL\\)\\s{1,3}FEMORAL\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABD\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"COMMENTS\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)COMMENTS:]]>\n" +
             "\t</header>\n" +
             "\t<header categories=\"OTHER, HIST\" captGroupNum=\"0\" >\n" +
             "\t\t<![CDATA[(?i)ABUSE\\s{1,3}HISTORY\\s{0,3}(:|;)]]>\n" +
             "\t</header>\n" +
             "</headers>";
 
     @Test
     public void sectionizerConfigCreatorTest1() throws Exception {
         String result = Utilities.transformDoc(HitexSectionizerImpl.sectionizerConfigHeaders(SECTIONIZER_CONFIG_CREATE_TEST1_INPUT));
        result=result.replaceAll("\r\n", "\n");
         assert (result.equals(SECTIONIZER_CONFIG_CREATE_TEST1_OUTPUT));
     }
 
     @Test
     public void sectionizerConfigCreatorTest2() throws Exception {
         String result = Utilities.transformDoc(HitexSectionizerImpl.sectionizerConfigHeaders(SECTIONIZER_CONFIG_CREATE_TEST2_INPUT));
        result=result.replaceAll("\r\n", "\n");
         assert (result.equals(SECTIONIZER_CONFIG_CREATE_TEST2_OUTPUT));
     }
 
    @Test
     public void sectionizerConfigCreatorTestException() throws Exception {
         try {
             HitexSectionizerImpl.sectionizerConfigHeaders("THIS IS JUNK!");
         } catch (RuntimeException e) {
             return;
         }
         fail("Expected exception.");
     }
 
     @Test
     public void testIncludes() throws Exception {
         List<String> result = HitexSectionizerImpl.getSectionizerConfigIncludes(SECTIONIZER_CONFIG_CREATE_TEST1_INPUT);
         assert (result.size() == 2);
         assert (result.contains("OTHER"));
         assert (result.contains("COMMENTS"));
 
         result = HitexSectionizerImpl.getSectionizerConfigIncludes(SECTIONIZER_CONFIG_CREATE_TEST3_INPUT);
         assert (result.size() == 0);
     }
 
 
     @Test
     public void testIncludesInvalidXml() throws Exception {
         try {
             HitexSectionizerImpl.getSectionizerConfigIncludes("THIS IS JUNK!");
         } catch (RuntimeException e) {
             return;
         }
         fail("Expected exception.");
     }
 
     @Test
     public void testExcludes() throws Exception {
         List<String> result = HitexSectionizerImpl.getSectionizerConfigExcludes(SECTIONIZER_CONFIG_CREATE_TEST1_INPUT);
         assert (result.size() == 1);
         assert (result.contains("HIST"));
         result = HitexSectionizerImpl.getSectionizerConfigExcludes(SECTIONIZER_CONFIG_CREATE_TEST3_INPUT);
         assert (result.size() == 0);
 
     }
 
 }
