 package moses.client.abstraction;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import moses.client.abstraction.apks.ApkInstallObserver;
 import moses.client.abstraction.apks.ExternalApplication;
 import moses.client.abstraction.apks.InstallApkActivity;
 import moses.client.com.ConnectionParam;
 import moses.client.com.NetworkJSON.BackgroundException;
 import moses.client.com.ReqTaskExecutor;
 import moses.client.com.requests.RequestDownloadlink;
 import moses.client.com.requests.RequestGetListAPK;
 import moses.client.com.requests.RequestLogin;
 import moses.client.service.MosesService;
 import moses.client.service.helpers.EHookTypes;
 import moses.client.service.helpers.EMessageTypes;
 import moses.client.service.helpers.Executor;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.Handler;
 import android.util.Log;
 
 public class ApkMethods {
 
 	private static class RequestDownloadLinkExecutor implements ReqTaskExecutor {
 		private ApkDownloadLinkRequestObserver observer;
 		private ExternalApplication app;
 
 		public RequestDownloadLinkExecutor(
 				ApkDownloadLinkRequestObserver observer, ExternalApplication app) {
 			this.observer = observer;
 			this.app = app;
 		}
 
 		@Override
 		public void handleException(Exception e) {
 			observer.apkDownloadLinkRequestFailed(e);
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
			Log.d("MoSeS.APK", "DownloadLinkRequest response: " + s);
 			try {
 				j = new JSONObject(s);
 				if (RequestDownloadlink.downloadLinkRequestAccepted(j)) {
 
 					String url = j.getString("URL");
 					observer.apkDownloadLinkRequestFinished(url, app);
 
 				} else {
 					Log.d("MoSeS.APK_METHODS",
							"Download link Request not successful! Server returned negative response: "+s);
					this.handleException(new RuntimeException("Download link Request not successful! Server returned negative response: "+s));
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	public static void getDownloadLinkFor(final ExternalApplication app,
 			final ApkDownloadLinkRequestObserver observer) {
 
 		if (MosesService.getInstance() != null)
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTDOWNLOADLINK, new Executor() {
 
 				@Override
 				public void execute() {
 					new RequestDownloadlink(new RequestDownloadLinkExecutor(
 							observer, app), RequestLogin.getSessionID(), app
 							.getID()).send();
 				}
 			});
 	}
 
 	private static class RequestApkListExecutor implements ReqTaskExecutor {
 		private ApkListRequestObserver observer;
 
 		public RequestApkListExecutor(ApkListRequestObserver observer) {
 			this.observer = observer;
 		}
 
 		@Override
 		public void handleException(Exception e) {
 			observer.apkListRequestFailed(e);
 		}
 
 		@Override
 		public void postExecution(String s) {
 			JSONObject j = null;
 			try {
 				Log.d("MOSES.APK", "APK-List response recived:"+s);
 				j = new JSONObject(s);
 				if (RequestGetListAPK.isListRetrived(j)) {
 
 					List<ExternalApplication> apps = new LinkedList<ExternalApplication>();
 
 					JSONArray apkInformations = j.getJSONArray("APK_LIST");
 					for (int i = 0; i < apkInformations.length(); i++) {
 						JSONObject apkInformation = apkInformations
 								.getJSONObject(i);
 						String id = apkInformation.getString("ID");
 						String name = apkInformation.getString("NAME");
 						String description = apkInformation.getString("DESCR");
 						JSONArray sensorsArray = apkInformation.getJSONArray("SENSORS");
 						List<Integer> resultSensors = new LinkedList<Integer>();
 						for(int k=0; k<sensorsArray.length(); k++) {
 							resultSensors.add(sensorsArray.getInt(k));
 						}
 
 						ExternalApplication externalApplication = new ExternalApplication(
 								id);
 						externalApplication.setName(name);
 						externalApplication.setDescription(description);
 						externalApplication.setSensors(resultSensors);
 						apps.add(externalApplication);
 					}
 
 					observer.apkListRequestFinished(apps);
 
 				} else {
 					observer.apkListRequestFailed(new RuntimeException("invalid response: " + j.toString()));
 				}
 			} catch (JSONException e) {
 				this.handleException(e);
 			}
 		}
 
 		@Override
 		public void updateExecution(BackgroundException c) {
 			if (c.c == ConnectionParam.EXCEPTION) {
 				handleException(c.e);
 			}
 		}
 	}
 
 	public static void getExternalApplications(
 			final ApkListRequestObserver observer) {
 
 		if (MosesService.getInstance() != null) {
 			MosesService.getInstance().executeLoggedIn(EHookTypes.POSTLOGINSUCCESS, EMessageTypes.REQUESTGETLISTAPK, new Executor() {
 
 				@Override
 				public void execute() {
 					Log.d("MoSeS.APKMETHODS", "requesting apk list");
 					new RequestGetListAPK(new RequestApkListExecutor(observer),
 							RequestLogin.getSessionID()).send();
 				}
 			});
 		}
 	}
 
 	/**
 	 * Installs a given apk file.
 	 * 
 	 * @param apk the apk file to install
 	 * @param baseActivity the base activity
 	 * @throws IOException if the file does not exist
 	 */
 	public static void installApk(File apk, ExternalApplication appRef, ApkInstallObserver o) throws IOException {
 		MosesService service = MosesService.getInstance();
 		if(service != null) {
 			if (apk.exists()) {
 				InstallApkActivity.setAppToInstall(apk, appRef, o);
 				Intent installActivityIntent = new Intent(service, InstallApkActivity.class);
 				installActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				service.startActivity(installActivityIntent);
 			} else {
 				o.apkInstallError(apk, appRef, new RuntimeException("Could not install apk file because it was nonexistent."));
 			}
 		} else {
 			o.apkInstallError(apk, appRef, new RuntimeException("Could not install apk file because the service was not running."));
 		}
 	}
 
 	public static void startApplication(String packageName,
 			Activity baseActivity) throws NameNotFoundException {
 		Intent intent = baseActivity.getApplicationContext()
 				.getPackageManager().getLaunchIntentForPackage(packageName);
 		baseActivity.startActivity(intent);
 	}
 
 	/**
 	 * Retrieves the package name from an apk file
 	 * 
 	 * @param apk
 	 *            the file
 	 * @param appContext
 	 *            the application context
 	 * @return the package name
 	 * @throws IOException
 	 *             if the apk file could not be parsed correctly
 	 */
 	public static String getPackageNameFromApk(File apk, Context appContext)
 			throws IOException {
 		PackageInfo appInfo = appContext.getPackageManager()
 				.getPackageArchiveInfo(apk.getAbsolutePath(), 0);
 		if (appInfo != null) {
 			return appInfo.packageName;
 		} else {
 			throw new IOException(
 					"the given package could not be parsed correctly");
 		}
 	}
 
 	/**
 	 * checks whether an application is installed or not
 	 * 
 	 * @param packageName
 	 *            the package name for the application
 	 * @param context
 	 * @return
 	 */
 	public static boolean isApplicationInstalled(String packageName,
 			Context context) {
 		try {
 			context.getPackageManager().getApplicationInfo(packageName, 0);
 		} catch (NameNotFoundException e) {
 			return false;
 		}
 		return true;
 	}
 
 }
