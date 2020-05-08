 package il.ac.huji.todolist;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
public class TodoListManagerActivity extends Activity {
 
 	private static List<String> todoStrings = new ArrayList<String> ();
 	private ArrayAdapter adapter;
 	private ListView todoList;
     static {
         todoStrings.add("Test!!!");
     }
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_todo_list_manager);
         adapter = new TodoAdapter(this);
         todoList = (ListView) findViewById(R.id.lstTodoItems);
         todoList.setAdapter(adapter);
 	}
 
 	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemAdd:
			// Blah Blah
			break;
		case R.id.menuItemDelete:
			// Other Blah Blah
			break;
		default:
			// Noop
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.todo_list_manager, menu);
 		return true;
 	}
 
 }
