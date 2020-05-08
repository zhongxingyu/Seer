 package rednus.gncandroid;
 
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 /**
  * This class holds all the data for an account spinner and its account type filter.
  *
  * @author gray
  */
 public class AccountSpinnerData {
 	private GNCAndroid app;
 
 	// Two parallel arrays of matching account names and GUIDs
 	private String[] accountNames;
 	private String[] accountGUIDs;
 	
 	// Support for the account type filters on the to/from spinners
 	// These 3 arrays are used as set
 	private CharSequence[] accountTypeKeys;		// The user friendly account type names
 	private String[] accountTypeValues;			// The account type values as they are used in the db
 	// accountType is public so the account filter dialogs can access it directly
 	public boolean[] accountTypes;				// Which types to use in the to account filter
 
 	/**
 	 * Instantiates a new account spinner data.
 	 *
 	 * @param app the app
 	 * @param values the initial account types
 	 */
	public AccountSpinnerData(GNCAndroid app, String values[]) {
 		TreeMap<String, String> accountTypeMapping = app.gncDataHandler.GetAccountTypeMapping();
 		int size = accountTypeMapping.size();
 		
 		accountTypeKeys = new CharSequence[size];
 		accountTypeValues = new String[size];
 		accountTypes = new boolean[size];
 
 		int i=0;
 		for (String key: accountTypeMapping.keySet()) {
 			accountTypeKeys[i] = key;
 			accountTypeValues[i] = accountTypeMapping.get(key);
 			accountTypes[i] = false;
 			i++;
 		}
 		
 		
 		setBitmapFromAccountList(values);
 		constructAccountLists(getAccountListFromBitmap());
 	}
 	
 	/**
 	 * Gets the account guid.
 	 *
 	 * @param pos the position of the requested guid
 	 * @return the account guid
 	 */
 	public String getAccountGUID(int pos) {
 		return accountGUIDs[pos];
 	}
 	
 	/**
 	 * Gets the account names.
 	 *
 	 * @return the account names
 	 */
 	public String[] getAccountNames() {
 		return accountNames;
 	}
 	
 	/**
 	 * Gets the account type keys.
 	 *
 	 * @return the account type keys
 	 */
 	public CharSequence[] getAccountTypeKeys() {
 		return accountTypeKeys;
 	}
 	
 	/**
 	 * Gets the update account names.
 	 *
 	 * @return the update account names
 	 */
 	public String[] getUpdateAccountNames() {
 		constructAccountLists(getAccountListFromBitmap());
 		return accountNames;
 	}
 	
 	/**
 	 * Gets the account guids.
 	 *
 	 * @return the account guids
 	 */
 	public String[] getAccountGUIDs() {
 		return accountGUIDs;
 	}
 	
 	/**
 	 * Construct account lists.
 	 *
 	 * @param filter the account type filter
 	 */
 	private void constructAccountLists(String[] filter) {
 		TreeMap<String, String> accounts = app.gncDataHandler
 				.GetAccountList(filter);
 		accountNames = new String[accounts.size()];
 		accountGUIDs = new String[accounts.size()];
 		accounts.keySet().toArray(accountNames);
 		for (int i = 0; i < accounts.size(); i++)
 			accountGUIDs[i] = accounts.get(accountNames[i]);
 	}
 
 	/**
 	 * Sets the bitmap from account list.
 	 *
 	 * @param values the list of accounts that should be set to true on the account bitmap
 	 */
 	private void setBitmapFromAccountList(String values[]) {
 		for(int i=0;i<accountTypeValues.length;i++) {
 			boolean found = false;
 			for(String v: values)
 				if ( accountTypeValues[i].equals(v))
 					found = true;
 			accountTypes[i] = found;
 		}		
 	}
 	
 	/**
 	 * Gets the account list from bitmap.
 	 *
 	 * @return the account list from bitmap
 	 */
 	private String[] getAccountListFromBitmap() {
 		ArrayList<String> l = new ArrayList<String>();
 		
 		for (int i=0;i<accountTypes.length;i++)
 			if (accountTypes[i] )
 				l.add(accountTypeValues[i]);
 		
 		String[] ret = new String[l.size()];
 		return l.toArray(ret);
 	}
 }
