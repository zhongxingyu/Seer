 package edu.ualberta.med.scannerconfig;
 
 import java.net.URL;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.ui.services.ISourceProviderService;
 import org.osgi.framework.BundleContext;
 
 import edu.ualberta.med.scannerconfig.dmscanlib.DecodeResult;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanCell;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanLib;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanLibResult;
 import edu.ualberta.med.scannerconfig.dmscanlib.ScanRegion;
 import edu.ualberta.med.scannerconfig.preferences.PreferenceConstants;
 import edu.ualberta.med.scannerconfig.preferences.scanner.profiles.ProfileManager;
 import edu.ualberta.med.scannerconfig.preferences.scanner.profiles.ProfileSettings;
 import edu.ualberta.med.scannerconfig.sourceproviders.PlateEnabledState;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class ScannerConfigPlugin extends AbstractUIPlugin {
 
     public static final String IMG_SCANNER = "scanner";
 
     // The plug-in ID
     public static final String PLUGIN_ID = "scannerConfig";
 
     // The shared instance
     private static ScannerConfigPlugin plugin;
 
     /**
      * The constructor
      */
     public ScannerConfigPlugin() {
         String osname = System.getProperty("os.name");
         boolean isMsWindows = osname.startsWith("Windows");
         boolean isLinux = osname.startsWith("Linux");
 
         if (isMsWindows || isLinux) {
             if (isMsWindows) {
                 System.loadLibrary("OpenThreadsWin32");
                 System.loadLibrary("cxcore210");
                 System.loadLibrary("cv210");
             } else {
                 System.loadLibrary("OpenThreads");
                 System.loadLibrary("cxcore");
                 System.loadLibrary("cv");
             }
 
             String osarch = System.getProperty("os.arch");
 
            if (!isMsWindows && osarch.equals("x86")) {
                System.loadLibrary("dmscanlib");
            } else {
                 System.loadLibrary("dmscanlib64");
             }
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
 
         getPreferenceStore().addPropertyChangeListener(
             new IPropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent event) {
                     if (event.getProperty().startsWith(
                         "scanner.plate.coords.enabled.")) {
                         IWorkbenchWindow window = PlatformUI.getWorkbench()
                             .getActiveWorkbenchWindow();
                         ISourceProviderService service = (ISourceProviderService) window
                             .getService(ISourceProviderService.class);
 
                         PlateEnabledState plateEnabledSourceProvider = (PlateEnabledState) service
                             .getSourceProvider(PlateEnabledState.PLATES_ENABLED);
                         Assert.isNotNull(plateEnabledSourceProvider);
                         plateEnabledSourceProvider.setPlateEnabled();
                     }
                 }
             });
     }
 
     @Override
     protected void initializeImageRegistry(ImageRegistry registry) {
         registerImage(registry, IMG_SCANNER, "selectScanner.png");
     }
 
     private void registerImage(ImageRegistry registry, String key,
         String fileName) {
         try {
             IPath path = new Path("icons/" + fileName);
             URL url = FileLocator.find(getBundle(), path, null);
             if (url != null) {
                 ImageDescriptor desc = ImageDescriptor.createFromURL(url);
                 registry.put(key, desc);
             }
         } catch (Exception e) {
         }
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
 
     public void initialize() {
     }
 
     public static void scanImage(double left, double top, double right,
         double bottom, String filename) throws Exception {
         IPreferenceStore prefs = getDefault().getPreferenceStore();
 
         int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
         int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
 
         ScanLibResult res = ScanLib.getInstance().scanImage(debugLevel, dpi,
             brightness, contrast, new ScanRegion(left, top, right, bottom),
             filename);
 
         if (res.getResultCode() != ScanLib.SC_SUCCESS) {
             throw new Exception("Could not scan image: " + res.getMessage());
         }
     }
 
     public static void scanFlatbed(String filename) throws Exception {
         IPreferenceStore prefs = getDefault().getPreferenceStore();
 
         int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
         int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
 
         ScanLibResult res = ScanLib.getInstance().scanFlatbed(debugLevel, dpi,
             brightness, contrast, filename);
 
         if (res.getResultCode() != ScanLib.SC_SUCCESS) {
             throw new Exception("Could not scan flatbed: " + res.getMessage());
         }
     }
 
     public static void scanPlate(int plateNumber, String filename)
         throws Exception {
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateNumber - 1];
 
         IPreferenceStore prefs = getDefault().getPreferenceStore();
 
         ScanRegion region = new ScanRegion(prefs.getDouble(prefsArr[0]),
             prefs.getDouble(prefsArr[1]), prefs.getDouble(prefsArr[2]),
             prefs.getDouble(prefsArr[3]));
 
         regionModifyIfScannerWia(region);
 
         scanImage(region.getLeft(), region.getTop(), region.getRight(),
             region.getBottom(), filename);
     }
 
     public static List<ScanCell> decodePlate(int plateNumber, String profileName)
         throws Exception {
         IPreferenceStore prefs = getDefault().getPreferenceStore();
 
         int dpi = prefs.getInt(PreferenceConstants.SCANNER_DPI);
         int brightness = prefs.getInt(PreferenceConstants.SCANNER_BRIGHTNESS);
         int contrast = prefs.getInt(PreferenceConstants.SCANNER_CONTRAST);
         int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
         int edgeThresh = prefs.getInt(PreferenceConstants.LIBDMTX_EDGE_THRESH);
         double scanGap = prefs.getDouble(PreferenceConstants.LIBDMTX_SCAN_GAP);
         int squareDev = prefs.getInt(PreferenceConstants.LIBDMTX_SQUARE_DEV);
         int corrections = prefs.getInt(PreferenceConstants.LIBDMTX_CORRECTIONS);
         double cellDistance = prefs
             .getDouble(PreferenceConstants.LIBDMTX_CELL_DISTANCE);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateNumber - 1];
 
         ScanRegion region = new ScanRegion(prefs.getDouble(prefsArr[0]),
             prefs.getDouble(prefsArr[1]), prefs.getDouble(prefsArr[2]),
             prefs.getDouble(prefsArr[3]));
 
         double gapX = prefs.getDouble(prefsArr[4]);
         double gapY = prefs.getDouble(prefsArr[5]);
 
         int orientation = prefs.getString(
             PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateNumber - 1])
             .equals("Landscape") ? 0 : 1;
 
         regionModifyIfScannerWia(region);
 
         ProfileSettings profile = ProfileManager.instance().getProfile(
             profileName);
 
         int[] words = profile.toWords();
 
         DecodeResult res = ScanLib.getInstance().decodePlate(debugLevel, dpi,
             brightness, contrast, plateNumber, region, scanGap, squareDev,
             edgeThresh, corrections, cellDistance, gapX, gapY, words[0],
             words[1], words[2], orientation);
 
         if (res.getResultCode() != ScanLib.SC_SUCCESS) {
             throw new Exception("Could not decode plate: " + res.getMessage());
         }
         return res.getCells();
     }
 
     public static List<ScanCell> decodeImage(int plateNumber,
         String profileName, String filename) throws Exception {
         IPreferenceStore prefs = getDefault().getPreferenceStore();
 
         int debugLevel = prefs.getInt(PreferenceConstants.DLL_DEBUG_LEVEL);
         int edgeThresh = prefs.getInt(PreferenceConstants.LIBDMTX_EDGE_THRESH);
         double scanGap = prefs.getDouble(PreferenceConstants.LIBDMTX_SCAN_GAP);
         int squareDev = prefs.getInt(PreferenceConstants.LIBDMTX_SQUARE_DEV);
         int corrections = prefs.getInt(PreferenceConstants.LIBDMTX_CORRECTIONS);
         double cellDistance = prefs
             .getDouble(PreferenceConstants.LIBDMTX_CELL_DISTANCE);
 
         String[] prefsArr = PreferenceConstants.SCANNER_PALLET_CONFIG[plateNumber - 1];
 
         ScanRegion region = new ScanRegion(prefs.getDouble(prefsArr[0]),
             prefs.getDouble(prefsArr[1]), prefs.getDouble(prefsArr[2]),
             prefs.getDouble(prefsArr[3]));
 
         double gapX = prefs.getDouble(prefsArr[4]);
         double gapY = prefs.getDouble(prefsArr[5]);
 
         int orientation = prefs.getString(
             PreferenceConstants.SCANNER_PALLET_ORIENTATION[plateNumber - 1])
             .equals("Landscape") ? 0 : 1;
 
         regionModifyIfScannerWia(region);
 
         ProfileSettings profile = ProfileManager.instance().getProfile(
             profileName);
 
         int[] words = profile.toWords();
 
         DecodeResult res = ScanLib.getInstance()
             .decodeImage(debugLevel, plateNumber, filename, scanGap, squareDev,
                 edgeThresh, corrections, cellDistance, gapX, gapY, words[0],
                 words[1], words[2], orientation);
 
         if (res.getResultCode() != ScanLib.SC_SUCCESS) {
             throw new Exception("Could not decode image: " + res.getMessage());
         }
         return res.getCells();
     }
 
     public boolean getPlateEnabled(int plateId) {
         Assert.isTrue((plateId > 0)
             && (plateId <= PreferenceConstants.SCANNER_PALLET_ENABLED.length),
             "plate id is invalid: " + plateId);
         return getPreferenceStore().getBoolean(
             PreferenceConstants.SCANNER_PALLET_ENABLED[plateId - 1]);
     }
 
     private static void regionModifyIfScannerWia(ScanRegion region) {
         if (!ScannerConfigPlugin.getDefault().getPreferenceStore()
             .getString(PreferenceConstants.SCANNER_DRV_TYPE)
             .equals(PreferenceConstants.SCANNER_DRV_TYPE_WIA))
             return;
 
         region.setRight(region.getRight() - region.getLeft());
         region.setBottom(region.getBottom() - region.getTop());
     }
 
     public int getPlateCount() {
         int result = 0;
         for (int i = 0; i < PreferenceConstants.SCANNER_PALLET_ENABLED.length; ++i) {
             if (getPreferenceStore().getBoolean(
                 PreferenceConstants.SCANNER_PALLET_ENABLED[i]))
                 ++result;
         }
         return result;
     }
 
     public static int getPlatesMax() {
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
      * Display an error message
      */
     public static void openError(String title, String message) {
         MessageDialog.openError(PlatformUI.getWorkbench()
             .getActiveWorkbenchWindow().getShell(), title, message);
     }
 
     public static void openInformation(String title, String message) {
         MessageDialog.openInformation(PlatformUI.getWorkbench()
             .getActiveWorkbenchWindow().getShell(), title, message);
     }
 
     public static boolean openConfim(String title, String message) {
         return MessageDialog.openConfirm(PlatformUI.getWorkbench()
             .getActiveWorkbenchWindow().getShell(), title, message);
     }
 
     /**
      * Display an error message asynchronously
      */
     public static void openAsyncError(final String title, final String message) {
         Display.getDefault().asyncExec(new Runnable() {
             @Override
             public void run() {
                 MessageDialog.openError(PlatformUI.getWorkbench()
                     .getActiveWorkbenchWindow().getShell(), title, message);
             }
         });
     }
 
     public int getPlateNumber(String barcode, boolean realscan) {
         for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
             if (realscan
                 && !ScannerConfigPlugin.getDefault().getPlateEnabled(i + 1))
                 continue;
 
             String pref = getPreferenceStore().getString(
                 PreferenceConstants.SCANNER_PLATE_BARCODES[i]);
             Assert.isTrue(!pref.isEmpty(), "preference not assigned");
             if (pref.equals(barcode)) {
                 return i + 1;
             }
         }
         return -1;
     }
 
     public static int getPlatesEnabledCount(boolean realscan) {
         int count = 0;
         for (int i = 0; i < PreferenceConstants.SCANNER_PLATE_BARCODES.length; i++) {
             if (!realscan
                 || ScannerConfigPlugin.getDefault().getPlateEnabled(i + 1))
                 count++;
         }
         return count;
     }
 }
