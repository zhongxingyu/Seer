 package il.ac.huji.todolist;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class TodoListManagerActivity extends Activity {
 	
 	private ListView todolist;
 	private ArrayAdapter<String> todoAdapter;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_todo_list_manager);
 		
		ArrayList<String> todos = new ArrayList<String>();
		// TODO: remove this addings:
		todos.add("Some todo");
		todos.add("Some todo2");
		todos.add("Some todo3");
		todos.add("Some todo4");
 		todolist = (ListView)findViewById(R.id.lstTodoItems);
 		todoAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,todos);
 		todolist.setAdapter(todoAdapter);
		
 		
 	}
 
 	
 	public void AddItemInTodoList()
 	{
 		EditText newTodo = (EditText)findViewById(R.id.edtNewItem);
     	String thestring = newTodo.getText().toString();
     	if (null == thestring || thestring == "" || thestring.length()==0)
     	{
     		return;
     	}
     	
     	todoAdapter.add(thestring);
 	}
 	
 	public void DeleteSelectedTodoItem()
 	{
 		ListView todoList = (ListView)findViewById(R.id.lstTodoItems);
		String selected = (String)todoList.getSelectedItem();
		if (null != selected)
		{
			Log.i("selected",selected);
			todoAdapter.remove(selected);
		}
		else
 		{
			Log.i("Select", "Clear");
			todoAdapter.clear();
 		}
 		
 	}
 	
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.todo_list_manager, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) 
 	{
 		switch (item.getItemId())
 		{
 		case R.id.menuItemAdd:
 			AddItemInTodoList();
 			break;
 		case R.id.menuItemDelete:
 			DeleteSelectedTodoItem();
 			break;
 		default:
 			return super.onOptionsItemSelected(item);	
 		}
 		return true;
 	}
 
 }
