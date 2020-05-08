 /**
  * This file is part of Plingnote.
  * Copyright (C) 2012 Linus Karlsson
  * 
  * Plingnote is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.plingnote;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.support.v4.app.ListFragment;
 import android.util.SparseBooleanArray;
 import android.view.ActionMode;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 
 /**
  * A class displaying a list of notes from a database to the user.
  * 
  * @author Linus Karlsson
  * 
  */
 public class FragmentListView extends ListFragment {
 	private DatabaseHandler db;
 	private NoteAdapter noteAdapter;
 	private List<Note> notes = new ArrayList<Note>();
 	private ActionMode actionBar;
 
 	@Override
 	public void onActivityCreated(Bundle savedState) {
 		super.onActivityCreated(savedState);
 
 		db = DatabaseHandler.getInstance(getActivity());
 
 		// Fill list with data from database
 		refreshNotes();
 
 		// Make it possible for the user to select multiple items.
 		getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
 		getListView().setMultiChoiceModeListener(new LongPress());
 
 		noteAdapter = new NoteAdapter(getActivity(),
				android.R.layout.simple_list_item_activated_1, notes);
 		setListAdapter(noteAdapter);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_listview, container, false);
 	}
 
 	/**
 	 * Handles single clicks on the notes. Opens the note of choice in a new
 	 * activity.
 	 */
 	@Override
 	public void onListItemClick(ListView parent, View v, int position, long id) {
 		Intent editNote = new Intent(getActivity(), ActivityNote.class);
 
 		// Get the row ID of the clicked note.
 		int noteId = notes.get(position).getId();
 		editNote.putExtra(IntentExtra.id.toString(), noteId);
 
 		// Start edit view.
 		startActivity(editNote);
 	}
 
 	/**
 	 * Private class handling long presses, which forces the action bar to show
 	 * up. Users can also choose multiple notes.
 	 * 
 	 * @author Linus Karlsson
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
 			actionBar = mode;
 
 			// Make the mobile vibrate on long click
 			((Vibrator) getActivity()
 					.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(50);
 
 			// Display contextual action bar to user.
 			MenuInflater inflater = mode.getMenuInflater();
 			inflater.inflate(R.menu.multi_select_menu, menu);
 			mode.setTitle("Select notes");
 			return true;
 		}
 
 		public void onDestroyActionMode(ActionMode mode) {
 			// Nothing to do here
 		}
 
 		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
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
 				// If no note is selected, don't set any subtitle.
 				mode.setSubtitle(null);
 				break;
 			case (1):
 				// If one note is selected
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
 	 * Refresh the notes that will later be added to the view.
 	 */
 	public void refreshNotes() {
 
 		// Clear list from previous notes.
 		clearNotes();
 
 		// Fill the list with info from database.
 		for (Note n : db.getNoteList()) {
 			this.addNote(n);
 		}
 
 		// Order notes after when they last were edited.
 		Collections.sort(notes, new NoteComparator());
 	}
 
 	/**
 	 * The number of notes displayed to the user.
 	 * 
 	 * @return number of notes displayed on the screen.
 	 */
 	public int numberOfNotes() {
 		return noteAdapter.getCount();
 	}
 
 	/**
 	 * Remove checked notes from the list.
 	 */
 	public void removeListItem() {
 
 		// Get the positions of all the checked items.
 		SparseBooleanArray checkedItemPositions = getListView()
 				.getCheckedItemPositions();
 
 		// Walk through the notes and delete the checked ones.
 		for (int i = getListView().getCount() - 1; i >= 0; i--) {
 			if (checkedItemPositions.get(i)) {
 				db.deleteNote(notes.get(i).getId());
 			}
 		}
 
 		// Refresh the note list.
 		refreshNotes();
 
 		// Update the adapter.
 		noteAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Refresh notes when returning to the list view.
 	 */
 	@Override
 	public void onResume() {
 		super.onResume();
 		refreshNotes();
 
 		// Update the adapter after loading new notes.
 		noteAdapter.notifyDataSetChanged();
 	}
 
 	/**
 	 * Close the contextual action bar (top menu) when changing to map view.
 	 */
 	@Override
 	public void setUserVisibleHint(boolean isActive) {
 		super.setUserVisibleHint(isActive);
 
 		// Check if current view
 		if (isVisible()) {
 			if (!isActive) {
 				// If user leaves the list view, close the top menu.
 				if (actionBar != null) {
 					actionBar.finish();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Add a note to the listview.
 	 * 
 	 * @param n
 	 *            the note to add
 	 */
 	public void addNote(Note n) {
 		notes.add(n);
 	}
 
 	/**
 	 * Clears the notes in the internal list Should be used if refreshNotes is
 	 * overridden
 	 */
 	public void clearNotes() {
 		notes.clear();
 	}
 
 }
