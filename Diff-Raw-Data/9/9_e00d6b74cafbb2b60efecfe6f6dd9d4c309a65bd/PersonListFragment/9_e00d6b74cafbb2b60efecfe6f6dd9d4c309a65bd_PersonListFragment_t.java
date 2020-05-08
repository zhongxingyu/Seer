 package de.capgeti.vereinlager;
 
 import android.app.*;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.SparseBooleanArray;
 import android.view.*;
 import android.widget.*;
 import de.capgeti.vereinlager.db.MemberDataSource;
 import de.capgeti.vereinlager.db.PersonDataSource;
 import de.capgeti.vereinlager.model.Person;
 import de.capgeti.vereinlager.util.CustomCursorAdapter;
 
 import static android.app.Activity.RESULT_OK;
 import static android.view.View.GONE;
 
 /**
  * Author: capgeti
  * Date:   05.09.13 23:11
  */
 public class PersonListFragment extends ListFragment implements AdapterView.OnItemClickListener {
 
     private PersonDataSource personDataSource;
     private MemberDataSource memberDataSource;
     private CustomCursorAdapter adapter;
     private long memberId;
     private ListView listView;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         personDataSource = new PersonDataSource(getActivity());
         memberDataSource = new MemberDataSource(getActivity());
 
         final Cursor memberCursor = memberDataSource.detail(getArguments().getLong("memberId", -1));
         memberCursor.moveToFirst();
         memberId = memberCursor.getLong(0);
 
         final ActionBar actionBar = getActivity().getActionBar();
         actionBar.setTitle(memberCursor.getString(1));
         actionBar.setDisplayHomeAsUpEnabled(true);
         actionBar.setIcon(R.drawable.ic_action_group_white);
         memberCursor.close();
 
         Cursor list = personDataSource.list(memberId);
         adapter = new CustomCursorAdapter(getActivity(), R.layout.double_text_list, list) {
 
             @Override
             protected void fillView(View listItemView, Cursor position) {
                 TextView lineOneView = (TextView) listItemView.findViewById(R.id.text1);
                 TextView lineTwoView = (TextView) listItemView.findViewById(R.id.text2);
                 lineTwoView.setVisibility(GONE);
 
                 String name = position.getString(position.getColumnIndex("name"));
                 String firstName = position.getString(position.getColumnIndex("firstName"));
                 String nickName = position.getString(position.getColumnIndex("nickName"));
                 lineOneView.setText(Person.getPersonName(name, firstName, nickName));
                 lineOneView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_person, 0, 0, 0);
             }
         };
         setListAdapter(adapter);
 
         listView = getListView();
         listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
         final AbsListView.MultiChoiceModeListener multiChoiceModeListener = new AbsListView.MultiChoiceModeListener() {
 
             @Override
             public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                 final int count = listView.getCheckedItemCount();
                 actionMode.setSubtitle(count + " ausgewählt");
                 actionMode.getMenu().findItem(R.id.action_edit).setVisible(count < 2);
             }
 
             @Override
             public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                 MenuInflater inflater = actionMode.getMenuInflater();
                 inflater.inflate(R.menu.person_list_action_menu, menu);
                 actionMode.setTitle("Personen");
                 return true;
             }
 
             @Override
             public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                 return false;
             }
 
             @Override
             public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
                 switch (menuItem.getItemId()) {
                     case R.id.action_edit:
                         final SparseBooleanArray positions = listView.getCheckedItemPositions();
                         int pos = -1;
                         for (int i = 0; i < listView.getCount(); i++) {
                             if (positions.get(i)) {
                                 pos = i;
                                 break;
                             }
                         }
                         final Cursor cursor = adapter.getCursor();
                         cursor.moveToPosition(pos);
 
                         Fragment fragment = new PersonEditFragment();
                         Bundle args = new Bundle();
                         args.putLong("personId", cursor.getLong(cursor.getColumnIndex("id")));
                         fragment.setArguments(args);
                         FragmentManager fragmentManager = getFragmentManager();
                         fragmentManager.beginTransaction()
                                 .replace(R.id.content_frame, fragment)
                                 .addToBackStack("persons")
                                 .commit();
                         getActivity().invalidateOptionsMenu();
 
                         actionMode.finish();
                         return true;
 
                     case R.id.action_delete:
                         new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), android.R.style.Theme_DeviceDefault_Light))
                                 .setTitle("Personen Löschen?")
                                 .setMessage("Möchtest du die Person/en wirklich löschen?\nAlle Verknüpfungen zu Elementen gehen dabei verloren!")
                                 .setPositiveButton("Ja, Okay", new DialogInterface.OnClickListener() {
                                     @Override
                                     public void onClick(DialogInterface dialogInterface, int i) {
                                         final SparseBooleanArray pos = listView.getCheckedItemPositions();
                                         final Cursor adapterCursor = adapter.getCursor();
                                         for (int j = 0; j < listView.getCount(); j++) {
                                             if (pos.get(j)) {
                                                 adapterCursor.moveToPosition(j);
                                                 personDataSource.delete(adapterCursor.getLong(0));
                                             }
                                         }
                                         refreshList();
                                         Toast.makeText(getActivity(), "Person/en gelöscht!", 2).show();
                                         actionMode.finish();
                                     }
                                 })
                                 .setNegativeButton("Ne doch nicht", null).create().show();
                         break;
 
                     case R.id.action_move:
                         startActivityForResult(new Intent(getActivity(), MemberAssignActivity.class), 1);
                         break;
                 }
                 return false;
             }
 
             @Override
             public void onDestroyActionMode(ActionMode actionMode) {
             }
         };
         listView.setMultiChoiceModeListener(multiChoiceModeListener);
         listView.setOnItemClickListener(this);
 
         refreshList();
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         personDataSource.open();
         if (requestCode == 1 && resultCode == RESULT_OK) {
             long memberId = data.getLongExtra("memberId", -1);
             if(memberId == -1) {
                Toast.makeText(getActivity(), "Keine Gruppe ausgewählt!", Toast.LENGTH_SHORT).show();
                 return;
             }
 
             final SparseBooleanArray pos = listView.getCheckedItemPositions();
             final Cursor adapterCursor = adapter.getCursor();
             for (int j = 0; j < listView.getCount(); j++) {
                 if (pos.get(j)) {
                     adapterCursor.moveToPosition(j);
                     personDataSource.setMember(adapterCursor.getLong(adapterCursor.getColumnIndex("id")), memberId);
                 }
             }
             refreshList();
             Toast.makeText(getActivity(), "Person/en verschoben!", 2).show();
         }
     }
 
 
     @Override
     public void onPause() {
         super.onPause();
         personDataSource.close();
         memberDataSource.close();
     }
 
     @Override
     public void onResume() {
         super.onResume();
         personDataSource.open();
         memberDataSource.open();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.person_list_add:
                 Fragment fragment = new PersonCreateFragment();
                 Bundle bundle = new Bundle();
                 bundle.putLong("memberId", memberId);
                 fragment.setArguments(bundle);
                 FragmentManager fragmentManager = getFragmentManager();
                 fragmentManager.beginTransaction()
                         .replace(R.id.content_frame, fragment)
                         .addToBackStack("persons")
                         .commit();
                 getActivity().invalidateOptionsMenu();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
         menuInflater.inflate(R.menu.person_list_menu, menu);
     }
 
     @Override
     public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
         final Cursor cursor = adapter.getCursor();
 
         PersonEditFragment fragment = new PersonEditFragment();
         Bundle args = new Bundle();
         args.putLong("personId", cursor.getLong(0));
         fragment.setArguments(args);
 
         FragmentManager fragmentManager = getFragmentManager();
         fragmentManager.beginTransaction()
                 .replace(R.id.content_frame, fragment)
                 .addToBackStack("persons")
                 .commit();
         getActivity().invalidateOptionsMenu();
     }
 
     private void refreshList() {
         adapter.changeCursor(personDataSource.list(memberId));
     }
 
 }
