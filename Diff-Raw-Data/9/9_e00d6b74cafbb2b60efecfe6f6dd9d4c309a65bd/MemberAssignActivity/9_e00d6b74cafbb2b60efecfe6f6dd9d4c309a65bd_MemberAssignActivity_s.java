 package de.capgeti.vereinlager;
 
 import android.app.ActionBar;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.TextView;
 import de.capgeti.vereinlager.db.MemberDataSource;
 import de.capgeti.vereinlager.util.CustomCursorAdapter;
 
 /**
  * Author: capgeti
  * Date:   05.09.13 23:11
  */
 public class MemberAssignActivity extends ListActivity {
     private MemberDataSource memberDataSource;
 
     @Override
     public void onPause() {
         super.onPause();
         memberDataSource.close();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         memberDataSource = new MemberDataSource(this);
 
         final Cursor member = memberDataSource.list();
 
         CustomCursorAdapter adapter = new CustomCursorAdapter(this, R.layout.person_assign_list_item, member) {
 
             @Override
             protected void fillView(final View listItemView, final Cursor position) {
                 TextView lineOneView = (TextView) listItemView.findViewById(R.id.text1);
                 lineOneView.setText(position.getString(position.getColumnIndex("name")));
                 lineOneView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_group, 0, 0, 0);
             }
         };
 
         final ActionBar actionBar = getActionBar();
        actionBar.setTitle("Stimmgruppe wählen");
         actionBar.setIcon(R.drawable.ic_action_labels_white);
         setListAdapter(adapter);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         Intent data = new Intent();
         data.putExtra("memberId", id);
         setResult(RESULT_OK, data);
         finish();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_cancel:
                 setResult(RESULT_CANCELED);
                 finish();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.cancel_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 }
