 package com.example.newone;
 
 
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 public class NewAddToDo extends Activity {
 	private Button _Done;
 	private RadioGroup _ActionType;
 	private EditText _StartTime;
 	private EditText _Deadline;
 	private EditText _Description;
 
 	//����UDone���îɡA���^MainActivity
 	private OnClickListener ListenerForDone = new OnClickListener() {
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 	/******		int i = _ActionType.getCheckedRadioButtonId();******/
 			RadioButton _choice = (RadioButton) findViewById(_ActionType.getCheckedRadioButtonId());
 
 			final String _tmpActionType =  _choice.getText().toString();			
 			final String _tmpStartTime = _StartTime.getText().toString();
 			final String _tmpDeadline = _Deadline.getText().toString();
 			final String _tmpDescription = _Description.getText().toString();
 		////////////////////////////////	
 			final httpService _hs = new httpService();
 			final String _url = "http://140.115.53.110:8080/helloJSP/index.html";
 			final String _value = "";
 			
 			
 			// Handler for httpservice thread
 			final Handler mHandler = new Handler() { 
 			     public void handleMessage(Message msg) { 
 			 		String _targetPlace=  msg.getData().get("1").toString();
 			 		//�s�W�@��ToDo�A�M���User��J���ѼƦs�JToDo tmp
 					ToDo tmp = new ToDo(_tmpActionType, _tmpStartTime, _tmpDeadline, _tmpDescription, _targetPlace);
 					//��tmp�ᵹToDoManager�A��L��tmp�[�J"ArrayList<ToDo> AllToDoListSavedInHere"
 					ToDoManager.getInstance().addUserToDo(tmp);
					Intent intent = new Intent(); 
					intent.setClass(NewAddToDo.this,MainActivity.class); 
					startActivity(intent); 
					NewAddToDo.this.finish(); 
 			     } 
 			 };
 			 
 			 // http thread cant run on main thread
 			Thread thread = new Thread(){
 			public void run(){
 				Bundle bundle = new Bundle();
 				bundle.putString("1", _hs.httpServiceGet(_url, _value));	
 				Message msg = new Message();
 				msg.setData(bundle);
 				mHandler.sendMessage(msg);
 			}
 			};
 			thread.start();
 	
 	////////////////////////		
 			
 		}
 	}; 
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.addtodo);
 
 		 _StartTime = (EditText) findViewById(R.id.StartTimeEditText);
 		 _Deadline = (EditText) findViewById(R.id.DeadlineEditText);
 		 _Description = (EditText) findViewById(R.id.DescriptionEditText);
 		 _ActionType = (RadioGroup) findViewById(R.id.radioGroup1);
 		 _Done = (Button) findViewById(R.id.Donebutton);
 		 //��U
 		_Done.setOnClickListener(ListenerForDone);
 
 		
 	}
 
 	
 	
 }
