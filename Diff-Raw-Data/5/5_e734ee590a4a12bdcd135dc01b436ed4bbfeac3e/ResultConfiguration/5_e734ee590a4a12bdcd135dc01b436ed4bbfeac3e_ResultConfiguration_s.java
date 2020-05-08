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
 package gov.nih.nci.caintegrator2.web.action.query.form;
 
 import gov.nih.nci.caintegrator2.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.ResultsOrientationEnum;
 import gov.nih.nci.caintegrator2.domain.application.SortTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 /**
  * Contains query form configuration for the result rows to be displayed.
  */
 public class ResultConfiguration {
 
     private final QueryForm form;
     private final List<ColumnSelectionList> columnSelectionLists;
 
     ResultConfiguration(QueryForm form) {
         this.form = form;
         columnSelectionLists = new ArrayList<ColumnSelectionList>();
         for (AnnotationGroup group : getStudy().getAnnotationGroups()) {
             columnSelectionLists.add(new ColumnSelectionList(this, 
                     getStudy().getVisibleAnnotationCollection(group.getName()), group));
         }
         Collections.sort(columnSelectionLists);
     }
 
     Study getStudy() {
         return getQuery().getSubscription().getStudy();
     }
 
     /**
      * @return the resultType
      */
     public String getResultType() {
         if (getQuery().getResultType() == null) {
             return "";
         } else {
             return getQuery().getResultType().getValue();
         }
     }
 
     /**
      * @param resultType the resultType to set
      */
     public void setResultType(String resultType) {
         if (StringUtils.isBlank(resultType)) {
             getQuery().setResultType(null);
         } else {
             getQuery().setResultType(ResultTypeEnum.getByValue(resultType));
         }
         if (ResultTypeEnum.GENOMIC.equals(getQuery().getResultType())) {
             getQuery().getColumnCollection().clear();
             if (StringUtils.isBlank(getReporterType())) {
                 setReporterType(ReporterTypeEnum.GENE_EXPRESSION_PROBE_SET.getValue());
             }
         }
     }
 
     /**
      * @return the orientation
      */
     public String getOrientation() {
         if (getQuery().getOrientation() == null) {
             return "";
         } else {
             return getQuery().getOrientation().getValue();
         }
     }
 
     /**
      * @param orientation the orientation to set
      */
     public void setOrientation(String orientation) {
         if (StringUtils.isBlank(orientation)) {
             getQuery().setOrientation(null);
         } else {
             getQuery().setOrientation(ResultsOrientationEnum.getByValue(orientation));
         }
     }
 
     /**
      * @return the reporterType
      */
     public String getReporterType() {
         if (getQuery().getReporterType() != null) {
             return getQuery().getReporterType().getValue();
         } else {
             return "";
         }
     }
 
     /**
      * @param reporterType the reporterType to set
      */
     public void setReporterType(String reporterType) {
         if (StringUtils.isBlank(reporterType)) {
             getQuery().setReporterType(null);
         } else {
             getQuery().setReporterType(ReporterTypeEnum.getByValue(reporterType));
         }
     }
 
     /**
      * @return the form
      */
     private QueryForm getForm() {
         return form;
     }
 
     /**
      * @return the query
      */
     Query getQuery() {
         return getForm().getQuery();
     }
 
     /**
      * @return the list of columns in order by columnIndex
      */
     public List<ResultColumn> getSelectedColumns() {
         List<ResultColumn> selectedColumns = new ArrayList<ResultColumn>();
         selectedColumns.addAll(getQuery().retrieveVisibleColumns());
         Collections.sort(selectedColumns);
         return selectedColumns;
     }
 
     /**
      * @param columnName get index of this column
      * @return the index
      */
     public int getColumnIndex(String columnName) {
         return getColumn(columnName).getColumnIndex() + 1;
     }
 
     /**
      * @return the allowable indexes
      */
     public int[] getColumnIndexOptions() {
         int[] indexes = new int[getQuery().retrieveVisibleColumns().size()];
         for (int i = 0; i < indexes.length; i++) {
             indexes[i] = i + 1;
         }
         return indexes;
     }
     
     /**
      * @param columnName get index of this column
      * @param index the index
      */
     public void setColumnIndex(String columnName, int index) {
        getColumn(columnName).setColumnIndex(index - 1);
     }
 
     private ResultColumn getColumn(String columnName) {
         for (ResultColumn column : getQuery().retrieveVisibleColumns()) {
             if (column.getAnnotationFieldDescriptor().getDefinition().getDisplayName().equals(columnName)) {
                 return column;
             }
         }
         return null;
     }
     
     /**
      * For getting the sortType as a string (for JSP purposes).
      * @param columnName get index of this column.
      * @return sortType.
      */
     public String getSortType(String columnName) {
         if (getColumn(columnName) != null && getColumn(columnName).getSortType() != null) {
             return getColumn(columnName).getSortType().getValue();
         } else {
             return "";
         }
     }
     
     /**
      * Sets sort type based on string input.
      * @param columnName get index of this column.
      * @param sortType to set sortType.
      */
     public void setSortType(String columnName, String sortType) {
         ResultColumn column = getColumn(columnName);
         if (column == null) {
             return;
         }
         if (StringUtils.isBlank(sortType)) {
             column.setSortType(null);
         } else {
             column.setSortType(SortTypeEnum.getByValue(sortType));
         }
     }
 
     /**
      * Revises the result column indexes to ensure there are no duplicates.
      */
     public void reindexColumns() {
         List<ResultColumn> selectedColumns = getSelectedColumns();
         for (int i = 0; i < selectedColumns.size(); i++) {
             selectedColumns.get(i).setColumnIndex(i);
         }
     }
 
     /**
      * @return the columnSelectionLists
      */
     public List<ColumnSelectionList> getColumnSelectionLists() {
         return columnSelectionLists;
     }
     
     /**
      * Selects all column values.
      */
     public void selectAllValues() {
         for (ColumnSelectionList columnSelectionList : columnSelectionLists) {
             columnSelectionList.selectAllValues();
         }
     }
 
 }
