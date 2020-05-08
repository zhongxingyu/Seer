 package dna.plot;
 
 import java.io.File;
 import java.io.IOException;
 
 import dna.io.filesystem.Dir;
 import dna.io.filesystem.PlotFilenames;
 import dna.plot.Gnuplot.PlotStyle;
 import dna.plot.data.PlotData;
 import dna.plot.data.PlotData.DistributionPlotType;
 import dna.plot.data.PlotData.NodeValueListOrder;
 import dna.plot.data.PlotData.NodeValueListOrderBy;
 import dna.plot.data.PlotData.PlotType;
 import dna.series.SeriesStats;
 import dna.series.aggdata.AggregatedBatch;
 import dna.series.aggdata.AggregatedDistribution;
 import dna.series.aggdata.AggregatedMetric;
 import dna.series.aggdata.AggregatedNodeValueList;
 import dna.series.aggdata.AggregatedSeries;
 import dna.series.aggdata.AggregatedValue;
 import dna.series.data.SeriesData;
 import dna.util.Config;
 import dna.util.Log;
 
 public class Plotting {
 
 	/*
 	 * Different plot calls
 	 */
 	public static void plot(SeriesData seriesData, String dstDir)
 			throws IOException, InterruptedException {
 		Plotting.plot(new SeriesData[] { seriesData }, dstDir);
 	}
 
 	public static void plot(SeriesData seriesData, String dstDir,
 			PlotType type, PlotStyle style) throws IOException,
 			InterruptedException {
 		Plotting.plot(
 				seriesData,
 				dstDir,
 				type,
 				style,
 				Config.getDistributionPlotType("GNUPLOT_DEFAULT_DIST_PLOTTYPE"),
 				Config.getNodeValueListOrderBy("GNUPLOT_DEFAULT_NVL_ORDERBY"),
 				Config.getNodeValueListOrder("GNUPLOT_DEFAULT_NVL_ORDER"));
 	}
 
 	public static void plot(SeriesData seriesData, String dstDir,
 			PlotType type, PlotStyle style, DistributionPlotType distPlotType,
 			NodeValueListOrderBy sortBy, NodeValueListOrder sortOrder)
 			throws IOException, InterruptedException {
 		Plotting.plot(new SeriesData[] { seriesData }, dstDir, type, style,
 				distPlotType, sortBy, sortOrder);
 	}
 
 	public static void plot(SeriesData[] seriesData, String dstDir)
 			throws IOException, InterruptedException {
 		Plotting.plot(
 				seriesData,
 				dstDir,
 				Config.getPlotType("GNUPLOT_DEFAULT_PLOTTYPE"),
 				Config.getPlotStyle("GNUPLOT_DEFAULT_PLOTSTYLE"),
 				Config.getDistributionPlotType("GNUPLOT_DEFAULT_DIST_PLOTTYPE"),
 				Config.getNodeValueListOrderBy("GNUPLOT_DEFAULT_NVL_ORDERBY"),
 				Config.getNodeValueListOrder("GNUPLOT_DEFAULT_NVL_ORDER"));
 	}
 
 	/**
 	 * Main plotting method that handles the whole plotting process.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory where plots and scripts will be written
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @param sortBy
 	 *            Argument the NodeValueList will be sorted by
 	 * @param sortOrder
 	 *            Sorting order
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plot(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style, DistributionPlotType distPlotType,
 			NodeValueListOrderBy sortBy, NodeValueListOrder sortOrder)
 			throws IOException, InterruptedException {
 		Log.infoSep();
 		Log.info("plotting all data for " + seriesData.length + " series ("
 				+ type + "/" + style + ")");
 		(new File(dstDir)).mkdirs();
 
 		// read aggregation data
 		for (int i = 0; i < seriesData.length; i++) {
 			seriesData[i].setAggregation(AggregatedSeries.read(
 					seriesData[i].getDir(), seriesData[i].getName() + i + "_"
 							+ Config.get("RUN_AGGREGATION"), true));
 		}
 		// plot different data from the aggregation data
 		Plotting.plotDistributions(seriesData, dstDir, type, style,
 				distPlotType);
 		Plotting.plotValues(seriesData, dstDir, type, style);
 		Plotting.plotStatistics(seriesData, dstDir, type, style);
 		Plotting.plotRuntimes(seriesData, dstDir, type, style);
 		Plotting.plotNodeValueLists(seriesData, dstDir, type, style, sortBy,
 				sortOrder);
 	}
 
 	/**
 	 * Plots Distributions by calling Plotting.plotDistribution for each
 	 * distribution.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plotDistributions(SeriesData[] seriesData,
 			String dstDir, PlotType type, PlotStyle style,
 			DistributionPlotType distPlotType) throws IOException,
 			InterruptedException {
 		Log.info("plotting distributions with " + distPlotType.toString());
 		for (AggregatedMetric m : seriesData[0].getAggregation().getBatches()[0]
 				.getMetrics().getList()) {
 			for (AggregatedDistribution d : m.getDistributions().getList()) {
 				Plotting.plotDistributon(seriesData, dstDir, type, style,
 						distPlotType, m.getName(), d.getName());
 			}
 		}
 	}
 
 	/**
 	 * Plots NodeValueLists by calling Plotting.plotNodeValueList for each list.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @param sortBy
 	 *            Argument the NodeValueList will be sorted by
 	 * @param sortOrder
 	 *            Sorting order
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plotNodeValueLists(SeriesData[] seriesData,
 			String dstDir, PlotType type, PlotStyle style,
 			NodeValueListOrderBy sortBy, NodeValueListOrder sortOrder)
 			throws IOException, InterruptedException {
 		Log.info("plotting nodevaluelists sorted by " + sortBy.toString()
 				+ " in " + sortOrder.toString() + " order");
 		for (AggregatedMetric m : seriesData[0].getAggregation().getBatches()[0]
 				.getMetrics().getList()) {
 			for (AggregatedNodeValueList n : m.getNodeValues().getList()) {
 				Plotting.plotNodeValueList(seriesData, dstDir, type, style,
 						m.getName(), n.getName(), sortBy, sortOrder);
 			}
 		}
 	}
 
 	/**
 	 * Plots Values by calling Plotting.plotValue for each value.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plotValues(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style) throws IOException,
 			InterruptedException {
 		Log.info("plotting values");
 		for (AggregatedMetric metric : seriesData[0].getAggregation()
 				.getBatches()[0].getMetrics().getList()) {
 			for (AggregatedValue value : metric.getValues().getList()) {
 				Plotting.plotValue(seriesData, dstDir, type, style,
 						metric.getName(), value.getName());
 			}
 		}
 	}
 
 	/**
 	 * Plots statistics by calling Plotting.plotValue for each statistical
 	 * value.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plotStatistics(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style) throws IOException,
 			InterruptedException {
 		Log.info("plotting statistics");
 		for (String value : SeriesStats.statisticsToPlot) {
 			Plotting.plotValue(seriesData, dstDir, type, style, null, value);
 		}
 	}
 
 	/**
 	 * Plots runtimes for each batch of each series.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	public static void plotRuntimes(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style) throws IOException,
 			InterruptedException {
 		Log.info("plotting runtimes");
 		for (SeriesData s : seriesData) {
 			if (s.getAggregation().getBatches().length < 2) {
 				return;
 			}
 		}
 
 		int gr = SeriesStats.generalRuntimesOfCombinedPlot.length
 				* seriesData.length;
 		int mr = 0;
 		for (SeriesData s : seriesData) {
 			mr += s.getAggregation().getBatches()[0].getMetricRuntimes().size();
 		}
 
 		for (String runtime : SeriesStats.generalRuntimesToPlot) {
 			AggregatedValue[][] runtimes = new AggregatedValue[seriesData.length][];
 			String[] names = new String[seriesData.length];
 			double[][] x = new double[seriesData.length][];
 			int index = 0;
 
 			for (SeriesData s : seriesData) {
 				runtimes[index] = getGeneralRuntimes(s.getAggregation(),
 						runtime);
 				x[index] = getX(s.getAggregation());
 				names[index] = s.getName();
 				index++;
 			}
 			Plotting.plot(runtimes, names, dstDir, PlotFilenames
 					.getRuntimesStatisticPlot(runtime), PlotFilenames
 					.getRuntimesGnuplotScript(PlotFilenames
 							.getRuntimesStatisticPlot(runtime)), runtime + " ("
 					+ type + ")", type, style);
 		}
 
 		for (String metric : seriesData[0].getAggregation().getBatches()[0]
 				.getMetricRuntimes().getNames()) {
 			AggregatedValue[][] runtimes = new AggregatedValue[seriesData.length][];
 			String[] names = new String[seriesData.length];
 			double[][] x = new double[seriesData.length][];
 			int index = 0;
 			for (SeriesData s : seriesData) {
 				runtimes[index] = getMetricRuntimes(s.getAggregation(), metric);
 				x[index] = getX(s.getAggregation());
 				names[index] = s.getName();
 				index++;
 			}
 			Plotting.plot(runtimes, names, dstDir, PlotFilenames
 					.getRuntimesMetricPlot(metric), PlotFilenames
 					.getRuntimesGnuplotScript(PlotFilenames
 							.getRuntimesMetricPlot(metric)), metric + " ("
 					+ type + ")", type, style);
 		}
 
 		// gather runtime data..
 		AggregatedValue[][] general = new AggregatedValue[gr][];
 		AggregatedValue[][] metrics = new AggregatedValue[mr][];
 		String[] generalNames = new String[gr];
 		String[] metricsNames = new String[mr];
 		double[][] generalX = new double[gr][];
 		double[][] metricsX = new double[mr][];
 
 		int index1 = 0;
 		int index2 = 0;
 		// for each series
 		for (SeriesData s : seriesData) {
 			// for each statistic
 			for (String runtime : SeriesStats.generalRuntimesOfCombinedPlot) {
 				general[index1] = getGeneralRuntimes(s.getAggregation(),
 						runtime);
 				generalX[index1] = getX(s.getAggregation());
 				generalNames[index1] = runtime + "-" + s.getName();
 				index1++;
 			}
 			// for each metric
 			for (String metric : s.getAggregation().getBatches()[0]
 					.getMetricRuntimes().getNames()) {
 				metrics[index2] = getMetricRuntimes(s.getAggregation(), metric);
 				metricsX[index2] = getX(s.getAggregation());
 				metricsNames[index2] = metric + "-" + s.getName();
 				index2++;
 			}
 		}
 
 		// TODO re-add fraction of runtimes....
 
 		// int index = 0;
 		// for (SeriesData s : seriesData) {
 		// double[] sum = new double[s.getAggregation().getBatches().length -
 		// 1];
 		// int metricCount = s.getAggregation().getBatches()[0]
 		// .getMetricRuntimes().getNames().size();
 		// for (int i = 0; i < metricCount; i++) {
 		// for (int j = 0; j < sum.length; j++) {
 		// sum[j] += metrics[index + i].getValues()[j][1];
 		// }
 		// }
 		// for (int i = 0; i < metricCount; i++) {
 		// for (int j = 0; j < sum.length; j++) {
 		// metricsFraction[index + i].getValues()[j][1] /= sum[j];
 		// }
 		// }
 		//
 		// index += metricCount;
 		// }
 
 		// generate plot script for runtime statistics and execute it
 		Plotting.plot(general, generalNames, dstDir, PlotFilenames
 				.getRuntimesStatisticPlot(Config.get("PLOT_GENERAL_RUNTIMES")),
 				PlotFilenames.getRuntimesGnuplotScript(PlotFilenames
 						.getRuntimesStatisticPlot(Config
 								.get("PLOT_GENERAL_RUNTIMES"))),
 				"general runtimes (" + type + ")", type, style);
 
 		// generate plot script for metric runtimes and execute it
 		Plotting.plot(metrics, metricsNames, dstDir, PlotFilenames
 				.getRuntimesMetricPlot(Config.get("PLOT_METRIC_RUNTIMES")),
 				PlotFilenames.getRuntimesGnuplotScript(PlotFilenames
 						.getRuntimesMetricPlot(Config
 								.get("PLOT_METRIC_RUNTIMES"))),
 				"metric runtimes (" + type + ")", type, style);
 	}
 
 	/**
 	 * Plots one Distribution of the input seriesData.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @param metric
 	 *            the metric which is being plotted
 	 * @param distribution
 	 *            the distribution which is being plotted
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown in Execute.exec
 	 */
 	private static void plotDistributon(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style, DistributionPlotType distPlotType,
 			String metric, String distribution) throws IOException,
 			InterruptedException {
 		Log.info("distribution " + distribution + " of " + metric + " with "
 				+ distPlotType.toString());
 		int batches = 0;
 		for (SeriesData s : seriesData) {
 			batches += s.getAggregation().getBatches().length;
 		}
 
 		PlotData[] data = new PlotData[batches];
 		int index = 0;
 
 		// gather data..
 		AggregatedDistribution[] DistributionTemp = new AggregatedDistribution[batches];
 		// for each series
 		for (SeriesData s : seriesData) {
 			// for each batch
 			for (AggregatedBatch b : s.getAggregation().getBatches()) {
 				DistributionTemp[index] = b.getMetrics().get(metric)
 						.getDistributions().get(distribution);
 				String path = Dir.getAggregatedMetricDataDir(s.getDir(),
 						b.getTimestamp(), metric)
 						+ distribution + Config.get("SUFFIX_DIST");
 				data[index++] = PlotData.get(path, style, s.getName() + " @ "
 						+ b.getTimestamp(), type);
 			}
 		}
 		// generate plot script and execute it
 		switch (distPlotType) {
 		case distOnly:
 			Plot plotDistOnly = new Plot(data, dstDir,
 					PlotFilenames.getDistributionPlot(metric, distribution),
 					PlotFilenames.getDistributionGnuplotScript(metric,
 							distribution), distPlotType);
 			plotDistOnly.setTitle(distribution + " (" + type + ")");
 			plotDistOnly.generate(DistributionTemp);
 			break;
 		case cdfOnly:
 			Plot plotCdfOnly = new Plot(data, dstDir,
 					PlotFilenames.getDistributionCdfPlot(metric, distribution),
 					PlotFilenames.getDistributionCdfGnuplotScript(metric,
 							distribution), distPlotType);
 			plotCdfOnly.setTitle("CDF of " + distribution + " (" + type + ")");
 			plotCdfOnly.generate(DistributionTemp);
 			break;
 		case distANDcdf:
 			Plot plotDist = new Plot(data, dstDir,
 					PlotFilenames.getDistributionPlot(metric, distribution),
 					PlotFilenames.getDistributionGnuplotScript(metric,
 							distribution), DistributionPlotType.distOnly);
 			plotDist.setTitle(distribution + " (" + type + ")");
 			plotDist.generate(DistributionTemp);
 			Plot plotCdf = new Plot(data, dstDir,
 					PlotFilenames.getDistributionCdfPlot(metric, distribution),
 					PlotFilenames.getDistributionCdfGnuplotScript(metric,
 							distribution), DistributionPlotType.cdfOnly);
 			plotCdf.setTitle("CDF of " + distribution + " (" + type + ")");
 			plotCdf.generate(DistributionTemp);
 			break;
 		}
 
 	}
 
 	/**
 	 * Plots one NodeValueList of the input seriesData.
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @param metric
 	 *            the metric which is being plotted
 	 * @param nodevaluelist
 	 *            the nodevaluelist which is being plotted
 	 * @param sortBy
 	 *            Argument the NodeValueList will be sorted by
 	 * @param sortOrder
 	 *            Sorting order
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown when metric is null or in Execute.exec
 	 */
 	private static void plotNodeValueList(SeriesData[] seriesData,
 			String dstDir, PlotType type, PlotStyle style, String metric,
 			String nodevaluelist, NodeValueListOrderBy sortBy,
 			NodeValueListOrder sortOrder) throws IOException,
 			InterruptedException {
 		Log.info("nodevaluelist " + nodevaluelist + " of " + metric);
 		if (metric == null)
 			throw new InterruptedException("null pointer on metric");
 		int batches = 0;
 		for (SeriesData s : seriesData) {
 			batches += s.getAggregation().getBatches().length;
 		}
 
 		PlotData[] data = new PlotData[batches];
 		int index = 0;
 
 		// gather data..
 		AggregatedNodeValueList[] NodeValueListsTemp = new AggregatedNodeValueList[batches];
 		// for each series
 		for (SeriesData s : seriesData) {
 			// for each batch
 			for (AggregatedBatch b : s.getAggregation().getBatches()) {
 				NodeValueListsTemp[index] = b.getMetrics().get(metric)
 						.getNodeValues().get(nodevaluelist);
 				NodeValueListsTemp[index].setsortIndex(sortBy, sortOrder);
 				String path = Dir.getAggregatedMetricDataDir(s.getDir(),
 						b.getTimestamp(), metric)
 						+ nodevaluelist + Config.get("SUFFIX_NVL");
 				path = dstDir
 						+ PlotFilenames.getNodeValueListDataFile(metric,
 								nodevaluelist, (int) b.getTimestamp());
 				data[index++] = PlotData.get(path, style, s.getName() + " @ "
 						+ b.getTimestamp(), type);
 			}
 		}
 		// generate plot script and execute it
 		Plot plot = new Plot(data, dstDir, PlotFilenames.getNodeValueListPlot(
 				metric, nodevaluelist),
 				PlotFilenames.getNodeValueListGnuplotScript(metric,
 						nodevaluelist));
 		plot.setTitle(nodevaluelist + " (" + type + ")");
 		plot.generate(NodeValueListsTemp);
 	}
 
 	/**
 	 * Plots a value for each series
 	 * 
 	 * @param seriesData
 	 *            SeriesData which will be plotted
 	 * @param dstDir
 	 *            Destination directory
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @param metric
 	 *            the metric which is being plotted
 	 * @param value
 	 *            the value which is being plotted
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown when metric is null or in Execute.exec
 	 */
 	private static void plotValue(SeriesData[] seriesData, String dstDir,
 			PlotType type, PlotStyle style, String metric, String value)
 			throws IOException, InterruptedException {

 		int index1 = 0;
 		String m = metric == null ? Config.get("PREFIX_STATS_PLOT") : metric;
 
 		// gather data..
 		// for each series
 		for (SeriesData s : seriesData) {
 			AggregatedValue[] values = new AggregatedValue[s.getAggregation()
 					.getBatches().length];
 			int index2 = 0;
 			// for each batch
 			for (AggregatedBatch b : s.getAggregation().getBatches()) {
 				if (metric == null) {
 					values[index2] = b.getValues().get(value);
 				} else {
 					values[index2] = b.getMetrics().get(metric).getValues()
 							.get(value);
 				}
 				index2++;
 			}
 
 			String filename = PlotFilenames.getValuesDataFile(m, value, index1);
 			String path = dstDir + "." + index1 + "." + filename;
 
			PlotData[] data = { PlotData.get(path, style, s.getName(), type) };
 
 			// generate plot script and execute it
 			Plot plot = new Plot(data, dstDir, PlotFilenames.getValuesPlot(m,
 					value + "." + index1),
 					PlotFilenames.getValuesGnuplotScript(m, value + "."
 							+ index1));
 			plot.setTitle(value + " (" + type + ")");
 			plot.generate(values);
 
 			index1++;
 		}
 	}
 
 	/**
 	 * This method is called in Plotting.plotRuntimes to plot the runtime
 	 * values.
 	 * 
 	 * @param values
 	 *            2-dimensional AggregatedValue-array containing the values to
 	 *            be plotted.
 	 * @param names
 	 *            names of the plotted values
 	 * @param dstDir
 	 *            destination directory
 	 * @param filename
 	 *            destination filename
 	 * @param script
 	 *            script filename
 	 * @param title
 	 *            plot title
 	 * @param type
 	 *            PlotType
 	 * @param style
 	 *            PlotStyle
 	 * @throws IOException
 	 *             thrown by the writer in Plot.writeScript or in Execute.exec
 	 * @throws InterruptedException
 	 *             thrown when metric is null or in Execute.exec
 	 */
 	public static void plot(AggregatedValue[][] values, String[] names,
 			String dstDir, String filename, String script, String title,
 			PlotType type, PlotStyle style) throws IOException,
 			InterruptedException {
 		PlotData[] data = new PlotData[values.length];
 		// gather data
 		for (int i = 0; i < values.length; i++) {
 			data[i] = PlotData.get(
 					dstDir + PlotFilenames.getRuntimesDataFile(names[i]),
 					style, names[i], type);
 		}
 		// generate plot script and execute it
 		Plot plot = new Plot(data, dstDir, filename, script);
 		plot.setTitle(title);
 		plot.generate(values);
 	}
 
 	/**
 	 * This method returns the general runtimes of all aggregated batches. Note:
 	 * The initialization batch will not be included. It's assumed that batch
 	 * with lowest timestamp is initiliazation batch.
 	 * 
 	 * @param aggregation
 	 * @param runtime
 	 * @return
 	 */
 	private static AggregatedValue[] getGeneralRuntimes(
 			AggregatedSeries aggregation, String runtime) {
 		AggregatedValue[] values = new AggregatedValue[aggregation.getBatches().length - 1];
 
 		// min is used to figure which batch is the initialization batch
 		long min = 0;
 		boolean init = false;
 		for (AggregatedBatch aggBatch : aggregation.getBatches()) {
 			if (!init) {
 				min = aggBatch.getTimestamp();
 				init = true;
 			}
 			if (aggBatch.getTimestamp() < min) {
 				min = aggBatch.getTimestamp();
 			}
 		}
 
 		// gather aggregated values
 		int offset = 0;
 		for (int i = 0; i < aggregation.getBatches().length; i++) {
 			if (aggregation.getBatches()[i].getTimestamp() != min) {
 				values[i - offset] = aggregation.getBatches()[i]
 						.getGeneralRuntimes().get(runtime)
 						.clone(1.0 / 1000.0 / 1000.0 / 1000.0);
 				if (values[i - offset] == null) {
 					values[i - offset] = AggregatedValue.getNaN();
 				}
 			} else {
 				offset++;
 			}
 		}
 		return values;
 	}
 
 	private static AggregatedValue[] getMetricRuntimes(
 			AggregatedSeries aggregation, String metric) {
 		AggregatedValue[] values = new AggregatedValue[aggregation.getBatches().length - 1];
 		for (int i = 1; i < aggregation.getBatches().length; i++) {
 			values[i - 1] = aggregation.getBatches()[i].getMetricRuntimes()
 					.get(metric).clone(1.0 / 1000.0 / 1000.0 / 1000.0);
 			if (values[i - 1] == null) {
 				values[i - 1] = AggregatedValue.getNaN();
 			}
 		}
 		return values;
 	}
 
 	private static double[] getX(AggregatedSeries aggregation) {
 		double[] x = new double[aggregation.getBatches().length - 1];
 		for (int i = 1; i < aggregation.getBatches().length; i++) {
 			x[i - 1] = aggregation.getBatches()[i].getTimestamp();
 		}
 		return x;
 	}
 
 }
