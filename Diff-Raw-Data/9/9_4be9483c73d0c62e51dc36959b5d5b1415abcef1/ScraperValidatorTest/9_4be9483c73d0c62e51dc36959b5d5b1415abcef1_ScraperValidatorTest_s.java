 package org.ocha.dap.validation;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.ocha.dap.model.validation.ValidationReportEntry;
 import org.ocha.dap.model.validation.ValidationStatus;
 import org.springframework.core.io.ClassPathResource;
 
 public class ScraperValidatorTest {
 
 	@Test
 	public void testEvaluateFile() throws IOException {
 		final ScraperValidator scraperValidator = new ScraperValidator();
 
		final File csvFile = new ClassPathResource("samples/scraper/csv-from-dragon.zip").getFile();
 		Assert.assertEquals(ValidationStatus.SUCCESS, scraperValidator.evaluateFile(csvFile).getStatus());
 		Assert.assertEquals(3, scraperValidator.evaluateFile(csvFile).getEntries().size());
 	}
 
 	@Test
 	public void testValidateDatasetFile() throws IOException {
 		final ScraperValidator scraperValidator = new ScraperValidator();
 
 		{
 			final File csvFile = new ClassPathResource("samples/scraper/dataset-from-csv-from-dragon.csv").getFile();
 			final List<ValidationReportEntry> result = scraperValidator.validateDatasetFile(csvFile);
 			Assert.assertEquals(0, result.size());
 		}
 
 		{
 			final File csvFile = new ClassPathResource("samples/scraper/dataset-from-csv-from-dragon-without-first-line.csv").getFile();
 			final List<ValidationReportEntry> result = scraperValidator.validateDatasetFile(csvFile);
 			Assert.assertEquals(1, result.size());
 			Assert.assertEquals(ValidationStatus.ERROR, result.get(0).getStatus());
 		}
 
 	}
 }
