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
 package gov.nih.nci.caintegrator2.web.action.study.management;
 
 import gov.nih.nci.caintegrator2.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.FileColumn;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.common.AnnotationValueUtil;
 import gov.nih.nci.caintegrator2.common.PermissibleValueUtil;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.CommonDataElement;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Action used to edit the type and annotation of an imaging file column by a Study Manager.
  */
 @SuppressWarnings("PMD.CyclomaticComplexity") // See selectDataElement()
 public class DefineImagingFileColumnAction extends AbstractImagingSourceAction {
 
     private static final long serialVersionUID = 1L;
 
     private static final String ANNOTATION_TYPE = "Annotation";
     private static final String IDENTIFIER_TYPE = "Identifier";
     private static final String TIMEPOINT_TYPE = "Timepoint";
    private static final String[] COLUMN_TYPES = new String[] {ANNOTATION_TYPE, IDENTIFIER_TYPE, TIMEPOINT_TYPE};
     
     private FileColumn fileColumn = new FileColumn();
     private boolean readOnly;
     private List<String> availableUpdateList = new ArrayList<String>();
     private List<String> permissibleUpdateList = new ArrayList<String>();
     private int dataElementIndex;
     private int definitionIndex;
     private String columnType;
 
     private void clearCacheMemory() {
         columnType = null;
         getDisplayableWorkspace().getDataElementSearchObject().clear();
         availableUpdateList.clear();
         permissibleUpdateList.clear();
     }
     
     private Collection<AbstractAnnotationValue> getAnnotationValueCollection() {
         return fileColumn.getFieldDescriptor().getDefinition().getAnnotationValueCollection();
     }
     
     private String getType() {
         return fileColumn.getFieldDescriptor().getDefinition().getType();
     }
 
     /**
      * Refreshes the current imaging source configuration.
      */
     @Override
     public void prepare() {
         super.prepare();
         if (getFileColumn().getId() != null) {
             setFileColumn(getStudyManagementService().getRefreshedStudyEntity(getFileColumn()));

         }
         setReadOnly(true);
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public void validate() {
         clearErrorsAndMessages();
     }
 
     /**
      * Edit a clinical data source file column.
      * 
      * @return the Struts result.
      */
     public String editFileColumn() {
         clearCacheMemory();
         return SUCCESS;
     }
     
     /**
      * Saves the column type to the database.
      * @return the Struts result.
      */
     public String saveColumnType() {
         updateColumnType();
         if (isColumnTypeAnnotation() && getFileColumn().getFieldDescriptor() == null) {
             getFileColumn().setFieldDescriptor(new AnnotationFieldDescriptor());
             getFileColumn().getFieldDescriptor().setName(getFileColumn().getName());
         }
         getStudyManagementService().save(getStudyConfiguration());
         updateImageDataSourceStatus();
         clearCacheMemory();
         return SUCCESS;
     }
     
     /**
      * Selects an existing annotation definition for a column.
      * 
      * @return the Struts result.
      */
     public String selectDefinition() {
         AnnotationDefinition definitionToUse = getStudyManagementService().
                         getRefreshedStudyEntity(getDefinitions().get(getDefinitionIndex()));
         getStudyManagementService().setDefinition(getStudyConfiguration().getStudy(),
                                                   getFileColumn(), 
                                                   definitionToUse,
                                                   EntityTypeEnum.IMAGESERIES);
         updateImageDataSourceStatus();
         return SUCCESS;
     }
     
     /**
      * Let's the user create a new AnnotationDefinition for a column.
      * @return the Struts result.
      */
     public String createNewDefinition() {
         getStudyManagementService().createDefinition(getFileColumn().getFieldDescriptor(), 
                                                      getStudyConfiguration().getStudy(),
                                                      EntityTypeEnum.IMAGESERIES);
         setReadOnly(false);
         clearCacheMemory();
         return SUCCESS;
     }
     
     /**
      * Selects an existing CaDSR data element as the definition for a column.
      * 
      * @return the Struts result.
      */
     @SuppressWarnings("PMD.CyclomaticComplexity") // Null Checks and Try/Catch  
     public String selectDataElement() {
         FileColumn originalFileColumn = getFileColumn();
         try {
             getStudyManagementService().setDataElement(getFileColumn(), 
                                                        retrieveSelectedDataElement(),
                                                        getStudyConfiguration().getStudy(),
                                                        EntityTypeEnum.IMAGESERIES,
                                                        getKeywordsForSearch());
         } catch (ConnectionException e) {
             addActionError(e.getMessage());
             setFileColumn(originalFileColumn);
             prepare();
             return ERROR;
         } catch (ValidationException e) {
             addActionError(e.getResult().getInvalidMessage());
             setFileColumn(originalFileColumn);
             prepare();
             return ERROR;
         }
         updateImageDataSourceStatus();
         return SUCCESS;
     }
     
     private CommonDataElement retrieveSelectedDataElement() {
         CommonDataElement selectedDataElement = getDataElements().get(getDataElementIndex());
         selectedDataElement.setId(null);
         selectedDataElement.setValueDomain(null);
         return selectedDataElement;
     }
     
 
     /**
      * Updates a clinical data source file column.
      * 
      * @return the Struts result.
      */
     public String updateFileColumn() {
         updateColumnType();
         if (isPermissibleOn() && !isFromCadsr()) {
             try {
                 updatePermissible();
             } catch (ParseException e) {
                 addActionError("Error parsing the data field!!!");
                 clearCacheMemory();
                 return ERROR;
             }
         }
         getStudyManagementService().save(getStudyConfiguration());
         updateImageDataSourceStatus();
         return SUCCESS;
     }
 
     private void updateImageDataSourceStatus() {
         getStudyManagementService().updateImageDataSourceStatus(getStudyConfiguration()); // If it changes state
     }
 
     /**
      * @return the fileColumn
      */
     public FileColumn getFileColumn() {
         return fileColumn;
     }
 
     /**
      * @param fileColumn the fileColumn to set
      */
     public void setFileColumn(FileColumn fileColumn) {
         this.fileColumn = fileColumn;
     }
 
     /**
      * @return the columnType
      */
     public String getColumnType() {
         if (this.columnType == null) {
             if (getFileColumn().isIdentifierColumn()) {
                 columnType = IDENTIFIER_TYPE;
             } else if (getFileColumn().isTimepointColumn()) {
                 columnType = TIMEPOINT_TYPE;
             } else {
                 columnType = ANNOTATION_TYPE;
             }
         }
         return columnType;
     }
 
     /**
      * @param columnType the columnType to set
      */
     public void setColumnType(String columnType) {
         this.columnType = columnType;
     }
 
     /**
 
      * 
      * @return if the ColumnType is ANNOTATION_TYPE.
      */
     public boolean isColumnTypeAnnotation() {
         if (getColumnType().equals(ANNOTATION_TYPE)) {
             return true;
         } 
         return false;
     }
 
 
     private void updateColumnType() {
         if (IDENTIFIER_TYPE.equals(columnType)) {
             getFileColumn().getAnnotationFile().setIdentifierColumn(getFileColumn());
         } else if (TIMEPOINT_TYPE.equals(columnType)) {
             getFileColumn().getAnnotationFile().setTimepointColumn(getFileColumn());
         } else if (ANNOTATION_TYPE.equals(columnType)) {
             getFileColumn().makeAnnotationColumn();
         }
     }
 
     /**
      * @return the columnTypes
      */
     @SuppressWarnings("PMD")    // Prevent internal array exposure warning
     public String[] getColumnTypes() {
         return COLUMN_TYPES;
     }
     
     /**
      * Converts the enum to the string list.
      * @return the annotationTypes
      */
     public String[] getAnnotationDataTypes() {
         List<String> types = new ArrayList<String>();
         for (AnnotationTypeEnum type : AnnotationTypeEnum.values()) {
             types.add(type.getValue());
         }
         return types.toArray(new String[types.size()]);
     }
     
     /**
      * @return the definitions
      */
     public List<AnnotationDefinition> getDefinitions() {
         return getDisplayableWorkspace().getDataElementSearchObject().getSearchDefinitions();
     }
 
     /**
      * @return the dataElements
      */
     public List<CommonDataElement> getDataElements() {
         return getDisplayableWorkspace().getDataElementSearchObject().getSearchCommonDataElements();
     }
 
     /**
      * @return the dataElementIndex
      */
     public int getDataElementIndex() {
         return dataElementIndex;
     }
 
     /**
      * @param dataElementIndex the dataElementIndex to set
      */
     public void setDataElementIndex(int dataElementIndex) {
         this.dataElementIndex = dataElementIndex;
     }
 
     /**
      * @return the definitionIndex
      */
     public int getDefinitionIndex() {
         return definitionIndex;
     }
 
     /**
      * @param definitionIndex the definitionIndex to set
      */
     public void setDefinitionIndex(int definitionIndex) {
         this.definitionIndex = definitionIndex;
     }
 
     /**
      * @return the readOnly
      */
     public boolean isReadOnly() {
         return readOnly;
     }
 
     /**
      * @param readOnly the readOnly to set
      */
     public void setReadOnly(boolean readOnly) {
         this.readOnly = readOnly;
     }
 
     /**
      * @return the keywordsForSearch
      */
     public String getKeywordsForSearch() {
         return getDisplayableWorkspace().getDataElementSearchObject().getKeywordsForSearch();
     }
 
 
 
     /**
      * 
      * @return if the Permissible is available.
      */
     public boolean isPermissibleOn() {
         if (isColumnTypeAnnotation()
                 && fileColumn.getFieldDescriptor().getDefinition() != null) {
             return true;
         } 
         return false;
     }
     
     /**
      * @return the availableValue
      * @throws ValidationException Invalid data
     */
    public Set<String> getAvailableValues() throws ValidationException {
        return AnnotationValueUtil.getAdditionalValue(getAnnotationValueCollection(),
                fileColumn.getDataValues(), getPermissibleValues());
    }
     
     /**
      * @return the permissibleValues
      */
     public Set<String> getPermissibleValues() {
         return PermissibleValueUtil.getDisplayPermissibleValue(getPermissibleCollection());
     }
     
     /**
      * @return the Struts result.
      * @throws ParseException 
      */
     private void updatePermissible() throws ParseException {
         PermissibleValueUtil.update(getType(),
                 getPermissibleCollection(), getPermissibleUpdateList());
     }
 
     private Collection<AbstractPermissibleValue> getPermissibleCollection() {
         return fileColumn.getFieldDescriptor().getDefinition().getPermissibleValueCollection();
     }
 
     /**
      * @return the permissibleReturnList
      */
     public List<String> getPermissibleUpdateList() {
         return permissibleUpdateList;
     }
 
     /**
      * @param permissibleUpdateList the new list of permissible display string
      */
     public void setPermissibleUpdateList(List<String> permissibleUpdateList) {
         this.permissibleUpdateList = permissibleUpdateList;
     }
 
     /**
      * @return the availableUpdateList
      */
     public List<String> getAvailableUpdateList() {
         return availableUpdateList;
     }
 
     /**
      * @param availableUpdateList the availableUpdateList to set
      */
     public void setAvailableUpdateList(List<String> availableUpdateList) {
         this.availableUpdateList = availableUpdateList;
     }
     
     /**
      * Determines if the AnnotationDefinition came from a CDE.
      * 
      * @return T/F value.
      */
     public boolean isFromCadsr() {
         if (getFileColumn() != null 
             && getFileColumn().getFieldDescriptor() != null 
             && getFileColumn().getFieldDescriptor().getDefinition() != null 
             && getFileColumn().getFieldDescriptor().getDefinition().getCde() != null) {
             return true;
         }
         return false;
     }
 }
