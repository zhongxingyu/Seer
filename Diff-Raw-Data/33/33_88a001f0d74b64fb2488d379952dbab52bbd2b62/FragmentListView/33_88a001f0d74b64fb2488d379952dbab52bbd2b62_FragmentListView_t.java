 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.support.v4.app.ListFragment;
 import android.os.Bundle;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 /**
  * Class displaying the saved notes as a list. The user can click on a note to
  * view or edit it.
  * 
  * @author Linus Karlsson
  * 
  */
 public class FragmentListView extends ListFragment {
 	private String[] groupies = { "Bar n' a bass", "Mushu", "Cristmas I A",
			"*PI", "Bolle", "Kleff", "Bark", "Sol"};
	private List<String> groupiesList = new ArrayList<String>(
			Arrays.asList(groupies));
 	private ArrayAdapter<String> listAdapter;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedState) {
 		// Fill adapter with items from the array.
 		listAdapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_activated_1, groupiesList);
 		setListAdapter(listAdapter);
 		return super.onCreateView(inflater, container, savedState);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
		
		// Make it possible for the user to select multiple items.
 		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		getListView().setMultiChoiceModeListener(new LongPress());
 	}
 
 	/**
 	 * Handles single clicks on the notes. Opens the note of choice in a new
 	 * activity.
 	 */
	@Override
 	public void onListItemClick(ListView parent, View v, int position, long id) {
		// Open the note in edit view.
 	}
 
 	/**
 	 * Private class handling long presses, which forces the action bar to show
 	 * up. Users can also choose multiple notes.
 	 * 
 	 * @author l1nuskarlsson
 	 * 
 	 */
 	private class LongPress implements ListView.MultiChoiceModeListener {
 
 		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
 			// Listen to user input and perform the action of choice.
 			switch (item.getItemId()) {
 			case R.id.remove:
 				removeListItem(); // Delete the selected notes.
 				mode.finish(); // Close the action bar after deletion.
 			default:
 				return false;
 			}

 		}
 
 		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
 			// Display contextual bar to user.
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.multi_select_menu, menu);
 			mode.setTitle("Select notes");
 			return true;
 		}
 
 		public void onDestroyActionMode(ActionMode arg0) {
 			// Nothing to do here.
 		}
 
 		public boolean onPrepareActionMode(ActionMode arg0, Menu arg1) {
 			return true;
 		}
 
 		/**
 		 * Called everytime the state of the list is changed, for example when a
 		 * note is selected. Displays the number of selected notes to the user.
 		 */
 		public void onItemCheckedStateChanged(ActionMode mode, int position,
 				long id, boolean checked) {
 			switch (getListView().getCheckedItemCount()) {
 			case (0):
 				// If no note is selected, leave the subtitle alone.
 				mode.setSubtitle(null);
 				break;
 			case (1):
 				// If one note is selected, show the following text.
 				mode.setSubtitle("One note selected");
 				break;
 			default:
 				// If more than one time are selected, display the number of
 				// selected notes to user.
 				mode.setSubtitle("" + getListView().getCheckedItemCount()
 						+ " notes selected");
 				break;
 			}
 		}
 
 	}
 
	/**
	 * Remove checked notes from the list.
	 */
 	public void removeListItem() {
		// Iterate through the list and remove the selected items from the adapter.
		for (int i = getListView().getCount() - 1; i >= 0; i--) {
 			if (getListView().getCheckedItemPositions().get(i)) {
				listAdapter.remove(listAdapter.getItem(i));
 			}
 		}
		// Update the view.
 		listAdapter.notifyDataSetChanged();
 	}
 }
