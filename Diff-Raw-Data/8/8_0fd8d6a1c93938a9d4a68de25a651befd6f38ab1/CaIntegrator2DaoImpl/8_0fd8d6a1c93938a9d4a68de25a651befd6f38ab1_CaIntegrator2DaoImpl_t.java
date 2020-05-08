 /**
  * Copyright 5AM Solutions Inc, ESAC, ScenPro & SAIC
  *
  * Distributed under the OSI-approved BSD 3-Clause License.
  * See http://ncip.github.com/caintegrator/LICENSE.txt for details.
  */
 package gov.nih.nci.caintegrator.data;
 
 import gov.nih.nci.caintegrator.application.query.InvalidCriterionException;
 import gov.nih.nci.caintegrator.application.study.AbstractClinicalSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.AnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator.application.study.AnnotationTypeEnum;
 import gov.nih.nci.caintegrator.application.study.AuthorizedAnnotationFieldDescriptor;
 import gov.nih.nci.caintegrator.application.study.AuthorizedStudyElementsGroup;
 import gov.nih.nci.caintegrator.application.study.FileColumn;
 import gov.nih.nci.caintegrator.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.ImageDataSourceConfiguration;
 import gov.nih.nci.caintegrator.application.study.MatchScoreComparator;
 import gov.nih.nci.caintegrator.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator.application.study.StudyLogo;
 import gov.nih.nci.caintegrator.common.Cai2Util;
 import gov.nih.nci.caintegrator.domain.analysis.GisticAnalysis;
 import gov.nih.nci.caintegrator.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator.domain.application.AbstractAnnotationCriterion;
 import gov.nih.nci.caintegrator.domain.application.CopyNumberAlterationCriterion;
 import gov.nih.nci.caintegrator.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.GenomicIntervalTypeEnum;
 import gov.nih.nci.caintegrator.domain.application.IdentifierCriterion;
 import gov.nih.nci.caintegrator.domain.application.ResultColumn;
 import gov.nih.nci.caintegrator.domain.application.SubjectListCriterion;
 import gov.nih.nci.caintegrator.domain.application.UserWorkspace;
 import gov.nih.nci.caintegrator.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator.domain.genomic.Array;
 import gov.nih.nci.caintegrator.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator.domain.genomic.ChromosomalLocation;
 import gov.nih.nci.caintegrator.domain.genomic.Gene;
 import gov.nih.nci.caintegrator.domain.genomic.GeneChromosomalLocation;
 import gov.nih.nci.caintegrator.domain.genomic.GeneLocationConfiguration;
 import gov.nih.nci.caintegrator.domain.genomic.GenomeBuildVersionEnum;
 import gov.nih.nci.caintegrator.domain.genomic.Platform;
 import gov.nih.nci.caintegrator.domain.genomic.PlatformConfiguration;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator.domain.genomic.SegmentData;
 import gov.nih.nci.caintegrator.domain.imaging.Image;
 import gov.nih.nci.caintegrator.domain.imaging.ImageSeries;
 import gov.nih.nci.caintegrator.domain.translational.Study;
 import gov.nih.nci.caintegrator.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator.security.SecurityManager;
 import gov.nih.nci.security.authorization.domainobjects.Group;
import gov.nih.nci.security.authorization.domainobjects.User;
 import gov.nih.nci.security.exceptions.CSException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Junction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Implementation of the DAO.
  */
 @Transactional
 @Repository("caIntegrator2Dao")
 public class CaIntegrator2DaoImpl extends HibernateDaoSupport implements CaIntegrator2Dao  {
 
     private static final String UNCHECKED = "unchecked";
     private static final String ANNOTATION_DEFINITION_ASSOCIATION = "annotationDefinition";
     private static final String ANNOTATION_VALUE_ASSOCIATION = "annotationValue";
     private static final String ANNOTATION_VALUE_COLLECTION_ASSOCIATION = "annotationCollection";
     private static final String STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION = "assignment";
     private static final String IMAGE_SERIES_ACQUISITION_ASSOCIATION = "imageStudy";
     private static final String STUDY_ASSOCIATION = "study";
     private static final String PLATFORM_ASSOCIATION = "platform";
     private static final String GENES_ASSOCIATION = "genes";
     private static final String NAME_ATTRIBUTE = "name";
     private static final String SYMBOL_ATTRIBUTE = "symbol";
     private SecurityManager securityManager;
 
     /**
      * Initialization method.
      * @param factory the session factory
      */
     @Autowired
     public void init(SessionFactory factory) {
         setSessionFactory(factory);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public UserWorkspace getWorkspace(String username) {
         List results = getCurrentSession().
                         createCriteria(UserWorkspace.class).
                         add(Restrictions.eq("username", username)).list();
         if (results.isEmpty()) {
             return null;
         } else {
             return (UserWorkspace) results.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void refresh(Object persistentObject) {
         getHibernateTemplate().refresh(persistentObject);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void clearSession() {
         getCurrentSession().clear();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void save(Object persistentObject) {
         getHibernateTemplate().saveOrUpdate(persistentObject);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void delete(Object persistentObject) {
         getHibernateTemplate().delete(persistentObject);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <T> T get(Long id, Class<T> objectClass) {
         return getHibernateTemplate().get(objectClass, id);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void saveSubjectSourceStatus(AbstractClinicalSourceConfiguration source) {
         getHibernateTemplate().evict(source);
         AbstractClinicalSourceConfiguration newSource = get(source.getId(), AbstractClinicalSourceConfiguration.class);
         newSource.setStatus(source.getStatus());
         newSource.setStatusDescription(source.getStatusDescription());
         save(newSource);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void removeObjects(Collection objects) {
         if (objects != null) {
             getHibernateTemplate().deleteAll(objects);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<AnnotationDefinition> findMatches(Collection<String> keywords) {
         List<AnnotationDefinition> annotationDefinitions = new ArrayList<AnnotationDefinition>();
         List<AnnotationDefinition> results = getHibernateTemplate().find("from AnnotationDefinition");
         for (AnnotationDefinition ad : results) {
             if (containsKeyword(ad, keywords)) {
                 annotationDefinitions.add(ad);
             }
         }
         Collections.sort(annotationDefinitions, new MatchScoreComparator(keywords));
         return annotationDefinitions;
     }
 
     private boolean containsKeyword(AnnotationDefinition ad, Collection<String> keywords) {
         for (String keyword : keywords) {
             if (ad.getKeywords() != null
                 && Cai2Util.containsIgnoreCase(Arrays.asList(StringUtils.split(ad.getKeywords())), keyword)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<UserWorkspace> retrieveAllSubscribedWorkspaces(Study study) {
         Criteria workspaceCriteria = getCurrentSession().createCriteria(UserWorkspace.class);
         workspaceCriteria.createCriteria("subscriptionCollection").
                           add(Restrictions.eq(STUDY_ASSOCIATION, study));
         return workspaceCriteria.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<ImageSeries> findMatchingImageSeries(AbstractAnnotationCriterion criterion, Study study) {
         if (!criterion.getEntityType().equals(EntityTypeEnum.IMAGESERIES)) {
             return new ArrayList<ImageSeries>();
         }
         Criteria imageSeriesCrit = getCurrentSession().createCriteria(ImageSeries.class);
         if (criterion instanceof IdentifierCriterion) {
             imageSeriesCrit.add(AbstractAnnotationCriterionHandler.create(criterion).translate());
         } else {
             createAnnotationValuesCriteria(criterion,
                                             imageSeriesCrit,
                                            ANNOTATION_VALUE_COLLECTION_ASSOCIATION);
         }
             createStudySubjectAssignmentCriteria(
                         imageSeriesCrit.createCriteria(IMAGE_SERIES_ACQUISITION_ASSOCIATION).
                         createCriteria(STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION),
                         study);
 
         return imageSeriesCrit.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<SampleAcquisition> findMatchingSamples(AbstractAnnotationCriterion criterion, Study study) {
         if (!criterion.getEntityType().equals(EntityTypeEnum.SAMPLE)) {
             return new ArrayList<SampleAcquisition>();
         } else {
             Criteria sampleAcquisitionCrit = getCurrentSession().createCriteria(SampleAcquisition.class);
             createAnnotationValuesCriteria(criterion,
                                            sampleAcquisitionCrit,
                                            ANNOTATION_VALUE_COLLECTION_ASSOCIATION);
             createStudySubjectAssignmentCriteria(
                     sampleAcquisitionCrit.createCriteria(STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION),
                     study);
             return sampleAcquisitionCrit.list();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<StudySubjectAssignment> findMatchingSubjects(AbstractAnnotationCriterion criterion, Study study) {
         if (!criterion.getEntityType().equals(EntityTypeEnum.SUBJECT)) {
             return new ArrayList<StudySubjectAssignment>();
         }
         Criteria studySubjectAssignmentCrit = getCurrentSession().createCriteria(StudySubjectAssignment.class);
         if (criterion instanceof IdentifierCriterion) {
             studySubjectAssignmentCrit.add(AbstractAnnotationCriterionHandler.create(criterion).translate());
         } else {
             createAnnotationValuesCriteria(criterion,
                                            studySubjectAssignmentCrit.createCriteria("subjectAnnotationCollection"),
                                            ANNOTATION_VALUE_ASSOCIATION);
         }
         createStudySubjectAssignmentCriteria(studySubjectAssignmentCrit, study);
         return studySubjectAssignmentCrit.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<StudySubjectAssignment> findMatchingSubjects(SubjectListCriterion subjectListCriterion, Study study) {
         Criteria studySubjectAssignmentCrit = getCurrentSession().createCriteria(StudySubjectAssignment.class);
         studySubjectAssignmentCrit.add(Restrictions.in("identifier", subjectListCriterion.getSubjectIdentifiers()));
         createStudySubjectAssignmentCriteria(studySubjectAssignmentCrit, study);
         return studySubjectAssignmentCrit.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public Set<AbstractReporter> findReportersForGenes(Set<String> geneSymbols,
             ReporterTypeEnum reporterType, Study study, Platform platform) {
         Set<ReporterList> studyReporterLists = getStudyReporterLists(study, reporterType, platform);
         if (studyReporterLists.isEmpty()) {
             return Collections.emptySet();
         }
         Set<AbstractReporter> reporters = new HashSet<AbstractReporter>();
         Criteria criteria = getCurrentSession().createCriteria(AbstractReporter.class);
         if (!geneSymbols.isEmpty()) {
             criteria.createCriteria(GENES_ASSOCIATION).add(Restrictions.in(SYMBOL_ATTRIBUTE, geneSymbols));
         }
         criteria.add(Restrictions.in("reporterList", studyReporterLists));
         reporters.addAll(criteria.list());
         return reporters;
     }
 
     /**
      * {@inheritDoc}
      * @throws InvalidCriterionException
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<SegmentData> findMatchingSegmentDatas(CopyNumberAlterationCriterion copyNumberCriterion,
             Study study, Platform platform) throws InvalidCriterionException {
         if (GenomicIntervalTypeEnum.GENE_NAME.equals(copyNumberCriterion.getGenomicIntervalType())
               && !copyNumberCriterion.getGeneSymbols().isEmpty()
               && (platform.getGenomeVersion() == null || !isGenomeVersionMapped(platform.getGenomeVersion()))) {
             throw new InvalidCriterionException("The platform genome version '"
                     + platform.getGenomeVersion().getValue() + "' is not mapped to chromosomal locations, "
                     + "it is not possible to query based on gene locations.");
         }
         return new CopyNumberAlterationCriterionConverter(copyNumberCriterion).
                         retrieveSegmentDataCriteria(study, platform, getCurrentSession()).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<SegmentData> findMatchingSegmentDatasByLocation(List<SegmentData> segmentDatasToMatch,
             Study study, Platform platform) {
         Criteria segmentDataCrit = getCurrentSession().createCriteria(SegmentData.class);
         Criteria arrayDataCrit = segmentDataCrit.createCriteria("arrayData");
         Criteria reporterListsCrit = arrayDataCrit.createCriteria("reporterLists");
         reporterListsCrit.add(Restrictions.eq(PLATFORM_ASSOCIATION, platform));
         arrayDataCrit.add(Restrictions.eq(STUDY_ASSOCIATION, study));
         Junction overallOrStatement = Restrictions.disjunction();
         for (SegmentData segmentData : segmentDatasToMatch) {
             ChromosomalLocation location = segmentData.getLocation();
             overallOrStatement.add(Restrictions.conjunction().
                     add(Restrictions.eq("Location.startPosition", location.getStartPosition())).
                     add(Restrictions.eq("Location.endPosition", location.getEndPosition())));
         }
         segmentDataCrit.add(overallOrStatement);
         return segmentDataCrit.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isGenomeVersionMapped(GenomeBuildVersionEnum genomeVersion) {
         return !getCurrentSession().createCriteria(GeneLocationConfiguration.class).
                     add(Restrictions.eq("genomeBuildVersion", genomeVersion)).list().isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public GeneLocationConfiguration getGeneLocationConfiguration(GenomeBuildVersionEnum genomeVersion) {
         List<GeneLocationConfiguration> geneLocationConfigurations =
             getCurrentSession().createCriteria(GeneLocationConfiguration.class).
                 add(Restrictions.eq("genomeBuildVersion", genomeVersion)).list();
         return geneLocationConfigurations.isEmpty() ? null : geneLocationConfigurations.get(0);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<Gene> findGenesByLocation(String chromosome, Integer startPosition, Integer endPosition,
             GenomeBuildVersionEnum genomeBuildVersion) {
         String locStartPosition = "location.startPosition";
         String locEndPosition = "location.endPosition";
         Criteria geneLocationCriteria = getCurrentSession().createCriteria(GeneChromosomalLocation.class);
         // (gene.startPos <= startPosition && gene.endPos >= startPosition)
         //  || (gene.startPos >= lowerInput  && gene.startPos <= higherInput)
         Junction overallOrStatement = Restrictions.disjunction();
         overallOrStatement.add(Restrictions.conjunction().add(Restrictions.le(locStartPosition, startPosition))
                 .add(Restrictions.ge(locEndPosition, startPosition)));
         overallOrStatement.add(Restrictions.conjunction().add(Restrictions.ge(locStartPosition, startPosition)).add(
                 Restrictions.le(locStartPosition, endPosition)));
         geneLocationCriteria.add(overallOrStatement);
         geneLocationCriteria.add(getChromosomeRestriction(chromosome));
         geneLocationCriteria.createCriteria("geneLocationConfiguration").
             add(Restrictions.eq("genomeBuildVersion", genomeBuildVersion));
         geneLocationCriteria.setProjection(Projections.property("geneSymbol"));
         List<String> geneSymbols = geneLocationCriteria.list();
         return geneSymbols.isEmpty() ? new ArrayList<Gene>()
                 : getCurrentSession().createCriteria(Gene.class).setProjection(
                         Projections.distinct(Projections.property(SYMBOL_ATTRIBUTE))).
                         add(Restrictions.in(SYMBOL_ATTRIBUTE, geneSymbols)).
                         addOrder(Order.asc(SYMBOL_ATTRIBUTE)).list();
     }
 
     private Criterion getChromosomeRestriction(String chromosome) {
         if (Cai2Util.getAlternateChromosome(chromosome) != null) {
             List<String> chromosomes = new ArrayList<String>();
             chromosomes.add(chromosome);
             chromosomes.add(Cai2Util.getAlternateChromosome(chromosome));
             return Restrictions.in("location.chromosome", chromosomes);
         }
         return Restrictions.eq("location.chromosome", chromosome);
     }
 
 
 
     private Set<ReporterList> getStudyReporterLists(Study study, ReporterTypeEnum reporterType, Platform platform) {
         Set<ReporterList> reporterLists = new HashSet<ReporterList>();
         for (ArrayData arrayData : study.getArrayDatas(reporterType, platform)) {
             reporterLists.addAll(arrayData.getReporterLists());
         }
         return reporterLists;
     }
 
     /**
      * This function adds the values criteria for getting back the correct annotation values.
      * @param criterion - The main criterion object for the values we want.
      * @param mainAnnotationCriteria - The Criteria on the object that links directly to AbstractAnnotationValue
      * @param annotationValueRelationship - Relationship name that the above object has with AbstractAnnotationValue
      */
     private void createAnnotationValuesCriteria(AbstractAnnotationCriterion criterion,
             Criteria mainAnnotationCriteria, String annotationValueRelationship) {
         if (criterion != null && criterion.getAnnotationFieldDescriptor() != null) {
             Criteria valuesCrit = mainAnnotationCriteria.createCriteria(annotationValueRelationship);
             valuesCrit.add(Restrictions.eq(ANNOTATION_DEFINITION_ASSOCIATION, criterion.getAnnotationFieldDescriptor()
                 .getDefinition()));
             valuesCrit.add(AbstractAnnotationCriterionHandler.create(criterion).translate());
         }
     }
 
     private void createStudySubjectAssignmentCriteria(Criteria studySubjectAssignmentCrit, Study study) {
         studySubjectAssignmentCrit.add(Restrictions.eq(STUDY_ASSOCIATION, study));
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)  // Hibernate operations are untyped
     public AnnotationDefinition getAnnotationDefinition(String name, AnnotationTypeEnum dataType) {
         List<AnnotationDefinition> values = getCurrentSession().createCriteria(AnnotationDefinition.class)
                 .add(Restrictions.eq("commonDataElement.longName", name))
                 .add(Restrictions.eq("commonDataElement.valueDomain.dataType", dataType)).list();
         if (values.isEmpty()) {
             return null;
         } else {
             return values.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)  // Hibernate operations are untyped
     public AnnotationDefinition getAnnotationDefinition(Long cdeId, Float version) {
         List<AnnotationDefinition> values = (version == null)
             ? getCurrentSession().createCriteria(AnnotationDefinition.class)
                 .add(Restrictions.eq("commonDataElement.publicID", cdeId)).list()
             : getCurrentSession().createCriteria(AnnotationDefinition.class)
                 .add(Restrictions.eq("commonDataElement.publicID", cdeId))
                 .add(Restrictions.eq("commonDataElement.version", version.toString())).list();
 
         if (values.isEmpty()) {
             return null;
         } else {
             return latestVersion(values);
         }
     }
 
     private AnnotationDefinition latestVersion(List<AnnotationDefinition> values) {
         AnnotationDefinition result = values.get(0);
         for (AnnotationDefinition definition : values) {
             if (Float.valueOf(result.getCommonDataElement().getVersion())
                     < Float.valueOf(definition.getCommonDataElement().getVersion())) {
                 result = definition;
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Map<String, Gene> getGeneSymbolMap() {
         Map <String, Gene> resultMap = new HashMap<String, Gene>();
         Query query = getCurrentSession().createQuery("from Gene");
         for (Object object : query.list()) {
             Gene gene = (Gene) object;
             resultMap.put(gene.getSymbol().toUpperCase(Locale.getDefault()), gene);
         }
         return resultMap;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Gene getGene(String symbol) {
         List values = getHibernateTemplate().findByNamedParam("from Gene where symbol = :symbol",
                 SYMBOL_ATTRIBUTE, symbol.toUpperCase(Locale.getDefault()));
         if (values.isEmpty()) {
             return null;
         } else {
             return (Gene) values.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Gene lookupOrCreateGene(String symbol) {
         Gene gene = getGene(symbol);
         if (gene == null) {
             gene = new Gene();
             gene.setSymbol(symbol.toUpperCase(Locale.getDefault()));
         }
         return gene;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Platform getPlatform(String name) {
         List values = getHibernateTemplate().findByNamedParam("from Platform where name = :name",
                 NAME_ATTRIBUTE, name);
         if (values.isEmpty()) {
             return null;
         } else {
             return (Platform) values.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public PlatformConfiguration getPlatformConfiguration(String name) {
         List values = getHibernateTemplate().findByNamedParam("from PlatformConfiguration where name = :name",
                 NAME_ATTRIBUTE, name);
         if (values.isEmpty()) {
             return null;
         } else {
             return (PlatformConfiguration) values.get(0);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)  // Hibernate operations are untyped
     public Set<String> retrieveGeneSymbolsInStudy(Collection<String> symbols, Study study) {
         Criteria reporterCriteria = getCurrentSession().createCriteria(AbstractReporter.class);
         reporterCriteria.createCriteria("reporterList").
                 createCriteria("arrayDatas").
                     add(Restrictions.eq(STUDY_ASSOCIATION, study));
         reporterCriteria.createCriteria(GENES_ASSOCIATION).
             add(Restrictions.in(SYMBOL_ATTRIBUTE, symbols));
         Set<AbstractReporter> reporterSet = new HashSet<AbstractReporter>();
         reporterSet.addAll(reporterCriteria.list());
         Set<String> geneSymbols = new HashSet<String>();
         for (AbstractReporter reporter : reporterSet) {
             for (Gene gene : reporter.getGenes()) {
                 String symbol = gene.getSymbol();
                 if (Cai2Util.containsIgnoreCase(symbols, symbol)) {
                     geneSymbols.add(symbol.toUpperCase(Locale.US));
                 }
             }
         }
         return geneSymbols;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public ReporterList getReporterList(String name) {
         List values = getHibernateTemplate().findByNamedParam("from ReporterList where name = :name",
                 NAME_ATTRIBUTE, name);
         if (values.isEmpty()) {
             return null;
         } else {
             return (ReporterList) values.get(0);
         }
     }
 
     private Session getCurrentSession() {
         return getHibernateTemplate().getSessionFactory().getCurrentSession();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<Platform> getPlatforms() {
         Query query = getCurrentSession().createQuery("from Platform order by name");
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<PlatformConfiguration> getPlatformConfigurations() {
         List<PlatformConfiguration> platformConfigurationList =
             getCurrentSession().
                 createCriteria(PlatformConfiguration.class).list();
         for (PlatformConfiguration configuration : platformConfigurationList) {
             if (configuration.getPlatform() == null) {
                 configuration.setInUse(false);
             } else {
                 configuration.setInUse(isPlatformInUsed(configuration.getPlatform()));
             }
         }
         return platformConfigurationList;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public boolean isPlatformInUsed(Platform platform) {
         List<GenomicDataSourceConfiguration> result = getHibernateTemplate().find(
                 "from GenomicDataSourceConfiguration where platformName = ?",
                 new Object[] {platform.getName()});
         if (result.isEmpty()) {
             result =  getHibernateTemplate().find(
                     "from Array where platform = ?", new Object[] {platform});
         }
         return !result.isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<StudyConfiguration> getStudyConfigurationsWhichNeedThisPlatform(Platform platform) {
         List<StudyConfiguration> listOfStudyConfigurations = new ArrayList<StudyConfiguration>();
         List<ArrayData> listOfArrayData = null;
         List<GenomicDataSourceConfiguration> genomicDataSourceConfigurationList = getHibernateTemplate().find(
                 "from GenomicDataSourceConfiguration where platformName = ?",
                 new Object[] {platform.getName()});
         if (!genomicDataSourceConfigurationList.isEmpty()) {
             listOfStudyConfigurations = getHibernateTemplate().find(
                     "select studyConfiguration from GenomicDataSourceConfiguration gdsc "
                     + "where gdsc.platformName = ?",
                     new Object[] {platform.getName()});
         } else {
             listOfArrayData = getHibernateTemplate().find(
                     "select arrayRef.arrayDataCollection from Array arrayRef "
                     + "where arrayRef.platform = ?",
                     new Object[] {platform});
             for (ArrayData arrayData : listOfArrayData) {
                 listOfStudyConfigurations.add(arrayData.getStudy().getStudyConfiguration());
             }
         }
         // before returning list remove duplicates by converting to set and back
         Set<StudyConfiguration> setOfStudyConfigurations =
             new LinkedHashSet<StudyConfiguration>(listOfStudyConfigurations);
         return new ArrayList<StudyConfiguration>(setOfStudyConfigurations);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<Study> getPublicStudies() {
         return getCurrentSession().createCriteria(Study.class)
                                   .add(Restrictions.eq("publiclyAccessible", true))
                                   .addOrder(Order.asc("shortTitleText")).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<Study> getStudies(String username) {
         secureCurrentSession(username);
         Query query = getCurrentSession().createQuery("from Study order by shortTitleText");
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED) // Hibernate operations are untyped
     public List<AuthorizedStudyElementsGroup> getAuthorizedStudyElementGroups(String username,
                                                                               Long studyConfigurationId) {
         secureCurrentSession(username);
         Query q = getCurrentSession().createQuery("from " + AuthorizedStudyElementsGroup.class.getName()
                 + " where studyConfiguration.id = :id");
         q.setParameter("id", studyConfigurationId);
 
         List<AuthorizedStudyElementsGroup> authorizedGroups = q.list();
         return getGroupsUserIsIn(username, authorizedGroups);
     }
 
     @SuppressWarnings(UNCHECKED)
     private List<AuthorizedStudyElementsGroup> getGroupsUserIsIn(String username,
             List<AuthorizedStudyElementsGroup> authorizedGroups) {
         List<AuthorizedStudyElementsGroup> groupsUserIsIn = new ArrayList<AuthorizedStudyElementsGroup>();
         try {
            User user = securityManager.getAuthorizationManager().getUser(username);
            if (user == null) {
                return Collections.emptyList();
            }
            String userId = String.valueOf(user.getUserId());
             Set<Group> userGroups = securityManager.getAuthorizationManager().getGroups(userId);
             for (AuthorizedStudyElementsGroup authorizedGroup : authorizedGroups) {
                 if (userGroups.contains(authorizedGroup.getAuthorizedGroup())) {
                     groupsUserIsIn.add(authorizedGroup);
                 }
             }
         } catch (CSException e) {
             throw new IllegalStateException("Unable to retrieve authorized groups");
         }
         return groupsUserIsIn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<AnnotationFieldDescriptor> getAuthorizedAnnotationFieldDescriptors(String username,
                                                                                 StudyConfiguration studyConfiguration) {
         secureCurrentSession(username);
         Long studyConfigurationId = studyConfiguration.getId();
         List<AuthorizedStudyElementsGroup> authorizedGroups = getAuthorizedStudyElementGroups(username,
                                                                                               studyConfigurationId);
         List<AnnotationFieldDescriptor> listAfd = new ArrayList<AnnotationFieldDescriptor>();
         for (AuthorizedStudyElementsGroup group : authorizedGroups) {
             for (AuthorizedAnnotationFieldDescriptor aafd : group.getAuthorizedAnnotationFieldDescriptors()) {
                 listAfd.add(aafd.getAnnotationFieldDescriptor());
             }
         }
 
         return listAfd;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public boolean isDuplicateStudyName(Study study, String username) {
         secureCurrentSession(username);
         long id = study.getId() == null ? 0 : study.getId().longValue();
         List<Study> result = getHibernateTemplate().find("from Study where shortTitleText = ? "
                 + "and enabled = true "
                 + "and id != ?",
                 new Object[] {study.getShortTitleText(), id });
         return !result.isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public StudyLogo retrieveStudyLogo(Long studyId, String studyShortTitleText) {
         List<StudyConfiguration> studyConfigurationList =
                 getCurrentSession().
                     createCriteria(StudyConfiguration.class).
                     createCriteria(STUDY_ASSOCIATION).
                         add(Restrictions.eq("id", studyId)).
                         add(Restrictions.like("shortTitleText", studyShortTitleText)).list();
         if (studyConfigurationList != null && !studyConfigurationList.isEmpty()) {
             return studyConfigurationList.get(0).getStudyLogo();
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public <T> List<T> retrieveUniqueValuesForStudyAnnotation(Study study,
                                                                AnnotationDefinition definition,
                                                                EntityTypeEnum entityType,
                                                                Class<T> objectClass) {
         AnnotationTypeEnum annotationType = definition.getDataType();
         if (annotationType == null) {
             throw new IllegalArgumentException("Data Type for the Annotation Definition is unknown.");
         }
         Criteria abstractAnnotationValueCriteria = getCurrentSession().createCriteria(AbstractAnnotationValue.class);
         abstractAnnotationValueCriteria.add(Restrictions.eq(ANNOTATION_DEFINITION_ASSOCIATION, definition));
         addValuesToStudyCriteria(study, entityType, abstractAnnotationValueCriteria);
 
         String uniqueAttribute = "stringValue";
         switch (annotationType) {
         case STRING:
             uniqueAttribute = "stringValue";
             break;
         case NUMERIC:
             uniqueAttribute = "numericValue";
             break;
         case DATE:
             uniqueAttribute = "dateValue";
             break;
         default:
             throw new IllegalStateException("Unknown Annotation Type Type.");
         }
         abstractAnnotationValueCriteria.setProjection(Projections.distinct(Projections.property(uniqueAttribute)));
         return abstractAnnotationValueCriteria.list();
 
     }
 
 
     /**
      * Adds the criteria to the AnnotationValue object which will associate it to the given Study.
      * @param study
      * @param entityType
      * @param abstractAnnotationValueCriteria
      */
     private void addValuesToStudyCriteria(Study study, EntityTypeEnum entityType,
             Criteria abstractAnnotationValueCriteria) {
         switch (entityType) {
         case SUBJECT:
             abstractAnnotationValueCriteria.createCriteria("subjectAnnotation")
                                             .createCriteria("studySubjectAssignment")
                                             .add(Restrictions.eq(STUDY_ASSOCIATION, study));
             break;
         case IMAGESERIES:
             abstractAnnotationValueCriteria.createCriteria("imageSeries")
                                            .createCriteria("imageStudy")
                                            .createCriteria(STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION)
                                            .add(Restrictions.eq(STUDY_ASSOCIATION, study));
             break;
         case IMAGE:
             abstractAnnotationValueCriteria.createCriteria("image")
                                             .createCriteria("series")
                                             .createCriteria("imageStudy")
                                             .createCriteria(STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION)
                                             .add(Restrictions.eq(STUDY_ASSOCIATION, study));
             break;
         case SAMPLE:
             abstractAnnotationValueCriteria.createCriteria("sampleAcquisition")
                                            .createCriteria(STUDY_SUBJECT_ASSIGNMENT_ASSOCIATION)
                                            .add(Restrictions.eq(STUDY_ASSOCIATION, study));
             break;
         default:
             throw new IllegalStateException("Unknown Entity Type.");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int retrieveNumberImages(Collection<ImageSeries> imageSeries) {
         Criteria imageCriteria = getCurrentSession().createCriteria(Image.class);
         imageCriteria.add(Restrictions.in("series", imageSeries));
         imageCriteria.setProjection(Projections.rowCount());
         return (Integer) imageCriteria.list().get(0);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<Platform> retrievePlatformsForGenomicSource(GenomicDataSourceConfiguration genomicSource) {
         Criteria arrayCriteria = getCurrentSession().createCriteria(Array.class);
         arrayCriteria.setProjection(Projections.distinct(Projections.property(PLATFORM_ASSOCIATION)))
                      .add(Restrictions.isNotNull(PLATFORM_ASSOCIATION))
                      .createCriteria("sampleCollection")
                      .add(Restrictions.eq("genomicDataSource", genomicSource));
 
         return arrayCriteria.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<FileColumn> getFileColumnsUsingAnnotationFieldDescriptor(AnnotationFieldDescriptor fieldDescriptor) {
         return getCurrentSession().createCriteria(FileColumn.class).add(
                 Restrictions.eq("fieldDescriptor", fieldDescriptor)).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<ResultColumn> getResultColumnsUsingAnnotation(AnnotationFieldDescriptor fieldDescriptor) {
         return getCurrentSession().createCriteria(ResultColumn.class).add(
                 Restrictions.eq("annotationFieldDescriptor", fieldDescriptor)).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<AbstractAnnotationCriterion> getCriteriaUsingAnnotation(
             AnnotationFieldDescriptor fieldDescriptor) {
         return getCurrentSession().
                 createCriteria(AbstractAnnotationCriterion.class).add(
                         Restrictions.eq("annotationFieldDescriptor", fieldDescriptor)).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <T> T merge(T persistentObject) {
         return getHibernateTemplate().merge(persistentObject);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public ImageDataSourceConfiguration retrieveImagingDataSourceForStudy(Study study) {
         Criteria imagingDataSourceConfigurationCrit = getCurrentSession().
                                                       createCriteria(ImageDataSourceConfiguration.class);
         imagingDataSourceConfigurationCrit.createCriteria("studyConfiguration")
                                           .add(Restrictions.eq(STUDY_ASSOCIATION, study));
         List<ImageDataSourceConfiguration> sourceList = imagingDataSourceConfigurationCrit.list();
         if (sourceList != null && !sourceList.isEmpty()) {
             return sourceList.get(0);
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings("unchecked")      // hibernate operation not parameterized
     public Set<GisticAnalysis> getGisticAnalysisUsingGenomicSource(
             GenomicDataSourceConfiguration genomicSource) {
         return new HashSet<GisticAnalysis>(getCurrentSession().createCriteria(GisticAnalysis.class).
                 createCriteria("samplesUsedForCalculation").
                 add(Restrictions.eq("genomicDataSource", genomicSource)).list());
     }
 
     private void secureCurrentSession(String username) {
         if (securityManager != null) {
             try {
                 securityManager.initializeFiltersForUserGroups(username, getCurrentSession());
             } catch (CSException e) {
                 throw new IllegalStateException("Unable to use instance level filters.");
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void setFlushMode(int mode) {
         getHibernateTemplate().setFlushMode(mode);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void runSessionKeepAlive() {
         getCurrentSession().createSQLQuery("select 1").list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings(UNCHECKED)
     public List<GenomicDataSourceConfiguration> getAllGenomicDataSources() {
         return getCurrentSession().createCriteria(GenomicDataSourceConfiguration.class).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void markStudiesAsNeedingRefresh() {
         StringBuilder builder = new StringBuilder();
         builder.append("update ").append(StudyConfiguration.class.getName()).append(" sc set sc.dataRefreshed = ")
             .append(" :refreshed where (select count(*) from sc.genomicDataSources where dataRefreshed is true) > 0");
         Query query = getCurrentSession().createQuery(builder.toString());
         query.setParameter("refreshed", Boolean.TRUE);
         query.executeUpdate();
     }
 
     /**
      * @return the securityManager
      */
     public SecurityManager getSecurityManager() {
         return securityManager;
     }
 
     /**
      * @param securityManager the securityManager to set
      */
     @Autowired
     public void setSecurityManager(SecurityManager securityManager) {
         this.securityManager = securityManager;
     }
 }
