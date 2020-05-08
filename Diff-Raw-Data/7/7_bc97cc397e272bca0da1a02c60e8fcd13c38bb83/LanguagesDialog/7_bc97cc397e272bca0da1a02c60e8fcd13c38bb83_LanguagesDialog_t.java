 // %3239344521:de.hattrickorganizer.tools.updater%
 /*
  * Created on 06.04.2005
  *
  */
 package de.hattrickorganizer.tools.updater;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.io.File;
 import java.util.Hashtable;
import java.util.Iterator;
 import java.util.Properties;
 
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 
 import de.hattrickorganizer.gui.pluginWrapper.GUIPluginWrapper;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.tools.HOLogger;
 import de.hattrickorganizer.tools.SprachFileFilter;
 
 
 /**
  * The LanguageDialog compare all installed language.properties Files and the Files on
  * www.hoplugins.de
  *
  * @author Thorsten Dietz
  *
  * @since 1.35
  */
 public final class LanguagesDialog extends UpdaterDialog {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     private static final String PROP_LANGUAGEFILE = HOVerwaltung.instance().getResource()
                                                                 .getProperty("Sprachdatei");
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     protected final String PROP_REFRESH = HOVerwaltung.instance().getResource().getProperty("Refresh");
 
     /** TODO Missing Parameter Documentation */
     protected final String WEB_LANGUAGE_DIR = UpdateController.PLUGINS_HOMEPAGE + "/Sprache/";
 
     /** TODO Missing Parameter Documentation */
     protected Hashtable hash;
 
     /** TODO Missing Parameter Documentation */
     private final String SPRACHE_DIRECTORY = System.getProperty("user.dir") + File.separator
                                              + "sprache";
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Creates a new LanguagesDialog object.
      *
      * @param data TODO Missing Constructuor Parameter Documentation
      */
     protected LanguagesDialog(Object data) {
         super(data, PROP_LANGUAGEFILE);
         hash = (Hashtable) data;
         inizialize();
 
         Container contenPane = getContentPane();
         contenPane.add(createTable(), BorderLayout.CENTER);
         contenPane.add(createButtons(), BorderLayout.SOUTH);
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param selected TODO Missing Method Parameter Documentation
      * @param columnNames2 TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected TableModel getModel(boolean selected, String[] columnNames2) {
         Object[][] updates = new Object[object.length][4];
         TableModel model = null;
         Properties props = null;
         String local_version = " - ";
         String hp_version = "-";
         boolean enabled = false;
 
         try {
             for (int i = 0; i < object.length; i++) {
                 local_version = " - ";
                 hp_version = "-";
                 enabled = false;
                 props = new java.util.Properties();
 
                 props.load(new java.io.FileInputStream(((File) object[i])));
                 local_version = props.getProperty("Version");
 
                 HPLanguageInfo info = (HPLanguageInfo) hash.get(((File) object[i]).getName());
 
                 if (info != null) {
                     hp_version = info.toString();
                     hash.remove(((File) object[i]).getName());
                 }
 
                 if ((local_version != null)
                     && !hp_version.equalsIgnoreCase("-")
                     && !local_version.equals(hp_version)) {
                     enabled = true;
                 }
 
                 updates[i][1] = getLabel(enabled, ((File) object[i]).getName());
                 updates[i][2] = getLabel(enabled, local_version);
                 updates[i][3] = getLabel(enabled, hp_version);
                 updates[i][0] = getCheckbox(selected, enabled);
             }
 
             Object[][] newLanguages = getNewLanguages(hash, selected);
             Object[][] value = new Object[updates.length + newLanguages.length][4];
 
             System.arraycopy(updates, 0, value, 0, updates.length);
             System.arraycopy(newLanguages, 0, value, updates.length, newLanguages.length);
 
             model = new TableModel(value, columnNames2);
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),e);
         }
 
         return model;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     protected void action() {
         if (table != null) {
             int i = 0;
 
             try {
                 for (i = 0; i < table.getRowCount(); i++) {
                     boolean selected = ((JCheckBox) table.getValueAt(i, 0)).isSelected();
 
                     if (selected) {
                         File languageFile = new File(SPRACHE_DIRECTORY + File.separator
                                                      + ((JLabel) table.getValueAt(i, 1)).getText());
                         languageFile.createNewFile();
 
                         String url = WEB_LANGUAGE_DIR + languageFile.getName();
                         UpdateHelper.instance().download(url, languageFile);
                     }
                      // selected
                 }
                  // for
 
                 GUIPluginWrapper.instance().getInfoPanel().clearLangInfo();
                 JOptionPane.showMessageDialog(null, PROP_NEW_START, PROP_LANGUAGEFILE,
                                               JOptionPane.INFORMATION_MESSAGE);
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null,
                                               PROP_FILE_NOT_FOUND + ": "
                                               + ((HPPluginInfo) object[i]).getZipFileName(),
                                               PROP_LANGUAGEFILE, JOptionPane.ERROR_MESSAGE);
             }
         }
 
         this.dispose();
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param hashi TODO Missing Method Parameter Documentation
      * @param selected TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private Object[][] getNewLanguages(Hashtable hashi, boolean selected) {
         Object[][] tmp = new Object[hashi.size()][4];
         boolean enabled = true;
 
        int i=0;
        for (Iterator it=hashi.values().iterator(); it.hasNext(); i++) {
            HPLanguageInfo element = (HPLanguageInfo)it.next();
             tmp[i][1] = getLabel(enabled, element.getFilename());
             tmp[i][2] = getLabel(enabled, "-");
             tmp[i][3] = getLabel(enabled, element.getVersion());
             tmp[i][0] = getCheckbox(selected, enabled);
         }
 
         return tmp;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void inizialize() {
         final File dir = new File(SPRACHE_DIRECTORY);
         object = dir.listFiles(new SprachFileFilter());
         columnNames = new String[4];
         columnNames[0] = PROP_REFRESH;
         columnNames[1] = PROP_NAME;
         columnNames[2] = "HO!";
         columnNames[3] = PROP_HOMEPAGE;
         okButtonLabel = PROP_APPLY;
     }
 }
