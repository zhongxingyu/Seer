 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.vecmath.Vector3f;
 
 import main.Settings;
 
 import org.jmol.adapter.smarter.SmarterJmolAdapter;
 import org.jmol.api.JmolAdapter;
 import org.jmol.api.JmolSimpleViewer;
 
 import util.ArrayUtil;
 import util.DoubleUtil;
 import util.StringUtil;
 import cluster.Cluster;
 import cluster.ClusterUtil;
 import cluster.Clustering;
 import cluster.Model;
 import data.ClusteringData;
 import dataInterface.MoleculeProperty;
 import dataInterface.SubstructureSmartsType;
 
 public class MainPanel extends JPanel implements ViewControler
 {
 	GUIControler guiControler;
 	JmolPanel jmolPanel;
 	View view;
 	private Clustering clustering;
 	private boolean spinEnabled = true;
 
 	private static final String DEFAULT_COLOR = "color cpk";
 
 	String style = STYLE_BALLS_AND_STICKS;
 	String color = DEFAULT_COLOR;
 
 	String modelInactiveSuffix = "; color translucent 0.9";
 	String modelActiveSuffix = "";
 
 	public static final Highlighter DEFAULT_HIGHLIGHTER = new SimpleHighlighter("Atom types");
 
 	HashMap<String, Highlighter[]> highlighters;
 	Highlighter selectedHighlighter = DEFAULT_HIGHLIGHTER;
 	boolean highlighterLabelsVisible = true;
 	HighlightSorting highlightSorting = HighlightSorting.Med;
 
 	public float zoomFactor = 1f;
 
 	public Clustering getClustering()
 	{
 		return clustering;
 	}
 
 	public boolean isSpinEnabled()
 	{
 		return spinEnabled;
 	}
 
 	public void setSpinEnabled(boolean spin)
 	{
 		setSpinEnabled(spin, false);
 	}
 
 	private void setSpinEnabled(boolean spinEnabled, boolean force)
 	{
 		if (this.spinEnabled != spinEnabled || force)
 		{
 			this.spinEnabled = spinEnabled;
 			view.setSpinEnabled(spinEnabled);
 		}
 	}
 
 	public MainPanel(GUIControler guiControler)
 	{
 		this.guiControler = guiControler;
 		jmolPanel = new JmolPanel();
 
 		setLayout(new BorderLayout());
 		add(jmolPanel);
 
 		clustering = new Clustering();
 		View.init(jmolPanel, guiControler);
 		view = View.instance;
 
 		// mouse listener to click atoms or clusters (zoom in)
 		jmolPanel.addMouseListener(new MouseAdapter()
 		{
 			public void mouseClicked(MouseEvent e)
 			{
 				if (!SwingUtilities.isLeftMouseButton(e))
 					return;
 				int atomIndex = view.sloppyFindNearestAtomIndex(e.getX(), e.getY());
 				if (atomIndex != -1)
 				{
 					if (clustering.getClusterActive().getSelected() == -1)
 					{
 						// no cluster active at the moment
 						// set a cluster to active (zooming will be done in listener)
 						clustering.getClusterActive().setSelected(
 								clustering.getClusterIndexForModelIndex(view.getAtomModelIndex(atomIndex)));
 						clustering.getClusterWatched().clearSelection();
 					}
 					else
 					{
 						// already a cluster active, zooming into model
 						//						final Model m = clustering.getModelWithModelIndex(view.getAtomModelIndex(atomIndex));
 						clustering.getModelWatched().clearSelection();
 						clustering.getModelActive().setSelected(view.getAtomModelIndex(atomIndex));
 					}
 				}
 				else
 					clustering.getClusterWatched().clearSelection();
 			}
 		});
 
 		jmolPanel.addMouseMotionListener(new MouseAdapter()
 		{
 
 			public void mouseMoved(MouseEvent e)
 			{
 				int atomIndex = view.findNearestAtomIndex(e.getX(), e.getY());
 
 				if (clustering.getClusterActive().getSelected() == -1)
 				{
 					if (atomIndex == -1)
 					{
 						// do not clear cluster selection
 						//clustering.getClusterWatched().clearSelection();
 					}
 					else
 						clustering.getClusterWatched().setSelected(
 								(clustering.getClusterIndexForModelIndex(view.getAtomModelIndex(atomIndex))));
 				}
 				else
 				{
 					if (atomIndex == -1)
 					{
 						// do not clear model selection
 						//clustering.getModelWatched().clearSelection();
 					}
 					else
 						clustering.getModelWatched().setSelected(view.getAtomModelIndex(atomIndex));
 				}
 			}
 		});
 	}
 
 	public String getStyle()
 	{
 		return style;
 	}
 
 	public void setStyle(String style)
 	{
 		setStyle(style, false);
 	}
 
 	private void setStyle(String style, boolean force)
 	{
 		if (!this.style.equals(style) || force)
 		{
 			this.style = style;
 			view.selectAll();
 			view.scriptWait(style);
 		}
 	}
 
 	@Override
 	public HashMap<String, Highlighter[]> getHighlighters()
 	{
 		return highlighters;
 	}
 
 	@Override
 	public Highlighter getHighlighter()
 	{
 		return selectedHighlighter;
 	}
 
 	@Override
 	public void setHighlighter(final Highlighter highlighter)
 	{
 		if (this.selectedHighlighter != highlighter)
 		{
 			selectedHighlighter = highlighter;
 			view.clearBfactorRange();
 
 			if (highlighter == DEFAULT_HIGHLIGHTER)
 				color = DEFAULT_COLOR;
 			else if (highlighter instanceof SubstructureHighlighter)
 				color = DEFAULT_COLOR;
 			else if (highlighter instanceof MoleculePropertyHighlighter)
 			{
 				color = "color temperature";
 				clustering.setTemperature(((MoleculePropertyHighlighter) highlighter).getProperty());
 			}
 
 			updateAllClusters();
 			updateAllModels();
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 		}
 	}
 
 	@Override
 	public boolean isHighlighterLabelsVisible()
 	{
 		return highlighterLabelsVisible;
 	}
 
 	@Override
 	public void setHighlighterLabelsVisible(boolean selected)
 	{
 		if (this.highlighterLabelsVisible != selected)
 		{
 			highlighterLabelsVisible = selected;
 			updateAllClusters();
 			updateAllModels();
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 		}
 	}
 
 	@Override
 	public void setHighlightSorting(HighlightSorting sorting)
 	{
 		if (this.highlightSorting != sorting)
 		{
 			highlightSorting = sorting;
 			updateAllClusters();
 			updateAllModels();
 			fireViewChange(PROPERTY_HIGHLIGHT_CHANGED);
 		}
 	}
 
 	@Override
 	public HighlightSorting getHighlightSorting()
 	{
 		return highlightSorting;
 	}
 
 	/**
 	 * udpates all clusters
 	 */
 	private void updateAllClusters()
 	{
 		for (int j = 0; j < clustering.numClusters(); j++)
 			updateCluster(j, true, -1);
 	}
 
 	/**
 	 * updates cluster with index i
 	 * update is done if i != old index or forceupdate is true
 	 * 
 	 * paints/removes box around cluster
 	 * hides models
 	 * 
 	 * @param i
 	 * @param forceUpdate
 	 * @param oldActiveIndex
 	 */
 	public void updateCluster(int i, boolean forceUpdate, int oldActiveIndex)
 	{
 		Cluster c = clustering.getCluster(i);
 		int active = clustering.getClusterActive().getSelected();
 		int watched = clustering.getClusterWatched().getSelected();
 
 		if (i != -1 && active == -1 && i == watched)
 		{
 			view.select(c.getBitSet(), false);
 			view.scriptWait("boundbox { selected } DOTTED");
 			//			String test = "osterhase";
 			//			viewer.scriptWait("label \"" + test + "\"");
 		}
 		else
 		{
 			view.scriptWait("boundbox OFF");
 		}
 
 		if (i != -1)
 		{
 			if (forceUpdate)
 			{
 				if (i != active)
 					hideSomeModels(c, true);
 				else
 					hideSomeModels(c, false);
 			}
 			else
 			{
 				if (i == active)
 					hideSomeModels(c, false);
 				if (i == oldActiveIndex)
 					hideSomeModels(c, true);
 			}
 		}
 	}
 
 	private void hideSomeModels(Cluster c, boolean hide)
 	{
 		view.select(c.getBitSet(), false);
 
 		if (hide)
 		{
 			int numModels = c.getModels().size();
 			int max;
 			if (numModels <= 10)
 				max = 10;
 			else
 				max = (numModels - 10) / 3 + 10;
 			max = Math.min(25, max);
 			//			System.err.println("hiding: ''" + hide + "'', num '" + numModels + "', max '" + max + "'");
 
 			if (numModels >= max)
 			{
 				List<Model> models;
 				if (selectedHighlighter instanceof MoleculePropertyHighlighter)
 					models = c.getModelsInOrder(((MoleculePropertyHighlighter) selectedHighlighter).getProperty(),
 							highlightSorting);
 				else
 					models = c.getModels();
 
 				int count = 0;
 				for (Model m : models)
 				{
 					boolean hideModel = count++ >= max;
 					if (m.isHidden() != hideModel)
 					{
 						BitSet bs = view.getModelUndeletedAtomsBitSet(m.getModelIndex());
 						view.select(bs, false);
 						if (hideModel)
 						{
 							//							System.err.println("hide: " + m.getModelIndex());
 							view.scriptWait("select selected OR hidden; hide selected");
 						}
 						else
 						{
 							//							System.err.println("show: " + m.getModelIndex());
 							view.scriptWait("select (not hidden) OR selected; select not selected; hide selected");
 						}
 						m.setHidden(hideModel);
 					}
 				}
 			}
 			//			System.err.println();
 		}
 		else
 		{
 			for (Model m : c.getModels())
 			{
 				if (m.isHidden())
 				{
 					BitSet bs = view.getModelUndeletedAtomsBitSet(m.getModelIndex());
 					view.select(bs, false);
 					view.scriptWait("select (not hidden) OR selected; select not selected; hide selected");
 					m.setHidden(false);
 				}
 			}
 		}
 	}
 
 	/**
 	 * updats all models
 	 */
 	private void updateAllModels()
 	{
 		for (Cluster c : clustering.getClusters())
 			for (Model m : c.getModels())
 				updateModel(m.getModelIndex(), true);
 	}
 
 	public void updateModel(int i)
 	{
 		updateModel(i, false);
 	}
 
 	/**
 	 * udpates single model
 	 * forceUpdate = true -> everything is reset (indipendent of model is part of active cluster or if single props habve changed)
 	 * 
 	 * shows/hides box around model
 	 * show/hides model label
 	 * set model translucent/opaque
 	 * highlight substructure in model 
 	 */
 	private void updateModel(int i, boolean forceUpdate)
 	{
 		int clus = clustering.getClusterIndexForModelIndex(i);
 		Cluster c = clustering.getCluster(clus);
 		Model m = clustering.getModelWithModelIndex(i);
 		if (m == null)
 		{
 			System.err.println("model is null!");
 			return;
 		}
 		int activeCluster = clustering.getClusterActive().getSelected();
 
 		boolean showBox = false;
 		boolean showLabel = false;
 		boolean translucent = true;
 		SubstructureSmartsType substructure = null;
 
 		if (forceUpdate || clus == activeCluster)
 		{
 			// inside the active cluster
 			if (clus == activeCluster)
 			{
 				translucent = false;
 				if (c.isSuperimposed() && !clustering.getModelWatched().isSelected(i)
 						&& !clustering.getModelActive().isSelected(i))
 					translucent = true;
 
 				if (clustering.getModelWatched().isSelected(i) || clustering.getModelActive().isSelected(i))
 				{
					if (selectedHighlighter instanceof MoleculePropertyHighlighter)
						showLabel = true;
 					if (clustering.getModelWatched().isSelected(i) && !c.isOverlap())
 						showBox = true;
 				}
 			}
 			else
 			{
 				List<Model> models;
 				if (selectedHighlighter instanceof MoleculePropertyHighlighter)
 					models = c.getModelsInOrder(((MoleculePropertyHighlighter) selectedHighlighter).getProperty(),
 							highlightSorting);
 				else
 					models = c.getModels();
 
 				if (selectedHighlighter instanceof MoleculePropertyHighlighter && models.indexOf(m) == 0)
 					showLabel = true;
 				translucent = (models.indexOf(m) > 0);
 			}
 
 			if (selectedHighlighter instanceof SubstructureHighlighter)
 				substructure = ((SubstructureHighlighter) selectedHighlighter).getType();
 		}
 		else
 		{
 			translucent = (c.getModels().indexOf(m) > 0);
 		}
 
 		if (!highlighterLabelsVisible)
 			showLabel = false;
 
 		BitSet bs = view.getModelUndeletedAtomsBitSet(i);
 		view.select(bs, false);
 
 		if (forceUpdate || translucent != m.isTranslucent())
 		{
 			m.setTranslucent(translucent);
 			if (translucent)
 				view.scriptWait(color + modelInactiveSuffix);
 			else
 				view.scriptWait(color + modelActiveSuffix);
 		}
 
 		if (forceUpdate || showBox != m.isShowBox())
 		{
 			m.setShowBox(showBox);
 			if (showBox)
 				view.scriptWait("boundbox { selected } DOTTED");
 			else
 				view.scriptWait("boundbox { selected } OFF");
 		}
 
 		// CHANGES SELECTION !!!
 		if (substructure != m.getSubstructureHighlighted())
 		{
 			boolean match = false;
 			String smarts = null;
 			if (substructure != null)
 				smarts = c.getSubstructureSmarts(substructure);
 			if (smarts != null && smarts.length() > 0)
 			{
 				BitSet matchBitSet = m.getSmartsMatch(smarts);
 				//compute match dynamically
 				if (matchBitSet == null)
 				{
 					System.out.println("smarts-matching: " + i + " " + substructure + " smarts: " + smarts
 							+ " smiles: " + m.getSmiles());
 					m.setSmartsMatch(smarts, view.getSmartsMatch(smarts, m.getBitSet()));
 					if (m.getSmartsMatch(smarts).cardinality() == 0)
 					{
 						System.out.flush();
 						System.err.println("could not match smarts!");
 						System.err.flush();
 					}
 					matchBitSet = m.getSmartsMatch(smarts);
 				}
 				if (matchBitSet.cardinality() > 0)
 				{
 					match = true;
 					view.select(matchBitSet, false);
 					if (m.isTranslucent())
 						view.scriptWait("color orange" + modelInactiveSuffix);
 					else
 						view.scriptWait("color orange" + modelActiveSuffix);
 				}
 			}
 			if (!match && !forceUpdate)
 			{
 				if (m.isTranslucent())
 					view.scriptWait(color + modelInactiveSuffix);
 				else
 					view.scriptWait(color + modelActiveSuffix);
 			}
 		}
 
 		// CHANGES SELECTION !!!
 		if (forceUpdate || showLabel != m.isShowLabel())
 		{
 			m.setShowLabel(showLabel);
 			if (showLabel)
 			{
 
 				BitSet empty = new BitSet(bs.length());
 				empty.set(bs.nextSetBit(0));
 				view.select(empty, false);
 				Object val = clustering.getModelWithModelIndex(i).getTemperature(
 						((MoleculePropertyHighlighter) selectedHighlighter).getProperty());
 				Double d = DoubleUtil.parseDouble(val + "");
 				if (d != null)
 					val = StringUtil.formatDouble(d);
 				//				System.err.println("label : " + i + " : " + c + " : " + val);
 				String l = ((MoleculePropertyHighlighter) selectedHighlighter).getProperty() + ": " + val;
 				view.scriptWait("label \"" + l + "\"");
 			}
 			else
 			{
 				//				System.err.println("label : " + i + " : " + c + " : off");
 				view.scriptWait("label OFF");
 			}
 		}
 	}
 
 	public void init(ClusteringData clusteredDataset)
 	{
 		clustering.newClustering(clusteredDataset);
 
 		clustering.getClusterActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterActive().getSelected(), cIndexOld, true);
 			}
 		});
 		clustering.getClusterWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int cIndexOldArray[] = ((int[]) e.getOldValue());
 				int cIndexOld = cIndexOldArray.length == 0 ? -1 : cIndexOldArray[0];
 				updateClusterSelection(clustering.getClusterWatched().getSelected(), cIndexOld, false);
 			}
 		});
 		clustering.getModelActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int mIndexOldArray[] = ((int[]) e.getOldValue());
 				int mIndexOld = mIndexOldArray.length == 0 ? -1 : mIndexOldArray[0];
 				updateModelSelection(clustering.getModelActive().getSelected(), mIndexOld, true);
 			}
 		});
 		clustering.getModelWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				int mIndexOldArray[] = ((int[]) e.getOldValue());
 				int mIndexOld = mIndexOldArray.length == 0 ? -1 : mIndexOldArray[0];
 				updateModelSelection(clustering.getModelWatched().getSelected(), mIndexOld, false);
 			}
 		});
 		clustering.addRemoveAddClusterListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (evt.getPropertyName().equals(Clustering.CLUSTER_ADDED))
 					updateClusteringNew();
 				else
 				{
 					updateClusterRemoved();
 					view.scriptWait("");
 				}
 			}
 		});
 
 		view.setBackground(Settings.BACKGROUND);
 		updateClusteringNew();
 	}
 
 	private void updateClusteringNew()
 	{
 		setStyle(getStyle(), true);
 
 		Highlighter[] h = new Highlighter[] { DEFAULT_HIGHLIGHTER };
 		if (clustering.getSubstructures().size() > 0)
 			for (SubstructureSmartsType type : clustering.getSubstructures())
 				h = ArrayUtil.concat(Highlighter.class, h, new Highlighter[] { new SubstructureHighlighter(type) });
 		highlighters = new LinkedHashMap<String, ViewControler.Highlighter[]>();
 		highlighters.put("", h);
 
 		List<MoleculeProperty> props = clustering.getFeatures();
 		MoleculePropertyHighlighter featureHighlighters[] = new MoleculePropertyHighlighter[props.size()];
 		int fCount = 0;
 		for (MoleculeProperty p : props)
 			featureHighlighters[fCount++] = new MoleculePropertyHighlighter(p);
 		highlighters.put("Properties used for clustering", featureHighlighters);
 
 		props = clustering.getProperties();
 		featureHighlighters = new MoleculePropertyHighlighter[props.size()];
 		fCount = 0;
 		for (MoleculeProperty p : props)
 			featureHighlighters[fCount++] = new MoleculePropertyHighlighter(p);
 		highlighters.put("Additional compound properties", featureHighlighters);
 
 		fireViewChange(PROPERTY_NEW_HIGHLIGHTERS);
 
 		updateAllClusters();
 		updateAllModels();
 
 		view.evalString("frame " + view.getModelNumberDotted(0) + " "
 				+ view.getModelNumberDotted(clustering.numModels() - 1));
 
 		view.zoomOut(clustering.getCenter(), 0, zoomFactor, clustering.getRadius());//, clustering.getBitSetAll());
 		setSpinEnabled(isSpinEnabled(), true);
 
 		if (clustering.getNumClusters() == 1)
 		{
 			clustering.getClusterActive().setSelected(0);
 		}
 	}
 
 	private void updateClusterRemoved()
 	{
 		setHighlighter(DEFAULT_HIGHLIGHTER);
 	}
 
 	private void updateModelSelection(int mIndex, int mIndexOld, boolean active)
 	{
 		//		System.out.println("update model active: " + active + " " + ArrayUtil.toString(mIndex));
 		int activeCluster = clustering.getClusterActive().getSelected();
 		if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(mIndexOld))
 			updateModel(mIndexOld);
 		if (activeCluster == -1 || activeCluster == clustering.getClusterIndexForModelIndex(mIndex))
 			updateModel(mIndex);
 
 		if (active)
 		{
 			if (mIndex != -1 && mIndex != mIndexOld)
 			{
 				final Model m = clustering.getModelWithModelIndex(mIndex);
 				view.zoomIn(new Vector3f(view.getAtomSetCenter(m.getBitSet())));
 			}
 			else if (mIndex == -1 && mIndexOld != -1)
 			{
 				final Cluster c = getClustering().getClusterForModelIndex(mIndexOld);
 				view.zoomOut(c.getNonOverlapCenter(), 0.5f, zoomFactor, c.getRadius());
 			}
 		}
 	}
 
 	/**
 	 * the cluster selection has changed
 	 * active=true -> the selection change is about active/inactive
 	 * active=false -> the selection change is about watched/not-watched
 	 * 
 	 * @param cIndex
 	 * @param cIndexOld
 	 * @param active
 	 */
 	private void updateClusterSelection(final int cIndex, int cIndexOld, boolean active)
 	{
 		// ignore watch updates when a cluster is selected
 		if (!active && clustering.getClusterActive().getSelected() != -1)
 			return;
 
 		System.out.println("updating cluster selection: " + cIndex + " " + cIndexOld + " " + active);
 
 		// draw boxes, hide models...
 		if (active)
 		{
 			updateCluster(cIndexOld, false, cIndexOld);
 			updateCluster(cIndex, false, cIndexOld);
 		}
 		else
 		{
 			if (cIndexOld != -1)
 				updateCluster(cIndexOld, false, -1);
 			if (cIndex != -1)
 				updateCluster(cIndex, false, -1);
 		}
 
 		// zooming
 		if (active)
 		{
 			if (cIndex != -1)
 			{
 				// zoom into cluster
 				final Cluster oldC = clustering.getCluster(cIndexOld);
 				final Cluster c = clustering.getCluster(cIndex);
 				view.selectModel(0, clustering.numModels() - 1);
 				int firstModel = c.getModel(0).getModelIndex();
 				int lastModel = c.getModel(c.size() - 1).getModelIndex();
 				view.evalString("frame " + view.getModelNumberDotted(firstModel) + " "
 						+ view.getModelNumberDotted(lastModel));
 
 				if (oldC != null)
 				{
 					oldC.setOverlap(true, View.MoveAnimation.NONE, false);
 					for (Model m : oldC.getModels())
 						updateModel(m.getModelIndex());
 				}
 
 				for (Model m : c.getModels())
 					updateModel(m.getModelIndex());
 
 				//				System.out.println(c.getRadius());
 				if (c.getModels().size() > 1)
 					view.zoomOut(c.getNonOverlapCenter(), 0.5f, zoomFactor, c.getRadius());//, c.getBitSet());
 				else
 				{
 					view.zoomIn(c.getCenter());
 				}
 				c.setOverlap(false, View.MoveAnimation.SLOW, false);
 			}
 			else
 			{
 				// zoom to home
 				final Cluster c = clustering.getCluster(cIndexOld);
 
 				for (Model m : c.getModels())
 					updateModel(m.getModelIndex(), true);
 
 				c.setOverlap(true, View.MoveAnimation.FAST, false);
 				//viewerUtils.zoomOut(true, new Vector3f(0, 0, 0), 0.5);
 				view.zoomOut(clustering.getCenter(), 0.5f, zoomFactor, clustering.getRadius()); //, clustering.getBitSetAll());
 
 				view.afterAnimation(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						view.selectModel(0, clustering.numModels() - 1);
 					}
 				}, "after zoom out - select all");
 			}
 		}
 	}
 
 	static class JmolPanel extends JPanel
 	{
 		JmolSimpleViewer viewer;
 		JmolAdapter adapter;
 
 		JmolPanel()
 		{
 			adapter = new SmarterJmolAdapter();
 			viewer = JmolSimpleViewer.allocateSimpleViewer(this, adapter);
 		}
 
 		public JmolSimpleViewer getViewer()
 		{
 			return viewer;
 		}
 
 		final Dimension currentSize = new Dimension();
 		final Rectangle rectClip = new Rectangle();
 
 		public void paint(Graphics g)
 		{
 			getSize(currentSize);
 			g.getClipBounds(rectClip);
 			viewer.renderScreenImage(g, currentSize, rectClip);
 		}
 	}
 
 	@Override
 	public void setSuperimpose(boolean superimpose)
 	{
 		int cIndex = clustering.getClusterActive().getSelected();
 		if (cIndex == -1)
 			return;
 		final Cluster c = clustering.getCluster(cIndex);
 		if (c.isOverlap() == superimpose)
 			return;
 
 		clustering.getModelWatched().clearSelection();
 
 		if (c.isOverlap())
 		{
 			view.zoomOut(c.getNonOverlapCenter(), 0.5f, zoomFactor, c.getRadius()); //, c.getBitSet());
 			c.setOverlap(false, View.MoveAnimation.SLOW, false);
 		}
 		else
 		{
 			c.setOverlap(true, View.MoveAnimation.SLOW, true);
 			// the center is changed in the animation superimposition
 			view.afterAnimation(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					view.zoomIn(c.getCenter());
 				}
 			}, "zoom to new(!) center");
 		}
 
 		view.afterAnimation(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				for (Model m : c.getModels())
 					updateModel(m.getModelIndex(), false);
 			}
 		}, "set models translucent");
 
 	}
 
 	List<PropertyChangeListener> viewListeners = new ArrayList<PropertyChangeListener>();
 
 	@Override
 	public void addViewListener(PropertyChangeListener l)
 	{
 		viewListeners.add(l);
 	}
 
 	private void fireViewChange(String prop)
 	{
 		for (PropertyChangeListener l : viewListeners)
 			l.propertyChange(new PropertyChangeEvent(this, prop, "old", "new"));
 	}
 
 	@Override
 	public boolean isMaxDensitiy()
 	{
 		return ClusterUtil.DENSITY <= 0.1f;
 	}
 
 	@Override
 	public void setDensitiyHigher(boolean higher)
 	{
 		if (higher)
 		{
 			if (!isMaxDensitiy())
 			{
 				ClusterUtil.DENSITY = ClusterUtil.DENSITY - 0.05f;
 				clustering.updatePositions();
 				fireViewChange(PROPERTY_DENSITY_CHANGED);
 			}
 		}
 		else
 		{
 			ClusterUtil.DENSITY = ClusterUtil.DENSITY + 0.05f;
 			clustering.updatePositions();
 			fireViewChange(PROPERTY_DENSITY_CHANGED);
 		}
 	}
 
 }
