 package cz.cacek.ebook.packager;
 
 import cz.cacek.ebook.Common;
 
 import java.awt.Color;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JTextArea;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.filechooser.FileFilter;
 
 
 /**
  * New Swing UI for EBookME
  *
  * @author Josef Cacek [josef.cacek (at) gmail.com]
  * @author $Author: jiritusla $
  * @version $Revision: 1.18 $
  * @created $Date: 2008/07/03 16:08:17 $
  */
 public class NewEbGui
 	extends javax.swing.JPanel
 {
 	private static final long serialVersionUID = 105139301555996L;
 	private static final boolean USE_GZIP = true;
 	protected final static PropertyProvider props =
 		PropertyProvider.getInstance();
 	protected final static ResourceProvider res =
 		ResourceProvider.getInstance();
 	protected final JFileChooser fc = new JFileChooser(".");
 	protected final JFileChooser fs = new JFileChooser(".");
 	protected final JFileChooser fcProject = new JFileChooser(".");
 	protected ImageChooser imageChooser = new ImageChooser();
 	protected DefaultListModel listModel = new DefaultListModel();
 	protected Set idSet = new HashSet();
 	protected int bookNr = 1;
 	protected String bookId = null;
 	protected int oldIdx = -1;
 	protected boolean bookChanged = false;
 	protected transient PrintStream logStream;
 	protected boolean bookListListenerEnabled = true;
 	protected DocumentListener bookChangeListener =
 		new DocumentListener() {
 			public void changedUpdate(DocumentEvent e) {
 				bookChangedEvent();
 			}
 
 			public void insertUpdate(DocumentEvent e) {
 				bookChangedEvent();
 			}
 
 			public void removeUpdate(DocumentEvent e) {
 				bookChangedEvent();
 			}
 		};
 
 	// Variables declaration - do not modify//GEN-BEGIN:variables
 	private javax.swing.JButton addBtn;
 	private javax.swing.JCheckBox advancedCb;
 	private javax.swing.JPanel advancedPanel;
 	private javax.swing.JCheckBox autoformatCb;
 	private javax.swing.JList bookList;
 	private javax.swing.JTextField bookName;
 	private javax.swing.JTextArea bookTextArea;
 	private javax.swing.JPanel boooksPanel;
 	private javax.swing.JComboBox charsetCombo;
 	private javax.swing.JButton defaultSplashBtn;
 	private javax.swing.JButton deleteBtn;
 	private javax.swing.JTextField description;
 	private javax.swing.JPanel detailsPanel;
 	private javax.swing.JButton downBtn;
 	private javax.swing.JButton fileButton;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JScrollPane jScrollPane2;
 	private javax.swing.JScrollPane jScrollPane3;
 	private javax.swing.JScrollPane jScrollPane4;
 	private javax.swing.JLabel lblBookName;
 	private javax.swing.JLabel lblBufferSize;
 	private javax.swing.JLabel lblCharset;
 	private javax.swing.JLabel lblDescription;
 	private javax.swing.JLabel lblOutFile;
 	private javax.swing.JLabel lblText;
 	private javax.swing.JLabel lblWelcomeScreen;
 	private javax.swing.JPanel libPanel;
 	private javax.swing.JTextArea logTextArea;
 	private javax.swing.JButton openProjectBtn;
 	private javax.swing.JButton outFileBtn;
 	private javax.swing.JPanel outPanel;
 	private javax.swing.JTextField partSizeText;
 	private javax.swing.JCheckBox rightToLeftCb;
 	private javax.swing.JButton saveBtn;
 	private javax.swing.JButton saveChangesBtn;
 	private javax.swing.JButton saveProjectBtn;
 	private javax.swing.JTextField saveTextField;
 	private javax.swing.JButton splashBtn;
 	private javax.swing.JLabel splashLabel;
 	private javax.swing.JButton upBtn;
 
 	// End of variables declaration//GEN-END:variables
 
 	/** Creates new form NewEbGui */
 	public NewEbGui() {
 		initComponents();
 		final class JadFileFilter
 			extends FileFilter
 		{
 			public boolean accept(File f) {
 				return f.isDirectory()
 				|| f.getName().toUpperCase().endsWith(".JAD");
 			}
 
 			public String getDescription() {
 				return "Java ME application description (.jad)";
 			}
 		}
 
 		final JadFileFilter tmpJadFF = new JadFileFilter();
 		fs.addChoosableFileFilter(tmpJadFF);
 		fs.setAcceptAllFileFilterUsed(false);
 		final class EmeFileFilter
 			extends FileFilter
 		{
 			public boolean accept(File f) {
 				return f.isDirectory()
 				|| f.getName().toUpperCase().endsWith(".EME");
 			}
 
 			public String getDescription() {
 				return "EBookME project (.eme)";
 			}
 		}
 		fcProject.addChoosableFileFilter(new EmeFileFilter());
 
 		bookName.getDocument().addDocumentListener(bookChangeListener);
 		description.getDocument().addDocumentListener(bookChangeListener);
 		bookTextArea.getDocument().addDocumentListener(bookChangeListener);
 		saveTextField.getDocument().addDocumentListener(
 			new DocumentListener() {
 				public void changedUpdate(DocumentEvent e) {
 					saveTextFieldChange();
 				}
 
 				public void insertUpdate(DocumentEvent e) {
 					saveTextFieldChange();
 				}
 
 				public void removeUpdate(DocumentEvent e) {
 					saveTextFieldChange();
 				}
 			});
 
 		setLabels();
 		setAdvancedEnabled();
 		fillCharsets();
 		bookList.setModel(listModel);
 		partSizeText.setText(props.getProperty(Constants.PROP_PARTSIZE));
 		logStream = new PrintStream(new JTextAreaStream(logTextArea));
 		bookList.setTransferHandler(new BookTransferHandler(this));
 	}
 
 	protected void runEmulator() {
 		try {
 			Class.forName("org.microemu.app.Main");
 			MicroEmulatorBridge.run(
 				props.getProperty(Constants.PROP_OUT) + ".jad");
 		} catch (Throwable e) {
 		}
 	}
 
 	protected void saveTextFieldChange() {
 		saveTextField.setBackground(
 				(saveTextField.getText().length() > 0)
 				? bookList.getBackground() : Color.RED);
 		checkSaveBtn();
 	}
 
 	/** sets localized labels */
 	private void setLabels() {
 		lblCharset.setText(res.get("gui.label.encoding"));
 		lblCharset.setDisplayedMnemonic(
 				res.get("gui.label.encoding.mne").charAt(0));
 		lblBufferSize.setText(res.get("gui.label.partsize"));
 		lblBufferSize.setDisplayedMnemonic(
 				res.get("gui.label.partsize.mne").charAt(0));
 		lblBookName.setText(res.get("gui.label.textname"));
 		lblBookName.setDisplayedMnemonic(
 				res.get("gui.label.textname.mne").charAt(0));
 		lblDescription.setText(res.get("gui.label.textdescription"));
 		lblDescription.setDisplayedMnemonic(
 				res.get("gui.label.textdescription.mne").charAt(0));
 		lblText.setText(res.get("gui.label.text"));
 		lblText.setDisplayedMnemonic(res.get("gui.label.text.mne").charAt(0));
 		lblWelcomeScreen.setText(res.get("gui.label.splashscreen"));
 		lblOutFile.setText(res.get("gui.label.outprefix"));
 		lblOutFile.setDisplayedMnemonic(
 				res.get("gui.label.outprefix.mne").charAt(0));
 
 		autoformatCb.setText(res.get("gui.label.autoformat"));
 		autoformatCb.setMnemonic(res.get("gui.label.autoformat.mne").charAt(0));
 		advancedCb.setText(res.get("gui.label.advanced"));
 		advancedCb.setToolTipText(res.get("gui.tooltip.advanced"));
 		advancedCb.setMnemonic(res.get("gui.label.advanced.mne").charAt(0));
 		rightToLeftCb.setText(res.get("gui.label.right"));
 		rightToLeftCb.setMnemonic(res.get("gui.label.right.mne").charAt(0));
 		rightToLeftCb.setToolTipText(res.get("gui.tooltip.right"));
 
 		addBtn.setToolTipText(res.get("gui.tooltip.btn.add"));
 		deleteBtn.setToolTipText(res.get("gui.tooltip.btn.delete"));
 		openProjectBtn.setToolTipText(res.get("gui.tooltip.btn.openProject"));
 		saveProjectBtn.setToolTipText(res.get("gui.tooltip.btn.saveProject"));
 		upBtn.setToolTipText(res.get("gui.tooltip.btn.up"));
 		downBtn.setToolTipText(res.get("gui.tooltip.btn.down"));
 		fileButton.setToolTipText(res.get("gui.tooltip.btn.openbook"));
 		saveChangesBtn.setToolTipText(res.get("gui.tooltip.btn.savechanges"));
 		saveBtn.setToolTipText(res.get("gui.tooltip.btn.createjar"));
 		defaultSplashBtn.setToolTipText(res.get("gui.tooltip.btn.defsplash"));
 
 		boooksPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						" " + res.get("gui.panel.books") + " "));
 		detailsPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						" " + res.get("gui.panel.details") + " "));
 		libPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						" " + res.get("gui.panel.library") + " "));
 		advancedPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						" " + res.get("gui.panel.advanced") + " "));
 		outPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						" " + res.get("gui.panel.out") + " "));
 
 		outFileBtn.setText(res.get("gui.btn.chooseFile"));
 		outFileBtn.setMnemonic(res.get("gui.btn.chooseFile.mne").charAt(0));
 		splashBtn.setText(res.get("gui.btn.chooseImage"));
 		splashBtn.setMnemonic(res.get("gui.btn.chooseImage.mne").charAt(0));
 		saveBtn.setText(res.get("gui.btn.createJar"));
 		saveBtn.setMnemonic(res.get("gui.btn.createJar.mne").charAt(0));
 	}
 
 	protected void bookChangedEvent() {
 		if (!bookChanged) {
 			detailsPanel.setBorder(
 					javax.swing.BorderFactory.createTitledBorder(
 							res.get("gui.panel.details") + " *"));
 		}
 
 		final String tmpId = Common.createIdFromName(bookName.getText());
 
 		if ((bookName.getText() == null)
 				|| "".equals(bookName.getText().trim())
 				|| (idSet.contains(tmpId) && !tmpId.equals(bookId))) {
 			bookName.setBackground(Color.RED);
 			saveChangesBtn.setEnabled(false);
 		} else {
 			bookName.setBackground(description.getBackground());
 			saveChangesBtn.setEnabled(true);
 		}
 
 		bookChanged = true;
 	}
 
 	/** fills charset combobox with available character encodings */
 	private void fillCharsets() {
 		charsetCombo.removeAllItems();
 
 		try {
 			charsetCombo.addItem(System.getProperty("file.encoding"));
 		} catch (Exception e) {
 			// it's probably running from applet :-) - we don't have permissions
 			// to read system properties
 		}
 
 		try {
 			Map tmpCharsets =
 				(Map) Class.forName("java.nio.charset.Charset").getMethod(
 						"availableCharsets",
 						null).invoke(null, null);
 
 			for (Iterator it = tmpCharsets.values().iterator(); it.hasNext();) {
 				Object charset = it.next();
 				String tmpName =
 					(String) Class.forName("java.nio.charset.Charset").getMethod(
 							"displayName",
 							null).invoke(charset, null);
 				charsetCombo.addItem(tmpName);
 			}
 		} catch (Exception e) {
 			charsetCombo.addItem("ISO-8859-1");
 			charsetCombo.addItem("ISO-8859-2");
 			charsetCombo.addItem("ISO-8859-3");
 			charsetCombo.addItem("ISO-8859-4");
 			charsetCombo.addItem("ISO-8859-5");
 			charsetCombo.addItem("ISO-8859-6");
 			charsetCombo.addItem("ISO-8859-7");
 			charsetCombo.addItem("windows-1250");
 			charsetCombo.addItem("windows-1251");
 			charsetCombo.addItem("windows-1252");
 			charsetCombo.addItem("windows-1253");
 			charsetCombo.addItem("windows-1257");
 			charsetCombo.addItem("ASCII");
 			charsetCombo.addItem("UTF-8");
 			charsetCombo.addItem("MacCentralEurope");
 			charsetCombo.addItem("KOI8");
 		}
 	}
 
 	/** enables or disables components on advanced panel */
 	private void setAdvancedEnabled() {
 		final boolean tmpEnabled = advancedCb.isSelected();
 		autoformatCb.setEnabled(tmpEnabled);
 		partSizeText.setEnabled(tmpEnabled);
 		charsetCombo.setEnabled(tmpEnabled);
 		rightToLeftCb.setEnabled(tmpEnabled);
 
 		lblBufferSize.setEnabled(tmpEnabled);
 		lblCharset.setEnabled(tmpEnabled);
 	}
 
 	protected void alert(String message) {
 		logStream.println(
 			res.get("gui.status.error", new String[] { message }));
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
 	private void initComponents() {
 		java.awt.GridBagConstraints gridBagConstraints;
 
 		detailsPanel = new javax.swing.JPanel();
 		lblBookName = new javax.swing.JLabel();
 		bookName = new javax.swing.JTextField();
 		description = new javax.swing.JTextField();
 		lblDescription = new javax.swing.JLabel();
 		lblText = new javax.swing.JLabel();
 		jScrollPane2 = new javax.swing.JScrollPane();
 		bookTextArea = new javax.swing.JTextArea();
 		saveChangesBtn = new javax.swing.JButton();
 		fileButton = new javax.swing.JButton();
 		advancedPanel = new javax.swing.JPanel();
 		autoformatCb = new javax.swing.JCheckBox();
 		lblCharset = new javax.swing.JLabel();
 		partSizeText = new javax.swing.JTextField();
 		lblBufferSize = new javax.swing.JLabel();
 		charsetCombo = new javax.swing.JComboBox();
 		advancedCb = new javax.swing.JCheckBox();
 		rightToLeftCb = new javax.swing.JCheckBox();
 		boooksPanel = new javax.swing.JPanel();
 		jScrollPane1 = new javax.swing.JScrollPane();
 		bookList = new javax.swing.JList();
 		addBtn = new javax.swing.JButton();
 		deleteBtn = new javax.swing.JButton();
 		upBtn = new javax.swing.JButton();
 		downBtn = new javax.swing.JButton();
 		openProjectBtn = new javax.swing.JButton();
 		saveProjectBtn = new javax.swing.JButton();
 		libPanel = new javax.swing.JPanel();
 		saveBtn = new javax.swing.JButton();
 		lblOutFile = new javax.swing.JLabel();
 		saveTextField = new javax.swing.JTextField();
 		lblWelcomeScreen = new javax.swing.JLabel();
 		outFileBtn = new javax.swing.JButton();
 		splashBtn = new javax.swing.JButton();
 		defaultSplashBtn = new javax.swing.JButton();
 		jScrollPane4 = new javax.swing.JScrollPane();
 		splashLabel = new javax.swing.JLabel();
 		outPanel = new javax.swing.JPanel();
 		jScrollPane3 = new javax.swing.JScrollPane();
 		logTextArea = new javax.swing.JTextArea();
 
 		setLayout(new java.awt.GridBagLayout());
 
 		detailsPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder("Details"));
 
 		lblBookName.setLabelFor(bookName);
 		lblBookName.setText("Book name");
 
 		bookName.setEnabled(false);
 		bookName.addFocusListener(
 			new java.awt.event.FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent evt) {
 					bookNameFocusGained(evt);
 				}
 			});
 
 		description.setEnabled(false);
 		description.addFocusListener(
 			new java.awt.event.FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent evt) {
 					descriptionFocusGained(evt);
 				}
 			});
 
 		lblDescription.setLabelFor(description);
 		lblDescription.setText("Description");
 
 		lblText.setLabelFor(bookTextArea);
 		lblText.setText("Text");
 
 		bookTextArea.setColumns(20);
 		bookTextArea.setLineWrap(true);
 		bookTextArea.setRows(5);
 		bookTextArea.setWrapStyleWord(true);
 		bookTextArea.setEnabled(false);
 		jScrollPane2.setViewportView(bookTextArea);
 
 		saveChangesBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/button_ok16.png"))); // NOI18N
 		saveChangesBtn.setEnabled(false);
 		saveChangesBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		saveChangesBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					saveChangesBtnActionPerformed(evt);
 				}
 			});
 
 		fileButton.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/fileopen16.png"))); // NOI18N
 		fileButton.setEnabled(false);
 		fileButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		fileButton.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					fileButtonActionPerformed(evt);
 				}
 			});
 
 		org.jdesktop.layout.GroupLayout detailsPanelLayout =
 			new org.jdesktop.layout.GroupLayout(detailsPanel);
 		detailsPanel.setLayout(detailsPanelLayout);
 		detailsPanelLayout.setHorizontalGroup(
 				detailsPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						org.jdesktop.layout.GroupLayout.TRAILING,
 						detailsPanelLayout.createSequentialGroup()
 										  .addContainerGap().add(
 								detailsPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.TRAILING)
 												  .add(
 										org.jdesktop.layout.GroupLayout.LEADING,
 										jScrollPane2,
 										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 										240,
 										Short.MAX_VALUE).add(
 										org.jdesktop.layout.GroupLayout.LEADING,
 										detailsPanelLayout.createSequentialGroup()
 														  .add(lblBookName).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														  .add(
 											bookName,
 											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 											155,
 											Short.MAX_VALUE)).add(
 										org.jdesktop.layout.GroupLayout.LEADING,
 										detailsPanelLayout.createSequentialGroup()
 														  .add(lblDescription).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														  .add(
 											description,
 											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 											156,
 											Short.MAX_VALUE)).add(
 										org.jdesktop.layout.GroupLayout.LEADING,
 										lblText).add(
 										org.jdesktop.layout.GroupLayout.LEADING,
 										detailsPanelLayout.createSequentialGroup()
 														  .add(fileButton).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														  .add(saveChangesBtn)))
 										  .addContainerGap()));
 		detailsPanelLayout.setVerticalGroup(
 				detailsPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						detailsPanelLayout.createSequentialGroup().add(
 								detailsPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.BASELINE)
 												  .add(lblBookName).add(
 									bookName,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 									org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 										  .addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								detailsPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.BASELINE)
 												  .add(lblDescription).add(
 										description,
 										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 										org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 										  .addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								lblText).addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								jScrollPane2,
 								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 								94,
 								Short.MAX_VALUE).addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								detailsPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.TRAILING)
 												  .add(fileButton).add(
 										saveChangesBtn)).addContainerGap()));
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		add(detailsPanel, gridBagConstraints);
 
 		advancedPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 					"Advanced options"));
 
 		autoformatCb.setSelected(true);
 		autoformatCb.setText("Autoformat");
 		autoformatCb.setBorder(
 			javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		autoformatCb.setEnabled(false);
 		autoformatCb.setHorizontalTextPosition(
 			javax.swing.SwingConstants.LEADING);
 		autoformatCb.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					autoformatCbActionPerformed(evt);
 				}
 			});
 
 		lblCharset.setLabelFor(charsetCombo);
 		lblCharset.setText("Character encoding");
 		lblCharset.setEnabled(false);
 
 		partSizeText.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
 		partSizeText.setText("3000");
 		partSizeText.setEnabled(false);
 		partSizeText.addFocusListener(
 			new java.awt.event.FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent evt) {
 					partSizeTextFocusGained(evt);
 				}
 			});
 
 		lblBufferSize.setLabelFor(partSizeText);
 		lblBufferSize.setText("Buffer size");
 		lblBufferSize.setEnabled(false);
 
 		charsetCombo.setModel(
 				new javax.swing.DefaultComboBoxModel(
 						new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 		charsetCombo.setEnabled(false);
 		charsetCombo.addFocusListener(
 			new java.awt.event.FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent evt) {
 					charsetComboFocusGained(evt);
 				}
 			});
 
 		advancedCb.setText("Enable advanced options");
 		advancedCb.setBorder(
 			javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		advancedCb.setHorizontalTextPosition(
 			javax.swing.SwingConstants.LEADING);
 		advancedCb.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					advancedCbActionPerformed(evt);
 				}
 			});
 
 		rightToLeftCb.setText("Right to left text");
 		rightToLeftCb.setBorder(
 			javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		rightToLeftCb.setEnabled(false);
 		rightToLeftCb.setHorizontalTextPosition(
 			javax.swing.SwingConstants.LEADING);
 
 		org.jdesktop.layout.GroupLayout advancedPanelLayout =
 			new org.jdesktop.layout.GroupLayout(advancedPanel);
 		advancedPanel.setLayout(advancedPanelLayout);
 		advancedPanelLayout.setHorizontalGroup(
 				advancedPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						advancedPanelLayout.createSequentialGroup()
 										   .addContainerGap().add(
 								advancedPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.LEADING)
 												   .add(advancedCb).add(
 										autoformatCb).add(rightToLeftCb)).add(
 							15,
 							15,
 							15).add(
 								advancedPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.LEADING)
 												   .add(
 										advancedPanelLayout.createSequentialGroup()
 														   .add(lblCharset).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														   .add(
 											charsetCombo,
 											0,
 											0,
 											Short.MAX_VALUE)).add(
 										advancedPanelLayout.createSequentialGroup()
 														   .add(lblBufferSize).add(
 												48,
 												48,
 												48).add(
 											partSizeText,
 											org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 											78,
 											Short.MAX_VALUE))).addContainerGap()));
 		advancedPanelLayout.setVerticalGroup(
 				advancedPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						advancedPanelLayout.createSequentialGroup()
 										   .add(advancedCb).addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								advancedPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.BASELINE)
 												   .add(autoformatCb).add(
 										lblBufferSize).add(
 									partSizeText,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 									org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 										   .addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								advancedPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.BASELINE)
 												   .add(rightToLeftCb).add(
 										lblCharset).add(
 									charsetCombo,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 									22,
 									org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 										   .addContainerGap(
 								org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 								Short.MAX_VALUE)));
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		add(advancedPanel, gridBagConstraints);
 
 		boooksPanel.setBorder(
 			javax.swing.BorderFactory.createTitledBorder("Books"));
 		boooksPanel.setMinimumSize(new java.awt.Dimension(150, 0));
 		boooksPanel.setPreferredSize(new java.awt.Dimension(150, 10));
 
 		bookList.setSelectionMode(
 			javax.swing.ListSelectionModel.SINGLE_SELECTION);
 		bookList.addListSelectionListener(
 				new javax.swing.event.ListSelectionListener() {
 					public void valueChanged(
 						javax.swing.event.ListSelectionEvent evt) {
 						bookListValueChanged(evt);
 					}
 				});
 		jScrollPane1.setViewportView(bookList);
 
 		addBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/filenew16.png"))); // NOI18N
 		addBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		addBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					addBtnActionPerformed(evt);
 				}
 			});
 
 		deleteBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/stop16.png"))); // NOI18N
 		deleteBtn.setEnabled(false);
 		deleteBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		deleteBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					deleteBtnActionPerformed(evt);
 				}
 			});
 
 		upBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/up16.png"))); // NOI18N
 		upBtn.setEnabled(false);
 		upBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		upBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					upBtnActionPerformed(evt);
 				}
 			});
 
 		downBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/down16.png"))); // NOI18N
 		downBtn.setEnabled(false);
 		downBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		downBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					downBtnActionPerformed(evt);
 				}
 			});
 
 		openProjectBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/fileopen16.png"))); // NOI18N
 		openProjectBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		openProjectBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					openProjectBtnActionPerformed(evt);
 				}
 			});
 
 		saveProjectBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/filesave16.png"))); // NOI18N
 		saveProjectBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		saveProjectBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					saveProjectBtnActionPerformed(evt);
 				}
 			});
 
 		org.jdesktop.layout.GroupLayout boooksPanelLayout =
 			new org.jdesktop.layout.GroupLayout(boooksPanel);
 		boooksPanel.setLayout(boooksPanelLayout);
 		boooksPanelLayout.setHorizontalGroup(
 				boooksPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						boooksPanelLayout.createSequentialGroup()
 										 .addContainerGap().add(
 							jScrollPane1,
 							org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 							85,
 							org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(
 								org.jdesktop.layout.LayoutStyle.RELATED).add(
 								boooksPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.TRAILING)
 												 .add(
 										boooksPanelLayout.createParallelGroup(
 												org.jdesktop.layout.GroupLayout.LEADING)
 														 .add(
 												boooksPanelLayout.createParallelGroup(
 														org.jdesktop.layout.GroupLayout.TRAILING)
 																 .add(addBtn).add(
 														deleteBtn)).add(
 											downBtn)).add(upBtn)
 												 .add(openProjectBtn).add(
 										saveProjectBtn)).addContainerGap()));
 		boooksPanelLayout.setVerticalGroup(
 				boooksPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						boooksPanelLayout.createSequentialGroup().add(
 								boooksPanelLayout.createParallelGroup(
 										org.jdesktop.layout.GroupLayout.LEADING)
 												 .add(
 										boooksPanelLayout.createSequentialGroup()
 														 .add(addBtn).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														 .add(deleteBtn).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														 .add(openProjectBtn).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED,
 												org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 												Short.MAX_VALUE)
 														 .add(saveProjectBtn).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														 .add(upBtn).addPreferredGap(
 												org.jdesktop.layout.LayoutStyle.RELATED)
 														 .add(downBtn)).add(
 										jScrollPane1,
 										org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 										222,
 										Short.MAX_VALUE)).addContainerGap()));
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 0;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weighty = 1.0;
 		add(boooksPanel, gridBagConstraints);
 
 		libPanel.setBorder(
 			javax.swing.BorderFactory.createTitledBorder("Library"));
 		libPanel.setLayout(new java.awt.GridBagLayout());
 
 		saveBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/package32.png"))); // NOI18N
 		saveBtn.setText("Create JAR");
 		saveBtn.setEnabled(false);
 		saveBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					saveBtnActionPerformed(evt);
 				}
 			});
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 2;
 		gridBagConstraints.gridy = 2;
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 0);
 		libPanel.add(saveBtn, gridBagConstraints);
 
 		lblOutFile.setLabelFor(saveTextField);
 		lblOutFile.setText("Output file");
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		libPanel.add(lblOutFile, gridBagConstraints);
 
 		saveTextField.setBackground(java.awt.Color.red);
 		saveTextField.addFocusListener(
 			new java.awt.event.FocusAdapter() {
 				public void focusGained(java.awt.event.FocusEvent evt) {
 					saveTextFieldFocusGained(evt);
 				}
 			});
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.weightx = 2.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
 		libPanel.add(saveTextField, gridBagConstraints);
 
 		lblWelcomeScreen.setText("Welcome screen");
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		libPanel.add(lblWelcomeScreen, gridBagConstraints);
 
 		outFileBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/fileopen16.png"))); // NOI18N
 		outFileBtn.setText("Choose file");
 		outFileBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		outFileBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					outFileBtnActionPerformed(evt);
 				}
 			});
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
 		libPanel.add(outFileBtn, gridBagConstraints);
 
 		splashBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/fileopen16.png"))); // NOI18N
 		splashBtn.setText("Choose image");
 		splashBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		splashBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					splashBtnActionPerformed(evt);
 				}
 			});
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 2;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 0);
 		libPanel.add(splashBtn, gridBagConstraints);
 
 		defaultSplashBtn.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/back16.png"))); // NOI18N
 		defaultSplashBtn.setEnabled(false);
 		defaultSplashBtn.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		defaultSplashBtn.addActionListener(
 			new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					defaultSplashBtnActionPerformed(evt);
 				}
 			});
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 3;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 0);
 		libPanel.add(defaultSplashBtn, gridBagConstraints);
 
 		jScrollPane4.setBorder(null);
 
 		splashLabel.setIcon(
 				new javax.swing.ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/ebook.png"))); // NOI18N
 		splashLabel.setToolTipText("Image will not be resized!");
 		splashLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
 		jScrollPane4.setViewportView(splashLabel);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 1;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.gridheight = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		gridBagConstraints.insets = new java.awt.Insets(2, 5, 0, 0);
 		libPanel.add(jScrollPane4, gridBagConstraints);
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 1;
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		add(libPanel, gridBagConstraints);
 
 		outPanel.setBorder(
 			javax.swing.BorderFactory.createTitledBorder("Output"));
 
 		jScrollPane3.setEnabled(false);
 		jScrollPane3.setName("logTextArea"); // NOI18N
 
 		logTextArea.setColumns(20);
 		logTextArea.setEditable(false);
 		logTextArea.setLineWrap(true);
 		logTextArea.setRows(5);
 		logTextArea.setWrapStyleWord(true);
 		jScrollPane3.setViewportView(logTextArea);
 
 		org.jdesktop.layout.GroupLayout outPanelLayout =
 			new org.jdesktop.layout.GroupLayout(outPanel);
 		outPanel.setLayout(outPanelLayout);
 		outPanelLayout.setHorizontalGroup(
 				outPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						outPanelLayout.createSequentialGroup().addContainerGap()
 									  .add(
 							jScrollPane3,
 							org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 							390,
 							Short.MAX_VALUE).addContainerGap()));
 		outPanelLayout.setVerticalGroup(
 				outPanelLayout.createParallelGroup(
 						org.jdesktop.layout.GroupLayout.LEADING).add(
 						outPanelLayout.createSequentialGroup().add(
 							jScrollPane3,
 							org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 							94,
 							Short.MAX_VALUE).addContainerGap()));
 
 		gridBagConstraints = new java.awt.GridBagConstraints();
 		gridBagConstraints.gridx = 0;
 		gridBagConstraints.gridy = 3;
 		gridBagConstraints.gridwidth = 2;
 		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
 		gridBagConstraints.weightx = 1.0;
 		gridBagConstraints.weighty = 1.0;
 		add(outPanel, gridBagConstraints);
 	} // </editor-fold>//GEN-END:initComponents
 
 	private void downBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_downBtnActionPerformed
 		setBookPos(bookList.getSelectedIndex() + 1);
 	} //GEN-LAST:event_downBtnActionPerformed
 
 	private void upBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_upBtnActionPerformed
 		setBookPos(bookList.getSelectedIndex() - 1);
 	} //GEN-LAST:event_upBtnActionPerformed
 
 	private void saveProjectBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_saveProjectBtnActionPerformed
 		// Need to determine if this project has been saved to file before. 
 		final File tmpFile = fcProject.getSelectedFile();
 		if (tmpFile == null) {
 			// Project hasn't been saved to file before, so show the save dialog.
 			if (fcProject.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				saveProject(tmpFile);
 			}
 		} else {
 			// Project already has been saved to file once before, so just save to that file again
 			// without showing save dialog
 			saveProject(tmpFile);
 		}
 	} //GEN-LAST:event_saveProjectBtnActionPerformed
 	
 	/**
 	 * Called by saveProjectBtnActionPerformed() to save project to the file selected in fcProject 
 	 * @param tmpFile the file to save changes to (the one selected in fcProject)
 	 */
 	private void saveProject(File tmpFile) {
 		// Using this instead of checkChangesInBook() so that the current book is saved
 		// automatically and the user isn't confused
 		if (bookChanged && oldIdx > -1) {
 			saveBook();
 		}
 
 		String tmpPath = tmpFile.getPath();
 
 		if (!tmpPath.toUpperCase().endsWith(".EME")) {
 			tmpPath = tmpPath + ".eme";
 
 			//				fcProject.setSelectedFile(new File(tmpPath));
 		}
 
 		ProjectInfo tmpInfo = new ProjectInfo();
 
 		if (props.existsNotNull(Constants.PROP_SPLASH)) {
 			tmpInfo.setSplashPath(props.getProperty(Constants.PROP_SPLASH));
 		}
 
 		tmpInfo.setJadPath(saveTextField.getText());
 		tmpInfo.setBookCount(listModel.size());
 
 		try {
 			OutputStream tmpOs = new FileOutputStream(tmpPath);
 
 			if (USE_GZIP) {
 				tmpOs = new GZIPOutputStream(tmpOs);
 			}
 
 			tmpOs = new BufferedOutputStream(tmpOs);
 
 			final XMLEncoder tmpEncoder = new XMLEncoder(tmpOs);
 			tmpEncoder.writeObject(Common.VERSION);
 			tmpEncoder.writeObject(tmpInfo);
 
 			for (int i = 0, n = listModel.size(); i < n; i++) {
 				tmpEncoder.writeObject(listModel.get(i));
 			}
 
 			tmpEncoder.close();
 			JOptionPane.showMessageDialog(
 				this,
 				res.get("gui.message.projectSaved"));
 		} catch (Exception e) {
 			alert(
 				res.get("gui.alert.err", new String[] { e.getMessage() }));
 			e.printStackTrace(logStream);
 			logStream.flush();
 		}
 	}
 
 	private void openProjectBtnActionPerformed(java.awt.event.ActionEvent evt) { //GEN-FIRST:event_openProjectBtnActionPerformed
 
 		if (fcProject.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			final File tmpFile = fcProject.getSelectedFile();
 
 			try {
 				InputStream tmpIs = new FileInputStream(tmpFile);
 
 				if (USE_GZIP) {
 					tmpIs = new GZIPInputStream(tmpIs);
 				}
 
 				tmpIs = new BufferedInputStream(tmpIs);
 
 				final XMLDecoder tmpDecoder = new XMLDecoder(tmpIs);
 				final String tmpProjFileVersion =
 					(String) tmpDecoder.readObject();
 
 				if (Common.VERSION.equals(tmpProjFileVersion)) {
 					listModel.clear();
 					idSet.clear();
 
 					final ProjectInfo tmpInfo =
 						(ProjectInfo) tmpDecoder.readObject();
 					saveTextField.setText(tmpInfo.getJadPath());
 
 					if (tmpInfo.getSplashPath() != null) {
 						openImage(tmpInfo.getSplashPath());
 					}
 
 					for (int i = 0; i < tmpInfo.getBookCount(); i++) {
 						Book tmpBook = (Book) tmpDecoder.readObject();
 						idSet.add(tmpBook.getId());
 						listModel.addElement(tmpBook);
 					}
 				} else {
 					JOptionPane.showMessageDialog(
 						this,
 						res.get(
 							"gui.error.projectVersionNotMatch",
 							new String[] { Common.VERSION, tmpProjFileVersion }),
 						res.get("gui.error.title"),
 						JOptionPane.ERROR_MESSAGE);
 				}
 
 				tmpDecoder.close();
 			} catch (Exception e) {
 				alert(
 					res.get("gui.alert.err", new String[] { e.getMessage() }));
 				e.printStackTrace(logStream);
 				logStream.flush();
 			}
 
 			checkSaveBtn();
 		}
 	} //GEN-LAST:event_openProjectBtnActionPerformed
 
 	private void bookNameFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_bookNameFocusGained
 		bookName.setSelectionStart(0);
 		bookName.setSelectionEnd(bookName.getText().length());
 	} //GEN-LAST:event_bookNameFocusGained
 
 	private void descriptionFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_descriptionFocusGained
 		description.setSelectionStart(0);
 		description.setSelectionEnd(description.getText().length());
 	} //GEN-LAST:event_descriptionFocusGained
 
 	private void saveTextFieldFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_saveTextFieldFocusGained
 		saveTextField.setSelectionStart(0);
 		saveTextField.setSelectionEnd(saveTextField.getText().length());
 	} //GEN-LAST:event_saveTextFieldFocusGained
 
 	private void partSizeTextFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_partSizeTextFocusGained
 		partSizeText.setSelectionStart(0);
 		partSizeText.setSelectionEnd(partSizeText.getText().length());
 	} //GEN-LAST:event_partSizeTextFocusGained
 
 	private void charsetComboFocusGained(java.awt.event.FocusEvent evt) { //GEN-FIRST:event_charsetComboFocusGained
 		charsetCombo.setPopupVisible(true);
 	} //GEN-LAST:event_charsetComboFocusGained
 
 	private void setBookPos(int i) {
 		bookListListenerEnabled = false;
 
 		int idx = bookList.getSelectedIndex();
 		bookChanged = false;
 
 		Book tmpBook = (Book) listModel.remove(idx);
 		listModel.add(i, tmpBook);
 		oldIdx = i;
 		bookList.setSelectedIndex(oldIdx);
 		bookListListenerEnabled = true;
 		downBtn.setEnabled(
 			(oldIdx > -1) && ((listModel.getSize() - 1) > oldIdx));
 		upBtn.setEnabled(oldIdx > 0);
 	}
 
 	private void checkChangesInBook() {
 		if (bookChanged && (oldIdx > -1)
 				&& (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
 					this,
 					res.get("gui.confirm.savechanged"),
 					res.get("gui.confirm.title"),
 					JOptionPane.YES_NO_OPTION))) {
 			saveBook();
 		}
 	}
 
 	protected void runPackager() {
 		saveBtn.setEnabled(false);
 		checkChangesInBook();
 
 		try {
 			final Packager packager = new Packager();
 
 			for (int i = 0, n = listModel.size(); i < n; i++) {
 				packager.addBook((Book) listModel.get(i));
 			}
 
 			if (packager.getBookCount() == 0) {
 				alert(res.get("gui.alert.notext"));
 
 				return;
 			}
 
 			String outPrefix = saveTextField.getText();
 
 			if (outPrefix.length() == 0) {
 				alert(res.get("gui.alert.nooutput"));
 
 				return;
 			}
 
 			if (outPrefix.lastIndexOf('.') != -1) {
 				outPrefix = outPrefix.substring(0, outPrefix.lastIndexOf('.'));
 			}
 
 			props.setProperty(Constants.PROP_OUT, outPrefix);
 			logStream.println("----------------------");
 			logStream.println(res.get("gui.log.genstarted"));
 
 			packager.createSuite(logStream);
 
 			logStream.println(res.get("gui.log.genfinished"));
 			runEmulator();
 		} catch (Exception e) {
 			alert(res.get("gui.alert.err", new String[] { e.getMessage() }));
 			e.printStackTrace(logStream);
 			logStream.flush();
 		}
 
 		saveBtn.setEnabled(true);
 	}
 
 	protected void addBook() {
 		int tmpPartSize = props.getAsInt(Constants.PROP_PARTSIZE);
 
 		try {
 			tmpPartSize = Integer.parseInt(partSizeText.getText());
 		} catch (NumberFormatException e) {
 			alert(
 				res.get(
 					"gui.alert.partsize",
 					new String[] { String.valueOf(tmpPartSize) }));
 			partSizeText.setText(String.valueOf(tmpPartSize));
 		}
 
 		final Book tmpBook =
 			new Book("Book " + (bookNr++), "Description", tmpPartSize);
 
 		while (idSet.contains(tmpBook.getId())) {
 			tmpBook.setName("Book " + (bookNr++));
 		}
 
 		tmpBook.setRightToLeft(rightToLeftCb.isSelected());
 		idSet.add(tmpBook.getId());
 		listModel.addElement(tmpBook);
 		bookList.setSelectedIndex(listModel.size() - 1);
 		checkSaveBtn();
 	}
 
 	protected void openBookFile(final File tmpFile) {
 		try {
 			Book book = new Book();
 			book.readText(
 				tmpFile.getPath(),
 				charsetCombo.getSelectedItem().toString());
 
 			if (!Constants.BOOK_NAME_DEF.equals(book.getName())) {
 				bookName.setText(book.getName());
 			}
 
 			bookTextArea.setText(book.getText());
 			bookTextArea.setCaretPosition(0);
 			description.setText(book.getDescription());
 		} catch (Exception e) {
 			alert(e.getMessage());
 		}
 	}
 
 	public void clearBookSelection() {
 		bookList.clearSelection();
 	}
 
 	private void saveBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_saveBtnActionPerformed
 		new Thread(
 			new Runnable() {
 				public void run() {
 					runPackager();
 				}
 			}).start();
 	} // GEN-LAST:event_saveBtnActionPerformed
 
 	protected void saveBook() {
 		final Book tmpBook = (Book) listModel.getElementAt(oldIdx);
 
 		if (saveChangesBtn.isEnabled()) {
 			tmpBook.setName(bookName.getText());
 		}
 
 		tmpBook.setDescription(description.getText());
 		tmpBook.setText(bookTextArea.getText());
 		tmpBook.setRightToLeft(rightToLeftCb.isSelected());
 		listModel.setElementAt(tmpBook, oldIdx);
 		idSet.remove(bookId);
 		bookId = tmpBook.getId();
 		idSet.add(bookId);
 		bookChanged = false;
 		detailsPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						res.get("gui.panel.details")));
 		saveChangesBtn.setEnabled(false);
 	}
 
 	private void saveChangesBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_saveChangesBtnActionPerformed
 		saveBook();
 	} // GEN-LAST:event_saveChangesBtnActionPerformed
 
 	private void checkSaveBtn() {
 		saveBtn.setEnabled(
 				(listModel.size() > 0)
 				&& (saveTextField.getText().length() > 0));
 	}
 
 	private void deleteBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_deleteBtnActionPerformed
 
 		int idx = bookList.getSelectedIndex();
 		bookChanged = false;
 
 		Book tmpBook = (Book) listModel.remove(idx);
 		oldIdx = -1;
 		idSet.remove(tmpBook.getId());
 
 		int size = listModel.getSize();
 
 		if (size > 0) {
 			if (idx == listModel.getSize()) {
 				// removed item in last position
 				idx--;
 			}
 
 			bookList.setSelectedIndex(idx);
 			bookList.ensureIndexIsVisible(idx);
 		}
 
 		checkSaveBtn();
 	} // GEN-LAST:event_deleteBtnActionPerformed
 
 	private void bookListValueChanged(javax.swing.event.ListSelectionEvent evt) { // GEN-FIRST:event_bookListValueChanged
 		checkChangesInBook();
 		oldIdx = bookList.getSelectedIndex();
 		downBtn.setEnabled(
 			(oldIdx > -1) && ((listModel.getSize() - 1) > oldIdx));
 		upBtn.setEnabled(oldIdx > 0);
 
 		final boolean tmpEnabled = oldIdx > -1;
 
 		if (tmpEnabled) {
 			final Book tmpBook = (Book) listModel.getElementAt(oldIdx);
 			bookId = tmpBook.getId();
 			bookName.setText(tmpBook.getName());
 			description.setText(tmpBook.getDescription());
 			bookTextArea.setText(tmpBook.getText());
 		} else {
 			bookName.setText("");
 			description.setText("");
 			bookTextArea.setText("");
 			bookId = null;
 		}
 
 		bookTextArea.setCaretPosition(0);
 		bookName.setBackground(description.getBackground());
 		bookChanged = false;
 		detailsPanel.setBorder(
 				javax.swing.BorderFactory.createTitledBorder(
 						res.get("gui.panel.details")));
 		bookName.setEnabled(tmpEnabled);
 		description.setEnabled(tmpEnabled);
 		bookTextArea.setEnabled(tmpEnabled);
 		fileButton.setEnabled(tmpEnabled);
 		saveChangesBtn.setEnabled(false);
 		deleteBtn.setEnabled(tmpEnabled);
 	} // GEN-LAST:event_bookListValueChanged
 
 	private void addBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_addBtnActionPerformed
 		addBook();
 	} // GEN-LAST:event_addBtnActionPerformed
 
 	private void autoformatCbActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_autoformatCbActionPerformed
 		props.setProperty(
 			Constants.PROP_AUTOFORMAT,
 			String.valueOf(autoformatCb.isSelected()));
 	} // GEN-LAST:event_autoformatCbActionPerformed
 
 	private void defaultSplashBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_defaultSplashBtnActionPerformed
 		splashLabel.setIcon(
 				new ImageIcon(
 						getClass().getResource(
 								"/cz/cacek/ebook/packager/resources/images/ebook.png")));
 		defaultSplashBtn.setEnabled(false);
 	} // GEN-LAST:event_defaultSplashBtnActionPerformed
 
 	private void outFileBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_outFileBtnActionPerformed
 
 		int tmpRetVal = fs.showSaveDialog(this);
 
 		if (tmpRetVal == JFileChooser.APPROVE_OPTION) {
 			final File tmpFile = fs.getSelectedFile();
 			String tmpPath = tmpFile.getPath();
 
 			if (!tmpPath.toUpperCase().endsWith(".JAD")) {
 				tmpPath = tmpPath + ".jad";
 			}
 
 			saveTextField.setText(tmpPath);
 		}
 	} // GEN-LAST:event_outFileBtnActionPerformed
 
 	private void openImage(final String aPath) {
 		splashLabel.setIcon(new ImageIcon(aPath));
 		defaultSplashBtn.setEnabled(true);
 		props.setProperty(Constants.PROP_SPLASH, aPath);
 	}
 
 	private void splashBtnActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_splashBtnActionPerformed
 
 		File tmpFile = imageChooser.openImageFile(this);
 
 		if (tmpFile != null) {
 			openImage(tmpFile.getAbsolutePath());
 		}
 	} // GEN-LAST:event_splashBtnActionPerformed
 
 	private void fileButtonActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_fileButtonActionPerformed
 
 		int tmpRetVal = fc.showOpenDialog(this);
 
 		if (tmpRetVal == JFileChooser.APPROVE_OPTION) {
 			try {
 				openBookFile(fc.getSelectedFile());
 			} catch (Exception e) {
 				alert(e.getMessage());
 			}
 		}
 	} // GEN-LAST:event_fileButtonActionPerformed
 
 	private void advancedCbActionPerformed(java.awt.event.ActionEvent evt) { // GEN-FIRST:event_advancedCbActionPerformed
 		setAdvancedEnabled();
 	} // GEN-LAST:event_advancedCbActionPerformed
 }
 
 
 /**
  * OutputStream wrapper for writing to JTextArea component
  *
  * @author Josef Cacek
  */
 class JTextAreaStream
 	extends OutputStream
 {
 	protected JTextArea textArea;
 	protected ByteArrayOutputStream baos;
 
 	public JTextAreaStream(JTextArea textArea) {
 		this.textArea = textArea;
 		this.baos = new ByteArrayOutputStream();
 	}
 
 	public void write(int c) {
 		this.baos.write((char) c);
 		this.update();
 	}
 
 	public void write(byte[] bytes, int offset, int length) {
 		this.baos.write(bytes, offset, length);
 		this.update();
 	}
 
 	protected void update() {
 		String text = new String(this.baos.toByteArray());
 		this.textArea.setText(text);
 		this.textArea.setCaretPosition(text.length());
 	}
 }
