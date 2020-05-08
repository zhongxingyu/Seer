 package com.sjl.housewhere;
 
 import android.app.Application;
 import android.content.Context;
 import android.widget.Toast;
 import com.baidu.mapapi.BMapManager;
 import com.baidu.mapapi.MKGeneralListener;
 import com.baidu.mapapi.map.MKEvent;
 import com.sjl.housewhere.database.AssetsDatabaseManager;
 
 public class HousewhereApplication extends Application {
 
     BMapManager mBMapManager = null;
     private String strKey = "0De30d2ecd212da334f228d1c302ea5d";
 
     @Override
     public void onCreate() {
         super.onCreate();    //To change body of overridden methods use File | Settings | File Templates.
 
         initEngineManager(this);
     }
 
     public void initEngineManager(Context context) {
         if (mBMapManager == null) {
             mBMapManager = new BMapManager(context);
         }
 
         if (!mBMapManager.init(strKey, new MyGeneralListener(context))) {
             Toast.makeText(context, "BMapManager  初始化错误!", Toast.LENGTH_LONG).show();
         }
 
     }
 
     // 常用事件监听，用来处理通常的网络错误，授权验证错误等
     class MyGeneralListener implements MKGeneralListener {
 
         private Context context;
 
         public MyGeneralListener(Context context) {
 
             this.context = context;
         }
 
         @Override
         public void onGetNetworkState(int iError) {
             if (iError == MKEvent.ERROR_NETWORK_CONNECT) {
                 Toast.makeText(context, "您的网络出错啦！",
                         Toast.LENGTH_LONG).show();
             } else if (iError == MKEvent.ERROR_NETWORK_DATA) {
                 Toast.makeText(context, "输入正确的检索条件！",
                         Toast.LENGTH_LONG).show();
             }
         }
 
         @Override
         public void onGetPermissionState(int iError) {
             if (iError ==  MKEvent.ERROR_PERMISSION_DENIED) {
                 //授权Key错误：
                 Toast.makeText(context,
                         "请在 DemoApplication.java文件输入正确的授权Key！", Toast.LENGTH_LONG).show();
             }
         }
     }
 }
