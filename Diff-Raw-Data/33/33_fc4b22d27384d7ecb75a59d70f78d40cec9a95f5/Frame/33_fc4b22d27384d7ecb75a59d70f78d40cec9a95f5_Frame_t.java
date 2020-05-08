 package ClassAdminFrontEnd;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.text.BadLocationException;
 
 import ClassAdminBackEnd.FileHandler;
 import ClassAdminBackEnd.Global;
 import ClassAdminBackEnd.SuperEntity;
 import ClassAdminBackEnd.UnsupportedFileTypeException;
 
 import org.imgscalr.Scalr;
 import org.jdesktop.swingx.JXPanel;
 
 public class Frame extends JFrame {
 
 	private JPanel contentPane;
	private FadePanel homePanel, workspacePanel, navBar, tabBar, infoPanel, scatterplotInfoPanel;
 	private ThreeStopGradientPanel bottomPanel;
 	private BackgroundGradientPanel backgroundPanel;
 	private GradientMenuBar menuBarWindows;
 	private JMenuBar menuBarMAC;
 	private ReflectionImagePanel container, containerRecentDocs,
 			containerImportImage, containerWorkspace, containerStudents;
 	private MenuImagePanel containerImportText, containerWorkspaceText,
 			containerStudentsText, studentsViewArrowOut, studentsViewArrowIn;
 	private ImagePanel containerImportTextSub, containerStudentsTextSub,
 			containerWorkspaceTextSub, boxChartImage, histogramChartImage,
 			scatterplotChartImage, studentPhoto;
 	private JFileChooser filechooser;
 	private JFrame frame = this;
 	private File currentFilePath;
 	private int tabCount = -1;
 	private FrmTable table;
 	private JTabbedPane tabbedPane;
 	private FileHandler fileHandler;
 	private BlurBackground blur;
 	private ReflectionButton homeButton, importButton, exportButton,
 			studentsButton, histogramButton, boxButton, scatterButton;
 	private FadePanel homeInfoPanel, importInfoPanel, exportInfoPanel,
 			studentsInfoPanel, histogramInfoPanel, boxplotInfoPanel;
 	private ShadowPanel studentPanel;
 
 	private JButton button;
 
 	private int HOME_SPACE_LEFT_X;
 	private int HOME_SPACE_Y;
 	private int HOME_BOTTOM_SPACE_Y;
 	private int HOME_SPACE_RIGHT_X;
 
 	private static String currentOs;
 	private static String MAC_OS = "MAC";
 	private static String WIN_OS = "WINDOWS";
 
 	public class TabButton extends JPanel {
 
 		private String text;
 		private JLabel label;
 		private JLabel button;
 		private TabButton tabbutton = this;
 
 		public TabButton(String _text) {
 
 			// create label with file name for tab
 			text = _text;
 			label = new JLabel(text);
 			add(label);
 
 			// create close button
 			button = new JLabel("x");
 			// button.setBorder(new EmptyBorder(1,1,1,1));
 			add(button);
 			button.setForeground(Color.white);
 
 			// set this panel with label and close button to transparent
 			this.setOpaque(false);
 			this.setBorder(null);
 
 			// close tab action
 			button.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent e) {
 					tabbedPane.remove(tabbedPane.indexOfTabComponent(tabbutton));
 					tabCount--;
 					if (tabCount == -1) {
 						if (studentsButton != null) {
 							studentsButton.setDisabled();
 						}
 						if (studentPanel != null) {
 							studentPanel.setVisible(false);
 						}
 					}
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent e) {
 					button.setForeground(Color.darkGray);
 				}
 
 				@Override
 				public void mouseExited(MouseEvent e) {
 					button.setForeground(Color.white);
 				}
 
 			});
 		}
 	}
 
 	class SelectionListener implements ListSelectionListener {
 		JTable table;
 
 		SelectionListener(JTable table) {
 			this.table = table;
 		}
 
 		public void valueChanged(ListSelectionEvent e) {
 			if (e.getSource() == table.getSelectionModel()
 					&& table.getRowSelectionAllowed()) {
 				int first = e.getFirstIndex();
 				int last = e.getLastIndex();
 				System.out.println("listen");
 			} else if (e.getSource() == table.getColumnModel()
 					.getSelectionModel() && table.getColumnSelectionAllowed()) {
 				int first = e.getFirstIndex();
 				int last = e.getLastIndex();
 				System.out.println("listen2");
 			}
 			if (e.getValueIsAdjusting()) {
 				System.out
 						.println("The mouse button has not yet been released");
 			}
 		}
 
 	}
 
 	public Frame() {
 
 		// set frame title
 		setTitle("UP Admin");
 
 		// get OS
 		determineOS();
 
 		// frame setup
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 1200, 700);
 
 		Image icon = Toolkit.getDefaultToolkit().getImage("Logo.png");
 		this.setIconImage(icon);
 
 		// Get the size of the screen
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		// Determine the new location of the window
 		int w = getSize().width;
 		int h = getSize().height;
 		int x = (dim.width - w) / 2;
 		int y = (dim.height - h) / 2;
 		// Move the window
 		setLocation(x, y);
 
 		// maximize window
 		// setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
 
 		// create content pane
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setBackground(new Color(0x212121));
 		contentPane.setLayout(null);
 
 		if (currentOs == MAC_OS) {
 			setupMAC();
 		} else {
 			setupWindows();
 		}
 
 		// create little bottom bar of home screen
 		bottomPanel = new ThreeStopGradientPanel(new Color(0xA1A1A1),
 				new Color(0x696969), new Color(0x000000), contentPane);
 		bottomPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, 12);
 		bottomPanel.setBounds(HOME_SPACE_LEFT_X, getHeight()
 				- HOME_BOTTOM_SPACE_Y, bottomPanel.getWidth(),
 				bottomPanel.getHeight());
 		contentPane.add(bottomPanel);
 
 		setupHomeScreen();
 		setupWorkspaceScreen();
 
 		// frame resize listener to put nav bar at bottom of frame on resize
 		this.addComponentListener(new ComponentListener() {
 
 			@Override
 			public void componentHidden(ComponentEvent arg0) {
 
 			}
 
 			@Override
 			public void componentMoved(ComponentEvent arg0) {
 
 			}
 
 			// resizes components with screen
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 
 				bottomPanel.setBounds(HOME_SPACE_LEFT_X, frame.getHeight()
 						- HOME_BOTTOM_SPACE_Y, frame.getWidth()
 						- HOME_SPACE_RIGHT_X, 12);
 
 				if (currentOs != MAC_OS) {
 					backgroundPanel.setBounds(HOME_SPACE_LEFT_X,
 							menuBarWindows.getHeight(), frame.getWidth()
 									- HOME_SPACE_RIGHT_X, frame.getHeight()
 									- HOME_SPACE_Y - menuBarWindows.getHeight());
 				} else {
 
 					backgroundPanel.setSize(frame.getWidth()
 							- HOME_SPACE_RIGHT_X, frame.getHeight()
 							- HOME_SPACE_Y);
 					backgroundPanel.rerenderBackground();
 
 					bottomPanel.rerenderBackground();
 				}
 
 				workspacePanel.setBounds(0, 0, backgroundPanel.getWidth(),
 						backgroundPanel.getHeight());
 
 				if (currentOs != MAC_OS) {
 					menuBarWindows.setBounds(0, 0, getWidth(), 30);
 				}
 
 				navBar.setBounds(0, backgroundPanel.getHeight() - 40 - 40,
 						getWidth(), 80);
 				workspacePanel.add(navBar);
 
 				if (tabbedPane != null) {
 					tabbedPane.setBounds(
 							20,
 							20,
 							workspacePanel.getWidth() - 40,
 							workspacePanel.getHeight() - 40
 									- navBar.getHeight());
 				}
 				if (tabBar != null) {
 					tabBar.setBounds(0, 0, frame.getWidth(), frame.getHeight());
 					tabBar.setLayout(null);
 				}
 				if (boxChartImage != null) {
 					boxChartImage.setBounds(tabBar.getWidth() - 70, 15, 50, 40);
 				}
 				if (histogramChartImage != null) {
 					histogramChartImage.setBounds(tabBar.getWidth() - 105, 15,
 							50, 40);
 				}
 				if (scatterplotChartImage != null) {
 					scatterplotChartImage.setBounds(tabBar.getWidth() - 140,
 							15, 50, 40);
 				}
 				if (infoPanel != null) {
 					infoPanel.setBounds(0, workspacePanel.getHeight() - 112,
 							getWidth(), 43);
 				}
 				if (studentPanel != null) {
 					studentPanel.setBounds(frame.getWidth() - 45, 0, 250,
 							getHeight() - 20);
 					studentPanel.setNewX(getWidth() - 45);
 					studentPanel.setOldX(getWidth() - 250);
 				}
 
 			}
 
 			@Override
 			public void componentShown(ComponentEvent arg0) {
 
 			}
 		});
 
 		fileHandler = FileHandler.get();
 	}
 
 	public void setupWindows() {
 		// create menubar
 		menuBarWindows = new GradientMenuBar();
 		menuBarWindows.setBounds(0, 0, getWidth(), 30);
 		contentPane.add(menuBarWindows);
 
 		// create menu
 		JMenu mnFile = new JMenu("File");
 		mnFile.setForeground(Color.white);
 		menuBarWindows.add(mnFile);
 
 		HOME_SPACE_LEFT_X = 3;
 		HOME_SPACE_Y = 55;
 		HOME_BOTTOM_SPACE_Y = 53;
 		HOME_SPACE_RIGHT_X = 22;
 
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, getHeight()
 				- HOME_SPACE_Y - menuBarWindows.getHeight());
 		backgroundPanel.setBounds(HOME_SPACE_LEFT_X,
 				menuBarWindows.getHeight(), backgroundPanel.getWidth(),
 				backgroundPanel.getHeight());
 		backgroundPanel.setLayout(null);
 		contentPane.add(backgroundPanel);
 	}
 
 	public void setupMAC() {
 		// create menubar
 		menuBarMAC = new JMenuBar();
 		contentPane.add(menuBarMAC);
 		setJMenuBar(menuBarMAC);
 
 		// create menu
 		JMenu mnFile = new JMenu("File");
 		menuBarMAC.add(mnFile);
 
 		JMenu mnEdit = new JMenu("Edit");
 		menuBarMAC.add(mnEdit);
 
 		HOME_SPACE_LEFT_X = 3;
 		HOME_SPACE_Y = 41;
 		HOME_BOTTOM_SPACE_Y = 39;
 		HOME_SPACE_RIGHT_X = 6;
 
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setSize(getWidth() - HOME_SPACE_RIGHT_X, getHeight()
 				- HOME_SPACE_Y);
 		backgroundPanel.setBounds(HOME_SPACE_LEFT_X, 0,
 				backgroundPanel.getWidth(), backgroundPanel.getHeight());
 		backgroundPanel.setLayout(null);
 		contentPane.add(backgroundPanel);
 	}
 
 	public void setupHomeScreen() {
 
 		homePanel = new FadePanel(false, 800, 400);
 		homePanel.setBounds(0, 0, backgroundPanel.getWidth(),
 				backgroundPanel.getHeight());
 		backgroundPanel.add(homePanel);
 		homePanel.setLayout(null);
 
 		blur = new BlurBackground(this);
 		this.setGlassPane(blur);
 		blur.setBounds(0, 0, getWidth(), getHeight());
 
 		// add title bars
 		try {
 			container = new ReflectionImagePanel(ImageIO.read(getClass()
 					.getResource("UPAdminHomeSelectTask.png")));
 			container.setBounds(117, 25, 953, 88);
 			homePanel.add(container);
 
 			containerRecentDocs = new ReflectionImagePanel(
 					ImageIO.read(getClass().getResource(
 							"UPAdminHomeRecentDocs.png")));
 			containerRecentDocs.setBounds(117, 366, 953, 81);
 			homePanel.add(containerRecentDocs);
 
 			containerImportImage = new ReflectionImagePanel(
 					ImageIO.read(getClass().getResource("Import.png")));
 			containerImportImage.setBounds(163, 139, 75, 138);
 			homePanel.add(containerImportImage);
 
 			containerWorkspace = new ReflectionImagePanel(
 					ImageIO.read(getClass().getResource("Workspace.png")));
 			containerWorkspace.setBounds(163, 235, 75, 102);
 			homePanel.add(containerWorkspace);
 
 			containerStudents = new ReflectionImagePanel(
 					ImageIO.read(getClass().getResource("Students.png")));
 			containerStudents.setBounds(544, 124, 75, 102);
 			homePanel.add(containerStudents);
 
 			containerImportText = new MenuImagePanel(ImageIO.read(getClass()
 					.getResource("ImportText.png")));
 			containerImportText.setBounds(210, 130, 89, 45);
 			homePanel.add(containerImportText);
 
 			containerImportTextSub = new ImagePanel(ImageIO.read(getClass()
 					.getResource("ImportSub.png")));
 			containerImportTextSub.setBounds(210, 166, 129, 32);
 			homePanel.add(containerImportTextSub);
 
 			containerStudentsText = new MenuImagePanel(ImageIO.read(getClass()
 					.getResource("StudentsText.png")));
 			containerStudentsText.setBounds(600, 130, 147, 54);
 			homePanel.add(containerStudentsText);
 
 			containerStudentsTextSub = new ImagePanel(ImageIO.read(getClass()
 					.getResource("StudentsSub.png")));
 			containerStudentsTextSub.setBounds(600, 166, 147, 32);
 			homePanel.add(containerStudentsTextSub);
 
 			containerWorkspaceText = new MenuImagePanel(ImageIO.read(getClass()
 					.getResource("WorkspaceText.png")));
 			containerWorkspaceText.setBounds(210, 224, 135, 53);
 			homePanel.add(containerWorkspaceText);
 
 			containerWorkspaceTextSub = new ImagePanel(ImageIO.read(getClass()
 					.getResource("WorkspaceSub.png")));
 			containerWorkspaceTextSub.setBounds(210, 259, 238, 32);
 			homePanel.add(containerWorkspaceTextSub);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		containerWorkspaceText.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				homeToWorkspaceTransition();
 			}
 		});
 
 		containerImportText.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				try {
 					openFile();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (BadLocationException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		homePanel.fadeIn();
 	}
 
 	private void setupWorkspaceScreen() {
 		// create background pane for workspace screen
 		workspacePanel = new FadePanel(false, 800, 400);
 		workspacePanel.setBounds(0, 0, backgroundPanel.getWidth(),
 				backgroundPanel.getHeight());
 		backgroundPanel.add(workspacePanel);
 		workspacePanel.setLayout(null);
 
 		// create navigation bar
 		navBar = new FadePanel(true, 800, 400);
 		navBar.setBounds(0, workspacePanel.getHeight() - 40 - 40, getWidth(),
 				80);
 		workspacePanel.add(navBar);
 		navBar.setLayout(null);
 
 		// create transparent panel on which info bubbles will be shown
 		infoPanel = new FadePanel(false, 200, 200);
 		infoPanel
 				.setBounds(0, workspacePanel.getHeight() - 112, getWidth(), 43);
 		workspacePanel.add(infoPanel);
 		infoPanel.setLayout(null);
 		infoPanel.fadeIn();
 
 		createStudentView();
 
 		try {
 
 			// create buttons on nav bar and add their respective mouselisteners
 			homeButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Home.png")));
 			homeButton.setBounds(8, 8, 68, 80);
 			navBar.add(homeButton);
 
 			importButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Import.png")));
 			importButton.setBounds(84, 8, 68, 80);
 			navBar.add(importButton);
 
 			exportButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Export.png")));
 			exportButton.setBounds(150, 8, 68, 80);
 			navBar.add(exportButton);
 
 			studentsButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Students.png")));
 			studentsButton.setBounds(217, 8, 68, 80);
 			navBar.add(studentsButton);
 			studentsButton.setDisabled();
 
 			histogramButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Histogram.png")));
 			histogramButton.setBounds(285, 12, 68, 80);
 			navBar.add(histogramButton);
 			histogramButton.setDisabled();
 
 			boxButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Box.png")));
 			boxButton.setBounds(355, 12, 68, 80);
 			navBar.add(boxButton);
 			boxButton.setDisabled();
 
 			scatterButton = new ReflectionButton(ImageIO.read(getClass()
 					.getResource("Scatter.png")));
 			scatterButton.setBounds(420, 12, 68, 80);
 			navBar.add(scatterButton);
 			scatterButton.setDisabled();
 
 			// create info bubbles panel
 			homeInfoPanel = new FadePanel(false, 200, 200);
 			homeInfoPanel.setBounds(8, 0, 62, infoPanel.getHeight());
 			homeInfoPanel.setLayout(null);
 			infoPanel.add(homeInfoPanel);
 
 			// create info bubble image
 			ImagePanel infoBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("HomeInfo.png")));
 			infoBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			infoBubble.setLayout(null);
 			homeInfoPanel.add(infoBubble);
 
 			// create import bubbles panel
 			importInfoPanel = new FadePanel(false, 200, 200);
 			importInfoPanel.setBounds(80, 0, 62, infoPanel.getHeight());
 			importInfoPanel.setLayout(null);
 			infoPanel.add(importInfoPanel);
 
 			// create import bubble image
 			ImagePanel importBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("ImportInfo.png")));
 			importBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			importBubble.setLayout(null);
 			importInfoPanel.add(importBubble);
 
 			// create export bubbles panel
 			exportInfoPanel = new FadePanel(false, 200, 200);
 			exportInfoPanel.setBounds(150, 0, 62, infoPanel.getHeight());
 			exportInfoPanel.setLayout(null);
 			infoPanel.add(exportInfoPanel);
 
 			// create export bubble image
 			ImagePanel exportBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("ExportInfo.png")));
 			exportBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			exportBubble.setLayout(null);
 			exportInfoPanel.add(exportBubble);
 
 			// create students bubbles panel
 			studentsInfoPanel = new FadePanel(false, 200, 200);
 			studentsInfoPanel.setBounds(195, 0, 125, infoPanel.getHeight());
 			studentsInfoPanel.setLayout(null);
 			infoPanel.add(studentsInfoPanel);
 
 			// create students bubble image
 			ImagePanel studentsBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("StudentsInfo.png")));
 			studentsBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			studentsBubble.setLayout(null);
 			studentsInfoPanel.add(studentsBubble);
 
 			// create histogram bubbles panel
 			histogramInfoPanel = new FadePanel(false, 200, 200);
 			histogramInfoPanel.setBounds(262, 0, 125, infoPanel.getHeight());
 			histogramInfoPanel.setLayout(null);
 			infoPanel.add(histogramInfoPanel);
 
 			// create histogram bubble image
 			ImagePanel histogramBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("InfoHistogram.png")));
 			histogramBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			histogramBubble.setLayout(null);
 			histogramInfoPanel.add(histogramBubble);
 
 			// create box plot bubbles panel
 			boxplotInfoPanel = new FadePanel(false, 200, 200);
 			boxplotInfoPanel.setBounds(345, 0, 125, infoPanel.getHeight());
 			boxplotInfoPanel.setLayout(null);
 			infoPanel.add(boxplotInfoPanel);
 
 			// create box plot bubble image
 			ImagePanel boxplotBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("InfoBoxPlot.png")));
 			boxplotBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
 			boxplotBubble.setLayout(null);
 			boxplotInfoPanel.add(boxplotBubble);
 
 			// create scatter plot bubbles panel
			scatterplotInfoPanel = new FadePanel(false, 200, 200);
			scatterplotInfoPanel.setBounds(415, 0, 125, infoPanel.getHeight());
			scatterplotInfoPanel.setLayout(null);
			infoPanel.add(scatterplotInfoPanel);
 
			// create scatterplot bubble image
			ImagePanel scatterplotBubble = new ImagePanel(ImageIO.read(getClass()
 					.getResource("InfoBoxPlot.png")));
			scatterplotBubble.setBounds(0, 0, infoPanel.getWidth(),
 					infoPanel.getHeight());
			scatterplotBubble.setLayout(null);
			scatterplotInfoPanel.add(scatterplotBubble);
 
 			homeButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					workspaceToHomeTransition();
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					homeInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					homeInfoPanel.fadeOut();
 				}
 			});
			
 
 			importButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					try {
 						openFile();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (BadLocationException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					importInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					importInfoPanel.fadeOut();
 				}
 			});
 
 			exportButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					try {
 						saveFileAs();
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					exportInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					exportInfoPanel.fadeOut();
 				}
 			});
 
 			studentsButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					if (!studentsButton.isDisabled()) {
 						table.getTable().getSelectedRow();
 
 						TreeView.createStudentFrm(
 								"name",
 								table.getData()
 										.get(table.getTable().getSelectedRow())
 										.get(0));
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					studentsInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					studentsInfoPanel.fadeOut();
 				}
 			});
 
 			histogramButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					if (!histogramButton.isDisabled()) {
 						HistogramFrame x = new HistogramFrame();
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					histogramInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					histogramInfoPanel.fadeOut();
 				}
 			});
 
 			boxButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					if (!boxButton.isDisabled()) {
 						BoxPlotFrame x = new BoxPlotFrame();
 						x.createBoxPlotFrame();
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
 					boxplotInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
 					boxplotInfoPanel.fadeOut();
 				}
 			});
 
 			scatterButton.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					if (!scatterButton.isDisabled()) {
 						ScatterPlotFrame x = new ScatterPlotFrame();
 					}
 				}
 
 				public void mouseEntered(MouseEvent arg0) {
					scatterplotInfoPanel.fadeIn();
 				}
 
 				public void mouseExited(MouseEvent arg0) {
					scatterplotInfoPanel.fadeOut();
 				}
 			});
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void openFile() throws IOException, BadLocationException {
 
 		File file;
 		// set the file extentions that may be chosen
 		FileFilter fileFilter = new FileNameExtensionFilter(
 				"Supported files types: pdat, csv", "pdat", "csv");
 
 		blur.fadeIn();
 		// Create a file chooser
 		filechooser = new JFileChooser();
 		if (currentFilePath != null) {
 			filechooser.setCurrentDirectory(currentFilePath);
 		}
 
 		// add the filter to the file chooser
 		filechooser.addChoosableFileFilter(fileFilter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showOpenDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 			currentFilePath = filechooser.getSelectedFile();
 			blur.fadeOut();
 			createTab(file);
 			homeToWorkspaceTransition();
 			tabBar.fadeIn();
 
 			// studentPanel.moveIn();
 		} else {
 			blur.fadeOut();
 		}
 	}
 
 	public void saveFileAs() throws IOException {
 
 		File file;
 		// set the file extentions that may be chosen
 		FileFilter filter = new FileNameExtensionFilter(
 				"Supported files types: pdat, csv", "pdat", "csv");
 
 		// Create a file chooser
 		final JFileChooser filechooser = new JFileChooser();
 		// remove the "All Files" type
 		// filechooser.setAcceptAllFileFilterUsed(false);
 		// add the filter to the file chooser
 		filechooser.addChoosableFileFilter(filter);
 
 		// shows the dialog, return value specifies file
 		int returnVal = filechooser.showSaveDialog(this);
 
 		// if the chosen file is valid
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			file = filechooser.getSelectedFile();
 			try {
 				FileHandler.get().saveFile(file.getAbsolutePath(),
 						Global.getGlobal().getActiveProject());
 			} catch (UnsupportedFileTypeException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 
 		}
 	}
 
 	/*
 	 * function to determine OS that is currently running
 	 */
 	public static void determineOS() {
 		currentOs = System.getProperty("os.name").toUpperCase();
 		if (currentOs.contains("MAC")) {
 			currentOs = MAC_OS;
 		} else if (currentOs.contains("WINDOWS")) {
 			currentOs = WIN_OS;
 		} else {
 			currentOs = null;
 		}
 	}
 
 	/*
 	 * create a new Tab when a new file is imported
 	 */
 	public void createTab(File file) {
 		try {
 			fileHandler.openFile(file.getAbsolutePath(), Global.getGlobal()
 					.getActiveProject());
 		} catch (UnsupportedFileTypeException e) {
 			e.printStackTrace();
 		}
 		// create table on panel
 		table = new FrmTable(Global.getGlobal().getActiveProject().getHead()
 				.getHeaders(), Global.getGlobal().getActiveProject().getHead()
 				.getDataLinkedList(), Global.getGlobal().getActiveProject());
 
 		// listener for changes of selection in table
 		table.getTable().getSelectionModel()
 				.addListSelectionListener(new ListSelectionListener() {
 					public void valueChanged(ListSelectionEvent e) {
 						showStudent();
 					}
 				});
 
 		// create tabbedPane
 		if (tabbedPane == null) {
 			tabbedPane = new JTabbedPane();
 			tabbedPane.setBounds(20, 20, workspacePanel.getWidth() - 40,
 					workspacePanel.getHeight() - 40 - navBar.getHeight());
 		}
 
 		// create panel on which tabbedPane will be
 		if (tabBar == null) {
 			tabBar = new FadePanel(false, 800, 400);
 			tabBar.setBounds(0, 0, frame.getWidth(), frame.getHeight());
 			tabBar.setLayout(null);
 		}
 		tabBar.add(tabbedPane);
 
 		workspacePanel.add(tabBar);
 
 		// put panel with table on a new tab
 		tabbedPane.addTab(file.getName(), table);
 		tabCount++;
 		tabbedPane.setTabComponentAt(tabCount, new TabButton(file.getName()));
 
 		studentsButton.setEnabled();
 		histogramButton.setEnabled();
 		boxButton.setEnabled();
 		scatterButton.setEnabled();
 
 		studentPanel.moveIn();
 	}
 
 	/*
 	 * Function to simulate transitions from home screen to workspace screen
 	 */
 	public void homeToWorkspaceTransition() {
 		homePanel.fadeOut();
 		workspacePanel.fadeIn();
 		navBar.fadeIn();
 		frame.remove(blur);
 	}
 
 	/*
 	 * Function to simulate transitions from workspace screen to home screen
 	 */
 	public void workspaceToHomeTransition() {
 		homePanel.fadeIn();
 		workspacePanel.fadeOut();
 		navBar.fadeOut();
 
 		if (studentPanel != null) {
 			studentPanel.setVisible(false);
 		}
 
 	}
 
 	public void createStudentView() {
 
 		studentPanel = new ShadowPanel(getWidth() - 45, 0, getWidth() - 250, 0);
 		studentPanel.setBounds(getWidth(), 0, 250, getHeight() - 20);
 		try {
 
 			studentsViewArrowOut = new MenuImagePanel(ImageIO.read(getClass()
 					.getResource("studentsViewArrowOut.png")));
 			studentsViewArrowIn = new MenuImagePanel(ImageIO.read(getClass()
 					.getResource("studentsViewArrowIn.png")));
 
 			studentsViewArrowOut.setBounds(3, 3, 20, 20);
 			studentsViewArrowIn.setBounds(3, 3, 20, 20);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		studentPanel.add(studentsViewArrowOut);
 		studentPanel.add(studentsViewArrowIn);
 
 		studentPanel.setLayout(null);
 
 		studentPanel.setShown(false);
 		studentsViewArrowIn.setVisible(false);
 
 		backgroundPanel.setLayer(studentPanel, 300);
 		backgroundPanel.add(studentPanel);
 		studentPanel.setVisible(false);
 
 		studentsViewArrowOut.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				studentPanel.moveOut();
 				studentsViewArrowOut.setVisible(false);
 				studentsViewArrowIn.setVisible(true);
 			}
 		});
 
 		studentsViewArrowIn.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				studentPanel.moveIn();
 				studentsViewArrowIn.setVisible(false);
 				studentsViewArrowOut.setVisible(true);
 			}
 		});
 	}
 
 	public void showStudent() {
 		String[] headers = Global.getGlobal().getActiveProject().getHead()
 				.getHeaders();
 		for (int i = 0; i < headers.length; i++) {
 			// System.out.println(headers[i]);
 		}
 
 		int row = table.getTable().getSelectedRow();
 		int colCount = table.getTable().getColumnCount();
 
 		String[] info = new String[colCount];
 
 		for (int i = 0; i < colCount; i++) {
 			info[i] = table.getTable().getValueAt(row, i).toString();
 			System.out.println(info[i]);
 		}
 
 		try {
 			BufferedImage photo = (ImageIO.read(getClass().getResource(
 					"/ClassAdminFrontEnd/StudentPhotos/10092120.JPG")));
 			photo = Scalr.resize(photo, 150);
 			studentPhoto = new ImagePanel(photo);
 			studentPanel.add(studentPhoto);
 			studentPhoto.setBounds(57, 50, photo.getWidth(), photo.getHeight());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
