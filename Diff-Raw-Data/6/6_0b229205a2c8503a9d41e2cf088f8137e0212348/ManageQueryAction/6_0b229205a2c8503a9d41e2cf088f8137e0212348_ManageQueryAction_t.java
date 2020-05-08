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
 package gov.nih.nci.caintegrator2.web.action.query;
 
 
 import gov.nih.nci.caintegrator2.application.query.QueryManagementService;
 import gov.nih.nci.caintegrator2.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator2.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.QueryResult;
 import gov.nih.nci.caintegrator2.domain.application.ResultRow;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.external.ncia.NCIABasket;
 import gov.nih.nci.caintegrator2.external.ncia.NCIADicomJob;
 import gov.nih.nci.caintegrator2.web.action.AbstractCaIntegrator2Action;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.opensymphony.xwork2.interceptor.ParameterNameAware;
 
 /**
  * Handles the form in which the user constructs, edits and runs a query.
  */
 @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength" }) // see execute method
 
 public class ManageQueryAction extends AbstractCaIntegrator2Action implements ParameterNameAware {
 
     private static final long serialVersionUID = 1L;
 
     private static final String RESULTS_TAB = "searchResults";
     private static final String CRITERIA_TAB = "criteria";
     private static final String COLUMNS_TAB = "columns";
     private static final String SORTING_TAB = "sorting";
     private static final String SAVE_AS_TAB = "saveAs";
 
     private static final String EXECUTE_QUERY = "executeQuery";
     
     private QueryManagementService queryManagementService;
     private StudyManagementService studyManagementService;
     private NCIABasket nciaBasket;
     private String selectedAction = EXECUTE_QUERY;
     private String displayTab;
     private int rowNumber;
     private Long queryId = null;
     private boolean export = false;
 
     /**
      * {@inheritDoc}
      */
     public boolean acceptableParameterName(String parameterName) {
         boolean retVal = true;
         String firstCharacter = parameterName.substring(0, 1);
 
         if (parameterName != null) {
             if (parameterName.startsWith("d-")) {
                 retVal = false;
                 if (parameterName.endsWith("-e")) {
                     setExport(true);
                 }
             } else if (StringUtils.isNumeric(firstCharacter)) {
                 retVal = false;
             }
         }
         return retVal;
     }
     
 
     /**
      * {@inheritDoc}
      */
     public void prepare() {
         super.prepare();
         if ("selectedTabSearchResults".equals(selectedAction)) {
             displayTab = RESULTS_TAB;
         } else {
             displayTab = CRITERIA_TAB;
         }
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         if (getStudySubscription() == null) {
             addActionError("Please select a study under \"My Studies\".");
             return;
         } else if (!getStudy().isDeployed()) {
             addActionError("The study '"
                     + getStudy().getShortTitleText()
                     + "' is not yet deployed.");
             return;
         } else if ("selectedTabSearchResults".equals(selectedAction)) {
             return;
         } else if (EXECUTE_QUERY.equals(selectedAction)
                 || "loadQuery".equals(selectedAction)) {
             validateExecuteQuery(); 
         } else if ("saveQuery".equals(selectedAction)) {
             validateSaveQuery();
         } else if ("saveAsQuery".equals(selectedAction)) {
             validateSaveQuery();
         } else if ("addCriterionRow".equals(selectedAction)) {
             validateAddCriterionRow();
         }
     }
     
     private void validateAddCriterionRow() {
         getQueryForm().validate(this);
     }
     
     private void validateExecuteQuery() {
         ensureQueryIsLoaded();
         getQueryForm().validate(this);
     }
     
     private void validateSaveQuery() {
         getQueryForm().validateForSave(this);
         if (this.hasErrors()) {
             displayTab = SAVE_AS_TAB;
         }
     }
     
     
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.ExcessiveMethodLength" }) // Checking action type.
     public String execute() {
         
         String returnValue = ERROR;
 
         if ("selectedTabSearchResults".equals(selectedAction)) {
             return (isExport()) ? "export" : SUCCESS;
         }
 
         // Check which user action is submitted
         if ("remove".equals(selectedAction)) {
             setQueryResult(null);
             returnValue = removeRow();
         } else if ("addCriterionRow".equals(selectedAction)) {
             setQueryResult(null);
             returnValue = addCriterionRow();
         } else if (EXECUTE_QUERY.equals(selectedAction)) {
            displayTab = RESULTS_TAB;
             returnValue = executeQuery();
         } else if ("saveQuery".equals(selectedAction)) {
             returnValue = saveQuery();
         } else if ("saveAsQuery".equals(selectedAction)) {
             returnValue = saveAsQuery();
         } else if ("deleteQuery".equals(selectedAction)) {
             returnValue = deleteQuery();
         } else if ("updateCriteria".equals(selectedAction)) {
             updateCriteria();
             returnValue = SUCCESS;
         } else if ("updateColumns".equals(selectedAction)) {
             displayTab = COLUMNS_TAB;
             returnValue = SUCCESS;
         } else if ("updateSorting".equals(selectedAction)) {
             updateSorting();
             displayTab = SORTING_TAB;
             returnValue = SUCCESS;
         } else if ("updateResultsPerPage".equals(selectedAction)) {
                displayTab = RESULTS_TAB;
                returnValue = SUCCESS;
         } else if ("createNewQuery".equals(selectedAction)) {
             getQueryForm().createQuery(getStudySubscription());
             setQueryResult(null);
             returnValue = SUCCESS;    
         } else if ("forwardToNcia".equals(selectedAction)) {
             displayTab = RESULTS_TAB;
             returnValue = forwardToNciaBasket();
         } else if ("selectAll".equals(selectedAction)) {
             displayTab = RESULTS_TAB;
             getDisplayableWorkspace().getQueryResult().setSelectAll(true);
             returnValue = toggleCheckboxSelections();
         } else if ("selectNone".equals(selectedAction)) {
             displayTab = RESULTS_TAB;
             getDisplayableWorkspace().getQueryResult().setSelectAll(false);
             returnValue = toggleCheckboxSelections();
         } else if ("retrieveDicomImages".equals(selectedAction)) {
             displayTab = RESULTS_TAB;
             returnValue = createDicomJob();
         } else if ("loadQuery".equals(selectedAction)) {
             displayTab = CRITERIA_TAB;
            returnValue = executeQuery();
         } else {
             addActionError("Unknown action '" + selectedAction + "'");
             returnValue = ERROR; 
         }
         return returnValue;
     }
 
     private void updateCriteria() {
         displayTab = CRITERIA_TAB;
         getQueryForm().processCriteriaChanges();
     }
     
     private void updateSorting() {
         getQueryForm().getResultConfiguration().reindexColumns();
     }
 
     /**
      * Selects all checkboxes.
      * @return the Struts result.
      */
     public String toggleCheckboxSelections() {
         if (getQueryResult() != null 
                 && getQueryResult().getRows() != null
                 && !getQueryResult().getRows().isEmpty()) {
             for (DisplayableResultRow row : getQueryResult().getRows()) {
                 row.setCheckedRow(getDisplayableWorkspace().getQueryResult().getSelectAll());
             }
         }
         return SUCCESS;
     }
     
     /**
      * Forwards to NCIA.
      * @return the Struts result.
      */
     public String forwardToNciaBasket() {
         nciaBasket = queryManagementService.createNciaBasket(retrieveCheckedRows());
         return "nciaBasket";
     }
 
     /**
      * Creates a dicom job and runs.
      * @return the Struts result.
      */
     public String createDicomJob() {
         if (getDisplayableWorkspace().getDicomJob() != null 
                 && getDisplayableWorkspace().getDicomJob().isCurrentlyRunning()) {
             addActionError("There is currently a Dicom retrieval job running on this session, " 
                             + "please wait for that to complete before running another.");
             return "dicomJobCurrentlyRunning";
         }
         NCIADicomJob dicomJob = queryManagementService.createDicomJob(retrieveCheckedRows());
         ImageDataSourceConfiguration dataSource = studyManagementService.retrieveImageDataSource(getStudy());
         if (dataSource != null) {
             dicomJob.setServerConnection(dataSource.getServerProfile());
         }
         getDisplayableWorkspace().setDicomJob(dicomJob);
         dicomJob.setCurrentlyRunning(true);
         return "dicomJobAjax";
     }
 
     private List<DisplayableResultRow> retrieveCheckedRows() {
         List <DisplayableResultRow> checkedRows = new ArrayList<DisplayableResultRow>();
         if (getQueryResult() != null 
             && getQueryResult().getRows() != null
             && !getQueryResult().getRows().isEmpty()) {
             for (DisplayableResultRow row : getQueryResult().getRows()) {
                 if (row.isCheckedRow()) {
                     checkedRows.add(row);
                 }
             }
         }
         return checkedRows;
     }
     
     /**
      * Add a a criterion row to the query, and validates that row has data for this study.
      * 
      * @return the Struts result.
      */
     public String addCriterionRow() {
         getQueryForm().getCriteriaGroup().addCriterion();
         return SUCCESS;
     }
     
     private String removeRow() {
         getQueryForm().getCriteriaGroup().getRows().get(rowNumber).remove();
         return SUCCESS;
     }
     
     /**
      * Execute the current query.
      * 
      * @return the Struts result.
      */
     public String executeQuery() {
         ensureQueryIsLoaded();
         updateSorting();
         if (ResultTypeEnum.GENOMIC.getValue().equals(getQueryForm().getResultConfiguration().getResultType())) {
             GenomicDataQueryResult genomicResult = 
                 queryManagementService.executeGenomicDataQuery(getQueryForm().getQuery());
             setGenomicDataQueryResult(genomicResult);
         } else {
             QueryResult result = queryManagementService.execute(getQueryForm().getQuery());
             loadAllImages(result);
             setQueryResult(new DisplayableQueryResult(result));
         }
         return SUCCESS;
     }
 
     private void ensureQueryIsLoaded() {
         if (getQueryForm().getQuery() == null) {
             getQueryForm().setQuery(getQueryById());
             getQueryForm().setOrgQueryName(getQueryForm().getQuery().getName());
         }
     }
 
     private void loadAllImages(QueryResult result) {
         for (ResultRow resultRow : result.getRowCollection()) {
             if (resultRow.getImageSeries() != null) {
                 resultRow.getImageSeries().getImageCollection().isEmpty();
             }
         }
     }
     
     /**
      * Save the current query.
      * 
      * @return the Struts result.
      */
     public String saveQuery() {
         updateSorting();
         Query query = getQueryForm().getQuery();
         if (!getStudySubscription().getQueryCollection().contains(query)) {
             getStudySubscription().getQueryCollection().add(query);
             query.setSubscription(getStudySubscription());
         }
         queryManagementService.save(query);
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         getQueryForm().setOrgQueryName(query.getName());
         return SUCCESS;
     }
     
     /**
      * Save the current saved query as a new query.
      * 
      * @return the Struts result.
      */
     public String saveAsQuery() {
         updateSorting();
         Query query;
         try {
             query = getQuery().clone();
         } catch (CloneNotSupportedException e) {
             addActionError("Error cloning the Query.");
             return ERROR;
         }
         queryManagementService.save(query);
         setQueryId(query.getId());
         getStudySubscription().getQueryCollection().add(query);
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         getQueryForm().setQuery(getQueryById());
         getQueryForm().setOrgQueryName(getQueryForm().getQuery().getName());
         return SUCCESS;
     }
 
     /**
      * Delete the current saved query.
      * @return the Struts result.
      */
     public String deleteQuery() {
         queryId = getQueryForm().getQuery().getId();
         Query query = getQueryById();
         getStudySubscription().getQueryCollection().remove(query);
         queryManagementService.delete(query);
         getWorkspaceService().saveUserWorkspace(getWorkspace());
         getQueryForm().createQuery(getStudySubscription());
         setQueryResult(null);
         return SUCCESS;
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
      * @return the selectedAction
      */
     public String getSelectedAction() {
         return selectedAction;
     }
 
     /**
      * @param selectedAction the action as selected in the input form
      */
     public void setSelectedAction(String selectedAction) {
         this.selectedAction = selectedAction;
     }
 
     /**
      * @return the rowNumber
      */
     public String getRowNumber() {
         return String.valueOf(rowNumber);
     }
     
     /**
      * @param rowNumber the rowNumber to set
      */
     public void setRowNumber(String rowNumber) {
         if (!StringUtils.isBlank(rowNumber)) {
             this.rowNumber = Integer.parseInt(rowNumber);
         }
     }
         
     /**
      * @return the displayTab
      */
     public String getDisplayTab() {
         return displayTab;
     }
 
     /**
      * @param queryId the queryId to set
      */
     public void setQueryId(Long queryId) {
         getQueryForm().setQuery(null);
         this.queryId = queryId;
     }
 
     private Query getQueryById() {
         for (Query query : getStudySubscription().getQueryCollection()) {
             if (queryId.equals(query.getId())) {
                 return query;
             }
         }
         return null;
     }
 
     /**
      * @return the currently open query id for the left nav menu.
      */
     public Long getOpenQueryId() {
         if (getQueryForm().getQuery() != null) {
             return getQueryForm().getQuery().getId();
         } else {
             return null;
         }
     }
     /**
      * @return the studyManagementService
      */
     public StudyManagementService getStudyManagementService() {
         return studyManagementService;
     }
     /**
      * @param studyManagementService the studyManagementService to set
      */
     public void setStudyManagementService(StudyManagementService studyManagementService) {
         this.studyManagementService = studyManagementService;
     }
     
     /**
      * @return the export
      */
     public boolean isExport() {
         return export;
     }
 
     /**
      * @param export the export to set
      */
     public void setExport(boolean export) {
         this.export = export;
     }
         
     /**
      * Set the page size.
      * @param pageSize the page size
      */
     public void setPageSize(int pageSize) {
         if (getQueryResult() != null) {
             getQueryResult().setPageSize(pageSize);
         }
     }
 
     /**
      * @return the nciaBasket
      */
     public NCIABasket getNciaBasket() {
         return nciaBasket;
     }
 
     /**
      * @return the nciaBasket
      */
     public String getNciaBasketUrl() {
         ImageDataSourceConfiguration dataSource = studyManagementService.retrieveImageDataSource(getStudy());
        return "https://" + dataSource.getServerProfile().getHostname() + "/ncia/externalDataBasketDisplay.jsf";
     }
 }
