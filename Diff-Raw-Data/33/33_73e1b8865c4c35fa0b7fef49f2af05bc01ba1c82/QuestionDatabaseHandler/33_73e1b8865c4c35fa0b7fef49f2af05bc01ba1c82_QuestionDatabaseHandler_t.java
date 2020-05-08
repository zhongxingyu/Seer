 package org.waldk33n3r.android.gnosis.games;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
 
 public class QuestionDatabaseHandler extends SQLiteOpenHelper {
 	
 	//Database Version
 	private static final int DATABASE_VERSION = 1;
 	
 	//Database Name
 	private static final String DATABASE_NAME = "questionsManager";
 	
 	//Questions table name
 	private static final String TABLE_QUESTIONS = "questions";
 	
 	//Questions Table Columns names
 	private static final String KEY_ID = "id";
     private static final String KEY_BODY = "body";
     private static final String KEY_ANSWER = "answer";
     private static final String KEY_OPTION1 = "option1";
     private static final String KEY_OPTION2 = "option2";
     private static final String KEY_OPTION3 = "option3";
 	
 	public QuestionDatabaseHandler(Context context){
 		super(context,DATABASE_NAME,null,DATABASE_VERSION);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		String CREATE_QUESTIONS_TABLE = "CREATE TABLE " + TABLE_QUESTIONS + "("
                 + KEY_ID + " INTEGER PRIMARY KEY," + KEY_BODY + " TEXT,"
                + KEY_ANSWER + " TEXT," + KEY_OPTION1 + " TEXT," + KEY_OPTION2 +
                " TEXT," + KEY_OPTION3 + " TEXT" + ")";
         db.execSQL(CREATE_QUESTIONS_TABLE);
 		
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
 		// Drop older table if existed
         db.execSQL("DROP TABLE IF EXISTS " + TABLE_QUESTIONS);
  
         // Create tables again
         onCreate(db);
 	}
 	
 	// Adding new question
 	public void addQuestion(Question question) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(KEY_BODY, question.getBody());
 		values.put(KEY_ANSWER, question.getAnswer());
 		values.put(KEY_OPTION1, question.getOption1());
 		values.put(KEY_OPTION2, question.getOption2());
 		values.put(KEY_OPTION3, question.getOption3());
		Log.e("DB","Inserting a question");
 		db.insert(TABLE_QUESTIONS, null, values);
 		db.close();
 	}
 	 
 	// Getting single question
 	public Question getQuestion(int id) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		Cursor cursor = db.query(TABLE_QUESTIONS, new String[] { KEY_ID,
 	            KEY_BODY, KEY_ANSWER, KEY_OPTION1, KEY_OPTION2, KEY_OPTION3 }, KEY_ID + "=?",
 	            new String[] { String.valueOf(id) }, null, null, null, null);
 		if(cursor != null){
 			cursor.moveToFirst();
 		}
 		
 		Question question = new Question(Integer.parseInt(cursor.getString(0)), 
 				cursor.getString(1), cursor.getString(2), cursor.getString(3), 
 				cursor.getString(4), cursor.getString(5));
 		
 		return question;
 	}
 	 
 	// Getting All Questions as a List
 	public List<Question> getAllQuestions() {
 		List<Question> questionList = new ArrayList<Question>();
 		
 	    String selectQuery = "SELECT  * FROM " + TABLE_QUESTIONS;
 	 
 	    SQLiteDatabase db = this.getWritableDatabase();
 	    Cursor cursor = db.rawQuery(selectQuery, null);
 	 
 	    if (cursor.moveToFirst()) {
 	        do {
 	            Question question = new Question();
 	            question.setID(Integer.parseInt(cursor.getString(0)));
 	            question.setBody(cursor.getString(1));
 	            question.setAnswer(cursor.getString(2));
 	            question.setOption1(cursor.getString(3));
 	            question.setOption2(cursor.getString(4));
 	            question.setOption3(cursor.getString(5));
 
 	            questionList.add(question);
 	        } while (cursor.moveToNext());
 	    }
 	 
 	    return questionList;
 	}
 	 
 	// Getting question Count
 	public int getQuestionsCount() {
 		String questionQuery = "SELECT  * FROM " + TABLE_QUESTIONS;
         SQLiteDatabase db = this.getReadableDatabase();
         Cursor cursor = db.rawQuery(questionQuery, null);
        int count = cursor.getCount();
         cursor.close();
  
        return count;
 	}
 	
 	// Updating single question
 	public int updateQuestion(Question question) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		 
 	    ContentValues values = new ContentValues();
 		values.put(KEY_BODY, question.getBody());
 		values.put(KEY_ANSWER, question.getAnswer());
 		values.put(KEY_OPTION1, question.getOption1());
 		values.put(KEY_OPTION2, question.getOption2());
 		values.put(KEY_OPTION3, question.getOption3());
 	 
 	    return db.update(TABLE_QUESTIONS, values, KEY_ID + " = ?",
 	            new String[] { String.valueOf(question.getID()) });
 	}
 	 
 	// Deleting single question
 	public void deleteQuestion(Question question) {
 		SQLiteDatabase db = this.getWritableDatabase();
 	    db.delete(TABLE_QUESTIONS, KEY_ID + " = ?",
 	            new String[] { String.valueOf(question.getID()) });
 	    db.close();
 	}
 }
