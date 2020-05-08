 package com.dary.xmpp.cmd;
 
 import org.jivesoftware.smack.Chat;
 
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationManager;
 
 import com.dary.xmpp.MyApp;
import com.dary.xmpp.Tools;
 
 public class GpsCmd extends CmdBase {
 	public static void Gps(Chat chat) {
 		LocationManager locManager = (LocationManager) MyApp.getContext().getSystemService(Context.LOCATION_SERVICE);
 
 		// if (MainService.locationmanager
 		// .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER))
 		// {
		// System.out.println("gps");
 		// Toast.makeText(XmppActivity.xmppactivity,
 		// "GPS模块正常", Toast.LENGTH_SHORT)
 		// .show();
 		// //return;
 		// }
 		// Toast.makeText(XmppActivity.xmppactivity, "Please Open GPS",
 		// Toast.LENGTH_SHORT).show();
 		// Intent intent = new Intent(
 		// Settings.ACTION_SECURITY_SETTINGS);
 		// MainService.mainservice.startActivity(intent);
 
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 低精度
 		// criteria.setAltitudeRequired(false); //高度
 		// criteria.setBearingRequired(false); //方向
 		criteria.setCostAllowed(false); // 免费
 		// criteria.setPowerRequirement(Criteria.POWER_LOW); 电量
 		String provider = locManager.getBestProvider(criteria, true); // 获取GPS信息
 		Location location = locManager.getLastKnownLocation(provider); // 通过GPS获取位置
 		if (location != null) {
			Tools.doLogJustPrint(location.getLatitude() + "");
			Tools.doLogJustPrint(location.getLongitude() + "");
 
 			sendMessageAndUpdateView(chat, "http://maps.google.com/maps?q=" + location.getLatitude() + ",+" + location.getLongitude());
 		}
 		// 设置监听器,自动更新的最小时间为间隔N秒(1秒为1*1000,这样写主要为了方便)或最小位移变化超过N米
 		// ServiceManager.locManager.requestLocationUpdates(provider, 10 * 1000,
 		// 100, new LocationListener() {
 		//
 		// public void onLocationChanged(Location location) {
 		// // // method stub
 		// // if (location != null)
 		// // {
 		// // System.out.println(location.getLatitude());
 		// // System.out.println(location.getLongitude());
 		// // try
 		// // {
 		// // chat.sendMessage("http://maps.google.com/maps?q="+
 		// // location.getLatitude() + ",+" +
 		// // location.getLongitude());
 		// // }
 		// // catch (XMPPException e)
 		// // {
 		// // e.printStackTrace();
 		// // }
 		// // }
 		//
 		// }
 		//
 		// public void onProviderDisabled(String provider) {
 		//
 		// }
 		//
 		// public void onProviderEnabled(String provider) {
 		//
 		// }
 		//
 		// public void onStatusChanged(String provider, int status, Bundle
 		// extras) {
 		//
 		// }
 		//
 		// });
 	}
 }
