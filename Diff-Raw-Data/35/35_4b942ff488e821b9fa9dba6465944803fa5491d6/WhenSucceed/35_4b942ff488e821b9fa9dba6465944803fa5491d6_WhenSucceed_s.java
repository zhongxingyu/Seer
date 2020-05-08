 package com.example.skylark;
 
 import java.util.HashMap;
 
 import com.example.ui.MyApplication;
 import com.umeng.api.common.SnsParams;
 import com.umeng.api.exp.UMSNSException;
 import com.umeng.api.sns.UMSnsService;
 import com.umeng.api.sns.UMSnsService.SHARE_TO;
 
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class WhenSucceed extends Activity{
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.whenfail);
 		setTitle("计划完成！");
 		TextView text=(TextView)findViewById(R.id.messageWhenFail);
 		Button button=(Button)findViewById(R.id.ok);
 		button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				finish();
 				//((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancelAll();
 				try {
 					publishInSNS();
 				} catch (UMSNSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		text.setText(getHowLong());
 		stopService(new Intent("com.example.skylark.monitorservice"));
 	}  
 	public String getHowLong()
 	{
 		String ans="";
 		Time t=new Time();
 		t.setToNow();
 		int hour=getIntent().getIntExtra("hour", 0)-t.hour;
 		int min=getIntent().getIntExtra("min", 0)-t.minute;
 		if(min<0)
 		{
 			min+=60;
 			hour--;
 		}
 		if(hour>0)
 		{
 			ans=ans+hour+"小时";
 		}
 		if(min>0)
 		{
 			ans=ans+min+"分钟";
 		}
 		RecordTool re=new RecordTool(WhenSucceed.this, true, getIntent().getStringExtra("blName"));
 		ans="计划完成，本次我坚持了"+ans+"。我一共成功了"+re.getTimes("succeed.re")+"次！";
 		return ans;
 	}
 	public void publishInSNS() throws UMSNSException
 	{
 		final String snsName=getIntent().getStringExtra("snsName");
 		if(snsName.equals(""))
 		{
 			return;
 		}
 		
 		if(snsName.equals("renren"))
 		{
 			UMSnsService.shareToRenr(this, getHowLong(), null);
 		}
 		if(snsName.equals("tencent"))
 		{
 			UMSnsService.shareToTenc(this, getHowLong(), null);
 		}
 		if(snsName.equals("sina"))
 		{
 			UMSnsService.shareToSina(this, getHowLong(), null);
 		}
 	}
 }
