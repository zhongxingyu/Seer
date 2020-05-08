 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.web.action.query.form;
 
 import gov.nih.nci.caintegrator.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator.application.study.AnnotationGroup;
 import gov.nih.nci.caintegrator.application.study.StudyManagementService;
 import gov.nih.nci.caintegrator.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator.domain.application.Query;
 import gov.nih.nci.caintegrator.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 import gov.nih.nci.caintegrator.security.SecurityHelper;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ValidationAware;
 
 /**
  * Top-level UI element for query management.
  */
 public class QueryForm {
     private static final int QUERY_NAME_LENGTH = 100;
     private static final int QUERY_DESCRIPTION_LENGTH = 200;
 
     private StudyManagementService studyManagementService;
     private Query query;
     private final List<AnnotationGroup> sortedAnnotationGroups = new ArrayList<AnnotationGroup>();
     private final List<String> annotationGroupNames = new ArrayList<String>();
     private final List<String> geneExpressionPlatformNames = new ArrayList<String>();
     private final List<String> copyNumberPlatformNames = new ArrayList<String>();
     private final List<String> copyNumberPlatformNamesWithCghCall = new ArrayList<String>();
     private final Map<String, AnnotationFieldDescriptorList> annotationGroupMap =
         new HashMap<String, AnnotationFieldDescriptorList>();
     private CriteriaGroup criteriaGroup;
     private ResultConfiguration resultConfiguration;
     private String orgQueryName = "";
     private boolean controlSamplesInStudy = false;
     private boolean studyHasSavedLists = false;
     private boolean studyHasMultipleGeneExpressionPlatforms = false;
     private boolean studyHasMultipleCopyNumberPlatforms = false;
     private boolean studyHasMultipleCopyNumberPlatformsWithCghCall = false;
 
     private String genomicPreviousSorting;
     private int genomicSortingOrder = -1;
 
     /**
      * Configures a new query.
      *
      * @param subscription query belongs to this subscription
      * @param geneExpressionPlatformsInStudy the gene expression platforms in the study (null if unknown)
      * @param copyNumberPlatformsInStudy the copy number platforms in the study (null if unknown)
      * @param copyNumberPlatformsWithCghCallInStudy the copy number platforms with CghCall in the study
      *           (null if unknown)
      */
     public void createQuery(StudySubscription subscription, Set<String> geneExpressionPlatformsInStudy,
             Set<String> copyNumberPlatformsInStudy, Set<String> copyNumberPlatformsWithCghCallInStudy) {
         query = new Query();
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setCriterionCollection(new HashSet<AbstractCriterion>());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND);
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setSubscription(subscription);
         query.setResultType(ResultTypeEnum.CLINICAL);
         setQuery(query, geneExpressionPlatformsInStudy, copyNumberPlatformsInStudy,
                 copyNumberPlatformsWithCghCallInStudy);
         setResultConfiguration(new ResultConfiguration(this));
     }
 
     private void initialize(Set<String> geneExpressionPlatformsInStudy, Set<String> copyNumberPlatformsInStudy,
             Set<String> copyNumberPlatformsWithCghCallInStudy) {
         studyHasMultipleGeneExpressionPlatforms = false;
         studyHasMultipleCopyNumberPlatforms = false;
         studyHasMultipleCopyNumberPlatformsWithCghCall = false;
         geneExpressionPlatformNames.clear();
         copyNumberPlatformNames.clear();
         if (query != null) {
             Study study = getQuery().getSubscription().getStudy();
             initializeAnnotationGroups(study);
             setupPlatforms(geneExpressionPlatformsInStudy, copyNumberPlatformsInStudy,
                     copyNumberPlatformsWithCghCallInStudy);
             criteriaGroup = new CriteriaGroup(this);
             resultConfiguration = new ResultConfiguration(this);
             controlSamplesInStudy = study.getStudyConfiguration().hasControlSamples();
            studyHasSavedLists = hasSavedSubjectLists(study);
         } else {
             criteriaGroup = null;
             resultConfiguration = null;
             annotationGroupNames.clear();
             annotationGroupMap.clear();
             sortedAnnotationGroups.clear();
         }
         orgQueryName = "";
     }
 
    private boolean hasSavedSubjectLists(Study study) {
        return !getQuery().getSubscription().getSubjectLists().isEmpty()
                || !study.getStudyConfiguration().getSubjectLists().isEmpty();
    }

     private void setupPlatforms(Set<String> geneExpressionPlatformsInStudy, Set<String> copyNumberPlatformsInStudy,
             Set<String> copyNumberPlatformsWithCghCallInStudy) {
         setupGeneExpressionPlatforms(geneExpressionPlatformsInStudy);
         setupCopyNumberPlatforms(copyNumberPlatformsInStudy);
         setupCopyNumberPlatformsWithCghCall(copyNumberPlatformsWithCghCallInStudy);
     }
 
     private void setupGeneExpressionPlatforms(Set<String> geneExpressionPlatformsInStudy) {
         if (geneExpressionPlatformsInStudy != null && geneExpressionPlatformsInStudy.size() > 1) {
             studyHasMultipleGeneExpressionPlatforms = true;
             geneExpressionPlatformNames.addAll(geneExpressionPlatformsInStudy);
             Collections.sort(geneExpressionPlatformNames);
         }
     }
 
     private void setupCopyNumberPlatforms(Set<String> copyNumberPlatformsInStudy) {
         if (copyNumberPlatformsInStudy != null && copyNumberPlatformsInStudy.size() > 1) {
             studyHasMultipleCopyNumberPlatforms = true;
             copyNumberPlatformNames.addAll(copyNumberPlatformsInStudy);
             Collections.sort(copyNumberPlatformNames);
         }
     }
 
     private void setupCopyNumberPlatformsWithCghCall(Set<String> copyNumberPlatformsWithCghCallInStudy) {
         if (copyNumberPlatformsWithCghCallInStudy != null && copyNumberPlatformsWithCghCallInStudy.size() > 1) {
             studyHasMultipleCopyNumberPlatformsWithCghCall = true;
             copyNumberPlatformNamesWithCghCall.addAll(copyNumberPlatformsWithCghCallInStudy);
             Collections.sort(copyNumberPlatformNamesWithCghCall);
         }
     }
 
     private void initializeAnnotationGroups(Study study) {
         annotationGroupNames.clear();
         String username = SecurityHelper.getCurrentUsername();
         Set<AnnotationGroup> groupsWithNoVisibleFieldDescriptors = new HashSet<AnnotationGroup>();
         for (AnnotationGroup group : study.getAnnotationGroups()) {
             Set<AnnotationFieldDescriptor> userAnnotationFields = studyManagementService
                 .getVisibleAnnotationFieldDescriptorsForUser(group, username);
             if (!userAnnotationFields.isEmpty()) {
                 annotationGroupMap.put(group.getName(), new AnnotationFieldDescriptorList(userAnnotationFields));
                 annotationGroupNames.add(group.getName());
             } else {
                 groupsWithNoVisibleFieldDescriptors.add(group);
             }
         }
         sortedAnnotationGroups.clear();
         sortedAnnotationGroups.addAll(study.getSortedAnnotationGroups());
         sortedAnnotationGroups.removeAll(groupsWithNoVisibleFieldDescriptors);
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
      * @param q the query for the form
      * @param geneExpressionPlatformsInStudy the gene expression platforms in the study (null if unknown)
      * @param copyNumberPlatformsInStudy the copy number platforms in the study (null if unknown)
      * @param copyNumberPlatformsWithCghCallInStudy the copy number platforms with CghCallin the study (null if unknown)
      */
     public void setQuery(Query q, Set<String> geneExpressionPlatformsInStudy,
             Set<String> copyNumberPlatformsInStudy, Set<String> copyNumberPlatformsWithCghCallInStudy) {
         this.query = q;
         initialize(geneExpressionPlatformsInStudy, copyNumberPlatformsInStudy, copyNumberPlatformsWithCghCallInStudy);
     }
 
     AnnotationFieldDescriptorList getAnnotations(String groupName) {
         return annotationGroupMap.get(groupName);
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
         validateQueryDescription(action);
     }
 
     private void validateQueryName(ValidationAware action) {
         if (StringUtils.isBlank(getQuery().getName())) {
             action.addActionError("Query Name is required.");
         } else if (StringUtils.length(getQuery().getName()) > QUERY_NAME_LENGTH) {
             action.addActionError("Query Name must be " + QUERY_NAME_LENGTH + " characters or less.");
         } else {
             validateUniqueQueryName(action);
         }
     }
 
     private void validateQueryDescription(ValidationAware action) {
         if (StringUtils.length(getQuery().getDescription()) > QUERY_DESCRIPTION_LENGTH) {
             action.addActionError("Query Description must be " + QUERY_DESCRIPTION_LENGTH + " characters or less.");
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
         for (AnnotationGroup group : sortedAnnotationGroups) {
             options.add(group.getName());
         }
         if (study.hasExpressionData()) {
             options.add(CriterionRowTypeEnum.GENE_EXPRESSION.getValue());
         }
         if (study.hasCopyNumberData()) {
             options.add(CriterionRowTypeEnum.COPY_NUMBER.getValue());
         }
         options.add(CriterionRowTypeEnum.UNIQUE_IDENTIIFER.getValue());
         if (studyHasSavedLists) {
             options.add(CriterionRowTypeEnum.SAVED_LIST.getValue());
         }
         return options;
     }
 
     /**
      * Check if search result is gene expression and all genes are going to be searched.
      * @return boolean of potentially a large query
      */
     public boolean isPotentiallyLargeQuery() {
         if (query != null && query.isGeneExpressionResultType()) {
             return criteriaGroup.hasNoGeneExpressionCriterion();
         }
         return false;
     }
 
     /**
      * @return the orgQueryName
      */
     public String getOrgQueryName() {
         return orgQueryName;
     }
 
     /**
      * @param orgQueryName the orgSaveName to set
      */
     public void setOrgQueryName(String orgQueryName) {
         this.orgQueryName = orgQueryName;
     }
 
     /**
      * @return boolean of has genomic data source
      */
     public boolean hasGenomicDataSources() {
         return query.getSubscription().getStudy().hasGenomicDataSources();
     }
 
     /**
      * @return boolean of has image mapping data
      */
     public boolean hasImageDataSources() {
         return query.getSubscription().getStudy().hasImageDataSources();
     }
 
     /**
      * @return boolean of has image mapping data
      */
     public boolean hasCopyNumberData() {
         return query.getSubscription().getStudy().hasCopyNumberData();
     }
 
     /**
      * @return boolean of has image mapping data
      */
     public boolean hasExpressionData() {
         return query.getSubscription().getStudy().hasExpressionData();
     }
 
     /**
      * @return the controlSamplesInStudy
      */
     public boolean isControlSamplesInStudy() {
         return controlSamplesInStudy;
     }
 
     /**
      * @return the annotationGroupNames
      */
     public List<String> getAnnotationGroupNames() {
         return annotationGroupNames;
     }
 
     /**
      * @return the genomicPreviousSorting
      */
     public String getGenomicPreviousSorting() {
         return genomicPreviousSorting;
     }
 
     /**
      * @param genomicPreviousSorting the genomicPreviousSorting to set
      */
     public void setGenomicPreviousSorting(String genomicPreviousSorting) {
         this.genomicPreviousSorting = genomicPreviousSorting;
     }
 
     /**
      * @return the genomicSortingOrder
      */
     public int getGenomicSortingOrder() {
         return genomicSortingOrder;
     }
 
     /**
      * @param genomicSortingOrder the genomicSortingOrder to set
      */
     public void setGenomicSortingOrder(int genomicSortingOrder) {
         this.genomicSortingOrder = genomicSortingOrder;
     }
 
     /**
      * Reverse the sorting order of the genomic results.
      */
     public void reverseGenomicSortingOrder() {
         this.genomicSortingOrder *= -1;
     }
 
     /**
      * @return the studyHasMultipleGeneExpressionPlatforms
      */
     public boolean isStudyHasMultipleGeneExpressionPlatforms() {
         return studyHasMultipleGeneExpressionPlatforms;
     }
 
     /**
      * @return the studyHasMultipleCopyNumberPlatforms
      */
     public boolean isStudyHasMultipleCopyNumberPlatforms() {
         return studyHasMultipleCopyNumberPlatforms;
     }
 
     /**
      * @return the studyHasMultipleCopyNumberPlatforms
      */
     public boolean isStudyHasMultipleCopyNumberPlatformsWithCghCall() {
         return studyHasMultipleCopyNumberPlatformsWithCghCall;
     }
 
     /**
      * @return the geneExpressionPlatformNames
      */
     public List<String> getGeneExpressionPlatformNames() {
         return geneExpressionPlatformNames;
     }
 
     /**
      * @return the copyNumberPlatformNames
      */
     public List<String> getCopyNumberPlatformNames() {
         return copyNumberPlatformNames;
     }
 
     /**
      * @return the copyNumberPlatformNames
      */
     public List<String> getCopyNumberPlatformNamesWithCghCall() {
         return copyNumberPlatformNamesWithCghCall;
     }
 
     /**
      * @return available result types for this study
      */
     public Map<String, String> getResultTypes() {
         Map<String, String> resultTypes = ResultTypeEnum.getValueToDisplayableMap();
         checkHasCopyNumberData(resultTypes);
         checkHasExpressionData(resultTypes);
         checkHasGenomicData(resultTypes);
         checkHasCghCalls(resultTypes);
         return resultTypes;
     }
 
     private void checkHasGenomicData(Map<String, String> resultTypes) {
         if (!hasCopyNumberData() && !hasExpressionData()) {
             resultTypes.remove(ResultTypeEnum.IGV_VIEWER.getValue());
         }
     }
 
     private void checkHasExpressionData(Map<String, String> resultTypes) {
         if (!hasExpressionData()) {
             resultTypes.remove(ResultTypeEnum.GENE_EXPRESSION.getValue());
         }
     }
 
     private void checkHasCopyNumberData(Map<String, String> resultTypes) {
         if (!hasCopyNumberData()) {
             resultTypes.remove(ResultTypeEnum.COPY_NUMBER.getValue());
             resultTypes.remove(ResultTypeEnum.HEATMAP_VIEWER.getValue());
         }
     }
 
     private void checkHasCghCalls(Map<String, String> resultTypes) {
         if (query.getSubscription().getStudy().hasCghCalls()) {
             resultTypes.remove(ResultTypeEnum.HEATMAP_VIEWER.getValue());
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
     @Autowired
     public void setStudyManagementService(StudyManagementService studyManagementService) {
         this.studyManagementService = studyManagementService;
     }
 }
