 package gov.nih.nci.rembrandt.queryservice.test;
 
 import gov.nih.nci.caintegrator.analysis.messaging.SampleGroup;
 import gov.nih.nci.caintegrator.dto.critieria.DiseaseOrGradeCriteria;
 import gov.nih.nci.caintegrator.dto.de.ArrayPlatformDE;
 import gov.nih.nci.caintegrator.dto.de.DiseaseNameDE;
 import gov.nih.nci.caintegrator.dto.de.ExprFoldChangeDE;
 import gov.nih.nci.caintegrator.dto.de.MultiGroupComparisonAdjustmentTypeDE;
 import gov.nih.nci.caintegrator.dto.de.StatisticTypeDE;
 import gov.nih.nci.caintegrator.dto.de.StatisticalSignificanceDE;
 import gov.nih.nci.caintegrator.dto.query.ClassComparisonQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.ClinicalQueryDTO;
 import gov.nih.nci.caintegrator.dto.query.QueryType;
 import gov.nih.nci.caintegrator.dto.view.ViewFactory;
 import gov.nih.nci.caintegrator.dto.view.ViewType;
 import gov.nih.nci.caintegrator.enumeration.ArrayPlatformType;
 import gov.nih.nci.caintegrator.enumeration.FindingStatus;
 import gov.nih.nci.caintegrator.enumeration.MultiGroupComparisonAdjustmentType;
 import gov.nih.nci.caintegrator.enumeration.Operator;
 import gov.nih.nci.caintegrator.enumeration.StatisticalMethodType;
 import gov.nih.nci.caintegrator.enumeration.StatisticalSignificanceType;
 import gov.nih.nci.caintegrator.exceptions.FrameworkException;
 import gov.nih.nci.caintegrator.service.findings.Finding;
 import gov.nih.nci.rembrandt.cache.CacheManagerDelegate;
 import gov.nih.nci.rembrandt.cache.ConvenientCache;
 import gov.nih.nci.rembrandt.dto.query.ClinicalDataQuery;
 import gov.nih.nci.rembrandt.queryservice.QueryManager;
 import gov.nih.nci.rembrandt.service.findings.RembrandtFindingsFactory;
 import gov.nih.nci.rembrandt.util.ApplicationContext;
 import gov.nih.nci.rembrandt.web.factory.ApplicationFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.StringTokenizer;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 /**
  * This test case was written to simulate a Front End call to the Middle-Tier to 
  * create a Class Comparison Finding
  * 
  * @author BauerD, SahniH
  *
  */
 
 public class AnalysisQueryTest extends TestCase {
 	private SampleGroup gbmGrp = new SampleGroup("GBM");
 	private SampleGroup astroGrp = new SampleGroup("ASTRO");
 	private SampleGroup normalGrp = new SampleGroup("NORMAL");
 	private SampleGroup oligoGrp = new SampleGroup("OLIGO");
 	private SampleGroup mixedGrp = new SampleGroup("MIXED");
 	
 	private String gbmHFids = "HF0024,HF0031,HF0048,HF0050,HF0066,HF0089,HF0138,HF0142,HF0180,HF0184,HF0212,HF0218,HF0244,HF0268,HF0300.3,HF0316,HF0350,HF0408,HF0435,HF0442.5,HF0445,HF0460,HF0505,HF0520,HF0543,HF0583,HF0627,HF0652.4,HF0654,HF0702,HF0790,HF0850,HF0855,HF0894,HF0936,HF0954.2,HF0963,HF0982,HF0986,HF0990,HF0992,HF0996,HF1057,HF1058,HF1077,HF1078,HF1097,HF1122,HF1137,HF1178,HF1186,HF1191,HF1220,HF1242,HF1255,HF1262,HF1280,HF1286,HF1292,HF1318,HF1326,HF1338,HF1356,HF1357,HF1382,HF1397,HF1409,HF1458,HF1475,HF1490,HF1492,HF1494,HF1509,HF1517,HF1534,HF1538,HF1540,HF1585,HF1589,HF1608,HF1618,HF1628,HF1640,HF1667,HF1671,HF1702";
 	private String oligoHFids = "HF0087,HF0251,HF0285,HF0291,HF0327,HF0329,HF0332,HF0434,HF0453,HF0471,HF0488,HF0510,HF0599,HF0615,HF0639,HF0670,HF0726,HF0813,HF0816,HF0822,HF0828,HF0835,HF0897,HF0899,HF0914,HF0920,HF0931,HF0960,HF0962,HF0966,HF0975,HF1136,HF1150,HF1156,HF1167,HF1185,HF1219,HF1227,HF1235,HF1325,HF1334,HF1345,HF1348,HF1380,HF1381,HF1489,HF1493,HF1502,HF1551,HF1606,HF1613,HF1677";
 	private String astroHFids = "HF0017,HF0026,HF0108,HF0152,HF0189,HF0223,HF0450,HF0491,HF0608,HF0757,HF0778,HF0953,HF1000,HF1032,HF1139,HF1232,HF1246,HF1269,HF1295,HF1316,HF1344,HF1366,HF1407,HF1442,HF1469,HF1487,HF1511,HF1568,HF1581,HF1587,HF1708";
 	private String mixedHFids = "HF0022,HF0183,HF0252,HF0305,HF0606,HF0802,HF0844,HF0891,HF1090,HF1297,HF1319,HF1588";
 	private String normalHFids = "HF0088,HF0120,HF0131,HF0137,HF0141,HF0151,HF0163,HF0171,HF0178,HF0201,HF0211,HF0232,HF0295,HF0303,HF0312,HF0377,HF0383,HF0467,HF0512,HF0523,HF0526,HF0533,HF0593,HF0616";
 
 	private ClassComparisonQueryDTO classComparisonQueryDTO;
 	private static ConvenientCache cacheManagerDelegate  = CacheManagerDelegate.getInstance();
 	public AnalysisQueryTest(String string) {
 		super(string);
 		
 	}
 
 	public static Test suite() {
 		TestSuite suite =  new TestSuite();
        suite.addTest(new AnalysisQueryTest("testCCQueryCompleted"));
        suite.addTest(new AnalysisQueryTest("testCCQueryError"));
        
         //suite.addTest(new AnalysisQueryTest("testPCAQuery"));
         //suite.addTest(new AnalysisQueryTest("testHCQuery"));
 
 
         return suite;
 	}
 
 	public static void main (String[] args) {		
 		junit.textui.TestRunner.run(suite());
 
     }
 	protected void setUp() throws Exception {
 		super.setUp();
 		ApplicationContext.init();
 		setUpCCQuery();
 		
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	private void setUpCCQuery(){
 		classComparisonQueryDTO = (ClassComparisonQueryDTO)ApplicationFactory.newQueryDTO(QueryType.CLASS_COMPARISON_QUERY);
 		classComparisonQueryDTO.setQueryName("CCQuery");
 		classComparisonQueryDTO.setStatisticTypeDE(new StatisticTypeDE(StatisticalMethodType.TTest));
 		classComparisonQueryDTO.setStatisticalSignificanceDE(new StatisticalSignificanceDE(0.5,Operator.GT,StatisticalSignificanceType.adjustedpValue));
 		classComparisonQueryDTO.setMultiGroupComparisonAdjustmentTypeDE(new MultiGroupComparisonAdjustmentTypeDE(MultiGroupComparisonAdjustmentType.FWER ));
 		classComparisonQueryDTO.setArrayPlatformDE(new ArrayPlatformDE(ArrayPlatformType.AFFY_OLIGO_PLATFORM.toString()));
 		classComparisonQueryDTO.setExprFoldChangeDE(new ExprFoldChangeDE.UpRegulation(new Float(2)));
 		
 		Collection<ClinicalQueryDTO> groupCollection= new ArrayList<ClinicalQueryDTO>();
 		//Create ClinicalQueryDTO 1 (Class 1) for the class comparison
 		ClinicalDataQuery group1 = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 		group1.setQueryName("GBM");
 		DiseaseOrGradeCriteria diseaseCrit = new DiseaseOrGradeCriteria();
 		diseaseCrit.setDisease(new DiseaseNameDE("GBM"));
 		group1.setDiseaseOrGradeCrit(diseaseCrit);
 		group1.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 		groupCollection.add(group1);
 		
 		//Create ClinicalQueryDTO 2 (Class 2) for the class comparison
 		ClinicalDataQuery group2 = (ClinicalDataQuery) QueryManager.createQuery(QueryType.CLINICAL_DATA_QUERY_TYPE);
 		group1.setQueryName("OLIG");
 		diseaseCrit = new DiseaseOrGradeCriteria();
 		diseaseCrit.setDisease(new DiseaseNameDE("OLIG"));
 		group2.setDiseaseOrGradeCrit(diseaseCrit);
 		group2.setAssociatedView(ViewFactory.newView(ViewType.CLINICAL_VIEW));
 		groupCollection.add(group2);
 		
 		classComparisonQueryDTO.setComparisonGroups(groupCollection);
 			
 	}
	public void testCCQueryCompleted(){
 		RembrandtFindingsFactory factory = new RembrandtFindingsFactory();
 		Finding finding = null;
 		try {
 			finding = factory.createClassComparisonFinding(classComparisonQueryDTO,"mySession","CCQuery");
 		} catch (FrameworkException e) {
 			e.printStackTrace();
 		}
 		FindingStatus status = finding.getStatus();
 		assert(status == FindingStatus.Running);
 		
 		while(finding.getStatus() == FindingStatus.Running){
 			 finding = cacheManagerDelegate.getFinding(finding.getSessionId(),finding.getTaskId());
 			 try {
 				Thread.sleep(500);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		status = finding.getStatus();
 		assert(status == FindingStatus.Completed);
 	}
	public void testCCQueryError(){
		RembrandtFindingsFactory factory = new RembrandtFindingsFactory();
		Finding finding = null;
		try {
			//create an error
			classComparisonQueryDTO.setStatisticalSignificanceDE(new StatisticalSignificanceDE(0.5,Operator.GT,StatisticalSignificanceType.adjustedpValue));
			classComparisonQueryDTO.setMultiGroupComparisonAdjustmentTypeDE(new MultiGroupComparisonAdjustmentTypeDE(MultiGroupComparisonAdjustmentType.NONE ));

			finding = factory.createClassComparisonFinding(classComparisonQueryDTO,"mySession","CCQuery");
		} catch (FrameworkException e) {
			e.printStackTrace();
		}
		FindingStatus status = finding.getStatus();
		assert(status == FindingStatus.Running);
		
		while(finding.getStatus() == FindingStatus.Running){
			 finding = cacheManagerDelegate.getFinding(finding.getSessionId(),finding.getTaskId());
			 try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		status = finding.getStatus();
		assert(status == FindingStatus.Error);
	}
 	private void initializeSampleGroups() {
 		  initializeSampleGroup(gbmGrp, gbmHFids);
 		  initializeSampleGroup(astroGrp, astroHFids);
 		  initializeSampleGroup(oligoGrp, oligoHFids);
 		  initializeSampleGroup(mixedGrp, mixedHFids);
 		  initializeSampleGroup(normalGrp, normalHFids);
 		}
 		
 		private void initializeSampleGroup(SampleGroup grp, String identifierStr) {
 		  StringTokenizer t = new StringTokenizer(identifierStr, ",");
 		  String id;
 		  while(t.hasMoreTokens()) {
 		    id = t.nextToken().trim();
 		    grp.add(id.trim());
 		  }
 		}
 }
