 package org.logparser.example;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.logparser.AnalyzeArguments;
 import org.logparser.FilterConfig;
 import org.logparser.IMessageFilter;
 import org.logparser.IStatsView;
 import org.logparser.LogEntry;
 import org.logparser.LogEntryFilter;
 import org.logparser.LogOrganiser;
 import org.logparser.LogSnapshot;
 import org.logparser.SamplingByFrequency;
 import org.logparser.SamplingByTime;
 import org.logparser.FilterConfig.Sampler;
 import org.logparser.io.ChartView;
 import org.logparser.io.CsvView;
 import org.logparser.io.LineByLineLogFilter;
 
 /**
  * Responsible for running the log parser via the command line.
  * 
  * <code>
  *  java -Xmx128m -jar log-parser-1.0.jar config.json
  * </code>
  * 
  * 24hrs worth of log file can take ~5mins to process.
  * 
  * @author jorge.decastro
  */
 public class CommandLineApplicationRunner {
 	/**
 	 * Run with no args to see help information.
 	 * 
 	 * @param args Run with no args to see help information.
 	 */
 	@SuppressWarnings("unchecked")
 	public static void main(String[] args) {
 		AnalyzeArguments aa = new AnalyzeArguments(args);
 
 		FilterConfig filterConfig = getFilterConfig(aa);
 		if (filterConfig != null) {
 			File[] files = getListOfLogFiles(filterConfig);
 
 			LogEntryFilter filter = new LogEntryFilter(filterConfig);
 			// for large log files sampling is preferred/required
 			IMessageFilter<LogEntry> sampler = null;
 			if (filterConfig.getSampler() != null) {
 				Sampler samplerConfig = filterConfig.getSampler();
 				switch (samplerConfig.sampleBy) {
 				case TIME:
 					sampler = new SamplingByTime<LogEntry>(filter, (Long) samplerConfig.getValue());
 					break;
 				case FREQUENCY:
 					sampler = new SamplingByFrequency<LogEntry>(filter, (Integer) samplerConfig.getValue());
 					break;
 				default:
 					sampler = null;
 				}
 			}
 			
 			LineByLineLogFilter<LogEntry> rlp = new LineByLineLogFilter<LogEntry>(filterConfig, sampler != null? sampler : filter);
 			LogOrganiser<LogEntry> logOrganiser;
 			Map<String, IStatsView<LogEntry>> organisedEntries;
 			ChartView<LogEntry> chartView;
 			CsvView<LogEntry> csvView;
 			String filepath;
 			String path;
 			String filename;
 			for (File f : files) {
 				filepath = f.getAbsolutePath();
 				filename = f.getName();
 				path = f.getParent();
 
 				long start = System.nanoTime();
 				LogSnapshot<LogEntry> ls = rlp.filter(filepath);
 				long end = (System.nanoTime() - start) / 1000000;
 				DecimalFormat df = new DecimalFormat("####.##");
 				System.out.println(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n",
 										filename, end, df.format(ls.getTotalEntries() / (double) end), ls.getTotalEntries(), ls.getFilteredEntries().size()));
 				// inject the parser onto the 'organiser'
 				logOrganiser = new LogOrganiser<LogEntry>();
 				// pass the class field used to group by
 				organisedEntries = logOrganiser.groupBy(ls);
 				chartView = new ChartView(ls);
 				chartView.write(path, filename);
 				csvView = new CsvView<LogEntry>(ls, organisedEntries);
 				csvView.write(path, filename);
 				df = new DecimalFormat("####.##%");
 				double percentOfFiltered = 0.0;
 				double percentOfTotal = 0.0;
 				int value = 0;
 				System.out.println("URL,\t# Count,\t% of Filtered,\t% of Total");
 				for (Entry<String, Integer> entries : ls.getSummary().entrySet()) {
 					value = entries.getValue() > 0 ? entries.getValue() : 0;
 					percentOfFiltered = value > 0 ? value / (double) ls.getFilteredEntries().size() : 0D;
 					percentOfTotal = value > 0 ? value / (double) ls.getTotalEntries() : 0D;
 					System.out.println(String.format("%s,\t %s,\t %s,\t %s",
 							entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
 				}
 
 				System.out.println("\n" + filterConfig.getGroupBy() + ",\t# Count,\t% of Filtered,\t% of Total\n");
 				for (Entry<String, Integer> entries : ls.getTimeBreakdown().entrySet()) {
 					value = entries.getValue() > 0 ? entries.getValue() : 0;
 					percentOfFiltered = value > 0 ? value / (double) ls.getFilteredEntries().size() : 0D;
 					percentOfTotal = value > 0 ? value / (double) ls.getTotalEntries() : 0D;
 					System.out.println(String.format("%s,\t %s,\t %s,\t %s", entries.getKey(), entries.getValue(), df.format(percentOfFiltered), df.format(percentOfTotal)));
 				}
 				rlp.cleanup();
 			}
 		}
 	}
 
 	private static FilterConfig getFilterConfig(final AnalyzeArguments aa) {
 		ObjectMapper mapper = new ObjectMapper();
 		try {
 			FilterConfig filterConfig = mapper.readValue(new File(aa.getPathToConfig()), FilterConfig.class);
 			filterConfig.validate();
 			System.out.print(filterConfig.toString() + "\n");
			if (filterConfig.getSampler() != null) {
				System.out.print(filterConfig.getSampler().toString());
			}
 			return filterConfig;
 		} catch (JsonParseException jpe) {
 			jpe.printStackTrace();
 		} catch (JsonMappingException jme) {
 			jme.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		return null;
 	}
 
 	private static File[] getListOfLogFiles(final FilterConfig filterConfig) {
 		String[] dirs = filterConfig.getBaseDirs();
 		List<File> listOfFiles = new ArrayList<File>();
 		Pattern filenamePattern = Pattern.compile(filterConfig.getFilenamePattern());
 		for (String path : dirs) {
 			File f = new File(path.trim());
 			if (!f.exists()) {
 				throw new IllegalArgumentException(String.format("Unable to find given path %s", path));
 			}
 			File[] contents = f.listFiles();
 			for (File file : contents) {
 				if (filenamePattern.matcher(file.getName()).matches()) {
 					listOfFiles.add(file);
 				}
 			}
 		}
 		return listOfFiles.toArray(new File[0]);
 	}
 }
