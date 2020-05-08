 package com.ba.languagechecker;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.log4j.Logger;
 
 import com.ba.languagechecker.pagechecker.output.CVSCrawlerOutputStream;
 import com.ba.languagechecker.pagechecker.output.ICrawlerOutputStream;
 import com.ba.languagechecker.properties.CrawlerProperties;
 import com.ba.languagechecker.properties.TaskProperties;
 
 public class App {
 	private static final String RESULT_FOLDER = "results/";
 	private final static String TASK_PROPERTIES_FILE_NAME = "task.properties";
 	private final static String CRAWLER_PROPERTIES_FILE_NAME = "crawler.properties";
 
 	private static Logger _log = Logger.getLogger(App.class.getCanonicalName());
 
 	public static void main(String[] args) throws FileNotFoundException,
 			IOException {
 		try (final FileInputStream crawlerPropertiesFileStream = new FileInputStream(
 				CRAWLER_PROPERTIES_FILE_NAME)) {
 			final CrawlerProperties crawlerProperties = new CrawlerProperties();
 			crawlerProperties.load(crawlerPropertiesFileStream);
 			final String taskPropertiesFileName = (args.length > 0) ? args[0]
 					: TASK_PROPERTIES_FILE_NAME;
			try (final FileInputStream faskPropertiesFileStream = new FileInputStream(
 					taskPropertiesFileName)) {
 				final TaskProperties taskProperties = new TaskProperties();
 				try (final OutputStream os = new FileOutputStream(RESULT_FOLDER
 						+ taskProperties.getOutputFileName())) {
 					try (final ICrawlerOutputStream crawlerOutputStream = new CVSCrawlerOutputStream(
 							os)) {
 						LanguageCheckerCrawlerRunner languageCheckerCrawlerRunner = new LanguageCheckerCrawlerRunner(
 								taskProperties, crawlerProperties,
 								crawlerOutputStream);
 						languageCheckerCrawlerRunner.run();
 					}
 				}
 
 			} catch (Exception e) {
 				_log.error(e, e);
 			}
 		} catch (Exception e1) {
 			_log.error(e1, e1);
 		}
 	}
 }
