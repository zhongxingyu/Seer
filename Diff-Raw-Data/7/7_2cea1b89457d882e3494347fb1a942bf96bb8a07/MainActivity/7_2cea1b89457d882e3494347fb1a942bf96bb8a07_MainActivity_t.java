 package com.unidevel.tools.locker;
 
 import android.app.Activity;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Html;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RemoteViews;
 import android.widget.TextView;
 
 import com.google.ads.AdRequest;
 import com.google.ads.AdSize;
 import com.google.ads.AdView;
 import com.unidevel.AppListActivity;
 import com.unidevel.util.DeviceUtil;
 import com.unidevel.util.RootUtil;
 
 public class MainActivity extends Activity
 {
 	MainActivity ctx=this;
 	boolean isRooted = false;
 	public void onCreate(Bundle bundle)
 	{
 		super.onCreate(bundle);
 		this.setContentView(R.layout.main);
 		isRooted = RootUtil.isRooted();
 		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		boolean useRoot = pref.getBoolean("root", isRooted);
 		pref.edit().putBoolean("root", useRoot).commit();
 
 		TextView changelog=(TextView) findViewById(R.id.changelog);
 		changelog.setText(Html.fromHtml(getString(R.string.changelog)));
 		showNotify(this);
 		CheckBox box=(CheckBox) this.findViewById(R.id.checkBoot);
 		box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
 
 				public void onCheckedChanged(CompoundButton button, boolean value)
 				{
 					SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 					pref.edit().putBoolean("boot",value).commit();
 				}
 			
 		});
 		loadSlots();
 		box.setChecked(pref.getBoolean("boot",true));
 		((CheckBox)findViewById(R.id.useRoot)).setChecked(useRoot);
		if ( !isRooted ) findViewById(R.id.useRoot).setEnabled(false);
 		else {
			findViewById(R.id.useRoot).setEnabled(true);
 			((CheckBox)findViewById(R.id.useRoot)).setOnCheckedChangeListener(new OnCheckedChangeListener(){
 
 				@Override
 				public void onCheckedChanged(CompoundButton button, boolean checked) {
 					pref.edit().putBoolean("root", checked).commit();
 					showNotify(ctx);
 				}
 			});
 		}
 		//ALocker a1505c63891818d
 		AdView adView = new AdView(this, AdSize.BANNER, "a1505c63891818d"); 
 		LinearLayout layout = (LinearLayout) findViewById(R.id.adLayout); 
 		layout.addView(adView);
 		AdRequest req = new AdRequest();
 		adView.loadAd(req);
 	}
 
 	private void loadSlots(){
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		String pkg = pref.getString("slot1.pkg","");
 		String name= pref.getString("slot1.name","");
 		TextView text=(TextView) findViewById(R.id.textSlot1);
 		if(pkg.length()==0)
 			text.setText(getString(R.string.add_shortcut));
 		else if(name!=null)
 			text.setText(name);
 		ImageView image = (ImageView) findViewById(R.id.imageSlot1);
 		Drawable icon=getAppIcon(this,pkg);
 		if(icon!=null)
 			image.setImageDrawable(icon);
 		else 
 			image.setImageResource(R.drawable.app);
 		View.OnClickListener l =new View.OnClickListener(){
 
 			public void onClick(View view)
 			{
 				Intent i=new Intent(ctx,AppListActivity.class);
			//	Intent i=new Intent(Intent.ACTION_ALL_APPS);
 				startActivityForResult(i,0);
 			}
 
 		};
 		
 		image.setOnClickListener(l);
 		text.setOnClickListener(l);
 	}
 	
 	public void onActivityResult(int req,int res,Intent intent){
 		super.onActivityResult(req,res,intent);
 		if(intent==null)return;
 		String name=intent.getStringExtra("name");
 		String pkg=intent.getPackage();
 		if(pkg==null)return;
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		pref.edit().putString("slot1.pkg",pkg).putString("slot1.name",name).commit();
 		loadSlots();
 		showNotify(ctx);
 	}
 	
 	public static void showNotify(Context ctx)
 	{
 		int version = DeviceUtil.getSDKVersion();
 		if ( version > 10 )
 			showNotifyForSDK11(ctx);
 		else 
 			showNotifyForSDK8(ctx);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public static void showNotifyForSDK8(Context ctx)
 	{
 		NotificationManager nm=(NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(1);
 		Notification n = new Notification(R.drawable.icon, ctx.getString(R.string.app_name), System.currentTimeMillis());
 		n.flags |= Notification.FLAG_NO_CLEAR;
 		{
 			Intent i = new Intent(ctx, ActionUIActivity.class);
 			//i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, 10, i, 0);
 			n.setLatestEventInfo(ctx, ctx.getString(R.string.app_name), "", pi);
 		}
 		nm.notify(1, n);
 	}
 	
 	public static void showNotifyForSDK11(Context ctx)
 	{
 		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ctx);
 		boolean isRooted = pref.getBoolean("root", false);
 		
 		NotificationManager nm=(NotificationManager) ctx.getSystemService(NOTIFICATION_SERVICE);
 		nm.cancel(1);
 		Notification n = new Notification();
 		n.icon = R.drawable.icon;
 		n.when = System.currentTimeMillis();
 		n.flags |= Notification.FLAG_NO_CLEAR;
 		{
 			Intent i = new Intent(ctx, MainActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			PendingIntent pi = PendingIntent.getActivity(ctx, 10, i, 0);
 			n.contentIntent = pi;	
 		}
 
 		RemoteViews view = new RemoteViews(ctx.getPackageName(), R.layout.tools);
 		int id = 1;
 		{
 			Intent i = new Intent(ctx, ActionActivity.class);
 			i.putExtra("action", ActionActivity.ACTION_LOCK);
 			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.labelLock, pi);
 		}
 
 		if (isRooted){
 			Intent i = new Intent(ctx, ActionActivity.class);
 			i.putExtra("action", ActionActivity.ACTION_SHUTDOWN);
 			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.labelShutdown, pi);
 		}
 		else {
 			view.setViewVisibility(R.id.labelShutdown, View.GONE);
 		}
 
 		{
 			Intent i = new Intent(ctx, ActionActivity.class);
 			i.putExtra("action", ActionActivity.ACTION_VOLUME_DOWN);
 			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.labelVolDown, pi);
 		}
 
 		{
 			Intent i = new Intent(ctx, ActionActivity.class);
 			i.putExtra("action", ActionActivity.ACTION_VOLUME_UP);
 			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.labelVolUp, pi);
 		}
 		
 		String pkgName = pref.getString("slot1.pkg", "");
 		String labelName = pref.getString("slot1.name", "");
 		if ( pkgName!=null&&pkgName.length()>0 )
 		{
 			Intent i = ctx.getPackageManager().getLaunchIntentForPackage(pkgName);
 //			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			if(i!=null){
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.slot1, pi);
 			if(labelName!=null)view.setTextViewText(R.id.textSlot1,labelName);
 			Drawable img=getAppIcon(ctx,pkgName);
 			if(img!=null){
 				Bitmap icon=((BitmapDrawable)img).getBitmap();
 				view.setImageViewBitmap(R.id.imageSlot1,icon);
 			}
 			else{
 				view.setImageViewResource(R.id.imageSlot1,R.drawable.app);
 			}
 			view.setViewVisibility(R.id.slot1,View.VISIBLE);
 			}
 			else{
 				view.setViewVisibility(R.id.slot1,View.GONE);
 			}
 		}
 		else{
 			view.setViewVisibility(R.id.slot1,View.GONE);
 		}
 		
 		{
 			Intent i = new Intent(ctx, ActionActivity.class);
 			i.putExtra("action", ActionActivity.ACTION_CANCEL);
 			i.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 			PendingIntent pi = PendingIntent.getActivity(ctx, id++, i, 0);
 			view.setOnClickPendingIntent(R.id.labelCancel, pi);
 		}	
 		
 		n.contentView = view;
 		nm.notify(1, n);
 	}
 	
 	public static Drawable getAppIcon(Context ctx,String pkg){
 		try
 		{
 			PackageManager pm = ctx.getPackageManager();
 			PackageInfo info=pm.getPackageInfo(pkg, 0);
 			return info.applicationInfo.loadIcon(pm);
 		}
 		catch(Throwable e)
 		{
 			return ctx.getResources().getDrawable(R.drawable.app);
 		}
 	}
 }
