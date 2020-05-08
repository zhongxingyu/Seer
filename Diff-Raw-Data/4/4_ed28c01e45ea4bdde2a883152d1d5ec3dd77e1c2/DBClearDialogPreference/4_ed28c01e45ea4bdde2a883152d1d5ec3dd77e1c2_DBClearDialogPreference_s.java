 package ua.in.leopard.androidCoocooAfisha;
 
 import android.content.Context;
 import android.content.DialogInterface;
import android.preference.DialogPreference;
 import android.util.AttributeSet;
 import android.widget.Toast;
 
public class DBClearDialogPreference extends DialogPreference {
 	private final Context myContext;
 
 	public DBClearDialogPreference(Context context, AttributeSet attrs) {
 		super(context, attrs);
 		this.myContext = context;
 	}
 	
 	@Override
 	public void onClick(DialogInterface dialog, int which){
 		if (DialogInterface.BUTTON_POSITIVE == which){
 			DatabaseHelper DatabaseHelperObject = new DatabaseHelper(this.myContext);
 			DatabaseHelperObject.clearAllTables();
 			Toast.makeText(this.myContext, this.myContext.getString(R.string.database_clear_message), Toast.LENGTH_LONG).show();
 		}
 		
 	}
 
 }
