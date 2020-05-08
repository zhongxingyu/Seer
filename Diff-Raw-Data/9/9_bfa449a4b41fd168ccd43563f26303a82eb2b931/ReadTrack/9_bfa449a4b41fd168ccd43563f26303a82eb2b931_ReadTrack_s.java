 /*--------------------------------------------------------------------------
  *  Copyright 2009 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-core Project
 //
 // ReadTrack.java
 // Since: May 16, 2010
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.client.track.lib;
 
 import java.util.List;
 
 import org.utgenome.gwt.utgb.client.UTGBEntryPointBase;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataVisitorBase;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB.DBType;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig.Layout;
 import org.utgenome.gwt.utgb.client.canvas.GWTGenomeCanvas;
 import org.utgenome.gwt.utgb.client.canvas.LocusClickHandler;
 import org.utgenome.gwt.utgb.client.db.ValueDomain;
 import org.utgenome.gwt.utgb.client.db.datatype.StringType;
 import org.utgenome.gwt.utgb.client.track.Track;
 import org.utgenome.gwt.utgb.client.track.TrackBase;
 import org.utgenome.gwt.utgb.client.track.TrackConfig;
 import org.utgenome.gwt.utgb.client.track.TrackConfigChange;
 import org.utgenome.gwt.utgb.client.track.TrackFrame;
 import org.utgenome.gwt.utgb.client.track.TrackGroup;
 import org.utgenome.gwt.utgb.client.track.TrackGroupPropertyChange;
 import org.utgenome.gwt.utgb.client.track.TrackWindow;
 import org.utgenome.gwt.utgb.client.track.UTGBProperty;
 import org.utgenome.gwt.utgb.client.util.BrowserInfo;
 import org.utgenome.gwt.utgb.client.util.CanonicalProperties;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * Track for displaying read data.
  * 
  * <h3>View Example</h3>
  * 
  * <pre>
  * -track
  *  -class: ReadTrack
  *  -name: (track name)
  *  -properties
  *    -path: (database path. e.g, db/imported/hg19/myread.bed)
  *    -onclick.url: http://www.google.com/search?q=%q
  *    -onclick.action: (none|link|info|set)
  *    -showLabels: (true|false)
  *    -onclick.p.key: current.read
  *    -onclick.p.value: %q
  * </pre>
  * 
  * <p>
  * You can customize the behavior of the browser when clicking a read using <i>onclick.action</i> parameter:
  * </p>
  * 
  * <h3>On-Click Action Types</h3>
  * <table>
  * <tr>
  * <th>Type</th>
  * <th>Action</th>
  * </tr>
  * <tr>
  * <td>none</td>
  * <td>Disables click action</td>
  * </tr>
  * <tr>
  * <td>link</td>
  * <td>Opens an URL specified in <i>onclick.url</i> parameter.</td>
  * </tr>
  * <tr>
  * <td>info</td>
  * <td>Displays the detailed read information.</td>
  * </tr>
  * <tr>
  * <td>set</td>
  * <td>Sets a track group property (specified by <i>onclick.p.key</i>) using the value <i>onclick.p.value</i>.</td>
  * </tr>
  * </table>
  * 
  * <p>
  * You can embed track or track group parameters in <i>onclick.url</i> and <i>target.value</i>:
  * </p>
  * 
  * <h3>Parameters for On-Click event</h3>
  * <table>
  * <tr>
  * <th>pattern</th>
  * <th>to be replaced with</th>
  * </tr>
  * <tr>
  * <td>%q</td>
  * <td>Clicked read name</td>
  * </tr>
  * <tr>
  * <td>%qstart</td>
  * <td>Start position of the clicked read</td>
  * </tr>
  * <tr>
  * <td>%qend</td>
  * <td>End position of the clicked read</td>
  * </tr>
  * <tr>
  * <td>%qlen</td>
  * <td>Length of the clicked read</td>
  * </tr>
  * <tr>
  * <td>%species</td>
  * <td>Species name</td>
  * </tr>
  * <tr>
  * <td>%ref</td>
  * <td>Reference sequence name</td>
  * </tr>
  * <tr>
  * <td>%chr</td>
  * <td>Chromosome/contig/scaffold name</td>
  * </tr>
  * <tr>
  * <td>%start</td>
  * <td>Start position on the genome (inclusive)</td>
  * </tr>
  * <tr>
  * <td>%end</td>
  * <td>End position on the genome (exclusive)</td>
  * </tr>
  * <tr>
  * <td>%len</td>
  * <td>Sequence length currently displayed</td>
  * </tr>
  * <tr>
  * <td>%pixelwidth</td>
  * <td>Pixel width of the tracks</td>
  * </tr>
  * 
  * 
  * </table>
  * 
  * @author leo
  * 
  */
 public class ReadTrack extends TrackBase {
 
 	// track configuration parameters
 	private final String CONFIG_LEFT_MARGIN = "leftMargin";
 	private final String CONFIG_DB_TYPE = "dbType";
 	private final String CONFIG_PATH = "path";
 	private final String CONFIG_LAYOUT = "layout";
 	private final String CONFIG_SHOW_LABELS = "showLabels";
 	private final String CONFIG_READ_HEIGHT = "read height";
 	private final String CONFIG_MIN_READ_HEIGHT = "min read height";
 	private final String CONFIG_COVERAGE_STYLE = "coverage.style";
 	private final String CONFIG_ONCLICK_ACTION = "onclick.action";
 	private final String CONFIG_ONCLICK_URL = "onclick.url";
 	private final String CONFIG_ONCLICK_P_KEY = "onclick.p.key";
 	private final String CONFIG_ONCLICK_P_VALUE = "onclick.p.value";
 
 	// widgets
 	private FlexTable layoutTable = new FlexTable();
 	private GWTGenomeCanvas geneCanvas = new GWTGenomeCanvas();
 
 	public static TrackFactory factory() {
 		return new TrackFactory() {
 			@Override
 			public Track newInstance() {
 				return new ReadTrack();
 			}
 		};
 	}
 
 	public ReadTrack() {
 		this("Read Track", "AUTO");
 
 	}
 
 	public ReadTrack(String trackName, String dbType) {
 		super("Read Track");
 
 		getConfig().setParameter(CONFIG_DB_TYPE, dbType);
 
 		layoutTable.setBorderWidth(0);
 		layoutTable.setCellPadding(0);
 		layoutTable.setCellSpacing(0);
 		layoutTable.setWidget(0, 1, geneCanvas);
 
 		updateClickAction();
 	}
 
 	public String resolveURL(String urlTemplate, OnGenome locus) {
 		String url = urlTemplate;
 		if (url == null)
 			return url;
 
 		if (locus != null) {
 			if (locus.getName() != null) {
 				if (url.contains("%q"))
 					url = url.replaceAll("%q", locus.getName());
 				if (url.contains("%qname"))
 					url = url.replaceAll("%qname", locus.getName());
 			}
 
 			if (url.contains("%qstart"))
 				url = url.replaceAll("%qstart", Integer.toString(locus.getStart()));
 			if (url.contains("%qend"))
 				url = url.replaceAll("%qend", Integer.toString(locus.getEnd()));
 			if (url.contains("%qlen"))
 				url = url.replaceAll("%qlen", Integer.toString(locus.length()));
 
 		}
 
 		// replace track group properties
 		return resolvePropertyValues(url);
 	}
 
 	private void updateClickAction() {
 
 		String clickAction = getConfig().getParameter(CONFIG_ONCLICK_ACTION);
 		if (clickAction == null)
 			return;
 		if ("none".equals(clickAction)) {
 			geneCanvas.setLocusClickHandler(null);
 		}
 		else if ("link".equals(clickAction)) {
 			geneCanvas.setLocusClickHandler(new LocusClickHandler() {
 				public void onClick(int x, int y, OnGenome locus) {
 					String url = getConfig().getParameter(CONFIG_ONCLICK_URL);
 					url = resolveURL(url, locus);
 					Window.open(url, "locus", "");
 				}
 			});
 		}
 		else if ("info".equals(clickAction)) {
 			geneCanvas.setLocusClickHandler(new LocusClickHandler() {
 				public void onClick(int x, int y, OnGenome locus) {
 					geneCanvas.displayInfo(x, y, locus);
 				}
 			});
 		}
 		else if ("set".equals(clickAction)) {
 			geneCanvas.setLocusClickHandler(new LocusClickHandler() {
 				public void onClick(int clientX, int clientY, OnGenome locus) {
 					String key = getConfig().getParameter(CONFIG_ONCLICK_P_KEY);
 					if (key == null)
 						return;
 					String value = getConfig().getParameter(CONFIG_ONCLICK_P_VALUE);
 					value = resolveURL(value, locus);
 					if (value == null)
 						return;
 
 					setTrackGroupProperty(key, value);
 				}
 			});
 		}
 
 	}
 
 	public Widget getWidget() {
 		return layoutTable;
 	}
 
 	@Override
 	public void draw() {
 
 		// set up drawing options
 		geneCanvas.setShowLabels(getConfig().getBoolean(CONFIG_SHOW_LABELS, true));
 		geneCanvas.setReadHeight(getConfig().getInt(CONFIG_READ_HEIGHT, 12));
 		geneCanvas.setReadHeightMin(getConfig().getInt(CONFIG_MIN_READ_HEIGHT, 2));
 		geneCanvas.setCoverageStyle(getConfig().getString(CONFIG_COVERAGE_STYLE, "default"));
 
 		geneCanvas.draw();
 		getFrame().loadingDone();
 
 	}
 
 	public static int calcXPositionOnWindow(long indexOnGenome, long startIndexOnGenome, long endIndexOnGenome, int windowWidth) {
 		double v = (indexOnGenome - startIndexOnGenome) * (double) windowWidth;
 		double v2 = v / (endIndexOnGenome - startIndexOnGenome);
 		return (int) v2;
 	}
 
 	@Override
 	public void onChangeTrackWindow(TrackWindow newWindow) {
 
 		update(newWindow, false);
 	}
 
 	@Override
 	public void onChangeTrackGroupProperty(TrackGroupPropertyChange change) {
 
 		if (change.containsOneOf(new String[] { UTGBProperty.SPECIES, UTGBProperty.REVISION, UTGBProperty.TARGET })) {
 			geneCanvas.clear();
 			update(change.getTrackWindow(), false);
 		}
 	}
 
 	@Override
 	public void setUp(TrackFrame trackFrame, TrackGroup group) {
 
 		geneCanvas.setTrackGroup(group);
 
 		update(group.getTrackWindow(), true);
 		TrackConfig config = getConfig();
 		config.addHiddenConfig(CONFIG_LEFT_MARGIN, "0");
 		config.addConfig("DB Path", new StringType(CONFIG_PATH));
 
 		config.addHiddenConfig(CONFIG_DB_TYPE, "AUTO");
 		//ValueDomain dbTypes = ValueDomain.createNewValueDomain(DBType.getDBTypeList());
 		//config.addConfigParameter("DB Type", new StringType(CONFIG_DB_TYPE, dbTypes), "AUTO");
 
 		ValueDomain layoutTypes = ValueDomain.createNewValueDomain(new String[] { "pileup", "coverage" });
 		config.addConfig("Layout", new StringType(CONFIG_LAYOUT, layoutTypes), "pileup");
 		config.addConfigBoolean("Show Labels", CONFIG_SHOW_LABELS, true);
 		config.addConfigInteger("Read Height", CONFIG_READ_HEIGHT, 12);
 		config.addConfigInteger("Read Height (min)", CONFIG_MIN_READ_HEIGHT, 2);
 
 		config.addConfig("Coverage Display Style",
 				new StringType(CONFIG_COVERAGE_STYLE, ValueDomain.createNewValueDomain(new String[] { "default", "smooth" })), "default");
 		ValueDomain actionTypes = ValueDomain.createNewValueDomain(new String[] { "none", "link", "info", "set" });
 		config.addConfig("On Click Action", new StringType(CONFIG_ONCLICK_ACTION, actionTypes), "link");
 		config.addConfig("On Click URL", new StringType(CONFIG_ONCLICK_URL), "http://www.google.com/search?q=%q");
 		config.addConfig("On Click - Set Key", new StringType(CONFIG_ONCLICK_P_KEY), "read");
 		config.addConfig("On Click - Set Value", new StringType(CONFIG_ONCLICK_P_VALUE), "%q");
 
 		updateClickAction();
 	}
 
 	private boolean needUpdateForGraphicRefinement = false;
 
 	protected void update(TrackWindow newWindow, boolean forceReload) {
 
 		if (!forceReload && geneCanvas.hasCacheCovering(newWindow)) {
 			if (!needUpdateForGraphicRefinement) {
 				geneCanvas.setTrackWindow(newWindow, false);
 				refresh();
 				return;
 			}
 		}
 
 		geneCanvas.setTrackWindow(newWindow, true);
 
 		// retrieve gene data from the API
 		TrackWindow prefetchWindow = geneCanvas.getPrefetchWindow();
 		String chr = getTrackGroupProperty(UTGBProperty.TARGET);
 
 		final String layout = getConfig().getString(CONFIG_LAYOUT, "pileup");
 		ReadQueryConfig queryConfig = new ReadQueryConfig(prefetchWindow.getPixelWidth(), BrowserInfo.isCanvasSupported(), Layout.valueOf(Layout.class, layout
 				.toUpperCase()));
 
 		getFrame().setNowLoading();
 		getBrowserService().getOnGenomeData(getGenomeDB(), new ChrLoc(chr, prefetchWindow.getStartOnGenome(), prefetchWindow.getEndOnGenome()), queryConfig,
 				new AsyncCallback<List<OnGenome>>() {
 
 					public void onFailure(Throwable e) {
 						GWT.log("failed to retrieve gene data", e);
 						UTGBEntryPointBase.showErrorMessage("read data retrieval failed: " + e.getMessage());
 						needUpdateForGraphicRefinement = true;
 						getFrame().loadingDone();
 					}
 
 					public void onSuccess(List<OnGenome> dataSet) {
 
 						if ("pileup".equals(layout) && dataSet.size() > 0 && DataChecker.isReadCoverage(dataSet.get(0))) {
 							needUpdateForGraphicRefinement = true;
 							// narrow down the prefetch range
 							float prefetchFactor = geneCanvas.getPrefetchFactor();
 							prefetchFactor /= 2.0;
 							geneCanvas.setPrefetchFactor(prefetchFactor);
 						}
 						else {
 							float newPrefetchFactor = geneCanvas.getPrefetchFactor() * 2.0f;
							if (newPrefetchFactor > 1.0)
 								newPrefetchFactor = 1.0f;
 
 							// broaden the prefetch range upon successful read data retrieval
 							geneCanvas.setPrefetchFactor(newPrefetchFactor);
 							needUpdateForGraphicRefinement = false;
 						}
 						geneCanvas.resetData(dataSet);
 						refresh();
 					}
 
 				});
 
 	}
 
 	private static class DataChecker extends OnGenomeDataVisitorBase {
 		public boolean flag = false;
 
 		public static boolean isReadCoverage(OnGenome data) {
 			DataChecker dataChecker = new DataChecker();
 			data.accept(dataChecker);
 			return dataChecker.flag;
 		}
 
 		@Override
 		public void visitReadCoverage(ReadCoverage readCoverage) {
 			flag = true;
 		}
 	}
 
 	public String getPath() {
 		String path = getConfig().getParameter(CONFIG_PATH);
 		return resolvePropertyValues(path);
 	}
 
 	/**
 	 * Override this method to extend db info parameters
 	 * 
 	 * @return
 	 */
 	public GenomeDB getGenomeDB() {
 		String ref = getTrackGroupProperty(UTGBProperty.REVISION);
 		String dbType = getConfig().getString("dbType", "AUTO");
 		return new GenomeDB(DBType.valueOf(DBType.class, dbType), getPath(), ref);
 	}
 
 	@Override
 	public void onChangeTrackConfig(TrackConfigChange change) {
 
 		if (change.containsOneOf(new String[] { CONFIG_ONCLICK_ACTION, CONFIG_ONCLICK_URL })) {
 			updateClickAction();
 		}
 
 		if (change.containsOneOf(new String[] { CONFIG_SHOW_LABELS, CONFIG_LEFT_MARGIN, CONFIG_PATH, CONFIG_DB_TYPE })) {
 			refresh();
 		}
 
 		if (change.containsOneOf(new String[] { CONFIG_LAYOUT, CONFIG_READ_HEIGHT, CONFIG_MIN_READ_HEIGHT, CONFIG_COVERAGE_STYLE })) {
 			update(getTrackWindow(), true);
 		}
 	}
 
 	@Override
 	public void restoreProperties(CanonicalProperties properties) {
 		super.restoreProperties(properties);
 		updateClickAction();
 	}
 
 }
