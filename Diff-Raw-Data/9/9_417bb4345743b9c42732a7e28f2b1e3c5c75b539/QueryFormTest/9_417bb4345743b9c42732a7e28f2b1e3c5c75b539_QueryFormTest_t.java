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
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator2.application.study.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.NumericComparisonOperatorEnum;
 import gov.nih.nci.caintegrator2.application.study.Status;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.WildCardTypeEnum;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.StringPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator2.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator2.domain.application.FoldChangeCriterion;
 import gov.nih.nci.caintegrator2.domain.application.GeneNameCriterion;
 import gov.nih.nci.caintegrator2.domain.application.NumericComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.Query;
 import gov.nih.nci.caintegrator2.domain.application.RegulationTypeEnum;
 import gov.nih.nci.caintegrator2.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator2.domain.application.SelectedValueCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StringComparisonCriterion;
 import gov.nih.nci.caintegrator2.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.opensymphony.xwork2.ValidationAwareSupport;
 
 public class QueryFormTest {
     
     private long nextId = 0;
     private QueryForm queryForm = new QueryForm();
     private StudySubscription subscription;
     private AnnotationDefinition stringClinicalAnnotation1;
     private AnnotationDefinition stringClinicalAnnotation2;
     private AnnotationDefinition numericClinicalAnnotation;
     private AnnotationDefinition selectClinicalAnnotation1;
     private AnnotationDefinition selectClinicalAnnotation2;
     private AnnotationDefinition testImageSeriesAnnotation;
     private StringPermissibleValue value1 = new StringPermissibleValue();
     private StringPermissibleValue value2 = new StringPermissibleValue();
     private StringPermissibleValue value3 = new StringPermissibleValue();
 
     @Before
     public void setUp() {
         subscription = new StudySubscription();
         subscription.setId(1L);
         Study study = new Study();
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.setStatus(Status.DEPLOYED);
         study.setStudyConfiguration(studyConfiguration);
         study.setSubjectAnnotationCollection(new HashSet<AnnotationDefinition>());
         study.setImageSeriesAnnotationCollection(new HashSet<AnnotationDefinition>());
         study.setSampleAnnotationCollection(new HashSet<AnnotationDefinition>());
         
         subscription.setStudy(study);
         stringClinicalAnnotation1 = createDefinition("stringClinicalAnnotation1", AnnotationTypeEnum.STRING);
         stringClinicalAnnotation2 = createDefinition("stringClinicalAnnotation2", AnnotationTypeEnum.STRING);
         numericClinicalAnnotation = createDefinition("numericClinicalAnnotation", AnnotationTypeEnum.NUMERIC);
         selectClinicalAnnotation1 = createDefinition("selectClinicalAnnotation1", AnnotationTypeEnum.STRING);
         selectClinicalAnnotation1.setPermissibleValueCollection(new HashSet<AbstractPermissibleValue>());
         value1.setStringValue("value1");
         value3.setStringValue("value3");
         value2.setStringValue("value2");
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value1);
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value2);
         selectClinicalAnnotation1.getPermissibleValueCollection().add(value3);
 
         selectClinicalAnnotation2 = createDefinition("selectClinicalAnnotation2", AnnotationTypeEnum.STRING);
         selectClinicalAnnotation2.setPermissibleValueCollection(new HashSet<AbstractPermissibleValue>());
         StringPermissibleValue value2_1 = new StringPermissibleValue();
         value2_1.setStringValue("value2_1");
         selectClinicalAnnotation2.getPermissibleValueCollection().add(value2_1);
 
         study.getSubjectAnnotationCollection().add(stringClinicalAnnotation1);
         study.getSubjectAnnotationCollection().add(stringClinicalAnnotation2);
         study.getSubjectAnnotationCollection().add(numericClinicalAnnotation);
         study.getSubjectAnnotationCollection().add(selectClinicalAnnotation1);
         study.getSubjectAnnotationCollection().add(selectClinicalAnnotation2);
         testImageSeriesAnnotation = createDefinition("testImageSeriesAnnotation", AnnotationTypeEnum.STRING);
         study.getImageSeriesAnnotationCollection().add(testImageSeriesAnnotation);
     }
 
     private AnnotationDefinition createDefinition(String name, AnnotationTypeEnum type) {
         AnnotationDefinition definition = new AnnotationDefinition();
         definition.setDisplayName(name);
         definition.setId(nextId++);
         definition.setType(type.getValue());
         definition.setPermissibleValueCollection(new HashSet<AbstractPermissibleValue>());
         definition.setAnnotationValueCollection(new HashSet<AbstractAnnotationValue>());
         return definition;
     }
 
     @Test
     public void testCreateQuery() {
         queryForm.createQuery(subscription);
         assertNotNull(queryForm.getQuery());
         assertNotNull(queryForm.getCriteriaGroup());
         assertEquals(subscription, queryForm.getQuery().getSubscription());
         assertEquals(6, queryForm.getClinicalAnnotations().getNames().size());
         assertEquals("numericClinicalAnnotation", queryForm.getClinicalAnnotations().getNames().get(1));
         assertEquals(stringClinicalAnnotation1, queryForm.getClinicalAnnotations().getDefinition("stringClinicalAnnotation1"));
 
         
         assertEquals(2, queryForm.getCriteriaTypeOptions().size());
         queryForm.getQuery().getSubscription().getStudy().getStudyConfiguration().getGenomicDataSources().add(new GenomicDataSourceConfiguration());
         assertEquals(3, queryForm.getCriteriaTypeOptions().size());
         queryForm.getQuery().getSubscription().getStudy().getImageSeriesAnnotationCollection().clear();
         assertEquals(2, queryForm.getCriteriaTypeOptions().size());
         testImageSeriesAnnotation = createDefinition("testImageSeriesAnnotation", AnnotationTypeEnum.STRING);
         queryForm.getQuery().getSubscription().getStudy().getImageSeriesAnnotationCollection().add(testImageSeriesAnnotation);
 
         queryForm.getQuery().setId(1L);
         assertTrue(queryForm.isSavedQuery());
         queryForm.getQuery().setId(null);
         assertFalse(queryForm.isSavedQuery());
     }
 
     @Test
     public void testValidation() {
         queryForm.createQuery(subscription);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName(CriterionRowTypeEnum.CLINICAL.getValue());
         group.addCriterion();
         ClinicalCriterionRow criterionRow = (ClinicalCriterionRow) group.getRows().get(0);
         setFieldName(criterionRow, "numericClinicalAnnotation");
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         parameter.setValue("2.0");
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
         parameter.setValue("not a number");
         assertEquals("not a number", parameter.getValue());
         queryForm.validate(validationAware);
         assertTrue(validationAware.hasActionErrors());
     }
     
     @Test
     public void testCriteriaGroup() {
         queryForm.createQuery(subscription);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         assertEquals(BooleanOperatorEnum.AND.getValue(), group.getBooleanOperator());
         group.setBooleanOperator("or");
         assertEquals(BooleanOperatorEnum.OR.getValue(), group.getBooleanOperator());
         group.setCriterionTypeName("");
         assertEquals("", group.getCriterionTypeName());
         group.setCriterionTypeName(CriterionRowTypeEnum.CLINICAL.getValue());
         assertEquals(CriterionRowTypeEnum.CLINICAL.getValue(), group.getCriterionTypeName());
         assertEquals(0, group.getRows().size());
         group.addCriterion();
         assertEquals(1, group.getRows().size());
     }
     
     @Test
     public void testCriterionRow() {
         queryForm.createQuery(subscription);
         CriteriaGroup group = queryForm.getCriteriaGroup();
         group.setCriterionTypeName(CriterionRowTypeEnum.CLINICAL.getValue());
         group.addCriterion();
         ClinicalCriterionRow criterionRow = (ClinicalCriterionRow) group.getRows().get(0);
         checkNewCriterionRow(group, criterionRow);
 
         checkSetNewRowToStringField(criterionRow);
         checkSetToEqualsOperator(criterionRow);
         checkSetStringOperandValue(criterionRow);
         checkChangeStringFieldOperator(criterionRow);
         checkChangeToDifferentStringField(criterionRow);
         checkChangeToSelectField(criterionRow);
         checkChangeToNumericField(criterionRow);
         checkChangeNumericOperator(criterionRow);
         checkAddImageSeriesCriterion(group);
         checkAddGeneExpressionCriterion(group);
         checkRemoveRow(group);
     }
 
     /**
      * @param group
      */
     private void checkRemoveRow(CriteriaGroup group) {
         assertEquals(3, group.getRows().size());
         assertEquals(3, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.getRows().get(2).remove();
         assertEquals(2, group.getRows().size());
         assertEquals(2, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.getRows().get(0).remove();
         assertEquals(1, group.getRows().size());
         assertEquals(1, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
         group.getRows().get(0).remove();
         assertEquals(0, group.getRows().size());
         assertEquals(0, queryForm.getQuery().getCompoundCriterion().getCriterionCollection().size());
     }
 
     @SuppressWarnings("unchecked")
     private void checkChangeToSelectField(ClinicalCriterionRow criterionRow) {
         setFieldName(criterionRow, "selectClinicalAnnotation1");
         SelectedValueCriterion criterion = (SelectedValueCriterion) criterionRow.getCriterion();
         assertEquals(selectClinicalAnnotation1, criterion.getAnnotationDefinition());
         assertEquals(EntityTypeEnum.SUBJECT.getValue(), criterion.getEntityType());
         assertTrue(criterion.getValueCollection().isEmpty());
         assertEquals(1, criterionRow.getParameters().size());
         SelectListParameter<AbstractPermissibleValue> select = (SelectListParameter<AbstractPermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), select.getOperator());
         assertTrue(select.getAvailableOperators().contains(CriterionOperatorEnum.EQUALS.getValue()));
         assertTrue(select.getAvailableOperators().contains(CriterionOperatorEnum.IN.getValue()));
         assertEquals("value1", select.getOptions().get(0).getDisplayValue());
         assertEquals("value2", select.getOptions().get(1).getDisplayValue());
         assertEquals("value3", select.getOptions().get(2).getDisplayValue());
         select.setValue("value2");
         assertEquals(1, criterion.getValueCollection().size());
         assertTrue(criterion.getValueCollection().contains(value2));
         setOperator(select, CriterionOperatorEnum.IN.getValue());
         assertEquals(1, criterionRow.getParameters().size());
         MultiSelectParameter<AbstractPermissibleValue> multiSelect = (MultiSelectParameter<AbstractPermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.IN.getValue(), multiSelect.getOperator());
         assertTrue(multiSelect.getAvailableOperators().contains(CriterionOperatorEnum.EQUALS.getValue()));
         assertTrue(multiSelect.getAvailableOperators().contains(CriterionOperatorEnum.IN.getValue()));
         assertEquals("value1", multiSelect.getOptions().get(0).getDisplayValue());
         assertEquals("value2", multiSelect.getOptions().get(1).getDisplayValue());
         assertEquals("value3", multiSelect.getOptions().get(2).getDisplayValue());
         assertTrue(multiSelect.getValues().length == 1);
         assertTrue(multiSelect.getValues()[0] == "value2");
         multiSelect.setValues(new String[] {"value1", "value3"});
         assertTrue(multiSelect.getValues().length == 2);
         assertTrue(multiSelect.getValues()[0] == "value1");
         assertTrue(multiSelect.getValues()[1] == "value3");
         assertEquals(2, criterion.getValueCollection().size());
         assertTrue(criterion.getValueCollection().contains(value1));
         assertTrue(criterion.getValueCollection().contains(value3));
         multiSelect.setValues(new String[] {"value1", "value3"});
         assertEquals(2, multiSelect.getValues().length);
         assertTrue(multiSelect.getValues()[0] == "value1");
         assertTrue(multiSelect.getValues()[1] == "value3");
         setFieldName(criterionRow, "selectClinicalAnnotation2");
         assertEquals(CriterionOperatorEnum.IN.getValue(), select.getOperator());
         multiSelect = (MultiSelectParameter<AbstractPermissibleValue>) criterionRow.getParameters().get(0);
         assertEquals(1, multiSelect.getOptions().size());
         assertEquals("value2_1", multiSelect.getOptions().get(0).getDisplayValue());
         assertEquals(0, multiSelect.getValues().length);
         multiSelect.setValues(new String[] {"invalid value"});
         assertEquals(0, multiSelect.getValues().length);
     }
 
     private void checkChangeNumericOperator(ClinicalCriterionRow criterionRow) {
         NumericComparisonCriterion criterion = (NumericComparisonCriterion) criterionRow.getCriterion();
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.GREATER_THAN.getValue());
         assertEquals(NumericComparisonOperatorEnum.GREATER.getValue(), criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.GREATER_THAN_OR_EQUAL_TO.getValue());
         assertEquals(NumericComparisonOperatorEnum.GREATEROREQUAL.getValue(), criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.LESS_THAN.getValue());
         assertEquals(NumericComparisonOperatorEnum.LESS.getValue(), criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.LESS_THAN_OR_EQUAL_TO.getValue());
         assertEquals(NumericComparisonOperatorEnum.LESSOREQUAL.getValue(), criterion.getNumericComparisonOperator());
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(NumericComparisonOperatorEnum.EQUAL.getValue(), criterion.getNumericComparisonOperator());
     }
 
     @SuppressWarnings("unchecked")
     private void checkAddGeneExpressionCriterion(CriteriaGroup group) {
         group.setCriterionTypeName(CriterionRowTypeEnum.GENE_EXPRESSION.getValue());
         group.addCriterion();
         assertEquals(3, group.getRows().size());
         assertEquals(2, group.getCompoundCriterion().getCriterionCollection().size());
         GeneExpressionCriterionRow criterionRow = (GeneExpressionCriterionRow) group.getRows().get(2);
         assertEquals(2, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("Gene Name"));
         assertTrue(criterionRow.getAvailableFieldNames().contains("Fold Change"));
         assertEquals(group, criterionRow.getGroup());
         setFieldName(criterionRow, "Gene Name");
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         assertEquals("Gene Name", criterionRow.getFieldName());
         assertTrue(criterionRow.getCriterion() instanceof GeneNameCriterion);
         ((TextFieldParameter) criterionRow.getParameters().get(0)).setValue("EGFR");
         assertEquals("EGFR", ((GeneNameCriterion) criterionRow.getCriterion()).getGeneSymbol());
 
         setFieldName(criterionRow, "Fold Change");
         assertEquals(0, criterionRow.getParameters().get(0).getAvailableOperators().size());
         assertTrue(criterionRow.getCriterion() instanceof FoldChangeCriterion);
         assertEquals(3, criterionRow.getParameters().size());
         TextFieldParameter geneSymbolParameter = ((TextFieldParameter) criterionRow.getParameters().get(0));
         SelectListParameter<RegulationTypeEnum> regulationParameter = ((SelectListParameter<RegulationTypeEnum>) criterionRow.getParameters().get(1));
         geneSymbolParameter.setValue("EGFR");
         regulationParameter.setValue(RegulationTypeEnum.UP.getValue());
         TextFieldParameter foldsUpParameter = ((TextFieldParameter) criterionRow.getParameters().get(2));
         foldsUpParameter.setValue("2.0");
         FoldChangeCriterion foldChangeCriterion = (FoldChangeCriterion) criterionRow.getCriterion();
         assertEquals("EGFR", foldChangeCriterion.getGeneSymbol());
         assertEquals(RegulationTypeEnum.UP, foldChangeCriterion.getRegulationType());
         assertEquals(2.0, foldChangeCriterion.getFoldsUp(), 0.0);
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         
         regulationParameter.setValue(RegulationTypeEnum.UP_OR_DOWN.getValue());
         TextFieldParameter foldsDownParameter = ((TextFieldParameter) criterionRow.getParameters().get(2));
         foldsUpParameter = ((TextFieldParameter) criterionRow.getParameters().get(3));
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
         foldsUpParameter.setValue("not a number");
         assertEquals("not a number", foldsUpParameter.getValue());
         assertEquals(2.0, foldChangeCriterion.getFoldsUp(), 0.0);
         foldsDownParameter.setValue("not a number");
         assertEquals("not a number", foldsDownParameter.getValue());
         assertEquals(2.0, foldChangeCriterion.getFoldsDown(), 0.0);
         queryForm.validate(validationAware);
         assertTrue(validationAware.hasActionErrors());
         
         regulationParameter.setValue(RegulationTypeEnum.UP.getValue());
         assertEquals(3, group.getCompoundCriterion().getCriterionCollection().size());
         regulationParameter.setValue(RegulationTypeEnum.UP_OR_DOWN.getValue());
         assertEquals(4, criterionRow.getParameters().size());
         regulationParameter.setValue(RegulationTypeEnum.DOWN.getValue());
         assertEquals(3, criterionRow.getParameters().size());
         regulationParameter.setValue(RegulationTypeEnum.UNCHANGED.getValue());
         assertEquals(4, criterionRow.getParameters().size());
     }
 
     private void checkAddImageSeriesCriterion(CriteriaGroup group) {
         group.setCriterionTypeName(CriterionRowTypeEnum.IMAGE_SERIES.getValue());
         group.addCriterion();
         ImageSeriesCriterionRow imageSeriesCriterionRow = (ImageSeriesCriterionRow) group.getRows().get(1);
        // TODO - Equal to 2 instead of 3 because we exclude the date type and include identifier.
        assertEquals(2, imageSeriesCriterionRow.getAvailableFieldNames().size());
         assertTrue(imageSeriesCriterionRow.getAvailableFieldNames().contains("testImageSeriesAnnotation"));
         setFieldName(imageSeriesCriterionRow, "testImageSeriesAnnotation");
         assertEquals(group, imageSeriesCriterionRow.getGroup());
         TextFieldParameter textField = (TextFieldParameter) imageSeriesCriterionRow.getParameters().get(0);
         setOperator(textField, CriterionOperatorEnum.EQUALS.getValue());
         textField.setValue("value");
         assertEquals(2, group.getRows().size());
         assertEquals(2, group.getCompoundCriterion().getCriterionCollection().size());
     }
 
     private void checkChangeToNumericField(ClinicalCriterionRow criterionRow) {
         setFieldName(criterionRow, "numericClinicalAnnotation");
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         assertEquals(5 , parameter.getAvailableOperators().size());
         assertEquals("equals", parameter.getOperator());
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), parameter.getOperator());
         assertTrue(criterionRow.getCriterion() instanceof NumericComparisonCriterion);
         assertEquals(1, criterionRow.getGroup().getCompoundCriterion().getCriterionCollection().size());
         assertTrue(criterionRow.getGroup().getCompoundCriterion().getCriterionCollection().iterator().next() instanceof NumericComparisonCriterion);
     }
 
 
     private void checkChangeToDifferentStringField(ClinicalCriterionRow criterionRow) {
         setFieldName(criterionRow, "stringClinicalAnnotation2");
         assertEquals("stringClinicalAnnotation2", criterionRow.getFieldName());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING.getValue(), criterion.getWildCardType());
         assertEquals("value", ((TextFieldParameter) criterionRow.getParameters().get(0)).getValue());
         assertEquals("value", criterion.getStringValue());
         assertEquals(stringClinicalAnnotation2, criterion.getAnnotationDefinition());
     }
 
     private void checkChangeStringFieldOperator(ClinicalCriterionRow criterionRow) {
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.BEGINS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_AFTER_STRING.getValue(), criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.ENDS_WITH.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_STRING.getValue(), criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_OFF.getValue(), criterion.getWildCardType());
         setOperator(parameter, CriterionOperatorEnum.CONTAINS.getValue());
         assertEquals(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING.getValue(), criterion.getWildCardType());
         assertEquals("value", ((TextFieldParameter) criterionRow.getParameters().get(0)).getValue());
         assertEquals("value", criterion.getStringValue());
     }
     
     private void setOperator(AbstractCriterionParameter parameter, String value) {
         parameter.setOperator(value);
         queryForm.processCriteriaChanges();
     }
     
     private void setFieldName(AbstractCriterionRow row, String name) {
         row.setFieldName(name);
         queryForm.processCriteriaChanges();
     }
 
     private void checkSetStringOperandValue(ClinicalCriterionRow criterionRow) {
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         parameter.setValue("value");
         assertEquals("value", parameter.getValue());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertEquals("value", criterion.getStringValue());
     }
 
     private void checkNewCriterionRow(CriteriaGroup group, ClinicalCriterionRow criterionRow) {
        // TODO - Equal to 6 instead of 7 because we exclude the date type and include the identifier.
        assertEquals(6, criterionRow.getAvailableFieldNames().size());
         assertTrue(criterionRow.getAvailableFieldNames().contains("stringClinicalAnnotation1"));
         assertEquals(group, criterionRow.getGroup());
         assertEquals(0 , criterionRow.getParameters().size());
         assertEquals("", criterionRow.getFieldName());
     }
 
     private void checkSetToEqualsOperator(ClinicalCriterionRow criterionRow) {
         assertEquals(1, criterionRow.getParameters().size());
         assertTrue(criterionRow.getParameters().get(0) instanceof TextFieldParameter);
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         setOperator(parameter, CriterionOperatorEnum.EQUALS.getValue());
         assertEquals(CriterionOperatorEnum.EQUALS.getValue(), parameter.getOperator());
         StringComparisonCriterion criterion = (StringComparisonCriterion) criterionRow.getCriterion();
         assertNotNull(criterion);
         assertEquals(WildCardTypeEnum.WILDCARD_OFF.getValue(), criterion.getWildCardType());
         assertEquals(stringClinicalAnnotation1, criterion.getAnnotationDefinition());
         assertEquals(EntityTypeEnum.SUBJECT.getValue(), criterion.getEntityType());
     }
 
     private void checkSetNewRowToStringField(ClinicalCriterionRow criterionRow) {
         setFieldName(criterionRow, "stringClinicalAnnotation1");
         assertEquals("stringClinicalAnnotation1", criterionRow.getFieldName());
         assertEquals(4 , criterionRow.getParameters().get(0).getAvailableOperators().size());
     }
 
     @SuppressWarnings("unchecked")
     @Test
     public void testSetQuery() {
         Query query = new Query();
         query.setSubscription(subscription);
         CompoundCriterion compoundCriterion = new CompoundCriterion();
         compoundCriterion.setId(1L);
         compoundCriterion.setBooleanOperator(BooleanOperatorEnum.OR.getValue());
         query.setCompoundCriterion(compoundCriterion);
         compoundCriterion.setCriterionCollection(new ArrayList<AbstractCriterion>());
         query.setColumnCollection(new HashSet<ResultColumn>());
         
         StringComparisonCriterion stringCriterion = new StringComparisonCriterion();
         stringCriterion.setId(1L);
         stringCriterion.setAnnotationDefinition(stringClinicalAnnotation1);
         stringCriterion.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         stringCriterion.setStringValue("value");
         stringCriterion.setWildCardType(WildCardTypeEnum.WILDCARD_BEFORE_AND_AFTER_STRING.getValue());
         compoundCriterion.getCriterionCollection().add(stringCriterion);
 
         FoldChangeCriterion foldChangeCriterion = new FoldChangeCriterion();
         foldChangeCriterion.setFoldsDown(3.0f);
         foldChangeCriterion.setRegulationType(RegulationTypeEnum.DOWN);
         foldChangeCriterion.setId(2L);
         compoundCriterion.getCriterionCollection().add(foldChangeCriterion);
         
         GeneNameCriterion geneNameCriterion = new GeneNameCriterion();
         geneNameCriterion.setGeneSymbol("EGFR");
         geneNameCriterion.setId(3L);
         compoundCriterion.getCriterionCollection().add(geneNameCriterion);
 
         ResultColumn subjectColumn1 = new ResultColumn();
         subjectColumn1.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         subjectColumn1.setAnnotationDefinition(stringClinicalAnnotation1);
         subjectColumn1.setColumnIndex(2);
         query.getColumnCollection().add(subjectColumn1);
 
         ResultColumn subjectColumn2 = new ResultColumn();
         subjectColumn2.setEntityType(EntityTypeEnum.SUBJECT.getValue());
         subjectColumn2.setAnnotationDefinition(numericClinicalAnnotation);
         subjectColumn2.setColumnIndex(1);
         query.getColumnCollection().add(subjectColumn2);
 
         ResultColumn imageSeriesColumn = new ResultColumn();
         imageSeriesColumn.setEntityType(EntityTypeEnum.IMAGESERIES.getValue());
         imageSeriesColumn.setAnnotationDefinition(testImageSeriesAnnotation);
         imageSeriesColumn.setColumnIndex(0);
         query.getColumnCollection().add(imageSeriesColumn);
         
         queryForm.setQuery(query);
         assertNotNull(queryForm.getQuery());
         CriteriaGroup group = queryForm.getCriteriaGroup();
         assertNotNull(group);
         assertEquals(compoundCriterion, group.getCompoundCriterion());
         assertEquals(BooleanOperatorEnum.OR.getValue(), group.getBooleanOperator());
         assertEquals(3, group.getRows().size());
         
         ClinicalCriterionRow criterionRow = (ClinicalCriterionRow) group.getRows().get(0);
         assertEquals(stringCriterion, criterionRow.getCriterion());
         assertEquals(stringClinicalAnnotation1.getDisplayName(), criterionRow.getFieldName());
         TextFieldParameter parameter = (TextFieldParameter) criterionRow.getParameters().get(0);
         assertEquals(CriterionOperatorEnum.CONTAINS.getValue(), parameter.getOperator());
         assertEquals("value", parameter.getValue());
         assertEquals("queryForm.criteriaGroup.rows[0].parameters[0]", parameter.getFormFieldName());
         
         GeneExpressionCriterionRow foldChangeRow = (GeneExpressionCriterionRow) group.getRows().get(1);
         assertEquals(foldChangeCriterion, foldChangeRow.getCriterion());
         assertEquals(FoldChangeCriterionWrapper.FOLD_CHANGE, foldChangeRow.getFieldName());
         SelectListParameter<String> regulationParameter = (SelectListParameter<String>) foldChangeRow.getParameters().get(1);
         TextFieldParameter foldsParameter = (TextFieldParameter) foldChangeRow.getParameters().get(2);
         assertEquals("Down", regulationParameter.getValue());
         assertEquals("3.0", foldsParameter.getValue());
         
         GeneExpressionCriterionRow geneNameRow = (GeneExpressionCriterionRow) group.getRows().get(2);
         assertEquals(geneNameCriterion, geneNameRow.getCriterion());
         assertEquals(GeneNameCriterionWrapper.FIELD_NAME, geneNameRow.getFieldName());
         TextFieldParameter geneNameParameter = (TextFieldParameter) geneNameRow.getParameters().get(0);
         assertEquals("EGFR", geneNameParameter.getValue());
         
         assertEquals(3, queryForm.getResultConfiguration().getSelectedColumns().size());
         assertEquals(testImageSeriesAnnotation, queryForm.getResultConfiguration().getSelectedColumns().get(0).getAnnotationDefinition());
         assertEquals(numericClinicalAnnotation, queryForm.getResultConfiguration().getSelectedColumns().get(1).getAnnotationDefinition());
         assertEquals(stringClinicalAnnotation1, queryForm.getResultConfiguration().getSelectedColumns().get(2).getAnnotationDefinition());
         
         assertEquals(5, queryForm.getResultConfiguration().getSubjectColumns().getColumnList().getOptions().size());
         List<String> columnNames = Arrays.asList(queryForm.getResultConfiguration().getSubjectColumns().getValues());
         assertEquals(2, columnNames.size());
         assertTrue(columnNames.contains("stringClinicalAnnotation1"));
         assertTrue(columnNames.contains("numericClinicalAnnotation"));
         assertEquals(1, queryForm.getResultConfiguration().getImageSeriesColumns().getColumnList().getOptions().size());
         assertEquals(1, queryForm.getResultConfiguration().getImageSeriesColumns().getValues().length);
 
         queryForm.getResultConfiguration().getImageSeriesColumns().setValues(new String[0]);
         queryForm.getResultConfiguration().getSubjectColumns().setValues(new String[] {"stringClinicalAnnotation2"});
         assertEquals(1, queryForm.getResultConfiguration().getSubjectColumns().getValues().length);
         assertEquals(1, query.getColumnCollection().size());
         ResultColumn column = query.getColumnCollection().iterator().next();
         assertEquals(EntityTypeEnum.SUBJECT.getValue(), column.getEntityType());
         assertEquals(stringClinicalAnnotation2, column.getAnnotationDefinition());
         assertEquals(0, (int) column.getColumnIndex());
         assertEquals(1, queryForm.getResultConfiguration().getColumnIndex("stringClinicalAnnotation2"));
         assertEquals(1, queryForm.getResultConfiguration().getSelectedColumns().size());
         assertEquals(stringClinicalAnnotation2, queryForm.getResultConfiguration().getSelectedColumns().get(0).getAnnotationDefinition());
 
         ValidationAwareSupport validationAware = new ValidationAwareSupport();
         queryForm.validate(validationAware);
         assertFalse(validationAware.hasFieldErrors());
     }
 
     @Test
     public void testIsPotentialLargeQuery() {
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.createQuery(subscription);
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.getQuery().setResultType("genomic");
         assertTrue(queryForm.isPotentiallyLargeQuery());
         queryForm.getCriteriaGroup().setCriterionTypeName("Gene Expression");
         queryForm.getCriteriaGroup().addCriterion();
         queryForm.getCriteriaGroup().getRows().get(0).setCriterion(new GeneNameCriterion());
         assertFalse(queryForm.isPotentiallyLargeQuery());
         queryForm.getCriteriaGroup().getRows().get(0).setCriterion(new FoldChangeCriterion());
         assertFalse(queryForm.isPotentiallyLargeQuery());
     }
 }
