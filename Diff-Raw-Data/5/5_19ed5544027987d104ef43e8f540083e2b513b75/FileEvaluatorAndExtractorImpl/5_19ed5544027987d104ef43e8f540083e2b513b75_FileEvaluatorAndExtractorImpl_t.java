 package org.ocha.dap.service;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.ocha.dap.model.ValidationReport;
 import org.ocha.dap.model.ValidationStatus;
 import org.ocha.dap.persistence.entity.ckan.CKANDataset;
 import org.ocha.dap.persistence.entity.ckan.CKANDataset.Type;
 import org.ocha.dap.tools.IOTools;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FileEvaluatorAndExtractorImpl implements FileEvaluatorAndExtractor {
 	
 	private static final Logger log = LoggerFactory.getLogger(FileEvaluatorAndExtractorImpl.class);
 
 	/**
 	 * performs a dummy evaluation of a CSV file
 	 * 
 	 * for this example, we assume we got some percentage for some categories,
 	 * per country all countries sum should be 100.
 	 * 
 	 * @return true if all countries have a sum of 100, false otherwise
 	 * 
 	 */
 	ValidationReport evaluateDummyCSVFile(final File file) {
 		final ValidationReport report = new ValidationReport(CKANDataset.Type.DUMMY);
 		try (final BufferedReader br = new BufferedReader(new FileReader(file))) {
 			final Map<String, Integer> totalForCountries = new HashMap<>();
 			String line;
 			while ((line = br.readLine()) != null) {
 
 				// use comma as separator
 				final String[] values = line.split(",");
 
 				if (values.length != 4) {
 					report.addEntry(ValidationStatus.ERROR,
 							String.format("A ligne contains an incorrect number of values, expected : 4, actual : %d", values.length));
 					// In this case, the next test cannot even be performed, so
 					// we return the root error
 					return report;
 				} else {
 					report.addEntry(ValidationStatus.SUCCESS, String.format("A ligne contains the correct number of values ", values.length));
 				}
 
 				final String country = values[0];
 				final Integer value = Integer.parseInt(values[2]);
 
 				final Integer total = totalForCountries.get(country);
 				if (total != null) {
 					totalForCountries.put(country, total + value);
 				} else {
 					totalForCountries.put(country, value);
 				}
 
 			}
 			for (final Entry<String, Integer> entry : totalForCountries.entrySet()) {
 				if (entry.getValue() != 100)
 					report.addEntry(ValidationStatus.ERROR, String.format("Total for region : %s is not 100", entry.getKey()));
 			}
 
 		} catch (final IOException e) {
 			report.addEntry(ValidationStatus.ERROR, "Error caused by an exception");
 		}
 		return report;
 	}
 
 	private ValidationReport evaluateScraper(final File file) {
 		final ValidationReport report = new ValidationReport(CKANDataset.Type.SCRAPER);
 		extractZipContent(file);
 		final File parent = file.getParentFile();
 		
 		final File datasetFile = new File(parent, "dataset.csv");
 		if(datasetFile.exists()){
 			report.addEntry(ValidationStatus.SUCCESS, "dataset.csv does exist");
 		}else {
 			report.addEntry(ValidationStatus.ERROR, "dataset.csv does not exist");
 		}
 		
 		final File indicatorFile = new File(parent, "indicator.csv");
 		if(indicatorFile.exists()){
 			report.addEntry(ValidationStatus.SUCCESS, "indicator.csv does exist");
 		}else {
 			report.addEntry(ValidationStatus.ERROR, "indicator.csv does not exist");
 		}
 		
 		final File valueFile = new File(parent, "value.csv");
 		if(valueFile.exists()){
			report.addEntry(ValidationStatus.SUCCESS, "value.csv does exist");
 		}else {
			report.addEntry(ValidationStatus.ERROR, "value.csv does not exist");
 		}
 		
 		return report;
 	}
 
 	private void extractZipContent(final File zipFile) {
 		final int BUFFER = 2048;
 
 		ZipFile zip = null;
 		try {
 			final File parent = zipFile.getParentFile();
 			zip = new ZipFile(zipFile);
 
 			final Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();
 
 			BufferedInputStream is = null;
 			FileOutputStream fos = null;
 			BufferedOutputStream dest = null;
 			try {
 				// Process each entry
 				while (zipFileEntries.hasMoreElements()) {
 					// grab a zip file entry
 					final ZipEntry entry = zipFileEntries.nextElement();
 					final String currentEntry = entry.getName();
 					final File destFile = new File(parent, currentEntry);
 					// destFile = new File(newPath, destFile.getName());
 					final File destinationParent = destFile.getParentFile();
 
 					// create the parent directory structure if needed
 					if (destinationParent.mkdirs()) {
 						log.debug(String.format("Failed to perform mkdirs for path : %s", destinationParent.getAbsolutePath()));
 					}
 
 					if (!entry.isDirectory()) {
 						is = new BufferedInputStream(zip.getInputStream(entry));
 						int currentByte;
 						// establish buffer for writing file
 						final byte data[] = new byte[BUFFER];
 
 						// write the current file to disk
 						fos = new FileOutputStream(destFile);
 						dest = new BufferedOutputStream(fos, BUFFER);
 
 						// read and write until last byte is encountered
 						while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
 							dest.write(data, 0, currentByte);
 						}
 						dest.flush();
 					}
 				}
 			} catch (final Exception e) {
 				log.debug(e.toString(), e);
 			} finally {
 				IOTools.closeResource(is);
 				IOTools.closeResource(fos);
 				IOTools.closeResource(dest);
 			}
 		} catch (final Exception e) {
 			log.debug(e.toString(), e);
 		} finally {
 			 IOTools.closeResource(zip);
 		}
 	}
 
 	private ValidationReport defaultFail(final File file) {
 		final ValidationReport report = new ValidationReport(CKANDataset.Type.SCRAPER);
 
 		report.addEntry(ValidationStatus.ERROR, "Mocked evaluator, always failing");
 		return report;
 	}
 
 	@Override
 	public ValidationReport evaluateResource(final File file, final Type type) {
 		switch (type) {
 		case DUMMY:
 			return evaluateDummyCSVFile(file);
 
 		case SCRAPER:
 			return evaluateScraper(file);
 
 		default:
 			return defaultFail(file);
 		}
 	}
 
 	@Override
 	public boolean transformAndImportDataFromResource(final File file, final Type type) {
 		switch (type) {
 		case DUMMY:
 			return false;
 
 		case SCRAPER:
 			return false;
 
 		default:
 			return false;
 		}
 	}
 
 }
