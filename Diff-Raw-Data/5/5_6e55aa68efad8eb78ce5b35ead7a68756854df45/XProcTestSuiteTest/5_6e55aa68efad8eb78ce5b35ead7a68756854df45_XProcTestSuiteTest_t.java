 /*
  * Copyright (C) 2010 Herve Quiroz
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  */
 package org.trancecode.xproc;
 
 import com.google.common.io.Files;
 
 import java.io.File;
 import java.net.URL;
 
 import org.testng.annotations.Test;
 import org.trancecode.io.Paths;
 import org.trancecode.io.Urls;
 
 /**
  * @author Herve Quiroz
  */
 public class XProcTestSuiteTest extends AbstractXProcTest
 {
     @Test
     public void required_add_attribute_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-001.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-002.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-003.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-004.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-005.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-006.xml"), "required");
     }
 
     @Test
     public void required_add_attribute_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-attribute-007.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-001.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-002.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-003.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-004.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-005.xml"), "required");
     }
 
     @Test
     public void required_add_xml_base_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/add-xml-base-006.xml"), "required");
     }
 
     @Test
     public void required_base_uri_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/base-uri-001.xml"), "required");
     }
 
     @Test
     public void required_base_uri_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/base-uri-002.xml"), "required");
     }
 
     @Test
     public void required_base_uri_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/base-uri-003.xml"), "required");
     }
 
     @Test
     public void required_choose_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-001.xml"), "required");
     }
 
     @Test
     public void required_choose_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-002.xml"), "required");
     }
 
     @Test
     public void required_choose_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-003.xml"), "required");
     }
 
     @Test
     public void required_choose_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-004.xml"), "required");
     }
 
     @Test
     public void required_choose_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-005.xml"), "required");
     }
 
     @Test
     public void required_choose_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-006.xml"), "required");
     }
 
     @Test
     public void required_choose_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/choose-007.xml"), "required");
     }
 
     @Test
     public void required_compare_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/compare-001.xml"), "required");
     }
 
     @Test
     public void required_compare_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/compare-002.xml"), "required");
     }
 
     @Test
     public void required_compare_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/compare-003.xml"), "required");
     }
 
     @Test
     public void required_compare_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/compare-004.xml"), "required");
     }
 
     @Test
     public void required_compare_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/compare-005.xml"), "required");
     }
 
     @Test
     public void required_count_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/count-001.xml"), "required");
     }
 
     @Test
     public void required_count_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/count-002.xml"), "required");
     }
 
     @Test
     public void required_count_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/count-003.xml"), "required");
     }
 
     @Test
     public void required_data_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-001.xml"), "required");
     }
 
     @Test
     public void required_data_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-002.xml"), "required");
     }
 
     @Test
     public void required_data_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-003.xml"), "required");
     }
 
     @Test
     public void required_data_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-004.xml"), "required");
     }
 
     @Test
     public void required_data_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-005.xml"), "required");
     }
 
     @Test
     public void required_data_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-006.xml"), "required");
     }
 
     @Test
     public void required_data_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-007.xml"), "required");
     }
 
     @Test
     public void required_data_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/data-008.xml"), "required");
     }
 
     @Test
     public void required_declare_step_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-001.xml"), "required");
     }
 
     @Test
     public void required_declare_step_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-002.xml"), "required");
     }
 
     @Test
     public void required_declare_step_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-003.xml"), "required");
     }
 
     @Test
     public void required_declare_step_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-004.xml"), "required");
     }
 
     @Test
     public void required_declare_step_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-005.xml"), "required");
     }
 
     @Test
     public void required_declare_step_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-006.xml"), "required");
     }
 
     @Test
     public void required_declare_step_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-007.xml"), "required");
     }
 
     @Test
     public void required_declare_step_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-008.xml"), "required");
     }
 
     @Test
     public void required_declare_step_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-009.xml"), "required");
     }
 
     @Test
     public void required_declare_step_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-010.xml"), "required");
     }
 
     @Test
     public void required_declare_step_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/declare-step-011.xml"), "required");
     }
 
     @Test
     public void required_delete_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/delete-001.xml"), "required");
     }
 
     @Test
     public void required_delete_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/delete-002.xml"), "required");
     }
 
     @Test
     public void required_delete_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/delete-003.xml"), "required");
     }
 
     @Test
     public void required_delete_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/delete-004.xml"), "required");
     }
 
     private File setupDirectoryListFiles() throws Exception
     {
         final File directory = File.createTempFile(getClass().getSimpleName(), "");
         directory.delete();
         directory.mkdir();
         final File directoryListTest = new File(directory, "directory-list-test");
         directoryListTest.mkdir();
         new File(directoryListTest, "afile").createNewFile();
         new File(directoryListTest, "bfile").createNewFile();
         final File adir = new File(directoryListTest, "adir");
         adir.mkdir();
         new File(adir, "cfile").createNewFile();
         final File bdir = new File(directoryListTest, "bdir");
         bdir.mkdir();
         bdir.setReadable(false);
         return directory;
     }
 
     private void testDirectoryList(final URL url, final String testSuite) throws Exception
     {
         final File directory = setupDirectoryListFiles();
         final File pipeline = new File(directory, Paths.getName(url.getPath()));
         Files.copy(Urls.asInputSupplier(url), pipeline);
         test(pipeline.toURI().toURL(), "required");
     }
 
     @Test
     public void required_directory_list_001() throws Exception
     {
         testDirectoryList(new URL("http://tests.xproc.org/tests/required/directory-list-001.xml"), "required");
     }
 
     @Test
     public void required_directory_list_002() throws Exception
     {
         testDirectoryList(new URL("http://tests.xproc.org/tests/required/directory-list-002.xml"), "required");
     }
 
     @Test
     public void required_document_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/document-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0002_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0002-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0003_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0003-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0003_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0003-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0004_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0004-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0005_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0005-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0005_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0005-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0006_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0006-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0010_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0010-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0010_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0010-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0012_001() throws Exception
     {
        testDirectoryList(new URL("http://tests.xproc.org/tests/required/err-c0012-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0013_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0013-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0014_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0014-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0014_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0014-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0017_001() throws Exception
     {
        testDirectoryList(new URL("http://tests.xproc.org/tests/required/err-c0017-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0019_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0019-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-003.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-004.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-005.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-006.xml"), "required");
     }
 
     @Test
     public void required_err_c0020_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0020-007.xml"), "required");
     }
 
     @Test
     public void required_err_c0022_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0022-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-003.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-004.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-005.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-006.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-007.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-008.xml"), "required");
     }
 
     @Test
     public void required_err_c0023_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0023-009.xml"), "required");
     }
 
     @Test
     public void required_err_c0025_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0025-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0025_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0025-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0027_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0027-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0027_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0027-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0027_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0027-003.xml"), "required");
     }
 
     @Test
     public void required_err_c0028_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0028-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0029_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0029-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0029_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0029-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0030_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0030-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0039_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0039-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0040_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0040-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0050_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0050-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0051_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0051-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0052_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0052-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0052_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0052-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0056_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0056-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0056_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0056-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0058_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0058-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0059_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0059-001.xml"), "required");
     }
 
     @Test
     public void required_err_c0059_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0059-002.xml"), "required");
     }
 
     @Test
     public void required_err_c0062_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-c0062-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0001_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0001-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0001_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0001-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0003_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0003-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0004_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0004-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0005_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0005-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0006_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0006-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0007_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0007-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0007_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0007-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0007_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0007-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0008_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0008-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0009_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0009-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0009_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0009-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0010_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0010-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0011_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0011-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0011_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0011-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0011_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0011-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0012_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0012-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0012_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0012-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0012_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0012-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0013_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0013-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0013_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0013-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0014_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0014-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0014_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0014-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0015_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0015-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0016_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0016-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0016_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0016-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0018_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0018-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0019_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0019-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0019_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0019-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0020_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0020-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0020_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0020-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0021_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0021-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0021_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0021-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0022_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0022-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-004.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-005.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-006.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-007.xml"), "required");
     }
 
     @Test
     public void required_err_d0023_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0023-008.xml"), "required");
     }
 
     @Test
     public void required_err_d0025_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0025-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0026_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0026-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0026_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0026-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0026_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0026-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0026_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0026-004.xml"), "required");
     }
 
     @Test
     public void required_err_d0026_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0026-005.xml"), "required");
     }
 
     @Test
     public void required_err_d0027_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0027-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0028_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0028-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0028_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0028-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0028_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0028-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0028_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0028-004.xml"), "required");
     }
 
     @Test
     public void required_err_d0029_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0029-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0029_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0029-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0030_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0030-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0030_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0030-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0031_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0031-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0031_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0031-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0033_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0033-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0033_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0033-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-001.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-002.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-003.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-004.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-005.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-006.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-007.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-008.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-009.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-010.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-011.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-012.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-013.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_014() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-014.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_015() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-015.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_016() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-016.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_017() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-017.xml"), "required");
     }
 
     @Test
     public void required_err_d0034_018() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-d0034-018.xml"), "required");
     }
 
     @Test
     public void required_err_primary_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-primary-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-006.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-007.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-008.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-010.xml"), "required");
     }
 
     @Test
     public void required_err_s0001_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0001-011.xml"), "required");
     }
 
     @Test
     public void required_err_s0002_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0002-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0002_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0002-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0003_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0003-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0003_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0003-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0003_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0003-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0004_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0004-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0004_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0004-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0004_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0004-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0004_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0004-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0004_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0004-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-006.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-007.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-008.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-009.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-010.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-011.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-012.xml"), "required");
     }
 
     @Test
     public void required_err_s0005_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0005-013.xml"), "required");
     }
 
     @Test
     public void required_err_s0006_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0006-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0007_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0007-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0007_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0007-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0007_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0007-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0008_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0008-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0009_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0009-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0009_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0009-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0009_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0009-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0009_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0009-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0010_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0010-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0010_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0010-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0010_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0010-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0011_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0011-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0011_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0011-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0011_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0011-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0011_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0011-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0014_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0014-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0015_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0015-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0017_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0017-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0018_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0018-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0018_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0018-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0018_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0018-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0019_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0019-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0020_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0020-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0020_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0020-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0020_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0020-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0022_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0022-006.xml"), "required");
     }
 
     @Test
     public void required_err_s0024_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0024-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0024_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0024-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0025_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0025-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0025_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0025-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0025_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0025-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0025_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0025-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0026_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0026-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0026_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0026-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0027_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0027-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0028_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0028-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0028_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0028-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0029_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0029-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0030_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0030-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0031_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0031-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0031_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0031-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0032_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0032-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0033_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0033-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0034_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0034-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0034_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0034-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0035_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0035-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0035_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0035-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0036_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0036-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0036_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0036-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0036_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0036-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0036_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0036-004.xml"), "required");
     }
 
     @Test
     public void required_err_s0036_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0036-005.xml"), "required");
     }
 
     @Test
     public void required_err_s0037_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0037-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0037_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0037-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0037_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0037-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0038_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0038-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0038_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0038-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0039_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0039-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0039_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0039-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0040_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0040-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0041_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0041-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0042_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0042-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0044_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0044-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0044_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0044-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0044_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0044-003.xml"), "required");
     }
 
     @Test
     public void required_err_s0048_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0048-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0051_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0051-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0051_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0051-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0052_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0052-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0052_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0052-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0053_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0053-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0055_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0055-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0055_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0055-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0057_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0057-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0057_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0057-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0058_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0058-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0059_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0059-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0061_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0061-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0062_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0062-001.xml"), "required");
     }
 
     @Test
     public void required_err_s0062_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0062-002.xml"), "required");
     }
 
     @Test
     public void required_err_s0063_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/err-s0063-001.xml"), "required");
     }
 
     @Test
     public void required_error_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/error-001.xml"), "required");
     }
 
     @Test
     public void required_error_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/error-002.xml"), "required");
     }
 
     @Test
     public void required_error_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/error-003.xml"), "required");
     }
 
     @Test
     public void required_escape_markup_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/escape-markup-001.xml"), "required");
     }
 
     @Test
     public void required_evaluation_order_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/evaluation-order-001.xml"), "required");
     }
 
     @Test
     public void required_evaluation_order_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/evaluation-order-002.xml"), "required");
     }
 
     @Test
     public void required_evaluation_order_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/evaluation-order-003.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-001.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-002.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-003.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-004.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-005.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-006.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-007.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-008.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-009.xml"), "required");
     }
 
     @Test
     public void required_exclude_inline_prefixes_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/exclude-inline-prefixes-010.xml"), "required");
     }
 
     @Test
     public void required_fibonacci() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/fibonacci.xml"), "required");
     }
 
     @Test
     public void required_filter_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/filter-001.xml"), "required");
     }
 
     @Test
     public void required_filter_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/filter-002.xml"), "required");
     }
 
     @Test
     public void required_filter_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/filter-003.xml"), "required");
     }
 
     @Test
     public void required_for_each_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-001.xml"), "required");
     }
 
     @Test
     public void required_for_each_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-002.xml"), "required");
     }
 
     @Test
     public void required_for_each_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-003.xml"), "required");
     }
 
     @Test
     public void required_for_each_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-004.xml"), "required");
     }
 
     @Test
     public void required_for_each_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-005.xml"), "required");
     }
 
     @Test
     public void required_for_each_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-006.xml"), "required");
     }
 
     @Test
     public void required_for_each_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-007.xml"), "required");
     }
 
     @Test
     public void required_for_each_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-008.xml"), "required");
     }
 
     @Test
     public void required_for_each_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-009.xml"), "required");
     }
 
     @Test
     public void required_for_each_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-010.xml"), "required");
     }
 
     @Test
     public void required_for_each_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/for-each-011.xml"), "required");
     }
 
     @Test
     public void required_group_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/group-001.xml"), "required");
     }
 
     @Test
     public void required_group_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/group-002.xml"), "required");
     }
 
     @Test
     public void required_http_request_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-001.xml"), "required");
     }
 
     @Test
     public void required_http_request_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-002.xml"), "required");
     }
 
     @Test
     public void required_http_request_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-003.xml"), "required");
     }
 
     @Test
     public void required_http_request_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-004.xml"), "required");
     }
 
     @Test
     public void required_http_request_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-005.xml"), "required");
     }
 
     @Test
     public void required_http_request_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-006.xml"), "required");
     }
 
     @Test
     public void required_http_request_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-007.xml"), "required");
     }
 
     @Test
     public void required_http_request_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-008.xml"), "required");
     }
 
     @Test
     public void required_http_request_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-009.xml"), "required");
     }
 
     @Test
     public void required_http_request_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-010.xml"), "required");
     }
 
     @Test
     public void required_http_request_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-011.xml"), "required");
     }
 
     @Test
     public void required_http_request_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-012.xml"), "required");
     }
 
     @Test
     public void required_http_request_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-013.xml"), "required");
     }
 
     @Test
     public void required_http_request_014() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/http-request-014.xml"), "required");
     }
 
     @Test
     public void required_identity_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/identity-001.xml"), "required");
     }
 
     @Test
     public void required_identity_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/identity-002.xml"), "required");
     }
 
     @Test
     public void required_identity_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/identity-003.xml"), "required");
     }
 
     @Test
     public void required_identity_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/identity-004.xml"), "required");
     }
 
     @Test
     public void required_identity_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/identity-005.xml"), "required");
     }
 
     @Test
     public void required_import_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-001.xml"), "required");
     }
 
     @Test
     public void required_import_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-002.xml"), "required");
     }
 
     @Test
     public void required_import_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-003.xml"), "required");
     }
 
     @Test
     public void required_import_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-004.xml"), "required");
     }
 
     @Test
     public void required_import_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-005.xml"), "required");
     }
 
     @Test
     public void required_import_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-006.xml"), "required");
     }
 
     @Test
     public void required_import_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-007.xml"), "required");
     }
 
     @Test
     public void required_import_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-008.xml"), "required");
     }
 
     @Test
     public void required_import_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-009.xml"), "required");
     }
 
     @Test
     public void required_import_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/import-010.xml"), "required");
     }
 
     @Test
     public void required_input_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-001.xml"), "required");
     }
 
     @Test
     public void required_input_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-002.xml"), "required");
     }
 
     @Test
     public void required_input_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-003.xml"), "required");
     }
 
     @Test
     public void required_input_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-004.xml"), "required");
     }
 
     @Test
     public void required_input_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-005.xml"), "required");
     }
 
     @Test
     public void required_input_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-006.xml"), "required");
     }
 
     @Test
     public void required_input_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-007.xml"), "required");
     }
 
     @Test
     public void required_input_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-008.xml"), "required");
     }
 
     @Test
     public void required_input_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-009.xml"), "required");
     }
 
     @Test
     public void required_input_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-010.xml"), "required");
     }
 
     @Test
     public void required_input_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-011.xml"), "required");
     }
 
     @Test
     public void required_input_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/input-012.xml"), "required");
     }
 
     @Test
     public void required_insert_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-001.xml"), "required");
     }
 
     @Test
     public void required_insert_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-002.xml"), "required");
     }
 
     @Test
     public void required_insert_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-003.xml"), "required");
     }
 
     @Test
     public void required_insert_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-004.xml"), "required");
     }
 
     @Test
     public void required_insert_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-005.xml"), "required");
     }
 
     @Test
     public void required_insert_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-006.xml"), "required");
     }
 
     @Test
     public void required_insert_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-007.xml"), "required");
     }
 
     @Test
     public void required_insert_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-008.xml"), "required");
     }
 
     @Test
     public void required_insert_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-009.xml"), "required");
     }
 
     @Test
     public void required_insert_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/insert-010.xml"), "required");
     }
 
     @Test
     public void required_iteration_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/iteration-001.xml"), "required");
     }
 
     @Test
     public void required_iteration_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/iteration-002.xml"), "required");
     }
 
     @Test
     public void required_labelelements_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-001.xml"), "required");
     }
 
     @Test
     public void required_labelelements_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-002.xml"), "required");
     }
 
     @Test
     public void required_labelelements_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-003.xml"), "required");
     }
 
     @Test
     public void required_labelelements_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-004.xml"), "required");
     }
 
     @Test
     public void required_labelelements_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-005.xml"), "required");
     }
 
     @Test
     public void required_labelelements_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-006.xml"), "required");
     }
 
     @Test
     public void required_labelelements_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-007.xml"), "required");
     }
 
     @Test
     public void required_labelelements_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-008.xml"), "required");
     }
 
     @Test
     public void required_labelelements_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/labelelements-009.xml"), "required");
     }
 
     @Test
     public void required_load_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/load-001.xml"), "required");
     }
 
     @Test
     public void required_load_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/load-002.xml"), "required");
     }
 
     @Test
     public void required_load_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/load-003.xml"), "required");
     }
 
     @Test
     public void required_load_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/load-004.xml"), "required");
     }
 
     @Test
     public void required_log_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/log-001.xml"), "required");
     }
 
     @Test
     public void required_log_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/log-002.xml"), "required");
     }
 
     @Test
     public void required_make_absolute_uris_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/make-absolute-uris-001.xml"), "required");
     }
 
     @Test
     public void required_make_absolute_uris_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/make-absolute-uris-002.xml"), "required");
     }
 
     @Test
     public void required_make_absolute_uris_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/make-absolute-uris-003.xml"), "required");
     }
 
     @Test
     public void required_make_sequence() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/make-sequence.xml"), "required");
     }
 
     @Test
     public void required_multipart_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/multipart-001.xml"), "required");
     }
 
     @Test
     public void required_multipart_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/multipart-002.xml"), "required");
     }
 
     @Test
     public void required_multipart_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/multipart-003.xml"), "required");
     }
 
     @Test
     public void required_multipart_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/multipart-004.xml"), "required");
     }
 
     @Test
     public void required_multipart_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/multipart-005.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-001.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-002.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-003.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-004.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-005.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-006.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-007.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-008.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-009.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-010.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-011.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-012.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-013.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_014() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-014.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_015() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-015.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_016() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-016.xml"), "required");
     }
 
     @Test
     public void required_namespace_rename_017() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespace-rename-017.xml"), "required");
     }
 
     @Test
     public void required_namespaces_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-001.xml"), "required");
     }
 
     @Test
     public void required_namespaces_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-002.xml"), "required");
     }
 
     @Test
     public void required_namespaces_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-003.xml"), "required");
     }
 
     @Test
     public void required_namespaces_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-004.xml"), "required");
     }
 
     @Test
     public void required_namespaces_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-005.xml"), "required");
     }
 
     @Test
     public void required_namespaces_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/namespaces-006.xml"), "required");
     }
 
     @Test
     public void required_nested_pipeline_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/nested-pipeline-001.xml"), "required");
     }
 
     @Test
     public void required_option_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/option-001.xml"), "required");
     }
 
     @Test
     public void required_option_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/option-002.xml"), "required");
     }
 
     @Test
     public void required_option_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/option-004.xml"), "required");
     }
 
     @Test
     public void required_output_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/output-001.xml"), "required");
     }
 
     @Test
     public void required_output_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/output-002.xml"), "required");
     }
 
     @Test
     public void required_pack_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-001.xml"), "required");
     }
 
     @Test
     public void required_pack_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-002.xml"), "required");
     }
 
     @Test
     public void required_pack_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-003.xml"), "required");
     }
 
     @Test
     public void required_pack_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-004.xml"), "required");
     }
 
     @Test
     public void required_pack_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-005.xml"), "required");
     }
 
     @Test
     public void required_pack_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pack-006.xml"), "required");
     }
 
     @Test
     public void required_param_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/param-001.xml"), "required");
     }
 
     @Test
     public void required_param_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/param-002.xml"), "required");
     }
 
     @Test
     public void required_param_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/param-003.xml"), "required");
     }
 
     @Test
     public void required_param_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/param-004.xml"), "required");
     }
 
     @Test
     public void required_parameters_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/parameters-001.xml"), "required");
     }
 
     @Test
     public void required_parameters_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/parameters-002.xml"), "required");
     }
 
     @Test
     public void required_pipe_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pipe-001.xml"), "required");
     }
 
     @Test
     public void required_pipeinfo_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/pipeinfo-001.xml"), "required");
     }
 
     @Test
     public void required_preserve_base_uri_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/preserve-base-uri-001.xml"), "required");
     }
 
     @Test
     public void required_preserve_base_uri_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/preserve-base-uri-002.xml"), "required");
     }
 
     @Test
     public void required_rename_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-001.xml"), "required");
     }
 
     @Test
     public void required_rename_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-002.xml"), "required");
     }
 
     @Test
     public void required_rename_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-003.xml"), "required");
     }
 
     @Test
     public void required_rename_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-004.xml"), "required");
     }
 
     @Test
     public void required_rename_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-005.xml"), "required");
     }
 
     @Test
     public void required_rename_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-006.xml"), "required");
     }
 
     @Test
     public void required_rename_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/rename-007.xml"), "required");
     }
 
     @Test
     public void required_replace_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/replace-001.xml"), "required");
     }
 
     @Test
     public void required_resolve_uri_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/resolve-uri-001.xml"), "required");
     }
 
     @Test
     public void required_resolve_uri_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/resolve-uri-002.xml"), "required");
     }
 
     @Test
     public void required_set_attributes_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/set-attributes-001.xml"), "required");
     }
 
     @Test
     public void required_set_attributes_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/set-attributes-002.xml"), "required");
     }
 
     @Test
     public void required_sink_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/sink-001.xml"), "required");
     }
 
     @Test
     public void required_sink_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/sink-002.xml"), "required");
     }
 
     @Test
     public void required_sink_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/sink-003.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-001.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-002.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-003.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-004.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-005.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-006.xml"), "required");
     }
 
     @Test
     public void required_split_sequence_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/split-sequence-007.xml"), "required");
     }
 
     @Test
     public void required_step_available_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-001.xml"), "required");
     }
 
     @Test
     public void required_step_available_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-002.xml"), "required");
     }
 
     @Test
     public void required_step_available_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-003.xml"), "required");
     }
 
     @Test
     public void required_step_available_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-004.xml"), "required");
     }
 
     @Test
     public void required_step_available_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-005.xml"), "required");
     }
 
     @Test
     public void required_step_available_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/step-available-006.xml"), "required");
     }
 
     @Test
     public void required_store_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/store-001.xml"), "required");
     }
 
     @Test
     public void required_string_replace_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/string-replace-001.xml"), "required");
     }
 
     @Test
     public void required_string_replace_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/string-replace-002.xml"), "required");
     }
 
     @Test
     public void required_string_replace_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/string-replace-003.xml"), "required");
     }
 
     @Test
     public void required_string_replace_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/string-replace-004.xml"), "required");
     }
 
     @Test
     public void required_string_replace_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/string-replace-005.xml"), "required");
     }
 
     @Test
     public void required_system_property_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/system-property-001.xml"), "required");
     }
 
     @Test
     public void required_system_property_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/system-property-002.xml"), "required");
     }
 
     @Test
     public void required_try_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-001.xml"), "required");
     }
 
     @Test
     public void required_try_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-002.xml"), "required");
     }
 
     @Test
     public void required_try_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-003.xml"), "required");
     }
 
     @Test
     public void required_try_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-004.xml"), "required");
     }
 
     @Test
     public void required_try_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-005.xml"), "required");
     }
 
     @Test
     public void required_try_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/try-006.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-001.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-002.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-003.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-004.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-005.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-006.xml"), "required");
     }
 
     @Test
     public void required_unescapemarkup_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unescapemarkup-007.xml"), "required");
     }
 
     @Test
     public void required_unwrap_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unwrap-001.xml"), "required");
     }
 
     @Test
     public void required_unwrap_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/unwrap-002.xml"), "required");
     }
 
     @Test
     public void required_use_when_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/use-when-001.xml"), "required");
     }
 
     @Test
     public void required_use_when_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/use-when-002.xml"), "required");
     }
 
     @Test
     public void required_use_when_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/use-when-003.xml"), "required");
     }
 
     @Test
     public void required_use_when_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/use-when-004.xml"), "required");
     }
 
     @Test
     public void required_value_available_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-001.xml"), "required");
     }
 
     @Test
     public void required_value_available_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-002.xml"), "required");
     }
 
     @Test
     public void required_value_available_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-003.xml"), "required");
     }
 
     @Test
     public void required_value_available_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-004.xml"), "required");
     }
 
     @Test
     public void required_value_available_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-005.xml"), "required");
     }
 
     @Test
     public void required_value_available_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/value-available-006.xml"), "required");
     }
 
     @Test
     public void required_variable_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-001.xml"), "required");
     }
 
     @Test
     public void required_variable_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-002.xml"), "required");
     }
 
     @Test
     public void required_variable_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-003.xml"), "required");
     }
 
     @Test
     public void required_variable_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-004.xml"), "required");
     }
 
     @Test
     public void required_variable_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-005.xml"), "required");
     }
 
     @Test
     public void required_variable_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/variable-006.xml"), "required");
     }
 
     @Test
     public void required_version_available_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/version-available-001.xml"), "required");
     }
 
     @Test
     public void required_version_available_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/version-available-002.xml"), "required");
     }
 
     @Test
     public void required_versioning_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-001.xml"), "required");
     }
 
     @Test
     public void required_versioning_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-002.xml"), "required");
     }
 
     @Test
     public void required_versioning_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-003.xml"), "required");
     }
 
     @Test
     public void required_versioning_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-004.xml"), "required");
     }
 
     @Test
     public void required_versioning_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-005.xml"), "required");
     }
 
     @Test
     public void required_versioning_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-006.xml"), "required");
     }
 
     @Test
     public void required_versioning_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/versioning-007.xml"), "required");
     }
 
     @Test
     public void required_viewport_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-001.xml"), "required");
     }
 
     @Test
     public void required_viewport_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-002.xml"), "required");
     }
 
     @Test
     public void required_viewport_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-003.xml"), "required");
     }
 
     @Test
     public void required_viewport_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-004.xml"), "required");
     }
 
     @Test
     public void required_viewport_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-005.xml"), "required");
     }
 
     @Test
     public void required_viewport_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-006.xml"), "required");
     }
 
     @Test
     public void required_viewport_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-007.xml"), "required");
     }
 
     @Test
     public void required_viewport_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-008.xml"), "required");
     }
 
     @Test
     public void required_viewport_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-009.xml"), "required");
     }
 
     @Test
     public void required_viewport_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-010.xml"), "required");
     }
 
     @Test
     public void required_viewport_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/viewport-011.xml"), "required");
     }
 
     @Test
     public void required_wrap_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-001.xml"), "required");
     }
 
     @Test
     public void required_wrap_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-002.xml"), "required");
     }
 
     @Test
     public void required_wrap_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-003.xml"), "required");
     }
 
     @Test
     public void required_wrap_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-004.xml"), "required");
     }
 
     @Test
     public void required_wrap_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-005.xml"), "required");
     }
 
     @Test
     public void required_wrap_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-006.xml"), "required");
     }
 
     @Test
     public void required_wrap_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-007.xml"), "required");
     }
 
     @Test
     public void required_wrap_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-008.xml"), "required");
     }
 
     @Test
     public void required_wrap_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-009.xml"), "required");
     }
 
     @Test
     public void required_wrap_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-010.xml"), "required");
     }
 
     @Test
     public void required_wrap_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-011.xml"), "required");
     }
 
     @Test
     public void required_wrap_sequence_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-sequence-001.xml"), "required");
     }
 
     @Test
     public void required_wrap_sequence_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-sequence-002.xml"), "required");
     }
 
     @Test
     public void required_wrap_sequence_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-sequence-003.xml"), "required");
     }
 
     @Test
     public void required_wrap_sequence_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-sequence-004.xml"), "required");
     }
 
     @Test
     public void required_wrap_sequence_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/wrap-sequence-005.xml"), "required");
     }
 
     @Test
     public void required_xinclude_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xinclude-001.xml"), "required");
     }
 
     @Test
     public void required_xinclude_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xinclude-002.xml"), "required");
     }
 
     @Test
     public void required_xinclude_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xinclude-003.xml"), "required");
     }
 
     @Test
     public void required_xinclude_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xinclude-004.xml"), "required");
     }
 
     @Test
     public void required_xinclude_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xinclude-005.xml"), "required");
     }
 
     @Test
     public void required_xml_id_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xml-id-001.xml"), "required");
     }
 
     @Test
     public void required_xml_id_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xml-id-002.xml"), "required");
     }
 
     @Test
     public void required_xpath_version_available_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xpath-version-available-001.xml"), "required");
     }
 
     @Test
     public void required_xpath_version_available_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xpath-version-available-002.xml"), "required");
     }
 
     @Test
     public void required_xslt_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-001.xml"), "required");
     }
 
     @Test
     public void required_xslt_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-002.xml"), "required");
     }
 
     @Test
     public void required_xslt_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-003.xml"), "required");
     }
 
     @Test
     public void required_xslt_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-004.xml"), "required");
     }
 
     @Test
     public void required_xslt_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-005.xml"), "required");
     }
 
     @Test
     public void required_xslt_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/required/xslt-006.xml"), "required");
     }
 
     @Test
     public void optional_document_template_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-001.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-002.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-003.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-004.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-005.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-006.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-007.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-008.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-009.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-010.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-011.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-012.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-013.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_014() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-014.xml"), "optional");
     }
 
     @Test
     public void optional_document_template_015() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/document-template-015.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0033_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0033-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0034_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0034-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0035_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0035-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0035_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0035-002.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0036_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0036-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0036_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0036-002.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0036_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0036-003.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0036_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0036-004.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0036_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0036-005.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0037_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0037-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0037_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0037-002.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0037_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0037-003.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0053_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0053-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0053_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0053-002.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0053_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0053-003.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0053_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0053-004.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0054_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0054-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0057_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0057-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0060_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0060-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0061_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0061-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0063_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0063-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0063_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0063-002.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0064_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0064-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0066_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0066-001.xml"), "optional");
     }
 
     @Test
     public void optional_err_c0066_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/err-c0066-002.xml"), "optional");
     }
 
     @Test
     public void optional_exec_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-001.xml"), "optional");
     }
 
     @Test
     public void optional_exec_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-002.xml"), "optional");
     }
 
     @Test
     public void optional_exec_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-003.xml"), "optional");
     }
 
     @Test
     public void optional_exec_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-004.xml"), "optional");
     }
 
     @Test
     public void optional_exec_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-005.xml"), "optional");
     }
 
     @Test
     public void optional_exec_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-006.xml"), "optional");
     }
 
     @Test
     public void optional_exec_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-007.xml"), "optional");
     }
 
     @Test
     public void optional_exec_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-008.xml"), "optional");
     }
 
     @Test
     public void optional_exec_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-009.xml"), "optional");
     }
 
     @Test
     public void optional_exec_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-010.xml"), "optional");
     }
 
     @Test
     public void optional_exec_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-011.xml"), "optional");
     }
 
     @Test
     public void optional_exec_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-012.xml"), "optional");
     }
 
     @Test
     public void optional_exec_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-013.xml"), "optional");
     }
 
     @Test
     public void optional_exec_014() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-014.xml"), "optional");
     }
 
     @Test
     public void optional_exec_015() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-015.xml"), "optional");
     }
 
     @Test
     public void optional_exec_016() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-016.xml"), "optional");
     }
 
     @Test
     public void optional_exec_017() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/exec-017.xml"), "optional");
     }
 
     @Test
     public void optional_hash_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-001.xml"), "optional");
     }
 
     @Test
     public void optional_hash_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-002.xml"), "optional");
     }
 
     @Test
     public void optional_hash_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-003.xml"), "optional");
     }
 
     @Test
     public void optional_hash_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-004.xml"), "optional");
     }
 
     @Test
     public void optional_hash_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-005.xml"), "optional");
     }
 
     @Test
     public void optional_hash_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/hash-006.xml"), "optional");
     }
 
     @Test
     public void optional_in_scope_names_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/in-scope-names-001.xml"), "optional");
     }
 
     @Test
     public void optional_in_scope_names_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/in-scope-names-002.xml"), "optional");
     }
 
     @Test
     public void optional_psvi_required_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/psvi-required-001.xml"), "optional");
     }
 
     @Test
     public void optional_uuid_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/uuid-001.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-001.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-002.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-003.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-004.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-005.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-006.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-008.xml"), "optional");
     }
 
     @Test
     public void optional_validrng_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validrng-009.xml"), "optional");
     }
 
     @Test
     public void optional_validsch_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validsch-001.xml"), "optional");
     }
 
     @Test
     public void optional_validsch_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validsch-002.xml"), "optional");
     }
 
     @Test
     public void optional_validsch_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validsch-003.xml"), "optional");
     }
 
     @Test
     public void optional_validsch_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validsch-004.xml"), "optional");
     }
 
     @Test
     public void optional_validsch_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validsch-005.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-001.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-002.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-003.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-004.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-005.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-006.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_007() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-007.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_008() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-008.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_009() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-009.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_010() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-010.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_011() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-011.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_012() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-012.xml"), "optional");
     }
 
     @Test
     public void optional_validxsd_013() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/validxsd-013.xml"), "optional");
     }
 
     @Test
     public void optional_www_form_urldecode_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/www-form-urldecode-001.xml"), "optional");
     }
 
     @Test
     public void optional_www_form_urlencode_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/www-form-urlencode-001.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-001.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-002.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-003.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-004.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-005.xml"), "optional");
     }
 
     @Test
     public void optional_xinclude_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xinclude-006.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-001.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-002.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-003.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_004() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-004.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_005() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-005.xml"), "optional");
     }
 
     @Test
     public void optional_xquery_006() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xquery-006.xml"), "optional");
     }
 
     @Test
     public void optional_xsl_formatter_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xsl-formatter-001.xml"), "optional");
     }
 
     @Test
     public void optional_xslt2_001() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xslt2-001.xml"), "optional");
     }
 
     @Test
     public void optional_xslt2_002() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xslt2-002.xml"), "optional");
     }
 
     @Test
     public void optional_xslt2_003() throws Exception
     {
         test(new URL("http://tests.xproc.org/tests/optional/xslt2-003.xml"), "optional");
     }
 }
