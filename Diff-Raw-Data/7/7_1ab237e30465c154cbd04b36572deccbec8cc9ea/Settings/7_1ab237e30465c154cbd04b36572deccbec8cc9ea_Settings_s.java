 package com.wazzup.ninedrink;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 
 public class Settings extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.settings);
 		findView();
 		setListener();
 		setCboxDefault();
 		mOpenHelper = new DatebaseHelper(this);
 		createTable();
 		getAll();
 	}
 	//ŧi
 	private Button btn_cancel;
 	private Button btn_done;
 	private CheckBox[] cbox_poker = new CheckBox[14] ;
 	private boolean[] poker_list ={false,false,false,false,false,false,false,false,false,false,false,false,false,false};
 	private CheckBox cbox_s ;
 	private int[] cboxId = {
 		R.id.checkBox0,R.id.checkBox1,R.id.checkBox2,
 		R.id.checkBox3,R.id.checkBox4,R.id.checkBox5,
 		R.id.checkBox6,R.id.checkBox7,R.id.checkBox8,
 		R.id.checkBox9,R.id.checkBox10,R.id.checkBox11,
 		R.id.checkBox12,R.id.checkBox13
 	};
 	private static final String DATABASE_NAME = "NdDb.db";
 	private static final int DATABASE_VERSION = 1;
 	private static final String TABLE_NAME = "set_poker";
 	private static final String TITLE = "poker_number";
 	private static final String BODY = "is_into";
 	//]wƮw
 	DatebaseHelper mOpenHelper;
 
 	private static class DatebaseHelper extends SQLiteOpenHelper {
 		DatebaseHelper(Context context){
 			super(context, DATABASE_NAME, null, DATABASE_VERSION);
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db){
 			String sql = "CREATE TABLE " + TABLE_NAME + " (" + TITLE +
 				" text not null, " + BODY + " boolean not null " + ");";
 			try{
 				db.execSQL(sql);
 			}catch(SQLException e){
 				Log.i("Test:createDB = ", e.toString());
 			}
 		}
 
 		@Override
 		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){}
 	}
 	//]wU
 	private void findView(){
 		cbox_s = (CheckBox)findViewById(R.id.checkBoxAll);
 		for(int i = 0; i < 14; i++){
 			cbox_poker[i] = (CheckBox)findViewById(cboxId[i]);
 		}
 		btn_cancel = (Button)findViewById(R.id.btn_setcancel);
 		btn_done = (Button)findViewById(R.id.btn_setcomplete);
 	}
 	//ť
 	private void setListener(){
 		cbox_s.setOnCheckedChangeListener(selectAll);
 		btn_cancel.setOnClickListener(setcancel);
 		for(int i = 0; i < 14; i++){
 			cbox_poker[i].setOnCheckedChangeListener(selected);
 		}
 		btn_done.setOnClickListener(setdone);
 	}
 	private Button.OnClickListener setdone = new Button.OnClickListener(){
 		public void onClick(View v){
 			int selectCount = 0;
 			for(int i = 0; i < 14; i++){
 				if(cbox_poker[i].isChecked())
 					selectCount++;
 			}
 			if(selectCount < 2){
 				setTitle(R.string.limit_msg);
 			} else {
 				for(int i = 0; i < 14; i++){
 					updateItem(i, poker_list[i]);
 				}
				finish();
 			}
 		}
 	};
 	private Button.OnClickListener setcancel = new Button.OnClickListener(){
 		public void onClick(View v){
 			finish();
 		}
 	};
 	private CheckBox.OnCheckedChangeListener selectAll= new CheckBox.OnCheckedChangeListener(){
 		@Override
 			public void onCheckedChanged(CompoundButton btnView, boolean isChecked){
 				for(int i = 0; i < 14; i++){
 					cbox_poker[i].setChecked(isChecked);
 				}
 		   }
 	};
 	private CheckBox.OnCheckedChangeListener selected= new CheckBox.OnCheckedChangeListener()
 	{
 		@Override
 		public void onCheckedChanged(CompoundButton btnView, boolean isChecked){
 			// TODO Auto-generated method stub
 			for(int i = 0; i < 14; i++){
 				if(cboxId[i] == btnView.getId()){
 					poker_list[i] = isChecked;
 					break;
 				}
 			}
 		}
 	};
 	
 	private void setCboxDefault(){
 		
 	}
 
 	// oҦO
 	public void getAll(){
 		int i = 0;
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		Cursor result = db.rawQuery("Select * from " + TABLE_NAME, null);
 		result.moveToFirst();
 		while (!result.isAfterLast()){
 			i = result.getInt(0);
 			poker_list[i] = Boolean.valueOf(result.getString(1).equals("0") ? "false" : "true");
 			cbox_poker[i].setChecked(poker_list[i]);
 			result.moveToNext();
 		}
 	}
 
 	//إ߸ƪ
 	public void createTable(){
 		int[] initValue = {1, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0};
 		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 		String SQL = "CREATE TABLE " + TABLE_NAME + " (" + TITLE +
 			" int not null, " + BODY + " boolean not null " + ");";
 		try {
 			//db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
 			db.execSQL(SQL);
 			//[Jtιw]
 			for(int i = 0; i < 14; i++){
 				//String sql_set_default = "Insert Into " + TABLE_NAME + " (" + TITLE + ", " + BODY + ") values(" + cboxId[i]+ ", '" + poker_list[i] + "');";
 				String sql_set_default = "Insert Into " + TABLE_NAME + " (" + TITLE + ", " + BODY + ") values(" + i + ", " + initValue[i] + ");";
 				try{
 				db.execSQL(sql_set_default);
 				}catch (SQLException e){
 					//setTitle("ƪإߥ");
 				}
 			}
 			//setTitle("ƪ\");
 		} catch (SQLException e){
 			//setTitle("ƪإ");
 		}
 		
 	}
 	//
 
 	//s@
 	public void updateItem(int poker_number ,boolean is_into){
 		try {
 			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
 			ContentValues args = new ContentValues();
 			args.put("is_into", is_into);
 			db.update("set_poker", args, "poker_number = "+ String.valueOf(poker_number) , null);
 		} catch (SQLException e){
 			setTitle("sƥ");
 		}
 	}
 }
