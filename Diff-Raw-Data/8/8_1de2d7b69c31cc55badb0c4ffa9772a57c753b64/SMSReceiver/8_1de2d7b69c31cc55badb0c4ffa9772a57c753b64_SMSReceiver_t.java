 package fake.domain.adamlopresto.goshop;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.telephony.SmsMessage;
 import android.widget.Toast;
import fake.domain.adamlopresto.goshop.contentprovider.GoShopContentProvider;
import fake.domain.adamlopresto.goshop.tables.ItemsTable;
 
 public class SMSReceiver extends BroadcastReceiver {
 
 	@Override
 	public void onReceive(Context context, Intent intent) {
 	    Object[] pdus=(Object[])intent.getExtras().get("pdus"); 
 	    StringBuilder text=new StringBuilder(); 
 	    // get sender from first PDU 
 	    
 	    SmsMessage shortMessage;
 	    
 	    for(int i=0;i<pdus.length;i++){ 
 	      shortMessage=SmsMessage.createFromPdu((byte[]) pdus[i]); 
 	      text.append(shortMessage.getDisplayMessageBody()); 
 	    } 	
 	    
 	    String msg = text.toString();
 	    if (!msg.startsWith("GoShop:\n")){
 	    	return;
 	    }
 	    
 	    SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
 	    String[] items = msg.split("\n");
 	    
 	    ContentValues cv = new ContentValues(1);
 	    cv.put(ItemsTable.COLUMN_STATUS, "N");
 	    
 	    ContentValues insertValues = new ContentValues(3);
 	    insertValues.put(ItemsTable.COLUMN_STATUS, "N");
 	    insertValues.put(ItemsTable.COLUMN_LIST, 1);
 	    
 	    String[] args = new String[1];
 	    
 	    db.beginTransaction();
 	    for (int i = 1; i < items.length; i++){
 	    	args[0] = items[i];
 	    	if (db.update(ItemsTable.TABLE, cv, ItemsTable.COLUMN_NAME + "= ?", args) <= 0){
 	    		insertValues.put(ItemsTable.COLUMN_NAME, items[i]);
 	    		db.insert(ItemsTable.TABLE, null, insertValues);
 	    	}
 	    }
 	    db.setTransactionSuccessful();
 	    db.endTransaction();
 	    db.close();
 	    
	    context.getContentResolver().notifyChange(GoShopContentProvider.ITEM_URI, null);
		context.getContentResolver().notifyChange(GoShopContentProvider.ITEM_AISLE_URI, null);
	    
 	    Toast.makeText(context, "Updated list. Marked "+items.length+" items needed.", Toast.LENGTH_LONG).show();
 	}
 
 }
