 package de.iweinzierl.passsafe.android.activity.list;
 
 import java.util.List;
 
 import android.app.Activity;
 
import android.content.Context;

 import android.os.Bundle;
 
 import android.support.v4.widget.DrawerLayout;
 
import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import de.iweinzierl.passsafe.android.PassSafeApplication;
 import de.iweinzierl.passsafe.android.R;
 import de.iweinzierl.passsafe.android.activity.entry.EntryActivityIntent;
 import de.iweinzierl.passsafe.android.adapter.CategoryListAdapter;
 import de.iweinzierl.passsafe.android.data.DatabaseEntry;
 import de.iweinzierl.passsafe.android.data.DatabaseEntryCategory;
 import de.iweinzierl.passsafe.android.data.SQLiteRepository;
 import de.iweinzierl.passsafe.android.logging.Logger;
 import de.iweinzierl.passsafe.shared.domain.Entry;
 import de.iweinzierl.passsafe.shared.domain.EntryCategory;
 
 public class ListActivity extends Activity implements ListFragment.Callback {
 
     private static final Logger LOGGER = new Logger("ListActivity");
 
     private class DrawerClickListener implements ListView.OnItemClickListener {
 
         @Override
         public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
             EntryCategory category = getCategoryByPosition(position);
 
             if (category == null) {
                 return;
             }
 
             LOGGER.debug("Clicked category: " + category.getTitle());
             showCategory(category);
 
             categoryDrawer.closeDrawers();
         }
     }
 
     private DrawerLayout categoryDrawer;
 
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         LOGGER.debug("Entered ListActivity");
 
         setContentView(R.layout.activity_list);
         getFragmentManager().beginTransaction().replace(R.id.entry_list_container, new ListFragment()).commit();
 
         categoryDrawer = (DrawerLayout) findViewById(R.id.root_layout);
         initializeCategoryList();
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         showAllEntries();
     }
 
     @Override
     public boolean onCreateOptionsMenu(final Menu menu) {
         getMenuInflater().inflate(R.menu.list, menu);
         return true;
     }
 
     @Override
     public void onEntryClicked(final Entry entry) {
         int entryId = ((DatabaseEntry) entry).getId();
         LOGGER.debug("Going to start EntryActivity with entry id: " + entryId);
 
         EntryActivityIntent intent = new EntryActivityIntent(this);
         intent.putEntryId(entryId);
 
         startActivity(intent);
 
     }
 
     private void initializeCategoryList() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

         ListView categoryList = (ListView) findViewById(R.id.category_list);
        categoryList.addHeaderView(layoutInflater.inflate(R.layout.category_drawer_header, null, false));
         categoryList.setAdapter(new CategoryListAdapter(this,
                 ((PassSafeApplication) getApplication()).getRepository()));
         categoryList.setOnItemClickListener(new DrawerClickListener());
     }
 
     protected List<Entry> getEntriesFromBackend() {
         SQLiteRepository repository = ((PassSafeApplication) getApplication()).getRepository();
         return repository.listEntries();
     }
 
     protected List<Entry> getEntriesFromBackend(final EntryCategory category) {
         SQLiteRepository repository = ((PassSafeApplication) getApplication()).getRepository();
         return repository.findEntries(((DatabaseEntryCategory) category).getId());
     }
 
     protected ListView getCategoryList() {
         return (ListView) findViewById(R.id.category_list);
     }
 
     protected ListFragment getListFragment() {
         return (ListFragment) getFragmentManager().findFragmentById(R.id.entry_list_container);
     }
 
     protected EntryCategory getCategoryByPosition(final int position) {
         ListView categoryList = getCategoryList();
         return (EntryCategory) categoryList.getAdapter().getItem(position);
     }
 
     protected void showAllEntries() {
         LOGGER.debug("> showAllEntries");
         getListFragment().showEntries(getEntriesFromBackend());
     }
 
     protected void showCategory(final EntryCategory category) {
         LOGGER.debug(String.format("> showCategory(%s)", category.getTitle()));
         getListFragment().showEntries(getEntriesFromBackend(category));
     }
 }
