 package cc.warlock.rcp.application;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.ProgressMonitorDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.update.configuration.IConfiguredSite;
 import org.eclipse.update.configuration.ILocalSite;
 import org.eclipse.update.core.IFeature;
 import org.eclipse.update.core.IFeatureReference;
 import org.eclipse.update.core.ISite;
 import org.eclipse.update.core.SiteManager;
 import org.eclipse.update.core.VersionedIdentifier;
 import org.eclipse.update.operations.IInstallFeatureOperation;
 import org.eclipse.update.operations.OperationsManager;
 
 import cc.warlock.rcp.plugin.Warlock2Plugin;
 
 public class WarlockUpdates {
 
 	public static List<IFeatureReference> promptUpgrade (Map<IFeatureReference, VersionedIdentifier> newVersions)
 	{
 		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		WarlockUpdateDialog dialog = new WarlockUpdateDialog(shell, newVersions);
 		int response = dialog.open();
 		
 		if (response == Window.OK)
 		{
 			return dialog.getSelectedFeatures();
 		}
 		
 		return Collections.emptyList();
 	}
 	
 	
 	public static final String UPDATE_SITE = "warlock.updates.url";
 	public static final String AUTO_UPDATE = "warlock.updates.autoupdate";
 	
 	protected static Properties updateProperties;
 	protected static Properties getUpdateProperties ()
 	{
 		if (updateProperties == null)
 		{
 			updateProperties = new Properties();
 			try {
 				InputStream stream = FileLocator.openStream(Warlock2Plugin.getDefault().getBundle(), new Path("warlock-updates.properties"), false);
 				updateProperties.load(stream);
 				stream.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		return updateProperties;
 	}
 	
 	public static boolean autoUpdate ()
 	{
 		boolean autoUpdate = false;
 		Properties updateProperties = getUpdateProperties();
 		
 		if (updateProperties.containsKey(AUTO_UPDATE))
 		{
 			autoUpdate = Boolean.parseBoolean(updateProperties.getProperty(AUTO_UPDATE));
 		}
 		return autoUpdate;
 	}
 	
 	public static void checkForUpdates (final IProgressMonitor monitor)
 	{
 		try {
 			Properties properties = getUpdateProperties();
 			String url = properties.getProperty(UPDATE_SITE);
 			if (url == null)
 				return;
 			
 			ISite updateSite = SiteManager.getSite(new URL(url), monitor);
 			IFeatureReference[] featureRefs = updateSite.getFeatureReferences();
 			final ILocalSite localSite = SiteManager.getLocalSite();
 			final IConfiguredSite configuredSite = localSite.getCurrentConfiguration().getConfiguredSites()[0];
 			IFeatureReference[] localFeatureRefs = configuredSite.getConfiguredFeatures();
 			
 			final HashMap<IFeatureReference, VersionedIdentifier> newVersions  = new HashMap<IFeatureReference, VersionedIdentifier>();
 
 			for (int i = 0; i < featureRefs.length; i++) {
 				for (int j = 0; j < localFeatureRefs.length; j++) {
 
 					VersionedIdentifier featureVersion = featureRefs[i].getVersionedIdentifier();
 					VersionedIdentifier localFeatureVersion = localFeatureRefs[j].getVersionedIdentifier();
 
 					if (featureVersion.getIdentifier().equals(localFeatureVersion.getIdentifier())) {
 
 						if (featureVersion.getVersion().isGreaterThan(localFeatureVersion.getVersion())) {
 
 							newVersions.put(featureRefs[i], localFeatureVersion);
 						}
 					}
 				}
 			}
 
 			if (newVersions.size() > 0)
 			{
 				Display.getDefault().syncExec(new Runnable() {
 					public void run () {
 						final List<IFeatureReference> featuresToUpgrade = promptUpgrade(newVersions);
 						
 						ProgressMonitorDialog dialog = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
 						dialog.setBlockOnOpen(false);
 						dialog.open();
 						try {
 							dialog.run(true, true, new IRunnableWithProgress () {
 								public void run(IProgressMonitor monitor)
 										throws InvocationTargetException,
 										InterruptedException {
 									try {
 										for (IFeatureReference featureRef : featuresToUpgrade)
 										{
 											IFeature feature = featureRef.getFeature(monitor);
 											
 											IInstallFeatureOperation operation = OperationsManager.getOperationFactory().createInstallOperation(
 												configuredSite, feature, null, null, null);
 											
 											operation.execute(monitor, null);
 										}
 										if (featuresToUpgrade.size() > 0) {
 											localSite.save();
 											
 											IFeatureReference featureRef = featuresToUpgrade.get(0);
 											IFeature feature = featureRef.getFeature(monitor);
 											
 											boolean restart = MessageDialog.openQuestion(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 												"Restart Warlock?", 
 												"Warlock has finished downloading and updating to version " + feature.getVersionedIdentifier().getVersion() + "." +
 												" Would you like to restart Warlock for the changes to take effect?");
 											
 											if (restart)
 											{
 												PlatformUI.getWorkbench().restart();
 											}
 										}
 									} catch (CoreException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									} catch (InvocationTargetException e) {
 										// TODO Auto-generated catch block
 										e.printStackTrace();
 									}
 								}
 							});
 						} catch (InvocationTargetException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 				});
 			}
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 	}
 }
