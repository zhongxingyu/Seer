 /**
  * The software subject to this notice and license includes both human readable
  * source code form and machine readable, binary, object code form. The caArray
  * Software was developed in conjunction with the National Cancer Institute
  * (NCI) by NCI employees, 5AM Solutions, Inc. (5AM), ScenPro, Inc. (ScenPro)
  * and Science Applications International Corporation (SAIC). To the extent
  * government employees are authors, any rights in such works shall be subject
  * to Title 17 of the United States Code, section 105.
  *
  * This caArray Software License (the License) is between NCI and You. You (or
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
  * its rights in the caArray Software to (i) use, install, access, operate,
  * execute, copy, modify, translate, market, publicly display, publicly perform,
  * and prepare derivative works of the caArray Software; (ii) distribute and
  * have distributed to and by third parties the caIntegrator Software and any
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
 package gov.nih.nci.caintegrator2.web.ajax;
 
 import gov.nih.nci.caintegrator2.application.analysis.heatmap.HeatmapParameters;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.deployment.DeploymentService;
 import gov.nih.nci.caintegrator2.web.DisplayableUserWorkspace;
 import gov.nih.nci.caintegrator2.web.SessionHelper;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.xwork.BooleanUtils;
 import org.directwebremoting.proxy.dwr.Util;
 
 /**
  * This is an object which is turned into an AJAX javascript file using the DWR framework.
  */
 public class StudyDeploymentAjaxUpdater extends AbstractDwrAjaxUpdater
     implements IStudyDeploymentAjaxUpdater {
 
     private static final String STATUS_TABLE = "studyDeploymentJobStatusTable";
     private static final String JOB_STUDY_NAME = "studyName_";
     private static final String JOB_STUDY_DESCRIPTION = "studyDescription_";
     private static final String JOB_STUDY_STATUS = "studyStatus_";
     private static final String JOB_LAST_MODIFIED_BY = "studyLastModified_";
     private static final String JOB_EDIT_STUDY_URL = "studyJobEditUrl_";
     private static final String JOB_COPY_STUDY_URL = "studyJobCopyUrl_";
     private static final String JOB_ARCHIVE_STUDY_URL = "studyJobArchiveUrl_";
     private static final String JOB_DELETE_STUDY_URL = "studyJobDeleteUrl_";
     private static final String JOB_START_DATE = "studyJobStartDate_";
     private static final String JOB_FINISH_DATE = "studyJobFinishDate_";
     private static final String JOB_NEW_DATA_AVAILABLE = "studyNewDataAvailable_";
     private static final String JOB_ACTION_BAR = "jobActionBar_";
 
     private DeploymentService deploymentService;
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void initializeDynamicTable(DisplayableUserWorkspace workspace) {
         int counter = 0;
         List <StudyConfiguration> studyConfigurationList = new ArrayList<StudyConfiguration>();
         studyConfigurationList.addAll(workspace.getManagedStudies());
         Comparator<StudyConfiguration> nameComparator = new Comparator<StudyConfiguration>() {
             public int compare(StudyConfiguration configuration1, StudyConfiguration configuration2) {
                 return configuration1.getStudy().getShortTitleText().
                        compareToIgnoreCase(configuration2.getStudy().getShortTitleText());
             }
         };
         Collections.sort(studyConfigurationList, nameComparator);
         String username = workspace.getUserWorkspace().getUsername();
         for (StudyConfiguration studyConfiguration : studyConfigurationList) {
             getDwrUtil(username).addRows(STATUS_TABLE,
                                             createRow(studyConfiguration),
                                             retrieveRowOptions(counter));
             updateJobStatus(username, studyConfiguration);
             counter++;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected void associateJobWithSession(DwrUtilFactory dwrUtilFactory, String username, Util util) {
         dwrUtilFactory.associateStudyConfigurationJobWithSession(username, util);
     }
 
     private String[][] createRow(StudyConfiguration studyConfiguration) {
         String[][] rowString = new String[1][8];
         String id = studyConfiguration.getId().toString();
         String startSpan = "<span id=\"";
         String endSpan = "\"> </span>";
         rowString[0][0] = startSpan + JOB_STUDY_NAME + id + endSpan;
         rowString[0][1] = startSpan + JOB_STUDY_DESCRIPTION + id + endSpan;
         rowString[0][2] = startSpan + JOB_LAST_MODIFIED_BY + id + endSpan;
         rowString[0][3] = startSpan + JOB_STUDY_STATUS + id + endSpan;
         rowString[0][4] = startSpan + JOB_START_DATE + id + endSpan;
         rowString[0][5] = startSpan + JOB_FINISH_DATE + id + endSpan;
         rowString[0][6] = startSpan + JOB_NEW_DATA_AVAILABLE + id + endSpan;
         rowString[0][7] = startSpan + JOB_EDIT_STUDY_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR + id + "e" + endSpan
                           + startSpan + JOB_COPY_STUDY_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR + id + "c" + endSpan
                           + startSpan + JOB_ARCHIVE_STUDY_URL + id + endSpan
                           + startSpan + JOB_ACTION_BAR + id + "a" + endSpan
                           + startSpan + JOB_DELETE_STUDY_URL + id + endSpan;
 
         return rowString;
     }
 
     /**
      * {@inheritDoc}
      */
     public void runJob(StudyConfiguration studyConfiguration, HeatmapParameters heatmapParameters) {
         Thread studyConfigurationRunner = new Thread(new StudyDeploymentAjaxRunner(
                 this, studyConfiguration, heatmapParameters));
         studyConfigurationRunner.start();
     }
 
     /**
      * Adds error to JSP.
      * @param errorMessage .
      * @param studyConfiguration to associate JSP script session to.
      */
     public void addError(String errorMessage, StudyConfiguration studyConfiguration) {
         getDwrUtil(studyConfiguration.getUserWorkspace().getUsername()).setValue("errorMessages", errorMessage);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected Util getDwrUtil(String username) {
         return getDwrUtilFactory().retrieveStudyConfigurationUtil(username);
     }
 
     /**
      * Updates studyConfiguration status.
      * @param username to update the status to.
      * @param studyConfiguration to update.
      */
     public void updateJobStatus(String username, StudyConfiguration studyConfiguration) {
         Util utilThis = getDwrUtil(username);
         String studyConfigurationId = studyConfiguration.getId().toString();
         String descriptionDivId = JOB_STUDY_DESCRIPTION + "div_" + studyConfigurationId;
         utilThis.setValue(JOB_STUDY_NAME + studyConfigurationId,
                 studyConfiguration.getStudy().getShortTitleText());
         utilThis.setValue(JOB_STUDY_DESCRIPTION + studyConfigurationId,
                 "<s:div id=\"" + descriptionDivId + "\">"
                 + studyConfiguration.getStudy().getLongTitleText() + "</s:div>");
         utilThis.setValue(JOB_LAST_MODIFIED_BY + studyConfigurationId,
                           studyConfiguration.getLastModifiedBy().getUsername());
         utilThis.setValue(JOB_STUDY_STATUS + studyConfigurationId,
                           getStatusMessage(studyConfiguration.getStatus()));
         utilThis.setValue(JOB_START_DATE + studyConfigurationId,
                           getDateString(studyConfiguration.getDeploymentStartDate()));
         utilThis.setValue(JOB_FINISH_DATE + studyConfigurationId,
                           getDateString(studyConfiguration.getDeploymentFinishDate()));
         utilThis.setValue(JOB_NEW_DATA_AVAILABLE + studyConfigurationId,
                 StringUtils.capitalize(BooleanUtils.toStringYesNo(studyConfiguration.isDataRefreshed())));
         updateRowActions(studyConfiguration, utilThis, studyConfigurationId);
         utilThis.addFunctionCall("truncateDescriptionDiv", descriptionDivId);
     }
 
     private void updateRowActions(StudyConfiguration studyConfiguration, Util utilThis, String studyConfigurationId) {
         if (!Status.PROCESSING.equals(studyConfiguration.getStatus())) {
             utilThis.setValue(JOB_EDIT_STUDY_URL + studyConfigurationId,
                     getEditStudyUrlString(studyConfiguration, "Edit", ""),
                     false);
             utilThis.setValue(JOB_ACTION_BAR + studyConfigurationId + "e", "&nbsp;|&nbsp;", false);
             utilThis.setValue(JOB_COPY_STUDY_URL + studyConfigurationId,
                     getCopyStudyUrlString(studyConfiguration),
                     false);
             utilThis.setValue(JOB_ACTION_BAR + studyConfigurationId + "c", "&nbsp;|&nbsp;", false);
             utilThis.setValue(JOB_ARCHIVE_STUDY_URL + studyConfigurationId,
                     getArchiveStudyUrlString(studyConfiguration),
                     false);
             utilThis.setValue(JOB_ACTION_BAR + studyConfigurationId + "a", "&nbsp;|&nbsp;", false);
 
             String deleteMsg = "The study: " + studyConfiguration.getStudy().getShortTitleText()
                 + " will be permanently deleted.";
             utilThis.setValue(JOB_DELETE_STUDY_URL + studyConfigurationId,
                     getDeleteStudyUrlString(studyConfiguration, deleteMsg),
                     false);
             addError(studyConfiguration, utilThis, studyConfigurationId);
         } else {
             utilThis.setValue(JOB_EDIT_STUDY_URL + studyConfigurationId, "");
             utilThis.setValue(JOB_ACTION_BAR + studyConfigurationId, "");
             utilThis.setValue(JOB_DELETE_STUDY_URL, "");
         }
     }
 
     private void addError(StudyConfiguration studyConfiguration, Util utilThis, String studyConfigurationId) {
             if (Status.ERROR.equals(studyConfiguration.getStatus())) {
                 utilThis.setValue(JOB_STUDY_STATUS + studyConfigurationId,
                     getEditStudyUrlString(studyConfiguration, "Error",
                             "Click to view the Error description in the Study Overview"),
                         false);
             }
     }
 
     /**
      * @param studyConfiguration
      * @param deleteMsg
      * @return the string which forms the html for the deleteStudy url.
      */
     private String getDeleteStudyUrlString(StudyConfiguration studyConfiguration, String deleteMsg) {
 
         String token = "", tokenName = "";
 
         try {
             token = SessionHelper.getInstance().getToken();
             tokenName = SessionHelper.getInstance().getTokenName();
         } catch (Exception e) { token = ""; }
 
        String returnString = "<a href=\"deleteStudy.action?studyConfiguration.id="
        + studyConfiguration.getId()
        + "&struts.token.name="
        + tokenName
        + "&struts.token="
        + token
        + "\" onclick=\"return confirm('" + deleteMsg + "')\">Delete</a>";
 
         return returnString;
     }
 
     /**
      * @param studyConfiguration
      * @return the string which forms the html for the editStudy url.
      */
     private String getEditStudyUrlString(StudyConfiguration studyConfiguration, String action, String title) {
         String returnString = null;
         if (studyConfiguration.getStudy().isEnabled()) {
             returnString = "<a href=\"editStudy.action?studyConfiguration.id="
            + studyConfiguration.getId()
            + "\" title=\"" + title + "\"";
         } else {
             returnString = "<a style=\"color:Grey; text-decoration:none;\""
             + " title=\"Edit Disabled - Enable Study to Edit\"";
         }
         return returnString + ">" + action + "</a>";
     }
 
     /**
      * @param studyConfiguration
      * @return the string which forms the html for the copyStudy url.
      */
     private String getCopyStudyUrlString(StudyConfiguration studyConfiguration) {
 
        String token = "", tokenName = "";
       
        try {
            token = SessionHelper.getInstance().getToken();
            tokenName = SessionHelper.getInstance().getTokenName();
        } catch (Exception e) { token = ""; }       
 
        String returnString = null;
 
        if (studyConfiguration.getStudy().isEnabled()) {
            returnString = "<a href=\"copyStudy.action?studyConfiguration.id="
        + studyConfiguration.getId()
        + "&struts.token.name="
        + tokenName
        + "&struts.token="
            + token;
        } else {
            returnString = "<a style=\"color:Grey; text-decoration:none;\""
                + " title=\"Copy Disabled - Enable Study to Copy\"";
        }
        return returnString + "\" >Copy</a>";
     }
 
     /**
      * @param studyConfiguration
      * @return the string which forms the html for the disable/enable Study url.
      */
     private String getArchiveStudyUrlString(StudyConfiguration studyConfiguration) {
 
        String token = SessionHelper.getInstance().getToken();
        String tokenName = SessionHelper.getInstance().getTokenName();
        String returnString = "";
        String actionName = "";
        if (BooleanUtils.isTrue(studyConfiguration.getStudy().isEnabled())) {
            returnString = "<a href=\"disableStudy.action?studyConfiguration.id=";
            actionName = "Disable";
        } else {
            returnString = "<a href=\"enableStudy.action?studyConfiguration.id=";
            actionName = "Enable";
        }
 
        return returnString + studyConfiguration.getId()
        + "&struts.token.name="
        + tokenName
        + "&struts.token="
        + token
        + "\" >" + actionName + "</a>";
     }
 
     private String getStatusMessage(Status studyConfigurationStatus) {
         if (Status.PROCESSING.equals(studyConfigurationStatus)) {
             return AJAX_LOADING_GIF + " " + studyConfigurationStatus.getValue();
         }
         return studyConfigurationStatus.getValue();
     }
 
     /**
      * @return the deploymentService
      */
     public DeploymentService getDeploymentService() {
         return deploymentService;
     }
 
     /**
      * @param deploymentService the deploymentService to set
      */
     public void setDeploymentService(DeploymentService deploymentService) {
         this.deploymentService = deploymentService;
     }
 
 
 }
