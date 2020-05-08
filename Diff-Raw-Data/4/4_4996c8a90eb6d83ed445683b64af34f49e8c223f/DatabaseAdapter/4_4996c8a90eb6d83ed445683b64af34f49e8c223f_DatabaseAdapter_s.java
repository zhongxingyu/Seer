 package com.designs_1393.asana;
 
 import com.designs_1393.asana.workspace.*;
 import com.designs_1393.asana.project.*;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import java.lang.Long;
 
 import android.util.Log;
 
 // SQL stuff
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 
 public class DatabaseAdapter
 {
 	/* Database Attribute Declarations */
 	public static final String WORKSPACES_TABLE_NAME    = "workspaces";
 	public static final String WORKSPACES_COL_ID        = "_id";
 	public static final String WORKSPACES_COL_ASANA_ID  = "workspace_id";
 	public static final String WORKSPACES_COL_NAME      = "workspace_name";
 
 	public static final String PROJECTS_TABLE_NAME      = "projects";
 	public static final String PROJECTS_COL_ID          = "_id";
 	public static final String PROJECTS_COL_ASANA_ID    = "project_id";
 	public static final String PROJECTS_COL_ARCHIVED    = "archived";
 	public static final String PROJECTS_COL_CREATED_AT  = "created_at";
 	public static final String PROJECTS_COL_FOLLOWERS   = "followers";
 	public static final String PROJECTS_COL_MODIFIED_AT = "modified_at";
 	public static final String PROJECTS_COL_NAME        = "project_name";
 	public static final String PROJECTS_COL_NOTES       = "project_notes";
 	public static final String PROJECTS_COL_WORKSPACE   = "workspace_id";
 
 	public static final String DATABASE_NAME           = "asana_data";
 	public static final int    DATABASE_VERSION        = 1;
 
 
 	/* Table CREATE Commands */
 	private static final String WORKSPACES_TABLE_CREATE =
 		"CREATE TABLE " +WORKSPACES_TABLE_NAME +" ("
 		+WORKSPACES_COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
 		+WORKSPACES_COL_ASANA_ID +" INTEGER NOT NULL, "
 		+WORKSPACES_COL_NAME +" TEXT NOT NULL);";
 
 	private static final String PROJECTS_TABLE_CREATE =
 		"CREATE TABLE " +PROJECTS_TABLE_NAME +" ("
 		+PROJECTS_COL_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "
 		+PROJECTS_COL_ASANA_ID    +" INTEGER NOT NULL, "
 		+PROJECTS_COL_ARCHIVED    +" INTEGER NOT NULL, "
 		+PROJECTS_COL_CREATED_AT  +" TEXT NOT NULL, "
 		+PROJECTS_COL_FOLLOWERS   +" TEXT NOT NULL, "
 		+PROJECTS_COL_MODIFIED_AT +" TEXT NOT NULL, "
 		+PROJECTS_COL_NAME        +" TEXT NOT NULL, "
 		+PROJECTS_COL_NOTES       +" TEXT NOT NULL, "
 		+PROJECTS_COL_WORKSPACE   +" INTEGER NOT NULL);";
 
 
 	/* Class Member Objects */
 	private static final String TAG = "Asana: DatabaseAdapter";
 
 	private DatabaseHelper DBhelper;
 	private SQLiteDatabase DB;
 
 	private final Context context;
 
 	/** Class constructor.
 	 *  Retains calling application's context, so that it can be used in
 	 *  additional functions.
 	 *  @param callingApplicationsContext  Calling application's context
 	 */
 	public DatabaseAdapter( Context callingApplicationsContext )
 	{
 		this.context = callingApplicationsContext;
 	}
 
 	/* Inner class providing a databse upgrade process. */
 	private static class DatabaseHelper extends SQLiteOpenHelper
 	{
 		/** Class constructor.
 		 *  Retains calling application's context, so that it can be used in
 		 *  additional functions.
 		 *  @param context  Calling application's context
 		 */
 		DatabaseHelper( Context context )
 		{
 			super( context, DATABASE_NAME, null, DATABASE_VERSION );
 		}
 
 		@Override
 		public void onCreate( SQLiteDatabase db )
 		{
 			db.execSQL( WORKSPACES_TABLE_CREATE );
 			db.execSQL( PROJECTS_TABLE_CREATE );
 		}
 
 		@Override
 		public void onUpgrade( SQLiteDatabase db,
 			                   int oldVersion,
 			                   int newVersion )
 		{
 			// nothing required here yet
 		}
 	}
 
 	/**
 	 * Opens the database helper for writing and returns the database adapter.
 	 * @return  Database adapter associated with the database.
 	 */
 	public DatabaseAdapter open() throws SQLException
 	{
 		DBhelper = new DatabaseHelper( this.context );
 		DB = DBhelper.getWritableDatabase();
 		return this;
 	}
 
 	/** Closes the database helper
 	 */
 	public void close()
 	{
 		DBhelper.close();
 	}
 
 	/** Returns a cursor containing every element of the "workspaces" table,
 	 *  sorted either alphabetically or in the order they're in on the website.
 	 *  @param sortAlphabetically  Whether to return the workspaces in
 	 *                             alphabetical order or in the order they
 	 *                             appear on asana.com
 	 *
 	 *  @return                    Cursor containing the row ID, short name,
 	 *                             and Asana workspace ID for each workspace in
 	 *                             the table.
 	 */
 	public Cursor getWorkspaces( boolean sortAlphabetically )
 	{
 		String sorter = WORKSPACES_COL_ASANA_ID;
 		if( sortAlphabetically )
 			sorter = WORKSPACES_COL_NAME;
 
 		return DB.query( WORKSPACES_TABLE_NAME,
 			new String[] {WORKSPACES_COL_ID,
 				WORKSPACES_COL_ASANA_ID,
 				WORKSPACES_COL_NAME},
 			null, null, null, null, sorter );
 	}
 
 	/** Sets the "workspaces" table to the data in the WorkspaceSet.
 	 *  This should be used primarily to inject the WorkspaceSet parsed from
 	 *  {@link AsanaAPI.getWorkspaces()}.  Note that this method deletes the
 	 *  entire contents of the "workspaces" table first, and then replaces them
 	 *  with the data in "workspaces".
 	 *  @param workspaces  WorkspaceSet parsed from Asana's list of workspaces.
 	 *  @return            true if the operation was successful, flase otherwise.
 	 */
 	public boolean setWorkspaces( WorkspaceSet workspaces )
 	{
 		// TODO: Handle rollback if "insert" fails?  Maybe this is possible
 		// with some a conflict clause?
 
 		// delete contents
 		DB.delete( WORKSPACES_TABLE_NAME, null, null );
 
 		ContentValues values;
 
 		// Get array of workspaces from WorkspaceSet
 		Workspace[] workspaceArray = workspaces.getData();
 
 		long insertResult = 0;
 
 		for( int i = 0; i < workspaceArray.length; i++ )
 		{
 			values = new ContentValues();
 
 			values.put( WORKSPACES_COL_ASANA_ID, workspaceArray[i].getID()   );
 			values.put( WORKSPACES_COL_NAME,     workspaceArray[i].getName() );
 
 			insertResult = DB.insert( WORKSPACES_TABLE_NAME, null, values );
 			if( insertResult == -1 )
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Returns a cursor containing all elements of the "projects" table, from a
 	 * specific workspace, sorted either alphabetically or in the order they're
 	 * list in on the website.
 	 * @param archived            Whether to return archived projects or not.
 	 * @param sortAlphabetically  Whether to return the projects in
 	 *                            alphabetical order or not.
 	 * @return                    Cursor containing the row ID, and name for
 	 *                            all projects in the workspace that qualify.
 	 */
 	public Cursor getProjects( long workspaceID, boolean sortAlphabetically )
 	{
 		String sorter = PROJECTS_COL_ID;
 		if( sortAlphabetically )
 			sorter = PROJECTS_COL_NAME;
 
 		String[] cols = new String[]
 			{ PROJECTS_COL_ASANA_ID,
 			  PROJECTS_COL_ID,
 			  PROJECTS_COL_NAME,
 			  PROJECTS_COL_WORKSPACE };
 		String selection       = PROJECTS_COL_WORKSPACE +" = " +workspaceID;
 		Log.i( TAG, "id = " +workspaceID );
 		Log.i( TAG, "id as string = " +String.valueOf(workspaceID) );
 
 		return DB.query( PROJECTS_TABLE_NAME,
 			cols, selection, null, null, null, sorter );
 	}
 
 	/**
 	 * Sets the "projects" table to the data in the ProjectSet.
 	 * This should be used primarily to inject the ProjectSet parsed from
 	 * {@link AsanaAPI.getProjects( long workspaceID )}.  Note that this method
 	 * deletes the entire contents of the "projects" table first, and then
 	 * replaces them with the data in "projects".
 	 * @param projects  ProjectSet parsed from Asana's list of projects for a
 	 *                  specific workspace.
 	 * @return          true if the operation was successful, false otherwise.
 	 */
 	public boolean setProjects( ProjectSet projects )
 	{
 		// TODO: Handle rollback if "insert" fails?  Maybe this is possible
 		// with some a conflict clause?
 
 		// delete contents
 		DB.delete( PROJECTS_TABLE_NAME, null, null );
 
 		ContentValues values;
 
 		// Get array of projects from ProjectSet
 		Project[] projectArray = projects.getData();
 
 		long insertResult = 0;
 
 		for( Project p : projectArray )
 		{
 			//User[] followers = p.getFollowers.getData();
 			//String userIDs = "";
 			//for( User u : followers )
 			//	userIDs += u.getID();
 
 			// build transaction values
 			values = new ContentValues();
 
 			values.put( PROJECTS_COL_ASANA_ID,    p.getID() );
 			values.put( PROJECTS_COL_ARCHIVED,    p.isArchived() ? 1 : 0 );
 			values.put( PROJECTS_COL_CREATED_AT,  p.getCreatedAt() );
 			//values.put( PROJECTS_COL_FOLLOWERS,   userIDs );
 			values.put( PROJECTS_COL_FOLLOWERS, "none" );
 			values.put( PROJECTS_COL_MODIFIED_AT, p.getModifiedAt() );
 			values.put( PROJECTS_COL_NAME,        p.getName() );
 			values.put( PROJECTS_COL_NOTES,       p.getNotes() );
 			values.put( PROJECTS_COL_WORKSPACE,   p.getWorkspace().getID() );
 
 			insertResult = DB.insert( PROJECTS_TABLE_NAME, null, values );
 			if( insertResult == -1 )
 				return false;
 		}
 
 		return true;
 	}
 }
