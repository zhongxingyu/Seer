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
 package gov.nih.nci.caintegrator2.web.action.analysis;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.AcegiAuthenticationStub;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisParameter;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisParameterType;
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisServiceStub;
 import gov.nih.nci.caintegrator2.application.analysis.GenomicDataParameterValue;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementServiceStub;
import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceServiceStub;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.web.SessionHelper;
 import gov.nih.nci.caintegrator2.web.ajax.GenePatternAjaxUpdater;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.acegisecurity.context.SecurityContextHolder;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 public class GenePatternAnalysisActionTest {
     
     private GenePatternAnalysisAction action;
 
 
     @Before
     public void setUp() {
         SecurityContextHolder.getContext().setAuthentication(new AcegiAuthenticationStub());
         ActionContext.getContext().setSession(new HashMap<String, Object>());
         StudySubscription subscription = new StudySubscription();
         Study study = new Study();
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.setStatus(Status.DEPLOYED);
         study.setStudyConfiguration(studyConfiguration);
         subscription.setStudy(study);
         subscription.setQueryCollection(new HashSet<Query>());
         SessionHelper.getInstance().getDisplayableUserWorkspace().setCurrentStudySubscription(subscription);
         ActionContext.getContext().getValueStack().setValue("studySubscription", subscription);
         action = new GenePatternAnalysisAction();
         action.setAnalysisService(new AnalysisServiceStub());
         action.setQueryManagementService(new QueryManagementServiceStub());
         action.setWorkspaceService(new WorkspaceServiceStub());
         action.setAjaxUpdater(new GenePatternAjaxUpdater());
     }
     
     @Test
     public void testOpen() {
         action.setSelectedAction(GenePatternAnalysisAction.OPEN_ACTION);
         assertEquals(ActionSupport.SUCCESS, action.execute());
     }
     
     @Test
     public void testConnect() {
         action.setSelectedAction(GenePatternAnalysisAction.CONNECT_ACTION);
         assertEquals(ActionSupport.SUCCESS, action.execute());
         assertTrue(action.getGenePatternAnalysisForm().getAnalysisMethods().isEmpty());
         action.getGenePatternAnalysisForm().setUrl("url");
         action.execute();
         assertNotNull(action.getGenePatternAnalysisForm().getAnalysisMethods());
         assertEquals(1, action.getGenePatternAnalysisForm().getAnalysisMethods().size());
     }
 
     @Test
     public void testSetAnalysisMethodName() {
         action.setSelectedAction(GenePatternAnalysisAction.CONNECT_ACTION);
         assertNull(action.getGenePatternAnalysisForm().getInvocation());
         action.getGenePatternAnalysisForm().setUrl("url");
         assertEquals("url", action.getGenePatternAnalysisForm().getUrl());
         action.execute();
         action.setSelectedAction(GenePatternAnalysisAction.CHANGE_METHOD_ACTION);
         assertEquals(1, action.getGenePatternAnalysisForm().getAnalysisMethodNames().size());
         action.setAnalysisMethodName("method");
         action.execute();
         assertNotNull(action.getGenePatternAnalysisForm().getInvocation());
         assertEquals("method", action.getGenePatternAnalysisForm().getAnalysisMethodName());
         assertEquals(1, action.getGenePatternAnalysisForm().getParameters().size());
         AbstractAnalysisFormParameter parameter = action.getGenePatternAnalysisForm().getParameters().get(0);
         assertEquals("parameter", parameter.getName());
         assertTrue(parameter.isRequired());
         assertTrue(action.getGenePatternAnalysisForm().isExecutable());
         action.setAnalysisMethodName("");
         action.execute();
         assertNull(action.getGenePatternAnalysisForm().getInvocation());
         assertFalse(action.getGenePatternAnalysisForm().isExecutable());
     }
     
     @Test
     public void testExecute() {
         action.setSelectedAction(GenePatternAnalysisAction.CONNECT_ACTION);
         action.getGenePatternAnalysisForm().setUrl("url");
         action.execute();
         action.setSelectedAction(GenePatternAnalysisAction.CHANGE_METHOD_ACTION);
         action.setAnalysisMethodName("method");
         action.execute();
         GenomicDataParameterValue genomicParameterValue = new GenomicDataParameterValue();
         AnalysisParameter genomicParameter = new AnalysisParameter();
         genomicParameter.setType(AnalysisParameterType.GENOMIC_DATA);
         genomicParameterValue.setParameter(genomicParameter);
         action.getGenePatternAnalysisForm().getInvocation().setParameterValue(genomicParameter, genomicParameterValue);
         action.setSelectedAction(GenePatternAnalysisAction.EXECUTE_ACTION);
         assertEquals("status", action.execute());
     }
     
     @Test
     public void testValidate() {
         action.setSelectedAction(GenePatternAnalysisAction.CONNECT_ACTION);
         action.getGenePatternAnalysisForm().setUrl("");
         action.getGenePatternAnalysisForm().setUsername("username");
         action.validate();
         assertTrue(action.hasErrors());
         action.clearErrorsAndMessages();
         action.getGenePatternAnalysisForm().setUrl("bad url");
         action.validate();
         assertTrue(action.hasErrors());
         action.clearErrorsAndMessages();
         action.getGenePatternAnalysisForm().setUrl("http://localhost/directory");
         action.getCurrentGenePatternAnalysisJob().setName("name");
         action.validate();
        assertTrue(action.hasErrors());
        action.clearErrorsAndMessages();
        action.getCurrentStudy().getStudyConfiguration().getGenomicDataSources().add(new GenomicDataSourceConfiguration());
        action.validate();
         assertFalse(action.hasErrors());
         action.clearErrorsAndMessages();
         action.execute();
         action.setSelectedAction(GenePatternAnalysisAction.CHANGE_METHOD_ACTION);
         action.setAnalysisMethodName("method");
         action.execute();
         action.setSelectedAction(GenePatternAnalysisAction.EXECUTE_ACTION);
         action.getGenePatternAnalysisForm().getParameters().get(0).setValue("");
         action.validate();
         assertTrue(action.hasErrors());
         action.clearErrorsAndMessages();
         action.getGenePatternAnalysisForm().getParameters().get(0).setValue("value");
         action.validate();
         assertFalse(action.hasErrors());
         action.getGenePatternAnalysisForm().getParameters().get(0).getParameterValue().getParameter().setType(AnalysisParameterType.INTEGER);
         action.validate();
         assertTrue(action.hasErrors());
         action.clearErrorsAndMessages();
         action.getGenePatternAnalysisForm().getParameters().get(0).getParameterValue().getParameter().setType(AnalysisParameterType.FLOAT);
         action.validate();
         assertTrue(action.hasErrors());
         action.clearErrorsAndMessages();
         action.getCurrentStudy().getStudyConfiguration().setStatus(Status.NOT_DEPLOYED);
         action.validate();
         assertTrue(action.getActionErrors().size() == 1);
         action.clearErrorsAndMessages();
         ActionContext.getContext().getValueStack().setValue("studySubscription", null);
         action.validate();
         assertTrue(action.getActionErrors().size() == 1);
     }
 
 }
