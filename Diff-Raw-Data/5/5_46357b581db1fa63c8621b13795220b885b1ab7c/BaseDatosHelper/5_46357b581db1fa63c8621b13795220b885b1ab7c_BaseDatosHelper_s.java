 package com.mindframe.speechcards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mindframe.speechcards.model.Card;
 import com.mindframe.speechcards.model.Category;
 import com.mindframe.speechcards.model.Speech;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteDatabase.CursorFactory;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 import android.util.Log;
 
 /**
  * 
  * Crea la base de datos y realiza 
  * todas las operaciones entre la bdd y la app
  * 
  * 
  * @author mindframe
  *
  */
 public class BaseDatosHelper extends SQLiteOpenHelper {
 
 //	String SQL_CREATE_SPEECH = "create table speech (id_speech integer, title text)";
 //	String SQL_CREATE_CARD = "create table card (id_card integer, id_speech integer, id_prev_card integer, id_next_card intenger, header text, body text)";
 
 	public static final String TABLE_NAME_SPEECH = "SPEECH";
 	public static final String TABLE_NAME_CARD = "CARD";
 	public static final String TABLE_NAME_CATEGORY = "CATEGORY";
 	
 	final String TAG = getClass().getName();
 
 	public static class speechColums implements BaseColumns {
 		public static final String TITLE = "TITLE";
 		public static final String SIZE = "SIZE";
 		public static final String COLOR = "COLOR";
 		public static final String ID_CATEGORY = "ID_CATEGORY";
 		
 	}
 	
 	public static class categoryColumns implements BaseColumns{
 		public static final String NAME = "name";
 		public static final String COLOR = "color";
 	}
 
 	public static class cardColumns implements BaseColumns {
 		public static final String ID_SPEECH = "ID_SPEECH";
 		public static final String ID_PREV_CARD = "ID_PREV_CARD";
 		public static final String ID_NEXT_CARD = "ID_NEXT_CARD";
 		public static final String HEADER = "HEADER";
 		public static final String BODY = "BODY";
 	}
 
 	public BaseDatosHelper(Context context, String name, CursorFactory factory, int version) {
 		super(context, name, factory, version);
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		createTables(db);
 		// db.execSQL(SQL_CREATE_CARD);dddd
 		// db.execSQL(SQL_CREATE_SPEECH);
 	}
 	
 	public void createTables(SQLiteDatabase db){
 		
 		StringBuilder createTableSpeech = new StringBuilder();
 		StringBuilder createTableCard = new StringBuilder();
 		StringBuilder createTableCategory = new StringBuilder();
 
 		createTableSpeech.append("CREATE TABLE " + TABLE_NAME_SPEECH + " (");
 		createTableSpeech.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
 		createTableSpeech.append(speechColums.TITLE + " TEXT NOT NULL, ");
 		createTableSpeech.append(speechColums.SIZE + " INTEGER, ");
 		createTableSpeech.append(speechColums.COLOR + " TEXT, ");
 		createTableSpeech.append(speechColums.ID_CATEGORY + " INTEGER");
 		createTableSpeech.append(");");
 
 		createTableCard.append("CREATE TABLE " + TABLE_NAME_CARD + " (");
 		createTableCard.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
 		createTableCard.append(cardColumns.ID_SPEECH + " INTEGER NOT NULL, ");
 		createTableCard.append(cardColumns.ID_PREV_CARD + " INTEGER, ");
 		createTableCard.append(cardColumns.ID_NEXT_CARD + " INTEGER, ");
 		createTableCard.append(cardColumns.HEADER + " TEXT, ");
 		createTableCard.append(cardColumns.BODY + " TEXT, ");
 		createTableCard.append("FOREIGN KEY (" + cardColumns.ID_SPEECH + ") REFERENCES " + TABLE_NAME_SPEECH+"("+BaseColumns._ID+") ");
 		createTableCard.append(");");
 		
 		createTableCategory.append("CREATE TABLE " + TABLE_NAME_CATEGORY + " (");
 		createTableCategory.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
 		createTableCategory.append(categoryColumns.NAME + " TEXT, ");
 		createTableCategory.append(categoryColumns.COLOR + " TEXT");
 		createTableCategory.append(");");
 
 		db.execSQL(createTableSpeech.toString());
 		db.execSQL(createTableCard.toString());
 		db.execSQL(createTableCategory.toString());
 	}
 
 	//Actualización version 3 db
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		Log.d(TAG, "upgradeDB. Old: " + oldVersion + ", new: " + newVersion);
 		
 		try{
 			db.execSQL("ALTER TABLE SPEECH ADD SIZE TEXT");
 		}catch (SQLiteException e) {
 			//ya está hecho
 		}
 		try{
 			db.execSQL("ALTER TABLE SPEECH ADD COLOR TEXT");
 		}catch (SQLiteException e) {
 			//ya está hecho
 		}
 		try{
 			db.execSQL("ALTER TABLE SPEECH ADD ID_CATEGORY INTEGER");
 		}catch (SQLiteException e) {
 			//ya está hecho
 		}
 		
 		
 		StringBuilder createTableCategory = new StringBuilder();
 		
 		createTableCategory.append("CREATE TABLE " + TABLE_NAME_CATEGORY + " (");
 		createTableCategory.append(BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,");
 		createTableCategory.append(categoryColumns.NAME + " TEXT, ");
 		createTableCategory.append(categoryColumns.COLOR + " TEXT");
 		createTableCategory.append(");");
 		
 		try{
 			db.execSQL(createTableCategory.toString());
 		}catch (SQLiteException e) {
 			//ya está hecho
 		}
 		
 		
 		
 	}
 
 	public int newSpeech(Speech speech) {
 		
 		int id_speech;
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues cv = new ContentValues();
 		cv.put(speechColums.TITLE, speech.getTitle().trim());
 		cv.put(speechColums.SIZE, speech.getSize());
 		cv.put(speechColums.ID_CATEGORY, speech.getId_category());
 		cv.put(speechColums.COLOR, speech.getColor());
 		
 		id_speech = (int) db.insert(TABLE_NAME_SPEECH, null, cv);
 		db.close();
 		
 		return id_speech;
 	}
 
 	public boolean existsSpeech(String title) {
 		boolean exists = false;
 		
		String sql = "SELECT * FROM " + TABLE_NAME_SPEECH + " WHERE " + speechColums.TITLE + " = '" + title.trim() + "' COLLATE NOCASE";
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		
 		Cursor c = db.rawQuery(sql, null);
 		
 		if(c.getCount() > 0){
 			exists = true;
 		}
 		db.close();
 		
 		
 		return exists;
 	}
 	
 	public List<Speech> getSpeechList(){
 		
 		List<Speech> speechList = new ArrayList<Speech>();
 		
 		String[] columns = {speechColums._ID, speechColums.TITLE, speechColums.SIZE, speechColums.COLOR, speechColums.ID_CATEGORY};
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor c = db.query(TABLE_NAME_SPEECH, columns, null, null, null, null, speechColums.ID_CATEGORY + " desc", null);
 		
 		if(c.moveToFirst()){
 			do{
 				Speech speech = new Speech();
 				speech.setId_speech(c.getInt(0));
 				speech.setTitle(c.getString(1));
 				speech.setSize(c.getInt(2));
 				speech.setColor(c.getString(3));
 				speech.setId_category(c.getInt(4));
 				
 				speechList.add(speech);
 				
 			}while(c.moveToNext());
 		}
 		
 		db.close();
 		return speechList;
 	}
 	/**
 	 * 
 	 * Si actCard es true, tenemos que actualizar la tarjeta anterior(id_prev_card) poniendole en 
 	 * id_next_card el id de esta nueva tarjeta, que nos lo devolverá al insertarla.
 	 * 
 	 * @param card
 	 * @param actCard
 	 * @return idNewCard
 	 */
 	public int addCard(Card card, boolean actCard) {
 		
 		int idNewCard;
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(cardColumns.BODY, card.getBody());
 		values.put(cardColumns.HEADER, card.getHeader());
 		values.put(cardColumns.ID_SPEECH, card.getId_speech());
 		values.put(cardColumns.ID_PREV_CARD, card.getId_prev_card());
 		values.put(cardColumns.ID_NEXT_CARD, card.getId_next_card());
 		
 		
 		idNewCard = (int) db.insert(TABLE_NAME_CARD, null, values);
 		
 		if(actCard){
 			updatePrev(card.getId_speech(), card.getId_prev_card(), idNewCard);
 		}
 		//Es una tarjeta de enmedio, tenemos que actualizar el id_prev de la siguiente.
 		if(card.getId_next_card() != 0){
 			updateNext(card.getId_speech(), card.getId_next_card(), idNewCard);
 		}
 			
 		db.close();
 		return idNewCard;
 	}
 	
 	
 	
 
 	/**
 	 * Cuando agreguemos una tarjeta nueva, tenemos 
 	 * que actualizar el id_next_card de la anterior
 	 * 
 	 * 
 	 * @param id_speech
 	 * @param id_prev_card
 	 * @param idNewCard
 	 */
 	private void updatePrev(int id_speech, int id_prev_card, int idNewCard) {
 	
 		ContentValues values = new ContentValues();
 		
 		values.put(cardColumns.ID_NEXT_CARD, idNewCard);
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		String where = cardColumns.ID_SPEECH + "= " + id_speech + " and " + cardColumns._ID + " = " + id_prev_card ; 
 		
 		db.update(TABLE_NAME_CARD, values, where, null);
 		
 		db.close();
 		
 	}
 	
 	/**
 	 * Cuando añadamos una tarjeta nueva al final 
 	 * tenemos que modificar el id_prev de la siguiente.
 	 * 
 	 * @param id_speech
 	 * @param id_prev_card
 	 * @param idNewCard
 	 */
 	private void updateNext(int id_speech, int id_next_card, int idNewCard) {
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(cardColumns.ID_PREV_CARD, idNewCard);
 		
 		String where = cardColumns.ID_SPEECH + "= " + id_speech + " and " + cardColumns._ID + " = " + id_next_card ; 
 		
 		db.update(TABLE_NAME_CARD, values, where, null);
 		
 		db.close();
 	}
 	
 //	private boolean upCard(Card curr){
 //		/**
 //		 * Caso: una tarjeta de enmedio:
 //		 * curr: La tarjeta a subir.
 //		 * prev: La tarjeta anterior.
 //		 * next: La tarjeta posterior.
 //		 * 
 //		 * next.prev = curr.prev
 //		 * 
 //		 * prev.next = curr.next
 //		 * curr.prev = prev.prev
 //		 * prev.prev = curr.id
 //		 * curr.next = prev.id
 //		 */
 //		
 //		//Si la anterior es 0 no se puede subir.
 //		if(curr.getId_prev_card() == 0){
 //			return false;
 //		}
 //		
 //		Card prev = getCardById(curr.getId_prev_card());
 //		
 //		//Si la posterior es 0 es la última tarjeta, no hay siguiente.
 //		if(curr.getId_next_card() != 0){
 //			Card next = getCardById(curr.getId_next_card());
 //			
 //			next.setId_prev_card(curr.getId_prev_card());
 //			updateCard(next);
 //		}
 //		
 //		prev.setId_next_card(curr.getId_next_card());
 //		curr.setId_prev_card(prev.getId_prev_card());
 //		prev.setId_prev_card(curr.getId_next_card());
 //		curr.setId_next_card(prev.getId_card());
 //		
 //		updateCard(curr);
 //		updateCard(prev);
 //		
 //		
 //		
 //		return true;
 //	}
 	
 //	private boolean downCard(Card curr){
 //		
 //		//Si la posterior es 0 no se puede bajar.
 //		if(curr.getId_next_card() == 0)
 //			return false;
 //		
 //		if(curr.getId_prev_card() != 0){
 //			Card prev = getCardById(curr.getId_prev_card());
 //			
 //		}
 //		Card next = getCardById(curr.getId_next_card());
 //		
 //		
 //		
 //		return true;
 //	}
 	
 	public List<Card> getCardsByIdSpeech(int id_speech){
 		List<Card> cardList = new ArrayList<Card>();
 		
 		String[] columns = {cardColumns._ID, cardColumns.ID_SPEECH, cardColumns.ID_PREV_CARD, 
 							cardColumns.ID_NEXT_CARD, cardColumns.HEADER, cardColumns.BODY};
 		String[] args = {String.valueOf(id_speech)};
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor c = db.query(TABLE_NAME_CARD, columns, "id_speech=?", args, null, null, null, null);
 		
 		if(c.moveToFirst()){
 			do{
 				Card card = new Card();
 				
 				card.setId_card(c.getInt(0));
 				card.setId_speech(c.getInt(1));
 				card.setId_prev_card(c.getInt(2));
 				card.setId_next_card(c.getInt(3));
 				card.setHeader(c.getString(4));
 				card.setBody(c.getString(5));
 				
 				cardList.add(card);
 			}while(c.moveToNext());
 		}
 		
 		db.close();
 		return cardList;
 		
 	}
 	
 	public Speech getSpeechById(int id_speech){
 		Speech speech = new Speech();
 		
 		String[] columns = {speechColums.TITLE, speechColums.SIZE, speechColums.COLOR, speechColums._ID, speechColums.ID_CATEGORY};
 		String[] args = {String.valueOf(id_speech)};
 		SQLiteDatabase db = this.getReadableDatabase();
 
 		Cursor c = db.query(TABLE_NAME_SPEECH, columns, "_id=?", args, null, null, null, null);
 		if(c.moveToFirst()){
 			speech.setTitle(c.getString(0));
 			speech.setSize(c.getInt(1));
 			speech.setColor(c.getString(2));
 			speech.setId_speech(c.getInt(3));
 			speech.setId_category(c.getInt(4));
 		}
 		
 		db.close();
 		return speech;
 	}
 	
 	public Card getCardById(int id_card){
 		Card card = new Card();
 		
 		String[] columns = {cardColumns._ID, cardColumns.ID_SPEECH, cardColumns.BODY, cardColumns.HEADER, cardColumns.ID_NEXT_CARD, cardColumns.ID_PREV_CARD};
 		String[] args = {String.valueOf(id_card)};
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor c = db.query(TABLE_NAME_CARD, columns, "_id=?", args, null, null, null, null);
 		
 		if(c.moveToFirst()){
 			card.setId_card(c.getInt(0));
 			card.setId_speech(c.getInt(1));
 			card.setBody(c.getString(2));
 			card.setHeader(c.getString(3));
 			card.setId_next_card(c.getInt(4));
 			card.setId_prev_card(c.getInt(5));
 			
 		}
 		
 		db.close();
 	
 		return card;
 	}
 
 	/**
 	 * Aquí recibiré una tarjeta a la cual se le ha 
 	 * modificado algún campo. UPDATE: 
 	 * @param card
 	 */
 	public boolean updateCard(Card card) {
 		int result = 0;
 		ContentValues val = new ContentValues();
 		val.put(cardColumns.HEADER, card.getHeader());
 		val.put(cardColumns.BODY, card.getBody());
 		val.put(cardColumns.ID_NEXT_CARD, card.getId_next_card());
 		val.put(cardColumns.ID_PREV_CARD, card.getId_prev_card());
 		val.put(cardColumns.ID_SPEECH, card.getId_speech());
 		
 		String whereClause =cardColumns._ID + " = " + card.getId_card();
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		result = db.update(TABLE_NAME_CARD, val, whereClause, null);
 		
 		db.close();
 		
 		if(result != 1)
 			return false;
 		else 
 			return true;
 	}
 	
 	public void updateSpeech(Speech speech){
 		ContentValues val = new ContentValues();
 		val.put(speechColums.TITLE, speech.getTitle());
 		val.put(speechColums.SIZE, speech.getSize());
 		val.put(speechColums.COLOR, speech.getColor());
 		val.put(speechColums.ID_CATEGORY, speech.getId_category());
 		
 		String whereClause = speechColums._ID + " =" + speech.getId_speech();
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		db.update(TABLE_NAME_SPEECH, val, whereClause, null);
 		
 		db.close();
 	}
 
 	public void updateCategory(Category cat){
 		ContentValues cv = new ContentValues();
 		cv.put(categoryColumns.COLOR, cat.getColor());
 		cv.put(categoryColumns.NAME, cat.getName());
 		
 		String whereClause = categoryColumns._ID + "=" + cat.getId();
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		db.update(TABLE_NAME_CATEGORY, cv, whereClause, null);
 		
 		db.close();
 	}
 	
 	/**
 	 * Borramos la tarjeta que recibimos y actualizamos la ordenación
 	 * de la lista. Se dan tres casos.
 	 * 1º Es la primera tarjeta:
 	 * 	Acualizamos el id_prev_card = 0 de la siguiente (si tiene) y borramos
 	 * 2º Es la última:
 	 * 	Actualizamos id_next_card = 0 de la anterior (si lo tiene) y borramos
 	 * 3º Está en medio.
 	 * 	todo lo anterior
 	 * @param card
 	 */
 	public int delCard(Card card) {
 		
 		
 		SQLiteDatabase db = this.getReadableDatabase();
 		if(card.isFirst()){
 			ContentValues cv = new ContentValues();
 			cv.put(cardColumns.ID_PREV_CARD, 0);
 			String whereClause = cardColumns._ID + " = " + card.getId_next_card();
 			db.update(TABLE_NAME_CARD, cv, whereClause, null);
 		}if(card.isLast()){
 			ContentValues cv = new ContentValues();
 			cv.put(cardColumns.ID_NEXT_CARD, 0);
 			String whereClause = cardColumns._ID + " = " + card.getId_prev_card();
 			db.update(TABLE_NAME_CARD, cv, whereClause, null);
 		}if(!card.isFirst() && !card.isLast()){
 			//Cambiamos la tarjeta previa
 			ContentValues cv_prev = new ContentValues();
 			cv_prev.put(cardColumns.ID_NEXT_CARD, card.getId_next_card());
 			String whereClause_prev = cardColumns._ID + " = " + card.getId_prev_card();
 			db.update(TABLE_NAME_CARD, cv_prev, whereClause_prev, null);
 			
 			//cambiamos la tarjeta siguiente
 			ContentValues cv_next = new ContentValues();
 			cv_next.put(cardColumns.ID_PREV_CARD, card.getId_prev_card());
 			String whereClause_next = cardColumns._ID + " = " + card.getId_next_card();
 			db.update(TABLE_NAME_CARD, cv_next, whereClause_next, null);
 		}
 		
 		//Borramos la tarjeta.
 		int result = db.delete(TABLE_NAME_CARD, cardColumns._ID + " = " + card.getId_card(), null);
 		
 		db.close();
 		
 		return result;
 	}
 	
 	
 	/**
 	 * 1º Se borran las tarjetas asociadas al discurso
 	 * 2º Se borra el discurso
 	 * 
 	 * @param id_speech
 	 */
 	public void deleteSpeech(int id_speech) {
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		String whereClauseCard = cardColumns.ID_SPEECH +  " = " + id_speech; 
 		String whereClauseSpeech = speechColums._ID + " = " + id_speech;
 		
 		db.delete(TABLE_NAME_CARD, whereClauseCard, null);
 		db.delete(TABLE_NAME_SPEECH, whereClauseSpeech, null);
 		
 		db.close();
 	}
 	
 	/**
 	 * 1º Se borrará el id_category de las colecciones asociadas
 	 * 2º Se borra la categoría
 	 * 
 	 * @param cat
 	 */
 	public void deleteCategory(Category cat){
 		SQLiteDatabase db = this.getReadableDatabase();
 		//Actualizar colecciones:
 		ContentValues cv  = new ContentValues();
 		cv.put(speechColums.ID_CATEGORY, "-1");
 		String whereClause = speechColums.ID_CATEGORY+"=?";
 		String whereArgs[] = new String[]{String.valueOf(cat.getId())};
 		
 		db.update(TABLE_NAME_SPEECH, cv, whereClause, whereArgs);
 		
 		//Borrar categoría:
 		db.delete(TABLE_NAME_CATEGORY, categoryColumns._ID+"="+cat.getId(), null);
 		
 		
 		db.close();
 	}
 
 	public List<Category> getCatList() {
 		List<Category> catList = new ArrayList<Category>();
 		
 		String[] columns = {categoryColumns._ID, categoryColumns.NAME, categoryColumns.COLOR};
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor c = db.query(TABLE_NAME_CATEGORY, columns, null, null, null, null, null);
 		
 		if(c.moveToFirst()){
 			do{
 				Category cat = new Category();
 				 cat.setId(c.getInt(0));
 				 cat.setName(c.getString(1));
 				 cat.setColor(c.getString(2));
 				 
 				 catList.add(cat);
 			}while(c.moveToNext());
 		}
 		
 		db.close();
 		
 		return catList;
 	}
 	
 	public void newCategory(Category cat){
 		
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues cv = new ContentValues();
 		cv.put(categoryColumns.NAME, cat.getName());
 		cv.put(categoryColumns.COLOR, cat.getColor());
 		db.insert(TABLE_NAME_CATEGORY, null, cv);
 		
 		db.close();
 		
 	}
 	
 	public boolean existsCategory(String catName){
 		boolean exists = false;
 		
		String sql = "SELECT * FROM " + TABLE_NAME_CATEGORY + " WHERE " + categoryColumns.NAME + " = '" + catName.trim() + "' COLLATE NOCASE";
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		Cursor c = db.rawQuery(sql, null);
 		
 		if(c.getCount() > 0){
 			exists = true;
 		}
 		db.close();
 		
 		return exists;
 	}
 	
 	public Category getCategoryById(int id){
 		Category cat = new Category();
 		
 		String[] columns = {categoryColumns._ID, categoryColumns.NAME, categoryColumns.COLOR};
 		SQLiteDatabase db = this.getReadableDatabase();
 		String[] args = {String.valueOf(id)};
 		Cursor c = db.query(TABLE_NAME_CATEGORY, columns, "_id=?", args, null, null, null);
 		
 		if(c.moveToFirst()){
 			cat.setId(c.getInt(0));
 			cat.setName(c.getString(1));
 			cat.setColor(c.getString(2));
 		}
 		db.close();
 		return cat;
 	}
 	
 }
