 package filmeUtils.gui.gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.net.URL;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.apache.commons.cli.Options;
 
 import filmeUtils.commons.OutputListener;
 
 @SuppressWarnings("serial")
 public class SearchScreen extends JFrame {
 	
 	private JTextField searchString;
 	private final DefaultListModel defaultListModel;
 	private JPanel upperPanel;
 	private JPanel searchPanel;
 	private GridBagLayout gbl_searchPanel;
 	private GridBagConstraints gbc_searchString;
 	private JButton searchButton;
 	private GridBagConstraints gbc_searchButton;
 	private JPanel optionsPanel;
 	private JButton newSubtitlesFolder;
 	private GridBagConstraints gbc_btnNovasLegendas;
 	private JComboBox resolution;
 	private GridBagConstraints gbc_resolution;
 	private JLabel subtitlesFolder;
 	private GridBagConstraints gbc_subtitlesFolder;
 	private JLabel subtitleFolderLabel;
 	private GridBagConstraints gbc_lblPastaDeLegendas;
 	private JButton subtitlesDest;
 	private GridBagConstraints gbc_subtitlesDest;
 	private JPanel searchResultsPanel;
 	private JList result;
 	private JScrollPane resultJScrollPane;
 	private JScrollPane outputJScrollPane;
 	private JTextArea outputTextArea;
 	private JPanel outputPanel;
 	private final GUISearchCallback endSearch;
 	private final ActionListener searchForSearchTerm;
 	private final SearchScreenNeeds searchScreenNeeds;
 	private JProgressBar progressBar;
 	private JSplitPane splitPane;
 	private ActionListener searchNewSubtitles;
 	
 	final String defaultSearchString = "Digite sua procura aqui...";
 	
 	public SearchScreen(final SearchScreenNeeds searchScreenNeeds) {
 		
 		this.searchScreenNeeds = searchScreenNeeds;
 		
 		defaultListModel = new DefaultListModel();
 		
 		endSearch = new GUISearchCallback() {
 			@Override
 			public void done() {
 				progressBar.setIndeterminate(false);
 				outputTextArea.append("\nProcura completa.");
 				outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
 			}
 			
 			@Override
 			public void found(final String name) {
 				addSubtitleToList(name);
 			}
 			
 			private void addSubtitleToList(final String name) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						defaultListModel.addElement(name);
 					}
 				});
 			}
 		};
 		
 		searchForSearchTerm = new ActionListener() {
 			
 			String lastSearch = "";
 			
 			@Override
 			public void actionPerformed(final ActionEvent event) {
 				final String searchTerm = searchString.getText();
 				final boolean noNewSearch = lastSearch.equals(searchTerm);
 				if(noNewSearch){
 					final int selectedIndex = result.getSelectedIndex();
 					if(selectedIndex != -1){
 						downloadSubtitleAtPosition(selectedIndex);
 					}
 					return;
 				}
 				lastSearch = searchTerm;
 				outputTextArea.append("\nProcurando "+searchTerm);
 				outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
 				progressBar.setIndeterminate(true);
 				clearList();
 				searchScreenNeeds.getResultsFor(searchTerm, endSearch);
 			}			
 		};
 		
 		setupJFrame();
 		
 		searchScreenNeeds.setOutputListener(new OutputListener() {
 			
 			@Override
 			public void printHelp(final String applicationName, final Options options) {
 				
 			}
 			
 			@Override
 			public void outVerbose(final String string) {
 				output(string);
 			}
 			
 			@Override
 			public void out(final String string) {
 				output(string);
 			}
 		});
 		
 		setupUpperPanel();
 		setupSearchResultPanel();
 		
 		setVisible(true);
 		searchNewSubtitles.actionPerformed(null);
 	}
 	
 	private void downloadSubtitleAtPosition(final int index) {
 		final ListModel dlm = result.getModel();
 		final String subtitlePackage = (String) dlm.getElementAt(index);
 		result.ensureIndexIsVisible(index);
 		download(subtitlePackage);
 	}
 
 	private void download(final String subtitlePackage) {
 		progressBar.setIndeterminate(true);
 		output("Fazendo o download de '"+subtitlePackage+"'.");
 		searchScreenNeeds.download(subtitlePackage, new DownloadCallback() {
 			@Override
 			public void done(boolean found) {
 				progressBar.setIndeterminate(false); 
 				if(found){
 					output("Dowload de '"+subtitlePackage+"' terminado com sucesso.");
 				}else{
 					output("ERRO!");
 					output("Torrent compatível para '"+subtitlePackage+"' não foi encontrado.");
 				}
 			}
 		});
 	}
 
 	private void setupSearchResultPanel() {
 		searchResultsPanel = new JPanel();
 		searchResultsPanel.setLayout(new BorderLayout(0, 0));		
 		
 		getContentPane().add(searchResultsPanel, BorderLayout.CENTER);
 		
 		setupOutputPanel();
 	}
 
 	@SuppressWarnings({ })
 	private void setupOutputPanel() {
 		splitPane = new JSplitPane();
 		splitPane.setContinuousLayout(true);
 		resultJScrollPane = new JScrollPane();	
 		
 		result = new JList();
 		result.setFocusable(false);
 		result.setModel(defaultListModel);
 		result.addMouseListener(new MouseAdapter() { @Override public void mouseClicked(final MouseEvent e) {
 			if (e.getClickCount() == 2) {
 				final int index = result.locationToIndex(e.getPoint());
 				downloadSubtitleAtPosition(index);
 			}
 		}});
 		
 
 		final JPopupMenu popup = new JPopupMenu();
 		
 		result.addMouseListener(new MouseAdapter() {
 			@Override public void mousePressed(MouseEvent e)  {check(e);}
 			@Override public void mouseReleased(MouseEvent e) {check(e);}
 			
 			public void check(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					int locationToIndex = result.locationToIndex(e.getPoint());
 					if(locationToIndex == -1) return;
 					result.setSelectedIndex(locationToIndex);
 					popup.show(result, e.getX(), e.getY());
 				}
 			}
 		});
 		
 		JMenuItem menuDownloadSubtitles = new JMenuItem("Fazer download somente das legendas");
 		menuDownloadSubtitles.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final String item = (String) result.getSelectedValue();
				searchScreenNeeds.downloadSubtitles(item, new DownloadCallback() {
					@Override
					public void done(boolean found) {
						progressBar.setIndeterminate(false); 
						if(found){
							output("Dowload de legendas de '"+item+"' terminado com sucesso.");
						}else{
							output("ERRO!");
							output("Ocorreu um erro ao pegar as legendas de '"+item+"'.");
						}
					}
				});
 			}
 		});
 		
 		JMenuItem menuDownloadSubtitlesAndTorrent = new JMenuItem("Fazer download de legenda e torrent");
 		menuDownloadSubtitlesAndTorrent.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				final String item = (String) result.getSelectedValue();
 				download(item);
 			}
 		});
 		
		popup.add(menuDownloadSubtitles);
 		popup.add(menuDownloadSubtitlesAndTorrent);
 		
 		progressBar = new JProgressBar();
 		
 		outputTextArea = new JTextArea();
 		outputPanel = new JPanel();
 		
 		outputJScrollPane = new JScrollPane();
 		outputTextArea.setRows(5);
 		outputJScrollPane.setViewportView(outputTextArea);
 		outputPanel.setLayout(new BorderLayout(0, 0));
 		outputPanel.add(outputJScrollPane);
 		
 		outputPanel.add(progressBar, BorderLayout.SOUTH);
 
 		resultJScrollPane.setViewportView(result);
 		
 		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		
 		splitPane.setLeftComponent(outputPanel);
 		splitPane.setRightComponent(resultJScrollPane);
 
 		
 		searchResultsPanel.add(splitPane, BorderLayout.CENTER);
 		
 	}
 
 	private void setupUpperPanel() {
 		upperPanel = new JPanel();
 		upperPanel.setLayout(new BorderLayout(0, 0));
 		
 		setupOptionsPanel();
 		
 		getContentPane().add(upperPanel, BorderLayout.NORTH);
 		searchPanel = new JPanel();
 		gbl_searchPanel = new GridBagLayout();
 		gbl_searchPanel.columnWidths = new int[]{122, 100, 0};
 		gbl_searchPanel.rowHeights = new int[]{27, 0};
 		gbl_searchPanel.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
 		gbl_searchPanel.rowWeights = new double[]{1.0, Double.MIN_VALUE};
 		searchPanel.setLayout(gbl_searchPanel);
 		upperPanel.add(searchPanel, BorderLayout.SOUTH);
 		
 		searchString = new JTextField();
 		gbc_searchString = new GridBagConstraints();
 		gbc_searchString.fill = GridBagConstraints.HORIZONTAL;
 		gbc_searchString.insets = new Insets(0, 0, 0, 5);
 		gbc_searchString.gridx = 0;
 		gbc_searchString.gridy = 0;
 		searchString.setColumns(10);
 		searchString.setText(defaultSearchString);
 		searchString.addFocusListener(new FocusListener() {
 			
 			@Override
 			public void focusLost(final FocusEvent e) {
 				if(searchString.getText().equals("")){
 					searchString.setText(defaultSearchString);
 				}
 			}
 			
 			@Override
 			public void focusGained(final FocusEvent e) {
 				if(searchString.getText().equals(defaultSearchString)){
 					searchString.setText("");
 				}
 			}
 		});
 		searchString.addActionListener(searchForSearchTerm);
 		searchString.addKeyListener(new KeyAdapter(){
 			@Override
 			public void keyPressed(final KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_UP || 
 					e.getKeyCode() == KeyEvent.VK_DOWN ||
 					e.getKeyCode() == KeyEvent.VK_PAGE_UP||
 					e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
 					result.dispatchEvent(e);
 			}
 		});
 		searchPanel.add(searchString, gbc_searchString);
 		
 		searchButton = new JButton("Procurar");
 		gbc_searchButton = new GridBagConstraints();
 		gbc_searchButton.fill = GridBagConstraints.HORIZONTAL;
 		gbc_searchButton.gridx = 1;
 		gbc_searchButton.gridy = 0;
 		searchButton.addActionListener(searchForSearchTerm);
 		searchButton.setFocusable(false);
 		searchPanel.add(searchButton, gbc_searchButton);
 	}
 
 	@SuppressWarnings({ })
 	private void setupOptionsPanel() {
 		optionsPanel = new JPanel();
 		final GridBagLayout gbl_optionsPanel = new GridBagLayout();
 		gbl_optionsPanel.columnWidths = new int[]{141, 0, 126, 0, 0, 0};
 		gbl_optionsPanel.rowHeights = new int[]{15, 0, 0};
 		gbl_optionsPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_optionsPanel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		optionsPanel.setLayout(gbl_optionsPanel);
 		upperPanel.add(optionsPanel, BorderLayout.CENTER);
 		
 		newSubtitlesFolder = new JButton("Novas legendas");
 		gbc_btnNovasLegendas = new GridBagConstraints();
 		gbc_btnNovasLegendas.fill = GridBagConstraints.VERTICAL;
 		gbc_btnNovasLegendas.anchor = GridBagConstraints.WEST;
 		gbc_btnNovasLegendas.insets = new Insets(0, 0, 5, 5);
 		gbc_btnNovasLegendas.gridx = 0;
 		gbc_btnNovasLegendas.gridy = 0;
 		newSubtitlesFolder.setFocusable(false);
 		searchNewSubtitles = new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent arg0) {
 				
 				final String text = "Procurando novas legendas...";
 				output(text);
 				progressBar.setIndeterminate(true);
 				clearList();
 				searchScreenNeeds.getNewAddsList(endSearch);
 			}
 
 		};
 		
 		newSubtitlesFolder.addActionListener(searchNewSubtitles);
 		optionsPanel.add(newSubtitlesFolder, gbc_btnNovasLegendas);
 		
 		resolution = new JComboBox();
 		gbc_resolution = new GridBagConstraints();
 		gbc_resolution.fill = GridBagConstraints.VERTICAL;
 		gbc_resolution.anchor = GridBagConstraints.WEST;
 		gbc_resolution.insets = new Insets(0, 0, 5, 5);
 		gbc_resolution.gridx = 1;
 		gbc_resolution.gridy = 0;
 		optionsPanel.add(resolution, gbc_resolution);
 		resolution.setFocusable(false);
 		resolution.setModel(new DefaultComboBoxModel(new String[] { searchScreenNeeds.allResolutionsString() , searchScreenNeeds.highResolutionString(), searchScreenNeeds.lowResolutionString()}));
 		resolution.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(final ActionEvent arg0) {
 				searchScreenNeeds.setResolution(resolution.getSelectedItem().toString());
 			}
 		});
 		
 		
 		subtitleFolderLabel = new JLabel("Pasta de legendas: ");
 		gbc_lblPastaDeLegendas = new GridBagConstraints();
 		gbc_lblPastaDeLegendas.fill = GridBagConstraints.VERTICAL;
 		gbc_lblPastaDeLegendas.anchor = GridBagConstraints.WEST;
 		gbc_lblPastaDeLegendas.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPastaDeLegendas.gridx = 2;
 		gbc_lblPastaDeLegendas.gridy = 0;
 		optionsPanel.add(subtitleFolderLabel, gbc_lblPastaDeLegendas);		
 		
 		subtitlesFolder = new JLabel("...");
 		gbc_subtitlesFolder = new GridBagConstraints();
 		gbc_subtitlesFolder.insets = new Insets(0, 0, 5, 5);
 		gbc_subtitlesFolder.gridx = 3;
 		gbc_subtitlesFolder.gridy = 0;
 		final String defaultSubtitlesFolder = searchScreenNeeds.getSubtitleFolder();
 		subtitlesFolder.setText(defaultSubtitlesFolder);
 		optionsPanel.add(subtitlesFolder, gbc_subtitlesFolder);		
 		
 		subtitlesDest = new JButton("...");
 		gbc_subtitlesDest = new GridBagConstraints();
 		gbc_subtitlesDest.fill = GridBagConstraints.VERTICAL;
 		gbc_subtitlesDest.anchor = GridBagConstraints.EAST;
 		gbc_subtitlesDest.insets = new Insets(0, 0, 5, 0);
 		gbc_subtitlesDest.gridx = 4;
 		gbc_subtitlesDest.gridy = 0;
 		optionsPanel.add(subtitlesDest, gbc_subtitlesDest);
 		subtitlesDest.setFocusable(false);
 		subtitlesDest.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				final JFileChooser jFileChooser = new JFileChooser();
 				jFileChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
 				jFileChooser.showDialog(null, "OK");
 				final File selectedFile = jFileChooser.getSelectedFile();
 				if(selectedFile == null) return;
 				searchScreenNeeds.setSubtitleFolder(selectedFile);
 				subtitlesFolder.setText(selectedFile.getAbsolutePath());
 			}
 		});
 		
 	}
 
 	private void output(final String text) {
 		if(!outputTextArea.getText().equals("")){
 			outputTextArea.append("\n");
 		}
 		outputTextArea.append(text);
 		outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength()-text.length());
 	}
 	
 	private void setupJFrame() {
 		final URL resource = SearchScreen.class.getResource("filmeUtils.png");
 		final ImageIcon imageIcon = new ImageIcon(resource);
 		setIconImage(imageIcon.getImage());
 		
 		try {
 			setSystemLookAndFeel();
 		} catch (final Exception e) {
 			try {
 				setNimbusLookAndFeel();
 			} catch (final Exception e1) {
 			}
 		}
 		setTitle("FilmeUtils");
 		getContentPane().setLayout(new BorderLayout(0, 0));
 		setSize(800,600);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void setSystemLookAndFeel() throws ClassNotFoundException,
 			InstantiationException, IllegalAccessException,
 			UnsupportedLookAndFeelException {
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	}
 	
 	private void clearList() {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				defaultListModel.clear();
 			}
 		});
 	}
 
 	private void setNimbusLookAndFeel() throws ClassNotFoundException,
 			InstantiationException, IllegalAccessException,
 			UnsupportedLookAndFeelException {
 		final LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
 		for (final LookAndFeelInfo info : installedLookAndFeels) {
 			if ("Nimbus".equals(info.getName())) {
 				UIManager.setLookAndFeel(info.getClassName());
 				break;
 			}
 		}
 	}
 	
 }
