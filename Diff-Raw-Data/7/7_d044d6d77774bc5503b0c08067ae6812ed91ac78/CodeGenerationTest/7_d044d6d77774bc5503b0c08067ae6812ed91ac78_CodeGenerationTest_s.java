 package com.coremedia.beanmodeller.tests;
 
 import com.coremedia.beanmodeller.processors.ContentBeanAnalyzationException;
 import com.coremedia.beanmodeller.processors.ContentBeanAnalyzerException;
 import com.coremedia.beanmodeller.processors.ContentBeanInformation;
 import com.coremedia.beanmodeller.processors.analyzator.ContentBeanAnalyzator;
 import com.coremedia.beanmodeller.processors.codegenerator.ContentBeanCodeGenerator;
 import com.coremedia.beanmodeller.testcontentbeans.testmodel.CBGAppointment;
 import com.coremedia.beanmodeller.testcontentbeans.testmodel.CBGAttendee;
 import com.coremedia.beanmodeller.testcontentbeans.testmodel.CBGContent;
 import com.sun.codemodel.CodeWriter;
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.writer.SingleStreamCodeWriter;
 import org.junit.Before;
 
 import java.io.IOException;
 import java.util.Set;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 /**
  * Telekom .COM Relaunch 2011
  * User: marcus
  * Date: 02.02.11
  * Time: 11:25
  */
 public class CodeGenerationTest {
 
   private ContentBeanAnalyzator analyzator;
   private ContentBeanCodeGenerator codegenerator;
 
   @Before
   public void setup() throws ContentBeanAnalyzationException {
     analyzator = new ContentBeanAnalyzator();
     analyzator.addContentBean(CBGAppointment.class);
     analyzator.addContentBean(CBGAttendee.class);
     analyzator.addContentBean(CBGContent.class);
     analyzator.analyzeContentBeanInformation();
 
     codegenerator = new ContentBeanCodeGenerator();
     codegenerator.setPackageName("test.code");
   }
 
 //  @Test doesn't succeed in Idea, but hangs in Status "terminated" and prevents further test execution.
 
   public void printGeneratedCode() throws ContentBeanAnalyzerException, IOException {
     Set<ContentBeanInformation> roots = analyzator.getContentBeanRoots();
     assertNotNull(roots);
     assertFalse(roots.isEmpty());
     JCodeModel code = codegenerator.generateCode(roots);
     CodeWriter output = new SingleStreamCodeWriter(System.out);
     code.build(output);
 
     //this is a manual test only
     //if we would like to automate it we would have to check the source or compile and check the classes
     //too complicated for now
   }
 }
