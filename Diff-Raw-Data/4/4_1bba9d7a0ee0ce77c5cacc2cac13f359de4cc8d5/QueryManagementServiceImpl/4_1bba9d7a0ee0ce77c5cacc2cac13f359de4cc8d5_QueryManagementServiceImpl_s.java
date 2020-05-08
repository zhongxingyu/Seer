 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.application.query;
 
 import gov.nih.nci.caintegrator.application.CaIntegrator2BaseService;
 import gov.nih.nci.caintegrator.application.analysis.geneexpression.GenesNotFoundInStudyException;
 import gov.nih.nci.caintegrator.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator.application.arraydata.PlatformDataTypeEnum;
 import gov.nih.nci.caintegrator.common.Cai2Util;
 import gov.nih.nci.caintegrator.common.QueryUtil;
 import gov.nih.nci.caintegrator.domain.application.AbstractAnnotationCriterion;
 import gov.nih.nci.caintegrator.domain.application.AbstractCriterion;
 import gov.nih.nci.caintegrator.domain.application.BooleanOperatorEnum;
 import gov.nih.nci.caintegrator.domain.application.CompoundCriterion;
 import gov.nih.nci.caintegrator.domain.application.GenomicCriterionTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.GenomicDataQueryResult;
 import gov.nih.nci.caintegrator.domain.application.Query;
 import gov.nih.nci.caintegrator.domain.application.QueryResult;
 import gov.nih.nci.caintegrator.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator.domain.application.ResultTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.StudySubscription;
 import gov.nih.nci.caintegrator.domain.application.SubjectList;
 import gov.nih.nci.caintegrator.domain.application.SubjectListCriterion;
 import gov.nih.nci.caintegrator.domain.genomic.Platform;
 import gov.nih.nci.caintegrator.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 import gov.nih.nci.caintegrator.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator.external.ncia.NCIABasket;
 import gov.nih.nci.caintegrator.external.ncia.NCIADicomJob;
 import gov.nih.nci.caintegrator.external.ncia.NCIAImageAggregationTypeEnum;
 import gov.nih.nci.caintegrator.external.ncia.NCIAImageAggregator;
 import gov.nih.nci.caintegrator.file.FileManager;
 import gov.nih.nci.caintegrator.web.action.query.DisplayableResultRow;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Implementation of the QueryManagementService interface.
  */
 @Service("queryManagementService")
 @Transactional(propagation = Propagation.REQUIRED)
 public class QueryManagementServiceImpl extends CaIntegrator2BaseService implements QueryManagementService {
 
     private ResultHandler resultHandler;
     private ArrayDataService arrayDataService;
     private FileManager fileManager;
 
     /**
      * @param resultHandler the resultHandler to set
      */
     @Autowired
     public void setResultHandler(ResultHandler resultHandler) {
         this.resultHandler = resultHandler;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public QueryResult execute(Query query) throws InvalidCriterionException {
         return new QueryTranslator(retrieveQueryToExecute(query), getDao(), arrayDataService, resultHandler).execute();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public GenomicDataQueryResult executeGenomicDataQuery(Query query) throws InvalidCriterionException {
         return new GenomicQueryHandler(retrieveQueryToExecute(query), getDao(), arrayDataService).execute();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @Transactional(readOnly = true)
     public Collection<SegmentData> retrieveSegmentDataQuery(Query query) throws InvalidCriterionException {
         return new GenomicQueryHandler(retrieveQueryToExecute(query),
                 getDao(), arrayDataService).retrieveSegmentDataQuery();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Query retrieveQueryToExecute(Query query) throws InvalidCriterionException {
         try {
             query.getCompoundCriterion().validateGeneExpressionCriterion();
             if (QueryUtil.isQueryGenomic(query)) {
                 addPlatformToQuery(query);
             }
             Query queryToExecute = query.clone();
             addGenesNotFoundToQuery(query);
             addSubjectsNotFoundToQuery(query);
             query.setHasMaskedValues(queryToExecute.getCompoundCriterion().isHasMaskedCriteria());
             if (query.isHasMaskedValues()) {
                 maskCompoundCriterion(queryToExecute.getCompoundCriterion());
             }
             checkCriterionColumnsForMasks(query);
             return queryToExecute;
         } catch (CloneNotSupportedException e) {
             throw new IllegalStateException("Unable to clone query.");
         }
     }
 
    private void addPlatformToQuery(Query query) throws InvalidCriterionException {
         if (QueryUtil.isQueryCopyNumber(query)) {
             query.setCopyNumberPlatform(
                 retrievePlatform(query, retrieveCopyNumberPlatformsForStudy(query.getSubscription().getStudy()),
                         GenomicCriterionTypeEnum.COPY_NUMBER));
         }
         if (QueryUtil.isQueryGeneExpression(query)) {
             query.setGeneExpressionPlatform(
                 retrievePlatform(query, retrieveGeneExpressionPlatformsForStudy(query.getSubscription().getStudy()),
                         GenomicCriterionTypeEnum.GENE_EXPRESSION));
         }
     }
 
     private Platform retrievePlatform(Query query, Set<String> platformNames,
             GenomicCriterionTypeEnum genomicCriterionType) throws InvalidCriterionException {
         if (platformNames.size() == 1) {
             return getDao().getPlatform(platformNames.iterator().next());
         } else {
             Set<String> allPlatformNames = query.getCompoundCriterion().getAllPlatformNames(genomicCriterionType);
             if (allPlatformNames.size() != 1) {
                throw new InvalidCriterionException("A " + genomicCriterionType.getValue()
                        + " query must contain exactly 1 platform of type "
                        + genomicCriterionType.getValue() + ".  This one contains "
                        + allPlatformNames.size() + " platforms.  "
                        + "Create a query and within the criterion, select a platform.");
             }
             return getDao().getPlatform(allPlatformNames.iterator().next());
         }
     }
 
     private void checkCriterionColumnsForMasks(Query query) {
         if (!query.isHasMaskedValues()) {
             for (ResultColumn column : query.getColumnCollection()) {
                 if (column.getAnnotationFieldDescriptor() != null
                      && !column.getAnnotationFieldDescriptor().getAnnotationMasks().isEmpty()) {
                     query.setHasMaskedValues(true);
                     break;
                 }
             }
         }
     }
 
     private void maskCompoundCriterion(CompoundCriterion compoundCriterion) {
         Set<AbstractCriterion> criterionToRemove = new HashSet<AbstractCriterion>();
         Set<AbstractCriterion> criterionToAdd = new HashSet<AbstractCriterion>();
         for (AbstractCriterion criterion : compoundCriterion.getCriterionCollection()) {
             if (!(criterion instanceof CompoundCriterion)) {
                 AbstractCriterion newCriterion = retrieveMaskedCriterion(criterion);
                 if (!criterion.equals(newCriterion)) {
                     criterionToAdd.add(newCriterion);
                     criterionToRemove.add(criterion);
                 }
             } else {
                 maskCompoundCriterion((CompoundCriterion) criterion);
             }
         }
         compoundCriterion.getCriterionCollection().addAll(criterionToAdd);
         compoundCriterion.getCriterionCollection().removeAll(criterionToRemove);
     }
 
     private AbstractCriterion retrieveMaskedCriterion(AbstractCriterion abstractCriterion) {
         if (abstractCriterion instanceof AbstractAnnotationCriterion
                 && ((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor() != null
                 && !((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor()
                     .getAnnotationMasks().isEmpty()) {
                 return MaskHandlerUtils.createMaskedCriterion(
                         ((AbstractAnnotationCriterion) abstractCriterion).getAnnotationFieldDescriptor()
                                 .getAnnotationMasks(), abstractCriterion);
             }
         return abstractCriterion;
     }
 
     private void addSubjectsNotFoundToQuery(Query query) throws InvalidCriterionException {
         query.getSubjectIdsNotFound().clear();
         query.getSubjectIdsNotFound().addAll(getAllSubjectsNotFoundInCriteria(query));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<String> getAllSubjectsNotFoundInCriteria(Query query) throws InvalidCriterionException {
         if (query == null) {
             return new HashSet<String>();
         }
         Set<String> allSubjectsInCriterion = query.getCompoundCriterion().getAllSubjectIds();
         Set<String> allSubjectsNotFound = new HashSet<String>(allSubjectsInCriterion);
         if (!allSubjectsInCriterion.isEmpty()) {
             Set<String> allSubjectsInStudy = getAllSubjectsInStudy(query);
             allSubjectsNotFound.removeAll(allSubjectsInStudy);
             if (areNoQuerySubjectsInStudy(allSubjectsInCriterion, allSubjectsNotFound)) {
                 String queryNameString = StringUtils.isNotBlank(query.getName()) ? "'" + query.getName() + "'  " : "";
                 throw new InvalidCriterionException("None of the Subject IDs in the query " + queryNameString
                         + "were found in the study.");
             }
         }
         return allSubjectsNotFound;
     }
 
     private Set<String> getAllSubjectsInStudy(Query query) {
         Set<String> allSubjectsInStudy = new HashSet<String>();
         for (StudySubjectAssignment assignment : query.getSubscription().getStudy().getAssignmentCollection()) {
             allSubjectsInStudy.add(assignment.getIdentifier());
         }
         return allSubjectsInStudy;
     }
 
     private boolean areNoQuerySubjectsInStudy(Set<String> allSubjectsInCriterion, Set<String> allSubjectsNotFound) {
         return !allSubjectsNotFound.isEmpty() && allSubjectsNotFound.size() == allSubjectsInCriterion.size();
     }
 
     private void addGenesNotFoundToQuery(Query query) throws InvalidCriterionException {
         List<String> allGeneSymbols = query.getCompoundCriterion().getAllGeneSymbols();
         query.getGeneSymbolsNotFound().clear();
         if (!allGeneSymbols.isEmpty() && !isQueryOnAllGenes(allGeneSymbols)) {
             try {
                 query.getGeneSymbolsNotFound().addAll(validateGeneSymbols(query.getSubscription(), allGeneSymbols));
             } catch (GenesNotFoundInStudyException e) {
                 throw new InvalidCriterionException(e.getMessage(), e);
             }
         }
     }
 
     private boolean isQueryOnAllGenes(Collection<String> allGeneSymbols) {
         if (allGeneSymbols.contains("")) {
             return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<String> retrieveGeneExpressionPlatformsForStudy(Study study) {
         Set<String> platformsInStudy = new HashSet<String>();
         for (Platform platform : arrayDataService.getPlatformsInStudy(study, PlatformDataTypeEnum.EXPRESSION)) {
             platformsInStudy.add(platform.getName());
         }
         return platformsInStudy;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<String> retrieveCopyNumberPlatformsForStudy(Study study) {
         Set<String> platformsInStudy = new HashSet<String>();
         for (Platform platform : arrayDataService.getPlatformsInStudy(
                 study, PlatformDataTypeEnum.COPY_NUMBER)) {
             platformsInStudy.add(platform.getName());
         }
         return platformsInStudy;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Set<String> retrieveCopyNumberPlatformsWithCghCallForStudy(Study study) {
         Set<String> platformsInStudy = new HashSet<String>();
         for (Platform platform : arrayDataService.getPlatformsWithCghCallInStudy(
                 study, PlatformDataTypeEnum.COPY_NUMBER)) {
             platformsInStudy.add(platform.getName());
         }
         return platformsInStudy;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Query> createQueriesFromSubjectLists(StudySubscription subscription) {
         List<Query> queries = new ArrayList<Query>();
         for (SubjectList subjectList : subscription.getStudy().getStudyConfiguration().getSubjectLists()) {
             queries.add(createQueryFromSubjectList(subscription, subjectList));
         }
         for (SubjectList subjectList : subscription.getSubjectLists()) {
             queries.add(createQueryFromSubjectList(subscription, subjectList));
         }
         return queries;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Query createQueryFromSubjectList(StudySubscription subscription, SubjectList subjectList) {
         Query query = new Query();
         SubjectListCriterion criterion = new SubjectListCriterion();
         criterion.getSubjectListCollection().add(subjectList);
         query.setName(subjectList.getName());
         query.setDescription(subjectList.getDescription());
         query.setCompoundCriterion(new CompoundCriterion());
         query.getCompoundCriterion().setBooleanOperator(BooleanOperatorEnum.AND);
         query.setColumnCollection(new HashSet<ResultColumn>());
         query.setSubscription(subscription);
         query.setResultType(ResultTypeEnum.CLINICAL);
         query.getCompoundCriterion().getCriterionCollection().add(criterion);
         query.setSubjectListQuery(true);
         query.setSubjectListVisibility(subjectList.getVisibility());
         return query;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public NCIADicomJob createDicomJob(List<DisplayableResultRow> checkedRows) {
         NCIADicomJob dicomJob = new NCIADicomJob();
         dicomJob.setImageAggregationType(retrieveAggregationType(checkedRows));
         fillImageAggregatorFromCheckedRows(dicomJob, checkedRows);
         return dicomJob;
 
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public NCIABasket createNciaBasket(List<DisplayableResultRow> checkedRows) {
         NCIABasket basket = new NCIABasket();
         basket.setImageAggregationType(retrieveAggregationType(checkedRows));
         fillImageAggregatorFromCheckedRows(basket, checkedRows);
         return basket;
     }
 
     private NCIAImageAggregationTypeEnum retrieveAggregationType(List<DisplayableResultRow> rows) {
         NCIAImageAggregationTypeEnum aggregationType = NCIAImageAggregationTypeEnum.IMAGESERIES;
         for (DisplayableResultRow row : rows) {
             if (row.getImageSeries() == null) {
                 aggregationType = NCIAImageAggregationTypeEnum.IMAGESTUDY;
                 break;
             }
         }
         return aggregationType;
     }
 
     private void fillImageAggregatorFromCheckedRows(NCIAImageAggregator imageAggregator,
                                                     List<DisplayableResultRow> checkedRows) {
         for (DisplayableResultRow row : checkedRows) {
             switch(imageAggregator.getImageAggregationType()) {
             case IMAGESERIES:
                 handleCheckedRowForImageSeries(imageAggregator, row);
                 break;
             case IMAGESTUDY:
                 handleCheckedRowForImageStudy(imageAggregator, row);
                 break;
             default:
                 throw new IllegalStateException("Aggregate Level Type is unknown.");
             }
         }
     }
 
     private void handleCheckedRowForImageSeries(NCIAImageAggregator imageAggregator, DisplayableResultRow row) {
         if (row.getImageSeries() != null) {
             imageAggregator.getImageSeriesIDs().add(row.getImageSeries().getIdentifier());
         } else {
             throw new IllegalArgumentException(
                 "Aggregation is based on Image Series, and a row doesn't contain an Image Series.");
         }
 
     }
 
     private void handleCheckedRowForImageStudy(NCIAImageAggregator imageAggregator, DisplayableResultRow row) {
         StudySubjectAssignment studySubjectAssignment = row.getSubjectAssignment();
         if (studySubjectAssignment != null) {
             studySubjectAssignment = getDao().get(row.getSubjectAssignment().getId(), StudySubjectAssignment.class);
         }
         if (studySubjectAssignment != null
                 && CollectionUtils.isNotEmpty(studySubjectAssignment.getImageStudyCollection())) {
             for (ImageSeriesAcquisition imageStudy : studySubjectAssignment.getImageStudyCollection()) {
                 imageAggregator.getImageStudyIDs().add(imageStudy.getIdentifier());
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public File createCsvFileFromGenomicResults(GenomicDataQueryResult result) {
         File csvFile = new File(fileManager.getNewTemporaryDirectory("tempGenomicResultsDownload"),
                 "genomicData-" + System.currentTimeMillis() + ".csv");
         return GenomicDataFileWriter.writeAsCsv(result, csvFile);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void save(Query query) {
         if (query.getId() == null) {
             getDao().save(query);
         } else {
             getDao().merge(query);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void delete(Query query) {
         getDao().delete(query);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<String> validateGeneSymbols(StudySubscription studySubscription, List<String> geneSymbols)
             throws GenesNotFoundInStudyException {
         List<String> genesNotFound = new ArrayList<String>();
         Set<String> genesInStudy = getDao().retrieveGeneSymbolsInStudy(geneSymbols, studySubscription.getStudy());
         if (genesInStudy.isEmpty()) {
             throw new GenesNotFoundInStudyException("None of the specified genes were found in study.");
         }
         for (String geneSymbol : geneSymbols) {
             if (!Cai2Util.containsIgnoreCase(genesInStudy, geneSymbol)) {
                 genesNotFound.add(geneSymbol);
             }
         }
         if (!genesNotFound.isEmpty()) {
             Collections.sort(genesNotFound);
         }
         return genesNotFound;
     }
 
     /**
      * @return the arrayDataService
      */
     public ArrayDataService getArrayDataService() {
         return arrayDataService;
     }
 
     /**
      * @param arrayDataService the arrayDataService to set
      */
     @Autowired
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
     /**
      * @return the fileManager
      */
     public FileManager getFileManager() {
         return fileManager;
     }
 
     /**
      * @param fileManager the fileManager to set
      */
     @Autowired
     public void setFileManager(FileManager fileManager) {
         this.fileManager = fileManager;
     }
 }
