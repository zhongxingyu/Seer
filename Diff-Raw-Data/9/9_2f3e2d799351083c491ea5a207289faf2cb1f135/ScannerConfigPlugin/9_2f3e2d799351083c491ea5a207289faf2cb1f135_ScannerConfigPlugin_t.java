 package edu.ualberta.med.scannerconfig;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 import edu.ualberta.med.scanlib.ScanCell;
 import edu.ualberta.med.scanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ScannerConfigPlugin extends AbstractUIPlugin {
 
     // The plug-in ID
     public static final String PLUGIN_ID = "scannerConfig";
 
     // The shared instance
     private static ScannerConfigPlugin plugin;
 
     /**
      * The constructor
      */
     public ScannerConfigPlugin() {
         String osname = System.getProperty("os.name");
         if (osname.startsWith("Windows")) {
             System.loadLibrary("scanlib");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
      * )
      */
     @Override
     public void start(BundleContext context) throws Exception {
         super.start(context);
         plugin = this;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
      * )
      */
     @Override
     public void stop(BundleContext context) throws Exception {
         plugin = null;
         super.stop(context);
     }
 
     /**
      * Returns the shared instance
      * 
      * @return the shared instance
      */
     public static ScannerConfigPlugin getDefault() {
         return plugin;
     }
 
     public void initialize() throws Exception {
         IniValidator iv = new IniValidator();
         iv.loadFromFile();
     }
 
     public static ScanCell[][] scan(int plateNumber) throws Exception {
         String dpiString = getDefault().getPreferenceStore().getString(
             PreferenceConstants.SCANNER_DPI);
         if (dpiString.length() == 0) {
             throw new Exception("bad value in preferences for scanner DPI");
         }
         int dpi = Integer.valueOf(dpiString);
        int res = ScanLib.getInstance().slDecodePlate(dpi, plateNumber, 0);
         if (res < ScanLib.SC_SUCCESS) {
             throw new Exception("Could not decode image. "
                 + ScanLib.getErrMsg(res));
         }
         return ScanCell.getScanLibResults();
     }
 
     public boolean getPalletEnabled(int palletId) {
         Assert.isTrue((palletId > 0)
             && (palletId <= PreferenceConstants.SCANNER_PALLET_ENABLED.length),
             "pallet id is invalid: " + palletId);
         return getPreferenceStore().getBoolean(
             PreferenceConstants.SCANNER_PALLET_ENABLED[palletId - 1]);
     }
 
     public int getPalletCount() {
         int result = 0;
         for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; ++i) {
             if (getPreferenceStore().getBoolean(
                 PreferenceConstants.SCANNER_PALLET_ENABLED[i]))
                 ++result;
         }
         return result;
     }
 
     public static int getPalletsMax() {
         return PreferenceConstants.SCANNER_PALLET_ENABLED.length;
     }
 
     public int getDpi() {
         return getPreferenceStore().getInt(PreferenceConstants.SCANNER_DPI);
     }
 
     public int getBrightness() {
         return getPreferenceStore().getInt(
             PreferenceConstants.SCANNER_BRIGHTNESS);
     }
 
     public int getContrast() {
         return getPreferenceStore()
             .getInt(PreferenceConstants.SCANNER_CONTRAST);
     }
 
     /**
      * Display an error message asynchronously
      */
     public static void openAsyncError(final String title, final String message) {
         Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 MessageDialog.openError(PlatformUI.getWorkbench()
                     .getActiveWorkbenchWindow().getShell(), title, message);
             }
         });
     }
 }
