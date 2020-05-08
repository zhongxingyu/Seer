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
 	
	private ArrayList<String> todos;
 	private ListView todolist;
 	private ArrayAdapter<String> todoAdapter;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_todo_list_manager);
 		
		todos = new ArrayList<String>();
		
		
 		todolist = (ListView)findViewById(R.id.lstTodoItems);
 		todoAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,todos);
 		todolist.setAdapter(todoAdapter);
		// TODO: remove this addings:
		todoAdapter.add("Some todo2");
		todoAdapter.add("Some todo");
		todoAdapter.add("Some todo3");
		todoAdapter.add("Some todo4");
 		
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
    	newTodo.setText("");
 	}
 	
 	public void DeleteSelectedTodoItem()
 	{
 		ListView todoList = (ListView)findViewById(R.id.lstTodoItems);
		int selected = todoList.getSelectedItemPosition();
		if (selected >=0 )
 		{
			todos.remove(selected);				
			todoAdapter.notifyDataSetChanged();
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
