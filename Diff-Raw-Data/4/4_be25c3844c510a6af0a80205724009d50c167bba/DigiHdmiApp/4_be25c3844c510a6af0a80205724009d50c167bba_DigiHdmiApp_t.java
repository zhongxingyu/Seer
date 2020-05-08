 /*
  * DigiHdmiApp.java
  */
 package com.intelix.digihdmi.app;
 
 import com.intelix.digihdmi.app.views.dialogs.SynchronizationDlg;
 import com.intelix.digihdmi.app.views.dialogs.DeviceConnectionDlg;
 import com.intelix.digihdmi.app.actions.*;
 import com.intelix.digihdmi.app.tasks.LoadIconsForInputTask;
 import com.intelix.digihdmi.app.tasks.LoadIconsForOutputTask;
 import com.intelix.digihdmi.app.views.*;
 import com.intelix.digihdmi.model.Device;
 import com.intelix.net.Connection;
 import com.intelix.net.IPConnection;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.event.MouseInputAdapter;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.Task.InputBlocker;
 
 /**
  * The main class of the application.
  */
 public class DigiHdmiApp extends SingleFrameApplication implements PropertyChangeListener {
 
     JComponent currentView;
     JComponent homeView;
     JComponent connectorView;
     JComponent connectorSelectionView;
     JComponent presetView;
     JComponent matrixView;
     JComponent lockView;
     JComponent adminView;
     JComponent passwordView;
     JComponent connectorChangeView;
     JComponent inputIconSelectionView;
     JComponent outputIconSelectionView;
 
     JDialog deviceConnectionDlg;
     JDialog syncDlg;
     
     FrameView mainFrame;
     Device device;
 
     public DigiHdmiApp() {}
 
     public JComponent getCurrentView() {
         return currentView;
     }
 
     public void setCurrentView(JComponent currentView) {
         this.currentView = currentView;
     }
 
     public Device getDevice() {
         return device;
     }
 
     public void setDevice(Device d) {
         device = d;
     }
     
     /**
      * At startup create and show the main frame of the application.
      */
     @Override
     protected void startup() {
         device = new Device();
 
         mainFrame = new DigiHdmiAppMainView(this);
         initializeComponents();
 
         show(mainFrame);
         showHomeView();
         mainFrame.getFrame().setMinimumSize(new Dimension(700, 400));
         mainFrame.getFrame().setPreferredSize(new Dimension(700, 400));
         mainFrame.getFrame().setLocationRelativeTo(null);
     }
 
     @Override
     protected void ready() {
         super.ready();
     }
 
     /**
      * This method is to initialize the specified window by injecting resources.
      * Windows shown in our application come fully initialized from the GUI
      * builder, so this additional configuration is not needed.
      */
     @Override
     protected void configureWindow(java.awt.Window root) {
     }
 
     /**
      * A convenient static getter for the application instance.
      * @return the instance of DigiHdmiApp
      */
     public static DigiHdmiApp getApplication() {
         return (DigiHdmiApp) Application.getInstance(DigiHdmiApp.class);
     }
 
     /**
      * Main method launching the application.
      */
     public static void main(String[] args) {
        System.setProperty("java.util.logging.config.file", "com/intelix/digihdmiapp/logging.properties");
        Logger.getLogger("com.intelix.digihdmi.app.DigiHdmiApp").info("Starting up!");
         launch(DigiHdmiApp.class, args);
     }
 
     private void initializeComponents() {
 
         connectorView = new ButtonListView();
         connectorSelectionView = new ConnectorSelectionView();
         presetView = new PresetLoadListView();
         Action a = getContext().getActionMap().get("showAndLoadPresetListView");
         ((PresetLoadListView)presetView).setBtnRefreshAction(a);
         homeView = new HomePanel();
         //int numOuts = getDevice().getNumOutputs();
 
         ActionMap aa = getContext().getActionMap(new AdminActions());
         ActionMap connectorMap = getContext().getActionMap(new ConnectorActions());
 
         ((HomePanel)homeView).setLockAction(getContext().getActionMap(new LockActions()).get("lock"));
         ((HomePanel)homeView).setPresetViewAction(getContext().getActionMap().get("showPresetListView"));
         ((HomePanel)homeView).setRoomViewAction(getContext().getActionMap().get("showOutputListView"));
         ((HomePanel)homeView).setAdminAction(aa.get("unlockUtilView"));
         ((HomePanel)homeView).setMatrixViewAction(getContext().getActionMap().get("showAndLoadMatrixView"));
 
         lockView = new LockView();
         ((LockView)lockView).setUnlockAction(getContext().getActionMap(new LockActions()).get("unlock"));
 
         matrixView = new MatrixView();
         ((MatrixView)matrixView).getMatrixPanel().setDefaultButtonAction(
             getContext().getActionMap(new MatrixActions()).get("setConnection")
         );
         ((MatrixView)matrixView).getMatrixPanel().setBtnRefreshAction(
                 getContext().getActionMap().get("showAndLoadMatrixView"));
 
         adminView = new AdminPanel();
         ((AdminPanel)adminView).setBtnPsswdAction(getContext().getActionMap().get("showPasswdView"));
         ((AdminPanel)adminView).setBtnDefineInputsAction(connectorMap.get("showInputListForCustomization"));
         ((AdminPanel)adminView).setBtnDefineOutputsAction(connectorMap.get("showOutputListForCustomization"));
 
         passwordView = new PasswordChangePanel();
         ((PasswordChangePanel)passwordView).setBtnAdminPsswdAction(
                 aa.get("setAdminPassword"));
         ((PasswordChangePanel)passwordView).setBtnUnlockPsswdAction(
                 aa.get("setUnlockPassword"));
 
         connectorChangeView = new CustomizeConnectorPanel();
         ((CustomizeConnectorPanel)connectorChangeView).setBtnDefIconAction(
                 connectorMap.get("showIconChoices"));
         ((CustomizeConnectorPanel)connectorChangeView).setBtnDefTextAction(
                 connectorMap.get("assignNewName"));
 
         inputIconSelectionView = new InputIconListView();
         getContext().getTaskService().execute(new LoadIconsForInputTask(this,
                 (IconContainerPanel)((ButtonListView)inputIconSelectionView).getButtonsPanel()));
         outputIconSelectionView = new OutputIconListView();
         getContext().getTaskService().execute(new LoadIconsForOutputTask(this,
                 (IconContainerPanel)((ButtonListView)outputIconSelectionView).getButtonsPanel()));
 
         // Set up menu actions
         ActionMap menuActionMap = getContext().getActionMap(new MenuActions());
         ActionMap deviceActionMap = getContext().getActionMap(new DeviceActions());
         ((DigiHdmiAppMainView) mainFrame).setConnectMenuItemAction(deviceActionMap.get("toggleDeviceConnect"));
         ((DigiHdmiAppMainView) mainFrame).setOptionsMenuItemAction(menuActionMap.get("onDeviceSettings"));
         ((DigiHdmiAppMainView) mainFrame).setDeviceMenuAction(menuActionMap.get("menuDevice"));
         ((DigiHdmiAppMainView) mainFrame).setResetCacheMenuItemAction(menuActionMap.get("resetCache"));
         ((DigiHdmiAppMainView) mainFrame).setFileLoadMenuItemAction(menuActionMap.get("onFileLoad"));
         ((DigiHdmiAppMainView) mainFrame).setFileSaveMenuItemAction(menuActionMap.get("onFileSave"));
 
         // Set up dialogs
         deviceConnectionDlg = new DeviceConnectionDlg(mainFrame.getFrame());
         ActionMap deviceCxnMap = getContext().getActionMap(new ConnectionDialogActions());
         ((DeviceConnectionDlg)deviceConnectionDlg).setBtnTestAction(deviceCxnMap.get("onTest"));
         ((DeviceConnectionDlg)deviceConnectionDlg).setBtnConnectAction(deviceCxnMap.get("onConnect"));
         ((DeviceConnectionDlg)deviceConnectionDlg).setBtnOkAction(deviceCxnMap.get("onOk"));
         ((DeviceConnectionDlg)deviceConnectionDlg).setBtnCancelAction(deviceCxnMap.get("onCancel"));
         
         syncDlg = new SynchronizationDlg(mainFrame.getFrame());
         ActionMap syncMap = getContext().getActionMap(new SynchronizationActions());
         ((SynchronizationDlg)syncDlg).setBtnDisconnectAction(syncMap.get("onCancel"));
         ((SynchronizationDlg)syncDlg).setBtnNoAction(syncMap.get("onPull"));
         ((SynchronizationDlg)syncDlg).setBtnYesAction(syncMap.get("onPush"));
 
         addDeviceListener(device);
     }
 
     public void addDeviceListener(Device d)
     {
         // Listen to the device for connection change information
         d.addPropertyChangeListener(this);
     }
     @Override
     public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals("connected"))
         {
             boolean connected = device.isConnected();
             ((DigiHdmiAppMainView)mainFrame).getMenuItemConnected().setText(
                     connected ? "Disconnect" : "Connect");
 
             ((DeviceConnectionDlg)deviceConnectionDlg).getConnectButton().setText(
                     connected ? "Disconnect" : "Connect");
             ((DeviceConnectionDlg)deviceConnectionDlg).getConnectButton().setSelected(
                     connected);
         }
     }
 
     @org.jdesktop.application.Action
     public void showHomeView() {
         showPanel(homeView, "Home");
     }
 
     @org.jdesktop.application.Action
     public void showOutputListView() {
         ((ButtonListView) connectorView).getButtonsPanel().clear();
         showPanel(connectorView, "Room View", new ConnectorActions(), "showOutputList");
     }
 
     @org.jdesktop.application.Action
     public void showInputSelectionView() {
         ((ButtonListView) connectorSelectionView).getButtonsPanel().clear();
         showPanel(connectorSelectionView, "Inputs View");
     }
 
     @org.jdesktop.application.Action
     public void showInputCustomizationView() {
         ((ButtonListView) connectorView).getButtonsPanel().clear();
         showPanel(connectorView, "Define Inputs");
     }
 
     @org.jdesktop.application.Action
     public void showOutputCustomizationView() {
         ((ButtonListView) connectorView).getButtonsPanel().clear();
         showPanel(connectorView, "Define Outputs");
     }
 
     @org.jdesktop.application.Action
     public void showMatrixView() {
         showPanel(matrixView, "Matrix View");
         //JOptionPane.showMessageDialog(null, "Ah ah ah! Not so fast! This isn't implemented yet.");
     }
 
     @org.jdesktop.application.Action
     public void showAndLoadMatrixView() {
         showPanel(matrixView, "Matrix View", new MatrixActions(), "loadMatrix");
         //JOptionPane.showMessageDialog(null, "Ah ah ah! Not so fast! This isn't implemented yet.");
     }
 
     @org.jdesktop.application.Action
     public void showPresetListView() {
         ((ButtonListView) presetView).getButtonsPanel().clear();
         showPanel(presetView, "Preset View", new PresetActions(), "showPresetListForLoad");
     }
 
     @org.jdesktop.application.Action
     public void showAndLoadPresetListView() {
         ((ButtonListView) presetView).getButtonsPanel().clear();
         device.setPresetReset(true);
         showPanel(presetView, "Preset View", new PresetActions(), "showPresetListForLoad");
     }
 
     @org.jdesktop.application.Action
     public void showPresetSaveView() {
         ((ButtonListView) presetView).getButtonsPanel().clear();
         showPanel(presetView, "Preset View", new PresetActions(), "showPresetListForSave");
     }
 
     @org.jdesktop.application.Action
     public void showInputChangeView()
     {
         showPanel(connectorChangeView, "Define Input");
     }
 
     @org.jdesktop.application.Action
     public void showOutputChangeView()
     {
         showPanel(connectorChangeView, "Define Output");
     }
 
     @org.jdesktop.application.Action
     public void showInputIconChoicePanel()
     {
         showPanel(inputIconSelectionView, "Choose Input Icon");
     }
 
     @org.jdesktop.application.Action
     public void showOutputIconChoicePanel()
     {
         showPanel(outputIconSelectionView, "Choose Output Icon");
     }
 
     private void showPanel(JComponent panel, String title) {
         currentView = panel;
         mainFrame.setComponent(currentView);
         mainFrame.getFrame().setTitle(title);
 
         mainFrame.getFrame().setVisible(true);
         mainFrame.getFrame().repaint();
     }
 
     private void showPanel(JComponent panel, String title, Object actionsInstance, String actionName) {
         showPanel(panel, title);
 
         ActionMap m = getContext().getActionMap(actionsInstance);
         javax.swing.Action getoutputs = m.get(actionName);
         getoutputs.actionPerformed(new ActionEvent(this, 1001, ""));
     }
 
     @org.jdesktop.application.Action
     public void lockApp() {
         //JOptionPane.showMessageDialog(null, "Ah ah ah! Not so fast! This isn't implemented yet.");
 
         // TODO: add a confirmation dialog
         showPanel(lockView, "Lock View");
     }
     
     @org.jdesktop.application.Action
     public void showUtilView() {
         showPanel(adminView, "Utilities");
     }
 
     @org.jdesktop.application.Action
     public void showPasswdView() {
         showPanel(passwordView, "Change Passwords");
     }
 
     @org.jdesktop.application.Action
     public void showSyncDlg()
     {
         syncDlg.setVisible(true);
     }
     
     @org.jdesktop.application.Action
     public void hideSyncDlg()
     {
         syncDlg.setVisible(false);
     }
 
     @org.jdesktop.application.Action
     public void showConnectionDlg()
     {
         DeviceConnectionDlg dlg = (DeviceConnectionDlg) deviceConnectionDlg;
 
         Connection c = device.getConnection();
         if (c instanceof IPConnection)
         {
             dlg.setIpAddr(((IPConnection)c).getIpAddr());
             dlg.setPort(((IPConnection)c).getPort());
             //dlg.toggleConnectButton(c.isConnected());
         }
 
         deviceConnectionDlg.setVisible(true);
     }
 
     @org.jdesktop.application.Action
     public void hideConnectionDlg()
     {
         deviceConnectionDlg.setVisible(false);
     }
 
     public DeviceConnectionDlg getConnectionDlg() {
         return (DeviceConnectionDlg)deviceConnectionDlg;
     }
 
     public SynchronizationDlg getSyncDlg() {
         return (SynchronizationDlg)syncDlg;
     }
 
     public class BusyInputBlocker extends InputBlocker
     {
         public BusyInputBlocker(Task t) {
             super(t, Task.BlockingScope.WINDOW,mainFrame.getFrame().getGlassPane());
             mainFrame.getFrame().getGlassPane().addMouseListener(new MouseInputAdapter() {});
         }
 
         @Override
         protected void block() {
             getMainFrame().getGlassPane().setVisible(true);
             getMainFrame().getGlassPane().requestFocusInWindow();
         }
 
         @Override
         protected void unblock() {
             getMainFrame().getGlassPane().setVisible(false);
         }
     }
 }
