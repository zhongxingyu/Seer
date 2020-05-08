 package eu.trentorise.opendata.ckanalyze.analyzers;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.WriterAppender;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 import eu.trentorise.opendata.ckanalyze.jpa.Catalog;
 import eu.trentorise.opendata.ckanalyze.jpa.Resource;
 import eu.trentorise.opendata.ckanalyze.jpa.ResourceStringDistribution;
 import eu.trentorise.opendata.ckanalyze.main.AnalysisMain;
 import eu.trentorise.opendata.ckanalyze.managers.PersistencyManager;
 
 public class TestCatalogAnalyzer {
 	@Test
 	public void analyzeDataset() {
 		try {
 			StringWriter toCheck = new StringWriter();
 			WriterAppender appender = new WriterAppender(new PatternLayout(
 					"%d{ISO8601} %p - %m%n"), toCheck);
 			appender.setName("LOGGER_APPENDER");
			Logger.getRootLogger().addAppender(appender);
 			appender.setThreshold(org.apache.log4j.Level.WARN);
 			List<String> dstest = new ArrayList<>();
 			dstest.add("rendiconto-del-2005");
 			AnalysisMain.tempDirConfig();
 			AnalysisMain.catalogAnalysis("http://dati.trentino.it", dstest);
 			assertTrue(toCheck.toString().isEmpty());
 			Session ss = PersistencyManager.getSessionFactory().openSession();
 			Query q = ss.createQuery("from Catalog");
 			@SuppressWarnings("unchecked")
 			List<Catalog> cats = q.list();
 			assertFalse(cats.isEmpty());
 			Catalog c = cats.get(0);
 			Set<Resource> ress = c.getCatalogResources();
 			double avgColumnCount = 0;
 			double avgRowCount = 0;
 			double avgStringLength = 0;
 			long strfields = 0;
 			for (Resource resource : ress) {
 				long strNum = 0;
 				avgRowCount = avgRowCount + resource.getRowCount();
 				avgColumnCount = avgColumnCount + resource.getColumnCount();
 				Set<ResourceStringDistribution> rdts = resource.getStringDistribution();
 				for (ResourceStringDistribution resourceStringDistribution : rdts) {
 					strNum = strNum + resourceStringDistribution.getFreq();
 				}
 				strfields = strfields + strNum;
 				avgStringLength = avgStringLength + (resource.getStringAvg() * strNum);
 				
 			}
 			assertFalse(avgRowCount == 0);
 			assertFalse(avgColumnCount == 0);
 			assertFalse(avgStringLength == 0);
 			avgColumnCount = avgColumnCount / ress.size();
 			avgRowCount = avgRowCount / ress.size();
 			avgStringLength = avgStringLength / strfields;
 			assertTrue(avgColumnCount == c.getAvgColumnCount());
 			assertTrue(avgRowCount == c.getAvgRowCount());
 			assertTrue(avgStringLength == c.getAvgStringLength());
 			ss.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 			assertTrue(false);
 			
 		}		
 
 	}
 }
