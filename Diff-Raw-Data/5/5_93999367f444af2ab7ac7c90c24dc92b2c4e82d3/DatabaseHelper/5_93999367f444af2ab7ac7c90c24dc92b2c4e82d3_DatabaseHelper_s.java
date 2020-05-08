 package com.imaginea.android.sugarcrm.provider;
 
 import android.content.Context;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.net.Uri;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Accounts;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.AccountsColumns;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Contacts;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.ContactsColumns;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Leads;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.LeadsColumns;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.Opportunites;
 import com.imaginea.android.sugarcrm.provider.SugarCRMContent.OpportunitesColumns;
 
 import java.util.HashMap;
 
 /**
  * This class helps open, create, and upgrade the database file.
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 
     private static final String DATABASE_NAME = "sugar_crm.db";
 
     private static final int DATABASE_VERSION = 1;
 
     public static final String ACCOUNTS_TABLE_NAME = "accounts";
 
     public static final String CONTACTS_TABLE_NAME = "contacts";
 
     public static final String LEADS_TABLE_NAME = "leads";
 
     public static final String OPPORTUNITIES_TABLE_NAME = "opportunities";
 
     public static final String MEETINGS_TABLE_NAME = "meetings";
 
     public static final String CALLS_TABLE_NAME = "calls";
 
     private static final String TAG = "DatabaseHelper";
 
     // TODO - replace with database calls - dynamic module generation
     public static final HashMap<String, String> modules = new HashMap<String, String>();
 
     public static final HashMap<String, String[]> moduleProjections = new HashMap<String, String[]>();
 
     public static final HashMap<String, String[]> moduleListSelections = new HashMap<String, String[]>();
 
     public static final HashMap<String, String> moduleSortOrder = new HashMap<String, String>();
 
     public static final HashMap<String, Uri> moduleUris = new HashMap<String, Uri>();
 
     static {
         // modules.put(0, "Accounts");
         // modules.put(1, "Contacts");
         // modules.put(2, "Leads");
         // modules.put(3, "Opportunity");
         // modules.put(4, "Meetings");
         // modules.put(5, "Calls");
 
         moduleProjections.put("Accounts", Accounts.DETAILS_PROJECTION);
         moduleProjections.put("Contacts", Contacts.DETAILS_PROJECTION);
         moduleProjections.put("Leads", Leads.DETAILS_PROJECTION);
        moduleProjections.put("Opportunites", Opportunites.DETAILS_PROJECTION);
         // moduleProjections.put(4, Meetings.DETAILS_PROJECTION );
 
         moduleListSelections.put("Accounts", Accounts.LIST_VIEW_PROJECTION);
         moduleListSelections.put("Contacts", Contacts.LIST_VIEW_PROJECTION);
         moduleListSelections.put("Leads", Leads.LIST_VIEW_PROJECTION);
        moduleListSelections.put("Opportunites", Opportunites.LIST_VIEW_PROJECTION);
 
         moduleSortOrder.put("Accounts", Accounts.DEFAULT_SORT_ORDER);
         moduleSortOrder.put("Contacts", Contacts.DEFAULT_SORT_ORDER);
 
         moduleUris.put("Accounts", Accounts.CONTENT_URI);
         moduleUris.put("Contacts", Contacts.CONTENT_URI);
         moduleUris.put("Leads", Leads.CONTENT_URI);
         moduleUris.put("Opportunities", Opportunites.CONTENT_URI);
     }
 
     DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
     }
 
     @Override
     public void onCreate(SQLiteDatabase db) {
         createAccountsTable(db);
         // TODO remove the drop statements
         dropContactsTable(db);
         createContactsTable(db);
         createLeadsTable(db);
         createOpportunitiesTable(db);
 
     }
 
     void dropAccountsTable(SQLiteDatabase db) {
         db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME);
     }
 
     void dropContactsTable(SQLiteDatabase db) {
         db.execSQL("DROP TABLE IF EXISTS " + CONTACTS_TABLE_NAME);
     }
 
     void dropLeadsTable(SQLiteDatabase db) {
         db.execSQL("DROP TABLE IF EXISTS " + LEADS_TABLE_NAME);
     }
 
     void dropOpportunitiesTable(SQLiteDatabase db) {
         db.execSQL("DROP TABLE IF EXISTS " + OPPORTUNITIES_TABLE_NAME);
     }
 
     @Override
     public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion
                                         + ", which will destroy all old data");
         db.execSQL("DROP TABLE IF EXISTS " + ACCOUNTS_TABLE_NAME);
         onCreate(db);
     }
 
     private static void createAccountsTable(SQLiteDatabase db) {
 
         db.execSQL("CREATE TABLE " + ACCOUNTS_TABLE_NAME + " (" + AccountsColumns.ID
                                         + " INTEGER PRIMARY KEY," + AccountsColumns.BEAN_ID
                                         + " TEXT," + AccountsColumns.NAME + " TEXT,"
                                         + AccountsColumns.EMAIL1 + " TEXT,"
                                         + AccountsColumns.PARENT_NAME + " TEXT,"
                                         + AccountsColumns.PHONE_OFFICE + " TEXT,"
                                         + AccountsColumns.PHONE_FAX + " TEXT," + " UNIQUE("
                                         + AccountsColumns.BEAN_ID + ")" + ");");
     }
 
     private static void createContactsTable(SQLiteDatabase db) {
 
         db.execSQL("CREATE TABLE " + CONTACTS_TABLE_NAME + " (" + ContactsColumns.ID
                                         + " INTEGER PRIMARY KEY," + ContactsColumns.BEAN_ID
                                         + " TEXT," + ContactsColumns.FIRST_NAME + " TEXT,"
                                         + ContactsColumns.LAST_NAME + " TEXT,"
                                         + ContactsColumns.ACCOUNT_NAME + " TEXT,"
                                         + ContactsColumns.PHONE_MOBILE + " TEXT,"
                                         + ContactsColumns.PHONE_WORK + " TEXT,"
                                         + ContactsColumns.EMAIL1 + " TEXT,"
                                         + ContactsColumns.CREATED_BY + " TEXT,"
                                         + ContactsColumns.MODIFIED_BY_NAME + " TEXT," + " UNIQUE("
                                         + ContactsColumns.BEAN_ID + ")" + ");");
     }
 
     private static void createLeadsTable(SQLiteDatabase db) {
 
         db.execSQL("CREATE TABLE " + LEADS_TABLE_NAME + " (" + LeadsColumns.ID
                                         + " INTEGER PRIMARY KEY," + LeadsColumns.BEAN_ID + " TEXT,"
                                         + LeadsColumns.NAME + " TEXT," + LeadsColumns.EMAIL1
                                         + " TEXT," + LeadsColumns.PARENT_NAME + " TEXT,"
                                         + LeadsColumns.PHONE_OFFICE + " TEXT,"
                                         + LeadsColumns.PHONE_FAX + " TEXT," + " UNIQUE("
                                         + LeadsColumns.BEAN_ID + ")" + ");");
     }
 
     private static void createOpportunitiesTable(SQLiteDatabase db) {
 
         db.execSQL("CREATE TABLE " + OPPORTUNITIES_TABLE_NAME + " (" + OpportunitesColumns.ID
                                         + " INTEGER PRIMARY KEY," + OpportunitesColumns.BEAN_ID
                                         + " TEXT," + OpportunitesColumns.NAME + " TEXT,"
                                         + OpportunitesColumns.ACCOUNT_NAME + " TEXT,"
                                         + OpportunitesColumns.AMOUNT + " TEXT,"
                                         + OpportunitesColumns.AMOUNT_USDOLLAR + " TEXT,"
                                         + OpportunitesColumns.ASSIGNED_USER_ID + " TEXT,"
                                         + OpportunitesColumns.ASSIGNED_USER_NAME + " TEXT,"
                                         + OpportunitesColumns.CAMPAIGN_NAME + " TEXT,"
                                         + OpportunitesColumns.CREATED_BY + " TEXT,"
                                         + OpportunitesColumns.CREATED_BY_NAME + " TEXT,"
                                         + OpportunitesColumns.CURRENCY_ID + " TEXT,"
                                         + OpportunitesColumns.CURRENCY_NAME + " TEXT,"
                                         + OpportunitesColumns.CURRENCY_SYMBOL + " TEXT,"
                                         + OpportunitesColumns.DATE_CLOSED + " TEXT,"
                                         + OpportunitesColumns.DATE_ENTERED + " TEXT,"
                                         + OpportunitesColumns.DATE_MODIFIED + " TEXT,"
                                         + OpportunitesColumns.DESCRIPTION + " TEXT,"
                                         + OpportunitesColumns.LEAD_SOURCE + " TEXT,"
                                         + OpportunitesColumns.MODIFIED_BY_NAME + " TEXT,"
                                         + OpportunitesColumns.MODIFIED_USER_ID + " TEXT,"
                                         + OpportunitesColumns.NEXT_STEP + " TEXT,"
                                         + OpportunitesColumns.OPPORTUNITY_TYPE + " TEXT,"
                                         + OpportunitesColumns.PROBABILITY + " TEXT,"
                                         + OpportunitesColumns.SALES_STAGE + " TEXT,"
 
                                         + " UNIQUE(" + OpportunitesColumns.BEAN_ID + ")" + ");");
     }
 
     public static String[] getModuleProjections(String moduleName) {
         return moduleProjections.get(moduleName);
     }
 
     public static String[] getModuleListSelections(String moduleName) {
         return moduleListSelections.get(moduleName);
     }
 
     public static String getModuleSortOrder(String moduleName) {
         return moduleSortOrder.get(moduleName);
     }
 
     public static Uri getModuleUri(String moduleName) {
         return moduleUris.get(moduleName);
     }
 
 }
