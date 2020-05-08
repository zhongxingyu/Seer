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
 package gov.nih.nci.caintegrator2.application.study.deployment;
 
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataService;
 import gov.nih.nci.caintegrator2.application.arraydata.ArrayDataValues;
 import gov.nih.nci.caintegrator2.application.arraydata.PlatformHelper;
 import gov.nih.nci.caintegrator2.application.study.GenomicDataSourceConfiguration;
 import gov.nih.nci.caintegrator2.application.study.ValidationException;
 import gov.nih.nci.caintegrator2.common.Cai2Util;
 import gov.nih.nci.caintegrator2.data.CaIntegrator2Dao;
 import gov.nih.nci.caintegrator2.domain.genomic.AbstractReporter;
 import gov.nih.nci.caintegrator2.domain.genomic.ArrayData;
 import gov.nih.nci.caintegrator2.domain.genomic.ReporterList;
 import gov.nih.nci.caintegrator2.domain.genomic.Sample;
 import gov.nih.nci.caintegrator2.external.ConnectionException;
 import gov.nih.nci.caintegrator2.external.DataRetrievalException;
 import gov.nih.nci.caintegrator2.external.caarray.CaArrayFacade;
 import gov.nih.nci.caintegrator2.external.caarray.GenericMultiSamplePerFileParser;
 import gov.nih.nci.caintegrator2.external.caarray.SupplementalDataFile;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Reads and retrieves copy number data from a caArray instance.
  */
 @Transactional (propagation = Propagation.REQUIRED)
 class GenericSupplementalMultiSamplePerFileHandler extends AbstractGenericSupplementalMappingFileHandler {
     
     private final List<Sample> samples = new ArrayList<Sample>();
     private final List<String> dataFileNames = new ArrayList<String>();
     private final List<SupplementalDataFile> dataFiles = new ArrayList<SupplementalDataFile>();
     
     private static final Logger LOGGER = Logger.getLogger(GenericSupplementalMultiSamplePerFileHandler.class);
    private static final int FIVE_THOUSAND = 5000;
     
     GenericSupplementalMultiSamplePerFileHandler(GenomicDataSourceConfiguration genomicSource,
             CaArrayFacade caArrayFacade, ArrayDataService arrayDataService, CaIntegrator2Dao dao) {
         super(genomicSource, caArrayFacade, arrayDataService, dao);
     }
 
     void mappingSample(String subjectIdentifier, String sampleName, SupplementalDataFile supplementalDataFile)
     throws FileNotFoundException, ValidationException {
         samples.add(getSample(sampleName, subjectIdentifier));
         if (!dataFileNames.contains(supplementalDataFile.getFileName())) {
             dataFileNames.add(supplementalDataFile.getFileName());
             dataFiles.add(supplementalDataFile);
         }
     }
 
     List<ArrayDataValues> loadArrayDataValue() 
     throws ConnectionException, DataRetrievalException, ValidationException {
         PlatformHelper platformHelper = new PlatformHelper(getDao().getPlatform(
             getGenomicSource().getPlatformName()));
         Set<ReporterList> reporterLists = platformHelper.getReporterLists(getReporterType());
         
         return createArrayDataByBucket(platformHelper, reporterLists);
     }
 
     private List<ArrayDataValues> createArrayDataByBucket(PlatformHelper platformHelper,
             Set<ReporterList> reporterLists) throws DataRetrievalException, ConnectionException, ValidationException {
         List<ArrayDataValues> arrayDataValuesList = new ArrayList<ArrayDataValues>();
         for (List<String> sampleBucket : createSampleBuckets(reporterLists, getSampleList())) {
             Map<String, Map<String, List<Float>>> dataMap = extractData(sampleBucket);
             loadArrayData(arrayDataValuesList, platformHelper, reporterLists, dataMap);
         }
         return arrayDataValuesList;
     }
 
     private List<List<String>> createSampleBuckets(Set<ReporterList> reporterLists, List<String> sampleList) {
         List<List<String>> sampleBuckets = new ArrayList<List<String>>();
         long sampleBucketSize = computeBucketSize(reporterLists);
         int sampleCount = 0;
         List<String> sampleBucket = new ArrayList<String>();
         for (String sample : sampleList) {
             if (sampleCount++ % sampleBucketSize == 0) {
                 sampleBucket = new ArrayList<String>();
                 sampleBuckets.add(sampleBucket);
             }
             sampleBucket.add(sample);
         }
         return sampleBuckets;
     }
 
     private long computeBucketSize(Set<ReporterList> reporterLists) {
         int numReporters = 0;
         for (ReporterList reporterList : reporterLists) {
             numReporters += reporterList.getReporters().size();
         }
        return (FIVE_THOUSAND * Cai2Util.getHeapSizeMB()) / numReporters;
     }
 
     private Map<String, Map<String, List<Float>>> extractData(List<String> mappedSampleBucket)
     throws DataRetrievalException, ConnectionException, ValidationException {
         Map<String, Map<String, List<Float>>> dataMap = new HashMap<String, Map<String, List<Float>>>();
         for (SupplementalDataFile dataFile : dataFiles) {
             GenericMultiSamplePerFileParser parser = new GenericMultiSamplePerFileParser(
                     getDataFile(dataFile.getFileName()), dataFile.getProbeNameHeader(),
                     dataFile.getSampleHeader(), mappedSampleBucket);
             parser.loadData(dataMap);
         }
         validateSampleMapping(dataMap, getSampleList());
         return dataMap;
     }
 
     private void validateSampleMapping(Map<String, Map<String, List<Float>>> dataMap, List<String> sampleList)
     throws DataRetrievalException {
         StringBuffer errorMsg = new StringBuffer();
         for (String sampleName : sampleList) {
             if (!dataMap.containsKey(sampleName)) {
                 if (errorMsg.length() > 0) {
                     errorMsg.append(", ");
                 }
                 errorMsg.append(sampleName);
             }
         }
         if (errorMsg.length() > 0) {
             throw new DataRetrievalException("Sample not found error: " + errorMsg.toString());
         }
     }
 
     private List<String> getSampleList() {
         List<String> sampleNames = new ArrayList<String>();
         for (Sample sample : samples) {
             sampleNames.add(sample.getName());
         }
         return sampleNames;
     }
 
     private Sample getSample(String sampleName) {
         for (Sample sample : samples) {
             if (sampleName.equals(sample.getName())) {
                 return sample;
             }
         }
         return null;
     }
     
     private void loadArrayData(List<ArrayDataValues> arrayDataValuesList, PlatformHelper platformHelper,
             Set<ReporterList> reporterLists, Map<String, Map<String, List<Float>>> dataMap)
     throws DataRetrievalException {
         for (String sampleName : dataMap.keySet()) {
             LOGGER.info("Start LoadArrayData for : " + sampleName);
             ArrayData arrayData = createArrayData(getSample(sampleName), reporterLists, getDataType());
             getDao().save(arrayData);
             ArrayDataValues values = new ArrayDataValues(platformHelper
                     .getAllReportersByType(getReporterType()));
             arrayDataValuesList.add(values);
             Map<String, List<Float>> reporterMap = dataMap.get(sampleName);
             for (String probeName : reporterMap.keySet()) {
                 AbstractReporter reporter = platformHelper.getReporter(getReporterType(), probeName);
                 if (reporter == null) {
                     LOGGER.warn("Reporter with name " + probeName + " was not found in platform "
                             + platformHelper.getPlatform().getName());
                 } else {
                     values.setFloatValue(arrayData, reporter, getDataValueType(), reporterMap
                             .get(probeName), getCentralTendencyCalculator());
                 }
             }
             getArrayDataService().save(values);
             LOGGER.info("Done LoadArrayData for : " + sampleName);
         }
     }
 
  }
