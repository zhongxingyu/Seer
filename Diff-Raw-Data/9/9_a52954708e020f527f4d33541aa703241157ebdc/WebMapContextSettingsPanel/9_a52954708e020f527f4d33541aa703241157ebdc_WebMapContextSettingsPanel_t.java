 /* gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
 
 /* CVS MESSAGES:
 *
 * $Id: WebMapContextSettingsPanel.java 24986 2008-11-12 12:02:30Z jcampos $
 * $Log$
 * Revision 1.20  2007-09-19 16:15:41  jaume
 * removed unnecessary imports
 *
 * Revision 1.19  2007/03/06 17:06:43  caballero
 * Exceptions
 *
 * Revision 1.18  2006/09/20 10:09:37  jaume
 * *** empty log message ***
 *
 * Revision 1.17  2006/09/20 07:45:21  caballero
 * constante registerName
 *
 * Revision 1.16  2006/09/15 10:44:24  caballero
 * extensibilidad de documentos
 *
 * Revision 1.15  2006/08/30 10:20:51  jaume
 * *** empty log message ***
 *
 * Revision 1.14  2006/08/29 07:56:15  cesar
 * Rename the *View* family of classes to *Window* (ie: SingletonView to SingletonWindow, ViewInfo to WindowInfo, etc)
 *
 * Revision 1.13  2006/08/29 07:21:03  cesar
 * Rename com.iver.cit.gvsig.fmap.Fmap class to com.iver.cit.gvsig.fmap.MapContext
 *
 * Revision 1.12  2006/08/23 09:42:32  jaume
 * *** empty log message ***
 *
 * Revision 1.11  2006/08/23 09:40:03  jaume
 * *** empty log message ***
 *
 * Revision 1.10  2006/07/21 11:51:13  jaume
 * improved appearance in wms panel and a wmc bug fixed
 *
 * Revision 1.9  2006/06/30 08:02:57  jaume
 * added a normative-sized button
 *
 * Revision 1.8  2006/05/25 10:28:12  jaume
 * *** empty log message ***
 *
 * Revision 1.7  2006/05/25 07:42:55  jaume
 * *** empty log message ***
 *
 * Revision 1.6  2006/05/12 07:47:39  jaume
 * removed unnecessary imports
 *
 * Revision 1.5  2006/05/03 07:51:21  jaume
 * *** empty log message ***
 *
 * Revision 1.4  2006/04/25 11:40:55  jaume
 * *** empty log message ***
 *
 * Revision 1.3  2006/04/21 10:27:32  jaume
 * exporting now supported
 *
 * Revision 1.2  2006/04/20 17:11:54  jaume
 * Attempting to export
 *
 * Revision 1.1  2006/04/19 07:57:29  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2006/04/07 12:10:37  jaume
 * *** empty log message ***
 *
 *
 */
 package com.iver.cit.gvsig.gui.panels;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.filechooser.FileFilter;
 
 import org.gvsig.gui.beans.swing.JButton;
 import org.gvsig.gui.beans.swing.JFileChooser;
 
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.ui.mdiManager.SingletonWindow;
 import com.iver.andami.ui.mdiManager.WindowInfo;
 import com.iver.cit.gvsig.fmap.MapContext;
 import com.iver.cit.gvsig.project.documents.view.ProjectView;
 import com.iver.cit.gvsig.project.documents.view.ProjectViewFactory;
 import com.iver.cit.gvsig.wmc.ExportWebMapContextExtension;
 import com.iver.cit.gvsig.wmc.ImportWebMapContextExtension;
 import com.iver.cit.gvsig.wmc.WebMapContext;
 import com.iver.cit.gvsig.wmc.WebMapContextTags;
 /**
  * A graphical form to fill up the customizable information of a destinantion
  * Web Map Context file.
  *
  * @author jaume dominguez faus - jaume.dominguez@iver.es
  *
  */
 public class WebMapContextSettingsPanel extends JPanel implements SingletonWindow {
 	public static Preferences fPrefs = Preferences.userRoot().node( "gvsig.mapcontext-settingspanel" );
 	private JPanel simplePanel = null;
 	private JPanel advancedPanel = null;
 	private JPanel buttonsPanel = null;
 	private JComboBox cmbVersion = null;
 	private JLabel lblTitle = null;
 	private JButton btnBrowseFileSystem = null;
 	private JTextField txtTitle = null;
 	private JLabel lblVersion = null;
 	private JLabel lblId = null;
 	private JTextField txtId = null;
 	private JLabel lblFile = null;
 	private JTextField txtFile = null;
 	private JButton btnAdvanced = null;
 	private JButton btnOk = null;
 	private JButton btnCancel = null;
 	private JLabel lblAbstract = null;
 	private JLabel lblLogoURL = null;
 	private JLabel lblDescriptionURL = null;
 	private JTextArea txtAbstract = null;
 	private JScrollPane scrlAbstract = null;
 	private JTextField txtLogoURL = null;
 	private JTextField txtDescriptionURL = null;
 	private JPanel pnlMapSize = null;
 	private JRadioButton rdBtnUseViewSize = null;
 	private JRadioButton rdBtnCustomSize = null;
 	private JLabel lblWidth = null;
 	private JLabel lblHeight = null;
 	private JTextField txtWidth = null;
 	private JTextField txtHeight = null;
 	private JPanel pnlContactInfo = null;
 	private JTextField txtKeyWords = null;
 	private JLabel lblKeyWords = null;
 	private JLabel lblContactPerson = null;
 	private JTextField txtContactPerson = null;
 	private JLabel lblContactOrganization = null;
 	private JTextField txtOrganization = null;
 	private JLabel lblContactPosition = null;
 	private JTextField txtContactPosition = null;
 	private JLabel lblAddress = null;
 	private JTextField txtAddress = null;
 	private JLabel lblCity = null;
 	private JTextField txtCity = null;
 	private JLabel lblStateProvince = null;
 	private JTextField txtStateOrProvince = null;
 	private JLabel lblPostCode = null;
 	private JTextField txtPostCode = null;
 	private JLabel lblCountry = null;
 	private JComboBox cmbCountries = null;
 	private JLabel lblPhone = null;
 	private JTextField txtTelephone = null;
 	private JLabel lblFax = null;
 	private JTextField txtFax = null;
 	private JLabel lblEMail = null;
 	private JTextField txtEMail = null;
 	private boolean first = true;
 	private boolean advanced = fPrefs.getBoolean("advanced-panel", false);
 	private WindowInfo m_viewInfo;
 	private String strAdvanced = PluginServices.getText(this, "advanced");
 	private File targetFile;
 	private WebMapContext wmc;
 	private ProjectView[] exportableViews;
 	private JLabel lblView = null;
 	private JComboBox cmbViews = null;
 	private String lastWidth, lastHeight;
 	private int defaultWidth = 500;
 	private int defaultHeight = 450;
 	private JLabel lblExtent = null;
 	private JRadioButton rdBtnCurrentViewExtent = null;
 	private JRadioButton rdBtnFullExtent = null;
 	private static String lastPath = null;
 
 	protected boolean useFullExtent = fPrefs.getBoolean("use_full_extent", false);
 
 	public WebMapContextSettingsPanel(ProjectView[] views) {
 		super();
 		exportableViews = views;
 		initialize();
 	}
 
 	/**
 	 * This method initializes this
 	 *
 	 * @return void
 	 */
 	private void initialize() {
 		this.setLayout(null);
 
 		switchPanels();
 	}
 
 	private void switchPanels() {
 		this.removeAll();
 		Dimension sz;
 		this.add(getSimplePanel(), java.awt.BorderLayout.NORTH);
 		if (advanced) {
 			this.add(getAdvancedPanel(), java.awt.BorderLayout.CENTER);
 			sz = new Dimension(535, 640);
 			btnAdvanced.setText(strAdvanced+"  <<");
 
 		} else {
 			sz = new Dimension(535, 260);
 			btnAdvanced.setText(strAdvanced+"  >>");
 
 		}
 
 		this.setSize(sz);
 
 
 		this.add(getButtonsPanel(), java.awt.BorderLayout.SOUTH);
 		fPrefs.putBoolean("advanced-panel", advanced);
 		if (!first) {
 			PluginServices.getMDIManager().changeWindowInfo(this, getWindowInfo());
 
 			PluginServices.getMDIManager().closeWindow(this);
 			PluginServices.getMDIManager().addWindow(this);
 		}
 		first = false;
 	}
 
 	public WindowInfo getWindowInfo() {
 
 		m_viewInfo = new WindowInfo(WindowInfo.ICONIFIABLE);
 		m_viewInfo.setTitle(PluginServices.getText(this, "web_map_context_settings"));
 		m_viewInfo.setWidth(this.getWidth()+8);
 		m_viewInfo.setHeight(getHeight());
 		return m_viewInfo;
 	}
 
 	/**
 	 * This method initializes simplePanel
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getSimplePanel() {
 		if (simplePanel == null) {
 			ButtonGroup group = new ButtonGroup();
 			lblExtent = new JLabel();
 			lblExtent.setBounds(9, 146, 79, 20);
 			lblExtent.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblExtent.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblExtent.setText(PluginServices.getText(this, "map_extent")+":");
 			lblView = new JLabel();
 			lblView.setBounds(9, 24, 79, 20);
 			lblView.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblView.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblView.setText(PluginServices.getText(this, ProjectViewFactory.registerName)+":");
 			lblTitle = new JLabel();
 			lblTitle.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblTitle.setBounds(9, 48, 79, 20);
 			lblTitle.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblTitle.setText(PluginServices.getText(this, "title")+":");
 			lblId = new JLabel();
 			lblId.setBounds(9, 72, 79, 20);
 			lblId.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblId.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblId.setText(PluginServices.getText(this, "id")+":");
 			lblVersion = new JLabel();
 			lblVersion.setBounds(9, 120, 79, 20);
 			lblVersion.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblVersion.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblVersion.setText(PluginServices.getText(this, "version")+":");
 			lblFile = new JLabel();
 			lblFile.setBounds(9, 96, 79, 20);
 			lblFile.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblFile.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblFile.setText(PluginServices.getText(this, "file_name")+":");
 			simplePanel = new JPanel();
 			simplePanel.setLayout(null);
 			simplePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
 					  null, PluginServices.getText(this, "options"),
 					  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 					  javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
 			simplePanel.setBounds(new java.awt.Rectangle(0,0,535,196));
 			simplePanel.add(lblView, null);
 			simplePanel.add(getCmbViews(), null);
 			simplePanel.add(getTxtTitle(), null);
 			simplePanel.add(lblTitle, null);
 			simplePanel.add(getBtnBrowseFileSystem(), null);
 			simplePanel.add(lblVersion, null);
 			simplePanel.add(lblId, null);
 			simplePanel.add(getTxtId(), null);
 			simplePanel.add(lblFile, null);
 			simplePanel.add(getTxtFile(), null);
 			simplePanel.add(getBtnAdvanced(), null);
 			simplePanel.add(getCmbVersion(), null);
 			simplePanel.add(lblExtent, null);
 			simplePanel.add(getRdBtnCurrentViewExtent(), null);
 			simplePanel.add(getRdBtnFullExtent(), null);
 			group.add(getRdBtnCurrentViewExtent());
 			group.add(getRdBtnFullExtent());
 		}
 		return simplePanel;
 	}
 
 	/**
 	 * This method initializes advancedPanel
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getAdvancedPanel() {
 		if (advancedPanel == null) {
 			lblKeyWords = new JLabel();
 			lblKeyWords.setBounds(10, 79, 99, 20);
 			lblKeyWords.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblKeyWords.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblKeyWords.setText(PluginServices.getText(this, "keywords")+":");
 			lblDescriptionURL = new JLabel();
 			lblDescriptionURL.setBounds(10, 104, 99, 20);
 			lblDescriptionURL.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblDescriptionURL.setText(PluginServices.getText(this, "description_URL")+":");
 			lblDescriptionURL.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblLogoURL = new JLabel();
 			lblLogoURL.setBounds(10, 130, 99, 20);
 			lblLogoURL.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblLogoURL.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblLogoURL.setText(PluginServices.getText(this, "logo_URL")+":");
 			lblAbstract = new JLabel();
 			lblAbstract.setText(PluginServices.getText(this, "abstract")+":");
 			lblAbstract.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblAbstract.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblAbstract.setBounds(10, 27, 99, 20);
 			advancedPanel = new JPanel();
 			advancedPanel.setLayout(null);
 			advancedPanel.setBounds(0, 194, 535, 386);
 			advancedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
 					  null, PluginServices.getText(this, "advanced_settings"),
 					  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 					  javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
 			advancedPanel.add(lblAbstract, null);
 			advancedPanel.add(lblKeyWords, null);
 			advancedPanel.add(lblLogoURL, null);
 			advancedPanel.add(lblDescriptionURL, null);
 			advancedPanel.add(getScrlAbstract(), null);
 			advancedPanel.add(getTxtLogoURL(), null);
 			advancedPanel.add(getTxtDescriptionURL(), null);
 			advancedPanel.add(getPnlMapSize(), null);
 			advancedPanel.add(getPnlContactInfo(), null);
 			advancedPanel.add(getTxtKeyWords(), null);
 		}
 		return advancedPanel;
 	}
 
 	/**
 	 * This method initializes buttonsPanel
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getButtonsPanel() {
 		if (buttonsPanel == null) {
 			FlowLayout flowLayout = new FlowLayout();
 			flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
 			flowLayout.setVgap(1);
 			buttonsPanel = new JPanel();
 			buttonsPanel.setLayout(flowLayout);
 			buttonsPanel.add(getBtnOk(), null);
 			buttonsPanel.add(getBtnCancel(), null);
 		}
 		buttonsPanel.setBounds(0, this.getHeight()-60, 535, 38);
 
 		return buttonsPanel;
 	}
 
 	/**
 	 * This method initializes cmbVersion
 	 *
 	 * @return javax.swing.JComboBox
 	 */
 	private JComboBox getCmbVersion() {
 		if (cmbVersion == null) {
 			cmbVersion = new JComboBox();
 			cmbVersion.setSize(89, 20);
 			cmbVersion.setLocation(91, 120);
 			for (int i = 0; i < WebMapContext.exportVersions.size(); i++) {
 				cmbVersion.addItem(WebMapContext.exportVersions.get(i));
 			}
 
 		}
 		return cmbVersion;
 	}
 
 
 
 	/**
 	 * This method initializes btnBrowseFileSystem
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBtnBrowseFileSystem() {
 		if (btnBrowseFileSystem == null) {
 			btnBrowseFileSystem = new JButton();
 			btnBrowseFileSystem.setBounds(403, 95, 120, 22);
 			btnBrowseFileSystem.addActionListener(new java.awt.event.ActionListener() {
 
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					JFileChooser fc = new JFileChooser(ImportWebMapContextExtension.WMC_FILECHOOSER_ID, lastPath);
 					fc.setFileFilter(new FileFilter() {
 						public boolean accept(File f) {
 							return f.isDirectory() || f.getAbsolutePath().toLowerCase().endsWith(WebMapContext.FILE_EXTENSION);
 						}
 
 						public String getDescription() {
							return PluginServices.getText(this, "ogc_mapcontext_file");
 						}
 					});
 					if (fc.showOpenDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION){
 
 						String fileName = fc.getSelectedFile().getAbsolutePath();
 						if (!fileName.toLowerCase().endsWith(WebMapContext.FILE_EXTENSION))
 							fileName += WebMapContext.FILE_EXTENSION;
 						targetFile = new File(fileName);
 						getTxtFile().setText(fileName);
 						lastPath  = fileName.substring(0, fileName.lastIndexOf(File.separatorChar));
 					}
 
 					fc = null;
 				}
 			});
 			btnBrowseFileSystem.setText(PluginServices.getText(this, "browse"));
 		}
 		return btnBrowseFileSystem;
 	}
 
 	/**
 	 * This method initializes txtTitle
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtTitle() {
 		if (txtTitle == null) {
 			txtTitle = new JTextField();
 			txtTitle.setBounds(91, 48, 432, 20);
 			txtTitle.setName(PluginServices.getText(this, "title"));
 		}
 		return txtTitle;
 	}
 
 	/**
 	 * This method initializes jTextField
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtId() {
 		if (txtId == null) {
 			txtId = new JTextField();
 			txtId.setBounds(91, 72, 432, 20);
 			txtId.setName(PluginServices.getText(this, "id"));
 		}
 		return txtId;
 	}
 
 	/**
 	 * This method initializes txtFile
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtFile() {
 		if (txtFile == null) {
 			txtFile = new JTextField();
 			txtFile.setBounds(91, 96, 308, 20);
 		}
 		return txtFile;
 	}
 
 	/**
 	 * This method initializes btnAdvanced
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBtnAdvanced() {
 		if (btnAdvanced == null) {
 			btnAdvanced = new JButton();
 //			btnAdvanced.setBounds(403, 160, 120, 25);
 			btnAdvanced.setLocation(403, 160);
 			btnAdvanced.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					advanced = !advanced;
 					switchPanels();
 				}
 			});
 		}
 		return btnAdvanced;
 	}
 
 	/**
 	 * This method initializes btnAceptar
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBtnOk() {
 		if (btnOk == null) {
 			btnOk = new JButton();
 			btnOk.setText(PluginServices.getText(this, "Ok"));
 			btnOk.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					execute("OK");
 				}
 			});
 		}
 		return btnOk;
 	}
 
 	/**
 	 * This method initializes btnCancel
 	 *
 	 * @return javax.swing.JButton
 	 */
 	private JButton getBtnCancel() {
 		if (btnCancel == null) {
 			btnCancel = new JButton();
 			btnCancel.setText(PluginServices.getText(this, "cancel"));
 			btnCancel.addActionListener(new java.awt.event.ActionListener() {
 				public void actionPerformed(java.awt.event.ActionEvent e) {
 					execute("CANCEL");
 				}
 			});
 		}
 		return btnCancel;
 	}
 
 	/**
 	 * This method initializes txtAbstract
 	 *
 	 * @return javax.swing.JTextArea
 	 */
 	private JTextArea getTxtAbstract() {
 		if (txtAbstract == null) {
 			txtAbstract = new JTextArea();
 		}
 		return txtAbstract;
 	}
 
 	/**
 	 * This method initializes scrlAbstract
 	 *
 	 * @return javax.swing.JScrollPane
 	 */
 	private JScrollPane getScrlAbstract() {
 		if (scrlAbstract == null) {
 			scrlAbstract = new JScrollPane();
 			scrlAbstract.setBounds(115, 27, 232, 48);
 			scrlAbstract.setViewportView(getTxtAbstract());
 		}
 		return scrlAbstract;
 	}
 
 	/**
 	 * This method initializes txtLogoURL
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtLogoURL() {
 		if (txtLogoURL == null) {
 			txtLogoURL = new JTextField();
 			txtLogoURL.setBounds(115, 130, 407, 20);
 		}
 		return txtLogoURL;
 	}
 
 	/**
 	 * This method initializes jTextField
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtDescriptionURL() {
 		if (txtDescriptionURL == null) {
 			txtDescriptionURL = new JTextField();
 			txtDescriptionURL.setBounds(115, 104, 232, 20);
 		}
 		return txtDescriptionURL;
 	}
 
 	/**
 	 * This method initializes pnlMapSize
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getPnlMapSize() {
 		if (pnlMapSize == null) {
 			ButtonGroup group = new ButtonGroup();
 			lblHeight = new JLabel();
 			lblHeight.setText(PluginServices.getText(this, "height"));
 			lblHeight.setBounds(11, 79, 52, 20);
 			lblHeight.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblHeight.setEnabled(false);
 			lblHeight.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblWidth = new JLabel();
 			lblWidth.setText(PluginServices.getText(this, "width"));
 			lblWidth.setBounds(11, 56, 52, 20);
 			lblWidth.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblWidth.setEnabled(false);
 			lblWidth.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			pnlMapSize = new JPanel();
 			pnlMapSize.setLayout(null);
 			pnlMapSize.setBounds(351, 19, 179, 108);
 			pnlMapSize.setBorder(javax.swing.BorderFactory.createTitledBorder(
 					  null, PluginServices.getText(this, "map_size_in_pixels"),
 					  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 					  javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
 			pnlMapSize.add(getRdBtnUseViewSize(), null);
 			pnlMapSize.add(getRdBtnCustomSize(), null);
 			pnlMapSize.add(lblWidth, null);
 			pnlMapSize.add(lblHeight, null);
 			pnlMapSize.add(getTxtWidth(), null);
 			pnlMapSize.add(getTxtHeight(), null);
 			group.add(getRdBtnUseViewSize());
 			group.add(getRdBtnCustomSize());
 
 		}
 		return pnlMapSize;
 	}
 
 	/**
 	 * This method initializes rdBtnUseViewSize
 	 *
 	 * @return javax.swing.JRadioButton
 	 */
 	private JRadioButton getRdBtnUseViewSize() {
 		if (rdBtnUseViewSize == null) {
 			rdBtnUseViewSize = new JRadioButton();
 			rdBtnUseViewSize.setBounds(8, 16, 142, 20);
 			rdBtnUseViewSize.setText(PluginServices.getText(this, "use_view_size"));
 			rdBtnUseViewSize.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					boolean b = !getRdBtnUseViewSize().isSelected();
 
 					getTxtWidth().setEnabled(b);
 					getTxtHeight().setEnabled(b);
 				}
 			});
 			rdBtnUseViewSize.setSelected(true);
 		}
 		return rdBtnUseViewSize;
 	}
 
 	/**
 	 * This method initializes jRadioButton
 	 *
 	 * @return javax.swing.JRadioButton
 	 */
 	private JRadioButton getRdBtnCustomSize() {
 		if (rdBtnCustomSize == null) {
 			rdBtnCustomSize = new JRadioButton();
 			rdBtnCustomSize.setBounds(8, 34, 142, 20);
 			rdBtnCustomSize.setText(PluginServices.getText(this, "use_custom_size"));
 		}
 		return rdBtnCustomSize;
 	}
 
 	/**
 	 * This method initializes txtWidth
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtWidth() {
 		if (txtWidth == null) {
 			txtWidth = new JTextField();
 			txtWidth.setBounds(66, 56, 105, 20);
 			txtWidth.setEnabled(false);
 			txtWidth.addKeyListener(new java.awt.event.KeyAdapter() {
 				public void keyTyped(java.awt.event.KeyEvent e) {
 					try {
 						String text = getTxtWidth().getText();
 						Integer.parseInt(text);
 						lastWidth = text;
 					} catch (Exception ex) {
 						getTxtWidth().setText(lastWidth);
 					}
 				}
 			});
 			Dimension sz = exportableViews[getCmbViews()
 				                            .getSelectedIndex()]
 				                            .getMapContext()
 				                            .getViewPort()
 				                            .getImageSize();
 			lastWidth = (sz != null)? (int) sz.getWidth()+"": defaultWidth+"";
 
 			txtWidth.setText(lastWidth);
 		}
 		return txtWidth;
 	}
 
 	/**
 	 * This method initializes jTextField
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtHeight() {
 		if (txtHeight == null) {
 			txtHeight = new JTextField();
 			txtHeight.setBounds(66, 79, 105, 20);
 			txtHeight.setEnabled(false);
 			txtHeight.addKeyListener(new java.awt.event.KeyAdapter() {
 				public void keyTyped(java.awt.event.KeyEvent e) {
 					try {
 						String text = getTxtHeight().getText();
 						Integer.parseInt(text);
 						lastHeight = text;
 					} catch (Exception ex) {
 						getTxtHeight().setText(lastHeight);
 					}
 				}
 			});
 			Dimension sz = exportableViews[getCmbViews()
 				                            .getSelectedIndex()]
 				                            .getMapContext()
 				                            .getViewPort()
 				                            .getImageSize();
 			lastHeight = (sz != null)? (int) sz.getHeight()+"": defaultHeight+"";
 			txtHeight.setText(lastHeight);
 		}
 		return txtHeight;
 	}
 
 	/**
 	 * This method initializes pnlContactInfo
 	 *
 	 * @return javax.swing.JPanel
 	 */
 	private JPanel getPnlContactInfo() {
 		if (pnlContactInfo == null) {
 			lblEMail = new JLabel();
 			lblEMail.setBounds(7, 193, 98, 20);
 			lblEMail.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblEMail.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblEMail.setText(PluginServices.getText(this, "e-mail")+":");
 			lblFax = new JLabel();
 			lblFax.setBounds(274, 168, 57, 20);
 			lblFax.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblFax.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblFax.setText(PluginServices.getText(this, "fax")+":");
 			lblPostCode = new JLabel();
 			lblPostCode.setBounds(7, 143, 98, 20);
 			lblPostCode.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblPostCode.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblPostCode.setText(PluginServices.getText(this, "postcode")+":");
 			lblPhone = new JLabel();
 			lblPhone.setBounds(7, 168, 98, 20);
 			lblPhone.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblPhone.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblPhone.setText(PluginServices.getText(this, "telephone")+":");
 			lblCountry = new JLabel();
 			lblCountry.setBounds(274, 143, 57, 20);
 			lblCountry.setText(PluginServices.getText(this, "country")+":");
 			lblCountry.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblCountry.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblStateProvince = new JLabel();
 			lblStateProvince.setBounds(252, 119, 111, 20);
 			lblStateProvince.setText(PluginServices.getText(this, "state_or_province")+":");
 			lblStateProvince.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblStateProvince.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 
 			lblCity = new JLabel();
 			lblCity.setBounds(7, 119, 98, 20);
 			lblCity.setText(PluginServices.getText(this, "city")+":");
 			lblCity.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblCity.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblContactPosition = new JLabel();
 			lblContactPosition.setBounds(6, 69, 99, 20);
 			lblContactPosition.setHorizontalTextPosition(SwingConstants.RIGHT);
 			lblContactPosition.setHorizontalAlignment(SwingConstants.RIGHT);
 			lblContactPosition.setText(PluginServices.getText(this, "contact_position"));
 			lblContactOrganization = new JLabel();
 			lblContactOrganization.setBounds(6, 44, 99, 20);
 			lblContactOrganization.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblContactOrganization.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblContactOrganization.setText(PluginServices.getText(this, "contact_organization")+":");
 			lblAddress = new JLabel();
 			lblAddress.setBounds(6, 94, 99, 20);
 			lblAddress.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblAddress.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblAddress.setText(PluginServices.getText(this, "address"));
 
 			lblContactPerson = new JLabel();
 			lblContactPerson.setText(PluginServices.getText(this, "contact_person")+":");
 			lblContactPerson.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 			lblContactPerson.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
 			lblContactPerson.setBounds(6, 19, 99, 20);
 			pnlContactInfo = new JPanel();
 			pnlContactInfo.setLayout(null);
 			pnlContactInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(
 					  null, PluginServices.getText(this, "contact_info"),
 					  javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
 					  javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
 			pnlContactInfo.setLocation(4, 158);
 			pnlContactInfo.setSize(527, 223);
 			pnlContactInfo.add(lblContactPerson, null);
 			pnlContactInfo.add(getTxtContactPerson(), null);
 			pnlContactInfo.add(lblContactOrganization, null);
 			pnlContactInfo.add(getTxtOrganization(), null);
 			pnlContactInfo.add(lblContactPosition, null);
 			pnlContactInfo.add(getTxtContactPosition(), null);
 			pnlContactInfo.add(lblAddress, null);
 			pnlContactInfo.add(getTxtAddress(), null);
 			pnlContactInfo.add(lblCity, null);
 			pnlContactInfo.add(getTxtCity(), null);
 			pnlContactInfo.add(lblStateProvince, null);
 			pnlContactInfo.add(getCmbCountries(), null);
 			pnlContactInfo.add(getTxtStateOrProvince(), null);
 			pnlContactInfo.add(lblPostCode, null);
 			pnlContactInfo.add(getTxtPostCode(), null);
 			pnlContactInfo.add(lblCountry, null);
 			pnlContactInfo.add(lblPhone, null);
 			pnlContactInfo.add(getTxtTelephone(), null);
 			pnlContactInfo.add(lblFax, null);
 			pnlContactInfo.add(getTxtFax(), null);
 			pnlContactInfo.add(lblEMail, null);
 			pnlContactInfo.add(getTxtEMail(), null);
 		}
 		return pnlContactInfo;
 	}
 
 	/**
 	 * This method initializes jTextField1
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtKeyWords() {
 		if (txtKeyWords == null) {
 			txtKeyWords = new JTextField();
 			txtKeyWords.setBounds(115, 79, 232, 20);
 		}
 		return txtKeyWords;
 	}
 
 	/**
 	 * This method initializes jTextField1
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtContactPerson() {
 		if (txtContactPerson == null) {
 			txtContactPerson = new JTextField();
 			txtContactPerson.setBounds(111, 19, 407, 20);
 		}
 		return txtContactPerson;
 	}
 
 	/**
 	 * This method initializes jTextField2
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtOrganization() {
 		if (txtOrganization == null) {
 			txtOrganization = new JTextField();
 			txtOrganization.setBounds(111, 44, 407, 20);
 		}
 		return txtOrganization;
 	}
 
 	/**
 	 * This method initializes jTextField
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtContactPosition() {
 		if (txtContactPosition == null) {
 			txtContactPosition = new JTextField();
 			txtContactPosition.setBounds(111, 69, 407, 20);
 		}
 		return txtContactPosition;
 	}
 
 	/**
 	 * This method initializes txtAddress
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtAddress() {
 		if (txtAddress == null) {
 			txtAddress = new JTextField();
 			txtAddress.setBounds(111, 94, 407, 20);
 		}
 		return txtAddress;
 	}
 
 	/**
 	 * This method initializes txtCity
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtCity() {
 		if (txtCity == null) {
 			txtCity = new JTextField();
 			txtCity.setBounds(111, 119, 135, 20);
 		}
 		return txtCity;
 	}
 
 	/**
 	 * This method initializes txtStateOrProvince
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtStateOrProvince() {
 		if (txtStateOrProvince == null) {
 			txtStateOrProvince = new JTextField();
 			txtStateOrProvince.setBounds(368, 119, 150, 20);
 		}
 		return txtStateOrProvince;
 	}
 
 	/**
 	 * This method initializes txtPostCode
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtPostCode() {
 		if (txtPostCode == null) {
 			txtPostCode = new JTextField();
 			txtPostCode.setBounds(111, 143, 159, 20);
 		}
 		return txtPostCode;
 	}
 
 	/**
 	 * This method initializes cmbCountries
 	 *
 	 * @return javax.swing.JComboBox
 	 */
 	private JComboBox getCmbCountries() {
 		if (cmbCountries == null) {
 			cmbCountries = new JComboBox();
 			cmbCountries.setBounds(336, 143, 182, 20);
 			String[] countries = PluginServices.getText(this, "countries_of_the_world").split(";");
 			for (int i = 0; i < countries.length; i++) {
 				cmbCountries.addItem(countries[i]);
 			}
 		}
 		return cmbCountries;
 	}
 
 	/**
 	 * This method initializes txtTelephone
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtTelephone() {
 		if (txtTelephone == null) {
 			txtTelephone = new JTextField();
 			txtTelephone.setBounds(111, 168, 159, 20);
 		}
 		return txtTelephone;
 	}
 
 	/**
 	 * This method initializes txtFax
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtFax() {
 		if (txtFax == null) {
 			txtFax = new JTextField();
 			txtFax.setBounds(336, 168, 182, 20);
 		}
 		return txtFax;
 	}
 
 	/**
 	 * This method initializes txtEMail
 	 *
 	 * @return javax.swing.JTextField
 	 */
 	private JTextField getTxtEMail() {
 		if (txtEMail == null) {
 			txtEMail = new JTextField();
 			txtEMail.setBounds(111, 193, 407, 20);
 		}
 		return txtEMail;
 	}
 
 	public Object getWindowModel() {
 		return PluginServices.getText(this, "web_map_context_settings");
 	}
 
 	private void execute(String actionCommand) {
 		if ("OK".equals(actionCommand)) {
 			String str = getTxtFile().getText();
 			if (str==null || str.equals("")) {
 				JOptionPane.showMessageDialog(this, PluginServices.getText(this, "must_specify_a_file"), PluginServices.getText(this, "error"), JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			ProjectView theView = exportableViews[getCmbViews().getSelectedIndex()];
 			MapContext mc = theView.getMapContext();
 			wmc = new WebMapContext();
 
 			// Version
 			wmc.fileVersion = (String) getCmbVersion().getSelectedItem();
 
 			// Web Map Context size
 			if (advanced) {
 				if (getRdBtnUseViewSize().isSelected()) {
 					// View's size
 					Dimension sz = null;
 					if (mc.getViewPort().getImageSize()!=null)
 						sz = new Dimension(mc.getViewPort().getImageSize());
 					if (sz == null || (int) sz.getHeight()==0 || (int) sz.getWidth()==0)
 						// View's size is not initialized, will use default size.
 						sz = new Dimension(defaultWidth, defaultHeight);
 					wmc.windowSize = sz;
 				} else {
 					// User defined size
 					try {
 					wmc.windowSize = new Dimension(Integer.parseInt(getTxtWidth().getText()),
 												   Integer.parseInt(getTxtHeight().getText()));
 					} catch (Exception e) {
 						JOptionPane.showMessageDialog(this, PluginServices.getText(this, "invalid_dimension_values"), PluginServices.getText(this, "error"), JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 				}
 			}
 			// SRS
 			wmc.srs = mc.getProjection().getAbrev();
 
 			// Bounding Box
 			if (useFullExtent)
 				try {
 					wmc.bBox = mc.getFullExtent();
 				} catch (ReadDriverException e) {
 					NotificationManager.addError(e);
 					return;
 				}
 			else
 				wmc.bBox = mc.getViewPort().getAdjustedExtent();
 
 			// Title
 			str = getTxtTitle().getText();
 			if (str!=null && !str.equals(""))
 				wmc.title = str;
 			else {
 				JOptionPane.showMessageDialog(this, getTxtTitle().getName()+" "+PluginServices.getText(this, "is_required"), PluginServices.getText(this, "error"), JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			// ID
 			str = getTxtId().getText();
 			if (str!=null && !str.equals(""))
 				wmc.id = str;
 			else {
 				JOptionPane.showMessageDialog(this, getTxtId().getName()+" "+PluginServices.getText(this, "is_required"), PluginServices.getText(this, "error"), JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			wmc.xmlns = WebMapContextTags.XMLNS_VALUE;
 			wmc.xmlns_xlink = WebMapContextTags.XMLNS_XLINK_VALUE;
 			wmc.xmlns_xsi = WebMapContextTags.XMLNS_XSI_VALUE;
 			wmc.xsi_schemaLocation = WebMapContextTags.XSI_SCHEMA_LOCATION_VALUE;
 
 			// Abstract
 			str = getTxtAbstract().getText();
 			if (str!=null && !str.equals(""))
 				wmc._abstract = str;
 
 			// Keyword list
 			str = getTxtKeyWords().getText();
 			if (str!=null && !str.equals("")) {
 				String[] ss = str.split("[, ;:]+"); // sequence of spaces, commas, colons or semicolons will be treated as word separator
 				for (int i = 0; i < ss.length; i++) {
 					if (wmc.keywordList==null) wmc.keywordList = new ArrayList();
 					wmc.keywordList.add(ss[i]);
 				}
 			}
 
 			// Logo URL
 			str = getTxtLogoURL().getText();
 			if (str!=null && !str.equals(""))
 				wmc.logoURL = str;
 
 			wmc.logoURLSize = null;			// yet skiped
 			wmc.logoURLFormat = null;		// yet skiped
 			wmc.descriptionURLFormat = null;// yet skiped
 
 			// Description URL
 			str = getTxtLogoURL().getText();
 			if (str!=null && !str.equals(""))
 				wmc.descriptionURL = str;
 
 			// Contact Person
 			str = getTxtContactPerson().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.contactPerson = str;
 				wmc.contactInfo = true;
 			}
 
 			// Contact Organization
 			str = getTxtOrganization().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.contactOrganization = str;
 				wmc.contactInfo = true;
 			}
 
 			// Contact Position
 			str = getTxtContactPosition().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.contactPosition = str;
 				wmc.contactInfo = true;
 			}
 
 			// Address
 			str = getTxtAddress().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.address = str;
 				wmc.contactInfo = true;
 			}
 
 			// City
 			str = getTxtCity().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.city = str;
 				wmc.contactInfo = true;
 			}
 
 			// State/Province
 			str = getTxtStateOrProvince().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.stateOrProvince = str;
 				wmc.contactInfo = true;
 			}
 
 			// Postcode
 			str = getTxtPostCode().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.postCode = str;
 			}
 
 			// Country
 			str = (String) getCmbCountries().getSelectedItem();
 			if (str!=null && !str.equals("")) {
 				wmc.country = str;
 			}
 
 			// Telephone
 			str = getTxtTelephone().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.telephone = str;
 				wmc.contactInfo = true;
 			}
 
 			// Fax
 			str = getTxtFax().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.fax = str;
 				wmc.contactInfo = true;
 			}
 
 			// e-mail
 			str = getTxtEMail().getText();
 			if (str!=null && !str.equals("")) {
 				wmc.email = str;
 				wmc.contactInfo = true;
 			}
 
 			ExportWebMapContextExtension exp = (ExportWebMapContextExtension)
 			PluginServices.
 			getExtension(ExportWebMapContextExtension.class);
 			exp.execute("DO_EXPORT");
 			PluginServices.getMDIManager().closeWindow(this);
 		} else if ("CANCEL".equals(actionCommand)) {
 			PluginServices.getMDIManager().closeWindow(this);
 		}
 	}
 
 	public String getXML() {
 		return wmc.toXML(exportableViews[getCmbViews().getSelectedIndex()]);
 	}
 
 	public File getTargetFile() {
 		if (targetFile == null) {
 			String fileName = getTxtFile().getText();
 			if (fileName != null)
 				targetFile = new File(fileName);
 		}
 		return targetFile;
 	}
 
 	/**
 	 * This method initializes cmbViews
 	 *
 	 * @return javax.swing.JComboBox
 	 */
 	private JComboBox getCmbViews() {
 		if (cmbViews == null) {
 			cmbViews = new JComboBox();
 			cmbViews.setBounds(91, 24, 432, 21);
 			cmbViews.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					ProjectView theView = exportableViews[getCmbViews().getSelectedIndex()];
 					Dimension sz = theView.getMapContext().
 										   getViewPort().
 										   getImageSize();
 					if (sz == null || (int) sz.getHeight()==0 || (int) sz.getWidth()==0)
 						sz = new Dimension(defaultWidth, defaultHeight);
 
 					getTxtWidth().setText((int) sz.getWidth()+"");
 					getTxtHeight().setText((int) sz.getHeight()+"");
 					getTxtTitle().setText(theView.getName());
 				}
 			});
 			for (int i = 0; i < exportableViews.length; i++) {
 				cmbViews.addItem(exportableViews[i].getName());
 			}
 		}
 		return cmbViews;
 	}
 
 	/**
 	 * This method initializes rdCurrentViewExtent
 	 *
 	 * @return javax.swing.JRadioButton
 	 */
 	private JRadioButton getRdBtnCurrentViewExtent() {
 		if (rdBtnCurrentViewExtent == null) {
 			rdBtnCurrentViewExtent = new JRadioButton();
 			rdBtnCurrentViewExtent.setBounds(91, 146, 270, 20);
 			rdBtnCurrentViewExtent.setText(PluginServices.getText(this, "defined_by_view_extent"));
 			rdBtnCurrentViewExtent.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					fPrefs.putBoolean("use_full_extent", !getRdBtnFullExtent().isEnabled());
 				}
 			});
 			rdBtnCurrentViewExtent.setEnabled(!useFullExtent);
 		}
 		return rdBtnCurrentViewExtent;
 	}
 
 	/**
 	 * This method initializes rdBtnFullExtent
 	 *
 	 * @return javax.swing.JRadioButton
 	 */
 	private JRadioButton getRdBtnFullExtent() {
 		if (rdBtnFullExtent == null) {
 			rdBtnFullExtent = new JRadioButton();
 			rdBtnFullExtent.setBounds(91, 164, 270, 20);
 			rdBtnFullExtent.setText(PluginServices.getText(this, "use_full_extent"));
 			rdBtnFullExtent.addItemListener(new java.awt.event.ItemListener() {
 				public void itemStateChanged(java.awt.event.ItemEvent e) {
 					boolean b = getRdBtnFullExtent().isEnabled();
 					fPrefs.putBoolean("use_full_extent", getRdBtnFullExtent().isEnabled());
 					useFullExtent = b;
 				}
 			});
 			rdBtnFullExtent.setSelected(useFullExtent);
 		}
 		return rdBtnFullExtent;
 	}
 
 	public Object getWindowProfile() {
 		return WindowInfo.PROPERTIES_PROFILE;
 	}
 }
