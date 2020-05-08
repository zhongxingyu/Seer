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
 package gov.nih.nci.caintegrator2.application.study;
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.workspace.WorkspaceService;
 import gov.nih.nci.caintegrator2.common.DateUtil;
 import gov.nih.nci.caintegrator2.common.PermissibleValueUtil;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractAnnotationValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AbstractPermissibleValue;
 import gov.nih.nci.caintegrator2.domain.annotation.AnnotationDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.CommonDataElement;
 import gov.nih.nci.caintegrator2.domain.annotation.SubjectAnnotation;
 import gov.nih.nci.caintegrator2.domain.annotation.SurvivalValueDefinition;
 import gov.nih.nci.caintegrator2.domain.annotation.ValueDomain;
 import gov.nih.nci.caintegrator2.domain.application.EntityTypeEnum;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.domain.genomic.SampleAcquisition;
 import gov.nih.nci.caintegrator2.domain.imaging.ImageSeriesAcquisition;
 import gov.nih.nci.caintegrator2.domain.translational.Study;
 import gov.nih.nci.caintegrator2.domain.translational.StudySubjectAssignment;
 import gov.nih.nci.caintegrator2.domain.translational.Timepoint;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacade;
 import gov.nih.nci.caintegrator2.external.caarray.ExperimentNotFoundException;
 import gov.nih.nci.caintegrator2.external.cadsr.CaDSRFacade;
 import gov.nih.nci.caintegrator2.external.ncia.NCIAFacade;
 import gov.nih.nci.caintegrator2.file.FileManager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Entry point to the StudyManagementService subsystem.
  */
 @Transactional(propagation = Propagation.REQUIRED)
 @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ExcessiveClassLength" })   // see configure study
 public class StudyManagementServiceImpl implements StudyManagementService {
 
     @SuppressWarnings("unused")
     private static final Logger LOGGER = Logger.getLogger(StudyManagementServiceImpl.class);
     private static final int DEFINITION_LENGTH = 1000;
     private CaIntegrator2Dao dao;
     private FileManager fileManager;
     private CaDSRFacade caDSRFacade;
     private NCIAFacade nciaFacade;
     private CaArrayFacade caArrayFacade;
     private ArrayDataService arrayDataService;
     private WorkspaceService workspaceService;
 
     /**
      * {@inheritDoc}
      */
     public void save(StudyConfiguration studyConfiguration) {
         if (isNew(studyConfiguration)) {
             configureNew(studyConfiguration);
             getWorkspaceService().subscribe(getWorkspaceService().getWorkspace(), studyConfiguration.getStudy());
         }
         persist(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public void saveAsynchronousStudyConfigurationJob(StudyConfiguration studyConfiguration) {
         dao.save(studyConfiguration);
     }
 
     /**
      *  {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration) {
         fileManager.deleteStudyDirectory(studyConfiguration);
         getWorkspaceService().unsubscribeAll(studyConfiguration.getStudy());
         studyConfiguration.getUserWorkspace().getStudyConfigurationJobs().remove(studyConfiguration);
         dao.save(studyConfiguration.getUserWorkspace());
         studyConfiguration.setUserWorkspace(null);
         dao.delete(studyConfiguration);
     }
     
     private boolean isNew(StudyConfiguration studyConfiguration) {
         return studyConfiguration.getId() == null;
     }
 
     private void configureNew(StudyConfiguration studyConfiguration) {
         configureNew(studyConfiguration.getStudy());
     }
 
     @SuppressWarnings({"PMD.CyclomaticComplexity", 
                         "PMD.ExcessiveMethodLength", 
                         "PMD.NPathComplexity" })   // multiple simple null checks
     private void configureNew(Study study) {
         if (study.getDefaultTimepoint() == null) {
             Timepoint defaultTimepoint = new Timepoint();
             String studyTitle = "";
             if (study.getShortTitleText() != null) {
                 studyTitle = study.getShortTitleText();
             } else if (study.getLongTitleText() != null) {
                 studyTitle = study.getLongTitleText();
             }
             defaultTimepoint.setDescription("Default Timepoint For Study '" + studyTitle + "'");
             defaultTimepoint.setName("Default");
             study.setDefaultTimepoint(defaultTimepoint);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public DelimitedTextClinicalSourceConfiguration addClinicalAnnotationFile(StudyConfiguration studyConfiguration,
             File inputFile, String filename) throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename, studyConfiguration);
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, dao);
         DelimitedTextClinicalSourceConfiguration clinicalSourceConfig = 
             new DelimitedTextClinicalSourceConfiguration(annotationFile, studyConfiguration);
         dao.save(studyConfiguration);
         return clinicalSourceConfig;
     }
     
     /**
      * {@inheritDoc}
      */
     public void addStudyLogo(StudyConfiguration studyConfiguration,
                              File imageFile,
                              String fileName, 
                              String fileType) throws IOException {
         if (studyConfiguration.getStudyLogo() == null) {
             studyConfiguration.setStudyLogo(new StudyLogo());
         }
         studyConfiguration.getStudyLogo().setFileName(fileName);
         studyConfiguration.getStudyLogo().setFileType(fileType);
         File studyLogoFile = getFileManager().storeStudyFile(imageFile, fileName, studyConfiguration);
         studyConfiguration.getStudyLogo().setPath(studyLogoFile.getPath());
         dao.save(studyConfiguration);
     }
     
     /**
      * {@inheritDoc}
      */
     public StudyLogo retrieveStudyLogo(Long studyId, String studyShortTitleText) {
         return dao.retrieveStudyLogo(studyId, studyShortTitleText);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadClinicalAnnotation(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSourceConfiguration)
         throws ValidationException {
         clinicalSourceConfiguration.loadAnnontation();
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void reLoadClinicalAnnotation(StudyConfiguration studyConfiguration) throws ValidationException {
         deleteClinicalAnnotation(studyConfiguration);
         for (AbstractClinicalSourceConfiguration configuration 
                 : studyConfiguration.getClinicalConfigurationCollection()) {
             configuration.reLoadAnnontation();
         }
         dao.removeObjects(studyConfiguration.removeObsoleteSubjectAssignment());
         refreshAnnotationDefinitions(studyConfiguration.getStudy());
         save(studyConfiguration);
     }
     
     private void refreshAnnotationDefinitions(Study study) {
         for (AnnotationDefinition annotationDefinition : study.getSubjectAnnotationCollection()) {
             dao.refresh(annotationDefinition);
         }
     }
     
     private void deleteClinicalAnnotation(StudyConfiguration studyConfiguration) {
         Study study = studyConfiguration.getStudy();
         for (StudySubjectAssignment studySubjectAssignment : study.getAssignmentCollection()) {
             for (SubjectAnnotation subjectAnnotation : studySubjectAssignment.getSubjectAnnotationCollection()) {
                 dao.delete(subjectAnnotation);
             }
             studySubjectAssignment.getSubjectAnnotationCollection().clear();
         }
         study.getSubjectAnnotationCollection().clear();
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete(StudyConfiguration studyConfiguration,
             AbstractClinicalSourceConfiguration clinicalSource) throws ValidationException {
         deleteClinicalAnnotation(studyConfiguration);
         studyConfiguration.getClinicalConfigurationCollection().remove(clinicalSource);
         dao.delete(clinicalSource);
         reLoadClinicalAnnotation(studyConfiguration);
     }
     
     private void persist(StudyConfiguration studyConfiguration) {
         for (StudySubjectAssignment assignment : studyConfiguration.getStudy().getAssignmentCollection()) {
             saveSubjectAnnotations(assignment.getSubjectAnnotationCollection());
             saveSampleAcquisitions(assignment.getSampleAcquisitionCollection());
             dao.save(assignment.getSubject());
             dao.save(assignment);
         }
         dao.save(studyConfiguration);
     }
 
     private void saveSampleAcquisitions(Collection<SampleAcquisition> sampleAcquisitionCollection) {
         for (SampleAcquisition sampleAcquisition : sampleAcquisitionCollection) {
             dao.save(sampleAcquisition.getSample());
             dao.save(sampleAcquisition);
         }
     }
 
     private void saveSubjectAnnotations(Collection<SubjectAnnotation> subjectAnnotations) {
         for (SubjectAnnotation annotation : subjectAnnotations) {
             dao.save(annotation.getAnnotationValue());
             dao.save(annotation);
         }
     }
     
     /**
      * {@inheritDoc}
      * @throws ValidationException 
      * @throws IOException 
      */
     public void mapSamples(StudyConfiguration studyConfiguration, File mappingFile)
         throws ValidationException, IOException {
         new SampleMappingHelper(studyConfiguration, mappingFile).mapSamples();
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      * @throws IOException 
      */
     public void addControlSamples(StudyConfiguration studyConfiguration, File controlSampleFile)
             throws ValidationException, IOException {
         new ControlSampleHelper(studyConfiguration, controlSampleFile).addControlSamples();
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void deployStudy(StudyConfiguration studyConfiguration) 
     throws ConnectionException, DataRetrievalException, ValidationException {
         studyConfiguration.setDeploymentStartDate(new Date());
         if (!studyConfiguration.getGenomicDataSources().isEmpty()) {
             new GenomicDataHelper(getCaArrayFacade(), getArrayDataService(), dao).loadData(studyConfiguration);
         }
         studyConfiguration.setStatus(Status.DEPLOYED);
         studyConfiguration.setDeploymentFinishDate(new Date());
         studyConfiguration.setStatusDescription("Minutes for deployment (approx):  " 
                 + DateUtil.compareDatesInMinutes(studyConfiguration.getDeploymentStartDate(), 
                                                  studyConfiguration.getDeploymentFinishDate()));
         dao.save(studyConfiguration);
     }
 
     /**
      * @param dao the dao to set
      */
     public void setDao(CaIntegrator2Dao dao) {
         this.dao = dao;
     }
 
     /**
      * {@inheritDoc}
      */
     public void addGenomicSource(StudyConfiguration studyConfiguration,
             GenomicDataSourceConfiguration genomicSource) 
     throws ConnectionException, ExperimentNotFoundException, NoSamplesForExperimentException {
         List<Sample> samples = getCaArrayFacade().getSamples(genomicSource.getExperimentIdentifier(), 
                 genomicSource.getServerProfile());
         if (samples.isEmpty() && genomicSource.getCopyNumberMappingFile() == null) {
             throw new NoSamplesForExperimentException("There were no samples found for experiment '" 
             + genomicSource.getExperimentIdentifier() + "'");
         }        
         studyConfiguration.getGenomicDataSources().add(genomicSource);
         genomicSource.setStudyConfiguration(studyConfiguration);
         genomicSource.setSamples(samples);
         for (Sample sample : samples) {
             sample.setGenomicDataSource(genomicSource);
         }
         dao.save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     public <T> T getRefreshedStudyEntity(T entity) {
         Long id;
         try {
             id = (Long) entity.getClass().getMethod("getId").invoke(entity);
         } catch (Exception e) {
             throw new IllegalArgumentException("Entity doesn't have a getId() method", e);
         }
         if (id == null) {
             throw new IllegalArgumentException("Id was null");
         }
         return (T) dao.get(id, entity.getClass());
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
     public void setFileManager(FileManager fileManager) {
         this.fileManager = fileManager;
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public List<AnnotationDefinition> getMatchingDefinitions(List<String> keywords) {
         return dao.findMatches(keywords);
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public List<CommonDataElement> getMatchingDataElements(List<String> keywords) {
         return caDSRFacade.retreiveCandidateDataElements(keywords);
     }
 
     /**
      * @return the caDSRFacade
      */
     public CaDSRFacade getCaDSRFacade() {
         return caDSRFacade;
     }
 
     /**
      * @param caDSRFacade the caDSRFacade to set
      */
     public void setCaDSRFacade(CaDSRFacade caDSRFacade) {
         this.caDSRFacade = caDSRFacade;
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("PMD.ExcessiveMethodLength")
     @Transactional(rollbackFor = {ConnectionException.class, ValidationException.class })
     public void setDataElement(FileColumn fileColumn, 
                                 CommonDataElement dataElement, 
                                 Study study, 
                                 EntityTypeEnum entityType,
                                 String keywords) 
     throws ConnectionException, ValidationException {
         AnnotationDefinition annotationDefinition = createDefinition(fileColumn.getFieldDescriptor(), 
                                                                      study, 
                                                                      entityType);
         annotationDefinition.setDisplayName(dataElement.getLongName());
         if (dataElement.getDefinition().length() > DEFINITION_LENGTH) {
             dataElement.setDefinition(dataElement.getDefinition().substring(0, DEFINITION_LENGTH - 7) + "...");
         }
         annotationDefinition.setPreferredDefinition(dataElement.getDefinition());
         annotationDefinition.setCde(dataElement);
         annotationDefinition.setKeywords(keywords);
         ValueDomain valueDomain = dataElement.getValueDomain();
         if (valueDomain == null) {
             valueDomain = retrieveValueDomain(dataElement);
             dataElement.setValueDomain(valueDomain);
         }
         annotationDefinition.setType(valueDomain.getDataType());
         addPermissibleValuesFromValueDomain(annotationDefinition, valueDomain);
         ValidationResult validationResult = 
             validateCurrentValuesWithPermissibleValues(study, entityType, annotationDefinition);
         if (!validationResult.isValid()) {
             throw new ValidationException(validationResult);
         }
         dao.save(dataElement);
         dao.save(annotationDefinition);
         dao.save(fileColumn);
     }
 
     private ValidationResult validateCurrentValuesWithPermissibleValues(Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) {
         ValidationResult validationResult = new ValidationResult();
         validationResult.setValid(true);
         if (!annotationDefinition.getPermissibleValueCollection().isEmpty() 
             && !annotationDefinition.getAnnotationValueCollection().isEmpty()) {
             Set<String> invalidValues = 
                 PermissibleValueUtil.retrieveValuesNotPermissible(study, entityType, annotationDefinition, dao);
             if (!invalidValues.isEmpty()) {
                 StringBuffer message = new StringBuffer();
                 message.append("The following values exist that are NOT permissible for '" 
                                 + annotationDefinition.getDisplayName() + "': <br> {");
                 for (String invalidValue : invalidValues) {
                     message.append(" '" + invalidValue + "' ");
                 }
                 message.append("} <br> Please select a different Data Element.");
                 validationResult.setValid(false);
                 validationResult.setInvalidMessage(message.toString());
             }
         }
         return validationResult;
     }
 
     private ValueDomain retrieveValueDomain(CommonDataElement dataElement)
             throws ConnectionException {
         ValueDomain valueDomain;
         String dataElementVersion = dataElement.getVersion(); 
         valueDomain = caDSRFacade.retrieveValueDomainForDataElement(dataElement.getPublicID(), 
                                                     NumberUtils.isNumber(dataElementVersion) 
                                                     ? Float.valueOf(dataElementVersion) : null);
         dao.save(valueDomain);
         return valueDomain;
     }
 
     private void addPermissibleValuesFromValueDomain(AnnotationDefinition annotationDefinition, 
                                                      ValueDomain valueDomain) {
         if (valueDomain.getPermissibleValueCollection() != null 
             && !valueDomain.getPermissibleValueCollection().isEmpty()) {
             for (AbstractPermissibleValue permissibleValue 
                 : valueDomain.getPermissibleValueCollection()) {
                 annotationDefinition.getPermissibleValueCollection().add(permissibleValue);
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void setDefinition(Study study, FileColumn fileColumn, AnnotationDefinition annotationDefinition, 
                                 EntityTypeEnum entityType) {
         if (fileColumn.getFieldDescriptor().getDefinition() == null 
             || !fileColumn.getFieldDescriptor().getDefinition().equals(annotationDefinition)) {
             addDefinitionToStudy(fileColumn.getFieldDescriptor(), study, entityType, annotationDefinition);
             dao.save(annotationDefinition);
             dao.save(fileColumn);
             dao.save(study);
         }
     }
 
     /**
      * @return the nciaFacade
      */
     public NCIAFacade getNciaFacade() {
         return nciaFacade;
     }
 
     /**
      * @param nciaFacade the nciaFacade to set
      */
     public void setNciaFacade(NCIAFacade nciaFacade) {
         this.nciaFacade = nciaFacade;
     }
 
     /**
      * @return the caArrayFacade
      */
     public CaArrayFacade getCaArrayFacade() {
         return caArrayFacade;
     }
 
     /**
      * @param caArrayFacade the caArrayFacade to set
      */
     public void setCaArrayFacade(CaArrayFacade caArrayFacade) {
         this.caArrayFacade = caArrayFacade;
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
     public void setArrayDataService(ArrayDataService arrayDataService) {
         this.arrayDataService = arrayDataService;
     }
 
     /**
      * {@inheritDoc}
      */
     public ImageAnnotationConfiguration addImageAnnotationFile(
             ImageDataSourceConfiguration imageDataSourceConfiguration,
             File inputFile, String filename) throws ValidationException, IOException {
         File permanentFile = getFileManager().storeStudyFile(inputFile, filename,
                 imageDataSourceConfiguration.getStudyConfiguration());
         AnnotationFile annotationFile = AnnotationFile.load(permanentFile, dao);
         ImageAnnotationConfiguration imageAnnotationConfiguration = 
             new ImageAnnotationConfiguration(annotationFile, imageDataSourceConfiguration);
         dao.save(imageAnnotationConfiguration);
         return imageAnnotationConfiguration;
     }
 
     /**
      * {@inheritDoc}
      */
    @Transactional(rollbackFor = {ConnectionException.class, IllegalStateException.class })
     public void addImageSource(StudyConfiguration studyConfiguration, ImageDataSourceConfiguration imageSource)
             throws ConnectionException {
         imageSource.setStudyConfiguration(studyConfiguration);
         List<ImageSeriesAcquisition> acquisitions = getNciaFacade().getImageSeriesAcquisitions(
                     imageSource.getCollectionName(), imageSource.getServerProfile());
         imageSource.getImageSeriesAcquisitions().addAll(acquisitions);
         for (ImageSeriesAcquisition acquisition : acquisitions) {
             acquisition.setImageDataSource(imageSource);
         }
        studyConfiguration.getImageDataSources().add(imageSource);
         dao.save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public void loadImageAnnotation(StudyConfiguration studyConfiguration) throws ValidationException {
         for (ImageDataSourceConfiguration configuration 
                 : studyConfiguration.getImageDataSources()) {
             if (configuration.getImageAnnotationConfiguration() != null) {
                 configuration.getImageAnnotationConfiguration().loadAnnontation();
             }
         }
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      * @throws IOException 
      * @throws ValidationException 
      */
     public void mapImageSeriesAcquisitions(StudyConfiguration studyConfiguration, File mappingFile)
         throws ValidationException, IOException {
         new ImageSeriesAcquisitionMappingHelper(studyConfiguration, mappingFile).mapImageSeries();
         save(studyConfiguration);
     }
 
     /**
      * {@inheritDoc}
      */
     public AnnotationDefinition createDefinition(AnnotationFieldDescriptor descriptor, 
                                                  Study study, 
                                                  EntityTypeEnum entityType) {
         AnnotationDefinition annotationDefinition = new AnnotationDefinition();
         annotationDefinition.setAnnotationValueCollection(new HashSet<AbstractAnnotationValue>());
         annotationDefinition.setPermissibleValueCollection(new HashSet<AbstractPermissibleValue>());
         annotationDefinition.setDisplayName(descriptor.getName());
         annotationDefinition.setType(AnnotationTypeEnum.STRING.getValue());
         annotationDefinition.setKeywords(annotationDefinition.getDisplayName());
         addDefinitionToStudy(descriptor, study, entityType, annotationDefinition);
         dao.save(annotationDefinition);
         dao.save(descriptor);
         dao.save(study);
         return annotationDefinition;
     }
     
     @SuppressWarnings({ "PMD.ExcessiveMethodLength" }) // Switch Statement and null checks
     private void addDefinitionToStudy(AnnotationFieldDescriptor descriptor, Study study, EntityTypeEnum entityType,
             AnnotationDefinition annotationDefinition) {
         AnnotationDefinition annotationDefinitionToRemove = null;
         if (descriptor.getDefinition() != null) {
             annotationDefinitionToRemove = descriptor.getDefinition();
         }
         descriptor.setDefinition(annotationDefinition);
         switch(entityType) {
             case SUBJECT:
                 study.getSubjectAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getSubjectAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             case IMAGESERIES:
                 study.getImageSeriesAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getImageSeriesAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             case SAMPLE:
                 study.getSampleAnnotationCollection().add(annotationDefinition);
                 if (annotationDefinitionToRemove != null) {
                     moveValuesToNewDefinition(study, annotationDefinition, annotationDefinitionToRemove);
                     study.getSampleAnnotationCollection().remove(annotationDefinitionToRemove);
                 }
                 break;
             default:
                 throw new IllegalStateException("Unknown EntityTypeEnum");
         }
     }
 
     /**
      * Moves AbstractAnnotationValues from one AnnotationDefinition to another.
      * @param study - Study that the values belong to.
      * @param annotationDefinition - new AnnotationDefinition where the Values will belong.
      * @param annotationDefinitionToRemove - Old AnnotationDefinition.
      */
     private void moveValuesToNewDefinition(Study study, AnnotationDefinition annotationDefinition,
             AnnotationDefinition annotationDefinitionToRemove) {
         if (annotationDefinition.getAnnotationValueCollection() == null) {
             annotationDefinition.setAnnotationValueCollection(new HashSet<AbstractAnnotationValue>());
         }
         if (annotationDefinitionToRemove.getAnnotationValueCollection() != null 
             && !annotationDefinitionToRemove.getAnnotationValueCollection().isEmpty()) {
             for (AbstractAnnotationValue value : annotationDefinitionToRemove.getAnnotationValueCollection()) {
                 if (studyContainsAnnotationValue(value, study)) {
                     value.setAnnotationDefinition(annotationDefinition);
                     annotationDefinition.getAnnotationValueCollection().add(value);
                 }
             }
         }
     }
     
     private boolean studyContainsAnnotationValue(AbstractAnnotationValue value, Study study) {
         if (value.getSubjectAnnotation() != null 
                 && study.equals(value.getSubjectAnnotation().getStudySubjectAssignment().getStudy())) {
             return true;
         } else if (value.getImageSeries() != null 
                 && study.equals(value.getImageSeries().getImageStudy().getAssignment().getStudy())) {
             return true;
         } else if (value.getSampleAcquisition() != null
                 && study.equals(value.getSampleAcquisition().getAssignment().getStudy())) { 
             return true;
         } else if (value.getImage() != null 
                 && study.equals(value.getImage().getSeries().getImageStudy().getAssignment().getStudy())) {
             return true;
         }
         return false;
     }
 
     /**
      * @return the workspaceService
      */
     public WorkspaceService getWorkspaceService() {
         return workspaceService;
     }
 
     /**
      * @param workspaceService the workspaceService to set
      */
     public void setWorkspaceService(WorkspaceService workspaceService) {
         this.workspaceService = workspaceService;
     }
 
     /**
      * {@inheritDoc}
      */
     @Transactional(readOnly = true)
     public boolean isDuplicateStudyName(Study study) {
         return dao.isDuplicateStudyName(study);
     }
     
     /**
      * {@inheritDoc}
      */
     public SurvivalValueDefinition createNewSurvivalValueDefinition(Study study) {
         SurvivalValueDefinition survivalValueDefinition = new SurvivalValueDefinition();
         study.getSurvivalValueDefinitionCollection().add(survivalValueDefinition);
         survivalValueDefinition.setName("[New Name]");
         dao.save(study);
         return survivalValueDefinition;
     }
     
     /**
      * {@inheritDoc}
      */
     public void removeSurvivalValueDefinition(Study study, SurvivalValueDefinition survivalValueDefinition) {
        study.getSurvivalValueDefinitionCollection().remove(survivalValueDefinition);
        Collection <SurvivalValueDefinition> objectsToRemove = new HashSet<SurvivalValueDefinition>();
        objectsToRemove.add(survivalValueDefinition);
        dao.removeObjects(objectsToRemove);
        dao.save(study);
     }
 
     /**
      * {@inheritDoc}
      */
     public ImageDataSourceConfiguration retrieveImageDataSource(Study study) {
         ImageDataSourceConfiguration dataSource = dao.retrieveImagingDataSourceForStudy(study);
         if (dataSource != null) {
             Hibernate.initialize(dataSource.getServerProfile());
         }
         return dataSource;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @throws IOException 
      */
     public void saveCopyNumberMappingFile(GenomicDataSourceConfiguration source,
             File mappingFile, String filename) throws IOException {
         File savedFile = getFileManager().storeStudyFile(mappingFile, filename, source.getStudyConfiguration());
         source.setCopyNumberMappingFile(new CopyNumberMappingFile());
         source.getCopyNumberMappingFile().setPath(savedFile.getAbsolutePath());
         dao.save(source);
     }
 
 }
