 package com.iver.cit.gvsig.gui.preferences;
 
 import java.util.ArrayList;
import java.util.HashMap;
 import java.util.Iterator;
 import java.util.prefs.Preferences;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import com.iver.andami.PluginServices;
 import com.iver.andami.messages.NotificationManager;
 import com.iver.andami.preferences.AbstractPreferencePage;
 import com.iver.andami.preferences.StoreException;
 import com.iver.cit.gvsig.project.documents.view.snapping.ISnapper;
 import com.iver.cit.gvsig.project.documents.view.snapping.gui.SnapConfig;
 import com.iver.utiles.extensionPoints.ExtensionPoint;
 import com.iver.utiles.extensionPoints.ExtensionPoints;
 import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;
 
 
 /**
  * Preferencias de snapping.
  *
  * @author Vicente Caballero Navarro
  */
 public class SnapConfigPage extends AbstractPreferencePage {
     private static Preferences prefs = Preferences.userRoot().node("snappers");
     private ImageIcon icon;
     private SnapConfig snapConfig;
     private ArrayList<ISnapper> snappers = new ArrayList<ISnapper>();
 	private static boolean applySnappers=true;
     @SuppressWarnings("unchecked")
	public static HashMap<ISnapper,Boolean> selected = new HashMap<ISnapper,Boolean>();
     static {
     	new SnapConfigPage().initializeValues();
     }
 
 
     public SnapConfigPage() {
         super();
         icon = new ImageIcon(this.getClass().getClassLoader().getResource("images/Snapper.png"));
         snapConfig = new SnapConfig();
         snappers = getSnappers();
         snapConfig.setSnappers(snappers);
         addComponent(snapConfig);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     private static ArrayList<ISnapper> getSnappers() {
         ArrayList<ISnapper> snappers = new ArrayList<ISnapper>();
         ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
 
         ExtensionPoint extensionPoint = (ExtensionPoint) extensionPoints.get(
                 "Snapper");
         Iterator iterator = extensionPoint.keySet().iterator();
 
         while (iterator.hasNext()) {
             try {
                 ISnapper snapper = (ISnapper) extensionPoint.create((String) iterator.next());
                 snappers.add(snapper);
             } catch (InstantiationException e) {
             	NotificationManager.addError(e.getMessage(),e);
             } catch (IllegalAccessException e) {
             	NotificationManager.addError(e.getMessage(),e);
             }
         }
 
         return snappers;
     }
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public static ArrayList<ISnapper> getActivesSnappers() {
        if (!applySnappers)
     	   return new ArrayList<ISnapper>();
        return new ArrayList<ISnapper>(selected.keySet());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @throws StoreException DOCUMENT ME!
      */
     public void storeValues() throws StoreException {
         selected.clear();
     	for (int n = 0; n < snappers.size(); n++) {
             Boolean b = (Boolean) snapConfig.getTableModel().getValueAt(n, 0);
             ISnapper snp = (ISnapper) snappers.get(n);
             String nameClass=snp.getClass().getName();
             nameClass=nameClass.substring(nameClass.lastIndexOf('.'));
             prefs.putBoolean("snapper_activated" + nameClass, b.booleanValue());
             if (b.booleanValue())
             	selected.put(snp, b);
             prefs.putInt("snapper_priority"+ nameClass,snp.getPriority());
         }
         boolean b=snapConfig.applySnappers();
         prefs.putBoolean("apply-snappers",b);
         applySnappers=b;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void setChangesApplied() {
     	setChanged(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getID() {
         return this.getClass().getName();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getTitle() {
         return PluginServices.getText(this, "Snapping");
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public JPanel getPanel() {
         return this;
     }
 
     /**
      * DOCUMENT ME!
      */
     public void initializeValues() {
     	if (prefs.get("apply-snappers", null) == null){
     		initializeDefaults();
     	}
         for (int n = 0; n < snappers.size(); n++) {
             ISnapper snp = (ISnapper) snappers.get(n);
             String nameClass=snp.getClass().getName();
             nameClass=nameClass.substring(nameClass.lastIndexOf('.'));
             boolean select = prefs.getBoolean("snapper_activated" + nameClass, false);
            if (select)
            	selected.put(snp, new Boolean(select));
             int priority = prefs.getInt("snapper_priority" + nameClass,3);
             snp.setPriority(priority);
         }
         applySnappers = prefs.getBoolean("apply-snappers",true);
         snapConfig.setApplySnappers(applySnappers);
         snapConfig.selectSnappers(selected);
 
     }
 
     /**
      * DOCUMENT ME!
      */
     public void initializeDefaults() {
     	for (int n = 0; n < snappers.size(); n++) {
             ISnapper snp = (ISnapper) snappers.get(n);
             String nameClass=snp.getClass().getName();
             nameClass=nameClass.substring(nameClass.lastIndexOf('.'));
             if (nameClass.equals(".FinalPointSnapper")){
             	selected.put(snp, new Boolean(true));
             	int priority = 1;
             	prefs.putInt("snapper_priority" + nameClass, priority);
                 snp.setPriority(priority);
             }else if (nameClass.equals(".NearestPointSnapper")){
             	selected.put(snp, new Boolean(true));
             	int priority = 2;
             	prefs.putInt("snapper_priority" + nameClass, priority);
                 snp.setPriority(priority);
             }else{
             	selected.put(snp, new Boolean(false));
             	int priority = 3;
             	prefs.putInt("snapper_priority" + nameClass, priority);
                 snp.setPriority(priority);
             }
         }
         applySnappers = true;
         snapConfig.setApplySnappers(applySnappers);
         snapConfig.selectSnappers(selected);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public ImageIcon getIcon() {
         return icon;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public boolean isValueChanged() {
     	return super.hasChanged();
     }
 }
