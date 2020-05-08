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
 
 import gov.nih.nci.caintegrator2.application.analysis.AnalysisService;
 import gov.nih.nci.caintegrator2.application.analysis.grid.GridDiscoveryServiceJob;
 import gov.nih.nci.caintegrator2.application.analysis.grid.comparativemarker.ComparativeMarkerSelectionParameters;
 import gov.nih.nci.caintegrator2.application.analysis.grid.preprocess.PreprocessDatasetParameters;
 import gov.nih.nci.caintegrator2.application.query.InvalidCriterionException;
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.common.HibernateUtil;
 import gov.nih.nci.caintegrator2.domain.application.AnalysisJobStatusEnum;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.external.ServerConnectionProfile;
 import gov.nih.nci.caintegrator2.web.Cai2WebUtil;
 import gov.nih.nci.caintegrator2.web.action.AbstractDeployedStudyAction;
 import gov.nih.nci.caintegrator2.web.ajax.IPersistedAnalysisJobAjaxUpdater;
 import gridextensions.ComparativeMarkerSelectionParameterSet;
 import gridextensions.PreprocessDatasetParameterSet;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * 
  */
 public class ComparativeMarkerSelectionAnalysisAction  extends AbstractDeployedStudyAction {
     
     private static final long serialVersionUID = 1L;
 
     /**
      * Indicates action should open the analysis page.
      */
     public static final String OPEN_ACTION = "open";
     
     /**
      * Indicates action should execute the analysis.
      */
     public static final String EXECUTE_ACTION = "execute";
     
     /**
      * Indicates action should go to the status page.
      */
     public static final String STATUS_ACTION = "status";
 
     private AnalysisService analysisService;
     private QueryManagementService queryManagementService;
     private IPersistedAnalysisJobAjaxUpdater ajaxUpdater;
     private String selectedAction = OPEN_ACTION;
     private List<String> platformsInStudy = new ArrayList<String>();
 
     /**
      * {@inheritDoc}
      */
    @Override
    public void prepare() {
        super.prepare();
        platformsInStudy = new ArrayList<String>(
                getQueryManagementService().retrieveGeneExpressionPlatformsForStudy(getStudy()));
        Collections.sort(platformsInStudy);
    }
     
     /**
      * Cancel action.
      * @return struts result.
      */
     public String cancel() {
        return SUCCESS; 
     }
     
     /**
      * {@inheritDoc}
      */
     public String execute() {
         if (OPEN_ACTION.equals(getSelectedAction())) {
             return open();
         } else if (EXECUTE_ACTION.equals(getSelectedAction())) {
             return executeAnalysis();
         } else  {
             addActionError("Invalid action: " + getSelectedAction());
             return INPUT;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         super.validate();
         validateStudyHasGenomicData("Comparative Marker Selection Analysis");
         
         if (EXECUTE_ACTION.equals(getSelectedAction())) {
             validateExecuteAnalysis();
         }
     }
 
     private void validateExecuteAnalysis() {
         if (StringUtils.isBlank(getCurrentComparativeMarkerSelectionAnalysisJob().getName())) {
             addFieldError("currentComparativeMarkerSelectionAnalysisJob.name", "Job name required.");
         }
         if (getComparativeMarkerSelectionAnalysisForm().getSelectedQueryNames().size() != 2) {
             addFieldError("comparativeMarkerSelectionAnalysisForm.unselectedQueryNames", "2 Queries/Lists required.");
         }
         if (isStudyHasMultiplePlatforms() 
                 && StringUtils.isBlank(getComparativeMarkerSelectionAnalysisForm().getPlatformName())) {
             addFieldError("comparativeMarkerSelectionAnalysisForm.platformName", "Platform name required.");
         }
     }
     
     /**
      * @return
      */
     private String open() {
         resetCurrentComparativeMarkerSelectionAnalysisJob();
         loadDefaultValues();
         return SUCCESS;
     }
     
     private void loadDefaultValues() {
         populateClinicalQueriesAndLists();
         getComparativeMarkerSelectionAnalysisForm().setPreprocessDatasetparameters(new PreprocessDatasetParameters());
         getComparativeMarkerSelectionAnalysisForm().setComparativeMarkerSelectionParameters(
                 new ComparativeMarkerSelectionParameters());
 
         String fileName = "CMS-" + System.currentTimeMillis();
         getPreprocessDatasetParameters().setProcessedGctFilename(fileName + ".gct");
         getComparativeMarkerSelectionParameters().setClassificationFileName(fileName + ".cls");
     }
 
     private void populateClinicalQueriesAndLists() {
         getComparativeMarkerSelectionAnalysisForm().setDisplayableQueries(
                 Cai2WebUtil.retrieveDisplayableQueries(getStudySubscription(), getQueryManagementService(), false));
         getComparativeMarkerSelectionAnalysisForm().getDisplayableQueryMap().clear();
         for (DisplayableQuery displayableQuery : getComparativeMarkerSelectionAnalysisForm().getDisplayableQueries()) {
             getComparativeMarkerSelectionAnalysisForm().getDisplayableQueryMap().put(displayableQuery.getDisplayName(),
                     displayableQuery);
         }
         getComparativeMarkerSelectionAnalysisForm().getUnselectedQueries().clear();
         for (DisplayableQuery query : getComparativeMarkerSelectionAnalysisForm().getDisplayableQueries()) {
             getComparativeMarkerSelectionAnalysisForm().getUnselectedQueries().put(query.getDisplayName(), query);
         }
     }
     
     private String executeAnalysis() {
         try {
             if (!loadParameters()) {
                 return INPUT;
             }
         } catch (InvalidCriterionException e) {
             addActionError(e.getMessage());
             return INPUT;
         }
         getCurrentComparativeMarkerSelectionAnalysisJob().setCreationDate(new Date());
         getCurrentComparativeMarkerSelectionAnalysisJob().setStatus(AnalysisJobStatusEnum.SUBMITTED);
         getStudySubscription().getAnalysisJobCollection()
             .add(getCurrentComparativeMarkerSelectionAnalysisJob());
         getCurrentComparativeMarkerSelectionAnalysisJob().setSubscription(getStudySubscription());
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         ajaxUpdater.runJob(getCurrentComparativeMarkerSelectionAnalysisJob());
         resetCurrentComparativeMarkerSelectionAnalysisJob();
         return STATUS_ACTION;
     }
     
     private boolean loadParameters() throws InvalidCriterionException {
         loadPlatforms();
         loadServers();
         return loadQueries();
     }
     
     private void loadPlatforms() {
         getComparativeMarkerSelectionAnalysisForm().getPreprocessDatasetparameters().setPlatformName(
                 getComparativeMarkerSelectionAnalysisForm().getPlatformName());
     }
     
     private void loadServers() {
         ServerConnectionProfile server = new ServerConnectionProfile();
         server.setUrl(getCurrentComparativeMarkerSelectionAnalysisJob().getPreprocessDataSetUrl());
         getPreprocessDatasetParameters().setServer(server);
         server = new ServerConnectionProfile();
         server.setUrl(getCurrentComparativeMarkerSelectionAnalysisJob().getComparativeMarkerSelectionUrl());
         getComparativeMarkerSelectionParameters().setServer(server);
     }
     
     private boolean loadQueries() throws InvalidCriterionException {
         if (!getComparativeMarkerSelectionAnalysisForm().getSelectedQueryNames().isEmpty()) {
             getPreprocessDatasetParameters().getClinicalQueries().clear();
             getComparativeMarkerSelectionParameters().getClinicalQueries().clear();
             for (String name : getComparativeMarkerSelectionAnalysisForm().getSelectedQueryNames()) {
                 Query currentQuery = getQuery(name);
                 if (!validateQuerySampleCount(currentQuery)) {
                     return false;
                 }
                 getPreprocessDatasetParameters().getClinicalQueries().add(currentQuery);
                 getComparativeMarkerSelectionParameters().getClinicalQueries().add(currentQuery);
             }
         }
         return true;
     }
 
     private boolean validateQuerySampleCount(Query currentQuery) throws InvalidCriterionException {
         int numSamples = 0;
         QueryResult results = queryManagementService.execute(currentQuery);
         for (ResultRow row : results.getRowCollection()) {
             if (!row.getSubjectAssignment().getSampleAcquisitionCollection().isEmpty()) {
                 numSamples++;
             }
             if (numSamples >= 2) {
                 break;
             }
         }
         return verifySampleCounts(currentQuery, numSamples);
     }
 
     private boolean verifySampleCounts(Query currentQuery, int numSamples) {
         if (numSamples < 2) {
            String invalidString = 
                "'" + currentQuery.getName() 
                    + "' is invalid because it contains less than 2 samples.";
            addActionError(currentQuery.isSubjectListQuery() ? "Subject List " + invalidString 
                    : "Query " + invalidString);
             return false;
         }
         return true;
     }
     
     private Query getQuery(String displayableQueryName) {
         Query query = getComparativeMarkerSelectionAnalysisForm().getDisplayableQueryMap().
             get(displayableQueryName).getQuery();
         if (!query.isSubjectListQuery()) {
             query = getQueryManagementService().getRefreshedEntity(query);
             HibernateUtil.loadCollection(query);
         }
         return query;
     }
 
     /**
      * @return the analysisService
      */
     public AnalysisService getAnalysisService() {
         return analysisService;
     }
 
     /**
      * @param analysisService the analysisService to set
      */
     public void setAnalysisService(AnalysisService analysisService) {
         this.analysisService = analysisService;
     }
 
     /**
      * @return the queryManagementService
      */
     public QueryManagementService getQueryManagementService() {
         return queryManagementService;
     }
 
     /**
      * @param queryManagementService the queryManagementService to set
      */
     public void setQueryManagementService(QueryManagementService queryManagementService) {
         this.queryManagementService = queryManagementService;
     }
 
     /**
      * @return the ajaxUpdater
      */
     public IPersistedAnalysisJobAjaxUpdater getAjaxUpdater() {
         return ajaxUpdater;
     }
 
     /**
      * @param ajaxUpdater the ajaxUpdater to set
      */
     public void setAjaxUpdater(IPersistedAnalysisJobAjaxUpdater ajaxUpdater) {
         this.ajaxUpdater = ajaxUpdater;
     }
 
     /**
      * @return the selectedAction
      */
     public String getSelectedAction() {
         return selectedAction;
     }
 
     /**
      * @param selectedAction the selectedAction to set
      */
     public void setSelectedAction(String selectedAction) {
         this.selectedAction = selectedAction;
     }
     
     /**
      * @return PreprocessDatasetParameters.
      */
     public PreprocessDatasetParameters getPreprocessDatasetParameters() {
         return getComparativeMarkerSelectionAnalysisForm().getPreprocessDatasetparameters();
     }
 
     /**
      * @return ComparativeMarkerSelectionParameters.
      */
     public ComparativeMarkerSelectionParameters getComparativeMarkerSelectionParameters() {
         return getComparativeMarkerSelectionAnalysisForm().getComparativeMarkerSelectionParameters();
     }
     
     /**
      * @return PreprocessDatasetParameters.
      */
     public PreprocessDatasetParameterSet getPreprocessDatasetParameterSet() {
         return getPreprocessDatasetParameters().getDatasetParameters();
     }
 
     /**
      * @return ComparativeMarkerSelectionParameters.
      */
     public ComparativeMarkerSelectionParameterSet getComparativeMarkerSelectionParameterSet() {
         return getComparativeMarkerSelectionParameters().getDatasetParameters();
     }
 
     /**
      * @return available PreprocessDataset services.
      */
     public Map<String, String> getPreprocessDatasetServices() {
         return GridDiscoveryServiceJob.getGridPreprocessServices();
     }
 
     /**
      * @return available ComparativeMarkerSelection services.
      */
     public Map<String, String> getComparativeMarkerSelectionServices() {
         return GridDiscoveryServiceJob.getGridCmsServices();
     }
     
     
     /**
      * Determines if study has multiple platforms.
      * @return T/F value if study has multiple platforms.
      */
     public boolean isStudyHasMultiplePlatforms() {
         return platformsInStudy.size() > 1;
     }
 
     /**
      * @return the platformsInStudy
      */
     public List<String> getPlatformsInStudy() {
         return platformsInStudy;
     }
 }
