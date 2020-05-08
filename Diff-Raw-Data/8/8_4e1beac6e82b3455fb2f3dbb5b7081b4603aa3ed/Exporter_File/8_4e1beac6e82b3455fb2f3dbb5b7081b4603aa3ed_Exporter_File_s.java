 package org.ocha.hdx.exporter;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.ocha.hdx.service.ExporterService;
 
 /**
  * Abstract class for File report generation. This class should be implemented for every report needing a File export (CSV, PDF, TXT, etc.).
  * 
  * Exporters follow the Decorator pattern (see constructors).
  * 
  * @author bmichiels
  * 
  * @param <QD>
  *            The information needed to query and gather the report data.
  */
 public abstract class Exporter_File<QD extends QueryData> extends AbstractExporter<File, QD> {
 
 	/**
 	 * This constructor is for the final exporter of a chain of exporters. It propagates the exporter service to its predecessors for proper generation.
 	 * 
 	 * @param exporterService
 	 */
 	public Exporter_File(final ExporterService exporterService) {
 		super(exporterService);
 	}
 
 	/**
 	 * This constructor takes the following exporter in the global export chain.
 	 * 
 	 * @param exporter
 	 */
 	public Exporter_File(final Exporter<File, QD> exporter) {
 		super(exporter);
 	}
 
 	/**
 	 * Write a CSV file.
 	 * 
 	 * @param content
 	 * @param separator
 	 * @param file
 	 * @throws IOException
 	 */
 	protected static void writeCSVFile(final List<String[]> content, final String separator, final File file) throws IOException {
 		final FileWriter writer = new FileWriter(file);
 		for (final String[] line : content) {
 			for (final Iterator<String> iterator = Arrays.asList(line).iterator(); iterator.hasNext();) {
 				final String cell = iterator.next();
				final String escaped = StringEscapeUtils.escapeCsv(cell);
				writer.write(escaped);
 				if (iterator.hasNext()) {
 					writer.write(separator);
 				}
 			}
 			writer.write(System.getProperty("line.separator"));
 		}
 		writer.close();
 	}
 
 	/**
 	 * Write a TXT file.
 	 * 
 	 * @param content
 	 * @param file
 	 * @throws IOException
 	 */
 	protected static void writeTXTFile(final List<String> content, final File file) throws IOException {
 		final FileWriter writer = new FileWriter(file);
 		for (final String line : content) {
 			writer.write(line);
 			writer.write(System.getProperty("line.separator"));
 		}
 		writer.close();
 	}
 }
