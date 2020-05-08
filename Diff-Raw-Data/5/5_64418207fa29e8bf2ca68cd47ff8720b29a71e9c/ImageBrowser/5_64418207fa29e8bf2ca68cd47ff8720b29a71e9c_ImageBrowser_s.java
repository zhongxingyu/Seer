 /*
  * Copyright 2011 Institut Pasteur.
  * 
  * This file is part of Image Browser, which is an ICY plugin.
  * 
  * Image Browser is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Image Browser is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Image Browser. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package plugins.nherve.browser;
 
 import icy.gui.util.GuiUtil;
 import icy.preferences.XMLPreferences;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import plugins.nherve.browser.cache.CacheException;
 import plugins.nherve.browser.viewer.ImageViewer;
 import plugins.nherve.toolbox.NherveToolbox;
 import plugins.nherve.toolbox.genericgrid.GridCellCollection;
 import plugins.nherve.toolbox.genericgrid.GridPanel;
 import plugins.nherve.toolbox.plugin.HelpWindow;
 import plugins.nherve.toolbox.plugin.PluginHelper;
 import plugins.nherve.toolbox.plugin.SingletonPlugin;
 
 public class ImageBrowser extends SingletonPlugin implements ActionListener, DocumentListener {
 	private class InternalFileFilter implements FileFilter {
 		private boolean recurse;
 
 		public InternalFileFilter(boolean recurse) {
 			super();
 			this.recurse = recurse;
 		}
 		
 		@Override
 		public boolean accept(File f) {
 			return (f.isDirectory() && recurse) || provider.isAbleToProvideThumbnailFor(f);
 		}
 	}
 
 	private final static String INPUT_PREFERENCES_NODE = "directory";
 	private final static String ZOOM = "zoom";
 	private final static String CACHE = "cache";
 	
	private static String HELP = "<html>" + "<p align=\"center\"><b>" + HelpWindow.TAG_FULL_PLUGIN_NAME + "</b></p>" + "<p align=\"center\"><b>" + NherveToolbox.DEV_NAME_HTML + "</b></p>" + "<p align=\"center\"><a href=\"http://www.herve.name/pmwiki.php/Main/ImageBrowser\">Online help is available</a></p>" + "<p align=\"center\"><b>" + NherveToolbox.COPYRIGHT_HTML + "</b></p>" 
 	+ "<hr/>"
 	+ "<p>On any thumbnail displayed, you can either : "
 	+"<ul><li>left click : open the image in Icy</li><li>right click : open the image viewer than allows you to navigate quickly between the directory images with the mouse scroll</li></ul>"
 	+"</p>"
 	+ "<hr/>"
	+ "<p>" + HelpWindow.TAG_PLUGIN_NAME + NherveToolbox.LICENCE_HTML + "</p>" + "<p>" + NherveToolbox.LICENCE_HTMLLINK + "</p>" + "</html>";
 
 	
 	public final static String NAME_INPUT_DIR = "Browse";
 	
 	private JTextField tfInputDir;
 
 	private JButton btInputDir;
 	private JButton btRefresh;
 	private JButton btHelp;
 	
 	private JCheckBox cbUseCache;
 	private JCheckBox cbRecurse;
 	private JButton btClearCache;
 	private JLabel lbCache;
 	
 	private GridPanel<BrowsedImage> igp;
 	private GridCellCollection<BrowsedImage> images;
 	private File workingDirectory;
 
 	private CacheThumbnailProvider provider;
 	
 	public ImageBrowser() {
 		super();
 		provider = new CombinedThumbnailProvider(true, GridPanel.DEFAULT_CELL_LENGTH * (int)GridPanel.DEFAULT_MAX_ZOOM_FACTOR);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object o = e.getSource();
 
 		if (o == null) {
 			return;
 		}
 
 		if (o instanceof JButton) {
 			JButton b = (JButton) o;
 
 			if (b == btInputDir) {
 				PluginHelper.fileChooserTF(JFileChooser.DIRECTORIES_ONLY, null, getPreferences().node(INPUT_PREFERENCES_NODE), "Choose directory to browse", tfInputDir, null);
 				return;
 			}
 			
 			if (b == btRefresh) {
 				updateDirectoryView();
 				return;
 			}
 			
 			if (b == btHelp) {
 				openHelpWindow(HELP, 400, 500);
 				return;
 			}
 			
 			if (b == btClearCache) {
 				try {
 					provider.clearCache();
 					lbCache.setText(provider.getCacheSizeInfo());
 				} catch (CacheException e1) {
 					e1.printStackTrace();
 				}
 				return;
 			}
 		}
 
 	}
 
 	@Override
 	protected void beforeDisplayInterface(JPanel mainPanel) {
 		super.beforeDisplayInterface(mainPanel);
 		
 		tfInputDir.getDocument().addDocumentListener(this);
 		updateDirectoryView();
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 		updateDirectoryView();
 	}
 
 	@Override
 	public void fillInterface(JPanel mainPanel) {
 		XMLPreferences preferences = getPreferences();
 
 		boolean useZoom = preferences.getBoolean(ZOOM, false);
 		boolean useCache = preferences.getBoolean(CACHE, true);
 		boolean recursive = false;
 		
 		btRefresh = new JButton("Refresh");
 		btRefresh.addActionListener(this);
 		
 		btInputDir = new JButton(NAME_INPUT_DIR);
 		btInputDir.addActionListener(this);
 		
 		btClearCache = new JButton("Clear cache");
 		btClearCache.addActionListener(this);
 		
 		lbCache = new JLabel(provider.getCacheSizeInfo());
 		
 		cbUseCache = new JCheckBox("Use cache");
 		cbUseCache.setSelected(useCache);
 		
 		cbRecurse = new JCheckBox("Recursive");
 		cbRecurse.setSelected(recursive);
 
 		Dimension maxDim = new Dimension(65000, 25);
 		Dimension minDim = new Dimension(75, 25);
 		tfInputDir = new JTextField();
 		tfInputDir.setPreferredSize(maxDim);
 		tfInputDir.setMaximumSize(maxDim);
 		tfInputDir.setMinimumSize(minDim);
 		tfInputDir.setName(NAME_INPUT_DIR);
 		String ifp = preferences.node(INPUT_PREFERENCES_NODE).get(PluginHelper.PATH, "");
 		tfInputDir.setText(ifp);
 		
 		btHelp = new JButton(NherveToolbox.questionIcon);
 		btHelp.addActionListener(this);
 		
 		mainPanel.add(GuiUtil.createLineBoxPanel(cbUseCache, lbCache, btClearCache, Box.createHorizontalGlue(), btRefresh, Box.createHorizontalGlue(), btInputDir, tfInputDir, cbRecurse, Box.createHorizontalGlue(), btHelp));
 
 		igp = new GridPanel<BrowsedImage>(useZoom);
 		mainPanel.add(igp);
 	}
 
 	@Override
 	public Dimension getDefaultFrameDimension() {
 		return new Dimension(400, 400);
 	}
 
 	private List<File> getFiles(File root, boolean recurse) {
 		File[] files = root.listFiles(new InternalFileFilter(recurse));
 		ArrayList<File> result = new ArrayList<File>();
 		
 		for (File f : files) {
 			if (recurse && f.isDirectory()) {
 				result.addAll(getFiles(f, recurse));
 			} else {
 				result.add(f);
 			}
 		}
 		
 		return result;
 	}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		updateDirectoryView();
 	}
 	
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		updateDirectoryView();
 	}
 	
 	@Override
 	public void sequenceHasChanged() {
 	}
 
 	@Override
 	public void sequenceWillChange() {
 	}
 
 	public void showViewer(BrowsedImage startingWith) {
 		ImageViewer v = new ImageViewer(images, provider);
 		v.startInterface(getFrame(), startingWith);
 	}
 	
 	@Override
 	public void stopInterface() {
 		XMLPreferences preferences = getPreferences();
 		preferences.putBoolean(ZOOM, igp.isZoomOnFocus());
 		preferences.putBoolean(CACHE, cbUseCache.isSelected());
 		
 		igp.setCells(null);
 		igp = null;
 		provider.close();
 	}
 
 	
 	private void updateDirectoryView() {
 		provider.setUseCache(cbUseCache.isSelected());
 		
 		if (cbUseCache.isSelected()) {
 			lbCache.setText(provider.getCacheSizeInfo());
 		}
 		
 		XMLPreferences preferences = getPreferences();
 		
 		preferences.putBoolean(ZOOM, igp.isZoomOnFocus());
 		preferences.putBoolean(CACHE, cbUseCache.isSelected());
 		
 		workingDirectory = new File(tfInputDir.getText());
 		if (workingDirectory.exists() && workingDirectory.isDirectory()) {
 			tfInputDir.setBackground(Color.GREEN);
 			preferences.node(INPUT_PREFERENCES_NODE).put(PluginHelper.PATH, workingDirectory.getAbsolutePath());
 			List<File> files = getFiles(workingDirectory, cbRecurse.isSelected());
 			if (files.size() > 0) {
 				images = new GridCellCollection<BrowsedImage>(provider);
 				Collections.sort(files);
 				for (File f : files) {
 					BrowsedImage ig = new BrowsedImage(f, workingDirectory, this);
 					images.add(ig);
 				}
 			} else {
 				images = null;
 			}
 		} else {
 			tfInputDir.setBackground(Color.RED);
 			images = null;
 		}
 		
 		igp.setCells(images);
 	}
 
 
 }
