 package com.ahutpt.lesson;
 
 import com.ahutpt.lesson.lesson.Lesson;
 import com.ahutpt.lesson.lesson.LessonManager;
 import com.ahutpt.lesson.time.Timetable;
 import com.mobclick.android.MobclickAgent;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class LessonActivity extends SherlockActivity {
 	private int week,time;
 	private Lesson lesson;
 	private TextView tvLessonName,tvLessonPlace,tvTeacherName,tvHomework,tvLessonTime,tvCurrentTime;
 	private Button btnEditHomework,btnDeleteHomework;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		setTheme(R.style.Theme_Sherlock_Light);
 		super.onCreate(savedInstanceState);
 		if(!Timetable.loaded)
 			new Timetable(this);
 		if(!LessonManager.loaded)
 			new LessonManager(this);
 		
 		week = getIntent().getExtras().getInt("week");
 		time = getIntent().getExtras().getInt("time");
 		
 		if(!((week >= 0 && week <= 6)&&(time >=0 && time <= 4))){
 			this.finish();
 			return;
 		}
 		
 		setContentView(R.layout.lesson);
 		
 		tvLessonName = (TextView)findViewById(R.id.tvLessonName);
 		tvLessonPlace = (TextView)findViewById(R.id.tvLessonPlace);
 		tvTeacherName = (TextView)findViewById(R.id.tvTeacherName);
 		tvHomework = (TextView)findViewById(R.id.tvHomework);
 		tvLessonTime = (TextView)findViewById(R.id.tvLessonTime);
 		
 		tvCurrentTime = (TextView)findViewById(R.id.tvCurrentTime);
 		tvCurrentTime.setText(Timetable.weekname[week] + Timetable.lessontime_name[time]);
 		
 		btnEditHomework = (Button)findViewById(R.id.btnEditHomework);
 		btnDeleteHomework = (Button)findViewById(R.id.btnDeleteHomework);
 		
 		btnEditHomework.setOnClickListener(new View.OnClickListener() {	
 			@Override
 			public void onClick(View arg0) {
 				editHomework();
 			}
 		});
 		btnDeleteHomework.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				deleteHomework();
 			}
 		});
 		
 		lesson = LessonManager.getLessonAt(week, time, this);
 		if(lesson != null){
 			tvLessonName.setText(lesson.name);
 			tvLessonPlace.setText(lesson.place);
 			tvTeacherName.setText(lesson.teacher);	
 			if(lesson.homework!= null && !lesson.homework.contentEquals("")){
 				tvHomework.setText(lesson.homework);
 			}
 		}else{
 			btnEditHomework.setVisibility(View.GONE);
 			btnDeleteHomework.setVisibility(View.GONE);
 		}
 		
 		tvLessonTime.setText(Timetable.begintime[time] + " ~ " +Timetable.endtime[time]);
 	}
 	
 	protected void editHomework() {
 		// ҵ
 		if(lesson == null)return;
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 		alert.setTitle("༭ҵ");
 		alert.setMessage("뱾γ̵ҵ");
 
 		final EditText input = new EditText(this);
 		alert.setView(input);
 		if(lesson.hasHomework && lesson.homework != null && !lesson.homework.contentEquals("")){
 			input.setText(lesson.homework);
 		}
 		alert.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 		public void onClick(DialogInterface dialog, int whichButton) {
 			String value = input.getText().toString();
 			if(!value.contentEquals("")){
 				lesson.homework = value;
 				lesson.hasHomework = true;
 				tvHomework.setText(lesson.homework);
 			}
 		  }
 		});
 
 		alert.setNegativeButton("ȡ", null);
 		alert.show();
 		LessonManager.editHomework(week, time, lesson.homework);
 	}
 
 	protected void deleteHomework() {
 		// ҵ
 		if(lesson == null)return;
 		if(!lesson.hasHomework)return;
 		new AlertDialog.Builder(LessonActivity.this).setTitle("ҵ")
 		.setMessage("ȷձγҵ")
 		.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which) {
 				lesson.homework = null;
 				lesson.hasHomework = false;
 				LessonManager.deleteHomework(week, time);
 				tvHomework.setText("");
 			}
 
 		}).setNegativeButton("ȡ", null).show();
 	}
 
 	@Override
 	protected void onResume() {
 		// ɾ޸ĺ
 		super.onResume();
 
 		MobclickAgent.onResume(this);
 		
 		tvLessonName = (TextView)findViewById(R.id.tvLessonName);
 		tvLessonPlace = (TextView)findViewById(R.id.tvLessonPlace);
 		tvTeacherName = (TextView)findViewById(R.id.tvTeacherName);
 		tvLessonTime = (TextView)findViewById(R.id.tvLessonTime);
 		
 		lesson = LessonManager.getLessonAt(week, time, this);
 		if(lesson != null){
 			tvLessonName.setText(lesson.name);
 			tvLessonPlace.setText(lesson.place);
 			tvTeacherName.setText(lesson.teacher);	
 		}
 	}
 	
 	@Override
     public void onPause() {
         super.onPause();
         MobclickAgent.onPause(this);
     }
 	
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.lesson, menu);
 		return true;
 	}
 
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch(item.getItemId()){
 		case R.id.menu_edit_lesson:
 			Intent i = new Intent(this,EditLessonActivity.class);
 			i.putExtra("week", week);
 			i.putExtra("time", time);
 			startActivity(i);
 			return true;
 		case R.id.menu_delete_lesson:
 			new AlertDialog.Builder(LessonActivity.this).setTitle("ɾγ")
 			.setMessage("ȷɾγ̣")
 			.setPositiveButton("ȷ", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 					if(lesson!=null){
 						MobclickAgent.onEvent(LessonActivity.this, "delete_lesson",lesson.name + " : " + lesson.place + " : " + lesson.teacher);
 						lesson.delete();
 					}
 					LessonActivity.this.finish();
 				}
 
 			}).setNegativeButton("ȡ", null).show();
 			return true;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
 	}
 
 }
