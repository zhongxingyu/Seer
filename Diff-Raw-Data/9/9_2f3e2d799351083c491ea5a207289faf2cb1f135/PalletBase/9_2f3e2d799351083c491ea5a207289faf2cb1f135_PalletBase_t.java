 package edu.ualberta.med.scannerconfig.preferences.scanner;
 
 import java.io.File;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.PlatformUI;
 
 import edu.ualberta.med.scanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.PalletImageManager;
 import edu.ualberta.med.scannerconfig.ScannerConfigPlugin;
 import edu.ualberta.med.scannerconfig.ScannerRegion;
 import edu.ualberta.med.scannerconfig.preferences.DoubleFieldEditor;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 
 public class PalletBase extends FieldEditorPreferencePage implements
     IWorkbenchPreferencePage {
 
     protected int palletId;
 
     private Text[] textControls;
 
     private PalletImageManager palletImageManager;
 
     private boolean calibrated;
 
     private ScannerRegion origScannerRegion;
 
     private Label statusLabel;
 
     private Canvas canvas;
 
     public PalletBase(int palletId) {
         super(GRID);
         this.palletId = palletId;
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
         textControls = new Text[4];
         calibrated = false;
     }
 
     @Override
     protected Control createContents(final Composite parent) {
         Composite top = new Composite(parent, SWT.NONE);
         top.setLayout(new GridLayout(2, false));
         top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
         File palletsFile = new File(PalletImageManager.PALLET_IMAGE_FILE);
         if (palletsFile.exists()) {
             palletsFile.delete();
         }
 
         Control s = super.createContents(top);
         GridData gd = (GridData) s.getLayoutData();
         if (gd == null) {
             gd = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
             s.setLayoutData(gd);
         }
         gd.verticalAlignment = SWT.BEGINNING;
 
         Composite right = new Composite(top, SWT.NONE);
         right.setLayout(new GridLayout(1, false));
         gd = new GridData(SWT.FILL, SWT.FILL, true, true);
         gd.widthHint = 200;
         right.setLayoutData(gd);
 
         createCanvasComp(right);
 
         statusLabel = new Label(right, SWT.BORDER);
         statusLabel
             .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 
         if (!ScannerConfigPlugin.getDefault().getPalletEnabled(palletId)) {
             statusLabel.setText("pallet not enabled");
         } else if (!palletsFile.exists()) {
             statusLabel.setText("scan required");
         } else {
             statusLabel.setText("adjust settings if required");
         }
 
         Button b = new Button(right, SWT.NONE);
         b.setText("Scan");
         b.addSelectionListener(new SelectionListener() {
 
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
             }
 
             @Override
             public void widgetSelected(SelectionEvent e) {
                 statusLabel.setText("scanning...");
                 BusyIndicator.showWhile(parent.getDisplay(), new Runnable() {
                     @Override
                     public void run() {
                         final int result = ScanLib.getInstance().slScanImage(
                             (int) PalletImageManager.PALLET_IMAGE_DPI, 0, 0,
                             20, 20, PalletImageManager.PALLET_IMAGE_FILE);
 
                         parent.getDisplay().asyncExec(new Runnable() {
                             public void run() {
                                 if (result != ScanLib.SC_SUCCESS) {
                                     MessageDialog.openError(PlatformUI
                                         .getWorkbench()
                                         .getActiveWorkbenchWindow().getShell(),
                                         "Scanner error", ScanLib
                                             .getErrMsg(result));
                                     return;
                                 }
 
                                 File palletsFile = new File(
                                     PalletImageManager.PALLET_IMAGE_FILE);
                                 if (palletsFile.exists()) {
                                     for (int i = 0; i < 4; ++i) {
                                         textControls[i].setEnabled(true);
                                     }
                                     if (palletImageManager != null) {
                                         canvas.redraw();
                                         canvas.update();
                                     }
                                 }
                             }
                         });
                     }
                 });
                 statusLabel.setText("adjust settings if required");
             }
         });
 
         if (System.getProperty("os.name").startsWith("Windows")
             && !palletsFile.exists()) {
             for (int i = 0; i < 4; ++i) {
                 textControls[i].setEnabled(false);
             }
         }
 
         return top;
     }
 
     @Override
     protected void createFieldEditors() {
         DoubleFieldEditor fe;
         String[] labels = { "Left", "Top", "Right", "Bottom" };
 
         BooleanFieldEditor bfe = new BooleanFieldEditor(
             PreferenceConstants.SCANNER_PALLET_ENABLED[palletId - 1], "Enable",
             getFieldEditorParent());
         addField(bfe);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1];
 
         for (int i = 0; i < 4; ++i) {
             fe = new DoubleFieldEditor(prefsArr[i], labels[i] + ":",
                 getFieldEditorParent());
             fe.setValidRange(0, 20);
             addField(fe);
             textControls[i] = fe.getTextControl(getFieldEditorParent());
             textControls[i].addModifyListener(new ModifyListener() {
                 @Override
                 public void modifyText(ModifyEvent e) {
                     if (palletImageManager == null)
                         return;
                     try {
                         Text source = (Text) e.getSource();
 
                         if (source == textControls[0]) {
                             palletImageManager.assignRegionLeft(Double
                                 .parseDouble(source.getText()));
                         } else if (source == textControls[1]) {
                             palletImageManager.assignRegionTop(Double
                                 .parseDouble(source.getText()));
                         } else if (source == textControls[2]) {
                             palletImageManager.assignRegionRight(Double
                                 .parseDouble(source.getText()));
                         } else if (source == textControls[3]) {
                             palletImageManager.assignRegionBottom(Double
                                 .parseDouble(source.getText()));
                         }
 
                         double left = Double.valueOf(textControls[0].getText());
                         double top = Double.valueOf(textControls[1].getText());
                         double right = Double
                             .valueOf(textControls[2].getText());
                         double bottom = Double.valueOf(textControls[3]
                             .getText());
 
                         ScannerRegion newRegion = new ScannerRegion(""
                             + palletId, left, top, right, bottom);
 
                         setValid(origScannerRegion.equal(newRegion));
                         if (!origScannerRegion.equal(newRegion)) {
                             statusLabel.setText("changes need to be applied");
                         }
                     } catch (NumberFormatException ex) {
                         // do nothing
                     }
                 }
             });
         }
 
     }
 
     @Override
     public void setValid(boolean enable) {
         if (System.getProperty("os.name").startsWith("Windows"))
             super.setValid(enable);
         getContainer().updateButtons();
         Button applyButton = getApplyButton();
         if (applyButton != null)
             applyButton.setEnabled(true);
     }
 
     private void createCanvasComp(Composite parent) {
         canvas = new Canvas(parent, SWT.BORDER);
         canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
         IPreferenceStore prefs = ScannerConfigPlugin.getDefault()
             .getPreferenceStore();
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1];
 
         origScannerRegion = new ScannerRegion("" + palletId,
             ScannerConfigPlugin.getDefault().getPreferenceStore().getDouble(
                 prefsArr[0]), prefs.getDouble(prefsArr[1]), prefs
                 .getDouble(prefsArr[2]), prefs.getDouble(prefsArr[3]));
 
         Color c = null;
 
         switch (palletId) {
         case 1:
             c = new Color(Display.getDefault(), 0, 0xFF, 0);
             break;
         case 2:
             c = new Color(Display.getDefault(), 0xFF, 0, 0);
             break;
         case 3:
             c = new Color(Display.getDefault(), 0xFF, 0xFF, 0);
             break;
         case 4:
             c = new Color(Display.getDefault(), 0, 0xFF, 0xFF);
             break;
         case 5:
             c = new Color(Display.getDefault(), 0, 0, 0xFF);
             break;
         default:
             Assert.isTrue(false, "Invalid value for palletId: " + palletId);
         }
 
         palletImageManager = new PalletImageManager(canvas, new ScannerRegion(
             origScannerRegion), c, textControls);
     }
 
     @Override
     public void init(IWorkbench workbench) {
         setPreferenceStore(ScannerConfigPlugin.getDefault()
             .getPreferenceStore());
     }
 
     @Override
     public void performApply() {
         super.performOk();
 
         if (!getPreferenceStore().getBoolean(
             PreferenceConstants.SCANNER_PALLET_ENABLED[palletId - 1]))
             return;
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_COORDS[palletId - 1];
 
         ScannerRegion newRegion = new ScannerRegion("" + palletId,
             getPreferenceStore().getDouble(prefsArr[0]), getPreferenceStore()
                 .getDouble(prefsArr[1]), getPreferenceStore().getDouble(
                 prefsArr[2]), getPreferenceStore().getDouble(prefsArr[3]));
 
         if (!calibrated || !origScannerRegion.equal(newRegion)) {
             ScanLib.getInstance().slConfigPlateFrame(palletId, newRegion.left,
                 newRegion.top, newRegion.right, newRegion.bottom);
             calibrate();
         }
     }
 
     private boolean calibrate() {
         statusLabel.setText("configuring pallet " + palletId + "...");
         String dpiString = ScannerConfigPlugin.getDefault()
             .getPreferenceStore().getString(PreferenceConstants.SCANNER_DPI);
 
         if (dpiString.length() == 0) {
             ScannerConfigPlugin.openAsyncError("Preferences Error",
                 "bad value in preferences for scanner DPI");
             return false;
         }
 
         final int dpi = Integer.valueOf(dpiString);
 
         BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
             public void run() {
                 int scanlibReturn = ScanLib.getInstance().slCalibrateToPlate(
                    dpi, palletId, 0);
                 calibrated = (scanlibReturn == ScanLib.SC_SUCCESS);
                 setValid(calibrated);
 
                 if (!calibrated) {
                     ScannerConfigPlugin.openAsyncError("Calibration Error",
                         ScanLib.getErrMsg(scanlibReturn));
                     ScanLib.getInstance().slConfigPlateFrame(palletId, 0, 0, 0,
                         0);
                 }
             }
         });
 
         statusLabel.setText(calibrated ? "configuration successful"
             : "configuration failed, try again");
         return calibrated;
     }
 }
