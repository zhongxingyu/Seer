 package biz.behnke.dupchecker;
 
 import biz.behnke.dupchecker.renderer.QuotesListCellRenderer;
 import biz.behnke.dupchecker.renderer.SeparatorListCellRenderer;
 import biz.behnke.dupchecker.renderer.FileNameListCellRenderer;
 import biz.behnke.dupchecker.listener.WindowClosingAdapter;
 import biz.behnke.dupchecker.listener.ChooseFileActionListener;
 import biz.behnke.dupchecker.listener.WindowPropertyChangeListener;
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 import com.googlecode.streamflyer.core.Modifier;
 import com.googlecode.streamflyer.core.ModifyingReader;
 import com.googlecode.streamflyer.regex.RegexModifier;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 public class DupChecker extends JFrame {
 
 	protected JTextArea log = new JTextArea("\nREADME\n======\n"
 			+ "Die Ausgabedatei enthält alle Daten aus Datei 2, die nicht in Datei 1 vorhanden sind.\n\n");
 
 	protected JComboBox file_input_old;
 	protected JComboBox file_input_new;
 	protected JComboBox file_output;
 
 	protected JComboBox
 			encoding_old, separator_old, quote_old,
 			encoding_new, separator_new, quote_new,
 			encoding_out, separator_out, quote_out;
 
 	protected JList moreOutput = new JList();
 
 	protected String lastdir = "";
 
 	File iniFile;
 	Properties applicationProperties = new Properties();
 
 	/**
 	 * set up combo boxes with their default values
 	 */
 	protected void initComboBoxes() {
 		String[] encodings = new String[]{"CP865", "UTF-8", "ISO-8859-1"};
 		String[] separators = new String[]{"\t", ",", ";"};
 		String[] quotes = new String[]{"\0", "\"", "'"};
 
 		String filelist_in_1 = getProperty("filelist_in_1", "");
 		String filelist_in_2 = getProperty("filelist_in_2", "");
 		String filelist_out = getProperty("filelist_out", "");
 
 		int filelist_in_1_selected = Integer.parseInt(getProperty("filelist_in_1_selected", "0"));
 		int filelist_in_2_selected = Integer.parseInt(getProperty("filelist_in_2_selected", "0"));
 		int filelist_out_selected = Integer.parseInt(getProperty("filelist_out_selected", "0"));
 
 		if (!filelist_in_1.equals("")) {
 			file_input_old = new JComboBox(new DefaultComboBoxModel(filelist_in_1.split("\\|")));
 			file_input_old.setSelectedIndex(filelist_in_1_selected);
 		} else {
 			file_input_old = new JComboBox(new DefaultComboBoxModel());
 		}
 
 		if (!filelist_in_2.equals("")) {
 			file_input_new = new JComboBox(new DefaultComboBoxModel(filelist_in_2.split("\\|")));
 			file_input_new.setSelectedIndex(filelist_in_2_selected);
 		} else {
 			file_input_new = new JComboBox(new DefaultComboBoxModel());
 		}
 
 		if (!filelist_out.equals("")) {
 			file_output = new JComboBox(new DefaultComboBoxModel(filelist_out.split("\\|")));
 			file_output.setSelectedIndex(filelist_out_selected);
 		} else {
 			file_output = new JComboBox(new DefaultComboBoxModel());
 		}
 
 
 		if (filelist_in_1.length() == 0) {
 			file_input_new.setEnabled(false);
 		}
 
 		if (filelist_in_2.length() == 0) {
 			file_output.setEnabled(false);
 		}
 
 		encoding_old = new JComboBox(encodings);
 		separator_old = new JComboBox(separators);
 		separator_old.setRenderer(new SeparatorListCellRenderer());
 		quote_old = new JComboBox(quotes);
 		quote_old.setRenderer(new QuotesListCellRenderer());
 
 		encoding_new = new JComboBox(encodings);
 		separator_new = new JComboBox(separators);
 		separator_new.setRenderer(new SeparatorListCellRenderer());
 		quote_new = new JComboBox(quotes);
 		quote_new.setRenderer(new QuotesListCellRenderer());
 
 		encoding_out = new JComboBox(encodings);
 		separator_out = new JComboBox(separators);
 		separator_out.setRenderer(new SeparatorListCellRenderer());
 		quote_out = new JComboBox(quotes);
 		quote_out.setRenderer(new QuotesListCellRenderer());
 	}
 
 	public DupChecker() {
 		super("Duplicate Finder - Naturkost Nord");
 		initialize();
 	}
 
 	@Override
 	protected void frameInit() {
 		super.frameInit();
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new BorderLayout());
 
 		MenuBar mb = new MenuBar();
 		Menu menu = new Menu("Info");
 		MenuItem menu_item_about = new MenuItem("Über");
 		final JFrame clone = this;
 		menu_item_about.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(clone, "Dieser Programm dienst dem Abgleich zweier CSV Dateien", "Über dieses Programm", JOptionPane.INFORMATION_MESSAGE);
 			}
 		});
 		menu.add(menu_item_about);
 		mb.add(menu);
 		setMenuBar(mb);
 	}
 
 	/**
 	 * dirty solution but works
 	 */
 	protected void setMargin() {
 		add(new JLabel(" "), BorderLayout.NORTH);
 		add(new JLabel(" "), BorderLayout.SOUTH);
 		add(new JLabel("   "), BorderLayout.EAST);
 		add(new JLabel("   "), BorderLayout.WEST);
 	}
 
 	/**
 	 * prints cvs row to log output
 	 *
 	 * @param String[] next
 	 */
 	protected void showExample(String[] next) {
 		addlog("Beispielausgabe:");
 		for (int i = 0; i <next.length; i++) {
 			addlog("\t"+i+": "+next[i]);
 		}
 		addlog("");
 	}
 
 	public String getLastdir() {
 		return lastdir;
 	}
 
 	public void setLastdir(String newdir) {
 		lastdir = newdir;
 	}
 
 	protected void resetLastWindowState() {
 		int x = Integer.parseInt(getProperty("window.left", "10"));
 		int y = Integer.parseInt(getProperty("window.top", "10"));
 		int w = Integer.parseInt(getProperty("window.width", "800"));
 		int h = Integer.parseInt(getProperty("window.height", "600"));
 
 		this.setBounds(x, y, w, h);
 
 		int s = Integer.parseInt(getProperty("window.state", "" + JFrame.MAXIMIZED_BOTH));
 		this.setExtendedState(s);
 	}
 
 	private void initialize() {
 		setupApplicationLocalData();
 		resetLastWindowState();
 
 		setMargin();
 
 		JPanel maincontainer = new JPanel(new BorderLayout());
 
 		/*
 		 * top panel
 		 */
 		JPanel top = new JPanel(new GridLayout(3, 8));
 
 		initComboBoxes();
 
 		// OLDDATA
 		file_input_old.setRenderer(new FileNameListCellRenderer());
 		top.add(file_input_old);
 		JButton btn_input_old = new JButton("Datei 1");
 		top.add(btn_input_old);
 		top.add(new JLabel("Zeichensatz", SwingConstants.CENTER));
 		top.add(encoding_old);
 		top.add(new JLabel("Trennzeichen", SwingConstants.CENTER));
 		top.add(separator_old);
 		top.add(new JLabel("Quotes", SwingConstants.CENTER));
 		top.add(quote_old);
 
 		// NEWDATA
 		file_input_new.setRenderer(new FileNameListCellRenderer());
 		top.add(file_input_new);
 		JButton btn_input_new = new JButton("Datei 2");
 		btn_input_new.setEnabled(file_input_old.getItemCount() > 0);
 		top.add(btn_input_new);
 		top.add(new JLabel("Zeichensatz", SwingConstants.CENTER));
 		top.add(encoding_new);
 		top.add(new JLabel("Trennzeichen", SwingConstants.CENTER));
 		top.add(separator_new);
 		top.add(new JLabel("Quotes", SwingConstants.CENTER));
 		top.add(quote_new);
 
 		// OUTPUTDATA
 		file_output.setRenderer(new FileNameListCellRenderer());
 		top.add(file_output);
 		JButton btn_output = new JButton("Ausgabedatei");
 		btn_output.setEnabled(file_input_new.getItemCount() > 0);
 		top.add(btn_output);
 		top.add(new JLabel("Zeichensatz", SwingConstants.CENTER));
 		top.add(encoding_out);
 		top.add(new JLabel("Trennzeichen", SwingConstants.CENTER));
 		top.add(separator_out);
 		top.add(new JLabel("Quotes", SwingConstants.CENTER));
 		top.add(quote_out);
 
 		Component[] dependingComponents = {btn_input_new, file_input_new};
 		btn_input_old.addActionListener(new ChooseFileActionListener(this, file_input_old, false, dependingComponents));
 
 		Component[] dependingComponents2 = {btn_output, file_output};
 		btn_input_new.addActionListener(new ChooseFileActionListener(this, file_input_new, false, dependingComponents2));
 
 		btn_output.addActionListener(new ChooseFileActionListener(this, file_output, true));
 
 		maincontainer.add(top, BorderLayout.NORTH);
 
 		/*
 		 * log panel
 		 */
 		JScrollPane log_scroll = new JScrollPane(this.log);
 		maincontainer.add(log_scroll, BorderLayout.CENTER);
 
 		/*
 		 * bottom panel
 		 */
 		JButton start = new JButton("Beginne den Abgleich");
 		start.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					process();
 				} catch (IOException e1) {
 					addlog("Fehler: "+e1.getMessage());
 				}
 			}
 
 		});
 		maincontainer.add(start, BorderLayout.SOUTH);
 
 		final DefaultListModel mymodel = new DefaultListModel();
 		mymodel.addElement("dupchecked=1");
 		moreOutput.setModel(mymodel);
 		moreOutput.setToolTipText("Double click to add more fields for output file; key=value");
 		moreOutput.addMouseListener(new MouseAdapter() {
 
 			@Override
 			public void mouseClicked(MouseEvent e) {
 
 				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
 					String result = JOptionPane.showInputDialog(moreOutput, "New key value pair:");
 					if (result != null && !result.equalsIgnoreCase("")) {
 						mymodel.addElement(result);
 					}
 					e.consume();
 				} else if (e.getButton() == MouseEvent.BUTTON3) {
 					if (moreOutput.getSelectedIndex() != -1) {
 						if (JOptionPane.showConfirmDialog(separator_new, "Delete '"+moreOutput.getSelectedValue()+"'") == JOptionPane.YES_OPTION) {
 							mymodel.remove(moreOutput.getSelectedIndex());
 						}
 					}
 					e.consume();
 				}
 			}
 
 		});
 
 //		JScrollPane field_scroll = new JScrollPane(this.moreOutput);
 		//maincontainer.add(field_scroll, BorderLayout.EAST);
 
 		/*
 		 * init overall panel
 		 */
 		add(maincontainer, BorderLayout.CENTER);
 
 		this.getContentPane().addHierarchyBoundsListener(new WindowPropertyChangeListener(this));
 		this.addWindowStateListener(new WindowPropertyChangeListener(this));
 		this.addWindowListener(new WindowClosingAdapter(this));
 	}
 
     protected Object unserializeObject( String serialized_patches1 ) {
         ByteArrayInputStream bis = new ByteArrayInputStream(serialized_patches1.getBytes());
 		ObjectInputStream oInputStream = null;
 		Object restored = null;
 		try {
 			oInputStream = new ObjectInputStream(bis);
 			restored = oInputStream.readObject();
 		} catch (ClassNotFoundException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (IOException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		} finally {
 			try {
 				oInputStream.close();
 			} catch (IOException ex) {
 				Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 		// patches1 equals restored_patches1
 
 		return restored;
     }
 
     protected String serializeObject( Serializable patches1 ) {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		ObjectOutputStream os = null;
 		String serialized_patches1 = "";
 		try {
 			os = new ObjectOutputStream(bos);
 			os.writeObject(patches1);
 			serialized_patches1 = bos.toString();
 		} catch (IOException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		} finally {
 			try {
 				os.close();
 			} catch (IOException ex) {
 				Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 
 		return serialized_patches1;
     }
 
 	/**
 	 * print a message to the log
 	 *
 	 * @param String msg
 	 */
 	protected void addlog(String msg) {
 		msg += "\n";
 		log.append(msg);
 	}
 
 	protected String[] buildColSelect(String[] next) {
 		String[] cols = new String[next.length];
 		for (int i = 0; i < cols.length; i++) {
 			cols[i] = ""+i;
 		}
 		return cols;
 	}
 
 	public String getFilelistIn1() {
 		int num_of_items = file_input_old.getItemCount();
 		String itemList = "";
 		for ( int i = 0;  i < num_of_items; i++) {
 			itemList += (String)file_input_old.getItemAt(i);
 			if (i < num_of_items - 1) {
 				itemList += "|";
 			}
 
 		}
 		return itemList;
 	}
 
 	public int getFilelistIn1Selected() {
 		return file_input_old.getSelectedIndex();
 	}
 
 	public String getFilelistIn2() {
 		int num_of_items = file_input_new.getItemCount();
 		String itemList = "";
 		for ( int i = 0;  i < num_of_items; i++) {
 			itemList += (String)file_input_new.getItemAt(i);
 			if (i < num_of_items - 1) {
 				itemList += "|";
 			}
 
 		}
 		return itemList;
 	}
 
 	public int getFilelistIn2Selected() {
 		return file_input_new.getSelectedIndex();
 	}
 
 	public String getFilelistOut() {
 		int num_of_items = file_output.getItemCount();
 		String itemList = "";
 		for ( int i = 0;  i < num_of_items; i++) {
 			itemList += (String)file_output.getItemAt(i);
 			if (i < num_of_items - 1) {
 				itemList += "|";
 			}
 
 		}
 		return itemList;
 	}
 
 	public int getFilelistOutSelected() {
 		return file_output.getSelectedIndex();
 	}
 
 	public String getProperty(String key, String defaultValue) {
 		String result = defaultValue;
 		try {
 			result = applicationProperties.getProperty(key, defaultValue);
 		} catch (Exception ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		return result;
 	}
 
 	public void updateProperty(String key, String value) {
 		try {
 			applicationProperties.setProperty(key, value);
 			applicationProperties.store(new FileOutputStream(iniFile), "application properties");
 		} catch (IOException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	protected void setupApplicationLocalData() {
 		String appdir = System.getProperty("user.home") + File.separator + ".dupchecker" + File.separator;
 		File f_appdir = new File(appdir);
 
 		if (!f_appdir.exists()) {
 			f_appdir.mkdir();
 		}
 
 		iniFile = new File(appdir + File.separator + "app.ini");
 
 
 		try {
 			if (!iniFile.exists()) {
 				iniFile.createNewFile();
 			}
 			applicationProperties.load(new FileInputStream(iniFile));
 		} catch (FileNotFoundException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (IOException ex) {
 			Logger.getLogger(DupChecker.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 	public static void main(String[] args) throws IOException {
 		DupChecker main = new DupChecker();
 		main.setVisible(true);
 	}
 
 	protected void process() throws IOException {
 
 		// INPUT file 1
 		BufferedReader bfr = new BufferedReader(new InputStreamReader(
 				new FileInputStream((String)file_input_old.getSelectedItem()), encoding_old.getSelectedItem().toString()));
 
 		Modifier myModifier = new RegexModifier(">", 0, "\t");
 		Reader modifyingReader = new ModifyingReader(bfr, myModifier);
 
 		String[] next;
 		String[] example = null;
 
 		HashMap<String, String[]> oldmap = new HashMap<String, String[]>(10000);
 		ArrayList templist = new ArrayList(10000);
 
 		CSVReader reader = new CSVReader(modifyingReader, separator_old.getSelectedItem().toString().charAt(0), quote_old.getSelectedItem().toString().charAt(0));
 		long time1 = System.currentTimeMillis();
 		addlog(String.format("Lese Datei 1 von '%s' mit dem Zeichensatz %s", (String)file_input_old.getSelectedItem(), encoding_old.getSelectedItem().toString()));
 
 		int key_column_old = 0;
 		int maxlength = 0;
 
 		while ((next = reader.readNext()) != null) {
 			next = removeUnwanted(next);
 			templist.add(next);
 
 			if (next.length > maxlength) {
 				maxlength = next.length;
 				example = next;
 			}
 		}
 		reader.close();
 
 		showExample(example);
 		if (JOptionPane.showConfirmDialog(this, "Beispielausgabe prüfen. Daten richtig erkannt?", "Prüfe Daten Datei 1", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
 			return;
 		}
 
 		Object result = JOptionPane.showInputDialog(this, "Spalte für den Abgleich wählen", "Spalte für den Abgleich wählen", JOptionPane.QUESTION_MESSAGE, null, buildColSelect(example), "0");
 		key_column_old = new Integer(result.toString()).intValue();
 
 		Iterator<String[]> it = templist.iterator();
 		while (it.hasNext()) {
 			next = it.next();
 			oldmap.put(next[key_column_old].trim(), next);
 		}
 
 		long time2 = System.currentTimeMillis();
 		addlog(String.format("Einlesen von Datei 1 (%d) dauerte %d ms, %d s", oldmap.size(), time2 - time1, (time2 - time1) / 1000));
 
 		HashMap<String, String[]> newmap = new HashMap<String, String[]>(10000);
 
 		// INPUT file 2
 		bfr = new BufferedReader(new InputStreamReader(new FileInputStream(
 				(String)file_input_new.getSelectedItem()), encoding_new.getSelectedItem().toString()));
 
 		myModifier = new RegexModifier(">", 0, "\t");
 		modifyingReader = new ModifyingReader(bfr, myModifier);
 
 		addlog(String.format("Lese Datei 2 von '%s' mit dem Zeichensatz %s", (String)file_input_new.getSelectedItem(), encoding_new.getSelectedItem().toString()));
 
 		reader = new CSVReader(modifyingReader, separator_new.getSelectedItem().toString().charAt(0), quote_new.getSelectedItem().toString().charAt(0));
 		time1 = System.currentTimeMillis();
 		long records = 0;
 //		boolean first = true;
 		int key_column_new = key_column_old;
 
 		// used for output file
 //		String[] column_data_2 = null;
 		while ((next = reader.readNext()) != null) {
 			next = removeUnwanted(next);
 			if (next.length != maxlength) {
 				continue;
 			}
 //			if (first) {
 //				column_data_2 = next;
 //				first = false;
 //			}
 			if (!oldmap.containsKey(next[key_column_new].trim())) {
 				newmap.put(next[key_column_new].trim(), next);
 			}
 			records++;
 		}
 		reader.close();
 		time2 = System.currentTimeMillis();
 		addlog(String.format("Processed %d datas. Found %d non-duplicates; took %d ms, %d s", records, newmap.size(), time2 - time1, (time2 - time1) / 1000));
 
 		// OUTPUT file
 		BufferedWriter bfw = new BufferedWriter(new OutputStreamWriter(
 				new FileOutputStream((String)file_output.getSelectedItem()), encoding_out.getSelectedItem().toString()));
 		CSVWriter writer = new CSVWriter(bfw, separator_out.getSelectedItem().toString().charAt(0), quote_out.getSelectedItem().toString().charAt(0));
 
 		// headline
 		ArrayList<String> l;
 		l = new ArrayList<String>();
 
 		String[] headline = new String[2];
 
		File infile1 = new File((String)file_input_new.getSelectedItem());
		File infile2 = new File((String)file_input_old.getSelectedItem());
 
 		headline[1] = "";
 		headline[1] = "Daten aus " + infile2.getName() + " nicht in " + infile1.getName();
 
 		l.addAll(Arrays.asList(headline));
 		String[] temp = l.toArray(new String[l.size()]);
 		writer.writeNext(temp);
 
 		l.clear();
 
 		// data
 		addlog(String.format("Schreibe Ausgabedatei nach '%s' mit Zeichensatz %s", (String)file_output.getSelectedItem(), encoding_out.getSelectedItem().toString()));
 
 		TreeMap<String, String[]> writemap = new TreeMap<String, String[]>(new NaturalOrderComparator());
 		writemap.putAll(newmap);
 
 		it = writemap.values().iterator();
 		while (it.hasNext()) {
 			l.clear();
 			next = (String[])it.next();
 
 			l.addAll(Arrays.asList(next));
 //			l.addAll(Arrays.asList(moreData));
 
 			writer.writeNext(l.toArray(new String[l.size()]));
 		}
 		writer.close();
 		addlog("Fertig.");
 
 	}
 
 	private String[] removeUnwanted(String[] row) {
 		for (int i = 0; i < row.length; i++) {
 			row[i] = row[i].replace("_|_|_|.", "");
 		}
 
 		return row;
 	}
 
 }
