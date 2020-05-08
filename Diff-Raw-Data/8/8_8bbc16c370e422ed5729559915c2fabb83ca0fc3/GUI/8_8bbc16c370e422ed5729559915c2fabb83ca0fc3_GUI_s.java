 package gui;
 
 import aniAdd.Communication.ComEvent;
 import aniAdd.IAniAdd;
 import aniAdd.Modules.IMod_GUI;
 import aniAdd.Modules.IModule;
 import aniAdd.misc.Misc;
 import aniAdd.misc.Mod_Memory;
 import java.awt.Color;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.StringReader;
 import java.util.*;
 import javax.swing.JComponent;
 import javax.swing.TransferHandler;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class GUI extends javax.swing.JPanel implements IMod_GUI {
 
     private HashMap<String, ITab> tabs;
     private IAniAdd aniAdd;
     private Mod_Memory mem;
     private Stack<ComEvent> errorLst;
 
     public GUI() {
         initComponents();
         pnl_Notice.setVisible(false);
 
         tabs = new HashMap<String, ITab>();
         errorLst = new Stack<ComEvent>();
     }
 
     private void SaveToMem(String key, Object value) {
         mem.put("GUI_" + key, value);
     }
 
     private Object LoadFromMem(String key, Object defVal) {
         return mem.get("GUI_" + key, defVal);
     }
 
     private Object LoadFromMem(String key) {
         return mem.get("GUI_" + key);
     }
 
     private int AddGuiTab(ITab tab) {
         int index = tbctrl_Main.getTabCount();
         for (int i = 0; i < tbctrl_Main.getTabCount(); i++) {
             if (tab.PreferredTabLocation() < tabs.get(tbctrl_Main.getTitleAt(i)).PreferredTabLocation()) {
                 index = i;
                 break;
             }
         }
 
         tabs.put(tab.TabName(), tab);
         tbctrl_Main.insertTab(tab.TabName(), null, (JComponent) tab, null, index);
         tab.Initialize(aniAdd, new Gui());
         return index;
     }
 
     private void RemoveGuiTab(String tabName) {
         for (int i = 0; i < tbctrl_Main.getTabCount(); i++) {
             if (tabName.equals(tbctrl_Main.getTitleAt(i))) {
                 tabs.remove(tabName);
                 tbctrl_Main.removeTabAt(i);
                 return;
             }
         }
     }
 
     private void attachDragAndDrop(TransferHandler transferHandler) {
         setTransferHandler(transferHandler);
     }
 
     protected static List<File> transferableToFileList(Transferable t) {
         try {
             List<File> files = new LinkedList<File>();
             if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                 String data = (String) t.getTransferData(DataFlavor.stringFlavor);
                 BufferedReader buf = new BufferedReader(new StringReader(data));
                 String line;
                 while ((line = buf.readLine()) != null) {
                     if (line.startsWith("file:/")) {
                         line = line.substring("file:/".length());
                         files.add(new File(line));
                     } else {
                         System.out.println("Not sure how to read: " + line);
                     }
                 }
                 return files;
             } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                 List data = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
                 Iterator i = data.iterator();
                 while (i.hasNext()) {
                     files.add((File) i.next());
                 }
                 return files;
             }
             return null;
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
     private void DisplayErrorEvent(ComEvent comEvent) {
         errorLst.add(comEvent);
 
         Color c = (comEvent.Type() == ComEvent.eType.Error) ? Color.RED : (comEvent.Type() == ComEvent.eType.Warning ? Color.YELLOW : Color.GREEN);
         pnl_Notice.setBackground(c);
        lbl_Notice.setText(Misc.DateToString(new Date()) + " " + (String) comEvent.Params(0));
 
         pnl_Notice.setVisible(true);
     }
     // <editor-fold defaultstate="collapsed" desc="IModule">
     protected String modName = "MainGUI";
     protected eModState modState = eModState.New;
 
     public eModState ModState() {
         return modState;
     }
 
     public String ModuleName() {
         return modName;
     }
 
     public void Initialize(IAniAdd aniAdd) {
         modState = eModState.Initializing;
 
         this.aniAdd = aniAdd;
         aniAdd.addComListener(new AniAddEventHandler());
 
         mem = (Mod_Memory) aniAdd.GetModule("Memory");
         mem.addComListener(new ComListener() {
 
             public void EventHandler(ComEvent comEvent) {
                 if (comEvent.Type() == ComEvent.eType.Information && ((String) comEvent.Params(0)).equals("SettingsCleared")) {
                     tbctrl_Main.removeAll();
 
                     tbctrl_Main.removeChangeListener(tbctrl_Main.getChangeListeners()[0]);
 
 
                     for (ITab tab : tabs.values()) {
                         tab.Terminate();
                     }
                     tabs.clear();
                     InitUI();
                 }
             }
         });
 
         for (IModule mod : aniAdd.GetModules()) {
             mod.addComListener(new ComListener() {
 
                 public void EventHandler(ComEvent comEvent) {
                     if (comEvent.Type() == ComEvent.eType.Error || comEvent.Type() == ComEvent.eType.Fatal || comEvent.Type() == ComEvent.eType.Warning) {
                         DisplayErrorEvent(comEvent);
                     }
                 }
             });
         }
 
         InitUI();
 
        if((Integer)(mem.get("FirstStart", 0)) < 9) {
             DisplayErrorEvent(new ComEvent(this, ComEvent.eType.Information, "<html>Changelog:<br/>" +
                 "Added more TagSystem functions: See new tab \"Variables\" in the TagSystem section<br/>"+
                 "Fixed GUI slowdown after processing many files<br/>" +
                 "Fixed Re-authentication issues<br/>" +
                 "</html>"));
         }
 
         modState = eModState.Initialized;
     }
 
     private void InitUI() {
         ITab tab = new GUI_FileAdd();
         AddGuiTab(tab);
         tab = new GUI_Help();
         AddGuiTab(tab);
         tab = new GUI_Logs();
         AddGuiTab(tab);
         tab = new GUI_Options();
         AddGuiTab(tab);
         tbctrl_Main.setSelectedIndex((Integer) LoadFromMem("SelectedTabIndex", 0));
 
         tbctrl_Main.addChangeListener(new ChangeListener() {
 
             String oldTabId = "";
 
             public void stateChanged(ChangeEvent e) {
                 int selIndex = tbctrl_Main.getSelectedIndex() == -1 ? 0 : tbctrl_Main.getSelectedIndex();
 
                 if (oldTabId.isEmpty()) {
                     oldTabId = tbctrl_Main.getTitleAt(0);
                 }
 
                 tabs.get(oldTabId).LostFocus();
                 tabs.get(tbctrl_Main.getTitleAt(selIndex)).GainedFocus();
 
                 oldTabId = tbctrl_Main.getTitleAt(selIndex);
             }
         });
     }
 
     public void Terminate() {
         modState = eModState.Terminating;
 
         for (ITab tab : tabs.values()) {
             tab.Terminate();
         }
 
         modState = eModState.Terminated;
     }
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Com System">
     private ArrayList<ComListener> listeners = new ArrayList<ComListener>();
 
     protected void ComFire(ComEvent comEvent) {
         for (ComListener listener : listeners) {
             listener.EventHandler(comEvent);
         }
     }
 
     public void addComListener(ComListener comListener) {
         listeners.add(comListener);
     }
 
     public void RemoveComListener(ComListener comListener) {
         listeners.remove(comListener);
     }
 
     class AniAddEventHandler implements ComListener {
 
         public void EventHandler(ComEvent comEvent) {
         }
     }
     // </editor-fold>
 
     public static interface ITab {
 
         String TabName();
 
         int PreferredTabLocation();
 
         void Initialize(IAniAdd aniAdd, IGUI gui);
 
         void Terminate();
 
         void GainedFocus();
 
         void LostFocus();
 
         void GUIEventHandler(ComEvent comEvent);
     }
 
     protected class Gui implements IGUI {
 
         public eModState ModState() {
             return modState;
         }
 
         public void ToMem(String key, Object value) {
             SaveToMem(key, value);
         }
 
         public Object FromMem(String key) {
             return LoadFromMem(key);
         }
 
         public Object FromMem(String key, Object defValue) {
             return LoadFromMem(key, defValue);
         }
 
         public void GUIEvent(ComEvent comEvent) {
             for (ITab tab : tabs.values()) {
                 tab.GUIEventHandler(comEvent);
             }
         }
 
         public void SetDragnDropHandler(TransferHandler th) {
             attachDragAndDrop(th);
         }
 
         public int AddTab(ITab tab) {
             return AddGuiTab(tab);
         }
 
         public void SelectTab(int tabIndex) {
             tbctrl_Main.setSelectedIndex(tabIndex);
         }
 
         public void RemoveTab(String tabName) {
             RemoveGuiTab(tabName);
         }
 
         public void LogEvent(ComEvent.eType type, Object... params) {
             ComFire(new ComEvent(this, type, params));
         }
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         tbctrl_Main = new javax.swing.JTabbedPane();
         pnl_Notice = new javax.swing.JPanel();
         btn_CloseNotice = new javax.swing.JButton();
         lbl_Notice = new javax.swing.JLabel();
 
         pnl_Notice.setBackground(new java.awt.Color(255, 0, 0));
 
         btn_CloseNotice.setText("X");
         btn_CloseNotice.setMargin(new java.awt.Insets(0, 0, 0, 0));
         btn_CloseNotice.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btn_CloseNoticeActionPerformed(evt);
             }
         });
 
         lbl_Notice.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 
         javax.swing.GroupLayout pnl_NoticeLayout = new javax.swing.GroupLayout(pnl_Notice);
         pnl_Notice.setLayout(pnl_NoticeLayout);
         pnl_NoticeLayout.setHorizontalGroup(
             pnl_NoticeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_NoticeLayout.createSequentialGroup()
                 .addComponent(lbl_Notice, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btn_CloseNotice, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
         );
         pnl_NoticeLayout.setVerticalGroup(
             pnl_NoticeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnl_NoticeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(btn_CloseNotice, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addComponent(lbl_Notice, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnl_Notice, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(tbctrl_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 668, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(pnl_Notice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(tbctrl_Main, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE))
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void btn_CloseNoticeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_CloseNoticeActionPerformed
         errorLst.pop();
 
         if (errorLst.empty()) {
             pnl_Notice.setVisible(false);
         } else {
             ComEvent comEvent = errorLst.peek();
             Color c = (comEvent.Type() == ComEvent.eType.Error) ? Color.RED : (comEvent.Type() == ComEvent.eType.Warning ? Color.YELLOW : Color.GREEN);
             pnl_Notice.setBackground(c);
             lbl_Notice.setText((String) comEvent.Params(0));
         }
     }//GEN-LAST:event_btn_CloseNoticeActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btn_CloseNotice;
     private javax.swing.JLabel lbl_Notice;
     private javax.swing.JPanel pnl_Notice;
     protected javax.swing.JTabbedPane tbctrl_Main;
     // End of variables declaration//GEN-END:variables
 }
