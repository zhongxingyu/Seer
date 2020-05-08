 package gov.nih.nci.caintegrator2.application.study.deployment;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import gov.nih.nci.caintegrator2.TestDataFiles;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataServiceStub;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformDataTypeEnum;
 import gov.nih.nci.caintegrator2.application.study.DnaAnalysisDataConfiguration;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.StudyConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.application.study.deployment.AbstractAffymetrixDnaAnalysisMappingFileHandler;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2DaoStub;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.DnaAnalysisReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.Platform;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterTypeEnum;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacade;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacadeStub;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Test;
 
 import affymetrix.calvin.data.ProbeSetMultiDataCopyNumberData;
 import affymetrix.calvin.data.CHPMultiDataData.MultiDataType;
 import affymetrix.calvin.exception.UnsignedOutOfLimitsException;
 import affymetrix.fusion.chp.FusionCHPDataReg;
 import affymetrix.fusion.chp.FusionCHPMultiDataData;
 
 public class AffymetrixCopyNumberMappingFileHandlerTest {
 
     @Test
    public void testLoadCopyNumberData() throws DataRetrievalException, ConnectionException, ValidationException {
         CaArrayFacade caArrayFacade = new LocalCaArrayFacadeStub();
         ArrayDataServiceStub arrayDataService = new ArrayDataServiceStub();
         CaIntegrator2DaoStub dao = new LocalCaIntegrator2DaoStub();
         GenomicDataSourceConfiguration source = new GenomicDataSourceConfiguration();
         StudyConfiguration studyConfiguration = new StudyConfiguration();
         studyConfiguration.getOrCreateSubjectAssignment("E09176");
         source.setStudyConfiguration(studyConfiguration);
         source.setDataType(PlatformDataTypeEnum.COPY_NUMBER);
         source.setDnaAnalysisDataConfiguration(new DnaAnalysisDataConfiguration());
         source.getDnaAnalysisDataConfiguration().setMappingFilePath(TestDataFiles.SHORT_COPY_NUMBER_FILE.getAbsolutePath());
        AbstractAffymetrixDnaAnalysisMappingFileHandler handler = new AffymetrixCopyNumberMappingFileHandler(source, caArrayFacade, arrayDataService, dao);
        handler.loadArrayData();
         Set<ArrayData> arrayDatas = studyConfiguration.getStudy().getArrayDatas(ReporterTypeEnum.DNA_ANALYSIS_REPORTER, null);
         assertEquals(1, arrayDatas.size());
         for (ArrayData arrayData : arrayDatas) {
             checkArrayData(arrayData);
         }
     }
 
     private void checkArrayData(ArrayData arrayData) {
         assertNotNull(arrayData.getArray());
         assertNotNull(arrayData.getArray().getPlatform());
         assertTrue(arrayData.getArray().getArrayDataCollection().contains(arrayData));
         assertNotNull(arrayData.getSample());
         assertTrue(arrayData.getSample().getArrayDataCollection().contains(arrayData));
         assertTrue(arrayData.getSample().getArrayCollection().contains(arrayData.getArray()));
     }
 
     static class LocalCaIntegrator2DaoStub extends CaIntegrator2DaoStub {
         
         private Platform platform;
 
         @Override
         public Platform getPlatform(String name) {
             return getPlatform();
         }
 
         private Platform getPlatform() {
             if (platform != null) {
                 return platform;
             }
             try {
                 Platform platform = new Platform();
                 FusionCHPMultiDataData.registerReader();
                 addReporterList(platform, TestDataFiles.HIND_COPY_NUMBER_CHP_FILE, "Mapping50K_Hind240");
                 addReporterList(platform, TestDataFiles.XBA_COPY_NUMBER_CHP_FILE, "Mapping50K_Xba240");
                 return platform;
             } catch (Exception e) {
                 throw new IllegalStateException(e);
             }
         }
 
         private void addReporterList(Platform platform, File chpFile, String reporterListName) throws IOException, UnsignedOutOfLimitsException {
             FusionCHPMultiDataData chpData = FusionCHPMultiDataData.fromBase(FusionCHPDataReg.read(chpFile.getAbsolutePath()));
             ReporterList reporterList = platform.addReporterList(reporterListName, ReporterTypeEnum.DNA_ANALYSIS_REPORTER);
             int numProbeSets = chpData.getEntryCount(MultiDataType.CopyNumberMultiDataType);
             for (int i = 0; i < numProbeSets; i++) {
                 ProbeSetMultiDataCopyNumberData probeSetData = 
                     chpData.getCopyNumberEntry(MultiDataType.CopyNumberMultiDataType, i);
                 DnaAnalysisReporter reporter = new DnaAnalysisReporter();
                 reporter.setName(probeSetData.getName());
                 reporterList.getReporters().add(reporter);
             }
         }
 
         @Override
         public ReporterList getReporterList(String name) {
             for (ReporterList reporterList : getPlatform().getReporterLists()) {
                 if (name.equals(reporterList.getName())) {
                     return reporterList;
                 }
             }
             return null;
         }
         
     }
 
     static class LocalCaArrayFacadeStub extends CaArrayFacadeStub {
         
         @Override
         public byte[] retrieveFile(GenomicDataSourceConfiguration source, String filename) {
             try {
                 return FileUtils.readFileToByteArray(TestDataFiles.HIND_COPY_NUMBER_CHP_FILE);
             } catch (IOException e) {
                 throw new IllegalStateException(e);
             }
         }
     }
 
 }
