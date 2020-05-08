 package uk.co.vurt.hakken.processor;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.wmfs.coalesce.csql.Expression;
 import net.wmfs.coalesce.csql.ExpressionException;
 import net.wmfs.coalesce.csql.ExpressionFactory;
 import uk.co.vurt.hakken.domain.job.DataItem;
 import uk.co.vurt.hakken.domain.job.JobDefinition;
 import uk.co.vurt.hakken.domain.task.Page;
 import uk.co.vurt.hakken.domain.task.PageSelector;
 import uk.co.vurt.hakken.domain.task.pageitem.PageItem;
 import uk.co.vurt.hakken.processor.csql.HakkenEvaluationVisitor;
 import uk.co.vurt.hakken.providers.Dataitem;
 import uk.co.vurt.hakken.providers.Job;
 import uk.co.vurt.hakken.providers.Task;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 
 public class JobProcessor {
 
 	private static final String TAG = "JobProcessor";
 	private static final String CURRENT_PAGE_KEY = TAG + "-current_page";
 	private static final String PREVIOUS_PAGE_POSITION_KEY = TAG + "-previous_position";
 	private static final String HISTORY_KEY = TAG + "-history";
 
 	public static final int COLUMN_INDEX_JOB_ID = 0;
 	public static final int COLUMN_INDEX_JOB_REMOTE_ID = 1;
 	public static final int COLUMN_INDEX_JOB_NAME = 2;
 	public static final int COLUMN_INDEX_JOB_TASKDEFINITION_ID = 3;
 	public static final int COLUMN_INDEX_JOB_CREATED = 4;
 	public static final int COLUMN_INDEX_JOB_DUE = 5;
 	public static final int COLUMN_INDEX_JOB_STATUS = 6;
     public static final int COLUMN_INDEX_JOB_GROUP = 7;	
 	public static final int COLUMN_INDEX_JOB_NOTES = 8;
 	public static final int COLUMN_INDEX_JOB_MODIFIED = 9;
     public static final int COLUMN_INDEX_JOB_ADHOC = 10;	
 	public static final int COLUMN_INDEX_SERVER_ERROR = 11;
 
 	private ContentResolver contentResolver;
 	private Cursor cursor;
 	private TaskProcessor taskProcessor;
 	private JobDefinition jobDefinition;
 	private ExpressionFactory expressionFactory = new ExpressionFactory();
 	private HakkenEvaluationVisitor expressionVisitor = new HakkenEvaluationVisitor();
 	
 	private List<Page> pages;
 	private Map<String, Page> pageCache;
 	
 	int currentPagePosition = 0;
 	ArrayList<Integer> pageHistory;
 	int pageHistoryPosition = -1;
 	
 	public JobProcessor(ContentResolver contentResolver, Uri jobUri){
 		Log.d(TAG, "Instantiating with URI: " + jobUri);
 		
 		this.contentResolver = contentResolver;
 		
 		cursor = contentResolver.query(jobUri, Job.Definitions.ALL, null, null, null);
 		
 		if(cursor != null){
 			cursor.moveToFirst();
 
 			jobDefinition = new JobDefinition(
 			        cursor.getLong(COLUMN_INDEX_JOB_ID),
 			        cursor.getString(COLUMN_INDEX_JOB_REMOTE_ID),
 					cursor.getString(COLUMN_INDEX_JOB_NAME), 
 					cursor.getLong(COLUMN_INDEX_JOB_TASKDEFINITION_ID), 
 					new Date(cursor.getLong(COLUMN_INDEX_JOB_CREATED)), 
 					new Date(cursor.getLong(COLUMN_INDEX_JOB_DUE)), 
 					cursor.getString(COLUMN_INDEX_JOB_STATUS), 
 					cursor.getString(COLUMN_INDEX_JOB_NOTES));
 			
 			jobDefinition.setAdhoc(cursor.getInt(COLUMN_INDEX_JOB_ADHOC)>0);
 			jobDefinition.setServerError(cursor.getString(COLUMN_INDEX_SERVER_ERROR));
 	
 			//initialise the TaskProcessor
 			Uri definitionUri = ContentUris.withAppendedId(Task.Definitions.CONTENT_URI, cursor.getInt(COLUMN_INDEX_JOB_TASKDEFINITION_ID));
 			taskProcessor = new TaskProcessor(contentResolver, definitionUri);
 
 			pages = taskProcessor.getPages();
 			pageHistory =  new ArrayList<Integer>();
 			pageHistory.add(currentPagePosition);
 			pageHistoryPosition = 0;
 		}
 		expressionVisitor.setJobProcessor(this);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public JobProcessor(ContentResolver contentResolver, Uri jobUri, Bundle savedState){
 		this(contentResolver, jobUri);
 		if(savedState != null){
 			if(savedState.containsKey(CURRENT_PAGE_KEY)){
 				this.currentPagePosition = savedState.getInt(CURRENT_PAGE_KEY);
 			}
 			
 			if(savedState.containsKey(HISTORY_KEY) && savedState.containsKey(PREVIOUS_PAGE_POSITION_KEY)){
 				this.pageHistory = (ArrayList<Integer>)savedState.getSerializable(HISTORY_KEY);
 				this.pageHistoryPosition = savedState.getInt(PREVIOUS_PAGE_POSITION_KEY);
 			}
 		}
 	}
 	
 	public String getTaskName(){
 		return taskProcessor.getTaskName();
 	}
 	
 	public List<PageItem> getPageItems(){
 		if(pages != null){
 			return getCurrentPage().getItems(); 
 		}else {
 			return null;
 		}
 	}
 	
 	public void saveInstanceState(Bundle outState){
 		outState.putInt(CURRENT_PAGE_KEY, currentPagePosition);
 		outState.putInt(PREVIOUS_PAGE_POSITION_KEY, pageHistoryPosition);
 		outState.putSerializable(HISTORY_KEY, pageHistory);
 	}
 	
 	public String getPageName(){
 		return getCurrentPage().getName();
 	}
 	
 	public String getPageTitle(){
 		return getCurrentPage() != null ? getCurrentPage().getTitle() : "";
 	}
 	
 	public boolean previousPages(){
 		Log.d(TAG, "previousPages()");
 		boolean showPreviousPages = false;
 		if(currentPagePosition > 0){
 			//there are previous pages, but we need to check to see if they are visible
 			String currentPageName = getCurrentPage().getName();
 			for(int pageId: pageHistory){
 				Page page = pages.get(pageId);
 				if(!page.getName().equals(currentPageName)){
 					showPreviousPages = showPreviousPages || isPageVisible(page);
 				}
 			}
 		}
 		return showPreviousPages;
 	}
 	
 	public boolean morePages(){
 		Log.d(TAG, "morePages()");
 		boolean morePages = false;
 		if(currentPagePosition < pages.size()){
 			//there are more pages, but we need to check to see if they are visible
 			for(int i = currentPagePosition + 1; i < pages.size(); i++){
 				morePages = morePages || isPageVisible(pages.get(i));
 			}
 		}
 		return morePages;
 	}
 	
 	public boolean lastPage(){
 		return currentPagePosition + 1 == pages.size();
 	}
 	
 	public void nextPage(){
 		String nextPageName = null;
 		
 		List<PageSelector> nextPages = getCurrentPage().getNextPages();
 		boolean incrementHistory = true;
 		
 		if(nextPages != null && nextPages.size() > 0 ){
 			//evaluate page selectors in turn, to find one that matches
 			for(int i = 0; i < nextPages.size() && nextPageName == null; i++){
 				PageSelector selector = nextPages.get(i);
 				if(selector.getCondition() != null && selector.getCondition().length() > 0 ){
 					try{
						Log.d(TAG, "About to evaluation condition '" + selector.getCondition() + "'");
 						Expression expression = expressionFactory.createCondition(selector.getCondition());
 						expressionVisitor.setExpression(expression);
 						
 						if(expressionVisitor.evaluateCondition()){
 							nextPageName = selector.getPageName();
 						}
 					}catch(ExpressionException ee){
 						Log.e(TAG, "Unable to evaluate page selector condition", ee);
 					}
 				} else {
 					nextPageName = selector.getPageName();
 				}
 			}
 
 			
 			currentPagePosition = getPagePosition(getPage(nextPageName));
 			
 		}else {
 			//otherwise just get the next page in the list.
 			if(morePages()){
 				currentPagePosition++;
 			}
 		}
 		
 		if(incrementHistory){
 			pageHistory.add(currentPagePosition);
 			pageHistoryPosition++;
 		}
 	}
 	
 	public boolean isCurrentPageVisible(){
 		return isPageVisible(getCurrentPage());
 	}
 	
 	private boolean isPageVisible(Page page){
 		boolean visible = true;
 		if("adhoc".equals(getPageVisibility(page)) && !isAdHocJob()){
 			visible = false;
 		}
 		Log.d(TAG, "visibility of page '" + page.getName() + "': " + visible);
 		return visible;
 	}
 	
 	private String getPageVisibility(Page page){
 		String visibility = "always";
 		if(page.getAttributes() != null && 
 				page.getAttributes().containsKey("visibility")){
 			visibility = page.getAttributes().get("visibility");
 		}
 		return visibility;
 	}
 	
 	public boolean evaluateCondition(String condition) throws ExpressionException{
 		Expression expression = expressionFactory.createCondition(condition);
 		expressionVisitor.setExpression(expression);
 		return expressionVisitor.evaluateCondition();
 	}
 	
 	public String evaluateExpression(String expression){
 		String value = null;
 		try {
 			Expression expr = expressionFactory.createExpression(expression);
 			expressionVisitor.setExpression(expr);
 			value = (String)expressionVisitor.evaluateExpression();
 		} catch (ExpressionException e) {
 			Log.e(TAG, "Unable to evaluate expression '" + expression + "'.", e);
 			e.printStackTrace();
 		}
 		return value;
 	}
 	private Page getPage(String name){
 		Page page = null;
 		if(pageCache == null){
 			pageCache = new HashMap<String, Page>();
 		}else if(pageCache.containsKey(name)){
 			page = pageCache.get(name);
 		}
 		
 		if(page == null){
 			//iterate through the list of pages
 			for(Page currentPage: pages){
 				if(currentPage.getName().equals(name)){
 					page = currentPage;
 					pageCache.put(name, page);
 				}
 			}
 		}
 		
 		return page;
 	}
 	
 	private int getPagePosition(Page page){
 		for(int i = 0; i < pages.size(); i++){
 			if(pages.get(i).equals(page)){
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	public void previousPage(){
 		Log.d(TAG, "Previous Page called. pageHistoryPosition: " + pageHistoryPosition);
 		if(pageHistoryPosition > 0){
 			pageHistoryPosition--;
 		}
 		Log.d(TAG, "pageHistoryPosition: " + pageHistoryPosition);
 		currentPagePosition = pageHistory.get(pageHistoryPosition);
 		pageHistory.subList(pageHistoryPosition + 1, pageHistory.size()).clear(); //remove old "next" pages
 	}
 	
 	public Page getCurrentPage(){
 		if(pages != null && currentPagePosition > -1){
 			return pages.get(currentPagePosition); 
 		}else {
 			return null;
 		}
 	}
 	
 	public void finish(){
 		ContentValues values = new ContentValues();
 		values.put(Job.Definitions.STATUS, "COMPLETED");
 		contentResolver.update(Uri.withAppendedPath(Job.Definitions.CONTENT_URI, ""+jobDefinition.getId()) , values, null, null);
 	}
 	
 	public long getJobId(){
 		return jobDefinition.getId();
 	}
 	
 	/*
 	 * Currently, dataItems are persisted straight away to the contentResolver.
 	 * Is this the best thing to do? Yes it's good from a convenience point of 
 	 * view (i.e. we don't need to worry about the application being put to 
 	 * sleep or destroyed) but is it bad in terms of performance?
 	 * 
 	 * Need to do some performance testing to determine if a local dataitem
 	 * cache would be of benefit. 
 	 */
 	public Uri storeDataItem(DataItem dataItem){
 		Uri dataItemUri = null;
 		
 		ContentValues values = new ContentValues();
 		values.put(Dataitem.Definitions.JOB_ID, getJobId());
 		values.put(Dataitem.Definitions.PAGENAME, dataItem.getPageName());
 		values.put(Dataitem.Definitions.NAME, dataItem.getName());
 		values.put(Dataitem.Definitions.TYPE, dataItem.getType());
 		values.put(Dataitem.Definitions.VALUE, dataItem.getValue());
 
 		Cursor cursor = contentResolver
 				.query(Dataitem.Definitions.CONTENT_URI,
 						new String[] { Dataitem.Definitions._ID },
 						Dataitem.Definitions.JOB_ID + "=? "
 								+ "AND " + Dataitem.Definitions.PAGENAME + "=? " 
 								+ "AND " + Dataitem.Definitions.NAME + "=? "
 								+ "AND " + Dataitem.Definitions.TYPE + "=?",
 						new String[] {
 								"" + getJobId(),
 								dataItem.getPageName(),
 								dataItem.getName(),
 								dataItem.getType() },
 						null);
 		int count = 0;
 		if (cursor != null) {
 			cursor.moveToFirst();
 			if (cursor.getCount() > 0) {
 				int dataItemId = cursor.getInt(0);
 				dataItemUri = Uri.withAppendedPath(
 							Dataitem.Definitions.CONTENT_URI,
 							"" + dataItemId);
 				Log.d(TAG,
 						"Updating dataitem: "
 								+ dataItemUri + "     "
 								+ dataItem.getName() + ":"
 								+ dataItem.getValue());
 				count = contentResolver.update(dataItemUri, values, null, null);
 			}
 			cursor.close();
 		}
 		if (count <= 0) {
 			Log.d(TAG, "Saving new dataitem: " + dataItem.getName()
 					+ ":" + dataItem.getValue());
 			dataItemUri = contentResolver.insert(
 					Dataitem.Definitions.CONTENT_URI, values);
 			Log.d(TAG, "Saved URI: " + dataItemUri);
 			
 		}
 		//set the modified flag on the job.
 		values = new ContentValues();
 		jobDefinition.setModified(true);
 		values.put(Job.Definitions.MODIFIED, jobDefinition.isModified());
 		contentResolver.update(Uri.withAppendedPath(Job.Definitions.CONTENT_URI, ""+jobDefinition.getId()) , values, null, null);
 		
 		return dataItemUri;
 	}
 	
 	
 	public DataItem retrieveDataItem(String pageName, String name, String type){
 		Log.d(TAG, "Retrieving DataItem: " + pageName + ":" + name + ":" + type);
 		
 		DataItem dataitem = null;
 		Cursor cursor = contentResolver.query(Dataitem.Definitions.CONTENT_URI,
 				new String[] { Dataitem.Definitions._ID,
 						Dataitem.Definitions.VALUE },
 				Dataitem.Definitions.JOB_ID + "=? " 
 						+ "AND " + Dataitem.Definitions.PAGENAME + "=? " 
 						+ "AND " + Dataitem.Definitions.NAME + "=? " 
 						+ "AND " + Dataitem.Definitions.TYPE + "=?",
 				new String[] { "" + getJobId(), pageName, name, type },
 				null);
 
 		if (cursor != null) {
 			cursor.moveToFirst();
 			if (cursor.getCount() > 0) {
 				dataitem = new DataItem(pageName, name, type, cursor.getString(1));
 			}
 			cursor.close();
 		}
 
 		return dataitem;
 	}
 	
 	public boolean showServerError(){
 		return "SERVER_ERROR".equals(jobDefinition.getStatus());
 	}
 	
 	public String getServerError(){
 		return jobDefinition.getServerError();
 	}
 	
 	public boolean isAdHocJob(){
 		return jobDefinition.isAdhoc();
 	}
 }
