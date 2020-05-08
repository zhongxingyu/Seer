 /*
  Copyright (c) 2006, 2007, 2009, 2010, The Cytoscape Consortium (www.cytoscape.org)
 
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
 
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package edu.ucsd.bioeng.coreplugin.tableImport.ui;
 
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.GeneAssociationTags.DB_OBJECT_SYMBOL;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.GeneAssociationTags.DB_OBJECT_SYNONYM;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.GeneAssociationTags.GO_ID;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.GeneAssociationTags.TAXON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.TextFileDelimiters.PIPE;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader.ObjectType.EDGE;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader.ObjectType.NETWORK;
 import static edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader.ObjectType.NODE;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.NOT_LOADED_COLOR;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogColorTheme.ONTOLOGY_COLOR;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.ITEM_FONT;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.LABEL_FONT;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogFontTheme.TITLE_FONT;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.BOOLEAN_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.FLOAT_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.ID_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.INT_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.LIST_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.LOCAL_SOURCE_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.REMOTE_SOURCE_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.REMOTE_SOURCE_ICON_LARGE;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.RIGHT_ARROW_ICON;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.SPREADSHEET_ICON_LARGE;
 import static edu.ucsd.bioeng.coreplugin.tableImport.ui.theme.ImportDialogIconSets.STRING_ICON;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListCellRenderer;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import javax.xml.bind.JAXBException;
 
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.jdesktop.layout.GroupLayout;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.bookmarks.Attribute;
 import cytoscape.bookmarks.Bookmarks;
 import cytoscape.bookmarks.DataSource;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.logger.CyLogger;
 import cytoscape.task.ui.JTaskConfig;
 import cytoscape.task.util.TaskManager;
 import cytoscape.util.BookmarksUtil;
 import cytoscape.util.CyFileFilter;
 import cytoscape.util.FileUtil;
 import cytoscape.util.URLUtil;
 import cytoscape.util.swing.ColumnResizer;
 import cytoscape.util.swing.JStatusBar;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.AttributeAndOntologyMappingParameters;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.AttributeMappingParameters;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.DefaultAttributeTableReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.ExcelAttributeSheetReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.ExcelNetworkSheetReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.GeneAssociationReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.NetworkTableMappingParameters;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.NetworkTableReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.OntologyAnnotationReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.SupportedFileType;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.TextFileDelimiters;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader;
 import edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader.ObjectType;
 import giny.model.Edge;
 import giny.model.Node;
 
 
 /**
  * Main UI for Table Import.
  *
  * @author kono
  */
 public class ImportTextTableDialog extends JDialog implements PropertyChangeListener,
                                                               TableModelListener {
 	
 	private static final long serialVersionUID = 7356378931577386260L;
 
 	/**
 	 * This dialog GUI will be switched based on the following parameters:
 	 *
 	 * SIMPLE_ATTRIBUTE_IMPORT: Import attributes in text table.
 	 *
 	 * ONTOLOGY_AND_ANNOTATION_IMPORT: Load ontology and map attributes in text
 	 * table.
 	 *
 	 * NETWORK_IMPORT: Import text table as a network.
 	 */
 	public static final int SIMPLE_ATTRIBUTE_IMPORT = 1;
 
 	/**
 	 *
 	 */
 	public static final int ONTOLOGY_AND_ANNOTATION_IMPORT = 2;
 
 	/**
 	 *
 	 */
 	public static final int NETWORK_IMPORT = 3;
 
 	/**
 	 * Enums for file types.
 	 */
 	public static enum FileTypes {
 		ATTRIBUTE_FILE,
 		NETWORK_FILE,
 		GENE_ASSOCIATION_FILE,
 		CUSTOM_ANNOTATION_FILE;
 	}
 
 	/*
 	 * Default value for Interaction edge attribute.
 	 */
 	private static final String DEFAULT_INTERACTION = "pp";
 
 	/*
 	 * Signals used among Swing components in this dialog:
 	 */
 	public static final String LIST_DELIMITER_CHANGED = "listDelimiterChanged";
 	public static final String LIST_DATA_TYPE_CHANGED = "listDataTypeChanged";
 	public static final String ATTR_DATA_TYPE_CHANGED = "attrDataTypeChanged";
 	public static final String ATTRIBUTE_NAME_CHANGED = "aliasTableChanged";
 	public static final String SHEET_CHANGED = "sheetChanged";
 	public static final String NETWORK_IMPORT_TEMPLATE_CHANGED = "networkImportTemplateChanged";
 
 	/*
 	 * HTML strings for tool tip text
 	 */
 	private String ontologyHtml = "<html><body bgcolor=\"white\"><p><strong><font size=\"+1\" face=\"serif\"><u>%DataSourceName%</u></font></strong></p><br>"
 	                              + "<p><em>Data Source URL</em>: <br><font color=\"blue\">%SourceURL%</font></p><br><p><em>Description</em>:<br>"
 	                              + "<table width=\"300\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\"><tr>"
 	                              + "<td rowspan=\"1\" colspan=\"1\">%Description%</td></tr></table></p></body></html>";
 	private String annotationHtml = "<html><body bgcolor=\"white\"><p><strong><font size=\"+1\" face=\"serif\"><u>%DataSourceName%</u></font></strong></p><br>"
 	                                + "<p><em>Annotation File URL</em>: <br><font color=\"blue\">%SourceURL%</font></p><br>"
 	                                + "<p><em>Data Format</em>: <font color=\"green\">%Format%</font></p><br>"
 	                                + "<p><em>Other Information</em>:<br>"
 	                                + "<table width=\"300\" border=\"0\" cellspacing=\"3\" cellpadding=\"3\">"
 	                                + "%AttributeTable%</table></p></body></html>";
 	private static final String DEF_ANNOTATION_ITEM = "Please select an annotation data source...";
 	private static final String[] keyTable = { "Alias?", "Column (Attribute Name)", "Data Type" };
 	private static final String ID = "ID";
 	private static final String GENE_ASSOCIATION = "gene_association";
 
 	// Key column index
 	private int keyInFile;
 
 	// Case sensitivity
 	private Boolean caseSensitive = true;
 
 	// Data Type
 	private edu.ucsd.bioeng.coreplugin.tableImport.reader.TextTableReader.ObjectType objType;
 	private final int dialogType;
 	private Map<String, String> annotationUrlMap;
 	private Map<String, String> annotationFormatMap;
 	private Map<String, Map<String, String>> annotationAttributesMap;
 	private Map<String, String> ontologyUrlMap;
 	private Map<String, String> ontologyTypeMap;
 	private Map<String, String> ontologyDescriptionMap;
 	private List<Byte> attributeDataTypes;
 
 	/*
 	 * This is for storing data type in the list object.
 	 */
 	private Byte[] listDataTypes;
 
 	/*
 	 * Tracking multiple sheets.
 	 */
 	private Map<String, AliasTableModel> aliasTableModelMap;
 	private Map<String, JTable> aliasTableMap;
 	private Map<String, Integer> primaryKeyMap;
 	private String[] columnHeaders;
 	private String listDelimiter;
 	private boolean[] importFlag;
 	private CyAttributes selectedAttributes;
 	private PropertyChangeSupport changes = new PropertyChangeSupport(this);
 	private File[] inputFiles;
 
 	private CyLogger logger;
 
 	/**
 	 * Creates new form ImportAttributesDialog
 	 *
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public ImportTextTableDialog(Frame parent, boolean modal) throws JAXBException, IOException {
 		this(parent, modal, ImportTextTableDialog.SIMPLE_ATTRIBUTE_IMPORT);
 	}
 
 	public ImportTextTableDialog(final Frame parent, final File tableFile,
 				     final String tableFileName, final int dialogType)
 		throws JAXBException, IOException
 	{
 		super(parent, /* modal = */ true);
 
 		// Default Attribute is node attr.
 		selectedAttributes = Cytoscape.getNodeAttributes();
 
 		this.objType = NODE;
 		this.dialogType = dialogType;
 		this.listDelimiter = PIPE.toString();
 
 		this.aliasTableModelMap = new HashMap<String, AliasTableModel>();
 		this.aliasTableMap = new HashMap<String, JTable>();
 		this.primaryKeyMap = new HashMap<String, Integer>();
 
 		annotationUrlMap = new HashMap<String, String>();
 		annotationFormatMap = new HashMap<String, String>();
 		annotationAttributesMap = new HashMap<String, Map<String, String>>();
 
 		ontologyUrlMap = new HashMap<String, String>();
 		ontologyDescriptionMap = new HashMap<String, String>();
 		ontologyTypeMap = new HashMap<String, String>();
 
 		attributeDataTypes = new ArrayList<Byte>();
 		initComponents(true);
 		updateComponents();
 
 		previewPanel.addPropertyChangeListener(this);
 
 		logger = CyLogger.getLogger( ImportTextTableDialog.class );
 
 		final File multiSource[] = { tableFile };
 		fileLoadAction(multiSource, tableFileName);
 	}
 
 	private void fileLoadAction(final File[] multiSource, final String displayName) throws IOException {
 		// Pick the first one and show preview.
 		this.inputFiles = multiSource;
 
 		final File sourceFile = multiSource[0];
 
 		targetDataSourceTextField.setText(displayName);
 
 		// Set tooltip as HTML
 		StringBuilder builder = new StringBuilder();
 		builder.append("<html><body><strong text=\"red\">File(s) to be imported</strong><ul>");
 
 		builder.append("<li>" + displayName + "</li>");
 		for (int i = 1; i < multiSource.length; i++)
 			builder.append("<li>" + multiSource[i].getName() + "</li>");
 
 		builder.append("</ul></body></html>");
 		targetDataSourceTextField.setToolTipText(builder.toString());
 
 		readAnnotationForPreview(multiSource[0].toURI().toURL(), checkDelimiter());
 
 		if (previewPanel.getPreviewTable() == null) {
 			JLabel label = new JLabel("File is broken or empty!");
 			label.setForeground(Color.RED);
 			JOptionPane.showMessageDialog(this, label);
 
 			return;
 		}
 
 		columnHeaders = new String[previewPanel.getPreviewTable().getColumnCount()];
 		transferNameCheckBox.setEnabled(true);
 		transferNameCheckBox.setSelected(false);
 
 		ColumnResizer.adjustColumnPreferredWidths(previewPanel.getPreviewTable());
 		previewPanel.getPreviewTable().repaint();
 	}
 
 	public ImportTextTableDialog(Frame parent, boolean modal, int dialogType)
 	    throws JAXBException, IOException {
 		super(parent, modal);
 
 		// Default Attribute is node attr.
 		selectedAttributes = Cytoscape.getNodeAttributes();
 
 		this.objType = NODE;
 		this.dialogType = dialogType;
 		this.listDelimiter = PIPE.toString();
 
 		this.aliasTableModelMap = new HashMap<String, AliasTableModel>();
 		this.aliasTableMap = new HashMap<String, JTable>();
 		this.primaryKeyMap = new HashMap<String, Integer>();
 
 		annotationUrlMap = new HashMap<String, String>();
 		annotationFormatMap = new HashMap<String, String>();
 		annotationAttributesMap = new HashMap<String, Map<String, String>>();
 
 		ontologyUrlMap = new HashMap<String, String>();
 		ontologyDescriptionMap = new HashMap<String, String>();
 		ontologyTypeMap = new HashMap<String, String>();
 
 		attributeDataTypes = new ArrayList<Byte>();
 		initComponents(false);
 		updateComponents();
 
 		previewPanel.addPropertyChangeListener(this);
 
 		logger = CyLogger.getLogger( ImportTextTableDialog.class );
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener l) {
 		if(changes == null) return;
 		changes.addPropertyChangeListener(l);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener l) {
 		changes.removePropertyChangeListener(l);
 	}
 
 	/**
 	 * Listening to local signals used among Swing components in this dialog.
 	 */
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName().equals(LIST_DELIMITER_CHANGED)) {
 			/*
 			 * List delimiter has been changed by preview table GUI.
 			 */
 			listDelimiter = evt.getNewValue().toString();
 		} else if (evt.getPropertyName().equals(LIST_DATA_TYPE_CHANGED)) {
 			listDataTypes = (Byte[]) evt.getNewValue();
 		} else if (evt.getPropertyName().equals(ATTR_DATA_TYPE_CHANGED)) {
 			/*
 			 * Data type of an attribute has been chabged.
 			 */
 			final Vector vec = (Vector) evt.getNewValue();
 			final Integer key = (Integer) vec.get(0);
 			final Byte newType = (Byte) vec.get(1);
 
 			if (key > attributeDataTypes.size()) {
 				attributeDataTypes = new ArrayList<Byte>();
 
 				for (Byte type : previewPanel.getCurrentDataTypes()) {
 					attributeDataTypes.add(type);
 				}
 			}
 
 			attributeDataTypes.set(key, newType);
 
 			if (dialogType != NETWORK_IMPORT) {
 				final JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
 				curTable.setDefaultRenderer(Object.class,
 				                            new AliasTableRenderer(attributeDataTypes,
 				                                                   primaryKeyComboBox.getSelectedIndex()));
 				curTable.repaint();
 			}
 		} else if (evt.getPropertyName().equals(ATTRIBUTE_NAME_CHANGED)) {
 			/*
 			 * Update Alias Table
 			 */
 			if (dialogType != NETWORK_IMPORT) {
 				final Vector vec = (Vector) evt.getNewValue();
 				final String name = (String) vec.get(1);
 				final Integer column = (Integer) vec.get(0);
 
 				// Update cell in the attribute table
 				updateAliasTableCell(name, column);
 
 				// Update Primary Key combo box
 				updatePrimaryKeyComboBox();
 			}
 		} else if (evt.getPropertyName().equals(SHEET_CHANGED)) {
 			/*
 			 * Only when the file is in Excel format.
 			 */
 			final int columnCount = previewPanel.getPreviewTable().getColumnCount();
 			aliasTableModelMap.put(previewPanel.getSelectedSheetName(),
 			                       new AliasTableModel(keyTable, columnCount));
 
 			initializeAliasTable(columnCount, null);
 			updatePrimaryKeyComboBox();
 		} else if (evt.getPropertyName().equals(NETWORK_IMPORT_TEMPLATE_CHANGED)) {
 			/*
 			 * This is a signal from network import options panel.
 			 */
 			List<Integer> columnIdx = (List<Integer>) evt.getNewValue();
 
 			final AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
 			                                                                                               .getCellRenderer(0,
 			                                                                                                                0);
 			rend.setSourceIndex(columnIdx.get(0));
 			rend.setTargetIndex(columnIdx.get(1));
 			rend.setInteractionIndex(columnIdx.get(2));
 
 			previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
 			previewPanel.getPreviewTable().repaint();
 
 			//previewPanel.repaint();
 		}
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is always
 	 * regenerated by the Form Editor.
 	 */
 
 	// <editor-fold defaultstate="collapsed" desc=" Generated Code">
 	private void initComponents(final boolean fileHasBeenProvided) {
 		statusBar = new JStatusBar();
 
 		importAllCheckBox = new JCheckBox("Import everything (Key is always ID)");
 		importAllCheckBox.addActionListener(new ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					importAllCheckBoxActionPerformed(evt);
 				}
 			});
 
 		caseSensitiveCheckBox = new JCheckBox("Case Sensitive");
 		caseSensitiveCheckBox.setToolTipText("<html><strong><font color=\"red\">Caution! If you uncheck this, import can be extrelely slow.</font></strong></html>");
 		caseSensitiveCheckBox.setSelected(true);
 		caseSensitiveCheckBox.addActionListener(new ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					ignoreCaseCheckBoxActionPerformed(evt);
 				}
 
 				private void ignoreCaseCheckBoxActionPerformed(ActionEvent evt) {
 					caseSensitive = caseSensitiveCheckBox.isSelected();
 				}
 			});
 
 		importTypeButtonGroup = new ButtonGroup();
 
 		attrTypePanel = new JPanel();
 
 		counterSpinner = new javax.swing.JSpinner();
 		counterLabel = new javax.swing.JLabel();
 		reloadButton = new javax.swing.JButton();
 		showAllRadioButton = new javax.swing.JRadioButton();
 		counterRadioButton = new javax.swing.JRadioButton();
 
 		attrTypeButtonGroup = new javax.swing.ButtonGroup();
 		titleIconLabel1 = new javax.swing.JLabel();
 		titleIconLabel2 = new javax.swing.JLabel();
 		titleIconLabel3 = new javax.swing.JLabel();
 		titleLabel = new javax.swing.JLabel();
 		titleSeparator = new javax.swing.JSeparator();
 		importButton = new javax.swing.JButton();
 		cancelButton = new javax.swing.JButton();
 		helpButton = new javax.swing.JButton();
 		basicPanel = new javax.swing.JPanel();
 		attribuiteLabel = new javax.swing.JLabel();
 		nodeRadioButton = new javax.swing.JRadioButton();
 		edgeRadioButton = new javax.swing.JRadioButton();
 		networkRadioButton = new javax.swing.JRadioButton();
 		annotationAndOntologyImportPanel = new javax.swing.JPanel();
 		ontologyLabel = new javax.swing.JLabel();
 		ontologyComboBox = new javax.swing.JComboBox();
 		browseOntologyButton = new javax.swing.JButton();
 		sourceLabel = new javax.swing.JLabel();
 		annotationComboBox = new javax.swing.JComboBox();
 		browseAnnotationButton = new javax.swing.JButton();
 
 		targetDataSourceTextField = new javax.swing.JTextField();
 		if (fileHasBeenProvided)
 			targetDataSourceTextField.setEnabled(false);
 		selectAttributeFileButton = new javax.swing.JButton();
 		if (fileHasBeenProvided)
 			selectAttributeFileButton.setEnabled(false);
 		advancedPanel = new javax.swing.JPanel();
 		advancedOptionCheckBox = new javax.swing.JCheckBox();
 		textImportCheckBox = new javax.swing.JCheckBox();
 		attr2annotationPanel = new javax.swing.JPanel();
 		primaryKeyLabel = new javax.swing.JLabel();
 		nodeKeyLabel = new javax.swing.JLabel();
 		mappingAttributeComboBox = new javax.swing.JComboBox();
 		aliasScrollPane = new javax.swing.JScrollPane();
 		arrowButton1 = new javax.swing.JButton();
 		ontology2annotationPanel = new javax.swing.JPanel();
 		targetOntologyLabel = new javax.swing.JLabel();
 		ontologyTextField = new javax.swing.JTextField();
 		ontologyInAnnotationLabel = new javax.swing.JLabel();
 		ontologyInAnnotationComboBox = new javax.swing.JComboBox();
 		arrowButton2 = new javax.swing.JButton();
 		textImportOptionPanel = new javax.swing.JPanel();
 		delimiterPanel = new javax.swing.JPanel();
 		tabCheckBox = new javax.swing.JCheckBox();
 		commaCheckBox = new javax.swing.JCheckBox();
 		semicolonCheckBox = new javax.swing.JCheckBox();
 		spaceCheckBox = new javax.swing.JCheckBox();
 		otherCheckBox = new javax.swing.JCheckBox();
 		otherDelimiterTextField = new javax.swing.JTextField();
 		transferNameCheckBox = new javax.swing.JCheckBox();
 
 		attributeNamePanel = new JPanel();
 		previewOptionPanel = new JPanel();
 		networkImportOptionPanel = new JPanel();
 
 		defaultInteractionLabel = new JLabel();
 		defaultInteractionTextField = new JTextField();
 
 		simpleAttributeImportPanel = new javax.swing.JPanel();
 		attributeFileLabel = new javax.swing.JLabel();
 		if (fileHasBeenProvided)
 			attributeFileLabel.setEnabled(false);
 
 		startRowSpinner = new JSpinner();
 		startRowLabel = new JLabel();
 
 		commentLineLabel = new JLabel();
 		commentLineTextField = new JTextField();
 		commentLineTextField.setName("commentLineTextField");
 
 		titleLabel.setFont(TITLE_FONT.getFont());
 
 		if (dialogType == NETWORK_IMPORT) {
 			previewPanel = new PreviewTablePanel(null, PreviewTablePanel.NETWORK_PREVIEW);
 		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			defaultInteractionLabel.setEnabled(false);
 			defaultInteractionTextField.setEnabled(false);
 			commentLineTextField.setText("!");
 			importAllCheckBox.setEnabled(false);
 			previewPanel = new PreviewTablePanel(null, PreviewTablePanel.ONTOLOGY_PREVIEW);
 		} else {
 			defaultInteractionLabel.setEnabled(false);
 			defaultInteractionTextField.setEnabled(false);
 
 			previewPanel = new PreviewTablePanel();
 		}
 
 		primaryLabel = new JLabel("Primary Key: ");
 		primaryKeyComboBox = new JComboBox();
 		primaryKeyComboBox.setEnabled(false);
 		primaryKeyComboBox.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent evt) {
 					primaryKeyComboBoxActionPerformed(evt);
 				}
 			});
 
 		/*
 		 * Set tooltips options.
 		 */
 		ToolTipManager tp = ToolTipManager.sharedInstance();
 		tp.setInitialDelay(40);
 		tp.setDismissDelay(50000);
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 		titleIconLabel2.setIcon(RIGHT_ARROW_ICON.getIcon());
 
 		titleIconLabel3.setIcon(new ImageIcon(Cytoscape.class.getResource("images/icon48.png")));
 
 		titleSeparator.setForeground(java.awt.Color.blue);
 
 		importButton.setText("Import");
 		importButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						importButtonActionPerformed(evt);
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		cancelButton.setText("Cancel");
 		cancelButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					cancelButtonActionPerformed(evt);
 				}
 			});
 
 		helpButton.setBackground(new java.awt.Color(255, 255, 255));
 		helpButton.setText("?");
 		helpButton.setToolTipText("Display help page...");
 		helpButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
 		helpButton.setPreferredSize(new java.awt.Dimension(14, 14));
 		helpButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					helpButtonActionPerformed(arg0);
 				}
 			});
 
 		/*
 		 * Data Source Panel Layouts.
 		 */
 		basicPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Sources",
 		                                                                  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                  javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                  new java.awt.Font("Dialog",
 		                                                                                    1, 11)));
 
 		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT)
 		    || (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
 			attribuiteLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			attribuiteLabel.setText("Attributes");
 
 			nodeRadioButton.setText("Node");
 			nodeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			nodeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			nodeRadioButton.setSelected(true);
 			nodeRadioButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						attributeRadioButtonActionPerformed(evt);
 					}
 				});
 
 			edgeRadioButton.setText("Edge");
 			edgeRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			edgeRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			edgeRadioButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						attributeRadioButtonActionPerformed(evt);
 					}
 				});
 
 			networkRadioButton.setText("Network");
 			networkRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			networkRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			networkRadioButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						attributeRadioButtonActionPerformed(evt);
 					}
 				});
 
 			org.jdesktop.layout.GroupLayout attrTypePanelLayout = new org.jdesktop.layout.GroupLayout(attrTypePanel);
 			attrTypePanel.setLayout(attrTypePanelLayout);
 			attrTypePanelLayout.setHorizontalGroup(attrTypePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                          .add(attrTypePanelLayout.createSequentialGroup()
 			                                                                                  .add(attribuiteLabel)
 			                                                                                  .add(24,
 			                                                                                       24,
 			                                                                                       24)
 			                                                                                  .add(nodeRadioButton)
 			                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                  .add(edgeRadioButton)
 			                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                  .add(networkRadioButton)
 			                                                                                  .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                                                   Short.MAX_VALUE)));
 			attrTypePanelLayout.setVerticalGroup(attrTypePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                        .add(attrTypePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                                                .add(attribuiteLabel)
 			                                                                                .add(nodeRadioButton)
 			                                                                                .add(edgeRadioButton)
 			                                                                                .add(networkRadioButton)));
 		}
 
 		/*
 		 * This panel is necessary only when this is an ontology import dialog.
 		 */
 		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			titleIconLabel1.setIcon(REMOTE_SOURCE_ICON_LARGE.getIcon());
 
 			ontologyLabel.setFont(LABEL_FONT.getFont());
 			ontologyLabel.setForeground(ONTOLOGY_COLOR.getColor());
 			ontologyLabel.setText("Ontology");
 
 			// ontologyComboBox.setFont(new java.awt.Font("SansSerif", 1, 14));
 			ontologyComboBox.setPreferredSize(new java.awt.Dimension(68, 25));
 
 			final ListCellRenderer ontologyLcr = ontologyComboBox.getRenderer();
 			ontologyComboBox.setFont(ITEM_FONT.getFont());
 			ontologyComboBox.setForeground(ONTOLOGY_COLOR.getColor());
 			ontologyComboBox.setRenderer(new ListCellRenderer() {
 					public Component getListCellRendererComponent(JList list, Object value,
 					                                              int index, boolean isSelected,
 					                                              boolean cellHasFocus) {
 						JLabel ontologyItem = (JLabel) ontologyLcr.getListCellRendererComponent(list,
 						                                                                        value,
 						                                                                        index,
 						                                                                        isSelected,
 						                                                                        cellHasFocus);
 						String url = ontologyUrlMap.get(value);
 
 						if (isSelected) {
 							ontologyItem.setBackground(list.getSelectionBackground());
 							ontologyItem.setForeground(list.getSelectionForeground());
 						} else {
 							ontologyItem.setBackground(list.getBackground());
 							ontologyItem.setForeground(list.getForeground());
 						}
 
 						if ((url != null) && url.startsWith("http://")) {
 							ontologyItem.setIcon(REMOTE_SOURCE_ICON.getIcon());
 						} else {
 							ontologyItem.setIcon(LOCAL_SOURCE_ICON.getIcon());
 						}
 
 						if (Cytoscape.getOntologyServer().getOntologyNames().contains(value)) {
 							ontologyItem.setForeground(ONTOLOGY_COLOR.getColor());
 						} else {
 							ontologyItem.setForeground(NOT_LOADED_COLOR.getColor());
 						}
 
 						return ontologyItem;
 					}
 				});
 
 			ontologyComboBox.addMouseListener(new MouseListener() {
 					public void mouseClicked(MouseEvent arg0) {
 					}
 
 					public void mouseEntered(MouseEvent arg0) {
 					}
 
 					public void mouseExited(MouseEvent arg0) {
 					}
 
 					public void mousePressed(MouseEvent arg0) {
 					}
 
 					public void mouseReleased(MouseEvent arg0) {
 					}
 				});
 
 			ontologyComboBox.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						ontologyComboBoxActionPerformed(evt);
 					}
 				});
 
 			browseOntologyButton.setText("Browse");
 			browseOntologyButton.setToolTipText("Browse local ontology file");
 			browseOntologyButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						browseOntologyButtonActionPerformed(evt);
 					}
 				});
 
 			sourceLabel.setFont(LABEL_FONT.getFont());
 			sourceLabel.setText("Annotation");
 
 			annotationComboBox.setName("annotationComboBox");
 			annotationComboBox.setFont(ITEM_FONT.getFont());
 			annotationComboBox.setPreferredSize(new java.awt.Dimension(68, 25));
 
 			final ListCellRenderer lcr = annotationComboBox.getRenderer();
 			annotationComboBox.setRenderer(new ListCellRenderer() {
 					public Component getListCellRendererComponent(JList list, Object value,
 					                                              int index, boolean isSelected,
 					                                              boolean cellHasFocus) {
 						JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index,
 						                                                       isSelected,
 						                                                       cellHasFocus);
 						String url = annotationUrlMap.get(value);
 
 						if (isSelected) {
 							cmp.setBackground(list.getSelectionBackground());
 							cmp.setForeground(list.getSelectionForeground());
 						} else {
 							cmp.setBackground(list.getBackground());
 							cmp.setForeground(list.getForeground());
 						}
 
 						if (value.toString().equals(DEF_ANNOTATION_ITEM)) {
 							cmp.setIcon(null);
 						} else if ((url != null) && url.startsWith("http://")) {
 							cmp.setIcon(REMOTE_SOURCE_ICON.getIcon());
 						} else {
 							cmp.setIcon(LOCAL_SOURCE_ICON.getIcon());
 						}
 
 						return cmp;
 					}
 				});
 			annotationComboBox.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						annotationComboBoxActionPerformed(evt);
 					}
 				});
 
 			browseAnnotationButton.setText("Browse");
 			browseAnnotationButton.setToolTipText("Browse local annotation file...");
 			browseAnnotationButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						browseAnnotationButtonActionPerformed(evt);
 					}
 				});
 
 			GroupLayout annotationAndOntologyImportPanelLayout = new GroupLayout(annotationAndOntologyImportPanel);
 			annotationAndOntologyImportPanel.setLayout(annotationAndOntologyImportPanelLayout);
 
 			annotationAndOntologyImportPanelLayout.setHorizontalGroup(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                .add(annotationAndOntologyImportPanelLayout.createSequentialGroup()
 			                                                                                                                                           .addContainerGap()
 			                                                                                                                                           .add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                                                                      .add(sourceLabel)
 			                                                                                                                                                                                      .add(ontologyLabel))
 			                                                                                                                                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                                           .add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.TRAILING)
 			                                                                                                                                                                                      .add(annotationComboBox,
 			                                                                                                                                                                                           0,
 			                                                                                                                                                                                           100,
 			                                                                                                                                                                                           Short.MAX_VALUE)
 			                                                                                                                                                                                      .add(ontologyComboBox,
 			                                                                                                                                                                                           0,
 			                                                                                                                                                                                           100,
 			                                                                                                                                                                                           Short.MAX_VALUE))
 			                                                                                                                                           .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                                           .add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                                                                      .add(browseAnnotationButton)
 			                                                                                                                                                                                      .add(browseOntologyButton))
 			                                                                                                                                           .addContainerGap()));
 			annotationAndOntologyImportPanelLayout.setVerticalGroup(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                              .add(annotationAndOntologyImportPanelLayout.createSequentialGroup()
 			                                                                                                                                         .addContainerGap()
 			                                                                                                                                         .add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.CENTER)
 			                                                                                                                                                                                    .add(sourceLabel)
 			                                                                                                                                                                                    .add(browseAnnotationButton)
 			                                                                                                                                                                                    .add(annotationComboBox,
 			                                                                                                                                                                                         GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                                                         GroupLayout.PREFERRED_SIZE))
 			                                                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                                         .add(annotationAndOntologyImportPanelLayout.createParallelGroup(GroupLayout.CENTER)
 			                                                                                                                                                                                    .add(ontologyLabel)
 			                                                                                                                                                                                    .add(ontologyComboBox,
 			                                                                                                                                                                                         GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                                                         GroupLayout.PREFERRED_SIZE)
 			                                                                                                                                                                                    .add(browseOntologyButton))
 			                                                                                                                                         .addContainerGap(GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                          Short.MAX_VALUE)));
 		}
 
 		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
 			titleIconLabel1.setIcon(SPREADSHEET_ICON_LARGE.getIcon());
 
 			attributeFileLabel.setText("Input File");
 			attributeFileLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			selectAttributeFileButton.setText("Select File(s)");
 			selectAttributeFileButton.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						try {
 							selectAttributeFileButtonActionPerformed(evt);
 						} catch (IOException e) {
 							JOptionPane.showMessageDialog(
 										      ImportTextTableDialog.this,
 										      "<html>Could not read selected file.<p>See <b>Help->Error Dialog</b> for further details.</html>",
 										      "ERROR", JOptionPane.ERROR_MESSAGE);
 							logger.warn("Could not read selected file.", e);
 						}
 					}
 				});
 
 			GroupLayout simpleAttributeImportPanelLayout = new GroupLayout(simpleAttributeImportPanel);
 			simpleAttributeImportPanel.setLayout(simpleAttributeImportPanelLayout);
 			simpleAttributeImportPanelLayout.setHorizontalGroup(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                                                    .add(simpleAttributeImportPanelLayout.createSequentialGroup()
 			                                                                                                                         .add(attributeFileLabel)
 			                                                                                                                         .add(24,
 			                                                                                                                              24,
 			                                                                                                                              24)
 			                                                                                                                         .add(targetDataSourceTextField,
 			                                                                                                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                                                                              300,
 			                                                                                                                              Short.MAX_VALUE)
 			                                                                                                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                         .add(selectAttributeFileButton)
 			                                                                                                                         .addContainerGap()));
 			simpleAttributeImportPanelLayout.setVerticalGroup(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                                                  .add(simpleAttributeImportPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                                                                                       .add(selectAttributeFileButton)
 			                                                                                                                       .add(targetDataSourceTextField,
 			                                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                                                                                                       .add(attributeFileLabel)));
 		}
 
 		GroupLayout basicPanelLayout = new GroupLayout(basicPanel);
 		basicPanel.setLayout(basicPanelLayout);
 
 		basicPanelLayout.setHorizontalGroup(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                    .add(basicPanelLayout.createSequentialGroup()
 		                                                                         .addContainerGap()
 		                                                                         .add(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                                              .add(basicPanelLayout.createSequentialGroup()
 		                                                                                                                   .add(simpleAttributeImportPanel,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                        Short.MAX_VALUE)
 		                                                                                                                   .addContainerGap())
 		                                                                                              .add(basicPanelLayout.createSequentialGroup()
 		                                                                                                                   .add(annotationAndOntologyImportPanel,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                        Short.MAX_VALUE)
 		                                                                                                                   .addContainerGap())
 		                                                                                              .add(basicPanelLayout.createSequentialGroup()
 		                                                                                                                   .add(attrTypePanel,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                                   .addContainerGap(50,
 		                                                                                                                                    Short.MAX_VALUE)))));
 		basicPanelLayout.setVerticalGroup(basicPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                  .add(basicPanelLayout.createSequentialGroup()
 		                                                                       .add(attrTypePanel,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                       .add(simpleAttributeImportPanel,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 		                                                                                        org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                        Short.MAX_VALUE)
 		                                                                       .add(annotationAndOntologyImportPanel,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                            org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		/*
 		 * Layout data for advanced panel
 		 */
 		advancedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Advanced",
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                     javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                     new java.awt.Font("Dialog",
 		                                                                                       1, 11)));
 
 		if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT)
 		    || (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
 			advancedOptionCheckBox.setText("Show Mapping Options");
 			advancedOptionCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			advancedOptionCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 			advancedOptionCheckBox.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						advancedOptionCheckBoxActionPerformed(evt);
 					}
 				});
 
 			attr2annotationPanel.setBackground(new java.awt.Color(250, 250, 250));
 			attr2annotationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
 			                                                                            "Annotation File to Attribute Mapping",
 			                                                                            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 			                                                                            javax.swing.border.TitledBorder.DEFAULT_POSITION,
 			                                                                            new java.awt.Font("Dialog",
 			                                                                                              1,
 			                                                                                              11)));
 			primaryKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			primaryKeyLabel.setForeground(new java.awt.Color(51, 51, 255));
 			primaryKeyLabel.setText("Key Column in Annotation File");
 
 			nodeKeyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			nodeKeyLabel.setForeground(new java.awt.Color(255, 0, 51));
 			nodeKeyLabel.setText("Key Attribute for Network");
 
 			mappingAttributeComboBox.setForeground(new java.awt.Color(255, 0, 51));
 			mappingAttributeComboBox.setEnabled(false);
 			mappingAttributeComboBox.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						nodeKeyComboBoxActionPerformed(evt);
 					}
 				});
 
 			arrowButton1.setBackground(new java.awt.Color(250, 250, 250));
 			arrowButton1.setOpaque(false);
 			arrowButton1.setIcon(RIGHT_ARROW_ICON.getIcon());
 			arrowButton1.setBorder(null);
 			arrowButton1.setBorderPainted(false);
 
 			GroupLayout attr2annotationPanelLayout = new GroupLayout(attr2annotationPanel);
 			attr2annotationPanel.setLayout(attr2annotationPanelLayout);
 			attr2annotationPanelLayout.setHorizontalGroup(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                        .add(attr2annotationPanelLayout.createSequentialGroup()
 			                                                                                                       .addContainerGap()
 			                                                                                                       .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                      .add(attr2annotationPanelLayout.createSequentialGroup()
 			                                                                                                                                                                     .add(primaryKeyLabel)
 			                                                                                                                                                                     .add(100,
 			                                                                                                                                                                          100,
 			                                                                                                                                                                          100))
 			                                                                                                                                      .add(attr2annotationPanelLayout.createSequentialGroup()
 			                                                                                                                                                                     .add(primaryLabel)
 			                                                                                                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                                                                     .add(primaryKeyComboBox,
 			                                                                                                                                                                          0,
 			                                                                                                                                                                          0,
 			                                                                                                                                                                          Short.MAX_VALUE))
 			                                                                                                                                      .add(aliasScrollPane,
 			                                                                                                                                           GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                           100,
 			                                                                                                                                           Short.MAX_VALUE))
 			                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                       .add(arrowButton1)
 			                                                                                                       .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                       .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                      .add(mappingAttributeComboBox,
 			                                                                                                                                           0,
 			                                                                                                                                           100,
 			                                                                                                                                           Short.MAX_VALUE)
 			                                                                                                                                      .add(nodeKeyLabel))
 			                                                                                                       .addContainerGap()));
 			attr2annotationPanelLayout.setVerticalGroup(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                      .add(attr2annotationPanelLayout.createSequentialGroup()
 			                                                                                                     .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
 			                                                                                                                                    .add(primaryKeyLabel)
 			                                                                                                                                    .add(nodeKeyLabel))
 			                                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                     .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
 			                                                                                                                                    .add(primaryLabel)
 			                                                                                                                                    .add(primaryKeyComboBox,
 			                                                                                                                                         GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                         GroupLayout.PREFERRED_SIZE))
 			                                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                     .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                    .add(aliasScrollPane,
 			                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                         100,
 			                                                                                                                                         Short.MAX_VALUE)
 			                                                                                                                                    .add(attr2annotationPanelLayout.createSequentialGroup()
 			                                                                                                                                                                   .add(attr2annotationPanelLayout.createParallelGroup(GroupLayout.TRAILING)
 			                                                                                                                                                                                                  .add(mappingAttributeComboBox,
 			                                                                                                                                                                                                       GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                                                                                       GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                                                                       GroupLayout.PREFERRED_SIZE)
 			                                                                                                                                                                                                  .add(arrowButton1))
 			                                                                                                                                                                   .addContainerGap()))));
 		}
 
 		textImportCheckBox.setText("Show Text File Import Options");
 		textImportCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		textImportCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		textImportCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					textImportCheckBoxActionPerformed(evt);
 				}
 			});
 
 		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			ontology2annotationPanel.setBackground(new java.awt.Color(250, 250, 250));
 			ontology2annotationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
 			                                                                                "Annotation File to Ontology Mapping",
 			                                                                                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 			                                                                                javax.swing.border.TitledBorder.DEFAULT_POSITION,
 			                                                                                new java.awt.Font("Dialog",
 			                                                                                                  1,
 			                                                                                                  11)));
 			targetOntologyLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			targetOntologyLabel.setForeground(new java.awt.Color(73, 127, 235));
 			targetOntologyLabel.setText("Ontology");
 
 			ontologyTextField.setFont(new java.awt.Font("SansSerif", 1, 14));
 			ontologyTextField.setForeground(ONTOLOGY_COLOR.getColor());
 			ontologyTextField.setBackground(Color.WHITE);
 			ontologyTextField.setEditable(false);
 			ontologyTextField.setToolTipText("This ontology will be used for mapping.");
 
 			ontologyInAnnotationLabel.setFont(new java.awt.Font("SansSerif", 1, 12));
 			ontologyInAnnotationLabel.setForeground(new java.awt.Color(0, 255, 255));
 			ontologyInAnnotationLabel.setText("Key Column in Annotation File");
 
 			ontologyInAnnotationComboBox.setForeground(ONTOLOGY_COLOR.getColor());
 			ontologyInAnnotationComboBox.addActionListener(new java.awt.event.ActionListener() {
 					public void actionPerformed(java.awt.event.ActionEvent evt) {
 						ontologyInAnnotationComboBoxActionPerformed(evt);
 					}
 				});
 
 			arrowButton2.setBackground(new java.awt.Color(250, 250, 250));
 			arrowButton2.setIcon(new javax.swing.ImageIcon(Cytoscape.class.getResource("images/ximian/stock_right-16.png")));
 			arrowButton2.setBorder(null);
 			arrowButton2.setBorderPainted(false);
 
 			GroupLayout ontology2annotationPanelLayout = new GroupLayout(ontology2annotationPanel);
 			ontology2annotationPanel.setLayout(ontology2annotationPanelLayout);
 			ontology2annotationPanelLayout.setHorizontalGroup(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                .add(ontology2annotationPanelLayout.createSequentialGroup()
 			                                                                                                                   .addContainerGap()
 			                                                                                                                   .add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                                                                                                      .add(ontologyInAnnotationLabel)
 			                                                                                                                                                      .add(ontologyInAnnotationComboBox,
 			                                                                                                                                                           0,
 			                                                                                                                                                           100,
 			                                                                                                                                                           Short.MAX_VALUE))
 			                                                                                                                   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                   .add(arrowButton2)
 			                                                                                                                   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                   .add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.TRAILING)
 			                                                                                                                                                      .add(GroupLayout.LEADING,
 			                                                                                                                                                           targetOntologyLabel)
 			                                                                                                                                                      .add(ontologyTextField,
 			                                                                                                                                                           GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                           100,
 			                                                                                                                                                           Short.MAX_VALUE))
 			                                                                                                                   .addContainerGap()));
 			ontology2annotationPanelLayout.setVerticalGroup(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.LEADING)
 			                                                                              .add(ontology2annotationPanelLayout.createSequentialGroup()
 			                                                                                                                 .add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
 			                                                                                                                                                    .add(ontologyInAnnotationLabel)
 			                                                                                                                                                    .add(targetOntologyLabel))
 			                                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                                                                 .add(ontology2annotationPanelLayout.createParallelGroup(GroupLayout.BASELINE)
 			                                                                                                                                                    .add(ontologyInAnnotationComboBox,
 			                                                                                                                                                         GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                         GroupLayout.PREFERRED_SIZE)
 			                                                                                                                                                    .add(ontologyTextField,
 			                                                                                                                                                         GroupLayout.PREFERRED_SIZE,
 			                                                                                                                                                         GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                                         GroupLayout.PREFERRED_SIZE)
 			                                                                                                                                                    .add(arrowButton2))
 			                                                                                                                 .addContainerGap(GroupLayout.DEFAULT_SIZE,
 			                                                                                                                                  Short.MAX_VALUE)));
 		}
 
 		/*
 		 * For Network Import
 		 */
 		if (dialogType == NETWORK_IMPORT) {
 			networkImportPanel = new NetworkImportOptionsPanel();
 			networkImportPanel.addPropertyChangeListener(this);
 			caseSensitiveCheckBox.setVisible(false);
 		}
 
 		textImportOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED),
 		                                                                             "Text File Import Options",
 		                                                                             javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 		                                                                             javax.swing.border.TitledBorder.DEFAULT_POSITION,
 		                                                                             new java.awt.Font("Dialog",
 		                                                                                               1,
 		                                                                                               11)));
 		delimiterPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Delimiter"));
 		tabCheckBox.setText("Tab");
 		tabCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		tabCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		tabCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						delimiterCheckBoxActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		commaCheckBox.setText("Comma");
 		commaCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		commaCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		commaCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						delimiterCheckBoxActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		semicolonCheckBox.setText("Semicolon");
 		semicolonCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		semicolonCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		semicolonCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						delimiterCheckBoxActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		spaceCheckBox.setText("Space");
 		spaceCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		spaceCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		spaceCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						delimiterCheckBoxActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		otherCheckBox.setText("Other");
 		otherCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		otherCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		otherCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						delimiterCheckBoxActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			});
 
 		otherDelimiterTextField.addKeyListener(new KeyListener() {
 				public void keyPressed(KeyEvent evt) {
 					try {
 						otherTextFieldActionPerformed(evt);
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				public void keyReleased(KeyEvent arg0) {
 				}
 
 				public void keyTyped(KeyEvent evt) {
 				}
 			});
 
 		GroupLayout delimiterPanelLayout = new GroupLayout(delimiterPanel);
 		delimiterPanel.setLayout(delimiterPanelLayout);
 		delimiterPanelLayout.setHorizontalGroup(delimiterPanelLayout.createParallelGroup(GroupLayout.LEADING)
 		                                                            .add(delimiterPanelLayout.createSequentialGroup()
 		                                                                                     .add(tabCheckBox)
 		                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                     .add(commaCheckBox)
 		                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                     .add(semicolonCheckBox)
 		                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                     .add(spaceCheckBox)
 		                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                     .add(otherCheckBox)
 		                                                                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                     .add(otherDelimiterTextField,
 		                                                                                          GroupLayout.DEFAULT_SIZE,
 		                                                                                          31,
 		                                                                                          Short.MAX_VALUE)));
 		delimiterPanelLayout.setVerticalGroup(delimiterPanelLayout.createParallelGroup(GroupLayout.LEADING)
 		                                                          .add(delimiterPanelLayout.createSequentialGroup()
 		                                                                                   .add(delimiterPanelLayout.createParallelGroup(GroupLayout.BASELINE)
 		                                                                                                            .add(tabCheckBox)
 		                                                                                                            .add(commaCheckBox)
 		                                                                                                            .add(semicolonCheckBox)
 		                                                                                                            .add(spaceCheckBox)
 		                                                                                                            .add(otherCheckBox)
 		                                                                                                            .add(otherDelimiterTextField,
 		                                                                                                                 GroupLayout.PREFERRED_SIZE,
 		                                                                                                                 GroupLayout.DEFAULT_SIZE,
 		                                                                                                                 GroupLayout.PREFERRED_SIZE))
 		                                                                                   .addContainerGap(GroupLayout.DEFAULT_SIZE,
 		                                                                                                    Short.MAX_VALUE)));
 
 		transferNameCheckBox.setEnabled(false);
 
 		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 1, 10000000, 10);
 		counterSpinner.setModel(spinnerModel);
 		counterSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
 				public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
 					counterSpinnerMouseWheelMoved(evt);
 				}
 			});
 		counterSpinner.setToolTipText("<html><body>Click <strong text=\"red\"><i>Refresh Preview</i></strong> button to update the table.</body></html>");
 
 		counterLabel.setText("entries.");
 
 		previewOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview Options"));
 		reloadButton.setText("Refresh Preview");
 		reloadButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
 		reloadButton.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					try {
 						reloadButtonActionPerformed(evt);
 					} catch (IOException e) {
 						e.printStackTrace();
 						throw new IllegalStateException("Could not reload target file.");
 					}
 				}
 			});
 
 		showAllRadioButton.setText("Show all entries in the file");
 		showAllRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		showAllRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
 		counterRadioButton.setText("Show first ");
 		counterRadioButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
 		counterRadioButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
 		org.jdesktop.layout.GroupLayout previewOptionPanelLayout = new org.jdesktop.layout.GroupLayout(previewOptionPanel);
 		previewOptionPanel.setLayout(previewOptionPanelLayout);
 
 		previewOptionPanelLayout.setHorizontalGroup(previewOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                    .add(previewOptionPanelLayout.createSequentialGroup()
 		                                                                                                 .addContainerGap()
 		                                                                                                 .add(showAllRadioButton)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(counterRadioButton)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(counterSpinner,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                      58,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(counterLabel)
 		                                                                                                 .addContainerGap(76,
 		                                                                                                                  Short.MAX_VALUE)));
 		previewOptionPanelLayout.setVerticalGroup(previewOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                  .add(counterRadioButton)
 		                                                                  .add(counterSpinner,
 		                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                       org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                       org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                  .add(counterLabel)
 		                                                                  .add(showAllRadioButton));
 
 		attributeNamePanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Attribute Names"));
 
 		transferNameCheckBox.setText("Transfer first line as attribute names");
 
 		transferNameCheckBox.setBorder(null);
 		transferNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
 		transferNameCheckBox.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent evt) {
 					transferNameCheckBoxActionPerformed(evt);
 				}
 			});
 
 		startRowLabel.setText("Start Import Row: ");
 		startRowLabel.setHorizontalAlignment(SwingConstants.RIGHT);
 
 		startRowSpinner.setName("startRowSpinner");
 
 		SpinnerNumberModel startRowSpinnerModel = new SpinnerNumberModel(1, 1, 10000000, 1);
 		startRowSpinner.setModel(startRowSpinnerModel);
 		startRowSpinner.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
 				public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
 					startRowSpinnerMouseWheelMoved(evt);
 				}
 			});
 		startRowSpinner.setToolTipText("<html>Load entries from this line. <p>"
 		                               + "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</p></html>");
 
 		commentLineLabel.setText("Comment Line:");
 
 		commentLineTextField.setToolTipText("<html>Lines start with this string will be ignored. <br>"
 		                                    + "(Click on the <strong><i>Refresh Preview</i></strong> button to refresh preview.)</html>");
 
 		GroupLayout attributeNamePanelLayout = new org.jdesktop.layout.GroupLayout(attributeNamePanel);
 		attributeNamePanel.setLayout(attributeNamePanelLayout);
 
 		attributeNamePanelLayout.setHorizontalGroup(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                    .add(attributeNamePanelLayout.createSequentialGroup()
 		                                                                                                 .add(transferNameCheckBox)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(startRowLabel)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(startRowSpinner,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                      51,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(commentLineLabel)
 		                                                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                 .add(commentLineTextField,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                      24,
 		                                                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                 .addContainerGap(12,
 		                                                                                                                  Short.MAX_VALUE)));
 		attributeNamePanelLayout.setVerticalGroup(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                  .add(attributeNamePanelLayout.createSequentialGroup()
 		                                                                                               .add(attributeNamePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                                            .add(transferNameCheckBox,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                 21,
 		                                                                                                                                 Short.MAX_VALUE)
 		                                                                                                                            .add(startRowLabel)
 		                                                                                                                            .add(startRowSpinner,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                                            .add(commentLineLabel)
 		                                                                                                                            .add(commentLineTextField,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                 org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 		                                                                                               .addContainerGap()));
 
 		networkImportOptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Import Options"));
 		defaultInteractionLabel.setText("Default Interaction:");
 		defaultInteractionTextField.setText(DEFAULT_INTERACTION);
 		defaultInteractionTextField.setToolTipText("<html>If <font color=\"red\"><i>Default Interaction</i></font>"
 		                                           + " is selected, this value will be used for <i>Interaction Type</i>.<br></html>");
 
 		org.jdesktop.layout.GroupLayout networkImportOptionPanelLayout = new org.jdesktop.layout.GroupLayout(networkImportOptionPanel);
 		networkImportOptionPanel.setLayout(networkImportOptionPanelLayout);
 
 		networkImportOptionPanelLayout.setHorizontalGroup(networkImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                                .add(networkImportOptionPanelLayout.createSequentialGroup()
 		                                                                                                                   .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                    Short.MAX_VALUE)
 		                                                                                                                   .add(defaultInteractionLabel)
 		                                                                                                                   .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                                   .add(defaultInteractionTextField,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                        58,
 		                                                                                                                        org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 		networkImportOptionPanelLayout.setVerticalGroup(networkImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                              .add(defaultInteractionLabel)
 		                                                                              .add(defaultInteractionTextField,
 		                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                   org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                   org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
 
 		org.jdesktop.layout.GroupLayout textImportOptionPanelLayout = new org.jdesktop.layout.GroupLayout(textImportOptionPanel);
 		textImportOptionPanel.setLayout(textImportOptionPanelLayout);
 
 		textImportOptionPanelLayout.setHorizontalGroup(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                          .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                                               textImportOptionPanelLayout.createSequentialGroup()
 		                                                                                                          .add(attributeNamePanel,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               Short.MAX_VALUE)
 		                                                                                                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                          .add(networkImportOptionPanel,
 		                                                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                          .add(29,
 		                                                                                                               29,
 		                                                                                                               29)
 		                                                                                                          .add(reloadButton))
 		                                                                          .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                                               textImportOptionPanelLayout.createSequentialGroup()
 		                                                                                                          .add(delimiterPanel,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               Short.MAX_VALUE)
 		                                                                                                          .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                          .add(previewOptionPanel,
 		                                                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 		textImportOptionPanelLayout.setVerticalGroup(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                        .add(textImportOptionPanelLayout.createSequentialGroup()
 		                                                                                                        .add(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                                                                                        .add(delimiterPanel,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                                             71,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                                                        .add(previewOptionPanel,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                             50,
 		                                                                                                                                             Short.MAX_VALUE))
 		                                                                                                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                                        .add(textImportOptionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                                                                                                        .add(networkImportOptionPanel,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                                                                             45,
 		                                                                                                                                             Short.MAX_VALUE)
 		                                                                                                                                        .add(attributeNamePanel,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                                                                             71,
 		                                                                                                                                             org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                                                                        .add(org.jdesktop.layout.GroupLayout.TRAILING,
 		                                                                                                                                             reloadButton))));
 
 		org.jdesktop.layout.GroupLayout advancedPanelLayout = new org.jdesktop.layout.GroupLayout(advancedPanel);
 		advancedPanel.setLayout(advancedPanelLayout);
 
 		advancedPanelLayout.setHorizontalGroup(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                          .add(advancedPanelLayout.createSequentialGroup()
 		                                                                                  .addContainerGap()
 		                                                                                  .add(advancedOptionCheckBox)
 		                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                  .add(textImportCheckBox)
 		                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                  .add(importAllCheckBox)
 		                                                                                  .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                  .add(caseSensitiveCheckBox))
 		                                                          .add(attr2annotationPanel,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               Short.MAX_VALUE)
 		                                                          .add(ontology2annotationPanel,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               Short.MAX_VALUE)
 		                                                          .add(textImportOptionPanel,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                               Short.MAX_VALUE));
 		advancedPanelLayout.setVerticalGroup(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 		                                                        .add(advancedPanelLayout.createSequentialGroup()
 		                                                                                .add(advancedPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 		                                                                                                        .add(advancedOptionCheckBox)
 		                                                                                                        .add(textImportCheckBox)
 		                                                                                                        .add(importAllCheckBox)
 		                                                                                                        .add(caseSensitiveCheckBox))
 		                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                .add(attr2annotationPanel,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                .add(ontology2annotationPanel,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 		                                                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 		                                                                                .add(textImportOptionPanel,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 		                                                                                     org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)));
 
 		globalLayout();
 
 		attr2annotationPanel.setVisible(false);
 		basicPanel.repaint();
 		ontology2annotationPanel.setVisible(false);
 		textImportOptionPanel.setVisible(false);
 
 		pack();
 	} // </editor-fold>
 
 	/**
 	 * Update UI based on the primary key selection.
 	 * @param evt
 	 */
 	private void primaryKeyComboBoxActionPerformed(ActionEvent evt) {
 		// Not necessary in Network Import.
 		if (dialogType == NETWORK_IMPORT)
 			return;
 
 		// Update primary key index.
 		keyInFile = primaryKeyComboBox.getSelectedIndex();
 
 		// Update 
 		previewPanel.getPreviewTable()
 		            .setDefaultRenderer(Object.class, getRenderer(previewPanel.getFileType()));
 
 		try {
 			if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
				setStatusBar(new URL(targetDataSourceTextField.getText()));
 			} else {
 				setStatusBar(new URL(annotationUrlMap.get(annotationComboBox.getSelectedItem()
 				                                                            .toString())));
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		previewPanel.repaint();
 
 		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
 		curTable.setModel(aliasTableModelMap.get(previewPanel.getSelectedSheetName()));
 
 		if (curTable.getCellRenderer(0, 1) != null) {
 			((AliasTableRenderer) curTable.getCellRenderer(0, 1)).setPrimaryKey(keyInFile);
 			aliasScrollPane.setViewportView(curTable);
 
 			primaryKeyMap.put(previewPanel.getSelectedSheetName(),
 			                  primaryKeyComboBox.getSelectedIndex());
 
 			aliasScrollPane.setViewportView(curTable);
 			curTable.repaint();
 		}
 
 		// Update table view
 		ColumnResizer.adjustColumnPreferredWidths(previewPanel.getPreviewTable());
 		previewPanel.getPreviewTable().repaint();
 	}
 
 	private void helpButtonActionPerformed(ActionEvent evt) {
 		// Quick help should be implemented!
 	}
 
 	private void otherTextFieldActionPerformed(KeyEvent evt) throws IOException {
 		if (otherCheckBox.isSelected()) {
 			displayPreview();
 		}
 	}
 
 	private void attributeRadioButtonActionPerformed(ActionEvent evt) {
 		if (nodeRadioButton.isSelected()) {
 			selectedAttributes = Cytoscape.getNodeAttributes();
 			objType = NODE;
 		} else if (edgeRadioButton.isSelected()) {
 			selectedAttributes = Cytoscape.getEdgeAttributes();
 			objType = EDGE;
 		} else {
 			selectedAttributes = Cytoscape.getNetworkAttributes();
 			objType = NETWORK;
 		}
 
 		updateMappingAttributeComboBox();
 		setKeyList();
 	}
 
 	private void advancedOptionCheckBoxActionPerformed(ActionEvent evt) {
 		if (advancedOptionCheckBox.isSelected()) {
 			attr2annotationPanel.setVisible(true);
 			ontology2annotationPanel.setVisible(true);
 		} else {
 			attr2annotationPanel.setVisible(false);
 			ontology2annotationPanel.setVisible(false);
 		}
 
 		if (dialogType == ImportTextTableDialog.SIMPLE_ATTRIBUTE_IMPORT) {
 			ontology2annotationPanel.setVisible(false);
 		}
 
 		pack();
 	}
 
 	/**
 	 * If Import All selected, ID combo box should be set to ID
 	 * @param evt
 	 */
 	private void importAllCheckBoxActionPerformed(ActionEvent evt) {
 		if (importAllCheckBox.isSelected()) {
 			// Lock key to ID
 			mappingAttributeComboBox.setSelectedItem(ID);
 			mappingAttributeComboBox.setEnabled(false);
 		} else {
 			mappingAttributeComboBox.setEnabled(true);
 		}
 	}
 
 	private void nodeKeyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		previewPanel.getPreviewTable()
 		            .setDefaultRenderer(Object.class, getRenderer(previewPanel.getFileType()));
 
 		setKeyList();
 	}
 
 	private void browseOntologyButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ONTOLOGY_TYPE,
 		                                                         Cytoscape.getDesktop(), true);
 		dssd.setLocationRelativeTo(Cytoscape.getDesktop());
 		dssd.setVisible(true);
 
 		String key = dssd.getSourceName();
 
 		if (key != null) {
 			ontologyComboBox.insertItemAt(key, 0);
 			ontologyUrlMap.put(key, dssd.getSourceUrlString());
 			ontologyComboBox.setSelectedItem(key);
 			ontologyComboBox.setToolTipText(getOntologyTooltip());
 		}
 	}
 
 	private void transferNameCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();
 
 		if (transferNameCheckBox.isSelected()) {
 			if ((previewPanel.getPreviewTable() != null) && (model != null)) {
 				columnHeaders = new String[previewPanel.getPreviewTable().getColumnCount()];
 
 				for (int i = 0; i < columnHeaders.length; i++) {
 					// Save the header
 					columnHeaders[i] = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
 					                               .getHeaderValue().toString();
 					previewPanel.getPreviewTable().getColumnModel().getColumn(i)
 					            .setHeaderValue((String) model.getValueAt(0, i));
 				}
 
 				model.removeRow(0);
 				previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
 			}
 
 			startRowSpinner.setEnabled(false);
 		} else {
 			// Restore row
 			String currentName = null;
 			Object headerVal = null;
 
 			for (int i = 0; i < columnHeaders.length; i++) {
 				headerVal = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
 				                        .getHeaderValue();
 
 				if (headerVal == null) {
 					currentName = "";
 				} else {
 					currentName = headerVal.toString();
 				}
 
 				previewPanel.getPreviewTable().getColumnModel().getColumn(i)
 				            .setHeaderValue(columnHeaders[i]);
 				columnHeaders[i] = currentName;
 			}
 
 			model.insertRow(0, columnHeaders);
 			previewPanel.getPreviewTable().getTableHeader().resizeAndRepaint();
 			startRowSpinner.setEnabled(true);
 		}
 
 		updateAliasTable();
 		updatePrimaryKeyComboBox();
 		repaint();
 	}
 
 	private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		dispose();
 	}
 
 	/**
 	 * Load from the data source.<br>
 	 *
 	 * @param evt
 	 * @throws Exception
 	 */
 	private void importButtonActionPerformed(ActionEvent evt) throws Exception {
 		if (checkDataSourceError() == false)
 			return;
 
 		boolean importAll = importAllCheckBox.isSelected();
 
 		/*
 		 * Get start line number. If "transfer" check box is true, then start
 		 * reading from the second line.
 		 */
 		int startLineNumber;
 		final int spinnerNumber = Integer.parseInt(startRowSpinner.getValue().toString());
 
 		if (transferNameCheckBox.isSelected()) {
 			startLineNumber = spinnerNumber;
 		} else {
 			startLineNumber = spinnerNumber - 1;
 		}
 
 		final String commentChar = commentLineTextField.getText();
 
 		/*
 		 * Get import flags
 		 */
 		final int colCount = previewPanel.getPreviewTable().getColumnModel().getColumnCount();
 		importFlag = new boolean[colCount];
 
 		for (int i = 0; i < colCount; i++) {
 			importFlag[i] = ((AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
 			                                                                 .getCellRenderer(0, i))
 			                .getImportFlag(i);
 		}
 
 		/*
 		 * Get Attribute Names
 		 */
 		final String[] attributeNames;
 		final List<String> attrNameList = new ArrayList<String>();
 
 		Object curName = null;
 
 		for (int i = 0; i < colCount; i++) {
 			curName = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();
 
 			if (attrNameList.contains(curName)) {
 				int dupIndex = 0;
 
 				for (int idx = 0; idx < attrNameList.size(); idx++) {
 					if (curName.equals(attrNameList.get(idx))) {
 						dupIndex = idx;
 
 						break;
 					}
 				}
 
 				if (importFlag[i] && importFlag[dupIndex]) {
 					final JLabel label = new JLabel("Duplicate Attribute Name Found: " + curName);
 					label.setForeground(Color.RED);
 					JOptionPane.showMessageDialog(this, label);
 
 					return;
 				}
 			}
 
 			if (curName == null) {
 				attrNameList.add("Column " + i);
 			} else {
 				attrNameList.add(curName.toString());
 			}
 		}
 
 		attributeNames = attrNameList.toArray(new String[0]);
 
 		/*
 		 * Get attribute data types
 		 */
 
 		// final byte[] attributeTypes = new byte[previewPanel.getPreviewTable()
 		// .getColumnCount()];
 		final Byte[] test = previewPanel.getDataTypes(previewPanel.getSelectedSheetName());
 		final Byte[] attributeTypes = new Byte[test.length];
 
 		for (int i = 0; i < test.length; i++) {
 			attributeTypes[i] = test[i];
 		}
 
 		// for (int i = 0; i < attributeTypes.length; i++) {
 		// attributeTypes[i] = attributeDataTypes.get(i);
 		// }
 		final List<Integer> aliasList = new ArrayList<Integer>();
 		String mappingAttribute = ID;
 
 		if (dialogType != NETWORK_IMPORT) {
 			/*
 			 * Get column indecies for alias
 			 */
 			JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
 
 			if (curTable != null) {
 				for (int i = 0; i < curTable.getModel().getRowCount(); i++) {
 					if ((Boolean) curTable.getModel().getValueAt(i, 0) == true) {
 						aliasList.add(i);
 					}
 				}
 			}
 
 			/*
 			 * Get mapping attribute
 			 */
 			mappingAttribute = mappingAttributeComboBox.getSelectedItem().toString();
 		}
 
 		ObjectType objType = null;
 
 		if (dialogType != NETWORK_IMPORT) {
 			if (nodeRadioButton.isSelected()) {
 				objType = NODE;
 			} else if (edgeRadioButton.isSelected()) {
 				objType = EDGE;
 			} else {
 				objType = NETWORK;
 			}
 		}
 
 		/*
 		 * Switch readers based on the dialog type.
 		 */
 		switch (dialogType) {
 			case SIMPLE_ATTRIBUTE_IMPORT:
 
 				/*
 				 * Case 1: Attribute table import.
 				 */
 				// Extract URL from the text table.
 				final URL source = new URL(targetDataSourceTextField.getText());
 				// Make sure primary key index is up-to-date.
 				keyInFile = primaryKeyComboBox.getSelectedIndex();
 
 				// Build mapping parameter object.
 				final AttributeMappingParameters mapping;
 				final List<String> del;
 
 				System.out.println("IsCytoscapeAttributeFile " + previewPanel.isCytoscapeAttributeFile(source));
 				if (previewPanel.isCytoscapeAttributeFile(source)) {
 					del = new ArrayList<String>();
 					del.add(" += +");
 				} else {
 					del = checkDelimiter();
 				}
 				mapping = new AttributeMappingParameters(objType, del,
 														 listDelimiter, keyInFile,
 														 mappingAttribute, aliasList,
 														 attributeNames, attributeTypes,
 														 listDataTypes, importFlag,
 														 caseSensitive);
 
 				if (source.toString().endsWith(SupportedFileType.EXCEL.getExtension()) || 
 						source.toString().endsWith(SupportedFileType.OOXML.getExtension())) {
 					/*
 					 * Read one sheet at a time
 					 */
 					InputStream is = null;
 					Workbook wb = null;
 					
 					try {
 						is = source.openStream();
 						wb = WorkbookFactory.create(is);
 					}
 					finally {
 						if (is != null) {
 							is.close();
 						}
 					}
 
 					// Load all sheets in the table
 					for (int i = 0; i < wb.getNumberOfSheets(); i++) {
 						final Sheet sheet = wb.getSheetAt(i);
 
 						loadAnnotation(new ExcelAttributeSheetReader(sheet, mapping,
 						                                             startLineNumber,
 																	 importAll),
 									   source.toString());
 					}
 				} else {
 					loadAnnotation(new DefaultAttributeTableReader(source, mapping,
 																   startLineNumber,
 																   null, importAll),
 								   source.toString());
 				}
 
 				break;
 
 			case ONTOLOGY_AND_ANNOTATION_IMPORT:
 
 				/*
 				 * Case 2: Import Ontology and its annotation.
 				 */
 				final String selectedOntologyName = ontologyComboBox.getSelectedItem().toString();
 				final String ontologySourceLocation = ontologyUrlMap.get(selectedOntologyName);
 
 				/*
 				 * If selected ontology is not loaded, load it first.
 				 */
 				if (Cytoscape.getOntologyServer().getOntologyNames().contains(selectedOntologyName) == false)
 					loadOntology(ontologySourceLocation, selectedOntologyName);
 
 				/*
 				 * Now, load & map annotation.
 				 */
 				final String annotationSource = annotationUrlMap.get(annotationComboBox.getSelectedItem());
 				final URL annotationSourceUrl = new URL(annotationSource);
 
 				if (previewPanel.getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
 					/*
 					 * This is a Gene Association file.
 					 */
 					GeneAssociationReader gaReader = null;
 					keyInFile = this.primaryKeyComboBox.getSelectedIndex();
 
 					InputStream is = null;
 					try {
 						is = URLUtil.getInputStream(annotationSourceUrl);
 						gaReader = new GeneAssociationReader(selectedOntologyName,
 															 is, mappingAttribute,
 															 importAll, keyInFile,
 															 caseSensitive);
 					}
 					catch (Exception e) {
 						if (is != null) {
 							is.close();
 						}
 						throw e;
 					}
 
 					loadGeneAssociation(gaReader, selectedOntologyName, annotationSource);
 				} else {
 					/*
 					 * This is a custom annotation file.
 					 */
 					final int ontologyIndex = ontologyInAnnotationComboBox.getSelectedIndex();
 
 					final AttributeAndOntologyMappingParameters aoMapping = new AttributeAndOntologyMappingParameters(objType,
 					                                                                                                  checkDelimiter(),
 					                                                                                                  listDelimiter,
 					                                                                                                  keyInFile,
 					                                                                                                  mappingAttribute,
 					                                                                                                  aliasList,
 					                                                                                                  attributeNames,
 					                                                                                                  attributeTypes,
 					                                                                                                  listDataTypes,
 					                                                                                                  importFlag,
 					                                                                                                  ontologyIndex,
 					                                                                                                  selectedOntologyName,
 																													  caseSensitive);
 					final OntologyAnnotationReader oaReader = new OntologyAnnotationReader(annotationSourceUrl,
 					                                                                       aoMapping,
 					                                                                       commentChar,
 					                                                                       startLineNumber);
 
 					loadAnnotation(oaReader, annotationSource);
 				}
 
 				break;
 
 			case NETWORK_IMPORT:
 
 				/*
 				 * Case 3: read as network table (Network + Edge Attributes)
 				 */
 
 				// Extract URL from the text table.
 				/*
 				 * Now multiple files are supported.
 				 */
 				URL[] sources = new URL[inputFiles.length];
 
 				for (int i = 0; i < sources.length; i++) {
 					sources[i] = inputFiles[i].toURI().toURL();
 				}
 
 				//final URL networkSource = new URL(targetDataSourceTextField.getText());
 				final int sourceColumnIndex = networkImportPanel.getSourceIndex();
 				final int targetColumnIndex = networkImportPanel.getTargetIndex();
 
 				final String defaultInteraction = defaultInteractionTextField.getText();
 
 				final int interactionColumnIndex = networkImportPanel.getInteractionIndex();
 
 				final NetworkTableMappingParameters nmp = new NetworkTableMappingParameters(checkDelimiter(),
 				                                                                            listDelimiter,
 				                                                                            attributeNames,
 				                                                                            attributeTypes,
 				                                                                            null,
 				                                                                            importFlag,
 				                                                                            sourceColumnIndex,
 				                                                                            targetColumnIndex,
 				                                                                            interactionColumnIndex,
 				                                                                            defaultInteraction);
 
 				NetworkTableReader reader;
 				String networkName;
 				boolean multi = true;
 
 				if (sources.length == 1)
 					multi = false;
 
 				for (int i = 0; i < sources.length; i++) {
 					if (sources[i].toString().endsWith(SupportedFileType.EXCEL.getExtension()) || sources[i].toString().endsWith(SupportedFileType.OOXML.getExtension())) {
 						// Extract name from the sheet name.
 						InputStream is = null;
 						Workbook wb = null;
 						try {
 							is = sources[i].openStream();
 							wb = WorkbookFactory.create(is);
 						}
 						finally {
 							if (is != null) {
 								is.close();
 							}
 						}
 						
 						
 						Sheet sheet = wb.getSheetAt(0);
 						networkName = wb.getSheetName(0);
 
 						reader = new ExcelNetworkSheetReader(networkName, sheet, nmp,
 						                                     startLineNumber);
 					} else {
 						// Get name from URL.
 						if ((commentChar != null) && (commentChar.length() != 0)
 						    && transferNameCheckBox.isSelected()) {
 							startLineNumber++;
 						}
 
 						final String[] parts = sources[i].toString().split("/");
 						networkName = parts[parts.length - 1];
 						reader = new NetworkTableReader(networkName, sources[i], nmp,
 						                                startLineNumber, commentChar);
 					}
 
 					loadNetwork(networkName, reader, sources[i], multi);
 				}
 
 				if (multi) {
 					StringBuilder builder = new StringBuilder();
 					builder.append("The following networks are loaded:\n\n");
 
 					for (File f : inputFiles) {
 						builder.append(f.getName() + "\n");
 					}
 
 					JOptionPane.showMessageDialog(this, builder.toString(),
 					                              "Multiple Networks Loaded",
 					                              JOptionPane.INFORMATION_MESSAGE);
 				}
 
 				break;
 
 			default:
 				return;
 		}
 
 		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 		dispose();
 	}
 
 	private void ontologyInAnnotationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		/*
 		 * Change color of the column in the preview panel.
 		 */
 		int ontologyCol = ontologyInAnnotationComboBox.getSelectedIndex();
 		List<Integer> gaAlias = new ArrayList<Integer>();
 		gaAlias.add(DB_OBJECT_SYNONYM.getPosition());
 		previewPanel.getPreviewTable()
 		            .setDefaultRenderer(Object.class,
 		                                new AttributePreviewTableCellRenderer(keyInFile, gaAlias,
 		                                                                      ontologyCol,
 		                                                                      TAXON.getPosition(),
 		                                                                      importFlag,
 		                                                                      listDelimiter));
 
 		try {
 			if ((dialogType == SIMPLE_ATTRIBUTE_IMPORT) || (dialogType == NETWORK_IMPORT)) {
 				setStatusBar(new URL(targetDataSourceTextField.getText()));
 			} else {
 				setStatusBar(new URL(annotationUrlMap.get(annotationComboBox.getSelectedItem()
 				                                                            .toString())));
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		previewPanel.repaint();
 	}
 
 	private void browseAnnotationButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		DataSourceSelectDialog dssd = new DataSourceSelectDialog(DataSourceSelectDialog.ANNOTATION_TYPE,
 		                                                         Cytoscape.getDesktop(), true);
 		dssd.setLocationRelativeTo(Cytoscape.getDesktop());
 		dssd.setVisible(true);
 
 		String key = dssd.getSourceName();
 
 		if (key != null) {
 			annotationComboBox.addItem(key);
 			annotationUrlMap.put(key, dssd.getSourceUrlString());
 			annotationComboBox.setSelectedItem(key);
 			annotationComboBox.setToolTipText(getAnnotationTooltip());
 		}
 	}
 
 	private void annotationComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		if (annotationComboBox.getSelectedItem().toString().equals(DEF_ANNOTATION_ITEM)) {
 			annotationComboBox.setToolTipText(null);
 
 			return;
 		}
 
 		annotationComboBox.setToolTipText(getAnnotationTooltip());
 
 		try {
 			final String selectedSourceName = annotationComboBox.getSelectedItem().toString();
 			final URL sourceURL = new URL(annotationUrlMap.get(selectedSourceName));
 			readAnnotationForPreview(sourceURL, checkDelimiter());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private void ontologyComboBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		ontologyComboBox.setToolTipText(getOntologyTooltip());
 		ontologyTextField.setText(ontologyComboBox.getSelectedItem().toString());
 	}
 
 	private void selectAttributeFileButtonActionPerformed(ActionEvent evt)
 	    throws IOException
 	{
 		final File[] multiSource = FileUtil.getFiles("Select local file", FileUtil.LOAD,
 		                                             new CyFileFilter[] {  });
 		if ((multiSource == null) || (multiSource[0] == null))
 			return;
 		fileLoadAction(multiSource, multiSource[0].toURI().toURL().toString());
 	}
 
 	private void delimiterCheckBoxActionPerformed(ActionEvent evt) throws IOException {
 		transferNameCheckBox.setSelected(false);
 		displayPreview();
 	}
 
 	private void textImportCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {
 		if (textImportCheckBox.isSelected()) {
 			textImportOptionPanel.setVisible(true);
 		} else {
 			textImportOptionPanel.setVisible(false);
 		}
 
 		pack();
 	}
 
 	
 	private void reloadButtonActionPerformed(java.awt.event.ActionEvent evt)
 	    throws IOException {
 		displayPreview();
 
 		if (transferNameCheckBox.isSelected())
 			this.transferNameCheckBoxActionPerformed(null);
 	}
 
 	/**
 	 * Actions for mouse wheel movement
 	 *
 	 * @param evt
 	 */
 	private void counterSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
 		JSpinner source = (JSpinner) evt.getSource();
 
 		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
 		Integer oldValue = (Integer) source.getValue();
 		int intValue = oldValue.intValue()
 		               - (evt.getWheelRotation() * model.getStepSize().intValue());
 		Integer newValue = new Integer(intValue);
 
 		if ((model.getMaximum().compareTo(newValue) >= 0)
 		    && (model.getMinimum().compareTo(newValue) <= 0)) {
 			source.setValue(newValue);
 		}
 	}
 
 	/**
 	 * Actions for selecting start line.
 	 *
 	 * @param evt
 	 */
 	private void startRowSpinnerMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
 		JSpinner source = (JSpinner) evt.getSource();
 
 		SpinnerNumberModel model = (SpinnerNumberModel) source.getModel();
 		Integer oldValue = (Integer) source.getValue();
 		int intValue = oldValue.intValue()
 		               - (evt.getWheelRotation() * model.getStepSize().intValue());
 		Integer newValue = new Integer(intValue);
 
 		if ((model.getMaximum().compareTo(newValue) >= 0)
 		    && (model.getMinimum().compareTo(newValue) <= 0)) {
 			source.setValue(newValue);
 		}
 	}
 
 	/* =============================================================================================== */
 	private List<Integer> getAliasList() {
 		final List<Integer> aliasList = new ArrayList<Integer>();
 		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
 
 		if (curModel == null) {
 			return aliasList;
 		}
 
 		for (int i = 0; i < curModel.getRowCount(); i++) {
 			if ((Boolean) curModel.getValueAt(i, 0)) {
 				aliasList.add(i);
 			}
 		}
 
 		return aliasList;
 	}
 
 	
 	private void displayPreview() throws IOException {
 		final String selectedSourceName;
 		final URL sourceURL;
 
 		if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			selectedSourceName = annotationComboBox.getSelectedItem().toString();
 			sourceURL = new URL(annotationUrlMap.get(selectedSourceName));
 		} else {
 			selectedSourceName = targetDataSourceTextField.getText();
 			sourceURL = new URL(selectedSourceName);
 		}
 
 		readAnnotationForPreview(sourceURL, checkDelimiter());
 		previewPanel.repaint();
 	}
 
 	private void updateComponents() throws JAXBException, IOException {
 		/*
 		 * Do misc. GUI setups
 		 */
 		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
 			setTitle("Import Annotation File");
 			titleLabel.setText("Import Attribute from Table");
 			annotationAndOntologyImportPanel.setVisible(false);
 			importAllCheckBox.setVisible(true);
 		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			setTitle("Import Ontology Data and Annotations");
 			titleLabel.setText("Import Ontology and Annotation");
 			ontology2annotationPanel.setVisible(false);
 			/*
 			 * Add items to Ontology Combobox
 			 */
 			setOntologyComboBox();
 			/*
 			 * Setup annotation Combo Box
 			 */
 			setAnnotationComboBox();
 
 			ontologyTextField.setText(ontologyComboBox.getSelectedItem().toString());
 		} else if (dialogType == NETWORK_IMPORT) {
 			setTitle("Import Network and Edge Attributes from Table");
 			titleLabel.setText("Import Network from Table");
 			annotationAndOntologyImportPanel.setVisible(false);
 
 			importAllCheckBox.setVisible(false);
 		}
 
 		reloadButton.setEnabled(false);
 		startRowSpinner.setEnabled(false);
 		startRowLabel.setEnabled(false);
 		previewPanel.getPreviewTable().getTableHeader().setReorderingAllowed(false);
 		setRadioButtonGroup();
 		pack();
 
 		updateMappingAttributeComboBox();
 
 		setStatusBar("-", "-", "File Size: Unknown");
 	}
 
 	private void updatePrimaryKeyComboBox() {
 		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();
 
 		primaryKeyComboBox.setRenderer(new ComboBoxRenderer(attributeDataTypes));
 
 		if ((model != null) && (model.getColumnCount() > 0)) {
 			primaryKeyComboBox.removeAllItems();
 
 			Object curValue = null;
 
 			for (int i = 0; i < model.getColumnCount(); i++) {
 				curValue = previewPanel.getPreviewTable().getColumnModel().getColumn(i)
 				                       .getHeaderValue();
 
 				if (curValue != null) {
 					primaryKeyComboBox.addItem(curValue.toString());
 				} else {
 					primaryKeyComboBox.addItem("");
 				}
 			}
 		}
 
 		primaryKeyComboBox.setEnabled(true);
 
 		Integer selectedIndex = primaryKeyMap.get(previewPanel.getSelectedSheetName());
 
 		if (selectedIndex == null) {
 			primaryKeyComboBox.setSelectedIndex(0);
 		} else {
 			primaryKeyComboBox.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	protected static ImageIcon getDataTypeIcon(byte dataType) {
 		ImageIcon dataTypeIcon = null;
 
 		if (dataType == CyAttributes.TYPE_STRING) {
 			dataTypeIcon = STRING_ICON.getIcon();
 		} else if (dataType == CyAttributes.TYPE_INTEGER) {
 			dataTypeIcon = INT_ICON.getIcon();
 		} else if (dataType == CyAttributes.TYPE_FLOATING) {
 			dataTypeIcon = FLOAT_ICON.getIcon();
 		} else if (dataType == CyAttributes.TYPE_BOOLEAN) {
 			dataTypeIcon = BOOLEAN_ICON.getIcon();
 		} else if (dataType == CyAttributes.TYPE_SIMPLE_LIST) {
 			dataTypeIcon = LIST_ICON.getIcon();
 		}
 
 		return dataTypeIcon;
 	}
 
 	private void setRadioButtonGroup() {
 		attrTypeButtonGroup.add(nodeRadioButton);
 		attrTypeButtonGroup.add(edgeRadioButton);
 		attrTypeButtonGroup.add(networkRadioButton);
 		attrTypeButtonGroup.setSelected(nodeRadioButton.getModel(), true);
 
 		importTypeButtonGroup.add(showAllRadioButton);
 		importTypeButtonGroup.add(counterRadioButton);
 		importTypeButtonGroup.setSelected(counterRadioButton.getModel(), true);
 
 		tabCheckBox.setSelected(true);
 		tabCheckBox.setEnabled(false);
 		commaCheckBox.setEnabled(false);
 
 		spaceCheckBox.setEnabled(false);
 
 		if (dialogType == NETWORK_IMPORT) {
 			spaceCheckBox.setSelected(true);
 		} else {
 			spaceCheckBox.setSelected(false);
 		}
 
 		semicolonCheckBox.setEnabled(false);
 		otherCheckBox.setEnabled(false);
 		otherDelimiterTextField.setEnabled(false);
 	}
 
 	private void setOntologyInAnnotationComboBox() {
 		final DefaultTableModel model = (DefaultTableModel) previewPanel.getPreviewTable().getModel();
 
 		if ((model != null) && (model.getColumnCount() > 0)) {
 			ontologyInAnnotationComboBox.removeAllItems();
 
 			for (int i = 0; i < model.getColumnCount(); i++) {
 				ontologyInAnnotationComboBox.addItem(previewPanel.getPreviewTable().getColumnModel()
 				                                                 .getColumn(i).getHeaderValue()
 				                                                 .toString());
 			}
 		}
 
 		ontologyInAnnotationComboBox.setEnabled(true);
 	}
 
 	/**
 	 * Setup ontology data source combo box.<br>
 	 * Basically, this method just load informaiton from bookmark.
 	 * @throws IOException
 	 * @throws JAXBException
 	 *
 	 * @throws IOException
 	 * @throws JAXBException
 	 *
 	 */
 	private void setOntologyComboBox() throws JAXBException, IOException {
 		Bookmarks bookmarks = Cytoscape.getBookmarks();
 		List<DataSource> annotations = BookmarksUtil.getDataSourceList("ontology",
 		                                                               bookmarks.getCategory());
 		String key = null;
 
 		Set<String> ontologyNames = Cytoscape.getOntologyServer().getOntologyNames();
 
 		for (DataSource source : annotations) {
 			key = source.getName();
 			ontologyComboBox.addItem(key);
 			ontologyUrlMap.put(key, source.getHref());
 			ontologyDescriptionMap.put(key, BookmarksUtil.getAttribute(source, "description"));
 			ontologyTypeMap.put(key, BookmarksUtil.getAttribute(source, "ontologyType"));
 		}
 
 		ontologyComboBox.setToolTipText(getOntologyTooltip());
 	}
 
 	private String getOntologyTooltip() {
 		final String key = ontologyComboBox.getSelectedItem().toString();
 		String tooltip = ontologyHtml.replace("%DataSourceName%", key);
 		final String description = ontologyDescriptionMap.get(key);
 
 		if (description == null) {
 			tooltip = tooltip.replace("%Description%", "N/A");
 		} else {
 			tooltip = tooltip.replace("%Description%", description);
 		}
 
 		if (ontologyUrlMap.get(key) != null) {
 			return tooltip.replace("%SourceURL%", ontologyUrlMap.get(key));
 		} else {
 			return tooltip.replace("%SourceURL%", "N/A");
 		}
 	}
 
 	private String getAnnotationTooltip() {
 		final String key = annotationComboBox.getSelectedItem().toString();
 		String tooltip = annotationHtml.replace("%DataSourceName%", key);
 
 		if (annotationUrlMap.get(key) == null) {
 			return "";
 		}
 
 		tooltip = tooltip.replace("%SourceURL%", annotationUrlMap.get(key));
 
 		if (annotationFormatMap.get(key) != null) {
 			tooltip = tooltip.replace("%Format%", annotationFormatMap.get(key));
 		} else {
 			String[] parts = annotationUrlMap.get(key).split("/");
 
 			if (parts[parts.length - 1].startsWith(GENE_ASSOCIATION)) {
 				tooltip = tooltip.replace("%Format%", "Gene Association");
 			}
 
 			tooltip = tooltip.replace("%Format%", "General Annotation Text Table");
 		}
 
 		if (annotationAttributesMap.get(key) != null) {
 			StringBuffer table = new StringBuffer();
 			final Map<String, String> annotations = annotationAttributesMap.get(key);
 
 			for (String anno : annotations.keySet()) {
 				table.append("<tr>");
 				table.append("<td><strong>" + anno + "</strong></td><td>" + annotations.get(anno)
 				             + "</td>");
 				table.append("</tr>");
 			}
 
 			return tooltip.replace("%AttributeTable%", table.toString());
 		}
 
 		return tooltip.replace("%AttributeTable%", "");
 	}
 
 	private void setAnnotationComboBox() throws JAXBException, IOException {
 		Bookmarks bookmarks = Cytoscape.getBookmarks();
 		List<DataSource> annotations = BookmarksUtil.getDataSourceList("annotation",
 		                                                               bookmarks.getCategory());
 		String key = null;
 
 		annotationComboBox.addItem(DEF_ANNOTATION_ITEM);
 
 		for (DataSource source : annotations) {
 			key = source.getName();
 			annotationComboBox.addItem(key);
 			annotationUrlMap.put(key, source.getHref());
 			annotationFormatMap.put(key, source.getFormat());
 
 			final Map<String, String> attrMap = new HashMap<String, String>();
 
 			for (Attribute attr : source.getAttribute()) {
 				attrMap.put(attr.getName(), attr.getContent());
 			}
 
 			annotationAttributesMap.put(key, attrMap);
 		}
 
 		// annotationComboBox.setToolTipText(getAnnotationTooltip());
 	}
 
 	/**
 	 * Generate preview table.<br>
 	 *
 	 * <p>
 	 * </p>
 	 *
 	 * @throws IOException
 	 */
 	private void readAnnotationForPreview(URL sourceURL, List<String> delimiters)
 	    throws IOException {
 		/*
 		 * Check number of lines we should load. if -1, load everything in the
 		 * file.
 		 */
 		final int previewSize;
 
 		if (showAllRadioButton.isSelected())
 			previewSize = -1;
 		else
 			previewSize = Integer.parseInt(counterSpinner.getValue().toString());
 		
 		/*
 		 * Load data from the given URL.
 		 */
 		final String commentChar = commentLineTextField.getText();
 		final int startLine = Integer.parseInt(startRowSpinner.getValue().toString());
 		previewPanel.setPreviewTable(sourceURL, delimiters, null, previewSize, commentChar,
 		                             startLine - 1);
 
 		if (previewPanel.getPreviewTable() == null)
 			return;
 
 		// Initialize import flags.
 		final int colSize = previewPanel.getPreviewTable().getColumnCount();
 		importFlag = new boolean[colSize];
 
 		for (int i = 0; i < colSize; i++) {
 			importFlag[i] = true;
 		}
 
 		listDataTypes = previewPanel.getCurrentListDataTypes();
 
 		/*
 		 * Initialize all Alias Tables
 		 */
 		if (dialogType == NETWORK_IMPORT) {
 			final String[] columnNames = new String[previewPanel.getPreviewTable().getColumnCount()];
 
 			for (int i = 0; i < columnNames.length; i++) {
 				columnNames[i] = previewPanel.getPreviewTable().getColumnName(i);
 			}
 
 			networkImportPanel.setComboBoxes(columnNames);
 
 			if (sourceURL.toString().endsWith(SupportedFileType.EXCEL.getExtension()) || sourceURL.toString().endsWith(SupportedFileType.OOXML.getExtension())) {
 				switchDelimiterCheckBoxes(false);
 			} else {
 				switchDelimiterCheckBoxes(true);
 			}
 
 			AttributePreviewTableCellRenderer rend = (AttributePreviewTableCellRenderer) previewPanel.getPreviewTable()
 			                                                                                         .getCellRenderer(0,
 			                                                                                                          0);
 			rend.setSourceIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
 			rend.setTargetIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
 			rend.setInteractionIndex(AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST);
 		} else {
 			for (int i = 0; i < previewPanel.getTableCount(); i++) {
 				final int columnCount = previewPanel.getPreviewTable(i).getColumnCount();
 
 				aliasTableModelMap.put(previewPanel.getSheetName(i),
 				                       new AliasTableModel(keyTable, columnCount));
 
 				if (previewPanel.getFileType() == FileTypes.GENE_ASSOCIATION_FILE) {
 					TableModel previewModel = previewPanel.getPreviewTable(i).getModel();
 					String[] columnNames = new String[previewModel.getColumnCount()];
 
 					for (int j = 0; j < columnNames.length; j++) {
 						columnNames[j] = previewModel.getColumnName(j);
 					}
 
 					initializeAliasTable(columnCount, columnNames, i);
 
 					AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSheetName(i));
 					curModel.setValueAt(true, DB_OBJECT_SYNONYM.getPosition(), 0);
 					disableComponentsForGA();
 				} else {
 					initializeAliasTable(columnCount, null, i);
 				}
 
 				updatePrimaryKeyComboBox();
 			}
 
 			setOntologyInAnnotationComboBox();
 
 			/*
 			 * If this is not an Excel file, enable delimiter checkboxes.
 			 */
 			FileTypes type = checkFileType(sourceURL);
 
 			if (type == FileTypes.GENE_ASSOCIATION_FILE) {
 				primaryKeyComboBox.setSelectedIndex(DB_OBJECT_SYMBOL.getPosition());
 				ontologyInAnnotationComboBox.setSelectedIndex(GO_ID.getPosition());
 				disableComponentsForGA();
 			} else if (sourceURL.toString().endsWith(SupportedFileType.EXCEL.getExtension()) == false || sourceURL.toString().endsWith(SupportedFileType.OOXML.getExtension()) == false) {
 				switchDelimiterCheckBoxes(true);
 				nodeRadioButton.setEnabled(true);
 				edgeRadioButton.setEnabled(true);
 				networkRadioButton.setEnabled(true);
 				importAllCheckBox.setEnabled(true);
 			} else {
 				importAllCheckBox.setEnabled(true);
 			}
 
 			attributeRadioButtonActionPerformed(null);
 			/*
 			 * Set Status bar
 			 */
 			setStatusBar(sourceURL);
 		}
 
 		pack();
 		repaint();
 
 		reloadButton.setEnabled(true);
 		startRowSpinner.setEnabled(true);
 		startRowLabel.setEnabled(true);
 	}
 
 	private void disableComponentsForGA() {
 		primaryKeyComboBox.setEnabled(true);
 		aliasTableMap.get(previewPanel.getSelectedSheetName()).setEnabled(true);
 		ontologyInAnnotationComboBox.setEnabled(false);
 
 		nodeRadioButton.setSelected(true);
 		nodeRadioButton.setEnabled(false);
 		edgeRadioButton.setEnabled(false);
 		networkRadioButton.setEnabled(false);
 
 		tabCheckBox.setEnabled(false);
 		tabCheckBox.setSelected(true);
 		commaCheckBox.setEnabled(false);
 		commaCheckBox.setSelected(false);
 		spaceCheckBox.setEnabled(false);
 		spaceCheckBox.setSelected(false);
 		semicolonCheckBox.setEnabled(false);
 		semicolonCheckBox.setSelected(false);
 		otherCheckBox.setEnabled(false);
 		otherCheckBox.setSelected(false);
 		otherDelimiterTextField.setEnabled(false);
 
 		importAllCheckBox.setEnabled(true);
 	}
 
 	private void switchDelimiterCheckBoxes(Boolean state) {
 		tabCheckBox.setEnabled(state);
 		commaCheckBox.setEnabled(state);
 		spaceCheckBox.setEnabled(state);
 		semicolonCheckBox.setEnabled(state);
 		otherCheckBox.setEnabled(state);
 		otherDelimiterTextField.setEnabled(state);
 	}
 
 	private FileTypes checkFileType(URL source) {
 		String[] parts = source.toString().split("/");
 		final String fileName = parts[parts.length - 1];
 
 		if (fileName.startsWith("gene_association")
 		    && (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT)) {
 			return FileTypes.GENE_ASSOCIATION_FILE;
 		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			return FileTypes.CUSTOM_ANNOTATION_FILE;
 		} else if (dialogType == NETWORK_IMPORT) {
 			return FileTypes.NETWORK_FILE;
 		}
 
 		return FileTypes.ATTRIBUTE_FILE;
 	}
 
 	private void setStatusBar(URL sourceURL) {
 		final String centerMessage;
 		final String rightMessage;
 
 		if (showAllRadioButton.isSelected()) {
 			centerMessage = "All entries are loaded for preview.";
 		} else {
 			centerMessage = "First " + counterSpinner.getValue().toString()
 			                + " entries are loaded for preview.";
 		}
 
 		if (sourceURL.toString().startsWith("file:")) {
 			int fileSize = 0;
 
 			try {
 				BufferedInputStream fis = (BufferedInputStream) sourceURL.openStream();
 				fileSize = fis.available();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			if ((fileSize / 1000) == 0) {
 				rightMessage = "File Size: " + fileSize + " Bytes";
 			} else {
 				rightMessage = "File Size: " + (fileSize / 1000) + " KBytes";
 			}
 		} else {
 			rightMessage = "File Size Unknown (Remote Data Source)";
 		}
 
 		setStatusBar("Key-Value Matched: "
 		             + previewPanel.checkKeyMatch(primaryKeyComboBox.getSelectedIndex()),
 		             centerMessage, rightMessage);
 	}
 
 	/**
 	 * Update the list of mapping attributes.
 	 *
 	 */
 	private void setKeyList() {
 		if (mappingAttributeComboBox.getSelectedItem() == null) {
 			return;
 		}
 
 		String selectedKeyAttribute = mappingAttributeComboBox.getSelectedItem().toString();
 
 		Iterator it;
 
 		Set<Object> valueSet = new TreeSet<Object>();
 
 		if (selectedKeyAttribute.equals(ID)) {
 			if (objType == NODE) {
 				it = Cytoscape.getRootGraph().nodesIterator();
 
 				while (it.hasNext()) {
 					Node node = (Node) it.next();
 					valueSet.add(node.getIdentifier());
 				}
 			} else if (objType == EDGE) {
 				it = Cytoscape.getRootGraph().edgesIterator();
 
 				while (it.hasNext()) {
 					valueSet.add(((Edge) it.next()).getIdentifier());
 				}
 			} else {
 				it = Cytoscape.getNetworkSet().iterator();
 
 				while (it.hasNext()) {
 					valueSet.add(((CyNetwork) it.next()).getTitle());
 				}
 			}
 		} else {
 			final byte attrType = selectedAttributes.getType(selectedKeyAttribute);
 
 			Object value = null;
 
 			Iterator attrIt = selectedAttributes.getMultiHashMap()
 			                                    .getObjectKeys(selectedKeyAttribute);
 
 			String stringValue = null;
 			Double dblValue = 0.;
 			Integer intValue = 0;
 			Boolean boolValue = false;
 			List listValue = null;
 
 			while (attrIt.hasNext()) {
 				value = attrIt.next();
 
 				switch (attrType) {
 					case CyAttributes.TYPE_STRING:
 						stringValue = selectedAttributes.getStringAttribute((String) value,
 						                                                    selectedKeyAttribute);
 						valueSet.add(stringValue);
 
 						break;
 
 					case CyAttributes.TYPE_FLOATING:
 						dblValue = selectedAttributes.getDoubleAttribute((String) value,
 						                                                 selectedKeyAttribute);
 						valueSet.add(dblValue);
 
 						break;
 
 					case CyAttributes.TYPE_INTEGER:
 						intValue = selectedAttributes.getIntegerAttribute((String) value,
 						                                                  selectedKeyAttribute);
 						valueSet.add(intValue);
 
 						break;
 
 					case CyAttributes.TYPE_BOOLEAN:
 						boolValue = selectedAttributes.getBooleanAttribute((String) value,
 						                                                   selectedKeyAttribute);
 						valueSet.add(boolValue);
 
 						break;
 
 					case CyAttributes.TYPE_SIMPLE_LIST:
 						listValue = selectedAttributes.getListAttribute((String) value,
 						                                                selectedKeyAttribute);
 						valueSet.addAll(listValue);
 
 						break;
 
 					default:
 						break;
 				}
 			}
 		}
 
 		previewPanel.setKeyAttributeList(valueSet);
 
 		// nodeKeyList.setListData(valueSet.toArray());
 	}
 
 	private void updateAliasTableCell(String name, int columnIndex) {
 		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
 		curTable.setDefaultRenderer(Object.class,
 		                            new AliasTableRenderer(attributeDataTypes,
 		                                                   primaryKeyComboBox.getSelectedIndex()));
 
 		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
 		curModel.setValueAt(name, columnIndex, 1);
 		curTable.setModel(curModel);
 		curTable.repaint();
 		aliasScrollPane.repaint();
 		repaint();
 	}
 
 	private void updateAliasTable() {
 		if (dialogType == NETWORK_IMPORT) {
 			return;
 		}
 
 		JTable curTable = aliasTableMap.get(previewPanel.getSelectedSheetName());
 
 		curTable.setDefaultRenderer(Object.class,
 		                            new AliasTableRenderer(attributeDataTypes,
 		                                                   primaryKeyComboBox.getSelectedIndex()));
 
 		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
 
 		Object curValue = null;
 
 		for (int i = 0; i < previewPanel.getPreviewTable().getColumnCount(); i++) {
 			curValue = previewPanel.getPreviewTable().getColumnModel().getColumn(i).getHeaderValue();
 
 			if (curValue != null) {
 				curModel.setValueAt(curValue.toString(), i, 1);
 			} else {
 				previewPanel.getPreviewTable().getColumnModel().getColumn(i).setHeaderValue("");
 				curModel.setValueAt("", i, 1);
 			}
 		}
 
 		curTable.setModel(curModel);
 		aliasScrollPane.setViewportView(curTable);
 		aliasScrollPane.repaint();
 	}
 
 	private void initializeAliasTable(int rowCount, String[] columnNames) {
 		initializeAliasTable(rowCount, columnNames, -1);
 	}
 
 	private void initializeAliasTable(int rowCount, String[] columnNames, int sheetIndex) {
 		Object[][] keyTableData = new Object[rowCount][keyTable.length];
 
 		AliasTableModel curModel = null;
 		String tabName;
 
 		if (sheetIndex == -1) {
 			tabName = previewPanel.getSelectedSheetName();
 		} else {
 			tabName = previewPanel.getSheetName(sheetIndex);
 		}
 
 		curModel = aliasTableModelMap.get(tabName);
 
 		curModel = new AliasTableModel();
 
 		Byte[] dataTypeArray = previewPanel.getDataTypes(tabName);
 
 		for (int i = 0; i < rowCount; i++) {
 			keyTableData[i][0] = new Boolean(false);
 
 			if (columnNames == null) {
 				keyTableData[i][1] = "Column " + (i + 1);
 			} else {
 				keyTableData[i][1] = columnNames[i];
 			}
 
 			if (dataTypeArray.length <= i) {
 				attributeDataTypes.add(CyAttributes.TYPE_STRING);
 			} else {
 				attributeDataTypes.add(dataTypeArray[i]);
 			}
 
 			keyTableData[i][2] = "String";
 		}
 
 		curModel = new AliasTableModel(keyTableData, keyTable);
 
 		aliasTableModelMap.put(tabName, curModel);
 
 		curModel.addTableModelListener(this);
 		/*
 		 * Set the list and combo box
 		 */
 		mappingAttributeComboBox.setEnabled(true);
 
 		JTable curTable = new JTable();
 		curTable.setModel(curModel);
 		aliasTableMap.put(tabName, curTable);
 
 		curTable.setDefaultRenderer(Object.class,
 		                            new AliasTableRenderer(attributeDataTypes,
 		                                                   primaryKeyComboBox.getSelectedIndex()));
 		curTable.setEnabled(true);
 		curTable.setSelectionBackground(Color.white);
 		curTable.getTableHeader().setReorderingAllowed(false);
 
 		curTable.getColumnModel().getColumn(0).setPreferredWidth(55);
 		curTable.getColumnModel().getColumn(1).setPreferredWidth(300);
 		curTable.getColumnModel().getColumn(2).setPreferredWidth(100);
 
 		aliasScrollPane.setViewportView(curTable);
 		repaint();
 	}
 
 	private void updateMappingAttributeComboBox() {
 		mappingAttributeComboBox.removeAllItems();
 
 		final ListCellRenderer lcr = mappingAttributeComboBox.getRenderer();
 		mappingAttributeComboBox.setRenderer(new ListCellRenderer() {
 				public Component getListCellRendererComponent(JList list, Object value, int index,
 				                                              boolean isSelected,
 				                                              boolean cellHasFocus) {
 					JLabel cmp = (JLabel) lcr.getListCellRendererComponent(list, value, index,
 					                                                       isSelected, cellHasFocus);
 
 					if (value.equals(ID)) {
 						cmp.setIcon(ID_ICON.getIcon());
 					} else {
 						cmp.setIcon(getDataTypeIcon(selectedAttributes.getType(value.toString())));
 					}
 
 					return cmp;
 				}
 			});
 
 		mappingAttributeComboBox.addItem(ID);
 
 		for (String name : selectedAttributes.getAttributeNames()) {
 			if ((selectedAttributes.getType(name) != CyAttributes.TYPE_COMPLEX)
 			    && (selectedAttributes.getType(name) != CyAttributes.TYPE_SIMPLE_MAP)
 			    && (selectedAttributes.getType(name) != CyAttributes.TYPE_UNDEFINED)) {
 				mappingAttributeComboBox.addItem(name);
 			}
 		}
 	}
 
 	private TableCellRenderer getRenderer(FileTypes type) {
 		final TableCellRenderer rend;
 
 		if (type == FileTypes.GENE_ASSOCIATION_FILE) {
 			keyInFile = this.primaryKeyComboBox.getSelectedIndex();
 
 			int ontologyCol = this.ontologyInAnnotationComboBox.getSelectedIndex();
 			List<Integer> gaAlias = new ArrayList<Integer>();
 
 			AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
 
 			for (int i = 0; i < curModel.getColumnCount(); i++) {
 				if ((Boolean) curModel.getValueAt(i, 0)) {
 					gaAlias.add(i);
 				}
 			}
 
 			gaAlias.add(DB_OBJECT_SYNONYM.getPosition());
 
 			rend = new AttributePreviewTableCellRenderer(keyInFile, gaAlias, ontologyCol,
 			                                             TAXON.getPosition(), importFlag,
 			                                             listDelimiter);
 		} else {
 			rend = new AttributePreviewTableCellRenderer(keyInFile, new ArrayList<Integer>(),
 			                                             AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
 			                                             AttributePreviewTableCellRenderer.PARAMETER_NOT_EXIST,
 			                                             importFlag, listDelimiter);
 		}
 
 		return rend;
 	}
 
 	/**
 	 * Create task for annotation reader and run it. tablechanged
 	 *
 	 * @param reader
 	 * @param ontology
 	 * @param source
 	 */
 	private void loadAnnotation(TextTableReader reader, String source) {
 		// Create LoadNetwork Task
 		ImportAttributeTableTask task = new ImportAttributeTableTask(reader, source);
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 
 	/**
 	 * Create task for ontology reader and run the task.<br>
 	 *
 	 * @param dataSource
 	 * @param ontologyName
 	 */
 	private void loadOntology(String dataSource, String ontologyName) {
 		// Create LoadNetwork Task
 		ImportOntologyTask task = new ImportOntologyTask(dataSource,
 		                                                 ontologyTypeMap.get(ontologyName),
 		                                                 ontologyName,
 		                                                 ontologyDescriptionMap.get(ontologyName));
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(true);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 
 	/**
 	 * Create task for annotation reader and run it.
 	 *
 	 * @param reader
 	 * @param ontology
 	 * @param source
 	 */
 	private void loadGeneAssociation(TextTableReader reader, String ontology, String source) {
 		// Create LoadNetwork Task
 		ImportOntologyAnnotationTask task = new ImportOntologyAnnotationTask(reader, ontology,
 		                                                                     source);
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayStatus(true);
 		jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 
 	private void loadNetwork(final String networkName, final GraphReader reader, final URL source,
 	                         boolean multi) {
 		// Create LoadNetwork Task
 		ImportNetworkTask task = new ImportNetworkTask(reader, source);
 
 		// Configure JTask Dialog Pop-Up Box
 		JTaskConfig jTaskConfig = new JTaskConfig();
 		jTaskConfig.setOwner(Cytoscape.getDesktop());
 		jTaskConfig.displayCloseButton(true);
 		jTaskConfig.displayStatus(true);
 
 		if (multi)
 			jTaskConfig.setAutoDispose(true);
 		else
 			jTaskConfig.setAutoDispose(false);
 
 		// Execute Task in New Thread; pops open JTask Dialog Box.
 		TaskManager.executeTask(task, jTaskConfig);
 	}
 
 	private void setStatusBar(String message1, String message2, String message3) {
 		statusBar.setLeftLabel(message1);
 		statusBar.setCenterLabel(message2);
 		statusBar.setRightLabel(message3);
 	}
 
 	/**
 	 * Alias table changed.
 	 */
 	public void tableChanged(TableModelEvent evt) {
 		final int row = evt.getFirstRow();
 		final int col = evt.getColumn();
 		AliasTableModel curModel = aliasTableModelMap.get(previewPanel.getSelectedSheetName());
 
 		if (col == 0) {
 			previewPanel.setAliasColumn(row, (Boolean) curModel.getValueAt(row, col));
 		}
 
 		aliasScrollPane.repaint();
 	}
 
 	private List<String> checkDelimiter() {
 		final List<String> delList = new ArrayList<String>();
 
 		if (tabCheckBox.isSelected()) {
 			delList.add(TextFileDelimiters.TAB.toString());
 		}
 
 		if (commaCheckBox.isSelected()) {
 			delList.add(TextFileDelimiters.COMMA.toString());
 		}
 
 		if (spaceCheckBox.isSelected()) {
 			delList.add(TextFileDelimiters.SPACE.toString());
 		}
 
 		if (semicolonCheckBox.isSelected()) {
 			delList.add(TextFileDelimiters.SEMICOLON.toString());
 		}
 
 		if (otherCheckBox.isSelected()) {
 			delList.add(otherDelimiterTextField.getText());
 		}
 
 		return delList;
 	}
 
 	/**
 	 * Error checker for imput table.<br>
 	 *
 	 * @return true if table looks OK.
 	 */
 	private boolean checkDataSourceError() {
 		/*
 		 * Number of ENABLED columns should be 2 or more.
 		 *
 		 */
 		final JTable table = previewPanel.getPreviewTable();
 
 		if ((table == null) || (table.getModel() == null) || (table.getColumnCount() == 0)) {
 			JOptionPane.showMessageDialog(this, "No table selected.", "Invalid Table!",
 			                              JOptionPane.INFORMATION_MESSAGE);
 
 			return false;
 		} else if ((table.getColumnCount() < 2) && (dialogType != NETWORK_IMPORT)) {
 			JOptionPane.showMessageDialog(this, "Table should contain at least 2 columns.",
 			                              "Invalid Table!", JOptionPane.INFORMATION_MESSAGE);
 
 			return false;
 		}
 
 		if (dialogType == NETWORK_IMPORT) {
 			final int sIdx = networkImportPanel.getSourceIndex();
 			final int tIdx = networkImportPanel.getTargetIndex();
 			final int iIdx = networkImportPanel.getInteractionIndex();
 
 			//			if ((sIdx == -1) || (tIdx == -1)) {
 			//				JOptionPane.showMessageDialog(this, "Source or Target index not selected.",
 			//				                              "Please select both source & target index.",
 			//				                              JOptionPane.INFORMATION_MESSAGE);
 			//
 			//				return false;
 			//			}
 			if ((sIdx == tIdx) || (((iIdx == sIdx) || (iIdx == tIdx)) && (iIdx != -1))) {
 				JOptionPane.showMessageDialog(this,
 				                              "Columns for source, target, and interaction type must be distinct.",
 				                              "Same column index!", JOptionPane.INFORMATION_MESSAGE);
 
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/*
 	 * Layout Information for the entire dialog.<br>
 	 *
 	 * <p> This layout will be switched by dialog type parameter. </p>
 	 *
 	 */
 	private void globalLayout() {
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 
 		/*
 		 * Case 1: Simple Attribute Import
 		 */
 		if (dialogType == SIMPLE_ATTRIBUTE_IMPORT) {
 			layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                .add(layout.createSequentialGroup().addContainerGap()
 			                                           .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(statusBar,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      Short.MAX_VALUE)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(importButton)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(cancelButton))
 			                                                      .add(previewPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           advancedPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(basicPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(titleIconLabel1)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel2)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel3)
 			                                                                 .add(20, 20, 20)
 			                                                                 .add(titleLabel)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 			                                                                                  350,
 			                                                                                  Short.MAX_VALUE)
 			                                                                 .add(helpButton,
 			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 			                                                      .add(titleSeparator,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           700, Short.MAX_VALUE))
 			                                           .addContainerGap()));
 			layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                              .add(layout.createSequentialGroup().addContainerGap()
 			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                                          .add(helpButton,
 			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                                               20,
 			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                                                          .add(titleIconLabel1)
 			                                                                          .add(titleIconLabel2)
 			                                                                          .add(titleIconLabel3))
 			                                                               .add(2, 2, 2))
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(titleLabel)
 			                                                               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
 			                                         .add(titleSeparator,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              10,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(basicPanel,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(advancedPanel,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(previewPanel,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              Short.MAX_VALUE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING,
 			                                                                         false)
 			                                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                               .add(cancelButton)
 			                                                               .add(importButton))
 			                                                    .add(statusBar,
 			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 			                                         .addContainerGap()));
 			pack();
 		} else if (dialogType == ONTOLOGY_AND_ANNOTATION_IMPORT) {
 			layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                .add(layout.createSequentialGroup().addContainerGap()
 			                                           .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(statusBar,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      Short.MAX_VALUE)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(importButton)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(cancelButton))
 			                                                      .add(previewPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           advancedPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(basicPanel,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(org.jdesktop.layout.GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(titleIconLabel1)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel2)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel3)
 			                                                                 .add(20, 20, 20)
 			                                                                 .add(titleLabel)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 			                                                                                  250,
 			                                                                                  Short.MAX_VALUE)
 			                                                                 .add(helpButton,
 			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                                      org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 			                                                      .add(titleSeparator,
 			                                                           org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                           300, Short.MAX_VALUE))
 			                                           .addContainerGap()));
 			layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
 			                              .add(layout.createSequentialGroup().addContainerGap()
 			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                                          .add(helpButton,
 			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                                               20,
 			                                                                               org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                                                          .add(titleIconLabel1)
 			                                                                          .add(titleIconLabel2)
 			                                                                          .add(titleIconLabel3))
 			                                                               .add(2, 2, 2))
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(titleLabel)
 			                                                               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
 			                                         .add(titleSeparator,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              10,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(basicPanel,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(advancedPanel,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(previewPanel,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                              Short.MAX_VALUE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING,
 			                                                                         false)
 			                                                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
 			                                                               .add(cancelButton)
 			                                                               .add(importButton))
 			                                                    .add(statusBar,
 			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE,
 			                                                         org.jdesktop.layout.GroupLayout.DEFAULT_SIZE,
 			                                                         org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
 			                                         .addContainerGap()));
 
 			annotationAndOntologyImportPanel.setVisible(true);
 			attrTypePanel.setVisible(true);
 		} else if (dialogType == NETWORK_IMPORT) {
 			/*
 			 * Layout for network import.
 			 */
 			layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.LEADING)
 			                                .add(layout.createSequentialGroup().addContainerGap()
 			                                           .add(layout.createParallelGroup(GroupLayout.LEADING)
 			                                                      .add(GroupLayout.TRAILING,
 			                                                           previewPanel,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(GroupLayout.TRAILING,
 			                                                           advancedPanel,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(GroupLayout.TRAILING,
 			                                                           networkImportPanel,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(basicPanel,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           Short.MAX_VALUE)
 			                                                      .add(GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(titleIconLabel1)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel2)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(titleIconLabel3)
 			                                                                 .add(20, 20, 20)
 			                                                                 .add(titleLabel)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED,
 			                                                                                  350,
 			                                                                                  Short.MAX_VALUE)
 			                                                                 .add(helpButton,
 			                                                                      GroupLayout.PREFERRED_SIZE,
 			                                                                      GroupLayout.DEFAULT_SIZE,
 			                                                                      GroupLayout.PREFERRED_SIZE))
 			                                                      .add(titleSeparator,
 			                                                           GroupLayout.DEFAULT_SIZE,
 			                                                           700, Short.MAX_VALUE)
 			                                                      .add(GroupLayout.TRAILING,
 			                                                           layout.createSequentialGroup()
 			                                                                 .add(importButton)
 			                                                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                                                 .add(cancelButton)))
 			                                           .addContainerGap()));
 			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.LEADING)
 			                              .add(layout.createSequentialGroup().addContainerGap()
 			                                         .add(layout.createParallelGroup(GroupLayout.TRAILING)
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(layout.createParallelGroup(GroupLayout.BASELINE)
 			                                                                          .add(helpButton,
 			                                                                               GroupLayout.PREFERRED_SIZE,
 			                                                                               20,
 			                                                                               GroupLayout.PREFERRED_SIZE)
 			                                                                          .add(titleIconLabel1)
 			                                                                          .add(titleIconLabel2)
 			                                                                          .add(titleIconLabel3))
 			                                                               .add(2, 2, 2))
 			                                                    .add(layout.createSequentialGroup()
 			                                                               .add(titleLabel)
 			                                                               .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)))
 			                                         .add(titleSeparator,
 			                                              GroupLayout.PREFERRED_SIZE, 10,
 			                                              GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(basicPanel, GroupLayout.PREFERRED_SIZE,
 			                                              GroupLayout.DEFAULT_SIZE,
 			                                              GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(networkImportPanel,
 			                                              GroupLayout.PREFERRED_SIZE,
 			                                              GroupLayout.DEFAULT_SIZE,
 			                                              GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(advancedPanel,
 			                                              GroupLayout.PREFERRED_SIZE,
 			                                              GroupLayout.DEFAULT_SIZE,
 			                                              GroupLayout.PREFERRED_SIZE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(previewPanel, GroupLayout.DEFAULT_SIZE,
 			                                              GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 			                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
 			                                         .add(layout.createParallelGroup(GroupLayout.BASELINE)
 			                                                    .add(cancelButton).add(importButton))
 			                                         .addContainerGap()));
 
 			// annotationAndOntologyImportPanel.setVisible(false);
 			networkImportPanel.setVisible(true);
 			attrTypePanel.setVisible(false);
 			advancedOptionCheckBox.setVisible(false);
 
 			// advancedOptionPanel.setVisible(false);
 		}
 
 		pack();
 	}
 
 	// Variables declaration - do not modify
 	private javax.swing.JCheckBox advancedOptionCheckBox;
 	private javax.swing.JCheckBox caseSensitiveCheckBox;
 	private javax.swing.JPanel advancedPanel;
 
 	// private JTable aliasTable;
 	private javax.swing.JScrollPane aliasScrollPane;
 	private javax.swing.JPanel annotationAndOntologyImportPanel;
 	private javax.swing.JButton arrowButton1;
 	private javax.swing.JButton arrowButton2;
 	private javax.swing.JPanel attr2annotationPanel;
 	private javax.swing.JCheckBox transferNameCheckBox;
 	private javax.swing.ButtonGroup attrTypeButtonGroup;
 	private javax.swing.JLabel attribuiteLabel;
 	private javax.swing.JLabel attributeFileLabel;
 	private javax.swing.JPanel basicPanel;
 	private javax.swing.JButton browseAnnotationButton;
 	private javax.swing.JButton browseOntologyButton;
 	private javax.swing.JButton cancelButton;
 	private javax.swing.JCheckBox commaCheckBox;
 	private javax.swing.JPanel delimiterPanel;
 	private javax.swing.JRadioButton edgeRadioButton;
 	private javax.swing.JButton helpButton;
 	private javax.swing.JButton importButton;
 	private javax.swing.JRadioButton networkRadioButton;
 	private javax.swing.JComboBox mappingAttributeComboBox;
 	private javax.swing.JLabel nodeKeyLabel;
 	private javax.swing.JRadioButton nodeRadioButton;
 	private javax.swing.JPanel ontology2annotationPanel;
 	private javax.swing.JComboBox ontologyComboBox;
 	private javax.swing.JComboBox ontologyInAnnotationComboBox;
 	private javax.swing.JLabel ontologyInAnnotationLabel;
 	private javax.swing.JLabel ontologyLabel;
 	private javax.swing.JTextField otherDelimiterTextField;
 	private javax.swing.JCheckBox otherCheckBox;
 	private PreviewTablePanel previewPanel;
 	private javax.swing.JLabel primaryKeyLabel;
 	private javax.swing.JButton selectAttributeFileButton;
 	private javax.swing.JCheckBox semicolonCheckBox;
 	private javax.swing.JPanel simpleAttributeImportPanel;
 	private javax.swing.JComboBox annotationComboBox;
 	private javax.swing.JLabel sourceLabel;
 	private javax.swing.JCheckBox spaceCheckBox;
 	private javax.swing.JCheckBox tabCheckBox;
 	private javax.swing.JTextField targetDataSourceTextField;
 	private javax.swing.JLabel targetOntologyLabel;
 	private javax.swing.JTextField ontologyTextField;
 	private javax.swing.JCheckBox textImportCheckBox;
 	private javax.swing.JPanel textImportOptionPanel;
 	private javax.swing.JLabel titleIconLabel1;
 	private javax.swing.JLabel titleIconLabel2;
 	private javax.swing.JLabel titleIconLabel3;
 	private javax.swing.JLabel titleLabel;
 	private javax.swing.JSeparator titleSeparator;
 	private JComboBox primaryKeyComboBox;
 	private JLabel primaryLabel;
 	private JPanel attrTypePanel;
 	private javax.swing.JRadioButton showAllRadioButton;
 	private javax.swing.JLabel counterLabel;
 	private javax.swing.JRadioButton counterRadioButton;
 	private javax.swing.JButton reloadButton;
 	private javax.swing.JSpinner counterSpinner;
 	private javax.swing.ButtonGroup importTypeButtonGroup;
 	private JPanel attributeNamePanel;
 	private JPanel previewOptionPanel;
 	private JPanel networkImportOptionPanel;
 	private JLabel defaultInteractionLabel;
 	private JTextField defaultInteractionTextField;
 	private JLabel startRowLabel;
 	private JSpinner startRowSpinner;
 	private JLabel commentLineLabel;
 	private JTextField commentLineTextField;
 
 	// End of variables declaration
 	JStatusBar statusBar;
 	private NetworkImportOptionsPanel networkImportPanel;
 
 	// private DefaultTableModel model;
 	private AliasTableModel aliasTableModel;
 	private JTable aliasTable;
 	private JCheckBox importAllCheckBox;
 }
 
 
 class ComboBoxRenderer extends JLabel implements ListCellRenderer {
 	private List<Byte> attributeDataTypes;
 
 	/**
 	 * Creates a new ComboBoxRenderer object.
 	 *
 	 * @param attributeDataTypes  DOCUMENT ME!
 	 */
 	public ComboBoxRenderer(List<Byte> attributeDataTypes) {
 		this.attributeDataTypes = attributeDataTypes;
 		setOpaque(true);
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param list DOCUMENT ME!
 	 * @param value DOCUMENT ME!
 	 * @param index DOCUMENT ME!
 	 * @param isSelected DOCUMENT ME!
 	 * @param cellHasFocus DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Component getListCellRendererComponent(JList list, Object value, int index,
 	                                              boolean isSelected, boolean cellHasFocus) {
 		setText(value.toString());
 		if (isSelected) {
 			setBackground(list.getSelectionBackground());
 			setForeground(list.getSelectionForeground());
 		} else {
 			setBackground(list.getBackground());
 			setForeground(list.getForeground());
 		}
 
 		if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
 		    && (index < attributeDataTypes.size()) && (index >= 0)) {
 			final Byte dataType = attributeDataTypes.get(index);
 
 			if (dataType == null) {
 				setIcon(null);
 			} else {
 				setIcon(ImportTextTableDialog.getDataTypeIcon(dataType));
 			}
 		} else if ((attributeDataTypes != null) && (attributeDataTypes.size() != 0)
 		           && (index < attributeDataTypes.size())) {
 			setIcon(ImportTextTableDialog.getDataTypeIcon(attributeDataTypes.get(list.getSelectedIndex())));
 		}
 
 		return this;
 	}
 }
 
 
 class AliasTableModel extends DefaultTableModel {
 	AliasTableModel(String[] columnNames, int rowNum) {
 		super(columnNames, rowNum);
 	}
 
 	AliasTableModel(Object[][] data, Object[] colNames) {
 		super(data, colNames);
 	}
 
 	AliasTableModel() {
 		super();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param col DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public Class getColumnClass(int col) {
 		return getValueAt(0, col).getClass();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param row DOCUMENT ME!
 	 * @param column DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean isCellEditable(int row, int column) {
 		if (column == 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 }
