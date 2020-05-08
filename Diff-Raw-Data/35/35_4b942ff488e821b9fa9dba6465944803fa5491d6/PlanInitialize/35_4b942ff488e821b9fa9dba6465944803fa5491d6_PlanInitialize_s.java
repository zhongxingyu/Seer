 package com.example.ui;
 /*
  * 主功能界面，大部分的用户体验在此。
  * 其界面包括规划时长，规划黑名单，是否发布到SNS等，其中两个菜单还可以连接到其他界面。
  */
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.apache.http.util.EncodingUtils;
 
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TimePicker;
 import android.widget.Toast;
 import com.example.skylark.*;
 import com.umeng.api.exp.UMSNSException;
 import com.umeng.api.sns.UMSnsService;
 import com.umeng.api.sns.UMSnsService.SHARE_TO;
 
 public class PlanInitialize{
 	TimePicker tp;
 	Spinner sns_sp,bl_sp;
 	Button startButton;
 	String snsName="";
 	String blName="";
 	TestFragment fragment;
 	Context context;
 	View view;
 	public PlanInitialize(View view, TestFragment fragment,Context context)
 	{
 		this.view=view;
 		this.fragment=fragment;
 		//this.context=MyApplication.getInstance();
 		this.context=context;
 	}
 	public void iniPlan() {
 		// TODO Auto-generated method stub
 		
 		tp=(TimePicker)view.findViewById(R.id.tp);
 		tp.setIs24HourView(true);
 		
 		sns_sp=(Spinner)view.findViewById(R.id.sns_sp);
 		bl_sp=(Spinner)view.findViewById(R.id.bl_sp);
 		iniSNS();
 		iniBL();
 		startButton=(Button)view.findViewById(R.id.start);
 		startButton.setClickable(false);
 		startButton.setOnClickListener(new OnClickListener() {
 			@SuppressLint("NewApi")
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				if(blName.equals(""))
 				{
 					Toast.makeText(context, "请选择黑名单", Toast.LENGTH_SHORT).show();
 					return;
 				}
 				//startButton.setBackground(MyApplication.getInstance().getResources().getDrawable(R.drawable.friends));
 				Intent intent=new Intent("com.example.skylark.monitorservice");
 				intent.putExtra("blName", blName);
 				intent.putExtra("snsName", snsName);
 				intent.putExtra("hour", tp.getCurrentHour());
 				intent.putExtra("min", tp.getCurrentMinute());
 				HashMap<String, SHARE_TO> Name=new HashMap<String, UMSnsService.SHARE_TO>();
 				Name.put("renren", SHARE_TO.RENR);
 				Name.put("tencent", SHARE_TO.TENC);
 				Name.put("sina",SHARE_TO.SINA);
 				if(!snsName.equals("") && !UMSnsService.isAuthorized(MyApplication.getInstance(), Name.get(snsName)))
 				{
 					UMSnsService.OauthCallbackListener listener = new UMSnsService.OauthCallbackListener(){
 				        public void onComplete(Bundle value, SHARE_TO platform) {
 				        	Toast.makeText(context, "绑定成功", Toast.LENGTH_LONG).show();
 				        }
 				        public void onError(UMSNSException e, SHARE_TO platform) {
 				        	Toast.makeText(context, "对不起，绑定失败，请检查网络设置", Toast.LENGTH_LONG).show();
 				        	//
 				        }
 					};
 					
 					Log.v("my","fore");
 					if(snsName.equals("renren"))
 					{
 						//Toast.makeText(context, "asdf", Toast.LENGTH_LONG).show();
 						UMSnsService.oauthRenr(context, listener);
 						Log.v("my","renren");
 					}
 					if(snsName.equals("tencent"))
 					{
 						UMSnsService.oauthTenc(context, listener);
 					}
 					if(snsName.equals("sina"))
 					{
 						UMSnsService.oauthSina(context, listener);
 					}
 					return ;
 				}
 				MyApplication.getInstance().startService(intent);
 			}
 		});
 		
 	}
 
 	/*
 	 * 用以初始化SNS Spinner
 	 */
 	private void iniSNS()
 	{
 		MyAdapter adapter=new MyAdapter(MyApplication.getInstance(), 
 				new int[]{0,R.drawable.renren,R.drawable.tencent,R.drawable.sina},
 				new String[]{"不发布","人人网","腾讯微博","新浪微博"},false);
 		sns_sp.setAdapter(adapter);
 		SharedPreferences setting=context.getSharedPreferences("Setting", 0);
 		
 		sns_sp.setSelection(setting.getInt("SNS", 0));
         sns_sp.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				String[] snsNames={"","renren","tencent","sina"};
 				snsName=snsNames[arg2];
 				SharedPreferences setting=context.getSharedPreferences("Setting", 0);
 				SharedPreferences.Editor editor=setting.edit();
 				editor.putInt("SNS", arg2);
 				editor.commit();
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 			}
         	
 		});
 	}
 	
 	/*
 	 * 用以初始化BL Spinner
 	 */
 	
 	private void iniBL()
 	{
 		final ArrayList<String> names=new ArrayList<String>();
 		names.add("");
 		String blNames = "";
 		FileInputStream fin;
 		try {
 			fin = MyApplication.getInstance().openFileInput(MyApplication.getInstance().getResources().getString(R.string.blListNames));
 			int length = fin.available();   
         byte [] buffer = new byte[length];   
         fin.read(buffer);       
         blNames = ""+EncodingUtils.getString(buffer, "UTF-8");
         fin.close();  
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		while(blNames.length()>0)
 		{
 			String name=blNames.substring(0,blNames.indexOf(" "));
 			names.add(name);
 			blNames=blNames.substring(blNames.indexOf(" ")+1);
 		}
 		names.add("自定义");
 		MyAdapter adapter=new MyAdapter(MyApplication.getInstance(), names,false);
 		bl_sp.setAdapter(adapter);
 		bl_sp.setSelection(0, true);
         bl_sp.setOnItemSelectedListener(new OnItemSelectedListener() {
 
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				// TODO Auto-generated method stub
 				if(arg2==arg0.getCount()-1)
 				{
 					Intent intent=new Intent();
 					intent.setClass(MyApplication.getInstance(), DefineBlackList.class);
 					fragment.startActivity(intent);
 				}
 				else blName=names.get(arg2);
 			}
 
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				Intent intent=new Intent();
 				intent.setClass(MyApplication.getInstance(), DefineBlackList.class);
 				fragment.startActivity(intent);
 			}
         	
 		});
        //bl_sp.set
 	}
 }
