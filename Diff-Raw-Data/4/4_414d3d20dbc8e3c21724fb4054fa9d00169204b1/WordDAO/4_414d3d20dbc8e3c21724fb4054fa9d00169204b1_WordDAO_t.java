 package edu.rit.se.agile.data;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class WordDAO {
 	private SQLiteDatabase database;
 	private WordsTemplate dbHelper;
 	private String[] allColumns = { WordsTemplate.COLUMN_ID,
 			WordsTemplate.COLUMN_WORD_TYPE, 
 			WordsTemplate.COLUMN_CATEGORY,
 			WordsTemplate.COLUMN_WORD};
 	private String[] category = {WordsTemplate.COLUMN_ID, WordsTemplate.COLUMN_CATEGORY};
 	
 	public WordDAO(Context context) {
 		dbHelper = new WordsTemplate(context);
 	}
 	
 	public void open() throws SQLException {
 		database = dbHelper.getWritableDatabase();
 	}
 
 	public void close() {
 		database.close();
 	}
 	
 	public void populateDatabase() {
 		if(!dbHelper.getDatabaseInitialized()) {
 			dbHelper.initializeDatabase();
 		}
 	}
 	
 	public Word createWord(String type, String value, String category) {
 		ContentValues values = new ContentValues();
 		values.put(WordsTemplate.COLUMN_WORD, value);
 		values.put(WordsTemplate.COLUMN_WORD_TYPE, type);
 		values.put(WordsTemplate.COLUMN_CATEGORY, category);
 		long insertId = database.insert(WordsTemplate.TABLE_NAME, null,
 				values);
 		Cursor cursor = database.query(WordsTemplate.TABLE_NAME,
 				allColumns, null, null,
 				null, null, null);
 		cursor.moveToFirst();
 		Word newWord = cursorToWord(cursor);
 		cursor.close();
 		return newWord;
 	}
 
 	public void deleteWord(Word word) {
 		long id = word.getId();
 		System.out.println("Comment deleted with id: " + id);
 		database.delete(WordsTemplate.TABLE_NAME, WordsTemplate.COLUMN_ID
 				+ " = " + id, null);
 	}
 
 	private Word cursorToWord(Cursor cursor) {
 		Word comment = new Word();
 		comment.setId(cursor.getLong(0));
 		comment.setType(cursor.getString(1));
		comment.setCategory(cursor.getString(2));
		comment.setWord(cursor.getString(3));
 		return comment;
 	}
 
 	public List<Word> getAllWords() {
 		List<Word> comments = new ArrayList<Word>();
 
 		Cursor cursor = database.query(WordsTemplate.TABLE_NAME,
 				allColumns, null, null, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Word comment = cursorToWord(cursor);
 			comments.add(comment);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return comments;
 	}
 	
 	public List<Word> getAllWords(String wordType) {
 		List<Word> words = new ArrayList<Word>();
 		String[] whereArgs = new String[] {
 			    wordType
 			};
 
 		Cursor cursor = database.query(WordsTemplate.TABLE_NAME,
 				allColumns, WordsTemplate.COLUMN_WORD_TYPE + " = ? ", whereArgs, null, null, null);
 
 		cursor.moveToFirst();
 		while (!cursor.isAfterLast()) {
 			Word comment = cursorToWord(cursor);
 			words.add(comment);
 			cursor.moveToNext();
 		}
 		// Make sure to close the cursor
 		cursor.close();
 		return words;
 	}
 	
 	public Cursor getCategories() {
 //		return database.rawQuery("Select DISTINCT " + WordsTemplate.COLUMN_CATEGORY + " from " + WordsTemplate.TABLE_NAME, null);
 		return database.query(WordsTemplate.TABLE_NAME, category, null, null, null, null, null);
 	}
 
 }
