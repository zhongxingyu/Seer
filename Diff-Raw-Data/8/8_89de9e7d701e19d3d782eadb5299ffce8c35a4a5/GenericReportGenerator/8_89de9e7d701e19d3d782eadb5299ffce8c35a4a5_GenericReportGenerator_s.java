 package org.gwaspi.reports;
 
 import java.awt.Color;
 import java.awt.geom.Rectangle2D;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import org.gwaspi.constants.cNetCDF;
 import org.gwaspi.global.Config;
 import org.gwaspi.gui.reports.ManhattanPlotZoom;
 import org.gwaspi.gui.reports.SampleQAHetzygPlotZoom;
 import org.gwaspi.model.MarkerKey;
 import org.gwaspi.model.OperationMetadata;
 import org.gwaspi.model.OperationsList;
 import org.gwaspi.model.SampleKey;
 import org.gwaspi.netCDF.markers.MarkerSet_opt;
 import org.gwaspi.netCDF.operations.MarkerOperationSet;
 import org.gwaspi.netCDF.operations.SampleOperationSet;
 import org.gwaspi.statistics.Chisquare;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.LegendItemCollection;
 import org.jfree.chart.axis.LogAxis;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.axis.TickUnitSource;
 import org.jfree.chart.plot.CombinedRangeXYPlot;
 import org.jfree.chart.plot.Marker;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.ValueMarker;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.TextAnchor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import ucar.nc2.NetcdfFile;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class GenericReportGenerator {
 
 	private static final Logger log
 			= LoggerFactory.getLogger(GenericReportGenerator.class);
 
 	public static final String PLOT_MANHATTAN_THRESHOLD_CONFIG = "CHART_MANHATTAN_PLOT_THRESHOLD";
 	public static final double PLOT_MANHATTAN_THRESHOLD_DEFAULT = 5E-7;
 	public static final String PLOT_MANHATTAN_BACKGROUND_CONFIG = "CHART_MANHATTAN_PLOT_BACKGROUND";
 	public static final Color PLOT_MANHATTAN_BACKGROUND_DEFAULT = new Color(200, 200, 200);
 	public static final String PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG = "CHART_MANHATTAN_PLOT_BACKGROUND_ALTERNATIVE";
 	public static final Color PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_DEFAULT = new Color(230, 230, 230);
 	public static final String PLOT_MANHATTAN_MAIN_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
 	public static final Color PLOT_MANHATTAN_MAIN_DEFAULT = Color.BLUE;
 	public static final String PLOT_QQ_BACKGROUND_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
 	public static final Color PLOT_QQ_BACKGROUND_DEFAULT = new Color(230, 230, 230);
 	public static final String PLOT_QQ_ACTUAL_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
 	public static final Color PLOT_QQ_ACTUAL_DEFAULT = Color.BLUE;
 	public static final String PLOT_QQ_MU_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
 	public static final Color PLOT_QQ_MU_DEFAULT = Color.GRAY;
 	public static final String PLOT_QQ_SIGMA_CONFIG = "CHART_MANHATTAN_PLOT_MAIN";
 	public static final Color PLOT_QQ_SIGMA_DEFAULT = Color.LIGHT_GRAY;
 
 	private GenericReportGenerator() {
 	}
 
 	//<editor-fold defaultstate="expanded" desc="ASSOCIATION CHARTS">
 	public static CombinedRangeXYPlot buildManhattanPlot(int opId, String netCDFVar) throws IOException {
 
 		//<editor-fold defaultstate="expanded" desc="PLOT DEFAULTS">
 		double threshold = Double.parseDouble(Config.getConfigValue(
 				PLOT_MANHATTAN_THRESHOLD_CONFIG,
 				String.valueOf(PLOT_MANHATTAN_THRESHOLD_DEFAULT)));
 		Color background = Config.getConfigColor(
 				PLOT_MANHATTAN_BACKGROUND_CONFIG,
 				PLOT_MANHATTAN_BACKGROUND_DEFAULT);
 		Color backgroundAlternative = Config.getConfigColor(
 				PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_CONFIG,
 				PLOT_MANHATTAN_BACKGROUND_ALTERNATIVE_DEFAULT);
 		Color main = Config.getConfigColor(
 				PLOT_MANHATTAN_MAIN_CONFIG,
 				PLOT_MANHATTAN_MAIN_DEFAULT);
 		//</editor-fold>
 
 		Map<MarkerKey, Object> dataSetMap = new LinkedHashMap<MarkerKey, Object>();
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 		//<editor-fold defaultstate="expanded" desc="GET POSITION DATA">
 		MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
 		rdInfoMarkerSet.initFullMarkerIdSetMap();
 
 //		long snpNumber = rdInfoMarkerSet.getMarkerSetSize();
 //		if (snpNumber < 250000) {
 //			hetzyThreshold = 0.5 / snpNumber;  // (0.05 / 10^6 SNPs => 5*10^(-7))
 //		}
 
 		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 		for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 			MarkerKey key = entry.getKey();
 			String chr = entry.getValue().toString();
 			Object[] data = new Object[3]; //CHR, POS, PVAL
 			data[0] = chr;
 			dataSetMap.put(key, data);
 		}
 
 		rdInfoMarkerSet.fillWith(0);
 		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 		if (rdInfoMarkerSet.getMarkerIdSetMap() != null) {
 			for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] data = (Object[]) dataSetMap.get(key); //CHR, POS, PVAL
 				int pos = (Integer) entry.getValue();
 				data[1] = pos;
 				dataSetMap.put(key, data);
 			}
 
 			rdInfoMarkerSet.getMarkerIdSetMap().clear();
 		}
 		//</editor-fold>
 
 		//<editor-fold defaultstate="expanded" desc="GET Pval">
 		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 		MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
 		Map<MarkerKey, Object> rdAssocMarkerSetMap = rdAssocMarkerSet.getOpSetMap();
 		rdAssocMarkerSetMap = rdAssocMarkerSet.fillOpSetMapWithVariable(assocNcFile, netCDFVar);
 		assocNcFile.close();
 
 		if (rdAssocMarkerSetMap != null) {
 			for (Map.Entry<MarkerKey, Object> entry : rdAssocMarkerSetMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] data = (Object[]) dataSetMap.get(key); // CHR, POS, PVAL
 
 				double[] value = (double[]) entry.getValue();
 				Double pval = (Double) value[1];  // PVAL
 				if (!Double.isNaN(pval) && !Double.isInfinite(pval)) { // Ignore NaN Pvalues
 					data[2] = pval;
 					dataSetMap.put(key, data);
 				}
 			}
 
 			rdAssocMarkerSetMap.clear();
 		}
 		//</editor-fold>
 
 		XYSeriesCollection currChrSC = new XYSeriesCollection();
 
 		NumberAxis sharedAxis = new NumberAxis("-log₁₀(P)");
 
 		CombinedRangeXYPlot combinedPlot = new CombinedRangeXYPlot(sharedAxis);
 		combinedPlot.setGap(0);
 
 		XYSeries currChrS = null;
 
 		// Subdividing points into sub-XYSeries, per chromosome
 		String currChr = "";
 		Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>(); // FIXME This is unused, was a global static var before (also private though), was the data added here actually used somewhere? (i think not)
 		for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 			MarkerKey markerKey = entry.getKey();
 			Object[] data = (Object[]) entry.getValue(); //CHR, POS, PVAL
 
 			String chr = data[0].toString();
 			int position = (Integer) data[1];
 
 			if (data[2] != null) {
 				double pVal = (Double) data[2]; // Is allready free of NaN
 				if (pVal < 1 && pVal != Double.POSITIVE_INFINITY && pVal != Double.NEGATIVE_INFINITY) {
 					if (chr.equals(currChr)) {
 						currChrS.add(position, pVal);
 						labeler.put(currChr + "_" + position, markerKey);
 					} else {
 						if (!currChr.equals("")) { // SKIP FIRST TIME (NO DATA YET!)
 							currChrSC.addSeries(currChrS);
 							appendToCombinedRangeManhattanPlot(combinedPlot, currChr, currChrSC, false, threshold, background, backgroundAlternative, main);
 						}
 						currChr = data[0].toString();
 						currChrSC = new XYSeriesCollection();
 						currChrS = new XYSeries(currChr);
 						labeler.put(currChr + "_" + position, markerKey);
 
 						currChrS.add(position, pVal);
 					}
 				}
 			}
 		}
 		if (currChrS != null) {
 			currChrSC.addSeries(currChrS);
 			// ADD LAST CHR TO PLOT
 			appendToCombinedRangeManhattanPlot(combinedPlot, currChr, currChrSC, true, threshold, background, backgroundAlternative, main);
 		}
 
 		// Remove Legend from the bottom of the chart
 		combinedPlot.setFixedLegendItems(new LegendItemCollection());
 
 		return combinedPlot;
 	}
 
 	private static void appendToCombinedRangeManhattanPlot(CombinedRangeXYPlot combinedPlot, String chromosome, XYSeriesCollection currChrSC, boolean showlables, double threshold, Color background, Color backgroundAlternative, Color main) {
 		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
 
 		// Set dot shape of the currently appended Series
 		renderer.setSeriesPaint(currChrSC.getSeriesCount() - 1, main);
 		renderer.setSeriesVisibleInLegend(currChrSC.getSeriesCount() - 1, showlables);
 		renderer.setSeriesShape(currChrSC.getSeriesCount() - 1, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
 
 		// Set range axis
 		if (combinedPlot.getSubplots().isEmpty()) {
 			LogAxis rangeAxis = new LogAxis("P value");
 			rangeAxis.setBase(10);
 			rangeAxis.setInverted(true);
 			rangeAxis.setNumberFormatOverride(new DecimalFormat("0.#E0#"));
 
 			rangeAxis.setTickMarkOutsideLength(2.0f);
 			rangeAxis.setMinorTickCount(2);
 			rangeAxis.setMinorTickMarksVisible(true);
 			rangeAxis.setAxisLineVisible(true);
 			rangeAxis.setUpperMargin(0);
 
 			TickUnitSource units = NumberAxis.createIntegerTickUnits();
 			rangeAxis.setStandardTickUnits(units);
 
 			combinedPlot.setRangeAxis(0, rangeAxis);
 		}
 
 		// Build subchart
 		JFreeChart subchart = ChartFactory.createScatterPlot("",
 				"Chr " + chromosome,
 				"",
 				currChrSC,
 				PlotOrientation.VERTICAL,
 				false,
 				false,
 				false);
 
 		// Get subplot from subchart
 		XYPlot subplot = (XYPlot) subchart.getPlot();
 		subplot.setRenderer(renderer);
 		subplot.setBackgroundPaint(null);
 
 		// CHART BACKGROUD COLOR
 		if (combinedPlot.getSubplots().size() % 2 == 0) {
 			subplot.setBackgroundPaint(background); // Hue, saturation, brightness
 		} else {
 			subplot.setBackgroundPaint(backgroundAlternative); // Hue, saturation, brightness
 		}
 
 		// Add significance Threshold to subplot
 		final Marker thresholdLine = new ValueMarker(threshold);
 		thresholdLine.setPaint(Color.red);
 		DecimalFormat df1 = new DecimalFormat("0.#E0#");
 		// Add legend to hetzyThreshold
 		if (showlables) {
 			thresholdLine.setLabel("P = " + df1.format(threshold));
 		}
 		thresholdLine.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
 		thresholdLine.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
 		subplot.addRangeMarker(thresholdLine);
 
 		// Chromosome Axis Labels
 		NumberAxis chrAxis = (NumberAxis) subplot.getDomainAxis();
 		chrAxis.setLabelAngle(1.0);
 		chrAxis.setAutoRangeIncludesZero(false);
 		chrAxis.setAxisLineVisible(true);
 
 		chrAxis.setTickLabelsVisible(false);
 		chrAxis.setTickMarksVisible(false);
 //            chrAxis.setNumberFormatOverride(new DecimalFormat("0.##E0#"));
 //            TickUnitSource units = NumberAxis.createIntegerTickUnits();
 //            chrAxis.setStandardTickUnits(units);
 
 		//combinedPlot.setGap(0);
 		combinedPlot.add(subplot, 1);
 	}
 
 	public static XYPlot buildQQPlot(int opId, String netCDFVar, int df) throws IOException {
 
 		//<editor-fold defaultstate="expanded" desc="PLOT DEFAULTS">
 		Color background = Config.getConfigColor(
 				PLOT_QQ_BACKGROUND_CONFIG,
 				PLOT_QQ_BACKGROUND_DEFAULT);
 		Color actual = Config.getConfigColor(
 				PLOT_QQ_ACTUAL_CONFIG,
 				PLOT_QQ_ACTUAL_DEFAULT);
 		Color sigma = Config.getConfigColor(
 				PLOT_QQ_SIGMA_CONFIG,
 				PLOT_QQ_SIGMA_DEFAULT);
 		Color mu = Config.getConfigColor(
 				PLOT_QQ_MU_CONFIG,
 				PLOT_QQ_MU_DEFAULT);
 		//</editor-fold>
 
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 		//<editor-fold defaultstate="expanded" desc="GET X^2">
 		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 		MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
 
 		List<double[]> gntypAssocChiSqrVals = rdAssocMarkerSet.getListWithVariable(assocNcFile, netCDFVar);
 		List<Double> obsChiSqrVals = new ArrayList<Double>();
 		for (double[] vals : gntypAssocChiSqrVals) {
 			Double chiSqr = (Double) vals[0];
 			if (!Double.isNaN(chiSqr) && !Double.isInfinite(chiSqr)) {
 				obsChiSqrVals.add(chiSqr);
 			}
 		}
 		Collections.sort(obsChiSqrVals);
 
 		int N = obsChiSqrVals.size();
 		List<Double> expChiSqrDist = null;
 		if (df == 1) {
 			expChiSqrDist = Chisquare.getChiSquareDistributionDf1(N, 1.0f);
 		} else if (df == 2) {
 			expChiSqrDist = Chisquare.getChiSquareDistributionDf2(N, 1.0f);
 		}
 		Collections.sort(expChiSqrDist); // FIXME might be null -> NPE
 
 		assocNcFile.close();
 		//</editor-fold>
 
 		//<editor-fold defaultstate="expanded" desc="GET CONFIDENCE BOUNDARY">
 		InputStream boundaryStream = GenericReportGenerator.class.getResourceAsStream("/samples/chisqrboundary-df" + df + ".txt");
 		InputStreamReader isr = new InputStreamReader(boundaryStream);
 		BufferedReader inputBufferReader = new BufferedReader(isr);
 
 		Double stopValue = expChiSqrDist.get(N - 1);
 		Double currentValue = 0d;
 		List<Double[]> boundaryAL = new ArrayList<Double[]>();
 		while (currentValue <= stopValue) {
 			String l = inputBufferReader.readLine();
 			if (l == null) {
 				break;
 			}
 			String[] cVals = l.split(",");
 			Double[] slice = new Double[3];
 			slice[0] = Double.parseDouble(cVals[0]);
 			slice[1] = Double.parseDouble(cVals[1]);
 			slice[2] = Double.parseDouble(cVals[2]);
 			currentValue = slice[1];
 			boundaryAL.add(slice);
 		}
 		inputBufferReader.close();
 		//</editor-fold>
 
 		XYSeriesCollection dataSeries = new XYSeriesCollection();
 		XYSeries seriesData = new XYSeries("X²");
 		XYSeries seriesRef = new XYSeries("Expected");
 
 		for (int i = 0; i < obsChiSqrVals.size(); i++) {
 			double obsVal = obsChiSqrVals.get(i);
 			double expVal = expChiSqrDist.get(i);
 
 			seriesData.add(expVal, obsVal);
 			seriesRef.add(expVal, expVal);
 		}
 
 		//constant chi-square boundaries
 		XYSeries seriesLower = new XYSeries("2σ boundary");
 		XYSeries seriesUpper = new XYSeries("");
 		for (int i = 0; i < boundaryAL.size(); i++) {
 			Double[] slice = boundaryAL.get(i);
 			seriesUpper.add(slice[1], slice[0]);
 			seriesLower.add(slice[1], slice[2]);
 		}
 
 		dataSeries.addSeries(seriesData);
 		dataSeries.addSeries(seriesRef);
 		dataSeries.addSeries(seriesUpper);
 		dataSeries.addSeries(seriesLower);
 		final XYDataset data = dataSeries;
 
 		//create QQ plot
 		final boolean withLegend = true;
 		JFreeChart chart = ChartFactory.createScatterPlot(
 				"QQ-plot", "Exp X²", "Obs X²",
 				data,
 				PlotOrientation.VERTICAL,
 				withLegend,
 				false,
 				false);
 
 		final XYPlot plot = chart.getXYPlot();
 		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(false, true);
 		renderer.setSeriesPaint(0, actual);
 		renderer.setSeriesPaint(1, mu);
 		renderer.setSeriesPaint(2, sigma);
 		renderer.setSeriesPaint(3, sigma);
 
 		renderer.setBaseShapesVisible(true);
 		renderer.setBaseShapesFilled(true);
 		renderer.setSeriesShape(0, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
 		renderer.setSeriesShape(1, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
 		renderer.setSeriesShape(2, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
 		renderer.setSeriesShape(3, new Rectangle2D.Double(-1.0, -1.0, 2.0, 2.0));
 
 		plot.setRenderer(renderer);
 
 		// PLOT BACKGROUND COLOR
 		plot.setBackgroundPaint(background); // Hue, saturation, brightness
 
 		return plot;
 	}
 
 	public static XYDataset getManhattanZoomByChrAndPos(
 			ManhattanPlotZoom manhattanPlotZoom,
 			int opId,
 			String netCDFVar,
 			String chr,
 			String markerId,
 			long requestedPhysPos,
 			long requestedPosWindow)
 	{
 		XYDataset resultXYDataset = null;
 
 		try {
 			Map<MarkerKey, Object> dataSetMap = new LinkedHashMap<MarkerKey, Object>();
 			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 			NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 			MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
 			Map<MarkerKey, Object> rdAssocMarkerSetMap = rdAssocMarkerSet.getOpSetMap();
 
 			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
 			rdInfoMarkerSet.initFullMarkerIdSetMap();
 			rdInfoMarkerSet.fillMarkerSetMapWithChrAndPos();
 
 			// ESTIMATE WINDOW SIZE
 			Long minPosition;
 			Long middlePosition;
 			Long maxPosition;
 			if (markerId == null) {
 				minPosition = requestedPhysPos;
				middlePosition = Math.round((double) minPosition + requestedPosWindow / 2);
 				maxPosition = minPosition + requestedPosWindow;
 			} else {
 				middlePosition = requestedPhysPos;
				minPosition = Math.round((double) middlePosition - requestedPosWindow / 2);
 				maxPosition = minPosition + requestedPosWindow;
 			}
 
 			// CUT READ-Map TO SIZE
 			for (Map.Entry<MarkerKey, Object> entry : rdAssocMarkerSetMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] chrInfo = (Object[]) rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				Object[] plotInfo = new Object[3];
 				if (chrInfo[0].toString().equals(chr)
 						&& ((Integer) chrInfo[1] >= minPosition)
 						&& ((Integer) chrInfo[1] <= maxPosition))
 				{
 					plotInfo[0] = chrInfo[0];
 					plotInfo[1] = chrInfo[1];
 					dataSetMap.put(key, plotInfo);
 				}
 			}
 
 			//<editor-fold defaultstate="expanded" desc="GET Pval">
 			rdAssocMarkerSetMap = rdAssocMarkerSet.fillOpSetMapWithVariable(assocNcFile, netCDFVar);
 			assocNcFile.close();
 			if (rdAssocMarkerSetMap != null) {
 				for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 					MarkerKey key = entry.getKey();
 					Object[] data = (Object[]) entry.getValue(); //CHR, POS, PVAL
 					double[] value = (double[]) rdAssocMarkerSetMap.get(key);
 					Double pval = (Double) value[1]; // PVAL
 					if (!Double.isNaN(pval) && !Double.isInfinite(pval)) { // Ignore NaN Pvalues
 						data[2] = pval;
 						entry.setValue(data);
 					}
 				}
 
 				rdAssocMarkerSetMap.clear();
 			}
 			//</editor-fold>
 
 			//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
 			XYSeries dataSeries = new XYSeries("");
 
 			Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>();
 			for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 				MarkerKey tmpMarker = entry.getKey();
 				Object[] data = (Object[]) entry.getValue(); // CHR, POS, PVAL
 
 				int position = (Integer) data[1];
 				double pVal = 1;
 				if (data[2] != null) {
 					pVal = (Double) data[2]; // Is allready free of NaN
 				}
 
 				if (pVal < 1 && !Double.isInfinite(pVal)) {
 					dataSeries.add(position, pVal);
 					labeler.put(chr + "_" + position, tmpMarker);
 					//labeler.put(key, "");
 				}
 			}
 			manhattanPlotZoom.setLabelerMap(labeler);
 
 			dataSeries.setDescription("Zoom chr " + chr + " from position " + minPosition + " to " + maxPosition);
 
 			resultXYDataset = new XYSeriesCollection(dataSeries);
 			//</editor-fold>
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 
 		return resultXYDataset;
 	}
 
 	/**
 	 * This getManhattanZoomByMarkerIdOrIdx has now been deprecated in favor of
 	 * getManhattanZoomByChrAndPos
 	 *
 	 * @deprecated Use getManhattanZoomByChrAndPos instead
 	 */
 	public static XYDataset getManhattanZoomByMarkerIdOrIdx(
 			ManhattanPlotZoom manhattanPlotZoom,
 			int opId,
 			String netCDFVar,
 			MarkerKey origMarkerKey,
 			int startIdxPos,
 			int requestedSetSize)
 	{
 		XYDataset resultXYDataset = null;
 
 		try {
 			Map<MarkerKey, Object> dataSetMap = new LinkedHashMap<MarkerKey, Object>();
 			OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 			NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 			MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
 			Map<MarkerKey, Object> rdAssocMarkerSetMap = rdAssocMarkerSet.getOpSetMap();
 
 			//<editor-fold defaultstate="expanded" desc="GET POSITION DATA">
 			MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
 			rdInfoMarkerSet.initFullMarkerIdSetMap();
 
 
 			//ESTIMATE WINDOW SIZE
 			Integer minPosition = 0;
 			Integer middlePosition = requestedSetSize / 2;
 			Integer maxPosition = requestedSetSize;
 
 			minPosition = startIdxPos;
 			middlePosition = Math.round((float) (minPosition + requestedSetSize) / 2);
 			maxPosition = minPosition + requestedSetSize;
 
 
 			if (rdAssocMarkerSetMap.size() < maxPosition) {
 				requestedSetSize = rdAssocMarkerSetMap.size();
 				minPosition = 0;
				middlePosition = Math.round(requestedSetSize / 2);
 				maxPosition = requestedSetSize;
 			} else {
 				Iterator it = rdAssocMarkerSetMap.keySet().iterator();
 				if (startIdxPos == Integer.MIN_VALUE) { // USE MARKERID TO LOCATE CENTER
 					boolean goOn = true;
 					int i = 0;
 					while (goOn && i < rdAssocMarkerSetMap.size()) {
 						Object key = it.next();
 						if (key.equals(origMarkerKey)) {
 							minPosition = i - Math.round(requestedSetSize / 2);
 							if (minPosition < 0) {
 								minPosition = 0;
 							}
 
 							middlePosition = i;
 
 							maxPosition = i + Math.round(requestedSetSize / 2);
 							if (maxPosition > rdAssocMarkerSetMap.size()) {
 								maxPosition = rdAssocMarkerSetMap.size();
 							}
 							goOn = false;
 						}
 						i++;
 					}
 
 				}
 			}
 
 			// CUT READ-Map TO SIZE
 			boolean goOn = true;
 			int i = 0;
 			Iterator<MarkerKey> it = rdAssocMarkerSetMap.keySet().iterator();
 			while (goOn && i < rdAssocMarkerSetMap.size()) {
 				MarkerKey key = it.next();
 				if (i >= minPosition && i <= maxPosition) {
 					dataSetMap.put(key, "");
 					if (i == middlePosition) { // MAKE SURE WE KNOW WHAT MARKERID IS IN THE MIDDLE
 						origMarkerKey = key;
 					}
 				}
 				if (i > maxPosition) {
 					goOn = false;
 				}
 				i++;
 			}
 
 			// GET MARKER CHR & POS INFO
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 			// First check for same chromosome data
 			String validateChr = rdInfoMarkerSet.getMarkerIdSetMap().get(origMarkerKey).toString();
 			manhattanPlotZoom.setCenterPhysPos((long) minPosition);
 
 			for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				String chr = rdInfoMarkerSet.getMarkerIdSetMap().get(key).toString();
 				Object[] data = new Object[3]; // CHR, POS, PVAL
 				data[0] = chr;
 				entry.setValue(data);
 			}
 
 			rdInfoMarkerSet.fillWith(0);
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 			for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] data = (Object[]) entry.getValue(); // CHR, POS, PVAL
 				int pos = (Integer) rdInfoMarkerSet.getMarkerIdSetMap().get(key);
 				data[1] = pos;
 				entry.setValue(data);
 			}
 			if (rdInfoMarkerSet.getMarkerIdSetMap() != null) {
 				rdInfoMarkerSet.getMarkerIdSetMap().clear();
 			}
 			//</editor-fold>
 
 			//<editor-fold defaultstate="expanded" desc="GET Pval">
 			rdAssocMarkerSetMap = rdAssocMarkerSet.fillOpSetMapWithVariable(assocNcFile, netCDFVar);
 			for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] data = (Object[]) entry.getValue(); // CHR, POS, PVAL
 				double[] value = (double[]) rdAssocMarkerSetMap.get(key);
 				Double pval = (Double) value[1]; // PVAL
 				if (!Double.isNaN(pval) && !Double.isInfinite(pval)) { // Ignore NaN Pvalues
 					data[2] = pval;
 					entry.setValue(data);
 				}
 			}
 			assocNcFile.close();
 			if (rdAssocMarkerSetMap != null) {
 				rdAssocMarkerSetMap.clear();
 			}
 			//</editor-fold>
 
 			//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
 			XYSeries dataSeries = new XYSeries("");
 
 			Map<String, MarkerKey> labeler = new LinkedHashMap<String, MarkerKey>();
 			for (Map.Entry<MarkerKey, Object> entry : dataSetMap.entrySet()) {
 				MarkerKey tmpMarker = entry.getKey();
 				Object[] data = (Object[]) entry.getValue(); //CHR, POS, PVAL
 
 				String chr = data[0].toString();
 				int position = (Integer) data[1];
 
 				if (data[2] != null && validateChr.equals(chr)) { // Check for same chromosome data before adding
 					double pVal = (Double) data[2]; // Is allready free of NaN
 					if (pVal < 1 && pVal != Double.POSITIVE_INFINITY && pVal != Double.NEGATIVE_INFINITY) {
 						dataSeries.add(position, pVal);
 						labeler.put(chr + "_" + position, tmpMarker);
 						//labelerHM.put(key,"");
 					}
 				}
 			}
 			long snpNumber = labeler.size();
 			manhattanPlotZoom.setLabelerMap(labeler);
 
 			dataSeries.setDescription("Zoom on " + origMarkerKey + ", window size: " + snpNumber);
 
 			resultXYDataset = new XYSeriesCollection(dataSeries);
 			//</editor-fold>
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 
 		return resultXYDataset;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="SAMPLE-QA PLOTS">
 	public static XYDataset getSampleHetzygDataset(SampleQAHetzygPlotZoom sampleQAHetzygPlotZoom, int opId) throws IOException {
 		XYDataset resultXYDataset;
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 		NetcdfFile sampleQANcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 		SampleOperationSet rdSampleQAOPSet = new SampleOperationSet(rdOPMetadata.getStudyId(), opId);
 
 		Map<SampleKey, Object> sampleSetMap = rdSampleQAOPSet.getOpSetMap();
 		List<Double> hetzygVals = rdSampleQAOPSet.getListWithVariable(sampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_HETZYRAT);
 		List<Double> missingratVals = rdSampleQAOPSet.getListWithVariable(sampleQANcFile, cNetCDF.Census.VAR_OP_SAMPLES_MISSINGRAT);
 
 		//<editor-fold defaultstate="expanded" desc="BUILD XYDataset">
 		XYSeries dataSeries = new XYSeries("");
 
 		int count = 0;
 		Map<String, SampleKey> samplesLabeler = new LinkedHashMap<String, SampleKey>();
 		for (Map.Entry<SampleKey, Object> entry : sampleSetMap.entrySet()) {
 			SampleKey tmpSampleKey = entry.getKey();
 			double tmpHetzyVal = hetzygVals.get(count);
 			double tmpMissratVal = missingratVals.get(count);
 			if (Double.isNaN(tmpHetzyVal) || Double.isInfinite(tmpHetzyVal)) {
 				tmpHetzyVal = 0;
 			}
 			if (Double.isNaN(tmpMissratVal) || Double.isInfinite(tmpMissratVal)) {
 				tmpMissratVal = 0;
 			}
 
 			dataSeries.add(tmpHetzyVal, tmpMissratVal);
 			samplesLabeler.put(count + "_" + tmpMissratVal + "_" + tmpHetzyVal, tmpSampleKey);
 			count++;
 		}
 		sampleQAHetzygPlotZoom.setLabelerMap(samplesLabeler);
 
 		dataSeries.setDescription(rdOPMetadata.getDescription());
 
 		resultXYDataset = new XYSeriesCollection(dataSeries);
 		//</editor-fold>
 
 		return resultXYDataset;
 	}
 	//</editor-fold>
 
 	//<editor-fold defaultstate="expanded" desc="HELPERS">
 	public static Map<MarkerKey, Object> getAnalysisVarData(int opId, String netCDFVar) throws IOException {
 
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 
 		NetcdfFile assocNcFile = NetcdfFile.open(rdOPMetadata.getPathToMatrix());
 		MarkerOperationSet rdAssocMarkerSet = new MarkerOperationSet(rdOPMetadata.getStudyId(), opId);
 		Map<MarkerKey, Object> rdAssocMarkerSetMap = rdAssocMarkerSet.getOpSetMap();
 		rdAssocMarkerSetMap = rdAssocMarkerSet.fillOpSetMapWithVariable(assocNcFile, netCDFVar);
 
 		assocNcFile.close();
 		return rdAssocMarkerSetMap;
 	}
 
 	public static Map<MarkerKey, Object> getMarkerSetChrAndPos(int opId) throws IOException {
 		Map<MarkerKey, Object> dataSetMap = new LinkedHashMap<MarkerKey, Object>();
 		OperationMetadata rdOPMetadata = OperationsList.getOperationMetadata(opId);
 
 		//<editor-fold defaultstate="expanded" desc="GET POSITION DATA">
 		MarkerSet_opt rdInfoMarkerSet = new MarkerSet_opt(rdOPMetadata.getStudyId(), rdOPMetadata.getParentMatrixId());
 		rdInfoMarkerSet.initFullMarkerIdSetMap();
 		rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_CHR);
 		if (rdInfoMarkerSet.getMarkerIdSetMap() != null) {
 			for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 				MarkerKey key = entry.getKey();
 				String chr = entry.getValue().toString();
 				Object[] data = new Object[2]; //CHR, POS
 				data[0] = chr;
 				dataSetMap.put(key, data);
 			}
 
 			rdInfoMarkerSet.fillWith(0);
 			rdInfoMarkerSet.fillInitMapWithVariable(cNetCDF.Variables.VAR_MARKERS_POS);
 			for (Map.Entry<MarkerKey, Object> entry : rdInfoMarkerSet.getMarkerIdSetMap().entrySet()) {
 				MarkerKey key = entry.getKey();
 				Object[] data = (Object[]) dataSetMap.get(key); //CHR, POS
 				int pos = (Integer) entry.getValue();
 				data[1] = pos;
 				dataSetMap.put(key, data);
 			}
 
 			rdInfoMarkerSet.getMarkerIdSetMap().clear();
 		}
 		//</editor-fold>
 
 		return dataSetMap;
 	}
 	//</editor-fold>
 }
