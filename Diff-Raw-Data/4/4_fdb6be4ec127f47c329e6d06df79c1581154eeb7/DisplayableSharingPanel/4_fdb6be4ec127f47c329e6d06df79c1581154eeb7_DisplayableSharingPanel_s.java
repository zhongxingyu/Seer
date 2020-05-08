 /**
  * 
  */
 package net.niconomicon.tile.source.app.sharing;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.InetAddress;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 
 import net.niconomicon.tile.source.app.Ref;
 import net.niconomicon.tile.source.app.DisplayableCreatorInputPanel;
 import net.niconomicon.tile.source.app.filter.DirOrDisplayableFilter;
 import net.niconomicon.tile.source.app.tiling.SQliteTileCreatorMultithreaded;
 import net.niconomicon.tile.source.app.viewer.DisplayableViewer;
 
 /**
  * @author niko
  * 
  */
 public class DisplayableSharingPanel extends JPanel implements TableModelListener {
 
 	boolean currentlySharing = false;
 	SharingManager sharingManager;
 	JButton shareButton;
 	CheckBoxTable mapList;
 	JSpinner portNumber;
 	JLabel sharingStatus;
 	JLabel sharingLocation;
 	// String rootDir = "/Users/niko/Sites/testApp/mapRepository";
 
 	DisplayableViewer viewer;
 
 	InetAddress localaddr;
 	Timer timer;
 
 	public DisplayableSharingPanel(DisplayableViewer viewer) {
 		this.viewer = viewer;
 		init();
 	}
 
 	/* (non-Javadoc)
 	 * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
 	 */
 	public void tableChanged(TableModelEvent e) {
 		if (sharingManager.isSharing()) {
 			sharingManager.setSharingList(mapList.getSelectedTilesSetFiles());
 			// update the list of shared documents
 		} else {
 			// don't care ;-)
 			return;
 		}
 	}
 
 	public JPanel createDirSelectionPanel() {
 		JPanel p = new JPanel();
 		p.setLayout(new BorderLayout());
 		p.add(new JLabel("Locate Displayables for sharing : "), BorderLayout.WEST);
 		JButton b = new JButton("Choose Displayables");
 		b.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser fc = new JFileChooser();
 				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 				fc.setFileFilter(new DirOrDisplayableFilter());
 				fc.setMultiSelectionEnabled(true);
 				int res = fc.showOpenDialog(DisplayableSharingPanel.this);
 				if (JFileChooser.APPROVE_OPTION == res) {
 					File[] files = fc.getSelectedFiles();
 					setSelectedFiles(files);
 				}
 			}
 		});
 		p.add(b, BorderLayout.EAST);
 		return p;
 	}
 
 	public void init() {
 
 		sharingManager = new SharingManager();
 		mapList = new CheckBoxTable(viewer);
 		timer = new Timer();
 
 		sharingLocation = new JLabel();
 		sharingStatus = new JLabel("Sharing status : [not running]");
 
 		mapList.getModel().addTableModelListener(this);
 		mapList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		// mapList.getSelectionModel().addListSelectionListener(this);
 		this.setLayout(new BorderLayout());
 		this.add(createDirSelectionPanel(), BorderLayout.NORTH);
 		// shared files
 		// //////////////////////////////////////////
 		this.add(new JScrollPane(mapList), BorderLayout.CENTER);
 		JPanel options = new JPanel(new GridLayout(0, 1));
 		JButton removeButton = new JButton("Remove selected Displayable(s)");
 		removeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int item = mapList.getSelectedRow();
 				if (item < 0) {
 					JOptionPane.showMessageDialog(DisplayableSharingPanel.this, "Please select a displayable", "No Displayable selected",
 							JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 				((CheckBoxTable.CustomTableModel) mapList.getModel()).removeDisplayable(mapList.getSelectedRows());
 			}
 		});
 
 		options.add(removeButton);
 
 		// //////////////////////////////////////////
 		// port number
 		JPanel portPanel = new JPanel(new GridLayout(0, 2));
 		portPanel.add(new JLabel("Sharing port : "));
 		portNumber = new JSpinner(new SpinnerNumberModel(Ref.sharing_port, 1025, 65536, 1));
 		portPanel.add(portNumber);
 		options.add(portPanel);
 		// start sharing
 		options.add(sharingStatus);
 		shareButton = new JButton("Start sharing");
 		shareButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				switchSharing(true);
 			}
 		});
 		long delta = 10000;
 		timer.scheduleAtFixedRate(new LocalHostChecker(), delta, delta);
 		switchSharing(false);
 		options.add(shareButton);
 		options.add(sharingLocation);
 		this.add(options, BorderLayout.SOUTH);
 	}
 
 	public void setTooltipHostname(String host) {
 		if (host == null) {
 			sharingStatus.setToolTipText(null);
 			sharingLocation.setText("");
 			return;
 		}
 		int port = ((SpinnerNumberModel) portNumber.getModel()).getNumber().intValue();
 		sharingStatus
 				.setToolTipText("If the list of items do not appear quickly on your iPhone/iPod touch, try accessing http://" + host + ":" + port + "/ in your iPhone / iPod touch web browser");
 		sharingLocation
 		// .setText("<html><body>If the list of items do not appear quickly on your iPhone/iPod touch, try accessing http://"
 		// + host + ":" + port + "/ in your iPhone / iPod touch web browser</body></html>");
 				.setText("<html><body>Also accessible in Safari at http://" + host + ":" + port + "/ </body></html>");
 
 	}
 
 	public static List<String> getDBFilesInSubDirectory(File dir) {
 		List<String> dbFiles = new ArrayList<String>();
 		dbFiles.addAll(Arrays.asList(Ref.getAbsolutePathOfDBFilesInDirectory(dir)));
 		for (File d : dir.listFiles()) {
 			if (d.isDirectory()) {
 				dbFiles.addAll(getDBFilesInSubDirectory(d));
 			}
 		}
 		return dbFiles;
 	}
 
 	public void setSelectedFiles(File[] files) {
 		if (null == files) { return; }// clear ?
 		List<String> dbFiles = new ArrayList<String>();
 		for (File f : files) {
 			if (f.isDirectory()) {
 				dbFiles.addAll(getDBFilesInSubDirectory(f));
 			} else {
 				if (f.getAbsolutePath().endsWith(Ref.ext_db)) {
 					dbFiles.add(f.getAbsolutePath());
 				}
 			}
 		}
 		Collections.sort(dbFiles);
 		Map<String, String> fileToTitle = new HashMap<String, String>();
 		for (String path : dbFiles) {
 			try {
 				// System.out.println("Going to get the title from " + path);
 				fileToTitle.put(path, SQliteTileCreatorMultithreaded.getTitle(path));
 			} catch (SQLException ex) {
 				ex.printStackTrace();
 			}
 		}
 		mapList.addData(fileToTitle);
 		if (sharingManager.isSharing()) {
 			sharingManager.setSharingList(mapList.getSelectedTilesSetFiles());
 		}
 	}
 
 	public void switchSharing(boolean shouldPopup) {
 		try {
 			localaddr = InetAddress.getLocalHost();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		if (!currentlySharing) {
 			sharingStatus.setText("Sharing status : [starting ...]");
 			sharingStatus.revalidate();
 			if (startSharing(shouldPopup)) {
 				sharingStatus.setText("Sharing status : [running]");
 				setTooltipHostname(localaddr.getHostName());
 				shareButton.setText("Stop sharing");
				currentlySharing = !currentlySharing;
 				return;
 			}
 		}
 		// stopping sharing or start sharing failed.
 		sharingStatus.setText("Sharing status : [stopping ...]");
 		sharingStatus.revalidate();
 		stopSharing();
 		setTooltipHostname(null);
 		sharingStatus.setText("Sharing status : [not running]");
 		shareButton.setText("Start sharing");
 	}
 
 	public boolean startSharing(boolean shouldPopup) {
 		// HashSet<String> sharedDB = new HashSet<String>();
 		Collection<String> sharedMaps = mapList.getSelectedTilesSetFiles();
 		System.out.println("should start sharing the maps, with " + (shouldPopup ? "popup" : "no popup") + " in case of problem");
 		// generate the xml;
 		int port = ((SpinnerNumberModel) portNumber.getModel()).getNumber().intValue();
 		try {
 			sharingManager.setPort(port);
 			sharingManager.setSharingList(sharedMaps);
 			sharingManager.startSharing();
 		} catch (Exception ex) {
 			try {
 				sharingManager.stopSharing();
 			} catch (Exception ex1) {
 				System.out.println("ex1 : ");
 				ex1.printStackTrace();
 			}
 			if (shouldPopup) {
 				JOptionPane
 						.showConfirmDialog(
 								this,
 								"<html><body>Error while starting the sharing component on port [" + port + "]: <br/><i>" + ex.getMessage() + "</i></body></html>",
 								"Error creating starting the sharing component", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
 			}
 			ex.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	public void stopSharing() {
 		try {
 			sharingManager.stopSharing();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param fileLocation
 	 *            This can be a temporary file.
 	 */
 	public void addDisplayableToShare(String fileLocation, String title) {
 		mapList.addDisplayable(fileLocation, title);
 	}
 
 	public void updateDisplayableLocation(String oldLocation, String newLocation) {
 		mapList.updateLocation(oldLocation, newLocation);
 	}
 
 	public Map<String, String> getDisplayableList(String rootDir, String[] maps) {
 		Map<String, String> fileToName = new HashMap<String, String>();
 		if (!rootDir.endsWith(File.separator)) {
 			rootDir += File.separator;
 		}
 		for (String string : maps) {
 			try {
 				String fileName = rootDir + string;
 				System.out.println("trying to open the map : " + fileName);
 				Connection mapDB = DriverManager.getConnection("jdbc:sqlite:" + fileName);
 				mapDB.setReadOnly(true);
 				Statement statement = mapDB.createStatement();
 				statement.setQueryTimeout(30); // set timeout to 30 sec.
 				ResultSet rs = statement.executeQuery("select " + Ref.infos_title + " from infos");
 				while (rs.next()) {
 					String name = rs.getString(Ref.infos_title);
 					System.out.println("name : " + name);
 					fileToName.put(fileName, name);
 				}
 				if (mapDB != null) mapDB.close();
 			} catch (Exception ex) {
 				System.err.println("ex for map : " + string);
 				ex.printStackTrace();
 			}
 		}
 		return fileToName;
 	}
 
 	public class LocalHostChecker extends TimerTask {
 		String hostname = "bla";
 		String localHost = "notBla";
 
 		public void run() {
 			try {
 				localHost = InetAddress.getLocalHost().getCanonicalHostName();
 				if (!hostname.equals(localHost)) {
 					hostname = localHost;
 					if (sharingManager.isSharing()) {
 						setTooltipHostname(hostname);
 					}
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 }
