 package ch.ffhs.esa.lifeguard;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Application;
 import android.database.sqlite.SQLiteOpenHelper;
 import ch.ffhs.esa.lifeguard.domain.Configurations;
 import ch.ffhs.esa.lifeguard.domain.Contacts;
 import ch.ffhs.esa.lifeguard.persistence.DatabaseHelper;
 import ch.ffhs.esa.lifeguard.persistence.TableGatewayInterface;
 
 /**
  * Lifeguard application object
  * 
  * @author Juerg Gutknecht <juerg.gutknecht@students.ffhs.ch>
  *
  */
 public class Lifeguard extends Application {
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PROPERTIES
 	 */
 	
 	private static SQLiteOpenHelper databaseHelper;
 	
 	public static final String APPLICATION_SETTINGS = "APPLICATION_SETTINGS";
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * INITIALIZATION
 	 */
 	
 	@Override
 	public void onCreate()
 	{
 		super.onCreate();
 		
 		databaseHelper = new DatabaseHelper(this);
 	}
 	
 	
 	/*//////////////////////////////////////////////////////////////////////////
 	 * PUBLIC INTERFACE
 	 */
 	
 	public static SQLiteOpenHelper getDatabaseHelper() {
 		return databaseHelper;
 	}
 	
	public static void setDatabaseHelper(SQLiteOpenHelper helper) {
	    databaseHelper = helper;
	}
	
 	@SuppressWarnings("rawtypes")
 	public static List<TableGatewayInterface> getTableGateways() {
 		List<TableGatewayInterface> gateways = new ArrayList<TableGatewayInterface>();
 		
 		gateways.add(new Contacts(databaseHelper));
 		gateways.add(new Configurations(databaseHelper));
 		
 		return gateways;
 	}
 }
