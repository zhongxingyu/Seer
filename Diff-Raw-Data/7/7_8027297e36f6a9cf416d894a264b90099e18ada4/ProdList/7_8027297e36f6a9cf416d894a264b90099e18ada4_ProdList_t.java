 package com.dnsalias.sanja.simplecarbocalc;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Locale;
 
 import android.content.ContentValues;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Environment;
 import android.text.TextUtils;
 import android.util.Log;
 
 
 public class ProdList {
 	private static final String LOGTAG = "SimpleCarboCalcActivity.ProdList";
 
 	/**
 	 * Field names
 	 */
 	static final String PROD_ID = "prod_id";
 	static final String PROD__ID = "_id";
 	static final String PROD_CARB = "prod_carb";
 	static final String PROD_LANG = "prod_lang";
 	static final String PROD_NAME = "prod_name";
 	static final String PROD_NAMES = "prod_names"; // for search purposes (sqlite can't search unicode correctly)
 	static final String PROD_LANGNAME = "prod_langname";
 
 	/**
 	 * tables names
 	 */
 	/* PROD_ID, PROD_CARB */
 	static final String FULLPRODLIST_TABLE_NAME = "fullprodlist";
 	/* PROD_ID, PROD_LANG, PROD_NAME, PROD_NAMES */
 	static final String FULLPRODLISTNAME_TABLE_NAME = "fullprodlistname";
 	/* PROD__ID, PROD_CARB, PROD_NAME, PROD_NAMES (for current locale) */
 	static final String PRODLIST_TABLE_NAME = "prodlist";
 	/* PROD_LANG, PROD_LANGNAME */
 	static final String LANGLIST_TABLE_NAME = "langlist";
 
 	private static final ProdList sInstance = new ProdList();
 	
 	private Hashtable<String,String> mLanguages= new Hashtable<String,String>(20);
 	private String mLangListShort[]= null;
 	private String mLangListLong[]= null;
 
 	/**
 	 * Returns the only instance of database management object
 	 * 
 	 * @return database management object
 	 */
 	public static ProdList getInstance() {
 		return sInstance;
 	}
 
 	/**
 	 * Connection to activity (for configuration and context).
 	 */
 	private SimpleCarboCalcActivity mActivity = null;
 	/**
 	 * Connection to database
 	 */
 	private ProdListOpenHelper mDbHelper = null;
 	/**
 	 * State of external storage (for backup).
 	 */
 	private boolean mExternalStorageAvailable = false;
 	private boolean mExternalStorageWriteable = false;
 	private File mExternalMyDirectory = null;
 
 	/**
 	 * empty constructor
 	 */
 	public ProdList()
 	{
 		mActivity = null;
 		mDbHelper = null;
 	}
 
 	/**
 	 * Assign activity
 	 * 
 	 * @param activity_arg
 	 *            reference to activity the database connected to
 	 */
 	public void setActivity(SimpleCarboCalcActivity activity_arg)
 	{
 		mActivity = activity_arg;
 		mDbHelper = new ProdListOpenHelper(mActivity);
 		SQLiteDatabase db= mDbHelper.getReadableDatabase();
 		updateLangHash(db);
 		db.close();
 	}
 
 	/**
 	 * Parse 'units' config option parameters
 	 * 
 	 * @param langLine
 	 *            - the line to parse
 	 * @return true in case of error, false if everything is OK
 	 */
 	private boolean parseUnitsLine(String unitParam)
 	{
 		int units;
 		try {
 			units = Integer.valueOf(unitParam).intValue();
 		} catch (NumberFormatException ex2) {
 			units = -1;
 		}
 		if (units < 0 && units > 2) {
 			Log.e(LOGTAG, "Unrecognized 'units' config value: '" + unitParam
 					+ "'");
 			return true;
 		}
 		mActivity.setUnits(units);
 		return false;
 	}
 
 	String checkLangLine(String langLine)
 	{
 		String langsDesc[] = langLine.split("\\|");
 		if (langsDesc.length < 1) {
 			Log.e(LOGTAG, "Unrecognized config lang line (|): '" + langLine
 					+ "'");
 			return null;
 		}
 		StringBuffer result= new StringBuffer(20);
 		for (int i = 0; i < langsDesc.length; i++) {
 			String lang[] = langsDesc[i].split("\\~");
 			if (lang.length != 2 || lang[0].length() != 2) {
 				Log.e(LOGTAG, "lang.length: " + lang.length
 						+ "  lang[0].length():" + lang[0].length() + "  '"
 						+ langsDesc[i] + "'");
 				Log.e(LOGTAG, "Unrecognized config lang line(~): '" + langLine
 						+ "'");
 				return null;
 			}
 			if (result.length() > 0)
 				result.append(',');
 			result.append(lang[0]);
 		}
 		return result.toString();
 	}
 	
 	/**
 	 * Parse 'lang' config option parameters
 	 * 
 	 * @param langLine
 	 *            - the line to parse
 	 * @param db
 	 *            - database to write changes
 	 * @return true in case of error, false if everything is OK
 	 */
 	private boolean parseLangLine(String langLine, SQLiteDatabase db)
 	{
 		String langsDesc[] = langLine.split("\\|");
 		if (langsDesc.length < 1) {
 			Log.e(LOGTAG, "Unrecognized config lang line (|): '" + langLine
 					+ "'");
 			return true;
 		}
 		for (int i = 0; i < langsDesc.length; i++) {
 			String lang[] = langsDesc[i].split("\\~");
 			if (lang.length != 2 || lang[0].length() != 2) {
 				Log.e(LOGTAG, "lang.length: " + lang.length
 						+ "  lang[0].length():" + lang[0].length() + "  '"
 						+ langsDesc[i] + "'");
 				Log.e(LOGTAG, "Unrecognized config lang line(~): '" + langLine
 						+ "'");
 				return true;
 			}
 			ContentValues vals = new ContentValues(2);
 			vals.put(PROD_LANG, lang[0]);
 			vals.put(PROD_LANGNAME, lang[1]);
 			db.replace(LANGLIST_TABLE_NAME, null, vals);
 		}
 	    updateLangHash(db);
 		return false;
 	}
 
 	Long makeNewProduct(SQLiteDatabase db, double proc)
 	{
 		Long id;
 		ContentValues vals = new ContentValues(2);
 		vals.put(PROD_CARB, proc);
 		vals.putNull(PROD_ID);
 		db.insert(FULLPRODLIST_TABLE_NAME, null, vals);
 		Cursor res = db.rawQuery("SELECT last_insert_rowid()", null);
 		res.moveToFirst();
 		id= res.getLong(0);
 		res.close();
 		return id;
 	}
 	
 	private long findProduct(SQLiteDatabase db, String prod)
 	{
 		long id= -1;
 		Cursor res= db.query(PRODLIST_TABLE_NAME, new String[] {PROD__ID}, PROD_NAMES + "= ?", new String[] {prod.toLowerCase()}, null, null, null);
 		if (res.moveToNext())
 			id= res.getLong(0);
 		res.close();
 		return id;
 	}
 	
 	private String parseProdInLang(String lang, String[] prodsDesc)
 	{
 		for (int i = 1; i < prodsDesc.length; i++)
 		{
 			String prod[] = prodsDesc[i].split("\\~");
 			if (prod.length != 2 || prod[0].length() != 2)
 				return null;
 			if (lang.equals(prod[0]))
 				return prod[1];
 		}
 		return null;
 	}
 	
 	String checkProdLine(String prodLine)
 	{
 		String prodsDesc[] = prodLine.split("\\|");
 		if (prodsDesc.length < 2) {
 			Log.e(LOGTAG, "Unrecognized config prod line (|): '" + prodLine
 					+ "'");
 			return null;
 		}
 		double proc;
 		try {
 			proc = Double.valueOf(prodsDesc[0]);
 		} catch (NumberFormatException ex2) {
 			proc = -1;
 		}
 		if (proc < 0 && proc > 100) {
 			Log.e(LOGTAG, "Unrecognized config prod line (0): '" + prodLine
 					+ "'");
 			return null;
 		}
 		StringBuffer result= new StringBuffer(100);
 		String lgProd= parseProdInLang(mActivity.getProdLang(), prodsDesc);
 		
 		if (lgProd != null)
 		{
 			SQLiteDatabase db= mDbHelper.getReadableDatabase();
 			result.append(findProduct(db, lgProd) >= 0 ? "# " : "+ ");
 			result.append(lgProd);
 			db.close();
 		}
 		if (result.length() == 0)
 			return null;
 		return result.toString();
 	}
 	
 	/**
 	 * Parse 'prod' config option parameters
 	 * 
 	 * @param langLine
 	 *            - the line to parse
 	 * @param db
 	 *            - database to write changes
 	 * @return true in case of error, false if everything is OK
 	 */
 	private boolean parseProdLine(String prodLine, SQLiteDatabase db)
 	{
 		String prodsDesc[] = prodLine.split("\\|");
 		if (prodsDesc.length < 2) {
 			Log.e(LOGTAG, "Unrecognized config prod line (|): '" + prodLine
 					+ "'");
 			return true;
 		}
 		double proc;
 		long id= -1;
 		try {
 			proc = Double.valueOf(prodsDesc[0]);
 		} catch (NumberFormatException ex2) {
 			proc = -1;
 		}
 		if (proc < 0 && proc > 100) {
 			Log.e(LOGTAG, "Unrecognized config prod line (0): '" + prodLine
 					+ "'");
 			return true;
 		}
 		
 		String lgProd= parseProdInLang(mActivity.getProdLang(), prodsDesc);
 		if (lgProd != null)
 		{
 			Log.v(LOGTAG, "Found Product: '" + lgProd + "'");
 			id= findProduct(db, lgProd);
 		}
 		if (id >= 0)
 		{
 			Log.v(LOGTAG, "Found Id: " + id);
 			ContentValues vals = new ContentValues(2);
 			vals.put(PROD_CARB, proc);
 			vals.put(PROD_ID, id);
 			db.replace(FULLPRODLIST_TABLE_NAME, null, vals);
 		}
 		else
 			id= makeNewProduct(db, proc);
 		Log.v(LOGTAG, "id: " + id + "  '" + prodLine + "'");
 
 		for (int i = 1; i < prodsDesc.length; i++) {
 			String prod[] = prodsDesc[i].split("\\~");
 			if (prod.length != 2 || prod[0].length() != 2) {
 				Log.e(LOGTAG, "prod.length: " + prod.length
 						+ "  prod[0].length():" + prod[0].length() + "  '"
 						+ prodsDesc[i] + "'");
 				Log.e(LOGTAG, "Unrecognized config prod line (~): '" + prodLine
 						+ "'");
 				return true;
 			}
 			ContentValues vals = new ContentValues(4);
 			vals.put(PROD_ID, id);
 			vals.put(PROD_LANG, prod[0]);
 			vals.put(PROD_NAME, prod[1]);
 			vals.put(PROD_NAMES, prod[1].toLowerCase());
 			db.replace(FULLPRODLISTNAME_TABLE_NAME, null, vals);
 		}
 		return false;
 	}
 
 	/**
 	 * Parse configuration/backup file line
 	 * 
 	 * @param line
 	 *            - the line t parse
 	 * @param db
 	 *            - database to write changes
 	 * @return true in case of error, false if everything is OK
 	 */
 	private boolean parseConfigLine(String line, SQLiteDatabase db)
 	{
 		String[] conf = TextUtils.split(line, "=");
 		if (conf.length != 2) {
 			Log.e(LOGTAG, "Unrecognized config line: '" + line + "'");
 		} else {
 			if (conf[0].equals("units")) {
 				return parseUnitsLine(conf[1]);
 			} else if (conf[0].equals("lang")) {
 				return parseLangLine(conf[1], db);
 			} else if (conf[0].equals("prod")) {
 				return parseProdLine(conf[1], db);
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Check and set the language for the product list
 	 * @param new_lang the language to set
 	 * @return language code which was really set.
 	 */
 	synchronized String setNewProdLang(String new_lang)
 	{
 		SQLiteDatabase db= mDbHelper.getWritableDatabase();
 		{ // check language
 			Cursor res= db.rawQuery("SELECT COUNT(*) FROM " + LANGLIST_TABLE_NAME + " WHERE " + PROD_LANG + " = ?;", new String[] {new_lang} );
 			res.moveToFirst();
 			if (res.getInt(0) != 1)
 			{  // assign just first legal language
 				Log.e(LOGTAG, "Language '" + new_lang + "' not found");
 				Cursor lg= db.rawQuery("SELECT " + PROD_LANG + " FROM " + LANGLIST_TABLE_NAME + ";", null);
 				if (lg.getCount() > 0)
 				{
 					lg.moveToFirst();
 					new_lang= lg.getString(0);
 					Log.w(LOGTAG, "Language '" + new_lang + "' taken");
 				}
 				else
 					Log.e(LOGTAG, "Language database is empty");
 				lg.close();
 			}
 			res.close();
 		}
 		db.beginTransaction();
 		if (!regenerateProductList(db, new_lang))
 			db.setTransactionSuccessful();
 		db.endTransaction();
 		db.close();
 		return new_lang;
 	}
 	
 	/**
 	 * Regenerate product list for given language 
 	 * @param db - database handler
 	 * @param new_lang the new language
 	 * @return true in case of error, false if everything is OK
 	 */
 	private boolean regenerateProductList(SQLiteDatabase db, String new_lang)
 	{
 		boolean ok= true;
 		db.execSQL("DELETE FROM " + PRODLIST_TABLE_NAME + ";");
 		/* Create your language table */
 		Cursor res = db.rawQuery("SELECT " + FULLPRODLISTNAME_TABLE_NAME
 				+ "." + PROD_ID + "," + PROD_CARB + "," + PROD_NAME
 				+ " FROM " + FULLPRODLIST_TABLE_NAME + ","
 				+ FULLPRODLISTNAME_TABLE_NAME + " WHERE "
 				+ FULLPRODLIST_TABLE_NAME + "." + PROD_ID + "="
 				+ FULLPRODLISTNAME_TABLE_NAME + "." + PROD_ID + " and "
 				+ PROD_LANG + "='" + new_lang
 				+ "'", null);
 		Log.v(LOGTAG, "count for '" + new_lang
 				+ "': " + res.getCount());
 		if (res.getCount() == 0) {
 			Log.e(LOGTAG, "Empty result for languge: '"
 					+ Locale.getDefault().getLanguage() + "'");
 			ok= false;
 		} else
 			while (res.moveToNext()) {
 				Log.v(LOGTAG,
 						"id: " + res.getLong(0) + "  carb: "
 								+ res.getDouble(1) + " name: '"
 								+ res.getString(2) + "'");
 				ContentValues vals = new ContentValues(4);
 				vals.put(PROD__ID, res.getLong(0));
 				vals.put(PROD_CARB, res.getDouble(1));
 				vals.put(PROD_NAME, res.getString(2));
 				vals.put(PROD_NAMES, res.getString(2).toLowerCase());
 				db.replace(PRODLIST_TABLE_NAME, null, vals);
 			}
 		;
 		res.close();
 		
 		return !ok;
 	}
 
 	synchronized boolean addConfig(String[] lines)
 	{
 		boolean ok= true;
 		SQLiteDatabase db= mDbHelper.getWritableDatabase();
 		db.beginTransaction();
 
 		for(int i= 0; i < lines.length; i++)
 		{
 			if (lines[i].length() != 0 && lines[i].charAt(0) != '#')
 			{
 				Log.v(LOGTAG, "Line: '" + lines[i] + "'");
 				if (parseConfigLine(lines[i], db)) {
 					Log.e(LOGTAG,
 						"There was errors in parsing backup file. Rollback.");
 					ok = false;
 				}
 			}
 		}
 		if (regenerateProductList(db, mActivity.getProdLang()))
 			ok= false;
 		if (ok)
 			db.setTransactionSuccessful();
 
 		db.endTransaction();
 		db.execSQL("VACUUM;");
 		db.close();
 		mActivity.dbChanged();
 		return !ok;
 	}
 
 	
 	/**
 	 * Loads configuration file.
 	 * 
 	 * @param merge
 	 *            true if we need merge (unimplemented yet)
 	 * @param inputStream
 	 *            stream with configuration
 	 * @return true in case of error, false if everything is OK
 	 */
 	synchronized boolean loadBackupFile(InputStream inputStream)
 	{
 		boolean ok= true;
 		SQLiteDatabase db= mDbHelper.getWritableDatabase();
 		db.beginTransaction();
 
 		// cleanup the database
 		db.execSQL("DELETE FROM " + FULLPRODLIST_TABLE_NAME + ";");
 		db.execSQL("DELETE FROM " + FULLPRODLISTNAME_TABLE_NAME + ";");
 		db.execSQL("DELETE FROM " + LANGLIST_TABLE_NAME + ";");
 
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				inputStream));
 
 		try {
 			String line;
 			while ((line = reader.readLine()) != null) {
 				Log.v(LOGTAG, "read: '" + line + "'");
 				if (line.length() != 0 && line.charAt(0) != '#') {
 					if (parseConfigLine(line, db)) {
 						Log.e(LOGTAG,
 								"There was errors in parsing backup file. Rollback.");
 						ok = false;
 					}
 				}
 			}
 			if (regenerateProductList(db, mActivity.getProdLang()))
 				ok= false;
 			if (ok)
 				db.setTransactionSuccessful();
 		} catch (IOException ex) {
 			Log.e(LOGTAG, "IO error: " + ex.toString());
 			ok = false;
 		} finally {
 			try {
 				reader.close();
 			} catch (IOException ex) {
 			}
 			Log.v(LOGTAG, "endTransaction");
 			db.endTransaction();
 		}
 		db.execSQL("VACUUM;");
 		db.close();
 		mActivity.dbChanged();
 		return !ok;
 	}
 
 	
 	synchronized void updateLangHash(SQLiteDatabase db)
 	{
 		Cursor res = db.query(LANGLIST_TABLE_NAME, new String[] {PROD_LANG, PROD_LANGNAME},
 				null, null, null, null, null);
 		mLanguages.clear();
 		mLangListShort= new String[res.getCount()];
 		mLangListLong= new String[res.getCount()];
 		int i= 0;
 		while(res.moveToNext())
 		{
 			mLangListShort[i]= res.getString(0);
 			mLanguages.put(mLangListShort[i], res.getString(1));
 			i++;
 		}
 		Arrays.sort(mLangListShort);
 		for(i= 0; i < mLangListLong.length; i++)
 			mLangListLong[i]= mLangListShort[i] + " " + mLanguages.get(mLangListShort[i]);
 
 		res.close();
 	}
 	
 	synchronized String getLanguageName(String key)
 	{
 		return mLanguages.get(key);
 	}
 
 	/**
 	 * Returns two lists of languages
 	 * @return array of two lists;
 	 */
 	synchronized String[][] getLangList() { return new String[][] {mLangListShort, mLangListLong}; }
 	
 	
 	/**
 	 * Loads initial configuration file in case if database is empty
 	 * 
 	 * @param resources
 	 *            resources
 	 * @return true in case of error, false if everything is OK
 	 */
 	boolean loadInitFileIfEmpty(Resources resources)
 	{
 		boolean rc = false; // OK
 		SQLiteDatabase db = mDbHelper.getReadableDatabase();
 		Cursor res = db.rawQuery("select exists(select * from "
 				+ FULLPRODLIST_TABLE_NAME + ");", null);
 		res.moveToFirst();
 		if (res.getInt(0) == 0)
 		{
 			Log.v(LOGTAG, "Data base is empty => load it");
 			rc = loadInitFile(resources);
 		}
 		res.close();
 		db.close();
 		return rc;
 	}
 
 	/**
 	 * Loads initial configuration file
 	 * 
 	 * @param resources
 	 *            resources
 	 * @return true in case of error, false if everything is OK
 	 */
 	boolean loadInitFile(Resources resources)
 	{
 		InputStream inputStream = resources
 				.openRawResource(R.raw.initial_backup);
 		return loadBackupFile(inputStream);
 	}
 
 	void checkExternal()
 	{
 		String state = Environment.getExternalStorageState();
 
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
 		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
 			mExternalStorageAvailable = true;
 			mExternalStorageWriteable = false;
 		} else {
 			mExternalStorageAvailable = mExternalStorageWriteable = false;
 		}
 		Log.v(LOGTAG, "external storage:"
 				+ (mExternalStorageAvailable ? "available " : "unavaliable ")
 				+ (mExternalStorageWriteable ? "writable" : "non-writable"));
 		if (mExternalStorageWriteable) {
 			File extDir = Environment.getExternalStorageDirectory();
 			if (mExternalMyDirectory == null)
 				mExternalMyDirectory = new File(extDir, "SimpleCarboCalc");
 			if (!mExternalMyDirectory.isDirectory()) {
 				if (!mExternalMyDirectory.mkdir()) {
 					Log.e(LOGTAG, "Can't create directory: "
 							+ mExternalMyDirectory);
 					mExternalStorageWriteable = false;
 				} else
 					Log.v(LOGTAG, "Directory created: " + mExternalMyDirectory);
 			} else
 				Log.v(LOGTAG, "Directory: " + mExternalMyDirectory);
 		}
 	}
 
 	void writeUnits(OutputStream out) throws IOException
 	{
 		StringBuffer str = new StringBuffer(10);
 		str.append("units=");
 		str.append(mActivity.getUnits());
 		str.append('\n');
 		out.write(str.toString().getBytes());
 	}
 
 	void writeLangs(OutputStream out, SQLiteDatabase db) throws IOException
 	{
 		StringBuffer str = new StringBuffer(100);
 		str.append("lang=");
 		String fields[] = { PROD_LANG, PROD_LANGNAME };
 		Cursor res = db.query(LANGLIST_TABLE_NAME, fields, null, null, null,
 				null, null);
 		if (res.getCount() == 0) {
 			Log.e(LOGTAG, "No laguages in DB");
 		}
 		boolean first = true;
 		while (res.moveToNext()) {
 			if (!first)
 				str.append('|');
 			str.append(res.getString(0));
 			str.append('~');
 			str.append(res.getString(1));
 			first = false;
 		}
 		res.close();
 		str.append('\n');
 		out.write(str.toString().getBytes());
 	}
 
 	void writeProds(OutputStream out, SQLiteDatabase db, long products[]) throws IOException
 	{
 		int current= 0;
 		String fieldsHeader[] = { PROD_ID, PROD_CARB };
 		String fields[] = { PROD_LANG, PROD_NAME };
 
 		if (products != null)
 			Arrays.sort(products);
 		Log.v(LOGTAG, "products: " + (products == null ? "<NULL>" : Arrays.toString(products)));
 		Cursor resHeader = db.query(FULLPRODLIST_TABLE_NAME, fieldsHeader,
 				null, null, null, null, PROD_ID);
 		if (resHeader.getCount() == 0) {
 			Log.e(LOGTAG, "No Prods in DB");
 		}
 		while (resHeader.moveToNext()) {
 			long prodID= resHeader.getLong(0);
 			while(products != null && current < products.length && prodID > products[current]) current++;
 			if (products == null || (current < products.length && prodID == products[current]))
 			{
 				StringBuffer str = new StringBuffer(100);
 				str.append("prod=");
 				str.append(resHeader.getDouble(1));
 				Cursor res = db.query(FULLPRODLISTNAME_TABLE_NAME, fields, PROD_ID
 						+ "=" + prodID, null, null, null, null, null);
 				if (res.getCount() == 0) {
 					Log.e(LOGTAG, "No Prod Names in DB :" + resHeader.getLong(0));
 				}
 				while (res.moveToNext()) {
 					str.append('|');
 					str.append(res.getString(0));
 					str.append('~');
 					str.append(res.getString(1));
 				}
 				str.append('\n');
 				out.write(str.toString().getBytes());
 			}
 		}
 		resHeader.close();
 	}
 
 	boolean restoreBackupConfig()
 	{
 		boolean res= false;
 		checkExternal();
 		if (!mExternalStorageAvailable)
 			return true;
 		File backup = new File(mExternalMyDirectory, "backup.txt");
 		InputStream in= null;
 
 		try
 		{
 			in= new BufferedInputStream(new FileInputStream(backup));
 		}
 		catch (FileNotFoundException ex)
 		{
 			res= true;
 		}
 		if (in != null)
 			res= loadBackupFile(in);
 		return res;
 	}
 	
 	/**
 	 * backup configuration into an external storage file 
 	 * @return false - success, true - error
 	 */
 	boolean SaveConfig(String file, boolean saveUnits, boolean saveLanguages, boolean saveProducts, long products[])
 	{
 		checkExternal();
 		if (!mExternalStorageWriteable)
 			return true;
 		File backup = new File(file);
 		try
 		{
 			if (!backup.isFile() && !backup.createNewFile()) {
 				Log.e(LOGTAG, "Can't create file " + backup);
 				return true;
 			}
 		} catch (IOException ex) {
 			Log.e(LOGTAG, "Can't create file '" + backup + "': " + ex);
 			return true;
 		}
 		OutputStream out = null;
 		try
 		{
 			out = new BufferedOutputStream(new FileOutputStream(backup));
 			SQLiteDatabase db = mDbHelper.getReadableDatabase();
 			if (saveUnits)
 				writeUnits(out);
 			if (saveLanguages)
 				writeLangs(out, db);
 			if (saveProducts)
 				writeProds(out, db, products);
 			out.flush();
 			db.close();
 		}
 
 		catch (FileNotFoundException ex) {
 			Log.e(LOGTAG, "Can't find file '" + backup + "': " + ex);
 			return true;
 		}
 
 		catch (IOException ex) {
 			Log.e(LOGTAG, "Can't write file '" + backup + "': " + ex);
 			return true;
 		}
 
 		finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException ex) {
 				}
 				;
 			}
 		}
 		return false;
 	}
 
 	Cursor getCoursorForRequest(String req, long id) {
 		String where= null;
 		String vars[]= null;
 		SQLiteDatabase db= mDbHelper.getReadableDatabase();
 		String fields[] = { PROD__ID, PROD_NAME, PROD_CARB };
 		if (id < 0)
 		{
 			if (req != null && req.length() != 0)
 			{
 				vars= new String[] { req.toLowerCase() + "*" };
 				where= PROD_NAMES + " MATCH ?";
 			}
 		}
 		else	
 		{
 			where= PROD__ID + "=" + Long.toString(id);
 		}
 		Log.v(LOGTAG, "where: '" + (where == null ? "<NULL>" : where.toString()) + "'   vars: "+ Arrays.toString(vars));
 		Cursor result=  db.query(PRODLIST_TABLE_NAME, fields, where, vars, null, null,
 				null);
 		Log.v(LOGTAG, "results: " + result.getCount());
 		return result;
 	}
 		
 	double getCarbProc(long id)
 	{
 		double proc;
 		SQLiteDatabase db= mDbHelper.getReadableDatabase();
 		String fields[]= { PROD_CARB };
 		Cursor res= db.query(FULLPRODLIST_TABLE_NAME, fields, PROD_ID + "=" + id,
 				null, null, null, null);
 		res.moveToFirst();
 		proc= res.getFloat(0);
 		res.close();
 		db.close();
 		return proc;
 	}
 	
 	String getProdName(long id)
 	{
 		String name;
 		SQLiteDatabase db= mDbHelper.getReadableDatabase();
 		String fields[]= { PROD_NAME };
 		Cursor res= db.query(PRODLIST_TABLE_NAME, fields, PROD__ID + "=" + id,
 				null, null, null, null);
 		res.moveToFirst();
 		name= res.getString(0);
 		res.close();
 		db.close();
 		return name;
 	}
 
 	public boolean changeName(SQLiteDatabase db, long id, String lang, String name)
 	{
 		assertEquals(lang.length(), 2);
 		ContentValues vals= new ContentValues(4);
 		vals.put(PROD_ID, id);
 		vals.put(PROD_LANG, lang);
 		vals.put(PROD_NAME, name);
 		vals.put(PROD_NAMES, name.toLowerCase());
 		Log.v(LOGTAG, "try to replace: " + vals.toString());
 		return (db.replace(FULLPRODLISTNAME_TABLE_NAME, null, vals) != -1);
 	}
 	
 	public boolean removeNameIfExists(SQLiteDatabase db, long id, String lang)
 	{
 		db.delete(FULLPRODLISTNAME_TABLE_NAME, PROD_ID + "=? AND " + PROD_LANG + "= ?", new String[] {Long.toString(id), lang});
 		return false;
 	}
 
 	public String editProduct(Long id, Double proc, String[] langs, String[] names)
 	{
 		String result= null;
 		SQLiteDatabase db= mDbHelper.getWritableDatabase();
 		db.beginTransaction();
 		Log.v(LOGTAG, "id: " + id + "  proc: " + proc + "  langs: " + Arrays.toString(langs) + "  names: " + Arrays.toString(names));
 		if (id < 0)
 		{
 			// Adding new...
 			id= makeNewProduct(db, proc);
 		}
 		else
 		{
 			// Changing percent
 			ContentValues vals = new ContentValues(1);
 			vals.put(PROD_CARB, proc);
 			if (db.update(FULLPRODLIST_TABLE_NAME, vals, PROD_ID + "=" + id, null) == 0)
 			{
 				db.endTransaction(); // abort
 				return "id not found";
 			}
 		}
 		int count= 0;
 		for (int i= 0; i < langs.length; i++)
 		{
 			if (names[i] != null && names[i].length() != 0)
 			{
 				changeName(db, id, langs[i], names[i]);
 				count++;
 			}
 			else
 			{
 				if (removeNameIfExists(db, id, langs[i]))
 				{
 					db.endTransaction();
 					return "problem of deleting name on " + langs[i];
 				}
 			}
 		}
 		if (count > 0)
 		{
 			db.setTransactionSuccessful();
 			regenerateProductList(db, mActivity.getProdLang());
 		}
 		else
 			result= "no product names in any language";
 		db.endTransaction();
 		db.close();
 		return result;
 	}
 	
 	void getNames(long id, String[] langs, String[] names)
 	{
 		SQLiteDatabase db= mDbHelper.getReadableDatabase();
 		Cursor res= db.query(FULLPRODLISTNAME_TABLE_NAME, new String[] { PROD_LANG, PROD_NAME }, PROD_ID + "=?", new String[] { Long.toString(id) },
 				null, null, null);
 		while(res.moveToNext())
 		{
 			String lang= res.getString(0);
 			int idx= Arrays.binarySearch(langs, lang);
 			if (idx >= 0)
 				names[idx]= res.getString(1);
 			else
 				Log.v(LOGTAG, "Language '" + lang + "' is not requested");
 		}
 		res.close();
 		db.close();
 	}
 	
 	boolean removeProduct(long id)
 	{
 		boolean result= false;
 		if (id > 0)
 		{
 			SQLiteDatabase db= mDbHelper.getWritableDatabase();
 			String val[]= {Long.toString(id)};
 			db.beginTransaction();
 			if (db.delete(FULLPRODLIST_TABLE_NAME, PROD_ID + "=?", val) <= 0)
 				result= true;
 			if (db.delete(FULLPRODLISTNAME_TABLE_NAME, PROD_ID + "=?", val) <= 0)
 				result= true;
 			if (!result)
 			{
 				db.setTransactionSuccessful();
 				regenerateProductList(db, mActivity.getProdLang());
 			}
 			db.endTransaction();
 			db.close();
 		}
 		return result;
 	}
 }
