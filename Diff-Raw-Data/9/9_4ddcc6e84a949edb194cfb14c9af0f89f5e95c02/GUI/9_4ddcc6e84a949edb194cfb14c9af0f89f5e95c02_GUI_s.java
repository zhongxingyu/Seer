 package gam;
 
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.sound.midi.MidiSystem;
 import javax.sound.midi.Sequencer;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.table.DefaultTableModel;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class GUI implements WindowListener {
 
 	// constants
 	private static final String WINDOW_TITLE = "Backing Track Generator";
 	private static final int TABLE_ROW_HEIGHT = 40;
	private static final String ICONS_FOLDER = 	"icons";
 	private static final int DEFAULT_TABLE_ROWS = 4;
 	private static final int DEFAULT_TABLE_COLUMNS = 4;
 
 	// string arrays
 	private final static String[] keys = { "C", "D", "E", "F", "G", "A", "B" };
 	private final static String[] accidentals = { "b", " ", "#" }; 
 
 	// button icons	
 	private final static String playIcon 		= ICONS_FOLDER + "\\play_small.png";
 	private final static String pauseIcon 		= ICONS_FOLDER + "\\pause_small.png";
 	private final static String stopIcon 		= ICONS_FOLDER + "\\stop_small.png";
 	private final static String generateIcon 	= ICONS_FOLDER + "\\generate.png";
 	private final static String saveIcon 		= ICONS_FOLDER + "\\save.png";
 	private final static String exportIcon 		= ICONS_FOLDER + "\\export2.png";
 	private final static String openIcon 		= ICONS_FOLDER + "\\open.png";
 	private final static String addIcon 		= ICONS_FOLDER + "\\add.png";
 	private final static String clearIcon 		= ICONS_FOLDER + "\\clear.png";
 
 	// ui stuff 
 	private JFrame frame;
 	private JSpinner spnBPM;
 	private JSpinner spnLoopCount; 
 	private JComboBox cbbPatterns;
 	private JComboBox cbbGenres;
 	private JComboBox cbbKeys;
 	private JComboBox cbbAccidentals;
 	private JCheckBox cbLoop;
 	private JComboBox cbbMajor; 
 	private JScrollPane scrollPane; 
 	private JFileChooser fileChooser;
 	private JTable table;
 	private DefaultTableModel tableModel;
 
 	// other
 	private static Api app;
 	private String patterns[];
 	private Progression prog;
 	private boolean paused = false;
 	private static String genre = "Jazz";
 
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {					
 					app = new Api();
 					app.loadXML();
 					app.loadSongs();
 					app.openMidiDevice();
 
 					GUI window = new GUI();
 					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 					window.frame.setVisible(true);
 
 
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public GUI() {
 		initialize();
 	}
 
 	public void start() {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		frame.setVisible(true);		
 	}
 
 	private void initialize() {
 
 		build();
 
 		buildFrame();
 
 		buildPlay();
 		buildPause();
 		buildStop();
 
 		buildOpen();
 		buildSave();
 		buildExport();
 
 		buildGenerate();
 
 		buildKey();
 		buildBPM();
 		buildPattern();
 		buildLoop();
 
 		buildTable();
 
 		buildAdd();
 		buildClear();
 
 		buildGenres();
 
 		buildLabels();
 
 	}
 
 
 	private void buildFrame() {
 		frame = new JFrame();
 		frame.setTitle(WINDOW_TITLE);
 		frame.setBounds(100, 100, 655, 490);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 	}
 
 	private void build() {
 		
 		patterns = new String[Api.getPatterns().size()];
 		for (int i = 0; i < Api.getPatterns().size(); i++) {
 			Pattern p = Api.getPatterns().get(i);
 			patterns[i] = p.getName();
 		}			
 
 		fileChooser = new JFileChooser();
 	}
 
 	private void buildPlay() {
 
 		JButton btnPlay = new JButton("");
 		btnPlay.setIcon(new ImageIcon(playIcon));
 		btnPlay.setBounds(17, 21, 32, 32);
 		frame.getContentPane().add(btnPlay);
 		btnPlay.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (!containsChords()) {
 					JOptionPane.showMessageDialog(frame, "nothing to play...");
 					return;
 				}
 
 				if (paused) {
 					paused = false;
 					app.resume();					
 					return;
 				}
 				
 //				app.playCountIn();
 //				return;
 				
 				// bpms
 				int bpm = (Integer) spnBPM.getValue();
 				app.setBPM(bpm);
 				
 //				float f = (float) (60.0 / bpm);
 //				float f2 = f*4*1000;
 //				int fi = (int)f2;
 //				try {
 //					Thread.sleep(fi);
 //				} catch (InterruptedException e1) {
 //					e1.printStackTrace();
 //				}
 
 				buildProgression();
 
 				// loop count
 				if (cbLoop.isSelected()) {
 					app.sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
 				}
 
 				else {
 					int iLoopCount = (Integer) spnLoopCount.getValue(); 
 					app.sequencer.setLoopCount(iLoopCount);
 				}
 
 				// actually play
 				app.play();
 			}
 		});
 	}
 
 	private void buildExport() {
 
 		JButton btnExport = new JButton("");
 		btnExport.setIcon(new ImageIcon(exportIcon));
 		btnExport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				if (!containsChords()) {
 					JOptionPane.showMessageDialog(frame, "nothing to export...");	
 					return;
 				}
 
 				int rval = fileChooser.showSaveDialog(frame);
 				if (rval == JFileChooser.APPROVE_OPTION) {
 					File file = fileChooser.getSelectedFile();				
 					try {
 						buildProgression();
 						MidiSystem.write(app.sequence, 1, file);
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		});
 
 		btnExport.setBounds(322, 11, 50, 50);
 		frame.getContentPane().add(btnExport);
 
 	}
 
 	private void buildSave() {
 
 		JButton btnSave = new JButton("");
 		btnSave.setIcon(new ImageIcon(saveIcon));
 		btnSave.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 
 				if (!containsChords()) {
 					JOptionPane.showMessageDialog(frame, "nothing to save...");
 					return;
 				}
 
 				ArrayList<String> chords = new ArrayList<String>();
 				for (int i = 0; i < tableModel.getRowCount(); i++) {
 					for (int j = 0; j < tableModel.getColumnCount(); j++) {
 						String s = (String) tableModel.getValueAt(i, j);
 						if (s != null) {
 							chords.add(s);
 						}
 					}
 				}
 
 				int rval = fileChooser.showSaveDialog(frame);
 				if (rval == JFileChooser.APPROVE_OPTION) {
 					File file = fileChooser.getSelectedFile();								
 					app.saveSong(chords, file);
 				}
 			}			
 		});
 		btnSave.setBounds(262, 11, 50, 50);
 		frame.getContentPane().add(btnSave);
 
 	}
 
 	private void buildPause() {
 
 		JButton btnPause = new JButton("");
 		btnPause.setIcon(new ImageIcon(pauseIcon));
 		btnPause.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (app.sequencer.isRunning()) {
 					app.pause();
 				}
 			}
 		});
 		btnPause.setBounds(70, 21, 32, 32);
 		frame.getContentPane().add(btnPause);
 
 	}
 
 	private void buildStop() {
 
 		JButton btnStop = new JButton("");
 		btnStop.setIcon(new ImageIcon(stopIcon));
 		btnStop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (app.sequencer.isRunning()) {
 					app.sequencer.stop();
 				}
 			}
 		});
 		btnStop.setBounds(130, 21, 32, 32);
 		frame.getContentPane().add(btnStop);
 
 	}
 
 	private void buildGenerate() {
 
 		JButton btnGenerate = new JButton("");
 		btnGenerate.setIcon(new ImageIcon(generateIcon));
 		btnGenerate.setBounds(532, 11, 50, 50);
 		btnGenerate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int rows = DEFAULT_TABLE_ROWS;
 				int cols = DEFAULT_TABLE_COLUMNS;
 				for (int i = 0; i < rows; i++) {
 					for (int j = 0; j < cols; j++) {
 						
 //						System.out.println(app.markovs.get(genre).map);
 						
 						String major = (String) cbbMajor.getSelectedItem();
 						String next;
 						if (i == 0 && j == 0) {
 							next = app.markovs.get(genre).getFirst(genre, major);							
 						}
 						else if (i == (DEFAULT_TABLE_COLUMNS-1) && j == (DEFAULT_TABLE_ROWS-1) ) {
 							if (major.equals("Major"))
 								next = "(0,1)";
 							else
 								next = "(0,2)";
 						}
 						else {
 							next = app.markovs.get(genre).getNext();
 						}
 												
 						Chord c = app.getMarkovChord(next);
 						int p = c.getRoot();
 						String let = Note.getLetter(p);
 						String sChord = let + c.getDef();
 						tableModel.setValueAt(sChord, i, j);
 					}
 				}
 			}						 
 		});
 		frame.getContentPane().add(btnGenerate);		
 	}
 
 	private void buildOpen() {
 
 		JButton btnOpen = new JButton("");
 		btnOpen.setIcon(new ImageIcon(openIcon));
 		btnOpen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg) {
 				int rval = fileChooser.showOpenDialog(frame);
 				if (rval == JFileChooser.APPROVE_OPTION) {
 					File file = fileChooser.getSelectedFile();								
 					loadSong(file);
 				}
 			}
 
 			private void loadSong(File file) {
 				try {
 					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 					dbf.setIgnoringElementContentWhitespace(true);
 					DocumentBuilder db = dbf.newDocumentBuilder();			
 					Document dom = db.parse(file);
 					dom.getDocumentElement().normalize();
 
 					NodeList songs = dom.getElementsByTagName("song");
 					Element elem = (Element) songs.item(0);
 					//					String name = elem.getAttribute("name");				
 					//					String key = elem.getAttribute("key");
 					String sProg = elem.getTextContent();
 					sProg = sProg.substring(sProg.indexOf(Api.CHORDS_DELIMITER), sProg.lastIndexOf(Api.CHORDS_DELIMITER)+1);
 
 					String tokens[] = sProg.split("\\" + Api.CHORDS_DELIMITER);
 
 					for (int i = 1; i < tokens.length; i++) {
 						String token = tokens[i].trim();
 						setChord(i-1, token);
 					}
 				}
 				catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 			private void setChord(int i, String s) {
 				int row = i / tableModel.getColumnCount();
 				int col = i % tableModel.getRowCount();
 				tableModel.setValueAt(s, row, col);
 			}
 		});
 		btnOpen.setBounds(202, 11, 50, 50);
 		frame.getContentPane().add(btnOpen);
 	}
 
 	private void buildLabels() {
 
 		JLabel lblPlay = new JLabel("Play");
 		lblPlay.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblPlay.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPlay.setBounds(10, 51, 46, 14);
 		frame.getContentPane().add(lblPlay);
 
 		JLabel lblPause = new JLabel("Pause");
 		lblPause.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblPause.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPause.setBounds(63, 51, 46, 14);
 		frame.getContentPane().add(lblPause);
 
 		JLabel lblStop = new JLabel("Stop");
 		lblStop.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblStop.setHorizontalAlignment(SwingConstants.CENTER);
 		lblStop.setBounds(123, 51, 46, 14);
 		frame.getContentPane().add(lblStop);
 
 		JLabel lblGenerate = new JLabel("Generate");
 		lblGenerate.setFont(new Font("Tahoma", Font.BOLD, 10));
 		lblGenerate.setHorizontalAlignment(SwingConstants.CENTER);
 		lblGenerate.setBounds(534, 64, 46, 14);
 		frame.getContentPane().add(lblGenerate);
 
 		JLabel lblSave = new JLabel("Save");
 		lblSave.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblSave.setHorizontalAlignment(SwingConstants.CENTER);
 		lblSave.setBounds(264, 64, 46, 14);
 		frame.getContentPane().add(lblSave);
 
 		JLabel lblExport = new JLabel("Export");
 		lblExport.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblExport.setHorizontalAlignment(SwingConstants.CENTER);
 		lblExport.setBounds(324, 64, 46, 14);
 		frame.getContentPane().add(lblExport);
 
 		JLabel lblOpen = new JLabel("Open");
 		lblOpen.setHorizontalAlignment(SwingConstants.CENTER);
 		lblOpen.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblOpen.setBounds(204, 64, 46, 14);
 		frame.getContentPane().add(lblOpen);
 
 		JLabel lblBpm = new JLabel("BPM");
 		lblBpm.setBounds(50, 120, 32, 14);
 		frame.getContentPane().add(lblBpm);
 
 		JLabel lblPattern = new JLabel("Pattern");
 		lblPattern.setBounds(213, 95, 67, 14);
 		frame.getContentPane().add(lblPattern);
 
 		JLabel lblAddBar = new JLabel("Add Bars");
 		lblAddBar.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblAddBar.setHorizontalAlignment(SwingConstants.CENTER);
 		lblAddBar.setBounds(43, 417, 46, 14);
 		frame.getContentPane().add(lblAddBar);
 
 		JLabel lblClear = new JLabel("Clear");
 		lblClear.setFont(new Font("Tahoma", Font.PLAIN, 10));
 		lblClear.setHorizontalAlignment(SwingConstants.CENTER);
 		lblClear.setBounds(96, 417, 46, 14);
 		frame.getContentPane().add(lblClear);
 
 		JLabel lblKey = new JLabel("Key");
 		lblKey.setBounds(50, 95, 32, 14);
 		frame.getContentPane().add(lblKey);
 
 		JLabel lblLoop = new JLabel("Loop");
 		lblLoop.setBounds(213, 120, 67, 14);
 		frame.getContentPane().add(lblLoop);
 		
 		JLabel lblGenre = new JLabel("Genre");
 		lblGenre.setHorizontalAlignment(SwingConstants.LEFT);
 		lblGenre.setBounds(415, 95, 39, 14);
 		frame.getContentPane().add(lblGenre);
 		
 		//TODO major TODO
 		cbbMajor = new JComboBox(new String[]{"Major", "Minor"});
 		cbbMajor.setSelectedIndex(0);
 		cbbMajor.setBounds(464, 117, 75, 20);
 		frame.getContentPane().add(cbbMajor);
 		
 		JLabel lblMajmin = new JLabel("Maj/Min");
 		lblMajmin.setHorizontalAlignment(SwingConstants.LEFT);
 		lblMajmin.setBounds(408, 120, 46, 14);
 		frame.getContentPane().add(lblMajmin);
 
 	}
 
 	private void buildTable() {
 
 		table = new JTable();
 		tableModel = new DefaultTableModel(new String[]{"", "", "", ""}, DEFAULT_TABLE_ROWS);
 		table.setModel(tableModel);
 		table.setTableHeader(null);
 
 		table.setRowHeight(TABLE_ROW_HEIGHT);
 
 		scrollPane = new JScrollPane();
 		scrollPane.setBounds(50, 160, 532, 208);
 		scrollPane.setViewportView(table);
 		frame.getContentPane().add(scrollPane);	
 
 	}
 
 	private void buildAdd() {
 
 		JButton btnAdd = new JButton("");
 		btnAdd.setIcon(new ImageIcon(addIcon));
 		btnAdd.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tableModel.addRow(new String[]{"", "", "", ""});
 			}
 		});
 		btnAdd.setBounds(50, 385, 32, 32);
 		frame.getContentPane().add(btnAdd);
 	}
 
 	private void buildClear() {
 
 		JButton btnClear = new JButton("");
 		btnClear.setIcon(new ImageIcon(clearIcon));
 		btnClear.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int rows;
 				while ( (rows = tableModel.getRowCount()) > 4) {
 					tableModel.removeRow(rows-1);
 				}
 
 				for (int i = 0; i < tableModel.getRowCount(); i++)
 					for (int j = 0; j < tableModel.getColumnCount(); j++)
 						tableModel.setValueAt(null, i, j);
 			}
 		});
 		btnClear.setBounds(103, 385, 32, 32);
 		frame.getContentPane().add(btnClear);
 	}
 
 	private void buildKey() {
 
 		cbbKeys = new JComboBox(keys);
 		cbbKeys.setBounds(103, 92, 39, 20);
 		frame.getContentPane().add(cbbKeys);
 		cbbKeys.setSelectedIndex(0);
 
 		cbbAccidentals = new JComboBox(accidentals);
 		cbbAccidentals.setBounds(143, 92, 32, 20);
 		frame.getContentPane().add(cbbAccidentals);
 		cbbAccidentals.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JComboBox cb = (JComboBox)e.getSource();
 				String sKey = (String)cb.getSelectedItem();
 				if (sKey.equals("b")) { 
 					app.key--;
 				}
 				else if (sKey.equals("#")) {
 					app.key++;
 				}
 			}
 		});
 		cbbAccidentals.setSelectedIndex(1);
 
 		cbbKeys.addActionListener(new ActionListener() {			
 			public void actionPerformed(ActionEvent e) {
 				JComboBox cb = (JComboBox)e.getSource();
 				String sKey = (String)cb.getSelectedItem();
 				int iKey = Note.getPitchClass(Note.getMidiValue(sKey));
 				app.key = iKey;
 			}
 		});
 
 	}
 
 	private void buildBPM() {
 		SpinnerNumberModel sm = new SpinnerNumberModel(Api.DEFAULT_BPM, 20, 200, 1);
 		spnBPM = new JSpinner(sm);
 		spnBPM.setBounds(103, 117, 46, 20);
 		frame.getContentPane().add(spnBPM);		
 	}
 
 	private void buildLoop() {
 
 		SpinnerModel sm = new SpinnerNumberModel(0, 0, 99, 1);
 		spnLoopCount = new JSpinner(sm);
 		spnLoopCount.setBounds(271, 117, 41, 20);
 		frame.getContentPane().add(spnLoopCount);
 
 		cbLoop = new JCheckBox("Forever");
 		cbLoop.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (cbLoop.isSelected())
 					spnLoopCount.setEnabled(false);
 				else
 					spnLoopCount.setEnabled(true);
 			}
 		});
 		cbLoop.setBounds(319, 116, 75, 23);
 		frame.getContentPane().add(cbLoop);
 
 	}
 
 	private void buildPattern() {
 		cbbPatterns = new JComboBox(patterns);
 		cbbPatterns.setBounds(271, 93, 123, 18);
 		cbbPatterns.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JComboBox cb = (JComboBox)e.getSource();
 				String sPattern = (String)cb.getSelectedItem();			
 				Pattern p = Api.getPattern(sPattern);
 				if (p.isMultiLine()) {
 					Api.pattern = new Pattern(sPattern, true);
 				}
 				else
 					Api.pattern = new Pattern(sPattern);
 			}		
 		});
 		frame.getContentPane().add(cbbPatterns);
 	}
 	
 	private void buildGenres() {
 		cbbGenres = new JComboBox(Api.getGenres().toArray());
 		cbbGenres.setBounds(464, 92, 115, 20);
 		cbbGenres.setSelectedIndex(0);
 		cbbGenres.addActionListener(new ActionListener() {		
 			public void actionPerformed(ActionEvent e) {
 				genre = (String) cbbGenres.getSelectedItem(); 
 			}
 		});
 		frame.getContentPane().add(cbbGenres);
 	}
 
 
 	public void buildProgression() {
 
 		prog = new Progression();	
 
 		for (int i = 0; i < tableModel.getRowCount(); i++) {
 			for (int j = 0; j < tableModel.getColumnCount(); j++) {
 				String s = (String) tableModel.getValueAt(i, j);
 				if (s != null) {					
 					try {
 						Chord c = new Chord(s);					
 						prog.addChord(c);
 					}
 					catch (Exception exc) {
 						exc.printStackTrace();
 					}
 				}
 			}
 		}
 
 		app.setProgression(prog);
 	}
 
 	public boolean containsChords() {
 		for (int i = 0; i < tableModel.getRowCount(); i++) {
 			for (int j = 0; j < tableModel.getColumnCount(); j++) {
 				String s = (String) tableModel.getValueAt(i, j);
 				if (s != null)
 					return true;
 			}
 		}
 		return false;
 	}
 
 	public void windowClosing(WindowEvent e) {
 		app.closeMidiDevice();
 	}
 
 	public void windowActivated(WindowEvent e) { }
 	public void windowClosed(WindowEvent e) { }
 	public void windowDeactivated(WindowEvent e) { }
 	public void windowDeiconified(WindowEvent e) { }
 	public void windowIconified(WindowEvent e) { }
 	public void windowOpened(WindowEvent e) { }	
 }
