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
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.opensymphony.xwork2.ValidationAware;
 
 import gov.nih.nci.caintegrator2.application.query.ResultTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.BooleanOperatorEnum;
import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 /**
  * Top-level UI element for query management.
  */
 public class QueryForm {
     
     private Query query;
     private AnnotationDefinitionList clinicalAnnotations;
     private AnnotationDefinitionList imageSeriesAnnotations;
     private CriteriaGroup criteriaGroup;
     private ResultConfiguration resultConfiguration;
     
     /**
      * Configures a new query.
      * 
      * @param subscription query belongs to this subscription
      */
     public void createQuery(StudySubscription subscription) {
         query = new Query();
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setCriterionCollection(new HashSet<AbstractCriterion>());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND.getValue());
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setSubscription(subscription);
         query.setResultType(ResultTypeEnum.CLINICAL.getValue());
         setQuery(query);
         setResultConfiguration(new ResultConfiguration(this));
     }
 
     private void initialize() {
         if (query != null) {
             Study study = getQuery().getSubscription().getStudy();
            Cai2Util.loadCollection(study.getStudyConfiguration().getGenomicDataSources());
            Cai2Util.loadCollection(study.getImageSeriesAnnotationCollection());
             clinicalAnnotations = new AnnotationDefinitionList(study.getSubjectAnnotationCollection());
             imageSeriesAnnotations = new AnnotationDefinitionList(study.getImageSeriesAnnotationCollection());
             criteriaGroup = new CriteriaGroup(this);
             resultConfiguration = new ResultConfiguration(this);
         } else {
             criteriaGroup = null;
             resultConfiguration = null;
             clinicalAnnotations = null;
             imageSeriesAnnotations = null;
         }
     }
 
     /**
      * Returns the current query.
      * 
      * @return the query.
      */
     public Query getQuery() {
         return query;
     }
 
     /**
      * Sets the query for the form.
      * 
      * @param query the query for the form
      */
     public void setQuery(Query query) {
         this.query = query;
         initialize();
     }
 
     AnnotationDefinitionList getClinicalAnnotations() {
         return clinicalAnnotations;
     }
 
     AnnotationDefinitionList getImageSeriesAnnotations() {
         return imageSeriesAnnotations;
     }
 
     /**
      * @return the criteriaGroup
      */
     public CriteriaGroup getCriteriaGroup() {
         return criteriaGroup;
     }
     
     /**
      * Validates the form prior to saving the query.
      * 
      * @param action receives validation errors.
      */
     public void validate(ValidationAware action) {
         getCriteriaGroup().validate(action);
     }
     
     /**
      * Validates the form prior to saving the query.
      * 
      * @param action receives validation errors.
      */
     public void validateForSave(ValidationAware action) {
         validate(action);
         validateQueryName(action);
     }
 
     private void validateQueryName(ValidationAware action) {
         if (StringUtils.isBlank(getQuery().getName())) {
             action.addActionError("Query Name is required.");
         } else {
             validateUniqueQueryName(action);
         }
     }
 
     private void validateUniqueQueryName(ValidationAware action) {
         for (Query nextQuery : getQuery().getSubscription().getQueryCollection()) {
             if (getQuery().getName().equalsIgnoreCase(nextQuery.getName()) && !getQuery().equals(nextQuery)) {
                 action.addActionError("There is already a Query named " + getQuery().getName() + ".");
             }
         }
     }
 
     /**
      * @return the resultConfiguration
      */
     public ResultConfiguration getResultConfiguration() {
         return resultConfiguration;
     }
 
     private void setResultConfiguration(ResultConfiguration resultConfiguration) {
         this.resultConfiguration = resultConfiguration;
     }
 
     /**
      * Updates the underlying query with any pending changes.
      */
     public void processCriteriaChanges() {
         getCriteriaGroup().processCriteriaChanges();
     }
 
     /**
      * Check if this is a saved query.
      * @return boolean of is a saved query
      */
     public boolean isSavedQuery() {
         return (getQuery().getId() != null);
     }
     
     /**
      * @return a list of criteria types
      */
     public List<String> getCriteriaTypeOptions() {
         Study study = query.getSubscription().getStudy();
         List<String> options = new ArrayList<String>();
         options.add(CriterionRowTypeEnum.CLINICAL.getValue());
         if (study.hasGenomicDataSources()) {
             options.add(CriterionRowTypeEnum.GENE_EXPRESSION.getValue());
         }
         if (study.hasImageSeriesData()) {
             options.add(CriterionRowTypeEnum.IMAGE_SERIES.getValue());
         }
         return options;
     }
 
     /**
      * Check if search result is genomic and all genes are going to be searched.
      * @return boolean of potentially a large query
      */
     public boolean isPotentiallyLargeQuery() {
         if (query != null && query.isGenomicResultType()) {
             return criteriaGroup.hasNoGenomicCriterion();
         }
         return false;
     }
 }
