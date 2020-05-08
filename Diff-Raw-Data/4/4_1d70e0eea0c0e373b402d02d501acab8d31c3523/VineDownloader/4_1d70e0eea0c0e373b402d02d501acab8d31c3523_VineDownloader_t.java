 package com.mohammadag.xposedvinedownloader;
 
 import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
 import static de.robv.android.xposed.XposedHelpers.findClass;
 import static de.robv.android.xposed.XposedHelpers.findConstructorExact;
 import static de.robv.android.xposed.XposedHelpers.getObjectField;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 
 import android.app.DownloadManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Environment;
 import android.util.SparseArray;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 import de.robv.android.xposed.IXposedHookLoadPackage;
 import de.robv.android.xposed.XC_MethodHook;
 import de.robv.android.xposed.XposedBridge;
 import de.robv.android.xposed.XposedHelpers;
 import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
 
 public class VineDownloader implements IXposedHookLoadPackage {
 
 	private long mPostId;
 	private Context mContext;
 	
 	private static int VINE_DOWNLOADER_RESULT = 99;
 	private static int VINE_POST_ID_ERROR = -91;
 	private static int VINE_ACTIVITY_RESULT = 999;
 	
 	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
 		if (!lpparam.packageName.equals("co.vine.android"))
 			return;
 		
 		Class<?> optionClass = findClass("co.vine.android.PostOptionsDialogActivity$Option", lpparam.classLoader);
 		final Constructor<?> optionConsturctor = findConstructorExact(optionClass, int.class, String.class);
 		final Class<?> postOptionsDialogActivityClass = findClass("co.vine.android.PostOptionsDialogActivity", lpparam.classLoader);
 		Class<?> optionArrayAdapterClass = findClass("co.vine.android.PostOptionsDialogActivity$OptionArrayAdapter", lpparam.classLoader);
 		
 		XposedBridge.hookAllConstructors(optionArrayAdapterClass, new XC_MethodHook() {
 			
 			@SuppressWarnings({ "rawtypes", "unchecked" })
 			@Override
 			protected void afterHookedMethod(MethodHookParam param)
 					throws Throwable {
 				mContext = (Context) param.args[0];
 				ArrayAdapter<?> array = (ArrayAdapter<?>) param.thisObject;
 				ArrayList localArrayList = new ArrayList();
 				localArrayList.add(optionConsturctor.newInstance(VINE_DOWNLOADER_RESULT, "Download"));
 				array.addAll(localArrayList);
 			}
 		});
 		
 		findAndHookMethod("co.vine.android.BaseTimelineFragment", lpparam.classLoader, "onActivityResult",
 				int.class, int.class, Intent.class, new XC_MethodHook() {
 			@SuppressWarnings("rawtypes")
 			@Override
 			protected void beforeHookedMethod(MethodHookParam param)
 					throws Throwable {
 				if (((Integer) param.args[1]).equals(VINE_ACTIVITY_RESULT)) {
 					Intent paramIntent = (Intent) param.args[2];
 					if (paramIntent != null) {
 						long postId = paramIntent.getLongExtra("post_id", VINE_POST_ID_ERROR);
 						if (postId != VINE_POST_ID_ERROR) {
 							Object feedAdapter = getObjectField(param.thisObject, "mFeedAdapter");
 							SparseArray vinePostArray = (SparseArray) getObjectField(feedAdapter, "mPosts");
 							Object vinePost = null;
 							if (vinePostArray != null) {
 								int key = 0;
 								for(int i = 0; i < vinePostArray.size(); i++) {
 									key = vinePostArray.keyAt(i);
 									Object obj = vinePostArray.get(key);
 									long localPostId = XposedHelpers.getLongField(obj, "postId");								
 									if (localPostId == postId) {
 										vinePost = obj;
 										break;
 									}
 								}
 								if (vinePost != null) {
 									String videoUrl = (String) getObjectField(vinePost, "videoUrl");
 									if (videoUrl != null && videoUrl.isEmpty()) {
 										videoUrl = (String) getObjectField(vinePost, "videoLowURL");
 									}
 									
 									if (!videoUrl.isEmpty()) {
 										Toast.makeText(mContext, "Downloading video", Toast.LENGTH_SHORT).show();
 										
 										String description = (String) getObjectField(vinePost, "description");
 										
 										File directory =
 												new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/Vine");
 										if (!directory.exists())
 											directory.mkdirs();
 										
 										DownloadManager.Request request = new DownloadManager.Request(Uri.parse(videoUrl));
 										request.setDescription(description);
 										request.setTitle("Vine Video");
 										request.allowScanningByMediaScanner();
 										request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
										request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Vine/" + description.replace("/", "") + ".mp4");
 
 										DownloadManager manager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
 										manager.enqueue(request);
 									}
 								}
 							}
 
 						}
 					}
 					
 					param.setResult(false);
 				}
 			}
 		});
 		
 		findAndHookMethod(postOptionsDialogActivityClass, "onListItemClick", 
 				ListView.class, View.class, int.class, long.class, new XC_MethodHook() {
 			
 			@Override
 			protected void beforeHookedMethod(MethodHookParam param)
 					throws Throwable {
 				View paramView = (View) param.args[1];
 				int tag = ((Integer)paramView.getTag()).intValue();
 				if (tag == VINE_DOWNLOADER_RESULT) {
 				    mPostId = (Long) getObjectField(param.thisObject, "mPostId");
 				    Intent localIntent = new Intent();
 				    localIntent.putExtra("post_id", mPostId);
 				    
 				    Method setResultMethod = postOptionsDialogActivityClass.getMethod("setResult", int.class, Intent.class);
 				    setResultMethod.invoke(param.thisObject, VINE_ACTIVITY_RESULT, localIntent);
 				    
 				    Method finishMethod = postOptionsDialogActivityClass.getMethod("finish");
 				    finishMethod.invoke(param.thisObject);
 				    
 				    param.setResult(false);
 				}
 			}
 		});
 	}
 }
