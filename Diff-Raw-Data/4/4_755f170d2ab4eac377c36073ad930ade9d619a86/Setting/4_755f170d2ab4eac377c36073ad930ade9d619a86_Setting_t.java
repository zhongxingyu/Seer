 /*
  * Movie Renamer
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.ui;
 
 import fr.free.movierenamer.Main;
 import fr.free.movierenamer.ui.res.ContextMenuFieldMouseListener;
 import fr.free.movierenamer.utils.Settings;
 import fr.free.movierenamer.utils.Utils;
 import fr.free.movierenamer.utils.Utils.CaseConversionType;
 import fr.free.movierenamer.worker.WorkerManager;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.beans.PropertyChangeSupport;
 import java.io.File;
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 import java.util.logging.Level;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  * Class Setting , Setting dialog
  *
  * @author Nicolas Magré
  */
 public class Setting extends JDialog {
 
   private Settings setting;
   private PropertyChangeSupport settingsChange;
   private String[] extensions;
   private List<String> filters;
   private int currentExtensionIndex;
   private int currentFilterIndex;
   private JRadioButton[] rBtnThumbList;
   private JRadioButton[] rBtnFanartList;
   private JRadioButton[] rBtnCase;
   private JRadioButton[] rBtnFolderCase;
   private JRadioButton[] rBtnScrapper;
   private String[][] format = {
     {"<t>", "Matrix"}, {"<ot>", "The Matrix"}, {"<y>", "1999"}, {"<tt>", "tt0133093"},
     {"<a>", "Keanu Reeves | Laurence Fishburne | Carrie-Anne Moss | Hugo Weaving | Gloria Foster"},
     {"<a1>", "Keanu Reeves"}, {"<a2>", "Laurence Fishburne"}, {"<a3>", "Carrie-Anne Moss"}, {"<a4>", "Hugo Weaving"}, {"<a5>", "Gloria Foster"},
     {"<g>", "Action | Adventure | Sci-Fi"}, {"<g1>", "Action"}, {"<g2>", "Adventure"}, {"<g3>", "Sci-Fi"},
     {"<d>", "Andy Wachowski | Lana Wachowski"}, {"<d1>", "Andy Wachowski"}, {"<d2>", "Lana Wachowski"},
     {"<c>", "USA | Australia"}, {"<c1>", "USA"}, {"<c2>", "Australia"}, {"<rt>", "136"}, {"<ra>", "8.8"},
     {"<mrt>", "2h 16mn"}, {"<mfs>", "7.179 GiB"}, {"<mc>", "DivX"}, {"<mdc>", "HD"}, {"<mf>", "720p"}, {"<mfr>", "23.976"}, {"<mr>", "1280x720"},
     {"<mcf>", "mkv"}, {"<mach>", "6ch | 6ch"}, {"<mach1>", "6ch"}, {"<mach2>", "6ch"}, {"<mac>", "DTS | DTS"}, {"<mac1>", "DTS"}, {"<mac2>", "DTS"},
     {"<mal>", "english | french"}, {"<mal1>", "english"}, {"<mal2>", "french"}, {"<matt>", "English DTS 1509kbps | French DTS 755kbps"},
     {"<matt1>", "English DTS 1509kbps"}, {"<matt2>", "French DTS 755kbps"},
     {"<mtt>", "English Forced | English | French Forced,"}, {"<mtt1>", "English Forced"}, {"<mtt2>", "English"}, {"<mtt3>", "French Forced,"}
   };
 
   /**
    * Creates new form Setting
    *
    * @param setting Movie Renamer Settings
    * @param settingsChange Settings property change
    * @param parent Parent to center on
    */
   public Setting(Settings setting, PropertyChangeSupport settingsChange, Component parent) {
     this.settingsChange = settingsChange;
     setIconImage(Utils.getImageFromJAR("/image/icon-32.png", getClass()));
     initComponents();
 
     rBtnThumbList = new JRadioButton[]{this.origThumbSizeRBtn, this.midThumbSizeRBtn, this.thumbThumbSizeRBtn};
     rBtnFanartList = new JRadioButton[]{this.origFanartSizeRBtn, this.midFanartSizeRBtn, this.thumbFanartSizeRBtn};
     rBtnCase = new JRadioButton[]{this.firstLoRbtn, this.firstLaRbtn, this.upperRbtn, this.lowerRbtn, this.noneRbtn};
     rBtnFolderCase = new JRadioButton[]{this.firstLoFolderRbtn, this.firstLaFolderRbtn, this.upperFolderRbtn, this.lowerFolderRbtn, this.noneFolderRbtn};
     rBtnScrapper = new JRadioButton[]{this.imdbRBtn, this.tmdbRbtn, this.allocineRbtn};
     this.setting = setting;
     extensions = setting.extensions;
    filters = new ArrayList<String>(setting.mediaNameFilters);// Fixed ref for equals method
 
     extentionJlist.addListSelectionListener(new ListSelectionListener() {
 
       @Override
       public void valueChanged(ListSelectionEvent lse) {
         if (extentionJlist.getSelectedIndex() != -1) {
           currentExtensionIndex = extentionJlist.getSelectedIndex();
         }
       }
     });
     loadList(extentionJlist, extensions);
     currentExtensionIndex = 0;
 
     filterJlist.addListSelectionListener(new ListSelectionListener() {
 
       @Override
       public void valueChanged(ListSelectionEvent lse) {
         if (filterJlist.getSelectedIndex() != -1) {
           currentFilterIndex = filterJlist.getSelectedIndex();
           if (currentFilterIndex == 0) {
             moveLeft.setEnabled(false);
           } else {
             moveLeft.setEnabled(true);
           }
           if (currentFilterIndex == (filters.size() - 1)) {
             moveRight.setEnabled(false);
           } else {
             moveRight.setEnabled(true);
           }
         } else {
           moveRight.setEnabled(false);
           moveLeft.setEnabled(false);
         }
       }
     });
 
     loadList(filterJlist, filters.toArray(new String[filters.size()]));
     currentFilterIndex = 0;
 
     // General Setting
     selectFirstMovieChk.setSelected(setting.selectFrstMedia);
     selectFirstResChk.setSelected(setting.selectFrstRes);
     showNotaMovieWarnChk.setSelected(setting.showNotaMovieWarn);
     scanSubfolderChk.setSelected(setting.scanSubfolder);
     useExtensionFilterChk.setSelected(setting.useExtensionFilter);
     movieInfoPanelChk.setSelected(setting.movieInfoPanel);
     actorImageChk.setSelected(setting.actorImage);
     thumbsChk.setSelected(setting.thumb);
     fanartsChk.setSelected(setting.fanart);
     autoSearchChk.setSelected(setting.autoSearchMedia);
     checkUpdateChk.setSelected(setting.checkUpdate);
 
     englishRbtn.setSelected(!setting.locale.equals("fr"));
     frenchRbtn.setSelected(setting.locale.equals("fr"));
 
     for (int i = 0; i < Settings.lookAndFeels.length; i++) {
       if (Settings.lookAndFeels[i].getName().equals(setting.laf)) {
         lafCmbBox.setSelectedIndex(i);
       }
     }
 
     xbmcNFORBtn.setSelected(setting.nfoType == 0);
     mediaPortalNFORBtn.setSelected(setting.nfoType == 1);
 
     // Rename Setting
     formatField.setText(setting.movieFilenameFormat);
     if (setting.movieFilenameCase.ordinal() >= caseGroup.getButtonCount()) {
       caseGroup.setSelected(rBtnCase[1].getModel(), true);
     } else {
       caseGroup.setSelected(rBtnCase[setting.movieFilenameCase.ordinal()].getModel(), true);
     }
     if (setting.movieFolderCase >= caseFolderGroup.getButtonCount()) {
       caseFolderGroup.setSelected(rBtnFolderCase[1].getModel(), true);
     } else {
       caseFolderGroup.setSelected(rBtnFolderCase[setting.movieFolderCase].getModel(), true);
     }
 
     separatorField.setText(setting.movieFilenameSeparator);
     limitField.setText("" + setting.movieFilenameLimit);
     rmSpcCharChk.setSelected(setting.movieFilenameTrim);
     rmDupSpaceChk.setSelected(setting.movieFilenameRmDupSpace);
 
     createDirChk.setSelected(setting.movieFilenameCreateDirectory);
     formatFolderField.setText(setting.movieFolderFormat);
     separatorFolderField.setText(setting.movieFolderSeparator);
     limitFolderField.setText("" + setting.movieFolderLimit);
     rmSpcCharFolderChk.setSelected(setting.movieFolderTrim);
     rmDupSpaceFolderChk.setSelected(setting.movieFolderRmDupSpace);
 
     // Search
     displayAppResultCheckBox.setSelected(setting.displayApproximateResult);
     scrapperFrRbtn.setSelected(setting.movieScrapperFR);
     scrapperEnRbtn.setSelected(!setting.movieScrapperFR);
     displayThumbResultChk.setSelected(setting.displayThumbResult);
     scrapperGroup.setSelected(rBtnScrapper[setting.movieScrapper.ordinal()].getModel(), true);
     sortbySimiChk.setSelected(setting.sortBySimiYear);
     limitResultComboBox.setSelectedIndex(setting.nbResult);
 
     //Image
     thumbGroup.setSelected(rBtnThumbList[setting.thumbSize].getModel(), true);
     fanartGroup.setSelected(rBtnFanartList[setting.fanartSize].getModel(), true);
     thumbExtCbBox.setSelectedIndex(setting.thumbExt);
 
     //Cache
     clearXmlCacheOnStartChk.setSelected(setting.clearXMLCache);
 
     String ssize;
     long size = Utils.getDirSizeInMegabytes(new File(Settings.thumbCacheDir));
     ssize = "" + size;
     if (size == 0) {
       ssize = "0." + Utils.getDirSize(new File(Settings.thumbCacheDir));
     }
     thumbCacheLbl.setText(ssize + Utils.i18n("useForThumb"));
 
     size = Utils.getDirSizeInMegabytes(new File(Settings.fanartCacheDir));
     ssize = "" + size;
     if (size == 0) {
       ssize = "0." + Utils.getDirSize(new File(Settings.fanartCacheDir));
     }
     fanartCacheLbl.setText(ssize + Utils.i18n("useForFanart"));
 
     size = Utils.getDirSizeInMegabytes(new File(Settings.actorCacheDir));
     ssize = "" + size;
     if (size == 0) {
       ssize = "0." + Utils.getDirSize(new File(Settings.actorCacheDir));
     }
     actorCacheLbl.setText(ssize + Utils.i18n("useForActor"));
 
     size = Utils.getDirSizeInMegabytes(new File(Settings.xmlCacheDir));
     ssize = "" + size;
     if (size == 0) {
       ssize = "0." + Utils.getDirSize(new File(Settings.xmlCacheDir));
     }
     xmlLbl.setText(ssize + Utils.i18n("useForXml"));
 
     // Proxy
     useProxyChk.setSelected(setting.useProxy);
     proxyUrlField.setText(setting.proxyUrl);
     proxyPortField.setText("" + setting.proxyPort);
 
     setTitle("Movie Renamer Settings " + setting.getVersion());
     setModal(true);
     setLocationRelativeTo(parent);
   }
 
   private void loadList(JList jlist, String[] array) {
     jlist.setListData(array);
     jlist.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
     jlist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
     jlist.setVisibleRowCount(-1);
     jlist.setSelectedIndex(0);
   }
 
   public Settings getSetting() {
     return setting;
   }
 
   private String movieRenamerTest(String nameFormat, int limit, Utils.CaseConversionType casse, String separator, boolean trim, boolean rmDupSpc, boolean extension) {
     String ext = "avi";
 
     for (int i = 0; i < format.length; i++) {
       if (limit > 0) {
         if (format[i][0].equals("<a>") || format[i][0].equals("<d>") || format[i][0].equals("<c>") || format[i][0].equals("<g>")
                 || format[i][0].equals("<mach>") || format[i][0].equals("<mac>") || format[i][0].equals("<mal>") || format[i][0].equals("<matt>")
                 || format[i][0].equals("<mat>") || format[i][0].equals("<mtt>")) {
           String[] tmp = format[i][1].split(" \\| ");
           String replace = "";
           for (int j = 0; j < tmp.length; j++) {
             if (j < limit) {
               replace += tmp[j].trim() + ((j + 1 < limit && (j + 1) < tmp.length) ? " | " : "");
             }
           }
           nameFormat = nameFormat.replaceAll(format[i][0], replace);
         } else {
           nameFormat = nameFormat.replaceAll(format[i][0], format[i][1]);
         }
       } else {
         nameFormat = nameFormat.replaceAll(format[i][0], format[i][1]);
       }
     }
 
     nameFormat = nameFormat.replace(" | ", separator);
 
     if (trim) {
       nameFormat = nameFormat.trim();
     }
 
     switch (casse) {
       case UPPER:
         nameFormat = nameFormat.toUpperCase() + (extension ? "." + ext.toUpperCase() : "");
         break;
       case LOWER:
         nameFormat = nameFormat.toLowerCase() + (extension ? "." + ext.toLowerCase() : "");
         break;
       case FIRSTLO:
         nameFormat = Utils.capitalizedLetter(nameFormat, true) + (extension ? "." + ext.toLowerCase() : "");
         break;
       case FIRSTLA:
         nameFormat = Utils.capitalizedLetter(nameFormat, false) + (extension ? "." + ext.toLowerCase() : "");
         break;
       default:
         nameFormat += (extension ? "." + ext.toLowerCase() : "");
         break;
     }
 
     if (rmDupSpc) {
       nameFormat = nameFormat.replaceAll("\\s+", " ");
     }
 
     return nameFormat;
   }
 
   /**
    * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
    */
   @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         thumbGroup = new ButtonGroup();
         fanartGroup = new ButtonGroup();
         languageGroup = new ButtonGroup();
         imdbLangGroup = new ButtonGroup();
         caseGroup = new ButtonGroup();
         nfoGroup = new ButtonGroup();
         scrapperGroup = new ButtonGroup();
         caseFolderGroup = new ButtonGroup();
         settingTabPan = new JTabbedPane();
         generalPnl = new JPanel();
         languagePnl = new JPanel();
         englishRbtn = new JRadioButton();
         frenchRbtn = new JRadioButton();
         lwarningLbl = new JLabel();
         updatePnl = new JPanel();
         checkUpdateChk = new JCheckBox();
         interfacePnl = new JPanel();
         selectFirstMovieChk = new JCheckBox();
         actorImageChk = new JCheckBox();
         movieInfoPanelChk = new JCheckBox();
         thumbsChk = new JCheckBox();
         fanartsChk = new JCheckBox();
         scanSubfolderChk = new JCheckBox();
         showNotaMovieWarnChk = new JCheckBox();
         lafLbl = new JLabel();
     String[] cmbmd = new String[Settings.lookAndFeels.length];
     for (int i=0; i < Settings.lookAndFeels.length; i++) {
       cmbmd[i] = Settings.lookAndFeels[i].getName();
     }
         lafCmbBox = new JComboBox(cmbmd);
         nfoPnl = new JPanel();
         xbmcNFORBtn = new JRadioButton();
         mediaPortalNFORBtn = new JRadioButton();
         renamePnl = new JPanel();
         renameTabPan = new JTabbedPane();
         movieFileNamePnl = new JPanel();
         defaultFormatLbl = new JLabel();
         filenameTestBtn = new JButton();
         filenameTestField = new JTextField();
         formatField = new JTextField();
         formatLbl = new JLabel();
         helpBtn = new JButton();
         firstLoRbtn = new JRadioButton();
         firstLaRbtn = new JRadioButton();
         upperRbtn = new JRadioButton();
         lowerRbtn = new JRadioButton();
         caseLbl = new JLabel();
         separatorLbl = new JLabel();
         separatorField = new JTextField();
         limitLbl = new JLabel();
         limitField = new JTextField();
         rmSpcCharChk = new JCheckBox();
         rmDupSpaceChk = new JCheckBox();
         noneRbtn = new JRadioButton();
         movieFolderTabPan = new JPanel();
         defaultFormatFolderLbl = new JLabel();
         formatFolderField = new JTextField();
         formatFolderLbl = new JLabel();
         separatorFolderLbl = new JLabel();
         separatorFolderField = new JTextField();
         limitFolderField = new JTextField();
         limitLbl1 = new JLabel();
         helpBtn1 = new JButton();
         caseFolderLbl = new JLabel();
         noneFolderRbtn = new JRadioButton();
         firstLoFolderRbtn = new JRadioButton();
         firstLaFolderRbtn = new JRadioButton();
         upperFolderRbtn = new JRadioButton();
         lowerFolderRbtn = new JRadioButton();
         rmSpcCharFolderChk = new JCheckBox();
         rmDupSpaceFolderChk = new JCheckBox();
         folderTestBtn = new JButton();
         folderTestField = new JTextField();
         createDirChk = new JCheckBox();
         movieFileNamePnl1 = new JPanel();
         SearchPnl = new JPanel();
         jTabbedPane2 = new JTabbedPane();
         jPanel6 = new JPanel();
         imdbSearchPnl = new JPanel();
         displayAppResultCheckBox = new JCheckBox();
         limitResultComboBox = new JComboBox();
         limitResultLbl = new JLabel();
         selectFirstResChk = new JCheckBox();
         autoSearchChk = new JCheckBox();
         displayThumbResultChk = new JCheckBox();
         resultHelp = new JButton();
         sortbySimiChk = new JCheckBox();
         jPanel3 = new JPanel();
         imdbRBtn = new JRadioButton();
         tmdbRbtn = new JRadioButton();
         allocineRbtn = new JRadioButton();
         jPanel2 = new JPanel();
         scrapperFrRbtn = new JRadioButton();
         scrapperEnRbtn = new JRadioButton();
         jPanel7 = new JPanel();
         jPanel5 = new JPanel();
         imdbRBtn1 = new JRadioButton();
         tmdbRbtn1 = new JRadioButton();
         allocineRbtn1 = new JRadioButton();
         jPanel8 = new JPanel();
         scrapperFrRbtn1 = new JRadioButton();
         scrapperEnRbtn1 = new JRadioButton();
         jPanel1 = new JPanel();
         movieImagePnl = new JPanel();
         thumbExtCbBox = new JComboBox();
         thumnailsExtLbl = new JLabel();
         imagesPnl = new JPanel();
         thumbThumbSizeRBtn = new JRadioButton();
         midThumbSizeRBtn = new JRadioButton();
         origThumbSizeRBtn = new JRadioButton();
         thumbSzeLbl = new JLabel();
         fanartSizeLbl = new JLabel();
         origFanartSizeRBtn = new JRadioButton();
         midFanartSizeRBtn = new JRadioButton();
         thumbFanartSizeRBtn = new JRadioButton();
         filtersPnl = new JPanel();
         extensionPnl = new JPanel();
         removeExtensuionBtn = new JButton();
         addExtensionBtn = new JButton();
         extensionScrollP = new JScrollPane();
         extentionJlist = new JList();
         extensionHelp = new JButton();
         useExtensionFilterChk = new JCheckBox();
         fileNameFilterPnl = new JPanel();
         moveLeft = new JButton();
         moveRight = new JButton();
         addFilter = new JButton();
         removeFilterBtn = new JButton();
         filterScrollP = new JScrollPane();
         filterJlist = new JList();
         filenameFilterHelp = new JButton();
         resetNameFilter = new JButton();
         cachePnl = new JPanel();
         imagePnl = new JPanel();
         actorCacheLbl = new JLabel();
         fanartCacheLbl = new JLabel();
         thumbCacheLbl = new JLabel();
         clearThumbBtn = new JButton();
         clearFanartBtn = new JButton();
         clearActorBtn = new JButton();
         xmlFilePnl = new JPanel();
         xmlLbl = new JLabel();
         clearXmlBtn = new JButton();
         clearXmlCacheOnStartChk = new JCheckBox();
         proxyPanel = new JPanel();
         proxySubPanel = new JPanel();
         useProxyChk = new JCheckBox();
         jLabel1 = new JLabel();
         proxyUrlField = new JTextField();
         jLabel2 = new JLabel();
         proxyPortField = new JTextField();
         saveBtn = new JButton();
         CancelBtn = new JButton();
 
         setTitle("Movie Renamer Settings");
         setResizable(false);
 
         generalPnl.setFont(new Font("Ubuntu", 1, 14)); ResourceBundle bundle = ResourceBundle.getBundle("fr/free/movierenamer/i18n/Bundle"); // NOI18N
         languagePnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("language"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14)));         languagePnl.setToolTipText("Under development");
 
         languageGroup.add(englishRbtn);
         englishRbtn.setFont(new Font("Ubuntu", 0, 12));         englishRbtn.setSelected(true);
         englishRbtn.setText(bundle.getString("english")); // NOI18N
 
         languageGroup.add(frenchRbtn);
         frenchRbtn.setFont(new Font("Ubuntu", 0, 12));         frenchRbtn.setText(bundle.getString("french")); // NOI18N
 
         lwarningLbl.setFont(new Font("Ubuntu", 1, 12));         lwarningLbl.setIcon(new ImageIcon(getClass().getResource("/image/dialog-warning.png")));         lwarningLbl.setText(bundle.getString("needRestart")); // NOI18N
 
         GroupLayout languagePnlLayout = new GroupLayout(languagePnl);
         languagePnl.setLayout(languagePnlLayout);
         languagePnlLayout.setHorizontalGroup(
             languagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(languagePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(englishRbtn)
                 .addGap(18, 18, 18)
                 .addComponent(frenchRbtn)
                 .addPreferredGap(ComponentPlacement.RELATED, 193, Short.MAX_VALUE)
                 .addComponent(lwarningLbl)
                 .addContainerGap())
         );
         languagePnlLayout.setVerticalGroup(
             languagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(languagePnlLayout.createSequentialGroup()
                 .addGroup(languagePnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(englishRbtn)
                     .addComponent(frenchRbtn)
                     .addComponent(lwarningLbl))
                 .addContainerGap())
         );
 
         updatePnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("update"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         checkUpdateChk.setFont(new Font("Ubuntu", 0, 12));         checkUpdateChk.setText(bundle.getString("chkUpdateOnStart")); // NOI18N
         checkUpdateChk.setToolTipText(bundle.getString("chkUpdateOnStartTt")); // NOI18N
 
         GroupLayout updatePnlLayout = new GroupLayout(updatePnl);
         updatePnl.setLayout(updatePnlLayout);
         updatePnlLayout.setHorizontalGroup(
             updatePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(updatePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(checkUpdateChk)
                 .addContainerGap(261, Short.MAX_VALUE))
         );
         updatePnlLayout.setVerticalGroup(
             updatePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(updatePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(checkUpdateChk)
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         interfacePnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("interface"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         selectFirstMovieChk.setFont(new Font("Ubuntu", 0, 12));         selectFirstMovieChk.setText(bundle.getString("autoSelFrstMovie")); // NOI18N
         selectFirstMovieChk.setToolTipText(bundle.getString("autoSelFrstMovieTt")); // NOI18N
 
         actorImageChk.setFont(new Font("Ubuntu", 0, 12));         actorImageChk.setText(bundle.getString("showActorImage")); // NOI18N
         actorImageChk.setToolTipText(bundle.getString("showActorImageTt")); // NOI18N
         actorImageChk.setEnabled(false);
 
         movieInfoPanelChk.setFont(new Font("Ubuntu", 0, 12));         movieInfoPanelChk.setText(bundle.getString("showMoviePanel")); // NOI18N
         movieInfoPanelChk.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 movieInfoPanelChkItemStateChanged(evt);
             }
         });
 
         thumbsChk.setFont(new Font("Ubuntu", 0, 12));         thumbsChk.setText(bundle.getString("showThumbs")); // NOI18N
         thumbsChk.setToolTipText(bundle.getString("showThumbsTt")); // NOI18N
         thumbsChk.setEnabled(false);
 
         fanartsChk.setFont(new Font("Ubuntu", 0, 12));         fanartsChk.setText(bundle.getString("showFanarts")); // NOI18N
         fanartsChk.setToolTipText(bundle.getString("showFanartsTt")); // NOI18N
         fanartsChk.setEnabled(false);
 
         scanSubfolderChk.setFont(new Font("Ubuntu", 0, 12));         scanSubfolderChk.setText(bundle.getString("autoScanSubfolder")); // NOI18N
         scanSubfolderChk.setToolTipText(bundle.getString("autoScanSubfolderTt")); // NOI18N
 
         showNotaMovieWarnChk.setFont(new Font("Ubuntu", 0, 12));         showNotaMovieWarnChk.setText(bundle.getString("showNotMovieWarn")); // NOI18N
         showNotaMovieWarnChk.setToolTipText(bundle.getString("showNotMovieWarnTt")); // NOI18N
 
         lafLbl.setFont(new Font("Dialog", 1, 12));         lafLbl.setText("Look and Feel");
 
         lafCmbBox.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 lafCmbBoxActionPerformed(evt);
             }
         });
 
         GroupLayout interfacePnlLayout = new GroupLayout(interfacePnl);
         interfacePnl.setLayout(interfacePnlLayout);
         interfacePnlLayout.setHorizontalGroup(
             interfacePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(interfacePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(interfacePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(interfacePnlLayout.createSequentialGroup()
                         .addComponent(lafLbl)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(lafCmbBox, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE))
                     .addComponent(selectFirstMovieChk)
                     .addComponent(scanSubfolderChk)
                     .addComponent(showNotaMovieWarnChk)
                     .addComponent(movieInfoPanelChk)
                     .addGroup(interfacePnlLayout.createSequentialGroup()
                         .addGap(21, 21, 21)
                         .addGroup(interfacePnlLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(thumbsChk)
                             .addComponent(actorImageChk)
                             .addComponent(fanartsChk))))
                 .addContainerGap(177, Short.MAX_VALUE))
         );
         interfacePnlLayout.setVerticalGroup(
             interfacePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(interfacePnlLayout.createSequentialGroup()
                 .addComponent(selectFirstMovieChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(scanSubfolderChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(showNotaMovieWarnChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(movieInfoPanelChk)
                 .addPreferredGap(ComponentPlacement.UNRELATED)
                 .addComponent(actorImageChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(thumbsChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(fanartsChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(interfacePnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(lafLbl)
                     .addComponent(lafCmbBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(19, Short.MAX_VALUE))
         );
 
         nfoPnl.setBorder(BorderFactory.createTitledBorder(null, "NFO", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         nfoGroup.add(xbmcNFORBtn);
         xbmcNFORBtn.setFont(new Font("Ubuntu", 0, 12));         xbmcNFORBtn.setText(bundle.getString("nfoXbmc")); // NOI18N
 
         nfoGroup.add(mediaPortalNFORBtn);
         mediaPortalNFORBtn.setFont(new Font("Ubuntu", 0, 12));         mediaPortalNFORBtn.setText(bundle.getString("nfoMediaPortal")); // NOI18N
 
         GroupLayout nfoPnlLayout = new GroupLayout(nfoPnl);
         nfoPnl.setLayout(nfoPnlLayout);
         nfoPnlLayout.setHorizontalGroup(
             nfoPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(nfoPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(xbmcNFORBtn)
                 .addGap(18, 18, 18)
                 .addComponent(mediaPortalNFORBtn)
                 .addContainerGap(233, Short.MAX_VALUE))
         );
         nfoPnlLayout.setVerticalGroup(
             nfoPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(nfoPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(nfoPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(xbmcNFORBtn)
                     .addComponent(mediaPortalNFORBtn))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         GroupLayout generalPnlLayout = new GroupLayout(generalPnl);
         generalPnl.setLayout(generalPnlLayout);
         generalPnlLayout.setHorizontalGroup(
             generalPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(generalPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(generalPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(interfacePnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(nfoPnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(updatePnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(languagePnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         generalPnlLayout.setVerticalGroup(
             generalPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(generalPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(interfacePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(nfoPnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(updatePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(languagePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(39, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab(bundle.getString("general"), generalPnl); // NOI18N
 
         renamePnl.setFont(new Font("Ubuntu", 1, 14)); 
         movieFileNamePnl.setBorder(null);
 
         defaultFormatLbl.setFont(new Font("Ubuntu", 1, 12));         defaultFormatLbl.setText(bundle.getString("defaultFormat")); // NOI18N
 
         filenameTestBtn.setFont(new Font("Ubuntu", 1, 12));         filenameTestBtn.setText("test");
         filenameTestBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 filenameTestBtnActionPerformed(evt);
             }
         });
 
         filenameTestField.setEditable(false);
 
         formatField.setFont(new Font("Ubuntu", 0, 12));         formatField.setText("<t> (<y>)");
         formatField.addMouseListener(new ContextMenuFieldMouseListener());
 
         formatLbl.setFont(new Font("Ubuntu", 1, 12));         formatLbl.setText("Format");
 
         helpBtn.setIcon(new ImageIcon(getClass().getResource("/image/system-help-3.png")));         helpBtn.setToolTipText(bundle.getString("help")); // NOI18N
         helpBtn.setMaximumSize(new Dimension(26, 26));
         helpBtn.setMinimumSize(new Dimension(26, 26));
         helpBtn.setPreferredSize(new Dimension(26, 26));
         helpBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 helpBtnActionPerformed(evt);
             }
         });
 
         caseGroup.add(firstLoRbtn);
         firstLoRbtn.setFont(new Font("Ubuntu", 0, 12));         firstLoRbtn.setText(bundle.getString("firstLo")); // NOI18N
 
         caseGroup.add(firstLaRbtn);
         firstLaRbtn.setFont(new Font("Ubuntu", 0, 12));         firstLaRbtn.setText(bundle.getString("firstLa")); // NOI18N
 
         caseGroup.add(upperRbtn);
         upperRbtn.setFont(new Font("Ubuntu", 0, 12));         upperRbtn.setText(bundle.getString("upper")); // NOI18N
 
         caseGroup.add(lowerRbtn);
         lowerRbtn.setFont(new Font("Ubuntu", 0, 12));         lowerRbtn.setText(bundle.getString("lower")); // NOI18N
 
         caseLbl.setFont(new Font("Ubuntu", 1, 13));         caseLbl.setText(bundle.getString("fileCase")); // NOI18N
 
         separatorLbl.setFont(new Font("Ubuntu", 1, 12));         separatorLbl.setText(bundle.getString("separator")); // NOI18N
 
         separatorField.setFont(new Font("Ubuntu", 0, 12));         separatorField.setText(",");
 
         limitLbl.setText(bundle.getString("limit")); // NOI18N
 
         limitField.setText("0");
 
         rmSpcCharChk.setFont(new Font("Ubuntu", 0, 12));         rmSpcCharChk.setText(bundle.getString("rmSpcChar")); // NOI18N
 
         rmDupSpaceChk.setFont(new Font("Ubuntu", 0, 12));         rmDupSpaceChk.setText(bundle.getString("rmDupSpace")); // NOI18N
 
         caseGroup.add(noneRbtn);
         noneRbtn.setFont(new Font("Ubuntu", 0, 12));         noneRbtn.setText(bundle.getString("none")); // NOI18N
 
         GroupLayout movieFileNamePnlLayout = new GroupLayout(movieFileNamePnl);
         movieFileNamePnl.setLayout(movieFileNamePnlLayout);
         movieFileNamePnlLayout.setHorizontalGroup(
             movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                         .addComponent(filenameTestBtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(filenameTestField)
                         .addGap(33, 33, 33))
                     .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                         .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                             .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                                 .addComponent(separatorLbl)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(separatorField, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(ComponentPlacement.UNRELATED)
                                 .addComponent(limitLbl)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(limitField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
                             .addComponent(caseLbl))
                         .addContainerGap(210, Short.MAX_VALUE))
                     .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                         .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                             .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                                 .addGap(12, 12, 12)
                                 .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                                     .addComponent(noneRbtn)
                                     .addComponent(firstLaRbtn)
                                     .addComponent(firstLoRbtn)
                                     .addComponent(upperRbtn)
                                     .addComponent(lowerRbtn)))
                             .addComponent(rmDupSpaceChk)
                             .addComponent(rmSpcCharChk))
                         .addGap(0, 0, Short.MAX_VALUE))
                     .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                         .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                             .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                                 .addComponent(formatLbl)
                                 .addGap(28, 28, 28)
                                 .addComponent(formatField))
                             .addComponent(defaultFormatLbl))
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(helpBtn, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())))
         );
         movieFileNamePnlLayout.setVerticalGroup(
             movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(movieFileNamePnlLayout.createSequentialGroup()
                         .addComponent(defaultFormatLbl)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.BASELINE)
                             .addComponent(formatLbl)
                             .addComponent(formatField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.BASELINE)
                             .addComponent(separatorField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                             .addComponent(separatorLbl)
                             .addComponent(limitLbl)
                             .addComponent(limitField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                         .addGap(7, 7, 7)
                         .addComponent(caseLbl)
                         .addPreferredGap(ComponentPlacement.UNRELATED)
                         .addComponent(noneRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(firstLoRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(firstLaRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(upperRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(lowerRbtn)
                         .addGap(18, 18, 18)
                         .addComponent(rmSpcCharChk)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(rmDupSpaceChk))
                     .addComponent(helpBtn, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addGap(56, 56, 56)
                 .addGroup(movieFileNamePnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(filenameTestBtn)
                     .addComponent(filenameTestField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(38, Short.MAX_VALUE))
         );
 
         renameTabPan.addTab(bundle.getString("movieFileName"), movieFileNamePnl); // NOI18N
 
         defaultFormatFolderLbl.setFont(new Font("Ubuntu", 1, 12));         defaultFormatFolderLbl.setText(bundle.getString("defaultFormat")); // NOI18N
 
         formatFolderField.setFont(new Font("Ubuntu", 0, 12));         formatFolderField.setText("<t> (<y>)");
         formatField.addMouseListener(new ContextMenuFieldMouseListener());
 
         formatFolderLbl.setFont(new Font("Ubuntu", 1, 12));         formatFolderLbl.setText("Format");
 
         separatorFolderLbl.setFont(new Font("Ubuntu", 1, 12));         separatorFolderLbl.setText(bundle.getString("separator")); // NOI18N
 
         separatorFolderField.setFont(new Font("Ubuntu", 0, 12));         separatorFolderField.setText(",");
 
         limitFolderField.setText("0");
 
         limitLbl1.setFont(new Font("Ubuntu", 1, 12));         limitLbl1.setText(bundle.getString("limit")); // NOI18N
 
         helpBtn1.setIcon(new ImageIcon(getClass().getResource("/image/system-help-3.png")));         helpBtn1.setToolTipText(bundle.getString("help")); // NOI18N
         helpBtn1.setMaximumSize(new Dimension(26, 26));
         helpBtn1.setMinimumSize(new Dimension(26, 26));
         helpBtn1.setPreferredSize(new Dimension(26, 26));
         helpBtn1.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 helpBtn1ActionPerformed(evt);
             }
         });
 
         caseFolderLbl.setFont(new Font("Ubuntu", 1, 13));         caseFolderLbl.setText(bundle.getString("fileCase")); // NOI18N
 
         caseFolderGroup.add(noneFolderRbtn);
         noneFolderRbtn.setFont(new Font("Ubuntu", 0, 12));         noneFolderRbtn.setText(bundle.getString("none")); // NOI18N
 
         caseFolderGroup.add(firstLoFolderRbtn);
         firstLoFolderRbtn.setFont(new Font("Ubuntu", 0, 12));         firstLoFolderRbtn.setText(bundle.getString("firstLo")); // NOI18N
 
         caseFolderGroup.add(firstLaFolderRbtn);
         firstLaFolderRbtn.setFont(new Font("Ubuntu", 0, 12));         firstLaFolderRbtn.setText(bundle.getString("firstLa")); // NOI18N
 
         caseFolderGroup.add(upperFolderRbtn);
         upperFolderRbtn.setFont(new Font("Ubuntu", 0, 12));         upperFolderRbtn.setText(bundle.getString("upper")); // NOI18N
 
         caseFolderGroup.add(lowerFolderRbtn);
         lowerFolderRbtn.setFont(new Font("Ubuntu", 0, 12));         lowerFolderRbtn.setText(bundle.getString("lower")); // NOI18N
 
         rmSpcCharFolderChk.setFont(new Font("Ubuntu", 0, 12));         rmSpcCharFolderChk.setText(bundle.getString("rmSpcChar")); // NOI18N
 
         rmDupSpaceFolderChk.setFont(new Font("Ubuntu", 0, 12));         rmDupSpaceFolderChk.setText(bundle.getString("rmDupSpace")); // NOI18N
 
         folderTestBtn.setFont(new Font("Ubuntu", 1, 12));         folderTestBtn.setText("test");
         folderTestBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 folderTestBtnActionPerformed(evt);
             }
         });
 
         folderTestField.setEditable(false);
 
         createDirChk.setFont(new Font("Ubuntu", 0, 12));         createDirChk.setText(bundle.getString("createDir")); // NOI18N
 
         GroupLayout movieFolderTabPanLayout = new GroupLayout(movieFolderTabPan);
         movieFolderTabPan.setLayout(movieFolderTabPanLayout);
         movieFolderTabPanLayout.setHorizontalGroup(
             movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(Alignment.TRAILING, movieFolderTabPanLayout.createSequentialGroup()
                         .addComponent(formatFolderLbl)
                         .addGap(28, 28, 28)
                         .addComponent(formatFolderField)
                         .addGap(40, 40, 40))
                     .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                         .addComponent(folderTestBtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(folderTestField)
                         .addGap(33, 33, 33))
                     .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                         .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                             .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                                 .addComponent(createDirChk)
                                 .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .addComponent(helpBtn1, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                             .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                                 .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                                     .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                                         .addGap(12, 12, 12)
                                         .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                                             .addComponent(noneFolderRbtn)
                                             .addComponent(firstLaFolderRbtn)
                                             .addComponent(firstLoFolderRbtn)
                                             .addComponent(upperFolderRbtn)
                                             .addComponent(lowerFolderRbtn)))
                                     .addComponent(rmDupSpaceFolderChk)
                                     .addComponent(rmSpcCharFolderChk))
                                 .addGap(0, 198, Short.MAX_VALUE)))
                         .addContainerGap())
                     .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                         .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(defaultFormatFolderLbl)
                             .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                                 .addComponent(separatorFolderLbl)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(separatorFolderField, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(ComponentPlacement.UNRELATED)
                                 .addComponent(limitLbl1)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(limitFolderField, GroupLayout.PREFERRED_SIZE, 57, GroupLayout.PREFERRED_SIZE))
                             .addComponent(caseFolderLbl))
                         .addContainerGap())))
         );
         movieFolderTabPanLayout.setVerticalGroup(
             movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(movieFolderTabPanLayout.createSequentialGroup()
                         .addComponent(createDirChk)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(defaultFormatFolderLbl)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.BASELINE)
                             .addComponent(formatFolderLbl)
                             .addComponent(formatFolderField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.BASELINE)
                             .addComponent(separatorFolderField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                             .addComponent(separatorFolderLbl)
                             .addComponent(limitLbl1)
                             .addComponent(limitFolderField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                         .addGap(7, 7, 7)
                         .addComponent(caseFolderLbl)
                         .addPreferredGap(ComponentPlacement.UNRELATED)
                         .addComponent(noneFolderRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(firstLoFolderRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(firstLaFolderRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(upperFolderRbtn)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(lowerFolderRbtn)
                         .addGap(18, 18, 18)
                         .addComponent(rmSpcCharFolderChk)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(rmDupSpaceFolderChk))
                     .addComponent(helpBtn1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addGap(56, 56, 56)
                 .addGroup(movieFolderTabPanLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(folderTestBtn)
                     .addComponent(folderTestField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         renameTabPan.addTab(bundle.getString("movieFolder"), movieFolderTabPan); // NOI18N
 
         movieFileNamePnl1.setBorder(null);
 
         GroupLayout movieFileNamePnl1Layout = new GroupLayout(movieFileNamePnl1);
         movieFileNamePnl1.setLayout(movieFileNamePnl1Layout);
         movieFileNamePnl1Layout.setHorizontalGroup(
             movieFileNamePnl1Layout.createParallelGroup(Alignment.LEADING)
             .addGap(0, 469, Short.MAX_VALUE)
         );
         movieFileNamePnl1Layout.setVerticalGroup(
             movieFileNamePnl1Layout.createParallelGroup(Alignment.LEADING)
             .addGap(0, 429, Short.MAX_VALUE)
         );
 
         renameTabPan.addTab("Tv Show File", movieFileNamePnl1);
 
         GroupLayout renamePnlLayout = new GroupLayout(renamePnl);
         renamePnl.setLayout(renamePnlLayout);
         renamePnlLayout.setHorizontalGroup(
             renamePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(renamePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(renameTabPan)
                 .addContainerGap())
         );
         renamePnlLayout.setVerticalGroup(
             renamePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(renamePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(renameTabPan, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(65, Short.MAX_VALUE))
         );
 
         renameTabPan.getAccessibleContext().setAccessibleName(bundle.getString("movieFileName")); // NOI18N
 
         settingTabPan.addTab(bundle.getString("rename"), renamePnl); // NOI18N
 
         imdbSearchPnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("result"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         displayAppResultCheckBox.setFont(new Font("Ubuntu", 0, 12));         displayAppResultCheckBox.setText(bundle.getString("showAppRes")); // NOI18N
         displayAppResultCheckBox.setToolTipText(bundle.getString("showAppRestt")); // NOI18N
 
         limitResultComboBox.setFont(new Font("Ubuntu", 1, 12));         limitResultComboBox.setModel(new DefaultComboBoxModel(new String[] { bundle.getString("all"), "5", "10", "15", "20", "30" }));
 
         limitResultLbl.setFont(new Font("Ubuntu", 0, 12));         limitResultLbl.setText(bundle.getString("resForEachType")); // NOI18N
 
         selectFirstResChk.setFont(new Font("Ubuntu", 0, 12));         selectFirstResChk.setText(bundle.getString("autoSelFrstRes")); // NOI18N
         selectFirstResChk.setToolTipText(bundle.getString("autoSelFrstResTt")); // NOI18N
 
         autoSearchChk.setFont(new Font("Ubuntu", 0, 12));         autoSearchChk.setText(bundle.getString("autoSearch")); // NOI18N
         autoSearchChk.setToolTipText(bundle.getString("autoSearchTt")); // NOI18N
 
         displayThumbResultChk.setFont(new Font("Ubuntu", 0, 12));         displayThumbResultChk.setText(bundle.getString("showImgResList")); // NOI18N
 
         resultHelp.setIcon(new ImageIcon(getClass().getResource("/image/system-help-3.png")));         resultHelp.setToolTipText(bundle.getString("help")); // NOI18N
         resultHelp.setMaximumSize(new Dimension(26, 26));
         resultHelp.setMinimumSize(new Dimension(26, 26));
         resultHelp.setPreferredSize(new Dimension(26, 26));
         resultHelp.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 resultHelpActionPerformed(evt);
             }
         });
 
         sortbySimiChk.setFont(new Font("Ubuntu", 0, 12));         sortbySimiChk.setText(bundle.getString("sortResultBysimi")); // NOI18N
 
         GroupLayout imdbSearchPnlLayout = new GroupLayout(imdbSearchPnl);
         imdbSearchPnl.setLayout(imdbSearchPnlLayout);
         imdbSearchPnlLayout.setHorizontalGroup(
             imdbSearchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(imdbSearchPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                         .addComponent(displayThumbResultChk)
                         .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(resultHelp, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE))
                     .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                         .addGroup(imdbSearchPnlLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(displayAppResultCheckBox)
                             .addComponent(autoSearchChk)
                             .addComponent(selectFirstResChk)
                             .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                                 .addComponent(limitResultComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(ComponentPlacement.RELATED)
                                 .addComponent(limitResultLbl))
                             .addComponent(sortbySimiChk))
                         .addGap(0, 132, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         imdbSearchPnlLayout.setVerticalGroup(
             imdbSearchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(displayThumbResultChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(autoSearchChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(selectFirstResChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(sortbySimiChk)
                 .addPreferredGap(ComponentPlacement.UNRELATED)
                 .addGroup(imdbSearchPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(limitResultComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(limitResultLbl))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(displayAppResultCheckBox)
                 .addContainerGap())
             .addGroup(imdbSearchPnlLayout.createSequentialGroup()
                 .addComponent(resultHelp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, Short.MAX_VALUE))
         );
 
         jPanel3.setBorder(BorderFactory.createTitledBorder(null, "Scrapper", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         scrapperGroup.add(imdbRBtn);
         imdbRBtn.setText("Imdb");
         imdbRBtn.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 imdbRBtnItemStateChanged(evt);
             }
         });
 
         scrapperGroup.add(tmdbRbtn);
         tmdbRbtn.setText("Tmdb");
 
         scrapperGroup.add(allocineRbtn);
         allocineRbtn.setText("Allocine");
         allocineRbtn.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 allocineRbtnItemStateChanged(evt);
             }
         });
 
         GroupLayout jPanel3Layout = new GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(imdbRBtn)
                 .addGap(18, 18, 18)
                 .addComponent(tmdbRbtn)
                 .addGap(18, 18, 18)
                 .addComponent(allocineRbtn)
                 .addContainerGap(226, Short.MAX_VALUE))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel3Layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(imdbRBtn)
                     .addComponent(tmdbRbtn)
                     .addComponent(allocineRbtn))
                 .addGap(248, 248, 248))
         );
 
         jPanel2.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("language"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 13))); 
         imdbLangGroup.add(scrapperFrRbtn);
         scrapperFrRbtn.setFont(new Font("Ubuntu", 0, 12));         scrapperFrRbtn.setText(bundle.getString("imdbFr")); // NOI18N
 
         imdbLangGroup.add(scrapperEnRbtn);
         scrapperEnRbtn.setFont(new Font("Ubuntu", 0, 12));         scrapperEnRbtn.setText(bundle.getString("imdbEn")); // NOI18N
 
         GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrapperFrRbtn)
                 .addGap(18, 18, 18)
                 .addComponent(scrapperEnRbtn)
                 .addContainerGap(304, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(scrapperFrRbtn)
                     .addComponent(scrapperEnRbtn))
                 .addContainerGap(15, Short.MAX_VALUE))
         );
 
         GroupLayout jPanel6Layout = new GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel6Layout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(jPanel3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(imdbSearchPnl, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel6Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel3, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(imdbSearchPnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         jTabbedPane2.addTab("Movie", jPanel6);
 
         jPanel5.setBorder(BorderFactory.createTitledBorder(null, "Scrapper", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         scrapperGroup.add(imdbRBtn1);
         imdbRBtn1.setSelected(true);
         imdbRBtn1.setText("Tvdb");
         imdbRBtn1.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 imdbRBtn1ItemStateChanged(evt);
             }
         });
 
         scrapperGroup.add(tmdbRbtn1);
         tmdbRbtn1.setText("Tvrage");
 
         scrapperGroup.add(allocineRbtn1);
         allocineRbtn1.setText("Allocine");
         allocineRbtn1.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 allocineRbtn1ItemStateChanged(evt);
             }
         });
 
         GroupLayout jPanel5Layout = new GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(imdbRBtn1)
                 .addGap(18, 18, 18)
                 .addComponent(tmdbRbtn1)
                 .addGap(18, 18, 18)
                 .addComponent(allocineRbtn1)
                 .addContainerGap(221, Short.MAX_VALUE))
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel5Layout.createSequentialGroup()
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel5Layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(imdbRBtn1)
                     .addComponent(tmdbRbtn1)
                     .addComponent(allocineRbtn1))
                 .addGap(248, 248, 248))
         );
 
         jPanel8.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("language"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 13))); 
         imdbLangGroup.add(scrapperFrRbtn1);
         scrapperFrRbtn1.setFont(new Font("Ubuntu", 0, 12));         scrapperFrRbtn1.setSelected(true);
         scrapperFrRbtn1.setText(bundle.getString("imdbFr")); // NOI18N
 
         imdbLangGroup.add(scrapperEnRbtn1);
         scrapperEnRbtn1.setFont(new Font("Ubuntu", 0, 12));         scrapperEnRbtn1.setText(bundle.getString("imdbEn")); // NOI18N
 
         GroupLayout jPanel8Layout = new GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(scrapperFrRbtn1)
                 .addGap(18, 18, 18)
                 .addComponent(scrapperEnRbtn1)
                 .addContainerGap(304, Short.MAX_VALUE))
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel8Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel8Layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(scrapperFrRbtn1)
                     .addComponent(scrapperEnRbtn1))
                 .addContainerGap(15, Short.MAX_VALUE))
         );
 
         GroupLayout jPanel7Layout = new GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel7Layout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(jPanel5, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanel8, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel7Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanel5, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(jPanel8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(229, Short.MAX_VALUE))
         );
 
         jTabbedPane2.addTab("Tv Show", jPanel7);
 
         GroupLayout SearchPnlLayout = new GroupLayout(SearchPnl);
         SearchPnl.setLayout(SearchPnlLayout);
         SearchPnlLayout.setHorizontalGroup(
             SearchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addComponent(jTabbedPane2)
         );
         SearchPnlLayout.setVerticalGroup(
             SearchPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(SearchPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTabbedPane2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(107, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab(bundle.getString("searchTitle"), SearchPnl); // NOI18N
 
         movieImagePnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("imageExt"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         thumbExtCbBox.setModel(new DefaultComboBoxModel(new String[] { ".jpg", ".tbn", "-thumb.jpg" }));
 
         thumnailsExtLbl.setFont(new Font("Ubuntu", 1, 12));         thumnailsExtLbl.setText(bundle.getString("thumbnails")); // NOI18N
 
         GroupLayout movieImagePnlLayout = new GroupLayout(movieImagePnl);
         movieImagePnl.setLayout(movieImagePnlLayout);
         movieImagePnlLayout.setHorizontalGroup(
             movieImagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieImagePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(thumnailsExtLbl)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(thumbExtCbBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(267, Short.MAX_VALUE))
         );
         movieImagePnlLayout.setVerticalGroup(
             movieImagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(movieImagePnlLayout.createSequentialGroup()
                 .addGroup(movieImagePnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(thumnailsExtLbl)
                     .addComponent(thumbExtCbBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         imagesPnl.setBorder(BorderFactory.createTitledBorder(null, "Image", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", 1, 14))); 
         thumbGroup.add(thumbThumbSizeRBtn);
         thumbThumbSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         thumbThumbSizeRBtn.setText(bundle.getString("small")); // NOI18N
 
         thumbGroup.add(midThumbSizeRBtn);
         midThumbSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         midThumbSizeRBtn.setText(bundle.getString("medium")); // NOI18N
 
         thumbGroup.add(origThumbSizeRBtn);
         origThumbSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         origThumbSizeRBtn.setSelected(true);
         origThumbSizeRBtn.setText("Original");
 
         thumbSzeLbl.setFont(new Font("Ubuntu", 1, 13));         thumbSzeLbl.setText(bundle.getString("thumbsSize")); // NOI18N
 
         fanartSizeLbl.setFont(new Font("Ubuntu", 1, 13));         fanartSizeLbl.setText(bundle.getString("fanartsSize")); // NOI18N
 
         fanartGroup.add(origFanartSizeRBtn);
         origFanartSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         origFanartSizeRBtn.setSelected(true);
         origFanartSizeRBtn.setText("Original");
 
         fanartGroup.add(midFanartSizeRBtn);
         midFanartSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         midFanartSizeRBtn.setText(bundle.getString("medium")); // NOI18N
 
         fanartGroup.add(thumbFanartSizeRBtn);
         thumbFanartSizeRBtn.setFont(new Font("Ubuntu", 0, 12));         thumbFanartSizeRBtn.setText(bundle.getString("small")); // NOI18N
 
         GroupLayout imagesPnlLayout = new GroupLayout(imagesPnl);
         imagesPnl.setLayout(imagesPnlLayout);
         imagesPnlLayout.setHorizontalGroup(
             imagesPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imagesPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(thumbSzeLbl)
                     .addGroup(imagesPnlLayout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addGroup(imagesPnlLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(midThumbSizeRBtn)
                             .addComponent(origThumbSizeRBtn)
                             .addComponent(thumbThumbSizeRBtn))))
                 .addGap(80, 80, 80)
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(fanartSizeLbl)
                     .addGroup(imagesPnlLayout.createSequentialGroup()
                         .addGap(12, 12, 12)
                         .addGroup(imagesPnlLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(midFanartSizeRBtn)
                             .addComponent(origFanartSizeRBtn)
                             .addComponent(thumbFanartSizeRBtn))))
                 .addContainerGap(186, Short.MAX_VALUE))
         );
         imagesPnlLayout.setVerticalGroup(
             imagesPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imagesPnlLayout.createSequentialGroup()
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(thumbSzeLbl)
                     .addComponent(fanartSizeLbl))
                 .addPreferredGap(ComponentPlacement.UNRELATED)
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(origThumbSizeRBtn)
                     .addComponent(origFanartSizeRBtn))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(midThumbSizeRBtn)
                     .addComponent(midFanartSizeRBtn))
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(imagesPnlLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(thumbThumbSizeRBtn)
                     .addComponent(thumbFanartSizeRBtn)))
         );
 
         GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                     .addComponent(movieImagePnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(imagesPnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(movieImagePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(imagesPnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(325, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab("Image", jPanel1);
 
         extensionPnl.setBorder(BorderFactory.createTitledBorder(null, "Extension", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         removeExtensuionBtn.setIcon(new ImageIcon(getClass().getResource("/image/list-remove-4.png")));         removeExtensuionBtn.setToolTipText(bundle.getString("removeExt")); // NOI18N
         removeExtensuionBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 removeExtensuionBtnActionPerformed(evt);
             }
         });
 
         addExtensionBtn.setIcon(new ImageIcon(getClass().getResource("/image/list-add-5.png")));         addExtensionBtn.setToolTipText(bundle.getString("addExt")); // NOI18N
         addExtensionBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 addExtensionBtnActionPerformed(evt);
             }
         });
 
         extentionJlist.setFont(new Font("Ubuntu", 0, 12));         extentionJlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         extensionScrollP.setViewportView(extentionJlist);
 
         extensionHelp.setIcon(new ImageIcon(getClass().getResource("/image/system-help-3.png")));         extensionHelp.setToolTipText(bundle.getString("help")); // NOI18N
         extensionHelp.setMaximumSize(new Dimension(26, 26));
         extensionHelp.setMinimumSize(new Dimension(26, 26));
         extensionHelp.setPreferredSize(new Dimension(26, 26));
         extensionHelp.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 extensionHelpActionPerformed(evt);
             }
         });
 
         useExtensionFilterChk.setFont(new Font("Ubuntu", 0, 12));         useExtensionFilterChk.setText(bundle.getString("useExtFilter")); // NOI18N
 
         GroupLayout extensionPnlLayout = new GroupLayout(extensionPnl);
         extensionPnl.setLayout(extensionPnlLayout);
         extensionPnlLayout.setHorizontalGroup(
             extensionPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(extensionPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(extensionPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(Alignment.TRAILING, extensionPnlLayout.createSequentialGroup()
                         .addComponent(addExtensionBtn, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(removeExtensuionBtn, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE))
                     .addComponent(extensionScrollP, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                     .addComponent(useExtensionFilterChk))
                 .addPreferredGap(ComponentPlacement.UNRELATED)
                 .addComponent(extensionHelp, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         extensionPnlLayout.setVerticalGroup(
             extensionPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(extensionPnlLayout.createSequentialGroup()
                 .addGroup(extensionPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(extensionPnlLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(useExtensionFilterChk)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(extensionScrollP, GroupLayout.PREFERRED_SIZE, 96, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(extensionPnlLayout.createParallelGroup(Alignment.LEADING)
                             .addComponent(addExtensionBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                             .addComponent(removeExtensuionBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)))
                     .addComponent(extensionHelp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         fileNameFilterPnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("movieFileNameFilter"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         moveLeft.setIcon(new ImageIcon(getClass().getResource("/image/go-previous-3.png")));         moveLeft.setToolTipText(bundle.getString("moveLeft")); // NOI18N
         moveLeft.setEnabled(false);
         moveLeft.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 moveLeftActionPerformed(evt);
             }
         });
 
         moveRight.setIcon(new ImageIcon(getClass().getResource("/image/go-next-3.png")));         moveRight.setToolTipText(bundle.getString("moveRight")); // NOI18N
         moveRight.setEnabled(false);
         moveRight.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 moveRightActionPerformed(evt);
             }
         });
 
         addFilter.setIcon(new ImageIcon(getClass().getResource("/image/list-add-5.png")));         addFilter.setToolTipText(bundle.getString("addFilter")); // NOI18N
         addFilter.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 addFilterActionPerformed(evt);
             }
         });
 
         removeFilterBtn.setIcon(new ImageIcon(getClass().getResource("/image/list-remove-4.png")));         removeFilterBtn.setToolTipText(bundle.getString("removeFilter")); // NOI18N
         removeFilterBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 removeFilterBtnActionPerformed(evt);
             }
         });
 
         filterJlist.setFont(new Font("Ubuntu", 0, 12));         filterJlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         filterScrollP.setViewportView(filterJlist);
 
         filenameFilterHelp.setIcon(new ImageIcon(getClass().getResource("/image/system-help-3.png")));         filenameFilterHelp.setToolTipText(bundle.getString("help")); // NOI18N
         filenameFilterHelp.setMaximumSize(new Dimension(26, 26));
         filenameFilterHelp.setMinimumSize(new Dimension(26, 26));
         filenameFilterHelp.setPreferredSize(new Dimension(26, 26));
         filenameFilterHelp.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 filenameFilterHelpActionPerformed(evt);
             }
         });
 
         resetNameFilter.setIcon(new ImageIcon(getClass().getResource("/image/dialog-cancel-2-16.png")));         resetNameFilter.setToolTipText(bundle.getString("resetFilterList")); // NOI18N
         resetNameFilter.setMargin(new Insets(2, 2, 2, 2));
         resetNameFilter.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 resetNameFilterActionPerformed(evt);
             }
         });
 
         GroupLayout fileNameFilterPnlLayout = new GroupLayout(fileNameFilterPnl);
         fileNameFilterPnl.setLayout(fileNameFilterPnlLayout);
         fileNameFilterPnlLayout.setHorizontalGroup(
             fileNameFilterPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(fileNameFilterPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                         .addComponent(moveLeft, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(moveRight, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED, 51, Short.MAX_VALUE)
                         .addComponent(addFilter, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(removeFilterBtn, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addGap(12, 12, 12))
                     .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                         .addComponent(filterScrollP, GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                         .addPreferredGap(ComponentPlacement.UNRELATED)))
                 .addGroup(fileNameFilterPnlLayout.createParallelGroup(Alignment.LEADING, false)
                     .addComponent(filenameFilterHelp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(resetNameFilter, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addContainerGap())
         );
         fileNameFilterPnlLayout.setVerticalGroup(
             fileNameFilterPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                 .addGroup(fileNameFilterPnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                         .addComponent(filterScrollP, GroupLayout.PREFERRED_SIZE, 108, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addGroup(fileNameFilterPnlLayout.createParallelGroup(Alignment.TRAILING)
                             .addGroup(fileNameFilterPnlLayout.createParallelGroup(Alignment.BASELINE)
                                 .addComponent(moveLeft, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                                 .addComponent(moveRight, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                             .addComponent(removeFilterBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                             .addComponent(addFilter, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)))
                     .addGroup(fileNameFilterPnlLayout.createSequentialGroup()
                         .addComponent(filenameFilterHelp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(resetNameFilter)))
                 .addGap(22, 22, 22))
         );
 
         GroupLayout filtersPnlLayout = new GroupLayout(filtersPnl);
         filtersPnl.setLayout(filtersPnlLayout);
         filtersPnlLayout.setHorizontalGroup(
             filtersPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, filtersPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(filtersPnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(fileNameFilterPnl, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(extensionPnl, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         filtersPnlLayout.setVerticalGroup(
             filtersPnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(filtersPnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(extensionPnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(fileNameFilterPnl, GroupLayout.PREFERRED_SIZE, 190, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(121, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab(bundle.getString("filter"), filtersPnl); // NOI18N
 
         imagePnl.setBorder(BorderFactory.createTitledBorder(null, "Image", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         actorCacheLbl.setFont(new Font("Ubuntu", 1, 13));         actorCacheLbl.setText(bundle.getString("useForActor")); // NOI18N
 
         fanartCacheLbl.setFont(new Font("Ubuntu", 1, 13));         fanartCacheLbl.setText(bundle.getString("useForFanart")); // NOI18N
 
         thumbCacheLbl.setFont(new Font("Ubuntu", 1, 13));         thumbCacheLbl.setText(bundle.getString("useForThumb")); // NOI18N
 
         clearThumbBtn.setIcon(new ImageIcon(getClass().getResource("/image/user-trash-full.png")));         clearThumbBtn.setToolTipText(bundle.getString("clearThumbCache")); // NOI18N
         clearThumbBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 clearThumbBtnActionPerformed(evt);
             }
         });
 
         clearFanartBtn.setIcon(new ImageIcon(getClass().getResource("/image/user-trash-full.png")));         clearFanartBtn.setToolTipText(bundle.getString("clearFanartCache")); // NOI18N
         clearFanartBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 clearFanartBtnActionPerformed(evt);
             }
         });
 
         clearActorBtn.setIcon(new ImageIcon(getClass().getResource("/image/user-trash-full.png")));         clearActorBtn.setToolTipText(bundle.getString("clearActorCache")); // NOI18N
         clearActorBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 clearActorBtnActionPerformed(evt);
             }
         });
 
         GroupLayout imagePnlLayout = new GroupLayout(imagePnl);
         imagePnl.setLayout(imagePnlLayout);
         imagePnlLayout.setHorizontalGroup(
             imagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imagePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(imagePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(thumbCacheLbl)
                     .addComponent(fanartCacheLbl)
                     .addComponent(actorCacheLbl))
                 .addPreferredGap(ComponentPlacement.RELATED, 259, Short.MAX_VALUE)
                 .addGroup(imagePnlLayout.createParallelGroup(Alignment.LEADING, false)
                     .addComponent(clearActorBtn, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(clearFanartBtn, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(clearThumbBtn, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         imagePnlLayout.setVerticalGroup(
             imagePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(imagePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(imagePnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(thumbCacheLbl)
                     .addComponent(clearThumbBtn))
                 .addGap(18, 18, 18)
                 .addGroup(imagePnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(fanartCacheLbl)
                     .addComponent(clearFanartBtn))
                 .addGap(18, 18, 18)
                 .addGroup(imagePnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(actorCacheLbl)
                     .addComponent(clearActorBtn))
                 .addContainerGap())
         );
 
         xmlFilePnl.setBorder(BorderFactory.createTitledBorder(null, bundle.getString("xmlFiles"), TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         xmlLbl.setFont(new Font("Ubuntu", 1, 13));         xmlLbl.setText(bundle.getString("useForXml")); // NOI18N
 
         clearXmlBtn.setIcon(new ImageIcon(getClass().getResource("/image/user-trash-full.png")));         clearXmlBtn.setToolTipText(bundle.getString("clearXmlCache")); // NOI18N
         clearXmlBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 clearXmlBtnActionPerformed(evt);
             }
         });
 
         clearXmlCacheOnStartChk.setFont(new Font("Ubuntu", 1, 13));         clearXmlCacheOnStartChk.setText(bundle.getString("clearXmlCacheOnStart")); // NOI18N
 
         GroupLayout xmlFilePnlLayout = new GroupLayout(xmlFilePnl);
         xmlFilePnl.setLayout(xmlFilePnlLayout);
         xmlFilePnlLayout.setHorizontalGroup(
             xmlFilePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(xmlFilePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(xmlFilePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addGroup(xmlFilePnlLayout.createSequentialGroup()
                         .addComponent(xmlLbl)
                         .addPreferredGap(ComponentPlacement.RELATED, 271, Short.MAX_VALUE)
                         .addComponent(clearXmlBtn))
                     .addGroup(xmlFilePnlLayout.createSequentialGroup()
                         .addComponent(clearXmlCacheOnStartChk)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         xmlFilePnlLayout.setVerticalGroup(
             xmlFilePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(xmlFilePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(xmlFilePnlLayout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(clearXmlBtn)
                     .addComponent(xmlLbl))
                 .addPreferredGap(ComponentPlacement.UNRELATED)
                 .addComponent(clearXmlCacheOnStartChk)
                 .addContainerGap(16, Short.MAX_VALUE))
         );
 
         GroupLayout cachePnlLayout = new GroupLayout(cachePnl);
         cachePnl.setLayout(cachePnlLayout);
         cachePnlLayout.setHorizontalGroup(
             cachePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(cachePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(cachePnlLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(imagePnl, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(xmlFilePnl, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         cachePnlLayout.setVerticalGroup(
             cachePnlLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(cachePnlLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(imagePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addComponent(xmlFilePnl, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(239, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab("Cache", cachePnl);
 
         proxySubPanel.setBorder(BorderFactory.createTitledBorder(null, "Proxy", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Ubuntu", 1, 14))); 
         useProxyChk.setFont(new Font("Ubuntu", 1, 13));         useProxyChk.setText(bundle.getString("useProxy")); // NOI18N
 
         jLabel1.setFont(new Font("Ubuntu", 1, 13));         jLabel1.setText("Url");
 
         jLabel2.setFont(new Font("Ubuntu", 1, 13));         jLabel2.setText("Port");
 
         GroupLayout proxySubPanelLayout = new GroupLayout(proxySubPanel);
         proxySubPanel.setLayout(proxySubPanelLayout);
         proxySubPanelLayout.setHorizontalGroup(
             proxySubPanelLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(proxySubPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(proxySubPanelLayout.createParallelGroup(Alignment.LEADING)
                     .addComponent(useProxyChk)
                     .addGroup(proxySubPanelLayout.createSequentialGroup()
                         .addComponent(jLabel1)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(proxyUrlField, GroupLayout.PREFERRED_SIZE, 282, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(jLabel2)
                         .addPreferredGap(ComponentPlacement.RELATED)
                         .addComponent(proxyPortField, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(19, Short.MAX_VALUE))
         );
         proxySubPanelLayout.setVerticalGroup(
             proxySubPanelLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(proxySubPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(useProxyChk)
                 .addPreferredGap(ComponentPlacement.RELATED)
                 .addGroup(proxySubPanelLayout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(jLabel1)
                     .addComponent(proxyUrlField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2)
                     .addComponent(proxyPortField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         GroupLayout proxyPanelLayout = new GroupLayout(proxyPanel);
         proxyPanel.setLayout(proxyPanelLayout);
         proxyPanelLayout.setHorizontalGroup(
             proxyPanelLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(proxyPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(proxySubPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         proxyPanelLayout.setVerticalGroup(
             proxyPanelLayout.createParallelGroup(Alignment.LEADING)
             .addGroup(proxyPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(proxySubPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(422, Short.MAX_VALUE))
         );
 
         settingTabPan.addTab("Proxy", proxyPanel);
 
         saveBtn.setIcon(new ImageIcon(getClass().getResource("/image/dialog-ok-2.png")));         saveBtn.setText(bundle.getString("save")); // NOI18N
         saveBtn.setToolTipText(bundle.getString("save")); // NOI18N
         saveBtn.setMargin(new Insets(2, 2, 2, 2));
         saveBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 saveBtnActionPerformed(evt);
             }
         });
 
         CancelBtn.setIcon(new ImageIcon(getClass().getResource("/image/dialog-cancel-2.png")));         CancelBtn.setText(bundle.getString("cancel")); // NOI18N
         CancelBtn.setToolTipText(bundle.getString("cancel")); // NOI18N
         CancelBtn.setMargin(new Insets(2, 2, 2, 2));
         CancelBtn.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent evt) {
                 CancelBtnActionPerformed(evt);
             }
         });
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                     .addComponent(settingTabPan, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 501, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(saveBtn, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(CancelBtn, GroupLayout.PREFERRED_SIZE, 84, GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(Alignment.LEADING)
             .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(settingTabPan)
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(Alignment.BASELINE)
                     .addComponent(saveBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE)
                     .addComponent(CancelBtn, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
   private void addFilterActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addFilterActionPerformed
     String s = (String) JOptionPane.showInputDialog(this, Utils.i18n("filter"), Utils.i18n("addFilter"), JOptionPane.PLAIN_MESSAGE, null, null, null);
     int index = currentFilterIndex;
     if (filters.contains(s)) {
       JOptionPane.showMessageDialog(null, s + " " + Utils.i18n("alreadyInList"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
     if ((s != null) && (s.length() > 0)) {
       filters.add(index, s);
       loadList(filterJlist, filters.toArray(new String[filters.size()]));
       filterJlist.setSelectedIndex(index);
       currentFilterIndex = index;
     }
 }//GEN-LAST:event_addFilterActionPerformed
 
   private void addExtensionBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_addExtensionBtnActionPerformed
     String s = (String) JOptionPane.showInputDialog(this, "Extension", Utils.i18n("addExt"), JOptionPane.PLAIN_MESSAGE, null, null, null);
 
     if ((s != null) && (s.length() > 0)) {
       extensions = Arrays.copyOf(extensions, extensions.length + 1);
       extensions[extensions.length - 1] = s;
       loadList(extentionJlist, extensions);
       extentionJlist.setSelectedIndex(extensions.length - 1);
       currentExtensionIndex = extensions.length - 1;
     }
 }//GEN-LAST:event_addExtensionBtnActionPerformed
 
   private void helpBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_helpBtnActionPerformed
     JOptionPane.showMessageDialog(this, Utils.i18n("movieFormatHelp").replace("|", separatorField.getText()).replace("\"limit\"", limitField.getText()), Utils.i18n("movieFileName"), JOptionPane.INFORMATION_MESSAGE);
 }//GEN-LAST:event_helpBtnActionPerformed
 
   private void movieInfoPanelChkItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_movieInfoPanelChkItemStateChanged
     boolean activate = true;
     if (!movieInfoPanelChk.isSelected()) {
       activate = false;
     }
     actorImageChk.setEnabled(activate);
     thumbsChk.setEnabled(activate);
     fanartsChk.setEnabled(activate);
   }//GEN-LAST:event_movieInfoPanelChkItemStateChanged
 
   private void filenameTestBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_filenameTestBtnActionPerformed
     int limit;
     Utils.CaseConversionType casse = CaseConversionType.FIRSTLO;
     try {
       limit = Integer.parseInt(limitField.getText());
     } catch (NumberFormatException e) {
       JOptionPane.showMessageDialog(this, Utils.i18n("nanLimit"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
 
     for (int i = 0; i < rBtnCase.length; i++) {
       if (rBtnCase[i].isSelected()) {
         casse = Utils.CaseConversionType.values()[i];
       }
     }
 
     String res = movieRenamerTest(formatField.getText(), limit, casse, separatorField.getText(), rmSpcCharChk.isSelected(), rmDupSpaceChk.isSelected(), true);
     filenameTestField.setText(res);
   }//GEN-LAST:event_filenameTestBtnActionPerformed
 
   private void extensionHelpActionPerformed(ActionEvent evt) {//GEN-FIRST:event_extensionHelpActionPerformed
     JOptionPane.showMessageDialog(this, Utils.i18n("extensionsHelp"), "Extension", JOptionPane.INFORMATION_MESSAGE);
   }//GEN-LAST:event_extensionHelpActionPerformed
 
   private void filenameFilterHelpActionPerformed(ActionEvent evt) {//GEN-FIRST:event_filenameFilterHelpActionPerformed
     JOptionPane.showMessageDialog(this, Utils.i18n("movieFileFilterHelp"), Utils.i18n("movieFileNameFilter"), JOptionPane.INFORMATION_MESSAGE);
   }//GEN-LAST:event_filenameFilterHelpActionPerformed
 
   private void moveRightActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveRightActionPerformed
     String value = filters.get(currentFilterIndex);
     filters.remove(currentFilterIndex);
     filters.add(currentFilterIndex + 1, value);
     loadList(filterJlist, filters.toArray(new String[filters.size()]));
     filterJlist.setSelectedIndex(filters.indexOf(value));
   }//GEN-LAST:event_moveRightActionPerformed
 
   private void moveLeftActionPerformed(ActionEvent evt) {//GEN-FIRST:event_moveLeftActionPerformed
     String value = filters.get(currentFilterIndex);
     filters.remove(currentFilterIndex);
     filters.add(currentFilterIndex - 1, value);
     loadList(filterJlist, filters.toArray(new String[filters.size()]));
     filterJlist.setSelectedIndex(filters.indexOf(value));
   }//GEN-LAST:event_moveLeftActionPerformed
 
   private void removeFilterBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeFilterBtnActionPerformed
     filters.remove(currentFilterIndex);
     int pos = currentFilterIndex;
     loadList(filterJlist, filters.toArray(new String[filters.size()]));
     filterJlist.setSelectedIndex((pos - 1) > 0 ? pos - 1 : 0);
   }//GEN-LAST:event_removeFilterBtnActionPerformed
 
   private void removeExtensuionBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_removeExtensuionBtnActionPerformed
     String[] newArray = Utils.removeFromArray(extensions, currentExtensionIndex);
     if (newArray != null) {
       extensions = newArray;
       loadList(extentionJlist, extensions);
       int pos = currentExtensionIndex - 1;
       if (pos < 0) {
         pos++;
       }
       extentionJlist.setSelectedIndex(pos);
       currentExtensionIndex = pos;
     }
   }//GEN-LAST:event_removeExtensuionBtnActionPerformed
 
   private void saveBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_saveBtnActionPerformed
     boolean restartApp = false;
     Settings oldSetting = Settings.getInstance();
     try {
       oldSetting = setting.clone();
     } catch (CloneNotSupportedException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     }
 
     // Check all number settings
     try {
       setting.movieFilenameLimit = Integer.parseInt(limitField.getText());
     } catch (NumberFormatException e) {
       JOptionPane.showMessageDialog(this, Utils.i18n("nanFileLimit"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
     try {
       setting.movieFolderLimit = Integer.parseInt(limitFolderField.getText());
     } catch (NumberFormatException e) {
       JOptionPane.showMessageDialog(this, Utils.i18n("nanFolderLimit"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
     try {
       setting.proxyPort = Integer.parseInt(proxyPortField.getText());
     } catch (NumberFormatException e) {
       JOptionPane.showMessageDialog(this, Utils.i18n("nanProxyPort"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
 
 
     if (!formatField.getText().contains("<t>") && !formatField.getText().contains("<ot>")) {
       JOptionPane.showMessageDialog(this, Utils.i18n("noTitle"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
 
     // General Setting
     if (setting.movieInfoPanel != movieInfoPanelChk.isSelected()) {
       Settings.interfaceChanged = true;
     }
     if (setting.thumb != thumbsChk.isSelected()) {
       Settings.interfaceChanged = true;
     }
     if (setting.fanart != fanartsChk.isSelected()) {
       Settings.interfaceChanged = true;
     }
     if (setting.actorImage != actorImageChk.isSelected()) {
       Settings.interfaceChanged = true;
     }
 
     if (!setting.laf.equals(Settings.lookAndFeels[lafCmbBox.getSelectedIndex()].getName())) {
       Settings.lafChanged = true;
     }
     setting.laf = Settings.lookAndFeels[lafCmbBox.getSelectedIndex()].getName();
     setting.selectFrstMedia = selectFirstMovieChk.isSelected();
     setting.selectFrstRes = selectFirstResChk.isSelected();
     setting.showNotaMovieWarn = showNotaMovieWarnChk.isSelected();
     setting.scanSubfolder = scanSubfolderChk.isSelected();
     setting.useExtensionFilter = useExtensionFilterChk.isSelected();
     setting.movieInfoPanel = movieInfoPanelChk.isSelected();
     setting.actorImage = actorImageChk.isSelected();
     setting.thumb = thumbsChk.isSelected();
     setting.fanart = fanartsChk.isSelected();
     setting.autoSearchMedia = autoSearchChk.isSelected();
     setting.checkUpdate = checkUpdateChk.isSelected();
 
     boolean langFr = setting.locale.equals("fr");
     if (langFr != frenchRbtn.isSelected()) {
       setting.locale = (frenchRbtn.isSelected() ? "fr" : "en");
       int n = JOptionPane.showConfirmDialog(this, Settings.APPNAME + Utils.SPACE + Utils.i18n("wantRestart"), "Question", JOptionPane.YES_NO_OPTION);
       if (n == JOptionPane.YES_OPTION) {
         restartApp = true;
       }
     }
 
     // Rename Setting
     for (int i = 0; i < rBtnCase.length; i++) {
       if (rBtnCase[i].isSelected()) {
         setting.movieFilenameCase = Utils.CaseConversionType.values()[i];
       }
     }
 
     for (int i = 0; i < rBtnFolderCase.length; i++) {
       if (rBtnFolderCase[i].isSelected()) {
         setting.movieFolderCase = i;
       }
     }
 
     for (int i = 0; i < rBtnThumbList.length; i++) {
       if (rBtnThumbList[i].isSelected()) {
         setting.thumbSize = i;
       }
     }
 
     for (int i = 0; i < rBtnFanartList.length; i++) {
       if (rBtnFanartList[i].isSelected()) {
         setting.fanartSize = i;
       }
     }
 
     for (int i = 0; i < rBtnScrapper.length; i++) {
       if (rBtnScrapper[i].isSelected()) {
         setting.movieScrapper = WorkerManager.MovieScrapper.values()[i];
       }
     }
 
     setting.movieFilenameTrim = rmSpcCharChk.isSelected();
     setting.movieFilenameRmDupSpace = rmDupSpaceChk.isSelected();
 
     setting.movieFilenameCreateDirectory = createDirChk.isSelected();
     setting.movieFolderFormat = formatFolderField.getText();
     setting.movieFolderSeparator = separatorFolderField.getText();
     setting.movieFolderTrim = rmSpcCharFolderChk.isSelected();
     setting.movieFolderRmDupSpace = rmDupSpaceFolderChk.isSelected();
 
     //Search
     setting.movieScrapperFR = scrapperFrRbtn.isSelected();
     setting.displayThumbResult = displayThumbResultChk.isSelected();
     setting.sortBySimiYear = sortbySimiChk.isSelected();
     setting.displayApproximateResult = displayAppResultCheckBox.isSelected();
     setting.nbResult = limitResultComboBox.getSelectedIndex();
     setting.nfoType = xbmcNFORBtn.isSelected() ? 0 : 1;
 
     // Movie Files
     setting.movieFilenameFormat = formatField.getText();
     setting.movieFilenameCreateDirectory = createDirChk.isSelected();
     setting.thumbExt = thumbExtCbBox.getSelectedIndex();
 
 
     setting.movieFilenameSeparator = separatorField.getText();
 
     // Filter
     setting.extensions = extensions;
     setting.mediaNameFilters = filters;
 
     //Cache
     setting.clearXMLCache = clearXmlCacheOnStartChk.isSelected();
 
     // Proxy
     setting.useProxy = useProxyChk.isSelected();
     setting.proxyUrl = proxyUrlField.getText();
 
     settingsChange.firePropertyChange("settingChange", oldSetting, setting);
     setting.saveSetting();
 
     if (restartApp) {
       try {
         if (!Utils.restartApplication(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()))) {
           JOptionPane.showMessageDialog(this, Utils.i18n("cantRestart"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
         } else {
           dispose();
           System.exit(0);
         }
       } catch (Exception ex) {
         JOptionPane.showMessageDialog(this, Utils.i18n("cantRestart") + Utils.ENDLINE + ex.getMessage(), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       }
     }
 
     dispose();
   }//GEN-LAST:event_saveBtnActionPerformed
 
   private void lafCmbBoxActionPerformed(ActionEvent evt) {//GEN-FIRST:event_lafCmbBoxActionPerformed
     try {
       UIManager.setLookAndFeel(Settings.lookAndFeels[lafCmbBox.getSelectedIndex()].getClassName());
       SwingUtilities.updateComponentTreeUI(this);
     } catch (ClassNotFoundException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (InstantiationException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (IllegalAccessException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     } catch (UnsupportedLookAndFeelException ex) {
       Settings.LOGGER.log(Level.SEVERE, null, ex);
     }
     pack();
   }//GEN-LAST:event_lafCmbBoxActionPerformed
 
   private void CancelBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_CancelBtnActionPerformed
     setVisible(false);
   }//GEN-LAST:event_CancelBtnActionPerformed
 
   private void clearXmlBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearXmlBtnActionPerformed
     Utils.deleteFileInDirectory(new File(Settings.xmlCacheDir));
     xmlLbl.setText(Utils.getDirSizeInMegabytes(new File(Settings.xmlCacheDir)) + Utils.i18n("useForXml"));
   }//GEN-LAST:event_clearXmlBtnActionPerformed
 
   private void clearActorBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearActorBtnActionPerformed
     Utils.deleteFileInDirectory(new File(Settings.actorCacheDir));
     actorCacheLbl.setText(Utils.getDirSizeInMegabytes(new File(Settings.actorCacheDir)) + Utils.i18n("useForActor"));
   }//GEN-LAST:event_clearActorBtnActionPerformed
 
   private void clearFanartBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearFanartBtnActionPerformed
     Utils.deleteFileInDirectory(new File(Settings.fanartCacheDir));
     fanartCacheLbl.setText(Utils.getDirSizeInMegabytes(new File(Settings.fanartCacheDir)) + Utils.i18n("useForFanart"));
   }//GEN-LAST:event_clearFanartBtnActionPerformed
 
   private void clearThumbBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_clearThumbBtnActionPerformed
     Utils.deleteFileInDirectory(new File(Settings.thumbCacheDir));
     thumbCacheLbl.setText(Utils.getDirSizeInMegabytes(new File(Settings.thumbCacheDir)) + Utils.i18n("useForThumb"));
   }//GEN-LAST:event_clearThumbBtnActionPerformed
 
   private void allocineRbtnItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_allocineRbtnItemStateChanged
     scrapperEnRbtn.setEnabled(!allocineRbtn.isSelected());
     scrapperFrRbtn.setEnabled(!allocineRbtn.isSelected());
   }//GEN-LAST:event_allocineRbtnItemStateChanged
 
   private void resultHelpActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resultHelpActionPerformed
     JOptionPane.showMessageDialog(this, Utils.i18n("resultHelp"), Utils.i18n("result"), JOptionPane.INFORMATION_MESSAGE);
   }//GEN-LAST:event_resultHelpActionPerformed
 
   private void imdbRBtnItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_imdbRBtnItemStateChanged
     limitResultComboBox.setEnabled(imdbRBtn.isSelected());
     displayAppResultCheckBox.setEnabled(imdbRBtn.isSelected());
   }//GEN-LAST:event_imdbRBtnItemStateChanged
 
   private void resetNameFilterActionPerformed(ActionEvent evt) {//GEN-FIRST:event_resetNameFilterActionPerformed
     loadList(filterJlist, Settings.nameFilters);
   }//GEN-LAST:event_resetNameFilterActionPerformed
 
   private void imdbRBtn1ItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_imdbRBtn1ItemStateChanged
     // TODO add your handling code here:
   }//GEN-LAST:event_imdbRBtn1ItemStateChanged
 
   private void allocineRbtn1ItemStateChanged(ItemEvent evt) {//GEN-FIRST:event_allocineRbtn1ItemStateChanged
     // TODO add your handling code here:
   }//GEN-LAST:event_allocineRbtn1ItemStateChanged
 
   private void helpBtn1ActionPerformed(ActionEvent evt) {//GEN-FIRST:event_helpBtn1ActionPerformed
     JOptionPane.showMessageDialog(this, Utils.i18n("movieFormatHelp").replace("|", separatorField.getText()).replace("\"limit\"", limitField.getText()), Utils.i18n("movieFileName"), JOptionPane.INFORMATION_MESSAGE);
   }//GEN-LAST:event_helpBtn1ActionPerformed
 
   private void folderTestBtnActionPerformed(ActionEvent evt) {//GEN-FIRST:event_folderTestBtnActionPerformed
     int limit;
     CaseConversionType casse = CaseConversionType.FIRSTLO;
     try {
       limit = Integer.parseInt(limitFolderField.getText());
     } catch (NumberFormatException e) {
       JOptionPane.showMessageDialog(this, Utils.i18n("nanLimit"), Utils.i18n("error"), JOptionPane.ERROR_MESSAGE);
       return;
     }
 
     for (int i = 0; i < rBtnFolderCase.length; i++) {
       if (rBtnFolderCase[i].isSelected()) {
         casse = CaseConversionType.values()[i];
       }
     }
 
     String res = movieRenamerTest(formatFolderField.getText(), limit, casse, separatorFolderField.getText(), rmSpcCharFolderChk.isSelected(), rmDupSpaceFolderChk.isSelected(), false);
     folderTestField.setText(res);
   }//GEN-LAST:event_folderTestBtnActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private JButton CancelBtn;
     private JPanel SearchPnl;
     private JLabel actorCacheLbl;
     private JCheckBox actorImageChk;
     private JButton addExtensionBtn;
     private JButton addFilter;
     private JRadioButton allocineRbtn;
     private JRadioButton allocineRbtn1;
     private JCheckBox autoSearchChk;
     private JPanel cachePnl;
     private ButtonGroup caseFolderGroup;
     private JLabel caseFolderLbl;
     private ButtonGroup caseGroup;
     private JLabel caseLbl;
     private JCheckBox checkUpdateChk;
     private JButton clearActorBtn;
     private JButton clearFanartBtn;
     private JButton clearThumbBtn;
     private JButton clearXmlBtn;
     private JCheckBox clearXmlCacheOnStartChk;
     private JCheckBox createDirChk;
     private JLabel defaultFormatFolderLbl;
     private JLabel defaultFormatLbl;
     private JCheckBox displayAppResultCheckBox;
     private JCheckBox displayThumbResultChk;
     private JRadioButton englishRbtn;
     private JButton extensionHelp;
     private JPanel extensionPnl;
     private JScrollPane extensionScrollP;
     private JList extentionJlist;
     private JLabel fanartCacheLbl;
     private ButtonGroup fanartGroup;
     private JLabel fanartSizeLbl;
     private JCheckBox fanartsChk;
     private JPanel fileNameFilterPnl;
     private JButton filenameFilterHelp;
     private JButton filenameTestBtn;
     private JTextField filenameTestField;
     private JList filterJlist;
     private JScrollPane filterScrollP;
     private JPanel filtersPnl;
     private JRadioButton firstLaFolderRbtn;
     private JRadioButton firstLaRbtn;
     private JRadioButton firstLoFolderRbtn;
     private JRadioButton firstLoRbtn;
     private JButton folderTestBtn;
     private JTextField folderTestField;
     private JTextField formatField;
     private JTextField formatFolderField;
     private JLabel formatFolderLbl;
     private JLabel formatLbl;
     private JRadioButton frenchRbtn;
     private JPanel generalPnl;
     private JButton helpBtn;
     private JButton helpBtn1;
     private JPanel imagePnl;
     private JPanel imagesPnl;
     private ButtonGroup imdbLangGroup;
     private JRadioButton imdbRBtn;
     private JRadioButton imdbRBtn1;
     private JPanel imdbSearchPnl;
     private JPanel interfacePnl;
     private JLabel jLabel1;
     private JLabel jLabel2;
     private JPanel jPanel1;
     private JPanel jPanel2;
     private JPanel jPanel3;
     private JPanel jPanel5;
     private JPanel jPanel6;
     private JPanel jPanel7;
     private JPanel jPanel8;
     private JTabbedPane jTabbedPane2;
     private JComboBox lafCmbBox;
     private JLabel lafLbl;
     private ButtonGroup languageGroup;
     private JPanel languagePnl;
     private JTextField limitField;
     private JTextField limitFolderField;
     private JLabel limitLbl;
     private JLabel limitLbl1;
     private JComboBox limitResultComboBox;
     private JLabel limitResultLbl;
     private JRadioButton lowerFolderRbtn;
     private JRadioButton lowerRbtn;
     private JLabel lwarningLbl;
     private JRadioButton mediaPortalNFORBtn;
     private JRadioButton midFanartSizeRBtn;
     private JRadioButton midThumbSizeRBtn;
     private JButton moveLeft;
     private JButton moveRight;
     private JPanel movieFileNamePnl;
     private JPanel movieFileNamePnl1;
     private JPanel movieFolderTabPan;
     private JPanel movieImagePnl;
     private JCheckBox movieInfoPanelChk;
     private ButtonGroup nfoGroup;
     private JPanel nfoPnl;
     private JRadioButton noneFolderRbtn;
     private JRadioButton noneRbtn;
     private JRadioButton origFanartSizeRBtn;
     public JRadioButton origThumbSizeRBtn;
     private JPanel proxyPanel;
     private JTextField proxyPortField;
     private JPanel proxySubPanel;
     private JTextField proxyUrlField;
     private JButton removeExtensuionBtn;
     private JButton removeFilterBtn;
     private JPanel renamePnl;
     private JTabbedPane renameTabPan;
     private JButton resetNameFilter;
     private JButton resultHelp;
     private JCheckBox rmDupSpaceChk;
     private JCheckBox rmDupSpaceFolderChk;
     private JCheckBox rmSpcCharChk;
     private JCheckBox rmSpcCharFolderChk;
     private JButton saveBtn;
     private JCheckBox scanSubfolderChk;
     private JRadioButton scrapperEnRbtn;
     private JRadioButton scrapperEnRbtn1;
     private JRadioButton scrapperFrRbtn;
     private JRadioButton scrapperFrRbtn1;
     private ButtonGroup scrapperGroup;
     private JCheckBox selectFirstMovieChk;
     private JCheckBox selectFirstResChk;
     private JTextField separatorField;
     private JTextField separatorFolderField;
     private JLabel separatorFolderLbl;
     private JLabel separatorLbl;
     private JTabbedPane settingTabPan;
     private JCheckBox showNotaMovieWarnChk;
     private JCheckBox sortbySimiChk;
     private JLabel thumbCacheLbl;
     private JComboBox thumbExtCbBox;
     private JRadioButton thumbFanartSizeRBtn;
     private ButtonGroup thumbGroup;
     private JLabel thumbSzeLbl;
     private JRadioButton thumbThumbSizeRBtn;
     private JCheckBox thumbsChk;
     private JLabel thumnailsExtLbl;
     private JRadioButton tmdbRbtn;
     private JRadioButton tmdbRbtn1;
     private JPanel updatePnl;
     private JRadioButton upperFolderRbtn;
     private JRadioButton upperRbtn;
     private JCheckBox useExtensionFilterChk;
     private JCheckBox useProxyChk;
     private JRadioButton xbmcNFORBtn;
     private JPanel xmlFilePnl;
     private JLabel xmlLbl;
     // End of variables declaration//GEN-END:variables
 }
