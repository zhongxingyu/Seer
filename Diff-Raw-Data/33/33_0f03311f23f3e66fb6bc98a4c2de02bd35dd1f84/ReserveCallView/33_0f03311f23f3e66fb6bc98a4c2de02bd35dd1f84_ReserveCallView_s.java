 package ghostsheep.com.classes;
 
 import ghostsheep.com.emergency.R;
 import ghostsheep.com.emergency.setting.Setting;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Handler;
 import android.os.SystemClock;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Chronometer;
 import android.widget.EditText;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 public class ReserveCallView implements OnClickListener {
 	private Activity activity;
 	
 	private Setting setting = null;
 	private RadioGroup easyTime;
 	private Chronometer chronometer;
 	private Button start;
 	private Button cancel;
 	private Button upButton, downButton;
 	private EditText numberEdit;
 	private int maxrange = 60, minrange = 1, value = 1;
 	
 	private Handler m_Handler = new Handler();
 	
 	public ReserveCallView(Activity activity) {
 		this.activity = activity;
 	}
 	
 	public void initView(View view) {
 		if (null == setting) {
         	setting = new Setting(activity);
         }
         setting.Load(activity);
         
         easyTime = (RadioGroup)view.findViewById(R.id.easy_time);
     	chronometer = (Chronometer)view.findViewById(R.id.chronometer);
     	start = (Button)view.findViewById(R.id.start);
     	
     	cancel = (Button)view.findViewById(R.id.cancel);
     	cancel.setEnabled(false);
     	cancel.setTextColor(Color.GRAY);
     	
     	upButton = (Button)view.findViewById(R.id.upButton);
     	upButton.setEnabled(false);
     	
     	numberEdit = (EditText)view.findViewById(R.id.numberEditText);
     	numberEdit.setEnabled(false);
 		numberEdit.setText(String.valueOf(value));
 		
     	downButton = (Button)view.findViewById(R.id.downButton);
 		downButton.setEnabled(false);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId()) {
 		case R.id.start:
 			start.setEnabled(false);
 			start.setTextColor(Color.GRAY);
 			cancel.setEnabled(true);
 			cancel.setTextColor(Color.WHITE);
 			if (R.id.min5 == easyTime.getCheckedRadioButtonId()) {
 				startTimer(getInterval(5));
 			} else if (R.id.min10 == easyTime.getCheckedRadioButtonId()) {
 				startTimer(getInterval(10));
 			} else if (R.id.min20 == easyTime.getCheckedRadioButtonId()) {
 				startTimer(getInterval(5));
 			} else if (R.id.setTimer == easyTime.getCheckedRadioButtonId()) {
 				startTimer(getInterval(Integer.valueOf(numberEdit.getText().toString())));
 			}
 			break;
 		case R.id.cancel:
 			stopTimer();
 			start.setEnabled(true);
 			start.setTextColor(Color.WHITE);
 			cancel.setEnabled(false);
 			cancel.setTextColor(Color.GRAY);
 			break;
 		case R.id.upButton:
 			if (value >= minrange && value <= maxrange) {
 				value++;
 			}
 			
 			if (value > maxrange) {
 				value = minrange;
 			}
 			
 			numberEdit .setText(value + "");
 			break;
 		case R.id.downButton:
 			if (value >= minrange && value <= maxrange) {
 				value--;
 			}
 			
 			if (value < minrange) {
 				value = maxrange;
 			}
 			
 			numberEdit .setText(value + "");
 			break;
 		}
 	}
     
     public void initEvent() {
 		start.setOnClickListener(this);
 		cancel.setOnClickListener(this);
 		upButton.setOnClickListener(this);
 		downButton.setOnClickListener(this);
     	
     	easyTime.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				upButton.setEnabled(false);
 				downButton.setEnabled(false);
 				
 				switch (checkedId) {
 				case R.id.min5:
 					break;
 				case R.id.min10:
 					break;
 				case R.id.min20:
 					break;
 				case R.id.setTimer:
 					upButton.setEnabled(true);
 					downButton.setEnabled(true);
 					break;
 				}
 			}
 		});
     	
     	// 범위 초과 check
     	numberEdit.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				// TODO Auto-generated method stub
 				if (Integer.valueOf(s.toString()) > maxrange) {
 					numberEdit.setText(maxrange + "");
 				}
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
     }
     
     /*
     * �Է� �޴� ���� ����Ͽ� interval ��ȯ
      */
     private long getInterval(int interval) {
     	return interval * 60000;
     }
     
     /*
      * 시간 check 하여 지정된 시간 도달 시 전화 연결
      */
     private void startTimer(final long timeSpan) {
     	chronometer.setBase(SystemClock.elapsedRealtime());
     	chronometer.start();
     	
      	m_Handler.postDelayed(new Runnable() {
     		@Override
      		public void run() {
     			makeCall();
      		}
      	}, timeSpan);
     }
     
     /*
     * 전화 걸
      */
     private void makeCall() {
     	setting.Load(activity);
     	
     	Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + setting.getEmergencyCall()));
     	activity.startActivity(intent);
 		
 		chronometer.stop();
 		chronometer.setBase(SystemClock.elapsedRealtime());
     	m_Handler.removeMessages(0);
     }
     
     /*
      * Timer & Handler stop
      */
     private void stopTimer() {
     	chronometer.stop();
     	m_Handler.removeMessages(0);
     	
     }
 }
