 package com.ag.zhaisujie.activity;
 
 import java.util.Calendar;
 
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.TimePickerDialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.TimePicker;
 
 import com.ag.zhaisujie.R;
 import com.ag.zhaisujie.ToastUtil;
 import com.ag.zhaisujie.ValidUtil;
 import com.ag.zhaisujie.model.Order;
 
 /**
  *    OrderFrstActivity.java
  *     <p>
  *     һҳ
  *     Copyright: Copyright(c) 2013 
  *     @author Gavin_Feng
  *     </p>
  * 
  */
 public class OrderFrstActivity extends BaseActivity {
 	private Button backBtn;
 	private TextView titleTxt;
 	private TextView addrTxt;
 	private Button dateBtn;
 	private Button orderBtn;
 	private EditText dateTxt;
 	private RadioButton time2Btn;
 	private RadioButton time3Btn;
 	private RadioButton time4Btn;
 	private int mYear; 
     private int mMonth;
     private int mDay;
     private int mHour;
     private int mMinute;
     
     private Button timeBtn;
 	private EditText timeTxt;
 
     private static final int SHOW_DATAPICK = 0;
     private static final int DATE_DIALOG_ID = 1; 
     private static final int SHOW_TIMEPICK = 2;
     private static final int TIME_DIALOG_ID = 3;
     private Order order;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.order_frst);
 		backBtn=(Button)findViewById(R.id.title_btn_back);
 		backBtn.setVisibility(View.VISIBLE);
 		backBtn.setOnClickListener(listener);
 		titleTxt=(TextView)findViewById(R.id.title);
 		titleTxt.setText(R.string.order_frst_title);
 		addrTxt=(TextView)findViewById(R.id.order_txt_addr);
 		dateBtn=(Button)findViewById(R.id.date_btn_input);
 		dateBtn.setOnClickListener(listener);
 		dateTxt=(EditText)findViewById(R.id.date_txt_input);
 		timeBtn=(Button)findViewById(R.id.time_btn_input);
 		timeBtn.setOnClickListener(listener);
 		timeTxt=(EditText)findViewById(R.id.time_txt_input);
 		
 		orderBtn=(Button)findViewById(R.id.order_btn_next);
 		orderBtn.setOnClickListener(listener);
 		time2Btn=(RadioButton)findViewById(R.id.time_btn_2);
 		time3Btn=(RadioButton)findViewById(R.id.time_btn_3);
 		time4Btn=(RadioButton)findViewById(R.id.time_btn_4);
 		
 		//õַ
 		Intent intent=getIntent();
 		order=(Order)getIntent().getSerializableExtra("Order");
 		String addr=order.getAddress();
 		addrTxt.setText(addr);
 		//ʱʼ
 		
 		 final Calendar c = Calendar.getInstance();
 		 c.add(Calendar.DAY_OF_MONTH, 1);
          mYear = c.get(Calendar.YEAR); 
          mMonth = c.get(Calendar.MONTH); 
          mDay = c.get(Calendar.DAY_OF_MONTH);
          mHour = c.get(Calendar.HOUR_OF_DAY);
          mMinute = c.get(Calendar.MINUTE);
 	}
 
     /**
      * 
      */
 	private void setDateTime(){
        final Calendar c = Calendar.getInstance();  
        
        mYear = c.get(Calendar.YEAR);  
        mMonth = c.get(Calendar.MONTH);  
        mDay = c.get(Calendar.DAY_OF_MONTH); 
   
        updateDateDisplay(); 
 	}
 	
 	/**
 	 * ʾ
 	 */
 	private void updateDateDisplay(){
 		dateTxt.setText(new StringBuilder().append(mYear).append("-")
     		   .append((mMonth + 1) < 10 ? "0" + (mMonth + 1) : (mMonth + 1)).append("-")
                .append((mDay < 10) ? "0" + mDay : mDay)); 
 	}
 	
     /** 
      * ڿؼ¼ 
      */  
     private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {  
   
        public void onDateSet(DatePicker view, int year, int monthOfYear,  
               int dayOfMonth) {  
            mYear = year;  
            mMonth = monthOfYear;  
            mDay = dayOfMonth;  
 
            updateDateDisplay();
        }  
     }; 
 	
 	/**
 	 * ʱ
 	 */
 	private void setTimeOfDay(){
 	   final Calendar c = Calendar.getInstance(); 
        mHour = c.get(Calendar.HOUR_OF_DAY);
        mMinute = c.get(Calendar.MINUTE);
        updateTimeDisplay();
 	}
 	
 	/**
 	 * ʱʾ
 	 */
 	private void updateTimeDisplay(){
 		timeTxt.setText(new StringBuilder().append(mHour).append(":")
                .append((mMinute < 10) ? "0" + mMinute : mMinute)); 
 	}
     
     /**
      * ʱؼ¼
      */
     private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
 		
 		@Override
 		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
 			mHour = hourOfDay;
 			mMinute = minute;
 			
 			updateTimeDisplay();
 		}
 	};
     
     @Override  
     protected Dialog onCreateDialog(int id) {  
        switch (id) {  
        case DATE_DIALOG_ID:  
            return new DatePickerDialog(this, mDateSetListener, mYear, mMonth,  
                   mDay);
        case TIME_DIALOG_ID:
     	   return new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true);
        }
     	   
        return null;  
     }  
   
     @Override  
     protected void onPrepareDialog(int id, Dialog dialog) {  
        switch (id) {  
        case DATE_DIALOG_ID:  
            ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDay);  
            break;
        case TIME_DIALOG_ID:
     	   ((TimePickerDialog) dialog).updateTime(mHour, mMinute);
     	   break;
        }
     }  
   
     /** 
      * ںʱؼHandler 
      */  
     Handler dateandtimeHandler = new Handler() {
   
        @Override  
        public void handleMessage(Message msg) {  
            switch (msg.what) {  
            case SHOW_DATAPICK:  
                showDialog(DATE_DIALOG_ID);  
                break; 
            case SHOW_TIMEPICK:
         	   showDialog(TIME_DIALOG_ID);
         	   break;
            }  
        }  
   
     }; 
     //ҳ
 	private void nextPage(){
 		if(!(time2Btn.isChecked()||time3Btn.isChecked()||time4Btn.isChecked())){
 			ToastUtil.show(this, "ѡʱ");
 			return;
 		}
 		if(dateTxt.getText().toString().trim().length()==0){
 			ToastUtil.show(this, "ѡڣ");
 			return;
 		}else if(ValidUtil.isDate(dateTxt.getText().toString())){
 			ToastUtil.show(this, "ѡȷڣyyyy-mm-dd");
 			return;
 		}else if(timeTxt.getText().toString().trim().length()==0){
 			ToastUtil.show(this, "ѡʱ䣡");
 			return;
 		}else if (ValidUtil.validTime(timeTxt.getText().toString()).length()>0){
 			ToastUtil.show(this, ValidUtil.validTime(timeTxt.getText().toString()));
 			return;
 		}
 		int timeLong=0;
 		int money=0;
		if(time2Btn.isChecked()){
 			timeLong=2;
 			money=36;
		}else if(time3Btn.isChecked()){
 			timeLong=3;
 			money=54;
		}else if(time4Btn.isChecked()){
 			timeLong=4;
 			money=72;
 		}
 		String dateTime=dateTxt.getText().toString()+" "+timeTxt.getText().toString();
 		order.setClean_hours(timeLong);
 		order.setPrice(money);
 		order.setBegin_time(dateTime);
 		
 		Intent intent = new Intent(OrderFrstActivity.this, ContactDetailActivity.class);
 		Bundle bundle = new Bundle();  
 		bundle.putSerializable("Order", order);
 		intent.putExtras(bundle);//ݵַһҳ
 		OrderFrstActivity.this.startActivity(intent);
 		
 	}
 	
 	OnClickListener listener = new OnClickListener() {
 		public void onClick(View v) {
 			Button btn = (Button) v;
 			switch (btn.getId()) {
 				case R.id.title_btn_back:
 					OrderFrstActivity.this.finish();
 					break;
 				case R.id.date_btn_input:
 					Message msgd = new Message();
 					msgd.what = SHOW_DATAPICK; 
 	                dateandtimeHandler.sendMessage(msgd);
 	                break;
 				case R.id.time_btn_input:   
 	                Message msgt = new Message();
 	                msgt.what = SHOW_TIMEPICK; 
 	                dateandtimeHandler.sendMessage(msgt);
 					break;
 				case R.id.order_btn_next:
 					nextPage();
 					break;
 			}
 		}
 	};
 
 }
