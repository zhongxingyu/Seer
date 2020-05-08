 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.event.*;
 import java.text.*;
 import java.util.prefs.*;
 
 class SettingWindow extends JFrame {
 	DecimalFormat df = new DecimalFormat("####");
 	DecimalFormat nf = new DecimalFormat("####.#####");
     //NumberFormat nf = NumberFormat.getNumberInstance();
 	//NumberFormat inf = NumberFormat.getIntegerInstance();
 	JFormattedTextField currentOutFactorField = new JFormattedTextField(nf);
     JFormattedTextField particleOutFactorField = new JFormattedTextField(nf);
     JFormattedTextField harmonicsField = new JFormattedTextField(df);
     JFormattedTextField currentFactorField = new JFormattedTextField(nf);
     JFormattedTextField chargeFactorField = new JFormattedTextField(nf);
     JFormattedTextField chargeField = new JFormattedTextField(df);
     JFormattedTextField timming1Field = new JFormattedTextField(nf);
     JFormattedTextField timming2Field = new JFormattedTextField(nf);
 	JButton import_but = new JButton("読み込み");
 	JButton save_but = new JButton("保存");
 	JButton saveas_but = new JButton("別名で保存...");
 	File settingsSourceFile = null;
 	JLabel settingsFileField = new JLabel();
 	File settingsCurrentFile = new File("settings.txt");
 	String filename = "settings.txt";
 	
 	double currentOutFactor = 1;
 	double particleOutFactor = 1;
 	int harmonics = 1;
 	double currentFactor = 1;
 	double chargeFactor = 1;
 	int charge = 1;
 	float timming1 = 10;
 	float timming2 = 600;
     
     private static SettingWindow instance = null;
     private MonitorWindowUI monitorWindow;
     
     private SettingWindow(String title, MonitorWindowUI aMonitorWindow) {
         super(title);
         monitorWindow = aMonitorWindow;
 		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(400, 500);
         setBackground(Color.white);
 		//getContentPane().setLayout(new GridBagLayout());
 		getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
 		
 		JPanel pathpanel = new JPanel(new FlowLayout());
        pathpanel.setPreferredSize(new Dimension(400,50));
 		pathpanel.add(new JLabel("設定ファイル : "));
 		pathpanel.add(settingsFileField);
 		getContentPane().add(pathpanel);


 		GridBagLayout gbl_fields = new GridBagLayout();
 		JPanel fpanel = new JPanel(gbl_fields);
         
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.gridwidth = 1;
         gbc.gridheight = 1;
         gbc.insets = new Insets(10, 10, 10, 10);
         gbc.fill = GridBagConstraints.HORIZONTAL;
         
         String lblarray[] = {
             "電流 OUT 校正係数",
             "粒子数 OUT 校正係数",
             "タイミング 1",
             "タイミング 2",
             "電流 校正係数",
             "電荷 校正係数",
             "価数",
             "Harmonics"
         };
         
         JTextField fieldarray[] = {
             currentOutFactorField,
             particleOutFactorField,
             timming1Field,
             timming2Field,
             currentFactorField,
             chargeFactorField,
             chargeField,
             harmonicsField
         };
         TextFieldListener tlistner = new TextFieldListener();
         for (int i=0; i<lblarray.length; i++) {
             gbc.gridx = 0;
             gbc.gridy = i;
             gbc.gridwidth = 1;
 			fpanel.add(new JLabel(lblarray[i], Label.RIGHT), gbc);
             gbc.gridx = 1;
 			gbc.gridwidth = 2;
             fieldarray[i].setHorizontalAlignment(JTextField.RIGHT);
 			fieldarray[i].getDocument().addDocumentListener(tlistner);
             //fieldarray[i].setInputVerifier(new NumberInputVerifier());
 			fpanel.add(fieldarray[i], gbc);
         }
 		getContentPane().add(fpanel);
 		/*
 		gbc.gridx = 1;
 		gbc.gridy++;
 		gbc.gridwidth = 1;
 		gbc.fill = GridBagConstraints.NONE;
 		gbc.anchor = GridBagConstraints.EAST;
 		*/
 		JPanel buttonpanel = new JPanel();
 		//buttonpanel.setBackground(Color.red);
 		//buttonpanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 		BoxLayout bxl = new BoxLayout(buttonpanel, BoxLayout.X_AXIS);
 		buttonpanel.setLayout(bxl);
 		//but.setPreferredSize(new Dimension(150, but.getPreferredSize().height));
 		//but.setAlignmentX(Component.RIGHT_ALIGNMENT);
 		//but.setAlignmentX(1.0f);
 		//getContentPane().add(but, gbc);
 		buttonpanel.add(import_but);
 		import_but.addActionListener(new ReadAction());
 		buttonpanel.add(save_but);
 		save_but.addActionListener(new SaveAction());
 		buttonpanel.add(saveas_but);
 		saveas_but.addActionListener(new SaveAsAction());
 		getContentPane().add(buttonpanel);
 		readData();
 		save_but.setEnabled(false);
     }
     
 	class TextFieldAction implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			save_but.setEnabled(true);
 		}
 	}
 	
 	class TextFieldListener implements DocumentListener {
 		public void changedUpdate(DocumentEvent de){ updates(de); }
 		public void insertUpdate(DocumentEvent de){ updates(de); }
 		public void removeUpdate(DocumentEvent de){ updates(de); }
 		
 		public void updates(DocumentEvent de) {
 			save_but.setEnabled(true);
 		}
 	}
 	
 	public void setSettingsSource(File file) {
 		settingsSourceFile = file;
 		settingsFileField.setText(file.getAbsolutePath());
 	}
 	
 	class SaveAsAction implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
             Preferences prefs = Preferences.userNodeForPackage(this.getClass());
             String path = prefs.get("saveLocation", null);
 			JFileChooser filechooser = new JFileChooser(path);
             filechooser.setSelectedFile(new File("settings.txt"));
 			int selected = filechooser.showSaveDialog(((JComponent)e.getSource()).getTopLevelAncestor());
 			if (selected == JFileChooser.APPROVE_OPTION){
                 File file_to_save = filechooser.getSelectedFile();
 				if (!saveDataToSource(file_to_save)) {
                     return;
                 }
                 prefs.put("saveLocation", file_to_save.getParent());
                 try {
                     prefs.flush();
                 } catch (BackingStoreException ex) {
                     Logger.getLogger(SettingWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
 			}else if (selected == JFileChooser.CANCEL_OPTION){
 				System.out.println("キャンセルされました");
 			}else if (selected == JFileChooser.ERROR_OPTION){
 				System.out.println("エラー又は取消しがありました");
 			}
 			save_but.setEnabled(false);
 		}
 	}
 	
 	class SaveAction implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			if (saveDataToSource(settingsSourceFile)) {;
                 save_but.setEnabled(false);
             }
 		}
 	}
 
 	class ReadAction implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			Preferences prefs = Preferences.userNodeForPackage(this.getClass());
             String path = prefs.get("saveLocation", null);
             JFileChooser filechooser = new JFileChooser(path);
 			int selected = filechooser.showOpenDialog(((JComponent)e.getSource()).getTopLevelAncestor());
 			if (selected == JFileChooser.APPROVE_OPTION){
 				File file_to_read = filechooser.getSelectedFile();
                 setSettingsSource(file_to_read);
 				readDataFromSettingsSource();
                  prefs.put("saveLocation", file_to_read.getParent());
                 try {
                     prefs.flush();
                 } catch (BackingStoreException ex) {
                     Logger.getLogger(SettingWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
 			}else if (selected == JFileChooser.CANCEL_OPTION){
 				System.out.println("キャンセルされました");
 			}else if (selected == JFileChooser.ERROR_OPTION){
 				System.out.println("エラー又は取消しがありました");
 			}
 			save_but.setEnabled(false);
 		}
 	}
 	
 	public boolean readDataFromSettingsSource() {
 		BufferedReader br;
 		boolean result = false;
 		try {
 			br = new BufferedReader(new FileReader(settingsSourceFile));
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 			return false;
 		}
 		result = writeToCurrentSettings(br);
 		if (!result) return result;
         appendSettingsSourceInfo();
 		result = setDataWithReader(br);
 		if (!result) return result;
 		try {
 			br.close();
 		} catch (IOException e) {
 			System.out.println(e);
 			result = false;
 		}
 		return result;
 	}
 	
 	public void saveDataToFile(File fout) {
 		PrintStream ps;
 		try {
 			ps = new PrintStream(fout);
 		} catch (FileNotFoundException e) {
 			System.out.println(e);
 			return;
 		}
         String bs = System.getProperty("line.separator");
 		ps.format("%f      %s%s", currentOutFactor, "# current out factor", bs);
 		ps.printf("%f      %s%s", particleOutFactor, "# particles out factor", bs);
         ps.printf("%f      %s%s", timming1, "# timming 1", bs);
 		ps.printf("%f      %s%s", timming2, "# timming 2", bs);
 		ps.printf("%f      %s%s", currentFactor, "# current factor", bs);
 		ps.printf("%f      %s%s", chargeFactor, "# charge factor", bs);
 		ps.printf("%d      %s%s", charge, "# charge", bs);
 		ps.printf("%d      %s%s", harmonics, "# harmonics", bs);
         ps.close();
 	}
 
     private boolean updateDataFromWindow() {
         try {
         currentOutFactor = Double.parseDouble(currentOutFactorField.getText());
         particleOutFactor = Double.parseDouble(particleOutFactorField.getText());
         timming1 = Float.parseFloat(timming1Field.getText());
         timming2 = Float.parseFloat(timming2Field.getText());
         currentFactor = Double.parseDouble(currentFactorField.getText());
         chargeFactor = Double.parseDouble(chargeFactorField.getText());
         charge = Integer.parseInt(chargeField.getText());
         harmonics = Integer.parseInt(harmonicsField.getText());
         } catch(NumberFormatException e) {
             UIManager.getLookAndFeel().provideErrorFeedback(this);
             int ret = JOptionPane.showConfirmDialog (this, e, "入力が間違っています。", JOptionPane.DEFAULT_OPTION);
             System.out.println(e);
             return false;
         }
         return true;
     }
 
 	private void appendSettingsSourceInfo() {
 		FileOutputStream fos;
         try {
             fos = new FileOutputStream(settingsCurrentFile, true);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(SettingWindow.class.getName()).log(Level.SEVERE, null, ex);
             return;
         }
         BufferedWriter fp = new BufferedWriter(new OutputStreamWriter(fos));
         try {
             fp.write(settingsSourceFile.getPath());
             fp.newLine();
             fp.flush();
             fp.close();
             fos.flush();
             fos.close();
         } catch (IOException ex) {
             Logger.getLogger(SettingWindow.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public boolean saveDataToSource(File newsource) {
         if (!updateDataFromWindow()) {
             return false;
         }
         saveDataToFile(settingsCurrentFile);
 		if (newsource != null) {
             saveDataToFile(newsource);
             setSettingsSource(newsource);
             appendSettingsSourceInfo();
 		}
         return true;
 	}
 	
     public static SettingWindow getInstance(MonitorWindowUI monitorWindow) {
         if (instance == null) {
              instance = new SettingWindow("設定", monitorWindow);
         }
         return instance;
     }
     
 	private String parseLine(String aLine) {
 		String numpart = aLine;
 		int compos = aLine.indexOf("#");
 		if (compos > -1) {
 			numpart = aLine.substring(0, compos);
 		}
 		return numpart.trim();
 	}
 	
 	abstract class ValueSetter {
 		abstract void setValue(String aString);
 	}
 	
 	public void setCurrentOutFactor(String aValue) {
 		currentOutFactorField.setText(aValue);
 		currentOutFactor = Double.parseDouble(aValue);
 	}
 
 	public void setParticleOutFactor(String aValue) {
 		particleOutFactorField.setText(aValue);
 		particleOutFactor = Double.parseDouble(aValue);
 	}
 
 	public void setHarmonics(String aValue) {
 		harmonicsField.setText(aValue);
 		harmonics = Integer.parseInt(aValue);
 	}
 
 	public void setChargeFactor(String aValue) {
 		chargeFactorField.setText(aValue);
 		chargeFactor = Double.parseDouble(aValue);
 	}
 
 	public void setCurrentFactor(String aValue) {
 		currentFactorField.setText(aValue);
 		currentFactor = Double.parseDouble(aValue);
 	}
 
 	public void setCharge(String aValue) {
 		chargeField.setText(aValue);
 		charge = Integer.parseInt(aValue);
 	}
 
     public float getTimming1() {
         return timming1;
     }
 	public void setTimming1(String aValue) {
 		timming1Field.setText(aValue);
 		timming1 = Float.parseFloat(aValue);
 	}
 
     public float getTimming2() {
         return timming2;
     }
 	public void setTimming2(String aValue) {
 		timming2Field.setText(aValue);
 		timming2 = Float.parseFloat(aValue);
 	}
 	
 	private boolean setDataWithReader(BufferedReader reader) {
 		ValueSetter setters[] = {
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setCurrentOutFactor(aString);}}, 
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setParticleOutFactor(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setTimming1(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setTimming2(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setCurrentFactor(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setChargeFactor(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setCharge(aString);}},
 			new ValueSetter() {void setValue(String aString) {
 			SettingWindow.this.setHarmonics(aString);}}
         };
 		
 		try{
 			String str;
 			for (int i = 0; i<setters.length; i++) {
 				if ((str = reader.readLine()) == null) break;
 				setters[i].setValue(parseLine(str));
 			}
             if ((str = reader.readLine()) != null) {
                 File source_file = new File(str);
                 if (source_file.exists()) {
                     setSettingsSource(source_file);
                 }
             }
 			reader.close();		
 		} catch(IOException e) {
             System.out.println(e);
 			return false;
 		}
 		return true;		
 	}
 	
 	private boolean writeToCurrentSettings(BufferedReader reader) {
 		try {
 			reader.mark(1024);
 			BufferedWriter bw = new BufferedWriter(new FileWriter(settingsCurrentFile));
 			String str;
 			while (reader.ready()) {
 				if ((str = reader.readLine()) == null) break;
 				bw.write(str);
 				bw.newLine();
 			}
 			bw.close();
 			reader.reset();
 		} catch (IOException e) {
 			System.out.println(e);
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean readData() {
 		BufferedReader br;
 		if (settingsCurrentFile.isFile() && settingsCurrentFile.canRead()) {
 			try {
 				br = new BufferedReader(new FileReader(settingsCurrentFile));
 			} catch (FileNotFoundException e) {
 				System.out.println(e);
 				return false;
 			}
 		} else {
 			InputStream fs=SettingWindow.class.getResourceAsStream("default-settings.txt");
 			br = new BufferedReader(new InputStreamReader(fs));
 			try {
 				br.mark(1024);
 				BufferedWriter bw = new BufferedWriter(new FileWriter(settingsCurrentFile));
 				String str;
 				while (br.ready()) {
 					if ((str = br.readLine()) == null) break;
 					bw.write(str);
 					bw.newLine();
 				}
 				bw.close();
 				br.reset();
 			} catch (IOException e) {
 				System.out.println(e);
 				return false;
 			}
 		}
 		boolean result = setDataWithReader(br);
 		try {
 			br.close();
 		} catch (IOException e) {
 			System.out.println(e);
 			result = false;
 		}
         //monitorWindow.updateTimmings(timming1, timming2);
 		return result;
 	}
 	
     public boolean writeData() {
        return true;
     }
 }
