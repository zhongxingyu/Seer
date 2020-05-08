 /**
  * ManagerDatabase,		part of Aptoide
  * 
  * Copyright (C) 2011  Duarte Silveira
  * duarte.silveira@caixamagica.pt
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 package pt.caixamagica.aptoide.appsbackup.data.database;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.TimeZone;
 
 import pt.caixamagica.aptoide.appsbackup.EnumAppsSorting;
 import pt.caixamagica.aptoide.appsbackup.data.AptoideServiceData;
 import pt.caixamagica.aptoide.appsbackup.data.ViewAppVersionRepository;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionExtras;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionInfo;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionStats;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayAppVersionsInfo;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplication;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplicationAvailable;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplicationBackup;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplicationInstalled;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayApplicationUpdatable;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayCategory;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListApps;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayListRepos;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayLogin;
 import pt.caixamagica.aptoide.appsbackup.data.display.ViewDisplayRepo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewAppComment;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewAppDownloadInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplication;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewApplicationInstalled;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewCategory;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewExtraInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewIconInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewListIds;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewLogin;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewRepository;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewScreenInfo;
 import pt.caixamagica.aptoide.appsbackup.data.model.ViewStatsInfo;
 import pt.caixamagica.aptoide.appsbackup.data.preferences.EnumAgeRating;
 import pt.caixamagica.aptoide.appsbackup.data.system.ViewHwFilters;
 import pt.caixamagica.aptoide.appsbackup.data.util.Constants;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.EnumDownloadType;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewDownload;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewDownloadInfo;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewListAppsDownload;
 import pt.caixamagica.aptoide.appsbackup.data.webservices.ViewUploadInfo;
 import pt.caixamagica.aptoide.appsbackup.debug.exceptions.AptoideExceptionDatabase;
 import pt.caixamagica.aptoide.appsbackup.ifaceutil.EnumAppStatus;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.DatabaseUtils.InsertHelper;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Build;
 import android.util.Log;
 
 /**
  * ManagerDatabase, manages aptoide's sqlite data persistence
  * 					regarding concurrency, the usage of SQLiteDatabase enforces a readers/writers model
  * 					so we don't need to worry about that in this manager.
  * 
  * @author dsilveira
  * @since 3.0
  *
  */
 public class ManagerDatabase {
 
 	private AptoideServiceData serviceData;
 	private static SQLiteDatabase db = null;
 	private ArrayList<Integer> categories_hashids;
 	
 	/**
 	 * ManagerDatabase Constructor, opens Aptoide's database or creates it if it doens't exist yet 
 	 *
 	 * @param Context context, Aptoide's activity context
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ManagerDatabase(AptoideServiceData serviceData) {
 		this.serviceData = serviceData;
 		if(db == null){
 			db = serviceData.openOrCreateDatabase(Constants.DATABASE, 0, null);
 			db.beginTransaction();
 			
 			db.execSQL(Constants.CREATE_TABLE_REPOSITORY);
 			db.execSQL(Constants.CREATE_TABLE_LOGIN);
 			db.execSQL(Constants.CREATE_TABLE_APPLICATION);
 			db.execSQL(Constants.CREATE_TABLE_CATEGORY);
 			db.execSQL(Constants.CREATE_TABLE_SUB_CATEGORY);
 			db.execSQL(Constants.CREATE_TABLE_APP_CATEGORY);
 			db.execSQL(Constants.CREATE_TABLE_APP_TO_INSTALL);
 			db.execSQL(Constants.CREATE_TABLE_APP_INSTALLED);
 			db.execSQL(Constants.CREATE_TABLE_ICON_INFO);
 			db.execSQL(Constants.CREATE_TABLE_SCREEN_INFO);
 			db.execSQL(Constants.CREATE_TABLE_DOWNLOAD_INFO);
 			db.execSQL(Constants.CREATE_TABLE_STATS_INFO);
 			db.execSQL(Constants.CREATE_TABLE_EXTRA_INFO);
 			db.execSQL(Constants.CREATE_TABLE_APP_COMMENTS);
 			
 			db.execSQL(Constants.CREATE_TRIGGER_REPO_DELETE_REPO);
 			db.execSQL(Constants.CREATE_TRIGGER_REPO_UPDATE_REPO_HASHID_STRONG);
 			db.execSQL(Constants.CREATE_TRIGGER_LOGIN_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_LOGIN_UPDATE_REPO_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APPLICATION_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_APPLICATION_UPDATE_REPO_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APPLICATION_DELETE);
 			db.execSQL(Constants.CREATE_TRIGGER_APPLICATION_UPDATE_APP_FULL_HASHID_STRONG);
 			db.execSQL(Constants.CREATE_TRIGGER_CATEGORY_DELETE);
 			db.execSQL(Constants.CREATE_TRIGGER_CATEGORY_UPDATE_CATEGORY_HASHID_STRONG);
 			db.execSQL(Constants.CREATE_TRIGGER_SUB_CATEGORY_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_SUB_CATEGORY_UPDATE_PARENT_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_SUB_CATEGORY_UPDATE_CHILD_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_CATEGORY_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_CATEGORY_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_CATEGORY_UPDATE_CATEGORY_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_TO_INSTALL_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_TO_INSTALL_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_ICON_INFO_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_ICON_INFO_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_SCREEN_INFO_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_SCREEN_INFO_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_DOWNLOAD_INFO_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_DOWNLOAD_INFO_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_STATS_INFO_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_STATS_INFO_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_EXTRA_INFO_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_EXTRA_INFO_UPDATE_APP_FULL_HASHID_WEAK);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_COMMENT_INSERT);
 			db.execSQL(Constants.CREATE_TRIGGER_APP_COMMENT_UPDATE_APP_FULL_HASHID_WEAK);
 			
 			db.execSQL(Constants.CREATE_INDEX_APPLICATION_HASHID);
 			db.execSQL(Constants.CREATE_INDEX_APPLICATION_REPO_HASHID);
 			db.execSQL(Constants.CREATE_INDEX_APPLICATION_PACKAGE_NAME);
 			db.execSQL(Constants.CREATE_INDEX_APP_INSTALLED_PACKAGE_NAME);
 			
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			hammerCategories();
 			Log.d("Aptoide-ManagerDatabase", "Database created!");
 		}else if(!db.isOpen()){
 			db = serviceData.openOrCreateDatabase(Constants.DATABASE, 0, null);
 			Log.d("Aptoide-ManagerDatabase", "Database opened!");
 		}
 		db.execSQL(Constants.PRAGMA_FOREIGN_KEYS_OFF);
 //		db.execSQL(Constants.PRAGMA_RECURSIVE_TRIGGERS_OFF);
 	}
 	
 	/**
 	 * CloseDB, handles closing of db
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void closeDB(){
 		db.close();
 		Log.d("Aptoide-ManagerDatabase", "Database closed!");
 	}	
 	
 	/**
 	 * optimizeQuerys, forces SGBD to optimize query planning 
 	 * 				   thus optimizing queries execution 
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void optimizeQuerys(){
 		try{
 			db.beginTransaction();
 			db.execSQL(Constants.ANALYZE);
 			db.setTransactionSuccessful();
 			Log.d("Aptoide-ManagerDatabase", "Database optimized!");
 		}catch (Exception e) {
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	/* ******************************************************** *
 	 * 															*
 	 *                     Dirty methods						*
 	 * 															*
 	 * ******************************************************** */
 	
 	
 	
 	
 	/**
 	 * prepareToTorchDB, handles smooth transition from old database schema
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public HashMap<String,Boolean> prepareToTorchDB(){
 								/* hard coded strings that represent old names */
 		Cursor cursor = db.query("servers", new String[]{"uri", "inuse"}, null, null, null, null, null);
 		HashMap<String, Boolean> repositories = new HashMap<String, Boolean>(cursor.getCount());
 		final int oldIndexRepoUri = 0;
 		final int oldIndexRepoInUse = 1;
 		cursor.moveToFirst();
 		do{		//TODO refactor to return Repository objects + Login Objects
 			repositories.put(cursor.getString(oldIndexRepoUri), cursor.getInt(oldIndexRepoInUse) == Constants.DB_TRUE?true:false);
 		} while(cursor.moveToNext());
 		cursor.close();
 
 		return repositories; 
 		
 		//TODO close db, delete db file
 		
 		//TODO call constructor
 		
 		//TODO re-populate repositories
 		
 	}
 	
 	
 	
 	/**
 	 * hammerCategories, inserts a hammered list of categories 
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void hammerCategories(){
 		categories_hashids = new ArrayList<Integer>();
 		ArrayList<ViewCategory> categories = new ArrayList<ViewCategory>(2);
 		
 		ViewCategory applications = new ViewCategory(Constants.CATEGORY_APPLICATIONS);
 		for (String subCategory : Constants.SUB_CATEGORIES_APPLICATIONS) {
 			applications.addChild(new ViewCategory(subCategory));
 			categories_hashids.add(subCategory.hashCode());
 		}
 
 		categories.add(applications);
 		
 		ViewCategory games = new ViewCategory(Constants.CATEGORY_GAMES);
 		for (String subCategory : Constants.SUB_CATEGORIES_GAMES) {
 			games.addChild(new ViewCategory(subCategory));
 			categories_hashids.add(subCategory.hashCode());
 		}
 		
 		categories.add(games);
 		
 		ViewCategory others = new ViewCategory(Constants.CATEGORY_OTHERS);
 		categories.add(others);
 		
 		insertCategories(categories);
 		Log.d("Aptoide-ManagerDatabase", "Categories Hammered in!");
 		
 	}
 	
 	
 	/* ******************************************************** *
 	 * 															*
 	 *                     Writer methods						*
 	 * 															*
 	 * ******************************************************** */
 	
 	
 	
 	/**
 	 * insertCategories, handles multiple categories insertion
 	 * 
 	 * @param ArrayList<Category> categories
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertCategories(ArrayList<ViewCategory> categories){
 		try{			
 			db.beginTransaction();
 			InsertHelper insertCategory = new InsertHelper(db, Constants.TABLE_CATEGORY);
 			ArrayList<ContentValues> subCategoriesRelations = new ArrayList<ContentValues>(categories.size());
 			ContentValues subCategoryRelation;
 			
 			for (ViewCategory category : categories) {
 				if(insertCategory.replace(category.getValues()) == Constants.DB_ERROR){		//TODO should be insert
 					//TODO throw exception;
 				}
 				if(category.hasChilds()){
 					for (ViewCategory subCategory : category.getSubCategories()) {
 						if(insertCategory.replace(subCategory.getValues()) == Constants.DB_ERROR){		//TODO should be insert
 							//TODO throw exception;
 						}
 						subCategoryRelation = new ContentValues(Constants.NUMBER_OF_COLUMNS_SUB_CATEGORY); //TODO check if this implicit object recycling works 
 						subCategoryRelation.put(Constants.KEY_SUB_CATEGORY_PARENT, category.getHashid());
 						subCategoryRelation.put(Constants.KEY_SUB_CATEGORY_CHILD, subCategory.getHashid());
 						subCategoriesRelations.add(subCategoryRelation);
 					}
 				}
 			}
 			insertCategory.close();
 			
 			InsertHelper insertCategoryRelation = new InsertHelper(db, Constants.TABLE_SUB_CATEGORY);
 			for (ContentValues categoryRelationValues : subCategoriesRelations) {
 				if(insertCategoryRelation.replace(categoryRelationValues) == Constants.DB_ERROR){		//TODO should be insert
 					//TODO throw exception;
 				}
 			}
 			insertCategoryRelation.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertCategory, handles single category insertion
 	 * 
 	 * @param ViewCategory category
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertCategory(ViewCategory category){
 		try{
 			db.beginTransaction();
 			ContentValues parentCategoryRelation;
 			
 			if(db.insert(Constants.TABLE_CATEGORY, null, category.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			if(!(category.getParentHashid() == Constants.TOP_CATEGORY)){
 				parentCategoryRelation = new ContentValues(Constants.NUMBER_OF_COLUMNS_SUB_CATEGORY); //TODO check if this implicit object recycling works 
 				parentCategoryRelation.put(Constants.KEY_SUB_CATEGORY_PARENT, category.getParentHashid());
 				parentCategoryRelation.put(Constants.KEY_SUB_CATEGORY_CHILD, category.getHashid());
 
 				if(db.insert(Constants.TABLE_SUB_CATEGORY, null, parentCategoryRelation) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	/**
 	 * insertRepositories, handles multiple repositories insertion
 	 * 
 	 * @param ArrayList<Repository> repositories
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertRepositories(ArrayList<ViewRepository> repositories){
 		try{
 			db.beginTransaction();
 			InsertHelper insertRepository = new InsertHelper(db, Constants.TABLE_REPOSITORY);
 			ArrayList<ContentValues> loginsValues = new ArrayList<ContentValues>(repositories.size());
 			
 			for (ViewRepository repository : repositories) {
 				if(insertRepository.replace(repository.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 				if(repository.isLoginRequired()){
 					loginsValues.add(repository.getLogin().getValues());
 				}
 			}
 			insertRepository.close();
 			
 			InsertHelper insertLogin = new InsertHelper(db, Constants.TABLE_LOGIN);
 			for (ContentValues loginValues : loginsValues) {
 				if(insertLogin.replace(loginValues) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertLogin.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 //			serviceData.updateReposLists();
 		}
 	}
 	
 	/**
 	 * insertRepository, handles single repository insertion
 	 * 
 	 * @param ViewRepository repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertRepository(ViewRepository repository){
 		repository.setLastSynchroTime(Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis());
 		try{
 			db.beginTransaction();
 			if(db.replace(Constants.TABLE_REPOSITORY, null, repository.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			if(repository.isLoginRequired()){
 				if(db.replace(Constants.TABLE_LOGIN, null, repository.getLogin().getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 			serviceData.updateReposLists();
 		}
 	}
 	
 	/**
 	 * updateRepository, handles single repository updating
 	 * 
 	 * @param ViewRepository repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void updateRepository(ViewRepository repository){
 		repository.setLastSynchroTime(Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis());
 		try{
 			db.beginTransaction();
 			if(db.update(Constants.TABLE_REPOSITORY, repository.getValues(), Constants.KEY_REPO_HASHID+"=?", new String[]{Integer.toString(repository.getHashid())}) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 			serviceData.updateReposLists();
 		}
 	}
 	
 	/**
 	 * removeLogin, handles single repository's login removal
 	 * 
 	 * @param int repoHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeLogin(int repoHashid){
 		try{
 			db.beginTransaction();
 			if(db.delete(Constants.TABLE_LOGIN, Constants.KEY_LOGIN_REPO_HASHID+"=?", new String[]{Integer.toString(repoHashid)}) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * updateLogin, handles single repository's login updating
 	 * 
 	 * @param ViewRepository repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void updateLogin(ViewRepository repository){
 		try{
 			db.beginTransaction();
 			if(db.update(Constants.TABLE_LOGIN, repository.getLogin().getValues(), Constants.KEY_LOGIN_REPO_HASHID+"=?", new String[]{Integer.toString(repository.getHashid())}) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 			serviceData.updateReposLists();
 		}
 	}
 	
 	/**
 	 * removeRepositories, handles multiple repositories removal
 	 * 
 	 * @param ViewListIds repoHashids
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeRepositories(ViewListIds repoHashids){	//TODO manually cascade triggers, because they don't automatically do it
 		try{
 			db.beginTransaction();
 			StringBuilder deleteWhere = new StringBuilder();
 			boolean firstWhere = true;
 			
 			deleteWhere.append(Constants.KEY_REPO_HASHID+" IN (");
 			for (int repoHashid : repoHashids) {
 				
 				if(firstWhere){
 					firstWhere = false;
 				}else{
 					deleteWhere.append(",");					
 				}
 				
 				deleteWhere.append(repoHashid);
 			}
 			deleteWhere.append(")");	
 			
 			
 			if(db.delete(Constants.TABLE_REPOSITORY, deleteWhere.toString(), null) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * removeRepository, handles single repository removal
 	 * 
 	 * @param int repoHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeRepository(int repoHashid){	//TODO manually cascade triggers, because they don't automatically do it
 		try{
 			db.beginTransaction();
 			String deleteWhere = Constants.KEY_REPO_HASHID+"=?";
 			
 			if(db.delete(Constants.TABLE_REPOSITORY, deleteWhere, new String[]{Integer.toString(repoHashid)}) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * toggleRepositoriesInUse, handles multiple repositories inUse toggle
 	 * 
 	 * @param ViewListIds repoHashids
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void toggleRepositoriesInUse(ViewListIds repoHashids, boolean setInUse){
 		try{
 			db.beginTransaction();
 			ContentValues setTrue = new ContentValues();
 			setTrue.put(Constants.KEY_REPO_IN_USE, (setInUse?Constants.DB_TRUE:Constants.DB_FALSE) );
 			
 			StringBuilder updateWhere = new StringBuilder();
 			boolean firstWhere = true;
 
 			updateWhere.append(Constants.KEY_REPO_HASHID+" IN (");
 			for (int repoHashid : repoHashids) {
 				
 				if(firstWhere){
 					firstWhere = false;
 				}else{
 					updateWhere.append(",");					
 				}
 				
 				updateWhere.append(repoHashid);
 			}
 			updateWhere.append(")");
 			
 			
 			if(db.update(Constants.TABLE_REPOSITORY, setTrue, updateWhere.toString(), null) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 			
 //			serviceData.resetAvailableLists();
 			
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * toggleRepositoryInUse, handles single repository inUse toggle
 	 * 
 	 * @param int repoHashids
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void toggleRepositoryInUse(int repoHashid, boolean setInUse){
 		try{
 			db.beginTransaction();
 			ContentValues setTrue = new ContentValues();
 			setTrue.put(Constants.KEY_REPO_IN_USE, (setInUse?Constants.DB_TRUE:Constants.DB_FALSE) );
 			
 			String updateWhere = Constants.KEY_REPO_HASHID+" IN ("+repoHashid+")";
 			
 			if(db.update(Constants.TABLE_REPOSITORY, setTrue, updateWhere, null) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 			serviceData.updateReposLists();
 		}
 	}
 		
 	
 	
 	
 	/**
 	 * insertApplications, handles multiple applications insertion
 	 * 
 	 * @param ArrayList<Application> applications
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertApplications(ArrayList<ViewApplication> applications){
 		
 		//################## Temporary fix ###########################
 		categories_hashids = new ArrayList<Integer>();
 		ArrayList<ViewCategory> categories = new ArrayList<ViewCategory>(2);
 		
 		ViewCategory category = new ViewCategory(Constants.CATEGORY_APPLICATIONS);
 		for (String subCategory : Constants.SUB_CATEGORIES_APPLICATIONS) {
 			category.addChild(new ViewCategory(subCategory));
 			categories_hashids.add(subCategory.hashCode());
 		}
 	
 		categories.add(category);
 		
 		ViewCategory games = new ViewCategory(Constants.CATEGORY_GAMES);
 		for (String subCategory : Constants.SUB_CATEGORIES_GAMES) {
 			games.addChild(new ViewCategory(subCategory));
 			categories_hashids.add(subCategory.hashCode());
 		}
 		
 		categories.add(games);
 		
 		ViewCategory others = new ViewCategory(Constants.CATEGORY_OTHERS);
 		categories.add(others);
 		//################################################################
 	
 		ArrayList<ContentValues> appCategoriesRelations = new ArrayList<ContentValues>(applications.size());
 		try{
 			db.beginTransaction();
 			
 			InsertHelper insertApplication = new InsertHelper(db, Constants.TABLE_APPLICATION);
 			ContentValues appCategoryRelation;
 
 			for (ViewApplication application : applications) {
 				if(insertApplication.insert(application.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 				appCategoryRelation = new ContentValues(Constants.NUMBER_OF_COLUMNS_APP_CATEGORY);
 				appCategoryRelation.put(Constants.KEY_APP_CATEGORY_APP_FULL_HASHID, application.getFullHashid());
 				appCategoryRelation.put(Constants.KEY_APP_CATEGORY_CATEGORY_HASHID
 										, (categories_hashids.contains(application.getCategoryHashid())?application.getCategoryHashid():Constants.CATEGORY_HASHID_OTHERS));
 				appCategoriesRelations.add(appCategoryRelation);
 				
 			}
 			insertApplication.close();
 			
 
 			
 			InsertHelper insertAppCategoryRelation = new InsertHelper(db, Constants.TABLE_APP_CATEGORY);
 			for (ContentValues appCategoryRelationValues : appCategoriesRelations) {
 				if(insertAppCategoryRelation.insert(appCategoryRelationValues) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertAppCategoryRelation.close();
 			db.setTransactionSuccessful();
 						
 		}catch (Exception e) {
 			Log.d("Aptoide-ManagerDatabase", "insert applications, exception!");
 			e.printStackTrace();
 			// TODO: handle exception
 		}finally{
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 		}
 	}
 	
 		
 	/**
 	 * insertApplication, handles single application insertion
 	 * 
 	 * @param ViewAppVersionRepository application
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertApplication(ViewApplication application){
 		ContentValues appCategoryRelation = null;
 
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_APPLICATION ,null, application.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			appCategoryRelation = new ContentValues(Constants.NUMBER_OF_COLUMNS_APP_CATEGORY); //TODO check if this implicit object recycling works 
 			appCategoryRelation.put(Constants.KEY_APP_CATEGORY_APP_FULL_HASHID, application.getFullHashid());
 			appCategoryRelation.put(Constants.KEY_APP_CATEGORY_CATEGORY_HASHID
 									, (categories_hashids.contains(application.getCategoryHashid())?application.getCategoryHashid():Constants.CATEGORY_HASHID_OTHERS));
 			
 			
 
 			if(db.insert(Constants.TABLE_APP_CATEGORY ,null, appCategoryRelation) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 
 			db.setTransactionSuccessful();
 			
 		}catch (Exception e) {
 			// TODO: handle exception
 		}finally{
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 		}
 	}
 	
 	/**
 	 * removeApplications, handles multiple applications removal
 	 * 
 	 * @param ViewListIds appsFullHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeApplications(ViewListIds appsFullHashid){
 		try{
 			db.beginTransaction();
 			StringBuilder deleteWhere = new StringBuilder(Constants.KEY_APPLICATION_FULL_HASHID+" IN (");
 			boolean firstWhere = true;
 			
 			for (int appFullHashid : appsFullHashid) {
 				
 				if(firstWhere){
 					firstWhere = false;
 				}else{
 					deleteWhere.append(",");
 				}
 				
 				deleteWhere.append(appFullHashid);
 			}
 			deleteWhere.append(")");
 			
 			if(db.delete(Constants.TABLE_APPLICATION, deleteWhere.toString(), null) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	/**
 	 * insertIconsInfo, handles multiple application's icon info insertion
 	 * 
 	 * @param ArrayList<IconInfo> iconsInfo
 	 */
 	public void insertIconsInfo(ArrayList<ViewIconInfo> iconsInfo){
 		try{
 			db.beginTransaction();
 			InsertHelper insertIconInfo = new InsertHelper(db, Constants.TABLE_ICON_INFO);
 			
 			for (ViewIconInfo iconInfo : iconsInfo) {
 				if(insertIconInfo.insert(iconInfo.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertIconInfo.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertIconInfo, handles single application's icon info insertion
 	 * 
 	 * @param ViewIconInfo iconInfo
 	 */
 	public void insertIconInfo(ViewIconInfo iconInfo){
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_ICON_INFO, null, iconInfo.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	/**
 	 * insertScreensInfo, handles multiple application's screen info insertion
 	 * 
 	 * @param ArrayList<ScreenInfo> screensInfo
 	 */
 	public void insertScreensInfo(ArrayList<ViewScreenInfo> screensInfo){
 		try{
 			db.beginTransaction();
 			InsertHelper insertScreenInfo = new InsertHelper(db, Constants.TABLE_SCREEN_INFO);
 			
 			for (ViewScreenInfo screenInfo : screensInfo) {
 				if(insertScreenInfo.insert(screenInfo.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertScreenInfo.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertScreenInfo, handles single application's screen info insertion
 	 * 
 	 * @param ViewScreenInfo screenInfo
 	 */
 	public void insertScreenInfo(ViewScreenInfo screenInfo){
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_SCREEN_INFO, null, screenInfo.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertDownloadsInfo, handles multiple application's download info insertion
 	 * 
 	 * @param ArrayList<DownloadInfo> downloadsInfo
 	 */
 	public void insertDownloadsInfo(ArrayList<ViewAppDownloadInfo> downloadsInfo){
 		try{
 			db.beginTransaction();
 			InsertHelper insertDownloadInfo = new InsertHelper(db, Constants.TABLE_DOWNLOAD_INFO);
 			
 			for (ViewAppDownloadInfo downloadInfo : downloadsInfo) {
 				if(insertDownloadInfo.insert(downloadInfo.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertDownloadInfo.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertDownloadInfo, handles single application's download info insertion
 	 * 
 	 * @param ViewAppDownloadInfo downloadInfo
 	 */
 	public void insertDownloadInfo(ViewAppDownloadInfo downloadInfo){
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_DOWNLOAD_INFO, null, downloadInfo.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertExtraInfos, handles multiple application's extra info insertion
 	 * 
 	 * @param ArrayList<ViewExtraInfo> extraInfos
 	 */
 	public void insertExtras(ArrayList<ViewExtraInfo> extraInfos){
 		try{
 			db.beginTransaction();
 			InsertHelper insertExtraInfo = new InsertHelper(db, Constants.TABLE_EXTRA_INFO);
 			
 			for (ViewExtraInfo extraInfo : extraInfos) {
 				if(insertExtraInfo.insert(extraInfo.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertExtraInfo.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertExtraInfo, handles single application's extra info insertion
 	 * 
 	 * @param ViewExtraInfo extraInfo
 	 */
 	public void insertExtra(ViewExtraInfo extraInfo){
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_EXTRA_INFO, null, extraInfo.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * insertAppComments, handles multiple application's comments insertion
 	 * 
 	 * @param ArrayList<AppComment> appComment
 	 */
 	public void insertAppComments(ArrayList<ViewAppComment> appComments){
 		try{
 			db.beginTransaction();
 			InsertHelper insertAppComments = new InsertHelper(db, Constants.TABLE_APP_COMMENTS);
 			
 			for (ViewAppComment appComment : appComments) {
 				if(insertAppComments.insert(appComment.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertAppComments.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * insertOrReplaceStatsInfos, handles multiple application's stats info insertion
 	 * 							  if already present replaces with new ones
 	 * 
 	 * @param ArrayList<StatsInfo> statsInfos
 	 */
 	public void insertOrReplaceStatsInfos(ArrayList<ViewStatsInfo> statsInfos){
 		try{
 			db.beginTransaction();
 			InsertHelper insertStatsInfo = new InsertHelper(db, Constants.TABLE_STATS_INFO);
 			
 			for (ViewStatsInfo statsInfo : statsInfos) {
 				if(insertStatsInfo.replace(statsInfo.getValues()) == Constants.DB_ERROR){
 					//TODO throw exception;
 				}
 			}
 			insertStatsInfo.close();
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertOrReplaceStatsInfo, handles single application's stats info insertion
 	 * 							 if already present replaces with new ones
 	 * 
 	 * @param ViewStatsInfo statsInfo
 	 */
 	public void insertOrReplaceStatsInfo(ViewStatsInfo statsInfo){
 		try{
 			db.beginTransaction();
 			if(db.replace(Constants.TABLE_STATS_INFO, null, statsInfo.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: send to errorHandler the exception
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 	
 	/**
 	 * insertApplicationToInstall, handles single application store for later install
 	 * 										 
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertApplicationToInstall(int appHashid){
 		ContentValues contentValues = new ContentValues();
 		contentValues.put(Constants.KEY_APP_TO_INSTALL_HASHID, appHashid);
 		
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_APP_TO_INSTALL ,null, contentValues) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 		}
 	}
 	
 	/**
 	 * removeApplicationToInstall, handles single application store for later install
 	 * 										 
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeApplicationToInstall(int appHashid){
 		try{
 			db.beginTransaction();
 			if(db.delete(Constants.TABLE_APP_TO_INSTALL ,Constants.KEY_APP_TO_INSTALL_HASHID+"=?", new String[]{Integer.toString(appHashid)}) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 		}
 	}
 		
 	
 	
 	
 	/**
 	 * insertInstalledApplications, handles multiple installed applications insertion
 	 * 										 
 	 * 
 	 * @param ArrayList<Application> installedApplications
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertInstalledApplications(ArrayList<ViewApplicationInstalled> installedApplications){
 		try{
 			db.beginTransaction();
//			db.execSQL(Constants.DROP_TABLE_APP_INSTALLED);		//TODO Switched to replace to speed up, may cause unreliable timestamp readings
//			db.execSQL(Constants.CREATE_TABLE_APP_INSTALLED);
 			
 			InsertHelper insertInstalledApplication = new InsertHelper(db, Constants.TABLE_APP_INSTALLED);
 			for (ViewApplicationInstalled application : installedApplications) {
				if(insertInstalledApplication.replace(application.getValues()) == Constants.DB_ERROR){ 
 					//TODO throw exception;
 				}
 			}
 			insertInstalledApplication.close();
 			
 			db.setTransactionSuccessful();
 			serviceData.resetInstalledLists();Log.d("Aptoide-ManagerDatabase", "Inserted installed apps");
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 			e.printStackTrace();
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * insertInstalledApplication, handles single installed application insertion
 	 * 										 
 	 * 
 	 * @param ViewAppVersionRepository installedApplication
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void insertInstalledApplication(ViewApplicationInstalled installedApplication){
 		try{
 			db.beginTransaction();
 			if(db.insert(Constants.TABLE_APP_INSTALLED ,null, installedApplication.getValues()) == Constants.DB_ERROR){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 			serviceData.resetInstalledLists();Log.d("Aptoide-ManagerDatabase", "Inserted installed app");
 			serviceData.resetAvailableLists();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * removeInstalledApplication, handles single application removal
 	 * 
 	 * @param String appHashid
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeInstalledApplication(int appHashid){
 		try{
 			db.beginTransaction();
 			if(db.delete(Constants.TABLE_APP_INSTALLED, Constants.KEY_APP_INSTALLED_HASHID+"="+appHashid, null) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 			serviceData.resetInstalledLists();Log.d("Aptoide-ManagerDatabase", "Removed installed app");
 			serviceData.resetAvailableLists();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	/**
 	 * removeInstalledApplication, handles single application removal
 	 * 
 	 * @param String packageName
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public void removeInstalledApplication(String packageName){
 		try{
 			db.beginTransaction();
 			if(db.delete(Constants.TABLE_APP_INSTALLED, Constants.KEY_APP_INSTALLED_PACKAGE_NAME+"=?",new String[]{packageName}) == Constants.DB_NO_CHANGES_MADE){
 				//TODO throw exception;
 			}
 			
 			db.setTransactionSuccessful();
 			serviceData.resetInstalledLists();Log.d("Aptoide-ManagerDatabase", "Removed installed app");
 			serviceData.resetAvailableLists();
 		}catch (Exception e) {
 			// TODO: *send to errorHandler the exception, possibly rollback first or find out what went wrong and deal with it and then call errorHandler*
 		}finally{
 			db.endTransaction();
 		}
 	}
 	
 	
 		
 	/* ******************************************************** *
 	 * 															*
 	 *                     Reader methods						*
 	 * 															*
 	 * ******************************************************** */
 	
 	
 	
 	/**
 	 * aptoideNonAtomicQuery, serves as a basis for all aptoide db's queries,
 	 * 						does not handle transactions
 	 * 
 	 * @param String sqlQuery  Android requires that Querys have no ";" termination 
 	 * @param String[] queryArgs
 	 * 
 	 * @return Cursor querysResults
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	private Cursor aptoideNonAtomicQuery(String sqlQuery, String[] queryArgs){
 		return db.rawQuery(sqlQuery, queryArgs);
 	}
 	
 	/**
 	 * aptoideAtomicQuery, serves as a basis for all aptoide db's queries,
 	 * 						handles transactions
 	 * 
 	 * @param String sqlQuery  Android requires that Querys have no ";" termination
 	 * @param String[] queryArgs
 	 * 
 	 * @return Cursor querysResults
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	private Cursor aptoideAtomicQuery(String sqlQuery, String[] queryArgs){
 		Cursor resultsCursor = null;
 		
 		try{
 			db.beginTransaction();
 			resultsCursor = db.rawQuery(sqlQuery, queryArgs);
 			
 			db.setTransactionSuccessful();
 		}catch (Exception e) {
 			if(resultsCursor != null && !resultsCursor.isClosed()){
 				resultsCursor.close();
 			}
 			// TODO: handle exception
 		}finally{
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 		}
 		
 		return resultsCursor;
 	}
 	
 	/**
 	 * aptoideNonAtomicQuery, serves as a basis for all aptoide db's queries with no arguments,
 	 * 							does not handle transactions
 	 * 
 	 * @param String sqlQuery  Android requires that Querys have no ";" termination
 	 * 
 	 * @return Cursor querysResults
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	private Cursor aptoideNonAtomicQuery(String sqlQuery){
 		return aptoideNonAtomicQuery(sqlQuery, null);
 	}
 	
 	/**
 	 * aptoideAtomicQuery, serves as a basis for all aptoide db's queries with no arguments,
 	 * 						handles transactions
 	 * 
 	 * @param String sqlQuery  Android requires that Querys have no ";" termination
 	 * 
 	 * @return Cursor querysResults
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	private Cursor aptoideAtomicQuery(String sqlQuery){
 		return aptoideAtomicQuery(sqlQuery, null);
 	}
 	
 	
 	/**
 	 * getRepository, retrieves a Repository
 	 * 
 	 * @param int repoHashid
 	 * 
 	 * @return ViewRepository, repository
 	 * @throws AptoideExceptionDatabase
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */	
 	public ViewRepository getRepository(int repoHashid){
 
 //		final int REPO_HASHID = Constants.COLUMN_FIRST;
 		final int URI = Constants.COLUMN_SECOND;
 //		final int IN_USE = Constants.COLUMN_THIRD;
 		final int SIZE = Constants.COLUMN_FOURTH;
 		final int BASE_PATH = Constants.COLUMN_FIFTH;
 		final int ICONS_PATH = Constants.COLUMN_SIXTH;
 		final int SCREENS_PATH = Constants.COLUMN_SEVENTH;
 		final int DELTA = Constants.COLUMN_EIGTH;
 		final int USERNAME = Constants.COLUMN_NINTH;
 		final int PASSWORD = Constants.COLUMN_TENTH;
 
 		String selectRepo = "SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_URI+", "+Constants.KEY_REPO_IN_USE+", "+Constants.KEY_REPO_SIZE
 									+", "+Constants.KEY_REPO_BASE_PATH+", "+Constants.KEY_REPO_ICONS_PATH+", "+Constants.KEY_REPO_SCREENS_PATH
 									+", "+Constants.KEY_REPO_DELTA+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 							+" WHERE "+Constants.KEY_REPO_HASHID+"="+repoHashid;
 		
 
 		Cursor repoCursor = aptoideAtomicQuery(selectRepo);
 		if(repoCursor.getCount() < 1){
 			throw new AptoideExceptionDatabase("No repository found with repoHashid = "+repoHashid);
 		}
 		
 		repoCursor.moveToFirst();
 
 		ViewRepository repo = new ViewRepository(repoCursor.getString(URI), repoCursor.getInt(SIZE), repoCursor.getString(BASE_PATH)
 				, repoCursor.getString(ICONS_PATH), repoCursor.getString(SCREENS_PATH), repoCursor.getString(DELTA));
 
 		if( !(repoCursor.isNull(USERNAME)) ){
 			ViewLogin login = new ViewLogin(repoCursor.getString(USERNAME),repoCursor.getString(PASSWORD));
 			repo.setLogin(login);
 		}
 		repoCursor.close();
 		//		Log.d("Aptoide-getAppRepo", "appRepo: "+repo+" "+selectRepo);
 
 		
 		return repo;
 	}
 	
 	
 	
 	/**
 	 * getRepoInUse, retrieves the managed repo in use
 	 * 
 	 * @return ViewRepository, repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewRepository getRepoInUse(){
 
 //		final int REPO_HASHID = Constants.COLUMN_FIRST;
 		final int URI = Constants.COLUMN_SECOND;
 //		final int IN_USE = Constants.COLUMN_THIRD;
 		final int SIZE = Constants.COLUMN_FOURTH;
 		final int BASE_PATH = Constants.COLUMN_FIFTH;
 		final int ICONS_PATH = Constants.COLUMN_SIXTH;
 		final int SCREENS_PATH = Constants.COLUMN_SEVENTH;
 		final int DELTA = Constants.COLUMN_EIGTH;
 		final int USERNAME = Constants.COLUMN_NINTH;
 		final int PASSWORD = Constants.COLUMN_TENTH;
 
 		String selectRepo = "SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_URI+", "+Constants.KEY_REPO_IN_USE+", "+Constants.KEY_REPO_SIZE
 									+", "+Constants.KEY_REPO_BASE_PATH+", "+Constants.KEY_REPO_ICONS_PATH+", "+Constants.KEY_REPO_SCREENS_PATH
 									+", "+Constants.KEY_REPO_DELTA+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 							+" WHERE "+Constants.KEY_REPO_IN_USE+"='"+Constants.DB_TRUE+"'";
 		
 
 		Cursor repoCursor = aptoideAtomicQuery(selectRepo);
 		if(repoCursor.getCount() < 1){
 			throw new AptoideExceptionDatabase("No repository in use");
 		}
 		
 		repoCursor.moveToFirst();
 
 		ViewRepository repo = new ViewRepository(repoCursor.getString(URI), repoCursor.getInt(SIZE), repoCursor.getString(BASE_PATH)
 				, repoCursor.getString(ICONS_PATH), repoCursor.getString(SCREENS_PATH), repoCursor.getString(DELTA));
 
 		if( !(repoCursor.isNull(USERNAME)) ){
 			ViewLogin login = new ViewLogin(repoCursor.getString(USERNAME),repoCursor.getString(PASSWORD));
 			repo.setLogin(login);
 		}
 		repoCursor.close();
 		
 		return repo;
 		
 	}
 	
 	
 	
 	/**
 	 * anyReposInUse, checks if there are any managed repos in use
 	 * 
 	 * @return boolean
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean anyReposInUse(){
 		
 		String selectReposInUse = "SELECT count(*)"
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" WHERE "+Constants.KEY_REPO_IN_USE+"='"+Constants.DB_TRUE+"'";
 		
 		boolean anyReposInUse = false;
 		
 		Cursor reposInUseCursor = aptoideAtomicQuery(selectReposInUse);
 		
 		if(reposInUseCursor == null || reposInUseCursor.isClosed()){
 			return anyReposInUse;
 		}
 		
 		reposInUseCursor.moveToFirst();
 		anyReposInUse = (reposInUseCursor.getInt(Constants.COLUMN_FIRST) > 0?true:false);
 		reposInUseCursor.close();
 		
 		return anyReposInUse;
 		
 	}
 	
 	
 	
 	/**
 	 * isRepoManaged, checks if repo referenced by this hashid is already managed
 	 * 
 	 * @param int repoHashid
 	 * 
 	 * @return boolean
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isRepoManaged(int repoHashid){
 		
 		String selectRepo = "SELECT count(*)"
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" WHERE "+Constants.KEY_REPO_HASHID+"='"+repoHashid+"'";
 		
 		boolean repoIsManaged = false;
 		
 		Cursor repoCursor = aptoideAtomicQuery(selectRepo);
 
 		if(repoCursor == null || repoCursor.isClosed()){
 			return repoIsManaged;
 		}
 		
 		repoCursor.moveToFirst();
 		repoIsManaged = (repoCursor.getInt(Constants.COLUMN_FIRST) == Constants.DB_TRUE?true:false);
 		repoCursor.close();
 		
 		return repoIsManaged;
 		
 	}
 	
 	
 	
 	/**
 	 * isAppDownloadInfoPresent, checks if the DownloadInfo of the Application referenced by this appFullHashid is already present
 	 * 
 	 * @param int appFullHashid
 	 * 
 	 * @return boolean
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isAppDownloadInfoPresent(int appFullHashid){
 		
 		String selectAppDownloadInfo = "SELECT count(*)"
 									+" FROM "+Constants.TABLE_DOWNLOAD_INFO
 									+" WHERE "+Constants.KEY_DOWNLOAD_APP_FULL_HASHID+"='"+appFullHashid+"'";
 		
 		boolean appDownloadInfoIsPresent = false;
 	
 		Cursor cursorAppDownloadInfo = aptoideAtomicQuery(selectAppDownloadInfo);
 
 		if(cursorAppDownloadInfo == null || cursorAppDownloadInfo.isClosed()){
 			return appDownloadInfoIsPresent;
 		}
 		
 		cursorAppDownloadInfo.moveToFirst();
 		appDownloadInfoIsPresent = (cursorAppDownloadInfo.getInt(Constants.COLUMN_FIRST) == Constants.DB_TRUE?true:false);
 		cursorAppDownloadInfo.close();
 	
 		return appDownloadInfoIsPresent;
 		
 	}
 	
 	
 	
 	/**
 	 * isAppExtraInfoPresent, checks if the ExtraInfo of the Application referenced by this appFullHashid is already present
 	 * 
 	 * @param int appFullHashid
 	 * 
 	 * @return boolean
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isAppExtraInfoPresent(int appFullHashid){
 		
 		String selectAppExtraInfo = "SELECT count(*)"
 									+" FROM "+Constants.TABLE_EXTRA_INFO
 									+" WHERE "+Constants.KEY_EXTRA_APP_FULL_HASHID+"='"+appFullHashid+"'";
 		
 		boolean appExtraInfoIsPresent = false;
 		
 		Cursor cursorAppExtraInfo = aptoideAtomicQuery(selectAppExtraInfo);
 
 		if(cursorAppExtraInfo == null || cursorAppExtraInfo.isClosed()){
 			return appExtraInfoIsPresent;
 		}
 				
 		cursorAppExtraInfo.moveToFirst();
 		appExtraInfoIsPresent = (cursorAppExtraInfo.getInt(Constants.COLUMN_FIRST) == Constants.DB_TRUE?true:false);
 		cursorAppExtraInfo.close();
 		
 		return appExtraInfoIsPresent;
 		
 	}
 	
 	
 	
 	
 	/**
 	 * excludeManagedRepos, excludes from the list of repos, those that are already managed
 	 * 
 	 * @param ViewDisplayListRepos
 	 * 
 	 * @return boolean
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListRepos excludeManagedRepos(ViewDisplayListRepos repos){
 				
 		String selectRepos = "SELECT "+Constants.KEY_REPO_HASHID
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" WHERE "+Constants.KEY_REPO_HASHID+" IN (";
 		
 		boolean firstWhere = true;
 		
 		for (Integer repoHashid : repos.getHashMap().keySet()) {
 			if(!firstWhere){
 				selectRepos += ", ";
 			}
 			selectRepos += "'"+repoHashid+"'";
 		}					
 		selectRepos += ")";
 //		Log.d("Aptoide-ManagerDatabase", "exclude managed repos: "+selectRepos);
 		
 		Cursor reposCursor = aptoideAtomicQuery(selectRepos);
 
 		if(reposCursor == null || reposCursor.isClosed()){
 			return repos;
 		}
 		
 		if(reposCursor.getCount() != Constants.EMPTY_INT){
 			reposCursor.moveToFirst();
 			do{
 				repos.removeRepo(reposCursor.getInt(Constants.COLUMN_FIRST));
 			}while(reposCursor.moveToNext());
 		}
 		reposCursor.close();
 
 		return repos;
 		
 	}
 	
 	
 	
 	/**
 	 * getReposDisplayInfo, retrieves a list of all known repos
 	 * 					   with display relevant information (uri, inUse, Login if required)
 	 * 
 	 * @return ViewDisplayListRepos list of stores with it's logins
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListRepos getReposDisplayInfo(){
 
 		final int REPO_HASHID = Constants.COLUMN_FIRST;
 		final int URI = Constants.COLUMN_SECOND;
 		final int IN_USE = Constants.COLUMN_THIRD;
 		final int SIZE = Constants.COLUMN_FOURTH;
 		final int USERNAME = Constants.COLUMN_FIFTH;
 		final int PASSWORD = Constants.COLUMN_SIXTH;
 
 		ViewDisplayListRepos listRepos = null;
 		
 		String selectRepos = "SELECT "+Constants.KEY_REPO_HASHID+","+Constants.KEY_REPO_URI
 									  +","+Constants.KEY_REPO_IN_USE+","+Constants.KEY_REPO_SIZE
 									  +","+Constants.KEY_LOGIN_USERNAME+","+Constants.KEY_LOGIN_PASSWORD
 							+" FROM "+Constants.TABLE_REPOSITORY
 							+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN;
 		ViewDisplayRepo repo;
 		ViewDisplayLogin login;
 //		Log.d("Aptoide-ManagerDatabase", "repos: "+selectRepos);
 
 		try{
 			db.beginTransaction();
 			
 			Cursor reposCursor = aptoideNonAtomicQuery(selectRepos);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			listRepos = new ViewDisplayListRepos(reposCursor.getCount());
 
 			reposCursor.moveToFirst();
 			
 			if(reposCursor.getCount() == 0){
 				reposCursor.close();
 				return listRepos;
 			}
 			
 			do{
 				repo = new ViewDisplayRepo(reposCursor.getInt(REPO_HASHID), reposCursor.getString(URI), (reposCursor.getInt(IN_USE)==Constants.DB_TRUE?true:false), reposCursor.getInt(SIZE) );
 				if(!reposCursor.isNull(USERNAME)){
 					login = new ViewDisplayLogin(reposCursor.getString(USERNAME),reposCursor.getString(PASSWORD));
 					repo.setLogin(login);
 				}
 				listRepos.addRepo(repo);
 			}while(reposCursor.moveToNext());
 			reposCursor.close();
 
 		}catch (Exception e) {
 			Log.d("Aptoide-ManagerDatabase", "get repos display info, exception! "+listRepos);
 			e.printStackTrace();
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 		}
 		return listRepos;		
 	}
 	
 	
 	
 	/**
 	 * getRepoIfUpdateNeeded, decides based on last synchronization timestamp if the repository referenced by the repoHashid needs updating
 	 * 
 	 * @param int repoHashid, references repository
 	 * 
 	 * @return ViewRepository, repository if it needs updating, null otherwise //TODO nullObject
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewRepository getRepoIfUpdateNeeded(final int repoHashid){
 		final int REPO_URI = Constants.COLUMN_FIRST;
 		final int REPO_IN_USE = Constants.COLUMN_SECOND;
 		final int REPO_SIZE = Constants.COLUMN_THIRD;
 		final int REPO_DELTA = Constants.COLUMN_FOURTH;
 		final int REPO_LAST_SYNCHRO = Constants.COLUMN_FIFTH;
 		final int USERNAME = Constants.COLUMN_SIXTH;
 		final int PASSWORD = Constants.COLUMN_SEVENTH;
 		
 		final long currentTimeStamp = Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis();
 		ViewRepository repositoryNeedingUpdate = null;
 		
 		String selectRepoNeedingUpdate = "SELECT "+Constants.KEY_REPO_URI+", "+Constants.KEY_REPO_IN_USE
 									+", "+Constants.KEY_REPO_SIZE+", "+Constants.KEY_REPO_DELTA+", "+Constants.KEY_REPO_LAST_SYNCHRO
 									+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 									+" FROM "+Constants.TABLE_REPOSITORY
 									+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 									+" WHERE "+Constants.KEY_REPO_HASHID+"="+repoHashid
 									+" AND "+Constants.KEY_REPO_LAST_SYNCHRO+"<"+(currentTimeStamp-(Constants.REPOS_UPDATE_INTERVAL*Constants.HOURS_TO_MILISECONDS))
 									+";";
 		
 //		Log.d("Aptoide-ManagerDatabase", "repo if update needed: "+selectRepoNeedingUpdate);
 		
 		try {
 			db.beginTransaction();
 			Cursor cursorRepoNeedingUpdate = aptoideNonAtomicQuery(selectRepoNeedingUpdate);
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			if(cursorRepoNeedingUpdate.getCount() != Constants.EMPTY_INT){
 				cursorRepoNeedingUpdate.moveToFirst();			
 				repositoryNeedingUpdate = new ViewRepository(cursorRepoNeedingUpdate.getString(REPO_URI));
 				repositoryNeedingUpdate.setInUse(cursorRepoNeedingUpdate.getInt(REPO_IN_USE)==Constants.DB_TRUE?true:false);
 				repositoryNeedingUpdate.setSize(cursorRepoNeedingUpdate.getInt(REPO_SIZE));
 				repositoryNeedingUpdate.setDelta(cursorRepoNeedingUpdate.getString(REPO_DELTA));
 				repositoryNeedingUpdate.setLastSynchroTime(cursorRepoNeedingUpdate.getLong(REPO_LAST_SYNCHRO));
 				
 				if(!cursorRepoNeedingUpdate.isNull(USERNAME)){
 					repositoryNeedingUpdate.setLogin(new ViewLogin(cursorRepoNeedingUpdate.getString(USERNAME),cursorRepoNeedingUpdate.getString(PASSWORD)));
 				}		
 			}
 			cursorRepoNeedingUpdate.close();
 		} catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return repositoryNeedingUpdate;
 	}
 	
 	
 	
 	/**
 	 * getReposNeedingUpdate, decides based on last synchronization timestamp if any of the repositories in use needs updating
 	 * 
 	 * @return ArrayList<ViewRepository>, list of repositories which need updating
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ArrayList<ViewRepository> getReposNeedingUpdate(boolean force){
 		final int REPO_URI = Constants.COLUMN_FIRST;
 		final int REPO_IN_USE = Constants.COLUMN_SECOND;
 		final int REPO_SIZE = Constants.COLUMN_THIRD;
 		final int REPO_DELTA = Constants.COLUMN_FOURTH;
 		final int REPO_LAST_SYNCHRO = Constants.COLUMN_FIFTH;
 		final int USERNAME = Constants.COLUMN_SIXTH;
 		final int PASSWORD = Constants.COLUMN_SEVENTH;
 		
 		final long currentTimeStamp = Calendar.getInstance(TimeZone.getTimeZone(Constants.UTC_TIMEZONE)).getTimeInMillis();
 		ArrayList<ViewRepository> reposNeedingUpdate = new ArrayList<ViewRepository>();
 		ViewRepository repositoryNeedingUpdate;
 		
 		String selectReposNeedingUpdate = "SELECT "+Constants.KEY_REPO_URI+", "+Constants.KEY_REPO_IN_USE
 									+", "+Constants.KEY_REPO_SIZE+", "+Constants.KEY_REPO_DELTA+", "+Constants.KEY_REPO_LAST_SYNCHRO
 									+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 									+" FROM "+Constants.TABLE_REPOSITORY
 									+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 									+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE;
 		if(!force){
 			selectReposNeedingUpdate	+= " AND "+Constants.KEY_REPO_LAST_SYNCHRO+"<"+(currentTimeStamp-(Constants.REPOS_UPDATE_INTERVAL*Constants.HOURS_TO_MILISECONDS));
 		}
 		
 		Cursor cursorReposNeedingUpdate = aptoideAtomicQuery(selectReposNeedingUpdate);
 //		Log.d("Aptoide-ManagerDatabase", "repos needing update: "+selectReposNeedingUpdate);
 
 		if(cursorReposNeedingUpdate == null || cursorReposNeedingUpdate.isClosed()){
 			cursorReposNeedingUpdate.close();
 			return reposNeedingUpdate;
 		}
 		
 		if(!(cursorReposNeedingUpdate.getCount() == Constants.EMPTY_INT)){
 			cursorReposNeedingUpdate.moveToFirst();
 			do{
 				repositoryNeedingUpdate = new ViewRepository(cursorReposNeedingUpdate.getString(REPO_URI));
 				repositoryNeedingUpdate.setInUse(cursorReposNeedingUpdate.getInt(REPO_IN_USE)==Constants.DB_TRUE?true:false);
 				repositoryNeedingUpdate.setSize(cursorReposNeedingUpdate.getInt(REPO_SIZE));
 				repositoryNeedingUpdate.setDelta(cursorReposNeedingUpdate.getString(REPO_DELTA));
 				repositoryNeedingUpdate.setLastSynchroTime(cursorReposNeedingUpdate.getLong(REPO_LAST_SYNCHRO));	
 
 				if(!cursorReposNeedingUpdate.isNull(USERNAME)){
 					repositoryNeedingUpdate.setLogin(new ViewLogin(cursorReposNeedingUpdate.getString(USERNAME),cursorReposNeedingUpdate.getString(PASSWORD)));
 				}
 				
 				reposNeedingUpdate.add(repositoryNeedingUpdate);
 			}while(cursorReposNeedingUpdate.moveToNext());
 			cursorReposNeedingUpdate.close();
 		}
 		return reposNeedingUpdate;
 	}
 	
 	
 	
 	
 	/**
 	 * getCategoriesDisplayInfo, retrieves a hierarquical list of categories
 	 *  
 	 * @return ViewDisplayCategory top category with its childs
 	 * @param EnumAgeRating ratingFilter
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayCategory getCategoriesDisplayInfo(boolean filterByHw, EnumAgeRating ratingFilter){	//TODO count only once per packageName
 		
 		ViewHwFilters filters = null;
 		
 		if(filterByHw){
 			filters = serviceData.getManagerSystemSync().getHwFilters();
 			Log.d("Aptoide-ManagerDatabase", "getCategoriesDisplayInfo HW filters ON: "+filters+" ratingFilter: "+ratingFilter);
 		}
 			
 		final int CATEGORY_HASHID = Constants.COLUMN_FIRST;
 		final int CATEGORY_NAME = Constants.COLUMN_SECOND;
 		final int CATEGORY_APPS = Constants.COLUMN_THIRD;
 		
 		final int OTHERS_APPS = Constants.COLUMN_SECOND;
 		
 		String selectApplicationsSubCategories = "SELECT C."+Constants.KEY_CATEGORY_HASHID+", C."+Constants.KEY_CATEGORY_NAME+", COUNT("+Constants.KEY_APP_CATEGORY_APP_FULL_HASHID+")"
 													+" FROM (SELECT * FROM "+Constants.TABLE_CATEGORY+" G"
 															+" INNER JOIN "+Constants.TABLE_SUB_CATEGORY+" S ON G."+Constants.KEY_CATEGORY_HASHID+"=S."+Constants.KEY_SUB_CATEGORY_CHILD
 													+" WHERE "+Constants.KEY_SUB_CATEGORY_PARENT+"="+Constants.CATEGORY_HASHID_APPLICATIONS+") C"
 												+" NATURAL INNER JOIN "+Constants.TABLE_APP_CATEGORY
 												+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 												+" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY
 												+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 		/* Exclude Installed apps */			+" AND NOT EXISTS (SELECT "+Constants.KEY_APP_INSTALLED_HASHID
 																+" FROM "+Constants.TABLE_APP_INSTALLED
 																+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID
 																	+"="+Constants.TABLE_APPLICATION+"."+Constants.KEY_APPLICATION_HASHID+")";
 		/* Filter by hardware? */
 		if(filterByHw){
 			selectApplicationsSubCategories +=	" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 												+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 												+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 		}
 		/* Filter by rating */
 		selectApplicationsSubCategories +=		" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 				
 												+" GROUP BY "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID
 												+" ORDER BY "+Constants.KEY_CATEGORY_NAME;
 		
 		String selectGamesSubCategories = "SELECT C."+Constants.KEY_CATEGORY_HASHID+", C."+Constants.KEY_CATEGORY_NAME+", COUNT("+Constants.KEY_APP_CATEGORY_APP_FULL_HASHID+")"
 											+" FROM (SELECT * FROM "+Constants.TABLE_CATEGORY+" G"
 													+" INNER JOIN "+Constants.TABLE_SUB_CATEGORY+" S ON G."+Constants.KEY_CATEGORY_HASHID+"=S."+Constants.KEY_SUB_CATEGORY_CHILD
 											+" WHERE "+Constants.KEY_SUB_CATEGORY_PARENT+"="+Constants.CATEGORY_HASHID_GAMES+") C"
 										+" NATURAL INNER JOIN "+Constants.TABLE_APP_CATEGORY
 										+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 										+" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY
 										+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 		/* Exclude Installed apps */	+" AND NOT EXISTS (SELECT "+Constants.KEY_APP_INSTALLED_HASHID
 														+" FROM "+Constants.TABLE_APP_INSTALLED
 														+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID
 															+"="+Constants.TABLE_APPLICATION+"."+Constants.KEY_APPLICATION_HASHID+")";
 		/* Filter by hardware? */
 		if(filterByHw){
 			selectGamesSubCategories +=	" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 										+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 										+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 		}
 		/* Filter by rating */
 		selectGamesSubCategories +=		" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 				
 										+" GROUP BY "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID
 										+" ORDER BY "+Constants.KEY_CATEGORY_NAME;
 
 		String selectOthersApplications =  "SELECT "+Constants.KEY_CATEGORY_HASHID+", COUNT("+Constants.KEY_APP_CATEGORY_APP_FULL_HASHID+")"
 											+" FROM (SELECT * FROM "+Constants.TABLE_APP_CATEGORY
 														+" WHERE "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID+"="+Constants.CATEGORY_HASHID_OTHERS+")"
 										+" NATURAL INNER JOIN (SELECT "+Constants.KEY_APPLICATION_FULL_HASHID+", "+Constants.KEY_APPLICATION_REPO_HASHID
 																	+" FROM "+Constants.TABLE_APPLICATION
 		/* Exclude Installed apps */								+" WHERE NOT EXISTS (SELECT "+Constants.KEY_APP_INSTALLED_HASHID
 																					+" FROM "+Constants.TABLE_APP_INSTALLED
 																					+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID
 																						+"="+Constants.TABLE_APPLICATION+"."+Constants.KEY_APPLICATION_HASHID+")";
 		/* Filter by hardware? */
 		if(filterByHw){
 			selectOthersApplications +=								" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 																	+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 																	+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 		}
 		/* Filter by rating */
 		selectOthersApplications +=									" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 				
 															+")"
 										+" NATURAL INNER JOIN (SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_IN_USE
 																	+" FROM "+Constants.TABLE_REPOSITORY
 																	+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE+")"
 										+" GROUP BY "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID;
 				
 		
 		ViewDisplayCategory topCategory = new ViewDisplayCategory("TOP", Constants.TOP_CATEGORY, 0);
 		
 //		Log.d("Aptoide-ManagerDatabase", "applicationCategories: "+selectApplicationsSubCategories);
 //		Log.d("Aptoide-ManagerDatabase", "gamesCategories: "+selectGamesSubCategories);
 //		Log.d("Aptoide-ManagerDatabase", "othersCategories: "+selectOthersApplications);
 		try{
 			db.beginTransaction();
 			
 			Cursor cursorApplicationsSubCategories = aptoideNonAtomicQuery(selectApplicationsSubCategories);
 			Cursor cursorGamesSubCategories = aptoideNonAtomicQuery(selectGamesSubCategories);
 			Cursor cursorOthersApplications = aptoideNonAtomicQuery(selectOthersApplications);
 			
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			
 			ViewDisplayCategory category;
 
 			if(cursorApplicationsSubCategories.getCount() != Constants.EMPTY_INT){
 				ViewDisplayCategory applications = new ViewDisplayCategory(Constants.CATEGORY_APPLICATIONS, Constants.CATEGORY_HASHID_APPLICATIONS, 0);
 				cursorApplicationsSubCategories.moveToFirst();
 				do{
 					if(cursorApplicationsSubCategories.getInt(CATEGORY_APPS) != Constants.EMPTY_INT){
 						category = new ViewDisplayCategory(cursorApplicationsSubCategories.getString(CATEGORY_NAME), cursorApplicationsSubCategories.getInt(CATEGORY_HASHID)
 														, cursorApplicationsSubCategories.getInt(CATEGORY_APPS));
 						applications.addSubCategory(category);
 					}
 				}while(cursorApplicationsSubCategories.moveToNext());
 				cursorApplicationsSubCategories.close();
 				if(applications.hasChildren()){
 					topCategory.addSubCategory(applications);
 				}
 			}
 
 
 			if(cursorGamesSubCategories.getCount() != Constants.EMPTY_INT){
 				ViewDisplayCategory games = new ViewDisplayCategory(Constants.CATEGORY_GAMES, Constants.CATEGORY_HASHID_GAMES, 0);
 				cursorGamesSubCategories.moveToFirst();
 				do{
 					if(cursorGamesSubCategories.getInt(CATEGORY_APPS) != Constants.EMPTY_INT){
 						category = new ViewDisplayCategory(cursorGamesSubCategories.getString(CATEGORY_NAME), cursorGamesSubCategories.getInt(CATEGORY_HASHID)
 														, cursorGamesSubCategories.getInt(CATEGORY_APPS));
 						games.addSubCategory(category);
 					}
 				}while(cursorGamesSubCategories.moveToNext());
 				cursorGamesSubCategories.close();
 				if(games.hasChildren()){
 					topCategory.addSubCategory(games);
 				}
 			}
 
 			
 			if(cursorOthersApplications.getCount() != Constants.EMPTY_INT){
 				cursorOthersApplications.moveToFirst();
 				topCategory.addSubCategory(new ViewDisplayCategory(Constants.CATEGORY_OTHERS, Constants.CATEGORY_HASHID_OTHERS
 																, cursorOthersApplications.getInt(OTHERS_APPS)));
 			}
 			cursorOthersApplications.close();
 			
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return topCategory;
 	}
 	
 	/**
 	 * isApplicationScheduledToInstall, returns true if an app is scheduled to install
 	 *  
 	 * @return boolean isAppToInstall
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isApplicationScheduledToInstall(int appHashid){
 		
 		String selectIsAppToInstall = "SELECT * FROM "+Constants.TABLE_APP_TO_INSTALL
 										+" WHERE "+Constants.KEY_APP_TO_INSTALL_HASHID+"="+appHashid;
 
 		Cursor cursorIsAppToInstall = aptoideAtomicQuery(selectIsAppToInstall);
 //		Log.d("Aptoide-ManagerDatabase", "is scheduled: "+selectIsAppToInstall);
 
 		if(cursorIsAppToInstall == null || cursorIsAppToInstall.isClosed()){
 			return false;
 		}
 		
 		if(cursorIsAppToInstall.getCount() == Constants.EMPTY_INT){
 			cursorIsAppToInstall.close();
 			return false;
 		}else{
 			cursorIsAppToInstall.close();
 			return true;
 		}
 	}
 	
 	/**
 	 * getApplicationsScheduledToInstall, gets list of apphashids for every application scheduled to install
 	 *  
 	 * @return ViewListIds
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewListIds getApplicationsScheduledToInstall(){
 		
 		final int HASHID = Constants.COLUMN_FIRST;
 		
 		ViewListIds appsList = null;
 				
 		String selectAppsToInstall = "SELECT * FROM "+Constants.TABLE_APP_TO_INSTALL;
 
 		Cursor cursorAppsToInstall = aptoideAtomicQuery(selectAppsToInstall);
 
 		if(cursorAppsToInstall == null || cursorAppsToInstall.isClosed()){
 			return appsList;
 		}
 		
 		if(cursorAppsToInstall.getCount() == Constants.EMPTY_INT){
 			cursorAppsToInstall.close();
 		}else{
 			cursorAppsToInstall.moveToFirst();
 			appsList = new ViewListIds();
 			do{
 				appsList.add(cursorAppsToInstall.getInt(HASHID));
 			}while(cursorAppsToInstall.moveToNext());
 			
 			cursorAppsToInstall.close();
 		}
 		return appsList;
 	}
 	
 	/**
 	 * getScheduledAppsInfo, gets list of applications scheduled to install
 	 *  
 	 * @return ViewDisplayListApps scheduled apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListApps getScheduledAppsInfo(){
 		
 		final int HASHID = Constants.COLUMN_FIRST;
 		final int NAME = Constants.COLUMN_SECOND;
 		final int VERSION = Constants.COLUMN_THIRD;
 		
 		
 		ViewDisplayListApps appsList = null;
 				
 		String selectAppsToInstall = "SELECT "+Constants.KEY_APPLICATION_HASHID+" ,"+Constants.KEY_APPLICATION_NAME+" ,"+Constants.KEY_APPLICATION_VERSION_NAME
 									+" FROM "+Constants.TABLE_APP_TO_INSTALL
 									+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 									+" GROUP BY "+Constants.KEY_APPLICATION_HASHID
 									+" ORDER BY "+Constants.KEY_APPLICATION_NAME;
 
 		Cursor cursorAppsToInstall = aptoideAtomicQuery(selectAppsToInstall);
 
 		if(cursorAppsToInstall == null || cursorAppsToInstall.isClosed()){
 			return appsList;
 		}
 		
 		if(cursorAppsToInstall.getCount() == Constants.EMPTY_INT){
 			cursorAppsToInstall.close();
 		}else{
 			cursorAppsToInstall.moveToFirst();
 			appsList = new ViewDisplayListApps();
 			do{
 				appsList.add(new ViewDisplayApplication(cursorAppsToInstall.getInt(HASHID), cursorAppsToInstall.getString(NAME), cursorAppsToInstall.getString(VERSION)));
 			}while(cursorAppsToInstall.moveToNext());
 			
 			cursorAppsToInstall.close();
 		}
 		return appsList;
 	}
 
 	
 	/**
 	 * isApplicationAvailable, returns true if an app is already in available
 	 * 
 	 * @param String packageName
 	 * 
 	 * @return boolean isApplicationAvailable
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isApplicationAvailable(String packageName){
 		
 		String selectIsAppAvailable = "SELECT MAX("+Constants.KEY_APPLICATION_VERSION_CODE+") FROM "+Constants.TABLE_APPLICATION
 										+" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY
 										+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 										+" AND "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'";
 
 		Cursor cursorIsAppAvailable = aptoideAtomicQuery(selectIsAppAvailable);
 //		Log.d("Aptoide-ManagerDatabase", "is installed: "+selectIsAppInstalled);
 
 		if(cursorIsAppAvailable == null || cursorIsAppAvailable.isClosed()){
 			return false;
 		}
 		
 		if(cursorIsAppAvailable.getCount() == Constants.EMPTY_INT){
 			cursorIsAppAvailable.close();
 			return false;
 		}else{
 			cursorIsAppAvailable.moveToFirst();
 			if(cursorIsAppAvailable.isNull(Constants.COLUMN_FIRST)){
 				cursorIsAppAvailable.close();
 				return false;
 			}else{
 				cursorIsAppAvailable.close();
 				return true;
 			}
 		}
 	}
 
 	
 	/**
 	 * isApplicationInstalled, returns true if an app is already installed
 	 * 
 	 * @param String packageName
 	 * 
 	 * @return boolean isApplicationInstalled
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isApplicationInstalled(String packageName){
 		
 		String selectIsAppInstalled = "SELECT * FROM "+Constants.TABLE_APP_INSTALLED
 										+" WHERE "+Constants.KEY_APP_INSTALLED_PACKAGE_NAME+"='"+packageName+"'";
 
 		Cursor cursorIsAppInstalled = aptoideAtomicQuery(selectIsAppInstalled);
 //		Log.d("Aptoide-ManagerDatabase", "is installed: "+selectIsAppInstalled);
 
 		if(cursorIsAppInstalled == null || cursorIsAppInstalled.isClosed()){
 			return false;
 		}
 		
 		if(cursorIsAppInstalled.getCount() == Constants.EMPTY_INT){
 			cursorIsAppInstalled.close();
 			return false;
 		}else{
 			cursorIsAppInstalled.close();
 			return true;
 		}
 	}
 
 	
 	/**
 	 * isApplicationInstalled, returns true if an app is already installed
 	 *  
 	 * @param int appHashid
 	 *  
 	 * @return boolean isApplicationInstalled
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean isApplicationInstalled(int appHashid){
 		
 		String selectIsAppInstalled = "SELECT * FROM "+Constants.TABLE_APP_INSTALLED
 										+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID+"='"+appHashid+"'";
 
 		Cursor cursorIsAppInstalled = aptoideAtomicQuery(selectIsAppInstalled);
 //		Log.d("Aptoide-ManagerDatabase", "is installed: "+selectIsAppInstalled);
 
 		if(cursorIsAppInstalled == null || cursorIsAppInstalled.isClosed()){
 			return false;
 		}
 		
 		if(cursorIsAppInstalled.getCount() == Constants.EMPTY_INT){
 			cursorIsAppInstalled.close();
 			return false;
 		}else{
 			cursorIsAppInstalled.close();
 			return true;
 		}
 	}
 	
 	
 	/**
 	 * getInstalledAppsDisplayInfo, retrieves a list of all installed apps
 	 *  
 	 * @return ViewDisplayListApps list of installed apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListApps getInstalledAppsDisplayInfo(EnumAppsSorting sortingPolicy, boolean showSystemApps){
 		
 		final int APP_NAME = Constants.COLUMN_FIRST;
 		final int APP_HASHID = Constants.COLUMN_SECOND;
 		final int INSTALLED_VERSION_NAME = Constants.COLUMN_THIRD;
 		final int INSTALLED_VERSION_CODE = Constants.COLUMN_FOURTH;
 		final int TIMESTAMP = Constants.COLUMN_FIFTH;
 		final int SIZE = Constants.COLUMN_SIXTH;
 		final int TYPE = Constants.COLUMN_SEVENTH;
 //		final int UP_TO_DATE_VERSION_NAME = Constants.COLUMN_FIFTH;
 		final int UP_TO_DATE_VERSION_CODE = Constants.COLUMN_EIGTH;
 //		final int DOWNGRADE_VERSION_NAME = Constants.COLUMN_SEVENTH;
 //		final int DOWNGRADE_VERSION_CODE = Constants.COLUMN_SIXTH;
 		
 		ViewDisplayListApps installedApps = null;
 		ViewDisplayApplicationBackup app;							
 		
 		String selectInstalledApps = "SELECT I."+Constants.KEY_APP_INSTALLED_NAME+",I."+Constants.KEY_APP_INSTALLED_HASHID
 											+",I."+Constants.KEY_APP_INSTALLED_VERSION_NAME+",I."+Constants.KEY_APP_INSTALLED_VERSION_CODE
 											+",I."+Constants.KEY_APP_INSTALLED_TIMESTAMP+",I."+Constants.KEY_APP_INSTALLED_SIZE
 											+",I."+Constants.KEY_APP_INSTALLED_TYPE+",U."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 									+" FROM "+Constants.TABLE_APP_INSTALLED+" I"
 									+" NATURAL LEFT JOIN (SELECT "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																+",MAX("+Constants.KEY_APPLICATION_VERSION_CODE+") AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 //																	+","+Constants.KEY_APPLICATION_VERSION_NAME+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 														 +" FROM "+Constants.TABLE_APPLICATION
 														 +" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY
 														 +" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 														 +" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") U";
 		
 		// not show System Apps
 		if(!showSystemApps){
 			selectInstalledApps		+=" WHERE "+"I."+Constants.KEY_APP_INSTALLED_TYPE+"!="+EnumAppStatus.SYSTEM.ordinal();
 		}
 		
 		// Sort by:
 		switch (sortingPolicy) {
 			case ALPHABETIC:
 				selectInstalledApps +=" ORDER BY I."+Constants.KEY_APP_INSTALLED_NAME;
 				break;
 			case FRESHNESS:
 				selectInstalledApps +=" ORDER BY I."+Constants.KEY_APP_INSTALLED_TIMESTAMP+" DESC ";
 				break;
 			case SIZE:
 				selectInstalledApps +=" ORDER BY I."+Constants.KEY_APP_INSTALLED_SIZE+" DESC ";
 				break;
 
 			default:
 				break;
 		}
 		Log.d("Aptoide-ManagerDatabase", "sorting by: "+sortingPolicy);
 //		Log.d("Aptoide-ManagerDatabase", "installed apps: "+selectInstalledApps);
 		try{
 			db.beginTransaction();
 			
 			Cursor appsCursor = aptoideNonAtomicQuery(selectInstalledApps);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			installedApps = new ViewDisplayListApps();
 			
 			if(appsCursor.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception
 				return installedApps;
 			}
 			
 			appsCursor.moveToFirst();
 			do{
 				app = new ViewDisplayApplicationBackup(appsCursor.getInt(APP_HASHID), appsCursor.getString(APP_NAME), appsCursor.getString(INSTALLED_VERSION_NAME)
 													,appsCursor.getLong(TIMESTAMP), appsCursor.getInt(SIZE) //Stored value is long (correct this in the future)
 													,(appsCursor.getInt(UP_TO_DATE_VERSION_CODE) == appsCursor.getInt(INSTALLED_VERSION_CODE)?EnumAppStatus.BACKED_UP:(EnumAppStatus.reverseOrdinal(appsCursor.getInt(TYPE))))
 													);
 				installedApps.add(app);
 
 			}while(appsCursor.moveToNext());
 			appsCursor.close();
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return installedApps;
 	}
 	
 	
 	/**
 	 * getTotalAvailableApps, retrieves how many available apps are visible
 	 * 						  in this category
 	 * 						  TODO sgbd second pass optimization not checked
 	 * 
 	 * @param int categoryHashid, hashid of category
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * 
 	 * @return int total available apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public int getTotalAvailableApps(int categoryHashid, boolean filterByHw, EnumAgeRating ratingFilter){
 
 		Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo ratingFilter: "+ratingFilter);
 		
 		final int TOTAL = Constants.COLUMN_FIRST;
 		
 		int totalApps = Constants.EMPTY_INT;
 		
 		String selectAvailableApps = "SELECT COUNT("+Constants.KEY_APPLICATION_HASHID+")"
 									+" FROM (SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_IN_USE
 											+" FROM "+Constants.TABLE_REPOSITORY
 											+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE+")"
 									+" NATURAL INNER JOIN (SELECT "+Constants.KEY_APPLICATION_FULL_HASHID+" ,"+Constants.KEY_APPLICATION_REPO_HASHID
 																	+" ,"+Constants.KEY_APPLICATION_HASHID+" ,"+Constants.KEY_APPLICATION_PACKAGE_NAME;
 			if(filterByHw){
 				selectAvailableApps	+=								" ,"+Constants.KEY_APPLICATION_MIN_SDK+" ,"+Constants.KEY_APPLICATION_MIN_SCREEN
 																	+" ,"+Constants.KEY_APPLICATION_MIN_GLES;
 			}
 				selectAvailableApps	+=							" FROM "+Constants.TABLE_APPLICATION
 			/* Exclude Installed apps */						+" WHERE NOT EXISTS (SELECT "+Constants.KEY_APP_INSTALLED_HASHID
 																					+" FROM "+Constants.TABLE_APP_INSTALLED
 																					+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID
 																						+"="+Constants.TABLE_APPLICATION+"."+Constants.KEY_APPLICATION_HASHID+")";
 			/* Filter by hardware? */
 			if(filterByHw){
 				ViewHwFilters filters = serviceData.getManagerSystemSync().getHwFilters();
 				Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo HW filters ON: "+filters);
 				
 				selectAvailableApps	+=							" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 																+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 																+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 			}
 			/* Filter by rating */
 			selectAvailableApps +=								" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 
 																+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+" )";
 			
 			/* Filter by category? */
 			if(categoryHashid != Constants.TOP_CATEGORY){
 				selectAvailableApps +=" NATURAL INNER JOIN (SELECT * FROM "+Constants.TABLE_APP_CATEGORY
 																+" WHERE "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID+"="+categoryHashid+")";
 			}			
 				
 //		Log.d("Aptoide-ManagerDatabase", "available apps: "+selectAvailableApps);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appsCursor = aptoideNonAtomicQuery(selectAvailableApps);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			appsCursor.moveToFirst();
 			
 			if(appsCursor.getCount() == 0){
 				appsCursor.close();
 				return Constants.EMPTY_INT;
 			}
 			
 			totalApps = appsCursor.getInt(TOTAL);
 			appsCursor.close();
 			
 			Log.d("Aptoide-ManagerDatabase", "total available apps: "+totalApps);
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return totalApps;
 	}
 	
 	
 	/**
 	 * getTotalAvailableApps, retrieves how many available apps are visible
 	 *  					  TODO sgbd second pass optimization not checked
 	 *  
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * 
 	 * @return int total available apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public int getTotalAvailableApps(boolean filterByHw, EnumAgeRating ratingFilter){
 		return getTotalAvailableApps(Constants.TOP_CATEGORY, filterByHw, ratingFilter);
 	}
 	
 	
 	/**
 	 * getAvailableAppsDisplayInfo, retrieves a list of all available apps by category
 	 * 
 	 * @param int offset, number of row to start from
 	 * @param int range, number of rows to list
 	 * @param int categoryHashid, hashid of category
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * @param EnumAppsSorting sortingPolicy
 	 * 
 	 * @return ViewDisplayListApps list of available apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListApps getAvailableAppsDisplayInfo(int offset, int range, int categoryHashid, boolean filterByHw, EnumAgeRating ratingFilter, EnumAppsSorting sortingPolicy){
 
 		Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo ratingFilter: "+ratingFilter);
 		
 		final int APP_HASHID = Constants.COLUMN_FIRST;
 		final int APP_NAME = Constants.COLUMN_SECOND;
 //		final int PACKAGE_NAME = Constants.COLUMN_THIRD;
 //		final int UP_TO_DATE_VERSION_CODE = Constants.COLUMN_FOURTH;
 		final int UP_TO_DATE_VERSION_NAME = Constants.COLUMN_FIFTH;
 		final int TIMESTAMP = Constants.COLUMN_SIXTH;
 		final int SIZE = Constants.COLUMN_SEVENTH;
 		final int INSTALLED = Constants.COLUMN_EIGTH;
 		
 		ViewDisplayListApps availableApps = null;
 		ViewDisplayApplicationBackup app;							
 		
 		String selectAvailableApps = "SELECT A."+Constants.KEY_APPLICATION_HASHID+", A."+Constants.KEY_APPLICATION_NAME+", A."+Constants.KEY_APPLICATION_PACKAGE_NAME
 											+", MAX(A."+Constants.KEY_APPLICATION_VERSION_CODE+") AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 											+", A."+Constants.KEY_APPLICATION_VERSION_NAME+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 											+", A."+Constants.KEY_APPLICATION_TIMESTAMP+", "+Constants.KEY_DOWNLOAD_SIZE
 											+", I."+Constants.KEY_APP_INSTALLED_HASHID+" AS installed"
 //											+", "+Constants.KEY_STATS_STARS+", "+Constants.KEY_STATS_DOWNLOADS
 									+" FROM (SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_IN_USE
 											+" FROM "+Constants.TABLE_REPOSITORY
 											+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE+")"
 									+" NATURAL INNER JOIN (SELECT * FROM "+Constants.TABLE_APPLICATION;
 
 			/* Filter by rating */
 			selectAvailableApps +=								" WHERE "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal();
 					
 			/* Filter by hardware? */
 			if(filterByHw){
 				ViewHwFilters filters = serviceData.getManagerSystemSync().getHwFilters();
 				Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo HW filters ON: "+filters);
 				
 				selectAvailableApps	+=							" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 																+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 																+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 			}
 			selectAvailableApps +=						" ) A";
 			
 			/* Filter by category? */
 			if(categoryHashid != Constants.TOP_CATEGORY){
 				selectAvailableApps +=" NATURAL INNER JOIN (SELECT * FROM "+Constants.TABLE_APP_CATEGORY
 																+" WHERE "+Constants.KEY_APP_CATEGORY_CATEGORY_HASHID+"="+categoryHashid+")";
 			}
 			
 				selectAvailableApps +=" NATURAL INNER JOIN "+Constants.TABLE_DOWNLOAD_INFO
 									+" NATURAL LEFT JOIN "+Constants.TABLE_STATS_INFO
 									+" LEFT JOIN "+Constants.TABLE_APP_INSTALLED+" I ON A."+Constants.KEY_APPLICATION_HASHID+"=I."+Constants.KEY_APP_INSTALLED_HASHID;
 			
 			
 				selectAvailableApps +=" GROUP BY A."+Constants.KEY_APPLICATION_PACKAGE_NAME;
 			// Sort by:
 			switch (sortingPolicy) {
 				case ALPHABETIC:
 					selectAvailableApps +=" ORDER BY A."+Constants.KEY_APPLICATION_NAME;
 					break;
 				case FRESHNESS:
 					selectAvailableApps +=" ORDER BY A."+Constants.KEY_APPLICATION_TIMESTAMP+" DESC ";
 					break;
 				case SIZE:
 					selectAvailableApps +=" ORDER BY "+Constants.KEY_DOWNLOAD_SIZE+" DESC ";
 					break;
 				case STARS:
 					selectAvailableApps +=" ORDER BY "+Constants.KEY_STATS_STARS+" DESC ";
 					break;
 				case DOWNLOADS:
 					selectAvailableApps +=" ORDER BY "+Constants.KEY_STATS_DOWNLOADS+" DESC ";
 					break;
 	
 				default:
 					break;
 			}
 			Log.d("Aptoide-ManagerDatabase", "sorting by: "+sortingPolicy);
 			if(range > 0){
 				selectAvailableApps +=	" LIMIT ?"
 										+" OFFSET ?";
 			}
 		String[] selectAvailableAppsArgs = new String[] {Integer.toString(range), Integer.toString(offset)};
 //		Log.d("Aptoide-ManagerDatabase", "available apps: "+selectAvailableApps);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appsCursor;
 			
 			if(range > 0){
 				appsCursor = aptoideNonAtomicQuery(selectAvailableApps, selectAvailableAppsArgs);
 			}else{
 				appsCursor = aptoideNonAtomicQuery(selectAvailableApps);
 			}
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			availableApps = new ViewDisplayListApps();
 			
 			appsCursor.moveToFirst();
 			
 			if(appsCursor.getCount() == 0){
 				appsCursor.close();
 				return availableApps;
 			}
 
 			do{									
 //				Log.d("Aptoide-ManagerDatabase", "installed: "+appsCursor.getLong(INSTALLED));										
 				app = new ViewDisplayApplicationBackup(appsCursor.getInt(APP_HASHID), appsCursor.getString(APP_NAME), appsCursor.getString(UP_TO_DATE_VERSION_NAME),
 													appsCursor.getLong(TIMESTAMP), appsCursor.getInt(SIZE), ((!appsCursor.isNull(INSTALLED) && appsCursor.getInt(INSTALLED) == appsCursor.getInt(APP_HASHID))?EnumAppStatus.INSTALLED:EnumAppStatus.OTHER));
 				availableApps.add(app);
 
 			}while(appsCursor.moveToNext());
 			appsCursor.close();
 			
 //			Log.d("Aptoide-ManagerDatabase", "available apps: "+availableApps);
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return availableApps;
 	}
 	
 	
 	/**
 	 * getAvailableAppsDisplayInfo, retrieves a list of all available apps
 	 * 
 	 * @param int offset, number of row to start from
 	 * @param int range, number of rows to list
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * @param EnumAppsSorting sortingPolicy
 	 * 
 	 * @return ViewDisplayListApps list of available apps
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListApps getAvailableAppsDisplayInfo(int offset, int range, boolean filterByHw, EnumAgeRating ratingFilter, EnumAppsSorting sortingPolicy){
 		return getAvailableAppsDisplayInfo(offset, range, Constants.TOP_CATEGORY, filterByHw, ratingFilter, sortingPolicy);
 	}
 	
 	
 	/**
 	 * getUpdatableAppsDisplayInfo, retrieves a list of all apps' updates
 	 * 
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * @param EnumAppsSorting sortingPolicy
 	 * 
 	 * @return ViewDisplayListApps list of apps available updates
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayListApps getUpdatableAppsDisplayInfo(boolean filterByHw, EnumAgeRating ratingFilter, EnumAppsSorting sortingPolicy){
 		Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo ratingFilter: "+ratingFilter);
 		
 		final int APP_HASHID = Constants.COLUMN_FIRST;
 		final int APP_NAME = Constants.COLUMN_SECOND;
 		final int INSTALLED_VERSION_NAME = Constants.COLUMN_THIRD;
 		final int INSTALLED_VERSION_CODE = Constants.COLUMN_FOURTH;
 		final int UP_TO_DATE_VERSION_NAME = Constants.COLUMN_FIFTH;
 		final int UP_TO_DATE_VERSION_CODE = Constants.COLUMN_SIXTH;
 		final int UPDATE_STARS = Constants.COLUMN_SEVENTH;
 		final int UPDATE_DOWNLOADS = Constants.COLUMN_EIGTH;
 		
 		ViewDisplayListApps updatableApps = null;
 		ViewDisplayApplicationUpdatable app;	
 		
 		String selectUpdatableApps = "SELECT "+Constants.KEY_APP_INSTALLED_HASHID+","+Constants.KEY_APP_INSTALLED_NAME
 											+","+Constants.KEY_APP_INSTALLED_VERSION_NAME+","+Constants.KEY_APP_INSTALLED_VERSION_CODE
 											+",U."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME+","+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 											+","+Constants.KEY_STATS_STARS+","+Constants.KEY_STATS_DOWNLOADS
 										+" FROM "+Constants.TABLE_APP_INSTALLED+" I"
 										+" NATURAL INNER JOIN (SELECT "+Constants.KEY_REPO_HASHID
 															+" FROM "+Constants.TABLE_REPOSITORY
 															+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE+")"
 										+" NATURAL INNER JOIN (SELECT "+Constants.KEY_APPLICATION_FULL_HASHID+", "+Constants.KEY_APPLICATION_REPO_HASHID
 																	+", "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																	+", "+Constants.KEY_APPLICATION_VERSION_CODE+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 																	+", "+Constants.KEY_APPLICATION_VERSION_NAME+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 																	+", "+Constants.KEY_APPLICATION_TIMESTAMP
 															+" FROM "+Constants.TABLE_APPLICATION+" A"
 															+" WHERE "+Constants.KEY_APPLICATION_VERSION_CODE
 																	+"="+"(SELECT MAX("+Constants.KEY_APPLICATION_VERSION_CODE+")"
 																		+" FROM "+Constants.TABLE_APPLICATION
 																		+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																				+"= A."+Constants.KEY_APPLICATION_PACKAGE_NAME+")";
 			// Filter by hardware?
 			if(filterByHw){
 				ViewHwFilters filters = serviceData.getManagerSystemSync().getHwFilters(); 
 				Log.d("Aptoide-ManagerDatabase", "getUpdatableAppsDisplayInfo HW filters ON: "+filters);
 				selectUpdatableApps	+=						" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 							+								" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 							+								" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 			}
 			/* Filter by rating */
 			selectUpdatableApps +=							" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 					
 															+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") U"
 										+" NATURAL LEFT JOIN "+Constants.TABLE_STATS_INFO
 										+" WHERE U."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE+"> I."+Constants.KEY_APP_INSTALLED_VERSION_CODE;
 			// Sort by:
 			switch (sortingPolicy) {
 				case ALPHABETIC:
 					selectUpdatableApps +=" ORDER BY I."+Constants.KEY_APP_INSTALLED_NAME;
 					break;
 				case FRESHNESS:
 					selectUpdatableApps +=" ORDER BY U."+Constants.KEY_APPLICATION_TIMESTAMP+" DESC ";
 					break;
 				case STARS:
 					selectUpdatableApps +=" ORDER BY S."+Constants.KEY_STATS_STARS+" DESC ";
 					break;
 				case DOWNLOADS:
 					selectUpdatableApps +=" ORDER BY S."+Constants.KEY_STATS_DOWNLOADS+" DESC ";
 					break;
 	
 				default:
 					break;
 			}
 		Log.d("Aptoide-ManagerDatabase", "sorting by: "+sortingPolicy);
 //		Log.d("Aptoide-ManagerDatabase", "updatable apps: "+selectUpdatableApps);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appsCursor = aptoideNonAtomicQuery(selectUpdatableApps);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			if(appsCursor.getCount() == Constants.EMPTY_INT){
 				appsCursor.close();
 				return null;
 			}
 			
 			updatableApps = new ViewDisplayListApps();
 			
 			appsCursor.moveToFirst();
 			do{
 				if( (appsCursor.getInt(UP_TO_DATE_VERSION_CODE) <= 0?false:(appsCursor.getInt(UP_TO_DATE_VERSION_CODE) > appsCursor.getInt(INSTALLED_VERSION_CODE))) ){
 					app = new ViewDisplayApplicationUpdatable(appsCursor.getInt(APP_HASHID), appsCursor.getString(APP_NAME), appsCursor.getString(INSTALLED_VERSION_NAME)
 							 , appsCursor.getString(UP_TO_DATE_VERSION_NAME), appsCursor.getFloat(UPDATE_STARS), appsCursor.getInt(UPDATE_DOWNLOADS));
 					updatableApps.add(app);
 				}
 
 			}while(appsCursor.moveToNext());
 			appsCursor.close();
 			
 //			Log.d("Aptoide-ManagerDatabase", "updatable apps: "+updatableApps);
 			
 		}catch (Exception e) {
 			e.printStackTrace();
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 		}
 		return updatableApps;
 		
 	}
 	
 	
 	/**
 	 * getUpdatableAppsDownloadInfo, retrieves a list of all updates download info
 	 * 
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * 
 	 * @return ViewListAppsDownload updates
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewListAppsDownload getUpdatableAppsDownloadInfo(boolean filterByHw, EnumAgeRating ratingFilter){
 		Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo ratingFilter: "+ratingFilter);
 		
 		final int APP_HASHID = Constants.COLUMN_FIRST;
 		final int UP_TO_DATE_VERSION_CODE = Constants.COLUMN_SECOND;
 		final int UP_TO_DATE_VERSION_NAME = Constants.COLUMN_THIRD;
 		final int APP_NAME = Constants.COLUMN_FOURTH;
 		final int INSTALLED_VERSION_CODE = Constants.COLUMN_FIFTH;
 		final int REPO_HASHID = Constants.COLUMN_SIXTH;
 		final int REMOTE_PATH_BASE = Constants.COLUMN_SEVENTH;
 		final int REMOTE_PATH_TAIL = Constants.COLUMN_EIGTH;
 		final int SIZE = Constants.COLUMN_NINTH;
 		final int MD5HASH = Constants.COLUMN_TENTH;
 		final int USERNAME = Constants.COLUMN_ELEVENTH;
 		final int PASSWORD = Constants.COLUMN_TWELVETH;
 
 		
 		ViewListAppsDownload updatableApps = new ViewListAppsDownload();
 		ViewDownload appDownload = null;
 		
 		String selectAppDownloadInfo = "SELECT up_to_date_app_hashid"
 										+","+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE+","+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 										+","+Constants.KEY_APPLICATION_NAME+","+Constants.KEY_APP_INSTALLED_VERSION_CODE
 										+", R."+Constants.KEY_REPO_HASHID+", R."+Constants.KEY_REPO_BASE_PATH
 										+", "+Constants.KEY_DOWNLOAD_REMOTE_PATH_TAIL+", "+Constants.KEY_DOWNLOAD_SIZE
 										+", "+Constants.KEY_DOWNLOAD_MD5HASH
 										+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 									+" FROM "+Constants.TABLE_APP_INSTALLED+" I"
 									+" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY+" R"
 									+" NATURAL INNER JOIN (SELECT "+Constants.KEY_APPLICATION_FULL_HASHID+", "+Constants.KEY_APPLICATION_REPO_HASHID
 																+", "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																+", "+Constants.KEY_APPLICATION_HASHID+" AS up_to_date_app_hashid"
 																+", "+Constants.KEY_APPLICATION_VERSION_CODE+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 																+", "+Constants.KEY_APPLICATION_VERSION_NAME+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 																+", "+Constants.KEY_APPLICATION_TIMESTAMP
 														+" FROM "+Constants.TABLE_APPLICATION+" A"
 														+" WHERE "+Constants.KEY_APPLICATION_VERSION_CODE
 																+"="+"(SELECT MAX("+Constants.KEY_APPLICATION_VERSION_CODE+")"
 																	+" FROM "+Constants.TABLE_APPLICATION
 																	+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																			+"= A."+Constants.KEY_APPLICATION_PACKAGE_NAME+")";
 		// Filter by hardware?
 		if(filterByHw){
 		ViewHwFilters filters = serviceData.getManagerSystemSync().getHwFilters(); 
 		Log.d("Aptoide-ManagerDatabase", "getUpdatableAppsDisplayInfo HW filters ON: "+filters);
 		selectAppDownloadInfo +=							" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 														+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 														+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 		}
 		/* Filter by rating */
 		selectAppDownloadInfo +=							" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 		
 														+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") U"
 									+" NATURAL LEFT JOIN "+Constants.TABLE_DOWNLOAD_INFO
 									+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 									+" WHERE U."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE+"> I."+Constants.KEY_APP_INSTALLED_VERSION_CODE
 									+" AND "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE;
 		
 //		Log.d("Aptoide-ManagerDatabase", "updatable apps download info: "+selectAppDownloadInfo);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appDownloadInfoCursor = aptoideNonAtomicQuery(selectAppDownloadInfo);
 			
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			if(appDownloadInfoCursor.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception (Unrecognized appHashid)
 			}
 			appDownloadInfoCursor.moveToFirst();
 
 			do{
 				if((appDownloadInfoCursor.getInt(UP_TO_DATE_VERSION_CODE) <= 0?false:(appDownloadInfoCursor.getInt(UP_TO_DATE_VERSION_CODE) > appDownloadInfoCursor.getInt(INSTALLED_VERSION_CODE)))){
 					if(!appDownloadInfoCursor.isNull(REMOTE_PATH_TAIL)){
 						if(appDownloadInfoCursor.isNull(USERNAME)){
 							appDownload = serviceData.getManagerDownloads().prepareApkDownload(appDownloadInfoCursor.getInt(APP_HASHID)
 												, appDownloadInfoCursor.getString(APP_NAME)+"   v."+appDownloadInfoCursor.getString(UP_TO_DATE_VERSION_NAME)
 												, appDownloadInfoCursor.getString(REMOTE_PATH_BASE)+appDownloadInfoCursor.getString(REMOTE_PATH_TAIL)
 												, appDownloadInfoCursor.getInt(SIZE)*Constants.KBYTES_TO_BYTES, appDownloadInfoCursor.getString(MD5HASH));
 						}else{
 							ViewLogin login = new ViewLogin(appDownloadInfoCursor.getString(USERNAME), appDownloadInfoCursor.getString(PASSWORD));
 							
 							appDownload = serviceData.getManagerDownloads().prepareApkDownload(appDownloadInfoCursor.getInt(APP_HASHID)
 									, appDownloadInfoCursor.getString(APP_NAME)+"   v."+appDownloadInfoCursor.getString(UP_TO_DATE_VERSION_NAME)
 									, appDownloadInfoCursor.getString(REMOTE_PATH_BASE)+appDownloadInfoCursor.getString(REMOTE_PATH_TAIL), login
 									, appDownloadInfoCursor.getInt(SIZE)*Constants.KBYTES_TO_BYTES, appDownloadInfoCursor.getString(MD5HASH));
 						}
 						updatableApps.addDownload(appDownload);
 					}else{
 						updatableApps.addNoInfoId(appDownloadInfoCursor.getInt(REPO_HASHID), appDownloadInfoCursor.getInt(APP_HASHID));
 					}
 				}
 			}while(appDownloadInfoCursor.moveToNext());
 			appDownloadInfoCursor.close();
 			
 //			Log.d("Aptoide-ManagerDatabase", "updatable apps: "+updatableApps);
 			
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 		}
 		return updatableApps;
 		
 	}
 	
 	
 	/**
 	 * getAppSearchResults, retrieves a list of available Apps matching the search string parameters
 	 * 
 	 * @param String searchString, search parameters
 	 * @param boolean filterByHw
 	 * @param EnumAgeRating ratingFilter
 	 * @param EnumAppsSorting sortingPolicy
 	 * 
 	 * @return ViewDisplayListApps, list of available Apps matching the search string parameters
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */	
 	public ViewDisplayListApps getAppSearchResultsDisplayInfo(String searchString, boolean filterByHw, EnumAgeRating ratingFilter, EnumAppsSorting sortingPolicy){
 
 		Log.d("Aptoide-ManagerDatabase", "getAvailableAppsDisplayInfo ratingFilter: "+ratingFilter);
 		
 		final int APP_NAME = Constants.COLUMN_FIRST;
 		final int APP_HASHID = Constants.COLUMN_SECOND;
 //		final int PACKAGE_NAME = Constants.COLUMN_THIRD;
 //		final int UP_TO_DATE_VERSION_CODE = Constants.COLUMN_FOURTH;
 		final int UP_TO_DATE_VERSION_NAME = Constants.COLUMN_FIFTH;
 		final int STARS = Constants.COLUMN_SIXTH;
 		final int DOWNLOADS = Constants.COLUMN_SEVENTH;
 		
 		ViewDisplayListApps availableApps = null;
 		ViewDisplayApplicationAvailable app;							
 		
 		String selectAvailableApps = "SELECT A."+Constants.KEY_APPLICATION_NAME+", A."+Constants.KEY_APPLICATION_HASHID+", A."+Constants.KEY_APPLICATION_PACKAGE_NAME
 											+", A."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE+", A."+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 											+", S."+Constants.KEY_STATS_STARS+", S."+Constants.KEY_STATS_DOWNLOADS
 											+" FROM "+Constants.TABLE_REPOSITORY									
 											+" NATURAL INNER JOIN (SELECT "+Constants.KEY_APPLICATION_FULL_HASHID+", "+Constants.KEY_APPLICATION_REPO_HASHID+", "+Constants.KEY_APPLICATION_HASHID
 																		+", "+Constants.KEY_APPLICATION_PACKAGE_NAME+", "+Constants.KEY_APPLICATION_NAME
 																		+", "+Constants.KEY_APPLICATION_VERSION_CODE+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_CODE
 																		+", "+Constants.KEY_APPLICATION_VERSION_NAME+" AS "+Constants.DISPLAY_APP_UP_TO_DATE_VERSION_NAME
 																+" FROM "+Constants.TABLE_APPLICATION+" P"
 																+" WHERE ("+Constants.KEY_APPLICATION_NAME+" LIKE '%"+searchString+"%'"
 																		+ "OR "+Constants.KEY_APPLICATION_PACKAGE_NAME+" LIKE '%"+searchString+"%'"+")"
 																+" AND "+Constants.KEY_APPLICATION_VERSION_CODE
 																		+"="+"(SELECT MAX("+Constants.KEY_APPLICATION_VERSION_CODE+")"
 																			+" FROM "+Constants.TABLE_APPLICATION
 																			+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME
 																					+"= P."+Constants.KEY_APPLICATION_PACKAGE_NAME+")";
 		
 		// Filter by hardware?
 		if(filterByHw){
 			ViewHwFilters filters = serviceData.getManagerSystemSync().getHwFilters(); 
 			Log.d("Aptoide-ManagerDatabase", "getUpdatableAppsDisplayInfo HW filters ON: "+filters);
 			selectAvailableApps	+=								" AND "+Constants.KEY_APPLICATION_MIN_SDK+"<="+filters.getSdkVersion()
 																+" AND "+Constants.KEY_APPLICATION_MIN_SCREEN+"<="+filters.getScreenSize()
 																+" AND "+Constants.KEY_APPLICATION_MIN_GLES+"<="+filters.getGlEsVersion();
 		}
 		/* Filter by rating */
 		selectAvailableApps +=									" AND "+Constants.KEY_APPLICATION_RATING+"<"+ratingFilter.ordinal()
 															
 																+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") A"
 														
 									+" NATURAL LEFT JOIN "+Constants.TABLE_STATS_INFO+" S"
 									+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE;
 			// Sort by:
 			switch (sortingPolicy) {
 				case ALPHABETIC:
 					selectAvailableApps +=" ORDER BY A."+Constants.KEY_APPLICATION_NAME;
 					break;
 				case FRESHNESS:
 					selectAvailableApps +=" ORDER BY A."+Constants.KEY_APPLICATION_TIMESTAMP+" DESC ";
 					break;
 				case STARS:
 					selectAvailableApps +=" ORDER BY S."+Constants.KEY_STATS_STARS+" DESC ";
 					break;
 				case DOWNLOADS:
 					selectAvailableApps +=" ORDER BY S."+Constants.KEY_STATS_DOWNLOADS+" DESC ";
 					break;
 	
 				default:
 					break;
 			}
 			Log.d("Aptoide-ManagerDatabase", "sorting by: "+sortingPolicy);
 //			Log.d("Aptoide-ManagerDatabase", "search results apps: "+selectAvailableApps);
 		
 		try{
 			db.beginTransaction();
 			Cursor appsCursor = aptoideNonAtomicQuery(selectAvailableApps);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			availableApps = new ViewDisplayListApps();
 			
 			appsCursor.moveToFirst();
 			
 			if(appsCursor.getCount() == 0){
 				appsCursor.close();
 				return availableApps;
 			}
 			
 			do{																			
 				app = new ViewDisplayApplicationAvailable(appsCursor.getInt(APP_HASHID), appsCursor.getString(APP_NAME), appsCursor.getFloat(STARS)
 														, appsCursor.getInt(DOWNLOADS), appsCursor.getString(UP_TO_DATE_VERSION_NAME));
 				availableApps.add(app);
 
 			}while(appsCursor.moveToNext());
 			appsCursor.close();
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return availableApps;
 		
 	}
 	
 	
 	/**
 	 * getIconsDownloadInfo, retrieves a list of icons' Download Info
 	 * 
 	 * @param ViewRepository repository
 	 * @param int offset, number of row to start from
 	 * @param int range, number of rows to list
 	 * 
 	 * @return ViewDownloadInfo list of icons Download Info
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */		//TODO refactor data transport -> move ArrayList to a full data transport object with it's size properly initialized
 	public ArrayList<ViewDownloadInfo> getIconsDownloadInfo(ViewRepository repository, int offset, int range){
 		
 		final int ICONS_PATH = Constants.COLUMN_FIRST;
 		final int REMOTE_PATH_TAIL = Constants.COLUMN_FIRST;
 		final int APP_HASHID = Constants.COLUMN_SECOND;
 		final int APP_NAME = Constants.COLUMN_THIRD;
 		
 		
 		ArrayList<ViewDownloadInfo> iconsInfo = null;
 		ViewDownloadInfo iconInfo;							
 		
 		String selectRepoIconsPath = "SELECT "+Constants.KEY_REPO_ICONS_PATH
 									+" FROM "+Constants.TABLE_REPOSITORY
 									+" WHERE "+Constants.KEY_REPO_HASHID+"="+repository.getHashid();
 		
 		String selectIconDownloadInfo = "SELECT "+Constants.KEY_ICON_REMOTE_PATH_TAIL+","+Constants.KEY_APPLICATION_HASHID+","+Constants.KEY_APPLICATION_NAME
 										+" FROM "+Constants.TABLE_REPOSITORY
 										+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 										+" NATURAL INNER JOIN "+Constants.TABLE_ICON_INFO
 										+" WHERE "+Constants.KEY_REPO_HASHID+"="+repository.getHashid()
 									+" ORDER BY "+Constants.KEY_APPLICATION_NAME
 									+" LIMIT ?"
 									+" OFFSET ?";
 		String[] selectIconDownloadInfoArgs = new String[] {Integer.toString(range), Integer.toString(offset)};
 		
 //		Log.d("Aptoide-ManagerDatabase", "icons: "+selectIconDownloadInfo);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor repoCursor = aptoideNonAtomicQuery(selectRepoIconsPath);
 			Cursor iconsCursor = aptoideNonAtomicQuery(selectIconDownloadInfo, selectIconDownloadInfoArgs);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			iconsInfo = new ArrayList<ViewDownloadInfo>(iconsCursor.getCount());
 			
 			if(iconsCursor.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception
 				return iconsInfo;
 			}
 			
 			repoCursor.moveToFirst();
 			String repoIconsPath = repoCursor.getString(ICONS_PATH);
 			repoCursor.close();
 			
 			
 			iconsCursor.moveToFirst();
 			do{
 				iconInfo = new ViewDownloadInfo(repoIconsPath+iconsCursor.getString(REMOTE_PATH_TAIL), iconsCursor.getString(APP_NAME)
 												, iconsCursor.getInt(APP_HASHID), EnumDownloadType.ICON);
 				iconsInfo.add(iconInfo);
 
 			}while(iconsCursor.moveToNext());
 			iconsCursor.close();
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return iconsInfo;
 	}
 	
 	
 	/**
 	 * getIconDownloadInfo, retrieves an icon's Download Info
 	 * 
 	 * @param ViewRepository repository
 	 * @param int appHashid
 	 * 
 	 * @return ViewDownloadInfo list of icons Download Info
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */	
 	public ViewDownloadInfo getIconDownloadInfo(ViewRepository repository, int appHashid){
 		final int ICONS_PATH = Constants.COLUMN_FIRST;
 		
 		final int REMOTE_PATH_TAIL = Constants.COLUMN_FIRST;
 		final int APP_NAME = Constants.COLUMN_SECOND;
 		
 		String repoIconsPath = repository.getIconsPath();
 		
 		ViewDownloadInfo iconInfo = null;							
 		
 		String selectRepoIconsPath = "SELECT "+Constants.KEY_REPO_ICONS_PATH
 									+" FROM "+Constants.TABLE_REPOSITORY
 									+" WHERE "+Constants.KEY_REPO_HASHID+"="+repository.getHashid();
 		
 		String selectIconDownloadInfo = "SELECT "+Constants.KEY_ICON_REMOTE_PATH_TAIL+","+Constants.KEY_APPLICATION_NAME
 										+" FROM "+Constants.TABLE_REPOSITORY
 										+" NATURAL LEFT JOIN "+Constants.TABLE_APPLICATION
 										+" NATURAL LEFT JOIN "+Constants.TABLE_ICON_INFO
 										+" WHERE "+Constants.KEY_REPO_HASHID+"="+repository.getHashid()
 										+" AND "+Constants.KEY_APPLICATION_HASHID+"="+appHashid
 									+" ORDER BY "+Constants.KEY_APPLICATION_NAME;
 		
 //		Log.d("Aptoide-ManagerDatabase", "icon: "+selectIconDownloadInfo);
 		try{
 			Cursor repoCursor = null;
 			db.beginTransaction();
 			
 			if(repoIconsPath == null){
 				repoCursor = aptoideNonAtomicQuery(selectRepoIconsPath);
 			}
 			Cursor iconsCursor = aptoideNonAtomicQuery(selectIconDownloadInfo);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			if(repoIconsPath == null){
 				repoCursor.moveToFirst();
 				repoIconsPath = repoCursor.getString(ICONS_PATH);
 				repoCursor.close();				
 			}
 			
 			
 			if(iconsCursor.getCount()!= Constants.EMPTY_INT){
 				iconsCursor.moveToFirst();
 				iconInfo = new ViewDownloadInfo(repoIconsPath+iconsCursor.getString(REMOTE_PATH_TAIL), iconsCursor.getString(APP_NAME)
 												, appHashid, EnumDownloadType.ICON);
 			}
 			iconsCursor.close();
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return iconInfo;
 	}
 	
 	
 	/**
 	 * getScreensDownloadInfo, retrieves a list of screens' Download Info
 	 * 
 	 * @param ViewRepository repository
 	 * @param int offset, number of row to start from
 	 * @param int range, number of rows to list
 	 * 
 	 * @return ViewDownloadInfo list of icons Download Info
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */		//TODO refactor data transport -> move ArrayList to a full data transport object with it's size properly initialized
 	public ArrayList<ViewDownloadInfo> getScreensDownloadInfo(ViewRepository repository, int appHashid){
 		
 		final int APP_HASHID = Constants.COLUMN_FIRST;
 		final int APP_NAME = Constants.COLUMN_SECOND;
 		final int SCREENS_BASE_PATH = Constants.COLUMN_THIRD;
 		final int REMOTE_PATH_TAIL = Constants.COLUMN_FOURTH;
 		
 		
 		ArrayList<ViewDownloadInfo> screensInfo = null;
 		ViewDownloadInfo iconInfo;							
 		
 		String selectScreenDownloadInfo = "SELECT "+Constants.KEY_APPLICATION_HASHID+","+Constants.KEY_APPLICATION_NAME
 												+","+Constants.KEY_REPO_SCREENS_PATH+","+Constants.KEY_SCREEN_REMOTE_PATH_TAIL
 										+" FROM "+Constants.TABLE_REPOSITORY
 										+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 										+" NATURAL INNER JOIN "+Constants.TABLE_SCREEN_INFO
 										+" WHERE "+Constants.KEY_REPO_HASHID+"="+repository.getHashid()
 										+" AND "+Constants.KEY_APPLICATION_HASHID+"="+appHashid;
 		
 //		Log.d("Aptoide-ManagerDatabase", "screens: "+selectScreenDownloadInfo);
 		try{
 			db.beginTransaction();
 			
 			Cursor screensCursor = aptoideNonAtomicQuery(selectScreenDownloadInfo);
 
 			db.setTransactionSuccessful();
 			db.endTransaction();
 
 			if(screensCursor.getCount() == Constants.EMPTY_INT){
 				screensCursor.close();				
 				return screensInfo;
 			}
 			
 			screensInfo = new ArrayList<ViewDownloadInfo>(screensCursor.getCount());
 			
 			screensCursor.moveToFirst();
 			do{
 				iconInfo = new ViewDownloadInfo(screensCursor.getString(SCREENS_BASE_PATH)+screensCursor.getString(REMOTE_PATH_TAIL)
 											, screensCursor.getString(APP_NAME), screensCursor.getInt(APP_HASHID), EnumDownloadType.SCREEN);
 				screensInfo.add(iconInfo);
 
 			}while(screensCursor.moveToNext());
 			screensCursor.close();
 	
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		return screensInfo;
 	}
 	
 	
 	/**
 	 * appInRepo, checks if the app/version referenced by appHashid is present on the repo referenced by the repoHashid
 	 * 
 	 * @param int repoHashid
 	 * @param int appHashid
 	 * 
 	 * @return boolean is app in repo?
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean appInRepo(int repoHashid, int appHashid){
 		final int COUNT = Constants.COLUMN_FIRST;
 		
 		boolean appInRepo = false;
 		
 		String selectAppInRepo = "SELECT count(distinct "+Constants.KEY_APPLICATION_HASHID+")"
 								+" FROM "+Constants.TABLE_REPOSITORY
 								+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION
 								+" WHERE "+Constants.KEY_REPO_HASHID+"="+repoHashid
 								+" AND "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 								+" AND "+Constants.KEY_APPLICATION_HASHID+"="+appHashid;
 
 		Cursor appInRepoCursor = aptoideAtomicQuery(selectAppInRepo);
 
 		if(appInRepoCursor == null || appInRepoCursor.isClosed()){
 			return appInRepo;
 		}
 		
 		appInRepoCursor.moveToFirst();
 		
 		appInRepo = (appInRepoCursor.getInt(COUNT)==Constants.DB_TRUE?true:false);
 		
 		appInRepoCursor.close();
 //		Log.d("Aptoide-appInRepo", "appInrepo? "+appInRepo+"  "+selectAppInRepo);
 		
 		return appInRepo;
 	}
 	
 	
 	/**
 	 * appAnyVersionInRepo, checks if the app referenced by packageName is present on the repo referenced by the repoHashid
 	 * 
 	 * @param int repoHashid
 	 * @param String packageName
 	 * 
 	 * @return boolean is app in repo?
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public boolean appAnyVersionInRepo(int repoHashid, String packageName){
 		final int COUNT = Constants.COLUMN_FIRST;
 		
 		boolean appInRepo = false;
 		
 		String selectAppInRepo = "SELECT count(distinct "+Constants.KEY_APPLICATION_PACKAGE_NAME+")"
 								+" FROM "+Constants.TABLE_REPOSITORY
 								+" NATURAL LEFT JOIN "+Constants.TABLE_APPLICATION
 								+" WHERE "+Constants.KEY_REPO_HASHID+"="+repoHashid
 								+" AND "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 								+" AND "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'";
 
 		Cursor appInRepoCursor = aptoideAtomicQuery(selectAppInRepo);
 
 		if(appInRepoCursor == null || appInRepoCursor.isClosed()){
 			return appInRepo;
 		}
 		
 		appInRepoCursor.moveToFirst();
 		
 		appInRepo = (appInRepoCursor.getInt(COUNT)==Constants.DB_TRUE?true:false);
 		
 		appInRepoCursor.close();
 //		Log.d("Aptoide-appInRepo", "appInrepo? "+appInRepo+" "+selectAppInRepo);
 		
 		return appInRepo;
 	}
 	
 	
 	/**
 	 * getAppRepo, get a repo that contains the app/version represented by the appHashid
 	 * 			   TODO sgbd second pass optimization not checked
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @return ViewRepository the repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewRepository getAppRepo(int appHashid){
 		final int REPO_HASHID = Constants.COLUMN_FIRST;
 		final int URI = Constants.COLUMN_SECOND;
 //		final int IN_USE = Constants.COLUMN_THIRD;
 		final int SIZE = Constants.COLUMN_FOURTH;
 		final int BASE_PATH = Constants.COLUMN_FIFTH;
 		final int ICONS_PATH = Constants.COLUMN_SIXTH;
 		final int SCREENS_PATH = Constants.COLUMN_SEVENTH;
 		final int DELTA = Constants.COLUMN_EIGTH;
 		
 //		final int LOGIN_REPO_HASHID = Constants.COLUMN_FIRST;
 		final int USERNAME = Constants.COLUMN_SECOND;
 		final int PASSWORD = Constants.COLUMN_THIRD;
 
 		String selectRepo = "SELECT "+Constants.KEY_REPO_HASHID+", "+Constants.KEY_REPO_URI+", "+Constants.KEY_REPO_IN_USE+", "+Constants.KEY_REPO_SIZE
 									+", "+Constants.KEY_REPO_BASE_PATH+", "+Constants.KEY_REPO_ICONS_PATH+", "+Constants.KEY_REPO_SCREENS_PATH
 									+", "+Constants.KEY_REPO_DELTA
 							+" FROM (SELECT * FROM "+Constants.TABLE_REPOSITORY;
 		if(appInRepo(Constants.APPS_REPO_HASHID, appHashid)){
 				selectRepo+=" WHERE "+Constants.KEY_REPO_HASHID+"="+Constants.APPS_REPO_HASHID+")";
 		}else{
 				selectRepo+=" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE+")"
 							+" NATURAL LEFT JOIN (SELECT "+Constants.KEY_APPLICATION_HASHID
 												+", "+Constants.KEY_APPLICATION_REPO_HASHID
 												+" FROM "+Constants.TABLE_APPLICATION
 												+" WHERE "+Constants.KEY_APPLICATION_HASHID+"="+appHashid
 												+" GROUP BY "+Constants.KEY_APPLICATION_HASHID+")";
 		}
 		Cursor repoCursor = aptoideAtomicQuery(selectRepo);
 		
 		if(repoCursor == null || repoCursor.isClosed() || repoCursor.getCount()==Constants.EMPTY_INT){
 			if(!repoCursor.isClosed()){
 				repoCursor.close();
 			}
 			return null;		//TODO refactor null object
 		}else{
 			repoCursor.moveToFirst();
 		}
 		
 		String selectLogin = "SELECT *"
 							 +" FROM "+Constants.TABLE_LOGIN
 							 +" WHERE "+Constants.KEY_LOGIN_REPO_HASHID+"="+repoCursor.getInt(REPO_HASHID);
 		Cursor loginCursor = aptoideAtomicQuery(selectLogin);
 		
 		ViewRepository repo = new ViewRepository(repoCursor.getString(URI), repoCursor.getInt(SIZE), repoCursor.getString(BASE_PATH)
 												, repoCursor.getString(ICONS_PATH), repoCursor.getString(SCREENS_PATH), repoCursor.getString(DELTA));
 		
 		if( !(loginCursor.getCount() == Constants.EMPTY_INT) ){
 			loginCursor.moveToFirst();
 			ViewLogin login = new ViewLogin(loginCursor.getString(USERNAME),loginCursor.getString(PASSWORD));
 			repo.setLogin(login);
 		}
 		loginCursor.close();
 //		Log.d("Aptoide-getAppRepo", "appRepo: "+repo+" "+selectRepo);
 		
 		repoCursor.close();
 		
 		return repo;
 	}
 	
 	
 	/**
 	 * getAppAnyVersionRepo, get a repo that contains the app represented by the appHashid in any version
 	 * 			   			 TODO sgbd second pass optimization not checked
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @return ViewAppVersionRepository the application's version and repository
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewAppVersionRepository getAppAnyVersionRepo(int appHashid){
 		final int REPO_HASHID = Constants.COLUMN_FIRST;
 		final int URI = Constants.COLUMN_SECOND;
 //		final int IN_USE = Constants.COLUMN_THIRD;
 		final int SIZE = Constants.COLUMN_FOURTH;
 		final int BASE_PATH = Constants.COLUMN_FIFTH;
 		final int ICONS_PATH = Constants.COLUMN_SIXTH;
 		final int SCREENS_PATH = Constants.COLUMN_SEVENTH;
 		final int DELTA = Constants.COLUMN_EIGTH;
 		final int APP_HASHID = Constants.COLUMN_NINTH;
 		
 //		final int LOGIN_REPO_HASHID = Constants.COLUMN_FIRST;
 		final int USERNAME = Constants.COLUMN_SECOND;
 		final int PASSWORD = Constants.COLUMN_THIRD;
 
 		String packageName = getAppPackageName(appHashid);
 		
 		String selectRepo = "SELECT R."+Constants.KEY_REPO_HASHID+", R."+Constants.KEY_REPO_URI+", R."+Constants.KEY_REPO_IN_USE+", R."+Constants.KEY_REPO_SIZE
 									+", R."+Constants.KEY_REPO_BASE_PATH+", R."+Constants.KEY_REPO_ICONS_PATH+", R."+Constants.KEY_REPO_SCREENS_PATH
 									+", R."+Constants.KEY_REPO_DELTA+", A."+Constants.KEY_APPLICATION_HASHID
 							+" FROM (SELECT * FROM "+Constants.TABLE_REPOSITORY;
 		if(appAnyVersionInRepo(Constants.APPS_REPO_HASHID, packageName)){
 				selectRepo+=		" WHERE "+Constants.KEY_REPO_HASHID+"="+Constants.APPS_REPO_HASHID+") R "
 							+" NATURAL LEFT JOIN (SELECT "+Constants.KEY_APPLICATION_REPO_HASHID+", "+Constants.KEY_APPLICATION_HASHID
 									+" FROM "+Constants.TABLE_APPLICATION
 									+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'"
 									+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") A";
 		}else{
 				selectRepo+=		" WHERE "+Constants.KEY_REPO_IN_USE+"='"+Constants.DB_TRUE+"') R"
 							+" NATURAL LEFT JOIN (SELECT "+Constants.KEY_APPLICATION_PACKAGE_NAME+", "+Constants.KEY_APPLICATION_REPO_HASHID
 														+", "+Constants.KEY_APPLICATION_HASHID
 												+" FROM "+Constants.TABLE_APPLICATION
 												+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'"
 												+" GROUP BY "+Constants.KEY_APPLICATION_PACKAGE_NAME+") A";
 							
 		}
 		
 		
 		Cursor repoCursor = aptoideAtomicQuery(selectRepo);
 		if(repoCursor == null || repoCursor.isClosed() || repoCursor.getCount()==Constants.EMPTY_INT){
 			if(!repoCursor.isClosed()){
 				repoCursor.close();
 			}
 			return null;		//TODO refactor null object
 		}else{
 			repoCursor.moveToFirst();
 		}
 		
 		String selectLogin = "SELECT * FROM "+Constants.TABLE_LOGIN
 							 +" WHERE "+Constants.KEY_LOGIN_REPO_HASHID+"="+repoCursor.getInt(REPO_HASHID);
 		Cursor loginCursor = aptoideAtomicQuery(selectLogin);
 		
 		ViewRepository repo = new ViewRepository(repoCursor.getString(URI), repoCursor.getInt(SIZE), repoCursor.getString(BASE_PATH)
 												, repoCursor.getString(ICONS_PATH), repoCursor.getString(SCREENS_PATH), repoCursor.getString(DELTA));
 		
 		if( !(loginCursor.getCount() == Constants.EMPTY_INT) ){
 			loginCursor.moveToFirst();
 			ViewLogin login = new ViewLogin(loginCursor.getString(USERNAME),loginCursor.getString(PASSWORD));
 			repo.setLogin(login);
 		}
 		loginCursor.close();
 		
 		ViewAppVersionRepository versionRepo = new ViewAppVersionRepository(repoCursor.getInt(APP_HASHID), repo);
 //		Log.d("Aptoide-getAppAnyVersionRepo", "appRepo: "+repo+" "+selectRepo);
 		
 		repoCursor.close();
 		
 		return versionRepo;
 	}
 	
 	/**
 	 * getAppDisplayInfo, retrieves information about all the versions of the app referenced by the appHashid
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @return ViewDisplayAppVersionsInfo app versions info
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDisplayAppVersionsInfo getAppDisplayInfo(int appHashid){
 		
 		final boolean sdkLargerThan1_6 = (Build.VERSION.SDK_INT > Build.VERSION_CODES.DONUT);
 		
 //		final boolean fullInfoAtomicRetrieval = (!serviceData.isInsertingDataInDb() && Build.VERSION.SDK_INT >= 8);
 		final boolean fullInfoAtomicRetrieval = (Build.VERSION.SDK_INT >= 8);
 //		final boolean fullInfoAtomicRetrieval = false;
 		Log.d("Aptoide-ManagerDatabase", "GetAppDisplayInfo fullInfoAtomicRetrieval? "+fullInfoAtomicRetrieval+" sdkVersion: "+Build.VERSION.SDK_INT);
 		
 		final int APP_FULL_HASHID = Constants.COLUMN_FIRST;
 		final int APP_HASHID = Constants.COLUMN_SECOND;
 		final int VERSION_CODE = Constants.COLUMN_THIRD;
 		final int VERSION_NAME = Constants.COLUMN_FOURTH;
 		final int APP_NAME = Constants.COLUMN_FIFTH;
 		final int REPO_URI = Constants.COLUMN_SIXTH;
 		final int REPO_HASHID = Constants.COLUMN_SEVENTH;
 		
 		final int LIKES = Constants.COLUMN_EIGTH;
 		final int DISLIKES = Constants.COLUMN_NINTH;
 		final int STARS = Constants.COLUMN_TENTH;
 		final int DOWNLOADS = Constants.COLUMN_ELEVENTH;
 		final int DESCRIPTION = Constants.COLUMN_TWELVETH;
 		final int SIZE = Constants.COLUMN_THERTEENTH;
 		
 		final int SCHEDULED;
 		if(fullInfoAtomicRetrieval){
 			SCHEDULED = Constants.COLUMN_FOURTEENTH;
 		}else{
 			SCHEDULED = Constants.COLUMN_EIGTH;
 		}
 		
 		final int INSTALLED_HASHID = Constants.COLUMN_FIRST;
 		final int INSTALLED_VERSION_CODE = Constants.COLUMN_SECOND;
 		final int INSTALLED_VERSION_NAME = Constants.COLUMN_THIRD;
 		final int INSTALLED_NAME = Constants.COLUMN_FOURTH;
 		
 		int installedVersionCode = Constants.EMPTY_INT;
 		
 		boolean installedAlsoAvailable = false;
 		boolean installedAdded = false;
 		
 		String packageName = getAppPackageName(appHashid);
 		if(packageName == null){
 			//TODO throw exception (Unrecognized appHashid)
 			return null;
 		}
 		
 		ViewDisplayAppVersionsInfo appVersions = new ViewDisplayAppVersionsInfo();
 		ViewDisplayAppVersionInfo installedVersion = null;
 		ViewDisplayAppVersionInfo appVersion = null;
 		
 		String selectAppVersions = "SELECT A."+Constants.KEY_APPLICATION_FULL_HASHID+", A."+Constants.KEY_APPLICATION_HASHID+", A."+Constants.KEY_APPLICATION_VERSION_CODE
 											+", A."+Constants.KEY_APPLICATION_VERSION_NAME+", A."+Constants.KEY_APPLICATION_NAME
 											+", R."+Constants.KEY_REPO_URI+", R."+Constants.KEY_REPO_HASHID;
 		if(fullInfoAtomicRetrieval){
 			selectAppVersions +=			", S."+Constants.KEY_STATS_LIKES+", S."+Constants.KEY_STATS_DISLIKES+", S."+Constants.KEY_STATS_STARS
 											+", S."+Constants.KEY_STATS_DOWNLOADS
 											+", E."+Constants.KEY_EXTRA_DESCRIPTION+", D."+Constants.KEY_DOWNLOAD_SIZE;
 		}
 		selectAppVersions +=				", T."+Constants.KEY_APP_TO_INSTALL_SCHEDULED
 									+" FROM "+Constants.TABLE_REPOSITORY+" R"
 									+" NATURAL INNER JOIN "+Constants.TABLE_APPLICATION+" A";
 		if(fullInfoAtomicRetrieval){
 			selectAppVersions += 	" NATURAL LEFT JOIN "+Constants.TABLE_STATS_INFO+" S"
 									+" NATURAL LEFT JOIN "+Constants.TABLE_EXTRA_INFO+" E"
 									+" NATURAL LEFT JOIN "+Constants.TABLE_DOWNLOAD_INFO+" D";
 		}
 		selectAppVersions += 		" NATURAL LEFT JOIN "+Constants.TABLE_APP_TO_INSTALL+" T"
 														
 //									+" NATURAL LEFT JOIN (SELECT "
 //															+Constants.KEY_SCREEN_APP_FULL_HASHID+", COUNT("+Constants.KEY_SCREEN_REMOTE_PATH_TAIL+")"
 //															+" FROM "+Constants.TABLE_SCREEN_INFO+")) C"
 									+" WHERE "+Constants.KEY_REPO_IN_USE+"="+Constants.DB_TRUE
 									+" AND "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'"
 									+" GROUP BY "+Constants.KEY_APPLICATION_VERSION_CODE	// Show only one version even if it's present in multiple repos
 									+" ORDER BY "+Constants.KEY_APPLICATION_VERSION_CODE+" DESC";
 		
 		String selectInstalledAppVersion = " SELECT "+Constants.KEY_APP_INSTALLED_HASHID+", "+Constants.KEY_APP_INSTALLED_VERSION_CODE
 													+", "+Constants.KEY_APP_INSTALLED_VERSION_NAME+", "+Constants.KEY_APP_INSTALLED_NAME
 											+" FROM "+Constants.TABLE_APP_INSTALLED
 											+" WHERE "+Constants.KEY_APPLICATION_PACKAGE_NAME+"='"+packageName+"'";
 		
 //		Log.d("Aptoide-ManagerDatabase", "app info: "+selectAppVersions);//+ " installed : "+selectInstalledAppVersion);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appVersionsCursor = aptoideNonAtomicQuery(selectAppVersions);
 			Cursor installedVersionCursor = aptoideNonAtomicQuery(selectInstalledAppVersion);
 			
 			db.setTransactionSuccessful();
 			db.endTransaction();
 						
 			if(installedVersionCursor.getCount() != Constants.EMPTY_INT){
 				installedVersionCursor.moveToFirst();
 				installedVersionCode = installedVersionCursor.getInt(INSTALLED_VERSION_CODE);
 			
 				installedVersion = new ViewDisplayAppVersionInfo(installedVersionCursor.getString(INSTALLED_NAME), installedVersionCursor.getString(INSTALLED_VERSION_NAME)
 														, installedVersionCode, Constants.EMPTY_INT, installedVersionCursor.getInt(INSTALLED_HASHID), true, false);
 			}
 			installedVersionCursor.close();
 			
 			if(appVersionsCursor.getCount() == Constants.EMPTY_INT){
 				if(installedVersion != null){
 					appVersions.add(installedVersion);
 				}
 			}else{
 				appVersionsCursor.moveToFirst();
 				
 				do{
 					int appVersionCode = appVersionsCursor.getInt(VERSION_CODE);
 					boolean appVersionIsInstalled = (appVersionsCursor.getInt(VERSION_CODE)==installedVersionCode);
 					installedAlsoAvailable = (installedAlsoAvailable?true:appVersionIsInstalled);
 					
 					appVersion = new ViewDisplayAppVersionInfo(appVersionsCursor.getString(APP_NAME), appVersionsCursor.getString(VERSION_NAME)
 							, appVersionCode, appVersionsCursor.getInt(APP_FULL_HASHID)
 							, appVersionsCursor.getInt(APP_HASHID)
 							, appVersionIsInstalled, (sdkLargerThan1_6?isApplicationScheduledToInstall(appVersionsCursor.getInt(APP_HASHID)):!appVersionsCursor.isNull(SCHEDULED)));
 					
 //					Log.d("Aptoide-ManagerDatabase", "GetAppDisplayInfo scheduled isnull? "+appVersionsCursor.isNull(SCHEDULED)+" value: "+appVersionsCursor.getInt(SCHEDULED));
 					
 					
 					if(!installedAlsoAvailable && appVersionCode < installedVersionCode && !installedAdded){
 						appVersions.add(installedVersion);
 						installedAdded = true;
 					}
 					if(!appVersionsCursor.isNull(REPO_URI)){
 						appVersion.setRepoInfo(appVersionsCursor.getInt(REPO_HASHID), appVersionsCursor.getString(REPO_URI));
 					}
 					
 					if(fullInfoAtomicRetrieval){
 						if(!appVersionsCursor.isNull(SIZE)){
 							appVersion.setSize(appVersionsCursor.getInt(SIZE));
 						}
 	
 						if(!appVersionsCursor.isNull(DOWNLOADS)){
 							appVersion.setStats(new ViewDisplayAppVersionStats(appVersionsCursor.getInt(APP_FULL_HASHID), appVersionsCursor.getInt(LIKES)
 																			, appVersionsCursor.getInt(DISLIKES), appVersionsCursor.getFloat(STARS)
 																			, appVersionsCursor.getInt(DOWNLOADS)));
 						}
 						if(!appVersionsCursor.isNull(DESCRIPTION)){
 							ViewDisplayAppVersionExtras extras = new ViewDisplayAppVersionExtras(appVersionsCursor.getInt(APP_FULL_HASHID)
 																								, appVersionsCursor.getString(DESCRIPTION));
 							appVersion.setExtras(extras);
 						}
 					}
 					
 					appVersions.add(appVersion);
 					
 				}while(appVersionsCursor.moveToNext());
 
 				if(installedVersionCode != Constants.EMPTY_INT && !installedAlsoAvailable && !installedAdded){
 					appVersions.add(installedVersion);						
 				}
 			}
 			appVersionsCursor.close();
 			
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		
 		return appVersions;
 	}
 	
 	/**
 	 * getAppDownload, retrieves the best download information for the app referenced by the appHashid
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @return ViewDownload downloadInfo
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public ViewDownload getAppDownload(int appHashid){	//TODO emulator < 2.2 gives wrong query results?!? in those cases use multi-query solution
 //		Log.d("Aptoide-ManagerDatabase", "download appHashid: "+appHashid);
 		
 		final int REMOTE_PATH_TAIL = Constants.COLUMN_FIRST;
 		final int SIZE = Constants.COLUMN_SECOND;
 		final int MD5HASH = Constants.COLUMN_THIRD;
 		final int REMOTE_PATH_BASE = Constants.COLUMN_FOURTH;
 		final int APP_NAME = Constants.COLUMN_FIFTH;
 		final int VERSION_NAME = Constants.COLUMN_SIXTH;
 		final int USERNAME = Constants.COLUMN_SEVENTH;
 		final int PASSWORD = Constants.COLUMN_EIGTH;
 		
 		ViewDownload appDownload = null;
 		
 		String selectAppDownloadInfo = "SELECT "+Constants.KEY_DOWNLOAD_REMOTE_PATH_TAIL+", "+Constants.KEY_DOWNLOAD_SIZE+", "+Constants.KEY_DOWNLOAD_MD5HASH
 											+", "+Constants.KEY_REPO_BASE_PATH+", "+Constants.KEY_APPLICATION_NAME
 											+", "+Constants.KEY_APPLICATION_VERSION_NAME+", "+Constants.KEY_LOGIN_USERNAME+", "+Constants.KEY_LOGIN_PASSWORD
 									+" FROM "+Constants.TABLE_APPLICATION
 									+" NATURAL INNER JOIN "+Constants.TABLE_DOWNLOAD_INFO
 									+" NATURAL INNER JOIN "+Constants.TABLE_REPOSITORY+" R"
 									+" NATURAL LEFT JOIN "+Constants.TABLE_LOGIN
 									+" WHERE "+Constants.KEY_APPLICATION_HASHID+"="+appHashid;
 		if(appInRepo(Constants.APPS_REPO_HASHID, appHashid)){
 			selectAppDownloadInfo +=" AND R."+Constants.KEY_REPO_HASHID+"="+Constants.APPS_REPO_HASHID;
 		} 
 		
 		Log.d("Aptoide-ManagerDatabase", "download: "+selectAppDownloadInfo);
 		
 		try{
 			db.beginTransaction();
 			
 			Cursor appDownloadInfoCursor = aptoideNonAtomicQuery(selectAppDownloadInfo);
 			
 			db.setTransactionSuccessful();
 			db.endTransaction();
 			
 			if(appDownloadInfoCursor.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception (Unrecognized appHashid)
 			}
 			appDownloadInfoCursor.moveToFirst();
 
 			if(appDownloadInfoCursor.isNull(USERNAME)){
 				appDownload = serviceData.getManagerDownloads().prepareApkDownload(appHashid
 									, appDownloadInfoCursor.getString(APP_NAME)+"   v."+appDownloadInfoCursor.getString(VERSION_NAME)
 									, appDownloadInfoCursor.getString(REMOTE_PATH_BASE)+appDownloadInfoCursor.getString(REMOTE_PATH_TAIL)
 									, appDownloadInfoCursor.getInt(SIZE)*Constants.KBYTES_TO_BYTES, appDownloadInfoCursor.getString(MD5HASH));
 			}else{
 				ViewLogin login = new ViewLogin(appDownloadInfoCursor.getString(USERNAME), appDownloadInfoCursor.getString(PASSWORD));
 				
 				appDownload = serviceData.getManagerDownloads().prepareApkDownload(appHashid
 						, appDownloadInfoCursor.getString(APP_NAME)+"   v."+appDownloadInfoCursor.getString(VERSION_NAME)
 						, appDownloadInfoCursor.getString(REMOTE_PATH_BASE)+appDownloadInfoCursor.getString(REMOTE_PATH_TAIL), login
 						, appDownloadInfoCursor.getInt(SIZE)*Constants.KBYTES_TO_BYTES, appDownloadInfoCursor.getString(MD5HASH));
 			}
 			appDownloadInfoCursor.close();
 			
 		}catch (Exception e) {
 			if(db.inTransaction()){
 				db.endTransaction();
 			}
 			// TODO: handle exception
 			e.printStackTrace();
 		}
 		
 		return appDownload;
 	}
 	
 	/**
 	 * getAppVersionDownloadSize, retrieves the download size for the app version referenced by the appFullHashid
 	 * 
 	 * @param int appFullHashid
 	 * 
 	 * @return int size
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	public int getAppVersionDownloadSize(int appFullHashid){
 		
 		final int SIZE = Constants.COLUMN_FIRST;
 		
 		int size = Constants.EMPTY_INT;
 		
 		String selectAppVersionDownloadSize = "SELECT "+Constants.KEY_DOWNLOAD_SIZE
 											+" FROM "+Constants.TABLE_DOWNLOAD_INFO
 											+" WHERE "+Constants.KEY_DOWNLOAD_APP_FULL_HASHID+"="+appFullHashid;
 		
 //		Log.d("Aptoide-ManagerDatabase", "download: "+selectAppDownloadInfo);
 		
 			Cursor cursorAppVersionDownloadSize = aptoideAtomicQuery(selectAppVersionDownloadSize);
 						
 			if(cursorAppVersionDownloadSize.getCount() == Constants.EMPTY_INT){
 					//TODO throw exception (Unrecognized appHashid)
 			}
 			cursorAppVersionDownloadSize.moveToFirst();
 			
 			size = cursorAppVersionDownloadSize.getInt(SIZE);
 			
 			cursorAppVersionDownloadSize.close();
 		
 		return size;
 	}
 	
 	
 	/**
 	 * getAppVersionStats, retrieves the stats for the app version referenced by the appFullHashid
 	 * 
 	 * @param int appFullHashid
 	 * 
 	 * @return ViewDisplayAppVersionStats, stats
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	
 	public ViewDisplayAppVersionStats getAppVersionStats(int appFullHashid){
 		
 		final int LIKES = Constants.COLUMN_FIRST;
 		final int DISLIKES = Constants.COLUMN_SECOND;
 		final int STARS = Constants.COLUMN_THIRD;
 		final int DOWNLOADS = Constants.COLUMN_FOURTH;
 		
 		ViewDisplayAppVersionStats stats = null;
 		
 		String selectAppVersionStats = "SELECT "+Constants.KEY_STATS_LIKES+", "+Constants.KEY_STATS_DISLIKES
 										+", "+Constants.KEY_STATS_STARS+", "+Constants.KEY_STATS_DOWNLOADS
 									+" FROM "+Constants.TABLE_STATS_INFO
 									+" WHERE "+Constants.KEY_STATS_APP_FULL_HASHID+"="+appFullHashid;
 		
 		Cursor cursorAppVersionStats = aptoideAtomicQuery(selectAppVersionStats);
 					
 		if(cursorAppVersionStats.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception (Unrecognized appHashid)
 		}
 		cursorAppVersionStats.moveToFirst();
 		
 		stats = new ViewDisplayAppVersionStats(appFullHashid, cursorAppVersionStats.getInt(LIKES)
 				, cursorAppVersionStats.getInt(DISLIKES), cursorAppVersionStats.getFloat(STARS)
 				, cursorAppVersionStats.getInt(DOWNLOADS));
 		
 		cursorAppVersionStats.close();
 	
 	return stats;
 	}
 	
 	
 	/**
 	 * getAppVersionExtras, retrieves the extras for the app version referenced by the appFullHashid
 	 * 
 	 * @param int appFullHashid
 	 * 
 	 * @return ViewDisplayAppVersionExtras, extras
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */
 	
 	public ViewDisplayAppVersionExtras getAppVersionExtras(int appFullHashid){
 
 		final int DESCRIPTION = Constants.COLUMN_FIRST;
 		
 		ViewDisplayAppVersionExtras extras = null;
 		
 		String selectAppVersionExtras = "SELECT "+Constants.KEY_EXTRA_DESCRIPTION
 									+" FROM "+Constants.TABLE_EXTRA_INFO
 									+" WHERE "+Constants.KEY_EXTRA_APP_FULL_HASHID+"="+appFullHashid;
 		
 		Cursor cursorAppVersionExtras = aptoideAtomicQuery(selectAppVersionExtras);
 					
 		if(cursorAppVersionExtras.getCount() == Constants.EMPTY_INT){
 				//TODO throw exception (Unrecognized appHashid)
 			return null;
 		}
 		cursorAppVersionExtras.moveToFirst();
 		extras = new ViewDisplayAppVersionExtras(appFullHashid, cursorAppVersionExtras.getString(DESCRIPTION));
 		
 		cursorAppVersionExtras.close();
 	
 	return extras;
 	}
 	
 	
 	/**
 	 * getAppPackageName, retrieves the packageName for the app referenced by the appHashid
 	 * 
 	 * @param int appHashid
 	 * 
 	 * @return String packageName
 	 * 
 	 * @author dsilveira
 	 * @since 3.0
 	 * 
 	 */	
 	public String getAppPackageName(int appHashid){
 		final int PACKAGE_NAME = Constants.COLUMN_FIRST;
 		
 		String packageName = null;
 		
 		String selectApp = "SELECT "+Constants.KEY_APP_INSTALLED_PACKAGE_NAME
 									+" FROM "+Constants.TABLE_APP_INSTALLED
 									+" WHERE "+Constants.KEY_APP_INSTALLED_HASHID+"='"+appHashid+"'"
 							+" UNION SELECT "+Constants.KEY_APPLICATION_PACKAGE_NAME
 									+" FROM "+Constants.TABLE_APPLICATION
 									+" WHERE "+Constants.KEY_APPLICATION_HASHID+"='"+appHashid+"'";
 		
 		Cursor cursorApp = aptoideAtomicQuery(selectApp);
 //		Log.d("Aptoide-ManagerDatabase", "package: "+selectApp);
 
 		if(cursorApp == null || cursorApp.isClosed()){
 			return packageName;
 		}
 		
 		if(cursorApp.getCount() == Constants.EMPTY_INT){
 			//TODO raise exception app not installed
 			return null;
 		}
 		
 		cursorApp.moveToFirst();
 		packageName = cursorApp.getString(PACKAGE_NAME);
 		cursorApp.close();
 		
 		return packageName;
 	}
 	
 }
