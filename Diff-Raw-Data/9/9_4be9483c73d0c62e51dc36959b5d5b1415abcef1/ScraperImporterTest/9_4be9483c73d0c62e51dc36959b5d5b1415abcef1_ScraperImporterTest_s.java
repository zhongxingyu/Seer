 package org.ocha.dap.importer;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.springframework.core.io.ClassPathResource;
 
 public class ScraperImporterTest {
 
 	@Test
 	public void testPrepareDataForImport() throws IOException {
 		final ScraperImporter scraperImporter = new ScraperImporter();
 
 		final File csvValueFile = new ClassPathResource("samples/scraper/csv.zip").getFile();
 
 		final PreparedData preparedData = scraperImporter.prepareDataForImport(csvValueFile);
 		Assert.assertTrue(preparedData.isSuccess());
		Assert.assertEquals(38392, preparedData.getIndicatorsToImport().size());
 	}
 
 }
