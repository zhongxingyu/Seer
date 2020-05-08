 package il.ac.huji.chores;
 
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 
 public class MyChoresListAdapter extends ArrayAdapter<Chore> {
 
     public MyChoresListAdapter(Context context, List<Chore> chores) {
         super(context, R.layout.my_chores_list_row);
     }
 
     @Override
     public View getView(int position, View convertView, ViewGroup parent) {
         final Chore chore = getItem(position);
         LayoutInflater inflater = LayoutInflater.from(getContext());
         View view = inflater.inflate(R.layout.my_chores_list_row, null);
         TextView choreTitle = (TextView) view.findViewById(R.id.myChoresRowTitle);
         TextView choreDueDate = (TextView)view.findViewById(R.id.myChoresRowDueDate);
 
         choreTitle.setText(chore.getName());
         String choreDueString = DateFormat.getDateInstance(DateFormat.SHORT).format(chore.getDeadline());
         choreDueDate.setText(choreDueString);
         ImageButton editButton = (ImageButton)view.findViewById(R.id.myChoresRowEditButton);
         editButton.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), ChoreCardActivity.class);
                intent.putExtra(Constants.CHORE_CARD_OPEN, chore.getId());
                 v.getContext().startActivity(intent);
             }
         });
         return super.getView(position, convertView, parent);
     }
 }
