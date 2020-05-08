 /*
  * #%L
  * SLIM plugin for combined spectral-lifetime image analysis.
  * %%
  * Copyright (C) 2010 - 2014 Board of Regents of the University of
  * Wisconsin-Madison.
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 package loci.slim2.histogram;
 
 import imagej.command.Command;
 import imagej.data.Dataset;
 import imagej.data.DatasetService;
 import imagej.data.command.InteractiveImageCommand;
 import imagej.data.display.DatasetView;
 import imagej.display.DisplayService;
 import imagej.menu.MenuConstants;
 import imagej.render.RenderingService;
 import net.imglib2.Binning;
 import net.imglib2.Cursor;
 import net.imglib2.algorithm.stats.ComputeMinMax;
 import net.imglib2.meta.Axes;
 import net.imglib2.meta.ImgPlus;
 import net.imglib2.type.numeric.RealType;
 
 import org.scijava.ItemIO;
 import org.scijava.plugin.Menu;
 import org.scijava.plugin.Parameter;
 import org.scijava.plugin.Plugin;
 
 /**
  * Plugin that sets the minimum and maximum for scaling of display values. Sets
  * the same min/max for each channel.
  * 
  * Based on {@link imagej.core.commands.display.interactive.BrightnessContrast}
  * by Curtis Rueden & Grant Harris.
  */
 //TODO ARG I thought perhaps there was an image realm for histograms and a data one,
 //  with the latter if you have enough measurements the distribution tends to normal
 //  then I found normalizing the histogram is a good image technique
 
 @Plugin(type = Command.class, menu = {
 	@Menu(label = MenuConstants.ANALYZE_LABEL, weight = MenuConstants.ANALYZE_WEIGHT,
 		mnemonic = MenuConstants.ANALYZE_MNEMONIC),
 	@Menu(label = "Data Histogram...", accelerator = "control shift C",
 		weight = 0) }, iconPath = "/icons/commands/contrast.png", headless = true, //TODO ARG use 'normal.png', which is on my Desktop
 		initializer = "initValues")
 public class DataHistogramCommand extends InteractiveImageCommand {
 
 	@Parameter
 	private DisplayService displayService;
 
 	@Parameter
 	private DatasetService datasetService;
 
 	@Parameter
 	private RenderingService renderingService;
 
 	@Parameter(type = ItemIO.BOTH, callback = "viewChanged")
 	private DatasetView view; // get "[WARNING] No widget found for input: view" unless a file has been loaded ahead of time
 
 	@Parameter(label = "Minimum", persist = false, callback = "minMaxChanged")
 	private double min = Double.NaN;
 
 	@Parameter(label = "Maximum", persist = false, callback = "minMaxChanged")
 	private double max = Double.NaN;
 
 	@Parameter(label = "Show full range", persist = true,
 		callback = "rangeChanged")
 	private boolean showFullRange;
 
 	@Parameter(label = "Logarithmic", persist = true,
 		callback = "logarithmicChanged")
 	private boolean logarithmic;
 
 	@Parameter(label = "Show low counts", persist = true,
 		callback = "showLowCountsChanged")
 	private boolean showLowCounts;
 
 	/** The minimum and maximum values of the data itself. */
 	private double dataMin, dataMax;
 
 	/** The initial minimum and maximum values of the data view. */
 	private double initialMin, initialMax;
 
 	private HistogramGraph histogramGraph;
 
 	private DatasetView saveView;
 
 	private volatile boolean running;
 
 	public DataHistogramCommand() {
 		super("view");
 		System.out.println("DataHistogramCommand ctor, view is " + view);
 		logarithmic = false;
 		showLowCounts = false;
 		running = false;
 	}
 
 	// -- Runnable methods --
 
 	@Override
 	public void run() {
 		System.out.println("DataHistogramCommand.run, view is " + view);
 		//TODO ARG
 		// 'run' gets called again after every UI change.  This plugin should
 		// stay active all the time.
 		if (!running) {
 			running = true;
 			if (null == histogramGraph) {
 				histogramGraph = new HistogramGraph(datasetService, renderingService);
 				Dataset dataset = histogramGraph.getDataset();
 				displayService.createDisplay(dataset);
 			}
 			else {
 				System.out.println("DHC run and histo was not null");
 			}
 			updateDisplay();
 			System.out.println("DONE");
 			running = false;
 		}
 	}
 
 	// -- DataHistogramCommand methods --
 
 	public DatasetView getView() {
 		return view;
 	}
 
 	public void setView(final DatasetView view) {
 		this.view = view;
 		saveView = view;
 	}
 
 	public double getMinimum() {
 		return min;
 	}
 
 	public void setMinimum(final double min) {
 		this.min = min;
 	}
 
 	public double getMaximum() {
 		return max;
 	}
 
 	public void setMaximum(final double max) {
 		this.max = max;
 	}
 
 	// -- Initializers --
 
 	protected void initValues() {
 		if (null != view) {
 			viewChanged();
 		}
 	}
 
 	// -- Callback methods --
 
 	/** Called when view changes. Updates everything to match. */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	protected void viewChanged() {
 		// ignore clicks on the histogram graph itself
 		if (null != view && null != histogramGraph && view.getData() == histogramGraph.getDataset()) {
 			// restore previous view
 			view = saveView;
 			return;
 		}
 
 		// did view really change?
 		if (saveView == null || view != saveView) {
 			saveView = view;
 			System.out.println("new view " + view);
 
 			final Dataset dataset = view.getData();
 			final ImgPlus img = dataset.getImgPlus();
 
 			//TODO ARG just autorange it for now
 			computeDataMinMax(img);
 			min = dataMin;
 			max = dataMax;
 
 			updateHistogram(img);
 
 			long[] dims = new long[img.numDimensions()];
 			img.dimensions(dims);
 			long channels = 1;
 			final int channelIndex = img.dimensionIndex(Axes.CHANNEL);
 			if (channelIndex >= 0) {
 				channels = img.dimension(channelIndex);
 			}
 			for (int c = 0; c < channels; ++c) {
 				System.out.println("channel " + c + " min " + img.getChannelMinimum(c) + " max " + img.getChannelMaximum(c));
 			}
 			System.out.println("data min " + dataMin + " max " + dataMax + " min " + min + " max " + max);
 			updateDisplay();
 		}
 	}
 
 	//TODO ARG having default min/max increments is not ideal for all min/max values; also no upper/lower limits on values
 	/** Called when min or max changes. Updates brightness and contrast. */
 	protected void minMaxChanged() {
 		System.out.println("min max changed " + min + " " + max);
 		if (null != view) {
 			final Dataset dataset = view.getData();
 			final ImgPlus img = dataset.getImgPlus();
 			updateHistogram(img);
 			updateDisplay();
 			//TODO ARG etc?? see above
 		}
 	}
 
 	/** Called when show full range changes. */
 	protected void rangeChanged() {
 		System.out.println("RANGE CHANGED");
 	}
 
 	/** Called when logarithmic changes. */
 	protected void logarithmicChanged() {
 		System.out.println("LOG CHANGED " + logarithmic);
 		if (null != histogramGraph) {
 			histogramGraph.setLogarithmic(logarithmic);
 		}
 	}
 
 	/** Called when show low counts changes. */
 	protected void showLowCountsChanged() {
 		System.out.println("LOW COUNTS CHANGED " + showLowCounts);
 		if (null != histogramGraph) {
 			histogramGraph.setDistinguishNonZero(showLowCounts);
 			long[] histogram = histogramGraph.getHistogram();
 			int i = 0;
 			for (long h : histogram) {
 				System.out.print(" " + h);
 				if (i++ % 10 == 0) System.out.println();
 			}
 		}
 	}
 
 	// -- Helper methods --
 
 	private <T extends RealType<T>> void updateHistogram(final ImgPlus<T> img) {
 		System.out.println("in updateHistogram and min max are " + min + " " + max);
 		//TODO ARG use statistics service
 		if (null != histogramGraph) {
 			long[] histogram = new long[256];
 			Cursor<T> cursor = img.cursor();
 			while (cursor.hasNext()) {
 				cursor.fwd();
 				double value = cursor.get().getRealDouble();
 				if (!Double.isNaN(value)) {
 					// 'exclusive' means don't count values outside the min/max range
 					int index = Binning.exclusiveValueToBin(256, min, max, value);
 					if (index >= 0 && index < histogram.length) {
 						++histogram[index];
 					}
 				}
 			}
 			histogramGraph.updateHistogram(histogram);
 		}
 	}
 
 	private <T extends RealType<T>> void computeDataMinMax(final ImgPlus<T> img) {
 		// FIXME: Reconcile this with DefaultDatasetView.autoscale(int). There is
 		// no reason to hardcode the usage of ComputeMinMax twice. Rather, there
 		// should be a single entry point for obtain the channel min/maxes from
 		// the metadata, and if they aren't there, then compute them. Probably
 		// Dataset (not DatasetView) is a good place for it, because it is metadata
 		// independent of the visualization settings.
 		//TODO ARG we need 2 versions/options of min/max: one for the entire channel, one for current plane of channel
		final T type = img.firstElement();
		final ComputeMinMax<T> computeMinMax =
			new ComputeMinMax<T>(img, type.createVariable(), type.createVariable());
 		computeMinMax.process();
 		dataMin = computeMinMax.getMin().getRealDouble();
 		dataMax = computeMinMax.getMax().getRealDouble();
 		log().debug("computeDataMinMax: dataMin=" + dataMin + ", dataMax=" + dataMax);
 	}
 
 	/** Updates the displayed min/max range to match min and max values. */
 	private void updateDisplay() {
 		System.out.println("updateDisplay min " + min + " max " + max);
 		view.setChannelRanges(min, max);
 		view.getProjector().map();
 		view.update();
 	}
 
 }
