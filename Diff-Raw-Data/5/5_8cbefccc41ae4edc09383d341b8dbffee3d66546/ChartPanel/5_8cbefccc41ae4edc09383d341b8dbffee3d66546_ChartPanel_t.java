 package gui;
 
 import freechart.AbstractFreeChartPanel;
 import freechart.FreeChartPanel.ChartMouseSelectionListener;
 import freechart.HistogramPanel;
 import freechart.StackedBarPlot;
 import gui.swing.ComponentFactory;
 import gui.swing.TransparentViewPanel;
 import gui.util.Highlighter;
 import gui.util.MoleculePropertyHighlighter;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import main.ScreenSetup;
 import util.ArrayUtil;
 import util.ColorUtil;
 import util.CountedSet;
 import util.DefaultComparator;
 import util.ObjectUtil;
 import util.SequentialWorkerThread;
 import util.ToStringComparator;
 import cluster.Cluster;
 import cluster.Clustering;
 import cluster.Model;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.Sizes;
 
 import dataInterface.MoleculeProperty;
 import dataInterface.MoleculeProperty.Type;
 import dataInterface.MoleculePropertyUtil;
 
 public class ChartPanel extends TransparentViewPanel
 {
 	Clustering clustering;
 	ViewControler viewControler;
 	GUIControler guiControler;
 
 	Cluster cluster;
 	List<Model> models;
 	MoleculeProperty property;
 
 	private JPanel featurePanel;
 	private JLabel featureNameLabel = ComponentFactory.createViewLabel("");
 	private JLabel featureSetLabel = ComponentFactory.createViewLabel("");
 	private JLabel featureDescriptionLabel = ComponentFactory.createViewLabel("");
 	private JLabel featureDescriptionLabelHeader = ComponentFactory.createViewLabel("Description:");
 	private JLabel featureSmartsLabelHeader = ComponentFactory.createViewLabel("Smarts:");
 	private JLabel featureSmartsLabel = ComponentFactory.createViewLabel("");
 	private JLabel featureMappingLabel = ComponentFactory.createViewLabel("");
 	private JLabel featureMissingLabel = ComponentFactory.createViewLabel("");
 
 	Set<String> cardContents = new HashSet<String>();
 	JPanel cardPanel;
 
 	SequentialWorkerThread workerThread = new SequentialWorkerThread();
 
 	public ChartPanel(Clustering clustering, ViewControler viewControler, GUIControler guiControler)
 	{
 		this.clustering = clustering;
 		this.viewControler = viewControler;
 		this.guiControler = guiControler;
 
 		buildLayout();
 		addListeners();
 		update(true);
 	}
 
 	private void buildLayout()
 	{
 		DefaultFormBuilder b = new DefaultFormBuilder(new FormLayout("p,3dlu,p"));
 		b.setLineGapSize(Sizes.pixel(2));
 		b.append(ComponentFactory.createViewLabel("<html><b>Feature:</b><html>"));
 		b.append(featureNameLabel);
 		b.nextLine();
 		b.append("");
 		b.append(featureSetLabel);
 		b.nextLine();
 		b.append(featureDescriptionLabelHeader);
 		b.append(featureDescriptionLabel);
 		b.nextLine();
 		b.append(featureSmartsLabelHeader);
 		b.append(featureSmartsLabel);
 		b.nextLine();
 		b.append(ComponentFactory.createViewLabel("<html>Usage:<html>"));
 		b.append(featureMappingLabel);
 		b.nextLine();
 		b.append(ComponentFactory.createViewLabel("<html>Missing values:<html>"));
 		b.append(featureMissingLabel);
 
 		featurePanel = b.getPanel();
 		featurePanel.setOpaque(false);
 
 		setLayout(new BorderLayout(3, 3));
 
 		add(featurePanel, BorderLayout.NORTH);
 
 		cardPanel = new JPanel(new CardLayout());
 		cardPanel.setOpaque(false);
 		add(cardPanel, BorderLayout.CENTER);
 
 		setOpaque(true);
 		//		setBackground(Settings.TRANSPARENT_BACKGROUND);
 	}
 
 	private void addListeners()
 	{
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_HIGHLIGHT_CHANGED))
 				{
 					update(false);
 				}
 			}
 		});
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(Clustering.CLUSTER_MODIFIED))
 				{
 					update(true);
 				}
 				else if (evt.getPropertyName().equals(Clustering.CLUSTER_REMOVED))
 				{
 					cardPanel.removeAll();
 					cardContents.clear();
 					update(true);
 				}
 				else if (evt.getPropertyName().equals(Clustering.CLUSTER_ADDED))
 				{
 					cardPanel.removeAll();
 					cardContents.clear();
 					update(true);
 				}
 			}
 		});
 		clustering.getClusterActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update(false);
 			}
 		});
 		clustering.getClusterWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update(false);
 			}
 		});
 		clustering.getModelActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update(false);
 			}
 		});
 		clustering.getModelWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				update(false);
 			}
 		});
 
 	}
 
 	private static String getKey(Cluster c, MoleculeProperty p, List<Model> m)
 	{
 		String mString = "";
 		for (Model model : m)
 			mString = mString.concat(model.toString());
 		return (c == null ? "null" : c.getName()) + "_" + p.toString() + "_" + mString;
 	}
 
 	boolean selfUpdate = false;
 	List<Integer> selfUpdateModels = null;
 
 	private abstract class ModelSelector implements ChartMouseSelectionListener
 	{
 		protected abstract boolean hasSelectionCriterionChanged();
 
 		protected abstract void updateSelectionCriterion();
 
 		protected abstract boolean isSelected(Model m, MoleculeProperty p);
 
 		@Override
 		public void hoverEvent()
 		{
 			handleEvent(true, false);
 		}
 
 		@Override
 		public void clickEvent(boolean ctrlDown)
 		{
 			handleEvent(false, ctrlDown);
 		}
 
 		private void handleEvent(boolean hover, boolean ctrlDown)
 		{
 			System.err.println();
 			if (selfUpdate)
 			{
 				System.err.println("self update");
 				return;
 
 			}
 			selfUpdate = true;
 			try
 			{
 				if (!hasSelectionCriterionChanged() && hover)
 				{
 					System.err.println("selection criterion has not changed");
 					return;
 				}
 				updateSelectionCriterion();
				if (this instanceof NumericModelSelector)
					System.err.println("interval : " + ((NumericModelSelector) this).hist.getSelectedMin() + " "
							+ ((NumericModelSelector) this).hist.getSelectedMax());
 
 				//				if (clustering.isClusterActive())
 				//				{
 				Highlighter h = viewControler.getHighlighter();
 				MoleculeProperty prop = null;
 				if (h instanceof MoleculePropertyHighlighter)
 					prop = ((MoleculePropertyHighlighter) h).getProperty();
 
 				final List<Integer> m = new ArrayList<Integer>();
 				Iterable<Model> models;
 				if (clustering.isClusterActive())
 					models = clustering.getCluster(clustering.getClusterActive().getSelected()).getModels();
 				else
 					models = clustering.getModels(false);
 				for (Model model : models)
 					if (isSelected(model, prop))
 						m.add(model.getModelIndex());
 
 				if (hover)
 				{
 					if (ObjectUtil.equals(selfUpdateModels, m))
 						return;
 					selfUpdateModels = m;
 					System.err.println("updating via chart panel " + m);
 					clustering.getModelWatched().setSelectedIndices(ArrayUtil.toPrimitiveIntArray(m));
 				}
 				else
 				{
 					System.err.println("before: "
 							+ ArrayUtil.toString(clustering.getModelActive().getSelectedIndices()));
 					System.err.println("select " + (!ctrlDown) + " " + m);
 					clustering.getModelActive().setSelectedIndices(ArrayUtil.toPrimitiveIntArray(m), !ctrlDown);
 					System.err
 							.println("after: " + ArrayUtil.toString(clustering.getModelActive().getSelectedIndices()));
 					System.err.println();
 				}
 			}
 			finally
 			{
 				selfUpdate = false;
 			}
 		}
 	}
 
 	double selectedMin = 1.0;
 	double selectedMax = 0.0;
 
 	private class NumericModelSelector extends ModelSelector
 	{
 		HistogramPanel hist;
 
 		public NumericModelSelector(HistogramPanel hist)
 		{
 			this.hist = hist;
 		}
 
 		@Override
 		protected boolean hasSelectionCriterionChanged()
 		{
 			return selectedMin != hist.getSelectedMin() || selectedMax != hist.getSelectedMax();
 		}
 
 		@Override
 		protected void updateSelectionCriterion()
 		{
 			selectedMin = hist.getSelectedMin();
 			selectedMax = hist.getSelectedMax();
 		}
 
 		@Override
 		protected boolean isSelected(Model m, MoleculeProperty p)
 		{
 			Double d = m.getDoubleValue(p);
 			return d != null && d >= selectedMin && d <= selectedMax;
 		}
 	}
 
 	private abstract class PlotData
 	{
 		AbstractFreeChartPanel plot;
 
 		public AbstractFreeChartPanel getPlot()
 		{
 			return plot;
 		}
 	}
 
 	private class NumericPlotData extends PlotData
 	{
 		List<String> captions;
 		List<double[]> vals;
 
 		public NumericPlotData(Cluster c, MoleculeProperty p, List<Model> m)
 		{
 			Double v[] = clustering.getDoubleValues(p);
 			captions = new ArrayList<String>();
 			vals = new ArrayList<double[]>();
 			captions.add("Dataset");
 			vals.add(ArrayUtil.toPrimitiveDoubleArray(ArrayUtil.removeNullValues(v)));
 			if (c != null)
 			{
 				captions.add(c.getName());
 				vals.add(ArrayUtil.toPrimitiveDoubleArray(ArrayUtil.removeNullValues(c.getDoubleValues(p))));
 			}
 
 			Double mVals[] = new Double[m.size()];
 			boolean notNull = false;
 			for (int i = 0; i < mVals.length; i++)
 			{
 				mVals[i] = m.get(i).getDoubleValue(p);
 				notNull |= mVals[i] != null;
 			}
 			if (m.size() > 0 && notNull)
 			{
 				if (m.size() == 1)
 					captions.add(m.get(0).toString());
 				else
 					captions.add("Selected compounds");
 				vals.add(ArrayUtil.toPrimitiveDoubleArray(ArrayUtil.removeNullValues(mVals)));
 			}
 
 			plot = new HistogramPanel(null, null, null, "#compounds", captions, vals, 20);
 			plot.addSelectionListener(new NumericModelSelector((HistogramPanel) plot));
 			configurePlotColors(plot, c, m, p);
 		}
 	}
 
 	String selectedCategory;
 
 	private class NominalModelSelector extends ModelSelector
 	{
 		StackedBarPlot bar;
 
 		public NominalModelSelector(StackedBarPlot bar)
 		{
 			this.bar = bar;
 		}
 
 		@Override
 		protected boolean hasSelectionCriterionChanged()
 		{
 			return !ObjectUtil.equals(selectedCategory, bar.getSelectedCategory());
 		}
 
 		@Override
 		protected void updateSelectionCriterion()
 		{
 			selectedCategory = bar.getSelectedCategory();
 		}
 
 		@Override
 		protected boolean isSelected(Model m, MoleculeProperty p)
 		{
 			return ObjectUtil.equals(m.getStringValue(p), selectedCategory);
 		}
 	}
 
 	private class NominalPlotData extends PlotData
 	{
 		LinkedHashMap<String, List<Double>> data;
 		String vals[];
 
 		public NominalPlotData(Cluster c, MoleculeProperty p, List<Model> ms)
 		{
 			Model m = ms.size() > 0 ? ms.get(0) : null;
 
 			String v[] = clustering.getStringValues(p, m);
 			CountedSet<String> datasetSet = CountedSet.fromArray(v);
 			List<String> datasetValues = datasetSet.values(new DefaultComparator<String>());
 
 			CountedSet<String> clusterSet = null;
 			if (c != null)
 			{
 				v = c.getStringValues(p, m);
 				clusterSet = CountedSet.fromArray(v);
 				List<String> clusterValues = clusterSet.values(new DefaultComparator<String>());
 
 				boolean newVal = false;
 				for (String vv : clusterValues)
 					if (!datasetValues.contains(vv))
 					{
 						newVal = true;
 						datasetValues.add(vv);
 					}
 				if (newVal)
 					Collections.sort(datasetValues, new ToStringComparator());
 			}
 			String compoundVal = null;
 			if (m != null && m.getStringValue(p) != null)
 			{
 				compoundVal = m.getStringValue(p);
 
 				if (!datasetValues.contains(compoundVal))
 				{
 					datasetValues.add(compoundVal);
 					Collections.sort(datasetValues, new ToStringComparator());
 				}
 			}
 			data = new LinkedHashMap<String, List<Double>>();
 			if (m != null)
 			{
 				List<Double> compoundCounts = new ArrayList<Double>();
 				for (String o : datasetValues)
 					compoundCounts.add(compoundVal.equals(o) ? 1.0 : 0.0);
 				data.put(m.toString(), compoundCounts);
 			}
 			if (c != null)
 			{
 				List<Double> clusterCounts = new ArrayList<Double>();
 				for (String o : datasetValues)
 					clusterCounts.add((double) clusterSet.getCount(o));
 				data.put(c.getName(), clusterCounts);
 			}
 			List<Double> datasetCounts = new ArrayList<Double>();
 			for (String o : datasetValues)
 				datasetCounts.add((double) datasetSet.getCount(o));
 			data.put("Dataset", datasetCounts);
 
 			vals = new String[datasetValues.size()];
 			datasetValues.toArray(vals);
 
 			for (int i = 0; i < vals.length; i++)
 				if (vals[i] == null)
 					vals[i] = "null";
 
 			plot = new StackedBarPlot(null, null, "#compounds", StackedBarPlot.convertTotalToAdditive(data), vals);
 			plot.addSelectionListener(new NominalModelSelector((StackedBarPlot) plot));
 			configurePlotColors(plot, c, ms, p);
 		}
 	}
 
 	private void configurePlotColors(AbstractFreeChartPanel chartPanel, Cluster cluster, List<Model> models,
 			MoleculeProperty property)
 	{
 		int dIndex = -1;
 		int cIndex = -1;
 		int mIndex = -1;
 
 		if (cluster == null)
 		{
 			if (models.size() == 0)
 				dIndex = 0;
 			else
 			{
 				mIndex = 0;
 				dIndex = 1;
 			}
 		}
 		else
 		{
 			if (models.size() == 0)
 			{
 				cIndex = 0;
 				dIndex = 1;
 			}
 			else
 			{
 				mIndex = 0;
 				cIndex = 1;
 				dIndex = 2;
 			}
 		}
 
 		if (chartPanel instanceof StackedBarPlot)
 		{
 			if (mIndex != -1)
 				throw new IllegalArgumentException(
 						"does NOT help much in terms of visualisation (color code should be enough), difficult to realize in terms of color brightness");
 
 			Color cols[] = MoleculePropertyUtil.getNominalColors(property);
 			if (cIndex == -1)
 			{
 				chartPanel.setSeriesColor(dIndex, ColorUtil.grayscale(MoleculePropertyUtil.getColor(0)));
 				((StackedBarPlot) chartPanel).setSeriesCategoryColors(dIndex, cols);
 			}
 			else
 			{
 				chartPanel.setSeriesColor(dIndex,
 						ColorUtil.grayscale(MoleculePropertyUtil.getColor(0).darker().darker().darker()));
 				chartPanel.setSeriesColor(cIndex, ColorUtil.grayscale(MoleculePropertyUtil.getColor(0)).brighter());
 
 				((StackedBarPlot) chartPanel).setSeriesCategoryColors(dIndex,
 						ColorUtil.darker(ColorUtil.darker(ColorUtil.darker(cols))));
 				((StackedBarPlot) chartPanel).setSeriesCategoryColors(cIndex, ColorUtil.brighter(cols));
 			}
 		}
 		else
 		{
 			if (cIndex == -1)
 				chartPanel.setSeriesColor(dIndex, MoleculePropertyUtil.getColor(0));
 			else
 			{
 				chartPanel.setSeriesColor(dIndex, MoleculePropertyUtil.getColor(0).darker().darker().darker());
 				chartPanel.setSeriesColor(cIndex, MoleculePropertyUtil.getColor(0).brighter());
 			}
 
 			if (mIndex != -1)
 				chartPanel.setSeriesColor(mIndex, MoleculePropertyUtil.getColor(1));
 		}
 
 		chartPanel.setOpaqueFalse();
 		chartPanel.setForegroundColor(ComponentFactory.FOREGROUND);
 		final AbstractFreeChartPanel finalP = chartPanel;
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_BACKGROUND_CHANGED))
 					finalP.setForegroundColor(ComponentFactory.FOREGROUND);
 			}
 		});
 		chartPanel.setShadowVisible(false);
 		chartPanel.setIntegerTickUnits();
 		chartPanel.setBarWidthLimited();
 		chartPanel.setFontSize(ScreenSetup.SETUP.getFontSize());
 	}
 
 	private void update(final boolean force)
 	{
 		if (clustering.getNumClusters() == 0)
 		{
 			setVisible(false);
 			return;
 		}
 
 		Highlighter h = viewControler.getHighlighter();
 		MoleculeProperty prop = null;
 		if (h instanceof MoleculePropertyHighlighter)
 			prop = ((MoleculePropertyHighlighter) h).getProperty();
 
 		int cIndex = clustering.getClusterActive().getSelected();
 		if (cIndex == -1)
 			cIndex = clustering.getClusterWatched().getSelected();
 		Cluster c = clustering.getCluster(cIndex);
 		if (clustering.getNumClusters() == 1)
 			c = null;
 
 		int mIndex[] = clustering.getModelActive().getSelectedIndices();
 		if (mIndex.length == 0)
 			mIndex = clustering.getModelWatched().getSelectedIndices();
 
 		List<Model> ms = new ArrayList<Model>();
 		for (int i : mIndex)
 			ms.add(clustering.getModelWithModelIndex(i));
 		if (prop != null && prop.getType() == Type.NOMINAL)
 		{
 			//does NOT help much in terms of visualisation (color code should be enough), difficult to realize in terms of color brightness
 			ms.clear();
 		}
 
 		System.err.println("update " + ms);
 
 		if (force || cluster != c || property != prop || !models.equals(ms))
 		{
 			cluster = c;
 			property = prop;
 			models = ms;
 
 			if (property == null)
 				setVisible(false);
 			else
 			{
 				final Cluster fCluster = this.cluster;
 				final MoleculeProperty fProperty = this.property;
 				final List<Model> fModels = this.models;
 
 				workerThread.addJob(new Runnable()
 				{
 					public void run()
 					{
 						if (fCluster != cluster || fProperty != property || fModels != models)
 							return;
 
 						System.out.println("updating chart");
 
 						String plotKey = getKey(fCluster, fProperty, fModels);
 						if (force && cardContents.contains(plotKey))
 							cardContents.remove(plotKey);
 						if (!cardContents.contains(plotKey))
 						{
 							System.out.println("create new plot");
 							MoleculeProperty.Type type = fProperty.getType();
 							PlotData d = null;
 							if (type == Type.NOMINAL)
 								d = new NominalPlotData(fCluster, fProperty, fModels);
 							else if (type == Type.NUMERIC)
 								d = new NumericPlotData(fCluster, fProperty, fModels);
 							if (d != null)
 							{
 								cardContents.add(plotKey);
 								cardPanel.add(d.getPlot(), plotKey);
 							}
 						}
 						else
 							System.out.println("plot was cached");
 
 						if (fCluster != cluster || fProperty != property || fModels != models)
 							return;
 						setIgnoreRepaint(true);
 
 						featureNameLabel.setText(fProperty.toString());
 						featureSetLabel.setText(fProperty.getMoleculePropertySet().toString());
 						//hack, ommits this for cdk features
 						featureSetLabel.setVisible(fProperty.getMoleculePropertySet().isSizeDynamic());
 						featureDescriptionLabel.setText(fProperty.getDescription() + "");
 						featureDescriptionLabel.setVisible(fProperty.getDescription() != null);
 						featureDescriptionLabelHeader.setVisible(fProperty.getDescription() != null);
 						featureSmartsLabel.setText(fProperty.getSmarts() + "");
 						featureSmartsLabel.setVisible(fProperty.getSmarts() != null);
 						featureSmartsLabelHeader.setVisible(fProperty.getSmarts() != null);
 						featureMappingLabel
 								.setText((fProperty.getMoleculePropertySet().isUsedForMapping() ? "Used for clustering and/or embedding."
 										: "NOT used for clustering and/or embedding."));
 						featureMissingLabel.setText(clustering.numMissingValues(fProperty) + "");
 
 						if (cardContents.contains(plotKey))
 						{
 							cardPanel.setVisible(true);
 							((CardLayout) cardPanel.getLayout()).show(cardPanel, plotKey);
 						}
 						else
 							cardPanel.setVisible(false);
 						revalidate();
 						setVisible(true);
 						setIgnoreRepaint(false);
 						repaint();
 					}
 				}, "update chart");
 			}
 		}
 	}
 
 	public Dimension getPreferredSize()
 	{
 		Dimension dim = super.getPreferredSize();
 		dim.width = Math.min(guiControler.getViewerWidth() / 3, dim.width);
 		dim.height = (int) (dim.width * 0.55);
 		return dim;
 	}
 }
