 package org.logparser.example;
 
 import static org.logparser.Constants.LINE_SEPARATOR;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.math.stat.descriptive.StatisticalSummary;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.logparser.Config;
 import org.logparser.Config.SamplerConfig;
 import org.logparser.ILogEntryFilter;
 import org.logparser.LogEntry;
 import org.logparser.LogEntryFilter;
 import org.logparser.LogSnapshot;
 import org.logparser.io.CommandLineArguments;
 import org.logparser.io.CsvView;
 import org.logparser.io.LineByLineLogFilter;
 import org.logparser.io.LogFiles;
 import org.logparser.sampling.SamplingByFrequency;
 import org.logparser.sampling.SamplingByTime;
 import org.logparser.stats.PercentagePredicate;
 import org.logparser.stats.StandardDeviationPredicate;
 import org.logparser.stats.TimeStats;
 
 import com.beust.jcommander.JCommander;
 import com.google.common.base.Predicates;
 
 /**
  * Responsible for running the log parser via the command line.
  * 
  * Example usage:
  * 
  * Run the maven assembly plugin to create a bundle with all the dependencies:
  * 
  * <pre>
  * 		mvn clean package assembly:single
  * </pre>
  * 
  * Execute the generated jar (running the default 'example' below):
  * 
  * <pre>
  * 		java -Xms256m -Xmx256m -jar target/log-parser-1.7.X-jar-with-dependencies.jar -configfile config.json -logname example
  * </pre>
  * 
  * @author jorge.decastro
  */
 public class CommandLineApplicationRunner {
 	private static final Logger LOGGER = Logger.getLogger(CommandLineApplicationRunner.class.getName());
 
 	public static void main(String[] args) {
 		CommandLineArguments cla = new CommandLineArguments();
 		JCommander jc = new JCommander(cla, args);
 		if (cla.help) {
 			jc.usage();
 			return;
 		}
 
 		ObjectMapper mapper = new ObjectMapper();
 		Config config = null;
 		try {
 			Map<String, Config> configs = mapper.readValue(new File(cla.configFile), new TypeReference<Map<String, Config>>() { });
 			config = configs.get(cla.logName);
 
 			config.validate();
 			LOGGER.info(String.format("Loaded '%s' configuration", config.getFriendlyName()));
 			// TODO fix exception handling
 		} catch (JsonParseException jpe) {
 			jpe.printStackTrace();
 		} catch (JsonMappingException jme) {
 			jme.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		if (config != null) {
 			LogFiles logfiles = config.getLogFiles();
 			File[] files = logfiles.list();
 
 			LogEntryFilter filter = new LogEntryFilter(config);
 			// for large log files sampling is preferred/required
 			ILogEntryFilter<LogEntry> sampler = null;
 			if (config.getSampler() != null) {
 				SamplerConfig samplerConfig = config.getSampler();
 				switch (samplerConfig.sampleBy) {
 				case TIME:
 					sampler = new SamplingByTime<LogEntry>(filter, samplerConfig.value);
 					break;
 				case FREQUENCY:
 					sampler = new SamplingByFrequency<LogEntry>(filter, samplerConfig.value);
 					break;
 				default:
 					sampler = null;
 				}
 			}
 
 			@SuppressWarnings("unchecked")
 			LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(config, sampler != null ? sampler : filter);
 			
 			DecimalFormat df = new DecimalFormat("####.##");
 
 			String filepath;
 			String path = null;
 			String filename = null;
 			int totalEntries = 0;
 			int previousTotal = 0;
 			int filteredEntries = 0;
 			int previousFiltered = 0;
 			LogSnapshot<LogEntry> logSnapshot = null;;
 			for (File f : files) {
 				filepath = f.getAbsolutePath();
 				filename = f.getName();
 				path = f.getParent();
 
 				long start = System.nanoTime();
 				logSnapshot = lineByLineParser.filter(filepath);
 				long end = (System.nanoTime() - start) / 1000000;
 				totalEntries = logSnapshot.getTotalEntries() - previousTotal;
 				filteredEntries = logSnapshot.getFilteredEntries().size() - previousFiltered;
				LOGGER.info(String.format("LINE_SEPARATOR%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %sLINE_SEPARATOR", filename, end, df.format(totalEntries / (double) end), totalEntries, filteredEntries));
 				previousTotal = totalEntries;
 				previousFiltered = filteredEntries;
 			}
 			
 			if (StringUtils.isNotBlank(path) && StringUtils.isNotBlank(filename)) {
 				CsvView csvView = new CsvView();
 				csvView.write(path, filename, logSnapshot);				
 				// ChartView<LogEntry> chartView;
 				// chartView = new ChartView<LogEntry>(logSnapshot);
 				// chartView.write(path, filename);
 			}
 
 			LOGGER.info(LINE_SEPARATOR + logSnapshot.getDayStats().toString() + LINE_SEPARATOR);
 			LOGGER.info("Filtering by 1xStandard Deviation" + LINE_SEPARATOR);
 			StandardDeviationPredicate variancePredicate = new StandardDeviationPredicate();
 			Map<String, TimeStats<LogEntry>> filtered = logSnapshot.getDayStats().filter(variancePredicate);
 			LOGGER.info(toString(filtered));
 			
 			LOGGER.info("Filtering by 30%" + LINE_SEPARATOR);
 			PercentagePredicate percentagePredicate = new PercentagePredicate(30);
 			filtered = logSnapshot.getDayStats().filter(percentagePredicate);
 			LOGGER.info(toString(filtered));
 			
 			LOGGER.info("Filtering by disjunction of 30% and 1xStandard Deviation" + LINE_SEPARATOR);
			filtered = logSnapshot.getDayStats().filter(Predicates.or(percentagePredicate, variancePredicate));
 			LOGGER.info(toString(filtered));
 		}
 	}
 	
 	public static String toString(final Map<String, TimeStats<LogEntry>> input) {
 		StringBuilder sb = new StringBuilder(LINE_SEPARATOR);
 		sb.append(LINE_SEPARATOR);
 		for (Entry<String, TimeStats<LogEntry>> entries : input.entrySet()) {
 			sb.append(entries.getKey());
 			sb.append(LINE_SEPARATOR);
 			sb.append("\tDay, \t#, \tMean, \tStandard Deviation, \tMax, \tMin");
 			for (Entry<Integer, StatisticalSummary> timeStats : entries.getValue().getTimeStats().entrySet()) {
 				sb.append(LINE_SEPARATOR);
 				StatisticalSummary summary = timeStats.getValue();
 				sb.append(String.format("\t%s, \t%s, \t%s, \t%s, \t%s, \t%s",
 						timeStats.getKey(), 
 						summary.getN(), 
 						summary.getMean(),
 						summary.getStandardDeviation(), 
 						summary.getMax(),
 						summary.getMin()));
 			}
 			sb.append(LINE_SEPARATOR);
 		}
 		return sb.toString();
 	}
 }
