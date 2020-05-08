 package org.gwaspi.gui;
 
 import org.gwaspi.constants.cImport;
 import org.gwaspi.constants.cImport.ImportFormat;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.utils.BrowserHelpUrlAction;
 import org.gwaspi.gui.utils.CursorUtils;
 import org.gwaspi.gui.utils.Dialogs;
 import org.gwaspi.gui.utils.HelpURLs;
 import org.gwaspi.gui.utils.LimitedLengthDocument;
 import org.gwaspi.gui.utils.MoreGWASinOneGoInfo;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.File;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 import org.gwaspi.netCDF.loader.GenotypesLoadDescription;
 import org.gwaspi.netCDF.matrices.MatrixMetadata;
 import org.gwaspi.netCDF.operations.GWASinOneGOParams;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.gwaspi.threadbox.MultiOperations;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class LoadDataPanel extends JPanel {
 
 	private static final Logger log = LoggerFactory.getLogger(LoadDataPanel.class);
 
 	// Variables declaration - do not modify
 	private JButton btn_Back;
 	private JButton btn_File1;
 	private JButton btn_File2;
 	private JButton btn_FileSampleInfo;
 	private JButton btn_Go;
 	private JButton btn_Help;
 	private JComboBox cmb_Format;
 	private JLabel lbl_File1;
 	private JLabel lbl_File2;
 	private JLabel lbl_FileSampleInfo;
 	private JLabel lbl_Format;
 	private JLabel lbl_NewMatrixName;
 	private JPanel pnl_Footer;
 	private JPanel pnl_Input;
 	private JPanel pnl_NameAndDesc;
 	private JPanel pnl_Gif;
 	private JPanel pnl_GifLeft;
 	private JScrollPane scrl_NewMatrixDescription;
 	private JTextArea txtA_NewMatrixDescription;
 	private JTextField txt_File1;
 	private JTextField txt_File2;
 	private JTextField txt_FileSampleInfo;
 	private JTextField txt_NewMatrixName;
 	private boolean dummySamples = true;
 	private int studyId;
 	private boolean[] fieldObligatoryState;
 	private GWASinOneGOParams gwasParams = new GWASinOneGOParams();
 	private final Action formatAction;
 	private final Action browseSampleInfoAction;
 	// End of variables declaration
 
 	public LoadDataPanel(int _studyId) {
 
 		studyId = _studyId;
 
 		pnl_NameAndDesc = new JPanel();
 		lbl_NewMatrixName = new JLabel();
 		txt_NewMatrixName = new JTextField();
 		scrl_NewMatrixDescription = new JScrollPane();
 		txtA_NewMatrixDescription = new JTextArea();
 		pnl_Input = new JPanel();
 		lbl_Format = new JLabel();
 		cmb_Format = new JComboBox();
 		lbl_File1 = new JLabel();
 		txt_File1 = new JTextField();
 		btn_File1 = new JButton();
 		lbl_File2 = new JLabel();
 		txt_File2 = new JTextField();
 		btn_File2 = new JButton();
 		lbl_FileSampleInfo = new JLabel();
 		txt_FileSampleInfo = new JTextField();
 		btn_FileSampleInfo = new JButton();
 		pnl_Footer = new JPanel();
 		btn_Back = new JButton();
 		btn_Go = new JButton();
 		btn_Help = new JButton();
 		pnl_Gif = new JPanel();
 		pnl_GifLeft = new JPanel();
 
 		formatAction = new LoadDataPanel.FormatAction();
 		browseSampleInfoAction = new LoadDataPanel.BrowseSampleInfoAction();
 
 		setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.importGenotypes, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("FreeSans", 1, 18))); // NOI18N
 
 		pnl_NameAndDesc.setBorder(BorderFactory.createTitledBorder(null, Text.All.nameAndDescription, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
 		lbl_NewMatrixName.setText(Text.Matrix.newMatrixName);
 		txt_NewMatrixName.setDocument(new LimitedLengthDocument(63));
 		txt_NewMatrixName.requestFocus();
 		txtA_NewMatrixDescription.setColumns(20);
 		txtA_NewMatrixDescription.setLineWrap(true);
 		txtA_NewMatrixDescription.setRows(5);
 		txtA_NewMatrixDescription.setBorder(BorderFactory.createTitledBorder(Text.All.description));
 		txtA_NewMatrixDescription.setDocument(new LimitedLengthDocument(1999));
 		txtA_NewMatrixDescription.setText(Text.All.optional);
 		txtA_NewMatrixDescription.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
 							txtA_NewMatrixDescription.selectAll();
 						}
 					}
 				});
 			}
 
 			@Override
 			public void focusLost(FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						txtA_NewMatrixDescription.select(0, 0);
 					}
 				});
 			}
 		});
 		scrl_NewMatrixDescription.setViewportView(txtA_NewMatrixDescription);
 
 		//<editor-fold defaultstate="collapsed" desc="LAYOUT NAME & DESC">
 		GroupLayout pnl_NameAndDescLayout = new GroupLayout(pnl_NameAndDesc);
 		pnl_NameAndDesc.setLayout(pnl_NameAndDescLayout);
 		pnl_NameAndDescLayout.setHorizontalGroup(
 				pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_NameAndDescLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
 				.addComponent(scrl_NewMatrixDescription, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 725, Short.MAX_VALUE)
 				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
 				.addComponent(lbl_NewMatrixName)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(txt_NewMatrixName, GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE)))
 				.addContainerGap()));
 		pnl_NameAndDescLayout.setVerticalGroup(
 				pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_NameAndDescLayout.createSequentialGroup()
 				.addGroup(pnl_NameAndDescLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(lbl_NewMatrixName)
 				.addComponent(txt_NewMatrixName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(scrl_NewMatrixDescription, GroupLayout.PREFERRED_SIZE, 116, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 
 		//</editor-fold>
 
 		pnl_Input.setBorder(BorderFactory.createTitledBorder(null, Text.Matrix.input, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("DejaVu Sans", 1, 13))); // NOI18N
 
 		lbl_Format.setText(Text.Matrix.format);
 
		List<ImportFormat> importFormatsList = new ArrayList<ImportFormat>(Arrays.asList(ImportFormat.values()));
 		importFormatsList.remove(ImportFormat.UNKNOWN);
 
 		cmb_Format.setModel(new DefaultComboBoxModel(importFormatsList.toArray()));
 		cmb_Format.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent evt) {
 				formatAction.actionPerformed(evt);
 			}
 		});
 		formatAction.actionPerformed(null);
 
 		lbl_File1.setText(Text.All.file1);
 		txt_File1.setEnabled(false);
 		txt_File1.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						txt_File1.selectAll();
 					}
 				});
 			}
 		});
 		btn_File1.setAction(new LoadDataPanel.Browse1Action());
 
 		lbl_File2.setText(Text.All.file2);
 		txt_File2.setEnabled(false);
 		txt_File2.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						txt_File2.selectAll();
 					}
 				});
 			}
 		});
 		btn_File2.setAction(new LoadDataPanel.Browse2Action());
 
 		lbl_FileSampleInfo.setText(Text.All.file3);
 		txt_FileSampleInfo.setEnabled(false);
 		txt_FileSampleInfo.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent evt) {
 				SwingUtilities.invokeLater(new Runnable() {
 					@Override
 					public void run() {
 						txt_FileSampleInfo.selectAll();
 					}
 				});
 			}
 		});
 
 		btn_FileSampleInfo.setAction(browseSampleInfoAction);
 
 		//<editor-fold defaultstate="collapsed" desc="LAYOUT InputLayout">
 		GroupLayout pnl_InputLayout = new GroupLayout(pnl_Input);
 		pnl_Input.setLayout(pnl_InputLayout);
 		pnl_InputLayout.setHorizontalGroup(
 				pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_InputLayout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(lbl_Format)
 				.addComponent(lbl_File1)
 				.addComponent(lbl_File2)
 				.addComponent(lbl_FileSampleInfo))
 				.addGap(130, 130, 130)
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(txt_File2, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
 				.addGroup(pnl_InputLayout.createSequentialGroup()
 				.addComponent(cmb_Format, 0, 294, Short.MAX_VALUE)
 				.addGap(161, 161, 161))
 				.addComponent(txt_File1, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE)
 				.addComponent(txt_FileSampleInfo, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 455, Short.MAX_VALUE))
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(btn_File1, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_File2, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_FileSampleInfo, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 88, GroupLayout.PREFERRED_SIZE))
 				.addContainerGap()));
 
 
 		pnl_InputLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_File1, btn_File2, btn_FileSampleInfo});
 
 		pnl_InputLayout.setVerticalGroup(
 				pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_InputLayout.createSequentialGroup()
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 				.addComponent(lbl_Format)
 				.addComponent(cmb_Format, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 				.addGap(18, 18, 18)
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
 				.addComponent(lbl_File1)
 				.addComponent(txt_File1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_File1))
 				.addGap(18, 18, 18)
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(lbl_File2)
 				.addComponent(txt_File2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_File2))
 				.addGap(18, 18, 18)
 				.addGroup(pnl_InputLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(lbl_FileSampleInfo)
 				.addComponent(txt_FileSampleInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_FileSampleInfo))
 				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 		//</editor-fold>
 
 		btn_Back.setAction(new LoadDataPanel.BackAction(studyId));
 
 		btn_Go.setAction(new LoadDataPanel.LoadGenotypesAction());
 
 		btn_Help.setAction(new BrowserHelpUrlAction(HelpURLs.QryURL.loadGts));
 
 		//<editor-fold defaultstate="collapsed" desc="LAYOUT FOOTER">
 		GroupLayout pnl_FooterLayout = new GroupLayout(pnl_Footer);
 		pnl_Footer.setLayout(pnl_FooterLayout);
 		pnl_FooterLayout.setHorizontalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(GroupLayout.Alignment.TRAILING, pnl_FooterLayout.createSequentialGroup()
 				.addComponent(btn_Back, GroupLayout.PREFERRED_SIZE, 79, GroupLayout.PREFERRED_SIZE)
 				.addGap(18, 18, 18)
 				.addComponent(btn_Help)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 409, Short.MAX_VALUE)
 				.addComponent(btn_Go, GroupLayout.PREFERRED_SIZE, 162, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap()));
 
 		pnl_FooterLayout.linkSize(SwingConstants.HORIZONTAL, new Component[]{btn_Back, btn_Help});
 
 		pnl_FooterLayout.setVerticalGroup(
 				pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(pnl_FooterLayout.createSequentialGroup()
 				.addGap(0, 0, 0)
 				.addGroup(pnl_FooterLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 				.addComponent(btn_Go, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
 				.addComponent(btn_Back)
 				.addComponent(btn_Help))));
 		//</editor-fold>
 
 		//<editor-fold defaultstate="collapsed" desc="LAYOUT GIF">
 		GroupLayout pnl_GifLeftLayout = new GroupLayout(pnl_GifLeft);
 //		pnl_GifLeft.setLayout(pnl_GifLeftLayout);
 //		pnl_GifLeftLayout.setHorizontalGroup(
 //				pnl_GifLeftLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGap(0, 308, Short.MAX_VALUE)
 //				);
 //		pnl_GifLeftLayout.setVerticalGroup(
 //				pnl_GifLeftLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGap(0, 100, Short.MAX_VALUE)
 //				);
 //
 //		GroupLayout pnl_GifCenterLayout = new GroupLayout(pnl_GifCenter);
 //		pnl_GifCenter.setLayout(pnl_GifCenterLayout);
 //		pnl_GifCenterLayout.setHorizontalGroup(
 //				pnl_GifCenterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addComponent(scrl_Gif, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 //				);
 //		pnl_GifCenterLayout.setVerticalGroup(
 //				pnl_GifCenterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addComponent(scrl_Gif, GroupLayout.PREFERRED_SIZE, 100, GroupLayout.PREFERRED_SIZE)
 //				);
 //
 //		GroupLayout pnl_GifRightLayout = new GroupLayout(pnl_GifRight);
 //		pnl_GifRight.setLayout(pnl_GifRightLayout);
 //		pnl_GifRightLayout.setHorizontalGroup(
 //				pnl_GifRightLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGap(0, 284, Short.MAX_VALUE)
 //				);
 //		pnl_GifRightLayout.setVerticalGroup(
 //				pnl_GifRightLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGap(0, 100, Short.MAX_VALUE)
 //				);
 //
 //		GroupLayout pnl_GifLayout = new GroupLayout(pnl_Gif);
 //		pnl_Gif.setLayout(pnl_GifLayout);
 //		pnl_GifLayout.setHorizontalGroup(
 //				pnl_GifLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(pnl_GifLayout.createSequentialGroup()
 //				.addContainerGap()
 //				.addComponent(pnl_GifLeft, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 //				.addGap(18, 18, 18)
 //				.addComponent(pnl_GifCenter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 //				.addGap(18, 18, 18)
 //				.addComponent(pnl_GifRight, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 //				.addGap(21, 21, 21))
 //				);
 //		pnl_GifLayout.setVerticalGroup(
 //				pnl_GifLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
 //				.addGroup(pnl_GifLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
 //				.addComponent(pnl_GifRight, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 //				.addComponent(pnl_GifLeft, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 //				.addComponent(pnl_GifCenter, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 //				);
 		//</editor-fold>
 
 		//<editor-fold defaultstate="collapsed" desc="LAYOUT">
 		GroupLayout layout = new GroupLayout(this);
 		this.setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				.addContainerGap()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addComponent(pnl_Gif, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addComponent(pnl_Input, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addComponent(pnl_NameAndDesc, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 				.addComponent(pnl_Footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				.addContainerGap()));
 		layout.setVerticalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 				.addComponent(pnl_NameAndDesc, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(pnl_Input, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 				.addComponent(pnl_Footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
 				.addComponent(pnl_Gif, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
 		//</editor-fold>
 
 		cmb_Format.setSelectedIndex(2);
 		formatAction.actionPerformed(null);
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="SET FIELD NAMES & DEFAULTS">
 	private class FormatAction extends AbstractAction { // FIXME make static
 
 		FormatAction() {
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			lbl_File1.setForeground(Color.black);
 			lbl_File2.setForeground(Color.black);
 			lbl_FileSampleInfo.setForeground(Color.black);
 
 			switch ((ImportFormat) cmb_Format.getSelectedItem()) {
 				case Affymetrix_GenomeWide6:
 					fieldObligatoryState = new boolean[]{true, true, false};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File2.setText(Text.Matrix.annotationFile);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File1.setText(Text.Matrix.genotypes + " " + Text.All.folder);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case PLINK:
 					fieldObligatoryState = new boolean[]{true, true, false};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.mapFile);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File2.setText(Text.Matrix.pedFile);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case PLINK_Binary:
 					fieldObligatoryState = new boolean[]{true, true, true};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.bedFile);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfoOrFam);
 					lbl_File2.setText(Text.Matrix.bimFile);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case HAPMAP:
 					fieldObligatoryState = new boolean[]{true, false, false};
 					lbl_File1.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File2.setEnabled(false);
 					lbl_File1.setText(Text.Matrix.genotypes);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File2.setText("");
 					txt_File1.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_File2.setEnabled(false);
 					txt_File2.setText("");
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					btn_File2.setEnabled(false);
 					break;
 				case BEAGLE:
 					fieldObligatoryState = new boolean[]{true, true, false};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.genotypes);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File2.setText(Text.Matrix.markerFile);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case HGDP1:
 					fieldObligatoryState = new boolean[]{true, true, false};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.genotypes);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File2.setText(Text.Matrix.markerFile);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case GWASpi:
 					fieldObligatoryState = new boolean[]{true, false, true};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(false);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.matrix);
 					lbl_File2.setText("");
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(false);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_File2.setText("");
 					txt_FileSampleInfo.setText("");
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(false);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case Illumina_LGEN:
 					fieldObligatoryState = new boolean[]{true, true, false};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.mapFile);
 					lbl_File2.setText(Text.Matrix.lgenFile);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					txt_File2.setText("");
 					txt_FileSampleInfo.setText(Text.All.optional);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				case Sequenom:
 					fieldObligatoryState = new boolean[]{true, true, true};
 					lbl_File1.setEnabled(true);
 					lbl_File2.setEnabled(true);
 					lbl_FileSampleInfo.setEnabled(true);
 					lbl_File1.setText(Text.Matrix.genotypes);
 					lbl_FileSampleInfo.setText(Text.Matrix.sampleInfo);
 					lbl_File2.setText(Text.Matrix.markerFile);
 					txt_File1.setEnabled(true);
 					txt_File2.setEnabled(true);
 					txt_FileSampleInfo.setEnabled(true);
 					btn_File1.setEnabled(true);
 					btn_File2.setEnabled(true);
 					browseSampleInfoAction.setEnabled(true);
 					break;
 				default:
 					fieldObligatoryState = new boolean[]{false, false, false};
 					lbl_File1.setEnabled(false);
 					lbl_File1.setText("");
 					lbl_File2.setEnabled(false);
 					lbl_File2.setText("");
 					lbl_FileSampleInfo.setEnabled(false);
 					lbl_FileSampleInfo.setText("");
 					txt_File1.setEnabled(false);
 					txt_File2.setEnabled(false);
 					txt_FileSampleInfo.setEnabled(false);
 					btn_File1.setEnabled(false);
 					btn_File2.setEnabled(false);
 					btn_FileSampleInfo.setEnabled(false);
 			}
 		}
 	}
 
 	//</editor-fold>
 	private class LoadGenotypesAction extends AbstractAction { // FIXME make static
 
 		LoadGenotypesAction() {
 
 			putValue(NAME, Text.All.go);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				String newMatrixName = txt_NewMatrixName.getText().trim();
 				if (!newMatrixName.isEmpty()) {
 					lbl_NewMatrixName.setForeground(Color.black);
 					boolean[] filesOK = validateFiles();
 					if (filesOK[0] && filesOK[1] && filesOK[2]) {
 						lbl_File1.setForeground(Color.black);
 						lbl_File2.setForeground(Color.black);
 						lbl_FileSampleInfo.setForeground(Color.black);
 
 						File sampleInfoDir = new File(txt_FileSampleInfo.getText());
 						if (sampleInfoDir.isFile()) {
 							dummySamples = false;
 						}
 
 						int performGwasInOneGo = Dialogs.showOptionDialogue(Text.Matrix.gwasInOne, Text.Matrix.ifCaseCtrlDetected, Text.All.yes, Text.Matrix.noJustLoad, Text.All.cancel);
 
 						if (performGwasInOneGo == JOptionPane.YES_OPTION) {
 							// ASK MORE QUESTIONS
 							gwasParams = new MoreGWASinOneGoInfo().showMoreInfo(cmb_Format.getSelectedItem().toString());
 							if (gwasParams.isProceed()) {
 								gwasParams.setFriendlyName(Dialogs.showInputBox(Text.Operation.GTFreqAndHWFriendlyName));
 							}
 						} else if (performGwasInOneGo == JOptionPane.NO_OPTION) {
 							gwasParams.setProceed(true);
 						}
 
 						//<editor-fold defaultstate="collapsed" desc="DATA LOAD">
 						if (txtA_NewMatrixDescription.getText().equals(Text.All.optional)) {
 							txtA_NewMatrixDescription.setText("");
 						}
 						if (gwasParams.isProceed()) {
 							// DO LOAD & GWAS
 							GenotypesLoadDescription loadDescription = new GenotypesLoadDescription(
 									txt_File1.getText(),
 									txt_FileSampleInfo.getText(),
 									txt_File2.getText(),
 									studyId,
 									(ImportFormat) cmb_Format.getSelectedItem(),
 									newMatrixName,
 									txtA_NewMatrixDescription.getText(),
 									gwasParams.getChromosome(),
 									gwasParams.getStrandType(),
 									gwasParams.getGtCode()
 									);
 							MultiOperations.loadMatrixDoGWASifOK(
 									loadDescription,
 									dummySamples,
 									performGwasInOneGo == JOptionPane.YES_OPTION,
 									gwasParams);
 
 							ProcessTab.getSingleton().showTab();
 						}
 						//</editor-fold>
 					}
 
 					if (!filesOK[0]) {
 						lbl_File1.setForeground(Color.red);
 						Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField + lbl_File1.getText() + "!");
 					}
 					if (!filesOK[1]) {
 						lbl_File2.setForeground(Color.red);
 						Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField + lbl_File2.getText() + "!");
 					}
 					if (!filesOK[2]) {
 						lbl_FileSampleInfo.setForeground(Color.red);
 						Dialogs.showWarningDialogue(Text.Matrix.warnInputFileInField + lbl_FileSampleInfo.getText() + "!");
 					}
 				} else {
 					lbl_NewMatrixName.setForeground(Color.red);
 					setCursor(CursorUtils.defaultCursor);
 					Dialogs.showWarningDialogue(Text.Matrix.warnInputNewMatrixName);
 				}
 			} catch (Exception ex) {
 				try {
 					Dialogs.showWarningDialogue(Text.All.warnLoadError + "\n" + Text.All.warnWrongFormat);
 					log.error(Text.All.warnLoadError, ex);
 					log.error(Text.All.warnWrongFormat);
 
 					// DELETE BROKEN NEW MATRIX AND REPORTS
 					MatrixMetadata deleteMxMetaData = org.gwaspi.netCDF.matrices.MatrixManager.getLatestMatrixId();
 					if (deleteMxMetaData.getMatrixFriendlyName().equals(txt_NewMatrixName.getText())) {
 						log.info("Deleting orphan files and references");
 						org.gwaspi.netCDF.matrices.MatrixManager.deleteMatrix(deleteMxMetaData.getMatrixId(), true);
 					}
 
 					GWASpiExplorerPanel.getSingleton().updateTreePanel(true);
 				} catch (IOException ex1) {
 					log.error(null, ex);
 				}
 				//Logger.getLogger(LoadDataPanel.class.getName()).log(Level.SEVERE, null, ex);
 			}
 		}
 	}
 
 	//<editor-fold defaultstate="collapsed" desc="HELPER METHODS">
 	private class Browse1Action extends AbstractAction { // FIXME make static
 
 		Browse1Action() {
 
 			setEnabled(false);
 			putValue(NAME, Text.All.browse);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			// CHECK IF HOMONYM .PED FILE EXISTS IN PLINK CASE
 			if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK)) {
 				// Use standard file opener
 				Dialogs.selectAndSetFileDialog(evt, btn_File1, txt_File1, "");
 				if (!txt_File1.getText().isEmpty()) {
 					File pedFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".ped");
 					if (txt_File2.getText().isEmpty() && pedFile.exists()) {
 						txt_File2.setText(pedFile.getPath());
 					} else if (pedFile.exists()) {
 						int option = Dialogs.showConfirmDialogue(Text.Matrix.findComplementaryPlink);
 						if (option == JOptionPane.YES_OPTION) {
 							txt_File2.setText(pedFile.getPath());
 						}
 					}
 				}
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK_Binary)) {
 				// Use standard file opener
 				Dialogs.selectAndSetFileDialog(evt, btn_File1, txt_File1, "");
 				if (!txt_File1.getText().isEmpty()) {
 					File bimFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".bim");
 					File famFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".fam");
 					int option = -1;
 					if (txt_File2.getText().isEmpty() && bimFile.exists()) {
 						txt_File2.setText(bimFile.getPath());
 					} else if (bimFile.exists()) {
 						option = Dialogs.showConfirmDialogue(Text.Matrix.findComplementaryPlinkBinary);
 						if (option == JOptionPane.YES_OPTION) {
 							txt_File2.setText(bimFile.getPath());
 						}
 					}
 					if (txt_FileSampleInfo.getText().isEmpty() && famFile.exists()) {
 						txt_FileSampleInfo.setText(famFile.getPath());
 					} else if (famFile.exists()) {
 						if (option == JOptionPane.YES_OPTION) {
 							txt_FileSampleInfo.setText(famFile.getPath());
 						}
 					}
 				}
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.Sequenom)) {
 				// Use directory selector
 				Dialogs.selectAndSetDirectoryDialog(evt, btn_File1, txt_File1, "", ""); //only dirs
 			} else {
 				// Use standard file opener
 				Dialogs.selectAndSetFileDialog(evt, btn_File1, txt_File1, "");
 			}
 		}
 	}
 
 	private class Browse2Action extends AbstractAction { // FIXME make static
 
 		Browse2Action() {
 
 			setEnabled(false);
 			putValue(NAME, Text.All.browse);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			// Use standard file opener
 			if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK)) {
 				Dialogs.selectAndSetFileDialog(evt, btn_File2, txt_File2, "");
 				if (!txt_File2.getText().isEmpty()) {
 					File mapFile = new File(txt_File2.getText().substring(0, txt_File2.getText().length() - 4) + ".map");
 					if (txt_File1.getText().isEmpty() && mapFile.exists()) {
 						txt_File1.setText(mapFile.getPath());
 					} else if (mapFile.exists()) {
 						int option = Dialogs.showConfirmDialogue(Text.Matrix.findComplementaryPlink);
 						if (option == JOptionPane.YES_OPTION) {
 							txt_File1.setText(mapFile.getPath());
 						}
 					}
 				}
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.PLINK_Binary)) {
 				Dialogs.selectAndSetFileDialog(evt, btn_File2, txt_File2, "");
 				if (!txt_File2.getText().isEmpty()) {
 					File bedFile = new File(txt_File2.getText().substring(0, txt_File2.getText().length() - 4) + ".bed");
 					File famFile = new File(txt_File1.getText().substring(0, txt_File1.getText().length() - 4) + ".fam");
 					int option = -1;
 					if (txt_File1.getText().isEmpty() && bedFile.exists()) {
 						txt_File1.setText(bedFile.getPath());
 					} else if (bedFile.exists()) {
 						option = Dialogs.showConfirmDialogue(Text.Matrix.findComplementaryPlinkBinary);
 						if (option == JOptionPane.YES_OPTION) {
 							txt_File1.setText(bedFile.getPath());
 						}
 					}
 					if (txt_FileSampleInfo.getText().isEmpty() && famFile.exists()) {
 						txt_FileSampleInfo.setText(famFile.getPath());
 					} else if (famFile.exists()) {
 						if (option == JOptionPane.YES_OPTION) {
 							txt_FileSampleInfo.setText(famFile.getPath());
 						}
 					}
 				}
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.BEAGLE)) {
 				Dialogs.selectAndSetFileDialog(evt, btn_File2, txt_File2, "");
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.HGDP1)) {
 				Dialogs.selectAndSetFileDialog(evt, btn_File2, txt_File2, "");
 			} else if (cmb_Format.getSelectedItem().equals(cImport.ImportFormat.Sequenom)) {
 				Dialogs.selectAndSetFileDialog(evt, btn_File2, txt_File2, "");
 			} else {
 				Dialogs.selectAndSetDirectoryDialog(evt, btn_File2, txt_File2, "", ""); //only dirs
 			}
 		}
 	}
 
 	private class BrowseSampleInfoAction extends AbstractAction { // FIXME make static
 
 		BrowseSampleInfoAction() {
 
 			setEnabled(false);
 			putValue(NAME, Text.All.browse);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			// Use standard file opener
 			Dialogs.selectAndSetFileDialog(evt, btn_FileSampleInfo, txt_FileSampleInfo, "");
 		}
 	}
 
 	private boolean[] validateFiles() {
 		lbl_File1.setForeground(Color.black);
 		lbl_File2.setForeground(Color.black);
 		lbl_FileSampleInfo.setForeground(Color.black);
 
 		boolean[] buttonsOK = new boolean[]{false, false, false};
 
 		File file1 = new File(txt_File1.getText());
 		File file2 = new File(txt_File2.getText());
 		File file3 = new File(txt_FileSampleInfo.getText());
 
 		if (txt_File1.isEnabled()) {
 			if (file1.exists()) {
 				buttonsOK[0] = true;
 			} else if (!fieldObligatoryState[0]) {
 				if (txt_File1.getText().contains(Text.All.optional)
 						|| txt_File1.getText().isEmpty()) {
 					buttonsOK[0] = true;
 				}
 			} else {
 				buttonsOK[0] = false;
 			}
 		} else {
 			buttonsOK[0] = true;
 		}
 		if (txt_File2.isEnabled()) {
 			if (file2.exists()) {
 				buttonsOK[1] = true;
 			} else if (!fieldObligatoryState[1]) {
 				if (txt_File2.getText().contains(Text.All.optional)
 						|| txt_File2.getText().isEmpty())
 				{
 					buttonsOK[1] = true;
 				}
 			} else {
 				buttonsOK[1] = false;
 			}
 		} else {
 			buttonsOK[1] = true;
 		}
 		if (txt_FileSampleInfo.isEnabled()) {
 			if (file3.exists()) {
 				buttonsOK[2] = true;
 			} else if (!fieldObligatoryState[2]) {
 				if (txt_FileSampleInfo.getText().contains(Text.All.optional)
 						|| txt_FileSampleInfo.getText().isEmpty())
 				{
 					buttonsOK[2] = true;
 				}
 			} else {
 				buttonsOK[2] = false;
 			}
 		} else {
 			buttonsOK[2] = true;
 		}
 
 		return buttonsOK;
 	}
 	//</editor-fold>
 
 	private static class BackAction extends AbstractAction {
 
 		private int studyId;
 
 		BackAction(int studyId) {
 
 			this.studyId = studyId;
 			putValue(NAME, Text.All.Back);
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent evt) {
 			try {
 				GWASpiExplorerPanel.getSingleton().setPnl_Content(new CurrentStudyPanel(studyId));
 				GWASpiExplorerPanel.getSingleton().getScrl_Content().setViewportView(GWASpiExplorerPanel.getSingleton().getPnl_Content());
 			} catch (IOException ex) {
 				log.error(null, ex);
 			}
 		}
 	}
 }
