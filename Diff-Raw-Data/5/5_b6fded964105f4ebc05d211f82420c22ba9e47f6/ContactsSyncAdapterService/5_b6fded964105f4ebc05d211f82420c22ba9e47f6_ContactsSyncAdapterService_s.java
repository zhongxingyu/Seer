 package com.gurkensalat.android.xingsync.sync;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.accounts.Account;
 import android.accounts.OperationCanceledException;
 import android.app.Service;
 import android.content.ContentProviderClient;
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SyncResult;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.provider.BaseColumns;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.RawContacts;
 
 import com.googlecode.androidannotations.annotations.Bean;
 import com.googlecode.androidannotations.annotations.EService;
 import com.gurkensalat.android.xingsync.api.ContactsCall;
 import com.gurkensalat.android.xingsync.api.User;
 
 @EService
 public class ContactsSyncAdapterService extends Service
 {
 	private static Logger LOG = LoggerFactory.getLogger(ContactsSyncAdapterService.class);
 
 	@Bean
 	static ContactsCall contactsCall;
 
 	private static ContactSyncAdapter sSyncAdapter = null;
 
 	private static ContentResolver mContentResolver = null;
 
 	public ContactsSyncAdapterService()
 	{
 		super();
 	}
 
 	@Override
 	public IBinder onBind(Intent intent)
 	{
 		IBinder ret = null;
 		ret = getSyncAdapter().getSyncAdapterBinder();
 		return ret;
 	}
 
 	private ContactSyncAdapter getSyncAdapter()
 	{
 		if (sSyncAdapter == null)
 		{
 			sSyncAdapter = new ContactSyncAdapter(this);
 		}
 
 		return sSyncAdapter;
 	}
 
 	static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider,
 	        SyncResult syncResult) throws OperationCanceledException
 	{
 		mContentResolver = context.getContentResolver();
 		LOG.info("performSync: " + account.toString());
 
 		HashMap<String, Long> localContacts = new HashMap<String, Long>();
 		mContentResolver = context.getContentResolver();
 		LOG.info("performSync: " + account.toString());
 
 		// Load the local Xing contacts
 		Uri rawContactUri = RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
 		        .appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type).build();
 		Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, RawContacts.SYNC1 }, null, null, null);
 		while (c1.moveToNext())
 		{
 			long androidId = c1.getLong(0);
 			String xingId = c1.getString(1);
 
 			LOG.info("    found " + androidId + " / '" + xingId + "'");
 
 			localContacts.put(xingId, androidId);
 		}
 
 		// Obtain contact list from Xing server
 		LOG.info("About to obtain contacts");
 		if (contactsCall == null)
 		{
 			LOG.info("ContactsCall not wired!");
 		}
 		else
 		{
 			LOG.info(contactsCall.toString());
 			List<User> contacts = contactsCall.performAndParse();
 			LOG.info(contacts.toString());
 
 			try
 			{
 				ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
 
 				for (User user : contacts)
 				{
 					LOG.info("    About to handle: " + user);
 					if (!(localContacts.containsKey(user.getId())))
 					{
						addContact(account, user.getDisplayName(), user.getId());
 					}
 				}
 
 				if (operationList.size() > 0)
 				{
 					mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
 				}
 			}
 			catch (Exception e1)
 			{
 				LOG.error("While handling additions", e1);
 			}
 		}
 	}
 
 	private static void addContact(Account account, String xingId, String name)
 	{
		LOG.info("Adding contact: '" + name + "', '" + xingId + "'");
 		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
 
 		// Create our RawContact
 		ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
 		builder.withValue(RawContacts.ACCOUNT_NAME, account.name);
 		builder.withValue(RawContacts.ACCOUNT_TYPE, account.type);
 		builder.withValue(RawContacts.SYNC1, xingId);
 		operationList.add(builder.build());
 
 		// Create a Data record of common type 'StructuredName' for our
 		// RawContact
 		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
 		builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
 		builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
 		builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
 		operationList.add(builder.build());
 
 		// Create a Data record of custom type
 		// "vnd.android.cursor.item/vnd.com.gurkensalat.android.xingsync.profile"
 		// to display a link to the Xing profile
 		// TODO replace profile name with resource lookup
 		builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
 		builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
 		builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.com.gurkensalat.android.xingsync.profile");
 		builder.withValue(ContactsContract.Data.DATA1, xingId);
 		builder.withValue(ContactsContract.Data.DATA2, "Xing Profile");
 		builder.withValue(ContactsContract.Data.DATA3, "View profile");
 		operationList.add(builder.build());
 
 		try
 		{
 			mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
 		}
 		catch (Exception e)
 		{
 			LOG.error("Something went wrong during creation! ", e);
 		}
 	}
 }
