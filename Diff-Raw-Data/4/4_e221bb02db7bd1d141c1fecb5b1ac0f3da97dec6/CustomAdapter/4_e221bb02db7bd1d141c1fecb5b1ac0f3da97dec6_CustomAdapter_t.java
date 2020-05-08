 package il.ac.huji.todolist;
 
 import java.util.Date;
 import java.util.List;
 
 import android.graphics.Color;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class CustomAdapter extends ArrayAdapter<Todo> {
 	public CustomAdapter(
 			TodoListManagerActivity activity,
 			List<Todo> todoList) {
 		super(activity, android.R.layout.simple_list_item_1, todoList);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		Todo todo = getItem(position);
 		LayoutInflater inflater = LayoutInflater.from(getContext());
 		View view = inflater.inflate(R.layout.todo_list_row, null);
 		TextView txtTaskTitle = (TextView)view.findViewById(R.id.txtTaskTitle);
 		TextView txtTodoDueDate = (TextView)view.findViewById(R.id.txtTodoDueDate);
 		Date now = new Date();
 		int color = todo.date.before(now) ? Color.RED : Color.BLACK;
 		txtTaskTitle.setText(todo.task);
 		txtTaskTitle.setTextColor(color);
 		txtTodoDueDate.setTextColor(color);
 		if (todo.date == null) {
 			txtTodoDueDate.setText("No due date");
 		} else {
 			txtTodoDueDate.setText(String.format("%02d/%02d/%04d", 
				todo.date.getDate(), 
				todo.date.getMonth()+1, 
 				todo.date.getYear()+1900));
 		}
 		return view;
 	}
 }
