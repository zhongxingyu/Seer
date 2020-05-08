 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caIntegrator2
  * Software was developed in conjunction with the National Cancer Institute 
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent 
  * government employees are authors, any rights in such works shall be subject 
  * to Title 17 of the United States Code, section 105. 
  *
  * This caIntegrator2 Software License (the License) is between NCI and You. You (or 
  * Your) shall mean a person or an entity, and all other entities that control, 
  * are controlled by, or are under common control with the entity. Control for 
  * purposes of this definition means (i) the direct or indirect power to cause 
  * the direction or management of such entity, whether by contract or otherwise,
  * or (ii) ownership of fifty percent (50%) or more of the outstanding shares, 
  * or (iii) beneficial ownership of such entity. 
  *
  * This License is granted provided that You agree to the conditions described 
  * below. NCI grants You a non-exclusive, worldwide, perpetual, fully-paid-up, 
  * no-charge, irrevocable, transferable and royalty-free right and license in 
  * its rights in the caIntegrator2 Software to (i) use, install, access, operate, 
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caIntegrator2 Software; (ii) distribute and 
  * have distributed to and by third parties the caIntegrator2 Software and any 
  * modifications and derivative works thereof; and (iii) sublicense the 
  * foregoing rights set out in (i) and (ii) to third parties, including the 
  * right to license such rights to further third parties. For sake of clarity, 
  * and not by way of limitation, NCI shall have no right of accounting or right 
  * of payment from You or Your sub-licensees for the rights granted under this 
  * License. This License is granted at no charge to You.
  *
  * Your redistributions of the source code for the Software must retain the 
  * above copyright notice, this list of conditions and the disclaimer and 
  * limitation of liability of Article 6, below. Your redistributions in object 
  * code form must reproduce the above copyright notice, this list of conditions 
  * and the disclaimer of Article 6 in the documentation and/or other materials 
  * provided with the distribution, if any. 
  *
  * Your end-user documentation included with the redistribution, if any, must 
  * include the following acknowledgment: This product includes software 
  * developed by 5AM, ScenPro, SAIC and the National Cancer Institute. If You do 
  * not include such end-user documentation, You shall include this acknowledgment 
  * in the Software itself, wherever such third-party acknowledgments normally 
  * appear.
  *
  * You may not use the names "The National Cancer Institute", "NCI", "ScenPro",
  * "SAIC" or "5AM" to endorse or promote products derived from this Software. 
  * This License does not authorize You to use any trademarks, service marks, 
  * trade names, logos or product names of either NCI, ScenPro, SAID or 5AM, 
  * except as required to comply with the terms of this License. 
  *
  * For sake of clarity, and not by way of limitation, You may incorporate this 
  * Software into Your proprietary programs and into any third party proprietary 
  * programs. However, if You incorporate the Software into third party 
  * proprietary programs, You agree that You are solely responsible for obtaining
  * any permission from such third parties required to incorporate the Software 
  * into such third party proprietary programs and for informing Your a
  * sub-licensees, including without limitation Your end-users, of their 
  * obligation to secure any required permissions from such third parties before 
  * incorporating the Software into such third party proprietary software 
  * programs. In the event that You fail to obtain such permissions, You agree 
  * to indemnify NCI for any claims against NCI by such third parties, except to 
  * the extent prohibited by law, resulting from Your failure to obtain such 
  * permissions. 
  *
  * For sake of clarity, and not by way of limitation, You may add Your own 
  * copyright statement to Your modifications and to the derivative works, and 
  * You may provide additional or different license terms and conditions in Your 
  * sublicenses of modifications of the Software, or any derivative works of the 
  * Software as a whole, provided Your use, reproduction, and distribution of the
  * Work otherwise complies with the conditions stated in this License.
  *
  * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
  * (INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY, 
  * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO 
  * EVENT SHALL THE NATIONAL CANCER INSTITUTE, 5AM SOLUTIONS, INC., SCENPRO, INC.,
  * SCIENCE APPLICATIONS INTERNATIONAL CORPORATION OR THEIR 
  * AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package gov.nih.nci.caintegrator2.application.study;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 
 public class AnnotationFileTest {
 
     private static final String VALID_FILE = "/csvtestclinical.csv";
     private static final String VALID_FILE_TIMEPOINT = "/csvtestclinical-timepoint.csv";
     private static final String INVALID_FILE_MISSING_VALUE = "/csvtestclinical-missing-value.csv";
     private static final String INVALID_FILE_EMPTY = "/emptyfile.txt";
     private static final String INVALID_FILE_NO_DATA = "/csvtestclinical-no-data.csv";
    private static final String INVALID_FILE_DOESNT_EXIST = "nofile.txt";
     
     private List<AnnotationFieldDescriptor> testAnnotationFieldDescriptors;
     
     @Before
     public void setUp() throws Exception {
         setupTestAnnotations();
     }
     
     private AnnotationFile createAnnotationFile(String filePath) throws ValidationException {
         return createAnnotationFile(getFile(filePath));
     }
     
     private File getFile(String filePath) {
         return new File(AnnotationFileTest.class.getResource(filePath).getFile());
     }
 
     private AnnotationFile createAnnotationFile(File file) throws ValidationException {
         return AnnotationFile.load(file);
     }
 
     /**
      * Test method for {@link gov.nih.nci.caintegrator2.application.study.DelimitedTextannotationFile#validateFile()}.
      * @throws ValidationException 
      */
     @Test
     public void testLoad() throws ValidationException {
         AnnotationFile annotationFile = createAnnotationFile(VALID_FILE);
         assertNotNull(annotationFile);
         assertEquals(4, annotationFile.getColumns().size());
         assertEquals("ID", annotationFile.getColumns().get(0).getName());
         assertEquals("Col1", annotationFile.getColumns().get(1).getName());
         assertEquals("Col2", annotationFile.getColumns().get(2).getName());
         assertEquals("Col3", annotationFile.getColumns().get(3).getName());
         checkInvalid(getFile(INVALID_FILE_MISSING_VALUE), "Number of values inconsistent with header line.");
         checkInvalid(getFile(INVALID_FILE_EMPTY), "The data file was empty.");
         checkInvalid(getFile(INVALID_FILE_NO_DATA), "The data file contained no data (header line only).");
        checkInvalid(new File(INVALID_FILE_DOESNT_EXIST), "The file nofile.txt could not be found");
     }
 
     private void checkInvalid(File file, String expectedMessage) {
         try {
             createAnnotationFile(file);
             fail("ValidationException expected");
         } catch (ValidationException e) {
             assertEquals(expectedMessage, e.getResult().getInvalidMessage());
         }
     }
 
     @Test
     public void testGetDescriptors() throws ValidationException {
         AnnotationFile annotationFile = createAnnotationFile(VALID_FILE_TIMEPOINT);
         annotationFile.setIdentifierColumn(annotationFile.getColumns().get(0));
         annotationFile.setTimepointColumn(annotationFile.getColumns().get(1));
         List<AnnotationFieldDescriptor> emptyList = Collections.emptyList();
         annotationFile.loadDescriptors(emptyList);
         validateAnnotationFieldDescriptor(testAnnotationFieldDescriptors, annotationFile.getDescriptors());
     }
 
     @Test
     public void testPositionAtData() throws ValidationException {
         AnnotationFile annotationFile = createAnnotationFile(VALID_FILE);
         assertTrue(annotationFile.hasNextDataLine());
         assertEquals("100", annotationFile.getDataValue(annotationFile.getColumns().get(0)));
         assertTrue(annotationFile.hasNextDataLine());
         assertEquals("101", annotationFile.getDataValue(annotationFile.getColumns().get(0)));
         assertFalse(annotationFile.hasNextDataLine());
         annotationFile.positionAtData();
         assertTrue(annotationFile.hasNextDataLine());
         assertEquals("100", annotationFile.getDataValue(annotationFile.getColumns().get(0)));
     }
 
     @Test
     public void testDataValue() throws ValidationException {
         AnnotationFile annotationFile = createAnnotationFile(VALID_FILE);
         annotationFile.setIdentifierColumn(annotationFile.getColumns().get(0));
         annotationFile.positionAtData();
         annotationFile.loadDescriptors(testAnnotationFieldDescriptors);
         
         assertTrue(annotationFile.hasNextDataLine());
         assertEquals("100", annotationFile.getDataValue(annotationFile.getIdentifierColumn()));
         assertEquals("1", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(0)));
         assertEquals("g", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(1)));
         assertEquals("N", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(2)));
         assertEquals("1", annotationFile.getDataValue(annotationFile.getColumns().get(1)));
         assertEquals("g", annotationFile.getDataValue(annotationFile.getColumns().get(2)));
         assertEquals("N", annotationFile.getDataValue(annotationFile.getColumns().get(3)));
         
         assertTrue(annotationFile.hasNextDataLine());
         assertEquals("101", annotationFile.getDataValue(annotationFile.getIdentifierColumn()));
         assertEquals("3", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(0)));
         assertEquals("g", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(1)));
         assertEquals("Y", annotationFile.getDataValue(testAnnotationFieldDescriptors.get(2)));
         assertEquals("3", annotationFile.getDataValue(annotationFile.getColumns().get(1)));
         assertEquals("g", annotationFile.getDataValue(annotationFile.getColumns().get(2)));
         assertEquals("Y", annotationFile.getDataValue(annotationFile.getColumns().get(3)));
         
         assertFalse(annotationFile.hasNextDataLine());
     }
 
     private void setupTestAnnotations() {
         testAnnotationFieldDescriptors = new ArrayList<AnnotationFieldDescriptor>();
         
         AnnotationFieldDescriptor testAnnotationFieldDescriptor = new AnnotationFieldDescriptor();
         testAnnotationFieldDescriptor.setName("Col1");
         testAnnotationFieldDescriptors.add(testAnnotationFieldDescriptor);
         
         testAnnotationFieldDescriptor = new AnnotationFieldDescriptor();
         testAnnotationFieldDescriptor.setName("Col2");
         testAnnotationFieldDescriptors.add(testAnnotationFieldDescriptor);
         
         testAnnotationFieldDescriptor = new AnnotationFieldDescriptor();
         testAnnotationFieldDescriptor.setName("Col3");
         testAnnotationFieldDescriptors.add(testAnnotationFieldDescriptor);
     }
     
     
     private void validateAnnotationFieldDescriptor(
                 List<AnnotationFieldDescriptor> testAnnotations, 
                 List<AnnotationFieldDescriptor> realAnnotations) {
         for (int x = 0; x < testAnnotations.size(); x++) {
             AnnotationFieldDescriptor testAnnotationDescriptor = testAnnotations.get(x);
             AnnotationFieldDescriptor realAnnotationDescriptor = realAnnotations.get(x);
             assertEquals(testAnnotationDescriptor.getName(), realAnnotationDescriptor.getName());
         }
     }
     
 }
